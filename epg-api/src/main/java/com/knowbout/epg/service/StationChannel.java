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

package com.knowbout.epg.service;

import java.io.Serializable;

public class StationChannel implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7510302187407272098L;

	/**
	 * The channel number based on the lineup it is in (55, SG51)
	 */
	private String channel;
	
	private String stationName;
	
	/**
	 * Mnemonic or FCC-recognized call sign for long name of a station.
	 */
	private String stationCallSign;
	
	/**
	 * Network, cable or broadcasting group with which a station is associated.
	 */
	private String affiliation;
	
	/**
	 * Construcsts a new StationChannel instance
	 */
	public StationChannel() {
	}

	/**
	 * @param channel The channel number based on the lineup it is in (55, SG51)
	 * @param stationName The long station name (WABC-TV)
	 * @param stationCallSign Mnemonic or FCC-recognized call sign for long name of a station (WABC)
	 * @param affiliation Network, cable or broadcasting group with which a station is associated. (ABC Affiliate)
	 */
	public StationChannel(String channel, String stationName, String stationCallSign, String affiliation) {
		// TODO Auto-generated constructor stub
		this.channel = channel;
		this.stationName = stationName;
		this.stationCallSign = stationCallSign;
		this.affiliation = affiliation;
	}

	/**
	 * Network, cable or broadcasting group with which a station is associated. (ABC Affiliate)
	 * @return Returns the affiliation.
	 */
	public String getAffiliation() {
		return affiliation;
	}

	/**
	 * Network, cable or broadcasting group with which a station is associated. (ABC Affiliate)
	 * @param affiliation The affiliation to set.
	 */
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	/**
	 * The channel number based on the lineup it is in (55, SG51)
	 * @return Returns the number.
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * The channel number based on the lineup it is in (55, SG51)
	 * @param number The number to set.
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * Mnemonic or FCC-recognized call sign for long name of a station (WABC).
	 * @return Returns the stationCallSign.
	 */
	public String getStationCallSign() {
		return stationCallSign;
	}

	/**
	 * Mnemonic or FCC-recognized call sign for long name of a station (WABC).
	 * @param stationCallSign The stationCallSign to set.
	 */
	public void setStationCallSign(String stationCallSign) {
		this.stationCallSign = stationCallSign;
	}

	/**
	 * The long station name (WABC-TV)
	 * @return Returns the stationName.
	 */
	public String getStationName() {
		return stationName;
	}

	/**
	 * The long station name (WABC-TV)
	 * @param stationName The stationName to set.
	 */
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	public String toString() {
		return "StationChannel[channel: " +channel +", stationName: " 
		+ stationName + ", stationCallSign: " + stationCallSign
		+ ", affiliation: " + affiliation;
	}
}
