package com.gpt.product.gpcash.corporate.transaction.timedeposit.withdrawal.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
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
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListInHouseModel;
import com.gpt.product.gpcash.corporate.corporate.VAStatus;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporatecharge.services.CorporateChargeService;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.repository.CorporateUserPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.timedeposit.constants.TimeDepositConstants;
import com.gpt.product.gpcash.corporate.transaction.timedeposit.placement.model.TimeDepositPlacementModel;
import com.gpt.product.gpcash.corporate.transaction.timedeposit.placement.repository.TimeDepositPlacementRepository;
import com.gpt.product.gpcash.corporate.transaction.validation.services.TransactionValidationService;
import com.gpt.product.gpcash.corporate.transactionstatus.model.TransactionStatusModel;
import com.gpt.product.gpcash.corporate.transactionstatus.repository.TransactionStatusRepository;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;
import com.gpt.product.gpcash.maintenance.timedeposit.product.model.TimeDepositProductModel;
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
public class TimeDepositWithdrawServiceImpl implements TimeDepositWithdrawService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.batch.transations.timeout-invoker}")
	private int invokerTimeout;
	
	@Value("${gpcash.batch.transations.timeout-job}")
	private int jobTimeout;

	@Value("${gpcash.batch.transations.concurrencies}")
	private int concurrencies;
	
	@Value("${gpcash.report.folder}")
	private String reportFolder;
	
	@Value("${gpcash.report.bank.logo}")
	private String bankLogo;
	
	@Value("${gpcash.report.money.format:#,###.00}")
	private String moneyFormat;
	
	@Value("${gpcash.trxstatus.download.path}")
	private String pathDownload;
	
	@Autowired
	private TimeDepositPlacementRepository placementRepo;

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
	private IDMRepository idmRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private ErrorMappingRepository errorMappingRepo;
	
	@Autowired
	private SysParamService sysParamService;
	
	@Autowired
	private EAIEngine eaiAdapter;
	
//	@Autowired
//	private TransactionStatusService trxStatusService;
	
	@Autowired
	private TransactionStatusRepository trxStatusRepo;
	
	@Autowired
	private CorporateUserPendingTaskRepository pendingTaskRepo;
	
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

			CorporateUserPendingTaskVO vo = setCorporateUserPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				TimeDepositPlacementModel modelOld = getExistingRecord((String) map.get("timeDepositId"));
				vo.setJsonObjectOld(setModelToMap(modelOld, true));
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
	
	public TimeDepositPlacementModel getExistingRecord(String timeDepositId) throws Exception {
		TimeDepositPlacementModel model = placementRepo.findOne(timeDepositId);
		
		if (model == null) {
			throw new BusinessException("GPT-0100001");
		} 

		return model;
	}
	
	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
//			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
//			String accountGroupDetaiId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
			Timestamp withdrawalDate = (Timestamp) map.get("withdrawalDate");
			Date dateWithdraw = DateUtils.getSQLDate(withdrawalDate);
			
			if(dateWithdraw.compareTo(DateUtils.getCurrentDate()) < 0){
				throw new BusinessException("GPT-0100240");
			}
		
			//------------------------------------------------------
//			CorporateAccountGroupDetailModel accountGroupDetail = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(corporateId, accountGroupDetaiId);
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
		CorporateUserModel maker = corporateUtilsRepo.isCorporateUserValid(createdBy);
		String userGroupId = maker.getCorporateUserGroup().getId();
		Timestamp withdrawalDate = (Timestamp) map.get("withdrawalDate");

		String accountGroupDetailId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
		String uniqueKey = createdBy.concat(ApplicationConstants.DELIMITER_PIPE).concat(accountGroupDetailId)
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(String.valueOf((withdrawalDate.getTime())));

