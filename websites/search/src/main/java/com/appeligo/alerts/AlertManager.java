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
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.transaction.Synchronization;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Transaction;

import com.appeligo.epg.DefaultEpg;
import com.appeligo.search.util.ConfigUtils;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.ScheduledProgram;
import com.knowbout.hibernate.TransactionManager;

public class AlertManager {
	
	private static final Log log = LogFactory.getLog(AlertManager.class);
	
	private static AlertManager singleton;
	
	private Configuration config;
	private EPGProvider epg;
	private String indexLocation;
	private String compositeIndexLocation;
	private PendingAlertThread pendingAlertThread;
	private KeywordAlertThread keywordAlertThread;
	private CheckAlertsThread checkAlertsThread;
	private Date lastEpgUpdate;
	private boolean epgUpdated = false;

	public static class Initializer implements ServletContextListener {

	    public void contextInitialized(ServletContextEvent event) {
	    	new AlertManager();
	    }
	    
	    public void contextDestroyed(ServletContextEvent event) {
	    	AlertManager.singleton.exitGracefully();
	    }
	}
	
	private AlertManager() {
		log.info("Listened for init of AlertQueueImpl");
		
        config = ConfigUtils.getSystemConfig();
		indexLocation = config.getString("luceneIndex");
		compositeIndexLocation = config.getString("compositeIndex");
		epg = DefaultEpg.getInstance();
		
		lastEpgUpdate = new Date();
		
		AlertManager.singleton = this;
		
		pendingAlertThread = new PendingAlertThread(config);
		pendingAlertThread.start();
		try {
			keywordAlertThread = new KeywordAlertThread(config);
			keywordAlertThread.start();
		} catch (IOException e) {
			log.fatal("Can't process keyword alerts", e);
		}
		
		/* WE DECIDED NOT TO DO THIS ON RESTART
		checkAlerts();
		*/
    }
	
    private synchronized void exitGracefully() {
    	log.debug("Trying to exit alert threads gracefully");
    	if (pendingAlertThread != null) {
	    	synchronized(pendingAlertThread) {
		    	log.debug("Notifying pendingAlertsThread");
	    		pendingAlertThread.setActive(false);
		    	pendingAlertThread.notifyAll();
	    	}
    	}
    	if (keywordAlertThread != null) {
	    	synchronized(keywordAlertThread) {
		    	log.debug("Notifying keywordAlertsThread");
	    		keywordAlertThread.setActive(false);
		    	keywordAlertThread.notifyAll();
	    	}
    	}
    	if (checkAlertsThread != null) {
	    	synchronized(checkAlertsThread) {
		    	log.debug("Notifying checkAlertsThread");
	    		checkAlertsThread.setActive(false);
		    	checkAlertsThread.notifyAll();
	    	}
    	}
    	while ((pendingAlertThread != null && pendingAlertThread.isAlive()) ||
    			(keywordAlertThread != null && keywordAlertThread.isAlive()) ||
    			(checkAlertsThread != null && checkAlertsThread.isAlive())) {
    		try {
    			StringBuilder sb = new StringBuilder();
    			sb.append("Waiting for alert threads to shutdown: ");
    			if (pendingAlertThread != null && pendingAlertThread.isAlive()) sb.append("pendingAlertThread ");
    			if (keywordAlertThread != null && keywordAlertThread.isAlive()) sb.append("keywordAlertThread ");
    			if (checkAlertsThread != null && checkAlertsThread.isAlive()) sb.append("checkAlertsThread ");
    			sb.append('\n');
		    	log.debug(sb.toString());
				wait(1000);
			} catch (InterruptedException e) {
			}
    	}
    	log.debug("All alert threads have shutdown.");
    }
	    
	public static AlertManager getInstance() {
		return singleton;
	}
	
	public EPGProvider getEpg() {
		return epg;
	}
		
	public String getLuceneIndex() {
		return indexLocation;
	}
		
	public String getCompositeIndex() {
		return compositeIndexLocation;
	}
		
	public Thread getPendingAlertThread() {
		return pendingAlertThread;
	}
		
	public Thread getKeywordAlertThread() {
		return keywordAlertThread;
	}

