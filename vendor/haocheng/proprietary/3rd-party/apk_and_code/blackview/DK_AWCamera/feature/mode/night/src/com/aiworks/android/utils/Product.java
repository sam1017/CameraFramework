package com.aiworks.android.utils;

import android.text.TextUtils;
import java.util.Locale;

public class Product {

    public static final int HARDWARE_PLATFORM_UNKNOW = 0;
    public static final int HARDWARE_PLATFORM_MTK = 1;
    public static final int HARDWARE_PLATFORM_SC = 2;
    public static final int HARDWARE_PLATFORM_QCOM = 3;
    public static final int HARDWARE_PLATFORM_ASR = 4;

    public static int mPlatformID = getHardwarePlatformID();
    public static final String MODEL_NAME = getProductModel();

    private static int getHardwarePlatformID() {
        int mPlatformID = HARDWARE_PLATFORM_UNKNOW;
        String mCpuName = SystemProperties.get("ro.board.platform", "");
        if (mCpuName != null) {
            String mIsgoreCpuName = mCpuName.toLowerCase(Locale.US);
            if (mIsgoreCpuName.contains("mt")) {
                mPlatformID = HARDWARE_PLATFORM_MTK;
            } else if (mIsgoreCpuName.contains("qualcomm") || mIsgoreCpuName.contains("msm")) {
                mPlatformID = HARDWARE_PLATFORM_QCOM;
            } else if (mIsgoreCpuName.contains("sc") || mIsgoreCpuName.contains("sp")) {
                mPlatformID = HARDWARE_PLATFORM_SC;
            } else if (mIsgoreCpuName.contains("aquilac") || mIsgoreCpuName.toLowerCase().contains("asr")) {
                mPlatformID = HARDWARE_PLATFORM_ASR;
            }
        }
        if (mPlatformID != HARDWARE_PLATFORM_UNKNOW) {
            return mPlatformID;
        }
        String hardware = SystemProperties.get("ro.boot.hardware", "");

        if (hardware != null) {
            String mIsgoreHwName = hardware.toLowerCase(Locale.US);
            if (mIsgoreHwName.contains("mt")) {
                mPlatformID = HARDWARE_PLATFORM_MTK;
            } else if (mIsgoreHwName.contains("qcom") || mIsgoreHwName.contains("msm")) {
                mPlatformID = HARDWARE_PLATFORM_QCOM;
            } else if (mIsgoreHwName.contains("sc") || mIsgoreHwName.contains("sp")) {
                mPlatformID = HARDWARE_PLATFORM_SC;
            } else if (mIsgoreHwName.contains("aquilac")) {
                mPlatformID = HARDWARE_PLATFORM_ASR;
            }
        }
        if (mPlatformID != HARDWARE_PLATFORM_UNKNOW) {
            return mPlatformID;
        }
        return HARDWARE_PLATFORM_UNKNOW;
    }

    public static String getHardwarePlatform() {
        return SystemProperties.get("ro.board.platform", "");
    }

    private static String getProductModel() {
        String modelName = SystemProperties.get("ro.product.model", null);
        if (TextUtils.isEmpty(modelName)) {
            modelName = "";
        }
        return modelName;
    }

}
