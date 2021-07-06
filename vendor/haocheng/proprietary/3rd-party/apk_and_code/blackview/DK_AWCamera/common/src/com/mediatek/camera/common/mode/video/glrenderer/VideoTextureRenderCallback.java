package com.mediatek.camera.common.mode.video.glrenderer;

public interface VideoTextureRenderCallback {

    void renderVideoFrame(int textureId, float[] texTransformMatrix, int fboId, VideoTextureRenderDrawer drawer);

    void renderBgVideoFrame(int textureId, int bgTextureId, float[] texTransformMatrix, int fboId, VideoTextureRenderDrawer drawer);

    void recordYUVdata(byte[] yuvData, int width, int height);

}
