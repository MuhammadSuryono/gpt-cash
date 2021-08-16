package com.gpt.product.gpcash.workflow;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.LockModeType;
import javax.persistence.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.platform.cash.workflow.WFEngine;
import com.gpt.product.gpcash.workflow.model.ProcessInstance;
import com.gpt.product.gpcash.workflow.model.TaskInstance;
import com.gpt.product.gpcash.workflow.model.Variable;
import com.gpt.product.gpcash.workflow.repository.ProcessInstanceRepository;
import com.gpt.product.gpcash.workflow.repository.TaskInstanceRepository;

@Component
public class SimpleWFEngine implements WFEngine {
	
	/**
	 * Hold all available definitions
	 */
	private Map<String, ProcessDefinition> definitions;

	/**
	 * Hold only latest version of each type of definitions
	 * The name is ignored here
	 */
	private Map<Type, ProcessDefinition> latestDefinitions;
	
	@Autowired
	private VariableFactory variableFactory;
	
	@Autowired
	private ProcessInstanceRepository piRepo;
	
	@Autowired
	private TaskInstanceRepository taskRepo;
	
	@Autowired
	public void setDefinitions(List<ProcessDefinition> rawDefinitions) {
		latestDefinitions = new HashMap<>(Type.values().length, 1);
		definitions = new HashMap<>(rawDefinitions.size(), 1);
		rawDefinitions.forEach( p -> {
			definitions.put(p.getName(), p);
			
			ProcessDefinition p2 = latestDefinitions.get(p.getType());
			if(p2 == null || p.getVersion() > p2.getVersion()) {
				latestDefinitions.put(p.getType(), p);
			}
		});
	}
	
	@Override
	public void expireInstance(String processInstanceId) throws BusinessException, ApplicationException {
		ProcessInstance pi = piRepo.findOne(processInstanceId);
		if(pi.getEndDate()!=null) {
			//TODO: use error business exception error code ???
			throw new ApplicationException("Workflow has already ended", null);
		}
		taskRepo.cancelUnfinishedTasks(processInstanceId);
		pi.setStatus(Status.EXPIRED);
		pi.setEndDate(new Timestamp(System.currentTimeMillis()));
		piRepo.save(pi);
	}
	
	@Override
	public void cancelInstance(String processInstanceId) throws BusinessException, ApplicationException {
		ProcessInstance pi = piRepo.findOne(processInstanceId);
		if(pi.getEndDate()!=null) {
			//TODO: use error business exception error code ???
			throw new ApplicationException("Workflow has already ended", null);
		}
		taskRepo.cancelUnfinishedTasks(processInstanceId);
		pi.setStatus(Status.CANCELED);
		pi.setEndDate(new Timestamp(System.currentTimeMillis()));
		piRepo.save(pi);
	}
	
