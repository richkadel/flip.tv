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

import java.io.Serializable;

import com.knowbout.hibernate.model.PersistentObject;

/**
 * 
 * @author fear
 */
public class Group extends PersistentObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -747931454934191796L;

	private long userId;
	
	private String username;
	
	private String group;

	public Group() {
		
	}
	
	public Group(String group, String username, long userId) {
		this.group = group;
		this.username = username;
	}
	
	/**
	 * 
	 */
	public int hashCode() {
		return (int)(getUserId() ^ getGroup().hashCode());
	}
	
	/**
	 * 
	 */
	public boolean equals(Object other) {
		if (!(other instanceof Group)) {
			return false;
		}
		Group o = (Group)other;
		return this.getUserId() == o.getUserId() && this.getGroup().equals(o.getGroup());
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}
}
