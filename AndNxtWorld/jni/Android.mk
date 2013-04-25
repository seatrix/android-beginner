LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := HelloNative
LOCAL_SRC_FILES := hello_native.c hello_next_native.cpp

LOCAL_LDLIBS    := -llog
include $(BUILD_SHARED_LIBRARY)

