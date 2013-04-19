LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := HelloNative
LOCAL_SRC_FILES := hello_native.c
include $(BUILD_SHARED_LIBRARY)
