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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.appeligo.epg.util.EpgUtils;
import com.knowbout.cc2nlp.CCSentenceEvent;
import com.knowbout.cc2nlp.CCXDSEvent;
import com.knowbout.cc2nlp.CaptionTypeChangeEvent;
import com.knowbout.cc2nlp.ITVLinkEvent;
import com.knowbout.cc2nlp.ProgramStartEvent;
import com.appeligo.util.Utils;
import com.knowbout.cc4j.CaptionType;
import com.knowbout.cc4j.ITVLink;
import com.knowbout.cc4j.XDSData;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.ScheduledProgram;

public class FileWriter {

	private static final Logger log = Logger.getLogger(FileWriter.class);

	private String ccDocumentRoot;
    private String lineupID;
    private String callsign;
	private ScheduledProgram program;
	private String filenamePrefix;
    
    private CaptionType currentCaptionType = null;
    private CaptionType newCaptionType = CaptionType.ROLLUP;

	private PrintStream doc;
	private PrintStream body;

	private XDSData xdsData;

	public FileWriter() {
	}
	
	public void write(CCSentenceEvent sentenceEvent) {
		ProcessStats.checkStats(ccDocumentRoot);
		if (body == null) {
			return;
		}
		if (currentCaptionType != newCaptionType) {
			body.println("</div>");
			currentCaptionType = newCaptionType;
			body.println("<div class=\""+currentCaptionType+"\">");
		}
		body.print("<a name=\"");
		body.print(sentenceEvent.getTimestamp());
		body.print("\"/>");
		String speaker = sentenceEvent.getSpeakerChange();
		if (speaker != null) {
			body.print("<span class=\"speaker\">");
			body.print(StringEscapeUtils.escapeHtml(speaker));
			body.print("&gt;&gt; </span>");
		}
		body.println(StringEscapeUtils.escapeHtml(sentenceEvent.getSentence()));
		body.println("<p/>");
	}
	
	public void write(CCXDSEvent xdsEvent) {
		
		/* THE XDS DATA IS UNRELIABLE!
	
		if (xdsData != null) {
			
	
			if ((xdsData.getProgramName() != null) &&
					(xdsEvent.getXDSData().getProgramName() != null) &&
					(xdsEvent.getXDSData().getProgramName().trim().length() > 0) &&
					(!xdsData.getProgramName().equals(xdsEvent.getXDSData().getProgramName()))) {
				closeDoc();
			} else if ((xdsData.getProgramStartTimeID() != null) &&
					(xdsEvent.getXDSData().getProgramStartTimeID() != null) &&
					(!xdsData.getProgramStartTimeID().equals(xdsEvent.getXDSData().getProgramStartTimeID()))) {
				closeDoc();
			}
		}
		
		*/
		
		if ((xdsData.getCallLettersAndNativeChannel() == null) &&
				(xdsEvent.getXDSData().getCallLettersAndNativeChannel() != null)) {
			xdsData.setCallLettersAndNativeChannel(
					xdsEvent.getXDSData().getCallLettersAndNativeChannel());
		}
		if ((xdsData.getNetworkName() == null) &&
				(xdsEvent.getXDSData().getNetworkName() != null)) {
			xdsData.setNetworkName(
					xdsEvent.getXDSData().getNetworkName());
		}
		if ((xdsData.getProgramName() == null) &&
				(xdsEvent.getXDSData().getProgramName() != null)) {
			xdsData.setProgramName(
					xdsEvent.getXDSData().getProgramName());
		}
		if ((xdsData.getProgramType() == null) &&
				(xdsEvent.getXDSData().getProgramType() != null)) {
			xdsData.setProgramType(
					xdsEvent.getXDSData().getProgramType());
		}
		if ((xdsData.getProgramStartTimeID() == null) &&
				(xdsEvent.getXDSData().getProgramStartTimeID() != null)) {
			xdsData.setProgramStartTimeID(
					xdsEvent.getXDSData().getProgramStartTimeID());
		}
		if ((xdsData.getProgramLength() == null) &&
				(xdsEvent.getXDSData().getProgramLength() != null)) {
			xdsData.setProgramLength(
					xdsEvent.getXDSData().getProgramLength());
		}
	}

	public void write(CaptionTypeChangeEvent captionTypeChangeEvent) {
		newCaptionType = captionTypeChangeEvent.getCaptionType();
	}

	public void write(ITVLinkEvent itvLinkEvent) {
		if (body == null) {
			return;
		}
		ITVLink itvLink = itvLinkEvent.getITVLink();
		body.println("<span class=\"ITVLink\"><a href=\""+itvLink.getURL()+"\">"+
			itvLink.getName()+"</a> ("+itvLink.getType()+")</span>");
	}
	
	public void setProgram(ScheduledProgram program) {
		if (program != this.program) {
    		if (this.program != null) {
    			closeDoc();
    		}
    		this.program = program;
    		if (program != null) {
        		openDoc();
    		}
		}
	}
		
	private void openDoc() {
		try {
    		filenamePrefix = EpgUtils.getCaptionFilePrefix(ccDocumentRoot, lineupID, callsign, program.getStartTime().getTime());
			String bodyFilename = filenamePrefix+".body";
			File bodyFile = new File(bodyFilename);
			if (bodyFile.exists()) {
				body = new PrintStream(new FileOutputStream(bodyFilename, true /*append*/));
			} else {
				body = new PrintStream(new FileOutputStream(bodyFilename));
				body.println("<div>");
			}
		} catch (IOException e) {
        	log.error("Can't create .body file.", e);
		}
		xdsData = new XDSData();
		if (currentCaptionType != null) {
			newCaptionType = currentCaptionType;
			currentCaptionType = null;
		}
	}

