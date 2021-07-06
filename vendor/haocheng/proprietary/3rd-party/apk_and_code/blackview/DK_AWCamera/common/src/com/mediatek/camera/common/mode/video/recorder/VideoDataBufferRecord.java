package com.mediatek.camera.common.mode.video.recorder;

import android.content.Context;
import android.graphics.ImageFormat;
import android.media.MediaRecorder;
import android.util.Log;
import android.util.Size;

import com.aiworks.android.utils.Util;
import com.mediatek.camera.common.mode.video.glrenderer.VideoTextureRenderCallback;
import com.mediatek.camera.common.mode.video.glrenderer.VideoTextureRenderDrawer;
import com.mediatek.camera.common.mode.video.glrenderer.VideoTextureRenderInterface;

import java.nio.ByteBuffer;

public class VideoDataBufferRecord extends VideoBaseRecord {
    private static final String TAG = VideoDataBufferRecord.class.getSimpleName();

    private static final int FRAME_PER_SECOND = 30;

    private int mVideoColorFormat;
    private Encoder mEncoder;

    private byte[] mSrcVideo;

    private static Object mLock = new Object();

    private VideoTextureRenderCallback mCallback = new VideoTextureRenderCallback() {

        @Override
        public void renderVideoFrame(int textureId, float[] texTransformMatrix, int fboId, VideoTextureRenderDrawer drawer) {
        }

        @Override
        public void renderBgVideoFrame(int textureId, int bgTextureId, float[] texTransformMatrix, int fboId, VideoTextureRenderDrawer drawer) {
        }

        @Override
        public void recordYUVdata(byte[] yuvData, int width, int height) {
            if (!isVideoRecording()) return;

            int ylen = width * height;
            int uvlen = width * height / 4;

            System.arraycopy(yuvData, 0, mSrcVideo, 0, ylen);

            if (mVideoColorFormat == 19) {
                System.arraycopy(yuvData, ylen + uvlen, mSrcVideo, ylen, uvlen);
                System.arraycopy(yuvData, ylen, mSrcVideo, ylen + uvlen, uvlen);
            } else {
                for (int i = 0; i < uvlen * 2; i += 2) {
                    mSrcVideo[ylen + i] = yuvData[ylen + i + 1];
                    mSrcVideo[ylen + i + 1] = yuvData[ylen + i];
                }
            }

            ByteBuffer buffer = ByteBuffer.wrap(mSrcVideo);
            long timestamp = System.currentTimeMillis() * 1000;
            buffer.rewind();
            if (!isVideoRecording()) {
                buffer.clear();
                return;
            }

            synchronized (mLock) {
                if (mEncoder != null) {
                    mEncoder.queueInputBuffer(buffer, timestamp, false);
                    mEncoder.dequeueOutputBuffer();
                }
            }
            buffer.clear();
        }

    };

    public VideoDataBufferRecord(Context context, VideoTextureRenderInterface i) {
        super(context, i);
    }

    @Override
    public boolean needProcessPreviewData() {
        return true;
    }

    @Override
    public void setVideoSize(Size videoSize) {
        super.setVideoSize(videoSize);
        int previewWidth = videoSize.getHeight();
        int previewHeight = videoSize.getWidth();

        mSrcVideo = new byte[previewWidth * previewHeight * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
        mVideoColorFormat = Util.getVideoColorFormat();
        Log.d(TAG, String.format("setVideoSize: %d*%d, videoColorFormat: %d", previewWidth, previewHeight, mVideoColorFormat));
    }

    @Override
    public void startVideoRecord() {
        int width = mVideoSize.getWidth();
        int height = mVideoSize.getHeight();
        renderInterface.setVideoRecordCallback(mCallback);
        mEncoder = new Encoder(mVideoFilename = getOutputVideoPath(MediaRecorder.OutputFormat.MPEG_4),
                width, height, FRAME_PER_SECOND);

        mEncoder.prepare(getVideoOrientation(), mVideoColorFormat);
    }

    private int getVideoOrientation() {
        int result = mSensorOrientation;
        if (mCameraId == Util.CAMERA_ID_FRONT) {
            if (mSensorOrientation == 270) {
                switch (mOrientation) {
                    case 0:
                        break;
                    case 90:
                        result = 90;
                        break;
                    case 180:
                        break;
                    case 270:
                        result = 90;
                        break;
                }
            } else if (mSensorOrientation == 90) {
                switch (mOrientation) {
                    case 0:
                        break;
                    case 90:
                        result = 270;
                        break;
                    case 180:
                        break;
                    case 270:
                        result = 270;
                        break;
                }

            }
        }

        return result;
    }

    @Override
    public void stopVideoRecord() {
        renderInterface.setVideoRecordCallback(null);
        synchronized (mLock) {
            mEncoder.stopAndRelease();
            mEncoder = null;
        }
    }

}
