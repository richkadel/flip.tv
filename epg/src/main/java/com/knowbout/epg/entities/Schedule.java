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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.StringType;

import com.knowbout.hibernate.HibernateUtil;

public class Schedule {

	private long id;	
    private Date airTime;
    private Date endTime;
    private int duration;
    private int partNumber;
    private int numberOfParts;
    private boolean cc;
    private boolean stereo;
    private boolean newEpisode;
    private String liveTapeDelay;
    private boolean subtitled;
    private String premiereFinale;
    private boolean joinedInProgress;
    private boolean cableInClassroom;
    private String tvRating;
    private boolean sap;
    private boolean sexRating;
    private boolean violenceRating;
    private boolean languageRating;
    private boolean dialogRating;
    private boolean fvRating;
    private boolean enhanced;
    private boolean threeD;
    private boolean letterbox;
    private boolean hdtv;
    private String dolby;
    private boolean dvs;
    private Network network;
    private String lineupId;
    private Program program;
    private Set<NetworkSchedule> networkSchedules;
    
    public Schedule() {    	
    }
    
	/**
	 * @return Returns the airTime.
	 */
	public Date getAirTime() {
		return airTime;
	}
	/**
	 * @param airTime The airTime to set.
	 */
	public void setAirTime(Date airTime) {
		this.airTime = airTime;
	}
	/**
	 * @return Returns the cableInClassroom.
	 */
	public boolean isCableInClassroom() {
		return cableInClassroom;
	}
	/**
	 * @param cableInClassroom The cableInClassroom to set.
	 */
	public void setCableInClassroom(boolean cableInClassroom) {
		this.cableInClassroom = cableInClassroom;
	}
	/**
	 * @return Returns the cc.
	 */
	public boolean isCc() {
		return cc;
	}
	/**
	 * @param cc The cc to set.
	 */
	public void setCc(boolean cc) {
		this.cc = cc;
	}
	/**
	 * @return Returns the dialogRating.
	 */
	public boolean isDialogRating() {
		return dialogRating;
	}
	/**
	 * @param dialogRating The dialogRating to set.
	 */
	public void setDialogRating(boolean dialogRating) {
		this.dialogRating = dialogRating;
	}
	/**
	 * @return Returns the dolby.
	 */
	public String getDolby() {
		return dolby;
	}
	/**
	 * @param dolby The dolby to set.
	 */
	public void setDolby(String dolby) {
		this.dolby = dolby;
	}
	/**
	 * @return Returns the duration.
	 */
	public int getDuration() {
		return duration;
	}
	/**
	 * @param duration The duration to set.
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}
	/**
	 * @return Returns the dvs.
	 */
	public boolean isDvs() {
		return dvs;
	}
	/**
	 * @param dvs The dvs to set.
	 */
	public void setDvs(boolean dvs) {
		this.dvs = dvs;
	}
	/**
	 * @return Returns the enhanced.
	 */
	public boolean isEnhanced() {
		return enhanced;
	}
	/**
	 * @param enhanced The enhanced to set.
	 */
	public void setEnhanced(boolean enhanced) {
		this.enhanced = enhanced;
	}
	/**
	 * @return Returns the fvRating.
	 */
	public boolean isFvRating() {
		return fvRating;
	}
	/**
	 * @param fvRating The fvRating to set.
	 */
	public void setFvRating(boolean fvRating) {
		this.fvRating = fvRating;
	}
	/**
	 * @return Returns the hdtv.
	 */
	public boolean isHdtv() {
		return hdtv;
	}
	/**
	 * @param hdtv The hdtv to set.
	 */
	public void setHdtv(boolean hdtv) {
		this.hdtv = hdtv;
	}
	/**
	 * @return Returns the id.
	 */
	public long getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(long id) {
		this.id = id;
	}
	/**
	 * @return Returns the joinedInProgress.
	 */
	public boolean isJoinedInProgress() {
		return joinedInProgress;
	}
	/**
	 * @param joinedInProgress The joinedInProgress to set.
	 */
	public void setJoinedInProgress(boolean joinedInProgress) {
		this.joinedInProgress = joinedInProgress;
	}
	/**
	 * @return Returns the languageRating.
	 */
	public boolean isLanguageRating() {
		return languageRating;
	}
	/**
	 * @param languageRating The languageRating to set.
	 */
	public void setLanguageRating(boolean languageRating) {
		this.languageRating = languageRating;
	}
	/**
	 * @return Returns the letterbox.
	 */
	public boolean isLetterbox() {
		return letterbox;
	}
	/**
	 * @param letterbox The letterbox to set.
	 */
	public void setLetterbox(boolean letterbox) {
		this.letterbox = letterbox;
	}
	/**
	 * @return Returns the liveTapeDelay.
	 */
	public String getLiveTapeDelay() {
		return liveTapeDelay;
	}
	/**
	 * @param liveTapeDelay The liveTapeDelay to set.
	 */
	public void setLiveTapeDelay(String liveTapeDelay) {
		this.liveTapeDelay = liveTapeDelay;
	}
	/**
	 * @return Returns the numberOfParts.
	 */
	public int getNumberOfParts() {
		return numberOfParts;
	}
	/**
	 * @param numberOfParts The numberOfParts to set.
	 */
	public void setNumberOfParts(int numberOfParts) {
		this.numberOfParts = numberOfParts;
	}
	/**
	 * @return Returns the partNumber.
	 */
	public int getPartNumber() {
		return partNumber;
	}
	/**
	 * @param partNumber The partNumber to set.
	 */
	public void setPartNumber(int partNumber) {
		this.partNumber = partNumber;
	}
	/**
	 * @return Returns the premiereFinale.
	 */
	public String getPremiereFinale() {
		return premiereFinale;
	}
	/**
	 * @param premiereFinale The premiereFinale to set.
	 */
	public void setPremiereFinale(String premiereFinale) {
		this.premiereFinale = premiereFinale;
	}
	/**
	 * @return Returns the program.
	 */
	public Program getProgram() {
		return program;
	}
	/**
	 * @param program The program to set.
	 */
	public void setProgram(Program program) {
		this.program = program;
	}

