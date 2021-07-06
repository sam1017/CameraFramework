package com.aiworks.android.Super;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.camera2.CameraCaptureSession;
import android.media.ImageReader;
import android.util.Size;

import com.aiworks.android.multiframe.MultiFrameCaptureCallback;

public interface SuperCaptureResultInterface {

    /**
     * 算法库及其他初始化
     */
    void init(Context context, Size photoSize, Size previewSize, Rect activeRect);

    /**
     * 算法库及其他释放
     */
    void destory();

    /**
     * 设置图片回调接口
     */
    void setCaptureCallback(MultiFrameCaptureCallback callback);

    /**
     * 拍照开始时调用
     */
    void onStartCapture(int frameNum, int format);

    /**
     * 设置相机拍照ImageReader的回调，获取返回的图片，送给算法做处理
     */
    ImageReader.OnImageAvailableListener getImageAvailableListener();

    /**
     * 设置相机拍照CaptureCallback，获取底层返回的参数信息，创建Exif等
     */
    CameraCaptureSession.CaptureCallback getCaptureCallback();

}
