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

package com.appeligo.responsetest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.commons.configuration.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.helpers.DateLayout;
import org.apache.log4j.net.SMTPAppender;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.varia.LevelRangeFilter;

public class ServerResponseChecker {

    private static final Logger log = Logger.getLogger(ServerResponseChecker.class);
    
    private static URL url;
    private static HttpURLConnection connection;
    private static boolean done = false;
	public static String status = "NO-STATUS";
	private static int bytesRead;
	private static Throwable throwable;
	private static long responseMillis;

	//private static int smtpPort = 3535; // NO WAY TO SET THIS!! WILL IT WORK WITHOUT IT?  Seems to
	private static String logFile = "/var/log/flip.tv/ServerResponseChecker.out";
    private static String servlet = "http://localhost:8080";
    private static long timeoutSeconds = 30;
	private static long responseTimeThresholdSeconds = 10;
    private static String reporter = "rich.kadel";
	private static String smtpServer = "smtpout.secureserver.net";
	private static String smtpUsername = "alerts@appeligo.com";
	private static String smtpPassword = "5tarfi5h";
	private static boolean smtpDebug = true;
    private static String mailTo = "errors@appeligo.com";
    
    private static String marker;
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	    PatternLayout pattern = new PatternLayout("%d{ISO8601} %-5p [%-c{1} - %t] - %m%n");
	    ConsoleAppender consoleAppender = new ConsoleAppender(pattern);
	    LevelRangeFilter infoFilter = new LevelRangeFilter();
	    infoFilter.setLevelMin(Level.INFO);
	    consoleAppender.addFilter(infoFilter);
	    BasicConfigurator.configure(consoleAppender);
		
		String configFile = "/etc/flip.tv/responsetest.xml";
		
		if (args.length > 0) {
			if (args.length == 2 && args[0].equals("-config")) {
				configFile = args[1];
			} else {
				log.error("Usage: java "+ServerResponseChecker.class.getName()+" [-config <xmlfile>]");
				System.exit(1);
			}
		}
		
		try {
			XMLConfiguration config = new XMLConfiguration(configFile);
			
			logFile = config.getString("logFile", logFile);
			servlet = config.getString("servlet", servlet);
		    timeoutSeconds = config.getLong("timeoutSeconds", timeoutSeconds);
		    responseTimeThresholdSeconds = config.getLong("responseTimeThresholdSeconds", responseTimeThresholdSeconds);
			reporter = config.getString("reporter", reporter);
			smtpServer = config.getString("smtpServer", smtpServer);
			smtpUsername = config.getString("smtpUsername", smtpUsername);
			smtpPassword = config.getString("smtpPassword", smtpPassword);
		    smtpDebug = config.getBoolean("smtpDebug", smtpDebug);
			mailTo = config.getString("mailTo", mailTo);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		
		marker = logFile+".mailed";
		
	    try {
			BasicConfigurator.configure(new RollingFileAppender(pattern, logFile, true));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	    // Add email appender
	    SMTPAppender mailme = new SMTPAppender();
	    LevelRangeFilter warnFilter = new LevelRangeFilter();
	    warnFilter.setLevelMin(Level.WARN);
	    mailme.addFilter(warnFilter);
	    mailme.setSMTPDebug(smtpDebug);
	    mailme.setSMTPHost(smtpServer);
	    mailme.setTo(mailTo);
	    mailme.setFrom(reporter+" <"+smtpUsername+">");
	    mailme.setBufferSize(1);
	    mailme.setSubject(servlet+" Not Responding!");
	    mailme.setSMTPUsername(smtpUsername);
	    mailme.setSMTPPassword(smtpPassword);
		mailme.setLayout(new SimpleLayout());
		mailme.activateOptions();
		mailme.setLayout(pattern);
	    BasicConfigurator.configure(mailme);
	    
		long before;
		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.start();
		synchronized(connectionThread) {
			connectionThread.setOkToGo(true);
			connectionThread.notifyAll();
			before = System.currentTimeMillis();
			long delay = timeoutSeconds * 1000;
			while (!done && delay > 0) {
				try {
					connectionThread.wait(delay);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				delay -= (System.currentTimeMillis() - before);
			}
		}
		long after = System.currentTimeMillis();
		responseMillis = after - before;
		String reportStatus = "Could not report";
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(servlet+"/responsetest/report.action");
			sb.append("?reporter="+URLEncoder.encode(reporter));
			sb.append("&status="+URLEncoder.encode(status));
			sb.append("&bytesRead="+bytesRead);
			sb.append("&timedOut="+(!done));
			if (throwable == null) {
				sb.append("&exception=none");
			} else {
				sb.append("&exception="+URLEncoder.encode(throwable.getClass().getName()+"-"+throwable.getMessage()));
			}
			sb.append("&responseMillis="+responseMillis);
			URL reportURL = new URL(sb.toString());
			connection = (HttpURLConnection) reportURL.openConnection();
			connection.connect();
			reportStatus = connection.getResponseCode()+" - "+connection.getResponseMessage();
		} catch (Throwable t) {
			reportStatus = t.getClass().getName()+"-"+t.getMessage();
		}
		StringBuilder sb = new StringBuilder();
		sb.append(servlet+": ");
		sb.append(status+", "+bytesRead+" bytes, ");
		if (done) {
			sb.append("DONE, ");
		} else {
			sb.append("TIMED OUT, ");
		}
		sb.append(responseMillis+" millisecond response, ");
		sb.append(" report status="+reportStatus);
		File markerFile = new File(marker);
		if (done && status.startsWith("200") && (throwable == null)) {
			if ((responseMillis / 1000) < responseTimeThresholdSeconds) {
				if (markerFile.exists()) {
					markerFile.delete();
				}
				log.debug(sb.toString());
			} else {
				if (markerFile.exists()) {
					log.info(sb.toString());
				} else {
					try {
						new FileOutputStream(marker).close();
						log.warn(sb.toString());
					} catch (IOException e) {
						log.info(sb.toString());
						log.info("Can't send email alert because could not write marker file: "+marker+". "+e.getMessage());
					}
				}
			}
		} else {
			if (throwable != null) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				throwable.printStackTrace(pw);
				sb.append(sw.toString());
			}
			if (markerFile.exists()) {
				log.info(sb.toString());
			} else {
				try {
					new FileOutputStream(marker).close();
					log.fatal(sb.toString()); // chosen appender layout ignoresThrowable()
				} catch (IOException e) {
					log.info(sb.toString());
					log.info("Can't send email alert because could not write marker file: "+marker+". "+e.getMessage());
				}
			}
		}
	}
	
	static class ConnectionThread extends Thread {
		
		boolean okToGo = false;
		
		public ConnectionThread() {
			super("ConnectionThread");
		}
		
		public void setOkToGo(boolean okToGo) {
			this.okToGo = okToGo;
		}
		
		public synchronized void run() {
			while (!okToGo) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
			try {
				url = new URL(servlet+"/responsetest/check.vm");
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();
				status = connection.getResponseCode()+" - "+connection.getResponseMessage();
				ServerResponseChecker.status = status;
				if (status.startsWith("200")) {
					InputStream is = connection.getInputStream();
					bytesRead = 0;
					while (is.read() >= 0) {
						bytesRead++;
					}
				}
			} catch (Throwable t) {
				throwable = t;
			} finally {
				done = true;
				notify();
			}
		}
	}
}
