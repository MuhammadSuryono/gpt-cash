package com.gpt.product.gpcash.menupackage.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface MenuPackageSC extends WorkflowService {
	
	String menuCode = "MNU_GPCASH_PRO_MNU_PC";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchMenuPackageDetailByCode(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchMenu(Map<String, Object> map) throws ApplicationException, BusinessException;

}
