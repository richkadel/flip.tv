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

package com.knowbout.epg;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ScrollableResults;
import org.hibernate.cfg.Environment;

import com.appeligo.alerts.api.AlertQueue;
import com.appeligo.epg.util.EpgUtils;
import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.epg.entities.Program;
import com.knowbout.epg.processor.Downloader;
import com.knowbout.epg.processor.Parser;
import com.knowbout.epg.processor.ScheduleParser;
import com.knowbout.epg.service.ScheduledProgram;
import com.knowbout.hibernate.HibernateUtil;

public class EPG {
	
	private static final Log log = LogFactory.getLog(EPG.class);
	
	public static void main(String[] args){
		String configFile = "/etc/flip.tv/epg.xml";
		boolean sitemaponly = false;
		if (args.length > 0) {
			if (args.length == 2 && args[0].equals("-config")) {
				configFile = args[1];
			} else if (args.length == 1 && args[0].equals("-sitemaponly")) {
				sitemaponly = true;
			} else {
				System.err.println("Usage: java "+EPG.class.getName()+" [-config <xmlfile>]");
				System.exit(1);
			}
		}
		
		try {
			XMLConfiguration config = new XMLConfiguration(configFile);
			
			HashMap<String, String> hibernateProperties = new HashMap<String, String>();
			Configuration database = config.subset("database");
			hibernateProperties.put(Environment.DRIVER, database.getString("driver"));
			hibernateProperties.put(Environment.URL, database.getString("url"));
			hibernateProperties.put(Environment.USER, database.getString("user"));
			hibernateProperties.put(Environment.PASS, database.getString("password"));
			hibernateProperties.put(Environment.DATASOURCE, null);
			
			HibernateUtil.setProperties(hibernateProperties);
			
			if (!sitemaponly) {
    			//test(config);
    			// Get the server configuration to download the content
    			Configuration provider = config.subset("provider");
    			InetAddress server = InetAddress.getByName(provider.getString("server"));
    			File destinationFolder = new File(provider.getString("destinationFolder"));
    			String username = provider.getString("username");
    			String password = provider.getString("password");
    			String remoteDirectory = provider.getString("remoteWorkingDirectory");
    			boolean forceDownload = provider.getBoolean("forceDownload");
    			Downloader downloader = new Downloader(server, username, password, destinationFolder, remoteDirectory, forceDownload);
    			int count = downloader.downloadFiles();
    			log.info("Downloaded " + count + " files");
    //			int count = 14;
    			if (count > 0) {
    				log.info("Processing downloads now.");
    				//Get the name of the files to process
    				Configuration files = config.subset("files");
    				File headend = new File(destinationFolder, files.getString("headend"));
    				File lineup = new File(destinationFolder, files.getString("lineup"));
    				File stations = new File(destinationFolder, files.getString("stations"));
    				File programs = new File(destinationFolder, files.getString("programs"));
    				File schedules = new File(destinationFolder, files.getString("schedules"));
    				
    
    				Parser parser = new Parser(config, headend, lineup, stations, 
    						programs, schedules);
    				parser.parse();
    				log.info("Finished parsing EPG Data. Invoking AlertQueue service now.");
    				String alertUrl = config.getString("alertUrl");
    				HessianProxyFactory factory = new HessianProxyFactory();
    				AlertQueue alerts = (AlertQueue)factory.create(AlertQueue.class,alertUrl);
    				alerts.checkAlerts();
    				log.info("Updating sitemap");
    				updateSitemap(config.getString("sitemap", "/usr/local/webapps/search/sitemap.xml.gz"));
    				log.info("Exiting EPG now.");
    			} else {
    				log.info("No files were downloaded, so don't process the old files.");
    			}
			} else {
				log.info("Updating sitemap");
				updateSitemap(config.getString("sitemap", "/usr/local/webapps/search/sitemap.xml.gz"));
				log.info("Done updating sitemap");
			}
		} catch (ConfigurationException e) {
			log.fatal("Configuration error in file "+configFile, e);
			e.printStackTrace();
		} catch (UnknownHostException e) {
			log.fatal("Unable to connect to host", e);
			e.printStackTrace();
		} catch (IOException e) {
			log.fatal("Error downloading or processing EPG information", e);
			e.printStackTrace();
		} catch (Throwable e) {
			log.fatal("Unexpected Error", e);
			e.printStackTrace();
		}

	}
	
