package com.aiworks.android.hdr;

import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
import android.media.ImageReader;
import android.util.Range;
import android.util.Size;

public interface HDRCaptureResultInterface {

    String HDR_CAPTURE_RESULT = "com.aiworks.android.hdr.HDRCaptureResult";
    String HDRLITE_CAPTURE_RESULT = "com.aiworks.android.hdr.HDRLiteCaptureResult";

    /**
     * 算法库及其他初始化
     */
    void init(Context context, Size photoSize, Range<Integer> evRange);

    /**
     * 算法库及其他释放
     */
    void destory();

    /**
     * 设置图片回调接口
     */
    void setCaptureCallback(HDRCaptureCallback callback);

    /**
     * 拍照开始时调用
     */
    void onStartCapture(int frameNum, int format, int jpegRotation);

    /**
     * 设置相机拍照ImageReader的回调，获取返回的图片，送给算法做处理
     */
    ImageReader.OnImageAvailableListener getImageAvailableListener();

    /**
     * 设置相机拍照CaptureCallback，获取底层返回的参数信息，创建Exif等
     */
    CameraCaptureSession.CaptureCallback getCaptureCallback();

}
