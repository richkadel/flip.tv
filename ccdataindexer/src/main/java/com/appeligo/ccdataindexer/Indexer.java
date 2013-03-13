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

package com.appeligo.ccdataindexer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Entity;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.appeligo.config.ConfigurationService;
import com.appeligo.lucene.DocumentUtil;
import com.appeligo.lucene.PorterStemAnalyzer;
import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.Network;
import com.knowbout.epg.service.Program;
import com.knowbout.epg.service.ScheduledProgram;

public class Indexer {

	private File rootDirectory;
	private EPGProvider epg;
	private String indexLocation;
	private String compositeIndexLocation;
	private Date afterDate;
	private IndexWriter indexWriter;
	private IndexWriter compositeIndexWriter;
    private static final Logger log = Logger.getLogger(Indexer.class);
	private static HashMap<String, String> channelNumberToCallSign = new HashMap<String, String>();
	static {
		//<!-- Versus -->
		channelNumberToCallSign.put("41", "VERSUS");
		//<!-- A&E -->
		channelNumberToCallSign.put("45", "AETV");
		//<!-- Travel Channel -->
		channelNumberToCallSign.put("70", "TRAV");
		//<!-- Discovery Channel -->
		channelNumberToCallSign.put("44", "DSC");
		//<!-- Learning Channel -->
		channelNumberToCallSign.put("55", "TLC");
		//<!-- Sci Fi Channel -->
		channelNumberToCallSign.put("57", "SCIFI");
		//<!-- CW -->
		channelNumberToCallSign.put("5", "CW");
         //<!-- Food Network -->
 		channelNumberToCallSign.put("51", "FOOD");
        //<!-- ESPN -->
 		channelNumberToCallSign.put("29", "ESPN");
        // <!-- Home & Garden TV -->
 		channelNumberToCallSign.put("53", "HGTV");
        // <!-- History Channel -->
 		channelNumberToCallSign.put("56", "HISTORY");
        //<!-- ABC -->
		channelNumberToCallSign.put("10", "ABC");
        //<!-- CBS -->
		channelNumberToCallSign.put("8", "CBS");
        //<!-- Fox -->
		channelNumberToCallSign.put("6", "FOX");
        //<!-- NBC -->
		channelNumberToCallSign.put("7", "NBC");
	}
	
	private List<String> networkLineups = new ArrayList<String>();
	private HashMap<String, Network> networks = new HashMap<String, Network>();
	private static long lookupTime;
	
	public Indexer(File rootDirectory, String epgUrl, String indexLocation, String compositeIndexLocation) throws IOException {
		this.rootDirectory = rootDirectory;
		this.indexLocation = indexLocation;
		this.compositeIndexLocation = compositeIndexLocation;
        HessianProxyFactory factory = new HessianProxyFactory();
        epg = (EPGProvider)factory.create(EPGProvider.class, epgUrl);
		List<Network> networkList = epg.getNetworks("P-DC");
		for (Network network: networkList) {
			networks.put(network.getStationCallSign(), network);
		}
		networkLineups.add("E-C");
		networkLineups.add("E-DC");
		networkLineups.add("E-S");
		networkLineups.add("M-C");
		networkLineups.add("M-DC");
		networkLineups.add("M-S");
		networkLineups.add("P-C");
		networkLineups.add("P-DC");
		networkLineups.add("P-S");
		networkLineups.add("H-C");
		networkLineups.add("H-DC");
		networkLineups.add("H-S");
	}


	/**
	 * @return Returns the afterDate.
	 */
	public Date getAfterDate() {
		return afterDate;
	}


	/**
	 * @param afterDate The afterDate to set.
	 */
	public void setAfterDate(Date afterDate) {
		this.afterDate = afterDate;
	}

	public boolean openIndex() throws IOException {
		if (indexWriter == null && compositeIndexWriter == null) {
			indexWriter = createIndexWriter();
			compositeIndexWriter = createCompositeIndexWriter();
			return true;
		} else {
			return false;		
		}
	}
	
