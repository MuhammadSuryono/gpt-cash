package com.gpt.platform.springboot.csrf;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.util.Assert;

public class HttpSessionCsrfTokenRepository implements CsrfTokenRepository {
	private static final String DEFAULT_CSRF_PARAMETER_NAME = "_csrf";

	private static final String DEFAULT_CSRF_HEADER_NAME = "X-XSRF-TOKEN";

	private String parameterName = DEFAULT_CSRF_PARAMETER_NAME;

	private String headerName = DEFAULT_CSRF_HEADER_NAME;

	private String sessionAttributeName = DEFAULT_CSRF_PARAMETER_NAME;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.security.web.csrf.CsrfTokenRepository#saveToken(org.
	 * springframework .security.web.csrf.CsrfToken,
	 * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 * 
	 * WE DO NOT ALLOW THE TOKEN TO BE SAVED IF SESSION DOES NOT EXIST
	 */
	public void saveToken(CsrfToken token, HttpServletRequest request,
			HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		if (session == null)
			return;
			
		if (token == null) {
			session.removeAttribute(this.sessionAttributeName);
		} else {
			session.setAttribute(this.sessionAttributeName, token);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.security.web.csrf.CsrfTokenRepository#loadToken(javax.servlet
	 * .http.HttpServletRequest)
	 */
	public CsrfToken loadToken(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		}
		return (CsrfToken) session.getAttribute(this.sessionAttributeName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.security.web.csrf.CsrfTokenRepository#generateToken(javax.
	 * servlet .http.HttpServletRequest)
	 */
	public CsrfToken generateToken(HttpServletRequest request) {
		return new DefaultCsrfToken(this.headerName, this.parameterName, UUID.randomUUID().toString());
	}

	/**
	 * Sets the {@link HttpServletRequest} parameter name that the {@link CsrfToken} is
	 * expected to appear on
	 * @param parameterName the new parameter name to use
	 */
	public void setParameterName(String parameterName) {
		Assert.hasLength(parameterName, "parameterName cannot be null or empty");
		this.parameterName = parameterName;
	}

	/**
	 * Sets the header name that the {@link CsrfToken} is expected to appear on and the
	 * header that the response will contain the {@link CsrfToken}.
	 *
	 * @param headerName the new header name to use
	 */
	public void setHeaderName(String headerName) {
		Assert.hasLength(headerName, "headerName cannot be null or empty");
		this.headerName = headerName;
	}

	/**
	 * Sets the {@link HttpSession} attribute name that the {@link CsrfToken} is stored in
	 * @param sessionAttributeName the new attribute name to use
	 */
	public void setSessionAttributeName(String sessionAttributeName) {
		Assert.hasLength(sessionAttributeName,
				"sessionAttributename cannot be null or empty");
		this.sessionAttributeName = sessionAttributeName;
	}
	
}
