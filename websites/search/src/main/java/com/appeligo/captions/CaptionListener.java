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

package com.appeligo.captions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;

import com.appeligo.ccdataweb.CaptionStore;
import com.appeligo.config.ConfigurationService;
import com.appeligo.epg.DefaultEpg;
import com.appeligo.lucene.DocumentUtil;
import com.appeligo.lucene.LuceneIndexer;
import com.appeligo.search.util.ConfigUtils;
import com.appeligo.util.Utils;
import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.cc2nlp.CCEventService;
import com.knowbout.cc2nlp.CCSentenceEvent;
import com.knowbout.cc2nlp.CCXDSEvent;
import com.knowbout.cc2nlp.CaptionTypeChangeEvent;
import com.knowbout.cc2nlp.ITVLinkEvent;
import com.knowbout.cc2nlp.ProgramStartEvent;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.Network;
import com.knowbout.epg.service.ScheduledProgram;

/**
 * This class provides a is an end point for close caption events.  It indexes
 * the sentences using Lucene to make the searchable.
 * 
 * @author fear
 */
public class CaptionListener implements CCEventService {

	private static final Logger log = Logger.getLogger(CaptionListener.class);
		
	private CaptionStore captionStore;

	private static String documentRoot = "/tmp"; 

	private static PrintStream statsFile;	
	private static String currentDay;
	private static long lastWrite;
	private static String hostname;
	
	private HashMap<String, ProgramCaptions> captions;
	
	private String programIndexLocation;
	private String compositeIndexLocation;
	private String liveIndexLocation;
	private String liveLineup;
	private EPGProvider epg;
	private List<String> lineupIds;
	
	static {
		documentRoot = ConfigUtils.getSystemConfig().getString("documentRoot[@path]", "/tmp");
	}
	/**
	 * 
	 *
	 */
	@SuppressWarnings("unchecked")
	public CaptionListener() throws MalformedURLException {
		if (log.isInfoEnabled()) {
			log.info("Instantiated a " + this.getClass().getName());
		}
		Configuration config = ConfigUtils.getSystemConfig();
		programIndexLocation = config.getString("luceneIndex");
		compositeIndexLocation = config.getString("compositeIndex");
		liveIndexLocation = config.getString("luceneLiveIndex");
		liveLineup = config.getString("liveLineup");

        //Set the optimization duraction of the live index to 30 minutes.
		int liveIndexOptimization = config.getInt("luceneLiveIndexOptimization", 30);
		LuceneIndexer liveIndex = LuceneIndexer.getInstance(liveIndexLocation);
		liveIndex.setOptimizeDuration(liveIndexOptimization);
    	epg = DefaultEpg.getInstance();
		captions = new HashMap<String, ProgramCaptions>();	
		//PENDING(CE): We should probably get this list form the EPG?  Seems like we should.
		Configuration lineupConfiguration = ConfigurationService.getConfiguration("lineups");
		lineupIds = (List<String>)lineupConfiguration.getList("lineups.lineup.id");
		
		DeleteOldProgramsThread.startThread();
	}
	
	
	private CaptionStore getCaptionStore()  throws MalformedURLException{
		if (captionStore == null) {
			Configuration config = ConfigUtils.getSystemConfig();
			HessianProxyFactory factory = new HessianProxyFactory();
	    	String url = config.getString("captionsEndpoint");
	    	if (log.isInfoEnabled()) {
	    		log.info("CaptionStore endpoint is " + url);
	    	}
	    	captionStore = (CaptionStore) factory.create(CaptionStore.class,url);
		}
    	return captionStore;
	
	}
		
	/* (non-Javadoc)
	 * @see com.knowbout.nlp.keywords.service.CCEventService#startCapture(java.lang.String, java.lang.String)
	 */
	public String startCapture() {
		log.info("startCapture()");
		return SUCCESS;
	}
	
