/*
 *   Copyright Statement:
 *
 *     This software/firmware and related documentation ("MediaTek Software") are
 *     protected under relevant copyright laws. The information contained herein is
 *     confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *     the prior written permission of MediaTek inc. and/or its licensors, any
 *     reproduction, modification, use or disclosure of MediaTek Software, and
 *     information contained herein, in whole or in part, shall be strictly
 *     prohibited.
 *
 *     MediaTek Inc. (C) 2016. All rights reserved.
 *
 *     BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *    THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *     RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *     ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *     WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *     WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *     NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *     RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *     INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *     TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *     RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *     OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *     SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *     RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *     STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *     ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *     RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *     MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *     CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *     The following software/firmware and/or related documentation ("MediaTek
 *     Software") have been modified by MediaTek Inc. All revisions are subject to
 *     any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.camera.common;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import com.mediatek.camera.ui.shutter.ShutterButtonManager;

import com.mediatek.camera.common.IAppUiListener.ISurfaceStatusListener;
import com.mediatek.camera.common.IAppUiListener.OnGestureListener;
import com.mediatek.camera.common.IAppUiListener.OnModeChangeListener;
import com.mediatek.camera.common.IAppUiListener.OnPreviewAreaChangedListener;
import com.mediatek.camera.common.IAppUiListener.OnPreviewTouchedListener;
import com.mediatek.camera.common.IAppUiListener.OnShutterButtonListener;
import com.mediatek.camera.common.IAppUiListener.OnThumbnailClickedListener;
import com.mediatek.camera.common.mode.IReviewUI;
import com.mediatek.camera.common.mode.photo.intent.IIntentPhotoUi;
import com.mediatek.camera.common.mode.video.videoui.IVideoUI;
import com.mediatek.camera.common.setting.ICameraSettingView;
import com.mediatek.camera.common.widget.PreviewFrameLayout;

//add by huangfei for more mode start
import com.mediatek.camera.ui.modepicker.ModePickerManager;
//add by huangfei for more mode end

//*/ hct.huangfei, 20201206.hdr view int topbar.
import com.mediatek.camera.ui.HdrManager;
//*/

import java.util.List;

import javax.annotation.Nonnull;

//*/ hct.huangfei, 20201028. add gridlines.
import com.mediatek.camera.feature.setting.gridlines.GridlinesMonitor;
//*/
//add by huangfei for water mark start
import com.mediatek.camera.common.exif.ExifInterface;
//add by huangfei for water mark end

//*/ hct.huangfei, 20201030. add customize zoom.
import com.mediatek.camera.common.mode.photo.view.IZoomSliderUI;
import com.mediatek.camera.ui.photo.ZoomSliderUICtrl;
import com.mediatek.camera.feature.setting.zoom.IZoomConfig;
//*/
import android.view.TextureView;
import com.mediatek.camera.ui.preview.TextureViewController;
//bv wuyonglin add for bug3677 20210204 start
import com.mediatek.camera.common.mode.video.VideoMode.VideoState;
//bv wuyonglin add for bug3677 20210204 end

/**
 * Camera app level UI interface, define the common APIs of app UI.
 */
public interface IAppUi {

    /**
     * APPUIState is a structure for features to specify their ideal
     * common UI visible state.
     * Once constructed by a feature, this class should be treated as read only.
     * Visible state value reference:
     * {@link android.view.View.VISIBLE,android.view.View.INVISIBLE,android.view.View.GONE}
     * Enable state value is true or false:
     * True if the view module enable click and focus.
     * False if the view module disable click and focus.
     */
     class AppUIState {
        public int mQuickSwitcherVisibleState;
        public int mModeSwitcherVisibleState;
        public int mThumbnailVisibleState;
        public int mShutterButtonVisibleState;
        public int mIndicatorVisibleState;
        public int mPreviewFrameVisibleState;
        public boolean mQuickSwitcherEnabled;
        public boolean mModeSwitcherEnabled;
        public boolean mThumbnailEnabled;
        public boolean mShutterButtonEnabled;
        public boolean mIndicatorEnabled;
    }

