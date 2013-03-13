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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.appeligo.lucene.LuceneIndexer;
import com.appeligo.search.util.ConfigUtils;

/**
 * Provides basic initialization of the configuration subsystem for this application.
 * 
 */
public class LuceneInitializer implements ServletContextListener {
	
    private static final Log log = LogFactory.getLog(LuceneInitializer.class);

    /**
     * Expected to initialize n servlet initializaton.
     */
    public void contextInitialized(ServletContextEvent event) {

    	Configuration config = ConfigUtils.getSystemConfig();
    	String programIndex = config.getString("luceneIndex");
    	String compositeIndex = config.getString("compositeIndex");
    	String spellIndex = config.getString("spellIndex");
    	String liveIndex = config.getString("luceneLiveIndex");
    	String productIndex = config.getString("luceneProductIndex");
    	unlockIndex(programIndex);
    	unlockIndex(compositeIndex);
    	unlockIndex(spellIndex);
    	unlockIndex(liveIndex);
    	unlockIndex(productIndex);
    }
    
    private void unlockIndex(String indexLocation) {
    	if (IndexReader.indexExists(indexLocation)) {
    		try {
	    		if (IndexReader.isLocked(indexLocation)) {
	    			Directory directory = FSDirectory.getDirectory(indexLocation);
	    			IndexReader.unlock(directory);
	    		}
    		} catch (IOException e) {
    			log.fatal("Error trying to unlock " + indexLocation + " index.", e);
    		}
    	}
    }

    public void contextDestroyed(ServletContextEvent event) {
    	log.debug("Trying to exit LuceneIndexer threads gracefully");
    	LuceneIndexer.shutdownAll();
    	log.debug("All LuceneIndexer threads have shutdown.");
    }

}
