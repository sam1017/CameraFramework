package com.mediatek.camera.common.mode.video.glrenderer;

public interface VideoTextureRenderInterface {

    void setVideoRecordCallback(VideoTextureRenderCallback callback);

    void queueEvent(Runnable runnable);

}
