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

public enum Device {
	DEFAULT { public String toString() { return "Cable"; } },
	A   { public String toString() { return "Cable A lineup"; } },
	B   { public String toString() { return "Cable B lineup"; } },
	C   { public String toString() { return "Reserved"; } },
	D   { public String toString() { return "Rebuild lineup"; } },
	E   { public String toString() { return "Reserved"; } },
	F   { public String toString() { return "D device cable ready and non-addressable for D"; } },
	G   { public String toString() { return "Non-addressable converters and cable-ready sets"; } },
	H   { public String toString() { return "Hamlin converter"; } },
	I   { public String toString() { return "Jerrold impulse converter"; } },
	J   { public String toString() { return "Jerrold converter"; } },
	K   { public String toString() { return "Reserved"; } },
	L   { public String toString() { return "Rebuild Digital"; } },
	M   { public String toString() { return "Reserved"; } },
	N   { public String toString() { return "Pioneer converter"; } },
	O   { public String toString() { return "Oak converter"; } },
	P   { public String toString() { return "Reserved"; } },
	Q   { public String toString() { return "Reserved"; } },
	R   { public String toString() { return "Cable-ready TV sets (non-rebuild)"; } },
	S   { public String toString() { return "Reserved"; } },
	T   { public String toString() { return "Tocom converter"; } },
	U   { public String toString() { return "Cable-ready TV sets with Cable A"; } },
	V   { public String toString() { return "Cable-ready TV sets with Cable B"; } },
	W   { public String toString() { return "Scientific-Atlanta converter"; } },
	X   { public String toString() { return "Digital (non-rebuild)"; } },
	Y   { public String toString() { return "Reserved"; } },
	Z   { public String toString() { return "Zenith converter"; } },
	
}
