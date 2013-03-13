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

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.knowbout.hibernate.model.PersistentObject;

public class Feedback extends PersistentObject {

	private long id;
	private String subject;
	private String url;
	private String email;
	private String message;
	private Date received;
	private User user;
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
	 * @return Returns the id.
	 */
	public long getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(long id) {
		this.id = id;
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
	 * @return Returns the recieved.
	 */
	public Date getRecieved() {
		return received;
	}
	/**
	 * @param recieved The recieved to set.
	 */
	public void setRecieved(Date received) {
		this.received = received;
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
	 * @return Returns the user.
	 */
	public User getUser() {
		return user;
	}
	/**
	 * @param user The user to set.
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Feedback> getFeedback() {
		Session session = getSession();
		Query query = session.getNamedQuery("Feedback.getAll");
		return query.list();
	}
}
