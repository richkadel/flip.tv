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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.appeligo.search.actions.BaseAction;
import com.appeligo.search.entity.Friend;
import com.appeligo.search.entity.FriendStatus;
import com.appeligo.search.entity.User;

public class FriendsAction extends BaseAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3755065431422605492L;
	private Collection<Friend> existingFriends;
	private List<Friend> networkRequests;
	private String inviteAction;
	private Long id;
	private Friend activeFriend;
	private String firstName;
	private String lastName;
	private boolean canEditNames;
	
	public String execute()  {
		User user = getUser();
		existingFriends = Friend.findFriends(user);
		networkRequests = Friend.findInvites(user);
		return SUCCESS;
	}
	
	public String respondToInvitation() {
		User user = getUser();		
		Friend friend = Friend.findByInvitee(id, user);
		//Did the Friend object already exist?
		if (friend != null ) {
			if (friend.isDeleted()) {
				addActionError("The invitation has been deleted.  You can send your own invitation if you wish.");
				return ERROR;
			}
			try {
				FriendStatus status = FriendStatus.valueOf(inviteAction);
				switch (status) {
					case ACCEPTED: {
						friend.setStatus(status);
						if (friend.getFriendUser() == null) {
							friend.setFriendUser(user);
						}
						//Since we accepted, automatically add them to our friend network.
						//First check to see if we already have a Friend for them
						Friend existing = Friend.findByUser(user, friend.getUser());
						if (existing != null) {
							if (existing.isDeleted()) {
								existing.setDeleted(false);
							}
							existing.setStatus(FriendStatus.ACCEPTED);
						} else {
							existing = new Friend();
							//We don't know the email, but that should be ok.
							existing.setUser(user);
							existing.setCreated(new Date());
							existing.setStatus(FriendStatus.ACCEPTED);
							//The friend already is a registered user, so set the user object.
							existing.setFriendUser(friend.getUser());
							user.addFriend(existing);							
						}
						break;
					}
					case DECLINED: {
						//If we decline, and have an existing invite (why we declined is beyond me)
						//Then delete our invite to them.  It is a bydirection friendship.
						friend.setStatus(FriendStatus.DECLINED);
						//PENDING(CE): We should have to do 2 queries here. This is bad.
						Friend existing = Friend.findByEmail(user, friend.getUser().getPrimaryEmail());
						if (existing == null) {
							existing = Friend.findByUser(user, friend.getUser());
						}
						if (existing != null) {
							existing.setDeleted(true);
						}
						break;
					}
					case BLOCKED: {
						//If we block, and have an existing invite (why we blocked is beyond me)
						//Then delete our invite to them.  It is a bydirection friendship.
						friend.setStatus(FriendStatus.BLOCKED);
						//PENDING(CE): We should have to do 2 queries here. This is bad.
						Friend existing = Friend.findByEmail(user, friend.getUser().getPrimaryEmail());
						if (existing == null) {
							existing = Friend.findByUser(user, friend.getUser());
						}
						if (existing != null) {
							existing.setDeleted(true);
						}
						break;						
					}
				}
			} catch (IllegalArgumentException e) {
				//Not a valid status
				addActionError("Unable to process the request.  Please try again.");
				return ERROR;
			}
		}

		existingFriends = user.getFriends();
		networkRequests = Friend.findInvites(user);

		return SUCCESS;			
	}

	public String editFriend() {
		User user = getUser();		
		activeFriend = Friend.findByInviter(id, user);
		canEditNames = true;
		if (activeFriend != null) {
			if (activeFriend.getFriendUser() != null) {
				String firstName = activeFriend.getFriendUser().getFirstName();
				String lastName = activeFriend.getFriendUser().getLastName();
				if ((firstName != null && firstName.length() > 0) || (lastName != null && lastName.length() >0)) {
					canEditNames = false;
				}
			}
			return SUCCESS;
		} else {
			existingFriends = user.getFriends();
			networkRequests = Friend.findInvites(user);
			addActionError("Unable to determine the friend to edit. Please try again.");
			return ERROR;
		}
	}
	
	public String saveFriend() {
		User user = getUser();		
		activeFriend = Friend.findByInviter(id, user);
		if (activeFriend != null) {
			activeFriend.setFirstName(firstName);
			activeFriend.setLastName(lastName);
			existingFriends = user.getFriends();
			networkRequests = Friend.findInvites(user);
			return SUCCESS;
		} else {
			addActionError("Unable to save the changes. Please try again.");
			return INPUT;
		}
	}
	
	public String deleteFriend() {
		User user = getUser();		
		Friend friend = Friend.findByInviter(id, user);
		//Did the Friend object exist?
		if (friend != null ) {
			friend.setDeleted(true);
			User friendUser = friend.getFriendUser();
			if (friendUser != null) {
				Friend inverse = Friend.findByUser(friendUser, user);
				if (inverse != null) {
					inverse.setStatus(FriendStatus.DECLINED);
				}
			}
		}
		existingFriends = Friend.findFriends(user);
		networkRequests = Friend.findInvites(user);
		return SUCCESS;

	}
	/**
	 * @return Returns the inviteAction.
	 */
	public String getInviteAction() {
		return inviteAction;
	}

	/**
	 * @param inviteAction The inviteAction to set.
	 */
	public void setInviteAction(String action) {
		this.inviteAction = action;
	}

	/**
	 * @return Returns the requests.
	 */
	public List<Friend> getNetworkRequests() {
		return networkRequests;
	}

	/**
	 * @param requests The requests to set.
	 */
	public void setNetworkRequests(List<Friend> networkRequests) {
		this.networkRequests = networkRequests;
	}

	/**
	 * @return Returns the id.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return Returns the existingFriends.
	 */
	public Collection<Friend> getExistingFriends() {
		return existingFriends;
	}

	/**
	 * @param existingFriends The existingFriends to set.
	 */
	public void setExistingFriends(Collection<Friend> existingFriends) {
		this.existingFriends = existingFriends;
	}

	/**
	 * @return Returns the activeFriend.
	 */
	public Friend getActiveFriend() {
		return activeFriend;
	}

	/**
	 * @param activeFriend The activeFriend to set.
	 */
	public void setActiveFriend(Friend activeFriend) {
		this.activeFriend = activeFriend;
	}

	/**
	 * @return Returns the canEditNames.
	 */
	public boolean isCanEditNames() {
		return canEditNames;
	}

	/**
	 * @param canEditNames The canEditNames to set.
	 */
	public void setCanEditNames(boolean canEditNames) {
		this.canEditNames = canEditNames;
	}

	/**
	 * @return Returns the firstName.
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName The firstName to set.
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return Returns the lastName.
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName The lastName to set.
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	
	
}
