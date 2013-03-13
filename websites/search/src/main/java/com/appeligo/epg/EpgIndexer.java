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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.appeligo.alerts.api.AlertQueue;
import com.appeligo.lucene.LuceneIndexer;
import com.appeligo.lucene.OptimizeAction;
import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.ScheduledProgram;

public class EpgIndexer {

	private static final Log log = LogFactory.getLog(EpgIndexer.class);
	
	private EPGProvider epg;
	private String programIndex;
	private String compositeIndex;
	private List<String> lineups;

	@SuppressWarnings("unchecked")
	public EpgIndexer(String indexLocation, String compositeIndexLocation, EPGProvider epg, List<String> lineups) {
		this.programIndex = indexLocation;
		this.compositeIndex = compositeIndexLocation;
		this.epg = epg;
		this.lineups = lineups;
	}
	
	public void updateEpgIndex(Date date) {		
		DefaultEpg.getInstance().clearCaches();
		log.info("Updating index " + programIndex + " starting at " + new Date()+  " processing programs updated after " + date);
		List<String> updatedPrograms = epg.getModifiedProgramIds(date);
		log.info("There are " + updatedPrograms.size() + " programs to index"); 
		processPrograms(updatedPrograms, date);
		log.info("Finished updating index.  Processed " + updatedPrograms.size() + " programs.  Done at " + new Date()); 
	}
	
	private void processPrograms(List<String> ids, Date modified) {
		int count = 0;
		int schedulesAdded = 0;
		LuceneIndexer indexer = LuceneIndexer.getInstance(programIndex);
		LuceneIndexer compositeIndexer = LuceneIndexer.getCompositeInstance(compositeIndex);
		while (count < ids.size()) {
			int subsetSize = (ids.size() < 100 ? ids.size() : 100);
			if (count % 1000 == 0) {
				log.info("Index programs into the Lucene Index. Current have processed " + count + " programs out of " + ids.size());
			}
			int endIndex = (count+subsetSize > ids.size() ? ids.size() : count+subsetSize);
			List<String> subset = ids.subList(count, endIndex);
			count +=subsetSize;
			HashMap<String, List<ScheduledProgram>> schedules = new HashMap<String, List<ScheduledProgram>>();
			ScheduledProgram[] programs = epg.getNextShowingPrograms(subset);
			for (ScheduledProgram program: programs) {
				if (program != null) {
					List<ScheduledProgram> schedule = schedules.get(program.getProgramId());
					if (schedule == null) {
						schedule = new ArrayList<ScheduledProgram>();
						schedules.put(program.getProgramId(), schedule);
					}
					schedule.add(program);
					schedulesAdded++;
				}
			}
			for(Entry<String, List<ScheduledProgram>> entry: schedules.entrySet()) {
				indexer.updateProgram(entry.getKey(), entry.getValue(), modified);
				compositeIndexer.updateCompositeProgram(entry.getKey(), entry.getValue(), modified);
			}
		}
		//Force an optimization after this large action.
		indexer.addAction(new OptimizeAction());
		compositeIndexer.addAction(new OptimizeAction());
		log.debug("added "+ schedulesAdded + " schedules across the lineups.");
	}

	public static void main(String[] args) throws Exception {
		HessianProxyFactory factory = new HessianProxyFactory();
		EPGProvider epg = (EPGProvider) factory.create(EPGProvider.class,"http://localhost/epg/channel.epg");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -5);
		cal.set(Calendar.DATE, 1);
		String[] lineups = new String[] {"SDTW-C", "P-C", "P-DC", "P-S","M-C", "M-DC", "M-S","E-C", "E-DC", "E-S","H-C", "H-DC", "H-S" };
		List<String> ids  = epg.getModifiedProgramIds(cal.getTime());  
		int count = 0;
		long average = 0;
		int counter = 0;
		int added = 0;
		while (count < ids.size()) {
			System.err.println("in loop: "+ counter + ", " + count +"," + ids.size());
			int subsetSize = (ids.size() < 100 ? ids.size() : 100);
			counter++;
			if (count % 1000 == 0) {
				log.debug("Index programs into the Lucene Index. Current have processed " + count + " programs out of " + ids.size());
			}			
			int endIndex = (count+subsetSize > ids.size() ? ids.size() : count+subsetSize);
			List<String> subset = ids.subList(count, endIndex);
			count +=subsetSize;
			
			long time = System.currentTimeMillis();
			HashMap<String, List<ScheduledProgram>> schedules = new HashMap<String, List<ScheduledProgram>>();
			for (String lineup: lineups) {
				ScheduledProgram[] programs = epg.getNextShowingList(lineup, subset);
				for (ScheduledProgram program: programs) {
					if (program != null) {
						List<ScheduledProgram> schedule = schedules.get(program.getProgramId());
						if (schedule == null) {
							schedule = new ArrayList<ScheduledProgram>();
							schedules.put(program.getProgramId(), schedule);
						}
						schedule.add(program);
						added++;
					}
				}
			}
			long after = System.currentTimeMillis();
			long diff = after - time;
			average += diff;
			System.err.println(diff + " - " + (average/counter) + " added: " + added);
		}
//		EpgIndexer indexer = new EpgIndexer(programIndex, epg, lineup);
//		Calendar cal = Calendar.getInstance();
//		cal.set(Calendar.DAY_OF_MONTH, 24);
//		cal.set(Calendar.HOUR_OF_DAY, 0);
//		indexer.updateEpgIndex(cal.getTime());
	}
	/*in loop: 0, 0,2499
11281 - 11281
in loop: 1, 100,2499
10828 - 11054
in loop: 2, 200,2499
10313 - 10807
in loop: 3, 300,2499
10343 - 10691
in loop: 4, 400,2499
10657 - 10684
in loop: 5, 500,2499
10203 - 10604
in loop: 6, 600,2499
9890 - 10502
in loop: 7, 700,2499
10235 - 10468
in loop: 8, 800,2499
9890 - 10404
in loop: 9, 900,2499
9735 - 10337
in loop: 10, 1000,2499
9968 - 10303
in loop: 11, 1100,2499
9719 - 10255
in loop: 12, 1200,2499
9735 - 10215
in loop: 13, 1300,2499
9984 - 10198
in loop: 14, 1400,2499
9656 - 10162
in loop: 15, 1500,2499
9672 - 10131
in loop: 16, 1600,2499
10125 - 10131
in loop: 17, 1700,2499
9641 - 10104
in loop: 18, 1800,2499
9734 - 10084
in loop: 19, 1900,2499
9953 - 10078
in loop: 20, 2000,2499
10016 - 10075
in loop: 21, 2100,2499
9937 - 10068
in loop: 22, 2200,2499
10860 - 10103
in loop: 23, 2300,2499
17250 - 10401
in loop: 24, 2400,2499
11937 - 10462
*/
}
