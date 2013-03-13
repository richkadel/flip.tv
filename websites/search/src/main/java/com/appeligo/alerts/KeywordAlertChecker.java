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

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

import com.appeligo.epg.DefaultEpg;
import com.appeligo.search.entity.Message;
import com.appeligo.search.entity.MessageContextException;
import com.appeligo.search.entity.User;
import com.knowbout.epg.service.ProgramType;
import com.knowbout.epg.service.ScheduledProgram;
import com.knowbout.hibernate.HibernateUtil;

class KeywordAlertChecker {
	
	private static final Log log = LogFactory.getLog(KeywordAlertChecker.class);
	
	private String liveLineup;
	private String url;
	
	public KeywordAlertChecker(Configuration config) {
		liveLineup = config.getString("liveLineup");
		url = config.getString("url");
	}
	
	/**
	 * Returns true only if we are not supposed to send any more keyword alerts
	 * for this keyword today.
	 * @param keywordAlert
	 * @return false if ok to send the alert
	 */
	public boolean maxAlertsExceeded(KeywordAlert keywordAlert) {
		int maxAlertsPerDay = keywordAlert.getMaxAlertsPerDay();
		int todaysAlertCount = keywordAlert.getTodaysAlertCount();
		if (todaysAlertCount < maxAlertsPerDay) {
			return false;
		}
		Date lastAlertDay = keywordAlert.getLastAlertDay();
		if (lastAlertDay == null) {
			return false;
		}
		User user = keywordAlert.getUser();
		Date today = calculateDay(user.getTimeZone(), user.getEarliestSmsTime());
		// Since calculateDay() sets the Date timestamp to midnight on a day, the
		// Date represents the Date part only, and the time part for all Date objects
		// created by calculateDay() is equal.  So simply comparing by greater than
		// is guaranteed to return true if the days are different.  No need for
		// a more sophisticated check.
		if (compare(today, lastAlertDay, user.getTimeZone()) > 0) {
			return false;
		}
		return true;
	}
			
	/**
	 * You should call this within a transaction as it will change the
	 * number of alerts today (todaysAlertCount).
	 * Call this when you have decided to send an alert.  You should check that
	 * maxAlertsExceeded() returns false first.  This method does not do
	 * the check again because it would probably be redundant. Many times
	 * you want to do something in between checking maxAlertsExceeded() and
	 * actually incrementing the count.
	 * @param keywordAlert
	 */
	public void incrementTodaysAlertCount(KeywordAlert keywordAlert) {
		int todaysAlertCount = keywordAlert.getTodaysAlertCount();
		Date lastAlertDay = keywordAlert.getLastAlertDay();
		User user = keywordAlert.getUser();
		Date today = calculateDay(user.getTimeZone(), user.getEarliestSmsTime());
		// Since calculateDay() sets the Date timestamp to midnight on a day, the
		// Date represents the Date part only, and the time part for all Date objects
		// created by calculateDay() is equal.  So simply comparing by greater than
		// is guaranteed to return true if the days are different.  No need for
		// a more sophisticated check.
		if ((lastAlertDay == null) ||
			(compare(today, lastAlertDay, user.getTimeZone()) > 0)) {
			todaysAlertCount = 0;
		}
		todaysAlertCount++;
		
		keywordAlert.setLastAlertDay(today);
		keywordAlert.setTodaysAlertCount(todaysAlertCount);
		keywordAlert.save();
	}
			
	public boolean isNewMatch(KeywordAlert keywordAlert, Document doc) {
		
		String programId = doc.get("programID");
		if (KeywordMatch.getKeywordMatch(keywordAlert.getId(), programId) != null) {
			if (log.isDebugEnabled()) log.debug("This is not a new match, so keyword alert has already been sent.");
			return false;
		}
		
		Date endTime;
		try {
			String endTimeString = doc.get("lineup-"+liveLineup+"-endTime");
			if (endTimeString == null) {
				log.error("Software bug that 'endTime' for lineup "+liveLineup+" was not found for program id "+programId);
				return false;
			}
			endTime = DateTools.stringToDate(endTimeString);
		} catch (ParseException e) {
			log.error("Software bug resulted in exception with document 'endTime' format in lucene document for program id "+programId, e);
			return false;
		}
			
		new KeywordMatch(keywordAlert, programId, endTime).insert();
		
		return true;
	}
			
