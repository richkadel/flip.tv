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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConfigurationService {
    private static final Log log = LogFactory.getLog(ConfigurationService.class);
    
    private static File rootDir = new File("config");
    
    // This is the default directory name searched under WEB-INF/config for default 
    // properties.
    private static String baseName = "base";
    private static String envName = "local";
    private static boolean initialized;
    private static Map<String, AbstractConfiguration> configMap;
    
    private ConfigurationService() {}
    
    public static synchronized void init() {
        if (!initialized) {
            configMap = new HashMap<String, AbstractConfiguration>();
            initialized = true;
        }
        
    }
    
    private synchronized static AbstractConfiguration loadConfiguration(String configuration) {
        if (log.isInfoEnabled()) {
            log.info("Loading AbstractConfiguration under: " + rootDir);
        }
        AbstractConfiguration ac = configMap.get(configuration);
        if (ac != null) {
        	return ac;
        }
        
        File envDir = new File(rootDir, envName);
        File baseDir = new File(rootDir, baseName);
        
        String[] environmentList = envDir.list();
        String envConfiguration = configuration;
        if (contains(environmentList, configuration + ".xml")) {
        	envConfiguration += ".xml";
        } else {
        	envConfiguration += ".properties";
        }
        String[] baseList = baseDir.list();
        String baseConfiguration = configuration;
        if (contains(baseList, configuration + ".xml")) {
        	baseConfiguration += ".xml";
        } else {
        	baseConfiguration += ".properties";
        }
        
        File envFile = new File(rootDir, envName + "/" + envConfiguration);
        File baseFile = new File(rootDir, baseName + "/" + baseConfiguration);
        ac =  createConfig(envFile, baseFile);
        configMap.put(configuration, ac);
        return ac;
    }
    
    private static boolean contains(String[] list, String instance) {
    	for (String test : list) {
    		if (instance.equals(test)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private static AbstractConfiguration createConfig(File envFile, File baseFile) {
    	if (log.isInfoEnabled()) {
    		log.info("Initiating configuration " + baseFile + " : " + envFile);
    	}
        AbstractConfiguration envConfig = null;
        AbstractConfiguration baseConfig = null;
        if (envFile.isFile()) {
            try {
                if (envFile.getName().endsWith(".xml")) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating xml config: " + envFile);
                    }
                    envConfig = new XMLConfiguration();
                    ((XMLConfiguration)envConfig).setValidating(false);
                    ((XMLConfiguration)envConfig).load(envFile);
                    
                } else if (envFile.getName().endsWith(".properties")) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating properties config: " + envFile);
                    }
                    envConfig = new PropertiesConfiguration(envFile);
                }
            } catch (ConfigurationException e) {
                if (log.isErrorEnabled()) {
                    log.error("Cannot create AbstractConfiguration for: " + envFile, e);
                }
            }
        }
        if (baseFile.isFile()) {
            try {
                if (baseFile.getName().endsWith(".xml")) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating xml config: " + baseFile);
                    }
                    baseConfig = new XMLConfiguration();
                    ((XMLConfiguration)baseConfig).setValidating(false);
                    ((XMLConfiguration)baseConfig).load(baseFile);
                    
                } else if (baseFile.getName().endsWith(".properties")) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating properties config: " + baseFile);
                    }
                    baseConfig = new PropertiesConfiguration(baseFile);
                }
            } catch (ConfigurationException e) {
                if (log.isErrorEnabled()) {
                    log.error("Cannot create AbstractConfiguration for: " + baseFile, e);
                }
            }
        }
        
        if (envConfig != null && baseConfig != null) {
            //create a combined AbstractConfiguration
            if (log.isDebugEnabled()) {
                log.debug("Creating combined config: " + envFile + " -> " + baseFile);
            }
            CombinedConfiguration combined = new CombinedConfiguration();
            combined.addConfiguration(envConfig);
            combined.addConfiguration(baseConfig);
            return combined;
            
        } else if (envConfig != null) {
            return envConfig;
            
        } else {
            return baseConfig;
        }
    }
    
    public static AbstractConfiguration getConfiguration(String name) {
        if (name == null || name.length() == 0) {
            return null;
        }
        if (name.charAt(0) == '/') {
            name = name.substring(1);
        }
        AbstractConfiguration configuration =  configMap.get(name);
        if (configuration == null) {
        	configuration = loadConfiguration(name);
        }
        return configuration;
    }

    public static String getEnvName() {
        return envName;
    }

    public static void setEnvName(String envName) {
        ConfigurationService.envName = envName;
    }

    public static File getRootDir() {
        return rootDir;
    }

    public static void setRootDir(File rootDir) {
        ConfigurationService.rootDir = rootDir;
    }

    public static String getBaseName() {
        return baseName;
    }

    public static void setBaseName(String baseName) {
        ConfigurationService.baseName = baseName;
    }
    
    public static void main(String[] args) {
        ConfigurationService.setRootDir(new File("src/main/test/config"));
        ConfigurationService.init();
        
        AbstractConfiguration config = getConfiguration("system");
        System.out.println("testPath=" + config.getString("testPath"));
        System.out.println("testValue=" + config.getString("testValue"));
    }
}
