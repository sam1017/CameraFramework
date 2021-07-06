package com.mediatek.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.mediatek.camera.common.exif.ExifInterface;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
//bv wuyonglin add for pro mode adpte A80Pro water logo 20200615 start
import com.mediatek.camera.common.utils.CameraUtil;
//bv wuyonglin add for pro mode adpte A80Pro water logo 20200615 end

/**
 * @author young on 19-1-24.
 */

public class WaterMarkUtil {

    private static final Tag TAG = new Tag(WaterMarkUtil.class.getSimpleName());
    private static boolean mRotation;
    private static byte[] yuvWaterMarkData;
    private static Bitmap mDefaultBitmap = null;
    private static Bitmap mBitmap = null;
    private static int yuvWaterMarkWidth;
    private static int yuvWaterMarkHeight;
    public static WaterMarkStatusListener mListener;

    //bv zhangjiachu add for add font type 20200823
    public static final Typeface OPPO_SANS_R = Typeface.create("oppo-sans-r", Typeface.NORMAL);
    public static final Typeface OPPO_SANS_M = Typeface.create("oppo-sans-m", Typeface.NORMAL);
    public static final Typeface OPPO_SANS_L = Typeface.create("oppo-sans-l", Typeface.NORMAL);

    public static void inityuvWaterMark(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        int mScreenWidth = dm.widthPixels;
        LogHelper.i(TAG,"mScreenWidth = " + mScreenWidth);

        if (CameraUtil.getDeviceModel().equals("A100")) {
            LogHelper.i(TAG,"inityuvWaterMark A100");
            mDefaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.water_mark_logo_a100);
            mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.water_mark_logo_a100);
        } else if(CameraUtil.getDeviceModel().equals("BL6000Pro")){
            LogHelper.i(TAG,"inityuvWaterMark BL6000Pro");
            mDefaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.water_mark_logo_bl6000pro);
            mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.water_mark_logo_bl6000pro);
