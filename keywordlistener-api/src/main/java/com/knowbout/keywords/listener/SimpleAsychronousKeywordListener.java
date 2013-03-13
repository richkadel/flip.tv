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

package com.knowbout.keywords.listener;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a seriously simple minded class for doing "oneway" invocations of
 * a keyword listener object.
 * @author fear
 *
 */
public class SimpleAsychronousKeywordListener implements KeywordListener {
    
	private static final Log log = LogFactory.getLog(SimpleAsychronousKeywordListener.class);
	
	private KeywordListener delegate;
	
	private List<KeywordEvent> queue = Collections.synchronizedList(new LinkedList<KeywordEvent>());
	
	private Sender sender;
	
	/**
	 * Wraps this listener object around some implementation to provide asynchronous behavior.
	 * @param delegate
	 */
	public SimpleAsychronousKeywordListener(KeywordListener delegate) {
		this.delegate = delegate;
		sender = new Sender();
		sender.start();
	}
	
	/**
	 * 
	 */
	public void keywordsFound(KeywordEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("Adding " + event + " to queue.");
		}
		synchronized(queue) {
			queue.add(event);
			queue.notify();
		}
	}

	private class Sender extends Thread {
		@Override
		public void run() {
			while(true) {
				try {
					KeywordEvent event = null;
					synchronized(queue) {
						if (queue.size() > 0) {
							event = queue.remove(0);
						}
					}
					if (event != null) {
						if (log.isDebugEnabled()) {
							log.debug("Sending " + event + " to search API.");
						}
						delegate.keywordsFound(event);
					}
					log.debug("Waiting on queue for 500 millis.");
					synchronized(queue) {
						queue.wait(500);
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Simple struct to pack data away in a waiting queue of requests.
	 * @author fear
	 *
	 */
	private class Params {
		Params(Program p, List<Keyword> k, Date s, Date e) {
			program = p;
			keywords = k;
			startDate = s;
			endDate = e;
		}
		private Program program;
		private List<Keyword> keywords;
		private Date startDate;
		private Date endDate;
	}
}
