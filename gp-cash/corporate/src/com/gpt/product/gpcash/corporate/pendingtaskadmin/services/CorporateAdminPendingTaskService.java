package com.gpt.product.gpcash.corporate.pendingtaskadmin.services;

import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminPendingTaskVO;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminWFContext;

@AutoDiscoveryImpl
public interface CorporateAdminPendingTaskService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchPendingTaskByUser(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchPendingTaskByReferenceNo(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchPendingTaskHistoryByReferenceNo(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> detailPendingTask(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailPendingTaskOld(Map<String, Object> map) throws ApplicationException, BusinessException;	
	
	Map<String, Object> savePendingTask(CorporateAdminPendingTaskVO vo) throws ApplicationException, BusinessException;
	
	void checkUniquePendingTask(CorporateAdminPendingTaskVO vo) throws ApplicationException, BusinessException;
	
	void checkUniquePendingTaskLike(CorporateAdminPendingTaskVO vo) throws ApplicationException, BusinessException;
	
	List<String> getUserForWorkflow(CorporateAdminWFContext profileContext) throws ApplicationException, BusinessException;

	Map<String, Object> approve(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> reject(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	/**
	 * Invoked by workflow engine
	 * @param pendingTaskId
	 * @param userId
	 * @throws ApplicationException
	 * @throws BusinessException
	 */
	void approve(String pendingTaskId, String userId) throws ApplicationException, BusinessException;
	
	/**
	 * Invoked by workflow engine
	 * @param pendingTaskId
	 * @param userId
	 * @throws ApplicationException
	 * @throws BusinessException
	 */
	void reject(String pendingTaskId, String userId) throws ApplicationException, BusinessException;
	
	Map<String, Object> countPendingTaskByUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchPendingTaskHistoryByPendingTaskId(String pendingTaskId)
			throws ApplicationException, BusinessException;

	Map<String, Object> detailPendingTask(String pendingTaskId) throws ApplicationException, BusinessException;
}
