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

package com.appeligo.channelfeed;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import com.appeligo.channelfeed.work.FileReaderThread;
import com.appeligo.channelfeed.work.Destinations;
import com.knowbout.cc4j.FrequencyStandard;

public class SendCaptionFiles extends CaptureApp {
	
    private static final Logger log = Logger.getLogger(SendCaptionFiles.class);
    private static final String DEFAULT_CONFIG_FILE = "/etc/flip.tv/sendcaptionfiles.xml";
    
	/**
	 * @param args
	 */
    public static void main(String[] args) {
    	new SendCaptionFiles(args);
    }
    
    public SendCaptionFiles(String[] args) {
    	super(args, DEFAULT_CONFIG_FILE);
    	
    }
    
    @Override
	protected void openSources(Configuration provider) {
		int fileCount = provider.getList("files.file[@name]").size();
		
		for (int j = 0; j < fileCount; j++) {
			
			String fileName = provider.getString("files.file("+j+")[@name]");
			String callsign = provider.getString("files.file("+j+")[@callsign]");
			String loop = provider.getString("files.file("+j+")[@loop]");
			String autoAdvance = provider.getString("files.file("+j+")[@autoAdvance]");
			String advanceSeconds = provider.getString("files.file("+j+")[@advanceSeconds]");
			
			if ((autoAdvance != null) && (advanceSeconds != null)) {
				log.error("autoAdvance and advanceSeconds are mutually exclusive");
				continue;
			}
			
			log.info("fileName="+fileName+", callsign="+callsign+", advanceSeconds="+advanceSeconds);
			
			if (fileName == null || callsign == null) {
				//TODO: change to a logging call
				log.error("Invalid configuration in: "+identifyMe());
				log.error("    fileName="+fileName+
						", callsign="+callsign);
				continue;
			}
			try {
        		FileReaderThread fileThread = new FileReaderThread("File Captions "+getLineupID()+", callsign "+callsign);
        		
        		fileThread.setEpgService(getEpgService());
        		fileThread.setCcDocumentRoot(getCaptionDocumentRoot());
        		fileThread.setCaptionFileName(fileName);
				if (advanceSeconds != null) {
					fileThread.setAdvanceSeconds(Integer.parseInt(advanceSeconds));
				} else if (autoAdvance != null) {
					fileThread.setAutoAdvance(Boolean.parseBoolean(autoAdvance));
				}
				if (loop != null) {
					fileThread.setLoop(Boolean.parseBoolean(loop));
				}
				
				Destinations destinations = setupDestinations();
				destinations.setCallsign(callsign);
				destinations.setSendXDS(false);
				destinations.setSendITV(false);
				destinations.setFileWriter(null);
				
        		fileThread.setDestinations(destinations);
        		
				destinations.connect();
    	    	fileThread.start();
    	    	
			} catch (MalformedURLException e1) {
				log.error("Exception on a channel", e1);
			} catch (NumberFormatException e1) {
				log.error("Exception on a channel", e1);
			} catch (IOException e1) {
				log.error("Exception on a channel", e1);
			}
		}
	}
}
