package com.upcrob.springsecurity.otp;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * This wraps another AuthenticationProvider in order to wrap authenticated tokens
 * with a PreOtpAuthenticationToken.  This indicates that the first phase of authentication
 * has been completed, but OTP verification has yet to take place.
 */
public class OtpGeneratingAuthenticationProvider implements AuthenticationProvider {
	
	private AuthenticationProvider provider;
	private Tokenstore tokenstore;
	private LookupStrategy lookupStrategy;
	private SendStrategy sendStrategy;
	private OtpGenerator gen;
	private static final int DEFAULT_OTP_LENGTH = 5;
	
	public OtpGeneratingAuthenticationProvider(AuthenticationProvider provider,
			Tokenstore tokenstore, LookupStrategy lookupStrategy, SendStrategy sendStrategy) {
		if (provider == null) {
			throw new IllegalArgumentException("Embedded authentication provider must not be null.");
		}
		if (tokenstore == null) {
			throw new IllegalArgumentException("Tokenstore must not be null.");
		}
		if (lookupStrategy == null) {
			throw new IllegalArgumentException("LookupStrategy must not be null.");
		}
		if (sendStrategy == null) {
			throw new IllegalArgumentException("SendStrategy must not be null.");
		}
		this.provider = provider;
		this.tokenstore = tokenstore;
		this.lookupStrategy = lookupStrategy;
		this.sendStrategy = sendStrategy;
		this.gen = new DefaultOtpGenerator(DEFAULT_OTP_LENGTH);
	}
	
	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		Authentication auth = provider.authenticate(authentication);
		if (auth.isAuthenticated()) {
			// Generate OTP token
			String contact = lookupStrategy.lookup(auth.getName());
			if (contact != null) {
				String otp = gen.generateToken();
				tokenstore.putToken(auth.getName(), otp);
				sendStrategy.send(otp, contact);
			}
		}
		return new PreOtpAuthenticationToken(auth);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return provider.supports(authentication);
	}
	
	public void setOtpGenerator(OtpGenerator generator) {
		if (generator == null) {
			throw new IllegalArgumentException("OTP generator instance cannot be null.");
		}
		gen = generator;
	}
}
