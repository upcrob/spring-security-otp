package com.upcrob.springsecurity.otp;

import org.springframework.security.core.AuthenticationException;

public class OtpAuthenticationException extends AuthenticationException {

	public OtpAuthenticationException(String msg) {
		super(msg);
	}

	public OtpAuthenticationException(String msg, Throwable e) {
		super(msg, e);
	}
}
