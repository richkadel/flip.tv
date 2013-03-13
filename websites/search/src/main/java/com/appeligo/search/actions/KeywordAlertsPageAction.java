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

import java.util.Comparator;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.appeligo.alerts.AlertManager;
import com.appeligo.alerts.KeywordAlert;
import com.appeligo.alerts.ProgramAlert;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.Program;
import com.knowbout.epg.service.ScheduledProgram;

public class KeywordAlertsPageAction extends BaseAction {
	
    private static final long serialVersionUID = 7019890080969775786L;
    
	private static final Log log = LogFactory.getLog(KeywordAlertsPageAction.class);
	
    private List<KeywordAlert> keywordAlerts;
    private long existingKeywordAlertId;
    private String alertsPage;
    private String message;
    private String valid;

    public String execute() throws Exception {
    	
    	//Sort keywords alphabetically
    	if (log.isDebugEnabled()) log.debug("=============== GETTING KeywordAlerts for Keyword Alerts page ==========");
    	keywordAlerts = new LinkedList<KeywordAlert>(getUser().getLiveKeywordAlerts());
    	Collections.sort(keywordAlerts,
			new Comparator<KeywordAlert>() {
				public int compare(KeywordAlert left, KeywordAlert right) {
					return left.getUserQuery().compareTo(right.getUserQuery());
				}
	    	}
    	);
        
        return SUCCESS;
    }

	public long getExistingKeywordAlertId() {
		return existingKeywordAlertId;
	}

	public void setExistingKeywordAlertId(long existingKeywordAlertId) {
		this.existingKeywordAlertId = existingKeywordAlertId;
	}

	public List<KeywordAlert> getKeywordAlerts() {
		return keywordAlerts;
	}

	public void setKeywordAlerts(List<KeywordAlert> keywordAlerts) {
		this.keywordAlerts = keywordAlerts;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getValid() {
		return valid;
	}

	public void setValid(String valid) {
		this.valid = valid;
	}

	@Override
	public String getFullRequestURL() {
		return super.getFullRequestURL().replaceFirst("&message=.*$", "");
	}

	public String getAlertsPage() {
		return alertsPage;
	}

	public void setAlertsPage(String alertsPage) {
		this.alertsPage = alertsPage;
	}
}
