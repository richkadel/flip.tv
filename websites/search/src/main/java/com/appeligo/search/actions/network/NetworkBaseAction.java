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

import java.util.Date;

import com.appeligo.search.actions.BaseAction;
import com.appeligo.search.entity.Friend;
import com.appeligo.search.entity.FriendStatus;
import com.appeligo.search.entity.User;

public class NetworkBaseAction  extends BaseAction {

	
	protected Friend processEmail(User user, String email) {
		 Friend friend = Friend.findByEmail(user, email);		 
		 User friendUser = User.findByEmail(email);
		 if (friend == null && friendUser != null) {
			 friend = Friend.findByUser(user, friendUser);
		 }

		 //Did the Friend object already exist?
		 if (friend != null) {
			 //If it is deleted, do we bring it back? I guess so
			 if (friend.isDeleted()) {
				 friend.setDeleted(false);
			 }
			 if (friend.getFriendUser() == null) {
				 if (friendUser != null) {
					 friend.setFriendUser(friendUser);							 
				 }
			 }					 
		 } else {
			 //Create it
			 friend = new Friend();
			 friend.setEmail(email);
			 friend.setUser(user);
			 friend.setCreated(new Date());
			 friend.setStatus(FriendStatus.INVITED);
			 //The friend already is a registered user, so set the user object.
			 if (friendUser != null) {
				 friend.setFriendUser(friendUser);
			 }
			 user.addFriend(friend);
		 }
		 if (friendUser != null) {
			 //Lets see if the friendUser has invited the user (inverse) if so, then make them friends
			 Friend inverse = Friend.findByEmail(friendUser, user.getPrimaryEmail());
			 if (inverse != null) {
				 if (!inverse.isDeleted() && friend.getStatus() != FriendStatus.BLOCKED) {
					 friend.setStatus(FriendStatus.ACCEPTED);
					 inverse.setStatus(FriendStatus.ACCEPTED);
					 inverse.setFriendUser(user);
				 }
			 }
		 }
		 return friend;

	}
}
