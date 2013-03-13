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
 * Manages a list of international VideoStandards.  The VideoStandard(s)
 * supported by a given VideoDevice may include more than one VideoStandard
 * so a mask is used to check for more than one VideoStandard and return
 * the correct results.
 * @author Rich Kadel
 * @author $Author: kadel $
 * @version $Rev: 75 $ $Date: 2006-07-22 01:42:31 -0700 (Sat, 22 Jul 2006) $
 */
public final class VideoStandard {
	
	public static VideoStandard PAL_B = new VideoStandard(0x00000001);
	public static VideoStandard PAL_B1 = new VideoStandard(0x00000002);
	public static VideoStandard PAL_G = new VideoStandard(0x00000004);
	public static VideoStandard PAL_H = new VideoStandard(0x00000008);
	public static VideoStandard PAL_I = new VideoStandard(0x00000010);
	public static VideoStandard PAL_D = new VideoStandard(0x00000020);
	public static VideoStandard PAL_D1 = new VideoStandard(0x00000040);
	public static VideoStandard PAL_K = new VideoStandard(0x00000080);

	public static VideoStandard PAL_M = new VideoStandard(0x00000100);
	public static VideoStandard PAL_N = new VideoStandard(0x00000200);
	public static VideoStandard PAL_Nc = new VideoStandard(0x00000400);
	public static VideoStandard PAL_60 = new VideoStandard(0x00000800);

	public static VideoStandard NTSC_M = new VideoStandard(0x00001000);
	public static VideoStandard NTSC_M_JP = new VideoStandard(0x00002000);

	public static VideoStandard SECAM_B = new VideoStandard(0x00010000);
	public static VideoStandard SECAM_D = new VideoStandard(0x00020000);
	public static VideoStandard SECAM_G = new VideoStandard(0x00040000);
	public static VideoStandard SECAM_H = new VideoStandard(0x00080000);
	public static VideoStandard SECAM_K = new VideoStandard(0x00100000);
	public static VideoStandard SECAM_K1 = new VideoStandard(0x00200000);
	public static VideoStandard SECAM_L = new VideoStandard(0x00400000);

	/* ATSC/HDTV */
	public static VideoStandard ATSC_8_VSB = new VideoStandard(0x01000000);
	public static VideoStandard ATSC_16_VSB = new VideoStandard(0x02000000);
	
	private long linuxVideoStandard;
	
	/* package scope for now */ VideoStandard(long linuxVideoStandard) {
		this.linuxVideoStandard = linuxVideoStandard;
		// If Linux ever changes the mask values, change this constructor
		// to call a native method to get the new values:
		// this.linuxVideoStandard =
		//    VideoDevice.getUpdatedLinuxVideoStandard(name());
		// The native method would be something like:
		//	  if (strcmp(name, "SECAM_B"))
		//        return V4L2_STD_SECAM_B;
		//    or get it from a hashmap or something similar in C++
	}

