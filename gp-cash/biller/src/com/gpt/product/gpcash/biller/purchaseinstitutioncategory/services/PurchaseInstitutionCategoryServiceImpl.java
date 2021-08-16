package com.gpt.product.gpcash.biller.purchaseinstitutioncategory.services;

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
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.biller.purchaseinstitutioncategory.model.PurchaseInstitutionCategoryModel;
import com.gpt.product.gpcash.biller.purchaseinstitutioncategory.repository.PurchaseInstitutionCategoryRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class PurchaseInstitutionCategoryServiceImpl implements PurchaseInstitutionCategoryService {

	@Autowired
	private PurchaseInstitutionCategoryRepository purchaseInstitutionCategoryRepo;
	
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<PurchaseInstitutionCategoryModel> result = purchaseInstitutionCategoryRepo.search(map, PagingUtils.createPageRequest(map));

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

	private List<Map<String, Object>> setModelToMap(List<PurchaseInstitutionCategoryModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (PurchaseInstitutionCategoryModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(PurchaseInstitutionCategoryModel model, boolean isGetDetail) {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		
		if(Locale.ENGLISH.equals(LocaleContextHolder.getLocale())) {
			map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getNameEng()));
		} else {
			map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		}
		
		if(isGetDetail){
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
		}
		
		return map;
	}

}
