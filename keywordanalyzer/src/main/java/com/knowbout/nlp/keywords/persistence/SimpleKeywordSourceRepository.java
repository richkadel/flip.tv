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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.knowbout.epg.service.ScheduledProgram;
import com.knowbout.keywords.listener.Program;

/**
 * A simple in memory representation of the repository that is suitable for doing 
 * NLP in most cases.  No text, input or output, is written permanently to anywhere
 * but the logging files when using this repository.
 * 
 * @author fear
 */
public class SimpleKeywordSourceRepository implements KeywordSourceRepository {

	private Map<String, KeywordContext> repository = new HashMap<String, KeywordContext>();

	private static final Logger log = Logger.getLogger(SimpleKeywordSourceRepository.class);
	
	/**
	 * 
	 */
	SimpleKeywordSourceRepository() {
		
	}
	

	
	public ScheduledProgram getCurrentProgram(String channel) {
		KeywordContext keywordContext = repository.get(channel);
		if (keywordContext != null) {
			return keywordContext.getScheduledProgram();
		}
		return null;
	}

	public void setCurrentProgram(String channel, ScheduledProgram program) {
		KeywordContext keywordContext = repository.get(channel);
		if (keywordContext != null) {
			keywordContext.setScheduledProgram(program);
		} else {
			keywordContext = new KeywordContext();
			keywordContext.setScheduledProgram(program);
			repository.put(channel, keywordContext);
		}
	}
	
	/**
	 * 
	 */
	public void newElement(String channel, String text) throws PersistenceException {
		if (text == null || text.length() == 0) return;
		KeywordContext keywordContext = null;
		synchronized(repository) {
		keywordContext = repository.get(channel);
			if (keywordContext == null) {
				keywordContext = new KeywordContext();
				repository.put(channel, keywordContext);
			}
		}
		keywordContext.addText(text);
		if (log.isDebugEnabled()) {
			log.debug(channel + '=' + repository.get(channel));
		}
	}
	
	/**
	 * 
	 */
	public void delete(String channel) throws PersistenceException {
		repository.remove(channel);
	}

	/**
	 * 
	 */
	public String getProgramText(String channel) throws PersistenceException {
		return repository.get(channel).getText();
	}

	/**
	 * 
	 */
	public String getProgramText(String channel, int maxTokens) throws PersistenceException {
		// If we don't have tokens, warn and return ""
		KeywordContext keywordContext = repository.get(channel);
		if (keywordContext == null) {
			log.warn("Null token set for channel " + channel);
			return "";
		}
		return keywordContext.getText(maxTokens);
	}

	/**
	 * 
	 */
	public void update(String channel, String fullProgramText) throws PersistenceException {
		this.newElement(channel, fullProgramText);
	}

}
