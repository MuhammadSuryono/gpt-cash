package com.gpt.product.gpcash.retail.pendingtaskuser.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.product.gpcash.retail.pendingtaskuser.valueobject.CustomerUserPendingTaskVO;

public interface CustomerUserWorkflowService {
	Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException;

	CustomerUserPendingTaskVO approve(CustomerUserPendingTaskVO vo) throws ApplicationException, BusinessException;
	
	CustomerUserPendingTaskVO reject(CustomerUserPendingTaskVO vo) throws ApplicationException, BusinessException;
}
