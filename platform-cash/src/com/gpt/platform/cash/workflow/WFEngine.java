package com.gpt.platform.cash.workflow;

import java.sql.Timestamp;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;

public interface WFEngine {

	enum Type {
		BackOfficeUser
	}
	
	Map<String, Object> createInstance(Type type, String createdBy, Timestamp createdDate, Map<String, Object> processVars) throws BusinessException, ApplicationException;

    void expireInstance(String processInstanceId) throws BusinessException, ApplicationException;
    
    Map<String, Object> endUserTask(String taskInstanceId, String userId, String processInstanceId, Status status, Map<String, Object> taskVars) throws BusinessException, ApplicationException;

	Map<String, Object> findTasksByUser(Map<String, Object> map, Type type) throws BusinessException, ApplicationException;
    
	Map<String, Object> findTasksByProcessInstanceId(String processInstanceId) throws BusinessException, ApplicationException; 

	int countActiveTasksByUser(String user) throws BusinessException, ApplicationException;

	void cancelInstance(String processInstanceId) throws BusinessException, ApplicationException;

	Map<String, Object> findTasksHistory(String processInstanceId) throws BusinessException, ApplicationException;

}
