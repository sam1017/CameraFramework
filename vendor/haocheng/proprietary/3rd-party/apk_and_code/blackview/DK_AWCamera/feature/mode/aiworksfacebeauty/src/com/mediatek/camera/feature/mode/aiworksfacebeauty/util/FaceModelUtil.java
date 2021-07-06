package com.mediatek.camera.feature.mode.aiworksfacebeauty.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.StatFs;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FaceModelUtil {

    private static final String TAG = "FaceModelUtil";

    private static final String MODEL_NAME = "FaceModelPlus";

    public void doCopy(Context context) {
        final String modelPath = getFaceModelPath(context);
        final File modelDir = new File(modelPath);
        if (!modelDir.exists()) {
            boolean result = modelDir.mkdirs();
            if(!result) {
                Log.w(TAG, "mkdirs error");
            }
        }
        AssetManager assetManager = context.getAssets();
        try {
            String[] children = assetManager.list(MODEL_NAME);
            for (String child : children) {
                File childFile = new File(modelPath + File.separator + child);
                child = MODEL_NAME + File.separator + child;
                InputStream is = assetManager.open(child);
                if (!childFile.exists() || is.available() != childFile.length()) {
                    copyFile(is, childFile);
                } else {
                    closeSilently(is);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFile(InputStream is, File file) {
        FileOutputStream fos = null;
        File temp = new File(file.getPath() + ".temp");
        try {
            fos = new FileOutputStream(temp/*file*/);
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();//刷新缓冲区

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeSilently(is);
            if (fos != null) {
                try {
                    fos.close();
                    if(temp.exists() && temp.length() > 0) {
                        if(file.exists()) {
                            boolean result = file.delete();
                            if(!result) {
                                Log.w(TAG, "delete error:"+file);
                            }
                        }
                        boolean result = temp.renameTo(file);
                        if(!result) {
                            Log.w(TAG, "renameTo error:"+temp);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static String getFaceModelPath(Context context) {
        File dir = context.getFilesDir();
        String path = dir.getAbsolutePath() + File.separator + MODEL_NAME;
        //Logs.d(TAG, "getFaceModelPath path:" + path);
        return path;
    }

    public static boolean isFaceModleExists(Context context) {
        AssetManager assetManager = context.getAssets();
        String[] children;
        InputStream inputStream = null;
        try {
            children = assetManager.list(FaceModelUtil.MODEL_NAME);
            for (String child : children) {
                File childFile = new File(
                        getFaceModelPath(context) + "/" + child);
                child = FaceModelUtil.MODEL_NAME + "/" + child;
                if (!childFile.exists()) {
                    return false;
                } else {
                    inputStream = assetManager.open(child);
                    if (inputStream.available() != childFile.length()) {
                        return false;
                    }
                    try {
                        inputStream.close();
                        inputStream = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "isFaceModleExists e:" + e);
            e.printStackTrace();
        } finally {
            closeSilently(inputStream);
        }
        return true;
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void createCacheDirs(String path) {
        File dir = new File(path);
        if (!dir.exists()) dir.mkdirs();
        File nomedia = new File(dir.getPath() + "/.nomedia");
        try {
            if (!nomedia.exists())
                nomedia.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long getAvailableSize(String path) {
        StatFs fileStats = new StatFs(path);
        fileStats.restat(path);
        return (long) fileStats.getAvailableBlocks() * fileStats.getBlockSize();
    }
    public static long isLowModelsStorage(String path) {
        try {
            long availableSize = getAvailableSize(path) / (1024 * 1024);
            Log.d(TAG, "isLowModelsStorage availableSize :" + availableSize);
            /*if (availableSize < 100) {
                Log.d(TAG, "isLowModelsStorage true");
                return true;
            }*/
            return availableSize;
        } catch (Exception e) {
            Log.w(TAG, "isLowModelsStorage Exception " + e);
            if(e instanceof IllegalArgumentException) {
                return 0;
            }
        }
        Log.d(TAG, "isLowModelsStorage false");
        return 1024/*false*/;
    }
}
