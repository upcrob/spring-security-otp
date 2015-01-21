package com.upcrob.springsecurity.otp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.Assert;

/**
 * Filter builds on the UsernamePasswordAuthenticationFilter to add one-time-password
 * functionality.  In addition to a username and password, authentication requests must
 * include an OTP token entered by the user.  The OTP token itself can be generated and
 * sent by accessing the OtpGenerationFilter.
 */
public class OtpAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	public static final String DEFAULT_OTP_PARAMETER_NAME = "otptoken";
	public String otpParameterName = DEFAULT_OTP_PARAMETER_NAME;
	
	private Tokenstore tokenStore;
	
	public OtpAuthenticationFilter(Tokenstore tokenStore) {
		Assert.notNull(tokenStore, "Tokenstore must not be null.");
		this.tokenStore = tokenStore;
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		// Get token from request
		String token = request.getParameter(otpParameterName);
		String username = request.getParameter(getUsernameParameter());
		if (token == null) {
			throw new OtpAuthenticationException("No OTP token found on request.");
		}
		if (username == null) {
			throw new OtpAuthenticationException("No username found on request.");
		}
		
		// Try to validate OTP token
		if (!tokenStore.isTokenValid(username, token)) {
			throw new OtpAuthenticationException("Invalid OTP token.");
		}
		
		// Do normal username/password authentication
		return super.attemptAuthentication(request, response);
	}
	
	public void setOtpParameter(String name) {
		otpParameterName = name;
	}
	
	public String getOtpParameter() {
		return otpParameterName;
	}
}
