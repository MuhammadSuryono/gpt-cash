package com.gpt.product.gpcash.corporate.transaction.inhouse.services;

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
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListInHouseModel;
import com.gpt.product.gpcash.corporate.beneficiarylist.repository.BeneficiaryListInHouseRepository;
import com.gpt.product.gpcash.corporate.beneficiarylist.services.BeneficiaryListInHouseService;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateaccount.services.CorporateAccountService;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporatecharge.services.CorporateChargeService;
import com.gpt.product.gpcash.corporate.corporatespecialrate.services.SpecialRateService;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.repository.CorporateUserPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.services.GlobalTransactionService;
import com.gpt.product.gpcash.corporate.transaction.inhouse.model.InHouseTransferModel;
import com.gpt.product.gpcash.corporate.transaction.inhouse.repository.InHouseTransferRepository;
import com.gpt.product.gpcash.corporate.transaction.utils.InstructionModeUtils;
import com.gpt.product.gpcash.corporate.transaction.validation.services.TransactionValidationService;
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
public class InHouseTransferServiceImpl implements InHouseTransferService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.batch.transations.timeout-invoker}")
	private int invokerTimeout;
	
	@Value("${gpcash.batch.transations.timeout-job}")
	private int jobTimeout;

	@Value("${gpcash.batch.transations.concurrencies}")
	private int concurrencies;
	
	@Autowired
	private InHouseTransferRepository inHouseTransferRepo;
	
	@Autowired
	private IDMRepository idmRepo;

	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;

	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;

	@Autowired
	private CorporateChargeService corporateChargeService;

	@Autowired
	private BeneficiaryListInHouseService beneficiaryListInHouseService;

	@Autowired
	private TransactionValidationService transactionValidationService;
	
	@Autowired
	private CorporateAccountService corporateAccountService;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private ErrorMappingRepository errorMappingRepo;
	
	@Autowired
	private SysParamService sysParamService;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;	
	
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
	private BeneficiaryListInHouseRepository beneficiaryListInHouseRepo;
	
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
	
	private void validateFinalPayment(String corporateId, String senderRefNo) throws BusinessException {
		pendingTaskService.validateFinalPayment(senderRefNo, InHouseTransferSC.menuCode, corporateId);
		
		List<InHouseTransferModel> modelList = inHouseTransferRepo.findTransactionForFinalPaymentFlag(corporateId, 
				ApplicationConstants.YES, senderRefNo);
		
		if(modelList.size() > 0) {
			throw new BusinessException("GPT-0100199");
		}
	}
	
	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String senderRefNo = (String) map.get("senderRefNo");
			
			// if isSaveBenFlag = Y then validate if accountNo already in
			// already exist or not
			if (ApplicationConstants.YES.equals(map.get("isSaveBenFlag"))) {
				BeneficiaryListInHouseModel beneModel = beneficiaryListInHouseService.getExistingRecord((String) map.get(ApplicationConstants.TRANS_BEN_ID), corporateId, false);
				if(beneModel != null) {
					throw new BusinessException("GPT-0100099");
				}
			}
			
			// if isBeneficiaryFlag = Y then validate if accountNo must exist in table
			if (ApplicationConstants.YES.equals(map.get("isBeneficiaryFlag"))) {
				BeneficiaryListInHouseModel beneModel = beneficiaryListInHouseRepo.findOne((String) map.get(ApplicationConstants.TRANS_BEN_ID));
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

	private CorporateUserPendingTaskVO setCorporateUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String isFinalPayment = (String) map.get("isFinalPayment");
		CorporateUserModel maker = corporateUtilsRepo.isCorporateUserValid(createdBy);
		String userGroupId = maker.getCorporateUserGroup().getId();

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
		String transactionServiceCode = (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE);
		
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		vo.setUniqueKey(uniqueKey);
		vo.setCreatedBy(createdBy);
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setTransactionServiceCode(transactionServiceCode);
		vo.setService("InHouseTransferSC");
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
		vo.setRemark1((String) map.get("remark1"));
		vo.setRemark2((String) map.get("remark2"));
		vo.setRemark3((String) map.get("remark3"));
		//------------------------------------------------------------------
		
		
		//set credit info
		vo.setBenRefNo((String) map.get("benRefNo"));
		if (ApplicationConstants.SRVC_GPT_FTR_IH_3RD.equals(transactionServiceCode)) {
			if (ApplicationConstants.YES.equals((String) map.get("isBeneficiaryFlag"))) {
				BeneficiaryListInHouseModel beneInHouse=  corporateUtilsRepo.getBeneficiaryListInHouseRepo().findOne(benId);
				
				vo.setBenAccount(beneInHouse.getBenAccountNo());
				vo.setBenAccountName(beneInHouse.getBenAccountName());
				
				CurrencyModel benCurrency = maintenanceRepo.isCurrencyValid(beneInHouse.getBenAccountCurrency());
				vo.setBenAccountCurrencyCode(benCurrency.getCode());
				vo.setBenAccountCurrencyName(benCurrency.getName());
			} else {
				vo.setBenAccount(benId);
				vo.setBenAccountName((String) map.get(ApplicationConstants.TRANS_BEN_ACCT_NAME));
				
				CurrencyModel benCurrency = maintenanceRepo.isCurrencyValid((String) map.get(ApplicationConstants.TRANS_BEN_ACCT_CURRENCY));
				vo.setBenAccountCurrencyCode(benCurrency.getCode());
				vo.setBenAccountCurrencyName(benCurrency.getName());
			}
		} else if (ApplicationConstants.SRVC_GPT_FTR_IH_OWN.equals(transactionServiceCode)) {
			// get benAccountNo from corporate account group detail
			CorporateAccountGroupDetailModel corporateAccountGroupDetail = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(corporateId, benId);
			AccountModel benAccount = corporateAccountGroupDetail.getCorporateAccount().getAccount();

			vo.setBenAccount(benAccount.getAccountNo());
			vo.setBenAccountName(benAccount.getAccountName());
			vo.setBenAccountCurrencyCode(benAccount.getCurrency().getCode());
			vo.setBenAccountCurrencyName(benAccount.getCurrency().getName());
		}
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
		
		String refNoSpecialRate = (String) map.get("treasuryCode");		
		vo.setRefNoSpecialRate(refNoSpecialRate);
		
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
	private InHouseTransferModel setMapToModel(InHouseTransferModel inHouseTransfer, Map<String, Object> map,
			boolean isNew, CorporateUserPendingTaskVO vo) throws Exception {
		CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(vo.getCreatedBy());
		CorporateUserGroupModel corporateUserGroup = corporateUser.getCorporateUserGroup();

		String benId = (String) map.get(ApplicationConstants.TRANS_BEN_ID);
		String transactionServiceCode = (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE);
		String isBeneficiaryFlag = (String) map.get("isBeneficiaryFlag");
		String isSaveBenFlag = (String) map.get("isSaveBenFlag");
		String instructionMode = (String) map.get("instructionMode");
		String isVirtualAccount = (String) map.get("isVirtualAccount");
		
		Timestamp instructionDate = vo.getInstructionDate();
		if(ApplicationConstants.SI_RECURRING.equals(vo.getInstructionMode())) {
			//override instructionDate to today timestamp
			Timestamp recurringInstructionDate = InstructionModeUtils.getStandingInstructionDate(vo.getRecurringParamType(), vo.getRecurringParam(), 
					DateUtils.getCurrentTimestamp());
			
			instructionDate = DateUtils.getInstructionDateBySessionTime(vo.getSessionTime(), recurringInstructionDate);
		}

		// set ID
		inHouseTransfer.setId(vo.getId());

		// set transaction information
		inHouseTransfer.setReferenceNo(vo.getReferenceNo());
		inHouseTransfer.setSenderRefNo(vo.getSenderRefNo());
		inHouseTransfer.setBenRefNo(vo.getBenRefNo());

		IDMMenuModel menu = new IDMMenuModel();
		menu.setCode(vo.getMenuCode());
		inHouseTransfer.setMenu(menu);
		
		CorporateModel corpModel = corporateUtilsRepo.isCorporateValid(vo.getCorporateId());
		inHouseTransfer.setCorporate(corpModel);
		
		inHouseTransfer.setCorporateUserGroup(corporateUserGroup);
		
		IDMApplicationModel application = new IDMApplicationModel();
		//application code diambil dr yg login
		application.setCode((String) map.get(ApplicationConstants.APP_CODE)); 
		inHouseTransfer.setApplication(application);

		AccountModel sourceAccountModel = productRepo.isAccountValid(vo.getSourceAccount());
		inHouseTransfer.setSourceAccount(sourceAccountModel);
		
		inHouseTransfer.setTransactionAmount(new BigDecimal(map.get(ApplicationConstants.TRANS_AMOUNT).toString()));
		inHouseTransfer.setTransactionCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		inHouseTransfer.setTotalChargeEquivalentAmount(
				new BigDecimal(map.get(ApplicationConstants.TRANS_TOTAL_CHARGE).toString()));
		inHouseTransfer.setTotalDebitedEquivalentAmount(
				new BigDecimal(map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT).toString()));

		// set transaction service code
		ServiceModel service = new ServiceModel();
		service.setCode(transactionServiceCode);
		inHouseTransfer.setService(service);

		// set benAccountNo based on benId
		setBenAccountInfo(inHouseTransfer, benId, transactionServiceCode, isSaveBenFlag,
				vo.getCorporateId(), corporateUserGroup.getId(), isBeneficiaryFlag, vo.getCreatedBy(), isVirtualAccount);

		// set additional information
		inHouseTransfer.setSenderRefNo((String) map.get("senderRefNo"));
		inHouseTransfer.setIsFinalPayment((String) map.get("isFinalPayment"));
		inHouseTransfer.setBenRefNo((String) map.get("benRefNo"));
		inHouseTransfer.setRemark1((String) map.get("remark1"));
		inHouseTransfer.setRemark2((String) map.get("remark2"));
		inHouseTransfer.setRemark3((String) map.get("remark3"));
		inHouseTransfer.setIsNotifyBen((String) map.get("isNotify"));
		inHouseTransfer.setNotifyBenValue((String) map.get("notifyBenValue"));
		
		setInstructionMode(inHouseTransfer, instructionMode, instructionDate, map);
		
		setCharge(inHouseTransfer, (ArrayList<Map<String,Object>>) map.get("chargeList"));
		
		inHouseTransfer.setRefNoSpecialRate(vo.getRefNoSpecialRate());

		return inHouseTransfer;
	}
	
	private void setCharge(InHouseTransferModel inHouseTransfer, List<Map<String, Object>> chargeList){
		for(int i=1; i <= chargeList.size(); i++){
			Map<String, Object> chargeMap = chargeList.get(i - 1);
			String chargeId = (String) chargeMap.get("id");
			BigDecimal value = new BigDecimal(chargeMap.get("value").toString());
			String currencyCode = (String) chargeMap.get("currencyCode");
			
			if(i == 1){
				inHouseTransfer.setChargeType1(chargeId);
				inHouseTransfer.setChargeTypeAmount1(value);
				inHouseTransfer.setChargeTypeCurrency1(currencyCode);
			} else if(i == 2){
				inHouseTransfer.setChargeType2(chargeId);
				inHouseTransfer.setChargeTypeAmount2(value);
				inHouseTransfer.setChargeTypeCurrency2(currencyCode);
			} else if(i == 3){
				inHouseTransfer.setChargeType3(chargeId);
				inHouseTransfer.setChargeTypeAmount3(value);
				inHouseTransfer.setChargeTypeCurrency3(currencyCode);
			} else if(i == 4){
				inHouseTransfer.setChargeType4(chargeId);
				inHouseTransfer.setChargeTypeAmount4(value);
				inHouseTransfer.setChargeTypeCurrency4(currencyCode);
			} else if(i == 5){
				inHouseTransfer.setChargeType5(chargeId);
				inHouseTransfer.setChargeTypeAmount5(value);
				inHouseTransfer.setChargeTypeCurrency5(currencyCode);
			} 
		}
	}

	private void setInstructionMode(InHouseTransferModel inHouseTransfer, String instructionMode, Timestamp instructionDate, 
			Map<String, Object> map) throws Exception {
		
		
		// set instruction mode
		inHouseTransfer.setInstructionMode(instructionMode);

		if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
			inHouseTransfer
					.setInstructionDate(transactionValidationService.getInstructionDateForRelease(instructionMode, instructionDate));
		} else if (instructionMode.equals(ApplicationConstants.SI_FUTURE_DATE)) {
			inHouseTransfer.setInstructionDate(instructionDate);
		} else if (instructionMode.equals(ApplicationConstants.SI_RECURRING)) {
			inHouseTransfer.setRecurringParamType((String) map.get("recurringParamType"));
			inHouseTransfer.setRecurringParam((Integer) map.get("recurringParam"));
			inHouseTransfer.setRecurringStartDate(instructionDate);
			inHouseTransfer.setRecurringEndDate((Timestamp) Helper.parseStringToTimestamp((String) map.get("recurringEndDate")));
			inHouseTransfer.setInstructionDate(instructionDate);
		}
	}

	private void setBenAccountInfo(InHouseTransferModel inHouseTransfer,String benId, String transactionServiceCode, 
			String isSaveBenFlag, String corporateId, String corporateUserGroupId, String isBeneficiaryFlag, String createdBy, String isVirtualAccount) throws Exception {
		
		String benAccountNo = null;
		String benAccountName = null;
		String benAccountCurrency = null;			
		if (ApplicationConstants.SRVC_GPT_FTR_IH_3RD.equals(transactionServiceCode)) {
			if (ApplicationConstants.YES.equals(isBeneficiaryFlag)) {
				// benId from beneficiary droplist
				
				// cek if benId exist in bene table
				BeneficiaryListInHouseModel beneficiaryListInHouse = corporateUtilsRepo.isBeneficiaryListInHouseValid(benId);
				
				benAccountNo = beneficiaryListInHouse.getBenAccountNo();
				benAccountName = beneficiaryListInHouse.getBenAccountName();
				benAccountCurrency = beneficiaryListInHouse.getBenAccountCurrency();
			} else {
				// benId from third party account
				
				if (ApplicationConstants.YES.equals(isVirtualAccount)) {
					
					Map<String, Object> inputs = new HashMap<>();
					inputs.put("accountNo", benId);
					
					Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.TRANSFER_VA_INQUIRY, inputs);
					
					benAccountNo = (String) outputs.get("accountNo");
					benAccountName = (String) outputs.get("accountName");
					benAccountCurrency = (String)outputs.get("accountCurrencyCode");
					
				} else {
				
					benAccountNo = benId;
					
					Map<String, Object> inputs = new HashMap<>();
					inputs.put("accountNo", benAccountNo);			
					Map<String, Object> outputs = corporateAccountService.searchOnlineByAccountNo(inputs);
					
					benAccountName = (String)outputs.get("accountName");
					benAccountCurrency = (String)outputs.get("accountCurrencyName");
				
				}
				
				if (ApplicationConstants.YES.equals(isSaveBenFlag)) {
					// save to bene table
					beneficiaryListInHouseService.saveBeneficiary(corporateId, corporateUserGroupId, benAccountNo,
							benAccountName, benAccountCurrency, ApplicationConstants.NO, null, createdBy, isVirtualAccount);

				}
			}

		} else if (ApplicationConstants.SRVC_GPT_FTR_IH_OWN.equals(transactionServiceCode)) {
			// get benAccountNo from corporate account group detail
			CorporateAccountGroupDetailModel corporateAccountGroupDetail = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(corporateId, benId);
			AccountModel benAccount = corporateAccountGroupDetail.getCorporateAccount().getAccount();
			benAccountNo = benAccount.getAccountNo(); 
			benAccountName = benAccount.getAccountName();
			benAccountCurrency = benAccount.getCurrency().getCode();
		}
		
		//set beneficiary info
		inHouseTransfer.setBenAccountNo(benAccountNo);
		inHouseTransfer.setBenAccountName(benAccountName);
		inHouseTransfer.setBenAccountCurrency(benAccountCurrency);
	}

	@SuppressWarnings("unchecked")
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {

		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				InHouseTransferModel inHouseTransfer = new InHouseTransferModel();
				setMapToModel(inHouseTransfer, map, true, vo);

				if(ApplicationConstants.SI_IMMEDIATE.equals(inHouseTransfer.getInstructionMode())) {
					saveInHouse(inHouseTransfer, vo.getCreatedBy(), ApplicationConstants.YES);
					
					doTransfer(inHouseTransfer, (String)map.get(ApplicationConstants.APP_CODE));
					vo.setErrorCode(inHouseTransfer.getErrorCode());
					vo.setIsError(inHouseTransfer.getIsError());
				} else {
					saveInHouse(inHouseTransfer, vo.getCreatedBy(), ApplicationConstants.NO);
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

	private void saveInHouse(InHouseTransferModel inHouseTransfer, String createdBy, String isProcessed)
			throws ApplicationException, BusinessException {
		// set default value
		inHouseTransfer.setIsProcessed(isProcessed);
		inHouseTransfer.setCreatedDate(DateUtils.getCurrentTimestamp());
		inHouseTransfer.setCreatedBy(createdBy);
		
		//jika pendingTaskId null maka transaksi bukan recurring, maka pendingTaskId = model.Id
		if(inHouseTransfer.getPendingTaskId() == null)
			inHouseTransfer.setPendingTaskId(inHouseTransfer.getId());

		inHouseTransferRepo.persist(inHouseTransfer);
	}

	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			String benAccountNo = null;
			String benAccountName = null;
			String benAccountCurrency = null;
			String benId = (String)map.get("benId");
			String transactionServiceCode = (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE);
			String corporateId = (String)map.get(ApplicationConstants.LOGIN_CORP_ID);
			String accountGroupDtlId = (String)map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
			String isVirtualAccount = (String) map.get("isVirtualAccount");
			
			if (ApplicationConstants.SRVC_GPT_FTR_IH_3RD.equals(transactionServiceCode)) {
				if (ApplicationConstants.YES.equals((String) map.get("isBeneficiaryFlag"))) {
					// benId from beneficiary droplist
					
					// cek if benId exist in bene table
					BeneficiaryListInHouseModel beneficiaryListInHouse = corporateUtilsRepo.getBeneficiaryListInHouseRepo().findOne(benId);
					benAccountNo = beneficiaryListInHouse.getBenAccountNo();
					benAccountName = beneficiaryListInHouse.getBenAccountName();
					benAccountCurrency = beneficiaryListInHouse.getBenAccountCurrency();								
				} else {
					// benId from third party account
					
					if (ApplicationConstants.YES.equals(isVirtualAccount)) {
						
						Map<String, Object> inputs = new HashMap<>();
						inputs.put("accountNo", benId);
						
						Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.TRANSFER_VA_INQUIRY, inputs);
						
						benAccountNo = (String) outputs.get("accountNo");
						benAccountName = (String) outputs.get("accountName");
						benAccountCurrency = (String)outputs.get("accountCurrencyCode");
						
					} else {

						benAccountNo = benId;
						
						Map<String, Object> inputs = new HashMap<>();
						inputs.put("accountNo", benAccountNo);			
						Map<String, Object> outputs = corporateAccountService.searchOnlineByAccountNo(inputs);
						
						benAccountName = (String)outputs.get("accountName");
						benAccountCurrency = (String)outputs.get("accountCurrencyName");								
					}
				}		
			} else if (ApplicationConstants.SRVC_GPT_FTR_IH_OWN.equals(transactionServiceCode)) {
				// get benAccountNo from corporate account group detail
				CorporateAccountGroupDetailModel corporateAccountGroupDetail = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(corporateId, benId);
				AccountModel benAccount = corporateAccountGroupDetail.getCorporateAccount().getAccount();
				benAccountNo = benAccount.getAccountNo(); 
				benAccountName = benAccount.getAccountName();
				benAccountCurrency = benAccount.getCurrency().getCode();
			}
				

			Map<String, Object> benAccountInfo = new HashMap<>();
			benAccountInfo.put("benId", benId);
			benAccountInfo.put("benAccountNo", benAccountNo);
			benAccountInfo.put("benAccountName", benAccountName);
			benAccountInfo.put("benAccountCurrency", benAccountCurrency);
			
			checkCustomValidation(map);
			
			//check source account cannot same to bene account
			CorporateAccountGroupDetailModel gdm = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(corporateId, 
					accountGroupDtlId);		
			AccountModel sourceAccount = gdm.getCorporateAccount().getAccount();
			checkSourceAccountAndBenAccountCannotSame(sourceAccount.getAccountNo(), benAccountNo);
			//----------------------------------------------
			
			resultMap.put("benAccountInfo", benAccountInfo);			
			resultMap.putAll(corporateChargeService.getCorporateCharges((String) map.get(ApplicationConstants.APP_CODE),
					(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE),
					(String) map.get(ApplicationConstants.LOGIN_CORP_ID)));
			
			resultMap.put(ApplicationConstants.TRANS_AMOUNT_EQ, map.get("equivalentAmount"));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private void doTransfer(InHouseTransferModel model, String appCode) throws Exception {
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
			
			//update bank forex limit
			transactionValidationService.updateBankForexLimit(
					model.getSourceAccount().getCurrency().getCode(),
					model.getTransactionCurrency(),
					model.getTransactionAmount(),
					model.getTotalDebitedEquivalentAmount());
			
			limitUpdated = true;

			Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
			eaiAdapter.invokeService(EAIConstants.INHOUSE_TRANSFER, inputs);
			
			model.setStatus("GPT-0100130");
			model.setIsError(ApplicationConstants.NO);
			
			this.updateSpecialRateStatus(model);
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
				globalTransactionService.save(model.getCorporate().getId(), InHouseTransferSC.menuCode, 
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
	
	private void updateSpecialRateStatus(InHouseTransferModel model) {
		// TODO Auto-generated method stub
		try {
			specialRateService.updateStatus(model.getRefNoSpecialRate(), ApplicationConstants.SPECIAL_RATE_SETTLED, model.getCreatedBy());
		} catch (Exception e) {
			logger.error("ERROR update special ref no"+ e.getMessage());
		}
	}

	private Map<String, Object> prepareInputsForEAI(InHouseTransferModel model, String appCode) {
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
		inputs.put("benRefNo", model.getBenRefNo());
		
		inputs.put("servicingBranch", model.getCorporate().getBranch().getCode());
		inputs.put("corpId", model.getCorporate().getId());
		inputs.put("userId", model.getCreatedBy());
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("inputs : " + inputs);
			logger.debug("\n\n\n\n\n");
		}
		
		return inputs;
		
	}
	
	/*private void notifyCorporate(InHouseTransferModel model, String appCode) {
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
	
	private void notifyTrxUsersHistory(InHouseTransferModel model, String appCode) {
		try {
			List<String> emails = pendingTaskService.getCorporateUserEmailWithNotifyTrx(model.getId());
			if (emails.size() > 0) {
				IDMMenuModel menu = idmRepo.isIDMMenuValid(InHouseTransferSC.menuCode);
				
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
	
	private void notifyBeneficiary(InHouseTransferModel model, String appCode) {
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
				
				List<InHouseTransferModel> inHouseTransferList = inHouseTransferRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_FUTURE_DATE, 
						new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(inHouseTransferList);
				
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
				
				List<InHouseTransferModel> inHouseTransferList = inHouseTransferRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_RECURRING, 
						new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(inHouseTransferList);
				
			}
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void executeAllBatch(List<InHouseTransferModel> list) throws Exception {
		List<SpringBeanInvokerData<String>> jobs = new ArrayList<>();
		for(InHouseTransferModel model : list) {
			jobs.add(new SpringBeanInvokerData<>(model.getId(), "InHouseTransferService", "executeBatchTransaction", new Object[] {model}, jobTimeout));
		}

		SpringBeanInvokerHelper.invokeAndForget(invoker, jobs, invokerTimeout, concurrencies);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void executeBatchTransaction(InHouseTransferModel inhouse) {
		if(logger.isDebugEnabled())
			logger.debug("Process recurring inHouse : " + inhouse.getId() + " - " + inhouse.getReferenceNo());
		
		boolean success = false;
		boolean isTimeOut = false;
		String errorCode = null;
		Timestamp activityDate = DateUtils.getCurrentTimestamp();
		try {			
			inhouse.setIsProcessed(ApplicationConstants.YES);
			inHouseTransferRepo.save(inhouse);
		
			//avoid lazy when get details
			inhouse = inHouseTransferRepo.findOne(inhouse.getId());
			
			doTransfer(inhouse, ApplicationConstants.APP_GPCASHBO);
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
				if(ApplicationConstants.SI_RECURRING.equals(inhouse.getInstructionMode())) {					
					Timestamp newInstructionDate = InstructionModeUtils.getStandingInstructionDate(inhouse.getRecurringParamType(), inhouse.getRecurringParam(), inhouse.getInstructionDate());
					
					Calendar calNewInstructionDate = DateUtils.getEarliestDate(newInstructionDate);
					Calendar calExpired = DateUtils.getEarliestDate(inhouse.getRecurringEndDate());
					
					if(logger.isDebugEnabled()) {
						logger.debug("Recurring new instruction date : " + calNewInstructionDate.getTime());
						logger.debug("Recurring expired date : " + calExpired.getTime());
					}
					
					isRecurring = true;
					
					if(calNewInstructionDate.compareTo(calExpired) <= 0) {
						Calendar oldInstructionDate = Calendar.getInstance();
						oldInstructionDate.setTime(inhouse.getInstructionDate());
						
						//get session time from old recurring
						calNewInstructionDate.set(Calendar.HOUR_OF_DAY, oldInstructionDate.get(Calendar.HOUR_OF_DAY));
						calNewInstructionDate.set(Calendar.MINUTE, oldInstructionDate.get(Calendar.MINUTE));
						saveNewTransactionForRecurring(inhouse, new Timestamp(calNewInstructionDate.getTimeInMillis()));
					} else {
						isExpired = true;
					}
					
				}
				
			} catch(Exception e) {
				logger.error("Failed inhouse id : " + inhouse.getId());
			}
			
			if (success) {
				if (ApplicationConstants.YES.equals(inhouse.getIsError())) {
					pendingTaskRepo.updatePendingTask(inhouse.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
				
					trxStatusService.addTransactionStatus(inhouse.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, inhouse.getId(), true, inhouse.getErrorCode());
				} else {
					
					if (isRecurring) {
						pendingTaskRepo.updatePendingTask(inhouse.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE);						
					} else {
						pendingTaskRepo.updatePendingTask(inhouse.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS);
					}
					
					trxStatusService.addTransactionStatus(inhouse.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, inhouse.getId(), false, null);
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
					pendingTaskRepo.updatePendingTask(inhouse.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE);						
					if(!isTimeOut) {
						trxStatusService.addTransactionStatus(inhouse.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, inhouse.getId(), true, errorCode);
				} else {
						trxStatusService.addTransactionStatus(inhouse.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE, inhouse.getId(), true, errorCode);
					}
					
				} else {
					if(!isTimeOut) {
					pendingTaskRepo.updatePendingTask(inhouse.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
						trxStatusService.addTransactionStatus(inhouse.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, inhouse.getId(), true, errorCode);
					}else{
						pendingTaskRepo.updatePendingTask(inhouse.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE);
						trxStatusService.addTransactionStatus(inhouse.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE, inhouse.getId(), true, errorCode);					
					}
				}
				
			}
			
			if (isExpired) {
				pendingTaskRepo.updatePendingTask(inhouse.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXPIRED);
				
				//insert into trx status EXPIRED
				trxStatusService.addTransactionStatus(inhouse.getPendingTaskId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXPIRED, null, true, null);
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void saveNewTransactionForRecurring(InHouseTransferModel model, Timestamp newInstructionDate) {
		try {
			InHouseTransferModel newModel = new InHouseTransferModel();
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

			//set new instructionDate for recurring
			newModel.setInstructionMode(model.getInstructionMode());
			newModel.setRecurringParamType(model.getRecurringParamType());
			newModel.setRecurringParam(model.getRecurringParam());
			newModel.setRecurringStartDate(model.getRecurringStartDate());
			newModel.setRecurringEndDate(model.getRecurringEndDate());
			newModel.setInstructionDate(newInstructionDate);
			
			saveInHouse(newModel, ApplicationConstants.CREATED_BY_SYSTEM, ApplicationConstants.NO);
			
			//insert into trx status PENDING_EXECUTE with new model id
			Timestamp now = DateUtils.getCurrentTimestamp();
			pendingTaskRepo.updatePendingTask(makerPendingTaskIdForRecurring, ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE);
			
			trxStatusService.addTransactionStatus(makerPendingTaskIdForRecurring, now, TransactionActivityType.EXECUTE, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE, null, false, null);
		} catch(Exception e) {
			// ignore any error to avoid transaction rollback caused by saving new transaction
			logger.error("Failed save InHouse recurring with id : " + model.getId());
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
				InHouseTransferModel model = inHouseTransferRepo.findOne(trxStatus.getEaiRefNo());
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
	
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> prepareDetailTransactionMap(InHouseTransferModel model, TransactionStatusModel trxStatus) throws Exception {
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
			
			InHouseTransferModel model = inHouseTransferRepo.findOne(trxStatus.getEaiRefNo());
			setParamForDownloadTrxStatus(reportParams, model);
			
			String masterReportFile = reportFolder + File.separator + "TransactionStatus" + 
					File.separator + "InHouse" + File.separator + "download-transaction-inhouse" + "-" + locale.getLanguage() + ".jasper";
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
	
	private void setCopyrightSubReport(Map<String, Object> reportParams) throws JRException {
		Locale locale = LocaleContextHolder.getLocale();
		String copyrightFile = reportFolder + File.separator + "Copyright" + File.separator + "bank-copyright" + "-" + locale.getLanguage() + ".jasper";
		JasperReport copyrightReport = (JasperReport) JRLoader.loadObject(new File(copyrightFile));
		reportParams.put("COPYRIGHT_REPORT", copyrightReport);
	}
	
	private void setSuspectTrxSubReport(Map<String, Object> reportParams) throws JRException {
		Locale locale = LocaleContextHolder.getLocale();
		String suspectTrxFile = reportFolder + File.separator + "SuspectTrx" + File.separator + "suspect-trx" + "-" + locale.getLanguage() + ".jasper";
		JasperReport suspectTrxReport = (JasperReport) JRLoader.loadObject(new File(suspectTrxFile));
		reportParams.put("SUSPECT_TRX_REPORT", suspectTrxReport);
	}
	
	@SuppressWarnings("unchecked")
	private void setParamForDownloadTrxStatus(Map<String, Object> reportParams, InHouseTransferModel model) throws JsonParseException, JsonMappingException, ClassNotFoundException, IOException {
		DecimalFormat df = new DecimalFormat(moneyFormat);
		if(model != null) {
			reportParams.put("service", ValueUtils.getValue(model.getService().getDscp()));
			
			AccountModel sourceAccount = model.getSourceAccount();
			reportParams.put("sourceAccount", sourceAccount.getAccountNo());
			reportParams.put("sourceAccountName", sourceAccount.getAccountName());
			
			reportParams.put("benAccount", model.getBenAccountNo());
			reportParams.put("benAccountName", model.getBenAccountName());
			
			reportParams.put("transactionCurrency", model.getTransactionCurrency());
			reportParams.put("transactionAmount", df.format(model.getTransactionAmount()));
			
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
				
				InHouseTransferModel inHouseTransfer = inHouseTransferRepo.findByPendingTaskIdAndIsProcessed(pendingTaskId, ApplicationConstants.NO);
				
				inHouseTransfer.setIsProcessed(ApplicationConstants.YES);
				inHouseTransfer.setUpdatedDate(DateUtils.getCurrentTimestamp());
				inHouseTransfer.setUpdatedBy(loginUserCode);
				
				inHouseTransferRepo.save(inHouseTransfer);
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
	
	private void notifyBankEmail(InHouseTransferModel model, String appCode) {		
		
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
			
			InHouseTransferModel inhouse = inHouseTransferRepo.findByPendingTaskIdAndIsProcessed(pendingTaskId, ApplicationConstants.YES);
			
			if(status.equals("SUCCESS")) {
				isError = "N";
				pendingTaskRepo.updatePendingTask(inhouse.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS);
				trxStatusService.addTransactionStatus(inhouse.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, inhouse.getId(), false, null);
				
				// send email to beneficiary only if not execute with failure
				if (ApplicationConstants.YES.equals(inhouse.getIsNotifyBen())) {
					notifyBeneficiary(inhouse, ApplicationConstants.APP_GPCASHBO);
				}
				
			}else {
				pendingTaskRepo.updatePendingTask(inhouse.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
				trxStatusService.addTransactionStatus(inhouse.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, inhouse.getId(), true, inhouse.getErrorCode());
			}
			
			pendingTaskRepo.updateErrorTimeoutFlagPendingTask(pendingTaskId, ApplicationConstants.NO);
			
			globalTransactionService.save(inhouse.getCorporate().getId(), InHouseTransferSC.menuCode, 
					inhouse.getService().getCode(), inhouse.getReferenceNo(), 
					inhouse.getId(), inhouse.getTransactionCurrency(), inhouse.getTotalChargeEquivalentAmount(), 
					inhouse.getTransactionAmount(), 
					inhouse.getTotalDebitedEquivalentAmount(), isError);
			
			notifyTrxUsersHistory(inhouse, ApplicationConstants.APP_GPCASHBO);
			
			
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	
	@Override
	public Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		String isVirtualAccount = (String) map.get("isVirtualAccount");
		
		try {
			
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("accountNo", map.get("benAccountNo"));
			
			Map<String, Object> result = new HashMap<>(12,1);
			if (ApplicationConstants.YES.equals(isVirtualAccount)) {
				Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.TRANSFER_VA_INQUIRY, inputs);
				result.put("benAccountNo", outputs.get("accountNo"));
				result.put("benAccountName", outputs.get("accountName"));
				result.put("benAccountCurrency", outputs.get("accountCurrencyCode"));
			} else {		
				Map<String, Object> outputs = corporateAccountService.searchOnlineByAccountNo(inputs);
				result.put("benAccountNo", outputs.get("accountNo"));
				result.put("benAccountName", outputs.get("accountName"));
				result.put("benAccountCurrency", outputs.get("accountCurrencyCode"));							
			}
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
}
