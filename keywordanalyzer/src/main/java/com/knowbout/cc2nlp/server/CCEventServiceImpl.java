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

package com.knowbout.cc2nlp.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.appeligo.lucene.LuceneIndexer;
import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.cc2nlp.CCEventService;
import com.knowbout.cc2nlp.CCSentenceEvent;
import com.knowbout.cc2nlp.CCXDSEvent;
import com.knowbout.cc2nlp.CaptionTypeChangeEvent;
import com.knowbout.cc2nlp.ITVLinkEvent;
import com.knowbout.cc2nlp.ProgramStartEvent;
import com.knowbout.cc4j.ITVLink;
import com.knowbout.cc4j.CaptionType;
import com.knowbout.cc4j.XDSField;
import com.knowbout.cc4j.XDSData;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.ScheduledProgram;
import com.knowbout.keywords.listener.Keyword;
import com.knowbout.keywords.listener.KeywordEvent;
import com.knowbout.keywords.listener.KeywordListener;
import com.knowbout.keywords.listener.Program;
import com.knowbout.keywords.listener.SimpleAsychronousKeywordListener;
import com.knowbout.nlp.keywords.Extractor;
import com.knowbout.nlp.keywords.KeywordExtracter;
import com.knowbout.nlp.keywords.persistence.KeywordSourceRepository;
import com.knowbout.nlp.keywords.persistence.KeywordSourceRepositoryFactory;
import com.knowbout.nlp.keywords.util.Config;
import com.knowbout.nlp.keywords.util.TextUtil;

/**
 * This class provides a simple wrapper around the core capabilities of the NLP core, and
 * delegates search capabilities to a configurable endpoint device.  It tracks the last query 
 * done for each distinct CC stream (based on the channel of the stream) and does not send
 * out requests if the object does not change.
 * 
 * @author fear
 */
public class CCEventServiceImpl implements CCEventService {

	private static final Logger log = Logger.getLogger(CCEventServiceImpl.class);
	
	private static String documentRoot = "/tmp"; 
	
	static {
		String configFile = "/etc/flip.tv/channelfeed.xml";
		
		try {
		    // Set up a simple configuration that logs on the console.
		    BasicConfigurator.configure();
			
			XMLConfiguration config = new XMLConfiguration(configFile);
			
			String root = config.getString("documentRoot[@path]");
			if (root != null) {
				documentRoot = root;
			}
			log.info("documentRoot = "+documentRoot);
		} catch (Throwable t) {
			log.error("Can't open channelfeed config file "+configFile, t);
		}
	}
	private Extractor extracter;
	
	private KeywordListener keywordListener;
	
	private EPGProvider epgProvider;
	
	private Map<String, Collection<Keyword>> lastKeywordList = new HashMap<String, Collection<Keyword>>();

	private String programName;

	private PrintStream statsFile;

	private String currentDay;

	private long lastWrite;

	private String hostname;
	
