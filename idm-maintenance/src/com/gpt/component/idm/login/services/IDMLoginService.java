package com.gpt.component.idm.login.services;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface IDMLoginService {
	Map<String, Object> login(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> getProfiles(String userId, String passwd, boolean isJoinWithWF) throws ApplicationException, BusinessException;

	void logout(List<String> loginHistoryList, String userId, Timestamp loginDate) throws ApplicationException, BusinessException;

	/**
	 * @deprecated use {@link #login(String, String, String, String)} instead
	 * 
	 * @param userCode
	 * @param passwd
	 * @param ipAddress
	 * @return
	 * @throws BusinessException
	 * @throws ApplicationException
	 */
	@Deprecated
	Map<String, Object> login(String userCode, String passwd, String ipAddress) throws BusinessException, ApplicationException;

	Map<String, Object> loginOutsource(String userCode, String ipAddress) throws BusinessException, ApplicationException;
	
	Map<String, Object> login(String userCode, String passwd, String heartBeat, String ipAddress) throws BusinessException, ApplicationException;

	String getPlainPasswd(String passwd, String key);

	
	
	/**
	 * Used for mobile
	 */
	
	/**
	 * Used only for authentication purposed, normally used during setup to authenticate that the user does exist and continue for registration
	 * A new OTP will be sent through sms to registered user's mobile phone.
	 * 
	 * @param userCode
	 * @param passwd
	 * @param heartBeat
	 * @param ipAddress
	 * @return
	 * @throws BusinessException
	 * @throws ApplicationException
	 */
	Map<String, Object> authenticate(String userCode, String passwd, String heartBeat, String ipAddress) throws BusinessException, ApplicationException;


}
