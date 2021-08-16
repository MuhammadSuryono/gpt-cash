package com.gpt.component.idm.user.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.component.idm.user.model.IDMUserModel;

@AutoDiscoveryImpl
public interface IDMUserService extends WorkflowService {
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	IDMUserModel saveIDMUser(String id, String userId, String userName, String email, String createdBy)
			throws Exception;

	IDMUserModel updateIDMUser(IDMUserModel idmUser, String userName, String email, String createdBy)
			throws ApplicationException, BusinessException;

	Map<String, Object> searchIDMUserForBackOffice(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	void unlockUser(String userCode, String createdBy) throws ApplicationException, BusinessException;
	
	void lockUser(String userCode, String createdBy) throws ApplicationException, BusinessException;
	
	void resetUser(String userCode, String createdBy) throws ApplicationException, BusinessException;
	
	void inactivateUser(String userCode, String createdBy) throws ApplicationException, BusinessException;
	
	void activateUser(String userCode, String createdBy) throws ApplicationException, BusinessException;

	/**
	 * @deprecated
	 * @param userCode
	 * @param oldPassword
	 * @param newPassword
	 * @param newPassword2
	 * @throws BusinessException
	 * @throws ApplicationException
	 * 
	 * use {@link #changePassword2(String, String, String)} instead
	 */
	@Deprecated
	void changePassword(String userCode, String oldPassword, String newPassword, String newPassword2)
			throws BusinessException, ApplicationException;

	void changePassword2(String userCode, String heartBeat, String newPassword, String newPassword2)
			throws BusinessException, ApplicationException;
	
	void updateLoginCount(String userCode) throws ApplicationException;

	void saveNewUser(IDMUserModel idmUser, String createdBy, boolean isNeedFlush) throws Exception;

	void resetUser(IDMUserModel idmUser, String createdBy) throws ApplicationException, BusinessException;

	Map<String, Object> uploadAvatar(byte[] rawData, String filename) throws ApplicationException, BusinessException;

	void saveAvatar(String userCode, String filename, String fileId) throws ApplicationException, BusinessException;

	void updateUserToInactive(String status, String userCode) throws ApplicationException, BusinessException;

	Map<String, Object> findBankUsers() throws ApplicationException;
}