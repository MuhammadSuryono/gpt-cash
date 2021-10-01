package com.gpt.product.gpcash.corporate.pendingtaskuser.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;

public interface CorporateUserWorkflowService {
	Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException;
	
	CorporateUserPendingTaskVO reject(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException;
}
