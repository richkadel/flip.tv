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
 * Captures VBI lines ending with a carriage return or similar control
 * code, and calls lineReady() on each line processed.
 * @author Rich Kadel
 * @author $Author: kadel $
 * @version $Rev: 591 $ $Date: 2007-01-25 16:51:22 -0800 (Thu, 25 Jan 2007) $
 */
public abstract class LineBuffer extends VBICommandAdapter {
	
	private StringBuffer buffer = new StringBuffer();
	private DataChannel dataChannel;
	private ITVLink itvLink;
	private StringBuffer itvLinkBuffer;
	
	/**
	 * Creates a LineBuffer for for data channel CC1, for
	 * reading, buffering, and then notifying of
	 * the completion of a displayable line of closed caption
	 * (usually terminated by a carriage return.
	 */
	protected LineBuffer() {
		this(DataChannel.CC1);
	}
	
	/**
	 * Creates a LineBuffer for for the given data channel, for
	 * reading, buffering, and then notifying of
	 * the completion of a displayable line of closed caption
	 * (usually terminated by a carriage return.
	 */
	protected LineBuffer(DataChannel dataChannel) {
		this.dataChannel = dataChannel;
	}
	
	/**
	 * Invoked on a subclass when the line has been fully read.
	 * @param s the line
	 */
	public abstract void lineReady(String s);
	
	private void resetBuffer() {
		if (buffer.length() > 0) {
			lineReady(buffer.toString());
		}
		buffer.setLength(0);
		itvLink = null;
		itvLinkBuffer = null;
	}
	
	private void addSpace() {
		int buflen = buffer.length();
		if ((buflen > 1) &&
			(buffer.charAt(buflen-1) != ' ')) {
			buffer.append(' ');
		}		
	}
	
	public boolean isSelectedMode(VBILine line) {
		if (line.getChannel() == dataChannel.getChannelCode() &&
				line.getField() == dataChannel.getField() &&
				line.isTextMode() == dataChannel.isTextMode()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * If a line is currently in process, this returns the characters processed
	 * so far, or the empty string if none.
	 * @return the most recently collected characters in the line
	 */
	public String peek() {
		return buffer.toString();
	}
	
	@Override
	public void parityError(VBILine line, int c1, int c2) {
		if (isSelectedMode(line)) {
			error(line, "Parity error ("+c1+", "+c2+")");
		}
	}
	
	@Override
	public void printUnicode(VBILine line, char c) {
		if (isSelectedMode(line)) {
			buffer.append(c);
		}
	}
	
	@Override
	public void printChar(VBILine line, int c) {
		if (isSelectedMode(line)) {
			if (itvLink != null) {
				buffer.append(VBILine.vbiToAscii(c));
			} else {
				buffer.append(VBILine.vbiToUnicode(c));
			}
			
			if ((dataChannel == DataChannel.TEXT2) &&
					(buffer.length() == 1) &&
					(c == '<')) {
				if (itvLink != null) {
					error(line, "Invalid ITV Link "+itvLink);
				}
				itvLink = new ITVLink();
				itvLinkBuffer = new StringBuffer();
				return;
			} 
			if (itvLink != null) {
				if (itvLink.getURL() == null) {
					if (c == '>') {
						itvLink.setURL(itvLinkBuffer.toString());
						itvLinkBuffer.setLength(0);
						return;
					}
					// got the first part, now get multiple square bracket parts
					// how do we know it's done?
				} else {
					if (c == '[') {
						return;
					}
					if (c == ']') {
						String value = itvLinkBuffer.toString();
						if (value.indexOf(':') >= 0) {
							itvLink.setAttribute(value);
							itvLinkBuffer.setLength(0);
						} else {
							String itvLinkString = buffer.toString();
							int lastAttribute = itvLinkString.lastIndexOf(']',itvLinkString.length()-2);
							itvLinkString = itvLinkString.substring(0,lastAttribute+1);
							if (itvLink.checksum(itvLinkString, value)) {
								receivedITVLink(itvLink);
							} else {
								error(line, "Invalid ITV Link. Checksum error.");
							}
							buffer.setLength(0);
							resetBuffer();
						}
						return;
					}
				}
			}
			if (itvLink != null) {
				itvLinkBuffer.append(VBILine.vbiToAscii(c));
			}
		}
	}

	public void receivedITVLink(ITVLink itvLink) {
	}

	@Override
	public void backspace(VBILine line) {
		if (isSelectedMode(line)) {
			if (buffer.length() > 0) {
				buffer.setLength(buffer.length()-1);
			}
		}
	}

	@Override
	public void carriageReturn(VBILine line) {
		if (isSelectedMode(line)) {
			resetBuffer();
		}
	}
	
	@Override
	public void midRow(VBILine line, int color, boolean underline) {
		if (isSelectedMode(line)) {
			addSpace();
		}
	}

	@Override
	public void resumeCaption(VBILine line) {
		if (isSelectedMode(line)) {
			resetBuffer();
		}
	}

	@Override
	public void rollUpCaption(VBILine line, int rows) {
        /*
		if (isSelectedMode(line)) {
			resetBuffer();
		}
        */
	}

	@Override
	public void resumeDirect(VBILine line) {
		/*
		if (isSelectedMode(line)) {
			resetBuffer();
		}
		*/
	}

	@Override
	public void textRestart(VBILine line) {
		if (isSelectedMode(line)) {
			if (itvLink != null) {
				buffer.setLength(0);
			}
			resetBuffer();
		}
	}

	@Override
	public void resumeText(VBILine line) {
		/*
		if (isSelectedMode(line)) {
			resetBuffer();
		}
		*/
	}

	@Override
	public void eraseDisplayed(VBILine line) {
		if (isSelectedMode(line)) {
			resetBuffer();
		}
	}

	@Override
	public void endOfCaption(VBILine line) {
		if (isSelectedMode(line)) {
			resetBuffer();
		}
	}

	@Override
	public void tab(VBILine line, int offset) {
		if (isSelectedMode(line)) {
			addSpace();
		}
	}
	
	@Override
	public void pac(VBILine line, int row, int column, boolean underline) {
		if (isSelectedMode(line)) {
			resetBuffer();
		}
		//TODO: We really could get more sophisticated by keeping track of
		// what our screen position should be and calculating what kind
		// of change the PAC and succeeding commands and chards would do to
		// our buffer.
	}

/* TODO: if we end up processing PAC, we will need to process deleteToEndOfRow
	public void deleteToEndOfRow(VBILine line) {
		System.out.println("delete to end of row ch="+line.getChannel()+" f="+line.getField());
	}
*/
}
