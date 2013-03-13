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

package com.appeligo.typeconverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.opensymphony.webwork.util.WebWorkTypeConverter;
import com.opensymphony.xwork.util.TypeConversionException;

public class TimeTypeConverter extends WebWorkTypeConverter  {

	private static final Logger log = Logger.getLogger(TimeTypeConverter.class);
	
	private static final String DEFAULT_FORMAT = "h:mma";
	
	private String format = DEFAULT_FORMAT;
	
	/**
	 * 
	 */
	@Override
	public Object convertFromString(Map context, String[] values, Class toClass) {
		if (values != null && values.length > 0) {
			if (toClass.equals(java.util.Date.class)) {
				DateFormat formatter = new SimpleDateFormat(format);
				try {
					return formatter.parse(values[0]);
				} catch (ParseException pe) {
					String message = "Could not parse " + values[0] + " using " + format;
					if (log.isDebugEnabled()) {
						log.debug(message);
					}
					throw new TypeConversionException(message);
				}
			}
		}
		return null;
	}

	@Override
	public String convertToString(Map context, Object value) {
		DateFormat formatter = new SimpleDateFormat(format);
		return formatter.format((Date)value);
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

}
