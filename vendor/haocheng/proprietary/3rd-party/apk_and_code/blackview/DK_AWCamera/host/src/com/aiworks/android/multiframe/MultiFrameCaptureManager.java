package com.aiworks.android.multiframe;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.media.ImageReader;
import android.os.Environment;
import android.util.Log;
import android.util.Range;
import android.util.Size;

import com.aiworks.android.Super.MTKSuperCaptureRequest;
import com.aiworks.android.Super.MTKSuperCaptureResult;
import com.aiworks.android.Super.QcomSuperCaptureRequest;
import com.aiworks.android.Super.QcomSuperCaptureResult;
import com.aiworks.android.Super.SuperCaptureRequestInterface;
import com.aiworks.android.Super.SuperCaptureResultInterface;
import com.aiworks.android.Super.SuperUtil;
import com.aiworks.android.deblur.ShakeCaptureRequest;
import com.aiworks.android.deblur.ShakeCaptureRequestInterface;
import com.aiworks.android.deblur.ShakeCaptureResult;
import com.aiworks.android.deblur.ShakeCaptureResultInterface;
import com.aiworks.android.judge.JudgeShake;
import com.aiworks.android.utils.Product;
import com.aiworks.android.utils.Util;

import java.util.ArrayList;

public class MultiFrameCaptureManager {

    public static final String TAG = MultiFrameCaptureManager.class.getSimpleName();

    public static final String MODEL_DIR = "AIWorksModels";
    public static final String MODEL_DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/Android/data/";

    private static final int DATA_ARRAY_NUM = 50;

    private ShakeCaptureRequestInterface mShakeCaptureRequest;
    private ShakeCaptureResultInterface mShakeCaptureResult;
    private SuperCaptureRequestInterface mSuperCaptureRequest;
    private SuperCaptureResultInterface mSuperCaptureResult;
    private Object mJudgeShakeSync = new Object();

    private boolean isShaking;
    private boolean mJudgeInitSuccess;

    public MultiFrameCaptureManager(Context context) {
        SuperUtil.superConfigData(context, SuperUtil.CONFIG_PATH);
        Log.w(TAG, "MultiFrameCaptureManager SuperUtil.SHAKE_THRESHOLD = "+SuperUtil.SHAKE_THRESHOLD);
        int ret = JudgeShake.init(DATA_ARRAY_NUM);
        if (ret == 0) {
            mJudgeInitSuccess = true;
        } else {
            mJudgeInitSuccess = false;
        }
        if (mJudgeInitSuccess) {
            JudgeShake.setDebugEnable(false);
            JudgeShake.setThreshold(SuperUtil.SHAKE_THRESHOLD);
        }
        mShakeCaptureRequest = new ShakeCaptureRequest();
        mShakeCaptureResult = new ShakeCaptureResult();

        SuperUtil.USE_PLATFORM_MFNR = true;

        if (Product.mPlatformID == Product.HARDWARE_PLATFORM_MTK) {
            mSuperCaptureRequest = new MTKSuperCaptureRequest();
            mSuperCaptureResult = new MTKSuperCaptureResult();
        } else {
            mSuperCaptureRequest = new QcomSuperCaptureRequest();
            mSuperCaptureResult = new QcomSuperCaptureResult();
        }
    }

    public void init(Context context, Size photoSize, Size previewSize, Range<Integer> evRange, Rect activeRect,
                     MultiFrameCaptureCallback callback) {
        Log.w(TAG, "AIWORKS_HDShot_V1.0.12");
        Log.w(TAG, "init");

        mShakeCaptureResult.setCaptureCallback(callback);
        mShakeCaptureResult.init(context, photoSize, previewSize, activeRect);

        mSuperCaptureResult.setCaptureCallback(callback);
        mSuperCaptureResult.init(context, photoSize, previewSize, activeRect);
    }

    public void destory() {

        synchronized (mJudgeShakeSync) {
        if (mJudgeInitSuccess) {
            mJudgeInitSuccess = false;
            JudgeShake.uninit();
            Log.w(TAG, "JudgeShake1 uninit");
        }
        }
        mShakeCaptureRequest.destory();
        mShakeCaptureResult.destory();
        mShakeCaptureRequest = null;
        mShakeCaptureResult = null;

        mSuperCaptureResult.destory();
        mSuperCaptureRequest = null;
        mSuperCaptureResult = null;
    }

    public void setAccData(float var0, float var1, float var2) {
        synchronized (mJudgeShakeSync) {
        if (mJudgeInitSuccess) {
            JudgeShake.setAccData(var0, var1, var2);
        }
        }
    }

    public void onStartCapture(int frameNum, int format, int jpegRotation) {
        if (isShaking) {
            mShakeCaptureResult.onStartCapture(frameNum, format, jpegRotation);
        } else {
            mSuperCaptureResult.onStartCapture(frameNum, format);
        }
    }

    public ImageReader.OnImageAvailableListener getImageAvailableListener() {
        if (isShaking) {
            return mShakeCaptureResult.getImageAvailableListener();
        }
        return mSuperCaptureResult.getImageAvailableListener();
    }

    public CameraCaptureSession.CaptureCallback getCaptureCallback() {
        if (isShaking) {
            return mShakeCaptureResult.getCaptureCallback();
        }
        return mSuperCaptureResult.getCaptureCallback();
    }

    public Size getPhotoSize() {
        return SuperUtil.photoSize;
    }

    public int getPhotoForamt() {
        return ImageFormat.YUV_420_888;
    }

    public CaptureRequest.Builder createPreviewRequest(CameraDevice mCameraDevice) {
        return mSuperCaptureRequest.createPreviewRequest(mCameraDevice);
    }

    public void onPreviewCaptureCompleted(CaptureResult result) {
        if (mShakeCaptureRequest != null) {
            mShakeCaptureRequest.onPreviewCaptureCompleted(result);
        }
        if (mSuperCaptureRequest != null) {
            mSuperCaptureRequest.onPreviewCaptureCompleted(result);
        }
    }

    public ArrayList<CaptureRequest.Builder> createCaptureRequest(CameraDevice mCameraDevice) {
        Log.w(TAG, "createCaptureRequest mJudgeInitSuccess ="+mJudgeInitSuccess);
        if (mJudgeInitSuccess) {
            isShaking = JudgeShake.isShaking();
        }
        Log.d(TAG, "createCaptureRequest isShaking = " + isShaking);
        if (isShaking) {
            return mShakeCaptureRequest.createCaptureRequest(mCameraDevice, true);
        }
        return mSuperCaptureRequest.createCaptureRequest(mCameraDevice);
    }

    public boolean onPreviewFrame(byte[] nv21, int width, int height, int orientation) {
        if (mShakeCaptureRequest != null) {
            mShakeCaptureRequest.onPreviewFrame(nv21, width, height, orientation);
        }
        if (mSuperCaptureRequest != null) {
            mSuperCaptureRequest.onPreviewFrame(nv21);
        }
        return false;
    }

    public Rect getScalerCropRegion(){
        return mSuperCaptureRequest.getScalerCropRegion();
    }
}
