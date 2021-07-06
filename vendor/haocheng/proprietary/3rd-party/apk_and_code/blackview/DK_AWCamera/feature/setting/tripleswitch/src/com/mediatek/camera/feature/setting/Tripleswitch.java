package com.mediatek.camera.feature.setting;

import android.app.Activity;
import android.hardware.Camera;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;
import android.view.Gravity;

import com.mediatek.camera.R;
import com.mediatek.camera.Config;
import com.mediatek.camera.CameraActivity;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.ISettingManager.SettingDeviceRequester;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.feature.setting.zoom.IZoomConfig;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.common.utils.Size;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.ui.CameraAppUI;
import android.os.SystemProperties;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class Tripleswitch extends SettingBase {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(Tripleswitch.class.getSimpleName());

    private static final String CAMERA_FACING_BACK = "back";
    private static final String CAMERA_FACING_WIDE = "wide";
    private static final String CAMERA_FACING_MACRO = "macro";
    private static final String CAMERA_FACING_NIGHT = "night";
    private static final String CAMERA_DEFAULT_FACING = CAMERA_FACING_BACK;
    private static final String KEY_TRIPLE_SWITCH = "key_triple_switch";
    private static final int WIDE_TAG = 1;
    private static final int NORMAL_TAG = 2;
    private static final int MACRO_TAG = 3;
    private static final int NGIHT_TAG = 4;
    private static final int ANIMATION_DURATION = 300;
    
    private String mFacing;
    private CameraActivity activity;
    private FrameLayout mFrameLayout;
    private TextView mSwitcherNormal;
    private TextView mSwitcherMacro;
    private TextView mSwitcherNight;
    private TextView mSwitcherWide;
    private ImageView img_selected_background;
    private int point_wide;
    private int point_normal;
    private int point_macro;
    private int point_night;
    private AnimationFactory mAnimationFactory;
    private String mLastRequestCameraId = "0";
    
    private static final String BACK_CAMERA_ID = "0";
    private static final String FRONT_CAMERA_ID = "1";
    private static String wide_camera_id = "2";
    private static String macro_camera_id = "3";
    private static String night_camera_id = "4";

    private boolean mEnable = false;
    private long mTime = 0;
    private String currentCameraID;
    private List<String> mSupportValues = new ArrayList<String>();
    private String nextFacing = null;
    private String newCameraId = null;
    private boolean isMacroSupport = false;
    private boolean isWideSupport = false;
    private boolean isNightSupport = false;
    private boolean isAutoMarginTop = false;
    private boolean isShowMacroToast = false;
    private boolean isSupportMacroVideo = false;
    
    private int point_1;
    private int point_2;
    private int point_3;
    private int point_4;
    
    @Override
    public void init(IApp app, 
                    ICameraContext cameraContext,
                    SettingController settingController) {
        super.init(app, cameraContext, settingController);
        mFacing = mDataStore.getValue(KEY_TRIPLE_SWITCH, CAMERA_DEFAULT_FACING, getStoreScope());
        int numOfCameras = Camera.getNumberOfCameras();
        activity = (CameraActivity)mApp.getActivity();
        mFrameLayout = activity.findViewById(R.id.triple_switch);
        mAnimationFactory = new AnimationFactory();
        if (numOfCameras > 1) {
            wide_camera_id = SystemProperties.get("ro.hct_wide_angle_id","-1");
            macro_camera_id = SystemProperties.get("ro.hct_macroLens_id","-1");
            night_camera_id = SystemProperties.get("ro.hct_night_vision_id","-1");
            isMacroSupport = "-1".equals(macro_camera_id) ? false : true;
            isWideSupport =  "-1".equals(wide_camera_id) ? false : true;
            isNightSupport =  "-1".equals(night_camera_id) ? false : true;
            mSupportValues.add(CAMERA_FACING_BACK);
            if(isNightSupport){
                mSupportValues.add(CAMERA_FACING_NIGHT);
            }
            if(isWideSupport){
                mSupportValues.add(CAMERA_FACING_WIDE);
            }
            if(isMacroSupport){
                mSupportValues.add(CAMERA_FACING_MACRO);
            }
            setSupportedPlatformValues(mSupportValues);
            setSupportedEntryValues(mSupportValues);
            setEntryValues(mSupportValues);
            isShowMacroToast = activity.getResources().getBoolean(R.bool.config_triple_switch_macro_toast_support);
            isSupportMacroVideo = activity.getResources().getBoolean(R.bool.config_triple_switch_macro_video_support);
            int margin_root = activity.getResources().getDimensionPixelOffset(R.dimen.switch_margin_root);
            int margin_child = activity.getResources().getDimensionPixelOffset(R.dimen.switch_margin_child);
            int switch_size = activity.getResources().getDimensionPixelOffset(R.dimen.switch_size);
            
            point_1 = margin_root;
            point_2 = margin_root + margin_child + switch_size;
            point_3 = margin_root + margin_child*2 + switch_size*2;
            point_4 = margin_root + margin_child*3 + switch_size*3;
            
            point_wide = point_1;
            point_normal = point_2;
            point_macro = point_3;
            point_night = point_4;
            
            if(isWideSupport && isNightSupport && !isMacroSupport){
                point_night = point_3;
            }
            
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    img_selected_background = activity.findViewById(R.id.camera_selected_background);
                    mSwitcherNormal = activity.findViewById(R.id.camera_normal_switch);
                    if(mSwitcherNormal != null){
                        FrameLayout.LayoutParams lp_normal = (FrameLayout.LayoutParams) mSwitcherNormal.getLayoutParams();
                        lp_normal.topMargin = point_normal;
                       if(!isMacroSupport || !isWideSupport){
                            lp_normal.bottomMargin = margin_root;
                        }
                        mSwitcherNormal.setLayoutParams(lp_normal);
                        mSwitcherNormal.setOnClickListener(listenerNormal);
                    }
                    if(isWideSupport){
                        mSwitcherWide = activity.findViewById(R.id.camera_wide_switch);
                        mSwitcherWide.setVisibility(View.VISIBLE);
                        if(mSwitcherWide != null){
                            FrameLayout.LayoutParams lp_wide = (FrameLayout.LayoutParams) mSwitcherWide.getLayoutParams();
                            lp_wide.topMargin = point_wide;
                            mSwitcherWide.setLayoutParams(lp_wide);
                            mSwitcherWide.setOnClickListener(listenerWide);
                        }
                    }
                    
                    if(isMacroSupport){
                        mSwitcherMacro = activity.findViewById(R.id.camera_macro_switch);
                        if(mSwitcherMacro != null){
                            FrameLayout.LayoutParams lp_macro = (FrameLayout.LayoutParams) mSwitcherMacro.getLayoutParams();
                            lp_macro.topMargin = point_macro;
                            mSwitcherMacro.setLayoutParams(lp_macro);
                            mSwitcherMacro.setOnClickListener(listenerMacro);
                        }
                        mSwitcherMacro.setVisibility(View.VISIBLE);
                    }
                    if(isNightSupport){
                        mSwitcherNight = activity.findViewById(R.id.camera_night_switch);
                        if(mSwitcherNight != null){
                            FrameLayout.LayoutParams lp_night = (FrameLayout.LayoutParams) mSwitcherNight.getLayoutParams();
                            lp_night.topMargin = point_night;
                            mSwitcherNight.setLayoutParams(lp_night);
                            mSwitcherNight.setOnClickListener(listenerNight);
                        }
                        mSwitcherNight.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        setValue(mFacing);
        mAppUi.setTripleSwitchListener(mTripleSwitchListener);
    }
    
    View.OnClickListener listenerNormal = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            long currentTime = System.currentTimeMillis();
            boolean enable = Math.abs(currentTime-mTime)> 1000 ?true:false;
            if(!enable){
                return;
            }
            String currenId = mSettingController.getCameraId();
            mTime = currentTime;
            String lastFacing = mFacing;
            nextFacing = CAMERA_FACING_BACK;
            newCameraId = BACK_CAMERA_ID;
            if(mFacing != nextFacing){
                boolean success = mApp.notifyCameraSelected(newCameraId);
                if (success) {
                    mFacing = nextFacing;
                    mDataStore.setValue(KEY_TRIPLE_SWITCH, mFacing, getStoreScope(), true);
                    if(currenId.equals(wide_camera_id)){
                        img_selected_background.startAnimation(mAnimationFactory.creatAnimation(
                            point_normal,point_normal,img_selected_background,ANIMATION_DURATION));
                    } else if(currenId.equals(night_camera_id)){
                        img_selected_background.startAnimation(mAnimationFactory.creatAnimation(
                            point_normal - point_night,point_normal,img_selected_background,ANIMATION_DURATION));
                    } else {
                        if(isWideSupport){
                            img_selected_background.startAnimation(mAnimationFactory.creatAnimation(
                                -point_normal,point_normal,img_selected_background,ANIMATION_DURATION));
                        } else {
                            img_selected_background.startAnimation(mAnimationFactory.creatAnimation(
                                point_normal,point_normal,img_selected_background,ANIMATION_DURATION));
                        }
                    }
                }
                mSwitcherNormal.setTextColor(Color.parseColor("#222222"));
                if(mSwitcherWide != null){
                    mSwitcherWide.setTextColor(Color.parseColor("#FFFFFF"));
                }
                if(mSwitcherMacro != null){
                    mSwitcherMacro.setTextColor(Color.parseColor("#FFFFFF"));
                }
                if(mSwitcherNight != null){
                    mSwitcherNight.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
        }
    };
    
    View.OnClickListener listenerMacro = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            long currentTime = System.currentTimeMillis();
            boolean enable = Math.abs(currentTime-mTime)> 1000 ?true:false;
            if(!enable){
                return;
            }
            String currenId = mSettingController.getCameraId();
            String lastFacing = mFacing;
            mTime = currentTime;
            nextFacing = CAMERA_FACING_MACRO;
            newCameraId = macro_camera_id;
            if(mFacing != nextFacing){
                boolean success = mApp.notifyCameraSelected(newCameraId);
                if (success) {
                    mFacing = nextFacing;
                    mDataStore.setValue(KEY_TRIPLE_SWITCH, mFacing, getStoreScope(), true);
                    if(currenId.equals(wide_camera_id)){
                        img_selected_background.startAnimation(mAnimationFactory.creatAnimation(
                            point_macro,point_macro,img_selected_background,ANIMATION_DURATION));
                    } else if(currenId.equals(night_camera_id)){
                        img_selected_background.startAnimation(mAnimationFactory.creatAnimation(
                            -point_normal,point_macro,img_selected_background,ANIMATION_DURATION));
                    } else {
                        if(isWideSupport){
                            img_selected_background.startAnimation(mAnimationFactory.creatAnimation(
                                point_normal,point_macro,img_selected_background,ANIMATION_DURATION));
                        } else {
                            img_selected_background.startAnimation(mAnimationFactory.creatAnimation(
                                -point_normal,point_macro,img_selected_background,ANIMATION_DURATION));
                        }
                    }
                    mSwitcherMacro.setTextColor(Color.parseColor("#222222"));
                    mSwitcherNormal.setTextColor(Color.parseColor("#FFFFFF"));
                    if(mSwitcherWide != null){
                        mSwitcherWide.setTextColor(Color.parseColor("#FFFFFF"));
                    }

                    if(mSwitcherNight != null){
                        mSwitcherNight.setTextColor(Color.parseColor("#FFFFFF"));
                    }
                    if(isShowMacroToast){
                        Toast mToast = Toast.makeText(mActivity, R.string.macro_lens_camera_toast, Toast.LENGTH_SHORT);
                        mToast.setGravity(Gravity.BOTTOM, 0, 260);
                        mToast.show();
                    }
                }
            }
        }
    };


    View.OnClickListener listenerNight = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            long currentTime = System.currentTimeMillis();
            boolean enable = Math.abs(currentTime-mTime)> 1000 ?true:false;
            if(!enable){
                return;
            }
            String currenId = mSettingController.getCameraId();
            String lastFacing = mFacing;
            mTime = currentTime;
            nextFacing = CAMERA_FACING_NIGHT;
            newCameraId = night_camera_id;
            if(mFacing != nextFacing){
                boolean success = mApp.notifyCameraSelected(newCameraId);
                if (success) {
                    mFacing = nextFacing;
                    mDataStore.setValue(KEY_TRIPLE_SWITCH, mFacing, getStoreScope(), true);
                    if(currenId.equals(wide_camera_id)){
                        img_selected_background.startAnimation(mAnimationFactory.creatAnimation(
                            point_night,point_night,img_selected_background,ANIMATION_DURATION));
                    } else if(currenId.equals(macro_camera_id)){
                        img_selected_background.startAnimation(mAnimationFactory.creatAnimation(
                            point_normal,point_night,img_selected_background,ANIMATION_DURATION));
                    } else if(currenId.equals(BACK_CAMERA_ID)){
                        img_selected_background.startAnimation(mAnimationFactory.creatAnimation(
                            point_night - point_normal,point_night,img_selected_background,ANIMATION_DURATION));
                    }
                    
                    mSwitcherNight.setTextColor(Color.parseColor("#222222"));
                    mSwitcherNormal.setTextColor(Color.parseColor("#FFFFFF"));
                    if(mSwitcherMacro != null){
                        mSwitcherMacro.setTextColor(Color.parseColor("#FFFFFF"));
                    }
                    if(mSwitcherWide != null){
                        mSwitcherWide.setTextColor(Color.parseColor("#FFFFFF"));
                    }
                }
            }
        }
    };

    View.OnClickListener listenerWide = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            long currentTime = System.currentTimeMillis();
            boolean enable = Math.abs(currentTime-mTime)> 1000 ?true:false;
            if(!enable){
                return;
            }
            String currenId = mSettingController.getCameraId();
            mTime = currentTime;
            nextFacing = CAMERA_FACING_WIDE;
            newCameraId = wide_camera_id;
            if(mFacing != nextFacing){
                boolean success = mApp.notifyCameraSelected(newCameraId);
                if (success) {
                    mFacing = nextFacing;
                    mDataStore.setValue(KEY_TRIPLE_SWITCH, mFacing, getStoreScope(), true);
                    if(currenId.equals(BACK_CAMERA_ID)){
                        img_selected_background.startAnimation(mAnimationFactory.creatAnimation(
                            -point_normal,point_wide,img_selected_background,ANIMATION_DURATION));
                    } else if(currenId.equals(night_camera_id)){
                        img_selected_background.startAnimation(mAnimationFactory.creatAnimation(
                            -point_night,point_wide,img_selected_background,ANIMATION_DURATION));
                    } else {
                        img_selected_background.startAnimation(mAnimationFactory.creatAnimation(
                            -point_macro,point_wide,img_selected_background,ANIMATION_DURATION));
                    }
                    mSwitcherWide.setTextColor(Color.parseColor("#222222"));
                    mSwitcherNormal.setTextColor(Color.parseColor("#FFFFFF"));
                    if(mSwitcherNight != null){
                        mSwitcherNight.setTextColor(Color.parseColor("#FFFFFF"));
                    }
                    if(mSwitcherMacro != null){
                        mSwitcherMacro.setTextColor(Color.parseColor("#FFFFFF"));
                    }
                }
            }
        }
    };

    @Override
    public void unInit() {
        if (mSwitcherNormal != null) {
            mSwitcherNormal.setOnClickListener(null);
            mAppUi.removeFromQuickSwitcher(mSwitcherNormal);
        }
        if (mSwitcherMacro != null) {
            mSwitcherMacro.setOnClickListener(null);
            mAppUi.removeFromQuickSwitcher(mSwitcherMacro);
        }
        if (mSwitcherWide != null) {
            mSwitcherWide.setOnClickListener(null);
            mAppUi.removeFromQuickSwitcher(mSwitcherWide);
        }
    }

    @Override
    public void postRestrictionAfterInitialized() {
    }

    @Override
    public void refreshViewEntry() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mFrameLayout != null) {
                    currentCameraID = mSettingController.getCameraId();
                    if (getEntryValues().size() <= 1) {
                        mFrameLayout.setVisibility(View.GONE);
                    } else if(!(BACK_CAMERA_ID.equals(currentCameraID)
                            || wide_camera_id.equals(currentCameraID)
                            || night_camera_id.equals(currentCameraID)
                            || macro_camera_id.equals(currentCameraID))){
                        mFrameLayout.setVisibility(View.GONE);
                    } else {
                        mFrameLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO_AND_VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_TRIPLE_SWITCH;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        return null;
    }

    @Override
    public String getStoreScope() {
        return mDataStore.getGlobalScope();
    }

    private IAppUi.TripleSwitchListener mTripleSwitchListener = new IAppUi.TripleSwitchListener() {
        @Override
        public void onConfigUIVisibility(int visibility) {
            if(mFrameLayout!=null && !mAppUi.getContentViewValue()){
                if (getEntryValues().size() > 1) {
                    mApp.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!isSupportMacroVideo && "Video".equals(mAppUi.getCurrentMode())){
                                return;
                            }
                            mFrameLayout.setVisibility(visibility);
                        }
                    });
                }
            }
        }
    };

}
