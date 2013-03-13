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

import com.knowbout.cc4j.ITVLink;
import com.knowbout.epg.service.ScheduledProgram;

/**
 * @author Jake Fear
 * @author Rich Kadel
 * @author $Author$
 * @version $Rev$ $Date$
 */
public class ITVLinkEvent extends CCEvent implements Serializable {

	private static final long serialVersionUID = -607863981372213815L;
	private ITVLink itvLink;
	
	/**
	 * Creates an empty event to fill.
	 */
	public ITVLinkEvent() {
	}
	
	/**
	 * Creates an event with the given values.
	 * @param headendID the identifier for the callsign provider.
	 * @param callsign the TV callsign from which these captions were read.
	 * @param timestamp the system time at which this event was generated, which
	 * should be immediately after the capture software recognized the end of
	 * a xdsField.
	 * @param xdsField the xdsField captured
	 */
	public ITVLinkEvent(String headendID, String callsign, ScheduledProgram scheduledProgram, long timestamp,
			ITVLink itvLink) {
		super(headendID, callsign, scheduledProgram, timestamp);
		this.itvLink = itvLink;
	}
	
	/**
	 * Creates an event with the given values headendID and callsign.  The other values
	 * need to be set before sending this event.  This creates and event that
	 * can easily be reused by setting and changing the values.
	 * @param headendID the identifier for the callsign provider.
	 * @param callsign the TV callsign from which these captions were read.
	 */
	public ITVLinkEvent(String headendID, String callsign) {
		super(headendID, callsign);
	}

	public ITVLink getITVLink() {
		return itvLink;
	}

	public void setITVLink(ITVLink itvLink) {
		this.itvLink = itvLink;
	}
	
	public String toString() {
		return "ITV Link="+itvLink;
	}
}
