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

package com.knowbout.cc4j;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * The VideoDevice has an optional associated VBIDevice for extracting
 * data from the Vertical Blanking Interval lines, e.g., Line 21 and line 284
 * for closed captions.
 * @author Rich Kadel
 * @author $Author: kadel $
 * @version $Rev: 259 $ $Date: 2006-09-19 10:07:49 -0700 (Tue, 19 Sep 2006) $
 */
public class VBIDevice {
	
	private int deviceNumber;
	private int fd;
	private DataInputStream inPipe;
	private DataOutputStream outPipe;
	private boolean push = false; //TODO
	
	/**
	 * This is only valid when the device is pushing VBI data to us, a la Windows DirectShow,
	 * not Linux.
	 */
	private int bufferSize = 1024;

	/**
	 * @param deviceNumber
	 */
	VBIDevice(int deviceNumber) throws IOException {
		this.deviceNumber = deviceNumber;
		fd = open(deviceNumber);
	}
	
	/**
	 * Native method that actually opens the device.
	 * @param deviceNumber
	 */
	private native int open(int deviceNumber) throws IOException;

	/**
	 * Closes the device file and any other held resources.
	 * @throws IOException
	 */
	void close() throws IOException {
		if (deviceNumber >= 0) {
			closeFile(fd);
			deviceNumber = -1;
			if (inPipe != null) {
				// write two ints to make sure no one is blocking
				outPipe.writeInt(0);
				outPipe.writeInt(0);
				outPipe.close();
				inPipe.close();
				outPipe = null;
				inPipe = null;
			}
		}
	}
	
	/**
	 * Closes the device file and any other held resources.
	 * @throws IOException
	 */
	private native void closeFile(int fd) throws IOException;
	
	/**
	 * Reads the next set of VBI (e.g., closed caption) characters
	 * @throws IOException
	 */
	private native void readVBI(int fd, VBILine vbi) throws IOException;
	
	/**
	 * Reads the next set of VBI (e.g., closed caption) characters
	 * @param vbi a VBIChars object to fill in, or null if you want
	 * readVBIChars to create one for you.
	 * @return the vbi you passed in, or a new one if you pass in null
	 * @throws IOException
	 */
	public VBILine readVBILine(VBILine vbi) throws IOException {
		if (vbi == null) {
			vbi = new VBILine();
		}
		if (!push) {
			readVBI(fd, vbi);
		} // check again because readVBI native code may have reset push
		if (push) {
			int c1 = 0;
			int c2 = 0;
			if (inPipe == null) {
				throw new IOException("VBIDevice is closed");
			}
			synchronized(inPipe) {
				c1 = inPipe.readInt();
				c2 = inPipe.readInt();
			}
			vbi.setValues(21, c1, c2);
		}
		return vbi;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	@SuppressWarnings("unused") // invoked natively
	private void setPush(boolean push) {
		this.push = push;
		if (outPipe == null) {
			PipedOutputStream out = new PipedOutputStream();
			try {
				PipedInputStream in = new PipedInputStream(out);
				outPipe = new DataOutputStream(out);
				inPipe = new DataInputStream(in);
			} catch (IOException e) {
				throw new Error(e);
			}
		}
	}
	
	@SuppressWarnings("unused") // invoked natively
	private void pushBytes(int c1, int c2) throws IOException {
		if (inPipe.available() >= 1000) { // big enough that between this line and the "synchronized",
										  // there should be no chance another thread is going to read
										  // 1000 characters and deplete it down to 0.  If they did,
										  // this could deadlock because they'd have the monitor, and
										  // would be waiting for this method to finish writing the next
										  // 2 chars, but we'd be waiting for the monitor here.
			synchronized(inPipe) {
				if (inPipe.available() >= bufferSize) {
					inPipe.readInt();
					inPipe.readInt();
				}
			}
		}
		outPipe.writeInt(c1);
		outPipe.writeInt(c2);
		outPipe.flush();
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
}
