package com.mediatek.camera.feature.mode.aiworksfacebeauty.util;

import android.media.Image;

import java.nio.ByteBuffer;

public class ImagePlane {

    private int rowStride;
    private int pixelStride;
    private ByteBuffer buffer;

    public ImagePlane(Image.Plane plane) {
        rowStride = plane.getRowStride();
        pixelStride = plane.getPixelStride();
        byte[] data = new byte[plane.getBuffer().remaining()];
        plane.getBuffer().get(data);
        byte[] temp = new byte[data.length];
        System.arraycopy(data, 0, temp, 0, data.length);
        buffer = ByteBuffer.wrap(temp);
    }

    public int getRowStride() {
        return rowStride;
    }

    public int getPixelStride() {
        return pixelStride;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void release() {
        if (buffer != null) {
            buffer.clear();
            buffer = null;
        }
    }

}
