/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.camera.common.loader;

import android.content.Context;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.debug.profiler.IPerformanceProfile;
import com.mediatek.camera.common.debug.profiler.PerformanceTracker;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.mode.photo.PhotoModeEntry;
import com.mediatek.camera.common.mode.photo.intent.IntentPhotoModeEntry;
import com.mediatek.camera.common.mode.video.VideoModeEntry;
import com.mediatek.camera.common.mode.video.intentvideo.IntentVideoModeEntry;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.feature.mode.dof.DofModeEntry;
import com.mediatek.camera.feature.mode.facebeauty.FaceBeautyModeEntry;
import com.mediatek.camera.feature.mode.hdr.HdrModeEntry;
import com.mediatek.camera.feature.mode.longexposure.LongExposureModeEntry;
import com.mediatek.camera.feature.mode.matrix.MatrixModeEntry;
import com.mediatek.camera.feature.mode.panorama.PanoramaModeEntry;
import com.mediatek.camera.feature.mode.slowmotion.SlowMotionEntry;
import com.mediatek.camera.feature.mode.vfacebeauty.VendorFaceBeautyModeEntry;
import com.mediatek.camera.feature.mode.vsdof.photo.SdofPhotoEntry;
import com.mediatek.camera.feature.mode.vsdof.video.SdofVideoModeEntry;
import com.mediatek.camera.feature.mode.aicombo.photo.AIBeautyPhotoEntry;
import com.mediatek.camera.feature.mode.aicombo.photo.AIBokehPhotoEntry;
import com.mediatek.camera.feature.mode.aicombo.photo.AIColorPhotoEntry;
import com.mediatek.camera.feature.mode.aicombo.photo.AILeggyPhotoEntry;
import com.mediatek.camera.feature.mode.aicombo.photo.AISlimmingPhotoEntry;
import com.mediatek.camera.feature.mode.aicombo.video.AIBeautyVideoModeEntry;
import com.mediatek.camera.feature.mode.aicombo.video.AIBokehVideoModeEntry;
import com.mediatek.camera.feature.mode.aicombo.video.AIColorVideoModeEntry;
import com.mediatek.camera.feature.mode.aicombo.video.AILeggyVideoModeEntry;
import com.mediatek.camera.feature.mode.aicombo.video.AISlimmingVideoModeEntry;
import com.mediatek.camera.feature.setting.CameraSwitcherEntry;
import com.mediatek.camera.feature.setting.ContinuousShotEntry;
import com.mediatek.camera.feature.setting.aaaroidebug.AaaRoiDebugEntry;
import com.mediatek.camera.feature.setting.ais.AISEntry;
import com.mediatek.camera.feature.setting.antiflicker.AntiFlickerEntry;
import com.mediatek.camera.feature.setting.demofb.DemoFbEntry;
import com.mediatek.camera.feature.setting.dng.DngEntry;
import com.mediatek.camera.feature.setting.dualcamerazoom.DualZoomEntry;
import com.mediatek.camera.feature.setting.fps60.Fps60Entry;
import com.mediatek.camera.feature.setting.eis.EISEntry;
import com.mediatek.camera.feature.setting.exposure.ExposureEntry;
import com.mediatek.camera.feature.setting.facedetection.FaceDetectionEntry;
import com.mediatek.camera.feature.setting.flash.FlashEntry;
import com.mediatek.camera.feature.setting.focus.FocusEntry;
import com.mediatek.camera.feature.setting.format.FormatEntry;
import com.mediatek.camera.feature.setting.hdr.HdrEntry;
import com.mediatek.camera.feature.setting.iso.ISOEntry;
import com.mediatek.camera.feature.setting.microphone.MicroPhoneEntry;
import com.mediatek.camera.feature.setting.hdr10.Hdr10Entry;
import com.mediatek.camera.feature.setting.noisereduction.NoiseReductionEntry;
import com.mediatek.camera.feature.setting.picturesize.PictureSizeEntry;
import com.mediatek.camera.feature.setting.postview.PostViewEntry;
import com.mediatek.camera.feature.setting.previewmode.PreviewModeEntry;
import com.mediatek.camera.feature.setting.scenemode.SceneModeEntry;
import com.mediatek.camera.feature.setting.selftimer.SelfTimerEntry;
import com.mediatek.camera.feature.setting.shutterspeed.ShutterSpeedEntry;
import com.mediatek.camera.feature.setting.slowmotionquality.SlowMotionQualityEntry;
import com.mediatek.camera.feature.setting.demoasd.DemoAsdEntry;
import com.mediatek.camera.feature.setting.demoeis.DemoEisEntry;
import com.mediatek.camera.feature.setting.videoquality.VideoQualityEntry;
import com.mediatek.camera.feature.setting.videoformat.VideoFormatEntry;
import com.mediatek.camera.feature.setting.whitebalance.WhiteBalanceEntry;
import com.mediatek.camera.feature.setting.zoom.ZoomEntry;
import com.mediatek.camera.feature.setting.zsd.ZSDEntry;
import com.mediatek.camera.feature.setting.visualsearch.VisualSearchSettingEntry;
import com.mediatek.camera.feature.mode.visualsearch.VisualSearchModeEntry;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

