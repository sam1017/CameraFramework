package com.aiworks.android.Super;

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

import com.aiworks.android.multiframe.MultiFrameCaptureCallback;
import com.aiworks.android.utils.ImageFormatUtil;
import com.aiworks.android.utils.ImagePlane;
import com.aiworks.yuvUtil.YuvEncodeJni;
import com.jni.lib.AiWorksMfsrJni;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MTKSuperCaptureResult implements SuperCaptureResultInterface {

    private static final String TAG = "MTKSuperCaptureResult";

    private MultiFrameCaptureCallback mCallback;

    private Size mPhotoSize;
    private long mSuperStart;
    private int mPhotoForamt;
    private int mCaptureNum;
    private int mCaptureFrames = 0;
    private int mBurstFrames = 1;

    private byte[] mYuvBytes1;
    private byte[] mYuvBytes2;
    private byte[] mYuvBytes3;
    private byte[] mJpegBytes1;
    private byte[] mJpegBytes2;
    private byte[] mJpegBytes3;
    private boolean mInited = false;
    private List<String> mFileNameList = new ArrayList<>();
    private List<TotalCaptureResult> mCaptureResultList = new ArrayList<>();
    private AiWorksMfsrJni mAiWorksMfsrJni = null;

    private byte[] loadBinData(Context context, String path) {
        InputStream inputStream = null;
        String sFile = "aw_mfsr.bin";
        try {
            File file = new File(path, sFile);
            if (file.exists() && file.length() > 0) {
                inputStream = new FileInputStream(file);
            } else {
                inputStream = context.getAssets().open(sFile);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[inputStream.available()];
            int n;
            while ((n = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void init(Context context, Size photoSize, Size previewSize, Rect activeRect) {
        mPhotoSize = photoSize;
        synchronized (this) {
            Log.d(TAG, "SuperResolutionJni.Init w = "+mPhotoSize.getWidth()+", h = "+mPhotoSize.getHeight());
            //if (!mInited) {
                if (mAiWorksMfsrJni == null) {
                    mAiWorksMfsrJni = new AiWorksMfsrJni(context);
                }
                int ret = mAiWorksMfsrJni.init("/sdcard/etc");
                Log.d(TAG, "mAiWorksMfsrJni.init ret = "+ret);
                ret |= mAiWorksMfsrJni.setParameter(mPhotoSize.getWidth(), mPhotoSize.getHeight(),
                        SuperUtil.mMf_ce, SuperUtil.mMf_sp, SuperUtil.mFaceLevel, SuperUtil.mMf_fast);
                Log.d(TAG, "mAiWorksMfsrJni.setParameter ret = " + ret + ", ce = " + SuperUtil.mMf_ce + ", sp = " + SuperUtil.mMf_sp + ", fast = " + SuperUtil.mMf_fast);
                if (ret == 0) {
                    mInited = true;
                } else {
                    mInited = false;
                }
            //}
        }
    }

    @Override
    public void destory() {
        synchronized (this) {
            if (mInited) {
                Log.i(TAG, "SuperResolutionJni.UnInit");
		if (mAiWorksMfsrJni != null) {
                mAiWorksMfsrJni.release();
                mAiWorksMfsrJni = null;
                mInited = false;
		}
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
            final int num = mCaptureNum;
            mCaptureNum++;
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    byte[] captureYuv;// = ImageFormatUtil.getDataFromImage(imagePlanes, crop, format, ImageFormatUtil.COLOR_FormatNV21);
                    if (image.getFormat() == ImageFormat.JPEG) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        image.close();
                        captureYuv = YuvEncodeJni.getInstance().Jpeg2Nv21(data, mPhotoSize.getWidth(), mPhotoSize.getHeight(), 1, 0, false);
                        procImageEffect(data, captureYuv, num);
                    } else {
                        captureYuv = YuvEncodeJni.getInstance().getBuffer(image,false);
                        image.close();
                        procImageEffect(null, captureYuv, num);
                    }
                    /*for (int i = 0; i < imagePlanes.length; i++) {
                        imagePlanes[i].release();
                    }*/

//                    procImageEffect(captureYuv, num);
                }
            }.start();
            //image.close();
        }

    };

    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            Log.e(TAG, "onCaptureCompleted 222 super");
            mCaptureFrames++;
            if (mCaptureFrames == 1) {
                mCallback.onCaptureStart(result);
            }
            mCaptureResultList.add(result);

        }
    };

    @Override
    public void onStartCapture(int frameNum, int format) {
        Log.e(TAG, "onStartCapture frameNum = " + frameNum + ", format = " + format);
        mBurstFrames = frameNum;
        mPhotoForamt = format;
        mSuperStart = System.currentTimeMillis();
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

    private synchronized void procImageEffect(byte[] jpeg, byte[] data, int num) {
        Log.i(TAG, "procImageEffect num = " + num);
        if (num == 0) {
            mYuvBytes1 = data;
            mJpegBytes1 = jpeg;
        } else if (num == 1) {
            mYuvBytes2 = data;
            mJpegBytes2 = jpeg;
        } else if (num == 2) {
            mYuvBytes3 = data;
            mJpegBytes3 = jpeg;
        }
        String fileName = SuperUtil.DUMP_FILE_PATH + SuperUtil.mDumpFileTitle + "_multiframe_" + num + ".jpg";
        mFileNameList.add(fileName);

        if (SuperUtil.USE_PLATFORM_MFNR) {
            mCallback.onCaptureCompleted(null);
            long cost1 = (System.currentTimeMillis() - mSuperStart);
            byte[] result = new byte[mPhotoSize.getWidth() * mPhotoSize.getHeight() * 3 / 2];
            byte[] faceMask = null;
            if (SuperUtil.hasFace) {
                faceMask = new byte[256*256];
            }
            synchronized (this) {
		if (mAiWorksMfsrJni != null) {
                int ret = mAiWorksMfsrJni.process(mYuvBytes1, faceMask, result);
                Log.i(TAG, "mAiWorksMfsrJni.process single ret = " + ret);
		}
            }
            long cost2 = (System.currentTimeMillis() - mSuperStart - cost1);
            String message = "super used : " + cost1 + " : " + cost2;
            Log.e(TAG, message);
            mCallback.showToast(message);
            mCallback.saveData(result, ImageFormat.NV21, SuperUtil.mDumpFileTitle);

            //mCallback.compare(mYuvBytes1, result, ImageFormat.NV21);

            if (SuperUtil.mIsDumpFile) {
                if (mPhotoForamt == ImageFormat.JPEG) {
                    ImageFormatUtil.dumpFile(mFileNameList.get(0), mJpegBytes1);
                } else {
                    ImageFormatUtil.dumpYuvDate(mYuvBytes1, mFileNameList.get(0), mPhotoSize);
                }
                mCallback.showToast("Super mYuvBytes saved");
            }
            mFileNameList.clear();
            dumpCaptureResult();
        } else {
            if (mFileNameList.size() == mBurstFrames) {
                mCallback.onCaptureCompleted(null);
                long cost1 = (System.currentTimeMillis() - mSuperStart);
                byte[] result = new byte[mPhotoSize.getWidth() * mPhotoSize.getHeight() * 3 / 2];
                byte[] faceMask = null;
                if (SuperUtil.hasFace) {
                    faceMask = new byte[256*256];
                }
                synchronized (this) {
                    if (mAiWorksMfsrJni != null) {
                    int ret = mAiWorksMfsrJni.process(mYuvBytes1, mYuvBytes2, mYuvBytes3, faceMask, result);
                    Log.i(TAG, "mAiWorksMfsrJni.process ret = " + ret);
                    }
                }
                long cost2 = (System.currentTimeMillis() - mSuperStart - cost1);
                String message = "super used : " + cost1 + " : " + cost2;
                Log.e(TAG, message);
                mCallback.showToast(message);
                mCallback.saveData(result, ImageFormat.NV21, SuperUtil.mDumpFileTitle);

                //mCallback.compare(mYuvBytes1, result, ImageFormat.NV21);

                if (SuperUtil.mIsDumpFile) {
                    if (mPhotoForamt == ImageFormat.JPEG) {
                        ImageFormatUtil.dumpFile(mFileNameList.get(0), mJpegBytes1);
                        ImageFormatUtil.dumpFile(mFileNameList.get(1), mJpegBytes2);
                        ImageFormatUtil.dumpFile(mFileNameList.get(2), mJpegBytes3);
                    } else {
                        ImageFormatUtil.dumpYuvDate(mYuvBytes1, mFileNameList.get(0), mPhotoSize);
                        ImageFormatUtil.dumpYuvDate(mYuvBytes2, mFileNameList.get(1), mPhotoSize);
                        ImageFormatUtil.dumpYuvDate(mYuvBytes3, mFileNameList.get(2), mPhotoSize);
                    }
                    mCallback.showToast("Super mYuvBytes saved");
                }
                mFileNameList.clear();
                dumpCaptureResult();
            }
        }
    }

    private void dumpCaptureResult() {
        if (SuperUtil.mIsDumpFile) {
            String fileName ="_result.txt";
            StringBuilder sb = new StringBuilder();
            for (int i =0 ; i < mCaptureResultList.size(); i++) {
                sb.append(i + "    expTimeUs :" + mCaptureResultList.get(i).get(CaptureResult.SENSOR_EXPOSURE_TIME)).append("\r\n");
                sb.append(i + "    sensitivity :" + mCaptureResultList.get(i).get(CaptureResult.SENSOR_SENSITIVITY)).append("\r\n");
            }
            ImageFormatUtil.dumpFile(SuperUtil.DUMP_FILE_PATH + SuperUtil.mDumpFileTitle + fileName, sb.toString().getBytes());
        }
        mCaptureResultList.clear();
    }

}
