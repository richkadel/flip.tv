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

import org.hibernate.Session;

import com.knowbout.hibernate.HibernateUtil;

public class Channel {
	private String id;
	private String channelNumber;
	private int serviceTier;
	private Date effectiveDate;
	private Date expirationDate;
	private Lineup lineup;
	private Station station;
	
	
	public Channel() {		
	}
	
	/**
	 * @return Returns the channelNumber.
	 */
	public String getChannelNumber() {
		return channelNumber;
	}
	/**
	 * @param channelNumber The channelNumber to set.
	 */
	public void setChannelNumber(String channelNumber) {
		this.channelNumber = channelNumber;
	}
	/**
	 * @return Returns the effectiveDate.
	 */
	public Date getEffectiveDate() {
		return effectiveDate;
	}
	/**
	 * @param effectiveDate The effectiveDate to set.
	 */
	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	/**
	 * @return Returns the expirationDate.
	 */
	public Date getExpirationDate() {
		return expirationDate;
	}
	/**
	 * @param expirationDate The expirationDate to set.
	 */
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
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
	 * @return Returns the lineup.
	 */
	public Lineup getLineup() {
		return lineup;
	}
	/**
	 * @param lineup The lineup to set.
	 */
	public void setLineup(Lineup lineup) {
		this.lineup = lineup;
	}
	/**
	 * @return Returns the serviceTier.
	 */
	public int getServiceTier() {
		return serviceTier;
	}
	/**
	 * @param serviceTier The serviceTier to set.
	 */
	public void setServiceTier(int serviceTier) {
		this.serviceTier = serviceTier;
	}
	/**
	 * @return Returns the station.
	 */
	public Station getStation() {
		return station;
	}
	/**
	 * @param station The station to set.
	 */
	public void setStation(Station station) {
		this.station = station;
	}
	
	public void insert() {
		Session session = HibernateUtil.currentSession();
		session.save(this);
	}
	
	public void delete() {
		Session session = HibernateUtil.currentSession();
		session.delete(this);
	}
	
	public static Channel selectById(String channelId) {
		Session session = HibernateUtil.currentSession();
		return (Channel)session.get(Channel.class, channelId);
	}	
	
}
