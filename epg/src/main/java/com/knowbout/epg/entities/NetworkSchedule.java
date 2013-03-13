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

import java.util.Date;

import org.hibernate.Query;
import org.hibernate.Session;

import com.knowbout.hibernate.HibernateUtil;

public class NetworkSchedule {

	private Schedule schedule;
	private NetworkLineup networkLineup;
	private Date airTime;
	private NetworkScheduleId id;
	
	public NetworkSchedule() {
	}
	
	/**
	 * @param network
	 * @param schedule
	 */
	public NetworkSchedule(Schedule schedule, NetworkLineup networkLineup, Date airTime) {
		id = new NetworkScheduleId(schedule.getId(), networkLineup.getId());
		this.schedule = schedule;
		this.networkLineup = networkLineup;
		this.airTime = airTime;
//		networkLineup.getNetworkSchedules().add(this);
		schedule.getNetworkSchedules().add(this);
	}

	/**
	 * @return Returns the schedule.
	 */
	public Schedule getSchedule() {
		return schedule;
	}

	/**
	 * @param schedule The schedule to set.
	 */
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	/**
	 * @return Returns the networkLineup.
	 */
	public NetworkLineup getNetworkLineup() {
		return networkLineup;
	}

	/**
	 * @param networkLineup The networkLineup to set.
	 */
	public void setNetworkLineup(NetworkLineup networkLineup) {
		this.networkLineup = networkLineup;
	}

	/**
	 * @return Returns the airTime.
	 */
	public Date getAirTime() {
		return airTime;
	}

	/**
	 * @param airTime The airTime to set.
	 */
	public void setAirTime(Date airTime) {
		this.airTime = airTime;
	}

	/**
	 * @return Returns the id.
	 */
	public NetworkScheduleId getId() {
		return id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(NetworkScheduleId id) {
		this.id = id;
	}
	
	public void insert() {
		Session session = HibernateUtil.currentSession();
		session.save(this);
	}
	
	public void delete() {
		Session session = HibernateUtil.currentSession();
		session.delete(this);
	}	
	
	public static int deleteAfter(Date date) {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("NetworkSchedule.deleteByDate");
		query.setTimestamp("date", date);
		int count = query.executeUpdate();
		return count;		
	}
}