    int QUICK_SWITCHER = 0;
    int MODE_SWITCHER = 1;
    int THUMBNAIL = 2;
    int SHUTTER_BUTTON = 3;
    int INDICATOR = 4;
    int PREVIEW_FRAME = 5;
    int GESTURE = 6;
    int SHUTTER_TEXT = 7;
    int SCREEN_HINT = 8;
    int VIDEO_FLASH = 9;
    //bv wuyonglin add for adjust custom FaceMode view position 20200303 start
    int SHUTTER_ROOTLAYOUT = 10;
    //bv wuyonglin add for adjust custom FaceMode view position 20200303 end

    int DEFAULT_PRIORITY = Integer.MAX_VALUE;

    /**
     * A item that has a mode information for register in App UI.
     */
    class ModeItem {
        //The view that will be shown on the mode list.
        public Drawable mModeUnselectedIcon;
        public Drawable mModeSelectedIcon;
        //The shutter view of the mode.
        public Drawable mShutterIcon;
        //The priority value, the smaller the value, the higher the priority.
        // the higher priority icon will be shown in the front of the mode list.
        public int mPriority;
        //The mode type. Such as "Picture" or "Video"
        public String mType;
        //Mode name.
        public String mClassName;
        //Mode key, different modes of the same feature only has one key value.
        //For example:
        //Pip feature has two modes, one for picture, another for video, but they should have same
        //key value.
        public String mModeName;
        //Used for judge current mode support which cameras.
        //such as panorama mode just supported in back camera, so will fill {0};
        //such as face beauty support both front camera and back camera, will fill {0,1}
        public String[] mSupportedCameraIds;
        //add by Jerry
        public String mMode;
        public String mTitle;
    }

    /**
     * Define animation data structure, i will contain the data to do
     * the animation.
     */
    class AnimationData {
        //The preview data.
        public byte[] mData;
        //The preview data format.
        public int mFormat;
        //The preview data width.
        public int mWidth;
        //The preview data height.
        public int mHeight;
        //The preview data orientation.
        public int mOrientation;
        //The preview data is need mirror or not.
        public boolean mIsMirror;
    }

    /**
     * Define the animation type.
     */
    enum AnimationType {
        TYPE_SWITCH_CAMERA,
        TYPE_CAPTURE,
        TYPE_SWITCH_MODE
    }

    /**
     * Screen hint type.
     */
    enum HintType {
        TYPE_ALWAYS_TOP,
        TYPE_AUTO_HIDE,
        TYPE_MANUAL_HIDE,  //add by huangfei 
        TYPE_ALWAYS_BOTTOM
    }

    /**
     * Screen hint information definition.
     */
    class HintInfo {
        public HintType mType;
        public String  mHintText;
        public Drawable mBackground;
        public int mDelayTime;
    }

    /**
     * Show screen hint.
     * @param info The hint information.
     */
    void showScreenHint(HintInfo info);

    /**
     * Hide screen hint.
     * @param info The hint information.
     */
    void hideScreenHint(HintInfo info);
    /**
     * Called indirectly from each feature in their initialization to get a view group
     * to inflate the module specific views in.
     *
     * @return a view group for modules to attach views to
     */
     ViewGroup getModeRootView();

    /**
     * Get shutter root view.
     * @return the shutter root view.
     */
    View getShutterRootView();

    /**
     * Get preview frame layout, it is a parent view for focus and face detection view.
     * @return preview frame layout.
     */
     PreviewFrameLayout getPreviewFrameLayout();

    /**
     * When preview started, notify the event to camera app UI.
     * @param cameraId current preview camera id.
     */
     void onPreviewStarted(String cameraId);

    /**
     * When switch camera started, notify the event to camera app UI.
     * @param cameraId To be previewing camera id.
     */
     void onCameraSelected(String cameraId);

