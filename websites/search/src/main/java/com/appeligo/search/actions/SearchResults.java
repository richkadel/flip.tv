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

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.store.FSDirectory;

import com.appeligo.epg.DefaultEpg;
import com.appeligo.lucene.DocumentWrapper;
import com.appeligo.lucene.LuceneIndexer;
import com.appeligo.lucene.PorterStemAnalyzer;
import com.appeligo.lucene.PorterStemmer;
import com.appeligo.search.util.ConfigUtils;
import com.appeligo.search.util.TermFormatter;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.Program;
import com.knowbout.epg.service.ScheduledProgram;

public class SearchResults implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5361446618088941069L;
	
	private static final Logger log = Logger.getLogger(SearchResults.class);
	private static Configuration config;
	private static DidYouMeanParser didYouMeanParser;
	private static String defaultField;
	private static int minimumHits;
	private static float minimumScore;
	//private static float relevanceMargin;
	private String suggestedQuery = null;
	private boolean usingSuggestedQuery;
	private static PorterStemAnalyzer analyzer;
	private static Map<String, Query> genreQueries;
	
	private String lineup;
	private String indexLocation;
	private String compositeIndexLocation;
	private String query;
	private SearchType searchType;
	private int totalHits;
	private ArrayList<SearchResult> results;
    private HashMap<String, SearchResult> programToSearchResult;
    private Collection<HitCount> genreCounts;
    private Collection<HitCount> programCounts;
    private Collection<HitCount> whatMatchedCounts;
	private int pageSize;
	private boolean hasMoreResults;
	private int lastDocumentIndex;
	private Date modifiedSince;
	
	/**
	 * @param headEnd
	 * @param epgProvider
	 * @param indexLocation
	 * @param pageSize
	 */
	public SearchResults(String indexLocation, String compositeIndexLocation, int pageSize, String lineup) {
		this.lineup = lineup;
		this.indexLocation = indexLocation;
		this.compositeIndexLocation = compositeIndexLocation;
		this.pageSize = pageSize;
        programToSearchResult = new HashMap<String, SearchResult>();
		
		initializeStatics();
	}
	
	/**
	 * This method has to be called sometime after deserialization as well as during initialization.
	 * Deserialization will not call the constructor.
	 */
	private void initializeStatics() {
        if (config == null) {
			config = ConfigUtils.getSystemConfig();
        }
        
		minimumHits = config.getInt("didYouMeanMinHits", 4);
		minimumScore = config.getFloat("didYouMeanMinScore", 1.0f);
		//relevanceMargin = config.getFloat("relevanceMargin", 0.10f);
		defaultField = "compositeField"; // the only field in the compositeIndex
		
		if (didYouMeanParser == null) {
			try {
				didYouMeanParser = new CompositeDidYouMeanParser(defaultField, FSDirectory.getDirectory(config.getString("spellIndex")));
			} catch (IOException e) {
				log.error("Error opening spell index" , e);
			}
		}
		analyzer = new PorterStemAnalyzer(LuceneIndexer.STOP_WORDS);
		genreQueries = new HashMap<String, Query>();
		String[] queries = config.getStringArray("genreQuery.genre");
		String[] labels = config.getStringArray("genreQuery.genre[@label]");
		PorterStemmer stemmer = new PorterStemmer();
		for (int i = 0; i < labels.length && i < labels.length; i++) {
			if (queries[i].indexOf(' ') > 0) {
				PhraseQuery phraseQuery = new PhraseQuery();
				String[] words = queries[i].split(" ");
				for(String word : words) {
					phraseQuery.add(new Term("genre", stemmer.stem(word)));
				}
    			genreQueries.put(labels[i], phraseQuery);
			} else {
    			genreQueries.put(labels[i], new TermQuery(new Term("genre", queries[i])));
			}
		}
	}

	public List<SearchResult> getSearchResults() {
		return getSearchResults(0);
	}
	
	public Query generateLuceneQuery(IndexSearcher searcher) throws ParseException {
		return generateLuceneQuery(query, searcher);
	}
	
	
	private Query getContentQuery(String givenQuery, IndexSearcher searcher) throws ParseException {
		return getFieldQuery(givenQuery, "text", searcher);
	}
	private Query getFieldQuery(String givenQuery, String field, IndexSearcher searcher) throws ParseException {
		if (givenQuery == null) {
			givenQuery = getQuery();
		}
        QueryParser parser = new QueryParser(field, analyzer);
    	parser.setDefaultOperator(Operator.AND);
    	Query luceneQuery;
    	try {
    		luceneQuery = parser.parse(givenQuery);		
    	} catch (ParseException e){
    		log.error("Error parsing query for : " +givenQuery);
    		if (log.isDebugEnabled()) {
    			log.debug("Error parsing query for : " +givenQuery, e);
    		}
			givenQuery = QueryParser.escape(givenQuery);
			luceneQuery = parser.parse(givenQuery);				    		
    	}
		return luceneQuery;
	}
	private Query generateLuceneQuery(String givenQuery, IndexSearcher searcher) throws ParseException {
		if (givenQuery == null) {
			givenQuery = getQuery();
		}
		HashMap<String, Float> boost = new HashMap<String, Float>();
		boost.put("programTitle", 8.0f);
		boost.put("episodeTitle", 3.0f);
		boost.put("description", 2.0f);
        MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"text","description","programTitle", "episodeTitle", "credits", "genre"},
        		analyzer, boost);
    	parser.setDefaultOperator(Operator.AND);
    	
    	
		Query luceneQuery = null;
		try {
			luceneQuery = parser.parse(givenQuery);				
		} catch (ParseException e) {
    		log.error("Error parsing query for : " +givenQuery);
    		if (log.isDebugEnabled()) {
    			log.debug("Error parsing query for : " +givenQuery, e);
    		}
			givenQuery = QueryParser.escape(givenQuery);
			luceneQuery = parser.parse(givenQuery);				
		}
		BooleanQuery combinedQuery = new BooleanQuery();
		combinedQuery.add(luceneQuery, Occur.MUST);
		
		//This will move into a setting on the user.
		Query tvma = new TermQuery(new Term("tvRating", "TVMA"));
		combinedQuery.add(tvma, Occur.MUST_NOT);

		if (lineup != null) {
			//Only find programs that were on networks that are in the lineup
			TermQuery  lineupQuery = new TermQuery(new Term("lineup-"+lineup, "true"));
			combinedQuery.add(lineupQuery, Occur.MUST);
		} 
		
		if (searchType != null) {
			if (lineup == null) {
				throw new ParseException("Lineup cannot be null if searching based on date.  searchType="+searchType);
			}
			switch (searchType) {
				case  FUTURE: {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DAY_OF_YEAR, 14);
					Date future = cal.getTime();
					Date now = new Date();				
					ConstantScoreRangeQuery  dateQuery = new ConstantScoreRangeQuery("lineup-"+lineup+"-endTime",DateTools.dateToString(now, DateTools.Resolution.MINUTE),DateTools.dateToString(future, DateTools.Resolution.MINUTE), true, true);
					combinedQuery.add(dateQuery, Occur.MUST);
					break;
				}
				case  TODAY: {
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY));
					cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE));
					Date future = cal.getTime();
					Date now = new Date();				
					ConstantScoreRangeQuery  dateQuery = new ConstantScoreRangeQuery("lineup-"+lineup+"-endTime",DateTools.dateToString(now, DateTools.Resolution.MINUTE),DateTools.dateToString(future, DateTools.Resolution.MINUTE), true, true);
					combinedQuery.add(dateQuery, Occur.MUST);
					break;
				}
			}
		}
		if (modifiedSince != null) {
			Date now = new Date();				
			ConstantScoreRangeQuery  dateQuery = new ConstantScoreRangeQuery("lastModified",DateTools.dateToString(modifiedSince, DateTools.Resolution.MINUTE),DateTools.dateToString(now, DateTools.Resolution.MINUTE), true, true);
			combinedQuery.add(dateQuery, Occur.MUST);
		}
		return combinedQuery;
	}
	
	public IndexSearcher newIndexSearcher() throws IOException {
		log.debug("getting index "+indexLocation);
        return new IndexSearcher(indexLocation);
	}
	
	public List<SearchResult> getSearchResults(int startIndex) {
		
		initializeStatics();
	
		hasMoreResults  = false;
		try {
	        IndexSearcher searcher = null;
	        
	        try {
		        searcher = newIndexSearcher();
		        IndexReader reader = searcher.getIndexReader();
		        
				Query luceneQuery = generateLuceneQuery(searcher);
				luceneQuery = luceneQuery.rewrite(reader);
	            Hits hits = searcher.search(luceneQuery);
	            
	            usingSuggestedQuery = false;
				suggestedQuery = null;
				if ((didYouMeanParser != null) &&
						((hits.length() < minimumHits) ||
						 (calcScore(searcher, getQuery()) < minimumScore))) {
					if (log.isDebugEnabled()) {
						log.debug("Need to suggest because either num hits "+hits.length()+" < "+minimumHits+
								"\n or top hit score "+(hits.length() > 0 ? hits.score(0) : "[NO HITS]")+" < "+minimumScore);
					}
					IndexSearcher compositeSearcher = new IndexSearcher(compositeIndexLocation);
					try {
						log.debug("calling suggest() with query="+getQuery()+" and composite index from "+compositeIndexLocation);
						//Query didYouMean = didYouMeanParser.suggest(getQuery(), compositeSearcher.getIndexReader());
						Query suggestedQueries[] = didYouMeanParser.getSuggestions(getQuery(), compositeSearcher.getIndexReader());
						TreeSet<Suggestion> suggestions = new TreeSet<Suggestion>();
							
						if (suggestedQueries != null) {
							for (int i = 0; i < suggestedQueries.length; i++) {
								log.debug("trying suggested query: "+suggestedQueries[i].toString(defaultField));
								String suggestedQueryString = suggestedQueries[i].toString(defaultField);
								String constrainedQueryString = suggestedQueryString;
								if (constrainedQueryString.indexOf('"') < 0 && constrainedQueryString.indexOf('\'') < 0) {
									constrainedQueryString = "\""+constrainedQueryString+"\"~5"; // proximity/distance query (within 5 words of each other)
								}
								Query suggestedLuceneQuery = generateLuceneQuery(constrainedQueryString, searcher);
								suggestedLuceneQuery = suggestedLuceneQuery.rewrite(reader);
					            Hits suggestedHits = searcher.search(suggestedLuceneQuery);
					            
					            float score = calcScore(suggestedQueryString, suggestedHits);
					            
					            log.debug("=========================================");
					            log.debug("SCORE = "+score);
					            log.debug("=========================================");
					            
					            suggestions.add(
				            		new Suggestion(suggestedQueryString,
				            						suggestedLuceneQuery,
				            						suggestedHits,
					            					score,
					            					((i == 0) ? didYouMeanParser.includesOriginal() : false)));
								log.debug("hits="+suggestedHits.length()+", score="+score);
							}
						}
							
						Suggestion best = null;
						if (suggestions.size() > 0) {
							best = suggestions.last();
						}
						
						if (best != null && !best.isOriginal()) {
							suggestedQuery = best.getQueryString();
							if (suggestedQuery != null && suggestedQuery.indexOf('+') >= 0 && getQuery().indexOf('+') < 0) {
								suggestedQuery = suggestedQuery.replaceAll("\\+", "");
							}
							if (hits.length() == 0) {
					            if (best.getHits().length() > 0) {
					            	// Requery probably required because we added proximity before
									String suggestedQueryString = best.getQueryString();
									luceneQuery = generateLuceneQuery(suggestedQueryString, searcher);
									luceneQuery = luceneQuery.rewrite(reader);
						            hits = searcher.search(luceneQuery);
					            	//hits = best.getHits();
					            	//luceneQuery = best.getLuceneQuery();
						            usingSuggestedQuery = true;
					            }
							}
							log.debug("DidYouMeanParser suggested "+suggestedQuery);
						} else {
							if (best != null && best.isOriginal()) {
								log.debug("The suggestion was the original query after all");
							}
							log.debug("DidYouMeanParser did not suggest anything");
						}
					} finally {
						compositeSearcher.close();
					}
				}
					/*
				if (hits.length() == 0 && suggestedQuery != null) {
					// If we didn't find anything at all, go ahead and show them what the suggested query
					// will give them
					Query suggestedLuceneQuery = generateLuceneQuery(suggestedQuery, searcher);
					suggestedLuceneQuery = suggestedLuceneQuery.rewrite(reader);
		            Hits suggestedHits = searcher.search(suggestedLuceneQuery);
		            if (suggestedHits.length() > 0) {
		            	hits = suggestedHits;
		            	luceneQuery = suggestedLuceneQuery;
			            usingSuggestedQuery = true;
		            }
				}
		            */
	            totalHits = hits.length();
	            //Get the genere matches:
	            try {
		            BitSetFacetHitCounter facetHitCounter = new BitSetFacetHitCounter();
		            facetHitCounter.setSearcher(searcher);
		            String baseQueryString = (isUsingSuggestedQuery() ? suggestedQuery : query);
		            String quotedQueryString = baseQueryString;
		            if (quotedQueryString.indexOf('"') == -1 && quotedQueryString.indexOf(' ') > -1) {
		            	quotedQueryString = "\"" + quotedQueryString+ "\"";
		            }
		            facetHitCounter.setBaseQuery(luceneQuery, baseQueryString);
		            
		            List<HitCount> subQueries = new ArrayList<HitCount>();
		            for(Map.Entry<String, Query> entry: genreQueries.entrySet()) {
		            	subQueries.add(new HitCount(entry.getKey(), entry.getValue(), entry.getValue().toString(), 0));
		            }
		            facetHitCounter.setSubQueries(subQueries);		            
		            genreCounts = facetHitCounter.getFacetHitCounts(true);
		            
		            whatMatchedCounts = new ArrayList<HitCount>();
		            whatMatchedCounts.add(new HitCount("Title", getFieldQuery(baseQueryString,"programTitle", searcher), "programTitle:"+quotedQueryString, 0));
		            whatMatchedCounts.add(new HitCount("Episode Title", getFieldQuery(baseQueryString,"episodeTitle", searcher), "episodeTitle:"+quotedQueryString,0));
		            whatMatchedCounts.add(new HitCount("Description",getFieldQuery(baseQueryString,"description", searcher), "description:"+quotedQueryString,0));            
		            whatMatchedCounts.add(new HitCount("Content", getFieldQuery(baseQueryString,"text", searcher), "text:"+quotedQueryString,0));	            
		            whatMatchedCounts.add(new HitCount("Credits", getFieldQuery(baseQueryString,"credits", searcher), "credits:"+quotedQueryString,0));	            
		            facetHitCounter.setSubQueries(whatMatchedCounts);		            
		            whatMatchedCounts = facetHitCounter.getFacetHitCounts(true);
		            
		            //Program Count  -- Not sure if there is a better way to do this.
		            HashSet<String> programTitles = new HashSet<String>(); 
		            programCounts = new ArrayList<HitCount>();
		            for (int i= 0; i < hits.length() && programCounts.size() < 5; i++) {
		            	String title = hits.doc(i).get("programTitle");
		            	if(!programTitles.contains(title)) {
		            		String queryTitle = title;
		            		queryTitle = QueryParser.escape(title);
		            		if (queryTitle.indexOf('"') > -1) {
		            			queryTitle.replace("\"","\\\"");
		            		}
		            		if (queryTitle.indexOf(' ') >-1) {
		            			queryTitle = "\""+queryTitle+"\"";
		            		}
		            		
		            		programCounts.add(new HitCount(title,  getFieldQuery(queryTitle,"programTitle", searcher), "programTitle:"+queryTitle, 0));
		            		programTitles.add(title);
		            	}		            	
		            }
		            facetHitCounter.setSubQueries(programCounts);		            
		            programCounts = facetHitCounter.getFacetHitCounts(false);
	            } catch (Exception e) {
	            	e.printStackTrace();
	            }

	            results = new ArrayList<SearchResult>();
	            programToSearchResult.clear();
	            Query userQuery = getContentQuery(query, searcher);
	            userQuery.rewrite(reader);
	        	Highlighter highlighter = new Highlighter(new TermFormatter(), new QueryScorer(userQuery, "text"));
	        	
				log.debug("#hits="+hits.length());

		    	EPGProvider epgProvider = DefaultEpg.getInstance();

		    	boolean missingWebPaths = false; // We added this to the index midstream, so some do and some don't.
		    									// Next index rebuild, and they'll all have it.
		    	for (int i=0; i < pageSize && i+startIndex < hits.length(); i++) {
    		    	if (hits.doc(i+startIndex).get("webPath") == null) {
    		    		missingWebPaths = true;
    		    		break;
    		    	}
		    	}
		    	Program[] programs = null;
		    	if (missingWebPaths) {
    		    	List<String> programIds = new ArrayList<String>(pageSize);
    		    	for (int i=0; i < pageSize && i+startIndex < hits.length(); i++) {
                		programIds.add(hits.doc(i+startIndex).get("programID"));
    		    	}
    		    	programs = DefaultEpg.getInstance().getProgramList(programIds);
		    	}
		    	for (int i=0; i < pageSize && i+startIndex < hits.length(); i++) {
		            	addDocument(hits.doc(i+startIndex), hits.score(i+startIndex),
		            			epgProvider, highlighter, analyzer,
		            			null, null, (programs==null?null:programs[i]));
	            }
	            if (results.size()+startIndex < hits.length()) {
	            	hasMoreResults = true;
	            }
	        } finally {
	        	if (searcher != null) {
	        		searcher.close();
	        	}
	        }
		} catch (IOException e) {
			log.error("Error searching index" , e);
		} catch (ParseException e) {
			log.error("Error searching index" , e);			
		}
		return results;
	}

	private float calcScore(IndexSearcher searcher, String suggestedQueryString) throws IOException, ParseException {
		String constrainedQueryString = suggestedQueryString;
		if (constrainedQueryString.indexOf('"') < 0 && constrainedQueryString.indexOf('\'') < 0) {
			constrainedQueryString = "\""+constrainedQueryString+"\"~5"; // proximity/distance query (within 5 words of each other)
		}
		Query luceneQuery = generateLuceneQuery(constrainedQueryString, searcher);
		luceneQuery = luceneQuery.rewrite(searcher.getIndexReader());
        Hits hits = searcher.search(luceneQuery);
		float score = calcScore(suggestedQueryString, hits);
		return score;
	}
	
	private float calcScore(String suggestedQueryString, Hits suggestedHits) throws IOException {
		float score = 0.0f;
		if (suggestedHits.length() > 0) {
			Document doc = suggestedHits.doc(0);
			StringBuilder sb = new StringBuilder();
			String s;
			s=doc.get("programTitle");
			if (s != null) {
		    	sb.append(s);
		    	sb.append(" ");
			}
			s=doc.get("episodeTitle");
			if (s != null) {
		    	sb.append(s);
		    	sb.append(" ");
			}
			s=doc.get("description");
			if (s != null) {
		    	sb.append(s);
		    	sb.append(" ");
			}
			s=doc.get("credits");
			if (s != null) {
		    	sb.append(s);
		    	sb.append(" ");
			}
			s=doc.get("genre");
			if (s != null) {
		    	sb.append(s);
		    	sb.append(" ");
			}
		    for (int j = 0; j < suggestedHits.length(); j++) {
		    	doc = suggestedHits.doc(j);
		    	s = doc.get("text");
		    	if (s != null && s.trim().length() > 30) { 
		    				// 30 is arbitrary, but no sense accepting a really short dialog
		        	sb.append(s);
		        	break;
		    	}
		    }
			String text = sb.toString();
			TokenStream tokenStream = analyzer.tokenStream("query", new StringReader(suggestedQueryString));
			ArrayList<String> queryTokens = new ArrayList<String>();
			Token token = tokenStream.next();
			while (token != null) {
				queryTokens.add(token.termText());
				token = tokenStream.next();
			}
			int current = 0;
			tokenStream = analyzer.tokenStream("compositeField", new StringReader(text));
			token = tokenStream.next();
			while (token != null) {
				String tokenText = token.termText();
				if (tokenText.equals(queryTokens.get(current))) {
					current++;
					if (current == queryTokens.size()) {
						score += 1.0f;
						current = 0;
					}
				} else if (current > 0) {
					current = 0;
					if (tokenText.equals(queryTokens.get(current))) {
						current++;
					}
				}
				token = tokenStream.next();
			}
		}
		log.debug("****************************************\n"+suggestedQueryString+" score: "+score+"\n***************************");
		return score;
	}
		
	private void addDocument(Document doc, float score,
			EPGProvider epgProvider, Highlighter highlighter, Analyzer analyzer,
			ScheduledProgram next, ScheduledProgram last, Program programInfo) throws IOException {
		String text = doc.get("text");
		String fragments = null;
		if (text != null) {
			TokenStream tokenStream = analyzer.tokenStream("text", new StringReader(text));
			fragments = highlighter.getBestFragments(tokenStream, text, 3, "...");
		}
		SearchResult searchResult = new SearchResult(lineup, new DocumentWrapper(doc, score, fragments), programInfo, last, next);
		results.add(searchResult);
		programToSearchResult.put(doc.get("programID"), searchResult);		
	}
	
	
	public List<SearchResult> getCachedResults() {
		return results;
	}
	


	/**
	 * @return Returns the headEnd.
	 */
	public String getLineup() {
		return lineup;
	}




	/**
	 * @param headEnd The headEnd to set.
	 */
	public void setLineup(String lineup) {
		this.lineup = lineup;
	}




	/**
	 * @return Returns the indexLocation.
	 */
	public String getIndexLocation() {
		return indexLocation;
	}




	/**
	 * @param indexLocation The indexLocation to set.
	 */
	public void setIndexLocation(String indexLocation) {
		this.indexLocation = indexLocation;
	}




	/**
	 * @return Returns the query.
	 */
	public String getQuery() {
		return query;
	}




	/**
	 * @param query The query to set.
	 */
	public void setQuery(String query) {
		this.query = query;
	}



	/**
	 * @return Returns the results.
	 */
	public ArrayList<SearchResult> getResults() {
		return results;
	}




	/**
	 * @param results The results to set.
	 */
	public void setResults(ArrayList<SearchResult> results) {
		this.results = results;
	}

	/**
	 * @return Returns the showingScheduledPrograms.
	 */              
	public SearchType getSearchType() {
		return searchType;
	}




	/**
	 * @param showingScheduledPrograms The showingScheduledPrograms to set.
	 */
	public void setSearchType(SearchType searchType) {
		this.searchType = searchType;
	}




	/**
	 * @return Returns the totalHits.
	 */
	public int getTotalHits() {
		return totalHits;
	}




	/**
	 * @param totalHits The totalHits to set.
	 */
	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}
	
	public SearchResult getSearchResult(String program) {
		return programToSearchResult.get(program);
	}
	
	public boolean hasMoreResults() {
		return hasMoreResults;
	}

	/**
	 * @return Returns the pageSize.
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize The pageSize to set.
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public int lastDocumentIndex() {
		return lastDocumentIndex;
	}

	public Date getModifiedSince() {
		return modifiedSince;
	}

	public void setModifiedSince(Date modifiedSince) {
		this.modifiedSince = modifiedSince;
	}

	public String getSuggestedQuery() {
		return suggestedQuery;
	}

	public boolean isUsingSuggestedQuery() {
		return usingSuggestedQuery;
	}
	


	private static final class Suggestion implements Comparable {
		
		private String queryString;
		private Query luceneQuery;
		private Hits hits;
		private double score;
		private boolean original;
		
		public Suggestion(String queryString, Query luceneQuery, Hits hits, double score, boolean original) {
			this.queryString = queryString;
			this.luceneQuery = luceneQuery;
			this.hits = hits;
			this.score = score;
			this.original = original;
		}

		public Hits getHits() {
			return hits;
		}

		public void setHits(Hits hits) {
			this.hits = hits;
		}
		
		public String getQueryString() {
			return queryString;
		}

		public void setQueryString(String queryString) {
			this.queryString = queryString;
		}
		
		public int getMagnitude() {
			return (int)(Math.log(hits.length() + 10) * 3.0);
		}

		public double getScore() {
			return score;
		}

		public void setScore(double score) {
			this.score = score;
		}

		public int compareTo(Object rhs) {
			Suggestion other = (Suggestion)rhs;
			if (getScore() < other.getScore()) {
				log.debug("2. "+queryString+" loses to "+other.queryString);
				return -1;
			}
			if (getScore() > other.getScore()) {
				log.debug("2. "+queryString+" beats "+other.queryString);
				return 1;
			}
			if (getMagnitude() < other.getMagnitude()) {
				log.debug("1. "+queryString+" loses to "+other.queryString);
				return -1;
			}
			if (getMagnitude() > other.getMagnitude()) {
				log.debug("1. "+queryString+" beats "+other.queryString);
				return 1;
			}
			return 0;
		}

		public boolean isOriginal() {
			return original;
		}

		public void setOriginal(boolean original) {
			this.original = original;
		}

		public Query getLuceneQuery() {
			return luceneQuery;
		}

		public void setLuceneQuery(Query luceneQuery) {
			this.luceneQuery = luceneQuery;
		}
	}



	/**
	 * @return Returns the genreCounts.
	 */
	public Collection<HitCount> getGenreCounts() {
		return genreCounts;
	}

	/**
	 * @param genreCounts The genreCounts to set.
	 */
	public void setGenreCounts(Collection<HitCount> genreCounts) {
		this.genreCounts = genreCounts;
	}

	/**
	 * @return Returns the programCounts.
	 */
	public Collection<HitCount> getProgramCounts() {
		return programCounts;
	}

	/**
	 * @param programCounts The programCounts to set.
	 */
	public void setProgramCounts(Collection<HitCount> programCounts) {
		this.programCounts = programCounts;
	}

	/**
	 * @return Returns the whatMatchedCounts.
	 */
	public Collection<HitCount> getWhatMatchedCounts() {
		return whatMatchedCounts;
	}

	/**
	 * @param whatMatchedCounts The whatMatchedCounts to set.
	 */
	public void setWhatMatchedCounts(Collection<HitCount> whereMatchedCounts) {
		this.whatMatchedCounts = whereMatchedCounts;
	}



	
}
