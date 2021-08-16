package com.gpt.product.gpcash.corporate.transaction.va.paymentva.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.spring.invoker.SpringBeanInvokerHelper;
import com.gpt.component.common.spring.invoker.SpringBeanInvokerHelper.SpringBeanInvokerData;
import com.gpt.component.common.spring.invoker.spi.ISpringBeanInvoker;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.maintenance.errormapping.model.ErrorMappingModel;
import com.gpt.component.maintenance.errormapping.repository.ErrorMappingRepository;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.sysparam.services.SysParamService;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.CorporateWFEngine;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporatecharge.model.CorporateChargeModel;
import com.gpt.product.gpcash.corporate.corporatecharge.repository.CorporateChargeRepository;
import com.gpt.product.gpcash.corporate.corporatecharge.services.CorporateChargeService;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.repository.CorporateUserPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.services.GlobalTransactionService;
import com.gpt.product.gpcash.corporate.transaction.utils.InstructionModeUtils;
import com.gpt.product.gpcash.corporate.transaction.va.paymentva.model.PaymentVAModel;
import com.gpt.product.gpcash.corporate.transaction.va.paymentva.repository.PaymentVARepository;
import com.gpt.product.gpcash.corporate.transaction.validation.services.TransactionValidationService;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionActivityType;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.product.gpcash.corporate.transactionstatus.model.TransactionStatusModel;
import com.gpt.product.gpcash.corporate.transactionstatus.repository.TransactionStatusRepository;
import com.gpt.product.gpcash.corporate.transactionstatus.services.TransactionStatusService;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;
import com.gpt.product.gpcash.service.model.ServiceModel;
import com.gpt.product.gpcash.servicecharge.model.ServiceChargeModel;
import com.gpt.product.gpcash.utils.ProductRepository;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

