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

package com.appeligo.channelfeed;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.net.SMTPAppender;
import org.apache.log4j.varia.LevelRangeFilter;

import com.appeligo.channelfeed.work.FileWriter;
import com.appeligo.channelfeed.work.NullEPGProvider;
import com.appeligo.channelfeed.work.Destinations;
import com.appeligo.channelfeed.work.VBISocketReaderThread;
import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.cc4j.FrequencyStandard;
import com.knowbout.epg.service.EPGProvider;

public abstract class CaptureApp {

    private static final Logger log = Logger.getLogger(CaptureApp.class);
    
	private String configFile;
	private XMLConfiguration config;
	private EPGProvider epgService;
	private boolean writing;
	private String[] destinationURLs;
	private boolean[] destinationRaws;
	private String headend;
	private String lineupDevice;
	private FrequencyStandard frequencyStandard;
	private String captionDocumentRoot;
	private String previewDocumentRoot;
    
	/**
	 * @param args
	 * @param defaultConfigFile 
	 * @throws ConfigurationException 
	 */
	public CaptureApp(String[] args, String defaultConfigFile) {
		
		try {
    	    // Set up a simple configuration that logs on the console.
    	    PatternLayout pattern = new PatternLayout("%d{ISO8601} %-5p [%-c{1} - %t] - %m%n");
    	    BasicConfigurator.configure(new ConsoleAppender(pattern));
    
    		configFile = defaultConfigFile;
    		if (args.length > 0) {
    			if (args.length == 2 && args[0].equals("-config")) {
    				configFile = args[1];
    			} else {
    				log.error("Usage: java "+getClass().getName()+" [-config <xmlfile>]");
    				throw new Error("Cannot continue");
    			}
    		}
    		
    		config = new XMLConfiguration(configFile);
        
    		configureLogging(config, pattern);
        		
    		String documentRoot = config.getString("documentRoot[@path]");
    		log.info("documentRoot = "+documentRoot);
    		if (documentRoot == null) {
				log.error("Document root is not set (typically it is \"/var/flip.tv\")");
				throw new Error("Cannot continue");
    		} else {
    			documentRoot.trim();
    			if (!documentRoot.endsWith("/")) {
    				documentRoot += "/";
    			}
    		}
    		captionDocumentRoot = documentRoot+"captiondb";
    		previewDocumentRoot = documentRoot+"previews";
        		
			String epgServer = config.getString("epgServer[@url]");
			log.info("epgServer = "+epgServer);
			connectToEPG(epgServer);
        	    
            writing = config.getBoolean("writing", true);
			log.info("writing = "+writing);
        
    		int destinationsCount = config.getList("destinations.destination[@url]").size();
    		destinationURLs = new String[destinationsCount];
			destinationRaws = new boolean[destinationsCount];
			for (int i = 0; i < destinationsCount; i++) {
    			Configuration destination = config.subset("destinations.destination("+i+")");
    			destinationURLs[i] = destination.getString("[@url]");
    			destinationRaws[i] = destination.getBoolean("[@raw]", false);
    			log.info("destination "+i+" = "+destinationURLs[i]+", raw="+destinationRaws[i]);
    		}
        		
    		final String captionPort = config.getString("captionPort[@number]");
    		log.info("captionPort = "+captionPort);
        		
    		if (captionPort != null) {
    			catchCaptions(captionPort);
    		}
        		
    		int providerCount = config.getList("providers.provider.headend").size();
        		
    		for (int i = 0; i < providerCount; i++) {
    			Configuration provider = config.subset("providers.provider("+i+")");
    			headend = provider.getString("headend");
				lineupDevice = provider.getString("lineupDevice");
				frequencyStandard = FrequencyStandard.valueOf(provider.getString("frequencyStandard"));
				log.info(identifyMe());
        
    			if (headend == null || lineupDevice == null || frequencyStandard == null) {
    				log.error("Invalid configuration in: "+identifyMe());
    				throw new Error("Cannot continue");
    			}
        
    			openSources(provider);
    		}
		} catch (ConfigurationException e) {
			log.error("Configuration error in file "+configFile, e);
			throw new Error("Cannot continue");
		} catch (Throwable e) {
			log.error("Unexpected Exception", e);
			throw new Error("Cannot continue");
		}
	}

	private void configureLogging(Configuration config, PatternLayout pattern) {
		Logger.getRootLogger().setLevel(Level.WARN);
		String logEmail = config.getString("logEmail[@address]");
		if ((logEmail == null) || (logEmail.trim().length() == 0)) {
			logEmail = "root@localhost";
		}
		log.info("logEmail = "+logEmail);
		configureFatalLogging(pattern, logEmail);
		
		int loggerCount = config.getList("loggers.logger[@name]").size();
		
		for (int i = 0; i < loggerCount; i++) {
			Configuration logger = config.subset("loggers.logger("+i+")");
			String loggerName = logger.getString("[@name]");
			String loggerLevel = logger.getString("[@level]");
			log.info("Setting logger "+loggerName+" to level "+loggerLevel);
			Logger newlog = Logger.getLogger(loggerName);
			Level level = Level.toLevel(loggerLevel, null);
			if (level == null) {
				log.error("Invalid log level '"+loggerLevel+"' in: "+configFile);
			} else {
				newlog.setLevel(level);
			}
		}
	}

