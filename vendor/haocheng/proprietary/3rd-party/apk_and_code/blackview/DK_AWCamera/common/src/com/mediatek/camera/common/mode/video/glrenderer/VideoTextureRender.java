package com.mediatek.camera.common.mode.video.glrenderer;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

public class VideoTextureRender {
    private static final String TAG = "VideoTextureRender";

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private final VideoTextureRenderInterface renderInterface;

    private EGLDisplay mEglDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext mEglContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface mEglSurface = EGL14.EGL_NO_SURFACE;
    private EGLConfig[] mConfigs = new EGLConfig[1];
    private int mWidth;
    private int mHeight;

    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    public static final int INIT_EGL = 0x10;
    public static final int DRAW_FRAME = 0x11;
    public static final int MSG_QUIT = 0x12;

    private EGLContext mShareGLContext;
    private Object mRenderLock = new Object();
    private boolean stopRender;
    private boolean bInited;
    private int mCameraID;
    private SurfaceRender mRender;
    private boolean firstFrame;

    public VideoTextureRender(VideoTextureRenderInterface i) {
        this.renderInterface = i;
        Log.i(TAG,"VideoTextureRender ");
        stopRender = true;
        mRender = new SurfaceRender();
        Log.i(TAG,"VideoTextureRender mRender = " + mRender);
    }

    private void init(Surface surface) {
        Message msg = mHandler.obtainMessage();
        msg.what = INIT_EGL;
        msg.obj = surface;
        mHandler.sendMessage(msg);
    }

    private ConditionVariable mCV;

    public void setConditionVariable(ConditionVariable cv) {
        mCV = cv;
    }

    public void setOrientation(int orientation) {
        mRender.setOrientation(orientation);
    }

    public void startThread(int cameraID) {
        mCameraID = cameraID;
        mHandlerThread = new HandlerThread("VST");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                Log.i(TAG,"handleMessage msg.what = " + msg.what);
                super.handleMessage(msg);
                switch (msg.what) {
                    case INIT_EGL:
                        try {
                            removeMessages(INIT_EGL);
                            setupEGL(mShareGLContext, (Surface) msg.obj);
                            mRender.onSurfaceCreate();
                            bInited = true;
                        } catch (Exception ex) {
                            Log.e(TAG, "INIT_EGL Exception");
                            bInited = false;
                        }
                        break;

                    case DRAW_FRAME:
                        synchronized (mRenderLock) {
                            if (stopRender || !bInited || msg.obj == null
                                    || mEglDisplay == EGL14.EGL_NO_DISPLAY || mRender == null) {
                                if (mCV != null)
                                    mCV.open();
                                break;
                            }
                        }
                        removeMessages(DRAW_FRAME);
                        VideoRenderImpl impl = (VideoRenderImpl) msg.obj;
                        if (firstFrame) {
                            firstFrame = false;
                            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                        }
                        impl.drawVideoFrame();

                        if (mCV != null)
                            mCV.open();

                        swapBuffers();
                        break;
                    case MSG_QUIT:
                        if (mRender != null)
                            mRender.release();
                        if (mCV != null)
                            mCV.open();

                        release();
                        Looper looper = Looper.myLooper();
                        if (looper != null) {
                            looper.quit();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public Handler getDrawHandler() {
        return mHandler;
    }

    public void setVideoSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public void stop() {
        synchronized (mRenderLock) {
            stopRender = true;
        }
        renderInterface.setVideoRecordCallback(null);
        //bv wuyonglin add for monkey test fc 20210202 start
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_QUIT));
        }
        //bv wuyonglin add for monkey test fc 20210202 end
    }

    public boolean isStopped() {
        return bInited && stopRender;
    }

