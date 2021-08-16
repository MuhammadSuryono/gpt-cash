package com.gpt.component.logging.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.logging.valueobject.ActivityLogVO;

@AutoDiscoveryImpl
public interface ActivityLogService {
	void saveActivityLog(ActivityLogVO vo) throws Exception;
	
	Map<String, Object> getMenuForActivityLog(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> getActivityByUser(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadReport(Map<String, Object> map) throws ApplicationException, BusinessException;
	
}
