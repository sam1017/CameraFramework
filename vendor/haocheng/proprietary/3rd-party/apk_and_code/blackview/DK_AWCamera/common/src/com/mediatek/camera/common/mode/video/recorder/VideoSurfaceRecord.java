package com.mediatek.camera.common.mode.video.recorder;

import android.content.Context;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.mediatek.camera.common.mode.video.glrenderer.VideoTextureRender;
import com.mediatek.camera.common.mode.video.glrenderer.VideoTextureRenderCallback;
import com.mediatek.camera.common.mode.video.glrenderer.VideoTextureRenderDrawer;
import com.mediatek.camera.common.mode.video.glrenderer.VideoTextureRenderInterface;

import java.io.IOException;

public class VideoSurfaceRecord extends VideoBaseRecord {
    private static final String TAG = VideoSurfaceRecord.class.getSimpleName();

    private static final int VIDEO_RENDER_BLOCK = 200; // unit: ms


    private Surface mVideoRecordSurface;
    private VideoTextureRender mVideoTextureRender;
    private ConditionVariable mCV = new ConditionVariable();

    private VideoTextureRenderCallback mVideoRenderCallback = new VideoTextureRenderCallback() {

        @Override
        public void renderVideoFrame(int textureId, float[] texTransformMatrix, int fboId, VideoTextureRenderDrawer drawer) {
            //Log.i(TAG,"renderVideoFrame textureId = " + textureId);
            renderBgVideoFrame(textureId, -1, texTransformMatrix, fboId, drawer);
        }

        @Override
        public void renderBgVideoFrame(int textureId, int bgTextureId, float[] texTransformMatrix, int fboId, VideoTextureRenderDrawer drawer) {
            //Log.i(TAG,"renderBgVideoFrame ");
            if (mVideoTextureRender.isStopped()) return;

            mCV.close();
            VideoTextureRender.VideoRenderImpl impl = new VideoTextureRender.VideoRenderImpl(drawer);
            impl.setTextureParams(textureId, texTransformMatrix, fboId);
            impl.setBgTextureId(bgTextureId);
            impl.setOutWH(mVideoSize.getHeight(), mVideoSize.getWidth());

            Handler drawHandler = mVideoTextureRender.getDrawHandler();
            Message msg = drawHandler.obtainMessage(VideoTextureRender.DRAW_FRAME, impl);
            drawHandler.sendMessage(msg);

            mCV.block(VIDEO_RENDER_BLOCK);
        }

        @Override
        public void recordYUVdata(byte[] yuvData, int width, int height) {
        }

    };

    public VideoSurfaceRecord(Context context, VideoTextureRenderInterface i) {
        super(context, i);
        Log.i(TAG,"VideoSurfaceRecord init");
        mVideoTextureRender = new VideoTextureRender(i);
        mVideoTextureRender.setConditionVariable(mCV);
    }

    @Override
    public boolean needProcessPreviewData() {
        return false;
    }

    public void setCameraId(int cameraId, int sensorOrientation) {
        super.setCameraId(cameraId, sensorOrientation);
        Log.i(TAG,"VideoSurfaceRecord setCameraId cameraId = " + cameraId + " sensorOrientation = " + sensorOrientation);
        //mVideoTextureRender.setOrientation(cameraId);
    }

    public void setOrientation(int orientation) {
        super.setOrientation(orientation);
        Log.i(TAG,"VideoSurfaceRecord setOrientation " + mOrientation);
        mVideoTextureRender.setOrientation(mOrientation);
    }

    @Override
    public void startVideoRecord() {
        Log.i(TAG,"startVideoRecord mVideoSize = " + mVideoSize);
        buildMediaRecorder(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.start();
        Log.i(TAG,"mVideoTextureRender.startThread mCameraId = " + mCameraId);
        mVideoTextureRender.startThread(mCameraId);
        mVideoTextureRender.start(mVideoRecordSurface, mVideoRenderCallback);
    }

    @Override
    public void stopVideoRecord() {
        mVideoTextureRender.stop();
        mMediaRecorder.stop();
        mMediaRecorder.reset();
    }

    public void setVideoSize(Size videoSize) {
        super.setVideoSize(videoSize);
        Log.i(TAG,"VideoSurfaceRecord setVideoSize videoSize = " + videoSize);
        mVideoTextureRender.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
    }

    public void setVideoFilename(String filepath){
        super.setVideoFilename(filepath);
        mVideoFilename = filepath;
        Log.i(TAG,"setVideoFilename mVideoFilename = " + mVideoFilename);
    }

    private MediaRecorder buildMediaRecorder(int width, int height) {
        try {
            releaseMediaRecorder();
            Log.i(TAG,"VideoSurfaceRecord buildMediaRecorder mVideoFilename = " + mVideoFilename);

            CamcorderProfile profile = CamcorderProfile.get(mCameraId, height == 720 ? CamcorderProfile.QUALITY_720P : CamcorderProfile.QUALITY_1080P);
            // todo need set up MediaRecorder with CamcorderProfile
            int outputFormat = MediaRecorder.OutputFormat.MPEG_4;
            mMediaRecorder = new MediaRecorder();

            if(mIsRecordAudio){
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            }else{
                //mMediaRecorder.setAudioSource(null);
            }
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

            mMediaRecorder.setOutputFormat(outputFormat);
            if(mVideoFilename == null){
                mVideoFilename = getOutputVideoPath(outputFormat);
            }
            mMediaRecorder.setOutputFile(mVideoFilename);
            Log.i(TAG,"VideoSurfaceRecord setOutputFile mVideoFilename = " + mVideoFilename);
            mMediaRecorder.setVideoEncodingBitRate(10000000);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(height, width);
            if(mIsHEVC){
                mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.HEVC);
            }else{
                mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            }

            mMediaRecorder.setAudioEncodingBitRate(profile.audioBitRate);
            mMediaRecorder.setAudioChannels(profile.audioChannels);
            mMediaRecorder.setAudioSamplingRate(profile.audioSampleRate);
            if(mIsRecordAudio){
                mMediaRecorder.setAudioEncoder(profile.audioCodec);
            }else{
                //mMediaRecorder.setAudioSource(null);
            }

            mMediaRecorder.setOrientationHint(mOrientation);
            Log.i(TAG,"VideoSurfaceRecord setOrientationHint mOrientation = " + mOrientation);
            mMediaRecorder.prepare();
            mVideoRecordSurface = mMediaRecorder.getSurface();
            Log.i(TAG,"mVideoRecordSurface = " + mVideoRecordSurface);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mMediaRecorder;
    }

    private void releaseMediaRecorder() {
        Log.v(TAG, "releaseMediaRecorder");
        try {
            if (mMediaRecorder != null) {
                cleanupEmptyFile();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
            if (mVideoRecordSurface != null) {
                // mVideoRecordSurface.release();
                mVideoRecordSurface = null;
            }
            //mVideoFilename = null;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void release() {
        super.release();
        releaseMediaRecorder();
    }

}
