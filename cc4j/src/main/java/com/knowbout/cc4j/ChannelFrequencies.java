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

import java.util.EnumMap;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;


/**
 * Provides methods for translating channels to their corresponding frequency
 * and vice versa, for supported frequency standards. Frequencies are in
 * Megahertz (MHz).
 * @author Rich Kadel
 * @author $Author: kadel $
 * @version $Rev: 204 $ $Date: 2006-08-31 11:08:50 -0700 (Thu, 31 Aug 2006) $
 */
public class ChannelFrequencies {

	private static Map<FrequencyStandard,Map<String,Double>> channelToFrequency =
		new EnumMap<FrequencyStandard,Map<String,Double>>(FrequencyStandard.class);
	
	private static Map<FrequencyStandard,Map<Double,String>> frequencyToChannel =
		new EnumMap<FrequencyStandard,Map<Double,String>>(FrequencyStandard.class);
	
	static {
		Map<String,Double> channelMap;
		
		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.US_BROADCAST, channelMap);
	    channelMap.put("2",	 55.250);
	    channelMap.put("3",	 61.250);
	    channelMap.put("4",	 67.250);
	    channelMap.put("5",	 77.250);
	    channelMap.put("6",	 83.250);
	    channelMap.put("7",	175.250);
	    channelMap.put("8",	181.250);
	    channelMap.put("9",	187.250);
	    channelMap.put("10",	193.250);
	    channelMap.put("11",	199.250);
	    channelMap.put("12",	205.250);
	    channelMap.put("13",	211.250);
	    channelMap.put("14",	471.250);
	    channelMap.put("15",	477.250);
	    channelMap.put("16",	483.250);
	    channelMap.put("17",	489.250);
	    channelMap.put("18",	495.250);
	    channelMap.put("19",	501.250);
	    channelMap.put("20",	507.250);
	    channelMap.put("21",	513.250);
	    channelMap.put("22",	519.250);
	    channelMap.put("23",	525.250);
	    channelMap.put("24",	531.250);
	    channelMap.put("25",	537.250);
	    channelMap.put("26",	543.250);
	    channelMap.put("27",	549.250);
	    channelMap.put("28",	555.250);
	    channelMap.put("29",	561.250);
	    channelMap.put("30",	567.250);
	    channelMap.put("31",	573.250);
	    channelMap.put("32",	579.250);
	    channelMap.put("33",	585.250);
	    channelMap.put("34",	591.250);
	    channelMap.put("35",	597.250);
	    channelMap.put("36",	603.250);
	    channelMap.put("37",	609.250);
	    channelMap.put("38",	615.250);
	    channelMap.put("39",	621.250);
	    channelMap.put("40",	627.250);
	    channelMap.put("41",	633.250);
	    channelMap.put("42",	639.250);
	    channelMap.put("43",	645.250);
	    channelMap.put("44",	651.250);
	    channelMap.put("45",	657.250);
	    channelMap.put("46",	663.250);
	    channelMap.put("47",	669.250);
	    channelMap.put("48",	675.250);
	    channelMap.put("49",	681.250);
	    channelMap.put("50",	687.250);
	    channelMap.put("51",	693.250);
	    channelMap.put("52",	699.250);
	    channelMap.put("53",	705.250);
	    channelMap.put("54",	711.250);
	    channelMap.put("55",	717.250);
	    channelMap.put("56",	723.250);
	    channelMap.put("57",	729.250);
	    channelMap.put("58",	735.250);
	    channelMap.put("59",	741.250);
	    channelMap.put("60",	747.250);
	    channelMap.put("61",	753.250);
	    channelMap.put("62",	759.250);
	    channelMap.put("63",	765.250);
	    channelMap.put("64",	771.250);
	    channelMap.put("65",	777.250);
	    channelMap.put("66",	783.250);
	    channelMap.put("67",	789.250);
	    channelMap.put("68",	795.250);
	    channelMap.put("69",	801.250);
	 
