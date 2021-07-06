package com.mediatek.camera.common.mode.video.glrenderer;

import android.graphics.SurfaceTexture;

public interface SurfaceTextureListener {
    void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture);

    void firstPreviewReceived();
}
