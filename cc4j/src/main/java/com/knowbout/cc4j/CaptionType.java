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
public enum CaptionType {
	
	ROLLUP,
	POPON,
	PAINTON,
	;
}