/*            mDefaultBitmap = getDefaultBitmap((mScreenWidth), "BL6000PRO");
            mBitmap = getDefaultBitmap((mScreenWidth), "BL6000PRO");*/
        } else if(CameraUtil.getDeviceModel().equals("BL5000")){
            LogHelper.i(TAG,"inityuvWaterMark BL5000");
            mDefaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.water_mark_logo_bl5000);
            mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.water_mark_logo_bl5000);
        } else {
            LogHelper.i(TAG,"inityuvWaterMark water_mark_logo");
            mDefaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.water_mark_logo);
            mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.water_mark_logo);
        }
        //yuvWaterMarkData = getYUVByBitmap(picBitmap);
    }

    public static Bitmap getDefaultBitmap(int width, String messege){
        int height = (int)(width * 0.20);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        Paint paintshadow = new Paint();

        Rect rect = new Rect(0, 0, width, height);
        LogHelper.i(TAG,"getDefaultBitmap widht = " + width + " height = " + height);

/*        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.GRAY);
        canvas.drawRect(rect, bgPaint);*/

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize((int)(height*0.345));
        paint.setTypeface(OPPO_SANS_M);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        paint.setLetterSpacing(0.24f);
        paint.setTextAlign(Paint.Align.LEFT);

        paintshadow.setColor(0xaa454545);
        paintshadow.setStyle(Paint.Style.FILL_AND_STROKE);
        paintshadow.setTextSize((int)(height*0.345));
        paintshadow.setTypeface(OPPO_SANS_M);
        paintshadow.setLetterSpacing(0.24f);
        paintshadow.setTextAlign(Paint.Align.LEFT);

        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;

        Rect rect1 = new Rect(0, 0, width, height/2);
        Rect rect1_shadow = new Rect(2, 2, width + 2, height/2 + 2);
        Rect rect2 = new Rect(0, height/2 - 8 , width , height - 8);
        Rect rect2_shadow = new Rect(2, height/2 - 8 + 2, width + 2, height - 8 + 2);
        LogHelper.i(TAG,"rect1 = " + rect1 + " rect2 = " + rect2);
        int baseLineY = (int)(rect1_shadow.centerY() - top/2 - bottom/2);
        canvas.drawText(messege,rect1.left,baseLineY,paintshadow);
        baseLineY = (int)(rect2_shadow.centerY() - top/2 - bottom/2);
        canvas.drawText("BLACKVIEW",rect2.left,baseLineY,paintshadow);
        baseLineY = (int)(rect1.centerY() - top/2 - bottom/2);
        canvas.drawText(messege,rect1.left,baseLineY,paint);
        baseLineY = (int)(rect2.centerY() - top/2 - bottom/2);
        canvas.drawText("BLACKVIEW",rect2.left,baseLineY,paint);

        return bitmap;
    }

    public static byte[] getYUVByBitmap(Context context, Bitmap bitmap, int photowidth) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float mScaleValue_percent = Float.parseFloat(context.getResources().getString(R.string.water_mark_scale_percent));
	    if (CameraUtil.getDeviceModel().equals("A100")) {
            mScaleValue_percent = Float.parseFloat(context.getResources().getString(R.string.water_mark_scale_percent_a100));
        }

        int newwidth = (int)(photowidth*mScaleValue_percent/100.0);
        int newheight = newwidth * height / width;

        LogHelper.i(TAG,"getYUVByBitmap photowidth = " + photowidth + " mScaleValue_percent = " + mScaleValue_percent);
        LogHelper.i(TAG,"getYUVByBitmap width = " + width + " height = " + height);
        LogHelper.i(TAG,"getYUVByBitmap newwidth = " + newwidth + " newheight = " + newheight);

        if(width != newwidth){
            if (mBitmap != null & !mBitmap.isRecycled())
            {
                mBitmap.recycle();
                mBitmap = null;
            }
            mBitmap = scaleImage(mDefaultBitmap, newwidth, newheight);
        }
        yuvWaterMarkWidth = newwidth;
        yuvWaterMarkHeight = newheight;
        int size = newwidth * newheight;
        LogHelper.i(TAG,"getYUVByBitmap size = " + size);
        int pixels[] = new int[size];
        mBitmap.getPixels(pixels, 0, newwidth, 0, 0, newwidth, newheight);
        byte[] data = rgb2YCbCr420(pixels, newwidth, newheight);

        return data;
    }

    //缩小图片到制定长宽
    public static Bitmap scaleImage(Bitmap bm, int newWidth, int newHeight)
    {
        if (bm == null)
        {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
        return newbm;
    }

    public static byte[] rgb2YCbCr420(int[] pixels, int width, int height) {
        int len = width * height;
        byte[] yuv = new byte[len * 3 / 2];
        int y, u, v;
        LogHelper.i(TAG,"rgb2YCbCr420 pixels.size = " + pixels.length + " len = " + len + " yuv.size = " + yuv.length);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = pixels[i * width + j] & 0x00FFFFFF;
                int r = rgb & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb >> 16) & 0xFF;
                y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;

                y = y < 16 ? 16 : (y > 255 ? 255 : y);
                u = u < 0 ? 0 : (u > 255 ? 255 : u);
                v = v < 0 ? 0 : (v > 255 ? 255 : v);
                yuv[i * width + j] = (byte) y;
                //yuv[len + (i >> 1) * width + (j & ~1) + 0] = (byte) u;
                //yuv[len + + (i >> 1) * width + (j & ~1) + 1] = (byte) v;
            }
        }
        return yuv;
    }

    public static byte[] yuvAddWaterMark(Context context, byte[] yuvData, int yuvW, int yuvH, int rotato){
        float mTranslateX_percent = Float.parseFloat(context.getResources().getString(R.string.water_mark_translate_x_percent));
        float mTranslateY_percent = Float.parseFloat(context.getResources().getString(R.string.water_mark_translate_y_percent));
        int minW = Math.min(yuvW, yuvH);
        int Translate_X = (int)(minW * mTranslateX_percent / 100.0f);
        int Translate_Y = (int)(minW * mTranslateY_percent / 100.0f);
        int scalewidth = 0;

        LogHelper.i(TAG,"minW = " + minW + " Translate_X = " + Translate_X + " Translate_Y = " + Translate_Y);
        if(yuvW > yuvH){
            scalewidth = yuvH;
        }else{
            scalewidth = yuvW;
        }

        LogHelper.i(TAG,"yuvAddWaterMark yuvW = " + yuvW + " yuvH = " + yuvH);
        yuvWaterMarkData = getYUVByBitmap(context, mBitmap, scalewidth);

        long time = System.currentTimeMillis();
        LogHelper.i(TAG,"minW = " + minW + " yuvWaterMarkWidth = " + yuvWaterMarkWidth + " yuvWaterMarkHeight = " + yuvWaterMarkHeight);
        if(rotato%360 == 0) {
            LogHelper.i(TAG,"yuvAddWaterMark Translate_X = " + Translate_X + " Translate_Y = " + Translate_Y + " rotato = " + rotato);
            Translate_Y = yuvH - yuvWaterMarkHeight - (int)(minW * mTranslateX_percent / 100.0f);
            Translate_X = (int)(minW * mTranslateY_percent / 100.0f);
            LogHelper.i(TAG,"yuvAddWaterMark 2 Translate_X = " + Translate_X + " Translate_Y = " + Translate_Y);
            for(int i = 0; i < yuvWaterMarkHeight; i++ ){
                for ( int j = 0; j < yuvWaterMarkWidth; j ++ ){
                    byte tempY = yuvWaterMarkData[i*yuvWaterMarkWidth+j];
                    //byte tempU = yuvWaterMarkData[yuvWaterMarkWidth*yuvWaterMarkHeight + (i >> 1) * yuvWaterMarkWidth + (j & ~1)];
                    //byte tempV = yuvWaterMarkData[yuvWaterMarkWidth*yuvWaterMarkHeight+ + (i >> 1) * yuvWaterMarkWidth + (j & ~1) + 1];
                    if( tempY!= 0x10 && tempY!= 0x80 && tempY!= 0xeb){
                        if(tempY == -21){
                            yuvData[(Translate_Y + i)*yuvW + Translate_X + j] = (byte)(255);
                        }else{
                            yuvData[(Translate_Y + i)*yuvW + Translate_X + j] = tempY;
                        }
                        //yuvData[yuvW*yuvH + ((Translate_Y + i) >> 1) * yuvW + ((Translate_X + j) & ~1)] = tempU;
                        //yuvData[yuvW*yuvH + + (i >> 1) * yuvW + ((Translate_X + j) & ~1) + 1] = tempV;
                    }
                }
            }
        }else if(rotato%360 == 90){
            Translate_Y = yuvH - (int)(minW * mTranslateY_percent / 100.0f) - yuvWaterMarkWidth;
            Translate_X = yuvW - (int)(minW * mTranslateX_percent / 100.0f) - yuvWaterMarkHeight;
            LogHelper.i(TAG,"yuvAddWaterMark 3 Translate_X = " + Translate_X + " Translate_Y = " + Translate_Y + " rotato = " + rotato);
            for(int i =0; i< yuvWaterMarkWidth; i++){
                for(int j =0; j< yuvWaterMarkHeight; j ++){
                    byte tempY = yuvWaterMarkData[j*yuvWaterMarkWidth + (yuvWaterMarkWidth - i)];
                    if( tempY!= 0x10 && tempY!= 0x80 && tempY!= 0xeb){
                        if(tempY == -21){
                            yuvData[(Translate_Y + i)*yuvW + Translate_X + j] = (byte)(255);
                        }else{
                            yuvData[(Translate_Y + i)*yuvW + Translate_X + j] = tempY;
                        }
                    }
                }
            }
        }else if(rotato%360 == 180){
            Translate_Y = (int)(minW * mTranslateY_percent / 100.0f);
            Translate_X = yuvW - (int)(minW * mTranslateY_percent / 100.0f) - yuvWaterMarkWidth;
            LogHelper.i(TAG,"yuvAddWaterMark 3 Translate_X = " + Translate_X + " Translate_Y = " + Translate_Y + " rotato = " + rotato);
            for(int i =0; i< yuvWaterMarkHeight; i++){
                for(int j =0; j< yuvWaterMarkWidth; j ++){
                    byte tempY = yuvWaterMarkData[(yuvWaterMarkHeight-1-i)*yuvWaterMarkWidth + (yuvWaterMarkWidth - j)];
                    if( tempY!= 0x10 && tempY!= 0x80 && tempY!= 0xeb){
                        if(tempY == -21){
                            yuvData[(Translate_Y + i)*yuvW + Translate_X + j] = (byte)(255);
                        }else{
                            yuvData[(Translate_Y + i)*yuvW + Translate_X + j] = tempY;
                        }
                    }
                }
            }
        }else if(rotato%360 == 270){
            Translate_Y = (int)(minW * mTranslateX_percent / 100.0f);
            Translate_X = (int)(minW * mTranslateY_percent / 100.0f);
            LogHelper.i(TAG,"yuvAddWaterMark 3 Translate_X = " + Translate_X + " Translate_Y = " + Translate_Y + " rotato = " + rotato);
            for(int i =0; i< yuvWaterMarkWidth; i++){
                for(int j =0; j< yuvWaterMarkHeight; j ++){
                    byte tempY = yuvWaterMarkData[(yuvWaterMarkHeight-1-j)*yuvWaterMarkWidth + i];
                    if( tempY!= 0x10 && tempY!= 0x80 && tempY!= 0xeb){
                        if(tempY == -21){
                            yuvData[(Translate_Y + i)*yuvW + Translate_X + j] = (byte)(255);
                        }else{
                            yuvData[(Translate_Y + i)*yuvW + Translate_X + j] = tempY;
                        }
                    }
                }
            }
        }

        LogHelper.i(TAG,"!!!! cost = " + (System.currentTimeMillis() - time));
        return yuvData;
    }

    //bv wuyonglin modify for AiWorksBokeh water logo 20200827 start
    public static void saveWaterMark(Context context, String filePath, long dateTaken,ExifInterface exif,boolean rotation, Bitmap mPictureBitmap) {
        mRotation = rotation;
        try {
            Bitmap bitmap = null;
            if (mPictureBitmap == null) {
            bitmap = BitmapFactory.decodeFile(filePath, null);
            } else {
            bitmap = mPictureBitmap;
            }
    //bv wuyonglin modify for AiWorksBokeh water logo 20200827 end
            Bitmap markBitmap = null;
            if (context.getResources().getBoolean(R.bool.config_timestamp_support)) {
                markBitmap = createWaterMarkBitmapTimestamp(context, bitmap, dateTaken);
            } else {
                if(rotation){
                    markBitmap = createWaterMarkBitmapBySdof(context, bitmap);
                }else{
                    markBitmap = createWaterMarkBitmap(context, bitmap);
                }
            }

            //modify by huangfei for exif for watermark start
            //compressAndSaveBitmap(markBitmap, filePath, 80);
            compressAndSaveBitmap(markBitmap, filePath, 80,exif);
            //modify by huangfei for exif for watermark end

            refreshFileInfoInDatabase(context, filePath);
        } catch (NullPointerException ex) {
            LogHelper.e(TAG, "NullPointerException : " + ex);
        }
    }

    private static Bitmap createWaterMarkBitmapTimestamp(Context context, Bitmap src, long dateTaken) {
        String datetime;
        if (src == null) return null;
        int w = src.getWidth();
        int h = src.getHeight();
        //start, wangsenhao, for timestamp color, 2019.06.04
        int TimestampColor = context.getResources().getInteger(R.integer.config_timestamp_color);
        //end, wangsenhao, for timestamp color, 2019.06.04

        String tsFormat = context.getResources().getString(R.string.config_timestamp_format);
        if (tsFormat.equals("dmy")) {
            datetime = DateFormat.format("kk:mm dd/MM/yyyy", dateTaken).toString();
        } else { //ymd
            datetime = DateFormat.format("yyyy/MM/dd kk:mm:ss", dateTaken).toString();
        }

        Bitmap bmpTemp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpTemp);
        Paint p = new Paint();
        String fontName = "serif";
        int textSize = 60;
        Typeface font = Typeface.create(fontName, Typeface.NORMAL);
        p.setColor(TimestampColor);
        p.setTypeface(font);
        if (w > 1536) {//vga/qvga
            p.setTextSize(textSize);
        } else if (w > 768) {//vga/qvga
            p.setTextSize((int) (textSize / 2));
        } else if (w < 480) {//vga/qvga
            p.setTextSize((int) (textSize / 10));
        } else {
            p.setTextSize(textSize / 4);
        }

        canvas.drawBitmap(src, 0, 0, p);

        Rect bounds = new Rect();
        p.getTextBounds(datetime, 0, datetime.length(), bounds);
        canvas.drawText(datetime, w - bounds.width() - 20, h - bounds.height(), p);
        //canvas.drawText(datetime, 0, textSize, p);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bmpTemp;
    }

    public static Bitmap createWaterMarkBitmap(Context context, Bitmap src) {
        if (src == null) return null;
        int mScaleValue = context.getResources().getInteger(R.integer.water_mark_scale);
        boolean mIsMarkRight = context.getResources().getBoolean(R.bool.set_water_mark_right);
        int mTranslateX = context.getResources().getInteger(R.integer.water_mark_translate_x);
        int mTranslateY = context.getResources().getInteger(R.integer.water_mark_translate_y);
        float mScaleValue_percent = Float.parseFloat(context.getResources().getString(R.string.water_mark_scale_percent));
        float mTranslateX_percent = Float.parseFloat(context.getResources().getString(R.string.water_mark_translate_x_percent));
        float mTranslateY_percent = Float.parseFloat(context.getResources().getString(R.string.water_mark_translate_y_percent));

        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap bmpTemp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpTemp);
        Paint p = new Paint();
        Bitmap picBitmap = null;
        picBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.water_mark_logo);
        Matrix matrix = new Matrix();
        float mScale = w / (1.0f * mScaleValue * picBitmap.getWidth());
        if(mScaleValue_percent>0){
            mScale = (w * mScaleValue_percent) / (100.0f * picBitmap.getWidth());
        }
        int Translate_X = mTranslateX;
        int Translate_Y = mTranslateY;
        if(mTranslateX_percent>0){
            Translate_X = (int)(w * mTranslateX_percent / 100.0f);
        }
        if(mTranslateY_percent>0){
            Translate_Y = (int)(w * mTranslateY_percent / 100.0f);
        }
        matrix.postScale(mScale, mScale);

        //modify by huangfei for watermark picture abnormal start
        //canvas.drawBitmap(src, 0, 0, p);
        Rect rect = new Rect(0, 0, w, h);
        canvas.drawBitmap(src, rect, rect, p);
        //modify by huangfei for watermark picture abnormal end

        Bitmap dstbmp = Bitmap.createBitmap(picBitmap, 0, 0, picBitmap.getWidth(), picBitmap.getHeight(), matrix, true);
        if (mIsMarkRight) {
            canvas.drawBitmap(dstbmp, picBitmap.getWidth() * mScale, h - dstbmp.getHeight(), null);
        } else {
            //HCT.ouyang water mark transform begin
            //canvas.drawBitmap(dstbmp, 0, h - dstbmp.getHeight(), null);
            //canvas.drawBitmap(dstbmp, Translate_X, h - dstbmp.getHeight() - Translate_Y, null);
            Rect rect1 = new Rect(Translate_X, h - dstbmp.getHeight() - Translate_Y, Translate_X+dstbmp.getWidth(), h- Translate_Y);
            canvas.drawBitmap(dstbmp, null, rect1, p);
            //HCT.ouyang end
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bmpTemp;
    }
	
	public static Bitmap createWaterMarkBitmapBySdof(Context context, Bitmap src) {
        if (src == null) return null;
        int mScaleValue = context.getResources().getInteger(R.integer.water_mark_scale);
        boolean mIsMarkRight = context.getResources().getBoolean(R.bool.set_water_mark_right);
        int mTranslateX = context.getResources().getInteger(R.integer.water_mark_translate_x);
        int mTranslateY = context.getResources().getInteger(R.integer.water_mark_translate_y);
        float mScaleValue_percent = Float.parseFloat(context.getResources().getString(R.string.water_mark_scale_percent));
        float mTranslateX_percent = Float.parseFloat(context.getResources().getString(R.string.water_mark_translate_x_percent));
        float mTranslateY_percent = Float.parseFloat(context.getResources().getString(R.string.water_mark_translate_y_percent));
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap bmpTemp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);;
        Canvas canvas = new Canvas(bmpTemp);
        Paint p = new Paint();
        Bitmap picBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.water_mark_logo);
        Matrix matrix = new Matrix();
        float mScale =  h / (1.0f * mScaleValue * picBitmap.getWidth());
        if(mScaleValue_percent>0){
            mScale = (h * mScaleValue_percent) / (100.0f * picBitmap.getWidth());
        }
        int Translate_X = mTranslateX;
        int Translate_Y = mTranslateY;
        if(mTranslateX_percent>0){
            Translate_X = (int)(h * mTranslateX_percent / 100.0f);
        }
        if(mTranslateY_percent>0){
            Translate_Y = (int)(h * mTranslateY_percent / 100.0f);
        }
        matrix.setRotate(270);
        matrix.postScale(mScale, mScale);
     
        //modify by huangfei for watermark picture abnormal start
        //canvas.drawBitmap(src, 0, 0, p);
        Rect rect = new Rect(0, 0, w, h);
        canvas.drawBitmap(src, rect, rect, p);
        //modify by huangfei for watermark picture abnormal end

        Bitmap dstbmp = Bitmap.createBitmap(picBitmap, 0, 0, picBitmap.getWidth(), picBitmap.getHeight(), matrix, true);

        //modify by huangfei for watermark picture abnormal start
        //canvas.drawBitmap(dstbmp, w - dstbmp.getWidth() - Translate_Y, h- dstbmp.getHeight()-Translate_X, null);
        Rect rect1 = new Rect(w - dstbmp.getWidth() - Translate_Y,h- dstbmp.getHeight()-Translate_X,
                w - Translate_Y, h-Translate_X);
        canvas.drawBitmap(dstbmp, null, rect1, p);
        //modify by huangfei for watermark picture abnormal end

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bmpTemp;
    }
	
    public static void compressAndSaveBitmap(Bitmap rawBitmap, String mFilePath, int quality,ExifInterface exif) {
        File saveFile = new File(mFilePath);
        if (saveFile.exists()) {
            saveFile.delete();
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(saveFile);
            if (fileOutputStream != null && rawBitmap != null) {

                //add by huangfei for exif for watermark start
                if(exif != null){
                    exif.writeExif(rawBitmap,fileOutputStream);
                }
                //add by huangfei for exif for watermark end

                rawBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void refreshFileInfoInDatabase(Context context, String filePath) {

        //adb by huangfei for watermarkThumbnailClick start
        if(Config.isWatermarkThumbnailClickLimited(context)){
            mListener.onWaterMarkCaptureEnd();
        }
        //adb by huangfei for watermarkThumbnailClick end

        /*context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + filePath)));*/
    }

    public interface WaterMarkStatusListener {
        void onWaterMarkCaptureEnd();
    }

    public static void setWaterMarkStatusListener(WaterMarkStatusListener listener){
        mListener = listener;
    }
}
