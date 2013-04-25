#include <stdio.h>
#include <stdlib.h>

#include <jni.h>
#include <android/log.h>
#include "com_exam_slieer_utils_jni_HelloNextNative.h"

#define  LOG_TAG    "HelloNextNative"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

static int i = 1;

JNIEXPORT jint JNICALL Java_com_exam_slieer_utils_jni_HelloNextNative_getInt
    (JNIEnv *env, jobject thiz){
    return i;
}

JNIEXPORT void JNICALL Java_com_exam_slieer_utils_jni_HelloNextNative_setInt
    (JNIEnv *env, jobject thiz, jint ji){
    LOGI("i value is %d\n", i);
    i = ji + 1;
}
