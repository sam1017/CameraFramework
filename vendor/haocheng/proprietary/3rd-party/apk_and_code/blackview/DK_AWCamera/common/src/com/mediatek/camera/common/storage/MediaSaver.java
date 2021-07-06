/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.common.storage;

import android.app.Activity;
import android.database.sqlite.SQLiteConstraintException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.ImageFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import androidx.annotation.NonNull;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.mode.photo.HeifHelper;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;
import com.mediatek.camera.portability.SystemProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
//add by huangfei for water mark start
import android.webkit.MimeTypeMap;
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.Config;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.exif.ExifInterface;
import com.mediatek.camera.WaterMarkUtil;
//add by huangfei for water mark end

//*/ hct.huangfei, 20201026. add storagepath.
import java.io.OutputStream;
import android.provider.MediaStore;
import android.content.ContentUris;
//*/
//bv wuyonglin add for AiWorksBokeh water logo 20200827 start
import android.graphics.Bitmap;
//bv wuyonglin add for AiWorksBokeh water logo 20200827 end
//bv wuyonglin add for bug2058 thumbnailView not update right 20200907 start
import java.io.ByteArrayOutputStream;
//bv wuyonglin add for bug2058 thumbnailView not update right 20200907 end

/**
 * the class for saving file after capturing a picture or video, need new it in
 * camera context.
 */
public class MediaSaver {
    private static final Tag TAG = new Tag(MediaSaver.class.getSimpleName());
    private static final String TEMP_SUFFIX = ".tmp";

    private final ContentResolver mContentResolver;
    private final List<Request> mSaveQueue = new LinkedList<>();
    private List<MediaSaverListener> mMediaSaverListeners = new ArrayList<>();
    private SaveTask mSaveTask;
    private static final int INVALID_DURATION = -1;
    private static final int FILE_ERROR = -2;

    private int mSaveDataVersion = SystemProperties.getInt("ro.vendor.mtk_camera_app_data_save_version",
            1);
	//add by huangfei for water mark start
    private static final String KEY_WATERMARK = "key_water_mark";
    private static final String KEY_WATERMARK_ENABLE = "key_water_mark_enable";
    private IAppUi mIAppUi;
    private DataStore mDataStore;
    //add by huangfei for water mark end
            
    //*/ hct.huangfei, 20201026. add storagepath.
    private Activity mActivity;
    //*/
    
    //*/ hct.huangfei, 20201026. add storagepath.
    private static final int VOLUME_SDCARD_INDEX = 1;
    //*/
    
    /**
     * the interface notify others when save completed.
     */
    public interface MediaSaverListener {
        /**
         * notified others when save completed.
         * @param uri The uri of saved file.
         */
        void onFileSaved(Uri uri);
    }

    /**
     * add media saver listener for those who want know new file is saved.
     * @param listener the use listener.
     */
    public void addMediaSaverListener(MediaSaverListener listener) {
        mMediaSaverListeners.add(listener);
    }
    /**
     * the constructor of mediaSaver.
     * @param activity The camera activity
     */
	//modify by huangfei for water mark start
    //public MediaSaver(Activity activity) {
	
	//modify by huangfei for water mark end
    public MediaSaver(Activity activity, IAppUi mIAppUi, DataStore mDataStore) {
        this.mIAppUi = mIAppUi;
        this.mDataStore = mDataStore;
        mContentResolver = activity.getContentResolver();
        
        //*/ hct.huangfei, 20201026. add storagepath.
        mActivity = activity;
        //*/
    }

	//add by huangfei for water mark start
    /**
     * @param contentValues The contentValues to insert into data base, can not be null.
     * @param filePath      The file path where video should save.
     * @param listener      MediaSaverListener notified.
     * @param isContinuousShot The is Continuous Shot Picture
     */
    public void addSaveRequest(@NonNull byte[] pictureData, ContentValues contentValues,
                               @NonNull String filePath, MediaSaverListener listener, boolean isContinuousShot) {
        if (pictureData == null) {
            LogHelper.w(TAG, "[addSaveRequest] there is no valid data need to save.");
            return;
        }
        Request request = new Request(pictureData, contentValues, filePath, listener, null, ImageFormat.JPEG, isContinuousShot, null , null);//bv wuyonglin modify for AiWorksBokeh water logo 20200827
        addRequest(request);
    }
	//add by huangfei for water mark end

