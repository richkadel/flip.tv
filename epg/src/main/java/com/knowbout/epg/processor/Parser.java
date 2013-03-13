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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.knowbout.epg.entities.Channel;
import com.knowbout.epg.entities.Community;
import com.knowbout.epg.entities.Credit;
import com.knowbout.epg.entities.CreditType;
import com.knowbout.epg.entities.Device;
import com.knowbout.epg.entities.Headend;
import com.knowbout.epg.entities.Lineup;
import com.knowbout.epg.entities.Program;
import com.knowbout.epg.entities.Schedule;
import com.knowbout.epg.entities.Station;
import com.knowbout.hibernate.HibernateUtil;
import com.knowbout.hibernate.TransactionManager;

public class Parser {

	private static final Log log = LogFactory.getLog(Parser.class);

	private File headendFile;
	private File lineupFile;
	private File stationFile;
	private File programFile;
	private File scheduleFile;
	private int headendTransaction;
	private int lineupTransaction;
	private int stationTransaction;
	private int programTransaction;
	private Configuration config;
	private HashMap<String, String> stationNames;
	@SuppressWarnings("unchecked")
	
	
	public Parser(Configuration config, File headendFile, File lineupFile, 
			File stationFile, File programFile,	File scheduleFile) {
		this.headendFile = headendFile;
		this.lineupFile = lineupFile;
		this.stationFile = stationFile;
		this.programFile = programFile;
		this.scheduleFile = scheduleFile;
		this.config = config;
		//Get the transaction count.  This can affect the performance a lot
		Configuration transactions = config.subset("transactions");
		headendTransaction = transactions.getInt("headends");
		lineupTransaction = transactions.getInt("lineups");
		stationTransaction = transactions.getInt("stations");
		programTransaction = transactions.getInt("programs");
		stationNames = new HashMap<String, String>();
		List<String> names = config.getList("stationNames.station", new ArrayList<String>());
		List<String> callSigns = config.getList("stationNames.station[@callSign]", new ArrayList<String>());
		for (int i = 0; i < callSigns.size() && i < names.size(); i++) {
			stationNames.put(callSigns.get(i), names.get(i));
		}
	}

	public void parse() throws IOException {
		HibernateUtil.openSession();
		try {
			TransactionManager.beginTransaction();
			log.debug("Begun transaction for headend");
			GZIPInputStream uis = new GZIPInputStream(new FileInputStream(headendFile));
			log.debug("About to parse Headends");
			parseHeadend(uis);		
			TransactionManager.commitTransaction();
	
			TransactionManager.beginTransaction();
			log.debug("Begun transaction for station");
			log.debug("About to parse Stations");
			uis = new GZIPInputStream(new FileInputStream(stationFile));
			parseStation(uis);
			TransactionManager.commitTransaction();
			
			TransactionManager.beginTransaction();
			log.debug("Begun transaction for lineup");
			log.debug("About to parse Lineups/Channels");
			uis = new GZIPInputStream(new FileInputStream(lineupFile));
			parseLineup(uis);
			TransactionManager.commitTransaction();
			
			TransactionManager.beginTransaction();
			log.debug("Begun transaction for programs");
			log.debug("About to parse Programs");
			uis = new GZIPInputStream(new FileInputStream(programFile));
			parseProgram(uis);
			log.debug("About to commit last transaction for programs");
			if (TransactionManager.currentTransaction().isActive()) {
				log.debug("it is an active transaction");
			} else {
				log.debug("it is NOT an active transaction");
			}
			TransactionManager.commitTransaction();
					
			uis = new GZIPInputStream(new FileInputStream(scheduleFile));
			log.debug("About to parse Schedules");
			ScheduleParser parser = new ScheduleParser();
			parser.parseSchedule(uis, config);
		} catch (IOException e) {
			TransactionManager.rollbackTransaction();
			throw e;
		} finally {
			HibernateUtil.closeSession();
		}

	}
	
