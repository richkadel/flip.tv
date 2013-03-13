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
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides basic initialization of the configuration subsystem for this application.
 * 
 */
public class ConfigurationInitializer implements ServletContextListener {
	
    private static final Log log = LogFactory.getLog(ConfigurationInitializer.class);

    /**
     * Expected to initialize n servlet initializaton.
     */
    public void contextInitialized(ServletContextEvent event) {

        ServletContext context = event.getServletContext();
        String rootPath = context.getInitParameter("ConfigurationRootDir");
        if (rootPath == null) {
            rootPath = "WEB-INF/config";
        }
        File rootDir = new File(context.getRealPath(rootPath));
        ConfigurationService.setRootDir(rootDir);

        String baseName = context.getInitParameter("ConfigurationBaseName");
        if (baseName != null) {
            ConfigurationService.setBaseName(baseName);
        }
        
        //configure the environment
        String envName = new EnvironmentLookup().getEnvironmentName();

        if (envName == null) {
            //look it up in the web.xml
            envName = context.getInitParameter("ConfigurationEnvName");
        }
        
        if (envName != null) {
            ConfigurationService.setEnvName(envName);
        } else {
            log.warn("Cannot find envName from \"deploymentEnvironment\" JNDI variable or \"ConfigurationEnvName\" init param in web.xml. Using default name...");
        }
        
        ConfigurationService.init();
    }

    public void contextDestroyed(ServletContextEvent event) {
    }

}
