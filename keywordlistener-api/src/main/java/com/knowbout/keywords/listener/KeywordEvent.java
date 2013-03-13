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

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * KeywordEvent encapsulates a requirest to a KeywordListener by aggregating
 * the components parts of such a request.  These events are typically 
 * delievered via a remote service.
 * 
 * @author fear
 */
public class KeywordEvent implements Serializable {

	private static final long serialVersionUID = 698174096101788163L;

	private long programStartTime;
	
	private long eventTimestamp;
	
	private Collection<Keyword> keywords;
	
	private String context;
	
	private Program program;
	
	private String supplemental;

	private String lineupID;

	private String channel;
	
	public KeywordEvent() {
		
	}
	
	/**
	 * This minimal set of parameters are the properties generally relied on
	 * by al consmers of these events.
	 * @param program
	 * @param keywords
	 * @param start
	 * @param end
	 */
	public KeywordEvent(Program program, Collection<Keyword> keywords,
			String lineupID, String channel, long programStartTime, long eventTimestamp) {
		this();
		this.program = program;
		this.keywords = keywords;
		this.lineupID = lineupID;
		this.channel = channel;
		this.programStartTime = programStartTime;
		this.eventTimestamp = eventTimestamp;
	}
	
	/**
	 * This more detailed consructor provides lots of information useful for
	 * diagnostic work.
	 * @param program
	 * @param keywords
	 * @param start
	 * @param end
	 * @param context
	 * @param supplemental
	 */
	public KeywordEvent(Program program, Collection<Keyword> keywords, String lineupID, String channel,
			long programStartTime,
			long eventTimestamp, String context, String supplemental) {
		this(program, keywords, lineupID, channel, programStartTime, eventTimestamp);
		this.context = context;
		this.supplemental = supplemental;
	}

	/**
	 * The context is he body of text used to find the keywords represented
	 * by this event.  This could be closed captioning text, email, etc. as
	 * the body.
	 * @return This value may be null depending on construction.
	 */
	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public Collection<Keyword> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<Keyword> keywords) {
		this.keywords = keywords;
	}

	public Program getProgram() {
		return program;
	}

	public void setProgram(Program program) {
		this.program = program;
	}

	public long getProgramStartTime() {
		return programStartTime;
	}

	public void setProgramStartTime(long programStartTime) {
		this.programStartTime = programStartTime;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("KeywordEvent: keywords=");
		sb.append(keywords);
		if (this.getProgram() != null) {
			sb.append("; channel=").append(this.getProgram().getChannel());
		}
		return sb.toString();
	}
	
	/**
	 * 
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((context == null) ? 0 : context.hashCode());
		result = PRIME * result + ((keywords == null) ? 0 : keywords.hashCode());
		result = PRIME * result + ((program == null) ? 0 : program.hashCode());
		result = (int) (PRIME * result + eventTimestamp);
		result = (int) (PRIME * result + programStartTime);
		return result;
	}

	/**
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final KeywordEvent other = (KeywordEvent) obj;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		if (lineupID == null) {
			if (other.lineupID != null)
				return false;
		} else if (!lineupID.equals(other.lineupID))
			return false;
		if (keywords == null) {
			if (other.keywords != null)
				return false;
		} else if (!keywords.equals(other.keywords))
			return false;
		if (program == null) {
			if (other.program != null)
				return false;
		} else if (!program.equals(other.program))
			return false;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (programStartTime != other.programStartTime) {
			return false;
		}
		if (eventTimestamp != other.eventTimestamp) {
			return false;
		}
		return true;
	}

	public String getSupplemental() {
		return supplemental;
	}

	public void setSupplemental(String supplemental) {
		this.supplemental = supplemental;
	}

	public String getLineupID() {
		return lineupID;
	}

	public void setLineupID(String lineupID) {
		this.lineupID = lineupID;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public void setEventTimestamp(long eventTimestamp) {
		this.eventTimestamp = eventTimestamp;
	}

	public long getEventTimestamp() {
		return eventTimestamp;
	}
	
	
}
