package com.upcrob.springsecurity.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

/**
 * Tokenstore that uses Redis to store, track, and expire tokens.
 */
public class RedisTokenstore implements Tokenstore {
	private Logger logger = LoggerFactory.getLogger(RedisTokenstore.class);
	private Jedis jedis;
	private boolean authenticated;
	private String password;
	private int maxLifetime;
	
	/**
	 * Create a RedisTokenstore.
	 * @param host Redis host.
	 * @param maxLifetime Max lifetime (in seconds) before the server expires a token.
	 */
	public RedisTokenstore(String host, int maxLifetime) {
		this(host, maxLifetime, -1, null);
	}
	
	/**
	 * Create a RedisTokenstore.
	 * @param host Redis host.
	 * @param maxLifetime Max lifetime (in seconds) before the server expires a token.
	 * @param port Redis server port (set to -1 if using the default port).
	 * @param password Redis server password (null if not using authentication).
	 */
	public RedisTokenstore(String host, int maxLifetime, int port, String password) {
		if (port <= 0) {
			this.jedis = new Jedis(host);
		} else {
			this.jedis = new Jedis(host, port);
		}
		this.maxLifetime = maxLifetime;
			
		// Store Redis password for authentication
		this.password = password;
		authenticate();
	}
	
	@Override
	public void putToken(String username, String token) {
		// Authenticate if we aren't already
		if (!authenticated) {
			if (!authenticate()) {
				throw new OtpAuthenticationException("Error adding token.");
			}
		}
		
		// Setup and execute transaction
		try {
			jedis.setex(username, maxLifetime, token);
			logger.debug("Token, '{}' added to Redis server.", token);
		} catch (JedisConnectionException e) {
			logger.error("Error connecting to Redis.  Exception message was: "
					+ e.getMessage());
			throw new OtpAuthenticationException("Error adding token.", e);
		}
	}

	@Override
	public boolean isTokenValid(String username, String token) {
		// Authenticate if we aren't already
		if (!authenticated) {
			if (!authenticate()) {
				throw new OtpAuthenticationException("Error validating token.");
			}
		}
		
		try {
			String tokenString = jedis.get(username);
			if (tokenString == null) {
				logger.debug("Token, '{}' does not exist in tokenstore.", token);
				return false;
			} else if (!tokenString.equals(token)) {
				logger.debug("Found match for key, but token values did not match.");
				return false;
			} else {
				jedis.del(username);
				logger.debug("Token, '{}' was valid.", token);
				return true;
			}
		} catch (JedisConnectionException e) {
			logger.error("Error connecting to Redis.  Exception message was: " + e.getMessage());
			throw new OtpAuthenticationException("Error validating token.", e);
		}
	}
	
	/**
	 * Authenticate to Redis server, if necessary.
	 */
	private boolean authenticate() {
		// Don't authenticate if there wasn't a password in the configuration
		if (password == null) {
			logger.debug("No password configured.");
			try {
				String resp = jedis.ping();
				if ("PONG".equals(resp)) {
					// 'PONG' response received from server, we can connect without authentication
					authenticated = true;
					return true;
				} else {
					// Unknown response received
					return false;
				}
			} catch (JedisConnectionException e) {
				// Error connecting to the server
				logger.error("Error connecting to Redis.  Exception message was: " + e.getMessage());
				return false;
			} catch (JedisDataException e) {
				// Server requires a password
				if (e.getMessage().contains("NOAUTH"))
					logger.error("Redis server requires a password.");
				else
					logger.error("Redis error: " + e.getMessage());
				return false;
			}
		}
		
		// Try to authenticate to the Redis server
		logger.debug("Attempting to authenticate to Redis server...");
		try {
			String resp = jedis.auth(password);
			if ("OK".equals(resp)) {
				// Authentication succeeded
				logger.debug("Authentication successful.");
				authenticated = true;
				return true;
			}
		} catch (JedisConnectionException e) {
			// Error connecting to server
			logger.error("Error connecting to Redis.  Exception message was: " + e.getMessage());
		} catch (JedisDataException e) {
			// Authentication failed
			if (e.getMessage().contains("NOAUTH")) {
				logger.error("Error authenticating to Redis server.");
			} else {
				logger.error("Redis error: " + e.getMessage());
			}
		}
		return false;
	}
}
