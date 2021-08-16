package com.gpt.component.idm.userapp.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.component.idm.user.model.IDMUserModel;

@AutoDiscoveryImpl
public interface IDMUserAppService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	void saveUserApplication(IDMUserModel idmUser, String applicationCode, String createdBy);

}
