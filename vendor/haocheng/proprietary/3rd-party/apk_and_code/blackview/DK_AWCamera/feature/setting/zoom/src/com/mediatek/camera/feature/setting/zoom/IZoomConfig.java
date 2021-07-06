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

package com.mediatek.camera.feature.setting.zoom;

/**
 * This is for zoom control flow to perform zoom.
 */

public interface IZoomConfig {

    public static final String KEY_CAMERA_ZOOM = "key_camera_zoom";
    public static final String ZOOM_ON = "on";
    public static final String ZOOM_OFF = "off";
    public static final String PATTERN = "%.1f";
    public static final int DISTANCE_RATIO_MIN = -1;
    public static final int DISTANCE_RATIO_MAX = 2;
    /**
     * This is to register zoom changed listener.
     *
     * @param zoomUpdateListener the zoom update listener.
     */
    public void setZoomUpdateListener(OnZoomLevelUpdateListener zoomUpdateListener);

    /**
     * This is onScale to prepare the zoom level.
     *
     * @param distanceRatio the zoom distance ratio.
     */
    public void onScalePerformed(double distanceRatio);

    /**
     * This is to notify scale status.
     *
     * @param isBegin the scale begin or not
     */
    public void onScaleStatus(boolean isBegin);

    //*/ hct.huangfei, 20201030. add customize zoom.
    public void onScale(float ratio);

    public String getZoomLevel();
    //*/
    /**
     * listener zoom level change.
     */
    public interface OnZoomLevelUpdateListener {
        /**
         * for zoom level update.
         * @param level the zoom level
         */
        public void onZoomLevelUpdate(String level);

        /**
         * This is to get the override value by other feature.
         * @return the override value.
         */
        public String onGetOverrideValue();
    }

    //add by huangfei for zoom switch start
    public void setOnZoomSwitchListener(OnZoomSwitchListener onZoomSwitchListener);
    public void removeOnZoomSwitchListener(OnZoomSwitchListener onZoomSwitchListener);
    public interface OnZoomSwitchListener {
        public void onZoomSwitchByDecrease(String cameraId,float basicZoomRatio);
        public void onZoomSwitchByIncrease(String cameraId,float basicZoomRatio);
    }
    //add by huangfei for zoom switch end
    //*/ hct.huangfei, 20201030. add customize zoom.
    public void setZoomSliderUpdateListener(ZoomLevelSliderListener zoomSliderUpdateListener);
    /**
     * listener zoom level change.
     */
    public interface ZoomLevelSliderListener {
        /**
         * for zoom ratio update notify.
         * @param ratio the zoom ratio
         */
        public void onZoomLevelUpdateNotify(String ratio);
        public void onScaleStatus(boolean isBegin);
        public void hide();
        public void reset();
    }
    //*/

    //*/ hct.huangfei, 20201210.add volume key function.
    public float getCurZoomRatio();


    public boolean isOutZoomRange(float ratio);

    //*/
    //add by huangfei for zoom switch start
    public boolean getCameraSwitchByZoom(double distanceRatio,float basicZoomRatio,float raito,float direction);
    //add by huangfei for zoom switch end

}
