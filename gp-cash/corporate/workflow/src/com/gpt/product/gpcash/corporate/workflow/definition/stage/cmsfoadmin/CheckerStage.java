package com.gpt.product.gpcash.corporate.workflow.definition.stage.cmsfoadmin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminWFContext;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.product.gpcash.corporate.workflow.CorporateStage;
import com.gpt.product.gpcash.corporate.workflow.definition.stage.BaseApproverStage;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateProcessInstance;

public class CheckerStage extends BaseApproverStage {

	@Autowired
	protected CorporateAdminPendingTaskService pendingTaskService;
	
	@Override
	public String getId() {
		return ApplicationConstants.WF_STAGE_CHECKER;
	}
	
	@Override
	public CorporateStage execute(CorporateProcessInstance pi) throws BusinessException, ApplicationException {
		CorporateStage nextStage = super.execute(pi);
		if(nextStage == null) {
			pi.setTrxStatus(TransactionStatus.PENDING_APPROVE);
		}
		return nextStage;
	}
	
	protected void createAndAssignTasksToUsers(CorporateProcessInstance pi, int noOfUser, int currApprLv, List<Object> approvalData) throws BusinessException, ApplicationException {
		// set curr approval level
		pi.setCurrApprLv(currApprLv);
		// reset approval counter for curr level
		pi.setCurrApprLvCount(0);
		
		CorporateAdminWFContext wfContext = new CorporateAdminWFContext();
		wfContext.setApprovalLevel((String)approvalData.get(0));
		wfContext.setPendingTaskId(pi.getId());

		List<String> users = pendingTaskService.getUserForWorkflow(wfContext);			

		if(users.size() < noOfUser) {
			//TODO: kode error nya sama dengan approver ?
			throw new BusinessException("GPT-0100037");
		}
		
        createAndAssignTasksToUsers(pi, users);
	}
	
}
