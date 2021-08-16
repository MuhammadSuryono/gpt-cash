package com.gpt.product.gpcash.corporate.transaction.directdebit.directdebit.services;

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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
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
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporatecharge.model.CorporateChargeModel;
import com.gpt.product.gpcash.corporate.corporatecharge.repository.CorporateChargeRepository;
import com.gpt.product.gpcash.corporate.corporatecharge.services.CorporateChargeService;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.repository.CorporateUserPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.directdebit.directdebit.constants.DirectDebitConstants;
import com.gpt.product.gpcash.corporate.transaction.directdebit.directdebit.model.DirectDebitDetailModel;
import com.gpt.product.gpcash.corporate.transaction.directdebit.directdebit.model.DirectDebitModel;
import com.gpt.product.gpcash.corporate.transaction.directdebit.directdebit.repository.DirectDebitRepository;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.services.GlobalTransactionService;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadDetailModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.repository.PendingUploadRepository;
import com.gpt.product.gpcash.corporate.transaction.validation.services.TransactionValidationService;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionActivityType;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.product.gpcash.corporate.transactionstatus.model.TransactionStatusModel;
import com.gpt.product.gpcash.corporate.transactionstatus.repository.TransactionStatusRepository;
import com.gpt.product.gpcash.corporate.transactionstatus.services.TransactionStatusService;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;
import com.gpt.product.gpcash.servicecharge.model.ServiceChargeModel;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

