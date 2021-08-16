package com.gpt.component.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.gpt.component.common.MDCHelper;
import com.gpt.component.scheduler.services.SchedulerService;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class JobExecutor implements Job {
	private static final Logger logger = LoggerFactory.getLogger(JobExecutor.class);
	
	@Autowired
	private SchedulerService jobTarget;
	
	public void execute(JobExecutionContext jobCtx) throws JobExecutionException {
		try {
			MDCHelper.initialize();
			JobDetail jobDetail = jobCtx.getJobDetail();
			MDCHelper.put("controller", "Scheduler_" + jobDetail.getKey().getName());
			jobTarget.executeJob(jobCtx);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			MDCHelper.clearMDCData();
		}
	}
}	