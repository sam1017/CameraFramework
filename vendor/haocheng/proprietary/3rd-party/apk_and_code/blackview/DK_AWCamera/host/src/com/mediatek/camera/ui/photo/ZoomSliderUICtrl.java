
package com.mediatek.camera.ui.photo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.mediatek.camera.Config;
import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.photo.view.IZoomSliderUI;
import com.mediatek.camera.feature.setting.zoom.IZoomConfig;
import com.mediatek.camera.ui.CircleTextView;
import com.mediatek.camera.ui.SlidingArcView;
import com.mediatek.camera.ui.SlidingArcViewUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class ZoomSliderUICtrl implements IZoomSliderUI, IZoomConfig.ZoomLevelSliderListener {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(ZoomSliderUICtrl.class.getSimpleName());

    private ViewGroup mRootViewGroup;
    private RelativeLayout mZoomView = null;
    private SlidingArcView mSlidingArcView = null;
    private CircleTextView mCircleTextView = null;

    private Context mContext;
    private static final int ZOOM_VIEW_INIT_AND_SHOW = 1;
    private static final int ZOOM_VIEW_SHOW_ALL = 2;
    private static final int ZOOM_VIEW_HIDE_ARC = 3;
    private static final int ZOOM_VIEW_LEVEL_UPDATE = 4;
    private static final int ZOOM_VIEW_SWITCH_UNINIT = 5;
    private static final int ZOOM_VIEW_UNINIT = 6;
    private static final int ZOOM_VIEW_SHOW_CIRCLETEXTVIEW = 7;
    private static final int ZOOM_VIEW_SHOW_MODE_SCROLL = 8;
    private boolean mFlag = false;
    ViewGroup.MarginLayoutParams mTextParam;
    ViewGroup.MarginLayoutParams mArcParam;

    private int mShutterBottom;
    private boolean isShow = false;
    private ArrayList<String> mZoomLevelValue = null;
    private int mLastZoomLevel = -1;

    private IApp mApp;
    private IZoomConfig mZoomConfig = null;
    private MainHandler mMainHandler;

    private CopyOnWriteArrayList<ZoomSliderUIListener> mListeners;
    private ZoomSliderUIListener mListener;
    private TranslateAnimation mTranslateAnimation;

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ZOOM_VIEW_INIT_AND_SHOW:
                    if(mZoomView == null){
                        initView();
                    }
                    break;
                case ZOOM_VIEW_SHOW_ALL:
                    showAll();
                    break;
                case ZOOM_VIEW_HIDE_ARC:
                    slidingArcViewHide(msg);
                    break;
                case ZOOM_VIEW_LEVEL_UPDATE:
                    changeZoomRatios((String) msg.obj);
                    break;
                case ZOOM_VIEW_UNINIT:
                    unInitView(false);
                    break;
                case ZOOM_VIEW_SWITCH_UNINIT:
                    unInitView(true);
                    break;
                case ZOOM_VIEW_SHOW_CIRCLETEXTVIEW:
                    if(msg.obj!=null){
                        mCircleTextView.setVisibility(View.INVISIBLE);
                    }else{
                        mCircleTextView.setVisibility(View.INVISIBLE);//VISIBLE bv modify
                    }
                    break;
                case ZOOM_VIEW_SHOW_MODE_SCROLL:
                    mApp.getAppUi().setUIVisibility(IAppUi.SHUTTER_ROOTLAYOUT, View.VISIBLE);
                    /* add by bv liangchangwei 20200828 fix bug 2016 start*/
                    mApp.getAppUi().onCameraSelected(mApp.getAppUi().getCameraId());
                    /* add by bv liangchangwei 20200828 fix bug 2016 end*/
                    break;
                default:
                    break;
            }
        }
    }

    public ZoomSliderUICtrl(IApp app, ViewGroup rootViewGroup){
        mApp = app;
        mRootViewGroup = rootViewGroup;
        mContext = mApp.getActivity();
        SlidingArcViewUtils.initScreen(app.getActivity());
        mListeners = new CopyOnWriteArrayList<ZoomSliderUIListener>();
    }

    @Override
    public void init() {
        mMainHandler = new MainHandler(mApp.getActivity().getMainLooper());
        mMainHandler.sendEmptyMessageDelayed(ZOOM_VIEW_INIT_AND_SHOW, 200);
    }

    @Override
    public void cameraSwitch(){
        if (isShow){
            mMainHandler.removeMessages(ZOOM_VIEW_HIDE_ARC);
            mMainHandler.removeMessages(ZOOM_VIEW_SHOW_MODE_SCROLL);
            mMainHandler.obtainMessage(ZOOM_VIEW_HIDE_ARC, true).sendToTarget();
            mMainHandler.obtainMessage(ZOOM_VIEW_SHOW_MODE_SCROLL, true).sendToTarget();
        }
        mMainHandler.sendEmptyMessage(ZOOM_VIEW_SWITCH_UNINIT);
    }

    @Override
    public void setZoomConfig(Object object){
        mZoomConfig = (IZoomConfig) object;
        if (mZoomConfig != null){
            mZoomConfig.setZoomSliderUpdateListener(this);

            if(!mApp.getAppUi().isZoomSwitchSupportCameraId()){
                resetCircleText();
            }
        }
        mZoomLevelValue = getZoomLevelValue();
        LogHelper.i(TAG, "ZoomSliderUi Config not empty(" + (mZoomConfig != null) +")");
    }

    @Override
    public void unInit() {
        if (isShow){
            mMainHandler.removeMessages(ZOOM_VIEW_HIDE_ARC);
            mMainHandler.removeMessages(ZOOM_VIEW_SHOW_MODE_SCROLL);
            mMainHandler.obtainMessage(ZOOM_VIEW_HIDE_ARC, true).sendToTarget();
            mMainHandler.obtainMessage(ZOOM_VIEW_SHOW_MODE_SCROLL, true).sendToTarget();
        }
        mMainHandler.sendEmptyMessage(ZOOM_VIEW_UNINIT);
    }

    @Override
    public void setZoomSliderUIListener(ZoomSliderUIListener zoomSliderUIListener) {

        if (!mListeners.contains(zoomSliderUIListener)) {
            mListeners.add(zoomSliderUIListener);
        }
    }


    @Override
    public void removeZoomSliderUIListener(ZoomSliderUIListener zoomSliderUIListener) {
        mListeners.remove(zoomSliderUIListener);
    }

    @Override
    public void onZoomLevelUpdateNotify(String ratio) {
        LogHelper.i(TAG, "onZoomLevelUpdateNotify ratio = " + ratio);
        mMainHandler.obtainMessage(ZOOM_VIEW_LEVEL_UPDATE, ratio).sendToTarget();
    }

    @Override
    public void hide() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void onScaleStatus(boolean isBegin) {
        
    }

    public void initView() {
        LogHelper.d(TAG, "initView +");

        mZoomView = (RelativeLayout) mApp.getActivity().getLayoutInflater()
                .inflate(R.layout.zoom_slidingarcview, mRootViewGroup, false).findViewById(R.id.zoom_custom_layout);
        mSlidingArcView = (SlidingArcView) mZoomView.findViewById(R.id.zoom_sliding_view);
        //bv wuyonglin add for bug2376 20200929 start
        mSlidingArcView.setForceDarkAllowed(false);
        //bv wuyonglin add for bug2376 20200929 end
        mCircleTextView = (CircleTextView) mZoomView.findViewById(R.id.zoom_sliding_value);
        mRootViewGroup.addView(mZoomView);
        if (mZoomLevelValue == null){
            mZoomLevelValue = getZoomLevelValue();
        }
        initSetting();
        mSlidingArcView.setZoomLevelValue(mZoomLevelValue);
        LogHelper.d(TAG, "initView - mSlidingArcView = " + mSlidingArcView);

        showCircleTextView();
    }

    private void initSetting() {

        mTextParam = (ViewGroup.MarginLayoutParams) mCircleTextView.getLayoutParams();
        mArcParam = (ViewGroup.MarginLayoutParams) mSlidingArcView.getLayoutParams();
        mShutterBottom = (int) mContext.getResources().getDimension(R.dimen.hct_shutter_layout_height);
        if (mZoomConfig != null){
            mCircleTextView.setText(mZoomConfig.getZoomLevel());
        }else {
            resetCircleText();
        }
        mCircleTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                //add by huangfei for continuousshot forbid zoom start
                if(getCsState()){
                    return false;
                }
                //add by huangfei for continuousshot forbid zoom end

                if (!isShow) mMainHandler.sendEmptyMessage(ZOOM_VIEW_SHOW_ALL);
                return false;
            }
        });
        mCircleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //add by huangfei for continuousshot forbid zoom start
                if(getCsState()){
                    return;
                }
                //add by huangfei for continuousshot forbid zoom end
                
                if ((mSlidingArcView.getVisibility() == View.GONE || mSlidingArcView.getVisibility() == View.INVISIBLE)
                        && mZoomConfig != null) {
                    mFlag = mZoomConfig.getZoomLevel().startsWith("2.0");
                    if (Config.isZoomRatios8Support(mContext)) {
                        changeZoomRatios(mFlag ? 0 : 4);
                    }else {
                        changeZoomRatios(mFlag ? 0 : 5);
                    }
                }
            }
        });
        mCircleTextView.setCircleTextTouchListener(new CircleTextView.CircleTextTouchListener() {
            @Override
            public void onScrollUp() {

                //add by huangfei for continuousshot forbid zoom start
                if(getCsState()){
                    return;
                }
                //add by huangfei for continuousshot forbid zoom end

                if (!isShow) mMainHandler.sendEmptyMessage(ZOOM_VIEW_SHOW_ALL);
            }
        });
        mSlidingArcView.setQtScrollListener(new SlidingArcView.QTScrollListener() {
            @Override
            public void onTouch(int index,float direction) {

                if (mLastZoomLevel != index) {
                     mLastZoomLevel = index;
                     for(ZoomSliderUIListener listener:mListeners) {
                         if(listener!=null){
                            listener.onZoomSliderReady(mZoomLevelValue.get(index),direction);
                         }
                     }
                    String ratioString = mZoomLevelValue.get(index);

                    float ratio  = Float.parseFloat(ratioString);
                    if(mApp.getAppUi().isZoomSwitchSupport() && Config.getWideAngleId().equals(mApp.getAppUi().getCameraId())
                             && ratio >0.9f){
                        setZoomRatios(""+0.9);
                    }else{
                        setZoomRatios(ratioString);
                    }
                }
            }

            @Override
            public void onFingerUp(boolean up) {
                LogHelper.i(TAG,"onFingerUp up = " + up);
                if (up) {
                    mMainHandler.removeMessages(ZOOM_VIEW_HIDE_ARC);
                    mMainHandler.sendEmptyMessageDelayed(ZOOM_VIEW_HIDE_ARC, 2000);
                } else {
                    mMainHandler.removeMessages(ZOOM_VIEW_HIDE_ARC);
                    mMainHandler.removeMessages(ZOOM_VIEW_SHOW_MODE_SCROLL);
                }
            }
        });
    }

    private void unInitView(boolean isSwitch){
        LogHelper.d(TAG, "unInitView +");
        if (mZoomView != null){
            mRootViewGroup.removeView(mZoomView);
        }
        mZoomView = null;
        if (isSwitch) {
            mZoomConfig = null;
        }
        LogHelper.d(TAG, "unInitView -");
    }

    private void resetCircleText(){

        //modify by huangfei for null point exception start
        //mCircleTextView.setText("1.0");
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
               if (mCircleTextView != null) {
                   mCircleTextView.setText("1.0");
               } else {
                   LogHelper.e(TAG, "mCircleTextView = " + mCircleTextView+"mZoomView = "+mZoomView);
               }
            }
        });
        //modify by huangfei for null point exception end
    }

    public void setZoomRatios(String ratios) {
        mCircleTextView.setText(ratios);
    }

    private void changeZoomRatios(String ratio) {
        LogHelper.d(TAG, "changeZoomRatios + ratio = " + ratio + " ++");
        //bv liangchangwei add for fix bug 1702 start
        if(mCircleTextView == null){
            return;
        }
        //bv liangchangwei add for fix bug 1702 end
        mCircleTextView.setText(ratio);
        float mRatio = Float.parseFloat(ratio);
        int index = 0;
        if (mRatio < 1.0f){
            if (mRatio >= 0.9f){
                index = 3;
            }else if (mRatio >= 0.8f){
                index = 2;
            }else if (mRatio >= 0.7f){
                index = 1;
            }else{
                index = 0;
            }
        }else {
            for (int i = 0; i < mZoomLevelValue.size(); i++) {
                String LevelValue = mZoomLevelValue.get(i);
                if (mRatio >= 1.0f && ratio.startsWith(LevelValue)) {
                    index = i;
                    break;
                }
            }
        }
        mSlidingArcView.flushViewsByIndex(index);
        LogHelper.d(TAG, "changeZoomRatios index = " + index + " --");
    }

    private void changeZoomRatios(int index) {
        mCircleTextView.setText(mZoomLevelValue.get(index));
        mSlidingArcView.flushViewsByIndex(index);
        for(ZoomSliderUIListener listener:mListeners) {
            listener.onZoomSliderReady(mZoomLevelValue.get(index),0);
        }
    }
    //add by bv liangchangwei for wide camera zoom start
    private float calculateWideAngleRatio(float ratio) {
        float realRatio = 1.0f;

        if(ratio <=1.0){
            realRatio = 0.6f;
        }else if(ratio <= 1.2f){
            realRatio = 0.7f;
        }else if(ratio <= 1.4f){
            realRatio = 0.8f;
        }else if(ratio <= 1.6f){
            realRatio = 0.9f;
        }else {
            realRatio = 1.0f;
        }
        LogHelper.i(TAG,"calculateWideAngleRatio ratio = " + ratio + " realRatio = " + realRatio);
        return realRatio;
    }
    //add by bv liangchangwei for wide camera zoom end

    public void showAll() {

        //Log.d("ZoomSliderUICtrl", Log.getStackTraceString(new Throwable()));
        //add by huangfei for continuousshot forbid zoom start
        if(isShow){
            return;
        }   
        //add by huangfei for continuousshot forbid zoom end

        //if(!showCircleTextView()){
        //    mZoomView.setVisibility(View.VISIBLE);
        //}
        LogHelper.d(TAG, "showAll +");
        isShow = true;
        mSlidingArcView.setVisibility(View.VISIBLE);
        if (mZoomConfig != null) {
            float ratio = mZoomConfig.getCurZoomRatio();
            //add by bv liangchangwei for wide camera zoom start
            LogHelper.d(TAG, "showAll ratio = " + ratio);
            if(Config.getWideAngleId().equals(mApp.getAppUi().getCameraId())){
                ratio = calculateWideAngleRatio(ratio);
                LogHelper.d(TAG, "showAll realRatio = " + ratio);
            }
            //add by bv liangchangwei for wide camera zoom end

            mZoomLevelValue = getZoomLevelValue();

            int index = 0;
            for(int i= 0; i< mZoomLevelValue.size(); i++){
                if(Float.parseFloat(mZoomLevelValue.get(i)) == ratio){
                    index = i;
                    mSlidingArcView.setChooseIndex(i);
                    break;
                }
            }
            mSlidingArcView.flushViewsByIndex(index);
/*
            float mRatio = Float.parseFloat(mZoomConfig.getZoomLevel());
            int index = 0;

            if(mApp.getAppUi().isZoomSwitchSupportCameraId()){
                 for (int i = 0; i < mZoomLevelValue.size(); i++) {
                     float LevelValue = Float.parseFloat(mZoomLevelValue.get(i));
                     if (LevelValue >=mRatio) {
                         index = i;
                         break;
                     }
                 }
            }else{
                if (mRatio < 2.0f){
                    if (mRatio >= 1.5f){
                        index = 3;
                    }else if (mRatio >= 1.3f){
                        index = 2;
                    }else if (mRatio >= 1.1f){
                        index = 1;
                    }
                }else {
                    for (int i = 0; i < mZoomLevelValue.size(); i++) {
                        float LevelValue = Float.parseFloat(mZoomLevelValue.get(i));
                        if (mRatio >= 2.0f && LevelValue >=mRatio) {
                            index = i;
                            break;
                        }
                    }
                }
             }
            if(!isShowCircleTextView()){
                mCircleTextView.setText(mRatio+"");
            }
            mSlidingArcView.flushViewsByIndex(index);
*/
        }else {
            if(!mApp.getAppUi().isZoomSwitchSupportCameraId()){
                mSlidingArcView.flushViewsByIndex(0);
            }else{
                mSlidingArcView.flushViewsByIndex(4);
            }
        }
        //mTextParam.setMargins(0, 0, 0, mShutterBottom + mSlidingArcView.getHeight()/3);
        mCircleTextView.setLayoutParams(mTextParam);
        //mCircleTextView.startAnimation(TranslateAnimation("show", false));
        mTranslateAnimation = getTranslateAnimation("show", false);
        mTranslateAnimation.setFillAfter(true);
        //mCircleTextView.startAnimation(mTranslateAnimation); //bv modify by liangchangwei
        mApp.getAppUi().setUIVisibility(IAppUi.SHUTTER_ROOTLAYOUT, View.GONE);
        mMainHandler.sendEmptyMessageDelayed(ZOOM_VIEW_HIDE_ARC, 3000);
        LogHelper.d(TAG, "showAll -");
    }

    private void slidingArcViewHide(Message msg) {
        LogHelper.d(TAG, "slidingArcViewHide +");

        //add by huangfei for continuousshot forbid zoom start
        if(!isShow){
            return;
        }   
        //add by huangfei for continuousshot forbid zoom end      

        isShow = false;
        boolean isSwitch = msg.obj != null;
/*        Animation alphaAnimation = AnimationUtils.loadAnimation(mContext, R.anim.zoom_sliding_exit);
        LogHelper.d(TAG, "slidingArcViewHide + mSlidingArcView = " + mSlidingArcView);
        mSlidingArcView.startAnimation(alphaAnimation);
        //mCircleTextView.startAnimation(TranslateAnimation("hide", isSwitch));
        mTranslateAnimation.setFillAfter(false);*/
        LogHelper.i(TAG,"mSlidingArcView set INVISIBLE");
        mSlidingArcView.setVisibility(View.INVISIBLE);

        /* remove by bv liangchangwei for fixbug 2047 20200904 start -*/
        /*float mRatio = 0.0f;
        if(mLastZoomLevel!=-1){
            mRatio = Float.parseFloat(mZoomLevelValue.get(mLastZoomLevel));
        }
        for(ZoomSliderUIListener listener:mListeners) {
            listener.onSlidingArcViewHide(mRatio);
        }*/
        /* remove by bv liangchangwei for fixbug 2047 20200904 end -*/

        //mTextParam.setMargins(0, 0, 0, mShutterBottom);
        //mCircleTextView.setLayoutParams(mTextParam);
        LogHelper.d(TAG, "slidingArcViewHide -");
        mApp.getAppUi().setZoomSwitchPreviousSpan(0, 0);
        //mMainHandler.sendEmptyMessageDelayed(ZOOM_VIEW_SHOW_MODE_SCROLL, 500);
        mMainHandler.removeMessages(ZOOM_VIEW_HIDE_ARC);
        mMainHandler.obtainMessage(ZOOM_VIEW_SHOW_MODE_SCROLL, true).sendToTarget();
    }

    private TranslateAnimation getTranslateAnimation(String type, boolean isSwitch){
        float fromYDelta;
        float toYDelta;
        if (type.equals("show")){
            fromYDelta = 0;
            toYDelta = mCircleTextView.getHeight()/2 - SlidingArcViewUtils.getScreenW() / 2 + 110;
        }else{
            fromYDelta = mCircleTextView.getHeight()/2 - SlidingArcViewUtils.getScreenW() / 2 + 110;
            toYDelta = 0;
        }
        TranslateAnimation translate = new TranslateAnimation(0, 0, fromYDelta, toYDelta);
        translate.setDuration(500);
        return translate;
    }

    //add by huangfei for continuousshot forbid zoom start
    private boolean getCsState(){
        return mApp.getAppUi().getCsState();
    }

    public void slidingArcViewHide() {
        if(mSlidingArcView == null && mCircleTextView == null){
            return;
        }
        LogHelper.d(TAG, "slidingArcViewHide +");
/*        Animation alphaAnimation = AnimationUtils.loadAnimation(mContext, R.anim.zoom_sliding_exit);
        mSlidingArcView.startAnimation(alphaAnimation);
        mTranslateAnimation.setFillAfter(false);*/
        mSlidingArcView.setVisibility(View.INVISIBLE);
        LogHelper.d(TAG, "slidingArcViewHide -");
        isShow = false;
        mApp.getAppUi().setZoomSwitchPreviousSpan(0, 0);
        mMainHandler.removeMessages(ZOOM_VIEW_HIDE_ARC);
        mMainHandler.obtainMessage(ZOOM_VIEW_SHOW_MODE_SCROLL, true).sendToTarget();
        //mMainHandler.sendEmptyMessageDelayed(ZOOM_VIEW_SHOW_MODE_SCROLL, 500);
    }

    /* add by bv liangchangwei for fixbug 2011 start */
    public void slidingArcViewHideForce() {
        if(mSlidingArcView == null && mCircleTextView == null){
            return;
        }
        LogHelper.d(TAG, "slidingArcViewHideForce +");
/*        Animation alphaAnimation = AnimationUtils.loadAnimation(mContext, R.anim.zoom_sliding_exit);
        mSlidingArcView.startAnimation(alphaAnimation);
        mTranslateAnimation.setFillAfter(false);*/
        mSlidingArcView.setVisibility(View.INVISIBLE);
        isShow = false;
        mMainHandler.removeMessages(ZOOM_VIEW_HIDE_ARC);
        LogHelper.d(TAG, "slidingArcViewHideForce -");
    }
    /* add by bv liangchangwei for fixbug 2011 end */

    public boolean getSlidingArcViewState(){
        return isShow;
    }
    //add by huangfei for continuousshot forbid zoom end

    public void setZoomByValue(String ratio){
        for(ZoomSliderUIListener listener:mListeners) {
            listener.onZoomSliderReady(ratio, 0);
        }
        changeZoomRatios(ratio);
    }

    public void showCircleTextView(){
        if(mCircleTextView == null){
            return;
        }
        getZoomLevelValue();
        if(mApp.getAppUi().getBasicZoomRatio()==0 && isShow){
            slidingArcViewHide();
        }
        mMainHandler.removeMessages(ZOOM_VIEW_SHOW_CIRCLETEXTVIEW);
        if(isShowCircleTextView()){
            mMainHandler.sendEmptyMessageDelayed(ZOOM_VIEW_SHOW_CIRCLETEXTVIEW, 800);
        }else{
            mMainHandler.obtainMessage(ZOOM_VIEW_SHOW_CIRCLETEXTVIEW, true).sendToTarget();
        }
        mSlidingArcView.setZoomLevelValue(mZoomLevelValue);
    }

    public boolean isShowCircleTextView(){
        if(Config.isTripleSwitchHorizontalSupport(mContext)&&!"1".equals(mApp.getAppUi().getCameraId())){
            return false;
        }
        return true;
    }

    public interface ZoomSliderUICtrlrListener {
        public void onSlidingArcViewHide(String ratio);
    }

    public void setZoomSliderUICtrlrListener(ZoomSliderUICtrlrListener ZoomSliderUICtrlrListener){
        
    }

    public boolean isShowAllSupport(){
        if(mZoomView==null){
            return false;
        }
        return true;
    }

    public boolean isShowAll(){
        return isShow;
    }

    public ArrayList<String> getZoomLevelValue(){
        mZoomLevelValue = new ArrayList<>();
        mZoomLevelValue.clear();
        if(!mApp.getAppUi().isZoomSwitchSupportCameraId()){
            if (Config.isZoomRatios8Support(mContext)) {
                mZoomLevelValue.addAll(Arrays.asList(mContext.getResources().getStringArray(R.array.zoom_ratios_8x)));
            }else{
                mZoomLevelValue.addAll(Arrays.asList(mContext.getResources().getStringArray(R.array.zoom_ratios_default)));
            }
            if(mSlidingArcView != null){
                mSlidingArcView.setChooseIndex(0);
            }
        }else{
            if (Config.isZoomRatios8Support(mContext)) {
                mZoomLevelValue.addAll(Arrays.asList(mContext.getResources().getStringArray(R.array.zoom_switch_ratio_8X)));
            }else if("Video".equals(mApp.getAppUi().getCurrentMode())&&mApp.getAppUi().is4KVideo()){
                mZoomLevelValue.addAll(Arrays.asList(mContext.getResources().getStringArray(R.array.zoom_switch_ratio_10x_no_triple)));
            }else{
                mZoomLevelValue.addAll(Arrays.asList(mContext.getResources().getStringArray(R.array.zoom_switch_ratio_10x_default)));
            }
            if(mSlidingArcView != null){
                if("Video".equals(mApp.getAppUi().getCurrentMode()) && mApp.getAppUi().is4KVideo()){
                    mSlidingArcView.setChooseIndex(0);
                }else{
                    mSlidingArcView.setChooseIndex(4);
                }
            }
        }
        if(mSlidingArcView != null){
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSlidingArcView.setIndexOfView(mZoomLevelValue.size());
                }
            });
        }
        return mZoomLevelValue;
    }

    public SlidingArcView getSlidingArcView(){
        return mSlidingArcView;
    }
}
