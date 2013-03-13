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
import java.util.Date;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.appeligo.epg.util.EpgUtils;
import com.knowbout.epg.service.ProgramBase;
import com.knowbout.epg.service.ProgramType;
import com.knowbout.hibernate.HibernateUtil;
import com.knowbout.hibernate.TransactionManager;

public class Program extends ProgramBase {
	
	private static final Log log = LogFactory.getLog(Program.class);
	
	private String reducedTitle70;
	private String reducedTitle40;
	private String reducedTitle20;
	private String reducedTitle10;
	private String altTitle;
	private String reducedDescription120;
	private String reducedDescription60;
	private String reducedDescription40;
	private String adultSituationsAdvisory;
	private String graphicLanguageAdvisory;
	private String briefNudityAdvisory;
	private String graphicViolenceAdvisory;
	private String sscAdvisory;
	private String rapeAdvisory;
	private String genreDescription;
	private String description;
	private Date year;
	private String mpaaRating;
	private float starRating;
	private int runTime;
	private String colorCode;
	private String programLanguage;
	private String orgCountry;
	private boolean madeForTv;
	private String sourceType;
	private String showType;
	private String holiday;
	private String synEpiNum;
	private String altSynEpiNum;
	private String netSynSource;
	private String netSynType;
	private String descriptionActors;
	private String reducedDescriptionActors;
	private String orgStudio;
	private Date gameDate;
	private Date gameTime;
	private String gameTimeZone;
	private Date orginalAirDate;
	private String uniqueId;
	private Set<Credit> credits;
	private Date lastModified;
	//private int programType;
	
	public Program() {	
	}
	
