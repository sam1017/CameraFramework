package com.aiworks.android.deblur;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import androidx.annotation.NonNull;
import android.util.Log;
import android.util.Size;

import com.aiworks.android.Super.SuperUtil;
import com.aiworks.android.multiframe.MultiFrameCaptureCallback;
import com.aiworks.android.utils.ImageFormatUtil;
import com.aiworks.android.utils.ImagePlane;
import com.jni.lib.AiWorksDeblurJni;
import com.aiworks.yuvUtil.YuvEncodeJni;

import java.util.ArrayList;
import java.util.List;

public class ShakeCaptureResult implements ShakeCaptureResultInterface {

    private static final String TAG = "ShakeCaptureResult";

    private MultiFrameCaptureCallback mCallback;
    private AiWorksDeblurJni mDeblurJni;

    private Size mPhotoSize;
    private long mStart;
    private int mPhotoForamt;
    private int mCaptureNum;
    private int mCaptureFrames = 0;
    private int mBurstFrames = 1;

    private byte[] mYuvBytes1;
    private byte[] mYuvBytes2;
    private byte[] mYuvBytes3;
    //bv wuyonglin delete for bug2544 20201021 start
    //private byte[] outPutBytes;
    //bv wuyonglin delete for bug2544 20201021 end
    private List<String> mFileNameList = new ArrayList<>();
    private List<TotalCaptureResult> mCaptureResultList = new ArrayList<>();

    @Override
    public void init(Context context, Size photoSize, Size previewSize, Rect activeRect) {
        mPhotoSize = photoSize;
        synchronized (this) {
            //if (mDeblurJni == null) {
                mDeblurJni = new AiWorksDeblurJni(context);
                mDeblurJni.init();
                mDeblurJni.setParameter(mPhotoSize.getWidth(), mPhotoSize.getHeight());
                Log.i(TAG, "AiWorksDeblurJni init finish w = " + mPhotoSize.getWidth() + ", h = " + mPhotoSize.getHeight());
            //}
            //bv wuyonglin delete for bug2544 20201021 start
            //outPutBytes = new byte[mPhotoSize.getWidth() * mPhotoSize.getHeight() * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
            //bv wuyonglin delete for bug2544 20201021 end
        }
    }

    @Override
    public void destory() {
        synchronized (this) {
            if (mDeblurJni != null) {
                Log.i(TAG, "release");
                mDeblurJni.release();
                mDeblurJni = null;
            }
        }
    }

