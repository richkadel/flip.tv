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
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;


import com.knowbout.cc4j.VBILine;
import com.knowbout.cc4j.VideoDevice;

public class VBISocketReaderThread extends VBIReaderThread {
	
	private static final Logger log = Logger.getLogger(VBISocketReaderThread.class);
	
    private DataInputStream socketData;

    /**
	 * @param socket The socket to read VBI from
	 * @throws IOException
	 */
	public VBISocketReaderThread(String threadName, Socket socket)
				throws IOException {
		super(threadName);
		socketData = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
	}
	
	public void setDestinations(Destinations destinations) throws IOException {
		super.setDestinations(destinations);
		try {
			destinations.setHeadendID(socketData.readUTF());
    		destinations.setLineupDevice(socketData.readUTF());
    		destinations.setCallsign(socketData.readUTF());
    		log.info("Reading socket captions for "+destinations.getHeadendID()+"-"+
    				destinations.getLineupDevice()+": "+destinations.getCallsign());
		} catch (IOException e) {
			log.fatal("Could not read the TV source information", e);
			throw e;
		}
	}

	/**
	 * @param vbi
	 * @return true if successful, or false if we should abort (e.g., because of a closed socket)
	 * @throws IOException
	 */
	@Override
	protected boolean readVBILine(VBILine vbi) throws IOException {
		try {
	    	vbi.setValues(socketData.readInt(), socketData.readInt(), socketData.readInt());
		} catch (EOFException e) {
			log.debug("Socket was closed by sender for "+getDestinations().getLineupID()+", station "+getDestinations().getCallsign());
    		return false;
		}
		return true;
	}
}

