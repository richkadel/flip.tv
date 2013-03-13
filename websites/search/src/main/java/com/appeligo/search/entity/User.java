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

package com.appeligo.search.entity;

import java.io.Serializable;
import java.util.Calendar;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.appeligo.alerts.KeywordAlert;
import com.appeligo.alerts.ProgramAlert;
import com.appeligo.search.util.ConfigUtils;
import com.knowbout.hibernate.model.PersistentObject;

/**
 * 
 * @author fear
 *
 */
public class User extends PersistentObject implements Serializable {

	private static final Logger log = Logger.getLogger(User.class);
	
	private static final long serialVersionUID = 4722963544634460204L;
	
	private static final int REGISTRATION_SECRET_SIZE = 32;
	
	private static final int SMS_SECRET_SIZE = 6;

	private static final char[] SECRET_CHARS = { 
		'0','1','2','3','4','5','6','7','8','9',
		'a','b','c','d','e','f','g','h','i','j',
		'k','l','m','n','o','p','q','r','s','t',
		'u','v','w','x','y','z',
		};
	
	private long userId;
	
	private String username;
	
	private String password;
	
	private String primaryEmail;
	
	private String gender;
	
	private Integer highAge;
	
	private Integer lowAge;
	
	private TimeZone timeZone;
	
	private String zipcode;
	
	private String smsEmail;
	
	private String city;
	
	private String state;
	
	private String registrationSecret;
	
	private boolean registrationComplete;
	
	private boolean smsVerified;
	
	private String smsVerificationCode;
		
	private Timestamp creationTime;
	
	private Time earliestSmsTime;
	
	private Time latestSmsTime;
	
	private int maxEntries;
	
	private Set<ProgramAlert> programAlerts = new HashSet<ProgramAlert>();
	private Set<KeywordAlert> keywordAlerts = new HashSet<KeywordAlert>();
	private Set<Friend> friends = new HashSet<Friend>();
	
	private Set<Group> groups;
	
	private Timestamp lastLogin;	
	private String firstName;
	private String lastName;
	private boolean enabled;
	private String lineupId;
	private String searchType;
	private int alertMinutesDefault = 15;
	private boolean usingPrimaryEmailDefault = true;
	private boolean usingAlternateEmailDefault;
	private boolean usingSMSDefault = true;
	private boolean usingIMDefault;
	private java.sql.Date birthMonthYear;
	
	public User() {
		
	}
	
	/**
	 * Creates a new user, and fills in the registration secret.
	 * @param name
	 * @param pass
	 */
	public User(String name, String pass) {
		this();
		this.username = name;
		this.password = pass;
		setRegistrationSecret(createRegistrationSecret());
		setCreationTime(new Timestamp(System.currentTimeMillis()));
		// I DON'T THINK THIS CONSTRUCTOR GETS CALLED!!!!!
        maxEntries = ConfigUtils.getSystemConfig().getInt("defaultMaxEntries", 25);
        log.debug("maxEntries="+maxEntries);
	}
	
	public void setProperties(User newState) {
		this.setCity(newState.city);
		this.setGender(newState.gender);
		this.setHighAge(newState.highAge);
		this.setLowAge(newState.lowAge);
		if (newState.password != null && !"".equals(newState.password)) {
			this.setPassword(newState.password);
		}
		this.setPrimaryEmail(newState.primaryEmail);
		this.setRegistrationComplete(newState.registrationComplete);
		this.setRegistrationSecret(newState.registrationSecret);
		this.setSmsEmail(newState.smsEmail);
		this.setState(newState.state);
		this.setZipcode(newState.zipcode);
		this.setLatestSmsTime(newState.latestSmsTime);
		this.setEarliestSmsTime(newState.earliestSmsTime);
		this.setTimeZone(newState.timeZone);
		this.setFirstName(newState.firstName);
		this.setLastName(newState.lastName);
		
	}
	
	/**
	 * 
	 * @throws MessageContextException
	 */
	public void sendRegistrationMessage() throws MessageContextException {
		Map<String, String> context = new HashMap<String, String>();
		String greeting = getUsername();
		context.put("username", greeting);
		String firstName = getFirstName();
		if (firstName != null && firstName.trim().length() > 0) {
			greeting = firstName;
		}
		context.put("greeting", greeting);
		context.put("regsecret", getRegistrationSecret());
		Message message = new Message("registration", context);
		// WARNING: DO NOT SET THE "USER" PROPERTY ON THIS Message OBJECT BECAUSE THE Messenger WILL NOT SEND
		// THE MESSAGE IF IT HAS A USER AND REGISTRATION IS NOT COMPLETE YET.
		message.setTo(getPrimaryEmail());
		message.insert();
	}
	
