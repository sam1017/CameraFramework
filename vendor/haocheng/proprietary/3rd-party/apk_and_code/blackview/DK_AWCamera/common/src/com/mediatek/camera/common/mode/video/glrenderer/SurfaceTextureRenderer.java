package com.mediatek.camera.common.mode.video.glrenderer;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class SurfaceTextureRenderer implements OnFrameAvailableListener {

    private static final String TAG = "Video_SurfaceTextureRenderer";

    private HandlerThread mEglThread;
    private Handler mEglHandler;

    private EGLConfig mEglConfig;
    private EGLDisplay mEglDisplay;
    private EGLContext mEglContext;
    private EGLSurface mEglSurface;

    private SurfaceTexture mSurfaceTexture;
    private LiveHDRDrawer mDrawer;
    private SurfaceTextureListener mSurfaceTextureListener;

    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mOutWidth;
    private int mOutHeight;
    private int mFrameNum;

    private static final int EGL_OPENGL_ES2_BIT = 4;
    private static final int[] CONFIG_SPEC = new int[]{
            EGL14.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 0,
            EGL14.EGL_DEPTH_SIZE, 0,
            EGL14.EGL_STENCIL_SIZE, 0,
            EGL14.EGL_NONE
    };

    private final Object mRenderLock = new Object();
    private Runnable mRenderTask = new Runnable() {
        @Override
        public void run() {
            synchronized (mRenderLock) {
                if (mEglDisplay != null && mEglSurface != null && mSurfaceTexture != null) {
                    try {
                    mSurfaceTexture.updateTexImage();
                    float[] mTransform = new float[16];
                    mSurfaceTexture.getTransformMatrix(mTransform);
                    Log.i(TAG,"mDrawer.onDrawFrame mPreviewWidth = " + mPreviewWidth + " mPreviewHeight = " + mPreviewHeight +
                            " mOutWidth = " + mOutWidth + " mOutHeight = " + mOutHeight );
                    mDrawer.onDrawFrame(mTransform, mPreviewHeight, mPreviewWidth, mOutWidth, mOutHeight);

                    EGL14.eglSwapBuffers(mEglDisplay, mEglSurface);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "SurfaceTexture updateTexImage  e = " + e);
                    }
                }
                mRenderLock.notifyAll();
            }
        }
    };

    public SurfaceTextureRenderer(SurfaceTexture target, int outWidth, int outHeight, LiveHDRDrawer drawer) {
        Log.i(TAG,"SurfaceTextureRenderer mOutWidth = " + outWidth + " mOutHeight = " + outHeight);
        mOutWidth = outWidth;
        mOutHeight = outHeight;
        mDrawer = drawer;

        mEglThread = new HandlerThread("SurfaceTextureRenderer");
        mEglThread.start();
        mEglHandler = new Handler(mEglThread.getLooper());
        initialize(target);
    }

    public void acquireSurfaceTexture(final int previewWidth, final int previewHeight, SurfaceTextureListener surfaceTextureListener) {
        Log.d(TAG, "acquireSurfaceTexture");
        mFrameNum = 0;
        mSurfaceTextureListener = surfaceTextureListener;
        mPreviewWidth = previewWidth;
        mPreviewHeight = previewHeight;
        Log.d(TAG, "acquireSurfaceTexture mPreviewWidth = " + mPreviewWidth + " mPreviewHeight = " + mPreviewHeight);
        mEglHandler.post((new Runnable() {
            @Override
            public void run() {
                mSurfaceTexture = mDrawer.createSurfaceTexture();
                mSurfaceTexture.setDefaultBufferSize(previewHeight, previewWidth);
                mSurfaceTexture.setOnFrameAvailableListener(SurfaceTextureRenderer.this);
                mSurfaceTextureListener.onSurfaceTextureAvailable(mSurfaceTexture);
            }
        }));
    }

    public void releaseSurfaceTexture() {
        Log.i(TAG, "releaseSurfaceTexture mSurfaceTexture = " + mSurfaceTexture);
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mSurfaceTexture != null) {
                    mSurfaceTexture.release();
                    mSurfaceTexture = null;
                }
                mDrawer.releaseSurfaceTexture();
            }
        });
        Log.i(TAG, "releaseSurfaceTexture end");
    }

    private void initialize(final SurfaceTexture target) {
        mEglHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "initialize target = " + target);

                mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

                if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
                    throw new RuntimeException("eglGetDisplay failed");
                }
                int[] version = new int[2];
                if (!EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)) {
                    mEglDisplay = null;
                    throw new RuntimeException("eglInitialize failed");
                }

                EGLConfig[] configs = new EGLConfig[1];
                int[] numConfigs = new int[1];
                if (!EGL14.eglChooseConfig(mEglDisplay, CONFIG_SPEC, 0, configs, 0, configs.length,
                        numConfigs, 0)) {
                    throw new RuntimeException("eglChooseConfig failed");
                }

                mEglConfig = configs[0];

                int[] attrib_list = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                        EGL14.EGL_NONE};
                mEglContext = EGL14.eglCreateContext(mEglDisplay, mEglConfig, EGL14.EGL_NO_CONTEXT, attrib_list, 0);

                if (mEglContext == null || mEglContext == EGL14.EGL_NO_CONTEXT) {
                    throw new RuntimeException("eglCreateContext failed");
                }

                int[] surfaceAttribs = {EGL14.EGL_NONE};
                try {
                    mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, target, surfaceAttribs, 0);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    Log.i(TAG,"EGL14.eglCreateWindowSurface failed!");
                }

                if (mEglSurface == null || mEglSurface == EGL14.EGL_NO_SURFACE) {
                    throw new RuntimeException("eglCreateWindowSurface failed");
                }

                if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
                    throw new RuntimeException("eglMakeCurrent failed");
                }
            }
        });
        waitDone();
    }

    public void release() {
        mEglHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "video surface release ");
                if (mEglSurface != null && mEglSurface != EGL14.EGL_NO_SURFACE) {
                    EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE,
                            EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
                    EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
                    Log.d(TAG, "video surface EGL14.eglDestroySurface ");
                }
                if (mEglContext != null) {
                    EGL14.eglDestroyContext(mEglDisplay, mEglContext);
                    Log.d(TAG, "video surface EGL14.eglDestroyContext ");
                }
                if (mEglDisplay != null) {
                    EGL14.eglTerminate(mEglDisplay);
                }
                mEglSurface = null;
                mEglContext = null;
                mEglDisplay = null;
            }
        });
        if (mEglThread != null) {
            mEglThread.getLooper().quitSafely();
            mEglThread = null;
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        draw(false);
        mFrameNum++;
        if (mFrameNum == 1) {
            mSurfaceTextureListener.firstPreviewReceived();
        }
    }

    private void draw(boolean sync) {
        Log.i(TAG,"draw sync = " + sync);
        synchronized (mRenderLock) {
            mEglHandler.post(mRenderTask);
            if (sync) {
                try {
                    mRenderLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void waitDone() {
        final Object lock = new Object();
        synchronized (lock) {
            mEglHandler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            });
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void queueEvent(Runnable runnable) {
        mEglHandler.post(runnable);
        waitDone();
    }

    public void setOutSize(int width, int height) {
        mOutWidth = width;
        mOutHeight = height;
    }

}
