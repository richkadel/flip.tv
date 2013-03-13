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

package com.appeligo.ccdataweb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import com.appeligo.util.Utils;

public class CaptionFile {
	
	private static final Logger log = Logger.getLogger(CaptionFile.class);
	private static String documentRoot;
	
	static {
		try {
			documentRoot = Config.getConfiguration().getString("fliptvDocumentRoot");
		} catch (Throwable t) {
			log.error("Couldn't use configuration for documentRoot", t);
		}
	}
	
	private RandomAccessFile document;

	private String lineupId;
	private String callsign;
	private long programStartTime;
	
	private Map<Long,Long> index;
	private List<Long> sortedKeys;
	private String documentFilename;
	private String compressedFilename;
	
	public CaptionFile(String lineupId, String callsign, long programStartTime) throws IOException {
		this.lineupId = lineupId;
		this.callsign = callsign;
		this.programStartTime = programStartTime;
		
		documentFilename = documentRoot+"/"+getFilePathPrefix(lineupId, callsign, programStartTime)+".html";
		compressedFilename = documentRoot+"/"+getFilePathPrefix(lineupId, callsign, programStartTime)+".html.gz";
		
		File documentFile = new File(documentFilename);
		File compressedFile = new File(compressedFilename);
		
		if ((!documentFile.exists()) && (!compressedFile.exists())) {
			documentFilename = documentRoot+"/"+getFilePathPrefix(lineupId, callsign, programStartTime)+".body";
			documentFile = new File(documentFilename);
		}
		
		openDocument();
		
		String indexFilename = documentRoot+"/"+getFilePathPrefix(lineupId, callsign, programStartTime)+".map";
		File indexFile = new File(indexFilename);
		
		if (indexFile.exists() && documentFile.exists() &&
				(indexFile.lastModified() < documentFile.lastModified())) {
			indexFile.delete();
		}
		
		if (indexFile.exists() && compressedFile.exists() &&
				 (indexFile.lastModified() < compressedFile.lastModified())) {
			indexFile.delete();
		}
		
		if (indexFile.exists()) {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFilename));
			try {
				index = (Map<Long,Long>)ois.readObject();
			} catch (ClassNotFoundException e) {
				throw (IOException)new IOException("Unexpected Object class for caption file index").initCause(e);
			}
			ois.close();
		} else {
			createIndex();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(indexFilename));
			oos.writeObject(index);
			oos.close();
		}
	}

	public CaptionFile(String headendId, String lineupDevice, String callsign, long programStartTime) throws IOException {
		this(headendId+":"+lineupDevice, callsign, programStartTime);
	}
	
	public void openDocument() throws IOException {
		if (document == null) {
			File file = new File(documentFilename);
			if (!file.exists()) {
				InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(compressedFilename)));
				OutputStream os = new BufferedOutputStream(new FileOutputStream(documentFilename));
				int ch;
				while ((ch = is.read()) >= 0) {
					os.write(ch);
				}
				is.close();
				os.close();
			}
			document = new RandomAccessFile(documentFilename, "r");
		}
	}
	
	public boolean isDocumentOpened() {
		return document != null;
	}
	
	public void closeDocument() throws IOException {
		document.close();
		document = null;
	}
	
	@Override
	protected void finalize() throws IOException {
		if (isDocumentOpened()) {
			closeDocument();
		}
	}
	
	public static String getFilePathPrefix(String lineupId,
			String callsign, long programStartTime) {
		lineupId = lineupId.replace(':', '-');
		return "captiondb/"+lineupId+"/"+Utils.getDatePath(programStartTime)+"/"+callsign+"/"+programStartTime;
	}
	
	public static String getFilePathPrefix(String headendId, String lineupDevice,
			String callsign, long programStartTime) {
		return getFilePathPrefix(headendId+"-"+lineupDevice, callsign, programStartTime);
	}
	
	public String getFilePathPrefix() {
		return getFilePathPrefix(lineupId, callsign, programStartTime);
	}
	
	public String getSentence(long timestamp) throws IOException {
		if (!isDocumentOpened()) {
			throw new IOException("document not opened");
		}
		Long positionLong = index.get(timestamp);
		if (positionLong == null) {
			return null;
		}
		document.seek(positionLong.longValue());
		String line = document.readLine();
		return parseSentence(line);
	}

	public String[] getSentences(long startTimestamp, long endTimestamp) throws IOException {
		if (startTimestamp > endTimestamp) {
			throw new IllegalArgumentException("startTimestamp "+startTimestamp+
					" must be less than or equal to endTimestamp "+endTimestamp);
		}
		if (!isDocumentOpened()) {
			throw new IOException("document not opened");
		}
		Long positionLong = index.get(startTimestamp);
		if (positionLong == null) {
			if (sortedKeys == null) {
				sortedKeys = new ArrayList<Long>(index.keySet());
				Collections.sort(sortedKeys);
			}
			for (Long key : sortedKeys) {
				if (key.longValue() > startTimestamp) {
					startTimestamp = key.longValue();
					positionLong = index.get(startTimestamp);
					break;
				}
			}
		}
		long position = positionLong.longValue();
		Long endLinePositionLong = index.get(endTimestamp);
		if (endLinePositionLong == null) {
			if (sortedKeys == null) {
				sortedKeys = new ArrayList<Long>(index.keySet());
				Collections.sort(sortedKeys);
			}
			int size = sortedKeys.size();
			for (int i = size-1; i >= 0; i--) {
				Long key = sortedKeys.get(i);
				if (key.longValue() < endTimestamp) {
					endTimestamp = key.longValue();
					endLinePositionLong = index.get(endTimestamp);
					break;
				}
			}
		}
		long endLinePosition = endLinePositionLong.longValue();
		List<String> sentences = new ArrayList<String>();
		long lastPosition;
		document.seek(position);
		do {
			lastPosition = position;
			String line = document.readLine();
			if (line.startsWith("<a name=")) {
				sentences.add(parseSentence(line));
			}
			position = document.getFilePointer();
		} while (lastPosition < endLinePosition);
		if (lastPosition != endLinePosition) {
			throw new IOException("Unexpected position "+lastPosition+
					" did not match expected position of the end line "+endLinePosition);
		}
		String[] rtn = new String[sentences.size()];
		return sentences.toArray(rtn);
	}

	private String parseSentence(String line) {
		int gt = line.indexOf('>');
		if (line.charAt(gt+1) == '<') {
			gt = line.indexOf('>', gt+1);
			gt = line.indexOf('>', gt+1);
		}
		return line.substring(gt+1);
	}
	
	private void createIndex() throws IOException {
		index = new HashMap<Long,Long>();
		long position = 0;
		long lineNumber = 1;
		document.seek(position);
		String line = document.readLine();
		while (line != null) {
			if (line.startsWith("<a name=")) {
				int startQuote = line.indexOf('"');
				int endQuote = line.indexOf('"', startQuote+1);
				long timestamp;
				try {
					timestamp = Long.parseLong(line.substring(startQuote+1, endQuote));
				} catch (NumberFormatException e) {
					throw (IOException)new IOException("Invalid timestamp format on line "+lineNumber).initCause(e);
				}
				index.put(timestamp, position);
			}
			position = document.getFilePointer();
			lineNumber++;
			line = document.readLine();
		}
	}

	public Object getCallsign() {
		return callsign;
	}

	public Object getLineupId() {
		return lineupId;
	}

	public Object getProgramStartTime() {
		return programStartTime;
	}

}