	/**
	 * 
	 * @throws MessageContextException
	 */
	public void sendSmsVerifyMessage() throws MessageContextException {
		Map<String, String> context = new HashMap<String, String>();
		context.put("username", getUsername());
		context.put("code", getSmsVerificationCode());
		Message message = new Message("sms_verify", context);
		// WARNING: DO NOT SET THE "USER" PROPERTY ON THIS Message OBJECT BECAUSE THE Messenger WILL NOT SEND
		// THE MESSAGE IF IT HAS A USER AND SMS VERIFICATION IS NOT COMPLETE YET.
		message.setTo(getSmsEmail());
		message.insert();
	}
	

	
	/**
	 * 
	 * @throws MessageContextException
	 */
	public void sendPasswordResetMessage(String password) throws MessageContextException {
		Map<String, String> context = new HashMap<String, String>();
		String greeting = getUsername();
		context.put("username", greeting);
		String firstName = getFirstName();
		if (firstName != null && firstName.trim().length() > 0) {
			greeting = firstName;
		}
		context.put("greeting", greeting);
		context.put("password", password);
		Message message = new Message("password_reset", context);
		message.setTo(getPrimaryEmail());
		message.insert();
	}
	
	/**
	 * Generates a secret string that can be used to verify a new user's registration.
	 * The length is 64 characters of 0-9 and a-z.
	 * @return
	 */
	public String createRegistrationSecret() {
		return createSecret(REGISTRATION_SECRET_SIZE);
	}
	
	/**
	 * 
	 * @return
	 */
	public String createSmsSecret() {
		return createSecret(SMS_SECRET_SIZE);
	}
	
	/**
	 * 
	 * @param size
	 * @return
	 */
	public String createSecret(int size) {
		StringBuilder secret = new StringBuilder(size);
		Random random = new Random();
		for (int i = 0; i < size; i++) {
			secret.append(SECRET_CHARS[random.nextInt(SECRET_CHARS.length)]);
		}
		return secret.toString();
	}
	
