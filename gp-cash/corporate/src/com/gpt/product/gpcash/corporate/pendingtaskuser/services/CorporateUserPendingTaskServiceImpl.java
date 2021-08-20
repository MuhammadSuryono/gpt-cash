package com.gpt.product.gpcash.corporate.pendingtaskuser.services;

import java.math.BigDecimal;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.sysparam.services.SysParamService;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.CorporateWFEngine;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.product.gpcash.approvallevel.model.ApprovalLevelModel;
import com.gpt.product.gpcash.corporate.authorizedlimitscheme.model.AuthorizedLimitSchemeModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateuser.repository.CorporateUserRepository;
import com.gpt.product.gpcash.corporate.notification.TaskNotification;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.repository.CorporateUserPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserWFContext;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionActivityType;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.product.gpcash.corporate.transactionstatus.services.TransactionStatusService;
import com.gpt.product.gpcash.service.model.ServiceModel;

@Service
@Transactional(rollbackFor = Exception.class)
public class CorporateUserPendingTaskServiceImpl implements CorporateUserPendingTaskService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CorporateWFEngine wfEngine;

	@Autowired
	private CorporateUserPendingTaskRepository pendingTaskRepo;

	@Autowired
	private CorporateUserRepository corporateUserRepo;
	
//	@Autowired
//	private AuthorizedLimitSchemeRepository authorizedLimitSchemeRepo;

	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private ApplicationContext appCtx;
	
	@Autowired
	private List<TaskNotification> notifications;
	
	@Autowired
	private TransactionStatusService trxStatusService;
	
	@Autowired
	private SysParamService sysParamService;

	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			CorporateUserPendingTaskVO pendingTaskVO = new CorporateUserPendingTaskVO();
			pendingTaskVO.setReferenceNo((String) map.get("referenceNo"));
			pendingTaskVO.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERCODE));
			pendingTaskVO.setMenuCode((String) map.get("pendingTaskMenuCode"));
			pendingTaskVO.setStatus((String) map.get("status"));

			Page<CorporateUserPendingTaskModel> result = pendingTaskRepo.searchPendingTask(pendingTaskVO,
					PagingUtils.createPageRequest(map));

			resultMap.put("result", setModelToMap(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	@Override
	public Map<String, Object> searchPendingTaskHistoryByReferenceNo(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		try {
			CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findByReferenceNo((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));
			if (pendingTask != null) {
				Map<String, Object> wfResultMap = wfEngine.findTasksHistory(pendingTask.getId());
				return wfResultMap;
			} else {
				throw new BusinessException("GPT-0100001");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	
	}
	
	@Override
	public Map<String, Object> searchPendingTaskHistoryByPendingTaskId(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		try {
			Map<String, Object> wfResultMap = wfEngine.findTasksHistory((String) map.get("pendingTaskId"));
			return wfResultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	
	}
	
	@Override
	public Map<String, Object> searchPendingTaskByReferenceNo(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		try {
			CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findByReferenceNoAndStatusIsPending((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));
			if (pendingTask != null) {
				Map<String, Object> resultMap = wfEngine.findTasksByProcessInstanceId(pendingTask.getId());
				return resultMap;
			} else {
				throw new BusinessException("GPT-0100001");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}	

	@Override
	public Map<String, Object> searchPendingTaskByUser(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = wfEngine.findTasksByUser(map, CorporateWFEngine.Type.CorporateUser);

		return resultMap;
	}

	@Override
	public Map<String, Object> countPendingTaskByUser(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		int count = wfEngine.countActiveTasksByUser((String)map.get(ApplicationConstants.LOGIN_USERCODE));
		Map<String, Object> result = new HashMap<>();
		result.put("total", count);
		return result;
	}
	
	private Map<String, Object> prepareVarsForWorkflow(CorporateUserPendingTaskVO vo) throws Exception {
		Map<String, Object> appVars = new HashMap<>(7, 1);
		appVars.put(ApplicationConstants.KEY_PT_ID, vo.getId());
		appVars.put(ApplicationConstants.KEY_MENU_CODE, vo.getMenuCode());
		appVars.put(ApplicationConstants.KEY_TRX_AMOUNT, vo.getTransactionAmount());
		appVars.put(ApplicationConstants.KEY_TRX_AMOUNT_CCY_CD, vo.getTransactionCurrency());
		appVars.put(ApplicationConstants.KEY_CORP_ID, vo.getCorporateId());
		appVars.put("actionByLevelName", vo.getActionByLevelName());
		appVars.put("actionByLevelAlias", vo.getActionByLevelAlias());
		return appVars;
	}

	private List<Map<String, Object>> setModelToMap(List<CorporateUserPendingTaskModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CorporateUserPendingTaskModel model : list) {
			Map<String, Object> map = new HashMap<>();
			map.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, ValueUtils.getValue(model.getReferenceNo()));
			map.put("status", ValueUtils.getValue(model.getStatus()));
			map.put("pendingTaskMenuCode", ValueUtils.getValue(model.getMenu().getCode()));
			map.put("actionDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("actionBy", ValueUtils.getValue(model.getCreatedBy()));

			resultList.add(map);
		}

		return resultList;
	}

	@Override
	public Map<String, Object> detailPendingTask(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		try {
			String referenceNo = (String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
			
			Map<String, Object> result = new HashMap<>();
			result.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, referenceNo);

			CorporateUserPendingTaskModel model = pendingTaskRepo.findByReferenceNo(referenceNo);
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

			CorporateUserPendingTaskModel model = pendingTaskRepo.findByReferenceNo(referenceNo);
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
	
	private CorporateUserModel isCorpUserValid(String code) throws Exception {
		CorporateUserModel model = corporateUserRepo.findOne(code);

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
	public Map<String, Object> savePendingTask(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			vo.setReferenceNo(Helper.generateCorporateReferenceNo());

			// validasi user
			CorporateUserModel user = isCorpUserValid(vo.getCreatedBy());
			
			AuthorizedLimitSchemeModel authModel = user.getAuthorizedLimit();
			if(authModel != null) {
				vo.setActionByLevelName(authModel.getApprovalLevel().getName());
				vo.setActionByLevelAlias(authModel.getApprovalLevelAlias());
			}
			
			CorporateUserPendingTaskModel pendingTask = new CorporateUserPendingTaskModel();
			pendingTask.setUniqueKey(vo.getUniqueKey());
			pendingTask.setUniqueKeyDisplay(vo.getUniqueKeyDisplay());

			CorporateModel corporate = new CorporateModel();
			corporate.setId(vo.getCorporateId());
			pendingTask.setCorporate(corporate);
			pendingTask.setReferenceNo(vo.getReferenceNo());
			pendingTask.setSenderRefNo(vo.getSenderRefNo());
			pendingTask.setIsFinalPayment(vo.getIsFinalPayment());
			pendingTask.setBenRefNo(vo.getBenRefNo());
			pendingTask.setAction(vo.getAction());
			pendingTask.setCreatedBy(vo.getCreatedBy());
			pendingTask.setCreatedDate(DateUtils.getCurrentTimestamp());
			pendingTask.setStatus(ApplicationConstants.WF_STATUS_PENDING);
			pendingTask.setSessionTime(vo.getSessionTime());
			pendingTask.setUserGroupId(vo.getUserGroupId());
			
			IDMMenuModel menu = idmRepo.getMenuRepo().findOne(vo.getMenuCode());
			pendingTask.setMenu(menu);

			pendingTask.setModel(vo.getJsonObject().getClass().getName());
			pendingTask.setService(vo.getService());
			pendingTask.setSourceAccountGroupDetailId(vo.getSourceAccountGroupDetailId());
			
			//set debit info
			pendingTask.setSourceAccount(vo.getSourceAccount());
			pendingTask.setSourceAccountName(vo.getSourceAccountName());
			pendingTask.setSourceAccountCurrencyCode(vo.getSourceAccountCurrencyCode());
			pendingTask.setSourceAccountCurrencyName(vo.getSourceAccountCurrencyName());
			
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
			
			if(ValueUtils.hasValue(vo.getBillingId())){
				pendingTask.setBillingId(vo.getBillingId());
			}
			
			if (vo.getBillExpiryDate() != null) {
				pendingTask.setBillExpiryDate(vo.getBillExpiryDate());
			}
			
			if (vo.getRefNoSpecialRate() != null) {
				pendingTask.setRefNoSpecialRate(vo.getRefNoSpecialRate());
			}

			if (logger.isDebugEnabled())
				logger.debug("User PendingTask : " + pendingTask);

			pendingTaskRepo.save(pendingTask);

			vo.setId(pendingTask.getId());
			
			// start workflow
			return wfEngine.createInstance(CorporateWFEngine.Type.CorporateUser, vo.getCreatedBy(), pendingTask.getCreatedDate(), prepareVarsForWorkflow(vo));

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public Map<String, Object> approve(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			// get pending task by reference
			CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findByReferenceNoAndStatusIsPending((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));

			if (pendingTask != null) {
				return wfEngine.endUserTask((String) map.get("taskId"), (String) map.get(ApplicationConstants.LOGIN_USERCODE), pendingTask.getId(), Status.APPROVED, null);
			} else {
				throw new BusinessException("GPT-0100001");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void approve(String pendingTaskId, String userId) throws ApplicationException, BusinessException {
		boolean approved = false;
		boolean isTimeOut = false;
		String errorCode = null;
		CorporateUserPendingTaskVO vo = null;
		Timestamp pendingExecuteDate = DateUtils.getCurrentTimestamp();
		Timestamp executedDate = null;		
		
		IDMMenuModel menu = null;
		try {
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n\n\n");
				logger.debug("approve pendingTaskId : " + pendingTaskId);
				logger.debug("\n\n\n\n\n");
			}

			CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findById(pendingTaskId);
			if (pendingTask == null) {
				throw new BusinessException("GPT-0100001");
			}
			
			menu = pendingTask.getMenu();

			CorporateUserWorkflowService service = (CorporateUserWorkflowService) appCtx
					.getBean(pendingTask.getService());

			vo = getVOFromCorporateUserPendingTaskModel(pendingTask);
			vo.setActionBy(userId);
			
			executedDate = DateUtils.getCurrentTimestamp();
			service.approve(vo);
			
			if(ApplicationConstants.SINGLE_TRANSACTION.equals(menu.getSubType())) {
				if(vo.getInstructionMode().equals(ApplicationConstants.SI_IMMEDIATE)) {
					if (ApplicationConstants.YES.equals(vo.getIsError())) {

						//ada pengecekan jika error code terdaftar dalam list errorcode timeout, status diupdate jdi inprogress offline, BSG 26 Feb 2020
						isTimeOut = maintenanceRepo.isTimeOutErrorCodeValid(vo.getErrorCode());
						
						if(isTimeOut) {
							pendingTaskRepo.updatePendingTask(pendingTask.getId(), ApplicationConstants.WF_STATUS_APPROVED, executedDate, userId, TransactionStatus.IN_PROGRESS_OFFLINE);
							pendingTaskRepo.updateErrorTimeoutFlagPendingTask(pendingTaskId, ApplicationConstants.YES);
						}else {
						if(TransactionStatus.EXECUTE_PARTIAL_SUCCESS.name().equals(vo.getErrorCode())) {
							pendingTaskRepo.updatePendingTask(pendingTask.getId(), ApplicationConstants.WF_STATUS_APPROVED, executedDate, userId, TransactionStatus.EXECUTE_PARTIAL_SUCCESS);
						} else {
							pendingTaskRepo.updatePendingTask(pendingTask.getId(), ApplicationConstants.WF_STATUS_APPROVED, executedDate, userId, TransactionStatus.EXECUTE_FAIL);
						}
						}
						
					}else {
						if ("MNU_GPCASH_F_FUND_INT".equals(vo.getMenuCode())) { // International Transfer
							pendingTaskRepo.updatePendingTask(pendingTask.getId(), ApplicationConstants.WF_STATUS_APPROVED, executedDate, userId, TransactionStatus.IN_PROGRESS_OFFLINE);
						} else {
						pendingTaskRepo.updatePendingTask(pendingTask.getId(), ApplicationConstants.WF_STATUS_APPROVED, executedDate, userId, TransactionStatus.EXECUTE_SUCCESS);
					}
					}
				} else {
					pendingTaskRepo.updatePendingTask(pendingTask.getId(), ApplicationConstants.WF_STATUS_APPROVED, executedDate, userId, TransactionStatus.PENDING_EXECUTE);
				}
			} else if(ApplicationConstants.BULK_TRANSACTION.equals(menu.getSubType())) {
				pendingTaskRepo.updatePendingTask(pendingTask.getId(), ApplicationConstants.WF_STATUS_APPROVED, executedDate, userId, TransactionStatus.PENDING_EXECUTE);
			} else { //sub type not SINGLE or BULK
				if (ApplicationConstants.YES.equals(vo.getIsError())) {
					pendingTaskRepo.updatePendingTask(pendingTask.getId(), ApplicationConstants.WF_STATUS_APPROVED, executedDate, userId, TransactionStatus.EXECUTE_FAIL);
				} else {
				pendingTaskRepo.updatePendingTask(pendingTask.getId(), ApplicationConstants.WF_STATUS_APPROVED, executedDate, userId, TransactionStatus.EXECUTE_SUCCESS);
			}
			}
			
			//khusus transaction immidiate on release, instruction date akan di update sesuai dengan tanggal trx di release, agar bisa dicari berdasarkan payment date di trx status
			if(ApplicationConstants.SI_IMMEDIATE.equals(vo.getInstructionMode())) {
				String immediateType = maintenanceRepo.isSysParamValid(SysParamConstants.IMMEDIATE_TYPE).getValue();
				
				if(immediateType.equals(ApplicationConstants.IMMEDIATE_TYPE_RELEASE)){
					pendingTaskRepo.updateInstructionDatePendingTask(pendingTaskId, DateUtils.getCurrentTimestamp());
				}
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
					trxStatusService.addTransactionStatus(pendingTaskId, pendingExecuteDate, TransactionActivityType.RELEASE, userId, TransactionStatus.PENDING_EXECUTE, null, false, null);
					
					if (approved) {				
						if(vo.getInstructionMode().equals(ApplicationConstants.SI_IMMEDIATE)) {
							if (ApplicationConstants.YES.equals(vo.getIsError())) {
								
								if(isTimeOut) {
									trxStatusService.addTransactionStatus(pendingTaskId, DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE, pendingTaskId, true, vo.getErrorCode());
								}else {
								//insert into trx status EXECUTE_FAIL, error code get from VO
								if(TransactionStatus.EXECUTE_PARTIAL_SUCCESS.name().equals(vo.getErrorCode())) {
									trxStatusService.addTransactionStatus(pendingTaskId, DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_PARTIAL_SUCCESS, pendingTaskId, true, vo.getErrorCode());
								} else {
									trxStatusService.addTransactionStatus(pendingTaskId, DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, pendingTaskId, true, vo.getErrorCode());
								}
								}
														
							} else {
								if (!"MNU_GPCASH_F_FUND_INT".equals(vo.getMenuCode())) {
									//insert into trx status EXECUTE_SUCCESS
									trxStatusService.addTransactionStatus(pendingTaskId, executedDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, pendingTaskId, false, null);										
								}else {
									trxStatusService.addTransactionStatus(pendingTaskId, executedDate, TransactionActivityType.RELEASE, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE, pendingTaskId, false, null);
								}
							}
						}
					} else {
						//insert into trx status EXECUTE_FAIL
						trxStatusService.addTransactionStatus(pendingTaskId, DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, pendingTaskId, true, errorCode);
					}	
				} else if(ApplicationConstants.BULK_TRANSACTION.equals(menu.getSubType())) {
					//insert into trx status PENDING_EXECUTE
					trxStatusService.addTransactionStatus(pendingTaskId, pendingExecuteDate, TransactionActivityType.RELEASE, userId, TransactionStatus.PENDING_EXECUTE, null, false, null);
					
					//jika ada yg mau di lakukan sesuatu untuk bulk transaksi maka letakin di sini 
				} else {
					//untuk yg menu type cd nya adalah M seperti beneficiary list
					if (approved) {	
						
						if (ApplicationConstants.YES.equals(vo.getIsError())) {
							trxStatusService.addTransactionStatus(pendingTaskId, pendingExecuteDate, TransactionActivityType.RELEASE, userId, TransactionStatus.EXECUTE_FAIL, pendingTaskId, true, vo.getErrorCode());
						} else {
						//insert into trx status EXECUTE_SUCCESS
						trxStatusService.addTransactionStatus(pendingTaskId, pendingExecuteDate, TransactionActivityType.RELEASE, userId, TransactionStatus.EXECUTE_SUCCESS, null, false, null);
						}						
					} else {
						//insert into trx status EXECUTE_SUCCESS
						trxStatusService.addTransactionStatus(pendingTaskId, pendingExecuteDate, TransactionActivityType.RELEASE, userId, TransactionStatus.EXECUTE_FAIL, null, false, null);
					}
					
				}
			}
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public Map<String, Object> reject(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			// get pending task by reference
			CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findByReferenceNoAndStatusIsPending((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));

			if (pendingTask != null) {
				return wfEngine.endUserTask((String) map.get("taskId"), (String)map.get(ApplicationConstants.LOGIN_USERCODE), pendingTask.getId(), Status.REJECTED, null);
			} else {
				throw new BusinessException("GPT-0100001");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void reject(String pendingTaskId, String userId) throws ApplicationException, BusinessException {
		try {
			CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findById(pendingTaskId);
			if (pendingTask == null) {
				throw new BusinessException("GPT-0100001");
			}

			CorporateUserWorkflowService service = (CorporateUserWorkflowService) appCtx
					.getBean(pendingTask.getService());

			CorporateUserPendingTaskVO vo = getVOFromCorporateUserPendingTaskModel(pendingTask);
			vo.setActionBy(userId);

			service.reject(vo);
			
			pendingTaskRepo.updatePendingTask(pendingTask.getId(), ApplicationConstants.WF_STATUS_REJECTED, DateUtils.getCurrentTimestamp(), userId, TransactionStatus.REJECTED);

			Timestamp now = DateUtils.getCurrentTimestamp();
			
			//insert into trx status PENDING_EXECUTE
			trxStatusService.addTransactionStatus(pendingTaskId, now, TransactionActivityType.REJECT, userId, TransactionStatus.REJECTED, pendingTaskId, true, null);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void checkUniquePendingTask(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			if (ValueUtils.hasValue(pendingTaskRepo.findByUniqueKeyAndStatusAndMenuCodeAndCorporateId(vo.getUniqueKey(), 
					ApplicationConstants.WF_STATUS_PENDING, vo.getMenuCode(), vo.getCorporateId()))) {
				throw new BusinessException("GPT-0100002");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void checkUniquePendingTaskLike(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		try {
			if (ValueUtils.hasValue(pendingTaskRepo.findByUniqueKeyContainingAndStatusAndMenuCodeAndCorporateId(vo.getUniqueKey(), ApplicationConstants.WF_STATUS_PENDING, 
					vo.getMenuCode(), vo.getCorporateId()))) {
				throw new BusinessException("GPT-0100002");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	private CorporateUserPendingTaskVO getVOFromCorporateUserPendingTaskModel(CorporateUserPendingTaskModel pendingTask)
			throws Exception {
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
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
		vo.setCorporateId(pendingTask.getCorporate().getId());
		vo.setSessionTime(pendingTask.getSessionTime());
		vo.setUserGroupId(pendingTask.getUserGroupId());
		
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
		
		vo.setSourceAccountGroupDetailId(pendingTask.getSourceAccountGroupDetailId());
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
		
		vo.setBillingId(pendingTask.getBillingId());
		vo.setBillExpiryDate(pendingTask.getBillExpiryDate());
		
		vo.setRefNoSpecialRate(pendingTask.getRefNoSpecialRate());
		
		Class<?> clazz = Class.forName(pendingTask.getModel());

		vo.setJsonObject(objectMapper.readValue(pendingTask.getValuesStr(), clazz));

		String oldValues = pendingTask.getOldValuesStr();
		if (oldValues != null)
			vo.setJsonObjectOld(objectMapper.readValue(oldValues, clazz));

		return vo;
	}

	private Object getPendingTaskValue(CorporateUserPendingTaskModel pendingTask) throws Exception {
		String strValues = pendingTask.getValuesStr();
		if (strValues != null) {
			return objectMapper.readValue(strValues, Class.forName(pendingTask.getModel()));
		}
		return null;
	}

	private Object getPendingTaskOldValue(CorporateUserPendingTaskModel pendingTask) throws Exception {
		String strValues = pendingTask.getOldValuesStr();
		if (strValues != null) { 
			return objectMapper.readValue(strValues, Class.forName(pendingTask.getModel()));
		}
		return null;
	}

	@Override
	public List<Map<String,String>> getUserForWorkflow(final CorporateUserWFContext profileContext)
			throws ApplicationException, BusinessException {
		if (logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n\nprofileContext.getPendingTaskId = " + profileContext.getPendingTaskId());
			logger.debug("approval level = " + profileContext.getApprovalLevel());
			logger.debug("stage name = " + profileContext.getStageName());
			logger.debug("user group option = " + profileContext.getUserGroupOption());
			logger.debug("user group = " + profileContext.getUserGroup());
			logger.debug("approverUserCode = " + profileContext.getApproverUserCode());
			logger.debug("approverHistory = " + profileContext.getApproverHistory());
			logger.debug("\n\n\n\n\n");
		}

		List<Map<String, String>> wfUsers;

		try {
			CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findOne(profileContext.getPendingTaskId());

			String makerUserCode = pendingTask.getCreatedBy();
			CorporateUserModel userMaker = corporateUserRepo.findOne(makerUserCode);

			String stageName = profileContext.getStageName();
//			String userGroupOption = profileContext.getUserGroupOption();
			String approverUserCode = ApplicationConstants.EMPTY_STRING;
			String approverUserGroupId = ApplicationConstants.EMPTY_STRING;
			
			//penggunaan approverUserGroupId hny untuk approver stage
			if(ApplicationConstants.WF_STAGE_APPROVE.equals(stageName)) {
				approverUserCode = profileContext.getApproverUserCode();
				CorporateUserModel userApprover = corporateUserRepo.findOne(approverUserCode);
				approverUserGroupId = userApprover.getCorporateUserGroup().getId();
			}
			
			List<String> approverHistory = profileContext.getApproverHistory();

//			CorporateModel corporate = pendingTask.getCorporate();
//			String corporateId = corporate.getId();
			
			boolean isTransactionMenu = ApplicationConstants.MENU_TYPE_TRANSACTION.equals(pendingTask.getMenu().getMenuType().getCode());
			boolean isFinancial = ApplicationConstants.YES.equals(pendingTask.getMenu().getIsFinancialFlag());
			
			final List<CorporateUserModel> userList = getUserList(profileContext, pendingTask.getCorporate(), userMaker, pendingTask, isTransactionMenu);
			
			if(userList.isEmpty())
				throw new BusinessException("GPT-0100031");

//			Map<String, AuthorizedLimitSchemeModel> authorizedListMap = new HashMap<>();

			BigDecimal transactionAmount = pendingTask.getTransactionAmount();
			
			wfUsers = new ArrayList<>(userList.size());
			
			for(int i = 0; i < userList.size(); i++) {
				CorporateUserModel assignedUser = userList.get(i);
				
				AuthorizedLimitSchemeModel als = assignedUser.getAuthorizedLimit();
				
				//seperti nya yg dibawah ini tidak perlu lg, tp di remark dl untuk beberapa saat. (variable yg gak jadi dipake yg di atas juga di remark)
//				String approvalLevelCode = assignedUser.getApprovalMap().getApprovalLevel().getCode();
//				if (authorizedListMap.containsKey(approvalLevelCode)) {
//					als = authorizedListMap.get(approvalLevelCode);
//				} else {
//					als = authorizedLimitSchemeRepo.findByCorporateIdAndApprovalLevelCode(corporateId, approvalLevelCode);
//
//					authorizedListMap.put(approvalLevelCode, als);
//				}

				IDMUserModel idmUser = assignedUser.getUser();
				String assignedUserId = idmUser.getCode();
				ApprovalLevelModel assignedUserLevel = als.getApprovalLevel();
				String assignedUserGroupId = assignedUser.getCorporateUserGroup().getId();
				
				// if releaser or isTransactionMenu = false then no need to check limit
				boolean removed = true;
				if (!isTransactionMenu|| ApplicationConstants.WF_STAGE_RELEASE.equalsIgnoreCase(stageName)) {
					removed = false;
				} else {
					//check to history, jika sudah pernah approve maka skip, di releaser tidak perlu di cek karena meskipun dy approve di approver stage, tetep boleh release di releaserStage
					if(!approverHistory.contains(assignedUserId)) {
						//requested by Susi and Diaz (28 Sept 2017), jika single approval limit > transaction amount, maka user tersebut HARUS dapat task
						if (als.getSingleApprovalLimit().compareTo(transactionAmount) >= 0) {
							removed = false;
						} else {
							if (als.getIntraGroupApprovalLimit().compareTo(transactionAmount) >= 0
									&& approverUserGroupId.equals(assignedUserGroupId)) {
								logger.debug("assignedUserId intra approval limit : {}", assignedUserId);
								removed = false;
							} else if (als.getCrossGroupApprovalLimit().compareTo(transactionAmount) >= 0
									&& !approverUserGroupId.equals(assignedUserGroupId)) {
								logger.debug("assignedUserId cross approval limit : {}", assignedUserId);
								removed = false;
							}
						}
					}
				}
				
				if(removed) {
					if(isTransactionMenu || isFinancial) {
						// need to remove this user from the list since it will be used later for notification below
						userList.remove(i--);
					}
				} else {
					Map<String, String> assignedUserMap = new HashMap<>();
					assignedUserMap.put("assignedUserId", assignedUserId);
					assignedUserMap.put("assignedUserLevelCode", assignedUserLevel.getCode());
					assignedUserMap.put("assignedUserLevelName", assignedUserLevel.getName());
					assignedUserMap.put("assignedUserLevelAlias", als.getApprovalLevelAlias());
					assignedUserMap.put("assignedUserGroupId", assignedUserGroupId);
					
					if(logger.isDebugEnabled()) {
						logger.debug("\n\n\n\n\n");
						logger.debug("assignedUser : " + assignedUserId);
						logger.debug("assignedUserGroupId : " + assignedUserGroupId);
						logger.debug("approverUserGroupId : " + approverUserGroupId);
//						logger.debug("authorizedListMap.approvalLevelCode : " + approvalLevelCode);
						logger.debug("als.getIntraGroupApprovalLimit() : " + als.getIntraGroupApprovalLimit());
						logger.debug("als.getCrossGroupApprovalLimit() : " + als.getCrossGroupApprovalLimit());
						logger.debug("als.getSingleApprovalLimit() : " + als.getSingleApprovalLimit());
						logger.debug("transactionAmount : " + transactionAmount);
						logger.debug("\n\n\n\n\n");
					}
					
					wfUsers.add(assignedUserMap);					
				}
			}
			
			//add condition for menu cheque and liquidity
			if(isTransactionMenu || isFinancial) {
				TransactionSynchronizationManager.registerSynchronization(new NotificationSender(profileContext.getPendingTaskId(), userList));
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Assign tasks to = {}", wfUsers);
			logger.debug("\n\n\n\n\n");
		}

		return wfUsers;
	}
	
	@Override
	public boolean isSkipToReleaser(String approverUserCode, BigDecimal transactionAmount) {
		CorporateUserModel user = corporateUserRepo.findOne(approverUserCode);
		AuthorizedLimitSchemeModel als = user.getAuthorizedLimit();
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("approverUserCode : " + approverUserCode);
			logger.debug("als.getSingleApprovalLimit() : " + als.getSingleApprovalLimit());
			logger.debug("transactionAmount : " + transactionAmount);
			logger.debug("\n\n\n\n\n");
		}
		
		//requested by Susi and Diaz (28 Sept 2017), jika single approval limit > transaction amount, maka skip lgsg ke releaser stage, karena dianggap super user
		if (als.getSingleApprovalLimit().compareTo(transactionAmount) >= 0) {
			return true;
		}
		
		return false;
	}
	
	private List<CorporateUserModel> getUserList(CorporateUserWFContext profileContext, CorporateModel corporate,
			CorporateUserModel corporateUser, CorporateUserPendingTaskModel pendingTask, boolean isTransactionMenu) throws Exception {
		String corporateId = corporate.getId();
		String sourceAccountGroupDetailId = pendingTask.getSourceAccountGroupDetailId();
		String menuCode = pendingTask.getMenu().getCode();

		String stageName = profileContext.getStageName();
		String userGroupOption = profileContext.getUserGroupOption();
		if(!ValueUtils.hasValue(userGroupOption)){
			userGroupOption = ApplicationConstants.USER_GROUP_OPTION_ANY_GROUP;
		}

		String approvalLevel = profileContext.getApprovalLevel();
		
		// jika menu nya adalah non transaksi maka sourceAccount = wildcard
		if(!isTransactionMenu){
			sourceAccountGroupDetailId = ApplicationConstants.WILDCARD;
		}

		String isReleaser = ApplicationConstants.NO;

		String wfCategory = ApplicationConstants.EMPTY_STRING;
		String wfRoleCode = ApplicationConstants.EMPTY_STRING;
		String userGroupId = corporateUser.getCorporateUserGroup().getId();

		if (ApplicationConstants.WF_STAGE_APPROVE.equalsIgnoreCase(stageName)) {
			wfCategory = ApplicationConstants.ROLE_WF_CRP_USR_CATEGORY;
			wfRoleCode = ApplicationConstants.WF_ROLE_APPROVER;
			if (!ValueUtils.hasValue(approvalLevel)) {
				approvalLevel = ApplicationConstants.WILDCARD;
			}

		} else if (ApplicationConstants.WF_STAGE_RELEASE.equalsIgnoreCase(stageName)) {
			wfCategory = ApplicationConstants.ROLE_WF_CRP_USR_CATEGORY;
			wfRoleCode = ApplicationConstants.WF_ROLE_RELEASER;
			approvalLevel = ApplicationConstants.WILDCARD;

			isReleaser = ApplicationConstants.YES;
		}

		//get approvalMapId berdasarkan wfCategory, wfRoleCode dan approvalLevel
		List<String> approvalMapList = idmRepo.getUserRepo().findApprovalMapForWorkflow(wfCategory, wfRoleCode, 
				approvalLevel);
		
		if(logger.isDebugEnabled()) {
			logger.debug("approvalMapList : " + approvalMapList);
			logger.debug("isReleaser : " + isReleaser);
		}
		
		if(approvalMapList.isEmpty())
			throw new BusinessException("GPT-0100031");
		
		//get user berdasarkan approvalMapId dan berdasarkan userGroupOption
		List<String> userByApprovalMap = getUserListByUserGroupOption(userGroupOption, corporateId, 
				userGroupId, profileContext.getUserGroup(), approvalMapList);
				
		if(logger.isDebugEnabled()) {
			logger.debug("userByApprovalMap : " + userByApprovalMap);
		}
		
		if(userByApprovalMap.isEmpty())
			throw new BusinessException("GPT-0100031");
		
		//get users yg dapat mengakses menuCode yg di kirim
		List<String> userList = idmRepo.getUserRepo().findAuthorizedUserForWorkflow(menuCode, userByApprovalMap);
		
		if(logger.isDebugEnabled()) {
			logger.debug("userList : " + userList);
		}
		
		if (userList.isEmpty()){
			//validasi user di lakukan di wf
			return new ArrayList<>();
		}
			
		String userMaker = pendingTask.getCreatedBy();

		List<String> authorizedUserByMenu = new ArrayList<>();
		for (String user : userList) {
			// validasi user yg di routing tidak boleh sama dengan maker dan
			// user tidak double
			if (!userMaker.equals(user) && ValueUtils.hasValue(user)
					&& !authorizedUserByMenu.contains(user)) {

				authorizedUserByMenu.add(user);
			}
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("authorizedUserByMenu : " + authorizedUserByMenu);
		}
		
		if(authorizedUserByMenu.isEmpty())
			throw new BusinessException("GPT-0100031");

		List<CorporateUserModel> authorizedUserByOption = new ArrayList<>();
		if (userGroupOption.equals(ApplicationConstants.USER_GROUP_OPTION_INTRA_GROUP)) {
			authorizedUserByOption = corporateUserRepo.findUserIncludeUserGroupIdForWorkflow(corporateId,
					userGroupId, sourceAccountGroupDetailId, authorizedUserByMenu, approvalMapList);
		}

		if (userGroupOption.equals(ApplicationConstants.USER_GROUP_OPTION_CROSS_GROUP)) {
			//mencari cross group tetapi role harus approver
			authorizedUserByOption = corporateUserRepo.findUserNotIncludeUserGroupIdForWorkflow(corporateId,
					userGroupId, sourceAccountGroupDetailId, authorizedUserByMenu, approvalMapList);
		}

		if (userGroupOption.equals(ApplicationConstants.USER_GROUP_OPTION_SPECIFY_GROUP)) {
			//jika any group maka search by account number bukan accountGroupDetailId karena sourceAccountGroupDetailId spesific dimiliki account group maker
			if(!isTransactionMenu) {
				authorizedUserByOption = corporateUserRepo.findUserForWorkflowSpesificGroupNonTransaction(corporateId,
						profileContext.getUserGroup(), authorizedUserByMenu, approvalMapList);
			} else {
				authorizedUserByOption = corporateUserRepo.findUserForWorkflowSpesificGroup(corporateId,
						profileContext.getUserGroup(), pendingTask.getSourceAccount(), authorizedUserByMenu, approvalMapList);
			}
			
		}

		// jika releaser atau user group any maka wildcard
		if (isReleaser.equals(ApplicationConstants.YES)
				|| userGroupOption.equals(ApplicationConstants.USER_GROUP_OPTION_ANY_GROUP)) {
			//jika any group maka search by account number bukan accountGroupDetailId karena sourceAccountGroupDetailId spesific dimiliki account group maker
			if(!isTransactionMenu) {
				authorizedUserByOption = corporateUserRepo.findUserForWorkflowNonTransaction(corporateId,
						authorizedUserByMenu);
			} else {
				authorizedUserByOption = corporateUserRepo.findUserForWorkflow(corporateId, pendingTask.getSourceAccount(),
						authorizedUserByMenu);
			}
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("authorizedUserByOption : " + authorizedUserByOption.size());
		}
		
		List<CorporateUserModel> wfUsers = new ArrayList<>();
		for (CorporateUserModel userCorporate : authorizedUserByOption) {
			// validasi user yg di routing tidak boleh sama dengan maker dan
			// user tidak double
			IDMUserModel user = userCorporate.getUser();
			
			if (!userMaker.equals(user.getCode()) && ValueUtils.hasValue(user.getCode())
					&& !wfUsers.contains(userCorporate)) {
				
				wfUsers.add(userCorporate);
			}
		}
		
		return wfUsers;
	}
	
	private List<String> getUserListByUserGroupOption(String userGroupOption, String corporateId, String userGroupId, 
			String spesifyUserGroupId,List<String> approvalMapList) throws Exception{
		List<String> userByApprovalMap = null;
		
		if (userGroupOption.equals(ApplicationConstants.USER_GROUP_OPTION_INTRA_GROUP)) {
			userByApprovalMap = corporateUserRepo.findAuthorizedUserByApprovalMapForWorkflowByUserGroup(corporateId, approvalMapList, userGroupId);
		} else if (userGroupOption.equals(ApplicationConstants.USER_GROUP_OPTION_CROSS_GROUP)) {
			userByApprovalMap = corporateUserRepo.findAuthorizedUserByApprovalMapForWorkflowCROSSUserGroup(corporateId, approvalMapList, userGroupId);
		}else if (userGroupOption.equals(ApplicationConstants.USER_GROUP_OPTION_SPECIFY_GROUP)) {
			userByApprovalMap = corporateUserRepo.findAuthorizedUserByApprovalMapForWorkflowByUserGroup(corporateId, approvalMapList, spesifyUserGroupId);
		} else { //else = ApplicationConstants.USER_GROUP_OPTION_ANY_GROUP
			userByApprovalMap = corporateUserRepo.findAuthorizedUserByApprovalMapForWorkflowANYUserGroup(corporateId, approvalMapList);
		}
		
		return userByApprovalMap;
	}
	
	@Override
	public void expiredPendingTask(String parameter) throws ApplicationException, BusinessException {
		try{
			Timestamp expiryDate = DateUtils.getCurrentTimestamp();
			int sessionTimeValue = 0;
			int indexDate = 0;
			
			if(ValueUtils.hasValue(parameter)){
				String[] parameterArr = parameter.split("\\|");
				
				if(parameterArr.length > 1) {
					sessionTimeValue = Integer.valueOf(parameterArr[0]);
					indexDate = 1;					
				}
				String[] expiryDateArr = parameterArr[indexDate].split("\\=");
				
				if(expiryDateArr.length > 1) {
					if(ValueUtils.hasValue(expiryDateArr[1])){
						expiryDate = new Timestamp(Helper.DATE_TIME_FORMATTER.parse(expiryDateArr[1]).getTime());
					}
				}else {
					sessionTimeValue = Integer.valueOf(parameterArr[0]);
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
			

			List<String> instructionModeList = new ArrayList<>();
			instructionModeList.add(ApplicationConstants.SI_FUTURE_DATE);
			instructionModeList.add(ApplicationConstants.SI_RECURRING);
						
			List<String> sessionTimeList = sysParamService.getTransactionSessionTime();
			if(sessionTimeValue > 0 && sessionTimeValue <= sessionTimeList.size()) { //for scheduler per session
				
				calFrom = DateUtils.getEarliestDate(expiryDate);
				
				String[] sessionTimeEnd = sessionTimeList.get(sessionTimeValue -1).split("\\:");
				int sessionTimeHourEnd = Integer.valueOf(sessionTimeEnd[0]);
				int sessionTimeMinuteEnd = Integer.valueOf(sessionTimeEnd[1]);
				
				calTo.set(Calendar.HOUR_OF_DAY, sessionTimeHourEnd);
				calTo.set(Calendar.MINUTE, sessionTimeMinuteEnd);
				calTo.set(Calendar.SECOND, 0);
				calTo.set(Calendar.MILLISECOND, 0);
				
			}else { // scheduler per day
				String immediateType = maintenanceRepo.isSysParamValid(SysParamConstants.IMMEDIATE_TYPE).getValue();
				
				if(immediateType.equals(ApplicationConstants.IMMEDIATE_TYPE_CREATE)){
					instructionModeList.add(ApplicationConstants.SI_IMMEDIATE);
				}
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("calFrom : " + calFrom.getTime());
				logger.debug("calTo : " + calTo.getTime());
			}
			

			List<CorporateUserPendingTaskModel> pendingList = pendingTaskRepo.findExpiredPendingTask(new Timestamp(calFrom.getTimeInMillis()), 
					new Timestamp(calTo.getTimeInMillis()), instructionModeList);
			
			for(CorporateUserPendingTaskModel pendingTask : pendingList) {
				if(logger.isDebugEnabled()) {
					logger.debug("expired pendingTaskId : " + pendingTask.getId());
				}
				
				//expired to wf engine
				try {
					wfEngine.expireInstance(pendingTask.getId());
				} catch (Exception e) {
					logger.debug(e.getMessage() + " " + pendingTask.getId());
				}
				
				
				//update expired to pending task
				pendingTaskRepo.updatePendingTask(pendingTask.getId(), Status.EXPIRED.name(), DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXPIRED);

				//insert into trx status EXPIRE
				trxStatusService.addTransactionStatus(pendingTask.getId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXPIRE, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXPIRED, null, true, null);
			}
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getCorporateUserEmailWithNotifyTrx(String pendingTaskId) throws ApplicationException, BusinessException {
		try {
			CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findOne(pendingTaskId);
			
			if (pendingTask != null) {
				Map<String, Object> wfResultMap = wfEngine.findTasksHistory(pendingTask.getId());
				
				List<Map<String, String>> historyList = (List<Map<String,String>>) wfResultMap.get("activities");
				if (historyList.size() > 0) {
					List<String> emails = new ArrayList<>();
					for(Map<String, String> taskMap : historyList) {
						String userCode = (String) taskMap.get("userCode");
						if(userCode != null) {
							CorporateUserModel corporateUser = corporateUserRepo.findOne(userCode);
							if(corporateUser != null && ApplicationConstants.YES.equals(corporateUser.getIsNotifyMyTrx())) {
								String email = corporateUser.getUser().getEmail(); 
								if (ValueUtils.hasValue(email))
									emails.add(email);
							}
						}
					}
					return emails;
				}	
				return null;
			} else {
				throw new BusinessException("GPT-0100001");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void updatePendingTask(String pendingTaskId, Timestamp activityDate, String activityBy,
			TransactionStatus trxStatus) {
		pendingTaskRepo.flush(); //harus di flush, jika tidak execute query yg masih di EntityManager bakal di drop
		pendingTaskRepo.updatePendingTask(pendingTaskId, activityDate, activityBy, trxStatus);
	}
	
	@Override
	public void validateFinalPayment(String senderRefNo, String menuCode, String corporateId) throws BusinessException {
		List<CorporateUserPendingTaskModel> pendingList = pendingTaskRepo.findBySenderRefNoAndMenuCodeAndCorporateId(senderRefNo, menuCode, corporateId);
		
		if(pendingList.size() > 0) {
			throw new BusinessException("GPT-0100199");
		}
	}

	@Override
	public Map<String, Object> searchPendingTaskHistoryByPendingTaskId(String pendingTaskId)
			throws ApplicationException, BusinessException {
		try {
			Map<String, Object> wfResultMap = wfEngine.findTasksHistory(pendingTaskId);
			return wfResultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	
	}
	
	class NotificationSender extends TransactionSynchronizationAdapter {
		List<CorporateUserModel> userList;
		String pendingTaskId;
		
		NotificationSender(String pendingTaskId, List<CorporateUserModel> userList) {
			this.pendingTaskId = pendingTaskId;
			this.userList = userList;
		}
		
		@Override
		public void afterCommit() {
			CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findOne(pendingTaskId);
			
			for(TaskNotification sender : notifications) {
				try {
					sender.sendNotification(pendingTask, userList);
				} catch(Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}
	
}
