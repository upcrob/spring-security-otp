package com.upcrob.springsecurity.otp;

/**
 * Strategy for looking up user contact information (email, phone number, etc) by username.
 */
public interface LookupStrategy {
	public String lookup(String username);
}
