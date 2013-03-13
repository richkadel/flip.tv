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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import opennlp.common.preprocess.Pipelink;
import opennlp.common.preprocess.Tokenizer;
import opennlp.common.util.ObjectIntPair;
import opennlp.common.xml.NLPDocument;
import opennlp.maxent.ContextGenerator;
import opennlp.maxent.Evalable;
import opennlp.maxent.EventCollector;
import opennlp.maxent.MaxentModel;
import opennlp.maxent.TrainEval;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * A "Search Word" Finder that uses maximum entropy to determine if a word is a good 
 * word to pass to a search engine or not.
 * 
 * @author Jake Fear
 * @version $Revision: 1.12 $, $Date: 2002/02/11 15:43:34 $
 */

class KeywordFinderME implements Pipelink, Evalable {

	private static final Logger log = Logger.getLogger(KeywordFinderME.class);
	
	/**
	 * The maximum entropy model to use to evaluate contexts.
	 */
	protected MaxentModel model;

	/**
	 * The feature context generator.
	 */
	protected static final ContextGenerator contextGenerator = new KeywordFinderContextGenerator();

	/**
	 * 
	 *
	 */
	protected KeywordFinderME() {
	}

	public KeywordFinderME(MaxentModel mod) {
		model = mod;
	}

	public String getNegativeOutcome() {
		return "F";
	}

	public EventCollector getEventCollector(Reader r) {
		return new KeywordFinderEventCollector(r);
	}

	public void localEval(MaxentModel model, Reader r, Evalable e, boolean verbose) {
	}

	/**
	 * 
	 * @param l
	 * @param pos
	 * @return
	 */
	protected boolean isSearchWord(String[] l, int pos) {
		ObjectIntPair info = new ObjectIntPair(l, pos);
		double[] eval = model.eval(contextGenerator.getContext(info));
		String guess = model.getBestOutcome(eval);
		boolean isSearchWord = guess.equals("T");
		if (isSearchWord && log.isDebugEnabled()) {
			log.debug("SearchWord:" + l[pos]);
		}
		return isSearchWord;
	}

	/**
	 * Find the names in a document.
	 */
	public void process(NLPDocument doc) {
		for (Iterator sentIt = doc.sentenceIterator(); sentIt.hasNext();) {
			Element sentEl = (Element) sentIt.next();
			List wordEls = doc.getWordElements(sentEl);
			String[] wordList = doc.getWords(sentEl);

			List<String> sanitized = new ArrayList<String>(wordList.length);
			for (String s : wordList) {
				if (s != null && s.trim().length() > 0) sanitized.add(s);
			}
			wordList = sanitized.toArray(new String[sanitized.size()]);
			
			for (int i = 0; i < wordList.length; i++) {
				if (isSearchWord(wordList, i)) {
					Element wordEl = (Element) wordEls.get(i);
					if (wordEl.getParent().getAttributeValue("type") == null) {
						// check that the part-of-speech is an allowed part of speech.
						String pos = wordEl.getAttributeValue("pos");
						if (isValidPartOfSpeech(pos)) {
							Element parentToken = ((Element) wordEls.get(i)).getParent();
							parentToken.setAttribute("type", "search");
							while (++i < wordList.length && isSearchWord(wordList, i)) {
								wordEl = (Element) wordEls.get(i);
								wordEl.getParent().detach();
								parentToken.addContent(wordEl.detach());
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Uses the abbreviations described in the "abbreviations file to determine
	 * if the part of speech assigned to a give word is a valid part of speech
	 * for keywords or not. If not part of speech (POS) is defined (null is 
	 * given to this method) then true is returned.
	 * @param pos
	 * @return
	 */
	private boolean isValidPartOfSpeech(String pos) {
		if (pos == null || "".equals(pos)) return true;
		char[] chars = pos.toCharArray();
		if (chars[0] == 'N' && chars[1] == 'N') return true;
		if (chars[0] == 'J' && chars[1] == 'J') return true;
		if (chars[0] == 'F' && chars[1] == 'W') return true;
		if (chars[0] == 'V' && chars[1] == 'B') return true;
		if (chars[0] == 'R' && chars[1] == 'B') return true;
		return false;
	}

	/*
	 *  (non-Javadoc)
	 * @see opennlp.common.preprocess.Pipelink#requires()
	 */
	public Set requires() {
		Set<Class> set = new HashSet<Class>();
		set.add(Tokenizer.class);
		return set;
	}

	/**
	 * Example training call:
	 * <p>
	 * java -mx512m opennlp.grok.preprocess.searchword.SearchWordFinderME -t -d ./ -c 5
	 * -s NewEngNF5.bin.gz nameTrain.data
	 * </p>
	 */
	public static void main(String[] args) throws IOException {
		TrainEval.run(args, new KeywordFinderME());
	}
}
