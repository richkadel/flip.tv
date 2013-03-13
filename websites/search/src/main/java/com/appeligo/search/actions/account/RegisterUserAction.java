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

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.context.HttpSessionContextIntegrationFilter;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.encoding.PasswordEncoder;
import org.apache.log4j.Logger;

import com.appeligo.search.entity.Friend;
import com.appeligo.search.entity.Group;
import com.appeligo.search.entity.User;
import com.appeligo.search.util.ConfigUtils;
import com.appeligo.epg.util.EpgUtils;
import com.knowbout.hibernate.TransactionManager;

/**
 * Provides the save action for user registration.  It is largely driven by 
 * configuration data from the system configuration RegisterUserAction-validation.xml file.
 * @author fear
 */
@SuppressWarnings("serial")
public class RegisterUserAction extends BaseAccountAction {
    
	private static final Logger log = Logger.getLogger(RegisterUserAction.class);
	
	public static final String LOGGED_IN = "logged_in";
	
	private PasswordEncoder passwordEncoder;
	
	private AuthenticationManager authenticationManager;
	
	@Override
	public String execute() {
		if (!super.isAdministrativeAction() && getServletRequest().getRemoteUser() != null) {
			return LOGGED_IN;
		}
		return SUCCESS;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public String register() throws Exception {
		if (!super.isAdministrativeAction() && getServletRequest().getRemoteUser() != null) {
			return LOGGED_IN;
		}
		
		User user = getPopulatedUser(true);
		if (User.findByUsername(user.getUsername()) != null) {
			addActionError(getText("username.alreadytaken", new String[]{user.getUsername()}));
			return INPUT;
		} 
		if (User.findByEmail(user.getPrimaryEmail()) != null) {
			addActionError(getText("email.alreadytaken", new String[]{user.getPrimaryEmail()}));
			return INPUT;
		} 
		if (User.findBySmsEmail(user.getPrimaryEmail()) != null) {
			addActionError(getText("email.alreadytaken", new String[]{user.getPrimaryEmail()}));
			return INPUT;
		} 
		user.setMaxEntries(ConfigUtils.getSystemConfig().getInt("defaultMaxEntries", 25));
		user.setCreationTime(new Timestamp(System.currentTimeMillis()));
		user.setLastLogin(new Timestamp(System.currentTimeMillis()));
		
		// We are going to cache off the password to auto-authenticate them when
		// they verify their email address..
		cachePassword(user.getPassword());
		
		user.setPassword(passwordEncoder.encodePassword(user.getPassword(), null));
		user.setSearchType("FUTURE");
		user.setEnabled(true);
		user.setLineupId(EpgUtils.determineLineup(user.getTimeZone(), Integer.parseInt(getStationLineup())));
		String rtn = setBirthMonthYear(user);
		if (!rtn.equals(SUCCESS)) {
			return rtn;
		}
		setSmsTime(user);
		user.insert();
		
		// If being done by an administrator, confirm the registration automatically...
		if (!this.isAdministrativeAction()) {
    		user.sendRegistrationMessage();
		}
		if (user.getSmsEmail() != null && !"".equals(user.getSmsEmail())) {
			user.setSmsVerificationCode(user.createSmsSecret());
			user.sendSmsVerifyMessage();
		}
		setTimeZone(user.getTimeZone());
		setLineup(user.getLineupId());
		
		// If being done by an administrator, confirm the registration automatically...
		if (this.isAdministrativeAction()) {
			this.completeRegistrationState(user);
		}
		
		return SUCCESS;
	}
	
	/**
	 * Annotated because supporting API does not use generics.
	 * @param password
	 */
	@SuppressWarnings("unchecked")
	private void cachePassword(String password) {
		getSession().put("password", password);
	}
	
	private String getCachedPassword() {
		return (String)getSession().get("password");
	}
	
	private void removeCachedPassword() {
		getSession().remove("password");
	}

	private void completeRegistrationState(User user) {
		user.setRegistrationComplete(true);
		Group defaultGroup = new Group();
		defaultGroup.setGroup(ConfigUtils.getSystemConfig().getString("defaultUserGroup", "user"));
		defaultGroup.setUsername(user.getUsername());
		defaultGroup.setUserId(user.getUserId());
		
		if (user.getGroups() == null) {
			user.setGroups(new HashSet<Group>());
		}
		
		if (!user.getGroups().contains(defaultGroup)) {
			user.getGroups().add(defaultGroup);
			defaultGroup.insert();
		}
		if (log.isInfoEnabled()) {
			log.info("User " + user.getUsername() + " has successfully completed registration.");
		}
	}
	
	/**
	 * Used to confirm a user's token to complete their registration.  If the account is found
	 * by the registration token the token is set to null, and the user is now fully registered.
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String confirmRegistration() throws Exception {
		User user = User.findByUsernameAndSecret(getUsername(), getRegistrationSecret());
		if (user != null) {
			// Set the registration flag, and add the user to the required default group.
			// From here on they have the default user rights.
			completeRegistrationState(user);
			int invites = updateFriends(user);
			getSession().put("friendNetworkInvites", new Integer(invites));
			
			TransactionManager.commitTransaction();
			TransactionManager.beginTransaction();
			attemptRegistrationConfirmationLogin(user);
			
			return SUCCESS;
		} else {
			return INPUT;
		}
	}
	
	/**
	 * Integrates with Acegi to authenticate the user (ensuring the same session is used) with 
	 * the password they gave during registration, and then binding the information to the current
	 * HttpSessoin object to complete the login sequence.
	 * 
	 * @param user The user to be logged in.
	 */
	private void attemptRegistrationConfirmationLogin(User user) {
		if (getCachedPassword() != null) {
			Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), getCachedPassword());
			authentication = authenticationManager.authenticate(authentication);
			removeCachedPassword();
			SecurityContextHolder.getContext().setAuthentication(authentication);
			getServletRequest().getSession().setAttribute(
					HttpSessionContextIntegrationFilter.ACEGI_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
		}
	}

	private int updateFriends(User user) {
		List<Friend> friends = Friend.findInvites(user);
		for (Friend friend: friends) {
			friend.setFriendUser(user);
		}
		return friends.size();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// The following methods are for spring injection.
	///////////////////////////////////////////////////////////////////////////
	
	public PasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public AuthenticationManager getAuthenticationManager() {
		return authenticationManager;
	}

	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

    
    
}