	    channelMap.put("70",	807.250);
	    channelMap.put("71",	813.250);
	    channelMap.put("72",	819.250);
	    channelMap.put("73",	825.250);
	    channelMap.put("74",	831.250);
	    channelMap.put("75",	837.250);
	    channelMap.put("76",	843.250);
	    channelMap.put("77",	849.250);
	    channelMap.put("78",	855.250);
	    channelMap.put("79",	861.250);
	    channelMap.put("80",	867.250);
	    channelMap.put("81",	873.250);
	    channelMap.put("82",	879.250);
	    channelMap.put("83",	885.250);

/* US cable */
		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.US_CABLE, channelMap);
        channelMap.put("1",	 73.250);
        channelMap.put("2",	 55.250);
        channelMap.put("3",	 61.250);
        channelMap.put("4",	 67.250);
        channelMap.put("5",	 77.250);
        channelMap.put("6",	 83.250);
        channelMap.put("7",	175.250);
        channelMap.put("8",	181.250);
        channelMap.put("9",	187.250);
        channelMap.put("10",	193.250);
        channelMap.put("11",	199.250);
        channelMap.put("12",	205.250);

        channelMap.put("13",	211.250);
        channelMap.put("14",	121.250);
        channelMap.put("15",	127.250);
        channelMap.put("16",	133.250);
        channelMap.put("17",	139.250);
        channelMap.put("18",	145.250);
        channelMap.put("19",	151.250);
        channelMap.put("20",	157.250);
 
        channelMap.put("21",	163.250);
        channelMap.put("22",	169.250);
        channelMap.put("23",	217.250);
        channelMap.put("24",	223.250);
        channelMap.put("25",	229.250);
        channelMap.put("26",	235.250);
        channelMap.put("27",	241.250);
        channelMap.put("28",	247.250);
        channelMap.put("29",	253.250);
        channelMap.put("30",	259.250);
        channelMap.put("31",	265.250);
        channelMap.put("32",	271.250);
        channelMap.put("33",	277.250);
        channelMap.put("34",	283.250);
        channelMap.put("35",	289.250);
        channelMap.put("36",	295.250);
        channelMap.put("37",	301.250);
        channelMap.put("38",	307.250);
        channelMap.put("39",	313.250);
        channelMap.put("40",	319.250);
        channelMap.put("41",	325.250);
        channelMap.put("42",	331.250);
        channelMap.put("43",	337.250);
        channelMap.put("44",	343.250);
        channelMap.put("45",	349.250);
        channelMap.put("46",	355.250);
        channelMap.put("47",	361.250);
        channelMap.put("48",	367.250);
        channelMap.put("49",	373.250);
        channelMap.put("50",	379.250);
        channelMap.put("51",	385.250);
        channelMap.put("52",	391.250);
        channelMap.put("53",	397.250);
        channelMap.put("54",	403.250);
        channelMap.put("55",	409.250);
        channelMap.put("56",	415.250);
        channelMap.put("57",	421.250);
        channelMap.put("58",	427.250);
        channelMap.put("59",	433.250);
        channelMap.put("60",	439.250);
        channelMap.put("61",	445.250);
        channelMap.put("62",	451.250);
        channelMap.put("63",	457.250);
        channelMap.put("64",	463.250);
        channelMap.put("65",	469.250);
        channelMap.put("66",	475.250);
        channelMap.put("67",	481.250);
        channelMap.put("68",	487.250);
        channelMap.put("69",	493.250);
 
        channelMap.put("70",	499.250);
        channelMap.put("71",	505.250);
        channelMap.put("72",	511.250);
        channelMap.put("73",	517.250);
        channelMap.put("74",	523.250);
        channelMap.put("75",	529.250);
        channelMap.put("76",	535.250);
        channelMap.put("77",	541.250);
        channelMap.put("78",	547.250);
        channelMap.put("79",	553.250);
        channelMap.put("80",	559.250);
        channelMap.put("81",	565.250);
        channelMap.put("82",	571.250);
        channelMap.put("83",	577.250);
        channelMap.put("84",	583.250);
        channelMap.put("85",	589.250);
        channelMap.put("86",	595.250);
        channelMap.put("87",	601.250);
        channelMap.put("88",	607.250);
        channelMap.put("89",	613.250);
        channelMap.put("90",	619.250);
        channelMap.put("91",	625.250);
        channelMap.put("92",	631.250);
        channelMap.put("93",	637.250);
        channelMap.put("94",	643.250);
        channelMap.put("95",	 91.250);
        channelMap.put("96",	 97.250);
        channelMap.put("97",	103.250);
        channelMap.put("98",	109.250);
        channelMap.put("99",	115.250);
        channelMap.put("100",	649.250);
        channelMap.put("101",	655.250);
        channelMap.put("102",	661.250);
        channelMap.put("103",	667.250);
        channelMap.put("104",	673.250);
        channelMap.put("105",	679.250);
        channelMap.put("106",	685.250);
        channelMap.put("107",	691.250);
        channelMap.put("108",	697.250);
        channelMap.put("109",	703.250);
        channelMap.put("110",	709.250);
        channelMap.put("111",	715.250);
        channelMap.put("112",	721.250);
        channelMap.put("113",	727.250);
        channelMap.put("114",	733.250);
        channelMap.put("115",	739.250);
        channelMap.put("116",	745.250);
        channelMap.put("117",	751.250);
        channelMap.put("118",	757.250);
        channelMap.put("119",	763.250);
        channelMap.put("120",	769.250);
        channelMap.put("121",	775.250);
        channelMap.put("122",	781.250);
        channelMap.put("123",	787.250);
        channelMap.put("124",	793.250);
        channelMap.put("125",	799.250);
 
        channelMap.put("T7", 	  8.250);
        channelMap.put("T8",	 14.250);
        channelMap.put("T9",	 20.250);
        channelMap.put("T10",	 26.250);
        channelMap.put("T11",	 32.250);
        channelMap.put("T12",	 38.250);
        channelMap.put("T13",	 44.250);
        channelMap.put("T14",	 50.250);

/* US HRC */
		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.US_CABLE_HRC, channelMap);
        channelMap.put("1",	  72.000);

        channelMap.put("2",	  54.000); 
        channelMap.put("3",	  60.000); 
        channelMap.put("4",	  66.000); 

        channelMap.put("5",	  78.000); 
        channelMap.put("6",	  84.000); 

        channelMap.put("7",	 174.000);
        channelMap.put("8",	 180.000);
        channelMap.put("9",	 186.000);
        channelMap.put("10",	 192.000);
        channelMap.put("11",	 198.000);
        channelMap.put("12",	 204.000);
        channelMap.put("13",	 210.000);
        channelMap.put("14",	 120.000);
        channelMap.put("15",	 126.000);
        channelMap.put("16",	 132.000);
        channelMap.put("17",	 138.000);
        channelMap.put("18",	 144.000);
        channelMap.put("19",	 150.000);
        channelMap.put("20",	 156.000);
        channelMap.put("21",	 162.000);
        channelMap.put("22",	 168.000);
        channelMap.put("23",	 216.000);
        channelMap.put("24",	 222.000);
        channelMap.put("25",	 228.000);
        channelMap.put("26",	 234.000);
        channelMap.put("27",	 240.000);
        channelMap.put("28",	 246.000);
        channelMap.put("29",	 252.000);
        channelMap.put("30",	 258.000);
        channelMap.put("31",	 264.000);
        channelMap.put("32",	 270.000);
        channelMap.put("33",	 276.000);
        channelMap.put("34",	 282.000);
        channelMap.put("35",	 288.000);
        channelMap.put("36",	 294.000);
        channelMap.put("37",	 300.000);
        channelMap.put("38",	 306.000);
        channelMap.put("39",	 312.000);
        channelMap.put("40",	 318.000);
        channelMap.put("41",	 324.000);
        channelMap.put("42",	 330.000);
        channelMap.put("43",	 336.000);
        channelMap.put("44",	 342.000);
        channelMap.put("45",	 348.000);
        channelMap.put("46",	 354.000);
        channelMap.put("47",	 360.000);
        channelMap.put("48",	 366.000);
        channelMap.put("49",	 372.000);
        channelMap.put("50",	 378.000);
        channelMap.put("51",	 384.000);
        channelMap.put("52",	 390.000);
        channelMap.put("53",	 396.000);
        channelMap.put("54",	 402.000);
        channelMap.put("55",	 408.000);
        channelMap.put("56",	 414.000);
        channelMap.put("57",	 420.000);
        channelMap.put("58",	 426.000);
        channelMap.put("59",	 432.000);
        channelMap.put("60",	 438.000);
        channelMap.put("61",	 444.000);
        channelMap.put("62",	 450.000);
        channelMap.put("63",	 456.000);
        channelMap.put("64",	 462.000);
        channelMap.put("65",	 468.000);
        channelMap.put("66",	 474.000);
        channelMap.put("67",	 480.000);
        channelMap.put("68",	 486.000);
        channelMap.put("69",	 492.000);
        channelMap.put("70",	 498.000);
        channelMap.put("71",	 504.000);
        channelMap.put("72",	 510.000);
        channelMap.put("73",	 516.000);
        channelMap.put("74",	 522.000);
        channelMap.put("75",	 528.000);
        channelMap.put("76",	 534.000);
        channelMap.put("77",	 540.000);
        channelMap.put("78",	 546.000);
        channelMap.put("79",	 552.000);
        channelMap.put("80",	 558.000);
        channelMap.put("81",	 564.000);
        channelMap.put("82",	 570.000);
        channelMap.put("83",	 576.000);
        channelMap.put("84",	 582.000);
        channelMap.put("85",	 588.000);
        channelMap.put("86",	 594.000);
        channelMap.put("87",	 600.000);
        channelMap.put("88",	 606.000);
        channelMap.put("89",	 612.000);
        channelMap.put("90",	 618.000);
        channelMap.put("91",	 624.000);
        channelMap.put("92",	 630.000);
        channelMap.put("93",	 636.000);
        channelMap.put("94",	 642.000);

        channelMap.put("95",	  90.000);
        channelMap.put("96",	  96.000);
        channelMap.put("97",	 102.000);
        channelMap.put("98",	 108.000);
        channelMap.put("99",	 114.000);

        channelMap.put("100",	 648.000);
        channelMap.put("101",	 654.000);
        channelMap.put("102",	 660.000);
        channelMap.put("103",	 666.000);
        channelMap.put("104",	 672.000);
        channelMap.put("105",	 678.000);
        channelMap.put("106",	 684.000);
        channelMap.put("107",	 690.000);
        channelMap.put("108",	 696.000);
        channelMap.put("109",	 702.000);
        channelMap.put("110",	 708.000);
        channelMap.put("111",	 714.000);
        channelMap.put("112",	 720.000);
        channelMap.put("113",	 726.000);
        channelMap.put("114",	 732.000);
        channelMap.put("115",	 738.000);
        channelMap.put("116",	 744.000);
        channelMap.put("117",	 750.000);
        channelMap.put("118",	 756.000);
        channelMap.put("119",	 762.000);
        channelMap.put("120",	 768.000);
        channelMap.put("121",	 774.000);
        channelMap.put("122",	 780.000);
        channelMap.put("123",	 786.000);
        channelMap.put("124",	 792.000);
        channelMap.put("125",	 798.000);
 
        channelMap.put("T7",	   7.000);  
        channelMap.put("T8",	  13.000); 
        channelMap.put("T9",	  19.000); 
        channelMap.put("T10",	  25.000); 
        channelMap.put("T11",	  31.000); 
        channelMap.put("T12",	  37.000); 
        channelMap.put("T13",	  43.000); 
        channelMap.put("T14",	  49.000); 