	/**
	 * @return Returns the newEpisode.
	 */
	public boolean isNewEpisode() {
		return newEpisode;
	}

	/**
	 * @param newEpisode The newEpisode to set.
	 */
	public void setNewEpisode(boolean newEpisode) {
		this.newEpisode = newEpisode;
	}

	/**
	 * @return Returns the sap.
	 */
	public boolean isSap() {
		return sap;
	}
	/**
	 * @param sap The sap to set.
	 */
	public void setSap(boolean sap) {
		this.sap = sap;
	}
	/**
	 * @return Returns the sexRating.
	 */
	public boolean isSexRating() {
		return sexRating;
	}
	/**
	 * @param sexRating The sexRating to set.
	 */
	public void setSexRating(boolean sexRating) {
		this.sexRating = sexRating;
	}

	/**
	 * @return Returns the stereo.
	 */
	public boolean isStereo() {
		return stereo;
	}
	/**
	 * @param stereo The stereo to set.
	 */
	public void setStereo(boolean stereo) {
		this.stereo = stereo;
	}
	/**
	 * @return Returns the subtitled.
	 */
	public boolean isSubtitled() {
		return subtitled;
	}
	/**
	 * @param subtitled The subtitled to set.
	 */
	public void setSubtitled(boolean subtitled) {
		this.subtitled = subtitled;
	}
	/**
	 * @return Returns the threeD.
	 */
	public boolean isThreeD() {
		return threeD;
	}
	/**
	 * @param threeD The threeD to set.
	 */
	public void setThreeD(boolean threeD) {
		this.threeD = threeD;
	}
	/**
	 * @return Returns the tvRating.
	 */
	public String getTvRating() {
		return tvRating;
	}
	/**
	 * @param tvRating The tvRating to set.
	 */
	public void setTvRating(String tvRating) {
		this.tvRating = tvRating;
	}
	/**
	 * @return Returns the violenceRating.
	 */
	public boolean isViolenceRating() {
		return violenceRating;
	}
	/**
	 * @param violenceRating The violenceRating to set.
	 */
	public void setViolenceRating(boolean violenceRating) {
		this.violenceRating = violenceRating;
	}
    
	public void insert() {
		Session session = HibernateUtil.currentSession();
		session.save(this);
	}
	
	public void delete() {
		Session session = HibernateUtil.currentSession();
		session.delete(this);
	}
	
	public static Schedule selectById(String scheduleId) {
		Session session = HibernateUtil.currentSession();
		return (Schedule)session.get(Schedule.class, scheduleId);
	}
	/**
	 * @return Returns the endTime.
	 */
	public Date getEndTime() {
		return endTime;
	}
	/**
	 * @param endTime The endTime to set.
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	
	/**
	 * @return Returns the lineupId.
	 */
	public String getLineupId() {
		return lineupId;
	}

	/**
	 * @param lineupId The lineupId to set.
	 */
	public void setLineupId(String lineupId) {
		this.lineupId = lineupId;
	}

	
	/**
	 * @return Returns the network.
	 */
	public Network getNetwork() {
		return network;
	}

