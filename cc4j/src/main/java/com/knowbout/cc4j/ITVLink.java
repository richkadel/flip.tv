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

/**
 * 
 */
package com.knowbout.cc4j;

/**
 * @author Rich Kadel
 * @author $Author$
 * @version $Rev$ $Date$
 */
public class ITVLink implements java.io.Serializable {
	
	private static final long serialVersionUID = 5893241691291799363L;
	
	private String url;
	private String name;
	private String expires;
	private String script;
	private String type;
	private String view;

	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}

	public void setAttribute(String attribute) {
		// TODO Auto-generated method stub
		int colon = attribute.indexOf(':');
		char name = Character.toLowerCase(attribute.charAt(0));
		String value = attribute.substring(colon+1);
		switch(name) {
		case 'n':
			setName(value);
			break;
			
		case 'e':
			setExpires(value);
			break;
			
		case 's':
			setScript(value);
			break;
			
		case 't':
			if (value.length() > 1) {
				setType(value);
			} else {
				char typeValue = Character.toLowerCase(value.charAt(0));
				switch(typeValue) {
				case 'p':
					setType("Program");
					break;
					
				case 'n':
					setType("Network");
					break;
					
				case 's':
					setType("Station");
					break;
					
				case 'o':
					setType("Operator");
					break;
					
				case 'a':
					setType("Sponsor");
					break;
				}
			}
			break;
			
		case 'v':
			setView(value);
			break;
			
		}
	}

	public boolean checksum(String itvLinkString, String checksumString) {
		int len = itvLinkString.length();
		int cs = 0;
		for (int i = 0; i < len; i += 2) {
			if ((i+1) < len) {
				cs = cs + itvLinkString.charAt(i+1);
			}
			cs = cs + (256 * itvLinkString.charAt(i));
		}
		
		// Convert to ones-complement
		cs = (cs % 65536) + ((cs & 0xffff0000) / 65536);
		
		// invert
		cs = 65535 - cs;
		
		try {
			int checksumValue = Integer.parseInt(checksumString, 16);
		
			if (cs == checksumValue) {
				return true;
			} else {
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setExpires(String expires) {
		this.expires = expires;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public void setView(String view) {
		this.view = view;
	}
	
	public String getName() {
		return name;
	}

	public String getExpires() {
		return expires;
	}

	public String getScript() {
		return script;
	}

	public String getType() {
		return type;
	}
	
	public String getView() {
		return view;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append('<');
		buffer.append(url);
		buffer.append(">");
		if (name != null) {
			buffer.append("[Name:");
			buffer.append(name);
			buffer.append("]");
		}
		if (expires != null) {
			buffer.append("[Expires:");
			buffer.append(expires);
			buffer.append("]");
		}
		if (script != null) {
			buffer.append("[Script:");
			buffer.append(script);
			buffer.append("]");
		}
		if (type != null) {
			buffer.append("[Type:");
			buffer.append(type);
			buffer.append("]");
		}
		if (view != null) {
			buffer.append("[View:");
			buffer.append(view);
			buffer.append("]");
		}
		return buffer.toString();
	}

}
