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

package com.knowbout.nlp.keywords.persistence;

import java.util.List;

import com.knowbout.epg.service.ScheduledProgram;
import com.knowbout.keywords.listener.Program;

/**
 *  This class provides a simple contract to abstract away the details of persistence from 
 *  the core service of finding keywords in blocks of text.
 *  
 * @author fear
 */
public interface KeywordSourceRepository {

	/**
	 * 
	 * @param channel
	 * @param program
	 */
	public void setCurrentProgram(String channel, ScheduledProgram program);
	
	/**
	 * 
	 * @param channel
	 * @return
	 */
	public ScheduledProgram getCurrentProgram(String channel);
	
	/**
	 * 
	 */
	public void newElement(String channel, String text) throws PersistenceException;
	
	/**
	 * 
	 * @param programId
	 * @throws PersistenceException
	 */
	public void delete(String channel) throws PersistenceException;
	
	/**
	 * 
	 * @param programId
	 * @param fullProgramText
	 * @throws PersistenceException
	 */
	public void update(String channel, String fullProgramText) throws PersistenceException;
	
	/**
	 * 
	 * @param count
	 * @return
	 * @throws PersistenceException
	 */
	public String getProgramText(String channel, int count) throws PersistenceException;
	
	/**
	 * 
	 * @param programid
	 * @return
	 * @throws PersistenceException
	 */
	public String getProgramText(String channel) throws PersistenceException;
}
