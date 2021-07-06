package com.aiworks.android.hdr;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.util.Size;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HDRCaptureBean {

    private long mCaptureStartTime;
    private int mTotalNum;
    private int mProcessNum;
    private int mPhotoForamt;
    private int mJpegRotation;
    private boolean mHasFace;
    private boolean mFaceCareEnabled;
    private boolean mSoftMfnrEnable;
    private Size mPhotoSize;
    private int mBaseIndex;

    private final List<TotalCaptureResult> mCaptureResultList = new ArrayList<>();
    private final List<CaptureRequest> mCaptureRequestList = new ArrayList<>();
    private final HashMap<Integer, byte[]> mYuvBufferList = new HashMap<>();

    public void setCaptureStartTime(long captureStartTime) {
        mCaptureStartTime = captureStartTime;
    }

    public long getStartTime() {
        return mCaptureStartTime;
    }

    public void setTotalNum(int totalNum) {
        mTotalNum = totalNum;
    }

    public int getTotalNum() {
        return mTotalNum;
    }

    public void setProcessNum(int processNum) {
        mProcessNum = processNum;
    }

    public int getProcessNum() {
        return mProcessNum;
    }

    public void setPhotoForamt(int photoForamt) {
        mPhotoForamt = photoForamt;
    }

    public int getPhotoForamt() {
        return mPhotoForamt;
    }


    public void setJpegRotation(int jpegRotation) {
        mJpegRotation = jpegRotation;
    }

    public int getJpegRotation() {
        return mJpegRotation;
    }

    public void setHasFace(boolean hasFace) {
        mHasFace = hasFace;
    }

    public boolean hasFace() {
        return mHasFace;
    }

    public void setFaceCareEnabled(boolean enable) {
        mFaceCareEnabled = enable;
    }

    public boolean getFaceCareEnabled() {
        return mFaceCareEnabled;
    }

    public void setSoftMfnrEnable(boolean softMfnrEnable) {
        this.mSoftMfnrEnable = softMfnrEnable;
    }

    public boolean getSoftMfnrEnable() {
        return mSoftMfnrEnable;
    }

    public void setPhotoSize(Size photoSize) {
        mPhotoSize = photoSize;
    }

    public Size getPhotoSize() {
        return mPhotoSize;
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

    public void addYuvBuffer(Integer num, byte[] yuvBuffer) {
        mYuvBufferList.put(num, yuvBuffer);
    }

    public HashMap<Integer, byte[]> getYuvBufferList() {
        return mYuvBufferList;
    }

    public int getBaseIndex() {
        return mBaseIndex;
    }

    public void setBaseIndex(int baseIndex) {
        mBaseIndex = baseIndex;
    }

}
