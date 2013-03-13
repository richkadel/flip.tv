#include <iostream>
#include <string>
#include <sstream>
#include <map>
#include <exception> // defines "exception" exception for STL classes
//#include <new> //defines "bad_alloc" exception
#include <windows.h>
#include <jni.h>
#include <com_knowbout_cc4j_VideoDevice.h>
#include <com_knowbout_cc4j_VBIDevice.h>

/* ETS 300 706, Section 8.3 Hamming 18/24 (code from AleVT) */

/* This table generates the parity checks for hamm24/18 decoding.
   Bit 0 is for test A, 1 for B, ...
   Thanks to R. Gancarz for this fine table *g* */
static const signed char
_vbi_hamm24_inv_par [3][256] = {
    {
        /* Parities of first byte */
     0, 33, 34,  3, 35,  2,  1, 32, 36,  5,  6, 39,  7, 38, 37,  4,
    37,  4,  7, 38,  6, 39, 36,  5,  1, 32, 35,  2, 34,  3,  0, 33,
    38,  7,  4, 37,  5, 36, 39,  6,  2, 35, 32,  1, 33,  0,  3, 34,
     3, 34, 33,  0, 32,  1,  2, 35, 39,  6,  5, 36,  4, 37, 38,  7,

    39,  6,  5, 36,  4, 37, 38,  7,  3, 34, 33,  0, 32,  1,  2, 35,
     2, 35, 32,  1, 33,  0,  3, 34, 38,  7,  4, 37,  5, 36, 39,  6,
     1, 32, 35,  2, 34,  3,  0, 33, 37,  4,  7, 38,  6, 39, 36,  5,
    36,  5,  6, 39,  7, 38, 37,  4,  0, 33, 34,  3, 35,  2,  1, 32,

    40,  9, 10, 43, 11, 42, 41,  8, 12, 45, 46, 15, 47, 14, 13, 44,
    13, 44, 47, 14, 46, 15, 12, 45, 41,  8, 11, 42, 10, 43, 40,  9,
    14, 47, 44, 13, 45, 12, 15, 46, 42, 11,  8, 41,  9, 40, 43, 10,
    43, 10,  9, 40,  8, 41, 42, 11, 15, 46, 45, 12, 44, 13, 14, 47,

    15, 46, 45, 12, 44, 13, 14, 47, 43, 10,  9, 40,  8, 41, 42, 11,
    42, 11,  8, 41,  9, 40, 43, 10, 14, 47, 44, 13, 45, 12, 15, 46,
    41,  8, 11, 42, 10, 43, 40,  9, 13, 44, 47, 14, 46, 15, 12, 45,
    12, 45, 46, 15, 47, 14, 13, 44, 40,  9, 10, 43, 11, 42, 41,  8
    }, {
        /* Parities of second byte */
     0, 41, 42,  3, 43,  2,  1, 40, 44,  5,  6, 47,  7, 46, 45,  4,
    45,  4,  7, 46,  6, 47, 44,  5,  1, 40, 43,  2, 42,  3,  0, 41,
    46,  7,  4, 45,  5, 44, 47,  6,  2, 43, 40,  1, 41,  0,  3, 42,
     3, 42, 41,  0, 40,  1,  2, 43, 47,  6,  5, 44,  4, 45, 46,  7,

    47,  6,  5, 44,  4, 45, 46,  7,  3, 42, 41,  0, 40,  1,  2, 43,
     2, 43, 40,  1, 41,  0,  3, 42, 46,  7,  4, 45,  5, 44, 47,  6,
     1, 40, 43,  2, 42,  3,  0, 41, 45,  4,  7, 46,  6, 47, 44,  5,
    44,  5,  6, 47,  7, 46, 45,  4,  0, 41, 42,  3, 43,  2,  1, 40,

    48, 25, 26, 51, 27, 50, 49, 24, 28, 53, 54, 31, 55, 30, 29, 52,
    29, 52, 55, 30, 54, 31, 28, 53, 49, 24, 27, 50, 26, 51, 48, 25,
    30, 55, 52, 29, 53, 28, 31, 54, 50, 27, 24, 49, 25, 48, 51, 26,
    51, 26, 25, 48, 24, 49, 50, 27, 31, 54, 53, 28, 52, 29, 30, 55,

    31, 54, 53, 28, 52, 29, 30, 55, 51, 26, 25, 48, 24, 49, 50, 27,
    50, 27, 24, 49, 25, 48, 51, 26, 30, 55, 52, 29, 53, 28, 31, 54,
    49, 24, 27, 50, 26, 51, 48, 25, 29, 52, 55, 30, 54, 31, 28, 53,
    28, 53, 54, 31, 55, 30, 29, 52, 48, 25, 26, 51, 27, 50, 49, 24
    }, {
        /* Parities of third byte, xor 0x3F */
    63, 14, 13, 60, 12, 61, 62, 15, 11, 58, 57,  8, 56,  9, 10, 59,
    10, 59, 56,  9, 57,  8, 11, 58, 62, 15, 12, 61, 13, 60, 63, 14,
     9, 56, 59, 10, 58, 11,  8, 57, 61, 12, 15, 62, 14, 63, 60, 13,
    60, 13, 14, 63, 15, 62, 61, 12,  8, 57, 58, 11, 59, 10,  9, 56,

     8, 57, 58, 11, 59, 10,  9, 56, 60, 13, 14, 63, 15, 62, 61, 12,
    61, 12, 15, 62, 14, 63, 60, 13,  9, 56, 59, 10, 58, 11,  8, 57,
    62, 15, 12, 61, 13, 60, 63, 14, 10, 59, 56,  9, 57,  8, 11, 58,
    11, 58, 57,  8, 56,  9, 10, 59, 63, 14, 13, 60, 12, 61, 62, 15,

    31, 46, 45, 28, 44, 29, 30, 47, 43, 26, 25, 40, 24, 41, 42, 27,
    42, 27, 24, 41, 25, 40, 43, 26, 30, 47, 44, 29, 45, 28, 31, 46,
    41, 24, 27, 42, 26, 43, 40, 25, 29, 44, 47, 30, 46, 31, 28, 45,
    28, 45, 46, 31, 47, 30, 29, 44, 40, 25, 26, 43, 27, 42, 41, 24,

    40, 25, 26, 43, 27, 42, 41, 24, 28, 45, 46, 31, 47, 30, 29, 44,
    29, 44, 47, 30, 46, 31, 28, 45, 41, 24, 27, 42, 26, 43, 40, 25,
    30, 47, 44, 29, 45, 28, 31, 46, 42, 27, 24, 41, 25, 40, 43, 26,
    43, 26, 25, 40, 24, 41, 42, 27, 31, 46, 45, 28, 44, 29, 30, 47
    }
};

