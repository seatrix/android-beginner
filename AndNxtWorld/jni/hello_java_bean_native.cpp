#include <stdio.h>
#include <stdlib.h>

#include <jni.h>
#include <android/log.h>
#include "com_exam_slieer_utils_jni_HelloJavaBeanNative.h"

#define  LOG_TAG    "HelloJavaBeanNative"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

jobject user;

JNIEXPORT void JNICALL Java_com_exam_slieer_utils_jni_HelloJavaBeanNative_setUser
(JNIEnv *env, jobject thiz, jstring name){
    jclass userClass = env->FindClass("com/exam/slieer/utils/bean/User");

    jmethodID userMethod = env->GetMethodID(userClass,"<init>","()V");

    jfieldID mId = env->GetFieldID(userClass,"id","J");
    jfieldID mUserName = env->GetFieldID(userClass,"userName","Ljava/lang/String;");
    jfieldID mIsMan = env->GetFieldID(userClass,"isMan","Z");
    jfieldID mAge = env->GetFieldID(userClass,"age","I");

    jobject userObject = env->NewObject(userClass,userMethod);
    env->SetObjectField(userObject,mUserName,name);
    env->SetLongField(userObject,mId,1001);
    env->SetBooleanField(userObject,mIsMan,1);
    env->SetIntField(userObject,mAge,21);
    LOGI("set ojbect properties is OK 4...");
    user = userObject;
}

JNIEXPORT jobject JNICALL Java_com_exam_slieer_utils_jni_HelloJavaBeanNative_getUser
  (JNIEnv *env, jobject thiz){
	LOGI("get user info");
	return user;
}
