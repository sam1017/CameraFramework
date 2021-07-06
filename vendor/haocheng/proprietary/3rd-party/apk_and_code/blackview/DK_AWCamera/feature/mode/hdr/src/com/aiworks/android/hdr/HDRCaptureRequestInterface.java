package com.aiworks.android.hdr;

import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.util.Size;

import java.util.ArrayList;

public interface HDRCaptureRequestInterface {

    String HDR_CAPTURE_REQUEST = "com.aiworks.android.hdr.HDRCaptureRequest";
    String HDRLITE_CAPTURE_REQUEST = "com.aiworks.android.hdr.HDRLiteCaptureRequest";

    /**
     * 拍照图片分辨率
     */
    Size getPhotoSize();

    /**
     * 拍照图片格式
     */
    int getPhotoForamt();

    /**
     * 创建HDR模式下预览请求，设置相应参数
     */
    CaptureRequest.Builder createPreviewRequest(CameraDevice mCameraDevice);

    /**
     * 获取相机预览每一帧的参数
     */
    void onPreviewCaptureCompleted(CaptureResult result);

    /**
     * 创建HDR模式下拍照请求，调用captureBurst接口下发请求，request数量即为拍照图片数量
     */
    ArrayList<CaptureRequest.Builder> createCaptureRequest(CameraDevice mCameraDevice);

    /**
     * 相机预览nv21数据，通过preview callback等方式获取
     */
    boolean onPreviewFrame(byte[] nv21, int width, int height, int orientation);

}
