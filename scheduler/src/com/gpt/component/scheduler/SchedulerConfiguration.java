package com.gpt.component.scheduler;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import com.gpt.component.calendar.services.CalendarService;
import com.gpt.component.common.broadcast.Broadcaster;
import com.gpt.component.scheduler.services.SchedulerService;

@Configuration
public class SchedulerConfiguration {
	public static final Logger logger = LoggerFactory.getLogger(SchedulerStarter.class);

	@Autowired
	private QuartzJobFactory jobFactory;
	
	@Value("${quartz.threadPool.threadCount:10}")
	private String threadCount;
	
	@Bean("Scheduler")
	public SchedulerFactoryBean createSchedulerFactory() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        factory.setOverwriteExistingJobs(true);		
        factory.setJobFactory(jobFactory);
        
        Properties quartzProperties = new Properties();
        quartzProperties.setProperty("org.quartz.threadPool.threadCount",threadCount);
        
        factory.setQuartzProperties(quartzProperties);
        
        return factory;
	}
	
	@Component
	public static class SchedulerStarter implements ApplicationListener<ContextRefreshedEvent> {
		
		@Autowired
		private SchedulerService service;

		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
			try {
				service.loadAll();
			}catch(Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		@Autowired
		private Broadcaster broadcaster;
		
		private String schedulerBroadcasterId;
		private String calendarBroadcasterId;
		
		@PostConstruct
		public void init() {
			schedulerBroadcasterId = broadcaster.registerListener(SchedulerService.class.getSimpleName(), (String schedulerId) -> {
				service.scheduleTask(schedulerId);
			});
			
			calendarBroadcasterId = broadcaster.registerListener(CalendarService.class.getSimpleName(), (String param) -> {
				try {
					service.loadAll();
				}catch(Exception e) {
					logger.error(e.getMessage(), e);
				}
			});
		}

		@PreDestroy
		public void destroy() {
			if(schedulerBroadcasterId == null)
				broadcaster.removeListener(SchedulerService.class.getSimpleName(), schedulerBroadcasterId);
			if(calendarBroadcasterId == null)
				broadcaster.removeListener(CalendarService.class.getSimpleName(), calendarBroadcasterId);
		}
		
	}
	
	
}