@Service
@Transactional(rollbackFor = Exception.class)
public class DirectDebitServiceImpl implements DirectDebitService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${gpcash.directdebit.upload.path}")
	private String pathUpload;
	
	@Value("${gpcash.directdebit.download.sample.path}")
	private String pathDownloadSampleFile;
	
	@Value("${gpcash.directdebit.download.sample.filename}")
	private String downloadSampleFileName;	
		
	@Value("${gpcash.batch.transations.timeout-invoker}")
	private int invokerTimeout;
	
	@Value("${gpcash.batch.transations.timeout-job}")
	private int jobTimeout;

	@Value("${gpcash.batch.transations.concurrencies}")
	private int concurrencies;

	@Autowired
	private CorporateChargeService corporateChargeService;
	
	@Autowired
	private TransactionValidationService transactionValidationService;

	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	@Autowired
	private TransactionStatusService trxStatusService;	
	
	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;	
	
	@Autowired
	private PendingUploadRepository pendingUploadRepo;	
	
	@Autowired
	private DirectDebitRepository directDebitRepo;
	
	@Autowired
	private ErrorMappingRepository errorMappingRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;	
	
	@Autowired
	private TransactionStatusRepository trxStatusRepo;	
	
	@Autowired
	private CorporateChargeRepository corporateChargeRepo;	
	
	@Autowired
	private EAIEngine eaiAdapter;
	
	@Autowired
	private MessageSource message;
	
	@Autowired
	private SysParamService sysParamService;
	
	@Autowired
	private ISpringBeanInvoker invoker;	
	
	@Value("${gpcash.batch.sot.timeout}")
	private int timeout;

	@Value("${gpcash.batch.sot.batch-size}")
	private int batchSizeThreshold;		
	
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
	private CorporateUserPendingTaskRepository pendingTaskRepo;
	
	@Autowired
	private GlobalTransactionService globalTransactionService;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@Override
	public Map<String, Object> upload(Map<String, Object> map) throws ApplicationException, BusinessException {
		byte[] rawdata = (byte[])map.get("rawdata");
		String filename = (String)map.get(ApplicationConstants.FILENAME);

		if (rawdata == null)
			throw new BusinessException("GPT-0100113"); // Please select a file to upload

        try {
        	String id = Helper.generateHibernateUUIDGenerator();
            Path path = Paths.get(pathUpload + File.separator + filename + "_" + id);
            Files.write(path, rawdata);
            
			Map<String,Object> result = new HashMap<>();
			result.put("fileId", id);
			result.put(ApplicationConstants.FILENAME, filename);
			result.put("uploadDateTime", new Date());
			
			return result;
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
	}
	
	@Override
	public Map<String, Object> downloadSample(Map<String, Object> map) throws ApplicationException, BusinessException {
		String fileFormat = (String)map.get("fileFormat");		
		String filename = downloadSampleFileName + "." + fileFormat.toLowerCase();
		try {
	        Path path = Paths.get(pathDownloadSampleFile + File.separator + filename);
	        byte[] rawdata = Files.readAllBytes(path);	
			
			Map<String, Object> result = new HashMap<String, Object>();
			result.put(ApplicationConstants.FILENAME, filename);
			result.put("rawdata", rawdata);
			
			return result;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("pendingUploadId", map.get("pendingUploadId"));
		resultMap.put("fileFormat", map.get("fileFormat"));
		resultMap.put(ApplicationConstants.FILENAME, map.get(ApplicationConstants.FILENAME));
		resultMap.put("fileDescription", map.get("fileDescription"));
		resultMap.put("status", map.get("status"));
		resultMap.put("uploadDateTime", map.get("uploadDateTime"));
		resultMap.put(ApplicationConstants.INSTRUCTION_MODE, map.get(ApplicationConstants.INSTRUCTION_MODE));
		resultMap.put(ApplicationConstants.INSTRUCTION_DATE, map.get(ApplicationConstants.INSTRUCTION_DATE));
		resultMap.put(ApplicationConstants.SESSION_TIME, map.get(ApplicationConstants.SESSION_TIME));
		resultMap.put(ApplicationConstants.ACCOUNT_GRP_DTL_ID, map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID));
		resultMap.put("creditAccountNo", map.get("sourceAccountNo"));
		resultMap.put("creditAccountName", map.get("sourceAccountName"));
		resultMap.put("creditAccountCurrencyCode", map.get("sourceAccountCurrencyCode"));
		resultMap.put(ApplicationConstants.TRANS_CURRENCY, map.get(ApplicationConstants.TRANS_CURRENCY));
		resultMap.put(ApplicationConstants.TRANS_AMOUNT, map.get(ApplicationConstants.TRANS_AMOUNT));
		resultMap.put("totalError", map.get("totalError"));
		resultMap.put("details", map.get("details"));
		resultMap.put("totalRecord", map.get("totalRecord"));
		resultMap.put("totalPage", map.get("totalPage"));
				
		resultMap.putAll(corporateChargeService.getCorporateChargesPerRecord((String) map.get(ApplicationConstants.APP_CODE),
				(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE),
				(String) map.get(ApplicationConstants.LOGIN_CORP_ID),
				(Long) map.get("totalRecord")));

		return resultMap;
	}	
	
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			String instructionMode = (String)map.get(ApplicationConstants.INSTRUCTION_MODE);

			//override instructionDate if Immediate to get server timestamp
			if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
				map.put(ApplicationConstants.INSTRUCTION_DATE, DateUtils.getCurrentTimestamp());
			}
			
			checkCustomValidation(map);
			
			String pendingUploadId = (String)map.get("pendingUploadId");
			
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

			updatePendingUpload(pendingUploadId, vo.getCreatedBy(), ApplicationConstants.YES, vo.getId());
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private void checkCustomValidation(Map<String, Object> map) throws Exception {
		String pendingUploadId = (String)map.get("pendingUploadId");
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);

		validateFinalPayment(pendingUploadId, corporateId);		
		
		//TODO validate mandate still valid or not
	}
	
	private void validateFinalPayment(String pendingUploadId, String corporateId) throws Exception {
		List<PendingUploadDetailModel> existingDtls = pendingUploadRepo.findTransactionForFinalPaymentFlag(pendingUploadId, corporateId);
		if (existingDtls.size() > 0) {			
			// put to the map to get better perfomance to check each existing sender ref no
			Map<String, String> existingRefNo = new HashMap<>();
			for (PendingUploadDetailModel dtl : existingDtls) {
				if (ValueUtils.hasValue(dtl.getSenderRefNo()))
					existingRefNo.put(dtl.getSenderRefNo(), dtl.getSenderRefNo());
			}
			
			PendingUploadModel pum = pendingUploadRepo.detailPendingUploadById(pendingUploadId);
			List<PendingUploadDetailModel> details = pum.getDetails();			
			for (PendingUploadDetailModel dtl : details) {
				if (dtl.getSenderRefNo()!=null && 
					dtl.getSenderRefNo().trim().length() > 0 && 
					existingRefNo.get(dtl.getSenderRefNo())!=null
					)
					throw new BusinessException("GPT-0100199", new String[] { dtl.getSenderRefNo() });
			}			
		}
	}
	
	private void updatePendingUpload(String pendingUploadId, String updatedBy, String isProcessed, String pendingTaskId) throws Exception {		
		PendingUploadModel model = pendingUploadRepo.detailPendingUploadById(pendingUploadId);
		
		if (model == null)
			throw new BusinessException("GPT-0100001");
		
		// set default value
		model.setIsProcessed(isProcessed);
		model.setPendingTaskId(pendingTaskId);		
		model.setUpdatedDate(DateUtils.getCurrentTimestamp());
		model.setUpdatedBy(updatedBy);

		pendingUploadRepo.save(model);
	}

	@SuppressWarnings("unchecked")
	private void saveDirectDebit(PendingUploadModel pum, CorporateUserPendingTaskVO vo, Map<String, Object> map) throws Exception {		
		CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(vo.getCreatedBy());
		CorporateUserGroupModel corporateUserGroup = corporateUser.getCorporateUserGroup();
				
		DirectDebitModel directDebitModel = new DirectDebitModel();
		directDebitModel.setId(vo.getId());
		directDebitModel.setCreatedBy(pum.getCreatedBy());
		directDebitModel.setCreatedDate(pum.getCreatedDate());
		directDebitModel.setFileDescription(pum.getFileDescription());
		directDebitModel.setFileFormat(pum.getFileFormat());
		directDebitModel.setFileName(pum.getFileName());
		directDebitModel.setIsError(ApplicationConstants.NO);
		directDebitModel.setIsHostProcessed(ApplicationConstants.NO);
		directDebitModel.setIsProcessed(ApplicationConstants.NO);
		directDebitModel.setTotalAmount(pum.getTotalAmount());
		directDebitModel.setTrxCurrencyCode(pum.getTrxCurrencyCode());
		directDebitModel.setTotalRecord(pum.getTotalRecord());
		directDebitModel.setTotalError(0);
		directDebitModel.setTotalChargeEquivalentAmount(vo.getTotalChargeEquivalentAmount());
		directDebitModel.setTotalDebitedEquivalentAmount(vo.getTotalDebitedEquivalentAmount());
		
		corporateUtilsRepo.isCorporateValid(vo.getCorporateId());

		directDebitModel.setCorporate(pum.getCorporate());
		directDebitModel.setMenu(pum.getMenu());
		directDebitModel.setService(pum.getService());
		directDebitModel.setCreditAccount(pum.getSourceAccount());
		directDebitModel.setReferenceNo(vo.getReferenceNo());
		
		directDebitModel.setInstructionMode(pum.getInstructionMode());
		Timestamp instructionDate = pum.getInstructionDate();
		
		setInstructionDate(directDebitModel, instructionDate);
		
		setCharge(directDebitModel, (ArrayList<Map<String,Object>>) map.get(ApplicationConstants.TRANS_CHARGE_LIST));
		
		directDebitModel.setCorporateUserGroup(corporateUserGroup);
		
		IDMApplicationModel application = new IDMApplicationModel();
		application.setCode((String) map.get(ApplicationConstants.APP_CODE)); 
		directDebitModel.setApplication(application);
		
		List<PendingUploadDetailModel> pumDetails = pum.getDetails();
		if (ValueUtils.hasValue(pumDetails)) {
			List<DirectDebitDetailModel> details = new ArrayList<>(pum.getDetails().size());
			int idx = 0;
			for(PendingUploadDetailModel pendingUploadModel : pumDetails) {
				DirectDebitDetailModel directDebitDetailModel = new DirectDebitDetailModel();
				directDebitDetailModel.setDebitAccountNo(pendingUploadModel.getBenAccountNo());
				directDebitDetailModel.setDebitAccountName(pendingUploadModel.getBenAccountName());
				
				directDebitDetailModel.setTrxAmount(pendingUploadModel.getTrxAmount());
				directDebitDetailModel.setTrxCurrencyCode(pendingUploadModel.getTrxCurrencyCode());
				
				directDebitDetailModel.setRemark1(pendingUploadModel.getRemark1());
				directDebitDetailModel.setRemark2(pendingUploadModel.getRemark2());
				directDebitDetailModel.setRemark3(pendingUploadModel.getRemark3());

				directDebitDetailModel.setBenRefNo(pendingUploadModel.getBenRefNo());
				directDebitDetailModel.setSenderRefNo(pendingUploadModel.getSenderRefNo());
				
				directDebitDetailModel.setBenNotificationFlag(pendingUploadModel.getBenNotificationFlag());
				directDebitDetailModel.setBenNotificationValue(pendingUploadModel.getBenNotificationValue());
				
				directDebitDetailModel.setFinalizeFlag(pendingUploadModel.getFinalizeFlag());
				
				directDebitDetailModel.setIsError(ApplicationConstants.NO);
				directDebitDetailModel.setIsHostProcessed(ApplicationConstants.NO);
				
				directDebitDetailModel.setCreatedBy(vo.getCreatedBy());
				directDebitDetailModel.setCreatedDate(DateUtils.getCurrentTimestamp());
				
				directDebitDetailModel.setDirectDebit(directDebitModel);
				
				directDebitDetailModel.setIdx(idx++);	
				
				details.add(directDebitDetailModel);
			}
			
			directDebitModel.setDirectDebitDetail(details);			
		}
		
		directDebitRepo.persist(directDebitModel);
	}
	
	private void setInstructionDate(DirectDebitModel directDebitModel, Timestamp instructionDate) throws Exception {
		
		if (directDebitModel.getInstructionMode().equals(ApplicationConstants.SI_IMMEDIATE)) {
			Calendar calInstDate = DateUtils.getEarliestDate(transactionValidationService.getInstructionDateForRelease(directDebitModel.getInstructionMode(), instructionDate));
			directDebitModel.setInstructionDate(new Timestamp(calInstDate.getTimeInMillis()));
		} else if (directDebitModel.getInstructionMode().equals(ApplicationConstants.SI_FUTURE_DATE)) {
			directDebitModel.setInstructionDate(instructionDate);
		}
	}
	
	private void setCharge(DirectDebitModel directDebitModel, List<Map<String, Object>> chargeList){
		for(int i=1; i <= chargeList.size(); i++){
			Map<String, Object> chargeMap = chargeList.get(i - 1);
			String chargeId = (String) chargeMap.get("id");
			BigDecimal value = new BigDecimal(chargeMap.get("value").toString());
			String currencyCode = (String) chargeMap.get("currencyCode");
			
			if(i == 1){
				directDebitModel.setChargeType1(chargeId);
				directDebitModel.setChargeTypeAmount1(value);
				directDebitModel.setChargeTypeCurrency1(currencyCode);
			} else if(i == 2){
				directDebitModel.setChargeType2(chargeId);
				directDebitModel.setChargeTypeAmount2(value);
				directDebitModel.setChargeTypeCurrency2(currencyCode);
			} else if(i == 3){
				directDebitModel.setChargeType3(chargeId);
				directDebitModel.setChargeTypeAmount3(value);
				directDebitModel.setChargeTypeCurrency3(currencyCode);
			} else if(i == 4){
				directDebitModel.setChargeType4(chargeId);
				directDebitModel.setChargeTypeAmount4(value);
				directDebitModel.setChargeTypeCurrency4(currencyCode);
			} else if(i == 5){
				directDebitModel.setChargeType5(chargeId);
				directDebitModel.setChargeTypeAmount5(value);
				directDebitModel.setChargeTypeCurrency5(currencyCode);
			} 
		}
	}	
	
	private CorporateUserPendingTaskVO setCorporateUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		CorporateUserModel maker = corporateUtilsRepo.isCorporateUserValid(createdBy);
		String userGroupId = maker.getCorporateUserGroup().getId();

		String accountGroupDetailId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
		BigDecimal transactionAmount = (BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT);
		String uniqueKey = createdBy.concat(ApplicationConstants.DELIMITER_PIPE).concat(accountGroupDetailId)
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(transactionAmount.toPlainString())
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(String.valueOf((DateUtils.getCurrentTimestamp().getTime())));
		
		String sessionTime = (String) map.get("sessionTime");
		BigDecimal totalCharge = (BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_CHARGE);
		BigDecimal totalDebitedAmount = transactionAmount.add(totalCharge);

		String instructionMode = (String) map.get(ApplicationConstants.INSTRUCTION_MODE);
		Timestamp instructionDate = (Timestamp) map.get(ApplicationConstants.INSTRUCTION_DATE);
		
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		vo.setUniqueKey(uniqueKey);
		vo.setCreatedBy(createdBy);
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setTransactionServiceCode((String) map.get(ApplicationConstants.TRANS_SERVICE_CODE));
		vo.setService("DirectDebitSC");
		vo.setCorporateId(corporateId);
		vo.setTransactionAmount(transactionAmount);
		vo.setTransactionCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		vo.setSourceAccountGroupDetailId(accountGroupDetailId);
		vo.setSessionTime(sessionTime);
		
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
						
		vo.setTotalChargeEquivalentAmount(totalCharge);
		vo.setTotalDebitedEquivalentAmount(totalDebitedAmount);
		vo.setInstructionMode(instructionMode);
		vo.setInstructionDate(DateUtils.getInstructionDateBySessionTime(sessionTime, instructionDate));
		
		vo.setTaskExpiryDate(transactionValidationService.validateExpiryDate(instructionMode, instructionDate));
		
		vo.setRemark1((String)map.get("fileDescription"));
		
		return vo;
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {				
				String pendingUploadId = (String)map.get("pendingUploadId");
				
				PendingUploadModel pendingUploadModel = pendingUploadRepo.findByIdAndIsProcessed(pendingUploadId, ApplicationConstants.YES);

				if (pendingUploadModel == null)
					throw new BusinessException("GPT-0100001");
				
				saveDirectDebit(pendingUploadModel, vo, map);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}

	@Override
	public CorporateUserPendingTaskVO reject(CorporateUserPendingTaskVO vo)	throws ApplicationException, BusinessException {
		return null;
	}

	@Override
	public void executeImmediateTransactionScheduler(String parameter) throws ApplicationException, BusinessException {
		try{
			Timestamp date = DateUtils.getCurrentTimestamp();
			if(ValueUtils.hasValue(parameter)){
				String[] parameterArr = parameter.split("\\|");
				
				if(ValueUtils.hasValue(parameterArr[0])){
					String[] dateArr = parameterArr[0].split("\\=");
					
					if(ValueUtils.hasValue(dateArr[1])){
						date = new Timestamp(Helper.DATE_TIME_FORMATTER.parse(dateArr[1]).getTime());
					}
				}
			}
			
			Calendar cal = DateUtils.getEarliestDate(date);

			if(logger.isDebugEnabled())
				logger.debug("Immediate date : " + new Timestamp(cal.getTimeInMillis()));
			
			List<DirectDebitModel> directDebitList = directDebitRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_IMMEDIATE, 
					new Timestamp(cal.getTimeInMillis()), new Timestamp(cal.getTimeInMillis()));
			
			executeAllBatch(directDebitList);
		} catch (BusinessException e) {
			throw e;			
		} catch (Exception e) {
			throw new ApplicationException(e);
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
				
				List<DirectDebitModel> directDebitList = directDebitRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_FUTURE_DATE, new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(directDebitList);
				
			}
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void executeAllBatch(List<DirectDebitModel> list) throws Exception {
		List<SpringBeanInvokerData<String>> jobs = new ArrayList<>();
		for(DirectDebitModel model : list) {
			jobs.add(new SpringBeanInvokerData<>(model.getId(), "DirectDebitService", "executeBatchTransaction", new Object[] {model}, jobTimeout));
		}

		SpringBeanInvokerHelper.invokeAndForget(invoker, jobs, invokerTimeout, concurrencies);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void executeBatchTransaction(DirectDebitModel directDebitModel) {
		if(logger.isDebugEnabled())
			logger.debug("Process DirectDebit : " + directDebitModel.getId() + " - " + directDebitModel.getReferenceNo());
		
		boolean success = false;
		String errorCode = null;
		Timestamp now = DateUtils.getCurrentTimestamp();
		try {
			//avoid lazy when get details
			directDebitModel = directDebitRepo.findOne(directDebitModel.getId());
			
			directDebitModel.setIsProcessed(ApplicationConstants.YES);
			directDebitModel.setUpdatedBy(ApplicationConstants.CREATED_BY_SYSTEM);
			directDebitModel.setUpdatedDate(DateUtils.getCurrentTimestamp());			
			directDebitModel.getDirectDebitDetail();
			doTransfer(directDebitModel, ApplicationConstants.APP_GPCASHIB); // TODO: GA BISA PK METHOD INI DIKARENAKAN INI BISA ROLLBACK DAN JIKA ROLLBACK MAKA TIDAK AKAN KIRIM NOTIFICATION, DAN BAGAIMANA HANDLING UPDATE LIMITNYA ? APAKAH SAMA?
			success = true;
		} catch (BusinessException e) {
			errorCode = e.getErrorCode();
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (success) {
				directDebitModel.setIsError(ApplicationConstants.NO);
				directDebitModel.setErrorCode(null);				
				for(DirectDebitDetailModel dtl : directDebitModel.getDirectDebitDetail()) {
					dtl.setIsError(ApplicationConstants.NO);
					dtl.setErrorCode(null);				
				}
				
				pendingTaskRepo.updatePendingTask(directDebitModel.getId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE);				
				trxStatusService.addTransactionStatus(directDebitModel.getId(), now, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE, null, false, null);
			} else {
				directDebitModel.setIsError(ApplicationConstants.YES);
				directDebitModel.setErrorCode(errorCode);				
				for(DirectDebitDetailModel dtl : directDebitModel.getDirectDebitDetail()) {
					dtl.setIsError(ApplicationConstants.YES);
					dtl.setErrorCode(errorCode);				
				}
				
				pendingTaskRepo.updatePendingTask(directDebitModel.getId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);				
				trxStatusService.addTransactionStatus(directDebitModel.getId(), now, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, directDebitModel.getId(), true, errorCode);
			}
			
			directDebitRepo.save(directDebitModel);
		}
	}
	
	private void doTransfer(DirectDebitModel directDebitModel, String appCode) throws Exception {
		boolean isRollback = false;
		boolean limitUpdated = false;

		try {
			//update transaction limit
			transactionValidationService.updateTransactionLimit(directDebitModel.getCorporate().getId(), 
					directDebitModel.getService().getCode(), 
					directDebitModel.getCreditAccount().getCurrency().getCode(), 
					directDebitModel.getTrxCurrencyCode(), 
					directDebitModel.getCorporateUserGroup().getId(), 
					directDebitModel.getTotalDebitedEquivalentAmount(),
					directDebitModel.getApplication().getCode());
			limitUpdated = true;

			Map<String, Object> inputs = prepareInputsForEAI(directDebitModel, appCode);
			eaiAdapter.invokeService(DirectDebitConstants.DIRECT_DEBIT_TRANSFER, inputs);
			
			directDebitModel.setStatus("GPT-0100185"); // IN-PROGRESS
			directDebitModel.setIsError(ApplicationConstants.NO);
			
		} catch (BusinessException e) {
			logger.error(e.getMessage(), e);
			String errorMsg = e.getMessage();
			if (errorMsg!=null && errorMsg.startsWith("EAI-")) {
				ErrorMappingModel errorMappingModel = errorMappingRepo.findOne(e.getMessage());
				
				if (errorMappingModel!=null && ApplicationConstants.YES.equals(errorMappingModel.getRollbackFlag())) {
					isRollback = true;
					throw e;
				}
				
				// Either no error mapping or no rollback...
				
				//jika transaksi gagal tetapi tidak di rollback maka kena error execute with failure
				directDebitModel.setStatus("GPT-0100129");
				directDebitModel.setIsError(ApplicationConstants.YES);
				directDebitModel.setErrorCode(errorMappingModel == null ? errorMsg : errorMappingModel.getCode());
			} else {
				isRollback = true;
				throw e;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			isRollback = true;
			throw new ApplicationException(e);
		} finally {
			//save transaction log
			globalTransactionService.save(directDebitModel.getCorporate().getId(), DirectDebitSC.menuCode, 
					directDebitModel.getService().getCode(), directDebitModel.getReferenceNo(), 
					directDebitModel.getId(), directDebitModel.getTrxCurrencyCode(), directDebitModel.getTotalChargeEquivalentAmount(), 
					directDebitModel.getTotalAmount(), 
					directDebitModel.getTotalDebitedEquivalentAmount(), directDebitModel.getIsError());
			//----------------------------------
			
			if(isRollback) {
				if(limitUpdated) { // reverse usage
					try {
						transactionValidationService.reverseUpdateTransactionLimit(directDebitModel.getCorporate().getId(), 
								directDebitModel.getService().getCode(), 
								directDebitModel.getCreditAccount().getCurrency().getCode(), 
								directDebitModel.getTrxCurrencyCode(), 
								directDebitModel.getCorporateUserGroup().getId(), 
								directDebitModel.getTotalDebitedEquivalentAmount(),
								directDebitModel.getApplication().getCode());
					} catch(Exception e) {
						logger.error("Failed to reverse the usage "+e.getMessage(),e);
					}
				}
			}
		}
	}
	
	private Map<String, Object> prepareInputsForEAI(DirectDebitModel model, String appCode) {
		Map<String, Object> inputs = new HashMap<>();
		
		inputs.put("channel", appCode);
		inputs.put("channelRefNo", model.getId());
		
		AccountModel creditAccountModel = model.getCreditAccount();
		
		inputs.put("creditAccountNo", creditAccountModel.getAccountNo());
		inputs.put("creditAccountName", creditAccountModel.getAccountName());
		inputs.put("creditAccountType", creditAccountModel.getAccountType().getCode());
		inputs.put("creditAccountCurrencyCode", creditAccountModel.getCurrency().getCode());
		
		inputs.put("trxCurrencyCode", model.getTrxCurrencyCode());
		inputs.put("trxAmount", model.getTotalAmount());
		
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
		
		inputs.put("totalChargeAmount", model.getTotalChargeEquivalentAmount());
		inputs.put("totalRecord", model.getTotalRecord());
				
		inputs.put("serviceCode", model.getService().getCode());
		inputs.put("serviceName", model.getService().getName());

		inputs.put("refNo", model.getReferenceNo());
		
		return inputs;
		
	}
	
	private void notifyTrxUsersHistory(DirectDebitModel model, String appCode) {
		try {
			List<String> emails = pendingTaskService.getCorporateUserEmailWithNotifyTrx(model.getId());
			if (emails.size() > 0) {
				IDMMenuModel menu = idmRepo.isIDMMenuValid(DirectDebitSC.menuCode);
				
				Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
				inputs.put("remarks", model.getFileDescription());
				inputs.put("instructionMode", model.getInstructionMode());
				inputs.put("instructionDate", model.getInstructionDate());
				inputs.put("status", model.getStatus());
				inputs.put("errorCode", model.getErrorCode());
				inputs.put("totalError", model.getTotalError());
				inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
				inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
				inputs.put("emails", emails);
				inputs.put("menuName", menu.getName());
				inputs.put("subject", menu.getName().concat(" Notification"));
				
				eaiAdapter.invokeService(EAIConstants.USER_DIRECT_DEBIT_TRANSFER_NOTIFICATION, inputs);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void executeDirectDebitResponseScheduler(String parameter) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("timeout", timeout);
			inputs.put("batchSizeThreshold", batchSizeThreshold);
			inputs.put("filename", parameter);
			
			if(logger.isDebugEnabled()) {
				logger.debug("executeDirectDebitResponseScheduler inputs : " + inputs);
			}
			
			eaiAdapter.invokeService(DirectDebitConstants.DIRECT_DEBIT_RESPONSE, inputs);
			
		} catch (BusinessException e) {
			throw e;			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}	
	
	/**
	 * called by eai
	 */	
	@Override
	public void transactionNotification(String directDebitId) throws Exception {
		DirectDebitModel directDebitModel = directDebitRepo.findByIdAndIsHostProcessed(directDebitId, ApplicationConstants.YES);
		if (directDebitModel!=null) {
			if ("GPT-0100130".equals(directDebitModel.getStatus())) {
				pendingTaskRepo.updatePendingTask(directDebitModel.getId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS);
				trxStatusService.addTransactionStatus(directDebitModel.getId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, directDebitModel.getId(), false, null);
			} else {
				String errorCode = directDebitModel.getErrorCode();
				if ("GPT-0100190".equals(directDebitModel.getStatus())) {
					pendingTaskRepo.updatePendingTask(directDebitModel.getId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_PARTIAL_SUCCESS);
					trxStatusService.addTransactionStatus(directDebitModel.getId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_PARTIAL_SUCCESS, directDebitModel.getId(), true, errorCode);			
				} else {
					pendingTaskRepo.updatePendingTask(directDebitModel.getId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
					trxStatusService.addTransactionStatus(directDebitModel.getId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, directDebitModel.getId(), true, errorCode);			
				}
			}
			
			// send email to transaction user history
			notifyTrxUsersHistory(directDebitModel, ApplicationConstants.APP_GPCASHIB);
			
			// send email to beneficiary
			AccountModel creditAccountModel = directDebitModel.getCreditAccount();			
			Map<String, Object> sourceInfo = new HashMap<>();			
			sourceInfo.put("creditAccountNo", creditAccountModel.getAccountNo());
			sourceInfo.put("creditAccountName", creditAccountModel.getAccountName());
			
			for (DirectDebitDetailModel dtl : directDebitModel.getDirectDebitDetail()) {
				if (ApplicationConstants.YES.equals(dtl.getIsHostProcessed()) && 
					ApplicationConstants.YES.equals(dtl.getBenNotificationFlag()) &&
					ApplicationConstants.NO.equals(dtl.getIsError())) {
					
					notifyBeneficiary(dtl, sourceInfo);					
				}
			}
		}
	}
	
	public void notifyBeneficiary(DirectDebitDetailModel model, Map<String, Object> sourceInfo) {
		try {
			Map<String, Object> inputs = new HashMap<>();
			inputs.putAll(sourceInfo);
			inputs.put("benAccountNo", sourceInfo.get("creditAccountNo"));
			inputs.put("benAccountName", sourceInfo.get("creditAccountName"));
			inputs.put("debitAccountNo", model.getDebitAccountNo());
			inputs.put("debitAccountName", model.getDebitAccountName());
			inputs.put("refNo", model.getBenRefNo());
			inputs.put("remark1", model.getRemark1());
			inputs.put("remark2", model.getRemark2());
			inputs.put("remark3", model.getRemark3());
			inputs.put("trxCurrencyCode", model.getTrxCurrencyCode());
			inputs.put("trxAmount", model.getTrxAmount());
			inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
			inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
			inputs.put("emails", Arrays.asList(model.getBenNotificationValue()));
			inputs.put("subject", "Beneficiary Transfer Notification");

			eaiAdapter.invokeService(EAIConstants.BENEFICIARY_TRANSFER_NOTIFICATION, inputs);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {		
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			String executedId = (String) map.get("executedId");
			TransactionStatusModel trxStatus = trxStatusRepo.findOne(executedId);			
			
			DirectDebitModel directDebitModel = directDebitRepo.findOne(trxStatus.getEaiRefNo());
			if (directDebitModel==null)
				throw new BusinessException("GPT-0100001");
			
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("channel", ApplicationConstants.APP_GPCASHIB);
			inputs.put("channelRefNo", directDebitModel.getId());
			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.RETRIEVE_TRANSACTION, inputs);
			
			Date executionDate= (Date)outputs.get("executionDate");

			Page<DirectDebitDetailModel> result = directDebitRepo.searchDirectDebitDetailById(directDebitModel.getId(), 
					PagingUtils.createPageRequest(map));

			List<Map<String, Object>> details = setDirectDebitDetailModelToMap(directDebitModel, result.getContent(), executionDate);
			
			PagingUtils.setPagingInfo(resultMap, result);
			
			resultMap.put("executedResult", details);
		
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
		
	private List<Map<String, Object>> setDirectDebitDetailModelToMap(DirectDebitModel directDebitModel, List<DirectDebitDetailModel> list, 
			Date executionDate) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (DirectDebitDetailModel dtlModel : list) {
			Map<String, Object> map = new HashMap<>();
			map.put("executedDate", executionDate);
			map.put("systemReferenceNo", directDebitModel.getId());
			map.put("debitAccount", dtlModel.getDebitAccountNo());
			map.put("debitAccountName", dtlModel.getDebitAccountName());
			map.put("creditAccount", directDebitModel.getCreditAccount().getAccountNo());
			map.put("creditAccountName", directDebitModel.getCreditAccount().getAccountName());
			
			//TODO diganti jika telah implement rate
			map.put("transactionCurrency", dtlModel.getTrxCurrencyCode());
			map.put("transactionAmount", dtlModel.getTrxAmount());
			
			map.put("debitAccountCurrency", dtlModel.getTrxCurrencyCode());
			map.put("debitTransactionCurrency", dtlModel.getTrxCurrencyCode());
			map.put("debitExchangeRate", ApplicationConstants.ZERO);
			map.put("debitEquivalentAmount", dtlModel.getTrxAmount());
			
			map.put("creditTransactionCurrency", directDebitModel.getCreditAccount().getCurrency().getName());
			map.put("creditEquivalentAmount", dtlModel.getTrxAmount());
			map.put("creditExchangeRate", ApplicationConstants.ZERO);
			
			
			//--------------------------------------------
			
			//TODO diganti jika telah implement periodical charges
			map.put("chargeAccount", ApplicationConstants.EMPTY_STRING);
			
			LinkedList<Map<String, Object>> chargeList = new LinkedList<>();
			
			Map<String, Object> chargeMap = null;
			
			if(directDebitModel.getChargeType1() != null) {
				chargeMap = new HashMap<>();
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(directDebitModel.getChargeType1());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				chargeMap.put("chargeType", serviceCharge.getName());
				chargeMap.put("chargeCurrency", directDebitModel.getChargeTypeCurrency1());
				chargeMap.put("chargeEquivalentAmount", directDebitModel.getChargeTypeAmount1());
				chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
				chargeList.add(chargeMap);
			}
			
			if(directDebitModel.getChargeType2() != null) {
				chargeMap = new HashMap<>();
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(directDebitModel.getChargeType2());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				chargeMap.put("chargeType", serviceCharge.getName());
				chargeMap.put("chargeCurrency", directDebitModel.getChargeTypeCurrency2());
				chargeMap.put("chargeEquivalentAmount", directDebitModel.getChargeTypeAmount2());
				chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
				chargeList.add(chargeMap);
			}
			
			if(directDebitModel.getChargeType3() != null) {
				chargeMap = new HashMap<>();
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(directDebitModel.getChargeType3());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				chargeMap.put("chargeType", serviceCharge.getName());
				chargeMap.put("chargeCurrency", directDebitModel.getChargeTypeCurrency3());
				chargeMap.put("chargeEquivalentAmount", directDebitModel.getChargeTypeAmount3());
				chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
				chargeList.add(chargeMap);
			}
			
			if(directDebitModel.getChargeType4() != null) {
				chargeMap = new HashMap<>();
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(directDebitModel.getChargeType4());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				chargeMap.put("chargeType", serviceCharge.getName());
				chargeMap.put("chargeCurrency", directDebitModel.getChargeTypeCurrency4());
				chargeMap.put("chargeEquivalentAmount", directDebitModel.getChargeTypeAmount4());
				chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
				chargeList.add(chargeMap);
			}
			
			if(directDebitModel.getChargeType5() != null) {
				chargeMap = new HashMap<>();
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(directDebitModel.getChargeType5());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				chargeMap.put("chargeType", serviceCharge.getName());
				chargeMap.put("chargeCurrency", directDebitModel.getChargeTypeCurrency5());
				chargeMap.put("chargeEquivalentAmount", directDebitModel.getChargeTypeAmount5());
				chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
				chargeList.add(chargeMap);
			}
			
			map.put("chargeList", chargeList);			
			
			String status = null;
			if (ApplicationConstants.NO.equals(dtlModel.getIsError()))
				status = "SUCCESS";
			else
				status = "FAILED";

			map.put("status", status);
			
			String errorCode = dtlModel.getErrorCode();
			if (ValueUtils.hasValue(errorCode))				
				map.put("errorCode", ValueUtils.getValue(message.getMessage(errorCode, null, errorCode, LocaleContextHolder.getLocale())));
			else
				map.put("errorCode", ApplicationConstants.EMPTY_STRING);
			
			resultList.add(map);
		}

		return resultList;
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
			
			DirectDebitModel model = directDebitRepo.findOne(trxStatus.getEaiRefNo());
			setParamForDownloadTrxStatus(reportParams, model);
			
			String masterReportFile = reportFolder + File.separator + "TransactionStatus" + 
					File.separator + "DirectDebit" + File.separator + "download-transaction-directdebit" + "-" + locale.getLanguage() + ".jasper";
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
	
	private void setParamForDownloadTrxStatus(Map<String, Object> reportParams, DirectDebitModel model) throws JsonParseException, JsonMappingException, ClassNotFoundException, IOException {
		DecimalFormat df = new DecimalFormat(moneyFormat);
		if(model != null) {
			reportParams.put("service", ValueUtils.getValue(model.getService().getDscp()));
			
			AccountModel creditAccount = model.getCreditAccount();
			reportParams.put("sourceAccount", creditAccount.getAccountNo());
			reportParams.put("sourceAccountName", creditAccount.getAccountName());
			
			reportParams.put(ApplicationConstants.FILENAME, model.getFileName());
			reportParams.put("fileDscp", ValueUtils.getValue(model.getFileDescription()));
			reportParams.put("totalRecord", String.valueOf(model.getTotalRecord()));
			reportParams.put("totalAmount", df.format(model.getTotalAmount()));			
			reportParams.put("transactionCurrency", model.getTrxCurrencyCode());
			
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
			reportParams.put("refNo", model.getReferenceNo());
		} 
	}
}