package com.gpt.product.gpcash.corporate.transaction.va.accountlistupload.services;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.constants.PendingUploadConstants;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadDetailModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.repository.PendingUploadRepository;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.services.PendingUploadService;
import com.gpt.product.gpcash.corporate.transaction.va.accountlist.VADetailStatus;
import com.gpt.product.gpcash.corporate.transaction.va.registration.constants.VARegistrationConstants;
import com.gpt.product.gpcash.corporate.transaction.va.registration.model.VARegistrationDetailModel;
import com.gpt.product.gpcash.corporate.transaction.va.registration.model.VARegistrationModel;
import com.gpt.product.gpcash.corporate.transaction.va.registration.repository.VARegistrationRepository;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class VAAccountListUploadServiceImpl implements VAAccountListUploadService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.vaaccountlistupload.upload.path}")
	private String pathUpload;
	
	@Value("${gpcash.vaaccountlistupload.download.sample.path}")
	private String pathDownloadSampleFile;
	
	@Value("${gpcash.vaaccountlistupload.download.sample.filename}")
	private String downloadSampleFileName;	
		
	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;	
	
	@Autowired
	private PendingUploadService pendingUploadService;	
	
	@Autowired
	private PendingUploadRepository pendingUploadRepo;	
	
	@Autowired
	private VARegistrationRepository vaRegistrationRepo;	
	
	@Value("${gpcash.batch.sot.timeout}")
	private int timeout;

	@Value("${gpcash.batch.sot.batch-size}")
	private int batchSizeThreshold;		
	
	@Value("${gpcash.report.folder}")
	private String reportFolder;
	
	@Value("${gpcash.report.bank.logo}")
	private String bankLogo;
	
	@Value("${gpcash.report.money.format:#,###.00}")
	private String moneyFormat;
	
	@Value("${gpcash.trxstatus.download.path}")
	private String pathDownload;
	
	@Autowired
	private EAIEngine eaiAdapter;
	
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
		resultMap.put(ApplicationConstants.ACCOUNT_GRP_DTL_ID, map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID));
		resultMap.put("totalError", map.get("totalError"));
		resultMap.put("details", map.get("details"));
		resultMap.put("totalRecord", map.get("totalRecord"));
		resultMap.put("totalPage", map.get("totalPage"));
				
		return resultMap;
	}	
	
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			String instructionMode = (String)map.get(ApplicationConstants.INSTRUCTION_MODE);
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);

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
				
				List<Map<String, Object>> vaDetailList = (ArrayList<Map<String,Object>>)map.get("details");
				for(Map<String, Object> vaDetailMap : vaDetailList) {
					String vaNo = (String) vaDetailMap.get("vaNo");
					
					checkUniqueRecord(vaNo, corporateId);
				}
				
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
	
	private void checkUniqueRecord(String vaNo, String corporateId) throws Exception {
		VARegistrationDetailModel model = vaRegistrationRepo.findVADetailListByVANo(vaNo);

		if (model != null) {
			throw new BusinessException("GPT-0100004");
		}
	}
	
	private void checkCustomValidation(Map<String, Object> map) throws Exception {

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

	private void saveVAAccountListUpload(PendingUploadModel pum, CorporateUserPendingTaskVO vo, Map<String, Object> map) throws Exception {		
		CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(vo.getCreatedBy());
		
		CorporateModel corporate = corporateUser.getCorporate();
		String corporateId = corporate.getId();
		
		VARegistrationModel vaRegistration = vaRegistrationRepo.
				findByCorporateIdAndAccountNo(corporateId, pum.getSourceAccount().getAccountNo(),
				pum.getFileDescription());
				
		List<PendingUploadDetailModel> pumDetails = pum.getDetails();
		if (ValueUtils.hasValue(pumDetails)) {
			int idx = 0;
			
			List<String> listBill = new ArrayList<>();
			String vaType = "";
			
			for(PendingUploadDetailModel pendingUploadModel : pumDetails) {
					
				checkUniqueRecord(pendingUploadModel.getVaNo(), corporateId);
				
//				listBill.add(pendingUploadModel.getVaNo().substring(5).concat(";").concat(pendingUploadModel.getVaName()).concat(";").concat(pendingUploadModel.getVaAmount().toString()));
				String bill = pendingUploadModel.getVaNo().substring(5).concat(";").concat(pendingUploadModel.getVaName());
				if (pendingUploadModel.getVaAmount().compareTo(BigDecimal.ZERO) > 0) {
					bill = bill.concat(";").concat(pendingUploadModel.getVaAmount().toString());
				}
				
				
				listBill.add(bill);
				
				vaType = pendingUploadModel.getVaType();
					
					VARegistrationDetailModel vaRegistrationDetail = new VARegistrationDetailModel();
				vaRegistrationDetail.setVaNo(pendingUploadModel.getVaNo());
				vaRegistrationDetail.setVaName(pendingUploadModel.getVaName());
				vaRegistrationDetail.setVaType(pendingUploadModel.getVaType());
				vaRegistrationDetail.setVaAmount(pendingUploadModel.getVaAmount());
					vaRegistrationDetail.setVaStatus(VADetailStatus.ACTIVE.name());
					vaRegistrationDetail.setCreatedBy(vo.getCreatedBy());
					vaRegistrationDetail.setCreatedDate(DateUtils.getCurrentTimestamp());
					vaRegistrationDetail.setVaRegistration(vaRegistration);
					vaRegistrationDetail.setIdx(idx++);	
					
					vaRegistration.getVaRegistrationDetail().add(vaRegistrationDetail);
				
			}
			
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("cif", corporate.getHostCifId());
			inputs.put("corporateId", corporateId);
			inputs.put("productCode", vaRegistration.getProductCode());
			inputs.put("listBill", listBill);
			inputs.put("vaType", vaType);
			inputs.put("fileName", pum.getFileName());
			inputs.put("userId", corporateUser.getUserId());
			eaiAdapter.invokeService(VARegistrationConstants.EAI_VA_ACCOUNT_REGISTRATION_BULK, inputs);
			
			vaRegistrationRepo.save(vaRegistration);
		}
	}
	
	private CorporateUserPendingTaskVO setCorporateUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		CorporateUserModel maker = corporateUtilsRepo.isCorporateUserValid(createdBy);
		String userGroupId = maker.getCorporateUserGroup().getId();

		String accountGroupDetailId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
		String uniqueKey = createdBy.concat(ApplicationConstants.DELIMITER_PIPE).concat(accountGroupDetailId)
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(String.valueOf((DateUtils.getCurrentTimestamp().getTime())));
		
		String sessionTime = (String) map.get("sessionTime");

		String instructionMode = (String) map.get(ApplicationConstants.INSTRUCTION_MODE);
		Timestamp instructionDate = (Timestamp) map.get(ApplicationConstants.INSTRUCTION_DATE);
		
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		vo.setUniqueKey(uniqueKey);
		vo.setCreatedBy(createdBy);
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("VAAccountListUploadSC");
		vo.setCorporateId(corporateId);
		vo.setTransactionAmount(new BigDecimal(ApplicationConstants.APPROVAL_MATRIX_NON_TRANSACTION));
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
						
		vo.setInstructionMode(instructionMode);
		vo.setInstructionDate(DateUtils.getInstructionDateBySessionTime(sessionTime, instructionDate));
		
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
				
				saveVAAccountListUpload(pendingUploadModel, vo, map);
			}
		} catch (BusinessException e) {
			
			if (e.getErrorCode().equals("GPT-0100004")) {
				vo.setErrorCode(e.getErrorCode());
				vo.setIsError(ApplicationConstants.YES);
			} else {
			throw e;
			}
			
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
	public Map<String, Object> submitBucket(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String loginCorporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			CorporateModel corporate = corporateUtilsRepo.isCorporateValid(loginCorporateId);
			
			Map<String, Object> additionalInput = new HashMap<>();
			additionalInput.put("productCode", map.get("product_code"));
			additionalInput.put("corpCorporateCode", corporate.getVaProductCode());
			additionalInput.put("vaType", map.get("vaType"));
			map.put(PendingUploadConstants.ADDITIONAL_INPUT, additionalInput);
			return pendingUploadService.submitToBucket(map);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> searchPendingUploadById(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			resultMap = pendingUploadService.searchPendingUploadById(map);
			String loginCorporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			CorporateModel corporate = corporateUtilsRepo.isCorporateValid(loginCorporateId);
			resultMap.put("corpCorporateCode", corporate.getVaProductCode());
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;
	}
}
