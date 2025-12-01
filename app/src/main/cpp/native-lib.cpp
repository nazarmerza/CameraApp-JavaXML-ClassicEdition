/*

 * Main.cpp
 *
 *  Created on: Feb 26, 2014
 *      Author: nazar
 */

#include <string.h>
#include <jni.h>
#include <android/bitmap.h>

#include "Converter.hpp"
#include "JniUtils.hpp"
#include "Log.hpp"


using namespace JniUtils;

static const bool VERBOSE = false;
static const int PLANAR = 1;
static const int SEMI_PLANAR = 2;

extern "C" {


JNIEXPORT
void JNICALL Java_com_nm_camerafx_VideoCameraActivity_setMap(JNIEnv * pEnv,
                                                             jclass pClass, jint filterType) {

    Converter::setMapData2(filterType);

    //if (VERBOSE )LOGD("Filter map set.");


}

JNIEXPORT
void JNICALL Java_com_nm_camerafx_camera_CameraView_decodeyuv420(
        JNIEnv * pEnv,
        jclass pClass, jobject rgbBitmap, jbyteArray yuv, jint format) {

    AndroidBitmapInfo bitmapInfo;
    if (AndroidBitmap_getInfo(pEnv, rgbBitmap, &bitmapInfo) < 0) {
        return;
    }
    if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return;
    }

    // create array, associate it with the bitmap object, and lock it.
    uint32_t* bitmap;
    if (AndroidBitmap_lockPixels(pEnv, rgbBitmap, (void**) &bitmap) < 0) {
        return;
    }

    // cast jbyteArray to char pointer
    //char *test = yuv;

    // lock array for access
    //jbyte* lSource = (jbyte *)pEnv->GetPrimitiveArrayCritical(yuv, 0);
    char* lSource = (char *)pEnv->GetPrimitiveArrayCritical(yuv, 0);
    if (lSource == NULL) {
        return;
    }

    int height = bitmapInfo.height;
    int width = bitmapInfo.width;

    if (format == PLANAR) {
        Converter::nv21_to_yuv420Planar(bitmap, lSource, width, height);
    } else if (format == SEMI_PLANAR) {
        Converter::nv21_to_yuv420semiplanar(bitmap, lSource, width, height);
        //Converter::nv21_to_yuv420semiplanar_color(bitmap, lSource, width, height);
    }

    // release locked resources
    pEnv->ReleasePrimitiveArrayCritical(yuv, lSource, 0);

    AndroidBitmap_unlockPixels(pEnv, rgbBitmap);

}

} // extern C



