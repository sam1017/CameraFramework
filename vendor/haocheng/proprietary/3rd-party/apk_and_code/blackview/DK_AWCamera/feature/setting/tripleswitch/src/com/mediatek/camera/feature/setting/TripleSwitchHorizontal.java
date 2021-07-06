package com.mediatek.camera.feature.setting;

import android.app.Activity;
import android.hardware.Camera;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
import android.view.ViewGroup;
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
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.common.utils.Size;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.widget.RotateStrokeTextView;
import com.mediatek.camera.ui.CameraAppUI;
import com.mediatek.camera.ui.SlidingArcViewUtils;
import com.mediatek.camera.ui.shutter.TripleSwitchHorizontalAdapter;
import com.mediatek.camera.ui.shutter.TripleSwitchHorizontalScrollView;
import com.mediatek.camera.common.mode.photo.view.IZoomSliderUI;
import com.mediatek.camera.feature.setting.zoom.IZoomConfig;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import com.mediatek.camera.ui.CircleTextView;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.mediatek.camera.feature.setting.zoom.IZoomConfig;
import javax.annotation.Nonnull;
import com.mediatek.camera.ui.SlidingArcView;
import android.os.SystemProperties;
import com.mediatek.camera.common.mode.video.videoui.IVideoUI.VideoUIState;
//bv wuyonglin add for screen 2300px adjust all icon position 20201024 start
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.content.Context;
//bv wuyonglin add for screen 2300px adjust all icon position 20201024 end
//bv wuyonglin add for bug2771 20201031 start
import android.media.CamcorderProfile;
//bv wuyonglin add for bug2771 20201031 end

public class TripleSwitchHorizontal extends SettingBase implements IZoomConfig.ZoomLevelSliderListener,IZoomSliderUI.ZoomSliderUIListener{

    private static final LogUtil.Tag TAG = new LogUtil.Tag(TripleSwitchHorizontal.class.getSimpleName());

    private static final String CAMERA_FACING_BACK = "back";
    private static final String CAMERA_FACING_WIDE = "wide";
    private static final String CAMERA_FACING_MACRO = "macro";
    private static final String CAMERA_FACING_ZOOM = "zoom";
    private static final String CAMERA_DEFAULT_FACING = CAMERA_FACING_BACK;
    private static final String KEY_TRIPLE_SWITCH = "key_triple_switch";
    private static final int WIDE_TAG = 1;
    private static final int NORMAL_TAG = 2;
    private static final int MACRO_TAG = 3;
    private static final int ANIMATION_DURATION = 300;

    public static final int TRIPLE_SWITCH_MACRO = 0;  
    public static final int TRIPLE_SWITCH_WIDE = 1; 
    public static final int TRIPLE_SWITCH_NORMAL = 2;
    public static final int TRIPLE_SWITCH_ZOOM = 3;
    public static final int TRIPLE_SWITCH_NUM_ALL = 4;

    public static final int MSG_TRIPLE_SWITCH_INITVIEW = 0X001; 
    public static final int MSG_TRIPLE_SWITCH_ZOOM = 0X002;
    public static final int ZOOM_VIEW_LEVEL_UPDATE = 0X003;
    public static final int ZOOM_VIEW_LEVEL_UPDATE_END = 0X004;
    public static final int MODE_ASD = 4;

    
    private String mFacing;
    private CameraActivity activity;
    //private FrameLayout mContainer;
    private TextView mSwitcherNormal;
    private TextView mSwitcherMacro;
    private TextView mSwitcherWide;
    private ImageView img_selected_background;
    private int point_wide;
    private int point_normal;
    private int point_macro;
    private AnimationFactory mAnimationFactory;
    private String mLastRequestCameraId = "0";

    private static final String BACK_CAMERA_ID = "0";
    private static final String FRONT_CAMERA_ID = "1";
    private static String wide_camera_id = "2";
    private static String macro_camera_id = "3";

    private boolean mEnable = false;
    private long mTime = 0;
    private String currentCameraID;
    private List<String> mSupportValues = new ArrayList<String>();
    private String nextFacing = null;
    private String newCameraId = null;
    private boolean isMacroSupport = false;
    private boolean isWideSupport = false;
    private boolean isAutoMarginTop = false;
    private boolean isShowMacroToast = false;
    private ViewGroup mRootViewGroup;
    private View mContainer;
    private View mView;
    private TripleSwitchHorizontalScrollView mScroller;
    private SwitchHolder mSwitchHolder;
    private List<SwitchHolder> mSwitchHolders;
    private int mDefaultIndex = -1;
    private int mCurrentIndex = -1;
    private String mLastVlaue = CAMERA_FACING_BACK;
    private RotateStrokeTextView mText;
    private MainHandler mMainHandler;
    private IZoomConfig mIZoomConfig;
    private float mRatio = 1.0f;
    private float mLastRatio = 1.0f;
    private SlidingArcView mSlidingArcView;
    private IAppUi.HintInfo mMacroCameraHint;
    private static final int SHOW_INFO_LENGTH_LONG = 2000;
    //bv wuyonglin add for screen 2300px adjust all icon position 20201024 start
    private int mScreenHeight;
    //bv wuyonglin add for screen 2300px adjust all icon position 20201024 end
    //bv wuyonglin add for bug2771 20201031 start
    private TripleSwitchHorizontalAdapter adapter;
    //bv wuyonglin add for bug2771 20201031 end


