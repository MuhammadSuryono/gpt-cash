package com.gpt.component.logging;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class ActivityLogConfiguration {

	@Bean(name = "ActivityLogTaskExecutor")
	public Executor threadPoolTaskExecutor(@Value("${gpcash.activity-log.thread-name}") String threadName, 
			                               @Value("${gpcash.activity-log.thread-core}") int threadCoreSize, 
			                               @Value("${gpcash.activity-log.thread-max}") int threadMax) {
		ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
		pool.setCorePoolSize(threadCoreSize);
		pool.setMaxPoolSize(threadMax);
		pool.setThreadGroupName(threadName);
		pool.setThreadNamePrefix(threadName);
		pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		pool.setWaitForTasksToCompleteOnShutdown(true);
		return pool;
	}
	
}