	/**
	 * Field # Field Name Min Max Field Description Field Example 
	 * 1 he_headend_id 7 7 unique 7-character alphanumeric code NY31703 
	 * 2 he_community_name 28 community served by a cable headend SALEM 
	 * 3 he_county_name 25 county served by a cable headend WASHINGTON 
	 * 4 he_county_size 1 county size determined by population: A, largest; D, smallest C 
	 * 5 he_st_county_code 5 unique code assigned to every U.S. county and state 31115 
	 * 6 he_state_served 2 state served by a cable headend NY 
	 * 7 he_zip_code 5 ZIP Code served by a cable headend 12865 
	 * 8 he_dma_code 3 unique 3-digit code assigned to each Designated Market Area 518 
	 * 9 he_dma_name 70 Designated Market Area of the provider's headend location ALBANY-SCHENECTADY-TROY 
	 * 10 he_mso_code 5 unique 5-digit code assigned to a Multiple System Operator 08670 
	 * 11 he_dma_rank 4 Designated Market Area rank based on TV households 52 
	 * 12 he_headend_name 42 name of the headend TIME WARNER CABLE 
	 * 13 he_headend_location 28 community where headend's physical equipment is located GREENWICH 
	 * 14 he_mso_name 42 name of Multiple System Operator TIME WARNER CABLE INC. 
	 * 15 he_time_zone_code 1 time zone where headend is located: 1-Eastern, 2-Central, 3-Mountain, 4-Pacific, 5-Yukon, 6-Hawaiian 
	 * 
	 * @param stream
	 * @throws IOException
	 */
	private void parseHeadend(InputStream stream) throws IOException  {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = reader.readLine();
		int count = 0;
		while (line != null) {
			if (count++ % headendTransaction == 0) {
				TransactionManager.commitTransaction();				
				TransactionManager.beginTransaction();			
			}
			String[] parts = line.split("\\|", -1);
			Headend headend = Headend.selectById(parts[0]);
			boolean newHeadend = false;
			if (headend == null) {
				headend = new Headend();
				headend.setId(parts[0]);
				newHeadend = true;
			}
			String zipCode = parts[6];
			Community community = Community.selectById(zipCode);
			if (community == null) {
				community = new Community();
				community.setId(zipCode);
				community.setName(parts[1]);
				community.setCountyName(parts[2]);
				community.setCountySize(parts[3]);
				community.setCountyCode(Integer.parseInt(parts[4]));
				community.setState(parts[5]);
				community.setZipCode(zipCode);
				community.addHeadend(headend);
				community.insert();
			} else {
				if (!community.getHeadends().contains(headend)) {
					log.debug("adding headend to community : "+ community.getZipCode() + " headend: " + headend.getId());
					community.addHeadend(headend);
				}
			}
			if (newHeadend) {
				headend.setDmaCode(Integer.parseInt(parts[7]));
				headend.setDmaName(parts[8]);
				headend.setMsoCode(Integer.parseInt(parts[9]));
				headend.setDmaRank(Integer.parseInt(parts[10]));
				headend.setHeadendName(parts[11]);			
				headend.setHeadendLocation(parts[12]);
				headend.setMsoName(parts[13]);
				headend.setTimeZoneCode(Integer.parseInt(parts[14]));
				headend.insert();
			}
			line = reader.readLine();
		}
		reader.close();		
	}
	
