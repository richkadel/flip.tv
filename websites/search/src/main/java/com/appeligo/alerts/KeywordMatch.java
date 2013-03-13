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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;

import com.appeligo.search.entity.Permissions;
import com.knowbout.hibernate.model.PersistentObject;

public class KeywordMatch extends PersistentObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5882171182431638042L;
	
	private static final Log log = LogFactory.getLog(KeywordMatch.class);
	
	private long keywordAlertId;
	private String programId;
	private Date programEndTime;
	private Date creationTime;
	
	public KeywordMatch() {
	}

	public KeywordMatch(KeywordAlert keywordAlert, String programId, Date programEndTime) {
		keywordAlertId = keywordAlert.getId();
		this.programId = programId;
		this.programEndTime = programEndTime;
		creationTime = new Date();
	}

	public long getKeywordAlertId() {
		return keywordAlertId;
	}

	public void setKeywordAlertId(long keywordAlertId) {
		this.keywordAlertId = keywordAlertId;
	}

	public String getProgramId() {
		return programId;
	}

	public void setProgramId(String programId) {
		this.programId = programId;
	}

	public Date getProgramEndTime() {
		return programEndTime;
	}

	public void setProgramEndTime(Date programEndTime) {
		this.programEndTime = programEndTime;
	}

	public Date getCreationTime() {
		return creationTime;
	}
	
	public static void deleteOldProgramMatches() {
    	Permissions.checkUser(Permissions.SUPERUSER);
		Session session = getSession();
		Query query = session.getNamedQuery("KeywordMatch.deleteOldProgramMatches");
		// Make sure it has been at least a half hour since the show
		// ended.  We can't remove the KeywordMatch too quickly (e.g., RIGHT
		// after it ends), or the LiveLuceneIndex may not have yet purged the
		// program, and we would get our match all over again (duplicate
		// keyword alerts were a problem)
		query.setTimestamp("thirtyMinutesAgo", new Date(System.currentTimeMillis()-(30*60*1000)));
		query.executeUpdate();
	}
	
	public static KeywordMatch getKeywordMatch(long keywordAlertId, String programId) {
		Session session = getSession();
		Query query = session.getNamedQuery("KeywordMatch.getKeywordMatch");
		query.setLong("keywordAlertId", keywordAlertId);
		query.setString("programId", programId);
		return (KeywordMatch)query.uniqueResult();
	}
	
	public boolean equals(Object rhs) {
		if (rhs == this) {
			return true;
		}
		if (!(rhs instanceof KeywordMatch)) {
			return false;
		}
		if ((((KeywordMatch)rhs).keywordAlertId == keywordAlertId) &&
			((KeywordMatch)rhs).programId.equals(programId)) {
			return true;
		}
		return false;
	}
	
	public int hashCode() {
		return (int)((keywordAlertId + programId.hashCode()) % Integer.MAX_VALUE);
	}
}
