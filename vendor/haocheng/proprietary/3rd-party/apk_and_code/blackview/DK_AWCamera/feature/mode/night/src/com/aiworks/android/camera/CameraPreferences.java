package com.aiworks.android.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.Xml;

import com.aiworks.android.utils.Product;
import com.mediatek.camera.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static android.hardware.camera2.CaptureRequest.CONTROL_MODE;

public class CameraPreferences {

    private static final String TAG = CameraPreferences.class.getSimpleName();

    public static final String KEY_NIGHT_CAMERA_ID = "night_camera_id_key";
    public static final String KEY_NIGHT_CAMERA_PHOTOSIZE = "night_camera_photoSize_key";
    public static final String KEY_NIGHT_CAMERA_PHOTOFORAMT = "night_camera_photoForamt_key";
    public static final String KEY_NIGHT_CAMERA_OPERATINGMODE = "night_camera_operatingMode";
    public static final String KEY_TEXTUREVIEW_ENABLE = "TextureView_enable_key";
    public static final String KEY_PREVIEWREADER_ENABLE = "PreviewReader_enable_key";
    public static final String KEY_NIGHT_CAMERA_AEMETERMODE = "night_camera_aeMeterMode_key";
    public static final String KEY_NIGHT_CAMERA_SCENEMODE = "night_camera_sceneMode_key";

    public static CameraCharacteristics cameraCharacteristics;
    public static Range<Long> exposureTimeRange;
    public static Range<Integer> sensorSensitivityRange;
    public static Range<Integer> dGainRange;
    public static Rect cropRectangle;

    public static int cameraId = 0;
    public static int operatingMode = 0x8000;
    public static Size photoSize = null;
    public static int photoForamt = ImageFormat.YUV_420_888;
    public static boolean usePreviewReader = false;
    public static boolean useTextureView = true;
    public static int aeMeterMode = -1;
    public static int sceneMode = -1;

    public static void parseCameraPreferencesXml(Context context, String path, String fileName) {

        HashMap<String, String> customPreferences = loadCustomizedCameraPreferences(
                context, path, fileName);
        if(customPreferences == null || customPreferences.size() == 0){
            return;
        }
        if (customPreferences.get(KEY_NIGHT_CAMERA_ID) != null) {
            cameraId = Integer.parseInt(customPreferences.get(KEY_NIGHT_CAMERA_ID));
        }
        if (customPreferences.get(KEY_NIGHT_CAMERA_OPERATINGMODE) != null) {
            operatingMode = Integer.parseInt(customPreferences.get(KEY_NIGHT_CAMERA_OPERATINGMODE).replace("0x", ""), 16);
        }
        if (customPreferences.get(KEY_NIGHT_CAMERA_PHOTOSIZE) != null) {
            photoSize = getPhotoSize(customPreferences.get(KEY_NIGHT_CAMERA_PHOTOSIZE));
        }
        if (customPreferences.get(KEY_NIGHT_CAMERA_PHOTOFORAMT) != null) {
            photoForamt = Integer.parseInt(customPreferences.get(KEY_NIGHT_CAMERA_PHOTOFORAMT));
        }
        if (customPreferences.get(KEY_TEXTUREVIEW_ENABLE) != null) {
            useTextureView = "on".equalsIgnoreCase(customPreferences.get(KEY_TEXTUREVIEW_ENABLE));
        }
        if (customPreferences.get(KEY_PREVIEWREADER_ENABLE) != null) {
            usePreviewReader = "on".equalsIgnoreCase(customPreferences.get(KEY_PREVIEWREADER_ENABLE));
        }
        if (customPreferences.get(KEY_NIGHT_CAMERA_AEMETERMODE) != null) {
            aeMeterMode = Integer.parseInt(customPreferences.get(KEY_NIGHT_CAMERA_AEMETERMODE));
        }
        if (customPreferences.get(KEY_NIGHT_CAMERA_SCENEMODE) != null) {
            sceneMode = Integer.parseInt(customPreferences.get(KEY_NIGHT_CAMERA_SCENEMODE));
        }
    }

