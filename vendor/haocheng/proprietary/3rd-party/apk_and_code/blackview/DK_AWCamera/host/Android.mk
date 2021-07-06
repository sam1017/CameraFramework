LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := com.mediatek.camera.portability
LOCAL_STATIC_JAVA_LIBRARIES += guava
LOCAL_STATIC_JAVA_LIBRARIES += appluginmanager
LOCAL_STATIC_JAVA_LIBRARIES += vendor.mediatek.hardware.camera.bgservice-V1.0-java
LOCAL_STATIC_JAVA_LIBRARIES += libcampostalgo_api
LOCAL_STATIC_JAVA_LIBRARIES += androidx.appcompat_appcompat
LOCAL_STATIC_JAVA_LIBRARIES += androidx.recyclerview_recyclerview
LOCAL_STATIC_JAVA_LIBRARIES += androidx.annotation_annotation

LOCAL_RENDERSCRIPT_TARGET_API := 18
LOCAL_RENDERSCRIPT_COMPATIBILITY := 18

LOCAL_JNI_SHARED_LIBRARIES := libnn_sample libimage_detect

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res $(LOCAL_PATH)/../feature/setting/cameraswitcher/res
LOCAL_RESOURCE_DIR += prebuilts/sdk/current/androidx/m2repository/androidx/appcompat/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/demofb/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/demoeis/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/demoasd/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/continuousshot/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/hdr/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/flash/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/focus/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/exposure/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/zoom/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/dng/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/dualcamerazoom/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/selftimer/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/facedetection/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/picturesize/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/previewmode/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/microphone/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/hdr10/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/videoquality/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/videoformat/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/noisereduction/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/fps60/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/eis/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/ais/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/scenemode/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/whitebalance/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/antiflicker/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/zsd/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/iso/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/aaaroidebug/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/panorama/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/shutterspeed/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/longexposure/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/hdr/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/slowmotion/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/vsdof/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/formats/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/slowmotionquality/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/dof/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/matrixdisplay/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/facebeauty/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/vfacebeauty/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/visualsearch/res

#add by huangfei for camerasound start
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/camerasound/res
#add by huangfei for camerasound end

#add by huangfei for mono mode start
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/mono/res
#add by huangfei for mono mode end

#add by huangfei for promode start
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/profession/res
#add by huangfei for promode end

# @{ hct.huangfei, 20201021. add hctfacebeauty mode.
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/hctfacebeauty/res
# @}

# @{ hct.huangfei, 20201021. add hctbokeh mode.
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/hctbokeh/res
# @}

# @{ hct.huangfei, 20201024. add location.
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/location/res
# @}

# @{ hct.huangfei, 20201026. add storagepath.
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/storagepath/res
# @}

# @{ hct.huangfei, 20201026. add camera mirror.
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/mirror/res
# @}

# @{ hct.huangfei, 20201028 add gridlines.
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/gridlines/res
# @}

# @{ hct.huangfei, 20201028. add water mark.
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/watermark/res
# @}

# @{ hct.huangfei, 20201210.add volume key function.
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/volumekey/res
# @}

# hct.wangsenhao, for camera switch @{
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/tripleswitch/res
# }@ hct.wangsenhao

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/visualsearch/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/aicombo/res
#bv wuyonglin add for Superphoto should fixed picture size 20201023 start
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/superphoto/res
#bv wuyonglin add for Superphoto should fixed picture size 20201023 end
#bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/aiworksfacebeauty/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/aiworksbokeh/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/aiworksbokehcolor/res
#bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
#add by liangchangwei for night mode start
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/night/res
#add by liangchangwei for night mode end


LOCAL_SRC_FILES := $(call all-java-files-under, src) $(call all-renderscript-files-under, src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/demofb/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/demoeis/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/demoasd/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/cameraswitcher/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/continuousshot/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/hdr/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/visualsearch/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/flash/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/focus/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/exposure/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/zoom/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/facedetection/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/dng/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/dualcamerazoom/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/selftimer/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/picturesize/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/previewmode/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/microphone/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/hdr10/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/videoquality/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/videoformat/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/noisereduction/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/fps60/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/eis/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/ais/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/scenemode/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/whitebalance/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/antiflicker/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/zsd/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/iso/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/aaaroidebug/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/panorama/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/shutterspeed/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/longexposure/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/hdr/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/slowmotion/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/vsdof/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/formats/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/slowmotionquality/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/postview/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/dof/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/matrixdisplay/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/facebeauty/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/vfacebeauty/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/visualsearch/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/aicombo/src)

LOCAL_SRC_FILES += $(call all-java-files-under, ../common/src)
LOCAL_SRC_FILES += $(call all-Iaidl-files-under, ../feature/mode/visualsearch/aidl)
LOCAL_SRC_FILES += ../feature/mode/visualsearch/aidl/com/visualsearch/DataInterface.aidl