	/* (non-Javadoc)
	 * @see com.knowbout.nlp.keywords.service.CCEventService#captureCCEvent(java.lang.String, java.lang.String)
	 */
	public String captureSentence(CCSentenceEvent event) {
		try {
			if (log.isDebugEnabled()) {
				log.debug("capture sentence(" + event +  ")");
			}
			checkStats();
			//PENDING(CE) Make sure the timestamp is a real time and not an offset
			ScheduledProgram scheduledProgram = event.getScheduledProgram();
			ProgramCaptions programCaptions = captions.get(event.getCallsign());
			if (programCaptions == null) {
				if (log.isInfoEnabled()) {
					log.info("Attempting to recover scheduled program, must have been a cold start.");
				}				
				if (scheduledProgram == null) {
					scheduledProgram = epg.getScheduledProgramByNetworkCallSign(liveLineup, event.getCallsign(), new Date(event.getTimestamp()));
				}
				if (scheduledProgram != null) {
					if (log.isInfoEnabled()) {
						log.info("Setting current program to " + scheduledProgram.getProgramId() + 
								":" + scheduledProgram.getProgramTitle());
					}
					List<ScheduledProgram> schedules = getSchedulePrograms(scheduledProgram);
					programCaptions = new ProgramCaptions(scheduledProgram.getProgramId(), scheduledProgram.getScheduleId(), scheduledProgram.getNetwork().getStationCallSign(), scheduledProgram, schedules );				
					try { 
						CaptionStore store = getCaptionStore();
						String[] pastCaptions = store.getSentences(event.getLineupID(), event.getCallsign(), event.getProgramStartTime(), event.getProgramStartTime(), event.getTimestamp());
						for (String caption: pastCaptions) {
							programCaptions.addSentence(DocumentUtil.prettySentence(caption));
						}
					} catch (MalformedURLException e) {
						log.error("Unable to locate the caption store to back fill captions.", e);
					} catch(Exception e) {
						log.error("Error retreving sentences from caption store for " + event.getLineupID() + " callsign:" + event.getCallsign() + " startTime: "+ event.getProgramStartTime() + " eventTime: " + event.getTimestamp(), e);						
					}
					programCaptions.addSentence(DocumentUtil.prettySentence(event.getSentence()));
					captions.put(event.getCallsign(), programCaptions);
					addToLiveIndex(event,programCaptions, true);
				} else {
					log.error("We are unable to find a program for callsign "+ event.getCallsign() + " on lineup: "+ event.getLineupID() + " at time:" + new Date(event.getTimestamp()));
					return FAILURE;
				}
			} else {				
				//Check to see if the program has changed.  This is a safty check in case the programStart event is out of synch
				//PENDING(CE): Can this happen? Need to talk to Rich about it.
				if (programCaptions.isCurrentProgram(scheduledProgram)) {
					programCaptions.addSentence(DocumentUtil.prettySentence(event.getSentence()));
					addToLiveIndex(event,programCaptions,false);
				} else {
					//It is not, so we reached the end of a program and now we need to index all of the data
					//Ok since we changed the behavior of the EPG service (7/14/07) to return the current program if it is not over yet
					//We need to get ask for the schedule list again.
					List<ScheduledProgram> nextShowing = getSchedulePrograms(programCaptions.getCurrentProgram());
					LuceneIndexer.getInstance(programIndexLocation).addProgram(programCaptions.getCaptions(), programCaptions.getProgramId(),  nextShowing, Field.Store.YES, true, new Date());
					LuceneIndexer.getCompositeInstance(compositeIndexLocation).addCompositeProgram(programCaptions.getCaptions(), programCaptions.getProgramId(),  programCaptions.getSchedule(), Field.Store.YES, true, new Date());
					//Now start saving sentences for the new program
					List<ScheduledProgram> schedules = getSchedulePrograms(scheduledProgram);
					programCaptions = new ProgramCaptions(scheduledProgram.getProgramId(), scheduledProgram.getStartTime().getTime(), scheduledProgram.getNetwork().getStationCallSign(), scheduledProgram, schedules);
					programCaptions.addSentence(DocumentUtil.prettySentence(event.getSentence()));
					captions.put(event.getCallsign(), programCaptions);
					addToLiveIndex(event,programCaptions,true);
				}
			}
			return SUCCESS;
		} catch (Throwable e) {
			log.error(e.getMessage() + event, e);
			return FAILURE;
		}
		
	}
	
