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

/**
 * 
 */
package com.knowbout.nlp.keywords.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.cc2nlp.CCEvent;
import com.knowbout.cc2nlp.CCEventService;
import com.knowbout.cc2nlp.CCSentenceEvent;
import com.knowbout.cc2nlp.CCXDSEvent;
import com.knowbout.cc4j.XDSData;
import com.knowbout.cc4j.XDSField;
import com.knowbout.epg.service.ScheduledProgram;
import com.knowbout.epg.service.StationChannel;

/**
 * @author fear
 *
 */
public class HessianExample {

	private static final BufferedReader getReader() throws Exception {
		InputStream is = HessianExample.class.getResourceAsStream("/resources/discoverchannel.txt");
		//InputStream is = HessianExample.class.getResourceAsStream("/resources/cnnheadlinenews.txt");
		//InputStream is = HessianExample.class.getResourceAsStream("/resources/newsorsimilar.txt");
		InputStreamReader isr = new InputStreamReader(is);
		return new BufferedReader(isr);
	}
	
	/**
	 * @param args 
	 */
	public static void main(String[] args) throws Exception {
		String CHANNEL = "70";
		BufferedReader reader = getReader();
		
		String url = "http://localhost:8080/keywords/cceventservice";
		//String url = "http://demo.knowbout.tv/keywords/cceventservice";
		HessianProxyFactory factory = new HessianProxyFactory();
		CCEventService eventService = (CCEventService) factory.create(CCEventService.class, url);
		System.out.println(eventService);
		
		ScheduledProgram scheduledProgram = new ScheduledProgram();
		StationChannel stationChannel = new StationChannel(CHANNEL, "History", "HIST", "Discover Networks");
		scheduledProgram.setChannel(stationChannel);
		scheduledProgram.setDescription("Jason and the Argonauts Description");
		scheduledProgram.setEndTime(new Date(System.currentTimeMillis() + 1000*60*60));
		scheduledProgram.setProgramTitle("Jason and the Argonauts");
		scheduledProgram.setStartTime(new Date());
		scheduledProgram.setEpisodeTitle("Episode 1");
		scheduledProgram.setProgramTitle("Mythic Adventures");
		scheduledProgram.setTvRating("PG-TV");
		scheduledProgram.setProgramId("EP7101690003");
		//eventService.startProgram(scheduledProgram);
		
		CCXDSEvent event = new CCXDSEvent();
		event.setXDSField(XDSField.PROGRAM_NAME);
		event.setXDSData(new XDSData());
		System.out.println(eventService.captureXDS(event));
		
		long programStartTime = System.currentTimeMillis();
		
		String textLine =  reader.readLine().toLowerCase();
		while (textLine != null) {
			String status = eventService.captureSentence(new CCSentenceEvent(
					"CA04542:DEFAULT", CHANNEL, programStartTime, System.currentTimeMillis(), "speaker", textLine + '\0'));
			Thread.sleep(1500);
			System.out.println(textLine + " : " + status);
			textLine =  reader.readLine().toLowerCase();
		}
		System.out.println("Did you see anything in the server console?");

	}

}