    /**
     * Config UI module visibility.
     * It will post a Runnable() to UI thread looper, latency will happened when invoke the
     * function in UI thread directly.
     * @param module Selected UI module.
     *               The UI module can be {@link QUICK_SWITCHER,MODE_SWITCHER, ...}
     * @param visibility Visible state value reference:
     * {@link android.view.View.VISIBLE,android.view.View.INVISIBLE,android.view.View.GONE}
     */
    void setUIVisibility(int module, int visibility);

    /**
     * Config UI module enable state.
     * It will post a Runnable() to UI thread looper, latency will happened when invoke the
     * function in UI thread directly.
     * @param module Selected UI module.
     *               The UI module can be {@link QUICK_SWITCHER,MODE_SWITCHER, ...}
     * @param enabled enabled Enable state value is true or false:
     * True: if the view module enable click and focus.
     * False: if the view module disable click and focus.
     */
    void setUIEnabled(int module, boolean enabled);

    /**
     * Config all common ui visibility.
     * It will post a Runnable() to UI thread looper, latency will happened when invoke the
     * function in UI thread directly.
     * @param visibility Visible state value reference:
     * {@link android.view.View.VISIBLE,android.view.View.INVISIBLE,android.view.View.GONE}
     */
     void applyAllUIVisibility(int visibility);

    /**
     * Config all common ui enable state.
     * It will post a Runnable() to UI thread looper, latency will happened when invoke the
     * function in UI thread directly.
     * @param enabled Enable state value is true or false:
     * True: if the view module enable click and focus.
     * False: if the view module disable click and focus.
     */
     void applyAllUIEnabled(boolean enabled);

    /**
     * Clear the Status previous listener that set when update preview size.
     * @param listener The listener need to be used.
     */
     void clearPreviewStatusListener(ISurfaceStatusListener listener);

    /**
     * Register preview touched listener.
     * @param listener The listener need to be registered.
     */
     void registerOnPreviewTouchedListener(OnPreviewTouchedListener listener);

    /**
     * Unregister preview touched listener.
     * @param listener The listener need to be unregistered.
     */
     void unregisterOnPreviewTouchedListener(OnPreviewTouchedListener listener);
    /**
     * Register preview area changed listener.
     * @param listener The listener need to be registered.
     */
    void registerOnPreviewAreaChangedListener(OnPreviewAreaChangedListener listener);

    /**
     * Unregister preview area changed listener.
     * @param listener The listener need to be unregistered.
     */
    void unregisterOnPreviewAreaChangedListener(OnPreviewAreaChangedListener listener);


    /**
     * Register gesture listener with a priority, the high priority listener can receiver
     * gesture info befor the low priority. high priority listener can consume one gesture
     * message by return true{@link OnGestureListener},and the low priority one can not receive
     * the message.
     * @param listener The listener need to be registered.
     * @param priority  Listener's priority, it is a int value
     *                  the smaller the value, the higher the priority.
     */
     void registerGestureListener(OnGestureListener listener, int priority);

    /**
     * Unregister gesture listener.
     * @param listener The listener need to be unregistered.
     */
     void unregisterGestureListener(OnGestureListener listener);

    /**
     * Register shutter button listener. the high priority listener can receiver
     * gesture info befor the low priority. high priority listener can consume one gesture
     * message by return true{@link OnShutterButtonListener},and the low priority one can not
     * receive the message.
     * @param listener The listener need to be registered.
     * @param priority  Listener's priority, it is a int value
     *                  the smaller the value, the higher the priority.
     */
     void registerOnShutterButtonListener(OnShutterButtonListener listener, int priority);

    /**
     * Unregister shutter button listener.
     * @param listener The listener need to be unregistered.
     */
      void unregisterOnShutterButtonListener(OnShutterButtonListener listener);

    /**
     * Set thumbnail clicked listener.
     * @param listener The listener need to be registered.
     */
     void setThumbnailClickedListener(OnThumbnailClickedListener listener);

    /**
     * Add view to quick switcher with specified priority.
     * @param view The view register to quick switcher.
     * @param priority The priority that the registered view sort order.
     */
     void addToQuickSwitcher(View view, int priority);

