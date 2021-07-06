package com.mediatek.camera;

import android.content.Context;
import android.os.SystemProperties;
import com.mediatek.camera.R;

//*/ hct.huangfei, 20210104.NullPointerException.
import com.mediatek.camera.common.utils.CameraUtil;
import android.content.Intent;
import android.app.Activity;
import android.provider.MediaStore;
//*/

/**
 * Created by huangfei on 17-8-23.
 */

public class Config {
    private static final String TAG = "Config";
    //*/ hct.huangfei, 20210104.NullPointerException.
    private static final int MTK_CAMERA_APP_VERSION_FOUR = 4;
    private static final int MTK_CAMERA_APP_VERSION_FIVE = 5;
    private static final int MTK_CAMERA_APP_VERSION_SIX = 6;
    //*/
    public static boolean isHctBeautySupported() {
        boolean enable = SystemProperties.getInt("ro.hct.camera_beauty_support", 0) == 1 ? true : false;
	enable = false;
        return enable;  // enable;  true
    }

    public static boolean isHctBokehSupported() {
        boolean enable = SystemProperties.getInt("ro.hct.camera_bokeh_support", 0) == 1 ? true : false;
	enable = false;
        return enable;  // enable;  true
    }

    public static boolean isFrontCameraRotating(Context context) {
        return context.getResources().getBoolean(R.bool.config_is_front_camera_rotating);
    }
    
    public static boolean isWaterMarkSupport(Context context) {
        return context.getResources().getBoolean(R.bool.config_watermark_support);
    }

    //*/ hct.huangfei, 20201210.add volume key function.
    public static boolean isVolumeKeySupport(Context context) {
        return context.getResources().getBoolean(R.bool.config_volume_key_support);
    }
    //*/

    //start, wangsenhao, press preview to take a picture, 2019.06.19
    public static boolean isSingleTapCaptureSupport(Context context) {
        return context.getResources().getBoolean(R.bool.config_single_tap_capture_support);
    }
    //end, wangsenhao, press preview to take a picture, 2019.06.19

    //add by huangfei for mirror start
    public static boolean isMirrorSupport(Context context) {
        return context.getResources().getBoolean(R.bool.config_is_mirror_support);
    }
    //add by huangfei for mirror end
    
    /* hct.wangsenhao, for camera switch @{ */
    public static boolean isTripleSwitchSupport(Context context){
        int mWideId = SystemProperties.getInt("ro.hct_wide_angle_id",-1);
        int mMacroId = SystemProperties.getInt("ro.hct_macroLens_id",-1);
        if(mWideId != -1 || mMacroId !=-1 ){
            return true;
        }
        return false;
    }
    /* }@ hct.wangsenhao */
    
    public static String waterMarkDefaultValue(Context context){
        return context.getResources().getString(R.string.config_watermark_default_value);
    }

    public static boolean isStoragePathSupport(Context context) {
        return context.getResources().getBoolean(R.bool.config_storage_path_support) && !CameraUtil.getDeviceModel().equals("BL5000");
    }

    public static boolean isCameraRatio18_9Support(Context context) {
        //*/ hct.huangfei, 20210105.numbers of picture size.
        if(!CameraUtil.is18_9PreviewSizeSupport(context)){
            return false;
        }
        //*/
        return context.getResources().getBoolean(R.bool.config_camera_ratio_18_9_support);
    }

    public static boolean pictureSizeShowModeSupport(Context context) {
        return context.getResources().getBoolean(R.bool.config_picture_size_show_Mode_support);
    }

