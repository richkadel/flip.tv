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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Formatter;

import static com.knowbout.cc4j.XDSField.*;

public class XDSData implements Serializable {
	
	private static final long serialVersionUID = 8803327976788915600L;

	private static String xdsProgramTypes[] = {
		"Education",	// 0x20
		"Entertainment",// 0x21
		"Movie",		// 0x22
		"News",			// 0x23
		"Religious",	// 0x24
		"Sports",		// 0x25
		"OTHER",		// 0x26
		"Action",		// 0x27
		"Advertisement",// 0x28
		"Animated",		// 0x29
		"Anthology",	// 0x2A
		"Automobile",	// 0x2B
		"Awards",		// 0x2C
		"Baseball",		// 0x2D
		"Basketball",	// 0x2E
		"Bulletin",		// 0x2F
		"Business",		// 0x30
		"Classical",	// 0x31
		"College",		// 0x32
		"Combat",		// 0x33
		"Comedy",		// 0x34
		"Commentary",	// 0x35
		"Concert",		// 0x36
		"Consumer",		// 0x37
		"Contemporary",	// 0x38
		"Crime",		// 0x39
		"Dance",		// 0x3A
		"Documentary",	// 0x3B
		"Drama",		// 0x3C
		"Elementary",	// 0x3D
		"Eritica",		// 0x3E
		"Exercise",		// 0x3F
		"Fantasy",		// 0x40
		"Farm",			// 0x41
		"Fashion",		// 0x42
		"Fiction",		// 0x43
		"Food",			// 0x44
		"Football",		// 0x45
		"Foreign",		// 0x46
		"Fund Raiser",	// 0x47
		"Game/Quiz",	// 0x48
		"Garden",		// 0x49
		"Golf",			// 0x4A
		"Government",	// 0x4B
		"Health",		// 0x4C
		"High School",	// 0x4D
		"History",		// 0x4E
		"Hobby",		// 0x4F
		"Hockey",		// 0x50
		"Home",			// 0x51
		"Horror",		// 0x52
		"Information",	// 0x53
		"Instruction",	// 0x54
		"International",// 0x55
		"Interview",	// 0x56
		"Language",		// 0x57
		"Legal",		// 0x58
		"Live",			// 0x59
		"Local",		// 0x5A
		"Math",			// 0x5B
		"Medical",		// 0x5C
		"Meeting",		// 0x5D
		"Military",		// 0x5E
		"Miniseries",	// 0x5F
		"Music",		// 0x60
		"Mystery",		// 0x61
		"National",		// 0x62
		"Nature",		// 0x63
		"Police",		// 0x64
		"Politics",		// 0x65
		"Premiere",		// 0x66
		"Prerecorded",	// 0x67
		"Product",		// 0x68
		"Professional",	// 0x69
		"Public",		// 0x6A
		"Racing",		// 0x6B
		"Reading",		// 0x6C
		"Repair",		// 0x6D
		"Repeat",		// 0x6E
		"Review",		// 0x6F
		"Romance",		// 0x70
		"Science",		// 0x71
		"Series",		// 0x72
		"Service",		// 0x73
		"Shopping",		// 0x74
		"Soap Opera",	// 0x75
		"Special",		// 0x76
		"Suspense",		// 0x77
		"Talk",			// 0x78
		"Technical",	// 0x79
		"Tennis",		// 0x7A
		"Travel",		// 0x7B
		"Variety",		// 0x7C
		"Video",		// 0x7D
		"Weather",		// 0x7E
		"Western",		// 0x7F
	};
	
	private static String xdsLanguages[] = {
		"Unknown",
		"English",
		"Spanish",
		"French",
		"German",
		"Italian",
		"Other",
		"None",
	};
	
	private static String xdsMainAudioTypes[] = {
		"Unknown",
		"Mono",
		"Simulated Stereo",
		"True Stereo",
		"Stereo Surround",
		"Data Service",
		"Other",
		"None",
	};
		
	private static String xdsSAPAudioTypes[] = {
		"Unknown",
		"Mono",
		"Audio Description",
		"Non-program Audio",
		"Special Effects",
		"Data Service",
		"Other",
		"None",
	};
	
	private static String xdsCaptionServices[] = {
		"CC1",
		"TEXT1",
		"CC2",
		"TEXT2",
		"CC3",
		"TEXT3",
		"CC4",
		"TEXT4",
	};
		
	private int control;
	private int type;
	private boolean activePacket;
	private StringBuffer vbiBuffer = new StringBuffer();
	private String programStartTimeID;
	private String programLength;
	private String programName;
	private String networkName;
	private String callLettersAndNativeChannel;
	private String programType;
	private String captionServices;