	/**
	 * Indicates that a user's registration process is complete.
	 * @return
	 */
	public boolean isRegistrationComplete() {
		return registrationComplete;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * 
	 * @param username
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static User findByUsername(String username) {
		Session session = getSession();
		Query query = session.getNamedQuery("User.findByUsername");
		query.setString("username", username);
		List<User> users = query.list();
		if (users.size() > 0) {
			return users.get(0);
		} else {
			return null;
		}
	}
	
	public static User findById(long userId) {
		return (User)getSession().get(User.class, userId);
	}
	
	/**
	 * 
	 * @param email
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static User findByEmail(String email) {
		return findByQuery("User.findByEmail", "email", email);
	}
	
	/**
	 * 
	 * @param email
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static User findBySmsEmail(String email) {
		return findByQuery("User.findBySmsEmail", "smsEmail", email);
	}
	
	private static User findByQuery(String queryName, String paramName, String param) {
		Session session = getSession();
		Query query = session.getNamedQuery(queryName);
		query.setString(paramName, param);
		List<User> users = query.list();
		if (users.size() > 0) {
			return users.get(0);
		} else {
			return null;
		}
	}
	
	public boolean isEmailAvailableForUser(String email) {
		Session session = getSession();
		Query query = session.getNamedQuery("User.checkEmailAvailableForUser");
		query.setString("email", email);
		query.setEntity("user", this);
		List test = query.list();
		if (test.size() > 0) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 
	 * @param username
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static User findByUsernameAndSecret(String username, String registrationSecret) {
		Session session = getSession();
		Query query = session.getNamedQuery("User.findByUsernameAndRegistrationSecret");
		query.setString("username", username);
		query.setString("registrationSecret", registrationSecret);
		if (log.isInfoEnabled()) {
			log.info("Attempting to load user by " + username + " and " + registrationSecret);
		}
		User user = (User)query.uniqueResult();
		return user;
	}

	/**
	 * @return Returns the smsEmail.
	 */
	public String getSmsEmail() {
		return smsEmail;
	}

	/**
	 * @param smsEmail The smsEmail to set.
	 */
	public void setSmsEmail(String smsEmail) {
		this.smsEmail = smsEmail;
	}

	public Integer getHighAge() {
		return highAge;
	}

	public void setHighAge(Integer highAge) {
		this.highAge = highAge;
	}

	public Integer getLowAge() {
		return lowAge;
	}

	public void setLowAge(Integer lowAge) {
		this.lowAge = lowAge;
	}

	public String getPrimaryEmail() {
		return primaryEmail;
	}

	public void setPrimaryEmail(String primaryEmail) {
		this.primaryEmail = primaryEmail;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Set<ProgramAlert> getProgramAlerts() {
		return programAlerts;
	}

	public void setProgramAlerts(Set<ProgramAlert> programAlerts) {
		this.programAlerts = programAlerts;
	}

	public Set<ProgramAlert> getLiveProgramAlerts() {
		Set<ProgramAlert> programAlerts = getProgramAlerts();
		Iterator<ProgramAlert> i = programAlerts.iterator();
		while (i.hasNext()) {
			ProgramAlert programAlert = i.next();
			if (programAlert.isDeleted()) {
				i.remove();
			}
		}
		return programAlerts;
	}

	public String getRegistrationSecret() {
		return registrationSecret;
	}

	public void setRegistrationSecret(String registrationSecret) {
		this.registrationSecret = registrationSecret;
	}

	public Timestamp getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Timestamp creationTime) {
		this.creationTime = creationTime;
	}

	public void setRegistrationComplete(boolean registrationComplete) {
		this.registrationComplete = registrationComplete;
	}

	public Set<KeywordAlert> getKeywordAlerts() {
		return keywordAlerts;
	}

	public Set<KeywordAlert> getLiveKeywordAlerts() {
		Set<KeywordAlert> keywordAlerts = getKeywordAlerts();
		Iterator<KeywordAlert> i = keywordAlerts.iterator();
		while (i.hasNext()) {
			KeywordAlert keywordAlert = i.next();
			if (keywordAlert.isDeleted()) {
				i.remove();
			}
		}
		return keywordAlerts;
	}

	public void setKeywordAlerts(Set<KeywordAlert> keywordAlerts) {
		this.keywordAlerts = keywordAlerts;
	}

	public Set<Group> getGroups() {
		return groups;
	}

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}
	
	public boolean isInGroup(String groupName) {
		Set<Group> groups = getGroups();
		if (groups != null) {
			for (Group group : getGroups()) {
				String thisGroupName = group.getGroup();
				if (thisGroupName != null && thisGroupName.equals(groupName)) {
					return true;
				}
			}
		}
		return false;
	}

	public Time getEarliestSmsTime() {
		return earliestSmsTime;
	}

	public void setEarliestSmsTime(Time earliestSmsTime) {
		this.earliestSmsTime = earliestSmsTime;
	}

	public Time getLatestSmsTime() {
		return latestSmsTime;
	}

	public void setLatestSmsTime(Time latestSmsTime) {
		this.latestSmsTime = latestSmsTime;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}
	
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public boolean isSmsVerified() {
		return smsVerified;
	}

	public void setSmsVerified(boolean smsVerified) {
		this.smsVerified = smsVerified;
	}

	public String getSmsVerificationCode() {
		return smsVerificationCode;
	}

	public void setSmsVerificationCode(String smsVerificationCode) {
		this.smsVerificationCode = smsVerificationCode;
	}

