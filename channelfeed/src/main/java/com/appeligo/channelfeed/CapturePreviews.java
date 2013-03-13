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
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import com.appeligo.channelfeed.work.PreviewThread;
import com.appeligo.channelfeed.work.VideoDeviceReaderThread;
import com.appeligo.channelfeed.work.Destinations;
import com.appeligo.channelfeed.work.TVThread;
import com.knowbout.cc4j.BadChannelException;
import com.knowbout.cc4j.FrequencyStandard;
import com.knowbout.cc4j.TVCapturer;
import com.knowbout.cc4j.VideoDevice;

public class CapturePreviews extends CaptureApp {

    private static final Logger log = Logger.getLogger(CapturePreviews.class);
	private static String DEFAULT_CONFIG_FILE = "/etc/flip.tv/capturepreviews.xml";
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new CapturePreviews(args);
	}
	
	public CapturePreviews(String[] args) {
		super(args, DEFAULT_CONFIG_FILE);
	}
		
    @Override
	protected void openSources(Configuration provider) {
	
    	XMLConfiguration config = getConfig();
    	
		String frameSize = config.getString("videoParams[@frameSize]");
		int frameRate = config.getInt("videoParams[@frameRate]", -1);
		int audioSyncSamplesPerSecond = config.getInt("videoParams[@audioSyncSamplesPerSecond]", -1);
		int audioSamplingFrequency = config.getInt("videoParams[@audioSamplingFrequency]", -1);
		int audioBitRate = config.getInt("videoParams[@audioBitRate]", -1);
		int audioChannels = config.getInt("videoParams[@audioChannels]", -1);
		String outputFormat = config.getString("videoParams[@outputFormat]");
		
		log.info("frameSize="+frameSize+
				", frameRate="+frameRate+
				", audioSyncSamplesPerSecond="+audioSyncSamplesPerSecond+
				", audioSamplingFrequency="+audioSamplingFrequency+
				", audioBitRate="+audioBitRate+
				", audioChannels="+audioChannels+
				", outputFormat="+outputFormat);
			
    	int clipLengthSeconds = config.getInt("previewRules[@clipLengthSeconds]", -1);
		int delaySeconds = config.getInt("previewRules[@delaySeconds]", -1);
			// This delaySeconds could be set directly on the TVCapturer, but we want the PreviewThread to be aware
		int earliestClipSeconds = config.getInt("previewRules[@earliestClipSeconds]", -1);
        int latestClipSeconds = config.getInt("previewRules[@latestClipSeconds]", -1);
        int latestClipPercentOfDuration = config.getInt("previewRules[@latestClipPercentOfDuration]", -1);
        int maxClips = config.getInt("previewRules[@maxClips]", -1);
        int minSecondsBetweenClips = config.getInt("previewRules[@minSecondsBetweenClips]", -1);
		
		log.info("clipLengthSeconds="+clipLengthSeconds+
				", delaySeconds="+delaySeconds+
				", earliestClipSeconds="+earliestClipSeconds+
				", latestClipSeconds="+latestClipSeconds+
				", latestClipPercentOfDuration="+latestClipPercentOfDuration+
				", maxClips="+maxClips+
				", minSecondsBetweenClips="+minSecondsBetweenClips);
			
		int tunerCount = provider.getList("tuners.tuner[@deviceNumber]").size();
		
		for (int j = 0; j < tunerCount; j++) {
			
			String deviceNumber = provider.getString("tuners.tuner("+j+")[@deviceNumber]");
			log.info("deviceNumber="+deviceNumber);
			
			try {
			
    			VideoDevice videoDevice = new VideoDevice(Integer.parseInt(deviceNumber), getFrequencyStandard());
    			
    			TVCapturer tvCapturer = new TVCapturer(videoDevice);
        			
				if (frameSize != null && (frameSize.trim().length() > 0)) {
                	tvCapturer.setFrameSize(frameSize);
				}
				if (frameRate >= 0) {
                	tvCapturer.setFrameRate(frameRate);
				}
                if (audioSyncSamplesPerSecond >= 0) {
                	tvCapturer.setAudioSyncSamplesPerSecond(audioSyncSamplesPerSecond);
            	}
                if (audioSamplingFrequency >= 0) {
                    tvCapturer.setAudioSamplingFrequency(audioSamplingFrequency);
            	}
                if (audioBitRate >= 0) {
                    tvCapturer.setAudioBitRate(audioBitRate);
            	}
                if (audioChannels >= 0) {
                    tvCapturer.setAudioChannels(audioChannels);
            	}
                if (outputFormat != null && outputFormat.trim().length() > 0) {
                    tvCapturer.setOutputFormat(outputFormat);
                }
                
    	    	PreviewThread previewThread = new PreviewThread("Preview thread for device #"+deviceNumber);
            	previewThread.setPreviewDocumentRoot(getPreviewDocumentRoot());
            	previewThread.setTVCapturer(tvCapturer);
            	previewThread.setEpgService(getEpgService());
            	previewThread.setLineupID(getLineupID());
            	
            	if (clipLengthSeconds >= 0) {
            		previewThread.setClipLengthSeconds(clipLengthSeconds);
            	}
                if (delaySeconds >= 0) {
                    previewThread.setDelaySeconds(delaySeconds);
            	}
        		if (earliestClipSeconds >= 0) {
                    previewThread.setEarliestClipSeconds(earliestClipSeconds);
                }
                if (latestClipSeconds >= 0) {
                    previewThread.setLatestClipSeconds(latestClipSeconds);
                }
                if (latestClipPercentOfDuration >= 0) {
                    previewThread.setLatestClipPercentOfDuration(latestClipPercentOfDuration);
                }
                if (maxClips >= 0) {
                    previewThread.setMaxClips(maxClips);
                }
                if (minSecondsBetweenClips >= 0) {
                    previewThread.setMinSecondsBetweenClips(minSecondsBetweenClips);
                }
        
    			Configuration tuner = provider.subset("tuners.tuner("+j+")");
        		int stationCount = tuner.getList("stations.station[@channel]").size();
        		for (int k = 0; k < stationCount; k++) {
    			
        			String channel = tuner.getString("stations.station("+k+")[@channel]");
        			String callsign = tuner.getString("stations.station("+k+")[@callsign]");
        			
        			log.info("channel="+channel+
        					", callsign="+callsign);
        			
                	previewThread.addStation(callsign, channel);
    			}
        		
    	    	previewThread.start();
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
