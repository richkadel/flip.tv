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

package com.appeligo.epg;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.appeligo.search.util.ConfigUtils;
import com.appeligo.util.ActiveCache;
import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.Network;
import com.knowbout.epg.service.Program;
import com.knowbout.epg.service.ScheduledProgram;

public class DefaultEpg implements EPGProvider {
	
	private static final Log log = LogFactory.getLog(DefaultEpg.class);
	
	private static DefaultEpg SINGLETON;
	
	private EPGProvider defaultEpgProvider;
	private Map<String,Program> programCache;
	private int minCachedPrograms;
	private int maxCachedPrograms;
	
	private DefaultEpg() {
		Configuration config = ConfigUtils.getSystemConfig();
		try {
			HessianProxyFactory factory = new HessianProxyFactory();
	    	String epgURL = config.getString("epgEndpoint");
	    	defaultEpgProvider = (EPGProvider) factory.create(EPGProvider.class, epgURL);
		} catch (Exception e) {
			log.fatal("Can't connect to EPG.", e);
		}
    	minCachedPrograms = config.getInt("minCachedPrograms", 1000);
    	maxCachedPrograms = config.getInt("maxCachedPrograms", 1500);
    	log.debug("minCachedPrograms="+minCachedPrograms+", maxCachedPrograms="+maxCachedPrograms);
    	programCache = Collections.synchronizedMap(new ActiveCache<String,Program>(minCachedPrograms, maxCachedPrograms));
	}
	
	public synchronized static DefaultEpg getInstance() {
		if (SINGLETON == null) {
			SINGLETON = new DefaultEpg();
		}
		return SINGLETON;
	}
	
	public void clearCaches() {
		programCache.clear();
	}
	
	public List<ScheduledProgram> getAllScheduledPrograms(String lineupId, Date time) {
		return defaultEpgProvider.getAllScheduledPrograms(lineupId, time);
	}

	public ScheduledProgram getLastShowing(String lineupId, String programId) {
		return defaultEpgProvider.getLastShowing(lineupId, programId);
	}

	public ScheduledProgram[] getLastShowingList(String lineupId, List<String> programIds) {
		return defaultEpgProvider.getLastShowingList(lineupId, programIds);
	}

	public List<String> getModifiedProgramIds(Date date) {
		return defaultEpgProvider.getModifiedProgramIds(date);
	}

	public List<Network> getNetworks(String lineupId) {
		return defaultEpgProvider.getNetworks(lineupId);
	}

	public ScheduledProgram getNextShowing(String lineupId, String programId, boolean onlyIfNew, boolean includeNowAiring) {
		return defaultEpgProvider.getNextShowing(lineupId, programId, onlyIfNew, includeNowAiring);
	}

	public ScheduledProgram[] getNextShowingList(String lineupId, List<String> programIds) {
		return defaultEpgProvider.getNextShowingList(lineupId, programIds);
	}

	public ScheduledProgram[] getNextShowingPrograms(List<String> programIds) {
		return defaultEpgProvider.getNextShowingPrograms(programIds);
	}

	public List<ScheduledProgram> getNextShowings(String lineupId, String programId, boolean onlyIfNew, boolean includeNowAiring) {
		return defaultEpgProvider.getNextShowings(lineupId, programId, onlyIfNew, includeNowAiring);
	}

	public Program getProgram(String programId) {
		Program program = programCache.get(programId);
		if (program == null) {
			log.debug("Program "+programId+" not in cache yet");
			program = defaultEpgProvider.getProgram(programId);
			if (program != null) {
				programCache.put(programId, program);
			}
    	} else {
			log.debug("Program "+program+" found in cache");
    	}
		return program;
	}

	public Program getShowByProgramId(String programId) {
		String showId = Program.getShowId(programId);
		return getProgram(showId);
	}
	
	public Program getProgramForTeam(String sportName, String teamName) {
		Program program = defaultEpgProvider.getProgramForTeam(sportName, teamName);
		programCache.put(program.getProgramId(), program);
		return program;
	}

	public Program[] getProgramList(List<String> programIds) {
		List<String> idsNotCached = new ArrayList<String>(programIds.size());
		Program[] programs = new Program[programIds.size()];
		int i = 0;
		synchronized(programCache) {
    		for (String programId : programIds) {
    			programs[i] = programCache.get(programId);
    			if (programs[i] == null) {
    				idsNotCached.add(programId);
    			}
    			i++;
    		}
		}
		log.debug("Ids not in cache yet: "+idsNotCached);
		Program[] programsFromServer = defaultEpgProvider.getProgramList(idsNotCached);
		int j = 0;
		for (i = 0; i < programs.length; i++) {
			if (programs[i] == null) {
				String programId = idsNotCached.get(j);
				Program program = programsFromServer[j++];
				programs[i] = program;
        		programCache.put(programId, program);
			}
		}
		return programs;
	}

	public ScheduledProgram getScheduledProgram(String lineupId, long scheduleId) {
		return defaultEpgProvider.getScheduledProgram(lineupId, scheduleId);
	}

	public ScheduledProgram getScheduledProgramByNetworkCallSign(String lineupId, String callSign, Date time) {
		return defaultEpgProvider.getScheduledProgramByNetworkCallSign(lineupId, callSign, time);
	}

	public ScheduledProgram getScheduledProgramByNetworkId(String lineupId, long networkId, Date time) {
		return defaultEpgProvider.getScheduledProgramByNetworkId(lineupId, networkId, time);
	}
}
