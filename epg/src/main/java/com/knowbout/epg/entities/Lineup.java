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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

import com.knowbout.hibernate.HibernateUtil;

public class Lineup {
	
	private String id;
	private String name;
	private Device device;
	private Set<Channel> channels;
	private Headend headend;
	
	public Lineup() {		
	}
	
	/**
	 * @return Returns the channels.
	 */
	public Set<Channel> getChannels() {
		return channels;
	}
	/**
	 * @param channels The channels to set.
	 */
	public void setChannels(Set<Channel> channels) {
		this.channels = channels;
	}
	/**
	 * @return Returns the device.
	 */
	public Device getDevice() {
		return device;
	}
	/**
	 * @param device The device to set.
	 */
	public void setDevice(Device device) {
		this.device = device;
	}
	/**
	 * @return Returns the headend.
	 */
	public Headend getHeadend() {
		return headend;
	}
	/**
	 * @param headend The headend to set.
	 */
	public void setHeadend(Headend headend) {
		this.headend = headend;
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
	
	public static Lineup selectById(String id) {
		Session session = HibernateUtil.currentSession();
		return (Lineup)session.get(Lineup.class, id);
	}
	
	/**
	 * Get the Channel(s) for the given channel number on this lineup.
	 * It is possible for there to be two channels for the same channel number.
	 * If this is the case, each will have a different station and obviously only
	 * one will be playing content at a time. This creates a time sharing situation 
	 * for that channel.  95% of the time, this does not happen, but it does for a few
	 * channels.
	 * @param channelNumber
	 * @return the list of channels for the given channel number.
	 */
	@SuppressWarnings("unchecked")
	public List<Channel> getChannel(String channelNumber) {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Lineup.getChannels");
		query.setString("lineupId", id);
		query.setString("channelNumber", channelNumber);
		List results = query.list();
		List<Channel> castResults = new ArrayList<Channel>(results.size());
		castResults.addAll(results);
		return castResults;
	}
}
