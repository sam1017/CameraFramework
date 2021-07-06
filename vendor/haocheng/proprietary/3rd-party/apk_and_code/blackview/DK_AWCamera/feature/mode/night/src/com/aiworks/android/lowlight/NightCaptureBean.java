package com.aiworks.android.lowlight;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build;
import android.util.Size;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NightCaptureBean {

    private final long mStartTime;
    private final int mTotalNum;
    private final int mJpegRotation;
    private int mProcessNum;

    private Size mPhotoSize;
    private final List<TotalCaptureResult> mCaptureResultList = new ArrayList<>();
    private final List<CaptureRequest> mCaptureRequestList = new ArrayList<>();
    private final HashMap<Integer, YuvImage> mYuvBufferList = new HashMap<>();
    private AIWorksNsExpJni.AIWorksNsAlgoParam mAlgoParam;

    public NightCaptureBean(long startTime, int totalNum, int jpegRotation) {
        mStartTime = startTime;
        mTotalNum = totalNum;
        mJpegRotation = jpegRotation;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public int getTotalNum() {
        return mTotalNum;
    }

    public int getJpegRotation() {
        return mJpegRotation;
    }

    public void setProcessNum(int processNum) {
        mProcessNum = processNum;
    }

    public int getProcessNum() {
        return mProcessNum;
    }

    public void addTotalCaptureResult(TotalCaptureResult captureResult) {
        mCaptureResultList.add(captureResult);
    }

    public List<TotalCaptureResult> getCaptureResultList() {
        return mCaptureResultList;
    }

    public void addCaptureRequest(CaptureRequest captureRequest) {
        mCaptureRequestList.add(captureRequest);
    }

    public List<CaptureRequest> getCaptureRequestList() {
        return mCaptureRequestList;
    }

    public void addYuvBuffer(Integer num, YuvImage yuvImage) {
        mYuvBufferList.put(num, yuvImage);
        mPhotoSize = new Size(yuvImage.getWidth(), yuvImage.getHeight());
    }

    public Size getPhotoSize() {
        return mPhotoSize;
    }

    public HashMap<Integer, YuvImage> getYuvBufferList() {
        return mYuvBufferList;
    }

    public void setAlgoParam(AIWorksNsExpJni.AIWorksNsAlgoParam algoParam) {
        mAlgoParam = algoParam;
    }

    public AIWorksNsExpJni.AIWorksNsAlgoParam getAlgoParam() {
        return mAlgoParam;
    }

    public void clear() {
        mYuvBufferList.clear();
        System.gc();
    }

    public void dump(String dumpPath, YuvImage result) {
        for (int num = 0; num < mYuvBufferList.size(); num++) {
            dumpYuvDate(mYuvBufferList.get(num), dumpPath + "_" + num + ".jpg", mPhotoSize);
        }
        dumpYuvDate(result, dumpPath + "_result.jpg", mPhotoSize);
        dumpCaptureResult(dumpPath);
    }

    private void dumpCaptureResult(String dumpPath) {
        String fileName = "_result.txt";
        StringBuilder sb = new StringBuilder();
        for (int num = 0; num < mCaptureResultList.size(); num++) {
            TotalCaptureResult captureResult = mCaptureResultList.get(num);
            if (captureResult != null) {
                sb.append(num).append("    expTimeUs :").append((int) (captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME) / 1000)).append("\r\n");
                sb.append(num).append("    sensitivity :").append(captureResult.get(CaptureResult.SENSOR_SENSITIVITY)).append("\r\n");
                sb.append(num).append("    gain :").append(captureResult.get(CaptureResult.CONTROL_POST_RAW_SENSITIVITY_BOOST)).append("\r\n");
                sb.append(num).append("    focusDistance :").append(captureResult.get(CaptureResult.LENS_FOCUS_DISTANCE)).append("\r\n");
                sb.append(num).append("    ev :").append(captureResult.get(CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION)).append("\r\n");
            }
        }
        dumpFile(dumpPath + fileName, sb.toString().getBytes());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void dumpYuvDate(YuvImage yuvImage, String fileName, Size size) {
        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream(new File(fileName));
            yuvImage.compressToJpeg(new Rect(0, 0, size.getWidth(), size.getHeight()), 100, fs);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void dumpFile(String fileName, byte[] data) {
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(fileName);
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to create output file " + fileName, ioe);
        }
        try {
            outStream.write(data);
            outStream.close();
        } catch (IOException ioe) {
            throw new RuntimeException("failed writing data to file " + fileName, ioe);
        }
    }

}
