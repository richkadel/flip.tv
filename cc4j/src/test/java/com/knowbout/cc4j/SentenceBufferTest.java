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

import junit.framework.TestCase;

public class SentenceBufferTest extends TestCase {
	
	static int phase = 0;
	
	public void testStringReady() {
		SentenceBuffer command = new SentenceBuffer() {

			@Override
			public void sentenceReady(String speakerChange, String sentence, int errors) {
				if (speakerChange != null) {
					System.out.print(speakerChange+": ");
				}
				System.out.println("'"+sentence+"'");
				assertTrue(errors == 0);
				phase++;
				switch (phase) {
				case 1:
					assertTrue(speakerChange == null);
					assertTrue(sentence.equals(
							"Now is the time for all good men to come to the aid of their country."
							));
					break;
					
				case 2:
					assertTrue(speakerChange == null);
					assertTrue(sentence.equals(
							"Four score and seven years ago, our forefathers brought forth beer!"
							));
					break;
					
				case 3:
					assertTrue(speakerChange.equals(""));
					assertTrue(sentence.equals(
							"Chips!"
							));
					break;
					
				case 4:
					assertTrue(speakerChange == null);
					assertTrue(sentence.equals(
							"And football!!"
							));
					break;
					
				case 5:
					assertTrue(speakerChange == null);
					assertTrue(sentence.equals(
							"Are you ready for some U.S. football Prof. Snape, Mr. Smith and Ms. Jones?!"
							));
					break;
					
				case 6:
					assertTrue(speakerChange == null);
					assertTrue(sentence.equals(
							"Are you ready for Monday Night?!"
							));
					break;
					
				case 7:
					assertTrue(speakerChange.equals(""));
					assertTrue(sentence.equals(
							"I am"
							));
					break;
					
				case 8:
					assertTrue(speakerChange.equals(""));
					assertTrue(sentence.equals(
							"I am too."
							));
					break;
					
				case 9:
					assertTrue(speakerChange.equals("Bob"));
					assertTrue(sentence.equals(
							"Well done."
							));
					break;
					
				case 10:
					assertTrue(speakerChange == null);
					assertTrue(sentence.equals(
							"I'm ready."
							));
					break;
					
				case 11:
					assertTrue(speakerChange.equals("Joe"));
					assertTrue(sentence.equals(
							"OK."
							));
					break;
					
				case 12:
					assertTrue(speakerChange == null);
					assertTrue(sentence.equals(
							"Me too"
							));
					break;
					
				case 13:
					assertTrue(speakerChange.equals(""));
					assertTrue(sentence.equals(""));
					break;
					
				case 14:
					assertTrue(speakerChange.equals(""));
					assertTrue(sentence.equals(
							"New speaker, new sentence."
							));
					break;
					
				case 15:
					assertTrue(speakerChange.equals(""));
					assertTrue(sentence.equals(
							"OK now."
							));
					break;
					
				case 16:
					assertTrue(speakerChange == null);
					assertTrue(sentence.equals(
							"OK now you swine."
							));
					break;
					
				case 17:
					assertTrue(speakerChange.equals("Andy"));
					assertTrue(sentence.equals(
							"--"
							));
					break;
					
				case 18:
					assertTrue(speakerChange.equals("Len"));
					assertTrue(sentence.equals(
							"SPEAK FOR YOURSELF."
							));
					break;
					
				default:
					fail("unintended phase");
				}
			}
		};
		
		command.lineReady("Now is the time for");
		command.lineReady("all good men to come");
		command.lineReady("to the aid of");
		command.lineReady("their country.  Four score");
		command.lineReady("and [screeching tires] seven years ago, ");
		command.lineReady("our forefathers");
		command.lineReady("[gun shot]");
		command.lineReady("brought forth beer! >> Chips! And football!!");
		command.lineReady("Are you ready for some U.S. football Prof. Snape, Mr. Smith and Ms. Jones?! Are you");
		command.lineReady("ready for Monday Night?!");
		command.lineReady("[audible laughter]");
		command.lineReady(">> I am");
		command.lineReady(">> I am too.");
		command.lineReady(">> Bob: Well done.  I'm");
		command.lineReady("ready.");
		command.lineReady(">> Joe: OK.  Me too >>");
		command.lineReady(">> New speaker, new sentence.");
		command.lineReady("[Sound of");
		command.lineReady("squealing pigs]");
		command.lineReady(">> OK now.");
		command.lineReady("\u0018OK now \u0018you \u0018swine\u0018.");
		command.lineReady("\u0018OK now you swine.");
		command.lineReady("\u0018OK now.");
		command.lineReady(">> Andy: -- >> Len: SPEAK FOR");
		command.lineReady("YOURSELF.");
		command.lineReady("Bob: See you: Monday");
		assertTrue(command.getCurrentSpeaker().equals("Bob"));
		assertTrue(command.peekSentence().equals("See you: Monday"));
	}

}
