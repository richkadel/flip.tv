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

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Session;

import com.knowbout.hibernate.HibernateUtil;

public class Headend {

	private String id;
	
	    private int dmaCode;
	    private String dmaName;
	    private int msoCode;
	    private int dmaRank;
	    private String headendName;
	    private String headendLocation;
	    private String msoName;
	    private int timeZoneCode;
	    private Set<Lineup> lineups;
	    private Set<Community> communities;
	    
	    public Headend() {	    	
	    }
	    
		/**
		 * @return Returns the communities.
		 */
		public Set<Community> getCommunities() {
			if (communities == null) {
				communities = new HashSet<Community>();
			}
			return communities;
		}
		/**
		 * @param communities The communities to set.
		 */
		public void setCommunities(Set<Community> communities) {
			this.communities = communities;
		}
		/**
		 * @return Returns the dmaCode.
		 */
		public int getDmaCode() {
			return dmaCode;
		}
		/**
		 * @param dmaCode The dmaCode to set.
		 */
		public void setDmaCode(int dmaCode) {
			this.dmaCode = dmaCode;
		}
		/**
		 * @return Returns the dmaName.
		 */
		public String getDmaName() {
			return dmaName;
		}
		/**
		 * @param dmaName The dmaName to set.
		 */
		public void setDmaName(String dmaName) {
			this.dmaName = dmaName;
		}
		/**
		 * @return Returns the dmaRank.
		 */
		public int getDmaRank() {
			return dmaRank;
		}
		/**
		 * @param dmaRank The dmaRank to set.
		 */
		public void setDmaRank(int dmaRank) {
			this.dmaRank = dmaRank;
		}
		/**
		 * @return Returns the headendLocation.
		 */
		public String getHeadendLocation() {
			return headendLocation;
		}
		/**
		 * @param headendLocation The headendLocation to set.
		 */
		public void setHeadendLocation(String headendLocation) {
			this.headendLocation = headendLocation;
		}
		/**
		 * @return Returns the headendName.
		 */
		public String getHeadendName() {
			return headendName;
		}
		/**
		 * @param headendName The headendName to set.
		 */
		public void setHeadendName(String headendName) {
			this.headendName = headendName;
		}
		/**
		 * @return Returns the id.
		 */
		public String getId() {
			return id;
		}
		/**
		 * @param id The id to set.
		 */
		public void setId(String id) {
			this.id = id;
		}
		/**
		 * @return Returns the lineups.
		 */
		public Set<Lineup> getLineups() {
			if (lineups == null) {
				lineups = new HashSet<Lineup>();
			}
			return lineups;
		}
		/**
		 * @param lineups The lineups to set.
		 */
		public void setLineups(Set<Lineup> lineups) {
			this.lineups = lineups;
		}
		
		public void addLineup(Lineup lineup) {
			getLineups().add(lineup);
			lineup.setHeadend(this);
		}

		public void removeLineup(Lineup lineup) {
			getLineups().remove(lineup);
			lineup.setHeadend(null);
		}
		
		

		/**
		 * @return Returns the msoCode.
		 */
		public int getMsoCode() {
			return msoCode;
		}
		/**
		 * @param msoCode The msoCode to set.
		 */
		public void setMsoCode(int msoCode) {
			this.msoCode = msoCode;
		}
		/**
		 * @return Returns the msoName.
		 */
		public String getMsoName() {
			return msoName;
		}
		/**
		 * @param msoName The msoName to set.
		 */
		public void setMsoName(String msoName) {
			this.msoName = msoName;
		}
		/**
		 * @return Returns the timeZoneCode.
		 */
		public int getTimeZoneCode() {
			return timeZoneCode;
		}
		/**
		 * @param timeZoneCode The timeZoneCode to set.
		 */
		public void setTimeZoneCode(int timeZoneCode) {
			this.timeZoneCode = timeZoneCode;
		}
	    
	    
		public void insert() {
			Session session = HibernateUtil.currentSession();
			session.save(this);
		}
		
		public void delete() {
			Session session = HibernateUtil.currentSession();
			session.delete(this);
		}
		
		public static Headend selectById(String headendId) {
			Session session = HibernateUtil.currentSession();
			return (Headend)session.get(Headend.class, headendId);
		}
	    
}