	private void configureFatalLogging(PatternLayout pattern, String logEmail) {
		// Add a fatal email appender
		SMTPAppender mailme = new SMTPAppender();
		LevelRangeFilter fatalFilter = new LevelRangeFilter();
		fatalFilter.setLevelMin(Level.FATAL);
		mailme.addFilter(fatalFilter);
		mailme.setSMTPDebug(true);
		mailme.setSMTPHost("localhost");
		mailme.setTo(logEmail);
		mailme.setFrom(logEmail);
		mailme.setBufferSize(1);
		mailme.setSubject("Fatal error in SendCaptions");
		mailme.setLayout(new SimpleLayout());
		mailme.activateOptions();
		mailme.setLayout(pattern);
		BasicConfigurator.configure(mailme);
	}

	private void catchCaptions(final String captionPort) {
		Thread serverThread = new Thread("Caption Catcher") {
			@Override
			public void run() {
				try {
					int count = 0;
					ServerSocket server = new ServerSocket(Integer.parseInt(captionPort));
					while(true) {
						try {
							count++;
							VBISocketReaderThread vbiSocketReaderThread =
								new VBISocketReaderThread("VBISocketReader "+count+"@"+captionPort,
										server.accept());
							
							Destinations destinations = new Destinations();
							destinations.setCaptionDocumentRoot(captionDocumentRoot);
							destinations.setDestinationURLs(destinationURLs, destinationRaws);
							destinations.setSendXDS(false);
							destinations.setSendITV(false);
							if (writing) {
    		                    destinations.setFileWriter(new FileWriter());
							}
							
		                    vbiSocketReaderThread.setEpgService(getEpgService());
		                    vbiSocketReaderThread.setDestinations(destinations);
		                    
							destinations.connect();
							vbiSocketReaderThread.start();
							
						} catch (MalformedURLException e1) {
							log.error("Exception on a channel", e1);
						} catch (IOException e1) {
							log.error("Exception on a channel", e1);
						}
					}
				} catch (IOException e1) {
					log.error("Exception with server socket", e1);
				} catch (NumberFormatException e1) {
					log.error("Bad port number: "+captionPort, e1);
					return;
				}
			}
		};
		serverThread.start();
	}

	protected abstract void openSources(Configuration provider);
	
	protected Destinations setupDestinations() {
		Destinations destinations = new Destinations();
		destinations.setCaptionDocumentRoot(captionDocumentRoot);
		destinations.setHeadendID(headend);
		destinations.setLineupDevice(lineupDevice);
		destinations.setDestinationURLs(destinationURLs, destinationRaws);
		if (writing) {
            destinations.setFileWriter(new FileWriter());
		}
		return destinations;
	}
	
	/**
	 * This returns a string identifying the CURRENT set of lineup parameters.  NOTE, that
	 * this is only valid for the current lineup as we are looping through the
	 * "providers".  It will change if there are multiple "providers" in the config file.
	 * @return
	 */
	protected String identifyMe() {
		StringBuilder sb = new StringBuilder();
		sb.append(configFile);
		sb.append("    headend="+headend);
		sb.append(", lineupDevice="+lineupDevice);
		sb.append(", frequencyStandard="+frequencyStandard);
		return sb.toString();
	}

	public EPGProvider getEpgService() {
		return epgService;
	}

	public void connectToEPG(String epgServer) {
        HessianProxyFactory factory = new HessianProxyFactory();
        if (epgServer != null) {
        	try {
    			epgService = (EPGProvider)factory.create(EPGProvider.class, epgServer);
    		} catch (MalformedURLException e1) {
    			log.error("Exception on a channel", e1);
    		}
	        if (epgService == null) {
	        	log.error("No EPG Service");
	        }
        } else {
        	epgService = new NullEPGProvider();
        }
	}
        
	/*
    public void setNlpEndpoint(String nlpEndpoint) throws MalformedURLException {
        HessianProxyFactory factory = new HessianProxyFactory();
        if (nlpEndpoint != null) {
	        nlpService = (CCEventService)factory.create(CCEventService.class , nlpEndpoint);
	        if (nlpService == null) {
	        	log.error("No NLP Service");
	        }
        } else {
        	nlpService = new NullCCEventService();
        }
    }
    */

	public String getHeadend() {
		return headend;
	}

	public void setHeadend(String headend) {
		this.headend = headend;
	}

	public String getLineupDevice() {
		return lineupDevice;
	}

	public void setLineupDevice(String lineupDevice) {
		this.lineupDevice = lineupDevice;
	}
	
	public String getLineupID() {
		return headend+"-"+lineupDevice;
	}

	public FrequencyStandard getFrequencyStandard() {
		return frequencyStandard;
	}

	public void setFrequencyStandard(FrequencyStandard frequencyStandard) {
		this.frequencyStandard = frequencyStandard;
	}

	public String getCaptionDocumentRoot() {
		return captionDocumentRoot;
	}

	public void setCaptionDocumentRoot(String captionDocumentRoot) {
		this.captionDocumentRoot = captionDocumentRoot;
	}

	public XMLConfiguration getConfig() {
		return config;
	}

	public String getPreviewDocumentRoot() {
		return previewDocumentRoot;
	}

	public void setPreviewDocumentRoot(String previewDocumentRoot) {
		this.previewDocumentRoot = previewDocumentRoot;
	}
}
