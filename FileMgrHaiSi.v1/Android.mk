
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)
# LOCAL_LDFLAGS := samba

LOCAL_JAVA_LIBRARIES :=

LOCAL_JNI_SHARED_LIBRARIES := libandroid_runtime

LOCAL_PACKAGE_NAME := FileM

LOCAL_CERTIFICATE := platform

#LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

