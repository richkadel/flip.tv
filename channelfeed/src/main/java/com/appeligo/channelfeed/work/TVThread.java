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
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.appeligo.epg.util.EpgUtils;
import com.appeligo.util.Utils;
import com.knowbout.cc4j.TVCapturer;
import com.knowbout.cc4j.VideoDevice;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.ScheduledProgram;

public class TVThread extends Thread {
	
	private static final Logger log = Logger.getLogger(TVThread.class);
	
	private TVCapturer tvCapturer;
	private EPGProvider epgService;
	private String lineupID;
	private String callsign;
    private boolean aborted;

	private String frameSize;

	private int frameRate;

	private VideoDevice videoDevice;

	private String ccDocumentRoot;

	public TVThread(String name) {
		super(name);
	}

	public void run() {
		boolean firstProgram = true;
		
		while (!aborted) {
			ScheduledProgram currentProgram = null;
            while (true) {
                try {
	                currentProgram = epgService.getScheduledProgramByNetworkCallSign(lineupID, callsign,
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
            
			if (firstProgram) {
				String flvFilename =
					EpgUtils.getCaptionFilePrefix(
    					ccDocumentRoot, lineupID, callsign,
    					currentProgram.getStartTime().getTime())
					+".flv";
				File file = new File(flvFilename);
				if (file.exists()) {
					if (!file.delete()) {
			        	log.error("Couldn't delete truncated/restarted Flash Video file "+flvFilename);
					}
				}
				try {
					tvCapturer = new TVCapturer(videoDevice);
					if (frameSize != null) {
						tvCapturer.setFrameSize(frameSize);
					}
					if (frameRate > 0) {
						tvCapturer.setFrameRate(frameRate);
					}
				} catch (IOException e) {
		        	log.error("Can't capture TV!", e);
				}
				firstProgram = false;
			}
			
			long nextProgramTime = currentProgram.getEndTime().getTime();
			
/*
System.out.println("DEBUG CODE TAKE THIS OUT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
System.out.println("DEBUG CODE TAKE THIS OUT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
System.out.println("DEBUG CODE TAKE THIS OUT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
if(firstProgram)
nextProgramTime = currentProgram.getStartTime().getTime();
//PUT THIS NEXT LINE BACK ABOVE WHERE COMMENTED OUT
 */
				firstProgram = false;

            if ((nextProgramTime - System.currentTimeMillis()) >
                    (12 * 60 * 60 * 1000)) {
                log.fatal("EPG returned a program duration greater than 12 hours.\n"+
                          "Can't record video for a program that long.\n"+
                          "Canceling all future video recordings.");
                break;
            }
			
            Utils.sleepUntil(nextProgramTime);
			
			try {
				tvCapturer.captureTo(
					EpgUtils.getCaptionFilePrefix(
    					ccDocumentRoot, lineupID, callsign,
    					nextProgramTime)
					+".flv");
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

	public String getFrameSize() {
		return frameSize;
	}

	public void setFrameSize(String frameSize) {
		this.frameSize = frameSize;
	}

	public int getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}

	public VideoDevice getVideoDevice() {
		return videoDevice;
	}

	public void setVideoDevice(VideoDevice videoDevice) {
		this.videoDevice = videoDevice;
	}
}