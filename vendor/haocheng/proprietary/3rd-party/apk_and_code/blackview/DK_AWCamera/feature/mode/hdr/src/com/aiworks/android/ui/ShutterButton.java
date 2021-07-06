/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aiworks.android.ui;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * A button designed to be used for the on-screen shutter button.
 * It's currently an {@code ImageView} that can call a delegate when the
 * pressed state changes.
 */
public class ShutterButton extends RotateImageView {
    public enum ShutterMode {
        PHOTO, VIDEO, PANORAMA, LIGHTCYCLE, PREVIEW, LONGEXPO, VIDEODELAY, DEFAULT
    }

    private static int PHOTO = 0;
    private static int VIDEO = 1;
    private static int PANORAMA = 2;
    private static int VIDEODELAY = 6;
    private boolean delay_recording = false;

    private Paint paintInner, paintOutter;
    private Paint paintRed, paintWhite, paintWhite1, paintWhite2;//video delay paints
    private int redUp = Color.rgb(255, 86, 53);
    private int redDown = Color.rgb(204, 52, 22);

    private Point headPoint;

    private float startRadius, endRadius, maxRadius;

    private float strokeWith = 1.0f;

    private int pressColor = Color.parseColor("#6D6D6D");

    private int mColorStart, mColorEnd, mColorRedDeta, mColorGreenDeta, mColorBlueDeta;

    private ObjectAnimator center;

    private int duration = 200;

    private String TAG = "ShutterButton";

    public static int getViewState(ShutterMode mode) {
        switch (mode) {
            case PHOTO:
                return PHOTO;
            case VIDEO:
                return VIDEO;
            case PANORAMA:
                return PANORAMA;
            case VIDEODELAY:
                return VIDEODELAY;
            default:
                return PHOTO;
        }
    }

    private ShutterMode mShutterMode = ShutterMode.DEFAULT;

    /**
     * A callback to be invoked when a ShutterButton's pressed state changes.
     */
    public interface OnShutterButtonListener {
        /**
         * Called when a ShutterButton has been pressed.
         *
         * @param pressed The ShutterButton that was pressed.
         */
        void onShutterButtonFocus(ShutterButton b, boolean pressed);

        void onShutterButtonClick(ShutterButton b);

        void onLongPressShutButton();

        boolean onCancelLongPressShutterButton();
    }

    private OnShutterButtonListener mListener;
    private boolean mOldPressed;

