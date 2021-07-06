package com.mediatek.camera.common.mode.video.glrenderer;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES32;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.aiworks.android.livehdr.JniInterface;
import com.mediatek.camera.common.mode.video.device.v2.LiveHDRConfig;
import com.mediatek.camera.common.mode.video.glrenderer.VideoTextureRenderDrawer;

public class LiveHDRDrawer implements VideoTextureRenderDrawer {

    
    private static final String TAG = LiveHDRDrawer.class.getSimpleName();

    private static final String MODULE_PROCESS_THREAD_NAME = "ModuleProcessThread";

    private static final int PROCESS_PIXEL = 1;

    private int mTextureId = -1;
    private VideoTextureRenderCallback mVideoRenderCallback;
    private CaptureRenderCallback mCaptureRenderCallback;

    private boolean mProcessSync;
    private boolean drawEffect = true;
    private boolean readPixelFlag = true;
    private float[] mTexTransformMatrix = new float[]{
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };

    private HandlerThread mModuleProcessThread;
    private ModuleProcessHandler mModuleProcessHandler;
    private static final Object mProcessLock = new Object();

    private static class ModuleProcessHandler extends Handler {

        private long time;
        private int fps;

        private ModuleProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case PROCESS_PIXEL:
                    long current = System.currentTimeMillis();
                    if (current - time > 1000) {
                        Log.i(TAG, "process pixel fps:" + fps);
                        time = current;
                        fps = 0;
                    } else {
                        fps++;
                    }
                    synchronized (mProcessLock) {
                        JniInterface.getInstance().processPixel(0);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public interface CaptureRenderCallback {
        void onCaptureRender(byte[] source, float[] coeffs_data, byte[] result, int outWidth, int outHeight);
    }

    public SurfaceTexture createSurfaceTexture() {
        Log.d(TAG, "createSurfaceTexture");
        mTextureId = genExternalOESTexture();
        SurfaceTexture surfaceTexture = new SurfaceTexture(mTextureId);
        Log.d(TAG, "createSurfaceTexture mTextureId = " + mTextureId);

        mModuleProcessThread = new HandlerThread(MODULE_PROCESS_THREAD_NAME);
        mModuleProcessThread.start();
        mModuleProcessHandler = new ModuleProcessHandler(mModuleProcessThread.getLooper());
        readPixelFlag = true;
        return surfaceTexture;
    }

    public void releaseSurfaceTexture() {
        Log.d(TAG, "releaseSurfaceTexture mTextureId = " + mTextureId);
        if (mModuleProcessHandler != null) {
            mModuleProcessHandler.removeCallbacksAndMessages(null);
            mModuleProcessHandler = null;
        }
        if (mModuleProcessThread != null) {
            mModuleProcessThread.getLooper().quitSafely();
            mModuleProcessThread = null;
        }
        synchronized (mProcessLock) {
            JniInterface.getInstance().destory();
        }
        GLES32.glDeleteTextures(1, new int[]{mTextureId}, 0);
    }

    public void init(String modelPath, boolean processSync, boolean debuggable) {
        mProcessSync = processSync;
        JniInterface.InitInfo initInfo = new JniInterface.InitInfo(JniInterface.MNN_FORWARD_CPU, 3);
        JniInterface.getInstance().init(modelPath, LiveHDRConfig.getModelType(), initInfo);
        JniInterface.getInstance().setDebuggable(debuggable);
        updateParameters();
    }

    private long time;
    private int fps;
    private int currentFps;

    public void onDrawFrame(float[] mTransform, int previewWidth, int previewHeight, int outWidth, int outHeight) {
        long current = System.currentTimeMillis();
        if (current - time > 1000) {
            Log.i(TAG, "drawFrame fps:" + fps + ", processSync = " + mProcessSync);
            time = current;
            currentFps = fps;
            fps = 0;
        } else {
            fps++;
        }
        if (mCaptureRenderCallback != null) {
            byte[] source = new byte[previewWidth * previewHeight * 4];
            float[] coeffs_data = new float[16 * 16 * 96];
            byte[] result = new byte[previewWidth * previewHeight * 4];
            JniInterface.getInstance().drawCaptureFrame(mTextureId, mTexTransformMatrix, previewWidth, previewHeight,
                    0, source, coeffs_data, result);
            mCaptureRenderCallback.onCaptureRender(source, coeffs_data, result, previewWidth, previewHeight);
            mCaptureRenderCallback = null;
        } else if (drawEffect) {
            if (mProcessSync) {
//                JniInterface.getInstance().drawFrameSync(mTextureId, mTransform, previewWidth, previewHeight, outWidth, outHeight, 0, 1.0f);
                JniInterface.getInstance().readPixelForModule(mTextureId, mTexTransformMatrix, previewWidth, previewHeight);
                JniInterface.getInstance().processPixel(0);
                JniInterface.getInstance().drawFrame(mTextureId, mTransform, previewWidth, previewHeight, outWidth, outHeight, 0);
            } else {
                if (readPixelFlag) {
                    JniInterface.getInstance().readPixelForModule(mTextureId, mTexTransformMatrix, previewWidth, previewHeight);
                    readPixelFlag = false;
                    mModuleProcessHandler.removeMessages(PROCESS_PIXEL);
                    mModuleProcessHandler.sendEmptyMessage(PROCESS_PIXEL);
                }
                int ret = JniInterface.getInstance().drawFrame(mTextureId, mTransform, previewWidth, previewHeight, outWidth, outHeight, 0);
                if (ret == 0) {
                    readPixelFlag = true;
                }
            }
        } else {
            JniInterface.getInstance().drawNormalFrame(mTextureId, mTransform, previewWidth, previewHeight, outWidth, outHeight, 0);
        }
        if (mVideoRenderCallback != null) {
            mVideoRenderCallback.renderVideoFrame(mTextureId, mTransform, 0, this);
        }
    }

    @Override
    public void drawVideoFrame(int textureId, int bgTextureId, float[] texTransformMatrix, int outWidth, int outHeight, int fboId) {
        if (drawEffect) {
            JniInterface.getInstance().drawVideoFrame(-1, texTransformMatrix, outWidth, outHeight, fboId);
        } else {
            JniInterface.getInstance().drawVideoFrame(mTextureId, texTransformMatrix, outWidth, outHeight, fboId);
        }
    }

    public Rect getHighLightContours(int previewWidth, int previewHeight) {
//        if (mPixel != null) {
//            if (mCurrentPixel == null) {
//                mCurrentPixel = new byte[MODULE_INPUT_SIZE * MODULE_INPUT_SIZE * 4];
//            }
//            System.arraycopy(mPixel, 0, mCurrentPixel, 0, mPixel.length);
//            int[] rect = new int[4];
//            int highlightArea = JniInterface.getInstance().getHighLightContours(
//                    mCurrentPixel, MODULE_INPUT_SIZE, MODULE_INPUT_SIZE, ImageFormat.FLEX_RGBA_8888, 250, rect);
//            if (highlightArea > 0) {
//                return new Rect(Math.max(0, rect[0] * previewWidth / MODULE_INPUT_SIZE),
//                        Math.max(0, rect[1] * previewHeight / MODULE_INPUT_SIZE),
//                        Math.min(previewWidth, rect[2] * previewWidth / MODULE_INPUT_SIZE),
//                        Math.min(previewHeight, rect[3] * previewHeight / MODULE_INPUT_SIZE));
//            }
//        }
        return null;
    }

    public void setVideoRecordCallback(VideoTextureRenderCallback callback) {
        mVideoRenderCallback = callback;
    }

    public void setDrawEffect(boolean bool) {
        drawEffect = bool;
    }

    public void setCaptureCallback(CaptureRenderCallback callback) {
        mCaptureRenderCallback = callback;
    }

    private int genExternalOESTexture() {
        int[] texture = new int[1];
        GLES32.glGenTextures(1, texture, 0);
        GLES32.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES32.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);
        GLES32.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR);
        GLES32.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE);
        GLES32.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    public int getFps() {
        return currentFps;
    }

    public void updateParameters() {
        JniInterface.getInstance().setParameters(LiveHDRConfig.getBrightness(),
                LiveHDRConfig.getSaturation(), LiveHDRConfig.getContrast(), false);
        JniInterface.getInstance().setDehazeParameters(LiveHDRConfig.getDehazeType(), LiveHDRConfig.getDehazeLevel(),
                LiveHDRConfig.getDehazeScene(), LiveHDRConfig.getDehazeWsat(), LiveHDRConfig.getDehazeBsat());
    }

}
