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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.net.InetAddress;
import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.appeligo.epg.util.EpgUtils;
import com.appeligo.util.Utils;
import com.knowbout.cc4j.BadChannelException;
import com.knowbout.cc4j.TVCapturer;
import com.knowbout.cc4j.VideoDevice;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.ProgramType;
import com.knowbout.epg.service.ScheduledProgram;

public class PreviewThread extends Thread {
	
	private static final Logger log = Logger.getLogger(PreviewThread.class);

	private static final int DEFAULT_CLIP_LENGTH_SECONDS = 30;
	private static final int DEFAULT_DELAY_SECONDS = 2;
	private static final int DEFAULT_EARLIEST_CLIP_SECONDS = 300;
	private static final int DEFAULT_LATEST_CLIP_SECONDS = 1800;
	private static final int DEFAULT_LATEST_CLIP_PERCENT_OF_DURATION = 50;
	private static final int DEFAULT_MAX_CLIPS = 3;
	private static final int DEFAULT_MIN_SECONDS_BETWEEN_CLIPS = 120;
	
	private static final String UNTITLED_EPISODE = "Untitled Episode";

	private static final long DELAY_MARGIN = 2 * 1000; // milliseconds
	
	private String previewDocumentRoot;
	private EPGProvider epgService;
	private String lineupID;
	private TVCapturer tvCapturer;
	private List<Station> stations = new ArrayList<Station>();
	private int clipLengthSeconds = DEFAULT_CLIP_LENGTH_SECONDS;
	private int delaySeconds = DEFAULT_DELAY_SECONDS;
	private int earliestClipSeconds = DEFAULT_EARLIEST_CLIP_SECONDS;
    private int latestClipSeconds = DEFAULT_LATEST_CLIP_SECONDS;
    private int latestClipPercentOfDuration = DEFAULT_LATEST_CLIP_PERCENT_OF_DURATION;
    private int maxClips = DEFAULT_MAX_CLIPS;
	private int minSecondsBetweenClips = DEFAULT_MIN_SECONDS_BETWEEN_CLIPS;
    private boolean aborted;

	public PreviewThread(String name) {
		super(name);
	}