    public ShutterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        delay_recording = false;
    }

    private void init() {

        paintInner = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintOutter = new Paint(Paint.ANTI_ALIAS_FLAG);

        paintInner.setColor(Color.WHITE);

        paintInner.setAntiAlias(true);
        paintInner.setFilterBitmap(true);
        paintInner.setStrokeWidth(strokeWith);

        paintOutter.setColor(Color.WHITE);

        paintOutter.setAntiAlias(true);
        paintOutter.setFilterBitmap(true);
        // paintOutter.setStrokeWidth(strokeWith);
        paintOutter.setStrokeWidth(4.0f);
        headPoint = new Point();
        headPoint.setRadius(0.0f);

        paintWhite = new Paint();
        paintWhite.setAntiAlias(true);
        paintWhite.setColor(Color.WHITE);
        paintWhite.setStyle(Style.STROKE);
        paintWhite.setStrokeWidth((float) Util.dpToPixel(3.3f));

        paintWhite1 = new Paint();
        paintWhite1.setAntiAlias(true);
        paintWhite1.setColor(Color.WHITE);
        paintWhite1.setStyle(Style.STROKE);
        paintWhite1.setStrokeWidth((float) Util.dpToPixel(6f));

        paintWhite2 = new Paint();
        paintWhite2.setAntiAlias(true);
        paintWhite2.setColor(Color.WHITE);
        paintWhite2.setStyle(Style.STROKE);
        paintWhite2.setStrokeWidth((float) Util.dpToPixel(6f));

        paintRed = new Paint();
        paintRed.setAntiAlias(true);
        paintRed.setColor(redUp);
        paintRed.setStyle(Style.FILL);
    }

    public Point getHeadPoint() {
        return this.headPoint;
    }

    public void setOnShutterButtonListener(OnShutterButtonListener listener) {
        mListener = listener;
    }

    /**
     * Hook into the drawable state changing to get changes to isPressed -- the
     * onPressed listener doesn't always get called when the pressed state
     * changes.
     */
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        final boolean pressed = isPressed();
        if (pressed != mOldPressed) {
            if (!pressed) {
                // When pressing the physical camera button the sequence of
                // events is:
                //    focus pressed, optional camera pressed, focus released.
                // We want to emulate this sequence of events with the shutter
                // button. When clicking using a trackball button, the view
                // system changes the drawable state before posting click
                // notification, so the sequence of events is:
                //    pressed(true), optional click, pressed(false)
                // When clicking using touch events, the view system changes the
                // drawable state after posting click notification, so the
                // sequence of events is:
                //    pressed(true), pressed(false), optional click
                // Since we're emulating the physical camera button, we want to
                // have the same order of events. So we want the optional click
                // callback to be delivered before the pressed(false) callback.
                //
                // To do this, we delay the posting of the pressed(false) event
                // slightly by pushing it on the event queue. This moves it
                // after the optional click notification, so our client always
                // sees events in this sequence:
                //     pressed(true), optional click, pressed(false)

                post(new Runnable() {
                    @Override
                    public void run() {
                        callShutterButtonFocus(pressed);
                    }
                });
            } else {
                callShutterButtonFocus(pressed);
            }
            mOldPressed = pressed;
        }
    }

    private void callShutterButtonFocus(boolean pressed) {
        if (mListener != null) {
            mListener.onShutterButtonFocus(this, pressed);
        }
        if (isLongPress) {
            tickCountDown(false);
        }
    }

    //    @Override
//    public boolean performClick() {
//        boolean result = super.performClick();
//        if (mListener != null) {
//            mListener.onShutterButtonClick(this);
//        }
//        return result;
//    }
    public void setShutterMode(ShutterMode mode) {
        mShutterMode = mode;
    }

    public ShutterMode getShutterMode() {
        return mShutterMode;
    }

