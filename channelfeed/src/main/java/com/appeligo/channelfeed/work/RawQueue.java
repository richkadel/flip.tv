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

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class RawQueue extends Thread {
	
	private static final Logger log = Logger.getLogger(RawQueue.class);

	private static final int OUTPUT_BUFFER_SIZE = 16384;

	private String headendID;
	private String lineupDevice;
	private String callsign;
	private String destinationURL;
	private ManagedPipedInputStream pipein;
	private DataOutputStream os;
	private DataInputStream is;
	private DataOutputStream destination;
    private Socket socket;
    private boolean aborted;
    private boolean inError;
	private int bytesDropped;

	RawQueue(String headendID, String lineupDevice, String callsign, String destinationURL) {
		super("RawQueue "+headendID+"-"+lineupDevice+":"+callsign+" to "+destinationURL);
		this.headendID = headendID;
		this.lineupDevice = lineupDevice;
		this.callsign = callsign;
		this.destinationURL = destinationURL;
		PipedOutputStream pipeout = new PipedOutputStream();
		try {
			pipein = new ManagedPipedInputStream(OUTPUT_BUFFER_SIZE, pipeout);
			os = new DataOutputStream(pipeout);
			is = new DataInputStream(pipein);
		} catch (IOException e) {
			log.fatal("Can't open pipe", e);
		}
	}

    /**
     * @throws MalformedURLException
     * @throws UnknownHostException
     * @throws IOException
     */
    private void openSocket() throws MalformedURLException, UnknownHostException, IOException {
        URL url = new URL(this.destinationURL);
        log.debug("opening socket to "+url.getHost()+", port "+url.getPort());
        socket = new Socket(url.getHost(), url.getPort());
        log.debug("socket opened");
		destination = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        log.debug("writing utf "+headendID);
		destination.writeUTF(headendID);
        log.debug("writing utf "+lineupDevice);
		destination.writeUTF(lineupDevice);
        log.debug("writing utf "+callsign);
		destination.writeUTF(callsign);
        log.debug("wrote UTFs");
    }

    private void closeSocket() throws IOException {
        try {
            destination.close();
        } catch (IOException e) {
        }
        socket.close();
    }
	
	public void write(int lineNumber, int c1, int c2) throws IOException {
		if (pipein.writeBytesBeforeBlocking() < 12) {
			throw new IOException("Can't write VBI to socket. Pipe is full, so the socket's probably blocked");
		}
		os.writeInt(lineNumber);
		os.writeInt(c1);
		os.writeInt(c2);
	}

	@Override
	public void run() {
        try {
    		openSocket();
			boolean inException = false;
			boolean inOpenSocketException = false;
			int[] vbiLineData = new int[3];
			while (!aborted) {
				try {
					for (int i = 0; i < 3; i++) {
						vbiLineData[i] = is.readInt();
					}
					for (int i = 0; i < 3; i++) {
						destination.writeInt(vbiLineData[i]);
					}
					if (inException) {
						log.error("GOOD! Recovered from exception");
					}
					inException = false;
				} catch (IOException e) {
					if (!inException) {
						log.error("Exception writing to raw destination", e);
						inException = true;
					}
                    try {
	                    closeSocket();
					} catch (IOException e2) {
                    }
                    try {
	                    openSocket();
						if (inOpenSocketException) {
							log.error("GOOD! Recovered from open socket exception");
							inOpenSocketException = false;
                        }
					} catch (IOException e2) {
						if (!inOpenSocketException) {
	                        log.error("Can't reopen socket", e2);
							inOpenSocketException = true;
						}
                    }
				}
			}
        } catch (Throwable t) {
            log.error("Unexpected exception", t);
            try {
				pipein.close();
			} catch (IOException e) {
				log.error("Couldn't close the pipe either!", e);
			}
        }
	}

	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

	public boolean isInError() {
		return inError;
	}

	public void setInError(boolean inError) {
		this.inError = inError;
	}

	public int getBytesDropped() {
		return bytesDropped;
	}

	public void setBytesDropped(int bytesDropped) {
		this.bytesDropped = bytesDropped;
	}
}
