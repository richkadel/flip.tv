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

package com.knowbout.nlp.keywords.util;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author fear
 */
public class TextUtil {

	public static final String TOKEN_SEPARATER = " ";
	
	/**
	 * 
	 * @param text
	 * @return
	 */
	public static List<String> tokenize(String text) {
		List<String> results = Arrays.asList(text.split("\\s"));
		return results;
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 */
	public static boolean isMixedCase(String text) {
		boolean upper = false;
		boolean lower = false;
		
		char[] chs = text.toCharArray();
		for (char c : chs) {
			if (!upper) {
				upper = Character.isUpperCase(c);
				continue;
			}
			if (!lower) {
				lower = Character.isLowerCase(c);
			}
		}
		return upper && lower;
	}
	
	/**
	 * 
	 * @param tokens
	 * @return
	 */
	public static String concat(List<String> tokens) {
		StringBuilder sb = new StringBuilder(tokens.size() * 10);
		int size = tokens.size();
		for (int i = 0; i < size; i++) {
			sb.append(tokens.get(i));
			if (i < size - 1) {
				sb.append(TOKEN_SEPARATER);
			}
		}
		return sb.toString();
	}
}
