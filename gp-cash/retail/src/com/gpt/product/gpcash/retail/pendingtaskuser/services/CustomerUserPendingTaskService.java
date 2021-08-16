package com.gpt.product.gpcash.retail.pendingtaskuser.services;

import java.sql.Timestamp;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.retail.pendingtaskuser.valueobject.CustomerUserPendingTaskVO;
import com.gpt.product.gpcash.retail.transactionstatus.CustomerTransactionStatus;

@AutoDiscoveryImpl
public interface CustomerUserPendingTaskService {
	Map<String, Object> detailPendingTask(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailPendingTaskOld(Map<String, Object> map) throws ApplicationException, BusinessException;	
	
	Map<String, Object> savePendingTask(CustomerUserPendingTaskVO vo) throws ApplicationException, BusinessException;
	
	void checkUniquePendingTask(CustomerUserPendingTaskVO vo) throws ApplicationException, BusinessException;
	
	void checkUniquePendingTaskLike(CustomerUserPendingTaskVO vo) throws ApplicationException, BusinessException;
	
	
	/**
	 * Invoked by workflow engine
	 * @param pendingTaskId
	 * @param userId
	 * @throws ApplicationException
	 * @throws BusinessException
	 */
	void reject(String pendingTaskId, String userId) throws ApplicationException, BusinessException;

	void expiredPendingTask(String parameter) throws ApplicationException, BusinessException;

	void updatePendingTask(String pendingTaskId, Timestamp activityDate, String activityBy, CustomerTransactionStatus trxStatus);

	void approve(CustomerUserPendingTaskVO vo, CustomerUserWorkflowService service)
			throws ApplicationException, BusinessException;

	Map<String, Object> searchPendingTask(Map<String, Object> map) throws ApplicationException, BusinessException;
}
