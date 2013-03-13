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

///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002 Jason Baldridge and Gann Bierner
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

package com.knowbout.nlp.keywords;

import opennlp.maxent.*;
import opennlp.common.util.*;
import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * An event generator for the maxent NameFinder.
 * 
 * @author Gann Bierner
 * @version $Revision: 1.6 $, $Date: 2002/02/08 12:13:36 $
 */

class KeywordFinderEventCollector implements EventCollector {

	private static final Logger log = Logger.getLogger(KeywordFinderEventCollector.class);
	
	private BufferedReader br;

	private ContextGenerator cg = new KeywordFinderContextGenerator();

	public KeywordFinderEventCollector(Reader data) {
		br = new BufferedReader(data);
	}

	public static Pair convertAnnotatedString(String str) {
		// build datalist to send to Event generator. Also build
		// outcomes for each token.
		ArrayList<String> tokens = new ArrayList<String>();
		ArrayList<String> outcomes = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(str);
		boolean inKeywords = false;
		int keywordCounter = 0;
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (tok.equals("{@")) {
				outcomes.add("T");
				tokens.add(st.nextToken());
				inKeywords = true;
			} else if (tok.equals("@}")) {
				inKeywords = false;
				keywordCounter = 0;
			} else if (inKeywords) {
				outcomes.add("T");
				tokens.add(tok);
				keywordCounter++;
				if (keywordCounter > 4) {
					System.err.println("Probably too many keywords: " + tokens);
				}
			} else {
				outcomes.add("F");
				tokens.add(tok);
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("tokens: " + tokens);
			log.debug("outcomes: " + outcomes);
		}
		return new Pair(tokens, outcomes);
	}

	/**
	 * Builds up the list of features using the Reader as input. For now, this
	 * should only be used to create training data.
	 */
	public Event[] getEvents() {
		return getEvents(false);
	}

	/**
	 * 
	 */
	public Event[] getEvents(boolean evalMode) {
		ArrayList elist = new ArrayList();
		int numMatches;

		try {
			String s = br.readLine();

			while (s != null) {
				Pair p = convertAnnotatedString(s);
				List tokenList = (ArrayList) p.a;
				String[] tokens = new String[tokenList.size()];
				tokenList.toArray(tokens);
				ArrayList outcomes = (ArrayList) p.b;

				for (int i = 0; i < tokens.length; i++) {
					String[] context = cg.getContext(new ObjectIntPair(tokens,
							i));
					Event e = new Event((String) outcomes.get(i), context);
					elist.add(e);
					// System.out.println(e);
				}
				s = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Event[] events = new Event[elist.size()];
		for (int i = 0; i < events.length; i++)
			events[i] = (Event) elist.get(i);

		return events;
	}

}
