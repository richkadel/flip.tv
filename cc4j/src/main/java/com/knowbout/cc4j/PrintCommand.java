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
 * Processes commands from VBILine and converts the commands into
 * text printed on System.err.out.
 * @author Rich Kadel
 * @author $Author: kadel $
 * @version $Rev: 339 $ $Date: 2006-12-04 14:34:49 -0800 (Mon, 04 Dec 2006) $
 */
public class PrintCommand implements VBICommand {

	public void parityError(VBILine line, int c1, int c2) {
		System.out.println(
			"Parity error in CC line="+
			line.getLineNumber() + ": " + c1 + " " + c2);
	}

	public void printChar(VBILine line, int c) {
		printUnicode(line, VBILine.vbiToUnicode(c));
	}

	public void printUnicode(VBILine line, char c) {
		System.out.println(
			"CC line="+
			line.getLineNumber() + ": text '" + c + "'");
	}

	public void updatedXDS(VBILine line, XDSData xds, XDSField change) {
		System.out.println("XDS data update. Change is: "+change);
		System.out.println(xds);
	}

	public void endXDSPacket(VBILine line) {
		System.out.println("XDS packet end");
	}
	
	public void none(VBILine line) {
		System.out.println("null");
	}

	public void pac(VBILine line, int row, int column, boolean underline) {
		System.out.println("PAC ch="+
				line.getChannel()+" row="+row+" column="+column+" underline="+ underline);
	}

	public void unknown(VBILine line) {
		System.out.println("unknown");
	}

	public void backgroundColor(VBILine line, int color, boolean translucent) {
		System.out.println("bkg. color ch="+
				line.getChannel()+" color="+color+" translucent="+translucent);
	}

	public void midRow(VBILine line, int color, boolean underline) {
		System.out.println("mid-row ch="+
    				line.getChannel()+" color="+ color+" underline="+ underline);
		
	}

	public void resumeCaption(VBILine line) {
		System.out.println("resume caption ch="+line.getChannel()+" f="+ line.getField());
	}

	public void backspace(VBILine line) {
		System.out.println("backspace ch="+line.getChannel()+" f="+line.getField());
	}

	public void alarmOff(VBILine line) {
		System.out.println("alarm off ch="+line.getChannel()+" f="+line.getField());
	}

	public void alarmOn(VBILine line) {
		System.out.println("alarm on ch="+line.getChannel()+" f="+line.getField());
	}

	public void deleteToEndOfRow(VBILine line) {
		System.out.println("delete to end of row ch="+line.getChannel()+" f="+line.getField());
	}

	public void rollUpCaption(VBILine line, int rows) {
		System.out.println("roll-up caption ch="+line.getChannel()+" f="+line.getField()+" rows="+rows);
	}

	public void flashOn(VBILine line) {
		System.out.println("flash on ch="+line.getChannel()+" f="+line.getField());
	}

	public void resumeDirect(VBILine line) {
		System.out.println("resume direct ch="+line.getChannel()+" f="+line.getField());
	}

	public void textRestart(VBILine line) {
		System.out.println("text restart ch="+line.getChannel()+" f="+line.getField());
	}

	public void resumeText(VBILine line) {
		System.out.println("resume text ch="+line.getChannel()+" f="+line.getField());
	}

	public void eraseDisplayed(VBILine line) {
		System.out.println("erase displayed ch="+line.getChannel()+" f="+line.getField());
	}

	public void carriageReturn(VBILine line) {
		System.out.println("carriage return ch="+line.getChannel()+" f="+line.getField());
	}

	public void eraseNonDisplayed(VBILine line) {
		System.out.println("erase non-displayed ch="+line.getChannel()+" f="+line.getField());
	}

	public void endOfCaption(VBILine line) {
		System.out.println("end of caption ch="+line.getChannel()+" f="+line.getField());
	}

	public void tab(VBILine line, int offset) {
		System.out.println("tab ch="+line.getChannel()+" offs="+ offset);
	}

	public void transparentBackground(VBILine line) {
		System.out.println("transp. bkg. ch="+ line.getChannel());
	}
	
	public void blackBackground(VBILine line, boolean underline) {
		System.out.println("black bkg. ch="+line.getChannel()+" underline="+underline);
	}

	public void error(VBILine line, String errstr) {
		System.out.println("Error: "+errstr);
	}
}
