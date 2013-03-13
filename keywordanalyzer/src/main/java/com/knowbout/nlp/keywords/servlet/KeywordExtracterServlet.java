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

package com.knowbout.nlp.keywords.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jdom.Element;

import com.knowbout.keywords.listener.Keyword;
import com.knowbout.nlp.keywords.Extractor;
import com.knowbout.nlp.keywords.KeywordExtracter;

import opennlp.common.Pipeline;
import opennlp.common.PipelineException;
import opennlp.common.xml.NLPDocument;

/**
 * Uses a pipline implementation in order to find keywords in the text provided.
 * 
 * @author Jake Fear
 *
 */
public class KeywordExtracterServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger(KeywordExtracterServlet.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6276515837918568233L;
	
	// Holds the names of the classes that will make up the language processing
	// pipline.
	private String[] pipelineClasses;
	
	/**
	 * 
	 */
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		String pipeline = servletConfig.getInitParameter("pipeline");
		if (pipeline != null) {
			pipelineClasses = pipeline.split(",");
			for (int i = 0; i < pipelineClasses.length; i++) {
				pipelineClasses[i] = pipelineClasses[i].trim();
			}
		} else {
			throw new ServletException("You must specify the pipline parameter to this servlet.");
		}
		
	}

	/**
	 * 
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * 
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Pass analyze the text in the "text" parameter and return a comma separated list of
		// keyword values.
		String text = request.getParameter("text");
		if (log.isInfoEnabled()) {
			log.info("Text: {" + text + "}");
		}
		if (text != null && !"".equals(text)) {
			PrintWriter writer = response.getWriter();
			Extractor extracter = new KeywordExtracter();
			Collection<Keyword> keywords = extracter.extract(text);
			StringBuilder sb = new StringBuilder();
			if (keywords.size() > 0) {
				sb.append('(');
				Iterator<Keyword> keywordIterator = keywords.iterator();
				while (keywordIterator.hasNext()) {
					// Maybe we should have a "long format" specifier here that includes
					// the "type" of word and the part of speech.
					if (request.getParameter("details") == null) {
						sb.append(keywordIterator.next().getKeyword());
					} else {
						sb.append(keywordIterator.next());
					}
					if (keywordIterator.hasNext()) {
						sb.append(' ');
					}
				}
				sb.append(')');
				if (log.isInfoEnabled()) {
					log.info("Keywords: " + sb.toString());
				}
				writer.print(sb.toString());
			} else {
				writer.println("<empty set>");
			}
		}
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	private Collection<String> processPipeline(String text) throws PipelineException {
		Pipeline pipeline = new Pipeline(pipelineClasses);
		NLPDocument doc = pipeline.run(text);
		// Need to iterator over document elements and find all the keyword
		// values and return them.
		Set<String> words = new HashSet<String>();
		addElements(words, "name", doc);
		addElements(words, "search", doc);
		addElements(words, "url", doc);
		return words;
	}
	
	@SuppressWarnings("unchecked")
	private void addElements(Collection<String> collector, String type, NLPDocument doc) {
		List<Element> elements = doc.getTokenElementsByType(type);
		for (Element e : elements) {
			String keyword = e.getChildText("w");
			if (keyword != null) {
				if (!"name".equals(type)) {
					keyword = keyword.toLowerCase();
				}
				if (!Character.isLetterOrDigit(keyword.charAt(keyword.length() - 1))) {
					keyword = keyword.substring(0, keyword.length() - 1);
				}
			}
			collector.add(keyword);
		}
	}
}
