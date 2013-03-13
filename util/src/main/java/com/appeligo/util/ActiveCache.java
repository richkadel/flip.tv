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

package com.appeligo.util;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ActiveCache<K,V> implements Map<K,V> {
	
	private static final Log log = LogFactory.getLog(ActiveCache.class);
	
	protected static final int DEFAULT_MIN_CACHED = 1000;
	protected static final int DEFAULT_MAX_CACHED = 1500;
	
	private Map<K,ActiveObject<K,V>> map;
	private Set<ActiveObject<K,V>> activeSet;
	private int minCached;
	private int maxCached;
	
	public ActiveCache() {
		this(DEFAULT_MIN_CACHED, DEFAULT_MAX_CACHED);
	}
	
	public ActiveCache(int minCached, int maxCached) {
    	map = new HashMap<K,ActiveObject<K,V>>();
    	activeSet = new TreeSet<ActiveObject<K,V>>();
    	this.minCached = minCached;
    	this.maxCached = maxCached;
	}
	
	public void clear() {
		map.clear();
		activeSet.clear();
	}
	
	private void trimCache() {
		int size = size();
		log.debug("Trimming...size is currently="+size+", maxCached="+maxCached+
				", minCached="+minCached+", trimming out "+(size-minCached));
		assert(size == activeSet.size()) : "activeSet and map sizes don't match!";
		Iterator<ActiveObject<K,V>> iter = activeSet.iterator();
		while ((size-- > minCached) && iter.hasNext()) {
    		ActiveObject<K,V> ao = iter.next();
    		ActiveObject<K,V> removed = map.remove(ao.getKey());
			iter.remove();
		}
	}

	public V get(Object key) {
		ActiveObject<K,V> ao = map.get(key);
		if (ao != null) {
			activeSet.remove(ao);
    		V value = ao.getValue();
			ao.setLastAccessed(System.currentTimeMillis());
			activeSet.add(ao);
    		return value;
		}
		return null;
	}
	
	public V put(K key, V value) {
		ActiveObject<K,V> ao = new ActiveObject<K,V>(key, value);
		activeSet.add(ao);
		ActiveObject<K,V> previous = map.put(key, ao);
		if (size() > maxCached) {
			trimCache();
		}
		if (previous != null) {
			activeSet.remove(previous);
    		return previous.getValue();
		}
		return null;
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		Collection<ActiveObject<K,V>> aos = map.values();
		for (ActiveObject<K,V> ao : aos) {
			if (ao.getValue().equals(value)) {
				return true;
			}
		}
		return false;
	}

	public Set<Map.Entry<K,V>> entrySet() {
		Set<Map.Entry<K,V>> set = new HashSet<Map.Entry<K,V>>();
		Set<Map.Entry<K,ActiveObject<K,V>>> aoEntries = map.entrySet();
		for (Map.Entry<K,ActiveObject<K,V>> ao : aoEntries) {
			set.add(new Entry<K,V>(ao.getKey(),ao.getValue().getValue()));
		}
		return set;
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public void putAll(Map<? extends K, ? extends V> t) {
		Set entries = t.entrySet();
		for (Object object : entries) {
			Map.Entry<K,V> entry = (Map.Entry<K,V>)object;
			put(entry.getKey(), entry.getValue());
		}
		if (size() > maxCached) {
			trimCache();
		}
	}

	public V remove(Object key) {
		ActiveObject<K,V> ao = map.remove(key);
		activeSet.remove(ao);
		return ao.getValue();
	}

	public int size() {
		return map.size();
	}

	public Collection<V> values() {
		ArrayList<V> rtn = new ArrayList<V>(size());
		Collection<ActiveObject<K,V>> aos = map.values();
		for (ActiveObject<K,V> ao : aos) {
			rtn.add(ao.getValue());
		}
		return rtn;
	}

	private static class ActiveObject<K,V> implements Comparable<ActiveObject<K,V>> {
		
		private K key;
		private V value;
		private long lastAccessed;
		
		public ActiveObject(K key, V value) {
			this.key = key;
			this.value = value;
			lastAccessed = System.currentTimeMillis();
		}
		
		public K getKey() {
			return key;
		}
		
		public V getValue() {
			return value;
		}
		
		public boolean equals() {
			return key.equals(key) && (value == value) &&
				(lastAccessed == lastAccessed);
		}
		/**
		 * Return a hashcode representing this Object. <code>ActiveObject</code>'s hash
		 * code is calculated by <code>(int) (lastAccessed ^ (lastAccessed &gt;&gt; 32))</code>.
		 *
		 * @return this Object's hash code
		 */
		public int hashCode() {
    		return (int) (lastAccessed ^ (lastAccessed >>> 32));
		}

		public int compareTo(ActiveObject<K,V> right) {
			if (lastAccessed < right.lastAccessed) {
				return -1;
			} else if (lastAccessed > right.lastAccessed) {
				return 1;
			} else {
				try {
    				return ((Comparable<K>)key).compareTo(right.key);
				} catch (ClassCastException e) {
					return key.toString().compareTo(right.key.toString());
				}
			}
		}

		public long getLastAccessed() {
			return lastAccessed;
		}
		
		public void setLastAccessed(long lastAccessed) {
			this.lastAccessed = lastAccessed;
		}
		
		public String toString() {
			return key.toString();
		}
	}
	
	private static class Entry<K,V> implements Map.Entry<K,V> {
		
		private K key;
		private V value;
		
		Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		public V setValue(V value) {
			V previous = this.value;
			this.value = value;
			return previous;
		}
		
		public boolean equals() {
			return (key == key || (key != null && key.equals(key))) &&
			(value == value || (value != null && value.equals(value)));
		}
		
		public int hashCode() {
    		return key.hashCode();
		}
	}
}
