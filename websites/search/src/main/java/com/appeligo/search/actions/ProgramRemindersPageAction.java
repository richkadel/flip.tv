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

package com.appeligo.search.actions;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.appeligo.alerts.AlertManager;
import com.appeligo.alerts.KeywordAlert;
import com.appeligo.alerts.ProgramAlert;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.Program;
import com.knowbout.epg.service.ScheduledProgram;

public class ProgramRemindersPageAction extends BaseAction {
	
    private static final long serialVersionUID = 7019890080969775786L;
    
	private static final Log log = LogFactory.getLog(ProgramRemindersPageAction.class);
	
    private List<ProgramAlert> programAlerts;
    private List<ScheduledProgram> nextAiringList;
    private long existingProgramAlertId;
    private String alertsPage;
    private String message;
    private String valid;

    public String execute() throws Exception {
    	
    	//sort programs alphabetically and by programId
    	programAlerts = new LinkedList<ProgramAlert>(getUser().getLiveProgramAlerts());
    	Collections.sort(programAlerts,
			new Comparator<ProgramAlert>() {
				public int compare(ProgramAlert left, ProgramAlert right) {
					String leftStr = left.getProgram().getLabel();
					String rightStr = right.getProgram().getLabel();
					int rtn = leftStr.compareTo(rightStr);
					if (rtn == 0 && left.getProgramId() != null && right.getProgramId() != null) {
						rtn = left.getProgramId().compareTo(right.getProgramId());
					}
					return rtn;
				}
	    	}
    	);
    	
    	//ensure that programAlerts on matching programs are clustered together
    	LinkedList<ProgramAlert> newlist = new LinkedList<ProgramAlert>();
    	while (programAlerts.size() > 0) {
    		ProgramAlert alert = programAlerts.remove(0);
    		newlist.add(alert);
    		String targetId = alert.getProgramId();
    		int j = 0;
    		while (j < programAlerts.size()) {
	    		if (programAlerts.get(j).getProgramId().equals(targetId)) {
	    			newlist.add(programAlerts.remove(j));
	    		} else {
	    			j++;
	    		}
    		}
    	}
    	programAlerts = newlist;
    	
    	AlertManager alertManager = AlertManager.getInstance();
    	EPGProvider epgProvider = alertManager.getEpg();
		String lineup = getUser().getLineupId();
    		
    	nextAiringList = new LinkedList<ScheduledProgram>();
		String previousTargetId = null;
		for (ProgramAlert programAlert : programAlerts) {
			String targetId = programAlert.getProgramId();
			if (!targetId.equals(previousTargetId)) {
				ScheduledProgram nextAiring = null;
				if (programAlert.isNewEpisodes()) {
    				nextAiring = epgProvider.getNextShowing(lineup, programAlert.getProgramId(), true, true);
				}
				if (nextAiring == null) {
    				nextAiring = epgProvider.getNextShowing(lineup, programAlert.getProgramId(), false, true);
				}
				nextAiringList.add(nextAiring);
				previousTargetId = targetId;
			} else {
				nextAiringList.add(null);
			}
		}
        
        return SUCCESS;
    }

	public long getExistingProgramAlertId() {
		return existingProgramAlertId;
	}

	public void setExistingProgramAlertId(long existingProgramAlertId) {
		this.existingProgramAlertId = existingProgramAlertId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<ProgramAlert> getProgramAlerts() {
		return programAlerts;
	}

	public void setProgramAlerts(List<ProgramAlert> programAlerts) {
		this.programAlerts = programAlerts;
	}

	public String getValid() {
		return valid;
	}

	public void setValid(String valid) {
		this.valid = valid;
	}

	@Override
	public String getFullRequestURL() {
		return super.getFullRequestURL().replaceFirst("&message=.*$", "");
	}

	public String getAlertsPage() {
		return alertsPage;
	}

	public void setAlertsPage(String alertsPage) {
		this.alertsPage = alertsPage;
	}

	public List<ScheduledProgram> getNextAiringList() {
		return nextAiringList;
	}
}
