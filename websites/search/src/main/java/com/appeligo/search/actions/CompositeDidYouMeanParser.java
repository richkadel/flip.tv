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
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;

public class CompositeDidYouMeanParser implements DidYouMeanParser {
	
	private static final Logger log = Logger.getLogger(CompositeDidYouMeanParser.class);
	
	private static final int SUGGESTED_PERMUTATIONS_MAXLEVEL = 2;
	private static final int DRILL_DOWN_LEVELS = 2;
	private static Analyzer noStopAnalyzer = new StandardAnalyzer(new HashSet());
		
	private String defaultField;
	private Directory spellIndexDirectory;
	private boolean original;
	
	public CompositeDidYouMeanParser(String defaultField, Directory spellIndexDirectory) {
		this.defaultField = defaultField;
		this.spellIndexDirectory = spellIndexDirectory;
	}

	public Query parse(String queryString) throws ParseException {
		QueryParser queryParser = new QueryParser(defaultField, new StandardAnalyzer());
		queryParser.setDefaultOperator(QueryParser.AND_OPERATOR);		
		return queryParser.parse(queryString);
	}

	public Query suggest(String queryString, IndexReader indexReader) throws ParseException {
		original = false;
		QuerySuggester querySuggester = new QuerySuggester(defaultField, new StandardAnalyzer(), indexReader);
		querySuggester.setDefaultOperator(QueryParser.AND_OPERATOR);
		Query query = querySuggester.parse(queryString);
		querySuggester.setInitialized(true);
		if (!querySuggester.hasSuggestedQuery()) {
			return null;
		}
		if (!querySuggester.isAllNew()) {
			original = true;
		}
		return query;
	}

	public boolean includesOriginal() {
		return original;
	}
	
	public Query[] getSuggestions(String queryString, IndexReader indexReader) throws ParseException {
		original = false;
		QuerySuggester querySuggester = new QuerySuggester(defaultField, new StandardAnalyzer(), indexReader);
		querySuggester.setDefaultOperator(QueryParser.AND_OPERATOR);
		Query query = querySuggester.parse(queryString);
		if (!querySuggester.hasSuggestedQuery()) {
			return null;
		}
		List<Query> queries = new ArrayList<Query>();
		if (!querySuggester.isAllNew()) {
			log.debug("+_++++++++++++++ setting original true");
			original = true;
		}
		queries.add(query);
		int numSuggestions = (int)(Math.pow(2, querySuggester.getNumCheckedTerms()) + 0.1) +  // PERMUTATIONS level 1 (all 0's and 1's)
							 ((int)(Math.pow(2, querySuggester.getNumCheckedTerms()) + 0.1) - 1) + // PERMUTATIONS level 2 (all 1's and 2's, but we did the top row of 1's already)
								(querySuggester.getNumCheckedTerms() * (DRILL_DOWN_LEVELS+querySuggester.getMaxSwapPermutations())); // DRILL DOWN ON INDIVIDUAL COLUMNS
			// 0.1 ensures we don't truncate below
			// the integer we expect
		//for (int i = 1; i < numVariants; i++) { // I don't know a good way to loop through all of the permutations
												// so we're taking a shortcut and only recommending among the
												// top 2 suggestions, which may included the terms given to us,
												// if in the spell index at all
					//Actually, I'm also going to check each term individually a little deeper,
					// and assume that it is the only misspelled term
		log.debug("============= NumSuggestions = "+numSuggestions);
		for (int i = 1; i < numSuggestions; i++) {
			querySuggester.setVariant(i);
			Query q = querySuggester.parse(queryString);
			if (!querySuggester.isInvalid()) {
				queries.add(q);
			}
		}
		return queries.toArray(new Query[queries.size()]);
	}

    private static int soundex(String s) { 

        char[] x = s.toUpperCase().toCharArray();

        // convert letters to numeric code
        for (int i = 0; i < x.length; i++) {
            switch (x[i]) {
                case 'B':
                case 'F':
                case 'P':
                case 'V': { x[i] = '1'; break; }

                case 'C':
                case 'G':
                case 'J':
                case 'K':
                case 'Q':
                case 'S':
                case 'X':
                case 'Z': { x[i] = '2'; break; }

                case 'D':
                case 'T': { x[i] = '3'; break; }

                case 'L': { x[i] = '4'; break; }

                case 'M':
                case 'N': { x[i] = '5'; break; }

                case 'R': { x[i] = '6'; break; }

                default:  { x[i] = '0'; break; }
            }
        }

        // remove duplicates
        String output = "" + x[0];
        char last = x[0];
        for (int i = 1; i < x.length; i++) {
            if (x[i] != '0' && x[i] != last) {
                last = x[i];
                output += last;
            }
        }   

        int rtn = 0;
        try {
        	rtn = Integer.parseInt(output);
        } catch (NumberFormatException e) {
        }

        return rtn;
    }

