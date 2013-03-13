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

package com.appeligo.search.actions.admin;

import java.util.List;

import org.hibernate.Session;

import com.appeligo.search.actions.BaseAction;
import com.appeligo.search.entity.User;
import com.knowbout.hibernate.HibernateUtil;

public class AdminAction extends BaseAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6407592497533066113L;
	
	private List<User> users;
	private long u;
	
	public String execute() throws Exception {
		users = User.getUsers();		
		return SUCCESS;
	}
	/**
	 * @return Returns the users.
	 */
	public List<User> getUsers() {
		return users;
	}
	/**
	 * @param users The users to set.
	 */
	public void setUsers(List<User> users) {
		this.users = users;
	}
	
	public String validateUser() {
		if (u > 0){
			Session session = HibernateUtil.currentSession();
			User user = (User)session.load(User.class, u);
			user.setRegistrationComplete(true);
			users = User.getUsers();		
			return SUCCESS;
		} else {
			addActionError("Unable to indentify the user to validate.");
			users = User.getUsers();		
			return ERROR;
		}
	}
	
	public String validateSMS() {
		if (u > 0){
			Session session = HibernateUtil.currentSession();
			User user = (User)session.load(User.class, u);
			user.setSmsVerified(true);
			users = User.getUsers();		
			return SUCCESS;
		} else {
			addActionError("Unable to indentify the user for this mobile device.");
			users = User.getUsers();		
			return ERROR;
		}
		
	}
	
	public String enableUser() {
		if (u > 0){
			Session session = HibernateUtil.currentSession();
			User user = (User)session.load(User.class, u);
			user.setEnabled(!user.isEnabled());
			users = User.getUsers();		
			return SUCCESS;
		} else {
			addActionError("Unable to indentify the user to enable/disable");
			users = User.getUsers();		
			return ERROR;
		}
		
	}
	/**
	 * @return Returns the u.
	 */
	public long getU() {
		return u;
	}
	/**
	 * @param u The u to set.
	 */
	public void setU(long u) {
		this.u = u;
	}
	

}
