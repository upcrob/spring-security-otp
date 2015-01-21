package com.upcrob.springsecurity.otp;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Strategy for sending tokens via SMTP.
 */
public class EmailSendStrategy implements SendStrategy {

	private String server;
	private String port;
	private String username;
	private String password;
	private String fromAddr;
	private boolean useTls;
	private String subject;
	private String messageText;
	
	public EmailSendStrategy(String server, String fromAddr) {
		this(server, 25, fromAddr);
	}
	
	public EmailSendStrategy(String server, int port, String fromAddr) {
		if (server == null) {
			throw new IllegalArgumentException("Mail server address cannot be null.");
		}
		if (port < 0) {
			throw new IllegalArgumentException("Invalid mail server port.");
		}
		if (fromAddr == null) {
			throw new IllegalArgumentException("Email 'from' address cannot be null.");
		}
		
		this.server = server;
		this.port = String.valueOf(port);
		this.username = null;
		this.password = null;
		this.useTls = true;
		this.fromAddr = fromAddr;
		this.subject = "Authentication Token";
		this.messageText = "Your temporary authentication token is: ";
	}
	
	@Override
	public void send(String token, String destination) {
		Address address;
		try {
			address = new InternetAddress(destination);
		} catch (AddressException e) {
			throw new SendException("Email address was invalid.", e);
		}
		
		Properties props = new Properties();
		props.put("mail.smtp.host",  server);
		props.put("mail.smtp.port", port);
		
		if (useTls) {
			props.put("mail.smtp.starttls.enable", "true");
		}
		
		Session sess;
		if (username != null) {
			// Use username / password authentication
			props.put("mail.smtp.auth", "true");
			sess = Session.getInstance(props,
					new Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(username, password);
						}
					});
		} else {
			// Don't use authentication
			sess = Session.getDefaultInstance(props);
		}
		
		Message msg = new MimeMessage(sess);
		try {
			msg.setFrom(new InternetAddress(fromAddr));
			msg.setRecipient(Message.RecipientType.TO, address);
			msg.setSubject(subject);
			msg.setText(messageText + token);
			Transport.send(msg);
		} catch (MessagingException e) {
			throw new SendException("Failed to send message.", e);
		}
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}

	public String getServer() {
		return server;
	}

	public String getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isUseTls() {
		return useTls;
	}

	public void setUseTls(boolean useTls) {
		this.useTls = useTls;
	}
}
