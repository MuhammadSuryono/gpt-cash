package com.gpt.component.idm.menutree.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface IDMMenuTreeSC {
	
	String menuCode = "MNU_GPCASH_IDM_MENU_TREE";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
}