	public boolean setCode(int c1, int c2) {
		if ((c1 % 2) == 1) { // start new packet
			resetPacket();
			activePacket = true;
			control = c1;
			type = c2;
		} else {  // continue code
			if (control > 0) { // is this the same packet we started?
				if (((c1 - 1) == control) && (c2 == type)) { // we're back in
					activePacket = true;
				} else { // does not match start code or type
					resetPacket();
				}
			}
		}
		return activePacket;
	}
	
	public void interrupt() {
		activePacket = false;
	}
	
	public XDSField endPacket(int checksum) {
		if (control == 0) {
			return NONE;
		}
		// calculate checksum to compare, as described in
		//  The Closed Captioning Handbook
		int calcChecksum = 0;
		int len = vbiBuffer.length();
		for (int i = 0; i < len; i++) {
			calcChecksum +=  vbiBuffer.charAt(i);
		}
		calcChecksum += (control + type + 0x0F + checksum);
		calcChecksum &= 0x7F;
		if (calcChecksum != 0) {
			return NONE;
		}
		
		switch (control) {
		case 0x01:
			return current();
			
		case 0x03:
			return future();
			
		case 0x05:
			return channel();
			
		case 0x07:
			return miscellaneous();
			
		case 0x09:
			return publicService();
			
		case 0x0B:
			return reserved();
			
		case 0x0D:
			return privateData();
			
		default:
			throw new Error("Invalid code.  This should be impossible.");
		}
	}
	
	private String vbiToUnicodeString(StringBuffer vbiBuffer) {
		int len = vbiBuffer.length();
		for (int i = 0; i < len; i++) {
			vbiBuffer.setCharAt(i,
					VBILine.vbiToUnicode(vbiBuffer.charAt(i)));
		}
		return vbiBuffer.toString();
	}
		
	private XDSField current() {
		// TODO Auto-generated method stub
		switch (type) {
		case 0x01:
			programStartTimeID = vbiToUnicodeString(vbiBuffer);
			return PROGRAM_START_TIME_ID;
			
		case 0x02:
			programLength = vbiToUnicodeString(vbiBuffer);
			return PROGRAM_LENGTH;
			
		case 0x03:
			programName = vbiToUnicodeString(vbiBuffer);
			return PROGRAM_NAME;
			
		case 0x04:
			programType = vbiBuffer.toString();
			return PROGRAM_TYPE;
			
		case 0x05:
			return NONE; // TODO V-Chip
			
		case 0x07:
			captionServices = vbiBuffer.toString();
			return CAPTION_SERVICES;
			
		}
//System.out.println("XDS Current: Not yet handling type "+type+", buffer "+vbiBuffer);
		return NONE;
	}
	
	private XDSField future() {
		// TODO Auto-generated method stub
//System.out.println("XDS Future: Not yet handling type "+type+", buffer "+vbiBuffer);
		return NONE;
	}

	private XDSField channel() {
		// TODO Auto-generated method stub
		switch (type) {
		case 0x01:
			networkName = vbiToUnicodeString(vbiBuffer);
			return NETWORK_NAME;
			
		case 0x02:
			callLettersAndNativeChannel = vbiToUnicodeString(vbiBuffer);
			return CALL_LETTERS_AND_NATIVE_CHANNEL;
			
		case 0x04:
			return NONE; // TODO Handle TSID data
			
		}
//System.out.println("XDS Channel: Not yet handling type "+type+", buffer "+vbiBuffer);
		return NONE;
	}

	private XDSField miscellaneous() {
		// TODO Auto-generated method stub
//System.out.println("XDS Miscellaneous: Not yet handling type "+type+", buffer "+vbiBuffer);
		return NONE;
	}

	private XDSField publicService() {
		// TODO Auto-generated method stub
//System.out.println("XDS Public Service: Not yet handling type "+type+", buffer "+vbiBuffer);
		return NONE;
	}

	private XDSField reserved() {
		// TODO Auto-generated method stub
//System.out.println("XDS Reserved: Not yet handling type "+type+", buffer "+vbiBuffer);
		return NONE;
	}

	private XDSField privateData() {
		// TODO Auto-generated method stub
//System.out.println("XDS Private Data: Not yet handling type "+type+", buffer "+vbiBuffer);
		return NONE;
	}

	private void resetPacket() {
		control = 0;
		activePacket = false;
		vbiBuffer.setLength(0);
	}
	
	public void addCharacters(int c1, int c2) {
		// TODO Auto-generated method stub
		vbiBuffer.append((char)c1);
		if (c2 > 0) {
			vbiBuffer.append((char)c2);
		}
	}

	public boolean isInPacket() {
		// TODO Auto-generated method stub
		return activePacket;
	}
	
	public String getCurrentPacketInfo() {
		StringBuffer tsbuf = new StringBuffer();
		tsbuf.append("\nXDS Packet Data");
		if (control > 0) {
			tsbuf.append("\n\tControl code: "+control);
			tsbuf.append("\n\tType code: "+type);
			tsbuf.append("\n\tActive Packet (incomplete): "+activePacket);
			tsbuf.append("\n\tVBI buffer (so far):"+activePacket);
		}
		return tsbuf.toString();
	}
	