    /**
     * Add save request to mediaSaver for only write data base after capturing,
     * most used in case that not need mediaSaver to write fileSystem, such as video.
     * @param contentValues The contentValues to insert into data base, can not be null.
     * @param filePath      The file path where video should save.
     * @param listener      MediaSaverListener notified.
     */
    public void addSaveRequest(@NonNull byte[] pictureData, ContentValues contentValues,
                               @NonNull String filePath, MediaSaverListener listener) {
        addSaveRequest(pictureData, contentValues, filePath, listener, ImageFormat.JPEG);
    }

    //bv wuyonglin add for AiWorksBokeh water logo 20200827 start
    public void addSaveRequest(@NonNull byte[] pictureData, ContentValues contentValues,
                               @NonNull String filePath, MediaSaverListener listener, boolean isContinuousShot, Bitmap pictureBitmap) {
        if (pictureBitmap == null) {
            LogHelper.w(TAG, "[addSaveRequest] there is no valid pictureBitmap need to save.");
            return;
        }
        Request request = new Request(pictureData, contentValues, filePath, listener, null, ImageFormat.JPEG, isContinuousShot, pictureBitmap, null);
        addRequest(request);
    }
    //bv wuyonglin add for AiWorksBokeh water logo 20200827 end

    //bv wuyonglin add for bug2058 thumbnailView not update right 20200907 start
    public void addSaveRequest(@NonNull byte[] pictureData, ContentValues contentValues,
                               @NonNull String filePath, MediaSaverListener listener, boolean isContinuousShot, ExifInterface exif) {
        if (exif == null) {
            LogHelper.w(TAG, "[addSaveRequest] there is no valid exif need to save.");
            return;
        }
        Request request = new Request(pictureData, contentValues, filePath, listener, null, ImageFormat.JPEG, isContinuousShot, null, exif);
        addRequest(request);
    }
    //bv wuyonglin add for bug2058 thumbnailView not update right 20200907 end

    /**
     * Add save request to mediaSaver for write fileSystem and write data base
     * after capturing a picture.
     * @param pictureData   The picture data to save, can not be null.
     * @param contentValues The contentValues to insert into data base, can be null when
     *                      no need insert data base.
     * @param filePath      The file path where picture/video should save.
     *                      can be null if ContentValues has file path.
     * @param type          image format
     * @param listener      MediaSaverListener notified.
     */
    public void addSaveRequest(@NonNull byte[] pictureData, ContentValues contentValues,
                               @NonNull String filePath, MediaSaverListener listener, int type) {
        if (pictureData == null) {
            LogHelper.w(TAG, "[addSaveRequest] there is no valid data need to save.");
            return;
        }
		
		//modify by huangfei for water mark start
        //Request request = new Request(pictureData, contentValues, filePath, listener, null, type);
        Request request = new Request(pictureData, contentValues, filePath, listener, null, type, false, null, null);//bv wuyonglin modify for AiWorksBokeh water logo 20200827
		//modify by huangfei for water mark end
        addRequest(request);
    }


    /**
     * Add save request to mediaSaver for only write data base after capturing,
     * most used in case that not need mediaSaver to write fileSystem, such as video.
     * @param contentValues The contentValues to insert into data base, can not be null.
     * @param filePath      The file path where video should save.
     * @param listener      MediaSaverListener notified.
     */
    public void addSaveRequest(String filePath,Uri uri,
                               MediaSaverListener listener) {
        Request request = new Request(null, null, filePath, listener, uri, 0, false, null, null);
        addRequest(request);
    }
    public void addSaveRequest(@NonNull ContentValues contentValues, String filePath,
                               MediaSaverListener listener) {
        if (contentValues == null) {
            LogHelper.w(TAG, "[addSaveRequest] there is no valid data need to save.");
            return;
        }
		
		//modify by huangfei for water mark start		
        //Request request = new Request(null, contentValues, filePath, listener, null, 0);
        Request request = new Request(null, contentValues, filePath, listener, null, 0, false, null, null);//bv wuyonglin modify for AiWorksBokeh water logo 20200827
		//modify by huangfei for water mark end		
		
        addRequest(request);
    }