	public boolean isSmsValid() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(earliestSmsTime);
		if (cal.get(Calendar.HOUR_OF_DAY) == 0) {
			cal.setTime(latestSmsTime);
			if (cal.get(Calendar.HOUR_OF_DAY) == 0) {
				return smsVerified; // all times OK... 12AM to 12AM
			}
		}
		if ((latestSmsTime.getTime()-earliestSmsTime.getTime()) > 60*60*1000) { // at least an hour apart
			return smsVerified;
		}
		return false;
	}
		
	public boolean isSmsOKNow() {
		if (!isSmsValid()) {
			return false;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(earliestSmsTime);
		if (cal.get(Calendar.HOUR_OF_DAY) == 0) {
			cal.setTime(latestSmsTime);
			if (cal.get(Calendar.HOUR_OF_DAY) == 0) {
				return true; // all times OK... 12AM to 12AM
			}
		}
		
		cal = Calendar.getInstance(timeZone);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int current = (hour*60)+minute;
		Calendar early = Calendar.getInstance();
		early.setTime(earliestSmsTime);
		hour = early.get(Calendar.HOUR_OF_DAY);
		minute = early.get(Calendar.MINUTE);
		int earliest = (hour*60)+minute;
		
		Calendar late = Calendar.getInstance();
		late.setTime(latestSmsTime);
		hour = late.get(Calendar.HOUR_OF_DAY);
		minute = late.get(Calendar.MINUTE);
		int latest = (hour*60)+minute;
		
		if (current > earliest &&
			current < latest) {
			return true;
		}
		return false;
	}

	/**
	 * @return the maximum number of ProgramAlerts per user, maximum number of
	 * KeywordAlerts per user. 
	 */
	public int getMaxEntries() {
		return maxEntries;
	}

	public void setMaxEntries(int maxEntries) {
		this.maxEntries = maxEntries;
	}
	
	
	
	/**
	 * @return Returns the friends.
	 */
	public Set<Friend> getFriends() {
		return friends;
	}

	/**
	 * @param friends The friends to set.
	 */
	public void setFriends(Set<Friend> friends) {
		this.friends = friends;
	}

	public void addFriend(Friend friend) {
		Set<Friend> friends = getFriends();
		friends.add(friend);
	}
	
	public void removeFriend(Friend friend) {
		Set<Friend> friends = getFriends();
		friends.remove(friend);
	}
	
	@SuppressWarnings("unchecked")
	public static List<User> getUsers() {
		Session session = getSession();
		Query query = session.getNamedQuery("User.findAll");
		return query.list();
	}
    
	public boolean equals(User rhs) {
		if (rhs == this) {
			return true;
		}
		if (!getClass().isInstance(rhs)) {
			return false;
		}
		User other = (User)rhs;
		if (other.getUserId() == getUserId()) {
			return true;
		}
		return false;
	}
	
	public String toString() {
		return getUsername()+":"+getUserId();
	}

	/**
	 * @return Returns the enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled The enabled to set.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return Returns the lastLogin.
	 */
	public Timestamp getLastLogin() {
		return lastLogin;
	}

	/**
	 * @param lastLogin The lastLogin to set.
	 */
	public void setLastLogin(Timestamp lastLogin) {
		this.lastLogin = lastLogin;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getDisplayName() {
		StringBuilder sb = new StringBuilder();
		if (firstName != null) {
			sb.append(firstName);
		}
		if (lastName != null) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(lastName);
		}
		if (sb.length() == 0) {
			sb.append(username);
		}
		return sb.toString();
	}

	public String getSearchType() {
		return searchType;
	}

	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}

	/**
	 * @return Returns the lineupId.
	 */
	public String getLineupId() {
		return lineupId;
	}

	/**
	 * @param lineupId The lineupId to set.
	 */
	public void setLineupId(String lineupId) {
		this.lineupId = lineupId;
	}

	public int getAlertMinutesDefault() {
		return alertMinutesDefault;
	}

	public void setAlertMinutesDefault(int alertMinutesDefault) {
		this.alertMinutesDefault = alertMinutesDefault;
	}

	public boolean isUsingAlternateEmailDefault() {
		return usingAlternateEmailDefault;
	}

	public void setUsingAlternateEmailDefault(boolean usingAlternateEmailDefault) {
		this.usingAlternateEmailDefault = usingAlternateEmailDefault;
	}

	public boolean isUsingIMDefault() {
		return usingIMDefault;
	}

	public void setUsingIMDefault(boolean usingIMDefault) {
		this.usingIMDefault = usingIMDefault;
	}

	public boolean isUsingPrimaryEmailDefault() {
		return usingPrimaryEmailDefault;
	}

	public void setUsingPrimaryEmailDefault(boolean usingPrimaryEmailDefault) {
		this.usingPrimaryEmailDefault = usingPrimaryEmailDefault;
	}

	public boolean isUsingSMSDefault() {
		return usingSMSDefault;
	}

	public void setUsingSMSDefault(boolean usingSMSDefault) {
		this.usingSMSDefault = usingSMSDefault;
	}

	public java.sql.Date getBirthMonthYear() {
		return birthMonthYear;
	}

	public void setBirthMonthYear(java.sql.Date birthMonthYear) {
		this.birthMonthYear = birthMonthYear;
	}
	
	
	
}