	public void closeIndex() throws IOException {
		if (indexWriter != null) {
			indexWriter.optimize();
			indexWriter.close();
			indexWriter = null;
		}
		if (compositeIndexWriter != null) {
			compositeIndexWriter.optimize();
			compositeIndexWriter.close();
			compositeIndexWriter = null;
		}
	}

	public int  index() throws IOException {
		openIndex();
		int count = 0;
		try {
			File[] files =  rootDirectory.listFiles();
			List<File> allDays = new ArrayList<File>();
			if (files != null) {
				for (File headend: files) {
					if (headend.isDirectory()) {
                		log.info("including headend "+headend.getName());
                		/*
            			String lineupId = headend.getName();
            			lineupId = lineupId.replace('-', ':');
            			*/
            			File[] days = headend.listFiles();
            			allDays.addAll(Arrays.asList(days));
					}
				}
    			Collections.sort(allDays, new Comparator<File>() {
    
    				/* (non-Javadoc)
    				 * @see java.util.Comparator#compare(T, T)
    				 */
    				public int compare(File o1, File o2) {
    					return o1.getName().compareToIgnoreCase(o2.getName());
    				}
    				
    			});
				for (File day: allDays) {
					if (day.isDirectory()) {
						count += indexDay(day);
					}
				}
			}
		} finally {
			closeIndex();
		}
		return count;
	}

	
	public int indexDay(File day) throws IOException {
		boolean needToClose = openIndex();
		int count = 0;
		try {
			File[] channels = day.listFiles();
			log.info("processing day directory " + day.getName() +" which contains " + channels.length + " channels.");
			if (channels != null) {
				for (File channel: channels) {
					if (channel.isDirectory()) {
						count += indexChannel(channel);
					}
				}
			}
		} finally {
			if (needToClose) {
				closeIndex();
			}
		}
		return count;
	}
	
	
	
