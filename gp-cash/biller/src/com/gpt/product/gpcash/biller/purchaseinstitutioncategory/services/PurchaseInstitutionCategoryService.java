package com.gpt.product.gpcash.biller.purchaseinstitutioncategory.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface PurchaseInstitutionCategoryService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
}
