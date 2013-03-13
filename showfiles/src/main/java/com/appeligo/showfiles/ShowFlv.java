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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.sun.corba.se.spi.orbutil.threadpool.ThreadPool;
import java.io.StringWriter;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.tools.view.servlet.VelocityViewServlet;
import org.apache.velocity.tools.view.servlet.WebappLoader;

/**
 * Servlet implementation class for Servlet: ShowFile
 *
 */
 public class ShowFlv extends VelocityViewServlet {
	 
    /**
	 * 
	 */
	private static final long serialVersionUID = 3003467672700494260L;

	private static final Logger log = Logger.getLogger(ShowFlv.class);
    
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public ShowFlv() {
		super();
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		/*

		try {
			Velocity.init();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		init(getServletConfig());
		
		VelocityContext context = new VelocityContext();

		context.put( "title", "Flip.TV Video Recall" );
		context.put( "server", request.getServerName());
		context.put( "contextPath", request.getContextPath());
		context.put( "flvUrl", request.getPathInfo());
		int start = 0;
		try {
			start = Integer.parseInt(request.getParameter("start"));
		} catch (NumberFormatException e) {
		}
		int duration = 0;
		try {
			duration = Integer.parseInt(request.getParameter("duration"));
		} catch (NumberFormatException e) {
		}
		context.put( "start", new Integer(start) );
		// Note, there is a bug in FlowPlayer where you have to add the
		// start time TWICE to the duration to get the "workable" value
		// for the "end" parameter.
		if (duration > 0) {
			context.put( "end", new Integer((start*2)+duration));
		}

		Template template = null;

		try {
		   template = getTemplate("/FlowPlayer.vm");
		} catch( ResourceNotFoundException rnfe ) {
		   // couldn't find the template
			rnfe.printStackTrace();
		} catch( ParseErrorException pee ) {
		  // syntax error : problem parsing the template
			pee.printStackTrace();
		} catch( MethodInvocationException mie ) {
		  // something invoked in the template
		  // threw an exception
			mie.printStackTrace();
		} catch( Exception e ) {
			e.printStackTrace();
		}

		try {
			template.merge( context, out ); // sw );
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MethodInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}   	  	    
}