    /**
     * update save request to mediaSaver for data base need to update data,
     * most used in case that update data base data according to uri.
     * @param pictureData the jpeg data, can not be null.
     * @param contentValues The contentValues to update into data base, can not be null.
     * @param filePath      The file path where the data should save.
     * @param uri      the uri of saved data.
     */
    public void updateSaveRequest(@NonNull byte[] pictureData,
                                  @NonNull ContentValues contentValues, String filePath,
                                  @Nonnull Uri uri) {
        if (contentValues == null) {
            LogHelper.w(TAG, "[updateSaveRequest] there is no valid data need to save.");
            return;
        }

		//modify by huangfei for water mark start		
        //Request request = new Request(pictureData, contentValues, filePath, null, uri, 0);
        Request request = new Request(pictureData, contentValues, filePath, null, uri, 0, false, null, null);//bv wuyonglin modify for AiWorksBokeh water logo 20200827
		//modify by huangfei for water mark end
		
        addRequest(request);
    }

    /**
     * get the total data bytes waiting in the save task.
     * @return The data size bytes waiting to save.
     */
    public long getBytesWaitingToSave() {
        long totalToWrite = 0;
        synchronized (mSaveQueue) {
            for (Request r : mSaveQueue) {
                totalToWrite += r.getDataSize();
            }
        }
        return totalToWrite;
    }

    /**
     * get the number in the save queue.
     * @return The number in the save queue.
     */
    public int getPendingRequestNumber() {
        synchronized (mSaveQueue) {
            return mSaveQueue.size();
        }
    }

    public Uri insertDB(ContentValues contentValues) {
        Uri uri = null;
        try {
            uri = mContentResolver.insert(
                    Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        } catch (IllegalArgumentException e) {
            // failed to insert into the database. This can happen if
            // the SD card is unmounted.
            LogHelper.e(TAG,
                    "failed to add image to media store, IllegalArgumentException:",
                    e);
        } catch (UnsupportedOperationException e) {
            // failed to insert into the database. This can happen if
            // the SD card is unmounted.
            LogHelper.e(TAG,
                    "failed to add image to media store, UnsupportedOperationException:",
                    e);
        }  catch (SQLiteConstraintException e) {
            // failed to insert into the database. unique constraint failed.
            LogHelper.e(TAG, "failed to add image to media store," +
                    "SQLiteConstraintException:", e);
        } finally {
            LogHelper.v(TAG, "Current image URI: " + uri);
        }
        return uri;
    }

    private void saveDataToStorage(Request request) {
        LogHelper.d(TAG, "[saveDataToStorage]+");
        if (request.mData == null) {
            LogHelper.w(TAG, "data is null,return!");
            return;
        }
        if (request.mFilePath == null && request.mValues != null) {
            LogHelper.d(TAG, "get filePath from contentValues.");
            request.mFilePath = request.mValues.getAsString(ImageColumns.DATA);
        }
        if (request.mFilePath == null) {
            LogHelper.w(TAG, "filePath is null, return");
            return;
        }

        String tempFilePath = request.mFilePath + TEMP_SUFFIX;
        if (request.mType == HeifHelper.FORMAT_HEIF) {
            int width = request.mValues.getAsInteger(ImageColumns.WIDTH).intValue();
            int height = request.mValues.getAsInteger(ImageColumns.HEIGHT).intValue();
            int orientation = request.mValues.getAsInteger(ImageColumns.ORIENTATION).intValue();
            HeifHelper.saveData(request.mData, width, height, orientation, request.mFilePath);
            LogHelper.d(TAG, "[saveDataToStorage]-");
            return;
        }
        //bv liangchangwei add for AiWorks begin
        File file = new File(request.mFilePath);
        if(file.exists()){
            LogHelper.d(TAG, " request.mFilePath = " + request.mFilePath + " exists");
            //add by huangfei for water mark start
           if(!Config.hctHalWaterMarkSupport() || request.mPictureBitmap != null){//bv wuyonglin modify for AiWorksBokeh water logo 20200827
            saveWaterMark(request);
           }
            //add by huangfei for water mark end
            return;
        }
        //bv liangchangwei add for AiWorks end
        FileOutputStream out = null;
        //bv wuyonglin add for bug2058 thumbnailView not update right 20200907 start
        byte[] saveData = null;
        //bv wuyonglin add for bug2058 thumbnailView not update right 20200907 end
        try {
            // Write to a temporary file and rename it to the final name.
            // This
            // avoids other apps reading incomplete data.
            LogHelper.d(TAG, "save the data to SD Card");
            out = new FileOutputStream(tempFilePath);
            //bv wuyonglin add for bug2058 thumbnailView not update right 20200907 start
            LogHelper.d(TAG, " start writeExif  request.mExif = "+request.mExif);
            if (request.mExif != null) {
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                OutputStream exifWriterStream = request.mExif.getExifWriterStream(outStream);
                request.mExif.writeExif(request.mData, exifWriterStream);
                saveData = outStream.toByteArray();
            } else {
                saveData = request.mData;
            }
            //LogHelper.d(TAG, " end writeExif request.mExif = "+request.mExif+" getLatLongAsDoubles[0] ="+" saveData ="+saveData);
            out.write(saveData);
            //bv wuyonglin add for bug2058 thumbnailView not update right 20200907 end
            out.close();
            new File(tempFilePath).renameTo(new File(request.mFilePath));
        } catch (IOException e) {
            LogHelper.e(TAG, "Failed to write image,ex:", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LogHelper.e(TAG, "IOException:", e);
                }
            }
        }
        LogHelper.d(TAG, "[saveDataToStorage]-");
        //add by huangfei for water mark start
        if(!Config.hctHalWaterMarkSupport() || request.mPictureBitmap != null){//bv wuyonglin modify for AiWorksBokeh water logo 20200827
            saveWaterMark(request);
        }
		//add by huangfei for water mark end
    }
	//add by huangfei for water mark start
    private void saveWaterMark(Request request) {
/*
        if (CameraUtil.isWaterMarkOn(mDataStore,mActivity)
                && !request.isContinuousShot && request.mValues != null
                && (request.mData != null || request.mFilePath != null)) {
            LogHelper.i(TAG, "[saveWaterMark]+");

            //add by huangfei for exif for watermark start
            ExifInterface exif  = new ExifInterface();
            try {
                exif.readExif(request.mFilePath);
            } catch (IOException e) {
                LogHelper.e(TAG, "Failed to read EXIF data", e);
            }
            //add by huangfei for exif for watermark start

            String mimeType = request.mValues.getAsString(ImageColumns.MIME_TYPE);


            WaterMarkUtil.saveWaterMark(mActivity,request.mFilePath, request.mValues.getAsLong("datetaken"),exif,false, request.mPictureBitmap);//bv wuyonglin modify for AiWorksBokeh water logo 20200827
            LogHelper.i(TAG, "[saveWaterMark]-");
        }
*/
    }
		//add by huangfei for water mark end

