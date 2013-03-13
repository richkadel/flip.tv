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

import java.util.HashMap;
import java.util.Map;

/**
 * This class holds the values for a typical closed caption
 * vertical blanking interval (VBI) line.  It represents only
 * two bytes of data, but also stores the line number (e.g., for NTSC
 * it is either line 21 (aka field 1) or line 284 (aka field 2).
 * And if a single instance of VBILine is reused continuously for
 * a given VBI stream and line/field, it will maintain state about
 * which channel (1 or 2) is active.  Channel is changed by two-byte
 * control codes, but must be remembered for subsequent characters,
 * and is only valid when processing executeCommand() and after.
 * @author Rich Kadel
 * @author $Author: kadel $
 * @version $Rev: 352 $ $Date: 2006-12-07 02:32:39 -0800 (Thu, 07 Dec 2006) $
 */
public class VBILine {
	
	private static Map<Integer, ExtendedChar> extendedCharMap = new HashMap<Integer, ExtendedChar>();
    private static Map<Character, ModifiedChar> modifiedCharMap = new HashMap<Character, ModifiedChar>();
    
    static {
    	for (ExtendedChar extendedChar : ExtendedChar.values()) {
	    	extendedCharMap.put((extendedChar.getC2(DataChannel.CC1)-0x30), extendedChar);
    	}
    	for (ModifiedChar modifiedChar : ModifiedChar.values()) {
	    	modifiedCharMap.put(modifiedChar.getASCII(), modifiedChar);
    	}
    }
	
	private static int row [] = {
		/* 0 */ 10,			/* 0x1040 */
		/* 1 */ -1,			/* unassigned */
		/* 2 */ 0, 1, 2, 3,		/* 0x1140 ... 0x1260 */
		/* 6 */ 11, 12, 13, 14,		/* 0x1340 ... 0x1460 */
		/* 10 */ 4, 5, 6, 7, 8, 9	/* 0x1540 ... 0x1760 */
	};
	
	private int c1;
	private int c2;
	private int[] channel = {1,1}; // a stateful variable that only changes for non-text commands
	private boolean textMode = false;
	private XDSData xds = new XDSData();
	private boolean processedXDS = false;
	
	/**
	 * line is 21 or 284 for NTSC, and possibly 22 for PAL
	 */
	private int line;
	
	// This method only exists to support a test case.
	void testSetup(){
		textMode=true;
		channel[0]=2;
	}

	/**
	 * Set line to NTSC 21 (field 1) or 284 (field 2)
	 * @param line currently limited to only 21 or 284.  Future versions for PAL
	 * would support line 22 instead of 21, and other changes are likely for
	 * PAL and other standards like SECAM, if supported
	 * @throws IllegalArgumentException if line is not either 21 or 284
	 */
	public void setLine(int line) {
		if ((line != 21) && (line != 284)) {
			throw new IllegalArgumentException(
					"Current implementation is limited to NTSC at this time:\n"+
					"Line must be either 21 or 284.");
		}
		this.line = line;
	}
    
	/**
	 * Set the value of the first unsigned byte of the VBI line
	 * @param c1 the first byte of the VBI line
	 */
	public void setC1(int c1) {
		this.c1 = c1;
		processedXDS = false;
	}
	
	/**
	 * Set the value of the second unsigned byte of the VBI line
	 * @param c2 the second byte of the VBI line
	 */
	public void setC2(int c2) {
		this.c2 = c2;
	}
	
	/**
	 * @param line currently limited to only 21 or 284.  Future versions for PAL
	 * would support line 22 instead of 21, and other changes are likely for
	 * PAL and other standards like SECAM, if supported
	 * @param c1 the first byte of the VBI line
	 * @param c2 the second byte of the VBI line
	 */
	public void setValues(int line, int c1, int c2) {
		this.line = line;
		setC1(c1);
		setC2(c2);
	}
	
	/**
	 * @param dataChannel sets the line/field from the given DataChannel
	 * @param code sets the c1 and c2 bytes from the TwoByteCode, dependent
	 * on the channel code indicated in the DataChannel (1 or 2)
	 */
	public void setValues(DataChannel dataChannel, TwoByteCode code) {
		setValues(dataChannel.getLine(), code.getC1(dataChannel), code.getC2(dataChannel));
	}
	
