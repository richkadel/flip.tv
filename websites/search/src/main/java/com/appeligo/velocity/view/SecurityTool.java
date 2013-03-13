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

package com.appeligo.velocity.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ViewTool;

/**
 * Provides support to the view layer to do better security.
 * @author fear
 */
public class SecurityTool implements ViewTool {
	
    private HttpServletRequest request;
    private HttpServletResponse response;


	public SecurityTool() {
		
	}

    /**
     * Initializes this instance for the current request.
     *
     * @param obj the ViewContext of the current request
     */
    public void init(Object obj)
    {
        ViewContext context = (ViewContext)obj;
        this.request = context.getRequest();
        this.response = context.getResponse();
    }
    
	public boolean isSecure() {
		return request.getScheme() != null && request.getScheme().equalsIgnoreCase("https");
	}
}
