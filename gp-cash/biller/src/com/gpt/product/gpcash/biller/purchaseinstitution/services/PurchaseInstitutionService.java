package com.gpt.product.gpcash.biller.purchaseinstitution.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.product.gpcash.biller.purchaseinstitution.model.PurchaseInstitutionModel;

@AutoDiscoveryImpl
public interface PurchaseInstitutionService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	PurchaseInstitutionModel isPurchaseInstitutionValid(String code) throws Exception;

	Map<String, Object> searchForPurchase(Map<String, Object> map) throws ApplicationException, BusinessException;
}
