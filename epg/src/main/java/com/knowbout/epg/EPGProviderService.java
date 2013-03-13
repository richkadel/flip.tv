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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;

import com.knowbout.epg.entities.Credit;
import com.knowbout.epg.entities.Network;
import com.knowbout.epg.entities.NetworkLineup;
import com.knowbout.epg.entities.NetworkSchedule;
import com.knowbout.epg.entities.Program;
import com.knowbout.epg.entities.Schedule;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.ScheduledProgram;
import com.knowbout.hibernate.HibernateUtil;



public class EPGProviderService implements EPGProvider {
	
	private static final Log log = LogFactory.getLog(EPGProviderService.class);

	public EPGProviderService() {
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getChannels(java.lang.String)
	 */
	public List<com.knowbout.epg.service.Network> getNetworks(String lineupId) {
		List<com.knowbout.epg.service.Network> serviceNetworks = new ArrayList<com.knowbout.epg.service.Network>();
		Session session = HibernateUtil.currentSession();
		NetworkLineup lineup = (NetworkLineup)session.get(NetworkLineup.class, lineupId);
		if (lineup != null) {
			Set<Network> networks = lineup.getNetworks();
			for (Network network: networks) {				
				serviceNetworks.add(createNetwork(network));
			}
		}
		return serviceNetworks;
	}

	private com.knowbout.epg.service.Network createNetwork(Network network) {
		com.knowbout.epg.service.Network serviceNetwork = new com.knowbout.epg.service.Network();
		serviceNetwork.setAffiliation(network.getAffiliation());
		serviceNetwork.setStationCallSign(network.getCallSign());
		serviceNetwork.setStationName(network.getName());
		serviceNetwork.setLogo(network.getLogo());
		serviceNetwork.setId(network.getId());
		Set<NetworkLineup> networkLineups = network.getNetworkLineups();
		ArrayList<String> lineups = new ArrayList<String>();
		for (NetworkLineup networkLineup: networkLineups) {
			lineups.add(networkLineup.getId());
		}
		serviceNetwork.setLineups(lineups);
		return serviceNetwork;
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getScheduledProgram(java.lang.String, java.lang.String, java.util.Date)
	 */
	public ScheduledProgram getScheduledProgramByNetworkId(String lineupId, long networkId, Date time) {
		//time = convertDate(time); DON'T CONVERT, ASSUME GMT
		ScheduledProgram schedule = null;
		NetworkSchedule foundSchedule = Schedule.selectByTimeAndNetwork(lineupId, time, networkId);
		if ( foundSchedule != null) {
			schedule = createScheduledProgram(foundSchedule, lineupId);
		}
		return schedule;
	}

	public ScheduledProgram getScheduledProgramByNetworkCallSign(String lineupId, String callsign, Date time) {
		//time = convertDate(time); DON'T CONVERT, ASSUME GMT
		ScheduledProgram schedule = null;
		NetworkSchedule foundSchedule = Schedule.selectByTimeAndNetwork(lineupId, time, callsign);
		if ( foundSchedule != null) {
			schedule = createScheduledProgram(foundSchedule, lineupId);
		}
		return schedule;
	}

	

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getScheduledProgram(long)
	 */
	public ScheduledProgram getScheduledProgram(String lineupId, long scheduleId) {
		ScheduledProgram schedule = null;
		Session session = HibernateUtil.currentSession();
		Schedule foundSchedule = (Schedule)session.get(Schedule.class, scheduleId);
		if ( foundSchedule != null) {
			schedule = createScheduledProgram(foundSchedule, lineupId);
		}
		return schedule;
	}

	private ScheduledProgram createScheduledProgram(NetworkSchedule networkSchedule, String lineupId) {
		return createScheduledProgram(networkSchedule.getSchedule(), lineupId);
	}

	private ScheduledProgram createScheduledProgram(Schedule schedule, String lineupId) {
		Network network = schedule.getNetwork();
		Program program = schedule.getProgram();
		ScheduledProgram skedProg = new ScheduledProgram();
		skedProg.setDescription(program.getDescription());
		skedProg.setDescriptionWithActors(program.getDescriptionActors());
		skedProg.setDialogRating(schedule.isDialogRating());
		skedProg.setEndTime(schedule.getEndTime());
		skedProg.setEnhanced(schedule.isEnhanced());
		skedProg.setEpisodeTitle(program.getEpisodeTitle());
		skedProg.setFvRating(schedule.isFvRating());
		skedProg.setHdtv(schedule.isHdtv());
		skedProg.setLanguageRating(schedule.isLanguageRating());
		skedProg.setProgramId(program.getProgramId());
		skedProg.setProgramTitle(program.getProgramTitle());
		skedProg.setReducedTitle40(program.getReducedTitle40());
		skedProg.setScheduleId(schedule.getId());
		skedProg.setSexRating(schedule.isSexRating());
		skedProg.setStarRating(program.getStarRating());
		skedProg.setStartTime(schedule.getAirTime());
		skedProg.setTvRating(schedule.getTvRating());
		skedProg.setViolenceRating(schedule.isViolenceRating());
		skedProg.setOriginalAirDate(program.getOrginalAirDate());
		skedProg.setRunTime(program.getRunTime());
		skedProg.setGenreDescription(program.getGenreDescription());
		skedProg.setNewEpisode(schedule.isNewEpisode());
		skedProg.setLineupId(lineupId);
		Set<Credit> credits = program.getCredits();
		for (Credit credit: credits) {
			com.knowbout.epg.service.Credit serviceCredit = new com.knowbout.epg.service.Credit(credit.getId(), credit.getFirstName(), credit.getLastName(), credit.getRoleDescription());
			skedProg.addCredit(serviceCredit);
		}
		skedProg.setNetwork(createNetwork(network));
		return skedProg;		
	}

	private com.knowbout.epg.service.Program createProgram(Program program) {
		com.knowbout.epg.service.Program serviceProgram = new com.knowbout.epg.service.Program();
		serviceProgram.setDescription(program.getDescription());
		serviceProgram.setDescriptionWithActors(program.getDescriptionActors());
		serviceProgram.setEpisodeTitle(program.getEpisodeTitle());
		serviceProgram.setLanguageRating(program.getGraphicLanguageAdvisory()!= null);
		serviceProgram.setProgramId(program.getProgramId());
		serviceProgram.setProgramTitle(program.getProgramTitle());
		serviceProgram.setReducedTitle40(program.getReducedTitle40());
		serviceProgram.setSexRating(program.getAdultSituationsAdvisory()!= null || program.getBriefNudityAdvisory() != null);
		serviceProgram.setStarRating((double)program.getStarRating());
		serviceProgram.setTvRating(program.getMpaaRating());
		serviceProgram.setViolenceRating(program.getGraphicViolenceAdvisory() != null);
		serviceProgram.setOriginalAirDate(program.getOrginalAirDate());
		serviceProgram.setGenreDescription(program.getGenreDescription());
		serviceProgram.setRunTime(program.getRunTime());
		serviceProgram.setLastModified(program.getLastModified());
//		serviceProgram.setProgramType(program.getProgramType());
		Set<Credit> credits = program.getCredits();
		for (Credit credit: credits) {
			com.knowbout.epg.service.Credit serviceCredit = new com.knowbout.epg.service.Credit(credit.getId(), credit.getFirstName(), credit.getLastName(), credit.getRoleDescription());
			serviceProgram.addCredit(serviceCredit);
		}
		
		return serviceProgram;		
	}
	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getScheduledPrograms(java.lang.String, java.util.Date)
	 */
	public List<ScheduledProgram> getAllScheduledPrograms(String lineupId, Date time) {
		List<ScheduledProgram> programs = new ArrayList<ScheduledProgram>();
		try {
			
			List<NetworkSchedule> schedules = Schedule.selectByTime(lineupId, time);
			for (NetworkSchedule sked: schedules) {
				programs.add(createScheduledProgram(sked, lineupId));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return programs;
	}
		
	/**
	 * Get a program for a given id.  Ignore the start/end times
	 * @param programId The program id
	 * @return the Program or null if it is not defined.
	 */
	public com.knowbout.epg.service.Program getProgram(String programId) {
		Program program = Program.selectById(programId);
		if (program != null) {
			return createProgram(program);
		} else {
			return null;
		}
	}

	/**
	 * Get a program for a given id.  Ignore the start/end times
	 * @param programId The program id
	 * @return the Program or null if it is not defined.
	 */
	public com.knowbout.epg.service.Program getProgramForTeam(String sportName, String teamName) {
		Program program = Program.selectByTeam(sportName, teamName);
		if (program != null) {
			return createProgram(program);
		} else {
			return null;
		}
	}

	
	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getShow(java.lang.String)
	 */
	public com.knowbout.epg.service.Program getShowByProgramId(String programId) {
		Program program = Program.selectShowById(programId, 1);
		if (program == null) {
			return null;
		} else {
			return createProgram(program);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getLastShowing(java.lang.String, java.lang.String)
	 */
	public ScheduledProgram getLastShowing(String lineupId, String programId) {
		NetworkSchedule schedule;
		if (programId.startsWith("SH")) {
    		schedule = Schedule.selectLastShowOrEpisode(lineupId, programId, new Date());
		} else if (programId.startsWith("TE")) {
    		schedule = Schedule.selectLastTeamGame(lineupId, programId, new Date());
		} else {
    		schedule = Schedule.selectLastProgram(lineupId, programId, new Date());
		}
		if (schedule != null) {
			return createScheduledProgram(schedule, lineupId);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getNextShowing(java.lang.String, java.lang.String, boolean)
	 */
	public ScheduledProgram getNextShowing(String lineupId, String programId, boolean onlyIfNew, boolean includeNowAiring) {
		List<NetworkSchedule> schedules;
		try {
			if (programId.startsWith("SH")) {
	    		schedules = Schedule.selectNextShowOrEpisodes(lineupId, programId, onlyIfNew, includeNowAiring, 1);
			} else if (programId.startsWith("TE")) {
	    		schedules = Schedule.selectNextTeamGames(lineupId, programId, onlyIfNew, includeNowAiring, 1);
			} else {
	    		schedules = Schedule.selectNextPrograms(lineupId, programId, onlyIfNew, includeNowAiring, 1);
			}
			if (schedules.size() > 0) {
				return createScheduledProgram(schedules.get(0), lineupId);
			}
		} catch (ObjectNotFoundException e){
			log.error("Unable to find object for lineup: "+ lineupId + " programId: " + programId + " onlyNew:  "+ onlyIfNew, e);
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getNextShowings(java.lang.String, java.lang.String, boolean)
	 */
	public List<ScheduledProgram> getNextShowings(String lineupId, String programId, boolean onlyIfNew, boolean includeNowAiring) {
		List<ScheduledProgram> skedProgs = new ArrayList<ScheduledProgram>();
		try {
			List<NetworkSchedule> schedules;
			if (programId.startsWith("SH")) {
	    		schedules = Schedule.selectNextShowOrEpisodes(lineupId, programId, onlyIfNew, includeNowAiring, 0);
			} else if (programId.startsWith("TE")) {
	    		schedules = Schedule.selectNextTeamGames(lineupId, programId, onlyIfNew, includeNowAiring, 0);
			} else if (programId.startsWith("SP")) {
				com.knowbout.epg.service.Program program = getProgram(programId);
	    		List<NetworkSchedule> awaySchedules = Schedule.selectNextTeamGamesByName(lineupId, program.getSportName(),
	    				program.getAwayTeamName(), onlyIfNew, includeNowAiring, 0);
	    		List<NetworkSchedule> homeSchedules = Schedule.selectNextTeamGamesByName(lineupId, program.getSportName(),
	    				program.getHomeTeamName(), onlyIfNew, includeNowAiring, 0);
	    		schedules = new ArrayList<NetworkSchedule>();
	    		schedules.addAll(awaySchedules);
	    		for (NetworkSchedule homeSchedule : homeSchedules) {
	    			boolean found = false;
	    			for (NetworkSchedule awaySchedule : awaySchedules) {
	    				if (homeSchedule.getId().equals(awaySchedule.getId())) {
	    					found = true;
	    					break;
	    				}
	    			}
	    			if (!found) {
	    				schedules.add(homeSchedule);
	    			}
	    		}
			} else {
	    		schedules = Schedule.selectNextPrograms(lineupId, programId, onlyIfNew, includeNowAiring, 0);
			}
			for (NetworkSchedule schedule: schedules) {
					skedProgs.add(createScheduledProgram(schedule, lineupId));
			}		
		} catch (ObjectNotFoundException e){
			log.error("getNextShowings: Unable to find object for lineup: "+ lineupId + " programId: " + programId + " onlyNew:  "+ onlyIfNew, e);
		}
		return skedProgs;
	}


	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getNextShowingPrograms(java.util.List)
	 */
	public ScheduledProgram[] getNextShowingPrograms(List<String> programIds) {
		try {
			if (programIds == null) {
				return new ScheduledProgram[0];
			} else {
				List<ScheduledProgram> skeds = new ArrayList<ScheduledProgram>();
				List<NetworkSchedule> nextAirings = Schedule.selectNextPrograms(programIds, new Date());
				for (NetworkSchedule airing : nextAirings) {
					skeds.add(createScheduledProgram(airing, airing.getNetworkLineup().getId()));
				}			
				return skeds.toArray(new ScheduledProgram[0]);
			}
		} catch (ObjectNotFoundException e){
			log.error("getNextShowings: Unable to find object for programIds " + programIds, e);
			return new ScheduledProgram[0];
		}
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getNextShowing(java.lang.String, java.util.List)
	 */
	public ScheduledProgram[] getNextShowingList(String headend, List<String> programIds) {
		if (programIds == null) {
			return new ScheduledProgram[0];
		} else {
			List<ScheduledProgram> skeds = new ArrayList<ScheduledProgram>();
			List<NetworkSchedule> nextAirings = Schedule.selectNextPrograms(headend, programIds, new Date());
			for (NetworkSchedule airing : nextAirings) {
				skeds.add(createScheduledProgram(airing, headend));
			}			
			return skeds.toArray(new ScheduledProgram[0]);
		}
	}
	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getLastShowingList(java.lang.String, java.util.List)
	 */
	public ScheduledProgram[] getLastShowingList(String headend, List<String> programIds) {
		if (programIds == null) {
			return new ScheduledProgram[0];
		} else {
			List<ScheduledProgram> skeds = new ArrayList<ScheduledProgram>();
			List<NetworkSchedule> airings = Schedule.selectLastPrograms(headend, programIds, new Date());
			for (NetworkSchedule airing : airings) {
				skeds.add(createScheduledProgram(airing, headend));
			}			
			return skeds.toArray(new ScheduledProgram[0]);
		}	
	}


	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getProgramList(java.util.List)
	 */
	public com.knowbout.epg.service.Program[] getProgramList(List<String> programIds) {
		if (programIds == null) {
			return new ScheduledProgram[0];
		} else {
			com.knowbout.epg.service.Program[] progs = new com.knowbout.epg.service.Program[programIds.size()];
			for (int i = 0; i < programIds.size(); i++) {
				progs[i] = getProgram(programIds.get(i));
			}
			return progs;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getModifiedProgramIds(java.util.Date)
	 */
	public List<String> getModifiedProgramIds(Date date) {
		List<Program> programs = Program.selectByModificationDate(date);
		List<String> ids = new ArrayList<String>();
		for (Program prog: programs) {
			ids.add(prog.getProgramId());
		}
		return ids;
	}

}
