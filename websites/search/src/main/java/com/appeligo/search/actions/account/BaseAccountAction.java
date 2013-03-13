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

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.appeligo.search.actions.BaseAction;
import com.appeligo.search.entity.User;
import com.appeligo.search.util.ConfigUtils;
import com.opensymphony.xwork.Preparable;

@SuppressWarnings("serial")
public class BaseAccountAction extends BaseAction implements Preparable {

	public static final String PASSWORD_MISMATCH = "password_mismatch";
	private static final Log log = LogFactory.getLog(RegisterUserAction.class);
	
	private boolean administrativeAction = false;
	
	private String states;
	/*
	private String ageRanges;
	private String ageGroup;
	*/
	private String years;
	private String months;
	private String gender;
	private String genders;
	private String username;
	private String firstName;
	private String lastName;

	
	private User user;
	
	private String birthYear;
	private String birthMonth;
	private String passwordConfirm;
	private String stationLineup;
	private String stationLineups;
	private Time earliestSmsTime;
	private Time latestSmsTime;
	
	private Map<String, String> ageMapper = new HashMap<String, String>();
	
	private String registrationSecret;

	/**
	 *
	 */
	public BaseAccountAction() {
		super();
	}

	private void validatePassword() {
		if (user != null && user.getPassword() != null && 
				!user.getPassword().equals(getPasswordConfirm())) {
			if (log.isDebugEnabled()) 
				log.debug("Invalid password match attempt for desired username " + user.getUsername());
			this.addFieldError("user.password", "Your password entries do not match.");
		}
	}
	
	@Override
	public void validate() {
		super.validate();
		validatePassword();
	}
	
	/**
	 * Prepares the list of states in a WW/OGNL compatible notation.
	 */
	public void prepare() throws Exception {
		Configuration config = ConfigUtils.getSystemConfig();
		//prepareAgeRanges(config);
		prepareStateList(config);
		prepareYearList(config);
		prepareMonthList(config);
		prepareGenderList(config);
		prepareStationLineups(config);
	}
	
	@SuppressWarnings("unchecked")
	protected void prepareStateList(Configuration config) {
	    List<String> stateList = config.getList("stateList.state", new ArrayList());
	    states = renderAsOgnlList(stateList);
	}

	@SuppressWarnings("unchecked")
	protected void prepareYearList(Configuration config) {
	    List<String> yearList = config.getList("years.year", new ArrayList());
	    years = renderAsOgnlList(yearList);
	}

	@SuppressWarnings("unchecked")
	protected void prepareMonthList(Configuration config) {
	    List<String> monthList = config.getList("months.month", new ArrayList());
	    months = renderAsOgnlList(monthList);
	}

	@SuppressWarnings("unchecked")
	protected void prepareGenderList(Configuration config) {
		ArrayList<String> temp = new ArrayList<String>();
		temp.add("F");
		temp.add("M");
		List<String> genderList = config.getList("genderList.gender", temp);
		genders = renderAsOgnlList(genderList);
	}

	protected void prepareStationLineups(Configuration config) {
		ArrayList<String> lineupList = new ArrayList<String>();
		lineupList.add("Cable");
		lineupList.add("Digital Cable");
		lineupList.add("Satellite");
		stationLineups = renderAsOgnlList(lineupList);
	}
	
	protected String determineStationLineup(String lineup) {
		if (lineup.endsWith("DC")) {
			return "2";
		} else if (lineup.endsWith("S")) {
			return "3";
		} else {
			return "1";
		}
	}
	
	protected void extractStationLineup(User user) {
		if (user != null && user.getLineupId() != null) {
			String lineup = user.getLineupId();
			stationLineup = determineStationLineup(lineup);
		}
	}
	
	protected void extractBirthMonthYear(User user) {
		if (user != null && user.getBirthMonthYear() != null) {
			java.util.Date date = new java.util.Date(user.getBirthMonthYear().getTime());
			SimpleDateFormat format = new SimpleDateFormat();
			format.applyPattern("yyyy");
			birthYear = format.format(date);
			format.applyPattern("MMM");
			birthMonth = format.format(date).toUpperCase();
		}
	}
	
	protected String setBirthMonthYear(User user) {
		SimpleDateFormat format = new SimpleDateFormat();
		if (getBirthYear() != null && getBirthYear().length() == 4) {
			if (getBirthMonth() != null && getBirthMonth().length() == 3) {
				format.applyPattern("yyyy MMM");
				try {
					java.util.Date date = format.parse(getBirthYear()+" "+getBirthMonth());
					java.sql.Date birthMonthYear = new java.sql.Date(date.getTime());
					user.setBirthMonthYear(birthMonthYear);
				} catch (ParseException e) {
					addActionError(getText("username.bothmonthyear"));
				}
			} else {
				addActionError(getText("username.bothmonthyear"));
				return INPUT;
			}
		} else if (getBirthMonth() != null && getBirthMonth().length() == 3) {
			addActionError(getText("username.bothmonthyear"));
			return INPUT;
		}
		return SUCCESS;
	}
	
