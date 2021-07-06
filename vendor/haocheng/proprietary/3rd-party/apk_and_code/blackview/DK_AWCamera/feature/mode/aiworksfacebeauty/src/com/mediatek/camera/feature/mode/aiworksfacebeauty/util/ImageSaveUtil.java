package com.mediatek.camera.feature.mode.aiworksfacebeauty.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.mediatek.camera.feature.mode.aiworksfacebeauty.util.FaceModelUtil.closeSilently;

public class ImageSaveUtil {
    private static final String TAG = "ImageSaveUtil";
    public static final  String PATH_IMAGES = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "face_beauty1";

    //保存图片到硬盘
    public static String saveFile(Activity activity, byte[] data, String appendFix) {
        if (data == null || data.length <= 0) {
            return "";
        }
        String fileName = System.currentTimeMillis() + appendFix;
        FileOutputStream outputStream = null;
        try {
            File file = new File(PATH_IMAGES);
            if (!file.exists()) {
                file.mkdirs();
            }
            outputStream = new FileOutputStream(PATH_IMAGES + File.separator + fileName);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            bufferedOutputStream.write(data, 0, data.length);

            // 其次把文件插入到系统图库
            try {
                MediaStore.Images.Media.insertImage(activity.getContentResolver(),
                        file.getAbsolutePath(), fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // 最后通知图库更新
            activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + PATH_IMAGES + File.separator + fileName)));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return PATH_IMAGES + File.separator + fileName;
    }

    /**
     * 将Bitmap保存到filePath中
     */
    public static String compressToFile(Activity activity, Bitmap bitmap) {
        if(bitmap == null) return null;
        ByteArrayOutputStream jpgStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, jpgStream);

        byte[] data = jpgStream.toByteArray();
        String fileName = System.currentTimeMillis()+".jpg";
        FileOutputStream out = null;
        try {
            String path = PATH_IMAGES + File.separator + fileName;
            out = new FileOutputStream(path);
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.write(data);
            // 其次把文件插入到系统图库
            /*try {
                MediaStore.Images.Media.insertImage(activity.getContentResolver(),
                        path, fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + PATH_IMAGES + File.separator + fileName)));*/

        } catch (Exception e) {
            Log.w(TAG, "compressToFile e:"+e);
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return PATH_IMAGES + File.separator + fileName;
    }

    public static String compressToFile1(Activity activity, Bitmap bitmap) {
        if(bitmap == null) return null;
        ByteArrayOutputStream jpgStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, jpgStream);

        byte[] data = jpgStream.toByteArray();
        String fileName = System.currentTimeMillis()+"th.jpg";
        FileOutputStream out = null;
        try {
            String path = PATH_IMAGES + File.separator + fileName;
            out = new FileOutputStream(path);
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.write(data);
            // 其次把文件插入到系统图库
            /*try {
                MediaStore.Images.Media.insertImage(activity.getContentResolver(),
                        path, fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + PATH_IMAGES + File.separator + fileName)));*/

        } catch (Exception e) {
            Log.w(TAG, "compressToFile e:"+e);
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return PATH_IMAGES + File.separator + fileName;
    }

    public static String compressToFile2(Activity activity, Bitmap bitmap) {
        if(bitmap == null) return null;
        ByteArrayOutputStream jpgStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, jpgStream);

        byte[] data = jpgStream.toByteArray();
        String fileName = System.currentTimeMillis()+"th2.jpg";
        FileOutputStream out = null;
        try {
            String path = PATH_IMAGES + File.separator + fileName;
            out = new FileOutputStream(path);
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.write(data);
            // 其次把文件插入到系统图库
            /*try {
                MediaStore.Images.Media.insertImage(activity.getContentResolver(),
                        path, fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + PATH_IMAGES + File.separator + fileName)));*/

        } catch (Exception e) {
            Log.w(TAG, "compressToFile e:"+e);
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return PATH_IMAGES + File.separator + fileName;
    }

    public static long writeFileToPath(String path, byte[] data) {
        if (data == null || data.length <= 0) {
            return 0;
        }

        FileOutputStream out = null;
        boolean delFlag = false;
        try {
            out = new FileOutputStream(path);
            out.write(data);
            return data.length;
        } catch (Exception e) {
            delFlag = true;
            Log.e(TAG, "writeFileToPath Failed to write data " + e.toString());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }

                if (delFlag) {
                    File file = new File(path);
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            } catch (Exception e) {
            }
        }
        return 0;
    }

    public static void saveNV21ToJpegFile(String fileName, byte[] nv21data, int width, int height, int quality) {
        File pictureFile = new File(fileName);
        FileOutputStream filecon = null;
        try {
            filecon = new FileOutputStream(pictureFile);
            YuvImage image = new YuvImage(nv21data, ImageFormat.NV21, width, height, null);
            Rect rect =  new Rect(0, 0, image.getWidth(), image.getHeight());
            image.compressToJpeg(
                    rect,
                    quality, filecon);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSilently(filecon);
        }
    }
}
