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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelSchedule {
	public static final long EAST_WEST_OFFSET = 3*60; //3 Hours (we dropped seconds/milliseconds from the key

	private StationLineup station;
	private String programId;
	private Map<Long, ScheduleAiring> pacificAirings;
	private Map<Long, ScheduleAiring> easternAirings;
	private List<ScheduleAiring> singleStationAirings;
	private List<ScheduleAiring> validAirings;

	public ChannelSchedule(String programId, StationLineup station) {
		this.station = station;
		this.programId = programId;
		pacificAirings = new HashMap<Long, ScheduleAiring>();
		easternAirings = new HashMap<Long, ScheduleAiring>();
		singleStationAirings = new ArrayList<ScheduleAiring>();
		validAirings = new ArrayList<ScheduleAiring>();
	}
	
	public void addAiring(ScheduleAiring airing) {		
		//If there are two channels, then we will get two airings for each program
		//On east and one west. Sometimes not always which is why we remove single programming below.
		//But if it is on both the east and west feed, this will happen.
		if (station.isDualChannels()) {
			if (station.isPacificTime(airing.getStationId())) {
				pacificAirings.put(airing.getKey(), airing);
				airing.setPacficifFeed(true);
			}
			if (station.isEasternTime(airing.getStationId())){
				easternAirings.put(airing.getKey(), airing);
				airing.setPacficifFeed(false);
			}
		} else {
			//It is a single station that is on both east and west coast (single stream, all airings are 
			//the same time i.e. its on at 7:00E, 4:00P), so there will only be a single schedule for the
			//the network
			if (station.isPacificTime(airing.getStationId())) {
				airing.setPacficifFeed(true);
			} else {
				airing.setPacficifFeed(false);
			}
			singleStationAirings.add(airing);
		}
	}
	
	public void removeSingleProgramming() {		
		validAirings.clear();
		//Loop over pacific airings
		for (ScheduleAiring airing: pacificAirings.values()) {
			//see if the same program is airing -3 hours in eastern time zone || is airing at the exact same time
			//If so, it is a valid program (i.e. they are showing the same live event or they are showing the same 
			//content, just 3 hours later
			long key = airing.getKey();
		
			//First check to see if they are the same instance
			ScheduleAiring eastAiring = easternAirings.get(key);

			//EXPLAIN(CE): Need to account for a program that hours at 6 and 9 on both east/west
			//When it does this, it looks like a live event and we get the wrong time in the database (the second showing on the east coast.)
			//3 hour repeat check
			long repeatKey = key + EAST_WEST_OFFSET;
			ScheduleAiring repeatAiring = pacificAirings.get(repeatKey);
			
			
			//The same program is airing at the same time on the east coast, it must be a live event, unless it is on twice 3 hours apart
			if (eastAiring != null && repeatAiring == null) {
				validAirings.add(eastAiring);
			} else {	
				//Check if it was on the east coast 3 hours ago
				key = key - EAST_WEST_OFFSET;
				eastAiring = easternAirings.get(key);
				//If its not null then we have the basic 3 hour difference
				if (eastAiring != null) {
					validAirings.add(eastAiring);
				}
			}
		}		
		
		//Add all the singleStationAirings
		validAirings.addAll(singleStationAirings);
	}
	
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/d HH:mm");
		StringBuilder sb = new StringBuilder();
		List<ScheduleAiring> west = new ArrayList<ScheduleAiring>();
		west.addAll(pacificAirings.values());
		Collections.sort(west);
		sb.append("West\t");
		for(ScheduleAiring airing: west) {
			sb.append(sdf.format(airing.getStartTime()));
			sb.append('\t');
		}
		sb.append('\n');
		List<ScheduleAiring> east = new ArrayList<ScheduleAiring>();
		east.addAll(easternAirings.values());
		Collections.sort(east);
		sb.append("East\t");
		for(ScheduleAiring airing: east) {
			sb.append(sdf.format(airing.getStartTime()));
			sb.append('\t');
		}
		sb.append('\n');
		List<ScheduleAiring> all = new ArrayList<ScheduleAiring>();
		all.addAll(validAirings);
		Collections.sort(all);
		sb.append(programId);
		sb.append(" Schedule\t");
		for(ScheduleAiring airing: all) {
			sb.append(sdf.format(airing.getStartTime()));
			sb.append('\t');
		}
		sb.append('\n');
		return sb.toString();
	}

	/**
	 * @return Returns the validAirings.
	 */
	public List<ScheduleAiring> getValidAirings() {
		return validAirings;
	}

	/**
	 * @param validAirings The validAirings to set.
	 */
	public void setValidAirings(List<ScheduleAiring> validAirings) {
		this.validAirings = validAirings;
	}

	/**
	 * @return Returns the programId.
	 */
	public String getProgramId() {
		return programId;
	}

	/**
	 * @param programId The programId to set.
	 */
	public void setProgramId(String programId) {
		this.programId = programId;
	}

	/**
	 * @return Returns the station.
	 */
	public StationLineup getStation() {
		return station;
	}

	/**
	 * @param station The station to set.
	 */
	public void setStation(StationLineup station) {
		this.station = station;
	}

	/**
	 * @return Returns the easternAirings.
	 */
	public Map<Long, ScheduleAiring> getEasternAirings() {
		return easternAirings;
	}

	/**
	 * @param easternAirings The easternAirings to set.
	 */
	public void setEasternAirings(Map<Long, ScheduleAiring> easternAirings) {
		this.easternAirings = easternAirings;
	}

	/**
	 * @return Returns the pacificAirings.
	 */
	public Map<Long, ScheduleAiring> getPacificAirings() {
		return pacificAirings;
	}

	/**
	 * @param pacificAirings The pacificAirings to set.
	 */
	public void setPacificAirings(Map<Long, ScheduleAiring> pacificAirings) {
		this.pacificAirings = pacificAirings;
	}

	/**
	 * @return Returns the singleStationAirings.
	 */
	public List<ScheduleAiring> getSingleStationAirings() {
		return singleStationAirings;
	}

	/**
	 * @param singleStationAirings The singleStationAirings to set.
	 */
	public void setSingleStationAirings(List<ScheduleAiring> singleStationAirings) {
		this.singleStationAirings = singleStationAirings;
	}
	
	

	
}
