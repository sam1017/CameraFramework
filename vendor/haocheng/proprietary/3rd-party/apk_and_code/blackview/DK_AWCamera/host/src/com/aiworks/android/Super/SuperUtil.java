package com.aiworks.android.Super;

import android.content.Context;
import android.graphics.ImageFormat;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.util.Xml;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class SuperUtil {
    private static final String TAG = "SuperUtil";

    public static int aeMeterMode = -1;
    public static int edgeMode = -1;
    public static int noiseReductionMode = -1;
    public static int sharpness = -1;
    public static int saturation = -1;
    public static int contrast = -1;
    public static int sceneMode = -1;

    public static boolean hasFace = false;

    public static final String CONFIG_PATH = "/sdcard/etc/";
    private static final String CAMERA_OUTSIDE_PREFERENCES_FILE_NAME = "customized_hd_preferences.xml";
    private static final String KEY_CAMERA_HD_CONFIG = "pref_hd_mode_config_key";
    public static final String DUMP_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/hd/";
    public static final String DUMP_FILE_DEBLUR_PATH = Environment.getExternalStorageDirectory().getPath() + "/deblur/";

    public static String mProductName = null;

    public static String mDumpFileTitle;
    public static boolean mIsDumpFile = false;

    public static Size photoSize = null;
    public static int mPhotoFormat = ImageFormat.YUV_420_888;

    public static float mMf_sp;
    public static float mMf_ce;
    public static boolean mMf_fast;
    public static float mFaceLevel;

    public static float mSf_sp;
    public static float mSf_ce;

    public static boolean mForceMtkMfnr;

    public static float SHAKE_THRESHOLD;

    public static boolean USE_PLATFORM_MFNR = false;
    private static String BIN_FILE_PATH = null;

    public static void superConfigData(Context context, String path) {
        Log.i(TAG, "superConfigData start");
        BIN_FILE_PATH = path;
        String superConfigJsonStr = null;
        HashMap<String, String> customPreferences = loadCustomizedCameraPreferences(context, path);
        if (customPreferences != null) {
            superConfigJsonStr = customPreferences.get(KEY_CAMERA_HD_CONFIG);
        }

        if (superConfigJsonStr != null && superConfigJsonStr.length() > 0) {
            try {
                JSONObject jsonObject = new JSONObject(superConfigJsonStr);
                jsonOpt(jsonObject);
            } catch (JSONException e) {
                Log.e(TAG, "Cannot parse customized_hd_preferences.xml! Please check format." + e.toString());
            }
        } else {
            Log.e(TAG, "customized_hd_preferences.xml is null.");
        }
        Log.i(TAG, " mProductName : " + mProductName);
        Log.i(TAG, "superConfigData end");

    }

    private static void jsonOpt(JSONObject jsonObject) {
        aeMeterMode = jsonObject.optInt("aeMeterMode", -1);
        edgeMode = jsonObject.optInt("edgeMode", -1);
        noiseReductionMode = jsonObject.optInt("noiseReductionMode", -1);
        sharpness = jsonObject.optInt("sharpness", -1);
        saturation = jsonObject.optInt("saturation", -1);
        contrast = jsonObject.optInt("contrast", -1);
        sceneMode = jsonObject.optInt("sceneMode", -1);

        mProductName = jsonObject.optString("productName");

        photoSize = getPhotoSize(jsonObject.optString("photoSize", ""));

        mPhotoFormat = jsonObject.optInt("photoForamt", ImageFormat.YUV_420_888);

        mMf_sp = (float)jsonObject.optDouble("mfnr_sp", 0.3f);
        mMf_ce = (float)jsonObject.optDouble("mfnr_ce", 0.3f);
        mMf_fast = jsonObject.optBoolean("mfnr_fast", true);

        mForceMtkMfnr = jsonObject.optBoolean("force_mtk_mfnr", false);

        mFaceLevel = (float)jsonObject.optDouble("mFaceLevel", 0.f);

        mSf_sp = (float)jsonObject.optDouble("sfnr_sp", 0.3f);
        mSf_ce = (float)jsonObject.optDouble("sfnr_ce", 0.3f);

        SHAKE_THRESHOLD = (float)jsonObject.optDouble("shake_threshold", 0.005f);

        mIsDumpFile = jsonObject.optBoolean("dump", false);

        if (mIsDumpFile) {
            new File(DUMP_FILE_PATH).mkdirs();
            new File(DUMP_FILE_DEBLUR_PATH).mkdirs();
        }
        Log.i(TAG, " mProductName : " + mProductName+", mIsDumpFile = "+mIsDumpFile+", mPhotoFormat = "+mPhotoFormat);
    }


    private static Size getPhotoSize(String string) {
        int index = string.indexOf('x');
        if (index == -1)
            return null;
        int width = Integer.parseInt(string.substring(0, index));
        int height = Integer.parseInt(string.substring(index + 1));
        return new Size(width, height);
    }

    public static void startCapture() {
    }

    public static void setDumpFileTitle(String dumpFileTitle) {
        SuperUtil.mDumpFileTitle = dumpFileTitle;
    }

    public static String getDumpFileTitle() {
        return SuperUtil.mDumpFileTitle;
    }

    private static HashMap<String, String> loadCustomizedCameraPreferences(Context mContext, String path) {
        HashMap<String, String> map = new HashMap<String, String>();
        XmlPullParser parser = Xml.newPullParser();
        InputStream is = null;
        String key = null;
        String defaultValue = null;
        try {
            File file = new File(path, CAMERA_OUTSIDE_PREFERENCES_FILE_NAME);
            if (file.exists()) {
                is = new FileInputStream(file);
            } else {
                is = mContext.getAssets().open(CAMERA_OUTSIDE_PREFERENCES_FILE_NAME);
            }
            parser.setInput(is, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        eventType = parser.next();
                        break;

                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("Item")) {

                        } else if (parser.getName().equals("key")) {
                            eventType = parser.next();
                            key = parser.getText();
                        } else if (parser.getName().equals("defaultValue")) {
                            eventType = parser.next();
                            defaultValue = parser.getText();
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("Item")) {
                            map.put(key, defaultValue);
                            key = null;
                            defaultValue = null;
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (FileNotFoundException e) {
            map = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

}
