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

import junit.framework.TestCase;

public class VBILineTest extends TestCase {
	
	private static int phase = 0;
	private static DataChannel ch = DataChannel.CC1;
	private static VBICommand command;
	private static VBICommand print;
	
	private TwoByteCode cmd;

	public void testExecuteCommand() {
		
		print = new PrintCommand();
		
		command = new VBICommandAdapter() {
			
			@Override
			public void resumeCaption(VBILine line) {
				phase++;
				assertTrue(cmd == CommandCode.RCL);
			}
			
			@Override
			public void backspace(VBILine line) {
				phase++;
				assertTrue(cmd == CommandCode.BS);
			}
			
			@Override
			public void alarmOff(VBILine line) {
				phase++;
				assertTrue(cmd == CommandCode.AOF);
			}
			
			@Override
			public void alarmOn(VBILine line) {
				phase++;
				assertTrue(cmd == CommandCode.AON);
			}
			
			@Override
			public void deleteToEndOfRow(VBILine line) {
				phase++;
				assertTrue(cmd == CommandCode.DER);
			}
			
			@Override
			public void rollUpCaption(VBILine line, int rows) {
				phase++;
				if (rows == 2) {
					assertTrue(cmd == CommandCode.RU2);
				} else if (rows == 3) {
					assertTrue(cmd == CommandCode.RU3);
				} else if (rows == 4) {
					assertTrue(cmd == CommandCode.RU4);
				} else {
					fail("bad rollup rows");
				}
			}
			
			@Override
			public void flashOn(VBILine line) {
				phase++;
				assertTrue(cmd == CommandCode.FON);
			}
			
			@Override
			public void resumeDirect(VBILine line) {
				phase++;
				assertTrue(cmd == CommandCode.RDC);
			}
			
			@Override
			public void textRestart(VBILine line) {
				phase++;
				assertTrue(cmd == CommandCode.TR);
			}
			
			@Override
			public void resumeText(VBILine line) {
				phase++;
				assertTrue(cmd == CommandCode.RTD);
			}
			
			@Override
			public void eraseDisplayed(VBILine line) {
				phase++;
				assertTrue(cmd == CommandCode.EDM);
			}
			
			@Override
			public void carriageReturn(VBILine line) {
				phase++;
				assertTrue(cmd == CommandCode.CR);
			}
			
			@Override
			public void eraseNonDisplayed(VBILine line) {
				phase++;
				assertTrue(cmd == CommandCode.ENM);
			}
			
			@Override
			public void endOfCaption(VBILine line) {
				phase++;
				assertTrue(cmd == CommandCode.EOC);
			}
			
			@Override
			public void tab(VBILine line, int offset) {
				phase++;
				if (offset == 1) {
					assertTrue(cmd == CommandCode.TO1);
				} else if (offset == 2) {
					assertTrue(cmd == CommandCode.TO2);
				} else if (offset == 3) {
					assertTrue(cmd == CommandCode.TO3);
				} else {
					fail("bad tab offset");
				}
			}
		};
		
		test(CommandCode.RCL);
		test(CommandCode.BS);
		test(CommandCode.AOF);
		test(CommandCode.AON);
		test(CommandCode.DER);
		test(CommandCode.RU2);
		test(CommandCode.RU3);
		test(CommandCode.RU4);
		test(CommandCode.FON);
		test(CommandCode.RDC);
		test(CommandCode.TR);
		test(CommandCode.RTD);
		test(CommandCode.EDM);
		test(CommandCode.CR);
		test(CommandCode.ENM);
		test(CommandCode.EOC);
		test(CommandCode.TO1);
		test(CommandCode.TO2);
		test(CommandCode.TO3);
	}
	
	private void test(TwoByteCode code) {
		int phasewas = phase;
		cmd = code;
		VBILine vbi = new VBILine();
		vbi.setValues(ch, code);
		vbi.executeCommand(print);
		vbi.executeCommand(command);
		assertTrue(phase == (phasewas+1));
	}
	
