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

package com.appeligo.epg.util;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;
import org.apache.log4j.Logger;

import com.appeligo.util.Utils;

import com.knowbout.epg.service.Program;
import com.knowbout.epg.service.ProgramType;

public class EpgUtils {
	
	private static final Logger log = Logger.getLogger(EpgUtils.class);
	
	private static final int DEFAULT_MIN_MUNGED_TITLE_LENGTH = 1;
	
	private EpgUtils() {
    }

	/**
	 * Determine the users lineup based on his timzone and station lineup (Cable, Digital Cable, or Satellite)
	 * @param timeZone
	 * @param stationLineup
	 * @return
	 */
	public static String determineLineup(TimeZone timeZone, int stationLineup) {
		StringBuilder lineup = new StringBuilder();
		int timeZoneOffsetHours = timeZone.getRawOffset() / (60 * 60 * 1000);
		if (timeZoneOffsetHours == -10) {
			lineup.append("H");
		} else if (timeZoneOffsetHours == -9) {
			lineup.append("P");
		} else if (timeZoneOffsetHours == -7) {
			lineup.append("M");
		} else if (timeZoneOffsetHours == -6 || timeZoneOffsetHours == -5) {
			lineup.append("E");
		} else {
			lineup.append("P");
		}
		lineup.append("-");
		//Make it 0 based index;
		stationLineup--;
		StationLineup sl = null;
		if (stationLineup < 0 || stationLineup > 2 ) {
			sl = StationLineup.DIGITAL_CABLE;
		} else {
			sl = StationLineup.values()[stationLineup];			
		}
		switch (sl) {
			case CABLE: { 
				lineup.append("C"); 
				break;
			}
			case DIGITAL_CABLE: { 
				lineup.append("DC"); 
				break;
			}
			case SATELLITE: { 
				lineup.append("S"); 
				break;
			}
			default: {
				lineup.append("DC");
			}
		}
		return lineup.toString();
		
	}
	
	/**
	 * @param timestamp
	 */
	public static String getCaptionFilePrefix(String ccDocumentRoot, String lineupID, String callsign, long timestamp) {
		if (!ccDocumentRoot.endsWith("/")) {
			ccDocumentRoot += "/";
		}
		String path = ccDocumentRoot+lineupID+"/"+Utils.getDatePath(timestamp)+"/"+callsign+"/";
		File f = new File(path);
		if (!f.exists()) {
			if (!f.mkdirs()) {
	        	log.warn("Couldn't create directory path "+path+". Trying again...");
				if (!f.mkdirs()) {
		        	log.error("Couldn't create directory path "+path+ " after 2 tries");
				} else {
					log.warn("...Worked the second time...for some reason.");
				}
			}
		}
		return (path+timestamp);
	}

	public static String getPreviewDir(String root, Program program) {
		String mungedTitle = cleanUrlTitle(program.getProgramTitle(), 3);
		
		mungedTitle = mungedTitle.replaceAll("-", "");
		mungedTitle = mungedTitle.toUpperCase();
		
		char ch0 = mungedTitle.charAt(0);
		char ch1 = mungedTitle.charAt(1);
		char ch2 = mungedTitle.charAt(2);
		
		String dirName = root+"/primary/"+ProgramType.fromProgram(program)+"/"+ch0+"/"+ch1+"/"+ch2+"/"+mungedTitle;
		return dirName;
	}

	public static String getPreviewFilePrefix(String root, Program program) {
		return getPreviewDir(root, program)+"/"+program.getProgramId();
	}

	public static String getLineupID(String headendID, String lineupDevice) {
		return headendID+"-"+lineupDevice;
	}

	public static String cleanUrlTitle(String programTitle) {
		return cleanUrlTitle(programTitle, DEFAULT_MIN_MUNGED_TITLE_LENGTH);
	}
	
	public static String cleanUrlTitle(String programTitle, int minLength) {
		String mungedTitle = programTitle.trim().toLowerCase();
		if (mungedTitle.startsWith("the ")) {
			mungedTitle = mungedTitle.substring(4);
		} else if (mungedTitle.startsWith("a ")) {
			mungedTitle = mungedTitle.substring(2);
		}
		mungedTitle = mungedTitle.replaceAll(" +& +", " and ");
		mungedTitle = mungedTitle.replaceAll("[^\\w ]", "");
		mungedTitle = mungedTitle.replaceAll(" ", "-");
		while (mungedTitle.length() < minLength) {
			mungedTitle += "_";
		}
		return mungedTitle;
	}
}