	private static void updateSitemap(String sitemap) {
		int lastslash = sitemap.lastIndexOf('/');
		String inprogress = sitemap.substring(0,lastslash)+"/inprogress-"+sitemap.substring(lastslash+1);
		String marker = "<!-- EVERYTHING BELOW IS AUTOMATICALLY GENERATED -->";
		String baseurl = null;
		try {
			PrintStream doc = new PrintStream(new GZIPOutputStream(new FileOutputStream(inprogress)));
			BufferedReader orig = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(sitemap))));
			String line = orig.readLine();
			while (line != null) {
				if ((line.indexOf("</urlset>") >= 0) || (line.indexOf(marker) >= 0)) {
					break;
				}
				if (baseurl == null) {
					if (line.indexOf("<loc>") >= 0) {
						int http = line.indexOf("http://");
						int nextslash = line.indexOf("/", http+7);
						baseurl = line.substring(http,nextslash);
					}
				}
				doc.println(line);
				line = orig.readLine();
			}
			doc.println(marker);
			Set<String> teams = new HashSet<String>();
    		HibernateUtil.openSession();
    		try {
    			ScrollableResults scroll = Program.selectAllTeams();
    			while (scroll.next()) {
        			Program program = (Program)scroll.get(0);
    				addToSitemap(doc, baseurl, program);
    				teams.add(program.getSportName()+":"+program.getTeamName());
    			}
    			scroll = Program.selectAllShowsMoviesSports();
    			while (scroll.next()) {
        			Program program = (Program)scroll.get(0);
        			if (program.isSports()) {
        				if (!teams.contains(program.getSportName()+":"+program.getHomeTeamName())) {
            				Program home = Program.selectByTeam(program.getSportName(), program.getHomeTeamName());
            				addToSitemap(doc, baseurl, home);
            				teams.add(home.getSportName()+":"+home.getTeamName());
        				}
        				if (!teams.contains(program.getSportName()+":"+program.getAwayTeamName())) {
            				Program away = Program.selectByTeam(program.getSportName(), program.getAwayTeamName());
            				addToSitemap(doc, baseurl, away);
            				teams.add(away.getSportName()+":"+away.getTeamName());
        				}
        			} else {
        				addToSitemap(doc, baseurl, program);
        			}
    			}
			} finally {
				HibernateUtil.closeSession();
			}
			doc.println("</urlset>");
    		doc.close();
    		orig.close();
    		File origFile = new File(sitemap);
    		File backupFile = new File(sitemap+".bak");
    		File inprogressFile = new File(inprogress);
    		backupFile.delete();
    		if (!origFile.renameTo(backupFile)) {
    			throw new IOException("Could not rename "+origFile+" to "+backupFile);
    		}
    		if (!inprogressFile.renameTo(origFile)) {
    			throw new IOException("Could not rename "+inprogressFile+" to "+origFile);
    		}
		} catch (FileNotFoundException e) {
			log.error("Could not write to "+inprogress, e);
		} catch (IOException e) {
			log.error("IO Exception for "+inprogress+" or "+sitemap, e);
		}
	}

	private static void addToSitemap(PrintStream doc, String baseurl, Program program) {
		doc.println("<url>");
		doc.println("<loc>"+baseurl+program.getWebPath()+"</loc>");
		doc.println("<priority>0.0500</priority>");
		doc.println("</url>");
	}

	private static void test(Configuration config) throws IOException {
		HibernateUtil.openSession();
		Configuration provider = config.subset("provider");
		File destinationFolder = new File(provider.getString("destinationFolder"));
		Configuration files = config.subset("files");
		File schedules = new File(destinationFolder, files.getString("schedules"));
		//Test loading
		
		GZIPInputStream uis = new GZIPInputStream(new FileInputStream(schedules));
		ScheduleParser SkedParser = new ScheduleParser();
		SkedParser.parseSchedule(uis, config);
		
		try {
		EPGProviderService service = new EPGProviderService();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 20);
		//Test date now.
		ScheduledProgram sp = service.getScheduledProgramByNetworkCallSign("P-C", "NBC", new Date());
		System.err.println("IS SP NULL: " + (sp == null));
		if (sp != null) {
			System.err.println("\tSP: "+ sp.getProgramTitle() + "  " + sp.getProgramId());
		}

		sp = service.getScheduledProgramByNetworkCallSign("SDTW-C", "NBC", new Date());
		System.err.println("SDTW- IS SP NULL: " + (sp == null));
		if (sp != null) {
			System.err.println("\tSP: "+ sp.getProgramTitle() + "  " + sp.getProgramId());
		}

		//		System.err.println("SP is :"  +sp.getProgramTitle());
//		try {
//		List<ScheduledProgram> skedProgs = service.getScheduleForShow("P-DC", "Survivorman", new Date(), cal.getTime(), 1);
//		for (ScheduledProgram prog: skedProgs) {
//			System.err.println(" limit 1 SHOW: " + prog.getProgramId()+ " " + prog.getEpisodeTitle() + " starts at " + prog.getStartTime() + " " + prog.getNetwork().getStationCallSign());
//		}
////		sp = service.getScheduledProgram("CA04542:DEFAULT", "41", new Date());
////		System.err.println("SP is :"  +sp.getProgramTitle());
//		skedProgs = service.getScheduleForShow("P-DC", "Survivorman", new Date(), cal.getTime(), 0);
//		for (ScheduledProgram prog: skedProgs) {
//			System.err.println("NO LIMIT SHOW: " + prog.getProgramId()+ " " + prog.getEpisodeTitle() + "(" + prog.getScheduleId()+")"+ " starts at " + prog.getStartTime()+ " " + prog.getNetwork().getStationCallSign());
//		}
//		sp = service.getScheduledProgram("CA04542:DEFAULT", "7", new Date());
//		System.err.println("SP is :"  +sp.getProgramTitle());
//		ScheduledProgram next = service.getNextShowing("CA04542:DEFAULT", "EP6856270007");
//		if (next != null) {
//			System.err.println("Next - " + next.getEpisodeTitle() + " starts at " + next.getStartTime());
//		} else {
//			System.err.println("Unable to find next showing");
//		}
//		ScheduledProgram last = service.getLastShowing("CA04542:DEFAULT", "EP6856270007");
//		if (last != null) {
//			System.err.println("Last - " + last.getEpisodeTitle() + " starts at " + last.getStartTime());
//		} else {
//			System.err.println("Unable to find last showing");
//		}
//		
//		
		} finally {
		HibernateUtil.closeSession();
		}
		if (true) {
			System.exit(-1);
		}
	}
}
