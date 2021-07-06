package com.mediatek.camera.feature.mode.aiworksbokeh.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.format.Formatter;

import java.io.*;

public class AiWorksUtil {

    public static void copyModle(Context context, String modelPath, String modelName) {
        if (!isModleExists(context, modelPath, modelName)) {
            final File modelDir = new File(modelPath);
            if (!modelDir.exists()) {
                modelDir.mkdirs();
            }
            AssetManager assetManager = context.getAssets();
            try {
                String[] children = assetManager.list(modelName);
                for (String child : children) {
                    File childFile = new File(modelPath + "/" + child);
                    child = modelName + "/" + child;
                    if (!childFile.exists() || assetManager.open(child).available() != childFile.length()) {
                        copyFile(assetManager.open(child), childFile);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isModleExists(Context context, String modelPath, String modelName) {
        AssetManager assetManager = context.getAssets();
        String[] children;
        try {
            children = assetManager.list(modelName);
            for (String child : children) {
                File childFile = new File(modelPath + "/" + child);
                child = modelName + "/" + child;
                if (!childFile.exists() || assetManager.open(child).available() != childFile.length()) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void copyFile(InputStream in, File target) {
        if (target == null)
            return;
        boolean copySuccess = true;
        OutputStream out = null;
        try {
            out = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            int counter;
            while ((counter = in.read(buffer)) != -1) {
                out.write(buffer, 0, counter);
            }
        } catch (IOException e) {
            e.printStackTrace();
            copySuccess = false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        if (!copySuccess) {
            target.delete();
        }
    }

    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    public static void showUri(Context context, Uri uri) {
        Intent imageIntent = new Intent(Intent.ACTION_VIEW);
        imageIntent.setDataAndType(uri, "image/*");
        context.startActivity(imageIntent);
    }

    public static Uri saveBitmap(Context context, Bitmap bitmap, String path, String fileName) {
        ByteArrayOutputStream jpgStream;
        jpgStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, jpgStream);
        byte[] data = jpgStream.toByteArray();
        return AiWorksUtil.saveJpeg(context, data, path, fileName);
    }

    public static Uri saveJpeg(Context context, byte[] data, String path, String fileName) {
        new File(path).mkdirs();
        File file = new File(path + fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(data);
            return insertContent(context, file, fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Uri insertContent(Context context, File file, String saveFileName) {
        long now = System.currentTimeMillis();
        final ContentValues values = new ContentValues();
        values.put(Images.Media.TITLE, saveFileName);
        values.put(Images.Media.DISPLAY_NAME, file.getName());
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put(Images.Media.DATE_TAKEN, now);
        values.put(Images.Media.DATE_MODIFIED, now / 1000);
        values.put(Images.Media.ORIENTATION, 0);
        values.put(Images.Media.DATA, file.getAbsolutePath());
        values.put(Images.Media.SIZE, file.length());
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            int imageLength = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
            int imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
            values.put(Images.Media.WIDTH, imageWidth);
            values.put(Images.Media.HEIGHT, imageLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return context.getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static int getJpegOrientation(String filepath) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(filepath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    public static Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    public static String getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return Formatter.formatFileSize(context, mi.availMem);
    }

}