	protected void setSmsTime(User user) {
		user.setEarliestSmsTime(earliestSmsTime);
		user.setLatestSmsTime(latestSmsTime);
	}
	
	/**
	 * Used to render a list of strings in Ognl notation.  Seems that this should find a new 
	 * home rather soon, just not sure where to locate it yet.
	 * @param values
	 * @return
	 */
	private String renderAsOgnlList(List<String> values) {
		StringBuilder sb = new StringBuilder("{");
	    // This format is particular to webwork Velocity integration.  I don't like it, and
	    // we need a common home for formatting shit like this:
	    for (int i = 0; i < values.size(); i++) {
	    	sb.append('\'');
	    	sb.append(values.get(i));
	    	sb.append('\'');
	    	if (i < values.size() - 1) {
	    		sb.append(',');
	    	}
	    }
	    sb.append('}');
	    return sb.toString();
	}

	
	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	public String getStates() {
		return states;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	protected User getPopulatedUser(boolean newRegSecret) {
		User user = getUser();
		/*
		if (getAgeGroup() != null) {
			String rangeValue = getAgeMapper().get(getAgeGroup());
			if (rangeValue != null) {
				String[] values = rangeValue.split("-");
				int low = Integer.parseInt(values[0]);
				int high = Integer.parseInt(values[1]);
				user.setLowAge(low);
				user.setHighAge(high);
			}
		}
		*/
		if (newRegSecret) {
			// Assuming a fair amount of regex validation has passed at this point, go ahead
			// and insert the new user, with his registration secret so he can verify.
			user.setRegistrationSecret(user.createRegistrationSecret());
		}
		return user;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
		extractStationLineup(user);
		extractBirthMonthYear(user);
		/*
		extractSmsTime(user);
		*/
	}

	protected void extractSmsTime(User user) {
		earliestSmsTime = user.getEarliestSmsTime();
		latestSmsTime = user.getLatestSmsTime();
	}

	public String getGenders() {
		return genders;
	}

	public void setGenders(String genders) {
		this.genders = genders;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	protected Map<String, String> getAgeMapper() {
		return ageMapper;
	}

	public String getRegistrationSecret() {
		return registrationSecret;
	}

	public void setRegistrationSecret(String registrationSecret) {
		this.registrationSecret = registrationSecret;
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

	/**
	 * @return Returns the stationLineup.
	 */
	public String getStationLineup() {
		return stationLineup;
	}

	/**
	 * @param stationLineup The stationLineup to set.
	 */
	public void setStationLineup(String stationLineup) {
		this.stationLineup = stationLineup;
	}

	
	/**
	 * @return Returns the stationLineups.
	 */
	public String getStationLineups() {
		return stationLineups;
	}

	/**
	 * @param stationLineups The stationLineups to set.
	 */
	public void setStationLineups(String stationLineups) {
		this.stationLineups = stationLineups;
	}

	public String getMonths() {
		return months;
	}

	public void setMonths(String months) {
		this.months = months;
	}

	public String getYears() {
		return years;
	}

	public void setYears(String years) {
		this.years = years;
	}

	public String getBirthMonth() {
		return birthMonth;
	}

	public void setBirthMonth(String birthMonth) {
		this.birthMonth = birthMonth;
	}

	public String getBirthYear() {
		return birthYear;
	}

	public void setBirthYear(String birthYear) {
		this.birthYear = birthYear;
	}

	public String getEarliestSmsTime() {
		if (earliestSmsTime == null) {
			return "8:00 AM";
		}
		DateFormat format = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
		return format.format(earliestSmsTime);
	}

	public void setEarliestSmsTime(String earliestSmsTime) {
		DateFormat format = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
		try {
			this.earliestSmsTime = new Time(format.parse(earliestSmsTime).getTime());
		} catch (ParseException e) {
			log.error("Format from account_macros.vm was wrong", e);
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public String getLatestSmsTime() {
		if (latestSmsTime == null) {
			return "8:00 PM";
		}
		DateFormat format = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
		return format.format(latestSmsTime);
	}

	public void setLatestSmsTime(String latestSmsTime) {
		DateFormat format = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
		try {
			this.latestSmsTime = new Time(format.parse(latestSmsTime).getTime());
		} catch (ParseException e) {
			log.error("Format from account_macros.vm was wrong", e);
		}
	}

	public boolean isAdministrativeAction() {
		return administrativeAction;
	}

	public void setAdministrativeAction(boolean administrativeAction) {
		this.administrativeAction = administrativeAction;
	}
	
}
