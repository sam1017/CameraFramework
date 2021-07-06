package com.mediatek.camera.feature.mode.aiworksfacebeauty.glrenderer;


import static com.aiworks.facesdk.AwFaceDetectApi.FACE_DOWN;
import static com.aiworks.facesdk.AwFaceDetectApi.FACE_LEFT;
import static com.aiworks.facesdk.AwFaceDetectApi.FACE_RIGHT;
import static com.aiworks.facesdk.AwFaceDetectApi.FACE_UP;

public class FacePointBeautyTransUtil {

    //预览人脸关键点信息变换 -----> 美颜预览坐标数据
    public static void dupFacePointsFrame(float inPoints[], float outPoints[], int length,
                                     int width, int height,
                                     int faceOrientation, boolean isFrontCamera) {
        if (null == inPoints) {
            return;
        }

        if (isFrontCamera) {
            switch (faceOrientation) {
                case FACE_UP:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = inPoints[i];
                        outPoints[i+1] = inPoints[i + 1];
                    }
                    break;

                case FACE_RIGHT:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = width - inPoints[i + 1];
                        outPoints[i+1] = inPoints[i];
                    }
                    break;

                case FACE_DOWN:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = width - inPoints[i];
                        outPoints[i+1] = height - inPoints[i + 1];
                    }
                    break;

                case FACE_LEFT:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = inPoints[i + 1];
                        outPoints[i+1] = height - inPoints[i];
                    }
                    break;
            }
        } else {
            switch (faceOrientation) {
                case FACE_UP:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = inPoints[i];
                        outPoints[i+1] = inPoints[i + 1];
                    }
                    break;

                case FACE_RIGHT:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = width - inPoints[i + 1];
                        outPoints[i+1] = inPoints[i];
                    }
                    break;

                case FACE_DOWN:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = width - inPoints[i];
                        outPoints[i+1] = height - inPoints[i + 1];
                    }
                    break;

                case FACE_LEFT:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = inPoints[i + 1];
                        outPoints[i+1] = height - inPoints[i];
                    }
                    break;
            }
        }
    }

    //预览人脸关键点信息变换-----> 美颜拍照坐标坐标
    public static void dupFacePointsShot(float inPoints[], float outPoints[], int length,
                                     int width, int height,
                                     int faceOrientation, boolean isFrontCamera) {
        //Log.i("AwFacePointUtil","dupFacePointsShot width = "+width+"  height = "+height+"  faceOrientation ="+faceOrientation);
        if (null == inPoints) {
            return;
        }

        if (isFrontCamera) {
            switch (faceOrientation) {
                case FACE_UP:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = inPoints[i];
                        outPoints[i + 1] = inPoints[i + 1];
                    }
                    break;

                case FACE_RIGHT:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = inPoints[i];
                        outPoints[i+1] = inPoints[i + 1];
                    }
                    break;

                case FACE_DOWN:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = inPoints[i];
                        outPoints[i + 1] = inPoints[i + 1];
                    }
                    break;

                case FACE_LEFT:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = inPoints[i];
                        outPoints[i + 1] = inPoints[i + 1];
                    }
                    break;
            }
        } else {
            switch (faceOrientation) {
                case FACE_UP:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = inPoints[i];
                        outPoints[i + 1] = inPoints[i + 1];
                    }
                    break;

                case FACE_RIGHT:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = inPoints[i];
                        outPoints[i + 1] = inPoints[i + 1];
                    }
                    break;

                case FACE_DOWN:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = inPoints[i];
                        outPoints[i + 1] = inPoints[i + 1];
                    }
                    break;

                case FACE_LEFT:
                    for (int i = 0; i < length; i += 2) {
                        outPoints[i] = inPoints[i];
                        outPoints[i + 1] = inPoints[i + 1];
                    }
                    break;
            }
        }

    }
}
