package com.gpt.product.gpcash.corporate.transactionstatus.services;

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
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.CorporateWFEngine;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.model.CorporateAdminPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.repository.CorporateAdminPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.repository.CorporateUserPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionActivityType;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.product.gpcash.corporate.transactionstatus.model.TransactionStatusModel;
import com.gpt.product.gpcash.corporate.transactionstatus.repository.TransactionStatusRepository;
import com.gpt.product.gpcash.corporate.transactionstatusservicemapping.model.TransactionStatusMappingModel;
import com.gpt.product.gpcash.corporate.transactionstatusservicemapping.repository.TransactionStatusMappingRepository;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class TransactionStatusServiceImpl implements TransactionStatusService{
	
	@Autowired
	private TransactionStatusRepository transactionStatusRepo;
	
	@Autowired
	private CorporateUserPendingTaskRepository corporateUserPendingTaskRepo;
	
	@Autowired
	private CorporateAdminPendingTaskRepository corporateAdminPendingTaskRepo;
	
	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	@Autowired
	private CorporateAdminPendingTaskService pendingTaskAdminService;
	
	@Autowired
	private CorporateWFEngine wfEngine;
	
	@Autowired
	private ApplicationContext appCtx;
	
	@Autowired
	private TransactionStatusMappingRepository transactionStatusMappingRepo;

	@Override
	public Map<String, Object> searchTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String loginUserId = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			CorporateUserModel user = corporateUtilsRepo.isCorporateUserValid(loginUserId);
			map.put("userGroupId", user.getCorporateUserGroup().getId());

			return wfEngine.findPendingTasks(map);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> searchTransactionStatusForBank(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String makerUserId = (String) map.get("makerUserId");
			if(ValueUtils.hasValue(makerUserId)) {
				String corporateId = (String) map.get(ApplicationConstants.CORP_ID); 
				String userCode = Helper.getCorporateUserCode(corporateId, makerUserId);
				map.put("makerUserCode", userCode);
			}
			
			return wfEngine.findPendingTasks(map);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> searchNonFinancialActivityForBank(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			return wfEngine.findNonFinancialPendingTasks(map);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> detailTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			LinkedList<Map<String, Object>> activitiesList = new LinkedList<>();
			String pendingTaskId = (String) map.get("pendingTaskId");
			
			CorporateUserPendingTaskModel wfHistoryPendingTask = corporateUserPendingTaskRepo.findById(pendingTaskId);
			
			//1. get data from transaction status table
			List<TransactionStatusModel> trxStatusList = transactionStatusRepo.
					findByPendingTaskId(pendingTaskId);
			for(TransactionStatusModel trxStatus : trxStatusList) {
				Map<String, Object> activity = new HashMap<>();
				CorporateUserPendingTaskModel trxStatusPendingTask = corporateUserPendingTaskRepo.findById(trxStatus.getPendingTaskId());
				IDMUserModel idmUser = idmRepo.isIDMUserValid(trxStatus.getUser());
				
				activity.put("activityDate", trxStatus.getActivityDate());
				activity.put("activity", trxStatus.getActionType());
				
				//agar ui dapat membedakan ada tombol view atau tidak
				if(trxStatus.getActionType().equals(TransactionActivityType.EXECUTE_TO_HOST)) {
					if(trxStatus.getEaiRefNo() != null) {
						activity.put("isExecute", ApplicationConstants.YES);
						activity.put("executedId", trxStatus.getId());
					}
					
					if(trxStatus.getStatus().equals(TransactionStatus.EXECUTE_SUCCESS) || trxStatus.getStatus().equals(TransactionStatus.EXECUTE_PARTIAL_SUCCESS)
							||(trxStatus.getStatus().equals(TransactionStatus.IN_PROGRESS_OFFLINE) && ApplicationConstants.YES.equals(wfHistoryPendingTask.getErrorTimeoutFlag()))) {
						
						IDMMenuModel menu = wfHistoryPendingTask.getMenu();
						if(ApplicationConstants.YES.equals(menu.getIsFinancialFlag())) {
							activity.put("isReceipt", ApplicationConstants.YES);
							activity.put("receiptId", trxStatus.getId());
						}
					}
				}
				
				activity.put("userId", idmUser.getUserId());
				activity.put("userName", idmUser.getName());
				activity.put("amountCcyCd", trxStatusPendingTask.getTransactionCurrency());
				activity.put("amount", trxStatusPendingTask.getTransactionAmount());
				activity.put("status", trxStatus.getStatus());
				
				//TODO harusnya referenceNo di table jika exist
				activity.put("referenceNo", trxStatusPendingTask.getReferenceNo());
				
				activitiesList.add(activity);
			}
			//----------------------------------------------------------------------------------
			
			
			//2. get data from wf history
			Map<String, Object> pendingTaskHistory = pendingTaskService.searchPendingTaskHistoryByPendingTaskId(map);
			List<Map<String, Object>> pendingTaskHistoryActivities =  (List<Map<String,Object>>) pendingTaskHistory.get("activities");
			for(int i=pendingTaskHistoryActivities.size()-1; i>=0 ; i--) {
				Map<String, Object> activity = pendingTaskHistoryActivities.get(i);
				activity.put("referenceNo", wfHistoryPendingTask.getReferenceNo());
				
				if(TransactionActivityType.CREATE.toString().equals(activity.get("activity"))) {
					activity.put("isCreate", ApplicationConstants.YES);
				}
				
				activitiesList.add(activity);
			}
			//----------------------------------------------------------------------------------
			
			
			
			resultMap.put("activities", activitiesList);
			
			//Validasi LOGIN_USERCODE di perlukan agar transaction status bank tidak ada cancel
			if(map.get(ApplicationConstants.LOGIN_USERCODE) != null) {
				if(TransactionStatus.PENDING_EXECUTE.toString().equals(wfHistoryPendingTask.getTrxStatus().name())) {
					//requested by Susi (12 October 2017), hanya user maker yg boleh cancel
					String loginUserCode =  (String) map.get(ApplicationConstants.LOGIN_USERCODE);
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
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> detailTransactionStatusForNonFinancial(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			LinkedList<Map<String, Object>> activitiesList = new LinkedList<>();
			String pendingTaskId = (String) map.get("pendingTaskId");
			
			CorporateAdminPendingTaskModel wfHistoryPendingTask = corporateAdminPendingTaskRepo.findById(pendingTaskId);
			
			//get data from wf history
			Map<String, Object> pendingTaskHistory = pendingTaskAdminService.searchPendingTaskHistoryByPendingTaskId(pendingTaskId);
			List<Map<String, Object>> pendingTaskHistoryActivities =  (List<Map<String,Object>>) pendingTaskHistory.get("activities");
			for(int i=pendingTaskHistoryActivities.size()-1; i>=0 ; i--) {
				Map<String, Object> activity = pendingTaskHistoryActivities.get(i);
				activity.put("referenceNo", wfHistoryPendingTask.getReferenceNo());
				
				if(TransactionActivityType.CREATE.toString().equals(activity.get("activity"))) {
					activity.put("isCreate", ApplicationConstants.YES);
				}
				
				activitiesList.add(activity);
			}
			//----------------------------------------------------------------------------------
			
			
			
			resultMap.put("activities", activitiesList);
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
			
			String service = ((TransactionStatusMappingModel) transactionStatusMappingRepo.findOne(executedMenuCode)).getMenuService();
			
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
	public void addTransactionStatus(String pendingTaskId, Timestamp activityDate, TransactionActivityType actionType, String user, TransactionStatus status, String eaiRefNo, boolean error, String errorCode) {
		
		TransactionStatusModel model = new TransactionStatusModel();
		
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
