package com.gpt.component.pendingtask.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface PendingTaskSC {
	
	String menuCode = "MNU_GPCASH_PENDING_TASK";
	
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchPendingTaskByUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchPendingTaskByReferenceNo(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchPendingTaskHistoryByReferenceNo(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> detailPendingTask(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailPendingTaskOld(Map<String, Object> map) throws ApplicationException, BusinessException;	
	
	Map<String, Object> approve(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> reject(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> approveList(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> rejectList(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> countPendingTaskByUser(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