	public Thread getCheckAlertsThread() {
		return checkAlertsThread;
	}
	
	public void checkAlerts() {
		checkAlertsThread = new CheckAlertsThread();
		checkAlertsThread.start();
	}

	public void checkIfPending(ProgramAlert programAlert) {
		if (programAlert.isDeleted() || programAlert.isDisabled() || (programAlert.getUser() == null)) {
			if (log.isDebugEnabled())log.debug("programAlert deleted="+programAlert.isDeleted()+", disabled="+programAlert.isDisabled()+", user="+programAlert.getUser());
			return;
		}
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 14); // 14 days in the future
		List<ScheduledProgram> scheduledPrograms =
			epg.getNextShowings(programAlert.getUser().getLineupId(),
								programAlert.getProgramId(), programAlert.isNewEpisodes(), true);
		
		log.debug("Adding alert for program "+programAlert.getProgramId()+", list of upcoming count="+scheduledPrograms.size());
		
		boolean pendingAlertsAdded = false;
		for (ScheduledProgram scheduledProgram : scheduledPrograms) {
			if (programAlert.isNewEpisodes() && // only alert on new episodes
				(!scheduledProgram.isNewEpisode())) {
				continue;
			}
			if (scheduledProgram == null) {
				if (log.isDebugEnabled())log.debug("checkIfPending did not find an upcoming program");
			}
			if (scheduledProgram != null) {
				if (log.isDebugEnabled())log.debug("Found next showing "+scheduledProgram.getProgramTitle()+", start: "+scheduledProgram.getStartTime());
				
				String callSign = scheduledProgram.getNetwork().getStationCallSign();
				Timestamp startTime = new Timestamp(scheduledProgram.getStartTime().getTime());
				Timestamp alertTime = new Timestamp(startTime.getTime() - (programAlert.getAlertMinutes() * 60000));
				
				Set<PendingAlert> pendingAlerts = programAlert.getLivePendingAlerts();
				for (PendingAlert pa : pendingAlerts) {
					if ((!pa.isDeleted()) &&
						(pa.getCallSign().equals(callSign)) &&
						(pa.getProgramStartTime().equals(startTime)) &&
						(pa.getAlertTime().equals(alertTime))) {
						if (log.isDebugEnabled())log.debug("found matching pending alert");
						continue;
					}
				}
				
				PendingAlert pendingAlert = new PendingAlert();
				pendingAlert.setProgramAlert(programAlert);
				pendingAlert.setProgramId(scheduledProgram.getProgramId());
				pendingAlert.setUserId(programAlert.getUser().getUserId());
				pendingAlert.setCallSign(callSign);
				pendingAlert.setProgramStartTime(scheduledProgram.getStartTime());
				pendingAlert.setAlertTime(alertTime);
				
				if (log.isDebugEnabled())log.debug("adding pending alert");
				pendingAlerts.add(pendingAlert);
				
				pendingAlertsAdded = true;
			}
		}
		if (pendingAlertsAdded) {
			Transaction currentTransaction = TransactionManager.currentTransaction();
			if (currentTransaction == null) {
				synchronized(pendingAlertThread) {
					if (log.isDebugEnabled())log.debug("Notifying pending alert thread immediately");
					pendingAlertThread.notifyAll();
				}
			} else {
				if (log.isDebugEnabled())log.debug("Will Notify pending alert thread after transaction is done");
				currentTransaction.registerSynchronization(
					new Synchronization() {
	
						public void afterCompletion(int arg0) {
							synchronized(pendingAlertThread) {
								if (log.isDebugEnabled())log.debug("Notifying pending alert thread now that the transaction is done");
								pendingAlertThread.notifyAll();
							}
						}
	
						public void beforeCompletion() {
							// Not needed
						}
					}
				);
			}
		}
	}

	Date getLastEpgUpdate() {
		return lastEpgUpdate;
	}

	void setLastEpgUpdate(Date lastEpgUpdate) {
		this.lastEpgUpdate = lastEpgUpdate;
	}

	public boolean isEpgUpdated() {
		return epgUpdated;
	}

	public void setEpgUpdated(boolean epgUpdated) {
		this.epgUpdated = epgUpdated;
	}
}
