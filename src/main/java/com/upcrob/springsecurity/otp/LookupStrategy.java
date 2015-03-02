package com.upcrob.springsecurity.otp;

/**
 * Strategy for looking up user contact information (email, phone number, etc) by username.
 */
public interface LookupStrategy {
	
	/**
	 * Lookup user contact information.  If user was not found but no error occurred then
	 * return null.
	 * @param username Username to lookup.
	 * @return Contact information.
	 */
	public String lookup(String username);
}