    public void start(final Surface surface) {
        synchronized (mRenderLock) {
            stopRender = false;
            firstFrame = true;
        }
        renderInterface.queueEvent(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                mShareGLContext = EGL14.eglGetCurrentContext();//mGLRootView.getEGLContext();
                init(surface);
            }
        });
    }

    public void start(final Surface surface, VideoTextureRenderCallback callback) {
        synchronized (mRenderLock) {
            stopRender = false;
            firstFrame = true;
        }
        renderInterface.setVideoRecordCallback(callback);
        renderInterface.queueEvent(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                mShareGLContext = EGL14.eglGetCurrentContext();//mGLRootView.getEGLContext();
                init(surface);
            }
        });
    }

    public void swapBuffers() {
        if (mEglDisplay != null && mEglSurface != null) {
            EGL14.eglSwapBuffers(mEglDisplay, mEglSurface);
        }
    }

    public void setupEGL(EGLContext shareContext, Surface surface) {
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            eglSetup(shareContext, surface);
            makeCurrent();
        }
    }

    private void eglSetup(EGLContext shareContext, Surface surface) {
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)) {
            mEglDisplay = null;
            throw new RuntimeException("unable to initialize EGL14");
        }

        // Configure EGL for recordable and OpenGL ES 2.0.  We want enough RGB bits
        // to minimize artifacts from possible YUV conversion.
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 0,
                EGL14.EGL_DEPTH_SIZE, 0,
                EGL14.EGL_STENCIL_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_NONE
        };
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(mEglDisplay, attribList, 0, mConfigs, 0, mConfigs.length,
                numConfigs, 0)) {
            throw new RuntimeException("unable to find RGB888+recordable ES2 EGL config");
        }

        // Configure context for OpenGL ES 2.0.
        int[] attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        mEglContext = EGL14.eglCreateContext(mEglDisplay, mConfigs[0], shareContext,
                attrib_list, 0);
        checkEglError("eglCreateContext");
        if (mEglContext == null) {
            throw new RuntimeException("null context");
        }

        // Create a window surface, and attach it to the Surface we received.
        createEGLSurface(surface);

    }

    public void makeUnCurrent() {
        if (!EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    /**
     * Makes our EGL context and surface current.
     */
    public void makeCurrent() {
        if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    /**
     * Queries the surface's width.
     */
    public int getWidth() {
        int[] value = new int[1];
        EGL14.eglQuerySurface(mEglDisplay, mEglSurface, EGL14.EGL_WIDTH, value, 0);
        return value[0];
    }

    /**
     * Queries the surface's height.
     */
    public int getHeight() {
        int[] value = new int[1];
        EGL14.eglQuerySurface(mEglDisplay, mEglSurface, EGL14.EGL_HEIGHT, value, 0);
        return value[0];
    }

    private void createEGLSurface(Surface surface) {
        //EGLConfig[] configs = new EGLConfig[1];
        int[] surfaceAttribs = {
                EGL14.EGL_NONE
        };
        mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mConfigs[0], surface,
                surfaceAttribs, 0);

        checkEglError("eglCreateWindowSurface");
        if (mEglSurface == null) {
            throw new RuntimeException("surface was null");
        }
    }

    private void releaseEGLSurface() {
        if (mEglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
            mEglSurface = EGL14.EGL_NO_SURFACE;
        }
    }

    public void release() {
        if (mEglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
            EGL14.eglDestroyContext(mEglDisplay, mEglContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(mEglDisplay);
        }
        mShareGLContext = null;
        //mVideoSurface.release();

        mEglDisplay = EGL14.EGL_NO_DISPLAY;
        mEglContext = EGL14.EGL_NO_CONTEXT;
        mEglSurface = EGL14.EGL_NO_SURFACE;

        //mVideoSurface = null;
    }

    /**
     * Checks for EGL errors.
     */
    private void checkEglError(String msg) {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            Log.d(TAG, msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }

    public static class VideoRenderImpl {
        VideoTextureRenderDrawer drawer;

        int textureId;
        int bgTextureId = -1;
        float[] texTransformMatrix;
        int outWidth, outHeight;
        int fboId;

        public VideoRenderImpl(VideoTextureRenderDrawer drawer) {
            this.drawer = drawer;
        }

        public void setTextureParams(int textureid, float[] textureMatrix, int fbo) {
            textureId = textureid;
            texTransformMatrix = textureMatrix;
            fboId = fbo;
        }

        public void setBgTextureId(int bgTextureId) {
            VideoRenderImpl.this.bgTextureId = bgTextureId;
        }

        public void setOutWH(int outW, int outH) {
            outWidth = outW;
            outHeight = outH;
        }

        public void drawVideoFrame() {
            drawer.drawVideoFrame(textureId, bgTextureId, texTransformMatrix, outWidth, outHeight, fboId);
        }
    }
}