	public int indexChannel(File channel) throws IOException {
		log.info("indexing channel "+channel.getName());
		boolean needToClose = openIndex();
		int count = 0;
		try {
			String channelNumber = channel.getName();
			String callSign = channelNumberToCallSign.get(channelNumber);
			if (callSign == null) {
				callSign = channelNumber;
				//log.error("Unable to indentify callsign for channel " +  channelNumber);
				//return 0;
			}
			
			Network network = networks.get(callSign);
			if (network == null) {
				log.error("Unable to indentify network for callsign " +  callSign);
				return 0;
			}
			
			File[] programFiles = channel.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.endsWith(".gz") || name.endsWith(".zip")) {
						return true;
					} else {
						return false;
					}
				}
			});
			if (programFiles != null) {
				for (File program: programFiles) {
					try {
						if (afterDate == null || program.lastModified() > afterDate.getTime()) {
							if (indexProgram(program, network)) {
								count++;
							}
						}
					} catch (Exception e) {
						log.error("Exception on Program File "+program.getAbsolutePath()+
								"\n"+e.getMessage()+"\n"+e.getStackTrace().toString(), e);
					}
				}
			}
			log.info("processed " + count + " programs for channel :" + channelNumber);
		} finally {
			if (needToClose) {
				closeIndex();
			}
		}
		return count;
	}
	
	public boolean indexProgram(File programFile, Network network) throws IOException{
		log.debug("processing file "  + programFile + " for " + network.getStationName());
		boolean needToClose = openIndex();
		StringBuilder captions = new StringBuilder();	
		InputStream is = null;
		try {
			is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(programFile)));

	        SAXReader reader = new SAXReader();
	        reader.setEntityResolver(new ExternalResolver());
	        Document document = null;
	        try {
				document = reader.read(is);
			} catch (DocumentException e) {
				log.warn("Could not open document "+programFile+"; ", e);
				return false;
			}
			
			//Node startTimeNode = document.selectSingleNode("//meta[@name='StartTime']");
			Node startTimeNode = document.selectSingleNode("//*[name()='meta'][@name='StartTime']");
			long startTime;
			try {
				startTime = Long.parseLong(startTimeNode.valueOf( "@content" ));
			} catch (NumberFormatException e) {
				log.warn("Error parsing StartTime "+startTimeNode+"; ", e);
				return false;
			}
			//Node programNode = document.selectSingleNode("//meta[@name='ProgramID']");
			Node programNode = document.selectSingleNode("//*[name()='meta'][@name='ProgramID']");
			String programId = programNode.valueOf("@content");
			programId = updateProgramId(programId);
			//Node endTimeNode = document.selectSingleNode("//meta[@name='EndTime']");
			Node endTimeNode = document.selectSingleNode("//*[name()='meta'][@name='EndTime']");
			long endTime;
			try {
				endTime = Long.parseLong(endTimeNode.valueOf( "@content" ));
			} catch (NumberFormatException e) {
				log.warn("Error parsing endTime "+endTimeNode+"; ", e);
				return false;
			}
							
			//List divs = document.selectNodes("/html/body/div");
			List divs = document.selectNodes("/*[name()='html']/*[name()='body']/*[name()='div']");
		
			while (divs.size() > 0){
				Element div = (Element)divs.remove(0);
				List children = div.selectNodes("child::node()");
				while (children.size() > 0){
					Node a = (Node)children.remove(0);
					while (!("a".equals(a.getName()))) {
						if (children.size() == 0) {
							break;
						}
						a = (Node)children.remove(0);
					}
					if (!("a".equals(a.getName()))) {
						break;
					}
					
					Node afterA = (Node)children.remove(0);
					if (afterA instanceof Element) {
						if (!("span".equals(afterA.getName()))) {
							throw new IOException("span expected... bad data in "+programFile);
						}
						//Don't include this in the captions or should we?
						//Element span = (Element)afterA;
						//captions.append(' ');
						//captions.append(span.getText().replace("&gt;&gt;", "").trim());
						afterA = (Node)children.remove(0);
					}
					
					StringBuilder sentence = new StringBuilder();
					if (afterA instanceof Text) {
						Text sentenceNode = (Text)afterA;
						sentence.append(sentenceNode.getText());
					} else {
						Entity entity = (Entity)afterA;
						sentence.append(entity.asXML());
					}
					/*
					while (children.get(0) instanceof Text) {
						Text moreText = (Text)children.remove(0);
						captions.append(' ');
						captions.append(DocumentUtil.prettySentence(moreText.getText()));
					}
					*/
					while (children.get(0) instanceof Text || children.get(0) instanceof Entity) {
						if (children.get(0) instanceof Text) {
							Text moreText = (Text)children.remove(0);
							sentence.append(moreText.getText());
						} else {
							Entity entity = (Entity)children.remove(0);
							sentence.append(entity.asXML());
						}
					}
					captions.append(DocumentUtil.prettySentence(sentence.toString().trim()));
					captions.append(' ');
				}
			}
			ArrayList<ScheduledProgram> skedulePrograms = new ArrayList<ScheduledProgram>();
			long lookupTimeStart = System.currentTimeMillis();
			for(String lineupId: networkLineups) {
				log.debug("looking for future scheduled program: " + programId +"  on lineup " + lineupId);
				ScheduledProgram skedProg = epg.getNextShowing(lineupId, programId, false, true);
				if (skedProg == null) {
					log.debug("looking for past scheduled program: " + programId +"  on lineup " + lineupId);
					skedProg = epg.getLastShowing(lineupId, programId);				
				}
				log.debug("Sked prog for program: "+ programId + " is null " + (skedProg == null));
					//We Can we fake a scheduled program?
					//Program program = epg.getProgram(programId);
				if (skedProg == null) {
					log.debug("Unable to locate ScheduleProgram for " + programId);
					Program program = epg.getProgram(programId);
					if (program != null) {
						skedProg = new ScheduledProgram();
						skedProg.setNetwork(network);
						skedProg.setDescription(program.getDescription());
						skedProg.setDescriptionWithActors(program.getDescriptionWithActors());
						skedProg.setEndTime(new Date(endTime));
						skedProg.setStartTime(new Date(startTime));
						skedProg.setCredits(program.getCredits());
						skedProg.setGenreDescription(program.getGenreDescription());
						skedProg.setProgramId(program.getProgramId());
						skedProg.setProgramTitle(program.getProgramTitle());
						skedProg.setEpisodeTitle(program.getEpisodeTitle());
						skedProg.setLastModified(program.getLastModified());
						skedProg.setOriginalAirDate(program.getOriginalAirDate());
						skedProg.setRunTime(program.getRunTime());
						skedProg.setScheduleId(0);
						skedProg.setTvRating(program.getTvRating());
						skedProg.setNewEpisode(false);
						skedProg.setLineupId(lineupId);
					}
				}
				if (skedProg != null) {
					skedulePrograms.add(skedProg);
    				if (indexWriter == null) { // probably only doing composite index, so one is all we need
    					break;
    				}
				}
			}
			lookupTime += (System.currentTimeMillis()-lookupTimeStart);
			if (captions.length() > 250 && skedulePrograms.size() > 0) {
				//Delete any old duplicates
				Term term = new Term("programID", skedulePrograms.get(0).getProgramId());
				if (indexWriter != null) {
					indexWriter.deleteDocuments(term);
					//Now insert the new document.
					org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
					DocumentUtil.addCaptions(doc, captions.toString());
					DocumentUtil.populateDocument(doc, skedulePrograms, new Date());
					indexWriter.addDocument(doc);
				}
				
				if (compositeIndexWriter != null) {
					compositeIndexWriter.deleteDocuments(term);
					org.apache.lucene.document.Document compositeDoc = new org.apache.lucene.document.Document();
					DocumentUtil.populateCompositeDocument(compositeDoc, captions.toString().replaceAll("[.!?]* ", " "), skedulePrograms);
					compositeIndexWriter.addDocument(compositeDoc);
				}
				
				log.debug("Adding to index now:" + skedulePrograms.get(0).getProgramId() + " " + skedulePrograms.get(0).getProgramTitle());
				return true;
			} else {
				log.debug("Limited CC data("+captions.length()+" character) or unable to locate EPG date for the program. Not adding this program to the index:" + programId);
				return false;				
			}
	   	} finally {
    		if (is != null) {
    			try {
    				is.close();
    			} catch (IOException e){
    				log.error("Error closing file", e);
    			}
    		}
    		if (needToClose) {
    			closeIndex();
    		}
	   	}
	}

    private IndexWriter createIndexWriter() throws IOException {
    	if (indexLocation == null) {
    		return null;
    	}
        File indexDir = new File(indexLocation);
        if (!indexDir.isDirectory()) {
            log.info("Creating Lucene index directory " + indexDir);
            indexDir.mkdirs();
        }
        IndexWriter indexWriter = null;
        Configuration config = ConfigurationService.getConfiguration("system");
        
        String[] stopWords = PorterStemAnalyzer.ENGLISH_STOP_WORDS;
        if (config != null) {   
        	stopWords = config.getStringArray("stopWords.word");
    	}
        log.info("Using stop words:" + Arrays.asList(stopWords));
        while (indexWriter == null) {
        	try {
        		indexWriter = new IndexWriter(indexDir, new PorterStemAnalyzer(stopWords));
        		indexWriter.setMaxBufferedDocs(1000);
        		indexWriter.setMaxMergeDocs(100);
        	} catch (IOException e) {
        		log.error("Failed to obtain lock for " + indexLocation + ".  Trying again.", e);
        	}
        }
        return indexWriter;
    }
    
    private IndexWriter createCompositeIndexWriter() throws IOException {
    	if (compositeIndexLocation == null) {
    		return null;
    	}
        File indexDir = new File(compositeIndexLocation);
        if (!indexDir.isDirectory()) {
            log.info("Creating Lucene composite index directory " + indexDir);
            indexDir.mkdirs();
        }
        IndexWriter indexWriter = null;
        
        /* NO STEMMING IN COMPOSITE INDEX... THROWS OFF SPELL CHECKER
        Configuration config = ConfigurationService.getConfiguration("system");
        String[] stopWords = PorterStemAnalyzer.ENGLISH_STOP_WORDS;
        if (config != null) {   
        	stopWords = config.getStringArray("stopWords.word");
    	}
        log.info("Using stop words:" + Arrays.asList(stopWords));
        */
        while (indexWriter == null) {
        	try {
        		indexWriter = new IndexWriter(indexDir, new StandardAnalyzer());
        		indexWriter.setMaxBufferedDocs(1000);
        		indexWriter.setMaxMergeDocs(100);
        	} catch (IOException e) {
        		log.error("Failed to obtain lock for " + compositeIndexLocation + ".  Trying again.", e);
        	}
        }
        return indexWriter;
    }
    
    
	public static void main(String args[]) throws Exception {
		ConfigurationService.setRootDir(new File("config"));
		ConfigurationService.setEnvName("live");
		ConfigurationService.init();
		if (args.length < 4 || args.length > 5) {
			usage();
			System.exit(-1);
		}
		File file = new File(args[0]);
		if (!file.isDirectory()) {
			usage();
			System.exit(-1);			
		} 
			
		Date date = null;
		if (args.length == 5) {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			date = format.parse(args[4]);
		}
		long now = System.currentTimeMillis();		
		String epgUrl = args[1];
		String indexLocation = args[2];
		String compositeIndexLocation = args[3];
		if (indexLocation.toLowerCase().trim().equals("null")) {
			indexLocation = null;
		}
		if (compositeIndexLocation.toLowerCase().trim().equals("null")) {
			compositeIndexLocation = null;
		}
		if (indexLocation == null && compositeIndexLocation == null) {
			log.error("At least one of indexLocation or compositeIndexLocation cannot be null.");
			usage();
		}
		Indexer indexer = new Indexer(file, epgUrl, indexLocation, compositeIndexLocation);
		if (date != null) {
			indexer.setAfterDate(date);
		}
		if (date == null) {
			log.info("Processing all files");
		} else {
			log.info("Processing all files after " + date +" sanityCheck " + indexer.getAfterDate());
			
		}
		int count = indexer.index();
		long after = System.currentTimeMillis();
		log.info("Processing took " + ((after - now) / (60*1000)) + " minutes to index the programs.");
		log.info("Time spent looking up scheduled programs in epg: "+(lookupTime/(60*1000))+" minutes");
		
		log.info("Indexed " + count+ " programs");
	}
	

    
	private static void usage() {
		log.info("Indexer rootFile urlToEpg indexLocation compositeIndexLocation [fileDate]");		
		log.info("Where file date is in the yyyy-mm-dd format.");
		log.info("The directory structure under the rootFile is as follows:");
		log.info("root-directory");
		log.info("  +-lineupDir (in format: heandend:device");
		log.info("      +-dateDir (in format yyyy-mm-dd)");
		log.info("          +-channelDir");
		log.info("          +-channelDir");
		log.info("      +-dateDir (in format yyyy-mm-dd)");
		log.info("          +-channelDir");
		log.info("          +-channelDir");
		log.info("  +-lineupDir");
		log.info("      +-dateDir (in format yyyy-mm-dd)");
		log.info("          +-channelDir");
		log.info("          +-channelDir");
		log.info("      +-dateDir (in format yyyy-mm-dd)");
		log.info("          +-channelDir");
		log.info("          +-channelDir");		
	}
	
	private static class ExternalResolver implements EntityResolver {
	    public InputSource resolveEntity(String publicID, String systemID)
	        throws SAXException {
	        if (systemID.equals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd")) {
	            // Return local copy of the copyright.xml file
	            InputSource is = new InputSource("dtd/xhtml1-transitional.dtd");
	            return is;
	        }
	        // If no match, returning null makes process continue normally
	        return null;
	    }
	}
	
	public static String updateProgramId(String programId) {
		//The old length was 12, update it to 14 if that is the case.
		if (programId.length() == 12) {
			StringBuilder sb = new StringBuilder();
			sb.append(programId.substring(0,2));
			sb.append("00");
			sb.append(programId.substring(2,12));
			return sb.toString();
		} else {
			return programId;
		}
	}	
}



