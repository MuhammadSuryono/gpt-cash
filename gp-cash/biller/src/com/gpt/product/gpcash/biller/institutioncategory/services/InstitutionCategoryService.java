package com.gpt.product.gpcash.biller.institutioncategory.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface InstitutionCategoryService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
}
