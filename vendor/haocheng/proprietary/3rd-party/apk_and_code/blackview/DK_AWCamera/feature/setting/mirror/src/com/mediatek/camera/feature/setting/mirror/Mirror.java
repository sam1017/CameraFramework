package com.mediatek.camera.feature.setting.mirror;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;
import javax.annotation.Nonnull;

import java.util.List;
//bv wuyonglin add for setting ui 20200923 start
import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
//bv wuyonglin add for setting ui 20200923 end

/**
 * This class is for Mirror feature interacted with others.
 */

public class Mirror extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(Mirror.class.getSimpleName());
    private static final String KEY_MIRROR = "key_camera_mirror";
    private static final String MIRROR_OFF = "0";
    private static final String MIRROR_ON = "1";
    private boolean mIsSupported = false;
    private ISettingChangeRequester mSettingChangeRequester;
    private MirrorSettingView mSettingView;
    private ICameraContext mICameraContext;
    private IApp mApp;
    //bv wuyonglin add for setting ui 20200923 start
    private String defaultMirrorValue = "";
    //bv wuyonglin add for setting ui 20200923 end

    @Override
    public void init(IApp app, ICameraContext cameraContext, SettingController settingController) {
        super.init(app, cameraContext, settingController);
        //bv wuyonglin add for setting ui 20200923 start
        //mSettingView = new MirrorSettingView();
        mSettingView = new MirrorSettingView(getCameraId());
        //bv wuyonglin add for setting ui 20200923 end
        mSettingView.setMirrorViewListener(mMirrorViewListener);
        mICameraContext = cameraContext;
        mApp = app;
        //bv wuyonglin add for setting ui 20200923 start
        defaultMirrorValue = app.getActivity().getResources().getString(R.string.pref_camera_mirror_default);
        if (getCameraId() == 1) {
        mAppUi.setRestoreSettingListener(mRestoreSettingListener);
        }
        //bv wuyonglin add for setting ui 20200923 end
    }

    @Override
    public void unInit() {
        //bv wuyonglin add for setting ui 20200923 start
        if (getCameraId() == 1) {
        mAppUi.removeRestoreSettingListener(mRestoreSettingListener);
        }
        //bv wuyonglin add for setting ui 20200923 end
    }

    @Override
    public void addViewEntry() {
        mAppUi.addSettingView(mSettingView);
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
    }

    @Override
    public void refreshViewEntry() {
        if (mSettingView != null) {
            mSettingView.setChecked(MIRROR_ON.equals(getValue()));
            //bv wuyonglin add for setting ui 20200923 start
            if (getCameraId() != 1) {
                mSettingView.setEnabled(false);
            } else {
                mSettingView.setEnabled(getEntryValues().size() > 1);
            }
            //bv wuyonglin add for setting ui 20200923 end
        }
    }

    @Override
    public void postRestrictionAfterInitialized() {

    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO;
    }

    @Override
    public String getKey() {
        return KEY_MIRROR;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mSettingChangeRequester == null) {
            MirrorCaptureRequestConfig config = new MirrorCaptureRequestConfig(this, mSettingDevice2Requester,
                    mActivity.getApplicationContext());
            mSettingChangeRequester = config;
        }
        return (MirrorCaptureRequestConfig) mSettingChangeRequester;
    }

    private MirrorSettingView.OnMirrorViewListener mMirrorViewListener
            = new MirrorSettingView.OnMirrorViewListener() {
        @Override
        public void onItemViewClick(boolean isOn) {

            LogHelper.d(TAG, "[onItemViewClick], isOn:" + isOn);
            String value = isOn ? MIRROR_ON : MIRROR_OFF;
            setValue(value);
            mDataStore.setValue(getKey(), value, getStoreScope(), false);
            mSettingChangeRequester.sendSettingChangeRequest();
        }

        @Override
        public boolean onCachedValue() {
            return MIRROR_ON.equals(
                    mDataStore.getValue(getKey(), MIRROR_OFF, getStoreScope()));
        }
    };

    /**
     * update set value.
     *
     * @param value the default value
     */
    public void updateValue(String value) {
        String mVaule = mDataStore.getValue(getKey(), value, getStoreScope());
        setValue(mVaule);
    }

    /**
     * update whether the settings is support.
     *
     * @param isSupported the result
     */
    public void updateIsSupported(boolean isSupported) {
        mIsSupported = isSupported;
        LogHelper.d(TAG, "[updateIsSupported] mIsSupported = " + mIsSupported);
    }

    /**
     * Get current camera id.
     * @return The current camera id.
     */
    protected int getCameraId() {
        int cameraId = Integer.parseInt(mSettingController.getCameraId());
        return cameraId;
    }

    public void onValueInitialized(List<String> platformSupportedValues,
                                   String defaultValue) {
        if (platformSupportedValues != null && platformSupportedValues.size() > 0) {
            setSupportedPlatformValues(platformSupportedValues);
            setSupportedEntryValues(platformSupportedValues);
            setEntryValues(platformSupportedValues);

            //bv wuyonglin add for setting ui 20200923 start
            //String value = mDataStore.getValue(getKey(), defaultValue, getStoreScope());
            String value = mDataStore.getValue(getKey(), defaultValue, "_preferences_1");
            setValue(value);
            //mDataStore.setValue(getKey(), value, getStoreScope(), false);
            mDataStore.setValue(getKey(), value, "_preferences_1", false);
            //bv wuyonglin add for setting ui 20200923 end
            mIsSupported = true;
        }
    }

    //bv wuyonglin add for setting ui 20200923 start
    private IAppUi.RestoreSettingListener mRestoreSettingListener = new IAppUi.RestoreSettingListener() {
        @Override
        public void restoreSettingtoValue() {
            LogHelper.i(TAG, "restoreSettingtoValue defaultMirrorValue ="+defaultMirrorValue);
            setValue(defaultMirrorValue);
            mDataStore.setValue(getKey(), defaultMirrorValue, getStoreScope(), false);
            mSettingChangeRequester.sendSettingChangeRequest();
        }
    };
    //bv wuyonglin add for setting ui 20200923 end
}
