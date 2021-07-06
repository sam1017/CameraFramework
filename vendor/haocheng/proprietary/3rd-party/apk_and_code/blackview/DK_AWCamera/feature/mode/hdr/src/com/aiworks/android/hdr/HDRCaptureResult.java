package com.aiworks.android.hdr;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.util.Log;
import android.util.Range;
import android.util.Size;

import androidx.annotation.NonNull;

import com.aiworks.android.utils.ImageFormatUtil;
import com.aiworks.android.utils.Product;
import com.aiworks.android.utils.Util;
import com.aiworks.yuvUtil.YuvEncodeJni;
import com.jni.lib.AiWorksHdrJni;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.O_MR1)
public class HDRCaptureResult implements HDRCaptureResultInterface {

    private static final String TAG = "HDRCaptureResult";

    private static final String MODEL_DIR = "AIWorksModels";

    private AiWorksHdrJni mMergeHdrJni;

    private Size mPhotoSize;
    private HDRCaptureCallback mCallback;

    private HDRExpEngine mHDRExpEngine;

    private int mImageNum;
    private int mCaptureNum;
    private int mStartedNum;
    private int mCompletedNum;

    private HDRCaptureBean mCurrentCaptureBean;
    private final LinkedList<HDRCaptureBean> mCaptureBeanQueue = new LinkedList<>();

    @Override
    public void init(Context context, Size photoSize, Range<Integer> evRange) {
        mPhotoSize = photoSize;

        synchronized (this) {
            if (mMergeHdrJni == null) {
                String binPath = context.getExternalFilesDir(MODEL_DIR).getAbsolutePath() + "/";
                Util.copyModle(context, binPath, MODEL_DIR);
                Log.i(TAG, "MergeHdrJni binPath = " + binPath);

                mMergeHdrJni = new AiWorksHdrJni(context);
                Log.i(TAG, "MergeHdrJni initOpenCL E");
                mMergeHdrJni.init(binPath);
                Log.i(TAG, "MergeHdrJni initOpenCL X");
            }
            Log.i(TAG, "MergeHdrJni setPhotoSize = " + mPhotoSize);
            mMergeHdrJni.setParameter(mPhotoSize.getWidth(), mPhotoSize.getHeight());
        }

        mHDRExpEngine = HDRExpEngine.getInstance();
        mHDRExpEngine.setAECompensationRange(evRange);

    }

    public void destory() {
        Log.i(TAG, "destory");
        synchronized (this) {
            if (mMergeHdrJni != null) {
                mMergeHdrJni.release();
                mMergeHdrJni = null;
            }
        }
    }

    @Override
    public void setCaptureCallback(HDRCaptureCallback callback) {
        if (mCallback != null && callback == null) {
            mCallback.onCaptureCompleted(null);
        }
        mCallback = callback;
    }

    @Override
    public void onStartCapture(int frameNum, int format, int jpegRotation) {
        Log.d(TAG, "onStartCapture frameNum = " + frameNum + ", format = " + format);

        mCurrentCaptureBean = new HDRCaptureBean();
        mCurrentCaptureBean.setCaptureStartTime(System.currentTimeMillis());
        mCurrentCaptureBean.setTotalNum(frameNum);
        mCurrentCaptureBean.setProcessNum("BL5000".equals(Product.MODEL_NAME) ? frameNum - 1 : frameNum);
        mCurrentCaptureBean.setPhotoForamt(format);
        mCurrentCaptureBean.setJpegRotation(jpegRotation);
        mCurrentCaptureBean.setHasFace(mHDRExpEngine.hasFace());
        mCurrentCaptureBean.setFaceCareEnabled(mHDRExpEngine.getFaceCareEnabled());
        mCurrentCaptureBean.setPhotoSize(mPhotoSize);
        mCurrentCaptureBean.setSoftMfnrEnable(HDRConfig.SOFT_MFNR_ENABLE);
        mCurrentCaptureBean.setBaseIndex(0);
        mCaptureBeanQueue.offer(mCurrentCaptureBean);

        mImageNum = 0;
        mCaptureNum = 0;
        mStartedNum = 0;
        mCompletedNum = 0;
    }

    @Override
    public ImageReader.OnImageAvailableListener getImageAvailableListener() {
        return mOnImageAvailableListener;
    }

    @Override
    public CameraCaptureSession.CaptureCallback getCaptureCallback() {
        return mCaptureCallback;
    }

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            final Image image = reader.acquireNextImage();

            final int num = mCaptureNum;
            mCaptureNum++;

//            new Thread() {
//                @Override
//                public void run() {
//                    super.run();
                    byte[] yuvbuffer;
                    int width = image.getWidth();
                    int height = image.getHeight();
                    Log.i(TAG, "onImageAvailable format = " + image.getFormat() + ", width = " + width + ", height = " + height);
                    if (image.getFormat() == ImageFormat.JPEG) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        image.close();
                        yuvbuffer = YuvEncodeJni.getInstance().Jpeg2Nv21(data, width, height, 1, 0, false);
                    } else {
                        yuvbuffer = YuvEncodeJni.getInstance().getBuffer(image,false);
                        image.close();
                    }
                    procImageEffect(yuvbuffer, num);
