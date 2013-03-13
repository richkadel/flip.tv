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

import java.io.IOException;
import java.net.MalformedURLException;

import com.appeligo.ccdataweb.CaptionStore;
import com.caucho.hessian.client.HessianProxyFactory;

import junit.framework.TestCase;

public class CaptionStoreTest extends TestCase {
	
	public void testStore() throws IOException {
		HessianProxyFactory factory = new HessianProxyFactory();
		factory.setOverloadEnabled(true);
    	CaptionStore store = (CaptionStore)factory.create(CaptionStore.class,"http://dev.flip.tv/ccdataweb/captionstore");

    	String sentence = store.getSentence("CA04542:DEFAULT", "41", 1172973600000L, 1172973786553L);
    	System.out.println(sentence);
		System.out.println("===================");

    	String sentences[] = store.getSentences("CA04542:DEFAULT", "41", 1172973600000L, 1172973894160L, 1172973922954L);
    	for (String s: sentences) {
    		System.out.println(s);
    	}
		System.out.println("===================");

    	sentences = store.getSentences("CA04542:DEFAULT", "41", 1172988000000L, 1172989392754L, 1172989447508L);
    	for (String s: sentences) {
    		System.out.println(s);
    	}
		System.out.println("===================");

    	sentences = store.getSentences("CA04542:DEFAULT", "41", 1172973600000L, 1172973986951L, 1172974019817L);
    	for (String s: sentences) {
    		System.out.println(s);
    	}
	}
}
