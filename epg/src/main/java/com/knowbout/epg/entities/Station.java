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
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

import com.knowbout.hibernate.HibernateUtil;

public class Station {
	
	private long id;
	private String timeZone;
	private String name;
	private String callSign;
	private String affiliation;
	private String city;
	private String state;
	private String zipCode;
	private String country;
	private String dmaName;
	private int dmaNumber;
	private int fccChannelNumber;
	private Set<Channel> channels;
	
	public Station() {	
	}
	
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
	 * @return Returns the channels.
	 */
	public Set<Channel> getChannels() {
		if (channels == null) {
			channels = new HashSet<Channel>();			
		}
		return channels;
	}
	/**
	 * @param channels The channels to set.
	 */
	public void setChannels(Set<Channel> channels) {
		this.channels = channels;
	}
	
	public void addChannel(Channel channel) {
		getChannels().add(channel);
		channel.setStation(this);
	}
	
	public void removeChannel(Channel channel) {
		getChannels().remove(channel);
		channel.setStation(null);
	}
	
	/**
	 * @return Returns the city.
	 */
	public String getCity() {
		return city;
	}
	/**
	 * @param city The city to set.
	 */
	public void setCity(String city) {
		this.city = city;
	}
	/**
	 * @return Returns the country.
	 */
	public String getCountry() {
		return country;
	}
	/**
	 * @param country The country to set.
	 */
	public void setCountry(String country) {
		this.country = country;
	}
	/**
	 * @return Returns the dmaName.
	 */
	public String getDmaName() {
		return dmaName;
	}
	/**
	 * @param dmaName The dmaName to set.
	 */
	public void setDmaName(String dmaName) {
		this.dmaName = dmaName;
	}
	/**
	 * @return Returns the dmaNumber.
	 */
	public int getDmaNumber() {
		return dmaNumber;
	}
	/**
	 * @param dmaNumber The dmaNumber to set.
	 */
	public void setDmaNumber(int dmaNumber) {
		this.dmaNumber = dmaNumber;
	}
	/**
	 * @return Returns the fccChannelNumber.
	 */
	public int getFccChannelNumber() {
		return fccChannelNumber;
	}
	/**
	 * @param fccChannelNumber The fccChannelNumber to set.
	 */
	public void setFccChannelNumber(int fccChannelNumber) {
		this.fccChannelNumber = fccChannelNumber;
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
	 * @return Returns the timeZone.
	 */
	public String getTimeZone() {
		return timeZone;
	}
	/**
	 * @param timeZone The timeZone to set.
	 */
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
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
	
	public static Station selectById(long stationlId) {
		Session session = HibernateUtil.currentSession();
		return (Station)session.get(Station.class, stationlId);
	}	
	
	public static List<Station> selectAll() {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Station.selectAll");
		List<Station> list = query.list();
		return list;
	}	
	
}
