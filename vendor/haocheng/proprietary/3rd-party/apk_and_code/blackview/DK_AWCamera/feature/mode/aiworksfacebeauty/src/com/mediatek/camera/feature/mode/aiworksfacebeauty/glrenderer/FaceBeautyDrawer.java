package com.mediatek.camera.feature.mode.aiworksfacebeauty.glrenderer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

//import com.aiworks.FaceBeautyActivity;
//import com.aiworks.facesdk.AwFaceDetectApi;
//import com.aiworks.facesdk.FaceInfo;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.util.OpenGlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.mediatek.camera.feature.mode.aiworksfacebeauty.glrenderer.GPUImageFilter.NO_FILTER_VERTEX_SHADER;
import static com.mediatek.camera.feature.mode.aiworksfacebeauty.glrenderer.GPUImageFilter.NO_OES_FILTER_FRAGMENT_SHADER;
import com.aiworks.awfacebeauty.AwBeautyFrame;
import com.aiworks.awfacebeauty.AwFaceInfo;

public class FaceBeautyDrawer {
    private static final String TAG = "FaceBeautyDrawer.SurfaceTextureRenderer";

    private final float[] mSTMatrix = {
            0.0f,-1.0f,0.0f,0.0f,
            1.0f,0.0f,0.0f,0.0f,
            0.0f,0.0f,1.0f,0.0f,
            0.0f,1.0f,0.0f,1.0f
    };

    private final float[] STM = {
            1.0f,0.0f,0.0f,0.0f,
            0.0f,1.0f,0.0f,0.0f,
            0.0f,0.0f,1.0f,0.0f,
            0.0f,0.0f,0.0f,1.0f
    };

    private int mPreviewWidth, mPreviewHeight;
    private int mSurfaceViewWidth, mSurfaceViewHeight;

    private boolean mIsNeedMakeup = true;
    private int mTextureId = -1;

    private SurfaceTexture mSurfaceTexture;

    private Context mContext;
    private AwBeautyFrame mAwBeautyFrame;
    private Handler handler;
    private Handler mFaceDecteHandler;

    private int[] mFrameBuffers = new int[1];
    private int[] mFrameTextures = new int[1];

    private boolean isOes = true;

    public FaceBeautyDrawer(Context context) {
        mTextureId = -1;
        mContext = context;
        mPreviewWidth = mPreviewHeight = -1;

        HandlerThread handlerThread = new HandlerThread( "face-detect-thread") ;
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        mFaceDecteHandler = new Handler(looper);
    }

    /**
     * Notifies the renderer thread that the activity is pausing.
     * <p>
     * For best results, call this *after* disabling Camera preview.
     */
    public void releaseSurfaceTexture() {
	Log.d(TAG, "releaseSurfaceTexture");
        /*if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }*/
        if (mAwBeautyFrame != null) {
            mAwBeautyFrame.release();     // assume the GLSurfaceView EGL context is about
            mAwBeautyFrame = null;             //  to be destroyed
        }

        if (mTextureId != -1) {
            GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
            mTextureId = -1;
        }

        GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);
        GLES20.glDeleteTextures(1, mFrameTextures, 0);
        for (int i = 0; i < mFrameTextures.length;i ++) {
            mFrameTextures[i] = 0;
            mFrameBuffers[i] =0;
        }

