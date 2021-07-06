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

package com.mediatek.camera.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.KeyEvent;
import android.view.MotionEvent;

import android.view.NextGestureDetector;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.camera.CameraActivity;
import com.mediatek.camera.Config;
import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener.ISurfaceStatusListener;
import com.mediatek.camera.common.IAppUiListener.OnGestureListener;
import com.mediatek.camera.common.IAppUiListener.OnModeChangeListener;
import com.mediatek.camera.common.IAppUiListener.OnPreviewAreaChangedListener;
import com.mediatek.camera.common.IAppUiListener.OnPreviewTouchedListener;
import com.mediatek.camera.common.IAppUiListener.OnShutterButtonListener;
import com.mediatek.camera.common.IAppUiListener.OnThumbnailClickedListener;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.mode.IReviewUI;
import com.mediatek.camera.common.mode.photo.intent.IIntentPhotoUi;
import com.mediatek.camera.common.mode.video.videoui.IVideoUI;
import com.mediatek.camera.common.setting.ICameraSettingView;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.widget.PreviewFrameLayout;
import com.mediatek.camera.common.widget.RotateLayout;
import com.mediatek.camera.gesture.GestureManager;
import com.mediatek.camera.ui.modepicker.ModePickerManager;
import com.mediatek.camera.ui.modepicker.ModeProvider;
import com.mediatek.camera.ui.photo.IntentPhotoUi;
import com.mediatek.camera.ui.preview.PreviewManager;
import com.mediatek.camera.ui.shutter.ShutterButtonManager;
import com.mediatek.camera.ui.video.VideoUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
//*/ hct.huangfei, 20201026. add storagepath.
import com.mediatek.camera.common.storage.SDCardFileUtils;
//*/

//*/ hct.huangfei, 20201028. add gridlines.
import com.mediatek.camera.feature.setting.gridlines.GridlinesMonitor;
//*/
//add by huangfei for water mark start
import com.mediatek.camera.common.exif.ExifInterface;
import com.mediatek.camera.WaterMarkUtil;
import android.content.Intent;
//add by huangfei for water mark end

//*/ hct.huangfei, 20201030. add customize zoom.
import com.mediatek.camera.common.mode.photo.view.IZoomSliderUI;
import com.mediatek.camera.ui.photo.ZoomSliderUICtrl;
import com.mediatek.camera.feature.setting.zoom.IZoomConfig;
//*/
//bv wuyonglin add for adjust all icon position 20200612 start
import android.util.DisplayMetrics;
import android.view.Display;
import android.content.Context;
//bv wuyonglin add for adjust all icon position 20200612 end
//bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
import android.view.TextureView;
import com.mediatek.camera.ui.preview.TextureViewController;
import android.widget.ImageView;
//bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end
import com.mediatek.camera.ui.shutter.ShutterRootLayout;
//bv wuyonglin add for setting ui 20200923 start
import android.graphics.Color;
import com.mediatek.camera.common.relation.DataStore;
import java.util.Set;
import java.util.Iterator;
//bv wuyonglin add for setting ui 20200923 end
//bv wuyonglin add for bug3677 20210204 start
import com.mediatek.camera.common.mode.video.VideoMode.VideoState;
import android.util.Log;
//bv wuyonglin add for bug3677 20210204 end

/**
 * CameraAppUI centralizes control of views shared across modules. Whereas module
 * specific views will be handled in each Module UI. For example, we can now
 * bring the flash animation and capture animation up from each module to app
 * level, as these animations are largely the same for all modules.
 *
 * This class also serves to disambiguate touch events. It recognizes all the
 * swipe gestures that happen on the preview by attaching a touch listener to
 * a full-screen view on top of preview TextureView. Since CameraAppUI has knowledge
 * of how swipe from each direction should be handled, it can then redirect these
 * events to appropriate recipient views.
 */
public class CameraAppUI implements IAppUi {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(CameraAppUI.class.getSimpleName());
    private final IApp mApp;

    private GestureManager mGestureManager;
    private ShutterButtonManager mShutterManager;
    private ThumbnailViewManager mThumbnailViewManager;
    private PreviewManager mPreviewManager;
    private ModePickerManager mModePickerManager;
    private QuickSwitcherManager mQuickSwitcherManager;
    private IndicatorViewManager mIndicatorViewManager;
    private SettingFragment mSettingFragment;
    /* add  by liangchangwei for Detai Fragment View begin -- */
    private DetailFragment mDetailFragment;
    /* add  by liangchangwei for Detai Fragment View end -- */
    private EffectViewManager mEffectViewManager;
    private OnScreenHintManager mOnScreenHintManager;
    private AnimationManager mAnimationManager;

    private final List<IViewManager> mViewManagers;

    private OnModeChangeListener mModeChangeListener;

    private ViewGroup mSavingDialog;

    private String mCurrentModeName;
    private String mCurrentCameraId = "0";
    private String mCurrentModeType;
    private String mCurrentMode;

    private ModeProvider mModeProvider;
    private Handler mConfigUIHandler = new ConfigUIHandler();
    private static final int APPLY_ALL_UI_VISIBILITY = 0;
    private static final int APPLY_ALL_UI_ENABLED = 1;
    private static final int SET_UI_VISIBILITY = 2;
    private static final int SET_UI_ENABLED = 3;
	//bv liangchangwei add for shutter prpgressbar
    private static final int SHOW_SHUTTER_PROGRESS_BAR = 5;
    private static final int HIDE_SHUTTER_PROGRESS_BAR = 6;
    private static final int HIDE_SCREEN_TOAST_VIEW = 7;
    private static final int SHOW_SCREEN_TOAST_VIEW = 8;
    private static final int DISABLE_THUMBNAIL_CLICK = 9;
    private static final int ENABLE_THUMBNAIL_CLICK = 10;
    //end, wangsenhao, under water camera, 2019.12.05
    //add by bv liangchangwei for hide slidingarcview
    private long mKeyUpime = 0;

    private final OnOrientationChangeListenerImpl mOrientationChangeListener;
    //add by Jerry
	//bv liangchangwei for HDR
    private boolean mHdrPictureProcessing = false;
    private boolean mIsCameraSwitch = false;
    private VideoUI mVideoUI;
    private boolean mIsSettingShow = false;
    List<IAppUi.ModeItem> mSupportitems = new ArrayList<>();
    //add by huangfei for notch screen layout adjust start
    private CameraActivity mCameraActivity;
    private int mCameraSwitchHeight =0;
    //add by huangfei for notch screen layout adjust end
    /* hct.wangsenhao, underwater camera @{ */
    private String mNormalType;
    ViewGroup mUnderWaterView;
    private int mTipShowTime = 0;
    private CameraSwitchListener mCameraSwitchListener;
    private static final int HIDE_UNDER_WATER_VIEW = 4;
    /* }@ hct.wangsenhao */
    //add by huangfei for shutter title change start
    private ViewGroup parentView;
    //add by huangfei for shutter title change end
    private boolean mShowContentView;
    //add by huangfei for mode sort by custome start
    private int[] modePriority;
    private int mOrignalModeIndex = 1;
    List<ModeItem> sortItems; 
    //add by huangfei for mode sort by custome end
    //add by huangfei for getCsState start
    private boolean mContinuousshoting;
    //add by huangfei for getCsState end
    //add by huangfei for continuousshot abnormal start
    private boolean mIsCaptureing = false;
    //add by huangfei for continuousshot abnormal end
    //add by huangfei for more mode start
    private int[] moreModeList;
    //add by huangfei for more mode end

    //*/ hct.huangfei, 20201028. add gridlines.
    private GridlinesMonitor mMonitor;
    //*/

    //*/ hct.huangfei, 20201030. add customize zoom.
    private ZoomSliderUICtrl mZoomSliderUICtrl;
    //*/

    private VideoQualitySwitcherListener mVideoQualitySwitcheListener;    //bv wuyonglin add for startVideoRecord not show videoquality quickSwitch 20191225
	
    private ZoomViewListener mZoomViewListener;
    private boolean mIsSelfTimerTextViewShow = false;
    //bv wuyonglin add for adjust custom FaceMode view position 20200303 start
    private boolean mIsCustomBeautyViewShow = false;
    //bv wuyonglin add for adjust custom FaceMode view position 20200303 end
    //bv wuyonglin add for adjust all icon position 20200612 start
    private int mScreenHeight = 0;
    //bv wuyonglin add for adjust all icon position 20200612 end
	//bv liangchangwei for HDR

    private IAppUi.HintInfo mTopScreenToast;
    private BokehViewListener mBokehViewListener;
    private FaceBeautyViewListener mFaceBeautyViewListener;
    //*/ hct.huangfei, 20201030. add zoom switch.
    private float mPeviousSpan;
    private float mBasicZoomRatio;
    //*/

    //*/ hct.huangfei, 20201130. enable navigationbar.
    private boolean isEdgeToEdgeEnabled;
    //*/

    //*/ hct.huangfei, 20201130.camera switch by swipe up and down on screen.
    private boolean mSupportUpDownSwitchCamera = false;
    private float mDownX = 0;
    private float mDownY= 0;
    private float mUpX = 0;
    private float mUpY= 0;
    private boolean mSinglePoint = true;
    //*/

    //*/ hct.huangfei, 20201206.hdr view int topbar.
    private HdrManager mHdrManager;
    private static final int HDR_ITEM_PRIORITY = 85;
    //*/
    
    /* hct.wangsenhao, for camera switch @{ */
    private TripleSwitchListener mTripleSwitchListener;
    /* }@ hct.wangsenhao */
    //add by huangfeifor front tripleswitchhorizontal start
    private IZoomConfig mZoomConfig = null;
    //add by huangfeifor front tripleswitchhorizontal end

    //add by huangfei for wide angle start
    private String wideAngleId ;
    private boolean wideAngleDistortion = false;
    //add by huangfei for wide angle end

    //add by huangfei for thumbnail update abnormal start
    private boolean mRecording = false;
    //add by huangfei for thumbnail update abnormal end
    //bv wuyonglin add for setting ui 20200923 start
    private List<RestoreSettingListener> mRestoreSettingListener = new ArrayList<>();
    //bv wuyonglin add for setting ui 20200923 end

    //bv wuyonglin add for adjust third app open camera ui 20200930 start
    private View exit;
    private boolean isShowExit = false;
    //bv wuyonglin add for adjust third app open camera ui 20200930 end
    //bv wuyonglin add for bug2771 20201031 start
    private boolean m4KVideo = false;
    //bv wuyonglin add for bug2771 20201031 end
    //bv wuyonglin add for bug3080 20201130 start
    private boolean mLastNightMode = false;
    //bv wuyonglin add for bug3080 20201130 end

    private Boolean isNotchSupport;