    /**
     * Add view to quick switcher with specified priority.
     * @param isChanged Do I need to switch mode.
     * @param flag view click number.
     */
    void isChangedMode(boolean isChanged,int flag);

    /**
     * Remove view from quick switcher.
     * @param view The view removed from quick switcher.
     */
     void removeFromQuickSwitcher(View view);

    /**
     * Add view to indicator view with specified priority.
     * @param view The view register to quick switcher.
     * @param priority The priority that the registered view sort order.
     */
    void addToIndicatorView(View view, int priority);

    /**
     * Remove view from indicator view.
     * @param view The view removed from quick switcher.
     */
    void removeFromIndicatorView(View view);
    /**
     * Set mode change listener.
     *
     * @param listener mode change listener instance.
     */
     void setModeChangeListener(OnModeChangeListener listener);

    /**
     * Invoke the OnModeChangeListener to switch mode.
     * @param newMode The new mode key value.
     */
    void triggerModeChanged(String newMode);

    /**
     * Invoke the onShutterButtonCLicked listener to start a capture event.
     * @param currentPriority Trigger module shutter button listener priority,
     *                        the trigger event will be pass to the modules which shutter listener
     *                        priority is lower than currentPriority value.
     *                        The zero value will pass the click event to all listeners.
     */
    void triggerShutterButtonClick(int currentPriority);

    /**
     * Invoke the onShutterButtonLongPressed listener to start a capture event.
     * @param currentPriority Trigger module shutter button listener priority,
     *                        the trigger event will be pass to the modules which shutter listener
     *                        priority is lower than currentPriority value.
     *                        The zero value will pass the click event to all listeners.
     */
    void triggerShutterButtonLongPressed(int currentPriority);

    /**
     * update thumbnailView.
     * @param bitmap
     *            the bitmap matched with the picture or video, such as
     *            orientation, content. suggest thumbnail view size.
     */
    void updateThumbnail(Bitmap bitmap);

    /**
     * get the width of thumbnail view.
     * @return the min value of width and height of thumbnail view.
     */
    int getThumbnailViewWidth();

    /**
     * Register quick switcher icon view, layout position will be decided by the priority.
     */
    void registerQuickIconDone();

    /**
     * Register indicator icon view, layout position will be decided by the priority.
     */
    void registerIndicatorDone();

    /**
     * Register mode icon that shown in the mode list view. The order of the mode icon will be
     * decided by the priority value.
     * @param items The mode information for register.
     */
    void registerMode(List<ModeItem> items);

    /**
     * Update current running mode.
     * Notice: This is used for mode manager to update current mode, do not
     * use it in any other place.
     * @param mode Mode name.
     */
    void updateCurrentMode(String mode);

    /**
     * Set Camera preview size.
     * @param width preview width, must > 0.
     * @param height preview height, must > 0.
     * @param listener Set preview status listener. The new listener will replace the old one.
     */
    void setPreviewSize(int width, int height, ISurfaceStatusListener listener);
    /**
     * used get the video recording ui.
     * @return video recording ui
     */
    IVideoUI getVideoUi();
    /**
     * used get the review ui.
     * @return review ui.
     */
    IReviewUI getReviewUI();
    /**
     * Get an implementation of intent photo ui.
     * @return an instance of IIntentPhotoUi.
     */
    @Nonnull
    IIntentPhotoUi getPhotoUi();

    /**
     * Add setting view instance to setting view list.
     *
     * @param view The instance of {@link ICameraSettingView}.
     */
    void addSettingView(ICameraSettingView view);

    /**
     * Remove setting view instance from setting view list.
     *
     * @param view The instance of {@link ICameraSettingView}.
     */
    void removeSettingView(ICameraSettingView view);

    /**
     * Refresh setting view.
     */
    void refreshSettingView();

    /**
     * Update setting icon visibility to decide setting icon is shown or not.
     */
    void updateSettingIconVisibility();

