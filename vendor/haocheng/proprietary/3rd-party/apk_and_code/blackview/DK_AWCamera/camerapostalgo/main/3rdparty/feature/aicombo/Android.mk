# Copyright Statement:
#
# This software/firmware and related documentation ("MediaTek Software") are
# protected under relevant copyright laws. The information contained herein
# is confidential and proprietary to MediaTek Inc. and/or its licensors.
# Without the prior written permission of MediaTek inc. and/or its licensors,
# any reproduction, modification, use or disclosure of MediaTek Software,
# and information contained herein, in whole or in part, shall be strictly prohibited.

# MediaTek Inc. (C) 2018. All rights reserved.
#
# BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
# THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
# RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
# AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
# NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
# SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
# SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
# THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
# THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
# CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
# SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
# STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
# CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
# AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
# OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
# MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
#
# The following software/firmware and/or related documentation ("MediaTek Software")
# have been modified by MediaTek Inc. All revisions are subject to any receiver's
# applicable license agreements with MediaTek Inc.

####################################################################################################
#
####################################################################################################

LOCAL_PATH := $(call my-dir)

HAS_CAMERA_ALGO_LIBRARY := $(shell test -d vendor/mediatek/proprietary/external/camera_algo && \
echo yes)
$(info "HAS_CAMERA_ALGO_LIBRARY = $(HAS_CAMERA_ALGO_LIBRARY))
ifeq (yes, $(HAS_CAMERA_ALGO_LIBRARY))
ifeq (yes, $(MTK_AI_CAMERA_SUPPORT))

####################################################################################################
#
####################################################################################################

include $(CLEAR_VARS)
LOCAL_MODULE := libaicamera_headers
LOCAL_EXPORT_C_INCLUDE_DIRS := $(TOP)/$(MTK_PATH_SOURCE)/hardware/power/include
LOCAL_EXPORT_C_INCLUDE_DIRS += $(TOP)/frameworks/hardware/interfaces/sensorservice/libsensorndkbridge
LOCAL_EXPORT_C_INCLUDE_DIRS += $(TOP)/hardware/interfaces/sensors/1.0/default/include
include $(BUILD_HEADER_LIBRARY)

include $(CLEAR_VARS)

#-----------------------------------------------------------
LOCAL_HEADER_LIBRARIES += postalgo_featurepipe_headers
LOCAL_HEADER_LIBRARIES += postalgo_main_headers
LOCAL_HEADER_LIBRARIES += camerapostalgo_headers
LOCAL_HEADER_LIBRARIES += 3rdparty_plugin_headers
LOCAL_HEADER_LIBRARIES += feature_utils_headers
LOCAL_HEADER_LIBRARIES += libandroid_sensor_headers
LOCAL_HEADER_LIBRARIES += libaicamera_headers
#-----------------------------------------------------------
LOCAL_SRC_FILES += $(call all-cpp-files-under, )
LOCAL_SRC_FILES += $(call all-cpp-files-under, ../utils)
#---------------------------------------------------------------------------------------------------
LOCAL_CFLAGS += -Wall -Werror -Wno-unused-parameter  -Wno-macro-redefined \
                      -DGL_GLEXT_PROTOTYPES -DEGL_EGLEXT_PROTOTYPES \
                      -DFT_AI_CAMERA_FEATURE
LOCAL_CPPFLAGS += -fexceptions -std=c++1y
#---------------------------------------------------------------------------------------------------
LOCAL_SHARED_LIBRARIES += liblog
LOCAL_SHARED_LIBRARIES += libutils
LOCAL_SHARED_LIBRARIES += libcutils
LOCAL_SHARED_LIBRARIES += libhidlbase
LOCAL_SHARED_LIBRARIES += libandroid
LOCAL_SHARED_LIBRARIES += vendor.mediatek.hardware.mms@1.0
LOCAL_SHARED_LIBRARIES += vendor.mediatek.hardware.mms@1.1
LOCAL_SHARED_LIBRARIES += vendor.mediatek.hardware.mms@1.2
LOCAL_SHARED_LIBRARIES += libpostalgo_stdutils
LOCAL_SHARED_LIBRARIES += libpostalgo_imgbuf
LOCAL_SHARED_LIBRARIES += libEGL
LOCAL_SHARED_LIBRARIES += libGLESv2
LOCAL_SHARED_LIBRARIES += libnativewindow
LOCAL_SHARED_LIBRARIES += libpostalgo_3rdparty.core
LOCAL_SHARED_LIBRARIES += libpostalgo_metadata
#LOCAL_SHARED_LIBRARIES += vendor.mediatek.hardware.power@2.0
LOCAL_SHARED_LIBRARIES += android.frameworks.sensorservice@1.0
LOCAL_SHARED_LIBRARIES += android.hardware.sensors@1.0
LOCAL_SHARED_LIBRARIES += libbase
LOCAL_SHARED_LIBRARIES += libhardware
LOCAL_STATIC_LIBRARIES += android.hardware.sensors@1.0-convert
#---------------------------------------------------------------------------------------------------
LOCAL_MODULE := libpostalgo.plugin.aicombo
LOCAL_PROPRIETARY_MODULE := false
LOCAL_MODULE_TAGS := optional
include $(BUILD_SHARED_LIBRARY)

####################################################################################################
#
####################################################################################################
endif
endif
