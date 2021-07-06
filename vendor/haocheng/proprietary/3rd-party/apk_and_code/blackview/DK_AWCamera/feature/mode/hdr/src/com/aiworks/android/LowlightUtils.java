package com.aiworks.android;

import android.graphics.Rect;

import java.util.List;

public class LowlightUtils {

    public static final int FACE_UP = 0;
    public static final int FACE_RIGHT = 1;
    public static final int FACE_DOWN = 2;
    public static final int FACE_LEFT = 3;

    public static int getFaceSegmentOrientation(int jpegRotation) {
        int faceOrientation = FACE_UP;
        switch (jpegRotation % 360) {
            case 0:
                faceOrientation = FACE_UP;
                break;
            case 90:
                faceOrientation = FACE_LEFT;
                break;
            case 180:
                faceOrientation = FACE_DOWN;
                break;
            case 270:
                faceOrientation = FACE_RIGHT;
                break;
        }
        return faceOrientation;
    }

    static {
        System.loadLibrary("aw_lowlightutils");
    }

    public static native int initFaceSegment(String modelPath, int precision);

    public static native void releaseFaceSegment();

    public static native int faceSegment(byte[] buffer, int width, int height, int format, int ori,
                                         int outWidth, int outHeight, byte[] result);

    public static native int faceSegmentWithRect(byte[] buffer, int width, int height, int format, int ori, int erodeLevel, int dilateLevel,
                                                 List<Rect> rects, int outWidth, int outHeight, byte[] result);

    public static native int faceSegment2(byte[] buffer, int width, int height, int format, int ori,
                                          int erodeLevel, int outWidth1, int outHeight1, byte[] result1,
                                          int dilateLevel, int outWidth2, int outHeight2, byte[] result2);

    public static native int initPortraitSegment(String modelPath, int precision);

    public static native void releasePortraitSegment();

    public static native int portraitSegment(byte[] buffer, int width, int height, int format, int ori,
                                             int outWidth, int outHeight, byte[] result);

    public static native int getHighLightContours(byte[] image, int width, int height, int format, int highLight, int[] rect);

    public static native int getLowLightContours(byte[] image, int width, int height, int format, int lowLight, int[] rect);

    public static native void erodeMask(byte[] mask, int width, int height, int erodeLevel, int outWidth, int outHeight, byte[] result);

    public static native void dilateMask(byte[] mask, int width, int height, int dilateLevel, int outWidth, int outHeight, byte[] result);

    public static native void setLogable(boolean enable);

}
