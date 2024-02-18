package com.gpt.product.gpcash.retail.transaction.timedeposit.placement.services;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.maintenance.errormapping.model.ErrorMappingModel;
import com.gpt.component.maintenance.errormapping.repository.ErrorMappingRepository;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.maintenance.timedepositretail.product.model.CustomerTimeDepositProductModel;
import com.gpt.product.gpcash.retail.CustomerUtilsRepository;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.customeraccount.model.CustomerAccountModel;
import com.gpt.product.gpcash.retail.customercharge.services.CustomerChargeService;
import com.gpt.product.gpcash.retail.pendingtaskuser.model.CustomerUserPendingTaskModel;
import com.gpt.product.gpcash.retail.pendingtaskuser.repository.CustomerUserPendingTaskRepository;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserPendingTaskService;
import com.gpt.product.gpcash.retail.pendingtaskuser.valueobject.CustomerUserPendingTaskVO;
import com.gpt.product.gpcash.retail.transaction.domestic.services.CustomerDomesticTransferSC;
import com.gpt.product.gpcash.retail.transaction.timedeposit.constants.CustomerTimeDepositConstants;
import com.gpt.product.gpcash.retail.transaction.timedeposit.placement.model.CustomerTimeDepositPlacementModel;
import com.gpt.product.gpcash.retail.transaction.timedeposit.placement.repository.CustomerTimeDepositPlacementRepository;
import com.gpt.product.gpcash.retail.transaction.validation.services.CustomerTransactionValidationService;
import com.gpt.product.gpcash.retail.transactionstatus.model.CustomerTransactionStatusModel;
import com.gpt.product.gpcash.retail.transactionstatus.repository.CustomerTransactionStatusRepository;
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
public class CustomerTimeDepositPlacementServiceImpl implements CustomerTimeDepositPlacementService {
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
	private CustomerTimeDepositPlacementRepository placementRepo;

	@Autowired
	private CustomerUtilsRepository customerUtilsRepo;

	@Autowired
	private CustomerUserPendingTaskService pendingTaskService;

	@Autowired
	private CustomerChargeService customerChargeService;

	@Autowired
	private CustomerTransactionValidationService transactionValidationService;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private ErrorMappingRepository errorMappingRepo;
	
//	@Autowired
//	private SysParamService sysParamService;
	
	@Autowired
	private EAIEngine eaiAdapter;
	
//	@Autowired
//	private TransactionStatusService trxStatusService;
	
	@Autowired
	private CustomerTransactionStatusRepository trxStatusRepo;
	
	@Autowired
	private CustomerUserPendingTaskRepository pendingTaskRepo;
	
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
			
//			checkCustomValidation(map);

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
//			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			//------------------------------------------------------
			Timestamp placementDate = (Timestamp) map.get("placementDate");
			Date datePlace = DateUtils.getSQLDate(placementDate);
			
			if(datePlace.compareTo(DateUtils.getCurrentDate()) < 0){
				throw new BusinessException("GPT-0100239");
			}
			
//			String mainAccount = customerUtilsRepo.getCorporateAccountGroupRepo().findAccountNoByAccountGroupDetailId(corporateId, (String) map.get(ApplicationConstants.ACCOUNT_DTL_ID));
		
			// TODO implement calculate equivalent amount in future if implement
			// cross currency transaction
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	private CustomerUserPendingTaskVO setCustomerUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String customerId = (String) map.get(ApplicationConstants.CUST_ID);
		
		String accountDetailId = (String) map.get(ApplicationConstants.ACCOUNT_DTL_ID);
		String uniqueKey = customerId.concat(ApplicationConstants.DELIMITER_PIPE).concat(accountDetailId)
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(String.valueOf((DateUtils.getCurrentTimestamp().getTime())));

		String instructionMode = (String) map.get(ApplicationConstants.INSTRUCTION_MODE);
		String sessionTime = (String) map.get("sessionTime");
		Timestamp instructionDate = (Timestamp) map.get(ApplicationConstants.INSTRUCTION_DATE);
		String transactionServiceCode = (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE);
		
		CustomerUserPendingTaskVO vo = new CustomerUserPendingTaskVO();
		vo.setUniqueKey(uniqueKey);
		vo.setCreatedBy(customerId);
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setTransactionServiceCode(transactionServiceCode);
		vo.setService("CustomerTimeDepositPlacementSC");
		vo.setCustomerId(customerId);
		vo.setTransactionAmount(new BigDecimal(ApplicationConstants.APPROVAL_MATRIX_NON_TRANSACTION));
		vo.setTransactionCurrency((String) map.get("principalCurrency"));
		vo.setSourceAccountDetailId(accountDetailId);
		vo.setSessionTime(sessionTime);
		
