package com.gpt.product.gpcash.corporate.pendingtaskuser.services;

import java.sql.Timestamp;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserWFContext;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;

@AutoDiscoveryImpl
public interface CorporateUserPendingTaskService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchPendingTaskByUser(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchPendingTaskByReferenceNo(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchPendingTaskHistoryByReferenceNo(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> detailPendingTask(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailPendingTaskOld(Map<String, Object> map) throws ApplicationException, BusinessException;	
	
	Map<String, Object> savePendingTask(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException;
	
	void checkUniquePendingTask(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException;
	
	void checkUniquePendingTaskLike(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException;
	
	List<Map<String,String>> getUserForWorkflow(CorporateUserWFContext profileContext) throws ApplicationException, BusinessException;

	Map<String, Object> approve(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> reject(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	/**
	 * Invoked by workflow engine
	 * @param pendingTaskId
	 * @param userId
	 * @throws ApplicationException
	 * @throws BusinessException
	 */
	CorporateUserPendingTaskVO approve(String pendingTaskId, String userId) throws ApplicationException, BusinessException;
	
	/**
	 * Invoked by workflow engine
	 * @param pendingTaskId
	 * @param userId
	 * @throws ApplicationException
	 * @throws BusinessException
	 */
	void reject(String pendingTaskId, String userId) throws ApplicationException, BusinessException;

	void expiredPendingTask(String parameter) throws ApplicationException, BusinessException;

	void updatePendingTask(String pendingTaskId, Timestamp activityDate, String activityBy, TransactionStatus trxStatus);
	
	List<String> getCorporateUserEmailWithNotifyTrx(String pendingTaskId) throws ApplicationException, BusinessException;
	
	Map<String, Object> countPendingTaskByUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	boolean isSkipToReleaser(String approverUserCode, BigDecimal transactionAmount);

	Map<String, Object> searchPendingTaskHistoryByPendingTaskId(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	void validateFinalPayment(String senderRefNo, String menuCode, String corporateId) throws BusinessException;

	Map<String, Object> searchPendingTaskHistoryByPendingTaskId(String pendingTaskId)
			throws ApplicationException, BusinessException;
	
	boolean isPopupCOT(Map<String, Object> map)throws ApplicationException, BusinessException;
	
}