    private static final String[] TRIPLE_SWITCH_VALUE = new String[TRIPLE_SWITCH_NUM_ALL];
    static {
        TRIPLE_SWITCH_VALUE[TRIPLE_SWITCH_MACRO] = CAMERA_FACING_MACRO;
        TRIPLE_SWITCH_VALUE[TRIPLE_SWITCH_WIDE] = CAMERA_FACING_WIDE;
        TRIPLE_SWITCH_VALUE[TRIPLE_SWITCH_NORMAL] = CAMERA_FACING_BACK;
        TRIPLE_SWITCH_VALUE[TRIPLE_SWITCH_ZOOM] = CAMERA_FACING_ZOOM;
    };

    private static final String[] TRIPLE_SWITCH_VALUE_RTL = new String[TRIPLE_SWITCH_NUM_ALL];
    static {
        TRIPLE_SWITCH_VALUE_RTL[TRIPLE_SWITCH_MACRO] = CAMERA_FACING_MACRO;
        TRIPLE_SWITCH_VALUE_RTL[TRIPLE_SWITCH_WIDE] = CAMERA_FACING_ZOOM;
        TRIPLE_SWITCH_VALUE_RTL[TRIPLE_SWITCH_NORMAL] = CAMERA_FACING_BACK;
        TRIPLE_SWITCH_VALUE_RTL[TRIPLE_SWITCH_ZOOM] = CAMERA_FACING_WIDE;
    };

    
    @Override
    public void init(IApp app, 
                    ICameraContext cameraContext,
                    SettingController settingController) {
        super.init(app, cameraContext, settingController);
        mMainHandler = new MainHandler(mApp.getActivity().getMainLooper());
        mFacing = mDataStore.getValue(KEY_TRIPLE_SWITCH, CAMERA_DEFAULT_FACING,  getStoreScope());
        int numOfCameras = Camera.getNumberOfCameras();
        LogHelper.i(TAG,"init mFacing = " + mFacing + " numOfCameras = " + numOfCameras);
        activity = (CameraActivity)mApp.getActivity();
        checkTripleSwitchSupportItems();          
        setValue(mFacing);
        mMainHandler.sendEmptyMessage(MSG_TRIPLE_SWITCH_INITVIEW);
        //bv wuyonglin add for screen 2300px adjust all icon position 20201024 start
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        mScreenHeight = dm.heightPixels;
        /* add by liangchangwei for Text Rotate begin */
        mApp.registerOnOrientationChangeListener(mOrientationListener);
        /* add by liangchangwei for Text Rotate end */
        //bv wuyonglin add for screen 2300px adjust all icon position 20201024 end
    }

    public void checkTripleSwitchSupportItems(){
        currentCameraID = mSettingController.getCameraId();
        wide_camera_id= SystemProperties.get("ro.hct_wide_angle_id","-1");
        macro_camera_id= SystemProperties.get("ro.hct_macroLens_id","-1");
        isMacroSupport = "-1".equals(macro_camera_id) ? false : true;
        isWideSupport =  "-1".equals(wide_camera_id) ? false : true;
        isShowMacroToast = activity.getResources().getBoolean(R.bool.config_triple_switch_macro_toast_support);

        mSupportValues = new ArrayList<String>();
        for(int i =0;i<TRIPLE_SWITCH_NUM_ALL;i++) {
            if(CAMERA_FACING_WIDE.equals(TRIPLE_SWITCH_VALUE[i])&&(!isWideSupport || isOnlyZoomSwitchSupport())){
                continue;
            }
            if(CAMERA_FACING_MACRO.equals(TRIPLE_SWITCH_VALUE[i])&&(!isMacroSupport || isOnlyZoomSwitchSupport())){
                continue;
            }
            mSupportValues.add(TRIPLE_SWITCH_VALUE[i]);
        }    
        setSupportedPlatformValues(mSupportValues);
        setSupportedEntryValues(mSupportValues);
        setEntryValues(mSupportValues);
        isShowMacroToast = activity.getResources().getBoolean(R.bool.config_triple_switch_macro_toast_support);
        isAutoMarginTop = activity.getResources().getBoolean(R.bool.config_triple_switch_auto_margin);
    }

