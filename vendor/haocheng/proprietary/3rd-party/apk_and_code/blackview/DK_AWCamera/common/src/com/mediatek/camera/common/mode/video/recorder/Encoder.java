package com.mediatek.camera.common.mode.video.recorder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class Encoder {
    private static final String TAG = "Encoder";
    private static final boolean LOG_DEBUG = true;

    private static final String VIDEO_MIME_TYPE = "video/avc";
    private static final int VIDEO_BITRATE = 8000000;    // 8000kbps
    private static final int I_FRAME_INTERVAL = 1;

    private String mFilename;
    private int mWidth;
    private int mHeight;
    private int mFps;

    private int mVideoTrackIndex;
    private MediaMuxer mMediaMuxer;
    private MediaCodec mVideoEncoder;

    private ByteBuffer[] mInputBuffers;
    private ByteBuffer[] mOutputBuffers;
    private Queue<Integer> mAvailableInputBuffers;
    private Queue<Integer> mAvailableOutputBuffers;
    private MediaCodec.BufferInfo[] mOutputBufferInfo;

    public Encoder(String filename, int width, int height, int fps) {
        Log.i(TAG, "construct encoder");
        mFilename = filename;
        mWidth = width;
        mHeight = height;
        mFps = fps;
    }

    public boolean prepare(int orientation, int colorFormat) {
        Log.i(TAG, "Creating MediaMuxer");
        try {
            mMediaMuxer = new MediaMuxer(mFilename, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        Log.d(TAG, "setOrientationHint:" + orientation);
        mMediaMuxer.setOrientationHint(orientation);

        Log.i(TAG, "Creating Video MediaCodec");
        MediaFormat videoFormat = new MediaFormat();
        videoFormat.setInteger(MediaFormat.KEY_WIDTH, mWidth);
        videoFormat.setInteger(MediaFormat.KEY_HEIGHT, mHeight);
        videoFormat.setString(MediaFormat.KEY_MIME, VIDEO_MIME_TYPE);
        //videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        //videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BITRATE);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mFps);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
        videoFormat.setFloat(MediaFormat.KEY_OPERATING_RATE, 90.0f);

        try {
            mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
            mMediaMuxer.release();
            return false;
        }
        mVideoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mVideoEncoder.start();

        mInputBuffers = mVideoEncoder.getInputBuffers();
        mOutputBuffers = mVideoEncoder.getOutputBuffers();
        mOutputBufferInfo = new MediaCodec.BufferInfo[mOutputBuffers.length];
        mAvailableInputBuffers = new ArrayDeque<>(mOutputBuffers.length);
        mAvailableOutputBuffers = new ArrayDeque<>(mInputBuffers.length);
        mAvailableOutputBuffers.clear();
        return true;
    }

    public boolean queueInputBuffer(final ByteBuffer input, final long presentationTimeUs, boolean isEos) {
        boolean result = false;
        int size = input.remaining();

        // check if we have dequed input buffers available from the codec
        if (size > 0 && !mAvailableInputBuffers.isEmpty()) {
            int index = mAvailableInputBuffers.remove();
            ByteBuffer buffer = mInputBuffers[index];

            // we can't write our sample to a lesser capacity input buffer.
            if (size > buffer.capacity()) {
                Log.e(TAG, "Insufficient capacity in MediaCodec buffer: "
                        + "tried to write " + input.remaining() + ", buffer capacity is " + buffer.capacity());
                return false;
            }

            buffer.clear();
            buffer.put(input);

            // Submit the buffer to the codec for decoding. The presentationTimeUs
            // indicates the position (play time) for the current sample.
            if (LOG_DEBUG) {
                Log.i(TAG, "queueInputBuffer: size = " + size + ", timeUs = " + presentationTimeUs + ", isEos = " + isEos);
            }
            if (isEos) {
                mVideoEncoder.queueInputBuffer(index, 0, size, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                mVideoEncoder.queueInputBuffer(index, 0, size, presentationTimeUs, 0);
            }

            result = true;
        }

        return result;
    }

    /**
     * Synchronize this object's state with the internal state of the wrapped
     * MediaCodec.
     */
    public void dequeueOutputBuffer() {
        int index;

        // Get valid input buffers from the codec to fill later in the same order they were
        // made available by the codec.
        while ((index = mVideoEncoder.dequeueInputBuffer(0)) != MediaCodec.INFO_TRY_AGAIN_LATER) {
            mAvailableInputBuffers.add(index);
        }

        // Likewise with output buffers. If the output buffers have changed, start using the
        // new set of output buffers. If the output format has changed, notify listeners.
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        while ((index = mVideoEncoder.dequeueOutputBuffer(info, 0)) != MediaCodec.INFO_TRY_AGAIN_LATER) {
            switch (index) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.i(TAG, "INFO_OUTPUT_BUFFERS_CHANGED: flags = " + info.flags);
                    mOutputBuffers = mVideoEncoder.getOutputBuffers();
                    mOutputBufferInfo = new MediaCodec.BufferInfo[mOutputBuffers.length];
                    mAvailableOutputBuffers.clear();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.i(TAG, "INFO_OUTPUT_FORMAT_CHANGED: flags = " + info.flags);
                    MediaFormat newFormat = mVideoEncoder.getOutputFormat();
                    mVideoTrackIndex = mMediaMuxer.addTrack(newFormat);
                    mMediaMuxer.start();
                    break;
                default:
                    // Making sure the index is valid before adding to output buffers. We've already
                    // handled INFO_TRY_AGAIN_LATER, INFO_OUTPUT_FORMAT_CHANGED &
                    // INFO_OUTPUT_BUFFERS_CHANGED i.e all the other possible return codes but
                    // asserting index value anyways for future-proofing the code.
                    if (index >= 0) {
                        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            Log.v(TAG, "video ignoring BUFFER_FLAG_CODEC_CONFIG");
                        } else if (info.size != 0) {
                            ByteBuffer encodedData = mVideoEncoder.getOutputBuffer(index);
                            encodedData.position(info.offset);
                            encodedData.limit(info.offset + info.size);
                            if (LOG_DEBUG) {
                                Log.i(TAG, "writeSampleData: index = " + index + ", flags = " + info.flags + ", timeUs = " + info.presentationTimeUs);
                            }
                            mMediaMuxer.writeSampleData(mVideoTrackIndex, encodedData, info);
                        }

                        // releases the buffer back to the codec
                        mVideoEncoder.releaseOutputBuffer(index, false);
                    } else {
                        throw new IllegalStateException("Unknown status from dequeueOutputBuffer");
                    }
                    break;
            }
        }
    }

    public void dequeueLastOutputBuffer() {
        Log.i(TAG, "dequeueLastOutputBuffer: E");
        boolean isEos = false;
        int index;

        // Likewise with output buffers. If the output buffers have changed, start using the
        // new set of output buffers. If the output format has changed, notify listeners.
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        while ((index = mVideoEncoder.dequeueOutputBuffer(info, 10000)) != MediaCodec.INFO_TRY_AGAIN_LATER) {
            switch (index) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.i(TAG, "INFO_OUTPUT_BUFFERS_CHANGED: flags = " + info.flags);
                    mOutputBuffers = mVideoEncoder.getOutputBuffers();
                    mOutputBufferInfo = new MediaCodec.BufferInfo[mOutputBuffers.length];
                    mAvailableOutputBuffers.clear();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.i(TAG, "INFO_OUTPUT_FORMAT_CHANGED: flags = " + info.flags);
                    MediaFormat newFormat = mVideoEncoder.getOutputFormat();
                    mVideoTrackIndex = mMediaMuxer.addTrack(newFormat);
                    mMediaMuxer.start();
                    break;
                default:
                    // Making sure the index is valid before adding to output buffers. We've already
                    // handled INFO_TRY_AGAIN_LATER, INFO_OUTPUT_FORMAT_CHANGED &
                    // INFO_OUTPUT_BUFFERS_CHANGED i.e all the other possible return codes but
                    // asserting index value anyways for future-proofing the code.
                    if (index >= 0) {
                        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            Log.v(TAG, "video ignoring BUFFER_FLAG_CODEC_CONFIG");
                        } else if (info.size != 0) {
                            ByteBuffer encodedData = mVideoEncoder.getOutputBuffer(index);
                            encodedData.position(info.offset);
                            encodedData.limit(info.offset + info.size);
                            if (LOG_DEBUG) {
                                Log.i(TAG, "writeSampleData: index = " + index + ", flags = " + info.flags + ", timeUs = " + info.presentationTimeUs);
                            }
                            mMediaMuxer.writeSampleData(mVideoTrackIndex, encodedData, info);

                            if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                                isEos = true;
                            }
                        }

                        // releases the buffer back to the codec
                        mVideoEncoder.releaseOutputBuffer(index, false);
                    } else {
                        throw new IllegalStateException("Unknown status from dequeueOutputBuffer");
                    }
                    break;
            }

            if (isEos) break;
        }
        Log.i(TAG, "dequeueLastOutputBuffer: X");
    }

    public void stopAndRelease() {
        Log.i(TAG, "stopAndRelease");
        mVideoEncoder.stop();
        mVideoEncoder.release();
        mMediaMuxer.stop();
        mMediaMuxer.release();

        mVideoEncoder = null;
        mMediaMuxer = null;
    }
}
