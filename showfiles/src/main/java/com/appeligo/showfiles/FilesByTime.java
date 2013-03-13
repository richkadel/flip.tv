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

package com.appeligo.showfiles;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class for Servlet: ShowFile
 *
 */
 public class FilesByTime extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	 
    /**
	 * 
	 */
	private static final long serialVersionUID = 4253806280094200390L;

	private static final Logger log = Logger.getLogger(FilesByTime.class);
    
	private static String documentRoot = "/tmp"; 
	
	static {
		try {
		    // Set up a simple configuration that logs on the console.
		    BasicConfigurator.configure();
	
			String configFile = "/etc/flip.tv/channelfeed.xml";
			
			XMLConfiguration config = new XMLConfiguration(configFile);
			
			documentRoot = config.getString("documentRoot[@path]");
			log.info("documentRoot = "+documentRoot);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public FilesByTime() {
		super();
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		/*
		System.out.println("contextPath = "+request.getContextPath());
		System.out.println("pathTranslated = "+request.getPathTranslated());
		System.out.println("queryString = "+request.getQueryString());
		System.out.println("pathInfo = "+request.getPathInfo());
		System.out.println("servletPath = "+request.getServletPath());
		*/
		String path = request.getPathInfo();
		if (path == null) {
			path = "/";
		}
		int limit = 0;
		String q = request.getQueryString();
		if (q != null) {
    		q = q.trim();
    		if (q.length() > 0) {
        		try {
        			limit = Integer.parseInt(q);
        		} catch (NumberFormatException e) {
        			// ignore
        		}
    		}
		}
		listFiles(request, out, documentRoot+path, limit);
	}

	private void addFile(Set<File> fileSet, File file) {
		if (file.isHidden()) {
			return;
		}
		if (file.isDirectory()) {
    		for (File child : file.listFiles()) {
    			addFile(fileSet, child);
    		}
		} else {
			fileSet.add(file);
		}
	}

	/**
	 * @param request
	 * @param out
	 * @param path
	 */
	private void listFiles(HttpServletRequest request, PrintWriter out, String path, int limit) {
		header(out);
		Comparator<File> comparator = new Comparator<File>() {
			public int compare(File leftFile, File rightFile) {
				long leftMod = leftFile.lastModified();
				long rightMod = rightFile.lastModified();
				if (leftMod < rightMod) {
					return -1;
				} else if (leftMod > rightMod) {
					return 1;
				} else {
					return leftFile.getPath().compareTo(rightFile.getPath());
				}
			}
		};
		SortedSet<File> fileSet = new TreeSet<File>(comparator);
		addFile(fileSet, new File(path));
		
        log.info("Total files in tree is "+fileSet.size());
        
		if (limit > 0 && fileSet.size() > limit) {
            log.info("Trimming tree to limit "+limit);
            Iterator<File> iter = fileSet.iterator();
            int toDrop = fileSet.size() - limit;
            for (int i = 0; i < toDrop; i++) {
            	iter.next();
            }
            File first = iter.next();
            fileSet = fileSet.tailSet(first);
		}
		
		int suggestedLimit = 1000;
		if (limit == 0 && fileSet.size() > suggestedLimit) {
			out.println("That's a lot of files!  There are "+fileSet.size()+" files to return.<br/>");
			out.println("How about just the <a href=\""+request.getRequestURI()+"?"+suggestedLimit+"\">last "+suggestedLimit+"</a>.<br/>");
			out.println("If you really want them all, <a href=\""+request.getRequestURI()+"?"+(fileSet.size()+suggestedLimit)+"\">click here</a>.<br/>");
		} else {
    		
    		DateFormat dateFormat = SimpleDateFormat.getDateInstance();
    		DateFormat timeFormat = SimpleDateFormat.getTimeInstance();
    		Calendar lastDay = Calendar.getInstance();
    		Calendar day = Calendar.getInstance();
    		boolean first = true;
    		
    		for (File file : fileSet) {
    			Date fileDate = new Date(file.lastModified());
    			day.setTime(fileDate);
    			if (first || lastDay.get(Calendar.DAY_OF_YEAR) != day.get(Calendar.DAY_OF_YEAR)) {
    				out.print("<b>"+dateFormat.format(fileDate)+"</b><br/>");
    			}
    			String servlet = "/ShowFile";
    			if (file.getPath().endsWith(".flv")) {
    				servlet = "/ShowFlv";
    			}
    			out.print(timeFormat.format(fileDate)+" <a href=\""+request.getContextPath()+servlet+
    				file.getPath().substring(documentRoot.length())+"\">"+file.getPath()+"</a>");
    			out.println("<br/>");
    			lastDay.setTime(fileDate);
    			first = false;
    		}
		}
		footer(out);
	}

	private void header(PrintWriter out) {
		out.println(
			"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+
			"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"+
			"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"+
			"<head>\n"+
			"<meta http-equiv='pragma' content='no-cache'>\n"+
			"<meta http-equiv='cache-control' content='no-store'>\n"+
			"<meta http-equiv='expires' content='-1'>\n"+
			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"+
			"<title>Files By Time</title>\n"+
			"</head>\n"+
			"<body>");
	}

	private void footer(PrintWriter out) {
		out.println(
			"</body>\n"+
			"<meta http-equiv='pragma' content='no-cache'>\n"+
			"<meta http-equiv='cache-control' content='no-store'>\n"+
			"<meta http-equiv='expires' content='-1'>\n"+
			"</html>");
	}
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}   	  	    
}
