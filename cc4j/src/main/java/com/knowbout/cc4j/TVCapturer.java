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
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * This is a wrapper around ffmpeg.
 * @author kadel
 */
public class TVCapturer {
	
    private static final Logger log = Logger.getLogger(TVCapturer.class);
    
	protected static final String DEFAULT_FRAME_SIZE = "352x288";
	protected static final int DEFAULT_FRAME_RATE = 10;
	protected static final int DEFAULT_AUDIO_SYNC_SAMPLES_PER_SECOND = 30;
	
	/**
	 * Note that 44100 Hz is pretty common too.
	 */
	protected static final int DEFAULT_AUDIO_SAMPLING_FREQUENCY = 11025;
	
	/**
	 * Note that 64K is pretty common too.
	 */
	protected static final int DEFAULT_AUDIO_BIT_RATE = 32;
	
	/**
	 * 1 is mono, 2 stereo if available
	 */
	protected static final int DEFAULT_AUDIO_CHANNELS = 1;
	protected static final int DEFAULT_DELAY_SECONDS = 0;
	protected static final String DEFAULT_OUTPUT_FORMAT = "flv";
	
	private ExecutorService threadPool;
	private VideoDevice videoDevice;
	private String channel;
	private Process process;
	private WritableByteChannel pipeToProcess;
	private String lastStderrLine;
	private boolean closed;
	private String filename;
	private Thread videoThread;
	private String frameSize = DEFAULT_FRAME_SIZE;
	private int frameRate = DEFAULT_FRAME_RATE;
	private int audioSyncSamplesPerSecond = DEFAULT_AUDIO_SYNC_SAMPLES_PER_SECOND;
	private int audioSamplingFrequency = DEFAULT_AUDIO_SAMPLING_FREQUENCY;
	private int audioBitRate = DEFAULT_AUDIO_BIT_RATE;
	private int audioChannels = DEFAULT_AUDIO_CHANNELS;
	private int delaySeconds = DEFAULT_DELAY_SECONDS;
	private String outputFormat = DEFAULT_OUTPUT_FORMAT;
	
	public TVCapturer(VideoDevice videoDevice) throws IOException {
		this.videoDevice = videoDevice;
		if (!videoDevice.isClosed()) {
    		try {
				channel = videoDevice.getChannel();
			} catch (BadChannelException e) {
				log.error("Error initializing channel from video device.", e);
			}
		}
		closeVideo(); // We'll reopen when ready
	}

	public synchronized void captureTo(String filename) throws IOException {
		if (filename != null && filename.indexOf(".") < 0) {
			filename += "."+outputFormat;
		}
		this.filename = filename;
		if (filename == null) {
			closeVideo();
		} else {
			reopenVideo();
		}
		if (videoThread == null) {
			videoThread = new Thread("TV capture from "+videoDevice.getDeviceNumber()) {
				@Override
				public void run() {
					try {
						mainloop();
					} catch (IOException e) {
						log.error("Exception in TVCapturer loop", e);
						if (lastStderrLine != null) {
    						log.error("Last message from ffmpeg stderr:\n"+lastStderrLine);
    						lastStderrLine = null;
						}
						videoThread = null;
					}
				}
			};
			videoThread.start();
		}
	}
		
	private void mainloop() throws IOException {
		ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
		threadPool = Executors.newCachedThreadPool();
		String currentFilename = null;
		while (!closed) {
			readVideo(buffer);
			currentFilename = reopenOutputFile(currentFilename);
			if (pipeToProcess != null) {
    			buffer.flip();
    			pipeToProcess.write(buffer);
			}
			buffer.clear();
		}
		if (process != null) {
			process.destroy();
			pipeToProcess.close();
		}
		threadPool.shutdownNow();
	}

	private synchronized void readVideo(ByteBuffer buffer) throws IOException {
		while (videoDevice.isClosed()) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		videoDevice.read(buffer);
	}

	private synchronized void reopenVideo() throws IOException {
		videoDevice.reopen();
		try {
			videoDevice.setChannel(channel);
    		notifyAll();
		} catch (BadChannelException e) {
			IOException ioe = new IOException("Bad channel: "+channel);
			ioe.initCause(e);
			throw ioe;
		}
	}

	private synchronized void closeVideo() throws IOException {
		videoDevice.close();
	}

