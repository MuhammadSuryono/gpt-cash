package com.gpt.component.idm.web.security;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

public abstract class BaseWebSecurityConfiguration extends WebSecurityConfigurerAdapter {

	protected abstract String getBaseURL();
	protected abstract String getIndexViewURL();
	
	protected abstract String[] getPermitAllURLs();
	protected abstract String[] getPermitAuthenticatedURLs();
	
	@Autowired
	protected CsrfTokenRepository csrfTokenRepository;

	@Autowired
	protected SessionRegistry sessionRegistry;
	
	@Autowired
	protected IDMSecurityHandler securityHandler;
	
	public static final boolean enableSecurity() {
		/**
         * Enable or Disable security by switching this value
         */
		return true;
	}
	
	protected boolean isCsrfCheckingEnabled() {
		return true;
	}
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
		HttpSessionRequestCache cache = new HttpSessionRequestCache();
		cache.setCreateSessionAllowed(false);
		
		http.antMatcher(getBaseURL() + "/**").cors().and()
			.requestCache().requestCache(cache).and()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.NEVER)
				.sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
				.maximumSessions(1).sessionRegistry(sessionRegistry)
				.maxSessionsPreventsLogin(false);
		
		final boolean isSecurityEnabled = enableSecurity();
		if(isSecurityEnabled) {
	    		if(isCsrfCheckingEnabled()) {
	        		http	.csrf().ignoringAntMatchers(getPermitAllURLs()).csrfTokenRepository(csrfTokenRepository);
	    		} else {
	    			http.csrf().disable();
	    		}
	    		
			http
				.headers().contentTypeOptions().disable().and() // cheat for selenium, comment this for production
				.authorizeRequests()
			    .antMatchers(getPermitAllURLs()).permitAll() // free for all
			    .antMatchers(getPermitAuthenticatedURLs()).authenticated() // can only be called for login user
	    		    .antMatchers(getBaseURL() + "/{menuCode}/**").access("@idm.hasAuthority(request, authentication, #menuCode)") // menuCode based access right
			    .and()
			    .exceptionHandling().authenticationEntryPoint((req, resp, auth) -> {
		    			// only redirect if this is a GET request
			    		if(req.getMethod().equals(HttpMethod.GET.name()) && getIndexViewURL() != null)
			    			resp.sendRedirect(getIndexViewURL());
			    		else
			    			// do we need to even response ???
			    			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			    });
			
			http.securityContext().addObjectPostProcessor(new ObjectPostProcessor<Object>() {
				@Override
				public <O> O postProcess(O object) {
					securityHandler.setSessionAuthenticationStrategy(http.getSharedObject(SessionAuthenticationStrategy.class));
					return object;
				}
			});
			
		} else {
    			http.cors().and()
    				.csrf().disable()
    				.authorizeRequests()
    				.antMatchers(getPermitAuthenticatedURLs()).permitAll()
				.antMatchers(getPermitAllURLs()).permitAll()
				.antMatchers(getBaseURL() + "/{menuCode}/**").access("@idm.hasAuthority(request, authentication, #menuCode)")
				.anyRequest().permitAll();
		}
    }
	
}
