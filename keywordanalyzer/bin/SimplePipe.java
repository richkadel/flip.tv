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

///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2001 Artifactus Ltd
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.jdom.Element;

import opennlp.common.*;

/**
 * A simple example of how to set up a pipeline.
 *
 * @author      Jason Baldridge
 * @version     $Revision: 1.14 $, $Date: 2002/03/12 12:51:20 $
 */
public class SimplePipe {

    private static String readFile(String fileName) throws IOException {
    	FileReader fileReader = new FileReader(fileName);
    	BufferedReader bufferedReader = new BufferedReader(fileReader);
    	String line;
    	StringBuilder buffer = new StringBuilder();
    	while ((line = bufferedReader.readLine()) != null) {
    		buffer.append(line).append('\n');
    	}
    	return buffer.toString();
    }
    
    public static void main (String[] args) throws Exception {
	String toProcess;
	String s1 = "First, it's a sentence with John Smith and the date 16/01/74 in it." + 
		" Now, this is another one written on Monday.\n\nFinally, this is a sentence in a new paragraph.";
	String s2 = "The Grok project is dedicated to developing a large collection of basic tools " + 
		"for use in natural language software. A particularly important aspect of Grok is " + 
		"that its natural language modules should not be ad hoc and should instead follow " + 
		"specific guidelines, or interfaces, so that they may be freely exchanged with other " + 
		"modules of the same type. To this goal, Grok provides a library of modules that " + 
		"implement the interfaces specified by OpenNLP.";
	
	String s3 = "Finally, this is a sentence in a new paragraph.";

	String s4 = "First, here is a sentence with Jake Fear, not some URL like http://jakarta.apache.org/. Now, this is another one written on Monday.";
	if (args.length == 0)
	    toProcess = s1 + "\n\n" + s4 + "\n\n" + s2;
	else
	    toProcess = readFile(args[0]);

	String[] ppLinks = {
	    //"SimpleLink"
		"opennlp.grok.preprocess.namefind.WebStuffDetector",
		"opennlp.grok.preprocess.sentdetect.EnglishSentenceDetectorME",
		"opennlp.grok.preprocess.tokenize.EnglishTokenizerME",
		"opennlp.grok.preprocess.postag.EnglishPOSTaggerME",
		//"opennlp.grok.preprocess.mwe.EnglishFixedLexicalMWE",
		"opennlp.grok.preprocess.namefind.EnglishNameFinderME",
		"com.knowbout.nlp.keywords.EnglishSearchWordFinderME",
		//"opennlp.grok.preprocess.cattag.EnglishCatterME",
        //"opennlp.grok.preprocess.chunk.EnglishChunkerME",
	}; 

	Pipeline pipe = null;
	try {
	    pipe = new Pipeline(ppLinks);
	    opennlp.common.xml.NLPDocument doc = pipe.run(toProcess);
	    List nameTokens = doc.getTokenElementsByType("name");
	    for (Object o : nameTokens) {
	    	Element e = (Element)o;
	    	System.out.println(e.getChildText("w"));
	    }
	    System.out.println(doc.toXml());
	    System.out.println(doc.toString());
	} catch (PipelineException ple) {
	    System.out.println("Pipeline error: " + ple.toString());
	    System.exit(0);
	}
    }
}