    /**
     * Start the animation.
     * @param type Animation type. {@link AnimationType}
     * @param data Animation data. {@link AnimationData}
     */
    void animationStart(AnimationType type, AnimationData data);

    /**
     * Stop the animation.
     * @param type Animation type. {@link AnimationType}
     */
    void animationEnd(AnimationType type);

    /**
     * Show saving dialog. The dialog will cover full screen and no ui should show behind it.
     * @param message The dialog's text view message.
     * @param isNeedShowProgress Is need show progress bar or not.
     */
    void showSavingDialog(String message, boolean isNeedShowProgress);

    /**
     * Hide saving dialog. Notify call showSavingDialog() firstly.
     */
    void hideSavingDialog();

    /**
     * Add nine grid view entry.
     *
     * @param view The entry view.
     */
    void setEffectViewEntry(View view);

    /**
     * Attach effect view entry to view tree;
     */
    void attachEffectViewEntry();

    /**
     * Show quick switcher option view, mode picker and quick switch will disappear.
     * @param optionView the option view, it should not attach to any parent view.
     */
    void showQuickSwitcherOption(View optionView);

    /**
     * Hide quick switcher option view, it will remove from the option parent view.
     */
    void hideQuickSwitcherOption();

    /**
     * Update background brightness when do panel flash.
     *
     * @param visible true when need to show panel,false to hide.
     */
    void updateBrightnessBackGround(boolean visible);


    /**

     * Update panel flash color when needed.
     * @param color The color of customized panel flash.
     */
    void updatePanelColor(int color);
	//add by Jerry
    String getCurrentMode();
    View.OnClickListener getVideoUiClick();
    boolean isSettingShow();
	
	//add by huangfei for lowpower tips start
    public String getCameraId();
	//add by huangfei for lowpower tips end
    //add by huangfei for getCsState start
    //add by huangfei for continuousshot abnormal start
    public void setCaptureStatus(boolean status);
    public boolean getCaptureStatus();
    //add by huangfei for continuousshot abnormal end
    //add by huangfei for more mode start
    public ModePickerManager getModePickerManager();

    public void updateCurrentModeByMore(String mode);
    //add by huangfei for more mode end
	
	//add by huangfei for getCsState start
    public void updateCsState(boolean continuousshoting) ;
    
    public boolean getCsState();
    //add by huangfei for getCsState end
	
    //add by huangfei for shutter title change start
    public View inflate(int layoutId) ;

    public ShutterButtonManager getShutterButtonManager();
    //add by huangfei for shutter title change end

    //*/ hct.huangfei, 20201026. add storagepath.
    void notifyUpdateThumbnail();
    //*/	
    
    //*/ hct.huangfei, 20201028. add gridlines.
    public GridlinesMonitor getMonitor();
    //*/
    /* hct.wangsenhao, underwater camera @{ */
    void modeSwitch(boolean isSwitchToVideo);
    int isUnderWaterSupport();
    public void SwitchCamera();
    /* }@ hct.wangsenhao */
    //add by huangfei for switch camera by up and down start
    public interface CameraSwitchListener {
        void onCameraByUpDownChange();
        void onConfigUIVisibility(int visibility);
        void onConfigUIEnabled(boolean enabled);
    }
    public void setCameraSwitcherListener(CameraSwitchListener listener) ;
    //add by huangfei for switch camera by up and down end

    //*/ hct.huangfei, 20201030. add customize zoom.
    IZoomSliderUI getZoomSliderUI();
    //*/

    //*/ hct.huangfei, 20201030. add customize zoom.
    public float getPreviousSpan();

    public float getBasicZoomRatio();

    public void setZoomSwitchPreviousSpan(float basicZoomRatio,float previousSpan);

    public boolean isZoomSwitchSupport();

    public boolean isZoomSlideSwitchSupport();

    public boolean isZoomSwitchSupportCameraId();
    public boolean isZoomSwitchMode();
    //*/

    //*/ hct.huangfei, 20201206.hdr view int topbar.
    public HdrManager getHdrManager();
    //*/
    
