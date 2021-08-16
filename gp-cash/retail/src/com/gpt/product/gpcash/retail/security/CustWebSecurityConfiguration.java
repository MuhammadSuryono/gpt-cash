package com.gpt.product.gpcash.retail.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.gpt.component.idm.web.security.BaseWebSecurityConfiguration;

@Configuration
@Order(96)
public class CustWebSecurityConfiguration extends BaseWebSecurityConfiguration {

	private static final String baseURL = "/retail";
	private static final String loginServiceURL = baseURL + "/login";
	private static final String headerURL = baseURL + "/getHeaderName";
	
	@Override
	protected String getBaseURL() {
		return baseURL;
	}
	
	@Override
	protected String getIndexViewURL() {
		return CustWebViewSecurityConfiguration.indexViewURL;
	}

	@Override
	protected String[] getPermitAuthenticatedURLs() {
		return new String[] {
				baseURL + "/getProfiles",
				baseURL + "/logout" 
		};
	}
	
	@Override
	protected String[] getPermitAllURLs() {
		return new String[] {loginServiceURL, headerURL, 
				baseURL + "/authenticate",
				baseURL + "/forgotPassword", 
				baseURL + "/forceChangePassword",
				baseURL + "/changeLanguage",
				baseURL + "/heartBeat",
				baseURL + "/customerVerification",
				baseURL + "/customerVerification2",
				baseURL + "/customerRegistration",
				baseURL + "/customerVerificationForExistingUser",
				baseURL + "/validateRegistrationUserId",
				baseURL + "/validateRegistrationExistingUserId",
				baseURL + "/mobileCustLogin",
				baseURL + "/mobileCustFPLogin"};
	}
	
	@Override
	protected boolean isCsrfCheckingEnabled() {
		return true;
	}
	
}