	public String getProgramName() { 
		return programName;
	}
	
	public void setProgramName(String programName) {
		this.programName = programName;
	}
	
	public String getNetworkName() { 
		return networkName;
	}
	
	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}
	
	public String getCallLettersAndNativeChannel() {
		return callLettersAndNativeChannel;
	}

	public void setCallLettersAndNativeChannel(String callLettersAndNativeChannel) {
		this.callLettersAndNativeChannel = callLettersAndNativeChannel;
	}
	
	public String getProgramType() {
		return programType;
	}
	
	public void setProgramType(String programType) {
		this.programType = programType;
	}

	public String getProgramStartTimeID() {
		return programStartTimeID;
	}
	
	public void setProgramStartTimeID(String programStartTimeID) {
		this.programStartTimeID = programStartTimeID;
	}

	public String getProgramLength() {
		return programLength;
	}
	
	public void setProgramLength(String programLength) {
		this.programLength = programLength;
	}

	public String toString() {
		return "XDS: startTimeID="+programStartTimeID+", program="+programName+", network="+networkName+", call="+callLettersAndNativeChannel+
				", programType="+programType;
	}

	public static String convertProgramType(String programType) {
		int len = programType.length();
		StringBuffer expanded = new StringBuffer();
		for (int i = 0; i < len; i++) {
			if (i > 0) {
				expanded.append(", ");
			}
			expanded.append(xdsProgramTypes[programType.charAt(i)-0x20]);
		}
		return expanded.toString();
	}

	public String getCaptionServices() {
		return captionServices;
	}

	public static String convertCaptionServices(String captionServices) {
		int len = captionServices.length();
		StringBuffer expanded = new StringBuffer();
		for (int i = 0; i < len; i++) {
			if (i > 0) {
				expanded.append(", ");
			}
			char ch = captionServices.charAt(i);
			expanded.append(xdsCaptionServices[ch&0x7]);
			expanded.append(": ");
			expanded.append(xdsLanguages[ch >> 3 & 0x7]);
		}
		return expanded.toString();
	}
	
	public static boolean convertProgramTapeDelayed(String programStartTimeID) {
		if (programStartTimeID.length() < 3) {
			return false;
		}
		return tapeDelayed(programStartTimeID.charAt(2));
	}
	
	public static long convertProgramStartTimeID(String programStartTimeID) {
		if (programStartTimeID.length() < 4) {
			return 0;
		}
		int minute = minute(programStartTimeID.charAt(0));
		int hour = hour(programStartTimeID.charAt(1));
		int day = dayOfMonth(programStartTimeID.charAt(2));
		int month = month(programStartTimeID.charAt(3));
		
		Calendar cal = Calendar.getInstance();
		int monthNow = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		if ((monthNow == Calendar.JANUARY) && 
				(month == Calendar.DECEMBER)) {
			year--;
		} else if ((monthNow == Calendar.DECEMBER) && 
				(month == Calendar.JANUARY)) {
			year++;
		}
		
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTimeInMillis();
	}

	public static String convertProgramLength(String programLengthAndTimeInShow) {
		if (programLengthAndTimeInShow.length() < 2) {
			return "00:00";
		}
		int minute = minute(programLengthAndTimeInShow.charAt(0));
		int hour = hour(programLengthAndTimeInShow.charAt(1));
		
	    StringBuilder sb = new StringBuilder();
	    Formatter formatter = new Formatter(sb);
	    formatter.format("%02d:%02d", hour, minute);

		return sb.toString();
	}
	
	public static int second(char code) {
		return code & 0x3F;
	}
	
	public static int minute(char code) {
		return code & 0x3F;
	}
	
	public static int hour(char code) {
		/*boolean daylightSavingsTime = ((code & 0x20) == 0x20);*/
		return code & 0x1F;
	}
	
	/**
	 * Return the day of the month, also known as the "Date" field.
	 * Bit 5 tells me if it's a leap year or not.  This helpful hint
	 * is not needed.
	 * @param code
	 * @return
	 */
	public static int dayOfMonth(char code) {
		/*boolean leap = ((code & 0x20) == 0x20);*/
		return code & 0x1F;
	}
	
	public static int dayOfWeek(char code) {
		return code & 0x7;
	}
	
	public static int month(char code) {
		return (code & 0xF) - 1; // Java Calendar starts month at 0 == January
	}
	
	/**
	 * If true, use "Tape Delay" field (to characters, minutes then hours)
	 * to determine the offset.  Tape Delay field is Type=0x03 of the
	 * Channel Information Class (start control code=0x05, continue=0x06).
	 * @param code
	 * @return
	 */
	public static boolean tapeDelayed(char code) {
		return ((code & 0x10) == 0x10);
	}
	
	public static int year(char code) {
		return 1990 + (code & 0x3F);
	}
}
