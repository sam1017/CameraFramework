package com.jni.lib;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AiWorksNsJni {
    private static final String TAG = "AiWorksNsJni";

    /**
     * 多帧模式
     * [1帧]1张正常曝光
     * [2帧]1张正常曝光，1张欠曝
     * [3帧]1张正常曝光，1张欠曝，1张更欠曝
     * [3帧]3张相同曝光
     * [4帧]4张相同曝光
     * [5帧]4张相同曝光，1张欠曝
     * [6帧]4张相同曝光，1张欠曝，1张更欠曝
     */
    public static int NS_MODE_EXPOSURE_MULTIPLE = 0;

    /**
     *  多帧降噪模式
     *  [3帧]3张相同曝光
     *  [4帧]4张相同曝光
     */
    public static int NS_MODE_EXPOSURE_SAME = 1;

    /**
     * HDR模式
     * [2帧]1张正常曝光，1张欠曝
     * [3帧]1张正常曝光，1张欠曝，1张更欠曝
     */
    public static int NS_MODE_HDR = 2;

    /* 降噪强度[chrom] */
    public static int NS_NR_LEVEL1 = 1;
    public static int NS_NR_LEVEL2 = 2;
    public static int NS_NR_LEVEL3 = 3;
    public static int NS_NR_LEVEL4 = 4;
    public static int NS_NR_LEVEL5 = 5;
    public static int NS_NR_LEVEL6 = 6;
    public static int NS_NR_LEVEL7 = 7;
    public static int NS_NR_LEVEL8 = 8;
    public static int NS_NR_LEVEL9 = 9;
    public static int NS_NR_LEVEL10 = 10;

    /* 降噪强度[luma] */
    public static int NS_LNR_LEVEL1 = 1;
    public static int NS_LNR_LEVEL2 = 2;
    public static int NS_LNR_LEVEL3 = 3;
    public static int NS_LNR_LEVEL4 = 4;
    public static int NS_LNR_LEVEL5 = 5;
    public static int NS_LNR_LEVEL6 = 6;
    public static int NS_LNR_LEVEL7 = 7;
    public static int NS_LNR_LEVEL8 = 8;
    public static int NS_LNR_LEVEL9 = 9;
    public static int NS_LNR_LEVEL10 = 10;

    /* 默认对比度 */
    public static float NS_DEF_CONTRAST_LEVEL = 0.0f;

    /* 默认锐化强度 */
    public static float NS_DEF_SP_LEVEL = 0.5f;

    /* 亮度等级 */
    public static int NS_TMO_LEVEL1 = 1;
    public static int NS_TMO_LEVEL2 = 2;
    public static int NS_TMO_LEVEL3 = 3;
    public static int NS_TMO_LEVEL4 = 4;
    public static int NS_TMO_LEVEL5 = 5;
    public static int NS_TMO_LEVEL6 = 6;
    public static int NS_TMO_LEVEL7 = 7;
    public static int NS_TMO_LEVEL8 = 8;
    public static int NS_TMO_LEVEL9 = 9;
    public static int NS_TMO_LEVEL10 = 10;

    public static int NS_FLAGS_NR = 0x01;
    public static int NS_FLAGS_SHARPNESS = 0x02;
    public static int NS_FLAGS_TMO = 0x04;
    public static int NS_FLAGS_LNR = 0x08;
    public static int NS_FLAGS_CE = 0x10;

    /* face orientation */
    public static final int AW_FACE_UP = 0;         // 人脸向上，即人脸朝向正常
    public static final int AW_FACE_LEFT = 1;       // 人脸向左，即人脸被逆时针旋转了90度
    public static final int AW_FACE_DOWN = 2;       // 人脸向下，即人脸被逆时针旋转了180度
    public static final int AW_FACE_RIGHT = 3;      // 人脸向右，即人脸被逆时针旋转了270度

    public static int getFaceSegmentOrientation(int jpegRotation) {
        int faceOrientation = AW_FACE_UP;
        switch (jpegRotation % 360) {
            case 0:
                faceOrientation = AW_FACE_UP;
                break;
            case 90:
                faceOrientation = AW_FACE_LEFT;
                break;
            case 180:
                faceOrientation = AW_FACE_DOWN;
                break;
            case 270:
                faceOrientation = AW_FACE_RIGHT;
                break;
        }
        return faceOrientation;
    }

    private Context mContext;
    private static String sFile = "merge.bin";

    private static Method sSystemPropertiesGetMethod = null;

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

    // Used to load the 'native-lib' library on application startup.
    static {
        String platform = get("ro.board.platform");
        Log.i(TAG, "loadLibrary: platforam = " + platform);
        String model = get("ro.product.model");
        Log.i(TAG, "loadLibrary: model = " + model);
        sFile = "aw_ns_pvr.bin";
        System.loadLibrary("aw_ns_pvr");
    }

    public AiWorksNsJni(Context context) {
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

    /**
     * 初始化
     * @param path [in]bin文件的路径，加载外部bin文件时用，不加载外部bin文件是设置为null
     * @param faceModelPath [in]人脸模型的路径, 传入null不特殊处理人脸
     * @return 0 成功; -1 未初始化；-2 参数错误；-3 内存不足；-4 其他错误
     */
    public int init(String path, String faceModelPath) {
        return native_init(path, getApkPath(), faceModelPath);
    }

    /**
     * 设置参数
     * @param width [in]NV21图像的宽
     * @param height [in]NV21图像的高
     * @param pitch [in]图像各个平面的跨距pitch
     * @param mode  [in]模式，设置为NS_MODE_EXPOSURE_MULTIPLE/NS_MODE_EXPOSURE_SAME/NS_MODE_HDR
     * @param fast [in]快速模式，取值true or false，设置true算法耗时更短
     * @param allocMem [in]预申请内存，取值true or false
     * @return 0 成功; -1 未初始化；-2 参数错误；-3 内存不足；-4 其他错误
     */
    public int setResolution(int width, int height, int[] pitch, int mode, boolean fast, boolean allocMem){
        return native_setResolution(width, height, pitch, mode, fast, allocMem);
    }

    /**
     * 获取拍照实例
     * @return 成功返回实例id; -1 未初始化；-2 参数错误；-3 内存不足；-4 其他错误
     */
    public long getInstance() {
        return native_getInstance();
    }

    /**
     * 设置参数
     * @param instance        [in]the instance from getInstance
     * @param numInputImages  [in]Number of input images
     * @param hasFace         [in]has face or not
     * @param orientation     [in]refer to face orientation
     * @param nrLevel         [in]chrom降噪强度，取值NS_NR_LEVEL1 - NS_NR_LEVEL10
     * @param lnrLevel        [in]luma降噪强度，取值NS_LNR_LEVEL1 - NS_LNR_LEVEL10
     * @param tmoLevel        [in]亮度等级，取值NS_TMO_LEVEL1 - NS_TMO_LEVEL10
     * @param ceLevel         [in]对比度强度，取值[0.0f, 1.0f]
     * @param spLevel         [in]锐化强度，取值[0.0f, 1.0f]
     * @return 0 成功; -1 未初始化；-2 参数错误；-3 内存不足；-4 其他错误
     */
    public int setParameter(long instance, int numInputImages, boolean hasFace, int orientation, int nrLevel, int lnrLevel, int tmoLevel, float ceLevel, float spLevel) {
        return native_setParameter(instance, numInputImages, hasFace, orientation, nrLevel, lnrLevel, tmoLevel, ceLevel, spLevel);
    }

    /**
     * add different exposure image, note:
     * (1)if ev of all the frames is not the same, ignore the exposureTimeUs and sensitivity
     * (2)if ev of all the frames is the same and ev != 0, ignore the exposureTimeUs and sensitivity
     * (3)if ev of all the frames is the same and ev == 0, exposureTimeUs and sensitivity must be set to the real value(not zero)
     *
     * @param       instance        [in]the instance from getInstance
     * @param       yuv             [in]input nv21 image
     * @param       ev              [in]exposure compensation
     * @param       exposureTimeUs  [in]exposure time[Us]
     * @param       sensitivity     [in]sensitivity
     * @param       isBaseImage     [in]is base image or not
     * @return  refer to the error code
     */
    public int addFrame(long instance, byte[] yuv, int index, int ev, int exposureTimeUs, int sensitivity, boolean isBaseImage) {
        return native_addFrame(instance, yuv, index, ev, exposureTimeUs, sensitivity, isBaseImage);
    }

    /**
     * addFrame之后调用
     * @param       instance        [in]the instance from getInstance
     * @param       result          [out]处理之后返回的NV21图像
     * @return 0 成功; -1 未初始化；-2 参数错误；-3 内存不足；-4 其他错误
     */
    public int process(long instance, byte[] result) {
        return native_process(instance, result);
    }

    /**
     * addFrame之后调用
     * @param       instance        [in]the instance from getInstance
     * @param       callback        异步处理回调，需要实现OnAsyncProcessCallback接口
     * @return 0 成功; -1 未初始化；-2 参数错误；-3 内存不足；-4 其他错误
     */
    public int processAsync(long instance, OnAsyncProcessCallback callback) {
        return native_processAsync(instance, callback);
    }

    /**
     * 释放资源
     */
    public void release() {
        native_release();
    }

    private native int native_init(String binPath, String path, String faceModelPath);
    private native int native_setResolution(int width, int height, int[] pitch, int mode, boolean fast, boolean allocMem);
    private native long native_getInstance();
    private native int native_setParameter(long instance, int numInputImages, boolean hasFace, int orientation, int cnrLevel, int lnrLevel, int tmoLevel, float ceLevel, float spLevel);
    private native int native_addFrame(long instance, byte[] yuv, int index, int ev, int exposureTimeUs, int sensitivity, boolean isBaseImage);
    private native int native_process(long instance, byte[] result);
    private native int native_processAsync(long instance, OnAsyncProcessCallback callback);
    private native void native_release();

    public interface OnAsyncProcessCallback{
        void onAsyncProcessFinish(long id, byte[] result);
    }
}
