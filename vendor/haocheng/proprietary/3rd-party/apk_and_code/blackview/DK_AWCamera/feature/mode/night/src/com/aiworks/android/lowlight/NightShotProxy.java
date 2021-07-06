package com.aiworks.android.lowlight;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.hardware.camera2.CaptureRequest;
import android.os.ConditionVariable;
import android.util.Log;
import android.util.Size;

import com.jni.lib.AiWorksNsJni;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class NightShotProxy {

    public static final String TAG = NightShotProxy.class.getSimpleName();

    private static volatile NightShotProxy mInstance;

    private NightShotProxy() {
    }

    public static NightShotProxy getInstance() {
        if (mInstance == null) {
            synchronized (NightShotProxy.class) {
                if (mInstance == null) {
                    mInstance = new NightShotProxy();
                }
            }
        }
        return mInstance;
    }

    private AiWorksNsJni mNsJni;
    private Size mPhotoSize;
    private final List<Long> mIdList = new ArrayList<>();
    private int mMaxCacheNum = 1;
    private volatile boolean mDestoryFlag = false;

    // Called when onResume
    public void init(Context context, String binPath) {
        synchronized (this) {
            mDestoryFlag = false;
            if (mNsJni == null) {
                mMaxCacheNum = getMaxCacheNum(context);
                Log.i(TAG, "init binPath = " + binPath);
                mNsJni = new AiWorksNsJni(context);
                int ret = mNsJni.init(binPath, binPath);
                Log.i(TAG, "init ret = " + ret);
            }
        }
    }

    // Called when onPause
    public void release() {
        synchronized (this) {
            if (mIdList.isEmpty()) {
                if (mNsJni != null) {
                    Log.i(TAG, "release");
                    mNsJni.release();
                    mNsJni = null;
                }
                mPhotoSize = null;
                mDestoryFlag = false;
            } else {
                // Exit when there are tasks to process
                mDestoryFlag = true;
            }
        }
    }

    private int getMaxCacheNum(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int largeMemoryClass = activityManager.getLargeMemoryClass();
        Log.i(TAG, "largeMemoryClass = " + largeMemoryClass);
        if (largeMemoryClass <= 256) {
            return 1;
        } else if (largeMemoryClass <= 384) {
            return 3;
        } else {
            return 5;
        }
    }

    public void setPhotoSize(Size photoSize, int[] pitch) {
        if (mNsJni != null && photoSize != null) {
            if (mPhotoSize == null || mPhotoSize.getWidth() != photoSize.getWidth() || mPhotoSize.getHeight() != photoSize.getHeight()) {
                // Need to process all the tasks before switch resolution
                while (!mIdList.isEmpty()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.i(TAG, "setResolution E size = " + mPhotoSize);
                mNsJni.setResolution(photoSize.getWidth(), photoSize.getHeight(), pitch,
                        AiWorksNsJni.NS_MODE_HDR, true, false);
                mPhotoSize = photoSize;
                Log.i(TAG, "setResolution X size = " + mPhotoSize +", pitch = " + Arrays.toString(pitch));
            }
        }
    }

    public YuvImage process(NightCaptureBean nightCaptureBean, boolean async) {
        AIWorksNsExpJni.AIWorksNsAlgoParam algoParam = nightCaptureBean.getAlgoParam();
        HashMap<Integer, YuvImage> yuvImageList = nightCaptureBean.getYuvBufferList();
        Size photoSize = nightCaptureBean.getPhotoSize();
        int[] photoStrides = yuvImageList.get(0).getStrides();
        int jpegRotation = nightCaptureBean.getJpegRotation();
        int totalNum = nightCaptureBean.getProcessNum();
        long id = -1;
        synchronized (this) {
            setPhotoSize(photoSize, photoStrides);
                Log.i(TAG, "process mIdList.size()  = " + mIdList.size()+" mMaxCacheNum ="+mMaxCacheNum);
            while (mIdList.size() >= mMaxCacheNum) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
                Log.i(TAG, "process mNsJni  = " + mNsJni);
            if (mNsJni != null) {
                id = mNsJni.getInstance();
                mIdList.add(id);
                Log.i(TAG, "getInstance id = " + id);
            }
        }
        if (id < 0) {
                Log.i(TAG, "return process id  = " + id+" algoParam.baseIndex ="+algoParam.baseIndex);
            return yuvImageList.get(algoParam.baseIndex);
        }
        mNsJni.setParameter(id, totalNum, /*algoParam.hasFace != 0*/false, AiWorksNsJni.getFaceSegmentOrientation(jpegRotation),
                algoParam.nrLevel, algoParam.lnrLevel, algoParam.tmoLevel, (float) algoParam.ceLevel, (float) algoParam.spLevel);
        for (int num = 0; num < totalNum; num++) {
            CaptureRequest request = nightCaptureBean.getCaptureRequestList().get(num);
            byte[] yuvBuffer = yuvImageList.get(num).getYuvData();
            Integer ev = request.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION);
            Long expTimeUs = request.get(CaptureRequest.SENSOR_EXPOSURE_TIME);
            Integer sensitivity = request.get(CaptureRequest.SENSOR_SENSITIVITY);
            Log.i(TAG, "procImageEffect id = " + id + ", num = " + num + " , ev = " + ev
                    + ", expTimeUs = " + (expTimeUs / 1000) + ", sensitivity = " + sensitivity);
            mNsJni.addFrame(id, yuvBuffer, num, ev, (int) (expTimeUs / 1000), sensitivity, algoParam.baseIndex == num);
        }
        byte[] result = null;
        int ret;
        if (async) {
            final ConditionVariable conditionVariable = new ConditionVariable(false);
            final byte[][] yuvBuffer = {null};
            ret = mNsJni.processAsync(id, new AiWorksNsJni.OnAsyncProcessCallback() {
                @Override
                public void onAsyncProcessFinish(long id, byte[] result) {
                    Log.i(TAG, "onAsyncProcessFinish id = " + id);
                    yuvBuffer[0] = result;
                    conditionVariable.open();
                }
            });
            if (ret == 0) {
                conditionVariable.block();
                result = yuvBuffer[0];
            }
            Log.i(TAG, "processAsync id = " + id + ", ret = " + ret);
        } else {
            result = new byte[(photoStrides[0] + photoStrides[1] / 2) * photoSize.getHeight()];
            ret = mNsJni.process(id, result);
            Log.i(TAG, "process id = " + id + ", ret = " + ret);
        }
        mIdList.remove(id);
        if (mIdList.isEmpty() && mDestoryFlag) {
            release();
        }
        if (ret != 0) {
            return yuvImageList.get(algoParam.baseIndex);
        }
        return new YuvImage(result, ImageFormat.NV21, photoSize.getWidth(), photoSize.getHeight(), photoStrides);
    }

}
