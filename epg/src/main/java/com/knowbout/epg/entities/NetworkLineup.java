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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.hibernate.Query;
import org.hibernate.Session;

import com.knowbout.hibernate.HibernateUtil;

public class NetworkLineup {

	private String id;
	private String name;
	private boolean digital;
	private int delay;
	private int affiliateDelay;
	private Set<Network> networks;
//	private Set<NetworkSchedule> schedules;
	
	/**
	 * @return Returns the affiliateDelay.
	 */
	public int getAffiliateDelay() {
		return affiliateDelay;
	}
	/**
	 * @param affiliateDelay The affiliateDelay to set.
	 */
	public void setAffiliateDelay(int affiliateDelay) {
		this.affiliateDelay = affiliateDelay;
	}
	/**
	 * @return Returns the delay.
	 */
	public int getDelay() {
		return delay;
	}
	/**
	 * @param delay The delay to set.
	 */
	public void setDelay(int delay) {
		this.delay = delay;
	}
	/**
	 * @return Returns the digital.
	 */
	public boolean isDigital() {
		return digital;
	}
	/**
	 * @param digital The digital to set.
	 */
	public void setDigital(boolean digital) {
		this.digital = digital;
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

	public void insert() {
		Session session = HibernateUtil.currentSession();
		session.save(this);
	}
	
	public void delete() {
		Session session = HibernateUtil.currentSession();
		session.delete(this);
	}
	
	
	
	/**
	 * @return Returns the networks.
	 */
	public Set<Network> getNetworks() {
		if (networks == null) {
			networks = new HashSet<Network>();
		}
		return networks;
	}
	/**
	 * @param networks The networks to set.
	 */
	public void setNetworks(Set<Network> schedules) {
		this.networks = schedules;
	}
	
//	/**
//	 * @return Returns the schedules.
//	 */
//	public Set<NetworkSchedule> getNetworkSchedules() {
//		if (schedules == null) {
//			schedules = new HashSet<NetworkSchedule>();
//		}
//		return schedules;
//	}
//	/**
//	 * @param schedules The schedules to set.
//	 */
//	public void setNetworkSchedules(Set<NetworkSchedule> schedules) {
//		this.schedules = schedules;
//	}
	
	public Date applyDelay(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		cal.setTime(date);
		cal.add(Calendar.HOUR_OF_DAY, delay);
		return cal.getTime();
	}
	
	public Date applyAffiliationDelay(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		cal.setTime(date);
		cal.add(Calendar.HOUR_OF_DAY, affiliateDelay);
		return cal.getTime();
	}
	
	public static List<NetworkLineup>  selectSearchableLineups() {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("NetworkLineup.selectSearchable");
		return query.list();
	
	}

	public static List<NetworkLineup>  selectAll() {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("NetworkLineup.selectAll");
		return query.list();
	
	}
	
}
