package com.mediatek.camera.common.mode.video.recorder;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.util.Size;

import com.mediatek.camera.R;
import com.mediatek.camera.common.mode.video.glrenderer.VideoTextureRenderInterface;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class VideoBaseRecord {
    private static final String TAG = VideoBaseRecord.class.getSimpleName();

    protected int mOrientation;
    protected int mCameraId;
    protected int mSensorOrientation;
    protected String mVideoFilename;
    protected boolean mIsRecordAudio;
    protected boolean mIsHEVC = false;
    public static final String SAVE_PATH = Environment.getExternalStorageDirectory().getPath() + "/DCIM/LiveHDR/";

    protected Size mVideoSize;
    protected MediaRecorder mMediaRecorder;

    protected boolean mIsVideoRecording = false;
    private Context mContext;
    protected VideoTextureRenderInterface renderInterface;

    public VideoBaseRecord(Context context, VideoTextureRenderInterface i) {
        mContext = context;
        renderInterface = i;
    }

    public void setCameraId(int cameraId, int sensorOrientation) {
        Log.i(TAG,"setCameraId cameraId = " + cameraId);
        this.mCameraId = cameraId;
        this.mSensorOrientation = sensorOrientation;
    }

    public void setOrientation(int orientation) {
        Log.i(TAG,"setOrientation mOrientation = " + mOrientation);
        this.mOrientation = orientation;
    }

    public void setVideoSize(Size videoSize) {
        mVideoSize = videoSize;
    }

    public void setAudioSource(boolean isRecordAudio){
        mIsRecordAudio = isRecordAudio;
    }

    public void setHEVC(boolean isHEVC){
        mIsHEVC = isHEVC;
    }

    public boolean isVideoRecording() {
        return mIsVideoRecording;
    }

    public void videoRecord() {
        Log.i(TAG,"videoRecord mIsVideoRecording = " + mIsVideoRecording);
        try {
            if (mIsVideoRecording) {
                stopVideoRecord();
            } else {
                startVideoRecord();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (mIsVideoRecording) {
/*                startVideoReview(listener);*/
            }
        }
        mIsVideoRecording = !mIsVideoRecording;
    }

    public MediaRecorder getMediaRecorder(){
        return mMediaRecorder;
    }

    public void release() {
        if (mIsVideoRecording) {
            stopVideoRecord();
            mIsVideoRecording = false;
        }
    }

    protected String getOutputVideoPath(int outputFormat) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(mContext.getString(R.string.video_file_name_format),
                Locale.US);
        String videoFileName = dateFormat.format(date);
        if (outputFormat == MediaRecorder.OutputFormat.MPEG_4) {
            videoFileName += ".mp4";
        } else {
            videoFileName += ".3gp";
        }

        String dirPath = SAVE_PATH;
        File videoDir = new File(dirPath);
        if (!videoDir.exists()) {
            videoDir.mkdirs();
        }

        try {
            File videoFile = new File(dirPath, videoFileName);
            videoFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dirPath + File.separator + videoFileName;
    }

    public void setVideoFilename(String filepath){
        mVideoFilename = filepath;
        Log.i(TAG,"setVideoFilename mVideoFilename = " + mVideoFilename);
    }

    protected void cleanupEmptyFile() {
        if (mVideoFilename == null) return;

        File f = new File(mVideoFilename);
        if (f.length() == 0 && f.delete()) {
            Log.v(TAG, "Empty video file deleted: " + mVideoFilename);
            //mVideoFilename = null;
        } else {
            Log.i(TAG, "cleanupEmptyFile: " + mVideoFilename);
        }
    }

/*    protected void startVideoReview(final CameraEngineListener listener) {
        if (mContext instanceof Activity) {
            MediaScannerConnection.scanFile(mContext.getApplicationContext(), new String[]{mVideoFilename}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, final Uri uri) {
                            Log.d(TAG, String.format("onScanCompleted: %s->%s", path, uri));
                            final Activity activity = (Activity) mContext;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Util.showUri(activity, uri, true);
                                    if (listener != null) {
                                        listener.onUpdateThumbnail(uri);
                                    }
                                }
                            });
                        }
                    }
            );
        }
    }*/

    abstract public boolean needProcessPreviewData();

    abstract public void startVideoRecord();

    abstract public void stopVideoRecord();
}
