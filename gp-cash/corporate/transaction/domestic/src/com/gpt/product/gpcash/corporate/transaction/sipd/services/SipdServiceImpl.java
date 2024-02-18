package com.gpt.product.gpcash.corporate.transaction.sipd.services;

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
import com.gpt.product.gpcash.corporate.transaction.sipd.model.SipdModel;
import com.gpt.product.gpcash.corporate.transaction.sipd.repository.SipdRepository;
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
public class SipdServiceImpl implements SipdService{
	
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
	private SipdRepository sipdRepository;
	
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
	
	@Autowired
	private ObjectMapper objectMapper;
	
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
	
	private final static String STATUS_PENDING = "PENDING";
	
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
				SipdModel sipdModel = sipdRepository.getOne(vo.getBillingId());
				
				String transactionType = this.isDomesticOrInhouse(vo.getTransactionServiceCode());
				
				if(DOMESTIC.equals(transactionType)) {
					String transactionServiceCdForCheck = this.findSrvcCdMatchWithSIPKD(vo.getTransactionServiceCode());
					domesticTransferService.checkTransactionThreshold(vo.getTransactionAmount(), transactionServiceCdForCheck);
				}
				
				//this.setMapToModel(SipdModel, map, true, vo);
				
				
					sipdModel.setReferenceNo(vo.getReferenceNo());
					this.saveSIPKD(sipdModel, vo.getCreatedBy(), ApplicationConstants.YES);

					this.doTransfer(sipdModel, (String)map.get(ApplicationConstants.APP_CODE));
					vo.setErrorCode(sipdModel.getErrorCode());
					vo.setIsError(sipdModel.getIsError());

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
		
		SipdModel sipd = null;
		
