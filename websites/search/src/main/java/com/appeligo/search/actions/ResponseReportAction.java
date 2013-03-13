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

package com.appeligo.search.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.appeligo.search.util.ConfigUtils;
import com.appeligo.util.Utils;

public class ResponseReportAction extends BaseAction {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = -7795615164855427697L;

	private static final Log log = LogFactory.getLog(ResponseReportAction.class);
	
	private static String documentRoot = "/tmp";
	
	static {
		documentRoot = ConfigUtils.getSystemConfig().getString("documentRoot[@path]", "/tmp");
	}
	
	private String reporter;
	private String status;
	private int bytesRead;
	private boolean timedOut;
	private String exception;
	private long responseMillis;

    public String execute() throws Exception {
		long timestamp = new Date().getTime();
		String day = Utils.getDatePath(timestamp);
		String hostname = null;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostname = "UnknownHost";
		}
		String dirname = documentRoot+"/stats/"+day+"/"+hostname;
		String responseFileName = dirname+"/response-"+reporter+".html";
		try {
			File dir = new File(dirname);
			if ((!dir.exists()) && (!dir.mkdirs())) {
				throw new IOException("Error creating directory "+dirname);
			}
			File file = new File(responseFileName);
			PrintStream responseFile = null;
			if (file.exists()) {
				responseFile = new PrintStream(new FileOutputStream(responseFileName, true));
			} else {
				responseFile = new PrintStream(new FileOutputStream(responseFileName));
				String title = "Response Times for "+getServletRequest().getServerName()+" to "+reporter;
				responseFile.println("<html><head><title>"+title+"</title></head>");
				responseFile.println("<body><h1>"+title+"</h1>");
				responseFile.println("<table border='1'>");
				responseFile.println("<tr>");
				responseFile.println("<th>Time (UTC)</th>");
				responseFile.println("<th>Response (Millis)</th>");
				responseFile.println("<th>Status</th>");
				responseFile.println("<th>Bytes Read</th>");
				responseFile.println("<th>Timed Out</th>");
				responseFile.println("<th>Exception</th>");
				responseFile.println("</tr>");
			}
			Calendar cal = Calendar.getInstance();
			cal.setTimeZone(TimeZone.getTimeZone("GMT"));
			cal.setTimeInMillis(timestamp);
			String time = String.format("%1$tH:%1$tM:%1$tS", cal);
			responseFile.print("<tr>");
			responseFile.print("<td>"+time+"</td>");
			responseFile.format("<td>%,d</td>", responseMillis);
			responseFile.print("<td>"+status+"</td>");
			responseFile.format("<td>%,d</td>", bytesRead);
			responseFile.print("<td>"+timedOut+"</td>");
			responseFile.print("<td>"+exception+"</td>");
			responseFile.println("</tr>");
		} catch (IOException e) {
			log.error("Error opening or writing to "+responseFileName, e);
		}
    	
        return SUCCESS;
    }
    
	public int getBytesRead() {
		return bytesRead;
	}

	public void setBytesRead(int bytesRead) {
		this.bytesRead = bytesRead;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public String getReporter() {
		return reporter;
	}

	public void setReporter(String reporter) {
		this.reporter = reporter;
	}

	public long getResponseMillis() {
		return responseMillis;
	}

	public void setResponseMillis(long responseMillis) {
		this.responseMillis = responseMillis;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isTimedOut() {
		return timedOut;
	}

	public void setTimedOut(boolean timedOut) {
		this.timedOut = timedOut;
	}
    
}