	/* package scope */ long getLinuxVideoStandard() {
		return linuxVideoStandard;
	}
	
// FOR REFERENCE... Unfortunately, I know things can change, so this file will
// need to be kept in sync somehow.
// For now, using CentOS 4.3, these are in sync, so I won't bother to
// do anything special.  But in the future, we might need a native
// method that passes in the old value and returns the new one if any of
// the max values changed.  See commented out code in constructor.
	
/*
	typedef __u64 v4l2_std_id;

	/ * one bit for each * /
	#define V4L2_STD_PAL_B          ((v4l2_std_id)0x00000001)
	#define V4L2_STD_PAL_B1         ((v4l2_std_id)0x00000002)
	#define V4L2_STD_PAL_G          ((v4l2_std_id)0x00000004)
	#define V4L2_STD_PAL_H          ((v4l2_std_id)0x00000008)
	#define V4L2_STD_PAL_I          ((v4l2_std_id)0x00000010)
	#define V4L2_STD_PAL_D          ((v4l2_std_id)0x00000020)
	#define V4L2_STD_PAL_D1         ((v4l2_std_id)0x00000040)
	#define V4L2_STD_PAL_K          ((v4l2_std_id)0x00000080)

	#define V4L2_STD_PAL_M          ((v4l2_std_id)0x00000100)
	#define V4L2_STD_PAL_N          ((v4l2_std_id)0x00000200)
	#define V4L2_STD_PAL_Nc         ((v4l2_std_id)0x00000400)
	#define V4L2_STD_PAL_60         ((v4l2_std_id)0x00000800)

	#define V4L2_STD_NTSC_M         ((v4l2_std_id)0x00001000)
	#define V4L2_STD_NTSC_M_JP      ((v4l2_std_id)0x00002000)

	#define V4L2_STD_SECAM_B        ((v4l2_std_id)0x00010000)
	#define V4L2_STD_SECAM_D        ((v4l2_std_id)0x00020000)
	#define V4L2_STD_SECAM_G        ((v4l2_std_id)0x00040000)
	#define V4L2_STD_SECAM_H        ((v4l2_std_id)0x00080000)
	#define V4L2_STD_SECAM_K        ((v4l2_std_id)0x00100000)
	#define V4L2_STD_SECAM_K1       ((v4l2_std_id)0x00200000)
	#define V4L2_STD_SECAM_L        ((v4l2_std_id)0x00400000)

	/ * ATSC/HDTV * /
	#define V4L2_STD_ATSC_8_VSB     ((v4l2_std_id)0x01000000)
	#define V4L2_STD_ATSC_16_VSB    ((v4l2_std_id)0x02000000)

	/ * some common needed stuff * /
	#define V4L2_STD_PAL_BG		(V4L2_STD_PAL_B		|\
					 V4L2_STD_PAL_B1	|\
					 V4L2_STD_PAL_G)
	#define V4L2_STD_PAL_DK		(V4L2_STD_PAL_D		|\
					 V4L2_STD_PAL_D1	|\
					 V4L2_STD_PAL_K)
	#define V4L2_STD_PAL		(V4L2_STD_PAL_BG	|\
					 V4L2_STD_PAL_DK	|\
					 V4L2_STD_PAL_H		|\
					 V4L2_STD_PAL_I)
	#define V4L2_STD_NTSC           (V4L2_STD_NTSC_M	|\
					 V4L2_STD_NTSC_M_JP)
	#define V4L2_STD_SECAM_DK      	(V4L2_STD_SECAM_D	|\
					 V4L2_STD_SECAM_K	|\
					 V4L2_STD_SECAM_K1)
	#define V4L2_STD_SECAM		(V4L2_STD_SECAM_B	|\
					 V4L2_STD_SECAM_G	|\
					 V4L2_STD_SECAM_H	|\
					 V4L2_STD_SECAM_DK	|\
					 V4L2_STD_SECAM_L)

	#define V4L2_STD_525_60		(V4L2_STD_PAL_M		|\
					 V4L2_STD_PAL_60	|\
					 V4L2_STD_NTSC)
	#define V4L2_STD_625_50		(V4L2_STD_PAL		|\
					 V4L2_STD_PAL_N		|\
					 V4L2_STD_PAL_Nc	|\
					 V4L2_STD_SECAM)
	#define V4L2_STD_ATSC           (V4L2_STD_ATSC_8_VSB    |\
			                 V4L2_STD_ATSC_16_VSB)

	#define V4L2_STD_UNKNOWN        0
	#define V4L2_STD_ALL            (V4L2_STD_525_60	|\
					 V4L2_STD_625_50)
*/
	private final long v() { // for brevity only
		return linuxVideoStandard;
	}
	
	public boolean isPAL_B() {
		return(0 != (linuxVideoStandard & PAL_B.v()));
	}
	
	public boolean isPAL_B1() {
		return(0 != (linuxVideoStandard & PAL_B1.v()));
	}
	
	public boolean isPAL_G() {
		return(0 != (linuxVideoStandard & PAL_G.v()));
	}
	
	public boolean isPAL_H() {
		return(0 != (linuxVideoStandard & PAL_H.v()));
	}
	