/* US IRC */
		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.US_CABLE_IRC, channelMap);
        channelMap.put("1",      73.250);
        channelMap.put("2",      55.250);
        channelMap.put("3",      61.250);
        channelMap.put("4",      67.250);
        channelMap.put("5",      79.250);
        channelMap.put("6",      85.250);
        channelMap.put("7",     175.250);
        channelMap.put("8",     181.250);
        channelMap.put("9",     187.250);
        channelMap.put("10",    193.250);
        channelMap.put("11",    199.250);
        channelMap.put("12",    205.250);
        channelMap.put("13",    211.250);

        channelMap.put("14",    121.150);
        channelMap.put("15",    127.150);
        channelMap.put("16",    133.150);
        channelMap.put("17",    139.150);
        channelMap.put("18",    145.150);
        channelMap.put("19",    151.150);
        channelMap.put("20",    157.150);
        channelMap.put("21",    163.150);
        channelMap.put("22",    169.150);

        channelMap.put("23",    217.250);
        channelMap.put("24",    223.250);
        channelMap.put("25",    229.250);
        channelMap.put("26",    235.250);
        channelMap.put("27",    241.250);
        channelMap.put("28",    247.250);
        channelMap.put("29",    253.250);
        channelMap.put("30",    259.250);
        channelMap.put("31",    265.250);
        channelMap.put("32",    271.250);
        channelMap.put("33",    277.250);
        channelMap.put("34",    283.250);
        channelMap.put("35",    289.250);
        channelMap.put("36",    295.250);
        channelMap.put("37",    301.250);
        channelMap.put("38",    307.250);
        channelMap.put("39",    313.250);
        channelMap.put("40",    319.250);
        channelMap.put("41",    325.250);
        channelMap.put("42",    331.250);
        channelMap.put("43",    337.250);
        channelMap.put("44",    343.250);
        channelMap.put("45",    349.250);
        channelMap.put("46",    355.250);
        channelMap.put("47",    361.250);
        channelMap.put("48",    367.250);
        channelMap.put("49",    373.250);
        channelMap.put("50",    379.250);
        channelMap.put("51",    385.250);
        channelMap.put("52",    391.250);
        channelMap.put("53",    397.250);
        channelMap.put("54",    403.250);
        channelMap.put("55",    409.250);
        channelMap.put("56",    415.250);
        channelMap.put("57",    421.250);
        channelMap.put("58",    427.250);
        channelMap.put("59",    433.250);
        channelMap.put("60",    439.250);
        channelMap.put("61",    445.250);
        channelMap.put("62",    451.250);
        channelMap.put("63",    457.250);
        channelMap.put("64",    463.250);
        channelMap.put("65",    469.250);
        channelMap.put("66",    475.250);
        channelMap.put("67",    481.250);
        channelMap.put("68",    487.250);
        channelMap.put("69",    493.250);
        channelMap.put("70",    499.250);
        channelMap.put("71",    505.250);
        channelMap.put("72",    511.250);
        channelMap.put("73",    517.250);
        channelMap.put("74",    523.250);
        channelMap.put("75",    529.250);
        channelMap.put("76",    535.250);
        channelMap.put("77",    541.250);
        channelMap.put("78",    547.250);
        channelMap.put("79",    553.250);
        channelMap.put("80",    559.250);
        channelMap.put("81",    565.250);
        channelMap.put("82",    571.250);
        channelMap.put("83",    577.250);
        channelMap.put("84",    583.250);
        channelMap.put("85",    589.250);
        channelMap.put("86",    595.250);
        channelMap.put("87",    601.250);
        channelMap.put("88",    607.250);
        channelMap.put("89",    613.250);
        channelMap.put("90",    619.250);
        channelMap.put("91",    625.250);
        channelMap.put("92",    631.250);
        channelMap.put("93",    637.250);
        channelMap.put("94",    643.250);

        channelMap.put("95",     91.250);
        channelMap.put("96",     97.250);
        channelMap.put("97",    103.250);
        channelMap.put("98",    109.250);
        channelMap.put("99",    115.250);
        channelMap.put("100",   649.250);
        channelMap.put("101",   655.250);
        channelMap.put("102",   661.250);
        channelMap.put("103",   667.250);
        channelMap.put("104",   673.250);
        channelMap.put("105",   679.250);
        channelMap.put("106",   685.250);
        channelMap.put("107",   691.250);
        channelMap.put("108",   697.250);
        channelMap.put("109",   703.250);
        channelMap.put("110",   709.250);
        channelMap.put("111",   715.250);
        channelMap.put("112",   721.250);
        channelMap.put("113",   727.250);
        channelMap.put("114",   733.250);
        channelMap.put("115",   739.250);
        channelMap.put("116",   745.250);
        channelMap.put("117",   751.250);
        channelMap.put("118",   757.250);
        channelMap.put("119",   763.250);
        channelMap.put("120",   769.250);
        channelMap.put("121",   775.250);
        channelMap.put("122",   781.250);
        channelMap.put("123",   787.250);
        channelMap.put("124",   793.250);
        channelMap.put("125",   799.250);

        channelMap.put("T7",      8.250);
        channelMap.put("T8",     14.250);
        channelMap.put("T9",     20.250);
        channelMap.put("T10",    26.250);
        channelMap.put("T11",    32.250);
        channelMap.put("T12",    38.250);
        channelMap.put("T13",    44.250);
        channelMap.put("T14",    50.250);


