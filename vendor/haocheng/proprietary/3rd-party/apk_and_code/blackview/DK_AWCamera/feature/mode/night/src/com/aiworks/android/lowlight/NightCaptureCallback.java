package com.aiworks.android.lowlight;

import android.hardware.camera2.TotalCaptureResult;
import android.util.Size;

public interface NightCaptureCallback {

    void onCaptureStart(TotalCaptureResult result);

    void onCaptureCompleted(TotalCaptureResult result);

    void showToast(String message);

    void saveNightData(byte[] data, int format, Size size);

}