    /**
     * Calls the given VBICommand's method corresponding to the text or
     * control codes indicated by the values of this object.
     * @param command a given VBICommand that will be called according to
     * the values of the line and control codes or text in this VBILine object
     */
    public void executeCommand(VBICommand command) {
    	
    	if (c1 == 0) {
    		command.none(this);
    		return;
    	}

		if ((c1 | c2) < 0) { // The native code set up the parity bits so this bitwise OR is supposed to work
			command.parityError(this, c1, c2);
			return;
		}
		
		if ((getField() == 2) && (c1 < 0x10)) { // process XDS
//System.out.println("XDS code "+c1+", checksum "+c2);
			if (processedXDS) {
				return;
			}
			if (c1 == 0x0F) {
				XDSField change = xds.endPacket(c2);
				if (change != XDSField.NONE) {
					command.updatedXDS(this, xds, change);
				}
			} else {
				xds.setCode(c1, c2);
			}
			processedXDS = true;
			return;
		}
		
		if (c1 >= 0x20) { // printable character, to be followed by a printable c2
			if ((getField() == 2) && xds.isInPacket()) {
				if (processedXDS) {
					return;
				}
				xds.addCharacters(c1, c2);
				processedXDS = true;
			} else {
				command.printChar(this, c1);
				if (c2 > 0) {
					command.printChar(this, c2);
				}
			}
			return;
		}
		
		if (xds.isInPacket()) {
			if (processedXDS) {
				return;
			}
			xds.interrupt(); // interrupted by non-XDS control codes
			processedXDS = true;
		}
		
    	/* Some common //bit groups. */
    	channel[getField()-1] = ((c1 >> 3) & 1) + 1;
    	int a7 = c1 & 7;
    	int b7 = (c2 >> 1) & 7;
    	boolean underline = (c2 & 1) != 0; /* underline */

    	if (c2 >= 0x40) {
    		/* Preamble Address Codes -- 001 crrr  1ri xxxu */
      
    		int rrrr = (int)(a7 * 2 + ((c2 >> 5) & 1));

    		if ((c2 & 0x10) != 0)
	    		command.pac(this, row[rrrr], (b7*4), underline );
    		else
	    		command.pac(this, row[rrrr], b7, underline );
    		return;
    	}

    	switch (c1 & 7) {
    	case 0:
    		if ((c2 & 0x10) != 0)
    			break;

    		command.backgroundColor(this, b7, underline);
    		return;

    	case 1:
    		if ((c2 & 0x10) != 0) {
    			command.printUnicode(this, extendedChar(c2 & 0x0F));
    		} else {
    			command.midRow(this, b7, underline);
    		}

    		return;

    	case 2: /* ? */
    	case 3: /* ? */
    		break;

    	case 4:
    	case 5:
    		if ((c2 & 0x10) != 0)
    			break;

    		switch (c2 & 0x0F) {
    		case 0:	// RCL
    			textMode = false;
    			command.resumeCaption(this);
    			return;

    		case 1: // BS
    			command.backspace(this);
    			return;

    		case 2: // AOF (Reserved)
    			command.alarmOff(this);
    			return;

    		case 3: // AON (Reserved)
    			command.alarmOn(this);
    			return;

    		case 4: // DER
    			command.deleteToEndOfRow(this);
    			return;

    		case 5: // RU2
    		case 6: // RU3
    		case 7: // RU4
    			textMode = false;
    			command.rollUpCaption(this, ((c2 & 7) - 3));
    			return;

    		case 8: // FON
    			command.flashOn(this);
    			return;

    		case 9: // RDC
    			textMode = false;
    			command.resumeDirect(this);
    			return;

    		case 10: // TR
    			textMode = true;
    			command.textRestart(this);
    			return;

    		case 11: // RTD
    			textMode = true;
    			command.resumeText(this);
    			return;

    		case 12: // EDM
    			command.eraseDisplayed(this);
    			return;

    		case 13: // CR
    			command.carriageReturn(this);
    			return;

    		case 14: // ENM
    			command.eraseNonDisplayed(this);
    			return;

    		case 15: // EOC
    			textMode = false;
    			command.endOfCaption(this);
    			return;
    		}

    		break;

    	case 6: /* reserved */
    		break;

    	case 7:
    		switch (c2) {
    		case 0x21: // TO1
    		case 0x22: // TO2
    		case 0x23: // TO3
    			command.tab(this, (c2 & 3));
    			return;

    		case 0x2D:
    			command.transparentBackground(this);
    			return;

    		case 0x2E:
    		case 0x2F:
    			command.blackBackground(this, underline);
    			return;

    		default: /* ? */
    			break;
    		}

    		break;
    	}

		command.unknown(this);
    }
    