//add by huangfei for camerasound start
import com.mediatek.camera.feature.setting.camerasound.CameraSoundEntry;
//add by huangfei for camerasound end

//add by huangfei for promode start
import com.mediatek.camera.feature.mode.pro.ProEntry;
//add by huangfei for promode end

//add by huangfei for monomode start
import com.mediatek.camera.feature.mode.mono.MonoEntry;
//add by huangfei for monomode end

//*/ hct.huangfei, 20201021. add hctfacebeauty mode.
import com.mediatek.camera.feature.mode.hctfacebeauty.HctFaceBeautyEntry;
//*/

//*/ hct.huangfei, 20201021. add hctbokeh mode.
import com.mediatek.camera.feature.mode.hctbokeh.HctBokehEntry;
//*/

//*/ hct.huangfei, 20201024. add location.
import com.mediatek.camera.feature.setting.location.LocationEntry;
//*/

//*/ hct.huangfei, 20201026. add storagepath.
import com.mediatek.camera.feature.setting.storagepath.StoragePathEntry;
//*/

//*/ hct.huangfei, 20201027. add camera mirror.
import com.mediatek.camera.feature.setting.mirror.MirrorEntry;
//*/

//*/ hct.huangfei, 20201028. add gridlines.
import com.mediatek.camera.feature.setting.gridlines.GridlinesEntry;
//*/

//*/ hct.huangfei, 20201028. add water mark.
import com.mediatek.camera.feature.setting.watermark.WaterMarkEntry;
//*/

//*/ hct.huangfei, 20201210.add volume key function.
import com.mediatek.camera.feature.setting.volumekey.VolumeKeyEntry;
//*/

/* hct.wangsenhao, for camera switch @{ */
import com.mediatek.camera.feature.setting.TripleswitchEntry;
/* }@ hct.wangsenhao */
//add by liangchangwei for nightmode start
import com.mediatek.camera.feature.mode.night.NightEntry;
//add by liangchangwei for nightmode end
//bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
import com.mediatek.camera.feature.mode.aiworksfacebeauty.AiworksFaceBeautyEntry;
import com.mediatek.camera.feature.mode.aiworksbokeh.AiWorksBokehEntry;
import com.mediatek.camera.feature.mode.aiworksbokehcolor.AiWorksBokehColorEntry;
//bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
//bv wuyonglin add for Superphoto should fixed picture size 20201023 start
import com.mediatek.camera.feature.mode.superphoto.SuperphotoEntry;
//bv wuyonglin add for Superphoto should fixed picture size 20201023 end

/**
 * Used for load the features.
 */
public class FeatureLoader {
    private static final Tag TAG = new Tag(FeatureLoader.class.getSimpleName());

    private static final String VisualSearchSetting = "com.mediatek.camera.feature.setting.visualsearch.VisualSearchSettingEntry";
    private static final String VisualSearch = "com.mediatek.camera.feature.mode.visualsearch.VisualSearchModeEntry";

