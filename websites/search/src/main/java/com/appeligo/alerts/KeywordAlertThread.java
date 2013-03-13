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

package com.appeligo.alerts;

import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.hibernate.Transaction;

import com.appeligo.lucene.LuceneIndexer;
import com.appeligo.lucene.PorterStemAnalyzer;
import com.appeligo.search.actions.SearchResults;
import com.appeligo.search.actions.SearchType;
import com.appeligo.search.entity.Permissions;
import com.appeligo.search.entity.User;
import com.appeligo.search.util.ChunkedResults;
import com.appeligo.search.util.TermFormatter;
import com.knowbout.hibernate.HibernateUtil;

class KeywordAlertThread extends Thread {
	
	private static final Log log = LogFactory.getLog(KeywordAlertThread.class);

	private static final long DEFAULT_SHORTEST_TIME_BETWEEN_QUERIES_MS = 2 * 60 * 1000;
	
	private String liveIndexDir;
	private String liveLineup;
	private IndexSearcher currentSearcher;
	private long shortestTimeBetweenQueriesMs;
	private int keywordAlertProximity;

	private QueryParser parser;

	private long currentSearcherCreated;

	private KeywordAlertChecker helper;
	private AlertManager alertManager;
	private boolean isActive;
	private int maxConsecutiveExceptions;

	public KeywordAlertThread(Configuration config) throws IOException {
		super("KeywordAlertThread");
		isActive = true;
		alertManager = AlertManager.getInstance();
        liveIndexDir = config.getString("luceneLiveIndex");
        liveLineup = config.getString("liveLineup");
        maxConsecutiveExceptions = config.getInt("maxConsecutiveExceptions", 10);
		shortestTimeBetweenQueriesMs = config.getLong("shortestTimeBetweenQueriesMs", DEFAULT_SHORTEST_TIME_BETWEEN_QUERIES_MS);
        keywordAlertProximity = config.getInt("keywordAlertProximity", 10);
        if (!IndexReader.indexExists(liveIndexDir)) {
        	log.error("Lucene Live Index is missing or invalid at "+liveIndexDir+". Trying anyway in case this gets resolved.");
        }
        parser = new QueryParser("text", new PorterStemAnalyzer(LuceneIndexer.STOP_WORDS));
		parser.setDefaultOperator(Operator.AND);
		helper = new KeywordAlertChecker(config);
	}
	
	private String proximitize(String normalizedQuery) {
		if ((normalizedQuery.indexOf('"') < 0) && (normalizedQuery.indexOf('\'') < 0) && (normalizedQuery.indexOf(' ') >= 0)) {
			return '"'+normalizedQuery+"\"~"+keywordAlertProximity; 
				// proximity query...find all words within 5 words distance
		} else {
			return normalizedQuery;
		}
	}
	
	public void run() {
		try {
			Permissions.setCurrentUser(Permissions.SUPERUSER);
				
			while (isActive()) {
				
				long startSearch = System.currentTimeMillis();
				
				HibernateUtil.openSession();
	    		try {
					boolean maxExceeded = executeKeywordSearch(
						new SearchExecutor() {
							
							Query luceneQuery;
							
							public Hits search(String lineupId, String normalizedQuery) throws ParseException, IOException {
								IndexSearcher searcher = getIndexSearcher();
								if (log.isDebugEnabled()) log.debug("Searching live index for "+proximitize(normalizedQuery));
								luceneQuery = parser.parse(proximitize(normalizedQuery));
					            return searcher.search(luceneQuery);
							}
							
							public Query getLuceneQuery() {
								return luceneQuery;
							}
						},
						"keyword_alert", true);
					if (maxExceeded) {
						log.fatal("Reached max consecutive exceptions for KeywordAlertThread. Cancelling current keyword check and waiting for next one.");
					} else {
					
						if (alertManager.isEpgUpdated()) {
							if (log.isDebugEnabled())log.debug("KeywordAlertThread has to check against the EPG");
							try {
								checkAgainstEpg();
							} catch (IOException e) {
								log.error("IOException checking keywords against the EPG", e);
							}
							alertManager.setEpgUpdated(false);
						}
						
						// Now that we aren't looping on KeywordAlerts, I should be able to safely
						// delete all of the KeywordAlerts flagged as deleted without screwing up
						// paging above
						if (log.isDebugEnabled())log.debug("KeywordAlertThread done checking all.  Now deleting all marked deleted, and old program matches");
						Transaction transaction = HibernateUtil.currentSession().beginTransaction();
						try {
							log.debug("deleting marked-deleted keyword alerts");
							KeywordAlert.deleteAllMarkedDeleted();
							log.debug("deleting old program matches");
							KeywordMatch.deleteOldProgramMatches(); // using current time vs. program end time
							log.debug("deleted marked-deleted keyword alerts and program matches");
						} finally {
							transaction.commit();
						}
					}
					
				} finally {
					HibernateUtil.closeSession();
				}
				long delay = (startSearch + shortestTimeBetweenQueriesMs) - System.currentTimeMillis();
				while ((delay > 0) && isActive()) {
					try {
						if (log.isDebugEnabled())log.debug("KeywordAlertThread waiting for "+delay+" ms");
						synchronized(this) {
							wait(delay);
						}
					} catch (InterruptedException e) {
					}
					delay = (startSearch + shortestTimeBetweenQueriesMs) - System.currentTimeMillis();
				}
			}
		} catch (Throwable t) {
			log.fatal("Caught unexpected exception, causing abort of thread!", t);
		}
	}
	
