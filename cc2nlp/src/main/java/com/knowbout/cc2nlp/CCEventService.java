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

package com.knowbout.cc2nlp;

import com.knowbout.cc4j.CaptionType;
import com.knowbout.cc4j.ITVLink;
import com.knowbout.epg.service.ScheduledProgram;

/**
 * 
 * @author fear
 *
 */
public interface CCEventService {

	public static final String SUCCESS = "SUCCESS";

	public static final String FAILURE = "FAILURE";
	
	/**
	 * 
	 * @param programId
	 * @param text
	 */
	public abstract String captureSentence(CCSentenceEvent event);

	public abstract String captureXDS(CCXDSEvent xdsEvent);
	
	public abstract String captureITVLink(ITVLinkEvent itvLinkEvent);

	public abstract String captionTypeChanged(CaptionTypeChangeEvent captionTypeChangedEvent);
	
	public abstract String startProgram(ProgramStartEvent programStartEvent);

}