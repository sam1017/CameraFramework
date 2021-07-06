package com.jni.lib;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AiWorksHdrJni {
    private static final String TAG = "AiWorksHdrJni";
    private Context mContext;

    /* error code */
    public static final int AW_SUCCESS = 0;             /* success */
    public static final int AW_ERR_NOT_INIT = -1;       /* not init or init failure */
    public static final int AW_ERR_WRONG_PARAM = -2;    /* wrong parameter */
    public static final int AW_ERR_OOM = -3;            /* out of memory */
    public static final int AW_ERR_MERGE = -4;          /* internal error */
    public static final int AW_ERR_LARGE_MOTION = -5;   /* camera large motion error */

    /* face orientation */
    public static final int AW_HDR_FACE_UP = 0;         // 人脸向上，即人脸朝向正常
    public static final int AW_HDR_FACE_LEFT = 1;       // 人脸向左，即人脸被逆时针旋转了90度
    public static final int AW_HDR_FACE_DOWN = 2;       // 人脸向下，即人脸被逆时针旋转了180度
    public static final int AW_HDR_FACE_RIGHT = 3;      // 人脸向右，即人脸被逆时针旋转了270度

    private static Method sSystemPropertiesGetMethod = null;
    private static String sFile;

    private static String get(final String name) {
        if (sSystemPropertiesGetMethod == null) {
            try {
                final Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
                if (systemPropertiesClass != null) {
                    sSystemPropertiesGetMethod =
                            systemPropertiesClass.getMethod("get", String.class);
                }
            } catch (final ClassNotFoundException e) {
                // Nothing to do
            } catch (final NoSuchMethodException e) {
                // Nothing to do
            }
        }
        if (sSystemPropertiesGetMethod != null) {
            try {
                return (String) sSystemPropertiesGetMethod.invoke(null, name);
            } catch (final IllegalArgumentException e) {
                // Nothing to do
            } catch (final IllegalAccessException e) {
                // Nothing to do
            } catch (final InvocationTargetException e) {
                // Nothing to do
            }
        }
        return null;
    }

    static {
        String platform = get("ro.board.platform");
        Log.i(TAG, "loadLibrary: platforam = " + platform);
        String model = get("ro.product.model");
        Log.i(TAG, "loadLibrary: model = " + model);
//        if (platform != null && platform.equals("mt6779") && model.equals("Armor 9")) {
        if (platform != null && (platform.equals("mt6779"))) {
            sFile = "aiworks_hdr_mali.bin";
            System.loadLibrary("aiworks_hdr_mali");
        } else if (platform.equals("mt6765") || platform.equals("mt6762") || platform.equals("mt6761")) {
            sFile = "aiworks_hdr_pvr.bin";
            System.loadLibrary("aiworks_hdr_pvr");
        } else if (platform != null && (platform.equals("mt6797") || platform.equals("mt6771") || platform.equals("mt6763")
                || platform.equals("mt6757") || platform.equals("mt6755") || platform.equals("mt6750") || platform.equals("mt6873")
                || platform.equals("mt6833") || platform.equals("exynos5") || platform.startsWith("kirin"))) {
            sFile = "aiworks_hdr_mali.bin";
            System.loadLibrary("aiworks_hdr_mali");
        } else {
            sFile = "aiworks_hdr.bin";
            System.loadLibrary("aiworks_hdr");
        }
    }

    public AiWorksHdrJni(Context context) {
        mContext = context;
    }

    private String getApkPath() {
        String path = mContext.getPackageResourcePath();
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            path = path.substring(0, lastSlash);
        }
        Log.i(TAG, "apk path: " + path);
        return path;
    }

    public static int getFaceSegmentOrientation(int jpegRotation) {
        int faceOrientation = AW_HDR_FACE_UP;
        switch (jpegRotation % 360) {
            case 0:
                faceOrientation = AW_HDR_FACE_UP;
                break;
            case 90:
                faceOrientation = AW_HDR_FACE_LEFT;
                break;
            case 180:
                faceOrientation = AW_HDR_FACE_DOWN;
                break;
            case 270:
                faceOrientation = AW_HDR_FACE_RIGHT;
                break;
        }
        return faceOrientation;
    }

    /**
     * getVersion
     *
     * @return  HDR sdk version
     */
    public String getVersion() {
        return native_getVersion();
    }

    /**
     * init
     *
     * @param   path        bin file directory
     * @return  refer to the error code
     */
    public int init(String path) {
        return native_init(path, getApkPath());
    }

    /**
     * setParameter     call the api if the resolution is changed, the api will allocate memory
     *
     * @param   width           input nv21 image width
     * @param   height          input nv21 image height
     * @return  refer to the error code
     */
    public int setParameter(int width, int height) {
        return native_setParameter(width, height);
    }

    /**
     * setFaceCareEnabled     call the api before capture
     * @param   enable        face care enabled or disabled
     *
     * @return  refer to the error code
     */
    public int setFaceCareEnabled(boolean enable) {
        return native_setFaceCareEnabled(enable);
    }

    /**
     * setCameraMotionThreshold  call the api after calling setParameter()
     * @param   threshold        the threshold for camera motion[0.0f - 1.0f]
     *
     * @return  refer to the error code
     */
    public int setCameraMotionThreshold(float threshold) {
        return native_setCameraMotionThreshold(threshold);
    }

    /**
     * start     call the api before capture
     * @param   numInputImages  Number of input images
     * @param   saturation      saturation enhancement level[1.0f - 1.5f]
     * @param   hasFace         has face or not
     * @param   orientation     refer to face orientation
     * @param   isSuperRes      output 4x size image
     *
     * @return  refer to the error code
     */
    public int start(int numInputImages, float saturation, boolean hasFace, int orientation, boolean isSuperRes) {
        return native_start(numInputImages, saturation, hasFace, orientation, isSuperRes);
    }

    /**
     * add different exposure image
     *
     * @param       yuv             input nv21 image
     * @param       ev              exposure compensation
     * @param       exposureTimeUs  exposure time[Us]
     * @param       sensitivity     sensitivity
     * @param       isBaseImage     is base image or not
     * @return  refer to the error code
     */
    public int addImage(byte[] yuv, int ev, int exposureTimeUs, int sensitivity, boolean isBaseImage) {
        return native_addImage(yuv, ev, exposureTimeUs, sensitivity, isBaseImage);
    }

    /**
     * merge three different exposure image
     *
     * @param       result      output nv21 image
     * @return  refer to the error code
     */
    public int process(byte[] result) {
        return native_process(result);
    }

    /**
     * free reserved memory, the memory is allocated by setParameter
     */
    public void freeMemory() {
        native_freeMemory();
    }

    /**
     * release
     */
    public void release() {
        native_release();
    }

    private native String native_getVersion();
    private native int native_init(String binPath, String apkPath);
    private native int native_setParameter(int width, int height);
    private native int native_setFaceCareEnabled(boolean enable);
    private native int native_setCameraMotionThreshold(float threshold);
    private native int native_start(int numInputImages, float saturation, boolean hasFace, int orientation, boolean isSuperRes);
    private native int native_addImage(byte[] yuv, int ev, int exposureTimeUs, int sensitivity, boolean isBaseImage);
    private native int native_process(byte[] result);
    private native void native_freeMemory();
    private native void native_release();

//    private byte[] readAssetsFile(String path) {
//        byte[] result = null;
//        InputStream inputStream = null;
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        try {
//            File file = new File(path, sFile);
//            if (file.exists() && file.length() > 0) {
//                inputStream = new FileInputStream(file);
//            } else {
//                inputStream = mContext.getAssets().open(sFile);
//            }
//
//            byte[] buffer = new byte[inputStream.available()];
//            int n;
//            while ((n = inputStream.read(buffer)) != -1) {
//                out.write(buffer, 0, n);
//            }
//            result = out.toByteArray();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//                out.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return result;
//    }

}