    public static HashMap<String, String> loadCustomizedCameraPreferences(Context mContext, String path, String fileName) {
        HashMap<String, String> map = new HashMap<String, String>();
        XmlPullParser parser = Xml.newPullParser();
        InputStream is = null;
        String key = null;
        String defaultValue = null;
        try {
            File file = new File(path, fileName);
            if (file.exists()) {
                is = new FileInputStream(file);
            } else {
                is = mContext.getAssets().open(fileName);
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

    private static Size getPhotoSize(String string) {
        int index = string.indexOf('x');
        if (index == -1)
            return null;
        int width = Integer.parseInt(string.substring(0, index));
        int height = Integer.parseInt(string.substring(index + 1));
        return new Size(width, height);
    }

    public static void setCameraCharacteristics(CameraCharacteristics characteristics) {
        cameraCharacteristics = characteristics;
        cropRectangle = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        exposureTimeRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
        sensorSensitivityRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
        dGainRange = characteristics.get(CameraCharacteristics.CONTROL_POST_RAW_SENSITIVITY_BOOST_RANGE);
        Log.w(TAG, "setCameraCharacteristics mCropRectangle = " + cropRectangle + ", mSensitivityRange = " + sensorSensitivityRange);
    }

    private static class CameraSizeComparator implements Comparator {
        String sort_type;

        public CameraSizeComparator(String mode) {
            sort_type = mode;
        }

        @Override
        public int compare(Object arg0, Object arg1) {
            Size size0 = (Size) arg0;
            Size size1 = (Size) arg1;
            if (sort_type.equals("HEIGHT")) {
                return size1.getHeight() - size0.getHeight();
            } else {
                return size1.getWidth() - size0.getWidth();
            }
        }
    }

    private static float[] mSelectableAspects = null;

    public static void setSelectableAspects(float[] aspects) {
        mSelectableAspects = aspects;
    }

    public static List<Size> getHighResolutionPictureSizeList(int imageFormat, Size defaultValue) {
        StreamConfigurationMap config = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (config == null) {
            return null;
        }
        List<Size> size_list;
        List<Size> size_list1 = null;
        if (isSupportedBurst()) {
            size_list1 = Arrays.asList(config.getHighResolutionOutputSizes(imageFormat));
        }
        List<Size> size_list2 = Arrays.asList(config.getOutputSizes(imageFormat));
        if (size_list1 == null) {
            size_list = size_list2;
        } else {
            size_list = new ArrayList<>();
            for (Size size : size_list1) {
                size_list.add(size);
            }
            for (Size size : size_list2) {
                size_list.add(size);
            }
        }
        if (defaultValue != null) {
            boolean flag = false;
            for (Size size : size_list) {
                if (size.getWidth() == defaultValue.getWidth() && size.getHeight() == defaultValue.getHeight()) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                size_list.add(defaultValue);
            }
        }
        if (mSelectableAspects != null) {
            size_list = convUseSizeList(size_list, mSelectableAspects);
        }
        return size_list;
    }

    public static Size getPictureSize(SharedPreferences sp, Context context) {
        List<Size> supportedPhotoSizes = getHighResolutionPictureSizeList(photoForamt, photoSize);
        Size[] supportedSizes = supportedPhotoSizes.toArray(new Size[supportedPhotoSizes.size()]);
        int index = Integer.parseInt(sp.getString(context.getString(R.string.preference_key_picture_size), "-1"));
        for (int i = 0; i < supportedSizes.length; i++) {
            Size size = supportedSizes[i];
            Log.d(TAG, "supportedPhotoSizes:" + size);
            if (index == -1 && photoSize != null && size.getWidth() == photoSize.getWidth() && size.getHeight() == photoSize.getHeight()) {
                index = i;
            }
        }
        if (0 <= index && index < supportedSizes.length) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(context.getString(R.string.preference_key_picture_size), index + "");
            editor.apply();
            return supportedSizes[index];
        } else {
            return supportedSizes[0];
        }
    }

    public static boolean isSupportedBurst() {
        if (null == cameraCharacteristics) {
            return false;
        }
        int[] keys = cameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
        if (keys == null) {
            return false;
        }
        for (int key : keys) {
            if (key == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<Size> convUseSizeList(List<Size> size_list, float[] aspect_list) {
        ArrayList<Size> use_size_list = new ArrayList<>();
        for (float anAspect_list : aspect_list) {
            use_size_list.addAll(convUseSizeList(size_list, anAspect_list, anAspect_list));
        }
        Collections.sort(use_size_list, new CameraSizeComparator("WIDTH"));
        return use_size_list;
    }

    private static ArrayList<Size> convUseSizeList(List<Size> size_list, float min_aspect, float max_aspect) {
        ArrayList<Size> use_size_list = new ArrayList<>();
        Size size;
        for (int i = 0; i < size_list.size(); i++) {
            size = size_list.get(i);
            if (isSelectableSize(size.getWidth(), size.getHeight(), min_aspect, max_aspect)) {
                use_size_list.add(size);
            }
        }
        return use_size_list;
    }

    private static final float ACCEPTABLE_ASPECT_RANGE = 0.01f;

    private static boolean isSelectableSize(int preview_w, int preview_h, float min_aspect, float max_aspect) {
        float src_aspect = (float) preview_h / preview_w;
        return (src_aspect >= (min_aspect - ACCEPTABLE_ASPECT_RANGE) && src_aspect <= (max_aspect + ACCEPTABLE_ASPECT_RANGE));
    }

    public static CaptureRequest.Builder createPreviewRequest(CameraDevice mCameraDevice) {
        try {
            CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AWB_LOCK, false);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, false);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                previewRequestBuilder.set(CaptureRequest.CONTROL_ENABLE_ZSL, false);
            }

            if (Product.mPlatformID == Product.HARDWARE_PLATFORM_MTK) {
                if ("mt6779".equalsIgnoreCase(Product.getHardwarePlatform())) {
                    previewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF);
                    previewRequestBuilder.set(VendorTagRequest.MTK_FACE_FORCE_3A, VendorTagMetadata.MTK_FACE_FORCE_3A_OFF);
                } else {
                    previewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE);
                    previewRequestBuilder.set(VendorTagRequest.MTK_FACE_FORCE_3A, VendorTagMetadata.MTK_FACE_FORCE_3A_ON);
                }
                previewRequestBuilder.set(VendorTagRequest.MTK_MFB_MODE, VendorTagMetadata.MTK_MFB_MODE_AUTO);
            } else if (Product.mPlatformID == Product.HARDWARE_PLATFORM_QCOM) {
                previewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE);
                if (CameraPreferences.aeMeterMode != -1) {
                    previewRequestBuilder.set(VendorTagRequest.CONTROL_QCOM_AE_METER_MODE, CameraPreferences.aeMeterMode);
                }
            }

            if (CameraPreferences.sceneMode != -1) {
                previewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraPreferences.sceneMode);
                if (CameraPreferences.sceneMode != 0) {
                    previewRequestBuilder.set(CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE);
                }
            }
//            dumpCaptureRequest(previewRequestBuilder);
            return previewRequestBuilder;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void dumpCaptureRequest(CaptureRequest.Builder captureBuilder) {
        CaptureRequest initialRequest = captureBuilder.build();
        for (CaptureRequest.Key key : initialRequest.getKeys()) {
            if (initialRequest.get(key) != null) {
                if ("[I".equalsIgnoreCase(initialRequest.get(key).getClass().getName())) {
                    for (int v : (int[]) initialRequest.get(key)) {
                        Log.e("initialRequest", key.getName() + ",    value = " + v);
                    }
                } else if ("[B".equalsIgnoreCase(initialRequest.get(key).getClass().getName())) {
                    for (byte v : (byte[]) initialRequest.get(key)) {
                        Log.e("initialRequest", key.getName() + ",    value = " + v);
                    }
                } else {
                    Log.e("initialRequest", key.getName() + "  " + initialRequest.get(key));
                }
            }
        }
    }

}
