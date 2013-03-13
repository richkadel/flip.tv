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

package com.knowbout.epg.entities;

import java.io.Serializable;


public class NetworkScheduleId implements Serializable{
	
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -2500304908172891724L;
	private Long scheduleId;
    private String networkLineupId;
    
    public NetworkScheduleId(){}

    public NetworkScheduleId(Long scheduleId, String contentProviderId){
        this.scheduleId = scheduleId;
        this.networkLineupId = contentProviderId;
    }

    /**
	 * @return Returns the scheduleId.
	 */
    public Long getScheduleId(){
        return this.scheduleId;
    }
    /**
	 * @param scheduleId
	 *            The scheduleId to set.
	 */
    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }
    /**
	 * @return Returns the networkLineupId.
	 */
    public String getNetworkLineupId() {
        return this.networkLineupId;
    }
    /**
	 * @param networkLineupId
	 *            The networkLineupId to set.
	 */
    public void setNetworkLineupId(String contentProviderId) {
        this.networkLineupId = contentProviderId;
    }
    
    public boolean equals(Object o) {
        if (o instanceof NetworkScheduleId){
        	NetworkScheduleId that = (NetworkScheduleId) o;
            return this.scheduleId.equals(that.scheduleId) && this.networkLineupId.equals(that.networkLineupId);
        } else {
            return false;
        }
    }
    public int hashCode() {
        return scheduleId.hashCode() + networkLineupId.hashCode();
    }
}

