#ifndef _PACKT_LOG_HPP_
#define _PACKT_LOG_HPP_

#include <jni.h>
#include <android/log.h>

namespace imgproc {

#define LOG_TAG "maincpp"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
}

#endif
