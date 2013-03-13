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

package com.knowbout.epg.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.knowbout.epg.entities.Channel;
import com.knowbout.epg.entities.Lineup;
import com.knowbout.epg.entities.Station;

public class StationLineup {

	public static final String CA_DEFAULT = "CA04542:DEFAULT";
	public static final String CA_R = "CA04542:R";
	public static final String CA_X = "CA04542:X";
	public static final String FL_DEFAULT = "FL09496:DEFAULT";
	public static final String FL_X = "FL09496:X";
	public static final String FL_2_DEFAULT = "FL09690:DEFAULT";
	public static final String FL_2_X = "FL09690:X";
	private Set<String> lineups;
	private Set<Long> stationIds;
	private Set<Station> stations;
	private Map<Long, Set<String>> lineupMap = new HashMap<Long, Set<String>>();
	private String name;
	private String callSign;
	private String originalName;
	private String affiliation;
	private boolean affilateStation;
	private boolean dualChannels;
	
	public StationLineup(Station station) {
		lineups = new HashSet<String>();
		stationIds = new HashSet<Long>();
		stations = new HashSet<Station>();
		affilateStation = false;
		name = station.getName();
		callSign = determinCallSign(station);
		originalName = station.getName();
		affiliation = station.getAffiliation();
		dualChannels = false;
		addStation(station);
	}
		
	public boolean isOnMultipleHeadends() {
		boolean westCoast = false;
		boolean eastCoast = false;
		if (lineups.contains(CA_DEFAULT) || lineups.contains(CA_R) || lineups.contains(CA_X)) {
			westCoast = true;
		}
		if (lineups.contains(FL_DEFAULT) || lineups.contains(FL_X) || lineups.contains(FL_2_DEFAULT) || lineups.contains(FL_2_X)) {
			eastCoast = true;
		}
		return westCoast && eastCoast;		
	}

	public void addStation(Station station) {
		Set<Channel> channels = station.getChannels();
		Set<String> lineupSet = lineupMap.get(station.getId());
		if (lineupSet == null) {
			lineupSet = new HashSet<String>();
			lineupMap.put(station.getId(), lineupSet);				
		}

		for (Channel channel: channels) {
			String lineupId = channel.getLineup().getId();
			lineups.add(lineupId);
			lineupSet.add(lineupId);
		}
		stationIds.add(station.getId());
		stations.add(station);		
	}
	
	public boolean possiblePacificChannel() {
		return callSign.endsWith("P");		
	}
	
	public String strippedPacificCallSign() {
		return getStrippedPacificCallSign(callSign);
	}
	
	protected String getStrippedPacificCallSign(String sign) {
		//Ugly hack for the history channel.  We will need to push this into a hashmap, if we find more
		//like it.
		if (sign.equals("HISTP")) {
			return "HISTORY";
		} else if (sign.equals("SPIKEP")){
			return "SPIKETV";
		} else {
			return sign.substring(0, sign.length() -1);
		}
		
	}
	
