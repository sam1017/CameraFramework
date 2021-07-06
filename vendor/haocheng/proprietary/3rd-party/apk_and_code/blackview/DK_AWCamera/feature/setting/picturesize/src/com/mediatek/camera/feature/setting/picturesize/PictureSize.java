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
package com.mediatek.camera.feature.setting.picturesize;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.portability.SystemProperties;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.camera.R;

//*/ hct.huangfei, 20201024. add picture size ratio(18:9).
import com.mediatek.camera.Config;
//*/
//bv wuyonglin add for PictureSize quickSwitch 20191224 start
import javax.annotation.Nonnull;
//bv wuyonglin add for PictureSize quickSwitch 20191224 end
//bv wuyonglin add for setting ui 20200923 start
import com.mediatek.camera.common.IAppUi;
//bv wuyonglin add for setting ui 20200923 end

/**
 * Picture size setting item.
 *
 */
public class PictureSize extends SettingBase implements
        PictureSizeSettingView.OnValueChangeListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(PictureSize.class.getSimpleName());
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private static final double DEGRESSIVE_RATIO = 0.5;
    private static final int MAX_COUNT = 3;
    private static final String FILTER_PICTURE_SIZE = "vendor.mtk.camera.app.filter.picture.size";
    private static boolean sFilterPictureSize =
            SystemProperties.getInt(FILTER_PICTURE_SIZE, 1) == 1;

    private ISettingChangeRequester mSettingChangeRequester;
    private PictureSizeSettingView mSettingView;
    private String mModeKey;
    private String mLastModeKey;

    private List<String> mYUVsupportedSize;
    private boolean isOverrideValues = false;	//bv wuyonglin add for open camera PictureSize quickSwitch first show 20191226
    //bv wuyonglin add for some quickSwitch icon posistion change quickly 20200113 start
    private boolean mFromPanoramaModeClosed = false;
    private String mModeKeyMode = "com.mediatek.camera.feature.mode.panorama.PanoramaMode";
    //bv wuyonglin add for some quickSwitch icon posistion change quickly 20200113 end
	
	//add by huangfei for default preview ratio start
	private double defaultRatio = 4d / 3;
	private int defaultRatioVlaue = 0;
	//add by huangfei for default preview ratio end
    private static final int PICTURE_SIZE_9M_WIDTH = 4096;
    private static final int PICTURE_SIZE_9M_HEIGHT = 2304;
    private static final String VFB_MODE
            = "com.mediatek.camera.feature.mode.vfacebeauty.VendorFaceBeautyMode";
    private static final String FB_MODE
            = "com.mediatek.camera.feature.mode.facebeauty.FaceBeautyMode";
    private static final String FILTER_MODE = "com.mediatek.camera.feature.mode.matrix.MatrixMode";
    private static final String HDR_MODE = "com.mediatek.camera.feature.mode.hdr.HdrMode";
    private static final String AIBEAUTYPHOTO_MODE = "com.mediatek.camera.feature.mode.aicombo.photo.AIBeautyPhotoMode";
    private static final String AIBOKEHPHOTO_MODE = "com.mediatek.camera.feature.mode.aicombo.photo.AIBokehPhotoMode";
    private static final String AICOLORPHOTO_MODE = "com.mediatek.camera.feature.mode.aicombo.photo.AIColorPhotoMode";
    private static final String AILEGGYPHOTO_MODE = "com.mediatek.camera.feature.mode.aicombo.photo.AILeggyPhotoMode";
    private static final String AISLIMMINGPHOTO_MODE = "com.mediatek.camera.feature.mode.aicombo.photo.AISlimmingPhotoMode";

    //*/ hct.huangfei, 20201024. picture size show numbers.
    private int mMaxCount = 3;
    //*/
	//add by huangfei for  picture size ratio 1:1 start
    public static final String PICTURE_SIZE_1_1 = "picture_size_1_1";
    //add by huangfei for  picture size ratio 1:1 end

	//add by huangfei for zoom switch start
	private static final String KEY_PREVIEW_RATIO = "key_preview_ratio";
	//add by huangfei for zoom switch end

    private PictureSizeViewController mPictureSizeViewController;	//bv wuyonglin add for PictureSize quickSwitch 20191224
    //bv wuyonglin add for setting ui 20200923 start
    private String defaultValueInStore = "";
    //bv wuyonglin add for setting ui 20200923 end

    //bv wuyonglin add for bug5742 20210429 start

    private static final String KEY_VIDEO_MODE = "com.mediatek.camera.common.mode.video.VideoMode";

    private static final String KEY_SLOW_MOTION_MODE = "com.mediatek.camera.feature.mode.slowmotion.SlowMotionMode";

    //bv wuyonglin add for bug5742 20210429 end

    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     SettingController settingController) {
        super.init(app, cameraContext, settingController);

        //add by huangfei for default preview ratio start
        defaultRatioVlaue = mActivity.getResources().getInteger(R.integer.default_preview_ratio);
        //add by huangfei for default preview ratio end
        //bv wuyonglin add for PictureSize quickSwitch 20191224 start
        String value = mDataStore.getValue(KEY_PICTURE_SIZE, null, getStoreScope());
        setValue(value);
        if (mPictureSizeViewController == null) {
            mPictureSizeViewController = new PictureSizeViewController(this, app);
        }
        //bv wuyonglin add for PictureSize quickSwitch 20191224 end
        //bv wuyonglin add for setting ui 20200923 start
/*        if (getCurrentMode() != null && !getCurrentMode().equals("Video") && !getCurrentMode().equals("SlowMotion")) {
        mAppUi.setRestoreSettingListener(mRestoreSettingListener);
        }*/
        //bv wuyonglin add for setting ui 20200923 end
        
        //*/ hct.huangfei, 20201024. picture size show numbers.
        mMaxCount = mActivity.getResources().getInteger(R.integer.picture_size_numbers_show);;
        //*/

    }

    @Override
    public void unInit() {
        //bv wuyonglin add for setting ui 20200923 start
        mAppUi.removeRestoreSettingListener(mRestoreSettingListener);
        //bv wuyonglin add for setting ui 20200923 end
    }

    @Override
    public void addViewEntry() {
        if (mSettingView == null) {
            mSettingView = new PictureSizeSettingView(getKey());
            mSettingView.setOnValueChangeListener(this);
        }
        mAppUi.addSettingView(mSettingView);
        //bv wuyonglin add for PictureSize quickSwitch 20191224 start
	//bv wuyonglin add for open camera PictureSize quickSwitch first show 20191226 start
        LogHelper.d(TAG, "[addViewEntry] getvalue="+getValue() +"mPictureSizeViewController ="+mPictureSizeViewController+" isOverrideValues ="+isOverrideValues);
        //bv wuyonglin add for some quickSwitch icon posistion change quickly 20200113 start
        if (getEntryValues().size() > 1) {
        //bv wuyonglin add for some quickSwitch icon posistion change quickly 20200113 end
        if (getValue() != null && isOverrideValues) {    //bv wuyonglin add for PictureSize quickSwitch null object reference 20191225
	//bv wuyonglin add for open camera PictureSize quickSwitch first show 20191226 end
        mPictureSizeViewController.addQuickSwitchIcon();
        mPictureSizeViewController.showQuickSwitchIcon(getEntryValues().size() > 1);
	isOverrideValues = false;	//bv wuyonglin add for open camera PictureSize quickSwitch first show 20191226
        }    //bv wuyonglin add for PictureSize quickSwitch null object reference 20191225
        //bv wuyonglin add for some quickSwitch icon posistion change quickly 20200113 start
        mFromPanoramaModeClosed = false;
        } else {
            if (mFromPanoramaModeClosed) {
                mPictureSizeViewController.addQuickSwitchIcon();
                mPictureSizeViewController.showQuickSwitchIcon(true);
                mFromPanoramaModeClosed = false;
            } else {
                mPictureSizeViewController.removeQuickSwitchIcon();
            }
        }
        //bv wuyonglin add for some quickSwitch icon posistion change quickly 20200113 end
        //bv wuyonglin add for PictureSize quickSwitch 20191224 end
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
        //bv wuyonglin add for PictureSize quickSwitch 20191224 start
        LogHelper.i(TAG, "[removeViewEntry] getvalue="+getValue());
        mPictureSizeViewController.removeQuickSwitchIcon();
        //bv wuyonglin add for PictureSize quickSwitch 20191224 end
        //bv wuyonglin add for some quickSwitch icon posistion change quickly 20200113 start
        mFromPanoramaModeClosed = false;
        //bv wuyonglin add for some quickSwitch icon posistion change quickly 20200113 end
    }

    @Override
    public void refreshViewEntry() {
        if (mSettingView != null) {
            mSettingView.setValue(getValue());
            LogHelper.i(TAG, "[refreshViewEntry] getEntryValues()="+getEntryValues());
            mSettingView.setEntryValues(getEntryValues());
            mSettingView.setEnabled(getEntryValues().size() > 1);
        }
        //bv wuyonglin add for PictureSize quickSwitch 20191224 start
        int size = getEntryValues().size();
            LogHelper.d(TAG, "[refreshViewEntry], size ="+size);
        if (size <= 1) {
            mPictureSizeViewController.showQuickSwitchIcon(false);
        } else {
            mPictureSizeViewController.showQuickSwitchIcon(true);
        }
        //bv wuyonglin add for PictureSize quickSwitch 20191224 end
    }

    //bv wuyonglin add for PictureSize quickSwitch 20191224 start
    @Override
    public void overrideValues(@Nonnull String headerKey, String currentValue,
                               List<String> supportValues) {
        LogHelper.d(TAG, "[overrideValuespj] headerKey = " + headerKey
                + " ,currentValue = " + currentValue + ",supportValues = " + supportValues+" getEntryValues().size() ="+getEntryValues().size());
        //mPictureSizeViewController.hidePictureSizeChoiceView();	//bv wuyonglin detele for optimize touch blank area quick switcher option can hide 2020025
	isOverrideValues = true;	//bv wuyonglin add for open camera PictureSize quickSwitch first show 20191226
    }
    //bv wuyonglin add for PictureSize quickSwitch 20191224 end

    @Override
    public void postRestrictionAfterInitialized() {

    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO;
    }

    @Override
    public String getKey() {
        return KEY_PICTURE_SIZE;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mSettingChangeRequester == null) {
            PictureSizeCaptureRequestConfig captureRequestConfig
                    = new PictureSizeCaptureRequestConfig(this, mSettingDevice2Requester);
            mSettingChangeRequester = captureRequestConfig;
        }
        return (PictureSizeCaptureRequestConfig) mSettingChangeRequester;
    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        LogHelper.d(TAG, "[onModeOpened] modeKey = " + modeKey);
        mModeKey = modeKey;
        //bv wuyonglin add for bug5742 20210429 start

        if (!KEY_VIDEO_MODE.equals(modeKey) && !KEY_SLOW_MOTION_MODE.equals(modeKey)) {

            mAppUi.setRestoreSettingListener(mRestoreSettingListener);

        }

        //bv wuyonglin add for bug5742 20210429 end
    }

    public void setYUVSupportSize(List<String> supportedPictureSize){
        mYUVsupportedSize=supportedPictureSize;
    }

    //bv wuyonglin add for PictureSize quickSwitch 20191224 start
    @Override
    public void onModeClosed(String modeKey) {
        LogHelper.d(TAG, "onModeClosed modeKey :" + modeKey);
        mLastModeKey = modeKey;
        mPictureSizeViewController.hidePictureSizeChoiceView();
        //bv wuyonglin add for some quickSwitch icon posistion change quickly 20200113 start
        if (mModeKeyMode.equals(modeKey)) {
            String valueInStore = mDataStore.getValue(getKey(), null, getStoreScope());
            setValue(valueInStore);
            mFromPanoramaModeClosed = true;
        }
        //bv wuyonglin add for some quickSwitch icon posistion change quickly 20200113 end
        super.onModeClosed(modeKey);
    }
    //bv wuyonglin add for PictureSize quickSwitch 20191224 end

    //add by huangfei for zoom switch start
    private double getRatio(String value){
        try {
           return  Double.parseDouble(value);
        } catch (Exception e) {
            return PictureSizeHelper.RATIO_4_3;
        }
    }
    //add by huangfei for zoom switch end

    /**
     * Invoked after setting's all values are initialized.
     *
     * @param supportedPictureSize Picture sizes which is supported in current platform.
     */
    public void onValueInitialized(List<String> supportedPictureSize) {
        LogHelper.d(TAG, "[onValueInitialized], supportedPictureSize:" + supportedPictureSize);

        double fullRatio = PictureSizeHelper.findFullScreenRatio(mActivity);
        List<Double> desiredAspectRatios = new ArrayList<>();
        desiredAspectRatios.add(fullRatio);

        //*/ hct.huangfei, 20201024. add picture size ratio(18:9).
        if (Config.isCameraRatio18_9Support(mActivity)){
            desiredAspectRatios.add(PictureSizeHelper.RATIO_16_9);
        }
        //*/
        //add by huangfei for  picture size ratio 1:1 start
        if (Config.isCameraRatio_1_1_Support(mActivity)){
            desiredAspectRatios.add(PictureSizeHelper.RATIO_1_1);
        }
        //add by huangfei for  picture size ratio 1:1 end		
        desiredAspectRatios.add(PictureSizeHelper.RATIO_4_3);
        PictureSizeHelper.setDesiredAspectRatios(desiredAspectRatios);

        //*/ hct.huangfei, 20201024. picture size show numbers.
        //PictureSizeHelper.setFilterParameters(DEGRESSIVE_RATIO, MAX_COUNT);
        PictureSizeHelper.setFilterParameters(DEGRESSIVE_RATIO, mMaxCount);
        //*/

        if (sFilterPictureSize) {
            //*/ hct.huangfei, 20201211.numbers of picture size.
            //supportedPictureSize = PictureSizeHelper.filterSizes(supportedPictureSize);
            supportedPictureSize = PictureSizeHelper.filterSizes(supportedPictureSize,mActivity);
            //*/
            LogHelper.i(TAG, "[onValueInitialized], after filter, supportedPictureSize = "
                    + supportedPictureSize+" PictureSizeHelper.getMaxTexureSize() ="+PictureSizeHelper.getMaxTexureSize());
        }
        if (FILTER_MODE.equals(mModeKey)
                || VFB_MODE.equals(mModeKey)
                || FB_MODE.equals(mModeKey)) {
            //for low rom
            if ((VFB_MODE.equals(mModeKey)||FILTER_MODE.equals(mModeKey))
                    && isLowRam()) {
                List<String> supportedPictureSizeAfterCheck = new ArrayList<String>();
                for (String pictureSize : supportedPictureSize) {
                    String[] size = pictureSize.split("x");
                    int width = Integer.parseInt(size[0]);
                    int height = Integer.parseInt(size[1]);
                    if (width < PICTURE_SIZE_9M_WIDTH
                            && height < PICTURE_SIZE_9M_HEIGHT) {
                        supportedPictureSizeAfterCheck.add(pictureSize);
                    }
                }
                supportedPictureSize = supportedPictureSizeAfterCheck;
                LogHelper.d(TAG, "[onValueInitialized], low ram, after check, " +
                        "supportedPictureSize:"
                        + supportedPictureSize);
            } else {
                List<String> supportedPictureSizeAfterCheck = new ArrayList<String>();
                for (String pictureSize : supportedPictureSize) {
                    String[] size = pictureSize.split("x");
                    int width = Integer.parseInt(size[0]);
                    int height = Integer.parseInt(size[1]);
                    if (width <= PictureSizeHelper.getMaxTexureSize()
                            && height <= PictureSizeHelper.getMaxTexureSize()) {
                        supportedPictureSizeAfterCheck.add(pictureSize);
                    }
                }
                supportedPictureSize = supportedPictureSizeAfterCheck;
                LogHelper.d(TAG, "[onValueInitialized], GPU Mode, after check, " +
                        "supportedPictureSize:"
                        + supportedPictureSize);
            }
        }
        if (HDR_MODE.equals(mModeKey)) {
            List<String> supportedPictureSizeAfterCheck = new ArrayList<String>();
            for (String pictureSize : supportedPictureSize) {
                for (String yuvSize:mYUVsupportedSize){
                    if(pictureSize.equals(yuvSize)){
                        supportedPictureSizeAfterCheck.add(pictureSize);
                    }
                }
            }
            supportedPictureSize=supportedPictureSizeAfterCheck;
            LogHelper.d(TAG, "[onValueInitialized], PostAlgo Mode, after check, supportedPictureSize:"
                    + supportedPictureSize);
        }
        if (AIBEAUTYPHOTO_MODE.equals(mModeKey)
                || AIBOKEHPHOTO_MODE.equals(mModeKey)
                || AICOLORPHOTO_MODE.equals(mModeKey)
                || AILEGGYPHOTO_MODE.equals(mModeKey)
                || AISLIMMINGPHOTO_MODE.equals(mModeKey)) {
            List<String> supportedPictureSizeAfterCheck = new ArrayList<String>();
            for (String pictureSize : supportedPictureSize) {
                    String[] size = pictureSize.split("x");
                    int width = Integer.parseInt(size[0]);
                    int height = Integer.parseInt(size[1]);
                    if (width <= PictureSizeHelper.getMaxTexureSize()
                            && height <= PictureSizeHelper.getMaxTexureSize()) {
                        supportedPictureSizeAfterCheck.add(pictureSize);
                    }
            }
            supportedPictureSize = supportedPictureSizeAfterCheck;
            LogHelper.d(TAG, "[onValueInitialized], mModeKey:" + mModeKey + ",after check, supportedPictureSize:"
                    + supportedPictureSize);
        }

        //bv wuyonglin add for modify PanoramaMode should fixed picture size 20200103 start
        if ("com.mediatek.camera.feature.mode.panorama.PanoramaMode".equals(mModeKey)) {
            List<String> supportedPictureSizeAfterCheck = new ArrayList<String>();
            for (String pictureSize : supportedPictureSize) {
                String[] size = pictureSize.split("x");
                int width = Integer.parseInt(size[0]);
                int height = Integer.parseInt(size[1]);
                if (Config.isCameraRatio18_9Support(mActivity)){
                    if ((width / height) == PictureSizeHelper.RATIO_18_9) {
                        supportedPictureSizeAfterCheck.add(pictureSize);
			break;
                    }
		} else {
                    if ((width / height) == PictureSizeHelper.RATIO_16_9) {
                        supportedPictureSizeAfterCheck.add(pictureSize);
			break;
                    }
		}
            }
            supportedPictureSize = supportedPictureSizeAfterCheck;
            LogHelper.d(TAG, "[onValueInitialized], PanoramaMode, after check, supportedPictureSize:"
                    + supportedPictureSize);
        }
        //bv wuyonglin add for modify PanoramaMode should fixed picture size 20200103 end

        //bv wuyonglin add for Superphoto should fixed picture size 20201023 start
        if ("com.mediatek.camera.feature.mode.superphoto.SuperphotoMode".equals(mModeKey)) {
            List<String> supportedPictureSizeAfterCheck = new ArrayList<String>();
            for (String pictureSize : supportedPictureSize) {
                String[] size = pictureSize.split("x");
                int width = Integer.parseInt(size[0]);
                int height = Integer.parseInt(size[1]);
                if (width == 8000 && height == 6000) {
                    supportedPictureSizeAfterCheck.add(pictureSize);
                    break;
                }
            }
            supportedPictureSize = supportedPictureSizeAfterCheck;
            LogHelper.d(TAG, "[onValueInitialized], SuperphotoMode, after check, supportedPictureSize:"
                    + supportedPictureSize);
        }
        //bv wuyonglin add for Superphoto should fixed picture size 20201023 end

        setSupportedPlatformValues(supportedPictureSize);
        setSupportedEntryValues(supportedPictureSize);
        setEntryValues(supportedPictureSize);
        //bv wuyonglin add for setting ui 20200923 start
        List<String> entryValues1 = getEntryValues();
        for (String value : entryValues1) {
            if (PictureSizeHelper.getStandardAspectRatio(value) == PictureSizeHelper.RATIO_4_3) {
		defaultValueInStore = value;
		LogHelper.d(TAG, "onValueInitialized new restoreSettingtoValue12 defaultValueInStore ="+defaultValueInStore);
		break;
            }
        }
        //bv wuyonglin add for setting ui 20200923 end
        //bv wuyonglin delete for some quickSwitch icon posistion change quickly 20200113 start
        //refreshViewEntry();
        //bv wuyonglin delete for some quickSwitch icon posistion change quickly 20200113 end

        //bv wuyonglin add for modify PanoramaMode should fixed picture size 20200103 start
        if (!"com.mediatek.camera.feature.mode.panorama.PanoramaMode".equals(mModeKey) && !"com.mediatek.camera.feature.mode.superphoto.SuperphotoMode".equals(mModeKey)) {
        //bv wuyonglin add for modify PanoramaMode should fixed picture size 20200103 end
        String valueInStore = mDataStore.getValue(getKey(), null, getStoreScope());
        if (valueInStore != null
                && !supportedPictureSize.contains(valueInStore)) {
            LogHelper.d(TAG, "[onValueInitialized], value:" + valueInStore
                    + " isn't supported in current platform");
            	if (PictureSizeHelper.getStandardAspectRatio(valueInStore) == PictureSizeHelper.RATIO_18_9) {
            		for (String value : supportedPictureSize) {
                		if (PictureSizeHelper.getStandardAspectRatio(value) == PictureSizeHelper.RATIO_18_9) {
                   			valueInStore = value;
                   			mDataStore.setValue(getKey(), valueInStore, getStoreScope(), false);
                    		break;
                		}
            		}
            		LogHelper.d(TAG, "[onValueInitialized], 18:9 final is not valueInStore:" + valueInStore);
            	} else if (PictureSizeHelper.getStandardAspectRatio(valueInStore) == PictureSizeHelper.RATIO_4_3) {
            		for (String value : supportedPictureSize) {
                		if (PictureSizeHelper.getStandardAspectRatio(value) == PictureSizeHelper.RATIO_4_3) {
                   			valueInStore = value;
                   			mDataStore.setValue(getKey(), valueInStore, getStoreScope(), false);
                    		break;
                		}
            		}
            		LogHelper.d(TAG, "[onValueInitialized], 4:3 final is not valueInStore:" + valueInStore);
            	}
        }
		//add by huangfei for zoom switch start
		String currentRatio = mDataStore.getValue(KEY_PREVIEW_RATIO, (PictureSizeHelper.RATIO_4_3+""), mDataStore.getGlobalScope());
        if(valueInStore != null && mAppUi.isZoomSwitchSupportCameraId()){
            double ratio = PictureSizeHelper.getStandardAspectRatio(valueInStore);
            double targetRatio = PictureSizeHelper.RATIO_4_3;
            targetRatio = getRatio(currentRatio);
            if(ratio!=targetRatio){
                valueInStore = null;
                mDataStore.setValue(getKey(), null, getStoreScope(), false);
            }
        }
		//add by huangfei for zoom switch end
        if (valueInStore == null) {
            // Default picture size is the max full-ratio size.
            List<String> entryValues = getEntryValues();
            for (String value : entryValues) {
				//modify by huangfei for default preview ratio start				
                /*if (PictureSizeHelper.getStandardAspectRatio(value) == fullRatio) {
                    valueInStore = value;
                    break;
                }*/
				if(defaultRatioVlaue==0){
					defaultRatio = PictureSizeHelper.RATIO_4_3;
				}else if(defaultRatioVlaue==1){
					defaultRatio = PictureSizeHelper.RATIO_16_9;
				}else{
					defaultRatio = fullRatio;
				}
				//add by huangfei for zoom switch start
				if(mAppUi.isZoomSwitchSupportCameraId()){
					defaultRatio = getRatio(mDataStore.getValue(KEY_PREVIEW_RATIO, (PictureSizeHelper.RATIO_4_3+""), mDataStore.getGlobalScope()));
				}
				//add by huangfei for zoom switch end
				if (PictureSizeHelper.getStandardAspectRatio(value) == defaultRatio) {
                    valueInStore = value;
                    break;
                }
				//modify by huangfei for default preview ratio end				
            }
        }
        // If there is no full screen ratio picture size, use the first value in
        // entry values as the default value.
        if (valueInStore == null) {
            valueInStore = getEntryValues().get(0);
        }
        setValue(valueInStore);
	//bv wuyonglin add for modify PanoramaMode should fixed picture size 20200103 start
	} else {
            LogHelper.d(TAG, "[onValueInitialized], final after check, supportedPictureSize.get(0):"
                    + supportedPictureSize.get(0));
            setValue(supportedPictureSize.get(0));
	}
	//bv wuyonglin add for modify PanoramaMode should fixed picture size 20200103 end
    }

    @Override
    public void onValueChanged(String value) {
        LogHelper.i(TAG, "[onValueChanged], value:" + value);
        if (!getValue().equals(value)) {
            setValue(value);
            //add by huangfei for zoom switch start
			if(mAppUi.isZoomSwitchSupportCameraId()){
                String ratio = PictureSizeHelper.getStandardAspectRatio(value)+"";
                mDataStore.setValue(KEY_PREVIEW_RATIO, ratio, mDataStore.getGlobalScope(), false);
            }
            //add by huangfei for zoom switch end
            mDataStore.setValue(getKey(), value, getStoreScope(), false);
            //bv wuyonglin add for setting add picture size option 20200219 start
            mSettingController.refreshViewEntry();
            //bv wuyonglin add for setting add picture size option 20200219 end
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            });
        }
    }

    private boolean isLowRam() {
        boolean enable = "true".equals(SystemProperties.getString("ro.config.low_ram", "false"));
        LogHelper.i(TAG, "[isLowRam]" + enable);
        return enable;
    }

    //add by huangfei for  picture size ratio 1:1 start
    public String getCurrentMode(){
        String mode = mAppUi.getCurrentMode();
        return mode;
    }
    //add by huangfei for  picture size ratio 1:1 end

    //bv wuyonglin add for PictureSize quickSwitch 20191224 start
    public void onPictureSizeValueChanged(String value) {
	//bv wuyonglin modify for setting add picture size option 20200219 start
	LogHelper.d(TAG, "[onPictureSizeValueChanged] value = " + value+" getValue() ="+getValue());
	if (!getValue().equals(value)) {
            LogHelper.d(TAG, "[onPictureSizeValueChanged] value = " + value+" getStoreScope ="+getStoreScope()+" getKey() ="+getKey());
            setValue(value);
            //add by huangfei for zoom switch start
            if (mAppUi.isZoomSwitchSupportCameraId()) {
                String ratio = PictureSizeHelper.getStandardAspectRatio(value)+"";
                mDataStore.setValue(KEY_PREVIEW_RATIO, ratio, mDataStore.getGlobalScope(), false);
            }
            //add by huangfei for zoom switch end
            mDataStore.setValue(getKey(), value, getStoreScope(), false);
            //bv wuyonglin add for hdr can not support 1:1 picture size 20200107 start
            mSettingController.refreshViewEntry();
            //bv wuyonglin add for hdr can not support 1:1 picture size 20200107 end
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
        });
	}
	//bv wuyonglin modify for setting add picture size option 20200219 end
    }
    //bv wuyonglin add for PictureSize quickSwitch 20191224 end

    //bv wuyonglin add for setting ui 20200923 start
    private IAppUi.RestoreSettingListener mRestoreSettingListener = new IAppUi.RestoreSettingListener() {
        @Override
        public void restoreSettingtoValue() {
            setValue(defaultValueInStore);
            if (mAppUi.isZoomSwitchSupportCameraId()) {
                String ratio = PictureSizeHelper.getStandardAspectRatio(defaultValueInStore) + "";
                mDataStore.setValue(KEY_PREVIEW_RATIO, ratio, mDataStore.getGlobalScope(), false);
            }
            mDataStore.setValue(getKey(), defaultValueInStore, getStoreScope(), false);
            mSettingController.refreshViewEntry();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            });
        }
    };
    //bv wuyonglin add for setting ui 20200923 end

    /**
     * Get current camera id.
     * @return The current camera id.
     */
    protected int getCameraId() {
        int cameraId = Integer.parseInt(mSettingController.getCameraId());
        return cameraId;
    }

    public String getModeKey() {
        return mModeKey;
    }
}
