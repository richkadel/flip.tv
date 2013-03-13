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

import java.io.Serializable;

import org.apache.lucene.search.Query;

public class HitCount implements Comparable, Serializable {
	  /**
	 * 
	 */
	private static final long serialVersionUID = -8986918036487798641L;
	
	private String label;
    private int count;
    private Query query;
    private String queryString;
          
    public HitCount(String label, Query query, String queryString, int count) {
        this.label = label;
        this.count = count;
        this.query = query;
        this.queryString = queryString;
    }

	/**
	 * @return Returns the count.
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return Returns the query.
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * @param query The query to set.
	 */
	public void setQuery(Query query) {
		this.query = query;
	}

	/**
	 * @return Returns the queryString.
	 */
	public String getQueryString() {
		return queryString;
	}

	/**
	 * @param queryString The queryString to set.
	 */
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	/**
	 * @param count The count to set.
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @param label The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(Object o) {
		if (o instanceof HitCount) {
			HitCount hc = (HitCount)o;
			int compare = hc.count - count;
			if (compare == 0) {
				return label.compareTo(hc.label);
			} else {
				return compare;
			}
		}
		return 1;
	}
	 
	  
}
