package com.gpt.component.idm.web.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.gpt.component.base.controller.BankBaseController;

@Configuration
@Order(100)
public class IDMWebSecurityConfiguration extends BaseWebSecurityConfiguration {

	private static final String baseURL = BankBaseController.baseBankUrl;
	private static final String indexViewURL = baseURL + "/index.html";
	private static final String loginServiceURL = baseURL + "/login";
	
	@Override
	protected String getBaseURL() {
		return baseURL;
	}
	
	@Override
	protected String getIndexViewURL() {
		return indexViewURL;
	}

	@Override
	protected String[] getPermitAuthenticatedURLs() {
		return new String[] {baseURL + "/getProfiles",
				baseURL + "/logout" };
	}
	
	@Override
	protected String[] getPermitAllURLs() {
		return new String[] {indexViewURL, loginServiceURL, 
				baseURL + "/forceChangePassword", 
				baseURL + "/changeLanguage",
				baseURL + "/sipd",
				baseURL + "/heartBeat"};
	}
	
	@Override
	protected boolean isCsrfCheckingEnabled() {
		return false;
	}
	
}
