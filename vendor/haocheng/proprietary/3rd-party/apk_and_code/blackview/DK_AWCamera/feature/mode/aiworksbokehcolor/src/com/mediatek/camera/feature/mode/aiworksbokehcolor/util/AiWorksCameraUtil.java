package com.mediatek.camera.feature.mode.aiworksbokehcolor.util;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.OrientationEventListener;
import android.view.WindowManager;
import com.android.ex.camera2.portability.Size;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.content.res.AssetManager;
import java.io.InputStream;
import java.io.OutputStream;

public class AiWorksCameraUtil {
    private static float sPixelDensity = 1;
    private static final int ORIENTATION_HYSTERESIS = 5;

    public static void setMetrics(DisplayMetrics metrics) {
        sPixelDensity = metrics.density;
    }

    public static int dpToPixel(int dp) {
        return Math.round(sPixelDensity * dp);
    }

    private static CameraSizeComparator sizeComparator = new CameraSizeComparator();

    private static class CameraSizeComparator implements Comparator<Size> {
        public int compare(Size lhs, Size rhs) {
            if (lhs.width() == rhs.width()) {
                return 0;
            } else if (lhs.width() > rhs.width()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private static boolean equalRate(Size s, float rate) {
        float r = (float) (s.width()) / (float) (s.height());
        return Math.abs(r - rate) <= 0.03;
    }

    public static Size getSizebyRatio(List<Size> sizes, float targetRatio) {
        Collections.sort(sizes, sizeComparator);
        int i = 0;
        for (Size s : sizes) {
            if (equalRate(s, targetRatio)) {
                break;
            }
            i++;
        }
        if (i == sizes.size()) {
            i = 0;
        }
        return sizes.get(i);
    }

    public static Size getSizebyRatio(List<Size> sizes, float targetRatio, int limit) {
        Collections.sort(sizes, sizeComparator);
        int i = 0;
        for (Size s : sizes) {
            if (equalRate(s, targetRatio) && s.height() <= limit) {
                break;
            }
            i++;
        }
        if (i == sizes.size()) {
            return null;
        }
        return sizes.get(i);
    }

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

    public static int roundOrientation(int orientation, int orientationHistory) {
        boolean changeOrientation = false;
        if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
            changeOrientation = true;
        } else {
            int dist = Math.abs(orientation - orientationHistory);
            dist = Math.min(dist, 360 - dist);
            changeOrientation = (dist >= 45 + ORIENTATION_HYSTERESIS);
        }
        if (changeOrientation) {
            return ((orientation + 45) / 90 * 90) % 360;
        }
        return orientationHistory;
    }

    public static int getJpegRotation(int cameraId, int orientation) {
        int rotation = 0;
        if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360;
            } else {
                rotation = (info.orientation + orientation) % 360;
            }
        }
        return rotation;
    }

    public static void calculateTapArea(float areaMultiple, int x, int y, int previewWidth, int previewHeight,
                                        Rect rect) {
        Matrix mMatrix = new Matrix();
        Matrix matrix = new Matrix();
        matrix.setScale( 1, 1);
        matrix.postRotate(90);
        matrix.postScale(previewWidth / 2000f, previewHeight / 2000f);
        matrix.postTranslate(previewWidth / 2f, previewHeight / 2f);
        matrix.invert(mMatrix);

        int areaSize = (int) (Math.max(previewWidth, previewHeight) / 8 * areaMultiple);
        int left = clamp(x - areaSize / 2, 0, previewWidth - areaSize);
        int top = clamp(y - areaSize / 2, 0, previewHeight - areaSize);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
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

    public static boolean isFocusAreaSupported(Camera.Parameters params) {
        return (params.getMaxNumFocusAreas() > 0 && isSupported(
                Camera.Parameters.FOCUS_MODE_AUTO, params.getSupportedFocusModes()));
    }

    public static boolean isMeteringAreaSupported(Camera.Parameters params) {
        return params.getMaxNumMeteringAreas() > 0;
    }

    public static boolean isSupported(String value, List<String> supported) {
        return supported != null && supported.indexOf(value) >= 0;
    }

    public static ArrayList<Size> getSelectedPictureSizes(
            List<Size> supportedPictureSizes, double targetAspectRatio, int[] targets) {
        ArrayList<Size> aspectRatioMatches = new ArrayList<>();
        for (Size size : supportedPictureSizes) {
            double aspectRatio = size.width() / (double) size.height();
            if (targetAspectRatio > 2.025) {
                if (aspectRatio - targetAspectRatio > 0) {
                    aspectRatioMatches.add(size);
                }
            } else if (Math.abs(aspectRatio - targetAspectRatio) < 0.05) {
                aspectRatioMatches.add(size);
            }
        }
        ArrayList<Size> sizes = new ArrayList<>();
        for(int i = 0; i < targets.length; i++) {
            if (targets[i] > 0) {
                Size select = null;
                int dist = -1;
                for(int j = 0; j < aspectRatioMatches.size(); j++) {
                    Size size = aspectRatioMatches.get(j);
                    int area = calculatePictureSizeArea(size);
                    int abs = Math.abs(targets[i] - area);
                    if (abs < 50) {
                        if (dist == -1 || dist > abs) {
                            dist = abs;
                            select = size;
                        }
                    }
                }
                if (select != null) {
                    sizes.add(select);
                }
            }
        }
        return sizes;
    }

    public static int calculatePictureSizeArea(Size size) {
        return size == null ? 0 : size.width() * size.height() / 10000;
    }

    public static int genExternalOESTexture(){
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    public static void deleteExternalOESTexture(int[] textureIds) {
        if (textureIds != null && textureIds.length > 0) {
            GLES20.glDeleteTextures(textureIds.length, textureIds, 0);
        }
    }

    public static void createFileWithByte(byte[] bytes, String name) {
        File file = new File(Environment.getExternalStorageDirectory(),
                name);
        FileOutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            if (file.exists()) {
                file.delete();
            }
            file = new File(name);
            outputStream = new FileOutputStream(file);
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            bufferedOutputStream.write(bytes);
            bufferedOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
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
}