	@Override
	public Map<String, Object> createInstance(Type type, String createdBy, Timestamp createdDate,
			Map<String, Object> processVars) throws BusinessException, ApplicationException {
		try {
			ProcessDefinition pd = latestDefinitions.get(type);
			
			assert(pd != null) : "Configuration exception, can not find process definition of type: " + type;
			
			ProcessInstance pi = new ProcessInstance();
			pd.setSpecifiParams(pi, processVars);
			pi.setCreatedBy(createdBy);
			pi.setCreatedDate(createdDate);
			pi.setProcessDefinition(pd.getName());
			if(!processVars.isEmpty()) {
				Set<Variable> vars = new HashSet<>();
				processVars.forEach( (key, value) -> {
					Variable var = variableFactory.createVariable(key, value);
					var.setProcessInstance(pi);
					vars.add(var);
				});
				pi.setVariables(vars);
			}
	
			piRepo.persist(pi);
			
			Stage s = pd.start(pi);
			while(s != null) {
				pi.setCurrentStageId(s.getId());
				s = s.execute(pi);
			}
	
			piRepo.save(pi);
			
			pd.updatePendingTaskActivity(pi);
			
			String strDateTime = Helper.DATE_TIME_FORMATTER.format(pi.getCreatedDate());
			Map<String, Object> result = new HashMap<>();
			result.put(ApplicationConstants.WF_FIELD_MESSAGE, getWFStatus(pi));
			result.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
			result.put("dateTime", strDateTime);
			return result;
		} catch (ApplicationException e) {
			throw e;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	protected String getWFStatus(ProcessInstance pi) {
		if(pi.getEndDate() != null) {
			// wf has ended
			switch(pi.getStatus()) {
				case APPROVED:
					return "GPT-0200003";
				case REJECTED:
					return "GPT-0200004";
				case RELEASED:
					return "GPT-0200005";
				case CANCELED:
					// canceled by maker
					return "GPT-0200007";
				default:
			}
		} else {
			// wf has pending task
			if(pi.getStatus() == null) {
				// submitted by maker waiting for approval
				return "GPT-0200001";
			}
			switch(pi.getStatus()) {
				case APPROVED:
					return "GPT-0200002";
				case RESUBMITTED:
					return "GPT-0200001";
				case DECLINED:
					return "GPT-0200006";
				default:
			}
		}
		
		return null;
	}
	
	@Override
	public int countActiveTasksByUser(String user) throws BusinessException, ApplicationException {
		return taskRepo.countActiveTasksByUser(user);
	}
	
	@Override
	public Map<String, Object> findTasksByUser(Map<String, Object> map, Type type) throws BusinessException, ApplicationException {
		try {
			Page<Tuple> page = taskRepo.findActiveTasksByUser(map, type, PagingUtils.createPageRequest(map));
			List<Map<String, Object>> tasks = new ArrayList<>();
			
			List<Tuple> ts = page.getContent();
			for(Tuple t : ts) {
				Map<String, Object> task = new LinkedHashMap<>(12, 1);
				task.put("pendingTaskId", t.get("pendingTaskId"));
				task.put("taskId", t.get("taskId"));
				
				String stageId = (String)t.get("stageId");
				
				task.put("stageId", stageId);
				task.put("startDate", t.get("startDate"));
				task.put("approvalLvCount", t.get("currApprLvCount"));
				
				task.put("referenceNo", t.get("referenceNo"));
				task.put("actionBy", t.get("actionBy"));
				task.put("actionByName", t.get("actionByName"));
				task.put("actionDate", t.get("actionDate"));

				task.put("pendingTaskMenuCode", t.get("pendingTaskMenuCode"));
				task.put("pendingTaskMenuName", t.get("pendingTaskMenuName"));
				task.put("uniqueKeyDisplay", ValueUtils.getValue(t.get("uniqueKeyDisplay")));
				task.put("action", t.get("action"));
				
				tasks.add(task);
			}
			
			Map<String, Object> result = new HashMap<>();
			result.put("tasks", tasks);
			PagingUtils.setPagingInfo(result, page);
			return result;
			
		} catch(Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> endUserTask(String taskInstanceId, String userId, String processInstanceId, Status status, Map<String, Object> taskVars) throws BusinessException, ApplicationException {
		ProcessInstance pi = piRepo.getOne(processInstanceId, LockModeType.PESSIMISTIC_WRITE);
		pi.setTrxStatus(null); // just in case
		 
		TaskInstance ti = taskRepo.findByIdAndUserAndProcessInstanceId(taskInstanceId, userId, processInstanceId);
		
		if(ti == null) {
			// there is no task with the specified userId and taskInstanceId in the process instance
			// somebody is trying to approve task that is not belonged to him/her
			throw new BusinessException("GPT-0100038");
		}
		
		if(ti.getEndDate()!=null) // task has ended
			throw new BusinessException("GPT-0100039");
		
		if(taskVars!=null && !taskVars.isEmpty()) {
			Set<Variable> vars = new HashSet<>();
			taskVars.forEach( (key, value) -> {
				Variable var = variableFactory.createVariable(key, value);
				var.setProcessInstance(pi);
				var.setTaskInstance(ti);
				vars.add(var);
			});
			ti.setVariables(vars);
		}
		
		ProcessDefinition pd = definitions.get(pi.getProcessDefinition());
		
		ti.setStatus(status);
		
		ti.setEndDate(DateUtils.getCurrentTimestamp());

		pi.setUpdatedBy(ti.getUser());
		pi.setUpdatedDate(ti.getEndDate());
		pi.setStatus(ti.getStatus());
		
		Stage s = pd.executeTask(ti);
		while(s != null) {
			pi.setCurrentStageId(s.getId());
			s = s.execute(pi);
		}

		piRepo.save(pi);

		pd.updatePendingTaskActivity(pi);
		
		String strDateTime = Helper.DATE_TIME_FORMATTER.format(ti.getEndDate());
		Map<String, Object> result = new HashMap<>();
		result.put(ApplicationConstants.WF_FIELD_MESSAGE, getWFStatus(pi));
		result.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
		result.put("dateTime", strDateTime);
		return result;
	}
			
	 @Override
	public Map<String, Object> findTasksByProcessInstanceId(String processInstanceId)
			throws BusinessException, ApplicationException {
		try {
			ProcessInstance pi = piRepo.findOne(processInstanceId);
			return definitions.get(pi.getProcessDefinition()).findTasksByProcessInstanceId(pi);
		}catch(Exception e) {
			throw new ApplicationException(e.getMessage(), e);
		}
	}
	 
	 @Override
		public Map<String, Object> findTasksHistory(String processInstanceId) throws BusinessException, ApplicationException {
			try {
				ProcessInstance pi = piRepo.findOne(processInstanceId);
				return definitions.get(pi.getProcessDefinition()).findTasksHistory(pi);
			}catch(Exception e) {
				throw new ApplicationException(e.getMessage(), e);
			}
		} 
}
