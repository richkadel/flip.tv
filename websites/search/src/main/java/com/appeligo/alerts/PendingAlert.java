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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.appeligo.search.entity.Permissions;
import com.appeligo.search.entity.User;
import com.appeligo.search.util.ChunkedResults;
import com.knowbout.epg.service.ScheduledProgram;
import com.knowbout.hibernate.model.PersistentObject;

public class PendingAlert extends PersistentObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5882171182431638042L;
	
	private static final Log log = LogFactory.getLog(PendingAlert.class);
	
	private long id;
	private ProgramAlert programAlert;
	private long userId;
	private Date alertTime;
	private String callSign;
	private String programId;
	private Date programStartTime;
	private ScheduledProgram scheduledProgram;
	private boolean manual;
	private boolean fired;
	private boolean deleted;
	private User user;
	
	public Date getAlertTime() {
		return alertTime;
	}

	public void setAlertTime(Date alertTime) {
		this.alertTime = alertTime;
	}

	public ProgramAlert getProgramAlert() {
		return programAlert;
	}

	public long getId() {
		return id;
	}

	public void setId(long pendingId) {
		this.id = pendingId;
	}

	public Date getProgramStartTime() {
		return programStartTime;
	}

	public void setProgramStartTime(Date programStartTime) {
		this.programStartTime = programStartTime;
	}

	public String getCallSign() {
		return callSign;
	}

	public void setCallSign(String callSign) {
		this.callSign = callSign;
	}

	public static void deleteAllMarkedDeleted() {
    	Permissions.checkUser(Permissions.SUPERUSER);
		Session session = getSession();
		Query query = session.getNamedQuery("PendingAlert.deleteAllMarkedDeleted");
		query.executeUpdate();
	}
	
	public static void deleteOldFired() {
    	Permissions.checkUser(Permissions.SUPERUSER);
		Session session = getSession();
		Query query = session.getNamedQuery("PendingAlert.deleteOldFired");
		query.setTimestamp("currentTime", new Date());
		query.executeUpdate();
	}
	
	@SuppressWarnings("unchecked")
	public static List<PendingAlert> getManualAlertsForUser(User user) {
    	Permissions.checkUser(user);
		Session session = getSession();
		Query query = session.getNamedQuery("PendingAlert.getManualAlertsForUser");
		query.setLong("userId", user.getUserId());
		return query.list();
	}
	
	@SuppressWarnings("unchecked")
	public static List<PendingAlert> getExpiredAlerts() {
    	Permissions.checkUser(Permissions.SUPERUSER);
		Session session = getSession();
		Query query = session.getNamedQuery("PendingAlert.getExpiredAlerts");
		query.setTimestamp("currentTime", new Date());
		return query.list();
	}
	
	@SuppressWarnings("unchecked")
	public static PendingAlert getNextAlert() {
    	Permissions.checkUser(Permissions.SUPERUSER);
		Session session = getSession();
		Query query = session.getNamedQuery("PendingAlert.getNextAlerts");
		ScrollableResults results = query.scroll();
		results.beforeFirst();
		PendingAlert pendingAlert = null;
		if (results.next()) {
			pendingAlert = (PendingAlert)results.get(0);
		}
		results.close();
		return pendingAlert;
	}

	public void setProgramAlert(ProgramAlert programAlert) {
		this.programAlert = programAlert;
	}

	public ScheduledProgram getScheduledProgram() {
		if (scheduledProgram == null) {
			scheduledProgram =
				AlertManager.getInstance().getEpg().
					getScheduledProgramByNetworkCallSign(getUser().getLineupId(), callSign, programStartTime);
		}
		return scheduledProgram;
	}
	
	public User getUser() {
		if (user == null) {
			user = User.findById(userId);
		}
		return user;
	}

	public boolean equals(Object rhs) {
		if (rhs == this) {
			return true;
		}
		if (!(rhs instanceof PendingAlert)) {
			return false;
		}
		PendingAlert other = (PendingAlert)rhs;
		if (other.id == id) {
			return true;
		}
		if ((other.userId == userId) &&
			(other.alertTime.equals(alertTime)) &&
			(other.callSign.equals(callSign)) &&
			(other.programStartTime.getTime() == programStartTime.getTime()) &&
			(other.fired == fired) &&
			(other.deleted == deleted)) {
			return true;
		}
		return false;
	}

	public int hashCode() {
		return (int)((userId % Integer.MAX_VALUE) | alertTime.hashCode());
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isFired() {
		return fired;
	}

	public void setFired(boolean fired) {
		this.fired = fired;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public static void markDeletedForProgramAlert(ProgramAlert programAlert) {
    	Permissions.checkUser(programAlert.getUser());
		Session session = getSession();
		Query query = session.getNamedQuery("PendingAlert.markDeletedForProgramAlert");
		query.setEntity("programAlert", programAlert);
		query.executeUpdate();
	}

	public boolean isManual() {
		return manual;
	}

	public void setManual(boolean manual) {
		this.manual = manual;
	}

	public String getProgramId() {
		return programId;
	}

	public void setProgramId(String programId) {
		this.programId = programId;
	}
	
	/* The join on update won't work
	public static void markDeletedWhereProgramAlertMarked() {
    	Permissions.checkUser(Permissions.SUPERUSER);
		Session session = getSession();
		Query query = session.getNamedQuery("PendingAlert.markDeletedWhereProgramAlertMarked");
		query.executeUpdate();
	}
	*/
}
