LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := filter
LOCAL_SRC_FILES := filter.cpp \ onload.cpp \ methodregister.cpp \ JNI_filter.cpp \ JNI_fft.cpp

include $(BUILD_SHARED_LIBRARY)