struct State {
	void* devInfo;
	jobject vbiDevice;
	JNIEnv* pushThread;
	bool closed;
	bool reusable;
};

using std::string;
using std::cerr;
using std::endl;
using std::exception;
using std::bad_alloc;

extern string KNOWBOUT_VIDEO_OpenDevice(int deviceNumber, void** devInfo);
extern string KNOWBOUT_VIDEO_CloseDevice(void* devInfo);
extern string KNOWBOUT_VIDEO_SetChannel(void* devInfo, int channel);
extern int KNOWBOUT_VIDEO_GetChannel(void* devInfo);
extern wchar_t* KNOWBOUT_VIDEO_GetDeviceName(void* devInfo);

static jclass videoDeviceClass = NULL;
static JavaVM* vm = NULL;

static std::map<int, State*> stateMap;

static int
vbi_unpar8			(unsigned int		c)
{
	/*
#ifdef __GNUC__
#if #cpu (i686)
	int r = c & 127;

	/* This saves cache flushes and an explicit branch. *//*
	__asm__ (" testb	%1,%1\n"
		 " cmovp	%2,%0\n"
		 : "+&a" (r) : "c" (c), "rm" (-1));
	return r;
#endif
#endif
*/
	if (_vbi_hamm24_inv_par[0][(unsigned char) c] & 32) {
		return c & 127;
	} else {
		/* The idea is to OR results together to find a parity
		   error in a sequence, rather than a test and branch on
		   each byte. */
		return -1;
	}
}

