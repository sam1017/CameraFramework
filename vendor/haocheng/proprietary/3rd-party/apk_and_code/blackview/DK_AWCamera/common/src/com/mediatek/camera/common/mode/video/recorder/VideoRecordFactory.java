package com.mediatek.camera.common.mode.video.recorder;

import android.content.Context;

import com.mediatek.camera.common.mode.video.glrenderer.VideoTextureRenderInterface;


public class VideoRecordFactory {

    public static VideoBaseRecord create(Context context, VideoTextureRenderInterface i, boolean yuvVideoRecord) {
        if (yuvVideoRecord) {
            return new VideoDataBufferRecord(context, i);
        }

        return new VideoSurfaceRecord(context, i);
    }
}