		try {
			
			sipd = sipdRepository.findByBillIdAndCorporate_IdAndStatus(billId, corpId, STATUS_PENDING);
			
			if (sipd !=null) {
				this.prepareResultDataSIPD(resultMap, sipd);
			} else {
				throw new BusinessException("Transaction Not Exist");
			}
			
			// Validate Source Account
			CorporateAccountGroupDetailModel corpAcctGrpDtl = this.searchSourceAccountByAcctNo(corpId, userCd, sipd.getSourceAccount().getAccountNo());
			
			resultMap.put(ApplicationConstants.ACCOUNT_GRP_DTL_ID, corpAcctGrpDtl.getId());
			resultMap.put("sourceAccNo", corpAcctGrpDtl.getCorporateAccount().getAccount().getAccountNo());
			resultMap.put("sourceAccName", corpAcctGrpDtl.getCorporateAccount().getAccount().getAccountName());
			resultMap.put("sourceAccCcy", corpAcctGrpDtl.getCorporateAccount().getAccount().getCurrency().getCode());
//			
//			this.checkSourceAccountAndBenAccountCannotSame((String) resultMap.get("sourceAccNo"), (String) resultMap.get("benAccountNo"));
			
		} catch (BusinessException e) {
			
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	private void prepareResultDataSIPD(Map<String, Object> resultMap, SipdModel sipdModel) throws BusinessException, ApplicationException {
		try {
			
			resultMap.put(ApplicationConstants.APP_CODE, ApplicationConstants.APP_GPCASHIB);
			resultMap.put("instructionMode", ApplicationConstants.SI_IMMEDIATE);
			resultMap.put("instructionDate", new Timestamp(System.currentTimeMillis()));
			
			resultMap.put("billId", sipdModel.getBillId());
			resultMap.put("txId", sipdModel.getSipdId());
			resultMap.put("txPartnerId", sipdModel.getTxPartnerId());
			resultMap.put("createdDate", sipdModel.getCreatedDate());
			resultMap.put("trxAmount", sipdModel.getTransactionAmount());
			resultMap.put("trxCurrency", sipdModel.getTransactionCurrency());
			resultMap.put("trxType", sipdModel.getTrxType());
			resultMap.put("remark", sipdModel.getRemark1());

			resultMap.put("benAccountNo", sipdModel.getBenAccountNo());
			resultMap.put("benAccountName", sipdModel.getBenAccountName());
			
			Map<String, Object> senderInfoMap = objectMapper.readValue(sipdModel.getSenderInfoStr(), HashMap.class);
			Map<String, Object> recipientInfoMap = objectMapper.readValue(sipdModel.getRecipientInfoStr(), HashMap.class);
			Map<String, Object> additionalDataMap = objectMapper.readValue(sipdModel.getAdditionalDataStr(), HashMap.class);
			
			resultMap.putAll(senderInfoMap);
			resultMap.putAll(recipientInfoMap);
			resultMap.putAll(additionalDataMap);
			
			resultMap.put(ApplicationConstants.TRANS_SERVICE_CODE, ApplicationConstants.SRVC_GPT_FTR_PEMDA_SIPD);
			
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
		String billId = (String) map.get("sipdId");

		String accountGroupDetailId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
		BigDecimal transactionAmount = (BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT);
		String benId = (String) map.get(ApplicationConstants.TRANS_BEN_ID);
		String uniqueKey = billId
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(transactionAmount.toPlainString())
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(benId).concat(Helper.generateShortTransactionReferenceNo());

		String instructionMode = (String) map.get(ApplicationConstants.INSTRUCTION_MODE);
		String sessionTime = (String) map.get("sessionTime");
		Timestamp instructionDate = (Timestamp) map.get(ApplicationConstants.INSTRUCTION_DATE);
		
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		vo.setUniqueKey(uniqueKey);
		vo.setCreatedBy(createdBy);
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setTransactionServiceCode((String) map.get(ApplicationConstants.TRANS_SERVICE_CODE));
		vo.setService("SipdSC");
		vo.setCorporateId(corporateId);
		vo.setTransactionAmount(transactionAmount);
		vo.setTransactionCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		vo.setSourceAccountGroupDetailId(accountGroupDetailId);
		vo.setSessionTime(sessionTime);
		vo.setBillingId((String) map.get("sipdId"));
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
	private SipdModel setMapToModel(SipdModel SipdModel, Map<String, Object> map, boolean isNew, CorporateUserPendingTaskVO vo) throws Exception {		
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

		// set transaction information
		SipdModel.setReferenceNo(vo.getReferenceNo());
		SipdModel.setSenderRefNo(vo.getSenderRefNo());
		SipdModel.setBenRefNo(vo.getBenRefNo());
		
		IDMMenuModel menu = new IDMMenuModel();
		menu.setCode(vo.getMenuCode());
		SipdModel.setMenu(menu);
		
		CorporateModel corpModel = corporateUtilsRepo.isCorporateValid(vo.getCorporateId());
		SipdModel.setCorporate(corpModel);
		
		SipdModel.setCorporateUserGroup(corporateUserGroup);
		
		IDMApplicationModel application = new IDMApplicationModel();
		//application code diambil dr yg login
		application.setCode((String) map.get(ApplicationConstants.APP_CODE));
		SipdModel.setApplication(application);
		
		AccountModel sourceAccountModel = productRepo.isAccountValid(vo.getSourceAccount());
		SipdModel.setSourceAccount(sourceAccountModel);
		
		SipdModel.setTransactionAmount(vo.getTransactionAmount());
		SipdModel.setTransactionCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		SipdModel.setTotalChargeEquivalentAmount(vo.getTotalChargeEquivalentAmount());
		SipdModel.setTotalDebitedEquivalentAmount(vo.getTotalDebitedEquivalentAmount());
		
		// set transaction service code
		ServiceModel service = new ServiceModel();
		service.setCode(transactionServiceCode);
		SipdModel.setService(service);

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
		setBenAccountInfo(SipdModel, benId, benAccountName, benAccountCurrency, transactionServiceCode,
				vo.getCorporateId(), corporateUserGroup.getId(), address1, address2, address3, isBenResident,
				benResidentCountryCode, isBenCitizen, benCitizenCountryCode, beneficiaryTypeCode, bankCode, vo.getCreatedBy(), isDomOrIh);
		
		// set additional information
		SipdModel.setSenderRefNo((String) map.get("senderRefNo"));
		SipdModel.setIsFinalPayment((String) map.get("isFinalPayment"));
		SipdModel.setBenRefNo((String) map.get("benRefNo"));
		SipdModel.setRemark1((String) map.get("remark1"));
		SipdModel.setRemark2((String) map.get("remark2"));
		SipdModel.setRemark3((String) map.get("remark3"));
		SipdModel.setIsNotifyBen((String) map.get("isNotify"));
		SipdModel.setNotifyBenValue((String) map.get("notifyBenValue"));
		
		SipdModel.setInstructionMode(instructionMode);
		SipdModel.setInstructionDate(transactionValidationService.getInstructionDateForRelease(instructionMode, instructionDate));
		
		setCharge(SipdModel, (ArrayList<Map<String,Object>>) map.get("chargeList"));
		
		SipdModel.setChargeTo(chargeInstruction);
		
		// START set SIPKD Info
		SipdModel.setBillId((String) map.get("bill_id"));
		
		Map<String, Object> sipkdInfoMap = (Map<String, Object>) map.get("sipkdInfo");
		if (sipkdInfoMap!= null) {
			/*SipdModel.setJnsBendaharaSumber((String) sipkdInfoMap.get("JnsBendahara_Sumber"));
			SipdModel.setNipBendaharaSumber((String) sipkdInfoMap.get("NipBend_Sumber"));
			SipdModel.setJnsPenerima((String) sipkdInfoMap.get("Jns_Penerima"));
			SipdModel.setNipPenerima((String) sipkdInfoMap.get("Nip_Penerima"));
			SipdModel.setNpwpPenerima((String) sipkdInfoMap.get("NPWP_Penerima"));
			SipdModel.setTglCair(this.convertStringToStimestamp((String) sipkdInfoMap.get("TglCair")));
			SipdModel.setKdUnit((String) sipkdInfoMap.get("KdUnit"));
			SipdModel.setNamaUnit((String) sipkdInfoMap.get("NmUnit"));
			SipdModel.setNoBPK((String) sipkdInfoMap.get("NoBpk"));
			SipdModel.setTglBPK(this.convertStringToStimestamp((String) sipkdInfoMap.get("Tgl_BPK")));
			SipdModel.setKdPemda((String) sipkdInfoMap.get("Kdpemda"));
			SipdModel.setNmPemda((String) sipkdInfoMap.get("Nmpemda"));
			SipdModel.setTahun((String) sipkdInfoMap.get("Tahun"));*/
		}
		
		return SipdModel;
	}
	
	private void saveSIPKD(SipdModel sipdModel, String createdBy, String isProcessed)
			throws ApplicationException, BusinessException {
		// set default value
		sipdModel.setIsProcessed(isProcessed);
		sipdModel.setCreatedDate(DateUtils.getCurrentTimestamp());
		sipdModel.setUpdatedBy(createdBy);
		sipdRepository.save(sipdModel);
	}
	
	private void doTransfer(SipdModel model, String appCode) throws Exception {
		boolean isRollback = false;
		boolean limitUpdated = false;
		//String transactionType = this.isDomesticOrInhouse(model.getService().getCode());
		String errorMsg = ApplicationConstants.EMPTY_STRING;
		try {
			//update transaction limit
//			transactionValidationService.updateTransactionLimit(model.getCorporate().getId(), 
//					model.getService().getCode(), 
//					model.getSourceAccount().getCurrency().getCode(), 
//					model.getTransactionCurrency(), 
//					model.getCorporateUserGroup().getId(), 
//					model.getTotalDebitedEquivalentAmount(),
//					model.getApplication().getCode());
			limitUpdated = true;
			
			Map<String, Object> inputs = prepareInputsForEAI(model, appCode, "");
			Map<String, Object> outputMap = new HashMap<>();		
//			if (DOMESTIC.equals(transactionType)) {
//				outputMap = eaiAdapter.invokeService(EAIConstants.DOMESTIC_TRANSFER, inputs);
//			} else {
				outputMap = eaiAdapter.invokeService(EAIConstants.INHOUSE_TRANSFER_SIPD, inputs);
//			}
			
			//this.setHostRefNo(outputMap, model);
			
			//this.doFlaggingSIPKD(model, sipkdInfoMap, SIPKD_SUCCESS_CD, SIPKD_SUCCESS_NM);
			
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
				
				//this.doFlaggingSIPKD(model, sipkdInfoMap, SIPKD_FAILED, errorMsg);
			} else {
				isRollback = true;
				throw e;
			}				
		} catch (Exception e) {
			isRollback = true;
			throw new ApplicationException(e);
		} finally {			
			//save transaction log
			/*globalTransactionService.save(model.getCorporate().getId(), SipdSC.menuCode, 
					model.getService().getCode(), model.getReferenceNo(), 
					model.getId(), model.getTransactionCurrency(), model.getTotalChargeEquivalentAmount(), 
					model.getTransactionAmount(), 
					model.getTotalDebitedEquivalentAmount(), model.getIsError());*/
			//----------------------------------
			
			if(isRollback) {
				if(limitUpdated) {
					/*try {
						transactionValidationService.reverseUpdateTransactionLimit(model.getCorporate().getId(), 
								model.getService().getCode(), 
								model.getSourceAccount().getCurrency().getCode(), 
								model.getTransactionCurrency(), 
								model.getCorporateUserGroup().getId(), 
								model.getTotalDebitedEquivalentAmount(),
								model.getApplication().getCode());
					} catch(Exception e) {
						logger.error("Failed to reverse the usage "+e.getMessage(),e);
					}*/
				}
			} else {
				try {
					// send email to corporate email
//					notifyCorporate(model, appCode);				
					
					// send email to transaction user history
					//notifyTrxUsersHistory(model, appCode, transactionType);
					
					// send email to beneficiary only if not execute with failure
//					if (!ApplicationConstants.YES.equals(model.getIsError()) && ApplicationConstants.YES.equals(model.getIsNotifyBen())) {
//						notifyBeneficiary(model, appCode, transactionType);
//					}
				} catch(Exception e) {
					// ignore any error, just in case something bad happens
					logger.error("ERROR notifyTrxUsersHistory OR notifyBeneficiary due to="+e.getMessage());
				}
			}
		}
	
	}
	
	private void setBenAccountInfo(SipdModel SipdModel, String benId, String benAccountName, String benAccountCurrency,
			String transactionServiceCode, String corporateId, String corporateUserGroupId,
			String address1, String address2, String address3, String isBenResident, String benResidentCountryCode,
			String isBenCitizen, String benCitizenCountryCode, String beneficiaryTypeCode, String bankCode,
			String createdBy, String isDomOrIh) throws Exception {
		
		String benAccountNo = null;
		DomesticBankModel domBankModel = null;		
		// benId from third party account
		benAccountNo = benId;
		
		SipdModel.setBenAccountNo(benAccountNo);
		SipdModel.setBenAccountName(benAccountName);
		SipdModel.setBenAccountCurrency(benAccountCurrency);
		
		if (DOMESTIC.equals(isDomOrIh)) {
			// check the bank first
			domBankModel = maitenanceRepo.isDomesticBankValid(bankCode);
			
			SipdModel.setBenAddr1(address1);
			SipdModel.setBenAddr2(address2);
			SipdModel.setBenAddr3(address3);
			SipdModel.setLldIsBenResidence(isBenResident);
			
			String localCountryCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_COUNTRY_CODE).getValue();
			
			if(ApplicationConstants.YES.equals(isBenResident)) {
				benResidentCountryCode = localCountryCode;
			}
			
			CountryModel benResidentCountry = new CountryModel();
			benResidentCountry.setCode(benResidentCountryCode);
			SipdModel.setLldBenResidenceCountry(benResidentCountry);
			
			SipdModel.setLldIsBenCitizen(isBenCitizen);
			
			if(ApplicationConstants.YES.equals(isBenCitizen)) {
				benCitizenCountryCode = localCountryCode;
			}
			CountryModel benCitizenCountry = new CountryModel();
			benCitizenCountry.setCode(benCitizenCountryCode);
			SipdModel.setLldBenCitizenCountry(benCitizenCountry);
	
			SipdModel.setBenDomesticBankCode(domBankModel);		
	
			BeneficiaryTypeModel benType = new BeneficiaryTypeModel();
			benType.setCode(beneficiaryTypeCode);
			SipdModel.setBenType(benType);
		}
	}
	
	private void setCharge(SipdModel SipdModel, List<Map<String, Object>> chargeList){
		for(int i=1; i <= chargeList.size(); i++){
			Map<String, Object> chargeMap = chargeList.get(i - 1);
			String chargeId = (String) chargeMap.get("id");
			BigDecimal value = new BigDecimal(chargeMap.get("value").toString());
			String currencyCode = (String) chargeMap.get("currencyCode");
			
			if(i == 1){
				SipdModel.setChargeType1(chargeId);
				SipdModel.setChargeTypeAmount1(value);
				SipdModel.setChargeTypeCurrency1(currencyCode);
			} else if(i == 2){
				SipdModel.setChargeType2(chargeId);
				SipdModel.setChargeTypeAmount2(value);
				SipdModel.setChargeTypeCurrency2(currencyCode);
			} else if(i == 3){
				SipdModel.setChargeType3(chargeId);
				SipdModel.setChargeTypeAmount3(value);
				SipdModel.setChargeTypeCurrency3(currencyCode);
			} else if(i == 4){
				SipdModel.setChargeType4(chargeId);
				SipdModel.setChargeTypeAmount4(value);
				SipdModel.setChargeTypeCurrency4(currencyCode);
			} else if(i == 5){
				SipdModel.setChargeType5(chargeId);
				SipdModel.setChargeTypeAmount5(value);
				SipdModel.setChargeTypeCurrency5(currencyCode);
			} 
		}
	}
	
	private Map<String, Object> prepareInputsForEAI(SipdModel model, String appCode, String transactionType) throws Exception {
		Map<String, Object> inputs = new HashMap<>();
		
		inputs.put("channel", appCode);
//		inputs.put("channelRefNo", model.getId());

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
		
		inputs.put("remark1", model.getRemark1());
		inputs.put("remark2", model.getRemark2());
		inputs.put("remark3", model.getRemark3());
		
		inputs.put("refNo", model.getReferenceNo());
		inputs.put("senderRefNo", model.getSenderRefNo());
		inputs.put("benRefNo", model.getBenRefNo());
		
		inputs.put("servicingBranch", "010");
		inputs.put("corpId", "GDBANK");
		inputs.put("userId", "pocgw");
		
		Map<String, Object> txadditionalDataMap = objectMapper.readValue(model.getAdditionalDataStr(), HashMap.class);
		inputs.put("totalAmount", txadditionalDataMap.get("nominal_sp2d").toString());
		
		List jurnalList = new ArrayList();
		Map jurnal = new HashMap();		
		jurnal.put("sourceAccount", sourceAccountModel.getAccountNo());
		jurnal.put("amount",  model.getTransactionAmount().toPlainString());
		jurnal.put("remarks",  model.getRemark1());
		jurnal.put("txId",  model.getReferenceNo().substring(model.getReferenceNo().length()-9));
		jurnal.put("destinationAccount",  model.getBenAccountNo());
		jurnalList.add(jurnal);
		
		Map<String, Object> senderMap = objectMapper.readValue(model.getSenderInfoStr(), HashMap.class);	
		Map additionalDataMap = (Map)senderMap.get("additional_data");
		if(additionalDataMap.get("pajak")!=null) {
			List<Map> pajakList = (List<Map>)additionalDataMap.get("pajak");
			for (Map pajakMap : pajakList) {
				jurnal = new HashMap();		
				jurnal.put("sourceAccount", sourceAccountModel.getAccountNo());
				jurnal.put("amount", pajakMap.get("nominal_pajak").toString());
				jurnal.put("remarks",  pajakMap.get("nomor_object_pajak").toString());
				jurnal.put("txId",  model.getReferenceNo().substring(model.getReferenceNo().length()-9));
				jurnal.put("destinationAccount", "01802026500343");
				jurnalList.add(jurnal);
			}
		}
		
		if(additionalDataMap.get("potongan")!=null) {
			List<Map> potonganList = (List<Map>)additionalDataMap.get("potongan");
			for (Map potonganMap : potonganList) {
				if(!potonganMap.get("nominal").toString().equals("0")) {
				jurnal = new HashMap();		
				jurnal.put("sourceAccount", sourceAccountModel.getAccountNo());
				jurnal.put("amount", potonganMap.get("nominal").toString());
				jurnal.put("remarks", potonganMap.get("nama_potongan").toString());
				jurnal.put("txId",  model.getReferenceNo().substring(model.getReferenceNo().length()-9));
				jurnal.put("destinationAccount", "01802026500343");
				jurnalList.add(jurnal);
				}
			}
		}

		
		inputs.put("jurnalList", jurnalList);
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("inputs : " + inputs);
			logger.debug("\n\n\n\n\n");
		}
		
		return inputs;
		
	}
	