/* --------------------------------------------------------------------- */

/* JP broadcast */
		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.JAPAN_BROADCAST, channelMap);
        channelMap.put("1",   91.250);
        channelMap.put("2",   97.250);
        channelMap.put("3",  103.250);
        channelMap.put("4",  171.250);
        channelMap.put("5",  177.250);
        channelMap.put("6",  183.250);
        channelMap.put("7",  189.250);
        channelMap.put("8",  193.250);
        channelMap.put("9",  199.250);
        channelMap.put("10", 205.250);
        channelMap.put("11", 211.250);
        channelMap.put("12", 217.250);

        channelMap.put("13", 471.250);
        channelMap.put("14", 477.250);
        channelMap.put("15", 483.250);
        channelMap.put("16", 489.250);
        channelMap.put("17", 495.250);
        channelMap.put("18", 501.250);
        channelMap.put("19", 507.250);
        channelMap.put("20", 513.250);
        channelMap.put("21", 519.250);
        channelMap.put("22", 525.250);
        channelMap.put("23", 531.250);
        channelMap.put("24", 537.250);
        channelMap.put("25", 543.250);
        channelMap.put("26", 549.250);
        channelMap.put("27", 555.250);
        channelMap.put("28", 561.250);
        channelMap.put("29", 567.250);
        channelMap.put("30", 573.250);
        channelMap.put("31", 579.250);
        channelMap.put("32", 585.250);
        channelMap.put("33", 591.250);
        channelMap.put("34", 597.250);
        channelMap.put("35", 603.250);
        channelMap.put("36", 609.250);
        channelMap.put("37", 615.250);
        channelMap.put("38", 621.250);
        channelMap.put("39", 627.250);
        channelMap.put("40", 633.250);
        channelMap.put("41", 639.250);
        channelMap.put("42", 645.250);
        channelMap.put("43", 651.250);
        channelMap.put("44", 657.250);

        channelMap.put("45", 663.250);
        channelMap.put("46", 669.250);
        channelMap.put("47", 675.250);
        channelMap.put("48", 681.250);
        channelMap.put("49", 687.250);
        channelMap.put("50", 693.250);
        channelMap.put("51", 699.250);
        channelMap.put("52", 705.250);
        channelMap.put("53", 711.250);
        channelMap.put("54", 717.250);
        channelMap.put("55", 723.250);
        channelMap.put("56", 729.250);
        channelMap.put("57", 735.250);
        channelMap.put("58", 741.250);
        channelMap.put("59", 747.250);
        channelMap.put("60", 753.250);
        channelMap.put("61", 759.250);
        channelMap.put("62", 765.250);

/* JP cable */
		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.JAPAN_CABLE, channelMap);
        channelMap.put("13",	109.250);
        channelMap.put("14",	115.250);
        channelMap.put("15",	121.250);
        channelMap.put("16",	127.250);
        channelMap.put("17",	133.250);
        channelMap.put("18",	139.250);
        channelMap.put("19",	145.250);
        channelMap.put("20",	151.250);
 
        channelMap.put("21",	157.250);
        channelMap.put("22",	165.250);
        channelMap.put("23",	223.250);
        channelMap.put("24",	231.250);
        channelMap.put("25",	237.250);
        channelMap.put("26",	243.250);
        channelMap.put("27",	249.250);
        channelMap.put("28",	253.250);
        channelMap.put("29",	259.250);
        channelMap.put("30",	265.250);
        channelMap.put("31",	271.250);
        channelMap.put("32",	277.250);
        channelMap.put("33",	283.250);
        channelMap.put("34",	289.250);
        channelMap.put("35",	295.250);
        channelMap.put("36",	301.250);
        channelMap.put("37",	307.250);
        channelMap.put("38",	313.250);
        channelMap.put("39",	319.250);
        channelMap.put("40",	325.250);
        channelMap.put("41",	331.250);
        channelMap.put("42",	337.250);
        channelMap.put("43",	343.250);
        channelMap.put("44",	349.250);
        channelMap.put("45", 	355.250);
        channelMap.put("46", 	361.250);
        channelMap.put("47", 	367.250);
        channelMap.put("48", 	373.250);
        channelMap.put("49", 	379.250);
        channelMap.put("50", 	385.250);
        channelMap.put("51", 	391.250);
        channelMap.put("52", 	397.250);
        channelMap.put("53", 	403.250);
        channelMap.put("54", 	409.250);
        channelMap.put("55", 	415.250);
        channelMap.put("56", 	421.250);
        channelMap.put("57", 	427.250);
        channelMap.put("58", 	433.250);
        channelMap.put("59", 	439.250);
        channelMap.put("60", 	445.250);
        channelMap.put("61", 	451.250);
        channelMap.put("62", 	457.250);
        channelMap.put("63",	463.250);

/* --------------------------------------------------------------------- */

