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

import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;

import com.appeligo.alerts.KeywordAlert;
import com.appeligo.epg.DefaultEpg;
import com.appeligo.search.entity.User;
import com.appeligo.search.util.ConfigUtils;
import com.knowbout.epg.service.Program;

public class CaptionSearchAction extends BaseAction {
	
	private static final Logger log = Logger.getLogger(CaptionSearchAction.class);
	private static final Logger queryLog = Logger.getLogger("fliptv.query");
    private static final long serialVersionUID = 7019890080969775786L;

    public static final String KEY ="SearchResultKey";
	private static final String ONERESULT = "oneresult";
    
    private SearchResults results;
    private List<SearchResult> searchResults;   
    
    private String action;
    private KeywordAlert keywordAlert;
    private int currentPage;
    private int startIndex;
    private int endIndex;
    private String webPath;
    
    public String execute() throws Exception {
    	String query = getQuery();
    	
        if (query == null || query.trim().length() == 0) {
        	query = null;
        	return INPUT;
        } else {
        	if (currentPage == 0) {
        		currentPage = 1;
        	}
        	User user = getUser();
        	StringBuilder sb = getCookieValue();
    		sb.append("|query=");
    		sb.append(query.replace("|","\\|"));
        	queryLog.info(sb.toString());
            String indexDir = ConfigUtils.getSystemConfig().getString("luceneIndex");
            String compositeIndexDir = ConfigUtils.getSystemConfig().getString("compositeIndex");
            if (IndexReader.indexExists(indexDir)) {
            	String lineup = getLineup();
    	    	results = (SearchResults)getSession().get(KEY);
    	    	if (results == null) {
    	    		results = new SearchResults(indexDir, compositeIndexDir, 10, lineup);
    	    		getSession().put(KEY, results);
    	    	}
    	    	results.setLineup(lineup);
    	    	results.setQuery(query);
    	    	results.setSearchType(getSearchTypeAsSearchType());
    	    	startIndex = 0;
    	    	if (currentPage > 0) {
    	    		startIndex = (currentPage-1)*results.getPageSize();
    	    	}
    	    	searchResults = results.getSearchResults(startIndex);
    	    	endIndex = startIndex + searchResults.size();
            } else {
            	log.warn(indexDir + " does not exist as an index. Check directory and file permissions.");
            }
        
            if (user != null) {
		        keywordAlert = KeywordAlert.getByNormalizedQuery(user, query);
            }
        }
        
        if (searchResults.size() == 1) {
        	Program program = DefaultEpg.getInstance().getProgram(searchResults.get(0).getProgramId());
        	if (program != null) {
            	setWebPath(program.getWebPath());
            	return ONERESULT;
        	}
        }
        return SUCCESS;
    }
   
    public List<SearchResult> getHits() {
        return searchResults;
    }
    
    public SearchResults getSearchResults() {
    	return results;
    }


	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public KeywordAlert getKeywordAlert() {
		return keywordAlert;
	}

	public void setKeywordAlert(KeywordAlert keywordAlert) {
		this.keywordAlert = keywordAlert;
	}

	/**
	 * @return Returns the currentPage.
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * @param currentPage The currentPage to set.
	 */
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getStartIndex() {
		return startIndex +1;
	}
	
	public int getEndIndex() {
		return endIndex;
	}

	public Date getNow() {
		return new Date();
	}

	public String getWebPath() {
		return webPath;
	}

	public void setWebPath(String webPath) {
		this.webPath = webPath;
	}

	

}
