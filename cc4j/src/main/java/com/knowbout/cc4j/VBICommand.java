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
public interface VBICommand {
	
	/**
	 * This is called when there is an error in the stream.
	 * @param errstr
	 */
	public void error(VBILine line, String errstr);
	
	/**
	 * @param line
	 * @param c1
	 * @param c2
	 */
	public void parityError(VBILine line, int c1, int c2);
	
	/**
	 * @param line
	 * @param c The VBI character, which can be converted to UNICODE via VBILine.vbiToUnicode()
	 */
	public void printChar(VBILine line, int c);
	
	/**
	 * @param line
	 * @param c A pre-converted unicode character
	 */
	public void printUnicode(VBILine line, char c);
	
	/**
	 * @param line
	 * @param xds
	 * @param change
	 */
	public void updatedXDS(VBILine line, XDSData xds, XDSField change);
	
	/**
	 * @param line
	 */
	public void none(VBILine line);
	
	/**
	 * @param line
	 */
	public void unknown(VBILine line);
	
	/**
	 * @param line
	 * @param row
	 * @param column
	 * @param underline
	 */
	public void pac(VBILine line, int row, int column, boolean underline);
	
	/**
	 * @param line
	 * @param color
	 * @param translucent
	 */
	public void backgroundColor(VBILine line, int color, boolean translucent);
	
	/**
	 * @param line
	 * @param color
	 * @param underline
	 */
	public void midRow(VBILine line, int color, boolean underline);
	
	/**
	 * BS - Backspace
	 * @param line
	 */
	public void backspace(VBILine line);
	
	/**
	 * RCL - Resume Caption Loading
	 * @param line
	 */
	public void resumeCaption(VBILine line);
	
	/**
	 * Reserved (formerly AOF - Alarm Off)
	 * @param line
	 */
	public void alarmOff(VBILine line);
	
	/**
	 * Reserved (formerly AON - Alarm On)
	 * @param line
	 */
	public void alarmOn(VBILine line);
	
	/**
	 * DER - Delete to End of Row
	 * @param line
	 */
	public void deleteToEndOfRow(VBILine line);
	
	/**
	 * RU2, RU3, and RU4 - Roll-Up Captions-X Rows (2, 3, or 4)
	 * @param line
	 * @param rows
	 */
	public void rollUpCaption(VBILine line, int rows);
	
	/**
	 * FON - Flash On
	 * @param line
	 */
	public void flashOn(VBILine line);
	
	/**
	 * RDC - Resume Direct Captioning
	 * @param line
	 */
	public void resumeDirect(VBILine line);
	
	/**
	 * TR - Text Restart
	 * @param line
	 */
	public void textRestart(VBILine line);
	
	/**
	 * RTD - Resume Text Display
	 * @param line
	 */
	public void resumeText(VBILine line);
	
	/**
	 * EDM - Erase Displayed Memory
	 * @param line
	 */
	public void eraseDisplayed(VBILine line);
	
	/**
	 * CR - Carriage Return
	 * @param line
	 */
	public void carriageReturn(VBILine line);
	
	/**
	 * ENM - Erase Non-Displayed Memory
	 * @param line
	 */
	public void eraseNonDisplayed(VBILine line);
	
	/**
	 * EOC - End of Caption
	 * @param line
	 */
	public void endOfCaption(VBILine line);
	
	/**
	 * TO1, TO2, and TO3 - Tab Offset X Columns (1, 2, or 3)
	 * @param line
	 * @param offset
	 */
	public void tab(VBILine line, int offset);
	
	/**
	 * @param line
	 */
	public void transparentBackground(VBILine line);
	
	/**
	 * @param line
	 * @param underline
	 */
	public void blackBackground(VBILine line, boolean underline);

}