    /**
     * Converts a standard ASCII character to the VBI equivalent
     * UNICODE character, converting ModifiedChar characters if
     * present.
     * @param c the input ASCII character
     * @return the UNICODE, possibly modified, equivalent
     */
    public static char vbiToUnicode(int c) {
        if (c < 0) {
            return 0;
        }

        c &= 0x7F;

        if (c < 0x20) {
            return 0;
        }
        
    	ModifiedChar modifiedChar = modifiedCharMap.get((char)c);
    	if (modifiedChar == null) {
	        return (char)c;
    	} else {
    		return modifiedChar.getUnicode();
    	}
    }
    
    /**
     * Returns a clean ASCII character as a char, without
     * converting to the VBI modified characters.  ITV (Interactive
     * TV) data (URLs) do not use the VBI charset.
     * @param c the input ASCII character
     * @return the character, possibly cleaned up
     */
    public static char vbiToAscii(int c) {
        if (c < 0) {
            return 0;
        }

        c &= 0x7F;

        if (c < 0x20) {
            return 0;
        }
        
        return (char)c;
    }
    
    private char extendedChar(int code) {
    	ExtendedChar extendedChar = extendedCharMap.get(code);
    	if (extendedChar == null) {
    		return 0;
    	} else {
    		return extendedChar.getUnicode();
    	}
    }

	/**
	 * @return 1, 2, 3, or 4, if the DataChannel indicated by the current
	 * field and channel code is either CC1, CC2, CC3, or CC4, respectively,
	 * or TEXT1, TEXT2, TEXT3, or TEXT4.
	 */
	public int getCCNumber() {
		int field = getField();
		int channel = getChannel();
		if (field == 1) {
			if (channel == 1) {
				return 1;
			} else {
				return 2;
			}
		} else {
			if (channel == 1) {
				return 3;
			} else {
				return 4;
			}
		}
	}
	
	/**
	 * @return the value of the first byte of the control code or text
	 */
	public int getC1() {
		return c1;
	}
	
	/**
	 * @return the value of the second byte of the control code or text
	 */
	public int getC2() {
		return c2;
	}
	
	/**
	 * @return the current line number (should be 21 or 284)
	 */
	public int getLineNumber() {
		return line;
	}
	
	/**
	 * @return 1 if line is 21 or 2 if line is 284
	 * @throws IllegalArgumentException if line is not either 21 or 284
	 */
	public int getField() {
		if (line == 21) {
			return 1;
		} else if (line == 284) {
			return 2;
		} else {
			throw new IllegalArgumentException(
					"Current implementation is limited to NTSC at this time:\n"+
					"Line must be either 21 or 284.");
		}
	}
	
	/**
	 * @return if executeCommand has been called with a control code in c1,
	 * this returns the current channel (1 or 2).  Otherwise, it returns
	 * the channel that was indicated by the last control code processed
	 * by executeCommand, or 1 (default upon initialization)
	 * if this VBILine has not yet processed any control codes.
	 */
	public int getChannel() {
    	return channel[getField()-1];
	}
	
	/**
	 * @return true if in Text Mode, initiated by certain control codes
	 * and terminated by others.
	 */
	public boolean isTextMode() {
		return textMode;
	}

	/**
	 * This is mainly for debugging and returns a printable string if
	 * the two bytes (c1 and c2) are representing printable characters.
	 * This may return a string containing the first character followed by
	 * the literal string "null" if c2 is zero (which I believe is allowed).
	 * This does not print two-byte extended characters, which have their
	 * own callback.
	 * @return two characters concatenated together, if they are printable
	 * (not control codes), else null
	 */
	public String getPrintableChars() {
		if (c1 >= 0x20) {
			return vbiToUnicode(c1) +""+ vbiToUnicode(c2);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the current state of the XDS data.
	 * @return the XDS data
	 */
	public XDSData getXDSData() {
		return xds;
	}
}