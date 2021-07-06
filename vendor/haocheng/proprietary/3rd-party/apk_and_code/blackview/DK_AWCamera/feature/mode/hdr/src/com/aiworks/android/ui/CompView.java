package com.aiworks.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CompView extends SurfaceView implements SurfaceHolder.Callback {

    public interface Callback {
        void onReady();
    }

    private static final String TAG = "CompView";

    private SurfaceHolder mHolder;
    private float mScale = 1f;
    private TouchEventListener mTouchEventListener;
    private GestureDetector mGesture;
    private boolean mTouchedSlider = false;
    private boolean mTouchAction = false;
    private static final float SLIDER_TOUCH_RANGE = 150.f;
    private boolean mNeedHighImageUpdate = false;

    private Matrix mMatrix = new Matrix();

    private float mFitScale = 1f;

    private Rect mResultSrcRect = new Rect();
    private RectF mResultDstRect = new RectF();
    private Rect mInputSrcRect = new Rect();
    private RectF mInputDstRect = new RectF();

    private float mMaxScale = 1f;

    private int mFitImageWidth;
    private int mFitImageHeight;

    private Point mOffset = new Point();

    private float mProgress;

    private int mViewWidth;
    private int mViewHeight;

    private int mImageWidth;
    private int mImageHeight;

    private Paint mPaintLine;

    private float mProgressPosition;

    private Bitmap mBmpLeft;
    private Bitmap mBmpRight;

    private Callback mCallback;

    public CompView(Context context) {
        super(context);
        init(context);
    }

    public CompView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.RGBA_8888);
        mTouchEventListener = new TouchEventListener(mMatrix);
        GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                mMatrix.reset();
                if (mScale == 1f) {
                    if (mFitScale < 1) {
                        mScale = 1f / mFitScale;
                        mMatrix.postScale(mScale, mScale, e.getX(), e.getY());
                    }
                } else {
                    mScale = 1f;
                }
                adjustProgress(mMatrix);
                doDraw(mMatrix);
                mNeedHighImageUpdate = true;
                return true;
            }
        };
        mGesture = new GestureDetector(context, mGestureListener);

        mPaintLine = new Paint();
        mPaintLine.setColor(Color.YELLOW);
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setStrokeWidth(3.0f);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        mProgress = mViewWidth >> 1;
        mProgressPosition = mProgress / mViewWidth;
        doDraw(mMatrix);
        if (mCallback != null) {
            mCallback.onReady();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mViewWidth = 0;
        mViewHeight = 0;
    }


    public void compare(Bitmap leftImage, Bitmap rightImage, int imageWidth, int imageHeight) {
        if (mViewWidth == 0 || mViewHeight == 0) {
            Log.w(TAG, "compare, view size is 0");
            return;
        }

        if (leftImage == null || rightImage == null) {
            Log.w(TAG, "compare, bitmap null");
            return;
        }

        mImageWidth = imageWidth;
        mImageHeight = imageHeight;
        mFitScale = getFitScale(mViewWidth, mViewHeight, mImageWidth, mImageHeight);
        mFitImageWidth = Math.round(mImageWidth * mFitScale);
        if ((mFitImageWidth & 0x01) != 0) mFitImageWidth--;
        mFitImageHeight = Math.round(mImageHeight * mFitScale);
        if ((mFitImageHeight & 0x01) != 0) mFitImageHeight--;

        Log.d(TAG, "compare, mImageWidth " + mImageWidth + " mImageHeight " + mImageHeight +
                ", mFitImageWidth " + mFitImageWidth + " mFitImageHeight " + mFitImageHeight);

        mMaxScale = 1f / mFitScale;

        mOffset.x = (mViewWidth - mFitImageWidth) >> 1;
        mOffset.y = (mViewHeight - mFitImageHeight) >> 1;

        mResultSrcRect.set(mImageWidth >> 1, 0, mImageWidth, mImageHeight);
        mResultDstRect.set(new RectF(mOffset.x + (mFitImageWidth / 2f), mOffset.y, mOffset.x + mFitImageWidth, mOffset.y + mFitImageHeight));
        mInputSrcRect.set(0, 0, mImageWidth >> 1, mImageHeight);
        mInputDstRect.set(new RectF(mOffset.x, mOffset.y, mOffset.x + (mFitImageWidth / 2f), mOffset.y + mFitImageHeight));

        mBmpLeft = leftImage;
        mBmpRight = rightImage;
        doDraw(mMatrix);
    }

    private float getFitScale(int parentWidth, int parentHeight, int viewWidth, int viewHeight) {
        float scale_w = (float)parentWidth / (float)viewWidth;
        float scale_h = (float)parentHeight / (float)viewHeight;
        return Math.min(scale_w , scale_h);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGesture.onTouchEvent(event);
        mMatrix = mTouchEventListener.onTouchEvent(event);
        if (mNeedHighImageUpdate && event.getAction() == MotionEvent.ACTION_UP) {
            mNeedHighImageUpdate = false;
        } else {
            doDraw(mMatrix);
        }
        return true;
    }


    private void setSliderPressed(boolean pressed) {
        if (pressed) {
            mPaintLine.setColor(Color.RED);
        } else {
            mPaintLine.setColor(Color.YELLOW);
        }
    }

    private void changeProgress(float progress) {
        float move_x = progress - mProgress;
        if (progress < mOffset.x) {
            progress = mOffset.x;
        } else if (progress > (mViewWidth - mOffset.x - 1)) {
            progress = (mViewWidth - mOffset.x - 1);
        }
        mProgress = progress;

        float src_move = Math.round(move_x / mFitScale);
        mInputSrcRect.right = Math.round(Math.min(Math.max(mInputSrcRect.right + src_move, mInputSrcRect.left), mImageWidth));
        mInputDstRect.right = Math.min(Math.max(mInputDstRect.right + src_move * mFitScale, mInputDstRect.left), mResultDstRect.right);
        mResultSrcRect.left = Math.round(Math.max(Math.min(mResultSrcRect.left + src_move, mResultSrcRect.right), 0));
        mResultDstRect.left = Math.max(Math.min(mResultDstRect.left + src_move * mFitScale, mResultDstRect.right), mOffset.x);

    }

    private class TouchEventListener {
        private float x = 0, y = 0, r = 1;
        private float px = 0, py = 0, pr = 1;
        private int pc = 1;

        private Matrix matrix, pmatrix;
        private static final float MIN_SCALE = 1;


        public TouchEventListener(Matrix matrix) {
            this.matrix = new Matrix(matrix);
            this.pmatrix = new Matrix();
        }

        public Matrix onTouchEvent(MotionEvent e) {
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_FULLSCREEN);
            }*/
            switch (e.getAction()) {
                case MotionEvent.ACTION_UP:
                    setSliderPressedSub(false);
                    mTouchAction = false;
                    break;
                case MotionEvent.ACTION_DOWN:
                    mTouchAction = true;
                    break;
            }
            if (e.getPointerCount() == 1) {
                if (pc == 1) {
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            x = e.getX();
                            y = e.getY();

                            if (mTouchedSlider) {
                                changeProgressSub(px, x);
                            } else {
                                if (mFitScale < 1) {
                                    if (x - px != 0.f && y - py != 0.f) {
                                        matrix.postTranslate(x - px, y - py);
                                        chop_matrix(matrix);
                                        if (mScale != 1f) {
                                            if (x != px) {
                                                adjustProgress(matrix);
                                            }
                                        }
                                        pmatrix.set(matrix);
                                        mNeedHighImageUpdate = true;
                                    }
                                }
                            }

                            px = x;
                            py = y;
                            break;

                        case MotionEvent.ACTION_DOWN: {
                            float x = e.getX();
                            float y = e.getY();
                            float pos = (x / mScale - mResultDstRect.left) * mScale;
                            RectF lineRect = new RectF(pos - SLIDER_TOUCH_RANGE, 0.f, pos + SLIDER_TOUCH_RANGE, 1.f);
                            float[] values = new float[9];
                            matrix.getValues(values);
                            if (lineRect.contains(values[Matrix.MTRANS_X], 0.f)) {
                                setSliderPressedSub(true);
                            }
                        }
                        // fall through.
                        default:
                            px = e.getX();
                            py = e.getY();
                            pmatrix.set(matrix);
                            break;
                    }
                } else {
                    px = e.getX();
                    py = e.getY();
                }
            } else if (e.getPointerCount() == 2) {
                if (mFitScale < 1) {
                    setSliderPressedSub(false);
                    if (pc == 2) {
                        switch (e.getAction()) {
                            case MotionEvent.ACTION_MOVE:
                                x = (e.getX(0) + e.getX(1)) / 2.F;
                                y = (e.getY(0) + e.getY(1)) / 2.F;
                                r = (float) Math.hypot(e.getX(0) - e.getX(1), e.getY(0) - e.getY(1));

                                float sh = r / pr;//Math.max( r / pr, shrink );

                                if (mScale * sh >= mMaxScale) {
                                    sh = mMaxScale / mScale;
                                }
                                if (mScale * sh <= MIN_SCALE) {
                                    sh = MIN_SCALE / mScale;
                                }

                                matrix.postScale(sh, sh, x, y);
                                matrix.postTranslate(x - px, y - py);

                                float[] values = chop_matrix(matrix);
                                mScale = values[Matrix.MSCALE_X];

                                adjustProgress(matrix);

                                px = x;
                                py = y;
                                pr = r;
                                pmatrix.set(matrix);
                                mNeedHighImageUpdate = true;
                                break;

                            default:
                                px = (e.getX(0) + e.getX(1)) / 2.F;
                                py = (e.getY(0) + e.getY(1)) / 2.F;
                                pr = (float) Math.hypot(e.getX(0) - e.getX(1), e.getY(0) - e.getY(1));
                                break;
                        }
                    } else {
                        px = (e.getX(0) + e.getX(1)) / 2.F;
                        py = (e.getY(0) + e.getY(1)) / 2.F;
                        pr = (float) Math.hypot(e.getX(0) - e.getX(1), e.getY(0) - e.getY(1));
                    }
                }
            }
            //}
            pc = e.getPointerCount();
            return matrix;
        }


        private float[] chop_matrix(Matrix mat) {
            float[] values = new float[9];
            mat.getValues(values);
            final float m_tx = values[Matrix.MTRANS_X];
            final float m_ty = values[Matrix.MTRANS_Y];
            final float m_sx = chop(values[Matrix.MSCALE_X], MIN_SCALE, mMaxScale);
            final float m_sy = chop(values[Matrix.MSCALE_Y], MIN_SCALE, mMaxScale);

            float view_w = mFitImageWidth + (mOffset.x << 1);
            float view_h = mFitImageHeight + (mOffset.y << 1);
            float scaled_view_w = view_w * m_sx;
            float scaled_view_h = view_h * m_sy;
            float scaled_fit_w = mFitImageWidth * m_sx;
            float scaled_fit_h = mFitImageHeight * m_sy;
            float diff_w = (view_w - scaled_view_w);
            float diff_h = (view_h - scaled_view_h);

            float max_tx = maxv(diff_w, -mOffset.x * m_sx);
            float min_tx = minv(diff_w + mOffset.x * m_sx, max_tx);
            float max_ty = maxv(diff_h, -mOffset.y * m_sy);
            float min_ty = minv(diff_h + mOffset.y * m_sy, max_ty);


            if (view_w > scaled_fit_w && view_h < scaled_fit_h) {
                values[Matrix.MTRANS_X] = diff_w / 2;
                values[Matrix.MTRANS_Y] = chop(m_ty, min_ty, max_ty);
            } else if (view_w < scaled_fit_w && view_h > scaled_fit_h) {
                values[Matrix.MTRANS_X] = chop(m_tx, min_tx, max_tx);
                values[Matrix.MTRANS_Y] = diff_h / 2;
            } else if (view_w > scaled_fit_w && view_h > scaled_fit_h) {
                values[Matrix.MTRANS_X] = diff_w / 2;
                values[Matrix.MTRANS_Y] = diff_h / 2;
            } else {
                values[Matrix.MTRANS_X] = chop(m_tx, min_tx, max_tx);
                values[Matrix.MTRANS_Y] = chop(m_ty, min_ty, max_ty);
            }
            values[Matrix.MSCALE_X] = m_sx;
            values[Matrix.MSCALE_Y] = m_sy;
            mat.setValues(values);
            return values;
        }

        private void changeProgressSub(float val1, float val2) {
            float carry = val2 > val1 ? 0.5f : -0.5f;
            changeProgress(mProgress + ((val2 - val1) / mScale + carry));
        }

        private void setSliderPressedSub(boolean pressed) {
            if (pressed) {
                mTouchedSlider = true;
                setSliderPressed(true);
            } else {
                if (mTouchedSlider) {
                    mTouchedSlider = false;
                    setSliderPressed(false);

                    float[] values = new float[9];
                    mMatrix.getValues(values);
                    float trans_x = values[Matrix.MTRANS_X];
                    float left = trans_x == 0f ? trans_x : (trans_x * -1);
                    float progress = mProgress * mScale - left;
                    mProgressPosition = progress / mViewWidth;
                }
            }
        }
    }


    private float chop(float src, float min, float max) {
        return (((min) <= (src)) ? (((src) <= (max)) ? (src) : (max)) : (min));
    }

    private float maxv(float v1, float v2) {
        return (v1 > v2) ? v1 : v2;
    }

    private float minv(float v1, float v2) {
        return (v1 < v2) ? v1 : v2;
    }

    private void adjustProgress(Matrix matrix) {
        float[] values = new float[9];
        matrix.getValues(values);
        float trans_x = values[Matrix.MTRANS_X];
        float line_pos = mProgressPosition;
        float left = trans_x == 0f ? trans_x : (trans_x * -1);
        float right = left + mViewWidth;
        float progress = ((right - left) * line_pos + left) / mScale;
        changeProgress(progress);
    }

    private void drawCenter(Canvas canvas, Matrix matrix) {
        canvas.save();
        if (matrix != null) {
            canvas.setMatrix(matrix);
        }
        if (mBmpLeft != null) {
            canvas.drawBitmap(mBmpLeft, mInputSrcRect, mInputDstRect, null);
        }
        if (mBmpRight != null) {
            canvas.drawBitmap(mBmpRight, mResultSrcRect, mResultDstRect, null);
        }
        mPaintLine.setStrokeWidth(Math.min(Math.max(1.0f, 3.0f / mScale), 10.0f));
        canvas.drawLine(mResultDstRect.left, mResultDstRect.top, mResultDstRect.left, mResultDstRect.bottom, mPaintLine);

        canvas.restore();
    }

    public void doDraw(Matrix matrix) {
        Canvas canvas = mHolder.lockCanvas();
        if (canvas == null) {
            return;
        }
        canvas.drawColor(Color.BLACK);

        drawCenter(canvas, matrix);

        mHolder.unlockCanvasAndPost(canvas);
    }
}