	private String reopenOutputFile(String currentFilename) throws IOException {
		if ((filename != currentFilename) && 
			(filename == null || (!filename.equals(currentFilename)))) {
			currentFilename = filename;
			if (process != null) {
				process.destroy();
				pipeToProcess.close();
			}
			if (currentFilename == null) {
				process = null;
				pipeToProcess = null;
			} else {
				process = new ProcessBuilder("/usr/local/bin/ffmpeg",
						"-i", "-",
						"-async", Integer.toString(getAudioSyncSamplesPerSecond()),
						"-s", getFrameSize(),
						"-ar", Integer.toString(getAudioSamplingFrequency()),
						"-ab", Integer.toString(getAudioBitRate()),
						"-ac", Integer.toString(getAudioChannels()),
						"-r", Integer.toString(getFrameRate()),
						"-ss", Integer.toString(getDelaySeconds()),
						"-y",
						"-f", outputFormat,
						currentFilename).start();
				pipeToProcess = openProcessStreams();
			}
		}
		return currentFilename;
	}

	private WritableByteChannel openProcessStreams() {
		WritableByteChannel stdinChannel = Channels.newChannel(process.getOutputStream()); // to stdin
		final InputStream stderr = process.getErrorStream();
		threadPool.execute(new Runnable() {
			public void run() {
				Thread.currentThread().setName("Swallow stderr for "+filename);
				try {
					int c;
					StringBuilder sb = new StringBuilder();
					while ((c = stderr.read()) >= 0) {
						char ch = (char)c;
						if (ch == '\n' || ch == '\r') {
							log.debug(sb.toString());
							lastStderrLine = sb.toString();
							sb.setLength(0);
						} else {
							sb.append(ch);
						}
					}
				} catch (IOException e) {
				}
			}
		});
		return stdinChannel;
	}
	
	public void close() throws IOException {
		closed = true;
	}

	/**
	 * Get the frames per seconds to record after the next call to {@link #captureTo(String)}.
	 * @return the frame rate per second
	 */
	public int getFrameRate() {
		return frameRate;
	}

	/**
	 * Sets the frames per second to capture after the next call to {@link #captureTo(String)}.
	 * The default is 10 frames per second.
	 * @param frameRate
	 */
	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}

	/**
	 * Get the frames per seconds to record after the next call to {@link #captureTo(String)}.
	 * @return the frame size value
	 */
	public String getFrameSize() {
		return frameSize;
	}

	/**
	 * Sets the frame size to capture after the next call to {@link #captureTo(String)}.
	 * The default is 160x128.
	 * @param frameSize the size parameter as either [width]x[height] or a preset of either
	 * `sqcif'  (128x96),  `qcif'  (176x144), `cif' (352x288), or `4cif'  (704x576)
	 */
	public void setFrameSize(String frameSize) {
		this.frameSize = frameSize;
	}

	public int getAudioSyncSamplesPerSecond() {
		return audioSyncSamplesPerSecond;
	}
	
	/**
	 * Audio sync method. "Stretches/squeezes" the audio stream to match the timestamps,
	 * @param audioSyncSamplesPerSecond the maximum samples per second by which the audio is changed.
	 * 1 is a special case where only the start of the audio stream is corrected without any later correction. 
	 */
	public void setAudioSyncSamplesPerSecond(int audioSyncSamplesPerSecond) {
		this.audioSyncSamplesPerSecond = audioSyncSamplesPerSecond;
	}

	public int getAudioSamplingFrequency() {
		return audioSamplingFrequency;
	}

	public void setAudioSamplingFrequency(int audioSamplingFrequency) {
		this.audioSamplingFrequency = audioSamplingFrequency;
	}

	public int getAudioBitRate() {
		return audioBitRate;
	}

	public void setAudioBitRate(int audioBitRate) {
		this.audioBitRate = audioBitRate;
	}

	public int getAudioChannels() {
		return audioChannels;
	}

	public void setAudioChannels(int audioChannels) {
		this.audioChannels = audioChannels;
	}

	public String getOutputFormat() {
		return outputFormat;
	}

	/**
	 * @param outputFormat the format for the video output file, which can be one of flv (the default), avi, mpeg,
	 * and several others. (See ffmpeg -formats)
	 */
	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}

	public VideoDevice getVideoDevice() {
		return videoDevice;
	}

	public int getDelaySeconds() {
		return delaySeconds;
	}

	/**
	 * @param delaySeconds the number of seconds to delay before capturing.  If you set this to 2 seconds,
	 * that seems to ensure the buffers will have been flushed.  (Warning: This is not guaranteed since
	 * some drivers and hardware may have larger buffers than others.)  The reason this property was implemented
	 * is because when changing channels, and then immediately capturing to a new file, there would often
	 * be a brief glimpse (mainly in audio) of the previous channel before the captured video started playing
	 * the new channel.
	 */
	public void setDelaySeconds(int delaySeconds) {
		this.delaySeconds = delaySeconds;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
}