	/**
	 * Field # Field Name Min Max Field Description Field Example 
	 * 1 cl_row_id 1 12 unique number for each row in this file 543680 
	 * 2 cl_headend_id 7 7 unique 7-character alphanumeric code NY31703 
	 * 3 cl_device 1 i.e., converter box C 
	 * 4 cl_station_num 12 unique station ID number 14075 
	 * 5 cl_tms_chan 5 physical location of a station or a cable network on a lineup. If this field is blank, it is channel 000. 081;1013;G5-13 
	 * 6 cl_service_tier 1 one of five levels of service: 1-Basic, 2-Extended Basic, 3-Premium, 4-Pay Per View, 5-Music 4 
	 * 7 cl_effective_date 8 date the channel becomes effective 19980408 
	 * 8 cl_expiration_date 8 date the channel expires 19980430
	 * @param stream
	 * @throws IOException
	 */
	private void parseLineup(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = reader.readLine();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		int count = 0;
		while (line != null) {
			if (count++ % lineupTransaction == 0) {
				TransactionManager.commitTransaction();				
				TransactionManager.beginTransaction();			
			}
			String[] parts = line.split("\\|", -1);
			Device device = Device.DEFAULT;
			if (parts[2].length() > 0) {
				device = Device.valueOf(parts[2]);
			}
			String headendId = parts[1];
			String lineupId = headendId +":"+device.name();
			Lineup lineup = Lineup.selectById(lineupId);
			
			if (lineup == null) {
				lineup = new Lineup();
				lineup.setId(lineupId);
				lineup.setDevice(device);
				lineup.setName(lineup.getDevice().toString());
				Headend headend = Headend.selectById(headendId);
				if (headend != null) {
					headend.addLineup(lineup);
				}
				lineup.insert();
			}
			//If the channel is past its expiration date, don't process it.
			//Should we delete it?
			boolean process = true;
			Date expirationDate = null;
			if (parts[7].length() > 0) {
				try {
					expirationDate = format.parse(parts[7]);
					if (expirationDate.getTime() < System.currentTimeMillis()) {
						process = false;
					}
				} catch (ParseException e) {
					log.error("ParseException", e);
				}
			}		
			if (process) {
				Long stationId = Long.parseLong(parts[3]);
				String channelNumber = parts[4];
				//Strip off any leading 0's;
				channelNumber = channelNumber.replaceAll("^0+", "");

				String channelId = lineupId+":"+channelNumber+":"+stationId;
				
				Channel channel = Channel.selectById(channelId);
				boolean newChannel = false;
				if (channel == null) {
					channel = new Channel();
					channel.setId(channelId);
					channel.setLineup(lineup);
					newChannel = true;
				} else {
					//TODO: How do we handle this?
//					log.debug("Found duplicate channel : " + Arrays.asList(parts));
				}
				channel.setChannelNumber(channelNumber);
				channel.setServiceTier(Integer.parseInt(parts[5]));
				if (parts[6].length() > 0) {
					try {
						channel.setEffectiveDate(format.parse(parts[6]));
					} catch (ParseException e) {
						log.error("ParseException", e);
					}
				}
				if (expirationDate != null) {
					channel.setExpirationDate(expirationDate);
				}			
				
				Station station = Station.selectById(stationId);
				if (station == null) {
					//TODO: Is there anything we can do about this?
					log.debug("Unable to find station for : "+ parts[3] + " from line " + Arrays.asList(parts));
				}
				if (newChannel && station != null) {
					station.addChannel(channel);
					channel.insert();
				} 
			}
			line = reader.readLine();
		}
		reader.close();
		
	}

	/**
	 * Field # Field Name Min Max Field Description Field Example 
	 * 1 tf_station_num 1 10 Unique station ID number. 11259 
	 * 2 tf_station_time_zone 30 Native time zone of a station. Eastern D.S. 
	 * 3 tf_station_name 40 Long name of a station. WABC-TV, Home Box Office 
	 * 4 tf_station_call_sign 1 10 Mnemonic or FCC-recognized call sign for long name of a station. WABC, HBO 
	 * 5 tf_station_affil 25 Network, cable or broadcasting group with which a station is associated. ABC Affiliate, PAY 
	 * 6 tf_station_city 20 Station mailing address: city. New York 
	 * 7 tf_station_state 15 Station mailing address: state. NY 
	 * 8 tf_station_zip_code 12 Station mailing address: ZIP Code. 10023 
	 * 9 tf_station_country 15 Station mailing address: country. United States 
	 * 10 tf_dma_name 70 City and state of the broadcast station designated market area. New York, NY 
	 * 11 tf_dma_num 10 Numeric ranking of the designated market area. 1 
	 * 12 tf_fcc_channel_num 8 FCC channel number of a broadcast station. 7 
	 * 13-22 tf_user_data Reserved
	 * @param stream
	 * @throws IOException
	 */
	private void parseStation(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = reader.readLine();
		int count = 0;
		while (line != null) {
			if (count++ % stationTransaction == 0) {
				TransactionManager.commitTransaction();				
				TransactionManager.beginTransaction();			
			}
			String[] parts = line.split("\\|", -1);
			Station station = Station.selectById(Long.parseLong(parts[0]));
			if (station == null) {
				station = new Station();
				station.setId(Long.parseLong(parts[0]));
				station.insert();
			}
			station.setTimeZone(parts[1]);
			String name = stationNames.get(parts[3]);
			if (name != null) {
				station.setName(name);
			} else {
				station.setName(parts[2]);			
			}
			
			station.setCallSign(parts[3]);
			station.setAffiliation(parts[4]);
			station.setCity(parts[5]);
			station.setState(parts[6]);
			station.setZipCode(parts[7]);
			station.setCountry(parts[8]);
			station.setDmaName(parts[9]);
			station.setDmaNumber(Integer.parseInt(parts[10]));
			if (parts[11].length() > 0) {
				station.setFccChannelNumber(Integer.parseInt(parts[11]));
			}
			
			line = reader.readLine();
		}
		reader.close();	
	}
	
