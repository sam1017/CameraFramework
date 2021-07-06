package com.aiworks.android.lowlight;

import android.content.Context;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;

import java.util.ArrayList;

public interface NightCaptureRequestInterface {

    /**
     * 算法库及其他初始化，cameraID需要与xml中的id相对应
     */
    void init(Context context, String xmlPath, int cameraID);

    /**
     * 获取相机预览每一帧的参数
     */
    void onPreviewCaptureCompleted(CaptureResult result);

    /**
     * 创建夜景模式下拍照请求，调用captureBurst接口下发请求，request数量即为拍照图片数量
     */
    ArrayList<CaptureRequest.Builder> createCaptureRequest(CameraDevice mCameraDevice);

    /**
     * 相机预览nv21数据，通过preview callback等方式获取
     */
    void onPreviewFrame(byte[] nv21, int width, int height, int jpegRotation);

    /**
     * 算法库及其他释放
     */
    void destory();

}