		CustomerAccountModel ca = customerUtilsRepo.isCustomerAccountIdValid(vo.getCustomerId(), vo.getSourceAccountDetailId());	
		
		//set debit info
		AccountModel sourceAccount = ca.getAccount();
		vo.setSourceAccount(sourceAccount.getAccountNo());
		vo.setSourceAccountName(sourceAccount.getAccountName());
		
		CurrencyModel sourceCurrency = sourceAccount.getCurrency();
		vo.setSourceAccountCurrencyCode(sourceCurrency.getCode());
		vo.setSourceAccountCurrencyName(sourceCurrency.getName());
		
		
		//------------------------------------------------------------------
		
		vo.setInstructionMode(instructionMode);
		vo.setInstructionDate(DateUtils.getInstructionDateBySessionTime(sessionTime, instructionDate));
		
		vo.setTaskExpiryDate(transactionValidationService.validateExpiryDate(instructionMode, instructionDate));
		
		return vo;
	}
	
	@SuppressWarnings("unchecked")
	private CustomerTimeDepositPlacementModel setMapToModel(CustomerTimeDepositPlacementModel placement, Map<String, Object> map, boolean isNew, CustomerUserPendingTaskVO vo) throws Exception {
		CustomerModel custModel = customerUtilsRepo.isCustomerValid(vo.getCustomerId());
		
		// set ID
		placement.setId(vo.getId());

		// set transaction information
		placement.setReferenceNo(vo.getReferenceNo());

		IDMMenuModel menu = new IDMMenuModel();
		menu.setCode(vo.getMenuCode());
		placement.setMenu(menu);
		
		placement.setCustomer(custModel);
		
		IDMApplicationModel application = new IDMApplicationModel();
		//application code diambil dr yg login
		application.setCode((String) map.get(ApplicationConstants.APP_CODE)); 
		placement.setApplication(application);

		AccountModel sourceAccountModel = productRepo.isAccountValid(vo.getSourceAccount());
		placement.setSourceAccount(sourceAccountModel);
		
		CustomerTimeDepositProductModel product = new CustomerTimeDepositProductModel();
		product.setCode((String) map.get("product"));
		placement.setProduct(product);
		placement.setDepositTermParam((Integer)(map.get("termParam")));
		placement.setDepositTermType((String) map.get("termType"));
		placement.setPrincipalCurrency((String) map.get("principalCurrency"));
		placement.setPrincipalAmount(new BigDecimal(map.get("principalAmount").toString()));
		placement.setMaturityInstruction((String) map.get("maturityInstruction"));
//		placement.setPlacementDate((Timestamp) Helper.parseStringToTimestamp((String) map.get("placementDate")));
		placement.setPlacementDate((Timestamp) map.get("placementDate"));
		
		placement.setIsDepositorSame((String) map.get("isDepositorSame"));
		placement.setDepositorName((String) map.get("depositorName"));
		
		placement.setIsPrincipalSame((String) map.get("isPrincipalSame"));
		if (placement.getIsPrincipalSame() != null && placement.getIsPrincipalSame().equals("N")) {
			
			Map<String, Object> principalInfo = (HashMap<String, Object>) map.get("principalAccountInfo");
			placement.setPrincipalAccountNo((String)principalInfo.get("accountNo"));
			placement.setPrincipalAccountName((String)principalInfo.get("accountName"));
			placement.setPrincipalAccountCurrency((String)principalInfo.get("accountCurrency"));
		}
		
		placement.setIsInterestSame((String) map.get("isInterestSame"));
		if (placement.getIsInterestSame() != null && placement.getIsInterestSame().equals("N")) {
			
			Map<String, Object> interestInfo = (HashMap<String, Object>) map.get("interestAccountInfo");
			placement.setInterestAccountNo((String)interestInfo.get("accountNo"));
			placement.setInterestAccountName((String)interestInfo.get("accountName"));
			placement.setInterestAccountCurrency((String)interestInfo.get("accountCurrency"));
		}

		return placement;
	}



	@SuppressWarnings("unchecked")
	@Override
	public CustomerUserPendingTaskVO approve(CustomerUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {

		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				CustomerTimeDepositPlacementModel placement = new CustomerTimeDepositPlacementModel();
				setMapToModel(placement, map, true, vo);
				
				saveTimeDeposit(placement, vo.getCreatedBy(), ApplicationConstants.YES);
				
				doSendToHost(placement, (String)map.get(ApplicationConstants.APP_CODE));
				
				vo.setTimeDepositNo(placement.getTimeDepositNo()); // for Mobile UI purpose
				
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
	public CustomerUserPendingTaskVO reject(CustomerUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

	private void saveTimeDeposit(CustomerTimeDepositPlacementModel placement, String createdBy, String isProcessed)
			throws ApplicationException, BusinessException {
		// set default value
		placement.setIsProcessed(isProcessed);
		placement.setCreatedDate(DateUtils.getCurrentTimestamp());
		placement.setCreatedBy(createdBy);
		
		//jika pendingTaskId null maka transaksi bukan recurring, maka pendingTaskId = model.Id
		if(placement.getPendingTaskId() == null)
			placement.setPendingTaskId(placement.getId());

		placementRepo.persist(placement);
	}

	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			checkCustomValidation(map);
			
			String isInterestSame = ValueUtils.getValue((String) map.get("isInterestSame"));
			String isPrincipalSame = ValueUtils.getValue((String) map.get("isPrincipalSame"));
			if (isInterestSame.equals("N")) {
//				AccountModel sourceAccountModel = productRepo.isAccountValid(vo.getSourceAccount());
				
				Map<String, Object> inputs = new HashMap();
				inputs.put("accountNo", ValueUtils.getValue(map.get("interestAccount")));
				Map<String, Object> outputMap = eaiAdapter.invokeService(EAIConstants.TIME_DEPOSIT_PLACEMENT_ACCOUNT_INQ, inputs);
				
				resultMap.put("interestAccountInfo", outputMap);
				
			
			}
			
			if (isPrincipalSame.equals("N")) {
//				AccountModel sourceAccountModel = productRepo.isAccountValid(vo.getSourceAccount());
				
				Map<String, Object> inputs = new HashMap();
				inputs.put("accountNo", ValueUtils.getValue(map.get("principalAccount")));
				Map<String, Object> outputMap = eaiAdapter.invokeService(EAIConstants.TIME_DEPOSIT_PLACEMENT_ACCOUNT_INQ, inputs);
				
				resultMap.put("principalAccountInfo", outputMap);
				
			
			}
			/*resultMap.putAll(customerChargeService.getCorporateCharges((String) map.get(ApplicationConstants.APP_CODE),
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
	
	private void doSendToHost(CustomerTimeDepositPlacementModel model, String appCode) throws Exception {
		boolean isRollback = false;
		
		try {
			
			Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
			Map<String, Object> outputMap = eaiAdapter.invokeService(EAIConstants.TIME_DEPOSIT_PLACEMENT, inputs);
			
			model.setStatus(CustomerTimeDepositConstants.STATUS_ACTIVE);
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
				model.setStatus(CustomerTimeDepositConstants.STATUS_FAILED_PLACEMENT);
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
	
	private void notifyTrxUsersHistory(CustomerTimeDepositPlacementModel model, String appCode) {
		try {
			CustomerModel customer = customerUtilsRepo.isCustomerValid(model.getCreatedBy());
			
			List<String> emails = new ArrayList<>();
			if(ApplicationConstants.YES.equals(customer.getIsNotifyMyTrx()) && customer.getEmail1() != null) {
				emails.add(customer.getEmail1());
				
				IDMMenuModel menu = idmRepo.isIDMMenuValid(CustomerTimeDepositPlacementSC.menuCode);
				
				Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
				inputs.put("status", model.getStatus());
				inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
				inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
				inputs.put("emails", emails);
				inputs.put("subject", menu.getName().concat(" Notification"));
				inputs.put("debitAccountNo", model.getSourceAccount().getAccountNo());
				inputs.put("debitAccountName", model.getSourceAccount().getAccountName());
				
				
				eaiAdapter.invokeService(EAIConstants.USER_TRANSFER_NOTIFICATION, inputs);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private Map<String, Object> prepareInputsForEAI(CustomerTimeDepositPlacementModel model, String appCode) {
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
		
		
		//For dummy Testing Only ==========
		String timeDepositNo = "00"+(new StringBuilder(DateUtils.getCurrentDateTime().toString().split("-")[0]).append(DateUtils.getCurrentDateTime().toString().split("-")[1]).append(
				DateUtils.getCurrentDateTime().toString().split("-")[2]).toString()).replaceAll("-", "") +Helper.getRandomString(10);
		inputs.put("timeDepositNo", timeDepositNo);
		//For dummy Testing Only ==========
		
		
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
			
			CustomerTimeDepositPlacementModel model = placementRepo.findOne(trxStatus.getEaiRefNo());
			setParamForDownloadTrxStatus(reportParams, model);
			
			String masterReportFile = reportFolder + File.separator + "CustomerTransactionStatus" + 
					File.separator + "TimeDeposit" + File.separator + "download-transaction-timedeposit" + "-" + locale.getLanguage() + ".jasper";
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
	private void setParamForDownloadTrxStatus(HashMap<String, Object> reportParams, CustomerTimeDepositPlacementModel model) {
		
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
			CustomerUserPendingTaskModel pt = pendingTaskRepo.findByReferenceNo(model.getReferenceNo());
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
	
	
}
