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

import org.hibernate.Query;
import org.hibernate.Session;

import com.knowbout.hibernate.HibernateUtil;

public class Network {
	private long id;
	private String name;
	private String callSign;
	private String affiliation;
	private String logo;
	private Set<NetworkLineup> networkLineups;			
	
	/**
	 * @return Returns the affiliation.
	 */
	public String getAffiliation() {
		return affiliation;
	}

	/**
	 * @param affiliation The affiliation to set.
	 */
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	/**
	 * @return Returns the callSign.
	 */
	public String getCallSign() {
		return callSign;
	}

	/**
	 * @param callSign The callSign to set.
	 */
	public void setCallSign(String callSign) {
		this.callSign = callSign;
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
	 * @return Returns the logo.
	 */
	public String getLogo() {
		return logo;
	}

	/**
	 * @param logo The logo to set.
	 */
	public void setLogo(String logo) {
		this.logo = logo;
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
	 * @return Returns the networkLinups.
	 */
	public Set<NetworkLineup> getNetworkLineups() {
		if (networkLineups == null) {
			networkLineups = new HashSet<NetworkLineup>();
		}
		return networkLineups;
	}

	/**
	 * @param networkLinups The networkLinups to set.
	 */
	public void setNetworkLineups(Set<NetworkLineup> lineup) {
		this.networkLineups = lineup;
	}

	public void insert() {
		Session session = HibernateUtil.currentSession();
		session.save(this);
	}
	
	public void delete() {
		Session session = HibernateUtil.currentSession();
		session.delete(this);
	}
	
	public static Network findByCallSign(String callSign) {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Network.findByCallSign");
		query.setString("callSign", callSign);
		query.setMaxResults(1);		
		return (Network)query.uniqueResult();
	}
	
}
