package com.gpt.product.gpcash.retail.registration.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface RegistrationSC {
	Map<String, Object> customerRegistration(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> customerVerification(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> customerVerification2(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> validateRegistrationUserId(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> forgotUserId(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> forgotPassword(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> customerVerificationForExistingUser(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> validateRegistrationExistingUserId(Map<String, Object> map)
			throws ApplicationException, BusinessException;
}
