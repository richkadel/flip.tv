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

package com.appeligo.alerts;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.appeligo.epg.util.EpgUtils;
import com.appeligo.search.entity.Permissions;
import com.appeligo.search.entity.User;
import com.appeligo.search.util.ChunkedResults;
import com.knowbout.epg.service.Program;
import com.knowbout.hibernate.DeleteHandler;
import com.knowbout.hibernate.HibernateUtil;
import com.knowbout.hibernate.model.PersistentObject;

public class ProgramAlert extends PersistentObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4569956530755235910L;
	
	private static final Log log = LogFactory.getLog(ProgramAlert.class);
	
	private User user;
	private KeywordAlert originatingKeywordAlert;
	private String programId;
	//private String programTitle;
	//private String sportName;
	//private String teamName;
	private boolean newEpisodes;
	private Set<PendingAlert> pendingAlerts = new HashSet<PendingAlert>();
	private Program program;
	private boolean disabled;
	private boolean deleted;
	private long id;
	protected int alertMinutes;
	protected boolean usingPrimaryEmail;
	protected boolean usingAlternateEmail;
	protected boolean usingSMS;
	protected boolean usingIM;
	protected Date lastModified = new Date();
	protected Date creationTime = new Date();

	public int getAlertMinutes() {
		return alertMinutes;
	}

	public void setAlertMinutes(int alertMinutes) {
		this.alertMinutes = alertMinutes;
	}

	public long getId() {
		return id;
	}

	public void setId(long alertId) {
		this.id = alertId;
	}

	public boolean isUsingAlternateEmail() {
		return usingAlternateEmail;
	}

	public void setUsingAlternateEmail(boolean usingAlternateEmail) {
		this.usingAlternateEmail = usingAlternateEmail;
	}

	public boolean isUsingIM() {
		return usingIM;
	}

	public void setUsingIM(boolean usingIM) {
		this.usingIM = usingIM;
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

	public Date getLastModified() {
		if (lastModified == null) {
			return getCreationTime();
		}
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationDate) {
		this.creationTime = creationDate;
	}
	
	public Program getProgram() {
		if (program == null) {
			if (programId != null) {
				program = AlertManager.getInstance().getEpg().getProgram(programId);
			}
		}
		return program;
	}
	
	public String getProgramId() {
		return programId;
	}

	public void setProgramId(String programId) {
		this.programId = programId;
		/*
		if (programId != null) {
			setProgramTitle(getProgram().getProgramTitle());
		}
		*/
	}

	public User getUser() {
		return user;
	}

	public Set<PendingAlert> getPendingAlerts() {
		return pendingAlerts;
	}
	
	public Set<PendingAlert> getLivePendingAlerts() {
		Set<PendingAlert> pendingAlerts = getPendingAlerts();
		Iterator<PendingAlert> i = pendingAlerts.iterator();
		while (i.hasNext()) {
			PendingAlert pendingAlert = i.next();
			if (pendingAlert.isDeleted()) {
				i.remove();
			}
		}
		return pendingAlerts;
	}

	public void setPendingAlerts(Set<PendingAlert> pendingAlerts) {
		this.pendingAlerts = pendingAlerts;
	}

	@SuppressWarnings("unchecked")
	public static ChunkedResults<ProgramAlert> getAll() {
    	Permissions.checkUser(Permissions.SUPERUSER);
		Session session = getSession();
		Query query = session.getNamedQuery("ProgramAlert.getAll");
		return new ChunkedResults<ProgramAlert>(query);
	}

	public static void markDeletedForProgram(User user, String programId) {
    	Permissions.checkUser(user);
		Session session = getSession();
		Query query = session.getNamedQuery("ProgramAlert.markDeletedForProgram");
		query.setEntity("user", user);
		query.setString("programId", programId);
		query.executeUpdate();
	}
	
	/*
	public static void markDeletedForProgramTitle(User user, String programTitle) {
    	Permissions.checkUser(user);
		Session session = getSession();
		Query query = session.getNamedQuery("ProgramAlert.markDeletedForProgramTitle");
		query.setEntity("user", user);
		query.setString("programTitle", programTitle);
		query.executeUpdate();
	}
	
	public static void markDeletedForTeam(User user, String sportName, String teamName) {
    	Permissions.checkUser(user);
		Session session = getSession();
		Query query = session.getNamedQuery("ProgramAlert.markDeletedForTeam");
		query.setEntity("user", user);
		query.setString("sportName", sportName);
		query.setString("teamName", teamName);
		query.executeUpdate();
	}
	*/
	
	@SuppressWarnings("unchecked")
	public static ProgramAlert getById(long id) {
    	ProgramAlert programAlert = (ProgramAlert)getSession().get(ProgramAlert.class, id);
    	Permissions.checkUser(programAlert.getUser());
    	return programAlert;
	}
	
	@SuppressWarnings("unchecked")
	public static void deleteAllMarkedDeleted() {
    	Permissions.checkUser(Permissions.SUPERUSER);
    	// PendingAlert.markDeletedWhereProgramAlertMarked(); the update join I'd like to do won't work
    	
    	List<ProgramAlert> programAlerts = getAllMarkedDeleted();
    	for (ProgramAlert programAlert : programAlerts) {
    		PendingAlert.markDeletedForProgramAlert(programAlert);
    	}
    	
		Session session = getSession();
		Query query = session.getNamedQuery("ProgramAlert.deleteAllMarkedDeleted");
		query.executeUpdate();
	}
	
	/*
	@SuppressWarnings("unchecked")
	public static List<ProgramAlert> getIfNullTitle() {
    	Permissions.checkUser(Permissions.SUPERUSER);
		Session session = getSession();
		Query query = session.getNamedQuery("ProgramAlert.getIfNullTitle");
		List<ProgramAlert> programAlerts = query.list();
		return programAlerts;
	}
	*/
	
	private static List<ProgramAlert> getAllMarkedDeleted() {
    	Permissions.checkUser(Permissions.SUPERUSER);
		Session session = getSession();
		Query query = session.getNamedQuery("ProgramAlert.getAllMarkedDeleted");
		List<ProgramAlert> programAlerts = query.list();
		return programAlerts;
	}

	@SuppressWarnings("unchecked")
	public static List<ProgramAlert> getByProgramId(User user, String programId) {
    	Permissions.checkUser(user);
		Session session = getSession();
		Query query = session.getNamedQuery("ProgramAlert.getByProgramIdForUser");
		query.setEntity("user", user);
		query.setString("programId", programId);
		List<ProgramAlert> programAlerts = query.list();
		return programAlerts;
	}
	
	/*
	@SuppressWarnings("unchecked")
	public static List<ProgramAlert> getByProgramTitle(User user, String programTitle) {
    	Permissions.checkUser(user);
		Session session = getSession();
		Query query = session.getNamedQuery("ProgramAlert.getByProgramTitleForUser");
		query.setEntity("user", user);
		query.setString("programTitle", programTitle);
		List<ProgramAlert> programAlerts = query.list();
		return programAlerts;
	}
	
	@SuppressWarnings("unchecked")
	public static List<ProgramAlert> getByTeam(User user, String sportName, String teamName) {
    	Permissions.checkUser(user);
		Session session = getSession();
		Query query = session.getNamedQuery("ProgramAlert.getByTeamForUser");
		query.setEntity("user", user);
		query.setString("sportName", sportName);
		query.setString("teamName", teamName);
		List<ProgramAlert> programAlerts = query.list();
		return programAlerts;
	}
	*/
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public KeywordAlert getOriginatingKeywordAlert() {
		return originatingKeywordAlert;
	}

	public void setOriginatingKeywordAlert(KeywordAlert originatingKeywordAlert) {
		this.originatingKeywordAlert = originatingKeywordAlert;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	@Override
	public boolean equals(Object rhs) {
		if (rhs == this) {
			return true;
		}
		if (!(rhs instanceof ProgramAlert)) {
			return false;
		}
		ProgramAlert other = (ProgramAlert)rhs;
		if (other.getId() == getId()) {
			return true;
		}
		if (other.alertMinutes		== alertMinutes &&
			other.usingPrimaryEmail	== usingPrimaryEmail &&
			other.usingAlternateEmail	== usingAlternateEmail &&
			other.usingSMS			== usingSMS &&
			other.usingIM				== usingIM &&
			((other.user == user) ||
				(other.user != null && user != null &&
					other.user.getUserId() == user.getUserId()))
		    && ((other.programId == programId) ||
				(other.programId != null && other.programId.equals(programId)))
				/*
		    && ((other.sportName == sportName) ||
				(other.sportName != null && other.sportName.equals(sportName)))
		    && ((other.teamName == teamName) ||
				(other.teamName != null && other.teamName.equals(teamName)))
				*/
			) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (int)(getId() % Integer.MAX_VALUE);
	}

	/*
	public String getProgramTitle() {
		return programTitle;
	}

	public void setProgramTitle(String programTitle) {
		this.programTitle = programTitle;
	}
	*/
	
	/*
	public String getTargetId() {
		if (programId != null) {
			return programId;
		} else {
			String targetId = EpgUtils.cleanUrlTitle(teamName)+"_"+EpgUtils.cleanUrlTitle(sportName);
			targetId = targetId.replaceAll("-","_");
			if (targetId.length() == 0) {
				targetId = "_";
			} else if (Character.isDigit(targetId.charAt(0))) {
				targetId = "_"+targetId;
			}
			return targetId;
		}
	}
	*/

	/*
	public String getLabel() {
		Program program = getProgram();
		if (program != null) {
			if (program.isEpisode()) {
				String episodeTitle = program.getEpisodeTitle();
				if (episodeTitle == null || episodeTitle.trim().length() == 0) {
					episodeTitle = "Unnamed Episode";
				}
				return program.getProgramTitle()+" ("+episodeTitle+")";
			} else {
    			return program.getProgramTitle();
			}
		} else {
			return sportName+" ("+teamName+")";
		}
	}
	*/

	public boolean isNewEpisodes() {
		return newEpisodes;
	}

	public void setNewEpisodes(boolean newEpisodes) {
		this.newEpisodes = newEpisodes;
	}

	/*
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
	*/
}