	private void notifyTrxUsersHistory(SipdModel model, String appCode, String transactionType) {
		try {
			List<String> emails = pendingTaskService.getCorporateUserEmailWithNotifyTrx(model.getPendingTaskId());
			if (emails.size() > 0) {
				
				IDMMenuModel menu = idmRepo.isIDMMenuValid(SipdSC.menuCode);
				
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
	
	private void notifyBeneficiary(SipdModel model, String appCode, String transactionType) {		
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
				SipdModel model = sipdRepository.findOne(trxStatus.getEaiRefNo());
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
	
	private List<Map<String, Object>> prepareDetailTransactionMap(SipdModel model, TransactionStatusModel trxStatus) throws Exception {
		List<Map<String, Object>> executedTransactionList = new LinkedList<>();
		
		AccountModel sourceAccount = model.getSourceAccount();
		
		Map<String, Object> modelMap = new HashMap<>();
		modelMap.put("executedDate", trxStatus.getActivityDate());
//		modelMap.put("systemReferenceNo", model.getId());
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
			
			SipdModel model = sipdRepository.findOne(trxStatus.getEaiRefNo());
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
	
	private void setParamForDownloadTrxStatus(Map<String, Object> reportParams, SipdModel model) throws Exception {
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
	private Map<String, Object> doFlaggingSIPKD(SipdModel model, Map<String, Object> inputMap, String status, String msgStatus) {
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
//				model.setNotifyToSIPKDStatus(status);
//				model.setNotifyToSIPKDStatusName(msgStatus);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (model != null) {
//				model.setNotifyToSIPKDStatus(status);
//				model.setNotifyToSIPKDStatusName(e.getMessage());
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
	
	private void setHostRefNo(Map<String, Object> outputMap, SipdModel model) {
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