	public void sendMessages(KeywordAlert keywordAlert, String fragments, Document doc, String messagePrefix) {
		
		User user = keywordAlert.getUser();
		if (user == null) {
			return;
		}
		String programId = doc.get("programID");
		String programTitle = doc.get("programTitle");
		
		if (log.isDebugEnabled()) log.debug("keywordAlert: "+keywordAlert.getUserQuery()+", sending message to "+(user==null?null:user.getUsername()));
		
		try {
			// Use the user's lineup to determine the start time of this program which might air at different times for diff timezones
			String startTimeString = doc.get("lineup-"+user.getLineupId()+"-startTime");
			if (startTimeString == null) {
				// This user doesn't have the channel or program that our local feed has
				if (log.isDebugEnabled()) {
        			String station = doc.get("lineup-"+liveLineup+"-stationName");
    				log.debug("No startTime for station "+station+", program "+programTitle+", lineup="+user.getLineupId()+", start time from live lineup="+doc.get("lineup-"+liveLineup+"-startTime"));
				}
				return;
			}
			Date startTime = DateTools.stringToDate(startTimeString);
			Date endTime = DateTools.stringToDate(doc.get("lineup-"+user.getLineupId()+"-endTime"));
			long durationMinutes = (endTime.getTime() - startTime.getTime()) / (60*1000);
			
    		Date now = new Date();
    		boolean future = endTime.after(now);
    		boolean onAirNow = startTime.before(now) && future;
    		boolean past = !(future || onAirNow);

			ProgramType programType = ProgramType.fromProgramID(programId);

			boolean uniqueProgram = false;
			if (programType == ProgramType.EPISODE ||
					programType == ProgramType.SPORTS ||
					programType == ProgramType.MOVIE) {
				uniqueProgram = true;
			}
			
			Map<String, String> context = new HashMap<String, String>();

			boolean includeDate;
			DateFormat format;
			if (Math.abs(startTime.getTime() - System.currentTimeMillis()) < 12*60*60*1000) {
				format = DateFormat.getTimeInstance(DateFormat.SHORT);
				includeDate = false;
			} else {
				format = new SimpleDateFormat("EEEE, MMMM d 'at' h:mm a");
				includeDate = true;
			}
			format.setTimeZone(user.getTimeZone());
			context.put("startTime", format.format(startTime));
			if (includeDate) {
    			format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    			format.setTimeZone(user.getTimeZone());
			}
			context.put("shortStartTime", format.format(startTime));
			
			context.put("durationMinutes", Long.toString(durationMinutes));
			// Use the SDTW-C lineup because this is how we know the right channel (station callsign) where we caught the
			// keyword.
			String stationName = doc.get("lineup-"+liveLineup+"-stationName");
			context.put("stationName", stationName);
			boolean sameStation = false;
			if (stationName.equals(doc.get("lineup-"+user.getLineupId()+"-stationName"))) {
				sameStation = true;
			}
			context.put("stationCallSign", doc.get("lineup-"+liveLineup+"-stationCallSign"));
			
			if (sameStation) {
				if (onAirNow) {
        			context.put("timeChannelIntro",
        		  		"We have been monitoring <b>"+stationName+
        		  		"</b>, and your topic was recently mentioned on the following program:");
				} else if (future) {
					if (uniqueProgram) {
						context.put("timeChannelIntro",
							"We are monitoring <b>"+stationName+
							"</b>, and your topic will be mentioned on the following program:");
					} else {
						context.put("timeChannelIntro",
							"We are monitoring <b>"+stationName+
							"</b>, and your topic was mentioned on <b>"+programTitle+
							"</b>. It may be mentioned when this program airs again:");
					}
				} else {
        			context.put("timeChannelIntro",
        		  		"We have been monitoring <b>"+stationName+
        		  		"</b>, and your topic was mentioned on a program that aired in your area in the past. "+
        		  		"You may have an opportunity to see this program in the future:");
				}
			} else {
				if (onAirNow) {
        			context.put("timeChannelIntro",
        		  		"We have been monitoring <b>"+programTitle+
        		  		"</b>, and your topic was recently mentioned:");
				} else if (future) {
					if (uniqueProgram) {
						context.put("timeChannelIntro",
							"We have been monitoring <b>"+programTitle+
							"</b>, and your topic was mentioned.  You may have an opportunity to catch this program when it airs again according to the following schedule:");
					} else {
						context.put("timeChannelIntro",
							"We have been monitoring <b>"+programTitle+
							"</b>, and your topic was mentioned.  This program will air again as follows, but the topics may or may not be the same:");
					}
				} else {
        			context.put("timeChannelIntro",
        		  		"We have been monitoring <b>"+programTitle+
        		  		"</b>, and your topic was mentioned.  However, this program aired in your area in the past. "+
        		  		"You may have an opportunity to see this program in the future:");
				}
			}
			if (onAirNow) {
				context.put("startsAt", "Started at");
			} else if (future) {
				if (includeDate) {
					context.put("startsAt", "Starts on");
				} else {
					context.put("startsAt", "Starts at");
				}
			} else {
				if (includeDate) {
					context.put("startsAt", "Last aired on");
				} else {
					context.put("startsAt", "Previously aired at");
				}
			}
			context.put("lcStartsAt", context.get("startsAt").toLowerCase());

			String webPath = doc.get("webPath");
			if (webPath == null) {
				webPath = DefaultEpg.getInstance().getProgram(programId).getWebPath();
			}
			if (webPath.charAt(0) == '/') {
    			webPath = webPath.substring(1);
			}
			String reducedTitle40 = doc.get("reducedTitle40");
			if (reducedTitle40 == null) {
				reducedTitle40 = DefaultEpg.getInstance().getProgram(programId).getReducedTitle40();
			}
			String programLabel = doc.get("programLabel");
			if (programLabel == null) {
				programLabel = DefaultEpg.getInstance().getProgram(programId).getLabel();
			}
			context.put("programId", programId);
			context.put("webPath", webPath);
    		context.put("programLabel", programLabel);
    		context.put("reducedTitle40", reducedTitle40);
			if (doc.get("description").trim().length() > 0) {
				context.put("description", "Description: "+doc.get("description")+"<br/>");
			} else {
				context.put("description", "");
			}
			if (fragments == null || fragments.trim().length() == 0) {
				context.put("fragments", "");
			} else {
				context.put("fragments", "Relevant Dialogue: <i>"+fragments+"</i><br/>");
			}
			context.put("query", keywordAlert.getUserQuery());
			context.put("keywordAlertId", Long.toString(keywordAlert.getId()));
			String greeting = user.getUsername();
			context.put("username", greeting);
			String firstName = user.getFirstName();
			if (firstName != null && firstName.trim().length() > 0) {
				greeting = firstName;
			}
			context.put("greeting", greeting);
			
			format = DateFormat.getTimeInstance(DateFormat.SHORT);
			format.setTimeZone(user.getTimeZone());
			context.put("now", format.format(new Date()));
			
			ScheduledProgram futureProgram = DefaultEpg.getInstance().getNextShowing(user.getLineupId(), programId, false, false);
			if (uniqueProgram) {
				String typeString = null;
				if (programType == ProgramType.EPISODE) {
					typeString = "episode";
				} else if (programType == ProgramType.SPORTS) {
					typeString = "game";
				} else {
					typeString = "movie";
				}
    			if (futureProgram != null) {
    				String timePreposition = null;
					if ((futureProgram.getStartTime().getTime() - System.currentTimeMillis()) < 12*60*60*1000) {
        				timePreposition = "at ";
						format = DateFormat.getTimeInstance(DateFormat.SHORT);
					} else {
        				timePreposition = "on ";
						format = new SimpleDateFormat("EEEE, MMMM d 'at' h:mm a");
					}
					format.setTimeZone(user.getTimeZone());
    				context.put("rerunInfo",
    						"You can still catch this "+typeString+" in its entirety!  It's scheduled to replay "+timePreposition+
    						format.format(futureProgram.getStartTime())+" on "+futureProgram.getNetwork().getStationName()+
    						". Do you want to <a href=\""+
    						url+webPath+
    						"#addreminder\">set a reminder</a> to be notified the next time this "+typeString+" airs?");
    			} else {
    				if (programType == ProgramType.SPORTS) {
        				context.put("rerunInfo", "");
        			} else {
						if (onAirNow) {
							context.put("rerunInfo",
									"If it's too late to flip on the program now, you can <a href=\""+
									url+webPath+
									"#addreminder\">set a reminder</a> to be notified the next time this "+typeString+" airs.");
						} else {
							context.put("rerunInfo",
									"You can <a href=\""+
									url+webPath+
									"#addreminder\">set a reminder</a> to be notified the next time this "+typeString+" airs.");
						}
        			}
    			}
			} else {
				if ((futureProgram != null) && futureProgram.isNewEpisode()) {
    				context.put("rerunInfo", "The next airing of this show will be new content, and is <i>not a rerun</i>," +
    						" so these same topics may or may not be discussed."+
    						"  You may still be interested in catching future airings, and you can" +
    						" <a href=\""+url+webPath+"#addreminder\">set a Flip.TV reminder for this show</a>.");
				} else {
    				context.put("rerunInfo", "The broadcaster did not provide enough information to know which future airings," +
    						" if any, are identical reruns with the same topics mentioned." +
    						"  You may still be interested in catching future airings, and you can" +
    						" <a href=\""+url+webPath+"#addreminder\">set a Flip.TV reminder for this show</a>.");
				}
			}
			
			if (keywordAlert.getTodaysAlertCount() == keywordAlert.getMaxAlertsPerDay()) {
				context.put("maxAlertsExceededSentence",
						"You asked to stop receiving alerts for this topic after receiving "+
						keywordAlert.getMaxAlertsPerDay()+
						" alerts in a single day. That limit has been reached. You can change this setting"+
						" at any time.  Otherwise, we will resume sending alerts"+
						" for this topic tomorrow.");
			} else {
				context.put("maxAlertsExceededSentence", "");
			}
			
			if (keywordAlert.isUsingPrimaryEmailRealtime()) {
				Message message = new Message(messagePrefix+"_email", context);
				message.setUser(user);
				message.setTo(user.getPrimaryEmail());
				if (log.isDebugEnabled()) log.debug("Sending email message to: "+user.getPrimaryEmail());
				message.insert();
			}
			if (keywordAlert.isUsingSMSRealtime() && user.getSmsEmail().trim().length() > 0) {
				Message message = new Message(messagePrefix+"_sms", context);
				message.setTo(user.getSmsEmail());
				message.setUser(user);
				message.setSms(true);
				if (log.isDebugEnabled()) log.debug("Sending sms message to: "+user.getSmsEmail());
				message.insert();
			}
		} catch (NumberFormatException e) {
			log.error("Couldn't process lucene document for program "+programId, e);
		} catch (MessageContextException e) {
			log.error("Software bug resulted in exception with email message context or configuration", e);
		} catch (ParseException e) {
			log.error("Software bug resulted in exception with document 'startTime' or 'endTime' format in lucene document for program id "+programId, e);
		}
	}
	
