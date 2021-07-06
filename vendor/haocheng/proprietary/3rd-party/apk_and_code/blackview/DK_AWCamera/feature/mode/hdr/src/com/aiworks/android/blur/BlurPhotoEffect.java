package com.aiworks.android.blur;

public class BlurPhotoEffect {

    static {
        System.loadLibrary("aw_portrait_bokeh");
    }

    public static native byte[] processBlur(byte[] image, int format, int width, int height, byte[] mask,
                                            int maskW, int maskH, int level);

    public static native byte[] processDofBlur(byte[] image, int format, int ori, int width, int height, byte[] mask,
                                               int maskW, int maskH, int level);

}
