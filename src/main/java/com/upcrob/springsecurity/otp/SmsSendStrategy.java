package com.upcrob.springsecurity.otp;

import java.util.List;

/**
 * Strategy for sending OTP tokens via SMS.  Tokens are sent to mobile providers (via
 * email) which subsequently send them to the user's phone via SMS text message.
 *
 * Note that this implementation will try to send messages tokens to every carrier,
 * regardless of the phone number used.  This may be problematic at scale as carriers
 * may choose to block certain senders if a large number of failed (or even successful)
 * attempts are made to send SMS messages from the same email address.  Using a dedicated
 * service for reliable SMS transmission is recommended.
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
