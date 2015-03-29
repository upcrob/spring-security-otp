package com.upcrob.springsecurity.otp;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/**
 * This class validates that an OTP token entered by a user is valid.  If it is,
 * the user will be redirected to the success URL.  Otherwise, they will be redirected
 * to the failure URL.
 */
public class OtpValidationFilter extends GenericFilterBean {

	public static final String DEFAULT_OTP_PARAMETER_NAME = "otptoken";
	public String otpParameterName = DEFAULT_OTP_PARAMETER_NAME;
	private Tokenstore tokenstore;
	private String endpoint;
	private String successUrl;
	private String failureUrl;
	
	public OtpValidationFilter(Tokenstore tokenstore, String endpoint, String successUrl, String failureUrl) {
		this.tokenstore = tokenstore;
		this.endpoint = endpoint;
		this.successUrl = successUrl;
		this.failureUrl = failureUrl;
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
			throw new IllegalArgumentException("Request and response must be over HTTP.");
		}
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		
		// Make sure validation endpoint was requested before continuing
		String path = req.getRequestURI().substring(req.getContextPath().length());
		if (!path.equals(endpoint)) {
			chain.doFilter(request, response);
			return;
		}
		
		// Get token from request
		String token = request.getParameter(otpParameterName);
		if (token == null) {
			resp.sendRedirect(failureUrl);
			return;
		}
		
		// Get username from security context
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			resp.sendRedirect(failureUrl);
			return;
		}
		if (!(auth instanceof PreOtpAuthenticationToken)) {
			resp.sendRedirect(failureUrl);
			return;
		}
		PreOtpAuthenticationToken authToken = (PreOtpAuthenticationToken) auth;
		String username = authToken.getName();
		
		// Validate token
		if (tokenstore.isTokenValid(username, token)) {
			SecurityContextHolder.getContext().setAuthentication(authToken.getEmbeddedToken());
			resp.sendRedirect(successUrl);
		} else {
			SecurityContextHolder.getContext().setAuthentication(null);
			resp.sendRedirect(failureUrl);
		}
	}
}
