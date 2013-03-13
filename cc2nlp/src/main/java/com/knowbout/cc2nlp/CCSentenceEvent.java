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

import com.knowbout.epg.service.ScheduledProgram;


/**
 * @author Jake Fear
 * @author Rich Kadel
 * @author $Author$
 * @version $Rev$ $Date$
 */
public class CCSentenceEvent extends CCEvent implements Serializable {

	/**
	 * A unique ID for serialization.
	 */
	private static final long serialVersionUID = 6877038674283926746L;
	private String speakerChange;
	private String sentence;
	private String fileDate;
	/**
	 * Creates an empty event to fill.
	 */
	public CCSentenceEvent() {
	}
	
	/**
	 * Creates an event with the given values.
	 * @param lineupId the identifier for the callsign provider.
	 * @param callsign the TV callsign from which these captions were read.
	 * @param timestamp the system time at which this event was generated, which
	 * should be immediately after the capture software recognized the end of
	 * a sentence.
	 * @param sentence the sentence captured
	 */
	public CCSentenceEvent(String lineupId, String callsign, ScheduledProgram scheduledProgram,
			long timestamp, String speakerChange, String sentence) {
		super(lineupId, callsign, scheduledProgram, timestamp);
		this.sentence = sentence;
		this.speakerChange = speakerChange;
	}
	
	/**
	 * Creates an event with the given values lineupId and callsign.  The other values
	 * need to be set before sending this event.  This creates and event that
	 * can easily be reused by setting and changing the values.
	 * @param lineupId the identifier for the callsign provider.
	 * @param callsign the TV callsign from which these captions were read.
	 */
	public CCSentenceEvent(String lineupId, String callsign) {
		super(lineupId, callsign);
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public String getSpeakerChange() {
		return speakerChange;
	}

	public void setSpeakerChange(String speakerChange) {
		this.speakerChange = speakerChange;
	}
	
	public String getFileDate() {
		return fileDate;
	}

	public void setFileDate(String fileDate) {
		this.fileDate = fileDate;
	}
	
	public String toString() {
		return getTimestamp()+"] "+speakerChange+": "+sentence;
	}
}
