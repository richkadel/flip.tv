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

import java.util.Date;
import java.util.List;

/**
 * The <code>KeywordListener</code> interface is used to listen for keywords that are coming in
 * from a program stream.
 * @author almilli
 */
public interface KeywordListener {
    /**
     * An event telling the listener that a list of keywords have been found.  It will give the
     * program that the keywords were found on and the time span of when the keywords are valid for.
     * @param program the program the keywords were found on
     * @param keywords the list of keywords that were found
     * @param startTime the start time of the time span when the keywords are valid for
     * @param endTime the end time of the time span when the keywords are valid for
     */
	public void keywordsFound(KeywordEvent event);
}
