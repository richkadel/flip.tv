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

package com.knowbout.epg.service;

import java.util.Date;
import java.util.List;

/**
 * This service provides the Electronic Program Guide information.
 * @author Chris Exline
 *
 */
public interface EPGProvider {

	
	/**
	 * This method will retrieve the service lineups that are offered for the
	 * given zipcode.  A ServiceLineup is the Headend (Time Warner) and the specific
	 * lineup (Digital or Analog).
	 * @param zipcode The 5 digit zipcode
	 * @return A list of ServiceLineups that are available for the given zipcode.  Will
	 * return an empty list if there are no providers for the given zipcode.
	 */
//	public List<ServiceLineup> getServiceLineup(String zipcode);
	
	/**
	 * Return the ScheduledProgram for the given ServiceLineup, on a given network for a given time.  
	 * @param lineup  The id of the ServiceLineup to use
	 * @param networkId The specific networkId desired
	 * @param time The time that the program will be airing.
	 * @return The ScheduledProgram
	 */	
	public ScheduledProgram getScheduledProgramByNetworkId(String lineupId, long networkId, Date time);
	

	/**
	 * Return the ScheduledProgram for the given ServiceLineup, on a given network for a given time.  
	 * @param lineup  The id of the ServiceLineup to use
	 * @param callSign The specific network callsign for the schedule desired
	 * @param time The time that the program will be airing.
	 * @return The ScheduledProgram
	 */	
	public ScheduledProgram getScheduledProgramByNetworkCallSign(String lineupId, String callSign, Date time);

	/**
	 * Get the list of all scheduled programs for all networks on a given lineup at a given time.
	 * @param lineup The lineup to get information about
	 * @param time The time that the programs will be airing
	 * @return A list of SchedulePrograms
	 */
	public List<ScheduledProgram> getAllScheduledPrograms(String lineupId, Date time);
	
	/**
	 * Get the list of scheduled progrsm for the given channels for the given lineup at the the given time.
	 * @param lineup The lineup to get the information about
	 * @param channels The list of channels to get the scheduled programs for
	 * @param date The time that the programs will be airing
	 * @return A list of scheduled Programs
	 */
//	public List<ScheduledProgram> getScheduledPrograms(String lineupId, List<String> channels, Date date);

	/**
	 * Get the ChannelSchedule for the lineup, channels and time window
	 * @param lineup The lineup to get the channel schedule for
	 * @param channels The desired channels to get the program schedule for
	 * @param start The start time of the time windo
	 * @param end The end time of the time window
	 * @return A list of ChannelSchedules 
	 */
//	public List<ChannelSchedule> getChannelSchedules(String lineupId, List<String> channels, Date start, Date end);
	
	/**
	 * Get the list of networks for a given ServiceLineup
	 * @param lineup The lineup to retrieve networks for
	 * @return A list of Networks
	 */
	public List<Network> getNetworks(String lineupId);
	
	
	/**
	 * Get a program for a given id.  
	 * @param programId The program id
	 * @return the Program or null if it is not defined.
	 */
	public Program getProgram(String programId);
	
	/**
	 * Get a program for a sports team.
	 * @param sportName Like "MLB Baseball"
	 * @param teamName Typically the city and name like "San Diego Chargers"
	 * @return the Program or null if it is not defined, creating a new team program
	 * if there is at least one known game for the team.
	 */
	public Program getProgramForTeam(String sportName, String teamName);
	
	/**
	 * If the given program ID is an EPisode, then this strips off the last 4 digits
	 * (episode part) and replaces it with 4 zeros, and changes EP to SH, looks up the
	 * SHow if available, and if not, but if there are matching EPisodes, it creates
	 * a new default SHow and returns it.  If it's not an EPisode id, then it just
	 * returns the matching Program for the given ID (creating SHows if SH and not
	 * found).
	 * Get a single program for a Show (i.e. a series like Friends or any other
	 * non-episode show). This returns
	 * a specific instance of the show, so the descriptions and such are
	 * not of much use for now.  
	 * @param programId The programId of the show or one of its episodes
	 * @return the Program or null if it is not defined.
	 */
	public Program getShowByProgramId(String programId);	
	
	/**
	 * Get the ScheduledProgram for the given id.
	 * @param scheduleId The scheduled program id.
	 * @return The ScheduledProgram or null if it is not defined.
	 */
	public ScheduledProgram getScheduledProgram(String lineupId, long scheduleId);
	
	/**
	 * Get the last showing for a particular program on a given lineup.
	 * @param lineupId The lineup to  search for scheduled programs
	 * @param programId The program you want information on
	 * @return The ScheduledProgram if the programed aired in the past, or null if it was not.
	 */
	public ScheduledProgram getLastShowing(String lineupId, String programId);

	/**
	 * Get the next showing for a particular program on a given lineup.
	 * @param lineupId The lineup to  search for scheduled programs
	 * @param programId The program you want information on
	 * @param onlyIfNew Only return new episodes
	 * @param includeNowAiring If the program indicated happens to be on now, include it if this is true,
	 * else if false, only return shows that START after now
	 * @return The ScheduledProgram if a showing is scheduled in the future is found or null if not.
	 */
	public ScheduledProgram getNextShowing(String lineupId, String programId, boolean onlyIfNew, boolean includeNowAiring);
	
	/**
	 * Get all upcoming showings for a particular program on a given lineup.
	 * @param lineupId The lineup to  search for scheduled programs
	 * @param programId The program you want information on
	 * @param onlyIfNew Return only new episodes or first airings
	 * @param includeNowAiring If the program indicated happens to be on now, include it if this is true,
	 * else if false, only return shows that START after now
	 * @return The ScheduledProgram if a showing is scheduled in the future is found or null if not.
	 */
	public List<ScheduledProgram> getNextShowings(String lineupId, String programId, boolean onlyIfNew, boolean includeNowAiring);

	/**
	 * Get the next showing for a list of programs on a given lineup.
	 * @param lineupId The lineup to  search for scheduled programs
	 * @param programId The program you want information on
	 * @return an array of shedule programs.  This list will not contain null entries. If a schedule is
	 * not found, then the returning array will be smaller than the program id list.
	 */
	public ScheduledProgram[] getNextShowingList(String lineupId, List<String> programIds);

	/**
	 * Get the next showing for a list of programs for any lineup
	 * @param programId The program you want information on
	 * @return an array of shedule programs.  This list will not contain null entries. If a schedule is
	 * not found, then the returning array may be smaller than the program id list.  If the a program is on
	 * multiple lineups, then a ScheduledProgram will be returned for each lineup, so the resulting array will 
	 * be larger than the list of programIds.
	 */
	public ScheduledProgram[] getNextShowingPrograms(List<String> programIds);
	
	/**
	 * Get the last showing for a list of programs on a given lineup.
	 * @param lineupId The lineup to  search for scheduled programs
	 * @param programId The program you want information on
	 * @return an array of shedule programs.  This list will not contain null entries. If a schedule is
	 * not found, then the returning array will be smaller than the program id list.
	 */
	public ScheduledProgram[] getLastShowingList(String lineupId, List<String> programIds);

	/**
	 * Get the program info for a list of programs
	 * @param programIds The list of programs you want information on
	 * @return an array of shedule programs.  It will contain null entries of there was not a 
	 *  program for the given program id at the same index in the list.
	 */
	public Program[] getProgramList(List<String> programIds);

	/**
	 * Returns the list of program ids for programs that have been modified since the given date.
	 * @param date the date 
	 * @return
	 */
	public List<String> getModifiedProgramIds(Date date);
}
