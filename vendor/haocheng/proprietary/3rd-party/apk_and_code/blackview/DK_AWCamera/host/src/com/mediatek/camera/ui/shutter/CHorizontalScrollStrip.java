package com.mediatek.camera.ui.shutter;

import androidx.core.view.ViewCompat;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

class CHorizontalScrollStrip extends LinearLayout {
    private Paint dividerPaint;
    private boolean mIsDivider = true;

    CHorizontalScrollStrip(Context context, AttributeSet attrs, boolean divided) {
        super(context);
        setWillNotDraw(false);
    }

}
