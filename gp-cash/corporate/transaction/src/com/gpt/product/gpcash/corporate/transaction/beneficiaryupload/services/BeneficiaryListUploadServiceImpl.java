package com.gpt.product.gpcash.corporate.transaction.beneficiaryupload.services;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.domesticbank.model.DomesticBankModel;
import com.gpt.component.maintenance.internationalbank.model.InternationalBankModel;
import com.gpt.component.maintenance.parametermt.model.BeneficiaryTypeModel;
import com.gpt.component.maintenance.parametermt.model.CountryModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListDomesticModel;
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListInHouseModel;
import com.gpt.product.gpcash.corporate.beneficiarylist.model.BeneficiaryListInternationalModel;
import com.gpt.product.gpcash.corporate.beneficiarylist.repository.BeneficiaryListDomesticRepository;
import com.gpt.product.gpcash.corporate.beneficiarylist.repository.BeneficiaryListInHouseRepository;
import com.gpt.product.gpcash.corporate.beneficiarylist.repository.BeneficiaryListInternationalRepository;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadBeneficiaryDetailModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.model.PendingUploadBeneficiaryModel;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.repository.PendingUploadBeneficiaryRepository;
import com.gpt.product.gpcash.corporate.transaction.validation.services.TransactionValidationService;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class BeneficiaryListUploadServiceImpl implements BeneficiaryListUploadService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${gpcash.beneficiary.upload.path}")
	private String pathUpload;
	
	@Value("${gpcash.beneficiary.download.sample.path}")
	private String pathDownloadSampleFile;
	
	@Value("${gpcash.beneficiary.download.sample.filename}")
	private String downloadSampleFileName;	
		
	@Autowired
	private TransactionValidationService transactionValidationService;

	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;	
	
	@Autowired
	private PendingUploadBeneficiaryRepository pendingUploadRepo;
	
	@Autowired
	private BeneficiaryListInHouseRepository beneficiaryInHouseRepo;
	
	@Autowired
	private BeneficiaryListDomesticRepository beneficiaryDomesticRepo;
	
	@Autowired
	private BeneficiaryListInternationalRepository beneficiaryInternationalRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;

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
		resultMap.put("beneficiary", map.get("beneficiary"));
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
			
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String pendingUploadId = (String)map.get("pendingUploadId");
			
			CorporateUserPendingTaskVO vo = setCorporateUserPendingTaskVO(map);
			vo.setJsonObject(map); 

			// check unique pending task
