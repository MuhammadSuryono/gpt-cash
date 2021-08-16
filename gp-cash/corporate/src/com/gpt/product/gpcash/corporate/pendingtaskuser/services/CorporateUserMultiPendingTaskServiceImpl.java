package com.gpt.product.gpcash.corporate.pendingtaskuser.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Service
public class CorporateUserMultiPendingTaskServiceImpl implements CorporateUserMultiPendingTaskService{
	private static final Logger logger = LoggerFactory.getLogger(CorporateUserMultiPendingTaskServiceImpl.class);
	
	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> approveList(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		List<Map<String, Object>> resultList = new ArrayList<>();

		List<Map<String, Object>> pendingTaskList = (ArrayList<Map<String,Object>>)map.get("pendingTaskList");
		
		for(Map<String, Object> pendingTaskMap: pendingTaskList){
			String taskId = (String) pendingTaskMap.get("taskId");
			String referenceNo = (String) pendingTaskMap.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
			
			Map<String, Object> inputMap = new HashMap<>();
			inputMap.put(ApplicationConstants.LOGIN_USERCODE, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
			inputMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, referenceNo);
			inputMap.put("taskId", taskId);
			
			try {
				pendingTaskMap.putAll(pendingTaskService.approve(inputMap));
			} catch (Exception e) {
				pendingTaskMap.put("message", e.getMessage());
				logger.error(e.getMessage(), e);
			}
			
			resultList.add(pendingTaskMap);
		}

		resultMap.put("result", resultList);
		return resultMap;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> rejectList(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		List<Map<String, Object>> resultList = new ArrayList<>();

		List<Map<String, Object>> pendingTaskList = (ArrayList<Map<String,Object>>)map.get("pendingTaskList");
		
		for(Map<String, Object> pendingTaskMap: pendingTaskList){
			String taskId = (String) pendingTaskMap.get("taskId");
			String referenceNo = (String) pendingTaskMap.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
			
			Map<String, Object> inputMap = new HashMap<>();
			inputMap.put(ApplicationConstants.LOGIN_USERCODE, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
			inputMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, referenceNo);
			inputMap.put("taskId", taskId);
			
			try {
				pendingTaskMap.putAll(pendingTaskService.reject(inputMap));
			} catch (Exception e) {
				pendingTaskMap.put("message", e.getMessage());
				logger.error(e.getMessage(), e);
			}
			
			resultList.add(pendingTaskMap);
		}

		resultMap.put("result", resultList);
		return resultMap;
	}
}
