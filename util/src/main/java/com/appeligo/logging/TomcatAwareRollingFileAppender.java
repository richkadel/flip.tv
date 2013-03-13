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

package com.appeligo.logging;

import java.io.IOException;

import org.apache.log4j.Layout;
import org.apache.log4j.RollingFileAppender;

/**
 * A simple extensionof the RolingFileAppender that takes advantage of details
 * that can be found in in the tomcat environment.  This only has any affect 
 * when a relative path is specified for the log file, which causes it to 
 * be prepended with the CATALINA_HOME environment variable.
 * @author fear
 *
 */
public class TomcatAwareRollingFileAppender extends RollingFileAppender {

	public TomcatAwareRollingFileAppender() {
	}

	/**
	 * 
	 * @param layout
	 * @param filename
	 * @throws IOException
	 */
	public TomcatAwareRollingFileAppender(Layout layout, String filename)
			throws IOException {
		super(layout, filename);
	}

	/**
	 * 
	 * @param layout
	 * @param filename
	 * @param append
	 * @throws IOException
	 */
	public TomcatAwareRollingFileAppender(Layout layout, String filename, boolean append)
			throws IOException {
		super(layout, filename, append);
	}

	/**
	 * Uses environment variables expected by to be present in a Tomcat environment to create
	 * a fully qualified path.
	 */
	@Override
	public void setFile(String filename) {
		if (isRelativePath(filename)) {
			String catalinaBase = System.getProperty("catalina.base");
			if (catalinaBase != null) {
				System.out.println("Found catalina.base of " + catalinaBase);
			} else {
				catalinaBase = System.getProperty("catalina.home");
				if (catalinaBase != null) {
					System.out.println("Found catalina.home of " + catalinaBase);
				}
			}
			if (catalinaBase != null) {
				StringBuilder logPath = new StringBuilder(catalinaBase);
				if (!logPath.toString().endsWith("/")) {
					logPath.append("/").append(filename);
				}
				filename = logPath.toString();
				System.out.println("Creating log file " + logPath);
			}
		}
		super.setFile(filename);
	}
	
	/**
	 * Trur if the file does not contain ':' and does not start with '/'
	 * @param filename
	 * @return
	 */
	private boolean isRelativePath(String filename) {
		if (filename != null && filename.indexOf(':') < 0 && !filename.startsWith("/")) {
			return true;
		} else {
			return false;
		}
	}

}