    public void initView(){
        mRootViewGroup = mApp.getAppUi().getModeRootView();
        mSwitchHolders = new ArrayList<SwitchHolder>();
        for (int i =0;i<mSupportValues.size();i++ ) {
            int switchHolderId = 0;
            if(CameraUtil.isRTL){
                for(int j =0;j<TRIPLE_SWITCH_NUM_ALL;j++) {
                    if(TRIPLE_SWITCH_VALUE_RTL[j].equals(mSupportValues.get(i))){
                        switchHolderId = j;
                        break;
                    }
                }

            }else{
                for(int j =0;j<TRIPLE_SWITCH_NUM_ALL;j++) {
                    if(TRIPLE_SWITCH_VALUE[j].equals(mSupportValues.get(i))){
                        switchHolderId = j;
                        break;
                    }
                }
            }
            if(mFacing.equals(mSupportValues.get(i))){
                mCurrentIndex = i;
            }
            if(CAMERA_FACING_BACK.equals(mSupportValues.get(i))){
                mDefaultIndex = i;
            }
            mSwitchHolders.add(new SwitchHolder(switchHolderId,mSupportValues.get(i)));    
        }
        LogHelper.i(TAG,"initView mSwitchHolders.size = " + mSwitchHolders.size() + " mFacing = " + mFacing);
        adapter = new TripleSwitchHorizontalAdapter(mApp.getActivity(), mSwitchHolders,
                R.layout.triple_switch_horizontal_item);	//bv wuyonglin modify for bug2771 20201031

        mContainer = activity.getLayoutInflater().inflate(R.layout.triple_switch_horizontal_picker,
                    mRootViewGroup, true);
        mView = mContainer.findViewById(R.id.triple_switch_horizontal_layout);
        /* add by liangchangwei for Text Rotate begin */
        mText = (RotateStrokeTextView)mContainer.findViewById(R.id.triple_switch_select);
        CameraUtil.rotateRotateLayoutChildView(mApp.getActivity(), mText,
                mApp.getGSensorOrientation(), false);
        /* add by liangchangwei for Text Rotate end */
        //bv wuyonglin add for bug2376 20200929 start
        mText.setForceDarkAllowed(false);
        //bv wuyonglin add for bug2376 20200929 end
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mText.getLayoutParams();

        if((params.leftMargin == 0)&& !CameraUtil.isRTL){
            int leftMargin = (SlidingArcViewUtils.getScreenW() - mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.sliding_zoom_view_height))/2 ;
            LogHelper.i(TAG,"initview getScreenW = " + SlidingArcViewUtils.getScreenW() + " getWidth = " + mText.getWidth());
            if (mScreenHeight == 2300) {
                params.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.triple_switch_select_margin_bottom_2300px);
            } else if (mScreenHeight == 2400){
                params.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.triple_switch_select_margin_bottom_2400px);
            }
            params.setMargins(leftMargin, params.topMargin,params.rightMargin, params.bottomMargin);
            mText.setLayoutParams(params);
        }else if((params.rightMargin == 0) && CameraUtil.isRTL){
            int leftMargin = (SlidingArcViewUtils.getScreenW() - mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.sliding_zoom_view_height))/2 ;
            LogHelper.i(TAG,"initview getScreenW = " + SlidingArcViewUtils.getScreenW() + " getWidth = " + mText.getWidth());
            if (mScreenHeight == 2300) {
                params.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.triple_switch_select_margin_bottom_2300px);
            } else if (mScreenHeight == 2400){
                params.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.triple_switch_select_margin_bottom_2400px);
            }
            params.setMargins(params.leftMargin, params.topMargin,leftMargin, params.bottomMargin);
            mText.setLayoutParams(params);
        }

        if(mContainer == null){
            return;
        }else{
            mScroller = (TripleSwitchHorizontalScrollView) mContainer.findViewById(R.id.triple_switch_horizontal_scroller);
        }
        mScroller.setOnItemClickListener(mOnItemClickListener);
        mScroller.setContext(mApp.getAppUi());  //bv wuyonglin add for bug2771 20201031
        if (mScreenHeight == 2300) {
            RelativeLayout.LayoutParams mScrollerLayoutParams = (RelativeLayout.LayoutParams) mScroller.getLayoutParams();
            mScrollerLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_shutter_bar_height_2300px);
            mScroller.setLayoutParams(mScrollerLayoutParams);
        } else if (mScreenHeight == 2400) {
            RelativeLayout.LayoutParams mScrollerLayoutParams = (RelativeLayout.LayoutParams) mScroller.getLayoutParams();
            mScrollerLayoutParams.bottomMargin = mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.camera_shutter_bar_height_2400px);
            mScroller.setLayoutParams(mScrollerLayoutParams);
	}
        LogHelper.i(TAG,"currentCameraID = " + currentCameraID + " mLastRequestCameraId = " + mLastRequestCameraId + " mCurrentIndex = " + mCurrentIndex);
        if (mScroller != null) {
            mScroller.setAdapter(adapter, mApp);
            if(isNormalCameraSwitch()){
                mScroller.setSelectIndex(mDefaultIndex);
                resetmTextShow();
                mText.setText("1.0x");
                mText.setVisibility(View.VISIBLE);
            }else{
                mScroller.setSelectIndex(mCurrentIndex);
                if(mFacing == CAMERA_FACING_BACK){
                    resetmTextShow();
                    mText.setText("1.0x");
                    mText.setVisibility(View.VISIBLE);
                }
            }
            
        }
        //mRootViewGroup.addView(mContainer);
        mLastRequestCameraId = currentCameraID;
        mAppUi.setTripleSwitchListener(mTripleSwitchListener);
    }

    private int findCurrentIndex(){
        for(int i =0;i<TRIPLE_SWITCH_NUM_ALL;i++) {
            if(mFacing.equals(TRIPLE_SWITCH_VALUE[i])){
                return  i;
            }
        }
        return 0;
    }
    
   
    TripleSwitchHorizontalScrollView.OnItemClickListener mOnItemClickListener = new TripleSwitchHorizontalScrollView.OnItemClickListener() {
        @Override
        public void onItemClick(int index) {
            LogHelper.i(TAG,"onItemClick index = " + index + " mLastVlaue = " + mLastVlaue + " mFacing = " + mFacing);
            if((mLastVlaue == mFacing) && (mCurrentIndex == index)){
                return;
            }
            if(mAppUi.getShutterButtonManager().isEnabled() == false){
                LogHelper.i(TAG,"onItemClick getShutterButtonManager isEnabled == false");
                return;
            }
            //bv wuyonglin add for bug3751 20210224 start
            if (CameraUtil.isVideo_HDR_changing) {
                LogHelper.i(TAG, "[onItemClick], don't do camera id change for when isVideo_HDR_changing = true");
                return;
            }

            if(mAppUi.isHdrPictureProcessing()){
                LogHelper.i(TAG,"[onItemClick], don't do camera id change for when mCameraAppUI.isHdrPictureProcessing() = " + mAppUi.isHdrPictureProcessing());
                return;
            }
            //bv wuyonglin add for bug3751 20210224 end
            mLastVlaue = mFacing;
            String newFacing = mSupportValues.get(index);
            LogHelper.i(TAG,"newFacing = " + newFacing + " mCurrentIndex = " + mCurrentIndex);
            if(CAMERA_FACING_MACRO.equals(mLastVlaue) && CAMERA_FACING_MACRO.equals(newFacing)){
                LogHelper.d(TAG, "onItemClick + newFacing " + newFacing+"mLastVlaue:"+mLastVlaue);
                return;
            }
            mCurrentIndex = index;
            mScroller.setSelectIndex(mCurrentIndex);
            mFacing = newFacing;
            mDataStore.setValue(KEY_TRIPLE_SWITCH, mFacing,getStoreScope(), true);
            if(mApp.getAppUi().getZoomSliderUICtrl().isShowAll()){
                mApp.getAppUi().getZoomSliderUICtrl().slidingArcViewHide();
            }
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) mText.getLayoutParams();

            if(CAMERA_FACING_MACRO.equals(mFacing)){
                mApp.notifyCameraSelected(macro_camera_id);
                mText.setVisibility(View.INVISIBLE);
                if(isShowMacroToast){
                    String tips = activity.getResources().getString(R.string.macro_lens_camera_toast);;
                    showMacroCameraTips(tips);
                }
            }else if(CAMERA_FACING_WIDE.equals(mFacing)){
                int leftMargin = (SlidingArcViewUtils.getScreenW() - mText.getWidth())/2 - mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.triple_switch_horizontal_scroller_left_padding);
                mApp.notifyCameraSelected(wide_camera_id);
                //mText.setVisibility(View.INVISIBLE);
                mText.setVisibility(View.VISIBLE);
                mText.setText("0.6x");
                //modify by bv liangchangwei for fixbug 3518
                mRatio = 0.6f;
                mLastRatio = mRatio;
                if(!CameraUtil.isRTL){
                    params.setMargins(leftMargin, params.topMargin,
                            params.rightMargin, params.bottomMargin);
                }else{
                    params.setMargins(params.leftMargin, params.topMargin,leftMargin, params.bottomMargin);
                }
                mText.setLayoutParams(params);
            }else if(CAMERA_FACING_BACK.equals(mFacing)){
                int leftMargin = 0;
                //if(FRONT_CAMERA_ID.equals(mLastRequestCameraId)){
                //    leftMargin = (SlidingArcViewUtils.getScreenW() - mText.getWidth())/2 - mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.triple_switch_horizontal_scroller_left_padding);
                //}else{
                    leftMargin = (SlidingArcViewUtils.getScreenW() - mText.getWidth())/2 ;
                //}
                if(needTolCameraSwitch()){
                    //mText.setVisibility(View.INVISIBLE);
                    mApp.notifyCameraSelected(BACK_CAMERA_ID);
                    mText.setVisibility(View.VISIBLE);
                    mText.setText("1.0x");
                    mRatio = 1.0f;
                    mLastRatio = mRatio;
                }else{
/*
                    if(mRatio==2.0f){
                        mApp.getAppUi().getZoomSliderUICtrl().setZoomByValue("1.0");
                        mText.setVisibility(View.VISIBLE);
                        mText.setText("1.0");
                        mRatio = 1.0f;
                    }else{
                        mApp.getAppUi().getZoomSliderUICtrl().setZoomByValue("2.0");
                        mText.setVisibility(View.VISIBLE);
                        mText.setText("2.0");
                        mRatio = 2.0f;
                    }
*/
                    mApp.getAppUi().getZoomSliderUICtrl().setZoomByValue("1.0");
                    mText.setVisibility(View.VISIBLE);
                    mText.setText("1.0x");
                    mRatio = 1.0f;
                    mLastRatio = mRatio;
                }
                if(!CameraUtil.isRTL){
                    params.setMargins(leftMargin, params.topMargin,
                            params.rightMargin, params.bottomMargin);
                }else{
                    params.setMargins(params.leftMargin, params.topMargin,leftMargin, params.bottomMargin);
                }
                mText.setLayoutParams(params);
            }else if(CAMERA_FACING_ZOOM.equals(mFacing)){
                int leftMargin = 0;
                //if(FRONT_CAMERA_ID.equals(mLastRequestCameraId)){
                //    leftMargin = (SlidingArcViewUtils.getScreenW() - mText.getWidth())/2 ;
                //}else{
                    leftMargin = (SlidingArcViewUtils.getScreenW() - mText.getWidth())/2 + mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.triple_switch_horizontal_scroller_left_padding);
                //}
                mText.setText("2.0x");
                if(needTolCameraSwitch()){
                    mApp.notifyCameraSelected(BACK_CAMERA_ID);
                    mMainHandler.sendEmptyMessageDelayed(MSG_TRIPLE_SWITCH_ZOOM, 10);
                }else{
                    mMainHandler.sendEmptyMessageDelayed(MSG_TRIPLE_SWITCH_ZOOM, 100);
                }
                if(!CameraUtil.isRTL){
                    params.setMargins(leftMargin, params.topMargin,
                            params.rightMargin, params.bottomMargin);
                }else{
                    params.setMargins(params.leftMargin, params.topMargin,leftMargin, params.bottomMargin);
                }
                mText.setLayoutParams(params);
            }
        }

        @Override
        public void onLongItemClick(int index) {
            if(!CAMERA_FACING_MACRO.equals(mFacing) && mApp.getAppUi().getZoomSliderUICtrl().isShowAllSupport() && mApp.getAppUi().isZoomSlideSwitchSupport()){
                //configViewVisibility(View.GONE);
                mApp.getAppUi().getZoomSliderUICtrl().showAll();
            }
        }

        @Override
        public void onScroll(MotionEvent ev) {
            if(!CAMERA_FACING_MACRO.equals(mFacing) && mApp.getAppUi().getZoomSliderUICtrl().isShowAllSupport() && mApp.getAppUi().isZoomSlideSwitchSupport()){
                //configViewVisibility(View.GONE);
                if(mApp.getAppUi().getZoomSliderUICtrl().isShowAll()){
                    mApp.getAppUi().getZoomSliderUICtrl().getSlidingArcView().onTouchEvent(ev);
                }else{
                    /* -- modify by bv liangchangwei for fix bug--*/
                    if(!mApp.getAppUi().isHdrPictureProcessing()){
                        mApp.getAppUi().getZoomSliderUICtrl().showAll();
                    }
                    /* -- modify by bv liangchangwei for fix bug--*/
                }
            }
        }
    };



    @Override
    public PreviewStateCallback getPreviewStateCallback() {
        if(mSupportValues.size()<=1){
            return null;
        }

        mIZoomConfig = mApp.getAppUi().getZoomConfig();
        if(mIZoomConfig!=null){
            mIZoomConfig.setZoomSliderUpdateListener(this);

            //add by huangfei for zoom switch start
            if(mAppUi.isZoomSwitchSupportCameraId()){
                mIZoomConfig.setOnZoomSwitchListener(mZoomSwitchListener);
            }
            //add by huangfei for zoom switch end
        }
        mApp.getAppUi().getZoomSliderUI().setZoomSliderUIListener(this);
        return null;
    }

    @Override
    public void unInit() {
        mRatio = 1.0f;
        if(isNormalCameraSwitch()){
            if(mFacing == CAMERA_FACING_BACK || mFacing == CAMERA_FACING_ZOOM){
                mFacing = CAMERA_FACING_BACK;
                mDataStore.setValue(KEY_TRIPLE_SWITCH, mFacing,getStoreScope(), true);
            }
        }
        mLastRatio = mRatio;
        mRootViewGroup.removeView(mContainer);
        //add by huangfei for zoom switch start
        if(mAppUi.isZoomSwitchSupportCameraId()){
            mIZoomConfig.removeOnZoomSwitchListener(mZoomSwitchListener);
        }
        //add by huangfei for zoom switch end
        mApp.getAppUi().getZoomSliderUI().removeZoomSliderUIListener(this);
        /* add by liangchangwei for Text Rotate begin */
        mApp.unregisterOnOrientationChangeListener(mOrientationListener);
        /* add by liangchangwei for Text Rotate end */
        mContainer = null;
    }

    @Override
    public void postRestrictionAfterInitialized() {
    }

    @Override
    public void refreshViewEntry() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mContainer != null && !mAppUi.getShutterButtonManager().getIsShowMoreModeView()) {
                    /* add by liangchangwei for fix bug 2569 start */
                    if(mAppUi.isVideoRecording()){
                        return;
                    }
                    /* add by liangchangwei for fix bug 2569 end */
                    //bv wuyonglin add for bug2771 20201031 start
                    if (Integer.toString(CamcorderProfile.QUALITY_2160P).equals(mSettingController.queryValue("key_video_quality")) || "60".equals(mSettingController.queryValue("key_video_quality"))) {
                        mAppUi.updateIs4KVideo(true);
                    }
                    //bv wuyonglin add for bug2771 20201031 end
                    if (getEntryValues().size() <= 1) {
                        configViewVisibility(View.GONE);
                    } else {
                        if(mApp.getAppUi().getZoomSliderUICtrl().isShowAll()){
                            configViewVisibility(View.VISIBLE);
                        }else{
                            configViewVisibility(View.VISIBLE);
                        }
                    }
                    //bv wuyonglin add for bug2771 20201031 start
                    if ("Video".equals(mAppUi.getCurrentMode()) && mScroller.getScrollStrip().getChildCount() == 3) {
                        View child = mScroller.getScrollStrip().getChildAt(0);
                        if (mAppUi.is4KVideo()) {
                                        child.setVisibility(View.INVISIBLE);
                        } else {
                            if (mScroller.getSelectIndex() != 0) {
                                            child.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    //bv wuyonglin add for bug2771 20201031 end
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

    @Override
    public void hide() {
        //bv wuyonglin add for monkey test fc 202101018 start
        mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mText != null) {
                        mText.setVisibility(View.INVISIBLE);
                    }
                    if (mScroller != null) {
                        mScroller.setVisibility(View.INVISIBLE);
                    }
                }
            });
        //bv wuyonglin add for monkey test fc 202101018 end
    }

    @Override
    public void reset() {
        mText.setVisibility(View.VISIBLE);
        mScroller.setVisibility(View.VISIBLE);
        if(mFacing.equals(CAMERA_FACING_BACK) || mFacing.equals(CAMERA_FACING_ZOOM)){
            mFacing = CAMERA_FACING_BACK;
            if(mAppUi.getCameraId().equals("1")){
                mCurrentIndex = 0;
            }else{
                mCurrentIndex = 1;
            }
            mScroller.setSelectIndex(mCurrentIndex);
            mDataStore.setValue(KEY_TRIPLE_SWITCH, mFacing,getStoreScope(), true);
            mRatio = 1.0f;
            mLastRatio = mRatio;
            mText.setText(mRatio + "x");
            resetmTextShow();
        }else if(mFacing.equals(CAMERA_FACING_WIDE)){
            mText.setText("0.6x");
        }
    }

    private void resetmTextShow(){
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mText.getLayoutParams();
        int leftMargin = (SlidingArcViewUtils.getScreenW() - mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.triple_switch_icon_height))/2 ;

        if(!CameraUtil.isRTL){
            params.setMargins(leftMargin, params.topMargin,
                    params.rightMargin, params.bottomMargin);
        }else{
            params.setMargins(params.leftMargin, params.topMargin,leftMargin, params.bottomMargin);
        }
        mText.setLayoutParams(params);
    }

    @Override
    public void onScaleStatus(boolean isBegin) {
        LogHelper.i(TAG,"onScaleStatus isBegin = " + isBegin);
        if(!isBegin){
            if(isZoomSupport()){
                mMainHandler.sendEmptyMessageDelayed(ZOOM_VIEW_LEVEL_UPDATE_END, 1000);
            };
        }else{
            if(isZoomSupport()){
                if(mIZoomConfig!=null && mText.getText().toString().isEmpty()){
                    mText.setText(mIZoomConfig.getZoomLevel()+"x");
                }
                mText.setVisibility(View.VISIBLE);
                mScroller.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onZoomLevelUpdateNotify(String ratio) {
        LogHelper.i(TAG,"onZoomLevelUpdateNotify ratio = " + ratio);
        if(isZoomSupport()){
	    //bv wuyonglin add for bug1909 20200818 start
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mText.setVisibility(View.VISIBLE);
                    mScroller.setVisibility(View.VISIBLE);
                }
            });
	    //bv wuyonglin add for bug1909 20200818 end
            mLastRatio = mRatio;
            mRatio = Float.parseFloat(ratio);
            mMainHandler.obtainMessage(ZOOM_VIEW_LEVEL_UPDATE, ratio).sendToTarget();
        }
        LogHelper.i(TAG,"onZoomLevelUpdateNotify mRatio = " + mRatio + " mFacing = " + mFacing);
    }

    /* add by liangchangwei for Text Rotate begin */
    private IApp.OnOrientationChangeListener mOrientationListener =
    new IApp.OnOrientationChangeListener() {
        @Override
        public void onOrientationChanged(int orientation) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LogHelper.i(TAG,"onOrientationChanged orientation = " + orientation);
                    CameraUtil.rotateRotateLayoutChildView(mApp.getActivity(), mText,
                            orientation, true);
                    if(mScroller != null){
                        mScroller.onOrientationChanged(mApp.getActivity(), orientation);
                    }
                }
            });
        }
    };
    /* add by liangchangwei for Text Rotate end */
    private IAppUi.TripleSwitchListener mTripleSwitchListener = new IAppUi.TripleSwitchListener() {
        @Override
        public void onConfigUIVisibility(int visibility) {
            if(mContainer!=null && !mAppUi.getContentViewValue()){
                if (getEntryValues().size() > 1) {
                    mApp.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            configViewVisibility(visibility);
                        }
                    });
                }
            }
        }
    };

    @Override
    public void onZoomSliderReady(String ratio,float direciton) {
        mLastRatio = mRatio;
        mRatio = Float.parseFloat(ratio);
        if(isZoomSupport()){
            mText.setText(ratio+"x");
            if((mLastRatio < mRatio) && (mRatio >=2.0f) &&
                    (BACK_CAMERA_ID.equals(currentCameraID)||FRONT_CAMERA_ID.equals(currentCameraID)) &&
                    (mFacing == CAMERA_FACING_BACK)){
                onZoomFaceBackSwitchToFaceZoom(mRatio);
            }
            if((mLastRatio > mRatio) && (mRatio < 2.0f) &&
                    (BACK_CAMERA_ID.equals(currentCameraID)||FRONT_CAMERA_ID.equals(currentCameraID)) &&
                    (mFacing == CAMERA_FACING_ZOOM)){
                onZoomFaceZoomSwitchToFaceBack(mRatio);
            }
        }
    }

    @Override
    public void onSlidingArcViewHide(float ratio) {
        if(mSupportValues.size()<=1){
            return;
        }
        configViewVisibility(View.VISIBLE);
        if(ratio!=0.0f){
            mLastRatio = mRatio;
            mRatio = ratio;
        }
        showTripleSwitchHorizontal(mRatio);
    }

    public boolean needTolCameraSwitch(){
        if(CAMERA_FACING_MACRO.equals(mLastVlaue)||CAMERA_FACING_WIDE.equals(mLastVlaue)){
            return true;
        }
        return false;
    }

    public boolean isNormalCameraSwitch(){
        if((BACK_CAMERA_ID.equals(currentCameraID)||FRONT_CAMERA_ID.equals(currentCameraID))
                &&(BACK_CAMERA_ID.equals(mLastRequestCameraId)||FRONT_CAMERA_ID.equals(mLastRequestCameraId))){
            return true;
        }else{
            return false;
        }
    }

    public boolean isZoomSupport(){
        if(CAMERA_FACING_MACRO.equals(mFacing)){
            return false;
        }
        return true;
    }

    public void configViewVisibility(int visibility){
        mView.setVisibility(visibility);
        /* add by bv liangchangwei for fixbug 2011 start */
        if((visibility == View.GONE) || (visibility == View.INVISIBLE)){
            if(mApp.getAppUi().getZoomSliderUICtrl().isShowAll()){
                mApp.getAppUi().getZoomSliderUICtrl().slidingArcViewHideForce();
            }
        }
        /* add by bv liangchangwei for fixbug 2011 end */
    }

    public void showTripleSwitchHorizontal(float ratio){
        if (mScroller != null) {
            mScroller.setVisibility(View.VISIBLE);
            mText.setText(ratio+"x");
        }
    }

    public static class SwitchHolder {
        private int mSwitchHolderId;
        private String mSwitchHolderName;

        SwitchHolder(int id, String shutterName) {
            this.mSwitchHolderId = id;
            this.mSwitchHolderName = shutterName;
        }

        public String getSwitchHolderNam() {
            return mSwitchHolderName;
        }

        public int getSwitchHolderId() {
            return mSwitchHolderId;
        }
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TRIPLE_SWITCH_ZOOM:
                    mApp.getAppUi().getZoomSliderUICtrl().setZoomByValue("2.0");
                    mText.setVisibility(View.VISIBLE);
                    mText.setText("2.0x");
                    mRatio = 2.0f;
                    mLastRatio = mRatio;
                    LogHelper.i(TAG,"MSG_TRIPLE_SWITCH_ZOOM mRatio = " + mRatio);
                    break;
                case MSG_TRIPLE_SWITCH_INITVIEW:
                    LogHelper.i(TAG,"MSG_TRIPLE_SWITCH_INITVIEW mRatio = " + mRatio);
                    initView();
                    break;
                case ZOOM_VIEW_LEVEL_UPDATE:
                    LogHelper.i(TAG,"ZOOM_VIEW_LEVEL_UPDATE mRatio = " + mRatio + " mLastRatio = " + mLastRatio + " currentCameraID = " + currentCameraID + " mFacing = " + mFacing);
                    if(isZoomSupport()){
                        mMainHandler.removeMessages(ZOOM_VIEW_LEVEL_UPDATE_END);
                        //modify by bv liangchangwei for fixbug 3518
                        //mText.setText((String) msg.obj+"x");
                        mText.setText(mRatio+"x");
                        if((mLastRatio < mRatio) && (mRatio >=2.0f) &&
                                (BACK_CAMERA_ID.equals(currentCameraID)||FRONT_CAMERA_ID.equals(currentCameraID)) &&
                                (mFacing == CAMERA_FACING_BACK)){
                            onZoomFaceBackSwitchToFaceZoom(mRatio);
                        }
                        if((mLastRatio > mRatio) && (mRatio < 2.0f) &&
                                (BACK_CAMERA_ID.equals(currentCameraID)||FRONT_CAMERA_ID.equals(currentCameraID)) &&
                                (mFacing == CAMERA_FACING_ZOOM)){
                            onZoomFaceZoomSwitchToFaceBack(mRatio);
                        }
                    }
                    break;
                case ZOOM_VIEW_LEVEL_UPDATE_END:
                    LogHelper.i(TAG,"ZOOM_VIEW_LEVEL_UPDATE_END mRatio = " + mRatio);
                    mMainHandler.removeMessages(ZOOM_VIEW_LEVEL_UPDATE);
                    showTripleSwitchHorizontal(mRatio);
                    break;                     
                default:
                    break;
            }
        }
    }

    public boolean isOnlyZoomSwitchSupport(){
        //bv wuyonglin modify for bug2771 20201031 start
        if(!(BACK_CAMERA_ID.equals(currentCameraID)|| wide_camera_id.equals(currentCameraID)
                || macro_camera_id.equals(currentCameraID)) && !(BACK_CAMERA_ID.equals(currentCameraID) && mAppUi.getCurrentMode().equals("Video") && mAppUi.is4KVideo())){
        //bv wuyonglin modify for bug2771 20201031 end
            return true;
        }
        return false;
    }
    //private void changeZoomRatios

    public void showMacroCameraTips(String tips){
        mMacroCameraHint = new IAppUi.HintInfo();
        int id = mApp.getActivity().getResources().getIdentifier("hint_text_background",
                "drawable", mApp.getActivity().getPackageName());
        mMacroCameraHint.mBackground = mApp.getActivity().getDrawable(id);
        mMacroCameraHint.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
        mMacroCameraHint.mDelayTime = SHOW_INFO_LENGTH_LONG;
        mMacroCameraHint.mHintText = tips;
        mApp.getAppUi().showScreenHint(mMacroCameraHint);
    }

    //add by huangfei for zoom switch start
    private IZoomConfig.OnZoomSwitchListener mZoomSwitchListener = new IZoomConfig.OnZoomSwitchListener() {
        @Override
        public void onZoomSwitchByDecrease(String cameraId,float basicZoomRatio) {
            LogHelper.i(TAG,"onZoomSwitchByDecrease cameraId = " + cameraId + " basicZoomRatio = " + basicZoomRatio);
            int index = findCameraIdIndex(cameraId);
            mScroller.setSelectIndex(index);
            mDataStore.setValue(KEY_TRIPLE_SWITCH, mFacing,getStoreScope(), true);
            mCurrentIndex = index;
            //modify by bv liangchangwei for fixbug 3518
            int tRatio = (int)(basicZoomRatio*6);
            LogHelper.i(TAG," basicZoomRatio*6 = " + basicZoomRatio*6);
            mRatio = (float)(tRatio/10);
            LogHelper.i(TAG," 1 mRatio = " + mRatio);
            mRatio = (float)(Math.round(tRatio*10))/100;

            LogHelper.i(TAG,"2 mRatio = " + mRatio);

            //mRatio = tRatio/10;
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) mText.getLayoutParams();
            int leftMargin = (SlidingArcViewUtils.getScreenW() - mText.getWidth())/2 - mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.triple_switch_horizontal_scroller_left_padding);

            if(!CameraUtil.isRTL){
                params.setMargins(leftMargin, params.topMargin,
                        params.rightMargin, params.bottomMargin);
            }else{
                params.setMargins(params.leftMargin, params.topMargin,leftMargin, params.bottomMargin);
            }
            mText.setLayoutParams(params);
            mText.setText(mRatio+"x");
            LogHelper.i(TAG,"onZoomSwitchByDecrease mRatio = " + mRatio);
            //modify by bv liangchangwei for fixbug 3518
        }

        @Override
        public void onZoomSwitchByIncrease(String cameraId,float basicZoomRatio) {
            LogHelper.i(TAG,"onZoomSwitchByIncrease cameraId = " + cameraId + " basicZoomRatio = " + basicZoomRatio);
            int index = findCameraIdIndex(cameraId);
            mScroller.setSelectIndex(index);
            mDataStore.setValue(KEY_TRIPLE_SWITCH, mFacing,getStoreScope(), true);
            mCurrentIndex = index;
            //Log.d("TripleSwitchHorizontal", Log.getStackTraceString(new Throwable()));
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) mText.getLayoutParams();
            int leftMargin = (SlidingArcViewUtils.getScreenW() - mText.getWidth())/2 ;
            if(!CameraUtil.isRTL){
                params.setMargins(leftMargin, params.topMargin,
                        params.rightMargin, params.bottomMargin);
            }else{
                params.setMargins(params.leftMargin, params.topMargin,leftMargin, params.bottomMargin);
            }
            mText.setLayoutParams(params);
        }
    };


    public boolean isZoomSwitchSupportByDecrease(String currentId,String newId){
        if("0".equals(currentId)&& newId.equals(Config.getWideAngleId())){
            return true;
        }
        return false;
    }

    public boolean isZoomSwitchSupportByIncrease(String currentId,String newId){
        if(Config.getWideAngleId().equals(currentId)&& newId.equals("0")){
            return true;
        }
        return false;
    }

    public int findCameraIdIndex(String id){
        for (int i =0;i<mSupportValues.size();i++ ) {
            if(id.equals(wide_camera_id) &&CAMERA_FACING_WIDE.equals(mSupportValues.get(i))){
                mFacing = CAMERA_FACING_WIDE;
                return i;
            }
            if(id.equals(BACK_CAMERA_ID) &&CAMERA_FACING_BACK.equals(mSupportValues.get(i))){
                mFacing = CAMERA_FACING_BACK;
                return i;
            }
        }
        return 0;
    }

    public void onZoomFaceBackSwitchToFaceZoom(float basicZoomRatio){
        LogHelper.i(TAG,"++ onZoomFaceBackSwitchToFaceZoom mFacing = " + mFacing + " basicZoomRatio = " + basicZoomRatio);
        if(mFacing == CAMERA_FACING_ZOOM){
            return;
        }
        for (int i =0;i<mSupportValues.size();i++ ) {
            if(CAMERA_FACING_ZOOM.equals(mSupportValues.get(i))){
                mFacing = CAMERA_FACING_ZOOM;
                mCurrentIndex = i;
                break;
            }
        }
        mScroller.setSelectIndex(mCurrentIndex);
        mDataStore.setValue(KEY_TRIPLE_SWITCH, mFacing,getStoreScope(), true);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mText.getLayoutParams();
        int leftMargin = (SlidingArcViewUtils.getScreenW() - mText.getWidth())/2 + mApp.getActivity().getResources().getDimensionPixelSize(R.dimen.triple_switch_horizontal_scroller_left_padding) ;
        if(!CameraUtil.isRTL){
            params.setMargins(leftMargin, params.topMargin,
                    params.rightMargin, params.bottomMargin);
        }else{
            params.setMargins(params.leftMargin, params.topMargin,leftMargin, params.bottomMargin);
        }
        mText.setLayoutParams(params);
        mText.setText(basicZoomRatio +"x");
        LogHelper.i(TAG,"-- onZoomFaceBackSwitchToFaceZoom mFacing = " + mFacing );
    }

    public void onZoomFaceZoomSwitchToFaceBack(float basicZoomRatio){
        LogHelper.i(TAG,"++ onZoomFaceZoomSwitchToFaceBack mFacing = " + mFacing + " basicZoomRatio = " + basicZoomRatio);
        if(mFacing == CAMERA_FACING_BACK){
            return;
        }
        for (int i =0;i<mSupportValues.size();i++ ) {
            if(CAMERA_FACING_BACK.equals(mSupportValues.get(i))){
                mFacing = CAMERA_FACING_BACK;
                mCurrentIndex = i;
                break;
            }
        }
        mScroller.setSelectIndex(mCurrentIndex);
        mDataStore.setValue(KEY_TRIPLE_SWITCH, mFacing,getStoreScope(), true);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mText.getLayoutParams();
        int leftMargin = (SlidingArcViewUtils.getScreenW() - mText.getWidth())/2 ;
        if(!CameraUtil.isRTL){
            params.setMargins(leftMargin, params.topMargin,
                    params.rightMargin, params.bottomMargin);
        }else{
            params.setMargins(params.leftMargin, params.topMargin,leftMargin, params.bottomMargin);
        }
        mText.setLayoutParams(params);
        mText.setText(basicZoomRatio +"x");
        LogHelper.i(TAG,"-- onZoomFaceZoomSwitchToFaceBack mFacing = " + mFacing );
    }
    //add by huangfei for zoom switch end
}
