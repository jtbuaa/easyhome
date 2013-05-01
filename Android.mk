#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# this file only for make under full source tree of Android source code.
# it will not generate armv7 lib
# make under source tree will do extra static check for source and resource file.
# you can copy this folder to source tree, mm in the folder, or mmm <your folder>,
# or make <you package>.
# mm/mmm is fast than make, but will not generate needed libs, such as libc.so
# refer to http://news.wangmeng.cn/detailNews/2621 for add jni in mk file
# and http://blogold.chinaunix.net/u3/108695/showart_2286103.html

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := EasyHome

LOCAL_STATIC_JAVA_LIBRARIES := admob

LOCAL_JNI_SHARED_LIBRARIES := libifprint

include $(BUILD_PACKAGE)

##################################################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := admob:libs/admob-sdk-android.jar
include $(BUILD_MULTI_PREBUILT)

##################################################
include $(CLEAR_VARS)

include $(call all-makefiles-under,$(LOCAL_PATH))
