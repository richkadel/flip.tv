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

package com.knowbout.cc4j;

import java.io.IOException;

import com.knowbout.cc4j.FrequencyStandard;
import com.knowbout.cc4j.TVCapturer;
import com.knowbout.cc4j.VideoDevice;

import junit.framework.TestCase;

public class TVCapturerTest extends TestCase {

	public void testTVCapturer() {
		try {
			if (System.getProperty("device.test.ok") != null) {
				final TVCapturer cap = new TVCapturer(new VideoDevice(0, FrequencyStandard.US_CABLE));
				new Thread() {
					@Override
					public void run() {
						try {
							cap.captureTo("test.flv");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}.start();
				try {
					Thread.sleep(15*1000);
				} catch (InterruptedException e) {
				}
				cap.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
