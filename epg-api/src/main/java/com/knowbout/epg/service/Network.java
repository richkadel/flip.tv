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
import java.util.List;

public class Network implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -8575739583504267136L;

	private long id;
	private String stationName;
	
	/**
	 * Mnemonic or FCC-recognized call sign for long name of a station.
	 */
	private String stationCallSign;
	
	/**
	 * Network, cable or broadcasting group with which a station is associated.
	 */
	private String affiliation;

	private String logo;

	private List<String> lineups;
	
	public Network() {
	}	
	
	/**
	 * @param stationName
	 * @param stationCallSign
	 * @param affiliation
	 * @param logo
	 */
	public Network(long id, String stationName, String stationCallSign, String affiliation, String logo, List<String> lineups) {
		// TODO Auto-generated constructor stub
		this.stationName = stationName;
		this.stationCallSign = stationCallSign;
		this.affiliation = affiliation;
		this.logo = logo;
		this.id = id;
		this.lineups = lineups;
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
	 * @return Returns the stationCallSign.
	 */
	public String getStationCallSign() {
		return stationCallSign;
	}

	/**
	 * @param stationCallSign The stationCallSign to set.
	 */
	public void setStationCallSign(String stationCallSign) {
		this.stationCallSign = stationCallSign;
	}

	/**
	 * @return Returns the stationName.
	 */
	public String getStationName() {
		return stationName;
	}

	/**
	 * @param stationName The stationName to set.
	 */
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	/**
	 * @return Returns the lineups.
	 */
	public List<String> getLineups() {
		return lineups;
	}

	/**
	 * @param lineups The lineups to set.
	 */
	public void setLineups(List<String> lineups) {
		this.lineups = lineups;
	}
	
	
}
