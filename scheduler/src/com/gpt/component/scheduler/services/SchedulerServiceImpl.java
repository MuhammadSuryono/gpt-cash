package com.gpt.component.scheduler.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.impl.calendar.BaseCalendar;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.gpt.component.calendar.services.CalendarService;
import com.gpt.component.common.MDCHelper;
import com.gpt.component.common.broadcast.Broadcaster;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.spring.Util;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.scheduler.JobExecutor;
import com.gpt.component.scheduler.model.SchedulerStatusModel;
import com.gpt.component.scheduler.model.SchedulerTaskModel;
import com.gpt.component.scheduler.model.SchedulerTriggerModel;
import com.gpt.component.scheduler.repository.SchedulerStatusRepository;
import com.gpt.component.scheduler.repository.SchedulerTaskRepository;
import com.gpt.component.scheduler.repository.SchedulerTriggerRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;

@Service
public class SchedulerServiceImpl implements SchedulerService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private SchedulerHelper schedulerHelper;
	
	@Autowired
	private SchedulerTriggerRepository schedulerTriggerRepo;
	
	@Autowired
	private SchedulerTaskRepository schedulerTaskRepo;
	
	@Autowired
	private SchedulerStatusRepository schedulerStatusRepo;
	
	@Autowired
	private ApplicationContext appCtx;

	@Autowired
	private Scheduler scheduler;
	
	@Autowired
	private Broadcaster broadcaster;
	
	@Autowired
	private CalendarService calendar;
	
	@PostConstruct
	public void init() throws Exception {
		scheduler.addCalendar("holidayChecking", new BaseCalendar() {
			@Override
			public long getNextIncludedTime(long timeStamp) {
		        java.util.Calendar day = getStartOfDayJavaCalendar(timeStamp);
		        while (isTimeIncluded(day.getTime().getTime()) == false) {
		            day.add(java.util.Calendar.DATE, 1);
		        }

		        return day.getTime().getTime();
			}
			
			@Override
			public boolean isTimeIncluded(long timeStamp) {
				return !calendar.isHoliday(new Date(timeStamp));
			}
		}, false, false);
	}
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<SchedulerTriggerModel> result = schedulerTriggerRepo.search(map, PagingUtils.createPageRequest(map));

			if(ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))){
				resultMap.put("result", setModelToMap(result.getContent(), true));
			}

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public Map<String, Object> searchStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String taskCode = (String) map.get("taskCode");
			Page<SchedulerStatusModel> result = schedulerStatusRepo.findByTaskCode(taskCode, PagingUtils.createPageRequest(map));

			resultMap.put("result", setModelToMapStatus(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMapStatus(List<SchedulerStatusModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (SchedulerStatusModel model : list) {
			resultList.add(setModelToMapStatus(model));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMapStatus(SchedulerStatusModel model) {
		Map<String, Object> map = new HashMap<>();
		
		SchedulerTaskModel task = model.getSchedulerTask();
		
		map.put("statusId", model.getId());
		map.put("taskCode", task.getCode());
		map.put("taskName", task.getName());
		map.put("startDate", ValueUtils.getValue(model.getStartDate()));
		map.put("endDate", ValueUtils.getValue(model.getEndDate()));
		map.put("status", ValueUtils.getValue(model.getStatus()));
		
		return map;
	}

	private List<Map<String, Object>> setModelToMap(List<SchedulerTriggerModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (SchedulerTriggerModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(SchedulerTriggerModel model, boolean isGetDetail) {
		Map<String, Object> map = new HashMap<>();
		
		SchedulerTaskModel task = model.getSchedulerTask();
		
		map.put("schedulerId", model.getId());
		map.put("taskCode", task.getCode());
		map.put("taskName", task.getName());
		map.put("taskDescription", ValueUtils.getValue(task.getDscp()));
		
		if(isGetDetail){
			map.put("service", task.getService());
			map.put("methodName", task.getMethod());
			map.put("parameter", ValueUtils.getValue(task.getParameter()));
			map.put("cron", model.getCronExpr());
			
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
		}
		
		return map;
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String schedulerId = (String) map.get("schedulerId");
			String cron = (String) map.get("cron");
			String parameter = (String) map.get("parameter");
			
			final SchedulerTriggerModel trigger = schedulerTriggerRepo.findOne(schedulerId);
			trigger.setCronExpr(cron);
			
			SchedulerTaskModel task = trigger.getSchedulerTask();
			task.setParameter(parameter);
			trigger.setSchedulerTask(task);
			
			trigger.setNextExecutionDate(null);
			trigger.setUpdatedDate(DateUtils.getCurrentTimestamp());
			trigger.setUpdatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
			schedulerTriggerRepo.save(trigger);
			
			// only load/reload scheduler if commit is successfull
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter(){
				@Override
				public void afterCommit() {
					// broadcast to all server to re-schedule this trigger
					broadcaster.broadcast(SchedulerService.class.getSimpleName(), trigger.getId());
				}
			});
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void loadAll() throws ApplicationException, BusinessException {
		try{
			Page<SchedulerTriggerModel> result = schedulerTriggerRepo.search(new HashMap<>(), null);
			
			for(SchedulerTriggerModel model : result.getContent()) {
				scheduleTask(model, model.getSchedulerTask());
			}
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void scheduleTask(String schedulerId) {
		SchedulerTriggerModel trigger = schedulerTriggerRepo.findOne(schedulerId);
		SchedulerTaskModel task = trigger.getSchedulerTask();
		scheduleTask(trigger, task);
	}
	
	private void scheduleTask(SchedulerTriggerModel triggerModel, SchedulerTaskModel task) {
		try {
			scheduler.deleteJob(new JobKey(task.getCode(), task.getService()));
			
			JobDetail job = JobBuilder.newJob(JobExecutor.class)
					.withIdentity(task.getCode(), task.getService())
					.usingJobData("nextFireTime", triggerModel.getNextExecutionDate() != null ? triggerModel.getNextExecutionDate().getTime() : -1L)
					.build();
			
			CronTriggerImpl cronTrigger = new CronTriggerImpl();
			cronTrigger.setName(triggerModel.getId());
			cronTrigger.setCronExpression(triggerModel.getCronExpr());
			
			if ("Y".equals(triggerModel.getWorkingDayFlag())) {
				cronTrigger.setCalendarName("holidayChecking");				
			}
						
	        scheduler.scheduleJob(job, cronTrigger);
	        
			if(logger.isDebugEnabled()) {
				logger.debug("[" + task.getCode() + "] Next Fire Time: " + cronTrigger.getNextFireTime());
			}
		}catch(Exception e) {
			logger.error("Error on SchedulerTrigger: " + triggerModel.getId() + " for task: " + task.getCode() + ", caused by: " + e.getMessage(), e);
		}
	}

	@Override
	public void execute(String taskCode, String executeBy, boolean isExecuteFromScheduler, String parameter) throws ApplicationException, BusinessException {
		SchedulerTaskModel task = schedulerTaskRepo.findOne(taskCode);
		
		SchedulerStatusModel status = new SchedulerStatusModel();
		status.setSchedulerTask(task);
		status.setCreatedBy(executeBy);
		status.setStartDate(DateUtils.getCurrentTimestamp());
		status.setLogId(MDCHelper.getTraceId());
		
		//jika di execute dr scheduler maka parameter diambil dari table
		//jika di execute dr ui maka parameter diambil input ui
		if(isExecuteFromScheduler) {
			parameter = task.getParameter();
		}
		
		try{
			Util.invokeSpringBean(appCtx, task.getService(), task.getMethod(), parameter);
			status.setStatus("SUCCESS");
		} catch (Throwable e) {
			status.setStatus("FAILED");
			status.setErrorTrace(ValueUtils.bufferString(1024, 0, e.getMessage()));
			logger.error(e.getMessage(), e);
		}
		
		status.setEndDate(DateUtils.getCurrentTimestamp());
		
		schedulerHelper.updateStatus(status);
	}
	
	@Override
	public void executeJob(JobExecutionContext jobCtx) throws ApplicationException, BusinessException {
		if(schedulerHelper.updateSchedule(jobCtx)) {
			JobDetail jobDetail = jobCtx.getJobDetail();
			execute(jobDetail.getKey().getName(), ApplicationConstants.CREATED_BY_SYSTEM, true, null);
		}
		
		JobDetail jobDetail = jobCtx.getJobDetail();
		if(logger.isDebugEnabled())
			logger.debug("[" + jobDetail.getKey().getName() + "] Next Fire Time (New): " + jobCtx.getTrigger().getNextFireTime());
	}
	
}
