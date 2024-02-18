package com.gpt.product.gpcash.corporate.transaction.bulkpayment.services;

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
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.maintenance.domesticbank.model.DomesticBankModel;
import com.gpt.component.maintenance.errormapping.model.ErrorMappingModel;
import com.gpt.component.maintenance.errormapping.repository.ErrorMappingRepository;
import com.gpt.component.maintenance.internationalbank.model.InternationalBankModel;
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
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporatecharge.services.CorporateChargeService;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.repository.CorporateUserPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.bulkpayment.model.BulkPaymentDetailModel;
import com.gpt.product.gpcash.corporate.transaction.bulkpayment.model.BulkPaymentModel;
import com.gpt.product.gpcash.corporate.transaction.bulkpayment.repository.BulkPaymentRepository;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.services.GlobalTransactionService;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.constants.PendingUploadConstants;
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

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

@Service
@Transactional(rollbackFor = Exception.class)
public class BulkPaymentServiceImpl implements BulkPaymentService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${gpcash.bulkpayment.upload.path}")
	private String pathUpload;
	
	@Value("${gpcash.bulkpayment.download.sample.path}")
	private String pathDownloadSampleFile;
	
	@Value("${gpcash.bulkpayment.download.sample.filename}")
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
	private BulkPaymentRepository bulkPaymentRepo;
	
	@Autowired
	private ErrorMappingRepository errorMappingRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;	
	
	@Autowired
	private TransactionStatusRepository trxStatusRepo;	
	
	@Autowired
	private EAIEngine eaiAdapter;
	
	@Autowired
	private MessageSource message;
	
	@Autowired
	private ObjectMapper objectMapper;
	
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
		
		//========================= untuk validasi nominal ketika validate file =====================
		BigDecimal sknThreshold = null;
		BigDecimal rtgsThreshold = null;
		try {
			sknThreshold = new BigDecimal(maintenanceRepo.isSysParamValid(SysParamConstants.SKN_THRESHOLD).getValue());
			rtgsThreshold = new BigDecimal(maintenanceRepo.isSysParamValid(SysParamConstants.MIN_RTGS_TRANSACTION).getValue());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		//========================= untuk validasi nominal ketika validate file =====================
		
		
        try {
        	String id = Helper.generateHibernateUUIDGenerator();
            Path path = Paths.get(pathUpload + File.separator + filename + "_" + id);
            Files.write(path, rawdata);
            
			Map<String,Object> result = new HashMap<>();
			result.put("fileId", id);
			result.put(ApplicationConstants.FILENAME, filename);
			result.put("uploadDateTime", new Date());
			
			result.put("sknThreshold", sknThreshold);
			result.put("rtgsThreshold", rtgsThreshold);
			
			
			
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
		resultMap.put("sourceAccountNo", map.get("sourceAccountNo"));
		resultMap.put("sourceAccountName", map.get("sourceAccountName"));
		resultMap.put("sourceAccountCurrencyCode", map.get("sourceAccountCurrencyCode"));
		resultMap.put(ApplicationConstants.TRANS_CURRENCY, map.get(ApplicationConstants.TRANS_CURRENCY));
		resultMap.put(ApplicationConstants.TRANS_AMOUNT, map.get(ApplicationConstants.TRANS_AMOUNT));
		resultMap.put("totalError", map.get("totalError"));
		resultMap.put("details", map.get("details"));
		resultMap.put("totalRecord", map.get("totalRecord"));
		resultMap.put("totalPage", map.get("totalPage"));
		resultMap.put("totalPage", map.get("totalPage"));
		resultMap.put("transactionServiceName", map.get("transactionServiceName"));
				
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
	private void saveBulkPayment(PendingUploadModel pum, CorporateUserPendingTaskVO vo, Map<String, Object> map) throws Exception {		
		CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(vo.getCreatedBy());
		CorporateUserGroupModel corporateUserGroup = corporateUser.getCorporateUserGroup();
				
		BulkPaymentModel bulkPaymentModel = new BulkPaymentModel();
		bulkPaymentModel.setId(vo.getId());
		bulkPaymentModel.setCreatedBy(pum.getCreatedBy());
		bulkPaymentModel.setCreatedDate(pum.getCreatedDate());
		bulkPaymentModel.setFileDescription(pum.getFileDescription());
		bulkPaymentModel.setFileFormat(pum.getFileFormat());
		bulkPaymentModel.setFileName(pum.getFileName());
		bulkPaymentModel.setIsError(ApplicationConstants.NO);
		bulkPaymentModel.setIsHostProcessed(ApplicationConstants.NO);
		bulkPaymentModel.setIsProcessed(ApplicationConstants.NO);
		bulkPaymentModel.setTotalAmount(pum.getTotalAmount());
		bulkPaymentModel.setTrxCurrencyCode(pum.getTrxCurrencyCode());
		bulkPaymentModel.setTotalRecord(pum.getTotalRecord());
		bulkPaymentModel.setTotalError(0);
		bulkPaymentModel.setTotalChargeEquivalentAmount(vo.getTotalChargeEquivalentAmount());
		bulkPaymentModel.setTotalDebitedEquivalentAmount(vo.getTotalDebitedEquivalentAmount());
		
		corporateUtilsRepo.isCorporateValid(vo.getCorporateId());

		bulkPaymentModel.setCorporate(pum.getCorporate());
		bulkPaymentModel.setMenu(pum.getMenu());
		bulkPaymentModel.setService(pum.getService());
		bulkPaymentModel.setSourceAccount(pum.getSourceAccount());
		bulkPaymentModel.setReferenceNo(vo.getReferenceNo());
		bulkPaymentModel.setIsSummaryOpt((String)map.get("isSummary"));
		
		bulkPaymentModel.setInstructionMode(pum.getInstructionMode());
		Timestamp instructionDate = pum.getInstructionDate();
		
		setInstructionDate(bulkPaymentModel, instructionDate);
		
		setCharge(bulkPaymentModel, (ArrayList<Map<String,Object>>) map.get(ApplicationConstants.TRANS_CHARGE_LIST));
		
		bulkPaymentModel.setCorporateUserGroup(corporateUserGroup);
		
		IDMApplicationModel application = new IDMApplicationModel();
		application.setCode((String) map.get(ApplicationConstants.APP_CODE)); 
		bulkPaymentModel.setApplication(application);
		
		List<PendingUploadDetailModel> pumDetails = pum.getDetails();
		if (ValueUtils.hasValue(pumDetails)) {
			List<BulkPaymentDetailModel> details = new ArrayList<>(pum.getDetails().size());
			int idx = 0;
			for(PendingUploadDetailModel pendingUploadModel : pumDetails) {
				BulkPaymentDetailModel bulkPaymentDetailModel = new BulkPaymentDetailModel();
				bulkPaymentDetailModel.setBenAccountNo(pendingUploadModel.getBenAccountNo());
				bulkPaymentDetailModel.setBenAccountName(pendingUploadModel.getBenAccountName());
				bulkPaymentDetailModel.setBenAddress1(pendingUploadModel.getBenAddress1());
				bulkPaymentDetailModel.setBenAddress2(pendingUploadModel.getBenAddress2());
				bulkPaymentDetailModel.setBenAddress3(pendingUploadModel.getBenAddress3());
				
				bulkPaymentDetailModel.setTrxAmount(pendingUploadModel.getTrxAmount());
				bulkPaymentDetailModel.setTrxCurrencyCode(pendingUploadModel.getTrxCurrencyCode());
				
				bulkPaymentDetailModel.setRemark1(pendingUploadModel.getRemark1());
				bulkPaymentDetailModel.setRemark2(pendingUploadModel.getRemark2());
				bulkPaymentDetailModel.setRemark3(pendingUploadModel.getRemark3());

				bulkPaymentDetailModel.setBenRefNo(pendingUploadModel.getBenRefNo());
				bulkPaymentDetailModel.setSenderRefNo(pendingUploadModel.getSenderRefNo());
				
				bulkPaymentDetailModel.setBenNotificationFlag(pendingUploadModel.getBenNotificationFlag());
				bulkPaymentDetailModel.setBenNotificationValue(pendingUploadModel.getBenNotificationValue());
				
				bulkPaymentDetailModel.setFinalizeFlag(pendingUploadModel.getFinalizeFlag());
				
				//domestic
				if (pendingUploadModel.getUploadType().equals("DOM") || pendingUploadModel.getUploadType().equals("INT")) {
					bulkPaymentDetailModel.setLldIsBenResidence(pendingUploadModel.getLldIsBenResidence());
					if(ApplicationConstants.NO.equals(bulkPaymentDetailModel.getLldIsBenResidence())){
						CountryModel benResidentCountry = new CountryModel();
						benResidentCountry.setCode(pendingUploadModel.getLldBenResidenceCountryCode());
						bulkPaymentDetailModel.setLldBenResidenceCountry(benResidentCountry);
					}
					
					bulkPaymentDetailModel.setLldIsBenCitizen(pendingUploadModel.getLldIsBenCitizen());
					if(ApplicationConstants.NO.equals(bulkPaymentDetailModel.getLldIsBenCitizen())){
						CountryModel benCitizenCountry = new CountryModel();
						benCitizenCountry.setCode(pendingUploadModel.getLldBenCitizenCountryCode());
						bulkPaymentDetailModel.setLldBenCitizenCountry(benCitizenCountry);
					}
					
					BeneficiaryTypeModel benType = new BeneficiaryTypeModel();
					benType.setCode(pendingUploadModel.getBeneTypeDomestic());
					bulkPaymentDetailModel.setBenType(benType);
					
					DomesticBankModel domBank = new DomesticBankModel();
					domBank.setCode(pendingUploadModel.getBenBankCode());
					bulkPaymentDetailModel.setBenDomesticBankCode(domBank);
				}
				
				//international
				if (pendingUploadModel.getUploadType().equals("INT")) {
					bulkPaymentDetailModel.setLldIsBenAffiliated(pendingUploadModel.getLldIsBenAffiliated());
					bulkPaymentDetailModel.setLldIsBenIdentical(pendingUploadModel.getLldIsBenIdentical());
					
					InternationalBankModel intBank = new InternationalBankModel();
					intBank.setCode(pendingUploadModel.getBenBankCode());
					bulkPaymentDetailModel.setBenInternationalBankCode(intBank);
					
					CountryModel bankCountryCode = new CountryModel();
					bankCountryCode.setCode(pendingUploadModel.getBenBankCountryCode());
					bulkPaymentDetailModel.setBenInternationalCountry(bankCountryCode);
					
				}
				
				bulkPaymentDetailModel.setIsError(ApplicationConstants.NO);
				bulkPaymentDetailModel.setIsHostProcessed(ApplicationConstants.NO);
				
				bulkPaymentDetailModel.setCreatedBy(vo.getCreatedBy());
				bulkPaymentDetailModel.setCreatedDate(DateUtils.getCurrentTimestamp());
				
				bulkPaymentDetailModel.setBulkPayment(bulkPaymentModel);
				
				bulkPaymentDetailModel.setIdx(idx++);				
				
				details.add(bulkPaymentDetailModel);
			}
			
			bulkPaymentModel.setBulkPaymentDetail(details);			
		}
		
		bulkPaymentRepo.persist(bulkPaymentModel);
	}
	
	private void setInstructionDate(BulkPaymentModel bulkPaymentModel, Timestamp instructionDate) throws Exception {
		
		if (bulkPaymentModel.getInstructionMode().equals(ApplicationConstants.SI_IMMEDIATE)) {
			Calendar calInstDate = DateUtils.getEarliestDate(transactionValidationService.getInstructionDateForRelease(bulkPaymentModel.getInstructionMode(), instructionDate));
			bulkPaymentModel.setInstructionDate(new Timestamp(calInstDate.getTimeInMillis()));
		} else if (bulkPaymentModel.getInstructionMode().equals(ApplicationConstants.SI_FUTURE_DATE)) {
			bulkPaymentModel.setInstructionDate(instructionDate);
		}
	}
	
	private void setCharge(BulkPaymentModel bulkPaymentModel, List<Map<String, Object>> chargeList){
		for(int i=1; i <= chargeList.size(); i++){
			Map<String, Object> chargeMap = chargeList.get(i - 1);
			String chargeId = (String) chargeMap.get("id");
			BigDecimal value = new BigDecimal(chargeMap.get("value").toString());
			String currencyCode = (String) chargeMap.get("currencyCode");
			
			if(i == 1){
				bulkPaymentModel.setChargeType1(chargeId);
				bulkPaymentModel.setChargeTypeAmount1(value);
				bulkPaymentModel.setChargeTypeCurrency1(currencyCode);
			} else if(i == 2){
				bulkPaymentModel.setChargeType2(chargeId);
				bulkPaymentModel.setChargeTypeAmount2(value);
				bulkPaymentModel.setChargeTypeCurrency2(currencyCode);
			} else if(i == 3){
				bulkPaymentModel.setChargeType3(chargeId);
				bulkPaymentModel.setChargeTypeAmount3(value);
				bulkPaymentModel.setChargeTypeCurrency3(currencyCode);
			} else if(i == 4){
				bulkPaymentModel.setChargeType4(chargeId);
				bulkPaymentModel.setChargeTypeAmount4(value);
				bulkPaymentModel.setChargeTypeCurrency4(currencyCode);
			} else if(i == 5){
				bulkPaymentModel.setChargeType5(chargeId);
				bulkPaymentModel.setChargeTypeAmount5(value);
				bulkPaymentModel.setChargeTypeCurrency5(currencyCode);
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
		vo.setService("BulkPaymentSC");
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
				
				saveBulkPayment(pendingUploadModel, vo, map);
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
			
			List<BulkPaymentModel> bulkPaymentList = bulkPaymentRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_IMMEDIATE, 
					new Timestamp(cal.getTimeInMillis()), new Timestamp(cal.getTimeInMillis()));
			
			executeAllBatch(bulkPaymentList);
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
				
				List<BulkPaymentModel> bulkPaymentList = bulkPaymentRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_FUTURE_DATE, new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(bulkPaymentList);
				
			}
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void executeAllBatch(List<BulkPaymentModel> list) throws Exception {
		List<SpringBeanInvokerData<String>> jobs = new ArrayList<>();
		for(BulkPaymentModel model : list) {
			jobs.add(new SpringBeanInvokerData<>(model.getId(), "BulkPaymentService", "executeBatchTransaction", new Object[] {model}, jobTimeout));
		}

		SpringBeanInvokerHelper.invokeAndForget(invoker, jobs, invokerTimeout, concurrencies);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void executeBatchTransaction(BulkPaymentModel bulkPaymentModel) {
		if(logger.isDebugEnabled())
			logger.debug("Process bulkPayment : " + bulkPaymentModel.getId() + " - " + bulkPaymentModel.getReferenceNo());
		
		boolean success = false;
		String errorCode = null;
		Timestamp now = DateUtils.getCurrentTimestamp();
		try {
			//avoid lazy when get details
			bulkPaymentModel =  bulkPaymentRepo.findOne(bulkPaymentModel.getId());
			
			bulkPaymentModel.setIsProcessed(ApplicationConstants.YES);
			bulkPaymentModel.setUpdatedBy(ApplicationConstants.CREATED_BY_SYSTEM);
			bulkPaymentModel.setUpdatedDate(DateUtils.getCurrentTimestamp());
			
			doTransfer(bulkPaymentModel, ApplicationConstants.APP_GPCASHIB);
			success = true;
		} catch (BusinessException e) {
			errorCode = e.getErrorCode();
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			errorCode = "GPT-GENERALERROR";
			logger.error(e.getMessage(), e);
		} finally {
			if (success) {
				bulkPaymentModel.setIsError(ApplicationConstants.NO);
				bulkPaymentModel.setErrorCode(null);				
				for(BulkPaymentDetailModel dtl : bulkPaymentModel.getBulkPaymentDetail()) {
					dtl.setIsError(ApplicationConstants.NO);
					dtl.setErrorCode(null);				
				}
				
				pendingTaskRepo.updatePendingTask(bulkPaymentModel.getId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE);				
				trxStatusService.addTransactionStatus(bulkPaymentModel.getId(), now, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE, null, false, null);
			} else {
				bulkPaymentModel.setIsError(ApplicationConstants.YES);
				bulkPaymentModel.setErrorCode(errorCode);				
				for(BulkPaymentDetailModel dtl : bulkPaymentModel.getBulkPaymentDetail()) {
					dtl.setIsError(ApplicationConstants.YES);
					dtl.setErrorCode(errorCode);				
				}
				
				pendingTaskRepo.updatePendingTask(bulkPaymentModel.getId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);				
				trxStatusService.addTransactionStatus(bulkPaymentModel.getId(), now, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, bulkPaymentModel.getId(), true, errorCode);
			}
			
			bulkPaymentRepo.save(bulkPaymentModel);
		}
	}
	
	private void doTransfer(BulkPaymentModel bulkPaymentModel, String appCode) throws Exception {
		boolean isRollback = false;
		boolean limitUpdated = false;

		try {
			//update transaction limit
			transactionValidationService.updateTransactionLimit(bulkPaymentModel.getCorporate().getId(), 
					bulkPaymentModel.getService().getCode(), 
					bulkPaymentModel.getSourceAccount().getCurrency().getCode(), 
					bulkPaymentModel.getTrxCurrencyCode(), 
					bulkPaymentModel.getCorporateUserGroup().getId(), 
					bulkPaymentModel.getTotalDebitedEquivalentAmount(),
					bulkPaymentModel.getApplication().getCode());
			limitUpdated = true;

			Map<String, Object> inputs = prepareInputsForEAI(bulkPaymentModel, appCode);
			
			if (bulkPaymentModel.getIsSummaryOpt().equals(ApplicationConstants.YES)) {
//				if (bulkPaymentModel.getService().getCode().equals(PendingUploadConstants.SRVC_GPT_MFT_BULK_IH)) {
				
					String escrowAccount = maintenanceRepo.isSysParamValid(SysParamConstants.BULK_ESCROW_ACCOUNT).getValue();
					inputs.put("benAccountNo", escrowAccount);
					inputs.put("benAccountCurrencyCode", bulkPaymentModel.getTrxCurrencyCode());
					inputs.put("totalDebitAmount", bulkPaymentModel.getTotalDebitedEquivalentAmount());
					inputs.put("totalChargeEquivalent", bulkPaymentModel.getTotalChargeEquivalentAmount());
					eaiAdapter.invokeService(EAIConstants.INHOUSE_TRANSFER, inputs);
//				}
			}
			
			eaiAdapter.invokeService(EAIConstants.BULK_TRANSFER, inputs);
			
			//for POC only, create response file
			eaiAdapter.invokeService(EAIConstants.BULK_POC_RESPONSE, inputs);
			
			bulkPaymentModel.setStatus("GPT-0100185"); // IN-PROGRESS
			bulkPaymentModel.setIsError(ApplicationConstants.NO);
			
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
				bulkPaymentModel.setStatus("GPT-0100129");
				bulkPaymentModel.setIsError(ApplicationConstants.YES);
				bulkPaymentModel.setErrorCode(errorMappingModel == null ? errorMsg : errorMappingModel.getCode());
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
			globalTransactionService.save(bulkPaymentModel.getCorporate().getId(), BulkPaymentSC.menuCode, 
					bulkPaymentModel.getService().getCode(), bulkPaymentModel.getReferenceNo(), 
					bulkPaymentModel.getId(), bulkPaymentModel.getTrxCurrencyCode(), bulkPaymentModel.getTotalChargeEquivalentAmount(), 
					bulkPaymentModel.getTotalAmount(), 
					bulkPaymentModel.getTotalDebitedEquivalentAmount(), bulkPaymentModel.getIsError());
			//----------------------------------
			
			if(isRollback) {
				if(limitUpdated) { // reverse usage
					try {
						transactionValidationService.reverseUpdateTransactionLimit(bulkPaymentModel.getCorporate().getId(), 
								bulkPaymentModel.getService().getCode(), 
								bulkPaymentModel.getSourceAccount().getCurrency().getCode(), 
								bulkPaymentModel.getTrxCurrencyCode(), 
								bulkPaymentModel.getCorporateUserGroup().getId(), 
								bulkPaymentModel.getTotalDebitedEquivalentAmount(),
								bulkPaymentModel.getApplication().getCode());
					} catch(Exception e) {
						logger.error("Failed to reverse the usage "+e.getMessage(),e);
					}
				}
			}
		}
	}
	
	private Map<String, Object> prepareInputsForEAI(BulkPaymentModel model, String appCode) throws Exception {
		Map<String, Object> inputs = new HashMap<>();
		
		inputs.put("channel", appCode);
		inputs.put("channelRefNo", model.getId());
		
		AccountModel sourceAccountModel = model.getSourceAccount();
		
		inputs.put("debitAccountNo", sourceAccountModel.getAccountNo());
		inputs.put("debitAccountName", sourceAccountModel.getAccountName());
		inputs.put("debitAccountType", sourceAccountModel.getAccountType().getCode());
		inputs.put("debitAccountCurrencyCode", sourceAccountModel.getCurrency().getCode());
		
		inputs.put("isSummary", model.getIsSummaryOpt());
		
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
		inputs.put("servicingBranch", model.getCorporate().getBranch().getCode());
		inputs.put("corpId", model.getCorporate().getId());
		inputs.put("userId", model.getCreatedBy());
		
		return inputs;
		
	}
	
	private void notifyTrxUsersHistory(BulkPaymentModel model, String appCode) {
		try {
			List<String> emails = pendingTaskService.getCorporateUserEmailWithNotifyTrx(model.getId());
			if (emails.size() > 0) {
				IDMMenuModel menu = idmRepo.isIDMMenuValid(BulkPaymentSC.menuCode);
				
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
				inputs.put("subject", menu.getName().concat(" Notification"));
				
				eaiAdapter.invokeService(EAIConstants.USER_BULK_TRANSFER_NOTIFICATION, inputs);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void executeBulkPaymentResponseScheduler(String parameter) throws ApplicationException, BusinessException {
		try {
			String filename = "";
			
			
				String[] requestDateArr = parameter.split("\\=");
				if(ValueUtils.hasValue(requestDateArr[0])) {
						if(ValueUtils.hasValue(requestDateArr[1])){
							if("filename".equals(requestDateArr[0])) {
								filename = requestDateArr[1];
							}
						}					
				}
				
			
			
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("timeout", timeout);
			inputs.put("batchSizeThreshold", batchSizeThreshold);
			inputs.put("filename", filename);
			
			if(logger.isDebugEnabled()) {
				logger.debug("executeBulkPaymentResponseScheduler inputs : " + inputs);
			}
			
			eaiAdapter.invokeService(EAIConstants.BULK_RESPONSE, inputs);
			
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
	public void transactionNotification(String bulkPaymentId) throws Exception {
		BulkPaymentModel bulkPaymentModel = bulkPaymentRepo.findByIdAndIsHostProcessed(bulkPaymentId, ApplicationConstants.YES);
		if (bulkPaymentModel!=null) {
			if ("GPT-0100130".equals(bulkPaymentModel.getStatus())) {
				pendingTaskRepo.updatePendingTask(bulkPaymentModel.getId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS);
				trxStatusService.addTransactionStatus(bulkPaymentModel.getId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, bulkPaymentModel.getId(), false, null);
			} else {
				String errorCode = bulkPaymentModel.getErrorCode();
				if ("GPT-0100190".equals(bulkPaymentModel.getStatus())) {
					pendingTaskRepo.updatePendingTask(bulkPaymentModel.getId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_PARTIAL_SUCCESS);
					trxStatusService.addTransactionStatus(bulkPaymentModel.getId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_PARTIAL_SUCCESS, bulkPaymentModel.getId(), true, errorCode);			
				} else {
					pendingTaskRepo.updatePendingTask(bulkPaymentModel.getId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
					trxStatusService.addTransactionStatus(bulkPaymentModel.getId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, bulkPaymentModel.getId(), true, errorCode);			
				}
			}
			
			// send email to transaction user history
			notifyTrxUsersHistory(bulkPaymentModel, ApplicationConstants.APP_GPCASHIB);
			
			// send email to beneficiary
			AccountModel sourceAccountModel = bulkPaymentModel.getSourceAccount();			
			Map<String, Object> sourceInfo = new HashMap<>();			
			sourceInfo.put("debitAccountNo", sourceAccountModel.getAccountNo());
			sourceInfo.put("debitAccountName", sourceAccountModel.getAccountName());
			
			Map<String, Object> inputs = prepareInputsForEAI(bulkPaymentModel, ApplicationConstants.APP_GPCASHIB);
			
			if (bulkPaymentModel.getIsSummaryOpt().equals(ApplicationConstants.YES)) {
				String escrowAccount = maintenanceRepo.isSysParamValid(SysParamConstants.BULK_ESCROW_ACCOUNT).getValue();
				inputs.put("debitAccountNo", escrowAccount);
			}else {
				//debitcharge
				if(bulkPaymentModel.getTotalChargeEquivalentAmount()!=null) {
					inputs.put("remark1", "Fee");
					String glAccount = maintenanceRepo.isSysParamValid(SysParamConstants.GL_ACCOUNT).getValue();
					inputs.put("benAccountNo", glAccount);
					inputs.put("benAccountName", glAccount);
					inputs.put("trxAmount", bulkPaymentModel.getTotalChargeEquivalentAmount());
					inputs.put("totalDebitAmount", bulkPaymentModel.getTotalChargeEquivalentAmount());
					inputs.put("totalChargeEquivalent", BigDecimal.ZERO);
					
					inputs.put("benAccountCurrencyCode", bulkPaymentModel.getTrxCurrencyCode());
					inputs.put("channelRefNo", bulkPaymentModel.getId().concat(glAccount));
					
					eaiAdapter.invokeService(EAIConstants.INHOUSE_TRANSFER, inputs); //for debit credit  FEE (only for POC Resona)
				}
			}
			
			//debit credit detail
			for (BulkPaymentDetailModel dtl : bulkPaymentModel.getBulkPaymentDetail()) {
				
				inputs.put("remark1", dtl.getRemark1());
				inputs.put("remark2", dtl.getRemark2());
				inputs.put("remark3", dtl.getRemark3());
				inputs.put("benAccountName", dtl.getBenAccountName());
				
				inputs.put("trxAmount", dtl.getTrxAmount());
				inputs.put("totalDebitAmount", dtl.getTrxAmount());
				inputs.put("totalChargeEquivalent", BigDecimal.ZERO);
				
				inputs.put("benAccountNo", dtl.getBenAccountNo());
				inputs.put("benAccountCurrencyCode", dtl.getTrxCurrencyCode());
				inputs.put("channelRefNo", dtl.getId());
				eaiAdapter.invokeService(EAIConstants.INHOUSE_TRANSFER, inputs); //for debit credit (only for POC Resona)
				
				/*if (ApplicationConstants.YES.equals(dtl.getIsHostProcessed()) && 
					ApplicationConstants.YES.equals(dtl.getBenNotificationFlag()) &&
					ApplicationConstants.NO.equals(dtl.getIsError())) {
					
					notifyBeneficiary(dtl, sourceInfo);					
				}*/				
			}
		}
	}
	
	public void notifyBeneficiary(BulkPaymentDetailModel model, Map<String, Object> sourceInfo) {
		try {
			Map<String, Object> inputs = new HashMap<>();
			inputs.putAll(sourceInfo);
			inputs.put("benAccountNo", model.getBenAccountNo());
			inputs.put("benAccountName", model.getBenAccountName());
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
			boolean isUserGrantView = false;
			
			String isViewFromBank = ValueUtils.getValue((String) map.get(ApplicationConstants.VIEW_FROM_BANK), 
					ApplicationConstants.NO);
			
			if(ApplicationConstants.NO.equals(isViewFromBank)) {
				String userCode = (String)map.get(ApplicationConstants.LOGIN_USERCODE);
				CorporateUserModel corpUserModel = corporateUtilsRepo.isCorporateUserValid(userCode);
				isUserGrantView = corpUserModel.getIsGrantViewDetail().equals(ApplicationConstants.YES);
			} else {
				//bank can see amount
				isUserGrantView = true;
			}
			
			String executedId = (String) map.get("executedId");
			TransactionStatusModel trxStatus = trxStatusRepo.findOne(executedId);			
			
			BulkPaymentModel bulkPaymentModel = bulkPaymentRepo.findOne(trxStatus.getEaiRefNo());
			if (bulkPaymentModel==null)
				throw new BusinessException("GPT-0100001");
			
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("channel", ApplicationConstants.APP_GPCASHIB);
			inputs.put("channelRefNo", bulkPaymentModel.getId());
			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.RETRIEVE_TRANSACTION, inputs);
			
			Date executionDate= (Date)outputs.get("executionDate");

			Page<BulkPaymentDetailModel> result = ((BulkPaymentRepository) bulkPaymentRepo).searchBulkPaymentDetailById(bulkPaymentModel.getId(), 
					PagingUtils.createPageRequest(map));

			List<Map<String, Object>> details = setBulkPaymentDetailModelToMap(bulkPaymentModel, result.getContent(), executionDate, isUserGrantView);
			
			PagingUtils.setPagingInfo(resultMap, result);
			
			resultMap.put("executedResult", details);
		
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
		
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> setBulkPaymentDetailModelToMap(BulkPaymentModel bulkPaymentModel, List<BulkPaymentDetailModel> list, 
			Date executionDate, boolean isUserGrantView) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (BulkPaymentDetailModel dtlModel : list) {
			Map<String, Object> map = new HashMap<>();
			map.put("executedDate", executionDate);
			map.put("systemReferenceNo", bulkPaymentModel.getId());
			map.put("debitAccount", bulkPaymentModel.getSourceAccount().getAccountNo());
			map.put("debitAccountName", bulkPaymentModel.getSourceAccount().getAccountName());
			map.put("creditAccount", dtlModel.getBenAccountNo());
			map.put("creditAccountName", dtlModel.getBenAccountName());
			
			//TODO diganti jika telah implement rate
			if (isUserGrantView) {
				map.put("transactionCurrency", dtlModel.getTrxCurrencyCode());
				map.put("transactionAmount", dtlModel.getTrxAmount());
				
				map.put("debitAccountCurrency", bulkPaymentModel.getSourceAccount().getCurrency().getName());
				map.put("debitTransactionCurrency", dtlModel.getTrxCurrencyCode());
				map.put("debitExchangeRate", ApplicationConstants.ZERO);
				map.put("debitEquivalentAmount", dtlModel.getTrxAmount());
				
				map.put("creditTransactionCurrency", dtlModel.getTrxCurrencyCode());
				map.put("creditEquivalentAmount", dtlModel.getTrxAmount());
				map.put("creditExchangeRate", ApplicationConstants.ZERO);
			} else {
				map.put("transactionCurrency", ApplicationConstants.EMPTY_STRING);
				map.put("transactionAmount", ApplicationConstants.NOT_AVAILABLE);
				
				map.put("debitAccountCurrency", ApplicationConstants.EMPTY_STRING);
				map.put("debitTransactionCurrency", ApplicationConstants.EMPTY_STRING);
				map.put("debitExchangeRate", ApplicationConstants.NOT_AVAILABLE);
				map.put("debitEquivalentAmount", ApplicationConstants.NOT_AVAILABLE);
				
				map.put("creditTransactionCurrency", ApplicationConstants.EMPTY_STRING);
				map.put("creditEquivalentAmount", ApplicationConstants.NOT_AVAILABLE);
				map.put("creditExchangeRate", ApplicationConstants.NOT_AVAILABLE);
			}
			
			
			//--------------------------------------------
			
			//TODO diganti jika telah implement periodical charges
			map.put("chargeAccount", ApplicationConstants.EMPTY_STRING);
			
			LinkedList<Map<String, Object>> chargeList = new LinkedList<>();
			Map<String, Object> chargeMap = null;
			CorporateUserPendingTaskModel pt = pendingTaskRepo.findByReferenceNo(bulkPaymentModel.getReferenceNo());
			
			String strValues = pt.getValuesStr();
			if (strValues != null) {
				try {
					Map<String, Object> valueMap = (Map<String, Object>) objectMapper.readValue(strValues, Class.forName(pt.getModel()));
					List<Map<String,Object>> listCharge = (List<Map<String,Object>>)valueMap.get("chargeList");
					
					for (int i = 0; i < listCharge.size(); i++) {
						Map<String, Object> mapCharge = listCharge.get(i);
						if (bulkPaymentModel.getChargeType1() != null && bulkPaymentModel.getChargeType1().equals(mapCharge.get("id"))) {
							chargeMap = new HashMap<>();
							chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
							chargeMap.put("chargeCurrency", bulkPaymentModel.getChargeTypeCurrency1());
							chargeMap.put("chargeEquivalentAmount", bulkPaymentModel.getChargeTypeAmount1());
							chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
							chargeList.add(chargeMap);
							continue;
						}
						if (bulkPaymentModel.getChargeType2() != null && bulkPaymentModel.getChargeType2().equals(mapCharge.get("id"))) {
							chargeMap = new HashMap<>();
							chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
							chargeMap.put("chargeCurrency", bulkPaymentModel.getChargeTypeCurrency2());
							chargeMap.put("chargeEquivalentAmount", bulkPaymentModel.getChargeTypeAmount2());
							chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
							chargeList.add(chargeMap);
							continue;
						}
						if (bulkPaymentModel.getChargeType3() != null && bulkPaymentModel.getChargeType3().equals(mapCharge.get("id"))) {
							chargeMap = new HashMap<>();
							chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
							chargeMap.put("chargeCurrency", bulkPaymentModel.getChargeTypeCurrency3());
							chargeMap.put("chargeEquivalentAmount", bulkPaymentModel.getChargeTypeAmount3());
							chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
							chargeList.add(chargeMap);
							continue;
						}
						if (bulkPaymentModel.getChargeType4() != null && bulkPaymentModel.getChargeType4().equals(mapCharge.get("id"))) {
							chargeMap = new HashMap<>();
							chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
							chargeMap.put("chargeCurrency", bulkPaymentModel.getChargeTypeCurrency4());
							chargeMap.put("chargeEquivalentAmount", bulkPaymentModel.getChargeTypeAmount4());
							chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
							chargeList.add(chargeMap);
							continue;
						}
						if (bulkPaymentModel.getChargeType5() != null && bulkPaymentModel.getChargeType5().equals(mapCharge.get("id"))) {
							chargeMap = new HashMap<>();
							chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
							chargeMap.put("chargeCurrency", bulkPaymentModel.getChargeTypeCurrency5());
							chargeMap.put("chargeEquivalentAmount", bulkPaymentModel.getChargeTypeAmount5());
							chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
							chargeList.add(chargeMap);
							continue;
						}
						
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			
			/*if(bulkPaymentModel.getChargeType1() != null) {
				chargeMap = new HashMap<>();
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(bulkPaymentModel.getChargeType1());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				chargeMap.put("chargeType", serviceCharge.getName());
				chargeMap.put("chargeCurrency", bulkPaymentModel.getChargeTypeCurrency1());
				chargeMap.put("chargeEquivalentAmount", bulkPaymentModel.getChargeTypeAmount1());
				chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
				chargeList.add(chargeMap);
			}
			
			if(bulkPaymentModel.getChargeType2() != null) {
				chargeMap = new HashMap<>();
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(bulkPaymentModel.getChargeType2());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				chargeMap.put("chargeType", serviceCharge.getName());
				chargeMap.put("chargeCurrency", bulkPaymentModel.getChargeTypeCurrency2());
				chargeMap.put("chargeEquivalentAmount", bulkPaymentModel.getChargeTypeAmount2());
				chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
				chargeList.add(chargeMap);
			}
			
			if(bulkPaymentModel.getChargeType3() != null) {
				chargeMap = new HashMap<>();
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(bulkPaymentModel.getChargeType3());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				chargeMap.put("chargeType", serviceCharge.getName());
				chargeMap.put("chargeCurrency", bulkPaymentModel.getChargeTypeCurrency3());
				chargeMap.put("chargeEquivalentAmount", bulkPaymentModel.getChargeTypeAmount3());
				chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
				chargeList.add(chargeMap);
			}
			
			if(bulkPaymentModel.getChargeType4() != null) {
				chargeMap = new HashMap<>();
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(bulkPaymentModel.getChargeType4());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				chargeMap.put("chargeType", serviceCharge.getName());
				chargeMap.put("chargeCurrency", bulkPaymentModel.getChargeTypeCurrency4());
				chargeMap.put("chargeEquivalentAmount", bulkPaymentModel.getChargeTypeAmount4());
				chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
				chargeList.add(chargeMap);
			}
			
			if(bulkPaymentModel.getChargeType5() != null) {
				chargeMap = new HashMap<>();
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(bulkPaymentModel.getChargeType5());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				chargeMap.put("chargeType", serviceCharge.getName());
				chargeMap.put("chargeCurrency", bulkPaymentModel.getChargeTypeCurrency5());
				chargeMap.put("chargeEquivalentAmount", bulkPaymentModel.getChargeTypeAmount5());
				chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
				chargeList.add(chargeMap);
			}*/
			
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
			
			BulkPaymentModel model = bulkPaymentRepo.findOne(trxStatus.getEaiRefNo());
			setParamForDownloadTrxStatus(reportParams, model);
			
			String masterReportFile = reportFolder + File.separator + "TransactionStatus" + 
					File.separator + "BulkPayment" + File.separator + "download-transaction-bulkpayment" + "-" + locale.getLanguage() + ".jasper";
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
	private void setParamForDownloadTrxStatus(Map<String, Object> reportParams, BulkPaymentModel model) throws JsonParseException, JsonMappingException, ClassNotFoundException, IOException {
		DecimalFormat df = new DecimalFormat(moneyFormat);
		if(model != null) {
			reportParams.put("service", ValueUtils.getValue(model.getService().getDscp()));
			
			AccountModel sourceAccount = model.getSourceAccount();
			reportParams.put("sourceAccount", sourceAccount.getAccountNo());
			reportParams.put("sourceAccountName", sourceAccount.getAccountName());
			
			reportParams.put(ApplicationConstants.FILENAME, model.getFileName());
			reportParams.put("fileDscp", ValueUtils.getValue(model.getFileDescription()));
			reportParams.put("totalRecord", String.valueOf(model.getTotalRecord()));
			reportParams.put("totalAmount", df.format(model.getTotalAmount()));			
			reportParams.put("transactionCurrency", model.getTrxCurrencyCode());
			
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
				
				BulkPaymentModel bulkPayment = bulkPaymentRepo.findByIdAndIsProcessed(pendingTaskId, ApplicationConstants.NO);
				
				bulkPayment.setIsProcessed(ApplicationConstants.YES);
				bulkPayment.setUpdatedDate(DateUtils.getCurrentTimestamp());
				bulkPayment.setUpdatedBy(loginUserCode);
				
				bulkPaymentRepo.save(bulkPayment);
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
	public void executeBulkPaymentVAResponseScheduler(String parameter) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("timeout", timeout);
			inputs.put("batchSizeThreshold", batchSizeThreshold);
			
			if(logger.isDebugEnabled()) {
				logger.debug("executeBulkPaymentResponseVAScheduler inputs : " + inputs);
			}
			
			eaiAdapter.invokeService(EAIConstants.BULK_RESPONSE, inputs);
			
		} catch (BusinessException e) {
			throw e;			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void executeBulkPaymentUpdateHeaderScheduler(String parameter) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("timeout", timeout);
			inputs.put("batchSizeThreshold", batchSizeThreshold);
			
			if(logger.isDebugEnabled()) {
				logger.debug("executeBulkPaymentUpdateHeaderScheduler inputs : " + inputs);
			}
			
			eaiAdapter.invokeService(EAIConstants.PAYROLL_UPDATE_HEADER_STATUS, inputs);
			
		} catch (BusinessException e) {
			throw e;			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
}