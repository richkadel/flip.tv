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

package com.knowbout.cc4j;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a VBICommand that can be passed to VBILine executeCommand.  It stores
 * up characters in a buffer and invokes the abstract method sentenceReady()
 * when it has recognized an entire sentence.  It can strip out soundEffects
 * (enclosed in [] or ()).
 * @author Rich Kadel
 * @author $Author: kadel $
 * @version $Rev: 612 $ $Date: 2007-01-28 08:36:05 -0800 (Sun, 28 Jan 2007) $
 */
public abstract class SentenceBuffer extends LineBuffer {
	
	private static Pattern midline = Pattern.compile("[.?!]+\"? +[^ ]");
	private static Pattern endofline = Pattern.compile("[.?!]+\"? *$");
	private static Pattern soundEffect = Pattern.compile(" *[(\\[].*[)\\]] *");
	private static Pattern garbage = Pattern.compile("[\\p{Cntrl}]");
	private static double garbageThreshold = 0.9; /* 90% or better */
    private static Set<String> abbreviationSet;
    
    static {
        abbreviationSet = new HashSet<String>();
        abbreviationSet.add("mr");
        abbreviationSet.add("mrs");
        abbreviationSet.add("ms");
        abbreviationSet.add("dr");
        abbreviationSet.add("sr");
        abbreviationSet.add("col");
        abbreviationSet.add("adm");
        abbreviationSet.add("gen");
        abbreviationSet.add("lt");
        abbreviationSet.add("cpl");
        abbreviationSet.add("cmd");
        abbreviationSet.add("capt");
        abbreviationSet.add("maj");
        abbreviationSet.add("sgt");
        abbreviationSet.add("rev");
        abbreviationSet.add("com");
        abbreviationSet.add("prof");
        abbreviationSet.add("pres");
        abbreviationSet.add("sec");
        abbreviationSet.add("dept");
    }
    
	private String speaker = null;
	private StringBuffer sentence = new StringBuffer();
	private boolean filterOutSoundEffects;
	private int errors;
	
	/**
	 * Creates a SentenceBuffer for CC1, and filters out sound effects in
	 * square brackets or parentheses (e.g., [tires screeching]).
	 */
	protected SentenceBuffer() {
		this(DataChannel.CC1, true);
	}
	
	/**
	 * Creates a SentenceBuffer for the given closed caption channel,
	 * and filters out sound effects if directed.
	 * @param dataChannel is typically either CC1, CC2, CC3, or CC4
	 * @param filterOutSoundEffects removes text in square brackets of parentheses
	 * e.g., [dog barking] or (audience laughing)
	 */
	protected SentenceBuffer(DataChannel dataChannel, boolean filterOutSoundEffects) {
		super(dataChannel);
		this.filterOutSoundEffects = filterOutSoundEffects;
	}
	
	/**
	 * A subclass implements this method to process complete sentences as they
	 * are recognized.
	 * This is called during processing of VBILine.executeCommand(), only if the
	 * given command (in that VBILine) indicates the completion of a sentence,
	 * such as the processing of a period, exclamation point, or question mark.
	 * @param speakerChange null if there is no indication of a speaker change, or
	 * the name of the speaker if given, and the empty string (non-null) if a
	 * speaker change was indicated but the name not given.  Speaker change is
	 * recognized by the double chevrons (>>)
	 * @param sentence The completed sentence in full
	 * @param errors The number of parity errors encountered while processing this
	 * sentence.  Generally, this should be zero.  But parity errors are not
	 * uncommon and usually indicate a corrupt signal.  A sentence with several
	 * parity errors (e.g., one or more for every 10 characters) indicates very
	 * poor performance, and the sentence is probably invalid (should be ignored
	 * and/or discarded). 
	 */
	public abstract void sentenceReady(String speakerChange, String sentence, int errors);
	
