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

package com.appeligo.epg.demo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.knowbout.epg.service.ChannelSchedule;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.Program;
import com.knowbout.epg.service.ScheduledProgram;
import com.knowbout.epg.service.ServiceLineup;
import com.knowbout.epg.service.StationChannel;



public class DemoEPGService implements EPGProvider {
	
	private static final Log log = LogFactory.getLog(DemoEPGService.class);

	public DemoEPGService() {
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getChannels(java.lang.String)
	 */
	public List<StationChannel> getChannels(String lineupId) {
		List<StationChannel> stationChannels = new ArrayList<StationChannel>();
		StationChannel station = createStationChannel("41","Versus", "VS","Versus");
		stationChannels.add(station);
		return stationChannels;
	}

	private StationChannel createStationChannel(String number, String affiliation, String callSign, String name) {
		StationChannel stationChannel = new StationChannel();
		stationChannel.setChannel(number);
		stationChannel.setAffiliation(affiliation);
		stationChannel.setStationCallSign(callSign);
		stationChannel.setStationName(name);
		return stationChannel;
	}
	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getChannelSchedules(java.lang.String, java.util.List, java.util.Date, java.util.Date)
	 */
	public List<ChannelSchedule> getChannelSchedules(String lineupId, List<String> channelList, Date start, Date end) {
		List<ChannelSchedule> channelSchedule = new ArrayList<ChannelSchedule>();
		for (String channelNumber: channelList) {
			StationChannel stationChannel = null;
			if (channelNumber.equals("41")) {
				stationChannel = createStationChannel("41","Versus", "VS","Versus");
			} else {
				stationChannel = createStationChannel(channelNumber, channelNumber, channelNumber,channelNumber);
			}
			Calendar cal = Calendar.getInstance();
			cal.setTime(start);
			int minute = cal.get(Calendar.MINUTE);
			if (minute < 30) {
				cal.set(Calendar.MINUTE, 0);
			} else {
				cal.set(Calendar.MINUTE, 30);
			}
			long startBlock = cal.getTimeInMillis()/1000/60/30;
			start = cal.getTime(); 
			cal.setTime(end);
			minute = cal.get(Calendar.MINUTE);
			if (minute < 30) {
				cal.set(Calendar.MINUTE, 30);
			} else {
				cal.set(Calendar.MINUTE, 0);
				cal.add(Calendar.HOUR_OF_DAY, 1);
			}
			end = cal.getTime();
			long endBlock = cal.getTimeInMillis()/1000/60/30;

			long diff = endBlock-startBlock;
			cal.setTime(start);
			ArrayList<ScheduledProgram> programs = new ArrayList<ScheduledProgram>();
			for (int i = 0; i < diff; i++){						    
				ScheduledProgram program = createScheduledProgram(cal.getTime(), null, stationChannel);
				programs.add(program);
				cal.add(Calendar.MINUTE,30);
			}
			ChannelSchedule chanSked = new ChannelSchedule(stationChannel, programs);
			channelSchedule.add(chanSked);
		}
		return channelSchedule;
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getScheduledProgram(java.lang.String, java.lang.String, java.util.Date)
	 */
	public ScheduledProgram getScheduledProgram(String lineupId, String channelNumber, Date time) {

		StationChannel stationChannel = null;
		if (channelNumber.equals("41")) {
			stationChannel = createStationChannel("41","Versus", "VS","Versus");
		} else {
			stationChannel = createStationChannel(channelNumber, channelNumber, channelNumber,channelNumber);
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		int minute = cal.get(Calendar.MINUTE);
		if (minute < 30) {
			cal.set(Calendar.MINUTE, 0);
		} else {
			cal.set(Calendar.MINUTE, 30);
		}
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		time = cal.getTime(); 
		ScheduledProgram program = createScheduledProgram(time, null, stationChannel);
		return program;
	}

	private ScheduledProgram createScheduledProgram(Date start, String programId, StationChannel station) {
		if (programId == null) {
			programId = "SH4849110000";
		}
		ScheduledProgram skedProg = new ScheduledProgram();
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		String scheduleId = "SID:" + cal.get(Calendar.YEAR)+cal.get(Calendar.MONTH)+cal.get(Calendar.DAY_OF_MONTH)+cal.get(Calendar.HOUR_OF_DAY)+cal.get(Calendar.MINUTE);
		cal.add(Calendar.MINUTE, 30);		
		skedProg.setDescription("Variety of African hunts.");
		skedProg.setDescriptionWithActors("Variety of African hunts.");
		skedProg.setDialogRating(false);
		skedProg.setEndTime(cal.getTime());
		skedProg.setEnhanced(true);
		skedProg.setEpisodeTitle("Namibia");
		skedProg.setFvRating(false);
		skedProg.setHdtv(true);
		skedProg.setLanguageRating(false);
		skedProg.setProgramId(programId);
		skedProg.setProgramTitle("Safari Hunter's Journal");
		skedProg.setScheduleId(scheduleId);
		skedProg.setSexRating(false);
		skedProg.setStarRating(4.0);
		skedProg.setStartTime(start);
		skedProg.setTvRating("TV-PG13");
		skedProg.setViolenceRating(false);
		skedProg.setChannel(station);
		return skedProg;		
	}

	private com.knowbout.epg.service.Program createProgram(String programId) {
		if (programId == null) {
			programId = "SH4849110000";
		}
		com.knowbout.epg.service.Program serviceProgram = new com.knowbout.epg.service.Program();
		serviceProgram.setDescription("Variety of African hunts.");
		serviceProgram.setDescriptionWithActors("Variety of African hunts.");
		serviceProgram.setDialogRating(false);
		serviceProgram.setEnhanced(true);
		serviceProgram.setEpisodeTitle("Namibia");
		serviceProgram.setFvRating(false);
		serviceProgram.setHdtv(true);
		serviceProgram.setLanguageRating(false);
		serviceProgram.setProgramId(programId);
		serviceProgram.setProgramTitle("Safari Hunter's Journal");
		serviceProgram.setSexRating(false);
		serviceProgram.setStarRating(4.0);
		serviceProgram.setTvRating("TV-PG13");
		serviceProgram.setViolenceRating(false);
		return serviceProgram;		
	}
	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getScheduledPrograms(java.lang.String, java.util.Date)
	 */
	public List<ScheduledProgram> getAllScheduledPrograms(String lineupId, Date time) {
		//time = convertDate(time); DON'T CONVERT, ASSUME GMT
		StationChannel stationChannel = createStationChannel("41","Versus", "VS","Versus");
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		int minute = cal.get(Calendar.MINUTE);
		if (minute < 30) {
			cal.set(Calendar.MINUTE, 0);
		} else {
			cal.set(Calendar.MINUTE, 30);
		}
		time = cal.getTime(); 
		List<ScheduledProgram> programs = new ArrayList<ScheduledProgram>();
		programs.add(createScheduledProgram(time,null, stationChannel));
		return programs;
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getScheduledPrograms(java.lang.String, java.util.List, java.util.Date)
	 */
	public List<ScheduledProgram> getScheduledPrograms(String lineupId, List<String> channelNumbers, Date time) {
		//time = convertDate(time); DON'T CONVERT, ASSUME GMT
		List<ScheduledProgram> programs = new ArrayList<ScheduledProgram>();
		for (String channelNumber: channelNumbers) {
			programs.add(getScheduledProgram(lineupId, channelNumber, time));
		}
		return programs;
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getServicelineup(java.lang.String)
	 */
	public List<ServiceLineup> getServiceLineup(String zipcode) {
		ArrayList<ServiceLineup> serviceLineups = new ArrayList<ServiceLineup>();
		ServiceLineup serviceLineup = new ServiceLineup();
		serviceLineup.setDemographicMarketArea("San Diego");
		serviceLineup.setName("Time Warner Cable");
		serviceLineup.setZipCode(zipcode);
		serviceLineup.setId("CA04542:DEFAULT");
		serviceLineup.setLineup("Cable");
		
		ServiceLineup serviceLineup2 = new ServiceLineup();
		serviceLineup2.setDemographicMarketArea("San Diego");
		serviceLineup2.setName("Time Warner Cable");
		serviceLineup2.setZipCode(zipcode);
		serviceLineup2.setId("CA04542:R");
		serviceLineup2.setLineup("Cable-ready TV sets (non-rebuild)");

		serviceLineups.add(serviceLineup);
		serviceLineups.add(serviceLineup2);
		return serviceLineups;
	}
		
	/**
	 * Get a program for a given id.  Ignore the start/end times
	 * @param programId The program id
	 * @return the Program or null if it is not defined.
	 */
	public com.knowbout.epg.service.Program getProgram(String programId) {
		return createProgram(programId);
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getLastShowing(java.lang.String, java.lang.String)
	 */
	public ScheduledProgram getLastShowing(String lineupId, String programId) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		cal.set(Calendar.HOUR, 18);
		cal.set(Calendar.MINUTE,0);
		StationChannel station = createStationChannel("41","Versus", "VS","Versus");
		return createScheduledProgram(cal.getTime(), programId, station);
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getNextShowing(java.lang.String, java.lang.String)
	 */
	public ScheduledProgram getNextShowing(String lineupId, String programId) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR, 18);
		cal.set(Calendar.MINUTE,0);
		StationChannel station = createStationChannel("41","Versus", "VS","Versus");
		return createScheduledProgram(cal.getTime(), programId, station);
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getScheduleForProgram(java.lang.String, java.lang.String, java.util.Date, java.util.Date)
	 */
	public List<ScheduledProgram> getScheduleForProgram(String lineupId, String programId, Date start, Date end) {
		List<ScheduledProgram> skedProgs = new ArrayList<ScheduledProgram>();
		skedProgs.add(getLastShowing(lineupId, programId));
		skedProgs.add(getNextShowing(lineupId, programId));
		return skedProgs;
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getNextShowingList(java.lang.String, java.util.List)
	 */
	public ScheduledProgram[] getNextShowingList(String headend, List<String> programIds) {
		if (programIds == null) {
			return new ScheduledProgram[0];
		} else {
			ScheduledProgram[] skeds = new ScheduledProgram[programIds.size()];
			for (int i = 0; i < programIds.size(); i++) {
				skeds[i] = getNextShowing(headend, programIds.get(i));
			}
			return skeds;
		}
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getLastShowingList(java.lang.String, java.util.List)
	 */
	public ScheduledProgram[] getLastShowingList(String headend, List<String> programIds) {
		if (programIds == null) {
			return new ScheduledProgram[0];
		} else {
			ScheduledProgram[] skeds = new ScheduledProgram[programIds.size()];
			for (int i = 0; i < programIds.size(); i++) {
				skeds[i] = getLastShowing(headend, programIds.get(i));
			}
			return skeds;
		}
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getScheduleForShow(java.lang.String, java.lang.String, java.util.Date, java.util.Date, int)
	 */
	public List<ScheduledProgram> getScheduleForShow(String arg0, String arg1, Date arg2, Date arg3, int arg4) {
		// TODO Auto-generated method stub
		return getScheduleForProgram(arg0, arg1, arg2, arg3);
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getProgramList(java.util.List)
	 */
	public Program[] getProgramList(List<String> programIds) {
		if (programIds == null) {
			return new ScheduledProgram[0];
		} else {
			Program[] progs = new Program[programIds.size()];
			for (int i = 0; i < programIds.size(); i++) {
				progs[i] = getProgram(programIds.get(i));
			}
			return progs;
		}
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getModifiedProgramIds(java.util.Date)
	 */
	public List<String> getModifiedProgramIds(Date arg0) {
		// TODO Auto-generated method stub
		return new ArrayList<String>();
	}

	
		

}
