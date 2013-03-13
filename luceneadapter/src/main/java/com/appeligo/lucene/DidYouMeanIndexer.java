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

package com.appeligo.lucene;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class DidYouMeanIndexer {

	private static final Log log = LogFactory.getLog(DidYouMeanIndexer.class);
	
    public static void createDefaultSpellIndex(String indexDir, String spellDir) throws IOException {
    	
    	String newSpellDir = spellDir+".new";
    	File newSpellDirFile = new File(newSpellDir);
    	if (newSpellDirFile.exists()) {
	    	String[] dirFiles = newSpellDirFile.list();
	    	for (String dirFile : dirFiles) {
	    		File f = new File(newSpellDirFile, dirFile);
	    		if (!f.delete()) {
	    			throw new IOException("Could not delete "+f.getAbsolutePath());
	    		}
	    	}
	    	if (!newSpellDirFile.delete()) {
	    		throw new IOException("Could not delete "+newSpellDirFile.getAbsolutePath());
	    	}
    	}
    	
    	/* This was for the original programIndex, but we found out that stemming was bad, and you get better
    	 * spelling suggestions if you can specify a single field, so we combined them.
		for (String field : new String[]{"text","description","programTitle", "episodeTitle", "credits", "genre"}) {
			createSpellIndex(field, FSDirectory.getDirectory(indexDir), FSDirectory.getDirectory(newSpellDir));
		}
		*/
		
		createSpellIndex("compositeField", FSDirectory.getDirectory(indexDir), FSDirectory.getDirectory(newSpellDir));
		
    	String oldSpellDir = spellDir+".old";
    	File oldSpellDirFile = new File(oldSpellDir);
    	if (oldSpellDirFile.exists()) {
	    	String[] dirFiles = oldSpellDirFile.list();
	    	for (String dirFile : dirFiles) {
	    		File f = new File(oldSpellDirFile, dirFile);
	    		if (!f.delete()) {
	    			throw new IOException("Could not delete "+f.getAbsolutePath());
	    		}
	    	}
	    	if (!oldSpellDirFile.delete()) {
	    		throw new IOException("Could not delete "+oldSpellDirFile.getAbsolutePath());
	    	}
    	}
    	
    	File spellDirFile = new File(spellDir);
    	if (spellDirFile.exists() && !spellDirFile.renameTo(oldSpellDirFile)) {
    		throw new IOException("could not rename "+spellDirFile.getAbsolutePath()+" to "+oldSpellDirFile.getAbsolutePath());
    	}
    	/* there is some small risk here that someone might try to get the spell index when the file isn't there yet */
    	/* I don't know of any way to really synchronize that from this class, and the risk is minor, unlikely, and not catastrophic */
    	spellDirFile = new File(spellDir);
    	if (!newSpellDirFile.renameTo(spellDirFile)) {
    		// What really bugs me is you can't close a SpellChecker, and I think that prevents us from renaming
    		// the spell index directory (at least on Windows Vista), so let's copy the files instead
    		/*
    		throw new IOException("could not rename "+newSpellDirFile.getAbsolutePath()+" to "+spellDirFile.getAbsolutePath());
    		*/
    		if (!spellDirFile.mkdir()) {
    			throw new IOException("Couldn't make directory "+spellDirFile.getAbsolutePath());
    		}
	    	String[] dirFiles = newSpellDirFile.list();
	    	for (String dirFile : dirFiles) {
	    		File f = new File(newSpellDirFile, dirFile);
	    		File toF = new File(spellDirFile, dirFile);
	    		InputStream is = new BufferedInputStream(new FileInputStream(f.getAbsolutePath()));
	    		OutputStream os = new BufferedOutputStream(new FileOutputStream(toF.getAbsolutePath()));
	    		int b;
	    		while ((b = is.read()) != -1) {
	    			os.write(b);
	    		}
	    		is.close();
	    		os.close();
	    		/* I'd like to do this, but the same reason the rename won't work is why this
	    		 * won't work... this current program still has one or more of the files open.
	    		if (!f.delete()) {
	    			throw new IOException("Could not delete "+f.getAbsolutePath());
	    		}
	    	}
	    	if (!newSpellDirFile.delete()) {
	    		throw new IOException("Could not delete "+newSpellDirFile.getAbsolutePath());
	    	}
	    		 */ }
    	}
    }

    public static void createSpellIndex(String field,
            Directory originalIndexDirectory,
            Directory spellIndexDirectory) throws IOException {

        IndexReader indexReader = null;
        try {
            indexReader = IndexReader.open(originalIndexDirectory);
            Dictionary dictionary = new LuceneDictionary(indexReader, field);
            SpellChecker spellChecker = new SpellChecker(spellIndexDirectory);
            spellChecker.indexDictionary(dictionary);
            if (log.isDebugEnabled()) {
				spellChecker = new SpellChecker(spellIndexDirectory); // need to re-open to see it work
	            log.debug("Does 'next' exist in the dictionary? "+spellChecker.exist("next"));
	            StringBuilder sb = new StringBuilder();
	            for (String s : spellChecker.suggestSimilar("noxt", 5, indexReader, "compositeField", true)) {
	            	sb.append(s+", ");
	            }
	            log.debug("Best suggestions for 'noxt': "+sb);
            }
        } finally {
            if (indexReader != null) {
                indexReader.close();
            }
        }
    }
}