#add by huangfei for camerasound start
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/camerasound/src)
#add by huangfei for camerasound end

#add by huangfei for mono mode start
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/mono/src)
#add by huangfei for mono mode end

#add by huangfei for promode start
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/profession/src)
#add by huangfei for promode end

# @{ hct.huangfei, 20201021. add hctfacebeauty mode.
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/hctfacebeauty/src)
# @}

# @{ hct.huangfei, 20201021. add hctbokeh mode.
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/hctbokeh/src)
# @}

# @{ hct.huangfei, 20201024. add location.
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/location/src)
# @}

# @{ hct.huangfei, 20201026. add storagepath.
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/storagepath/src)
# @}

# @{ hct.huangfei, 20201026. add camera mirror.
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/mirror/src)
# @}

# @{ hct.huangfei, 20201028 add gridlines.
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/gridlines/src)
# @}

# @{ hct.huangfei, 20201028. add water mark.
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/watermark/src)
# @}

# @{ hct.huangfei, 20201210.add volume key function.
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/volumekey/src)
# @}

# hct.wangsenhao, for camera switch @{
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/tripleswitch/src)
# }@ hct.wangsenhao
#bv wuyonglin add for Superphoto should fixed picture size 20201023 start
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/superphoto/src)
#bv wuyonglin add for Superphoto should fixed picture size 20201023 end
#bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/aiworksfacebeauty/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/aiworksbokeh/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/aiworksbokehcolor/src)
#bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
#add by liangchangwei for night mode start
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/night/src)
#add by liangchangwei for night mode end

LOCAL_AIDL_INCLUDES := $(LOCAL_PATH)/../feature/mode/visualsearch/aidl

#LOCAL_ASSET_FILES += $(call find-subdir-assets)
#LOCAL_ASSET_DIR += $(LOCAL_PATH)/../feature/mode/visualsearch/assets
#add by liangchangwei for night mode and Hdr start
LOCAL_STATIC_JAVA_AAR_LIBRARIES:= aw_ns_exp_jni \
				aw_yuvencode \
				aiworks_hdr \
				awfacebeauty-release \
				awfacedetect_plus-release \
				camera2 \
				aiworks_portrait \
				aiworks_livehdr \
				aiworks_deblur \
				aiworks_srmf \
				aiworks_judgeShake
#add by liangchangwei for night mode and Hdr end
#LOCAL_AAPT_FLAGS := -0 .tflite
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --no-version-vectors
LOCAL_AAPT_FLAGS += --extra-packages androidx.appcompat
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.appcompat
#add by liangchangwei for night mode and Hdr start
LOCAL_SHARED_LIBRARIES := libjni_yuv_encode \
			libaw_ns_mali \
			libjpeg_awturbo \
			libaw_hdr \
			libaw_hdr_mali \
			libscene_detect \
			libaw_lowlightutils

LOCAL_LDFLAGS += $(LOCAL_PATH)/LOCAL_SRC_FILES_arm
LOCAL_LDFLAGS += $(LOCAL_PATH)/LOCAL_SRC_FILES_arm64
#add by liangchangwei for night mode and Hdr end

LOCAL_MIN_SDK_VERSION := 23

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_PACKAGE_NAME := DKAWCamera
LOCAL_DEX_PREOPT := false
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_SYSTEM_EXT_MODULE := true
#overrides aosp camera
LOCAL_OVERRIDES_PACKAGES := DKCamera Camera2 Camera
include $(BUILD_PACKAGE)
#add by liangchangwei for night mode and Hdr start
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := aw_ns_exp_jni:libs/aw_ns_exp_jni.aar \
				aw_yuvencode:libs/aw_yuvencode.aar \
				aiworks_hdr:libs/aiworks_hdr.aar \
				awfacebeauty-release:libs/awfacebeauty-release.aar \
				awfacedetect_plus-release:libs/awfacedetect_plus-release.aar \
				camera2:libs/camera2.aar \
				aiworks_portrait:libs/aiworks_portrait.aar \
				aiworks_livehdr:libs/aiworks_livehdr.aar \
				aiworks_deblur:libs/aiworks_deblur.aar \
				aiworks_srmf:libs/aiworks_srmf.aar \
				aiworks_judgeShake:libs/aiworks_judgeShake.aar

LOCAL_AAPT_FLAGS += --extra-packages com.aiworks.yuvUtil
LOCAL_AAPT_FLAGS += --extra-packages com.aiworks.android.lowlight
LOCAL_AAPT_FLAGS += --extra-packages com.aiworks.android.hdrnet.jni

include $(BUILD_MULTI_PREBUILT)
#add by liangchangwei for night mode and Hdr end
