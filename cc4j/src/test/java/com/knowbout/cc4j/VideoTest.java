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
import junit.framework.TestCase;

public class VideoTest extends TestCase {

	private VideoDevice dev0;
	
	public static void main(String args[]) {
		
		VideoTest videoTest;
		//try {
			videoTest = new VideoTest();
	//		videoTest.testFrequency();
		//} catch (BadChannelException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}
	}

	public VideoTest() {
	}
	
	public void testFrequency() throws IOException, BadChannelException {
		if (System.getProperty("device.test.ok") != null) {
			dev0 = new VideoDevice(0, FrequencyStandard.US_CABLE);
			String channel = "7";
			dev0.setChannel(channel);
			assertTrue(dev0.getChannel() == channel);
			channel = "8";
			dev0.setChannel(channel);
			assertTrue(dev0.getChannel() == channel);
			dev0.close();
		}
	}

	public void testVideoStandard() throws IOException, BadChannelException {
		if (System.getProperty("device.test.ok") != null) {
			dev0 = new VideoDevice(0, FrequencyStandard.US_CABLE);
			assertTrue(dev0.getVideoStandard().isNTSC_M());
			dev0.close();
		}
	}

	public void testVBI() throws IOException {
		if (System.getProperty("device.test.ok") != null) {
			dev0 = new VideoDevice(0, FrequencyStandard.US_CABLE);
			assertTrue(dev0.getVBIDevice() != null);
			dev0.close();
		}
	}
	
	public void testCapability() throws IOException {
		if (System.getProperty("device.test.ok") != null) {
			for (int i = 0; i < 12; i++) {
				System.out.println("Testing Video Tuner #"+i);
				try {
					dev0 = new VideoDevice(i, FrequencyStandard.US_CABLE);
					assert(dev0.getDriver().length() > 0);
					assert(dev0.getCard().length() > 0);
					assert(dev0.isVideoCaptureCapable());
					assert(dev0.isVBICaptureCapable());
					assert(dev0.isTunerCapable());
					System.out.println("driver = "+dev0.getDriver());
					System.out.println("card = "+dev0.getCard());
					System.out.println("bus info = "+dev0.getBusInfo());
					System.out.println("video capture = "+dev0.isVideoCaptureCapable());
					System.out.println("video output = "+dev0.isVideoOutputCapable());
					System.out.println("video overlay = "+dev0.isVideoOverlayCapable());
					System.out.println("VBI capture = "+dev0.isVBICaptureCapable());
					System.out.println("VBI output = "+dev0.isVBIOutputCapable());
					System.out.println("RDS capture = "+dev0.isRDSCaptureCapable());
					System.out.println("tuner = "+dev0.isTunerCapable());
					System.out.println("audio = "+dev0.isAudioCapable());
					System.out.println("radio = "+dev0.isRadioCapable());
					System.out.println("readwrite = "+dev0.isReadWriteCapable());
					System.out.println("asyncio = "+dev0.isAsyncIOCapable());
					System.out.println("streaming = "+dev0.isStreamingCapable());
					dev0.close();
				} catch (IOException e) {
					System.out.println("Tuner not found or error.");
				}
			}
		}
	}
	
	/*
	public void testVBI3() throws IOException, BadChannelException {
		dev0 = new VideoDevice(0, FrequencyStandard.US_CABLE);
		dev0.setChannel("10");
		VBILine vbi = new VBILine();
		for (int i = 0; i < 100; i++) {
			vbi = dev0.getVBIDevice().readVBILine(vbi);
			if (vbi.getCCNumber() == 1) {
				String s = vbi.getPrintableChars();
				if (s != null && s.length() > 0) {
					System.out.print(s);
				}
			}
		}
		System.out.println();
		dev0.close();
	}
	*/
	
