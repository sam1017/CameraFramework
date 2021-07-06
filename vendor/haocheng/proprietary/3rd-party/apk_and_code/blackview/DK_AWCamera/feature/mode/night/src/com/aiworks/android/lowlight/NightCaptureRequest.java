package com.aiworks.android.lowlight;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.Face;
import android.os.Build;
import android.util.Log;

import com.aiworks.android.camera.VendorTagMetadata;
import com.aiworks.android.camera.VendorTagRequest;
import com.aiworks.android.utils.Product;
import com.aiworks.android.utils.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NightCaptureRequest implements NightCaptureRequestInterface {

    protected static final String TAG = "NightCaptureRequest";

    private static final String MODEL_DIR = "AIWorksModels";

    private final AIWorksNsExpJni mNsExpJni;

    private Integer sensitivity = 100;
    private Long exposureTime = 30000000L;
    private Integer postRawSensitivityBoost = 100;
    private Float aecLux = 0f;
    private Face[] mFace = null;
    private Rect mScalerCropRegion = null;
    //bv wuyonglin add for bug5276 20210415 start
    private float mCurZoomRatio = 1.0f;
    private Rect mSensorRect;
    //bv wuyonglin add for bug5276 20210415 end
    public NightCaptureRequest() {
        mNsExpJni = AIWorksNsExpJni.getInstance();
    }

    @Override
    public void init(Context context, String xmlPath, int cameraID) {
        String binPath = context.getExternalFilesDir(MODEL_DIR).getAbsolutePath() + "/";
        Util.copyModle(context, binPath, MODEL_DIR);
        mNsExpJni.init(xmlPath, binPath + "FD_J_o_v1.tl", cameraID);
        if ("userdebug".equals(Build.TYPE)) {
            mNsExpJni.setLogable(true);
        }
    }

    @Override
    public void destory() {
        mNsExpJni.release();
    }

    @Override
    public void onPreviewCaptureCompleted(CaptureResult result) {
        sensitivity = result.get(CaptureResult.SENSOR_SENSITIVITY);
        exposureTime = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
        mFace = result.get(CaptureResult.STATISTICS_FACES);
        mScalerCropRegion = result.get(CaptureResult.SCALER_CROP_REGION);
        //bv wuyonglin add for bug5276 20210415 start
        mCurZoomRatio = result.get(CaptureResult.CONTROL_ZOOM_RATIO);
        mSensorRect = result.get(CaptureResult.SCALER_CROP_REGION);
        //bv wuyonglin add for bug5276 20210415 end
    }

    @Override
    public void onPreviewFrame(byte[] nv21, int width, int height, int jpegRotation) {
        if (exposureTime != null && sensitivity != null && postRawSensitivityBoost != null) {
            synchronized (this) {
                AIWorksNsExpJni.AIWorksNsExpSrc expSrc = new AIWorksNsExpJni.AIWorksNsExpSrc(
                        exposureTime, sensitivity, postRawSensitivityBoost, aecLux.intValue(),
                        mFace != null && mFace.length > 0 ? 1 : 0, AIWorksNsExpJni.getFaceOrientation(jpegRotation));
                mNsExpJni.addImage(nv21, width, height, width, expSrc);
                int ret = mNsExpJni.process();
            }
        }
    }

    @Override
    public ArrayList<CaptureRequest.Builder> createCaptureRequest(CameraDevice mCameraDevice) {

        long dateTaken = System.currentTimeMillis();
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String title = dateFormat.format(date);
        synchronized (this) {
            NightShotConfig.setDumpFileTitle(title);
            if (NightShotConfig.isDump()) {
                mNsExpJni.dump(NightShotConfig.getDumpFilePath());
            }
            mNsExpJni.cloneParam();
        }
        AIWorksNsExpJni.AIWorksNsExpDst expDst = mNsExpJni.getAIWorksNsExpDst();
        Log.w(TAG, expDst.toString());

        ArrayList<CaptureRequest.Builder> builders = new ArrayList<>();
        addCaptureRequest(mCameraDevice, builders, expDst);
/*        if ("BL5000".equals(Product.MODEL_NAME)) {
            builders.add(addAECaptureRequest(mCameraDevice));
            Log.i(TAG,"createCaptureRequest add addAECaptureRequest");
        }*/
        return builders;
    }

    private void addCaptureRequest(CameraDevice mCameraDevice, ArrayList<CaptureRequest.Builder> builders, AIWorksNsExpJni.AIWorksNsExpDst expDst) {
        try {
            for (int i = 0; i < expDst.frameNum; i++) {
                CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.set(CaptureRequest.CONTROL_AWB_LOCK, true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    captureBuilder.set(CaptureRequest.CONTROL_ENABLE_ZSL, false);
                }
				Log.w(TAG,"addCaptureRequest configureSession mCurZoomRatio ="+mCurZoomRatio+" mSensorRect ="+mSensorRect);
                //captureBuilder.set(SCALER_CROP_REGION, mScalerCropRegion);
                captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, mSensorRect);
                captureBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO,mCurZoomRatio);
                //bv wuyonglin add for bug5276 20210415 end
                //if (mScalerCropRegion != null) {
                //    captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, mScalerCropRegion);
                //}

                boolean mfbEnable = expDst.mfnrs[i] != 0;
                Log.d(TAG, "mfbEnable = " + mfbEnable);
                if (Product.mPlatformID == Product.HARDWARE_PLATFORM_MTK) {
                    captureBuilder.set(VendorTagRequest.MTK_MFB_MODE, mfbEnable ? VendorTagMetadata.MTK_MFB_MODE_AUTO : VendorTagMetadata.MTK_MFB_MODE_OFF);
                    if (expDst.noiseReductionModes[i] != -1) {
                        captureBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, expDst.noiseReductionModes[i]);
                    }
                    if (expDst.edgeModes[i] != -1) {
                        captureBuilder.set(CaptureRequest.EDGE_MODE, expDst.edgeModes[i]);
                    }
                } else if (Product.mPlatformID == Product.HARDWARE_PLATFORM_QCOM) {
                    if (mfbEnable) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            captureBuilder.set(VendorTagRequest.QCOM_CUSTOM_NOISE_REDUCTION, (byte) 0x01);
                        }
                        captureBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY);
                    } else {
                        if (expDst.noiseReductionModes[i] != -1) {
                            captureBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, expDst.noiseReductionModes[i]);
                        }
                    }
                    if (expDst.edgeModes[i] != -1) {
                        captureBuilder.set(CaptureRequest.EDGE_MODE, expDst.edgeModes[i]);
                    }
                    if (expDst.saturations[i] != -1) {
                        captureBuilder.set(VendorTagRequest.CONTROL_QCOM_SATURATION_LEVEL, expDst.saturations[i]);
                    }
                    if (expDst.sharpnesss[i] != -1) {
                        captureBuilder.set(VendorTagRequest.CONTROL_QCOM_SHARPNESS_STRENGTH, expDst.sharpnesss[i]);
                    }
                    if (expDst.contrasts[i] != -1) {
                        captureBuilder.set(VendorTagRequest.CONTROL_QCOM_CONTRAST_LEVEL, expDst.contrasts[i]);
                    }
                }

                if (expDst.exposureTimes[i] != 0) {
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                    captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, expDst.exposureTimes[i]);
                    captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, expDst.sensititys[i]);
                    captureBuilder.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, expDst.dGains[i]);
                    captureBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true);
                } else {
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                }
                builders.add(captureBuilder);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CaptureRequest.Builder addAECaptureRequest(CameraDevice mCameraDevice) {
        CaptureRequest.Builder captureBuilder = null;
        try {
            captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AE_LOCK, false);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return captureBuilder;
    }

}