/*
 * Class:     com_knowbout_cc4j_VideoDevice
 * Method:    open
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT jint JNICALL Java_com_knowbout_cc4j_VideoDevice_open
	(JNIEnv* env, jobject jobj, jint deviceNumber) {

	int fd = deviceNumber;
	try {
  		if (videoDeviceClass == NULL) {
  			videoDeviceClass = (jclass)env->NewGlobalRef(env->GetObjectClass(jobj));
	  		env->GetJavaVM(&vm);
  		}
	  	
		env->MonitorEnter(env->GetObjectClass(jobj));
		State* state = stateMap[fd];
		if (state == NULL) {
			state = new State();
			stateMap[fd] = state;
		} else if ((state->closed && state->reusable)) {
			state->closed = false;
			state->reusable = false;
		} else {
			fd = -1;
		}

		env->MonitorExit(env->GetObjectClass(jobj));
		
		if (fd == -1) {
			std::ostringstream ostr;
			ostr << "Device number " << deviceNumber << " is already open";
			env->ThrowNew(env->FindClass("java/io/IOException"), ostr.str().c_str());
			return -1;
		}

		string error = KNOWBOUT_VIDEO_OpenDevice(deviceNumber, &state->devInfo);
		
		if (error != "") {
			fd = -1;
			env->ThrowNew(env->FindClass("java/io/IOException"), error.c_str());
		}
	} catch (bad_alloc e) {
		fd = -1;
		env->ThrowNew(env->FindClass("java/io/IOException"), "native bad_alloc exception");
	} catch (exception e) {
		fd = -1;
		env->ThrowNew(env->FindClass("java/io/IOException"), "native STL exception");
	} catch (...) {
		fd = -1;
		env->ThrowNew(env->FindClass("java/io/IOException"), "native unknown exception");
	}
		
	return fd;
}

/*
 * Class:     com_knowbout_cc4j_VideoDevice
 * Method:    readFile
 * Signature: (ILjava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_com_knowbout_cc4j_VideoDevice_readFile
  (JNIEnv* env, jobject jobj, jint fd, jobject byteBuffer) {
  	
	env->ThrowNew(env->FindClass("java/io/IOException"), "VideoDevice.read() not implemented on Windows at this time.");
}


/*
 * Class:     com_knowbout_cc4j_VideoDevice
 * Method:    setFrequency
 * Signature: (D)V
 */
JNIEXPORT void JNICALL Java_com_knowbout_cc4j_VideoDevice_setFrequency
  (JNIEnv* env, jobject jobj, jint fd, jdouble newfreq) {

	try {
		env->MonitorEnter(env->GetObjectClass(jobj));
  		State* state = stateMap[fd];
		env->MonitorExit(env->GetObjectClass(jobj));
		
		if (state == NULL || state->closed) {
			env->ThrowNew(env->FindClass("java/io/IOException"), "Attempt to use VideoDevice after it was closed.");
			return;
		}
	  	
  		//TODO: This is a kludge, but Windows Direct Show doesn't support directly setting
  		// frequencies.  I started with Linux which does, and added frequency maps to my
  		// Java maps rather than relying on the native platform for it.  Windows provides
  		// its own frequency maps, so it's hard to say what I really should be doing.  I
  		// know this is incomplete for now (only supporting NTSC_M at the moment).
	  	
		jclass cls = env->GetObjectClass(jobj);
		jmethodID mid = env->GetMethodID(cls, "getFrequencyStandard", "()Lcom/knowbout/cc4j/FrequencyStandard;");
  		jobject freqstd = env->CallObjectMethod(jobj, mid);
	  	
		cls = env->FindClass("com/knowbout/cc4j/ChannelFrequencies");
		mid = env->GetStaticMethodID(cls, "getChannel", "(Lcom/knowbout/cc4j/FrequencyStandard;D)Ljava/lang/String;");
  		jstring channelString = (jstring)env->CallStaticObjectMethod(cls, mid, freqstd, newfreq);
	  	
  		jboolean isCopy;
  		const char* channelChars = env->GetStringUTFChars(channelString, &isCopy);
	  	
  		string stdString(channelChars);
  		std::istringstream inStr(stdString);
  		int channel = 0;
  		inStr >> channel;
	  	
  		env->ReleaseStringUTFChars(channelString, channelChars);
	  	
		string error = KNOWBOUT_VIDEO_SetChannel(state->devInfo, channel);
		
		if (error != "") {
			fd = -1;
			env->ThrowNew(env->FindClass("java/io/IOException"), error.c_str());
		}
	} catch (exception e) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native STL exception");
	} catch (...) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native unknown exception");
	}
}