	/**
	 * @param timeZone the user's timezone
	 * @param startOfDay We're using the value of earliestSmsTime (user account setting) as a starting point,
	 * so anything before this time is credited to the previous day)
	 * @return 12am (midnight) using system time (not user time) because "Date" objects stored in SQL
	 * come back in system time.
	 */
	private Date calculateDay(TimeZone timeZone, Time startOfDay) {
		Calendar cal = Calendar.getInstance(timeZone);
		Calendar start = Calendar.getInstance();
		start.setTimeInMillis(startOfDay.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 0-start.get(Calendar.HOUR_OF_DAY));
		cal.add(Calendar.MINUTE, 0-start.get(Calendar.MINUTE));
		cal.add(Calendar.SECOND, 0-start.get(Calendar.SECOND));
		cal.clear(Calendar.MILLISECOND);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.HOUR_OF_DAY);
		cal.clear(Calendar.HOUR);
		cal.clear(Calendar.AM_PM);
		TimeZone systemTimeZone = TimeZone.getDefault();
		long now = System.currentTimeMillis();
		long difference = systemTimeZone.getOffset(now) - timeZone.getOffset(now);
		return new Date(cal.getTimeInMillis() - difference);
	}
	
	private int compare(Date left, Date right, TimeZone timeZone) {
		Calendar cal = Calendar.getInstance(timeZone);
		cal.setTime(left);
		int leftYear = cal.get(Calendar.YEAR);
		int leftDay = cal.get(Calendar.DAY_OF_YEAR);
		cal.setTime(right);
		int rightYear = cal.get(Calendar.YEAR);
		int rightDay = cal.get(Calendar.DAY_OF_YEAR);
		if (leftYear < rightYear) {
			return -1;
		}
		if (leftYear > rightYear) {
			return 1;
		}
		if (leftDay < rightDay) {
			return -1;
		}
		if (leftDay > rightDay) {
			return 1;
		}
		return 0;
	}
}
