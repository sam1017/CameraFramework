package com.aiworks.android.multiframe;

import android.hardware.camera2.TotalCaptureResult;

public interface MultiFrameCaptureCallback {

    void onCaptureStart(TotalCaptureResult result);

    void onCaptureCompleted(TotalCaptureResult result);

    void showToast(String message);

    void saveData(byte[] data, int format, String title);

    void compare(byte[] origin, byte[] result, int format);

}
