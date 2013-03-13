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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import com.appeligo.alerts.ProgramAlert;
import com.appeligo.epg.DefaultEpg;
import com.appeligo.epg.util.EpgUtils;
import com.appeligo.search.entity.Favorite;
import com.appeligo.search.entity.Rating;
import com.appeligo.search.entity.User;
import com.appeligo.search.util.ConfigUtils;
import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.Program;
import com.knowbout.epg.service.ScheduledProgram;

public class ProgramAction extends BaseAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6590911390662677436L;

	public static final String PROGRAM_NOT_FOUND = "programNotFound";
	public static final String REDIRECT = "redirect";
	private static final Logger log = Logger.getLogger(ProgramAction.class);
	private static final Logger queryLog = Logger.getLogger("fliptv.query");

	private static final Comparator<ProgramAlert> PROGRAM_ALERT_COMPARATOR =
		new Comparator<ProgramAlert>() {
			public int compare(ProgramAlert left, ProgramAlert right) {
				return 0-left.getLastModified().compareTo(right.getLastModified());
			}
    	};
    			    	
    private String programId;
    private String showId;
    private String sportName;
    private String teamName;
    private SearchResult searchResult;
    private int startIndex;
    private ScheduledProgram nextShowAiring;
    private ScheduledProgram nextAiring;
    private ScheduledProgram lastAiring;
    private Program programInfo;
    private String fragments;
    private ProgramGuide programGuide;
    private List<ProgramAlert> seriesAlerts;
    private List<ProgramAlert> episodeAlerts;
    private List<ProgramAlert> teamAlerts;
    private String message;
    private boolean fromPath;
    private boolean oneResult;
    private boolean valid = true;
    private boolean autoDeleteReminders;
    private int prewarn;
    private String prewarnUnits;
    private boolean usingPrimaryEmail;
    private boolean usingSMS;
    private List<String> previewURLs;
    
    private Favorite favoriteShow;
    private Favorite favoriteEpisode;
    private Rating episodeRating;
    private Rating showRating;
    private boolean episodeOnly;
    private Program homeTeam;
    private Program awayTeam;
    
    public String execute() throws Exception {    	    	
        if (programId != null && programId.length()  > 0) {
        	StringBuilder sb = getCookieValue();
    		sb.append("|query=");
    		String query = getQuery();
    		query = query == null ? query : query.replace("|","\\|");
    		sb.append(query);
    		sb.append("|programId=");
    		sb.append(programId);
        	queryLog.info(sb.toString());
    		SearchResults results = (SearchResults)getSession().get(CaptionSearchAction.KEY);
        	//I'm doing the seach again in case the session expired, but they book marked the page.
        	//This way it should still work, unless program falls on a different page. 
        	//TODO: Think about how to fix that!
	    	if (results != null) {
	    		searchResult = results.getSearchResult(programId); 
	    		if (searchResult != null) {
	    			fragments = searchResult.getDoc().getFragments();
	    		}
	    	} else {
	    		log.debug("SearchResults were not in the session.  Either an old session or it is not coming from a search page");
	    	}
			Configuration config = ConfigUtils.getSystemConfig();
	    	EPGProvider epgProvider = DefaultEpg.getInstance();
    		String lineup = getLineup();
    		
    		//Pull the data for the program from the EPG.
			programInfo = epgProvider.getProgram(programId);
			
			if (!fromPath) {
				return REDIRECT;
			}
			nextAiring = epgProvider.getNextShowing(lineup, programId, false, true);
			lastAiring = epgProvider.getLastShowing(lineup, programId);
			if (programInfo.isSports()) {
    			awayTeam = epgProvider.getProgramForTeam(programInfo.getSportName(), programInfo.getAwayTeamName());
    			homeTeam = epgProvider.getProgramForTeam(programInfo.getSportName(), programInfo.getHomeTeamName());
			}
	    	if (getUser() != null) {
	    		if (programInfo.isEpisode() || programInfo.isSports()) {
    		    	episodeAlerts = ProgramAlert.getByProgramId(getUser(), programId);
    		    	Collections.sort(episodeAlerts, PROGRAM_ALERT_COMPARATOR);
	    		}
    			if (programInfo.isSports()) {
    		    	teamAlerts = new LinkedList<ProgramAlert>(
    		    			ProgramAlert.getByProgramId(getUser(), awayTeam.getProgramId()));
    		    	teamAlerts.addAll(
    		    			ProgramAlert.getByProgramId(getUser(), homeTeam.getProgramId()));
    		    	Collections.sort(teamAlerts, PROGRAM_ALERT_COMPARATOR);
	    		} else {
    		    	seriesAlerts = ProgramAlert.getByProgramId(getUser(), programInfo.getShowId());
    		    	Collections.sort(seriesAlerts, PROGRAM_ALERT_COMPARATOR);
	    		}
	    	}
	    	
	    	String previewUrlRoot = "/previews";
	    	String documentRoot = config.getString("documentRoot");
	    	
    		String urlPath = EpgUtils.getPreviewDir(previewUrlRoot, programInfo);
    		String directory = documentRoot+urlPath;
	    	String titleFileName = directory+"/"+programInfo.getProgramId()+".title";
	    	/*
log.warn("previewUrlRoot="+previewUrlRoot);
log.warn("urlPath="+urlPath);
log.warn("documentRoot="+documentRoot);
log.warn("directory="+directory);
log.warn("previewUrlPrefix="+previewUrlPrefix);
log.warn("previewFilePrefix="+previewFilePrefix);
*/
	    	File titleFile = new File(titleFileName);
	    	if (titleFile.exists()) {
	    		File dirFile = new File(directory);
	    		String[] filenames = dirFile.list(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						if (name.startsWith(programInfo.getProgramId()) && name.endsWith(".flv")) {
							return true;
						}
						return false;
					}
	    		});
	    		if (filenames.length > 0) {
    	    		Arrays.sort(filenames);
    	    		previewURLs = new ArrayList<String>();
    	    		for (int i = 0; i < filenames.length; i++) {
        	    		previewURLs.add(urlPath+"/"+filenames[i]);
    	    		}
	    		} else {
	    			previewURLs = null;
	    		}
	    	}

    		if (programInfo != null) {
	    		Calendar cal = Calendar.getInstance();
	    		cal.add(Calendar.DATE, 14);
		    	List<ScheduledProgram> schedule;
        			schedule = epgProvider.getNextShowings(lineup,
        								programInfo.getShowId(), false, true);
		    	programGuide = new ProgramGuide(getUser(), getTimeZone(), schedule);
		    	if (schedule.size() > 0) {
		    		nextShowAiring = schedule.get(0);
		    	}
    		} else {
	    		log.debug("Unable to find Program in EPG for :" +programId+ " SearchResult was found, i.e. it came from search: " + (searchResult != null));

    			//This happens when we have data in the lucene index that is not in the epg.
    			//This typically means the epg database got broken somehow, or a restore did 
    			//Not happen. It is unlikely, but catch it and report an error instead.
    			return PROGRAM_NOT_FOUND;
    		}
        }
        
		User user = getUser();
		if (user != null) {
			int alertMinutes = user.getAlertMinutesDefault();
			if (alertMinutes < 120) {
				setPrewarn(alertMinutes);
				setPrewarnUnits("Minutes");
			} else if (alertMinutes < 48 * 60) {
				setPrewarn(alertMinutes / 60);
				setPrewarnUnits("Hours");
			} else {
				setPrewarn(alertMinutes / 24 * 60);
				setPrewarnUnits("Days");
			}
			setUsingPrimaryEmail(user.isUsingPrimaryEmailDefault());
			setUsingSMS(user.isUsingSMSDefault());
		} else {
			setPrewarn(15);
			setPrewarnUnits("Minutes");
			setUsingPrimaryEmail(true);
		}
        
		if (user != null) {
			if (programInfo.isEpisode()) {
    			favoriteEpisode = Favorite.findFavoriteProgram(user, programInfo.getProgramId());
    			episodeRating = Rating.findProgramRating(user, programInfo.getProgramId());
			}
			favoriteShow = Favorite.findFavoriteProgram(user, programInfo.getShowId());
			showRating = Rating.findProgramRating(user, programInfo.getShowId());			
		}
        return SUCCESS;
    }
    
	public StringBuffer getRequestURL() {
		StringBuffer requestURL = getServletRequest().getRequestURL();
		int slashes = requestURL.indexOf("//");
		int nextSlash = requestURL.indexOf("/", slashes+2);
		requestURL.delete(nextSlash, requestURL.length());
		requestURL.append(programInfo.getWebPath());
		return requestURL;
	}
    
	public String getFullRequestURL() {
		StringBuffer url = getRequestURL();
		String queryString = getServletRequest().getQueryString();
		if (queryString != null && queryString.length() > 0) {
			queryString = queryString.replaceAll("programId=.{14}&*", "");
			queryString = queryString.replaceAll("fromPath=true&*", "").trim();
			if (queryString.length() > 0 && queryString.charAt(0) != '?') {
				queryString = "?"+queryString;
			}
			url.append(queryString);
		}
		return url.toString();
	}
	
    public String setAlertDefaults() throws Exception {
    	String rtn = SUCCESS;
    	
		if (getPrewarn() > 999) {
			setMessage("Pre-warning must be less than 1000 "+getPrewarnUnits()+".");
			rtn = INPUT;
		}
		
		int alertMinutes = 0;
		if (getPrewarnUnits().equals("Minutes")) {
			alertMinutes = getPrewarn();
		} else if (getPrewarnUnits().equals("Hours")) {
			alertMinutes = getPrewarn() * 60;
		} else if (getPrewarnUnits().equals("Days")) {
			alertMinutes = getPrewarn() * 60 * 24;
		} else {
			setMessage("Pre-warning can be in Minutes, Hours, or Days.");
			rtn = INPUT;
		}
		
		if (!isUsingPrimaryEmail() && !isUsingSMS()) {
			setMessage("You must select at least one method for receiving reminders.");
			rtn = INPUT;
		}
		
		User user = getUser();
		if (user == null) {
			throw new Error("No user! We shouldn't be able to get here without one.");
		}
		
		user.setAlertMinutesDefault(alertMinutes);
		user.setUsingPrimaryEmailDefault(usingPrimaryEmail);
		user.setUsingSMSDefault(usingSMS);
		user.save();
		
    	execute();
    	
		return rtn;
    }

    public String setFavorite() throws Exception {
    	User user = getUser();
    	if (user != null) {
			Configuration config = ConfigUtils.getSystemConfig();
	    	EPGProvider epgProvider = DefaultEpg.getInstance();
	    	
    		//Pull the data for the program from the EPG.
			programInfo = epgProvider.getProgram(programId);
			Favorite favorite = null;
    		if (episodeOnly) {
    			favorite = Favorite.findFavoriteProgram(user, programInfo.getProgramId());
    		} else {
    			favorite = Favorite.findFavoriteProgram(user, programInfo.getShowId());
    		}
    		
			//If it doesn't exist, It is a favorite
			if (favorite == null) {
				favorite = new Favorite();
				favorite.setCreated(new Date());
				favorite.setLabel(programInfo.getLabel());
				Favorite top = null;
				if (episodeOnly) {
					favorite.setProgramId(programInfo.getProgramId());
					top = Favorite.getTopFavoriteEpisode(user);
				} else {
					favorite.setProgramId(programInfo.getShowId());
					top = Favorite.getTopFavoriteShow(user);					
				}
				if (top == null) {
					favorite.setRank(1.0);
				} else {
					favorite.setRank((top.getRank()/2.0));
				}
				
				favorite.setUser(user);
				favorite.insert();
			} else {
				//Toggle the delete state
				favorite.setDeleted(!favorite.isDeleted());
			}
    	}    	
    	execute();
    	return SUCCESS;
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
	 * @return Returns the nextShowAiring.
	 */
	public ScheduledProgram getNextShowAiring() {
		return nextShowAiring;
	}

	/**
	 * @param nextShowAiring The nextShowAiring to set.
	 */
	public void setNextShowAiring(ScheduledProgram nextShowAiring) {
		this.nextShowAiring = nextShowAiring;
	}

	/**
	 * @return Returns the startIndex.
	 */
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * @param startIndex The startIndex to set.
	 */
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public String getMessage() {
		if (message != null && isFromPath()) {
			// Might be a bug... This kludge sucks.  URLRewrite forward, with webwork, hoses things.
			// I think webwork has the bug.  URLRewrite seems right, but the forward doesn't work.
			int len = message.length();
			if (len > 2 && ((len % 2) == 0)) {
				int mid = len/2;
				// "test, test"
				// len == 10
				// mid == 5
				// front = "test" = substr(0,4)
				// back = "test" = substr(6,len)
				String front = message.substring(0, mid-1);
				String back = message.substring(mid+1, len);
				if (front.equals(back)) {
					return front;
				}
			}
		}
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return Returns the lastAiring.
	 */
	public ScheduledProgram getLastAiring() {
		return lastAiring;
	}

	/**
	 * @param lastAiring The lastAiring to set.
	 */
	public void setLastAiring(ScheduledProgram lastAiring) {
		this.lastAiring = lastAiring;
	}

	/**
	 * @return Returns the nextAiring.
	 */
	public ScheduledProgram getNextAiring() {
		return nextAiring;
	}

	/**
	 * @param nextAiring The nextAiring to set.
	 */
	public void setNextAiring(ScheduledProgram nextAiring) {
		this.nextAiring = nextAiring;
	}

	/**
	 * @return Returns the programInfo.
	 */
	public Program getProgramInfo() {
		return programInfo;
	}
	
	public String[] getGenres() {
		return programInfo.getGenreDescription().split(", ");
	}

	/**
	 * @param programInfo The programInfo to set.
	 */
	public void setProgramInfo(Program programInfo) {
		this.programInfo = programInfo;
	}

	/**
	 * @return Returns the fragments.
	 */
	public String getFragments() {
		return fragments;
	}

	/**
	 * @return Returns the fragments.
	 */
	public SearchResult getSearchResult() {
		return searchResult;
	}

	/**
	 * @param fragments The fragments to set.
	 */
	public void setFragments(String fragments) {
		this.fragments = fragments;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * @return Returns the programGuide.
	 */
	public ProgramGuide getProgramGuide() {
		return programGuide;
	}

	/**
	 * @param programGuide The programGuide to set.
	 */
	public void setProgramGuide(ProgramGuide programGuide) {
		this.programGuide = programGuide;
	}

	public boolean isAutoDeleteReminders() {
		return autoDeleteReminders;
	}

	public void setAutoDeleteReminders(boolean autoDeleteReminders) {
		this.autoDeleteReminders = autoDeleteReminders;
	}

	public int getPrewarn() {
		return prewarn;
	}

	public void setPrewarn(int prewarn) {
		this.prewarn = prewarn;
	}

	public String getPrewarnUnits() {
		return prewarnUnits;
	}

	public void setPrewarnUnits(String prewarnUnits) {
		this.prewarnUnits = prewarnUnits;
	}

	public boolean isUsingPrimaryEmail() {
		return usingPrimaryEmail;
	}

	public void setUsingPrimaryEmail(boolean usingPrimaryEmail) {
		this.usingPrimaryEmail = usingPrimaryEmail;
	}

	public boolean isUsingSMS() {
		return usingSMS;
	}

	public void setUsingSMS(boolean usingSMS) {
		this.usingSMS = usingSMS;
	}


	public List<ProgramAlert> getSeriesAlerts() {
		return seriesAlerts;
	}


	public void setSeriesAlerts(List<ProgramAlert> seriesAlerts) {
		this.seriesAlerts = seriesAlerts;
	}


	public List<ProgramAlert> getEpisodeAlerts() {
		return episodeAlerts;
	}


	public void setEpisodeAlerts(List<ProgramAlert> episodeAlerts) {
		this.episodeAlerts = episodeAlerts;
	}


	/**
	 * @return Returns the favoriteEpisode.
	 */
	public Favorite getFavoriteEpisode() {
		return favoriteEpisode;
	}


	/**
	 * @param favoriteEpisode The favoriteEpisode to set.
	 */
	public void setFavoriteEpisode(Favorite favoriteProgram) {
		this.favoriteEpisode = favoriteProgram;
	}


	/**
	 * @return Returns the favoriteShow.
	 */
	public Favorite getFavoriteShow() {
		return favoriteShow;
	}


	/**
	 * @param favoriteShow The favoriteShow to set.
	 */
	public void setFavoriteShow(Favorite favoriteShow) {
		this.favoriteShow = favoriteShow;
	}


	/**
	 * @return Returns the episodeRating.
	 */
	public Rating getEpisodeRating() {
		return episodeRating;
	}


	/**
	 * @param episodeRating The episodeRating to set.
	 */
	public void setEpisodeRating(Rating programRating) {
		this.episodeRating = programRating;
	}


	/**
	 * @return Returns the showRating.
	 */
	public Rating getShowRating() {
		return showRating;
	}


	/**
	 * @param showRating The showRating to set.
	 */
	public void setShowRating(Rating showRating) {
		this.showRating = showRating;
	}


	/**
	 * @return Returns the programOnly.
	 */
	public boolean isEpisodeOnly() {
		return episodeOnly;
	}


	/**
	 * @param programOnly The programOnly to set.
	 */
	public void setEpisodeOnly(boolean episodeOnly) {
		this.episodeOnly = episodeOnly;
	}

	/**
	 * @return Returns the showId.
	 */
	public String getShowId() {
		return showId;
	}

	/**
	 * @param showId The showId to set.
	 */
	public void setShowId(String showId) {
		this.showId = showId;
	}

	public List<String> getPreviewURLs() {
		return previewURLs;
	}

	public void setPreviewURLs(List<String> previewURLs) {
		this.previewURLs = previewURLs;
	}

	public List<ProgramAlert> getTeamAlerts() {
		return teamAlerts;
	}

	public void setTeamAlerts(List<ProgramAlert> teamAlerts) {
		this.teamAlerts = teamAlerts;
	}

	public String getSportName() {
		return sportName;
	}

	public void setSportName(String sportName) {
		this.sportName = sportName;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public Program getAwayTeam() {
		return awayTeam;
	}

	public void setAwayTeam(Program awayTeam) {
		this.awayTeam = awayTeam;
	}

	public Program getHomeTeam() {
		return homeTeam;
	}

	public void setHomeTeam(Program homeTeam) {
		this.homeTeam = homeTeam;
	}
	
	public Date getNow() {
		return new Date();
	}

	public boolean isFromPath() {
		return fromPath;
	}

	public void setfromPath(boolean fromPath) {
		this.fromPath = fromPath;
	}

	public String getWebPath() {
		return programInfo.getWebPath();
	}
	
	public String getQueryStringNoProgramId() {
		String queryString = getServletRequest().getQueryString();
		String[] params = queryString.split("&");
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (String param : params) {
			if (!param.startsWith("programId=")) {
				if (!first) {
					sb.append("&");
				}
    			sb.append(param);
				first = false;
			}
		}
		if (sb.length() > 0) {
			sb.insert(0, '?');
		}
		return sb.toString();
	}

	public boolean isOneResult() {
		return oneResult;
	}

	public void setOneResult(boolean oneResult) {
		this.oneResult = oneResult;
	}
}
