package com.gpt.product.gpcash.corporate.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.workflow.CorporateWFEngine;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.product.gpcash.approvallevel.model.ApprovalLevelModel;
import com.gpt.product.gpcash.approvallevel.repository.ApprovalLevelRepository;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateProcessInstance;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateTaskInstance;
import com.gpt.product.gpcash.corporate.workflow.repository.CorporateProcessInstanceRepository;
import com.gpt.product.gpcash.corporate.workflow.repository.CorporateTaskInstanceRepository;

public abstract class CorporateProcessDefinition {
	
	protected CorporateStage startStage;
	protected CorporateStage endStage;
	
	protected AutowireCapableBeanFactory beanFactory;
	
	@Autowired
	protected CorporateTaskInstanceRepository taskRepo;
	
	@Autowired
	protected CorporateProcessInstanceRepository piRepo;
	
	@Autowired
	protected IDMUserRepository idmUserRepository;
	
	@Autowired
	protected ApprovalLevelRepository apprLevelRepo;
	
	protected Map<String, CorporateStage> stages;
	
	public CorporateProcessDefinition() {
		startStage = new CorporateStage.StartStage();
		
		endStage = new CorporateStage.EndStage() {
			@Override
			public CorporateStage execute(CorporateProcessInstance pi) throws BusinessException, ApplicationException {
				executeEndStage(pi);
				return super.execute(pi);
			}
		};
	}
	
	@Autowired
	public void setApplicationContext(ApplicationContext appCtx) {
		beanFactory = appCtx.getAutowireCapableBeanFactory();
	}
	
	protected <T> T autowireBeanObject(T target) {
		beanFactory.autowireBean(target);
		return target;
	}
	
