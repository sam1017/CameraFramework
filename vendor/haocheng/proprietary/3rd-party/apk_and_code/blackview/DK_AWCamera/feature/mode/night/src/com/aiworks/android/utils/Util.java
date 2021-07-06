package com.aiworks.android.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.camera2.params.MeteringRectangle;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.OrientationEventListener;
import android.view.WindowManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;

import com.android.camera.exif.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class Util {
    public static final int CAMERA_ID_FRONT = 1;
    public static int[] getScreenSize(Context context) {
        int[] screenSize = new int[2];
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getRealSize(point);
        screenSize[0] = point.x;
        screenSize[1] = point.y;
        if (point.y > point.x) {
            screenSize[0] = point.x;
            screenSize[1] = point.y;
        } else {
            screenSize[0] = point.y;
            screenSize[1] = point.x;
        }
        return screenSize;
    }

    public static int getJpegRotation(int cameraId, int orientation) {
        int rotation = 0;
        if (cameraId > 1) {
            cameraId = 0;
        }
        if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(cameraId, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360;
            } else {
                rotation = (info.orientation + orientation) % 360;
            }
        }
        return rotation;
    }

    public static long writeImage(String path, ExifInterface exif, byte[] jpeg) {
        if (exif != null) {
            try {
                exif.writeExif(jpeg, path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            writeFile(path, jpeg);
        }
        return getFileSize(path);
    }

    private static void writeFile(String path, byte[] data) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static long getFileSize(String path) {
        File file = new File(path);
        return file.length();
    }

    public static Uri insertContent(Context context, String path, String title, long fileLength) {
        long now = System.currentTimeMillis();
        final ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_TAKEN, now);
        values.put(MediaStore.Images.Media.DATE_MODIFIED, now / 1000);
        values.put(MediaStore.Images.Media.DATE_ADDED, now);
        values.put(MediaStore.Images.Media.ORIENTATION, 0);
        values.put(MediaStore.Images.Media.DATA, path);
        values.put(MediaStore.Images.Media.SIZE, fileLength);
        return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

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

    public static void showUri(Context context, Uri uri) {
        if (uri == null || context == null) return;

        Intent imageIntent = new Intent(Intent.ACTION_VIEW);
        imageIntent.setDataAndType(uri, "image/*");
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(imageIntent, 0);
        if (list != null && list.size() > 0) {
            context.startActivity(imageIntent);
        }
    }

    public static boolean compareRect(Rect rect1, Rect rect2, int limit) {
        return Math.abs(rect1.left - rect2.left) > limit || Math.abs(rect1.right - rect2.right) > limit
                || Math.abs(rect1.top - rect2.top) > limit || Math.abs(rect1.bottom - rect2.bottom) > limit;
    }

    public static void calculateTapArea(int width, int height, int x, int y, int previewWidth, int previewHeight, Rect rect, boolean mirror) {
        Matrix mMatrix = new Matrix();
        Matrix matrix = new Matrix();
        matrix.setScale(mirror ? -1 : 1, 1);
        matrix.postRotate(90);
        matrix.postScale(previewWidth / 2000f, previewHeight / 2000f);
        matrix.postTranslate(previewWidth / 2f, previewHeight / 2f);
        matrix.invert(mMatrix);

        int left = clamp(x - width / 2, 0, previewWidth - width);
        int top = clamp(y - height / 2, 0, previewHeight - height);
        RectF rectF = new RectF(left, top, left + width, top + height);
        mMatrix.mapRect(rectF);
        rectFToRect(rectF, rect);
    }

    private static int clamp(int x, int min, int max) {
        if (x > max)
            return max;
        if (x < min)
            return min;
        return x;
    }

    private static void rectFToRect(RectF rectF, Rect rect) {
        rect.left = Math.round(rectF.left);
        rect.top = Math.round(rectF.top);
        rect.right = Math.round(rectF.right);
        rect.bottom = Math.round(rectF.bottom);
    }

    public static MeteringRectangle[] legacyAreasToMeteringRectangles(
            List<Camera.Area> reference, Rect cropRectangle) {
        MeteringRectangle[] transformed = null;
        if (reference.size() > 0) {
            transformed = new MeteringRectangle[reference.size()];
            for (int index = 0; index < reference.size(); ++index) {
                android.hardware.Camera.Area source = reference.get(index);
                Rect rectangle = source.rect;
                double oldLeft = (rectangle.left + 1000) / 2000.0;
                double oldTop = (rectangle.top + 1000) / 2000.0;
                double oldRight = (rectangle.right + 1000) / 2000.0;
                double oldBottom = (rectangle.bottom + 1000) / 2000.0;
                int left = cropRectangle.left + toIntConstrained(
                        cropRectangle.width() * oldLeft, 0, cropRectangle.width() - 1);
                int top = cropRectangle.top + toIntConstrained(
                        cropRectangle.height() * oldTop, 0, cropRectangle.height() - 1);
                int right = cropRectangle.left + toIntConstrained(
                        cropRectangle.width() * oldRight, 0, cropRectangle.width() - 1);
                int bottom = cropRectangle.top + toIntConstrained(
                        cropRectangle.height() * oldBottom, 0, cropRectangle.height() - 1);
                transformed[index] = new MeteringRectangle(left, top, right - left, bottom - top,
                        source.weight);
            }
        }
        return transformed;
    }

    private static int toIntConstrained(double original, int min, int max) {
        original = Math.max(original, min);
        original = Math.min(original, max);
        return (int) original;
    }

    public static int getVideoColorFormat() {
        boolean isSupportNV21 = false;
        boolean isSupportYV12 = false;

        MediaCodec codec;
        try {
            codec = MediaCodec.createEncoderByType("video/avc");
            MediaCodecInfo info = codec.getCodecInfo();

            int[] mColorFormats = info.getCapabilitiesForType("video/avc").colorFormats;
            for (int i = 0; i < mColorFormats.length; i++) {
                if (mColorFormats[i] == 19) {
                    isSupportYV12 = true;
                }
                if (mColorFormats[i] == 21) {
                    isSupportNV21 = true;
                }
            }
            codec.stop();
            codec.release();

            if (isSupportNV21) {
                return 21;
            } else if (isSupportYV12) {
                return 19;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
