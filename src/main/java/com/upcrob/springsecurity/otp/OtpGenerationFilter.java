package com.upcrob.springsecurity.otp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Central filter for picking up requests to generate OTP tokens.  Requests to this endpoint
 * must include a "username" parameter.  This tells the plugin to whom the newly generated
 * OTP token should be sent.  The actual name of this username parameter on the request is
 * configurable.
 * 
 * For most applications, the best user experience can be achieved by making a call to this
 * endpoint via an AJAX request from the login page (by clicking a "Generate Token" button,
 * for example).  JavaScript on the client side may pickup the username from the login form's
 * username box and send it to this endpoint.  When users receive the OTP token, they can
 * complete the form and finish logging in.
 */
public class OtpGenerationFilter extends GenericFilterBean {
	private static final int DEFAULT_OTP_LENGTH = 5;
	private String usernameParameter = OtpAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;
	private Tokenstore tokenStore;
	private SendStrategy sendStrategy;
	private LookupStrategy lookupStrategy;
	private OtpGenerator gen;
	private String endpoint;
	private boolean postOnly;
	
	public OtpGenerationFilter(Tokenstore tokenStore, LookupStrategy lookupStrategy, SendStrategy sendStrategy, String endpoint) {
		Assert.notNull(tokenStore, "Tokenstore must be non-null.");
		Assert.notNull(lookupStrategy, "Lookup strategy must be non-null.");
		Assert.notNull(sendStrategy, "Send strategy must be non-null.");
		Assert.notNull(endpoint, "OTP generation filter endpoint must be non-null.");
		
		this.tokenStore = tokenStore;
		this.lookupStrategy = lookupStrategy;
		this.sendStrategy = sendStrategy;
		this.gen = new OtpGenerator(DEFAULT_OTP_LENGTH);
		this.endpoint = endpoint;
		this.postOnly = false;
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
			throw new IllegalArgumentException("Request and response must be over HTTP.");
		}
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		
		String path = req.getRequestURI().substring(req.getContextPath().length());
		if (!path.equals(endpoint)) {
			chain.doFilter(request, response);
			return;
		}
		
		if (postOnly && !"POST".equals(req.getMethod())) {
			throw new AuthenticationServiceException("Authentication method not supported: " + req.getMethod());
		}

		String username = req.getParameter(usernameParameter);
		if (username == null) {
			throw new ServletException("No username on request.");
		}
		
		String token = gen.generateToken();
		tokenStore.putToken(username, token);
		String contact = lookupStrategy.lookup(username);
		if (contact != null) {
			sendStrategy.send(token, contact);
		}
		
		resp.setStatus(200);
		PrintWriter out = resp.getWriter();
		out.write("");
		out.close();
	}
	
	public void setUsernameParameter(String usernameParm) {
		usernameParameter = usernameParm;
	}
	
	public String getUsernameParameter() {
		return usernameParameter;
	}
	
	public void setOtpGenerator(OtpGenerator generator) {
		Assert.notNull(generator, "OTP Generator must not be null.");
		gen = generator;
	}
	
	public void setPostOnly(boolean postOnly) {
		this.postOnly = postOnly;
	}
}
