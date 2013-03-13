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
 * Vertical Blanking Interval (VBI) commands that can be inserted into a
 * VBI stream (used primarily for testing).
 * @author Rich Kadel
 * @author $Author: kadel $
 * @version $Rev: 75 $ $Date: 2006-07-22 01:42:31 -0700 (Sat, 22 Jul 2006) $
 */
public enum CommandCode implements TwoByteCode {
	
	RCL	(0x14, 0x20, 0x1C, 0x20, "Resume Caption Loading"),
	BS	(0x14, 0x21, 0x1C, 0x21, "Backspace"),
	AOF	(0x14, 0x22, 0x1C, 0x22, "Alarm Off"), // no longer used)
	AON	(0x14, 0x23, 0x1C, 0x23, "Alarm On"), // no longer used)
	DER	(0x14, 0x24, 0x1C, 0x24, "Delete to End of Row"),
	RU2	(0x14, 0x25, 0x1C, 0x25, "Roll-up, 2 rows"),
	RU3	(0x14, 0x26, 0x1C, 0x26, "Roll-up, 3 rows"),
	RU4	(0x14, 0x27, 0x1C, 0x27, "Roll-up, 4 rows"),
	FON	(0x14, 0x28, 0x1C, 0x28, "Flash On"), // heavily discouraged)
	RDC	(0x14, 0x29, 0x1C, 0x29, "Resume Direct Captioning"),
	TR	(0x14, 0x2A, 0x1C, 0x2A, "Text Restart"),
	RTD	(0x14, 0x2B, 0x1C, 0x2B, "Resume Text Display"),
	EDM	(0x14, 0x2C, 0x1C, 0x2C, "Erase Displayed Memory"),
	CR	(0x14, 0x2D, 0x1C, 0x2D, "Carriage Return"),
	ENM	(0x14, 0x2E, 0x1C, 0x2E, "Erase Nondisplayed Memory"),
	EOC	(0x14, 0x2F, 0x1C, 0x2F, "End Of Caption"), // flip memories)
	TO1	(0x17, 0x21, 0x1F, 0x21, "Tab Offset, 1 column"),
	TO2	(0x17, 0x22, 0x1F, 0x22, "Tab Offset, 2 columns"),
	TO3	(0x17, 0x23, 0x1F, 0x23, "Tab Offset, 3 columns"),
	;

	private int chan1c1;
	private int chan1c2;
	private int chan2c1;
	private int chan2c2;
	private String description;
	
	private CommandCode(int chan1c1, int chan1c2, int chan2c1, int chan2c2,
						String description) {
		this.chan1c1 = chan1c1;
		this.chan1c2 = chan1c2;
		this.chan2c1 = chan2c1;
		this.chan2c2 = chan2c2;
		this.description = description;
	}
	
	/**
	 * Returns the first data byte of the code.
	 * @param dataChannel the data channel (1 or 2)
	 * @return the first unsigned byte (as an int)
	 */
	public int getC1(DataChannel dataChannel) {
		if (dataChannel.getChannelCode() == 1) {
			return chan1c1;
		} else {
			return chan2c1;
		}
	}

	/**
	 * Returns the second data byte of the code.
	 * @param dataChannel the data channel (1 or 2)
	 * @return the second unsigned byte (as an int)
	 */
	public int getC2(DataChannel dataChannel) {
		if (dataChannel.getChannelCode() == 1) {
			return chan1c2;
		} else {
			return chan2c2;
		}
	}

	/**
	 * @return a descriptive name for the command
	 */
	public String getDescription() {
		return description;
	}
}
