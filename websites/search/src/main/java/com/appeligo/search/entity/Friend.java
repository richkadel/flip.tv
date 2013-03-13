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
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.knowbout.hibernate.model.PersistentObject;

public class Friend  extends PersistentObject {
	
	private long id;
	private User user;
	private User friendUser;
	private String firstName;
	private String lastName;
	private String email;
	private Date created;
	private FriendStatus status;	
	private boolean deleted;
	private boolean recent;
	
	public Friend() {		
	}

	/**
	 * @return Returns the created.
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * @param created The created to set.
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * @return Returns the deleted.
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * @param deleted The deleted to set.
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
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
	 * @return Returns the friendUser.
	 */
	public User getFriendUser() {
		return friendUser;
	}

	/**
	 * @param friendUser The friendUser to set.
	 */
	public void setFriendUser(User friendUser) {
		this.friendUser = friendUser;
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

	/**
	 * @return Returns the recent.
	 */
	public boolean isRecent() {
		return recent;
	}

	/**
	 * @param recent The recent to set.
	 */
	public void setRecent(boolean recent) {
		this.recent = recent;
	}

	/**
	 * @return Returns the status.
	 */
	public FriendStatus getStatus() {
		return status;
	}

	/**
	 * @param status The status to set.
	 */
	public void setStatus(FriendStatus status) {
		this.status = status;
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

	public String getDisplayName() {
		StringBuilder display = new StringBuilder();
		if (friendUser != null) {
			display.append(friendUser.getUsername());
		} 
		StringBuilder friendName = new StringBuilder();
		if (firstName != null) {
			friendName.append(firstName);
		}
		if (lastName != null) {
			if (firstName != null && firstName.length() >0) {
				friendName.append(' ');
			}
			friendName.append(lastName);
		}
		if ((firstName == null || firstName.length() == 0) && (lastName == null ||lastName.length() == 0) && email != null && email.length() > 0) {
			friendName.append(email);
		}
		if (friendUser != null && friendName.length() > 0) {
			display.append(" (");
			display.append(friendName);
			display.append(")");
		} else {
			display.append(friendName);
		}
		return display.toString();

	}
	
	public static Friend findByEmail(User user, String email) {
		Session session = getSession();
		Query query = session.getNamedQuery("Friend.getByEmail");
		query.setString("email", email);
		query.setEntity("user", user);
		return (Friend)query.uniqueResult();
	}
	public static Friend findByInvitee(long id, User user) {
		Session session = getSession();
		Query query = session.getNamedQuery("Friend.getByInvitee");
		query.setLong("id", id);
		query.setEntity("user", user);
		return (Friend)query.uniqueResult();
	}
	
	public static Friend findByInviter(long id, User user) {
		Session session = getSession();
		Query query = session.getNamedQuery("Friend.getByInviter");
		query.setLong("id", id);
		query.setEntity("user", user);
		return (Friend)query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public static List<Friend> findFriends(User user) {
		Session session = getSession();
		Query query = session.getNamedQuery("Friend.getFriends");
		query.setEntity("user", user);
		return query.list();		
	}
	
	@SuppressWarnings("unchecked")
	public static List<Friend> findInvites(User user) {
		Session session = getSession();
		Query query = session.getNamedQuery("Friend.getInvites");
		query.setString("email", user.getPrimaryEmail());
		query.setEntity("invitee", user);
		return query.list();		
	}
	
	public static int getInviteCount(User user) {
		Session session = getSession();
		Query query = session.getNamedQuery("Friend.getInviteCount");
		query.setString("email", user.getPrimaryEmail());
		return (Integer)query.uniqueResult();				
	}
	public static Friend findByUser(User user, User friend) {
		Session session = getSession();
		Query query = session.getNamedQuery("Friend.getByUser");
		query.setEntity("inviter", user);
		query.setEntity("invitee", friend);
		return (Friend)query.uniqueResult();				
	}
	
}
