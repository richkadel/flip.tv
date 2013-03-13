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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

public class IndexerQueue extends Thread {
    private static final Logger log = Logger.getLogger(IndexerQueue.class);
    private List<QueueAction> actionList = new ArrayList<QueueAction>();
    private long nextOptimizeTime;
    private String indexLocation;
    private int optimizeDuration;
    private Analyzer analyzer;
    private boolean stop;
    
    public IndexerQueue(String indexLocation, String compositeIndexLocation, Analyzer analyzer) {
        this("Lucene Indexer", indexLocation, analyzer, 24*60);
    }
    
    public IndexerQueue(String threadName, String indexLocation, Analyzer analyzer, int optimizeDuration) {
        super(threadName);
        setDaemon(true);
        this.analyzer = analyzer;
        this.indexLocation = indexLocation;
        this.optimizeDuration = optimizeDuration;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, optimizeDuration);
        if (optimizeDuration >= 24*60) {
	        calendar.clear(Calendar.HOUR_OF_DAY);
	        calendar.clear(Calendar.MINUTE);
	        calendar.clear(Calendar.SECOND);
	        calendar.clear(Calendar.MILLISECOND);
        }
        nextOptimizeTime = calendar.getTimeInMillis();
        stop = false;
    }
    
    public synchronized void addDocument(Document doc) {
    	actionList.add(new AddDocumentAction(doc));
        notifyAll();
    }
    
    public synchronized void deleteDocuments(Term term) {
    	actionList.add(new DeleteDocumentsAction(term));
        notifyAll();
    }

	public synchronized void deleteDocuments(Term[] terms) {
    	actionList.add(new DeleteDocumentsMultiTermAction(terms));
        notifyAll();
	}

    public synchronized void addQueueAction(QueueAction action) {
    	actionList.add(action);
        notifyAll();
    }
    
    public void run() {
        while (!stop) {
            try {
                if (!actionList.isEmpty()) {
                    //NOTE: this is prone to losing the documents if the server 
                    //shuts down while documents are queued up or if an exception is thrown
                    List<QueueAction> toIndex = new ArrayList<QueueAction>(actionList.size());
                    synchronized (this) {
                        toIndex.addAll(actionList);
                        actionList.clear();
                    }
                    boolean optimize = (nextOptimizeTime < System.currentTimeMillis());
                    indexDocuments(toIndex, optimize);
                }
                
                synchronized (this) {
                    if (actionList.isEmpty()) {
                        wait();
                    }
                }
            } catch (Throwable t) {
                log.error("Error indexing documents.", t);
            }
        }
    }
    
    private synchronized void indexDocuments(List<QueueAction> actions, boolean optimize) throws IOException {
        IndexWriter indexWriter = createIndexWriter();
        try {
            for (QueueAction action : actions) {
            	if (log.isDebugEnabled()) {
            		log.debug("Processing document " + action);
            	}
            	try {
            		action.performAction(indexWriter);
            	} catch (IOException e) {
            		//If one fails try the rest?  
            		log.error("Unable to process action: "+ action, e);
            	}
            }
            
            if (optimize) {
                //reset the next optimize time
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(nextOptimizeTime);
                calendar.add(Calendar.MINUTE, optimizeDuration);
                nextOptimizeTime = calendar.getTimeInMillis();
                
                log.info("Optimizing index for " + indexLocation);
                indexWriter.optimize();
                log.info("Finished with optimization for " + indexLocation);                
            }
        } finally {
            indexWriter.close();
        }
    }
    
    private IndexWriter createIndexWriter() throws IOException {
        File indexDir = new File(indexLocation);
        if (!indexDir.isDirectory()) {
            log.info("Creating Lucene index directory " + indexDir);
            indexDir.mkdirs();
        }
        IndexWriter indexWriter = null;
        while (indexWriter == null) {
        	try {
        		indexWriter = new IndexWriter(indexDir, analyzer);
        	} catch (IOException e) {
        		log.error("Failed to obtain lock for " + indexLocation + ".  Trying again.", e);
        	}
        }
        return indexWriter;
    }


	/**
	 * @return Returns the optimizeDuration.
	 */
	public int getOptimizeDuration() {
		return optimizeDuration;
	}

	/**
	 * @param optimizeDuration The optimizeDuration to set.
	 */
	public void setOptimizeDuration(int optimizeDuration) {
		this.optimizeDuration = optimizeDuration;
	}
    
    public void stopIndexing() {
    	stop = true;
    	notifyAll();
    }

	/**
	 * @return Returns the analyzer.
	 */
	public Analyzer getAnalyzer() {
		return analyzer;
	}

	/**
	 * @return Returns the indexLocation.
	 */
	public String getIndexLocation() {
		return indexLocation;
	}

    
}
