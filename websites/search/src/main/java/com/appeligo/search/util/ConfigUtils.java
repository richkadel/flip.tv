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

package com.appeligo.search.util;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;

import com.appeligo.config.ConfigurationService;

public class ConfigUtils {
	
	private ConfigUtils() {
    }

    private static AbstractConfiguration messageConfiguration;

    static {
    	CombinedConfiguration combined = new CombinedConfiguration();
    	combined.addConfiguration(getSystemConfig());
        combined.addConfiguration(ConfigurationService.getConfiguration("messages"));
    	messageConfiguration = combined;
    }

    public static AbstractConfiguration getSystemConfig() {
        return ConfigurationService.getConfiguration("system");
    }

    public static AbstractConfiguration getAmazonConfig() {
        return ConfigurationService.getConfiguration("amazon");
    }

    public static AbstractConfiguration getMessageConfig() {
	    return messageConfiguration;
    }
}
