package com.mediatek.camera.feature.mode.aiworksfacebeauty.util;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Size;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageFormatUtil {

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

    public static int getFormatFromImage(Image image, boolean isOne2One) {
        if (image != null && !isOne2One
                && image.getPlanes()[1].getPixelStride() == 2) { // NV21
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
        //QKDebug.debug("lwr", "getDataFromImage: "+colorFormat+" planes.length: "+planes.length+" data.length: "+data.length);
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

            //QKDebug.debug("lwr", "pixelStride " + pixelStride);
            //QKDebug.debug("lwr", "rowStride " + rowStride);
            //QKDebug.debug("lwr", "buffer size " + buffer.remaining());
            
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            //QKDebug.debug("lwr", "i: " + i+" shift: "+shift);
            //QKDebug.debug("lwr", "width " + w);
            //QKDebug.debug("lwr", "height " + h);
            //QKDebug.debug("lwr", "crop.top: " + crop.top+" crop.left: "+crop.left);
            //QKDebug.debug("lwr", "crop.top: " + top);
            //0,0
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w; //800
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;//400
                    //QKDebug.debug("lwr", " >>>length: " + length);
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                	//QKDebug.debug("lwr", "buffer.position(): " + buffer.position());
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }

    public static byte[] getDataFromImage(ImagePlane[] planes, Rect crop, int format, int colorFormat) {
        if (colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21) {
            throw new IllegalArgumentException("only support COLOR_FormatI420 " + "and COLOR_FormatNV21");
        }

        int width = crop.width();
        int height = crop.height();

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
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
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

    public static void dumpFile(String fileName, byte[] data) {
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(fileName);
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to create output file " + fileName, ioe);
        }
        try {
            outStream.write(data);
            outStream.close();
        } catch (IOException ioe) {
            throw new RuntimeException("failed writing data to file " + fileName, ioe);
        }
    }

    public static void compressToJpeg(String fileName, Image image) {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(fileName);
            Rect rect = image.getCropRect();
            YuvImage yuvImage = new YuvImage(getDataFromImage(image, COLOR_FormatNV21), ImageFormat.NV21, rect.width(), rect.height(), null);
            yuvImage.compressToJpeg(rect, 100, outStream);
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to create output file " + fileName, ioe);
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void dumpYuvDate(byte[] data, String fileName, Size size) {
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, size.getWidth(), size.getHeight(), null);
        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream(new File(fileName));
            yuvImage.compressToJpeg(new Rect(0, 0, size.getWidth(), size.getHeight()), 100, fs);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
