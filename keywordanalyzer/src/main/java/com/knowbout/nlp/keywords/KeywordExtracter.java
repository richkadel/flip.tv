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

package com.knowbout.nlp.keywords;

import java.io.File;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import opennlp.common.Pipeline;
import opennlp.common.PipelineException;
import opennlp.common.preprocess.Pipelink;
import opennlp.common.xml.NLPDocument;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.jdom.Element;

import com.knowbout.keywords.listener.Keyword;
import com.knowbout.nlp.keywords.util.Config;

/**
 * Encapsulates a pipeline that is specially tuned for finding keyword
 * values in bodies of text that facilite targeted search results for the 
 * next in question.
 * 
 * @author fear
 */
public class KeywordExtracter implements Extractor {

	private static final Logger log = Logger.getLogger(KeywordExtracter.class);
	
	private static final String[] DEFAULT_PIPELINE = 
	{
		"opennlp.grok.preprocess.namefind.WebStuffDetector",
		"opennlp.grok.preprocess.sentdetect.EnglishSentenceDetectorME",
	    "opennlp.grok.preprocess.tokenize.EnglishTokenizerME",
	    "opennlp.grok.preprocess.postag.EnglishPOSTaggerME",
	    // PENDING JMF: As badly as we still need this, for now we are going to 
	    // lean on the name finder until we can actually train a real model.
	    //"com.knowbout.nlp.keywords.EnglishSearchWordFinderME",
	    "opennlp.grok.preprocess.namefind.EnglishNameFinderME",
	};
	
	private Pipeline pipeline;
	
	/**
	 * Allows a specific pipeline configuration to be specified, thus overriding the
	 * default configuration.  Make sure you know what you are doing...
	 * 
	 * @param pipelineConfiguration A list of class name values that will make up the
	 * pipline in the order they are specified.
	 */
	private KeywordExtracter(String... pipelineConfiguration) {
		try {
			pipeline = new Pipeline(pipelineConfiguration);
		} catch (PipelineException pe) {
			log.error(pe.getMessage(), pe);
			throw new RuntimeException(pe.getMessage());
		}
	}
	
	/**
	 * Creates a KeywordExtrater with a default pipline.
	 */
	public KeywordExtracter() {
		this(DEFAULT_PIPELINE);
	}
	
	/* (non-Javadoc)
	 * @see com.knowbout.nlp.keywords.Extractor#extract(java.lang.String)
	 */
	public Collection<Keyword> extract(String text) {
		return processPipeline(text);
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 */
	public Collection<Keyword> extract(File text) {
		return processPipeline(text);
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 */
	public Collection<Keyword> extract(Reader text) {
		return processPipeline(text);
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 */
	private Collection<Keyword> processPipeline(Object text) {
		NLPDocument doc;
		try {
			doc = pipeline.run(text);
		} catch (PipelineException pe) {
			log.error(pe.getMessage() + text, pe);
			throw new RuntimeException(pe.getMessage());
		}
		// Need to iterator over document elements and find all the keyword
		// values and return them.  Keywords will be ordered according to their
		// natural ordering.
		Collection<Keyword> words = new TreeSet<Keyword>();
		addElements(words, "name", doc);
		addElements(words, "search", doc);
		addElements(words, "url", doc);
		return words;
	}
	
	/**
	 * 
	 * @param collector
	 * @param type
	 * @param doc
	 */
	@SuppressWarnings("unchecked")
	private void addElements(Collection<Keyword> collector, String type, NLPDocument doc) {
		List<Element> elements = doc.getTokenElementsByType(type);
		for (Element e : elements) {
			List<Element> words = e.getChildren("w");
			String keyword = getTokens(words);
			if (!"name".equals(type)) {
				keyword = keyword.toLowerCase();
			}
			Keyword term = new Keyword(Keyword.Type.valueOf(type.toUpperCase()), keyword);
			if (!collector.contains(term)) {
				collector.add(term);
			}
		}
	}
	
	/**
	 * Extract the tokens for a list of XML elements from the NLPDocument object.
	 * @param words
	 * @return
	 */
	private String getTokens(List<Element> words) {
		if (words.size() == 1) {
			Element word = words.get(0);
			String token = word.getText();
			return cleanToken(token);
		} else if (words.size() > 1) {
			StringBuilder sb = new StringBuilder();
			Iterator<Element> wordIterator = words.iterator();
			while (wordIterator.hasNext()) {
				String token = cleanToken(wordIterator.next().getText());
				if (token != null) {
					sb.append(token).append(' '); // Pad it for the next token.
				}
			}
			// This is the easiest way to eliminate excess white space. It could
			// be tuned by doing it on the string buffer above if needed, but this
			// is more of an edge case (multi-word names) than the normal flow.
			return sb.toString().trim(); 
		} else {
			return null;
		}
	}
	
	/**
	 * Attempts to clobber trailing punctuation, explicitley leaves the possessive '
	 * character in place if found.
	 * 
	 * @param dirtyToken
	 * @return
	 */
	private String cleanToken(String dirtyToken) {
		Configuration config = Config.getConfiguration();
		String regex = config.getString("preprocessingRegex", null);
		if (regex != null) {
			if (log.isDebugEnabled()) {
				log.debug("preprocessing keyword " + dirtyToken + " with regex " + 
						regex + " to get " + dirtyToken.replaceAll(regex, ""));
			}
			dirtyToken = dirtyToken.replaceAll(regex, "");
		}
		
		int l = dirtyToken.length();
		if (l > 1 && !Character.isLetterOrDigit(dirtyToken.charAt(l - 1))) {
			// But don't clobber the possessive suffix of a '.
			if (!dirtyToken.endsWith("'"))
				dirtyToken = dirtyToken.substring(0, l - 1);
		}
		return dirtyToken; // Which should now be a clean token...
	}
	
}
