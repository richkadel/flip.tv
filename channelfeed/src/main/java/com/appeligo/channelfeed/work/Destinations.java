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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.appeligo.epg.util.EpgUtils;
import com.appeligo.util.Utils;
import com.knowbout.cc2nlp.CCSentenceEvent;
import com.knowbout.cc2nlp.CCXDSEvent;
import com.knowbout.cc2nlp.CaptionTypeChangeEvent;
import com.knowbout.cc2nlp.ITVLinkEvent;
import com.knowbout.cc2nlp.ProgramStartEvent;
import com.knowbout.cc4j.CaptionType;
import com.knowbout.cc4j.ITVLink;
import com.knowbout.cc4j.VBILine;
import com.knowbout.cc4j.XDSData;
import com.knowbout.cc4j.XDSField;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.ScheduledProgram;

/**
 * This class opens a VBI device, reads closed captioning, and sends it to
 * another module for processing, sentence-by-sentence.
 * @author Rich Kadel
 * @author $Author$
 * @version $Rev$ $Date$
 */
public class Destinations {

	private static final Logger log = Logger.getLogger(Destinations.class);

	public static final long SCHEDULE_VARIANCE = 2*60*1000; // check 2 minutes in just in case they're late

	private String ccDocumentRoot;

	private String headendID;
    private String lineupDevice;
    private String lineupID;
    private String callsign;
    private CookedQueue[] cookedQueues;
	private RawQueue[] rawQueues;
    
	private boolean sendXDS;

	private boolean sendITV;

	private String[] destinationURLs;
	private boolean[] destinationRaw;
	private FileWriter fileWriter;

	public Destinations() {
	}
	
	public String getCallsign() {
		return callsign;
	}

	public void setCallsign(String callsign) {
		this.callsign = callsign;
	}

	public String getHeadendID() {
		return headendID;
	}

	public void setHeadendID(String headendID) {
		this.headendID = headendID;
		lineupID = EpgUtils.getLineupID(headendID, lineupDevice);
	}

	public String getLineupDevice() {
		return lineupDevice;
	}

	public void setLineupDevice(String lineupDevice) {
		this.lineupDevice = lineupDevice;
		lineupID = EpgUtils.getLineupID(headendID, lineupDevice);
	}

	public String getLineupID() {
		return lineupID;
	}

	public boolean getSendXDS() {
		return sendXDS;
	}

	public void setSendXDS(boolean sendXDS) {
		this.sendXDS = sendXDS;
	}

	public boolean getSendITV() {
		return sendITV;
	}

	public void setSendITV(boolean sendITV) {
		this.sendITV = sendITV;
	}
	
	public String[] getDestinationURLs() {
		return destinationURLs;
	}
	
	public boolean[] getDestinationRaw() {
		return destinationRaw;
	}
	
	public void setDestinationURLs(String[] destinationURLs, boolean[] destinationRaw) {
		this.destinationURLs = destinationURLs;
		this.destinationRaw = destinationRaw;
	}

	public String getCaptionDocumentRoot() {
		return ccDocumentRoot;
	}

	public void setCaptionDocumentRoot(String ccDocumentRoot) {
		if (!ccDocumentRoot.endsWith("/")) {
			ccDocumentRoot += "/";
		}
		this.ccDocumentRoot = ccDocumentRoot;
	}
	
	public void connect() throws MalformedURLException {
		
		if (destinationURLs != null) {
			cookedQueues = new CookedQueue[destinationURLs.length];
			rawQueues = new RawQueue[destinationURLs.length];
			for (int i = 0; i < destinationURLs.length; i++) {
				if (destinationRaw[i]) {
					rawQueues[i] = new RawQueue(headendID, lineupDevice, callsign, destinationURLs[i]);
			    	rawQueues[i].start();
				} else {
			    	cookedQueues[i] = new CookedQueue("Captions "+lineupID+", callsign "+callsign, destinationURLs[i]);
			    	cookedQueues[i].start();
				}
			}
		}
		
		if (fileWriter != null) {
			fileWriter.setCcDocumentRoot(ccDocumentRoot);
			fileWriter.setLineupID(lineupID);
			fileWriter.setCallsign(callsign);
		}
	}
	
	public void disconnect() {
		for (int i = 0; i < destinationURLs.length; i++) {
			if (rawQueues[i] != null) {
            	rawQueues[i].setAborted(true);
			}
			if (cookedQueues[i] != null) {
            	cookedQueues[i].setAborted(true);
			}
        }
        //TODO close the connection to hessian?  Need to add synchronization so we
        //ensure the read loop doesn't try to send capture data on a closed connection
	}
	
