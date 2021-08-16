package com.gpt.product.gpcash.corporate.transaction.sipkd.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeoutException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
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
import com.gpt.component.maintenance.parametermt.services.ParameterMaintenanceService;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateaccount.services.CorporateAccountService;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.services.CorporateAccountGroupService;
import com.gpt.product.gpcash.corporate.corporatecharge.model.CorporateChargeModel;
import com.gpt.product.gpcash.corporate.corporatecharge.repository.CorporateChargeRepository;
import com.gpt.product.gpcash.corporate.corporatecharge.services.CorporateChargeService;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateuser.services.CorporateUserService;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.repository.CorporateUserPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.domestic.services.DomesticTransferService;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.services.GlobalTransactionService;
import com.gpt.product.gpcash.corporate.transaction.sipkd.model.SipkdModel;
import com.gpt.product.gpcash.corporate.transaction.sipkd.repository.SipkdRepository;
import com.gpt.product.gpcash.corporate.transaction.validation.services.TransactionValidationService;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.product.gpcash.corporate.transactionstatus.model.TransactionStatusModel;
import com.gpt.product.gpcash.corporate.transactionstatus.repository.TransactionStatusRepository;
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
public class SipkdServiceImpl implements SipkdService{
	
	@Autowired
	private EAIEngine eaiAdapter;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private CorporateAccountGroupService corporateAccountGroupService;
	
	@Autowired
	private CorporateChargeService corporateChargeService;
	
	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;
	
	@Autowired
	private TransactionValidationService transactionValidationService;
	
	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	@Autowired
	private ParameterMaintenanceService parameterMaintenanceService;
	
	@Autowired
	private CorporateUserService corporateUserService;
	
	@Autowired
	private DomesticTransferService domesticTransferService;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private MaintenanceRepository maitenanceRepo;	
	
	@Autowired
	private SipkdRepository sipkdRepository;
	
	@Autowired
	private ErrorMappingRepository errorMappingRepo;
	
	@Autowired
	private GlobalTransactionService globalTransactionService;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private CorporateUserPendingTaskRepository pendingTaskRepo;
	
	@Autowired
	private TransactionStatusRepository trxStatusRepo;
	
	@Autowired
	private CorporateChargeRepository corporateChargeRepo;
	
	@Autowired
	private CorporateAccountService corporateAccountService;
	
	@Autowired
	private MessageSource message;
	
	@Value("${gpcash.report.folder}")
	private String reportFolder;
	
	@Value("${gpcash.report.bank.logo}")
	private String bankLogo;
	
	@Value("${gpcash.report.money.format:#,###.00}")
	private String moneyFormat;
	
	@Value("${gpcash.trxstatus.download.path}")
	private String pathDownload;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final static String TRANS_SRVC = "transService";
	private final static String SIPKD_SKN = "SKN";
	private final static String SIPKD_RTGS = "RTGS";
	private final static String SIPKD_IH = "OVB";
	private final static String SIPKD_ONLINE = "ONL";
	private final static String SIPKD_PJDL = "PJDL";
	private final static String SIPKD_MPN = "MPN";
	private final static String BEN_BANK_CD = "benBankCode";
	private final static String CODE = "code";
	private final static String NAME = "name";
	private final static String OVERBOOKING = "INHOUSE";
	private final static String DOMESTIC = "DOMESTIC";
	
	private final static String SIPKD_SUCCESS_CD = "1";
	private final static String SIPKD_SUCCESS_NM = "Berhasil";
	private final static String SIPKD_FAILED = "2";
	
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		try {
			String instructionMode = (String) map.get("instructionMode");

			//override instructionDate if Immediate to get server timestamp
			if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
				map.put(ApplicationConstants.INSTRUCTION_DATE, DateUtils.getCurrentTimestamp());
			}

			vo = setCorporateUserPendingTaskVO(map);
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
			if(!"GPT-0100002".equals(e.getMessage())) {
				try {
					@SuppressWarnings("unchecked")
					Map<String, Object> sipkdInfoMap = (Map<String, Object>) map.get("sipkdInfo");
					
					this.doFlaggingSIPKD(null, sipkdInfoMap, SIPKD_FAILED, e.getMessage());
				}catch (Exception ex) {
					logger.error("ERROR doFlaggingSIPKD in Submit due to="+ex.getMessage());
				}
			}
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			if(logger.isDebugEnabled()) {
				logger.debug("map : " + map);
			}

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				SipkdModel sipkdModel = new SipkdModel();
				
				String transactionType = this.isDomesticOrInhouse(vo.getTransactionServiceCode());
				
				if(DOMESTIC.equals(transactionType)) {
					String transactionServiceCdForCheck = this.findSrvcCdMatchWithSIPKD(vo.getTransactionServiceCode());
					domesticTransferService.checkTransactionThreshold(vo.getTransactionAmount(), transactionServiceCdForCheck);
				}
				
				this.setMapToModel(sipkdModel, map, true, vo);
				
				@SuppressWarnings("unchecked")
				Map<String, Object> sipkdInfoMap = (Map<String, Object>) map.get("sipkdInfo");
				
