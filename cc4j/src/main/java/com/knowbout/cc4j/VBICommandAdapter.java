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

/**
 * @author Rich Kadel
 * @author $Author: kadel $
 * @version $Rev: 339 $ $Date: 2006-12-04 14:34:49 -0800 (Mon, 04 Dec 2006) $
 */
public class VBICommandAdapter implements VBICommand {

	protected VBICommandAdapter() {
		super();
	}
	
	public void error(VBILine line, String errstr) {
	}

	public void parityError(VBILine line, int c1, int c2) {
		error(line, "Parity error ("+c1+", "+c2+")");
	}

	public void printChar(VBILine line, int c) {
	}

	public void printUnicode(VBILine line, char c) {
	}

	public void updatedXDS(VBILine line, XDSData xds, XDSField change) {
	}

	public void none(VBILine line) {
	}

	public void unknown(VBILine line) {
		error(line, "Unknown VBI command");
	}

	public void pac(VBILine line, int row, int column, boolean underline) {
	}

	public void backgroundColor(VBILine line, int color, boolean translucent) {
	}

	public void midRow(VBILine line, int color, boolean underline) {
	}

	public void resumeCaption(VBILine line) {
	}

	public void backspace(VBILine line) {
	}

	public void alarmOff(VBILine line) {
	}

	public void alarmOn(VBILine line) {
	}

	public void deleteToEndOfRow(VBILine line) {
	}

	public void rollUpCaption(VBILine line, int rows) {
	}

	public void flashOn(VBILine line) {
	}

	public void resumeDirect(VBILine line) {
	}

	public void textRestart(VBILine line) {
	}

	public void resumeText(VBILine line) {
	}

	public void eraseDisplayed(VBILine line) {
	}

	public void carriageReturn(VBILine line) {
	}

	public void eraseNonDisplayed(VBILine line) {
	}

	public void endOfCaption(VBILine line) {
	}

	public void tab(VBILine line, int offset) {
	}

	public void transparentBackground(VBILine line) {
	}

	public void blackBackground(VBILine line, boolean underline) {
	}
}