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

package com.appeligo.ccdataweb;

import java.io.IOException;
import java.util.AbstractList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Queue;

import org.apache.log4j.Logger;

public class CaptionStoreImpl implements CaptionStore {
	
	private static final Logger log = Logger.getLogger(CaptionStoreImpl.class);
	
	private int maxOpenDocuments;
	private int maxCachedIndexes;
	private Map<String,CaptionFile> cache = Collections.synchronizedMap(
			new HashMap<String,CaptionFile>());
	private AbstractList<CaptionFile> recentlyUsedIndexes = new LinkedList<CaptionFile>();
	private AbstractList<CaptionFile> recentlyUsedDocuments = new LinkedList<CaptionFile>();

	private int openDocuments;
	
	private static CaptionStoreImpl instance;
	
	public CaptionStoreImpl() {
		if (instance != null) {
			log.error("Hessian is creating more than one CaptionStoreImpl!");
		}
		instance = this;
		try {
			maxOpenDocuments = Config.getConfiguration().getInt("maxOpenDocuments", 50);
			maxCachedIndexes = Config.getConfiguration().getInt("maxCachedIndexes", 1000);
		} catch (Throwable t) {
			log.error("Couldn't use configuration for documentRoot", t);
		}
	}
	
	public synchronized CaptionFile getCaptionFile(String lineupId,
			String callsign, long programStartTime) throws IOException {
		
		String path = CaptionFile.getFilePathPrefix(lineupId, callsign, programStartTime);
		CaptionFile file = cache.get(path);
		if (file == null) {
			if (openDocuments == maxOpenDocuments) {
				CaptionFile lru = recentlyUsedDocuments.remove(0);
				lru.closeDocument();
				openDocuments--;
			}
			file = new CaptionFile(lineupId, callsign, programStartTime);
			openDocuments++;
			if (cache.size() == maxCachedIndexes) {
				CaptionFile lru = recentlyUsedIndexes.remove(0);
				cache.remove(lru.getFilePathPrefix());
				openDocuments--;
			}
			cache.put(path, file);
			recentlyUsedIndexes.add(file);
		}
		if (!file.isDocumentOpened()) {
			if (openDocuments == maxOpenDocuments) {
				CaptionFile lru = recentlyUsedDocuments.remove(0);
				lru.closeDocument();
				openDocuments--;
			}
			file.openDocument();
			openDocuments++;
			recentlyUsedDocuments.add(file);
		}
		int size = recentlyUsedDocuments.size();
		if (size > 0) {
			CaptionFile mru = recentlyUsedDocuments.get(size-1);
			if (file != mru) {
				for (int i = size-2; i >= 0; i--) {
					CaptionFile recent = recentlyUsedDocuments.get(i);
					if (recent == file) {
						recentlyUsedDocuments.remove(i);
						break;
					}
				}
				recentlyUsedDocuments.add(file);
			}
		}
		size = recentlyUsedIndexes.size();
		if (size > 0) {
			CaptionFile mru = recentlyUsedIndexes.get(size-1);
			if (file != mru) {
				for (int i = size-2; i >= 0; i--) {
					CaptionFile recent = recentlyUsedIndexes.get(i);
					if (recent == file) {
						recentlyUsedIndexes.remove(i);
						break;
					}
				}
				recentlyUsedIndexes.add(file);
			}
		}
		return file;
	}
	
	public CaptionFile getCaptionFile(String headendId, String lineupDevice,
			String callsign, long programStartTime) throws IOException {
		return getCaptionFile(headendId+"-"+lineupDevice, callsign, programStartTime);
	}
	
	/* (non-Javadoc)
	 * @see com.appeligo.ccdataweb.CaptionStore#getSentence(java.lang.String, java.lang.String, long, long)
	 */
	public String getSentence(String lineupId,
			String callsign, long programStartTime, long timestamp) throws IOException {
		return getCaptionFile(lineupId,
				callsign, programStartTime).getSentence(timestamp);
	}
	
	/* (non-Javadoc)
	 * @see com.appeligo.ccdataweb.CaptionStore#getSentence(java.lang.String, java.lang.String, java.lang.String, long, long)
	 */
	public String getSentence(String headendId, String lineupDevice,
			String callsign, long programStartTime, long timestamp) throws IOException {
		return getCaptionFile(headendId, lineupDevice,
				callsign, programStartTime).getSentence(timestamp);
	}
	
	/* (non-Javadoc)
	 * @see com.appeligo.ccdataweb.CaptionStore#getSentences(java.lang.String, java.lang.String, long, long, long)
	 */
	public String[] getSentences(String lineupId,
			String callsign, long programStartTime, long startTimestamp, long endTimestamp) throws IOException {
		return getCaptionFile(lineupId,
				callsign, programStartTime).getSentences(startTimestamp, endTimestamp);
	}
	
	/* (non-Javadoc)
	 * @see com.appeligo.ccdataweb.CaptionStore#getSentences(java.lang.String, java.lang.String, java.lang.String, long, long, long)
	 */
	public String[] getSentences(String headendId, String lineupDevice,
			String callsign, long programStartTime, long startTimestamp, long endTimestamp) throws IOException {
		return getCaptionFile(headendId, lineupDevice,
				callsign, programStartTime).getSentences(startTimestamp, endTimestamp);
	}
}
