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
import java.util.Date;
import java.util.List;

public class ScheduledProgram extends Program implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1423432892820912805L;

	private Date startTime;
	private Date endTime;
    private boolean newEpisode;
    
	private long scheduleId;
	private Network network;
	private String lineupId;
	
	public ScheduledProgram() {
	}

	/**
	 * @param programTitle
	 * @param episodeTitle
	 * @param description
	 * @param descriptionWithActors
	 * @param startTime
	 * @param endTime
	 * @param starRating
	 * @param tvRating
	 * @param sap
	 * @param blackout
	 * @param sexRating
	 * @param violenceRating
	 * @param languageRating
	 * @param dialogRating
	 * @param fvRating
	 * @param enhanced
	 * @param threeD
	 * @param letterbox
	 * @param hdtv
	 * @param scheduleId
	 * @param programId
	 */
	public ScheduledProgram(String programTitle, String reducedTitle40, String episodeTitle, String description, 
			String descriptionWithActors, Date startTime, Date endTime, double starRating, 
			String tvRating, boolean sap, boolean blackout, boolean sexRating, boolean violenceRating, 
			boolean languageRating, boolean dialogRating, boolean fvRating, boolean enhanced, 
			boolean threeD, boolean letterbox, boolean hdtv, Network network, long scheduleId, 
			String programId, String genreDescription, List<Credit> credits, 
			//int programType,
			Date originalAirDate,  int runTime, boolean newEpisode) {
		super(programTitle, reducedTitle40, episodeTitle, description, descriptionWithActors, starRating, tvRating, 
				sexRating, violenceRating, languageRating, dialogRating, fvRating, enhanced, hdtv, 
				programId, genreDescription, credits, originalAirDate, runTime);
				//, programType);
		this.startTime = startTime;
		this.endTime = endTime;
		this.scheduleId = scheduleId;
		this.network = network;
		this.newEpisode = newEpisode;
	}


	/**
	 * @return Returns the endTime.
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime The endTime to set.
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return Returns the scheduleId.
	 */
	public long getScheduleId() {
		return scheduleId;
	}

	/**
	 * @param scheduleId The scheduleId to set.
	 */
	public void setScheduleId(long scheduleId) {
		this.scheduleId = scheduleId;
	}

	/**
	 * @return Returns the startTime.
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime The startTime to set.
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return Returns the channel.
	 */
	public Network getNetwork() {
		return network;
	}

	/**
	 * @param channel The channel to set.
	 */
	public void setNetwork(Network network) {
		this.network = network;
	}

	/**
	 * @return Returns the repeat.
	 */
	public boolean isNewEpisode() {
		return newEpisode;
	}

	/**
	 * @param repeat The repeat to set.
	 */
	public void setNewEpisode(boolean newEpisode) {
		this.newEpisode = newEpisode;
	}

	/**
	 * @return Returns the lineupId.
	 */
	public String getLineupId() {
		return lineupId;
	}

	/**
	 * @param lineupId The lineupId to set.
	 */
	public void setLineupId(String lineupId) {
		this.lineupId = lineupId;
	}	
	
	
}
