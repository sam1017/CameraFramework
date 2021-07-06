package com.mediatek.camera.feature.setting;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.mediatek.camera.R;
import com.mediatek.camera.Config;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.loader.FeatureEntryBase;
import com.mediatek.camera.common.setting.ICameraSetting;
import android.widget.FrameLayout;
import com.mediatek.camera.common.utils.CameraUtil;
import android.view.View;
import android.os.SystemProperties;
import javax.annotation.Nonnull;

import android.util.Log;

public class TripleswitchEntry extends FeatureEntryBase {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(TripleswitchEntry.class.getSimpleName());
    private Tripleswitch mTripleswitch;
    private String mCameraId = "0";
    private int numOfCameras;
    private FrameLayout mFrameLayout;
    private String mWideId;
    private String mMacroId;
    private boolean isMacroSupport = false;
    private boolean isWideSupport = false;

    //add by huangfeifor front tripleswitchhorizontal start
    private boolean mHorizontalSwitch = false;
    private TripleSwitchHorizontal mTripleSwitchHorizontal;
    //add by huangfeifor front tripleswitchhorizontal end

    public TripleswitchEntry(Context context, Resources resources) {
        super(context, resources);
        mWideId = SystemProperties.get("ro.hct_wide_angle_id","-1");
        mMacroId = SystemProperties.get("ro.hct_macroLens_id","-1");
        isMacroSupport = "-1".equals(mMacroId) ? false : true;
        isWideSupport = "-1".equals(mWideId) ? false : true;

        //add by huangfeifor front tripleswitchhorizontal start
        mHorizontalSwitch = Config.isTripleSwitchHorizontalSupport(context);
        //add by huangfeifor front tripleswitchhorizontal end
    }

    @Override
    public void notifyBeforeOpenCamera(@Nonnull String cameraId, @Nonnull CameraDeviceManagerFactory.CameraApi cameraApi) {
        super.notifyBeforeOpenCamera(cameraId, cameraApi);
        mCameraId = cameraId;
    }

    @Override
    public boolean isSupport(CameraDeviceManagerFactory.CameraApi currentCameraApi, Activity activity) {
        mFrameLayout = activity.findViewById(R.id.triple_switch);
        //start, wangsenhao ,hide some button on underwater mode, 2020.04.22
        int isUnderWater = CameraUtil.getUnderWaterSupport(activity);
        if(isThirdPartyIntent(activity) || isUnderWater == 1) {
        //end, wangsenhao ,hide some button on underwater mode, 2020.04.22
            mFrameLayout.setVisibility(View.GONE);
            return false;
        }
        if((mWideId.equals("-1") && isWideSupport) || (mMacroId.equals("-1") && isMacroSupport)){
            mFrameLayout.setVisibility(View.GONE);
            return false;
        }
        if(isMacroSupport || isWideSupport){
            return true;
        }
        mFrameLayout.setVisibility(View.GONE);
        return false;
    }

    @Override
    public String getFeatureEntryName() {
        return TripleswitchEntry.class.getName();
    }

    @Override
    public Class getType() {
        return ICameraSetting.class;
    }

    @Override
    public Object createInstance() {

        //add by huangfeifor front tripleswitchhorizontal start
        if(mHorizontalSwitch){
            if(mTripleSwitchHorizontal==null){
                mTripleSwitchHorizontal = new TripleSwitchHorizontal();
            }
            return mTripleSwitchHorizontal;
        }
        //add by huangfeifor front tripleswitchhorizontal end
        
        if (mTripleswitch == null) {
            mTripleswitch = new Tripleswitch();
        }
        return mTripleswitch;
    }
}
