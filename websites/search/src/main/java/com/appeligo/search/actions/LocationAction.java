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

import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.appeligo.epg.util.EpgUtils;

public class LocationAction extends BaseAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3333260100035500673L;
	public static final String ACCOUNT = "account";
	private static final Logger log = Logger.getLogger(LocationAction.class);
	
    private String selectedTimeZone;
    private int contentLineup;
    private boolean locationChanged;
	private String returnUrl;
	private String hash;
    
    public String execute() throws Exception {
    	if (getUser() != null) {
    		return ACCOUNT;
    	}
    	selectedTimeZone = getTimeZone().getID();
    	String lineup = getLineup();
    	if (lineup.endsWith("S")) {
    		contentLineup = 3;
    	} else if (lineup.endsWith("DC")) {
    		contentLineup = 2;
    	} else {
    		contentLineup = 1;
    	}
        return SUCCESS;
    }
    
    public String saveLocation() {
    	
    	TimeZone tz = TimeZone.getTimeZone(selectedTimeZone);
    	if (tz.getID().equals(TimeZone.getTimeZone("GMT"))) {
    		addActionError("Unable to determine the correct timezone. Please select on from the list.");
    		return INPUT;
    	}
    	if (contentLineup < 1 || contentLineup > 3) {
    		addActionError("Unable to determine the correct channel lineup. Please select on from the list.");
    		return INPUT;
    	}
    	locationChanged = true;
    	String lineupId = EpgUtils.determineLineup(tz, contentLineup);
    	setTimeZone(tz);
    	setLineup(lineupId);
    	return SUCCESS;
    }

	/**
	 * @return Returns the contentLineup.
	 */
	public int getContentLineup() {
		return contentLineup;
	}

	/**
	 * @param contentLineup The contentLineup to set.
	 */
	public void setContentLineup(int contentLineup) {
		this.contentLineup = contentLineup;
	}

	/**
	 * @return Returns the selectedTimeZone.
	 */
	public String getSelectedTimeZone() {
		return selectedTimeZone;
	}

	/**
	 * @param selectedTimeZone The selectedTimeZone to set.
	 */
	public void setSelectedTimeZone(String selectedTimeZone) {
		this.selectedTimeZone = selectedTimeZone;
	}

	/**
	 * @return Returns the locationChanged.
	 */
	public boolean isLocationChanged() {
		return locationChanged;
	}

	/**
	 * @param locationChanged The locationChanged to set.
	 */
	public void setLocationChanged(boolean locationChanged) {
		this.locationChanged = locationChanged;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String encodedUrl) {
		returnUrl = getUrlEncoder().decode(encodedUrl);
	}
}


