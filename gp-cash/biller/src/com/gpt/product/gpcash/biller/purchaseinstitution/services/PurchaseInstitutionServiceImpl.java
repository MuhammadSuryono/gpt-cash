package com.gpt.product.gpcash.biller.purchaseinstitution.services;

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
import com.gpt.product.gpcash.biller.purchaseinstitution.model.PurchaseInstitutionModel;
import com.gpt.product.gpcash.biller.purchaseinstitution.repository.PurchaseInstitutionRepository;
import com.gpt.product.gpcash.biller.purchaseinstitutioncategory.model.PurchaseInstitutionCategoryModel;

@Service
@Transactional(rollbackFor = Exception.class)
public class PurchaseInstitutionServiceImpl implements PurchaseInstitutionService {

	@Autowired
	private PurchaseInstitutionRepository purchaseInstitutionRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<PurchaseInstitutionModel> result = purchaseInstitutionRepo.search(map, PagingUtils.createPageRequest(map));

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

	private List<Map<String, Object>> setModelToMap(List<PurchaseInstitutionModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (PurchaseInstitutionModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(PurchaseInstitutionModel model, boolean isGetDetail) {
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
			map.put("purchaseInstitutionType", ValueUtils.getValue(model.getPurchaseInstitutionType()));
			map.put("regex1", ValueUtils.getValue(model.getRegex1()));
			map.put("regex2", ValueUtils.getValue(model.getRegex2()));
			map.put("regex3", ValueUtils.getValue(model.getRegex3()));
			map.put("regex4", ValueUtils.getValue(model.getRegex4()));
			map.put("regex5", ValueUtils.getValue(model.getRegex5()));
			
			CurrencyModel currency = model.getAmountCurrency();
			map.put("amountCurrencyCode", ValueUtils.getValue(currency.getCode()));
			
			PurchaseInstitutionCategoryModel purchaseinstitutionCategory = model.getPurchaseInstitutionCategory();
			map.put("purchaseInstitutionCategoryCode", ValueUtils.getValue(purchaseinstitutionCategory.getCode()));
			map.put("purchaseInstitutionCategoryName", ValueUtils.getValue(purchaseinstitutionCategory.getName()));
			
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
		}
		
		return map;
	}
	
	@Override
	public Map<String, Object> searchForPurchase(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<PurchaseInstitutionModel> result = purchaseInstitutionRepo.search(map, PagingUtils.createPageRequest(map));

			List<Map<String, Object>> resultList = new ArrayList<>();

			//khusus request buat ui
			for (PurchaseInstitutionModel model : result) {
				Map<String, Object> purchaseinstitutionMap = new HashMap<>();
				purchaseinstitutionMap.put(ApplicationConstants.STR_CODE, model.getCode());
				if(Locale.ENGLISH.equals(LocaleContextHolder.getLocale())) {
					purchaseinstitutionMap.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getNameEng()));
				} else {
					purchaseinstitutionMap.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
				}
				purchaseinstitutionMap.put("dscp", ValueUtils.getValue(model.getDscp()));
				
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
					
					if(ValueUtils.hasValue(model.getName())) {
						key = new HashMap<>();
						key.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName4()));
						key.put("type", ValueUtils.getValue(model.getType4()));
						key.put("typeParameter", ValueUtils.getValue(model.getTypeParameter4()));
						key.put("typeParameterDataMap", ValueUtils.getValue(model.getTypeParameterDataMap4()));
						nameList.add(key);
					}
					
					if(ValueUtils.hasValue(model.getName())) {
						key = new HashMap<>();
						key.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName5()));
						key.put("type", ValueUtils.getValue(model.getType5()));
						key.put("typeParameter", ValueUtils.getValue(model.getTypeParameter5()));
						key.put("typeParameterDataMap", ValueUtils.getValue(model.getTypeParameterDataMap5()));
						nameList.add(key);
					}
				}
				
				purchaseinstitutionMap.put("nameList", nameList);
				
				resultList.add(purchaseinstitutionMap);
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
				PurchaseInstitutionModel purchaseinstitutionOld = getExistingRecord(
						(String) map.get(ApplicationConstants.STR_CODE), true);
				vo.setJsonObjectOld(setModelToMap(purchaseinstitutionOld, true));
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

	private PurchaseInstitutionModel getExistingRecord(String code, boolean isThrowError) throws BusinessException {
		PurchaseInstitutionModel model = purchaseInstitutionRepo.findOne(code);

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
		vo.setService("PurchaseInstitutionSC");

		return vo;
	}

	private PurchaseInstitutionModel setMapToModel(Map<String, Object> map) throws Exception {
		PurchaseInstitutionModel purchaseinstitution = new PurchaseInstitutionModel();
		purchaseinstitution.setCode((String) map.get(ApplicationConstants.STR_CODE));
		purchaseinstitution.setName((String) map.get(ApplicationConstants.STR_NAME));
		purchaseinstitution.setDscp((String) map.get("dscp"));
		purchaseinstitution.setAmountSelection((String) map.get("amountSelection"));
		purchaseinstitution.setBillOnlineFlag((String) map.get("billOnlineFlag"));
		purchaseinstitution.setCharges(new BigDecimal (map.get("charges").toString()));
		purchaseinstitution.setPurchaseInstitutionType((String) map.get("purchaseInstitutionType"));
		purchaseinstitution.setName1((String) map.get("name1"));
		purchaseinstitution.setName2((String) map.get("name2"));
		purchaseinstitution.setName3((String) map.get("name3"));
		purchaseinstitution.setName4((String) map.get("name4"));
		purchaseinstitution.setName5((String) map.get("name5"));
		purchaseinstitution.setNameEng1((String) map.get("nameEng1"));
		purchaseinstitution.setNameEng2((String) map.get("nameEng2"));
		purchaseinstitution.setNameEng3((String) map.get("nameEng3"));
		purchaseinstitution.setNameEng4((String) map.get("nameEng4"));
		purchaseinstitution.setNameEng5((String) map.get("nameEng5"));
		purchaseinstitution.setRegex1((String) map.get("regex1"));
		purchaseinstitution.setRegex2((String) map.get("regex2"));
		purchaseinstitution.setRegex3((String) map.get("regex3"));
		purchaseinstitution.setRegex4((String) map.get("regex4"));
		purchaseinstitution.setRegex5((String) map.get("regex5"));
		
		CurrencyModel currency = new CurrencyModel();
		
		if(ValueUtils.hasValue(map.get("amountCurrencyCode"))){
			currency.setCode((String) map.get("amountCurrencyCode"));
		} else {
			currency.setCode(maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_CURRENCY_CODE).getValue());
		}
		
		purchaseinstitution.setAmountCurrency(currency);
		
		PurchaseInstitutionCategoryModel purchaseinstitutionCategory = new PurchaseInstitutionCategoryModel();
		purchaseinstitutionCategory.setCode((String) map.get("purchaseInstitutionCategoryCode"));
		purchaseinstitution.setPurchaseInstitutionCategory(purchaseinstitutionCategory);
		
		return purchaseinstitution;
	}
	
	private void checkUniqueRecord(String code) throws Exception {
		PurchaseInstitutionModel model = purchaseInstitutionRepo.findOne(code);

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
				PurchaseInstitutionModel purchaseinstitution = setMapToModel(map);
				PurchaseInstitutionModel purchaseinstitutionExisting = purchaseInstitutionRepo
						.findOne(purchaseinstitution.getCode());

				// cek jika record replacement
				if (purchaseinstitutionExisting != null
						&& ApplicationConstants.YES.equals(purchaseinstitutionExisting.getDeleteFlag())) {
					
					setForUpdate(purchaseinstitutionExisting, purchaseinstitution);

					savePurchaseInstitution(purchaseinstitutionExisting, vo.getCreatedBy());
				} else {
					savePurchaseInstitution(purchaseinstitution, vo.getCreatedBy());
				}
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				PurchaseInstitutionModel purchaseinstitutionNew = setMapToModel(map);

				PurchaseInstitutionModel purchaseinstitutionExisting = getExistingRecord(purchaseinstitutionNew.getCode(), true);

				setForUpdate(purchaseinstitutionExisting, purchaseinstitutionNew);
				
				updatePurchaseInstitution(purchaseinstitutionExisting, vo.getCreatedBy());
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				// check existing record exist or not
				PurchaseInstitutionModel purchaseinstitution = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);

				deletePurchaseInstitution(purchaseinstitution, vo.getCreatedBy());
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}
	
	private void setForUpdate(PurchaseInstitutionModel purchaseinstitutionExisting, PurchaseInstitutionModel purchaseinstitutionNew){
		purchaseinstitutionExisting.setName(purchaseinstitutionNew.getName());
		purchaseinstitutionExisting.setDscp(purchaseinstitutionNew.getDscp());
		purchaseinstitutionExisting.setAmountSelection(purchaseinstitutionNew.getAmountSelection());
		purchaseinstitutionExisting.setBillOnlineFlag(purchaseinstitutionNew.getBillOnlineFlag());
		purchaseinstitutionExisting.setCharges(purchaseinstitutionNew.getCharges());
		purchaseinstitutionExisting.setPurchaseInstitutionType(purchaseinstitutionNew.getPurchaseInstitutionType());
		purchaseinstitutionExisting.setName1(purchaseinstitutionNew.getName1());
		purchaseinstitutionExisting.setName2(purchaseinstitutionNew.getName2());
		purchaseinstitutionExisting.setName3(purchaseinstitutionNew.getName3());
		purchaseinstitutionExisting.setName4(purchaseinstitutionNew.getName4());
		purchaseinstitutionExisting.setName5(purchaseinstitutionNew.getName5());
		purchaseinstitutionExisting.setNameEng1(purchaseinstitutionNew.getNameEng1());
		purchaseinstitutionExisting.setNameEng2(purchaseinstitutionNew.getNameEng2());
		purchaseinstitutionExisting.setNameEng3(purchaseinstitutionNew.getNameEng3());
		purchaseinstitutionExisting.setNameEng4(purchaseinstitutionNew.getNameEng4());
		purchaseinstitutionExisting.setNameEng5(purchaseinstitutionNew.getNameEng5());
		purchaseinstitutionExisting.setRegex1(purchaseinstitutionNew.getRegex1());
		purchaseinstitutionExisting.setRegex2(purchaseinstitutionNew.getRegex2());
		purchaseinstitutionExisting.setRegex3(purchaseinstitutionNew.getRegex3());
		purchaseinstitutionExisting.setRegex4(purchaseinstitutionNew.getRegex4());
		purchaseinstitutionExisting.setRegex5(purchaseinstitutionNew.getRegex5());
		purchaseinstitutionExisting.setAmountCurrency(purchaseinstitutionNew.getAmountCurrency());
		purchaseinstitutionExisting.setPurchaseInstitutionCategory(purchaseinstitutionNew.getPurchaseInstitutionCategory());
	}

	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {

		// if any spesific business function for reject, applied here

		return vo;
	}

	private void savePurchaseInstitution(PurchaseInstitutionModel purchaseinstitution, String createdBy)
			throws ApplicationException, BusinessException {
		// set default value
		boolean isNew = purchaseinstitution.getDeleteFlag() == null;
		purchaseinstitution.setDeleteFlag(ApplicationConstants.NO);
		purchaseinstitution.setCreatedDate(DateUtils.getCurrentTimestamp());
		purchaseinstitution.setCreatedBy(createdBy);
		purchaseinstitution.setUpdatedDate(null);
		purchaseinstitution.setUpdatedBy(null);

		if(isNew)
			purchaseInstitutionRepo.persist(purchaseinstitution);
		else
			purchaseInstitutionRepo.save(purchaseinstitution);
	}

	private void updatePurchaseInstitution(PurchaseInstitutionModel purchaseinstitution, String updatedBy)
			throws ApplicationException, BusinessException {
		purchaseinstitution.setUpdatedDate(DateUtils.getCurrentTimestamp());
		purchaseinstitution.setUpdatedBy(updatedBy);
		purchaseInstitutionRepo.save(purchaseinstitution);
	}

	private void deletePurchaseInstitution(PurchaseInstitutionModel purchaseinstitution, String deletedBy)
			throws ApplicationException, BusinessException {
		purchaseinstitution.setDeleteFlag(ApplicationConstants.YES);
		purchaseinstitution.setUpdatedDate(DateUtils.getCurrentTimestamp());
		purchaseinstitution.setUpdatedBy(deletedBy);

		purchaseInstitutionRepo.save(purchaseinstitution);
	}
	
	@Override
	public PurchaseInstitutionModel isPurchaseInstitutionValid(String code) throws Exception {
		PurchaseInstitutionModel model = purchaseInstitutionRepo.findOne(code);

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