	/**
	 * @return Returns the adultSituationsAdvisory.
	 */
	public String getAdultSituationsAdvisory() {
		return adultSituationsAdvisory;
	}
	/**
	 * @param adultSituationsAdvisory The adultSituationsAdvisory to set.
	 */
	public void setAdultSituationsAdvisory(String adultSituationsAdvisory) {
		this.adultSituationsAdvisory = adultSituationsAdvisory;
	}
	/**
	 * @return Returns the altSynEpiNum.
	 */
	public String getAltSynEpiNum() {
		return altSynEpiNum;
	}
	/**
	 * @param altSynEpiNum The altSynEpiNum to set.
	 */
	public void setAltSynEpiNum(String altSynEpiNum) {
		this.altSynEpiNum = altSynEpiNum;
	}
	/**
	 * @return Returns the altTitle.
	 */
	public String getAltTitle() {
		return altTitle;
	}
	/**
	 * @param altTitle The altTitle to set.
	 */
	public void setAltTitle(String altTitle) {
		this.altTitle = altTitle;
	}
	/**
	 * @return Returns the briefNudityAdvisory.
	 */
	public String getBriefNudityAdvisory() {
		return briefNudityAdvisory;
	}
	/**
	 * @param briefNudityAdvisory The briefNudityAdvisory to set.
	 */
	public void setBriefNudityAdvisory(String briefNudityAdvisory) {
		this.briefNudityAdvisory = briefNudityAdvisory;
	}
	/**
	 * @return Returns the colorCode.
	 */
	public String getColorCode() {
		return colorCode;
	}
	/**
	 * @param colorCode The colorCode to set.
	 */
	public void setColorCode(String colorCode) {
		this.colorCode = colorCode;
	}
	/**
	 * @return Returns the credits.
	 */
	public Set<Credit> getCredits() {
		if (credits == null) {
			credits = new HashSet<Credit>();
		}
		return credits;
	}
	/**
	 * @param credits The credits to set.
	 */
	public void setCredits(Set<Credit> credits) {
		this.credits = credits;
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
	 * @return Returns the descriptionActors.
	 */
	public String getDescriptionActors() {
		return descriptionActors;
	}
	/**
	 * @param descriptionActors The descriptionActors to set.
	 */
	public void setDescriptionActors(String descriptionActors) {
		this.descriptionActors = descriptionActors;
	}
	/**
	 * @return Returns the gameDate.
	 */
	public Date getGameDate() {
		return gameDate;
	}
	/**
	 * @param gameDate The gameDate to set.
	 */
	public void setGameDate(Date gameDate) {
		this.gameDate = gameDate;
	}
	/**
	 * @return Returns the gameTime.
	 */
	public Date getGameTime() {
		return gameTime;
	}
	/**
	 * @param gameTime The gameTime to set.
	 */
	public void setGameTime(Date gameTime) {
		this.gameTime = gameTime;
	}
	/**
	 * @return Returns the gameTimeZone.
	 */
	public String getGameTimeZone() {
		return gameTimeZone;
	}
	/**
	 * @param gameTimeZone The gameTimeZone to set.
	 */
	public void setGameTimeZone(String gameTimeZone) {
		this.gameTimeZone = gameTimeZone;
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
	 * @return Returns the graphicLanguageAdvisory.
	 */
	public String getGraphicLanguageAdvisory() {
		return graphicLanguageAdvisory;
	}
	/**
	 * @param graphicLanguageAdvisory The graphicLanguageAdvisory to set.
	 */
	public void setGraphicLanguageAdvisory(String graphicLanguageAdvisory) {
		this.graphicLanguageAdvisory = graphicLanguageAdvisory;
	}
	/**
	 * @return Returns the graphicViolenceAdvisory.
	 */
	public String getGraphicViolenceAdvisory() {
		return graphicViolenceAdvisory;
	}
	/**
	 * @param graphicViolenceAdvisory The graphicViolenceAdvisory to set.
	 */
	public void setGraphicViolenceAdvisory(String graphicViolenceAdvisory) {
		this.graphicViolenceAdvisory = graphicViolenceAdvisory;
	}
	/**
	 * @return Returns the holiday.
	 */
	public String getHoliday() {
		return holiday;
	}
	/**
	 * @param holiday The holiday to set.
	 */
	public void setHoliday(String holiday) {
		this.holiday = holiday;
	}
	/**
	 * @return Returns the madeForTv.
	 */
	public boolean isMadeForTv() {
		return madeForTv;
	}
	/**
	 * @param madeForTv The madeForTv to set.
	 */
	public void setMadeForTv(boolean madeForTv) {
		this.madeForTv = madeForTv;
	}
	/**
	 * @return Returns the mpaaRating.
	 */
	public String getMpaaRating() {
		return mpaaRating;
	}
	/**
	 * @param mpaaRating The mpaaRating to set.
	 */
	public void setMpaaRating(String mpaaRating) {
		this.mpaaRating = mpaaRating;
	}
	/**
	 * @return Returns the netSynSource.
	 */
	public String getNetSynSource() {
		return netSynSource;
	}
	/**
	 * @param netSynSource The netSynSource to set.
	 */
	public void setNetSynSource(String netSynSource) {
		this.netSynSource = netSynSource;
	}
	/**
	 * @return Returns the netSynType.
	 */
	public String getNetSynType() {
		return netSynType;
	}
	/**
	 * @param netSynType The netSynType to set.
	 */
	public void setNetSynType(String netSynType) {
		this.netSynType = netSynType;
	}
	/**
	 * @return Returns the orgCountry.
	 */
	public String getOrgCountry() {
		return orgCountry;
	}
	/**
	 * @param orgCountry The orgCountry to set.
	 */
	public void setOrgCountry(String orgCountry) {
		this.orgCountry = orgCountry;
	}
	/**
	 * @return Returns the orginalAirDate.
	 */
	public Date getOrginalAirDate() {
		return orginalAirDate;
	}
	/**
	 * @param orginalAirDate The orginalAirDate to set.
	 */
	public void setOrginalAirDate(Date orginalAirDate) {
		this.orginalAirDate = orginalAirDate;
	}
	/**
	 * @return Returns the orgStudio.
	 */
	public String getOrgStudio() {
		return orgStudio;
	}
	/**
	 * @param orgStudio The orgStudio to set.
	 */
	public void setOrgStudio(String orgStudio) {
		this.orgStudio = orgStudio;
	}
	/**
	 * @return Returns the programLanguage.
	 */
	public String getProgramLanguage() {
		return programLanguage;
	}
	/**
	 * @param programLanguage The programLanguage to set.
	 */
	public void setProgramLanguage(String programLanguage) {
		this.programLanguage = programLanguage;
	}
	/**
	 * @return Returns the rapeAdvisory.
	 */
	public String getRapeAdvisory() {
		return rapeAdvisory;
	}
	/**
	 * @param rapeAdvisory The rapeAdvisory to set.
	 */
	public void setRapeAdvisory(String rapeAdvisory) {
		this.rapeAdvisory = rapeAdvisory;
	}
	/**
	 * @return Returns the reducedDescription120.
	 */
	public String getReducedDescription120() {
		return reducedDescription120;
	}
	/**
	 * @param reducedDescription120 The reducedDescription120 to set.
	 */
	public void setReducedDescription120(String reducedDescription120) {
		this.reducedDescription120 = reducedDescription120;
	}
	/**
	 * @return Returns the reducedDescription40.
	 */
	public String getReducedDescription40() {
		return reducedDescription40;
	}
	/**
	 * @param reducedDescription40 The reducedDescription40 to set.
	 */
	public void setReducedDescription40(String reducedDescription40) {
		this.reducedDescription40 = reducedDescription40;
	}
	/**
	 * @return Returns the reducedDescription60.
	 */
	public String getReducedDescription60() {
		return reducedDescription60;
	}
	/**
	 * @param reducedDescription60 The reducedDescription60 to set.
	 */
	public void setReducedDescription60(String reducedDescription60) {
		this.reducedDescription60 = reducedDescription60;
	}
	/**
	 * @return Returns the reducedDescriptionActors.
	 */
	public String getReducedDescriptionActors() {
		return reducedDescriptionActors;
	}
	/**
	 * @param reducedDescriptionActors The reducedDescriptionActors to set.
	 */
	public void setReducedDescriptionActors(String reducedDescriptionActors) {
		this.reducedDescriptionActors = reducedDescriptionActors;
	}
	/**
	 * @return Returns the reducedTitle10.
	 */
	public String getReducedTitle10() {
		return reducedTitle10;
	}
	/**
	 * @param reducedTitle10 The reducedTitle10 to set.
	 */
	public void setReducedTitle10(String reducedTitle10) {
		this.reducedTitle10 = reducedTitle10;
	}
	/**
	 * @return Returns the reducedTitle20.
	 */
	public String getReducedTitle20() {
		return reducedTitle20;
	}
	/**
	 * @param reducedTitle20 The reducedTitle20 to set.
	 */
	public void setReducedTitle20(String reducedTitle20) {
		this.reducedTitle20 = reducedTitle20;
	}
	/**
	 * @return Returns the reducedTitle40.
	 */
	public String getReducedTitle40() {
		return reducedTitle40;
	}
	/**
	 * @param reducedTitle40 The reducedTitle40 to set.
	 */
	public void setReducedTitle40(String reducedTitle40) {
		this.reducedTitle40 = reducedTitle40;
	}
	/**
	 * @return Returns the reducedTitle70.
	 */
	public String getReducedTitle70() {
		return reducedTitle70;
	}
	/**
	 * @param reducedTitle70 The reducedTitle70 to set.
	 */
	public void setReducedTitle70(String reducedTitle70) {
		this.reducedTitle70 = reducedTitle70;
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
	 * @return Returns the showType.
	 */
	public String getShowType() {
		return showType;
	}
	/**
	 * @param showType The showType to set.
	 */
	public void setShowType(String showType) {
		this.showType = showType;
	}
	/**
	 * @return Returns the sourceType.
	 */
	public String getSourceType() {
		return sourceType;
	}
	/**
	 * @param sourceType The sourceType to set.
	 */
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}
	/**
	 * @return Returns the sscAdvisory.
	 */
	public String getSscAdvisory() {
		return sscAdvisory;
	}
	/**
	 * @param sscAdvisory The sscAdvisory to set.
	 */
	public void setSscAdvisory(String sscAdvisory) {
		this.sscAdvisory = sscAdvisory;
	}
	/**
	 * @return Returns the starRating.
	 */
	public float getStarRating() {
		return starRating;
	}
	/**
	 * @param starRating The starRating to set.
	 */
	public void setStarRating(float starRating) {
		this.starRating = starRating;
	}
	/**
	 * @return Returns the synEpiNum.
	 */
	public String getSynEpiNum() {
		return synEpiNum;
	}
	/**
	 * @param synEpiNum The synEpiNum to set.
	 */
	public void setSynEpiNum(String synEpiNum) {
		this.synEpiNum = synEpiNum;
	}
	/**
	 * @return Returns the uniqueId.
	 */
	public String getUniqueId() {
		return uniqueId;
	}
	/**
	 * @param uniqueId The uniqueId to set.
	 */
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	/**
	 * @return Returns the year.
	 */
	public Date getYear() {
		return year;
	}
	/**
	 * @param year The year to set.
	 */
	public void setYear(Date year) {
		this.year = year;
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
	
	public void insert() {
		Session session = HibernateUtil.currentSession();
		session.save(this);
	}
	
	public void delete() {
		Session session = HibernateUtil.currentSession();
		session.delete(this);
	}
	
	public static Program selectById(String programId) {
		Session session = HibernateUtil.currentSession();
		
		Program program = (Program)session.get(Program.class, programId);
		if (program == null && programId.startsWith("SH")) {
			program = createMissingShow(session, programId);
		}
		return program;
	}	
	
	@SuppressWarnings("unchecked")
	private static Program createMissingShow(Session session, String showId) {
		log.debug("Calling createMissingShow()");
		boolean commit = false;
		if (TransactionManager.currentTransaction() == null) {
			TransactionManager.beginTransaction();
			commit = true;
		}
		try {
    		Query query = session.getNamedQuery("Program.selectLikeId");
    		String programIdLikeEpisode = "EP"+showId.substring(2,10)+"%";
    		query.setString("programIdLike", programIdLikeEpisode);
    		query.setMaxResults(1);
    		List<Program> programs = query.list();
    		if (programs == null || programs.size() == 0) {
    			return null;
    		}
    		Program sample = programs.get(0);
    		Program show = new Program();
    		show.setAdultSituationsAdvisory(sample.getAdultSituationsAdvisory());
    		show.setAltSynEpiNum("");
    		show.setAltTitle("");
    		show.setBriefNudityAdvisory(sample.getBriefNudityAdvisory());
    		show.setColorCode(sample.getColorCode());
    		show.setDescription(truncateWithDots(255, "A series with a variety of episodes such as: "+sample.getDescription()));
    		show.setDescriptionActors(show.getDescription());
    		show.setGenreDescription(sample.getGenreDescription());
    		show.setGraphicLanguageAdvisory(sample.getGraphicLanguageAdvisory());
    		show.setGraphicViolenceAdvisory(sample.getGraphicViolenceAdvisory());
    		show.setHoliday("");
    		show.setProgramId(showId);
    		show.setLastModified(new Date());
    		show.setMadeForTv(sample.isMadeForTv());
    		show.setMpaaRating("");
    		show.setNetSynSource(sample.getNetSynSource());
    		show.setNetSynType(sample.getNetSynType());
    		show.setOrgCountry(sample.getOrgCountry());
    		show.setOrginalAirDate(null);
    		show.setOrgStudio(sample.getOrgStudio());
    		show.setProgramLanguage(sample.getProgramLanguage());
    		show.setRapeAdvisory(sample.getRapeAdvisory());
    		show.setReducedDescription120("A series with a variety of episodes.");
    		show.setReducedDescription60("A series with a variety of episodes.");
    		show.setReducedDescription40("A series with a variety of episodes.");
    		show.setReducedDescriptionActors("A series with a variety of episodes.");
    		show.setReducedTitle10(sample.getReducedTitle10());
    		show.setReducedTitle20(sample.getReducedTitle20());
    		show.setReducedTitle40(sample.getReducedTitle40());
    		show.setReducedTitle70(sample.getReducedTitle70());
    		show.setRunTime(sample.getRunTime());
    		show.setShowType(sample.getShowType());
    		show.setSourceType(sample.getSourceType());
    		show.setSscAdvisory(sample.getSscAdvisory());
    		show.setStarRating(sample.getStarRating());
    		show.setSynEpiNum(null);
    		show.setProgramTitle(sample.getProgramTitle());
    		show.setUniqueId("");
    		show.setYear(null);
    		show.insert();
    		if (commit) {
    			TransactionManager.commitTransaction();
    		}
    		
    		return show;
		} catch (Error e) {
			log.error("Could not create SHow", e);
			TransactionManager.rollbackTransaction();
			throw e;
		}
	}
	
	private static String truncateWithDots(int i, String string) {
        if (string.length() > i) {
            string = string.substring(0, i-3)+"...";
        }
        return string;
    }

    @SuppressWarnings("unchecked")
	private static Program createMissingTeam(Session session, String sportName, String teamName) {
		log.debug("Calling createMissingTeam()");
		boolean commit = false;
		if (TransactionManager.currentTransaction() == null) {
			TransactionManager.beginTransaction();
			commit = true;
		}
		try {
    		Query query = session.getNamedQuery("Program.selectProgramsWithTeam");
    		query.setString("programTitle", sportName);
    		String episodeTitleLike = "%"+teamName+"%";
    		query.setString("episodeTitleLike", episodeTitleLike);
    		query.setMaxResults(1);
    		List<Program> programs = query.list();
    		if (programs == null || programs.size() == 0) {
    			return null;
    		}
    		
    		query = session.createQuery("select MAX(programId) from Program");
    		String maxProgramId = (String)query.uniqueResult();
    		String teamId = null;
    		if (maxProgramId != null && maxProgramId.startsWith("TE")) {
                StringBuilder sb = new StringBuilder();
                Formatter formatter = new Formatter(sb, Locale.US);
                formatter.format("TE%08d0000", Integer.valueOf(maxProgramId.substring(2,10))+1);
                teamId = sb.toString();
    		} else {
    			teamId = "TE000000010000";
    		}
    		
    		Program sample = programs.get(0);
    		Program team = new Program();
    		team.setAdultSituationsAdvisory(sample.getAdultSituationsAdvisory());
    		team.setAltSynEpiNum("");
    		team.setAltTitle("");
    		team.setBriefNudityAdvisory(sample.getBriefNudityAdvisory());
    		team.setColorCode(sample.getColorCode());
    		team.setDescription("Games played by the "+teamName);
    		team.setDescriptionActors(team.getDescription());
    		team.setEpisodeTitle(sportName);
    		team.setGenreDescription(sample.getGenreDescription());
    		team.setGraphicLanguageAdvisory(sample.getGraphicLanguageAdvisory());
    		team.setGraphicViolenceAdvisory(sample.getGraphicViolenceAdvisory());
    		team.setHoliday("");
    		team.setProgramId(teamId);
    		team.setLastModified(new Date());
    		team.setMadeForTv(sample.isMadeForTv());
    		team.setMpaaRating("");
    		team.setNetSynSource(sample.getNetSynSource());
    		team.setNetSynType(sample.getNetSynType());
    		team.setOrgCountry(sample.getOrgCountry());
    		team.setOrginalAirDate(null);
    		team.setOrgStudio(sample.getOrgStudio());
    		team.setProgramLanguage(sample.getProgramLanguage());
    		team.setRapeAdvisory(sample.getRapeAdvisory());
    		team.setReducedDescription120(truncateWithDots(120, team.getDescription()));
    		team.setReducedDescription60("Games played by this team.");
    		team.setReducedDescription40("Games played by this team.");
    		team.setReducedDescriptionActors("Games played by this team.");
    		team.setReducedTitle10(teamName.length()>10?teamName.substring(0,10):teamName);
    		team.setReducedTitle20(teamName.length()>20?teamName.substring(0,20):teamName);
    		team.setReducedTitle40(teamName.length()>40?teamName.substring(0,40):teamName);
    		team.setReducedTitle70(teamName.length()>70?teamName.substring(0,70):teamName);
    		team.setRunTime(sample.getRunTime());
    		team.setShowType(sample.getShowType());
    		team.setSourceType(sample.getSourceType());
    		team.setSscAdvisory(sample.getSscAdvisory());
    		team.setStarRating(sample.getStarRating());
    		team.setSynEpiNum(null);
    		team.setProgramTitle(teamName);
    		team.setUniqueId("");
    		team.setYear(null);
    		team.insert();
			if (commit) {
				TransactionManager.commitTransaction();
			}
    		
    		return team;
		} catch (Error e) {
			log.error("Could not create TEam", e);
			TransactionManager.rollbackTransaction();
			throw e;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Program selectShowById(String programId, int limit) {
		Session session = HibernateUtil.currentSession();
		String showId = com.knowbout.epg.service.Program.getShowId(programId);
		Program show = (Program)session.get(Program.class, showId);
		if (show == null) {
			if (showId.startsWith("SH")) { 
				// If we started with anything other that SH or EP types, this will be false
				show = createMissingShow(session, showId);
			}
		}
		return show;
	}
	
	@SuppressWarnings("unchecked")
	public static Program selectByTeam(String sportName, String teamName) {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Program.selectByTeam"); 
		query.setString("programTitle", teamName);
		query.setString("episodeTitle", sportName);
		Program program = (Program)query.uniqueResult();
		if (program == null) {
			program = createMissingTeam(session, sportName, teamName);
		}
		return program;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Program> selectByModificationDate(Date date) {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Program.selectAfterModifiedDate");
		query.setTimestamp("date", date);
		List<Program> list = query.list();
		return list;		

	}

	public static ScrollableResults selectAllShowsMoviesSports() {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Program.selectAllShowsMoviesSports");
		ScrollableResults scroll = query.scroll(ScrollMode.FORWARD_ONLY);
		return scroll;
	}
	
	public static ScrollableResults selectAllTeams() {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Program.selectAllTeams");
		ScrollableResults scroll = query.scroll(ScrollMode.FORWARD_ONLY);
		return scroll;
	}
}

	