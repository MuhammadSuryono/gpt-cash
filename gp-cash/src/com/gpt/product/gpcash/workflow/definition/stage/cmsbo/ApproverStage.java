package com.gpt.product.gpcash.workflow.definition.stage.cmsbo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.WFContext;
import com.gpt.product.gpcash.workflow.Stage;
import com.gpt.product.gpcash.workflow.TransactionStatus;
import com.gpt.product.gpcash.workflow.definition.stage.BaseApproverStage;
import com.gpt.product.gpcash.workflow.model.ProcessInstance;

public class ApproverStage extends BaseApproverStage {

	@Autowired
	protected PendingTaskService pendingTaskService;
	
	@Override
	public Stage execute(ProcessInstance pi) throws BusinessException, ApplicationException {
		Stage nextStage = super.execute(pi);
		if(nextStage == null) {
			pi.setTrxStatus(TransactionStatus.PENDING_APPROVE);
		}
		return nextStage;
	}
	
	protected void createAndAssignTasksToUsers(ProcessInstance pi, int noOfUser, int currApprLv, List<Object> approvalData) throws BusinessException, ApplicationException {
		// set curr approval level
		pi.setCurrApprLv(currApprLv);
		// reset approval counter for curr level
		pi.setCurrApprLvCount(0);
		
		WFContext wfContext = new WFContext();
		wfContext.setApprovalLevel((String)approvalData.get(0));
		wfContext.setPendingTaskId(pi.getId());
		wfContext.setBranchOption((String) approvalData.get(2));

		List<String> users = pendingTaskService.getUserForWorkflow(wfContext);			

		if(users.size() < noOfUser) {
			throw new BusinessException("GPT-0100037");
		}
		
        createAndAssignTasksToUsers(pi, users);
		
	}
	
}