	private synchronized void closeDoc() {
		if (body == null) {
			return;
		}
		try {
			doc = new PrintStream(new GZIPOutputStream(new FileOutputStream(filenamePrefix+".html.gz")));
			writeHead();
		} catch (IOException e) {
        	log.error("Can't create .html.gz file.", e);
        	return;
		}
		try {
        	String programTitle = program.getProgramTitle();
            	
			doc.println("<!-- WARNING, XDS IS INFORMATIONAL ONLY AND CAN BE VERY UNRELIABLE -->");
			if (xdsData.getCallLettersAndNativeChannel() != null) {
				doc.println("<meta name=\"XDSCallLettersAndNativeChannel\" content=\""+
						StringEscapeUtils.escapeHtml(xdsData.getCallLettersAndNativeChannel().replaceAll("[\\p{Cntrl}]", ""))+"\"/>");
			}
			if (xdsData.getNetworkName() != null) {
				doc.println("<meta name=\"XDSNetworkName\" content=\""+
						StringEscapeUtils.escapeHtml(xdsData.getNetworkName().replaceAll("[\\p{Cntrl}]", ""))+"\"/>");
			}
			if (xdsData.getProgramName() != null) {
				doc.println("<meta name=\"XDSProgramName\" content=\""+
						StringEscapeUtils.escapeHtml(xdsData.getProgramName().replaceAll("[\\p{Cntrl}]", ""))+"\"/>");
				if (programTitle == null) {
					programTitle = xdsData.getProgramName();
				}
			}
			try {
				if (xdsData.getProgramType() != null) {
					doc.println("<meta name=\"XDSProgramType\" content=\""+
							XDSData.convertProgramType(xdsData.getProgramType())+"\"/>");
				}
				if (xdsData.getProgramStartTimeID() != null) {
					long startTimestamp = XDSData.convertProgramStartTimeID(xdsData.getProgramStartTimeID());
					doc.println("<meta name=\"XDSProgramStartTimeID\" content=\""+startTimestamp+"\"/>");
					Date date = new Date(startTimestamp);
					DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG);
					df.setTimeZone(TimeZone.getTimeZone("GMT"));
					doc.println("<meta name=\"XDSProgramStartTime\" content=\""+df.format(date)+"\"/>");
					if (XDSData.convertProgramTapeDelayed(xdsData.getProgramStartTimeID())) {
						doc.println("<meta name=\"XDSProgramTapeDelayed\" content=\"true\"/>");
					}
				}
				if (xdsData.getProgramLength() != null) {
					doc.println("<meta name=\"XDSProgramLength\" content=\""+
							XDSData.convertProgramLength(xdsData.getProgramLength())+"\"/>");
				}
			} catch (Exception e) {
				//ignore program type errors... probably bad data... such is XDS
			}
			if (program != null ) {
				doc.println("<meta name=\"ProgramID\" content=\""+
						program.getProgramId()+"\"/>");
				doc.println("<meta name=\"ScheduleID\" content=\""+
						program.getScheduleId()+"\"/>");
				doc.println("<meta name=\"EpisodeTitle\" content=\""+
						StringEscapeUtils.escapeHtml(program.getEpisodeTitle())+"\"/>");
				doc.println("<meta name=\"StartTime\" content=\""+
						program.getStartTime().getTime()+"\"/>");
				doc.println("<meta name=\"EndTime\" content=\""+
						program.getEndTime().getTime()+"\"/>");
				doc.println("<meta name=\"TVRating\" content=\""+
						StringEscapeUtils.escapeHtml(program.getTvRating())+"\"/>");
				if (programTitle == null) {
					programTitle = Long.toString(program.getStartTime().getTime());
				}
			}
			doc.println("<title>"+StringEscapeUtils.escapeHtml(programTitle)+"</title>");
			doc.println("</head>");
			doc.println("<body>");
			doc.println("<h1>"+StringEscapeUtils.escapeHtml(programTitle)+"</h1>");
			body.close();
			body = null;
			InputStream readBody = new BufferedInputStream(new FileInputStream(filenamePrefix+".body"));
			int b = readBody.read();
			while (b >= 0) {
				doc.write(b);
				b = readBody.read();
			}
			readBody.close();
			File f = new File(filenamePrefix+".body");
			f.delete();
			writeFoot();
			doc.close();
			doc = null;
		} catch (FileNotFoundException e) {
        	log.error("Lost .body file!", e);
		} catch (IOException e) {
        	log.error("Error copying .body to .html.gz", e);
		}
		doc = null;
	}
	
	private void writeHead() {
		doc.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		doc.println("<html>");
		doc.println("<head>");
		doc.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-15\" />");
	}

	private void writeFoot() {
		if (currentCaptionType != null) {
			doc.println("</div>");
		}
		doc.println("</body>");
		doc.println("</html>");
	}

	public synchronized void cancelWriting() {
        if (doc != null) {
	        body.println("============ Interrupted =============\n<p/>");
	        closeDoc();
        }
	}

	public String getCallsign() {
		return callsign;
	}

	public void setCallsign(String callsign) {
		this.callsign = callsign;
	}

	public String getCcDocumentRoot() {
		return ccDocumentRoot;
	}

	public void setCcDocumentRoot(String ccDocumentRoot) {
		this.ccDocumentRoot = ccDocumentRoot;
	}

	public String getLineupID() {
		return lineupID;
	}

	public void setLineupID(String lineupID) {
		this.lineupID = lineupID;
	}
}
