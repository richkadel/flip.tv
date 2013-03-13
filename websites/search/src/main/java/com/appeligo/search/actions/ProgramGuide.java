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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeMap;

import com.appeligo.alerts.PendingAlert;
import com.appeligo.search.entity.User;
import com.knowbout.epg.service.Network;
import com.knowbout.epg.service.ScheduledProgram;

public class ProgramGuide {
	
	private boolean singleStation = true;
	private User user;
	private TimeZone timeZone;
	private Collection<List<ProgramEntry>> guide;
	private int alertCount;
	
	/**
	 * @param user - may be null
	 * @param programs
	 */
	public ProgramGuide(User user, TimeZone timeZone, List<ScheduledProgram> programs) {
		this.user = user;
		this.timeZone = timeZone;
		TreeMap<Integer, List<ProgramEntry>> guideData = new TreeMap<Integer, List<ProgramEntry>>();
		ProgramEntryComparator sorter = new ProgramEntryComparator();
		Calendar cal = Calendar.getInstance(timeZone);
		Network network = null;
		int entryId = 0;
		for(ScheduledProgram program: programs) {
			Integer key = getKey(program.getStartTime(), cal);
			if (network == null) {
				network = program.getNetwork();
			} else if (singleStation) {
				singleStation = network.getId() == program.getNetwork().getId();
			}
			List<ProgramEntry> day = guideData.get(key);
			if (day == null) {
				day = new ArrayList<ProgramEntry>();
				guideData.put(key, day);
			}			
			day.add(new ProgramEntry(entryId++, program));
			Collections.sort(day, sorter);
		}
		guide = guideData.values();
		
		if (user != null) {
			List<PendingAlert> pendingAlerts = PendingAlert.getManualAlertsForUser(user);
			
			for (List<ProgramEntry> day : guide) {
				for (ProgramEntry programEntry : day) {
					ScheduledProgram scheduledProgram = programEntry.getScheduledProgram();
					for (PendingAlert pendingAlert : pendingAlerts) {
						if (pendingAlert.isManual() &&
								pendingAlert.getProgramId().equals(scheduledProgram.getProgramId()) &&
								pendingAlert.getCallSign().equals(scheduledProgram.getNetwork().getStationCallSign()) &&
								pendingAlert.getProgramStartTime().getTime() == scheduledProgram.getStartTime().getTime()) {
							programEntry.setPendingAlert(pendingAlert);
							alertCount++;
						}
					}
				}
			}
		}
	}
	
	public Collection<List<ProgramEntry>> getSchedule() {
		return guide;
	}
	
	private Integer getKey(Date date, Calendar cal) {
		cal.setTime(date);
		return new Integer(cal.get(Calendar.DAY_OF_YEAR));
	}
	
	/**
	 * @return Returns the singleStation.
	 */
	public boolean isSingleStation() {
		return singleStation;
	}

	/**
	 * @param singleStation The singleStation to set.
	 */
	public void setSingleStation(boolean singleStation) {
		this.singleStation = singleStation;
	}
	
	public static class ProgramEntry {
		
		private int id;
		private ScheduledProgram scheduledProgram;
		private PendingAlert pendingAlert;

		public ProgramEntry(int id, ScheduledProgram scheduledProgram) {
			this.id = id;
			this.scheduledProgram = scheduledProgram;
		}
		
		public int getId() {
			return id;
		}
		
		public ScheduledProgram getScheduledProgram() {
			return scheduledProgram;
		}
		
		public void setPendingAlert(PendingAlert pendingAlert) {
			this.pendingAlert = pendingAlert;
		}
		
		public PendingAlert getPendingAlert() {
			return pendingAlert;
		}
	}

	private static class ProgramEntryComparator implements Comparator<ProgramEntry> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(T, T)
		 */
		public int compare(ProgramEntry o1, ProgramEntry o2) {
			int compare =  o1.getScheduledProgram().getStartTime().compareTo(o2.getScheduledProgram().getStartTime());
			if (compare == 0) {
				long diff = o1.getScheduledProgram().getScheduleId() - o2.getScheduledProgram().getScheduleId();
				if (diff > 0 ) {
					return 1;
				} if (diff < 0) {
					return -1;
				} else {
					return 0;
				}
			} else {
				return compare;
			}
		}
	}

	public int getAlertCount() {
		return alertCount;
	}
}
