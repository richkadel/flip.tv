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

package com.knowbout.cc4j;

import static com.knowbout.cc4j.VideoStandard.*;

/**
 * Manages a list of several frequency standards corresponding to a VideoStandard
 * and FrequencyStandard (frequency table).
 * @author Rich Kadel
 * @author $Author: kadel $
 * @version $Rev: 114 $ $Date: 2006-08-02 18:33:16 -0700 (Wed, 02 Aug 2006) $
 */
public enum FrequencyStandard {

    US_BROADCAST(NTSC_M),
    US_CABLE(NTSC_M),
    US_CABLE_HRC(NTSC_M),
    US_CABLE_IRC(NTSC_M),
    JAPAN_BROADCAST(NTSC_M_JP),
    JAPAN_CABLE(NTSC_M_JP),
    EUROPE_WEST(PAL_G), // UHF only (VHF is PAL_B)
    EUROPE_EAST(SECAM_K), // UHF only (VHF is SECAM_D)
    ITALY(PAL_G), // UHF only (VHF if PAL_B)
    NEWZEALAND(PAL_G), // UHF only (VHF is PAL_B)
    AUSTRALIA(PAL_G), // Not sure if OPTUS is also PAL_G
    								// but also note that this is UHF only.
    								// Some areas of Australia use VHF and PAL_B
    AUSTRALIA_OPTUS(PAL_G),
    IRELAND(PAL_I),
    FRANCE(SECAM_L),
    CHINA_BROADCAST(PAL_D),
    SOUTHAFRICA_BROADCAST(PAL_I),
    ARGENTINA_BROADCAST(PAL_N), // For the future: Cable is PAL_Nc
    ;
    
    private VideoStandard videoStandard;
    
    private FrequencyStandard(VideoStandard videoStandard) {
    	this.videoStandard = videoStandard;
    }
    
    /**
     * @return the VideoStandard for the current FrequencyStandard
     */
    public VideoStandard getVideoStandard() {
    	return videoStandard;
    }
}