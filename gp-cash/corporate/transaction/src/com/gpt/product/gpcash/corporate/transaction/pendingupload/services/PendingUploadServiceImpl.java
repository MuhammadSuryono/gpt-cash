package com.gpt.product.gpcash.corporate.transaction.pendingupload.services;

import java.io.File;
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
import java.util.concurrent.Callable;

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
import com.gpt.component.common.invoker.spi.ICallableInvoker;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.constants.PendingUploadConstants;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadBeneficiaryDetailModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadBeneficiaryErrorModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadBeneficiaryModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadDetailModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadErrorModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.repository.PendingUploadBeneficiaryRepository;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.repository.PendingUploadRepository;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;
import com.gpt.product.gpcash.service.model.ServiceModel;

@Service
@Transactional(rollbackFor = Exception.class)
public class PendingUploadServiceImpl implements PendingUploadService {
	private final Logger logger = LoggerFactory.getLogger(getClass());	
	
	@Value("${gpcash.currency.local}")
	private String localCurrency;
	
	@Autowired
	private PendingUploadService self;
	
	@Autowired
	private PendingUploadRepository pendingUploadRepo;
	
	@Autowired
	private PendingUploadBeneficiaryRepository beneficiaryRepo;
	
	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;
	
	@Autowired
	private ICallableInvoker invoker;
		
	@Autowired
	private EAIEngine eaiAdapter;
	
	@Autowired
	private MessageSource message;	
	
	@Override
	public Map<String, Object> searchPendingUpload(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			Page<PendingUploadModel> result = pendingUploadRepo.searchPendingUpload(map, PagingUtils.createPageRequest(map));

			resultMap.put("result", setPendingUploadModelToMap(result.getContent()));
			
			PagingUtils.setPagingInfo(resultMap, result);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;		
	}
	
