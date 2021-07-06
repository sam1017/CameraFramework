package com.mediatek.camera.common.mode.video.device.v2;

import android.util.Log;
import android.util.Size;
import com.aiworks.android.livehdr.JniInterface;
import org.json.JSONException;
import org.json.JSONObject;

public class LiveHDRConfig {
    private static final String TAG = LiveHDRConfig.class.getSimpleName();

    private static String mProductName = null;

    private static int ModelType = JniInterface.TYPE_UNDER_DIRECT;

    public static int Brightness = 90;
    public static int Brightness_Night = 60;
    private static int Saturation = 5;
    private static int Contrast = 0;

    private static int DehazeType = JniInterface.DEHAZE_TYPE_DEFOGGING;
    private static int DehazeLevel = 10;
    private static float DehazeWsat = 0.005f;
    private static float DehazeBsat = 0.00003f;
    private static float DehazeWsat_Night = 0.00001f;
    private static float DehazeBsat_Night = 0.001f;

    public static int backCameraId = 0;
    public static int frontCameraId = 1;
    public static Size photoSize = null;
    public static Size previewSize = null;

    public static int aeMeterMode = -1;
    public static int edgeMode = -1;
    public static int noiseReductionMode = -1;

    public static boolean configData(String configJsonStr) {
        Log.i(TAG, "configData start");
        if (configJsonStr != null && configJsonStr.length() > 0) {
            try {
                JSONObject jsonObject = new JSONObject(configJsonStr);
                jsonOpt(jsonObject);
                Log.i(TAG, "configData end, mProductName = " + mProductName);
                return true;
            } catch (JSONException e) {
                Log.e(TAG, "Cannot parse xml! Please check format." + e.toString());
            }
        } else {
            Log.e(TAG, "xml is null.");
        }
        return false;
    }

    private static void jsonOpt(JSONObject jsonObject) {

        mProductName = jsonObject.optString("productName");

        backCameraId = jsonObject.optInt("backCameraId", backCameraId);
        photoSize = getSize(jsonObject.optString("photoSize", ""));
        previewSize = getSize(jsonObject.optString("previewSize", ""));

        Brightness = jsonObject.optInt("Brightness", Brightness);
        Saturation = jsonObject.optInt("Saturation", Saturation);
        Contrast = jsonObject.optInt("Contrast", Contrast);

        DehazeType = jsonObject.optInt("DehazeType", DehazeType);
        DehazeLevel = jsonObject.optInt("DehazeLevel", DehazeLevel);
        DehazeWsat = (float) jsonObject.optDouble("DehazeWsat", DehazeWsat);
        DehazeBsat = (float) jsonObject.optDouble("DehazeBsat", DehazeBsat);
        DehazeWsat_Night = (float) jsonObject.optDouble("DehazeWsat_Night", DehazeWsat_Night);
        DehazeBsat_Night = (float) jsonObject.optDouble("DehazeBsat_Night", DehazeBsat_Night);

        aeMeterMode = jsonObject.optInt("aeMeterMode", aeMeterMode);
        edgeMode = jsonObject.optInt("edgeMode", edgeMode);
        noiseReductionMode = jsonObject.optInt("noiseReductionMode", noiseReductionMode);
    }

    private static Size getSize(String string) {
        int index = string.indexOf('x');
        if (index == -1 || "".equals(string))
            return null;
        int width = Integer.parseInt(string.substring(0, index));
        int height = Integer.parseInt(string.substring(index + 1));
        return new Size(width, height);
    }

    public static int getModelType() {
        return ModelType;
    }

    public static void setModelType(int type) {
        ModelType = type;
    }

    public static int getBrightness() {
        return ModelType == JniInterface.TYPE_NIGHT ? Brightness_Night : Brightness;
    }

    public static void setBrightness(int brightness) {
        Brightness = brightness;
    }

    public static void setBrightness_Night(int brightness) {
        Brightness_Night = brightness;
    }

    public static int getSaturation() {
        return Saturation;
    }

    public static void setSaturation(int saturation) {
        Saturation = saturation;
    }

    public static int getContrast() {
        return Contrast;
    }

    public static void setContrast(int contrast) {
        Contrast = contrast;
    }

    public static int getDehazeType() {
        return DehazeType;
    }

    public static void setDehazeType(int type) {
        DehazeType = type;
    }

    public static int getDehazeLevel() {
        return DehazeLevel;
    }

    public static void setDehazeLevel(int dehazeLevel) {
        DehazeLevel = dehazeLevel;
    }

    public static int getDehazeScene() {
        return ModelType == JniInterface.TYPE_NIGHT ? 1 : 0;
    }

    public static float getDehazeWsat() {
        return ModelType == JniInterface.TYPE_NIGHT ? DehazeWsat_Night : DehazeWsat;
    }

    public static float getDehazeBsat() {
        return ModelType == JniInterface.TYPE_NIGHT ? DehazeBsat_Night : DehazeBsat;
    }
}
