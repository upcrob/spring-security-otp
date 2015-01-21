package com.upcrob.springsecurity.otp;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of Tokenstore.  Because this implementation is not distributed, it
 * should only be used for local development and NOT for production environments where high
 * availability is desired.
 */
public class LocalTokenstore implements Tokenstore {

	private Map<String, Token> tokens;
	private int maxLifetime;
	private ReaperThread reaper;
	
	public LocalTokenstore(int maxLifetime) {
		if (maxLifetime < 1) {
			throw new IllegalArgumentException("Maximum token lifetime must be greater than 0.");
		}
		
		this.tokens = new ConcurrentHashMap<String, Token>();
		this.maxLifetime = maxLifetime * 1000;
		reaper = new ReaperThread(this);
		reaper.start();
	}
	
	@Override
	public void putToken(String username, String token) {
		Token t = new Token(token, System.currentTimeMillis() + maxLifetime);
		tokens.put(username, t);
	}

	@Override
	public boolean isTokenValid(String username, String token) {
		synchronized (tokens) {
			Token t = tokens.get(username);
			if (t == null) {
				return false;
			} else if (t.expires < System.currentTimeMillis()) {
				tokens.remove(username);
				return false;
			} else {
				if (t.value.equals(token)) {
					tokens.remove(username);
					return true;
				} else {
					return false;
				}
			}
		}
	}

	private void removeExpired() {
		Set<Entry<String, Token>> entries = tokens.entrySet();
		for (Entry<String, Token> entry : entries) {
			if (entry.getValue().expires > System.currentTimeMillis()) {
				tokens.remove(entry.getKey());
			}
		}
	}
	
	private static class Token {
		public final String value;
		public final long expires;
		
		public Token(String value, long expires) {
			this.value = value;
			this.expires = expires;
		}
	}
	
	private static class ReaperThread extends Thread {
		private LocalTokenstore ts;
		
		public ReaperThread(LocalTokenstore tokenstore) {
			setDaemon(true);
			ts = tokenstore;
		}
		
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(ts.maxLifetime);
					ts.removeExpired();
				} catch (InterruptedException e) {
					// Do nothing
				}
			}
		}
	}
}
