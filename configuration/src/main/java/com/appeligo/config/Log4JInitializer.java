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

package com.appeligo.config;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * 
 * @author fear
 */
public class Log4JInitializer implements ServletContextListener {

	/**
	 * 
	 */
	public void contextDestroyed(ServletContextEvent event) {
		
	}

	/**
	 * 
	 */
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
        String rootPath = context.getInitParameter("ConfigurationRootDir");
        if (rootPath == null) {
            rootPath = "WEB-INF/config";
        }
        File rootDir = new File(context.getRealPath(rootPath));
        
        //configure the environment
        String envName = new EnvironmentLookup().getEnvironmentName();

        if (envName == null) {
            //look it up in the web.xml
            envName = context.getInitParameter("ConfigurationEnvName");
        }
        
        // Now we need to look for a log4j.xml file (preferred) and then log4j.properties
        // if we don't find that file where we expect it.
        String log4jConfigFilename = rootDir.getAbsolutePath() + 
        	"/" + envName + "/log4j.xml";
        boolean configed = configFromFile(context, log4jConfigFilename);
        if (!configed) {
            // We only need the basename if we can't find a file for our current evironment.
            String baseName = context.getInitParameter("ConfigurationBaseName");
            if (baseName == null) {
            	baseName = "base";
            }
            log4jConfigFilename = rootDir.getAbsolutePath() + 
        		"/" + baseName + "/log4j.xml";
            configed = configFromFile(context, log4jConfigFilename);
            if (!configed) {
            	context.log("Unabled to configure log4j, default log4j techniques still apply.");
            }
        }
		
	}

	private boolean configFromFile(ServletContext context, String filename) {
		System.out.println("Checking for log configuration at " + filename + "/properties");
		File configFile = new File(filename);
        if (configFile.exists() && configFile.isFile()) {
        	context.log("Initialing log4j from " + configFile.getAbsolutePath());
        	DOMConfigurator.configureAndWatch(filename, 60000);
        	Logger.getLogger(this.getClass()).info("Configured log4j from resources " + filename);
        	return true;
        } else {
        	context.log("Could not find " + filename + " trying .properties.");
        	filename = filename.replace(".xml", ".properties");
        	configFile = new File(filename);
        	if (configFile.exists() && configFile.isFile()) {
        		PropertyConfigurator.configureAndWatch(filename, 60000);
            	Logger.getLogger(this.getClass()).info("Configured log4j from resources " + filename);
        		return true;
        	}
        }
		return false;
	}
}
