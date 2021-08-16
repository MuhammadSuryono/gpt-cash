package com.gpt.product.gpcash.corporate.security;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import com.gpt.component.idm.web.security.BaseWebSecurityConfiguration;

@Configuration
@Order(98)
public class CorpWebViewSecurityConfiguration extends BaseWebSecurityConfiguration {

	protected static final String baseURL = "/corporate";
	protected static final String indexViewURL =  baseURL + "/index.html";
	
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
		return null;
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers(getPermitAllURLs());
	}
	
	@Override
	protected String[] getPermitAllURLs() {
		return new String[] {indexViewURL, baseURL + "/assets/**"};
	}
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
		HttpSessionRequestCache cache = new HttpSessionRequestCache();
		cache.setCreateSessionAllowed(false);
		
		http.antMatcher(baseURL + "/**")
		    .csrf().disable() // disable cross site request forgery checking (not needed for view)
		    .requestCache().requestCache(cache).and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.NEVER);
		
		if(enableSecurity()) {
			http	.authorizeRequests()
//			    .antMatchers(getPermitAllURLs()).permitAll() // free for all
			    .anyRequest().authenticated() // can only be called for login user
			    .and()
			    .exceptionHandling().authenticationEntryPoint((req, resp, auth) -> {
		    			// only redirect if this is a GET request
			    		if(req.getMethod().equals(HttpMethod.GET.name()))
			    			resp.sendRedirect(getIndexViewURL());
			    		else
			    			// do we need to even response ???
			    			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			    });
		} else {
	        http	.authorizeRequests()
		    		.anyRequest().permitAll();
		}
		
	}
	
	
}
