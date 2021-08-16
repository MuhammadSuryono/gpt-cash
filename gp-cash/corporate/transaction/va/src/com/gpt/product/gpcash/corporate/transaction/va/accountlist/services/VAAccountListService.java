package com.gpt.product.gpcash.corporate.transaction.va.accountlist.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface VAAccountListService extends CorporateUserWorkflowService {

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchMainAccount(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchVADetailList(Map<String, Object> map) throws ApplicationException, BusinessException;
}