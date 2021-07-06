package com.aiworks.android.hdr;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.Face;
import android.os.Build;
import android.util.Size;

import com.aiworks.android.camera.VendorTagMetadata;
import com.aiworks.android.camera.VendorTagRequest;
import com.aiworks.android.utils.Product;

import java.util.ArrayList;

import static android.hardware.camera2.CaptureRequest.SCALER_CROP_REGION;

@TargetApi(Build.VERSION_CODES.O_MR1)
public class HDRCaptureRequest implements HDRCaptureRequestInterface {

    private static final String TAG = "HDRCaptureRequest";

    private Integer sensitivity = 100;
    private Long exposureTime = 30000000L;
    private Integer postRawSensitivityBoost = 100;
    private Float focusDistance = 1.0f;
    private boolean mHasFace = false;
    //bv wuyonglin add for bug5276 20210415 start
    private float mCurZoomRatio = 1.0f;
    private Rect mSensorRect;
    //bv wuyonglin add for bug5276 20210415 end
    private HDRExpEngine mHDRExpEngine;
    private Rect mScalerCropRegion;

    public HDRCaptureRequest() {
        mHDRExpEngine = HDRExpEngine.getInstance();
        mHDRExpEngine.createExposureCaculator(HDRConfig.cameraID == 1);
    }

    public Size getPhotoSize() {
        return HDRConfig.getPhotoSize();
    }

    public int getPhotoForamt() {
        return HDRConfig.getPhotoForamt();
    }

