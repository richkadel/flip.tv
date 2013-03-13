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
// Copyright (C) 2002 Jason Baldridge and Gann Bierner
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

package com.knowbout.nlp.keywords;

import java.io.*;
import java.util.zip.*;
import opennlp.maxent.*;
import opennlp.maxent.io.*;

/**
 * A keyword detection class that uses a Maxent model.
 * 
 * @author Jake Fear
 */
public class EnglishSearchWordFinderME extends KeywordFinderME {
	
	private static final String modelFile = "data/EnglishNF.bin.gz";
	
	/**
	 * 
	 * @param modelFilename
	 */
	public EnglishSearchWordFinderME(String modelFilename) {
		super(getModel(modelFilename));
	}
	
	/**
	 * 
	 *
	 */
	public EnglishSearchWordFinderME() {
		super(getModel(modelFile));
	}

	/**
	 * Method used to actually load a maxent model.
	 * @param name
	 * @return
	 */
	private static MaxentModel getModel(String name) {
		try {
			MaxentModel model;
			if (modelFile.endsWith("gz")) {
				model = new BinaryGISModelReader(new DataInputStream(
					new GZIPInputStream(EnglishSearchWordFinderME.class
							.getResourceAsStream(name)))).getModel();
			} else {
				InputStream is = EnglishSearchWordFinderME.class.getResourceAsStream(name);
				InputStreamReader reader = new InputStreamReader(is);
				BufferedReader bufferedReader = new BufferedReader(reader);
				PlainTextGISModelReader mr = new PlainTextGISModelReader(bufferedReader);
				model = mr.getModel();
			}
			System.err.println(model.getClass().getName());
			return model;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) throws IOException {
		TrainEval.eval(getModel(modelFile), new FileReader(args[0]),
				new KeywordFinderME());
	}

}