    private static final String CAMERA_SWITCH = "com.mediatek.camera.feature.setting.CameraSwitcherEntry";
    private static final String CONTINUOUSSHOT = "com.mediatek.camera.feature.setting.ContinuousShotEntry";
    private static final String DNG = "com.mediatek.camera.feature.setting.dng.DngEntry";
    private static final String DUAL_ZOOM =
            "com.mediatek.camera.feature.setting.dualcamerazoom.DualZoomEntry";
    private static final String SELFTIME = "com.mediatek.camera.feature.setting.selftimer.SelfTimerEntry";
    private static final String FACE_DETECTION = "com.mediatek.camera.feature.setting.facedetection.FaceDetectionEntry";
    private static final String FLASH = "com.mediatek.camera.feature.setting.flash.FlashEntry";
    private static final String HDR = "com.mediatek.camera.feature.setting.hdr.HdrEntry";
    private static final String PICTURE_SIZE = "com.mediatek.camera.feature.setting.picturesize.PictureSizeEntry";
    private static final String PREVIEW_MODE = "com.mediatek.camera.feature.setting.previewmode.PreviewModeEntry";
    private static final String VIDEO_QUALITY = "com.mediatek.camera.feature.setting.videoquality.VideoQualityEntry";
    private static final String VIDEO_FORMAT = "com.mediatek.camera.feature.setting.videoformat.VideoFormatEntry";
    private static final String ZOOM = "com.mediatek.camera.feature.setting.zoom.ZoomEntry";
    private static final String FOCUS = "com.mediatek.camera.feature.setting.focus.FocusEntry";
    private static final String EXPOSURE = "com.mediatek.camera.feature.setting.exposure.ExposureEntry";
    private static final String MICHROPHONE = "com.mediatek.camera.feature.setting.microphone.MicroPhoneEntry";
    private static final String HDR10 = "com.mediatek.camera.feature.setting.hdr10.Hdr10Entry";
    private static final String NOISE_REDUCTION = "com.mediatek.camera.feature.setting.noisereduction.NoiseReductionEntry";
    private static final String EIS = "com.mediatek.camera.feature.setting.eis.EISEntry";
    private static final String FPS60 = "com.mediatek.camera.feature.setting.fps60.Fps60Entry";
    private static final String AIS = "com.mediatek.camera.feature.setting.ais.AISEntry";
    private static final String SCENE_MODE = "com.mediatek.camera.feature.setting.scenemode.SceneModeEntry";
    private static final String WHITE_BALANCE = "com.mediatek.camera.feature.setting.whitebalance.WhiteBalanceEntry";
    private static final String ANTI_FLICKER = "com.mediatek.camera.feature.setting.antiflicker.AntiFlickerEntry";
    private static final String ZSD = "com.mediatek.camera.feature.setting.zsd.ZSDEntry";
    private static final String ISO = "com.mediatek.camera.feature.setting.iso.ISOEntry";
    private static final String AE_AF_DEBUG = "com.mediatek.camera.feature.setting.aaaroidebug.AaaRoiDebugEntry";
    private static final String SDOF_PHOTO_MODE = "com.mediatek.camera.feature.mode.vsdof.photo.SdofPhotoEntry";
    private static final String SDOF_VIDEO_MODE = "com.mediatek.camera.feature.mode.vsdof.video.SdofVideoModeEntry";
    private static final String SHUTTER_SPEED = "com.mediatek.camera.feature.setting.shutterspeed.ShutterSpeedEntry";
    private static final String LONG_EXPUSURE_MODE = "com.mediatek.camera.feature.mode.longexposure.LongExposureModeEntry";
    private static final String HDR_MODE = "com.mediatek.camera.feature.mode.hdr.HdrModeEntry";
    private static final String PANORAMA_MODE
            = "com.mediatek.camera.feature.mode.panorama.PanoramaModeEntry";
    private static final String PHOTO_MODE = "com.mediatek.camera.common.mode.photo.PhotoModeEntry";
    private static final String VIDEO_MODE = "com.mediatek.camera.common.mode.video.VideoModeEntry";
    private static final String INTENT_PHOTO_MODE
            = "com.mediatek.camera.common.mode.photo.intent.IntentPhotoModeEntry";
    private static final String INTENT_VIDEO_MODE
            = "com.mediatek.camera.common.mode.video.intentvideo.IntentVideoModeEntry";
    private static final String SLOW_MOTION_MODE
            = "com.mediatek.camera.feature.mode.slowmotion.SlowMotionEntry";
    private static final String MATRIX_MODE
            = "com.mediatek.camera.feature.mode.matrix.MatrixModeEntry";
    private static final String DOF_MODE = "com.mediatek.camera.feature.mode.dof.DofModeEntry";
    private static final String FORMATS = "com.mediatek.camera.feature.setting.format.FormatEntry";
    private static final String SLOW_MOTION_QUALITY =
            "com.mediatek.camera.feature.setting.videoquality.SlowMotionQualityEntry";
    private static final String POST_VIEW
            = "com.mediatek.camera.feature.setting.postview.PostViewEntry";
    private static final String FB_MODE
            = "com.mediatek.camera.feature.mode.facebeauty.FaceBeautyModeEntry";
    private static final String VFB_MODE
            = "com.mediatek.camera.feature.mode.vfacebeauty.VendorFaceBeautyModeEntry";

    private static final String AIBEAUTY_PHOTO_MODE = "com.mediatek.camera.feature.mode.aicombo.photo.AIBeautyPhotoEntry";
    private static final String AIBEAUTY_VIDEO_MODE = "com.mediatek.camera.feature.mode.aicombo.video.AIBeautyVideoModeEntry";
    private static final String AIBOKEH_PHOTO_MODE = "com.mediatek.camera.feature.mode.aicombo.photo.AIBokehPhotoEntry";
    private static final String AIBOKEH_VIDEO_MODE = "com.mediatek.camera.feature.mode.aicombo.video.AIBokehVideoModeEntry";
    private static final String AICOLOR_PHOTO_MODE = "com.mediatek.camera.feature.mode.aicombo.photo.AIColorPhotoEntry";
    private static final String AICOLOR_VIDEO_MODE = "com.mediatek.camera.feature.mode.aicombo.video.AIColorVideoModeEntry";
    private static final String AILEGGY_PHOTO_MODE = "com.mediatek.camera.feature.mode.aicombo.photo.AILeggyPhotoEntry";
    private static final String AILEGGY_VIDEO_MODE = "com.mediatek.camera.feature.mode.aicombo.video.AILeggyVideoModeEntry";
    private static final String AISLIMMING_PHOTO_MODE = "com.mediatek.camera.feature.mode.aicombo.photo.AISlimmingPhotoEntry";
    private static final String AISLIMMING_VIDEO_MODE = "com.mediatek.camera.feature.mode.aicombo.video.AISlimmingVideoModeEntry";
    private static final String TPI_FB
            = "com.mediatek.camera.feature.setting.demofb.DemoFbEntry";
    private static final String TPI_EIS
            = "com.mediatek.camera.feature.setting.demoeis.DemoEisEntry";
    private static final String TPI_ASYNC
            = "com.mediatek.camera.feature.setting.demoasd.DemoAsdEntry";
    private static ConcurrentHashMap<String, IFeatureEntry>
            sBuildInEntries = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, IFeatureEntry>
            sPluginEntries = new ConcurrentHashMap<>();
    //add by huangfei for camerasound start				
    private static final String CAMERA_SOUND = "com.mediatek.camera.feature.setting.camerasound.CameraSound";			
    //add by huangfei for camerasound end

