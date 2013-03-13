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

package com.appeligo.captions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import com.appeligo.lucene.LuceneIndexer;
import com.appeligo.search.util.ConfigUtils;
import com.appeligo.util.Utils;

class DeleteOldProgramsThread extends Thread {

	private static final Logger log = Logger.getLogger(DeleteOldProgramsThread.class);
	
	private static DeleteOldProgramsThread thread;
	
	private String liveIndexLocation;
	private String liveLineup;
	
	private DeleteOldProgramsThread() {
		Configuration config = ConfigUtils.getSystemConfig();
		liveIndexLocation = config.getString("luceneLiveIndex");
		liveLineup = config.getString("liveLineup");
	}
	
	public synchronized static void startThread() {
		if (thread == null) {
			thread = new DeleteOldProgramsThread();
			thread.setDaemon(true);
			thread.start();
		}
	}
	
	@Override
	public void run() {
		
		while (true) {
    		LuceneIndexer liveIndex = LuceneIndexer.getInstance(liveIndexLocation);
			Calendar wayback = Calendar.getInstance();
			wayback.add(Calendar.MONTH, -3);
			Calendar tenminutesago = Calendar.getInstance();
			tenminutesago.add(Calendar.MINUTE, -10);
			log.info("Deleting old programs from live index, between "+wayback.getTime()+" and "+tenminutesago.getTime());
			String dateField = "lineup-"+liveLineup+"-endTime";
			ConstantScoreRangeQuery  dateQuery =
				new ConstantScoreRangeQuery(
						dateField,
						DateTools.dateToString(wayback.getTime(), DateTools.Resolution.MINUTE),
						DateTools.dateToString(tenminutesago.getTime(), DateTools.Resolution.MINUTE),
						true, true);
			IndexSearcher searcher = null;
			try {
				searcher = new IndexSearcher(liveIndexLocation);
				Hits hits = searcher.search(dateQuery);
				Set<Term> terms = new HashSet<Term>();
				if (hits.length() > 0) {
					for (int index = 0; index < hits.length(); index++) {
						Document doc = hits.doc(index);
						Term term = new Term(dateField, doc.get(dateField));
						terms.add(term);										
					}
				}
				liveIndex.deleteDocuments(terms.toArray(new Term[terms.size()]));
			} catch (IOException e) {
				log.error("Error deleting old programs from live index", e);
			} finally {
				if (searcher != null) {
					try {
						searcher.close();
					} catch (IOException e) {
						log.error("Error closing searcher when deleting old programs from live index", e);
						
					}
				}
			}
        		
    		Calendar cal = Calendar.getInstance();
    		int minute = cal.get(Calendar.MINUTE);
    		if (minute < 15) {
    			cal.set(Calendar.MINUTE, 15);
    		} else if (minute >= 45) {
    			cal.set(Calendar.MINUTE, 15);
    			cal.add(Calendar.HOUR, 1);
    		} else {
    			cal.set(Calendar.MINUTE, 45);
    		}
			log.info("queued up that delete, now we're waiting until "+cal.getTime());
			Utils.sleepUntil(cal.getTimeInMillis());
		}
	}
}
