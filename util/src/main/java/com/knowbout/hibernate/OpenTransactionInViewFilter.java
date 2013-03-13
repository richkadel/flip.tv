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

package com.knowbout.hibernate;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The <code>OpenTransactionInViewFilter</code> class
 * @author almilli
 */
public class OpenTransactionInViewFilter implements Filter {
	private static final Log log = LogFactory.getLog(OpenTransactionInViewFilter.class);
	private boolean printFullExceptions;

	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		String printFullExceptionsString = config.getInitParameter("printFullExceptions");
		if (printFullExceptionsString != null && printFullExceptionsString.toLowerCase().equals("true")) {
			printFullExceptions = true;
		}
	}

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		TransactionManager.beginTransaction();
		boolean commit = false;
		try {
			chain.doFilter(request, response);
			commit = !TransactionManager.isRollbackOnly();
		} catch (ServletException se) {
			String message = se.toString();
			if (se.getRootCause() != null) {
				message = se.getRootCause().toString();
			}
			if (printFullExceptions) {
    			if (se.getRootCause() != null) {
    				log.error(message, se.getRootCause());
    			} else {
    				log.error(message, se);
    			}
			} else {
				log.error(message);
			}
			commit = false;
			throw se;
		} catch (Throwable t) {
			log.error("Error with transaction, rolling back instead.", t);
			commit = false;
		} finally {
			//dispose of the open transaction by commit or rollback
			//transactionHolder.remove(transaction);
			if (commit) {
				try {
					TransactionManager.commitTransaction();
				} catch (Throwable e) {
					log.error("Cannot commit transaction, rolling back instead.", e);
					TransactionManager.rollbackTransaction();
					throw new ServletException(e);
				}
			} else {
				try {
					TransactionManager.rollbackTransaction();
				} catch (Throwable e) {
					log.error("Cannot rollback transaction.", e);
					throw new ServletException(e);
				}
			}
		}
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	}
}
