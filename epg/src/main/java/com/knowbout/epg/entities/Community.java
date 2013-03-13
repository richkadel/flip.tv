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

package com.knowbout.epg.entities;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Session;

import com.knowbout.hibernate.HibernateUtil;

public class Community {
	
	private String id;	
	private String name;
	private String countyName;
	private String countySize;
	private int countyCode;
	private String state;
	private String zipCode;
	private Set<Headend> headends;
	
	public Community() {		
	}
	
	/**
	 * @return Returns the countyCode.
	 */
	public int getCountyCode() {
		return countyCode;
	}
	/**
	 * @param countyCode The countyCode to set.
	 */
	public void setCountyCode(int countyCode) {
		this.countyCode = countyCode;
	}
	/**
	 * @return Returns the countyName.
	 */
	public String getCountyName() {
		return countyName;
	}
	/**
	 * @param countyName The countyName to set.
	 */
	public void setCountyName(String countyName) {
		this.countyName = countyName;
	}
	/**
	 * @return Returns the countySize.
	 */
	public String getCountySize() {
		return countySize;
	}
	/**
	 * @param countySize The countySize to set.
	 */
	public void setCountySize(String countySize) {
		this.countySize = countySize;
	}
	/**
	 * @return Returns the headends.
	 */
	public Set<Headend> getHeadends() {
		if (headends == null) {
			headends = new HashSet<Headend>();
		}
		return headends;
	}
	/**
	 * @param headends The headends to set.
	 */
	public void setHeadends(Set<Headend> headends) {
		this.headends = headends;
	}
	
	public void addHeadend(Headend headend) {
		getHeadends().add(headend);
		headend.getCommunities().add(this);
	}

	public void removeHeadend(Headend headend) {
		getHeadends().remove(headend);
		headend.getCommunities().remove(this);
	}	
	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return Returns the state.
	 */
	public String getState() {
		return state;
	}
	/**
	 * @param state The state to set.
	 */
	public void setState(String state) {
		this.state = state;
	}
	/**
	 * @return Returns the zipCode.
	 */
	public String getZipCode() {
		return zipCode;
	}
	/**
	 * @param zipCode The zipCode to set.
	 */
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	
	public void insert() {
		Session session = HibernateUtil.currentSession();
		session.save(this);
	}
	
	public void delete() {
		Session session = HibernateUtil.currentSession();
		session.delete(this);
	}
	
	public static Community selectById(String communityId) {
		Session session = HibernateUtil.currentSession();
		return (Community)session.get(Community.class, communityId);
	}
	
}