	private void addToLiveIndex(CCSentenceEvent event, ProgramCaptions captions, boolean newProgram) {
		String callsign = event.getCallsign();
		LuceneIndexer liveIndex = LuceneIndexer.getInstance(liveIndexLocation);
		liveIndex.deleteDocuments(new Term("lineup-"+liveLineup+"-stationCallSign", callsign));
		liveIndex.addProgram(captions.getCaptions(),captions.getProgramId(), captions.getSchedule(), Field.Store.YES, false, new Date());	            
	}
	
	private synchronized static void checkStats() {
		int interval = 5; // minutes
		long timestamp = new Date().getTime();
		if ((timestamp-lastWrite) > (interval * 60 * 1000)) {
			lastWrite = timestamp;
    		String day = Utils.getDatePath(timestamp);
    		if (!day.equals(currentDay)) {
    			if (statsFile != null) {
    				statsFile.println("</table></body></html>");
    				statsFile.close();
    				statsFile = null;
    			}
    			currentDay = day;
    		}
    		if (hostname == null) {
    			try {
    				hostname = InetAddress.getLocalHost().getHostName();
    			} catch (UnknownHostException e) {
    				hostname = "UnknownHost";
    			}
    		}
    		String dirname = documentRoot+"/stats/"+currentDay+"/"+hostname;
    		String statsFileName = dirname+"/searchprocstats.html";
    		try {
    			if (statsFile == null) {
    				File dir = new File(dirname);
    				if ((!dir.exists()) && (!dir.mkdirs())) {
    					throw new IOException("Error creating directory "+dirname);
    				}
    				File file = new File(statsFileName);
    				if (file.exists()) {
    					statsFile = new PrintStream(new FileOutputStream(statsFileName, true));
    					statsFile.println("<tr><td colspan='5'>Restart</td></tr>");
    				} else {
    					statsFile = new PrintStream(new FileOutputStream(statsFileName));
    					String title = "Search Process (tomcat) status for "+currentDay;
    					statsFile.println("<html><head><title>"+title+"</title></head>");
    					statsFile.println("<body><h1>"+title+"</h1>");
    					statsFile.println("<table border='1'>");
    					statsFile.println("<tr>");
    					statsFile.println("<th colspan='2'>"+interval+" Minute Intervals</th>" +
    									  "<th colspan='3'>Mem Pre GC</th>" +
    									  "<th>GC</th>" +
    									  "<th colspan='3'>Mem Post GC</th>");
    					statsFile.println("</tr>");
    					statsFile.println("<tr>");
    					statsFile.println("<th>Timestamp</th>");
    					statsFile.println("<th>Time</th>");
    					statsFile.println("<th>Used</th>");
    					statsFile.println("<th>Committed</th>");
    					statsFile.println("<th>Max</th>");
    					statsFile.println("<th>Millis</th>");
    					statsFile.println("<th>Used</th>");
    					statsFile.println("<th>Committed</th>");
    					statsFile.println("<th>Max</th>");
    					statsFile.println("</tr>");
    				}
    			}
				Calendar cal = Calendar.getInstance();
				cal.setTimeZone(TimeZone.getTimeZone("GMT"));
				cal.setTimeInMillis(timestamp);
				String time = String.format("%1$tH:%1$tM:%1$tS", cal);
				MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
				MemoryUsage memory = memoryBean.getHeapMemoryUsage();
				statsFile.print("<tr>");
				statsFile.print("<td>"+timestamp+"</td>");
				statsFile.print("<td>"+time+"</td>");
				statsFile.format("<td>%,d</td>", memory.getUsed());
				statsFile.format("<td>%,d</td>", memory.getCommitted());
				statsFile.format("<td>%,d</td>", memory.getMax());
				long beforeGC = System.currentTimeMillis();
				System.gc();
				long elapsed = System.currentTimeMillis() - beforeGC;
				statsFile.format("<td>%,d</td>", (int)elapsed);
				memoryBean = ManagementFactory.getMemoryMXBean();
				memory = memoryBean.getHeapMemoryUsage();
				statsFile.format("<td>%,d</td>", memory.getUsed());
				statsFile.format("<td>%,d</td>", memory.getCommitted());
				statsFile.format("<td>%,d</td>", memory.getMax());
				statsFile.println("</tr>");
    		} catch (IOException e) {
    			log.error("Error opening or writing to "+statsFileName, e);
    		}
		}
	}
	
