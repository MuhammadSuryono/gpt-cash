package com.gpt.product.gpcash.corporate.workflow.definition;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.platform.cash.workflow.CorporateWFEngine.Type;
import com.gpt.product.gpcash.approvallevel.model.ApprovalLevelModel;
import com.gpt.product.gpcash.corporate.approvalmatrix.model.CorporateApprovalMatrixDetailModel;
import com.gpt.product.gpcash.corporate.approvalmatrix.services.CorporateApprovalMatrixService;
import com.gpt.product.gpcash.corporate.authorizedlimitscheme.model.AuthorizedLimitSchemeModel;
import com.gpt.product.gpcash.corporate.authorizedlimitscheme.repository.AuthorizedLimitSchemeRepository;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.product.gpcash.corporate.workflow.CorporateProcessDefinition;
import com.gpt.product.gpcash.corporate.workflow.definition.stage.cmsfouser.ApproverStage;
import com.gpt.product.gpcash.corporate.workflow.definition.stage.cmsfouser.MakerStage;
import com.gpt.product.gpcash.corporate.workflow.definition.stage.cmsfouser.ReleaserStage;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateProcessInstance;

@Component
public class CMSFOUser extends CorporateProcessDefinition {

	@Autowired
	protected CorporateApprovalMatrixService approvalMatrixService;
	
	@Autowired
	protected CorporateUserPendingTaskService pendingTaskService;
	
	@Autowired
	protected AuthorizedLimitSchemeRepository apprLimitSchemeRepo;
	
	@PostConstruct
	public void init() {
		ApproverStage approverStage = autowireBeanObject(new ApproverStage());
		ReleaserStage releaserStage = autowireBeanObject(new ReleaserStage());
		MakerStage makerStage = autowireBeanObject(new MakerStage());
		
		// construct the flow
		startStage.addTransition(null, approverStage);
		approverStage.addTransition(Status.DECLINED.name(), makerStage);
		approverStage.addTransition(Status.REJECTED.name(), endStage);
		approverStage.addTransition(Status.APPROVED.name(), releaserStage);
		
		makerStage.addTransition(Status.CANCELED.name(), endStage);
		makerStage.addTransition(Status.RESUBMITTED.name(), approverStage);
		
		releaserStage.addTransition(null, endStage);
		
		stages = new HashMap<>(3, 1);
		stages.put(approverStage.getId(), approverStage);
		stages.put(makerStage.getId(), makerStage);
		stages.put(releaserStage.getId(), releaserStage);
	}
	
	@Override
	public String getName() {
		return "CMSFOUser";
	}
	
	@Override
	public Type getType() {
		return Type.CorporateUser;
	}

	@Override
	public int getVersion() {
		return 1;
	}
	
	@Override
	protected List<List<Object>> getApprovalMatrix(CorporateProcessInstance pi) throws BusinessException, ApplicationException {
		BigDecimal transactionAmount = pi.getApprovalLimit();
		boolean isNonTransaction = ApplicationConstants.APPROVAL_MATRIX_NON_TRANSACTION.equals(transactionAmount.toPlainString());
		
		List<CorporateApprovalMatrixDetailModel> levels = approvalMatrixService.getApprovalMatrixDetailForWorkflow(pi.getMenuCode(), pi.getCorporateId());
		
		if(levels.size() == 0) {
			throw new BusinessException("GPT-0100079");
		}
		
		BigDecimal amountLimitChoosen = null;
		List<List<Object>> approvalMatrix = new ArrayList<>(levels.size());
		for(CorporateApprovalMatrixDetailModel level : levels) {
			BigDecimal amountLimit = level.getCorporateApprovalMatrixSub().getLongAmountLimit();
			CorporateUserGroupModel userGroup = level.getCorporateUserGroup();
			String userGroupId = null;
			if(userGroup != null){
				userGroupId = userGroup.getId();
			}
			
			if(isNonTransaction){
				if(ApplicationConstants.APPROVAL_MATRIX_NON_TRANSACTION.equals(amountLimit.toPlainString())){
					AuthorizedLimitSchemeModel schemeModel = level.getAuthorizedLimitScheme();
					approvalMatrix.add(Arrays.asList(schemeModel!=null ? schemeModel.getApprovalLevel().getCode() : null, level.getNoUser(), level.getUserGroupOption(), userGroupId, schemeModel!=null ? schemeModel.getId() : null));
				}
			} else {
				if(amountLimit.compareTo(transactionAmount) >= 0){
					if (amountLimitChoosen == null || amountLimitChoosen.compareTo(amountLimit) == 0) {
						amountLimitChoosen = amountLimit;
						AuthorizedLimitSchemeModel schemeModel = level.getAuthorizedLimitScheme();
						approvalMatrix.add(Arrays.asList(schemeModel!=null ? schemeModel.getApprovalLevel().getCode() : null, level.getNoUser(), level.getUserGroupOption(), userGroupId, schemeModel!=null ? schemeModel.getId() : null));
					}
				}
			}
 		}
		
		if(approvalMatrix.size() == 0) {
			throw new BusinessException("GPT-0100079");
		}
		
		return approvalMatrix;
	}
	
