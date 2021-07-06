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

import static android.hardware.camera2.CaptureRequest.CONTROL_MODE;
import android.graphics.Rect;

@TargetApi(Build.VERSION_CODES.O_MR1)
public class QcomSuperCaptureRequest implements SuperCaptureRequestInterface {

    private static final String TAG = "QcomSuperCaptureRequest";

    public QcomSuperCaptureRequest() {
    }

    @Override
    public CaptureRequest.Builder createPreviewRequest(CameraDevice mCameraDevice) {
        try {
            CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            previewRequestBuilder.set(CaptureRequest.CONTROL_ENABLE_ZSL, true);
            previewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE);
            if (SuperUtil.sceneMode != -1) {
                previewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, SuperUtil.sceneMode);
            } else {
                previewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_FACE_PRIORITY);
            }
            if (SuperUtil.sceneMode != 0) {
                previewRequestBuilder.set(CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE);
            }

            if (SuperUtil.edgeMode != -1) {
                previewRequestBuilder.set(CaptureRequest.EDGE_MODE, SuperUtil.edgeMode);
            }
            if (SuperUtil.noiseReductionMode != -1) {
                previewRequestBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, SuperUtil.noiseReductionMode);
            }
            if (SuperUtil.aeMeterMode != -1) {
                previewRequestBuilder.set(VendorTagRequest.CONTROL_QCOM_AE_METER_MODE, SuperUtil.aeMeterMode);
            }
            if (SuperUtil.saturation != -1) {
                previewRequestBuilder.set(VendorTagRequest.CONTROL_QCOM_SATURATION_LEVEL, SuperUtil.saturation);
            }
            if (SuperUtil.sharpness != -1) {
                previewRequestBuilder.set(VendorTagRequest.CONTROL_QCOM_SHARPNESS_STRENGTH, SuperUtil.sharpness);
            }
            if (SuperUtil.contrast != -1) {
                previewRequestBuilder.set(VendorTagRequest.CONTROL_QCOM_CONTRAST_LEVEL, SuperUtil.contrast);
            }

            if (SuperUtil.USE_PLATFORM_MFNR) {
                previewRequestBuilder.set(CaptureRequest.CONTROL_ENABLE_ZSL, true);
            }
            return previewRequestBuilder;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<CaptureRequest.Builder> createCaptureRequest(CameraDevice mCameraDevice) {

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

//            captureBuilder.set(CaptureRequest.CONTROL_ENABLE_ZSL, true);
            captureBuilder.set(CaptureRequest.CONTROL_AWB_LOCK, true);

            if (SuperUtil.edgeMode != -1) {
                captureBuilder.set(CaptureRequest.EDGE_MODE, SuperUtil.edgeMode);
            }
            if (SuperUtil.noiseReductionMode != -1) {
                captureBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, SuperUtil.noiseReductionMode);
            }
            if (SuperUtil.saturation != -1) {
                captureBuilder.set(VendorTagRequest.CONTROL_QCOM_SATURATION_LEVEL, SuperUtil.saturation);
            }
            if (SuperUtil.sharpness != -1) {
                captureBuilder.set(VendorTagRequest.CONTROL_QCOM_SHARPNESS_STRENGTH, SuperUtil.sharpness);
            }
            if (SuperUtil.contrast != -1) {
                captureBuilder.set(VendorTagRequest.CONTROL_QCOM_CONTRAST_LEVEL, SuperUtil.contrast);
            }

            if (mf) {
                if (!captureBuilder.build().getKeys().contains(VendorTagRequest.QCOM_CUSTOM_NOISE_REDUCTION)) {
                    Log.e(TAG, "QCOM_CUSTOM_NOISE_REDUCTION not exists");
                } else {
                    captureBuilder.set(CaptureRequest.CONTROL_ENABLE_ZSL, true);
                    captureBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY);
                    captureBuilder.set(VendorTagRequest.QCOM_CUSTOM_NOISE_REDUCTION, (byte) 0x01);
                }
            } else {
//                captureBuilder.set(VendorTagRequest.QCOM_CUSTOM_NOISE_REDUCTION, (byte) 0x0);
            }

            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);

//            if (getPhotoForamt() == ImageFormat.JPEG) {
//                captureBuilder.set(CaptureRequest.JPEG_QUALITY, (byte) 100);
//                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, mSaveOrientation);
//            }

            builders.add(captureBuilder);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreviewCaptureCompleted(CaptureResult result) {

        final Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
        if (faces != null && faces.length > 0) {
            SuperUtil.hasFace = true;
            Log.d(TAG, "onPreviewCaptureCompleted faces.length = "+faces.length);
        } else {
            SuperUtil.hasFace = false;
            Log.d(TAG, "onPreviewCaptureCompleted faces is null or faces.length is 0");
        }
    }

    @Override
    public void onPreviewFrame(byte[] nv21) {
    }

    @Override
    public Rect getScalerCropRegion() {
	return null;
    }
}
