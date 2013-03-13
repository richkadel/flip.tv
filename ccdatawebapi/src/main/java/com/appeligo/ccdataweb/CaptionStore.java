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

package com.appeligo.ccdataweb;

import java.io.IOException;

public interface CaptionStore {

	public abstract String getSentence(String lineupId, String callsign,
			long programStartTime, long timestamp) throws IOException;

	public abstract String getSentence(String headendId, String lineupDevice,
			String callsign, long programStartTime, long timestamp)
			throws IOException;

	public abstract String[] getSentences(String lineupId, String callsign,
			long programStartTime, long startTimestamp, long endTimestamp)
			throws IOException;

	public abstract String[] getSentences(String headendId,
			String lineupDevice, String callsign, long programStartTime,
			long startTimestamp, long endTimestamp) throws IOException;

}