	/**
	 * @param network The network to set.
	 */
	public void setNetwork(Network network) {
		this.network = network;
	}
	
	
	
	/**
	 * @return Returns the lineups.
	 */
	public Set<NetworkSchedule> getNetworkSchedules() {
		if (networkSchedules == null) {
			networkSchedules = new HashSet<NetworkSchedule>();
		}
		return networkSchedules;
	}

	/**
	 * @param lineups The lineups to set.
	 */
	public void setNetworkSchedules(Set<NetworkSchedule> networkSchedules) {
		this.networkSchedules = networkSchedules;
	}
	
	public void addNetworkSchedule(NetworkSchedule networkSchedule) {
		Set<NetworkSchedule> ns = getNetworkSchedules();
		if (!ns.contains(networkSchedule)) {
			ns.add(networkSchedule);
		}
	}
	
	public void removeNetworkSchedule(NetworkSchedule networkSchedule){
		getNetworkSchedules().remove(networkSchedule);
	}

	public void copyValues(Schedule schedule) {
		this.airTime = schedule.airTime;
		this.endTime = schedule.endTime;
		this.duration = schedule.duration;
		this.partNumber = schedule.partNumber;
		this.numberOfParts = schedule.numberOfParts;
		this.cc = schedule.cc;
		this.stereo = schedule.stereo;
		this.newEpisode = schedule.newEpisode;
		this.liveTapeDelay = schedule.liveTapeDelay;
		this.subtitled = schedule.subtitled;
		this.premiereFinale = schedule.premiereFinale;
		this.joinedInProgress = schedule.joinedInProgress;
		this.cableInClassroom = schedule.cableInClassroom;
		this.tvRating = schedule.tvRating;
		this.sap = schedule.sap;
		this.sexRating = schedule.sexRating;
		this.violenceRating = schedule.violenceRating;
		this.languageRating = schedule.languageRating;
		this.dialogRating = schedule.dialogRating;
		this.fvRating = schedule.fvRating;
		this.enhanced = schedule.enhanced;
		this.threeD = schedule.threeD;
		this.letterbox = schedule.letterbox;
		this.hdtv = schedule.hdtv;
		this.dolby = schedule.dolby;
		this.dvs = schedule.dvs;
		this.network = schedule.network;
		this.lineupId = schedule.lineupId;
		this.program = schedule.program;		
	}

	/**
	 */
	public static NetworkSchedule selectByTimeAndNetwork(String lineup, Date date, long networkId) {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Schedule.getNowPlaying");
		query.setTimestamp("currentTime", date);
		query.setLong("network", networkId);
		query.setString("lineup", lineup);
		NetworkSchedule sked=  (NetworkSchedule)query.uniqueResult();
		return sked;
	}
	
	/**
	 */
	public static NetworkSchedule selectByTimeAndNetwork(String lineup, Date date, String callSign) {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Schedule.getNowPlayingCallSign");
		query.setTimestamp("currentTime", date);
		query.setString("callSign", callSign);
		query.setString("lineup", lineup);
		NetworkSchedule sked=  (NetworkSchedule)query.uniqueResult();
		return sked;
	}	

	/**
	 */
	@SuppressWarnings("unchecked")
	public static List<NetworkSchedule> selectByTime(String lineup, Date date) {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Schedule.getNowPlayingLineup");
		query.setTimestamp("currentTime", date);
		query.setString("lineup", lineup);
		return (List<NetworkSchedule>)query.uniqueResult();
	}

	private static Query createNextAiringsQuery(String lineup, String where,
			boolean onlyIfNew, boolean includeNowAiring, int limit) {
		StringBuilder q = new StringBuilder();
		q.append("from NetworkSchedule as n ");
		q.append(where);
		q.append(" and n.networkLineup.id = :lineup");
		if (includeNowAiring) {
    		q.append(" and n.schedule.endTime > :currentTime");
		} else {
    		q.append(" and n.schedule.airTime > :currentTime");
		}
		if (onlyIfNew) {
			q.append(" and n.schedule.newEpisode = true");
		}
		q.append(" order by n.schedule.airTime");
		Session session = HibernateUtil.currentSession();
		Query query = session.createQuery(q.toString());
		query.setTimestamp("currentTime", new Date());
		query.setString("lineup", lineup);
		if (limit > 0) {
    		query.setMaxResults(limit);		
		}
		return query;
	}

	@SuppressWarnings("unchecked")
	public static List<NetworkSchedule> selectNextPrograms(String lineup, String programId,
			boolean onlyIfNew, boolean includeNowAiring, int limit) {
		Query query = createNextAiringsQuery(lineup,
        		"where n.schedule.program.programId = :programId",
				onlyIfNew, includeNowAiring, limit);
		query.setString("programId", programId);
		return query.list();
		
	}

