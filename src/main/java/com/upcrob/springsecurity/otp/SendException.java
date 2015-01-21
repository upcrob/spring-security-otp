package com.upcrob.springsecurity.otp;

/**
 * Describes an Exception that occurs while attempting to send a token.
 */
public class SendException extends RuntimeException {
	public SendException(String msg) {
		super(msg);
	}
	
	public SendException(String msg, Throwable e) {
		super(msg, e);
	}
}
