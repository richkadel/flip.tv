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

package com.knowbout.web;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import java.io.*;

public class HessianLogger implements Filter {

	private static final Logger log = Logger.getLogger(HessianLogger.class);
	
	private FilterConfig filterConfig;

	/**
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		if (log.isDebugEnabled()) {
			HttpServletRequest request = (HttpServletRequest)req;
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				StringBuilder sb = new StringBuilder();
				for (Cookie c : cookies) {
					sb.append(c.getName()).append('=').append(c.getValue()).append(';');
				}
				log.debug(sb);
			} else {
				log.debug("Null cookie array.");
			}
			
		}
		// pass the request/response on
		try {
			chain.doFilter(req, res);
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
			throw new ServletException(t);
		}
	}

	public void init(FilterConfig filterConfig) {
		this.filterConfig = filterConfig;
	}

	public void destroy() {
		this.filterConfig = null;
	}
}
