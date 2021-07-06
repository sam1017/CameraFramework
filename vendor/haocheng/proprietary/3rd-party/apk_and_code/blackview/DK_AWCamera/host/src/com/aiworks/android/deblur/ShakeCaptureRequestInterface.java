package com.aiworks.android.deblur;

import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;

import java.util.ArrayList;

public interface ShakeCaptureRequestInterface {

    /**
     * 创建防抖模式下预览请求，设置相应参数
     */
    CaptureRequest.Builder createPreviewRequest(CameraDevice mCameraDevice);

    /**
     * 获取相机预览每一帧的参数
     */
    void onPreviewCaptureCompleted(CaptureResult result);

    /**
     * 创建防抖模式下拍照请求，调用captureBurst接口下发请求，request数量即为拍照图片数量
     */
    ArrayList<CaptureRequest.Builder> createCaptureRequest(CameraDevice mCameraDevice, boolean shaking);

    /**
     * 相机预览nv21数据，通过preview callback等方式获取
     */
    void onPreviewFrame(byte[] nv21, int width, int height, int orientation);

    /**
     * 算法库及其他释放
     */
    void destory();

}
