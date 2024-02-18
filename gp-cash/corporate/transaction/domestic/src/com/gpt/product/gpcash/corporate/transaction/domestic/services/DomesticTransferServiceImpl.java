package com.gpt.product.gpcash.corporate.transaction.domestic.services;

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

import com.gpt.component.calendar.services.CalendarService;
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
import com.gpt.platform.cash.workflow.CorporateWFEngine;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListDomesticModel;
import com.gpt.product.gpcash.corporate.beneficiarylist.repository.BeneficiaryListDomesticRepository;
import com.gpt.product.gpcash.corporate.beneficiarylist.services.BeneficiaryListDomesticService;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporatecharge.services.CorporateChargeService;
import com.gpt.product.gpcash.corporate.corporatespecialrate.services.SpecialRateService;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.repository.CorporateUserPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.domestic.model.DomesticTransferModel;
import com.gpt.product.gpcash.corporate.transaction.domestic.repository.DomesticTransferRepository;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.services.GlobalTransactionService;
import com.gpt.product.gpcash.corporate.transaction.utils.InstructionModeUtils;
import com.gpt.product.gpcash.corporate.transaction.validation.services.TransactionValidationService;
import com.gpt.product.gpcash.corporate.transactionholidayupdate.model.HolidayTransactionModel;
import com.gpt.product.gpcash.corporate.transactionholidayupdate.repository.HolidayTransactionRepository;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionActivityType;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.product.gpcash.corporate.transactionstatus.model.TransactionStatusModel;
import com.gpt.product.gpcash.corporate.transactionstatus.repository.TransactionStatusRepository;
import com.gpt.product.gpcash.corporate.transactionstatus.services.TransactionStatusService;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;
import com.gpt.product.gpcash.service.model.ServiceModel;
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
public class DomesticTransferServiceImpl implements DomesticTransferService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.batch.transations.timeout-invoker}")
	private int invokerTimeout;
	
	@Value("${gpcash.batch.transations.timeout-job}")
	private int jobTimeout;

	@Value("${gpcash.batch.transations.concurrencies}")
	private int concurrencies;
	
	@Autowired
	private DomesticTransferRepository domesticTransferRepo;

	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;

	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;

	@Autowired
	private CorporateChargeService corporateChargeService;

	@Autowired
	private BeneficiaryListDomesticService beneficiaryListDomesticService;

	@Autowired
	private TransactionValidationService transactionValidationService;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private MaintenanceRepository maitenanceRepo;	
	
	@Autowired
	private ErrorMappingRepository errorMappingRepo;
	
	@Autowired
	private HolidayTransactionRepository holidayTrxRepo;
	
	@Autowired
	private SysParamService sysParamService;
	
	@Autowired
	private EAIEngine eaiAdapter;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
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
	private BeneficiaryListDomesticRepository beneficiaryListDomecticRepo;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private CorporateWFEngine wfEngine;
	
	@Autowired
	private SpecialRateService specialRateService;
	
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
	
	@Autowired
	private CalendarService calendarService;
	
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
			
			// if isSaveBenFlag = Y then validate if accountNo already in
			// already exist or not
			if (ApplicationConstants.YES.equals(map.get("isSaveBenFlag"))) {
				BeneficiaryListDomesticModel beneModel = beneficiaryListDomesticService.getExistingRecord((String) map.get(ApplicationConstants.TRANS_BEN_ID), corporateId, false);
				if(beneModel != null) {
					throw new BusinessException("GPT-0100099");
				}
			}
			
			// if isBeneficiaryFlag = Y then validate if accountNo must exist in table
			if (ApplicationConstants.YES.equals(map.get("isBeneficiaryFlag"))) {
				BeneficiaryListDomesticModel beneModel = beneficiaryListDomecticRepo.findOne((String) map.get(ApplicationConstants.TRANS_BEN_ID));
				if(beneModel == null) {
					throw new BusinessException("GPT-0100100");
				}
			}
			
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
	
	private void validateFinalPayment(String corporateId, String senderRefNo) throws BusinessException {
		pendingTaskService.validateFinalPayment(senderRefNo, DomesticTransferSC.menuCode, corporateId);
		
		List<DomesticTransferModel> modelList = domesticTransferRepo.findTransactionForFinalPaymentFlag(corporateId, 
				ApplicationConstants.YES, senderRefNo);
		
		if(modelList.size() > 0) {
			throw new BusinessException("GPT-0100199");
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

	private CorporateUserPendingTaskVO setCorporateUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		CorporateUserModel maker = corporateUtilsRepo.isCorporateUserValid(createdBy);
		String userGroupId = maker.getCorporateUserGroup().getId();
		String isFinalPayment = (String) map.get("isFinalPayment");

		String accountGroupDetailId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
		BigDecimal transactionAmount = (BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT);
		String benId = (String) map.get(ApplicationConstants.TRANS_BEN_ID);
		String uniqueKey = createdBy.concat(ApplicationConstants.DELIMITER_PIPE).concat(accountGroupDetailId)
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(transactionAmount.toPlainString())
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(benId)
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(String.valueOf((DateUtils.getCurrentTimestamp().getTime())));

		String instructionMode = (String) map.get(ApplicationConstants.INSTRUCTION_MODE);
		String sessionTime = (String) map.get("sessionTime");
		Timestamp instructionDate = (Timestamp) map.get(ApplicationConstants.INSTRUCTION_DATE);
		
		String refNoSpecialRate = (String) map.get("treasuryCode");

		
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		vo.setUniqueKey(uniqueKey);
		vo.setCreatedBy(createdBy);
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setTransactionServiceCode((String) map.get(ApplicationConstants.TRANS_SERVICE_CODE));
		vo.setService("DomesticTransferSC");
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
			BeneficiaryListDomesticModel beneDomestic=  corporateUtilsRepo.getBeneficiaryListDomesticRepo().findOne(benId);
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
		
		vo.setRefNoSpecialRate(refNoSpecialRate);
		return vo;
	}

	@SuppressWarnings("unchecked")
	private DomesticTransferModel setMapToModel(DomesticTransferModel domesticTransfer, Map<String, Object> map,
			boolean isNew, CorporateUserPendingTaskVO vo) throws Exception {
		CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(vo.getCreatedBy());
		CorporateUserGroupModel corporateUserGroup = corporateUser.getCorporateUserGroup();

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
		
		String benRefNo = vo.getBenRefNo();
		domesticTransfer.setBenRefNo(benRefNo);
		
		if (transactionServiceCode.equals(ApplicationConstants.SRVC_GPT_FTR_DOM_ONLINE)) {
			if (benRefNo !=null && benRefNo.length() >= 16 ) {
			domesticTransfer.setBenRefNo(benRefNo.substring(0, 16));
			}	
		}

		IDMMenuModel menu = new IDMMenuModel();
		menu.setCode(vo.getMenuCode());
		domesticTransfer.setMenu(menu);
		
		CorporateModel corpModel = corporateUtilsRepo.isCorporateValid(vo.getCorporateId());
		domesticTransfer.setCorporate(corpModel);
		
		domesticTransfer.setCorporateUserGroup(corporateUserGroup);
		
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
				vo.getCorporateId(), corporateUserGroup.getId(), benAliasName, address1, address2, address3, isBenResident,
				benResidentCountryCode, isBenCitizen, benCitizenCountryCode, beneficiaryTypeCode, bankCode, isBeneficiaryFlag, vo.getCreatedBy());

		// set additional information
		domesticTransfer.setSenderRefNo((String) map.get("senderRefNo"));
		domesticTransfer.setIsFinalPayment((String) map.get("isFinalPayment"));
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
	
	private void setCharge(DomesticTransferModel domesticTransfer, List<Map<String, Object>> chargeList){
		for(int i=1; i <= chargeList.size(); i++){
			Map<String, Object> chargeMap = chargeList.get(i - 1);
			String chargeId = (String) chargeMap.get("id");
			BigDecimal value = new BigDecimal(chargeMap.get("value").toString());
			String currencyCode = (String) chargeMap.get("currencyCode");
			
			if(i == 1){
				domesticTransfer.setChargeType1(chargeId);
				domesticTransfer.setChargeTypeAmount1(value);
				domesticTransfer.setChargeTypeCurrency1(currencyCode);
			} else if(i == 2){
				domesticTransfer.setChargeType2(chargeId);
				domesticTransfer.setChargeTypeAmount2(value);
				domesticTransfer.setChargeTypeCurrency2(currencyCode);
			} else if(i == 3){
				domesticTransfer.setChargeType3(chargeId);
				domesticTransfer.setChargeTypeAmount3(value);
				domesticTransfer.setChargeTypeCurrency3(currencyCode);
			} else if(i == 4){
				domesticTransfer.setChargeType4(chargeId);
				domesticTransfer.setChargeTypeAmount4(value);
				domesticTransfer.setChargeTypeCurrency4(currencyCode);
			} else if(i == 5){
				domesticTransfer.setChargeType5(chargeId);
				domesticTransfer.setChargeTypeAmount5(value);
				domesticTransfer.setChargeTypeCurrency5(currencyCode);
			} 
		}
	}

	private void setInstructionMode(DomesticTransferModel domesticTransfer, String instructionMode, Timestamp instructionDate, 
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
			domesticTransfer.setRecurringEndDate((Timestamp) Helper.parseStringToTimestamp((String) map.get("recurringEndDate")));
			domesticTransfer.setInstructionDate(instructionDate);
		}
	}

	private void setBenAccountInfo(DomesticTransferModel domesticTransfer, String benId, String benAccountName, String benAccountCurrency,
			String transactionServiceCode, String isSaveBenFlag, String corporateId, String corporateUserGroupId, String benAliasName,
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
			BeneficiaryListDomesticModel beneficiaryListDomestic = corporateUtilsRepo.isBeneficiaryListDomesticValid(benId);

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
				beneficiaryListDomesticService.saveBeneficiary(corporateId, corporateUserGroupId, benAccountNo,
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
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {

		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			if(logger.isDebugEnabled()) {
				logger.debug("map : " + map);
			}

			if("N".equals(vo.getIsCheckCOT())){
				SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				vo.setSessionTime("07:00");
				vo.setInstructionMode(ApplicationConstants.SI_FUTURE_DATE);
				Timestamp nextDate = getNextWorkingDayFromTimestamp(DateUtils.getCurrentTimestamp());
				vo.setInstructionDate(DateUtils.getInstructionDateBySessionTime(vo.getSessionTime(), nextDate));
				map.put("sessionTime", vo.getSessionTime());
				map.put("instructionMode", vo.getInstructionMode());
				map.put("instructionDate", sdfDate.format(vo.getInstructionDate()));
				vo.setJsonObject(map);
			}

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				DomesticTransferModel domesticTransfer = new DomesticTransferModel();
				
				checkTransactionThreshold(vo.getTransactionAmount(), vo.getTransactionServiceCode());
				
				setMapToModel(domesticTransfer, map, true, vo);
				
				if(ApplicationConstants.SI_IMMEDIATE.equals(domesticTransfer.getInstructionMode())) {
					saveDomestic(domesticTransfer, vo.getCreatedBy(), ApplicationConstants.YES);

					doTransfer(domesticTransfer, (String)map.get(ApplicationConstants.APP_CODE));
					vo.setErrorCode(domesticTransfer.getErrorCode());
					vo.setIsError(domesticTransfer.getIsError());
				} else {
					saveDomestic(domesticTransfer, vo.getCreatedBy(), ApplicationConstants.NO);
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

	private void saveDomestic(DomesticTransferModel domesticTransfer, String createdBy, String isProcessed)
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
			String serviceCode = (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE);
			String benId = (String)map.get("benId");
			String bankCode = (String) map.get("bankCode");
			String benAccountNo = null;
			String benAccountName = null;
			
			DomesticBankModel domesticBank = maintenanceRepo.isDomesticBankValid(bankCode);
			
			if (ApplicationConstants.YES.equals((String) map.get("isBeneficiaryFlag"))) {
				BeneficiaryListDomesticModel beneDomestic=  corporateUtilsRepo.getBeneficiaryListDomesticRepo().findOne(benId);
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
					
//					if (serviceCode.equals(ApplicationConstants.SRVC_GPT_FTR_DOM_RTGS)) {
						String beneType = ValueUtils.getValue((String) map.get("beneficiaryTypeCode"));
						if (beneType.equals("")) {
							throw new BusinessException("GPT-0100238");
					}
//					}
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
				BeneficiaryListDomesticModel beneficiary = beneficiaryListDomesticService.findBeneficiary(benId);
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
			resultMap.putAll(corporateChargeService.getCorporateChargesEquivalent((String) map.get(ApplicationConstants.APP_CODE),
					(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE),
					(String) map.get(ApplicationConstants.LOGIN_CORP_ID),(String) map.get("sourceAccountCurrency")));
			
			resultMap.put(ApplicationConstants.TRANS_AMOUNT_EQ, map.get("equivalentAmount"));
			
			if (ApplicationConstants.BENEFICIARY_CHARGES_INSTRUCTION.equals(map.get("chargeTo"))) {
				BigDecimal totalCharge = new BigDecimal((String) resultMap.get(ApplicationConstants.TRANS_TOTAL_CHARGE));
				BigDecimal trxAmt = (BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT);
				if (totalCharge.compareTo(trxAmt) > 0) {
					throw new BusinessException("Transaction amount must be greater than or equal to charge amount for Charge to BEN (Beneficiary)");
				}
				
				this.checkTransactionThreshold(trxAmt.subtract(totalCharge), (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE));
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private void doTransfer(DomesticTransferModel model, String appCode) throws Exception {
		boolean isRollback = false;
		boolean limitUpdated = false;
		boolean isTimeOut = false;
		
		try {
			
			//untuk hitung totalcharge dalam IDR(localcurrency) charge salalu IDR
			BigDecimal totalCharge = model.getChargeTypeAmount1()!=null?model.getChargeTypeAmount1():BigDecimal.ZERO
					.add(model.getChargeTypeAmount2()!=null?model.getChargeTypeAmount2():BigDecimal.ZERO)
					.add(model.getChargeTypeAmount3()!=null?model.getChargeTypeAmount3():BigDecimal.ZERO)
					.add(model.getChargeTypeAmount4()!=null?model.getChargeTypeAmount4():BigDecimal.ZERO)
					.add(model.getChargeTypeAmount5()!=null?model.getChargeTypeAmount5():BigDecimal.ZERO);
			
			//update transaction limit
			transactionValidationService.updateTransactionLimitEquivalent(model.getCorporate().getId(), 
					model.getService().getCode(), 
					model.getSourceAccount().getCurrency().getCode(), 
					model.getTransactionCurrency(), 
					model.getCorporateUserGroup().getId(), 
					model.getTotalDebitedEquivalentAmount(),
					model.getApplication().getCode(),
					totalCharge);			
			
			//update bank forex limit
			transactionValidationService.updateBankForexLimit(
					model.getSourceAccount().getCurrency().getCode(),
					model.getTransactionCurrency(),
					model.getTransactionAmount(),
					model.getTotalDebitedEquivalentAmount());
			
			limitUpdated = true;
			
			Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
			Map<String, Object> outputMap = eaiAdapter.invokeService(EAIConstants.DOMESTIC_TRANSFER, inputs);
			
			model.setStatus("GPT-0100130");
			model.setIsError(ApplicationConstants.NO);
			
			this.setHostRefNoAndTrxTime(outputMap, model);
			this.updateSpecialRateStatus(model);
		} catch (BusinessException e) {
			String errorMsg = e.getMessage();
			
			//============ get hostRefNo and trxTime when timeout
			if (e.getErrorArgs() != null) {
			String[] respArr = e.getErrorArgs();
			if (respArr.length > 0) {
					String refNo = respArr[0];
					model.setTrxReferenceNo(refNo);
					model.setHostReferenceNo(refNo);
					if (model.getService().getCode().equals(ApplicationConstants.SRVC_GPT_FTR_DOM_LLG)) {
						model.setHostReferenceNo(refNo.substring(6));					
					}
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				Date localTrxTime = dateFormat.parse(respArr[1]);
				model.setTransactionTime(new Timestamp(localTrxTime.getTime()));
			
					pendingTaskRepo.updateHostRefNoAndRetrievalRefNoPendingTask(model.getPendingTaskId(), model.getHostReferenceNo(), refNo);
			
				}
			}
			//============ get hostRefNo and trxTime when timeout
			
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
			globalTransactionService.save(model.getCorporate().getId(), DomesticTransferSC.menuCode, 
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
	
	private void updateSpecialRateStatus(DomesticTransferModel model) {
		// TODO Auto-generated method stub
		try {
			specialRateService.updateStatus(model.getRefNoSpecialRate(), ApplicationConstants.SPECIAL_RATE_SETTLED, model.getCreatedBy());
		} catch (Exception e) {
			logger.error("ERROR update special ref no"+ e.getMessage());
		}
	}

	private Map<String, Object> prepareInputsForEAI(DomesticTransferModel model, String appCode) throws Exception {
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
		
		CorporateModel corpModel = model.getCorporate();
		inputs.put("remitterAddress1", corpModel.getAddress1());
		inputs.put("remitterAddress2", corpModel.getAddress2());
		inputs.put("remitterAddress3", corpModel.getAddress3());
		inputs.put("remitterResidentFlag", corpModel.getLldIsResidence());
		inputs.put("remitterResidentCountryCode", corpModel.getResidenceCountry().getCode());
		inputs.put("remitterCitizenFlag", corpModel.getLldIsCitizen());
		inputs.put("remitterCitizenCountryCode", corpModel.getCitizenCountry().getCode());
		inputs.put("remitterType", corpModel.getLldCategory());
				
		inputs.put("destinationBankCode", model.getBenDomesticBankCode().getCode());
		inputs.put("destinationBankName", model.getBenDomesticBankCode().getName());
		inputs.put("destinationMemberBankCode", model.getBenDomesticBankCode().getMemberCode());
		inputs.put("destinationOnlineBankCode", model.getBenDomesticBankCode().getOnlineBankCode());
		
		inputs.put("trxCurrencyCode", model.getTransactionCurrency());
		inputs.put("trxAmount", model.getTransactionAmount());
		inputs.put("totalDebitAmount", model.getTotalDebitedEquivalentAmount());
		inputs.put("totalChargeEquivalent", model.getTotalChargeEquivalentAmount());
		
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
		inputs.put("benRefNo", model.getBenRefNo() !=null ? model.getBenRefNo() : "");
		
		inputs.put("chargeTo", model.getChargeTo());
		inputs.put("channelOnline", model.getBenDomesticBankCode().getChannel());
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("inputs : " + inputs);
			logger.debug("\n\n\n\n\n");
		}
		
		return inputs;
		
	}
	
	/*private void notifyCorporate(DomesticTransferModel model, String appCode) {
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
	
	private void notifyTrxUsersHistory(DomesticTransferModel model, String appCode) {
		try {
			List<String> emails = pendingTaskService.getCorporateUserEmailWithNotifyTrx(model.getId());
			if (emails.size() > 0) {
				
				IDMMenuModel menu = idmRepo.isIDMMenuValid(DomesticTransferSC.menuCode);
				
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
	
	private void notifyUpdateInstructionDate(DomesticTransferModel model, String appCode) {
		try {
			List<String> emails = pendingTaskService.getCorporateUserEmailWithNotifyTrx(model.getId());
			if (emails.size() > 0) {
				
				IDMMenuModel menu = idmRepo.isIDMMenuValid(DomesticTransferSC.menuCode);
				
				Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
				inputs.put("instructionMode", model.getInstructionMode());
				inputs.put("instructionDate", model.getInstructionDate());
				inputs.put("status", model.getStatus());
				inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
				inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
				inputs.put("emails", emails);
				inputs.put("subject", menu.getName().concat(" Update Instruction Date Notification"));
				
				eaiAdapter.invokeService(EAIConstants.UPDATE_INS_DATE_NOTIFICATION, inputs);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}	
	
	private void notifyBeneficiary(DomesticTransferModel model, String appCode) {		
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
				
				List<DomesticTransferModel> domesticTransferList = domesticTransferRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_FUTURE_DATE, new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				if(calendarService.isHoliday(new Timestamp(calFrom.getTimeInMillis()))){
					for (DomesticTransferModel domesticTransferModel : domesticTransferList) {
						Map inputMap = new HashMap();
						inputMap.put("pendingTaskId", domesticTransferModel.getPendingTaskId());
						inputMap.put("holidayTrxId", domesticTransferModel.getPendingTaskId());
						inputMap.put("updateType", "NEXT WORKING DAY");
						updateHolidayTransaction(inputMap);
					}
				}else {
				
				executeAllBatch(domesticTransferList);
				}
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
				
				List<DomesticTransferModel> domesticTransferList = domesticTransferRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_RECURRING, 
						new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(domesticTransferList);
				
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void executeAllBatch(List<DomesticTransferModel> list) throws Exception {
		List<SpringBeanInvokerData<String>> jobs = new ArrayList<>();
		for(DomesticTransferModel model : list) {
			jobs.add(new SpringBeanInvokerData<>(model.getId(), "DomesticTransferService", "executeBatchTransaction", new Object[] {model}, jobTimeout));
		}

		SpringBeanInvokerHelper.invokeAndForget(invoker, jobs, invokerTimeout, concurrencies);		
	}
		
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void executeBatchTransaction(DomesticTransferModel domestic) {
		if(logger.isDebugEnabled())
			logger.debug("Process recurring domestic : " + domestic.getId() + " - " + domestic.getReferenceNo());
		
		boolean success = false;
		boolean isTimeOut = false;
		
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
			boolean isRecurring = false;
			try {
				if(ApplicationConstants.SI_RECURRING.equals(domestic.getInstructionMode())) {					
					Timestamp newInstructionDate = InstructionModeUtils.getStandingInstructionDate(domestic.getRecurringParamType(), domestic.getRecurringParam(), domestic.getInstructionDate());
					
					Calendar calNewInstructionDate = DateUtils.getEarliestDate(newInstructionDate);
					Calendar calExpired = DateUtils.getEarliestDate(domestic.getRecurringEndDate());
					
					if(logger.isDebugEnabled()) {
						logger.debug("Recurring new instruction date : " + calNewInstructionDate.getTime());
						logger.debug("Recurring expired date : " + calExpired.getTime());
					}
					
					isRecurring = true;
					
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
					pendingTaskRepo.updatePendingTask(domestic.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
				
					trxStatusService.addTransactionStatus(domestic.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, domestic.getId(), true, domestic.getErrorCode());
				} else {
					
					if (isRecurring) {
						pendingTaskRepo.updatePendingTask(domestic.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE);						
					} else {
						pendingTaskRepo.updatePendingTask(domestic.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS);
					}
					
					trxStatusService.addTransactionStatus(domestic.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, domestic.getId(), false, null);
				}
			} else {
				
				//ada pengecekan jika error code terdaftar dalam list errorcode timeout, status diupdate jdi inprogress offline, BSG 26 Feb 2020
				try {
					isTimeOut = maintenanceRepo.isTimeOutErrorCodeValid(errorCode);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (isRecurring) {
					pendingTaskRepo.updatePendingTask(domestic.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE);						
					if(!isTimeOut) {
						trxStatusService.addTransactionStatus(domestic.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, domestic.getId(), true, errorCode);
					}else {
						trxStatusService.addTransactionStatus(domestic.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE, domestic.getId(), true, errorCode);
					}
					
				} else {
					if(!isTimeOut) {
					pendingTaskRepo.updatePendingTask(domestic.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);					
						trxStatusService.addTransactionStatus(domestic.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, domestic.getId(), true, errorCode);
					}else{
						pendingTaskRepo.updatePendingTask(domestic.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE);
						trxStatusService.addTransactionStatus(domestic.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE, domestic.getId(), true, errorCode);					
					}
				}
				
			}
			
			if (isExpired) {
				pendingTaskRepo.updatePendingTask(domestic.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXPIRED);
				
				//insert into trx status EXPIRED
				trxStatusService.addTransactionStatus(domestic.getPendingTaskId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXPIRED, null, true, null);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void saveNewTransactionForRecurring(DomesticTransferModel model, Timestamp newInstructionDate) {
		try {
			DomesticTransferModel newModel = new DomesticTransferModel();
			newModel.setId(Helper.generateHibernateUUIDGenerator());
			newModel.setReferenceNo(model.getReferenceNo());
			newModel.setSenderRefNo(model.getSenderRefNo());
			newModel.setBenRefNo(model.getBenRefNo());
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
			newModel.setRemark1(model.getRemark1());
			newModel.setRemark2(model.getRemark2());
			newModel.setRemark3(model.getRemark3());
			newModel.setIsNotifyBen(model.getIsNotifyBen());
			newModel.setNotifyBenValue(model.getNotifyBenValue());
			
			boolean isBenOnline = false;
			
			if (newModel.getService().getCode().equals(ApplicationConstants.SRVC_GPT_FTR_DOM_ONLINE)) {
				isBenOnline = true;
			}
			
			//set ben info
			newModel.setBenAccountNo(model.getBenAccountNo());
			newModel.setBenAccountName(model.getBenAccountName());
			newModel.setBenAccountCurrency(model.getBenAccountCurrency());
			newModel.setBenAliasName(model.getBenAliasName());
			newModel.setBenAddr1(model.getBenAddr1());
			newModel.setBenAddr2(model.getBenAddr2());
			newModel.setBenAddr3(model.getBenAddr3());
			
			if (!isBenOnline) {
			newModel.setLldIsBenResidence(model.getLldIsBenResidence());
			newModel.setLldBenResidenceCountry(model.getLldBenResidenceCountry());
			newModel.setLldIsBenCitizen(model.getLldIsBenCitizen());
			newModel.setLldBenCitizenCountry(model.getLldBenCitizenCountry());
				newModel.setBenType(model.getBenType());
			}
			
			newModel.setBenDomesticBankCode(model.getBenDomesticBankCode());		

			//set new instructionDate for recurring
			newModel.setInstructionMode(model.getInstructionMode());
			newModel.setRecurringParamType(model.getRecurringParamType());
			newModel.setRecurringParam(model.getRecurringParam());
			newModel.setRecurringStartDate(model.getRecurringStartDate());
			newModel.setRecurringEndDate(model.getRecurringEndDate());
			newModel.setInstructionDate(newInstructionDate);
			
			newModel.setChargeTo(model.getChargeTo());
			
			saveDomestic(newModel, ApplicationConstants.CREATED_BY_SYSTEM, ApplicationConstants.NO);

	                //insert into trx status PENDING_EXECUTE with new model id
			Timestamp now = DateUtils.getCurrentTimestamp();
			pendingTaskRepo.updatePendingTask(makerPendingTaskIdForRecurring, ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE);
			
			trxStatusService.addTransactionStatus(makerPendingTaskIdForRecurring, now, TransactionActivityType.EXECUTE, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE, null, false, null);
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
			TransactionStatusModel trxStatus = trxStatusRepo.findOne(transactionStatusId);
			
			//jika success ambil ke model nya, jika tidak maka ambil dr pending task krn blm masuk table
			if(trxStatus.getStatus().equals(TransactionStatus.EXECUTE_SUCCESS)) {
				DomesticTransferModel model = domesticTransferRepo.findOne(trxStatus.getEaiRefNo());
				resultMap.put("executedResult", prepareDetailTransactionMap(model, trxStatus));
			} else {
				CorporateUserPendingTaskModel model = pendingTaskRepo.findOne(trxStatus.getPendingTaskId());
				resultMap.put("executedResult", globalTransactionService.prepareDetailTransactionMapFromPendingTask(model, trxStatus));
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> prepareDetailTransactionMap(DomesticTransferModel model, TransactionStatusModel trxStatus) throws Exception {
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
		modelMap.put("debitTransactionCurrency", model.getSourceAccount().getCurrency().getCode());
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
		CorporateUserPendingTaskModel pt = pendingTaskRepo.findByReferenceNo(model.getReferenceNo());
		
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
						chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount1());
						chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
						chargeList.add(chargeMap);
						continue;
					}
					if (model.getChargeType2() != null && model.getChargeType2().equals(mapCharge.get("id"))) {
						chargeMap = new HashMap<>();
						chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
						chargeMap.put("chargeCurrency", model.getChargeTypeCurrency2());
						chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount2());
						chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
						chargeList.add(chargeMap);
						continue;
					}
					if (model.getChargeType3() != null && model.getChargeType3().equals(mapCharge.get("id"))) {
						chargeMap = new HashMap<>();
						chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
						chargeMap.put("chargeCurrency", model.getChargeTypeCurrency3());
						chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount3());
						chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
						chargeList.add(chargeMap);
						continue;
					}
					if (model.getChargeType4() != null && model.getChargeType4().equals(mapCharge.get("id"))) {
						chargeMap = new HashMap<>();
						chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
						chargeMap.put("chargeCurrency", model.getChargeTypeCurrency4());
						chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount4());
						chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
						chargeList.add(chargeMap);
						continue;
					}
					if (model.getChargeType5() != null && model.getChargeType5().equals(mapCharge.get("id"))) {
						chargeMap = new HashMap<>();
						chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
						chargeMap.put("chargeCurrency", model.getChargeTypeCurrency5());
						chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount5());
						chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
						chargeList.add(chargeMap);
						continue;
					}
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
		/*if(model.getChargeType1() != null) {
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
		}*/
		
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
			String loginUserCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			
			if(logger.isDebugEnabled()) {
				logger.debug("cancel pendingTaskId : " + pendingTaskId);
			}
			
			try {
				
				DomesticTransferModel domesticTransfer = domesticTransferRepo.findByPendingTaskIdAndIsProcessed(pendingTaskId, ApplicationConstants.NO);
				
				domesticTransfer.setIsProcessed(ApplicationConstants.YES);
				domesticTransfer.setUpdatedDate(DateUtils.getCurrentTimestamp());
				domesticTransfer.setUpdatedBy(loginUserCode);
				
				domesticTransferRepo.save(domesticTransfer);
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
	public Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {			
			Locale locale = LocaleContextHolder.getLocale();
			String transactionStatusId = (String) map.get("receiptId");
			TransactionStatusModel trxStatus = trxStatusRepo.findOne(transactionStatusId);
			
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
			
			DomesticTransferModel model = domesticTransferRepo.findOne(trxStatus.getEaiRefNo());
			if(model.getTransactionTime() != null) {
				reportParams.put("trxTime", sdfTime.format(model.getTransactionTime()));
			}
			setParamForDownloadTrxStatus(reportParams, model);
			
			String masterReportFile = reportFolder + File.separator + "TransactionStatus" + 
					File.separator + "Domestic" + File.separator + "download-transaction-domestic" + "-" + locale.getLanguage() + ".jasper";
			JasperReport masterReport = (JasperReport) JRLoader.loadObject(new File(masterReportFile));
			
			setCopyrightSubReport(reportParams);
			
			CorporateUserPendingTaskModel pt = pendingTaskRepo.findOne(model.getPendingTaskId());
			if(ApplicationConstants.YES.equals(pt.getErrorTimeoutFlag())) {
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
	
	@SuppressWarnings("unchecked")
	private void setParamForDownloadTrxStatus(Map<String, Object> reportParams, DomesticTransferModel model) throws Exception {
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
			reportParams.put("retrievalRefNo", model.getTrxReferenceNo());
			
			CorporateUserPendingTaskModel pt = pendingTaskRepo.findByReferenceNo(model.getReferenceNo());
			String strValues = pt.getValuesStr();
			if (strValues != null) {
				try {
					Map<String, Object> valueMap = (Map<String, Object>) objectMapper.readValue(strValues, Class.forName(pt.getModel()));
					List<Map<String,Object>> listCharge = (List<Map<String,Object>>)valueMap.get("chargeList");
					
					for (int i = 0; i < listCharge.size(); i++) {
						Map<String, Object> mapCharge = listCharge.get(i);
						if (model.getChargeType1() != null && model.getChargeType1().equals(mapCharge.get("id")) && model.getChargeTypeAmount1().compareTo(BigDecimal.ZERO) > 0) {
				reportParams.put("chargeCurrency1", model.getChargeTypeCurrency1());
							reportParams.put("chargeType1", mapCharge.get("serviceChargeName"));
				reportParams.put("chargeAmount1", df.format(model.getChargeTypeAmount1()));
							continue;
			}
						if (model.getChargeType2() != null && model.getChargeType2().equals(mapCharge.get("id")) && model.getChargeTypeAmount2().compareTo(BigDecimal.ZERO) > 0) {
				reportParams.put("chargeCurrency2", model.getChargeTypeCurrency2());
							reportParams.put("chargeType2", mapCharge.get("serviceChargeName"));
				reportParams.put("chargeAmount2", df.format(model.getChargeTypeAmount2()));
							continue;
			}
						if (model.getChargeType3() != null && model.getChargeType3().equals(mapCharge.get("id")) && model.getChargeTypeAmount3().compareTo(BigDecimal.ZERO) > 0) {
				reportParams.put("chargeCurrency3", model.getChargeTypeCurrency3());
							reportParams.put("chargeType3", mapCharge.get("serviceChargeName"));
				reportParams.put("chargeAmount3", df.format(model.getChargeTypeAmount3()));
							continue;
			}
						if (model.getChargeType4() != null && model.getChargeType4().equals(mapCharge.get("id")) && model.getChargeTypeAmount4().compareTo(BigDecimal.ZERO) > 0) {
				reportParams.put("chargeCurrency4", model.getChargeTypeCurrency4());
							reportParams.put("chargeType4", mapCharge.get("serviceChargeName"));
				reportParams.put("chargeAmount4", df.format(model.getChargeTypeAmount4()));
							continue;
			}
						if (model.getChargeType5() != null && model.getChargeType5().equals(mapCharge.get("id")) && model.getChargeTypeAmount5().compareTo(BigDecimal.ZERO) > 0) {
				reportParams.put("chargeCurrency5", model.getChargeTypeCurrency5());
							reportParams.put("chargeType5", mapCharge.get("serviceChargeName"));
				reportParams.put("chargeAmount5", df.format(model.getChargeTypeAmount5()));
							continue;
						}
						
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			
			if(model.getTotalChargeEquivalentAmount().compareTo(BigDecimal.ZERO) > 0)
				reportParams.put("totalCharge", df.format(model.getTotalChargeEquivalentAmount()));
			
			reportParams.put("totalDebited", df.format(model.getTotalDebitedEquivalentAmount()));
			
			reportParams.put("remark1", ValueUtils.getValue(model.getRemark1()));
			reportParams.put("refNo", model.getReferenceNo());
			
			if (model.getService().getCode().equals(ApplicationConstants.SRVC_GPT_FTR_DOM_ONLINE)) {
				reportParams.put("custRefNo", model.getBenRefNo());
			}
			
		}
	}

	@Override
	public Map<String, Object> cancelTransactionWF(Map<String, Object> map) throws ApplicationException, BusinessException {
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
	
	private void setHostRefNoAndTrxTime(Map<String, Object> outputMap, DomesticTransferModel model) {
		try {
			if (outputMap != null) {
				String retrievalRefNo = "";
				String hostRefNo = "";
				
				if (outputMap.get("retrievalRefNo") != null) {
					retrievalRefNo = (String) outputMap.get("retrievalRefNo");
					model.setTrxReferenceNo(retrievalRefNo);
				}
				
				if (outputMap.get("hostRefNo") !=null) {
					hostRefNo = (String) outputMap.get("hostRefNo");
					model.setHostReferenceNo(hostRefNo);
				}
				
				if(outputMap.get("localTrxTime") != null) {
					Date localTrxTime = (Date) outputMap.get("localTrxTime");
					model.setTransactionTime(new Timestamp(localTrxTime.getTime()));
				}
				
				pendingTaskRepo.updateHostRefNoAndRetrievalRefNoPendingTask(model.getPendingTaskId(), hostRefNo, retrievalRefNo);
				
			}
		} catch (Exception e) {
			logger.error("ERROR setHostRefNo Domestic Transfer due to"+ e.getMessage());
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
			
			DomesticTransferModel domestic = domesticTransferRepo.findByPendingTaskIdAndIsProcessed(pendingTaskId, ApplicationConstants.YES);
			
			if(status.equals("SUCCESS")) {
				isError = "N";
				pendingTaskRepo.updatePendingTask(domestic.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS);
				trxStatusService.addTransactionStatus(domestic.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, domestic.getId(), false, null);
				
				// send email to beneficiary only if not execute with failure
				if (ApplicationConstants.YES.equals(domestic.getIsNotifyBen())) {
					notifyBeneficiary(domestic, ApplicationConstants.APP_GPCASHBO);
				}
				
			}else {
				pendingTaskRepo.updatePendingTask(domestic.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
				trxStatusService.addTransactionStatus(domestic.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, domestic.getId(), true, domestic.getErrorCode());
			}
			
			pendingTaskRepo.updateErrorTimeoutFlagPendingTask(pendingTaskId, ApplicationConstants.NO);
			
			globalTransactionService.save(domestic.getCorporate().getId(), DomesticTransferSC.menuCode, 
					domestic.getService().getCode(), domestic.getReferenceNo(), 
					domestic.getId(), domestic.getTransactionCurrency(), domestic.getTotalChargeEquivalentAmount(), 
					domestic.getTransactionAmount(), 
					domestic.getTotalDebitedEquivalentAmount(), isError);
			
			notifyTrxUsersHistory(domestic, ApplicationConstants.APP_GPCASHBO);
			
			
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private void notifyBankEmail(DomesticTransferModel model, String appCode) {		
		
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
				
				List<DomesticTransferModel> domesticTransferList = domesticTransferRepo.findOnlineTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_FUTURE_DATE, new Timestamp(calFrom.getTimeInMillis()), 
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
				
				List<DomesticTransferModel> domesticTransferList = domesticTransferRepo.findOnlineTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_RECURRING, 
						new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(domesticTransferList);
				
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> validateCOT(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		String referenceNo = (String) map.get("referenceNo");
		CorporateUserPendingTaskModel model = pendingTaskRepo.findByReferenceNo(referenceNo);

		if(ApplicationConstants.SI_IMMEDIATE.equals(model.getInstructionMode()) && model.getTrxStatus().equals(TransactionStatus.PENDING_RELEASE)){
				transactionValidationService.validateCOT(model.getInstructionMode(), 
						model.getTransactionService().getCode(), 
						model.getTransactionCurrency(),ApplicationConstants.APP_GPCASHIB,
						null, true, false);					
				
				transactionValidationService.validateHoliday(model.getInstructionMode(), 
						DateUtils.getCurrentTimestamp(), 
						"", 1,
						model.getInstructionDate(), model.getInstructionDate(),
						model.getSessionTime(),true,model.getTransactionCurrency());
				
				try {
					pendingTaskRepo.updateCheckCOTFlagPendingTask(model.getId(), "Y");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new ApplicationException(e);
				}
		}
					
		return resultMap;
	}
	
	@Override
	public Map<String, Object> updatecheckCOTFlag(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			String checkCOTFlag = (String) map.get("checkCOTFlag");			
			String referenceNo = (String) map.get("referenceNo");
			CorporateUserPendingTaskModel model = pendingTaskRepo.findByReferenceNo(referenceNo);
			
			pendingTaskRepo.updateCheckCOTFlagPendingTask(model.getId(), checkCOTFlag);		
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> updatecheckCOTFlagList(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<Map<String, Object>> pendingTaskList = (ArrayList<Map<String,Object>>)map.get("pendingTaskList");
			for(Map<String, Object> pendingTaskMap: pendingTaskList){
				String referenceNo = (String) pendingTaskMap.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
				CorporateUserPendingTaskModel model = pendingTaskRepo.findByReferenceNo(referenceNo);
				
				if(ApplicationConstants.SI_IMMEDIATE.equals(model.getInstructionMode()) && 
						model.getTrxStatus().equals(TransactionStatus.PENDING_RELEASE) &&( 
						ApplicationConstants.SRVC_GPT_FTR_DOM_LLG.equals(model.getTransactionService().getCode())||ApplicationConstants.SRVC_GPT_FTR_DOM_RTGS.equals(model.getTransactionService().getCode()))){
					
					pendingTaskRepo.updateCheckCOTFlagPendingTask(model.getId(), "N");	
				}
			}

		} catch (Exception e) {	
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	public Timestamp getNextWorkingDayFromTimestamp(Timestamp timestamp) {
		Timestamp nextDate = getNextDayFromTimestamp(timestamp);
		while(calendarService.isHoliday(nextDate)) {
			nextDate = getNextDayFromTimestamp(nextDate);
		}	
		return nextDate;		
	}
	
	public static Timestamp getNextDayFromTimestamp(Timestamp timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(timestamp.getTime()));
		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date date = cal.getTime();
		return new Timestamp(date.getTime());		
	}
	
	public Timestamp getPrevWorkingDayFromTimestamp(Timestamp timestamp) {
		Timestamp nextDate = getPrevDayFromTimestamp(timestamp);
		while(calendarService.isHoliday(nextDate)) {
			nextDate = getPrevDayFromTimestamp(nextDate);
		}	
		return nextDate;		
	}
	
	public static Timestamp getPrevDayFromTimestamp(Timestamp timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(timestamp.getTime()));
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Date date = cal.getTime();
		return new Timestamp(date.getTime());		
	}
	
	@Override
	public void saveTransactionHoliday(Date holidayDate) throws ApplicationException, BusinessException {
		try{
			Timestamp futureDate = new Timestamp(holidayDate.getTime());
			
			Calendar calFrom = DateUtils.getEarliestDate(futureDate);
			
			List<String> sessionTimeList = sysParamService.getTransactionSessionTime();
			
				Calendar calTo = DateUtils.getNextSessionTime(futureDate, 4, sessionTimeList);
				
				if(logger.isDebugEnabled()) {
					logger.debug("Holiday start : " + new Timestamp(calFrom.getTimeInMillis()));
					logger.debug("Holiday end : " + new Timestamp(calTo.getTimeInMillis()));
				}
				
				List<DomesticTransferModel> domesticTransferList = domesticTransferRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_FUTURE_DATE, new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				saveTrxHoliday(domesticTransferList);
				
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void saveTrxHoliday(List<DomesticTransferModel> list) throws Exception {
		List<SpringBeanInvokerData<String>> jobs = new ArrayList<>();
		for(DomesticTransferModel model : list) {
			jobs.add(new SpringBeanInvokerData<>(model.getId(), "DomesticTransferService", "saveTransactionHolidayModel", new Object[] {model}, jobTimeout));
		}

		SpringBeanInvokerHelper.invokeAndForget(invoker, jobs, invokerTimeout, concurrencies);		
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void saveTransactionHolidayModel(DomesticTransferModel domestic) {
		if(logger.isDebugEnabled())
			logger.debug("Process save Trx domestic holiday: " + domestic.getId() + " - " + domestic.getReferenceNo());

		try {
			
			HolidayTransactionModel holidayTrxModel = new HolidayTransactionModel();
			holidayTrxModel.setId(domestic.getPendingTaskId());
			holidayTrxModel.setPendingTaskId(domestic.getPendingTaskId());
			holidayTrxModel.setStatus(domestic.getStatus());
			holidayTrxModel.setReferenceNo(domestic.getReferenceNo());
			holidayTrxModel.setMenu(domestic.getMenu());
			holidayTrxModel.setService(domestic.getService());
			holidayTrxModel.setSourceAccount(domestic.getSourceAccount());
			holidayTrxModel.setCorporate(domestic.getCorporate());
			holidayTrxModel.setTransactionAmount(domestic.getTransactionAmount());
			holidayTrxModel.setTransactionCurrency(domestic.getTransactionCurrency());
			holidayTrxModel.setTotalDebitedEquivalentAmount(domestic.getTotalDebitedEquivalentAmount());
			holidayTrxModel.setInstructionMode(domestic.getInstructionMode());
			holidayTrxModel.setInstructionDate(domestic.getInstructionDate());
			holidayTrxModel.setCreatedBy(domestic.getCreatedBy());
			holidayTrxModel.setCreatedDate(DateUtils.getCurrentTimestamp());
			holidayTrxModel.setIsProcessed("N");
			
			holidayTrxRepo.save(holidayTrxModel);
			
			//send notification email
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} 
	}
	
	
	@Override
	public Map<String, Object> updateHolidayTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		SimpleDateFormat sdfDate2 = new SimpleDateFormat("dd-MMMM-yyyy");
		try {
			//cancelId = pendingTaskId
			String pendingTaskId = (String) map.get("pendingTaskId");
			String updateType = (String) map.get("updateType");			
			String holidayTrxId = (String) map.get("holidayTrxId");
			if(logger.isDebugEnabled()) {
				logger.debug("pendingTaskId : " + pendingTaskId);
			}
			
			
			DomesticTransferModel domestic = domesticTransferRepo.findByPendingTaskIdAndIsProcessed(pendingTaskId, ApplicationConstants.NO);
			Timestamp newInstructionDate = domestic.getInstructionDate();
			
			if(updateType.equals("PREVIOUS WORKING DAY")) {
				newInstructionDate = getPrevWorkingDayFromTimestamp(domestic.getInstructionDate());				
			}else {
				newInstructionDate = getNextWorkingDayFromTimestamp(domestic.getInstructionDate());
			}
			
			domestic.setInstructionDate(DateUtils.getInstructionDateBySessionTime("07:00",newInstructionDate));
			domesticTransferRepo.save(domestic);
			
			CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findById(pendingTaskId);				
			pendingTask.setSessionTime("07:00");
			pendingTask.setInstructionDate(newInstructionDate);				
			Class<?> clazz = Class.forName(pendingTask.getModel());
			Map<String, Object> jsonMap = (Map<String, Object>) objectMapper.readValue(pendingTask.getValuesStr(), clazz);
			
			jsonMap.put("sessionTime", pendingTask.getSessionTime());
			jsonMap.put("instructionDate", sdfDate.format(newInstructionDate));
			Map confirmData = (Map<String, Object>)jsonMap.get("confirm_data");
			confirmData.put("payment_date", sdfDate2.format(newInstructionDate));
			jsonMap.put("confirm_data", confirmData);
			
			String jsonObj = objectMapper.writeValueAsString(jsonMap);
			pendingTask.setValues(jsonObj);
			
			pendingTaskRepo.save(pendingTask);
			
			//update Holiday Trx Model
			HolidayTransactionModel holidayTransactionModel = holidayTrxRepo.findOne(holidayTrxId);
			if(holidayTransactionModel!=null) {
			holidayTransactionModel.setIsProcessed("Y");
			
			holidayTrxRepo.save(holidayTransactionModel);
			}
			
			//notifyUser ins date berubah
			notifyUpdateInstructionDate(domestic, ApplicationConstants.APP_GPCASHBO);
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
}