    private void insertDb(Request request) {
        LogHelper.d(TAG, "[insertDb]");
        if (request.mValues == null) {
            LogHelper.w(TAG, "[insertDb] ContentValues is null, return");
            return;
        }
        if (request.mData != null) {
            try {
                // because get the exif from inner is error. so use the SDK api.
                updateContentValues(request);

                //*/ hct.huangfei, 20201026. add storagepath.
                /*request.mUri = mContentResolver.insert(
                        Images.Media.EXTERNAL_CONTENT_URI, request.mValues);*/     
                if(SDCardFileUtils.isSDCardSupport()){
                    request.mUri = mContentResolver.insert(MediaStore.Images.Media.getContentUri(getSdCardExternalVolumeName()),
                         request.mValues);
                    long id = ContentUris.parseId(request.mUri);
                    request.mUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.getContentUri("external"),
                        id);

                }else{
                    request.mUri = mContentResolver.insert(
                        Images.Media.EXTERNAL_CONTENT_URI, request.mValues);
                }         
               //*/ hct.huangfei, 20201026. add storagepath. 
                        
            } catch (IllegalArgumentException e) {
                // failed to insert into the database. This can happen if
                // the SD card is unmounted.
                LogHelper.e(TAG,
                                "failed to add image to media store, IllegalArgumentException:",
                                e);
            } catch (UnsupportedOperationException e) {
                // failed to insert into the database. This can happen if
                // the SD card is unmounted.
                LogHelper.e(TAG,
                        "failed to add image to media store, UnsupportedOperationException:",
                        e);
            }  catch (SQLiteConstraintException e) {
                // failed to insert into the database. unique constraint failed.
                LogHelper.e(TAG, "failed to add image to media store," +
                        "SQLiteConstraintException:", e);
            } finally {
                LogHelper.v(TAG, "Current image URI: " + request.mUri);
            }
        } else {
            if (request.mFilePath == null) {
                LogHelper.w(TAG, "filePath is null when insert video DB");
                return;
            }
            String filePath = request.mValues.getAsString(Video.Media.DATA);
            File temp = new File(request.mFilePath);
            File file = new File(filePath);
            temp.renameTo(file);
            try {

                //*/ hct.huangfei, 20201026. add storagepath.
                /*request.mUri = mContentResolver.insert(
                        Video.Media.EXTERNAL_CONTENT_URI, request.mValues);*/
                if(SDCardFileUtils.isSDCardSupport()){
                    request.mUri = mContentResolver.insert(MediaStore.Video.Media.getContentUri(getSdCardExternalVolumeName()),
                         request.mValues);
                    long id = ContentUris.parseId(request.mUri);     
                    request.mUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.getContentUri("external"),
                        id);
                    LogHelper.i(TAG, "insertDb isSDCardSupport request.mUri: " + request.mUri);
                }else{
                    request.mUri = mContentResolver.insert(
                        Video.Media.EXTERNAL_CONTENT_URI, request.mValues);
                    LogHelper.i(TAG, "insertDb request.mUri: " + request.mUri);
                }        
                //*/
            } catch (IllegalArgumentException e) {
                // failed to insert into the database. This can happen if
                // the SD card is unmounted.
                LogHelper.e(TAG,
                                "failed to add video to media store, IllegalArgumentException:",
                                e);
            } catch (UnsupportedOperationException e) {
                // failed to insert into the database. This can happen if
                // the SD card is unmounted.
                LogHelper.e(TAG,
                        "failed to add video to media store, UnsupportedOperationException:",
                        e);
            } catch (SQLiteConstraintException e) {
                // failed to insert into the database. unique constraint failed.
                LogHelper.e(TAG, "failed to add video to media store," +
                        "SQLiteConstraintException:", e);
            } finally {
                LogHelper.i(TAG, "Current video URI: " + request.mUri);
            }
        }
    }

    //*/ hct.huangfei, 20201026. add storagepath.
    private void updateInsertDbFilePath(Request request){
        if (SDCardFileUtils.isSDCardSupport()){
            String mFilePath = request.mValues.getAsString(Video.Media.DATA);
            if (mFilePath != null && mFilePath.startsWith(SDCardFileUtils.SDCARD_FILE_PATH_START)){
                String mDBFile = mFilePath.replaceFirst(SDCardFileUtils.SDCARD_FILE_PATH_START, SDCardFileUtils.FILE_PATH_INDEX);
                request.mValues.put(Video.Media.DATA, mDBFile);
                LogHelper.d(TAG, "DBFilePath = " + mDBFile);
            }
        }
    }
    //*/

    private void updateDbAccordingUri(Request request) {
        LogHelper.d(TAG, "[updateDbAccordingUri]");
        if (request.mValues == null) {
            LogHelper.w(TAG, "[updateDbAccordingUri] ContentValues is null, return");
            return;
        }
        if (request.mData != null) {
            try {
                // because get the exif from inner is error. so use the SDK api.
                updateContentValues(request);

                mContentResolver.update(
                        request.mUri, request.mValues, null, null);
            } catch (IllegalArgumentException e) {
                // failed to insert into the database. This can happen if
                // the SD card is unmounted.
                LogHelper.e(TAG,
                        "failed to update image to media store, IllegalArgumentException:",
                        e);
            } catch (UnsupportedOperationException e) {
                // failed to insert into the database. This can happen if
                // the SD card is unmounted.
                LogHelper.e(TAG,
                        "failed to update image to media store, UnsupportedOperationException:",
                        e);
            } catch (SQLiteConstraintException e) {
                // failed to insert into the database. unique constraint failed.
                LogHelper.e(TAG, "failed to update image to media store," +
                        "SQLiteConstraintException:", e);
            } finally {
                LogHelper.v(TAG, "Current image URI: " + request.mUri);
            }
        }
    }

    private void addRequest(Request request) {
        LogHelper.d(TAG, "[addSaveRequest]+, the queue number is = "
                + mSaveQueue.size() + "mSaveTask:" + mSaveTask);
        synchronized (mSaveQueue) {
            mSaveQueue.add(request);
        }
        if (mSaveTask == null) {
            mSaveTask = new SaveTask();
            mSaveTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            LogHelper.d(TAG, "[addRequest]execute save AsyncTask.");
        }
        LogHelper.d(TAG, "[addRequest]-, the queue number is = "
                + mSaveQueue.size());
    }

    private void updateContentValues(Request request) {
        if (request.mValues == null){
            LogHelper.d(TAG, "[updateContentValues]request.mValues is null");
            return;
        }
        if (request.mFilePath != null) {
            Integer width = request.mValues.getAsInteger(ImageColumns.WIDTH);
            Integer height = request.mValues.getAsInteger(ImageColumns.HEIGHT);
            LogHelper.d(TAG, "[updateContentValues] size :" + width + " X " + height);
            if (width != null && height != null &&
                    (width.intValue() == 0 || height.intValue() == 0)) {
                //change the mValues;
                Size pictureSize = CameraUtil.getSizeFromSdkExif(request.mFilePath);
                request.mValues.put(ImageColumns.WIDTH, pictureSize.getWidth());
                request.mValues.put(ImageColumns.HEIGHT, pictureSize.getHeight());
                LogHelper.d(TAG, "[updateContentValues] ,update width & height");
            }
        }
    }
    private long getDuration(String fileName) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(fileName);
            return Long.valueOf(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (IllegalArgumentException e) {
            return INVALID_DURATION;
        } catch (RuntimeException e) {
            return FILE_ERROR;
        } finally {
            retriever.release();
        }
    }
    /**
     * inner class for mediaSaver use.
     */
    private class Request {
        private byte[] mData;
        private ContentValues mValues;
        private String mFilePath;
        private MediaSaverListener mMediaSaverListener;
        private Uri mUri;
        private int mType;

	    //add by huangfei for water mark start
        private boolean isContinuousShot;
        //add by huangfei for water mark end
        //bv wuyonglin add for AiWorksBokeh water logo 20200827 start
        private Bitmap mPictureBitmap;
        //bv wuyonglin add for AiWorksBokeh water logo 20200827 end
        //bv wuyonglin add for bug2058 thumbnailView not update right 20200907 start
        private ExifInterface mExif;
        //bv wuyonglin add for bug2058 thumbnailView not update right 20200907 end


        //modify by huangfei for water mark start
        /*public Request(byte[] data, ContentValues values, String filePath,
                MediaSaverListener listener, Uri uri, int type) {*/
        public Request(byte[] data, ContentValues values, String filePath,
                       MediaSaverListener listener, Uri uri, int type,
                       boolean isContinuousShot, Bitmap pictureBitmap , ExifInterface exif) {	//bv wuyonglin modify for AiWorksBokeh water logo 20200827 start
        //modify by huangfei for water mark end

            this.mData = data;
            this.mValues = values;
            this.mFilePath = filePath;
            this.mMediaSaverListener = listener;
            this.mUri = uri;
            this.mType = type;

        //add by huangfei for water mark start
        this.isContinuousShot = isContinuousShot;         
        //add by huangfei for water mark end

        //bv wuyonglin add for AiWorksBokeh water logo 20200827 start
        this.mPictureBitmap = null; //pictureBitmap;
        //bv wuyonglin add for AiWorksBokeh water logo 20200827 end
        //bv wuyonglin add for bug2058 thumbnailView not update right 20200907 start
        this.mExif = exif;
        //bv wuyonglin add for bug2058 thumbnailView not update right 20200907 end
        }

        private int getDataSize() {
            if (mData == null) {
                return 0;
            } else {
                return mData.length;
            }
        }

        private void saveRequest() {
            if(mSaveDataVersion == 0) {
                if (mData != null) {
                    if(mUri == null) {

                        //*/ hct.huangfei, 20201202. add storagepath.
                        //savePhotoRequest();
                        if(SDCardFileUtils.isSDCardSupport()){
                            savePhotoRequestForSDCard();
                        }else{
                            savePhotoRequest();
                        }
                        //*/

                        updateContentValues(this);
                    }
                } else {
                    updateVideoContentValues(this);
                }
            }else {
            saveDataToStorage(this);
            if (this.mUri == null) {
                insertDb(this);
            } else {
                //Update data base according to uri.
                updateDbAccordingUri(this);
                }
            }
        }
        private void updateVideoContentValues(Request request) {
            request.mValues = new ContentValues();
            request.mValues.put(MediaStore.Video.Media.IS_PENDING, 0);
            long duration = getDuration(request.mFilePath);
            request.mValues.put(MediaStore.Video.Media.DURATION, duration);
            LogHelper.d(TAG, "[updateContentValues]request.mUri"+request.mUri);
            try {
                mContentResolver.update(request.mUri,request.mValues,null,null);
            }catch (SecurityException e){
                LogHelper.e(TAG, "updateVideoContentValues Failed ", e);
            }
            mUri = request.mUri;
        }

        private void savePhotoRequest() {
            LogHelper.d(TAG, "[savePhotoRequest] + ");
            final Uri uri = mContentResolver.insert(Images.Media.EXTERNAL_CONTENT_URI, mValues);
            try {
                OutputStream out = mContentResolver.openOutputStream(uri);
                out.write(mData);
                out.flush();
                out.close();
            } catch (Exception e) {
                LogHelper.e(TAG, "savePhotoRequest Failed to write image,ex:", e);
            }
            mUri = uri;
            LogHelper.d(TAG, "[savePhotoRequest] - | mUri = " + mUri);
        }

        //*/ hct.huangfei, 20201202. add storagepath.
        private void savePhotoRequestForSDCard() {
            LogHelper.d(TAG, "[savePhotoRequestForSDCard] + ");
            final Uri uri = mContentResolver.insert(MediaStore.Images.Media.getContentUri(getSdCardExternalVolumeName()),
                        mValues);
            try {
                OutputStream out = mContentResolver.openOutputStream(uri);
                out.write(mData);
                out.flush();
                out.close();
            } catch (Exception e) {
                LogHelper.e(TAG, "savePhotoRequestForSDCard Failed to write image,ex:", e);
            }
            mUri = uri;
            LogHelper.d(TAG, "[savePhotoRequestForSDCard] - | mUri = " + mUri);
        }
        //*/

        private void saveVideoRequest() {
            LogHelper.d(TAG, "[saveVideoRequest] + ");
            final Uri uri = mContentResolver.insert(Video.Media.EXTERNAL_CONTENT_URI, mValues);
            try {
                OutputStream os = mContentResolver.openOutputStream(uri, "w");
                File tempFile = new File(mFilePath);
                Files.copy(tempFile.toPath(), os);
                os.flush();
                os.close();
                tempFile.delete();
            } catch (Exception e) {
                LogHelper.e(TAG, "saveRequest3 Failed to write image,ex:", e);
            }
            mUri = uri;
            LogHelper.d(TAG, "[saveVideoRequest] - | mUri = " + mUri);
        }
    }
    /**
     * the AsyncTask to handle all request to save files.
     */
    private class SaveTask extends AsyncTask<Void, Void, Void> {
        Request mRequest;

        public SaveTask() {
        }

        @Override
        protected void onPreExecute() {
            LogHelper.d(TAG, "[SaveTask]onPreExcute.");
        }

        @Override
        protected Void doInBackground(Void... v) {
            LogHelper.d(TAG, "[SaveTask]doInBackground+, queue is empty = "
                    + mSaveQueue.isEmpty());
            while (!mSaveQueue.isEmpty()) {
                synchronized (mSaveQueue) {
                    if (!mSaveQueue.isEmpty()) {
                        mRequest = mSaveQueue.get(0);
                        mSaveQueue.remove(0);
                    } else {
                        break;
                    }
                }
                mRequest.saveRequest();
                if (mRequest.mMediaSaverListener != null) {
                    mRequest.mMediaSaverListener.onFileSaved(mRequest.mUri);
                    for (MediaSaverListener listener : mMediaSaverListeners) {
                        listener.onFileSaved(mRequest.mUri);
                    }
                }
            }
            mSaveTask = null;
            LogHelper.d(TAG, "[SaveTask] doInBackground-");
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
        }
    }

    //*/ hct.huangfei, 20201026. add storagepath.
    public  String getSdCardExternalVolumeName() {
        ArrayList<String> volumenames = new ArrayList<>( MediaStore.getExternalVolumeNames(mActivity));
        String mVolumeName = null;
        if(volumenames.size() > 0){
            mVolumeName = volumenames.get(0);
            if("external_primary".equals(mVolumeName)){
                mVolumeName = volumenames.get(1);
            }
        }
        return mVolumeName;
    }
    //*/
}