/*
 * Class:     com_knowbout_cc4j_VideoDevice
 * Method:    getFrequency
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_com_knowbout_cc4j_VideoDevice_getFrequency
  (JNIEnv* env, jobject jobj, jint fd) {

	try {
		env->MonitorEnter(env->GetObjectClass(jobj));
  		State* state = stateMap[fd];
		env->MonitorExit(env->GetObjectClass(jobj));
		
		if (state == NULL || state->closed) {
			env->ThrowNew(env->FindClass("java/io/IOException"), "Attempt to use VideoDevice after it was closed.");
			return -1.0;
		}
		
		int channel = KNOWBOUT_VIDEO_GetChannel(state->devInfo);
		if (channel < 0) {
			env->ThrowNew(env->FindClass("java/io/IOException"), "Couldn't get the channel");
			return -1.0;
		}
		string channelStdString = ""+ channel;
		std::ostringstream ostr;
		ostr << channel;
		jstring channelJString = env->NewStringUTF(ostr.str().c_str());
		
		jclass cls = env->GetObjectClass(jobj);
		jmethodID mid = env->GetMethodID(cls, "getFrequencyStandard", "()Lcom/knowbout/cc4j/FrequencyStandard;");
  		jobject freqstd = env->CallObjectMethod(jobj, mid);
	  	
		cls = env->FindClass("com/knowbout/cc4j/ChannelFrequencies");
		mid = env->GetStaticMethodID(cls, "getFrequency", "(Lcom/knowbout/cc4j/FrequencyStandard;Ljava/lang/String;)D");
  		return env->CallStaticDoubleMethod(cls, mid, freqstd, channelJString);
	} catch (exception e) {
		return -1.0;
		env->ThrowNew(env->FindClass("java/io/IOException"), "native STL exception");
	} catch (...) {
		return -1.0;
		env->ThrowNew(env->FindClass("java/io/IOException"), "native unknown exception");
	}
}

/*
 * Class:     com_knowbout_cc4j_VideoDevice
 * Method:    setFrequency
 * Signature: (D)V
 */
JNIEXPORT void JNICALL Java_com_knowbout_cc4j_VideoDevice_setLinuxVideoStandard
  (JNIEnv* env, jobject jobj, jint fd, jlong linuxVideoStandard) {
	/*
 	try {
	*/
 		//Not implemented
		//TODO: Use this to change from NTSC_M to something else
		if (linuxVideoStandard != 0x1000) {
			std::ostringstream ostr;
			ostr << "The call setVideoStandard for " << linuxVideoStandard << " is not supported on Windows yet.";
			env->ThrowNew(env->FindClass("java/io/IOException"), ostr.str().c_str());
		}
	/*
	} catch (exception e) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native STL exception");
	} catch (...) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native unknown exception");
	}
	*/
}

/*
 * Class:     com_knowbout_cc4j_VideoDevice
 * Method:    getLinuxVideoStandard
 * Signature: ()D
 */
JNIEXPORT jlong JNICALL Java_com_knowbout_cc4j_VideoDevice_getLinuxVideoStandard
  (JNIEnv* env, jobject jobj, jint fd) {
	/*
	try {
	*/
  		//Not implemented, except for returning NTSC_M hardcoded
		//TODO: Use this to support change from NTSC_M to something else
		return 0x1000; // NTSC_M
	/*
	} catch (exception e) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native STL exception");
	} catch (...) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native unknown exception");
	}
	return 0;
	*/
}

