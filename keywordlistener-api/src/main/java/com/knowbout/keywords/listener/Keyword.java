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

package com.knowbout.keywords.listener;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Provides a simple encapsulation of a keyword along with that keywords'
 * "type", which is assigned by the pipeline component that recognized the
 * keyword as such.  The type defines what the keyword is useful for.
 * @author fear
 * @author almilli
 */
public class Keyword implements Serializable, Comparable<Keyword> {

	private static final long serialVersionUID = 1457690227134033107L;

	public enum Type {
		NAME,
		PERSON,
		LOCATION,
		DATE,
		TIME,
		ORGANIZATION,
		SEARCH,
		URL,
		EMAIL,
	}
	
    private String keyword;
    private Type type;
    private double weight;
    private transient List<String> keywordList;

    /**
     * Creates a new keyword instance without setting any of the properties
     */
    public Keyword() {
    }

    public Keyword(Type type, Object keyword) {
    	this.type = type;
    	this.keyword = keyword.toString();
    	weight = (double)this.keyword.length()/100D;
    	if (this.isMultiword()) {
    		weight *= 0.8D;
    	}
    }
    
    public Keyword(String type, Object keyword) {
    	this(Type.valueOf(type.toUpperCase()), keyword);
    }
    
    /**
     * 
     * @param type
     * @param keyword
     * @param weight
     */
    public Keyword(String type, Object keyword, double weight) {
        this(Type.valueOf(type.toUpperCase()), keyword, weight);
    }
    
    /**
     * Creates a new keyword instance
     * @param type the type of keyword this is
     * @param keyword the keyword text
     * @param weight the weight of this keyword relative to others
     */
    public Keyword(Type type, Object keyword, double weight) {
        this(type, keyword);
        this.weight = weight;
    }
   
    /**
     * Gets the complete text for the keyword.
     * @return the keyword text
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Sets the keyword text.
     * @param keyword the keyword text
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
        this.keywordList = null;
    }
    
    /**
     * Gets the keywords as a list of words.  If this keyword is a multiword value, then this
     * list will have more than one item in it.
     * @return the list of words in this keyword
     */
    public List<String> getWordList() {
    	if (keywordList == null) {
    		keywordList = Arrays.asList(keyword.split(" "));
    	}
    	return keywordList;
    }

    /**
     * Gets the type of keyword this is
     * @return one of the Keyword.Type enum values.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of keyword this is.
     * @param type the keyword type
     */
    public void setType(Type type) {
        this.type = type;
    }
   
    /**
     * If more than one word makes up this "keyword", true.
     * @return true if the keyword is made up of multiple words, false if a single word.
     */
    public boolean isMultiword() {
        return keyword.indexOf(' ') > -1;
    }

    /**
     * Gives the weight of the keyword relative to every other keyword.  Weight defines the 
     * importance of the keyword.
     * @return the weight of the keyword
     */
	public double getWeight() {
		return weight;
	}

    /**
     * Sets the weight for this keyword relative to others.
     * @param weight the weight of this keyword.
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }
   
    /**
     * Generates a string representation of this keyword
     */
    public String toString() {
        StringBuilder sb = new StringBuilder().append(getType()).append(':').append(getKeyword());
        sb.append("; weight=").append(weight);
        return sb.toString();
    }
   
    /**
     * Gives the hashcode for this keyword.
     */
    public int hashCode() {
        return keyword.hashCode();
    }
    
    /**
     * 
     */
	public int compareTo(Keyword o) {
		int value = -1*new Double(this.weight).compareTo(o.weight);
		// PENDING JMF: I know this is a ridicuous substitute for a meaningful weight, 
		// but in a small amount of testing using several search engines this seems to
		// be a decent metric for finding words that have fewer/more specific meanings.
		// While there is not better method being provided by the NLP mechanism, this will
		// be better than nothing.
		if (value == 0) {
			value = -1*new Integer(this.keyword.length()).compareTo(o.keyword.length());
		}
		return value;
	}
   
    /**
     * PENDING JMF: For the sake of equivalence only the keyword is considered, this may change to include the
     * type value in the future as well, but at this time that is not needed, or desirable.
     */
    public boolean equals(Object other) {
        if (other instanceof Keyword) {
        	Keyword ok = (Keyword)other;
            boolean equal = ok.getKeyword().equals(this.getKeyword());
            if (equal) {
            	equal = ok.getType().equals(this.getType());
            }
            if (equal) {
            	equal = ok.getWeight() == this.getWeight();
            }
            return equal;
        } else {
            return false;
        }
    }
}