	/*
	public void testThreaded() throws IOException, BadChannelException {
		dev0 = new VideoDevice(0, FrequencyStandard.US_CABLE);
		dev0.setChannel("10");
		new Thread() {
			public void run() {
				try {
					VBILine vbi = new VBILine();
					for (int i = 0; i < 400; i++) {
						vbi = dev0.getVBIDevice().readVBILine(vbi);
						if (vbi.getCCNumber() == 1) {
							String s = vbi.getPrintableChars();
							if (s != null && s.length() > 0) {
								System.out.print(s);
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println();
				synchronized(VideoTest.this) {
					VideoTest.this.notifyAll();
				}
			}
		}.start();
		new Thread() {
			public void run() {
				try {
					sleep(1000);
					System.err.println("Channel is "+dev0.getChannel());
					sleep(2000);
					dev0.setChannel("8");
					System.err.println("Channel is "+dev0.getChannel());
					sleep(1000);
					System.err.println("Card is "+dev0.getCard());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BadChannelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				synchronized(VideoTest.this) {
					VideoTest.this.notifyAll();
				}
			}
		}.start();
		try {
			System.out.println("Waiting for test threads...");
			synchronized(this) {
				wait();
				wait();
			}
			dev0.close();
			System.out.println("...done waiting");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void testVBI2() throws IOException, BadChannelException {
		dev0 = new VideoDevice(0, FrequencyStandard.US_CABLE);
		dev0.setChannel("8");
		VideoDevice dev1 = null;
		try {
			dev1 = new VideoDevice(1, FrequencyStandard.US_CABLE);
			dev1.setChannel("7");
		} catch (IOException e) {
			System.err.println("Video Device 1 does not exist or is not available...");
		}
		VBILine vbi = new VBILine();
		for (int i = 0; i < 300; i++) {
			vbi = dev0.getVBIDevice().readVBILine(vbi);
			String s = vbi.getPrintableChars();
			if (s != null && s.length() > 0) {
				System.out.print("Video 0: ");
				System.out.print(s);
				System.out.println();
			}
			//TODO: THIS LOCKS UP BECAUSE dev1 NEVER GETS ANY DATA on Windows Direct Show!!
			if (dev1 != null) {
				vbi = dev1.getVBIDevice().readVBILine(vbi);
				s = vbi.getPrintableChars();
				if (s != null && s.length() > 0) {
					System.out.print("Video 1: ");
					System.out.print(s);
					System.out.println();
				}
			}
		}
		if (dev1 != null) {
			dev1.close();
		}
		dev0.close();
	}
*/
	/*
	public void testVBI4() throws IOException, BadChannelException {
		dev0 = new VideoDevice(0, FrequencyStandard.US_CABLE);
		PrintCommand print = new PrintCommand();
		VBILine vbi = new VBILine();
		for (int i = 0; i < 200; i++) {
			vbi = dev0.getVBIDevice().readVBILine(vbi);
			//System.out.print("Video 0: ");
			vbi.executeCommand(print);
		}
		dev0.close();
	}
	*/
	
	/*
	public void testVBI5() throws IOException, BadChannelException {
		dev0 = new VideoDevice(0, FrequencyStandard.US_CABLE);
		LineBuffer lineBuffer = new LineBuffer(DataChannel.CC1) {

			@Override
			public void lineReady(String s) {
				System.out.println(s);
			}
		};
		dev0.setChannel("42");
		VBILine vbi = new VBILine();
		for (int i = 0; i < 2000; i++) {
			vbi = dev0.getVBIDevice().readVBILine(vbi);
			vbi.executeCommand(lineBuffer);
		}
		System.out.println(lineBuffer.peek());
		dev0.close();
	}
	*/
	