//    public boolean pointerInViewArea(float localX, float localY) {
//        return localX > 0 && localY > 0 && (localX < ((mRight - mLeft)) && localY < ((mBottom - mTop)));
//    }


    @Override
    protected void onDraw(Canvas canvas) {
//        if (getShutterMode() == ShutterMode.PHOTO) {
//            drawZZ(canvas);
//        }
        if (getShutterMode() == ShutterMode.VIDEODELAY) {
            drawVideoDelayShutterBtn(canvas);
        } else {
            super.onDraw(canvas);
        }

    }

    private void startPhotoAnmimation() {
        //paintInner.setColor(pressColor);

        setStartEndColor(Color.WHITE, pressColor);

        center = ObjectAnimator.ofFloat(this, centerProperty, startRadius, endRadius, startRadius);
        center.setDuration(duration);
        center.setInterpolator(new LinearInterpolator());
        center.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                // TODO Auto-generated method stub
                //paintInner.setColor(Color.WHITE);
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
                // TODO Auto-generated method stub

            }
        });
        center.start();
    }

    private Property<ShutterButton, Float> centerProperty = new Property<ShutterButton, Float>(
            Float.class, "centerRadius") {
        @Override
        public Float get(ShutterButton object) {

            return object.getPhotoCenter();
        }

        @Override
        public void set(ShutterButton object, Float value) {

//            Log.d("temp4","value:"+value);

            float fraction = center.getAnimatedFraction();

            if (fraction < 0.5f) {
                setAscendColor(fraction, 255);
            } else {
                setDescendColor(fraction, 255);
            }

            object.setPhotoCenter(value);
        }
    };

    private float getPhotoCenter() {
        return headPoint.getRadius();
    }

    public void setPhotoCenter(float radius) {
//        Log.e(TAG, "setPhotoCenter");
        headPoint.setRadius(radius);
        if (mShutterMode != ShutterMode.VIDEODELAY) {
//            Log.e(TAG, "setPhotoCenter invalidate");
            invalidate();
        }

    }

    private void drawZZ(Canvas canvas) {

        Drawable drawable = getDrawable();
        if (drawable == null)
            return;

        Rect bounds = drawable.getBounds();
        int w = bounds.right - bounds.left;
        int h = bounds.bottom - bounds.top;

        if (w == 0 || h == 0)
            return; // nothing to draw

//        Log.d("temp3","bounds.right:"+bounds.right 
//                + " bounds.left:"+bounds.left + " bounds.bottom:"+bounds.bottom 
//                + " bounds.top:"+bounds.top);


        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        int width = getWidth() - left - right;
        int height = getHeight() - top - bottom;

        maxRadius = w / 2.0f - 1;
        startRadius = maxRadius - 10;
        endRadius = maxRadius - 15;

        headPoint.setX(width / 2.0f);
        headPoint.setY(height / 2.0f);
        if (getHeadPoint().getRadius() < 0.001f && getHeadPoint().getRadius() > -0.001f) {
            headPoint.setRadius(startRadius);
        }

//        Log.d("temp3","maxRadius:"+maxRadius + "    w:"+ w
//                + " width:"+getWidth() + " height:"+getHeight());
        canvas.save();

        paintOutter.setStyle(Style.STROKE);
        canvas.drawCircle(getHeadPoint().getX(), getHeadPoint().getY(), maxRadius, paintOutter);

        if (Util.SHOW_SHUTTBUTTON) {//liubin mod for show progressbar 2015-7-29
            paintInner.setStyle(Style.FILL);
            canvas.drawCircle(getHeadPoint().getX(), getHeadPoint().getY(), getHeadPoint().getRadius(), paintInner);
        }

        canvas.restore();
    }


    public void setStartEndColor(int startColor, int endColor) {
        mColorStart = startColor;
        mColorEnd = endColor;
        mColorRedDeta = Color.red(mColorEnd) - Color.red(mColorStart);
        mColorGreenDeta = Color.green(mColorEnd) - Color.green(mColorStart);
        mColorBlueDeta = Color.blue(mColorEnd) - Color.blue(mColorStart);

//        Log.d("temp4","mColorStart:"+mColorStart + "    mColorEnd:"+mColorEnd 
//                + "    mColorRedDeta:"+mColorRedDeta +" mColorGreenDeta:"+mColorGreenDeta
//                +" mColorBlueDeta:"+mColorBlueDeta);
    }

    public void setAscendColor(float radio, int alpha) {

        int red = Color.red(mColorStart) + (int) (mColorRedDeta * radio);
        int green = Color.green(mColorStart) + (int) (mColorGreenDeta * radio);
        int blue = Color.blue(mColorStart) + (int) (mColorBlueDeta * radio);

        int color = Color.argb(alpha, red, green, blue);
        paintInner.setColor(color);

        //Log.d("temp4","setUnselectingTextColor:"+"red:"+red+" green:"+green + " blue:"+blue + " color:"+color);
    }

    public void setDescendColor(float radio, int alpha) {

        int red = Color.red(mColorEnd) - (int) (mColorRedDeta * radio);
        int green = Color.green(mColorEnd) - (int) (mColorGreenDeta * radio);
        int blue = Color.blue(mColorEnd) - (int) (mColorBlueDeta * radio);

        int color = Color.argb(alpha, red, green, blue);
        paintInner.setColor(color);

        //Log.d("temp4","setSelectingTextColor"+ " red:"+red+" green:"+green + " blue:"+blue + " color:"+color);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub

        int action = event.getActionMasked();

        //modify by xiaokelong@qiku.com for continuous optimization 2016-6-15 start
        switch (action) {
            case MotionEvent.ACTION_DOWN:
//            Log.d("temp2","action down...");
                startPhotoAnmimation();
                paintRed.setColor(redDown);
//            if (mListener != null) {
//             mListener.onShutterButtonClick(this);
//            }
                //add by renshangyuan@qiku.com for touch down  take photos 2016-3-3
                tickCountDown(true);
                break;
            case MotionEvent.ACTION_UP:
                paintRed.setColor(redUp);
                //add by renshangyuan@qiku.com for touch down  take photos 2016-3-3
                if (mListener != null && !isLongPress) {
                    mListener.onShutterButtonClick(this);
                }
                tickCountDown(false);
                if (isLongPress) {
                    isLongPress = false;
                }
				break;
            case MotionEvent.ACTION_CANCEL:
                isLongPress = false;
                break;
            default:
                break;
        }
        //modify by xiaokelong@qiku.com for continuous optimization 2016-6-15 end
        return super.onTouchEvent(event);
    }


    static class Point {
        private float mR, mX, mY;

        public void setRadius(float r) {
            this.mR = r;
        }

        public float getRadius() {
            return mR;
        }

        public void setX(float x) {
            this.mX = x;
        }

        public void setY(float y) {
            this.mY = y;
        }

        public float getX() {
            return this.mX;
        }

        public float getY() {
            return this.mY;
        }
    }

    //begin add by renshangyuan@qiku.com for video delay recording
    private static class VideoDelayHand extends Handler {
        WeakReference<ShutterButton> mRef;

        VideoDelayHand(ShutterButton bar) {
            mRef = new WeakReference<ShutterButton>(bar);
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            ShutterButton mOwer = mRef.get();
            if (mOwer == null) {
                //Log.w(TAG, "ModeContextHandler reference get null");
                return;
            }
            if (msg.what == 1) {
                mOwer.invalidate();
            }
        }
    }

    private Handler mVideoDelayHand = new VideoDelayHand(this);

    private class PlayThread extends Thread {
        @Override
        public void run() {
            while (delay_recording) {
                try {
                    Message msg = new Message();
                    msg.what = 1;
                    mVideoDelayHand.sendMessage(msg);
                    if (isFristTime) {
                        degrees += 6;
                        speed = 10;
                    } else {
                        degrees += 6;
                        speed = 100;
                    }
                    if (degrees >= 360) {
                        degrees = 0;
                        if (isFristTime) {
                            isFristTime = !isFristTime;
                            isOpen = false;
                        } else {
                            isOpen = !isOpen;
                        }
                    }
                    Thread.sleep(speed);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private int speed = 100;//ms
    private int degrees = 0;
    private boolean isFristTime = true;
    private boolean isOpen = true;
    private PlayThread mPlayThread;

    public void setStateDelayRecording(boolean recording) {
//        Log.e(TAG, "setStateDelayRecording " + recording);
        delay_recording = recording;
        if (recording) {
            degrees = 0;
            isFristTime = true;
            isOpen = true;
            mPlayThread = null;
            mPlayThread = new PlayThread();
            if (mPlayThread != null) {
                mPlayThread.start();
            }
        } else {
            mVideoDelayHand.removeMessages(1);
            mVideoDelayHand.removeCallbacks(mPlayThread);
            mPlayThread = null;
        }
        invalidate();
    }

    private void drawVideoDelayShutterBtn(Canvas canvas) {

        Drawable drawable = getDrawable();
        if (drawable == null)
            return;
        Rect bounds = drawable.getBounds();
        int w = bounds.right - bounds.left;
        int h = bounds.bottom - bounds.top;
        int width = getWidth();
        int height = getHeight();
//        Log.i(TAG, "w:" + w + " h:" + h + " width" + width + " height" + height + "  degrees " + degrees);
        if (w == 0 || h == 0) {
            return; // nothing to draw
        }
        //draw outSide large circle
        canvas.save();
        float broad2 = Util.dpToPixel(3.2f);
        RectF oval2 = new RectF(0 + broad2, 0 + broad2, width - broad2, height - broad2);
        float sweepAngle1;
        sweepAngle1 = Util.dpToPixel(1.1f);
        for (int i = -90; i <= 270; i = i + 36) {
            canvas.drawArc(oval2, i, sweepAngle1, false, paintWhite1);
        }
        if (delay_recording) {
            canvas.drawArc(oval2, degrees - 90, sweepAngle1, false, paintWhite2);
        }
        canvas.restore();
        //draw outSide small circle
        canvas.save();
        float broad = Util.dpToPixel(2f);
        RectF oval = new RectF(0 + broad, 0 + broad, width - broad, height - broad);
        float sweepAngle2;
        sweepAngle2 = Util.dpToPixel(1f);
        if (delay_recording) {
            if (isOpen) {
                for (int i = degrees - 90; i <= 270; i = i + 6) {
                    canvas.drawArc(oval, i, sweepAngle2, false, paintWhite);
                }
            } else {
                for (int i = -90; i <= degrees - 90; i = i + 6) {
                    canvas.drawArc(oval, i, sweepAngle2, false, paintWhite);
                }
            }
            canvas.restore();

        } else {
            for (int i = -90; i <= 270; i = i + 6) {
                canvas.drawArc(oval, i, sweepAngle2, false, paintWhite);
            }
            canvas.restore();
        }


        //-------------red button------------------
        if (delay_recording) {
            //draw stop
            canvas.save();
            int space = Util.dpToPixel(17);
            RectF oval3 = new RectF(0 + space, 0 + space, width - space, height - space);
            canvas.drawRoundRect(oval3, 5, 5, paintRed);//
            canvas.restore();
        } else {
            //draw play
            canvas.save();
            int radius = (width - Util.dpToPixel(17)) / 2;
            canvas.drawCircle(width / 2, height / 2, radius, paintRed);
            canvas.restore();
        }
    }

    //end add by renshangyuan@qiku.com for video delay recording
//add by renshangyuan@qiku.com for touch down  take photos 2016-3-3
    private final long mContinuousShotModeCountdownMilliSec = 600;//1500
    private MyCountDownTimer mContinuousShotModeCountdownTimer = null;

    private class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
//            Log.d(TAG, "Time remaining until entering Continuous Shot Mode "
//                    + millisUntilFinished + " .\n");
        }

        @Override
        public void onFinish() {
            isLongPress = true;
//            Log.d(TAG, "onFinish()---come---onLongPressShutButton---begin!!!");
            if(mListener != null && isPressed()) {
                mListener.onLongPressShutButton();
            }
        }
    }

    private boolean tickCountDown(boolean start) {
        boolean rec = false;
//        Log.e(TAG, "tickCountDown " + start);
        if (start) {
            if (mContinuousShotModeCountdownTimer != null) {
                mContinuousShotModeCountdownTimer.cancel();
                mContinuousShotModeCountdownTimer = null;
            }
            mContinuousShotModeCountdownTimer = new MyCountDownTimer(
                    mContinuousShotModeCountdownMilliSec,
                    mContinuousShotModeCountdownMilliSec);
            mContinuousShotModeCountdownTimer.start();
        } else {
            if (mContinuousShotModeCountdownTimer != null) {
                mContinuousShotModeCountdownTimer.cancel();
                mContinuousShotModeCountdownTimer = null;
                if (mListener != null) {
                    rec = mListener.onCancelLongPressShutterButton();
                }
            }
        }
        return rec;
    }

    /* qiku add begin,zhengtengkai@qiku.com */
        /* for retina flash */
//    private CameraUI mUI;
    private boolean enabled = false;
    private boolean isLongPress = false;
    public static final int MIN_CLICK_DELAY_TIME = 1200;
    private long lastClickTime = 0;

//    public void setUI(CameraUI UI) {
//        mUI = UI;
//    }

    public void enableCaptureOverlayer(boolean enable) {
        enabled = enable;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                long currentTime = Calendar.getInstance().getTimeInMillis();
                if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                    lastClickTime = currentTime;
//                    if (mUI != null && enabled && !isLongPress) {
//                        enabled = false;
//                        mUI.showCaptureOverlayer();
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e1) {
//                            // TODO Auto-generated catch block
//                            e1.printStackTrace();
//                        }
//                        mUI.hideCaptureOverlayer();
//                        enabled = true;
//                    }
                }

                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }
        /* qiku add end */
}
