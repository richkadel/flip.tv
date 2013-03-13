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

public abstract class VBIReaderThread extends CaptionReaderThread {
	
	private static final Logger log = Logger.getLogger(VBIReaderThread.class);
	
	public VBIReaderThread(String name) {
		super(name);
	}

	public void run() {
		
    	ProcessStats.addCallsign(getDestinations().getCallsign());
		
		SentenceBuffer sentenceBuffer = new SentenceBuffer() {
	
			@Override
			public void sentenceReady(String speakerChange, String sentence, int errors) {
	            if (errors > 0) {
	            	log.debug("Errors: "+errors);
	            	ProcessStats.error(getDestinations().getCallsign(), errors);
		            if (errors > (sentence.length() / 5)) {
		            	log.debug("Too many errors. Ignoring sentence.");
		            	return;
		            }
	            }
            	ProcessStats.sentence(getDestinations().getCallsign());
	            
	            long timestamp = System.currentTimeMillis();
	            
				if ((getProgram() == null) ||
					(timestamp > getProgram().getEndTime().getTime())) {
            		setProgram(timestamp);
				}
				

				if (getProgram() != null) {
		            getDestinations().writeSentence(getProgram(), timestamp,
							speakerChange, sentence);
				}
			}
			
			@Override 
			public void updatedXDS(VBILine vbi, XDSData xds, XDSField change) {
				if (log.getLevel() == Level.DEBUG) {
					log.debug("XDS");
				}
				log.debug(change+" = ");
				try {
					switch (change) {
					case PROGRAM_START_TIME_ID:
						log.debug(xds.getProgramStartTimeID());
						break;
						
					case PROGRAM_LENGTH:
						log.debug(xds.getProgramLength());
						break;
						
					case PROGRAM_NAME:
						log.debug(xds.getProgramName());
						break;
						
					case PROGRAM_TYPE:
						log.debug(xds.getProgramType()+": "+
								XDSData.convertProgramType(xds.getProgramType()));
						break;
						
					case NETWORK_NAME:
						log.debug(xds.getNetworkName());
						break;
						
					case CALL_LETTERS_AND_NATIVE_CHANNEL:
						log.debug(xds.getCallLettersAndNativeChannel());
						break;
						
					case CAPTION_SERVICES:
						log.debug(xds.getCaptionServices()+": "+
								XDSData.convertCaptionServices(xds.getCaptionServices()));
						break;
						
					default:
						log.debug("NOT HANDLED IN TEST!");
					}
				} catch (Exception e) {
					//ignore conversion errors... probably bad data... such is XDS
				}
				if (getProgram() != null) {
    	            getDestinations().writeXDS(getProgram(), System.currentTimeMillis(), xds, change);
				}
			}
			
			@Override
			public void error(VBILine vbi, String errstr) {
				if (isSelectedMode(vbi)) {
	            	log.debug("Error: "+errstr);
				}
			}
		};
		VBICommand itvBuffer = new LineBuffer(DataChannel.TEXT2) {
	
			private CaptionType captionType;
	
			@Override
			public void lineReady(String s) {
			}
			
			@Override
			public void rollUpCaption(VBILine vbi, int rows) {
				setCaptionType(CaptionType.ROLLUP);
				super.rollUpCaption(vbi, rows);
			}
			
			@Override
			public void resumeCaption(VBILine vbi) {
				setCaptionType(CaptionType.POPON);
				super.resumeCaption(vbi);
			}
			
			@Override
			public void endOfCaption(VBILine vbi) {
				setCaptionType(CaptionType.POPON);
				super.endOfCaption(vbi);
			}
			
			@Override
			public void resumeDirect(VBILine vbi) {
				setCaptionType(CaptionType.PAINTON);
				super.resumeDirect(vbi);
			}
			
			private void setCaptionType(CaptionType captionType) {
				if (captionType != this.captionType) {
					if (getProgram() != null) {
						this.captionType = captionType;
			            getDestinations().writeCaptionTypeChange(getProgram(), System.currentTimeMillis(), captionType);
		            }
	            }
			}
	
			@Override
			public void receivedITVLink(ITVLink itvLink) {
				if (getProgram() != null) {
    	            getDestinations().writeITV(getProgram(), System.currentTimeMillis(), itvLink);
	            }
			}
			
			@Override
			public void error(VBILine vbi, String errstr) {
				if (isSelectedMode(vbi)) {
	            	log.debug("Error TEXT2: "+errstr);
				}
			}
		};
		
		setProgram(System.currentTimeMillis());
		
		int exceptions = 0;
		int maxExceptions = 10;
		long resetExceptionCounterMillis = 15 * 60 * 1000; // 15 minutes
		long lastException = 0;
        Level currentLogLevel = log.getLevel();
        boolean loggingOff = false;
		VBILine vbi = new VBILine();
		while (!isAborted()) {
	    	try {
                if (loggingOff) {
                    if (((System.currentTimeMillis() - lastException) >
	    					resetExceptionCounterMillis) &&
	    			      (exceptions < maxExceptions)) {
                        log.setLevel(currentLogLevel);
                        loggingOff = false;
		    			log.fatal("Exceptions seem to have calmed down. Logging is turned back on.");
	    			}
                } else if (exceptions > 0) {
	    			if ((System.currentTimeMillis() - lastException) >
	    					resetExceptionCounterMillis) {
	    				exceptions = 0;
	    			}
	    		}
                if (!readVBILine(vbi)) {
                	return;
                }
	        	vbi.executeCommand(sentenceBuffer);
	        	vbi.executeCommand(itvBuffer);
				getDestinations().writeRawVBI(vbi);
	    	} catch (Exception e) {
	    		log.error("Uncaught exception!", e);
	    		exceptions++;
				lastException = System.currentTimeMillis();
	    		if (exceptions > maxExceptions) {
	    			log.fatal("Exceptions too frequent. Turning all logging off until this gets back under control."); 
                    log.setLevel(Level.OFF);
                    loggingOff = true;
                    exceptions = 0;
	    			break;
	    		}
			}
		}
	}

	/**
	 * @param vbi
	 * @return true if successful, or false if we should abort (e.g., because of a closed socket)
	 * @throws IOException
	 */
	protected abstract boolean readVBILine(VBILine vbi) throws IOException;
}