	private List<Map<String, Object>> setPendingUploadModelToMap(List<PendingUploadModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (PendingUploadModel model : list) {
			String statusDescription = "";
			if (ApplicationConstants.PENDING_UPLOAD_STATUS_COMPLETED.equals(model.getStatus()))
				statusDescription = "Completed";
			else if (ApplicationConstants.PENDING_UPLOAD_STATUS_IN_PROGRESS.equals(model.getStatus()))
				statusDescription = "In Progress";
			else if (ApplicationConstants.PENDING_UPLOAD_STATUS_FAILED.equals(model.getStatus()))
				statusDescription = "Failed";
			
			Map<String, Object> map = new HashMap<>();
			map.put("id", model.getId());
			map.put("status", model.getStatus());
			map.put("statusDescription", statusDescription);			
			map.put(ApplicationConstants.FILENAME, model.getFileName());
			map.put("fileDescription", ValueUtils.getValue(model.getFileDescription()));
			map.put("uploadDateTime", model.getUploadDate());
			map.put("totalRecord", model.getTotalRecord());
			map.put("totalError", model.getTotalError());
			
			AccountModel account = model.getSourceAccount();
			map.put("sourceAccountNo", account.getAccountNo());
			map.put("sourceAccountName", account.getAccountName());
			map.put("sourceAccountCurrencyCode", account.getCurrency().getCode());
			
			map.put(ApplicationConstants.TRANS_SERVICE_CODE, model.getService().getCode());
			map.put("serviceName", model.getService().getName());
			
			resultList.add(map);
		}

		return resultList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> submitToBucket(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {			
			String instructionMode = (String) map.get("instructionMode");
			//override instructionDate if Immediate to get server timestamp
			if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
				map.put(ApplicationConstants.INSTRUCTION_DATE, DateUtils.getCurrentTimestamp());
			}			
			String corpId = (String)map.get(ApplicationConstants.LOGIN_CORP_ID);
			String userCode = (String)map.get(ApplicationConstants.LOGIN_USERCODE);
			
			String accountGroupDetailId = (String)map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
			CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
			CorporateUserGroupModel corporateUserGroup = corporateUser.getCorporateUserGroup();

			CorporateAccountGroupDetailModel gdm = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(corpId, accountGroupDetailId);
			
			String accountNo = gdm.getCorporateAccount().getAccount().getAccountNo();
			
			map.put("sourceAccountNo", accountNo);
			map.put(ApplicationConstants.ACCOUNT_GRP_DTL_ID, accountGroupDetailId);
			map.put("corporateUserGroupId", corporateUserGroup.getId());
			
			//add additionalInput if exist
			if(ValueUtils.hasValue(map.get(PendingUploadConstants.ADDITIONAL_INPUT))) {
				map.putAll((Map<String, Object>) map.get(PendingUploadConstants.ADDITIONAL_INPUT));
			}
			//----------------------------
			
			String filePath = (String) map.get(ApplicationConstants.PATH_UPLOAD);
			String fileId = (String)map.get("fileId");
			String filename = (String)map.get(ApplicationConstants.FILENAME);
			
            Path path = Paths.get(filePath + File.separator + filename + "_" + fileId);
            byte[] rawdata = Files.readAllBytes(path);
             
            // we cannot call save method internal directly coz we need to create new transaction
            self.save(map);
            validateFile(map, rawdata);
			
			Map<String, Object> result = new HashMap<>();			
			result.put(ApplicationConstants.WF_FIELD_MESSAGE, "{GPT-0100114|"+ filename +"}");
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> searchPendingUploadById(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String pendingUploadId = (String)map.get("pendingUploadId");			
			PendingUploadModel pm = detailPendingUploadById(pendingUploadId);
			
			//add validation to make sure data upload is valid for the login user id
			//pentest 4 June 2022 BJB
			if(!pm.getCreatedBy().equals(map.get(ApplicationConstants.LOGIN_USERID).toString())){
				throw new BusinessException("GPT-0100001");
			}
			//
			
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("pendingUploadId", pm.getId());
			resultMap.put("fileFormat", pm.getFileFormat());
			resultMap.put(ApplicationConstants.FILENAME, pm.getFileName());
			resultMap.put("fileDescription", ValueUtils.getValue(pm.getFileDescription()));
			resultMap.put("status", pm.getStatus());
			resultMap.put("uploadDateTime", pm.getUploadDate());
			resultMap.put(ApplicationConstants.INSTRUCTION_MODE, pm.getInstructionMode());
			resultMap.put(ApplicationConstants.INSTRUCTION_DATE, pm.getInstructionDate());
			resultMap.put(ApplicationConstants.SESSION_TIME, DateUtils.getSessionTimeByInstructionDate(pm.getInstructionDate()));
			
			AccountModel account = pm.getSourceAccount();
			resultMap.put("sourceAccountNo", account.getAccountNo());
			resultMap.put("sourceAccountName", account.getAccountName());
			resultMap.put("sourceAccountCurrencyCode", account.getCurrency().getCode());
			
			resultMap.put(ApplicationConstants.TRANS_CURRENCY, pm.getTrxCurrencyCode());
			resultMap.put(ApplicationConstants.TRANS_AMOUNT, pm.getTotalAmount());
			resultMap.put("totalRecord", pm.getTotalRecord());
			resultMap.put("totalError", pm.getTotalError());
			
			resultMap.put(ApplicationConstants.TRANS_SERVICE_CODE, pm.getService().getCode());
			resultMap.put("serviceName", pm.getService().getName());
			
			if (pm.getTotalError()==0) {
				Page<PendingUploadDetailModel> result = pendingUploadRepo.searchPendingUploadDetail(pendingUploadId, 
						PagingUtils.createPageRequest(map));

				resultMap.put("details", setPendingUploadDetailModelToMap(result.getContent(), true));
				
				PagingUtils.setPagingInfo(resultMap, result);
			} else {
				Page<PendingUploadErrorModel> result = pendingUploadRepo.searchPendingUploadError(pendingUploadId, 
						PagingUtils.createPageRequest(map));

				resultMap.put("details", setPendingUploadErrorModelToMap(result.getContent()));
				
				PagingUtils.setPagingInfo(resultMap, result);
			}
						
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}
	
	private PendingUploadModel detailPendingUploadById(String id) throws Exception {
		PendingUploadModel pm = pendingUploadRepo.detailPendingUploadById(id);
		
		if (pm == null)
			throw new BusinessException("GPT-0100001");

		return pm;
	}
	
	private List<Map<String, Object>> setPendingUploadDetailModelToMap(List<PendingUploadDetailModel> list, boolean isUserGrantView) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (PendingUploadDetailModel model : list) {
			Map<String, Object> map = new HashMap<>();
			map.put("benAccountNo", model.getBenAccountNo());
			map.put("benAccountName", model.getBenAccountName());
			
			if (isUserGrantView) {
				map.put("trxCurrencyCode", model.getTrxCurrencyCode());
				map.put("trxAmount", model.getTrxAmount());
			} else {
				map.put("trxCurrencyCode", ApplicationConstants.EMPTY_STRING);
				map.put("trxAmount", ApplicationConstants.NOT_AVAILABLE);
			}
			
			StringBuilder description = new StringBuilder();
			if (model.getRemark1()!=null && model.getRemark1().trim().length()>0)
				description.append(model.getRemark1());

			if (model.getRemark2()!=null && model.getRemark2().trim().length()>0) {
			    if (description.toString().trim().length()>0)
			    	description.append(" ");
			    description.append(model.getRemark2());
			}

			if (model.getRemark3()!=null && model.getRemark3().trim().length()>0) {
			    if (description.toString().trim().length()>0)
			    	description.append(" ");
			    description.append(model.getRemark3());
			}
			
			map.put("description", ValueUtils.getValue(description));
			map.put("senderRefNo", ValueUtils.getValue(model.getSenderRefNo()));
			map.put("finalizeFlag", ValueUtils.getValue(model.getFinalizeFlag()));
			map.put("benRefNo", ValueUtils.getValue(model.getBenRefNo()));
			map.put("benBankCode", ValueUtils.getValue(model.getBenBankCode()));
			
			map.put("isBenResident", ValueUtils.getValue(model.getLldIsBenResidence()));
			map.put("benResidentCountryCode", ValueUtils.getValue(model.getLldBenResidenceCountryCode()));
			map.put("isBenCitizen", ValueUtils.getValue(model.getLldIsBenCitizen()));
			map.put("benCitizenCountryCode", ValueUtils.getValue(model.getLldBenCitizenCountryCode()));
			map.put("beneTypeDomestic", ValueUtils.getValue(model.getBeneTypeDomestic()));
			map.put("benBankCountryCode", ValueUtils.getValue(model.getBenBankCountryCode()));
			map.put("isBenIdentical", ValueUtils.getValue(model.getLldIsBenIdentical()));
			map.put("isBenAffiliated", ValueUtils.getValue(model.getLldIsBenAffiliated()));

			map.put("vaNo", model.getVaNo());
			map.put("vaName", model.getVaName());
			map.put("vaType", model.getVaType());
			map.put("vaAmount", model.getVaAmount());

			resultList.add(map);
		}

		return resultList;
	}	
	
	private List<Map<String, Object>> setPendingUploadErrorModelToMap(List<PendingUploadErrorModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (PendingUploadErrorModel model : list) {
			Map<String, Object> map = new HashMap<>();
			map.put("line", model.getLine());
			map.put("lineType", model.getLineType());
			map.put("accountNo", ValueUtils.getValue(model.getAccountNo()));
			map.put("trxCurrencyCode", ValueUtils.getValue(model.getTrxCurrencyCode()));
			map.put("trxAmount", ValueUtils.getValue(model.getTrxAmount()));			

			String errorCode = model.getErrorCode();			
			map.put("errorReason", message.getMessage(errorCode, null, errorCode, LocaleContextHolder.getLocale()));

			resultList.add(map);
		}

		return resultList;
	}		

	public Map<String, Object> searchPendingUploadByIdValidOnly(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String pendingUploadId = (String)map.get("pendingUploadId");
			PendingUploadModel pm = detailPendingUploadByIdValidOnly(pendingUploadId);
			
			//add validation to make sure data upload is valid for the login user id
			//pentest 4 June 2022 BJB
			if(!pm.getCreatedBy().equals(map.get(ApplicationConstants.LOGIN_USERID).toString())){
				throw new BusinessException("GPT-0100001");
			}
			//
			
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("pendingUploadId", pm.getId());
			resultMap.put("fileFormat", pm.getFileFormat());
			resultMap.put(ApplicationConstants.FILENAME, pm.getFileName());
			resultMap.put("fileDescription", ValueUtils.getValue(pm.getFileDescription()));
			resultMap.put("status", pm.getStatus());
			resultMap.put("uploadDateTime", pm.getUploadDate());
			resultMap.put(ApplicationConstants.INSTRUCTION_MODE, pm.getInstructionMode());
			resultMap.put(ApplicationConstants.INSTRUCTION_DATE, pm.getInstructionDate());
			resultMap.put(ApplicationConstants.SESSION_TIME, DateUtils.getSessionTimeByInstructionDate(pm.getInstructionDate()));			
			resultMap.put(ApplicationConstants.ACCOUNT_GRP_DTL_ID, pm.getCorporateAccountGroupDetail());
			
			AccountModel account = pm.getSourceAccount();
			resultMap.put("sourceAccountNo", account.getAccountNo());
			resultMap.put("sourceAccountName", account.getAccountName());
			resultMap.put("sourceAccountCurrencyCode", account.getCurrency().getCode());
			resultMap.put(ApplicationConstants.TRANS_CURRENCY, pm.getTrxCurrencyCode());
			resultMap.put(ApplicationConstants.TRANS_AMOUNT, pm.getTotalAmount());
//			resultMap.put("totalRecord", pm.getTotalRecord()); // will be set in PagingUtils.setPagingInfo below
			resultMap.put("totalError", pm.getTotalError());
			
			Page<PendingUploadDetailModel> result = pendingUploadRepo.searchPendingUploadDetail(pendingUploadId, 
					PagingUtils.createPageRequest(map));

			resultMap.put("details", setPendingUploadDetailModelToMap(result.getContent(), true));
			
			PagingUtils.setPagingInfo(resultMap, result);
			
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}
	
	private PendingUploadModel detailPendingUploadByIdValidOnly(String id) throws Exception {
		PendingUploadModel model = pendingUploadRepo.detailPendingUploadByIdValidOnly(id);
		if (model == null)
			throw new BusinessException("GPT-0100001");

		return model;
	}	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public PendingUploadModel save(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			PendingUploadModel pm = pendingUploadRepo.findOne((String)map.get("fileId"));
			if (pm != null)
				throw new BusinessException("GPT-file has been uploaded"); // TODO: FILE HAS BEEN UPLOADED
			
			pm = new PendingUploadModel();
			pm.setId((String)map.get("fileId"));
			pm.setFileName((String)map.get(ApplicationConstants.FILENAME));		
			pm.setFileFormat((String)map.get("fileFormat"));
			pm.setFileDescription((String)map.get("fileDescription"));
			
			AccountModel sourceAccountModel = new AccountModel();
			sourceAccountModel.setAccountNo((String)map.get("sourceAccountNo"));
			pm.setSourceAccount(sourceAccountModel);
					
			pm.setUploadDate(new Timestamp(((Date)map.get("uploadDateTime")).getTime()));
			pm.setStatus(ApplicationConstants.PENDING_UPLOAD_STATUS_IN_PROGRESS);
			pm.setInstructionMode((String)map.get("instructionMode"));
			pm.setInstructionDate(DateUtils.getInstructionDateBySessionTime((String)map.get("sessionTime"), 
					(Timestamp)map.get("instructionDate")));
			
			CorporateModel corpModel = new CorporateModel();
			corpModel.setId((String)map.get(ApplicationConstants.LOGIN_CORP_ID));
			pm.setCorporate(corpModel);
			
			CorporateUserGroupModel corporateUserGroupModel = new CorporateUserGroupModel();
			corporateUserGroupModel.setId((String)map.get("corporateUserGroupId"));
			pm.setCorporateUserGroup(corporateUserGroupModel);
			
			CorporateAccountGroupDetailModel corporateAccountGroupDetailModel = new CorporateAccountGroupDetailModel();
			corporateAccountGroupDetailModel.setId((String)map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID));
			pm.setCorporateAccountGroupDetail(corporateAccountGroupDetailModel.getId());
			
			IDMMenuModel menu = new IDMMenuModel();
			menu.setCode((String)map.get(ApplicationConstants.STR_MENUCODE));		
			pm.setMenu(menu);

			// set transaction service code
			String serviceCode = (String)map.get(ApplicationConstants.TRANS_SERVICE_CODE);
			if(ValueUtils.hasValue(serviceCode)) {
				ServiceModel service = new ServiceModel();
				service.setCode(serviceCode);		
				pm.setService(service);
			}
			
			pm.setCreatedBy((String)map.get(ApplicationConstants.LOGIN_USERCODE));
			pm.setCreatedDate(new Timestamp(System.currentTimeMillis()));
			
			pm.setDeleteFlag(ApplicationConstants.NO);
			pm.setIsProcessed(ApplicationConstants.NO);
			pm.setIsError(ApplicationConstants.NO);
			
			pendingUploadRepo.persist(pm);
			
			return pm;			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public PendingUploadBeneficiaryModel saveBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			PendingUploadBeneficiaryModel pm = beneficiaryRepo.findOne((String)map.get("fileId"));
			if (pm != null)
				throw new BusinessException("GPT-file has been uploaded"); // TODO: FILE HAS BEEN UPLOADED
			
			pm = new PendingUploadBeneficiaryModel();
			pm.setId((String)map.get("fileId"));
			pm.setFileName((String)map.get(ApplicationConstants.FILENAME));		
			pm.setFileFormat((String)map.get("fileFormat"));
			pm.setFileDescription((String)map.get("fileDescription"));
			pm.setUploadDate(new Timestamp(((Date)map.get("uploadDateTime")).getTime()));
			pm.setBeneficiaryUploadType((String) map.get("beneficiary"));
			pm.setStatus(ApplicationConstants.PENDING_UPLOAD_STATUS_IN_PROGRESS);
			
			CorporateModel corpModel = new CorporateModel();
			corpModel.setId((String)map.get(ApplicationConstants.LOGIN_CORP_ID));
			pm.setCorporate(corpModel);
			
			CorporateUserGroupModel corporateUserGroupModel = new CorporateUserGroupModel();
			corporateUserGroupModel.setId((String)map.get("corporateUserGroupId"));
			pm.setCorporateUserGroup(corporateUserGroupModel);
			
			IDMMenuModel menu = new IDMMenuModel();
			menu.setCode((String)map.get(ApplicationConstants.STR_MENUCODE));		
			pm.setMenu(menu);
			
			pm.setCreatedBy((String)map.get(ApplicationConstants.LOGIN_USERCODE));
			pm.setCreatedDate(new Timestamp(System.currentTimeMillis()));
			
			pm.setDeleteFlag(ApplicationConstants.NO);
			pm.setIsProcessed(ApplicationConstants.NO);
			pm.setIsError(ApplicationConstants.NO);
			
			beneficiaryRepo.persist(pm);
			
			return pm;			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public PendingUploadModel update(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			PendingUploadModel pm = pendingUploadRepo.findOne((String)map.get("fileId"));
			if (pm == null)
				throw new BusinessException("GPT-0100001");
			
			String status = (String)map.get("status");
			if (ApplicationConstants.PENDING_UPLOAD_STATUS_COMPLETED.equals(status)) {
				Map<String, Object> header = (Map<String, Object>)map.get("header");
				if (header!=null) {
					pm.setTrxCurrencyCode(localCurrency);
					pm.setTotalAmount((BigDecimal)header.get("totalAmount"));
					pm.setTotalRecord((Integer)header.get("totalRecord"));
				}
				
				List<Map<String, Object>> details = (List<Map<String, Object>>)map.get("details");				
				if (details!=null && details.size()>0) {
					List<PendingUploadDetailModel> dtls = new ArrayList<>();
					
					for(Map<String, Object> dtl : details) {
						PendingUploadDetailModel detailModel = new PendingUploadDetailModel();
						detailModel.setBenAccountNo((String)dtl.get("benAccountNo"));
						detailModel.setBenAccountName((String)dtl.get("benAccountName"));
						detailModel.setTrxCurrencyCode(localCurrency);
						detailModel.setTrxAmount((BigDecimal)dtl.get("trxAmount"));
						detailModel.setRemark1((String)dtl.get("remark1"));
						detailModel.setRemark2((String)dtl.get("remark2"));
						detailModel.setRemark3((String)dtl.get("remark3"));
						detailModel.setBenAddress1((String)dtl.get("benAddress1"));
						detailModel.setBenAddress2((String)dtl.get("benAddress2"));
						detailModel.setBenAddress3((String)dtl.get("benAddress3"));
						detailModel.setBenNotificationFlag((String)dtl.get("benNotificationFlag"));
						detailModel.setBenNotificationValue((String)dtl.get("benNotificationValue"));
						detailModel.setSenderRefNo((String)dtl.get("senderRefNo"));
						detailModel.setFinalizeFlag((String)dtl.get("finalizeFlag"));
						detailModel.setBenRefNo((String)dtl.get("benRefNo"));
						detailModel.setParent(pm);
						
						detailModel.setVaNo((String)dtl.get("vaNo"));
						detailModel.setVaName((String)dtl.get("vaName"));
						detailModel.setVaType((String)dtl.get("vaType"));
						detailModel.setVaAmount((BigDecimal)dtl.get("vaAmount"));
//						
						detailModel.setBenBankCode((String) dtl.get("benBankCode"));
						detailModel.setLldIsBenResidence((String) dtl.get("isBenResident"));
						detailModel.setLldIsBenCitizen((String) dtl.get("isBenCitizen"));
						detailModel.setLldBenResidenceCountryCode((String) dtl.get("benResidentCountryCode"));
						detailModel.setLldBenCitizenCountryCode((String) dtl.get("benCitizenCountryCode"));
						detailModel.setBeneTypeDomestic((String) dtl.get("beneTypeDomestic"));
						detailModel.setBenBankCountryCode((String) dtl.get("benBankCountryCode"));
						detailModel.setLldIsBenIdentical((String) dtl.get("isBenIdentical"));
						detailModel.setLldIsBenAffiliated((String) dtl.get("isBenAffiliated"));
						detailModel.setUploadType((String)dtl.get("uploadType"));
						
						dtls.add(detailModel);					
					}						
					pm.setDetails(dtls);
				}
				
				List<Map<String, Object>> errors = (List<Map<String, Object>>)map.get("errors");
				if (errors!=null && errors.size()>0) {
					List<PendingUploadErrorModel> errs = new ArrayList<>();
					for (Map<String, Object> error : errors) {
						PendingUploadErrorModel errorModel = new PendingUploadErrorModel();
						errorModel.setLine((Integer)error.get("line"));
						errorModel.setLineType((String)error.get("lineType"));
						errorModel.setAccountNo((String)error.get("accountNo"));
						errorModel.setTrxCurrencyCode(localCurrency);
						errorModel.setTrxAmount((BigDecimal)error.get("trxAmount"));
						errorModel.setErrorCode((String)error.get("errorCode"));
						errorModel.setParent(pm);
						
						errs.add(errorModel);
					}
					pm.setTotalError(errors.size());
					pm.setErrors(errs);
				}
				
				pm.setStatus(ApplicationConstants.PENDING_UPLOAD_STATUS_COMPLETED);
				pm.setUpdatedBy(pm.getCreatedBy());
				pm.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
			} else {
				pm.setStatus(ApplicationConstants.PENDING_UPLOAD_STATUS_FAILED);
			}
			
			pendingUploadRepo.save(pm);
			
			return pm;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}	
	
	private void validateFile(Map<String, Object> map, byte[] rawdata) {
		invoker.invoke(map, new FileValidator(map, rawdata), 0, this::onSuccess, this::onFailed);		
	}
	
	private void onSuccess(Map<String, Object> map, Map<String, Object> output) {
		try {
			output.put("status", ApplicationConstants.PENDING_UPLOAD_STATUS_COMPLETED);			
			
			// we cannot call save method internal directly coz we need to create new transaction
			PendingUploadModel model = self.update(output);

			logger.debug("File "+ model.getFileFormat() + " has been validated successfully");				
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}			
	}
	
	private void onFailed(Map<String, Object> map, Exception ex) {
		logger.error(ex.getMessage(), ex);
		
		try {
			map.put("status", ApplicationConstants.PENDING_UPLOAD_STATUS_FAILED);
			
			// we cannot call save method internal directly coz we need to create new transaction
			self.update(map);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}			
	}
	
	class FileValidator implements Callable<Map<String, Object>> {
		private Map<String, Object> map;
		private byte[] rawdata;
		
		public FileValidator(Map<String, Object> map, byte[] rawdata) {
			this.map = map;
			this.rawdata = rawdata;
		}

		@Override
		public Map<String, Object> call() throws Exception {
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("rawdata", rawdata);
			inputs.put("fileFormat", map.get("fileFormat"));
			inputs.put("sourceAccountNo", map.get("sourceAccountNo"));
			inputs.put("instructionDate", map.get("instructionDate"));		
			inputs.put("serviceCode", map.get(ApplicationConstants.TRANS_SERVICE_CODE));
			inputs.put("corpCorporateCode", map.get("corpCorporateCode"));
			inputs.put("productCode", map.get("product_code"));
			inputs.put("vaType", map.get("vaType"));
			inputs.put("corporateId", map.get("loginCorporateId"));
			inputs.put("trxType", map.get("trxType"));
			
			//====== untuk kebutuhan validate file BulkPayment =======
			if (String.valueOf(map.get(ApplicationConstants.EAI_SERVICE_NAME)).equals(EAIConstants.PARSE_CUSTOMER_BULK)) {
				inputs.put("sknThreshold", map.get("sknThreshold"));
				inputs.put("rtgsThreshold", map.get("rtgsThreshold"));
			}
			//====== untuk kebutuhan validate file BulkPayment =======
			
			Map<String, Object> outputs = eaiAdapter.invokeService((String) map.get(ApplicationConstants.EAI_SERVICE_NAME), inputs);
			
			map.put("header", outputs.get("header"));
			map.put("details", outputs.get("details"));
			map.put("errors", outputs.get("errors"));
			
			return map;
		}		
	}	
	
	@Override
	public Map<String, Object> detailCreatedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String userCode = (String)map.get(ApplicationConstants.LOGIN_USERCODE);
			CorporateUserModel corpUserModel = corporateUtilsRepo.isCorporateUserValid(userCode);
			boolean isUserGrantView = corpUserModel.getIsGrantViewDetail().equals(ApplicationConstants.YES);
			
			String pendingTaskId = (String)map.get("executedId");			
			PendingUploadModel pm = pendingUploadRepo.findByPendingTaskId(pendingTaskId);
			if (pm == null)
				throw new BusinessException("GPT-0100001");
			
			Page<PendingUploadDetailModel> result = pendingUploadRepo.searchPendingUploadDetail(pm.getId(), 
					PagingUtils.createPageRequest(map));

			resultMap.put("details", setPendingUploadDetailModelToMap(result.getContent(), isUserGrantView));
			
			PagingUtils.setPagingInfo(resultMap, result);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}			
		
