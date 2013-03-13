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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryFilter;

public class BitSetFacetHitCounter {

	private Query baseQuery;
	private String baseQueryString;
	
	private Collection<HitCount> subQueries;

	private IndexSearcher searcher;

	private BitSet baseBitSet;

	public BitSetFacetHitCounter() {
		super();
	}

	public void setBaseQuery(Query baseQuery, String baseQueryString) {
		this.baseQuery = baseQuery;
		this.baseQueryString = baseQueryString;
		baseBitSet = null;
	}

	public void setSubQueries(Collection<HitCount> subQueries) {
		this.subQueries = subQueries;
	}

	public void setSearcher(IndexSearcher searcher) {
		this.searcher = searcher;
		baseBitSet = null;
	}

	public BitSet getBaseBitSet() throws IOException {
		if (baseBitSet == null) {
			if (baseQuery != null && searcher != null) {
				IndexReader reader = searcher.getIndexReader();
				QueryFilter baseQueryFilter = new QueryFilter(baseQuery);
				baseBitSet = baseQueryFilter.bits(reader);
			}
		}
		return baseBitSet;
	}

	public Collection<HitCount> getFacetHitCounts(boolean sortByCount) throws IOException {
		List<HitCount> facetCounts = new ArrayList<HitCount>();
		IndexReader reader = searcher.getIndexReader();
		for (HitCount hitCount : subQueries) {
			QueryFilter filter = new QueryFilter(hitCount.getQuery());
			BitSet filterBitSet = filter.bits(reader);
			BitSet baseBits = getBaseBitSet();
			if (baseBits != null) {
				facetCounts.add(new HitCount(hitCount.getLabel(), hitCount.getQuery(), baseQueryString + " AND " + hitCount.getQueryString(),
						getFacetHitCount(baseBitSet, filterBitSet)));
			}
		}
		if (sortByCount) {
			Collections.sort(facetCounts);			
		}
		return facetCounts;
	}

	private int getFacetHitCount(BitSet baseBitSet, BitSet filterBitSet) {
		filterBitSet.and(baseBitSet);
		return filterBitSet.cardinality();
	}

}
