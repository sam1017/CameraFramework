package com.aiworks.android.deblur;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import com.aiworks.android.Super.SuperUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShakeUtil {

    public static String mDumpFileTitle;

    private static byte[] currentPreviewData;
    private static int currentPreviewDataWidth;
    private static int currentPreviewDataHeight;
    private static byte[] previewData;
    private static int previewDataWidth;
    private static int previewDataHeight;

    public static void startCapture() {
        currentPreviewData = previewData;
        currentPreviewDataWidth = previewDataWidth;
        currentPreviewDataHeight = previewDataHeight;
    }

    public static void setDumpFileTitle(String dumpFileTitle) {
        ShakeUtil.mDumpFileTitle = dumpFileTitle;
    }

    public static String getDumpFileTitle() {
        return ShakeUtil.mDumpFileTitle;
    }

//    public static void nightHist(byte[] nv21, int width, int height) {
//        previewData = nv21;
//        previewDataWidth = width;
//        previewDataHeight = height;
//    }

    public static void dumpPreviewData() {
        if (currentPreviewData != null && currentPreviewDataWidth != 0 && currentPreviewDataHeight != 0) {
            YuvImage yuvImage = new YuvImage(currentPreviewData, ImageFormat.NV21, currentPreviewDataWidth, currentPreviewDataHeight, null);
            FileOutputStream fs = null;
            try {
                fs = new FileOutputStream(new File(SuperUtil.DUMP_FILE_DEBLUR_PATH + ShakeUtil.mDumpFileTitle + "_preview.jpg"));
                yuvImage.compressToJpeg(new Rect(0, 0, currentPreviewDataWidth, currentPreviewDataHeight), 100, fs);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fs != null) {
                        fs.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
