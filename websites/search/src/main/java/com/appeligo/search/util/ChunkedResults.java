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

package com.appeligo.search.util;

import java.util.List;

import org.hibernate.Query;

public class ChunkedResults<T> {
	
	private Query query;
	private int chunkSize;
	private List<T> list;
	private int nextChunkStart;
	private int pos;

	public ChunkedResults(Query query) {
		this(query, 1000);
	}

	public ChunkedResults(Query query, int chunkSize) {
		this.query = query;
		this.chunkSize = chunkSize;
		query.setMaxResults(chunkSize);
		beforeFirst();
	}

	public void beforeFirst() {
		if (nextChunkStart != chunkSize) {
			list = null;
			nextChunkStart = 0;
		}
		//query.setFirstResult(0);
		//pos = 0;
	}

	@SuppressWarnings("unchecked")
	public boolean next() {
		if (list != null) {
			if (pos >= list.size()) {
				list = null;
			}
		}
		if (list == null) {
			query.setFirstResult(nextChunkStart);
			list = query.list();
			nextChunkStart += chunkSize;
			pos = 0;
		} else {
			pos++;
		}
		if (pos >= list.size()) {
			return false;
		}
		return true;
	}

	public T get() {
		if (list == null || pos >= list.size()) {
			return null;
		} else {
			return list.get(pos);
		}
	}
}
