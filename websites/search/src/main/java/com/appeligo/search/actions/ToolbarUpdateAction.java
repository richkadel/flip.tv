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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;

import com.appeligo.search.util.ConfigUtils;

public class ToolbarUpdateAction extends BaseAction {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = -7795615164855427697L;

	private static final Log log = LogFactory.getLog(ToolbarUpdateAction.class);
	
	private static Map<String,SearchEngine> searchEngineMap;
	
	private String toolbarId;
	private String url;
	private int revision;
    private SearchResults searchResults;
	private List<SearchResult> hits;
	
	static {
		Configuration config = ConfigUtils.getSystemConfig();
		try {
			String[] specs = config.getStringArray("searchEngines");
			searchEngineMap = new HashMap<String,SearchEngine>();
			for (int i = 0; i < specs.length-2; i += 3) {
				log.debug(i+": host="+specs[i]);
				log.debug("path="+specs[i+1]);
				log.debug("param="+specs[i+2]);
				String host = specs[i];
				int port = 80;
				if (host.indexOf(':') > 0) {
					String[] parts = host.split(":");
					host = parts[0];
					port = Integer.parseInt(parts[1]);
				}
				if (host.startsWith("www.")) {
					host = host.substring(4);
				}
				searchEngineMap.put(host, new SearchEngine(host, port, specs[i+1], specs[i+2]+"="));
			}
		} catch (Throwable t) {
			log.error("Can't load search engine specs!", t);
		}
	}

    public String execute() throws Exception {
		Integer toolbarRevision = (Integer)getSession().get("toolbarRevision");
		if (toolbarRevision == null) {
			revision = 0;
		} else {
			revision = toolbarRevision.intValue()+1;
		}
		getSession().put("toolbarRevision", new Integer(revision));
		try {
			URL u = new URL(url);
			String host = u.getHost();
			if (host.startsWith("www.")) {
				host = host.substring(4);
			}
log.debug(host);	
log.debug(u.getPort());	
log.debug(u.getPath());	
			SearchEngine searchEngine = searchEngineMap.get(host);
			if ((searchEngine != null) &&
				(u.getPort() == searchEngine.getPort() ||
						((u.getPort() == -1) && (searchEngine.getPort() == 80))) &&
				(u.getPath().indexOf(searchEngine.getPath()) == 0)) {
				String query = u.getQuery();
				if (query != null) {
					String[] params = query.split("&");
					for (String param : params) {
						if (param.startsWith(searchEngine.getParam())) {
log.debug(param);
							String q = param.substring(searchEngine.getParam().length());
							q = URLDecoder.decode(q, "UTF-8");
log.debug(q);
							setQuery(q);
				            String indexDir = ConfigUtils.getSystemConfig().getString("luceneIndex");
				            String compositeIndexDir = ConfigUtils.getSystemConfig().getString("compositeIndex");
				            if (IndexReader.indexExists(indexDir)) {
				            	String lineup = getLineup();
				    	    	searchResults = new SearchResults(indexDir, compositeIndexDir, 10, lineup);
				    	    	searchResults.setLineup(lineup);
				    	    	searchResults.setQuery(getQuery());
				    	    	searchResults.setSearchType(getSearchTypeAsSearchType());
				    	    	hits = searchResults.getSearchResults(0);
				            }
							break;
						}
					}
				}
			}
		} catch (MalformedURLException e) {
			//ignore;
		}
		
        return SUCCESS;
    }

	public String getToolbarId() {
		return toolbarId;
	}

	public void setToolbarId(String toolbarId) {
		this.toolbarId = toolbarId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	private static class SearchEngine {
		private String host;
		private int port;
		private String path; 
		private String param;
		public SearchEngine(String host, int port, String path, String param) {
			this.host = host;
			this.port = port;
			this.path = path;
			this.param = param;
		}
		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
		}
		public String getParam() {
			return param;
		}
		public void setParam(String param) {
			this.param = param;
		}
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
	}

	public int getRevision() {
		return revision;
	}

	public Log getLog() {
		return log;
	}

	public List<SearchResult> getHits() {
		return hits;
	}
    
	public SearchResults getSearchResults() {
		return searchResults;
	}
}
