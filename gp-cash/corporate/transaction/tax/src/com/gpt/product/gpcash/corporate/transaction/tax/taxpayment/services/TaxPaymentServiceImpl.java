package com.gpt.product.gpcash.corporate.transaction.tax.taxpayment.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
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
import com.gpt.component.maintenance.parametermt.model.IdentityTypeModel;
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
import com.gpt.product.gpcash.corporate.transaction.tax.deposittype.model.DepositTypeModel;
import com.gpt.product.gpcash.corporate.transaction.tax.deposittype.repository.DepositTypeRepository;
import com.gpt.product.gpcash.corporate.transaction.tax.taxpayment.constants.TaxPaymentConstants;
import com.gpt.product.gpcash.corporate.transaction.tax.taxpayment.model.TaxPaymentModel;
import com.gpt.product.gpcash.corporate.transaction.tax.taxpayment.repository.TaxPaymentRepository;
import com.gpt.product.gpcash.corporate.transaction.tax.taxtype.model.TaxTypeModel;
import com.gpt.product.gpcash.corporate.transaction.tax.taxtype.repository.TaxTypeRepository;
import com.gpt.product.gpcash.corporate.transaction.utils.InstructionModeUtils;
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
public class TaxPaymentServiceImpl implements TaxPaymentService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.batch.transations.timeout-invoker}")
	private int invokerTimeout;
	
	@Value("${gpcash.batch.transations.timeout-job}")
	private int jobTimeout;

	@Value("${gpcash.batch.transations.concurrencies}")
	private int concurrencies;
	
	@Autowired
	private TaxPaymentRepository taxPaymentRepo;

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
	private MaintenanceRepository maintenanceRepo;	
	
	@Autowired
	private CorporateChargeRepository corporateChargeRepo;
	
	@Autowired
	private EAIEngine eaiAdapter;
	
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
	
	@Autowired
	private TaxTypeRepository taxTypeRepo;
	
	@Autowired
	private DepositTypeRepository depositTypeRepo;
	
	@Value("${gpcash.report.folder}")
	private String reportFolder;
	
	@Value("${gpcash.report.bank.logo}")
	private String bankLogo;
	
	@Value("${gpcash.report.money.format:#,###.00}")
	private String moneyFormat;
	
	@Value("${gpcash.trxstatus.download.path}")
	private String pathDownload;
	
	@Autowired
	private MessageSource message;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private ObjectMapper objectMapper;
	
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

			CorporateUserPendingTaskVO vo = setCorporateUserPendingTaskVO(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
			} else {
				throw new BusinessException("GPT-0100003");
			}
			
			//============ create billing id ================
			Map<String, Object> inputs = prepareInputsForEAI(map);			
			Map<String, Object> inputsBill = eaiAdapter.invokeService(TaxPaymentConstants.EAI_CREATE_BILLING_ID, inputs);
			//============ create billing id ================
			
			vo.setBillingId((String) inputsBill.get("billingId"));
			vo.setBillExpiryDate(new Timestamp(((Date)inputsBill.get("expiryDate")).getTime()));
			
			map.put("billingId", (String) inputsBill.get("billingId"));
			map.put("expiryDate", inputsBill.get("expiryDate"));
			vo.setJsonObject(map);

			resultMap.put("billingId", map.get("billingId"));
			resultMap.put("expiryDate", map.get("expiryDate"));
			resultMap.putAll(pendingTaskService.savePendingTask(vo));
			resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private Map<String, Object> prepareInputsForEAI(Map<String, Object> map) {
		Map<String, Object> inputs = new HashMap<>();
		
		inputs.put("channel", (String)map.get(ApplicationConstants.APP_CODE));
		
		inputs.put("isNpwp", (String) map.get("isNpwp"));
		inputs.put("npwpNo", (String) map.get("npwpNo"));
		inputs.put("kppNo", (String) map.get("kppNo"));
		inputs.put("identityNo", (String) map.get("identityNo"));
		inputs.put("benName", (String) map.get("benName"));
		inputs.put("benAddress1", (String) map.get("benAddress1"));
		inputs.put("benAddress2", (String) map.get("benAddress2"));
		inputs.put("benAddress3", (String) map.get("benAddress3"));
		inputs.put("cityName", (String) map.get("cityName"));
		inputs.put("taxType", (String) map.get("taxType"));
		inputs.put("depositType", (String) map.get("depositType"));
		inputs.put("nopNo", (String) map.get("nopNo"));
		inputs.put("skNo", (String) map.get("skNo"));
		inputs.put("idPeserta", (String) map.get("depositorNpwpNo"));
		inputs.put("periodMonthFrom", (String) map.get("periodMonthFrom"));
		inputs.put("periodMonthTo", (String) map.get("periodMonthTo"));
		inputs.put("periodYear", (String) map.get("periodYear"));
		inputs.put("trxCurrencyCode", (String) map.get(ApplicationConstants.TRANS_CURRENCY));
		inputs.put("trxAmount", new BigDecimal(map.get(ApplicationConstants.TRANS_AMOUNT).toString()));
		inputs.put("dscp", (String)map.get("description"));
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("inputs : " + inputs);
			logger.debug("\n\n\n\n\n");
		}
		
		return inputs;
	}

	private void validateFinalPayment(String corporateId, String senderRefNo) throws BusinessException {
		pendingTaskService.validateFinalPayment(senderRefNo, TaxPaymentSC.menuCode, corporateId);
		
		List<TaxPaymentModel> modelList = taxPaymentRepo.findTransactionForFinalPaymentFlag(corporateId, 
				ApplicationConstants.YES, senderRefNo);
		
		if(modelList.size() > 0) {
			throw new BusinessException("GPT-0100199");
		}
	}
	
	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String senderRefNo = (String) map.get("senderRefNo");
			
			//validate NON NPWP and NPWP mandatory
			String isNpwp = (String) map.get("isNpwp");
			String npwpNo = (String) map.get("npwpNo");
			String identityType = (String) map.get("identityType");
			String identityNo = (String) map.get("identityNo");
			if(isNpwp.equals(ApplicationConstants.YES)) {
				if(!ValueUtils.hasValue(npwpNo)) 
					throw new BusinessException("GPT-0100216");
			} else {
				if(!ValueUtils.hasValue(identityType))
					throw new BusinessException("GPT-0100217");
				
				if(!ValueUtils.hasValue(identityNo))
					throw new BusinessException("GPT-0100218");
				
				maintenanceRepo.isIdentityTypeValid(identityType);
			}
			
			String nop = (String) map.get("nopNo");
			
			//validate taxType
			isTaxTypeValid((String) map.get("taxType"), nop.equals("") ? true : false);
			
			//validate depositType
			isDepositTypeValid((String) map.get("depositUniqueCode"), nop.equals("") ? true : false);
			
			//validate month
			isTaxPeriodMonthValid((String) map.get("periodMonthFrom"), (String) map.get("periodMonthTo"));
			
			//validate year
			isPeriodYearValid((String) map.get("periodYear"));
			
			//validate final payment
			validateFinalPayment(corporateId, senderRefNo);
			//------------------------------------------------------

			// TODO implement calculate equivalent amount in future if implement
			// cross currency transaction
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	private CorporateUserPendingTaskVO setCorporateUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String isFinalPayment = (String) map.get("isFinalPayment");
		CorporateUserModel maker = corporateUtilsRepo.isCorporateUserValid(createdBy);
		String userGroupId = maker.getCorporateUserGroup().getId();

		String accountGroupDetailId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
		BigDecimal transactionAmount = (BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT);
		String uniqueKey = createdBy.concat(ApplicationConstants.DELIMITER_PIPE).concat(accountGroupDetailId)
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(transactionAmount.toPlainString())
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(String.valueOf((DateUtils.getCurrentTimestamp().getTime())));

		String instructionMode = (String) map.get(ApplicationConstants.INSTRUCTION_MODE);
		String sessionTime = (String) map.get("sessionTime");
		Timestamp instructionDate = (Timestamp) map.get(ApplicationConstants.INSTRUCTION_DATE);
		String transactionServiceCode = (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE);
		
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		vo.setUniqueKey(uniqueKey);
		vo.setCreatedBy(createdBy);
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setTransactionServiceCode(transactionServiceCode);
		vo.setService("TaxPaymentSC");
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
		
		vo.setSenderRefNo((String) map.get("senderRefNo"));
		
		//set remark
		vo.setRemark1((String) map.get("description"));
		vo.setRemark2((String) map.get("remark2"));
		vo.setRemark3((String) map.get("remark3"));
		//------------------------------------------------------------------
		
		vo.setTotalChargeEquivalentAmount((BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_CHARGE));
		vo.setTotalDebitedEquivalentAmount((BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT));
		vo.setInstructionMode(instructionMode);
		
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
		
		checkSourceAccountAndBenAccountCannotSame(vo.getSourceAccount(), vo.getBenAccount());
		
		return vo;
	}
	
	public void checkSourceAccountAndBenAccountCannotSame(String sourceAccount, String benAccount) throws BusinessException, Exception {
		try {
			if(sourceAccount.equals(benAccount)) {
				throw new BusinessException("GPT-0100149");
			}
		} catch (BusinessException e) {
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	private TaxPaymentModel setMapToModel(TaxPaymentModel taxPayment, Map<String, Object> map,
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

		// set ID
		taxPayment.setId(vo.getId());

		// set transaction information
		taxPayment.setReferenceNo(vo.getReferenceNo());
		taxPayment.setSenderRefNo(vo.getSenderRefNo());

		IDMMenuModel menu = new IDMMenuModel();
		menu.setCode(vo.getMenuCode());
		taxPayment.setMenu(menu);
		
		CorporateModel corpModel = corporateUtilsRepo.isCorporateValid(vo.getCorporateId());
		taxPayment.setCorporate(corpModel);
		
		taxPayment.setCorporateUserGroup(corporateUserGroup);
		
		IDMApplicationModel application = new IDMApplicationModel();
		//application code diambil dr yg login
		application.setCode((String) map.get(ApplicationConstants.APP_CODE)); 
		taxPayment.setApplication(application);

		AccountModel sourceAccountModel = productRepo.isAccountValid(vo.getSourceAccount());
		taxPayment.setSourceAccount(sourceAccountModel);
		
		taxPayment.setTransactionAmount(new BigDecimal(map.get(ApplicationConstants.TRANS_AMOUNT).toString()));
		taxPayment.setTransactionCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		taxPayment.setTotalChargeEquivalentAmount(
				new BigDecimal(map.get(ApplicationConstants.TRANS_TOTAL_CHARGE).toString()));
		taxPayment.setTotalDebitedEquivalentAmount(
				new BigDecimal(map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT).toString()));

		// set transaction service code
		ServiceModel service = new ServiceModel();
		service.setCode(transactionServiceCode);
		taxPayment.setService(service);

		// set additional information
		taxPayment.setSenderRefNo((String) map.get("senderRefNo"));
		taxPayment.setIsFinalPayment((String) map.get("isFinalPayment"));
		taxPayment.setRemark1((String) map.get("description"));
		taxPayment.setRemark2((String) map.get("remark2"));
		taxPayment.setRemark3((String) map.get("remark3"));
		taxPayment.setIsNotifyBen((String) map.get("isNotify"));
		taxPayment.setNotifyBenValue((String) map.get("notifyBenValue"));
		
		
		String isNpwp = (String) map.get("isNpwp");
		taxPayment.setIsNpwp(isNpwp);
		if(isNpwp.equals(ApplicationConstants.YES)) {
			taxPayment.setNpwpNo((String) map.get("npwpNo"));
		} else {
			taxPayment.setKppNo((String) map.get("kppNo"));
			
			IdentityTypeModel identityType = new IdentityTypeModel();
			identityType.setCode((String) map.get("identityType"));
			taxPayment.setIdentityType(identityType);
			
			taxPayment.setIdentityNo((String) map.get("identityNo"));
		}
		
		taxPayment.setBenName((String) map.get("benName"));
		taxPayment.setBenAddress1((String) map.get("benAddress1"));
		taxPayment.setBenAddress2((String) map.get("benAddress2"));
		taxPayment.setBenAddress3((String) map.get("benAddress3"));
		taxPayment.setCityName((String) map.get("cityName"));
		
		TaxTypeModel taxType = new TaxTypeModel();
		taxType.setCode((String) map.get("taxType"));
		taxPayment.setTaxType(taxType);
		
		DepositTypeModel depositType = new DepositTypeModel();
		depositType.setCode((String) map.get("depositUniqueCode"));
		depositType.setTaxDepositCode((String) map.get("depositType"));
		taxPayment.setDepositType(depositType);
		
		taxPayment.setNopNo((String) map.get("nopNo"));
		taxPayment.setSkNo((String) map.get("skNo"));
		taxPayment.setDepositorNpwpNo((String) map.get("depositorNpwpNo"));
		taxPayment.setPeriodMonthFrom((String) map.get("periodMonthFrom"));
		taxPayment.setPeriodMonthTo((String) map.get("periodMonthTo"));
		taxPayment.setPeriodYear((String) map.get("periodYear"));
		taxPayment.setBillExpiryDate(vo.getBillExpiryDate());
		taxPayment.setBillingId(vo.getBillingId());
		
		
		setInstructionMode(taxPayment, instructionMode, instructionDate, map);
		
		setCharge(taxPayment, (ArrayList<Map<String,Object>>) map.get("chargeList"));
		

		return taxPayment;
	}
	
	private void setCharge(TaxPaymentModel taxPayment, List<Map<String, Object>> chargeList){
		for(int i=1; i <= chargeList.size(); i++){
			Map<String, Object> chargeMap = chargeList.get(i - 1);
			String chargeId = (String) chargeMap.get("id");
			BigDecimal value = new BigDecimal(chargeMap.get("value").toString());
			String currencyCode = (String) chargeMap.get("currencyCode");
			
			if(i == 1){
				taxPayment.setChargeType1(chargeId);
				taxPayment.setChargeTypeAmount1(value);
				taxPayment.setChargeTypeCurrency1(currencyCode);
			} else if(i == 2){
				taxPayment.setChargeType2(chargeId);
				taxPayment.setChargeTypeAmount2(value);
				taxPayment.setChargeTypeCurrency2(currencyCode);
			} else if(i == 3){
				taxPayment.setChargeType3(chargeId);
				taxPayment.setChargeTypeAmount3(value);
				taxPayment.setChargeTypeCurrency3(currencyCode);
			} else if(i == 4){
				taxPayment.setChargeType4(chargeId);
				taxPayment.setChargeTypeAmount4(value);
				taxPayment.setChargeTypeCurrency4(currencyCode);
			} else if(i == 5){
				taxPayment.setChargeType5(chargeId);
				taxPayment.setChargeTypeAmount5(value);
				taxPayment.setChargeTypeCurrency5(currencyCode);
			} 
		}
	}

	private void setInstructionMode(TaxPaymentModel taxPayment, String instructionMode, Timestamp instructionDate, 
			Map<String, Object> map) throws Exception {
		
		
		// set instruction mode
		taxPayment.setInstructionMode(instructionMode);

		if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
			taxPayment
					.setInstructionDate(transactionValidationService.getInstructionDateForRelease(instructionMode, instructionDate));
		} else if (instructionMode.equals(ApplicationConstants.SI_FUTURE_DATE)) {
			taxPayment.setInstructionDate(instructionDate);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {

		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				TaxPaymentModel taxPayment = new TaxPaymentModel();
				setMapToModel(taxPayment, map, true, vo);

				if(ApplicationConstants.SI_IMMEDIATE.equals(taxPayment.getInstructionMode())) {
					saveTaxPayment(taxPayment, vo.getCreatedBy(), ApplicationConstants.YES);
					
					doTransfer(taxPayment, (String)map.get(ApplicationConstants.APP_CODE));
					vo.setErrorCode(taxPayment.getErrorCode());
					vo.setIsError(taxPayment.getIsError());
				} else {
					saveTaxPayment(taxPayment, vo.getCreatedBy(), ApplicationConstants.NO);
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

	private void saveTaxPayment(TaxPaymentModel taxPayment, String createdBy, String isProcessed)
			throws ApplicationException, BusinessException {
		// set default value
		taxPayment.setIsProcessed(isProcessed);
		taxPayment.setCreatedDate(DateUtils.getCurrentTimestamp());
		taxPayment.setCreatedBy(createdBy);
		
		//jika pendingTaskId null maka transaksi bukan recurring, maka pendingTaskId = model.Id
		if(taxPayment.getPendingTaskId() == null)
			taxPayment.setPendingTaskId(taxPayment.getId());

		taxPaymentRepo.persist(taxPayment);
	}

	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			checkCustomValidation(map);
			
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
	
	private void doTransfer(TaxPaymentModel model, String appCode) throws Exception {
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
			Date billExpiryDate = model.getBillExpiryDate();
			Date today = DateUtils.getCurrentTimestamp();
			
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n\n\n");
				logger.debug("billExpiryDate : " + billExpiryDate);
				logger.debug("todayDate : " + today);
				logger.debug("\n\n\n\n\n");
			}			
			if (billExpiryDate.compareTo(today) < 0) {
				throw new BusinessException("GPT-0200016");
			}
			model.setPaymentDate(DateUtils.getCurrentTimestamp());
			
			Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
			Map<String, Object> result = eaiAdapter.invokeService(TaxPaymentConstants.EAI_TAX_PAYMENT_TRANSFER, inputs);
			
			//set ntb and ntp
			model.setNtb((String) result.get("ntb"));
			model.setNtp((String) result.get("ntp"));
			model.setStan((String) result.get("stan"));
			
			
			model.setStatus("GPT-0100130");
			model.setIsError(ApplicationConstants.NO);
			
			//save othersInfo ke pending task untuk keperluan receipt
			if(result.get("othersInfo") != null) {
				CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findOne(model.getPendingTaskId());
				
				if(pendingTask != null) {
					Class<?> clazz = Class.forName(pendingTask.getModel());

					if(pendingTask.getValuesStr() != null) {
						Map<String, Object> valueMap = (Map<String, Object>) objectMapper.readValue(pendingTask.getValuesStr(), clazz);
						
						List<Map<String,Object>> othersInfo = (List<Map<String,Object>>)result.get("othersInfo");
						List<Map<String,Object>> billsInfo = new ArrayList<>();
						if (ValueUtils.hasValue(othersInfo)) {
							Locale locale = LocaleContextHolder.getLocale();
							for (Map<String,Object> info : othersInfo) {
								String label = (String)info.get("label");
								
								Map<String, Object> billInfo = new HashMap<>();
								billInfo.put("key", info.get("key"));
								billInfo.put("label", message.getMessage(label, null, label, locale));
								billInfo.put("value", info.get("value"));	
								billInfo.put("visible", info.get("visible"));
								billInfo.put("type", info.get("type"));
								billsInfo.add(billInfo);
							}
						}
						
						//replace new othersInfo to map
						valueMap.put("othersInfo", billsInfo);
						String jsonObj = objectMapper.writeValueAsString(valueMap);
						pendingTask.setValues(jsonObj);
						pendingTaskRepo.save(pendingTask);
						//----------------------------------------------
						
					}
				}
			}
			
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
				globalTransactionService.save(model.getCorporate().getId(), TaxPaymentSC.menuCode, 
						model.getService().getCode(), model.getReferenceNo(), 
						model.getId(), model.getTransactionCurrency(), model.getTotalChargeEquivalentAmount(), 
						model.getTransactionAmount(), 
						model.getTotalDebitedEquivalentAmount(), model.getIsError());
			}else {
				notifyBankEmail(model, appCode);
			}
			//----------------------------------
			
			if(isRollback) {
				if(limitUpdated) { // reverse usage
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
	
	private Map<String, Object> prepareInputsForEAI(TaxPaymentModel model, String appCode) throws Exception {
		Map<String, Object> inputs = new HashMap<>();
		
		inputs.put("channel", appCode);
		inputs.put("channelRefNo", model.getId());
		
		AccountModel sourceAccountModel = model.getSourceAccount();
		
		inputs.put("debitAccountNo", sourceAccountModel.getAccountNo());
		inputs.put("debitAccountName", sourceAccountModel.getAccountName());
		inputs.put("debitAccountType", sourceAccountModel.getAccountType().getCode());
		inputs.put("debitAccountCurrencyCode", sourceAccountModel.getCurrency().getCode());
		
		inputs.put("isNpwp", model.getIsNpwp());
		
		String isNpwp = model.getIsNpwp();
		if(isNpwp.equals(ApplicationConstants.YES)) {
			inputs.put("npwpNo", model.getNpwpNo());
		} else {
			inputs.put("kppNo", model.getKppNo());
			inputs.put("identityType", model.getIdentityType().getCode());
			inputs.put("identityNo", model.getIdentityNo());
		}
		
		inputs.put("benName", model.getBenName());
		inputs.put("benAddress1", model.getBenAddress1());
		inputs.put("benAddress2", model.getBenAddress2());
		inputs.put("benAddress3", model.getBenAddress3());
		inputs.put("cityName", model.getCityName());
		inputs.put("taxType", model.getTaxType().getCode());
		inputs.put("depositType", model.getDepositType().getTaxDepositCode());
		inputs.put("nopNo", model.getNopNo());
		inputs.put("skNo", model.getSkNo());
		inputs.put("periodMonthFrom", model.getPeriodMonthFrom());
		inputs.put("periodMonthTo", model.getPeriodMonthTo());
		inputs.put("periodYear", model.getPeriodYear());
		
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
				
		inputs.put("remark1", model.getRemark1());
		inputs.put("remark2", model.getRemark2());
		inputs.put("remark3", model.getRemark3());
		
		inputs.put("serviceCode", model.getService().getCode());
		inputs.put("serviceName", model.getService().getName());

		inputs.put("refNo", model.getReferenceNo());
		inputs.put("senderRefNo", model.getSenderRefNo());
		
		inputs.put("idPeserta", model.getDepositorNpwpNo());
		inputs.put("billingId", model.getBillingId());
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("inputs : " + inputs);
			logger.debug("\n\n\n\n\n");
		}
		
		return inputs;
		
	}
	
	/*private void notifyCorporate(TaxPaymentModel model, String appCode) {
		try {
			CorporateModel corpModel = model.getCorporate();
			String email1 = corpModel.getEmail1();
			String email2 = corpModel.getEmail2();
			
			List<String> emails = new ArrayList<>();
			if (ValueUtils.hasValue(email1))
				emails.add(email1);
			
			if (ValueUtils.hasValue(email2))
				emails.add(email2);
			
			if (emails.size()>0) {
				Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
				inputs.put("emails", emails);
				inputs.put("subject", "notify corporate");
				
				eaiAdapter.invokeService(EAIConstants.CORPORATE_TRANSFER_NOTIFICATION, inputs);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}*/
	
	private void notifyTrxUsersHistory(TaxPaymentModel model, String appCode) {
		try {
			List<String> emails = pendingTaskService.getCorporateUserEmailWithNotifyTrx(model.getId());
			if (emails.size() > 0) {
				IDMMenuModel menu = idmRepo.isIDMMenuValid(TaxPaymentSC.menuCode);
				
				Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
				inputs.put("instructionMode", model.getInstructionMode());
				inputs.put("instructionDate", model.getInstructionDate());
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
	
	private void notifyBeneficiary(TaxPaymentModel model, String appCode) {
		if (ValueUtils.hasValue(model.getNotifyBenValue())) {
			String[] vals = getMultipleEntries(model.getNotifyBenValue());			
			if (vals!=null && vals.length>0) {
				try {
					Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
					inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
					inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
					inputs.put("emails", Arrays.asList(vals));
					inputs.put("subject", "Beneficiary Transfer Notification");

					eaiAdapter.invokeService(EAIConstants.BENEFICIARY_TRANSFER_NOTIFICATION, inputs);
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
				
				List<TaxPaymentModel> taxPaymentList = taxPaymentRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_FUTURE_DATE, 
						new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(taxPaymentList);
				
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void executeAllBatch(List<TaxPaymentModel> list) throws Exception {
		List<SpringBeanInvokerData<String>> jobs = new ArrayList<>();
		for(TaxPaymentModel model : list) {
			jobs.add(new SpringBeanInvokerData<>(model.getId(), "TaxPaymentService", "executeBatchTransaction", new Object[] {model}, jobTimeout));
		}

		SpringBeanInvokerHelper.invokeAndForget(invoker, jobs, invokerTimeout, concurrencies);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void executeBatchTransaction(TaxPaymentModel taxPayment) {
		if(logger.isDebugEnabled())
			logger.debug("Process recurring taxPayment : " + taxPayment.getId() + " - " + taxPayment.getReferenceNo());
		
		boolean success = false;
		boolean isTimeOut = false;
		
		String errorCode = null;
		Timestamp activityDate = DateUtils.getCurrentTimestamp();
		try {			
			taxPayment.setIsProcessed(ApplicationConstants.YES);
			taxPaymentRepo.save(taxPayment);
			
			//avoid lazy when get details
			taxPayment = taxPaymentRepo.findOne(taxPayment.getId());
		
			doTransfer(taxPayment, ApplicationConstants.APP_GPCASHBO);
			success = true;
		} catch (BusinessException e) {
			errorCode = e.getErrorCode();
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (success) {
				if (ApplicationConstants.YES.equals(taxPayment.getIsError())) {
					pendingTaskRepo.updatePendingTask(taxPayment.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
				
					trxStatusService.addTransactionStatus(taxPayment.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, taxPayment.getId(), true, taxPayment.getErrorCode());
				} else {
					pendingTaskRepo.updatePendingTask(taxPayment.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS);
					
					trxStatusService.addTransactionStatus(taxPayment.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, taxPayment.getId(), false, null);
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
					pendingTaskRepo.updatePendingTask(taxPayment.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);				
					trxStatusService.addTransactionStatus(taxPayment.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, taxPayment.getId(), true, errorCode);
				}else {
					pendingTaskRepo.updatePendingTask(taxPayment.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE);				
					trxStatusService.addTransactionStatus(taxPayment.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE, taxPayment.getId(), true, errorCode);
				}
			}
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
				TaxPaymentModel model = taxPaymentRepo.findOne(trxStatus.getEaiRefNo());
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
	
	@Override
	public Map<String, Object> cancelTransaction(Map<String, Object> map)
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
	
	private List<Map<String, Object>> prepareDetailTransactionMap(TaxPaymentModel model, TransactionStatusModel trxStatus) throws Exception {
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
		modelMap.put("creditAccount", ApplicationConstants.EMPTY_STRING);
		modelMap.put("creditAccountName", ApplicationConstants.EMPTY_STRING);
		modelMap.put("creditAccountCurrency", ApplicationConstants.EMPTY_STRING);
		modelMap.put("senderRefNo", ValueUtils.getValue(model.getSenderRefNo()));
		modelMap.put("benRefNo", ApplicationConstants.EMPTY_STRING);
		
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
	public Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {			
			Locale locale = LocaleContextHolder.getLocale();
			String transactionStatusId = (String) map.get("receiptId");
			TransactionStatusModel trxStatus = trxStatusRepo.findOne(transactionStatusId);
			
			SimpleDateFormat sdfDateTimeTrxDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			SimpleDateFormat sdfDate2 = new SimpleDateFormat("dd MMM yyyy");
//			SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
			
			HashMap<String, Object> reportParams = new HashMap<>();
			reportParams.put("trxDate", sdfDateTimeTrxDate.format(trxStatus.getActivityDate()));
			reportParams.put("trxTime", sdfDate2.format(trxStatus.getActivityDate()));
			reportParams.put("transactionStatus", message.getMessage(trxStatus.getStatus().toString(), null, trxStatus.getStatus().toString(), locale));
			reportParams.put("errorDscp", trxStatus.getErrorCode());
			
			Path bankLogoPath = Paths.get(bankLogo);
			BufferedImage bankLogo = ImageIO.read(bankLogoPath.toFile());
			reportParams.put("bankLogo", bankLogo);
			
			TaxPaymentModel model = taxPaymentRepo.findOne(trxStatus.getEaiRefNo());
			CorporateUserPendingTaskModel pending = (CorporateUserPendingTaskModel) pendingTaskRepo.findOne(trxStatus.getPendingTaskId());
			setParamForDownloadTrxStatus(reportParams, model,pending);
			
			String masterReportFile = reportFolder + File.separator + "TransactionStatus" + 
					File.separator + "BillPayment" + File.separator + "download-transaction-bill" + "-" + ((String) reportParams.get("institution")).toLowerCase() + "-"
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
	
	private void setParamForDownloadTrxStatus(Map<String, Object> reportParams, TaxPaymentModel model, CorporateUserPendingTaskModel pending) throws JsonParseException, JsonMappingException, ClassNotFoundException, IOException {
		DecimalFormat df = new DecimalFormat(moneyFormat);
		if(model != null) {
			reportParams.put("service", ValueUtils.getValue(model.getService().getDscp()));
			
			AccountModel sourceAccount = model.getSourceAccount();
			reportParams.put("sourceAccount", sourceAccount.getAccountNo());
			reportParams.put("sourceAccountName", sourceAccount.getAccountName());
			
			reportParams.put("benAccount", ApplicationConstants.EMPTY_STRING);
			reportParams.put("benAccountName", ApplicationConstants.EMPTY_STRING);
			
			reportParams.put("transactionCurrency", model.getTransactionCurrency());
			reportParams.put("transactionAmount", df.format(model.getTransactionAmount()));
			
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
			
			reportParams.put("remark1", ValueUtils.getValue(model.getRemark1()));
			reportParams.put("refNo", model.getReferenceNo());
			
			Map<String, Object> pendingTaskValues = (Map<String, Object>) objectMapper.readValue(pending.getValuesStr(), Class.forName(pending.getModel()));
			
			setReportParamMPNG2(reportParams, pendingTaskValues);
			
			String ntpn = taxPaymentRepo.findNTPByRefNo(model.getReferenceNo());
			reportParams.put("ntpn", ntpn);
			
			String billId = model.getBillingId();
			String prefix = billId.substring(0,1);				
			switch (prefix) {
				case "0":
				case "1":
				case "2":
				case "3":
					reportParams.put("institution", "djp");
					
					// Re-Format masaPajak for DJP only
					if(reportParams.get("masaPajak")!= null) {
						String masaPajak = (String) reportParams.get("masaPajak");
						String pattern = "dd/MM/yyyy HH:mm:ss";
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
						LocalDateTime localDateTime = LocalDateTime.from(formatter.parse(masaPajak));
						
						Timestamp tms = Timestamp.valueOf(localDateTime);
						SimpleDateFormat sdfDate = new SimpleDateFormat("ddMMyyyy");
						reportParams.put("masaPajak", sdfDate.format(tms));
					}
					break;
				case "4":
				case "5":
				case "6":
					reportParams.put("institution", "djbc");
					break;
				case "7":
				case "8":
				case "9":
					reportParams.put("institution", "dja");
					break;
				default:
					break;
				}
			
			reportParams.put("channel", "Internet Banking Corporate");
			reportParams.put("ntb", model.getNtb());
			reportParams.put("ntpn", model.getNtp());
			reportParams.put("stan", model.getStan());
			
		} 
	}
	
	@SuppressWarnings("unchecked")
	private void setReportParamMPNG2(Map<String, Object> reportParams,Map<String, Object> pendingTaskValues) {
		
		if (pendingTaskValues.get("othersInfo") != null) {

			DecimalFormat df = new DecimalFormat(moneyFormat);
			List<Map<String, Object>> infoList = (List<Map<String, Object>>) pendingTaskValues.get("othersInfo");

			for (Map<String, Object> infoMap : infoList) {
				
				if (infoMap.get("key") !=null) {
					if (String.valueOf(infoMap.get("key")).toLowerCase().contains("amount")) {
						reportParams.put(String.valueOf(infoMap.get("key")), df.format(new BigDecimal(String.valueOf(infoMap.get("value")))));
					}else {
						reportParams.put(String.valueOf(infoMap.get("key")), String.valueOf(infoMap.get("value")));
					}
					
				}
				
			}

		}
	}
	
	@Override
	public Map<String, Object> npwpInquiry(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {			
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("npwpNo", (String) map.get("npwpNo"));
			inputs.put("kodeMAP", (String) map.get("kodeMAP"));
			inputs.put("jenisSetoran", (String) map.get("jenisSetoran"));
			return eaiAdapter.invokeService(TaxPaymentConstants.EAI_INQUIRY_NPWP, inputs);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private TaxTypeModel isTaxTypeValid(String code, boolean isNopEmpty) throws Exception {
		TaxTypeModel model = taxTypeRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100220");
			} else {
				if (model.getCode().startsWith("4113") && isNopEmpty == true) {
					throw new BusinessException("GPT-0200015");
				}
			}
		} else {
			throw new BusinessException("GPT-0100220");
		}
		
		return model;
	}
	
	private DepositTypeModel isDepositTypeValid(String code, boolean isNopEmpty) throws Exception {
		DepositTypeModel model = depositTypeRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100221");
			} else {
				if (model.getTaxDepositCode().equals("402") && isNopEmpty == true) {
					throw new BusinessException("GPT-0200015");
				}
			}
		} else {
			throw new BusinessException("GPT-0100221");
		}
		
		return model;
	}
	

	private void isPeriodYearValid(String yearInput) throws Exception{
		Calendar inputYear = Calendar.getInstance();
		inputYear.set(Calendar.YEAR, Integer.parseInt(yearInput));
		
		Calendar minYear = Calendar.getInstance();
		minYear.set(Calendar.YEAR, 1945);
			
		int maxYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
		
		if (minYear.get(Calendar.YEAR) > inputYear.get(Calendar.YEAR) || inputYear.get(Calendar.YEAR) > maxYear) {
			throw new BusinessException("GPT-0200018");
		}
		
	}
	
	private void isTaxPeriodMonthValid(String fromMonth, String toMonth) throws BusinessException{
		int fromMonthInt = Integer.valueOf(fromMonth);
		int toMonthInt = Integer.valueOf(toMonth);
		
		if (fromMonthInt > toMonthInt) {
			throw new BusinessException("GPT-0200019");
		}
	}
	
	private void notifyBankEmail(TaxPaymentModel model, String appCode) {		
		
		try {

			String emails = maintenanceRepo.isSysParamValid(SysParamConstants.BANK_EMAIL).getValue();
			String[] vals = getMultipleEntries(emails);
			if (vals!=null && vals.length>0) {
				Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
				inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
				inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
				inputs.put("emails",Arrays.asList(vals));
				inputs.put("subject", "Suspect Transaction Notification");

				eaiAdapter.invokeService(EAIConstants.SUSPECT_TRANCATION_NOTIFICATION, inputs);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}				
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
			
			TaxPaymentModel taxPayment = taxPaymentRepo.findByPendingTaskIdAndIsProcessed(pendingTaskId, ApplicationConstants.YES);
			
			if(status.equals("SUCCESS")) {
				isError = "N";
				pendingTaskRepo.updatePendingTask(taxPayment.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS);
				trxStatusService.addTransactionStatus(taxPayment.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, taxPayment.getId(), false, null);
				
				// send email to beneficiary only if not execute with failure
				if (ApplicationConstants.YES.equals(taxPayment.getIsNotifyBen())) {
					notifyBeneficiary(taxPayment, ApplicationConstants.APP_GPCASHBO);
				}
				
			}else {
				pendingTaskRepo.updatePendingTask(taxPayment.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
				trxStatusService.addTransactionStatus(taxPayment.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, taxPayment.getId(), true, taxPayment.getErrorCode());
			}
			
			pendingTaskRepo.updateErrorTimeoutFlagPendingTask(pendingTaskId, ApplicationConstants.NO);
			
			globalTransactionService.save(taxPayment.getCorporate().getId(), TaxPaymentSC.menuCode, 
					taxPayment.getService().getCode(), taxPayment.getReferenceNo(), 
					taxPayment.getId(), taxPayment.getTransactionCurrency(), taxPayment.getTotalChargeEquivalentAmount(), 
					taxPayment.getTransactionAmount(), 
					taxPayment.getTotalDebitedEquivalentAmount(), isError);
			
			notifyTrxUsersHistory(taxPayment, ApplicationConstants.APP_GPCASHBO);
			
			
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

}