/* australia */
		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.AUSTRALIA, channelMap);
        channelMap.put("0",	 46.250);
        channelMap.put("1",	 57.250);
        channelMap.put("2",	 64.250);
        channelMap.put("3",	 86.250);
        channelMap.put("4",  	 95.250);
        channelMap.put("5",  	102.250);
        channelMap.put("5A",  	138.250);
        channelMap.put("6",  	175.250);
        channelMap.put("7",  	182.250);
        channelMap.put("8",  	189.250);
        channelMap.put("9",  	196.250);
        channelMap.put("10", 	209.250);
        channelMap.put("11",	216.250);
        channelMap.put("28",	527.250);
        channelMap.put("29",	534.250);
        channelMap.put("30",	541.250);
        channelMap.put("31",	548.250);
        channelMap.put("32",	555.250);
        channelMap.put("33",	562.250);
        channelMap.put("34",	569.250);
        channelMap.put("35",	576.250);
        channelMap.put("36",     591.250);
        channelMap.put("39",	604.250);
        channelMap.put("40",	611.250);
        channelMap.put("41",	618.250);
        channelMap.put("42",	625.250);
        channelMap.put("43",	632.250);
        channelMap.put("44",	639.250);
        channelMap.put("45",	646.250);
        channelMap.put("46",	653.250);
        channelMap.put("47",	660.250);
        channelMap.put("48",	667.250);
        channelMap.put("49",	674.250);
        channelMap.put("50",	681.250);
        channelMap.put("51",	688.250);
        channelMap.put("52",	695.250);
        channelMap.put("53",	702.250);
        channelMap.put("54",	709.250);
        channelMap.put("55",	716.250);
        channelMap.put("56",	723.250);
        channelMap.put("57",	730.250);
        channelMap.put("58",	737.250);
        channelMap.put("59",	744.250);
        channelMap.put("60",	751.250);
        channelMap.put("61",	758.250);
        channelMap.put("62",	765.250);
        channelMap.put("63",	772.250);
        channelMap.put("64",	779.250);
        channelMap.put("65",	786.250);
        channelMap.put("66",	793.250);
        channelMap.put("67",	800.250);
        channelMap.put("68",	807.250);
        channelMap.put("69",	814.250);

		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.AUSTRALIA_OPTUS, channelMap);
   channelMap.put("1",  138.250);
   channelMap.put("2",  147.250);
   channelMap.put("3",  154.250);
   channelMap.put("4",  161.250);
   channelMap.put("5",  168.250);
   channelMap.put("6",  175.250);
   channelMap.put("7",  182.250);
   channelMap.put("8",  189.250);
   channelMap.put("9",  196.250);
   channelMap.put("10", 209.250);
   channelMap.put("11", 216.250);
   channelMap.put("12", 224.250);
   channelMap.put("13", 231.250);
   channelMap.put("14", 238.250);
   channelMap.put("15", 245.250);
   channelMap.put("16", 252.250);
   channelMap.put("17", 259.250);
   channelMap.put("18", 266.250);
   channelMap.put("19", 273.250);
   channelMap.put("20", 280.250);
   channelMap.put("21", 287.250);
   channelMap.put("22", 294.250);
   channelMap.put("23", 303.250);
   channelMap.put("24", 310.250);
   channelMap.put("25", 317.250);
   channelMap.put("26", 324.250);
   channelMap.put("27", 338.250);
   channelMap.put("28", 345.250);
   channelMap.put("29", 352.250);
   channelMap.put("30", 359.250);
   channelMap.put("31", 366.250);
   channelMap.put("32", 373.250);
   channelMap.put("33", 380.250);
   channelMap.put("34", 387.250);
   channelMap.put("35", 394.250);
   channelMap.put("36", 401.250);
   channelMap.put("37", 408.250);
   channelMap.put("38", 415.250);
   channelMap.put("39", 422.250);
   channelMap.put("40", 429.250);
   channelMap.put("41", 436.250);
   channelMap.put("42", 443.250);
   channelMap.put("43", 450.250);
   channelMap.put("44", 457.250);
   channelMap.put("45", 464.250);
   channelMap.put("46", 471.250);
   channelMap.put("47", 478.250);
   channelMap.put("48", 485.250);
   channelMap.put("49", 492.250);
   channelMap.put("50", 499.250);
   channelMap.put("51", 506.250);
   channelMap.put("52", 513.250);
   channelMap.put("53", 520.250);
   channelMap.put("54", 527.250);
   channelMap.put("55", 534.250);


/* --------------------------------------------------------------------- */
/* europe                                                                */

/* CCIR frequencies */

        Map<String,Double> FREQ_CCIR_I_III = new HashMap<String,Double>();
        FREQ_CCIR_I_III.put("E2",	  48.250);
        FREQ_CCIR_I_III.put("E3",	  55.250);
        FREQ_CCIR_I_III.put("E4",	  62.250);

        FREQ_CCIR_I_III.put("S01",	  69.250);
        FREQ_CCIR_I_III.put("S02",	  76.250);
        FREQ_CCIR_I_III.put("S03",	  83.250);

        FREQ_CCIR_I_III.put("E5",	 175.250);
        FREQ_CCIR_I_III.put("E6",	 182.250);
        FREQ_CCIR_I_III.put("E7",	 189.250);
        FREQ_CCIR_I_III.put("E8",	 196.250);
        FREQ_CCIR_I_III.put("E9",	 203.250);
        FREQ_CCIR_I_III.put("E10",	 210.250);
        FREQ_CCIR_I_III.put("E11",	 217.250);
        FREQ_CCIR_I_III.put("E12",	 224.250);

        Map<String,Double> FREQ_CCIR_SL_SH = new HashMap<String,Double>();
        FREQ_CCIR_SL_SH.put("SE1",	 105.250);
        FREQ_CCIR_SL_SH.put("SE2",	 112.250);
        FREQ_CCIR_SL_SH.put("SE3",	 119.250);
        FREQ_CCIR_SL_SH.put("SE4",	 126.250);
        FREQ_CCIR_SL_SH.put("SE5",	 133.250);
        FREQ_CCIR_SL_SH.put("SE6",	 140.250);
        FREQ_CCIR_SL_SH.put("SE7",	 147.250);
        FREQ_CCIR_SL_SH.put("SE8",	 154.250);
        FREQ_CCIR_SL_SH.put("SE9",	 161.250);
        FREQ_CCIR_SL_SH.put("SE10",    168.250);

        FREQ_CCIR_SL_SH.put("SE11",    231.250);
        FREQ_CCIR_SL_SH.put("SE12",    238.250);
        FREQ_CCIR_SL_SH.put("SE13",    245.250);
        FREQ_CCIR_SL_SH.put("SE14",    252.250);
        FREQ_CCIR_SL_SH.put("SE15",    259.250);
        FREQ_CCIR_SL_SH.put("SE16",    266.250);
        FREQ_CCIR_SL_SH.put("SE17",    273.250);
        FREQ_CCIR_SL_SH.put("SE18",    280.250);
        FREQ_CCIR_SL_SH.put("SE19",    287.250);
        FREQ_CCIR_SL_SH.put("SE20",    294.250);

        Map<String,Double> FREQ_CCIR_H = new HashMap<String,Double>();
        FREQ_CCIR_H.put("S21", 303.250);
        FREQ_CCIR_H.put("S22", 311.250);
        FREQ_CCIR_H.put("S23", 319.250);
        FREQ_CCIR_H.put("S24", 327.250);
        FREQ_CCIR_H.put("S25", 335.250);
        FREQ_CCIR_H.put("S26", 343.250);
        FREQ_CCIR_H.put("S27", 351.250);
        FREQ_CCIR_H.put("S28", 359.250);
        FREQ_CCIR_H.put("S29", 367.250);
        FREQ_CCIR_H.put("S30", 375.250);
        FREQ_CCIR_H.put("S31", 383.250);
        FREQ_CCIR_H.put("S32", 391.250);
        FREQ_CCIR_H.put("S33", 399.250);
        FREQ_CCIR_H.put("S34", 407.250);
        FREQ_CCIR_H.put("S35", 415.250);
        FREQ_CCIR_H.put("S36", 423.250);
        FREQ_CCIR_H.put("S37", 431.250);
        FREQ_CCIR_H.put("S38", 439.250);
        FREQ_CCIR_H.put("S39", 447.250);
        FREQ_CCIR_H.put("S40", 455.250);
        FREQ_CCIR_H.put("S41", 463.250);

