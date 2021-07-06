package com.aiworks.android.camera;

import android.hardware.camera2.CaptureRequest;

import com.aiworks.android.utils.CameraHelper;

public class VendorTagRequest {

    public static final CaptureRequest.Key<Integer> CONTROL_QCOM_AE_METER_MODE = (CaptureRequest.Key<Integer>) CameraHelper.getCaptureRequestKey(
            "org.codeaurora.qcamera3.exposure_metering.exposure_metering_mode", Integer.class);

    public static final CaptureRequest.Key<Integer> CONTROL_QCOM_SATURATION_LEVEL = (CaptureRequest.Key<Integer>) CameraHelper.getCaptureRequestKey(
            "org.codeaurora.qcamera3.saturation.use_saturation", Integer.class);

    public static final CaptureRequest.Key<Integer> CONTROL_QCOM_SHARPNESS_STRENGTH = (CaptureRequest.Key<Integer>) CameraHelper.getCaptureRequestKey(
            "org.codeaurora.qcamera3.sharpness.strength", Integer.class);
    public static final CaptureRequest.Key<Integer> CONTROL_QCOM_CONTRAST_LEVEL = (CaptureRequest.Key<Integer>) CameraHelper.getCaptureRequestKey(
            "org.codeaurora.qcamera3.contrast.level", Integer.class);

    public static final CaptureRequest.Key<int[]> MTK_MFB_MODE = (CaptureRequest.Key<int[]>) CameraHelper.getCaptureRequestKey(
            "com.mediatek.mfnrfeature.mfbmode", int[].class);

    public static final CaptureRequest.Key<byte[]> CONTROL_ZSL_MODE = (CaptureRequest.Key<byte[]>) CameraHelper.getCaptureRequestKey(
            "com.mediatek.control.capture.zsl.mode", byte[].class);

    public static final CaptureRequest.Key<int[]> MTK_FACE_FORCE_3A = (CaptureRequest.Key<int[]>) CameraHelper.getCaptureRequestKey(
            "com.mediatek.facefeature.forceface3a", int[].class);

    public static final CaptureRequest.Key<Integer> CONTROL_QCOM_SELECT_PRIO = (CaptureRequest.Key<Integer>) CameraHelper.getCaptureRequestKey(
            "org.codeaurora.qcamera3.iso_exp_priority.select_priority", Integer.class);

    public static final CaptureRequest.Key<Long> CONTROL_QCOM_ISO_EXP = (CaptureRequest.Key<Long>) CameraHelper.getCaptureRequestKey(
            "org.codeaurora.qcamera3.iso_exp_priority.use_iso_exp_priority", Long.class);

    public static final CaptureRequest.Key<Integer> CONTROL_QCOM_USE_ISO_VALUE = (CaptureRequest.Key<Integer>) CameraHelper.getCaptureRequestKey(
            "org.codeaurora.qcamera3.iso_exp_priority.use_iso_value", Integer.class);

    public static final CaptureRequest.Key<Integer> CONTROL_AWNIGHT_ENABLE = (CaptureRequest.Key<Integer>) CameraHelper.getCaptureRequestKey(
            "com.mediatek.awnight.3rd.party.algo.enable", Integer.class);

    public static final CaptureRequest.Key<float[]> CONTROL_AWNIGHT_PARAMETER = (CaptureRequest.Key<float[]>) CameraHelper.getCaptureRequestKey(
            "com.mediatek.awnight.3rd.party.algo.parameter", float[].class);

    public static CaptureRequest.Key<Byte> QCOM_CUSTOM_NOISE_REDUCTION = (CaptureRequest.Key<Byte>) CameraHelper.getCaptureRequestKey(
            "org.quic.camera.CustomNoiseReduction.CustomNoiseReduction", byte.class);

    public static CaptureRequest.Key<Integer> ZTE_CHI_FUNCTION = (CaptureRequest.Key<Integer>) CameraHelper.getCaptureRequestKey(
            "com.zte.chi.function.enable", Integer.class);

    public static CaptureRequest.Key<Integer> ZTE_CHI_FEATURE = (CaptureRequest.Key<Integer>) CameraHelper.getCaptureRequestKey(
            "com.zte.chi.feature.enable", Integer.class);

}
