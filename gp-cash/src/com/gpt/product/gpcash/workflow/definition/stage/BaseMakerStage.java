package com.gpt.product.gpcash.workflow.definition.stage;

import java.util.ArrayList;
import java.util.List;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.product.gpcash.workflow.Stage;
import com.gpt.product.gpcash.workflow.model.ProcessInstance;
import com.gpt.product.gpcash.workflow.model.TaskInstance;

public abstract class BaseMakerStage extends Stage {
	
	@Override
	public String getId() {
		return ApplicationConstants.WF_STAGE_MAKER;
	}
	
	@Override
	public Stage execute(ProcessInstance pi) throws BusinessException, ApplicationException {
		// assign to maker
		List<String> users = new ArrayList<>();
		users.add(pi.getCreatedBy());
		createAndAssignTasksToUsers(pi, users);
		// wait here
		return null;
	}

	@Override
	public Stage execute(TaskInstance ti) throws BusinessException, ApplicationException {
		
		Status result = ti.getStatus();
		
		assert(result !=null && (result.equals(Status.CANCELED) || result.equals(Status.RESUBMITTED))) : "TaskInstance: " + ti.getId() + ", status must be set to CANCELED or RESUBMITTED";
		
		// route to next stage
		return getNextStage(result.name());
	}
}