	/**
	 * 
	 *
	 */
	public CCEventServiceImpl() {
		if (log.isInfoEnabled()) {
			log.info("Instantiated a " + this.getClass().getName());
		}
		try {
			extracter = new KeywordExtracter();
			keywordListener = getListener();
			epgProvider = getEpgEndpoint();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	private EPGProvider getEpgEndpoint() throws Exception {
		Configuration config = Config.getConfiguration();
		HessianProxyFactory factory = new HessianProxyFactory();
    	String epgURL = config.getString("epgEndpoint");
    	if (log.isInfoEnabled()) {
    		log.info("EPG endpoint is " + epgURL);
    	}
    	EPGProvider epg = (EPGProvider) factory.create(EPGProvider.class,epgURL);
    	return epg;
	}
	
	/**
	 * Creates listener, optionally wrapping it in a hession endpoint.
	 */
	private KeywordListener getListener() throws Exception {
		HessianProxyFactory factory = new HessianProxyFactory();
		Configuration config = Config.getConfiguration();
		
		if (log.isInfoEnabled()) {
			log.info("Search endpoint is " + config.getString("searchEndpoint"));
		}
		
		KeywordListener keywordListener = (KeywordListener)factory.
			create(KeywordListener.class, config.getString("searchEndpoint"));
		if (config.getBoolean("userAsyncSender", false)) {
			keywordListener = new SimpleAsychronousKeywordListener(keywordListener);
		}
		return keywordListener;
	}
	
	/* (non-Javadoc)
	 * @see com.knowbout.nlp.keywords.service.CCEventService#startCapture(java.lang.String, java.lang.String)
	 */
	public String startCapture() {
		log.info("startCapture()");
		return SUCCESS;
	}
	
	/* (non-Javadoc)
	 * @see com.knowbout.nlp.keywords.service.CCEventService#captureCCEvent(java.lang.String, java.lang.String)
	 */
	public String captureSentence(CCSentenceEvent event) {
		try {
			if (log.isDebugEnabled()) {
				log.debug("capture sentence(" + event +  ")");
			}
			checkStats();
			KeywordSourceRepository repository = getRepository();
			
			// Check to see if we've had a cold start, and fill in the scheduled program if needed.
			ScheduledProgram scheduledProgram = repository.getCurrentProgram(event.getChannel());
			if (scheduledProgram == null) {
				if (log.isInfoEnabled()) {
					log.info("Attempting to recover scheduled program, must have been a cold start.");
				}
				scheduledProgram = epgProvider.getScheduledProgram(
						event.getLineupID(), event.getChannel(), new Date());
				if (scheduledProgram != null) {
					if (log.isInfoEnabled()) {
						log.info("Setting current program to " + scheduledProgram.getProgramId() + 
								":" + scheduledProgram.getProgramTitle());
					}
					repository.setCurrentProgram(event.getChannel(), scheduledProgram);
				}
				
			}
			
			Program program = new Program(event.getLineupID(), null, event.getChannel(), scheduledProgram.getProgramId());
			
			repository.newElement(program.getChannel(), clean(event.getSentence()));
			int maxTokensToProcess = getConfigInt("maximumNLPTokens", 1000);
			String text = repository.getProgramText(program.getChannel(), maxTokensToProcess);
			Collection<Keyword> keywords = extracter.extract(text.toLowerCase());
			removeStopWordsAndLengthCheck(keywords);
			
			KeywordEvent keywordEvent = null;
			
			List<Keyword> searchTerms = getPreferredSearchTerms(keywords);
			if (Config.getConfiguration().getBoolean("verboseEvents", true)) {
				StringBuilder extraInfo = new StringBuilder();
				extraInfo.append("maximumNLPTokens=").append(maxTokensToProcess);
				extraInfo.append("; maximumSearchTerms=").append(getConfigInt("maximumSearchTerms", 0));
				extraInfo.append("; maximumContextTokens=").append(getConfigInt("maximumContextTokens", 0));
				keywordEvent = new KeywordEvent(program, searchTerms,
						event.getLineupID(), event.getChannel(),
						event.getProgramStartTime(), event.getTimestamp(),
						text, extraInfo.toString()); 
			} else {
				keywordEvent = new KeywordEvent(program, searchTerms,
						event.getLineupID(), event.getChannel(),
						event.getProgramStartTime(), event.getTimestamp());
			}
			
			// This line implies a ton of functionality.
			LuceneIndexer.getInstance().addClosedCaptionEvent(event.getSentence(), keywordEvent, scheduledProgram);
			
			if(areKeywordsChanged(keywordEvent.getProgram(), keywordEvent.getKeywords())) {
				if (log.isInfoEnabled()) {
					log.info("Sending event to keyword listener " + keywordEvent);
				}
				keywordListener.keywordsFound(keywordEvent);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Event for channel " + keywordEvent.getProgram().getChannel() + 
						" did not change, not sending.");
				}
			}
			if (log.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder("AllKeyword=");
				sb.append(keywords);
				sb.append("; keywordEvent.keywords=").append(keywordEvent.getKeywords());
				log.debug(sb);
			}
			return SUCCESS;
		} catch (Throwable e) {
			log.error(e.getMessage() + event, e);
			return FAILURE;
		}
	}
	
