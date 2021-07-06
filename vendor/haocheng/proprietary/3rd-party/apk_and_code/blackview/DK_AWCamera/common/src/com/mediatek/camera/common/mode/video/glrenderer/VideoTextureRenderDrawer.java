package com.mediatek.camera.common.mode.video.glrenderer;

public interface VideoTextureRenderDrawer {

    void drawVideoFrame(int textureId, int bgTextureId, float[] texTransformMatrix, int outWidth, int outHeight, int fboId);

}