    //add by huangfei for promode start
    private static final String PRO_MODE = "com.mediatek.camera.feature.mode.pro.ProEntry";
    //add by huangfei for promode end
	
    //add by huangfei for monomode start
    private static final String MONO_MODE = "com.mediatek.camera.feature.mode.mono.MonoEntry";
    //add by huangfei for monomode end

    //*/ hct.huangfei, 20201024. add location.
    private static final String LOCATION = "com.mediatek.camera.feature.setting.location.LocationEntry";
    //*/

    //*/ hct.huangfei, 20201021. add hctfacebeauty mode.
    private static final String HCTFACEBEAUTY_MODE = "com.mediatek.camera.feature.mode.hctfacebeauty.HctFaceBeautyEntry";
    //*/
    
    //*/ hct.huangfei, 20201021. add hctbokeh mode.
    private static final String HCTBOKEH_MODE = "com.mediatek.camera.feature.mode.hctbokeh.HctBokehEntry";
    //*/
    
    //*/ hct.huangfei, 20201026. add storagepath.
    private static final String STORAGE_PATH = "com.mediatek.camera.feature.setting.storagepath.StoragePathEntry";
    //*/ 

    //*/ hct.huangfei, 20201027. add camera mirror.   
    private static final String MIRROR = "com.mediatek.camera.feature.setting.mirror.MirrorEntry";
    //*/

    //*/ hct.huangfei, 20201028. add gridlines.
    private static final String GRIDLINES = "com.mediatek.camera.feature.setting.gridlines.GridlinesEntry";
    //*/

    //*/ hct.huangfei, 20201028. add water mark.
    private static final String WATER_MARK = "com.mediatek.camera.feature.setting.watermark.WaterMarkEntry";
    //*/

    //*/ hct.huangfei, 20201210.add volume key function.
    private static final String VOLUME_KEY = "com.mediatek.camera.feature.setting.volumekey.VolumeKeyEntry";
    //*/

    /* hct.wangsenhao, for camera switch @{ */
    private static final String TRIPLE_SWITCH = "com.mediatek.camera.feature.setting.TripleswitchEntry";
    /* }@ hct.wangsenhao */

    //add by liangchangwei for night mode start
    private static final String NIGHT_MODE = "com.mediatek.camera.feature.mode.night.NightEntry";
    //add by liangchangwei for night mode end
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
    private static final String AIWORKSFACEBEAUTY_MODE = "com.mediatek.camera.feature.mode.aiworksfacebeauty.AiworksFaceBeautyEntry";
    private static final String AIWORKSBOKEH_MODE = "com.mediatek.camera.feature.mode.aiworksbokeh.AiWorksBokehEntry";
    private static final String AIWORKSBOKEHCOLOR_MODE = "com.mediatek.camera.feature.mode.aiworksbokehcolor.AiWorksBokehColorEntry";
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
    //bv wuyonglin add for Superphoto should fixed picture size 20201023 start
    private static final String SUPERPHOTO_MODE = "com.mediatek.camera.feature.mode.superphoto.SuperphotoEntry";
    //bv wuyonglin add for Superphoto should fixed picture size 20201023 end

    /**
     * Update current mode key to feature entry, dual camera zoom need to set properties
     * in photo and video mode before open camera, this notify only update to setting feature.
     *
     * @param context        current application context.
     * @param currentModeKey current mode key.
     */
    public static void updateSettingCurrentModeKey(@Nonnull Context context,
                                                   @Nonnull String currentModeKey) {
        LogHelper.d(TAG, "[updateCurrentModeKey] current mode key:" + currentModeKey);
        if (sBuildInEntries.size() <= 0) {
            loadBuildInFeatures(context);
        }
    }