	public void setSpecifiParams(CorporateProcessInstance pi, Map<String, Object> processVars) throws BusinessException, ApplicationException {
		BigDecimal trxAmount = (BigDecimal)processVars.remove(ApplicationConstants.KEY_TRX_AMOUNT);
		String trxAmountCcyCd =  (String) processVars.remove(ApplicationConstants.KEY_TRX_AMOUNT_CCY_CD);
		String corpId = (String) processVars.remove(ApplicationConstants.KEY_CORP_ID);
		String actionByLevelName = (String) processVars.remove("actionByLevelName");
		String actionByLevelAlias = (String) processVars.remove("actionByLevelAlias");
		
		assert(trxAmount!=null);
		assert(corpId!=null);
		
		pi.setApprovalLimit(trxAmount);
		pi.setApprovalLimitCcyCd(trxAmountCcyCd);
		pi.setCorporateId(corpId);
		pi.setCreatedByLevelName(actionByLevelName);
		pi.setCreatedByLevelAlias(actionByLevelAlias);
		
		super.setSpecifiParams(pi, processVars);
	}
	
	protected void executeEndStage(CorporateProcessInstance pi) throws BusinessException, ApplicationException {
		pi.setTrxStatus(null); // let application update this trx status
		if(Status.APPROVED.equals(pi.getStatus())) {
			pendingTaskService.approve(pi.getId(), pi.getUpdatedBy());
		} else {
			pendingTaskService.reject(pi.getId(), pi.getUpdatedBy());
		}
	}
	
	@Override
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
			task.put("userApprovalLvName", t.get("approvalLvName"));
			task.put("userApprovalLvAlias", t.get("approvalLvAlias"));
			task.put("imageUrl", t.get("imageUrl"));

			if(startDate == null)
				startDate = t.get("startDate");

			tasks.add(task);
		}
		
		String stageId = pi.getCurrentStageId();
		result.put("stageId", stageId);
		result.put("startDate", startDate);
		
		String approvalLevel = null;
		String approvalLevelAlias = null;
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
			
			if(approval.get(4)!=null) {
				AuthorizedLimitSchemeModel schemeModel = apprLimitSchemeRepo.findOne((String)approval.get(4));
				if(schemeModel != null)
					approvalLevelAlias = schemeModel.getApprovalLevelAlias();
			}
		}
		
		result.put("approvalLvName", approvalLevel);
		result.put("approvalLvAlias", approvalLevelAlias);
		result.put("approvalLvCount", approvalLevelCount);
		result.put("approvalLvRequired", approvalLevelRequired);
		result.put("tasks", tasks);
		
		return result;
	}
	
	@Override
	protected Map<String, Object> findTasksHistory(CorporateProcessInstance pi) throws BusinessException, ApplicationException {
		try {
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
			activity.put("amount", pi.getApprovalLimit());
			activity.put("amountCcyCd", pi.getApprovalLimitCcyCd());
			activity.put("approvalLvName", pi.getCreatedByLevelName());
			activity.put("approvalLvAlias", pi.getCreatedByLevelAlias());
			activity.put("imageUrl", maker.getProfileImgUrl());
			
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
				activity.put("amount", t.get("amount"));
				activity.put("amountCcyCd", t.get("amountCcyCd"));
				activity.put("approvalLvName", t.get("approvalLvName"));
				activity.put("approvalLvAlias", t.get("approvalLvAlias"));
				activity.put("imageUrl", t.get("imageUrl"));
				
				String stageId = (String)t.get("stageId");
				
				if(stageId.equals(ApplicationConstants.WF_STAGE_APPROVE)) {
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
		}catch(Exception e) {
			throw new ApplicationException(e.getMessage(), e);
		}
	}
	
	@Override
	protected void updatePendingTaskActivity(CorporateProcessInstance pi) {
		if(pi.getTrxStatus() != null) {
			pendingTaskService.updatePendingTask(pi.getId(), 
					pi.getUpdatedDate() == null ? pi.getCreatedDate() : pi.getUpdatedDate(), 
					pi.getUpdatedBy() == null ? pi.getCreatedBy() : pi.getUpdatedBy(), 
					pi.getTrxStatus());
		}
	}
	
}	
