package com.mediatek.camera.feature.setting.mirror;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.mediatek.camera.CameraActivity;
import com.mediatek.camera.Config;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.loader.FeatureEntryBase;
import com.mediatek.camera.common.setting.ICameraSetting;

import javax.annotation.Nonnull;


/**
 * Mirror entry for feature provider.
 */

public class MirrorEntry extends FeatureEntryBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(MirrorEntry.class.getSimpleName());
    private String mCameraId = "0";
    private CameraActivity mCameraActivity;

    /**
     * create an entry.
     *
     * @param context   current activity.
     * @param resources current resources.
     */
    public MirrorEntry(Context context, Resources resources) {
        super(context, resources);
    }

    @Override
    public boolean isSupport(CameraApi currentCameraApi, Activity activity) {
        mCameraActivity = (CameraActivity)activity;
        mCameraId = mCameraActivity.getAppUi().getCameraId();
        //bv wuyonglin add for setting ui 20200923 start
        //boolean support = Config.isMirrorSupport(mContext) && "1".equals(mCameraId);
        boolean support = Config.isMirrorSupport(mContext);
        //bv wuyonglin add for setting ui 20200923 end
        android.util.Log.i("MirrorEntry","isSupport:"+support);
        return support;
    }

    @Override
    public String getFeatureEntryName() {
        return MirrorEntry.class.getName();
    }

    @Override
    public Class getType() {
        return ICameraSetting.class;
    }

    @Override
    public Object createInstance() {
        return new Mirror();
    }
}