	/**
	 * Returns a partially processed sentence, before the end of sentence--such
	 * as a period--is reached.
	 * @return the characters processed thus far in the next sentence, or the
	 * empty string if no characters have been processed since the last
	 * call to sentenceReady, or since initialization.
	 */
	public String peekSentence() {
		return sentence.toString().trim();
	}
	
	/**
	 * For the next sentence, before the end of sentence is reached, if a speaker
	 * change was recognized, this returns the name of the new speaker, or
	 * the empty string if a speakerChange was noted without indicating the
	 * speaker name.
	 * @return the current speaker if the sentence being processed has a speaker
	 * change indicated, the empty string if the speaker name was not specified,
	 * or null if the speaker has not changed as far as we know.
	 */
	public String getCurrentSpeaker() {
		return speaker;
	}
	
	private void resetBuffer() {
		if (filterOutSoundEffects) {
			Matcher m = soundEffect.matcher(sentence);
			if (m.find()) {
				sentence = new StringBuffer(m.replaceAll(" "));
			}
		}
		String trimmed = sentence.toString().trim();
		if ((speaker != null) ||
				(trimmed.length() > 0)) {
			sentenceReady(speaker, trimmed, errors);
		}
		sentence.setLength(0);
		speaker = null;
		errors = 0;
	}

	@Override
	public final void lineReady(String line) {
		double startlen = line.length();
		Matcher m = garbage.matcher(line);
		line = m.replaceAll("");
		if ((line.length() / startlen) < garbageThreshold) {
			resetBuffer();
			return;
		}
		if (filterOutSoundEffects) {
			m = soundEffect.matcher(line);
			line = m.replaceAll(" ");
		}
		line = findSpeaker(line);
		int len = sentence.length();
		if ((len > 0) && (sentence.charAt(len-1) != ' ')) {
			sentence.append(' ');
		}
		int chevron = line.indexOf(">>");
		m = midline.matcher(line);
		boolean foundMidline = m.find();
		while (foundMidline || (chevron >= 0)) {
			int end;
			if ((!foundMidline) || 
					(chevron >= 0) && (chevron < m.end())) {
				end = chevron;
			} else {
				end = m.end()-1;
			}
			sentence.append(line.substring(0, end));
            if (!endIsAbbreviation()) {
				resetBuffer();
            }
			line = line.substring(end);
			line = findSpeaker(line);
			chevron = line.indexOf(">>");
			m = midline.matcher(line);
			foundMidline = m.find();
		}
		
		sentence.append(line);
		
		m = endofline.matcher(line);
		if (m.find() && (!endIsAbbreviation())) {
			resetBuffer();
		}
	}

	private boolean endIsAbbreviation() {
		String trimmed = sentence.toString().trim();
        int len = trimmed.length();
        if (len < 3) {
            return false;
        }
        if (!Character.isLetter(trimmed.charAt(len-2)) ||
                (trimmed.charAt(len-1) != '.')) {
	        return false;
        }
        if (trimmed.charAt(len-3) == '.') {
            return true;
        }
        int lastSpace = trimmed.lastIndexOf(' ');
        String lastWord = trimmed.substring(lastSpace+1,len-1);
        if (abbreviationSet.contains(lastWord.toLowerCase())) {
            return true;
        }
        return false;
    }

    /**
	 * @param line
	 * @return
	 */
	private String findSpeaker(String line) {
		line = line.trim();
		boolean reset = false;
		while (line.startsWith(">>")) {
			resetBuffer();
			reset = true;
			line = line.substring(2).trim();
			speaker = "";
		}
		int colon = line.indexOf(':');
		if (colon >= 0) {
			if (!reset) {
				resetBuffer();
			}
			speaker = line.substring(0,colon).trim();
			if (colon == (line.length()-1)) {
				return "";
			}
			line = line.substring(colon+1);
		}
		return line;
	}

	@Override
	public void parityError(VBILine line, int c1, int c2) {
		errors++;
	}
}