        mFaceDecteHandler.removeCallbacksAndMessages(null);
        mPreviewWidth = mPreviewHeight = -1;
    }

    public void onDestory() {
        mFaceDecteHandler.getLooper().quitSafely();
    }

    private boolean mIsPreviewSizeChanged = true;
    private boolean mIsSetCameraPreviewSize = false;
    public void setCameraPreviewSize(int width, int height) {
        Log.d(TAG, "setCameraPreviewSize width:"+width+",height:"+height);
        mIsPreviewSizeChanged = true;
        mIsSetCameraPreviewSize = true;
        mPreviewWidth = width;
        mPreviewHeight = height;
        //bv wuyonglin add for bug3529 20210126 start
        mSurfaceViewWidth = height;
        mSurfaceViewHeight = width;
        //bv wuyonglin add for bug3529 20210126 end
    }

    public SurfaceTexture createSurfaceTexture() {
        Log.d(TAG, "createSurfaceTexture");
        mAwBeautyFrame = new AwBeautyFrame();
        mTextureId = OpenGlUtil.genExternalOESTexture();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        Log.d(TAG, "createSurfaceTexture mTextureId ="+mTextureId);
        mGPUImageFilter = new GPUImageFilter(NO_FILTER_VERTEX_SHADER, NO_OES_FILTER_FRAGMENT_SHADER);
        mGPUImageFilter.init();
	return mSurfaceTexture;

    }

    /**
     * 适配Nubia红魔手机callback无数据问题,建议使用previewcallback数据进行异步检测
     * 人脸检测相关逻辑
     */
    private ByteBuffer SavedDataBuffer;
    private int faceDetectWidth = 242;
    private int faceDetectHeight = 0;
    private int[] faceDetectTexture = new int[1];
    private int[] faceDetectFrameBuffer = new int[1];
    private GPUImageFilter mGPUImageFilter;
    private volatile boolean isFaceDetecting = false;

    /*private void detectFaceInfoAsync() {
        if (isFaceDetecting) {
            return;
        }
        isFaceDetecting = true;

        if (mIsPreviewSizeChanged) {
            mIsPreviewSizeChanged = false;
            faceDetectHeight = (int) (faceDetectWidth * (mPreviewHeight * 1.0f / mPreviewWidth));

            if (faceDetectFrameBuffer[0] > 0) {
                GLES20.glDeleteFramebuffers(1, faceDetectFrameBuffer, 0);
                GLES20.glDeleteTextures(1, faceDetectTexture, 0);
                for (int i = 0; i < faceDetectTexture.length;i ++) {
                    faceDetectTexture[i] = -1;
                    faceDetectFrameBuffer[i] =-1;
                }
            }
            OpenGlUtil.createFBOTextureBuffer(faceDetectFrameBuffer, faceDetectTexture, faceDetectWidth, faceDetectHeight);

            SavedDataBuffer = ByteBuffer.allocateDirect(
                    4 * faceDetectWidth * faceDetectHeight).order(ByteOrder.nativeOrder());
        }

        Log.d(TAG,"faceDetectFrameBuffer:"+faceDetectFrameBuffer[0]);
        GLES20.glViewport(0, 0, faceDetectWidth, faceDetectHeight);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, faceDetectFrameBuffer[0]);
        mGPUImageFilter.onDraw(mTextureId);
        SavedDataBuffer.position(0);
        GLES20.glReadPixels(0, 0, faceDetectWidth, faceDetectHeight, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, SavedDataBuffer);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

//        Bitmap bmp = Bitmap.createBitmap(faceDetectWidth, faceDetectHeight, Bitmap.Config.ARGB_8888);
//        bmp.copyPixelsFromBuffer(SavedDataBuffer);
//        ImageSaveUtil.compressToFile((Activity) mContext, bmp);

        mFaceDecteHandler.post(new Runnable(){
            @Override
            public void run() {
                FaceInfo faceInfos[] = AwFaceDetectApi.detectPicData(
                        SavedDataBuffer.array(),
                        faceDetectWidth, faceDetectHeight,
                        AwFaceDetectApi.PIX_FMT_RGBA8888,
                        //((FaceBeautyActivity)mContext).getFaceDetectOrientation(),
			0,
                        AwFaceDetectApi.RESIZE_320,
                        false);
                //Log.d(TAG,"faceInfos:"+faceInfos.length);

                if (faceInfos == null || faceInfos.length == 0) {
                    Log.e(TAG,"faceInfos = null");
                    mHasFace = false;
                    if (mAwBeautyFrame != null) {
                        mAwBeautyFrame.setFaceInfo(null);
                    }
                    isFaceDetecting =false;
                    return;
                }

                mHasFace = true;
                AwFaceInfo[] awFaceInfos = new AwFaceInfo[faceInfos.length];
                for (int i = 0; i < faceInfos.length; i++) {
                    AwFaceInfo mFrameFaceInfo = new AwFaceInfo();
                    mFrameFaceInfo.gender = 1;
                    mFrameFaceInfo.imgWidth = faceDetectWidth;
                    mFrameFaceInfo.imgHeight = faceDetectHeight;
                    mFrameFaceInfo.pointItemCount = faceInfos[i].points.length;
                    mFrameFaceInfo.facePoints = new float[faceInfos[i].points.length];
                    FacePointBeautyTransUtil.dupFacePointsFrame(faceInfos[i].points, mFrameFaceInfo.facePoints,  //转换人脸坐标
                            mFrameFaceInfo.pointItemCount,
                            mFrameFaceInfo.imgWidth, mFrameFaceInfo.imgHeight,
                            0,false
                            ((FaceBeautyActivity)mContext).getFaceDetectOrientation(), ((FaceBeautyActivity)mContext).isFrontCamera());
                    awFaceInfos[i] = mFrameFaceInfo;
                }

                mAwFaceInfos = awFaceInfos;
                if (mAwBeautyFrame != null) {
                    mAwBeautyFrame.setFaceInfo(awFaceInfos);
                }
                isFaceDetecting =false;
            }
        });

    }*/

    public void onDrawFrame(float[] mTransform, int previewWidth, int previewHeight, int outWidth, int outHeight) {
        /*Log.d(TAG, "onDrawFrame NEW1 mTextureId = "+mTextureId+"  mPreviewWidth = "+mPreviewWidth+ "  mPreviewHeight = " +mPreviewHeight
                +"  mSurfaceViewWidth = "+mSurfaceViewWidth+ "  mSurfaceViewHeight = "+mSurfaceViewHeight+"; mHasFace = "+mHasFace+" outWidth ="+outWidth+" outHeight ="+outHeight+" mIsSetCameraPreviewSize ="+mIsSetCameraPreviewSize);*/
        //bv wuyonglin delete for bug3529 20210126 start
        //mSurfaceViewWidth = previewHeight;
        //mSurfaceViewHeight = previewWidth;
        //bv wuyonglin delete for bug3529 20210126 end
        long startTime = System.currentTimeMillis();
        if (mSurfaceTexture == null || mAwBeautyFrame == null) {
            return;
        }

        try {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mSTMatrix);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        //if (mIsSetCameraPreviewSize) {
        //    mIsSetCameraPreviewSize = false;
        //    return;
        //}
        if (mPreviewWidth <= 0 || mPreviewHeight <= 0) {
            return;
        }
        /**
         * 适配Nubia红魔手机callback无数据问题,建议使用previewcallback数据进行异步检测
         * 人脸检测相关逻辑
         */
        //detectFaceInfoAsync();

        mAwBeautyFrame.setUseOES(isOes);
        //Log.e(TAG, "isOes :" + isOes);

        mHasFace = mAwFaceInfos != null && mAwFaceInfos.length > 0;
        if (mAwBeautyFrame != null && mHasFace) {
            mAwBeautyFrame.setFaceInfo(mAwFaceInfos);
        } else {
            mAwBeautyFrame.setFaceInfo(null);
        }

        if (isOes) {
        //Log.e(TAG, "mIsNeedMakeup3 :" + mIsNeedMakeup+" mHasFace ="+mHasFace);
            if (mIsNeedMakeup && mHasFace) {
                mAwBeautyFrame.drawFaceBeautyFrame(mTextureId, mSTMatrix, mPreviewWidth, mPreviewHeight, mSurfaceViewWidth, mSurfaceViewHeight,  0, 1.0f);
            } else {
                mAwBeautyFrame.drawSoftBeautyFrame(mTextureId, mSTMatrix, mPreviewWidth, mPreviewHeight, mSurfaceViewWidth, mSurfaceViewHeight,  0, 1.0f);
            }

        } else {
            if ((mPreviewWidth > 0 && mPreviewHeight > 0 && mFrameBuffers[0] <= 0) || mIsPreviewSizeChanged) {
                mIsPreviewSizeChanged = false;
                GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);
                GLES20.glDeleteTextures(1, mFrameTextures, 0);
                for (int i = 0; i < mFrameTextures.length;i ++) {
                    mFrameTextures[i] = 0;
                    mFrameBuffers[i] =0;
                }
                OpenGlUtil.createFBOTextureBuffer(mFrameBuffers, mFrameTextures, mPreviewWidth, mPreviewHeight);
            }

            GLES20.glViewport(0, 0, mPreviewWidth, mPreviewHeight);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
            mGPUImageFilter.onDraw(mTextureId);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            Log.e(TAG, "mIsNeedMakeup :" + mIsNeedMakeup+" mHasFace ="+mHasFace);
            if (mIsNeedMakeup && mHasFace) {
                mAwBeautyFrame.drawFaceBeautyFrame(mFrameTextures[0], mSTMatrix, mPreviewWidth, mPreviewHeight, mSurfaceViewWidth, mSurfaceViewHeight,  0, 1.0f);
            } else {
                mAwBeautyFrame.drawSoftBeautyFrame(mFrameTextures[0], mSTMatrix, mPreviewWidth, mPreviewHeight, mSurfaceViewWidth, mSurfaceViewHeight,  0, 1.0f);
            }

        }