				if(ApplicationConstants.SI_IMMEDIATE.equals(sipkdModel.getInstructionMode())) {
					this.saveSIPKD(sipkdModel, vo.getCreatedBy(), ApplicationConstants.YES);

					this.doTransfer(sipkdModel, sipkdInfoMap, (String)map.get(ApplicationConstants.APP_CODE));
					vo.setErrorCode(sipkdModel.getErrorCode());
					vo.setIsError(sipkdModel.getIsError());
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

	@SuppressWarnings("unchecked")
	@Override
	public CorporateUserPendingTaskVO reject(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			Map<String, Object> sipkdInfoMap = (Map<String, Object>) map.get("sipkdInfo");
			
			this.doFlaggingSIPKD(null, sipkdInfoMap, SIPKD_FAILED, "Reject By User");
		} catch (Exception e) {
			logger.error("FAILED to doFlaggingSIPKD during Reject");
		}
		
		return vo;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		String billId = (String) map.get("billId");
		String corpId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String userCd = (String) map.get(ApplicationConstants.LOGIN_USERID);
		
		CorporateUserModel corpUsr = corporateUserService.getCorpUserByUserCd(userCd);
		
		Map<String, Object> inputs = new HashMap<>();
		inputs.put("billId", billId);
		inputs.put("corpId", corpId);
		inputs.put("userId", corpUsr.getUserId());
		
		try {
			resultMap = eaiAdapter.invokeService(EAIConstants.SIPKD_INQUIRY, inputs);
			
			// check unique pending task
			BigDecimal transactionAmount = (BigDecimal) resultMap.get(ApplicationConstants.TRANS_AMOUNT);
			String benId = (String) resultMap.get("benAccountNo");
			String uniqueKey = billId
					.concat(ApplicationConstants.DELIMITER_PIPE).concat(transactionAmount.toPlainString())
					.concat(ApplicationConstants.DELIMITER_PIPE).concat(benId);

			String corporateId = (String) resultMap.get(ApplicationConstants.LOGIN_CORP_ID);
		
			CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
			vo.setUniqueKey(uniqueKey);
			vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
			vo.setCorporateId(corporateId);
			pendingTaskService.checkUniquePendingTask(vo);
			
			// Validate Source Account
			CorporateAccountGroupDetailModel corpAcctGrpDtl = this.searchSourceAccountByAcctNo(corpId, userCd, (String) resultMap.get("sourceAccNo"));
			
			resultMap.put(ApplicationConstants.ACCOUNT_GRP_DTL_ID, corpAcctGrpDtl.getId());
			resultMap.put("sourceAccName", corpAcctGrpDtl.getCorporateAccount().getAccount().getAccountName());
			resultMap.put("sourceAccCcy", corpAcctGrpDtl.getCorporateAccount().getAccount().getCurrency().getCode());
			resultMap.put(ApplicationConstants.LOGIN_USERCODE, userCd);
			
			this.checkSourceAccountAndBenAccountCannotSame((String) resultMap.get("sourceAccNo"), (String) resultMap.get("benAccountNo"));
			this.prepareResultDataSIPKD(resultMap);
			
		} catch (BusinessException e) {
			if(resultMap.get("sipkdInfo") != null && !"GPT-0100002".equals(e.getMessage())) {
				this.doFlaggingSIPKD(null, (Map<String, Object>) resultMap.get("sipkdInfo"), SIPKD_FAILED, e.getMessage());
			}
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	private void prepareResultDataSIPKD(Map<String, Object> resultMap) throws BusinessException, ApplicationException {
		try {
			String transactionServiceCode = ApplicationConstants.EMPTY_STRING;
			
			//Set App Code
			resultMap.put(ApplicationConstants.APP_CODE, ApplicationConstants.APP_GPCASHIB);
			
			// Instruction info always Immediate
			resultMap.put("instructionMode", ApplicationConstants.SI_IMMEDIATE);
			resultMap.put("instructionDate", new Timestamp(System.currentTimeMillis()));
			
			//SIPKD always IDR to IDR
			String currencyCode = maintenanceRepo.getSysParamRepo().findOne(SysParamConstants.LOCAL_CURRENCY_CODE).getValue();
			CurrencyModel localCurrency = maintenanceRepo.isCurrencyValid(currencyCode);
			resultMap.put("benAccountCurrencyCode", localCurrency.getCode());
			resultMap.put("benAccountCurrencyName", localCurrency.getName());
			resultMap.put("transactionCurrency", localCurrency.getCode());
			
			String transSrvc = (String) resultMap.get(TRANS_SRVC);
			if (SIPKD_SKN.equals(transSrvc) 
					|| SIPKD_RTGS.equals(transSrvc) 
					|| SIPKD_ONLINE.equals(transSrvc)) {
				
				if(SIPKD_SKN.equals(transSrvc)) {
					transactionServiceCode = ApplicationConstants.SRVC_GPT_FTR_SIPKD_DOM_LLG;
				} else if (SIPKD_RTGS.equals(transSrvc)) {
					transactionServiceCode = ApplicationConstants.SRVC_GPT_FTR_SIPKD_DOM_RTGS;
				} else if (SIPKD_ONLINE.equals(transSrvc)) {
					transactionServiceCode = ApplicationConstants.SRVC_GPT_FTR_SIPKD_DOM_ONLINE;
				}
				
				DomesticBankModel domesticBank = maintenanceRepo.isDomesticBankValid((String) resultMap.get(BEN_BANK_CD));
									
				resultMap.put("bankCode", domesticBank.getCode());
				resultMap.put("name", ValueUtils.getValue(domesticBank.getName()));
				resultMap.put("onlineBankCode", ValueUtils.getValue(domesticBank.getOnlineBankCode())); //sknCode
				resultMap.put("memberCode", ValueUtils.getValue(domesticBank.getMemberCode())); //RTGS Code
				
				resultMap.put("address1", resultMap.get("address"));
				resultMap.put("address2", "");
				resultMap.put("address3", "");
				
				String benResidentCountry = (String) resultMap.get("benResidentCountry");
				Map<String, String> benResidentMap = this.getCountryByName(benResidentCountry);
				resultMap.put("isBenResident", benResidentMap.get("isBenCitizenResident"));
				resultMap.put("benResidentCountryCode", benResidentMap.get("code"));
				resultMap.put("benResidentCountryName", benResidentMap.get("name"));
				
				String benCitizen = (String) resultMap.get("benCitizen");
				Map<String, String> benCitizenMap = this.getCountryByName(benCitizen);
				resultMap.put("isBenCitizen", benCitizenMap.get("isBenCitizenResident"));
				resultMap.put("benCitizenCountryCode", benCitizenMap.get(CODE));
				resultMap.put("benCitizenCountryName", benCitizenMap.get(NAME));
				
				String benTypeName = (String) resultMap.get("benType");
				Map<String, String> benTypMap = this.getBeneficiaryTypeByName(benTypeName);
				resultMap.put("beneficiaryTypeCode", benTypMap.get(CODE));
				resultMap.put("beneficiaryTypeName", benTypMap.get(NAME));
					
			} else if  (SIPKD_IH.equals(transSrvc)) {
				String benAccountNo = (String) resultMap.get("benAccountNo");
				
				CorporateAccountGroupDetailModel corpAccGrpDtlModel  = corporateAccountGroupService.searchCorporateAccountByAccountNoForCredit((String) resultMap.get(ApplicationConstants.LOGIN_CORP_ID), 
						(String) resultMap.get(ApplicationConstants.LOGIN_USERCODE),  benAccountNo, false);
				
				if(corpAccGrpDtlModel!=null) {
					transactionServiceCode = ApplicationConstants.SRVC_GPT_FTR_SIPKD_IH_OWN;
					AccountModel benAccount = corpAccGrpDtlModel.getCorporateAccount().getAccount();
					String benAccountName = benAccount.getAccountName();
					
					resultMap.put("benAccountName", benAccountName);
				} else {
					transactionServiceCode = ApplicationConstants.SRVC_GPT_FTR_SIPKD_IH_3RD;
					
					Map<String, Object> inputs = new HashMap<>();
					inputs.put("accountNo", benAccountNo);
					Map<String, Object> outputs = corporateAccountService.searchOnlineByAccountNo(inputs);
					
					resultMap.put("benAccountName", (String) outputs.get("accountName"));
				}
			} else if (SIPKD_PJDL.equals(transSrvc) || SIPKD_MPN.equals(transSrvc)) {	
				transactionServiceCode = ApplicationConstants.SRVC_GPT_FTR_SIPKD_BILL_PAY;
				
			} else {
				throw new BusinessException("Invalid Transaction Service");
			}
			
			resultMap.put(ApplicationConstants.TRANS_SERVICE_CODE, transactionServiceCode);
			
			resultMap.putAll(corporateChargeService.getCorporateCharges((String) resultMap.get(ApplicationConstants.APP_CODE),
					(String) resultMap.get(ApplicationConstants.TRANS_SERVICE_CODE),
					(String) resultMap.get(ApplicationConstants.LOGIN_CORP_ID)));
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private CorporateAccountGroupDetailModel searchSourceAccountByAcctNo(String corpId, String userId, String acctNo)
			throws ApplicationException, BusinessException {
		return corporateAccountGroupService.searchCorporateAccountByAccountNo(corpId, 
				userId, acctNo, true);
	}
	
	private CorporateUserPendingTaskVO setCorporateUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		CorporateUserModel maker = corporateUtilsRepo.isCorporateUserValid(createdBy);
		String userGroupId = maker.getCorporateUserGroup().getId();
		String billId = (String) map.get("bill_id");

		String accountGroupDetailId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
		BigDecimal transactionAmount = (BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT);
		String benId = (String) map.get(ApplicationConstants.TRANS_BEN_ID);
		String uniqueKey = billId
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(transactionAmount.toPlainString())
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(benId);

		String instructionMode = (String) map.get(ApplicationConstants.INSTRUCTION_MODE);
		String sessionTime = (String) map.get("sessionTime");
		Timestamp instructionDate = (Timestamp) map.get(ApplicationConstants.INSTRUCTION_DATE);
		
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		vo.setUniqueKey(uniqueKey);
		vo.setCreatedBy(createdBy);
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setTransactionServiceCode((String) map.get(ApplicationConstants.TRANS_SERVICE_CODE));
		vo.setService("SipkdSC");
		vo.setCorporateId(corporateId);
		vo.setTransactionAmount(transactionAmount);
		vo.setTransactionCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		vo.setSourceAccountGroupDetailId(accountGroupDetailId);
		vo.setSessionTime(sessionTime);
		vo.setBillingId(billId);
		
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
		
		vo.setBenAccount(benId);
		vo.setBenAccountName((String) map.get(ApplicationConstants.TRANS_BEN_ACCT_NAME));
		
		if((String) map.get(ApplicationConstants.TRANS_BEN_ACCT_CURRENCY) != null) {
			benCurrencyCode = (String) map.get(ApplicationConstants.TRANS_BEN_ACCT_CURRENCY);
		}
		
		CurrencyModel benCurrency = maintenanceRepo.isCurrencyValid(benCurrencyCode);
		vo.setBenAccountCurrencyCode(benCurrency.getCode());
		vo.setBenAccountCurrencyName(benCurrency.getName());
		
		vo.setBenBankCode((String) map.get("bankCode"));
		//------------------------------------------------------------------
		
		vo.setTotalChargeEquivalentAmount((BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_CHARGE));
		vo.setTotalDebitedEquivalentAmount((BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT));
		vo.setInstructionMode(instructionMode);
		
		vo.setInstructionDate(DateUtils.getInstructionDateBySessionTime(sessionTime, instructionDate));
		
		vo.setTaskExpiryDate(transactionValidationService.validateExpiryDate(instructionMode, instructionDate));
		
		return vo;
	}
	
	private Map<String, String> getBeneficiaryTypeByName(String benName) throws BusinessException{
		Map<String, String> resultMap = new HashMap<>();
		com.gpt.component.maintenance.parametermt.model.BeneficiaryTypeModel benTypeModel;
		try {
			benTypeModel = (com.gpt.component.maintenance.parametermt.model.BeneficiaryTypeModel) parameterMaintenanceService.getParameterMaintenanceByModelAndName("COM_MT_BENEFICIARY_TYP", benName);
		} catch (Exception e) {
			throw new BusinessException("GPT-0100041");
		}
		
		resultMap.put(CODE, benTypeModel.getCode());
		resultMap.put(NAME, benTypeModel.getName());
		return resultMap;
	}
	
	private Map<String, String> getCountryByName(String countryName) throws BusinessException {
		Map<String, String> resultMap = new HashMap<>();
		if ("WNI".equals(countryName)) {
			countryName = "INDONESIA";
		}
		
		if ("INDONESIA".equalsIgnoreCase(countryName)) {
			resultMap.put("isBenCitizenResident", ApplicationConstants.YES);
		} else {
			resultMap.put("isBenCitizenResident", ApplicationConstants.NO);
		}
		
		com.gpt.component.maintenance.parametermt.model.CountryModel countryModel;
		try {
			countryModel = (com.gpt.component.maintenance.parametermt.model.CountryModel) parameterMaintenanceService.getParameterMaintenanceByModelAndName("COM_MT_COUNTRY", countryName);
		} catch (Exception e) {
			throw new BusinessException("GPT-0100045");
		}
		
		resultMap.put(CODE, countryModel.getCode());
		resultMap.put(NAME, countryModel.getName());
		return resultMap;
	}
	
	@SuppressWarnings("unchecked")
	private SipkdModel setMapToModel(SipkdModel sipkdModel, Map<String, Object> map, boolean isNew, CorporateUserPendingTaskVO vo) throws Exception {		
		CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(vo.getCreatedBy());
		CorporateUserGroupModel corporateUserGroup = corporateUser.getCorporateUserGroup();
		
		String benId = (String) map.get(ApplicationConstants.TRANS_BEN_ID);
		String benAccountName = (String) map.get(ApplicationConstants.TRANS_BEN_ACCT_NAME);
		String benAccountCurrency = (String) map.get(ApplicationConstants.TRANS_BEN_ACCT_CURRENCY);
		String transactionServiceCode = (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE);
		String instructionMode = (String) map.get("instructionMode");
		String chargeInstruction = (String) map.get("chargeInstruction");
		
		Timestamp instructionDate = vo.getInstructionDate();
		
		String isDomOrIh = this.isDomesticOrInhouse(transactionServiceCode);
		
		// set ID
		sipkdModel.setId(vo.getId());

		// set transaction information
		sipkdModel.setReferenceNo(vo.getReferenceNo());
		sipkdModel.setSenderRefNo(vo.getSenderRefNo());
		sipkdModel.setBenRefNo(vo.getBenRefNo());
		
		IDMMenuModel menu = new IDMMenuModel();
		menu.setCode(vo.getMenuCode());
		sipkdModel.setMenu(menu);
		
		CorporateModel corpModel = corporateUtilsRepo.isCorporateValid(vo.getCorporateId());
		sipkdModel.setCorporate(corpModel);
		
		sipkdModel.setCorporateUserGroup(corporateUserGroup);
		
		IDMApplicationModel application = new IDMApplicationModel();
		//application code diambil dr yg login
		application.setCode((String) map.get(ApplicationConstants.APP_CODE));
		sipkdModel.setApplication(application);
		
		AccountModel sourceAccountModel = productRepo.isAccountValid(vo.getSourceAccount());
		sipkdModel.setSourceAccount(sourceAccountModel);
		
		sipkdModel.setTransactionAmount(vo.getTransactionAmount());
		sipkdModel.setTransactionCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		sipkdModel.setTotalChargeEquivalentAmount(vo.getTotalChargeEquivalentAmount());
		sipkdModel.setTotalDebitedEquivalentAmount(vo.getTotalDebitedEquivalentAmount());
		
		// set transaction service code
		ServiceModel service = new ServiceModel();
		service.setCode(transactionServiceCode);
		sipkdModel.setService(service);

		String address1 = (String) map.get("address1");
		String address2 = (String) map.get("address2");
		String address3 = (String) map.get("address3");
		String isBenResident = (String) map.get("isBenResident");
		String benResidentCountryCode = (String) map.get("benResidentCountryCode");
		String isBenCitizen = (String) map.get("isBenCitizen");
		String benCitizenCountryCode = (String) map.get("benCitizenCountryCode");
		String beneficiaryTypeCode = (String) map.get("beneficiaryTypeCode");
		String bankCode = (String) map.get("bankCode");
		
		// set beneficiary info
		// set benAccountNo based on benId
		setBenAccountInfo(sipkdModel, benId, benAccountName, benAccountCurrency, transactionServiceCode,
				vo.getCorporateId(), corporateUserGroup.getId(), address1, address2, address3, isBenResident,
				benResidentCountryCode, isBenCitizen, benCitizenCountryCode, beneficiaryTypeCode, bankCode, vo.getCreatedBy(), isDomOrIh);
		
		// set additional information
		sipkdModel.setSenderRefNo((String) map.get("senderRefNo"));
		sipkdModel.setIsFinalPayment((String) map.get("isFinalPayment"));
		sipkdModel.setBenRefNo((String) map.get("benRefNo"));
		sipkdModel.setRemark1((String) map.get("remark1"));
		sipkdModel.setRemark2((String) map.get("remark2"));
		sipkdModel.setRemark3((String) map.get("remark3"));
		sipkdModel.setIsNotifyBen((String) map.get("isNotify"));
		sipkdModel.setNotifyBenValue((String) map.get("notifyBenValue"));
		
		sipkdModel.setInstructionMode(instructionMode);
		sipkdModel.setInstructionDate(transactionValidationService.getInstructionDateForRelease(instructionMode, instructionDate));
		
		setCharge(sipkdModel, (ArrayList<Map<String,Object>>) map.get("chargeList"));
		
		sipkdModel.setChargeTo(chargeInstruction);
		
		// START set SIPKD Info
		sipkdModel.setBillId((String) map.get("bill_id"));
		
		Map<String, Object> sipkdInfoMap = (Map<String, Object>) map.get("sipkdInfo");
		if (sipkdInfoMap!= null) {
			sipkdModel.setJnsBendaharaSumber((String) sipkdInfoMap.get("JnsBendahara_Sumber"));
			sipkdModel.setNipBendaharaSumber((String) sipkdInfoMap.get("NipBend_Sumber"));
			sipkdModel.setJnsPenerima((String) sipkdInfoMap.get("Jns_Penerima"));
			sipkdModel.setNipPenerima((String) sipkdInfoMap.get("Nip_Penerima"));
			sipkdModel.setNpwpPenerima((String) sipkdInfoMap.get("NPWP_Penerima"));
			sipkdModel.setTglCair(this.convertStringToStimestamp((String) sipkdInfoMap.get("TglCair")));
			sipkdModel.setKdUnit((String) sipkdInfoMap.get("KdUnit"));
			sipkdModel.setNamaUnit((String) sipkdInfoMap.get("NmUnit"));
			sipkdModel.setNoBPK((String) sipkdInfoMap.get("NoBpk"));
			sipkdModel.setTglBPK(this.convertStringToStimestamp((String) sipkdInfoMap.get("Tgl_BPK")));
			sipkdModel.setKdPemda((String) sipkdInfoMap.get("Kdpemda"));
			sipkdModel.setNmPemda((String) sipkdInfoMap.get("Nmpemda"));
			sipkdModel.setTahun((String) sipkdInfoMap.get("Tahun"));
		}
		
		return sipkdModel;
	}
	
	private void saveSIPKD(SipkdModel sipkdModel, String createdBy, String isProcessed)
			throws ApplicationException, BusinessException {
		// set default value
		sipkdModel.setIsProcessed(isProcessed);
		sipkdModel.setCreatedDate(DateUtils.getCurrentTimestamp());
		sipkdModel.setCreatedBy(createdBy);
		
		//jika pendingTaskId null maka transaksi bukan recurring, maka pendingTaskId = model.Id
		if(sipkdModel.getPendingTaskId() == null)
			sipkdModel.setPendingTaskId(sipkdModel.getId());

		sipkdRepository.persist(sipkdModel);
	}
	
	private void doTransfer(SipkdModel model, Map<String, Object> sipkdInfoMap, String appCode) throws Exception {
		boolean isRollback = false;
		boolean limitUpdated = false;
		String transactionType = this.isDomesticOrInhouse(model.getService().getCode());
		String errorMsg = ApplicationConstants.EMPTY_STRING;
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
			
			Map<String, Object> inputs = prepareInputsForEAI(model, appCode, transactionType);
			Map<String, Object> outputMap = new HashMap<>();		
			if (DOMESTIC.equals(transactionType)) {
				outputMap = eaiAdapter.invokeService(EAIConstants.DOMESTIC_TRANSFER, inputs);
			} else {
				outputMap = eaiAdapter.invokeService(EAIConstants.INHOUSE_TRANSFER, inputs);
			}
			
			this.setHostRefNo(outputMap, model);
			
			this.doFlaggingSIPKD(model, sipkdInfoMap, SIPKD_SUCCESS_CD, SIPKD_SUCCESS_NM);
			
			model.setStatus("GPT-0100130");
			model.setIsError(ApplicationConstants.NO);
		/*} catch (TimeoutException e) { // if Timeout will not roll back
			logger.error("TimeoutException="+e.getMessage());
			isError = true;
			errorMsg = e.getMessage();
			isRollback = false;
			//set status to In Progress Offline
			model.setStatus("GPT-0100185");
			model.setIsError(ApplicationConstants.NO);
		} catch (SocketTimeoutException e) {
			logger.error("SocketTimeoutException="+e.getMessage());
			isError = true;
			errorMsg = e.getMessage();
			isRollback = false;
			//set status to In Progress Offline
			model.setStatus("GPT-0100185");
			model.setIsError(ApplicationConstants.NO); */
		} catch (BusinessException e) {
			errorMsg = e.getMessage();
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
				
				this.doFlaggingSIPKD(model, sipkdInfoMap, SIPKD_FAILED, errorMsg);
			} else {
				isRollback = true;
				throw e;
			}				
		} catch (Exception e) {
			isRollback = true;
			throw new ApplicationException(e);
		} finally {			
			//save transaction log
			globalTransactionService.save(model.getCorporate().getId(), SipkdSC.menuCode, 
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
					notifyTrxUsersHistory(model, appCode, transactionType);
					
					// send email to beneficiary only if not execute with failure
					if (!ApplicationConstants.YES.equals(model.getIsError()) && ApplicationConstants.YES.equals(model.getIsNotifyBen())) {
						notifyBeneficiary(model, appCode, transactionType);
					}
				} catch(Exception e) {
					// ignore any error, just in case something bad happens
					logger.error("ERROR notifyTrxUsersHistory OR notifyBeneficiary due to="+e.getMessage());
				}
			}
		}
	
	}
	
