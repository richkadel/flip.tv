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
 * This class represents the standard NTSC closed caption channels, including CC1, CC2, CC3, CC4
 * TEXT1, TEXT2, TEXT3, TEXT4
 * @author Rich Kadel
 * @author $Author: kadel $
 * @version $Rev: 339 $ $Date: 2006-12-04 14:34:49 -0800 (Mon, 04 Dec 2006) $
 */
public enum DataChannel {
	CC1(1, 1, false),
	CC2(1, 2, false),
	CC3(2, 1, false),
	CC4(2, 2, false),
	TEXT1(1, 1, true),
	TEXT2(1, 2, true),
	TEXT3(2, 1, true),
	TEXT4(2, 2, true),
	;
	
	private int field;
	private int channelCode;
	private boolean textMode;
	
	private DataChannel(int field, int channelCode, boolean textMode) {
		this.field = field;
		this.channelCode = channelCode;
		this.textMode = textMode;
	}

	/**
	 * @return the field (1 for Line 21 and 2 for Line 284) for the given DataChannel
	 */
	public int getField() {
		return field;
	}
	
	/**
	 * @return the channel code (1 or 2) for the tiven DataChannel
	 */
	public int getChannelCode() {
		return channelCode;
	}
	
	/**
	 * @return true if in Text Mode
	 */
	public boolean isTextMode() {
		return textMode;
	}

	/**
	 * @return the line corresponding to the current field
	 */
	public int getLine() {
		if (field == 1) {
			return 21;
		} else {
			return 284;
		}
	}
}