    public static boolean isFillLightSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_is_fill_light_support);
    }

    public static String fillLightDefaultValue(Context context){
        return context.getResources().getString(R.string.config_fill_light_default_value);
    }
    public static boolean locationSupport(Context context) {
        return context.getResources().getBoolean(R.bool.config_location_support);
    }

    public static boolean isHdrSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_hdr_support);
    }


    public static boolean isDngSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_dng_support);
    }

    public static boolean isFocusSoundSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_focus_sound_support);
    }

    public static boolean isLongExposureSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_long_exposure_support);
    }

    public static boolean isSlowMotionSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_slow_motion_support);
    }

    //add by huangfei for matrixmode start
    public static boolean isMatrixModeSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_matrixmode_support);
    }
    //add by huangfei for matrixmode ed

    public static boolean isLongScreenSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_long_screen_support);
    }

    public static boolean isContinuousShotNumSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_continuous_shot_num_support);
    }

    public static boolean isCameraSwitchForCustomSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_camera_switcher_for_custom);
    }

    public static boolean isTorchModeSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_torch_mode_support);
    }
    
    public static boolean isZoomRatios8Support(Context context){
        return context.getResources().getBoolean(R.bool.config_zoom_ratios_8x_support);
    }

    public static boolean isSuperNightSupport(Context context){
        boolean enable = SystemProperties.getInt("ro.hct_super_night_support", 0) == 1 ? true : false;
        return enable;
    }
    //start, wangsenhao, vsdof tips, 2019.07.08
    public static boolean isVsdofTipSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_vsdof_tip_support);
    }
    //end, wangsenhao, vsdof tips, 2019.07.08
    
    public static boolean isMFSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_mf_support);
    }

    public static boolean isShutterSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_shutter_support);
    }

    public static boolean isWaterMarkAllCaptureModeSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_watermark_all_capture_mode_support);
    }

    public static String getCurrentPlatForm(){
        return SystemProperties.get("ro.mediatek.platform");
    }

    //add by huangfei for more mode start
    public static int[] getMoreModeList(Context context){
        //bv wuyonglin add for a100 more mode 20210331 start
        if (CameraUtil.getDeviceModel().equals("BL5000")) {
            return context.getResources().getIntArray(R.array.more_mode_item_priority_bl5000);
        }
        //bv wuyonglin add for a100 more mode 20210331 end
        return context.getResources().getIntArray(R.array.more_mode_item_priority);
    }
    //add by huangfei for more mode end

    //add by huangfei for gridlines start    
    public static boolean isGridlinesSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_gridlines_support);
    }    
    //add by huangfei for gridlines end
    
    //add by huang fei disable setting items start
    public static boolean isSceneModeSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_scene_mode_support);
    }
    
    public static boolean isAisSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_ais_support) || CameraUtil.getDeviceModel().equals("BL6000Pro");
    }

    public static boolean isWhiteBalanceSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_white_balance_support);
    }
    public static boolean isZsdOnAndHide(Context context){
        return context.getResources().getBoolean(R.bool.zsd_defalut_on_and_hide);
    }

    public static boolean isIsoSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_iso_support);
    }

    //add by huang fei disable setting items end    

    //add by huangfei for  picture size ratio 1:1 start
    public static boolean isCameraRatio_1_1_Support(Context context) {
        return context.getResources().getBoolean(R.bool.config_camera_ratio_1_1_support);
    }
    //add by huangfei for  picture size ratio 1:1 end
    public static boolean isFrontBokehSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_front_bokeh_support);
    }

    //add by huangfeifor front tripleswitchhorizontal start
    public static boolean isTripleSwitchHorizontalSupport(Context context){
        return context.getResources().getBoolean(R.bool.config_tripleswitch_horizontal_support);
    }
    //add by huangfeifor front tripleswitchhorizontal end

    //adb by huangfei for watermarkThumbnailClick start
    public static boolean  isWatermarkThumbnailClickLimited(Context context){
        return context.getResources().getBoolean(R.bool.config_watermark_thumbnail_click_limited);
    }
    //adb by huangfei for watermarkThumbnailClick end

    //add by huangfei for wide angle start
    public static boolean wideAngleDistortionSupport() {
        boolean enable = SystemProperties.getInt("ro.wide_angle_distortion_support", 0) == 1 ? true : false;
        return enable;  // enable;  true
    }
    //add by huangfei for wide angle end    

    //add by huangfei for QRCodescan in camera start
    public static boolean isQRCodescanInCameraSupport(Context context){
        boolean enableQrScanCode = SystemProperties.getInt("ro.hct_support_scancode", 0) == 1 ? true : false;
        boolean enableQrScanCodeInCamera = context.getResources().getBoolean(R.bool.config_qrcodescan_in_camera_support);
        android.util.Log.i("Config","enableQrScanCode:"+enableQrScanCode+"enableQrScanCodeInCamera:"+enableQrScanCodeInCamera);
        if(enableQrScanCode && enableQrScanCodeInCamera){
            return true;
        }
        return false;
    }
    //add by huangfei for QRCodescan in camera end

    //add by huangfei for zoom switch start
    public static boolean isZoomSwitchSupport(Context context){
        int wideAngleId = SystemProperties.getInt("ro.hct_wide_angle_id",-1);
        boolean isZoomSwitchSupport = context.getResources().getBoolean(R.bool.config_zoom_switch_support);
        android.util.Log.i("Config","wideAngleId:"+wideAngleId+"isZoomSwitchSupport:"+isZoomSwitchSupport);
        if(wideAngleId!=-1 && isZoomSwitchSupport){
            return true;
        }
        return false;
    }

    public static boolean isZoomSlideSwitchSupport(Context context){
        boolean isSupport = false;
        int wideAngleId = SystemProperties.getInt("ro.hct_wide_angle_id",-1);
        isSupport = context.getResources().getBoolean(R.bool.config_zoom_slide_switch_support);
        android.util.Log.i("Config","isZoomSlideSwitchSupport isSupport :"+ isSupport);
        if(wideAngleId!=-1 && isSupport){
            return true;
        }
        return false;
    }

    public static String getWideAngleId(){
        return SystemProperties.get("ro.hct_wide_angle_id","-1");
    }

    //add by huangfei for hct hal watermark start
    public static boolean hctHalWaterMarkSupport() {
        boolean enable = SystemProperties.getInt("ro.hct_hal_watermark_support", 0)== 1;
        return enable;  // enable;  true
    }
    //add by huangfei for hct hal watermark end

    //*/ hct.huangfei, 20210104.NullPointerException.
    public static boolean  isHdrInTopBarSupprot(Activity activity){
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        boolean support = !MediaStore.ACTION_IMAGE_CAPTURE.equals(action)
                && (CameraUtil.getAppVersionLevel() == MTK_CAMERA_APP_VERSION_FOUR
                || CameraUtil.getAppVersionLevel() == MTK_CAMERA_APP_VERSION_FIVE
                || CameraUtil.getAppVersionLevel() == MTK_CAMERA_APP_VERSION_SIX);
        android.util.Log.i(TAG, "[isHdrInTopBarSupprot isSupport] : " + support);
        if(!support){
            return false;
        }
        return activity.getResources().getBoolean(R.bool.config_hdr_in_topbar_support);
    }
    //*/

    //add by huangfei for update thumbnail without exif start
    public static boolean updateThumbnailWithoutExif(Context context){
        return context.getResources().getBoolean(R.bool.config_update_thumbnail_without_exif_support);
    }
    //add by huangfei for update thumbnail without exif end

    //add by huangfei for wideWideAngle and macroLens id are same start
    public static boolean wideAngleAndMacroLensSameId(){
        int wideAngleId = SystemProperties.getInt("ro.hct_wide_angle_id",-1);
        int macroCameraId = SystemProperties.getInt("ro.hct_macroLens_id",-1);
        if(wideAngleId !=-1 && macroCameraId !=-1 && wideAngleId == macroCameraId){
            return true;
        }
        return false;
    }    
    //add by huangfei for wideWideAngle and macroLens id are same end
    
    /* hct.wangsenhao, for mono mode, @{ */
    public static boolean  isMonoModeSupprot(Context context){
        return context.getResources().getBoolean(R.bool.config_mono_mode_support);
    }
    /* }@ hct.wangsenhao */
    /* hct.wangsenhao, for pro mode, @{ */
    public static boolean  isProModeSupprot(Context context){
        return context.getResources().getBoolean(R.bool.config_pro_mode_support);
    }
    /* }@ hct.wangsenhao */
}

