package com.gpt.product.gpcash.corporate.workflow;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateProcessInstance;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateTaskInstance;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateVariable;
import com.gpt.product.gpcash.corporate.workflow.repository.CorporateProcessInstanceRepository;
import com.gpt.product.gpcash.corporate.workflow.repository.CorporateTaskInstanceRepository;
import com.gpt.product.gpcash.corporate.workflow.repository.CorporateVariableRepository;

public abstract class CorporateStage {
	
	@Autowired
	private CorporateVariableFactory variableFactory;
	
	@Autowired
	protected CorporateVariableRepository variableRepo;
	
	@Autowired
	protected CorporateTaskInstanceRepository taskRepo;

	@Autowired
	protected CorporateProcessInstanceRepository piRepo;
	
	protected CorporateStage defaultTransition;
	protected Map<String, CorporateStage> transitions;
	
	public abstract String getId();
	
	@SuppressWarnings("unchecked")
	public <T> T getVariable(CorporateProcessInstance pi, String name) {
		return (T)variableRepo.findByProcessInstanceAndName(pi, name).getValue();
	}

	public void setVariable(CorporateProcessInstance pi, String name, Object value) {
		CorporateVariable var = variableFactory.createVariable(name, value);
		var.setProcessInstance(pi);
		variableRepo.save(var);
	}
	
	public void updateVariable(CorporateProcessInstance pi, String name, Object value) {
		if(value == null) {
			variableRepo.deleteByProcessInstanceAndName(pi, name);
		} else {
			CorporateVariable prevVar = variableRepo.findByProcessInstanceAndName(pi, name);
			if(prevVar == null) {
				prevVar = variableFactory.createVariable(name, value);
				prevVar.setProcessInstance(pi);
			} else {
				prevVar.setValue(value);
			}
			variableRepo.save(prevVar);
		}
	}
	
	protected void cancelUnfinishedTasks(CorporateProcessInstance pi) {
		taskRepo.cancelUnfinishedTasks(pi.getId());
	}

	protected void createAndAssignTasksToUsers(CorporateProcessInstance pi, List<String> users) {
		Timestamp now = DateUtils.getCurrentTimestamp();
		for(String user : users) {
			CorporateTaskInstance ti = new CorporateTaskInstance();
			ti.setStartDate(now);
			ti.setProcessInstance(pi);
			ti.setStageId(getId());
			ti.setUser(user);
			ti.setCurrApprLv(pi.getCurrApprLv());
			taskRepo.save(ti);
		}
	}
	
	protected void createAndAssignTasksToCorporateUsers(CorporateProcessInstance pi, List<Map<String, String>> users) {
		Timestamp now = DateUtils.getCurrentTimestamp();
		for(Map<String, String> user : users) {
			CorporateTaskInstance ti = new CorporateTaskInstance();
			ti.setStartDate(now);
			ti.setProcessInstance(pi);
			ti.setStageId(getId());
			ti.setUser(user.get("assignedUserId"));
			ti.setCurrApprLv(pi.getCurrApprLv());
			ti.setUserApprovalLvCode(user.get("assignedUserLevelCode"));
			ti.setUserApprovalLvAlias(user.get("assignedUserLevelAlias"));
			ti.setUserApprovalLvName(user.get("assignedUserLevelName"));
			ti.setUserGroupId(user.get("assignedUserGroupId"));
			taskRepo.save(ti);
		}
	}
	
	protected CorporateStage getNextStage(String transition) {
		CorporateStage nextStage = transitions.get(transition);
		assert(nextStage != null);
		return nextStage;
	}
	
	/**
	 * called only once when the flow arrives at this stage
	 * @param pi
	 * @return null if the flow should wait for tasks, or a stage to continue the flow
	 * @throws Exception
	 */
	public CorporateStage execute(CorporateProcessInstance pi) throws BusinessException, ApplicationException {
		return defaultTransition;
	}

	/**
	 * can be called multiple times based on number of task instances created, everytime a task instance is ended, this method is called 
	 * @param ti
	 * @return null if the flow should wait for tasks, or a stage to continue the flow
	 * @throws Exception
	 */
	public CorporateStage execute(CorporateTaskInstance ti) throws BusinessException, ApplicationException {
		return null;
	}

	public void addTransition(String name, CorporateStage s) {
		if(name==null)
			defaultTransition = s;
		else {
			if(transitions == null)
				transitions = new LinkedHashMap<>();
			transitions.put(name, s);
		}
	}
	
	public static class StartStage extends CorporateStage {
		@Override
		public String getId() {
			return "StartStage";
		}
	}
	
	public static class EndStage extends CorporateStage {
		@Override
		public String getId() {
			return "EndStage";
		}

		@Override
		public CorporateStage execute(CorporateProcessInstance pi) throws BusinessException, ApplicationException {
			pi.setEndDate(new Timestamp(System.currentTimeMillis()));
			return null;
		}
		
	}	
	
}
