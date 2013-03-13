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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.appeligo.alerts.ProgramAlert;

public abstract class AlertAction extends BaseAction {
	
	private static final Log log = LogFactory.getLog(AlertAction.class);
	
	protected String returnUrl;
	protected String hash;
    private int prewarn;
    private String prewarnUnits;
    private boolean usingPrimaryEmail;
    private boolean usingSMS;
	private int alertMinutes;
	private boolean disabled;

	protected void setProgramAlert(ProgramAlert programAlert) {
		programAlert.setAlertMinutes(alertMinutes);
		programAlert.setUsingAlternateEmail(false);
		programAlert.setUsingIM(false);
		programAlert.setUsingPrimaryEmail(isUsingPrimaryEmail());
		programAlert.setUsingSMS(isUsingSMS());
	}

	protected String checkInput() {
		if (getPrewarn() > 999) {
			setReturnMessage("Pre-warning must be less than 1000 "+getPrewarnUnits()+".");
			return INPUT;
		}
		
		alertMinutes = 0;
		if (getPrewarnUnits().equals("Minutes")) {
			alertMinutes = getPrewarn();
		} else if (getPrewarnUnits().equals("Hours")) {
			alertMinutes = getPrewarn() * 60;
		} else if (getPrewarnUnits().equals("Days")) {
			alertMinutes = getPrewarn() * 60 * 24;
		} else {
			setReturnMessage("Pre-warning can be in Minutes, Hours, or Days.");
			return INPUT;
		}
		
		if (!isUsingPrimaryEmail() && !isUsingSMS()) {
			setReturnMessage("You must select at least one method for receiving reminders.");
			return INPUT;
		}
		return SUCCESS;
	}
	
	public abstract String getReturnUrl();
	
    protected void setReturnMessage(String message) {
    	try {
			message = URLEncoder.encode(message, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unexpected error", e);
		}
    	getReturnUrl();
		if (returnUrl.indexOf('?') >= 0) {
			returnUrl += "&message="+message;
		} else {
			returnUrl += "?message="+message;
		}
    }
    
	public void setReturnUrl(String encodedUrl) {
		returnUrl = getUrlEncoder().decode(encodedUrl);
    	hash = "";
    	int hashpos = returnUrl.indexOf('#');
    	if (hashpos >= 0) {
    		hash = returnUrl.substring(hashpos);
	    	returnUrl = returnUrl.substring(0, hashpos);
    	}
		returnUrl = returnUrl.replaceFirst("[&?]message=.*$", "");
		returnUrl = returnUrl.replaceFirst("[&?]valid=.*$", "");
		returnUrl = returnUrl.replaceFirst("[&?]autoDeleteReminders=.*$", "");
	}
	
	public void setReturnUrlExplicitely(String returnUrl) {
		this.returnUrl = returnUrl;
	}
	
	public int getPrewarn() {
		return prewarn;
	}

	public void setPrewarn(int prewarn) {
		this.prewarn = prewarn;
	}

	public String getPrewarnUnits() {
		return prewarnUnits;
	}

	public void setPrewarnUnits(String prewarnUnits) {
		this.prewarnUnits = prewarnUnits;
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

	public int getAlertMinutes() {
		return alertMinutes;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
}