package com.mediatek.camera.ui;

import android.widget.TextView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import com.mediatek.camera.R;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.util.Log;

/**
 * Created by aland on 17-5-27.
 */

public class CircleTextView extends TextView {
    private static final String MTAG = "CircleTextView";
    private Paint mBgPaint = new Paint();
    PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0,
            Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private float lastY = 0;
    private float downY = 0;
    private final float MIN_SCROLL = 1;  // 4
    private boolean isAnimationIng = false;

    public CircleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CircleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBgPaint.setAntiAlias(true);
    }

    public CircleTextView(Context context) {
        super(context);
        mBgPaint.setAntiAlias(true);
    }

    CircleTextTouchListener mListener;
    public interface CircleTextTouchListener {
        void onScrollUp();
    }
    public void setCircleTextTouchListener(CircleTextTouchListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dis = lastY - event.getY();
                Log.d(MTAG, " onTouchEvent  move  "+dis);
                if( dis > MIN_SCROLL){
                    mListener.onScrollUp();
                }
            case MotionEvent.ACTION_UP:
                Log.d(MTAG, " onTouchEvent  up  ");
            case MotionEvent.ACTION_DOWN:
                downY = lastY = event.getY();
                Log.d(MTAG, " onTouchEvent  down  "+downY);
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int max = Math.max(measuredWidth, measuredHeight);
        setMeasuredDimension(max, max);
    }
    @Override
    protected void onAnimationStart() {
        isAnimationIng = true;
        super.onAnimationStart();

    }
    @Override
    protected void onAnimationEnd() {
        isAnimationIng = false;
        super.onAnimationEnd();
    }
    public boolean isAnimationIng(){
        return isAnimationIng;
    }

    @Override
    public void setBackgroundColor(int color) {
        mBgPaint.setColor(color);
    }
    public void setNotifiText(int text) {
        setText(text + "");
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.setDrawFilter(pfd);
        //mBgPaint.setColor(Color.WHITE);
        //canvas.drawCircle(getWidth() / 2, getHeight() / 2,Math.max(getWidth(), getHeight()) / 2, mBgPaint);

        //mBgPaint.setColor(ContextCompat.getColor(mContext, R.color.tripleswitch_background));
        //canvas.drawCircle(getWidth() / 2, getHeight() / 2,Math.max(getWidth(), getHeight()) / 2 - 3, mBgPaint);

        super.draw(canvas);
    }

}
