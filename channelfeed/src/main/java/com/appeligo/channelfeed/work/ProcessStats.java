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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.appeligo.util.Utils;

public class ProcessStats {
	
	private static final Logger log = Logger.getLogger(ProcessStats.class);

	private static String currentDay;
	private static String hostname;
	private static PrintStream statsFile;
	private static long lastWrite;

	private static TreeMap<String,CaptionPerformance> callsigns;
	
	public synchronized static void checkStats(String ccDocumentRoot) {
		long timestamp = new Date().getTime();
		String day = Utils.getDatePath(timestamp);
		if (!day.equals(currentDay)) {
			if (statsFile != null) {
				statsFile.println("</table></body></html>");
				statsFile.close();
				statsFile = null;
			}
			currentDay = day;
		}
		String documentRoot = ccDocumentRoot.substring(0, ccDocumentRoot.lastIndexOf("/", ccDocumentRoot.length()-2));
		if (hostname == null) {
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				hostname = "UnknownHost";
			}
		}
		String dirname = documentRoot+"/stats/"+currentDay+"/"+hostname;
		String statsFileName = dirname+"/cfprocstats.html";
		int interval = 5; // minutes
		try {
			if (statsFile == null) {
				File dir = new File(dirname);
				if ((!dir.exists()) && (!dir.mkdirs())) {
					throw new IOException("Error creating directory "+dirname);
				}
				File file = new File(statsFileName);
				if (file.exists()) {
					statsFile = new PrintStream(new FileOutputStream(statsFileName, true));
				} else {
					statsFile = new PrintStream(new FileOutputStream(statsFileName));
					String title = "Channelfeed Process Statistics for "+currentDay;
					statsFile.println("<html><head><title>"+title+"</title></head>");
					statsFile.println("<body><h1>"+title+"</h1>");
					statsFile.println("<table border='1'>");
				}
				statsFile.println("<tr>");
				statsFile.println("<th colspan='2'>"+interval+" Minute Intervals</th><th colspan='3'>Memory</th>");
				for (String callsign : callsigns.keySet()) {
					CaptionPerformance captionPerformance = callsigns.get(callsign);
					statsFile.println("<th colspan='2'>"+callsign+
							"<br/>dev: "+captionPerformance.deviceNumber+
							", ch:"+captionPerformance.channel+"</th>");
				}
				statsFile.println("</tr>");
				statsFile.println("<tr>");
				statsFile.println("<th>Timestamp</th>");
				statsFile.println("<th>Time</th>");
				statsFile.println("<th>Used</th>");
				statsFile.println("<th>Committed</th>");
				statsFile.println("<th>Max</th>");
				for (String callsign : callsigns.keySet()) {
					statsFile.println("<th>Sentences</th>");
					statsFile.println("<th>Errors</th>");
				}
				statsFile.println("</tr>");
			}
			if ((timestamp-lastWrite) > (interval * 60 * 1000)) {
				lastWrite = timestamp;
				Calendar cal = Calendar.getInstance();
				cal.setTimeZone(TimeZone.getTimeZone("GMT"));
				cal.setTimeInMillis(timestamp);
				String time = String.format("%1$tH:%1$tM:%1$tS", cal);
				MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
				MemoryUsage memory = memoryBean.getHeapMemoryUsage();
				statsFile.print("<tr>");
				statsFile.print("<td>"+timestamp+"</td>");
				statsFile.print("<td>"+time+"</td>");
				statsFile.format("<td>%,d</td>", memory.getUsed());
				statsFile.format("<td>%,d</td>", memory.getCommitted());
				statsFile.format("<td>%,d</td>", memory.getMax());
				for (String callsign : callsigns.keySet()) {
					CaptionPerformance captionPerformance = callsigns.get(callsign);
    				statsFile.format("<td>%,d</td>", captionPerformance.sentences);
    				statsFile.format("<td>%,d</td>", captionPerformance.errors);
    				captionPerformance.sentences = 0;
    				captionPerformance.errors = 0;
				}
				statsFile.println("</tr>");
			}
		} catch (IOException e) {
			log.error("Error opening or writing to "+statsFileName, e);
		}
	}

	public static void error(String callsign, int errors) {
		callsigns.get(callsign).errors += errors;
	}

	public static void sentence(String callsign) {
		callsigns.get(callsign).sentences++;
	}

	public synchronized static void addCallsign(String callsign) {
		if (callsigns == null) {
    		callsigns = new TreeMap<String,CaptionPerformance>();
		}
		if (callsigns.get(callsign) == null) {
    		callsigns.put(callsign, new CaptionPerformance());
		}
	}

	public static void setDeviceNumber(String callsign, int deviceNumber) {
		addCallsign(callsign);
		callsigns.get(callsign).deviceNumber = deviceNumber;
	}

	public static void setChannel(String callsign, String channel) {
		addCallsign(callsign);
		callsigns.get(callsign).channel = channel;
	}
	
}