	private void checkStats() {
		long timestamp = new Date().getTime();
		String day = getFileDate(timestamp);
		if (!day.equals(currentDay)) {
			if (statsFile != null) {
				statsFile.println("</table></body></html>");
				statsFile.close();
				statsFile = null;
			}
			currentDay = day;
		}
		if (hostname == null) {
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				hostname = "UnknownHost";
			}
		}
		String dirname = documentRoot+"/stats/"+currentDay+"/"+hostname;
		String statsFileName = dirname+"/nlpprocstats.html";
		int interval = 5; // minutes
		try {
			if (statsFile == null) {
				File dir = new File(dirname);
				if ((!dir.exists()) && (!dir.mkdirs())) {
					throw new IOException("Error creating directory "+dirname);
				}
				File file = new File(statsFileName);
				if (file.exists()) {
					statsFile = new PrintStream(new FileOutputStream(statsFileName, true));
					statsFile.println("<tr><td colspan='5'>Restart</td></tr>");
				} else {
					statsFile = new PrintStream(new FileOutputStream(statsFileName));
					String title = "NLP Process (tomcat) status for "+currentDay;
					statsFile.println("<html><head><title>"+title+"</title></head>");
					statsFile.println("<body><h1>"+title+"</h1>");
					statsFile.println("<table border='1'>");
					statsFile.println("<tr>");
					statsFile.println("<th colspan='2'>"+interval+" Minute Intervals</th><th colspan='3'>Memory</th>");
					statsFile.println("</tr>");
					statsFile.println("<tr>");
					statsFile.println("<th>Timestamp</th>");
					statsFile.println("<th>Time</th>");
					statsFile.println("<th>Used</th>");
					statsFile.println("<th>Committed</th>");
					statsFile.println("<th>Max<th>");
					statsFile.println("</tr>");
				}
			}
			if ((timestamp-lastWrite) > (interval * 60 * 1000)) {
				lastWrite = timestamp;
				Calendar cal = Calendar.getInstance();
				cal.setTimeZone(TimeZone.getTimeZone("GMT"));
				cal.setTimeInMillis(timestamp);
				String time = String.format("%1$tH:%1$tM:%1$tS", cal);
				MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
				MemoryUsage memory = memoryBean.getHeapMemoryUsage();
				statsFile.print("<tr>");
				statsFile.print("<td>"+timestamp+"</td>");
				statsFile.print("<td>"+time+"</td>");
				statsFile.format("<td>%,d</td>", memory.getUsed());
				statsFile.format("<td>%,d</td>", memory.getCommitted());
				statsFile.format("<td>%,d</td>", memory.getMax());
				statsFile.println("</tr>");
			}
		} catch (IOException e) {
			log.error("Error opening or writing to "+statsFileName, e);
		}
	}
	
	private String getFileDate(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(timestamp);
		return String.format("%1$tY-%1$tm-%1$td", cal);
	}

	/**
	 * 
	 * @param input
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String clean(String input) {
		List<String> cleaningExpressions = Config.getConfiguration().getList("cleaners.regex", null);
		if (log.isDebugEnabled()) {
			log.debug("Cleaning expression list is " + cleaningExpressions);
		}
		if (cleaningExpressions != null) {
			String cleaned = input;
			for (String cleaningExpression : cleaningExpressions) {
				cleaned = cleaned.replaceAll(cleaningExpression, "");
				if (log.isDebugEnabled() && !input.equals(cleaned)) {
					log.debug("Cleaned the string \"" + input + "\" into \"" + cleaned + "\"");
				}
				if (cleaned.length() < 1) break;
			}
			return cleaned;
		}
		return input;
	}
	
	private void removeStopWordsAndLengthCheck(Collection<Keyword> keywords) {
		List<Keyword> toRemove = new ArrayList<Keyword>();
		Set<String> stopWords = new HashSet<String>(this.getConfigStrings("stopWords.word"));
		for (Keyword kw : keywords) {
			if (!kw.isMultiword() && stopWords.contains(kw.getKeyword())) {
				toRemove.add(kw);
			} else if (!kw.isMultiword() && kw.getKeyword().length() > this.getConfigInt("maximumKeywordLength", 30)) {
				log.warn("Removed excessively long keyword " + kw.getKeyword());
				toRemove.add(kw);
			} else if (kw.isMultiword() && kw.getKeyword().length() > this.getConfigInt("maximumMultiwordKeywordLength", 120)) {
				log.warn("Removed excessively long keyword " + kw.getKeyword());
				toRemove.add(kw);
			}
		}
		if (!toRemove.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("removing stopwords: " + toRemove);
			}
			keywords.removeAll(toRemove);
		}
	}
	
	/**
	 * 
	 * @param program
	 * @param keywords
	 * @return
	 */
	private boolean areKeywordsChanged(Program program, Collection<Keyword> keywords) {
		Collection<Keyword> previous = lastKeywordList.get(program.getChannel());
		lastKeywordList.put(program.getChannel(), keywords);
		if (previous == null) {
			return true;
		} else {
			return !previous.equals(keywords);
		}
	}
	
	/**
	 * All of this class' config params are ints, to made a convenience method for it.
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	private int getConfigInt(String name, int defaultValue) {
		Configuration config = Config.getConfiguration();
		return config.getInt(name, defaultValue); 
	}
	

	private List<String> getConfigStrings(String name) {
		Configuration config = Config.getConfiguration();
		return config.getList(name);
	}
	
	/**
	 * 
	 * @param keywords
	 * @return
	 */
	private List<Keyword> getPreferredSearchTerms(Collection<Keyword> keywords) {
		int maxKeywords = getConfigInt("maximumSearchTerms", 0);
		if (maxKeywords > 0 && maxKeywords < keywords.size()) {
			List<Keyword> l = new ArrayList<Keyword>(maxKeywords);
			Iterator<Keyword> ki = keywords.iterator();
			for (int i = 0; i < maxKeywords; i++) {
				l.add(ki.next());
			}
			return l;
		} else {
			return new ArrayList<Keyword>(keywords);
		}
	}
	
	/**
	 * 
	 */
	public String endCapture() {
		if (log.isDebugEnabled()) {
			log.debug("endCapture()");
		}
		return SUCCESS;
	}
	
	/**
	 * Oversimplified implementation...
	 * @return
	 */
	private KeywordSourceRepository getRepository() {
		return KeywordSourceRepositoryFactory.getKeywordSourceRepository();
	}

	/**
	 * 
	 */
	public String captureXDS(CCXDSEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("capture xds(" + event +  ")");
		}
		//TODO: Change to something intelligent
		if (event.getXDSField() == XDSField.PROGRAM_NAME) {
			String programName = event.getXDSData().getProgramName();
			if (programName != this.programName) {
				//TODO: Change to something intelligent
				// context has changed.  clear NLP buffers
				this.programName = programName;
			}
		}
		return SUCCESS;
	}

	public String captureITVLink(ITVLinkEvent itvLinkEvent) {
		if (log.isDebugEnabled()) {
			log.debug("capture itvlink(" + itvLinkEvent.getITVLink() +  ")");
		}
		return SUCCESS;
	}

	public String captionTypeChanged(CaptionTypeChangeEvent captionTypeChangedEvent) {
		return SUCCESS;
	}

	public String startProgram(ProgramStartEvent programStartEvent) {
		ScheduledProgram scheduledProgram = programStartEvent.getScheduledProgram();
		String channel = scheduledProgram.getChannel().getChannel();
		if (channel != null) {
			if (log.isInfoEnabled()) {
				log.info("Due to program start event, purging storage for channel " + channel);
			}
			KeywordSourceRepository repository = getRepository();
			repository.setCurrentProgram(channel, scheduledProgram);
		}
		return SUCCESS;
	}


}
