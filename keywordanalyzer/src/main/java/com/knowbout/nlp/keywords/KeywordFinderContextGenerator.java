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
 * An event generator for the maxent NameFinder.  This guy probably needs some work.  This
 * should help is deal with the particular weirdness of closed captioning by allowing
 * us to generate a context that is particular to it.
 * 
 * I'll need deeper knowledge of what the relevant values are to make a lot of headway
 * with this.
 * 
 * @author Jake fear
 */

class KeywordFinderContextGenerator implements ContextGenerator {

	private static final Logger log = Logger.getLogger(KeywordFinderContextGenerator.class);
	
	/**
	 * Builds up the list of features, anchored around a position within the
	 * String[].
	 */
	public String[] getContext(Object o) {
		String[] ls = (String[]) ((ObjectIntPair) o).a;
		int i = ((ObjectIntPair) o).b;

		List<String> contextElements = new ArrayList<String>();

		String lex = ls[i];
		contextElements.add("keyword=" + lex);
		if (Character.isUpperCase(lex.charAt(0))) {
			contextElements.add("cap");
		}

		String prev = "";
		if (i > 0) {
			prev = ls[i - 1];
			if (prev.equals("")) {
				contextElements.add("firstword");
			} else {
				if (PerlHelp.isPunctuation(prev)) {
					contextElements.add("prevpunct");
				}
				if (Character.isUpperCase(prev.charAt(0))) {
					contextElements.add("prevcap");
				}
				if (i < 2 || ls[i - 2].equals("")) {
					contextElements.add("prevfirstword");
				}
				if (prev.charAt(prev.length() -1) == '>') {
					contextElements.add("speakerchange");
				}
			}
		} else {
			contextElements.add("firstword");
		}

		String next = "";
		if (i < ls.length - 1) {
			next = ls[i + 1];
			if (Character.isUpperCase(next.charAt(0))) {
				contextElements.add("nextcap");
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Context Elements:" + contextElements);
		}
		String[] context = new String[contextElements.size()];
		contextElements.toArray(context);
		return context;
	}

	// public static void main(String[] args) {
	// String data =
	// "But the {@ Bush @} administration says it wants to see evidence that all
	// {@ Cocom @} members are complying fully with existing export-control
	// procedures before it will support further liberalization .\nTo make its
	// point , it is challenging the Italian government to explain reports that
	// {@ Olivetti @} may have supplied the {@ Soviet Union @} with
	// sophisticated computer-driven devices that could be used to build parts
	// for combat aircraft .\nThe {@ New York Times @} is great .";
	// NFEventGenerator nteg = new NFEventGenerator();
	// nteg.generateEventList(new StringReader(data));
	// }

}
