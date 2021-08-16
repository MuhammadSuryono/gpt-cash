package com.gpt.component.idm.web.security;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;

import com.gpt.platform.cash.constants.ApplicationConstants;

@Component("idm")
public class IDMSecurityHandler {

	@Autowired
	protected CsrfTokenRepository csrfTokenRepository;

	private SessionAuthenticationStrategy sessionAuthenticationStrategy;;

	public void setSessionAuthenticationStrategy(SessionAuthenticationStrategy sessionAuthenticationStrategy) {
		this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
	}

	public HttpSession handleSessionAndAuthorizations(HttpServletRequest request, HttpServletResponse response, String principal, List<String> menuList, Map<String, Object> resultMap) {
		IDMAuthentication authentication = new IDMAuthentication(principal);
		if(sessionAuthenticationStrategy != null)
			sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
		
		if(menuList != null) {
			Set<GrantedAuthority> authorities = new HashSet<>(menuList.size(), 1);
			for(String menu : menuList) {
				authorities.add(new SimpleGrantedAuthority(menu));
			}
			authentication.setAuthorities(authorities);
		}
		
		SecurityContext securityContext = SecurityContextHolder.getContext();
		securityContext.setAuthentication(authentication);
	
		HttpSession session = request.getSession();
//		if(!session.isNew())
//			throw new RuntimeException("Existing http session found!\nDid you forget to invalidate the session first?\nYou should never see this error if you code properly!");

		session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

		// csrf handling
		CsrfToken csrfToken = (CsrfToken)request.getAttribute("_csrf");
		if(csrfToken != null) {
			resultMap.put("_csrf", csrfToken.getToken());
			csrfTokenRepository.saveToken(csrfToken, request, response);
		}
		
		return session;
	}
	
	public boolean hasAuthority(HttpServletRequest request, Authentication auth, String menuCode) {
		
		if(auth.getAuthorities().contains(new SimpleGrantedAuthority(menuCode))) {
			request.setAttribute(ApplicationConstants.STR_MENUCODE, menuCode);
			return true;
		}

		if(BaseWebSecurityConfiguration.enableSecurity())
			return false;
		else {
			// always authrorize since we disable security
			request.setAttribute(ApplicationConstants.STR_MENUCODE, menuCode);
			return true;
		}
	}
	
}