//		String transactionServiceCode = (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE);
		
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		vo.setUniqueKey(uniqueKey);
		vo.setCreatedBy(createdBy);
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
//		vo.setTransactionServiceCode(transactionServiceCode);
		vo.setService("TimeDepositWithdrawSC");
		vo.setCorporateId(corporateId);
		vo.setTransactionAmount(new BigDecimal(ApplicationConstants.APPROVAL_MATRIX_NON_TRANSACTION));
		vo.setTransactionCurrency((String) map.get("principalCurrency"));
		vo.setSourceAccountGroupDetailId(accountGroupDetailId);
		
		//set userGroupId
		vo.setUserGroupId(userGroupId);
		
		CorporateAccountGroupDetailModel gdm = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(vo.getCorporateId(), 
				vo.getSourceAccountGroupDetailId());		
		
		AccountModel sourceAccount = gdm.getCorporateAccount().getAccount();
		vo.setSourceAccount(sourceAccount.getAccountNo());
		vo.setSourceAccountName(sourceAccount.getAccountName());
		
		CurrencyModel sourceCurrency = sourceAccount.getCurrency();
		vo.setSourceAccountCurrencyCode(sourceCurrency.getCode());
		vo.setSourceAccountCurrencyName(sourceCurrency.getName());
		
		String instructionMode = (String) map.get(ApplicationConstants.INSTRUCTION_MODE);
		String sessionTime = (String) map.get("sessionTime");
		Timestamp instructionDate = (Timestamp) map.get(ApplicationConstants.INSTRUCTION_DATE);
		
		vo.setInstructionMode(instructionMode);
		vo.setInstructionDate(DateUtils.getInstructionDateBySessionTime(sessionTime, instructionDate));
		
		vo.setTaskExpiryDate(transactionValidationService.validateExpiryDate(instructionMode, instructionDate));
		
		return vo;
	}
	
	@SuppressWarnings("unchecked")
	private TimeDepositPlacementModel setMapToModel(TimeDepositPlacementModel placement, Map<String, Object> map,
			boolean isNew, CorporateUserPendingTaskVO vo) throws Exception {
		CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(vo.getCreatedBy());
		CorporateUserGroupModel corporateUserGroup = corporateUser.getCorporateUserGroup();

		// set ID
		placement.setId(vo.getId());

		// set transaction information
		placement.setReferenceNo(vo.getReferenceNo());

		IDMMenuModel menu = new IDMMenuModel();
		menu.setCode(vo.getMenuCode());
		placement.setMenu(menu);
		
		CorporateModel corpModel = corporateUtilsRepo.isCorporateValid(vo.getCorporateId());
		placement.setCorporate(corpModel);
		placement.setCorporateUserGroup(corporateUserGroup);
		
		IDMApplicationModel application = new IDMApplicationModel();
		//application code diambil dr yg login
		application.setCode((String) map.get(ApplicationConstants.APP_CODE)); 
		placement.setApplication(application);

		AccountModel sourceAccountModel = productRepo.isAccountValid(vo.getSourceAccount());
		placement.setSourceAccount(sourceAccountModel);
		
		TimeDepositProductModel product = new TimeDepositProductModel();
		product.setCode((String) map.get("product"));
		placement.setProduct(product);
		placement.setDepositTermParam((Integer)(map.get("termParam")));
		placement.setDepositTermType((String) map.get("termType"));
		placement.setPrincipalCurrency((String) map.get("principalCurrency"));
		placement.setPrincipalAmount(new BigDecimal(map.get("principalAmount").toString()));
		placement.setMaturityInstruction((String) map.get("maturityInstruction"));
		placement.setPlacementDate((Timestamp) Helper.parseStringToTimestamp((String) map.get("placementDate")));
		placement.setWithdrawalDate((Timestamp) Helper.parseStringToTimestamp((String) map.get("withdrawalDate")));

		return placement;
	}



	@SuppressWarnings("unchecked")
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {

		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				TimeDepositPlacementModel placement = getExistingRecord((String) map.get("timeDepositId"));
				placement.setWithdrawalDate((Timestamp) Helper.parseStringToTimestamp((String) map.get("withdrawalDate")));
//				setMapToModel(placement, map, true, vo);
				
				saveTimeDeposit(placement, vo.getCreatedBy(), ApplicationConstants.YES);
				
				doSendToHost(placement, (String)map.get(ApplicationConstants.APP_CODE));
				
				vo.setErrorCode(placement.getErrorCode());
				vo.setIsError(placement.getIsError());
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

	private void saveTimeDeposit(TimeDepositPlacementModel placement, String createdBy, String isProcessed)
			throws ApplicationException, BusinessException {
		// set default value
		placement.setIsProcessed(isProcessed);
		placement.setCreatedDate(DateUtils.getCurrentTimestamp());
		placement.setCreatedBy(createdBy);

		placementRepo.save(placement);
	}

	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			checkCustomValidation(map);
			
			/*resultMap.putAll(corporateChargeService.getCorporateCharges((String) map.get(ApplicationConstants.APP_CODE),
					(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE),
					(String) map.get(ApplicationConstants.LOGIN_CORP_ID)));
			
			resultMap.put("totalRecord", map.get("totalRecord"));*/
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private void doSendToHost(TimeDepositPlacementModel model, String appCode) throws Exception {
		boolean isRollback = false;
		
		try {
			
			Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
			Map<String, Object> outputMap = eaiAdapter.invokeService(EAIConstants.TIME_DEPOSIT_PLACEMENT, inputs);
			
			model.setStatus(TimeDepositConstants.STATUS_WITHDRAWN);
			model.setIsError(ApplicationConstants.NO);
			model.setTimeDepositNo(ValueUtils.getValue((String) outputMap.get("timeDepositNo")));
			//---------------------------------------
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
				model.setStatus(TimeDepositConstants.STATUS_FAILED_WITHDRAWN);
				model.setIsError(ApplicationConstants.YES);
				model.setErrorCode(errorMappingModel == null ? errorMsg : errorMappingModel.getCode());
			} else {
				throw e;
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		} finally {
			//save transaction log
			
			//----------------------------------
			
			try {
				// send email to corporate email
//				notifyCorporate(model, appCode);				
				
				// send email to transaction user history
				notifyTrxUsersHistory(model, appCode);
				
			} catch(Exception e) {
				// ignore any error, just in case something bad happens
			}
		}
	}
	
	private void notifyTrxUsersHistory(TimeDepositPlacementModel model, String appCode) {
		try {
			List<String> emails = pendingTaskService.getCorporateUserEmailWithNotifyTrx(model.getPendingTaskId());
			if (emails.size() > 0) {
				Map<String, Object> inputs = new HashMap<>();
				AccountModel mainAccount = model.getSourceAccount();
				
				IDMMenuModel menu = idmRepo.isIDMMenuValid(TimeDepositWithdrawSC.menuCode);
				
				inputs.put("mainAccountNo", mainAccount.getAccountNo());
				inputs.put("mainAccountName", mainAccount.getAccountName());
				inputs.put("trxCurrencyCode", mainAccount.getCurrency().getCode());
//				inputs.put("trxAmount", model.getTotalDebitedEquivalentAmount());
//				inputs.put("totalRecord", model.getSweepInDetail().size());
//				inputs.put("totalError", model.getTotalError());
				inputs.put("menuName", menu.getName());
				inputs.put("refNo", model.getReferenceNo());
				
//				inputs.put("instructionMode", model.getInstructionMode());
//				inputs.put("instructionDate", model.getInstructionDate());
				inputs.put("status", model.getStatus());
				inputs.put("errorCode", model.getErrorCode());
				inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
				inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
				inputs.put("emails", emails);
				inputs.put("subject", menu.getName().concat(" Notification"));
				
				eaiAdapter.invokeService(EAIConstants.USER_LIQUIDITY_NOTIFICATION, inputs);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private Map<String, Object> prepareInputsForEAI(TimeDepositPlacementModel model, String appCode) {
		Map<String, Object> inputs = new HashMap<>();
		
		inputs.put("channel", appCode);
		
		AccountModel sourceAccount = model.getSourceAccount();
		inputs.put("sourceAccountNo", sourceAccount.getAccountNo());
		inputs.put("sourceAccountName", sourceAccount.getAccountName());
		inputs.put("sourceAccountCurrency", sourceAccount.getCurrency().getCode());
		inputs.put("product", model.getProduct().getCode());
		inputs.put("termParam", model.getDepositTermParam());
		inputs.put("termType", model.getDepositTermType());
		inputs.put("principalCurrency", model.getPrincipalCurrency());
		inputs.put("principalAmount", model.getPrincipalAmount());
		inputs.put("maturytyInstruction", model.getMaturityInstruction());
		inputs.put("placementDate", model.getPlacementDate());
		
		inputs.put("refNo", model.getReferenceNo());
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("inputs : " + inputs);
			logger.debug("\n\n\n\n\n");
		}
		
		return inputs;
		
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
			
			TimeDepositPlacementModel model = placementRepo.findOne(trxStatus.getEaiRefNo());
			setParamForDownloadTrxStatus(reportParams, model);
			
			String masterReportFile = reportFolder + File.separator + "TransactionStatus" + 
					File.separator + "Liquidity" + File.separator + "download-transaction-sweepin" + "-" + locale.getLanguage() + ".jasper";
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

	@SuppressWarnings("unchecked")
	private void setParamForDownloadTrxStatus(HashMap<String, Object> reportParams, TimeDepositPlacementModel model) {
		
		DecimalFormat df = new DecimalFormat(moneyFormat);
		if(model != null) {
			
			AccountModel sourceAccount = model.getSourceAccount();
			reportParams.put("sourceAccount", sourceAccount.getAccountNo());
			reportParams.put("sourceAccountName", sourceAccount.getAccountName());
			
//			reportParams.put("autoSweepBack", model.getIsAutoSweepBack());
//			reportParams.put("amountType", model.getSweepAmountType());
//			reportParams.put("totalRecord", String.valueOf(model.getSweepInDetail().size()));		
//			reportParams.put("transactionCurrency", model.getSweepCurrency());
//			
			CorporateUserPendingTaskModel pt = pendingTaskRepo.findByReferenceNo(model.getReferenceNo());
			String strValues = pt.getValuesStr();
			if (strValues != null) {
				try {
					Map<String, Object> valueMap = (Map<String, Object>) objectMapper.readValue(strValues, Class.forName(pt.getModel()));
					
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			
				
			reportParams.put("refNo", model.getReferenceNo());
		} 
		
		
	}

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			
			String loginUserCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			String loginCorporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String accountGroupDetaiId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
			
			corporateUtilsRepo.isCorporateUserValid(loginUserCode);
			
			CorporateAccountGroupDetailModel accountGroupDetail = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(loginCorporateId, accountGroupDetaiId);
			map.put("sourceAccount", accountGroupDetail.getCorporateAccount().getAccount().getAccountNo());
			map.put("corporate", loginCorporateId);
			
			Page<TimeDepositPlacementModel> result = placementRepo.search(map, PagingUtils.createPageRequest(map));

			if (ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))) {
				resultMap.put("result", setModelToMap(result.getContent(), false));
			} else if (ApplicationConstants.WF_ACTION_DETAIL.equals(map.get(ApplicationConstants.WF_ACTION))) {
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
	
	private List<Map<String, Object>> setModelToMap(List<TimeDepositPlacementModel> list, boolean isGetDetail) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (TimeDepositPlacementModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(TimeDepositPlacementModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("id", model.getId());
		map.put("sourceAccountNo", model.getSourceAccount().getAccountNo());
		map.put("sourceAccountName", model.getSourceAccount().getAccountName());
		map.put("sourceAccountCcy", model.getSourceAccount().getCurrency().getCode());
		map.put("productCode", model.getProduct().getCode());
		map.put("productName", model.getProduct().getName());
		map.put("termType", model.getDepositTermType());
		map.put("termParam", model.getDepositTermParam());
		map.put("principalAmount", model.getPrincipalAmount());
		map.put("principalCcy", model.getPrincipalCurrency());
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MMM-yyyy");
		map.put("placementDate", ValueUtils.getValue(sdfDate.format(model.getPlacementDate())));
	
		if (isGetDetail) {
			map.put("maturityInstruction", model.getMaturityInstruction());
			map.put("timeDepositNo", ValueUtils.getValue(model.getTimeDepositNo()));
			map.put("status", ValueUtils.getValue(model.getStatus()));
			map.put("referenceNo", ValueUtils.getValue(model.getReferenceNo()));
			
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
		}
		
		
		return map;
	}

	@Override
	public Map<String, Object> detailTimeDeposit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			
			String loginUserCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			corporateUtilsRepo.isCorporateUserValid(loginUserCode);
			
			TimeDepositPlacementModel tdModel = placementRepo.findOne((String) map.get("id"));
			resultMap.put("result", setModelToMap(tdModel, true));
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	
}
