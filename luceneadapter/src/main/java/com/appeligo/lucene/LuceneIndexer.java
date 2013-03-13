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

package com.appeligo.lucene;


import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;

import com.appeligo.config.ConfigurationService;
import com.knowbout.epg.service.ScheduledProgram;

/**
 * 
 * @author fear
 *
 */
public class LuceneIndexer {
    private static final Logger log = Logger.getLogger(LuceneIndexer.class);
    private static HashMap<String, LuceneIndexer> indexers = new HashMap<String, LuceneIndexer>();
    private IndexerQueue queue;
    private String indexLocation;
    public static final String[] STOP_WORDS;
    
    static {
    	Configuration config = ConfigurationService.getConfiguration("system");
    	String[] words = config.getStringArray("stopWords.word");
    	if (words == null || words.length == 0) {
    		words = PorterStemAnalyzer.ENGLISH_STOP_WORDS;
    	}    
    	STOP_WORDS = words;
    }
    
    @SuppressWarnings("unchecked")
	private LuceneIndexer(String indexLocation, Analyzer analyzer) {
    	this.indexLocation = indexLocation;
    	queue = new IndexerQueue("Lucene Indexer: " + indexLocation, indexLocation, 
    			analyzer, 24*60);
        queue.start();
    }
    
    public static LuceneIndexer[] getAllInstances() {
    	return indexers.values().toArray(new LuceneIndexer[0]); 
    }
    
    public synchronized static LuceneIndexer getInstance(String indexLocation) {
    	LuceneIndexer indexer = indexers.get(indexLocation);
        if (indexer == null) {
        	indexer = new LuceneIndexer(indexLocation, new PorterStemAnalyzer(STOP_WORDS));
        	indexers.put(indexLocation, indexer);        	
        } else {
        	if (!(indexer.queue.getAnalyzer() instanceof PorterStemAnalyzer)) {
        		throw new RuntimeException("Obtaining a Instance of the LuceneIndexer that does not have a PorterStemAnalyzer.  This means it was created with the getCompositeIndex method instead of the getInstance method.");
        	}
        }
        return indexer;
    }
    
    public synchronized static LuceneIndexer getCompositeInstance(String indexLocation) {
    	LuceneIndexer indexer = indexers.get(indexLocation);    	
        if (indexer == null) {
        	indexer = new LuceneIndexer(indexLocation, new StandardAnalyzer());
        	indexers.put(indexLocation, indexer);        	
        } else {
        	if (!(indexer.queue.getAnalyzer() instanceof StandardAnalyzer)) {
        		throw new RuntimeException("Obtaining a CompositInstance of the LuceneIndexer that does not have a StandardAnalyzer.  This means it was created with the getInstance method instead of the getCompositeIndex method.");
        	}
        }
        return indexer;
    }
    
    public void addAction(QueueAction action) {
    	queue.addQueueAction(action);
    }
    /**
     * 
     */
    public void addProgram(String captions, String programId, List<ScheduledProgram> programs, Store store, boolean replace, Date modified) {
    	if (replace) {
			Term term = new Term("programID", programId);
			queue.deleteDocuments(term);
    	}
        Document doc = new Document();
        if (store == null) {
        	store = Field.Store.NO;
        }
        DocumentUtil.addCaptions(doc, captions);
    	DocumentUtil.populateDocument(doc, programs, modified);
    	
    	queue.addDocument(doc);    
    }

    public void addCompositeProgram(String captions, String programId, List<ScheduledProgram> programs, Store store, boolean replace, Date modified) {
    	if (replace) {
			Term term = new Term("programID", programId);
			queue.deleteDocuments(term);
    	}
        Document doc = new Document();
        if (store == null) {
        	store = Field.Store.NO;
        }    	
        Document compositeDoc = new Document();
    	DocumentUtil.populateCompositeDocument(doc, captions, programs);
    	queue.addDocument(compositeDoc);
    	
    }

    /**
     * 
     */
    public void updateProgram(String programId, List<ScheduledProgram> programs, Date modified) {
        Document programDoc = getProgramDocument(programId);
        Document doc = new Document();
    	if (programDoc != null) {
    		String captions = programDoc.get("text");
    		if (captions != null) {
    			DocumentUtil.addCaptions(doc, captions);
    		}
			Term term = new Term("programID", programId);
			queue.deleteDocuments(term);
    	}
    	DocumentUtil.populateDocument(doc, programs, modified);
    	queue.addDocument(doc);
    }
    
    private Document getProgramDocument(String programId) {
        IndexSearcher searcher = null;
        Document programDoc = null;
        try {
	        searcher = new IndexSearcher(indexLocation);
	        TermQuery termQuery = new TermQuery(new Term("programID", programId));
	        Hits hits = searcher.search(termQuery);
	        if (hits.length() > 0) {
	        	programDoc = hits.doc(0);
	        }
        } catch (IOException e) {
        	log.error(e);
        } finally { 
        	try {        		
        		searcher.close();
        	} catch(IOException e) {
        		log.error(e);
        	}
        }
        return programDoc;    	
    }
    /**
     * 
     */
    public void updateCompositeProgram(String programId, List<ScheduledProgram> programs, Date modified) {
        Document programDoc = getProgramDocument(programId);
        Document doc = new Document();
        String captions = "";
    	if (programDoc != null) {
    		captions = programDoc.get("text");
			Term term = new Term("programID", programId);
			queue.deleteDocuments(term);
    	}
    	DocumentUtil.populateCompositeDocument(doc, captions, programs);
    	queue.addDocument(doc);
    }
    
    public void deleteDocuments(Term term) {
    	queue.deleteDocuments(term);
    }
    
	public void deleteDocuments(Term[] terms) {
    	queue.deleteDocuments(terms);
	}
    
    /**
     * Returns the number of minutes between optimizations of the index.
     * @return
     */
    public int getOptimizeDuration() {
    	return queue.getOptimizeDuration();
    }
    
    /**
     * Sets the number of minutes between index optimizations. This will not take affect until after
     * the next optimization.  If you need to force an optimization, use the OptimizeAction instead.
     * @param minutes
     */
    public void setOptimizeDuration(int minutes) {
    	queue.setOptimizeDuration(minutes);
    }
    
    public static void shutdownAll() {
    	LuceneIndexer[] indexers = getAllInstances();
    	for (LuceneIndexer index: indexers){
    		index.shutdown();
    	}
    }
    
    public void shutdown() {
    	if (queue != null) {
    		synchronized(queue) {
    			log.debug("Notifying " + indexLocation +" to shutdown.");
    	    	queue.stopIndexing();
    		}
    	}
    	while (queue != null && queue.isAlive()) {
    		try {
    			log.debug("Waiting for " + indexLocation + " to shutdown.");
    			synchronized(this) {
    				wait(500);
    			}
    		} catch (InterruptedException e) {    			
    		}
    	}
    }
        
}
