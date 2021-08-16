package com.gpt.product.gpcash.corporate.workflow.definition.stage.cmsfouser;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserWFContext;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.product.gpcash.corporate.workflow.CorporateStage;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateProcessInstance;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateTaskInstance;

public class ReleaserStage extends CorporateStage {

	@Autowired
	protected CorporateUserPendingTaskService pendingTaskService;
	
	@Override
	public String getId() {
		return ApplicationConstants.WF_STAGE_RELEASE;
	}

	@Override
	public CorporateStage execute(CorporateProcessInstance pi) throws BusinessException, ApplicationException {
		// set curr approval level
		pi.setCurrApprLv(0);
		// reset approval counter for curr level
		pi.setCurrApprLvCount(0);
		
		CorporateUserWFContext wfContext = new CorporateUserWFContext();
		wfContext.setPendingTaskId(pi.getId());
		wfContext.setStageName(getId());
		
		List<Map<String,String>> users = pendingTaskService.getUserForWorkflow(wfContext);			

		if(users.size() <= 0) {
			throw new BusinessException("GPT-0100080");
		}
		
		createAndAssignTasksToCorporateUsers(pi, users);
		
		pi.setTrxStatus(TransactionStatus.PENDING_RELEASE);
		// wait here
		return null;
	}

	@Override
	public CorporateStage execute(CorporateTaskInstance ti) throws BusinessException, ApplicationException {
		
		Status result = ti.getStatus();
		
		assert(result !=null && (result.equals(Status.REJECTED) || result.equals(Status.RELEASED))) : "TaskInstance: " + ti.getId() + ", status must be set to REJECTED or RELEASED";
		
		cancelUnfinishedTasks(ti.getProcessInstance());
		
		// route to next stage
		return defaultTransition;
	}	
}
