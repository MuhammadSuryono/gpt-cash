package com.gpt.product.gpcash.retail.transactionstatus.services;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.spring.Util;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.retail.pendingtaskuser.model.CustomerUserPendingTaskModel;
import com.gpt.product.gpcash.retail.pendingtaskuser.repository.CustomerUserPendingTaskRepository;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserPendingTaskService;
import com.gpt.product.gpcash.retail.transactionstatus.CustomerTransactionActivityType;
import com.gpt.product.gpcash.retail.transactionstatus.CustomerTransactionStatus;
import com.gpt.product.gpcash.retail.transactionstatus.model.CustomerTransactionStatusModel;
import com.gpt.product.gpcash.retail.transactionstatus.repository.CustomerTransactionStatusRepository;
import com.gpt.product.gpcash.retail.transactionstatusservicemapping.model.CustomerTransactionStatusMappingModel;
import com.gpt.product.gpcash.retail.transactionstatusservicemapping.repository.CustomerTransactionStatusMappingRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerTransactionStatusServiceImpl implements CustomerTransactionStatusService{
	
	@Autowired
	private CustomerTransactionStatusRepository transactionStatusRepo;
	
	@Autowired
	private CustomerUserPendingTaskRepository customerUserPendingTaskRepo;
	
	@Autowired
	private CustomerUserPendingTaskService customerUserPendingTaskService;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private ApplicationContext appCtx;
	
	@Autowired
	private CustomerTransactionStatusMappingRepository transactionStatusMappingRepo;

	@Override
	public Map<String, Object> searchTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			return customerUserPendingTaskService.searchPendingTask(map);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> detailTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			LinkedList<Map<String, Object>> activitiesList = new LinkedList<>();
			String pendingTaskId = (String) map.get("pendingTaskId");
			
			CustomerUserPendingTaskModel wfHistoryPendingTask = customerUserPendingTaskRepo.findById(pendingTaskId);
			
			//1. get data from transaction status table
			List<CustomerTransactionStatusModel> trxStatusList = transactionStatusRepo.
					findByPendingTaskId(pendingTaskId);
			
			for(CustomerTransactionStatusModel trxStatus : trxStatusList) {
				Map<String, Object> activity = new HashMap<>();
				CustomerUserPendingTaskModel pendingTask = customerUserPendingTaskRepo.findById(trxStatus.getPendingTaskId());
				
				IDMUserModel idmUser = idmRepo.isIDMUserValid(trxStatus.getUser());
				
				activity.put("activityDate", trxStatus.getActivityDate());
				activity.put("activity", trxStatus.getActionType());
				
				//agar ui dapat membedakan ada tombol view atau tidak
				if(trxStatus.getActionType().equals(CustomerTransactionActivityType.EXECUTE_TO_HOST)) {
					if(trxStatus.getEaiRefNo() != null) {
						activity.put("isExecute", ApplicationConstants.YES);
						activity.put("executedId", trxStatus.getId());
					}
					
					if(trxStatus.getStatus().equals(CustomerTransactionStatus.EXECUTE_SUCCESS) || trxStatus.getStatus().equals(CustomerTransactionStatus.EXECUTE_PARTIAL_SUCCESS)) {
						
						IDMMenuModel menu = wfHistoryPendingTask.getMenu();
						if(ApplicationConstants.YES.equals(menu.getIsFinancialFlag())) {
							activity.put("isReceipt", ApplicationConstants.YES);
							activity.put("receiptId", trxStatus.getId());
						}
					}
				} else if(trxStatus.getActionType().equals(CustomerTransactionActivityType.CREATE)) {
					activity.put("isCreate", ApplicationConstants.YES);
				}
				
				activity.put("userId", idmUser.getUserId());
				activity.put("userName", idmUser.getName());
				activity.put("amountCcyCd", pendingTask.getTransactionCurrency());
				activity.put("amount", pendingTask.getTransactionAmount());
				activity.put("status", trxStatus.getStatus());
				
				activity.put("referenceNo", pendingTask.getReferenceNo());
				
				activitiesList.add(activity);
			}
			//----------------------------------------------------------------------------------
			
			
			resultMap.put("activities", activitiesList);
			
			if(map.get(ApplicationConstants.CUST_ID) != null) {
				if(CustomerTransactionStatus.PENDING_EXECUTE.toString().equals(wfHistoryPendingTask.getTrxStatus().name())) {
					String loginUserCode =  (String) map.get(ApplicationConstants.CUST_ID);
					if(loginUserCode.equals(wfHistoryPendingTask.getCreatedBy())) {
						resultMap.put("isCancel", ApplicationConstants.YES);
						resultMap.put("cancelId", pendingTaskId);
					}
				}
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String executedMenuCode = (String) map.get("executedMenuCode");
			
			String service = ((CustomerTransactionStatusMappingModel) transactionStatusMappingRepo.findOne(executedMenuCode)).getMenuService();
			
			map.put(ApplicationConstants.VIEW_FROM_BANK, ApplicationConstants.YES);
			
			resultMap = Util.invokeSpringBean(appCtx, service, "detailExecutedTransaction", map);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void addTransactionStatus(String pendingTaskId, Timestamp activityDate, CustomerTransactionActivityType actionType, String user, CustomerTransactionStatus status, String eaiRefNo, boolean error, String errorCode) {
		
		CustomerTransactionStatusModel model = new CustomerTransactionStatusModel();
		
		model.setPendingTaskId(pendingTaskId);
		model.setActivityDate(activityDate);
		model.setActionType(actionType);
		model.setUser(user);
		model.setStatus(status);
		model.setEaiRefNo(eaiRefNo);
		model.setError(error);
		if (error && errorCode==null)
			errorCode = "GPT-GENERALERROR";
		model.setErrorCode(errorCode);
		
		transactionStatusRepo.save(model);
		
	}

}
