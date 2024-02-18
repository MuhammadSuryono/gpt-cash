package com.gpt.product.gpcash.corporate.transaction.international.services;

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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.spring.invoker.SpringBeanInvokerHelper;
import com.gpt.component.common.spring.invoker.SpringBeanInvokerHelper.SpringBeanInvokerData;
import com.gpt.component.common.spring.invoker.spi.ISpringBeanInvoker;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.errormapping.model.ErrorMappingModel;
import com.gpt.component.maintenance.errormapping.repository.ErrorMappingRepository;
import com.gpt.component.maintenance.exchangerate.model.ExchangeRateModel;
import com.gpt.component.maintenance.internationalbank.model.InternationalBankModel;
import com.gpt.component.maintenance.parametermt.model.CountryModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.parametermt.model.TransactionPurposeModel;
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
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListInternationalModel;
import com.gpt.product.gpcash.corporate.beneficiarylist.repository.BeneficiaryListInternationalRepository;
import com.gpt.product.gpcash.corporate.beneficiarylist.services.BeneficiaryListInternationalServiceImpl;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporatecharge.model.CorporateChargeModel;
import com.gpt.product.gpcash.corporate.corporatecharge.repository.CorporateChargeRepository;
import com.gpt.product.gpcash.corporate.corporatecharge.services.CorporateChargeService;
import com.gpt.product.gpcash.corporate.corporatespecialrate.services.SpecialRateService;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.forwardcontract.repository.ForwardContractRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.repository.CorporateUserPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.services.GlobalTransactionService;
import com.gpt.product.gpcash.corporate.transaction.international.constants.InternationalConstants;
import com.gpt.product.gpcash.corporate.transaction.international.model.InternationalTransferModel;
import com.gpt.product.gpcash.corporate.transaction.international.repository.InternationalTransferCustomRepository;
import com.gpt.product.gpcash.corporate.transaction.international.repository.InternationalTransferRepository;
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
public class InternationalTransferServiceImpl implements InternationalTransferService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.batch.transations.timeout-invoker}")
	private int invokerTimeout;
	
	@Value("${gpcash.batch.transations.timeout-job}")
	private int jobTimeout;

	@Value("${gpcash.batch.transations.concurrencies}")
	private int concurrencies;
	
	@Autowired
	private InternationalTransferRepository internationalTransferRepo;

	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;

	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;

	@Autowired
	private CorporateChargeService corporateChargeService;

	@Autowired
	private BeneficiaryListInternationalServiceImpl beneficiaryListInternationalService;

	@Autowired
	private TransactionValidationService transactionValidationService;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private MaintenanceRepository maitenanceRepo;	
	
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
	private BeneficiaryListInternationalRepository beneficiaryListInternationalRepo;
	
	@Autowired
	private CorporateWFEngine wfEngine;
	
	@Autowired
	private SpecialRateService specialRateService;
	
	@Autowired
	private ForwardContractRepository forwardContractRepo;
	
	
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
	private IDMUserRepository idmUserRepo;
	
	@Autowired
	private InternationalTransferCustomRepository internationalTransferCustomRepo;
	
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
				BeneficiaryListInternationalModel beneModel = beneficiaryListInternationalService.getExistingRecord((String) map.get(ApplicationConstants.TRANS_BEN_ID), corporateId, false);
				if(beneModel != null) {
					throw new BusinessException("GPT-0100099");
				}
			}
			
			// if isBeneficiaryFlag = Y then validate if accountNo must exist in table
			if (ApplicationConstants.YES.equals(map.get("isBeneficiaryFlag"))) {
				BeneficiaryListInternationalModel beneModel = beneficiaryListInternationalRepo.findOne((String) map.get(ApplicationConstants.TRANS_BEN_ID));
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
		pendingTaskService.validateFinalPayment(senderRefNo, InternationalTransferSC.menuCode, corporateId);
		
		List<InternationalTransferModel> modelList = internationalTransferRepo.findTransactionForFinalPaymentFlag(corporateId, 
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
		String benId = (String) map.get(ApplicationConstants.TRANS_BEN_ID);
		String uniqueKey = createdBy.concat(ApplicationConstants.DELIMITER_PIPE).concat(accountGroupDetailId)
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(transactionAmount.toPlainString())
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(benId)
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(String.valueOf((DateUtils.getCurrentTimestamp().getTime())));

		String instructionMode = (String) map.get(ApplicationConstants.INSTRUCTION_MODE);
		String sessionTime = (String) map.get("sessionTime");
		Timestamp instructionDate = (Timestamp) map.get(ApplicationConstants.INSTRUCTION_DATE);
		
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		vo.setUniqueKey(uniqueKey);
		vo.setCreatedBy(createdBy);
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setTransactionServiceCode((String) map.get(ApplicationConstants.TRANS_SERVICE_CODE));
		vo.setService("InternationalTransferSC");
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
			BeneficiaryListInternationalModel beneInternational=  corporateUtilsRepo.getBeneficiaryListInternationalRepo().findOne(benId);
			vo.setBenAccount(beneInternational.getBenAccountNo());
			vo.setBenAccountName(beneInternational.getBenAccountName());
			
			if(beneInternational.getBenAccountCurrency() != null) {
				benCurrencyCode = beneInternational.getBenAccountCurrency();
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
		
		String refNoSpecialRate = (String) map.get("treasuryCode");		
		vo.setRefNoSpecialRate(refNoSpecialRate);
		
		return vo;
	}

	@SuppressWarnings("unchecked")
	private InternationalTransferModel setMapToModel(InternationalTransferModel internationalTransfer, Map<String, Object> map,
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
		String isBenIdentity = (String) map.get("isBenIdentical");
		String isBenAffiliated = (String) map.get("isBenAffiliated");
		String benCountryCode = (String) map.get("benCountryCode");
		String trxDesc = (String) map.get("transactionDescription");
		String trxPurposeCode = (String) map.get("transactionPurpose");

		// set ID
		internationalTransfer.setId(vo.getId());

		// set transaction information
		internationalTransfer.setReferenceNo(vo.getReferenceNo());
		internationalTransfer.setSenderRefNo(vo.getSenderRefNo());
		internationalTransfer.setBenRefNo(vo.getBenRefNo());

		IDMMenuModel menu = new IDMMenuModel();
		menu.setCode(vo.getMenuCode());
		internationalTransfer.setMenu(menu);
		
		CorporateModel corpModel = corporateUtilsRepo.isCorporateValid(vo.getCorporateId());
		internationalTransfer.setCorporate(corpModel);
		
		internationalTransfer.setCorporateUserGroup(corporateUserGroup);
		
		IDMApplicationModel application = new IDMApplicationModel();
		//application code diambil dr yg login
		application.setCode((String) map.get(ApplicationConstants.APP_CODE));
		internationalTransfer.setApplication(application);
		
		AccountModel sourceAccountModel = productRepo.isAccountValid(vo.getSourceAccount());
		internationalTransfer.setSourceAccount(sourceAccountModel);
		
		internationalTransfer.setTransactionAmount(new BigDecimal(map.get(ApplicationConstants.TRANS_AMOUNT).toString()));
		internationalTransfer.setTransactionCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		internationalTransfer.setTotalChargeEquivalentAmount(
				new BigDecimal(map.get(ApplicationConstants.TRANS_TOTAL_CHARGE_EQ).toString()));
		internationalTransfer.setTotalDebitedEquivalentAmount(
				new BigDecimal(map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT).toString()));

		// set transaction service code
		ServiceModel service = new ServiceModel();
		service.setCode(transactionServiceCode);
		internationalTransfer.setService(service);

		// set beneficiary info
		// set benAccountNo based on benId
		setBenAccountInfo(internationalTransfer, benId, benAccountName, benAccountCurrency, transactionServiceCode, isSaveBenFlag,
				vo.getCorporateId(), corporateUserGroup.getId(), benAliasName, address1, address2, address3, isBenResident,
				benResidentCountryCode, isBenCitizen, benCitizenCountryCode, beneficiaryTypeCode, bankCode, isBeneficiaryFlag,
				isBenIdentity,isBenAffiliated, benCountryCode, vo.getCreatedBy());

		// set additional information
		internationalTransfer.setSenderRefNo((String) map.get("senderRefNo"));
		internationalTransfer.setIsFinalPayment((String) map.get("isFinalPayment"));
		internationalTransfer.setBenRefNo((String) map.get("benRefNo"));
		internationalTransfer.setRemark1((String) map.get("remark1"));
		internationalTransfer.setRemark2((String) map.get("remark2"));
		internationalTransfer.setRemark3((String) map.get("remark3"));
		internationalTransfer.setIsNotifyBen((String) map.get("isNotify"));
		internationalTransfer.setNotifyBenValue((String) map.get("notifyBenValue"));
		
		setInstructionMode(internationalTransfer, instructionMode, instructionDate, map);
		
		setCharge(internationalTransfer, (ArrayList<Map<String,Object>>) map.get("chargeList"));
		
		internationalTransfer.setChargeTo(chargeInstruction);
		
		internationalTransfer.setTransactionDescription(trxDesc);
		
		TransactionPurposeModel trxPurpose = new TransactionPurposeModel();
		trxPurpose.setCode(trxPurposeCode);
		internationalTransfer.setTransactionPurposeModel(trxPurpose);
		
		BranchModel branch = new BranchModel();
		branch.setCode((String) map.get("branchCode"));
		internationalTransfer.setBranch(branch);
		
		internationalTransfer.setUnderlyingCode((String)map.get("underlyingCode"));

		internationalTransfer.setRefNoSpecialRate(vo.getRefNoSpecialRate());
				
		return internationalTransfer;
	}
	
	private void setCharge(InternationalTransferModel internationalTransfer, List<Map<String, Object>> chargeList){
		for(int i=1; i <= chargeList.size(); i++){
			Map<String, Object> chargeMap = chargeList.get(i - 1);
			String chargeId = (String) chargeMap.get("id");
			BigDecimal value = new BigDecimal(chargeMap.get("value").toString());
			String currencyCode = (String) chargeMap.get("currencyCode");
			
			if(i == 1){
				internationalTransfer.setChargeType1(chargeId);
				internationalTransfer.setChargeTypeAmount1(value);
				internationalTransfer.setChargeTypeCurrency1(currencyCode);
			} else if(i == 2){
				internationalTransfer.setChargeType2(chargeId);
				internationalTransfer.setChargeTypeAmount2(value);
				internationalTransfer.setChargeTypeCurrency2(currencyCode);
			} else if(i == 3){
				internationalTransfer.setChargeType3(chargeId);
				internationalTransfer.setChargeTypeAmount3(value);
				internationalTransfer.setChargeTypeCurrency3(currencyCode);
			} else if(i == 4){
				internationalTransfer.setChargeType4(chargeId);
				internationalTransfer.setChargeTypeAmount4(value);
				internationalTransfer.setChargeTypeCurrency4(currencyCode);
			} else if(i == 5){
				internationalTransfer.setChargeType5(chargeId);
				internationalTransfer.setChargeTypeAmount5(value);
				internationalTransfer.setChargeTypeCurrency5(currencyCode);
			} 
		}
	}

	private void setInstructionMode(InternationalTransferModel internationalTransfer, String instructionMode, Timestamp instructionDate, 
			Map<String, Object> map) throws Exception {
		
		
		// set instruction mode
		internationalTransfer.setInstructionMode(instructionMode);

		if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
			internationalTransfer
					.setInstructionDate(transactionValidationService.getInstructionDateForRelease(instructionMode, instructionDate));
		} else if (instructionMode.equals(ApplicationConstants.SI_FUTURE_DATE)) {
			internationalTransfer.setInstructionDate(instructionDate);
		} else if (instructionMode.equals(ApplicationConstants.SI_RECURRING)) {
			internationalTransfer.setRecurringParamType((String) map.get("recurringParamType"));
			internationalTransfer.setRecurringParam((Integer) map.get("recurringParam"));
			internationalTransfer.setRecurringStartDate(instructionDate);
			internationalTransfer.setRecurringEndDate((Timestamp) Helper.parseStringToTimestamp((String) map.get("recurringEndDate")));
			internationalTransfer.setInstructionDate(instructionDate);
		}
	}

	private void setBenAccountInfo(InternationalTransferModel internationalTransfer, String benId, String benAccountName, String benAccountCurrency,
			String transactionServiceCode, String isSaveBenFlag, String corporateId, String corporateUserGroupId, String benAliasName,
			String address1, String address2, String address3, String isBenResident, String benResidentCountryCode,
			String isBenCitizen, String benCitizenCountryCode, String beneficiaryTypeCode, String bankCode, String isBeneficiaryFlag, 
			String isBenIdentity, String isBenAffiliated,String benCountryCode, String createdBy) throws Exception {
		
		String benAccountNo = null;
		InternationalBankModel intlBankModel = null;
		CountryModel benCountry = null;
		if (ApplicationConstants.YES.equals(isBeneficiaryFlag)) {
			// cek if benId exist in bene table
			BeneficiaryListInternationalModel beneficiaryListInternational = corporateUtilsRepo.isBeneficiaryListInternationalValid(benId);

			// benId from beneficiary droplist
			benAccountNo = beneficiaryListInternational.getBenAccountNo();
			benAccountName = beneficiaryListInternational.getBenAccountName();
			benAccountCurrency = beneficiaryListInternational.getBenAccountCurrency();
			benAliasName = beneficiaryListInternational.getBenAliasName();
			address1 = beneficiaryListInternational.getBenAddr1();
			address2 = beneficiaryListInternational.getBenAddr2();
			address3 = beneficiaryListInternational.getBenAddr3();
			isBenResident = beneficiaryListInternational.getLldIsBenResidence();
			benResidentCountryCode = beneficiaryListInternational.getLldBenResidenceCountry().getCode();
			isBenCitizen = beneficiaryListInternational.getLldIsBenCitizen();
			benCitizenCountryCode = beneficiaryListInternational.getLldBenCitizenCountry().getCode();
			intlBankModel = beneficiaryListInternational.getBenInternationalBankCode();
			isBenIdentity = beneficiaryListInternational.getLldIsBenIdentical();
			isBenAffiliated = beneficiaryListInternational.getLldIsBenAffiliated();
			benCountry = beneficiaryListInternational.getBenInternationalCountry();
			
		} else {
			// benId from third party account
			benAccountNo = benId;
			
			// check the bank first
			intlBankModel = maitenanceRepo.isInternationalBankValid(bankCode);
		
			if (ApplicationConstants.YES.equals(isSaveBenFlag)) {
				// save to bene table
				beneficiaryListInternationalService.saveBeneficiary(corporateId, corporateUserGroupId, benAccountNo,
					benAccountName, benAccountCurrency, ApplicationConstants.NO, null, 
					benAliasName, address1, address2, address3, isBenResident, benResidentCountryCode, 
					isBenCitizen, benCitizenCountryCode, isBenIdentity,isBenAffiliated,benCountryCode, bankCode, createdBy);

			}
		}
		
		internationalTransfer.setBenAccountNo(benAccountNo);
		internationalTransfer.setBenAccountName(benAccountName);
		internationalTransfer.setBenAccountCurrency(benAccountCurrency);
		internationalTransfer.setBenAliasName(benAliasName);
		internationalTransfer.setBenAddr1(address1);
		internationalTransfer.setBenAddr2(address2);
		internationalTransfer.setBenAddr3(address3);
		internationalTransfer.setLldIsBenResidence(isBenResident);
		
		String localCountryCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_COUNTRY_CODE).getValue();
		
		if(ApplicationConstants.YES.equals(isBenResident)) {
			benResidentCountryCode = localCountryCode;
		}
		
		CountryModel benResidentCountry = new CountryModel();
		benResidentCountry.setCode(benResidentCountryCode);
		internationalTransfer.setLldBenResidenceCountry(benResidentCountry);
		
		
		internationalTransfer.setLldIsBenCitizen(isBenCitizen);
		
		if(ApplicationConstants.YES.equals(isBenCitizen)) {
			benCitizenCountryCode = localCountryCode;
		}
		CountryModel benCitizenCountry = new CountryModel();
		benCitizenCountry.setCode(benCitizenCountryCode);
		internationalTransfer.setLldBenCitizenCountry(benCitizenCountry);

		internationalTransfer.setBenInternationalBankCode(intlBankModel);		
		internationalTransfer.setLldIsBenIdentical(isBenIdentity);
		internationalTransfer.setLldIsBenAffiliated(isBenAffiliated);
		
		benCountry = new CountryModel();
		benCountry.setCode(benCountryCode);
		internationalTransfer.setBenCountry(benCountry);
		
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

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				InternationalTransferModel internationalTransfer = new InternationalTransferModel();
				
				setMapToModel(internationalTransfer, map, true, vo);
				
				if(ApplicationConstants.SI_IMMEDIATE.equals(internationalTransfer.getInstructionMode())) {
					saveInternational(internationalTransfer, vo.getCreatedBy(), ApplicationConstants.YES);
					
					internationalTransfer.setIsError(ApplicationConstants.NO);
					doTransfer(internationalTransfer, (String)map.get(ApplicationConstants.APP_CODE));
					
					vo.setErrorCode(internationalTransfer.getErrorCode());
					vo.setIsError(internationalTransfer.getIsError());
					
					if(internationalTransfer.getRefNoSpecialRate()!=null) {
						if("FC".equals((String)map.get("exchangeRate"))){
							this.updateForwardContractUsage(internationalTransfer);
						}else if("SR".equals((String)map.get("exchangeRate"))){
							this.updateSpecialRateStatus(internationalTransfer);
						}
					}
					
				} else {
					saveInternational(internationalTransfer, vo.getCreatedBy(), ApplicationConstants.NO);
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

	private void saveInternational(InternationalTransferModel internationalTransfer, String createdBy, String isProcessed)
			throws ApplicationException, BusinessException {
		// set default value
		internationalTransfer.setIsProcessed(isProcessed);
		internationalTransfer.setCreatedDate(DateUtils.getCurrentTimestamp());
		internationalTransfer.setCreatedBy(createdBy);
		internationalTransfer.setInternationalTransferStatus(InternationalConstants.INT_STS_NEW_REQUEST);
		
		//jika pendingTaskId null maka transaksi bukan recurring, maka pendingTaskId = model.Id
		if(internationalTransfer.getPendingTaskId() == null)
			internationalTransfer.setPendingTaskId(internationalTransfer.getId());

		internationalTransferRepo.persist(internationalTransfer);
	}

	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			String benId = (String)map.get("benId");
			String bankCode = (String) map.get("bankCode");
			String benAccountNo = null;
			String benAccountName = null;
			
			InternationalBankModel internationalBank = maintenanceRepo.isInternationalBankValid(bankCode);
						
			
			//remark dibuka jika bank telah implemen international online
//			Map<String, Object> inputs = new HashMap<>();
//			inputs.put("accountNo", benId);
//			inputs.put("onlineBankCode", internationalBank.getOnlineBankCode());
//			
//			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.INTERNATIONAL_ONLINE_ACCOUNT_INQUIRY, inputs);
//
//			Map<String, Object> benAccountInfo = new HashMap<>();
//			benAccountInfo.put("benId", benId);
//			benAccountInfo.put("benAccountNo", outputs.get("accountNo"));
//			benAccountInfo.put("benAccountName", outputs.get("accountName"));
			//----------------------------------------------------------------------------
			
			if (ApplicationConstants.YES.equals((String) map.get("isBeneficiaryFlag"))) {
				BeneficiaryListInternationalModel beneInternational=  corporateUtilsRepo.getBeneficiaryListInternationalRepo().findOne(benId);
				benAccountNo = beneInternational.getBenAccountNo(); 
				benAccountName = beneInternational.getBenAccountName();					
				
			} else {
				benAccountNo = benId;
				benAccountName = (String) map.get(ApplicationConstants.TRANS_BEN_ACCT_NAME); 
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
			
			resultMap.put("bankCode", internationalBank.getCode());			
			resultMap.put("name", ValueUtils.getValue(internationalBank.getName()));

			String isBeneficiaryFlag = (String) map.get("isBeneficiaryFlag");
			if(isBeneficiaryFlag.equals(ApplicationConstants.YES)) {
				BeneficiaryListInternationalModel beneficiary = beneficiaryListInternationalService.findBeneficiary(benId);
				benAccountInfo.put("address1",ValueUtils.getValue(beneficiary.getBenAddr1()));
				benAccountInfo.put("address2", ValueUtils.getValue(beneficiary.getBenAddr2()));
				benAccountInfo.put("address3", ValueUtils.getValue(beneficiary.getBenAddr3()));
				
				benAccountInfo.put("isBenResident", ValueUtils.getValue(beneficiary.getLldIsBenResidence()));
				CountryModel lldBenResidenceCountry = beneficiary.getLldBenResidenceCountry();
				benAccountInfo.put("benResidentCountryCode", ValueUtils.getValue(lldBenResidenceCountry.getCode()));
				benAccountInfo.put("benResidentCountryName", ValueUtils.getValue(lldBenResidenceCountry.getName()));
				
				benAccountInfo.put("isBenCitizen", ValueUtils.getValue(beneficiary.getLldIsBenCitizen()));
				CountryModel lldBenCitizenCountry = beneficiary.getLldBenCitizenCountry();
				benAccountInfo.put("benCitizenCountryCode", ValueUtils.getValue(lldBenCitizenCountry.getCode()));
				benAccountInfo.put("benCitizenCountryName", ValueUtils.getValue(lldBenCitizenCountry.getName()));				
			}
			
			checkCustomValidation(map);

			resultMap.put("benAccountInfo", benAccountInfo);			
			resultMap.putAll(corporateChargeService.getCorporateChargesEquivalent((String) map.get(ApplicationConstants.APP_CODE),
					(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE),
					(String) map.get(ApplicationConstants.LOGIN_CORP_ID), (String) map.get("sourceAccountCurrency")));
			
			resultMap.put("amountEquivalent", map.get("equivalentAmount"));
			if (currencyCode.equals(map.get("sourceAccountCurrency"))) {
				Map<String, Object> rateMap = (Map<String, Object>) map.get("rateMap");
				ExchangeRateModel exchangeRate = (ExchangeRateModel) rateMap.get(map.get("transactionCurrency"));
				resultMap.put("trxSellRate", exchangeRate.getTransactionSellRate());
				resultMap.put("trxMidRate", exchangeRate.getTransactionMidRate());
				resultMap.put("trxBuyRate", exchangeRate.getTransactionBuyRate());
			}
			
			resultMap.put(ApplicationConstants.TRANS_AMOUNT_EQ, map.get("equivalentAmount"));
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private void doTransfer(InternationalTransferModel model, String appCode) throws Exception {
		boolean isRollback = false;
		boolean limitUpdated = false;
		
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
					totalCharge);
			
			limitUpdated = true;
			
			Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
			eaiAdapter.invokeService(EAIConstants.INTERNATIONAL_TRANSFER, inputs);
			
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
			globalTransactionService.save(model.getCorporate().getId(), model.getMenu().getCode(), 
					model.getService().getCode(), model.getReferenceNo(), 
					model.getId(), model.getTransactionCurrency(), model.getTotalChargeEquivalentAmount(), 
					model.getTransactionAmount(), 
					model.getTotalDebitedEquivalentAmount(), model.getIsError());
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
	
	private Map<String, Object> prepareInputsForEAI(InternationalTransferModel model, String appCode) throws Exception {
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
		inputs.put("benResidentFlag", model.getLldIsBenResidence());
		inputs.put("benCitizenFlag", model.getLldIsBenCitizen());
		inputs.put("benResidentCountryCode", model.getLldBenResidenceCountry().getCode());
		inputs.put("benCitizenCountryCode", model.getLldBenCitizenCountry().getCode());
		
		CorporateModel corpModel = model.getCorporate();
		inputs.put("remitterAddress1", corpModel.getAddress1());
		inputs.put("remitterAddress2", corpModel.getAddress2());
		inputs.put("remitterAddress3", corpModel.getAddress3());
		inputs.put("remitterResidentFlag", corpModel.getLldIsResidence());
		inputs.put("remitterResidentCountryCode", corpModel.getResidenceCountry().getCode());
		inputs.put("remitterCitizenFlag", corpModel.getLldIsCitizen());
		inputs.put("remitterCitizenCountryCode", corpModel.getCitizenCountry().getCode());
		inputs.put("remitterType", corpModel.getLldCategory());
				
		inputs.put("destinationBankCode", model.getBenInternationalBankCode().getCode());
		inputs.put("destinationBankName", model.getBenInternationalBankCode().getName());
		
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
		inputs.put("benRefNo", model.getBenRefNo());
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("inputs : " + inputs);
			logger.debug("\n\n\n\n\n");
		}
		
		return inputs;
		
	}
	
	/*private void notifyCorporate(InternationalTransferModel model, String appCode) {
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
	
	private void notifyTrxUsersHistory(InternationalTransferModel model, String appCode) {
		try {
			List<String> emails = pendingTaskService.getCorporateUserEmailWithNotifyTrx(model.getId());
			if (emails.size() > 0) {
				Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
				inputs.put("instructionMode", model.getInstructionMode());
				inputs.put("instructionDate", model.getInstructionDate());
				inputs.put("status", model.getStatus());
				inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
				inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
				inputs.put("emails", emails);
				inputs.put("subject", "notify user history");
				
				eaiAdapter.invokeService(EAIConstants.USER_TRANSFER_NOTIFICATION, inputs);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}	
	
	private void notifyBeneficiary(InternationalTransferModel model, String appCode) {		
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
				
				List<InternationalTransferModel> internationalTransferList = internationalTransferRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_FUTURE_DATE, new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(internationalTransferList);
				
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
				
				List<InternationalTransferModel> internationalTransferList = internationalTransferRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_RECURRING, 
						new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(internationalTransferList);
				
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void executeAllBatch(List<InternationalTransferModel> list) throws Exception {
		List<SpringBeanInvokerData<String>> jobs = new ArrayList<>();
		for(InternationalTransferModel model : list) {
			jobs.add(new SpringBeanInvokerData<>(model.getId(), "InternationalTransferService", "executeBatchTransaction", new Object[] {model}, jobTimeout));
		}

		SpringBeanInvokerHelper.invokeAndForget(invoker, jobs, invokerTimeout, concurrencies);		
	}
		
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void executeBatchTransaction(InternationalTransferModel international) {
		if(logger.isDebugEnabled())
			logger.debug("Process recurring international : " + international.getId() + " - " + international.getReferenceNo());
		
		boolean success = false;
		String errorCode = null;
		try {
			international.setIsProcessed(ApplicationConstants.YES);
			internationalTransferRepo.save(international);

			doTransfer(international, ApplicationConstants.APP_GPCASHBO);
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
				if(ApplicationConstants.SI_RECURRING.equals(international.getInstructionMode())) {					
					Timestamp newInstructionDate = InstructionModeUtils.getStandingInstructionDate(international.getRecurringParamType(), international.getRecurringParam(), international.getInstructionDate());
					
					Calendar calNewInstructionDate = DateUtils.getEarliestDate(newInstructionDate);
					Calendar calExpired = DateUtils.getEarliestDate(international.getRecurringEndDate());
					
					if(logger.isDebugEnabled()) {
						logger.debug("Recurring new instruction date : " + calNewInstructionDate.getTime());
						logger.debug("Recurring expired date : " + calExpired.getTime());
					}
					
					if(calNewInstructionDate.compareTo(calExpired) >= 0) {
						Calendar oldInstructionDate = Calendar.getInstance();
						oldInstructionDate.setTime(international.getInstructionDate());
						
						//get session time from old recurring
						calNewInstructionDate.set(Calendar.HOUR_OF_DAY, oldInstructionDate.get(Calendar.HOUR_OF_DAY));
						calNewInstructionDate.set(Calendar.MINUTE, oldInstructionDate.get(Calendar.MINUTE));
						saveNewTransactionForRecurring(international, new Timestamp(calNewInstructionDate.getTimeInMillis()));
					} else {
						isExpired = true;
					}
					
				}
			} catch(Exception e) {
				logger.error("Failed international id : " + international.getId());
			}
			
			if (success) {
				if (ApplicationConstants.YES.equals(international.getIsError())) {
					pendingTaskRepo.updatePendingTask(international.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
				
					trxStatusService.addTransactionStatus(international.getPendingTaskId(),  DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, international.getId(), true, international.getErrorCode());
				} else {
					pendingTaskRepo.updatePendingTask(international.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS);
					
					trxStatusService.addTransactionStatus(international.getPendingTaskId(),  DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, international.getId(), false, null);
				}
			} else {
				pendingTaskRepo.updatePendingTask(international.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
				
				trxStatusService.addTransactionStatus(international.getPendingTaskId(),  DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, international.getId(), true, errorCode);
			}
			
			if (isExpired) {
				pendingTaskRepo.updatePendingTask(international.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXPIRED);
				
				//insert into trx status EXPIRED
				trxStatusService.addTransactionStatus(international.getPendingTaskId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXPIRED, null, true, null);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void saveNewTransactionForRecurring(InternationalTransferModel model, Timestamp newInstructionDate) {
		try {
			InternationalTransferModel newModel = new InternationalTransferModel();
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
			newModel.setBenInternationalBankCode(model.getBenInternationalBankCode());		

			//set new instructionDate for recurring
			newModel.setInstructionMode(model.getInstructionMode());
			newModel.setRecurringParamType(model.getRecurringParamType());
			newModel.setRecurringParam(model.getRecurringParam());
			newModel.setRecurringStartDate(model.getRecurringStartDate());
			newModel.setRecurringEndDate(model.getRecurringEndDate());
			newModel.setInstructionDate(newInstructionDate);
			
			saveInternational(newModel, ApplicationConstants.CREATED_BY_SYSTEM, ApplicationConstants.NO);

	                //insert into trx status PENDING_EXECUTE with new model id
			Timestamp now = DateUtils.getCurrentTimestamp();
			pendingTaskRepo.updatePendingTask(makerPendingTaskIdForRecurring, ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE);
			
			trxStatusService.addTransactionStatus(makerPendingTaskIdForRecurring, now, TransactionActivityType.EXECUTE, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE, null, false, null);
		} catch(Exception e) {
			// ignore any error to avoid transaction rollback caused by saving new transaction
			logger.error("Failed save international recurring with id : " + model.getId());
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
			if(trxStatus.getStatus().equals(TransactionStatus.EXECUTE_SUCCESS) || (trxStatus.getStatus().equals(TransactionStatus.EXECUTE_FAIL) && trxStatus.getActionType().equals(TransactionActivityType.EXECUTE_TO_HOST))) {
				InternationalTransferModel model = internationalTransferRepo.findOne(trxStatus.getPendingTaskId());
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
	
	private List<Map<String, Object>> prepareDetailTransactionMap(InternationalTransferModel model, TransactionStatusModel trxStatus) throws Exception {
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
		modelMap.put("creditAccountCurrency", model.getTransactionCurrency());
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
			
			InternationalTransferModel model = internationalTransferRepo.findOne(trxStatus.getPendingTaskId());
			setParamForDownloadTrxStatus(reportParams, model);
			
			String masterReportFile = reportFolder + File.separator + "TransactionStatus" + 
					File.separator + "International" + File.separator + "download-transaction-international" + "-" + locale.getLanguage() + ".jasper";
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
	
	private void setParamForDownloadTrxStatus(Map<String, Object> reportParams, InternationalTransferModel model) throws Exception {
		DecimalFormat df = new DecimalFormat(moneyFormat);
		
		if(model != null) {
			InternationalBankModel internationalBank = model.getBenInternationalBankCode();
			reportParams.put("destinationBank", internationalBank.getName());
			reportParams.put("branchName", internationalBank.getOrganizationUnitName());
			reportParams.put("countryName", internationalBank.getCountry().getName());
			reportParams.put("swiftCode", internationalBank.getCode());
			
			reportParams.put("transactionPurpose", model.getTransactionPurposeModel().getCode().concat(" - ").concat(model.getTransactionPurposeModel().getName()));
			
			AccountModel sourceAccount = model.getSourceAccount();
			reportParams.put("sourceAccount", sourceAccount.getAccountNo());
			reportParams.put("sourceAccountName", sourceAccount.getAccountName());
			
			reportParams.put("benAccount", model.getBenAccountNo());
			reportParams.put("benAccountName", model.getBenAccountName());
			
			reportParams.put("transactionCurrency", model.getTransactionCurrency());
			reportParams.put("transactionAmount", df.format(model.getTransactionAmount()));
			
			/*if(model.getChargeTypeAmount1() != null && model.getChargeTypeAmount1().compareTo(BigDecimal.ZERO) > 0) {
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
			}*/
			
			CorporateUserPendingTaskModel pt = pendingTaskRepo.findByReferenceNo(model.getReferenceNo());
			
			String strValues = pt.getValuesStr();
			if (strValues != null) {
				try {
					Map<String, Object> valueMap = (Map<String, Object>) objectMapper.readValue(strValues, Class.forName(pt.getModel()));
					List<Map<String,Object>> listCharge = (List<Map<String,Object>>)valueMap.get("chargeList");
					
					for (int i = 0; i < listCharge.size(); i++) {
						Map<String, Object> mapCharge = listCharge.get(i);
						if (model.getChargeType1() != null && model.getChargeType1().equals(mapCharge.get("id"))) {
							reportParams.put("chargeType1", mapCharge.get("serviceChargeName"));
							reportParams.put("chargeCurrency1", model.getChargeTypeCurrency1());
							reportParams.put("chargeAmount1", df.format(model.getChargeTypeAmount1()));
						}
						if (model.getChargeType2() != null && model.getChargeType2().equals(mapCharge.get("id"))) {
							reportParams.put("chargeType2", mapCharge.get("serviceChargeName"));
							reportParams.put("chargeCurrency2", model.getChargeTypeCurrency2());
							reportParams.put("chargeAmount2", df.format(model.getChargeTypeAmount2()));
						}
						if (model.getChargeType3() != null && model.getChargeType3().equals(mapCharge.get("id"))) {
							reportParams.put("chargeType3", mapCharge.get("serviceChargeName"));
							reportParams.put("chargeCurrency3", model.getChargeTypeCurrency3());
							reportParams.put("chargeAmount3", df.format(model.getChargeTypeAmount3()));
						}
						if (model.getChargeType4() != null && model.getChargeType4().equals(mapCharge.get("id"))) {
							reportParams.put("chargeType4", mapCharge.get("serviceChargeName"));
							reportParams.put("chargeCurrency4", model.getChargeTypeCurrency4());
							reportParams.put("chargeAmount4", df.format(model.getChargeTypeAmount4()));
						}
						if (model.getChargeType5() != null && model.getChargeType5().equals(mapCharge.get("id"))) {
							reportParams.put("chargeType5", mapCharge.get("serviceChargeName"));
							reportParams.put("chargeCurrency5", model.getChargeTypeCurrency5());
							reportParams.put("chargeAmount5", df.format(model.getChargeTypeAmount5()));
						}
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			
			if(model.getTotalChargeEquivalentAmount().compareTo(BigDecimal.ZERO) > 0)
				reportParams.put("totalCharge", df.format(model.getTotalChargeEquivalentAmount()));
			
			
			reportParams.put("totalDebited", df.format(model.getTotalDebitedEquivalentAmount()));
			
			reportParams.put("remark1", ValueUtils.getValue(model.getTransactionDescription()));
			reportParams.put("refNo", model.getReferenceNo());
			reportParams.put("retrievalRefNo", model.getProcessRefNo());
		}
	}

	@Override
	public Map<String, Object> searchForBank(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String loginUserId = (String) map.get(ApplicationConstants.LOGIN_USERID);
			
			IDMUserModel idmUser = idmUserRepo.findOne(loginUserId);
			String userBranchCode = idmUser.getBranch().getCode();
			
			//only search by login user branch
			map.put("branchCode", userBranchCode);
			Page<InternationalTransferModel> result = internationalTransferCustomRepo.searchForBank(map, PagingUtils.createPageRequest(map));

			if(ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))){
				resultMap.put("result", setModelToMap(result.getContent(), false));
			} else {
				resultMap.put("result", setModelToMap(result.getContent(), true));
			}
			
			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<InternationalTransferModel> list, boolean isGetDetail) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (InternationalTransferModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(InternationalTransferModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("id", model.getId());
		map.put("refNo", model.getReferenceNo());
		
		CorporateModel corporate = model.getCorporate();
		map.put("corpId", corporate.getId());
		map.put("corpName", corporate.getName());
		
		Locale locale = LocaleContextHolder.getLocale();
		map.put("statusCode", model.getInternationalTransferStatus());
		map.put("statusName", message.getMessage(model.getInternationalTransferStatus(), null, model.getInternationalTransferStatus(), locale));
		
		
		if(isGetDetail){
			AccountModel sourceAccount = model.getSourceAccount();
			map.put("sourceAccountNo", sourceAccount.getAccountNo());
			map.put("sourceAccountName", sourceAccount.getAccountName());
			map.put("sourceAccountCurrency", sourceAccount.getCurrency().getCode());
			
			BranchModel branch = model.getBranch();
			map.put("branchCode", branch.getCode());
			map.put("branchName", branch.getName());
			
			map.put("trxAmount", model.getTransactionAmount());
			map.put("trxCurrency", model.getTransactionCurrency());
			
			map.put("chargeTo", model.getChargeTo());
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
			
			map.put("chargeList", chargeList);
			
			map.put("totalChargeAmount", model.getTotalChargeEquivalentAmount());
			map.put("totalDebitAmount", model.getTotalDebitedEquivalentAmount());
			map.put("remark1", model.getRemark1());
			map.put("remark2", model.getRemark2());
			map.put("remark3", model.getRemark3());
			map.put("trxDescription", model.getTransactionDescription());
			
			TransactionPurposeModel trxPurpose = model.getTransactionPurposeModel();
			map.put("trxPurposeCode", trxPurpose.getCode());
			map.put("trxPurposeName", trxPurpose.getName());

			SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MMMM-yyyy");
			map.put("instructionMode", model.getInstructionMode());
			map.put("instructionDate", sdfDate.format(model.getInstructionDate()));
			Calendar cal = Calendar.getInstance();
			cal.setTime(model.getInstructionDate());
			map.put("sessionTime", cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE));
			
			CountryModel benCountry = model.getBenCountry();
			map.put("benCountryCode", benCountry.getCode());
			map.put("benCountryName", benCountry.getName());
			
			InternationalBankModel benBank = model.getBenInternationalBankCode();
			map.put("benBankCode", benBank.getCode());
			map.put("benBankName", benBank.getName());
			map.put("benBankBranch", benBank.getOrganizationUnitName());
			map.put("benBankAddress1", ValueUtils.getValue(benBank.getAddress1()));
			map.put("benBankAddress2", ValueUtils.getValue(benBank.getAddress2()));
			map.put("benBankAddress3", ValueUtils.getValue(benBank.getAddress3()));
			
			
			map.put("benAccountNo", model.getBenAccountNo());
			map.put("benAccountName", model.getBenAccountName());
			map.put("benAccountCurrency", model.getBenAccountCurrency());
			map.put("benAddress1", ValueUtils.getValue(model.getBenAddr1()));
			map.put("benAddress2", ValueUtils.getValue(model.getBenAddr2()));
			map.put("benAddress3", ValueUtils.getValue(model.getBenAddr3()));
			
			map.put("isResident", model.getLldIsBenResidence());
			CountryModel residentCountry = model.getLldBenResidenceCountry();
			map.put("residentCountryCode", residentCountry.getCode());
			map.put("residentCountryName", residentCountry.getName());
			
			map.put("isCitizen", model.getLldIsBenCitizen());
			CountryModel citizenCountry = model.getLldBenCitizenCountry();
			map.put("citizenCountryCode", citizenCountry.getCode());
			map.put("citizenCountryName", citizenCountry.getName());
			
			map.put("isIdentical", model.getLldIsBenIdentical());
			map.put("isAffiliated", model.getLldIsBenAffiliated());
			
			map.put("processDate", ValueUtils.getValue(model.getProcessDate()));
			map.put("processRefNo", ValueUtils.getValue(model.getProcessRefNo()));
			map.put("processRemark", ValueUtils.getValue(model.getProcessRemark()));
			map.put("hostTransactionId", ValueUtils.getValue(model.getTransactionId()));
			map.put("declineDate", ValueUtils.getValue(model.getDeclineDate()));
			map.put("declineRefNo", ValueUtils.getValue(model.getDeclineRefNo()));
			map.put("declineRemark", ValueUtils.getValue(model.getDeclineReason()));
			
		}
		
		return map;
	}

	@Override
	public Map<String, Object> process(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		String intTransferId = (String) map.get("internationalTransferId");
		String loginUser = (String) map.get(ApplicationConstants.LOGIN_USERID);
		String transactionId = (String) map.get("transactionId");
		String processRemark = (String) map.get("processRemark");
		
		try {
			InternationalTransferModel international = internationalTransferRepo.findOne(intTransferId);
			
			if(InternationalConstants.INT_STS_NEW_REQUEST.equals(international.getInternationalTransferStatus())) {
				String processReferenceNo = Helper.generateBackOfficeReferenceNo();
				
				international.setTransactionId(transactionId);
				international.setProcessRemark(processRemark);
				international.setIsProcessed(ApplicationConstants.YES);
				international.setProcessRefNo(processReferenceNo);
				international.setInternationalTransferStatus(InternationalConstants.INT_STS_PROCESSED);
				international.setProcessDate(DateUtils.getCurrentTimestamp());
				updateInternationalTransfer(international, loginUser);
				
				pendingTaskRepo.updatePendingTask(international.getPendingTaskId(), ApplicationConstants.WF_STATUS_APPROVED, DateUtils.getCurrentTimestamp(), loginUser, TransactionStatus.EXECUTE_SUCCESS);
				trxStatusService.addTransactionStatus(international.getId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, processReferenceNo, false, null);
				
				notifyTrxUsersHistory(international, ApplicationConstants.APP_GPCASHBO);
				
				Locale locale = LocaleContextHolder.getLocale();
				resultMap.put("statusCode", international.getInternationalTransferStatus());
				resultMap.put("statusName", message.getMessage(international.getInternationalTransferStatus(), null, international.getInternationalTransferStatus(), locale));
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, processReferenceNo);
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200009");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + Helper.DATE_TIME_FORMATTER.format(DateUtils.getCurrentTimestamp()));
			} else {
				throw new BusinessException("GPT-ILLEGAL-ACTION");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	@Override
	public Map<String, Object> decline(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		String intTransferId = (String) map.get("internationalTransferId");
		String loginUser = (String) map.get(ApplicationConstants.LOGIN_USERID);
		String declineRemark = (String) map.get("declineRemark");
		
		try {
			InternationalTransferModel international = internationalTransferRepo.findOne(intTransferId);
			
			if(InternationalConstants.INT_STS_NEW_REQUEST.equals(international.getInternationalTransferStatus())) {
				String declineRefNo = Helper.generateBackOfficeReferenceNo();
				
				Timestamp declineDate = DateUtils.getCurrentTimestamp();
				
				international.setDeclineReason(declineRemark);
				international.setDeclineRefNo(declineRefNo);
				international.setDeclineDate(declineDate);
				international.setInternationalTransferStatus(InternationalConstants.INT_STS_DECLINED);
				
				updateInternationalTransfer(international, loginUser);
				
				pendingTaskRepo.updatePendingTask(international.getPendingTaskId(), ApplicationConstants.WF_STATUS_REJECTED, DateUtils.getCurrentTimestamp(), loginUser, TransactionStatus.EXECUTE_FAIL);
				trxStatusService.addTransactionStatus(international.getId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, declineRefNo, false, declineRemark);
				
				notifyTrxUsersHistory(international, ApplicationConstants.APP_GPCASHBO);
				
				Locale locale = LocaleContextHolder.getLocale();
				resultMap.put("statusCode", international.getInternationalTransferStatus());
				resultMap.put("statusName", message.getMessage(international.getInternationalTransferStatus(), null, international.getInternationalTransferStatus(), locale));
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, declineRefNo);
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200009");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + Helper.DATE_TIME_FORMATTER.format(declineDate));
				
			} else {
				throw new BusinessException("GPT-ILLEGAL-ACTION");
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private void updateInternationalTransfer(InternationalTransferModel international, String updatedBy)
			throws ApplicationException, BusinessException {
		international.setUpdatedDate(DateUtils.getCurrentTimestamp());
		international.setUpdatedBy(updatedBy);
		internationalTransferRepo.save(international);
	}
	
	private void updateSpecialRateStatus(InternationalTransferModel model) {
		// TODO Auto-generated method stub
		try {
			specialRateService.updateStatus(model.getRefNoSpecialRate(), ApplicationConstants.SPECIAL_RATE_SETTLED, model.getCreatedBy());
		} catch (Exception e) {
			logger.error("ERROR update special ref no"+ e.getMessage());
		}
	}
	
	private void updateForwardContractUsage(InternationalTransferModel model) {
		// TODO Auto-generated method stub
		try {
			forwardContractRepo.updateContractAmountLimit(model.getTransactionAmount(), model.getRefNoSpecialRate());			
		} catch (Exception e) {
			logger.error("ERROR update Forward COntract"+ e.getMessage());
		}
	}
}