	private IndexSearcher getIndexSearcher() throws IOException {
		if ((currentSearcher != null) &&
			((System.currentTimeMillis() - currentSearcherCreated)
					> shortestTimeBetweenQueriesMs)) {
			currentSearcher.close();
			currentSearcher = null;
		}
		if (currentSearcher == null) {
			if (log.isDebugEnabled())log.debug("KeywordAlertThread time to get a new searcher");
			currentSearcherCreated = System.currentTimeMillis();
	        currentSearcher = new IndexSearcher(liveIndexDir);
		}
		return currentSearcher;
	}
	
	private void checkAgainstEpg() throws IOException {
		
		final SearchResults searchResults = new SearchResults(
				alertManager.getLuceneIndex(), null,
				10, null);
		searchResults.setSearchType(SearchType.FUTURE);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -20); // Allowing for an abnormally long 4 hour EPG index update should be safe enough
		Date yesterday = cal.getTime();
		searchResults.setModifiedSince(yesterday);
		final IndexSearcher searcher = searchResults.newIndexSearcher();
		try {
			boolean maxExceeded = executeKeywordSearch(
				new SearchExecutor() {
					
					Query luceneQuery;
					
					public Hits search(String lineupId, String normalizedQuery) throws ParseException, IOException {
			    		searchResults.setQuery(proximitize(normalizedQuery));
			    		searchResults.setLineup(lineupId);
			    		luceneQuery = searchResults.generateLuceneQuery(searcher);
			    		//Required to use the highlighter
			    		luceneQuery = searcher.rewrite(luceneQuery);
			    		return searcher.search(luceneQuery);
					}
					
					public Query getLuceneQuery() {
						return luceneQuery;
					}
				},
				"keyword_alert_epg", false);
			if (maxExceeded) {
				log.fatal("Reached max consecutive exceptions for KeywordAlertThread. Exiting thread immediately.");
				return;
			}
		} finally {
			searcher.close();
    	}
		if (log.isDebugEnabled())log.debug("KeywordAlertThread done with check of KeywordAlerts against EPG data");
	}

	/**
	 * @param searchExecutor callback to get the set of hits for the given query. This can be
	 * executed in different ways.
	 * @return true if we hit too many consecutive exceptions so we broke out of the loop
	 */
	private boolean executeKeywordSearch(SearchExecutor searchExecutor, String messagePrefix, boolean groupQueries) {
		ChunkedResults<KeywordAlert> results = KeywordAlert.getAllInNormalizedQueryOrder();
        Hits hits = null;
        String lastNormalizedQuery = null;
        Query lastLuceneQuery = null;
        int consecutiveExceptions = 0;
		results.beforeFirst();
		while (results.next() && isActive()) {
			KeywordAlert keywordAlert = results.get();
			try {
				if (keywordAlert.isDeleted() || keywordAlert.isDisabled()) {
					if (log.isDebugEnabled())log.debug("keyword alert is deleted or disabled");
					continue;
				}
				User user = keywordAlert.getUser();
				if (user == null) {
					if (log.isDebugEnabled())log.debug("keyword alert is implicitly deleted (user is null)");
					keywordAlert.setDeleted(true);
					keywordAlert.save();
					continue;
				}
    			
				if (helper.maxAlertsExceeded(keywordAlert)) {
					continue;
				}
				
				if (groupQueries) {
					if ((hits == null) || (!keywordAlert.getNormalizedQuery().equals(lastNormalizedQuery))) {
			    		hits = searchExecutor.search(null, keywordAlert.getNormalizedQuery());
			    		lastLuceneQuery = searchExecutor.getLuceneQuery();
					} else if (log.isDebugEnabled()) log.debug("Not searching on "+keywordAlert.getNormalizedQuery()+" again");
				} else {
		    		hits = searchExecutor.search(keywordAlert.getUser().getLineupId(), keywordAlert.getNormalizedQuery());
		    		// Note that I'm searching with the lineup from the user, which will
		    		// only ensure that the liveIndex doesn't return shows that don't ever
		    		// play for this lineup.  However, it does not guarantee that the show
		    		// on this user's lineup is playing at the same time (meaning alerts
		    		// might tell the user of a show that is only in the future).
		    		lastLuceneQuery = searchExecutor.getLuceneQuery();
				}
				lastNormalizedQuery = keywordAlert.getNormalizedQuery();
	        	Highlighter highlighter = new Highlighter(new TermFormatter(), new QueryScorer(lastLuceneQuery));
	        	PorterStemAnalyzer analyzer = new PorterStemAnalyzer(LuceneIndexer.STOP_WORDS);
				
	        	for (int i = 0; i < hits.length(); i++) {
	        		Document doc = hits.doc(i);
		        		
	        		if (!isActive()) {
	        			break;
	        		}
	        		
//	        		if (groupQueries && (!"true".equals(doc.get("lineup-"+keywordAlert.getUser().getLineupId())))) {
	        		if (groupQueries &&
    						(doc.get("lineup-"+keywordAlert.getUser().getLineupId()+"-startTime") == null)) {
	        			// This "if" statement checks to make sure the program is or did play on the user's
	        			// lineup, which might be on a different station, a different time, past or future.
						if (log.isDebugEnabled()) log.debug(doc.get("programTitle")+" matched on "+keywordAlert.getNormalizedQuery()+
								" but it isn't airing on this user's lineup anytime soon.");
	        			continue;
	        		}
		        		
	            	Transaction transaction = HibernateUtil.currentSession().beginTransaction();
		            try {
    					if ((!helper.maxAlertsExceeded(keywordAlert)) && helper.isNewMatch(keywordAlert, doc)) {
    						if (log.isDebugEnabled())log.debug("KeywordAlertThread found match in "+doc.get("programTitle")+" for "+keywordAlert.getNormalizedQuery()+
    								"... sending messages");
    						String text = doc.get("text");
    						String fragments = null;
    						if (text != null) {
    							TokenStream tokenStream = analyzer.tokenStream("text", new StringReader(text));
    							fragments = highlighter.getBestFragments(tokenStream, text,3, "...");
    						}
    						
    						helper.incrementTodaysAlertCount(keywordAlert);
    		            	helper.sendMessages(keywordAlert, fragments, doc, messagePrefix);
    					} else if (log.isDebugEnabled())log.debug("KeywordAlertThread found match in "+doc.get("programTitle")+" for "+keywordAlert.getNormalizedQuery()+" but max exceeded or we already matched this one");
					} catch (Throwable t) {
						log.error("Error processing keyword alerts when searching live lucene index. Rolling back transaction.", t);
						transaction.rollback();
					} finally {
						if (!transaction.wasRolledBack()) {
							transaction.commit();
						}
		        	}
				}
	        	consecutiveExceptions = 0;
			} catch (Throwable t) {
				User user = keywordAlert.getUser();
				log.error("Caught throwable on keyword "+keywordAlert.getId()+", "+keywordAlert.getUserQuery()+
						", user "+((user==null)?null:user.getUsername()), t);
				consecutiveExceptions++;
				if (consecutiveExceptions >= maxConsecutiveExceptions) {
					return true;
				}
			}
		}
    	return false;
	}
	
	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	private interface SearchExecutor {
		public Hits search(String lineupId, String normalizedQuery) throws ParseException, IOException;
		public Query getLuceneQuery();
	}
}
