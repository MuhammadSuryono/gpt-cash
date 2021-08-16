package com.gpt.product.gpcash.corporate.pendingtaskadmin.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminPendingTaskVO;

public interface CorporateAdminWorkflowService {
	Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException;

	CorporateAdminPendingTaskVO approve(CorporateAdminPendingTaskVO vo) throws ApplicationException, BusinessException;
	
	CorporateAdminPendingTaskVO reject(CorporateAdminPendingTaskVO vo) throws ApplicationException, BusinessException;
}
