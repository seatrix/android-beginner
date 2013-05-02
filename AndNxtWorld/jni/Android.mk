LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := HelloNative

LOCAL_SRC_FILES := hello_native.c	\
		hello_next_native.cpp	\
		hello_java_bean_native.cpp	\
		corejava/corejava8_jni_printf2.c	\
		corejava/corejava8_jni_printf4.c	\

LOCAL_LDLIBS    := -llog
include $(BUILD_SHARED_LIBRARY)

