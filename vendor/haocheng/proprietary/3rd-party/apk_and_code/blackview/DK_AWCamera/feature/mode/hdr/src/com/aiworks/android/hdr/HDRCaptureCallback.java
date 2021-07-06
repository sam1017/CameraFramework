package com.aiworks.android.hdr;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.util.Size;

public interface HDRCaptureCallback {

    void onCaptureStart(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result);

    void onCaptureCompleted(TotalCaptureResult result);

    void showToast(String message);

    void saveHDRData(byte[] data, int format, Size size);

    void compare(byte[] origin, byte[] result, int format);

}
