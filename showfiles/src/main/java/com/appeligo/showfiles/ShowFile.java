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
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
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
 public class ShowFile extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	 
    /**
	 * 
	 */
	private static final long serialVersionUID = 4253806280094200390L;

	private static final Logger log = Logger.getLogger(ShowFile.class);
    
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
	public ShowFile() {
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
		
		File f = new File(documentRoot+path);
		if (!f.exists()) {
			noFile(out, path, f);
		} else if (f.isDirectory()) {
			listFiles(request, out, path, f);
		} else if (path.endsWith(".html.gz")) {
			showCaptionsForTagging(request, out, path);
		} else if (path.endsWith(".swf")) {
			showFlash(response, out, path);
		} else {
			writeContents(response, out, path);
		}
	}

	/**
	 * @param response
	 * @param out
	 * @param path
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void writeContents(HttpServletResponse response, PrintWriter out, String path) throws FileNotFoundException, IOException {
		if (!path.endsWith(".html")) {
			response.setContentType("text/plain");
		}
		BufferedReader r = new BufferedReader(
			new FileReader(documentRoot+path));
		String line;
		while ((line = r.readLine()) != null) {
			out.println(line);
		}
	}

	/**
	 * @param response
	 * @param out
	 * @param path
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void showFlash(HttpServletResponse response, PrintWriter out, String path) throws FileNotFoundException, IOException {
		response.setContentType("application/x-shockwave-flash");
		BufferedInputStream s = new BufferedInputStream(
			new FileInputStream(documentRoot+path));
		int ch;
		while ((ch = s.read()) >= 0) {
			out.write(ch);
		}
	}

	/**
	 * @param request
	 * @param out
	 * @param path
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void showCaptionsForTagging(HttpServletRequest request,
			PrintWriter out, String path) throws IOException, FileNotFoundException {
		long startTime = 0;
		long writtenStartTime = 0;	// An early bug had us writing the wrong
									// start/end time in the metadata, so if it
									// doesn't match the filename, don't use it
		int programDurationSeconds = 0;
		
		try {
			int lastslash = path.lastIndexOf('/');
			int dot = path.indexOf('.', lastslash+1);
			if (lastslash >= 0 && dot > 0) {
				startTime = Long.parseLong(path.substring(lastslash+1,dot));
			}
		} catch (NumberFormatException e) {
			log.error("Can't parse the filename into a start time!", e);
		}
		if (path.endsWith("/")) {
			path = path.substring(0, path.length()-1);
		}
		String fullPathname = documentRoot+path;
		
		boolean hasVideo = false;
		if (new File(fullPathname.replace(".html.gz", ".flv")).exists()) {
			hasVideo = true;
		}
		
		BufferedReader r = new BufferedReader(
					new InputStreamReader(
					new GZIPInputStream(
					new FileInputStream(fullPathname))));
		boolean addLinks = true;
		boolean addFunctions = true;
		String line;
		while ((line = r.readLine()) != null) {
			if (line.contains("writeMarkup()")) {
				addLinks = false;
			}
			if (line.contains("ShowFile.js")) {
				addFunctions = false;
			}
			if (hasVideo && startTime > 0) {
				String startMetaTag = "<meta name=\"StartTime\"";
				if (line.startsWith(startMetaTag)) {
					writtenStartTime = getQuotedLong(line, startMetaTag.length()+1);
				}
				if (line.startsWith("<meta name=\"EndTime\"")) {
					long writtenEndTime = getQuotedLong(line, startMetaTag.length()+1);
					if ((startTime > 0) &&
						(writtenEndTime > startTime)) {
						programDurationSeconds = (int)((writtenEndTime - writtenStartTime)/1000);
					}
				}
				if (line.startsWith("<a name=")) {
					long timestamp = 0;
					timestamp = getQuotedLong(line, 0);
					
					if (timestamp > startTime) {
						int fastForwardSeconds = Math.max(0, (int)((timestamp - startTime) / 1000) - 20);
							// Start 20 seconds before the caption
						int remainingSeconds = programDurationSeconds - fastForwardSeconds;
						if (writtenStartTime != startTime) {
							remainingSeconds = 2*60; // we don't know, so show 2 of video
						}
						out.println("<a href=\""+request.getContextPath()+
								"/ShowFlv"+
								request.getPathInfo().replace(".html.gz", ".flv")+
								"?start="+fastForwardSeconds+"&duration="+remainingSeconds+"\">"+
							    "<img src=\""+request.getContextPath()+"/skins/default/videoIcon.gif\" alt=\"video\"/>"+
								"</a>");
					}
				}
			}
			if (line.trim().equals("</body>")) {
				//out.println("</pre>");
				if (addLinks) {
					out.println(
						"<p/><a href='javascript:writeMarkup()'>Write Markup</a>"+
						" (Warning! This overwrites any previously stored markup for the same file.)\n"+
						"<div id='messageDiv'></div>\n");
					out.println("<p/><a href='"+request.getContextPath()+
							request.getServletPath()+path.substring(0,path.lastIndexOf("/"))+
							"'>Back</a>");
				}
			}
			if (line.trim().equals("</head>")) {
				if (addFunctions) {
					writeFunctions(out);
					writeStyle(out);
				}
			}
			out.println(line);
		}
		r.close();
	}

	/**
	 * @param request
	 * @param out
	 * @param filename
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void showPreview(HttpServletRequest request,
			PrintWriter out, String path, String filename) {
		
		if (filename.endsWith("/")) {
			filename = filename.substring(0, filename.length()-1);
		}
		
		int lastslash = filename.lastIndexOf('/');
		int comma = filename.lastIndexOf(',');
		int minus = filename.lastIndexOf('-');
		int dot = filename.lastIndexOf('.');
		
		String programID = filename.substring(lastslash+1,comma);
		
		long clipStartSecs = 0;
		long clipLengthSecs = 0;
		try {
			clipStartSecs = Long.parseLong(filename.substring(comma+1,minus));
			clipLengthSecs = Long.parseLong(filename.substring(minus+1,dot));
		} catch (NumberFormatException e) {
			log.error("Can't parse the filename into a start time!", e);
			throw e;
		} catch (StringIndexOutOfBoundsException e) {
			log.error("Can't parse the filename for preview time info", e);
			throw e;
		}
		
		out.println("<a href=\""+request.getContextPath()+
								"/ShowFlv"+
								path+
								filename+
								"?start="+0+"&duration="+clipLengthSecs+"\">"+
							    "<img src=\""+request.getContextPath()+"/skins/default/videoIcon.gif\" alt=\"video\"/>"+
							    (clipStartSecs/60)+":"+String.format("%02d", (clipStartSecs%60))+
								"</a>");
		
		File titleFile = new File(documentRoot+path+programID+".title");
		if (titleFile.exists()) {
			try {
				FileReader titleReader = new FileReader(titleFile);
				BufferedReader lineReader = new BufferedReader(titleReader);
				out.println(" "+lineReader.readLine());
				titleReader.close();
			} catch (IOException e) {
				out.println("(Error reading title file) "+e.getMessage());
			}
		}
    }
	
	/**
	 * @param line
	 * @param timestamp
	 * @return
	 */
	private long getQuotedLong(String line, int startIndex) {
		long longValue = 0;
		try {
			int quote1 = line.indexOf('"', startIndex);
			int quote2 = line.indexOf('"', quote1+1);
			if (quote1 > 0 && quote2 > 0) {
				longValue = Long.parseLong(line.substring(quote1+1,quote2));
			}
		} catch (NumberFormatException e) {
		}
		return longValue;
	}

	/**
	 * @param request
	 * @param out
	 * @param path
	 * @param f
	 */
	private void listFiles(HttpServletRequest request, PrintWriter out, String path, File f) {
		header(out, path);
		String[] filenames = f.list();
		Arrays.sort(filenames);
		for (int i = 0; i < filenames.length; i++) {
			String filename = filenames[i];
			String fullPathname = documentRoot+path+"/"+filename;
			File child = new File(fullPathname);
			if (child.isHidden()) {
				continue;
			}
			if (filename.endsWith(".flv")) {
				if (new File(fullPathname.replace(".flv", ".html.gz")).exists()) {
					continue;
				}
			}
			filename = filename.replace(":", "%3a");
			if (!path.endsWith("/")) {
				path = path+"/";
			}
			String displayName = filenames[i];
			if (child.isDirectory()) {
				displayName += "/";
				if (!filename.endsWith("/")) {
					filename = filename+"/";
				}
			}
			if (filename.endsWith(".html.gz")) {
				if (new File(fullPathname.replace(".html.gz", ".flv")).exists()) {
					out.println("<a href=\""+request.getContextPath()+
						"/ShowFlv"+
						//request.getServletPath()+
						path+filename.replace(".html.gz", ".flv")+"\">"+
					    "<img src=\""+request.getContextPath()+"/skins/default/videoIcon.gif\" alt=\"video\"/>"+
						"</a>");
				}
				String tsString = filename.substring(0,filename.indexOf('.'));
				try {
					long timestamp = Long.parseLong(tsString);
					DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
					df.setTimeZone(TimeZone.getTimeZone("GMT"));
					String time = df.format(new Date(timestamp))+" - ";
					if (time.indexOf(':') == 1) {
						time = "0"+time;
					}
					out.print(time);
				} catch (NumberFormatException e) {
				}
    		} else if (filename.endsWith(".flv") && (filename.indexOf(",") > 0)) {
    			showPreview(request, out, path, filename);
    		}
			out.print("<a href=\""+request.getContextPath()+
				request.getServletPath()+
				path+filename+"\">"+displayName+"</a>");
			if (filename.endsWith(".html.gz")) {
				BufferedReader r;
				try {
					r = new BufferedReader(
							new InputStreamReader(
							new GZIPInputStream(
							new FileInputStream(fullPathname))));
					String line;
					while ((line = r.readLine()) != null) {
						if (line.startsWith("<title>")) {
							int nextLT = line.indexOf('<', 7);
							String title = line.substring(7,nextLT);
							out.print(" "+title);
							break;
						}
					}
					r.close();
				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				}
			}
			out.println("<br/>");
		}
		footer(out);
	}

	/**
	 * @param out
	 * @param path
	 * @param f
	 */
	private void noFile(PrintWriter out, String path, File f) {
		header(out, path);
		out.println(f.getAbsolutePath()+" does not exist");
		footer(out);
	}
	
	private void header(PrintWriter out, String path) {
		out.println(
			"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+
			"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"+
			"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"+
			"<head>\n"+
			"<meta http-equiv='pragma' content='no-cache'>\n"+
			"<meta http-equiv='cache-control' content='no-store'>\n"+
			"<meta http-equiv='expires' content='-1'>\n"+
			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"+
			"<title>Show File "+path+"</title>\n"+
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
	
	private void writeFunctions(PrintWriter out) {
		out.println("<script type='text/javascript' src='/showfiles/lib/YahooUI/yahoo/yahoo.js'></script>");
		out.println("<script type='text/javascript' src='/showfiles/lib/YahooUI/connection/connection.js'></script>");
		out.println("<script type='text/javascript' src='/showfiles/ShowFile.js'></script>");
	}
	
	private void writeStyle(PrintWriter out) {
		out.println("<style>");
		out.println("span.speaker { display: none }");
//		out.println("span.timestamp { display: none }");
		out.println("</style>");
	}
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}   	  	    
}