    @Override
    public void setCaptureCallback(MultiFrameCaptureCallback callback) {
        mCallback = callback;
    }

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {

            final Image image = reader.acquireNextImage();
            /*Image image = reader.acquireNextImage();
            final Rect crop = image.getCropRect();
            final int format = image.getFormat();
            final int width = image.getWidth();
            final int height = image.getHeight();
            Image.Plane[] planes = image.getPlanes();
            final ImagePlane[] imagePlanes = new ImagePlane[planes.length];
            for (int i = 0; i < planes.length; i++) {
                imagePlanes[i] = new ImagePlane(planes[i]);
            }*/
            final int num = mCaptureNum;
            mCaptureNum++;
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    byte[] captureYuv = YuvEncodeJni.getInstance().getBuffer(image,false);
                    image.close();
                    /*for (int i = 0; i < imagePlanes.length; i++) {
                        imagePlanes[i].release();
                    }*/
                    procImageEffect(captureYuv, num);
                }
            }.start();
            //image.close();
        }

    };

    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            Log.e(TAG, "onCaptureCompleted  111 shake");
            mCaptureFrames++;
            if (mCaptureFrames == 1) {
                mCallback.onCaptureStart(result);
            }
            mCaptureResultList.add(result);

        }
    };

    @Override
    public void onStartCapture(int frameNum, int format, int jpegRotation) {
        Log.e(TAG, "onStartCapture frameNum = " + frameNum + ", format = " + format+" jpegRotation ="+jpegRotation);
        mBurstFrames = frameNum;
        mPhotoForamt = format;
        mStart = System.currentTimeMillis();
        mCaptureNum = 0;
        mCaptureFrames = 0;
        mCaptureResultList.clear();
        mFileNameList.clear();
    }

    @Override
    public ImageReader.OnImageAvailableListener getImageAvailableListener() {
        return mOnImageAvailableListener;
    }

    @Override
    public CameraCaptureSession.CaptureCallback getCaptureCallback() {
        return mCaptureCallback;
    }

    private synchronized void procImageEffect(byte[] data, int num) {
        Log.i(TAG, "procImageEffect num = " + num);
        if (num == 0) {
            mYuvBytes1 = data;
        } else if (num == 1) {
            mYuvBytes2 = data;
        } else if (num == 2) {
            mYuvBytes3 = data;
        }
        String fileName = SuperUtil.DUMP_FILE_DEBLUR_PATH + ShakeUtil.mDumpFileTitle + "_" + num + ".jpg";
        mFileNameList.add(fileName);

        if (1 == mBurstFrames) {
            if (mCallback != null) {
                mCallback.onCaptureCompleted(null);
                mCallback.saveData(mYuvBytes1, ImageFormat.NV21, ShakeUtil.mDumpFileTitle);
            }
        } else if (mFileNameList.size() == mBurstFrames) {
            if (mCallback != null) {
                mCallback.onCaptureCompleted(null);
            }
            long cost1 = (System.currentTimeMillis() - mStart);

            byte[] result = null;
            int ret = -1;
            synchronized (this) {
                if (mDeblurJni != null) {
                    result = new byte[mPhotoSize.getWidth() * mPhotoSize.getHeight() * 3 / 2];
                    ret = mDeblurJni.deblur(mYuvBytes1, mYuvBytes2, mYuvBytes3, result);
                }
            }

            long cost2 = (System.currentTimeMillis() - mStart - cost1);
            String message = "used : " + cost1 + " : " + cost2+", mCallback = "+mCallback+", ret = "+ret;
            Log.e(TAG, message);
            if (mCallback != null && ret == 0) {
                mCallback.saveData(result, ImageFormat.NV21, ShakeUtil.mDumpFileTitle);
                //mCallback.compare(mYuvBytes2, result, ImageFormat.NV21);
            }
            if (SuperUtil.mIsDumpFile) {
                if (mYuvBytes1 != null) ImageFormatUtil.dumpYuvDate(mYuvBytes1, mFileNameList.get(0), mPhotoSize);
                if (mYuvBytes2 != null) ImageFormatUtil.dumpYuvDate(mYuvBytes2, mFileNameList.get(1), mPhotoSize);
                if (mYuvBytes3 != null) ImageFormatUtil.dumpYuvDate(mYuvBytes3, mFileNameList.get(2), mPhotoSize);
                ShakeUtil.dumpPreviewData();
                dumpCaptureResult();
            }
            mFileNameList.clear();

        }
    }

    private void dumpCaptureResult() {
        if (SuperUtil.mIsDumpFile) {
            String fileName = "_result.txt";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mCaptureResultList.size(); i++) {
                sb.append(i + "    expTimeUs :" + (int) (mCaptureResultList.get(i).get(CaptureResult.SENSOR_EXPOSURE_TIME) / 1000)).append("\r\n");
                sb.append(i + "    sensitivity :" + mCaptureResultList.get(i).get(CaptureResult.SENSOR_SENSITIVITY)).append("\r\n");
                sb.append(i + "    gain :" + mCaptureResultList.get(i).get(CaptureResult.CONTROL_POST_RAW_SENSITIVITY_BOOST)).append("\r\n");
                sb.append(i + "    focusDistance :" + mCaptureResultList.get(i).get(CaptureResult.LENS_FOCUS_DISTANCE)).append("\r\n");
            }
            ImageFormatUtil.dumpFile(SuperUtil.DUMP_FILE_DEBLUR_PATH + ShakeUtil.mDumpFileTitle + fileName, sb.toString().getBytes());
        }
        mCaptureResultList.clear();
    }

}
