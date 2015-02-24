package com.upcrob.springsecurity.otp;

/**
 * Implementations of this interface store tokens and invalidate them after
 * a configured period of time.
 */
public interface Tokenstore {
	/**
	 * Adds a username/token pair to the Tokenstore.
	 */
	public void putToken(String username, String token);

	/**
	 * Determines if a token is valid.  If the token is valid, this method
	 * should return true, and invalidate the underlying token.  That is,
	 * two successive calls to this method with the same username and
	 * password should not return true.
	 */
	public boolean isTokenValid(String username, String token);
}
