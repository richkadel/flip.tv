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

package com.appeligo.search.actions.account;

import com.appeligo.search.entity.MessageContextException;
import com.appeligo.search.entity.User;
import com.appeligo.search.util.ConfigUtils;
import com.appeligo.epg.util.EpgUtils;

@SuppressWarnings("serial")
public class UpdateAccountAction extends BaseAccountAction {
	
	public static final String LOGOUT = "logout";
	
	private String resend;
	
	private String smsVerifyCode;
	
	private boolean informationSaved;
	
	public UpdateAccountAction() {
		
	}
	
	private void setUser() {
		setUser(User.findByUsername(getServletRequest().getRemoteUser()));
	}
	
	public String viewAccount() {
		setUser();
		/*
		if (getUser().getHighAge() != null && getUser().getLowAge() != null) {
			if (getUser().getHighAge() < 90) {
				super.setAgeGroup(getUser().getLowAge() + " to " + getUser().getHighAge());
			} else {
				super.setAgeGroup(getUser().getLowAge() + " and over");
			}
		}
		*/
		super.extractStationLineup(getUser());
		super.extractBirthMonthYear(getUser());
		super.extractSmsTime(getUser());
		return SUCCESS;
	}
	
	public String enterSmsAddress() {
		setUser();
		if (getUser().getSmsEmail() == null || "".equals(getUser().getSmsEmail().trim())) {
			addActionError("You must first enter your mobile device address.");
			return INPUT;
		} else {
			return SUCCESS;
		}
	}
	
	public String resendSmsCode() throws MessageContextException {
		setUser();
		User user = getUser();
		user.setSmsVerificationCode(user.createSmsSecret());
		user.sendSmsVerifyMessage();
		return INPUT;
	}
	
	public String verifySmsAddress() {
		setUser();
		if (smsVerifyCode != null && smsVerifyCode.equalsIgnoreCase(getUser().getSmsVerificationCode())) {
			getUser().setSmsVerified(true);
			getUser().setSmsVerificationCode(null);
			return SUCCESS;
		} else {
			addActionError("The verification code for your mobile device is incorrect. Ensure your mobile device address is correct.  You should receive the verification code on your device.");
			return INPUT;
		}
	}
	
	/**
	 * 
	 */
	public String saveAccount() throws Exception {
		User newState = getUser();
		// If we've gotten this far we've already validated our dataset.
		User userEntity = User.findByUsername(getServletRequest().getRemoteUser());
		
		// If they change their email, we need to rerun email validation step.
		boolean registrationComplete = newState.getPrimaryEmail().
			equals(userEntity.getPrimaryEmail());
		
		// We check this immediately to see if we need to compare an updated
		// email value on this account to 
		if (!registrationComplete && !userEntity.isEmailAvailableForUser(newState.getPrimaryEmail())) {
			addActionError(getText("email.alreadytaken", new String[]{newState.getPrimaryEmail()}));
			return INPUT;
		}
		
		// Check to see if SMS has changed in a way that requires confirmation.  If
		// the value has just been emptied out, do nothing but overwrite it.
		String sms = newState.getSmsEmail();
		boolean verifySms = !"".equals(sms.trim()) && !sms.equals(userEntity.getSmsEmail());
		if (!sms.equals(userEntity.getSmsEmail())) {
			//If they null out the field, but they had verified it before, we need to unverify it
			//So we will not send out SMS to the old account.
			userEntity.setSmsVerified(false);
		}
		newState.setRegistrationComplete(registrationComplete);
		newState = super.getPopulatedUser(!registrationComplete);
		userEntity.setProperties(newState);
		userEntity.setLineupId(EpgUtils.determineLineup(userEntity.getTimeZone(), Integer.parseInt(getStationLineup())));		
		String rtn = setBirthMonthYear(userEntity);
		if (!rtn.equals(SUCCESS)) {
			return rtn;
		}
		setSmsTime(userEntity);
		userEntity.save();
		//Incase the lineup changed reset the saved lineup in the session;
		setTimeZone(userEntity.getTimeZone());
		setLineup(userEntity.getLineupId());
		if (!registrationComplete) {
			userEntity.sendRegistrationMessage();
			return LOGOUT;
		}
		if (verifySms) {
			userEntity.setSmsVerificationCode(userEntity.createSmsSecret());
			userEntity.sendSmsVerifyMessage();
		}
		setInformationSaved(true);
		return SUCCESS;
	}

	public String getSmsVerifyCode() {
		return smsVerifyCode;
	}

	public void setSmsVerifyCode(String smsVerifyCode) {
		this.smsVerifyCode = smsVerifyCode;
	}

	public boolean isInformationSaved() {
		return informationSaved;
	}

	public void setInformationSaved(boolean informationSaved) {
		this.informationSaved = informationSaved;
	}

	public String getResend() {
		return resend;
	}

	public void setResend(String resend) {
		this.resend = resend;
	}
}