/*
        ByteBuffer SavedDataBuffer = ByteBuffer.allocateDirect(
                4 * mSurfaceViewWidth * mSurfaceViewHeight).order(ByteOrder.nativeOrder());
        SavedDataBuffer.position(0);
        GLES20.glReadPixels(0, 0, mSurfaceViewWidth, mSurfaceViewHeight, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, SavedDataBuffer);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        Bitmap bmp = Bitmap.createBitmap(mSurfaceViewWidth, mSurfaceViewHeight, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(SavedDataBuffer);
        ImageSaveUtil.compressToFile((Activity) mContext, bmp);
*/


        long drawTime = System.currentTimeMillis() - startTime;
        fps(drawTime);
    }

    private int fps = 0;
    private long totalDrawTime = 0;
    private long lastTime = System.currentTimeMillis(); // ms
    private int frameCount = 0;
    private int fps(long drawTime)
    {
        //Log.i(TAG,"drawTime = "+drawTime);
        ++frameCount;
        totalDrawTime += drawTime;
        long curTime = System.currentTimeMillis();
        if (curTime - lastTime > 1000) // 取固定时间间隔为1秒
        {
            fps = frameCount;
            final double avgfps = fps / ((curTime - lastTime) / 1000.0);
            final double avgDrawTime = totalDrawTime / fps;

            if (mFPSCallBack != null) {
                mFPSCallBack.onTrackdetected(avgfps,avgDrawTime);
            }

            frameCount = 0;
            lastTime = curTime;
            totalDrawTime = 0;
        }
        return fps;
    }


    private boolean mHasFace = false;
    private AwFaceInfo[] mAwFaceInfos = null;
    public void updateFaceInfo(AwFaceInfo[] info) {
        mAwFaceInfos = info;
    }

    public void setBeautyLevel(int index, float level) {
        Log.e(TAG, "setBeautyLevel index :" + index+" level ="+level+" mAwBeautyFrame ="+mAwBeautyFrame);
        if (mAwBeautyFrame != null) {
            mAwBeautyFrame.setBeautyLevel(index,level);
        }
    }

    public int getWidth() {
        return mSurfaceViewWidth;
    }

    public int getHeight() {
        return mSurfaceViewHeight;
    }

    //bv wuyonglin add for bug3529 20210126 start
    public void updateSurfaceViewSize(int surfaceViewWidth, int surfaceViewHeight) {
        mSurfaceViewWidth = surfaceViewWidth;
        mSurfaceViewHeight = surfaceViewHeight;
    }
    //bv wuyonglin add for bug3529 20210126 end

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public void setIsNeedMakeup(boolean isNeedMakeup) {
        mIsNeedMakeup = isNeedMakeup;
    }

    public void setOes(boolean oes) {
        isOes = oes;
    }

    private FPSCallBack mFPSCallBack;
    public void setFPSCallBack(FPSCallBack callBack) {
        mFPSCallBack = callBack;
    }

    public interface FPSCallBack {
        void onTrackdetected(double avgfps, double avgDrawTime);
    }

}
