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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
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
import com.appeligo.lucene.DidYouMeanIndexer;
import com.appeligo.lucene.DocumentUtil;
import com.appeligo.lucene.PorterStemAnalyzer;
import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.Network;
import com.knowbout.epg.service.Program;
import com.knowbout.epg.service.ScheduledProgram;

public class SpellIndexer {

	private File rootDirectory;
	private EPGProvider epg;
	private String indexLocation;
	private Date afterDate;
	private IndexWriter indexWriter;
    private static final Logger log = Logger.getLogger(SpellIndexer.class);
	
	private List<String> networkLineups = new ArrayList<String>();
	private HashMap<String, Network> networks = new HashMap<String, Network>();
	
	public SpellIndexer(File rootDirectory, String indexLocation) throws IOException {
		this.rootDirectory = rootDirectory;
		this.indexLocation = indexLocation;
	}

    
	public static void main(String args[]) throws Exception {
		ConfigurationService.setRootDir(new File("config"));
		ConfigurationService.setEnvName("live");
		ConfigurationService.init();
		if (args.length < 2 || args.length > 3) {
			usage();
			System.exit(-1);
		}
		File file = new File(args[0]);
		if (!file.isDirectory()) {
			usage();
			System.exit(-1);			
		} 
			
		long now = System.currentTimeMillis();		
		try {
			
			DidYouMeanIndexer.createDefaultSpellIndex(args[0], args[1]);
		} catch (IOException e) {
			log.error("Can't create spell index", e);
		}

		long after = System.currentTimeMillis();
		log.info("Processing took " + ((after - now) / (60*1000)) + " minutes to index the programs.");
		
		//log.info("Indexed " + count+ " programs");
	}
    
	private static void usage() {
		log.info("SpellIndexer luceneIndexDir spellIndexDir");		
	}
	
}



