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

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.appeligo.alerts.AlertManager;
import com.appeligo.alerts.PendingAlert;
import com.appeligo.alerts.ProgramAlert;
import com.appeligo.search.actions.ProgramGuide.ProgramEntry;
import com.appeligo.search.entity.User;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.Program;

public class ProgramAlertAction extends AlertAction {
    private static final long serialVersionUID = 7019890080969775786L;
    
	private static final Log log = LogFactory.getLog(ProgramAlertAction.class);
	private static final Logger queryLog = Logger.getLogger("fliptv.query");
   
    private long programAlertId;
    private String programId;
    private String sportName;
    private String teamName;
    private boolean newEpisodes;
    private String callSign;
    private long programStartTime;
    private String onOff;
    private String reminderType;

	private boolean teamAlerts;

    public String createProgramAlert() throws Exception {
    	
    	User user = getUser();
    	
		log.debug("Creating program alert for user "+user.getUsername()+" on program "+programId);
    	
		String rtn = checkInput();
		if (!rtn.equals(SUCCESS)) {
			return rtn;
		}

    	ProgramAlert programAlert = new ProgramAlert();
		setProgramAlert(programAlert);
		if (reminderType.equals("episode") || reminderType.equals("game") || reminderType.equals("team")) {
			programAlert.setProgramId(programId);
		} else if (reminderType.equals("series")){
			programId = Program.getShowId(programId);
			programAlert.setProgramId(programId);
			programAlert.setNewEpisodes(newEpisodes);
		} else if (reminderType.equals("awayTeam")) {
        	EPGProvider epg = AlertManager.getInstance().getEpg();
        	Program program = epg.getProgram(programId);
        	Program team = epg.getProgramForTeam(program.getSportName(), program.getAwayTeamName());
			programAlert.setProgramId(team.getProgramId());
		} else if (reminderType.equals("homeTeam")) {
        	EPGProvider epg = AlertManager.getInstance().getEpg();
        	Program program = epg.getProgram(programId);
        	Program team = epg.getProgramForTeam(program.getSportName(), program.getHomeTeamName());
			programAlert.setProgramId(team.getProgramId());
		} else {
			throw new Error("reminderType must be episode, series, game, team, awayTeam, or homeTeam");
		}
		programAlert.setUser(user);
		
    	Set<ProgramAlert> programAlerts = getUser().getLiveProgramAlerts();
    	
    	if (programAlerts.size() >= getUser().getMaxEntries()) {
			setReturnMessage("Sorry.  You've exceeded your quota.  Delete some old reminders or send us feedback if you think you need more.");
			log.fatal(getUsername()+" exceeded the maximum number of ProgramAlerts. maxEntries="
					+getUser().getMaxEntries());
			return INPUT;
    	}
    	
		for (ProgramAlert element : programAlerts) {
			if (element.equals(programAlert)) {
				setReturnMessage("A reminder already exists with these settings.");
				setReturnUrlExplicitely(getReturnUrl() + "&existingProgramAlertId=" + element.getId());
				return INPUT;
			}
		}
    	programAlerts.add(programAlert);
    	programAlert.save(); // to guarantee the id is set, needed for the web displays
    	StringBuilder sb = getCookieValue();
		sb.append("|reminder=");
		sb.append(reminderType);
		sb.append("|programId=");
		sb.append(programId);
		sb.append("|sportName=");
		sb.append(sportName);
		sb.append("|teamName=");
		sb.append(teamName);
		queryLog.info(sb.toString());
       
		AlertManager.getInstance().checkIfPending(programAlert);
		
        return SUCCESS;
    }

    public String changeProgramAlertDisabled() throws Exception {
    	ProgramAlert programAlert = ProgramAlert.getById(getProgramAlertId());
    	if (programAlert.isDisabled() != isDisabled()) {
	    	programAlert.setDisabled(isDisabled());
	    	if (isDisabled()) {
	    		for (PendingAlert pendingAlert : programAlert.getLivePendingAlerts()) {
	    			pendingAlert.setDeleted(true);
			    	pendingAlert.save();
	    		}
	    	}
	    	programAlert.save();
	    	if (!isDisabled()) {
    			AlertManager.getInstance().checkIfPending(programAlert);
	    	}
    	}
    	
        return SUCCESS;
    }
    
    public String deleteProgramAlert() throws Exception {
    	ProgramAlert programAlert = ProgramAlert.getById(getProgramAlertId());
    	Program program = programAlert.getProgram();
    	String label = program.getLabel();
    	/*
    	if (program != null) {
        	label = program.getProgramTitle();
	    	String episode = program.getEpisodeTitle();
	    	if (episode != null && episode.trim().length() > 0) {
	    		label += " ("+episode.trim()+")";
	    	}
    	} else {
    		label = programAlert.getSportName()+" ("+programAlert.getTeamName().trim()+")"; 
    	}
    	*/
    	programAlert.setDeleted(true);
    	programAlert.save();
		for (PendingAlert pendingAlert : programAlert.getLivePendingAlerts()) {
			pendingAlert.setDeleted(true);
	    	pendingAlert.save();
		}
    	if (program != null) {
			setReturnMessage("Deleted reminder for <a href=\""+getServletRequest().getContextPath()+program.getWebPath()+"\">"+label+"</a>.");
    	} else {
			setReturnMessage("Deleted reminder for "+label+".");
    	}
        return SUCCESS;
    }
    
