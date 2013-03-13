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

package com.appeligo.search.messenger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

/**
 * Uses the servlet initializer methods simply in order to get bootstrapped and start a thread
 * for sending out email messages.
 * 
 * @author fear
 */
@SuppressWarnings("serial")
public class MessengerService extends HttpServlet {

	private static final Logger log = Logger.getLogger(MessengerService.class);
	
	private static final Object LOCK = new Object();
	
	private SenderThread sender;
	
	/**
	 * 
	 */
	@Override
	public void destroy() {
		super.destroy();
		if (log.isInfoEnabled()) {
			log.info("Shutting down the MessengerService.");
		}
		try {
		if (sender != null) {
			sender.setContinuing(false);
			synchronized(LOCK) {
				LOCK.notify();
			}
		}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 
	 */
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		if (log.isInfoEnabled()) {
			log.info("Initialing MessengerService class in Servlet.init method.");
		}
		sender = new SenderThread();
		sender.setName("Mail-Sender-Servlet");
		sender.setDaemon(true);
		sender.start();
	}

	/**
	 * 
	 * @author fear
	 */
	private static final class SenderThread extends Thread {
		
		private boolean continuing = true;
		
		/**
		 * Loop to send messages.
		 */
		public void run() {
			
			// Check for messages added to the DB, and wait in between.
			while (isContinuing()) {
				try {
					if (log.isDebugEnabled()) {
						log.debug("Sending unsent messages.");
					}
					Messenger messenger = new Messenger();
					messenger.sendUnsent();
				} catch (Throwable t) {
					log.error(t.getMessage(), t);
				}
				synchronized(LOCK) {
					try {
						LOCK.wait(10 * 1000);
					} catch (InterruptedException ie) {
						
					}
				}
			}
		}

		public boolean isContinuing() {
			return continuing;
		}

		public void setContinuing(boolean continuing) {
			this.continuing = continuing;
		}
	}
}
