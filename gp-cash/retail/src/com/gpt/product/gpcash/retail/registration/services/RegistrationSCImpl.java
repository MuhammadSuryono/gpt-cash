package com.gpt.product.gpcash.retail.registration.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.retail.customer.services.CustomerService;

@Validate
@Service
public class RegistrationSCImpl implements RegistrationSC {
	
	@Autowired
	private CustomerService customerService;
	
	
	@Validate
	@Input({
		@Variable(name = "cardNo"),
		@Variable(name = "accountNo"),
		@Variable(name = "mobileNo")
	})
	@Override
	public Map<String, Object> customerVerification(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerService.customerVerification(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "cardNo"),
		@Variable(name = "accountNo"),
		@Variable(name = "mobileNo")
	})
	@Override
	public Map<String, Object> customerVerificationForExistingUser(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerService.customerVerificationForExistingUser(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "cardNo"),
		@Variable(name = "accountNo"),
		@Variable(name = "mobileNo"),
		@Variable(name = "registrationCode")
	})
	@Override
	public Map<String, Object> customerVerification2(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerService.customerVerification2(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "cardNo"),
		@Variable(name = "accountNo"),
		@Variable(name = "mobileNo"),
		@Variable(name = "registrationCode"),
		@Variable(name = "userId")
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> customerRegistration(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerService.customerRegistration(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "cardNo"),
		@Variable(name = "accountNo"),
		@Variable(name = "mobileNo"),
		@Variable(name = "registrationCode"),
		@Variable(name = "userId")
	})
	@Override
	public Map<String, Object> validateRegistrationUserId(Map<String, Object> map) throws ApplicationException, BusinessException {
		customerService.validateRegistrationUserId(map);
		return map;
	}
	
	@Validate
	@Input({
		@Variable(name = "cardNo"),
		@Variable(name = "accountNo"),
		@Variable(name = "mobileNo"),
		@Variable(name = "registrationCode"),
		@Variable(name = "userId")
	})
	@Override
	public Map<String, Object> validateRegistrationExistingUserId(Map<String, Object> map) throws ApplicationException, BusinessException {
		customerService.validateRegistrationExistingUserId(map);
		return map;
	}
	
	@Validate
	@Input({
		@Variable(name = "cardNo"),
		@Variable(name = "accountNo"),
		@Variable(name = "mobileNo"),
		@Variable(name = "registrationCode"),
		@Variable(name = "userId")
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> forgotUserId(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerService.forgotUserId(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "cardNo"),
		@Variable(name = "accountNo"),
		@Variable(name = "mobileNo"),
		@Variable(name = "registrationCode"),
		@Variable(name = "userId")
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> forgotPassword(Map<String, Object> map) throws ApplicationException, BusinessException {
		return customerService.forgotPassword(map);
	}
}
