package com.gpt.product.gpcash.corporate.workflow.definition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.platform.cash.workflow.CorporateWFEngine.Type;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminPendingTaskService;
import com.gpt.product.gpcash.corporate.workflow.CorporateProcessDefinition;
import com.gpt.product.gpcash.corporate.workflow.definition.stage.cmsfoadmin.CheckerStage;
import com.gpt.product.gpcash.corporate.workflow.definition.stage.cmsfoadmin.MakerStage;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateProcessInstance;

@Component
public class CMSFOAdmin extends CorporateProcessDefinition {
	
	@Autowired
	protected CorporateAdminPendingTaskService pendingTaskService;
	
	@PostConstruct
	public void init() {
		CheckerStage checkerStage = autowireBeanObject(new CheckerStage());
		MakerStage makerStage = autowireBeanObject(new MakerStage());
		
		// construct the flow
		startStage.addTransition(null, checkerStage);
		checkerStage.addTransition(Status.REJECTED.name(), endStage);
		checkerStage.addTransition(Status.APPROVED.name(), endStage);
		checkerStage.addTransition(Status.DECLINED.name(), makerStage);
		
		makerStage.addTransition(Status.CANCELED.name(), endStage);
		makerStage.addTransition(Status.RESUBMITTED.name(), checkerStage);
		
		stages = new HashMap<>(2, 1);
		stages.put(checkerStage.getId(), checkerStage);
		stages.put(makerStage.getId(), makerStage);
	}
	
	@Override
	public String getName() {
		return "CMSFOAdmin";
	}
	
	@Override
	public Type getType() {
		return Type.CorporateAdmin;
	}

	@Override
	public int getVersion() {
		return 1;
	}
	
	@Override
	protected List<List<Object>> getApprovalMatrix(CorporateProcessInstance pi) throws BusinessException, ApplicationException {
		//TODO only for development change to 0, production need to change to 1 for Checker
		return Arrays.asList(Arrays.asList(null, 1));
	}
	
	@Override
	protected void executeEndStage(CorporateProcessInstance pi) throws BusinessException, ApplicationException {
		pi.setTrxStatus(null); // let application update this trx status
		if(Status.APPROVED.equals(pi.getStatus())) {
			pendingTaskService.approve(pi.getId(), pi.getUpdatedBy());
		} else {
			pendingTaskService.reject(pi.getId(), pi.getUpdatedBy());
		}
	}
	
}
