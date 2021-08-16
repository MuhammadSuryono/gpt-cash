package com.gpt.component.idm.menutree.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.idm.menutree.model.IDMMenuTreeModel;

@AutoDiscoveryImpl
public interface IDMMenuTreeService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	IDMMenuTreeModel searchByMenuCode(String menuCode, String applicationCode) throws ApplicationException, BusinessException;
}