//                }
//            }.start();
        }

    };

    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            mCurrentCaptureBean.addCaptureRequest(request);
            Integer ev = request.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION);
            Long exposureTime = request.get(CaptureRequest.SENSOR_EXPOSURE_TIME);
            Integer sensitivity = request.get(CaptureRequest.SENSOR_SENSITIVITY);
            Log.d(TAG, "onCaptureStarted    " + mStartedNum + " , ev = " + ev + ", expTimeUs = " + (exposureTime / 1000) + ", sensitivity = " + sensitivity);
            mStartedNum++;
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            mCurrentCaptureBean.addTotalCaptureResult(result);
            if (mCompletedNum == mCurrentCaptureBean.getBaseIndex() && mCallback != null) {
                mCallback.onCaptureStart(session, request, result);
            }
            Integer ev = result.get(CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION);
            Long exposureTime = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
            Integer sensitivity = result.get(CaptureResult.SENSOR_SENSITIVITY);
            Log.d(TAG, "onCaptureCompleted    " + mCompletedNum + " , ev = " + ev + ", expTimeUs = " + (exposureTime / 1000) + ", sensitivity = " + sensitivity);
            mCompletedNum++;

        }
    };

    private synchronized void procImageEffect(byte[] yuvBuffer, int num) {
        HDRCaptureBean currentBean = mCurrentCaptureBean;
        currentBean.addYuvBuffer(num, yuvBuffer);

        Log.i(TAG,"procImageEffect num = " + num + " currentBean.getCaptureRequestList().size = " + currentBean.getCaptureRequestList().size());
        if (mMergeHdrJni != null) {
            int index = 0;
            if (currentBean.getCaptureRequestList().size() > 0) {
            if(num < currentBean.getCaptureRequestList().size()){
                index = num;
            }else{
                index = currentBean.getCaptureRequestList().size() -1;
            }
            CaptureRequest request = currentBean.getCaptureRequestList().get(index);
            Integer ev = request.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION);
            Long expTimeUs = request.get(CaptureRequest.SENSOR_EXPOSURE_TIME);
            Integer sensitivity = request.get(CaptureRequest.SENSOR_SENSITIVITY);
            Log.i(TAG, "procImageEffect    " + index + " , ev = " + ev + ", expTimeUs = " + (expTimeUs / 1000) + ", sensitivity = " + sensitivity);
            synchronized (this) {
                if (num == 0) {
                    mMergeHdrJni.start(3, HDRConfig.mSaturation, currentBean.hasFace(),
                            AiWorksHdrJni.getFaceSegmentOrientation(currentBean.getJpegRotation()), false);
                    mMergeHdrJni.setCameraMotionThreshold(0.6f);
                    mMergeHdrJni.setFaceCareEnabled(currentBean.getFaceCareEnabled());
                }
                if (num < currentBean.getProcessNum()) {
                    mMergeHdrJni.addImage(yuvBuffer, ev, (int) (expTimeUs / 1000), sensitivity, num == currentBean.getBaseIndex());
                }
            }
            }
        }
        mImageNum++;

        if (mImageNum == currentBean.getTotalNum()) {
            currentBean = mCaptureBeanQueue.removeFirst();
            long startTime = currentBean.getStartTime();
            int totalNum = currentBean.getTotalNum();
            int baseIndex = currentBean.getBaseIndex();
            Size photoSize = currentBean.getPhotoSize();
            HashMap<Integer, byte[]> yuvBufferList = currentBean.getYuvBufferList();

            long cost1 = (System.currentTimeMillis() - startTime);

            if (mCallback != null) {
                mCallback.onCaptureCompleted(null);
            }
            if (totalNum == 1) {
                mCallback.saveHDRData(yuvBufferList.get(0), ImageFormat.NV21, photoSize);
            } else {
                byte[] result = new byte[photoSize.getWidth() * photoSize.getHeight() * 3 / 2];
                int ret = -1;
                synchronized (this) {
                    if (mMergeHdrJni != null) {
                        ret = mMergeHdrJni.process(result);
                    }
                }
                if (ret != 0) {
                    result = yuvBufferList.get(baseIndex);
                }

                long cost2 = (System.currentTimeMillis() - startTime - cost1);
                String message = "frame num : " + totalNum + ", capture used : " + cost1 + ", algo used: " + cost2;
                Log.i(TAG, message);
                if (mCallback != null) {
                    mCallback.saveHDRData(result, ImageFormat.NV21, photoSize);
                }
                if (HDRConfig.isDump()) {
                    String path = HDRConfig.getDumpFilePath();
                    for (int i = 0; i < totalNum; i++) {
                        ImageFormatUtil.dumpYuvDate(yuvBufferList.get(i), path + "_" + i + ".jpg", photoSize);
                    }
                    ImageFormatUtil.dumpYuvDate(result, path + "_result.jpg", photoSize);
                    dumpCaptureResult(currentBean.getCaptureResultList());
                }
            }
            yuvBufferList.clear();
            System.gc();
        }
    }

    private void dumpCaptureResult(List<TotalCaptureResult> captureResultList) {
        String fileName = "_result.txt";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < captureResultList.size(); i++) {
            sb.append(i + "    expTimeUs :" + (int) (captureResultList.get(i).get(CaptureResult.SENSOR_EXPOSURE_TIME) / 1000)).append("\r\n");
            sb.append(i + "    sensitivity :" + captureResultList.get(i).get(CaptureResult.SENSOR_SENSITIVITY)).append("\r\n");
            sb.append(i + "    focusDistance :" + captureResultList.get(i).get(CaptureResult.LENS_FOCUS_DISTANCE)).append("\r\n");
            sb.append(i + "    ev :" + captureResultList.get(i).get(CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION)).append("\r\n");
        }
        ImageFormatUtil.dumpFile(HDRConfig.getDumpFilePath() + fileName, sb.toString().getBytes());
    }

}
