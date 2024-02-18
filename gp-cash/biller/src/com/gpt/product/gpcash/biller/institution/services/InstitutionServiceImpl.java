package com.gpt.product.gpcash.biller.institution.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.biller.institution.model.InstitutionModel;
import com.gpt.product.gpcash.biller.institution.repository.InstitutionRepository;
import com.gpt.product.gpcash.biller.institutioncategory.model.InstitutionCategoryModel;

@Service
@Transactional(rollbackFor = Exception.class)
public class InstitutionServiceImpl implements InstitutionService {

	@Autowired
	private InstitutionRepository institutionRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<InstitutionModel> result = institutionRepo.search(map, PagingUtils.createPageRequest(map));

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

	private List<Map<String, Object>> setModelToMap(List<InstitutionModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (InstitutionModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(InstitutionModel model, boolean isGetDetail) {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		
		if(Locale.ENGLISH.equals(LocaleContextHolder.getLocale())) {
			map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getNameEng()));
		} else {
			map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		}
		
		map.put("dscp", ValueUtils.getValue(model.getDscp()));
		
		if(Locale.ENGLISH.equals(LocaleContextHolder.getLocale())) {
			map.put("name1", ValueUtils.getValue(model.getName1()));
			map.put("name2", ValueUtils.getValue(model.getName2()));
			map.put("name3", ValueUtils.getValue(model.getName3()));
			map.put("name4", ValueUtils.getValue(model.getName4()));
			map.put("name5", ValueUtils.getValue(model.getName5()));
		} else {
			map.put("name1", ValueUtils.getValue(model.getNameEng1()));
			map.put("name2", ValueUtils.getValue(model.getNameEng2()));
			map.put("name3", ValueUtils.getValue(model.getNameEng3()));
			map.put("name4", ValueUtils.getValue(model.getNameEng4()));
			map.put("name5", ValueUtils.getValue(model.getNameEng5()));
		}
		
		if(isGetDetail){
			map.put("amountSelection", ValueUtils.getValue(model.getAmountSelection()));
			map.put("billOnlineFlag", ValueUtils.getValue(model.getBillOnlineFlag()));
			map.put("charges", ValueUtils.getValue(model.getCharges()));
			map.put("institutionType", ValueUtils.getValue(model.getInstitutionType()));
			map.put("regex1", ValueUtils.getValue(model.getRegex1()));
			map.put("regex2", ValueUtils.getValue(model.getRegex2()));
			map.put("regex3", ValueUtils.getValue(model.getRegex3()));
			map.put("regex4", ValueUtils.getValue(model.getRegex4()));
			map.put("regex5", ValueUtils.getValue(model.getRegex5()));
			
			CurrencyModel currency = model.getAmountCurrency();
			map.put("amountCurrencyCode", ValueUtils.getValue(currency.getCode()));
			
			InstitutionCategoryModel institutionCategory = model.getInstitutionCategory();
			map.put("institutionCategoryCode", ValueUtils.getValue(institutionCategory.getCode()));
			map.put("institutionCategoryName", ValueUtils.getValue(institutionCategory.getName()));
			
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
		}
		
		return map;
	}
	
	@Override
	public Map<String, Object> searchForBillPayment(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<InstitutionModel> result = institutionRepo.search(map, PagingUtils.createPageRequest(map));

			List<Map<String, Object>> resultList = new ArrayList<>();

			//khusus request buat ui
			for (InstitutionModel model : result) {
				Map<String, Object> institutionMap = new HashMap<>();
				institutionMap.put(ApplicationConstants.STR_CODE, model.getCode());
				if(Locale.ENGLISH.equals(LocaleContextHolder.getLocale())) {
					institutionMap.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getNameEng()));
				} else {
					institutionMap.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
				}
				institutionMap.put("dscp", ValueUtils.getValue(model.getDscp()));
				institutionMap.put("institutionType", model.getInstitutionType());
				institutionMap.put("amountType", model.getAmountSelection());
				
				List<Map<String, Object>> nameList = new ArrayList<>();
				
