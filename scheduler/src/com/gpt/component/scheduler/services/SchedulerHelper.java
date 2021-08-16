package com.gpt.component.scheduler.services;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.LockModeType;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.scheduler.model.SchedulerStatusModel;
import com.gpt.component.scheduler.model.SchedulerTriggerModel;
import com.gpt.component.scheduler.repository.SchedulerStatusRepository;
import com.gpt.component.scheduler.repository.SchedulerTriggerRepository;

@Component
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
public class SchedulerHelper {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private SchedulerTriggerRepository schedulerTriggerRepo;
	
	@Autowired
	private SchedulerStatusRepository schedulerStatusRepo;
	
	public boolean updateSchedule(JobExecutionContext jobCtx) {
		Trigger trigger = jobCtx.getTrigger();
		JobDetail jobDetail = jobCtx.getJobDetail();
		
		String triggerId = trigger.getKey().getName();
		
		// check for concurrency by locking the row
		SchedulerTriggerModel triggerModel = schedulerTriggerRepo.getOne(triggerId, LockModeType.PESSIMISTIC_WRITE);
		
		long scheduledFireTime = (long)jobDetail.getJobDataMap().get("nextFireTime");
		long nextFireTime = trigger.getNextFireTime().getTime();
		
		if(logger.isDebugEnabled()) {
			logger.debug("[" + jobDetail.getKey().getName() + "] Scheduled Fire Time : " + (scheduledFireTime != -1 ? new Date(scheduledFireTime).toString() : "None"));
			logger.debug("[" + jobDetail.getKey().getName() + "] Next Fire Time      : " + (triggerModel.getNextExecutionDate() != null ? new Date(triggerModel.getNextExecutionDate().getTime()).toString() : "Now"));
		}
		
		if(triggerModel.getNextExecutionDate() == null || scheduledFireTime >= triggerModel.getNextExecutionDate().getTime()) {
			triggerModel.setNextExecutionDate(new Timestamp(nextFireTime));
			jobDetail.getJobDataMap().put("nextFireTime", nextFireTime);
			return true;
		} else {
			// another server has eecuted this job, skip and schedule for next fire time only
			jobDetail.getJobDataMap().put("nextFireTime", nextFireTime);
			return false;
		}
	}
	
	public void updateStatus(SchedulerStatusModel status) {
		schedulerStatusRepo.save(status);
	}	
}

