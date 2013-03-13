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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.knowbout.epg.PersistenceTestCase;
import com.knowbout.epg.entities.Community;
import com.knowbout.epg.entities.Headend;

public class EntityTest extends PersistenceTestCase {
    private static final Log log = LogFactory.getLog(EntityTest.class);
    
	public void testHeadend() throws Exception {

		Headend headend = new Headend();

		headend.setId("CA04542");
		headend.setDmaCode(825);
		headend.setDmaName("San Diego");
		headend.setMsoCode(8670);
		headend.setDmaRank(26);
		headend.setHeadendName("Time Warner Cable");
		headend.setHeadendLocation("San Diego");
		headend.setMsoName("Time Warner Cable");
		headend.setTimeZoneCode(4);
		
//		headend.insert();
//		commitTransaction();
		
		Headend found = Headend.selectById("CA04542");
		assertTrue("Retrieved Headed from database", found != null);
		
		Community community = new Community();
		
		community.setId("92014");
		community.setCountyName("San Diego");
		community.setCountyCode(5073);
		community.setCountySize("A");
		community.setName("Del Mar");
		community.setZipCode("92104");
		community.setState("CA");
		community.addHeadend(found);
//		community.insert();
//		commitTransaction();
		
		Headend communityTest = Headend.selectById("CA04542");
		assertTrue("Retrieved Headed from database", found != null);
		assertTrue("Headend has one community", communityTest.getCommunities().size() >= 1);
		assertTrue("It is the matching community", communityTest.getCommunities().iterator().next().getId().equals("92014"));				
	}
}
