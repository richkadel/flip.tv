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

package com.knowbout.epg.entities;

import java.text.NumberFormat;
import java.util.Comparator;

public class ChannelComparator implements Comparator {

	public int compare(Object obj1, Object obj2) {
		Channel chan1 = (Channel)obj1;
		Channel chan2 = (Channel)obj2;
		String num1 = chan1.getChannelNumber();
		String num2 = chan2.getChannelNumber();
		//Is the channel number a number or string channel
		if (num1.matches("\\D")) {
			//It was a String channel (think satallite)
			if (num2.matches("\\D")) {
				//Channel 2 was also a string, so do a String sort.
				return num1.compareTo(num2);				
			} else {
				//Else return 1 to sort the number channel before the string channel
				return 1;
			}
		} else {
			//It was a channel 1 was a number
			if (num2.matches("\\D")) {
				//Channel 2 is a String so return -1, to sort numbers before strings
				return -1;
			} else {
				//Both were integers, so parse them and compare
				int chanNumber1 = 0;
				int chanNumber2 = 0;
				try {
					chanNumber1 = Integer.parseInt(num1);
				} catch (NumberFormatException e){					
				}
				try {
					chanNumber2 = Integer.parseInt(num2);
				} catch (NumberFormatException e){					
				}
				return (chanNumber1 - chanNumber2);
			}
		}		
	}

}