/* OIRT frequencies */

        Map<String,Double> FREQ_OIRT_I_III = new HashMap<String,Double>();
        FREQ_OIRT_I_III.put("R1",       49.750);
        FREQ_OIRT_I_III.put("R2",       59.250);

        FREQ_OIRT_I_III.put("R3",       77.250);
        FREQ_OIRT_I_III.put("R4",       85.250);
        FREQ_OIRT_I_III.put("R5",       93.250);

        FREQ_OIRT_I_III.put("R6",	 175.250);
        FREQ_OIRT_I_III.put("R7",	 183.250);
        FREQ_OIRT_I_III.put("R8",	 191.250);
        FREQ_OIRT_I_III.put("R9",	 199.250);
        FREQ_OIRT_I_III.put("R10",	 207.250);
        FREQ_OIRT_I_III.put("R11",	 215.250);
        FREQ_OIRT_I_III.put("R12",	 223.250);

        Map<String,Double> FREQ_OIRT_SL_SH = new HashMap<String,Double>();
        FREQ_OIRT_SL_SH.put("SR1",	 111.250);
        FREQ_OIRT_SL_SH.put("SR2",	 119.250);
        FREQ_OIRT_SL_SH.put("SR3",	 127.250);
        FREQ_OIRT_SL_SH.put("SR4",	 135.250);
        FREQ_OIRT_SL_SH.put("SR5",	 143.250);
        FREQ_OIRT_SL_SH.put("SR6",	 151.250);
        FREQ_OIRT_SL_SH.put("SR7",	 159.250);
        FREQ_OIRT_SL_SH.put("SR8",	 167.250);

        FREQ_OIRT_SL_SH.put("SR11",    231.250);
        FREQ_OIRT_SL_SH.put("SR12",    239.250);
        FREQ_OIRT_SL_SH.put("SR13",    247.250);
        FREQ_OIRT_SL_SH.put("SR14",    255.250);
        FREQ_OIRT_SL_SH.put("SR15",    263.250);
        FREQ_OIRT_SL_SH.put("SR16",    271.250);
        FREQ_OIRT_SL_SH.put("SR17",    279.250);
        FREQ_OIRT_SL_SH.put("SR18",    287.250);
        FREQ_OIRT_SL_SH.put("SR19",    295.250);

        Map<String,Double> FREQ_UHF = new HashMap<String,Double>();
        FREQ_UHF.put("21",  471.250);
        FREQ_UHF.put("22",  479.250);
        FREQ_UHF.put("23",  487.250);
        FREQ_UHF.put("24",  495.250);
        FREQ_UHF.put("25",  503.250);
        FREQ_UHF.put("26",  511.250);
        FREQ_UHF.put("27",  519.250);
        FREQ_UHF.put("28",  527.250);
        FREQ_UHF.put("29",  535.250);
        FREQ_UHF.put("30",  543.250);
        FREQ_UHF.put("31",  551.250);
        FREQ_UHF.put("32",  559.250);
        FREQ_UHF.put("33",  567.250);
        FREQ_UHF.put("34",  575.250);
        FREQ_UHF.put("35",  583.250);
        FREQ_UHF.put("36",  591.250);
        FREQ_UHF.put("37",  599.250);
        FREQ_UHF.put("38",  607.250);
        FREQ_UHF.put("39",  615.250);
        FREQ_UHF.put("40",  623.250);
        FREQ_UHF.put("41",  631.250);
        FREQ_UHF.put("42",  639.250);
        FREQ_UHF.put("43",  647.250);
        FREQ_UHF.put("44",  655.250);
        FREQ_UHF.put("45",  663.250);
        FREQ_UHF.put("46",  671.250);
        FREQ_UHF.put("47",  679.250);
        FREQ_UHF.put("48",  687.250);
        FREQ_UHF.put("49",  695.250);
        FREQ_UHF.put("50",  703.250);
        FREQ_UHF.put("51",  711.250);
        FREQ_UHF.put("52",  719.250);
        FREQ_UHF.put("53",  727.250);
        FREQ_UHF.put("54",  735.250);
        FREQ_UHF.put("55",  743.250);
        FREQ_UHF.put("56",  751.250);
        FREQ_UHF.put("57",  759.250);
        FREQ_UHF.put("58",  767.250);
        FREQ_UHF.put("59",  775.250);
        FREQ_UHF.put("60",  783.250);
        FREQ_UHF.put("61",  791.250);
        FREQ_UHF.put("62",  799.250);
        FREQ_UHF.put("63",  807.250);
        FREQ_UHF.put("64",  815.250);
        FREQ_UHF.put("65",  823.250);
        FREQ_UHF.put("66",  831.250);
        FREQ_UHF.put("67",  839.250);
        FREQ_UHF.put("68",  847.250);
        FREQ_UHF.put("69",  855.250);

		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.EUROPE_WEST, channelMap);
	channelMap.putAll(FREQ_CCIR_I_III);
	channelMap.putAll(FREQ_CCIR_SL_SH);
	channelMap.putAll(FREQ_CCIR_H);
	channelMap.putAll(FREQ_UHF);

		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.EUROPE_EAST, channelMap);
	channelMap.putAll(FREQ_OIRT_I_III);
	channelMap.putAll(FREQ_OIRT_SL_SH);
	channelMap.putAll(FREQ_CCIR_I_III);
	channelMap.putAll(FREQ_CCIR_SL_SH);
	channelMap.putAll(FREQ_CCIR_H);
	channelMap.putAll(FREQ_UHF);

		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.ITALY, channelMap);
        channelMap.put("A",	 53.750);
        channelMap.put("B",	 62.250);
        channelMap.put("C",	 82.250);
        channelMap.put("D",	175.250);
        channelMap.put("E",	183.750);
        channelMap.put("F",	192.250);
        channelMap.put("G",	201.250);
        channelMap.put("H",	210.250);
        channelMap.put("H1",	217.250);
        channelMap.put("H2",	224.250);
	channelMap.putAll(FREQ_UHF);

		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.IRELAND, channelMap);
        channelMap.put("A0",    45.750);
        channelMap.put("A1",    48.000);
        channelMap.put("A2",    53.750);
        channelMap.put("A3",    56.000);
        channelMap.put("A4",    61.750);
        channelMap.put("A5",    64.000);
        channelMap.put("A6",   175.250);
        channelMap.put("A7",   176.000);
        channelMap.put("A8",   183.250);
        channelMap.put("A9",   184.000);
        channelMap.put("A10",   191.250);
        channelMap.put("A11",   192.000);
        channelMap.put("A12",   199.250);
        channelMap.put("A13",   200.000);
        channelMap.put("A14",   207.250);
        channelMap.put("A15",   208.000);
        channelMap.put("A16",   215.250);
        channelMap.put("A17",   216.000);
        channelMap.put("A18",   224.000);
        channelMap.put("A19",   232.000);
        channelMap.put("A20",   248.000);
        channelMap.put("A21",   256.000);
        channelMap.put("A22",   264.000);
        channelMap.put("A23",   272.000);
        channelMap.put("A24",   280.000);
        channelMap.put("A25",   288.000);
        channelMap.put("A26",   296.000);
        channelMap.put("A27",   304.000);
        channelMap.put("A28",   312.000);
        channelMap.put("A29",   320.000);
        channelMap.put("A30",   344.000);
        channelMap.put("A31",   352.000);
        channelMap.put("A32",   408.000);
        channelMap.put("A33",   416.000);
        channelMap.put("A34",   448.000);
        channelMap.put("A35",   480.000);
        channelMap.put("A36",   520.000);
	channelMap.putAll(FREQ_UHF);

		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.FRANCE, channelMap);
        channelMap.put("K01",    47.750);
        channelMap.put("K02",    55.750);
        channelMap.put("K03",    60.500);
        channelMap.put("K04",    63.750);
        channelMap.put("K05",   176.000);
        channelMap.put("K06",   184.000);
        channelMap.put("K07",   192.000);
        channelMap.put("K08",   200.000);
        channelMap.put("K09",   208.000);
        channelMap.put("K10",   216.000);
        channelMap.put("KB",    116.750);
        channelMap.put("KC",    128.750);
        channelMap.put("KD",    140.750);
        channelMap.put("KE",    159.750);
        channelMap.put("KF",    164.750);
        channelMap.put("KG",    176.750);
        channelMap.put("KH",    188.750);
        channelMap.put("KI",    200.750);
        channelMap.put("KJ",    212.750);
        channelMap.put("KK",    224.750);
        channelMap.put("KL",    236.750);
        channelMap.put("KM",    248.750);
        channelMap.put("KN",    260.750);
        channelMap.put("KO",    272.750);
        channelMap.put("KP",    284.750);
        channelMap.put("KQ",    296.750);
        channelMap.put("H01",   303.250);
        channelMap.put("H02",   311.250);
        channelMap.put("H03",   319.250);
        channelMap.put("H04",   327.250);
        channelMap.put("H05",   335.250);
        channelMap.put("H06",   343.250);
        channelMap.put("H07",   351.250);
        channelMap.put("H08",   359.250);
        channelMap.put("H09",   367.250);
        channelMap.put("H10",   375.250);
        channelMap.put("H11",   383.250);
        channelMap.put("H12",   391.250);
        channelMap.put("H13",   399.250);
        channelMap.put("H14",   407.250);
        channelMap.put("H15",   415.250);
        channelMap.put("H16",   423.250);
        channelMap.put("H17",   431.250);
        channelMap.put("H18",   439.250);
        channelMap.put("H19",   447.250);
	channelMap.putAll(FREQ_UHF);

