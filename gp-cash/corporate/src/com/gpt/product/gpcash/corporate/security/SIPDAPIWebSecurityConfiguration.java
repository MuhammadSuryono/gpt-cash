package com.gpt.product.gpcash.corporate.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import com.gpt.component.base.controller.SIPDAPIBaseController;
import com.gpt.component.idm.web.security.IDMSecurityHandler;


@Configuration
@Order(101)
public class SIPDAPIWebSecurityConfiguration extends WebSecurityConfigurerAdapter {

	private static final String baseURL = SIPDAPIBaseController.baseSIPDUrl;
	
	protected String getBaseURL() {
		return baseURL;
	}
		
	protected String[] getPermitAllURLs() {
		return new String[] {
				baseURL + "/gettoken",
				baseURL + "/postdata",
				baseURL + "/inquiry",
				baseURL + "/checkstatus"};
	}

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
		http.csrf().disable()
			.addFilterAfter(new JWTAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
			.authorizeRequests()
			.antMatchers(HttpMethod.POST, "/sipd/gettoken").permitAll()
			.anyRequest().authenticated();
	}
	
	/*@Override
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
	    		
			http.csrf().disable();
	    		
			http
				.headers().contentTypeOptions().disable().and() // cheat for selenium, comment this for production
				.authorizeRequests()
			    .antMatchers(getPermitAllURLs()).permitAll() // free for all			   
	    		    .antMatchers(getBaseURL() + "/{menuCode}/**").access("@idm.hasAuthority(request, authentication, #menuCode)") // menuCode based access right
	    		    .and()
			    .exceptionHandling().authenticationEntryPoint((req, resp, auth) -> {
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
				.antMatchers(getPermitAllURLs()).permitAll()
				.antMatchers(getBaseURL() + "/{menuCode}/**").access("@idm.hasAuthority(request, authentication, #menuCode)")
				.anyRequest().permitAll();
		}
    }*/
}