    public CaptureRequest.Builder createPreviewRequest(CameraDevice mCameraDevice) {
        try {
            CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            if (HDRConfig.getFaceAeEnable()) {
                previewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE);
                if (Product.mPlatformID == Product.HARDWARE_PLATFORM_MTK) {
                    previewRequestBuilder.set(VendorTagRequest.MTK_FACE_FORCE_3A, VendorTagMetadata.MTK_FACE_FORCE_3A_ON);
                }
            } else {
                previewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF);
                if (Product.mPlatformID == Product.HARDWARE_PLATFORM_MTK) {
                    previewRequestBuilder.set(VendorTagRequest.MTK_FACE_FORCE_3A, VendorTagMetadata.MTK_FACE_FORCE_3A_OFF);
                }
            }
            if (Product.mPlatformID == Product.HARDWARE_PLATFORM_QCOM) {
                previewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_FACE_PRIORITY);
                previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE);
            }
            return previewRequestBuilder;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<CaptureRequest.Builder> createCaptureRequest(CameraDevice mCameraDevice) {
        ArrayList<CaptureRequest.Builder> builders = new ArrayList<>();

        mHDRExpEngine.startCapture(exposureTime, sensitivity, postRawSensitivityBoost, mHasFace);

        boolean manualExp = HDRConfig.getManualExp();
        mHDRExpEngine.process(0);
        CaptureRequest.Builder captureBuilder = addCaptureRequest(mCameraDevice, manualExp, 0);
        mHDRExpEngine.process(-1);
        CaptureRequest.Builder captureBuilder2 = addCaptureRequest(mCameraDevice, manualExp, -1);
        mHDRExpEngine.process(1);
        CaptureRequest.Builder captureBuilder3 = addCaptureRequest(mCameraDevice, manualExp, 1);

//        for (int i = 0; i < HDRConfig.getMidExposureCount(); i++) {
            builders.add(captureBuilder);
//        }
//        for (int i = 0; i < HDRConfig.getMaxExposureCount(); i++) {
            builders.add(captureBuilder3);
//        }
//        for (int i = 0; i < HDRConfig.getMinExposureCount(); i++) {
            builders.add(captureBuilder2);
//        }
        //if ("A100".equals(Product.MODEL_NAME)) {
        //    builders.add(captureBuilder);
        //}

        if ("BL5000".equals(Product.MODEL_NAME)) {
            builders.add(addAECaptureRequest(mCameraDevice));
        }
        if (HDRConfig.SOFT_MFNR_ENABLE) {
            builders.add(captureBuilder);
        }
        return builders;
    }

    private CaptureRequest.Builder addCaptureRequest(CameraDevice mCameraDevice, boolean manualExp, int adjEV) {
        CaptureRequest.Builder captureBuilder = null;
        try {
            captureBuilder = mCameraDevice.createCaptureRequest(HDRConfig.getTempLateType());
            //captureBuilder.set(SCALER_CROP_REGION, mScalerCropRegion);
            captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, mSensorRect);
            captureBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO,mCurZoomRatio);


            if (HDRConfig.getTempLateType() == CameraDevice.TEMPLATE_STILL_CAPTURE) {
                captureBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true);
            }
            if (focusDistance != 0) {
                captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focusDistance);
            }
            if (manualExp) {
                captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, mHDRExpEngine.getResultExpTime());
                captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, mHDRExpEngine.getResultSensitity());
                captureBuilder.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, postRawSensitivityBoost);
            } else {
                captureBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, mHDRExpEngine.getResultEV());
            }

            if (HDRConfig.getFaceAeEnable()) {
                captureBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE);
                if (Product.mPlatformID == Product.HARDWARE_PLATFORM_MTK) {
                    captureBuilder.set(VendorTagRequest.MTK_FACE_FORCE_3A, VendorTagMetadata.MTK_FACE_FORCE_3A_ON);
                }
            } else {
                captureBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF);
                if (Product.mPlatformID == Product.HARDWARE_PLATFORM_MTK) {
                    captureBuilder.set(VendorTagRequest.MTK_FACE_FORCE_3A, VendorTagMetadata.MTK_FACE_FORCE_3A_OFF);
                }
            }

            if (HDRConfig.getMtkMfnrEnable() && Product.mPlatformID == Product.HARDWARE_PLATFORM_MTK && !mHDRExpEngine.hasFace()) {
                captureBuilder.set(VendorTagRequest.MTK_MFB_MODE, new int[]{255});
            }

            if (adjEV == 0) {
                if (HDRConfig.getMidEdgeMode() != -1) {
                    captureBuilder.set(CaptureRequest.EDGE_MODE, HDRConfig.getMidEdgeMode());
                }
                if (HDRConfig.getMidNoiseMode() != -1) {
                    captureBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, HDRConfig.getMidNoiseMode());
                }
                if(HDRConfig.mMidMfnr/* && !mHDRExpEngine.hasFace()*/) {
                    captureBuilder.set(VendorTagRequest.MTK_MFB_MODE, new int[]{255});
                }
            } else if (adjEV < 0) {
                if (HDRConfig.getMinEdgeMode() != -1) {
                    captureBuilder.set(CaptureRequest.EDGE_MODE, HDRConfig.getMinEdgeMode());
                }
                if (HDRConfig.getMinNoiseMode() != -1) {
                    captureBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, HDRConfig.getMinNoiseMode());
                }
                if(HDRConfig.mMinMfnr/* && !mHDRExpEngine.hasFace()*/) {
                    captureBuilder.set(VendorTagRequest.MTK_MFB_MODE, new int[]{255});
                }
            } else {
                if (HDRConfig.getMaxEdgeMode() != -1) {
                    captureBuilder.set(CaptureRequest.EDGE_MODE, HDRConfig.getMaxEdgeMode());
                }
                if (HDRConfig.getMaxNoiseMode() != -1) {
                    captureBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, HDRConfig.getMaxNoiseMode());
                }
                if(HDRConfig.mMaxMfnr/* && !mHDRExpEngine.hasFace()*/) {
                    captureBuilder.set(VendorTagRequest.MTK_MFB_MODE, new int[]{255});
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return captureBuilder;
    }

    private CaptureRequest.Builder addAECaptureRequest(CameraDevice mCameraDevice) {
        CaptureRequest.Builder captureBuilder = null;
        try {
            captureBuilder = mCameraDevice.createCaptureRequest(HDRConfig.getTempLateType());
            captureBuilder.set(CaptureRequest.CONTROL_AE_LOCK, false);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return captureBuilder;
    }

    public void onPreviewCaptureCompleted(CaptureResult result) {
        sensitivity = result.get(CaptureResult.SENSOR_SENSITIVITY);
        exposureTime = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
        postRawSensitivityBoost = result.get(CaptureResult.CONTROL_POST_RAW_SENSITIVITY_BOOST);
        if (postRawSensitivityBoost == null) {
            postRawSensitivityBoost = 100;
        }
        mScalerCropRegion = result.get(CaptureResult.SCALER_CROP_REGION);
        focusDistance = result.get(CaptureResult.LENS_FOCUS_DISTANCE);

        Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
        mHasFace = faces != null && faces.length > 0;
        //bv wuyonglin add for bug5276 20210415 start
        mCurZoomRatio = result.get(CaptureResult.CONTROL_ZOOM_RATIO);
        mSensorRect = result.get(CaptureResult.SCALER_CROP_REGION);
        //bv wuyonglin add for bug5276 20210415 end
    }

    public boolean onPreviewFrame(byte[] nv21, int width, int height, int orientation) {
        return mHDRExpEngine.hdrHist(nv21, width, height);
    }

    public Rect getScalerCropRegion(){
        return mScalerCropRegion;
    }

}
