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

/**
 * The <code>Program</code> class defines the television program and channel that keywords were
 * found on.
 * @author almilli
 */
public class Program implements Serializable {
	private static final long serialVersionUID = -4836501829606343314L;
	
	private String headendId;
	private String lineupDevice;
	private String channel;
	private String programId;
    
    /**
     * Creates a new program instance without setting any of the properties
     */
    public Program() {
    }
	
    /**
     * Creates a new program definition.
     * @param headendId the headend of the program
     * @param lineupDevice the lineup device for the program
     * @param channel the channel that the program was found on
     * @param programId the program identifier
     */
	public Program(String headendId, String lineupDevice, String channel, String programId) {
		this.headendId = headendId;
		this.lineupDevice = lineupDevice;
		this.channel = channel;
		this.programId = programId;
		if (this.headendId == null) this.headendId = "";
		if (this.lineupDevice == null) this.lineupDevice = "";
		if (this.channel == null) this.channel = "";
		if (this.programId == null) this.programId = "";
	}

    /**
     * Gets the channel the program was recorded on
     * @return the channel
     */
	public String getChannel() {
		return channel;
	}

    /**
     * Sets the channel that the program was recorded on
     * @param channel the channel for this program
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Gets the headend identifier
     * @return the headened identifier
     */
	public String getHeadendId() {
		return headendId;
	}

    /**
     * Sets the headend identifier
     * @param headendId the headened identifier
     */
    public void setHeadendId(String headendId) {
        this.headendId = headendId;
    }

    /**
     * Gets the lineup device for this program
     * @return the lineup device
     */
	public String getLineupDevice() {
		return lineupDevice;
	}

    /**
     * Sets the lineup device for this program
     * @param lineupDevice the lineup device
     */
    public void setLineupDevice(String lineupDevice) {
        this.lineupDevice = lineupDevice;
    }

    /**
     * Gets the program identifier
     * @return the program identifier
     */
	public String getProgramId() {
		return programId;
	}

    /**
     * Sets the program identifier
     * @param programId the program identifier
     */
    public void setProgramId(String programId) {
        this.programId = programId;
    }
    
    /**
     * Gets the hashcode for this program
     * @return the hash code for this program
     */
    public int hashCode() {
        return getHeadendId().hashCode() ^
            getLineupDevice().hashCode() ^
            getChannel().hashCode() ^
            getProgramId().hashCode();
    }
    
    /**
     * Checks equality of this program.
     * @return true if equals, false if not
     */
    public boolean equals(Object other) {
        if (other instanceof Program) {
            Program prog = (Program) other;
            return getHeadendId().equals(prog.getHeadendId())
                    && getLineupDevice().equals(prog.getLineupDevice())
                    && getChannel() == prog.getChannel()
                    && getProgramId().equals(prog.getProgramId());
        } else {
            return false;
        }
    }
    
    /**
     * Gets a string representation of this program
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Program ");
        sb.append("[headendId=").append(headendId);
        sb.append(",lineupDevice=").append(lineupDevice);
        sb.append(",channel=").append(channel);
        sb.append(",programId=").append(programId);
        sb.append("]");
        return sb.toString();
    }
}
