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

package com.appeligo.search.actions;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.document.DateTools;

import com.appeligo.lucene.DocumentWrapper;
import com.knowbout.epg.service.Program;
import com.knowbout.epg.service.ScheduledProgram;

public class SearchResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(SearchResult.class);
	
	private DocumentWrapper doc;
	private ScheduledProgram lastShowing;
	private ScheduledProgram nextShowing;
	private Program programInfo;
	private String programId;
	private String programTitle;
	private String episodeTitle;
	private String stationName;
	private String stationCallSign;
	private String description;	
	private Date airing;
	private Date endTime;
	private boolean future;
	private boolean onAir;
	private String lineup;
	/**
	 * @param doc
	 * @param lastShowing
	 * @param nextShowing
	 * @param programInfo
	 */
	public SearchResult(String lineup, DocumentWrapper doc, Program programInfo, ScheduledProgram lastShowing, ScheduledProgram nextShowing) {
		this.doc = doc;
		this.lineup = lineup;
		programId = doc.get("programID");
		programTitle = doc.get("programTitle");
		episodeTitle = doc.get("episodeTitle");
		stationName = doc.get("lineup-"+lineup+"-stationName");
		stationCallSign = doc.get("lineup-"+lineup+"-stationCallSign");
		description = doc.get("description");
		try {
			airing = DateTools.stringToDate(doc.get("lineup-"+lineup+"-startTime"));
			endTime = DateTools.stringToDate(doc.get("lineup-"+lineup+"-endTime"));
    		Date now = new Date();
    		future = endTime.after(now);
    		onAir = airing.before(now) && future;
		} catch (ParseException e) {
			log.error("Couldn't parse start or end time for "+programId, e);
		}
		
		this.lastShowing = lastShowing;
		this.nextShowing = nextShowing;
		this.programInfo = programInfo;
	}

	/**
	 * @return Returns the doc.
	 */
	public DocumentWrapper getDoc() {
		return doc;
	}

	/**
	 * @param doc The doc to set.
	 */
	public void setDoc(DocumentWrapper doc) {
		this.doc = doc;
	}

	/**
	 * @return Returns the lastShowing.
	 */
	public ScheduledProgram getLastShowing() {
		return lastShowing;
	}

	/**
	 * @param lastShowing The lastShowing to set.
	 */
	public void setLastShowing(ScheduledProgram lastShowing) {
		this.lastShowing = lastShowing;
	}

	/**
	 * @return Returns the nextShowing.
	 */
	public ScheduledProgram getNextShowing() {
		return nextShowing;
	}

	/**
	 * @param nextShowing The nextShowing to set.
	 */
	public void setNextShowing(ScheduledProgram nextShowing) {
		this.nextShowing = nextShowing;
	}

	/**
	 * @return Returns the programInfo.
	 */
	public Program getProgramInfo() {
		return programInfo;
	}

	/**
	 * @param programInfo The programInfo to set.
	 */
	public void setProgramInfo(Program programInfo) {
		this.programInfo = programInfo;
	}

	/**
	 * @return Returns the airing.
	 */
	public Date getAiring() {
		return airing;
	}

	/**
	 * @param airing The airing to set.
	 */
	public void setAiring(Date airing) {
		this.airing = airing;
	}

	/**
	 * @return Returns the episodeTitle.
	 */
	public String getEpisodeTitle() {
		return episodeTitle;
	}

	/**
	 * @param episodeTitle The episodeTitle to set.
	 */
	public void setEpisodeTitle(String episodeTitle) {
		this.episodeTitle = episodeTitle;
	}

	/**
	 * @return Returns the future.
	 */
	public boolean isFuture() {
		return future;
	}

	/**
	 * @param future The future to set.
	 */
	public void setFuture(boolean future) {
		this.future = future;
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
	 * @return Returns the programTitle.
	 */
	public String getProgramTitle() {
		return programTitle;
	}

	/**
	 * @param programTitle The programTitle to set.
	 */
	public void setProgramTitle(String programTitle) {
		this.programTitle = programTitle;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
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
	 * @return Returns the onAir.
	 */
	public boolean isOnAir() {
		return onAir;
	}

	/**
	 * @param onAir The onAir to set.
	 */
	public void setOnAir(boolean onAir) {
		this.onAir = onAir;
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
	
	
}
