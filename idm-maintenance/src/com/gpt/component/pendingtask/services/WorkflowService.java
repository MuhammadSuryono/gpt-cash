package com.gpt.component.pendingtask.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;

public interface WorkflowService {
	Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException;

	PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException;
	
	PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException;
}