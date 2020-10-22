# Copyright 2006 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE:= libhtpa3232
LOCAL_SRC_FILES:= htpa3232.c htpa3232_API.c
LOCAL_LDLIBS += -llog
LOCAL_DEX_PREOPT := false

LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)
