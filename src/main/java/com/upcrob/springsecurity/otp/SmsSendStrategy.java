package com.upcrob.springsecurity.otp;

import java.util.List;

/**
 * Strategy for sending OTP tokens via SMS.  Tokens are sent to mobile providers (via
 * email) which subsequently send them to the user's phone via SMS text message.
 */
public class SmsSendStrategy implements SendStrategy {
	
	private EmailSendStrategy emailSender;
	private List<String> carrierDomains;
	
	public SmsSendStrategy(EmailSendStrategy emailSender, List<String> carrierDomains) {
		this.emailSender = emailSender;
		this.carrierDomains = carrierDomains;
	}

	@Override
	public void send(String token, String phoneNumber) {
		for (String domain : carrierDomains) {
			emailSender.send(token, phoneNumber + "@" + domain);
		}
	}
}