	public boolean isPAL_I() {
		return(0 != (linuxVideoStandard & PAL_I.v()));
	}
	
	public boolean isPAL_D() {
		return(0 != (linuxVideoStandard & PAL_D.v()));
	}
	
	public boolean isPAL_D1() {
		return(0 != (linuxVideoStandard & PAL_D1.v()));
	}
	
	public boolean isPAL_K() {
		return(0 != (linuxVideoStandard & PAL_K.v()));
	}
	
	public boolean isPAL_M() {
		return(0 != (linuxVideoStandard & PAL_M.v()));
	}
	
	public boolean isPAL_N() {
		return(0 != (linuxVideoStandard & PAL_N.v()));
	}
	
	public boolean isPAL_Nc() {
		return(0 != (linuxVideoStandard & PAL_Nc.v()));
	}
	
	public boolean isPAL_60() {
		return(0 != (linuxVideoStandard & PAL_60.v()));
	}
	
	public boolean isNTSC_M() {
		return(0 != (linuxVideoStandard & NTSC_M.v()));
	}
	
	public boolean isNTSC_M_JP() {
		return(0 != (linuxVideoStandard & NTSC_M_JP.v()));
	}
	
	public boolean isSECAM_B() {
		return(0 != (linuxVideoStandard & SECAM_B.v()));
	}
	
	public boolean isSECAM_D() {
		return(0 != (linuxVideoStandard & SECAM_D.v()));
	}
	
	public boolean isSECAM_G() {
		return(0 != (linuxVideoStandard & SECAM_G.v()));
	}
	
	public boolean isSECAM_H() {
		return(0 != (linuxVideoStandard & SECAM_H.v()));
	}
	
	public boolean isSECAM_K() {
		return(0 != (linuxVideoStandard & SECAM_K.v()));
	}
	
	public boolean isSECAM_K1() {
		return(0 != (linuxVideoStandard & SECAM_K1.v()));
	}
	
	public boolean isSECAM_L() {
		return(0 != (linuxVideoStandard & SECAM_L.v()));
	}
	
	public boolean isATSC_8_VSB() {
		return(0 != (linuxVideoStandard & ATSC_8_VSB.v()));
	}
	
	public boolean isATSC_16_VSB() {
		return(0 != (linuxVideoStandard & ATSC_16_VSB.v()));
	}
	
	public boolean isPAL_BG() {
		return(0 != (linuxVideoStandard & (PAL_B.v() | PAL_B1.v()	| PAL_G.v())));
	}
	
	public boolean isPAL_DK() {
		return(0 != (linuxVideoStandard & (PAL_D.v() | PAL_D1.v() | PAL_K.v())));
	}
	
	public boolean isPAL() {
		return(isPAL_BG() || isPAL_DK() ||
				(0 != (linuxVideoStandard & (PAL_H.v() | PAL_I.v()))));
	}
	
	public boolean isNTSC() {
		return(0 != (linuxVideoStandard & (NTSC_M.v() | NTSC_M_JP.v())));
	}
	
	public boolean isSECAM_DK() {
		return(0 != (linuxVideoStandard & (SECAM_D.v() | SECAM_K.v() | SECAM_K1.v())));
	}
	
	public boolean isSECAM() {
		return(isSECAM_DK() ||
				(0 != (linuxVideoStandard &
						(SECAM_B.v() | SECAM_G.v() | SECAM_H.v() | SECAM_L.v()))));
	}
	

	public boolean is525_60() {
		return(isNTSC() ||
				(0 != (linuxVideoStandard & (PAL_M.v() | PAL_60.v()))));
	}
	
	public boolean is625_50() {
		return(isPAL() || isSECAM() ||
				(0 != (linuxVideoStandard & (PAL_N.v() | PAL_Nc.v()))));
	}
	
	public boolean isATSC() {
		return(0 != (linuxVideoStandard & (ATSC_8_VSB.v() | ATSC_16_VSB.v())));
	}

	public boolean isALL() {
		return (is525_60() || is625_50());
	}
}
