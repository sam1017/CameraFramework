package com.mediatek.camera.feature.mode.aiworksfacebeauty.util;


import static com.aiworks.facesdk.AwFaceDetectApi.FACE_DOWN;
import static com.aiworks.facesdk.AwFaceDetectApi.FACE_LEFT;
import static com.aiworks.facesdk.AwFaceDetectApi.FACE_RIGHT;
import static com.aiworks.facesdk.AwFaceDetectApi.FACE_UP;
import static com.mediatek.camera.feature.mode.aiworksfacebeauty.util.Accelerometer.Deg0;
import static com.mediatek.camera.feature.mode.aiworksfacebeauty.util.Accelerometer.Deg180;
import static com.mediatek.camera.feature.mode.aiworksfacebeauty.util.Accelerometer.Deg270;
import static com.mediatek.camera.feature.mode.aiworksfacebeauty.util.Accelerometer.Deg90;


public class FaceOrientationUtil {

    public static int dupFaceOrientation(int sensorOrientation, boolean isFrontCamera) {
        int faceOrientation = FACE_RIGHT;
        if (isFrontCamera) {
            switch (sensorOrientation) {
                case Deg0:
                    faceOrientation = FACE_RIGHT;
                    break;
                case Deg90:
                    faceOrientation = FACE_DOWN;
                    break;
                case Deg180:
                    faceOrientation = FACE_LEFT;
                    break;
                case Deg270:
                    faceOrientation = FACE_UP;
                    break;
            }

        } else {
            switch (sensorOrientation) {
                case Deg0:
                    faceOrientation = FACE_LEFT;
                    break;
                case Deg90:
                    faceOrientation = FACE_DOWN;
                    break;
                case Deg180:
                    faceOrientation = FACE_RIGHT;
                    break;
                case Deg270:
                    faceOrientation = FACE_UP;
                    break;
            }
        }

        return faceOrientation;
    }


    public static int dupFaceOrientation(int jpegRotation) {

        int faceOrientation = FACE_RIGHT;
        switch (jpegRotation) {
            case Deg0:
                faceOrientation = FACE_UP;
                break;
            case Deg90:
                faceOrientation = FACE_LEFT;
                break;
            case Deg180:
                faceOrientation = FACE_DOWN;
                break;
            case Deg270:
                faceOrientation = FACE_RIGHT;
                break;
        }

        return faceOrientation;
    }


    public static void dupSoftFacePointsFrame(float inPoints[], float outPoints[], int length,
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

}
