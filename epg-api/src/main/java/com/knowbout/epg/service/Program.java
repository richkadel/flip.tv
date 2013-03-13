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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import com.appeligo.epg.util.EpgUtils;

public class Program extends ProgramBase implements Serializable {

	private String reducedTitle40;
	private String description;
	private String descriptionWithActors;
	private String genreDescription;
	private List<Credit> credits;
	private Date originalAirDate;
	private double starRating;
	private String tvRating;
    private boolean sexRating;
    private boolean violenceRating;
    private boolean languageRating;
    private boolean dialogRating;
    private boolean fvRating;
    private boolean enhanced;
    private boolean hdtv;
	private int runTime;
	private Date lastModified;
	
	public Program() {
		credits = new ArrayList<Credit>();
	}
	
	/**
	 * @param programTitle
	 * @param episodeTitle
	 * @param description
	 * @param descriptionWithActors
	 * @param tvRating
	 * @param sexRating
	 * @param violenceRating
	 * @param languageRating
	 * @param dialogRating
	 * @param fvRating
	 * @param enhanced
	 * @param hdtv
	 * @param programId
	 */
	public Program(String programTitle, String reducedTitle40, String episodeTitle, String description, 
			String descriptionWithActors, double starRating, String tvRating, 
			boolean sexRating, boolean violenceRating, boolean languageRating, boolean dialogRating, 
			boolean fvRating, boolean enhanced, boolean hdtv, String programId, String genreDescription, 
			List<Credit> credits, Date originalAirDate, int runTime) {
		super(programId, programTitle, episodeTitle);
		this.reducedTitle40 = reducedTitle40;
		this.description = description;
		this.descriptionWithActors = descriptionWithActors;
		this.starRating = starRating;
		this.tvRating = tvRating;
		this.sexRating = sexRating;
		this.violenceRating = violenceRating;
		this.languageRating = languageRating;
		this.dialogRating = dialogRating;
		this.fvRating = fvRating;
		this.enhanced = enhanced;
		this.hdtv = hdtv;
		this.genreDescription = genreDescription;
		this.credits = credits;
		this.originalAirDate = originalAirDate;
		this.runTime = runTime;
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
	 * @return Returns the descriptionWithActors.
	 */
	public String getDescriptionWithActors() {
		return descriptionWithActors;
	}

	/**
	 * @param descriptionWithActors The descriptionWithActors to set.
	 */
	public void setDescriptionWithActors(String descriptionWithActors) {
		this.descriptionWithActors = descriptionWithActors;
	}

	/**
	 * @return Returns the dialogRating.
	 */
	public boolean isDialogRating() {
		return dialogRating;
	}

	/**
	 * @param dialogRating The dialogRating to set.
	 */
	public void setDialogRating(boolean dialogRating) {
		this.dialogRating = dialogRating;
	}

	/**
	 * @return Returns the enhanced.
	 */
	public boolean isEnhanced() {
		return enhanced;
	}

	/**
	 * @param enhanced The enhanced to set.
	 */
	public void setEnhanced(boolean enhanced) {
		this.enhanced = enhanced;
	}

	/**
	 * @return Returns the fvRating.
	 */
	public boolean isFvRating() {
		return fvRating;
	}

	/**
	 * @param fvRating The fvRating to set.
	 */
	public void setFvRating(boolean fvRating) {
		this.fvRating = fvRating;
	}

	/**
	 * @return Returns the hdtv.
	 */
	public boolean isHdtv() {
		return hdtv;
	}

	/**
	 * @param hdtv The hdtv to set.
	 */
	public void setHdtv(boolean hdtv) {
		this.hdtv = hdtv;
	}

	/**
	 * @return Returns the languageRating.
	 */
	public boolean isLanguageRating() {
		return languageRating;
	}

	/**
	 * @param languageRating The languageRating to set.
	 */
	public void setLanguageRating(boolean languageRating) {
		this.languageRating = languageRating;
	}

	/**
	 * @return a version of the title with a max 40 characters (humans may have
	 * reduced it intelligently, rather than simple truncation)
	 */
	public String getReducedTitle40() {
		return reducedTitle40;
	}

	/**
	 * @param reducedTitle40 Reduced if necessary to a max 40 characters
	 */
	public void setReducedTitle40(String reducedTitle40) {
		this.reducedTitle40 = reducedTitle40;
	}

	/**
	 * @return Returns the sexRating.
	 */
	public boolean isSexRating() {
		return sexRating;
	}

	/**
	 * @param sexRating The sexRating to set.
	 */
	public void setSexRating(boolean sexRating) {
		this.sexRating = sexRating;
	}

	/**
	 * @return Returns the tvRating.
	 */
	public String getTvRating() {
		return tvRating;
	}

	/**
	 * @param tvRating The tvRating to set.
	 */
	public void setTvRating(String tvRating) {
		this.tvRating = tvRating;
	}

	/**
	 * @return Returns the violenceRating.
	 */
	public boolean isViolenceRating() {
		return violenceRating;
	}

	/**
	 * @param violenceRating The violenceRating to set.
	 */
	public void setViolenceRating(boolean violenceRating) {
		this.violenceRating = violenceRating;
	}

	/**
	 * @return Returns the starRating.
	 */
	public double getStarRating() {
		return starRating;
	}

	/**
	 * @param starRating The starRating to set.
	 */
	public void setStarRating(double starRating) {
		this.starRating = starRating;
	}

	public void addCredit(Credit credit) {
		credits.add(credit);
	}
	/**
	 * @return Returns the credits.
	 */
	public List<Credit> getCredits() {
		return credits;
	}

	/**
	 * @param credits The credits to set.
	 */
	public void setCredits(List<Credit> credits) {
		this.credits = credits;
	}

	/**
	 * @return Returns the genreDescription.
	 */
	public String getGenreDescription() {
		return genreDescription;
	}

	/**
	 * @param genreDescription The genreDescription to set.
	 */
	public void setGenreDescription(String genreDescription) {
		this.genreDescription = genreDescription;
	}

	/**
	 * @return Returns the originalAirDate.
	 */
	public Date getOriginalAirDate() {
		return originalAirDate;
	}

	/**
	 * @param originalAirDate The originalAirDate to set.
	 */
	public void setOriginalAirDate(Date originalAirDate) {
		this.originalAirDate = originalAirDate;
	}

	/**
	 * @return Returns the runTime.
	 */
	public int getRunTime() {
		return runTime;
	}

	/**
	 * @param runTime The runTime to set.
	 */
	public void setRunTime(int runTime) {
		this.runTime = runTime;
	}

	/**
	 * @return Returns the lastModified.
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * @param lastModified The lastModified to set.
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
}

