package com.aiworks.android.deblur;

import android.hardware.camera2.TotalCaptureResult;

public interface ShakeCaptureCallback {

    void onCaptureStart(TotalCaptureResult result);

    void onCaptureCompleted(TotalCaptureResult result);

    void onCaptureCompleted(int captureNum);

    void showToast(String message);

    void saveDeblurData(byte[] data, int format);

    void saveDeblurData(byte[] data, int format, String suffix);

    public void compare(byte[] origin, byte[] result, int format);
}