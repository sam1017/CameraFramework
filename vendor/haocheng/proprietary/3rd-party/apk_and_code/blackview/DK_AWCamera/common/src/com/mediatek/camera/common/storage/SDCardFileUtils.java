package com.mediatek.camera.common.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class SDCardFileUtils {
    private static final String TAG = "SDCardFileUtils";
    private static final long LIMIT = (long) (100 * 1024 * 1024); //100MB

    public static final int REQUEST_RW_CODE = 1001;
    public static final String FILE_PATH_INDEX = "/storage/";
    public static final String SDCARD_FILE_PATH_START = "/mnt/media_rw/";
    public static final String SDCARD_PATH_TYPE = "storage_path_value";
    public static final String AUTHORIZATION_PATH = "sdcard_file_path";
    public static final String TREE_URI = "tree_uri";

    private static StorageManager mStorageManager;
    private static SDCardFileUtils mInstance;
    private static Context mContext;

    public static SDCardChangeListener mListener;
    public static void setSDCardChangeListener(SDCardChangeListener listener){
        if (mListener == null)
            mListener = listener;
    }
    public static SDCardChangeListener getSDCardChangeListener(){
        return mListener;
    }
    public interface SDCardChangeListener {
        void onSDCardChanged(String file);
    }

    public static SDCardFileUtils getInstance(Context context){
        if (mInstance == null) {
            mInstance = new SDCardFileUtils(context);
        }
        return mInstance;
    }

    private SDCardFileUtils(Context context) {
        mContext = context;
    }

    public static String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = mContext.getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    public static String getSDCardVolumePath(){
        if (mStorageManager == null)
            mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] storageVolumeList = mStorageManager.getVolumeList();
        if (storageVolumeList != null) {
            for (StorageVolume volume : storageVolumeList) {
                String path = volume.getPath();
                if(volume.isRemovable() && MountPoint.isMounted(mStorageManager, path)){
                    /*MountPoint mountPoint = new MountPoint();
                    mountPoint.mDescription = volume.getDescription(mContext);
                    mountPoint.mPath = path;
                    mountPoint.mIsMounted = MountPoint.isMounted(path);
                    mountPoint.mIsExternal = volume.isRemovable();
                    mountPoint.mMaxFileSize = volume.getMaxFileSize();
                    Log.d(TAG, "path = " + mountPoint.mPath);*/
                    return path;
                }
            }
        }
        return null;
    }

    public static SharedPreferences getSharedPreferences(){
        return mContext.getSharedPreferences("storage_path", Context.MODE_PRIVATE);
    }

    public static boolean isSDCardSupport() {
        boolean isSupport = false;
        String volumePath = getSDCardVolumePath();
        String type = getSharedPreferences().getString(SDCARD_PATH_TYPE, "Internal");
        ArrayList<String> volumenames = new ArrayList<>( MediaStore.getExternalVolumeNames(mContext));
        android.util.Log.i("SDCardFileUtils","volumenames size:"+volumenames.size()+"type:"+type);
        if("External".equals(type)&& volumenames.size()>1){
            return true;
        }
        return isSupport;
    }

    private static class MountPoint {
        public String mDescription;
        public String mPath;
        public boolean mIsExternal;
        public boolean mIsMounted;
        public long mMaxFileSize;
        public long mFreeSpace;
        public long mTotalSpace;

        public static boolean isMounted(StorageManager storageManager, String mountPoint) {
            if (TextUtils.isEmpty(mountPoint)) {
                return false;
            }
            String state = storageManager.getVolumeState(mountPoint);
            return Environment.MEDIA_MOUNTED.equals(state);
        }
    }
}
