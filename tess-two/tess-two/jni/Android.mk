LOCAL_PATH := $(call my-dir)
TESSERACT_PATH := $(LOCAL_PATH)/com_googlecode_tesseract_android/src
LEPTONICA_PATH := $(LOCAL_PATH)/com_googlecode_leptonica_android/src
LIBPNG_PATH := $(LOCAL_PATH)/../../../libpng-android/jni





# Just build the Android.mk files in the subdirs
include $(call all-subdir-makefiles)
include $(LIBPNG_PATH)/Android.mk

#include $(LOCAL_PATH)/com_googlecode_tesseract_android/Android.mk
#include $(LOCAL_PATH)/com_googlecode_leptonica_android/Android.mk