    /* hct.wangsenhao, for camera switch @{ */
    public interface TripleSwitchListener {
        void onConfigUIVisibility(int visibility);
    }
    public void setTripleSwitchListener(TripleSwitchListener listener);
    /* }@ hct.wangsenhao */
	//add by huangfei for water mark start
	void saveWaterMark(String filePath, long dateTaken,ExifInterface exif);
	//add by huangfei for water mark end
    //add by huangfei for watermark start
    public boolean isWaterMarkOn();
    //add by huangfei for watermark end

    //add by huangfei for wide angle start
    public boolean isWideAngleDistortionSupport();
    //add by huangfei for wide angle end 

    ZoomSliderUICtrl getZoomSliderUICtrl();

    public void setZoomConfig(IZoomConfig object);

    public IZoomConfig getZoomConfig();

    //add by huangfei by camera switcher for custom start
    public boolean getContentViewValue();
    public void setContentViewValue(boolean showContentView);
    //add by huangfei by camera switcher for custom end
	
    //add by huangfei for thumbnail update abnormal start
    public boolean isVideoRecording();

    public void updateVideoState(boolean recording);
    //add by huangfei for thumbnail update abnormal end 

    //bv wuyonglin add for bug3677 20210204 start
    public boolean getVideoStateIsPreview();

    public void updateVideoState(VideoState state);
    //bv wuyonglin add for bug3677 20210204 end
	
    public interface ZoomViewListener {
        void onConfigZoomUIVisibility(int visibility);
    }
    public void setZoomViewListener(ZoomViewListener listener) ;

    public void applyZoomViewVisibilityImmediately(int visibility);

    void setIsSelfTimerTextViewShow(boolean isShow);

    boolean getIsSelfTimerTextViewShow();

    //bv wuyonglin add for startVideoRecord not show videoquality quickSwitch 20191225 start
    public interface VideoQualitySwitcherListener {
        void onConfigVideoQualityUIVisibility(int visibility);
    }
    public void setVideoQualitySwitcherListener(VideoQualitySwitcherListener listener);
    //bv wuyonglin add for startVideoRecord not show videoquality quickSwitch 20191225 end

    //bv wuyonglin add for adjust custom FaceMode view position 20200303 start
    void setIsCustomBeautyViewShow(boolean isShow);

    boolean getIsCustomBeautyViewShow();
    //bv wuyonglin add for adjust custom FaceMode view position 20200303 end
    // add by liangchangwei for shutter progressbar
    void ShowShutterProgressBar();

    void CameraSwitheronConfigUIEnabled(boolean enabled);

    void HideShutterProgressBar();
    // add by liangchangwei for hdr pictureprocessing
    void setPictureProcessing(boolean isShow);

    boolean isHdrPictureProcessing();

    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
    TextureView getPreviewTextureView();

    TextureViewController getPreviewController();
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end

    public interface BokehViewListener {
        void onConfigBokehUIVisibility(int visibility);
    }
    public void setBokehViewListener(BokehViewListener listener) ;

    public void applyBokehViewVisibilityImmediately(int visibility);

    public interface FaceBeautyViewListener {
        void onConfigFaceBeautyUIVisibility(int visibility);
    }
    public void setFaceBeautyViewListener(FaceBeautyViewListener listener) ;

    public void applyFaceBeautyViewVisibilityImmediately(int visibility);

    //bv wuyonglin add for setting ui 20200923 start
    public interface RestoreSettingListener {
        void restoreSettingtoValue();
    }
    public void setRestoreSettingListener(RestoreSettingListener listener);

    public void removeRestoreSettingListener(RestoreSettingListener listener);
    //bv wuyonglin add for setting ui 20200923 end

    //bv wuyonglin add for adjust third app open camera ui 20200930 start
    public View getExitView();
    //bv wuyonglin add for adjust third app open camera ui 20200930 end

    //bv wuyonglin add for bug2771 20201031 start
    public boolean is4KVideo();
    public void updateIs4KVideo(boolean is4KVideo);
    //bv wuyonglin add for bug2771 20201031 end
}