    /**
     * Notify setting feature before open camera, this event only need to notify setting feature.
     *
     * @param context   the context.
     * @param cameraId  want to open which camera.
     * @param cameraApi use which api.
     */
    public static void notifySettingBeforeOpenCamera(@Nonnull Context context,
                                                     @Nonnull String cameraId,
                                                     @Nonnull CameraApi cameraApi) {
        LogHelper.d(TAG, "[notifySettingBeforeOpenCamera] id:" + cameraId + ", api:" + cameraApi);
        //don't consider plugin feature? because plugin feature need more time to load
        if (sBuildInEntries.size() <= 0) {
            loadBuildInFeatures(context);
        }
        Iterator iterator = sBuildInEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry item = (Map.Entry) iterator.next();
            IFeatureEntry entry = (IFeatureEntry) item.getValue();
            if (ICameraSetting.class.equals(entry.getType())) {
                entry.notifyBeforeOpenCamera(cameraId, cameraApi);
            }
        }
    }

    /**
     * Load plugin feature entries, should be called in non-ui thread.
     *
     * @param context the application context.
     * @return the plugin features.
     */
    public static ConcurrentHashMap<String, IFeatureEntry> loadPluginFeatures(
            final Context context) {
        return sPluginEntries;
    }

    /**
     * Load build in feature entries, should be called in non-ui thread.
     *
     * @param context the application context.
     * @return the build-in features.
     */
    public static ConcurrentHashMap<String, IFeatureEntry> loadBuildInFeatures(Context context) {
        if (sBuildInEntries.size() > 0) {
            return sBuildInEntries;
        }
        IPerformanceProfile profile = PerformanceTracker.create(TAG,
                "Build-in Loading");
        profile.start();
        sBuildInEntries = new ConcurrentHashMap<>(loadClasses(context));
        profile.stop();
        return sBuildInEntries;
    }

    private static LinkedHashMap<String, IFeatureEntry> loadClasses(Context context) {
        LinkedHashMap<String, IFeatureEntry> entries = new LinkedHashMap<>();
        DeviceSpec deviceSpec = CameraApiHelper.getDeviceSpec(context);

        IFeatureEntry postviewEntry = new PostViewEntry(context, context.getResources());
        postviewEntry.setDeviceSpec(deviceSpec);
        entries.put(POST_VIEW, postviewEntry);

        IFeatureEntry cameraSwitchEntry = new CameraSwitcherEntry(context, context.getResources());
        cameraSwitchEntry.setDeviceSpec(deviceSpec);
        entries.put(CAMERA_SWITCH, cameraSwitchEntry);

        IFeatureEntry continuousShotEntry = new ContinuousShotEntry(context,
                context.getResources());
        continuousShotEntry.setDeviceSpec(deviceSpec);
        entries.put(CONTINUOUSSHOT, continuousShotEntry);

        IFeatureEntry dngEntry = new DngEntry(context, context.getResources());
        dngEntry.setDeviceSpec(deviceSpec);
        entries.put(DNG, dngEntry);

        IFeatureEntry dualZoomEntry = new DualZoomEntry(context, context.getResources());
        dualZoomEntry.setDeviceSpec(deviceSpec);
        entries.put(DUAL_ZOOM, dualZoomEntry);

        IFeatureEntry selfTimeEntry = new SelfTimerEntry(context, context.getResources());
        selfTimeEntry.setDeviceSpec(deviceSpec);
        entries.put(SELFTIME, selfTimeEntry);

        IFeatureEntry faceDetectionEntry = new FaceDetectionEntry(context, context.getResources());
        faceDetectionEntry.setDeviceSpec(deviceSpec);
        entries.put(FACE_DETECTION, faceDetectionEntry);

        IFeatureEntry flashEntry = new FlashEntry(context, context.getResources());
        flashEntry.setDeviceSpec(deviceSpec);
        entries.put(FLASH, flashEntry);

        IFeatureEntry hdrEntry = new HdrEntry(context, context.getResources());
        hdrEntry.setDeviceSpec(deviceSpec);
        entries.put(HDR, hdrEntry);

        IFeatureEntry hdrModeEntry = new HdrModeEntry(context, context.getResources());
        hdrModeEntry.setDeviceSpec(deviceSpec);
        entries.put(HDR_MODE, hdrModeEntry);

        IFeatureEntry panoramaModeEntry = new PanoramaModeEntry(context, context.getResources());
        panoramaModeEntry.setDeviceSpec(deviceSpec);
        entries.put(PANORAMA_MODE, panoramaModeEntry);

        IFeatureEntry pictureSizeEntry = new PictureSizeEntry(context, context.getResources());
        pictureSizeEntry.setDeviceSpec(deviceSpec);
        entries.put(PICTURE_SIZE, pictureSizeEntry);

        IFeatureEntry previewModeEntry = new PreviewModeEntry(context, context.getResources());
        previewModeEntry.setDeviceSpec(deviceSpec);
        entries.put(PREVIEW_MODE, previewModeEntry);

        IFeatureEntry videoQualityEntry = new VideoQualityEntry(context, context.getResources());
        videoQualityEntry.setDeviceSpec(deviceSpec);
        entries.put(VIDEO_QUALITY, videoQualityEntry);

        IFeatureEntry videoFormatEntry = new VideoFormatEntry(context, context.getResources());
        videoFormatEntry.setDeviceSpec(deviceSpec);
        entries.put(VIDEO_FORMAT, videoFormatEntry);

        IFeatureEntry zoomEntry = new ZoomEntry(context, context.getResources());
        zoomEntry.setDeviceSpec(deviceSpec);
        entries.put(ZOOM, zoomEntry);

        IFeatureEntry focusEntry = new FocusEntry(context, context.getResources());
        focusEntry.setDeviceSpec(deviceSpec);
        entries.put(FOCUS, focusEntry);

        IFeatureEntry exposureEntry = new ExposureEntry(context, context.getResources());
        exposureEntry.setDeviceSpec(deviceSpec);
        entries.put(EXPOSURE, exposureEntry);

        IFeatureEntry microPhoneEntry = new MicroPhoneEntry(context, context.getResources());
        microPhoneEntry.setDeviceSpec(deviceSpec);
        entries.put(MICHROPHONE, microPhoneEntry);

        IFeatureEntry hdr10Entry = new Hdr10Entry(context, context.getResources());
        hdr10Entry.setDeviceSpec(deviceSpec);
        entries.put(HDR10, hdr10Entry);

        IFeatureEntry noiseReductionEntry = new NoiseReductionEntry(context, context.getResources());
        noiseReductionEntry.setDeviceSpec(deviceSpec);
        entries.put(NOISE_REDUCTION, noiseReductionEntry);

        IFeatureEntry EisPhoneEntry = new EISEntry(context, context.getResources());
        EisPhoneEntry.setDeviceSpec(deviceSpec);
        entries.put(EIS, EisPhoneEntry);

        IFeatureEntry Fps60PhoneEntry = new Fps60Entry(context, context.getResources());
        Fps60PhoneEntry.setDeviceSpec(deviceSpec);
        entries.put(FPS60, Fps60PhoneEntry);

        IFeatureEntry aisEntry = new AISEntry(context, context.getResources());
        aisEntry.setDeviceSpec(deviceSpec);
        entries.put(AIS, aisEntry);

        IFeatureEntry sceneModeEntry = new SceneModeEntry(context, context.getResources());
        sceneModeEntry.setDeviceSpec(deviceSpec);
        entries.put(SCENE_MODE, sceneModeEntry);

        IFeatureEntry whiteBalanceEntry = new WhiteBalanceEntry(context, context.getResources());
        whiteBalanceEntry.setDeviceSpec(deviceSpec);
        entries.put(WHITE_BALANCE, whiteBalanceEntry);

        IFeatureEntry antiFlickerEntry = new AntiFlickerEntry(context, context.getResources());
        antiFlickerEntry.setDeviceSpec(deviceSpec);
        entries.put(ANTI_FLICKER, antiFlickerEntry);

        IFeatureEntry zsdEntry = new ZSDEntry(context, context.getResources());
        zsdEntry.setDeviceSpec(deviceSpec);
        entries.put(ZSD, zsdEntry);

        IFeatureEntry isoEntry = new ISOEntry(context, context.getResources());
        isoEntry.setDeviceSpec(deviceSpec);
        entries.put(ISO, isoEntry);

        IFeatureEntry aeAfDebugEntry = new AaaRoiDebugEntry(context, context.getResources());
        aeAfDebugEntry.setDeviceSpec(deviceSpec);
        entries.put(AE_AF_DEBUG, aeAfDebugEntry);

        IFeatureEntry sDofPhotoEntry = new SdofPhotoEntry(context, context.getResources());
        sDofPhotoEntry.setDeviceSpec(deviceSpec);
        entries.put(SDOF_PHOTO_MODE, sDofPhotoEntry);

        IFeatureEntry sDofVideoEntry = new SdofVideoModeEntry(context, context.getResources());
        sDofVideoEntry.setDeviceSpec(deviceSpec);
        entries.put(SDOF_VIDEO_MODE, sDofVideoEntry);

        IFeatureEntry shutterSpeedEntry = new ShutterSpeedEntry(context, context.getResources());
        shutterSpeedEntry.setDeviceSpec(deviceSpec);
        entries.put(SHUTTER_SPEED, shutterSpeedEntry);

        IFeatureEntry longExposureEntry = new LongExposureModeEntry(context,
                context.getResources());
        longExposureEntry.setDeviceSpec(deviceSpec);
        entries.put(LONG_EXPUSURE_MODE, longExposureEntry);

        IFeatureEntry photoEntry = new PhotoModeEntry(context, context.getResources());
        photoEntry.setDeviceSpec(deviceSpec);
        entries.put(PHOTO_MODE, photoEntry);

        IFeatureEntry videoEntry = new VideoModeEntry(context, context.getResources());
        videoEntry.setDeviceSpec(deviceSpec);
        entries.put(VIDEO_MODE, videoEntry);

        IFeatureEntry intentVideoEntry = new IntentVideoModeEntry(context, context.getResources());
        intentVideoEntry.setDeviceSpec(deviceSpec);
        entries.put(INTENT_VIDEO_MODE, intentVideoEntry);

        IFeatureEntry intentPhotoEntry = new IntentPhotoModeEntry(context, context.getResources());
        intentPhotoEntry.setDeviceSpec(deviceSpec);
        entries.put(INTENT_PHOTO_MODE, intentPhotoEntry);

        IFeatureEntry slowMotionEntry = new SlowMotionEntry(context, context.getResources());
        slowMotionEntry.setDeviceSpec(deviceSpec);
        entries.put(SLOW_MOTION_MODE, slowMotionEntry);

        IFeatureEntry formatsEntry = new FormatEntry(context, context.getResources());
        formatsEntry.setDeviceSpec(deviceSpec);
        entries.put(FORMATS, formatsEntry);

        IFeatureEntry slowMotionQualityEntry = new SlowMotionQualityEntry(context,
                context.getResources());
        slowMotionQualityEntry.setDeviceSpec(deviceSpec);
        entries.put(SLOW_MOTION_QUALITY, slowMotionQualityEntry);

        IFeatureEntry matrixModeEntry = new MatrixModeEntry(context,
                context.getResources());
        matrixModeEntry.setDeviceSpec(deviceSpec);
        entries.put(MATRIX_MODE, matrixModeEntry);

//        IFeatureEntry matrixDisplayEntry = new MatrixDisplayEntry(context,
//                context.getResources());
//        matrixDisplayEntry.setDeviceSpec(deviceSpec);
//        entries.put(MATRIX_SETTING, matrixDisplayEntry);

        IFeatureEntry dofEntry = new DofModeEntry(context,
                context.getResources());
        dofEntry.setDeviceSpec(deviceSpec);
        entries.put(DOF_MODE, dofEntry);

        IFeatureEntry fbEntry = new FaceBeautyModeEntry(context,
                context.getResources());
        fbEntry.setDeviceSpec(deviceSpec);
        entries.put(FB_MODE, fbEntry);

        IFeatureEntry vfbEntry = new VendorFaceBeautyModeEntry(context,
                context.getResources());
        vfbEntry.setDeviceSpec(deviceSpec);
        entries.put(VFB_MODE, vfbEntry);

        IFeatureEntry visualSearchSettingEntry = new VisualSearchSettingEntry(context,
                context.getResources());
        visualSearchSettingEntry.setDeviceSpec(deviceSpec);
        entries.put(VisualSearchSetting, visualSearchSettingEntry);

        IFeatureEntry visualSearchModeEntry = new VisualSearchModeEntry(context,
                context.getResources());
        visualSearchModeEntry.setDeviceSpec(deviceSpec);
        entries.put(VisualSearch, visualSearchModeEntry);

        IFeatureEntry aiBeautyPhotoEntry = new AIBeautyPhotoEntry(context, context.getResources());
        aiBeautyPhotoEntry.setDeviceSpec(deviceSpec);
        entries.put(AIBEAUTY_PHOTO_MODE, aiBeautyPhotoEntry);

        IFeatureEntry aiBeautyVideoEntry = new AIBeautyVideoModeEntry(context, context.getResources());
        aiBeautyVideoEntry.setDeviceSpec(deviceSpec);
        entries.put(AIBEAUTY_VIDEO_MODE, aiBeautyVideoEntry);

        IFeatureEntry aiBokehPhotoEntry = new AIBokehPhotoEntry(context, context.getResources());
        aiBokehPhotoEntry.setDeviceSpec(deviceSpec);
        entries.put(AIBOKEH_PHOTO_MODE, aiBokehPhotoEntry);

        IFeatureEntry aiBokehVideoEntry = new AIBokehVideoModeEntry(context, context.getResources());
        aiBokehVideoEntry.setDeviceSpec(deviceSpec);
        entries.put(AIBOKEH_VIDEO_MODE, aiBokehVideoEntry);

        IFeatureEntry aiColorPhotoEntry = new AIColorPhotoEntry(context, context.getResources());
        aiColorPhotoEntry.setDeviceSpec(deviceSpec);
        entries.put(AICOLOR_PHOTO_MODE, aiColorPhotoEntry);

        IFeatureEntry aiColorVideoEntry = new AIColorVideoModeEntry(context, context.getResources());
        aiColorVideoEntry.setDeviceSpec(deviceSpec);
        entries.put(AICOLOR_VIDEO_MODE, aiColorVideoEntry);

        IFeatureEntry aiLeggyPhotoEntry = new AILeggyPhotoEntry(context, context.getResources());
        aiLeggyPhotoEntry.setDeviceSpec(deviceSpec);
        entries.put(AILEGGY_PHOTO_MODE, aiLeggyPhotoEntry);

        IFeatureEntry aiLeggyVideoEntry = new AILeggyVideoModeEntry(context, context.getResources());
        aiLeggyVideoEntry.setDeviceSpec(deviceSpec);
        entries.put(AILEGGY_VIDEO_MODE, aiLeggyVideoEntry);

        IFeatureEntry aiSlimmingPhotoEntry = new AISlimmingPhotoEntry(context, context.getResources());
        aiSlimmingPhotoEntry.setDeviceSpec(deviceSpec);
        entries.put(AISLIMMING_PHOTO_MODE, aiSlimmingPhotoEntry);

        IFeatureEntry aiSlimmingVideoEntry = new AISlimmingVideoModeEntry(context, context.getResources());
        aiSlimmingVideoEntry.setDeviceSpec(deviceSpec);
        entries.put(AISLIMMING_VIDEO_MODE, aiSlimmingVideoEntry);

        IFeatureEntry demoFbEntry = new DemoFbEntry(context, context.getResources());
        demoFbEntry.setDeviceSpec(deviceSpec);
        entries.put(TPI_FB, demoFbEntry);

        IFeatureEntry demoEisEntry = new DemoEisEntry(context, context.getResources());
        demoEisEntry.setDeviceSpec(deviceSpec);
        entries.put(TPI_EIS, demoEisEntry);

        IFeatureEntry demoAsdEntry = new DemoAsdEntry(context, context.getResources());
        demoAsdEntry.setDeviceSpec(deviceSpec);
        entries.put(TPI_ASYNC, demoAsdEntry);

		//add by huangfei for camerasound start
        IFeatureEntry soundEntry = new CameraSoundEntry(context, context.getResources());
        soundEntry.setDeviceSpec(deviceSpec);
        entries.put(CAMERA_SOUND, soundEntry);
        //add by huangfei for camerasound end
		
        //add by huangfei for monomode start
        IFeatureEntry monoEntry = new MonoEntry(context, context.getResources());
        monoEntry.setDeviceSpec(deviceSpec);
        entries.put(MONO_MODE, monoEntry);
        //add by huangfei for monomode end		

        //add by huangfei for promode start
        IFeatureEntry proEntry = new ProEntry(context, context.getResources());
        proEntry.setDeviceSpec(deviceSpec);
        entries.put(PRO_MODE, proEntry);
        //add by huangfei for promode end
		
		//*/ hct.huangfei, 20201021. add hctfacebeauty mode.
        IFeatureEntry hctFaceBeautyEntry = new HctFaceBeautyEntry(context, context.getResources());
        hctFaceBeautyEntry.setDeviceSpec(deviceSpec);
        entries.put(HCTFACEBEAUTY_MODE, hctFaceBeautyEntry);
        //*/
        
        //*/ hct.huangfei, 20201021. add hctbokeh mode.
        IFeatureEntry hctBokehEntry = new HctBokehEntry(context, context.getResources());
        hctBokehEntry.setDeviceSpec(deviceSpec);
        entries.put(HCTBOKEH_MODE, hctBokehEntry);        
        //*/

        //*/ hct.huangfei, 20201024. add location.
        IFeatureEntry locationEntry = new LocationEntry(context, context.getResources());
        locationEntry.setDeviceSpec(deviceSpec);
        entries.put(LOCATION, locationEntry);	
        //*/
		
        //*/ hct.huangfei, 20201026. add storagepath.
        IFeatureEntry storagePathEntry = new StoragePathEntry(context, context.getResources());
        storagePathEntry.setDeviceSpec(deviceSpec);
        entries.put(STORAGE_PATH, storagePathEntry);
        //*/

        //*/ hct.huangfei, 20201027. add camera mirror.
        IFeatureEntry mirrorEntry = new MirrorEntry(context, context.getResources());
        mirrorEntry.setDeviceSpec(deviceSpec);
        entries.put(MIRROR, mirrorEntry);
        //*/

        //*/ hct.huangfei, 20201028. add gridlines.
        IFeatureEntry gridlinesEntry = new GridlinesEntry(context, context.getResources());
        gridlinesEntry.setDeviceSpec(deviceSpec);
        entries.put(GRIDLINES, gridlinesEntry);
        //*/

        //*/ hct.huangfei, 20201028. add water mark.
        IFeatureEntry waterMarkEntry = new WaterMarkEntry(context, context.getResources());
        waterMarkEntry.setDeviceSpec(deviceSpec);
        entries.put(WATER_MARK, waterMarkEntry);
        //*/

        //*/ hct.huangfei, 20201210.add volume key function.
        IFeatureEntry volumeKeyEntry = new VolumeKeyEntry(context, context.getResources());
        volumeKeyEntry.setDeviceSpec(deviceSpec);
        entries.put(VOLUME_KEY, volumeKeyEntry);
        //*/
        
        /* hct.wangsenhao, for camera switch @{ */
        IFeatureEntry tripleswitchEntry = new TripleswitchEntry(context, context.getResources());
        tripleswitchEntry.setDeviceSpec(deviceSpec);
        entries.put(TRIPLE_SWITCH, tripleswitchEntry);
        /* }@ hct.wangsenhao */

        //add by liangchangwei for nightmode start
        IFeatureEntry NightEntry = new NightEntry(context, context.getResources());
        NightEntry.setDeviceSpec(deviceSpec);
        entries.put(NIGHT_MODE, NightEntry);
        //add by liangchangwei for nightmode end

        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
        IFeatureEntry aiworksFaceBeautyEntry = new AiworksFaceBeautyEntry(context, context.getResources());
        aiworksFaceBeautyEntry.setDeviceSpec(deviceSpec);
        entries.put(AIWORKSFACEBEAUTY_MODE, aiworksFaceBeautyEntry);

        IFeatureEntry aiworksBokehEntry = new AiWorksBokehEntry(context, context.getResources());
        aiworksBokehEntry.setDeviceSpec(deviceSpec);
        entries.put(AIWORKSBOKEH_MODE, aiworksBokehEntry);

        IFeatureEntry aiworksBokehColorEntry = new AiWorksBokehColorEntry(context, context.getResources());
        aiworksBokehColorEntry.setDeviceSpec(deviceSpec);
        entries.put(AIWORKSBOKEHCOLOR_MODE, aiworksBokehColorEntry);
        //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end

        //bv wuyonglin add for Superphoto should fixed picture size 20201023 start
        IFeatureEntry superphotoEntry = new SuperphotoEntry(context, context.getResources());
        superphotoEntry.setDeviceSpec(deviceSpec);
        entries.put(SUPERPHOTO_MODE, superphotoEntry);
        //bv wuyonglin add for Superphoto should fixed picture size 20201023 start
        return entries;
    }
}