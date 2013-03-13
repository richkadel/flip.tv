#include <iostream>
#include <string>
#include <sstream>
#include <map>
#include <jni.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <linux/videodev.h>
#include <linux/videodev2.h>
#include "zvbi-0.2.25/libzvbi.h"

#include <com_knowbout_cc4j_VBIDevice.h>

unsigned int services =
    VBI_SLICED_VBI_525 
        | VBI_SLICED_CAPTION_525;
        /*
    VBI_SLICED_VBI_525 | VBI_SLICED_VBI_625
        | VBI_SLICED_TELETEXT_B | VBI_SLICED_CAPTION_525
        | VBI_SLICED_CAPTION_625 | VBI_SLICED_VPS
        | VBI_SLICED_WSS_625 | VBI_SLICED_WSS_CPR1204;
        */

struct vbi_state {
	vbi_capture* cap;
	int src_w, src_h;
	uint8_t* raw;
	vbi_sliced* sliced;
	vbi_sliced* slice;
	int lines;
	double timestamp;
	struct timeval tv;
};

static std::map<int, vbi_state*> vbi_map;

//static vbi_bool			option_decode_caption;
/*
static vbi_bool			option_decode_xds;
*/
/* Demultiplexers. */
    
/*
static vbi_xds_demux *		xds;
*/


using std::string;
using std::cerr;
using std::endl;

/*
 * Class:     com_knowbout_cc4j_VBIDevice
 * Method:    open
 * Signature: (I)V
 */
JNIEXPORT jint JNICALL Java_com_knowbout_cc4j_VBIDevice_open
	(JNIEnv* env, jobject jobj, jint deviceNumber) {

	std::ostringstream ostr;
	ostr << "/dev/vbi" << deviceNumber;
	string devString = ostr.str();

	int fd = open(devString.c_str(), O_RDWR);

	if (fd == -1) {
		string message = "Could not open " + devString;
		env->ThrowNew(env->FindClass("java/io/IOException"), message.c_str());
	}
	vbi_state* vbi = new vbi_state();
	env->MonitorEnter(env->GetObjectClass(jobj));
	vbi_map[fd] = vbi;
	env->MonitorExit(env->GetObjectClass(jobj));
	
	char* errstr;
	vbi->cap = vbi_capture_v4l2_new (devString.c_str(),
				    /* buffers */ 5,
				    &services,
				    0,
				    &errstr,
				    false);
				    
	if (vbi->cap == NULL) {
		string message = "Cannot capture vbi data with v4l2 interface:\n"
			+ string(errstr);
		env->ThrowNew(env->FindClass("java/io/IOException"), message.c_str());
		free (errstr);
		return -1;
	}

	vbi_raw_decoder* par = vbi_capture_parameters(vbi->cap);
	if (par == 0) {
		string message = "vbi_capture_parameters(cap) failed";
		env->ThrowNew(env->FindClass("java/lang/Error"), message.c_str());
	}
	
	vbi->src_w = par->bytes_per_line / 1;
	vbi->src_h = par->count[0] + par->count[1];

	vbi->tv.tv_sec = 2;
	vbi->tv.tv_usec = 0;

	vbi->raw = (uint8_t*)malloc(vbi->src_w * vbi->src_h);
	vbi->sliced = (vbi_sliced*)malloc(sizeof(vbi_sliced) * vbi->src_h);
	
	if (vbi->raw == 0 || vbi->sliced == 0) {
		string message = "failed to allocate VBI raw and sliced memory";
		env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"), message.c_str());
	}
	
	/*

	if (option_decode_xds) {
		xds = vbi_xds_demux_new (xds_cb,
					 / * used_data * / NULL);
		if (NULL == xds)
			no_mem_exit ();
	}
	*/
	
	return fd;
}

/*
 * Class:     com_knowbout_cc4j_VBIDevice
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_knowbout_cc4j_VBIDevice_closeFile
  (JNIEnv* env, jobject jobj, jint fd) {
  	
	env->MonitorEnter(env->GetObjectClass(jobj));
  	vbi_state* vbi = vbi_map[fd];
	env->MonitorExit(env->GetObjectClass(jobj));
  	if (vbi->cap != NULL) {
		vbi_capture_delete(vbi->cap);
  	}
	delete vbi;
	env->MonitorEnter(env->GetObjectClass(jobj));
	vbi_map.erase(fd);
	env->MonitorExit(env->GetObjectClass(jobj));
	
	//what about deleting the par? No need.  It is part of the cap.
	/*
	vbi_xds_demux_delete (xds);
	*/

	close(fd); /* MOVE TO CLOSE */
}

/*
 *  libzvbi test
 *
 *  Copyright (C) 2000-2006 Michael H. Schimek
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

// Put this read in a loop...
/*
 * Class:     com_knowbout_cc4j_VBIDevice
 * Method:    readVBI
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_knowbout_cc4j_VBIDevice_readVBI
  (JNIEnv* env, jobject jobj, jint fd, jobject vbiline) {
  	
	env->MonitorEnter(env->GetObjectClass(jobj));
  	vbi_state* vbi = vbi_map[fd];
	env->MonitorExit(env->GetObjectClass(jobj));
	
  	while (true) {
	  	
	  	if (vbi->lines < 1) {
			int r = vbi_capture_read(vbi->cap, vbi->raw, vbi->sliced,
					     &vbi->lines, &vbi->timestamp, &vbi->tv);
					     
			vbi->slice = vbi->sliced;
		
		    if (r == -1) {
				string message = "VBI read error: ";
				message+=" - " + string(strerror(errno));
				env->ThrowNew(env->FindClass("java/io/IOException"), message.c_str());
				return;
			} else if (r == 0) {
				string message = "VBI read timeout";
				env->ThrowNew(env->FindClass("java/io/IOException"), message.c_str());
				// we might chose to ignore these?
				return;
			}
	  	}
	
		while (vbi->lines > 0) {
			if (vbi->slice->id == VBI_SLICED_CAPTION_525_F1 ||
				vbi->slice->id == VBI_SLICED_CAPTION_525_F2 ||
				vbi->slice->id == VBI_SLICED_CAPTION_525) {
	
		    	if (/*option_decode_caption
		    	    &&*/ (vbi->slice->line == 21 || vbi->slice->line == 284 /* NTSC */
		    		|| vbi->slice->line == 22 /* PAL */)) {
		    			
		    		int c1 = vbi_unpar8 (vbi->slice->data[0]);
		    		int c2 = vbi_unpar8 (vbi->slice->data[1]);
		    		
		    	    jclass cls = env->GetObjectClass(vbiline);
		    	    
				    jmethodID mid = env->GetMethodID(cls, "setValues", "(III)V");
				    env->CallVoidMethod(vbiline, mid, vbi->slice->line, c1, c2);
		    		
					++vbi->slice;
					--vbi->lines;
			
					return;
				} else {
					++vbi->slice;
					--vbi->lines;
				}	
			}
		}
  	}
}


/*
static vbi_bool
xds_cb				(vbi_xds_demux *	xd,
				 const vbi_xds_packet *	xp,
				 void *			user_data)
{
	xd = xd;
	user_data = user_data;

	_vbi_xds_packet_dump (xp, stdout);

	return TRUE; / * no errors * /
}
*/


