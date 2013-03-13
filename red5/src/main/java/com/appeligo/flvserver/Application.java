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

package com.appeligo.flvserver;

import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.stream.IServerStream;
import org.red5.server.api.stream.IStreamCapableConnection;
import org.red5.server.api.stream.support.SimpleBandwidthConfigure;

public class Application extends ApplicationAdapter {
	private IScope appScope;

	private IServerStream serverStream;

	@Override
	public boolean appStart(IScope app) {
		appScope = app;
		return true;
	}

	@Override
	public boolean appConnect(IConnection conn, Object[] params) {
		// Trigger calling of "onBWDone", required for some FLV players
		measureBandwidth(conn);
		if (conn instanceof IStreamCapableConnection) {
			IStreamCapableConnection streamConn = (IStreamCapableConnection) conn;
			SimpleBandwidthConfigure sbc = new SimpleBandwidthConfigure();
			sbc.setMaxBurst(8 * 1024 * 1024);
			sbc.setBurst(8 * 1024 * 1024);
			sbc.setOverallBandwidth(2 * 1024 * 1024);
			streamConn.setBandwidthConfigure(sbc);
		}
		/*
		 * if (appScope == conn.getScope()) { serverStream =
		 * StreamUtils.createServerStream(appScope, "live0"); SimplePlayItem
		 * item = new SimplePlayItem(); item.setStart(0); item.setLength(10000);
		 * item.setName("on2_flash8_w_audio"); serverStream.addItem(item); item =
		 * new SimplePlayItem(); item.setStart(20000); item.setLength(10000);
		 * item.setName("on2_flash8_w_audio"); serverStream.addItem(item);
		 * serverStream.start(); }
		 */
		return super.appConnect(conn, params);
	}

	@Override
	public void appDisconnect(IConnection conn) {
		if (appScope == conn.getScope() && serverStream != null) {
			serverStream.close();
		}
		super.appDisconnect(conn);
	}
}