				if(Locale.ENGLISH.equals(LocaleContextHolder.getLocale())) {
					Map<String, Object> key = new HashMap<>();
					
					if(ValueUtils.hasValue(model.getNameEng1())) {
						key.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getNameEng1()));
						key.put("type", ValueUtils.getValue(model.getType1()));
						key.put("typeParameter", ValueUtils.getValue(model.getTypeParameter1()));
						key.put("typeParameterDataMap", ValueUtils.getValue(model.getTypeParameterDataMap1()));
						nameList.add(key);
					}
					
					if(ValueUtils.hasValue(model.getNameEng2())) {
						key = new HashMap<>();
						key.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getNameEng2()));
						key.put("type", ValueUtils.getValue(model.getType2()));
						key.put("typeParameter", ValueUtils.getValue(model.getTypeParameter2()));
						key.put("typeParameterDataMap", ValueUtils.getValue(model.getTypeParameterDataMap2()));
						nameList.add(key);
					}
					
					if(ValueUtils.hasValue(model.getNameEng3())) {
						key = new HashMap<>();
						key.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getNameEng3()));
						key.put("type", ValueUtils.getValue(model.getType3()));
						key.put("typeParameter", ValueUtils.getValue(model.getTypeParameter3()));
						key.put("typeParameterDataMap", ValueUtils.getValue(model.getTypeParameterDataMap3()));
						nameList.add(key);
					}
					
					if(ValueUtils.hasValue(model.getNameEng4())) {
						key = new HashMap<>();
						key.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getNameEng4()));
						key.put("type", ValueUtils.getValue(model.getType4()));
						key.put("typeParameter", ValueUtils.getValue(model.getTypeParameter4()));
						key.put("typeParameterDataMap", ValueUtils.getValue(model.getTypeParameterDataMap4()));
						nameList.add(key);
					}
					
					if(ValueUtils.hasValue(model.getNameEng5())) {
						key = new HashMap<>();
						key.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getNameEng5()));
						key.put("type", ValueUtils.getValue(model.getType5()));
						key.put("typeParameter", ValueUtils.getValue(model.getTypeParameter5()));
						key.put("typeParameterDataMap", ValueUtils.getValue(model.getTypeParameterDataMap5()));
						nameList.add(key);
					}
				} else {
					Map<String, Object> key = new HashMap<>();
					
					if(ValueUtils.hasValue(model.getName1())) {
						key.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName1()));
						key.put("type", ValueUtils.getValue(model.getType1()));
						key.put("typeParameter", ValueUtils.getValue(model.getTypeParameter1()));
						key.put("typeParameterDataMap", ValueUtils.getValue(model.getTypeParameterDataMap1()));
						nameList.add(key);
					}
					
					if(ValueUtils.hasValue(model.getName2())) {
						key = new HashMap<>();
						key.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName2()));
						key.put("type", ValueUtils.getValue(model.getType2()));
						key.put("typeParameter", ValueUtils.getValue(model.getTypeParameter2()));
						key.put("typeParameterDataMap", ValueUtils.getValue(model.getTypeParameterDataMap2()));
						nameList.add(key);
					}
					
					if(ValueUtils.hasValue(model.getName3())) {
						key = new HashMap<>();
						key.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName3()));
						key.put("type", ValueUtils.getValue(model.getType3()));
						key.put("typeParameter", ValueUtils.getValue(model.getTypeParameter3()));
						key.put("typeParameterDataMap", ValueUtils.getValue(model.getTypeParameterDataMap3()));
						nameList.add(key);
					}
					
					if(ValueUtils.hasValue(model.getName4())) {
						key = new HashMap<>();
						key.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName4()));
						key.put("type", ValueUtils.getValue(model.getType4()));
						key.put("typeParameter", ValueUtils.getValue(model.getTypeParameter4()));
						key.put("typeParameterDataMap", ValueUtils.getValue(model.getTypeParameterDataMap4()));
						nameList.add(key);
					}
					
					if(ValueUtils.hasValue(model.getName5())) {
						key = new HashMap<>();
						key.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName5()));
						key.put("type", ValueUtils.getValue(model.getType5()));
						key.put("typeParameter", ValueUtils.getValue(model.getTypeParameter5()));
						key.put("typeParameterDataMap", ValueUtils.getValue(model.getTypeParameterDataMap5()));
						nameList.add(key);
					}
				}
				
				institutionMap.put("nameList", nameList);
				
				resultList.add(institutionMap);
			}
			
			resultMap.put("result", resultList);
			
			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			PendingTaskVO vo = setPendingTaskVO(map);
			vo.setJsonObject(map); 
			
			checkCustomValidation(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				checkUniqueRecord((String) map.get(ApplicationConstants.STR_CODE)); 
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				InstitutionModel institutionOld = getExistingRecord(
						(String) map.get(ApplicationConstants.STR_CODE), true);
				vo.setJsonObjectOld(setModelToMap(institutionOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);

				// check existing record exist or not
				getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);
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
	
	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			String institutionType = (String) map.get("institutionType");
			String amountType = (String) map.get("amountType");
			if(ApplicationConstants.INSTITUTION_TYPE_ANY.equals(institutionType)){
				if(!(ApplicationConstants.INSTITUTION_AMOUNT_TYPE_USER_INPUT.equals(amountType))){
					throw new BusinessException("GPT-0100117");
				}
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	private InstitutionModel getExistingRecord(String code, boolean isThrowError) throws BusinessException {
		InstitutionModel model = institutionRepo.findOne(code);

		if (model == null) {
			if (isThrowError)
				throw new BusinessException("GPT-0100001");
		} else {
			if (ApplicationConstants.YES.equals(model.getDeleteFlag())) {
				if (isThrowError) {
					throw new BusinessException("GPT-0100001");
				} else {
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
		vo.setService("InstitutionSC");

		return vo;
	}

	private InstitutionModel setMapToModel(Map<String, Object> map) throws Exception {
		InstitutionModel institution = new InstitutionModel();
		institution.setCode((String) map.get(ApplicationConstants.STR_CODE));
		institution.setName((String) map.get(ApplicationConstants.STR_NAME));
		institution.setDscp((String) map.get("dscp"));
		institution.setAmountSelection((String) map.get("amountType"));
		institution.setBillOnlineFlag((String) map.get("billOnlineFlag"));
		institution.setCharges(new BigDecimal (map.get("charges").toString()));
		institution.setInstitutionType((String) map.get("institutionType"));
		institution.setName1((String) map.get("name1"));
		institution.setName2((String) map.get("name2"));
		institution.setName3((String) map.get("name3"));
		institution.setName4((String) map.get("name4"));
		institution.setName5((String) map.get("name5"));
		institution.setNameEng1((String) map.get("nameEng1"));
		institution.setNameEng2((String) map.get("nameEng2"));
		institution.setNameEng3((String) map.get("nameEng3"));
		institution.setNameEng4((String) map.get("nameEng4"));
		institution.setNameEng5((String) map.get("nameEng5"));
		institution.setRegex1((String) map.get("regex1"));
		institution.setRegex2((String) map.get("regex2"));
		institution.setRegex3((String) map.get("regex3"));
		institution.setRegex4((String) map.get("regex4"));
		institution.setRegex5((String) map.get("regex5"));
		
		CurrencyModel currency = new CurrencyModel();
		
		if(ValueUtils.hasValue(map.get("amountCurrencyCode"))){
			currency.setCode((String) map.get("amountCurrencyCode"));
		} else {
			currency.setCode(maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_CURRENCY_CODE).getValue());
		}
		
		institution.setAmountCurrency(currency);
		
		InstitutionCategoryModel institutionCategory = new InstitutionCategoryModel();
		institutionCategory.setCode((String) map.get("institutionCategoryCode"));
		institution.setInstitutionCategory(institutionCategory);
		
		return institution;
	}
	
	private void checkUniqueRecord(String code) throws Exception {
		InstitutionModel model = institutionRepo.findOne(code);

		if (model != null && ApplicationConstants.NO.equals(model.getDeleteFlag())) {
			throw new BusinessException("GPT-0100004");
		}
	}	

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				InstitutionModel institution = setMapToModel(map);
				InstitutionModel institutionExisting = institutionRepo
						.findOne(institution.getCode());

				// cek jika record replacement
				if (institutionExisting != null
						&& ApplicationConstants.YES.equals(institutionExisting.getDeleteFlag())) {
					
					setForUpdate(institutionExisting, institution);

					saveInstitution(institutionExisting, vo.getCreatedBy());
				} else {
					saveInstitution(institution, vo.getCreatedBy());
				}
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				InstitutionModel institutionNew = setMapToModel(map);

				InstitutionModel institutionExisting = getExistingRecord(institutionNew.getCode(), true);

				setForUpdate(institutionExisting, institutionNew);
				
				updateInstitution(institutionExisting, vo.getCreatedBy());
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				// check existing record exist or not
				InstitutionModel institution = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);

				deleteInstitution(institution, vo.getCreatedBy());
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}
	
	private void setForUpdate(InstitutionModel institutionExisting, InstitutionModel institutionNew){
		institutionExisting.setName(institutionNew.getName());
		institutionExisting.setDscp(institutionNew.getDscp());
		institutionExisting.setAmountSelection(institutionNew.getAmountSelection());
		institutionExisting.setBillOnlineFlag(institutionNew.getBillOnlineFlag());
		institutionExisting.setCharges(institutionNew.getCharges());
		institutionExisting.setInstitutionType(institutionNew.getInstitutionType());
		institutionExisting.setName1(institutionNew.getName1());
		institutionExisting.setName2(institutionNew.getName2());
		institutionExisting.setName3(institutionNew.getName3());
		institutionExisting.setName4(institutionNew.getName4());
		institutionExisting.setName5(institutionNew.getName5());
		institutionExisting.setNameEng1(institutionNew.getNameEng1());
		institutionExisting.setNameEng2(institutionNew.getNameEng2());
		institutionExisting.setNameEng3(institutionNew.getNameEng3());
		institutionExisting.setNameEng4(institutionNew.getNameEng4());
		institutionExisting.setNameEng5(institutionNew.getNameEng5());
		institutionExisting.setRegex1(institutionNew.getRegex1());
		institutionExisting.setRegex2(institutionNew.getRegex2());
		institutionExisting.setRegex3(institutionNew.getRegex3());
		institutionExisting.setRegex4(institutionNew.getRegex4());
		institutionExisting.setRegex5(institutionNew.getRegex5());
		institutionExisting.setAmountCurrency(institutionNew.getAmountCurrency());
		institutionExisting.setInstitutionCategory(institutionNew.getInstitutionCategory());
	}

	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {

		// if any spesific business function for reject, applied here

		return vo;
	}

	private void saveInstitution(InstitutionModel institution, String createdBy)
			throws ApplicationException, BusinessException {
		// set default value
		boolean isNew = institution.getDeleteFlag() == null;
		institution.setDeleteFlag(ApplicationConstants.NO);
		institution.setCreatedDate(DateUtils.getCurrentTimestamp());
		institution.setCreatedBy(createdBy);
		institution.setUpdatedDate(null);
		institution.setUpdatedBy(null);

		if(isNew)
			institutionRepo.persist(institution);
		else
			institutionRepo.save(institution);
	}

	private void updateInstitution(InstitutionModel institution, String updatedBy)
			throws ApplicationException, BusinessException {
		institution.setUpdatedDate(DateUtils.getCurrentTimestamp());
		institution.setUpdatedBy(updatedBy);
		institutionRepo.save(institution);
	}

	private void deleteInstitution(InstitutionModel institution, String deletedBy)
			throws ApplicationException, BusinessException {
		institution.setDeleteFlag(ApplicationConstants.YES);
		institution.setUpdatedDate(DateUtils.getCurrentTimestamp());
		institution.setUpdatedBy(deletedBy);

		institutionRepo.save(institution);
	}
	
	@Override
	public InstitutionModel isInstitutionValid(String code) throws Exception {
		InstitutionModel model = institutionRepo.findOne(code);

		if (model != null) {
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				throw new BusinessException("GPT-0100120");
			}
		} else {
			throw new BusinessException("GPT-0100120");
		}
		
		return model;
	}

}
