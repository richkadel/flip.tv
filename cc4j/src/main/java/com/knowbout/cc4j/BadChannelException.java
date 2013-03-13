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

/**
 * Thrown when a channel is requested that is not found in the frequency
 * table for the current FrequencyStandard.
 * @author Rich Kadel
 * @author $Author: kadel $
 * @version $Rev: 75 $ $Date: 2006-07-22 01:42:31 -0700 (Sat, 22 Jul 2006) $
 */
public class BadChannelException extends Exception {

	private static final long serialVersionUID = -5839486451312849023L;
	
	/**
	 * @param message a detail message
	 */
	public BadChannelException(String message) {
		super(message);
	}
}
