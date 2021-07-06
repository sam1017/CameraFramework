package com.aiworks.android.deblur;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import android.graphics.Rect;
import static android.hardware.camera2.CaptureRequest.SCALER_CROP_REGION;
import com.aiworks.android.Super.SuperUtil;
import com.aiworks.android.camera.VendorTagRequest;

@TargetApi(Build.VERSION_CODES.O_MR1)
public class ShakeCaptureRequest implements ShakeCaptureRequestInterface {

    private static final String TAG = "ShakeCaptureRequest";

    private Integer sensitivity; // ISO
    private Long exposureTime; // 曝光时间
    private float focusDistance;
    private Integer postRawSensitivityBoost;
    private boolean mProcessResult = false;
    private Rect mScalerCropRegion;
    //bv wuyonglin add for bug5276 20210415 start
    private float mCurZoomRatio = 1.0f;
    private Rect mSensorRect;
    //bv wuyonglin add for bug5276 20210415 end

    public ShakeCaptureRequest() {
    }

    @Override
    public void destory() {
    }

    @Override
    public CaptureRequest.Builder createPreviewRequest(CameraDevice mCameraDevice) {
        try {
            CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//            previewRequestBuilder.set(CaptureRequest.CONTROL_ENABLE_ZSL, true);
            previewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE);
            return previewRequestBuilder;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<CaptureRequest.Builder> createCaptureRequest(CameraDevice mCameraDevice, boolean shaking) {

        long dateTaken = System.currentTimeMillis();
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        ShakeUtil.setDumpFileTitle(dateFormat.format(date));

        ShakeUtil.startCapture();
        ArrayList<CaptureRequest.Builder> builders = new ArrayList<>();

        mProcessResult = false;
        // normal exposure.
//        if (shaking) {
//            addCaptureRequest(mCameraDevice, exposureTime, sensitivity, postRawSensitivityBoost, builders);
//            addCaptureRequest(mCameraDevice, exposureTime, sensitivity, postRawSensitivityBoost, builders);
//            addCaptureRequest(mCameraDevice, exposureTime, sensitivity, postRawSensitivityBoost, builders);
//        } else {
//            addCaptureRequest(mCameraDevice, exposureTime, sensitivity, postRawSensitivityBoost, builders);
//        }
        if (shaking) {
            addCaptureRequest(mCameraDevice, builders);
            addCaptureRequest(mCameraDevice, builders);
            addCaptureRequest(mCameraDevice, builders);
        } else {
            addCaptureRequest(mCameraDevice, builders);
        }
        // recalculated time and iso
//        addCaptureRequest(mCameraDevice, mExposureCaculator.getResult_ExpTime(), mExposureCaculator.getResult_Sensitity(), mExposureCaculator.getResult_DGain(), builders);
//        addCaptureRequest(mCameraDevice, mExposureCaculator.getResult_ExpTime(), mExposureCaculator.getResult_Sensitity(), mExposureCaculator.getResult_DGain(), builders);
//        addCaptureRequest(mCameraDevice, mExposureCaculator.getResult_ExpTime(), mExposureCaculator.getResult_Sensitity(), mExposureCaculator.getResult_DGain(), builders);
        return builders;
    }

    private void addCaptureRequest(CameraDevice mCameraDevice, ArrayList<CaptureRequest.Builder> builders) {
        final CaptureRequest.Builder captureBuilder;
        try {
            captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
            if (focusDistance != 0) {
                captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);
                captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focusDistance);
            }

//            captureBuilder.set(CaptureRequest.CONTROL_ENABLE_ZSL, true);
            captureBuilder.set(CaptureRequest.CONTROL_AWB_LOCK, true);

            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            if (SuperUtil.mForceMtkMfnr) {
                captureBuilder.set(VendorTagRequest.MTK_MFB_MODE, new int[]{255});
            }
            //bv wuyonglin add for bug5276 20210415 start
            android.util.Log.w(TAG,"addCaptureRequest configureSession mCurZoomRatio ="+mCurZoomRatio+" mSensorRect ="+mSensorRect);
            //captureBuilder.set(SCALER_CROP_REGION, mScalerCropRegion);
            captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, mSensorRect);
            captureBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO,mCurZoomRatio);
            //bv wuyonglin add for bug5276 20210415 end

            builders.add(captureBuilder);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void addCaptureRequest(CameraDevice mCameraDevice, long expTime, int sensitity, int dGain, ArrayList<CaptureRequest.Builder> builders) {
        final CaptureRequest.Builder captureBuilder;
        try {
            captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
            if (focusDistance != 0) {
                captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);
                captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focusDistance);
            }

//            captureBuilder.set(CaptureRequest.CONTROL_ENABLE_ZSL, true);
            captureBuilder.set(CaptureRequest.CONTROL_AWB_LOCK, true);

            if (mProcessResult) {
                captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, expTime);
                captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, sensitity);
                captureBuilder.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, dGain);
            } else {
                captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            }
            //bv wuyonglin add for bug5276 20210415 start
            android.util.Log.w(TAG,"addCaptureRequest configureSession mCurZoomRatio ="+mCurZoomRatio+" mSensorRect ="+mSensorRect);
            //captureBuilder.set(SCALER_CROP_REGION, mScalerCropRegion);
            captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, mSensorRect);
            captureBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO,mCurZoomRatio);
            //bv wuyonglin add for bug5276 20210415 end

            builders.add(captureBuilder);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreviewCaptureCompleted(CaptureResult result) {
        sensitivity = result.get(CaptureResult.SENSOR_SENSITIVITY);
        exposureTime = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
        focusDistance = result.get(CaptureResult.LENS_FOCUS_DISTANCE);
        postRawSensitivityBoost = result.get(CaptureResult.CONTROL_POST_RAW_SENSITIVITY_BOOST);
        if (postRawSensitivityBoost == null) {
            postRawSensitivityBoost = 100;
        }
	mScalerCropRegion = result.get(CaptureResult.SCALER_CROP_REGION);
        //bv wuyonglin add for bug5276 20210415 start
        mCurZoomRatio = result.get(CaptureResult.CONTROL_ZOOM_RATIO);
        mSensorRect = result.get(CaptureResult.SCALER_CROP_REGION);
        //bv wuyonglin add for bug5276 20210415 end
    }

    @Override
    public void onPreviewFrame(byte[] nv21, int width, int height, int orientation) {
    }

    public Rect getScalerCropRegion(){
        return mScalerCropRegion;
    }

}
