package com.gpt.product.gpcash.corporate.workflow.definition.stage.cmsfouser;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserWFContext;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.product.gpcash.corporate.workflow.CorporateStage;
import com.gpt.product.gpcash.corporate.workflow.definition.stage.BaseApproverStage;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateProcessInstance;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateTaskInstance;
import com.gpt.product.gpcash.corporate.workflow.repository.CorporateTaskInstanceRepository;

public class ApproverStage extends BaseApproverStage {
	
	@Autowired
	protected CorporateUserPendingTaskService pendingTaskService;

	@Autowired 
	protected CorporateTaskInstanceRepository tiRepo;
	
	protected void createAndAssignTasksToUsers(CorporateProcessInstance pi, int noOfUser, int currApprLv, List<Object> approvalData) throws BusinessException, ApplicationException {
		// set curr approval level
		pi.setCurrApprLv(currApprLv);
		// reset approval counter for curr level
		pi.setCurrApprLvCount(0);
		
		CorporateUserWFContext wfContext = new CorporateUserWFContext();
		wfContext.setApprovalLevel((String)approvalData.get(0));
		wfContext.setPendingTaskId(pi.getId());
		wfContext.setStageName(getId());
		
		wfContext.setUserGroupOption((String)approvalData.get(2));
		wfContext.setUserGroup((String)approvalData.get(3));
		
		if(pi.getUpdatedBy() == null) {
			wfContext.setApproverUserCode(pi.getCreatedBy());
		} else {
			wfContext.setApproverUserCode(pi.getUpdatedBy());
		}
		
		List<String> assignedUsers = tiRepo.findAssignedUserByProcessInstanceId(pi.getId());
		wfContext.setApproverHistory(assignedUsers);
		
		List<Map<String,String>> users = pendingTaskService.getUserForWorkflow(wfContext);			

		if(users.size() < noOfUser) {
			throw new BusinessException("GPT-0100037");
		}
		
        createAndAssignTasksToCorporateUsers(pi, users);
	}
	
	@Override
	public CorporateStage execute(CorporateProcessInstance pi) throws BusinessException, ApplicationException {
		CorporateStage nextStage = super.execute(pi);
		if(nextStage == null) {
			pi.setTrxStatus(TransactionStatus.PENDING_APPROVE);
		}
		return nextStage;
	}
	
	@Override
	public CorporateStage execute(CorporateTaskInstance ti) throws BusinessException, ApplicationException {
		CorporateStage nextStage = super.execute(ti);
		if(nextStage == null) {
			ti.getProcessInstance().setTrxStatus(TransactionStatus.APPROVAL_IN_PROGRESS);
		}
		return nextStage;
	}
	
	@Override
	protected boolean isSkipToNextStage(CorporateProcessInstance pi) {
		return pendingTaskService.isSkipToReleaser(pi.getUpdatedBy(), pi.getApprovalLimit());
	}
}
