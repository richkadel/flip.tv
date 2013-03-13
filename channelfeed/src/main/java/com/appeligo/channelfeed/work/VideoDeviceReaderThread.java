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

import org.apache.log4j.Logger;


import com.knowbout.cc4j.BadChannelException;
import com.knowbout.cc4j.VBILine;
import com.knowbout.cc4j.VideoDevice;

public class VideoDeviceReaderThread extends VBIReaderThread {
	
    private static final Logger log = Logger.getLogger(VideoDeviceReaderThread.class);
    
	private VideoDevice videoDevice;

	public VideoDeviceReaderThread(String name) {
		super(name);
	}

	/**
	 * @param vbi
	 * @return true if successful, or false if we should abort (e.g., because of a closed socket)
	 * @throws IOException
	 */
	@Override
	protected boolean readVBILine(VBILine vbi) throws IOException {
		videoDevice.getVBIDevice().readVBILine(vbi);
		return true;
	}

	public VideoDevice getVideoDevice() {
		return videoDevice;
	}

	public void setVideoDevice(VideoDevice videoDevice) {
		this.videoDevice = videoDevice;
		ProcessStats.setDeviceNumber(getDestinations().getCallsign(), videoDevice.getDeviceNumber());
		try {
			ProcessStats.setChannel(getDestinations().getCallsign(), videoDevice.getChannel());
		} catch (IOException e) {
			log.error("Could not get channel from video device", e);
		} catch (BadChannelException e) {
			log.error("Could not get channel from video device", e);
		}
	}
}

