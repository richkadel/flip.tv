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

import java.util.Date;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.appeligo.alerts.KeywordAlert;
import com.appeligo.alerts.ProgramAlert;
import com.appeligo.search.util.ConfigUtils;

public class KeywordAlertAction extends AlertAction {
    private static final long serialVersionUID = 7019890080969775786L;
	private static final Logger queryLog = Logger.getLogger("fliptv.query");

	private static final Log log = LogFactory.getLog(KeywordAlertAction.class);
    
    private long keywordAlertId;
    private boolean usingPrimaryEmailRealtime;
    private boolean usingSMSRealtime;
    private int maxAlertsPerDay;
    
    public String createKeywordAlert() throws Exception {
    	
    	String alertTopic = getQuery();
    	if (alertTopic == null || alertTopic.trim().length() == 0) {
			setReturnMessage("You have to enter a topic to be alerted on.");
    		return INPUT;
    	}
    	KeywordAlert keywordAlert = new KeywordAlert();
    	keywordAlert.setUser(getUser());
    	keywordAlert.setUserQuery(getQuery());
    	if (getUser().isSmsValid()) {
	    	keywordAlert.setUsingPrimaryEmailRealtime(false);
	    	keywordAlert.setUsingSMSRealtime(true);
    	} else {
	    	keywordAlert.setUsingPrimaryEmailRealtime(true);
	    	keywordAlert.setUsingSMSRealtime(false);
    	}
    	keywordAlert.setMaxAlertsPerDay(ConfigUtils.getSystemConfig().getInt("defaultMaxAlertsPerDay",5));
    	
    	keywordAlert.setUsingAlternateEmailRealtime(false);
    	keywordAlert.setUsingIMRealtime(false);
    	Set<KeywordAlert> keywordAlerts = getUser().getLiveKeywordAlerts();
    	
    	if (keywordAlerts.size() >= getUser().getMaxEntries()) {
			setReturnMessage("Sorry.  You've exceeded your quota.  Delete some old topic alerts or send us feedback if you think you need more.");
			log.fatal(getUsername()+" exceeded the maximum number of KeywordAlerts. maxEntries="
					+getUser().getMaxEntries());
			return INPUT;
    	}
    	
    	
    	
		for (KeywordAlert element : keywordAlerts) {
			if (element.getUserQuery().equals(keywordAlert.getUserQuery())) {
				setReturnMessage("An alert on \""+keywordAlert.getUserQuery()+"\" already exists.");
				setReturnUrlExplicitely(getReturnUrl() + "&existingKeywordAlertId=" + element.getId());
				return INPUT;
			}
		}
		
    	getUser().getKeywordAlerts().add(keywordAlert);
    	keywordAlert.save(); // to guarantee the id is set
    	
    	//Log the alert
    	StringBuilder sb = getCookieValue();
		sb.append("|keyword=");
		String query = getQuery();
		sb.append(query.replace("|","\\|"));
		queryLog.info(sb.toString());
        return SUCCESS;
    }
    
    public String changeKeywordAlert() throws Exception {
    	if (!(isUsingPrimaryEmailRealtime() || isUsingSMSRealtime())) {
    		setReturnMessage("You must select at least one method for receiving alerts");
    		return INPUT;
    	}
    	KeywordAlert keywordAlert = KeywordAlert.getById(getKeywordAlertId());
    	keywordAlert.setUsingPrimaryEmailRealtime(isUsingPrimaryEmailRealtime());
    	keywordAlert.setUsingSMSRealtime(isUsingSMSRealtime());
    	keywordAlert.setMaxAlertsPerDay(getMaxAlertsPerDay());
    	keywordAlert.setLastModified(new Date());
    	keywordAlert.save();
        return SUCCESS;
    }
    
    public String changeKeywordAlertDisabled() throws Exception {
    	KeywordAlert keywordAlert = KeywordAlert.getById(getKeywordAlertId());
    	keywordAlert.setDisabled(isDisabled());
    	keywordAlert.save();
        return SUCCESS;
    }
    
    public String deleteKeywordAlert() throws Exception {
    	KeywordAlert keywordAlert = KeywordAlert.getById(getKeywordAlertId());
    	Set<ProgramAlert> programAlerts = keywordAlert.getProgramAlerts();
    	for (ProgramAlert programAlert : programAlerts) {
    		programAlert.setOriginatingKeywordAlert(null);
    	}
    	programAlerts.clear(); //ProgramAlert.removeKeywordAssociation(keywordAlert);
    	keywordAlert.setDeleted(true);
    	keywordAlert.save();
		setReturnMessage("Deleted all alerts for <a href=\""+getServletRequest().getContextPath()+"/search/search.action?query="+keywordAlert.getUserQuery()+"\">"+keywordAlert.getUserQuery()+"</a>");
    	
        return SUCCESS;
    }
    
	public String getReturnUrl() {
		if (returnUrl == null) {
			returnUrl = "/alerts/alerts.action";
		}
		return returnUrl;
	}

	public long getKeywordAlertId() {
		return keywordAlertId;
	}

	public void setKeywordAlertId(long keywordAlertId) {
		this.keywordAlertId = keywordAlertId;
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

	public int getMaxAlertsPerDay() {
		return maxAlertsPerDay;
	}

	public void setMaxAlertsPerDay(int maxAlertsPerDay) {
		this.maxAlertsPerDay = maxAlertsPerDay;
	}
}