/* --------------------------------------------------------------------- */

		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.NEWZEALAND, channelMap);
        channelMap.put("1", 	  45.250); 
        channelMap.put("2",	  55.250); 
        channelMap.put("3",	  62.250);
        channelMap.put("4",	 175.250);
        channelMap.put("5",	 182.250);
        channelMap.put("6",	 189.250);
        channelMap.put("7",	 196.250);
        channelMap.put("8",	 203.250);
        channelMap.put("9",	 210.250);
        channelMap.put("10",	 217.250);
        channelMap.put("11",	 224.250);
	channelMap.putAll(FREQ_UHF);

/* --------------------------------------------------------------------- */

/* China broadcast */
		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.CHINA_BROADCAST, channelMap);
        channelMap.put("1",	49.750);
        channelMap.put("2",	57.750);
        channelMap.put("3",	65.750);
        channelMap.put("4",	77.250);
        channelMap.put("5",	85.250);
        channelMap.put("6",	112.250);
        channelMap.put("7",	120.250);
        channelMap.put("8",	128.250);
        channelMap.put("9",	136.250);
        channelMap.put("10",	144.250);
        channelMap.put("11",	152.250);
        channelMap.put("12",	160.250);
        channelMap.put("13",	168.250);
        channelMap.put("14",	176.250);
        channelMap.put("15",	184.250);
        channelMap.put("16",	192.250);
        channelMap.put("17",	200.250);
        channelMap.put("18",	208.250);
        channelMap.put("19",	216.250);
        channelMap.put("20",	224.250);
        channelMap.put("21",	232.250);
        channelMap.put("22",	240.250);
        channelMap.put("23",	248.250);
        channelMap.put("24",	256.250);
        channelMap.put("25",	264.250);
        channelMap.put("26",	272.250);
        channelMap.put("27",	280.250);
        channelMap.put("28",	288.250);
        channelMap.put("29",	296.250);
        channelMap.put("30",	304.250);
        channelMap.put("31",	312.250);
        channelMap.put("32",	320.250);
        channelMap.put("33",	328.250);
        channelMap.put("34",	336.250);
        channelMap.put("35",	344.250);
        channelMap.put("36",	352.250);
        channelMap.put("37",	360.250);
        channelMap.put("38",	368.250);
        channelMap.put("39",	376.250);
        channelMap.put("40",	384.250);
        channelMap.put("41",	392.250);
        channelMap.put("42",	400.250);
        channelMap.put("43",	408.250);
        channelMap.put("44",	416.250);
        channelMap.put("45",	424.250);
        channelMap.put("46",	432.250);
        channelMap.put("47",	440.250);
        channelMap.put("48",	448.250);
        channelMap.put("49",	456.250);
        channelMap.put("50",	463.250);
        channelMap.put("51",	471.250);
        channelMap.put("52",	479.250);
        channelMap.put("53",	487.250);
        channelMap.put("54",	495.250);
        channelMap.put("55",	503.250);
        channelMap.put("56",	511.250);
        channelMap.put("57",	519.250);
        channelMap.put("58",	527.250);
        channelMap.put("59",	535.250);
        channelMap.put("60",	543.250);
        channelMap.put("61",	551.250);
        channelMap.put("62",	559.250);
        channelMap.put("63",	607.250);
        channelMap.put("64",	615.250);
        channelMap.put("65",	623.250);
        channelMap.put("66",	631.250);
        channelMap.put("67",	639.250);
        channelMap.put("68",	647.250);
        channelMap.put("69",	655.250);
        channelMap.put("70",	663.250);
        channelMap.put("71",	671.250);
        channelMap.put("72",	679.250);
        channelMap.put("73",	687.250);
        channelMap.put("74",	695.250);
        channelMap.put("75",	703.250);
        channelMap.put("76",	711.250);
        channelMap.put("77",	719.250);
        channelMap.put("78",	727.250);
        channelMap.put("79",	735.250);
        channelMap.put("80",	743.250);
        channelMap.put("81",	751.250);
        channelMap.put("82",	759.250);
        channelMap.put("83",	767.250);
        channelMap.put("84",	775.250);
        channelMap.put("85",	783.250);
        channelMap.put("86",	791.250);
        channelMap.put("87",	799.250);
        channelMap.put("88",	807.250);
        channelMap.put("89",	815.250);
        channelMap.put("90",	823.250);
        channelMap.put("91",	831.250);
        channelMap.put("92",	839.250);
        channelMap.put("93",	847.250);
        channelMap.put("94",	855.250);

