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

package com.knowbout.epg;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.XMLConfiguration;
import org.hibernate.cfg.Environment;

import com.knowbout.epg.entities.Schedule;
import com.knowbout.epg.service.ScheduledProgram;
import com.knowbout.epg.service.ServiceLineup;
import com.knowbout.epg.service.StationChannel;
import com.knowbout.hibernate.HibernateUtil;

public class EPGServiceTest extends PersistenceTestCase {

	
	public void setUp() throws Exception {
	
		XMLConfiguration config = new XMLConfiguration("sample.epg.xml");
		
		HashMap<String, String> hibernateProperties = new HashMap<String, String>();
		Configuration database = config.subset("database");
		hibernateProperties.put(Environment.DRIVER, database.getString("driver"));
		hibernateProperties.put(Environment.URL, database.getString("url"));
		hibernateProperties.put(Environment.USER, database.getString("user"));
		hibernateProperties.put(Environment.PASS, database.getString("password"));
		hibernateProperties.put(Environment.DATASOURCE, null);
		
		HibernateUtil.setProperties(hibernateProperties);
		super.setUp();
	}
	
	public void testSchedules() {
		String lineupId = "CA04542:R";
		String programId = "SH0000010000";
		Calendar cal = Calendar.getInstance();
		Date now = new Date();
		cal.add(Calendar.DAY_OF_MONTH, -14);		
		Date past = cal.getTime();
		cal.setTime(now);
		cal.add(Calendar.DAY_OF_MONTH, 14);
		Date future = cal.getTime();
		EPGProviderService service = new EPGProviderService();		
		List<ScheduledProgram> progs = service.getScheduleForProgram(lineupId, programId, past, future);
		for (ScheduledProgram prog: progs) {
			System.err.println("prog:" + prog.getEpisodeTitle() + ", " + prog.getStartTime() +" linup" +prog.getChannel().getChannel() + " title:" + prog.getProgramTitle());
		}
		ScheduledProgram next = service.getNextShowing(lineupId, programId);
		System.err.println("Next:" + next.getEpisodeTitle() + ", " + next.getStartTime());
		assertNotNull(next);
		ScheduledProgram last = service.getLastShowing(lineupId, programId);
		System.err.println("last:" + last.getEpisodeTitle() + ", " + last.getStartTime());
		assertNotNull(last);
	}
	
	public void testDelete() {
		int previousCount = Schedule.deleteAfter(new Date());
		System.err.println("deleted: "+ previousCount);
	}
	/*
	 * Test method for 'com.knowbout.epg.EPGProviderService.getChannels(String)'
	 */
	public void testGetChannels() {
//		System.err.println("Encoding: " + System.getProperty("file.encoding"));
		String lineupId = "CA04542:R";
		EPGProviderService service = new EPGProviderService();
		List<StationChannel> channels = service.getChannels(lineupId);
		assertNotNull(channels);
		assertTrue(channels.size() > 0);
		for (StationChannel channel: channels) {
			System.out.println(channel);
		}
	}

	/*
	 * Test method for 'com.knowbout.epg.EPGProviderService.getScheduledProgram(String, String, Date)'
	 */
	public void testGetScheduledProgram() {
		String lineupId = "CA04542:R";
		String channel = "7";
		Date time = new Date();
		EPGProviderService service = new EPGProviderService();
		ScheduledProgram program = service.getScheduledProgram(lineupId, channel, time);
		assertNotNull(program);
		System.err.println("Now playing on channel 7 is " + program.getProgramTitle());		
	}

	/*
	 * Test method for 'com.knowbout.epg.EPGProviderService.getServiceLineup(String)'
	 */
	public void testGetServiceLineup() {
		String zipCode = "92128";
		EPGProviderService service = new EPGProviderService();
		List<ServiceLineup> lineups = service.getServiceLineup(zipCode);
		assertNotNull(lineups);
		assertTrue(lineups.size() > 0);
		for (ServiceLineup lineup: lineups) {
			System.out.println(lineup);
		}
	}

}
