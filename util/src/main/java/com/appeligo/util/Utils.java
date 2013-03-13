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

package com.appeligo.util;

import java.util.Calendar;
import java.util.TimeZone;

public class Utils {
	
	private static final String EMAIL_REGEX = "^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}$";

	private Utils() {
    }

	public static boolean isEmailAddress(String email) {
		if (email == null) {
			return false;
		} else {
			return email.matches(EMAIL_REGEX);
		}
	}
	
	public static String getDatePath(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(timestamp);
		return String.format("%1$tY/%1$tm/%1$td", cal);
	}
	
	public static void sleepForMillis(long millis) {
		sleepUntil(System.currentTimeMillis() + millis);
	}

	public static void sleepForSeconds(long seconds) {
		sleepForMillis(seconds*1000);
	}

	public static void sleepUntil(long timestamp) {
		long currentTime;
		while ((currentTime = System.currentTimeMillis()) < timestamp) {
			try {
				Thread.sleep(timestamp-currentTime);
			} catch (InterruptedException e) {
			}
		}
	}

}