/* --------------------------------------------------------------------- */
/* South Africa Broadcast */

		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.SOUTHAFRICA_BROADCAST, channelMap);
        channelMap.put("1", 175.250);
        channelMap.put("2", 183.250);
        channelMap.put("3", 191.250);
        channelMap.put("4", 199.250);
        channelMap.put("5", 207.250);
        channelMap.put("6", 215.250);
        channelMap.put("7", 223.250);
        channelMap.put("8", 231.250);
        channelMap.put("9", 239.250);
        channelMap.put("10", 247.250);
        channelMap.put("11", 255.250);
        channelMap.put("12", 263.250);
        channelMap.put("13", 271.250);
	channelMap.putAll(FREQ_UHF);

/* --------------------------------------------------------------------- */

		channelMap = new HashMap<String,Double>();
		channelToFrequency.put(FrequencyStandard.ARGENTINA_BROADCAST, channelMap);
        channelMap.put("001",   56.250);
        channelMap.put("002",   62.250);
        channelMap.put("003",   68.250);
        channelMap.put("004",   78.250);
        channelMap.put("005",   84.250);
        channelMap.put("006",  176.250);
        channelMap.put("007",  182.250);
        channelMap.put("008",  188.250);
        channelMap.put("009",  194.250);
        channelMap.put("010",  200.250);
        channelMap.put("011",  206.250);
        channelMap.put("012",  212.250);
        channelMap.put("013",  122.250);
        channelMap.put("014",  128.250);
        channelMap.put("015",  134.250);
        channelMap.put("016",  140.250);
        channelMap.put("017",  146.250);
        channelMap.put("018",  152.250);
        channelMap.put("019",  158.250);
        channelMap.put("020",  164.250);
        channelMap.put("021",  170.250);
        channelMap.put("022",  218.250);
        channelMap.put("023",  224.250);
        channelMap.put("024",  230.250);
        channelMap.put("025",  236.250);
        channelMap.put("026",  242.250);
        channelMap.put("027",  248.250);
        channelMap.put("028",  254.250);
        channelMap.put("029",  260.250);
        channelMap.put("030",  266.250);
        channelMap.put("031",  272.250);
        channelMap.put("032",  278.250);
        channelMap.put("033",  284.250);
        channelMap.put("034",  290.250);
        channelMap.put("035",  296.250);
        channelMap.put("036",  302.250);
        channelMap.put("037",  308.250);
        channelMap.put("038",  314.250);
        channelMap.put("039",  320.250);
        channelMap.put("040",  326.250);
        channelMap.put("041",  332.250);
        channelMap.put("042",  338.250);
        channelMap.put("043",  344.250);
        channelMap.put("044",  350.250);
        channelMap.put("045",  356.250);
        channelMap.put("046",  362.250);
        channelMap.put("047",  368.250);
        channelMap.put("048",  374.250);
        channelMap.put("049",  380.250);
        channelMap.put("050",  386.250);
        channelMap.put("051",  392.250);
        channelMap.put("052",  398.250);
        channelMap.put("053",  404.250);
        channelMap.put("054",  410.250);
        channelMap.put("055",  416.250);
        channelMap.put("056",  422.250);
        channelMap.put("057",  428.250);
        channelMap.put("058",  434.250);
        channelMap.put("059",  440.250);
        channelMap.put("060",  446.250);
        channelMap.put("061",  452.250);
        channelMap.put("062",  458.250);
        channelMap.put("063",  464.250);
        channelMap.put("064",  470.250);
        channelMap.put("065",  476.250);
        channelMap.put("066",  482.250);
        channelMap.put("067",  488.250);
        channelMap.put("068",  494.250);
        channelMap.put("069",  500.250);
        channelMap.put("070",  506.250);
        channelMap.put("071",  512.250);
        channelMap.put("072",  518.250);
        channelMap.put("073",  524.250);
        channelMap.put("074",  530.250);
        channelMap.put("075",  536.250);
        channelMap.put("076",  542.250);
        channelMap.put("077",  548.250);
        channelMap.put("078",  554.250);
        channelMap.put("079",  560.250);
        channelMap.put("080",  566.250);
        channelMap.put("081",  572.250);
        channelMap.put("082",  578.250);
        channelMap.put("083",  584.250);
        channelMap.put("084",  590.250);
        channelMap.put("085",  596.250);
        channelMap.put("086",  602.250);
        channelMap.put("087",  608.250);
        channelMap.put("088",  614.250);
        channelMap.put("089",  620.250);
        channelMap.put("090",  626.250);
        channelMap.put("091",  632.250);
        channelMap.put("092",  638.250);
        channelMap.put("093",  644.250);
        
		Map<Double,String> freqMap;
		for (FrequencyStandard fs : channelToFrequency.keySet()) {
			channelMap = channelToFrequency.get(fs);
			freqMap = new HashMap<Double,String>();
			frequencyToChannel.put(fs, freqMap);
			for (String channel : channelMap.keySet()) {
				double freq = channelMap.get(channel);
				freqMap.put(freq, channel);
			}
		}
	}

	/**
	 * @param frequencyStandard a frequency standard, including a table of channels
	 * and their frequencies
	 * @return a Set of channels for that standard
	 */
	public static Set<String> getChannels(FrequencyStandard frequencyStandard) {
		return channelToFrequency.get(frequencyStandard).keySet();
	}

	/**
	 * Returns a frequency for a given channel, if valid for the given standard.
	 * @param frequencyStandard
	 * @param channel
	 * @return the frequency
	 * @throws BadChannelException
	 */
	public static double getFrequency(
				FrequencyStandard frequencyStandard, String channel)
				throws BadChannelException {
		Double frequency = channelToFrequency.get(frequencyStandard).get(channel);
		if (frequency == null) {
			throw new BadChannelException("Channel "+channel+
					" not found in FrequencyStandard "+frequencyStandard);
		}
		return frequency;
	}

	/**
	 * Given a frenquency and known FrequencyStandard, this returns the corresponding
	 * channel.
	 * @param frequencyStandard the table of frequencies vs. channels
	 * @param frequency the frequency to look up.
	 * @return the channel associated with the given frequency for the given
	 * FrequencyStandard
	 * @throws BadChannelException if the given frequency does not exist in the
	 * given FrequencyTable
	 */
	public static String getChannel(FrequencyStandard frequencyStandard, double frequency) 
			throws BadChannelException {
		String channel = frequencyToChannel.get(frequencyStandard).get(frequency);
		if (channel == null) {
			throw new BadChannelException("Frequency "+frequency+
					" not found in FrequencyStandard "+frequencyStandard);
		}
		return channel;
	}
}