    /**
     * Constructor of cameraAppUi.
     * @param app The {@link IApp} implementer.
     */
    public CameraAppUI(IApp app) {
        mApp = app;
        mOrientationChangeListener = new OnOrientationChangeListenerImpl();
        mViewManagers = new ArrayList<>();
        //add by huangfei for notch screen layout adjust start
        mCameraActivity = (CameraActivity) mApp.getActivity() ;
        //add by huangfei for notch screen layout adjust end
        /* hct.wangsenhao, underwater camera @{ */
        mNormalType = mCameraActivity.getResources().getString(R.string.normal_mode_title);
        /* }@ hct.wangsenhao */

        //*/ hct.huangfei, 20201130.camera switch by swipe up and down on screen.
        mSupportUpDownSwitchCamera = mCameraActivity.getResources().getBoolean(R.bool.config_is_up_down_switch_camera_support);
        //*/

        //add by huangfei for mode sort by custome start
        //bv wuyonglin add for a100 more mode 20210331 start
        if (CameraUtil.getDeviceModel().equals("BL5000")) {
            modePriority = mCameraActivity.getResources().getIntArray(R.array.mode_item_priority_bl5000);
        } else {
            modePriority = mCameraActivity.getResources().getIntArray(R.array.mode_item_priority);
        }
        //bv wuyonglin add for a100 more mode 20210331 end
        //add by huangfei for mode sort by custome end
        //add by huangfei for more mode start
        moreModeList = Config.getMoreModeList(mCameraActivity);
        //add by huangfei for more mode end
        //add by huangfei for underwater camera start
        mTipShowTime = mCameraActivity.getResources().getInteger(R.integer.underwater_tips_show_time);
        //add by huangfei for underwater camera end

        //add by huangfei for wide angle start
        wideAngleId = SystemProperties.get("ro.hct_wide_angle_id","-1");
        wideAngleDistortion = Config.wideAngleDistortionSupport();
        //add by huangfei for wide angle end	

        //bv wuyonglin add for adjust all icon position 20200612 start
        WindowManager wm = (WindowManager) mCameraActivity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        mScreenHeight = dm.heightPixels;
        //bv wuyonglin add for adjust all icon position 20200612 end
    }
    /**
     * Called when activity's onCreate() is invoked.
     */
    public void onCreate() {

        //*/ hct.huangfei, 20201028. add gridlines.
        if(Config.isGridlinesSupport(mCameraActivity)){
            mMonitor = new GridlinesMonitor();
        }
        //*/

        //HCT:ouyang DisplayCutout begin
        Window window = mApp.getActivity().getWindow();
        DisplayInfo mInfo = new DisplayInfo();
        mApp.getActivity().getDisplay().getDisplayInfo(mInfo);
        isNotchSupport = false;
        if (mInfo.displayCutout != null){
            isNotchSupport = !mInfo.displayCutout.isEmpty();
        }
        if (isNotchSupport) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }else{
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        //HCT:ouyang end

        ViewGroup rootView = (ViewGroup) mApp.getActivity()
                .findViewById(R.id.app_ui_root);

        parentView = (ViewGroup) mApp.getActivity().getLayoutInflater()
                .inflate(R.layout.camera_ui_root, rootView, true);
        View appUI = parentView.findViewById(R.id.camera_ui_root);
        //modify by huangfei navigationBar hide start
        /*if (CameraUtil.isHasNavigationBar(mApp.getActivity())) {
            //get navigation bar height.
            int navigationBarHeight = CameraUtil.getNavigationBarHeight(mApp.getActivity());
            //set root view bottom margin to let the UI above the navigation bar.
            /*
            FrameLayout.LayoutParams params =
                    (FrameLayout.LayoutParams) appUI.getLayoutParams();
            if (CameraUtil.isTablet()) {
                int displayRotation = CameraUtil.getDisplayRotation(mApp.getActivity());
               LogHelper.d(TAG, " onCreate displayRotation  " + displayRotation);
                if (displayRotation == 90 || displayRotation == 270) {
                    params.leftMargin += navigationBarHeight;
                    appUI.setLayoutParams(params);
                } else {
                    params.bottomMargin += navigationBarHeight;
                    appUI.setLayoutParams(params);
                }
            } else {
                params.bottomMargin += navigationBarHeight;
                appUI.setLayoutParams(params);
            }//
            View shutterRootLayout = parentView.findViewById(R.id.shutter_root);
            View shutterView = parentView.findViewById(R.id.shutter_view_root);
            View effect = parentView.findViewById(R.id.effect);
            View thumbnail = parentView.findViewById(R.id.thumbnail);

            RelativeLayout.LayoutParams shutterRootLayoutParams = (RelativeLayout.LayoutParams) shutterRootLayout.getLayoutParams();
            shutterRootLayoutParams.bottomMargin += navigationBarHeight;
            shutterRootLayout.setLayoutParams(shutterRootLayoutParams);

            RelativeLayout.LayoutParams shutterViewLayoutParams = (RelativeLayout.LayoutParams) shutterView.getLayoutParams();
            shutterViewLayoutParams.bottomMargin += navigationBarHeight;
            shutterView.setLayoutParams(shutterViewLayoutParams);

            RelativeLayout.LayoutParams effectLayoutParams = (RelativeLayout.LayoutParams) effect.getLayoutParams();
            effectLayoutParams.bottomMargin += navigationBarHeight;
            effect.setLayoutParams(effectLayoutParams);

            RelativeLayout.LayoutParams thumbnailLayoutParams = (RelativeLayout.LayoutParams) thumbnail.getLayoutParams();
            thumbnailLayoutParams.bottomMargin += navigationBarHeight;
            thumbnail.setLayoutParams(thumbnailLayoutParams);
        }*/
        int navigationBarHeight = CameraUtil.getNavigationBarHeight(mApp.getActivity());

        //modify by huangfei for gestural navigationbar start
        //if(navigationBarHeight==-1){
        if(navigationBarHeight==-1||CameraUtil.isEdgeToEdgeEnabled(mApp.getActivity())){
        //modify by huangfei for gestural navigationbar end
            navigationBarHeight = mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.navigationbar_hide_bottom_margin);
        }
        View shutterRootLayout = parentView.findViewById(R.id.shutter_root);
        View shutterView = parentView.findViewById(R.id.shutter_view_root);
        View effect = parentView.findViewById(R.id.effect);
        View thumbnail = parentView.findViewById(R.id.thumbnail);
        //bv wuyonglin add for adjust third app open camera ui 20200930 start
        exit = parentView.findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mApp.getActivity().finish();
            }
        });
        //bv wuyonglin add for adjust third app open camera ui 20200930 end

		//bv liangchangwei for HDR
        mTopScreenToast = new IAppUi.HintInfo();
        int id = mApp.getActivity().getResources().getIdentifier("hint_text_background",
                "drawable", mApp.getActivity().getPackageName());
        mTopScreenToast.mBackground = mApp.getActivity().getDrawable(id);
        mTopScreenToast.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
        mTopScreenToast.mDelayTime = 2000;
        mTopScreenToast.mHintText = mApp.getActivity().getString(R.string.optimize_picture);

        RelativeLayout.LayoutParams shutterRootLayoutParams = (RelativeLayout.LayoutParams) shutterRootLayout.getLayoutParams();
        shutterRootLayoutParams.bottomMargin += navigationBarHeight;
        shutterRootLayout.setLayoutParams(shutterRootLayoutParams);
        RelativeLayout.LayoutParams shutterViewLayoutParams = (RelativeLayout.LayoutParams) shutterView.getLayoutParams();
        //bv wuyonglin add for adjust all icon position 20200612 start
        //shutterViewLayoutParams.bottomMargin += navigationBarHeight;
        if (mScreenHeight == 1560) {
            shutterViewLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_shutter_margin_Bottom_1560px);
            shutterView.setLayoutParams(shutterViewLayoutParams);
            //bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
        } else if (mScreenHeight == 1440) {
            shutterViewLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_shutter_margin_Bottom_1440px);
            shutterView.setLayoutParams(shutterViewLayoutParams);
            //bv wuyonglin add for screen 1440px adjust all icon position 20200709 end
        } else if (mScreenHeight == 2300) {
            shutterViewLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_shutter_margin_Bottom_2300px);
            shutterView.setLayoutParams(shutterViewLayoutParams);
        } else if (mScreenHeight == 2400) {
            shutterViewLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_shutter_margin_Bottom_2400px);
            shutterView.setLayoutParams(shutterViewLayoutParams);
        }
        //bv wuyonglin add for adjust all icon position 20200612 end

        RelativeLayout.LayoutParams effectLayoutParams = (RelativeLayout.LayoutParams) effect.getLayoutParams();
        effectLayoutParams.bottomMargin += navigationBarHeight;
        effect.setLayoutParams(effectLayoutParams);
        RelativeLayout.LayoutParams thumbnailLayoutParams = (RelativeLayout.LayoutParams) thumbnail.getLayoutParams();
        //bv wuyonglin add for adjust third app open camera ui 20200930 start
        RelativeLayout.LayoutParams exitLayoutParams = (RelativeLayout.LayoutParams) exit.getLayoutParams();
        //bv wuyonglin add for adjust all icon position 20200612 start
        //thumbnailLayoutParams.bottomMargin += navigationBarHeight;
        if (mScreenHeight == 1560) {
            thumbnailLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_switcher_margin_bottom_1560px);
            thumbnail.setLayoutParams(thumbnailLayoutParams);
            exitLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_switcher_margin_bottom_1560px);
            exit.setLayoutParams(exitLayoutParams);
            //bv wuyonglin add for screen 1440px adjust all icon position 20200709 start
        } else if (mScreenHeight == 1440) {
            thumbnailLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_switcher_margin_bottom_1440px);
            thumbnail.setLayoutParams(thumbnailLayoutParams);
            exitLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_switcher_margin_bottom_1440px);
            exit.setLayoutParams(exitLayoutParams);
            //bv wuyonglin add for screen 1440px adjust all icon position 20200709 end
        } else if (mScreenHeight == 2300) {
            thumbnailLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_switcher_margin_bottom_2300px);
            thumbnail.setLayoutParams(thumbnailLayoutParams);
            exitLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_switcher_margin_bottom_2300px);
            exit.setLayoutParams(exitLayoutParams);
        } else if (mScreenHeight == 2400) {
            thumbnailLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_switcher_margin_bottom_2400px);
            thumbnail.setLayoutParams(thumbnailLayoutParams);
            exitLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_switcher_margin_bottom_2400px);
            exit.setLayoutParams(exitLayoutParams);
        }
        //bv wuyonglin add for adjust third app open camera ui 20200930 end

        //bv wuyonglin add for adjust all icon position 20200612 end
        //modify by huangfei navigationBar hide end
         //add by huangfei for notch screen layout adjust start
         FrameLayout.LayoutParams params =
                 (FrameLayout.LayoutParams) appUI.getLayoutParams();
         //if(isNotchSupport){
             if (mScreenHeight == 2300) {
                params.topMargin += mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_ui_root_margin_top_2300px);
             } else {
                params.topMargin += mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_ui_root_margin_top_2400px);
             }
             appUI.setLayoutParams(params);
         //}
         mCameraSwitchHeight = params.height; 
        mModeProvider = new ModeProvider(mApp.getActivity());
        String action = mApp.getActivity().getIntent().getAction();
        mGestureManager = new GestureManager(mApp.getActivity());
        mAnimationManager = new AnimationManager(mApp, this);

        mShutterManager = new ShutterButtonManager(mApp, parentView);
        mShutterManager.setVisibility(View.VISIBLE);
        mShutterManager.setOnShutterChangedListener(new OnShutterChangeListenerImpl());
        mViewManagers.add(mShutterManager);

       //bv wuyonglin add for all area fling to change mode start
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //bv wuyonglin add for optimize touch blank area quick switcher option can hide 2020025 start
                if (event.getAction() == MotionEvent.ACTION_UP) {
		            ImageView beautyExpand = (ImageView) getModeRootView().findViewById(R.id.aiworks_beauty_expand_button);
	                if (getShutterRootView() != null && mShutterManager.getIsCanSwitchMode()) {
                        ((ShutterRootLayout) getShutterRootView()).enableOnScroll(true);
                    }
                    if("AiworksFaceBeauty".equals(getCurrentMode()) && beautyExpand != null && beautyExpand.getVisibility() == View.GONE){
			            getModeRootView().findViewById(R.id.century_facebeauty_onekey).setVisibility(View.GONE);
			            getModeRootView().findViewById(R.id.century_facebeauty_custom).setVisibility(View.GONE);
			            getModeRootView().findViewById(R.id.aiworks_facebeauty_custom_btn).setVisibility(View.GONE);
			            getModeRootView().findViewById(R.id.aiworks_facebeauty_onekey_btn).setVisibility(View.GONE);
			            setUIVisibility(IAppUi.SHUTTER_ROOTLAYOUT, View.VISIBLE);
			            if (mIsCustomBeautyViewShow) {
			                beautyExpand.setImageResource(R.drawable.aiworks_beatuty_custom_expand);
			            } else {
			                beautyExpand.setImageResource(R.drawable.aiworks_beatuty_onekey_expand);
			            }
			            beautyExpand.setVisibility(View.VISIBLE);
			            mGestureManager.getGestureRecognizer().onTouchEvent(event);	//bv wuyonglin add for at FaceBeauty mode click preview view to focus will go to AE/AF locked 20200910
			            return true;
	                } else {
                        hideQuickSwitcherOption();
                        //bv wuyonglin add for optimize touch blank area quick switcher option can hide 2020025 end
		            }
                }
	            if (event.getAction() == MotionEvent.ACTION_MOVE && "AiworksFaceBeauty".equals(getCurrentMode()) && getModeRootView().findViewById(R.id.aiworks_beauty_expand_button) != null && getModeRootView().findViewById(R.id.aiworks_beauty_expand_button).getVisibility() != View.VISIBLE && mIsCustomBeautyViewShow) {
			        return true;
	            }
                mGestureManager.getGestureRecognizer().onTouchEvent(event);
                if(mZoomSliderUICtrl.isShowAll()){
                    mZoomSliderUICtrl.getSlidingArcView().onTouchEvent(event);
                    //add by bv liangchangwei for hide slidingarcview
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        mKeyUpime = System.currentTimeMillis();
                    }else if(event.getAction() == MotionEvent.ACTION_UP){
                        if((System.currentTimeMillis() - mKeyUpime) < 100){
                            LogHelper.d(TAG,"need slidingArcViewHide");
                            getZoomSliderUICtrl().slidingArcViewHide();
                        }
                    }
                    //add by bv liangchangwei for hide slidingarcview
                }
                return true;
            }
        });
       //bv wuyonglin add for all area fling to change mode end
        if (!(MediaStore.ACTION_IMAGE_CAPTURE.equals(action)
                || MediaStore.ACTION_VIDEO_CAPTURE.equals(action))) {
            mThumbnailViewManager = new ThumbnailViewManager(mApp, parentView);
            mViewManagers.add(mThumbnailViewManager);
            mThumbnailViewManager.setVisibility(View.VISIBLE);
            isShowExit = false;
        //bv wuyonglin add for adjust third app open camera ui 20200930 start
        } else {
            exit.setVisibility(View.VISIBLE);
            isShowExit = true;
        //bv wuyonglin add for adjust third app open camera ui 20200930 end
        }

        mPreviewManager = new PreviewManager(mApp);
        //Set gesture listener to receive touch event.
        mPreviewManager.setOnTouchListener(new OnTouchListenerImpl());

        //*/ hct.huangfei, 20201206.hdr view int topbar.
        if(Config.isHdrInTopBarSupprot(mApp.getActivity()) && !mCameraActivity.isThirdPartyIntent(mApp.getActivity())
                && !mCameraActivity.isSecureCameraIntent( mApp.getActivity())){
            mHdrManager = new HdrManager(mApp, parentView);
            mViewManagers.add(mHdrManager);
        }
        //*/
        mModePickerManager = new ModePickerManager(mApp, parentView);
        mModePickerManager.setSettingClickedListener(new OnSettingClickedListenerImpl());
        /* add  by liangchangwei for Detai Fragment View begin -- */
        mModePickerManager.setDetailClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogHelper.i(TAG,"showDetail ");
                showDetail();
            }
        });
        /* add  by liangchangwei for Detai Fragment View end -- */
        mModePickerManager.setModeChangeListener(new OnModeChangedListenerImpl());
	//bv wuyonglin delete for setting icon not show with quick switcher icon 20200220 start
        //mModePickerManager.setVisibility(View.VISIBLE);
	//bv wuyonglin delete for setting icon not show with quick switcher icon 20200220 end
        mViewManagers.add(mModePickerManager);

        mQuickSwitcherManager = new QuickSwitcherManager(mApp, parentView);
        mQuickSwitcherManager.setVisibility(View.VISIBLE);
        mQuickSwitcherManager.setModeChangeListener(new OnQuickModeChangedListenerImpl());
        mViewManagers.add(mQuickSwitcherManager);
        
        //start, wangsenhao ,hide some button on underwater mode, 2020.04.22
        if(isUnderWaterSupport() == 1){
            mModePickerManager.setVisibility(View.GONE);
            mQuickSwitcherManager.setVisibility(View.GONE);
        }
        //end, wangsenhao ,hide some button on underwater mode, 2020.04.22

       //bv wuyonglin add for all area fling to change mode start
        mQuickSwitcherManager.getView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureManager.getGestureRecognizer().onTouchEvent(event);
                return true;
            }
        });
       //bv wuyonglin add for all area fling to change mode end

        mIndicatorViewManager = new IndicatorViewManager(mApp, parentView);
        mIndicatorViewManager.setVisibility(View.VISIBLE);
        mViewManagers.add(mIndicatorViewManager);

        mSettingFragment = new SettingFragment();
        mSettingFragment.setStateListener(new SettingStateListener());

        /* add  by liangchangwei for Detai Fragment View begin -- */
        mDetailFragment = new DetailFragment();
        mDetailFragment.setStateListener(new SettingStateListener());
        /* add  by liangchangwei for Detai Fragment View end -- */
        layoutSettingUI();

        mEffectViewManager = new EffectViewManager(mApp, parentView);
        mEffectViewManager.setVisibility(View.VISIBLE);
        mViewManagers.add(mEffectViewManager);

        mOnScreenHintManager = new OnScreenHintManager(mApp, parentView);
        //call each manager's onCreate()
        for (int count = 0; count < mViewManagers.size(); count ++) {
            mViewManagers.get(count).onCreate();
        }
        mApp.registerOnOrientationChangeListener(mOrientationChangeListener);
        //add by Jerry
        mVideoUI = new VideoUI(mApp, getModeRootView());
        //*/ hct.huangfei, 20201026. add storagepath.
        SDCardFileUtils.getInstance(mApp.getActivity());
        //*/
        /* hct.wangsenhao, underwater camera @{ */
        if(CameraUtil.getUnderWaterSupport(mCameraActivity)==1){
            mUnderWaterView = (ViewGroup) mApp.getActivity().findViewById(R.id.under_water_view);
            //mUnderWaterView = (ViewGroup)parentView.findViewById(R.id.under_water_view);
            mUnderWaterView.setVisibility(View.VISIBLE);
            mConfigUIHandler.sendEmptyMessageDelayed(HIDE_UNDER_WATER_VIEW, mTipShowTime);
        }
        /* }@ hct.wangsenhao */

        //*/ hct.huangfei, 20201030. add customize zoom.
        if(CameraUtil.isZoomViewCustomizeSupport(mCameraActivity)){
            mZoomSliderUICtrl = new ZoomSliderUICtrl(mApp, getModeRootView());
        }
        //*/

        // [Add for CCT tool] Receive keycode and enable/disable ZSD @{
        mApp.registerKeyEventListener(getKeyEventListener(), IApp.DEFAULT_PRIORITY - 1);
        // @}
    }

    /**
     * Called when activity's onResume() is invoked.
     */
    public void onResume() {
        RotateLayout root = (RotateLayout) mApp.getActivity().findViewById(R.id.app_ui);
        Configuration newConfig = mApp.getActivity().getResources().getConfiguration();
        hideAlertDialog();
        LogHelper.d(TAG, "onResume orientation = " + newConfig.orientation);
        if (root != null) {
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                root.setOrientation(0, false);
            } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                root.setOrientation(90, false);
            }
        }
        //call each manager's onResume()
        for (int count = 0; count < mViewManagers.size(); count ++) {
            mViewManagers.get(count).onResume();
        }
    }

    /**
     * Called when activity's onPause() is invoked.
     */
    public void onPause() {
        //call each manager's onPause()
        for (int count = 0; count < mViewManagers.size(); count ++) {
            mViewManagers.get(count).onPause();
        }
        hideAlertDialog();
        hideSetting();
        hideDetail();
        mPreviewManager.onPause();
    }

    /**
     * Called when activity's onDestroy() is invoked.
     */
    public void onDestroy() {
        //call each manager's onDestroy()
        for (int count = 0; count < mViewManagers.size(); count ++) {
            mViewManagers.get(count).onDestroy();
        }
        mApp.unregisterOnOrientationChangeListener(mOrientationChangeListener);
        // [Add for CCT tool] Receive keycode and enable/disable ZSD @{
        mApp.unRegisterKeyEventListener(getKeyEventListener());
        // @}
    }

    /**
     * Called by the system when the device configuration changes while your
     * activity is running.
     * @param newConfig The new device configuration.
     */
    public void onConfigurationChanged(Configuration newConfig) {
	//bv wuyonglin add for bug3080 20201130 start
	if (mIsSettingShow) {
        /*add by liangchangwei for fixbug 7040 start */
        mSettingFragment.initToolBar();
        refreshSettingView();
        /*add by liangchangwei for fixbug 7040 end */

        if (((newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES) != mLastNightMode) {
              mLastNightMode = ((newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES);
        }
        mApp.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mApp.getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        if(!getShutterButtonManager().getIsShowMoreModeView()){
            mApp.getActivity().getWindow().setNavigationBarColor(mApp.getActivity().getResources().getColor(android.R.color.white));
            if (!mLastNightMode) {
                //bv wuyonglin add for bug5081 20210406 start
                mApp.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                mApp.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                //bv wuyonglin add for bug5081 20210406 end
                mApp.getActivity().getWindow().setStatusBarColor(Color.WHITE);
                mApp.getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
        }
	}
	//bv wuyonglin add for bug3080 20201130 end
    }

    /**
     * Update thumbnailView, when the bitmap finished update, is will be recycled
     * immediately, do not use the bitmap again!
     * @param bitmap
     *            the bitmap matched with the picture or video, such as
     *            orientation, content. suggest thumbnail view size.
     */
    public void updateThumbnail(final Bitmap bitmap) {
        if (mThumbnailViewManager != null) {
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mThumbnailViewManager.updateThumbnail(bitmap);
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
            });
        }
    }

    /**
     * get the width of thumbnail view.
     * @return the min value of width and height of thumbnail view.
     */
    public int getThumbnailViewWidth() {
        if (mThumbnailViewManager != null) {
            return mThumbnailViewManager.getThumbnailViewWidth();
        } else {
            return 0;
        }
    }

    @Override
    public void registerQuickIconDone() {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //bv wuyonglin add for setting icon not show with quick switcher icon 20200220 start
		if (!getShutterButtonManager().getIsShowMoreModeView()) {
                mModePickerManager.setVisibility(View.VISIBLE);
		}
                //bv wuyonglin add for setting icon not show with quick switcher icon 20200220 end
                mQuickSwitcherManager.registerQuickIconDone();
            }
        });
    }

    @Override
    public void registerIndicatorDone() {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIndicatorViewManager.registerQuickIconDone();
            }
        });
    }

    @Override
    public void registerMode(List<ModeItem> items) {
        ModeItem item = null;
        mModeProvider.clearAllModes();
        //add by Jerry

        //modify by huangfei for mode sort by custome start
        //sortSizesIncrease(items);
        sortItems = sortModeItemsIncrease(items);
        items = sortItems;
        //modify by huangfei for mode sort by custome end
        for (int i = 0; i < items.size(); i++) {
            item = items.get(i);
            /* hct.wangsenhao, underwater camera @{ */
            if(isUnderWaterSupport()==1){
                if(!mNormalType.equals(item.mModeName)){
                    continue;
                }
            }
            /* }@ hct.wangsenhao */
            mModeProvider.registerMode(item);
            if(!mIsCameraSwitch){
                //mShutterManager.registerShutterButton(item, i);
                for(int j = 0;j < item.mSupportedCameraIds.length;j++) {
                    if(item.mSupportedCameraIds[j].equals(mCurrentCameraId)) {
                        mShutterManager.registerShutterButton(item, i);
                        break;
                    }
                }
            }
        }
        mModePickerManager.registerModeProvider(mModeProvider);

        if( !mIsCameraSwitch) {//add by Jerry
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mShutterManager.registerDone();
                }
            });
            mIsCameraSwitch = false;//add by Jerry
        }
    }
    private void sortSizesIncrease(List<ModeItem> items){
               Collections.sort(items, new Comparator<ModeItem>() {
            @Override
            public int compare(ModeItem s, ModeItem t1) {
                int poriorityP = s.mPriority;
                int poriorityN = t1.mPriority;
                if(poriorityP - poriorityN > 0) {
                    return 1;
                }else{
                    return -1;
                }
            }
        });
    }

    private List<ModeItem> sortModeItemsIncrease(List<ModeItem> items){
        List<ModeItem> sortItems = new ArrayList<ModeItem>();;
        for(int i = 0;i< modePriority.length;i++){
            for(int j =0;j< items.size();j++){
                if(removeModeBySecureCameraIntent(items.get(j).mMode)){
                    break;
                }
                if(items.get(j).mPriority==modePriority[i]){
                    sortItems.add(items.get(j));
                    break;
                }
            }
        }
        return sortItems;
    }
    /**
     * Notice: This is used for mode manager to update current mode, do not
     * use it in any other place.
     */
    @Override
    public void updateCurrentMode(final String modeEntry) {
        LogHelper.d(TAG, "updateCurrentMode mode = " + modeEntry);

        //add by huangfei for continuousshot abnormal start
        mIsCaptureing = false;
        //add by huangfei for continuousshot abnormal end

        if (mModeProvider != null) {
            ModeItem item = mModeProvider.getMode(modeEntry);
            if (item == null) {
                return;
            }
            if (item.mModeName.equals(mCurrentModeName)) {
                return;
            }
            mCurrentModeName = item.mModeName;
            mCurrentModeType = item.mType;
            mCurrentMode = item.mMode; //add by Jerry
            final String[] supportTypes;
            supportTypes = mModeProvider.getModeSupportTypes(mCurrentModeName,
                    mCurrentCameraId);
            mModePickerManager.updateCurrentModeItem(item);

            int mCurrentpriority = item.mPriority;
            int modeIndex = mOrignalModeIndex;
            for(int i = 0;i< sortItems.size();i++){
                if(sortItems.get(i).mPriority==mCurrentpriority){
                    modeIndex = i;
                }
            }
            mShutterManager.onShutterChangedStart(mCurrentModeName,mCurrentModeType,modeIndex);
        }
    }

    @Override
    public void setPreviewSize(final int width, final int height,
                               final ISurfaceStatusListener listener) {
        LogHelper.i(TAG, "setPreviewSize listener = " + listener);
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
        LogHelper.i(TAG, "setPreviewSize to updatePreviewSize listener = " + listener+" width ="+width+" height ="+height);
                mPreviewManager.updatePreviewSize(width, height, listener);
            }
        });
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key.  The default implementation simply finishes the current activity,
     * but you can override this to do whatever you want.
     * @return Return <code>true</code> to prevent this event from being propagated
     * further, or <code>false</code> to indicate that you have not handled
     * this event and it should continue to be propagated.
     */
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void showScreenHint(final HintInfo info) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOnScreenHintManager.showScreenHint(info);
            }
        });
    }

    @Override
    public void hideScreenHint(final HintInfo info) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOnScreenHintManager.hideScreenHint(info);
            }
        });

    }

    @Override
    public ViewGroup getModeRootView() {
        return (ViewGroup) mApp.getActivity()
                .findViewById(R.id.feature_root);
    }

    @Override
    public View getShutterRootView() {
        if (mShutterManager != null) {
            return mShutterManager.getShutterRootView();
        }
        return null;
    }

    @Override
    public PreviewFrameLayout getPreviewFrameLayout() {
        return mPreviewManager.getPreviewFrameLayout();
    }

    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 start
    @Override
    public TextureView getPreviewTextureView() {
        return (TextureView) mPreviewManager.getView();
    }

    @Override
    public TextureViewController getPreviewController() {
        return (TextureViewController) mPreviewManager.getPreviewController();
    }
    //bv wuyonglin add for aiworks facebeauty and bokeh 20200722 end

    @Override
    public void onPreviewStarted(final String previewCameraId) {
        LogHelper.d(TAG, "onPreviewStarted previewCameraId = " + previewCameraId);
        if (previewCameraId == null) {
            return;
        }
        synchronized (mCurrentCameraId) {
            mCurrentCameraId = previewCameraId;
        }

        mModePickerManager.onPreviewStarted(previewCameraId);
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeTopSurface();
            }
        });
    }

    @Override
    public void onCameraSelected(final String cameraId) {

        LogHelper.i(TAG,"onCameraSelected cameraId = " + cameraId);
        //add by huangfei for continuousshot abnormal start
        mIsCaptureing = false;
        //add by huangfei for continuousshot abnormal end

        synchronized (mCurrentCameraId) {
            mCurrentCameraId = cameraId;
        }

        //add by huangfei for zoom switch start
        if(CameraUtil.isZoomViewCustomizeSupport(mCameraActivity)){
            mZoomSliderUICtrl.showCircleTextView();
        }
        //add by huangfei for zoom switch end

        //add by Jerry
        if(mModeProvider != null && mShutterManager != null) {
            mIsCameraSwitch = true;
			mShutterManager.clearShutterButtonItems();
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mShutterManager.clearShutterButton();
                }
            });
            List<IAppUi.ModeItem> items = mModeProvider.getAllModes();
            ModeItem item = null;
            int count = 0;
            mSupportitems.clear();           
            for (int i = 0; i < items.size(); i++) {
                item = items.get(i);
                if(isMoreMode(item.mPriority,moreModeList)){
                    continue;
                }

                //*/ hct.huangfei, 20201206.hdr view int topbar.
                if(Config.isHdrInTopBarSupprot(mApp.getActivity()) && item.mPriority == HDR_ITEM_PRIORITY){
                    continue;
                }
                //*/

                for(int j = 0;j < item.mSupportedCameraIds.length;j++) {
                    if(item.mSupportedCameraIds[j].equals(cameraId)) {
                        mShutterManager.registerShutterButton(item, count);
                        mSupportitems.add(item);               
                        count++;
                    }
                }
            }
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mShutterManager.registerDone();
                }
            });
            if(!mSupportitems.isEmpty()&&mCurrentMode!=null) {
                for (int i = 0;i < mSupportitems.size();i++) {
                    item = mSupportitems.get(i);
                    if(mCurrentMode.equals(item.mMode)) {
                        mShutterManager.setChangIndex(i);
                        mApp.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mShutterManager.updateModeSupportType();
                            }
                        });
                    }
                }
            }
        }
        mModePickerManager.onPreviewStarted(cameraId);

        //*/ hct.huangfei, 20201206.hdr view int topbar.
        if(Config.isHdrInTopBarSupprot(mApp.getActivity()) && mHdrManager!=null){
            if("0".equals(cameraId)){
                mHdrManager.showHdrView();
            }else{
                mHdrManager.hideHdrView();
            }
        }
        //*/
    }

    @Override
    public IVideoUI getVideoUi() {
        //return new VideoUI(mApp, getModeRootView());
        return mVideoUI;
    }
    @Override
    public View.OnClickListener getVideoUiClick() {
        if(mVideoUI != null) {
            return mVideoUI.getVideoUiClick();
        }
        return null;
    }
    @Override
    public boolean isSettingShow() {
        return mIsSettingShow;
    }

    @Override
    public boolean getIsSelfTimerTextViewShow(){
        return mIsSelfTimerTextViewShow;
    }

    @Override
    public void setIsSelfTimerTextViewShow(boolean isShow){
        mIsSelfTimerTextViewShow = isShow;
    }

    //bv wuyonglin add for adjust custom FaceMode view position 20200303 start
    @Override
    public void setIsCustomBeautyViewShow(boolean isShow){
        mIsCustomBeautyViewShow = isShow;
    }

    @Override
    public boolean getIsCustomBeautyViewShow(){
        return mIsCustomBeautyViewShow;
    }
    //bv wuyonglin add for adjust custom FaceMode view position 20200303 end

    @Override
    public IReviewUI getReviewUI() {
        ViewGroup appUI = (ViewGroup) mApp.getActivity().findViewById(R.id.app_ui);
        ViewGroup reviewRoot = (ViewGroup) appUI.getChildAt(0);
        return new ReviewUI(mApp, reviewRoot);
    }

    @Override
    public IIntentPhotoUi getPhotoUi() {
        return new IntentPhotoUi(mApp.getActivity(), getModeRootView(), this);
    }

    @Override
    public void animationStart(final AnimationType type, final AnimationData data) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAnimationManager.animationStart(type, data);
            }
        });
    }

    @Override
    public void animationEnd(final AnimationType type) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAnimationManager.animationEnd(type);
            }
        });
    }

    @Override
    public void setUIVisibility(int module, int visibility) {
        if (!isMainThread()) {
            LogHelper.d(TAG, "setUIVisibility + module " + module + " visibility " + visibility);
            Message msg = Message.obtain();
            msg.arg1 = module;
            msg.arg2 = visibility;
            msg.what = SET_UI_VISIBILITY;
            mConfigUIHandler.sendMessage(msg);
            LogHelper.d(TAG, "setUIVisibility - ");
        } else {
            setUIVisibilityImmediately(module, visibility);
        }

    }
    @Override
    public void setUIEnabled(int module, boolean enabled) {
        if (!isMainThread()) {
            LogHelper.d(TAG, "setUIEnabled + module " + module + " enabled " + enabled);
            Message msg = Message.obtain();
            msg.arg1 = module;
            msg.arg2 = enabled ? 1 : 0;
            msg.what = SET_UI_ENABLED;
            mConfigUIHandler.sendMessage(msg);
            LogHelper.d(TAG, "setUIEnabled - ");
        } else {
            setUIEnabledImmediately(module, enabled);
        }
    }


    @Override
    public void applyAllUIVisibility(final int visibility) {
        //add by huangfei for thumbnail update abnormal start
        if(isVideoRecording()){
            LogHelper.i(TAG, "isVideoRecording return");
            return;
        }
        //add by huangfei for thumbnail update abnormal end

        if (visibility == View.VISIBLE && mShutterManager.getIsShowMoreModeView()) {
                return;
        }
        if (!isMainThread()) {
            LogHelper.d(TAG, "applyAllUIVisibility + visibility " + visibility);

            //add by huangfei for view abnormal when Panorama capture start
            if("Panorama".equals(getCurrentMode()) && mIsCaptureing){
                LogHelper.i(TAG,"applyAllUIVisibility Panorama return:");
                return;
            }
            //add by huangfei for view abnormal when Panorama capture end

            Message msg = Message.obtain();
            msg.arg1 = visibility;
            msg.what = APPLY_ALL_UI_VISIBILITY;
            mConfigUIHandler.sendMessage(msg);
            LogHelper.d(TAG, "applyAllUIVisibility -");
        } else {
            applyAllUIVisibilityImmediately(visibility);
        }
    }

    @Override
    public void applyAllUIEnabled(final boolean enabled) {
        if (!isMainThread()) {
            LogHelper.d(TAG, "applyAllUIEnabled + enabled " + enabled);
            Message msg = Message.obtain();
            msg.arg1 = enabled ? 1 : 0;
            msg.what = APPLY_ALL_UI_ENABLED;
            mConfigUIHandler.sendMessage(msg);
            LogHelper.d(TAG, "applyAllUIEnabled -");
        } else {
            applyAllUIEnabledImmediately(enabled);
        }
        //add by huangfei for more mode start
	if (mShutterManager.getIsCanSwitchMode()) {
        mShutterManager.moreModeTextEnabled(enabled);
	}
        //add by huangfei for more mode end
    }

    private void setUIVisibilityImmediately(int module, int visibility) {
        LogHelper.d(TAG, "setUIVisibilityImmediately + module " + module
                                                + " visibility " + visibility);
        configUIVisibility(module, visibility);
    }

    private void setUIEnabledImmediately(int module, boolean enabled) {
        LogHelper.d(TAG, "setUIEnabledImmediately + module " + module + " enabled " + enabled);
        configUIEnabled(module, enabled);
    }

    @Override
    public void updateBrightnessBackGround(boolean visible) {
        LogHelper.d(TAG, "setBackgroundColor visible = " + visible);
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                 View rootView = mApp.getActivity()
                         .findViewById(R.id.brightness_view);
                 if (visible) {
                     rootView.setVisibility(View.VISIBLE);
                 } else {
                     rootView.setVisibility(View.GONE);
                 }
            }
        });
    }

    @Override
    public void updatePanelColor(int color) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                View rootView = mApp.getActivity()
                        .findViewById(R.id.brightness_view);
                rootView.setBackgroundColor(color);
            }
        });
    }

    private void applyAllUIVisibilityImmediately(int visibility) {
        LogHelper.d(TAG, "applyAllUIVisibilityImmediately + visibility " + visibility);
        mConfigUIHandler.removeMessages(APPLY_ALL_UI_VISIBILITY);
        for (int count = 0; count < mViewManagers.size(); count++) {
            mViewManagers.get(count).setVisibility(visibility);
        }
        getPreviewFrameLayout().setVisibility(visibility);
        mOnScreenHintManager.setVisibility(visibility);
        if (visibility == View.GONE) {
            mQuickSwitcherManager.hideQuickSwitcherImmediately();
        }

        //*/ hct.huangfei, 20201130.camera switcher for custom.
        if(mCameraSwitchListener!=null){
            mCameraSwitchListener.onConfigUIVisibility(visibility);
        }
        //*/
        
        /* hct.wangsenhao ,for camera switch @{ */
        if(mTripleSwitchListener!=null /*&& !getCameraId().equals("1")*/){	//bv wuyonglin delete for bug1759 20200803
            mTripleSwitchListener.onConfigUIVisibility(visibility);
        }
        //bv wuyonglin add for from other app enter camera intent photo mode after take picture should not show zoom view 20200629 start
        if (visibility == View.VISIBLE) {
        applyZoomViewVisibilityImmediately(visibility);
        }
        //bv wuyonglin add for from other app enter camera intent photo mode after take picture should not show zoom view 20200629 end
        /* }@ hct.wangsenhao */
        applyBokehViewVisibilityImmediately(visibility);
        applyFaceBeautyViewVisibilityImmediately(visibility);
        LogHelper.d(TAG, "applyAllUIVisibilityImmediately + visibility " + visibility+" getIsInMoreModeView ="+mShutterManager.getIsInMoreModeView());
        if(isShowExit){
            exit.setVisibility(visibility);
        }
    }

    private void applyAllUIEnabledImmediately(boolean enabled) {
        LogHelper.d(TAG, "applyAllUIEnabledImmediately + enabled " + enabled);
        mConfigUIHandler.removeMessages(APPLY_ALL_UI_ENABLED);
        if (mCameraSwitchListener != null) {
            mCameraSwitchListener.onConfigUIEnabled(enabled);
        }
        for (int count = 0; count < mViewManagers.size(); count++) {
            mViewManagers.get(count).setEnabled(enabled);
        }
    }

    public void applyZoomViewVisibilityImmediately(int visibility) {
        LogHelper.d(TAG, "applyZoomViewVisibilityImmediately + visibility " + visibility+" mZoomViewListener =" +mZoomViewListener);
        if(mZoomViewListener!=null){
            mZoomViewListener.onConfigZoomUIVisibility(visibility);
        }
    }
    private boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    @Override
    public void clearPreviewStatusListener(final ISurfaceStatusListener listener) {
        LogHelper.i(TAG, "clearPreviewStatusListener listener =" +listener);
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPreviewManager.clearPreviewStatusListener(listener);
                LogHelper.i(TAG, "clearPreviewStatusListener end listener =" +listener);
            }
        });
    }

    @Override
    public void registerOnPreviewTouchedListener(OnPreviewTouchedListener listener) {

    }

    @Override
    public void unregisterOnPreviewTouchedListener(OnPreviewTouchedListener listener) {

    }

    @Override
    public void registerOnPreviewAreaChangedListener(OnPreviewAreaChangedListener listener) {
        mPreviewManager.registerPreviewAreaChangedListener(listener);
    }

    @Override
    public void unregisterOnPreviewAreaChangedListener(OnPreviewAreaChangedListener listener) {
        mPreviewManager.unregisterPreviewAreaChangedListener(listener);
    }

    @Override
    public void registerGestureListener(OnGestureListener listener, int priority) {
        mGestureManager.registerGestureListener(listener, priority);
    }

    @Override
    public void unregisterGestureListener(OnGestureListener listener) {
        mGestureManager.unregisterGestureListener(listener);
    }

    @Override
    public void registerOnShutterButtonListener(OnShutterButtonListener listener, int priority) {
        mShutterManager.registerOnShutterButtonListener(listener, priority);
    }

    @Override
    public void unregisterOnShutterButtonListener(OnShutterButtonListener listener) {
        mShutterManager.unregisterOnShutterButtonListener(listener);
    }

    @Override
    public void setThumbnailClickedListener(OnThumbnailClickedListener listener) {
        if (mThumbnailViewManager != null) {
            mThumbnailViewManager.setThumbnailClickedListener(listener);
        }
    }

    @Override
    public void setModeChangeListener(OnModeChangeListener listener) {
        mModeChangeListener = listener;
    }

    @Override
    public void triggerModeChanged(String newMode) {
        mModePickerManager.modeChanged(newMode);
    }

    @Override
    public void triggerShutterButtonClick(int currentPriority) {
        mShutterManager.triggerShutterButtonClicked(currentPriority);
    }

    @Override
    public void triggerShutterButtonLongPressed(int currentPriority) {
        mShutterManager.triggerShutterButtonLongPressed(currentPriority);
    }

    @Override
    public void addToQuickSwitcher(View view, int priority) {
        mQuickSwitcherManager.addToQuickSwitcher(view, priority);
    }

    @Override
    public void isChangedMode(boolean isChanged,int flag) {
        mQuickSwitcherManager.isChangedMode(isChanged,flag);
    }

    @Override
    public void removeFromQuickSwitcher(View view) {
        mQuickSwitcherManager.removeFromQuickSwitcher(view);
    }

    @Override
    public void addToIndicatorView(View view, int priority) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIndicatorViewManager.addToIndicatorView(view, priority);
            }
        });
    }

    @Override
    public void removeFromIndicatorView(View view) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIndicatorViewManager.removeFromIndicatorView(view);
            }
        });
    }

    @Override
    public void addSettingView(ICameraSettingView view) {
        mSettingFragment.addSettingView(view);
    }

    @Override
    public void removeSettingView(ICameraSettingView view) {
        mSettingFragment.removeSettingView(view);
    }

    @Override
    public void refreshSettingView() {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSettingFragment.refreshSettingView();
            }
        });
    }

    @Override
    public void updateSettingIconVisibility() {
        boolean visible = mSettingFragment.hasVisibleChild();
        mModePickerManager.setSettingIconVisible(visible);
    }

    @Override
    public void showSavingDialog(String message, boolean isNeedShowProgress) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup root = (ViewGroup) mApp.getActivity().getWindow().getDecorView();
                TextView text;
                if (mSavingDialog == null) {
                    mSavingDialog = (ViewGroup) mApp.getActivity().getLayoutInflater()
                            .inflate(R.layout.rotate_dialog, root, false);
                    View progress = mSavingDialog.findViewById(R.id.dialog_progress);
                    text = (TextView) mSavingDialog.findViewById(R.id.dialog_text);
                    if (isNeedShowProgress) {
                        progress.setVisibility(View.VISIBLE);
                    } else {
                        progress.setVisibility(View.GONE);
                    }
                    if (message != null) {
                        text.setText(message);
                    } else {
                        text.setText(R.string.saving_dialog_default_string);
                    }
                    root.addView(mSavingDialog);
                    int orientation = mApp.getGSensorOrientation();
                    if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                        int compensation = CameraUtil.getDisplayRotation(mApp.getActivity());
                        orientation = orientation + compensation;
                        CameraUtil.rotateViewOrientation(mSavingDialog, orientation, false);
                    }
                    mSavingDialog.setVisibility(View.VISIBLE);
                } else {
                    text = (TextView) mSavingDialog.findViewById(R.id.dialog_text);
                    text.setText(message);
                }
            }
        });
    }
    @Override
    public void hideSavingDialog() {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSavingDialog != null) {
                    ViewGroup root = (ViewGroup) mApp.getActivity().getWindow().getDecorView();
                    mSavingDialog.setVisibility(View.GONE);
                    root.removeView(mSavingDialog);
                    mSavingDialog = null;
                }
            }
        });
    }

    @Override
    public void setEffectViewEntry(View view) {
        mEffectViewManager.setViewEntry(view);;
    }

    @Override
    public void attachEffectViewEntry() {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEffectViewManager.attachViewEntry();
            }
        });
    }

    @Override
    public void showQuickSwitcherOption(View optionView) {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mQuickSwitcherManager.showQuickSwitcherOption(optionView);
            }
        });
    }

    @Override
    public void hideQuickSwitcherOption() {
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mQuickSwitcherManager.hideQuickSwitcherOption();
            }
        });
    }

    @Override
    public String getCurrentMode() {
        return mCurrentMode;
    }
    protected void removeTopSurface() {
        mPreviewManager.removeTopSurface();
    }

    private void layoutSettingUI() {
        LinearLayout settingRootView = (LinearLayout) mApp.getActivity()
                .findViewById(R.id.setting_ui_root);

        if ("1".equals(android.os.SystemProperties.get("ro.hct_navigation_bar_style"))
             || CameraUtil.isHasNavigationBar(mApp.getActivity())) {
            // Get the preview height don't contain navigation bar height.
            Point size = new Point();
            mApp.getActivity().getWindowManager().getDefaultDisplay().getSize(size);
            LogHelper.d(TAG, "[layoutSettingUI], preview size don't contain navigation:" + size);
            LinearLayout settingContainer = (LinearLayout) settingRootView
                    .findViewById(R.id.setting_container);
            int navigationBarHeight = CameraUtil.getNavigationBarHeight(mApp.getActivity());
            LinearLayout.LayoutParams containerParams =
                    (LinearLayout.LayoutParams) settingContainer.getLayoutParams();
            //containerParams.height = size.y;
            if(CameraUtil.isNotchScreenSupport()==1){
                containerParams.topMargin += CameraUtil.getStatusBarHeight(mCameraActivity);
            }
            /*if(navigationBarHeight!=-1){
                containerParams.bottomMargin = navigationBarHeight;
            }*/
            settingContainer.setLayoutParams(containerParams);

            LinearLayout settingTail = (LinearLayout) settingRootView
                    .findViewById(R.id.setting_tail);
            //get navigation bar height.
            // int navigationBarHeight = CameraUtil.getNavigationBarHeight(mApp.getActivity());
            LogHelper.d(TAG, "[layoutSettingUI], navigationBarHeight:" + navigationBarHeight);
            //set setting tail view height as navigation bar height.
            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) settingTail.getLayoutParams();
            params.height = navigationBarHeight;
            settingTail.setLayoutParams(params);
        }
    }

    private void showSetting() {
        if (!isNotchSupport) {
            LinearLayout settingRootView = (LinearLayout) mApp.getActivity().findViewById(R.id.setting_ui_root);
            LinearLayout settingContainer = (LinearLayout) settingRootView.findViewById(R.id.setting_container);
            LinearLayout.LayoutParams containerParams = (LinearLayout.LayoutParams) settingContainer.getLayoutParams();
            containerParams.height = mScreenHeight - CameraUtil.getNavigationBarHeight(mApp.getActivity());
            settingContainer.setLayoutParams(containerParams);
        }

        FragmentTransaction transaction = mApp.getActivity().getFragmentManager()
                .beginTransaction();
        transaction.addToBackStack("setting_fragment");
        transaction.replace(R.id.setting_container, mSettingFragment, "Setting")
                .commitAllowingStateLoss();

        //*/ hct.huangfei, 20201130. enable navigationbar.
        //enableNavigationbar(false);
        //*/
    }

    private void hideSetting() {
        mApp.getActivity().getFragmentManager().popBackStackImmediate("setting_fragment",
                FragmentManager.POP_BACK_STACK_INCLUSIVE);

        //*/ hct.huangfei, 20201130. enable navigationbar.
        //enableNavigationbar(true);
        //*/
    }
    /* add  by liangchangwei for Detai Fragment View begin -- */
    private void showDetail() {
        if (!isNotchSupport) {
            LinearLayout settingRootView = (LinearLayout) mApp.getActivity().findViewById(R.id.setting_ui_root);
            LinearLayout settingContainer = (LinearLayout) settingRootView.findViewById(R.id.setting_container);
            LinearLayout.LayoutParams containerParams = (LinearLayout.LayoutParams) settingContainer.getLayoutParams();
            /* modify by bv liangchangwei for fix bug 6555 -- */
            if(CameraUtil.isEdgeToEdgeEnabled(mApp.getActivity())){
                containerParams.height = mScreenHeight - mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.navigationbar_hide_bottom_margin) - CameraUtil.getRealStatusBarHeight(mCameraActivity) + 30;
            }else{
                containerParams.height = mScreenHeight - mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.navigationbar_hide_bottom_margin) + 12;
            }
            /* modify by bv liangchangwei for fix bug 6555 -- */
            settingContainer.setLayoutParams(containerParams);
        }

        FragmentTransaction transaction = mApp.getActivity().getFragmentManager()
                .beginTransaction();
        transaction.addToBackStack("detail_fragment");
        transaction.replace(R.id.setting_container, mDetailFragment, "Detail")
                .commitAllowingStateLoss();
    }

    private void hideDetail() {
        mApp.getActivity().getFragmentManager().popBackStackImmediate("detail_fragment",
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
    /* add  by liangchangwei for Detai Fragment View end -- */

    private void hideAlertDialog() {
        CameraUtil.hideAlertDialog(mApp.getActivity());
    }

    /**
     * Shutter change listener implementer.
     */
    private class OnShutterChangeListenerImpl implements
                                           ShutterButtonManager.OnShutterChangeListener {

        @Override
        public void onShutterTypeChanged(String newModeName,String newModeType) {

            //add by huangfei for watermark start
            mCameraActivity.setThumbnailClicked(true);
            //add by huangfei for watermark end

            mCurrentModeType = newModeType;
            mCurrentModeName = newModeName;
            LogHelper.i(TAG, "onShutterTypeChanged mCurrentModeType " + mCurrentModeType + " mCurrentModeName = " + mCurrentModeName);
            ModeItem item = mModeProvider.getModeEntryName(mCurrentModeName, mCurrentModeType);
            mCurrentMode = item.mMode; //add by Jerry
            LogHelper.i(TAG,"item.mMode = " + item.mMode + " item.mClassName = " + item.mClassName + " item.mType = " + item.mType);
            if(item.mClassName != null && mModeChangeListener != null){
                mModeChangeListener.onModeSelected(item.mClassName);
            }
            mShutterManager.updateCurrentModeShutter(item.mType, item.mShutterIcon);
        }
    }

    /**
     * Setting state listener implementer.
     */
    private class SettingStateListener implements SettingFragment.StateListener {

        @Override
        public void onCreate() {
            View view = mApp.getActivity().findViewById(R.id.setting_ui_root);
            view.setVisibility(View.VISIBLE);
            //bv wuyonglin add for setting ui 20200923 start
            mApp.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mApp.getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            if(!getShutterButtonManager().getIsShowMoreModeView()){
                //bv wuyonglin add for bug3080 20201130 start
                mLastNightMode = (mApp.getActivity().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                        == Configuration.UI_MODE_NIGHT_YES;
                mApp.getActivity().getWindow().setNavigationBarColor(mApp.getActivity().getResources().getColor(android.R.color.white));
                if (!mLastNightMode) {
                    //bv wuyonglin add for bug5081 20210406 start
                    mApp.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    mApp.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    //bv wuyonglin add for bug5081 20210406 end
                    mApp.getActivity().getWindow().setStatusBarColor(Color.WHITE);
                    mApp.getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                } else {
                    mApp.getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                }
                //bv wuyonglin add for bug3080 20201130 end
            }else{
                mApp.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                mApp.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                mApp.getActivity().getWindow().setNavigationBarColor(mApp.getActivity().getResources().getColor(android.R.color.black));
                mApp.getActivity().getWindow().setStatusBarColor(mApp.getActivity().getResources().getColor(android.R.color.black));
            }
            //bv wuyonglin add for setting ui 20200923 end
            applyAllUIVisibility(View.GONE);
            /* add  by liangchangwei for Detai Fragment View begin -- */
            if(getShutterButtonManager().getIsShowMoreModeView()){
                mModePickerManager.hideMoreModeView();
            }
            /* add  by liangchangwei for Detai Fragment View end -- */
            mIsSettingShow = true; //add by Jerry
        }

        @Override
        public void onResume() {

        }

        @Override
        public void onPause() {

        }

        @Override
        public void onDestroy() {
            View view = mApp.getActivity().findViewById(R.id.setting_ui_root);
            view.setVisibility(View.GONE);
            //bv wuyonglin add for setting ui 20200923 start
            mApp.getActivity().getWindow().getDecorView().setSystemUiVisibility(View.INVISIBLE);
            mApp.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mApp.getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            mApp.getActivity().getWindow().setNavigationBarColor(Color.TRANSPARENT);
            //bv wuyonglin add for setting ui 20200923 end
            applyAllUIVisibility(View.VISIBLE);

            /* add  by liangchangwei for Detai Fragment View begin -- */
            if(getShutterButtonManager().getIsShowMoreModeView()){
                mModePickerManager.showMoreModeView();
                applyAllUIVisibility(View.INVISIBLE);
                setUIVisibility(IAppUi.SHUTTER_ROOTLAYOUT, View.VISIBLE);
            }
            /* add  by liangchangwei for Detai Fragment View end -- */
            mIsSettingShow = false;//add by Jerry
        }

        //bv wuyonglin add for setting ui 20200923 start
        @Override
        public void onRestoreSetting() {
            getShutterButtonManager().goToPhotoMode();
            synchronized (mRestoreSettingListener) {
                LogHelper.i(TAG, "onRestoreSetting restoreSettingtoValue mRestoreSettingListener.size() = "+mRestoreSettingListener.size());
                for (int i = 0;i < mRestoreSettingListener.size();i++) {
                    mRestoreSettingListener.get(i).restoreSettingtoValue();
                }
	        m4KVideo = false;	//bv wuyonglin add for bug2771 20201031
            }
        }
        //bv wuyonglin add for setting ui 20200923 end
    }

    /**
     * Implementer of onTouch listener.
     */
    private class OnTouchListenerImpl implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (mGestureManager != null) {
                Rect rect = new Rect();
                getShutterRootView().getHitRect(rect);
                Configuration config = mApp.getActivity().getResources().getConfiguration();
                if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (motionEvent.getRawY() > rect.top) {
                        //If the touch point is below shutter, ignore it.
                        return true;
                    }
                } else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (motionEvent.getRawX() > rect.top) {
                        //If the touch point is below shutter, ignore it.
                        return true;
                    }
                }
                mGestureManager.getOnTouchListener().onTouch(view, motionEvent);
            }

            //*/ hct.huangfei, 20201130.camera switch by swipe up and down on screen.
            if(motionEvent.getPointerCount()>1){
                mSinglePoint = false;
            }
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = motionEvent.getX();
                    mDownY = motionEvent.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    if(!mSinglePoint){
                        mSinglePoint = true;
                        return true;
                    }
                    mUpX = motionEvent.getX();
                    mUpY = motionEvent.getY();
                    if(mSupportUpDownSwitchCamera){
                        SwitchCameraByUpDown();
                    }
            }
            //*/

            return true;
        }
    }

    /**
     *  Implementer of setting button click listener.
     */
    private class OnSettingClickedListenerImpl implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (mSettingFragment.hasVisibleChild()) {
                if(!CameraUtil.isVideo_HDR_changing){
                    showSetting();
                }
            }
        }
    }

    /**
     * The implementer of OnModeChangeListener.
     */
    private class OnModeChangedListenerImpl implements ModePickerManager.OnModeChangedListener {

        @Override
        public void onModeChanged(String modeName) {
            mCurrentModeName = modeName;
            isVisualSearchMode(modeName);
            ModeItem item = mModeProvider.getModeEntryName(mCurrentModeName, mCurrentModeType);
            mModeChangeListener.onModeSelected(item.mClassName);
            mModePickerManager.updateCurrentModeItem(item);
            String[] supportTypes =
                    mModeProvider.getModeSupportTypes(item.mModeName, mCurrentCameraId);
            mShutterManager.updateModeSupportType();
            mShutterManager.updateCurrentModeShutter(item.mType, item.mShutterIcon);
        }
    }

    /**
     * The implementer of OnModeChangeListener.
     */
    private class OnQuickModeChangedListenerImpl implements QuickSwitcherManager.OnModeChangedListener {

        @Override
        public void onModeChanged(String modeName) {
            mCurrentModeName = modeName;
            isVisualSearchMode(modeName);
            ModeItem item = mModeProvider.getModeEntryName(mCurrentModeName, mCurrentModeType);
            if (item != null) {
                mModeChangeListener.onModeSelected(item.mClassName);
                String[] supportTypes =
                    mModeProvider.getModeSupportTypes(item.mModeName, mCurrentCameraId);
                mShutterManager.updateModeSupportType();
                mShutterManager.updateCurrentModeShutter(item.mType, item.mShutterIcon);
            }
        }
    }

	/**
	* Whether the modeName is VisualSearchMode
	**/
    private void isVisualSearchMode(String modeName){
        String visualSearchMode = mApp.getActivity().getResources().getString(mApp.getActivity().getResources().getIdentifier("visual_search_mode_title",
            "string", mApp.getActivity().getPackageName()));
        if (modeName.equals(visualSearchMode)) {
            CameraUtil.setChangeIconState(true);
        } else {
            CameraUtil.setChangeIconState(false);
        }
    }

    /**
     * Implementer of OnOrientationChangeListener.
     */
    private class OnOrientationChangeListenerImpl implements IApp.OnOrientationChangeListener {

        @Override
        public void onOrientationChanged(int orientation) {
            if (mSavingDialog != null) {
                int compensation = CameraUtil.getDisplayRotation(mApp.getActivity());
                orientation = orientation + compensation;
                CameraUtil.rotateViewOrientation(mSavingDialog, orientation, true);
            }

            if(exit != null && isShowExit){
                int compensation = CameraUtil.getDisplayRotation(mApp.getActivity());
                orientation = orientation + compensation;
                CameraUtil.rotateViewOrientation(exit, orientation, true);
            }
        }
    }

    /**
     * Handler let some task execute in main thread.
     */
    private class ConfigUIHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogHelper.i(TAG, "handleMessage what =  " + msg.what);
            switch (msg.what) {
                case APPLY_ALL_UI_VISIBILITY:
                    //call each manager's setVisibility()
                    int visibility = msg.arg1;
                    for (int count = 0; count < mViewManagers.size(); count++) {
                        mViewManagers.get(count).setVisibility(visibility);
                    }
                    getPreviewFrameLayout().setVisibility(visibility);
                    mOnScreenHintManager.setVisibility(visibility);
                    if (visibility == View.GONE) {
                        mQuickSwitcherManager.hideQuickSwitcherImmediately();
                    }

                    //*/ hct.huangfei, 20201130.camera switcher for custom.
                    if(mCameraSwitchListener!=null){
                        mCameraSwitchListener.onConfigUIVisibility(visibility);
                    }
                    //*/
                    /* hct.wangsenhao, for camera switch @{*/
                    if(mTripleSwitchListener!=null && !getCameraId().equals("1")){
                        mTripleSwitchListener.onConfigUIVisibility(visibility);
                    }
                    /* }@ hct.wangsenhao */
                    //bv wuyonglin add for from other app enter camera intent photo mode after take picture should not show zoom view 20200629 start
                    if (visibility == View.INVISIBLE) {
                        applyZoomViewVisibilityImmediately(visibility);
                    }
                    //bv wuyonglin add for from other app enter camera intent photo mode after take picture should not show zoom view 20200629 start
                    if(isShowExit){
                        exit.setVisibility(visibility);
                    }
                    break;
                case APPLY_ALL_UI_ENABLED:
                    //call each manager's setEnabled()
                    boolean enabled = msg.arg1 == 1;
                    if (mCameraSwitchListener != null) {
                        mCameraSwitchListener.onConfigUIEnabled(enabled);
                    }
                    for (int count = 0; count < mViewManagers.size(); count++) {
                        mViewManagers.get(count).setEnabled(enabled);
                    }
                    break;
                case SET_UI_VISIBILITY:
                    configUIVisibility(msg.arg1, msg.arg2);
                    break;
                case SET_UI_ENABLED:
                    configUIEnabled(msg.arg1, msg.arg2 == 1);
                /* hct.wangsenhao, underwater camera @{ */
                    break;
                case HIDE_UNDER_WATER_VIEW:
                    mUnderWaterView.setVisibility(View.GONE);
                /* }@ hct.wangsenhao */
                    break;
				//bv liangchangwei add for shutter prpgressbar
                case SHOW_SHUTTER_PROGRESS_BAR:
                    Log.i("CameraAppUI", "showCircleProgressBar");
                    mShutterManager.showCircleProgressBar();
                    break;
                case HIDE_SHUTTER_PROGRESS_BAR:
                    Log.i("CameraAppUI", "hideCircleProgressBar");
                    mShutterManager.hideCircleProgressBar();
                    break;
				//bv liangchangwei for HDR
                case HIDE_SCREEN_TOAST_VIEW:
                    if (mTopScreenToast != null) {
                        mApp.getAppUi().hideScreenHint(mTopScreenToast);
                    }
                    break;
                case SHOW_SCREEN_TOAST_VIEW:
                    mConfigUIHandler.removeMessages(HIDE_SCREEN_TOAST_VIEW);
                    if (mTopScreenToast != null) {
                        mApp.getAppUi().showScreenHint(mTopScreenToast);
                    }
                    //mConfigUIHandler.sendEmptyMessageDelayed(HIDE_SCREEN_TOAST_VIEW, 2000);
                    break;
                case ENABLE_THUMBNAIL_CLICK:
                    mHdrPictureProcessing = true;
                    break;
                case DISABLE_THUMBNAIL_CLICK:
                    mHdrPictureProcessing = false;
                    break;
				//bv liangchangwei for HDR
                default:
                    break;
            }
        }
    }

    private void configUIVisibility(int module, int visibility) {
        LogHelper.d(TAG, "configUIVisibility + module " + module + " visibility " + visibility);
        switch (module) {
            case QUICK_SWITCHER:

                //*/ hct.huangfei, 20201130.camera switcher for custom.
                if(mCameraSwitchListener!=null){
                    mCameraSwitchListener.onConfigUIVisibility(visibility);
                }
                //*/
                /* hct.wangsenhao, for camera switch @{*/
                if(mTripleSwitchListener!=null && !getCameraId().equals("1")){
                    mTripleSwitchListener.onConfigUIVisibility(visibility);
                }
                /* }@ hct.wangsenhao*/
                mQuickSwitcherManager.setVisibility(visibility);
                break;
            case MODE_SWITCHER:
                mModePickerManager.setVisibility(visibility);
                break;
            case THUMBNAIL:
                if (mThumbnailViewManager != null) {
                    mThumbnailViewManager.setVisibility(visibility);
                }
                break;
            case SHUTTER_BUTTON:
                mShutterManager.setVisibility(visibility);
                break;
            case INDICATOR:
                mIndicatorViewManager.setVisibility(visibility);
                break;
            case PREVIEW_FRAME:
                getPreviewFrameLayout().setVisibility(visibility);
                break;
            case SCREEN_HINT:
                mOnScreenHintManager.setVisibility(visibility);
            case VIDEO_FLASH:
                mQuickSwitcherManager.setVisibility(visibility);
                //bv wuyonglin add for adjust third app open camera ui 20200930 start
                exit.setVisibility(View.INVISIBLE);
                //bv wuyonglin add for adjust third app open camera ui 20200930 end
                //bv wuyonglin add for startVideoRecord not show videoquality quickSwitch 20191225 start
                if (visibility == 0) {
                    mVideoQualitySwitcheListener.onConfigVideoQualityUIVisibility(4);
                } else {
                    mVideoQualitySwitcheListener.onConfigVideoQualityUIVisibility(0);
                }
                //bv wuyonglin add for startVideoRecord not show videoquality quickSwitch 20191225 end
                break;
            //bv wuyonglin add for adjust custom FaceMode view position 20200303 start
            case SHUTTER_ROOTLAYOUT:
                mShutterManager.setShutterRootLayoutVisibility(visibility);
            //bv wuyonglin add for adjust custom FaceMode view position 20200303 end
            default:
                break;
        }
    }

    private void configUIEnabled(int module, boolean enabled) {
        LogHelper.i(TAG, "configUIEnabled + module " + module + " enabled " + enabled+" mCameraSwitchListener ="+mCameraSwitchListener);
        switch (module) {
            case QUICK_SWITCHER:
                //*/ hct.huangfei, 20201130.camera switcher for custom.
                if(mCameraSwitchListener!=null){
                    mCameraSwitchListener.onConfigUIEnabled(enabled);
                }
                //*/
                mQuickSwitcherManager.setEnabled(enabled);
                break;
            case MODE_SWITCHER:
                mModePickerManager.setEnabled(enabled);
                break;
            case THUMBNAIL:
                if (mThumbnailViewManager != null) {
                    mThumbnailViewManager.setEnabled(enabled);
                }
                break;
            case SHUTTER_BUTTON:
                mShutterManager.setEnabled(enabled);
                break;
            case INDICATOR:
                mIndicatorViewManager.setEnabled(enabled);
                break;
            case PREVIEW_FRAME:
                break;
            case GESTURE:
                mPreviewManager.setEnabled(enabled);
                break;
            case SHUTTER_TEXT:
                mShutterManager.setTextEnabled(enabled);
                break;
            case VIDEO_FLASH:
                mQuickSwitcherManager.setEnabled(enabled);
                break;
            default:
                break;
        }
    }

    private void dumpUIState(AppUIState state) {
        if (state != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("mIndicatorEnabled:")
                    .append(state.mIndicatorEnabled)
                    .append(", mIndicatorVisibleState:")
                    .append(state.mIndicatorVisibleState)
                    .append(", mModeSwitcherEnabled:")
                    .append(state.mModeSwitcherEnabled)
                    .append(", mModeSwitcherVisibleState:")
                    .append(state.mModeSwitcherVisibleState)
                    .append(", mQuickSwitcherEnabled:")
                    .append(state.mQuickSwitcherEnabled)
                    .append(", mQuickSwitcherVisibleState: ")
                    .append(state.mQuickSwitcherVisibleState)
                    .append(", mShutterButtonEnabled:")
                    .append(state.mShutterButtonEnabled)
                    .append(", mShutterButtonVisibleState:")
                    .append(state.mShutterButtonVisibleState)
                    .append(", mThumbnailEnabled:")
                    .append(state.mThumbnailEnabled)
                    .append(", mThumbnailVisibleState:")
                    .append(state.mThumbnailVisibleState)
                    .append(", mPreviewFrameVisibleState:")
                    .append(state.mPreviewFrameVisibleState)
                    .toString();
            LogHelper.i(TAG, "[dumpUIState]: " + builder);
        }
    }

    // [Add for CCT tool] Receive keycode and enable/disable ZSD @{
    public IApp.KeyEventListener getKeyEventListener() {
        return new IApp.KeyEventListener() {
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                return false;
            }

            @Override
            public boolean onKeyUp(int keyCode, KeyEvent event) {
                if (!CameraUtil.isSpecialKeyCodeEnabled()) {
                    return false;
                }
                if (!CameraUtil.isNeedInitSetting(keyCode)) {
                    return false;
                }
                showSetting();
                hideSetting();
                /* add  by liangchangwei for Detai Fragment View start -- */
                showDetail();
                hideDetail();
                /* add  by liangchangwei for Detai Fragment View end -- */
                return false;
            }
        };
    }
    // @}

    //*/ hct.huangfei, 20201130.camera switch by swipe up and down on screen.
    public void setCameraSwitcherListener(CameraSwitchListener listener) {
        mCameraSwitchListener = listener;
    }
    private void SwitchCameraByUpDown() {
        int mMoveX = (int) Math.abs(mUpX - mDownX);
        int mMoveY = (int) Math.abs(mUpY - mDownY);
        if (mMoveX < 120 && mMoveY > 200&&mCameraSwitchListener!=null) {
            mCameraSwitchListener.onCameraByUpDownChange();
        }
    }
    //*/

    //add by huangfei for lowpower tips start
    public String getCameraId(){
       return mCurrentCameraId;
    }
    //add by huangfei for lowpower tips end
    //add by huangfei for shutter title change start
    public View inflate(int layoutId) {
        return mApp.getActivity().getLayoutInflater().inflate(layoutId, parentView, true);
    }


    public ShutterButtonManager getShutterButtonManager(){
        return mShutterManager;
    }
    //add by huangfei for shutter title change end
    //add by huangfei by camera switcher for custom start
    public boolean getContentViewValue(){
        return mShowContentView;
    }
    //add by huangfei by camera switcher for custom end

    public void setContentViewValue(boolean showContentView){
        mShowContentView = showContentView;
    }

    //add by huangfei for getCsState start
    public void updateCsState(boolean continuousshoting){
        mContinuousshoting = continuousshoting ;
    }

    public boolean getCsState(){
        return mContinuousshoting;
    }
    //add by huangfei for getCsState end
    public void setCaptureStatus(boolean status){
        mIsCaptureing = status;
	//bv wuyonglin add for after photo mode self timer oninterrupt can not go to gallery 20200629 start
        if (!status) {
            mCameraActivity.setThumbnailClicked(true);
        }
	//bv wuyonglin add for after photo mode self timer oninterrupt can not go to gallery 20200629 end
    }

    public boolean getCaptureStatus(){
        LogHelper.i(TAG, "getCaptureStatus + mIsCaptureing: " + mIsCaptureing);
        return mIsCaptureing ;
    }
    //add by huangfei for continuousshot abnormal end
    //add by huangfei for more mode start
    public ModePickerManager getModePickerManager(){
        return mModePickerManager;
    }

    public void updateCurrentModeByMore(String mode){
        if(mode!=null&&!"".equals(mode)){
            mCurrentMode = mode;
        }
    }
    //add by huangfei for more mode end

    //*/ hct.huangfei, 20201026. add storagepath.
    @Override
    public void notifyUpdateThumbnail() {
        if (mThumbnailViewManager != null) {
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mThumbnailViewManager.getLastThumbnail();
                }
            });
        }
    }
    //*/ 
    //add by huangfei for more mode start
    private boolean isMoreMode(int priority,int[] moreModeList){
        if(moreModeList.length==0){
            return false;
        }
        for(int i = 0;i < moreModeList.length;i++) {
            if(moreModeList[i]==(priority)) {
               return true;
            }
        }
        return false;
    }
    //add by huangfei for more mode end
    //add by huangfei for campostalgo.FeatureResult null start
    private boolean removeModeBySecureCameraIntent(String mode){
        if(mCameraActivity.isSecureCameraIntent(mApp.getActivity())){
            if("Hdr".equals(mode) || "Panorama".equals(mode)){
                return true;
            }
        }
        return false;
    }
    //add by huangfei for campostalgo.FeatureResult null end

    //*/ hct.huangfei, 20201028. add gridlines.
    public GridlinesMonitor getMonitor(){
        return mMonitor;
    }
    ///*/
    /* hct.wangsenhao, underwater camera @{ */
    public void modeSwitch(boolean isSwitchToVideo){
        if(isSwitchToVideo){
            mShutterManager.onShutterChangedStart(mNormalType,"Video",0);
            mShutterManager.setChangIndex(0);
        }else{
            mShutterManager.onShutterChangedStart(mNormalType,"Picture",1);
            mShutterManager.setChangIndex(1);
        }
        mShutterManager.updateModeSupportType();
    }
    public int isUnderWaterSupport(){
        return CameraUtil.getUnderWaterSupport(mCameraActivity);
    }
    public void SwitchCamera(){
        mCameraSwitchListener.onCameraByUpDownChange();
    }
    /* }@ hct.wangsenhao */
	//add by huangfei for water mark start
    @Override
    public void saveWaterMark(String filePath, long dateTaken,ExifInterface exif) {
        WaterMarkUtil.saveWaterMark(mApp.getActivity(), filePath, dateTaken,exif,false, null);    //bv wuyonglin modify for AiWorksBokeh water logo 20200827
    }
	//add by huangfei for water mark end

     //*/ hct.huangfei, 20201030. add customize zoom.
    public IZoomSliderUI getZoomSliderUI(){
        return mZoomSliderUICtrl;
    }
    //*/

    public ZoomSliderUICtrl getZoomSliderUICtrl(){
        return mZoomSliderUICtrl;
    }

    public void setZoomConfig(IZoomConfig object){
        mZoomConfig = object;
    }

    public IZoomConfig getZoomConfig(){
        return mZoomConfig;
    }
    //add by huangfeifor front tripleswitchhorizontal end

    //add by huangfei for watermark start
    public boolean isWaterMarkOn(){
        if (CameraUtil.isWaterMarkOn(mApp.getCurrentCameraMode().getDataStore(),mCameraActivity)){
            return true;
        }
        return false;
    }
    //add by huangfei for watermark end
	//bv liangchangwei for HDR
    public void showScreenToastView(){
        if(mConfigUIHandler != null){
            LogHelper.i(TAG,"showScreenToastView SHOW_SCREEN_TOAST_VIEW");
            mConfigUIHandler.sendEmptyMessage(SHOW_SCREEN_TOAST_VIEW);
        }
    }
	//bv liangchangwei for HDR
    public boolean isHdrPictureProcessing(){
        return mHdrPictureProcessing;
    }

    public void setPictureProcessing(boolean processing){
        LogHelper.i(TAG,"setPictureProcessing processing ="+processing);
        /*if(processing){
            mConfigUIHandler.removeMessages(ENABLE_THUMBNAIL_CLICK);
            mConfigUIHandler.sendEmptyMessageDelayed(ENABLE_THUMBNAIL_CLICK,500);
        }else{
            mConfigUIHandler.sendEmptyMessage(DISABLE_THUMBNAIL_CLICK);
        }*/
        if (!processing) {
            mShutterManager.moreModeTextEnabled(true);
        }
        mHdrPictureProcessing = processing;
    }

    //add by huangfei for wide angle start
    public boolean isWideAngleDistortionSupport(){
        if(wideAngleId.equals(mCurrentCameraId)&&wideAngleDistortion){
            return true;
        }
        return false;
    }
    //add by huangfei for wide angle end

	//bv liangchangwei for HDR
    public void setZoomViewListener(ZoomViewListener listener) {
        mZoomViewListener = listener;
    }

    //bv wuyonglin add for startVideoRecord not show videoquality quickSwitch 20191225 start
    public void setVideoQualitySwitcherListener(VideoQualitySwitcherListener listener) {
        mVideoQualitySwitcheListener = listener;
    }
    //bv wuyonglin add for startVideoRecord not show videoquality quickSwitch 20191225 end
	//bv liangchangwei add for shutter prpgressbar
    public void ShowShutterProgressBar(){
	    //CameraSwitheronConfigUIEnabled(false);
        setUIEnabled(QUICK_SWITCHER, false);
        mConfigUIHandler.sendEmptyMessage(SHOW_SHUTTER_PROGRESS_BAR);
    }

    public void HideShutterProgressBar(){
        mConfigUIHandler.sendEmptyMessage(HIDE_SHUTTER_PROGRESS_BAR);
	    //CameraSwitheronConfigUIEnabled(true);
        setUIEnabled(QUICK_SWITCHER, true);
    }

    public void CameraSwitheronConfigUIEnabled(boolean enabled){
        if(mCameraSwitchListener!=null){
            mCameraSwitchListener.onConfigUIEnabled(enabled);
        }
    }
	
    public void setBokehViewListener(BokehViewListener listener) {
        mBokehViewListener = listener;
    }

    public void applyBokehViewVisibilityImmediately(int visibility) {
        LogHelper.d(TAG, "applyBokehViewVisibilityImmediately2 + visibility " + visibility+" mBokehViewListener =" +mBokehViewListener);
        if(mBokehViewListener!=null){
            mBokehViewListener.onConfigBokehUIVisibility(visibility);
        }
    }

    public void setFaceBeautyViewListener(FaceBeautyViewListener listener) {
        LogHelper.d(TAG, "applyFaceBeautyViewVisibilityImmediately setFaceBeautyViewListener + listener " + listener);
        mFaceBeautyViewListener = listener;
    }

    public void applyFaceBeautyViewVisibilityImmediately(int visibility) {
        LogHelper.d(TAG, "applyFaceBeautyViewVisibilityImmediately + visibility " + visibility+" mFaceBeautyViewListener =" +mFaceBeautyViewListener);
        if(mFaceBeautyViewListener!=null){
            mFaceBeautyViewListener.onConfigFaceBeautyUIVisibility(visibility);
        }
    }

    //bv wuyonglin add for bug3677 20210204 start
    private VideoState mVideoState;

    public void updateVideoState(VideoState state){
        mRecording = (state == VideoState.STATE_RECORDING);
        mVideoState = state;
        LogHelper.i(TAG, "updateVideoState VideoState mRecording =" + mRecording+" mVideoState ="+mVideoState+" state ="+state);
    }

    public boolean getVideoStateIsPreview(){
        LogHelper.i(TAG, "getVideoState VideoState mRecording =" + mRecording+" mVideoState ="+mVideoState);
        return mVideoState == VideoState.STATE_PREVIEW;
    }
    //bv wuyonglin add for bug3677 20210204 end

    //add by huangfei for thumbnail update abnormal start
    public boolean isVideoRecording(){
        return mRecording;
    }

    public void updateVideoState(boolean recording){
        mRecording = recording;
    }
    //add by huangfei for thumbnail update abnormal end
    //*/ hct.huangfei, 20201030. add zoom switch.
    public float getPreviousSpan(){
        return mPeviousSpan;
    }

    public float getBasicZoomRatio(){
        return mBasicZoomRatio;
    }


    public void setZoomSwitchPreviousSpan(float basicZoomRatio,float previousSpan){
        mPeviousSpan = previousSpan;
        mBasicZoomRatio = basicZoomRatio;
    }

    public boolean isZoomSwitchSupport(){
        return Config.isZoomSwitchSupport(mCameraActivity);
    }

    public boolean isZoomSlideSwitchSupport(){
        return Config.isZoomSlideSwitchSupport(mCameraActivity);
    }

    public boolean isZoomSwitchSupportCameraId(){
        if(isZoomSwitchSupport() && ("0".equals(mCurrentCameraId) || Config.getWideAngleId().equals(mCurrentCameraId))){
            return true;
        }
        return false;
    }

    public boolean isZoomSwitchMode(){
        //bv wuyonglin modify for bug2771 20201031 start
        if("Photo".equals(getCurrentMode()) || ("Video".equals(getCurrentMode()) && !is4KVideo()) || "Night".equals(getCurrentMode())){
        //bv wuyonglin modify for bug2771 20201031 end
            return true;
        }
        return false;
    }
    //*/

    //*/ hct.huangfei, 20201130. enable navigationbar.
    private void enableNavigationbar(boolean enable){
        if(CameraUtil.isEdgeToEdgeEnabled(mApp.getActivity())){
            return;
        }
        View decorView = mApp.getActivity().getWindow().getDecorView();
        if(enable){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_LAYOUT_FLAGS
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }else{
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE
              | View.SYSTEM_UI_FLAG_FULLSCREEN
              | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
              | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
              | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

    }
    //*/

    //*/ hct.huangfei, 20201206.hdr view int topbar.
    public HdrManager getHdrManager(){
        return mHdrManager;
    }
    //*/
    /* hct.wangsenhao, for camera switch @{*/
    public void setTripleSwitchListener(TripleSwitchListener listener) {
        mTripleSwitchListener = listener;
    }
    /* }@ hct.wangsenhao */

    //bv wuyonglin add for setting ui 20200923 start
    public void setRestoreSettingListener(RestoreSettingListener restoreSettingListener) {
	synchronized (mRestoreSettingListener) {
            if (!mRestoreSettingListener.contains(restoreSettingListener)) {
                mRestoreSettingListener.add(restoreSettingListener);
            }
        }
    }

    public void removeRestoreSettingListener(RestoreSettingListener restoreSettingListener) {
	synchronized (mRestoreSettingListener) {
            if (mRestoreSettingListener.contains(restoreSettingListener)) {
                mRestoreSettingListener.remove(restoreSettingListener);
            }
        }
    }
    //bv wuyonglin add for setting ui 20200923 end

    //bv wuyonglin add for adjust third app open camera ui 20200930 start
    public View getExitView() {
        return exit;
    }
    //bv wuyonglin add for adjust third app open camera ui 20200930 end

    //bv wuyonglin add for bug2771 20201031 start
    public boolean is4KVideo(){
        return m4KVideo;
    }

    public void updateIs4KVideo(boolean is4KVideo){
                LogHelper.i(TAG,"updateIs4KVideo is4KVideo ="+is4KVideo);
        m4KVideo = is4KVideo;
    }
    //bv wuyonglin add for bug2771 20201031 end
}