/*
 * Class:     com_knowbout_cc4j_VideoDevice
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_knowbout_cc4j_VideoDevice_closeFile
  (JNIEnv* env, jobject jobj, jint fd) {

	try {
		env->MonitorEnter(env->GetObjectClass(jobj));
  		State* state = stateMap[fd];
  		bool wasClosed = state->closed;
  		state->reusable = false;
  		state->closed = true;
		env->MonitorExit(env->GetObjectClass(jobj));
		
		if (state == NULL || wasClosed) {
			env->ThrowNew(env->FindClass("java/io/IOException"), "Attempt to use VideoDevice after it was closed.");
			return;
		}
		
		string error = KNOWBOUT_VIDEO_CloseDevice(state->devInfo);
		if (vm != NULL) {
			vm->DetachCurrentThread();
		}
		state->pushThread = NULL;
		if (error != "") {
			env->ThrowNew(env->FindClass("java/io/IOException"), error.c_str());
		}
		
		// We should completely clean up the Direct Show graph and other heap objects in amcap.cpp,
		// but I'm not doing that right now.  Allow this state object to be reused by the next guy.
		
		//delete state;
		
		env->MonitorEnter(env->GetObjectClass(jobj));
		//stateMap.erase(fd);
  		state->reusable = true; // signals the object is ready to be reused
		env->MonitorExit(env->GetObjectClass(jobj));
	} catch (exception e) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native STL exception");
	} catch (...) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native unknown exception");
	}
}

/*
 * Class:     com_knowbout_cc4j_VideoDevice
 * Method:    readCapability
 * Signature: (ILcom/knowbout/cc4j/VideoDevice/VideoCapability;)V
 */
JNIEXPORT void JNICALL Java_com_knowbout_cc4j_VideoDevice_readCapability
  (JNIEnv* env, jobject jobj, jint fd, jobject videoCapability) {
  	
	try {
		env->MonitorEnter(env->GetObjectClass(jobj));
  		State* state = stateMap[fd];
		env->MonitorExit(env->GetObjectClass(jobj));
		
		jclass cls = env->GetObjectClass(videoCapability);

		jfieldID fieldID = env->GetFieldID(cls, "driver", "Ljava/lang/String;");
		env->SetObjectField(videoCapability, fieldID,
			env->NewStringUTF((const char*)"Microsoft Direct Show"));
		fieldID = env->GetFieldID(cls, "card", "Ljava/lang/String;");
		wchar_t* devname = KNOWBOUT_VIDEO_GetDeviceName(state->devInfo);
		jsize len = (jsize)wcslen(devname);
		env->SetObjectField(videoCapability, fieldID,
			env->NewString((const jchar*)devname, len));
		fieldID = env->GetFieldID(cls, "busInfo", "Ljava/lang/String;");
		env->SetObjectField(videoCapability, fieldID,
			env->NewStringUTF((const char*)"not available"));
		fieldID = env->GetFieldID(cls, "videoCapture", "Z");
		env->SetBooleanField(videoCapability, fieldID, true);
		fieldID = env->GetFieldID(cls, "videoOutput", "Z");
		env->SetBooleanField(videoCapability, fieldID, true);
		fieldID = env->GetFieldID(cls, "videoOverlay", "Z");
		env->SetBooleanField(videoCapability, fieldID, true);
		fieldID = env->GetFieldID(cls, "vbiCapture", "Z");
		env->SetBooleanField(videoCapability, fieldID, true);
		fieldID = env->GetFieldID(cls, "vbiOutput", "Z");
		env->SetBooleanField(videoCapability, fieldID, true);
		fieldID = env->GetFieldID(cls, "rdsCapture", "Z");
		env->SetBooleanField(videoCapability, fieldID, false);
		fieldID = env->GetFieldID(cls, "tuner", "Z");
		env->SetBooleanField(videoCapability, fieldID, true);
		fieldID = env->GetFieldID(cls, "audio", "Z");
		env->SetBooleanField(videoCapability, fieldID, true);
		fieldID = env->GetFieldID(cls, "radio", "Z");
		env->SetBooleanField(videoCapability, fieldID, false);
		fieldID = env->GetFieldID(cls, "readwrite", "Z");
		env->SetBooleanField(videoCapability, fieldID, true);
		fieldID = env->GetFieldID(cls, "asyncio", "Z");
		env->SetBooleanField(videoCapability, fieldID, true);
		fieldID = env->GetFieldID(cls, "streaming", "Z");
		env->SetBooleanField(videoCapability, fieldID, true);

	} catch (exception e) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native STL exception");
	} catch (...) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native unknown exception");
	}
}