	private void setBenAccountInfo(SipkdModel sipkdModel, String benId, String benAccountName, String benAccountCurrency,
			String transactionServiceCode, String corporateId, String corporateUserGroupId,
			String address1, String address2, String address3, String isBenResident, String benResidentCountryCode,
			String isBenCitizen, String benCitizenCountryCode, String beneficiaryTypeCode, String bankCode,
			String createdBy, String isDomOrIh) throws Exception {
		
		String benAccountNo = null;
		DomesticBankModel domBankModel = null;		
		// benId from third party account
		benAccountNo = benId;
		
		sipkdModel.setBenAccountNo(benAccountNo);
		sipkdModel.setBenAccountName(benAccountName);
		sipkdModel.setBenAccountCurrency(benAccountCurrency);
		
		if (DOMESTIC.equals(isDomOrIh)) {
			// check the bank first
			domBankModel = maitenanceRepo.isDomesticBankValid(bankCode);
			
			sipkdModel.setBenAddr1(address1);
			sipkdModel.setBenAddr2(address2);
			sipkdModel.setBenAddr3(address3);
			sipkdModel.setLldIsBenResidence(isBenResident);
			
			String localCountryCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_COUNTRY_CODE).getValue();
			
			if(ApplicationConstants.YES.equals(isBenResident)) {
				benResidentCountryCode = localCountryCode;
			}
			
			CountryModel benResidentCountry = new CountryModel();
			benResidentCountry.setCode(benResidentCountryCode);
			sipkdModel.setLldBenResidenceCountry(benResidentCountry);
			
			sipkdModel.setLldIsBenCitizen(isBenCitizen);
			
			if(ApplicationConstants.YES.equals(isBenCitizen)) {
				benCitizenCountryCode = localCountryCode;
			}
			CountryModel benCitizenCountry = new CountryModel();
			benCitizenCountry.setCode(benCitizenCountryCode);
			sipkdModel.setLldBenCitizenCountry(benCitizenCountry);
	
			sipkdModel.setBenDomesticBankCode(domBankModel);		
	
			BeneficiaryTypeModel benType = new BeneficiaryTypeModel();
			benType.setCode(beneficiaryTypeCode);
			sipkdModel.setBenType(benType);
		}
	}
	
	private void setCharge(SipkdModel sipkdModel, List<Map<String, Object>> chargeList){
		for(int i=1; i <= chargeList.size(); i++){
			Map<String, Object> chargeMap = chargeList.get(i - 1);
			String chargeId = (String) chargeMap.get("id");
			BigDecimal value = new BigDecimal(chargeMap.get("value").toString());
			String currencyCode = (String) chargeMap.get("currencyCode");
			
			if(i == 1){
				sipkdModel.setChargeType1(chargeId);
				sipkdModel.setChargeTypeAmount1(value);
				sipkdModel.setChargeTypeCurrency1(currencyCode);
			} else if(i == 2){
				sipkdModel.setChargeType2(chargeId);
				sipkdModel.setChargeTypeAmount2(value);
				sipkdModel.setChargeTypeCurrency2(currencyCode);
			} else if(i == 3){
				sipkdModel.setChargeType3(chargeId);
				sipkdModel.setChargeTypeAmount3(value);
				sipkdModel.setChargeTypeCurrency3(currencyCode);
			} else if(i == 4){
				sipkdModel.setChargeType4(chargeId);
				sipkdModel.setChargeTypeAmount4(value);
				sipkdModel.setChargeTypeCurrency4(currencyCode);
			} else if(i == 5){
				sipkdModel.setChargeType5(chargeId);
				sipkdModel.setChargeTypeAmount5(value);
				sipkdModel.setChargeTypeCurrency5(currencyCode);
			} 
		}
	}
	
	private Map<String, Object> prepareInputsForEAI(SipkdModel model, String appCode, String transactionType) throws Exception {
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
		
		inputs.put("serviceCode", this.findSrvcCdMatchWithSIPKD(model.getService().getCode()));
		inputs.put("serviceName", model.getService().getName());
		
		inputs.put("refNo", model.getReferenceNo());
		inputs.put("senderRefNo", model.getSenderRefNo());
		inputs.put("benRefNo", model.getBenRefNo());
		
		if (DOMESTIC.equals(transactionType)) {
			CorporateModel corpModel = model.getCorporate();
			inputs.put("remitterAddress1", corpModel.getAddress1());
			inputs.put("remitterAddress2", corpModel.getAddress2());
			inputs.put("remitterAddress3", corpModel.getAddress3());
			inputs.put("remitterResidentFlag", corpModel.getLldIsResidence());
			inputs.put("remitterResidentCountryCode", corpModel.getResidenceCountry().getCode());
			inputs.put("remitterCitizenFlag", corpModel.getLldIsCitizen());
			inputs.put("remitterCitizenCountryCode", corpModel.getCitizenCountry().getCode());
			inputs.put("remitterType", corpModel.getLldCategory());
			
			// Find the differences and put here
			inputs.put("benAddress1", model.getBenAddr1());
			inputs.put("benAddress2", model.getBenAddr2());
			inputs.put("benAddress3", model.getBenAddr3());
			inputs.put("benResidentFlag", model.getLldIsBenResidence());
			inputs.put("benCitizenFlag", model.getLldIsBenCitizen());
			inputs.put("benResidentCountryCode", model.getLldBenResidenceCountry().getCode());
			inputs.put("benCitizenCountryCode", model.getLldBenCitizenCountry().getCode());
			inputs.put("benTypeCode", model.getBenType().getCode());
			
			inputs.put("destinationBankCode", model.getBenDomesticBankCode().getCode());
			inputs.put("destinationBankName", model.getBenDomesticBankCode().getName());
			inputs.put("destinationMemberBankCode", model.getBenDomesticBankCode().getMemberCode());
			inputs.put("destinationOnlineBankCode", model.getBenDomesticBankCode().getOnlineBankCode());
			
			inputs.put("chargeTo", model.getChargeTo());
			inputs.put("channel", model.getBenDomesticBankCode().getChannel());
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("inputs : " + inputs);
			logger.debug("\n\n\n\n\n");
		}
		
		return inputs;
		
	}
	
	private void notifyTrxUsersHistory(SipkdModel model, String appCode, String transactionType) {
		try {
			List<String> emails = pendingTaskService.getCorporateUserEmailWithNotifyTrx(model.getId());
			if (emails.size() > 0) {
				
				IDMMenuModel menu = idmRepo.isIDMMenuValid(SipkdSC.menuCode);
				
				Map<String, Object> inputs = prepareInputsForEAI(model, appCode, transactionType);
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
	
	private void notifyBeneficiary(SipkdModel model, String appCode, String transactionType) {		
		if (ValueUtils.hasValue(model.getNotifyBenValue())) {
			String[] vals = getMultipleEntries(model.getNotifyBenValue());
			if (vals!=null && vals.length>0) {
				try {
					Map<String, Object> inputs = prepareInputsForEAI(model, appCode, transactionType);
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

	private Timestamp convertStringToStimestamp(String inputString) {
		try {
	        String timeStampFormat = "yyyy-MM-dd HH:mm:ss.SSS";
			
			SimpleDateFormat tmsFormatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
			Date createdDt = tmsFormatter.parse(inputString);
			((SimpleDateFormat) tmsFormatter).applyPattern(timeStampFormat);
	
			return Timestamp.valueOf(tmsFormatter.format(createdDt));
		} catch (Exception e) {
			logger.error("ERROR convertStringToStimestamp due to="+e.getMessage(), e);
			return null;
		}
	}
	
	private String findSrvcCdMatchWithSIPKD(String sipkdSrvcCd) {
		return sipkdSrvcCd.replace("SIPKD_", "");
	}
	
	private String isDomesticOrInhouse(String srvcCd) {
		if(ApplicationConstants.SRVC_GPT_FTR_SIPKD_DOM_LLG.equals(srvcCd) 
				|| ApplicationConstants.SRVC_GPT_FTR_SIPKD_DOM_RTGS.equals(srvcCd)
				|| ApplicationConstants.SRVC_GPT_FTR_SIPKD_DOM_ONLINE.equals(srvcCd)) {
			return DOMESTIC;
		}
		
		return OVERBOOKING;
	}
	
	@Override
	public Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {

		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			String transactionStatusId = (String) map.get("executedId");
			TransactionStatusModel trxStatus = trxStatusRepo.findOne(transactionStatusId);
			
			//jika success ambil ke model nya, jika tidak maka ambil dr pending task krn blm masuk table
			if(trxStatus.getStatus().equals(TransactionStatus.EXECUTE_SUCCESS)) {
				SipkdModel model = sipkdRepository.findOne(trxStatus.getEaiRefNo());
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
	
	private List<Map<String, Object>> prepareDetailTransactionMap(SipkdModel model, TransactionStatusModel trxStatus) throws Exception {
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
			
			SipkdModel model = sipkdRepository.findOne(trxStatus.getEaiRefNo());
			setParamForDownloadTrxStatus(reportParams, model);
			
			String masterReportFile = reportFolder + File.separator + "TransactionStatus" + 
					File.separator + "SIPKD" + File.separator + "download-transaction-sipkd" + "-" + locale.getLanguage() + ".jasper";
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
	
	private void setParamForDownloadTrxStatus(Map<String, Object> reportParams, SipkdModel model) throws Exception {
		DecimalFormat df = new DecimalFormat(moneyFormat);
		
		if(model != null) {
			
			if(DOMESTIC.equals(this.isDomesticOrInhouse(model.getService().getCode()))) {
				DomesticBankModel domesticBank = model.getBenDomesticBankCode();
				reportParams.put("destinationBank", domesticBank.getName());
				reportParams.put("sknCode", ValueUtils.getValue(domesticBank.getOnlineBankCode()));
				reportParams.put("rtgsCode", ValueUtils.getValue(domesticBank.getMemberCode()));
				reportParams.put("branchCode", domesticBank.getOrganizationUnitCode());
				reportParams.put("branchName", domesticBank.getOrganizationUnitName());
			}
			
			reportParams.put("billId", model.getBillId());			
			reportParams.put("service", ValueUtils.getValue(model.getService().getDscp()));
			
			AccountModel sourceAccount = model.getSourceAccount();
			reportParams.put("sourceAccount", sourceAccount.getAccountNo());
			reportParams.put("sourceAccountName", sourceAccount.getAccountName());
			
			reportParams.put("benAccount", model.getBenAccountNo());
			reportParams.put("benAccountName", model.getBenAccountName());
			
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
		}
	}
	
	private void setCopyrightSubReport(Map<String, Object> reportParams) throws JRException {
		Locale locale = LocaleContextHolder.getLocale();
		String copyrightFile = reportFolder + File.separator + "Copyright" + File.separator + "bank-copyright" + "-" + locale.getLanguage() + ".jasper";
		JasperReport copyrightReport = (JasperReport) JRLoader.loadObject(new File(copyrightFile));
		reportParams.put("COPYRIGHT_REPORT", copyrightReport);
	}
	
	/*
	 *  Status :
	 * 	0 = Belum Diproses
	 *  1 = Berhasil
	 *  2 = Gagal
	 */
	private Map<String, Object> doFlaggingSIPKD(SipkdModel model, Map<String, Object> inputMap, String status, String msgStatus) {
		Map<String, Object> outputMap = new HashMap<>();
		try {			
			if(SIPKD_SUCCESS_CD.equals(status)) {
				inputMap.put("TglCair", new Timestamp(System.currentTimeMillis()));
			} else {
				inputMap.put("TglCair", ApplicationConstants.EMPTY_STRING);
			}
			inputMap.put("Cair", status);
			inputMap.put("KdStatusIBC", status);
			inputMap.put("MsgStatusIBC", msgStatus);
			
			outputMap = eaiAdapter.invokeService(EAIConstants.SIPKD_POST_BELANAJA, inputMap);
			
			if (model != null) {
				// Update Status after Payment
				model.setNotifyToSIPKDStatus(status);
				model.setNotifyToSIPKDStatusName(msgStatus);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (model != null) {
				model.setNotifyToSIPKDStatus(status);
				model.setNotifyToSIPKDStatusName(e.getMessage());
			}

		}
		return outputMap;
	}
	
	private void checkSourceAccountAndBenAccountCannotSame(String sourceAccount, String benAccount) throws BusinessException, Exception {
		try {
			if(sourceAccount.equals(benAccount)) {
				throw new BusinessException("GPT-0100149");
			}
		} catch (BusinessException e) {
			throw e;
		}
	}
	
	private void setHostRefNo(Map<String, Object> outputMap, SipkdModel model) {
		try {
			if (outputMap != null && outputMap.get("retrievalRefNo") != null) {
				String retrievalRefNo = (String) outputMap.get("retrievalRefNo");
				retrievalRefNo = retrievalRefNo.substring(6, 12);
				model.setHostReferenceNo(retrievalRefNo);
			}
		} catch (Exception e) {
			logger.error("ERROR setHostRefNo SIPKD due to"+ e.getMessage());
		}
	}
}