	@SuppressWarnings("unchecked")
	public static List<NetworkSchedule> selectNextShowOrEpisodes(
			String lineup, String showId, boolean onlyIfNew, boolean includeNowAiring, int limit) {
		assert showId.startsWith("SH") : "showId must start with SH";
		Query query = createNextAiringsQuery(lineup,
    			"where ( n.schedule.program.programId = :showId or n.schedule.program.programId like :episodeIdLike )",
				onlyIfNew, includeNowAiring, limit);
		query.setString("showId", showId);
		query.setString("episodeIdLike", "EP"+showId.substring(2,10)+"%");
		return query.list();
	}

	@SuppressWarnings("unchecked")
	public static List<NetworkSchedule> selectNextTeamGames(String lineup, String teamId, boolean onlyIfNew, boolean includeNowAiring, int limit) {
		assert teamId.startsWith("TE") : "teamId must start with TE";
		Program team = Program.selectById(teamId);
		assert (team != null) : "Couldn't find team for programId "+teamId;
		return selectNextTeamGamesByName(lineup, team.getSportName(), team.getTeamName(), onlyIfNew, includeNowAiring, limit);
	}

	public static List<NetworkSchedule> selectNextTeamGamesByName(String lineup,
			String sportName, String teamName, boolean onlyIfNew, boolean includeNowAiring, int limit) {
		Query query = createNextAiringsQuery(lineup,
    			"where n.schedule.program.programId like 'SP%' and" +
    			" n.schedule.program.programTitle = :sportName and" +
    			" n.schedule.program.episodeTitle like :teamLike",
    			onlyIfNew, includeNowAiring, limit);
		query.setString("sportName", sportName);
		query.setString("teamLike", "%"+teamName+"%");
		return query.list();
	}

	@SuppressWarnings("unchecked")
	public static List<NetworkSchedule> selectNextPrograms(List<String> programId, Date startDate) {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Schedule.getAllNextProgramSchedulesForList");
		query.setTimestamp("startTime", startDate);
		query.setParameterList("programIds", programId, new StringType());
		return query.list();			
	}
	
	@SuppressWarnings("unchecked")
	public static List<NetworkSchedule> selectNextPrograms(String lineup, List<String> programId, Date startDate) {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Schedule.getNextProgramSchedulesForList");
		query.setTimestamp("startTime", startDate);
		query.setParameterList("programIds", programId, new StringType());
		query.setString("lineup", lineup);
		return query.list();			
	}
	
	@SuppressWarnings("unchecked")
	public static List<NetworkSchedule> selectLastPrograms(String lineup, List<String> programId, Date startDate) {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Schedule.getLastProgramSchedulesForList");
		query.setTimestamp("startTime", startDate);
		query.setParameterList("programIds", programId, new StringType());
		query.setString("lineup", lineup);
		return query.list();			
	}
	
	
	@SuppressWarnings("unchecked")
	public static NetworkSchedule selectLastProgram(String lineup, String programId, Date startDate) {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Schedule.getLastProgramSchedule");
		query.setTimestamp("startTime", startDate);
		query.setString("programId", programId);
		query.setString("lineup", lineup);
		query.setMaxResults(1);		
		return (NetworkSchedule)query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public static NetworkSchedule selectLastShowOrEpisode(String lineup, String showId, Date startDate) {
		assert showId.startsWith("SH") : "showId must start with SH";
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Schedule.getLastShowSchedule");
		query.setTimestamp("startTime", startDate);
		query.setString("showId", showId);
		query.setString("episodeIdLike", "EP"+showId.substring(2,10)+"%");
		query.setString("lineup", lineup);
		query.setMaxResults(1);		
		return (NetworkSchedule)query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public static NetworkSchedule selectLastTeamGame(String lineup, String teamId, Date startDate) {
		assert teamId.startsWith("TE") : "teamId must start with TE";
		Program team = Program.selectById(teamId);
		assert (team != null) : "Couldn't find team for programId "+teamId;
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Schedule.getLastTeamSchedule");
		query.setTimestamp("startTime", startDate);
		query.setString("sportName", team.getProgramTitle());
		query.setString("teamLike", "%"+team.getEpisodeTitle()+"%");
		query.setString("lineup", lineup);
		query.setMaxResults(1);		
		return (NetworkSchedule)query.uniqueResult();
	}

	
	public static int deleteAfter(Date date) {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Schedule.deleteByDate");
		query.setTimestamp("date", date);
		int count = query.executeUpdate();
		return count;		
	}
}
