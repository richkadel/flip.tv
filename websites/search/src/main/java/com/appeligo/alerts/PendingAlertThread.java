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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Transaction;

import com.appeligo.search.entity.Message;
import com.appeligo.search.entity.MessageContextException;
import com.appeligo.search.entity.Permissions;
import com.appeligo.search.entity.User;
import com.knowbout.epg.service.Program;
import com.knowbout.epg.service.ScheduledProgram;
import com.knowbout.hibernate.HibernateUtil;

class PendingAlertThread extends Thread {
	
	private static final Log log = LogFactory.getLog(PendingAlertThread.class);
	private AlertManager alertManager;
	private boolean isActive;
	private int maxConsecutiveExceptions;
	private String url;
	
	public PendingAlertThread(Configuration config) {
		super("PendingAlertThread");
		isActive = true;
		alertManager = AlertManager.getInstance();
        maxConsecutiveExceptions = config.getInt("maxConsecutiveExceptions", 10);
        url = config.getString("url", "http://localhost:8080");
	}
	
	public void run() {
		try {
			Permissions.setCurrentUser(Permissions.SUPERUSER);
			
			while (isActive()) {
				
				PendingAlert next = null;
				
				HibernateUtil.openSession();
				try {
					next = PendingAlert.getNextAlert();
				} finally {
					HibernateUtil.closeSession();
				}
			
				long delay = 0;
				if (next != null) {
					delay = next.getAlertTime().getTime() - System.currentTimeMillis();
					log.debug("current time="+System.currentTimeMillis()+", next alert time="+next.getAlertTime().getTime());
				}
				
				while (((delay > 0) || (next == null)) && isActive()) {
					log.debug("Waiting for next pending alert. next="+(next==null?null:next.getId())+", time before next alert="+delay);
					try {
						synchronized(this) {
							wait(delay); // value can only be zero (as initialized) or
										 // a positive value (as checked in while condition).
										 // wait(0) == wait forever
						}
					} catch (InterruptedException e) {
					}
					//need to see if there is a newer alert
					//(in which case, notifyAll was called), or the
					//alert we were waiting for was deleted
					delay = 0;
					HibernateUtil.openSession();
					try {
						next = PendingAlert.getNextAlert();
					} finally {
						HibernateUtil.closeSession();
					}
					if (next != null) {
						delay = next.getAlertTime().getTime() - System.currentTimeMillis();
						log.debug("current time="+System.currentTimeMillis()+", next alert time="+next.getAlertTime().getTime());
					}
				}
				
				if (isActive()) log.debug("Ready to fire off at least one pending alert. next="+next.getId()+", deleted="+next.isDeleted());
				
				HibernateUtil.openSession();
				List<PendingAlert> pendingAlerts = PendingAlert.getExpiredAlerts();
				try {
					if (isActive()) log.debug("checking next");
					for (PendingAlert pendingAlert : pendingAlerts) {
						if (!isActive()) {
							break;
						}
						log.debug("next");
						User user = null;
						int consecutiveExceptions = 0;
						Transaction transaction = HibernateUtil.currentSession().beginTransaction();
						try {
							if (pendingAlert.isDeleted()) {
								log.debug("deleted");
								continue;
							}
							ProgramAlert programAlert = pendingAlert.getProgramAlert();
							if (programAlert == null) { // then this should be a manual pending alert
								pendingAlert.setDeleted(true);
								pendingAlert.save();
								if (!pendingAlert.isManual()) {
									log.debug("no program alert");
									continue;
								}
							}
							user = pendingAlert.getUser();
							String programId = pendingAlert.getProgramId();
							if (user == null || programId == null) {
								log.debug("user ("+user+") can't be null and programId ("+programId+") can't be null");
								if (programAlert != null) {
									programAlert.setDeleted(true);
									programAlert.save();
								}
								continue;
							}
							ScheduledProgram scheduledProgram =
								alertManager.getEpg().
									getScheduledProgramByNetworkCallSign(pendingAlert.getUser().getLineupId(),
											pendingAlert.getCallSign(), pendingAlert.getProgramStartTime());
			        		String targetId;
			        		if (programAlert == null) {
				        		targetId = scheduledProgram.getProgramId();
			        		} else {
				        		targetId = programAlert.getProgramId();
				        			// not a manual alert, so this is the id for the reminder itself
			        		}
							Program targetProgram = alertManager.getEpg().getProgram(targetId);
							
							boolean invalidProgramAlert = false;
							
							if (scheduledProgram == null) {
								if (log.isDebugEnabled()) log.debug("no scheduled program");
							} else {
								if (!scheduledProgram.getProgramId().equals(programId)) {
									invalidProgramAlert = true;
									if (log.isDebugEnabled()) log.debug("Schedule must have changed...no matching programId: "+scheduledProgram.getProgramId()+" != "+programId);
								} else if (programAlert != null) { // Not a manual (quick) alert
									if (programAlert.isDisabled()) {
										invalidProgramAlert = true;
										if (log.isDebugEnabled()) log.debug("program alert disabled");
									} else if (programAlert.isDeleted()) {
										invalidProgramAlert = true;
										if (log.isDebugEnabled()) log.debug("program alert was deleted");
									}
								}
							}
							
							if ((scheduledProgram != null) &&
								(!invalidProgramAlert)) {
								
								boolean scheduledAlert = false;
								boolean episodeAlert = false;
								if (programAlert == null) {
									scheduledAlert = true;
								} else {
									episodeAlert = true;
								}
								
								log.debug("firing pending alert="+pendingAlert.getId());
							
					    		Date startTime = scheduledProgram.getStartTime();
					    		Date endTime = scheduledProgram.getEndTime();
					    		
				        		long durationMinutes = (endTime.getTime() - startTime.getTime()) / (60*1000);
				        		
								Map<String, String> context = new HashMap<String, String>();
								
								DateFormat format = null;
								DateFormat shortFormat = null;
								if ((startTime.getTime() - System.currentTimeMillis()) < 12*60*60*1000) {
									format = DateFormat.getTimeInstance(DateFormat.SHORT);
									shortFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
    								context.put("timePreposition", "at");
								} else {
									shortFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                    				format = new SimpleDateFormat("EEEE, MMMM d 'at' h:mm a");
    								context.put("timePreposition", "on");
								}
								format.setTimeZone(user.getTimeZone());
								shortFormat.setTimeZone(user.getTimeZone());
								context.put("startTime", format.format(startTime));
								context.put("shortStartTime", shortFormat.format(startTime));
								
								context.put("durationMinutes", Long.toString(durationMinutes));
				        		context.put("programId", scheduledProgram.getProgramId()); // don't use ID from programAlert
				        			// which may be a show, but this may be an episode, or may be a team, but this a game
                				String webPath = scheduledProgram.getWebPath();
                    			if (webPath.charAt(0) == '/') {
                        			webPath = webPath.substring(1);
                    			}
                    			context.put("webPath", webPath);
				        		context.put("targetId", targetId);
				        		context.put("programLabel", scheduledProgram.getLabel());
				        		if (targetId.startsWith("TE")) {
    				        		context.put("whatfor", "any game featuring the "+targetProgram.getTeamName());
    				        		context.put("reducedTitle40", targetProgram.getReducedTitle40());
				        		} else {
    				        		context.put("whatfor", "the following program");
    				        		context.put("reducedTitle40", scheduledProgram.getReducedTitle40());
				        		}
								if (scheduledProgram.getDescription().trim().length() > 0) {
									context.put("description", "Description: "+scheduledProgram.getDescription()+"<br/>");
								} else {
									context.put("description", "");
								}
				        		context.put("stationName", scheduledProgram.getNetwork().getStationName());
								String greeting = user.getUsername();
								context.put("username", greeting);
								String firstName = user.getFirstName();
								if (firstName != null && firstName.trim().length() > 0) {
									greeting = firstName;
								}
								context.put("greeting", greeting);
                				String targetWebPath = targetProgram.getWebPath();
                    			if (targetWebPath.charAt(0) == '/') {
                        			targetWebPath = targetWebPath.substring(1);
                    			}
								context.put("showDetailsLink", 
									"<a href=\""+url+targetWebPath+"#reminders\">"+
												 url+targetWebPath+"#reminders</a>");
								if (episodeAlert) {
									context.put("deleteRemindersSentence",
									  		"Did you already see this program? If you're done with this reminder, "+
											"<a href=\""+url+"reminders/deleteProgramReminders?programId="+targetId+
												"\">click here to delete the reminders for this program</a>.");
									/*
  		LATER, REPLACE THE LAST PARAGRAPH ABOVE WITH...
		  		<p>
		  		Did you already see this program?  <a href="${url}reminders/deleteProgramReminders?programId=@programId@">Click
		  		here to delete the reminders for this program</a>\, and if you don't mind\, tell us what you think.
		  		</p>
		  		AND PUT A SIMILAR MESSAGE WITHOUT THE DELETE BUT WITH A LINK IN IF NOT EPISODEREMINDER, in "ELSE" BELOW
							  		  */
								} else {
									context.put("deleteRemindersSentence", "");
								}
								if ((programAlert != null && programAlert.isUsingPrimaryEmail()) ||
										user.isUsingPrimaryEmailDefault()) {
									Message message = new Message("program_reminder_email", context);
									if ((startTime.getTime() - System.currentTimeMillis()) > 4*60*60*1000) {
										message.setPriority(2);
									}
									message.setUser(user);
									message.setTo(user.getPrimaryEmail());
									message.insert();
								}
								if ((programAlert != null && programAlert.isUsingSMS() && user.getSmsEmail().trim().length() > 0) ||
										user.isUsingSMSDefault()) {
									Message message = new Message("program_reminder_sms", context);
									if ((startTime.getTime() - System.currentTimeMillis()) > 4*60*60*1000) {
										message.setPriority(2);
									}
									message.setUser(user);
									message.setTo(user.getSmsEmail());
									message.setSms(true);
									message.insert();
								}
							}
				        	consecutiveExceptions = 0;
						} catch (MessageContextException e) {
							log.error("Software bug resulted in exception with email message context or configuration", e);
						} catch (Throwable t) {
							log.error("Could not complete pending alert execution", t);
							transaction.rollback();
							log.error("Caught throwable on pendingAlert "+pendingAlert.getId()+
									", user "+((user==null)?null:user.getUsername()), t);
							consecutiveExceptions++;
							if (consecutiveExceptions >= maxConsecutiveExceptions) {
								log.fatal("Reached max consecutive exceptions for PendingAlertThread. Exiting thread immediately.");
								return;
							}
						} finally {
							if (transaction.wasRolledBack()) {
								transaction = HibernateUtil.currentSession().beginTransaction();
							}
							try {
								log.debug("marking pending alert="+pendingAlert.getId()+" as fired. Should see commit message next.");
								pendingAlert.setFired(true);
								pendingAlert.save();
								log.debug("committing after marking pending alert");
							} catch (Throwable t) {
								log.error("Coult not mark pending alert", t);
							} finally {
								transaction.commit();
							}
						}
					}
			
					// Now that we aren't looping on PendingAlerts, I should be able to safely
					// delete all of the PendingAlerts flagged as deleted without screwing up
					// paging (if we decide to implement paging up above)
					Transaction transaction = HibernateUtil.currentSession().beginTransaction();
					try {
						log.debug("deleting marked-deleted pending alerts");
						PendingAlert.deleteAllMarkedDeleted();
						log.debug("deleted marked-deleted pending alerts");
						log.debug("deleting marked-fired pending alerts if program has started");
						PendingAlert.deleteOldFired();
						log.debug("deleted marked-deleted pending alerts");
					} finally {
						transaction.commit();
					}
				} finally {
					HibernateUtil.closeSession();
				}
			}
		} catch (Throwable t) {
			log.fatal("Caught unexpected exception, causing abort of thread!", t);
		}
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}