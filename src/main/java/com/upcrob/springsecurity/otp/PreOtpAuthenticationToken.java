package com.upcrob.springsecurity.otp;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Wrapper token that contains an embedded token that has already been authenticated.
 * Presence of this token indicates that users have passed primary authentication (e.g.
 * username/password), but have yet to validate their OTP credentials.  Once OTP credentials
 * have been validated, the embedded token is unwrapped and placed in the security context.
 */
public class PreOtpAuthenticationToken implements Authentication {
	
	private Authentication embeddedToken;
	
	public PreOtpAuthenticationToken(Authentication auth) {
		embeddedToken = auth;
	}
	
	public Authentication getEmbeddedToken() {
		return embeddedToken;
	}

	@Override
	public String getName() {
		return embeddedToken.getName();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		return authorities;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getDetails() {
		return embeddedToken.getDetails();
	}

	@Override
	public Object getPrincipal() {
		return embeddedToken.getPrincipal();
	}

	@Override
	public boolean isAuthenticated() {
		return embeddedToken.isAuthenticated();
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated)
			throws IllegalArgumentException {
		embeddedToken.setAuthenticated(isAuthenticated);
	}
}
