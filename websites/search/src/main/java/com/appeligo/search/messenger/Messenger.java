/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package com.appeligo.search.messenger;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import com.appeligo.search.entity.User;
import com.appeligo.search.util.ConfigUtils;
import com.knowbout.hibernate.HibernateUtil;
import com.knowbout.hibernate.TransactionManager;

/**
 * Provides very simple message sending capability.  Could be, and probably should be, 
 * optimized to hold on to the mail session object for some period so that it is not constantly 
 * setup and torn down.
 * 
 * This was originally derived from fliptv code for sending SMS messages, but significantly
 * reworked for the search site.
 * 
 * @author fear, exline, kadel, almilli
 */
public class Messenger {

	private static final Logger log = Logger.getLogger(Messenger.class);

	private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000;
	private static long lastDelete = 0;
	
	private String mailHost;
	private int port;
	private String smtpUser;
	private String password;
	private boolean debug;
	private int maxAttempts;
	private int deleteMessagesOlderThanDays;

	public Messenger() {
        Configuration config = ConfigUtils.getMessageConfig();
        mailHost = config.getString("smtpServer", "localhost");
        password = config.getString("smtpSenderPassword", null);
        smtpUser = config.getString("smtpUser", null);
        debug = config.getBoolean("debugMailSender", true);
        port = config.getInt("smtpPort", 25);
        maxAttempts = config.getInt("maxMessageAttempts", 2);
        deleteMessagesOlderThanDays = config.getInt("deleteMessagesOlderThanDays", 7);
	}
		
	public void sendUnsent() {
		int max = ConfigUtils.getMessageConfig().getInt("maxSendBlock", 10);
		sendUnsent(max);
	}
	
	public void sendUnsent(int maxResults) {
		boolean commit = true;
		try {
			HibernateUtil.openSession();
			TransactionManager.beginTransaction();
			if ((lastDelete == 0) ||
					(System.currentTimeMillis() > (lastDelete + ONE_DAY_MILLIS))) {
				com.appeligo.search.entity.Message.deleteOldMessages(deleteMessagesOlderThanDays, maxAttempts);
				lastDelete = System.currentTimeMillis();
			}
			List<com.appeligo.search.entity.Message> messages = 
					com.appeligo.search.entity.Message.getUnsentMessages(maxResults, maxAttempts);
			if (messages != null && messages.size() > 0) {
				int sent = send(messages);
				if (log.isInfoEnabled() && (sent > 0)) {
					log.info("Sent "+sent+" of "+messages.size()+" email messages.");
				}
			}
		} catch (Throwable t) {
			commit = false;
			TransactionManager.rollbackTransaction();
		} finally {
			if (commit) {
				try {
				TransactionManager.commitTransaction();
				} catch (Throwable t) {
					log.error(t.getMessage(), t);
				}
			}
			HibernateUtil.closeSession();
		}
	}
	
	/**
	 * 
	 * @param messages
	 */
	public int send(Collection<com.appeligo.search.entity.Message> messages) {
		if (messages != null) {
			return send(messages.toArray(new com.appeligo.search.entity.Message[0]));
		}
		return 0;
	}
	
	/**
	 * 
	 * @param messages
	 */
	public int send(com.appeligo.search.entity.Message... messages) {
		int sent = 0;
		if (messages == null) {
			return 0;
		}
		for (com.appeligo.search.entity.Message message: messages) {
			User user = message.getUser();
			if (user != null) {
				boolean changed = false;
				boolean abort = false;
				if ((!user.isEnabled()) ||
					(!user.isRegistrationComplete()) ||
					(message.isSms() &&
							(!user.isSmsValid()))) {
					abort = true;
					if (message.getExpires() == null) {
						Calendar cal = Calendar.getInstance();
						cal.add(Calendar.HOUR, 24);
						message.setExpires(new Timestamp(cal.getTimeInMillis()));
						changed = true;
					}
				}
				if (message.isSms() &&
						user.isSmsValid() &&
						(!user.isSmsOKNow())) {
					
					Calendar now = Calendar.getInstance(user.getTimeZone());
					now.set(Calendar.MILLISECOND, 0);
					now.set(Calendar.SECOND, 0);
					
					Calendar nextWindow = Calendar.getInstance(user.getTimeZone());
					nextWindow.setTime(user.getEarliestSmsTime());
					nextWindow.set(Calendar.MILLISECOND, 0);
					nextWindow.set(Calendar.SECOND, 0);
					nextWindow.add(Calendar.MINUTE, 1); // compensate for zeroing out millis, seconds
					
					nextWindow.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE));
					
					int nowMinutes = (now.get(Calendar.HOUR)*60) + now.get(Calendar.MINUTE);
					int nextMinutes = (nextWindow.get(Calendar.HOUR)*60) + nextWindow.get(Calendar.MINUTE);
					if (nowMinutes > nextMinutes) {
						nextWindow.add(Calendar.HOUR, 24);
					}
					message.setDeferUntil(new Timestamp(nextWindow.getTimeInMillis()));
					changed = true;
					abort = true;
				}
				if (changed) {
					message.save();
				}
				if (abort) {
					continue;
				}
			}
			String to = message.getTo();		
			String from = message.getFrom();
			String subject = message.getSubject();
			String body = message.getBody();
			String contentType = message.getMimeType();
			try {
							
				Properties props = new Properties();
				
				//Specify the desired SMTP server
				props.put("mail.smtp.host", mailHost);
				props.put("mail.smtp.port", Integer.toString(port));
				// create a new Session object
				Session session = null;
				if (password != null) {
					props.put("mail.smtp.auth", "true");
					session = Session.getInstance(props,new SMTPAuthenticator(smtpUser, password));
				} else {
					session = Session.getInstance(props, null);
				}
				session.setDebug(debug);
				
				// create a new MimeMessage object (using the Session created above)
				Message mimeMessage = new MimeMessage(session);
				mimeMessage.setFrom(new InternetAddress(from));
				mimeMessage.setRecipients(Message.RecipientType.TO, new InternetAddress[] { new InternetAddress(to) });
				mimeMessage.setSubject(subject);
				mimeMessage.setContent(body.toString(), contentType);
				if (mailHost.trim().equals("")) {
					log.info("No Mail Host.  Would have sent:");
					log.info("From: "+from);
					log.info("To: "+to);
					log.info("Subject: "+subject);
					log.info(mimeMessage.getContent());
				} else {
					Transport.send(mimeMessage);
					sent++;
				}
				message.setSent(new Date());
			} catch (Throwable t) {
				message.failedAttempt();
				if (message.getAttempts() >= maxAttempts) {
					message.setExpires(new Timestamp(System.currentTimeMillis()));
				}
				log.error(t.getMessage(), t);
			}
		}
		return sent;
	}
	
	/**
	* SimpleAuthenticator is used to do simple authentication
	* when the SMTP server requires it.
	*/
	private static class SMTPAuthenticator extends javax.mail.Authenticator {
		private String username;
		private String password;
		
		public SMTPAuthenticator(String username, String password) {
			this.username = username;
			this.password = password;
		}
	    public PasswordAuthentication getPasswordAuthentication() {
	        return new PasswordAuthentication(username, password);
	    }
	}
}