	/**
	 * Field # Field Name Min Max Field Description Field Example 
	 * 1 tf_database_key[3] 12 12 Unique description identifier, necessary to reference movies, shows, episodes, sports from description file. MV1234560000; SH0123450000 
	 * 2 tf_title 1 120 Official name by which a movie, show, episode or sports event is known. In the Heat of the Night 
	 * 3 tf_reduced_title 70 Shortened version of a program's original title. In the Heat of Night 
	 * 4 tf_reduced_title 40 Shortened version of a program's original title. Heat of the Night 
	 * 5 tf_reduced_title 20 Shortened version of a program's original title. Heat of Night
	 * 6 tf_reduced_title 10 Shortened version of a program's original title. Heat
	 * 7 tf_alt_title 120 Alias name for program title; the title Paid Programming is stored here. Feed the Children 
	 * 8 tf_reduced_desc 100 Shorter version of a program's original description. Pete and Berg tell Bill the story of how they met Sharon in college. 
	 * 9 tf_reduced_desc 60 Shorter version of a program's original description. Pete and Berg tell Bill how they met Sharon in college. 
	 * 10 tf_reduced_desc 40 Shorter version of a program's original description. Pete and Berg recall how they met Sharon. 
	 * 11 tf_advisory_desc 30 Notation of adult content in movies, shows and episodes. Adult Situations 
	 * 12 tf_advisory_desc 30 Notation of explicit language in movies, shows and episodes. Graphic Language 
	 * 13 tf_advisory_desc 30 Notation of nudity in movies, shows and episodes. Brief Nudity 
	 * 14 tf_advisory_desc 30 Notation of violence in movies, shows and episodes. Graphic Violence 
	 * 15 tf_advisory_desc 30 Notation of sexual content in movies, shows and episodes. Strong Sexual Content 
	 * 16 tf_advisory_desc 30 Notation of rape in movies, shows and episodes. Rape 
	 * 17,20- 74 tf_cast_first_name 20 First name of an actor listed in the cast of a show, episode or movie. Tom 
	 * 18,21- 75 tf_cast_last_name 20 Last name of an actor listed in the cast of a show, episode or movie. Hanks 
	 * 19,22-76 tf_cast_role_desc 30 Designates actor or guest star. Actor 
	 * 77,80-134 tf_credits_first_name 20 First name of a host, director, producer, executive producer or writer of a show, episode or movie. Cameron 
	 * 78,81-135 tf_credits_last_name 20 Last name of a host, director, producer, executive producer or writer of a show, episode or movie. Crowe 
	 * 79,82-136 tf_credits_role_desc 30 Describes programming credits of a show or movie. Director 
	 * 137-142 tf_genre_desc (See Appendix A) 30 Word or group of words that classifies a show, episode, movie or sports event. Cooking 
	 * 143 tf_desc 255 Word string that describes the show, episode or movie content. Roseanne buys a shiny new 1998 Chevy Camaro. 
	 * 144 tf_year 4 The year in which a feature film was released; yyyy format; for movies only. 1998 
	 * 145 tf_mpaa_rating 5 Rating supplied by the Motion Picture Association of America; for movies only. PG-13 
	 * 146 tf_star_rating 5 In movies, an arbitrary critical rating from 1/2 to 4 stars. ***+ 
	 * 147 tf_run_time 4 Actual length of time any programming airs. Not the same as duration; hhmm format; for movies only. 0059 is fifty-nine minutes; 0125 is one hour and twenty five minutes 
	 * 148 tf_color_code 20 Designates whether a program was produced in color or black/white. Colorized 
	 * 149 tf_program_language 10 Language of the copy (description) of a program. Spanish 
	 * 150 tf_org_country 15 Used in movies to distinguish between domestic and foreign films. Also known as country of origin. United States 
	 * 151 tf_made_for_tv 1 Designator for a film that was made specifically for television. Y or N 
	 * 152 tf_source_type 10 Specifies network, local, syndicated or multiple-block programming. Syndicated 
	 * 153 tf_show_type 30 Distinguishes how a program was originally produced and/or distributed. Paid Programming; Series 
	 * 154 tf_holiday 30 Description of a recognized or traditional holiday. Christmas 
	 * 155 tf_syn_epi_num 20 Distributor-designated number corresponding to an episode of a specific show. 16 
	 * 156 tf_alt_syn_epi_num 20 Alternate numbering system for syndicated programming. Can differ from syndicated numbering system. 809 
	 * 157 tf_epi_title 150 Also known as the subtitle; descriptive title within an episode; team vs. team can be located here. The Puffy Shirt; Super Bowl XXXIII: Atlanta Falcons vs. Denver Broncos 
	 * 158 tf_net_syn_source 10 Originating network. Fox 
	 * 159 tf_net_syn_type 21 Specifies broadcast network, first run syndicated, cash barter or off-network programming. First run syndicated 
	 * 160 tf_desc 255 Word string that describes the show, episode or movie content and includes embedded actors within the description. A tornado whisks Kansas farm girl Dorothy (Judy Garland) and her dog, Toto, to a magical land populated by odd characters (Ray Bolger)(Bert Lahr). 
	 * 161 tf_reduced_desc 100 Shorter version of a program's original description which includes embedded actors within the description. A tornado whisks Kansas farm girl Dorothy (Judy Garland) into a magical land. 
	 * 162 tf_org_studio 25 Name of company responsible for the distribution of a movie. 20th Century Fox 
	 * 163 tf_game_date 8 Game date as reported by league or station schedule; yyyymmdd format. 19991025 
	 * 164 tf_game_time 4 Game time as reported by league or station schedule; hhmm format. 1700 
	 * 165 tf_game_time_zone 30 Time zone of tf_game_time, not necessarily the time zone of the event. Eastern D.S. 
	 * 166 tf_org_air_date 8 Original air date for the program. 19960914 
	 * 167 tf_unique_id 8 Unique hexadecimal ID for the program. 15b9275a 
	 * 168-180 tf_user_data reserved
	 * @param stream
	 * @throws IOException
	 */
	private void parseProgram(InputStream stream) throws IOException {
		HashMap<String, Program> programs = new HashMap<String, Program>();
		HashMap<Long, Credit> credits = new HashMap<Long, Credit>();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
		SimpleDateFormat yearMonthDayFormat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat hourMinutesFormat = new SimpleDateFormat("HHmm");

		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "Cp850"));
		String line = reader.readLine();
		Date lastModified = new Date();
		int count = 1;
		while (line != null) {
			if (count++ % programTransaction == 0) {
				log.debug("Committing transaction for programs, count="+count);
				if (TransactionManager.currentTransaction().isActive()) {
					log.debug("it is an active transaction");
				} else {
					log.debug("it is NOT an active transaction");
				}
				TransactionManager.commitTransaction();				
				TransactionManager.beginTransaction();
				log.debug("Begun another transaction for programs");
				if (TransactionManager.currentTransaction().isActive()) {
					log.debug("it is an active transaction");
				} else {
					log.debug("it is NOT an active transaction");
				}
				programs.clear();
				credits.clear();
			}
			String[] parts = line.split("\\|", -1);
			String programId = parts[0];
			if (programId.trim().length() == 0) {
				line = reader.readLine();
				continue;
			}
			Program program = programs.get(programId);			
			if (program == null) {
				program = Program.selectById(programId);
				if (program == null) {
					program = new Program();
					program.setProgramId(programId);
					program.setLastModified(lastModified);
					program.insert();
					programs.put(programId, program);		
					parseCredits(program, parts, credits);
				}
			} else {
				log.debug(" Found duplicate program for : " +programId + " in line " + Arrays.asList(parts));
			}

			//PENDING(CE): Always set last modified so the file can be checked for updates to
			//the schedule and/or other fields.
			program.setLastModified(lastModified);
			String oldTitle = program.getProgramTitle();
			if ((oldTitle == null && parts[1].length() > 0) || (oldTitle != null && !oldTitle.equals(parts[1]))) {
				program.setLastModified(lastModified);
				
			}

			program.setProgramTitle(parts[1]);
			program.setReducedTitle70(parts[2]);
			program.setReducedTitle40(parts[3]);
			program.setReducedTitle20(parts[4]);
			program.setReducedTitle10(parts[5]);
			program.setAltTitle(parts[6]);
			program.setReducedDescription120(parts[7]);
			program.setReducedDescription60(parts[8]);
			program.setReducedDescription40(parts[9]);
			program.setAdultSituationsAdvisory(parts[10]);
			program.setGraphicLanguageAdvisory(parts[11]);
			program.setBriefNudityAdvisory(parts[12]);
			program.setGraphicViolenceAdvisory(parts[13]);
			program.setSscAdvisory(parts[14]);
			program.setRapeAdvisory(parts[15]);
			
			//TODO need to address Genre
			StringBuilder genre = new StringBuilder();
			for (int g = 136; g < 142; g++) {
				if (parts[g].length() >0) {
					if (genre.length() > 0) {
						genre.append(", ");
					}
					genre.append(parts[g]);
					
				}
			}
			String genreDescription = genre.toString();
			if (genreDescription.length() > 99) {
				genreDescription = genreDescription.substring(0, 100);
			}
			program.setGenreDescription(genreDescription);
			String oldDescription = program.getDescription();
			if ((oldDescription == null && parts[142].length() > 0) || (oldDescription != null && !oldDescription.equals(parts[142]))) {
				program.setLastModified(lastModified);
				
			}
			program.setDescription(parts[142]);
			if (parts[143].length() > 0) {
				try {
					program.setYear(dateFormat.parse(parts[143]));
				} catch (ParseException e) {
					log.error("Parse exception", e);
				}				
			}
			program.setMpaaRating(parts[144]);
			String rating = parts[145];
			if (rating.length() > 0) {
				if (rating.indexOf("+") > 	0) {
					program.setStarRating((float)rating.length() - .5f);
				} else {
					program.setStarRating(rating.length());
				}
			}
			String runtime = parts[146];
			if (runtime.length() == 4) {
				int hours = Integer.parseInt(runtime.substring(0,2));
				int minutes = Integer.parseInt(runtime.substring(2,4));
				program.setRunTime(hours*60+minutes);
			} else if (runtime.length() == 2) {
				program.setRunTime(Integer.parseInt(runtime));
				
			}
			program.setColorCode(parts[147]);
			program.setProgramLanguage(parts[149]);
			program.setOrgCountry(parts[149]);
			program.setMadeForTv(parts[150].length() > 0 ? parts[150].equalsIgnoreCase("Y") : false);
			program.setSourceType(parts[151]);
			program.setShowType(parts[152]);
			program.setHoliday(parts[153]);
			program.setSynEpiNum(parts[154]);
			program.setAltSynEpiNum(parts[155]);
			program.setEpisodeTitle(parts[156]);
			program.setNetSynSource(parts[157]);
			program.setNetSynType(parts[158]);
			program.setDescriptionActors(parts[159]);
			program.setReducedDescriptionActors(parts[160]);
			program.setOrgStudio(parts[161]);
			if (parts[162].length() > 0) {
				try {
					program.setGameDate(yearMonthDayFormat.parse(parts[162]));
				} catch (ParseException e) {
					log.error("Parse exception", e);
				}
			}
			if (parts[163].length() > 0) {
				try {
					program.setGameTime(hourMinutesFormat.parse(parts[163]));
				} catch (ParseException e) {
					log.error("Parse exception", e);
				}
			}
			program.setGameTimeZone(parts[164]);
			if (parts[165].length() > 0) {
				try {
					program.setOrginalAirDate(yearMonthDayFormat.parse(parts[165]));
				} catch (ParseException e) {
					log.error("Parse exception", e);
				}
			}		
			program.setUniqueId(parts[166]);

			line = reader.readLine();
		}
		reader.close();	
	}
	
	private void parseCredits(Program program, String[] parts, HashMap<Long, Credit> credits) {
		//Process actors
		try {
			for (int i = 16; i < 74; i = i+3) {
				String firstName = parts[i];				
				String lastName = parts[i+1];
				String description = parts[i+2];
				if (firstName.length() + lastName.length() > 0) {
					//PENDING(CE): Need to check if the credit for the actor exists, but
					//This was causing problems.  Need to revist this.
					Credit credit = new Credit();
					credit.setType(CreditType.ACTOR);
					credit.setFirstName(firstName);
					credit.setLastName(lastName);
					credit.setRoleDescription(description);
					credit.insert();
					credits.put(credit.getId(), credit);
					credit.addProgram(program);
				}
			}
			
			//Process Credits
			for (int i = 76; i <= 133; i = i+3) {
				String firstName = parts[i];
				String lastName = parts[i+1];
				String description = parts[i+2];
				if (firstName.length() + lastName.length() > 0) {
					//PENDING(CE): Need to check if the credit for the actor exists, but
					//This was causing problems.  Need to revist this.
					Credit credit = new Credit();				
					credit.setType(CreditType.PROGRAMMING_CREDIT);
					credit.setFirstName(firstName);
					credit.setLastName(lastName);
					credit.setRoleDescription(description);
					credit.insert();
					credits.put(credit.getId(), credit);
					credit.addProgram(program);
				}
			}
		} catch (Exception e) {
			log.error("Exception", e);
		}
	}
	
	/**
	 * Field # Field Name Min Max Field Description Field Example 
	 * 1 tf_station_num 1 10 Unique station ID number. 11259 
	 * 2 tf_database_key[3] 12 12 Unique description identifier necessary to reference movies, shows, episodes, sports from description file. MV1234560000; SH0123450000 
	 * 3 tf_air_date 8 8 Date the program airs based on a 12:00 AM start of day; yyyymmdd format. 19950721 
	 * 4 tf_air_time 4 4 Time of day the program airs; hhmm military format. 0000 is midnight 
	 * 5 tf_duration 4 Calculated by subtracting the current program's air time from the subsequent program's air time; hhmm format. 0059 is fifty-nine minutes; 0125 is one hour and twenty five minutes 
	 * 6 tf_part_num 3 Designates which part, when a program is split into 2 or more viewings. 1 
	 * 7 tf_num_of_parts 3 Designates when a program is split into 2 or more parts for viewing. 3 
	 * 8 tf_cc 1 Closed Captioning: Spoken content of program is listed on screen for the hearing-impaired. Y or N 
	 * 9 tf_stereo 1 Value designates whether a show, episode, movie or sports event is being broadcast in stereo. Y or N 
	 * 10 tf_repeat 1 Designates a program which has aired previously. Y or N 
	 * 11 tf_live_tape_delay 5 Designates whether a sports event is being played live, same-day delay, or taped prior to the air date. Live 
	 * 12 tf_subtitled 1 Used for foreign movies and shows, if the audio is in a foreign language, the English translation appears on-screen. Y or N 
	 * 13 tf_premiere_finale 15 Designates a program's premiere or finale, if applicable. Season Premiere 
	 * 14 tf_joined_in_progress 1 Joined in progress identifies when a station begins airing a program after the official start time. Y or N 
	 * 15 tf_cable_in_the_classroom 1 Designates a show is available through the Cable in the Classroom program. Y or N 
	 * 16 tf_tv_rating 4 TV Parental Guidelines in text form. TV13 
	 * 17 tf_sap 1 Designates whether the program is subject to Secondary Audio Program coding. Y or N 
	 * 18 tf_blackout 1 Designates whether the program is subject to blackout restrictions. Y or N 
	 * 19 tf_sex_rating 1 Indicates adult situations. Y or N 
	 * 20 tf_violence_rating 1 Indicates violent situations. Y or N 
	 * 21 tf_language_rating 1 Indicates strong language. Y or N 
	 * 22 tf_dialog_rating 1 Indicates strong dialogue. Y or N 
	 * 23 tf_fv_rating 1 Indicates fantasy violence. Y or N 
	 * 24 tf_enhanced 1 Designates enhanced program information. Y or N 
	 * 25 tf_three_d 1 Designates show is in 3-D. Y or N 
	 * 26 tf_letterbox 1 Designates program is a letterbox version. Y or N 
	 * 27 tf_hdtv 1 Designates whether a show is broadcast in High Definition TV. Y or N 
	 * 28 tf_dolby 5 Designates a program in Dolby or Dolby Digital. Dolby, DD 
	 * 29 tf_dvs 1 Designates a program with Descriptive Video Service. Y or N 
	 * 30 - 40 tf_user_data reserved
	 * @param stream
	 * @throws IOException
	 */
	private void parseSchedule(InputStream stream) throws IOException  {
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		//Even though we download it on the 27, the data is really from the 26 forward, so back 
		//up one day as so not to have duplicate schedule entries.
		cal.add(Calendar.DATE, -1);
		log.debug("Deleting schedules after : " + cal.getTime());
		int previousCount = Schedule.deleteAfter(cal.getTime());
		log.debug("Deleted : "+ previousCount + " schedules");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = reader.readLine();
		SimpleDateFormat  dateTimeFormat = new SimpleDateFormat("yyyyMMddHHmm");
		dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		int count = 1;
		while (line != null) {
//			if (count++ % scheduleTransaction == 0) {
//				TransactionManager.commitTransaction();				
//				TransactionManager.beginTransaction();			
//			}
			String[] parts = line.split("\\|", -1);
			long stationId = Long.parseLong(parts[0]);
			Date airTime = null;
			try {
				airTime = dateTimeFormat.parse(parts[2]+parts[3]);
			} catch (ParseException e) {
				log.error("Error parsing airtime for :" + line);
			}
			Station station = Station.selectById(stationId);
			Program program = Program.selectById(parts[1]);
			if (station != null && airTime != null && program != null) {
//				Schedule schedule = Schedule.selectByTimeAndStation(airTime, stationId);
//				if (schedule != null) {
//					//There was a scheduled item already, so we should delete it
//					schedule.delete();
//				}
				Schedule schedule = new Schedule();			
//				schedule.setId(station.getId()+":"+airTime);
//				schedule.setStation(station);
				schedule.setProgram(program);
				schedule.setAirTime(airTime);
				schedule.setLineupId("FOOBAR");
				schedule.insert();
				String duration = parts[4];
				int durationInMinutes = 0;
				if (duration.length() == 4) {
					int hours = Integer.parseInt(duration.substring(0,2));
					int minutes = Integer.parseInt(duration.substring(2,4));
					durationInMinutes = hours*60+minutes;
					schedule.setDuration(durationInMinutes);
				}
				//End time is calculated, but we need it for searching
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(airTime);
				calendar.add(Calendar.MINUTE, durationInMinutes);
				schedule.setEndTime(calendar.getTime());
				if (parts[5].length() > 0) {
					schedule.setPartNumber(Integer.parseInt(parts[5]));
				}
				if (parts[6].length() > 0) {
					schedule.setNumberOfParts(Integer.parseInt(parts[6]));
				}

  			    schedule.setCc(parts[7] == null ? false : parts[7].equalsIgnoreCase("Y"));
				schedule.setStereo(parts[8] == null ? false : parts[8].equalsIgnoreCase("Y"));
				//Repeat is gone, now has new
//				schedule.setRepeat(parts[9] == null ? false : parts[9].equalsIgnoreCase("Y"));				
				schedule.setLiveTapeDelay(parts[10]);
				schedule.setSubtitled(parts[11] == null ? false : parts[11].equalsIgnoreCase("Y"));
				schedule.setPremiereFinale(parts[12]);
				schedule.setJoinedInProgress(parts[13] == null ? false : parts[13].equalsIgnoreCase("Y"));
				schedule.setCableInClassroom(parts[14] == null ? false : parts[14].equalsIgnoreCase("Y"));
				schedule.setTvRating(parts[15]);
				schedule.setSap(parts[16] == null ? false : parts[16].equalsIgnoreCase("Y"));
				//Blackout is gone
//				schedule.setBlackout(parts[17] == null ? false : parts[17].equalsIgnoreCase("Y"));
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
				schedule.setNewEpisode(parts[30] == null ? false : parts[30].equalsIgnoreCase("New"));
			}
			line = reader.readLine();
		}
		reader.close();		
	}
	
 }