	public void testVBI6() throws IOException, BadChannelException {
		
		if (System.getProperty("device.test.ok") != null) {
			VBICommand itvBuffer = new LineBuffer(DataChannel.TEXT2) {
				
				@Override
				public void lineReady(String line) {
					System.out.println("TEXT2 line: "+line);
				}
				
				@Override
				public void receivedITVLink(ITVLink itvLink) {
					System.out.println("ITVLink: "+itvLink);
				}
				
				@Override
				public void error(VBILine vbi, String errstr) {
					if (isSelectedMode(vbi)) {
						System.out.println("Error: "+errstr);
					}
				}
			};
			
			VBILine vbi = new VBILine();
			vbi.testSetup();
			vbi.setValues(DataChannel.TEXT2, CommandCode.TR);
			//String testITVLink = "<http://www.robson.org/gary/>[t:p][n:Gary Robson][BD55]";
			//String testITVLink = "<mailto:gary@robson.org>[t:p][n:Contact the Author][FFC0]";
			String testITVLink = "<http://www.appeligo.com>[t:a][n:Appeligo, Inc.][6877]>";
			int len = testITVLink.length();
			for (int i = 0; i < len; i++) {
				itvBuffer.printChar(vbi, testITVLink.charAt(i));
			}
		}
	}
	
	public void testVBI7() throws IOException, BadChannelException {
		if (System.getProperty("device.test.ok") != null) {
			dev0 = new VideoDevice(0, FrequencyStandard.US_CABLE);
			final VBILine vbi = new VBILine();
			
			VBICommand sentenceBuffer = new SentenceBuffer(DataChannel.CC1, true) {
				
				@Override 
				public void updatedXDS(VBILine vbi, XDSData xds, XDSField change) {
					System.out.print(change+" = ");
					switch (change) {
					case PROGRAM_START_TIME_ID:
						System.out.println(xds.getProgramStartTimeID());
						break;
						
					case PROGRAM_NAME:
						System.out.println(xds.getProgramName());
						break;
						
					case PROGRAM_TYPE:
						System.out.println(xds.getProgramType()+": "+
								XDSData.convertProgramType(xds.getProgramType()));
						break;
						
					case NETWORK_NAME:
						System.out.println(xds.getNetworkName());
						break;
						
					case CALL_LETTERS_AND_NATIVE_CHANNEL:
						System.out.println(xds.getCallLettersAndNativeChannel());
						break;
						
					case CAPTION_SERVICES:
						System.out.println(xds.getCaptionServices()+": "+
								XDSData.convertCaptionServices(xds.getCaptionServices()));
						break;
						
					default:
						System.out.println("NOT HANDLED IN TEST!");
					}
				}
	
				@Override
				public void sentenceReady(String speakerChange, String sentence, int errors) {
					XDSData xds = vbi.getXDSData();
					if (xds.getProgramName() != null) {
						System.out.println("Program Name: "+xds.getProgramName());
						System.out.println("Network name = "+xds.getNetworkName());
						System.out.println("Call letters = "+xds.getCallLettersAndNativeChannel());
					}
					if (errors > 0) {
						System.out.println("Errors: "+errors+"...");
					}
					if (speakerChange != null) {
						System.out.print(speakerChange+": ");
					}
					System.out.println(sentence);
				}
			};
			
			VBICommand itvBuffer = new LineBuffer(DataChannel.TEXT2) {
				
				@Override
				public void lineReady(String line) {
					System.out.println("TEXT2 line: "+line);
				}
				
				@Override
				public void receivedITVLink(ITVLink itvLink) {
					System.out.println("ITVLink: "+itvLink);
				}
				
				@Override
				public void error(VBILine vbi, String errstr) {
					if (isSelectedMode(vbi)) {
						System.out.println("Error: "+errstr);
					}
				}
			};
			
			dev0.setChannel("10");
			for (int i = 0; i < 0 /*2000*/; i++) {
				dev0.getVBIDevice().readVBILine(vbi);
				vbi.executeCommand(sentenceBuffer);
			}
			
//while (true) {
while (/*false*/dev0==null) {
	for (int j=2; j<=60; j++) {
		dev0.setChannel(""+j);
		System.err.println("Channel "+j);
		for (int i = 0; i < 2000; i++) {
			dev0.getVBIDevice().readVBILine(vbi);
			vbi.executeCommand(sentenceBuffer);
			vbi.executeCommand(itvBuffer);
		}
	}
}
//		dev0.close();
		}
	}
}