		return resultMap;
	}

	@Override
	public Map<String, Object> searchPendingUploadDetail(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String userCode = (String)map.get(ApplicationConstants.LOGIN_USERCODE);
			CorporateUserModel corpUserModel = corporateUtilsRepo.isCorporateUserValid(userCode);
			boolean isUserGrantView = true;
			
			String pendingUploadId = (String)map.get("pendingUploadId");			
			PendingUploadModel pm = pendingUploadRepo.findByIdAndIsProcessed(pendingUploadId, ApplicationConstants.YES);
			if (pm == null)
				throw new BusinessException("GPT-0100001");
			
			Page<PendingUploadDetailModel> result = pendingUploadRepo.searchPendingUploadDetail(pm.getId(), 
					PagingUtils.createPageRequest(map));
			
			//isUserGrantView only for payroll, else true
			if(pm.getMenu().getCode().equals(PendingUploadConstants.MNU_GPCASH_F_MASS_FUND_PAYROLL)) {
				isUserGrantView = corpUserModel.getIsGrantViewDetail().equals(ApplicationConstants.YES);
			}

			resultMap.put("details", setPendingUploadDetailModelToMap(result.getContent(), isUserGrantView));
			
			PagingUtils.setPagingInfo(resultMap, result);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}			
		
		return resultMap;
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> deletePendingUpload(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			List<String> pendingUploadIds = (List<String>)map.get("pendingUploadId");
			
			for(String pendingUploadId : pendingUploadIds) {
				PendingUploadModel model = findByIdAndIsProcessedAndDeleteFlag(pendingUploadId, ApplicationConstants.NO, ApplicationConstants.NO);			
				model.setDeleteFlag(ApplicationConstants.YES);
				model.setUpdatedDate(DateUtils.getCurrentTimestamp());
				model.setUpdatedBy((String)map.get(ApplicationConstants.LOGIN_USERCODE));

				pendingUploadRepo.save(model);
			}
			
			Map<String, Object> result = new HashMap<>();
			result.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100202");
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}	
	
	private PendingUploadModel findByIdAndIsProcessedAndDeleteFlag(String id, String isProcessed, String deleteFlag) throws Exception {
		PendingUploadModel model = pendingUploadRepo.findByIdAndIsProcessedAndDeleteFlag(id, isProcessed, deleteFlag);
		
		if (model==null)
			throw new BusinessException("GPT-0100001");
		
		return model;		
	}

	@Override
	public Map<String, Object> submitBeneficiaryToBucket(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {			
			
			String corpId = (String)map.get(ApplicationConstants.LOGIN_CORP_ID);
			String userCode = (String)map.get(ApplicationConstants.LOGIN_USERCODE);
			
			CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
			CorporateUserGroupModel corporateUserGroup = corporateUtilsRepo.isCorporateUserGroupValid(corpId, corporateUser.getCorporateUserGroup().getCode());
			
			map.put("corporateUserGroupId", corporateUserGroup.getId());
			
			//add additionalInput if exist
			if(ValueUtils.hasValue(map.get(PendingUploadConstants.ADDITIONAL_INPUT))) {
				map.putAll((Map<String, Object>) map.get(PendingUploadConstants.ADDITIONAL_INPUT));
			}
			//----------------------------
			
			String filePath = (String) map.get(ApplicationConstants.PATH_UPLOAD);
			String fileId = (String)map.get("fileId");
			String filename = (String)map.get(ApplicationConstants.FILENAME);
			
            Path path = Paths.get(filePath + File.separator + filename + "_" + fileId);
            byte[] rawdata = Files.readAllBytes(path);
             
            // we cannot call save method internal directly coz we need to create new transaction
            self.saveBeneficiary(map);
            validateBeneficairyFile(map, rawdata);
			
			Map<String, Object> result = new HashMap<>();			
			result.put(ApplicationConstants.WF_FIELD_MESSAGE, "{GPT-0100114|"+ filename +"}");
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> searchPendingUploadBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			Page<PendingUploadBeneficiaryModel> result = beneficiaryRepo.searchPendingUpload(map, PagingUtils.createPageRequest(map));

			resultMap.put("result", setModelToMap(result.getContent()));
			
			PagingUtils.setPagingInfo(resultMap, result);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<PendingUploadBeneficiaryModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (PendingUploadBeneficiaryModel model : list) {
			String statusDescription = "";
			if (ApplicationConstants.PENDING_UPLOAD_STATUS_COMPLETED.equals(model.getStatus()))
				statusDescription = "Completed";
			else if (ApplicationConstants.PENDING_UPLOAD_STATUS_IN_PROGRESS.equals(model.getStatus()))
				statusDescription = "In Progress";
			else if (ApplicationConstants.PENDING_UPLOAD_STATUS_FAILED.equals(model.getStatus()))
				statusDescription = "Failed";
			
			Map<String, Object> map = new HashMap<>();
			map.put("id", model.getId());
			map.put("status", model.getStatus());
			map.put("statusDescription", statusDescription);			
			map.put(ApplicationConstants.FILENAME, model.getFileName());
			map.put("fileDescription", ValueUtils.getValue(model.getFileDescription()));
			map.put("uploadDateTime", model.getUploadDate());
			map.put("totalRecord", model.getTotalRecord());
			map.put("totalError", model.getTotalError());
			map.put("beneficiary", model.getBeneficiaryUploadType());

			resultList.add(map);
		}

		return resultList;
	}
	
	private void validateBeneficairyFile(Map<String, Object> map, byte[] rawdata) {
		invoker.invoke(map, new FileValidatorBen(map, rawdata), 0, this::onSuccessBen, this::onFailedBen);		
	}
	
	private void onSuccessBen(Map<String, Object> map, Map<String, Object> output) {
		try {
			output.put("status", ApplicationConstants.PENDING_UPLOAD_STATUS_COMPLETED);			
			
			// we cannot call save method internal directly coz we need to create new transaction
			PendingUploadBeneficiaryModel model = self.updateBeneficiary(output);

			logger.debug("File "+ model.getFileFormat() + " has been validated successfully");				
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}			
	}
	
	private void onFailedBen(Map<String, Object> map, Exception ex) {
		logger.error(ex.getMessage(), ex);
		
		try {
			map.put("status", ApplicationConstants.PENDING_UPLOAD_STATUS_FAILED);
			
			// we cannot call save method internal directly coz we need to create new transaction
			self.updateBeneficiary(map);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}			
	}
	
	class FileValidatorBen implements Callable<Map<String, Object>> {
		private Map<String, Object> map;
		private byte[] rawdata;
		
		public FileValidatorBen(Map<String, Object> map, byte[] rawdata) {
			this.map = map;
			this.rawdata = rawdata;
		}

		@Override
		public Map<String, Object> call() throws Exception {
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("rawdata", rawdata);
			inputs.put("fileFormat", map.get("fileFormat"));
			inputs.put("beneficiary", map.get("beneficiary"));
			inputs.put("corporateId", map.get("loginCorporateId"));
			Map<String, Object> outputs = eaiAdapter.invokeService((String) map.get(ApplicationConstants.EAI_SERVICE_NAME), inputs);
			
			map.put("header", outputs.get("header"));
			map.put("details", outputs.get("details"));
			map.put("errors", outputs.get("errors"));
			
			return map;
		}		
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public PendingUploadBeneficiaryModel updateBeneficiary(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			PendingUploadBeneficiaryModel pm = beneficiaryRepo.findOne((String)map.get("fileId"));
			if (pm == null)
				throw new BusinessException("GPT-0100001");
			
			String status = (String)map.get("status");
			if (ApplicationConstants.PENDING_UPLOAD_STATUS_COMPLETED.equals(status)) {
				Map<String, Object> header = (Map<String, Object>)map.get("header");
				if (header!=null) {
					pm.setTotalRecord((Integer)header.get("totalRecord"));
				}
				
				List<Map<String, Object>> details = (List<Map<String, Object>>)map.get("details");				
				if (details!=null && details.size()>0) {
					List<PendingUploadBeneficiaryDetailModel> dtls = new ArrayList<>();
					
					for(Map<String, Object> dtl : details) {
						PendingUploadBeneficiaryDetailModel detailModel = new PendingUploadBeneficiaryDetailModel();
						detailModel.setBenAccountNo((String)dtl.get("benAccountNo"));
						detailModel.setBenAccountName((String)dtl.get("benAccountName"));
//						detailModel.setTrxCurrencyCode(localCurrency);
//						detailModel.setTrxAmount((BigDecimal)dtl.get("trxAmount"));
						detailModel.setRemark1((String)dtl.get("remark1"));
						detailModel.setRemark2((String)dtl.get("remark2"));
						detailModel.setRemark3((String)dtl.get("remark3"));
						detailModel.setBenAddress1((String)dtl.get("benAddress1"));
						detailModel.setBenAddress2((String)dtl.get("benAddress2"));
						detailModel.setBenAddress3((String)dtl.get("benAddress3"));
						detailModel.setBenNotificationFlag((String)dtl.get("benNotificationFlag"));
						detailModel.setBenNotificationValue((String)dtl.get("benNotificationValue"));
//						detailModel.setSenderRefNo((String)dtl.get("senderRefNo"));
//						detailModel.setFinalizeFlag((String)dtl.get("finalizeFlag"));
//						detailModel.setBenRefNo((String)dtl.get("benRefNo"));
						detailModel.setParent(pm);
						
						detailModel.setVaNo((String)dtl.get("vaNo"));
						detailModel.setVaName((String)dtl.get("vaName"));
						detailModel.setVaType((String)dtl.get("vaType"));
						detailModel.setVaAmount((BigDecimal)dtl.get("vaAmount"));
						
						detailModel.setBenAccountCurrencyCode((String) dtl.get("benAccountCurrency"));
						detailModel.setBenAliasName((String) dtl.get("benAliasName"));
						detailModel.setBenBankCode((String) dtl.get("benBankCode"));
						detailModel.setLldIsBenResidence((String) dtl.get("isBenResident"));
						detailModel.setLldIsBenCitizen((String) dtl.get("isBenCitizen"));
						detailModel.setLldBenResidenceCountryCode((String) dtl.get("benResidentCountryCode"));
						detailModel.setLldBenCitizenCountryCode((String) dtl.get("benCitizenCountryCode"));
						detailModel.setBeneTypeDomestic((String) dtl.get("beneTypeDomestic"));
						detailModel.setBenBankCountryCode((String) dtl.get("benBankCountryCode"));
						detailModel.setLldIsBenIdentical((String) dtl.get("isBenIdentical"));
						detailModel.setLldIsBenAffiliated((String) dtl.get("isBenAffiliated"));
						
						dtls.add(detailModel);					
					}						
					pm.setDetails(dtls);
				}
				
				List<Map<String, Object>> errors = (List<Map<String, Object>>)map.get("errors");
				if (errors!=null && errors.size()>0) {
					List<PendingUploadBeneficiaryErrorModel> errs = new ArrayList<>();
					for (Map<String, Object> error : errors) {
						PendingUploadBeneficiaryErrorModel errorModel = new PendingUploadBeneficiaryErrorModel();
						errorModel.setLine((Integer)error.get("line"));
						errorModel.setLineType((String)error.get("lineType"));
						errorModel.setErrorCode((String)error.get("errorCode"));
						errorModel.setParent(pm);
						
						errs.add(errorModel);
					}
					pm.setTotalError(errors.size());
					pm.setErrors(errs);
				}
				
				pm.setStatus(ApplicationConstants.PENDING_UPLOAD_STATUS_COMPLETED);
				pm.setUpdatedBy(pm.getCreatedBy());
				pm.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
			} else {
				pm.setStatus(ApplicationConstants.PENDING_UPLOAD_STATUS_FAILED);
			}
			
			beneficiaryRepo.save(pm);
			
			return pm;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}
	
	@Override
	public Map<String, Object> searchBeneficiaryUploadById(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String pendingUploadId = (String)map.get("pendingUploadId");			
			PendingUploadBeneficiaryModel pm = beneficiaryRepo.detailPendingUploadById(pendingUploadId);
			if (pm == null)
				throw new BusinessException("GPT-0100001");
			
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("pendingUploadId", pm.getId());
			resultMap.put("fileFormat", pm.getFileFormat());
			resultMap.put(ApplicationConstants.FILENAME, pm.getFileName());
			resultMap.put("fileDescription", ValueUtils.getValue(pm.getFileDescription()));
			resultMap.put("status", pm.getStatus());
			resultMap.put("uploadDateTime", pm.getUploadDate());
			resultMap.put("totalRecord", pm.getTotalRecord());
			resultMap.put("totalError", pm.getTotalError());
			resultMap.put("beneficiary", pm.getBeneficiaryUploadType());
			
			
			if (pm.getTotalError()==0) {
				Page<PendingUploadBeneficiaryDetailModel> result = beneficiaryRepo.searchPendingUploadDetail(pendingUploadId, 
						PagingUtils.createPageRequest(map));

				resultMap.put("details", setDetailModelToMap(result.getContent()));
				
				PagingUtils.setPagingInfo(resultMap, result);
			} else {
				Page<PendingUploadBeneficiaryErrorModel> result = beneficiaryRepo.searchPendingUploadError(pendingUploadId, 
						PagingUtils.createPageRequest(map));

				resultMap.put("details", setErrorModelToMap(result.getContent()));
				
				PagingUtils.setPagingInfo(resultMap, result);
			}
						
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}
	
	private List<Map<String, Object>> setDetailModelToMap(List<PendingUploadBeneficiaryDetailModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (PendingUploadBeneficiaryDetailModel model : list) {
			Map<String, Object> map = new HashMap<>();
			map.put("benAccountNo", model.getBenAccountNo());
			map.put("benAccountName", model.getBenAccountName());
			
			StringBuilder description = new StringBuilder();
			if (model.getRemark1()!=null && model.getRemark1().trim().length()>0)
				description.append(model.getRemark1());

			if (model.getRemark2()!=null && model.getRemark2().trim().length()>0) {
			    if (description.toString().trim().length()>0)
			    	description.append(" ");
			    description.append(model.getRemark2());
			}

			if (model.getRemark3()!=null && model.getRemark3().trim().length()>0) {
			    if (description.toString().trim().length()>0)
			    	description.append(" ");
			    description.append(model.getRemark3());
			}
			
			map.put("description", ValueUtils.getValue(description));
			map.put("benAddress1", ValueUtils.getValue(model.getBenAddress1()));
			map.put("benAddress2", ValueUtils.getValue(model.getBenAddress2()));
			map.put("benAddress3", ValueUtils.getValue(model.getBenAddress3()));
			map.put("isNotify", ValueUtils.getValue(model.getBenNotificationFlag()));
			map.put("notifyEmail", ValueUtils.getValue(model.getBenNotificationValue()));
			map.put("benAccountCurrency", ValueUtils.getValue(model.getBenAccountCurrencyCode()));
			map.put("benAliasName", ValueUtils.getValue(model.getBenAliasName()));
			map.put("benBankCode", ValueUtils.getValue(model.getBenBankCode()));
			map.put("isBenResident", ValueUtils.getValue(model.getLldIsBenResidence()));
			map.put("benResidentCountryCode", ValueUtils.getValue(model.getLldBenResidenceCountryCode()));
			map.put("isBenCitizen", ValueUtils.getValue(model.getLldIsBenCitizen()));
			map.put("benCitizenCountryCode", ValueUtils.getValue(model.getLldBenCitizenCountryCode()));
			map.put("beneTypeDomestic", ValueUtils.getValue(model.getBeneTypeDomestic()));
			map.put("benBankCountryCode", ValueUtils.getValue(model.getBenBankCountryCode()));
			map.put("isBenIdentical", ValueUtils.getValue(model.getLldIsBenIdentical()));
			map.put("isBenAffiliated", ValueUtils.getValue(model.getLldIsBenAffiliated()));
			
			map.put("vaNo", model.getVaNo());
			map.put("vaName", model.getVaName());
			map.put("vaType", model.getVaType());
			map.put("vaAmount", model.getVaAmount());
			

			resultList.add(map);
		}

		return resultList;
	}	
	
	private List<Map<String, Object>> setErrorModelToMap(List<PendingUploadBeneficiaryErrorModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (PendingUploadBeneficiaryErrorModel model : list) {
			Map<String, Object> map = new HashMap<>();
			map.put("line", model.getLine());
			map.put("lineType", model.getLineType());

			String errorCode = model.getErrorCode();			
			map.put("errorReason", message.getMessage(errorCode, null, errorCode, LocaleContextHolder.getLocale()));

			resultList.add(map);
		}

		return resultList;
	}
	
	@Override
	public Map<String, Object> deleteBeneficiaryUpload(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			List<String> pendingUploadIds = (List<String>)map.get("pendingUploadId");
			
			for(String pendingUploadId : pendingUploadIds) {
				PendingUploadBeneficiaryModel model = beneficiaryRepo.findByIdAndIsProcessedAndDeleteFlag(pendingUploadId, ApplicationConstants.NO, ApplicationConstants.NO);			
				if (model==null)
					throw new BusinessException("GPT-0100001");
				
				model.setDeleteFlag(ApplicationConstants.YES);
				model.setUpdatedDate(DateUtils.getCurrentTimestamp());
				model.setUpdatedBy((String)map.get(ApplicationConstants.LOGIN_USERCODE));

				beneficiaryRepo.save(model);
			}
			
			Map<String, Object> result = new HashMap<>();
			result.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100202");
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}	
	
	@Override
	public Map<String, Object> searchBeneficiaryUploadByIdValidOnly(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String pendingUploadId = (String)map.get("pendingUploadId");
			PendingUploadBeneficiaryModel pm = beneficiaryRepo.detailPendingUploadByIdValidOnly(pendingUploadId);
			if (pm == null)
				throw new BusinessException("GPT-0100001");
			
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("pendingUploadId", pm.getId());
			resultMap.put("fileFormat", pm.getFileFormat());
			resultMap.put(ApplicationConstants.FILENAME, pm.getFileName());
			resultMap.put("fileDescription", ValueUtils.getValue(pm.getFileDescription()));
			resultMap.put("status", pm.getStatus());
			resultMap.put("uploadDateTime", pm.getUploadDate());
			resultMap.put("beneficiary", pm.getBeneficiaryUploadType());
			
//			resultMap.put("totalRecord", pm.getTotalRecord()); // will be set in PagingUtils.setPagingInfo below
			resultMap.put("totalError", pm.getTotalError());
			
			Page<PendingUploadBeneficiaryDetailModel> result = beneficiaryRepo.searchPendingUploadDetail(pendingUploadId, PagingUtils.createPageRequest(map));
			resultMap.put("details", setDetailModelToMap(result.getContent()));
			
			PagingUtils.setPagingInfo(resultMap, result);
			
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}
}
