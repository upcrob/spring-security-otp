package com.upcrob.springsecurity.otp;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Generates random OTP tokens.
 */
public class OtpGenerator {
	private static final char[] CHARS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
		'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3',
		'4', '5', '6', '7', '8', '9'};
	
	private Random rand;
	private int length;
	
	/**
	 * Creates a new generator.
	 * @param length String length of tokens that will be generated.
	 */
	public OtpGenerator(int length) {
		this.length = length;
		this.rand = new SecureRandom();
	}
	
	
	/**
	 * Generates and returns a random token.
	 */
	public String generateToken() {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(CHARS[rand.nextInt(CHARS.length)]);
		}
		return sb.toString();
	}
}
