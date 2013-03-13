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

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.Writer;

public class TextPreprocessor {
	
	private static final int MAX_LINE = 80;
	
	private static final String[] TRANSITION_CHARS = {">", ".", ":"};
	
	/**
	 * 
	 * @param token
	 * @return
	 */
	private static boolean isTransitionToken(String token) {
		for (int i = 0; i < TRANSITION_CHARS.length; i++) {
			if (token.endsWith(TRANSITION_CHARS[i])) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param tokenizer
	 * @return
	 * @throws IOException
	 */
	private static String getLine(StreamTokenizer tokenizer) throws IOException {
		StringBuilder sb = new StringBuilder();
		
		String lastToken = null;
		tokenizer.nextToken();
		while (sb.length() < MAX_LINE && tokenizer.ttype != StreamTokenizer.TT_EOF) {
			String token;
			if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
				token = tokenizer.sval;
			} else {
				if (Math.ceil(tokenizer.nval) == tokenizer.nval || 
						Math.floor(tokenizer.nval) == tokenizer.nval) {
					token = new Long((long)tokenizer.nval).toString();
				} else {
					token = String.valueOf(tokenizer.nval);
				}
			}
			// Maybe upper case it...
			if (lastToken != null && isTransitionToken(lastToken)) {
				char[] chars = token.toCharArray();
				chars[0] = Character.toUpperCase(chars[0]);
				token = new String(chars);
			}
			// Add it or push it back on.
			if (sb.length() + token.length() < MAX_LINE) {
				if (sb.length() > 0) {
					sb.append(' ');
				}
				sb.append(token);
				lastToken = token;
				tokenizer.nextToken();
			} else {
				tokenizer.pushBack();
				break;
			}
			
		}
		if (sb.length() > 0) {
			return sb.toString();
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String inputFilename = args[0];
		String outputFilename = args[1];
		Reader reader = new FileReader(inputFilename);
		StreamTokenizer tokenizer = new StreamTokenizer(reader);
		tokenizer.eolIsSignificant(false);
		tokenizer.lowerCaseMode(true);
		tokenizer.wordChars(',', ',');
		tokenizer.wordChars('\'', '\'');
		tokenizer.wordChars('"', '"');
		tokenizer.wordChars(':', ':');
		tokenizer.wordChars(';', ';');
		tokenizer.wordChars('-', '-');
		tokenizer.wordChars('>', '>');
		tokenizer.wordChars('<', '<');
		Writer writer = new FileWriter(outputFilename);
		PrintWriter printer = new PrintWriter(writer);
		String line;
		while((line = getLine(tokenizer)) != null) {
			System.out.println("line="+line);
			printer.println(line);
		}
		reader.close();
		writer.flush();
		writer.close();
	}
}