/*
 * Class:     com_knowbout_cc4j_VBIDevice
 * Method:    open
 * Signature: (I)V
 */
JNIEXPORT jint JNICALL Java_com_knowbout_cc4j_VBIDevice_open
	(JNIEnv* env, jobject jobj, jint deviceNumber) {

	/*
	try {
	*/
		return deviceNumber;
	/*
	} catch (exception e) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native STL exception");
	} catch (...) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native unknown exception");
	}
	*/
}

/*
 * Class:     com_knowbout_cc4j_VBIDevice
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_knowbout_cc5j_VBIDevice_closeFile
  (JNIEnv* env, jobject jobj, jint fd) {
  	/*
	try {
	} catch (exception e) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native STL exception");
	} catch (...) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native unknown exception");
	}
	*/
}

/*
 * Class:     com_knowbout_cc4j_VBIDevice
 * Method:    readVBI
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_knowbout_cc4j_VBIDevice_readVBI
  (JNIEnv* env, jobject jobj, jint fd, jobject vbiline) {
  	
	try {
		env->MonitorEnter(env->GetObjectClass(jobj));
  		State* state = stateMap[fd];
		env->MonitorExit(env->GetObjectClass(jobj));
		
		if (state == NULL || state->closed) {
			env->ThrowNew(env->FindClass("java/io/IOException"), "Attempt to use VideoDevice after it was closed.");
			return;
		}
		
		jclass cls = env->GetObjectClass(jobj);
		jmethodID mid = env->GetMethodID(cls, "setPush", "(Z)V");
		env->CallVoidMethod(jobj, mid, true);

  		state->vbiDevice = env->NewGlobalRef(jobj); // THIS MUST BE LAST SO pushCCChars BODY IS NOT EXECUTED UNTIL ALL IS INITIALIZED
	  	
		// TODO: We shouldn't start running the thread until now, so add another extern call to amcap
		// Also, we should provide for a way to stop, or at least limit how much we are buffering!!!
	} catch (exception e) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native STL exception");
	} catch (...) {
		env->ThrowNew(env->FindClass("java/io/IOException"), "native unknown exception");
	}
}

extern void KNOWBOUT_VIDEO_pushCCChars(int fd, unsigned char c1, unsigned char c2) {

  	State* state = stateMap[fd]; // We can't guarantee we're connected to the VM yet,
  									// nor do we have a way to get the interface pointer (env)
  									// before calling this map accessor, so we just call it and
  									// hope that at least this method is thread safe. And we
  									// check for a null return!
	
	if (state != NULL && !state->closed && state->vbiDevice != NULL) {
		if (state->pushThread == NULL) {
			jint rtn = vm->AttachCurrentThread((void**)&state->pushThread, NULL);
		}
		JNIEnv* env = state->pushThread;
		jobject jobj = state->vbiDevice;
		jclass cls = env->GetObjectClass(jobj);
		jmethodID mid = env->GetMethodID(cls, "pushBytes", "(II)V");
		env->CallVoidMethod(jobj, mid, vbi_unpar8(c1), vbi_unpar8(c2));

		if (env->ExceptionCheck()) {
			env->ExceptionClear();
			mid = env->GetMethodID(cls, "close", "()V");
			env->CallVoidMethod(jobj, mid);
			env->ExceptionClear();
		}
	}
}
