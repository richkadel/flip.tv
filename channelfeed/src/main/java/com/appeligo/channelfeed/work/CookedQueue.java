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

package com.appeligo.channelfeed.work;

import java.net.MalformedURLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.HessianRuntimeException;
import com.knowbout.cc2nlp.CCEventService;
import com.knowbout.cc2nlp.CCSentenceEvent;
import com.knowbout.cc2nlp.CCXDSEvent;
import com.knowbout.cc2nlp.CaptionTypeChangeEvent;
import com.knowbout.cc2nlp.ITVLinkEvent;
import com.knowbout.cc2nlp.ProgramStartEvent;

public class CookedQueue extends Thread {
	
	private static final Logger log = Logger.getLogger(CookedQueue.class);

	private CCEventService destination;
	private String destinationURL;
	private BlockingQueue<Object> sendQueue;
    private boolean aborted;
    private boolean printedConnectError = false;

	CookedQueue(String prefix, String destinationURL) throws MalformedURLException {
		super("CookedQueue "+prefix+" to "+destinationURL);
		this.destinationURL = destinationURL;
		sendQueue = new LinkedBlockingQueue<Object>(1000);
		// If we have 1000 objects, something else is seriously wrong,
		// but at least we won't kill the memory
        HessianProxyFactory factory = new HessianProxyFactory();
        destination = (CCEventService)factory.create(CCEventService.class , destinationURL);
        if (destination == null) {
        	log.error("Destination "+destinationURL+" is not available.");
        }
	}
	
	boolean offer(Object object) {
		try {
    		return sendQueue.offer(object);
		} catch (NullPointerException e) {
			return false;
		}
	}

	@Override
	public void run() {
		
		try {
    		while (!aborted) {
    			Object toSend = null;
    			while (toSend == null) {
    				try {
    					toSend = sendQueue.take();
    				} catch (InterruptedException e1) {
    				}
    			}
    				
    	        String status = null;
    	        int retries = 10;
    	        for (int i = 0; i < retries; i++) {
    	        	try {
    					if (toSend instanceof CCSentenceEvent) {
    			            status = destination.captureSentence((CCSentenceEvent)toSend);
    					} else if (toSend instanceof CCXDSEvent) {
    			            status = destination.captureXDS((CCXDSEvent)toSend);
    		            } else if (toSend instanceof CaptionTypeChangeEvent) {
    			            status = destination.captionTypeChanged((CaptionTypeChangeEvent)toSend);
    		            } else if (toSend instanceof ITVLinkEvent) {
    			            status = destination.captureITVLink((ITVLinkEvent)toSend);
    		            } else if (toSend instanceof ProgramStartEvent) {
    				        status = destination.startProgram((ProgramStartEvent)toSend);
    					} else {
    						status = "NOT USED";
    					}
    		            break;
    	        	} catch (Throwable t) {
    	        		if (!printedConnectError) {
    	        			log.error("Exception sending a "+toSend.getClass(), t);
    	        		}
    	        		try {
    	        			/* I don't know who put in this break, but if the hessian service connection really isn't working
    	        			 * then this floods the logs.  There is no delay, no retry for 100 times, nothing.
    	        			 * So I'm commenting it out.  -- Rich
    						if (toSend instanceof CCSentenceEvent) {
    							break;
    						}
    						*/
    	        			Thread.sleep(1000);
    	        		} catch (InterruptedException e2) {
    	        			// ignore
    	        		}
    	        	}
    	        }
    	        if (status == null) {
    	        	if (!printedConnectError) {
        	        	log.error("Couldn't connect to Hessian service at "+destinationURL+" after "+retries+
        	        			" tries... Dropping events, but still trying.");
        	        	printedConnectError = true;
    	        	}
    	        	sendQueue.clear();
    	        } else {
    	        	if (printedConnectError) {
        	        	log.error("Finally connected to Hessian service at "+destinationURL);
        	        	printedConnectError = false;
    	        	}
    	            log.debug(status+" sending "+toSend.getClass());
    	        }
    		}
    	} catch (Throwable t) {
            log.fatal("Unexpected exception in CookedQueue, and we can't continue to send events to this destination.", t);
			sendQueue = null;
    	}
	}
	
	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}
}


