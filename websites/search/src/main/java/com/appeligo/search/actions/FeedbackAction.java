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

package com.appeligo.search.actions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.appeligo.search.entity.Feedback;
import com.appeligo.search.entity.Message;
import com.appeligo.search.entity.User;
import com.appeligo.search.util.ConfigUtils;

public class FeedbackAction extends BaseAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3153898742595758926L;
	
	
	private String subject;
	private String message;
	private String email;
	private String url;
	private String send;
	
	public String execute() throws Exception {
		return SUCCESS;
	}
	/* (non-Javadoc)
	 * @see com.opensymphony.xwork.ActionSupport#execute()
	 */
	public String save() throws Exception {
		if (send != null) {
			if (getMessage() != null) {
				Feedback feedback = new Feedback();
				feedback.setSubject(getSubject());
				feedback.setMessage(getMessage());
				feedback.setEmail(getEmail());
				feedback.setRecieved(new Date());
				if (url != null && url.length() > 0) {
					feedback.setUrl(url);
				} else {
					feedback.setUrl("Unknown");
				}
				User user = getUser();
				if (user != null) {
					feedback.setUser(user);
				}
				feedback.insert();
				
				Map<String, String> context = new HashMap<String, String>();
				context.put("subject", feedback.getSubject());
				context.put("message", feedback.getMessage());
				context.put("username", (user!= null ? user.getUsername() : "NONE"));
				context.put("email", (feedback.getEmail()!= null ? feedback.getEmail() : "NONE"));
				Message message = new Message("feedback", context);
	            String alertEmail = ConfigUtils.getSystemConfig().getString("feedbackEmail","feedback@appeligo.com");
				message.setTo(alertEmail);
				message.insert();
			}
		}
		return SUCCESS;
	}

	/**
	 * @return Returns the email.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email The email to set.
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return Returns the message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message The message to set.
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return Returns the subject.
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject The subject to set.
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url The url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return Returns the send.
	 */
	public String getSend() {
		return send;
	}
	/**
	 * @param send The send to set.
	 */
	public void setSend(String send) {
		this.send = send;
	}
	
	
	
}
