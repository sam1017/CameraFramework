
package com.mediatek.camera.feature.mode.hctbokeh.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.MotionEvent;
//import com.mediatek.camera.util.Log;


public class FilterSeekBar extends AppCompatSeekBar {

    private Drawable mThumb;

    private static final String TAG = "FilterSeekBar";
    public boolean mfromUser = false;

    private OnSeekBarChangeListener mOnSeekBarChangeListener;

    public FilterSeekBar(Context context) {
        this(context, null);
    }

    public FilterSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.seekBarStyle);
    }

    public FilterSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        super.setOnSeekBarChangeListener(l);
        mOnSeekBarChangeListener = l;
    }

    void onStartTrackingTouch() {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    void onStopTrackingTouch() {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    void onProgressRefresh(float scale, boolean fromUser) {
        Drawable thumb = mThumb;
        if (thumb != null) {
            setThumbPos(getHeight(), thumb, scale, Integer.MIN_VALUE);
            invalidate();
        }
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onProgressChanged(this, getProgress(), fromUser);
        }
    }

    private void setThumbPos(int w, Drawable thumb, float scale, int gap) {
        int available = w + getPaddingLeft() - getPaddingRight();
        int thumbWidth = thumb.getIntrinsicWidth();
        int thumbHeight = thumb.getIntrinsicHeight();
        available -= thumbWidth;
        // The extra space for the thumb to move on the track
        //  available += getThumbOffset() * 2;

        int thumbPos = (int) (scale * available);
        int topBound, bottomBound;
        if (gap == Integer.MIN_VALUE) {
            Rect oldBounds = thumb.getBounds();
            topBound = oldBounds.top;
            bottomBound = oldBounds.bottom;
        } else {
            topBound = gap;
            bottomBound = gap + thumbHeight;
        }
        thumb.setBounds(thumbPos, topBound, thumbPos + thumbWidth, bottomBound);
    }

    @Override
    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(), 0);
        super.onDraw(c);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    public void setThumb(Drawable thumb) {
        mThumb = thumb;
        super.setThumb(thumb);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldw, oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //setPressed(true);
                onStartTrackingTouch();
                trackTouchEvent(event);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;

            case MotionEvent.ACTION_MOVE:
                trackTouchEvent(event);
                attemptClaimDrag();
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;

            case MotionEvent.ACTION_UP:
                trackTouchEvent(event);
                onStopTrackingTouch();
                //setPressed(false);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;

            case MotionEvent.ACTION_CANCEL:
                onStopTrackingTouch();
                //setPressed(false);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;
        }
        return true;
    }

    private void trackTouchEvent(MotionEvent event) {
        final int Height = getHeight();
        final int available = Height - getPaddingBottom() - getPaddingTop();
        int Y = (int) event.getY();
        float scale;
        float progress = 0;
        if (Y > Height - getPaddingBottom()) {
            scale = 0.0f;
        } else if (Y < getPaddingTop()) {
            scale = 1.0f;
        } else {
            scale = (float) (Height - getPaddingBottom() - Y) / (float) available;
        }

        final int max = getMax();
        progress = scale * max;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mfromUser = false;
        } else {
            mfromUser = true;
        }
        setProgress((int) progress);
    }

    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    public synchronized void setProgressAndThumb(int progress) {
        mfromUser = false;
        //setProgress(progress);
        setProgress(getMax() - (getMax() - progress));
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }

}
