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

package com.appeligo.channelfeed.work;

import java.io.EOFException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


import com.knowbout.cc2nlp.CCSentenceEvent;
import com.knowbout.cc2nlp.CCXDSEvent;
import com.knowbout.cc2nlp.CaptionTypeChangeEvent;
import com.knowbout.cc2nlp.ITVLinkEvent;
import com.knowbout.cc4j.CaptionType;
import com.knowbout.cc4j.DataChannel;
import com.knowbout.cc4j.ITVLink;
import com.knowbout.cc4j.LineBuffer;
import com.knowbout.cc4j.SentenceBuffer;
import com.knowbout.cc4j.VBICommand;
import com.knowbout.cc4j.VBILine;
import com.knowbout.cc4j.VideoDevice;
import com.knowbout.cc4j.XDSData;
import com.knowbout.cc4j.XDSField;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.ScheduledProgram;

public abstract class CaptionReaderThread extends Thread {
	
	private static final Logger log = Logger.getLogger(CaptionReaderThread.class);
	
	public static final long SCHEDULE_VARIANCE = 2*60*1000; // check 2 minutes in just in case they're late

	private EPGProvider epgService;
    private Destinations destinations;
    
    private ScheduledProgram program;
    private boolean aborted;
	private boolean printedConnectError = false;
	private boolean printedProgramError = false;
    
	public CaptionReaderThread(String name) {
		super(name);
	}

	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

	public EPGProvider getEpgService() {
		return epgService;
	}

	public void setEpgService(EPGProvider epgService) {
		this.epgService = epgService;
	}
	
	protected ScheduledProgram setProgram(long timestamp) {
		
		Date delayedTime = new Date(timestamp+SCHEDULE_VARIANCE); // check 2 minutes in just in case they're late
		
		DateFormat df = null;
        if (log.isDebugEnabled()) {
    		df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG);
    		log.debug("openDoc -> timestamp = "+timestamp);
    		df.setTimeZone(TimeZone.getTimeZone("GMT"));
    		log.debug(df.format(new Date(timestamp)));
    		df.setTimeZone(TimeZone.getTimeZone("PST"));
    		log.debug(df.format(new Date(timestamp)));
        }

		ScheduledProgram scheduledProgram = null;
		
        try {
			scheduledProgram = epgService.getScheduledProgramByNetworkCallSign(destinations.getLineupID(), destinations.getCallsign(), delayedTime);
			if (scheduledProgram == null) {
            	if (!printedProgramError) {
    				log.fatal("INVALID! Got null program from EPG!  "+
                                "At time "+df.format(delayedTime)+"\n"+
                                "Dropping events but will keep trying. "+
                                "Lineup ID: "+destinations.getLineupID()+", Callsign: "+destinations.getCallsign());
                    printedProgramError = true;
            	}
			} else if (scheduledProgram.getEndTime().getTime() == 0) {
            	if (!printedProgramError) {
    				log.fatal("INVALID! Got an invalid program from EPG (end time is zero - 0)!  "+
                                "At program start time "+df.format(scheduledProgram.getStartTime())+"\n"+
                                "for program "+scheduledProgram.getProgramId()+": "+scheduledProgram.getProgramTitle()+
                                "Dropping events but will keep trying. "+
                                "Lineup ID: "+destinations.getLineupID()+", Callsign: "+destinations.getCallsign());
                    printedProgramError = true;
            		scheduledProgram = null;
            	}
			}
        } catch (Throwable t) {
        	if (!printedConnectError) {
                log.error("Can't connect to EPG yet, to get program information.  Dropping events, but will keep trying. Lineup ID: "+
                		destinations.getLineupID()+", Callsign: "+destinations.getCallsign());
                // not printing stack trace by adding "t" parameter to log.error call.
                printedConnectError = true;
        	}
        }
        
        if (scheduledProgram != null) {
            if (printedConnectError || printedProgramError) {
                log.error("Finally connected to EPG and got a valid program information for "+
                		destinations.getLineupID()+", Callsign: "+destinations.getCallsign());
                printedProgramError = false;
                printedConnectError = false;
            }
        		
            if (log.isDebugEnabled()) {
        		log.debug("Lineup ID: "+destinations.getLineupID()+", Callsign: "+destinations.getCallsign());
        		log.debug("Program name: "+scheduledProgram.getProgramTitle());
        		log.debug("Next Program Start Time:");
        		df.setTimeZone(TimeZone.getTimeZone("GMT"));
        		log.debug(df.format(new Date(scheduledProgram.getEndTime().getTime())));
        		df.setTimeZone(TimeZone.getTimeZone("PST"));
        		log.debug(df.format(new Date(scheduledProgram.getEndTime().getTime())));
            }
        }
		setProgram(scheduledProgram);
		
		return scheduledProgram;
	}

	public Destinations getDestinations() {
		return destinations;
	}

	public void setDestinations(Destinations destinations) throws IOException {
		this.destinations = destinations;
	}

	public ScheduledProgram getProgram() {
		return program;
	}

	public void setProgram(ScheduledProgram program) {
		this.program = program;
		destinations.setProgram(program);
	}
}