	public void testExtendedChars() {
		
		LineBuffer lineBuffer = new LineBuffer() {

			@Override
			public void lineReady(String s) {
				System.out.println(s);
				StringBuffer expected = new StringBuffer();
				expected.append(ExtendedChar.REGISTERED_TRADEMARK.getUnicode());
				expected.append(ExtendedChar.DEGREE.getUnicode());
				expected.append(ExtendedChar.ONE_HALF.getUnicode());
				expected.append(ExtendedChar.INVERTED_QUESTION_MARK.getUnicode());
				expected.append(ExtendedChar.TRADEMARK.getUnicode());
				expected.append(ExtendedChar.CENTS.getUnicode());
				expected.append(ExtendedChar.POUNDS_STERLING.getUnicode());
				expected.append(ExtendedChar.MUSIC_NOTE.getUnicode());
				expected.append(ExtendedChar.LC_A_WITH_GRAVE.getUnicode());
				expected.append(ExtendedChar.TRANSPARENT_SPACE.getUnicode());
				expected.append(ExtendedChar.LC_E_WITH_GRAVE.getUnicode());
				expected.append(ExtendedChar.LC_A_WITH_CIRCUMFLEX.getUnicode());
				expected.append(ExtendedChar.LC_E_WITH_CIRCUMFLEX.getUnicode());
				expected.append(ExtendedChar.LC_I_WITH_CIRCUMFLEX.getUnicode());
				expected.append(ExtendedChar.LC_O_WITH_CIRCUMFLEX.getUnicode());
				expected.append(ExtendedChar.LC_U_WITH_CIRCUMFLEX.getUnicode());
				assertTrue(s.equals(expected.toString()));
			}
		};

		VBILine vbi = new VBILine();
		vbi.setValues(ch, ExtendedChar.REGISTERED_TRADEMARK);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(ch, ExtendedChar.DEGREE);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(ch, ExtendedChar.ONE_HALF);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(ch, ExtendedChar.INVERTED_QUESTION_MARK);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(ch, ExtendedChar.TRADEMARK);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(ch, ExtendedChar.CENTS);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(ch, ExtendedChar.POUNDS_STERLING);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(ch, ExtendedChar.MUSIC_NOTE);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(ch, ExtendedChar.LC_A_WITH_GRAVE);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(ch, ExtendedChar.TRANSPARENT_SPACE);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(ch, ExtendedChar.LC_E_WITH_GRAVE);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(ch, ExtendedChar.LC_A_WITH_CIRCUMFLEX);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(ch, ExtendedChar.LC_E_WITH_CIRCUMFLEX);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(ch, ExtendedChar.LC_I_WITH_CIRCUMFLEX);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(ch, ExtendedChar.LC_O_WITH_CIRCUMFLEX);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(ch, ExtendedChar.LC_U_WITH_CIRCUMFLEX);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(DataChannel.CC1, CommandCode.CR);
		vbi.executeCommand(lineBuffer);
	}
		
	public void testModifiedChars() {
		LineBuffer lineBuffer = new LineBuffer() {

			@Override
			public void lineReady(String s) {
				System.out.println(s);
				StringBuffer expected = new StringBuffer();
				expected.append(" !\"#$%&'()");
				expected.append(ModifiedChar.LC_A_ACUTE.getUnicode());
				expected.append('+');
				expected.append(ModifiedChar.LC_E_ACUTE.getUnicode());
				expected.append(ModifiedChar.LC_I_ACUTE.getUnicode());
				expected.append(ModifiedChar.LC_O_ACUTE.getUnicode());
				expected.append(ModifiedChar.LC_U_ACUTE.getUnicode());
				expected.append(ModifiedChar.LC_C_CEDILLA.getUnicode());
				expected.append(ModifiedChar.DIVISION.getUnicode());
				expected.append(ModifiedChar.UC_N_TILDE.getUnicode());
				expected.append(ModifiedChar.LC_N_TILDE.getUnicode());
				expected.append(ModifiedChar.SOLID_BLOCK.getUnicode());
				assertTrue(s.equals(expected.toString()));
			}
		};

		VBILine vbi = new VBILine();
		vbi.setValues(21, ' ', '!');
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, '"', '#');
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, '$', '%');
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, '&', '\'');
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, '(', ')');
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, '*', '+');
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, '\\', '^');
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, '_', '`');
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, '{', '|');
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, '}', '~');
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x7F, 0);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(DataChannel.CC1, CommandCode.CR);
		vbi.executeCommand(lineBuffer);
	}
		
	public void testBookSample1() {
		LineBuffer lineBuffer = new LineBuffer() {

			@Override
			public void lineReady(String s) {
				System.out.println(s);
				assertTrue(s.equals("STRAIGHT TEXT"));
			}
		};

		VBILine vbi = new VBILine();
		vbi.setValues(21, 0x53, 0x54);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x52, 0x41);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x49, 0x47);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x48, 0x54);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x20, 0x54);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x45, 0x58);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x54, 0);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(DataChannel.CC1, CommandCode.CR);
		vbi.executeCommand(lineBuffer);
	}
		
	public void testBookSample2() {
		LineBuffer lineBuffer = new LineBuffer() {

			@Override
			public void lineReady(String s) {
				System.out.println(s);
				assertTrue(s.equals("ONE ITALIC WORD"));
			}
		};

		VBILine vbi = new VBILine();
		vbi.setValues(21, 0x4F, 0x4E);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x45, 0);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x11, 0x2E);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x49, 0);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x54, 0x41);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x4C, 0x49);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x43, 0x00);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x11, 0x20);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x57, 0x4F);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(21, 0x52, 0x44);
		vbi.executeCommand(lineBuffer);
		vbi.setValues(DataChannel.CC1, CommandCode.CR);
		vbi.executeCommand(lineBuffer);
		
		System.out.println();
	}
}