	private class QuerySuggester extends QueryParser {
		
		private boolean suggestedQuery = false;
		private boolean allNew = false;
		private IndexReader indexReader;
		private boolean initialized = false;
		private int numCheckedTerms = 0;
		private int checkedTermId;
		private int variant = 0;
		private int numVariants = 1;
		private int maxSwapPermutations = 0;
		private List<String[]> termSuggestions = new ArrayList<String[]>();
		private boolean invalid;
		
		public QuerySuggester(String field, Analyzer analyzer, IndexReader indexReader) {
			super(field, analyzer);
			this.indexReader = indexReader;
		}
		
		@Override
		public Query parse(String queryString) throws ParseException {
			invalid = false;
			checkedTermId = -1;
			Query query = super.parse(queryString);
			numCheckedTerms = checkedTermId + 1;
			initialized = true;
			return query;
		}
		
		protected Query getFieldQuery(String field, String queryText) throws ParseException {
			// Copied from org.apache.lucene.queryParser.QueryParser
			// replacing construction of TermQuery with call to getTermQuery()
			// which finds close matches.
		    TokenStream origSource = noStopAnalyzer.tokenStream(field, new StringReader(queryText));
		    TokenStream source = getAnalyzer().tokenStream(field, new StringReader(queryText));
			Vector origV = new Vector();
			Vector v = new Vector();
			Token t;
			
			while (true) {
				try {
					t = origSource.next();
				} catch (IOException e) {
					t = null;
				}
				if (t == null)
					break;
				origV.addElement(t.termText());
			}
			try {
				origSource.close();
			} catch (IOException e) {
				// ignore
			}
			
			while (true) {
				try {
					t = source.next();
				} catch (IOException e) {
					t = null;
				}
				if (t == null)
					break;
				v.addElement(t.termText());
			}
			try {
				source.close();
			} catch (IOException e) {
				// ignore
			}

			if (v.size() == 0) { // Add back the stop words
				if (origV.size() == 0) {
					return null;
				} else if (origV.size() == 1) {
					return new TermQuery(new Term(field, (String) origV.elementAt(0)));
				} else {
					PhraseQuery q = new PhraseQuery();
					q.setSlop(getPhraseSlop());
					for (int i = 0; i < origV.size(); i++) {
						q.add(new Term(field, (String)origV.get(i)));
					}
					return q;
				}
			} else if (v.size() == 1) {
				try {
					return new TermQuery(getTerm(field, (String) v.elementAt(0)));
				} catch (IOException e) {
					ParseException pe = new ParseException("IO Exception: "+e.getMessage());
					pe.setStackTrace(e.getStackTrace());
					throw pe;
				}
			} else {
				PhraseQuery q = new PhraseQuery();
				q.setSlop(getPhraseSlop());
				for (int i = 0; i < v.size(); i++) {
					try {
						q.add(getTerm(field, (String) v.elementAt(i)));
					} catch (IOException e) {
						ParseException pe = new ParseException("IO Exception: "+e.getMessage());
						pe.setStackTrace(e.getStackTrace());
						throw pe;
					}
				}
				return q;
			}
		}
		
