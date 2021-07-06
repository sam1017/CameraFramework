package com.aiworks.android.Super;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.Face;
import android.os.Build;
import android.util.Log;

import com.aiworks.android.camera.VendorTagRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import android.graphics.Rect;
import static android.hardware.camera2.CaptureRequest.SCALER_CROP_REGION;

@TargetApi(Build.VERSION_CODES.O_MR1)
public class MTKSuperCaptureRequest implements SuperCaptureRequestInterface {

    private static final String TAG = "MTKSuperCaptureRequest";
    private Rect mScalerCropRegion;
    //bv wuyonglin add for bug5276 20210415 start
    private float mCurZoomRatio = 1.0f;
    private Rect mSensorRect;
    //bv wuyonglin add for bug5276 20210415 end

    public MTKSuperCaptureRequest() {
    }

    @Override
    public CaptureRequest.Builder createPreviewRequest(CameraDevice mCameraDevice) {
        Log.w(TAG, "createPreviewRequest");
        try {
            CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            previewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE);
            previewRequestBuilder.set(VendorTagRequest.MTK_FACE_FORCE_3A, new int[]{1});

            previewRequestBuilder.set(VendorTagRequest.CONTROL_ZSL_MODE, new byte[]{1});
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            if (SuperUtil.USE_PLATFORM_MFNR) {
                previewRequestBuilder.set(VendorTagRequest.MTK_MFB_MODE, new int[]{255});
            }
            if (SuperUtil.edgeMode != -1) {
                previewRequestBuilder.set(CaptureRequest.EDGE_MODE, SuperUtil.edgeMode);
            }
            if (SuperUtil.noiseReductionMode != -1) {
                previewRequestBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, SuperUtil.noiseReductionMode);
            }
            if (SuperUtil.sceneMode != -1) {
                previewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, SuperUtil.sceneMode);
                if (SuperUtil.sceneMode != 0) {
                    previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE);
                }
            }
            return previewRequestBuilder;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<CaptureRequest.Builder> createCaptureRequest(CameraDevice mCameraDevice) {
        Log.w(TAG, "createCaptureRequest");
        long dateTaken = System.currentTimeMillis();
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        SuperUtil.setDumpFileTitle(dateFormat.format(date));


        ArrayList<CaptureRequest.Builder> builders = new ArrayList<>();

        if (SuperUtil.USE_PLATFORM_MFNR) {
            addCaptureRequest(mCameraDevice, builders, true);
        } else {
            addCaptureRequest(mCameraDevice, builders, false);
            addCaptureRequest(mCameraDevice, builders, false);
            addCaptureRequest(mCameraDevice, builders, false);
        }

        return builders;
    }

    private void addCaptureRequest(CameraDevice mCameraDevice, ArrayList<CaptureRequest.Builder> builders, boolean mf) {
        final CaptureRequest.Builder captureBuilder;
        try {
            captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            if (distance != 0) {
                captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);
                captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, distance);
            }

            if (mf || SuperUtil.mForceMtkMfnr) {
                captureBuilder.set(VendorTagRequest.MTK_MFB_MODE, new int[]{255});
            } else {
                captureBuilder.set(VendorTagRequest.MTK_MFB_MODE, new int[]{0});
            }
            if (SuperUtil.edgeMode != -1) {
                captureBuilder.set(CaptureRequest.EDGE_MODE, SuperUtil.edgeMode);
            }
            if (SuperUtil.noiseReductionMode != -1) {
                captureBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, SuperUtil.noiseReductionMode);
            }

            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            //bv wuyonglin add for bug5276 20210415 start
            Log.w(TAG,"addCaptureRequest configureSession mCurZoomRatio ="+mCurZoomRatio+" mSensorRect ="+mSensorRect);
            //captureBuilder.set(SCALER_CROP_REGION, mScalerCropRegion);
            captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, mSensorRect);
            captureBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO,mCurZoomRatio);
            //bv wuyonglin add for bug5276 20210415 end

            builders.add(captureBuilder);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private float distance = 0;
    @Override
    public void onPreviewCaptureCompleted(CaptureResult result) {

        if (result != null) {
            distance = result.get(CaptureResult.LENS_FOCUS_DISTANCE);
        }
        final Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
        if (faces != null && faces.length > 0) {
            SuperUtil.hasFace = true;
            Log.d(TAG, "onPreviewCaptureCompleted faces.length = "+faces.length);
        } else {
            SuperUtil.hasFace = false;
            Log.d(TAG, "onPreviewCaptureCompleted faces is null or faces.length is 0");
        }
	mScalerCropRegion = result.get(CaptureResult.SCALER_CROP_REGION);
        //bv wuyonglin add for bug5276 20210415 start
        mCurZoomRatio = result.get(CaptureResult.CONTROL_ZOOM_RATIO);
        mSensorRect = result.get(CaptureResult.SCALER_CROP_REGION);
        //bv wuyonglin add for bug5276 20210415 end
    }

    @Override
    public void onPreviewFrame(byte[] nv21) {
    }

    public Rect getScalerCropRegion(){
        return mScalerCropRegion;
    }
}