	public void setSpecifiParams(CorporateProcessInstance pi, Map<String, Object> processVars) throws BusinessException, ApplicationException {
		String pendingTaskId = (String)processVars.remove(ApplicationConstants.KEY_PT_ID);
		String menuCode = (String)processVars.remove(ApplicationConstants.KEY_MENU_CODE);
		
		assert(pendingTaskId!=null && menuCode!=null);
		
		pi.setId(pendingTaskId);
		pi.setMenuCode(menuCode);
		
		try {
			pi.setListOfApprovalMatrix(getApprovalMatrix(pi));
		} catch (BusinessException e) {
			throw e;
		} catch(Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	protected Map<String, Object> findTasksByProcessInstanceId(CorporateProcessInstance pi) throws Exception {
		List<List<Object>> approvalMatrix = pi.getListOfApprovalMatrix();
		
		Map<String, Object> result = new LinkedHashMap<>();
		List<Map<String, Object>> tasks = new ArrayList<>();
		Object startDate = null;
		List<Tuple> tuple = taskRepo.findActiveTasksByProcessInstanceId(pi.getId());
		for(Tuple t : tuple) {
			Map<String, Object> task = new LinkedHashMap<>();
			task.put("userId", t.get("userId"));
			task.put("userCode", t.get("userCode"));
			task.put("userName", t.get("userName"));
			
			if(startDate == null)
				startDate = t.get("startDate");

			tasks.add(task);
		}
		
		String stageId = pi.getCurrentStageId();
		result.put("stageId", stageId);
		result.put("startDate", startDate);
		
		String approvalLevel = null;
		int approvalLevelRequired = 1;
		int approvalLevelCount = 0;
		if(stageId != null && stageId.equals(ApplicationConstants.WF_STAGE_APPROVE)) {
			approvalLevelRequired = 0;
			for(int i=0;i<approvalMatrix.size();i++) {
				List<Object> approval = approvalMatrix.get(i);
				int required = (int)approval.get(1);
				if(i == pi.getCurrApprLv()) {
					approvalLevelCount = approvalLevelRequired + pi.getCurrApprLvCount();
				}
				approvalLevelRequired += required;
			}
			
			List<Object> approval = approvalMatrix.get(pi.getCurrApprLv());
			String approvalLevelCode = (String)approval.get(0);
			if(approvalLevelCode != null) {
				ApprovalLevelModel approvalLevelModel = apprLevelRepo.findOne(approvalLevelCode);
				if(approvalLevelModel != null)
					approvalLevel = approvalLevelModel.getName();
			}
		}
		
		result.put("approvalLvName", approvalLevel);
		result.put("approvalLvCount", approvalLevelCount);
		result.put("approvalLvRequired", approvalLevelRequired);
		result.put("tasks", tasks);
		
		return result;
	}
	
	protected Map<String, Object> findTasksHistory(CorporateProcessInstance pi) throws Exception {
		List<List<Object>> approvalMatrix = pi.getListOfApprovalMatrix();
		
		Map<String, Object> result = new HashMap<>();
		List<Map<String, Object>> activities = new ArrayList<>();
		result.put("activities", activities);
		
		// add maker info
		IDMUserModel maker = idmUserRepository.findOne(pi.getCreatedBy());
		Map<String, Object> activity = new LinkedHashMap<>();
		activity.put("userCode", maker.getCode());
		activity.put("userId", maker.getUserId());
		activity.put("userName", maker.getName());
		activity.put("activity", getI18NCode(Status.CREATED));
		activity.put("activityDate", pi.getCreatedDate());
		activity.put("approvalLvName", ApplicationConstants.EMPTY_STRING);

		int approvalLevelRequired = 0;
		for(List<Object> approval : approvalMatrix) {
			approvalLevelRequired += (int)approval.get(1);
		}
		
		activity.put("approvalLvCount", 0);
		activity.put("approvalLvRequired", approvalLevelRequired);
		
		activities.add(activity);
		
		List<Tuple> tuple = taskRepo.findTaskHistoryByProcessInstanceId(pi.getId());
		
		int approvalLevelCount = 0;
		boolean setMakerInfo = true;
		for(Tuple t : tuple) {
			activity = new LinkedHashMap<>();
			activity.put("userCode", t.get("userCode"));
			activity.put("userId", t.get("userId"));
			activity.put("userName", t.get("userName"));
			
			Status status = (Status) t.get("status");
			
			activity.put("activity", getI18NCode(status));
			activity.put("activityDate", t.get("endDate"));

			String stageId = (String)t.get("stageId");
			
			int currApprLv = (int)t.get("currApprLv");
			if(stageId.equals(ApplicationConstants.WF_STAGE_APPROVE)) {
				List<Object> approval = approvalMatrix.get(currApprLv);
				String approvalLevelCode = (String)approval.get(0);
				
				ApprovalLevelModel approvalLevelModel = apprLevelRepo.findOne(approvalLevelCode);
				if(approvalLevelModel != null)
					activity.put("approvalLvName", approvalLevelModel.getName());
				
				if(status == Status.APPROVED) {
					approvalLevelCount++;
					if(approvalLevelCount < approvalLevelRequired) {
						activity.put("status", getI18NCode(TransactionStatus.APPROVAL_IN_PROGRESS));
					} else {
						activity.put("status", getI18NCode(TransactionStatus.PENDING_RELEASE));
					}
				}
				
				activity.put("approvalLvCount", approvalLevelCount);
				activity.put("approvalLvRequired", approvalLevelRequired);
				
				if(setMakerInfo) {
					setMakerInfo = false;
					Map<String, Object> makerTaskHistory = activities.get(0);
					makerTaskHistory.put("status", getI18NCode(TransactionStatus.PENDING_APPROVE));
				}
			} else if(stageId.equals(ApplicationConstants.WF_STAGE_RELEASE)) {
				setMakerInfo = false;
				Map<String, Object> prevTaskHistory = activities.get(activities.size() - 1);
				prevTaskHistory.put("status", getI18NCode(TransactionStatus.PENDING_RELEASE));
			}
			activities.add(activity);
		}
		
		if(setMakerInfo) {
			// nobody approve or release the task yet
			if(pi.getCurrentStageId().equals(ApplicationConstants.WF_STAGE_APPROVE)) {
				activity.put("status", getI18NCode(TransactionStatus.PENDING_APPROVE));
			} else if(pi.getCurrentStageId().equals(ApplicationConstants.WF_STAGE_RELEASE)) {
				activity.put("status", getI18NCode(TransactionStatus.PENDING_RELEASE));
			}
		}
		
		if(pi.getEndDate() != null) {
			if(pi.getStatus() == Status.APPROVED || pi.getStatus() == Status.RELEASED) { 
				// process flow has ended with approve or release status
				// last activity is Pending Execute and should not be part of this result
				activities.remove(activities.size() - 1);
			} else if(pi.getStatus() == Status.REJECTED || pi.getStatus() == Status.DECLINED) {
				// update last activity to match last status of ProcessInstance
				activities.remove(activities.size() - 1);
			}
		} else if(!setMakerInfo && pi.getStatus() == Status.APPROVED && pi.getCurrentStageId().equals(ApplicationConstants.WF_STAGE_RELEASE)) {
			// previous activity is not maker activity and we are in releaser stage so the previous state must be pending release
			Map<String, Object> prevTaskHistory = activities.get(activities.size() - 1);
			prevTaskHistory.put("status", getI18NCode(TransactionStatus.PENDING_RELEASE));
		}
		
		return result;
	}
	
	
	public CorporateStage executeTask(CorporateTaskInstance ti) throws BusinessException, ApplicationException {
		CorporateProcessInstance pi = ti.getProcessInstance();
		
		CorporateStage s = stages.get(pi.getCurrentStageId());
		
		assert(s != null) : "Stage id: " + pi.getCurrentStageId() + " not found on process definition: " + getName();
		
		s = s.execute(ti);
		
		taskRepo.save(ti);

		return s;
	}
	
	public CorporateStage start(CorporateProcessInstance pi) {
		return startStage;
	}
	
	/**
	 * TODO: return the i18n code
	 * @param message
	 * @return
	 */
	protected String getI18nFinalStatusCode(Status message) {
		switch(message) {
			case REJECTED:
				return "REJECTED";
			case CANCELED: 
				return "CANCELLED";
			case DECLINED:
				return "DECLINED";
			default:
				return "-";
		}
	}
	
	/**
	 * TODO: return the i18n code
	 * @param message
	 * @return
	 */
	protected String getI18NCode(Object message) {
		if(Status.APPROVED == message)
			return "APPROVE";
		else if(Status.RELEASED == message)
			return "RELEASE";
		else if(Status.EXPIRED == message)
			return "EXPIRE";
		else if(Status.CREATED == message)
			return "CREATE";
		else if(Status.REJECTED == message)
			return "REJECT";
		else if(Status.DECLINED == message)
			return "DECLINE";
		else if(TransactionStatus.APPROVAL_IN_PROGRESS == message)
			return "APPROVAL_IN_PROGRESS";
		else if(TransactionStatus.PENDING_RELEASE == message)
			return "PENDING_RELEASE";
		else if(TransactionStatus.PENDING_APPROVE == message)
			return "PENDING_APPROVE";
		else if(TransactionStatus.PENDING_EXECUTE == message)
			return "PENDING_EXECUTE";
			
		return message.toString();
	}	

	protected void updatePendingTaskActivity(CorporateProcessInstance pi) {
		
	}
	
	public abstract String getName();
	
	public abstract CorporateWFEngine.Type getType();
	
	public abstract int getVersion();
	
	protected abstract void executeEndStage(CorporateProcessInstance pi) throws BusinessException, ApplicationException;
	
	protected abstract List<List<Object>> getApprovalMatrix(CorporateProcessInstance pi) throws BusinessException, ApplicationException;
}
