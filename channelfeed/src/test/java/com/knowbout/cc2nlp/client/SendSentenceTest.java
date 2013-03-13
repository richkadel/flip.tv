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

package com.knowbout.cc2nlp.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import com.appeligo.channelfeed.work.Destinations;
import com.appeligo.channelfeed.work.NullEPGProvider;
import com.appeligo.channelfeed.work.VideoDeviceReaderThread;
import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.cc4j.BadChannelException;
import com.knowbout.cc4j.VideoDevice;
import com.knowbout.cc4j.FrequencyStandard;
import com.knowbout.epg.service.EPGProvider;

import junit.framework.TestCase;

public class SendSentenceTest extends TestCase {
	
    private static final Logger log = Logger.getLogger(SendSentenceTest.class);

	public EPGProvider connectToEPG(String epgServer) {
		EPGProvider epgService = null;
        HessianProxyFactory factory = new HessianProxyFactory();
        if (epgServer != null) {
        	try {
    			epgService = (EPGProvider)factory.create(EPGProvider.class, epgServer);
    		} catch (MalformedURLException e1) {
    			log.error("Exception on a channel", e1);
    		}
	        if (epgService == null) {
	        	log.error("No EPG Service");
	        }
        } else {
        	epgService = new NullEPGProvider();
        }
        return epgService;
	}
	
	public void testSendSentence() throws MalformedURLException, IOException, BadChannelException {

		log.setLevel(Level.WARN);
		log.addAppender(new ConsoleAppender(new SimpleLayout(), "System.err"));
		log.info("Starting Test");
		Logger newlog = Logger.getLogger(Destinations.class);
		//Level level = Level.toLevel("WARN", null);
		newlog.setLevel(Level.INFO);
		newlog.addAppender(new ConsoleAppender(new SimpleLayout(), "System.err"));
		
		if (System.getProperty("device.test.ok") != null) {
			
			Destinations destinations = new Destinations();
        	destinations.setHeadendID("SDTW");
			destinations.setLineupDevice("C");
			destinations.setCallsign("HIST");
			String channel = "56";
			
			destinations.setCaptionDocumentRoot("/tmp/captiondb");
			destinations.setDestinationURLs(null, null);
			destinations.setSendXDS(false);
			destinations.setSendITV(false);
			
	    	VideoDeviceReaderThread captionThread = new VideoDeviceReaderThread("Captions "+destinations.getLineupID()+
	    			", channel "+channel+
	    			", callsign "+destinations.getCallsign());
        	    	
	    	VideoDevice videoDevice = new VideoDevice(0, FrequencyStandard.US_CABLE);
	    	videoDevice.setChannel(channel);
	    	captionThread.setVideoDevice(new VideoDevice(0, FrequencyStandard.US_CABLE));
    		captionThread.setEpgService(connectToEPG("http://dev.knowbout.tv/epg/channel.epg"));
        	    	
    		captionThread.setDestinations(destinations);
    				
			newlog.warn("Starting Test, channel "+destinations.getCallsign());
			
			destinations.connect();
	    	captionThread.start();
			
			try {
				//Thread.sleep(1000000);
				Thread.sleep(30000);
				//Thread.sleep(2000000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			captionThread.setAborted(true);
			destinations.disconnect();
		}

	}

}