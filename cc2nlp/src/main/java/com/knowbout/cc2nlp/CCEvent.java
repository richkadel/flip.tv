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

package com.knowbout.cc2nlp;

import java.io.Serializable;
import java.util.Date;

import com.knowbout.epg.service.ScheduledProgram;

/**
 * @author Jake Fear
 * @author Rich Kadel
 * @author $Author$
 * @version $Rev$ $Date$
 */
public class CCEvent implements Serializable {

	private static final long serialVersionUID = -8159873530316184013L;
	
	private String lineupID;
	private String callsign;
	protected ScheduledProgram scheduledProgram;
	private long timestamp;

	public CCEvent() {
		super();
	}

	/**
	 * Creates an event with the given values.
	 * @param lineupID the identifier for the callsign provider.
	 * @param callsign the TV callsign from which these captions were read.
	 * @param timestamp the system time at which this event was generated, which
	 * should be immediately after the capture software recognized the end of
	 * a sentence.
	 * @param timestamp 
	 */
	public CCEvent(String lineupID, String callsign,
			ScheduledProgram scheduledProgram, long timestamp) {
		this.lineupID = lineupID;
		this.callsign = callsign;
		this.scheduledProgram = scheduledProgram;
		this.timestamp = timestamp;
	}
	
	/**
	 * Creates an event with the given values lineupID and callsign.  The other values
	 * need to be set before sending this event.  This creates and event that
	 * can easily be reused by setting and changing the values.
	 * @param lineupID the identifier for the callsign provider.
	 * @param callsign the TV callsign from which these captions were read.
	 */
	public CCEvent(String lineupID, String callsign) {
		this.lineupID = lineupID;
		this.callsign = callsign;
	}
	
	public String getLineupID() {
		return lineupID;
	}

	public void setLineupID(String lineupID) {
		this.lineupID = lineupID;
	}

	public String getCallsign() {
		return callsign;
	}

	public void setCallsign(String callsign) {
		this.callsign = callsign;
	}

	public long getProgramStartTime() {
		return scheduledProgram.getStartTime().getTime();
	}

	public void setProgramStartTime(long programStartTime) {
		scheduledProgram.setStartTime(new Date(programStartTime));
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	/**
	 * TODO: This really needs to include the lineup as well to fully identify a 
	 * given stream of CC data.  For now, since we only consume analog signals, this
	 * will suffice.
	 * 
	 * @return
	 */
	public String getIdentifier() {
		return lineupID + "-" + callsign;
	}

	public String toString() {
		return "lineupID:"+ lineupID +
		", callsign:" + callsign+
		", timestamp:" + timestamp;
	}

	public ScheduledProgram getScheduledProgram() {
		return scheduledProgram;
	}

	public void setScheduledProgram(ScheduledProgram scheduledProgram) {
		this.scheduledProgram = scheduledProgram;
	}
}