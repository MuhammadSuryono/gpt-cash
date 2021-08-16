package com.gpt.product.gpcash.workflow.definition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.platform.cash.workflow.WFEngine.Type;
import com.gpt.product.gpcash.bankapprovalmatrix.model.BankApprovalMatrixDetailModel;
import com.gpt.product.gpcash.bankapprovalmatrix.services.BankApprovalMatrixService;
import com.gpt.product.gpcash.workflow.ProcessDefinition;
import com.gpt.product.gpcash.workflow.definition.stage.cmsbo.ApproverStage;
import com.gpt.product.gpcash.workflow.definition.stage.cmsbo.MakerStage;
import com.gpt.product.gpcash.workflow.model.ProcessInstance;

@Component
public class CMSBO extends ProcessDefinition {
	
	@Autowired
	protected BankApprovalMatrixService bankApprovalMatrixService;
	
	@Autowired
	protected PendingTaskService pendingTaskService;
	
	@PostConstruct
	public void init() {
		ApproverStage approverStage = autowireBeanObject(new ApproverStage());
		MakerStage makerStage = autowireBeanObject(new MakerStage());
		
		// construct the flow
		startStage.addTransition(null, approverStage);
		approverStage.addTransition(Status.REJECTED.name(), endStage);
		approverStage.addTransition(Status.APPROVED.name(), endStage);

		approverStage.addTransition(Status.DECLINED.name(), makerStage);
		makerStage.addTransition(Status.CANCELED.name(), endStage);
		makerStage.addTransition(Status.RESUBMITTED.name(), approverStage);
		
		stages = new HashMap<>(2, 1);
		stages.put(approverStage.getId(), approverStage);
		stages.put(makerStage.getId(), makerStage);
	}
	
	@Override
	public Type getType() {
		return Type.BackOfficeUser;
	}

	@Override
	public int getVersion() {
		return 1;
	}
	
	@Override
	public String getName() {
		return "CMSBO";
	}
	
	protected List<List<Object>> getApprovalMatrix(ProcessInstance pi) throws BusinessException, ApplicationException {
		List<BankApprovalMatrixDetailModel> levels = bankApprovalMatrixService.getBankApprovalMatrixDetailForWorkflow(pi.getMenuCode());
		
		if(levels.size() == 0) {
			throw new BusinessException("GPT-0100036");
		}
		
		List<List<Object>> approvalMatrix = new ArrayList<>(levels.size());
		for(int i=0;i<levels.size();i++) {
			BankApprovalMatrixDetailModel level = levels.get(i);
			approvalMatrix.add(Arrays.asList(level.getApprovalLevel()!=null ? level.getApprovalLevel().getCode() : null, level.getNoOfUser(), level.getBranchOption()));
 		}
		
		return approvalMatrix;
	}
	
	@Override
	protected void executeEndStage(ProcessInstance pi) throws BusinessException, ApplicationException {
		pi.setTrxStatus(null); // let application update this trx status
		if(Status.APPROVED.equals(pi.getStatus())) {
			pendingTaskService.approve(pi.getId(), pi.getUpdatedBy());
		} else {
			pendingTaskService.reject(pi.getId(), pi.getUpdatedBy());
		}
	}
	
}
