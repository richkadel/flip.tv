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

package com.appeligo.alerts.api;

import java.io.IOException;
import java.net.MalformedURLException;

import com.appeligo.alerts.api.AlertQueue;
import com.caucho.hessian.client.HessianProxyFactory;

import junit.framework.TestCase;

public class ProgramAlertsTest extends TestCase {
	
	public void testStore() throws IOException {
		HessianProxyFactory factory = new HessianProxyFactory();
		factory.setOverloadEnabled(true);
    	AlertQueue alerts = (AlertQueue)factory.create(AlertQueue.class,"http://localhost:8080/search/alertqueue");

    	alerts.checkAlerts();
	}
}
