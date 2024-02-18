package com.gpt.product.gpcash.retail.transaction.domestic.services;

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
import com.gpt.component.maintenance.domesticbank.model.DomesticBankModel;
import com.gpt.component.maintenance.errormapping.model.ErrorMappingModel;
import com.gpt.component.maintenance.errormapping.repository.ErrorMappingRepository;
import com.gpt.component.maintenance.parametermt.model.BeneficiaryTypeModel;
import com.gpt.component.maintenance.parametermt.model.CountryModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.sysparam.services.SysParamService;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.retail.CustomerUtilsRepository;
import com.gpt.product.gpcash.retail.beneficiarylist.model.CustomerBeneficiaryListDomesticModel;
import com.gpt.product.gpcash.retail.beneficiarylist.repository.CustomerBeneficiaryListDomesticRepository;
import com.gpt.product.gpcash.retail.beneficiarylist.services.CustomerBeneficiaryListDomesticService;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.customeraccount.model.CustomerAccountModel;
import com.gpt.product.gpcash.retail.customercharge.model.CustomerChargeModel;
import com.gpt.product.gpcash.retail.customercharge.repository.CustomerChargeRepository;
import com.gpt.product.gpcash.retail.customercharge.services.CustomerChargeService;
import com.gpt.product.gpcash.retail.pendingtaskuser.model.CustomerUserPendingTaskModel;
import com.gpt.product.gpcash.retail.pendingtaskuser.repository.CustomerUserPendingTaskRepository;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserPendingTaskService;
import com.gpt.product.gpcash.retail.pendingtaskuser.valueobject.CustomerUserPendingTaskVO;
import com.gpt.product.gpcash.retail.transaction.domestic.model.CustomerDomesticTransferModel;
import com.gpt.product.gpcash.retail.transaction.domestic.repository.CustomerDomesticTransferRepository;
import com.gpt.product.gpcash.retail.transaction.globaltransaction.services.CustomerGlobalTransactionService;
import com.gpt.product.gpcash.retail.transaction.utils.InstructionModeUtils;
import com.gpt.product.gpcash.retail.transaction.validation.services.CustomerTransactionValidationService;
import com.gpt.product.gpcash.retail.transactionstatus.CustomerTransactionActivityType;
import com.gpt.product.gpcash.retail.transactionstatus.CustomerTransactionStatus;
import com.gpt.product.gpcash.retail.transactionstatus.model.CustomerTransactionStatusModel;
import com.gpt.product.gpcash.retail.transactionstatus.repository.CustomerTransactionStatusRepository;
import com.gpt.product.gpcash.retail.transactionstatus.services.CustomerTransactionStatusService;
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
public class CustomerDomesticTransferServiceImpl implements CustomerDomesticTransferService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.batch.transations.timeout-invoker}")
	private int invokerTimeout;
	
	@Value("${gpcash.batch.transations.timeout-job}")
	private int jobTimeout;

	@Value("${gpcash.batch.transations.concurrencies}")
	private int concurrencies;
	
	@Autowired
	private CustomerDomesticTransferRepository domesticTransferRepo;

	@Autowired
	private CustomerUtilsRepository customerUtilsRepo;

	@Autowired
	private CustomerUserPendingTaskService pendingTaskService;

	@Autowired
	private CustomerChargeService customerChargeService;

	@Autowired
	private CustomerBeneficiaryListDomesticService beneficiaryListDomesticService;

	@Autowired
	private CustomerTransactionValidationService transactionValidationService;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private MaintenanceRepository maitenanceRepo;	
	
	@Autowired
	private ErrorMappingRepository errorMappingRepo;
	
	@Autowired
	private SysParamService sysParamService;
	
	@Autowired
	private CustomerChargeRepository customerChargeRepo;
	
	@Autowired
	private EAIEngine eaiAdapter;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private ISpringBeanInvoker invoker;	
	
	@Autowired
	private CustomerTransactionStatusService trxStatusService;

	@Autowired
	private CustomerTransactionStatusRepository trxStatusRepo;
	
	@Autowired
	private CustomerGlobalTransactionService globalTransactionService;
	
	@Autowired
	private CustomerUserPendingTaskRepository pendingTaskRepo;
	
	@Autowired
	private CustomerBeneficiaryListDomesticRepository beneficiaryListDomecticRepo;
	
	@Autowired
	private IDMRepository idmRepo;
	
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

			CustomerUserPendingTaskVO vo = setCustomerUserPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
			} else {
				throw new BusinessException("GPT-0100003");
			}

			pendingTaskService.savePendingTask(vo);

			resultMap.put(ApplicationConstants.PENDINGTASK_VO, vo);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			String customerId = (String) map.get(ApplicationConstants.CUST_ID);
			
			// if isSaveBenFlag = Y then validate if accountNo already in
			// already exist or not
			if (ApplicationConstants.YES.equals(map.get("isSaveBenFlag"))) {
				CustomerBeneficiaryListDomesticModel beneModel = beneficiaryListDomesticService.getExistingRecord((String) map.get(ApplicationConstants.TRANS_BEN_ID), customerId, false);
				if(beneModel != null) {
					throw new BusinessException("GPT-0100099");
				}
			}
			
			// if isBeneficiaryFlag = Y then validate if accountNo must exist in table
			if (ApplicationConstants.YES.equals(map.get("isBeneficiaryFlag"))) {
				CustomerBeneficiaryListDomesticModel beneModel = beneficiaryListDomecticRepo.findOne((String) map.get(ApplicationConstants.TRANS_BEN_ID));
				if(beneModel == null) {
					throw new BusinessException("GPT-0100100");
				}
			}
			
			//------------------------------------------------------
			
			// TODO implement calculate equivalent amount in future if implement
			// cross currency transaction
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Override
	public void checkTransactionThreshold(BigDecimal equivalentTransactionAmount, String transactionServiceCode) throws BusinessException, ApplicationException{
		try {
			if(ApplicationConstants.SRVC_GPT_FTR_DOM_LLG.equals(transactionServiceCode)) {
				BigDecimal thresholdValue = new BigDecimal(maintenanceRepo.isSysParamValid(SysParamConstants.SKN_THRESHOLD).getValue());
				if(equivalentTransactionAmount.compareTo(thresholdValue) > 0) {
					throw new BusinessException("GPT-0100150", new String[] {thresholdValue.toPlainString()});
				}
			} else if(ApplicationConstants.SRVC_GPT_FTR_DOM_RTGS.equals(transactionServiceCode)) {
				BigDecimal minRTGS = new BigDecimal(maintenanceRepo.isSysParamValid(SysParamConstants.MIN_RTGS_TRANSACTION).getValue());
				
				if(equivalentTransactionAmount.compareTo(minRTGS) < 0) {
					throw new BusinessException("GPT-0100207", new String[] {minRTGS.toPlainString()});
				}
			} else if(ApplicationConstants.SRVC_GPT_FTR_DOM_ONLINE.equals(transactionServiceCode)) {
				BigDecimal maxOnline = new BigDecimal(maintenanceRepo.isSysParamValid(SysParamConstants.MAX_ONLINE_TRANSACTION).getValue());
				
				if(equivalentTransactionAmount.compareTo(maxOnline) > 0) {
					throw new BusinessException("GPT-0100236", new String[] {maxOnline.toPlainString()});
				}
				
			}
		} catch (BusinessException e) {
			throw e;
		}catch (Exception e) {
			throw new ApplicationException(e.getMessage(), e);
		}
	}

	private CustomerUserPendingTaskVO setCustomerUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String customerId = (String) map.get(ApplicationConstants.CUST_ID);
		
		String accountDetailId = (String) map.get(ApplicationConstants.ACCOUNT_DTL_ID);
		BigDecimal transactionAmount = (BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT);
		String benId = (String) map.get(ApplicationConstants.TRANS_BEN_ID);
		String uniqueKey = customerId.concat(ApplicationConstants.DELIMITER_PIPE).concat(accountDetailId)
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(transactionAmount.toPlainString())
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(benId)
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(String.valueOf((DateUtils.getCurrentTimestamp().getTime())));

		String instructionMode = (String) map.get(ApplicationConstants.INSTRUCTION_MODE);
		String sessionTime = (String) map.get("sessionTime");
		Timestamp instructionDate = (Timestamp) map.get(ApplicationConstants.INSTRUCTION_DATE);
		
		CustomerUserPendingTaskVO vo = new CustomerUserPendingTaskVO();
		vo.setUniqueKey(uniqueKey);
		vo.setCreatedBy(customerId);
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setTransactionServiceCode((String) map.get(ApplicationConstants.TRANS_SERVICE_CODE));
		vo.setService("CustomerDomesticTransferSC");
		vo.setCustomerId(customerId);
		vo.setTransactionAmount(transactionAmount);
		vo.setTransactionCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		vo.setSourceAccountDetailId(accountDetailId);
		vo.setSessionTime(sessionTime);
		
		CustomerAccountModel ca = customerUtilsRepo.isCustomerAccountIdValid(vo.getCustomerId(), 
				vo.getSourceAccountDetailId());	
		
		//set debit info
		AccountModel sourceAccount = ca.getAccount();
		vo.setSourceAccount(sourceAccount.getAccountNo());
		vo.setSourceAccountName(sourceAccount.getAccountName());
		
		CurrencyModel sourceCurrency = sourceAccount.getCurrency();
		vo.setSourceAccountCurrencyCode(sourceCurrency.getCode());
		vo.setSourceAccountCurrencyName(sourceCurrency.getName());
		
		//------------------------------------------------------------------
		
		//set remark
		vo.setRemark1((String) map.get("remark1"));
		vo.setRemark2((String) map.get("remark2"));
		vo.setRemark3((String) map.get("remark3"));
		//------------------------------------------------------------------
				
		String benCurrencyCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_CURRENCY_CODE).getValue();
		
		//set credit info
		vo.setBenRefNo((String) map.get("benRefNo"));
		
		if (ApplicationConstants.YES.equals((String) map.get("isBeneficiaryFlag"))) {
			CustomerBeneficiaryListDomesticModel beneDomestic=  customerUtilsRepo.getBeneficiaryListDomesticRepo().findOne(benId);
			vo.setBenAccount(beneDomestic.getBenAccountNo());
			vo.setBenAccountName(beneDomestic.getBenAccountName());
			
			if(beneDomestic.getBenAccountCurrency() != null) {
				benCurrencyCode = beneDomestic.getBenAccountCurrency();
			}
			
			CurrencyModel benCurrency = maintenanceRepo.isCurrencyValid(benCurrencyCode);
			vo.setBenAccountCurrencyCode(benCurrency.getCode());
			vo.setBenAccountCurrencyName(benCurrency.getName());
			
		} else {
			vo.setBenAccount(benId);
			vo.setBenAccountName((String) map.get(ApplicationConstants.TRANS_BEN_ACCT_NAME));
			
			if((String) map.get(ApplicationConstants.TRANS_BEN_ACCT_CURRENCY) != null) {
				benCurrencyCode = (String) map.get(ApplicationConstants.TRANS_BEN_ACCT_CURRENCY);
			}
			
			CurrencyModel benCurrency = maintenanceRepo.isCurrencyValid(benCurrencyCode);
			vo.setBenAccountCurrencyCode(benCurrency.getCode());
			vo.setBenAccountCurrencyName(benCurrency.getName());
		}
		
		vo.setBenBankCode((String) map.get("bankCode"));
		//------------------------------------------------------------------
		
		vo.setTotalChargeEquivalentAmount((BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_CHARGE_EQ));
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
		
		String refNoSpecialRate = (String) map.get("treasuryCode");		
		vo.setRefNoSpecialRate(refNoSpecialRate);
		
		return vo;
	}

	@SuppressWarnings("unchecked")
	private CustomerDomesticTransferModel setMapToModel(CustomerDomesticTransferModel domesticTransfer, Map<String, Object> map,
			boolean isNew, CustomerUserPendingTaskVO vo) throws Exception {
		String benId = (String) map.get(ApplicationConstants.TRANS_BEN_ID);
		String benAccountName = (String) map.get(ApplicationConstants.TRANS_BEN_ACCT_NAME);
		String benAccountCurrency = (String) map.get(ApplicationConstants.TRANS_BEN_ACCT_CURRENCY);
		String transactionServiceCode = (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE);
		String isBeneficiaryFlag = (String) map.get("isBeneficiaryFlag");
		String isSaveBenFlag = (String) map.get("isSaveBenFlag");
		String instructionMode = (String) map.get("instructionMode");
		
		Timestamp instructionDate = vo.getInstructionDate();
		if(ApplicationConstants.SI_RECURRING.equals(vo.getInstructionMode())) {
			//override instructionDate to today timestamp
			Timestamp recurringInstructionDate = InstructionModeUtils.getStandingInstructionDate(vo.getRecurringParamType(), vo.getRecurringParam(), 
					DateUtils.getCurrentTimestamp());
			
			instructionDate = DateUtils.getInstructionDateBySessionTime(vo.getSessionTime(), recurringInstructionDate);
		}
		
		String chargeInstruction = (String) map.get("chargeInstruction");
		String benAliasName = (String) map.get("benAliasName");
		String address1 = (String) map.get("address1");
		String address2 = (String) map.get("address2");
		String address3 = (String) map.get("address3");
		String isBenResident = (String) map.get("isBenResident");
		String benResidentCountryCode = (String) map.get("benResidentCountryCode");
		String isBenCitizen = (String) map.get("isBenCitizen");
		String benCitizenCountryCode = (String) map.get("benCitizenCountryCode");
		String beneficiaryTypeCode = (String) map.get("beneficiaryTypeCode");
		String bankCode = (String) map.get("bankCode");

		// set ID
		domesticTransfer.setId(vo.getId());

		// set transaction information
		domesticTransfer.setReferenceNo(vo.getReferenceNo());
		domesticTransfer.setSenderRefNo(vo.getSenderRefNo());
		domesticTransfer.setBenRefNo(vo.getBenRefNo());

		IDMMenuModel menu = new IDMMenuModel();
		menu.setCode(vo.getMenuCode());
		domesticTransfer.setMenu(menu);
		
		CustomerModel custModel = customerUtilsRepo.isCustomerValid(vo.getCustomerId());
		domesticTransfer.setCustomer(custModel);
		
		IDMApplicationModel application = new IDMApplicationModel();
		//application code diambil dr yg login
		application.setCode((String) map.get(ApplicationConstants.APP_CODE));
		domesticTransfer.setApplication(application);
		
		AccountModel sourceAccountModel = productRepo.isAccountValid(vo.getSourceAccount());
		domesticTransfer.setSourceAccount(sourceAccountModel);
		
		domesticTransfer.setTransactionAmount(new BigDecimal(map.get(ApplicationConstants.TRANS_AMOUNT).toString()));
		domesticTransfer.setTransactionCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		domesticTransfer.setTotalChargeEquivalentAmount(
				new BigDecimal(map.get(ApplicationConstants.TRANS_TOTAL_CHARGE_EQ).toString()));
		domesticTransfer.setTotalDebitedEquivalentAmount(
				new BigDecimal(map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT).toString()));

		// set transaction service code
		ServiceModel service = new ServiceModel();
		service.setCode(transactionServiceCode);
		domesticTransfer.setService(service);

		// set beneficiary info
		// set benAccountNo based on benId
		setBenAccountInfo(domesticTransfer, benId, benAccountName, benAccountCurrency, transactionServiceCode, isSaveBenFlag,
				vo.getCustomerId(), benAliasName, address1, address2, address3, isBenResident,
				benResidentCountryCode, isBenCitizen, benCitizenCountryCode, beneficiaryTypeCode, bankCode, isBeneficiaryFlag, vo.getCreatedBy());

		// set additional information
		domesticTransfer.setSenderRefNo((String) map.get("senderRefNo"));
		domesticTransfer.setIsFinalPayment((String) map.get("isFinalPayment"));
		domesticTransfer.setBenRefNo((String) map.get("benRefNo"));
		domesticTransfer.setRemark1((String) map.get("remark1"));
		domesticTransfer.setRemark2((String) map.get("remark2"));
		domesticTransfer.setRemark3((String) map.get("remark3"));
		domesticTransfer.setIsNotifyBen((String) map.get("isNotify"));
		domesticTransfer.setNotifyBenValue((String) map.get("notifyBenValue"));
		
		setInstructionMode(domesticTransfer, instructionMode, instructionDate, map);
		
		setCharge(domesticTransfer, (ArrayList<Map<String,Object>>) map.get("chargeList"));
		
		domesticTransfer.setChargeTo(chargeInstruction);
		
		domesticTransfer.setRefNoSpecialRate(vo.getRefNoSpecialRate());
		
		return domesticTransfer;
	}
	
	private void setCharge(CustomerDomesticTransferModel domesticTransfer, List<Map<String, Object>> chargeList){
		for(int i=1; i <= chargeList.size(); i++){
			Map<String, Object> chargeMap = chargeList.get(i - 1);
			String chargeId = (String) chargeMap.get("id");
			BigDecimal value = new BigDecimal(chargeMap.get("value").toString());
			BigDecimal valueEquivalent = new BigDecimal(chargeMap.get("valueEq").toString());
			String currencyCode = (String) chargeMap.get("currencyCode");
			
			if(i == 1){
				domesticTransfer.setChargeType1(chargeId);
				domesticTransfer.setChargeTypeAmount1(value);
				domesticTransfer.setChargeTypeAmountEquivalent1(valueEquivalent);
				domesticTransfer.setChargeTypeCurrency1(currencyCode);
			} else if(i == 2){
				domesticTransfer.setChargeType2(chargeId);
				domesticTransfer.setChargeTypeAmount2(value);
				domesticTransfer.setChargeTypeAmountEquivalent2(valueEquivalent);
				domesticTransfer.setChargeTypeCurrency2(currencyCode);
			} else if(i == 3){
				domesticTransfer.setChargeType3(chargeId);
				domesticTransfer.setChargeTypeAmount3(value);
				domesticTransfer.setChargeTypeAmountEquivalent3(valueEquivalent);
				domesticTransfer.setChargeTypeCurrency3(currencyCode);
			} else if(i == 4){
				domesticTransfer.setChargeType4(chargeId);
				domesticTransfer.setChargeTypeAmount4(value);
				domesticTransfer.setChargeTypeAmountEquivalent4(valueEquivalent);
				domesticTransfer.setChargeTypeCurrency4(currencyCode);
			} else if(i == 5){
				domesticTransfer.setChargeType5(chargeId);
				domesticTransfer.setChargeTypeAmount5(value);
				domesticTransfer.setChargeTypeAmountEquivalent5(valueEquivalent);
				domesticTransfer.setChargeTypeCurrency5(currencyCode);
			} 
		}
	}

	private void setInstructionMode(CustomerDomesticTransferModel domesticTransfer, String instructionMode, Timestamp instructionDate, 
			Map<String, Object> map) throws Exception {
		
		
		// set instruction mode
		domesticTransfer.setInstructionMode(instructionMode);

		if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
			domesticTransfer
					.setInstructionDate(transactionValidationService.getInstructionDateForRelease(instructionMode, instructionDate));
		} else if (instructionMode.equals(ApplicationConstants.SI_FUTURE_DATE)) {
			domesticTransfer.setInstructionDate(instructionDate);
		} else if (instructionMode.equals(ApplicationConstants.SI_RECURRING)) {
			domesticTransfer.setRecurringParamType((String) map.get("recurringParamType"));
			domesticTransfer.setRecurringParam((Integer) map.get("recurringParam"));
			domesticTransfer.setRecurringStartDate(instructionDate);
			if (map.get("recurringEndDate") instanceof Timestamp) {
				domesticTransfer.setRecurringEndDate((Timestamp) map.get("recurringEndDate")); //dari mobile balikin Timestamp
			} else {
			domesticTransfer.setRecurringEndDate((Timestamp) Helper.parseStringToTimestamp((String) map.get("recurringEndDate")));
			}
			domesticTransfer.setInstructionDate(instructionDate);
		}
	}

	private void setBenAccountInfo(CustomerDomesticTransferModel domesticTransfer, String benId, String benAccountName, String benAccountCurrency,
			String transactionServiceCode, String isSaveBenFlag, String customerId, String benAliasName,
			String address1, String address2, String address3, String isBenResident, String benResidentCountryCode,
			String isBenCitizen, String benCitizenCountryCode, String beneficiaryTypeCode, String bankCode, String isBeneficiaryFlag,
			String createdBy) throws Exception {
		
		String benAccountNo = null;
		DomesticBankModel domBankModel = null;
		boolean isBenOnline = false;
		
		if (domesticTransfer.getService().getCode().equals(ApplicationConstants.SRVC_GPT_FTR_DOM_ONLINE)) {
			isBenOnline = true;
		}
		
		if (ApplicationConstants.YES.equals(isBeneficiaryFlag)) {
			// cek if benId exist in bene table
			CustomerBeneficiaryListDomesticModel beneficiaryListDomestic = customerUtilsRepo.isCustomerBeneficiaryListDomesticValid(benId);

			// benId from beneficiary droplist
			benAccountNo = beneficiaryListDomestic.getBenAccountNo();
			benAccountName = beneficiaryListDomestic.getBenAccountName();
			benAccountCurrency = beneficiaryListDomestic.getBenAccountCurrency();
			benAliasName = beneficiaryListDomestic.getBenAliasName();
			address1 = beneficiaryListDomestic.getBenAddr1();
			address2 = beneficiaryListDomestic.getBenAddr2();
			address3 = beneficiaryListDomestic.getBenAddr3();
			
			if (!isBenOnline) {
				isBenResident = beneficiaryListDomestic.getLldIsBenResidence();
				benResidentCountryCode = beneficiaryListDomestic.getLldBenResidenceCountry().getCode();
				isBenCitizen = beneficiaryListDomestic.getLldIsBenCitizen();
				benCitizenCountryCode = beneficiaryListDomestic.getLldBenCitizenCountry().getCode();
				beneficiaryTypeCode = beneficiaryListDomestic.getBenType().getCode();
			}
			domBankModel = beneficiaryListDomestic.getBenDomesticBankCode();
		} else {
			// benId from third party account
			benAccountNo = benId;
			
			// check the bank first
			domBankModel = maitenanceRepo.isDomesticBankValid(bankCode);
		
			if (ApplicationConstants.YES.equals(isSaveBenFlag)) {
				// save to bene table
				beneficiaryListDomesticService.saveCustomerBeneficiary(customerId, benAccountNo,
					benAccountName, benAccountCurrency, ApplicationConstants.NO, null, 
					benAliasName, address1, address2, address3, isBenResident, benResidentCountryCode, 
					isBenCitizen, benCitizenCountryCode, beneficiaryTypeCode, bankCode, createdBy, isBenOnline);

			}
		}
		
		domesticTransfer.setBenAccountNo(benAccountNo);
		domesticTransfer.setBenAccountName(benAccountName);
		domesticTransfer.setBenAccountCurrency(benAccountCurrency);
		domesticTransfer.setBenAliasName(benAliasName);
		domesticTransfer.setBenAddr1(address1);
		domesticTransfer.setBenAddr2(address2);
		domesticTransfer.setBenAddr3(address3);
		domesticTransfer.setBenDomesticBankCode(domBankModel);		
		
		if (!isBenOnline) {
			
			domesticTransfer.setLldIsBenResidence(isBenResident);
			
			String localCountryCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_COUNTRY_CODE).getValue();
			
			if(ApplicationConstants.YES.equals(isBenResident)) {
				benResidentCountryCode = localCountryCode;
			}
			
			CountryModel benResidentCountry = new CountryModel();
			benResidentCountry.setCode(benResidentCountryCode);
			domesticTransfer.setLldBenResidenceCountry(benResidentCountry);
			
			
			domesticTransfer.setLldIsBenCitizen(isBenCitizen);
			
			if(ApplicationConstants.YES.equals(isBenCitizen)) {
				benCitizenCountryCode = localCountryCode;
			}
			CountryModel benCitizenCountry = new CountryModel();
			benCitizenCountry.setCode(benCitizenCountryCode);
			domesticTransfer.setLldBenCitizenCountry(benCitizenCountry);
			
			
			BeneficiaryTypeModel benType = new BeneficiaryTypeModel();
			benType.setCode(beneficiaryTypeCode);
			domesticTransfer.setBenType(benType);
		}
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public CustomerUserPendingTaskVO approve(CustomerUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {

		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			if(logger.isDebugEnabled()) {
				logger.debug("map : " + map);
			}

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				CustomerDomesticTransferModel domesticTransfer = new CustomerDomesticTransferModel();
				
				checkTransactionThreshold(vo.getTransactionAmount(), vo.getTransactionServiceCode());
				
				setMapToModel(domesticTransfer, map, true, vo);
				
				if(ApplicationConstants.SI_IMMEDIATE.equals(domesticTransfer.getInstructionMode())) {
					saveDomestic(domesticTransfer, vo.getCreatedBy(), ApplicationConstants.YES);

					doTransfer(domesticTransfer, (String)map.get(ApplicationConstants.APP_CODE));
					vo.setErrorCode(domesticTransfer.getErrorCode());
					vo.setIsError(domesticTransfer.getIsError());
				} else {
					saveDomestic(domesticTransfer, vo.getCreatedBy(), ApplicationConstants.NO);
					vo.setIsError(ApplicationConstants.NO);
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
	public CustomerUserPendingTaskVO reject(CustomerUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

	private void saveDomestic(CustomerDomesticTransferModel domesticTransfer, String createdBy, String isProcessed)
			throws ApplicationException, BusinessException {
		// set default value
		domesticTransfer.setIsProcessed(isProcessed);
		domesticTransfer.setCreatedDate(DateUtils.getCurrentTimestamp());
		domesticTransfer.setCreatedBy(createdBy);
		
		//jika pendingTaskId null maka transaksi bukan recurring, maka pendingTaskId = model.Id
		if(domesticTransfer.getPendingTaskId() == null)
			domesticTransfer.setPendingTaskId(domesticTransfer.getId());

		domesticTransferRepo.persist(domesticTransfer);
	}

	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			String benId = (String)map.get("benId");
			String bankCode = (String) map.get("bankCode");
			String benAccountNo = null;
			String benAccountName = null;
			String serviceCode = (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE);
			
			DomesticBankModel domesticBank = maintenanceRepo.isDomesticBankValid(bankCode);
			
			if (ApplicationConstants.YES.equals((String) map.get("isBeneficiaryFlag"))) {
				CustomerBeneficiaryListDomesticModel beneDomestic=  customerUtilsRepo.getBeneficiaryListDomesticRepo().findOne(benId);
				benAccountNo = beneDomestic.getBenAccountNo(); 
				benAccountName = beneDomestic.getBenAccountName();					
				
			} else {
				
				if (serviceCode.equals(ApplicationConstants.SRVC_GPT_FTR_DOM_ONLINE)) {
					Map<String, Object> inputs = new HashMap<>();
					inputs.put("accountNo", benId);
					inputs.put("onlineBankCode", domesticBank.getOnlineBankCode());
					inputs.put("chargeTo", map.get("chargeTo"));
					inputs.put("channel", domesticBank.getChannel());
					inputs.put("custRefNo", (String) map.get("benRefNo"));
					
					Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.DOMESTIC_ONLINE_ACCOUNT_INQUIRY, inputs);
					
					benAccountNo = (String) outputs.get("accountNo");
					benAccountName = (String) outputs.get("accountName");
					
				} else {
					benAccountNo = benId;
					benAccountName = (String) map.get(ApplicationConstants.TRANS_BEN_ACCT_NAME); 
					
					String beneType = ValueUtils.getValue((String) map.get("beneficiaryTypeCode"));
					if (beneType.equals("")) {
						throw new BusinessException("Beneficiary Type is mandatory");
					}
					
				}				
			}
			
			Map<String, Object> benAccountInfo = new HashMap<>();
			benAccountInfo.put("benId", benId);
			benAccountInfo.put("benAccountNo", benAccountNo);
			benAccountInfo.put("benAccountName", benAccountName);
			
			//saat ini masih implementasi local currency
			String currencyCode = maintenanceRepo.getSysParamRepo().findOne(SysParamConstants.LOCAL_CURRENCY_CODE).getValue();
			CurrencyModel localCurrency = maintenanceRepo.isCurrencyValid(currencyCode);
			benAccountInfo.put("benAccountCurrencyCode", localCurrency.getCode());
			benAccountInfo.put("benAccountCurrencyName", localCurrency.getName());
			
			resultMap.put("bankCode", domesticBank.getCode());
			resultMap.put("onlineBankCode", ValueUtils.getValue(domesticBank.getOnlineBankCode()));
			resultMap.put("memberCode", ValueUtils.getValue(domesticBank.getMemberCode()));
			resultMap.put("name", ValueUtils.getValue(domesticBank.getName()));

			String isBeneficiaryFlag = (String) map.get("isBeneficiaryFlag");
			if(isBeneficiaryFlag.equals(ApplicationConstants.YES)) {
				CustomerBeneficiaryListDomesticModel beneficiary = beneficiaryListDomesticService.findCustomerBeneficiary(benId);
				benAccountInfo.put("address1",ValueUtils.getValue(beneficiary.getBenAddr1()));
				benAccountInfo.put("address2", ValueUtils.getValue(beneficiary.getBenAddr2()));
				benAccountInfo.put("address3", ValueUtils.getValue(beneficiary.getBenAddr3()));
				
				if (!serviceCode.equals(ApplicationConstants.SRVC_GPT_FTR_DOM_ONLINE)) {
					benAccountInfo.put("isBenResident", ValueUtils.getValue(beneficiary.getLldIsBenResidence()));
					CountryModel lldBenResidenceCountry = beneficiary.getLldBenResidenceCountry();
					benAccountInfo.put("benResidentCountryCode", ValueUtils.getValue(lldBenResidenceCountry.getCode()));
					benAccountInfo.put("benResidentCountryName", ValueUtils.getValue(lldBenResidenceCountry.getName()));
					
					benAccountInfo.put("isBenCitizen", ValueUtils.getValue(beneficiary.getLldIsBenCitizen()));
					CountryModel lldBenCitizenCountry = beneficiary.getLldBenCitizenCountry();
					benAccountInfo.put("benCitizenCountryCode", ValueUtils.getValue(lldBenCitizenCountry.getCode()));
					benAccountInfo.put("benCitizenCountryName", ValueUtils.getValue(lldBenCitizenCountry.getName()));
					
					benAccountInfo.put("beneficiaryTypeCode", ValueUtils.getValue(beneficiary.getBenType().getCode()));
					benAccountInfo.put("beneficiaryTypeName", ValueUtils.getValue(beneficiary.getBenType().getName()));
				}
			}
			
			checkCustomValidation(map);

			resultMap.put("benAccountInfo", benAccountInfo);			
			resultMap.putAll(customerChargeService.getCustomerChargesEquivalent((String) map.get(ApplicationConstants.APP_CODE),
					(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE),
					(String) map.get(ApplicationConstants.CUST_ID),(String) map.get("sourceAccountCurrency")));
			resultMap.put(ApplicationConstants.TRANS_AMOUNT_EQ, map.get("equivalentAmount"));
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private void doTransfer(CustomerDomesticTransferModel model, String appCode) throws Exception {
		boolean isRollback = false;
		boolean limitUpdated = false;
		
		try {
			//untuk hitung totalcharge dalam IDR(localcurrency) charge salalu IDR
			BigDecimal totalCharge = model.getChargeTypeAmount1()!=null?model.getChargeTypeAmount1():BigDecimal.ZERO
					.add(model.getChargeTypeAmount2()!=null?model.getChargeTypeAmount2():BigDecimal.ZERO)
					.add(model.getChargeTypeAmount3()!=null?model.getChargeTypeAmount3():BigDecimal.ZERO)
					.add(model.getChargeTypeAmount4()!=null?model.getChargeTypeAmount4():BigDecimal.ZERO)
					.add(model.getChargeTypeAmount5()!=null?model.getChargeTypeAmount5():BigDecimal.ZERO);
			
			/*//update transaction limit
			transactionValidationService.updateTransactionLimit(model.getCustomer().getId(), 
					model.getService().getCode(), 
					model.getSourceAccount().getCurrency().getCode(), 
					model.getTransactionCurrency(), 
					model.getTotalDebitedEquivalentAmount(),
					model.getApplication().getCode());*/
			
			//untuk multi currency limit harus selalu IDR
			transactionValidationService.updateTransactionLimitEquivalent(model.getCustomer().getId(), 
					model.getService().getCode(), 
					model.getSourceAccount().getCurrency().getCode(), 
					model.getTransactionCurrency(), 
					model.getTransactionAmount(),
					model.getApplication().getCode(),
					totalCharge);
			
			//update bank forex limit
			transactionValidationService.updateBankForexLimit(
					model.getSourceAccount().getCurrency().getCode(),
					model.getTransactionCurrency(),
					model.getTransactionAmount(),
					totalCharge);
			
			limitUpdated = true;
			
			Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
			eaiAdapter.invokeService(EAIConstants.DOMESTIC_TRANSFER, inputs);
			
			model.setStatus("GPT-0100130");
			model.setIsError(ApplicationConstants.NO);
		} catch (BusinessException e) {
			String errorMsg = e.getMessage();
			if (errorMsg!=null && errorMsg.startsWith("EAI-")) {
				ErrorMappingModel errorMappingModel = errorMappingRepo.findOne(e.getMessage());
				
				if (errorMappingModel!=null && ApplicationConstants.YES.equals(errorMappingModel.getRollbackFlag())) {
					isRollback = true;
					throw e;
				}
				
				// Either no error mapping or no rollback...
				
			        //jika transaksi gagal tetapi tidak di rollback maka kena error execute with failure
				model.setStatus("GPT-0100129");
				model.setIsError(ApplicationConstants.YES);
				model.setErrorCode(errorMappingModel == null ? errorMsg : errorMappingModel.getCode());
			} else {
				isRollback = true;
				throw e;
			}				
		} catch (Exception e) {
			isRollback = true;
			throw new ApplicationException(e);
		} finally {
			//save transaction log
			globalTransactionService.save(model.getCustomer().getId(), CustomerDomesticTransferSC.menuCode, 
					model.getService().getCode(), model.getReferenceNo(), 
					model.getId(), model.getTransactionCurrency(), model.getTotalChargeEquivalentAmount(), 
					model.getTransactionAmount(), 
					model.getTotalDebitedEquivalentAmount(), model.getIsError());
			//----------------------------------
			
			if(isRollback) {
				if(limitUpdated) {
					try {
						transactionValidationService.reverseUpdateTransactionLimit(model.getCustomer().getId(), 
								model.getService().getCode(), 
								model.getSourceAccount().getCurrency().getCode(), 
								model.getTransactionCurrency(), 
								model.getTotalDebitedEquivalentAmount(),
								model.getApplication().getCode());
					} catch(Exception e) {
						logger.error("Failed to reverse the usage "+e.getMessage(),e);
					}
				}
			} else {
				try {
					// send email to customer email
//					notifyCustomer(model, appCode);				
					
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
	
	private Map<String, Object> prepareInputsForEAI(CustomerDomesticTransferModel model, String appCode) throws Exception {
		Map<String, Object> inputs = new HashMap<>();
		
		inputs.put("channel", appCode);
		inputs.put("channelRefNo", model.getId());

		AccountModel sourceAccountModel = model.getSourceAccount();
				
		inputs.put("debitAccountNo", sourceAccountModel.getAccountNo());
		inputs.put("debitAccountName", sourceAccountModel.getAccountName());
		inputs.put("debitAccountType", sourceAccountModel.getAccountType().getCode());
		inputs.put("debitAccountCurrencyCode", sourceAccountModel.getCurrency().getCode());
		
		inputs.put("benAccountNo", model.getBenAccountNo());
		inputs.put("benAccountName", model.getBenAccountName());
		inputs.put("benAccountCurrencyCode", model.getBenAccountCurrency());
		inputs.put("benAddress1", model.getBenAddr1());
		inputs.put("benAddress2", model.getBenAddr2());
		inputs.put("benAddress3", model.getBenAddr3());
		
		if (!model.getService().getCode().equals(ApplicationConstants.SRVC_GPT_FTR_DOM_ONLINE)) {
			inputs.put("benResidentFlag", model.getLldIsBenResidence());
			inputs.put("benCitizenFlag", model.getLldIsBenCitizen());
			inputs.put("benResidentCountryCode", model.getLldBenResidenceCountry().getCode());
			inputs.put("benCitizenCountryCode", model.getLldBenCitizenCountry().getCode());
			inputs.put("benTypeCode", model.getBenType().getCode());
		}
				
		CustomerModel custModel = model.getCustomer();
		inputs.put("remitterAddress1", custModel.getAddress1());
		inputs.put("remitterAddress2", custModel.getAddress2());
		inputs.put("remitterAddress3", custModel.getAddress3());
		inputs.put("remitterResidentFlag", custModel.getLldIsResidence());
		inputs.put("remitterResidentCountryCode", custModel.getResidenceCountry().getCode());
		inputs.put("remitterCitizenFlag", custModel.getLldIsCitizen());
		inputs.put("remitterCitizenCountryCode", custModel.getCitizenCountry().getCode());
		inputs.put("remitterType", ApplicationConstants.REMITTER_TYPE_INDIVIDUAL_CODE);
				
		inputs.put("destinationBankCode", model.getBenDomesticBankCode().getCode());
		inputs.put("destinationBankName", model.getBenDomesticBankCode().getName());
		inputs.put("destinationMemberBankCode", model.getBenDomesticBankCode().getMemberCode());
		inputs.put("destinationOnlineBankCode", model.getBenDomesticBankCode().getOnlineBankCode());
		
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

		inputs.put("refNo", model.getReferenceNo());
		inputs.put("senderRefNo", model.getSenderRefNo());
		inputs.put("benRefNo", model.getBenRefNo());
		
		inputs.put("chargeTo", model.getChargeTo() !=null ? model.getChargeTo() : "OUR"); //hardcoded for mobile testing (sementara)
		inputs.put("channelOnline", model.getBenDomesticBankCode().getChannel());
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("inputs : " + inputs);
			logger.debug("\n\n\n\n\n");
		}
		
		return inputs;
		
	}
	
	/*private void notifyCustomer(CustomerDomesticTransferModel model, String appCode) {
		try {
			CustomerModel custModel = model.getCustomer();
			String email1 = custModel.getEmail1();
			String email2 = custModel.getEmail2();
			
			List<String> emails = new ArrayList<>();
			if (ValueUtils.hasValue(email1))
				emails.add(email1);
			
			if (ValueUtils.hasValue(email2))
				emails.add(email2);
			
			if (emails.size()>0) {
				Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
				inputs.put("emails", emails);
				inputs.put("subject", "notify customer");
				
				eaiAdapter.invokeService(EAIConstants.CORPORATE_TRANSFER_NOTIFICATION, inputs);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}*/
	
	private void notifyTrxUsersHistory(CustomerDomesticTransferModel model, String appCode) {
		try {
			CustomerModel customer = customerUtilsRepo.isCustomerValid(model.getCreatedBy());
			
			List<String> emails = new ArrayList<>();
			if(ApplicationConstants.YES.equals(customer.getIsNotifyMyTrx()) && customer.getEmail1() != null) {
				emails.add(customer.getEmail1());
				
				IDMMenuModel menu = idmRepo.isIDMMenuValid(CustomerDomesticTransferSC.menuCode);
				
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
	
	private void notifyBeneficiary(CustomerDomesticTransferModel model, String appCode) {		
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
				
				List<CustomerDomesticTransferModel> domesticTransferList = domesticTransferRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_FUTURE_DATE, new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(domesticTransferList);
				
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void executeRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException {
		try{
			Timestamp recurringDate = DateUtils.getCurrentTimestamp();
			String[] parameterArr = parameter.split("\\|");
			
			int sessionTimeValue = Integer.valueOf(parameterArr[0]);
			
			if(parameterArr.length > 1) {
				String[] recurringDateArr = parameterArr[1].split("\\=");
				
				if(ValueUtils.hasValue(recurringDateArr[1])){
					recurringDate = new Timestamp(Helper.DATE_TIME_FORMATTER.parse(recurringDateArr[1]).getTime());
				}
			}
			
			
			Calendar calFrom = DateUtils.getEarliestDate(recurringDate);
			
			List<String> sessionTimeList = sysParamService.getTransactionSessionTime();
			
			if(sessionTimeValue > 0 && sessionTimeValue <= sessionTimeList.size()) {
			
				Calendar calTo = DateUtils.getNextSessionTime(recurringDate, sessionTimeValue, sessionTimeList);

				if(logger.isDebugEnabled()) {
					logger.debug("Recurring start : " + new Timestamp(calFrom.getTimeInMillis()));
					logger.debug("Recurring end : " + new Timestamp(calTo.getTimeInMillis()));
				}
				
				List<CustomerDomesticTransferModel> domesticTransferList = domesticTransferRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_RECURRING, 
						new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(domesticTransferList);
				
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void executeAllBatch(List<CustomerDomesticTransferModel> list) throws Exception {
		List<SpringBeanInvokerData<String>> jobs = new ArrayList<>();
		for(CustomerDomesticTransferModel model : list) {
			jobs.add(new SpringBeanInvokerData<>(model.getId(), "CustomerDomesticTransferService", "executeBatchTransaction", new Object[] {model}, jobTimeout));
		}

		SpringBeanInvokerHelper.invokeAndForget(invoker, jobs, invokerTimeout, concurrencies);		
	}
		
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void executeBatchTransaction(CustomerDomesticTransferModel domestic) {
		if(logger.isDebugEnabled())
			logger.debug("Process recurring domestic : " + domestic.getId() + " - " + domestic.getReferenceNo());
		
		boolean success = false;
		String errorCode = null;
		Timestamp activityDate = DateUtils.getCurrentTimestamp();
		try {
			domestic.setIsProcessed(ApplicationConstants.YES);
			domesticTransferRepo.save(domestic);

			//avoid lazy when get details
			domestic = domesticTransferRepo.findOne(domestic.getId());
			
			doTransfer(domestic, ApplicationConstants.APP_GPCASHBO);
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
				if(ApplicationConstants.SI_RECURRING.equals(domestic.getInstructionMode())) {					
					Timestamp newInstructionDate = InstructionModeUtils.getStandingInstructionDate(domestic.getRecurringParamType(), domestic.getRecurringParam(), domestic.getInstructionDate());
					
					Calendar calNewInstructionDate = DateUtils.getEarliestDate(newInstructionDate);
					Calendar calExpired = DateUtils.getEarliestDate(domestic.getRecurringEndDate());
					
					if(logger.isDebugEnabled()) {
						logger.debug("Recurring new instruction date : " + calNewInstructionDate.getTime());
						logger.debug("Recurring expired date : " + calExpired.getTime());
					}
					
					if(calNewInstructionDate.compareTo(calExpired) <= 0) {
						Calendar oldInstructionDate = Calendar.getInstance();
						oldInstructionDate.setTime(domestic.getInstructionDate());
						
						//get session time from old recurring
						calNewInstructionDate.set(Calendar.HOUR_OF_DAY, oldInstructionDate.get(Calendar.HOUR_OF_DAY));
						calNewInstructionDate.set(Calendar.MINUTE, oldInstructionDate.get(Calendar.MINUTE));
						saveNewTransactionForRecurring(domestic, new Timestamp(calNewInstructionDate.getTimeInMillis()));
					} else {
						isExpired = true;
					}
					
				}
			} catch(Exception e) {
				logger.error("Failed domestic id : " + domestic.getId());
			}
			
			if (success) {
				if (ApplicationConstants.YES.equals(domestic.getIsError())) {
					pendingTaskRepo.updatePendingTask(domestic.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.EXECUTE_FAIL);
				
					trxStatusService.addTransactionStatus(domestic.getPendingTaskId(),  activityDate, CustomerTransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.EXECUTE_FAIL, domestic.getId(), true, domestic.getErrorCode());
				} else {
					pendingTaskRepo.updatePendingTask(domestic.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.EXECUTE_SUCCESS);
					
					trxStatusService.addTransactionStatus(domestic.getPendingTaskId(),  activityDate, CustomerTransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.EXECUTE_SUCCESS, domestic.getId(), false, null);
				}
			} else {
				pendingTaskRepo.updatePendingTask(domestic.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.EXECUTE_FAIL);
				
				trxStatusService.addTransactionStatus(domestic.getPendingTaskId(),  activityDate, CustomerTransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.EXECUTE_FAIL, domestic.getId(), true, errorCode);
			}
			
			if (isExpired) {
				pendingTaskRepo.updatePendingTask(domestic.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.EXPIRED);
				
				//insert into trx status EXPIRED
				trxStatusService.addTransactionStatus(domestic.getPendingTaskId(), DateUtils.getCurrentTimestamp(), CustomerTransactionActivityType.EXECUTE, ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.EXPIRED, null, true, null);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void saveNewTransactionForRecurring(CustomerDomesticTransferModel model, Timestamp newInstructionDate) {
		try {
			CustomerDomesticTransferModel newModel = new CustomerDomesticTransferModel();
			newModel.setId(Helper.generateHibernateUUIDGenerator());
			newModel.setReferenceNo(model.getReferenceNo());
			newModel.setSenderRefNo(model.getSenderRefNo());
			newModel.setBenRefNo(model.getBenRefNo());
			newModel.setMenu(model.getMenu());
			newModel.setCustomer(model.getCustomer());
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
			Map<String, Object> chargesInfo =  customerChargeService.getCustomerChargesEquivalent(model.getApplication().getCode(),
					model.getService().getCode(), model.getCustomer().getId(), model.getTransactionCurrency());
			
			BigDecimal totalChargeFromTable = new BigDecimal((String) chargesInfo.get(ApplicationConstants.TRANS_TOTAL_CHARGE_EQ));
			BigDecimal totalTransaction = model.getTotalDebitedEquivalentAmount().add(totalChargeFromTable);
			
			newModel.setTotalChargeEquivalentAmount(totalChargeFromTable);
			newModel.setTotalDebitedEquivalentAmount(totalTransaction);
			
			setCharge(newModel, (List<Map<String, Object>>)chargesInfo.get(ApplicationConstants.TRANS_CHARGE_LIST));
			
			newModel.setService(model.getService());
			newModel.setSenderRefNo(model.getSenderRefNo());
			newModel.setIsFinalPayment(model.getIsFinalPayment());
			newModel.setBenRefNo(model.getBenRefNo());
			newModel.setRemark1(model.getRemark1());
			newModel.setRemark2(model.getRemark2());
			newModel.setRemark3(model.getRemark3());
			newModel.setIsNotifyBen(model.getIsNotifyBen());
			newModel.setNotifyBenValue(model.getNotifyBenValue());
			
			//set ben info
			newModel.setBenAccountNo(model.getBenAccountNo());
			newModel.setBenAccountName(model.getBenAccountName());
			newModel.setBenAccountCurrency(model.getBenAccountCurrency());
			newModel.setBenAliasName(model.getBenAliasName());
			newModel.setBenAddr1(model.getBenAddr1());
			newModel.setBenAddr2(model.getBenAddr2());
			newModel.setBenAddr3(model.getBenAddr3());
			newModel.setLldIsBenResidence(model.getLldIsBenResidence());
			newModel.setLldBenResidenceCountry(model.getLldBenResidenceCountry());
			newModel.setLldIsBenCitizen(model.getLldIsBenCitizen());
			newModel.setLldBenCitizenCountry(model.getLldBenCitizenCountry());
			newModel.setBenDomesticBankCode(model.getBenDomesticBankCode());		
			newModel.setBenType(model.getBenType());

			//set new instructionDate for recurring
			newModel.setInstructionMode(model.getInstructionMode());
			newModel.setRecurringParamType(model.getRecurringParamType());
			newModel.setRecurringParam(model.getRecurringParam());
			newModel.setRecurringStartDate(model.getRecurringStartDate());
			newModel.setRecurringEndDate(model.getRecurringEndDate());
			newModel.setInstructionDate(newInstructionDate);
			
			saveDomestic(newModel, ApplicationConstants.CREATED_BY_SYSTEM, ApplicationConstants.NO);

	                //insert into trx status PENDING_EXECUTE with new model id
			Timestamp now = DateUtils.getCurrentTimestamp();
			pendingTaskRepo.updatePendingTask(makerPendingTaskIdForRecurring, ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.PENDING_EXECUTE);
			
			trxStatusService.addTransactionStatus(makerPendingTaskIdForRecurring, now, CustomerTransactionActivityType.EXECUTE, ApplicationConstants.CREATED_BY_SYSTEM, CustomerTransactionStatus.PENDING_EXECUTE, null, false, null);
		} catch(Exception e) {
			// ignore any error to avoid transaction rollback caused by saving new transaction
			logger.error("Failed save domestic recurring with id : " + model.getId());
		}
	}
	
	@Override
	public Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException {

		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			String transactionStatusId = (String) map.get("executedId");
			CustomerTransactionStatusModel trxStatus = trxStatusRepo.findOne(transactionStatusId);
			
			//jika success ambil ke model nya, jika tidak maka ambil dr pending task krn blm masuk table
			if(trxStatus.getStatus().equals(CustomerTransactionStatus.EXECUTE_SUCCESS)) {
				CustomerDomesticTransferModel model = domesticTransferRepo.findOne(trxStatus.getEaiRefNo());
				resultMap.put("executedResult", prepareDetailTransactionMap(model, trxStatus));
			} else {
				CustomerUserPendingTaskModel model = pendingTaskRepo.findOne(trxStatus.getEaiRefNo());
				resultMap.put("executedResult", globalTransactionService.prepareDetailTransactionMapFromPendingTask(model, trxStatus));
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private List<Map<String, Object>> prepareDetailTransactionMap(CustomerDomesticTransferModel model, CustomerTransactionStatusModel trxStatus) throws Exception {
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
		modelMap.put("creditAccount", ValueUtils.getValue(model.getBenAccountNo()));
		modelMap.put("creditAccountName", ValueUtils.getValue(model.getBenAccountName()));
		modelMap.put("creditAccountCurrency", ValueUtils.getValue(model.getBenAccountCurrency()));
		modelMap.put("senderRefNo", ValueUtils.getValue(model.getSenderRefNo()));
		modelMap.put("benRefNo", ValueUtils.getValue(model.getBenRefNo()));
		
		//TODO diganti jika telah implement rate
		modelMap.put("debitTransactionCurrency", sourceAccount.getCurrency().getCode());
		modelMap.put("debitEquivalentAmount", model.getTotalDebitedEquivalentAmount());
		modelMap.put("debitExchangeRate", ApplicationConstants.ZERO);
		modelMap.put("creditTransactionCurrency", model.getTransactionCurrency());
		modelMap.put("creditEquivalentAmount", model.getTransactionAmount());
		modelMap.put("creditExchangeRate", ApplicationConstants.ZERO);
		//--------------------------------------------
		
		//TODO diganti jika telah implement periodical charges
		modelMap.put("chargeAccount", ApplicationConstants.EMPTY_STRING);
		
		LinkedList<Map<String, Object>> chargeList = new LinkedList<>();
		
		Map<String, Object> chargeMap = null;
		
		/*if(model.getChargeType1() != null) {
			chargeMap = new HashMap<>();
			CustomerChargeModel customerCharge = customerChargeRepo.findOne(model.getChargeType1());
			ServiceChargeModel serviceCharge = customerCharge.getServiceCharge();
			chargeMap.put("chargeType", serviceCharge.getName());
			chargeMap.put("chargeCurrency", model.getChargeTypeCurrency1());
			chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount1());
			chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
			chargeList.add(chargeMap);
		}
		
		if(model.getChargeType2() != null) {
			chargeMap = new HashMap<>();
			CustomerChargeModel customerCharge = customerChargeRepo.findOne(model.getChargeType2());
			ServiceChargeModel serviceCharge = customerCharge.getServiceCharge();
			chargeMap.put("chargeType", serviceCharge.getName());
			chargeMap.put("chargeCurrency", model.getChargeTypeCurrency2());
			chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount2());
			chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
			chargeList.add(chargeMap);
		}
		
		if(model.getChargeType3() != null) {
			chargeMap = new HashMap<>();
			CustomerChargeModel customerCharge = customerChargeRepo.findOne(model.getChargeType3());
			ServiceChargeModel serviceCharge = customerCharge.getServiceCharge();
			chargeMap.put("chargeType", serviceCharge.getName());
			chargeMap.put("chargeCurrency", model.getChargeTypeCurrency3());
			chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount3());
			chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
			chargeList.add(chargeMap);
		}
		
		if(model.getChargeType4() != null) {
			chargeMap = new HashMap<>();
			CustomerChargeModel customerCharge = customerChargeRepo.findOne(model.getChargeType4());
			ServiceChargeModel serviceCharge = customerCharge.getServiceCharge();
			chargeMap.put("chargeType", serviceCharge.getName());
			chargeMap.put("chargeCurrency", model.getChargeTypeCurrency4());
			chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount4());
			chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
			chargeList.add(chargeMap);
		}
		
		if(model.getChargeType5() != null) {
			chargeMap = new HashMap<>();
			CustomerChargeModel customerCharge = customerChargeRepo.findOne(model.getChargeType5());
			ServiceChargeModel serviceCharge = customerCharge.getServiceCharge();
			chargeMap.put("chargeType", serviceCharge.getName());
			chargeMap.put("chargeCurrency", model.getChargeTypeCurrency5());
			chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount5());
			chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
			chargeList.add(chargeMap);
		}*/
		
		//ikut existing corporate
		
				CustomerUserPendingTaskModel pt = pendingTaskRepo.findByReferenceNo(model.getReferenceNo());
				
				String strValues = pt.getValuesStr();
				if (strValues != null) {
					try {
						Map<String, Object> valueMap = (Map<String, Object>) objectMapper.readValue(strValues, Class.forName(pt.getModel()));
						List<Map<String,Object>> listCharge = (List<Map<String,Object>>)valueMap.get("chargeList");
						
						for (int i = 0; i < listCharge.size(); i++) {
							Map<String, Object> mapCharge = listCharge.get(i);
							if (model.getChargeType1() != null && model.getChargeType1().equals(mapCharge.get("id"))) {
								chargeMap = new HashMap<>();
								chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
								chargeMap.put("chargeCurrency", model.getChargeTypeCurrency1());
								chargeMap.put("chargeAmount", model.getChargeTypeAmount1());
								chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmountEquivalent1());
								chargeMap.put("debitCurrency", sourceAccount.getCurrency().getCode());
								chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
								chargeList.add(chargeMap);
								continue;
							}
							if (model.getChargeType2() != null && model.getChargeType2().equals(mapCharge.get("id"))) {
								chargeMap = new HashMap<>();
								chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
								chargeMap.put("chargeAmount", model.getChargeTypeAmount2());
								chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmountEquivalent2());
								chargeMap.put("debitCurrency", sourceAccount.getCurrency().getCode());
								chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
								chargeList.add(chargeMap);
								continue;
							}
							if (model.getChargeType3() != null && model.getChargeType3().equals(mapCharge.get("id"))) {
								chargeMap = new HashMap<>();
								chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
								chargeMap.put("chargeAmount", model.getChargeTypeAmount3());
								chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmountEquivalent3());
								chargeMap.put("debitCurrency", sourceAccount.getCurrency().getCode());
								chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
								chargeList.add(chargeMap);
								continue;
							}
							if (model.getChargeType4() != null && model.getChargeType4().equals(mapCharge.get("id"))) {
								chargeMap = new HashMap<>();
								chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
								chargeMap.put("chargeAmount", model.getChargeTypeAmount4());
								chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmountEquivalent4());
								chargeMap.put("debitCurrency", sourceAccount.getCurrency().getCode());
								chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
								chargeList.add(chargeMap);
								continue;
							}
							if (model.getChargeType5() != null && model.getChargeType5().equals(mapCharge.get("id"))) {
								chargeMap = new HashMap<>();
								chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
								chargeMap.put("chargeAmount", model.getChargeTypeAmount5());
								chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmountEquivalent5());
								chargeMap.put("debitCurrency", sourceAccount.getCurrency().getCode());
								chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
								chargeList.add(chargeMap);
								continue;
							}
							
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					} 
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
	public Map<String, Object> cancelTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException {

		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			//cancelId = pendingTaskId
			String pendingTaskId = (String) map.get("cancelId");
			String loginUserCode = (String) map.get(ApplicationConstants.CUST_ID);
			
			if(logger.isDebugEnabled()) {
				logger.debug("cancel pendingTaskId : " + pendingTaskId);
			}
			
			//update expired to pending task
			pendingTaskRepo.updatePendingTask(pendingTaskId, Status.CANCELED.name(), DateUtils.getCurrentTimestamp(), loginUserCode, CustomerTransactionStatus.CANCELLED);

			//insert into trx status EXPIRE
			trxStatusService.addTransactionStatus(pendingTaskId, DateUtils.getCurrentTimestamp(), CustomerTransactionActivityType.CANCEL, loginUserCode, CustomerTransactionStatus.CANCELLED, null, true, null);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {			
			Locale locale = LocaleContextHolder.getLocale();
			String transactionStatusId = (String) map.get("receiptId");
			CustomerTransactionStatusModel trxStatus = trxStatusRepo.findOne(transactionStatusId);
			
			SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy");
			SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
			
			HashMap<String, Object> reportParams = new HashMap<>();
			reportParams.put("trxDate", sdfDate.format(trxStatus.getActivityDate()));
			reportParams.put("trxTime", sdfTime.format(trxStatus.getActivityDate()));
			reportParams.put("transactionStatus", message.getMessage(trxStatus.getStatus().toString(), null, trxStatus.getStatus().toString(), locale));
			reportParams.put("errorDscp", trxStatus.getErrorCode());
			
			Path bankLogoPath = Paths.get(bankLogo);
			BufferedImage bankLogo = ImageIO.read(bankLogoPath.toFile());
			reportParams.put("bankLogo", bankLogo);
			
			CustomerDomesticTransferModel model = domesticTransferRepo.findOne(trxStatus.getEaiRefNo());
			setParamForDownloadTrxStatus(reportParams, model);
			
			String masterReportFile = reportFolder + File.separator + "CustomerTransactionStatus" + 
					File.separator + "Domestic" + File.separator + "download-transaction-domestic" + "-" + locale.getLanguage() + ".jasper";
			JasperReport masterReport = (JasperReport) JRLoader.loadObject(new File(masterReportFile));
			
			setCopyrightSubReport(reportParams);
			
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
	
	private void setCopyrightSubReport(Map<String, Object> reportParams) throws JRException {
		Locale locale = LocaleContextHolder.getLocale();
		String copyrightFile = reportFolder + File.separator + "Copyright" + File.separator + "bank-copyright" + "-" + locale.getLanguage() + ".jasper";
		JasperReport copyrightReport = (JasperReport) JRLoader.loadObject(new File(copyrightFile));
		reportParams.put("COPYRIGHT_REPORT", copyrightReport);
	}
	
	private void setParamForDownloadTrxStatus(Map<String, Object> reportParams, CustomerDomesticTransferModel model) throws Exception {
		DecimalFormat df = new DecimalFormat(moneyFormat);
		
		if(model != null) {
			DomesticBankModel domesticBank = model.getBenDomesticBankCode();
			reportParams.put("destinationBank", domesticBank.getName());
			reportParams.put("sknCode", ValueUtils.getValue(domesticBank.getOnlineBankCode()));
			reportParams.put("rtgsCode", ValueUtils.getValue(domesticBank.getMemberCode()));
			
			reportParams.put("service", ValueUtils.getValue(model.getService().getDscp()));
			
			AccountModel sourceAccount = model.getSourceAccount();
			reportParams.put("sourceAccount", sourceAccount.getAccountNo());
			reportParams.put("sourceAccountName", sourceAccount.getAccountName());
			
			reportParams.put("benAccount", model.getBenAccountNo());
			reportParams.put("benAccountName", model.getBenAccountName());
			
			reportParams.put("branchCode", domesticBank.getOrganizationUnitCode());
			reportParams.put("branchName", domesticBank.getOrganizationUnitName());
			
			reportParams.put("transactionCurrency", model.getTransactionCurrency());
			reportParams.put("transactionAmount", df.format(model.getTransactionAmount()));
			
			if(model.getChargeTypeAmount1() != null && model.getChargeTypeAmount1().compareTo(BigDecimal.ZERO) > 0) {
				CustomerChargeModel customerCharge = customerChargeRepo.findOne(model.getChargeType1());
				ServiceChargeModel serviceCharge = customerCharge.getServiceCharge();
				reportParams.put("chargeCurrency1", model.getChargeTypeCurrency1());
				reportParams.put("chargeType1", serviceCharge.getName());
				reportParams.put("chargeAmount1", df.format(model.getChargeTypeAmount1()));
			}

			if(model.getChargeTypeAmount2() != null && model.getChargeTypeAmount2().compareTo(BigDecimal.ZERO) > 0) {
				CustomerChargeModel customerCharge = customerChargeRepo.findOne(model.getChargeType2());
				ServiceChargeModel serviceCharge = customerCharge.getServiceCharge();
				reportParams.put("chargeCurrency2", model.getChargeTypeCurrency2());
				reportParams.put("chargeType2", serviceCharge.getName());
				reportParams.put("chargeAmount2", df.format(model.getChargeTypeAmount2()));
			}
			
			if(model.getChargeTypeAmount3() != null && model.getChargeTypeAmount3().compareTo(BigDecimal.ZERO) > 0) {
				CustomerChargeModel customerCharge = customerChargeRepo.findOne(model.getChargeType3());
				ServiceChargeModel serviceCharge = customerCharge.getServiceCharge();
				reportParams.put("chargeCurrency3", model.getChargeTypeCurrency3());
				reportParams.put("chargeType3", serviceCharge.getName());
				reportParams.put("chargeAmount3", df.format(model.getChargeTypeAmount3()));
			}
			
			if(model.getChargeTypeAmount4() != null && model.getChargeTypeAmount4().compareTo(BigDecimal.ZERO) > 0) {
				CustomerChargeModel customerCharge = customerChargeRepo.findOne(model.getChargeType4());
				ServiceChargeModel serviceCharge = customerCharge.getServiceCharge();
				reportParams.put("chargeCurrency4", model.getChargeTypeCurrency4());
				reportParams.put("chargeType4", serviceCharge.getName());
				reportParams.put("chargeAmount4", df.format(model.getChargeTypeAmount4()));
			}
			
			if(model.getChargeTypeAmount5() != null && model.getChargeTypeAmount5().compareTo(BigDecimal.ZERO) > 0) {
				CustomerChargeModel customerCharge = customerChargeRepo.findOne(model.getChargeType5());
				ServiceChargeModel serviceCharge = customerCharge.getServiceCharge();
				reportParams.put("chargeCurrency5", model.getChargeTypeCurrency5());
				reportParams.put("chargeType5", serviceCharge.getName());
				reportParams.put("chargeAmount5", df.format(model.getChargeTypeAmount5()));
			}
			
			if(model.getTotalChargeEquivalentAmount().compareTo(BigDecimal.ZERO) > 0)
				reportParams.put("totalCharge", df.format(model.getTotalChargeEquivalentAmount()));
			
			
			reportParams.put("totalDebited", df.format(model.getTotalDebitedEquivalentAmount()));
			
			reportParams.put("remark1", ValueUtils.getValue(model.getRemark1()));
			reportParams.put("refNo", model.getReferenceNo());
		}
	}

	@Override
	public void executeOnlineFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException {
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
					logger.debug("Future Online start : " + new Timestamp(calFrom.getTimeInMillis()));
					logger.debug("Future Online end : " + new Timestamp(calTo.getTimeInMillis()));
				}
				
				List<CustomerDomesticTransferModel> domesticTransferList = domesticTransferRepo.findOnlineTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_FUTURE_DATE, new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(domesticTransferList);
				
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
	}

	@Override
	public void executeOnlineRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException {
		try{
			Timestamp recurringDate = DateUtils.getCurrentTimestamp();
			String[] parameterArr = parameter.split("\\|");
			
			int sessionTimeValue = Integer.valueOf(parameterArr[0]);
			
			if(parameterArr.length > 1) {
				String[] recurringDateArr = parameterArr[1].split("\\=");
				
				if(ValueUtils.hasValue(recurringDateArr[1])){
					recurringDate = new Timestamp(Helper.DATE_TIME_FORMATTER.parse(recurringDateArr[1]).getTime());
				}
			}
			
			
			Calendar calFrom = DateUtils.getEarliestDate(recurringDate);
			
			List<String> sessionTimeList = sysParamService.getTransactionSessionTime();
			
			if(sessionTimeValue > 0 && sessionTimeValue <= sessionTimeList.size()) {
				
				Calendar calTo = DateUtils.getNextSessionTime(recurringDate, sessionTimeValue, sessionTimeList);
				
				if(logger.isDebugEnabled()) {
					logger.debug("Recurring Online start : " + new Timestamp(calFrom.getTimeInMillis()));
					logger.debug("Recurring Online end : " + new Timestamp(calTo.getTimeInMillis()));
				}
				
				List<CustomerDomesticTransferModel> domesticTransferList = domesticTransferRepo.findOnlineTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_RECURRING, 
						new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(domesticTransferList);
				
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
	}
}
