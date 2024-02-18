package com.gpt.product.gpcash.retail.pendingtaskuser.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.customer.repository.CustomerRepository;
import com.gpt.product.gpcash.retail.pendingtaskuser.model.CustomerUserPendingTaskModel;
import com.gpt.product.gpcash.retail.pendingtaskuser.repository.CustomerUserPendingTaskRepository;
import com.gpt.product.gpcash.retail.pendingtaskuser.valueobject.CustomerUserPendingTaskVO;
import com.gpt.product.gpcash.retail.transactionstatus.CustomerTransactionActivityType;
import com.gpt.product.gpcash.retail.transactionstatus.CustomerTransactionStatus;
import com.gpt.product.gpcash.retail.transactionstatus.services.CustomerTransactionStatusService;
import com.gpt.product.gpcash.service.model.ServiceModel;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerUserPendingTaskServiceImpl implements CustomerUserPendingTaskService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CustomerUserPendingTaskRepository pendingTaskRepo;

	@Autowired
	private CustomerRepository customerRepo;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private ApplicationContext appCtx;
	
	@Autowired
	private CustomerTransactionStatusService trxStatusService;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;

	@Override
	public Map<String, Object> searchPendingTask(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		try {
			Page<CustomerUserPendingTaskModel> pendingTasks = pendingTaskRepo.searchPendingTask(map, PagingUtils.createPageRequest(map));
			List<CustomerUserPendingTaskModel> list = pendingTasks.getContent();
			
			Map<String, Object> resultMap = new HashMap<>();
			
			List<Map<String, Object>> resultList = new ArrayList<>(list.size());

			for (CustomerUserPendingTaskModel model : list) {
				resultList.add(setModelToMapPendingTask(model));
			}

			resultMap.put("result", resultList);

			PagingUtils.setPagingInfo(resultMap, pendingTasks);
			
			return resultMap;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private Map<String, Object> setModelToMapPendingTask(CustomerUserPendingTaskModel model) {
		Map<String, Object> map = new HashMap<>();
		
		CustomerModel customer = model.getCustomer();
		map.put("customerId", customer.getId());
		map.put("customerName", customer.getName());
		map.put("latestActivityDate", model.getActivityDate());
		map.put("referenceNo", model.getReferenceNo());
		
		IDMUserModel idmUser = idmRepo.getUserRepo().findOne(model.getCreatedBy());
		map.put("createdByUserId", idmUser.getUserId());
		map.put("createdByUserName", idmUser.getName());
		map.put("pendingTaskId", model.getId());
		map.put("pendingTaskMenuCode", model.getMenu().getCode());
		map.put("pendingTaskMenuName", model.getMenu().getName());
		map.put("sourceAccount", ValueUtils.getValue(model.getSourceAccount()));
		map.put("sourceAccountName", ValueUtils.getValue(model.getSourceAccountName()));
		map.put("sourceAccountCurrencyCode", ValueUtils.getValue(model.getSourceAccountCurrencyCode()));
		map.put("sourceAccountCurrencyName", ValueUtils.getValue(model.getSourceAccountCurrencyName()));
		map.put("transactionAmount", ValueUtils.getValue(model.getTransactionAmount()));
		map.put("transactionCurrency", ValueUtils.getValue(model.getTransactionCurrency()));
		map.put("status", model.getTrxStatus());

		return map;
	}	

	@Override
	public Map<String, Object> detailPendingTask(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		try {
			String referenceNo = (String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
			
			Map<String, Object> result = new HashMap<>();
			result.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, referenceNo);

			CustomerUserPendingTaskModel model = pendingTaskRepo.findByReferenceNo(referenceNo);
			if (model != null) {
				result.put("service", model.getService()); // put service
				result.put("details", getPendingTaskValue(model));
			}

			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> detailPendingTaskOld(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		try {
			String referenceNo = (String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);

			Map<String, Object> result = new HashMap<>();
			result.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, referenceNo);

			CustomerUserPendingTaskModel model = pendingTaskRepo.findByReferenceNo(referenceNo);
			if (model != null) {
				result.put("service", model.getService()); // put service
				result.put("details", getPendingTaskOldValue(model));
			}

			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private CustomerModel isCustUserValid(String code) throws Exception {
		CustomerModel model = customerRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100009");
			}
		} else {
			throw new BusinessException("GPT-0100009");
		}
		
		return model;
	}

	@Override
	public Map<String, Object> savePendingTask(CustomerUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			vo.setReferenceNo(Helper.generateCustomerReferenceNo());

			// validasi user
			isCustUserValid(vo.getCreatedBy());
			
			Timestamp createdDate = DateUtils.getCurrentTimestamp();
			
			CustomerUserPendingTaskModel pendingTask = new CustomerUserPendingTaskModel();
			pendingTask.setUniqueKey(vo.getUniqueKey());
			pendingTask.setUniqueKeyDisplay(vo.getUniqueKeyDisplay());

			CustomerModel customer = new CustomerModel();
			customer.setId(vo.getCustomerId());
			pendingTask.setCustomer(customer);
			pendingTask.setReferenceNo(vo.getReferenceNo());
			pendingTask.setSenderRefNo(vo.getSenderRefNo());
			pendingTask.setIsFinalPayment(vo.getIsFinalPayment());
			pendingTask.setBenRefNo(vo.getBenRefNo());
			pendingTask.setAction(vo.getAction());
			pendingTask.setCreatedBy(vo.getCreatedBy());
			pendingTask.setCreatedDate(createdDate);
			pendingTask.setStatus(ApplicationConstants.WF_STATUS_PENDING);
			pendingTask.setSessionTime(vo.getSessionTime());
			
			IDMMenuModel menu = idmRepo.getMenuRepo().findOne(vo.getMenuCode());
			pendingTask.setMenu(menu);

			pendingTask.setModel(vo.getJsonObject().getClass().getName());
			pendingTask.setService(vo.getService());
			
			//set debit info
			pendingTask.setSourceAccount(vo.getSourceAccount());
			pendingTask.setSourceAccountName(vo.getSourceAccountName());
			pendingTask.setSourceAccountCurrencyCode(vo.getSourceAccountCurrencyCode());
			pendingTask.setSourceAccountCurrencyName(vo.getSourceAccountCurrencyName());
			pendingTask.setSourceAccountDetailId(vo.getSourceAccountDetailId());
			
			//set credit info
			pendingTask.setBenAccount(vo.getBenAccount());
			pendingTask.setBenAccountName(vo.getBenAccountName());
			pendingTask.setBenAccountCurrencyCode(vo.getBenAccountCurrencyCode());
			pendingTask.setBenAccountCurrencyName(vo.getBenAccountCurrencyName());
			pendingTask.setBenBankCode(vo.getBenBankCode());
			
			pendingTask.setTransactionAmount(vo.getTransactionAmount());
			pendingTask.setTransactionCurrency(vo.getTransactionCurrency());
			pendingTask.setTotalChargeEquivalentAmount(vo.getTotalChargeEquivalentAmount());
			pendingTask.setTotalDebitedEquivalentAmount(vo.getTotalDebitedEquivalentAmount());
			pendingTask.setInstructionMode(vo.getInstructionMode());
			pendingTask.setInstructionDate(vo.getInstructionDate());
			pendingTask.setRecurringParamType(vo.getRecurringParamType());
			pendingTask.setRecurringParam(vo.getRecurringParam());
			pendingTask.setRecurringStartDate(vo.getRecurringStartDate());
			pendingTask.setRecurringEndDate(vo.getRecurringEndDate());
			pendingTask.setTaskExpiryDate(vo.getTaskExpiryDate());
			
			//set remarks
			pendingTask.setRemark1(vo.getRemark1());
			pendingTask.setRemark2(vo.getRemark2());
			pendingTask.setRemark3(vo.getRemark3());
			
			if (vo.getRefNoSpecialRate() != null) {
				pendingTask.setRefNoSpecialRate(vo.getRefNoSpecialRate());
			}
			
			if(vo.getTransactionCurrency()!=null && vo.getSourceAccountCurrencyCode()!=null) {
				pendingTask.setServiceCurrencyMatrix(getCurrencyMatrixCode(vo.getSourceAccountCurrencyCode(), vo.getTransactionCurrency()));
			}
			
			if(ValueUtils.hasValue(vo.getTransactionServiceCode())){
				ServiceModel service = new ServiceModel();
				service.setCode(vo.getTransactionServiceCode());
				pendingTask.setTransactionService(service);
			}

			if (vo.getJsonObject() != null) {
				String jsonObj = objectMapper.writeValueAsString(vo.getJsonObject());
				pendingTask.setValues(jsonObj);
			}

			if (vo.getJsonObjectOld() != null) {
				String jsonObjOld = objectMapper.writeValueAsString(vo.getJsonObjectOld());
				pendingTask.setOldValues(jsonObjOld);
			}
			
			pendingTask.setTrxStatus(CustomerTransactionStatus.PENDING_EXECUTE);
			
			if (logger.isDebugEnabled())
				logger.debug("User PendingTask : " + pendingTask);

			pendingTaskRepo.save(pendingTask);
			
			vo.setId(pendingTask.getId());
			vo.setCreatedDate(createdDate);
			
			return null;

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void approve(CustomerUserPendingTaskVO vo, CustomerUserWorkflowService service) throws ApplicationException, BusinessException {
		boolean approved = false;
		String errorCode = null;
		Timestamp pendingExecuteDate = vo.getCreatedDate();
		Timestamp executedDate = DateUtils.getCurrentTimestamp();
		String userId = vo.getCreatedBy();
		
		IDMMenuModel menu = null;
		try {
			menu = idmRepo.isIDMMenuValid(vo.getMenuCode());
		} catch (Exception e1) {
			//this should not happen
			logger.debug("menu not found with vo ID : " + vo.getId());
			throw new ApplicationException(e1);
		}
		
		try {
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n\n\n");
				logger.debug("execute vo ID : " + vo.getId());
				logger.debug("\n\n\n\n\n");
			}

			service.approve(vo);
			
			if(ApplicationConstants.SINGLE_TRANSACTION.equals(menu.getSubType())) {
				if(vo.getInstructionMode().equals(ApplicationConstants.SI_IMMEDIATE)) {
					if (ApplicationConstants.YES.equals(vo.getIsError())) {
						if(CustomerTransactionStatus.EXECUTE_PARTIAL_SUCCESS.name().equals(vo.getErrorCode())) {
							pendingTaskRepo.updatePendingTask(vo.getId(), ApplicationConstants.WF_STATUS_APPROVED, executedDate, userId, CustomerTransactionStatus.EXECUTE_PARTIAL_SUCCESS);
						} else {
							pendingTaskRepo.updatePendingTask(vo.getId(), ApplicationConstants.WF_STATUS_APPROVED, executedDate, userId, CustomerTransactionStatus.EXECUTE_FAIL);
						}
						
					}else {
						pendingTaskRepo.updatePendingTask(vo.getId(), ApplicationConstants.WF_STATUS_APPROVED, executedDate, userId, CustomerTransactionStatus.EXECUTE_SUCCESS);
					}
				} else {
					pendingTaskRepo.updatePendingTask(vo.getId(), ApplicationConstants.WF_STATUS_APPROVED, executedDate, userId, CustomerTransactionStatus.PENDING_EXECUTE);
				}
			} else { //sub type not SINGLE or BULK
				pendingTaskRepo.updatePendingTask(vo.getId(), ApplicationConstants.WF_STATUS_APPROVED, executedDate, userId, CustomerTransactionStatus.EXECUTE_SUCCESS);
			}
			
			approved = true;
		} catch (BusinessException e) {
			errorCode = e.getErrorCode();
			throw e;
		} catch (ApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		} finally {
			if(menu != null) {
				if(ApplicationConstants.SINGLE_TRANSACTION.equals(menu.getSubType())) {
					//insert into trx status PENDING_EXECUTE
					trxStatusService.addTransactionStatus(vo.getId(), pendingExecuteDate, CustomerTransactionActivityType.CREATE, userId, CustomerTransactionStatus.PENDING_EXECUTE, null, false, null);
					
					if (approved) {				
						if(vo.getInstructionMode().equals(ApplicationConstants.SI_IMMEDIATE)) {
							if (ApplicationConstants.YES.equals(vo.getIsError())) {
								//insert into trx status EXECUTE_FAIL, error code get from VO
								if(CustomerTransactionStatus.EXECUTE_PARTIAL_SUCCESS.name().equals(vo.getErrorCode())) {
									trxStatusService.addTransactionStatus(vo.getId(), DateUtils.getCurrentTimestamp(), CustomerTransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.EXECUTE_PARTIAL_SUCCESS, vo.getId(), true, vo.getErrorCode());
								} else {
									trxStatusService.addTransactionStatus(vo.getId(), DateUtils.getCurrentTimestamp(), CustomerTransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.EXECUTE_FAIL, vo.getId(), true, vo.getErrorCode());
								}
														
							} else {
								//insert into trx status EXECUTE_SUCCESS
								trxStatusService.addTransactionStatus(vo.getId(), executedDate, CustomerTransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.EXECUTE_SUCCESS, vo.getId(), false, null);										
							}
						}
					} else {
						//insert into trx status EXECUTE_FAIL
						trxStatusService.addTransactionStatus(vo.getId(), DateUtils.getCurrentTimestamp(), CustomerTransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.EXECUTE_FAIL, vo.getId(), true, errorCode);
					}	
				} else {
					//untuk yg menu type cd nya adalah M seperti beneficiary list
					if (approved) {	
						//insert into trx status EXECUTE_SUCCESS
						trxStatusService.addTransactionStatus(vo.getId(), pendingExecuteDate, CustomerTransactionActivityType.EXECUTE, userId, CustomerTransactionStatus.EXECUTE_SUCCESS, null, false, null);
					} else {
						//insert into trx status EXECUTE_SUCCESS
						trxStatusService.addTransactionStatus(vo.getId(), pendingExecuteDate, CustomerTransactionActivityType.EXECUTE, userId, CustomerTransactionStatus.EXECUTE_FAIL, null, false, null);
					}
					
				}
			}
		}
	}

	@Override
	public void reject(String pendingTaskId, String userId) throws ApplicationException, BusinessException {
		try {
			CustomerUserPendingTaskModel pendingTask = pendingTaskRepo.findById(pendingTaskId);
			if (pendingTask == null) {
				throw new BusinessException("GPT-0100001");
			}

			CustomerUserWorkflowService service = (CustomerUserWorkflowService) appCtx
					.getBean(pendingTask.getService());

			CustomerUserPendingTaskVO vo = getVOFromCustomerUserPendingTaskModel(pendingTask);
			vo.setActionBy(userId);

			service.reject(vo);
			
			pendingTaskRepo.updatePendingTask(pendingTask.getId(), ApplicationConstants.WF_STATUS_REJECTED, DateUtils.getCurrentTimestamp(), userId, CustomerTransactionStatus.REJECTED);

			Timestamp now = DateUtils.getCurrentTimestamp();
			
			//insert into trx status PENDING_EXECUTE
			trxStatusService.addTransactionStatus(pendingTaskId, now, CustomerTransactionActivityType.REJECT, userId, CustomerTransactionStatus.REJECTED, pendingTaskId, true, null);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void checkUniquePendingTask(CustomerUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			if (ValueUtils.hasValue(pendingTaskRepo.findByUniqueKeyAndStatusAndMenuCodeAndCustomerId(vo.getUniqueKey(), 
					ApplicationConstants.WF_STATUS_PENDING, vo.getMenuCode(), vo.getCustomerId()))) {
				throw new BusinessException("GPT-0100002");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void checkUniquePendingTaskLike(CustomerUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		try {
			if (ValueUtils.hasValue(pendingTaskRepo.findByUniqueKeyContainingAndStatusAndMenuCodeAndCustomerId(vo.getUniqueKey(), ApplicationConstants.WF_STATUS_PENDING, 
					vo.getMenuCode(), vo.getCustomerId()))) {
				throw new BusinessException("GPT-0100002");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	private CustomerUserPendingTaskVO getVOFromCustomerUserPendingTaskModel(CustomerUserPendingTaskModel pendingTask)
			throws Exception {
		CustomerUserPendingTaskVO vo = new CustomerUserPendingTaskVO();
		vo.setId(pendingTask.getId());
		vo.setUniqueKey(pendingTask.getUniqueKey());
		vo.setUniqueKeyDisplay(pendingTask.getUniqueKeyDisplay());
		vo.setReferenceNo(pendingTask.getReferenceNo());
		vo.setSenderRefNo(pendingTask.getSenderRefNo());
		vo.setIsFinalPayment(pendingTask.getIsFinalPayment());
		vo.setBenRefNo(pendingTask.getBenRefNo());
		vo.setAction(pendingTask.getAction());
		vo.setCreatedBy(pendingTask.getCreatedBy());
		vo.setCreatedDate(pendingTask.getCreatedDate());
		vo.setStatus(pendingTask.getStatus());
		vo.setMenuCode(pendingTask.getMenu().getCode());
		vo.setService(pendingTask.getService());
		vo.setModel(pendingTask.getModel());
		vo.setCustomerId(pendingTask.getCustomer().getId());
		vo.setSessionTime(pendingTask.getSessionTime());
		
		//set debit info
		vo.setSourceAccount(pendingTask.getSourceAccount());
		vo.setSourceAccountName(pendingTask.getSourceAccountName());
		vo.setSourceAccountCurrencyCode(pendingTask.getSourceAccountCurrencyCode());
		vo.setSourceAccountCurrencyName(pendingTask.getSourceAccountCurrencyName());
		
		//set credit info
		vo.setBenAccount(pendingTask.getBenAccount());
		vo.setBenAccountName(pendingTask.getBenAccountName());
		vo.setBenAccountCurrencyCode(pendingTask.getBenAccountCurrencyCode());
		vo.setBenAccountCurrencyName(pendingTask.getBenAccountCurrencyName());
		vo.setBenBankCode(pendingTask.getBenBankCode());
		
		vo.setTransactionCurrency(pendingTask.getTransactionCurrency());
		vo.setTransactionAmount(pendingTask.getTransactionAmount());
		vo.setTotalChargeEquivalentAmount(pendingTask.getTotalChargeEquivalentAmount());
		vo.setTotalDebitedEquivalentAmount(pendingTask.getTotalDebitedEquivalentAmount());
		vo.setTransactionServiceCode(pendingTask.getTransactionService() != null ?pendingTask.getTransactionService().getCode():null);
		vo.setInstructionMode(pendingTask.getInstructionMode());
		vo.setInstructionDate(pendingTask.getInstructionDate());
		vo.setRecurringParamType(pendingTask.getRecurringParamType());
		vo.setRecurringParam(pendingTask.getRecurringParam());
		vo.setRecurringStartDate(pendingTask.getRecurringStartDate());
		vo.setRecurringEndDate(pendingTask.getRecurringEndDate());
		vo.setTaskExpiryDate(pendingTask.getTaskExpiryDate());
		
		//set remarks
		vo.setRemark1(pendingTask.getRemark1());
		vo.setRemark2(pendingTask.getRemark2());
		vo.setRemark3(pendingTask.getRemark3());
		
		Class<?> clazz = Class.forName(pendingTask.getModel());

		vo.setJsonObject(objectMapper.readValue(pendingTask.getValuesStr(), clazz));

		String oldValues = pendingTask.getOldValuesStr();
		if (oldValues != null)
			vo.setJsonObjectOld(objectMapper.readValue(oldValues, clazz));

		return vo;
	}

	private Object getPendingTaskValue(CustomerUserPendingTaskModel pendingTask) throws Exception {
		String strValues = pendingTask.getValuesStr();
		if (strValues != null) {
			return objectMapper.readValue(strValues, Class.forName(pendingTask.getModel()));
		}
		return null;
	}

	private Object getPendingTaskOldValue(CustomerUserPendingTaskModel pendingTask) throws Exception {
		String strValues = pendingTask.getOldValuesStr();
		if (strValues != null) { 
			return objectMapper.readValue(strValues, Class.forName(pendingTask.getModel()));
		}
		return null;
	}

	@Override
	public void expiredPendingTask(String parameter) throws ApplicationException, BusinessException {
		try{
			Timestamp expiryDate = DateUtils.getCurrentTimestamp();
			if(ValueUtils.hasValue(parameter)){
				String[] expiryDateArr = parameter.split("\\=");
				
				if(ValueUtils.hasValue(expiryDateArr[1])){
					expiryDate = new Timestamp(Helper.DATE_TIME_FORMATTER.parse(expiryDateArr[1]).getTime());
				}
			}
			
			Calendar calFrom = Calendar.getInstance();
			calFrom.setTime(expiryDate);
			calFrom.set(Calendar.HOUR_OF_DAY, 0);
			calFrom.set(Calendar.MINUTE, 0);
			calFrom.set(Calendar.SECOND, 0);
			calFrom.set(Calendar.MILLISECOND, 0);
			calFrom.add(Calendar.DAY_OF_MONTH, -1);
			
			Calendar calTo = Calendar.getInstance();
			calTo.setTime(expiryDate);
			calTo.set(Calendar.HOUR_OF_DAY, 0);
			calTo.set(Calendar.MINUTE, 0);
			calTo.set(Calendar.SECOND, 0);
			calTo.set(Calendar.MILLISECOND, 0);
			
			
			if(logger.isDebugEnabled()) {
				logger.debug("calFrom : " + calFrom.getTime());
				logger.debug("calTo : " + calTo.getTime());
			}
			
			//hanya expired transaksi future yg masih pending
			List<String> instructionModeList = new ArrayList<>();
			instructionModeList.add(ApplicationConstants.SI_FUTURE_DATE);
			instructionModeList.add(ApplicationConstants.SI_RECURRING);
			List<CustomerUserPendingTaskModel> pendingList = pendingTaskRepo.findExpiredPendingTask(new Timestamp(calFrom.getTimeInMillis()), 
					new Timestamp(calTo.getTimeInMillis()), instructionModeList);
			
			for(CustomerUserPendingTaskModel pendingTask : pendingList) {
				if(logger.isDebugEnabled()) {
					logger.debug("expired pendingTaskId : " + pendingTask.getId());
				}
				
				//update expired to pending task
				pendingTaskRepo.updatePendingTask(pendingTask.getId(), Status.EXPIRED.name(), DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.EXPIRED);

				//insert into trx status EXPIRE
				trxStatusService.addTransactionStatus(pendingTask.getId(), DateUtils.getCurrentTimestamp(), CustomerTransactionActivityType.EXPIRE, ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.EXPIRED, null, true, null);
			}
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void updatePendingTask(String pendingTaskId, Timestamp activityDate, String activityBy,
			CustomerTransactionStatus trxStatus) {
		pendingTaskRepo.flush(); //harus di flush, jika tidak execute query yg masih di EntityManager bakal di drop
		pendingTaskRepo.updatePendingTask(pendingTaskId, activityDate, activityBy, trxStatus);
	}
	
	private String getCurrencyMatrixCode(String fromCurrency, String toCurrency)
			throws Exception {
		String localCurrency = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_CURRENCY_CODE).getValue();
		
		if (fromCurrency.equalsIgnoreCase(localCurrency) && fromCurrency.equalsIgnoreCase(toCurrency)) {
			return ApplicationConstants.CCY_MTRX_LL;
		} else if (fromCurrency.equalsIgnoreCase(localCurrency) 
				&& !toCurrency.equalsIgnoreCase(fromCurrency)) {
			return ApplicationConstants.CCY_MTRX_LF;
		} else if (toCurrency.equalsIgnoreCase(localCurrency) 
				&& !toCurrency.equalsIgnoreCase(fromCurrency)) {
			return ApplicationConstants.CCY_MTRX_FL;
		}else if (!fromCurrency.equalsIgnoreCase(localCurrency) && fromCurrency.equalsIgnoreCase(toCurrency)) {
			return ApplicationConstants.CCY_MTRX_FS;
		} else if (!fromCurrency.equalsIgnoreCase(localCurrency) && !toCurrency.equalsIgnoreCase(localCurrency)
				&& !fromCurrency.equalsIgnoreCase(toCurrency)) {
			return ApplicationConstants.CCY_MTRX_FC;
		}

		return null;
	}
	
}
