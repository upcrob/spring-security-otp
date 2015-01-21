package com.upcrob.springsecurity.otp;

/**
 * Implementations of this interface store tokens and invalidate them after
 * a configured period of time.
 */
public interface Tokenstore {
	public void putToken(String username, String token);
	public boolean isTokenValid(String username, String token);
}