//			pendingTaskService.checkUniquePendingTask(vo);

			List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>) map.get("details");
			
			if (map.get(ApplicationConstants.WF_ACTION).equals("CREATE_OVERBOOKING")) {
				
				for(Map<String, Object> benAccountMap : benAccountList){
					String benAccountNo = (String) benAccountMap.get("benAccountNo");
					checkUniqueRecordInhouse(benAccountNo, corporateId);
				}
				vo.setAction("CREATE_OVERBOOKING");
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("CREATE_DOMESTIC")) {
				
				for(Map<String, Object> benAccountMap : benAccountList){
					checkCustomValidationDomestic(benAccountMap);
					String benAccountNo = (String) benAccountMap.get("benAccountNo");
					checkUniqueRecordDomestic(benAccountNo, corporateId);					
				}
				vo.setAction("CREATE_DOMESTIC");
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("CREATE_DOMESTIC_ONLINE")) {
				for(Map<String, Object> benAccountMap : benAccountList){
					maintenanceRepo.isDomesticBankValid((String) benAccountMap.get("benBankCode"));
					String benAccountNo = (String) benAccountMap.get("benAccountNo");
					checkUniqueRecordDomesticOnline(benAccountNo, corporateId);
				}
				vo.setAction("CREATE_DOMESTIC_ONLINE");
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("CREATE_INTERNATIONAL")) {
				for(Map<String, Object> benAccountMap : benAccountList){
					checkCustomValidationInternational(benAccountMap);
//					String benAccountNo = (String) benAccountMap.get("benAccountNo");
					
//					checkUniqueRecordInternational(benAccountNo, corporateId);
				}
				vo.setAction("CREATE_INTERNATIONAL");

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
	
	private void checkUniqueRecordInhouse(String accountNo, String corporateId) throws Exception {
		List<BeneficiaryListInHouseModel> modelList = beneficiaryInHouseRepo
				.findByBenAccountNoAndCorporateId(accountNo, corporateId);

		if (modelList.size() > 0) {
			BeneficiaryListInHouseModel model = modelList.get(0);
			if (model != null && ApplicationConstants.NO.equals(model.getDeleteFlag())) {
				throw new BusinessException("GPT-0100004");
			}
		}
	}
	
	private void checkUniqueRecordDomestic(String accountNo, String corporateId) throws Exception {
		List<BeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo
				.findByBenAccountNoAndCorporateId(accountNo, corporateId);

		if (modelList.size() > 0) {
			BeneficiaryListDomesticModel model = modelList.get(0);
			if (model != null && ApplicationConstants.NO.equals(model.getDeleteFlag())) {
				throw new BusinessException("GPT-0100004");
			}
		}
	}
	
	private void checkUniqueRecordDomesticOnline(String benAccountNo, String corporateId) throws Exception{
		List<BeneficiaryListDomesticModel> modelList = beneficiaryDomesticRepo.findByBenAccountNoAndCorporateIdAndIsBenOnline(benAccountNo, corporateId, ApplicationConstants.YES);

		if (modelList.size() > 0) {
			BeneficiaryListDomesticModel model = modelList.get(0);
			if (model != null && ApplicationConstants.NO.equals(model.getDeleteFlag())) {
				throw new BusinessException("GPT-0100004");
			}
		}
	}
	
	private void checkCustomValidationDomestic(Map<String, Object> map) throws BusinessException, Exception {
		try {
			maintenanceRepo.isDomesticBankValid((String) map.get("benBankCode"));
			maintenanceRepo.isBeneficiaryTypeValid((String) map.get("beneTypeDomestic"));
			
			if(ApplicationConstants.NO.equals((String) map.get("isBenResident"))){
				if(ValueUtils.hasValue(map.get("benResidentCountryCode"))){
					maintenanceRepo.isCountryValid((String) map.get("benResidentCountryCode"));
				} else {
					throw new BusinessException("GPT-0100089");
				}
			}
			
			if(ApplicationConstants.NO.equals((String) map.get("isBenCitizen"))){
				if(ValueUtils.hasValue(map.get("benCitizenCountryCode"))){
					maintenanceRepo.isCountryValid((String) map.get("benCitizenCountryCode"));
				} else {
					throw new BusinessException("GPT-0100090");
				}
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
	
	private void checkCustomValidationInternational(Map<String, Object> map) throws BusinessException, Exception {
		try {
			maintenanceRepo.isInternationalBankValid((String) map.get("benBankCode"));
			
			if(ApplicationConstants.NO.equals((String) map.get("isBenResident"))){
				if(ValueUtils.hasValue(map.get("benResidentCountryCode"))){
					maintenanceRepo.isCountryValid((String) map.get("benResidentCountryCode"));
				} else {
					throw new BusinessException("GPT-0100089");
				}
			}
			
			if(ApplicationConstants.NO.equals((String) map.get("isBenCitizen"))){
				if(ValueUtils.hasValue(map.get("benCitizenCountryCode"))){
					maintenanceRepo.isCountryValid((String) map.get("benCitizenCountryCode"));
				} else {
					throw new BusinessException("GPT-0100090");
				}
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
	
	private void updatePendingUpload(String pendingUploadId, String updatedBy, String isProcessed, String pendingTaskId) throws Exception {		
		PendingUploadBeneficiaryModel model = pendingUploadRepo.detailPendingUploadById(pendingUploadId);
		
		if (model == null)
			throw new BusinessException("GPT-0100001");
		
		// set default value
		model.setIsProcessed(isProcessed);
		model.setPendingTaskId(pendingTaskId);		
		model.setUpdatedDate(DateUtils.getCurrentTimestamp());
		model.setUpdatedBy(updatedBy);

		pendingUploadRepo.save(model);
	}
		
	private CorporateUserPendingTaskVO setCorporateUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		CorporateUserModel maker = corporateUtilsRepo.isCorporateUserValid(createdBy);
		String userGroupId = maker.getCorporateUserGroup().getId();
		String beneListType = (String) map.get("beneficiary");
		
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setCorporateId(corporateId);
		
//		String uniqueKey = corporateId.concat(beneListType).concat((String) map.get("benAccountNo"));
		
		List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>) map.get("details");
		for(Map<String, Object> benAccountMap : benAccountList){
			String uniqueKey = corporateId.concat(ApplicationConstants.DELIMITER_PIPE)
					.concat(beneListType).concat(ApplicationConstants.DELIMITER_PIPE)
					.concat((String) benAccountMap.get("benAccountNo"));
			//check unique pending task
			vo.setUniqueKey(uniqueKey);
			vo.setUniqueKeyDisplay((String) benAccountMap.get("benAccountNo"));
			pendingTaskService.checkUniquePendingTaskLike(vo);
		}
		
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERCODE));
		vo.setService("BeneficiaryListUploadSC");
		vo.setTransactionAmount(new BigDecimal(ApplicationConstants.APPROVAL_MATRIX_NON_TRANSACTION));
		
		//set userGroupId
		vo.setUserGroupId(userGroupId);
		
		return vo;
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
//			String beneListType = (String) map.get("beneficiary");
			
			String pendingUploadId = (String)map.get("pendingUploadId");
			
			PendingUploadBeneficiaryModel pendingUploadModel = pendingUploadRepo.findByIdAndIsProcessed(pendingUploadId, ApplicationConstants.YES);
			
			if (pendingUploadModel == null)
				throw new BusinessException("GPT-0100001");
			
			List<Map<String, Object>> benAccountList = (ArrayList<Map<String,Object>>)map.get("details");
			
			if ("CREATE_OVERBOOKING".equals(vo.getAction())) {		
				List<PendingUploadBeneficiaryDetailModel> pumDetails = pendingUploadModel.getDetails();
				List<BeneficiaryListInHouseModel> inhouseList = new ArrayList<>(pendingUploadModel.getDetails().size());
				if (ValueUtils.hasValue(pumDetails)) {
					for(PendingUploadBeneficiaryDetailModel pendingUploadDetail : pumDetails) {
						inhouseList.add(setPumToModelInhouse(map, pendingUploadDetail));
					}
				}
				beneficiaryInHouseRepo.save(inhouseList);
				
			} else if ("CREATE_DOMESTIC".equals(vo.getAction())) {
				List<PendingUploadBeneficiaryDetailModel> pumDetails = pendingUploadModel.getDetails();
				List<BeneficiaryListDomesticModel> domesticList = new ArrayList<>(pendingUploadModel.getDetails().size());
				if (ValueUtils.hasValue(pumDetails)) {
					for(PendingUploadBeneficiaryDetailModel pendingUploadDetail : pumDetails) {
						domesticList.add(setPumToModelDomestic(map, pendingUploadDetail, false));
					}
				}
				beneficiaryDomesticRepo.save(domesticList);
				
			} else if ("CREATE_DOMESTIC_ONLINE".equals(vo.getAction())) {
				List<PendingUploadBeneficiaryDetailModel> pumDetails = pendingUploadModel.getDetails();
				List<BeneficiaryListDomesticModel> domesticList = new ArrayList<>(pendingUploadModel.getDetails().size());
				if (ValueUtils.hasValue(pumDetails)) {
					for(PendingUploadBeneficiaryDetailModel pendingUploadDetail : pumDetails) {
						domesticList.add(setPumToModelDomestic(map, pendingUploadDetail, true));
					}
				}
				beneficiaryDomesticRepo.save(domesticList);
				
			} else if ("CREATE_INTERNATIONAL".equals(vo.getAction())) {
				List<PendingUploadBeneficiaryDetailModel> pumDetails = pendingUploadModel.getDetails();
				List<BeneficiaryListInternationalModel> internationalList = new ArrayList<>(pendingUploadModel.getDetails().size());
				if (ValueUtils.hasValue(pumDetails)) {
					for(PendingUploadBeneficiaryDetailModel pendingUploadDetail : pumDetails) {
						internationalList.add(setPumToModelInternational(map, pendingUploadDetail));
					}
				}
				beneficiaryInternationalRepo.save(internationalList);
				
			}
			
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}
	
	private BeneficiaryListInternationalModel setPumToModelInternational(Map<String, Object> map, PendingUploadBeneficiaryDetailModel pendingUploadDetail) throws Exception {
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		CorporateUserModel corpUser = corporateUtilsRepo.isCorporateUserValid(userCode);
		CorporateModel corporate = corporateUtilsRepo.isCorporateValid(corporateId);
		String localCountryCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_COUNTRY_CODE).getValue();
		
		BeneficiaryListInternationalModel beneficiaryInternational = new BeneficiaryListInternationalModel();
		beneficiaryInternational.setBenAccountNo(pendingUploadDetail.getBenAccountNo());
		beneficiaryInternational.setBenAccountName(pendingUploadDetail.getBenAccountName());
		beneficiaryInternational.setBenAccountCurrency(pendingUploadDetail.getBenAccountCurrencyCode());
		beneficiaryInternational.setBenAliasName(pendingUploadDetail.getBenAliasName());
		beneficiaryInternational.setBenAddr1(pendingUploadDetail.getBenAddress1());
		beneficiaryInternational.setBenAddr2(pendingUploadDetail.getBenAddress2());
		beneficiaryInternational.setBenAddr3(pendingUploadDetail.getBenAddress3());
		beneficiaryInternational.setIsNotifyBen(pendingUploadDetail.getBenNotificationFlag());
		beneficiaryInternational.setEmail(pendingUploadDetail.getBenNotificationValue());
		beneficiaryInternational.setCorporate(corporate);
		beneficiaryInternational.setCorporateUserGroup(corpUser.getCorporateUserGroup());
		beneficiaryInternational.setDeleteFlag(ApplicationConstants.NO);
		beneficiaryInternational.setCreatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryInternational.setCreatedBy(corpUser.getUser().getCode());
		beneficiaryInternational.setUpdatedDate(null);
		beneficiaryInternational.setUpdatedBy(null);
		
		beneficiaryInternational.setLldIsBenResidence(pendingUploadDetail.getLldIsBenResidence());
		CountryModel benResidentCountry = new CountryModel();
		benResidentCountry.setCode(localCountryCode);
		beneficiaryInternational.setLldBenResidenceCountry(benResidentCountry);
		
		if(ApplicationConstants.NO.equals(beneficiaryInternational.getLldIsBenResidence())){
			benResidentCountry.setCode(pendingUploadDetail.getLldBenResidenceCountryCode());
			beneficiaryInternational.setLldBenResidenceCountry(benResidentCountry);
		}
		
		beneficiaryInternational.setLldIsBenCitizen(pendingUploadDetail.getLldIsBenCitizen());
		CountryModel benCitizenCountry = new CountryModel();
		benCitizenCountry.setCode(localCountryCode);
		beneficiaryInternational.setLldBenCitizenCountry(benCitizenCountry);
		
		if(ApplicationConstants.NO.equals(beneficiaryInternational.getLldIsBenCitizen())){
			benCitizenCountry.setCode(pendingUploadDetail.getLldBenCitizenCountryCode());
			beneficiaryInternational.setLldBenCitizenCountry(benCitizenCountry);
		}
		
		beneficiaryInternational.setLldIsBenAffiliated(pendingUploadDetail.getLldIsBenAffiliated());
		beneficiaryInternational.setLldIsBenIdentical(pendingUploadDetail.getLldIsBenIdentical());
		
		InternationalBankModel intBank = new InternationalBankModel();
		intBank.setCode(pendingUploadDetail.getBenBankCode());
		beneficiaryInternational.setBenInternationalBankCode(intBank);
		
		CountryModel bankCountryCode = new CountryModel();
		bankCountryCode.setCode(pendingUploadDetail.getBenBankCountryCode());
		beneficiaryInternational.setBenInternationalCountry(bankCountryCode);
		
		return beneficiaryInternational;
	}

	private BeneficiaryListDomesticModel setPumToModelDomestic(Map<String, Object> map, PendingUploadBeneficiaryDetailModel pendingUploadDetail, boolean isOnline) throws Exception {
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		CorporateUserModel corpUser = corporateUtilsRepo.isCorporateUserValid(userCode);
		CorporateModel corporate = corporateUtilsRepo.isCorporateValid(corporateId);
		String localCountryCode = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_COUNTRY_CODE).getValue();
		
		BeneficiaryListDomesticModel beneficiaryDomestic = new BeneficiaryListDomesticModel();
		beneficiaryDomestic.setBenAccountNo(pendingUploadDetail.getBenAccountNo());
		beneficiaryDomestic.setBenAccountName(pendingUploadDetail.getBenAccountName());
		beneficiaryDomestic.setBenAccountCurrency(pendingUploadDetail.getBenAccountCurrencyCode());
		beneficiaryDomestic.setBenAliasName(pendingUploadDetail.getBenAliasName());
		beneficiaryDomestic.setBenAddr1(pendingUploadDetail.getBenAddress1());
		beneficiaryDomestic.setBenAddr2(pendingUploadDetail.getBenAddress2());
		beneficiaryDomestic.setBenAddr3(pendingUploadDetail.getBenAddress3());
		beneficiaryDomestic.setIsBenOnline(ApplicationConstants.NO);
		
		if (isOnline) {
			beneficiaryDomestic.setIsBenOnline(ApplicationConstants.YES);
		} else {
			beneficiaryDomestic.setIsBenOnline(ApplicationConstants.NO);
			CountryModel localCountry = new CountryModel();
			localCountry.setCode(localCountryCode);
			beneficiaryDomestic.setLldBenResidenceCountry(localCountry);
			
			beneficiaryDomestic.setLldIsBenResidence(pendingUploadDetail.getLldIsBenResidence());
			if(ApplicationConstants.NO.equals(beneficiaryDomestic.getLldIsBenResidence())){
				CountryModel benResidentCountry = new CountryModel();
				benResidentCountry.setCode(pendingUploadDetail.getLldBenResidenceCountryCode());
				beneficiaryDomestic.setLldBenResidenceCountry(benResidentCountry);
			}
			
			beneficiaryDomestic.setLldBenCitizenCountry(localCountry);
			beneficiaryDomestic.setLldIsBenCitizen(pendingUploadDetail.getLldIsBenCitizen());
			if(ApplicationConstants.NO.equals(beneficiaryDomestic.getLldIsBenCitizen())){
				CountryModel benCitizenCountry = new CountryModel();
				benCitizenCountry.setCode(pendingUploadDetail.getLldBenCitizenCountryCode());
				beneficiaryDomestic.setLldBenCitizenCountry(benCitizenCountry);
			}
			
			BeneficiaryTypeModel benType = new BeneficiaryTypeModel();
			benType.setCode(pendingUploadDetail.getBeneTypeDomestic());
			beneficiaryDomestic.setBenType(benType);
		}
		
		DomesticBankModel domBank = new DomesticBankModel();
		domBank.setCode(pendingUploadDetail.getBenBankCode());
		beneficiaryDomestic.setBenDomesticBankCode(domBank);
		
		beneficiaryDomestic.setIsNotifyBen(pendingUploadDetail.getBenNotificationFlag());
		beneficiaryDomestic.setEmail(pendingUploadDetail.getBenNotificationValue());

		beneficiaryDomestic.setCorporate(corporate);
		beneficiaryDomestic.setCorporateUserGroup(corpUser.getCorporateUserGroup());
		
		beneficiaryDomestic.setDeleteFlag(ApplicationConstants.NO);
		beneficiaryDomestic.setCreatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryDomestic.setCreatedBy(corpUser.getUser().getCode());
		beneficiaryDomestic.setUpdatedDate(null);
		beneficiaryDomestic.setUpdatedBy(null);

		return beneficiaryDomestic;
	}

	private BeneficiaryListInHouseModel setPumToModelInhouse(Map<String, Object> map, PendingUploadBeneficiaryDetailModel pendingUploadDetail) throws Exception {
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String beneListType = (String) map.get("beneficiary");
		CorporateUserModel corpUser = corporateUtilsRepo.isCorporateUserValid(userCode);
		CorporateModel corporate = corporateUtilsRepo.isCorporateValid(corporateId);
		
		BeneficiaryListInHouseModel beneficiaryInHouse = new BeneficiaryListInHouseModel();
		beneficiaryInHouse.setBenAccountNo(pendingUploadDetail.getBenAccountNo());
		beneficiaryInHouse.setBenAccountName(pendingUploadDetail.getBenAccountName());
		beneficiaryInHouse.setBenAccountCurrency(pendingUploadDetail.getBenAccountCurrencyCode());
		beneficiaryInHouse.setIsNotifyBen(pendingUploadDetail.getBenNotificationFlag());
		beneficiaryInHouse.setEmail(pendingUploadDetail.getBenNotificationValue());
		beneficiaryInHouse.setDeleteFlag(ApplicationConstants.NO);
		beneficiaryInHouse.setCreatedDate(DateUtils.getCurrentTimestamp());
		beneficiaryInHouse.setCreatedBy(corpUser.getUser().getCode());
		beneficiaryInHouse.setUpdatedDate(null);
		beneficiaryInHouse.setUpdatedBy(null);
		beneficiaryInHouse.setIsBenVirtualAccount(ApplicationConstants.NO);
		
		if (beneListType.equals(ApplicationConstants.BENELIST_TYPE_VIRTUAL_ACCOUNT)) {
			beneficiaryInHouse.setIsBenVirtualAccount(ApplicationConstants.YES);
		}
		
		beneficiaryInHouse.setCorporate(corporate);
		beneficiaryInHouse.setCorporateUserGroup(corpUser.getCorporateUserGroup());

		return beneficiaryInHouse;
	}
	
	
	
	@Override
	public CorporateUserPendingTaskVO reject(CorporateUserPendingTaskVO vo)	throws ApplicationException, BusinessException {
		return null;
	}
	
}