package com.aiworks.android.lowlight;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;

import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import com.aiworks.android.utils.Product;

import com.aiworks.android.utils.Util;
import com.aiworks.yuvUtil.YuvEncodeJni;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NightCaptureResult implements NightCaptureResultInterface {

    protected static final String TAG = "NightCaptureResult";

    private static final String MODEL_DIR = "AIWorksModels";

    private final NightShotProxy mNsProxy;
    private NightCaptureCallback mCallback;
    private YuvEncodeJni mYuvEncodeJni;
    private Size mPhotoSize;
    private Size mCapturePhotoSize;

    private int mTotalNum;
    private int mImageNum;
    private int mCaptureNum;
    private int mStartedNum;
    private int mCompletedNum;

    private NightCaptureBean mCurrentCaptureBean;
    private final LinkedList<NightCaptureBean> mCaptureBeanQueue = new LinkedList<>();
    private final ExecutorService mExecutor;

    public NightCaptureResult() {
        mNsProxy = NightShotProxy.getInstance();
        mExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void init(final Context context, final Size photoSize) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                String binPath = context.getExternalFilesDir(MODEL_DIR).getAbsolutePath() + "/";
                Util.copyModle(context, binPath, MODEL_DIR);
                mNsProxy.init(context, binPath);
                mNsProxy.setPhotoSize(photoSize, new int[] {photoSize.getWidth(), photoSize.getWidth()});
                mPhotoSize = photoSize;
            }
        });
        mYuvEncodeJni = YuvEncodeJni.getInstance();
    }

    @Override
    public void destory() {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                mNsProxy.release();
            }
        });
    }

    @Override
    public void setCaptureCallback(NightCaptureCallback callback) {
        mCallback = callback;
    }

    @Override
    public void onStartCapture(final int frameNum, final int format, final int jpegRotation) {
        Log.w(TAG, "onStartCapture frameNum = " + frameNum + ", format = " + format);
        mTotalNum = frameNum;
        mImageNum = 0;
        mCaptureNum = 0;
        mStartedNum = 0;
        mCompletedNum = 0;

        mCurrentCaptureBean = new NightCaptureBean(System.currentTimeMillis(), mTotalNum, jpegRotation);
        mCurrentCaptureBean.setAlgoParam(AIWorksNsExpJni.getInstance().getAIWorksNsAlgoParam());
        mCurrentCaptureBean.setProcessNum(frameNum);
        mCaptureBeanQueue.offer(mCurrentCaptureBean);
        mCapturePhotoSize = mPhotoSize;

    }

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {

            final Image image = reader.acquireNextImage();
            final int width = reader.getWidth();
            final int height = reader.getHeight();

            final int num = mCaptureNum;
            mCaptureNum++;

            YuvImage yuvImage;
            if (image.getFormat() == ImageFormat.JPEG) {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                byte[] yuvbuffer = mYuvEncodeJni.Jpeg2Nv21(data, image.getWidth(), image.getHeight(), 1, 0, false);
                yuvImage = new YuvImage(yuvbuffer, ImageFormat.NV21, width, height, new int[]{width, width});
            } else {
                yuvImage = mYuvEncodeJni.getYuvFromImage(image, false);
            }
            addFrame(yuvImage, num);
            image.close();
        }

    };

    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            mCurrentCaptureBean.addCaptureRequest(request);
            Integer ev = request.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION);
            Long expTimeUs = request.get(CaptureRequest.SENSOR_EXPOSURE_TIME);
            Integer sensitivity = request.get(CaptureRequest.SENSOR_SENSITIVITY);
            Log.i(TAG, "onCaptureStarted    " + mStartedNum + " , ev = " + ev + ", expTimeUs = " + (expTimeUs / 1000) + ", sensitivity = " + sensitivity);
            mStartedNum++;
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            Log.i(TAG, "onCaptureCompleted   " + mCompletedNum);
            if (mCurrentCaptureBean.getAlgoParam().baseIndex == mCompletedNum) {
                if (mCallback != null) {
                    mCallback.onCaptureStart(result);
                }
            }
            mCurrentCaptureBean.addTotalCaptureResult(result);
            mCompletedNum++;
        }
    };

    @Override
    public ImageReader.OnImageAvailableListener getImageAvailableListener() {
        return mOnImageAvailableListener;
    }

    @Override
    public CameraCaptureSession.CaptureCallback getCaptureCallback() {
        return mCaptureCallback;
    }

    private synchronized void addFrame(YuvImage yuvImage, int num) {
        Log.i(TAG, "addFrame num = " + num);

        mCurrentCaptureBean.addYuvBuffer(num, yuvImage);

        mImageNum++;
        Log.i(TAG, "addFrame mImageNum = " + mImageNum+" mTotalNum ="+mTotalNum);
        if (mImageNum == mTotalNum) {
            final NightCaptureBean nightCaptureBean = mCaptureBeanQueue.removeFirst();
            //bv wuyonglin delete for bug5689 20210513 start
            /*if (mCallback != null) {
                mCallback.onCaptureCompleted(null);
            }*/
            //bv wuyonglin delete for bug5689 20210513 end
            notifyMergeData(nightCaptureBean);
        }
    }

    private void notifyMergeData(final NightCaptureBean nightCaptureBean) {
        final long startTime = nightCaptureBean.getStartTime();
        final long cost1 = (System.currentTimeMillis() - startTime);
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
		//bv wuyonglin add for bug5689 20210513 start
                if (mCallback != null) {
                    mCallback.onCaptureCompleted(null);
                }
		//bv wuyonglin add for bug5689 20210513 end
                YuvImage result = mNsProxy.process(nightCaptureBean, false);
                long cost2 = (System.currentTimeMillis() - startTime - cost1);
                String message = "frame num : " + mTotalNum + ", capture used : " + cost1 + ", algo used: " + cost2;
                Log.i(TAG, message);

                if (mCallback != null) {
                    mCallback.showToast(message);
                    mCallback.saveNightData(result.getYuvData(), ImageFormat.NV21, mCapturePhotoSize);
                }

                if (NightShotConfig.isDump()) {
                    String path = NightShotConfig.getDumpFilePath();
                    nightCaptureBean.dump(path, result);
                }
                nightCaptureBean.clear();
            }
        });
    }

}
