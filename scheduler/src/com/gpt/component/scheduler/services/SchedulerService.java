package com.gpt.component.scheduler.services;

import java.util.Map;

import org.quartz.JobExecutionContext;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;

public interface SchedulerService {

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	void submit(Map<String, Object> map) throws ApplicationException, BusinessException;

	void loadAll() throws ApplicationException, BusinessException;

	void scheduleTask(String schedulerId);

	void executeJob(JobExecutionContext jobCtx) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchStatus(Map<String, Object> map) throws ApplicationException, BusinessException;

	void execute(String taskCode, String executeBy, boolean isExecuteFromScheduler, String parameter)
			throws ApplicationException, BusinessException;
	
}
