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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * This class opens and manipulates or queries a video capture and TV tuner device.
 * @author Rich Kadel
 * @author $Author: kadel $
 * @version $Rev: 2101 $ $Date: 2007-08-28 15:44:42 -0700 (Tue, 28 Aug 2007) $
 */
public class VideoDevice {
	
	private int deviceNumber;
	private int fd;
	private FrequencyStandard frequencyStandard;
	private VBIDevice vbi;
	private VideoCapability videoCapability;
	
	static {
		try {
			Properties props = new Properties();
			InputStream s = VideoDevice.class.getResourceAsStream("VideoDevice.properties");
			props.load(s);
			String zvbiver = props.getProperty("zvbi.current");
			String cc4jver = props.getProperty("cc4j.current");
			if (!System.getProperty("os.name").startsWith("Windows")) {
				System.loadLibrary("zvbi-"+zvbiver);
			}
			System.loadLibrary("cc4j-"+cc4jver);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Open a device given the device filename
	 * @param deviceNumber - the device number from 0 to number of TV cards minus 1.
	 * This gets translated on Linux to "/dev/video0" for device number 0, for instance.
	 * On Windows, device number 0 is the first card.
	 * @throws IOException
	 */
	public VideoDevice(int deviceNumber, FrequencyStandard frequencyStandard) throws IOException {
		this.deviceNumber = deviceNumber;
		this.frequencyStandard = frequencyStandard;
		fd = open(deviceNumber);
		setVideoStandard(frequencyStandard.getVideoStandard());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	/**
	 * Closes the device file and any other held resources.
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (fd >= 0) {
			if (vbi != null) {
				vbi.close();
				vbi = null;
			}
			closeFile(fd);
			fd = -1;
		}
	}
	
	/**
	 * @return true if we have closed (via close()) and not reopened this device
	 */
	public boolean isClosed() {
		return (fd < 0);
	}
	
	public void reopen() throws IOException {
		if (fd >= 0) {
			throw new IOException("Device is already open");
		}
		fd = open(deviceNumber);
		setVideoStandard(frequencyStandard.getVideoStandard());
	}
	
	/**
	 * Closes the device file and any other held resources.
	 * @throws IOException
	 */
	private native void closeFile(int fd) throws IOException;
	
	public VBIDevice getVBIDevice() throws IOException {
		if (vbi == null) {
			vbi = new VBIDevice(deviceNumber);
		}
		return vbi;
	}
	
	/**
	 * Native method that actually opens the device.
	 * @param deviceNumber
	 */
	private native int open(int deviceNumber) throws IOException;

	/**
	 * Set the frequency (corresponding to a channel).
	 * @param frequency in megahertz
	 * @throws IOException
	 */
	private native void setFrequency(int fd, double frequency) throws IOException;
	
	/**
	 * Returns the current frequency setting.
	 * @return the frequency in megahertz
	 * @throws IOException
	 */
	private native double getFrequency(int fd) throws IOException;
	
	/**
	 * Set the frequency (corresponding to a channel).
	 * @param frequency in megahertz
	 * @throws IOException
	 */
	private native void setLinuxVideoStandard(
			int fd, long linuxVideoStandard) throws IOException;
	
	/**
	 * Returns the current frequency setting.
	 * @return the frequency in megahertz
	 * @throws IOException
	 */
	private native long getLinuxVideoStandard(int fd) throws IOException;
	
	//TODO: I'm confused.  Why did I ever have a public setVideoStandard and not a
	// setFrequencyStandard, which appears to encompass the video standard and more?
	/**
	 * Set the video standard.
	 * @param videoStandard VideoStandard.NTSC, for example
	 * @throws IOException
	 * @see VideoStandard
	 */
	private void setVideoStandard(VideoStandard videoStandard) throws IOException {
		setLinuxVideoStandard(fd, videoStandard.getLinuxVideoStandard());
	}
	
	/**
	 * Returns the current video standard setting.
	 * @return the current setting
	 * @throws IOException
	 */
	public VideoStandard getVideoStandard() throws IOException {
		return new VideoStandard(getLinuxVideoStandard(fd));
	}

	/**
	 * Returns the frequency standard.
	 * @return the current setting
	 */
	public FrequencyStandard getFrequencyStandard() {
		return frequencyStandard;
	}
	
	/**
	 * @return The channel name (usually a number as a string, e.g., "7" for channel 7),
	 * or <b>null</b> if the current frequency does not match a valid channel for the
	 * current {@link FrequencyStandard}
	 * @throws IOException
	 * @throws BadChannelException if the frequency on the card does not match
	 * a know frequency for the current FrequencyStandard.
	 */
	public String getChannel() throws IOException, BadChannelException {
		double freq = getFrequency(fd);
		String channel = ChannelFrequencies.getChannel(frequencyStandard, freq);
		return channel;
	}
	
	/**
	 * Sets the channel frequency on the video device.
	 * @param channelName usually a number as a string, but in some countries it can
	 * include a letter
	 * @throws IOException
	 * @throws BadChannelException if the given channel was not valid for the current
	 * {@link FrequencyStandard}
	 */
	public void setChannel(String channelName) throws IOException, BadChannelException {
		double freq = ChannelFrequencies.getFrequency(frequencyStandard, channelName);
		setFrequency(fd, freq);
	}
	
	/**
	 * Returns the video capability object, reading it if not already read.
	 * @return the VideoCapability
	 * @throws IOException
	 */
	private VideoCapability getCapability() throws IOException {
		if (videoCapability == null) {
			videoCapability = new VideoCapability();
			readCapability(fd, videoCapability);
		}
		return videoCapability;
	}
	
	/**
	 * Reads values into the VideoCapability stucture for later retrieval.
	 * This should not change, so it only needs to be called once after
	 * opening the device.
	 * @throws IOException
	 */
	private native void readCapability(int fd, VideoCapability videoCapability) throws IOException;
	
	public String getDriver() throws IOException {
		return getCapability().driver;
	}
	
	public String getCard() throws IOException {
		return getCapability().card;
	}
	
	public String getBusInfo() throws IOException {
		return getCapability().busInfo;
	}
	
	public boolean isVideoCaptureCapable() throws IOException {
		return getCapability().videoCapture;
	}
	
	public boolean isVideoOutputCapable() throws IOException {
		return getCapability().videoOutput;
	}
	
	public boolean isVideoOverlayCapable() throws IOException {
		return getCapability().videoOverlay;
	}
	
	public boolean isVBICaptureCapable() throws IOException {
		return getCapability().vbiCapture;
	}
	
	public boolean isVBIOutputCapable() throws IOException {
		return getCapability().vbiOutput;
	}
	
	public boolean isRDSCaptureCapable() throws IOException {
		return getCapability().rdsCapture;
	}
	
	public boolean isTunerCapable() throws IOException {
		return getCapability().tuner;
	}
	
	public boolean isAudioCapable() throws IOException {
		return getCapability().audio;
	}
	
	public boolean isRadioCapable() throws IOException {
		return getCapability().radio;
	}
	
	public boolean isReadWriteCapable() throws IOException {
		return getCapability().readwrite;
	}
	
	public boolean isAsyncIOCapable() throws IOException {
		return getCapability().asyncio;
	}
	
	public boolean isStreamingCapable() throws IOException {
		return getCapability().streaming;
	}

	public void read(ByteBuffer buffer) throws IOException {
		readFile(fd, buffer);
		buffer.position(buffer.capacity());
	}
	
	private native void readFile(int fd, ByteBuffer buffer) throws IOException;

	public int getDeviceNumber() {
		return deviceNumber;
	}

	private class VideoCapability {
		String driver;
		String card;
		String busInfo;
		boolean videoCapture;
		boolean videoOutput;
		boolean videoOverlay;
		boolean vbiCapture;
		boolean vbiOutput;
		boolean rdsCapture;
		boolean tuner;
		boolean audio;
		boolean radio;
		boolean readwrite;
		boolean asyncio;
		boolean streaming;
	}
}