	public void run() {
		
        long clipLength = clipLengthSeconds * 1000;
        long delay = delaySeconds * 1000;
        long earliestClip = earliestClipSeconds * 1000;
        long latestClip = latestClipSeconds * 1000;
        
		long searchStartTime = System.currentTimeMillis();
		boolean foundRecordable = false;
		long earliestNewRecording = 0;
		int i = -1;
		while (!aborted) {
			i++;
			boolean ignoreSkip = false;
			if (i == stations.size()) {
				i = 0;
				if (!foundRecordable) {
					if (earliestNewRecording < searchStartTime - DELAY_MARGIN) {
							// I'm not sure why this occasionally fails
							// when the two times are within about a second of each other
							// so I put in the delay for now.  Need to check the logic to
							// see how this could ever happen.
						ignoreSkip = true; // this is rare, but make sure we don't skip so we can reset earliestNewRecording
    					/* Don't fail on this test.  We've got it covered everytime we set earliestNewRecording.
    					 * Sometimes this test fails, and I think it's just a short delay before we can start a loop, such as
    					 * when waiting for the EPG during a nightly EPG update.  Here's the error I got.
                            //2007-08-28 04:15:21,861 FATAL [PreviewThread - Preview thread for device #5] - EPG error or bug...
                            //Can't find a valid future program time. 
                            //earliestNewRecording=Tue Aug 28 04:15:19 PDT 2007,
                            //searchStartTime=     Tue Aug 28 04:15:21 PDT 2007
						log.fatal("EPG error or bug... Can't find a valid future program time. earliestNewRecording="+new Date(earliestNewRecording)
								+", searchStartTime="+new Date(searchStartTime));
						throw new Error("Can't continue");
    					*/
					}
					log.info("Nothing to record, so we'll try again at "+new Date(earliestNewRecording));
					Utils.sleepUntil(earliestNewRecording);
				}
				searchStartTime = System.currentTimeMillis();
        		foundRecordable = false;
			}
			
			Station station = stations.get(i);
			
			if ((station.program == null) ||
					(station.program.getEndTime().getTime() < System.currentTimeMillis())) {
				closeInProgressFile(station);
                station.program = getScheduledProgram(station);
                station.programClips = 0;
                station.lastClipEnd = 0;
                station.skipProgram = false;
			}
			
			if (ignoreSkip) {
				station.skipProgram = false;
			}
			
			if (station.skipProgram) {
				continue;
			}
			
			log.info("Checking station "+station.callsign+", channel "+station.channel+", program "+
					station.program.getProgramTitle()+" ("+station.program.getEpisodeTitle()+")");
			
			if ((earliestNewRecording < searchStartTime) ||
    				((station.program.getEndTime().getTime() + earliestClip) < earliestNewRecording)) {
				earliestNewRecording =  station.program.getEndTime().getTime() + earliestClip;
    			if (earliestNewRecording < searchStartTime) {
					log.fatal("EPG error or bug... earliestNewRecording="+new Date(earliestNewRecording)
							+", searchStartTime="+new Date(searchStartTime));
					throw new Error("Can't continue");
    			}
			}
		
			ProgramType programType = ProgramType.fromProgram(station.program);
			if (programType != ProgramType.EPISODE && programType != ProgramType.MOVIE) {
				log.info("Not recording this type of program: "+programType+". program id="+station.program.getProgramId());
				station.skipProgram = true;
				continue;
			}
			
            long clipStart = System.currentTimeMillis();
			long programStart = station.program.getStartTime().getTime();
			long programEnd = station.program.getEndTime().getTime();
			long programDuration = programEnd - programStart;
			long latestByPercent = (programDuration * latestClipPercentOfDuration) / 100;
			long relativeClipStart = clipStart - programStart;
			long relativeClipEnd = relativeClipStart + clipLength;
			long relativeClipStartSeconds = relativeClipStart/1000;
			long minMillisBetweenClips = minSecondsBetweenClips*1000;
			
			if (relativeClipStart < earliestClip) {
				log.info("Before time window allowed for previews...skipping this clip but we should try again");
				long nextAllowedRecording = programStart + earliestClip;
    			if (nextAllowedRecording < earliestNewRecording) {
    				earliestNewRecording = nextAllowedRecording;
        			if (earliestNewRecording < searchStartTime) {
    					log.fatal("Bug... earliestNewRecording="+new Date(earliestNewRecording)
    							+", searchStartTime="+new Date(searchStartTime));
    					throw new Error("Can't continue");
        			}
    			}
				// don't skip program... still waiting for valid time
				continue;
			}
			
			if ((relativeClipEnd > latestClip) ||
				(relativeClipEnd > latestByPercent)) {
				
    			if (relativeClipEnd > latestClip) {
    				log.info("After time window allowed for previews, clip end would be "+(relativeClipEnd/1000)+
    						" seconds, which is beyond latest clip seconds="+latestClipSeconds);
    			} else {
    				log.info("After time window allowed for previews, clip end would be "+(relativeClipEnd/1000)+
    						" seconds, which is beyond latest clip by percent seconds="+(latestByPercent/1000)+
    						" based on "+latestClipPercentOfDuration+"% for this program's duration of "+(programDuration/1000)+
    						" seconds, or "+(programDuration/1000/60)+" minutes");
    			}
				
    			closeInProgressFile(station);
				station.skipProgram = true;
				continue;
			}
			
			String fileDir = EpgUtils.getPreviewDir(previewDocumentRoot, station.program);
			File directory = new File(fileDir);
    		if ((!directory.exists()) && (!directory.mkdirs())) {
				log.error("Could not create preview subdirectory "+fileDir);
				station.skipProgram = true;
				continue;
			}
			String filePrefix = fileDir+"/"+station.program.getProgramId();
			
			if (station.programClips == 0) {
				
    			File titleFile = new File(filePrefix+".title");
				station.programPreviewsInProgressFile = new File(filePrefix+"-"+station.callsign+".inprogress");
				
				if (station.programPreviewsInProgressFile.exists()) {
					
    				station.programPreviewsInProgressFile.delete();
    				
					for (File file : directory.listFiles()) {
						String filename = file.getName();
						if (filename.startsWith("inprogress-")) {
							file.delete();
						} else if (filename.startsWith(station.program.getProgramId()) &&
								filename.endsWith(".flv")) {
							int comma = filename.indexOf(',');
							int dash = filename.indexOf('-');
							int dot = filename.indexOf('.');
							if ((comma > 0) && (dash > 0)) {
            					station.programClips++;
								int existingClipStartSeconds = Integer.parseInt(filename.substring(comma+1,dash));
								int existingClipLengthSeconds = Integer.parseInt(filename.substring(dash+1,dot));
								int existingClipEndSeconds = existingClipStartSeconds + existingClipLengthSeconds;
								long adjustedClipEnd = clipStart + (existingClipEndSeconds * 1000);
								if (adjustedClipEnd > station.lastClipEnd) {
									station.lastClipEnd = adjustedClipEnd;
								}
							}
						}
					}
					
					titleFile.delete(); // Now that we're sure we're going to want more clips, let's deleted the
                    					// title file... It will be recreated next.
				}
				
    			if (titleFile.exists()) {
    				log.info("We must have already collected clips for this program at a prior airing, "+
    						 "or another station is airing it and started collecting previews before we did.  Skipping.");
    				station.skipProgram = true;
    				station.programPreviewsInProgressFile = null;
						// since we never created it (would have been the next step)
    				continue;
    			} else {
    				
        			try {
            			
        				if (!station.programPreviewsInProgressFile.createNewFile()) {
        					log.info("Can't create previews in progress file "+station.programPreviewsInProgressFile+
        							". It could be a timing coincidence, if another tuner is recording the same program.");
        					station.skipProgram = true;
        					continue;
        				}
        				
            			FileWriter inprogressFile = new FileWriter(station.programPreviewsInProgressFile);
            			PrintWriter printInProgress = new PrintWriter(inprogressFile);
            		
            			printInProgress.println("Channel: "+station.channel);
            			printInProgress.println("Tuner device number: "+tvCapturer.getVideoDevice().getDeviceNumber());
            			printInProgress.println("Server: "+InetAddress.getLocalHost().getHostName());
            			inprogressFile.close();
        				
            			FileWriter printFile = new FileWriter(titleFile);
            			PrintWriter printTitle = new PrintWriter(printFile);
            		
            			printTitle.print(station.program.getProgramTitle());
            			if (programType == ProgramType.EPISODE) {
            				String episodeTitle = station.program.getEpisodeTitle();
            				if (episodeTitle == null || episodeTitle.trim().length() == 0) {
            					episodeTitle = UNTITLED_EPISODE;
            				}
            				printTitle.println(" ("+episodeTitle+")");
            			} else {
            				printTitle.println();
            			}
        			
            			printFile.close();
        			} catch (IOException e) {
        				log.error("Could not write title file "+titleFile, e);
        				station.skipProgram = true;
        				continue;
        			}
    			}
			}
			
			if (station.programClips >= maxClips) {
				log.info("Reached max clips for this program");
    			closeInProgressFile(station);
				station.skipProgram = true;
				continue;
			}
		
			if ((clipStart - station.lastClipEnd) < minMillisBetweenClips) {
				log.info("Clip too soon.  Must be at least "+minSecondsBetweenClips+
						" seconds since last clip, which ended at "+new Date(station.lastClipEnd));
				long nextAllowedRecording = station.lastClipEnd + minMillisBetweenClips;
    			if (nextAllowedRecording < earliestNewRecording) {
    				earliestNewRecording = nextAllowedRecording;
        			if (earliestNewRecording < searchStartTime) {
    					log.fatal("Bug... earliestNewRecording="+new Date(earliestNewRecording)
    							+", searchStartTime="+new Date(searchStartTime));
    					throw new Error("Can't continue");
        			}
    			}
    			// don't skip... we're waiting for the right time to record this again
				continue;
			}
		
			try {
    			String clipFilePrefix = filePrefix
        				+","
        				+String.format("%05d", relativeClipStartSeconds)
        				+"-"
        				+clipLengthSeconds;
    			
    			String clipFileName = clipFilePrefix
    					+".flv";
    			int lastSlash = clipFileName.lastIndexOf('/');
    			String inProgressClipFileName = clipFileName.substring(0,lastSlash+1)+"inprogress-"+clipFileName.substring(lastSlash+1);
    			
				station.programClips++;
				station.lastClipEnd = clipStart + clipLength;
					
				foundRecordable = true;
				tvCapturer.setChannel(station.channel);
				tvCapturer.captureTo(inProgressClipFileName);
        			
				log.info("Recording preview from now until "+new Date(System.currentTimeMillis()+clipLength));
				
    			Utils.sleepForMillis(delay+clipLength);
        			
				tvCapturer.captureTo(null);
				
				File inProgressClipFile = new File(inProgressClipFileName);
				
				if (!inProgressClipFile.exists()) {
					throw new Error("Bug. File does not exist: "+inProgressClipFileName);
				} else if (inProgressClipFile.length() == 0L) {
					inProgressClipFile.delete();
					log.fatal("Video capture for previews is failing for station "+station.callsign+
							  ", device number "+tvCapturer.getVideoDevice().getDeviceNumber()+
							  ". Deleting zero length file: "+inProgressClipFileName+
							  " for program "+station.program.getProgramId()+
							  ": "+station.program.getProgramTitle()+" ("+station.program.getEpisodeTitle()+")");
				} else {
    				File clipFile = new File(clipFileName);
    				inProgressClipFile.renameTo(clipFile);
				}
    				
			} catch (IOException e) {
	        	log.error("Can't capture TV!", e);
	        	break;
			}
		}
		
        if (tvCapturer != null) {
			try {
				tvCapturer.close();
			} catch (IOException e) {
		    	log.error("Can't close TV capturer!", e);
			}
        }
	}

