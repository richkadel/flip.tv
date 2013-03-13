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

package com.appeligo.channelfeed.work;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Entity;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.io.SAXReader;

import com.knowbout.cc2nlp.CCSentenceEvent;
import com.knowbout.epg.service.ScheduledProgram;

/**
 * Reads a caption file and sends it through to a caption event processor (like our search indexer).
 * 
 * Note that we should not bother to write caption files since we are reading one.
 * 
 * @author kadel
 *
 */
public class FileReaderThread extends CaptionReaderThread {
	
	private static final Logger log = Logger.getLogger(FileReaderThread.class);
	
    private boolean aborted;
	private int advanceSeconds;
	private String captionFilename;
	private boolean autoAdvance;
	private boolean loop;

	private String ccDocumentRoot;

	public FileReaderThread(String name) {
		super(name);
	}

	public void run() {
		
    	try {
	        SAXReader reader = new SAXReader();
        	Document document;
	        try {
				document = reader.read(new File(ccDocumentRoot+captionFilename));
			} catch (DocumentException e) {
				log.error("Could not open document "+ccDocumentRoot+captionFilename+"; ", e);
				return;
			}
			
    		do {
				//Node startTimeNode = document.selectSingleNode("//meta[@name='StartTime']");
				Node startTimeNode = document.selectSingleNode("//*[name()='meta'][@name='StartTime']");
				long startTime;
				try {
					startTime = Long.parseLong(startTimeNode.valueOf( "@content" ));
				} catch (NumberFormatException e) {
					throw new Error(e);
				}
				
				//Node endTimeNode = document.selectSingleNode("//meta[@name='EndTime']");
				Node endTimeNode = document.selectSingleNode("//*[name()='meta'][@name='EndTime']");
				long endTime;
				try {
					endTime = Long.parseLong(endTimeNode.valueOf( "@content" ));
				} catch (NumberFormatException e) {
					throw new Error(e);
				}
					
				long durationMillis = endTime - startTime;
				
				if (autoAdvance) {
					long durationSeconds = durationMillis / 1000;
					Calendar midnight = Calendar.getInstance();
					midnight.setTime(new Date());
					midnight.setTimeZone(TimeZone.getTimeZone("GMT"));
					int year = midnight.get(Calendar.YEAR);
					int month = midnight.get(Calendar.MONTH);
					int dayOfMonth = midnight.get(Calendar.DAY_OF_MONTH);
					midnight.clear();
					midnight.set(year, month, dayOfMonth);
					long midnightMillis = midnight.getTimeInMillis();
					long secondsSinceMidnight = (System.currentTimeMillis() - midnightMillis) / 1000;
					//int loopsSinceMidnight = (int)(secondsSinceMidnight / durationSeconds);
					advanceSeconds = (int)(secondsSinceMidnight % durationSeconds);
				}
				
				long newStartTime = System.currentTimeMillis() - (advanceSeconds*1000);
				long newEndTime = newStartTime + durationMillis;
				
        		setProgram(newStartTime).setStartTime(new Date(newStartTime));
				
//				List divs = document.selectNodes("/html/body/div");
				List divs = document.selectNodes("/*[name()='html']/*[name()='body']/*[name()='div']");
			
				while ((divs.size() > 0) && (!aborted)){
					Element div = (Element)divs.remove(0);
					List children = div.selectNodes("child::node()");
					while ((children.size() > 0) && (!aborted)){
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
						long timestamp;
						try {
							timestamp = Long.parseLong(a.valueOf( "@name" ));
						} catch (NumberFormatException e) {
							throw new Error(e);
						}
						long offset = timestamp - startTime;
						long delayUntil = newStartTime+offset;
						log.debug("Next sentence in "+((delayUntil-System.currentTimeMillis())/1000)+" seconds");
						while (System.currentTimeMillis() < delayUntil) {
							try {
								Thread.sleep(delayUntil-System.currentTimeMillis());
							} catch (InterruptedException e) {
							}
						}
						
						String speakerChange = null;
						Node afterA = (Node)children.remove(0);
						if (afterA instanceof Element) {
							if (!("span".equals(afterA.getName()))) {
								throw new Error("span expected");
							}
							Element span = (Element)afterA;
							speakerChange = span.getText().replace("&gt;&gt;", "").trim();
							afterA = (Node)children.remove(0);
						}
						
						String sentence;
						if (afterA instanceof Text) {
							Text sentenceNode = (Text)afterA;
							sentence = sentenceNode.getText();
						} else {
							Entity entity = (Entity)afterA;
							sentence = entity.asXML();
						}
						while (children.get(0) instanceof Text || children.get(0) instanceof Entity) {
							if (children.get(0) instanceof Text) {
								Text moreText = (Text)children.remove(0);
								sentence += moreText.getText();
							} else {
								Entity entity = (Entity)children.remove(0);
								sentence += entity.asXML();
							}
						}
						sentence = sentence.trim();
	
						/*
						if (!docOpened) {
							openDoc(newStartTime - SCHEDULE_VARIANCE);
						}
						*/
						//if (program != null) {
						getDestinations().writeSentence(getProgram(), delayUntil,
								speakerChange, sentence);
						//}
					}
				}
				
				if (loop) {
					autoAdvance = false;
					advanceSeconds = 0;
					log.debug("Waiting for end of program in "+((newEndTime-System.currentTimeMillis())/1000)+" seconds");
					while (System.currentTimeMillis() < newEndTime) {
						try {
							Thread.sleep(newEndTime-System.currentTimeMillis());
						} catch (InterruptedException e) {
						}
					}
				}
	    	} while (loop);
    	} catch (Exception e) {
    		log.error("Uncaught exception!", e);
		}
	}

	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}
	
	public int getAdvanceSeconds() {
		return advanceSeconds;
	}

	public void setAdvanceSeconds(int advanceSeconds) {
		this.advanceSeconds = advanceSeconds;
	}

	public boolean isAutoAdvance() {
		return autoAdvance;
	}

	/**
	 * This is mutually exclusive with setAdvanceSeconds().  This will calculate
	 * a loopable start time for the program that is always the same for the duration
	 * of the program, and then advances to the next time.  For instance,
	 * for a 1/2 hour show, if it is now 7:10 or 7:20, this calculates
	 * a start time of 7:00am and advances the captions to the current time.
	 * @param autoAdvance true if we are to do this
	 */
	public void setAutoAdvance(boolean autoAdvance) {
		this.autoAdvance = autoAdvance;
	}

	public boolean isLoop() {
		return loop;
	}
	
	public void setLoop(boolean loop) {
		this.loop = loop;
	}
    
	public String getCaptionFilename() {
		return captionFilename;
	}

	public void setCaptionFileName(String captionFilename) {
		this.captionFilename = captionFilename;
	}

	public String getCcDocumentRoot() {
		return ccDocumentRoot;
	}

	public void setCcDocumentRoot(String ccDocumentRoot) {
		this.ccDocumentRoot = ccDocumentRoot;
	}
}

