package com.gpt.product.gpcash.corporate.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.gpt.component.idm.web.security.BaseWebSecurityConfiguration;

@Configuration
@Order(99)
public class CorpWebSecurityConfiguration extends BaseWebSecurityConfiguration {

	private static final String baseURL = "/corp";
	private static final String loginServiceURL = baseURL + "/login";
	private static final String headerURL = baseURL + "/getHeaderName";
	
	@Override
	protected String getBaseURL() {
		return baseURL;
	}
	
	@Override
	protected String getIndexViewURL() {
		return CorpWebViewSecurityConfiguration.indexViewURL;
	}

	@Override
	protected String[] getPermitAuthenticatedURLs() {
		return new String[] {
				baseURL + "/getProfiles",
				baseURL + "/registerFP",
				baseURL + "/logout" 
		};
	}
	
	@Override
	protected String[] getPermitAllURLs() {
		return new String[] {loginServiceURL, headerURL, 
				baseURL + "/loginOSAdmin",
				baseURL + "/authenticate",
				baseURL + "/forgotPassword", 
				baseURL + "/forceChangePassword",
				baseURL + "/mobileCorpLogin",
				baseURL + "/mobileCorpFPLogin",
				baseURL + "/changeLanguage",
				baseURL + "/heartBeat",
				baseURL + "/sipdapi/gettoken",
				baseURL + "/sipdapi/postdata",
				baseURL + "/sipdapi/inquiry",
				baseURL + "/sipdapi/checkstatus"};
	}
	
	@Override
	protected boolean isCsrfCheckingEnabled() {
		return true;
	}
	
}
