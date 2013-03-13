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

package com.knowbout.epg.service;

import java.io.Serializable;
import java.util.List;

public class ChannelSchedule implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8439642159170180856L;

	private StationChannel channel;
	private List<ScheduledProgram> schedule;
	
	public ChannelSchedule() {
	}

	/**
	 * @param channel
	 * @param schedule
	 */
	public ChannelSchedule(StationChannel channel, List<ScheduledProgram> schedule) {
		this.channel = channel;
		this.schedule = schedule;
	}

	/**
	 * @return Returns the channel.
	 */
	public StationChannel getChannel() {
		return channel;
	}

	/**
	 * @param channel The channel to set.
	 */
	public void setChannel(StationChannel channel) {
		this.channel = channel;
	}

	/**
	 * @return Returns the schedule.
	 */
	public List<ScheduledProgram> getSchedule() {
		return schedule;
	}

	/**
	 * @param schedule The schedule to set.
	 */
	public void setSchedule(List<ScheduledProgram> schedule) {
		this.schedule = schedule;
	}
	

	

}
