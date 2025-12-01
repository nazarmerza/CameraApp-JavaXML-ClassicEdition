/*
 * JniUtil.hpp
 *
 *  Created on: Mar 2, 2014
 *      Author: nazar
 */

#ifndef JNIUTIL_HPP_
#define JNIUTIL_HPP_

#include <jni.h>
#include <android/bitmap.h>

namespace JniUtils {

inline static void* lockBitmap(JNIEnv * env, jobject bitmap, AndroidBitmapInfo* info) {

	int ret;

		void* pixels = 0;

		ret = AndroidBitmap_getInfo(env, bitmap, info);

		if (ret = AndroidBitmap_getInfo(env, bitmap, info) < 0) {
			//LOGD("Could not get bitmap info");
			return NULL;
		}
		// make sure the data is in RGBA format
		if ((*info).format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
			//LOGD("Format is not RGBA");
			return NULL;
		}

		// get pointer to map array
		if (AndroidBitmap_lockPixels(env, bitmap, (void**)&pixels) < 0) {
			return NULL;
		}

		return pixels;
}

inline	static void unlockBitmap(JNIEnv * pEnv, jobject bitmap){
		AndroidBitmap_unlockPixels(pEnv, bitmap);
	}

}


#endif /* JNIUTIL_HPP_ */
