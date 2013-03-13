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

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;

import com.knowbout.epg.service.Credit;
import com.knowbout.epg.service.Network;
import com.knowbout.epg.service.ScheduledProgram;
import com.knowbout.epg.service.StationChannel;

public class DocumentUtil {

	private DocumentUtil() {}
	
	
	public static void addCaptions(Document doc, String captions) {
		doc.add(new Field("text", captions, Field.Store.YES, Field.Index.TOKENIZED));
	}
	
	public static void populateDocument(Document doc, List<ScheduledProgram> programs, Date modified) {
    	// Now add the details of the ScheduledProgram.
		for (ScheduledProgram program : programs) {
	    	if (program.getNetwork() != null) {
	    		Network network = program.getNetwork();
	    		if (network.getStationCallSign() != null) {
	    			doc.add(new Field("lineup-"+program.getLineupId()+"-stationCallSign", network.getStationCallSign(), Field.Store.YES, Field.Index.UN_TOKENIZED));
	    		}
	    		if (network.getAffiliation() != null) {
	    			doc.add(new Field("lineup-"+program.getLineupId()+"-affiliation", network.getAffiliation(), Field.Store.YES, Field.Index.TOKENIZED));

	    		}
	    		if (network.getStationName() != null) {
	    			doc.add(new Field("lineup-"+program.getLineupId()+"-stationName", network.getStationName(), Field.Store.YES, Field.Index.TOKENIZED));
	    		}
	    	}
	    	if (program.getStartTime() != null) {
	    		doc.add(new Field("lineup-"+program.getLineupId()+"-startTime", DateTools.dateToString(program.getStartTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.UN_TOKENIZED));
	    	}
	    	
	    	if (program.getEndTime() != null) {
	    		doc.add(new Field("lineup-"+program.getLineupId()+"-endTime", DateTools.dateToString(program.getEndTime(), DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.UN_TOKENIZED));
	    	}
    		doc.add(new Field("lineup-"+program.getLineupId(), "true", Field.Store.YES, Field.Index.UN_TOKENIZED));
	    	
		}
		ScheduledProgram program = programs != null && programs.size() > 0 ? programs.get(0) : null;
    	if (program != null) {
	    	if (program.getDescriptionWithActors() != null) {
	    		doc.add(new Field("description", program.getDescriptionWithActors(), Field.Store.YES, Field.Index.TOKENIZED));
	    	} else if (program.getDescription() != null) {
	    		doc.add(new Field("description", program.getDescription(), Field.Store.YES, Field.Index.TOKENIZED));
	    	}
	    	
	    	if (program.getProgramId() != null) {
	    		doc.add(new Field("programID", program.getProgramId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
	    	}
	    	   	
	    	if (program.getProgramTitle() != null) {
	    		doc.add(new Field("programTitle", program.getProgramTitle(), Field.Store.YES, Field.Index.TOKENIZED));
	    	}
	    	
	    	if (program.getEpisodeTitle() != null) {
	    		doc.add(new Field("episodeTitle", program.getEpisodeTitle(), Field.Store.YES, Field.Index.TOKENIZED));
	    	}
	    	
	    	if (program.getReducedTitle40() != null) {
	    		doc.add(new Field("reducedTitle40", program.getReducedTitle40(), Field.Store.YES, Field.Index.UN_TOKENIZED));
	    	}
	    	
	    	if (program.getLabel() != null) {
	    		doc.add(new Field("programLabel", program.getLabel(), Field.Store.YES, Field.Index.UN_TOKENIZED));
	    	}
	    	
	    	if (program.getWebPath() != null) {
	    		doc.add(new Field("webPath", program.getWebPath(), Field.Store.YES, Field.Index.UN_TOKENIZED));
	    	}
	    	
	    	if (program.getProgramType() != null) {
	    		doc.add(new Field("programType", program.getProgramType().toString(), Field.Store.YES, Field.Index.TOKENIZED));
	    	}
	    	
			doc.add(new Field("lastModified", DateTools.dateToString(modified, DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.UN_TOKENIZED));
	    	
	    	if (program.getTvRating() != null) {
	    		doc.add(new Field("tvRating", program.getTvRating(), Field.Store.YES, Field.Index.UN_TOKENIZED));
	    	}
	
	    	if (program.getStarRating() > 0) {
	    		double rating = program.getStarRating();
	    		doc.add(new Field("starRating", pad((int)(rating * 1000)), Field.Store.YES, Field.Index.UN_TOKENIZED));
	    	}    	
	
	    	List<Credit> credits = program.getCredits();
	    	StringBuilder sb = new StringBuilder();
	    	for (Credit credit: credits) {
	    		String first = credit.getFirstName();
	    		if (first != null && first.trim().length() > 0) {
	    			sb.append(' ');
	    			sb.append(first);
	    		}
	    		String last = credit.getLastName();
	    		if (last != null && last.trim().length() > 0) {
	    			sb.append(' ');
					sb.append(last);
	    		}
	    	}    	
			doc.add(new Field("credits", sb.toString(), Field.Store.NO, Field.Index.TOKENIZED));
			doc.add(new Field("newEpisode", Boolean.toString(program.isNewEpisode()), Field.Store.YES,Field.Index.UN_TOKENIZED));
			doc.add(new Field("programType", program.getProgramType().toString(), Field.Store.YES,Field.Index.UN_TOKENIZED));
			if (program.getGenreDescription() == null) {
				doc.add(new Field("genre", "", Field.Store.NO, Field.Index.TOKENIZED));
			} else {
				doc.add(new Field("genre", program.getGenreDescription(), Field.Store.NO, Field.Index.TOKENIZED));
			}
    	}
	}
	
	public static void populateCompositeDocument(Document doc, String captions, List<ScheduledProgram> programs) {
		ScheduledProgram program = programs != null && programs.size() > 0 ? programs.get(0) : null;
    	if (program != null) {
	    	if (program.getProgramId() != null) {
	    		doc.add(new Field("programID", program.getProgramId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
	    	}
	    	   	
			StringBuilder compositeField = new StringBuilder();
			
			if (captions != null) {
				compositeField.append(captions);
				compositeField.append(" ");
			}
	    	if (program.getDescriptionWithActors() != null) {
	    		compositeField.append(program.getDescriptionWithActors());
	    		compositeField.append(" ");
	    	} else if (program.getDescription() != null) {
	    		compositeField.append(program.getDescription());
	    		compositeField.append(" ");
	    	}
	    	
	    	if (program.getProgramTitle() != null) {
	    		compositeField.append(program.getProgramTitle());
	    		compositeField.append(" ");
	    	}
	    	
	    	if (program.getEpisodeTitle() != null) {
	    		compositeField.append(program.getEpisodeTitle());
	    		compositeField.append(" ");
	    	}
	    	
			if (program.getGenreDescription() != null) {
				compositeField.append(program.getGenreDescription());
	    		compositeField.append(" ");
			}
			
	    	List<Credit> credits = program.getCredits();
	    	StringBuilder creditsBuffer = new StringBuilder();
	    	for (Credit credit: credits) {
	    		String first = credit.getFirstName();
	    		if (first != null && first.trim().length() > 0) {
	    			creditsBuffer.append(' ');
	    			creditsBuffer.append(first);
	    		}
	    		String last = credit.getLastName();
	    		if (last != null && last.trim().length() > 0) {
	    			creditsBuffer.append(' ');
					creditsBuffer.append(last);
	    		}
	    	}    	
	    	if (creditsBuffer.length() > 0) {
				compositeField.append(creditsBuffer.toString().trim());
	    		compositeField.append(" ");
	    	}
	    		
			if (compositeField.length() > 0) {
	    		doc.add(new Field("compositeField", compositeField.toString().trim(), Field.Store.YES, Field.Index.TOKENIZED));
			}
    	}
	}
	
    private static final DecimalFormat formatter = new DecimalFormat("0000000"); 

    public static String pad(int n) {
          return formatter.format(n);
    }
    
    public static String prettySentence(String sentence) {    	
    	//Could we do this with a regex? Possibly, but I'm not sure.
    	StringTokenizer tokenizer = new StringTokenizer(sentence, ".!?",true);
		StringBuilder sb = new StringBuilder();		
    	Pattern pattern = Pattern.compile("[A-Z][a-z]+");

		while (tokenizer.hasMoreTokens()) {
			String string = tokenizer.nextToken();
	        Matcher matcher = pattern.matcher(string);
	        //If the sentece is not in  normal case, the lower case and add caps.
	        if (!matcher.find()) {
				string = string.toLowerCase();
				boolean hasSpace = false;
				for (int i = 0 ; i < string.length(); i++) {
					char c = string.charAt(i);	
					if (Character.isLetter(c)) {
						if (!hasSpace) {
							sb.append(' ');
						}
						sb.append(Character.toTitleCase(c));
						sb.append(string.substring(i+1));
						break;
					} else {
						if (Character.isSpaceChar(c)) {
							hasSpace = true;
						}
						sb.append(c);
					}
				}
	        } else {
	        	sb.append(string);
	        }
		}
		String results = sb.toString();
		//Upper case I and I'll and I'm and the rest.
		results = results.replaceAll("(\\bi\\b)", "I");
		return results.trim();
    }
    
}
