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

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.appeligo.config.ConfigurationService;
import com.appeligo.epg.EpgIndexer;
import com.appeligo.lucene.DidYouMeanIndexer;
import com.appeligo.search.entity.Permissions;
import com.appeligo.search.entity.User;
import com.appeligo.search.util.ChunkedResults;
import com.appeligo.search.util.ConfigUtils;
import com.knowbout.hibernate.HibernateUtil;

public class CheckAlertsThread extends Thread {
	
	private static final Log log = LogFactory.getLog(CheckAlertsThread.class);
	
	private AlertManager alertManager;
	private boolean isActive;
	
	public CheckAlertsThread() {
		super("Check Alerts Thread");
		isActive = true;
		alertManager = AlertManager.getInstance();
	}

	public void run() {
		Permissions.setCurrentUser(Permissions.SUPERUSER);
		Session session = HibernateUtil.openSession();
		try {
			checkProgramAlerts();
			updateEpgIndex();
			checkKeywordAlerts();
			refreshDidYouMeanIndex();
		} catch (Exception e){
			log.error("error", e);
		} finally {
			session.close();
		}
	}
	
	private void refreshDidYouMeanIndex() throws IOException {
		Configuration config = ConfigUtils.getSystemConfig();
		String luceneIndex = config.getString("compositeIndex");
		String spellIndex = config.getString("spellIndex");
		DidYouMeanIndexer.createDefaultSpellIndex(luceneIndex, spellIndex);
	}

	private void checkProgramAlerts() throws IOException {
		ChunkedResults<ProgramAlert> results = ProgramAlert.getAll();
		if (log.isDebugEnabled())log.debug("CheckAlertsThread starting to check ProgramAlerts for pending");
		results.beforeFirst();
		while (results.next() && isActive()) {
			ProgramAlert programAlert = results.get();
			if (programAlert.isDeleted() || programAlert.isDisabled()) {
				if (log.isDebugEnabled())log.debug("program alert is deleted or disabled");
				continue;
			}
			User user = programAlert.getUser();
			if (user == null) {
				if (log.isDebugEnabled())log.debug("program alert is implicitly deleted (user is null)");
				programAlert.setDeleted(true);
				programAlert.save();
				continue;
			}
			Transaction transaction = HibernateUtil.currentSession().beginTransaction();
			try {
				alertManager.checkIfPending(programAlert);
				transaction.commit();
			} catch (Throwable t) {
				log.error("Error checking program alerts. Rolling back transaction.", t);
				transaction.rollback();
			}
		}
		if (log.isDebugEnabled())log.debug("CheckAlertsThread done with check ProgramAlerts for pending. Now deleting marked deleted ProgramAlerts.");
		
		// Now that we aren't looping on ProgramAlerts, I should be able to safely
		// delete all of the ProgramAlerts flagged as deleted without screwing up
		// paging through results as above.
		Transaction transaction = HibernateUtil.currentSession().beginTransaction();
		try {
			log.debug("deleting marked-deleted program alerts");
			ProgramAlert.deleteAllMarkedDeleted();
			log.debug("deleted marked-deleted program alerts");
		} finally {
			transaction.commit();
		}
	}
	
	private void updateEpgIndex() {
		Date updated = new Date();
		//Update the index with new programs now:
		Configuration lineupConfiguration = ConfigurationService.getConfiguration("lineups");
		List<String> lineups = (List<String>)lineupConfiguration.getList("lineups.lineup.id");

		EpgIndexer indexer = new EpgIndexer(alertManager.getLuceneIndex(),
				alertManager.getCompositeIndex(),
				alertManager.getEpg(), lineups);
		indexer.updateEpgIndex(alertManager.getLastEpgUpdate());
		alertManager.setLastEpgUpdate(updated);
	}

	/**
	 * search for keyword matches on future programs
	 * @throws ParseException
	 * @throws IOException
	 */
	private void checkKeywordAlerts() {
		alertManager.setEpgUpdated(true);
		// Let's the AlertManager know that we have to check the EPG
		// against KeywordAlerts, but we want the KeywordAlertsThread to do it.
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}
