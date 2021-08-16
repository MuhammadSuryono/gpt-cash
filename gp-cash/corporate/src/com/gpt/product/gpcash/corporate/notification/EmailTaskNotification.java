package com.gpt.product.gpcash.corporate.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.domesticbank.model.DomesticBankModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.service.model.ServiceModel;
import java.math.BigDecimal;

@Component
public class EmailTaskNotification implements TaskNotification {

	@Autowired
	private EAIEngine eaiAdapter;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;

	private void checkIsUserNotifyTask(CorporateUserModel assignedUser, List<String> emails){
		if(ApplicationConstants.YES.equals(assignedUser.getIsNotifyMyTask())){
			String email = assignedUser.getUser().getEmail();
			if (ValueUtils.hasValue(email)) {
				emails.add(email);
			}
		}
	}
	
	@Override
	public void sendNotification(CorporateUserPendingTaskModel pendingTask, List<CorporateUserModel> users)
			throws Exception {
		
		List<String> emails = new ArrayList<>(users.size());
		for(CorporateUserModel user: users) {
			checkIsUserNotifyTask(user, emails);
		}
		
		if(emails.isEmpty())
			return;
		
		Map<String, Object> inputs = new HashMap<>();
		inputs.put("emails", emails);
		inputs.put("subject", "Task Notification");
		
		//based on Susi (12 October 2017), trxDate untuk routing task adalah createdDate
		inputs.put("createdDate", pendingTask.getCreatedDate());
		//---------------------------
		
		inputs.put("debitAccountNo", pendingTask.getSourceAccount());
		inputs.put("debitAccountName", pendingTask.getSourceAccountName());
		inputs.put("debitAccountCurrency", pendingTask.getSourceAccountCurrencyCode());
		inputs.put("benAccountNo", ValueUtils.getValue(pendingTask.getBenAccount()));
		inputs.put("benAccountName", ValueUtils.getValue(pendingTask.getBenAccountName()));
		inputs.put("benAccountCurrencyCode", ValueUtils.getValue(pendingTask.getBenAccountCurrencyCode()));
		
		if(pendingTask.getBenBankCode() != null) {
			DomesticBankModel domBank = maintenanceRepo.getDomesticBankRepo().findOne(pendingTask.getBenBankCode());
			if(domBank != null) {
				inputs.put("destinationBankName", domBank.getName());
			}
		}
		inputs.put("trxCurrencyCode", pendingTask.getTransactionCurrency());
		if(ApplicationConstants.APPROVAL_MATRIX_NON_TRANSACTION.equals(pendingTask.getTransactionAmount().toPlainString())){
			inputs.put("trxAmount",BigDecimal.ZERO);
		}else {			
			inputs.put("trxAmount", pendingTask.getTransactionAmount());
		}
		
		inputs.put("menuName", pendingTask.getMenu().getName());
		
		ServiceModel serviceModel = pendingTask.getTransactionService();
		inputs.put("serviceCode", serviceModel.getCode());
		inputs.put("serviceName", serviceModel.getName());
		
		inputs.put("instructionMode", pendingTask.getInstructionMode());
		inputs.put("instructionDate", pendingTask.getInstructionDate());
		inputs.put("refNo", pendingTask.getReferenceNo());
		inputs.put("taskStatus", pendingTask.getTrxStatus() != null ? pendingTask.getTrxStatus().name() : ApplicationConstants.EMPTY_STRING);
		inputs.put("remark1", pendingTask.getRemark1()); 
		inputs.put("remark2", pendingTask.getRemark2()); 
		inputs.put("remark3", pendingTask.getRemark3()); 
		
		inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
		inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());
		
		eaiAdapter.invokeService(EAIConstants.TASK_NOTIFICATION, inputs);
		
	}
}
