package com.gpt;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.gpt.component.common.modularity.ModuleInfo;
import com.gpt.component.license.LicenseReader;

@Configuration("BaseModuleConfiguration")
public class ModuleConfiguration extends ModuleInfo {
	
	public ModuleConfiguration() {
		super("base", new String[] { ModuleConfiguration.class.getPackage().getName() });
	}
	
	@Autowired
	public void startDefaultScheduler(Scheduler scheduler, @Value("${gpcash.scheduler.default.cron:0 30 23 * * ?}") String cron) throws Exception {
		try {
			LicenseReader.getInstance();
			showLiceseWarningIfNeeded();
		}catch(Exception e) {}
		
		JobDetail job = JobBuilder.newJob(DefaultJob.class)
				.withIdentity("default-1", "default")
				.build();
		
		CronTriggerImpl cronTrigger = new CronTriggerImpl();
		cronTrigger.setName("Default-Scheduler");
		cronTrigger.setCronExpression(cron);
        scheduler.scheduleJob(job, cronTrigger);
	}
	
	private static long DAY_IN_MILLIS = 24L * 3600 * 1000;
	private static long LICENSE_EXPIRATION_WARNING_LIMIT = 30L * DAY_IN_MILLIS;  
	
	private static void showLiceseWarningIfNeeded() throws Exception {
		long now = System.currentTimeMillis();
		LicenseReader.getInstance().getProducts().forEach((productName, product) -> {
			product.getComponents().forEach((compName, comp) -> {
				if(comp.getExpiration() != null) {
					long diff = comp.getExpiration().getTime() - now;
					if(diff > 0 && diff < LICENSE_EXPIRATION_WARNING_LIMIT) {
						long elapsed = diff / DAY_IN_MILLIS;
						System.out.println("*******************************************************************************");
						System.out.printf("WARNING: License for [%s] - [%s] will be expired in less than : %d days%n", 
								productName, compName, elapsed);
						System.out.println("*******************************************************************************");
					}
				}
			});
		});
	}
	
	public static class DefaultJob implements Job {
		
		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			try {
				LicenseReader.checkForUpdate();
				showLiceseWarningIfNeeded();
			}catch(Exception e) {
				// must not print stack trace
			}
		}
	}
	
	
}
