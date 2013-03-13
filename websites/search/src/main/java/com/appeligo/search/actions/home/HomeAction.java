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

package com.appeligo.search.actions.home;

import java.util.Collection;
import java.util.List;

import com.appeligo.search.actions.BaseAction;
import com.appeligo.search.entity.Favorite;
import com.appeligo.search.entity.Friend;
import com.appeligo.search.entity.User;

public class HomeAction extends BaseAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2157613811695317607L;
	
	private static final String HOME = "home";
	private Collection<Friend> existingFriends;
	private List<Friend> networkRequests;
	private List<Favorite> favoriteShows;
	private List<Favorite> favoriteEpisodes;
	protected User user;
	public String execute()  {
		user = getUser();
		if (user == null || (!isPublicLaunch())) {
			return SUCCESS;
			
		} else {
			existingFriends = Friend.findFriends(user);
			networkRequests = Friend.findInvites(user);
			favoriteShows = Favorite.getFavoriteShows(user);
			favoriteEpisodes = Favorite.getFavoriteEpisodes(user);
			return HOME;
		}
	}

	public User getCurrentUser() {
		return user;
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
	 * @return Returns the favoriteShows.
	 */
	public List<Favorite> getFavoriteShows() {
		return favoriteShows;
	}

	/**
	 * @param favoriteShows The favoriteShows to set.
	 */
	public void setFavoriteShows(List<Favorite> favoriteShows) {
		this.favoriteShows = favoriteShows;
	}

	/**
	 * @return Returns the networkRequests.
	 */
	public List<Friend> getNetworkRequests() {
		return networkRequests;
	}

	/**
	 * @param networkRequests The networkRequests to set.
	 */
	public void setNetworkRequests(List<Friend> networkRequests) {
		this.networkRequests = networkRequests;
	}

	/**
	 * @return Returns the favoriteEpisodes.
	 */
	public List<Favorite> getFavoriteEpisodes() {
		return favoriteEpisodes;
	}

	/**
	 * @param favoriteEpisodes The favoriteEpisodes to set.
	 */
	public void setFavoriteEpisodes(List<Favorite> favoriteEpisodes) {
		this.favoriteEpisodes = favoriteEpisodes;
	}
	
	
}
