package com.gpt.product.gpcash.chargepackage.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface ChargePackageService  extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchServiceChargeTRX(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchChargePackageDetailByCode(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchByCode(String code) throws ApplicationException;

	Map<String, Object> searchSpesific(String code, String applicationCode) throws ApplicationException;

}
