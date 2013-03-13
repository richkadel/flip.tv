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

package com.appeligo.search.entity;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.appeligo.search.util.ConfigUtils;
import com.knowbout.hibernate.model.PersistentObject;

/**
 * Provides a general abstraction of a message.  This is expected to be used for
 * email in general, but the send message can be overridden to support other transport
 * types if needed.
 * 
 * @author fear
 */
public class Message extends PersistentObject {

	private static final Logger log = Logger.getLogger(Message.class);
	
	private long id;
	
	private String subject;
	
	private String body;
	
	private String to;
	
	private String from;
	
	private Date sent;
	
	private Timestamp deferUntil;
	
	private Timestamp expires;
	
	private String mimeType;
	
	private int attempts;
	
	private User user;
	
	private boolean sms;
	
	private int priority;
	
	/**
	 * An array of type objects used to provide keys into the message template repository
	 * when a default message format can be used to craft a message.
	 * @author fear
	public enum MessageType {
		REGISTRATION,
		SMS_VERIFY,
		WELCOME,
		FEEDBACK,
		KEYWORD_ALERT,
		PROGRAM_REMINDER,
		PASSWORD_RESET,
	}
	 */
	
	public Message() {
		Configuration config = ConfigUtils.getMessageConfig();
		setFrom(config.getString("defaultFrom", "noreply@fliptv.com"));
		setPriority(1);
	}
	
	public Message(String type, Map<String, String> context) throws MessageContextException {
		this();
		createContent(type, context);
	}

	/**
	 * Will definitely need some localization at some point.
	 * @param type
	 * @param context
	 * @throws MessageContextException
	 */
	public void createContent(String type, Map<String, String> context) throws MessageContextException {
		Configuration config = ConfigUtils.getMessageConfig();
		String subject = config.getString(type + ".subject", "No subject");
		String[] bodyComponents = config.getStringArray(type + ".body");
		StringBuilder sb = new StringBuilder();
		for (String bodyComponent : bodyComponents) {
			if (sb.length() != 0) {
				sb.append(", ");
			}
			sb.append(bodyComponent);
		}
		String body = sb.toString();
		String mimeType = config.getString(type + ".mimeType", "text/plain");
		this.setMimeType(mimeType);
		subject = macroReplacement(subject, context);
		body = macroReplacement(body, context);
		if (log.isDebugEnabled()) {
			log.debug("Preparing message from: " + subject + ":" + body + ":" + mimeType);
		}
		this.setSubject(subject);
		this.setBody(body);
		
		this.setFrom(config.getString(type + ".from", this.getFrom()));
	}
	
	private String macroReplacement(String target, Map<String, String> context) {
		Iterator<String> macroTags = context.keySet().iterator();
		while(macroTags.hasNext() && target.indexOf('@') > -1) {
			String nextTag = macroTags.next();
			String value = context.get(nextTag);
			if (value == null) {
				throw new NullPointerException("Missing tag @"+nextTag+"@");
			}
			target = target.replace(buildMacroTag(nextTag), context.get(nextTag));
		}
		return target;
	}
	
	private String buildMacroTag(String tag) {
		return new StringBuilder("@").append(tag).append('@').toString();
	}
	
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public Date getSent() {
		return sent;
	}

	public void setSent(Date sentDate) {
		this.sent = sentDate;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	/**
	 * 
	 * @param max
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Message> getUnsentMessages(int maxResults, int maxAttempts) {
		Session session = getSession();
		Query query = session.getNamedQuery("Message.getUnsent");
		query.setTimestamp("now", new Timestamp(System.currentTimeMillis()));
		query.setInteger("maxAttempts", maxAttempts);
		query.setMaxResults(maxResults);
		return query.list();
	}

	public static void deleteOldMessages(int days, int maxAttempts) {
		Session session = getSession();
		Query query = session.getNamedQuery("Message.deleteOldMessages");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, (0-(days * 24)));
		query.setTimestamp("oldestSent", new Timestamp(cal.getTimeInMillis()));
		query.setInteger("maxAttempts", maxAttempts);
		query.executeUpdate();
	}

	public void failedAttempt() {
		setAttempts(this.getAttempts() + 1);
	}
	
	public int getAttempts() {
		return attempts;
	}

	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}

	public boolean isSms() {
		return sms;
	}

	public void setSms(boolean sms) {
		this.sms = sms;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Timestamp getDeferUntil() {
		return deferUntil;
	}

	public void setDeferUntil(Timestamp deferUntil) {
		this.deferUntil = deferUntil;
	}

	public Timestamp getExpires() {
		return expires;
	}

	public void setExpires(Timestamp expires) {
		this.expires = expires;
	}
}