	/**
	 * 
	 */
	public String endCapture() {
		if (log.isDebugEnabled()) {
			log.debug("endCapture()");
		}
		return SUCCESS;
	}
	
	/**
	 * 
	 */
	public String captureXDS(CCXDSEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("capture xds(" + event +  ")");
		}
		return SUCCESS;
	}

	public String captureITVLink(ITVLinkEvent itvLinkEvent) {
		if (log.isDebugEnabled()) {
			log.debug("capture itvlink(" + itvLinkEvent.getITVLink() +  ")");
		}
		return SUCCESS;
	}

	public String captionTypeChanged(CaptionTypeChangeEvent captionTypeChangedEvent) {
		return SUCCESS;
	}

	/* (non-Javadoc)
	 * @see com.knowbout.cc2nlp.CCEventService#startProgram(com.knowbout.cc2nlp.CCEvent)
	 */
	public String startProgram(ProgramStartEvent programStartEvent) {
		ScheduledProgram scheduledProgram = programStartEvent.getScheduledProgram();
		Network network = scheduledProgram.getNetwork();
		if (network != null) {
			if (log.isInfoEnabled()) {
				log.info("Due to program start event, purging storage for callsign " + network.getStationName());
			}
			ProgramCaptions programCaptions = captions.remove(programStartEvent.getCallsign());
			//We had captured CC for this callsign already. It must be an old program, so lets
			//Index off that data
			if (programCaptions != null) {
				//It is not, so we reached the end of a program and now we need to index all of the data
				List<ScheduledProgram> schedule = getSchedulePrograms(scheduledProgram);
				LuceneIndexer.getInstance(programIndexLocation).addProgram(programCaptions.getCaptions(), programCaptions.getProgramId(), programCaptions.getSchedule(), Field.Store.YES, true, new Date());
				LuceneIndexer.getCompositeInstance(compositeIndexLocation).addCompositeProgram(programCaptions.getCaptions(), programCaptions.getProgramId(), programCaptions.getSchedule(), Field.Store.YES, true, new Date());
				ProgramCaptions newCaptions = new ProgramCaptions(scheduledProgram.getProgramId(),scheduledProgram.getScheduleId(),network.getStationCallSign(), scheduledProgram, schedule);
				captions.put(programStartEvent.getCallsign(), newCaptions);
			}
		}
		return SUCCESS;	
	}
	
	private List<ScheduledProgram> getSchedulePrograms(ScheduledProgram program) {
		String programId = program.getProgramId();
		ArrayList<ScheduledProgram> programs = new ArrayList<ScheduledProgram>();
		for(String lineup: lineupIds) {
			if (program.getLineupId().equals(lineup)) {
				programs.add(program);
			} else {
				ScheduledProgram sked = epg.getNextShowing(lineup, programId, false, true);
				if (sked == null) {
					sked = epg.getLastShowing(lineup, programId);
				}
				if (sked != null) {
					programs.add(sked);
				}
			}
		}
		return programs;		
	}

	
	
}
