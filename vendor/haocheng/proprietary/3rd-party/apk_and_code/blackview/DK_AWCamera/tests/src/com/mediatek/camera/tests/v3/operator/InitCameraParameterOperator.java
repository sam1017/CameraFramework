package com.mediatek.camera.tests.v3.operator;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.tests.v3.arch.OperatorOne;
import com.mediatek.camera.tests.v3.arch.Page;
import com.mediatek.camera.tests.v3.arch.TestContext;
import com.mediatek.camera.tests.v3.util.LogHelper;
import com.mediatek.camera.tests.v3.util.ReflectUtils;
import com.mediatek.camera.tests.v3.util.Utils;

public class InitCameraParameterOperator extends OperatorOne {
    private static final LogUtil.Tag TAG = Utils.getTestTag(InitCameraParameterOperator.class
            .getSimpleName());

    private static final String BACK_CAMERA = "0";
    private static final String FRONT_CAMERA = "1";


    @Override
    protected void doOperate() {
        initWhenAPI2();
    }

    @Override
    public Page getPageBeforeOperate() {
        return null;
    }

    @Override
    public Page getPageAfterOperate() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Init camera parameters in both API1 and API2 for test environment needed";
    }

    private void initWhenAPI2() {
        if (TestContext.mBackCameraCharacteristics == null) {
            CameraManager cameraManager = (CameraManager) ReflectUtils.createInstance(
                    ReflectUtils.getConstructor("android.hardware.camera2.CameraManager",
                            Context.class), Utils.getTargetContext());
            try {
                LogHelper.d(TAG, "[initWhenAPI2] BACK_CAMERA");
                TestContext.mBackCameraCharacteristics =
                        cameraManager.getCameraCharacteristics(BACK_CAMERA);
            } catch (CameraAccessException e) {
                LogHelper.e(TAG, "[initWhenAPI2] back camera, CameraAccessException " + e);
            } catch (IllegalArgumentException e) {
                LogHelper.e(TAG, "[initWhenAPI2] back camera, IllegalArgumentException " + e);
            }
        }

        if (TestContext.mFrontCameraCharacteristics == null) {
            CameraManager cameraManager = (CameraManager) ReflectUtils.createInstance(
                    ReflectUtils.getConstructor("android.hardware.camera2.CameraManager",
                            Context.class), Utils.getTargetContext());
            try {
                LogHelper.d(TAG, "[initWhenAPI2] FRONT_CAMERA");
                TestContext.mFrontCameraCharacteristics =
                        cameraManager.getCameraCharacteristics(FRONT_CAMERA);
            } catch (CameraAccessException e) {
                LogHelper.e(TAG, "[initWhenAPI2] front camera, CameraAccessException " + e);
            } catch (IllegalArgumentException e) {
                LogHelper.e(TAG, "[initWhenAPI2] front camera, IllegalArgumentException " + e);
            }
        }
    }
}
