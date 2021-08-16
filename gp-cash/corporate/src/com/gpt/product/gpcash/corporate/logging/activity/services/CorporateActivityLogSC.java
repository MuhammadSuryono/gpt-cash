package com.gpt.product.gpcash.corporate.logging.activity.services;

import java.sql.Timestamp;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface CorporateActivityLogSC {
	
	String menuCode = "MNU_GPCASH_F_NON_FIN";
	
	void saveActivityLog(String menuCode, String menuName, String action, String loginId, boolean isError, String errorCode, String errorDescription, String errorTrace, String referenceNo, String corporateId, Timestamp activityDate, String logId);

	Map<String, Object> getActivityByUser(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchByReferenceNo(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailPendingTask(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchUser(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadActivity(Map<String, Object> map) throws ApplicationException, BusinessException;
}
