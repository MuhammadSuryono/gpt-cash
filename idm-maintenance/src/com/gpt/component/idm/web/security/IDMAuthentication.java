package com.gpt.component.idm.web.security;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

public class IDMAuthentication implements Authentication {
	private Collection<GrantedAuthority> authorities;
	private boolean authenticated = false;
	private final String principal;

	public IDMAuthentication(String principal) {
		this.principal = principal;
		this.authenticated = true;
	}
	public IDMAuthentication(String principal, Collection<GrantedAuthority> authorities) {
		this.principal = principal;
		this.authenticated = true;
		setAuthorities(authorities);
	}
	
	public void setAuthorities(Collection<GrantedAuthority> authorities) {
		if (authorities == null) {
			this.authorities = AuthorityUtils.NO_AUTHORITIES;
			return;
		}

		for (GrantedAuthority a : authorities) {
			if (a == null) {
				throw new IllegalArgumentException(
						"Authorities collection cannot contain any null elements");
			}
		}
		this.authorities = Collections.unmodifiableCollection(authorities);
	}
	
	@Override
	public String getName() {
		if (this.getPrincipal() instanceof UserDetails) {
			return ((UserDetails) this.getPrincipal()).getUsername();
		}

		if (getPrincipal() instanceof Principal) {
			return ((Principal) getPrincipal()).getName();
		}

		return (this.getPrincipal() == null) ? "" : this.getPrincipal().toString();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getDetails() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return principal;
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		throw new UnsupportedOperationException("Can not update authentication status");
	}

}
