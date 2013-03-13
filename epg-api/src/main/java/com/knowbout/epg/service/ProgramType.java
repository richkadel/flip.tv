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

public enum ProgramType {
	EPISODE,
	MOVIE,
	SHOW,
	SPORTS,
	TEAM;
	
	public static ProgramType fromProgram(Program program) {
		return fromProgramID(program.getProgramId());
	}

	public static ProgramType fromProgramID(String programId) {
		if (programId.startsWith("EP")) {
			return EPISODE;
		} else if (programId.startsWith("MV")) {
			return MOVIE;
		} else if (programId.startsWith("SH")) {
			return SHOW;
		} else if (programId.startsWith("SP")) {
			return SPORTS;
		} else if (programId.startsWith("TE")) {
			return TEAM;
		}
		throw new Error("Invalid Program Id");
	}

	public String abbreviation() {
		switch(this) {
		case EPISODE:
			return "EP";
		case MOVIE:
			return "MV";
		case SHOW:
			return "SH";
		case SPORTS:
			return "SP";
		case TEAM:
			return "TE";
		}
		throw new Error("Impossible value");
	}
}
