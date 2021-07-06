package com.mediatek.camera.feature.setting.mirror;

import android.annotation.TargetApi;

import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.view.Surface;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager.SettingDevice2Requester;
import com.mediatek.camera.R;

import java.util.ArrayList;
import java.util.List;


/**
 * This is for Mirror capture flow in camera API2.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MirrorCaptureRequestConfig implements ICameraSetting.ICaptureRequestConfigure {
    private static final LogUtil.Tag TAG
            = new LogUtil.Tag(MirrorCaptureRequestConfig.class.getSimpleName());
    private static final String MIRROR_OFF = "0";
    private static final String MIRROR_ON = "1";
    private SettingDevice2Requester mDevice2Requester;
    private CaptureRequest.Key<int[]> mKeyMirrorRequestValue;
    private Mirror mMirror;
    private Context mContext;
    private String defaultValue;

    /**
     * Mirror capture request configure constructor.
     * @param mirror The instance of {@link Mirror}.
     * @param device2Requester The implementer of {@link SettingDevice2Requester}.
     */
    public MirrorCaptureRequestConfig(Mirror mirror, SettingDevice2Requester device2Requester, Context context) {
        mMirror = mirror;
        mDevice2Requester = device2Requester;
        mContext = context;
        defaultValue = context.getResources().getString(R.string.pref_camera_mirror_default);
    }

    @Override
    public void setCameraCharacteristics(CameraCharacteristics characteristics) {
        DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(mContext)
                .getDeviceDescriptionMap().get(String.valueOf(mMirror.getCameraId()));
        if (deviceDescription != null) {
            mKeyMirrorRequestValue = deviceDescription.getKeyFlipRequestMode();
			if(mKeyMirrorRequestValue==null){
				LogHelper.e(TAG, "flip mode does not define in metadata");
				return;
			}
        }
        List<String> supportMirrorList = new ArrayList<>();
        supportMirrorList.add(MIRROR_OFF);
        supportMirrorList.add(MIRROR_ON);
        mMirror.onValueInitialized(supportMirrorList, defaultValue);
    }

    @Override
    public void configCaptureRequest(CaptureRequest.Builder captureBuilder) {
        String value = mMirror.getValue();
        LogHelper.d(TAG, "[configCaptureRequest], value:" + value);
        //bv wuyonglin add for setting ui 20200923 start
        if (value != null && captureBuilder != null && mMirror.getCameraId() == 1) {
        //bv wuyonglin add for setting ui 20200923 end
            int[] mode = new int[1];
            mode[0] = Integer.parseInt(value);
            captureBuilder.set(mKeyMirrorRequestValue, mode);
        }
    }

    @Override
    public void configSessionSurface(List<Surface> surfaces) {

    }

    @Override
    public Surface configRawSurface() {
        return null;
    }

    @Override
    public CameraCaptureSession.CaptureCallback getRepeatingCaptureCallback() {
        return null;
    }

    @Override
    public void sendSettingChangeRequest() {
        mDevice2Requester.createAndChangeRepeatingRequest();
    }
}