	public void setProgram(ScheduledProgram program) {
		if (program != null) {
    		boolean errorMessage = false;
    		for (int i = 0; i < destinationURLs.length; i++) {
    			if (!destinationRaw[i]) {
        			if (cookedQueues[i] != null) {
        		        if (!cookedQueues[i].offer(new ProgramStartEvent(lineupID, callsign, program,
        		        		program.getStartTime().getTime()))) {
        		        	if (!errorMessage) {
            		        	log.fatal("Could not put event on sendQueue.  The captions are not getting to the indexer. Is the search webapp down?");
        		        		errorMessage = true;
        		        	}
        		        } else {
        		        	if (errorMessage) {
            		        	log.fatal("RESOLVED...Now able to put events on sendQueue");
        		        		errorMessage = false;
        		        	}
        		        }
    		        }
    			}
    		}
		}
		if (fileWriter != null) {
			fileWriter.setProgram(program);
		}
	}
	
	public void writeSentence(ScheduledProgram program, long timestamp, String speakerChange, String sentence) {
		
		log.debug(getCallsign()+": "+sentence);
		
		CCSentenceEvent sentenceEvent = new CCSentenceEvent(getLineupID(), getCallsign(), program,
				timestamp, speakerChange, sentence);
		sentenceEvent.setFileDate(Utils.getDatePath(program.getStartTime().getTime()));

		for (int i = 0; i < destinationURLs.length; i++) {
			if (!destinationRaw[i]) {
    			if (cookedQueues[i] != null) {
    	            if (!cookedQueues[i].offer(sentenceEvent)) {
    	            	log.error("Could not put event on sendQueue");
    	            }
	            }
			}
		}
		if (fileWriter != null) {
			fileWriter.write(sentenceEvent);
		}
	}
	
	public void writeCaptionTypeChange(ScheduledProgram program, long timestamp, CaptionType captionType) {
		if (program == null) {
			return;
		}
		
        CaptionTypeChangeEvent captionTypeChangeEvent = new CaptionTypeChangeEvent(
        		getLineupID(), getCallsign(), program, timestamp, captionType);
		for (int i = 0; i < destinationURLs.length; i++) {
			if (!destinationRaw[i]) {
    			if (cookedQueues[i] != null) {
    	            if (!cookedQueues[i].offer(captionTypeChangeEvent)) {
    	            	log.error("Could not put event on sendQueue");
    	            }
	            }
			}
		}
		if (fileWriter != null) {
			fileWriter.write(captionTypeChangeEvent);
		}
	}

	public void writeXDS(ScheduledProgram program, long timestamp, XDSData xds, XDSField change) {
		if (program == null) {
			return;
		}
		
		CCXDSEvent xdsEvent = new CCXDSEvent(
				getLineupID(), getCallsign(), program, timestamp, xds, change);
		if (getSendXDS()) {
    		for (int i = 0; i < destinationURLs.length; i++) {
    			if (!destinationRaw[i]) {
        			if (cookedQueues[i] != null) {
        	            if (!cookedQueues[i].offer(xdsEvent)) {
        	            	log.error("Could not put event on sendQueue");
        	            }
    	            }
    			}
    		}
		}
		if (fileWriter != null) {
			fileWriter.write(xdsEvent);
		}
	}

	public void writeITV(ScheduledProgram program, long timestamp, ITVLink itvLink) {
		if (program == null) {
			return;
		}
		
        ITVLinkEvent itvLinkEvent = new ITVLinkEvent(
        		getLineupID(), getCallsign(), program, timestamp, itvLink);
		if (getSendITV()) {
    		for (int i = 0; i < destinationURLs.length; i++) {
    			if (!destinationRaw[i]) {
        			if (cookedQueues[i] != null) {
        	            if (!cookedQueues[i].offer(itvLinkEvent)) {
        	            	log.error("Could not put event on sendQueue");
        	            }
    	            }
    			}
    		}
		}
		if (fileWriter != null) {
			fileWriter.write(itvLinkEvent);
		}
	}

	public void writeRawVBI(VBILine vbi) throws IOException {
		for (int i = 0; i < destinationURLs.length; i++) {
			if (destinationRaw[i]) {
    			if (rawQueues[i] != null) {
    				try {
        				rawQueues[i].write(vbi.getLineNumber(), vbi.getC1(), vbi.getC2());
    					if (rawQueues[i].isInError()) {
    						log.error("Recovered writing to raw queue at "+rawQueues[i].getName()+", but dropped "+rawQueues[i].getBytesDropped()+" bytes");
    						rawQueues[i].setInError(false);
    					}
    				} catch (IOException e) {
    					if (!rawQueues[i].isInError()) {
    						log.error("Can't write to raw queue at "+rawQueues[i].getName(), e);
							rawQueues[i].setBytesDropped(12);
    						rawQueues[i].setInError(true);
    					} else {
							rawQueues[i].setBytesDropped(
								rawQueues[i].getBytesDropped()+12);
						}
    				}
				}
			}
		}
	}
	
	public FileWriter getFileWriter() {
		return fileWriter;
	}

	public void setFileWriter(FileWriter fileWriter) {
		this.fileWriter = fileWriter;
	}
}
