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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import com.appeligo.epg.DefaultEpg;
import com.appeligo.search.entity.Friend;
import com.appeligo.search.entity.FriendStatus;
import com.appeligo.search.entity.Message;
import com.appeligo.search.entity.MessageContextException;
import com.appeligo.search.entity.User;
import com.appeligo.search.util.ConfigUtils;
import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.Program;

import com.appeligo.util.Utils;

public class RecommendProgramAction extends NetworkBaseAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4356686707018719294L;
	private static final Logger log = Logger.getLogger(RecommendProgramAction.class);

	private Message message;
	private String addresses;
	private String programId;
	private Program program;
	private String programTitle;
	private List<Friend> friends;
	private List<String> friendIds;
	private boolean showOnly;


	public String prepareRecommendation() throws MessageContextException {
		program = getProgram(programId);
		if (program != null) {
			User user = getUser();
			friends = Friend.findFriends(user);
			Map<String, String> context = new HashMap<String, String>();
			String name = user.getDisplayName();
			context.put("name", name);
			generateProgramTitle(program);
			context.put("programTitle", programTitle);
			context.put("link", generateLink(program));
		    message = new Message("programRecommendation", context);
			return SUCCESS;
		} else {
			return ERROR;
		}
	}
	
	public String sendRecommendation() throws MessageContextException {
		User user = getUser();
		program = getProgram(programId);
		if (program == null) {
			friends = Friend.findFriends(user);
			return ERROR;
		}
		StringTokenizer tokenizer = new StringTokenizer(addresses,", \n");
		ArrayList<String> validEmails = new ArrayList<String>();
		Map<String, String> context = new HashMap<String, String>();
		String name = user.getDisplayName();
		context.put("name", name);
		context.put("link", generateLink(program));
		generateProgramTitle(program);
		context.put("programTitle", programTitle);

		while (tokenizer.hasMoreTokens()) {
			String email = tokenizer.nextToken().trim();
			if (Utils.isEmailAddress(email)) {
				validEmails.add(email);
			} else {
				addActionError(email + " is not a valid email address. Please correct the address to send the invite.");
			}
		}
		ArrayList<Friend> toList = new ArrayList<Friend>();
		if (friendIds != null) {
			for (String id: friendIds) {
				Friend friend = Friend.findByInviter(Long.parseLong(id), user);
				if (friend != null) {
					toList.add(friend);
				}
			}
		}
		if (hasActionErrors()) {
			message = new Message("programRecommendation", context);
			friends = Friend.findFriends(user);
			return INPUT;
		} else {
			for (String email: validEmails) {
				 Friend friend = processEmail(user, email);				 
				 if (friend.getStatus() != FriendStatus.BLOCKED) {
					 Message message = new Message("programRecommendation", context);
					 message.setTo(email);
					 message.insert();
				 }
			}
			for (Friend friend: toList) {
				if (friend.getStatus() != FriendStatus.BLOCKED) {
					 Message message = new Message("programRecommendation", context);
					 message.setTo(friend.getFriendUser() != null ? friend.getFriendUser().getPrimaryEmail() : friend.getEmail());
					 message.insert();					
				}
			}
			return SUCCESS;			
		}
	}

	private Program getProgram(String id) {
    	EPGProvider epgProvider = DefaultEpg.getInstance();
    	if (showOnly) {
    		return epgProvider.getShowByProgramId(id);
    	} else  {
    		return epgProvider.getProgram(id);	    		
    	}
	}
	
	private void generateProgramTitle(Program program) {
		programTitle = program.getLabel();
	}
	
	private String generateLink(Program program) {
		Configuration configuration = ConfigUtils.getSystemConfig();
		StringBuilder link = new StringBuilder();
		String url = configuration.getString("url");		
		link.append(url);
		if (url.endsWith("/")) {
			link.deleteCharAt(link.length()-1);
		}
		if (showOnly) {
			link.append(program.getShowWebPath());
		} else {
			link.append(program.getWebPath());
			
		}
		return link.toString();
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
	 * @return Returns the programId.
	 */
	public String getProgramId() {
		return programId;
	}

	/**
	 * @param programId The programId to set.
	 */
	public void setProgramId(String programId) {
		this.programId = programId;
	}

	/**
	 * @return Returns the program.
	 */
	public Program getProgram() {
		return program;
	}

	/**
	 * @return Returns the programTitle.
	 */
	public String getProgramTitle() {
		return programTitle;
	}

	/**
	 * @return Returns the friends.
	 */
	public List<Friend> getFriends() {
		return friends;
	}

	/**
	 * @param friends The friends to set.
	 */
	public void setFriends(List<Friend> friends) {
		this.friends = friends;
	}
	
	/**
	 * @return Returns the friendId.
	 */
	public List<String> getFriendIds() {
		return friendIds;
	}

	/**
	 * @param friendId The friendId to set.
	 */
	public void setFriendIds(List<String> friendIds) {
		this.friendIds = friendIds;
	}

	/**
	 * @return Returns the showOnly.
	 */
	public boolean isShowOnly() {
		return showOnly;
	}

	/**
	 * @param showOnly The showOnly to set.
	 */
	public void setShowOnly(boolean showOnly) {
		this.showOnly = showOnly;
	}
	
	
	
}