@Service
@Transactional(rollbackFor = Exception.class)
public class PaymentVAServiceImpl implements PaymentVAService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.batch.transations.timeout-invoker}")
	private int invokerTimeout;
	
	@Value("${gpcash.batch.transations.timeout-job}")
	private int jobTimeout;

	@Value("${gpcash.batch.transations.concurrencies}")
	private int concurrencies;

	@Autowired
	private PaymentVARepository paymentVARepo;

	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;

	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;

	@Autowired
	private CorporateChargeService corporateChargeService;

	@Autowired
	private TransactionValidationService transactionValidationService;
	
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private ErrorMappingRepository errorMappingRepo;	
	
	@Autowired
	private SysParamService sysParamService;
	
	@Autowired
	private CorporateChargeRepository corporateChargeRepo;
	
	@Autowired
	private EAIEngine eaiAdapter;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;	
	
	@Autowired
	private MessageSource message;	
	
	@Autowired
	private ISpringBeanInvoker invoker;	
	
	@Autowired
	private TransactionStatusService trxStatusService;
	
	@Autowired
	private TransactionStatusRepository trxStatusRepo;
	
	@Autowired
	private GlobalTransactionService globalTransactionService;
	
	@Autowired
	private CorporateUserPendingTaskRepository pendingTaskRepo;
	
	@Autowired
	private CorporateWFEngine wfEngine;
	
	@Value("${gpcash.report.folder}")
	private String reportFolder;
	
	@Value("${gpcash.report.bank.logo}")
	private String bankLogo;
	
	@Value("${gpcash.report.money.format:#,###.00}")
	private String moneyFormat;
	
	@Value("${gpcash.trxstatus.download.path}")
	private String pathDownload;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			String instructionMode = (String) map.get("instructionMode");

			//override instructionDate if Immediate to get server timestamp
			if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
				map.put(ApplicationConstants.INSTRUCTION_DATE, DateUtils.getCurrentTimestamp());
			}
			
			checkCustomValidation(map);
			
			BigDecimal transactionAmount = (BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT);
			BigDecimal transactionAmountInquiry = (BigDecimal) map.get("transactionAmountInquiry");
			String vaAccountType = (String) map.get("vaAccountType");
			
			
			if(vaAccountType.equals("O") && transactionAmountInquiry.compareTo(BigDecimal.ZERO)!=0) {
				validateOpenAmount(transactionAmount,transactionAmountInquiry);
			}

			CorporateUserPendingTaskVO vo = setCorporateUserPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
			} else {
				throw new BusinessException("GPT-0100003");
			}

			resultMap.putAll(pendingTaskService.savePendingTask(vo));
			resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String senderRefNo = (String) map.get("senderRefNo");
			
			//validate final payment
			validateFinalPayment(corporateId, senderRefNo);

			// TODO implement calculate equivalent amount in future if implement
			// cross currency transaction
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
	
	private void validateOpenAmount(BigDecimal transactionAmount, BigDecimal transactionAmountInquiry) throws BusinessException {
		// TODO Auto-generated method stub
		if(transactionAmount.compareTo(transactionAmountInquiry) > 0) {
			throw new BusinessException("GPT-0200023");
		}
	}

	private void validateFinalPayment(String corporateId, String senderRefNo) throws BusinessException {
		pendingTaskService.validateFinalPayment(senderRefNo, PaymentVASC.menuCode, corporateId);
		
		List<PaymentVAModel> modelList = paymentVARepo.findTransactionForFinalPaymentFlag(corporateId, 
				ApplicationConstants.YES, senderRefNo);
		
		if(modelList.size() > 0) {
			throw new BusinessException("GPT-0100199");
		}
	}

	private CorporateUserPendingTaskVO setCorporateUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		CorporateUserModel maker = corporateUtilsRepo.isCorporateUserValid(createdBy);
		String userGroupId = maker.getCorporateUserGroup().getId();
		String isFinalPayment = (String) map.get("isFinalPayment");

		String accountGroupDetailId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
		BigDecimal transactionAmount = (BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT);
		String vaAccountNo = (String) map.get("vaAccountNo");
		String uniqueKey = createdBy.concat(ApplicationConstants.DELIMITER_PIPE).concat(accountGroupDetailId)
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(transactionAmount.toPlainString())
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(vaAccountNo)
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(String.valueOf((DateUtils.getCurrentTimestamp().getTime())));

		String instructionMode = (String) map.get(ApplicationConstants.INSTRUCTION_MODE);
		String sessionTime = (String) map.get("sessionTime");
		Timestamp instructionDate = (Timestamp) map.get(ApplicationConstants.INSTRUCTION_DATE);
	
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		vo.setUniqueKey(uniqueKey);
		vo.setCreatedBy(createdBy);
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setTransactionServiceCode((String) map.get(ApplicationConstants.TRANS_SERVICE_CODE));
		vo.setService("PaymentVASC");
		vo.setCorporateId(corporateId);
		vo.setTransactionAmount(transactionAmount);
		vo.setTransactionCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		vo.setSourceAccountGroupDetailId(accountGroupDetailId);
		vo.setSessionTime(sessionTime);
		vo.setIsFinalPayment(isFinalPayment);
		//set userGroupId
		vo.setUserGroupId(userGroupId);
		
		CorporateAccountGroupDetailModel gdm = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(vo.getCorporateId(), 
				vo.getSourceAccountGroupDetailId());		

		//set debit info
		AccountModel sourceAccount = gdm.getCorporateAccount().getAccount();
		vo.setSourceAccount(sourceAccount.getAccountNo());
		vo.setSourceAccountName(sourceAccount.getAccountName());
		
		CurrencyModel sourceCurrency = sourceAccount.getCurrency();
		vo.setSourceAccountCurrencyCode(sourceCurrency.getCode());
		vo.setSourceAccountCurrencyName(sourceCurrency.getName());
		
		vo.setTotalChargeEquivalentAmount((BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_CHARGE));
		vo.setTotalDebitedEquivalentAmount((BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT));
		vo.setInstructionMode(instructionMode);
		
		vo.setSenderRefNo((String) map.get("senderRefNo"));
		
		//if recurring, then instruction Date pertama kali adalah recurringStartDate
		if(ApplicationConstants.SI_RECURRING.equals(instructionMode)) {
			String recurringParamType = (String) map.get("recurringParamType"); 
			int recurringParam = (Integer) map.get("recurringParam");
			
			vo.setRecurringParamType(recurringParamType);
			vo.setRecurringParam(recurringParam);
			Timestamp recurringInstructionDate = InstructionModeUtils.getStandingInstructionDate(recurringParamType, recurringParam, 
					(Timestamp) map.get("recurringStartDate"));
			
			recurringInstructionDate = DateUtils.getInstructionDateBySessionTime(sessionTime, recurringInstructionDate);
			vo.setInstructionDate(recurringInstructionDate);
			vo.setRecurringStartDate(recurringInstructionDate);
			vo.setRecurringEndDate(DateUtils.getInstructionDateBySessionTime(sessionTime, (Timestamp) map.get("recurringEndDate")));
		} else {
			vo.setInstructionDate(DateUtils.getInstructionDateBySessionTime(sessionTime, instructionDate));
		}
				
		vo.setTaskExpiryDate(transactionValidationService.validateExpiryDate(instructionMode, instructionDate));
		
		return vo;
	}

	@SuppressWarnings("unchecked")
	private PaymentVAModel setMapToModel(PaymentVAModel paymentVa, Map<String, Object> map,
			boolean isNew, CorporateUserPendingTaskVO vo) throws Exception {
		CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(vo.getCreatedBy());
		CorporateUserGroupModel corporateUserGroup = corporateUser.getCorporateUserGroup();

		String transactionServiceCode = (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE);
		String instructionMode = (String) map.get("instructionMode");

		Timestamp instructionDate = vo.getInstructionDate();
		if(ApplicationConstants.SI_RECURRING.equals(vo.getInstructionMode())) {
			//override instructionDate to today timestamp
			Timestamp recurringInstructionDate = InstructionModeUtils.getStandingInstructionDate(vo.getRecurringParamType(), vo.getRecurringParam(), 
					DateUtils.getCurrentTimestamp());
			
			instructionDate = DateUtils.getInstructionDateBySessionTime(vo.getSessionTime(), recurringInstructionDate);
		}
		
		String productName = (String) map.get("productName");
		String vaAccountNo = (String) map.get("vaAccountNo");
		String vaAccountType = (String) map.get("vaAccountType");
		
		// set ID
		paymentVa.setId(vo.getId());

		// set transaction information
		paymentVa.setReferenceNo(vo.getReferenceNo());

		IDMMenuModel menu = new IDMMenuModel();
		menu.setCode(vo.getMenuCode());
		paymentVa.setMenu(menu);
		
		CorporateModel corpModel = corporateUtilsRepo.isCorporateValid(vo.getCorporateId());
		paymentVa.setCorporate(corpModel);
		
		paymentVa.setCorporateUserGroup(corporateUserGroup);
		
		IDMApplicationModel application = new IDMApplicationModel();
		//application code diambil dr yg login
		application.setCode((String) map.get(ApplicationConstants.APP_CODE));
		paymentVa.setApplication(application);
		
		AccountModel sourceAccountModel = productRepo.isAccountValid(vo.getSourceAccount());
		paymentVa.setSourceAccount(sourceAccountModel);		
		
		paymentVa.setTransactionAmount(new BigDecimal(map.get(ApplicationConstants.TRANS_AMOUNT).toString()));
		paymentVa.setTransactionCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		paymentVa.setTotalChargeEquivalentAmount(
				new BigDecimal(map.get(ApplicationConstants.TRANS_TOTAL_CHARGE).toString()));
		paymentVa.setTotalDebitedEquivalentAmount(
				new BigDecimal(map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT).toString()));

		// set transaction service code
		ServiceModel service = new ServiceModel();
		service.setCode(transactionServiceCode);
		paymentVa.setService(service);

		
		// set additional information
		paymentVa.setVaAccountNo(vaAccountNo);
		paymentVa.setProductName(productName);
		paymentVa.setVaAccountType(vaAccountType);
		paymentVa.setVaAccountName((String) map.get("vaAccountName"));
		paymentVa.setInstitutionName((String) map.get("institutionName"));
		
		paymentVa.setRemark((String) map.get("remark"));
		paymentVa.setSenderRefNo((String) map.get("senderRefNo"));
		paymentVa.setIsFinalPayment((String) map.get("isFinalPayment"));
		paymentVa.setBenRefNo((String) map.get("benRefNo"));
		paymentVa.setIsNotifyBen((String) map.get("isNotify"));
		paymentVa.setNotifyBenValue((String) map.get("notifyBenValue"));
		
		setInstructionMode(paymentVa, instructionMode, instructionDate, map);
		
		setCharge(paymentVa, (ArrayList<Map<String,Object>>) map.get("chargeList"));
		
		return paymentVa;
	}
	
	private void setCharge(PaymentVAModel paymentVa, List<Map<String, Object>> chargeList){
		for(int i=1; i <= chargeList.size(); i++){
			Map<String, Object> chargeMap = chargeList.get(i - 1);
			String chargeId = (String) chargeMap.get("id");
			BigDecimal value = new BigDecimal(chargeMap.get("value").toString());
			String currencyCode = (String) chargeMap.get("currencyCode");
			
			if(i == 1){
				paymentVa.setChargeType1(chargeId);
				paymentVa.setChargeTypeAmount1(value);
				paymentVa.setChargeTypeCurrency1(currencyCode);
			} else if(i == 2){
				paymentVa.setChargeType2(chargeId);
				paymentVa.setChargeTypeAmount2(value);
				paymentVa.setChargeTypeCurrency2(currencyCode);
			} else if(i == 3){
				paymentVa.setChargeType3(chargeId);
				paymentVa.setChargeTypeAmount3(value);
				paymentVa.setChargeTypeCurrency3(currencyCode);
			} else if(i == 4){
				paymentVa.setChargeType4(chargeId);
				paymentVa.setChargeTypeAmount4(value);
				paymentVa.setChargeTypeCurrency4(currencyCode);
			} else if(i == 5){
				paymentVa.setChargeType5(chargeId);
				paymentVa.setChargeTypeAmount5(value);
				paymentVa.setChargeTypeCurrency5(currencyCode);
			} 
		}
	}

	private void setInstructionMode(PaymentVAModel paymentVa, String instructionMode, Timestamp instructionDate, 
			Map<String, Object> map) throws Exception {
		
		
		// set instruction mode
		paymentVa.setInstructionMode(instructionMode);

		if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
			paymentVa
					.setInstructionDate(transactionValidationService.getInstructionDateForRelease(instructionMode, instructionDate));
		} else if (instructionMode.equals(ApplicationConstants.SI_FUTURE_DATE)) {
			paymentVa.setInstructionDate(instructionDate);
		} else if (instructionMode.equals(ApplicationConstants.SI_RECURRING)) {
			paymentVa.setRecurringParamType((String) map.get("recurringParamType"));
			paymentVa.setRecurringParam((Integer) map.get("recurringParam"));
			paymentVa.setRecurringStartDate(instructionDate);
			paymentVa.setRecurringEndDate((Timestamp) Helper.parseStringToTimestamp((String) map.get("recurringEndDate")));
			paymentVa.setInstructionDate(instructionDate);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				PaymentVAModel paymentVa = new PaymentVAModel();
				setMapToModel(paymentVa, map, true, vo);

				if(ApplicationConstants.SI_IMMEDIATE.equals(paymentVa.getInstructionMode())) {
					savePaymentVA(paymentVa, vo.getCreatedBy(), ApplicationConstants.YES);

					doTransfer(paymentVa, (String)map.get(ApplicationConstants.APP_CODE));
                    vo.setErrorCode(paymentVa.getErrorCode());
					vo.setIsError(paymentVa.getIsError());
				} else {
					savePaymentVA(paymentVa, vo.getCreatedBy(), ApplicationConstants.NO);
				}
			}
		} catch (BusinessException e) {
			throw e;
		} catch (ApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}

	@Override
	public CorporateUserPendingTaskVO reject(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

	private void savePaymentVA(PaymentVAModel paymentVa, String createdBy, String isProcessed)
			throws ApplicationException, BusinessException {
		// set default value
		paymentVa.setIsProcessed(isProcessed);
		paymentVa.setCreatedDate(DateUtils.getCurrentTimestamp());
		paymentVa.setCreatedBy(createdBy);
		
		//jika pendingTaskId null maka transaksi bukan recurring, maka pendingTaskId = model.Id
		if(paymentVa.getPendingTaskId() == null)
			paymentVa.setPendingTaskId(paymentVa.getId());

		paymentVARepo.persist(paymentVa);
	}

	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			String vaAccountNo = (String)map.get("vaAccountNo");
			String accountGroupDetailId = (String)map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
			String corpId = (String)map.get(ApplicationConstants.LOGIN_CORP_ID);

			
			CorporateAccountGroupDetailModel gdm = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(corpId, accountGroupDetailId);			
			String accountNo = gdm.getCorporateAccount().getAccount().getAccountNo();			
			
			checkCustomValidation(map);
			
				
				Map<String, Object> inputs = new HashMap<>();
				inputs.put("debitAccountNo", accountNo);
				inputs.put("vaAccountNo", vaAccountNo);
				
				Map<String, Object> outputs = null;
				
				outputs = eaiAdapter.invokeService(EAIConstants.VA_PAYMENT_INQUIRY, inputs);
				
				//totalBillAmount always get from eai because maybe some biller has adminFee. Eai will add with fee
				resultMap.put("transactionAmount", outputs.get("totalBillAmount"));
				resultMap.put("adminFee", outputs.get("adminFee"));
				resultMap.put("customerName", outputs.get("customerName"));
				resultMap.put("institutionName", outputs.get("institutionName"));
				resultMap.put("productName", outputs.get("productName"));
				resultMap.put("referenceCode", outputs.get("referenceCode"));
				resultMap.put("vaAccountType", outputs.get("vaType"));
				

			resultMap.putAll(corporateChargeService.getCorporateCharges((String) map.get(ApplicationConstants.APP_CODE),
					(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE),
					(String) map.get(ApplicationConstants.LOGIN_CORP_ID)));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	
	private void doTransfer(PaymentVAModel model, String appCode) throws Exception {
		boolean isRollback = false;
		boolean limitUpdated = false;
		boolean isTimeOut = false;
		
		try {
			//update transaction limit
			transactionValidationService.updateTransactionLimit(model.getCorporate().getId(), 
					model.getService().getCode(), 
					model.getSourceAccount().getCurrency().getCode(), 
					model.getTransactionCurrency(), 
					model.getCorporateUserGroup().getId(), 
					model.getTotalDebitedEquivalentAmount(),
					model.getApplication().getCode());
			limitUpdated = true;
			
			Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
			Map<String, Object> outputMap = eaiAdapter.invokeService(EAIConstants.VA_PAYMENT, inputs);
			
			model.setStatus("GPT-0100130");
			model.setIsError(ApplicationConstants.NO);	
			
			this.setTrxReferenceNo(outputMap, model);
		} catch (BusinessException e) {
			String errorMsg = e.getMessage();
			if (errorMsg!=null && errorMsg.startsWith("EAI-")) {
				ErrorMappingModel errorMappingModel = errorMappingRepo.findOne(e.getMessage());
				
				//ada pengecekan jika error code terdaftar dalam list errorcode timeout, status diupdate jdi inprogress offline, BSG 26 Feb 2020
				isTimeOut = maintenanceRepo.isTimeOutErrorCodeValid(e.getMessage());
				
				if (errorMappingModel!=null && ApplicationConstants.YES.equals(errorMappingModel.getRollbackFlag()) && !isTimeOut) {
					isRollback = true;
					throw e;
				}
				
				// Either no error mapping or no rollback...
				
				//jika transaksi gagal tetapi tidak di rollback maka kena error execute with failure
				model.setIsError(ApplicationConstants.YES);
				model.setErrorCode(errorMappingModel == null ? errorMsg : errorMappingModel.getCode());
				if(!isTimeOut) {
					model.setStatus("GPT-0100129");					
				}else {
					model.setStatus("GPT-0100185"); // in Progress Offline
				}
								
			} else {
				isRollback = true;
				throw e;
			}
		} catch (Exception e) {
			isRollback = true;
			throw new ApplicationException(e);
		} finally {
			//save transaction log
			if(!isTimeOut) {
			globalTransactionService.save(model.getCorporate().getId(), PaymentVASC.menuCode, 
					model.getService().getCode(), model.getReferenceNo(), 
					model.getId(), model.getTransactionCurrency(), model.getTotalChargeEquivalentAmount(), 
					model.getTransactionAmount(), 
					model.getTotalDebitedEquivalentAmount(), model.getIsError());
			}else {
				notifyBankEmail(model, appCode);
			}
			//----------------------------------
			
			if(isRollback) {
				if(limitUpdated) {
					try {
						transactionValidationService.reverseUpdateTransactionLimit(model.getCorporate().getId(), 
								model.getService().getCode(), 
								model.getSourceAccount().getCurrency().getCode(), 
								model.getTransactionCurrency(), 
								model.getCorporateUserGroup().getId(), 
								model.getTotalDebitedEquivalentAmount(),
								model.getApplication().getCode());
					} catch(Exception e) {
						logger.error("Failed to reverse the usage "+e.getMessage(),e);
					}
				}
			} else {
				try {
					// send email to corporate email
//					notifyCorporate(model, appCode);
					
					// send email to transaction user history
					notifyTrxUsersHistory(model, appCode);					
					
					// send email to beneficiary only if not execute with failure
					if (!ApplicationConstants.YES.equals(model.getIsError()) && ApplicationConstants.YES.equals(model.getIsNotifyBen())) {
						notifyBeneficiary(model, appCode);
					}
				} catch(Exception e) {
					// ignore any error, just in case something bad happens
				}
			}
		}
	}
	
	private void setTrxReferenceNo(Map<String, Object> outputMap, PaymentVAModel model) {
		try {
			if (outputMap != null) {
				String retrievalRefNo = "";
				
				if (outputMap.get("retrievalRefNo") != null) {
					retrievalRefNo = (String) outputMap.get("retrievalRefNo");
					model.setTrxReferenceNo(retrievalRefNo);
				}

				pendingTaskRepo.updateHostRefNoAndRetrievalRefNoPendingTask(model.getPendingTaskId(), retrievalRefNo, retrievalRefNo);
				
			}
		} catch (Exception e) {
			logger.error("ERROR setHostRefNo Domestic Transfer due to"+ e.getMessage());
		}
	}
	
	private Map<String, Object> prepareInputsForEAI(PaymentVAModel model, String appCode) throws Exception {
		Map<String, Object> inputs = new HashMap<>();
		
		inputs.put("channel", appCode);
		inputs.put("channelRefNo", model.getId());

		AccountModel sourceAccountModel = model.getSourceAccount();
				
		inputs.put("debitAccountNo", sourceAccountModel.getAccountNo());
		inputs.put("debitAccountName", sourceAccountModel.getAccountName());
		inputs.put("debitAccountType", sourceAccountModel.getAccountType().getCode());
		inputs.put("debitAccountCurrencyCode", sourceAccountModel.getCurrency().getCode());
			
				
		inputs.put("trxCurrencyCode", model.getTransactionCurrency());
		inputs.put("trxAmount", model.getTransactionAmount());
		
		inputs.put("chargeType1", model.getChargeType1());
		inputs.put("chargeTypeCurrencyCode1", model.getChargeTypeCurrency1());
		inputs.put("chargeTypeAmount1", model.getChargeTypeAmount1());

		inputs.put("chargeType2", model.getChargeType2());
		inputs.put("chargeTypeCurrencyCode2", model.getChargeTypeCurrency2());
		inputs.put("chargeTypeAmount2", model.getChargeTypeAmount2());

		inputs.put("chargeType3", model.getChargeType3());
		inputs.put("chargeTypeCurrencyCode3", model.getChargeTypeCurrency3());
		inputs.put("chargeTypeAmount3", model.getChargeTypeAmount3());

		inputs.put("chargeType4", model.getChargeType4());
		inputs.put("chargeTypeCurrencyCode4", model.getChargeTypeCurrency4());
		inputs.put("chargeTypeAmount4", model.getChargeTypeAmount4());
		
		inputs.put("chargeType5", model.getChargeType5());
		inputs.put("chargeTypeCurrencyCode5", model.getChargeTypeCurrency5());
		inputs.put("chargeTypeAmount5", model.getChargeTypeAmount5());
				
		inputs.put("serviceCode", model.getService().getCode());

		inputs.put("refNo", model.getReferenceNo());
		inputs.put("senderRefNo", model.getSenderRefNo());
		inputs.put("benRefNo", model.getBenRefNo());
		inputs.put("remark", model.getRemark());
		
		inputs.put("vaAccountNo", model.getVaAccountNo());
		inputs.put("vaProductName", model.getProductName());
		
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("inputs : " + inputs);
			logger.debug("\n\n\n\n\n");
		}
		
		return inputs;
		
	}
	
	private void notifyTrxUsersHistory(PaymentVAModel model, String appCode) {
		try {
			List<String> emails = pendingTaskService.getCorporateUserEmailWithNotifyTrx(model.getId());
			if (emails.size() > 0) {
				IDMMenuModel menu = idmRepo.isIDMMenuValid(PaymentVASC.menuCode);
				
				Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
				inputs.put("instructionMode", model.getInstructionMode());
				inputs.put("instructionDate", model.getInstructionDate());
				inputs.put("benAccountNo", model.getVaAccountNo());
				inputs.put("status", model.getStatus());
				inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
				inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
				inputs.put("emails", emails);
				inputs.put("subject", menu.getName().concat(" Notification"));
				
				eaiAdapter.invokeService(EAIConstants.USER_TRANSFER_NOTIFICATION, inputs);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}	
	
	private void notifyBeneficiary(PaymentVAModel model, String appCode) {		
		if (ValueUtils.hasValue(model.getNotifyBenValue())) {
			String[] vals = getMultipleEntries(model.getNotifyBenValue());
			if (vals!=null && vals.length>0) {
				try {
					Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
					inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
					inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
					inputs.put("emails", Arrays.asList(vals));
					inputs.put("subject", "Beneficiary Transfer Notification");

					eaiAdapter.invokeService(EAIConstants.BENEFICIARY_BP_NOTIFICATION, inputs);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}				
			}
		}		
	}
	
	private String[] getMultipleEntries(String entry) {
		StringTokenizer tokenizer = new StringTokenizer(entry,",;");
		int size = tokenizer.countTokens();
		if(size>0) {
			String[] entries = new String[size];
			for(int i=0;tokenizer.hasMoreTokens();i++) {
				entries[i] = (String)tokenizer.nextToken();
			}
			return entries;
		}
		return null;
	}	
	
	private void notifyBankEmail(PaymentVAModel model, String appCode) {		
		
		try {

			String emails = maintenanceRepo.isSysParamValid(SysParamConstants.BANK_EMAIL).getValue();
			String[] vals = getMultipleEntries(emails);
			if (vals!=null && vals.length>0) {
				Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
				inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
				inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
				inputs.put("emails",Arrays.asList(vals));
				inputs.put("subject", "Suspect Transaction Notification");
				inputs.put("benAccountNo", model.getVaAccountNo());

				eaiAdapter.invokeService(EAIConstants.SUSPECT_TRANCATION_NOTIFICATION, inputs);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}				
}
	
	@Override
	public void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException {
		try{
			Timestamp futureDate = DateUtils.getCurrentTimestamp();
			String[] parameterArr = parameter.split("\\|");
			
			int sessionTimeValue = Integer.valueOf(parameterArr[0]);
			
			if(parameterArr.length > 1) {
				String[] futureDateArr = parameterArr[1].split("\\=");
				
				if(ValueUtils.hasValue(futureDateArr[1])){
					futureDate = new Timestamp(Helper.DATE_TIME_FORMATTER.parse(futureDateArr[1]).getTime());
				}
			}
			
			Calendar calFrom = DateUtils.getEarliestDate(futureDate);
			
			List<String> sessionTimeList = sysParamService.getTransactionSessionTime();
			
			if(sessionTimeValue > 0 && sessionTimeValue <= sessionTimeList.size()) {
				
				Calendar calTo = DateUtils.getNextSessionTime(futureDate, sessionTimeValue, sessionTimeList);
				
				if(logger.isDebugEnabled()) {
					logger.debug("Future start : " + new Timestamp(calFrom.getTimeInMillis()));
					logger.debug("Future end : " + new Timestamp(calTo.getTimeInMillis()));
				}
				
				List<PaymentVAModel> paymentVaList = paymentVARepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_FUTURE_DATE, new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(paymentVaList);
				
			}
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void executeAllBatch(List<PaymentVAModel> list) throws Exception {
		List<SpringBeanInvokerData<String>> jobs = new ArrayList<>();
		for(PaymentVAModel model : list) {
			jobs.add(new SpringBeanInvokerData<>(model.getId(), "PaymentVAService", "executeBatchTransaction", new Object[] {model}, jobTimeout));
		}

		SpringBeanInvokerHelper.invokeAndForget(invoker, jobs, invokerTimeout, concurrencies);						
	}
		
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void executeBatchTransaction(PaymentVAModel paymentVA) {
		if(logger.isDebugEnabled())
			logger.debug("Process recurring Va payment : " + paymentVA.getId() + " - " + paymentVA.getReferenceNo());

		boolean success = false;
		boolean isTimeOut = false;
		
		String errorCode = null;
		Timestamp activityDate = DateUtils.getCurrentTimestamp();
		try {			
			paymentVA.setIsProcessed(ApplicationConstants.YES);
			paymentVARepo.save(paymentVA);
			
			//avoid lazy when get details
			paymentVA = paymentVARepo.findOne(paymentVA.getId());
		
			doTransfer(paymentVA, ApplicationConstants.APP_GPCASHBO);
			success = true;
		} catch (BusinessException e) {
			errorCode = e.getErrorCode();
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			/*
			 	Only generate new recurring if transaction executed successfully
				create new instructionDate if instructionMode = Recurring 
			*/
			
			boolean isExpired = false;
			try {
				if(ApplicationConstants.SI_RECURRING.equals(paymentVA.getInstructionMode())) {					
					Timestamp newInstructionDate = InstructionModeUtils.getStandingInstructionDate(paymentVA.getRecurringParamType(), paymentVA.getRecurringParam(), paymentVA.getInstructionDate());
					
					Calendar calNewInstructionDate = DateUtils.getEarliestDate(newInstructionDate);
					Calendar calExpired = DateUtils.getEarliestDate(paymentVA.getRecurringEndDate());
					
					if(logger.isDebugEnabled()) {
						logger.debug("Recurring new instruction date : " + calNewInstructionDate.getTime());
						logger.debug("Recurring expired date : " + calExpired.getTime());
					}
					
					if(calNewInstructionDate.compareTo(calExpired) <= 0) {
						Calendar oldInstructionDate = Calendar.getInstance();
						oldInstructionDate.setTime(paymentVA.getInstructionDate());
						
						//get session time from old recurring
						calNewInstructionDate.set(Calendar.HOUR_OF_DAY, oldInstructionDate.get(Calendar.HOUR_OF_DAY));
						calNewInstructionDate.set(Calendar.MINUTE, oldInstructionDate.get(Calendar.MINUTE));
						saveNewTransactionForRecurring(paymentVA, new Timestamp(calNewInstructionDate.getTimeInMillis()));
					} else {
						isExpired = true;
					}
					
				}
			} catch(Exception e) {
				logger.error("Failed va payment id : " + paymentVA.getId());
			}
			
			if (success) {
				if (ApplicationConstants.YES.equals(paymentVA.getIsError())) {
					pendingTaskRepo.updatePendingTask(paymentVA.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
				
					trxStatusService.addTransactionStatus(paymentVA.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, paymentVA.getId(), true, paymentVA.getErrorCode());
				} else {
					pendingTaskRepo.updatePendingTask(paymentVA.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS);
					
					trxStatusService.addTransactionStatus(paymentVA.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, paymentVA.getId(), false, null);
				}
			} else {
				
				//ada pengecekan jika error code terdaftar dalam list errorcode timeout, status diupdate jdi inprogress offline, BSG 26 Feb 2020
				try {
					isTimeOut = maintenanceRepo.isTimeOutErrorCodeValid(errorCode);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
					if(!isTimeOut) {
				pendingTaskRepo.updatePendingTask(paymentVA.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
						trxStatusService.addTransactionStatus(paymentVA.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, paymentVA.getId(), true, errorCode);
					}else{
						pendingTaskRepo.updatePendingTask(paymentVA.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE);
						trxStatusService.addTransactionStatus(paymentVA.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE, paymentVA.getId(), true, errorCode);					
					}
				
			}
			
			if (isExpired) {
				pendingTaskRepo.updatePendingTask(paymentVA.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXPIRED);
				
				//insert into trx status EXPIRED
				trxStatusService.addTransactionStatus(paymentVA.getId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXPIRED, null, true, null);
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void saveNewTransactionForRecurring(PaymentVAModel model, Timestamp newInstructionDate) {
		try {
			PaymentVAModel newModel = new PaymentVAModel();
			newModel.setId(Helper.generateHibernateUUIDGenerator());
			newModel.setReferenceNo(model.getReferenceNo());
			newModel.setMenu(model.getMenu());
			newModel.setCorporate(model.getCorporate());
			newModel.setCorporateUserGroup(model.getCorporateUserGroup());
			newModel.setApplication(model.getApplication());
			newModel.setSourceAccount(model.getSourceAccount());
			newModel.setTransactionAmount(model.getTransactionAmount());
			newModel.setTransactionCurrency(model.getTransactionCurrency());
			
			//set pendingTaskId maker for recurring
			String makerPendingTaskIdForRecurring = model.getPendingTaskId();
			if(!ValueUtils.hasValue(makerPendingTaskIdForRecurring)) {
				makerPendingTaskIdForRecurring = model.getId();
			}
			newModel.setPendingTaskId(makerPendingTaskIdForRecurring);
			//--------------------------------------------
			
			//TODO recalculate all if implement forex trx
			
			//calculate new charge for recurring
			Map<String, Object> chargesInfo =  corporateChargeService.getCorporateCharges(model.getApplication().getCode(),
					model.getService().getCode(), model.getCorporate().getId());
			
			BigDecimal totalChargeFromTable = new BigDecimal((String) chargesInfo.get(ApplicationConstants.TRANS_TOTAL_CHARGE));
			BigDecimal totalTransaction = model.getTransactionAmount().add(totalChargeFromTable);
			
			newModel.setTotalChargeEquivalentAmount(totalChargeFromTable);
			newModel.setTotalDebitedEquivalentAmount(totalTransaction);
			
			setCharge(newModel, (List<Map<String, Object>>)chargesInfo.get(ApplicationConstants.TRANS_CHARGE_LIST));
			
			newModel.setService(model.getService());
			newModel.setSenderRefNo(model.getSenderRefNo());
			newModel.setIsFinalPayment(model.getIsFinalPayment());
			newModel.setBenRefNo(model.getBenRefNo());
			newModel.setIsNotifyBen(model.getIsNotifyBen());
			newModel.setNotifyBenValue(model.getNotifyBenValue());
			
			newModel.setProductName(model.getProductName());
			newModel.setVaAccountNo(model.getVaAccountNo());
			newModel.setVaAccountType(model.getVaAccountType());
			
			//set new instructionDate for recurring
			newModel.setInstructionMode(model.getInstructionMode());
			newModel.setRecurringParamType(model.getRecurringParamType());
			newModel.setRecurringParam(model.getRecurringParam());
			newModel.setRecurringStartDate(model.getRecurringStartDate());
			newModel.setRecurringEndDate(model.getRecurringEndDate());
			newModel.setInstructionDate(newInstructionDate);
			
			savePaymentVA(newModel, ApplicationConstants.CREATED_BY_SYSTEM, ApplicationConstants.NO);

	        //insert into trx status PENDING_EXECUTE with new model id
			Timestamp now = DateUtils.getCurrentTimestamp();
			pendingTaskRepo.updatePendingTask(makerPendingTaskIdForRecurring, ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE);
			
			trxStatusService.addTransactionStatus(makerPendingTaskIdForRecurring, now, TransactionActivityType.EXECUTE, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE, null, false, null);
		} catch(Exception e) {
			// ignore any error to avoid transaction rollback caused by saving new transaction
			logger.error("Failed save VA Payment recurring with id : " + model.getId());
		}
	}
	
	@Override
	public Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException {

		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			String transactionStatusId = (String) map.get("executedId");
			TransactionStatusModel trxStatus = trxStatusRepo.findOne(transactionStatusId);
			
			//jika success ambil ke model nya, jika tidak maka ambil dr pending task krn blm masuk table
			if(trxStatus.getStatus().equals(TransactionStatus.EXECUTE_SUCCESS)) {
				PaymentVAModel model = paymentVARepo.findOne(trxStatus.getEaiRefNo());
				resultMap.put("executedResult", prepareDetailTransactionMap(model, trxStatus));
			} else {
				CorporateUserPendingTaskModel model = pendingTaskRepo.findOne(trxStatus.getEaiRefNo());
				resultMap.put("executedResult", globalTransactionService.prepareDetailTransactionMapFromPendingTask(model, trxStatus));
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private List<Map<String, Object>> prepareDetailTransactionMap(PaymentVAModel model, TransactionStatusModel trxStatus) throws Exception {
		List<Map<String, Object>> executedTransactionList = new LinkedList<>();
		
		AccountModel sourceAccount = model.getSourceAccount();
		
		Map<String, Object> modelMap = new HashMap<>();
		modelMap.put("executedDate", trxStatus.getActivityDate());
		modelMap.put("systemReferenceNo", model.getId());
		modelMap.put("debitAccount", sourceAccount.getAccountNo());
		modelMap.put("debitAccountName", sourceAccount.getAccountName());
		modelMap.put("debitAccountCurrency", sourceAccount.getCurrency().getCode());
		modelMap.put("debitAccountCurrencyName", sourceAccount.getCurrency().getName());
		modelMap.put("transactionCurrency", model.getTransactionCurrency());
		modelMap.put("transactionAmount", model.getTransactionAmount());
		modelMap.put("senderRefNo", ValueUtils.getValue(model.getSenderRefNo()));
		modelMap.put("benRefNo", ValueUtils.getValue(model.getBenRefNo()));
		modelMap.put("vaAccountNo", ValueUtils.getValue(model.getVaAccountNo()));
		modelMap.put("vaAccountType", ValueUtils.getValue(model.getVaAccountType()));
		modelMap.put("productName", ValueUtils.getValue(model.getProductName()));
		
		//TODO diganti jika telah implement rate
		modelMap.put("debitTransactionCurrency", model.getTransactionCurrency());
		modelMap.put("debitEquivalentAmount", model.getTransactionAmount());
		modelMap.put("debitExchangeRate", ApplicationConstants.ZERO);
		modelMap.put("creditTransactionCurrency", model.getTransactionCurrency());
		modelMap.put("creditEquivalentAmount", model.getTransactionAmount());
		modelMap.put("creditExchangeRate", ApplicationConstants.ZERO);
		//--------------------------------------------
		
		//TODO diganti jika telah implement periodical charges
		modelMap.put("chargeAccount", ApplicationConstants.EMPTY_STRING);
		
		LinkedList<Map<String, Object>> chargeList = new LinkedList<>();
		
		Map<String, Object> chargeMap = null;
		
		if(model.getChargeType1() != null) {
			chargeMap = new HashMap<>();
			CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType1());
			ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
			chargeMap.put("chargeType", serviceCharge.getName());
			chargeMap.put("chargeCurrency", model.getChargeTypeCurrency1());
			chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount1());
			chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
			chargeList.add(chargeMap);
		}
		
		if(model.getChargeType2() != null) {
			chargeMap = new HashMap<>();
			CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType2());
			ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
			chargeMap.put("chargeType", serviceCharge.getName());
			chargeMap.put("chargeCurrency", model.getChargeTypeCurrency2());
			chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount2());
			chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
			chargeList.add(chargeMap);
		}
		
		if(model.getChargeType3() != null) {
			chargeMap = new HashMap<>();
			CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType3());
			ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
			chargeMap.put("chargeType", serviceCharge.getName());
			chargeMap.put("chargeCurrency", model.getChargeTypeCurrency3());
			chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount3());
			chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
			chargeList.add(chargeMap);
		}
		
		if(model.getChargeType4() != null) {
			chargeMap = new HashMap<>();
			CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType4());
			ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
			chargeMap.put("chargeType", serviceCharge.getName());
			chargeMap.put("chargeCurrency", model.getChargeTypeCurrency4());
			chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount4());
			chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
			chargeList.add(chargeMap);
		}
		
		if(model.getChargeType5() != null) {
			chargeMap = new HashMap<>();
			CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType5());
			ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
			chargeMap.put("chargeType", serviceCharge.getName());
			chargeMap.put("chargeCurrency", model.getChargeTypeCurrency5());
			chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount5());
			chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
			chargeList.add(chargeMap);
		}
		
		modelMap.put("chargeList", chargeList);
		
		//--------------------------------------------
		
		modelMap.put("status", trxStatus.getStatus());
		modelMap.put("errorCode", ValueUtils.getValue(trxStatus.getErrorCode(), ApplicationConstants.EMPTY_STRING));
		
		//ini buat di translate
		Locale locale = LocaleContextHolder.getLocale();
		modelMap.put("errorDscp", ValueUtils.getValue(message.getMessage(trxStatus.getErrorCode(), null, trxStatus.getErrorCode(), locale), ApplicationConstants.EMPTY_STRING));
		
		executedTransactionList.add(modelMap);
		
		return executedTransactionList;
	}
	
	@Override
	public Map<String, Object> cancelTransactionWF(Map<String, Object> map)
			throws ApplicationException, BusinessException {

		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			//cancelId = pendingTaskId
			String pendingTaskId = (String) map.get("cancelId");
			String loginUserCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			
			if(logger.isDebugEnabled()) {
				logger.debug("cancel pendingTaskId : " + pendingTaskId);
			}
			
			//expired to wf engine
			try {
				wfEngine.cancelInstance(pendingTaskId);
			} catch (Exception e) {
				logger.debug(e.getMessage() + " " + pendingTaskId);
			}
			
			
			//update expired to pending task
			pendingTaskRepo.updatePendingTask(pendingTaskId, Status.CANCELED.name(), DateUtils.getCurrentTimestamp(), loginUserCode, TransactionStatus.CANCELLED);

			//insert into trx status EXPIRE
			trxStatusService.addTransactionStatus(pendingTaskId, DateUtils.getCurrentTimestamp(), TransactionActivityType.CANCEL, loginUserCode, TransactionStatus.CANCELLED, null, true, null);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	public Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Locale locale = LocaleContextHolder.getLocale();
			String transactionStatusId = (String) map.get("receiptId");
			TransactionStatusModel trxStatus = trxStatusRepo.findOne(transactionStatusId);
			
			SimpleDateFormat sdfDateTimeTrxDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
			
			HashMap<String, Object> reportParams = new HashMap<>();
			reportParams.put("trxDate", sdfDateTimeTrxDate.format(trxStatus.getActivityDate()));
			reportParams.put("trxTime", sdfTime.format(trxStatus.getActivityDate()));
			reportParams.put("transactionStatus", message.getMessage(trxStatus.getStatus().toString(), null, trxStatus.getStatus().toString(), locale));
			reportParams.put("errorDscp", trxStatus.getErrorCode());
			reportParams.put("printDate", sdfDateTime.format(new Date()));
			
			Path bankLogoPath = Paths.get(bankLogo);
			BufferedImage bankLogo = ImageIO.read(bankLogoPath.toFile());
			reportParams.put("bankLogo", bankLogo);
			
			PaymentVAModel model = paymentVARepo.findOne(trxStatus.getEaiRefNo());
			CorporateUserPendingTaskModel pending = (CorporateUserPendingTaskModel) pendingTaskRepo.findOne(trxStatus.getPendingTaskId());
			setParamForDownloadTrxStatus(reportParams, model, pending);
			
			String masterReportFile = reportFolder + File.separator + "TransactionStatus" + 
					File.separator + "VirtualAccount" + File.separator + "download-transaction-vapayment" + "-"
					+ locale.getLanguage() + ".jasper";
			
			JasperReport masterReport = (JasperReport) JRLoader.loadObject(new File(masterReportFile));
			
			setCopyrightSubReport(reportParams);
			
			if(ApplicationConstants.YES.equals(pending.getErrorTimeoutFlag())) {
				setSuspectTrxSubReport(reportParams);
			}
			
			JasperPrint print = JasperFillManager.fillReport(masterReport, reportParams, new JREmptyDataSource());
			byte[] bytes = JasperExportManager.exportReportToPdf(print);
		
			//download reference no agar unique tiap request download
			String downloadRefNo = Helper.generateReportReferenceNo();
			String destinationFile = pathDownload + File.separator + downloadRefNo.concat(".").concat("pdf");
			Path destinationFilePath = Paths.get(destinationFile);
			
			//write files
			Files.write(destinationFilePath, bytes);
			
			Map<String, Object> result = new HashMap<>();
			result.put(ApplicationConstants.FILENAME, destinationFile);
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void setSuspectTrxSubReport(Map<String, Object> reportParams) throws JRException {
		Locale locale = LocaleContextHolder.getLocale();
		String suspectTrxFile = reportFolder + File.separator + "SuspectTrx" + File.separator + "suspect-trx" + "-" + locale.getLanguage() + ".jasper";
		JasperReport suspectTrxReport = (JasperReport) JRLoader.loadObject(new File(suspectTrxFile));
		reportParams.put("SUSPECT_TRX_REPORT", suspectTrxReport);
	}
	
	
	private void setCopyrightSubReport(Map<String, Object> reportParams) throws JRException {
		Locale locale = LocaleContextHolder.getLocale();
		String copyrightFile = reportFolder + File.separator + "Copyright" + File.separator + "bank-copyright" + "-" + locale.getLanguage() + ".jasper";
		JasperReport copyrightReport = (JasperReport) JRLoader.loadObject(new File(copyrightFile));
		reportParams.put("COPYRIGHT_REPORT", copyrightReport);
	}

	private void setParamForDownloadTrxStatus(Map<String, Object> reportParams, PaymentVAModel model, CorporateUserPendingTaskModel pending) throws Exception {
		DecimalFormat df = new DecimalFormat(moneyFormat);
		
		
		if(model != null) {
			
			AccountModel sourceAccount = model.getSourceAccount();
			reportParams.put("sourceAccountNo", sourceAccount.getAccountNo());
			reportParams.put("sourceAccountName", sourceAccount.getAccountName());
			reportParams.put("trxReferenceNo", model.getTrxReferenceNo());
			reportParams.put("senderRefNo", model.getSenderRefNo());
			reportParams.put("benRefNo", model.getBenRefNo());
			reportParams.put("remark", model.getRemark());
			reportParams.put("refNo", model.getReferenceNo());
			reportParams.put("transactionCurrency", model.getTransactionCurrency());
			reportParams.put("corporateName", model.getInstitutionName());
			reportParams.put("vaAccountNo", model.getVaAccountNo());
			reportParams.put("vaAccountName", model.getVaAccountName());
			reportParams.put("vaAccountType", model.getVaAccountType());
			reportParams.put("productName", model.getProductName());
			reportParams.put("trxAmount", df.format(model.getTransactionAmount()));
			
			if(model.getChargeTypeAmount1() != null && model.getChargeTypeAmount1().compareTo(BigDecimal.ZERO) > 0) {
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType1());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				reportParams.put("chargeCurrency1", model.getChargeTypeCurrency1());
				reportParams.put("chargeType1", serviceCharge.getName());
				reportParams.put("chargeAmount1", df.format(model.getChargeTypeAmount1()));
			}

			if(model.getChargeTypeAmount2() != null && model.getChargeTypeAmount2().compareTo(BigDecimal.ZERO) > 0) {
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType2());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				reportParams.put("chargeCurrency2", model.getChargeTypeCurrency2());
				reportParams.put("chargeType2", serviceCharge.getName());
				reportParams.put("chargeAmount2", df.format(model.getChargeTypeAmount2()));
			}
			
			if(model.getChargeTypeAmount3() != null && model.getChargeTypeAmount3().compareTo(BigDecimal.ZERO) > 0) {
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType3());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				reportParams.put("chargeCurrency3", model.getChargeTypeCurrency3());
				reportParams.put("chargeType3", serviceCharge.getName());
				reportParams.put("chargeAmount3", df.format(model.getChargeTypeAmount3()));
			}
			
			if(model.getChargeTypeAmount4() != null && model.getChargeTypeAmount4().compareTo(BigDecimal.ZERO) > 0) {
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType4());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				reportParams.put("chargeCurrency4", model.getChargeTypeCurrency4());
				reportParams.put("chargeType4", serviceCharge.getName());
				reportParams.put("chargeAmount4", df.format(model.getChargeTypeAmount4()));
			}
			
			if(model.getChargeTypeAmount5() != null && model.getChargeTypeAmount5().compareTo(BigDecimal.ZERO) > 0) {
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType5());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				reportParams.put("chargeCurrency5", model.getChargeTypeCurrency5());
				reportParams.put("chargeType5", serviceCharge.getName());
				reportParams.put("chargeAmount5", df.format(model.getChargeTypeAmount5()));
			}
			
			if(model.getTotalChargeEquivalentAmount().compareTo(BigDecimal.ZERO) > 0)
				reportParams.put("totalCharge", df.format(model.getTotalChargeEquivalentAmount()));
			
			reportParams.put("totalDebited", df.format(model.getTotalDebitedEquivalentAmount()));
			

			
		}
	}
	
    @Override
	public Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			//cancelId = pendingTaskId
			String pendingTaskId = (String) map.get("cancelId");
			String loginUserCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			
			if(logger.isDebugEnabled()) {
				logger.debug("cancel pendingTaskId : " + pendingTaskId);
			}
			
			try {
				
				PaymentVAModel paymentVa = paymentVARepo.findByPendingTaskIdAndIsProcessed(pendingTaskId, ApplicationConstants.NO);
				
				paymentVa.setIsProcessed(ApplicationConstants.YES);
				paymentVa.setUpdatedDate(DateUtils.getCurrentTimestamp());
				paymentVa.setUpdatedBy(loginUserCode);
				
				paymentVARepo.save(paymentVa);
			} catch (Exception e) {
				logger.debug(e.getMessage() + " " + pendingTaskId);
			}
			
			
			//update expired to pending task
			pendingTaskRepo.updatePendingTask(pendingTaskId, Status.CANCELED.name(), DateUtils.getCurrentTimestamp(), loginUserCode, TransactionStatus.CANCELLED);

			//insert into trx status EXPIRE
			trxStatusService.addTransactionStatus(pendingTaskId, DateUtils.getCurrentTimestamp(), TransactionActivityType.CANCEL, loginUserCode, TransactionStatus.CANCELLED, null, true, null);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	
	@Override
	public Map<String, Object> updateTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			//cancelId = pendingTaskId
			String pendingTaskId = (String) map.get("pendingTaskId");
			String status = (String) map.get("status");			
			if(logger.isDebugEnabled()) {
				logger.debug("pendingTaskId : " + pendingTaskId);
			}
			
			Timestamp activityDate = DateUtils.getCurrentTimestamp();
			String isError = "Y";
			
			PaymentVAModel billpayment = paymentVARepo.findByPendingTaskIdAndIsProcessed(pendingTaskId, ApplicationConstants.YES);
			
			if(status.equals("SUCCESS")) {
				isError = "N";
				pendingTaskRepo.updatePendingTask(billpayment.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS);
				trxStatusService.addTransactionStatus(billpayment.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, billpayment.getId(), false, null);
				
				// send email to beneficiary only if not execute with failure
				if (ApplicationConstants.YES.equals(billpayment.getIsNotifyBen())) {
					notifyBeneficiary(billpayment, ApplicationConstants.APP_GPCASHBO);
				}
				
			}else {
				pendingTaskRepo.updatePendingTask(billpayment.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
				trxStatusService.addTransactionStatus(billpayment.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, billpayment.getId(), true, billpayment.getErrorCode());
			}
			
			pendingTaskRepo.updateErrorTimeoutFlagPendingTask(pendingTaskId, ApplicationConstants.NO);
			
			globalTransactionService.save(billpayment.getCorporate().getId(), PaymentVASC.menuCode, 
					billpayment.getService().getCode(), billpayment.getReferenceNo(), 
					billpayment.getId(), billpayment.getTransactionCurrency(), billpayment.getTotalChargeEquivalentAmount(), 
					billpayment.getTransactionAmount(), 
					billpayment.getTotalDebitedEquivalentAmount(), isError);
			
			notifyTrxUsersHistory(billpayment, ApplicationConstants.APP_GPCASHBO);
			
			
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
}
