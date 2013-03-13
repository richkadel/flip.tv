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

package com.appeligo.channelfeed.work;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.Network;
import com.knowbout.epg.service.Program;
import com.knowbout.epg.service.ScheduledProgram;

public class NullEPGProvider  implements EPGProvider {

	public List<ScheduledProgram> getAllScheduledPrograms(String arg0, Date arg1) {
		// TODO Auto-generated method stub
		return new LinkedList<ScheduledProgram>();
	}

	public ScheduledProgram getLastShowing(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public ScheduledProgram[] getLastShowingList(String arg0, List<String> arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getModifiedProgramIds(Date arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Network> getNetworks(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ScheduledProgram getNextShowing(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public ScheduledProgram[] getNextShowingList(String arg0, List<String> arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Program getProgram(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Program[] getProgramList(List<String> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ScheduledProgram> getScheduleForProgram(String arg0, String arg1, Date arg2, Date arg3) {
		// TODO Auto-generated method stub
		return new LinkedList<ScheduledProgram>();
	}

	public ScheduledProgram getScheduledProgram(String arg0, long arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public ScheduledProgram getScheduledProgramByNetworkCallSign(String arg0, String arg1, Date arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public ScheduledProgram getScheduledProgramByNetworkId(String arg0, long arg1, Date arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.knowbout.epg.service.EPGProvider#getNextShowingPrograms(java.util.List)
	 */
	public ScheduledProgram[] getNextShowingPrograms(List<String> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ScheduledProgram> getScheduleForTeam(String arg0, String arg1, String arg2, Date arg3, Date arg4, int arg5) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ScheduledProgram> getScheduleForAnyEpisode(String arg0, String arg1, Date arg2, Date arg3, int arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	public Program getShowByProgramId(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Program getProgramForTeam(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public ScheduledProgram getNextShowing(String arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ScheduledProgram> getNextShowings(String arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public ScheduledProgram getNextShowing(String arg0, String arg1, boolean arg2, boolean arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ScheduledProgram> getNextShowings(String arg0, String arg1, boolean arg2, boolean arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	public List<ScheduledProgram> getNextShowings(String arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return null;
	}
	*/
	
	
}