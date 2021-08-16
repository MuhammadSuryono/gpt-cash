package com.gpt.component.otp;

import org.springframework.stereotype.Component;

import com.gpt.component.common.otp.OTPEngine;

@Component
public class DummyOTPEngine implements OTPEngine {

	@Override
	public String generateOTP(String id, int length) {
		return "123456";
	}

	@Override
	public boolean validateOTP(String id, String otp) {
		if("123456".equals(otp))
			return true;
		return false;
	}

}