	public String determinCallSign(Station station) {
		String affiliation = station.getAffiliation();
		affilateStation = true;
		String callSign = station.getCallSign().toUpperCase();
		
		if (callSign.matches("[A-Z]{4}DT\\d")) {
			affilateStation = false;
			return station.getCallSign();
		}
		if (affiliation.startsWith("ABC")){
			return "ABC";
		} else if (affiliation.startsWith("NBC")) {
			return "NBC";
		} else if (affiliation.startsWith("CBS")) {
			return "CBS";
		} else if (affiliation.startsWith("Fox")) {
			return "FOX";
		} else if (affiliation.startsWith("PBS")) {
			return "PBS";
		} else if (affiliation.startsWith("CW")) {
			return "CW";
		} else {
			affilateStation = false;
			return station.getCallSign();
		}
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
	 * @return Returns the lineups.
	 */
	public Set<String> getLineups() {
		return lineups;
	}

	/**
	 * @param lineups The lineups to set.
	 */
	public void setLineups(Set<String> lineups) {
		this.lineups = lineups;
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
	 * @return Returns the originalName.
	 */
	public String getOriginalName() {
		return originalName;
	}

	/**
	 * @param originalName The originalName to set.
	 */
	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	/**
	 * @return Returns the stationIds.
	 */
	public Set<Long> getStationIds() {
		return stationIds;
	}

	/**
	 * @param stationIds The stationIds to set.
	 */
	public void setStationIds(Set<Long> stationIds) {
		this.stationIds = stationIds;
	}

	/**
	 * @return Returns the stations.
	 */
	public Set<Station> getStations() {
		return stations;
	}

	/**
	 * @param stations The stations to set.
	 */
	public void setStations(Set<Station> stations) {
		this.stations = stations;
	}
	
	public void merge(StationLineup sl) {
		Set<Station> lineupStations = sl.getStations();
		for (Station channelStation: lineupStations) {
			addStation(channelStation);
		}

	}

	/**
	 * @return Returns the dualChannels.
	 */
	public boolean isDualChannels() {
		return dualChannels;
	}

	/**
	 * @param dualChannels The dualChannels to set.
	 */
	public void setDualChannels(boolean dualChannels) {
		this.dualChannels = dualChannels;
	}

	public boolean isPacificTime(long stationId) throws IllegalArgumentException {
		//If it is a single channel, then in both,
		//If not then we check the callsign to check for pacific.
		//Check if it is dual channels, if it is, then check
		Station station = null;
		for (Station s: stations) {
			if (s.getId() == stationId) {
				station = s;
				break;
			}
		}
		if (station == null) {
			throw new IllegalArgumentException("Unable to find Station for given stationId:" + stationId);
		}
		return station.getTimeZone().startsWith("Pacific");
	}

	public boolean isEasternTime(long stationId) throws IllegalArgumentException {
		Station station = null;
		for (Station s: stations) {
			if (s.getId() == stationId) {
				station = s;
				break;
			}
		}
		if (station == null) {
			throw new IllegalArgumentException("Unable to find Station for given stationId:" + stationId);
		}
		return station.getTimeZone().startsWith("Eastern");	}
	
	
	public boolean isDigital(long stationId) {
		Set<String> lineup = lineupMap.get(stationId);
		if (lineup == null) {
			throw new IllegalArgumentException("Unable to find Station for given stationId:" + stationId);
		}
		return ( lineup.contains(FL_X) || lineup.contains(FL_2_X) || lineup.contains(CA_X));
		
	}
	public boolean isDigital() {
		return ( lineups.contains(FL_X) || lineups.contains(FL_2_X) || lineups.contains(CA_X));
	}
	
	public boolean isSanDiegoAnalogCable() {
		return (lineups.contains(CA_DEFAULT)|| lineups.contains(CA_R));
	}
	
	public boolean isAnalog(long stationId) {
		Set<String> lineup = lineupMap.get(stationId);
		if (lineup == null) {
			throw new IllegalArgumentException("Unable to find Station for given stationId:" + stationId);
		}
		return ( lineup.contains(CA_DEFAULT) || lineup.contains(CA_R) || lineup.contains(FL_DEFAULT)|| lineup.contains(FL_2_DEFAULT));		
	}
	
	public boolean isAnalog() {
		return ( lineups.contains(CA_DEFAULT) || lineups.contains(CA_R) || lineups.contains(FL_DEFAULT)|| lineups.contains(FL_2_DEFAULT));		
	}

	/**
	 * @return Returns the affilateStation.
	 */
	public boolean isAffilateStation() {
		return affilateStation;
	}

	/**
	 * @param affilateStation The affilateStation to set.
	 */
	public void setAffilateStation(boolean affilateStation) {
		this.affilateStation = affilateStation;
	}
	
}
