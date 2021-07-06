package com.mediatek.camera.feature.mode.aiworksfacebeauty.glrenderer;

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
import android.view.TextureView;
import android.content.Context;

//import com.aiworks.android.portrait.PortraitEffect;

public class SurfaceTextureRenderer implements OnFrameAvailableListener {

    private static final String TAG = SurfaceTextureRenderer.class.getSimpleName();

    private HandlerThread mEglThread;
    private Handler mEglHandler;

    private EGLConfig mEglConfig;
    private EGLDisplay mEglDisplay;
    private EGLContext mEglContext;
    private EGLSurface mEglSurface;

    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    private FaceBeautyDrawer mFaceBeautyDrawer;

    private int mPreviewWidth;
    private int mPreviewHeight;

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
                if (mEglDisplay != null && mEglSurface != null) {
                    try {
                    //mSurfaceTexture.updateTexImage();
                    float[] mTransform = new float[16];
                    //mSurfaceTexture.getTransformMatrix(mTransform);
                    mFaceBeautyDrawer.onDrawFrame(mTransform, mPreviewWidth, mPreviewHeight, mTextureView.getWidth(), mTextureView.getHeight());

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

    public SurfaceTextureRenderer(TextureView textureView, Context context, SurfaceTexture target) {

        mTextureView = textureView;

        mEglThread = new HandlerThread("SurfaceTextureRenderer");
        mEglThread.start();
        mEglHandler = new Handler(mEglThread.getLooper());
        Log.d(TAG, "SurfaceTextureRenderer initialize target ="+target+" mSurfaceTexture ="+mSurfaceTexture+" textureView ="+textureView+" mTextureView.getWidth() ="+mTextureView.getWidth()+" mTextureView.getHeight() ="+mTextureView.getHeight());
        mEglHandler.post((new Runnable() {
            @Override
            public void run() {
        mFaceBeautyDrawer = new FaceBeautyDrawer(context);
            }
        }));
        initialize(target);
    }

    public void acquireSurfaceTexture(final int previewWidth, final int previewHeight, final SurfaceTextureListener surfaceTextureListener) {
        Log.d(TAG, "acquireSurfaceTexture onSurfaceTextureAvailable previewWidth ="+previewWidth+" previewHeight ="+previewHeight);
        mPreviewWidth = previewHeight;
        mPreviewHeight = previewWidth;
        mFaceBeautyDrawer.setCameraPreviewSize(mPreviewWidth,mPreviewHeight);
        mEglHandler.post((new Runnable() {
            @Override
            public void run() {
                mSurfaceTexture = mFaceBeautyDrawer.createSurfaceTexture();
       Log.d(TAG, "acquireSurfaceTexture onSurfaceTextureAvailable setDefaultBufferSize1 previewHeight ="+previewHeight+" previewWidth ="+previewWidth);
                mSurfaceTexture.setDefaultBufferSize(previewWidth, previewHeight);
                mSurfaceTexture.setOnFrameAvailableListener(SurfaceTextureRenderer.this);
        Log.d(TAG, "acquireSurfaceTexture onSurfaceTextureAvailable mSurfaceTexture ="+mSurfaceTexture);
                surfaceTextureListener.onSurfaceTextureAvailable(mSurfaceTexture);
            }
        }));
    }

    //bv wuyonglin add for bug3529 20210126 start
    public void updateSurfaceTextureSize(final int previewWidth, final int previewHeight) {
        Log.d(TAG, "updateSurfaceTextureSize onSurfaceTextureAvailable previewWidth ="+previewWidth+" previewHeight ="+previewHeight);
        mFaceBeautyDrawer.updateSurfaceViewSize(previewWidth,previewHeight);
    }
    //bv wuyonglin add for bug3529 20210126 end

    public void releaseSurfaceTexture() {
        Log.i(TAG, "releaseSurfaceTexture");
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        mEglHandler.post((new Runnable() {
            @Override
            public void run() {
        mFaceBeautyDrawer.releaseSurfaceTexture();
            }
        }));
    }

    public FaceBeautyDrawer getFaceBeautyDrawer() {
        return mFaceBeautyDrawer;
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

                android.opengl.EGLConfig[] configs = new android.opengl.EGLConfig[1];
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
                Log.d(TAG, "initialize mEglSurface = " + mEglSurface);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                Log.d(TAG, "initialize e = " + e);
                }

                if (mEglSurface == null || mEglSurface == EGL14.EGL_NO_SURFACE) {
                Log.d(TAG, "initialize RuntimeException mEglSurface = " + mEglSurface+" mEglSurface no ="+(mEglSurface == EGL14.EGL_NO_SURFACE));
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
                Log.d(TAG, "release ");
                if (mEglSurface != null && mEglSurface != EGL14.EGL_NO_SURFACE) {
                    EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE,
                            EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
                    EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
                }
                if (mEglContext != null) {
                    EGL14.eglDestroyContext(mEglDisplay, mEglContext);
                }
                if (mEglDisplay != null) {
                    EGL14.eglTerminate(mEglDisplay);
                }
                mEglSurface = null;
                mEglContext = null;
                mEglDisplay = null;
            }
        });
        synchronized (mRenderLock) {
        if (mEglThread != null) {
            mEglThread.getLooper().quitSafely();
            mEglThread = null;
        }
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        draw(false);
    }

    private void draw(boolean sync) {
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

}