		private Term getTerm(String field, String queryText) throws ParseException, IOException {
			log.debug("getTerm "+field+": "+queryText+", using spell index="+spellIndexDirectory+" with indexReader="+indexReader+" and defaultField="+defaultField);
			checkedTermId++;
			if (!initialized) {
				String[] similarWords = null;
				SpellChecker spellChecker = new SpellChecker(spellIndexDirectory);
				similarWords = spellChecker.suggestSimilar(queryText, 40, indexReader, defaultField, false);
				int size = similarWords.length;
				ArrayList<String> wordlist = new ArrayList<String>(size);
				Collections.addAll(wordlist, similarWords);
				StringBuilder swapper = new StringBuilder(queryText);
				log.debug("@#@#@#@#@#@##@#@@#@#@#@#@#   swapper="+swapper+", len="+swapper.length());
				int swapPermutations = 0;
				for (int i = 0; i < queryText.length()-1; i++) {
					char swap = swapper.charAt(i);
					swapper.setCharAt(i, swapper.charAt(i+1));
					swapper.setCharAt(i+1, swap);
					String permutation = swapper.toString();
					log.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ trying permutation "+permutation);
					if (!wordlist.contains(permutation) && spellChecker.exist(permutation)) {
						log.debug("adding "+permutation);
						wordlist.add(Math.min(wordlist.size(), SUGGESTED_PERMUTATIONS_MAXLEVEL+1), permutation);
						swapPermutations++;
					}
					swapper.setCharAt(i+1, swapper.charAt(i));
					swapper.setCharAt(i, swap);
				}
				if (swapPermutations > maxSwapPermutations) {
					maxSwapPermutations = swapPermutations;
				}
				if (wordlist.size() == 0) {
					String[] oneWord = new String[1];
					oneWord[0] = queryText;
					termSuggestions.add(oneWord);
					return new Term(field, queryText);
				}			
				if (log.isDebugEnabled()) {
					StringBuilder words = new StringBuilder();
					for (String word : wordlist) {
						words.append(word);
						words.append(", ");
					}
					
					log.debug("Suggestions before soundex: "+words);
				}
				int soundex = soundex(queryText);
				int lastMatch = -1;
				for (int i = 0; i < size; i++) {
					if (soundex(wordlist.get(i)) == soundex) {
						lastMatch++;
						if (lastMatch < i) {
							String match = wordlist.remove(i);
							wordlist.add(lastMatch, match);
						}
					}
				}
				if (queryText.length() < 3 || spellChecker.exist(queryText)) {
			log.debug("+_-------------- NOT setting allNew");
					wordlist.add(0, queryText);
				} else {
			log.debug("+_++++++++++++++ setting allNew true");
					allNew = true;
				}
				similarWords = wordlist.toArray(new String[wordlist.size()]);
				if (log.isDebugEnabled()) {
					StringBuilder words = new StringBuilder();
					for (String word : similarWords) {
						words.append(word);
						words.append(", ");
					}
					
					log.debug("Suggestions included: "+words);
				}
				termSuggestions.add(similarWords);
				suggestedQuery = true;
				numVariants *= similarWords.length;
			}
			log.debug("initialized="+initialized+", numCheckedTerms="+numCheckedTerms);
			String[] termSuggestion = termSuggestions.get(checkedTermId);
			int suggestionIndex = 0;
			if ((!initialized) || (variant < (int)(Math.pow(2, numCheckedTerms) + 0.1))) {
				if (((variant >> checkedTermId) & 0x1) != 0) {
					suggestionIndex = Math.min(1, termSuggestion.length-1);
					if (termSuggestion.length == 1) {
						log.debug("INVALID termSuggestion[0]="+termSuggestion[0]);
						invalid = true; // The above min will create a dup, so invalidate this query
					}
					// I don't have a good algorithm to calculate indexes other than zero and 1, so we're stopping
					// after variants > 2 to the power of numCheckedTerms
				}
			} else if (variant < ((int)((Math.pow(2, numCheckedTerms)*2) + 0.1)) - 1) { // check 2nd and 3rd choices
				suggestionIndex = Math.min(1, termSuggestion.length-1);
				if ((((variant-(int)((Math.pow(2, numCheckedTerms)*2) + 0.1)+1) >> checkedTermId) & 0x1) != 0) {
					suggestionIndex = Math.min(2, termSuggestion.length-1);
					// I don't have a good algorithm to calculate indexes other than zero and 1, so we're stopping
					// after variants > 2 to the power of numCheckedTerms
				}
				if (termSuggestion.length < suggestionIndex+1) {
					log.debug("INVALID termSuggestion");
					invalid = true; // The above min will create a dup, so invalidate this query
				}
			} else { // use our other method for checking deeper (1st and 3rd, 1st and 4th)
				
				int subvariant = variant - (((int)(Math.pow(2, numCheckedTerms)*SUGGESTED_PERMUTATIONS_MAXLEVEL + 0.1)) - 1);
				int term = subvariant / numCheckedTerms;
				int depth = subvariant % numCheckedTerms;
				if (term == checkedTermId) {
					suggestionIndex = SUGGESTED_PERMUTATIONS_MAXLEVEL+depth;
					if (suggestionIndex >= termSuggestion.length) {
						suggestionIndex = 0;
						invalid = true;
					}
				} else {
					suggestionIndex = 0;
				}
			}
			log.debug("VARIANT "+variant+": termSuggestion["+suggestionIndex+"]="+termSuggestion[suggestionIndex]);
			return new Term(field, termSuggestion[suggestionIndex]);
		}		
		public boolean hasSuggestedQuery() {
			return suggestedQuery;
		}

		public boolean isInitialized() {
			return initialized;
		}

		public void setInitialized(boolean initialized) {
			this.initialized = initialized;
		}

		public int getVariant() {
			return variant;
		}

		public void setVariant(int variant) {
			this.variant = variant;
		}

		public int getNumVariants() {
			return numVariants;
		}

		public void setNumVariants(int numVariants) {
			this.numVariants = numVariants;
		}

		public int getNumCheckedTerms() {
			return numCheckedTerms;
		}

		public void setNumCheckedTerms(int numCheckedTerms) {
			this.numCheckedTerms = numCheckedTerms;
		}

		public boolean isInvalid() {
			return invalid;
		}

		public boolean isAllNew() {
			return allNew;
		}

		public int getMaxSwapPermutations() {
			return maxSwapPermutations;
		}
		
	}
}
