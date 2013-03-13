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

import org.acegisecurity.providers.encoding.PasswordEncoder;

import com.appeligo.search.actions.BaseAction;
import com.appeligo.search.entity.User;

/**
 * Action methods related to changing/recovering an existing password.
 * @author fear
 */
@SuppressWarnings("serial")
public class ChangePasswordAction extends BaseAction {

	private PasswordEncoder passwordEncoder;
	
	private String currentPassword;
	
	private String newPassword;
	
	private String newPasswordConfirm;
	
	private String emailAddress;
	
	private boolean validatePassword = true;
	
	public ChangePasswordAction() {
		
	}
	
	@Override
	public void validate() {
		if (!validatePassword) {
			return;
		}
		// If the configurable validation has already detected an error, consider
		// that a short circuit.
		if (hasFieldErrors()) {
			return;
		}
		User user = getUser();
		if (!user.getPassword().equals(passwordEncoder.encodePassword(currentPassword, null))) {
			addFieldError("currentPassword", getText("currentpassword.required"));
			return;
		}
		if (!newPassword.equals(newPasswordConfirm)) {
			addFieldError("newPassword", getText("password.confirmationincorrect"));
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public String savePassword() throws Exception {
		User user = getUser();
		user.setPassword(passwordEncoder.encodePassword(newPassword, null));
		return SUCCESS;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public String resetPassword() throws Exception {
		User user = User.findByEmail(emailAddress);
		if (user == null) {
			addActionError(getText("emailaddress.invalidforrecovery"));
			return INPUT;
		}
		
		String temporaryPassword = user.createSecret(7);
		
		user.setPassword(passwordEncoder.encodePassword(temporaryPassword, null));
		// PENDING JMF: If we enabled Acegi's UserDetails cache, we may need to flush it here.
		user.sendPasswordResetMessage(temporaryPassword);
		return SUCCESS;
	}
	
	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getNewPasswordConfirm() {
		return newPasswordConfirm;
	}

	public void setNewPasswordConfirm(String newPasswordConfirm) {
		this.newPasswordConfirm = newPasswordConfirm;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public boolean isValidatePassword() {
		return validatePassword;
	}

	public void setValidatePassword(boolean validate) {
		this.validatePassword = validate;
	}

	public PasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}
}
