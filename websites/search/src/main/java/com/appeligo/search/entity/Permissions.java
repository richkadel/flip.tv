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

package com.appeligo.search.entity;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Permissions {
	
	private static final Log log = LogFactory.getLog(Permissions.class);

    public static final ThreadLocal<User> currentUser = new ThreadLocal<User>();
    
	public static final User SUPERUSER = new User();
	
	static {
		try {
			SUPERUSER.setUserId(-1);
			SUPERUSER.setUsername("<superuser>");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public static void setCurrentUser(User user) {
		if (user != null && currentUser.get() != null) {
			log.warn("Changing the user from "+currentUser.get()+" to "+user+
					". If you really want to do this, set the user to null first.\n"+
					Arrays.toString(Thread.currentThread().getStackTrace()).replaceAll(", ","\n"));
		}
		currentUser.set(user);
	}
	
	public static User getCurrentUser() {
		return currentUser.get();
	}
	
	public static void checkUser(User user) {
		if ((getCurrentUser() != SUPERUSER) &&
			(getCurrentUser() != null) &&
			(!getCurrentUser().equals(user))) {
			throw new SecurityException("Invalid user. User id "+user.getUserId()+" does not match current user: "+getCurrentUser());
		}
	}
}
