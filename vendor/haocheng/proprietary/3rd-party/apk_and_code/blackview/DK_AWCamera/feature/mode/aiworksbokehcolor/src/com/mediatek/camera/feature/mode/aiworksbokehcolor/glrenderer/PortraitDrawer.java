package com.mediatek.camera.feature.mode.aiworksbokehcolor.glrenderer;

import android.graphics.SurfaceTexture;
import android.util.Log;
import com.aiworks.android.portrait.PortraitEffect;
import com.mediatek.camera.feature.mode.aiworksbokehcolor.util.AiWorksCameraUtil;
//import com.aiworks.yuvUtil.YuvEncodeJni;
import android.graphics.YuvImage;
import java.io.FileOutputStream;
import java.io.File;
import android.graphics.Rect;
import java.io.FileNotFoundException;
import android.os.Environment;

public class PortraitDrawer {

    private static final String TAG = PortraitDrawer.class.getSimpleName();

    private PortraitEffect mPortraitEffect;
    private int mTextureId = -1;
    private int i = 0;

    public PortraitDrawer(PortraitEffect portraitEffect) {
        mPortraitEffect = portraitEffect;
    }

    public SurfaceTexture createSurfaceTexture() {
        Log.d(TAG, "createSurfaceTexture");
        mTextureId = AiWorksCameraUtil.genExternalOESTexture();
        SurfaceTexture surfaceTexture = new SurfaceTexture(mTextureId);
        mPortraitEffect.initGLEnvironment(true);
        mPortraitEffect.startPortraitDetection();
        Log.d(TAG, "createSurfaceTexture mTextureId = " + mTextureId);
        return surfaceTexture;
    }

    public void releaseSurfaceTexture() {
        Log.d(TAG, "PortraitEffect releaseSurfaceTexture1");
        //mPortraitEffect.destroy();
        mPortraitEffect.stopPortraitDetection();
        mPortraitEffect.unitGLEnvironment();
        AiWorksCameraUtil.deleteExternalOESTexture(new int[] {mTextureId});
        mTextureId = -1;
    }

    public void releaseSurfaceTexture(boolean isDestroy) {
        Log.d(TAG, "PortraitEffect releaseSurfaceTexture1 isDestroy ="+isDestroy);
        //mPortraitEffect.destroy();
        mPortraitEffect.stopPortraitDetection();
        mPortraitEffect.unitGLEnvironment();
        AiWorksCameraUtil.deleteExternalOESTexture(new int[] {mTextureId});
        mTextureId = -1;
    }

    //private long time;
    //private int fps;

    public void onDrawFrame(float[] mTransform, int previewWidth, int previewHeight, int outWidth, int outHeight) {
        /*long current = System.currentTimeMillis();
        if (current - time > 1000) {
            Log.i(TAG, "fps:" + fps);
            time = current;
            fps = 0;
        } else {
            fps++;
        }

        Log.i(TAG, "onDrawFrame to drawFrame  previewWidth ="+previewWidth+" previewHeight ="+previewHeight+" outWidth ="+outWidth+" outHeight ="+outHeight);*/
        mPortraitEffect.drawFrame(mTextureId, mTransform, previewWidth, previewHeight, outHeight, outWidth, 0, 1.0f);


	//byte[] data = mPortraitEffect.drawFrame(mTextureId, mTransform, previewWidth, previewHeight, outWidthoutHeight, , 0, 1.0f);
	/*i++;
	if (i == 50) {
	    Log.e("tank","1234  i="+i);
	byte[] nv21 = YuvEncodeJni.getInstance().Argb2Yuv(data, 128, 128, 128*4, android.graphics.ImageFormat.NV21);
	dumpYuvDate(nv21,Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" +"/1.jpg", new android.util.Size(128,128));
	}*/

//byte[] nv21 = YuvEncodeJni.getInstance().Argb2Yuv(data, outWidth, outHeight, outWidth*4, android.graphics.ImageFormat.NV21);
//dumpYuvDate(nv21, Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" +"/1.jpg", new android.util.Size(outWidth, outHeight));

    }

public static void dumpYuvDate(byte[] data, String fileName, android.util.Size size) {
    YuvImage yuvImage = new YuvImage(data, 17, size.getWidth(), size.getHeight(), (int[])null);

    try {
        FileOutputStream fs = new FileOutputStream(new File(fileName));
        yuvImage.compressToJpeg(new Rect(0, 0, size.getWidth(), size.getHeight()), 95, fs);
    } catch (FileNotFoundException var6) {
        var6.printStackTrace();
    }

}
}
