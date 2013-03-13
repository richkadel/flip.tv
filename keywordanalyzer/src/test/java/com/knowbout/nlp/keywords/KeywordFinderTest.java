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

package com.knowbout.nlp.keywords;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationFactory;
import org.apache.commons.configuration.XMLConfiguration;

import com.knowbout.keywords.listener.Keyword;
import com.knowbout.keywords.listener.Program;
import com.knowbout.nlp.keywords.persistence.KeywordSourceRepository;
import com.knowbout.nlp.keywords.persistence.KeywordSourceRepositoryFactory;
import com.knowbout.nlp.keywords.util.Config;
import com.knowbout.nlp.keywords.util.TextUtil;

import junit.framework.TestCase;

public class KeywordFinderTest extends TestCase {

	private static final String SAMPLE = 
		"We all have a big picture. What's yours 0 regions. " + 
		"that's the power of everyday confidence.  Okay. Good morning. How are you go " +
		"it's only tuesday. We are glad to have you around. I'm robin meade. This is cnn " +
		"news. away we go checking out the top stories.  I heard a rumbling. The wall " +
		"swelled out a bit. Then the top of the building just collapsed in.  That man"  +
		"was lucky to escape a building collapse in central missouri. Two floors of the " +
		"three-story building came crashing down last night as members of an elks lodge " +
		"sat down to dinner. At one point, ten people were trapped inside. Almost " +
		"everyone has been pulled to safety.  Some 450 detainees at Iraq's Abu Ghraib " +
		"prison are free this morning. Iraq's national security advisor says none of the " +
		"men had been convicted of a crime.  They were released under the prime minister's " +
		"reconciliation plan. The plan calls for a quarter million prisoners released. " +
		"but the prime minister says that won't include terrorists.  The annual cost " +
		"of keeping U.S. troops properly equipped in iraq and afghanistan is set to " +
		"triple. That's what army and marine corps leaders are expected to tell congress " +
		"today. They estimate it'll now cost between 450 12 billion and 12 13 billion a " +
		"year to repair, replace and upgrade equipment. The army's tab for next year is " +
		"expected to be about 13 17 billion.  Andrea yates' retrial starts its second " +
		"day today. During yesterday's opening statements, the prosecution said yates " +
		"knew her actions were wrong when she drowned her five kids in a bathtub in 2001 " +
		", but her defense said she'd been taken off anti-psychotic drugs and that led to " +
		"killings. Yates has pleaded not guilty by reason of insanity as she did in her " +
		"2002 murder trial.  Water overran a major tunnel into washington , d.c. " +
		"forced to ditch their cars and wade out. More car woes for drivers in northern " +
		"virginia. Mud and water cover this entire hotel garage.  We couldn't believe " +
		"how fast and furious and how absolutely devastating that was.  Reporter: " +
		"rescue teams raced into action pulling this man out of floodwaters in maryland. " +
		"Why was you out there 48  Swimming in it.  You lost your mind  " +
		"reporter: City systems from maryland to albany, new york could not handle the " +
		"rainfall from the tropical weather system stalled over the northeast. The " +
		"national weather service warns more rain could hit the region again. Bad news " +
		"for delaware and the town of seaford where a wal-mart looked like an island in " +
		"the lake and city crews evacuated hundreds of residents. Flash floods have " +
		"already overrun two dams. The federal government is open but nonessential " +
		"workers have been told they can take unscheduled leave. This area is covers and " +
		"it's just started to pour out here.  All right a little bit more on the way. " +
		"thank you very much kyung lah .  rescuers have pulled seven people out of a " +
		"collapsed building in clinton, missouri . The roof caved in last night. That " +
		"caused two floors to collapse. Three people are still trapped. About 50 people " +
		"were inside the building when it gave way. Most were at an elks club dinner on " +
		"the second floor. The town of clinton has less than 10,000 people. The mayor " +
		"says this accident has affected everyone.  This is the worst nightmares for " +
		"myself and our community.  we have a loving community. These are all our " +
		"folks. And every life in this community is touched by this.  Some of the " +
		"people who were trapped inside the building were able to talk to rescuers on " +
		"their cell phones. At least 11 people were taken to the hospital.  president " +
		"bush and others in washington are not happy about reports on a secret treasury " +
		"program to track the financial records of terror suspects . Three major " +
		"newspapers carried that story. mr. bush says they're making it harder to win the " +
		"on terror.  The disclosure of this program is disgraceful. We are at war with " +
		"a bunch of people who want to hurt the united states of america. And for people " +
		"to leak that program and for a newspaper to publish it does great harm to the " +
		"united states of america.";
	
	public KeywordFinderTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public KeywordFinderTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public void setUp() throws Exception {
		Configuration config = new XMLConfiguration("src/main/webapp/WEB-INF/config/base/system.xml");
		Config.setConfiguration(config);
	}
	
	public void testConstructKeywordExtracter() throws Exception {
		// Construction the KeywordExtracter to make sure this will work
		// fine in production environment.  
		KeywordExtracter test = new KeywordExtracter();
	}
	
	public void testNLPToolsSentence() throws Exception {
		Extractor extracter = new KeywordExtracter();
		Collection<Keyword> keywords = extracter.extract(SAMPLE.toUpperCase());
		
		for (Keyword kw : keywords) {
			System.out.print(kw.getType() + ":" + kw.getKeyword());
			System.out.println();
		}
	}
	
	public void testRepository() throws Exception {
		KeywordSourceRepository repo = KeywordSourceRepositoryFactory.getKeywordSourceRepository();
		Program p1 = new Program("ABC", null, "2", "ABC-2");
		Program p2 = new Program("DEF", null, "3", "DEF-2");
		repo.newElement(p1.getChannel(), "text one");
		repo.newElement(p1.getChannel(), "text two");
		repo.newElement(p1.getChannel(), "three");
		repo.newElement(p2.getChannel(), "brainless drivel");
		assertEquals("text one text two three", repo.getProgramText(p1.getChannel()));
		
		// Now make sure tokens are rolling out as they get old...
		for (int i = 0; i < 1500; i++) {
			repo.newElement(p1.getChannel(), "three" + i);
		}
		String text = repo.getProgramText(p1.getChannel());
		List<String> tokens = TextUtil.tokenize(text);
		//assertEquals(Config.getConfiguration().getInt("maximumContextTokens", 1000), tokens.size());
	}

}
