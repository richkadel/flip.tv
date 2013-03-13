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

package com.appeligo.search.actions.network;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.appeligo.search.actions.BaseAction;
import com.appeligo.search.entity.Friend;
import com.appeligo.search.entity.FriendStatus;
import com.appeligo.search.entity.Message;
import com.appeligo.search.entity.MessageContextException;
import com.appeligo.search.entity.User;
import com.appeligo.search.util.ConfigUtils;

import com.appeligo.util.Utils;

public class InviteAction extends NetworkBaseAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4356686707018719294L;
	
	private Message message;
	private String addresses;
	private List<User> existingUsers = new ArrayList<User>();
	
	public String prepareInvite() throws MessageContextException {
		User user = getUser();
		Map<String, String> context = new HashMap<String, String>();
		String name = user.getDisplayName();
		context.put("name", name);
		 message = new Message("invite", context);
		return SUCCESS;
	}
	
	public String sendInvite() throws MessageContextException {
		StringTokenizer tokenizer = new StringTokenizer(addresses,", \n");
		ArrayList<String> validEmails = new ArrayList<String>();
		User user = getUser();
		Map<String, String> context = new HashMap<String, String>();
		String name = user.getDisplayName();
		context.put("name", name);
		if (user.getGender() == null || user.getGender().length() == 0) {
			context.put("his", "their");
		} else if (user.getGender().charAt(0) == 'M') {
			context.put("his", "his");
		} else {
			context.put("his", "her");
		}

		while (tokenizer.hasMoreTokens()) {
			String email = tokenizer.nextToken().trim();
			if (Utils.isEmailAddress(email)) {
				validEmails.add(email);
			} else {
				addActionError(email + " is not a valid email address. Please correct the address to send the invite.");
			}
		}
		if (hasActionErrors()) {
			 message = new Message("invite", context);
			return INPUT;
		} else {
			for (String email: validEmails) {
				 Friend friend = processEmail(user, email);		
				 if (friend.getFriendUser() != null) {
					 existingUsers.add(friend.getFriendUser());
				 }
				 //If they have blocked this user, or they are already a friend, don't send them an email.
				 if (friend.getStatus() != FriendStatus.BLOCKED && friend.getStatus() != FriendStatus.ACCEPTED) {
					 if (friend.getFriendUser() == null) {		
						 Message message = new Message("invite", context);
						 message.setTo(email);
						 message.insert();
					 } else {
						 Message message = new Message("friend", context);
						 message.setTo(email);
						 message.insert();					 
					 }
				 }
			}
			return SUCCESS;			
		}
	}

	/**
	 * @return Returns the message.
	 */
	public Message getMessage() {
		return message;
	}

	/**
	 * @return Returns the addresses.
	 */
	public String getAddresses() {
		return addresses;
	}

	/**
	 * @param addresses The addresses to set.
	 */
	public void setAddresses(String addresses) {
		this.addresses = addresses;
	}

	/**
	 * @return Returns the existingUsers.
	 */
	public List<User> getExistingUsers() {
		return existingUsers;
	}

	/**
	 * @param existingUsers The existingUsers to set.
	 */
	public void setExistingUsers(List<User> existingUsers) {
		this.existingUsers = existingUsers;
	}
	
	
	
}
