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

package com.knowbout.epg.processor;

import java.util.Calendar;
import java.util.Date;

public class ScheduleAiring implements Comparable {

	private ChannelSchedule schedule;
	private long key;
	private long stationId;
	private Date startTime;
	private Date endtime;
	private String inputText;		
	private int duration;
	private boolean pacificFeed;
	/**
	 * @param schedule
	 * @param stationId
	 * @param startTime
	 * @param endtime
	 * @param inputText
	 */
	public ScheduleAiring(ChannelSchedule schedule, long stationId, Date startTime, Date endtime, int duration, String inputText) {
		// TODO Auto-generated constructor stub
		this.schedule = schedule;
		this.stationId = stationId;
		this.startTime = startTime;
		this.endtime = endtime;
		this.inputText = inputText;
		this.duration = duration;
		//We only care about minutes/ so make that the key
		key = startTime.getTime()/(60*1000);
	}
	
	public boolean isPacificFeed() {
		return pacificFeed;
	}
	
	public void setPacficifFeed(boolean pacificFeed) {
		this.pacificFeed = pacificFeed;
	}
	/**
	 * @return Returns the endtime.
	 */
	public Date getEndtime() {
		return endtime;
	}
	/**
	 * @param endtime The endtime to set.
	 */
	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}
	/**
	 * @return Returns the inputText.
	 */
	public String getInputText() {
		return inputText;
	}
	/**
	 * @param inputText The inputText to set.
	 */
	public void setInputText(String inputText) {
		this.inputText = inputText;
	}
	/**
	 * @return Returns the schedule.
	 */
	public ChannelSchedule getSchedule() {
		return schedule;
	}
	/**
	 * @param schedule The schedule to set.
	 */
	public void setSchedule(ChannelSchedule schedule) {
		this.schedule = schedule;
	}
	/**
	 * @return Returns the startTime.
	 */
	public Date getStartTime() {
		return startTime;
	}
	/**
	 * @param startTime The startTime to set.
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	/**
	 * @return Returns the stationId.
	 */
	public long getStationId() {
		return stationId;
	}
	/**
	 * @param stationId The stationId to set.
	 */
	public void setStationId(long stationId) {
		this.stationId = stationId;
	}
	
	
	/**
	 * @return Returns the key.
	 */
	public long getKey() {
		return key;
	}
	/**
	 * @param key The key to set.
	 */
	public void setKey(long key) {
		this.key = key;
	}		
	
	/**
	 * @return Returns the duration.
	 */
	public int getDuration() {
		return duration;
	}
	/**
	 * @param duration The duration to set.
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(Object o) {
		if (o instanceof ScheduleAiring) {
			ScheduleAiring sa = (ScheduleAiring)o;
			long result = key - sa.key;
			if (result > 0) {
				return 1;
			} else if (result < 0 ) {
				return -1;
			} else {
				return 0;
			}
		}
		return 0;
	}
	
	
	
	
}
