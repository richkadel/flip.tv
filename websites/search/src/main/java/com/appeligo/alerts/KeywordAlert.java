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
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;

import com.appeligo.search.entity.Permissions;
import com.appeligo.search.entity.User;
import com.appeligo.search.util.ChunkedResults;
import com.knowbout.hibernate.model.PersistentObject;

public class KeywordAlert extends PersistentObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4569956530755235910L;
	
	private static final Log log = LogFactory.getLog(KeywordAlert.class);
	
	private long id;
	private User user;
	private String userQuery;
	private String normalizedQuery;
	private boolean usingPrimaryEmailRealtime;
	private boolean usingAlternateEmailRealtime;
	private boolean usingSMSRealtime;
	private boolean usingIMRealtime;
	private Set<ProgramAlert> programAlerts = new HashSet<ProgramAlert>();
	private Date lastModified = new Date();
	private Date creationTime = new Date();
	private int maxAlertsPerDay;
	private int todaysAlertCount;
	private Date lastAlertDay;
	private boolean disabled;
	private boolean deleted;
	
	public String getUserQuery() {
		return userQuery;
	}

	public void setUserQuery(String userQuery) {
		this.userQuery = userQuery;
		normalizedQuery = normalizeQuery(userQuery);
	}
	
	public static String normalizeQuery(String query) {
		return query.trim().replaceAll("\\s+", " ").toLowerCase();
	}

	public User getUser() {
		return user;
	}

	public long getId() {
		return id;
	}

	public void setId(long alertId) {
		this.id = alertId;
	}

	public static KeywordAlert getById(long id) {
    	KeywordAlert keywordAlert = (KeywordAlert)getSession().get(KeywordAlert.class, id);
    	Permissions.checkUser(keywordAlert.getUser());
		return keywordAlert;
	}
	
	public static void deleteAllMarkedDeleted() {
    	Permissions.checkUser(Permissions.SUPERUSER);
		Session session = getSession();
		Query query = session.getNamedQuery("KeywordAlert.deleteAllMarkedDeleted");
		query.executeUpdate();
	}
	
	@SuppressWarnings("unchecked")
	public static KeywordAlert getByNormalizedQuery(User user, String query) {
    	Permissions.checkUser(user);
		Session session = getSession();
		Query hqlQuery = session.getNamedQuery("KeywordAlert.getByNormalizedQuery");
		hqlQuery.setLong("userId", user.getUserId());
		hqlQuery.setString("normalizedQuery", query);
		List<KeywordAlert> programAlerts = hqlQuery.list();
		if (programAlerts.size() > 0) {
			return programAlerts.get(0);
		} else {
			return null;
		}
	}
	
	/*
	public static ChunkedResults<KeywordAlert> getAllEnabledInNormalizedQueryOrder() {
		Session session = getSession();
		Query query = session.getNamedQuery("KeywordAlert.getAllEnabledInNormalizedQueryOrder");
		return new ChunkedResults<KeywordAlert>(query);
	}
	*/
	public static ChunkedResults<KeywordAlert> getAllInNormalizedQueryOrder() {
    	Permissions.checkUser(Permissions.SUPERUSER);
		Session session = getSession();
		Query query = session.getNamedQuery("KeywordAlert.getAllInNormalizedQueryOrder");
		query.setTimestamp("latestCreationTime", new Date());
		return new ChunkedResults<KeywordAlert>(query);
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	@Override
	public boolean equals(Object rhs) {
		if (rhs == this) {
			return true;
		}
		if (!(rhs instanceof KeywordAlert)) {
			return false;
		}
		KeywordAlert other = (KeywordAlert)rhs;
		if (other.getId() == getId()) {
			return true;
		}
		if (((other.user == user) ||
				(other.user != null && user != null &&
						other.user.getUserId() == user.getUserId())) &&
		    ((other.normalizedQuery == normalizedQuery) ||
				(other.normalizedQuery != null &&
					other.normalizedQuery.equals(normalizedQuery)))) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (int)(getId() % Integer.MAX_VALUE);
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

	public boolean isUsingAlternateEmailRealtime() {
		return usingAlternateEmailRealtime;
	}

	public void setUsingAlternateEmailRealtime(boolean usingAlternateEmailRealtime) {
		this.usingAlternateEmailRealtime = usingAlternateEmailRealtime;
	}

	public boolean isUsingIMRealtime() {
		return usingIMRealtime;
	}

	public void setUsingIMRealtime(boolean usingIMRealtime) {
		this.usingIMRealtime = usingIMRealtime;
	}

	public boolean isUsingPrimaryEmailRealtime() {
		return usingPrimaryEmailRealtime;
	}

	public void setUsingPrimaryEmailRealtime(boolean usingPrimaryEmailRealtime) {
		this.usingPrimaryEmailRealtime = usingPrimaryEmailRealtime;
	}

	public boolean isUsingSMSRealtime() {
		return usingSMSRealtime;
	}

	public void setUsingSMSRealtime(boolean usingSMSRealtime) {
		this.usingSMSRealtime = usingSMSRealtime;
	}

	public Set<ProgramAlert> getProgramAlerts() {
		return programAlerts;
	}

	public void setProgramAlerts(Set<ProgramAlert> programAlerts) {
		this.programAlerts = programAlerts;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public int getMaxAlertsPerDay() {
		return maxAlertsPerDay;
	}

	public void setMaxAlertsPerDay(int maxAlertsPerDay) {
		this.maxAlertsPerDay = maxAlertsPerDay;
	}

	public String getNormalizedQuery() {
		return normalizedQuery;
	}
	
	public void setNormalizedQuery(String normalizedQuery) {
		this.normalizedQuery = normalizedQuery;
	}

	public Date getLastAlertDay() {
		return lastAlertDay;
	}

	public void setLastAlertDay(Date lastAlertDay) {
		this.lastAlertDay = lastAlertDay;
	}

	public int getTodaysAlertCount() {
		return todaysAlertCount;
	}

	public void setTodaysAlertCount(int todaysAlertCount) {
		this.todaysAlertCount = todaysAlertCount;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
}