	private void closeInProgressFile(Station station) {
		if (station.programPreviewsInProgressFile != null) {
			String name = station.programPreviewsInProgressFile.getPath();
			int dot = name.lastIndexOf('.');
			String prefix = name.substring(0,dot);
			File newFile = new File(prefix+".complete");
			if (!station.programPreviewsInProgressFile.renameTo(newFile)) {
				log.error("Could not rename "+station.programPreviewsInProgressFile+" to "+newFile);
			}
			station.programPreviewsInProgressFile = null;
		}
	}
	
	private ScheduledProgram getScheduledProgram(Station station) {
		ScheduledProgram program = null;
		while (true) {
		    try {
		        program = epgService.getScheduledProgramByNetworkCallSign(lineupID, station.callsign,
						new Date(System.currentTimeMillis()));
		        break;
		    } catch (Throwable t) {
		        log.error("Can't connect to EPG yet, to get video record information.  Trying again in 10 seconds.", t);
		        try {
		            Thread.sleep(10*1000);
		        } catch (InterruptedException e) {
		        }
		    }
		}
		if (program == null) {
			log.fatal("EPG returned null program for "+lineupID+":"+station.callsign+" at current time");
			throw new Error("Can't continue");
		}
		return program;
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

	public String getLineupID() {
		return lineupID;
	}

	public void setLineupID(String lineupID) {
		this.lineupID = lineupID;
	}

	public String getPreviewDocumentRoot() {
		return previewDocumentRoot;
	}

	public void setPreviewDocumentRoot(String previewDocumentRoot) {
		this.previewDocumentRoot = previewDocumentRoot;
	}

	public void setTVCapturer(TVCapturer tvCapturer) {
		this.tvCapturer = tvCapturer;
		tvCapturer.setDelaySeconds(delaySeconds);
	}

	public void addStation(String callsign, String channel) {
		stations.add(new Station(callsign, channel));
	}

	public int getClipLengthSeconds() {
		return clipLengthSeconds;
	}

	public void setClipLengthSeconds(int clipLengthSeconds) {
		if (clipLengthSeconds < 1) {
			throw new Error("Invalid clipLengthSeconds "+clipLengthSeconds);
		}
		this.clipLengthSeconds = clipLengthSeconds;
	}

	public int getEarliestClipSeconds() {
		return earliestClipSeconds;
	}

	public void setEarliestClipSeconds(int earliestClipSeconds) {
		if (earliestClipSeconds < 0) {
			throw new Error("Invalid earliestClipSeconds "+earliestClipSeconds);
		}
		this.earliestClipSeconds = earliestClipSeconds;
	}

	public int getLatestClipSeconds() {
		return latestClipSeconds;
	}

	public void setLatestClipSeconds(int latestClipSeconds) {
		if (latestClipSeconds < 1) {
			throw new Error("Invalid latestClipSeconds "+latestClipSeconds);
		}
		this.latestClipSeconds = latestClipSeconds;
	}

	public int getLatestClipPercentOfDuration() {
		return latestClipPercentOfDuration;
	}

	public void setLatestClipPercentOfDuration(int latestClipPercentOfDuration) {
		if ((latestClipPercentOfDuration < 1) ||
				(latestClipPercentOfDuration > 100)) {
			throw new Error("Invalid latestClipPercentOfDuration "+latestClipPercentOfDuration);
		}
		this.latestClipPercentOfDuration = latestClipPercentOfDuration;
	}

	public int getMaxClips() {
		return maxClips;
	}

	public void setMaxClips(int maxClips) {
		if (maxClips < 1) {
			throw new Error("Invalid maxClips "+maxClips);
		}
		this.maxClips = maxClips;
	}

	public int getMinSecondsBetweenClips() {
		return minSecondsBetweenClips;
	}

	public void setMinSecondsBetweenClips(int minSecondsBetweenClips) {
		this.minSecondsBetweenClips = minSecondsBetweenClips;
	}
	
	private static class Station {
		
		String callsign;
		String channel;
		ScheduledProgram program = null;
		int programClips = 0;
		long lastClipEnd = 0;
		File programPreviewsInProgressFile = null;
		boolean skipProgram = false;
		
		Station(String callsign, String channel) {
			this.callsign = callsign;
			this.channel = channel;
		}
	}

	public int getDelaySeconds() {
		return delaySeconds;
	}

	public void setDelaySeconds(int delaySeconds) {
		this.delaySeconds = delaySeconds;
		if (tvCapturer != null) {
			tvCapturer.setDelaySeconds(delaySeconds);
		}
	}
}
