package com.gpt.component.maintenance.internationalbank.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.internationalbank.model.InternationalBankModel;
import com.gpt.component.maintenance.parametermt.model.CountryModel;
import com.gpt.component.maintenance.parametermt.model.InternationalBankOrganizationModel;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;

@Service
@Transactional(rollbackFor = Exception.class)
public class InternationalBankServiceImpl implements InternationalBankService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MaintenanceRepository maintenanceRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<InternationalBankModel> result = maintenanceRepo.getInternationalBankRepo().search(map, PagingUtils.createPageRequest(map));

			if(ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))){
				resultMap.put("result", setModelToMap(result.getContent(), false));
			} else {
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

	private List<Map<String, Object>> setModelToMap(List<InternationalBankModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (InternationalBankModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(InternationalBankModel model, boolean isGetDetail) {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put("name", ValueUtils.getValue(model.getName()));
		
		CountryModel country = model.getCountry();
		map.put("countryCode", country.getCode());
		map.put("countryName", country.getName());
		
		map.put("cityName", ValueUtils.getValue(model.getCityName()));
		
		InternationalBankOrganizationModel intBankOrg = model.getInternationalBankOrganization();
		map.put("internationalBankOrganizationCode", intBankOrg.getCode());
		map.put("internationalBankOrganizationName", intBankOrg.getName());
		
		if(isGetDetail){
			map.put("organizationUnitName", ValueUtils.getValue(model.getOrganizationUnitName()));
			map.put("cityName", ValueUtils.getValue(model.getCityName()));
			map.put("substateName", ValueUtils.getValue(model.getSubstateName()));
			map.put("stateName", ValueUtils.getValue(model.getStateName()));
			map.put("postcode", ValueUtils.getValue(model.getPostcode()));
			map.put("address1", ValueUtils.getValue(model.getAddress1()));
			map.put("address2", ValueUtils.getValue(model.getAddress2()));
			map.put("address3", ValueUtils.getValue(model.getAddress3()));
			
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
		}
		return map;
	}

	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			PendingTaskVO vo = setPendingTaskVO(map); 
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				checkCustomValidation(map);
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				checkUniqueRecord((String) map.get(ApplicationConstants.STR_CODE)); // cek ke table jika record already exist
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				checkCustomValidation(map);
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				//check existing record exist or not
				InternationalBankModel internationalBankOld = getExistingRecord((String)map.get(ApplicationConstants.STR_CODE), true);
				vo.setJsonObjectOld(setModelToMap(internationalBankOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
				
				//check existing record exist or not
				getExistingRecord((String)map.get(ApplicationConstants.STR_CODE), true);
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
	
	private InternationalBankModel getExistingRecord(String code, boolean isThrowError) throws BusinessException{
		InternationalBankModel model = maintenanceRepo.getInternationalBankRepo().findOne(code);
		
		if(model == null){
			if(isThrowError)
				throw new BusinessException("GPT-0100001");
		}else{
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				if(isThrowError){
					throw new BusinessException("GPT-0100001");
				}else{
					return null;
				}
			}
		}
		
		return model;
	}

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.STR_CODE));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("InternationalBankSC");

		return vo;
	}

	private InternationalBankModel setMapToModel(Map<String, Object> map) {
		InternationalBankModel internationalBank = new InternationalBankModel();
		internationalBank.setCode((String) map.get(ApplicationConstants.STR_CODE));
		internationalBank.setName((String) map.get(ApplicationConstants.STR_NAME));
		
		CountryModel country = new CountryModel();
		country.setCode((String) map.get("countryCode"));
		internationalBank.setCountry(country);

		InternationalBankOrganizationModel interBankOrg = new InternationalBankOrganizationModel();
		interBankOrg.setCode((String) map.get("internationalBankOrganizationCode"));
		internationalBank.setInternationalBankOrganization(interBankOrg);
		
		if (ValueUtils.hasValue(map.get("organizationUnitName")))
			internationalBank.setOrganizationUnitName((String) map.get("organizationUnitName"));
		
		if (ValueUtils.hasValue(map.get("cityName")))
			internationalBank.setCityName((String) map.get("cityName"));
		
		if (ValueUtils.hasValue(map.get("substateName")))
			internationalBank.setSubstateName((String) map.get("substateName"));
		
		if (ValueUtils.hasValue(map.get("stateName")))
			internationalBank.setStateName((String) map.get("stateName"));
		
		if (ValueUtils.hasValue(map.get("postcode")))
			internationalBank.setPostcode((String) map.get("postcode"));

		if (ValueUtils.hasValue(map.get("address1")))
			internationalBank.setAddress1((String) map.get("address1"));

		if (ValueUtils.hasValue(map.get("address2")))
			internationalBank.setAddress2((String) map.get("address2"));

		if (ValueUtils.hasValue(map.get("address3")))
			internationalBank.setAddress3((String) map.get("address3"));

		return internationalBank;
	}

	private void checkUniqueRecord(String code) throws Exception {
		InternationalBankModel internationalBank = maintenanceRepo.getInternationalBankRepo().findOne(code);

		if (internationalBank != null && ApplicationConstants.NO.equals(internationalBank.getDeleteFlag())) {
			throw new BusinessException("GPT-0100004");
		}
	}

	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				InternationalBankModel internationalBank = setMapToModel(map);
				InternationalBankModel internationalBankExisting = maintenanceRepo.getInternationalBankRepo().findOne(internationalBank.getCode());

				// cek jika record replacement
				if (internationalBankExisting != null
						&& ApplicationConstants.YES.equals(internationalBankExisting.getDeleteFlag())) {
					// set value yg bisa di ganti
					internationalBankExisting.setName(internationalBank.getName());
					internationalBankExisting.setOrganizationUnitName(internationalBank.getOrganizationUnitName());
					internationalBankExisting.setCountry(internationalBank.getCountry());
					internationalBankExisting.setInternationalBankOrganization(internationalBank.getInternationalBankOrganization());
					internationalBankExisting.setCityName(internationalBank.getCityName());
					internationalBankExisting.setSubstateName(internationalBank.getSubstateName());
					internationalBankExisting.setStateName(internationalBank.getStateName());
					internationalBankExisting.setPostcode(internationalBank.getPostcode());
					internationalBankExisting.setAddress1(internationalBank.getAddress1());
					internationalBankExisting.setAddress2(internationalBank.getAddress2());
					internationalBankExisting.setAddress3(internationalBank.getAddress3());

					
					// set default value
					internationalBankExisting.setIdx(ApplicationConstants.IDX);
					internationalBankExisting.setDeleteFlag(ApplicationConstants.NO);
					internationalBankExisting.setInactiveFlag(ApplicationConstants.NO);
					internationalBankExisting.setSystemFlag(ApplicationConstants.NO);
					internationalBankExisting.setCreatedDate(DateUtils.getCurrentTimestamp());
					internationalBankExisting.setCreatedBy(vo.getCreatedBy());

					// set reset value
					internationalBankExisting.setUpdatedDate(null);
					internationalBankExisting.setUpdatedBy(null);

					maintenanceRepo.getInternationalBankRepo().save(internationalBankExisting);
				} else {
					// set default value
					internationalBank.setIdx(ApplicationConstants.IDX);
					internationalBank.setDeleteFlag(ApplicationConstants.NO);
					internationalBank.setInactiveFlag(ApplicationConstants.NO);
					internationalBank.setSystemFlag(ApplicationConstants.NO);
					internationalBank.setCreatedDate(DateUtils.getCurrentTimestamp());
					internationalBank.setCreatedBy(vo.getCreatedBy());

					maintenanceRepo.getInternationalBankRepo().persist(internationalBank);
				}

			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				InternationalBankModel internationalBankNew = setMapToModel(map);

				InternationalBankModel internationalBankExisting = getExistingRecord(internationalBankNew.getCode(), true);

				// set value yg boleh di edit
				internationalBankExisting.setName(internationalBankNew.getName());
				internationalBankExisting.setOrganizationUnitName(internationalBankNew.getOrganizationUnitName());
				internationalBankExisting.setCountry(internationalBankNew.getCountry());
				internationalBankExisting.setInternationalBankOrganization(internationalBankNew.getInternationalBankOrganization());
				internationalBankExisting.setCityName(internationalBankNew.getCityName());
				internationalBankExisting.setSubstateName(internationalBankNew.getSubstateName());
				internationalBankExisting.setStateName(internationalBankNew.getStateName());
				internationalBankExisting.setPostcode(internationalBankNew.getPostcode());
				internationalBankExisting.setAddress1(internationalBankNew.getAddress1());
				internationalBankExisting.setAddress2(internationalBankNew.getAddress2());
				internationalBankExisting.setAddress3(internationalBankNew.getAddress3());

				internationalBankExisting.setUpdatedDate(DateUtils.getCurrentTimestamp());
				internationalBankExisting.setUpdatedBy(vo.getCreatedBy());

				maintenanceRepo.getInternationalBankRepo().save(internationalBankExisting);
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				//check existing record exist or not
				InternationalBankModel internationalBank = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);

				internationalBank.setDeleteFlag(ApplicationConstants.YES);
				internationalBank.setUpdatedDate(DateUtils.getCurrentTimestamp());
				internationalBank.setUpdatedBy(vo.getCreatedBy());

				maintenanceRepo.getInternationalBankRepo().save(internationalBank);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return vo;
	}

	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {

	}
}
