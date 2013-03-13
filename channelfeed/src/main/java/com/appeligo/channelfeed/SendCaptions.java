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

import com.appeligo.channelfeed.work.VideoDeviceReaderThread;
import com.appeligo.channelfeed.work.Destinations;
import com.appeligo.channelfeed.work.TVThread;
import com.knowbout.cc4j.BadChannelException;
import com.knowbout.cc4j.FrequencyStandard;
import com.knowbout.cc4j.VideoDevice;

public class SendCaptions extends CaptureApp {

    private static final Logger log = Logger.getLogger(SendCaptions.class);
	private static String DEFAULT_CONFIG_FILE = "/etc/flip.tv/channelfeed.xml";
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SendCaptions(args);
	}
	
	public SendCaptions(String[] args) {
		super(args, DEFAULT_CONFIG_FILE);
	}
		
    @Override
	protected void openSources(Configuration provider) {
		int tunerCount = provider.getList("tuners.tuner[@deviceNumber]").size();
		
		for (int j = 0; j < tunerCount; j++) {
			String deviceNumber = provider.getString("tuners.tuner("+j+")[@deviceNumber]");
			String channel = provider.getString("tuners.tuner("+j+")[@channel]");
			String callsign = provider.getString("tuners.tuner("+j+")[@callsign]");
			boolean capturetv = provider.getBoolean("tuners.tuner("+j+")[@capturetv]", false);
			String framesize = provider.getString("tuners.tuner("+j+")[@framesize]");
			int framerate = provider.getInt("tuners.tuner("+j+")[@framerate]", -1);
			boolean nocaptions = provider.getBoolean("tuners.tuner("+j+")[@nocaptions]", false);
			boolean xds = provider.getBoolean("tuners.tuner("+j+")[@xds]", false);
			boolean itv = provider.getBoolean("tuners.tuner("+j+")[@itv]", false);
			
			log.info("deviceNumber="+deviceNumber+
					", channel="+channel+
					", callsign="+callsign+
					", capturetv="+capturetv+
					", framesize="+framesize+
					", framerate="+framerate+
					", nocaptions="+nocaptions+
					", xds="+xds+
					", itv="+itv);

			if (deviceNumber == null || channel == null || callsign == null) {
				log.error("Invalid configuration in: "+identifyMe());
				log.error("    deviceNumber="+deviceNumber+
						", channel="+channel+
						", callsign="+callsign);
				continue;
			}
			try {
				VideoDevice videoDevice = new VideoDevice(Integer.parseInt(deviceNumber), getFrequencyStandard());
				videoDevice.setChannel(channel);
				
        		if (capturetv) {
        	    	TVThread tvThread = new TVThread("Video record "+getLineupID()+", channel "+channel+", callsign "+callsign);
                	tvThread.setEpgService(getEpgService());
                	tvThread.setLineupID(getLineupID());
                	tvThread.setCallsign(callsign);
                	tvThread.setCcDocumentRoot(getCaptionDocumentRoot());
    				if (framesize != null && (framesize.trim().length() > 0)) {
                    	tvThread.setFrameSize(framesize);
    				}
    				if (framerate >= 0) {
                    	tvThread.setFrameRate(framerate);
    				}
                	tvThread.setVideoDevice(videoDevice);
        	    	tvThread.start();
        		}
		
        		if (!nocaptions) {
        			
        	    	VideoDeviceReaderThread captionThread = new VideoDeviceReaderThread("Captions "+getLineupID()+", channel "+channel+", callsign "+callsign);
        	    	
    				Destinations destinations = setupDestinations();
    				
            		destinations.setCallsign(callsign);
    				destinations.setSendXDS(xds);
    				destinations.setSendITV(itv);
    				
            		captionThread.setDestinations(destinations);
        	    	captionThread.setVideoDevice(videoDevice); // important to set destination and it's callsign before video device...sorry
            		captionThread.setEpgService(getEpgService());
    				
    				destinations.connect();
        	    	captionThread.start();
        		}
			} catch (MalformedURLException e1) {
				log.error("Exception on a channel", e1);
			} catch (NumberFormatException e1) {
				log.error("Exception on a channel", e1);
			} catch (IOException e1) {
				log.error("Exception on a channel", e1);
			} catch (BadChannelException e1) {
				log.error("Exception on a channel", e1);
			} catch (Throwable e1) {
				log.error("Unexpected exception", e1);
			}
		}
	}
}
