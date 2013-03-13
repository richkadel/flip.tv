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
public enum ModifiedChar {
	
	LC_A_ACUTE		('*',		'\u00E1'),
	LC_E_ACUTE		('\\',		'\u00E9'),
	LC_I_ACUTE		('^',		'\u00ED'),
	LC_O_ACUTE		('_',		'\u00F3'),
	LC_U_ACUTE		('`',		'\u00FA'),
	LC_C_CEDILLA	('{',		'\u00E7'),
	DIVISION		('|',		'\u00F7'),
	UC_N_TILDE		('}',		'\u00D1'),
	LC_N_TILDE		('~',		'\u00F1'),
	SOLID_BLOCK		('\u007F',	'\u2588'),
	;

    char ascii;
	char Unicode;
	
	private ModifiedChar(char origASCII, char newUnicode) {
		this.ascii = origASCII;
		this.Unicode =  newUnicode;
	}
	
	/**
	 * Returns the ascii value that is sent via VBI, but intended to be
	 * interpreted as a different Unicode value.
	 * @return the original ascii value that is modified for VBI.
	 */
	public char getASCII() {
		return ascii;
	}

	/**
	 * Returns the Unicode value that should be displayed when the associated
	 * ASCII value is read from VBI.
	 * interpreted as a different Unicode value.
	 * @return the new Unicode value to display
	 */
	public char getUnicode() {
		return Unicode;
	}
}
