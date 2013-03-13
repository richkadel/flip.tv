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

package com.knowbout.epg.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import com.knowbout.epg.entities.Network;
import com.knowbout.epg.entities.NetworkLineup;
import com.knowbout.epg.entities.NetworkSchedule;
import com.knowbout.epg.entities.Program;
import com.knowbout.epg.entities.Schedule;
import com.knowbout.epg.entities.Station;
import com.knowbout.hibernate.HibernateUtil;
import com.knowbout.hibernate.TransactionManager;

public class ScheduleParser {

	private static final Log log = LogFactory.getLog(ScheduleParser.class);
	private static final String SAN_DIEGO_TW_CABLE = "SDTW-C";
	private HashMap<String, StationLineup> coveredStations = new HashMap<String, StationLineup>(); 
	private HashMap<Key, ChannelSchedule> programSchedules = new HashMap<Key, ChannelSchedule>();
	private Map<Long, StationLineup> stationIdToLineup = new HashMap<Long, StationLineup>();
	private Configuration config;
	
	public void parseSchedule(InputStream stream, Configuration config) throws IOException  {
		this.config = config;
		loadLineups();
		processChannels();
		processNetworks();
		Date firstSchedule = processSchedules(stream);
		TransactionManager.beginTransaction();
		log.info("About to delete from NetworkSchedule.");
		NetworkSchedule.deleteAfter(firstSchedule);
		log.info("Done deleting from NetworkSchedule. Now deleting from Schedule");
		int deleted = Schedule.deleteAfter(firstSchedule);
		log.info("Deleted schedules after " + firstSchedule + ", " + deleted + " total schedules.");
		int count = 0;				
		int newSchedules = 0;
		List<NetworkLineup> lineups = NetworkLineup.selectSearchableLineups();
		for (ChannelSchedule schedule: programSchedules.values()) {
			//Now if it is a split channel (east & west coast) then dump any show that does not show on both
			schedule.removeSingleProgramming();
			int airings = schedule.getValidAirings().size();
			if (airings > 0) {
				newSchedules += airings;
				createSchedule(schedule, lineups, false);
			}
			if (++count % 50 == 0) {
				Session session = HibernateUtil.currentSession();
				session.flush();
				session.clear();
			}
			if (count % 1000 == 0) {
				log.debug("Processed " + count + " programs for a total of " + newSchedules  + " schedules");
			}
		}
		log.info("Added " + newSchedules + " new schedules (note this these may be on one or more lineups)");
		log.info("Processing special SDTW-C lineup to preserve local data for cc data processing");
		Session session = HibernateUtil.currentSession();
		NetworkLineup networkLineup = (NetworkLineup)session.get(NetworkLineup.class, SAN_DIEGO_TW_CABLE);
		if (networkLineup != null) {
			count = 0;
			List<NetworkLineup> lineupList = new ArrayList<NetworkLineup>();
			lineupList.add(networkLineup);
			newSchedules = 0;
			for (ChannelSchedule schedule: programSchedules.values()) {
				int airings = schedule.getPacificAirings().size();
				airings += schedule.getSingleStationAirings().size();
				if (airings > 0) {
					createSchedule(schedule, lineupList, true);
					newSchedules += airings;
				}
				if (++count % 50 == 0) {
					session.flush();
					session.clear();
				}
				if (count % 1000 == 0) {
					log.debug("Processed " + count + " programs for a total of " + newSchedules  + " schedules");
				}
			}
		}
		TransactionManager.commitTransaction();
		log.info("Finished processing special SDTW-C lineup to preserve local data for cc data processing");
	}
	
