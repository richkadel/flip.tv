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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Servlet implementation class for Servlet: ShowFile
 *
 */
 public class PutFile extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	 
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(PutFile.class);
    
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
	public PutFile() {
		super();
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BufferedReader r = request.getReader();
		
		String name = (String)request.getParameter("name");
		
		PrintWriter out = new PrintWriter(
			new FileWriter(documentRoot+"/"+name));
		
		String line = r.readLine();
		while (line != null) {
			out.println(line);
			line = r.readLine();
		}
		
		out.close();
	}   	  	    
}