    private String deleteAlerts(String programId, String describesProgram) throws Exception {
    	if (describesProgram == null) {
    		describesProgram = "";
    	} else if (!describesProgram.endsWith(" ")) {
    		describesProgram += " ";
    	}
    	Program program = AlertManager.getInstance().getEpg().getProgram(programId);
    	ProgramAlert.markDeletedForProgram(getUser(), programId);
		setReturnMessage("Deleted all reminders for "+describesProgram+
				"<a href=\""+getServletRequest().getContextPath()+program.getWebPath()+"\">"+
				program.getLabel()+"</a>.");
    	
        return SUCCESS;
    }
    
    public String deleteSeriesAlerts() throws Exception {
    	return deleteAlerts(programId, null);
    }
    
    //public String deleteEpisodeAlerts() throws Exception {
    public String deleteProgramAlerts() throws Exception {
    	if (teamAlerts) {
        //    public String deleteTeamAlerts() throws Exception {
        	EPGProvider epg = AlertManager.getInstance().getEpg();
        	Program game = epg.getProgram(programId);
        	Program awayTeam = epg.getProgramForTeam(game.getSportName(), game.getAwayTeamName());
        	Program homeTeam = epg.getProgramForTeam(game.getSportName(), game.getHomeTeamName());
    		setReturnMessage("Deleted all reminders for games including either the "+
    				"<a href=\""+getServletRequest().getContextPath()+awayTeam.getWebPath()+"\">"+
    				awayTeam.getLabel()+"</a>"+
    				" or the <a href=\""+getServletRequest().getContextPath()+homeTeam.getWebPath()+"\">"+
    				homeTeam.getLabel()+"</a>.");
        	return SUCCESS;
        //}
    	} else {
        	if (programId.startsWith("SP")) {
            	return deleteAlerts(programId, "this specific game of ");
        	} else if (programId.startsWith("TE")) {
            	return deleteAlerts(programId, "the team ");
        	} else if (programId.startsWith("SH")) {
            	return deleteAlerts(programId, "all airings or episodes of ");
        	} else if (programId.startsWith("MV")) {
            	return deleteAlerts(programId, "all airings of the movie ");
        	} else {
            	return deleteAlerts(programId, "this specific episode of ");
        	}
    	}
    }
    
    public String setScheduledAlert() throws Exception {
    	User user = getUser();
    	boolean on = onOff.trim().toLowerCase().equals("on");
    	
		Timestamp startTime = new Timestamp(getProgramStartTime());
		
		List<PendingAlert> pendingAlerts = PendingAlert.getManualAlertsForUser(user);
		
		for (PendingAlert pendingAlert : pendingAlerts) {
			if (pendingAlert.isManual() &&
				pendingAlert.getProgramId().equals(programId) &&
				pendingAlert.getCallSign().equals(callSign) &&
				pendingAlert.getProgramStartTime().getTime() == getProgramStartTime()) {
	
				if (!on) {
					pendingAlert.setDeleted(true);
					pendingAlert.save();
					return SUCCESS;
				} else {
					log.warn("Tried to turn on an existing scheduled alert!");
					return INPUT;
				}
			}
		}
		
		if (!on) {
			log.warn("Tried to turn off a non-existent scheduled alert!");
			return INPUT;
		}
		
		PendingAlert pendingAlert = new PendingAlert();
		pendingAlert.setManual(true);
		pendingAlert.setUserId(user.getUserId());
		pendingAlert.setProgramId(getProgramId());
		pendingAlert.setCallSign(getCallSign());
		pendingAlert.setProgramStartTime(startTime);
		
		Timestamp alertTime = new Timestamp(startTime.getTime() - (user.getAlertMinutesDefault() * 60000));
		
		pendingAlert.setAlertTime(alertTime);
		
		pendingAlert.save();
		
		setReturnUrl("/empty.vm");
    	
        return SUCCESS;
    }
    
	public String getReturnUrl() {
		if (returnUrl == null) {
			returnUrl = "/alerts/reminders.action";
		}
		return returnUrl;
	}

	public String getProgramId() {
		return programId;
	}

	public void setProgramId(String programId) {
		this.programId = programId;
	}

	public long getProgramAlertId() {
		return programAlertId;
	}

	public void setProgramAlertId(long programAlertId) {
		this.programAlertId = programAlertId;
	}

	public String getCallSign() {
		return callSign;
	}

	public void setCallSign(String callSign) {
		this.callSign = callSign;
	}

	public String getOnOff() {
		return onOff;
	}

	public void setOnOff(String onOff) {
		this.onOff = onOff;
	}

	public long getProgramStartTime() {
		return programStartTime;
	}

	public void setProgramStartTime(long programStartTime) {
		this.programStartTime = programStartTime;
	}

	public String getReminderType() {
		return reminderType;
	}

	public void setReminderType(String reminderType) {
		this.reminderType = reminderType;
	}

	public boolean isNewEpisodes() {
		return newEpisodes;
	}

	public void setNewEpisodes(boolean newEpisodes) {
		this.newEpisodes = newEpisodes;
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

	public boolean isTeamAlerts() {
		return teamAlerts;
	}

	public void setTeamAlerts(boolean teamAlerts) {
		this.teamAlerts = teamAlerts;
	}
}
