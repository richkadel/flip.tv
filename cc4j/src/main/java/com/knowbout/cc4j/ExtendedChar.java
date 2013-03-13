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
 * @version $Rev: 72 $ $Date: 2006-07-21 09:54:46 +0000 (Fri, 21 Jul 2006) $
 */
public enum ExtendedChar implements TwoByteCode {
	
	REGISTERED_TRADEMARK	( 0x11, 0x30,  0x19, 0x30, '\u00AE'),
	DEGREE					( 0x11, 0x31,  0x19, 0x31, '\u00B0'),
	ONE_HALF				( 0x11, 0x32,  0x19, 0x32, '\u00BD'),
	INVERTED_QUESTION_MARK	( 0x11, 0x33,  0x19, 0x33, '\u00BF'),
	TRADEMARK				( 0x11, 0x34,  0x19, 0x34, '\u2122'),
	CENTS					( 0x11, 0x35,  0x19, 0x35, '\u00A2'),
	POUNDS_STERLING			( 0x11, 0x36,  0x19, 0x36, '\u00A3'),
	MUSIC_NOTE				( 0x11, 0x37,  0x19, 0x37, '\u266A'),
	LC_A_WITH_GRAVE			( 0x11, 0x38,  0x19, 0x38, '\u00E0'),
	TRANSPARENT_SPACE		( 0x11, 0x39,  0x19, 0x39, ' '), // should be a "transparent space" (no background)
	LC_E_WITH_GRAVE			( 0x11, 0x3A,  0x19, 0x3A, '\u00E8'),
	LC_A_WITH_CIRCUMFLEX	( 0x11, 0x3B,  0x19, 0x3B, '\u00E2'),
	LC_E_WITH_CIRCUMFLEX	( 0x11, 0x3C,  0x19, 0x3C, '\u00EA'),
	LC_I_WITH_CIRCUMFLEX	( 0x11, 0x3D,  0x19, 0x3D, '\u00EE'),
	LC_O_WITH_CIRCUMFLEX	( 0x11, 0x3E,  0x19, 0x3E, '\u00F4'),
	LC_U_WITH_CIRCUMFLEX	( 0x11, 0x3F,  0x19, 0x3F, '\u00FB'),
	;

	int chan1c1;
	int chan1c2;
	int chan2c1;
	int chan2c2;
	char unicode;
	
	private ExtendedChar(int chan1c1, int chan1c2, int chan2c1, int chan2c2, char unicode) {
		this.chan1c1 = chan1c1;
		this.chan1c2 = chan1c2;
		this.chan2c1 = chan2c1;
		this.chan2c2 = chan2c2;
		this.unicode = unicode;
	}
	
	/* (non-Javadoc)
	 * @see com.knowbout.cc4j.TwoByteCode#getC1(com.knowbout.cc4j.DataChannel)
	 */
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

	/* (non-Javadoc)
	 * @see com.knowbout.cc4j.TwoByteCode#getC2(com.knowbout.cc4j.DataChannel)
	 */
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
     * @return the printable unicode value of this extended char
     */
    public char getUnicode() {
    	return unicode;
	}
}