	private Date processSchedules(InputStream stream) throws IOException {
		log.debug("Processing raw text schedules for schedules");
		Date firstProgram = null;		
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = reader.readLine();
		SimpleDateFormat  dateTimeFormat = new SimpleDateFormat("yyyyMMddHHmm");
		dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));		
		while (line != null) {
			String[] parts = line.split("\\|", -1);
			long stationId = Long.parseLong(parts[0]);
			String programId = parts[1];
			Date airTime = null;
			try {
				airTime = dateTimeFormat.parse(parts[2]+parts[3]);
			} catch (ParseException e) {
				log.error("Error parsing airtime for :" + line);
			}
			if (airTime != null) {	
				if (firstProgram == null || firstProgram.after(airTime)) {
					firstProgram = airTime;
				}
				StationLineup lineup = stationIdToLineup.get(stationId);
				if (lineup != null) {								
					ChannelSchedule schedule = programSchedules.get(new Key(programId, lineup.getCallSign()));
					if (schedule == null) {
						schedule = new ChannelSchedule(programId, lineup);
						programSchedules.put(new Key(programId, lineup.getCallSign()), schedule);
					} 				
					if (schedule != null) {
						String duration = parts[4];
						int durationInMinutes = 0;
						if (duration.length() == 4) {
							int hours = Integer.parseInt(duration.substring(0,2));
							int minutes = Integer.parseInt(duration.substring(2,4));
							durationInMinutes = hours*60+minutes;
						}
						//End time is calculated, but we need it for searching
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
						calendar.setTime(airTime);
						calendar.add(Calendar.MINUTE, durationInMinutes);
						ScheduleAiring airing = new ScheduleAiring(schedule, stationId,airTime,calendar.getTime(),durationInMinutes, line);
						schedule.addAiring(airing);
					}
				}
				
			}
			line = reader.readLine();
		}
		reader.close();
		log.debug("Finished processing raw text schedules for schedules");
		return firstProgram;
	}
	private void createSchedule(ChannelSchedule channel, List<NetworkLineup> lineups, boolean localHeadend) {
		Session session = HibernateUtil.currentSession();	
		Program program = (Program)session.load(Program.class, channel.getProgramId());
		//Update program rating and runtime. Overloading this information
		Network network = Network.findByCallSign(channel.getStation().getCallSign());
		String tvRating = null;
		int duration = 0;
		Collection<ScheduleAiring> airings;
		if (localHeadend) {
			airings = new ArrayList<ScheduleAiring>();
			airings.addAll(channel.getPacificAirings().values());
			airings.addAll(channel.getSingleStationAirings());
		} else {
			airings = channel.getValidAirings();
		}
		for (ScheduleAiring airing: airings) {
			String rawData = airing.getInputText();
			Schedule schedule = parseSchedule(airing, program, rawData);
			schedule.setNetwork(network);
			HashMap<Date, Schedule> dates = new HashMap<Date, Schedule>();
			for(NetworkLineup lineup: lineups) {
				Date airTime = null;
				//We only modify the time if there are multiple stations (TLC and TLCP)   if
				//It is like ESPN which only has a single feed, east and west coast get the same
				//exact content at the same time (GMT based), just at different local time because of the timezone
				if (channel.getStation().isOnMultipleHeadends()) {
					if (channel.getStation().isAffilateStation()) { 
						airTime = lineup.applyAffiliationDelay(schedule.getAirTime());
					} else {
						airTime = lineup.applyDelay(schedule.getAirTime());
					}
				}
				Schedule existingSchedule = dates.get(airTime);
				if (existingSchedule != null) {
					NetworkSchedule ns = new NetworkSchedule(existingSchedule, lineup, existingSchedule.getAirTime());
					ns.insert();
				} else {
					if (tvRating == null) {
						tvRating = schedule.getTvRating();
						duration = schedule.getDuration();
					}
					existingSchedule = insertSchedule(channel, schedule, lineup);
					if (existingSchedule != null) {
						existingSchedule.insert();
						NetworkSchedule ns = new NetworkSchedule(existingSchedule, lineup, existingSchedule.getAirTime());
						ns.insert();
						dates.put(airTime, existingSchedule);
					}
				}
			}			
		}
		//Since the program does not have this information on it, add it to the override the movie
		//specific values with TV values.
		program.setMpaaRating(tvRating);
		program.setRunTime(duration);
	}
	
	private Schedule insertSchedule(ChannelSchedule channel, Schedule schedule, NetworkLineup lineup) {
		if ((lineup.isDigital() && channel.getStation().isDigital()) ||(!lineup.isDigital() && channel.getStation().isAnalog())) {
			Schedule lineupSpecific = new Schedule();			
			lineupSpecific.copyValues(schedule);
			lineupSpecific.setLineupId(lineup.getId());
			if (channel.getStation().isDualChannels()) {
				if (channel.getStation().isAffilateStation()) {
					lineupSpecific.setAirTime(lineup.applyAffiliationDelay(schedule.getAirTime()));
					lineupSpecific.setEndTime(lineup.applyAffiliationDelay(schedule.getEndTime()));
				} else {
					lineupSpecific.setAirTime(lineup.applyDelay(schedule.getAirTime()));
					lineupSpecific.setEndTime(lineup.applyDelay(schedule.getEndTime()));				
				}
			} else {
				//Single broadcast, so same GMT time regardless of if it is east or west coast feed
				lineupSpecific.setAirTime(schedule.getAirTime());
				lineupSpecific.setEndTime(schedule.getEndTime());								
			}
			
			return lineupSpecific;
		} else {
			return null;
		}
	}
	
	private Schedule parseSchedule(ScheduleAiring airing, Program program, String rawData) {
		Schedule schedule = new Schedule();
		String[] parts = rawData.split("\\|", -1);
		schedule.setAirTime(airing.getStartTime());
		schedule.setEndTime(airing.getEndtime());
		schedule.setProgram(program);
		schedule.setDuration(airing.getDuration());
		if (parts[5].length() > 0) {
			schedule.setPartNumber(Integer.parseInt(parts[5]));
		}
		if (parts[6].length() > 0) {
			schedule.setNumberOfParts(Integer.parseInt(parts[6]));
		}
	    schedule.setCc(parts[7] == null ? false : parts[7].equalsIgnoreCase("Y"));
		schedule.setStereo(parts[8] == null ? false : parts[8].equalsIgnoreCase("Y"));
		//Repeat is gone, now has new
//		schedule.setRepeat(parts[9] == null ? false : parts[9].equalsIgnoreCase("Y"));				
		schedule.setLiveTapeDelay(parts[10]);
		schedule.setSubtitled(parts[11] == null ? false : parts[11].equalsIgnoreCase("Y"));
		schedule.setPremiereFinale(parts[12]);
		schedule.setJoinedInProgress(parts[13] == null ? false : parts[13].equalsIgnoreCase("Y"));
		schedule.setCableInClassroom(parts[14] == null ? false : parts[14].equalsIgnoreCase("Y"));
		schedule.setTvRating(parts[15]);
		schedule.setSap(parts[16] == null ? false : parts[16].equalsIgnoreCase("Y"));
		//Blackout is gone
//		schedule.setBlackout(parts[17] == null ? false : parts[17].equalsIgnoreCase("Y"));
		schedule.setSexRating(parts[18] == null ? false : parts[18].equalsIgnoreCase("Y"));
		schedule.setViolenceRating(parts[19] == null ? false : parts[19].equalsIgnoreCase("Y"));
		schedule.setLanguageRating(parts[20] == null ? false : parts[20].equalsIgnoreCase("Y"));
		schedule.setDialogRating(parts[21] == null ? false : parts[21].equalsIgnoreCase("Y"));
		schedule.setFvRating(parts[22] == null ? false : parts[22].equalsIgnoreCase("Y"));
		schedule.setEnhanced(parts[23] == null ? false : parts[23].equalsIgnoreCase("Y"));
		schedule.setThreeD(parts[24] == null ? false : parts[24].equalsIgnoreCase("Y"));
		schedule.setLetterbox(parts[25] == null ? false : parts[25].equalsIgnoreCase("Y"));
		schedule.setHdtv(parts[26] == null ? false : parts[26].equalsIgnoreCase("Y"));			
		schedule.setDolby(parts[27]);
		schedule.setDvs(parts[28] == null ? false : parts[28].equalsIgnoreCase("Y"));
		schedule.setNewEpisode(parts[30] == null ? false : parts[30].equalsIgnoreCase("new"));
		return schedule;
	}
	
	private void processNetworks() {
		log.debug("Processing networks for schedules");
		HashMap<String, Network> networks = new HashMap<String, Network>();
		List<NetworkLineup> networkLineups = NetworkLineup.selectAll();
		TransactionManager.beginTransaction();
		for(StationLineup lineup: coveredStations.values()) {
			for (Station station: lineup.getStations()) {
				if (stationIdToLineup.put(station.getId(), lineup) != null) {
					log.error("Found duplication StationLine for station " + station.getId());
				} 
			}
			Network network = Network.findByCallSign(lineup.getCallSign());
			if (network == null) {
				network = networks.get(lineup.getCallSign());
				if (network == null) {
					network = new Network();
					network.setAffiliation(lineup.getAffiliation());
					network.setCallSign(lineup.getCallSign());
					network.setName(lineup.getName());
					network.insert();
					for(NetworkLineup networkLineup: networkLineups) {
						//Seperate out the digital/analog channels on the correct lineup.
						if ((!networkLineup.isDigital() && lineup.isAnalog()) || (networkLineup.isDigital() && lineup.isDigital())) {
							Set<Network> availableNetworks = networkLineup.getNetworks();						
							if (!availableNetworks.contains(network)) {
								availableNetworks.add(network);
								network.getNetworkLineups().add(networkLineup);
							}
						}
					}
					networks.put(lineup.getCallSign(), network);
				}
			}
		}
		TransactionManager.commitTransaction();
		log.debug("Finished processing networks for schedules");
}
	private void processChannels() {
		log.debug("Processing channels for schedule");
		HashMap<String, StationLineup> stationMap = new HashMap<String, StationLineup>(); 
		List<Station> stations = Station.selectAll();
		//Process all stations
		for (Station station: stations){
			//HACK(CE): This is work around a CW bug.
			if (!station.getCallSign().equals("KTLA")) {
				StationLineup sl = new StationLineup(station);
				StationLineup existing = stationMap.get(sl.getCallSign());
				//This will happen for affiliations because we are changing the callsigns for them
				//So that the east and west coast lineup.
				if (existing == null) {
					stationMap.put(sl.getCallSign(), sl);			
				} else {
					existing.merge(sl);
					existing.setDualChannels(true);
				}
			}
		}		
		
		Set<StationLineup> processed = new HashSet<StationLineup>();
		//Now process check each station to see if it is on both coasts, or there is a east and pacific station
		for (StationLineup station: stationMap.values().toArray(new StationLineup[0])) {
			//If it might be a pacific station, but only on the pacific headend, then lets check
			if (!processed.contains(station)){
				if (station.possiblePacificChannel()) {
					String eastCallSign = station.strippedPacificCallSign();
					StationLineup eastLineup = stationMap.get(eastCallSign);
					if (eastLineup != null) {
						//Check to see if both are on both headends
						if (eastLineup.isOnMultipleHeadends() && station.isOnMultipleHeadends()) {
							//Add them both to the list since everyone has access to both channels
							coveredStations.put(station.getCallSign(), station);
							coveredStations.put(eastLineup.getCallSign(), eastLineup);
							processed.add(station);
							processed.add(eastLineup);
						} else {
							//Combine them into a single station {
							//Now check to see if it is on both headends
							eastLineup.merge(station);
							if (eastLineup.isOnMultipleHeadends()) {
								eastLineup.setDualChannels(true);
								coveredStations.put(eastLineup.getCallSign(), eastLineup);
								
							}
							processed.add(station);
							processed.add(eastLineup);
						}
					} else {
						//It might not have really been a pacific station, there are about 10 that are like that
						if (station.isOnMultipleHeadends()) {
							coveredStations.put(station.getCallSign(), station);
						}
					}
				}
			}
		}
		for (StationLineup station: stationMap.values().toArray(new StationLineup[0])) {
			if (!processed.contains(station)) {
				if (station.isOnMultipleHeadends()) {
					coveredStations.put(station.getCallSign(), station);				   
				}
				processed.add(station);
			}
		}
		log.debug("Finished processing channels for schedule");
		
	}
	private void loadLineups() {
		log.debug("Processing lineups for schedules");
		int lineupCount = config.getList("lineups.lineup.name").size();
		Session session = HibernateUtil.currentSession();
		TransactionManager.beginTransaction();
		for (int i = 0; i < lineupCount; i++) {
			Configuration lineupConfig = config.subset("lineups.lineup("+i+")");
			String name = lineupConfig.getString("name");
			String id = lineupConfig.getString("id");
			boolean digital = lineupConfig.getBoolean("digital", false);
			int delay = lineupConfig.getInt("delay", 0);
			int affiliateDelay = lineupConfig.getInt("affiliateDelay", 0);
			NetworkLineup sl = null;
			boolean found = false;
			try {
				sl = (NetworkLineup)session.get(NetworkLineup.class, id);	
				found = (sl != null);
			} catch (Exception e)  {
				
			}
			
			if (!found) {
				sl = new NetworkLineup();
				sl.setId(id);
			}
			sl.setName(name);
			sl.setId(id);
			sl.setDigital(digital);
			sl.setDelay(delay);
			sl.setAffiliateDelay(affiliateDelay);
			if (!found){
				sl.insert();
			}
		}
		TransactionManager.commitTransaction();
		log.debug("finished Processing lineups for schedules");
		
	}
	private void debugStationList(Map<String, StationLineup> stationMap) {
		List<StationLineup> values = new ArrayList<StationLineup>();
		values.addAll(stationMap.values());
		Collections.sort(values, new Comparator<StationLineup>() {

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(T, T)
			 */
			public int compare(StationLineup o1, StationLineup o2) {				
				return o1.getCallSign().compareTo(o2.getCallSign());
			}
			
		});
		for (StationLineup station: values) {
			StationLineup covered = coveredStations.get(station.getCallSign());
			String lineups = "";
			String stationIds="";
			if (covered != null) {
				Set<String> lineupsStrings = covered.getLineups();
				for (String id: lineupsStrings) {
					if(lineups.length() > 0) {
						lineups += ", " + id;
					} else {
						lineups += id; 
					}
				}
				Set<Long> stationIdString = covered.getStationIds();
				for (Long id: stationIdString) {
					if(stationIds.length() > 0) {
						stationIds += ", " + id;
					} else {
						stationIds += id; 
					}
				}
			}
			System.err.println(station.getCallSign()+"\t"+station.getName()+"\t"+station.getAffiliation()+"\t"+(covered!=null)+"\t"+lineups +"\t"+stationIds);
		
		}
		System.err.println("Total channels from DB: " + stationMap.size() + " total covered by app: " + coveredStations.size());
	}
	
	private static class Key {
		private String programId;
		private String callSign;
		
		public Key(String programId, String callSign) {
			this.programId = programId;
			this.callSign = callSign;
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof Key) {
				Key key = (Key)obj;
				return programId.equals(key.programId) && callSign.equals(key.callSign);				
			} else {
				return false;
			}
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {			
			return programId.hashCode() ^ callSign.hashCode();
		}
		
		
		
	}
}
