package com.gpt.product.gpcash.workflow;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.workflow.model.ProcessInstance;
import com.gpt.product.gpcash.workflow.model.TaskInstance;
import com.gpt.product.gpcash.workflow.model.Variable;
import com.gpt.product.gpcash.workflow.repository.ProcessInstanceRepository;
import com.gpt.product.gpcash.workflow.repository.TaskInstanceRepository;
import com.gpt.product.gpcash.workflow.repository.VariableRepository;

public abstract class Stage {
	
	@Autowired
	private VariableFactory variableFactory;
	
	@Autowired
	protected VariableRepository variableRepo;
	
	@Autowired
	protected TaskInstanceRepository taskRepo;

	@Autowired
	protected ProcessInstanceRepository piRepo;
	
	protected Stage defaultTransition;
	protected Map<String, Stage> transitions;
	
	public abstract String getId();
	
	@SuppressWarnings("unchecked")
	public <T> T getVariable(ProcessInstance pi, String name) {
		return (T)variableRepo.findByProcessInstanceAndName(pi, name).getValue();
	}

	public void setVariable(ProcessInstance pi, String name, Object value) {
		Variable var = variableFactory.createVariable(name, value);
		var.setProcessInstance(pi);
		variableRepo.save(var);
	}
	
	public void updateVariable(ProcessInstance pi, String name, Object value) {
		if(value == null) {
			variableRepo.deleteByProcessInstanceAndName(pi, name);
		} else {
			Variable prevVar = variableRepo.findByProcessInstanceAndName(pi, name);
			if(prevVar == null) {
				prevVar = variableFactory.createVariable(name, value);
				prevVar.setProcessInstance(pi);
			} else {
				prevVar.setValue(value);
			}
			variableRepo.save(prevVar);
		}
	}
	
	protected void cancelUnfinishedTasks(ProcessInstance pi) {
		taskRepo.cancelUnfinishedTasks(pi.getId());
	}

	protected void createAndAssignTasksToUsers(ProcessInstance pi, List<String> users) {
		Timestamp now = DateUtils.getCurrentTimestamp();
		for(String user : users) {
			TaskInstance ti = new TaskInstance();
			ti.setStartDate(now);
			ti.setProcessInstance(pi);
			ti.setStageId(getId());
			ti.setUser(user);
			ti.setCurrApprLv(pi.getCurrApprLv());
			taskRepo.save(ti);
		}
	}
	
	protected Stage getNextStage(String transition) {
		Stage nextStage = transitions.get(transition);
		assert(nextStage != null);
		return nextStage;
	}
	
	/**
	 * called only once when the flow arrives at this stage
	 * @param pi
	 * @return null if the flow should wait for tasks, or a stage to continue the flow
	 * @throws Exception
	 */
	public Stage execute(ProcessInstance pi) throws BusinessException, ApplicationException {
		return defaultTransition;
	}

	/**
	 * can be called multiple times based on number of task instances created, everytime a task instance is ended, this method is called 
	 * @param ti
	 * @return null if the flow should wait for tasks, or a stage to continue the flow
	 * @throws Exception
	 */
	public Stage execute(TaskInstance ti) throws BusinessException, ApplicationException {
		return null;
	}

	public void addTransition(String name, Stage s) {
		if(name==null)
			defaultTransition = s;
		else {
			if(transitions == null)
				transitions = new LinkedHashMap<>();
			transitions.put(name, s);
		}
	}
	
	public static class StartStage extends Stage {
		@Override
		public String getId() {
			return "StartStage";
		}
	}
	
	public static class EndStage extends Stage {
		@Override
		public String getId() {
			return "EndStage";
		}

		@Override
		public Stage execute(ProcessInstance pi) throws BusinessException, ApplicationException {
			pi.setEndDate(new Timestamp(System.currentTimeMillis()));
			return null;
		}
		
	}	
	
}
