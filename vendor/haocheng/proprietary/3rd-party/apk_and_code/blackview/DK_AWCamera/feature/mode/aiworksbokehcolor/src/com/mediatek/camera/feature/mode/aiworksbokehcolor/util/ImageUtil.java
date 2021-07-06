package com.mediatek.camera.feature.mode.aiworksbokehcolor.util;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ExifInterface;
import android.media.Image;
import android.os.Build;

import com.android.ex.camera2.portability.Size;
//import com.aiworks.yuvUtil.YuvEncodeJni;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageUtil {

    public static final int COLOR_FormatI420 = 1;
    public static final int COLOR_FormatNV21 = 2;

    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
        case ImageFormat.YUV_420_888:
        case ImageFormat.NV21:
        case ImageFormat.YV12:
            return true;
        }
        return false;
    }

    public static int getFormatFromImage(Image image) {
        if (image.getPlanes()[1].getPixelStride() == 2) {
            return ImageFormat.NV21;
        }
        return ImageFormat.YUV_420_888;
    }

    public static byte[] getDataFromImageShot(Image image) {
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();
        byte[] data, yBytes, uBytes, vBytes;
        yBytes = new byte[yBuffer.remaining()];
        vBytes = new byte[vBuffer.remaining()];
        yBuffer.get(yBytes);
        vBuffer.get(vBytes);
        if (image.getPlanes()[1].getPixelStride() == 2) { // NV21
            data = new byte[yBytes.length + vBytes.length];
            System.arraycopy(yBytes, 0, data, 0, yBytes.length);
            System.arraycopy(vBytes, 0, data, yBytes.length, vBytes.length);
        } else { // I420
            uBytes = new byte[uBuffer.remaining()];
            uBuffer.get(uBytes);
            data = new byte[yBytes.length + uBytes.length + vBytes.length];
            System.arraycopy(yBytes, 0, data, 0, yBytes.length);
            System.arraycopy(uBytes, 0, data, yBytes.length, uBytes.length);
            System.arraycopy(vBytes, 0, data, yBytes.length + uBytes.length, vBytes.length);
        }
        return data;
    }

    public static byte[] getDataFromImage(Image image, int colorFormat) {
        if (colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21) {
            throw new IllegalArgumentException("only support COLOR_FormatI420 " + "and COLOR_FormatNV21");
        }
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
            case 0:
                channelOffset = 0;
                outputStride = 1;
                break;
            case 1:
                if (colorFormat == COLOR_FormatI420) {
                    channelOffset = width * height;
                    outputStride = 1;
                } else if (colorFormat == COLOR_FormatNV21) {
                    channelOffset = width * height + 1;
                    outputStride = 2;
                }
                break;
            case 2:
                if (colorFormat == COLOR_FormatI420) {
                    channelOffset = (int) (width * height * 1.25);
                    outputStride = 1;
                } else if (colorFormat == COLOR_FormatNV21) {
                    channelOffset = width * height;
                    outputStride = 2;
                }
                break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;

            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w; //800
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;//400
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }

    public static void compressToJpeg(String fileName, Image image) {
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(fileName);
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to create output file " + fileName, ioe);
        }
        Rect rect = image.getCropRect();
        YuvImage yuvImage = new YuvImage(getDataFromImage(image, COLOR_FormatNV21), ImageFormat.NV21, rect.width(), rect.height(), null);
        yuvImage.compressToJpeg(rect, 100, outStream);
    }

    /*public static Bitmap getBitmapFromImage(Image image, int jpgRotate) {
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();
        byte[] yBytes, uBytes = null, vBytes;
        yBytes = new byte[yBuffer.remaining()];
        vBytes = new byte[vBuffer.remaining()];
        yBuffer.get(yBytes);
        vBuffer.get(vBytes);
        int format;
        if (image.getPlanes()[1].getPixelStride() == 2) {
            format = android.graphics.ImageFormat.NV21;
        } else {
            format = android.graphics.ImageFormat.YUV_420_888;
            uBytes = new byte[uBuffer.remaining()];
            uBuffer.get(uBytes);
        }
        Bitmap bmp = Bitmap.createBitmap(jpgRotate % 180 == 0 ? image.getWidth() : image.getHeight(),
                jpgRotate % 180 == 0 ? image.getHeight() : image.getWidth(),
                Bitmap.Config.ARGB_8888);
        YuvEncodeJni.getInstance().Yuv2Rgb(bmp, yBytes, uBytes, vBytes, format, image.getWidth(), image.getHeight(),
                image.getPlanes()[0].getRowStride(), image.getPlanes()[1].getRowStride(), jpgRotate);
        return bmp;
    }*/

    public static Bitmap createSourceBitmap(byte[] jpegData, int inSampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = inSampleSize;
        options.inMutable = true;
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
        } catch (OutOfMemoryError e) {
            bitmap = null;
            System.gc();
        }
        int degree = getJpegOrientation(jpegData);
        if (degree != 0) {
            bitmap = rotateBitmapByDegree(bitmap, degree);
        }
        return bitmap;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static int getJpegOrientation(byte[] jpegData) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(new ByteArrayInputStream(jpegData));
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static Size getJpegSize(byte[] jpegData) {
        try {
            ExifInterface exifInterface = new ExifInterface(new ByteArrayInputStream(jpegData));
            int imageWidth = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
            int imageLength = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
            return new Size(imageWidth, imageLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    public static Bitmap getMirrorBitmap(Bitmap bmp) {
        Bitmap mirrorBmp;
        final Matrix m = new Matrix();
        m.preScale(-1, 1);
        try {
            mirrorBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                    bmp.getHeight(), m, false);
        } catch (OutOfMemoryError e) {
            return bmp;
        }
        bmp.recycle();
        return mirrorBmp;
    }

}
