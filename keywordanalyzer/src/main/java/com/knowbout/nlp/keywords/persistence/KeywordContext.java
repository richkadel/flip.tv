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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.knowbout.epg.service.ScheduledProgram;
import com.knowbout.nlp.keywords.util.Config;
import com.knowbout.nlp.keywords.util.TextUtil;

/**
 * This provides a holder and logic to deal with text coming in from an external source
 * and pack it into a meaning full context so that it can easily be treated as a unit.
 * This class is private to the persistence package, and should not be exposed externally
 * under any circumstances.
 * 
 * @author fear
 */
class KeywordContext implements Serializable {

	private static final Logger log = Logger.getLogger(KeywordContext.class);
	
	private static final long serialVersionUID = 1034583290420322661L;
	
	private boolean mixedCase;
	
	private int maximumTokens;
	
	private LinkedList<String> tokens;
	
	private ScheduledProgram scheduledProgram;
	
	/**
	 * 
	 *
	 */
	KeywordContext() {
		mixedCase = false;
		maximumTokens = Config.getConfiguration().getInt("maximumContextTokens", 1000);
		tokens = new LinkedList<String>();
	}
	
	/**
	 * 
	 * @param text
	 */
	void addText(String text) {
		if (log.isDebugEnabled()) {
			log.debug("Appending " + text + " to " + this);
		}
		
		// If the given text is not mixed case, send it all to lower case.
		mixedCase = TextUtil.isMixedCase(text);
		if (!mixedCase) {
			text = text.toLowerCase();
		}
		
		List<String> newTokens = TextUtil.tokenize(text);
		if (tokens.size() + newTokens.size() < maximumTokens) {
			tokens.addAll(newTokens);
		} else {
			int oldTokensRetained = maximumTokens - newTokens.size();
			int fromIndex = tokens.size() - oldTokensRetained;
			List<String> retained = tokens.subList(fromIndex, tokens.size());
			LinkedList<String> temp = new LinkedList<String>(retained);
			temp.addAll(newTokens);
			synchronized(this) {
				tokens = temp;
			}
		}
	}
	
	public String toString() {
		return getText();
	}
	
	/**
	 * 
	 * @return
	 */
	String getText() {
		return TextUtil.concat(tokens);
	}
	
	String getText(int maxTokens) {
		if (tokens.size() <= maxTokens || maxTokens < 1) {
			return TextUtil.concat(tokens);
		} else {
			return TextUtil.concat(tokens.subList(tokens.size() - maxTokens, tokens.size()));
		}
	}
	
	boolean isMixedCase() {
		return mixedCase;
	}

	public ScheduledProgram getScheduledProgram() {
		return scheduledProgram;
	}

	public void setScheduledProgram(ScheduledProgram scheduledProgram) {
		this.scheduledProgram = scheduledProgram;
	}
}
