#include <iostream>
#include <string>
#include <sstream>
#include <jni.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <linux/videodev.h>
#include <linux/videodev2.h>

#include <com_knowbout_cc4j_VideoDevice.h>

using std::string;
using std::cerr;
using std::endl;

/*
 * Class:     com_knowbout_cc4j_VideoDevice
 * Method:    open
 * Signature: (I)V
 */
JNIEXPORT jint JNICALL Java_com_knowbout_cc4j_VideoDevice_open
	(JNIEnv* env, jobject jobj, jint deviceNumber) {
		
	std::ostringstream ostr;
	ostr << "/dev/video" << deviceNumber;
	string devString = ostr.str();

	int fd = open(devString.c_str(), O_RDWR);

	if (fd == -1) {
		string message = "Could not open " + devString;
		env->ThrowNew(env->FindClass("java/io/IOException"), message.c_str());
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
  	
  	void* buf = env->GetDirectBufferAddress(byteBuffer);
  	int capacity = env->GetDirectBufferCapacity(byteBuffer);
  	
  	while (capacity > 0) {
  		ssize_t n = read(fd, buf, capacity);
  		if (n < 0) {
			std::ostringstream message;
			message << "Could not read VideoDevice: " << strerror(errno);
			env->ThrowNew(env->FindClass("java/io/IOException"), message.str().c_str());
  			return;
  		} else {
  			capacity -= n;
  			buf = ((char*)buf) + n;
  		}
  	}
}

/*
 * Class:     com_knowbout_cc4j_VideoDevice
 * Method:    setFrequency
 * Signature: (D)V
 */
JNIEXPORT void JNICALL Java_com_knowbout_cc4j_VideoDevice_setFrequency
  (JNIEnv* env, jobject jobj, jint fd, jdouble newfreq) {

	struct v4l2_frequency frequency;
	bzero(&frequency, sizeof(frequency));
	frequency.frequency = (__u32)(newfreq * 16.0);
	frequency.type = V4L2_TUNER_ANALOG_TV;
	int ret = ioctl(fd, VIDIOC_S_FREQUENCY, &frequency);

	if (ret < 0) {
		std::ostringstream message;
		message << "Can't set V4L2 capabilities. ioctl errno: " << ret;
		env->ThrowNew(env->FindClass("java/io/IOException"), message.str().c_str());
		return;
	}
}

/*
 * Class:     com_knowbout_cc4j_VideoDevice
 * Method:    getFrequency
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_com_knowbout_cc4j_VideoDevice_getFrequency
  (JNIEnv* env, jobject jobj, jint fd) {

	struct v4l2_frequency frequency;
	bzero(&frequency, sizeof(frequency));
	frequency.type = V4L2_TUNER_ANALOG_TV;
	int ret = ioctl(fd, VIDIOC_G_FREQUENCY, &frequency);

	if (ret < 0) {
		std::ostringstream message;
		message << "Can't query V4L2 capabilities. ioctl errno: " << ret;
		env->ThrowNew(env->FindClass("java/io/IOException"), message.str().c_str());
		return 0.0;
	}

	return frequency.frequency/16.0;
}

/*
 * Class:     com_knowbout_cc4j_VideoDevice
 * Method:    setFrequency
 * Signature: (D)V
 */
JNIEXPORT void JNICALL Java_com_knowbout_cc4j_VideoDevice_setLinuxVideoStandard
  (JNIEnv* env, jobject jobj, jint fd, jlong linuxVideoStandard) {

	v4l2_std_id std;
	std = (v4l2_std_id)linuxVideoStandard;
	int ret = ioctl(fd, VIDIOC_S_STD, &std);

	if (ret < 0) {
		std::ostringstream message;
		message << "Can't set V4L2 capabilities. ioctl errno: " << ret;
		env->ThrowNew(env->FindClass("java/io/IOException"), message.str().c_str());
		return;
	}
}

/*
 * Class:     com_knowbout_cc4j_VideoDevice
 * Method:    getLinuxVideoStandard
 * Signature: ()D
 */
JNIEXPORT jlong JNICALL Java_com_knowbout_cc4j_VideoDevice_getLinuxVideoStandard
  (JNIEnv* env, jobject jobj, jint fd) {

	v4l2_std_id std;
	int ret = ioctl(fd, VIDIOC_G_STD, &std);

	if (ret < 0) {
		std::ostringstream message;
		message << "Can't query V4L2 capabilities. ioctl errno: " << ret;
		env->ThrowNew(env->FindClass("java/io/IOException"), message.str().c_str());
		return 0;
	}

	return (jlong)std;
}

/*
 * Class:     com_knowbout_cc4j_VideoDevice
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_knowbout_cc4j_VideoDevice_closeFile
  (JNIEnv* env, jobject jobj, jint fd) {

	close(fd);
}

/*
 * Class:     com_knowbout_cc4j_VideoDevice
 * Method:    readCapability
 * Signature: (ILcom/knowbout/cc4j/VideoDevice/VideoCapability;)V
 */
JNIEXPORT void JNICALL Java_com_knowbout_cc4j_VideoDevice_readCapability
  (JNIEnv* env, jobject jobj, jint fd, jobject videoCapability) {

	struct v4l2_capability capability;
	bzero(&capability, sizeof(v4l2_capability));
	int ret = ioctl(fd, VIDIOC_QUERYCAP, &capability);

	if (ret < 0) {
		std::ostringstream message;
		message << "Can't query V4L2 capabilities. ioctl errno: " << ret;
		env->ThrowNew(env->FindClass("java/io/IOException"), message.str().c_str());
		return;
	}
	
	jclass clazz = env->FindClass("com/knowbout/cc4j/VideoDevice$VideoCapability");

	jfieldID fieldID = env->GetFieldID(clazz, "driver", "Ljava/lang/String;");
	env->SetObjectField(videoCapability, fieldID,
		env->NewStringUTF((const char*)capability.driver));
	fieldID = env->GetFieldID(clazz, "card", "Ljava/lang/String;");
	env->SetObjectField(videoCapability, fieldID,
		env->NewStringUTF((const char*)capability.card));
	fieldID = env->GetFieldID(clazz, "busInfo", "Ljava/lang/String;");
	env->SetObjectField(videoCapability, fieldID,
		env->NewStringUTF((const char*)capability.bus_info));
	fieldID = env->GetFieldID(clazz, "videoCapture", "Z");
	env->SetBooleanField(videoCapability, fieldID,
		((capability.capabilities &  V4L2_CAP_VIDEO_CAPTURE) != 0));
	fieldID = env->GetFieldID(clazz, "videoOutput", "Z");
	env->SetBooleanField(videoCapability, fieldID,
		((capability.capabilities &  V4L2_CAP_VIDEO_OUTPUT) != 0));
	fieldID = env->GetFieldID(clazz, "videoOverlay", "Z");
	env->SetBooleanField(videoCapability, fieldID,
		((capability.capabilities &  V4L2_CAP_VIDEO_OVERLAY) != 0));
	fieldID = env->GetFieldID(clazz, "vbiCapture", "Z");
	env->SetBooleanField(videoCapability, fieldID,
		((capability.capabilities &  V4L2_CAP_VBI_CAPTURE) != 0));
	fieldID = env->GetFieldID(clazz, "vbiOutput", "Z");
	env->SetBooleanField(videoCapability, fieldID,
		((capability.capabilities &  V4L2_CAP_VBI_OUTPUT) != 0));
	fieldID = env->GetFieldID(clazz, "rdsCapture", "Z");
	env->SetBooleanField(videoCapability, fieldID,
		((capability.capabilities &  V4L2_CAP_RDS_CAPTURE) != 0));
	fieldID = env->GetFieldID(clazz, "tuner", "Z");
	env->SetBooleanField(videoCapability, fieldID,
		((capability.capabilities &  V4L2_CAP_TUNER) != 0));
	fieldID = env->GetFieldID(clazz, "audio", "Z");
	env->SetBooleanField(videoCapability, fieldID,
		((capability.capabilities &  V4L2_CAP_AUDIO) != 0));
	fieldID = env->GetFieldID(clazz, "radio", "Z");
	env->SetBooleanField(videoCapability, fieldID,
		((capability.capabilities &  V4L2_CAP_RADIO) != 0));
	fieldID = env->GetFieldID(clazz, "readwrite", "Z");
	env->SetBooleanField(videoCapability, fieldID,
		((capability.capabilities &  V4L2_CAP_READWRITE) != 0));
	fieldID = env->GetFieldID(clazz, "asyncio", "Z");
	env->SetBooleanField(videoCapability, fieldID,
		((capability.capabilities &  V4L2_CAP_ASYNCIO) != 0));
	fieldID = env->GetFieldID(clazz, "streaming", "Z");
	env->SetBooleanField(videoCapability, fieldID,
		((capability.capabilities &  V4L2_CAP_STREAMING) != 0));
}
