package com.gpt.product.gpcash.chargeupdate.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface ChargeUpdateSC  extends WorkflowService {
	
	String menuCode = "MNU_GPCASH_PRO_CH_UPDATE";

	Map<String, Object> searchAll(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchSpesific(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchAllChargePackage(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchCurrencyForDroplist(Map<String, Object> map)
			throws ApplicationException, BusinessException;

}
