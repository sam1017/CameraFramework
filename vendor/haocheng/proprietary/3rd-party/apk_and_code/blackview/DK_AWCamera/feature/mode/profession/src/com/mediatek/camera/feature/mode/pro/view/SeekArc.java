package com.mediatek.camera.feature.mode.pro.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.utils.CameraUtil;

public class SeekArc extends View {
    private static int INVALID_PROGRESS_VALUE = -1;
    private static final LogUtil.Tag TAG = new LogUtil.Tag(ProViewCtrl.class.getSimpleName());
    private Action mAction = Action.Collapse;
    private int mMax = 100;
    private int mArcWidth = 2;
    private int mProgressWidth = 4;

    private Paint mArcPaint = new Paint();
    private RectF mArcRect = new RectF();
    private RectF mMiddleArcRect = new RectF();
    private Rect mArcTextBounds = new Rect();
    private Paint mArcTextPaint = new Paint(1);;
    private Matrix mMatrix = new Matrix();
    private String mArcText = null;
    private Context mContext;
    private Drawable mThumb;
    private Drawable[] mFixedLabels;
    private Drawable[] mIndicateLabels;
    private Paint.FontMetrics mFontMetrics;
    private int mArcRadius = 0;
    private int mProgress = 0;
    private float mProgressSweep = 0.0F;
    private final Scroller mScroller;
    private int mAngleOffset;
    private int mAngleRange;
    private int mStartAngle = 0;
    private int mStartDegree = 0;
    private int mSweepAngle = 54;
    private int mCurrentDegree = 180;
    private int mTargetDegree = 180;
    private int mTextSize;
    private int mLabelXPos;
    private int mLabelYPos;
    private int mThumbXPos = 0;
    private int mThumbYPos = 0;
    private int mTranslateX;
    private int mTranslateY;
    private int mPaddingBottom;
    private double mTouchAngle;
    private float mTouchIgnoreRadius;
    private long mAnimationStartTime = 0L;
    private long mAnimationEndTime = 0L;
    private boolean mRoundedEdges = false;
    private boolean mTouchInside = true;
    private boolean mClockwise = false;
    private boolean m_bAnimStart = false;
    private boolean m_bDrawText = false;
    private boolean m_bReverse = false;
    private OnSeekArcChangeListener mOnSeekArcChangeListener;
    private OnLayoutListener mLayouted;
	private Bitmap mBgBitmap;
    private String mTitle;
    //bv wuyonglin add for after iso and shutter speed change not auto exp should unavailable 20200117 start
    private boolean mEnabledispatchTouchEvent = true;
    //bv wuyonglin add for after iso and shutter speed change not auto exp should unavailable 20200117 end

    public static abstract interface OnLayoutListener {
        public abstract void onLayouted();
    }

    public SeekArc(Context context) {
        this(context, null, 0);
    }

    public SeekArc(Context context, AttributeSet attrs) {
        this(context, attrs, 2130772039);
    }

    public SeekArc(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
        mScroller = new Scroller(context);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        mContext = context;
        Resources res = getResources();
        float density = context.getResources().getDisplayMetrics().density;
        int arcColor = res.getColor(R.color.century_seekarc_gray);
        int seekTagColor = res.getColor(R.color.century_seek_tag_color);
        boolean attachText = true;
        mProgressWidth = ((int) (density * mProgressWidth));
        mTextSize = res.getDimensionPixelSize(R.dimen.century_seek_tag_text_size);
        mPaddingBottom = 0;
        mAngleRange = 60;

        //modify by huangfei for resource loss start
        //mBgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.century_seekarc_bg);
        mBgBitmap =  readBitMap(mContext,R.drawable.century_seekarc_bg);
        //modify by huangfei for resource loss end        
		
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekArc, defStyle, 0);
            mThumb = a.getDrawable(R.styleable.SeekArc_thumb);
            if (mThumb != null) {
                int thumbHalfWidth = mThumb.getIntrinsicWidth() / 2;
                int thumbHalfheight = mThumb.getIntrinsicHeight() / 2;
                mThumb.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth, thumbHalfheight);
            }
            mMax = a.getInteger(R.styleable.SeekArc_max, mMax);
            mProgress = a.getInteger(R.styleable.SeekArc_progress, 0);
            mProgressWidth = ((int) a.getDimension(R.styleable.SeekArc_progressWidth, mProgressWidth));
            mArcWidth = ((int) a.getDimension(R.styleable.SeekArc_arcWidth, mArcWidth));
            mStartAngle = a.getInt(R.styleable.SeekArc_startAngle, mStartAngle);
            mSweepAngle = a.getInt(R.styleable.SeekArc_sweepAngle, mSweepAngle);
            arcColor = a.getColor(R.styleable.SeekArc_arcColor, arcColor);
            mTextSize = 20;
            mRoundedEdges = a.getBoolean(R.styleable.SeekArc_roundEdges, mRoundedEdges);
            mTouchInside = a.getBoolean(R.styleable.SeekArc_touchInside, true);
            mClockwise = a.getBoolean(R.styleable.SeekArc_clockwise, true);
            m_bDrawText = a.getBoolean(R.styleable.SeekArc_attachText, false);
            mAngleRange = a.getInt(R.styleable.SeekArc_angleRange, mAngleRange);
            a.getColor(R.styleable.SeekArc_text_color, seekTagColor);
            m_bReverse = a.getBoolean(R.styleable.SeekArc_reverse, true);
            mPaddingBottom = 10;
            a.recycle();
        }

        mProgress = (mProgress > mMax) ? mMax : mProgress;
        mProgress = (mProgress < 0) ? 0 : mProgress;
        mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
        mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;
        mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
        mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;
        LogHelper.d(TAG, "initSeekArc: mStartAngle=" + mStartAngle + ", mSweepAngle=" + mSweepAngle + ", mAngleRange="
                + mAngleRange + ", mStartDegree=" + mStartDegree + ", mTextSize=" + mTextSize + ", m_bReverse="
                + m_bReverse + ", mTouchInside=" + mTouchInside);
        setTouchInSide(true);

        mArcPaint.setColor(arcColor);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mArcWidth);

        if (m_bReverse) {
            setRotation(180.0F);
        }
        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
            mArcTextPaint.setStrokeCap(Paint.Cap.ROUND);
        }
        if (m_bDrawText) {
            mArcTextPaint.setStyle(Paint.Style.FILL);
            mArcTextPaint.setTextSize(mTextSize);
            mArcTextPaint.setColor(-1);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.clipRect(0, 0, getMeasuredWidth(), getMeasuredHeight());
		//drawBG(canvas);
        drawArc(canvas);
        drawFixedLabel(canvas);
        drawThumb(canvas);
        drawText(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = calculateMeasureWidth(widthMeasureSpec);
        int height = calculateMeasureHeight(heightMeasureSpec);
        int leftAngle = (180 - mAngleRange) / 2;
        int n = 4;
        mAngleOffset = leftAngle + n;
        mSweepAngle = mAngleRange - n * 2;

        int thumbMinY = height - mThumb.getIntrinsicHeight() / 2;
        if (mArcTextPaint != null) {
            thumbMinY = (int) (thumbMinY - (mArcTextPaint.descent() - mArcTextPaint.ascent()) / 2.0F);
        }
        int distance = calculateDistance(new Point(width / 2, thumbMinY), new Point(width, thumbMinY / 2));
        double ddd = Math.sin(Math.toRadians(0.25D * mAngleRange));
        int radius = (int) (0.5D + 0.5D * distance / ddd);
        int diameter = radius * 2;
        int left = width / 2 - radius;
        int top = thumbMinY - diameter;
        mArcRect = new RectF(left, top, left + diameter, top + diameter);
        mMiddleArcRect = new RectF(left, top, left + diameter, top + diameter);

        mTranslateX = ((int) mArcRect.centerX());
        mTranslateY = ((int) mArcRect.centerY());
        mThumbXPos = ((int) (radius * Math.cos(Math.toRadians(mAngleOffset))));
        mThumbYPos = ((int) (radius * Math.sin(Math.toRadians(mAngleOffset))));
        mArcRadius = radius;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mLayouted != null) {
            mLayouted.onLayouted();
        }
    }

    @Override
    protected void onFinishInflate() {
        updateThumbPosition();

        super.onFinishInflate();
    }

    private int calculateDistance(Point point1, Point point2) {
        return (int) (0.5D + Math.sqrt((point2.y - point1.y) * (point2.y - point1.y) + (point2.x - point1.x)
                * (point2.x - point1.x)));
    }

    private int calculateMeasureWidth(int widthMeasureSpec) {
        int mode = View.MeasureSpec.getMode(widthMeasureSpec);

        int minn = Math.min(540, 960);
        if ((mode == MeasureSpec.AT_MOST) || (mode == MeasureSpec.EXACTLY)) {
            minn = View.MeasureSpec.getSize(widthMeasureSpec);
        }
        return minn;
    }

    private int calculateMeasureHeight(int heightMeasureSpec) {
        int mode = View.MeasureSpec.getMode(heightMeasureSpec);
        int height = dp2px(mContext, 120);
        if ((mode == MeasureSpec.AT_MOST) || (mode == MeasureSpec.EXACTLY)) {
            height = View.MeasureSpec.getSize(heightMeasureSpec);
        }
        return height;
    }
	
	private void drawBG(Canvas canvas) {
        mMatrix.postRotate(-mCurrentDegree);

        Paint paint = new Paint();
        paint.setFilterBitmap(false);
        paint.setStyle(Paint.Style.STROKE);
        paint.setShader(null);
        paint.setColor(0x66000000);
        paint.setAntiAlias(true);

        paint.setStrokeWidth(120);
        canvas.save();
        canvas.drawArc(mMiddleArcRect, mStartAngle, 180.0F, false, paint);
        canvas.restore();
    }


    private void drawArc(Canvas canvas) {
		mMatrix.postRotate(-mCurrentDegree);
        canvas.drawArc(mArcRect, mStartAngle, 180.0F, false, mArcPaint);
    }

    private void drawThumb(Canvas canvas) {
        int k, n, rotate;
        canvas.save();
        if (mCurrentDegree != mTargetDegree) {
			//
        } else {
            int i = mTranslateX + mThumbXPos;
            int j = mTranslateY + mThumbYPos;

            mMatrix.reset();
            mMatrix.postRotate(-mCurrentDegree);
            mMatrix.postTranslate(i, j);
            canvas.concat(mMatrix);
            mThumb.draw(canvas);
            canvas.restore();
        }
    }

    private void drawFixedLabel(Canvas canvas) {
        if (mFixedLabels == null)
            return;
        int num = mFixedLabels.length;
        Drawable label = null;

        int gap = mSweepAngle / (num - 1);
        int destAngle = mAngleOffset + mSweepAngle;
        int n = mThumb.getIntrinsicHeight() / 4;

        for (int i = 0; i < num; i++) {
            label = mFixedLabels[i];

            if (label != null) {
                canvas.save();
                destAngle = mAngleOffset + i * gap;
                int xPos = (int) (mArcRadius * Math.cos(Math.toRadians(destAngle)));
                int yPos = (int) (mArcRadius * Math.sin(Math.toRadians(destAngle)));
                int i4 = xPos + mTranslateX;
                int i5 = n + (yPos + mTranslateY);
                mMatrix.reset();
                mMatrix.postRotate(-mCurrentDegree);
                mMatrix.postTranslate(i4, i5);
                canvas.concat(mMatrix);
                label.draw(canvas);
                canvas.restore();
            }
        }
    }

    private void drawText(Canvas canvas) {
        if (!m_bDrawText)
            return;
        int thumbHalfWidth = mThumb.getIntrinsicWidth() / 2;
        int thumbHalfheight = mThumb.getIntrinsicHeight() / 2;
        int k = mArcTextBounds.width() / 2;
        int m = mArcTextBounds.height() / 2;
        canvas.save();
        int n = -thumbHalfWidth;
        int i1 = thumbHalfheight + m;
        int i2 = n + (thumbHalfWidth - k);
        int i3 = mTranslateX + mThumbXPos;
        int i4 = mTranslateY + mThumbYPos;
        float[] pts = new float[2];
        pts[0] = i2;
        pts[1] = i1;
        Matrix localMatrix = canvas.getMatrix();
        canvas.translate(i3, i4);
        canvas.rotate(-mCurrentDegree);
        localMatrix.mapPoints(pts);
        if (mArcTextPaint != null && mArcText != null) {
            canvas.drawText(mArcText, pts[0], pts[1], mArcTextPaint);
        }
        canvas.restore();
    }

    private int getProgressForAngle(double proress) {
        int touchProgress = (int) Math.round((proress - mAngleOffset) * valuePerDegree());
        touchProgress = (touchProgress < 0) ? INVALID_PROGRESS_VALUE : touchProgress;
        touchProgress = (touchProgress > mMax) ? INVALID_PROGRESS_VALUE : touchProgress;
        return touchProgress;
    }

    private double getTouchDegrees(float xPos, float yPos) {
        float f = xPos - mTranslateX;
        double angle = Math.toDegrees(Math.atan2(yPos - mTranslateY, f));
        if (angle < 0.0D) {
            angle += 360.0D;
        }
        return (angle - this.mStartAngle);
    }

    private boolean ignoreTouch(float xPos, float yPos) {
        boolean ignore = false;
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
        if (touchRadius < mTouchIgnoreRadius) {
            ignore = true;
        }
        return ignore;
    }

    private boolean isAcceptTouchEvent(float xPos, float yPos) {
        double d = getTouchDegrees(xPos, yPos);
        float f1 = (float) (mArcRadius * Math.cos(Math.toRadians(d)));
        float f2 = (float) (mArcRadius * Math.sin(Math.toRadians(d)));
        float f3 = f1 + mTranslateX;
        float f4 = f2 + mTranslateY;
        return (float) Math.sqrt((f4 - yPos) * (f4 - yPos) + (f3 - xPos) * (f3 - xPos)) <= mThumb.getIntrinsicWidth() / 2;
    }

    private void onProgressRefresh(int progress, boolean fromUser) {
        updateProgress(progress, fromUser);
    }

    private void onStartTrackingTouch() {
        if (mOnSeekArcChangeListener != null)
            mOnSeekArcChangeListener.onStartTrackingTouch(this);
    }

    private void onStopTrackingTouch() {
        if (mOnSeekArcChangeListener != null)
            mOnSeekArcChangeListener.onStopTrackingTouch(this);
    }

    private void updateOnTouch(MotionEvent ev) {
        if (ignoreTouch(ev.getX(), ev.getY()))
            return;
        setPressed(true);
        mTouchAngle = getTouchDegrees(ev.getX(), ev.getY());
        onProgressRefresh(getProgressForAngle(mTouchAngle), true);
    }

    private void updateProgress(int progress, boolean fromUser) {
        if (progress == INVALID_PROGRESS_VALUE) {
            return;
        }

        if ((mOnSeekArcChangeListener != null) && (progress != mProgress)) {
            mOnSeekArcChangeListener.onProgressChanged(this, progress, fromUser);
        }

        progress = (progress > mMax) ? mMax : progress;
        progress = (mProgress < 0) ? 0 : progress;

        mProgress = progress;
        mProgressSweep = (progress / valuePerDegree() + mAngleOffset);
        updateThumbPosition();
        invalidate();
    }

    private void updateThumbPosition() {
        int thumbAngle = (int) (mStartAngle + mProgressSweep);
        mThumbXPos = ((int) (mArcRadius * Math.cos(Math.toRadians(thumbAngle))));
        mThumbYPos = ((int) (mArcRadius * Math.sin(Math.toRadians(thumbAngle))));
    }

    private float valuePerDegree() {
        return (float) mMax / mSweepAngle;
    }

    public void collapse() {
        if (!m_bAnimStart) {
            mAction = Action.Collapse;
            setDrawingCacheEnabled(true);
            m_bAnimStart = true;
            mScroller.startScroll(0, -getMeasuredHeight(), 0, getMeasuredHeight(), 30);
            mOnSeekArcChangeListener.onStartAnimation(this);
            invalidate();
        }
    }

    public void computeScroll() {
        LogHelper.d(TAG, "computeScroll!!!");
        super.computeScroll();
        if ((mScroller.isFinished()) && (this.m_bAnimStart)) {
            m_bAnimStart = false;
            if (mAction == Action.Collapse)
                setVisibility(4);
            setDrawingCacheEnabled(false);
            setProgress(mProgress);
            mOnSeekArcChangeListener.onEndAnimation(this);
        }
        while (!mScroller.computeScrollOffset())
            return;
        scrollTo(0, mScroller.getCurrY());
        postInvalidate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        //bv wuyonglin add for after iso and shutter speed change not auto exp should unavailable 20200117 start
        if (!mEnabledispatchTouchEvent) {
            return true;
        }
        //bv wuyonglin add for after iso and shutter speed change not auto exp should unavailable 20200117 end
        if ((e.getAction() == 0) && (!isAcceptTouchEvent(e.getX(), e.getY())))
            return false;
        return super.dispatchTouchEvent(e);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if ((mThumb != null) && (mThumb.isStateful())) {
            int[] state = getDrawableState();
            mThumb.setState(state);
        }
        invalidate();
    }

    public void expand() {
        if (!m_bAnimStart) {
            mAction = Action.Expand;
            setVisibility(0);
            setDrawingCacheEnabled(true);
            m_bAnimStart = true;
            mScroller.startScroll(0, getMeasuredHeight(), 0, -getMeasuredHeight(), 30);
            mOnSeekArcChangeListener.onStartAnimation(this);
            invalidate();
        }
    }

    public int getArcWidth() {
        return mArcWidth;
    }

    public int getProgressWidth() {
        return mProgressWidth;
    }

    //add by huang fei for MF start
    public int getProgress(){
        return mProgress;
    }
    //add by huang fei for MF end

    public int getStartAngle() {
        return mStartAngle;
    }

    public int getSweepAngle() {
        return mSweepAngle;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        LogHelper.d(TAG, "SeekArc onTouchEvent.");
        switch (e.getAction()) {
        case MotionEvent.ACTION_DOWN:
            onStartTrackingTouch();
            updateOnTouch(e);
            break;
        case MotionEvent.ACTION_MOVE:
            updateOnTouch(e);
            break;
        case MotionEvent.ACTION_UP:
            onStopTrackingTouch();
            setPressed(false);
            break;
        case MotionEvent.ACTION_CANCEL:
            onStopTrackingTouch();
            setPressed(false);
            break;
        }
        return true;
    }

    public void setArcWidth(int width) {
        mArcWidth = width;
        mArcPaint.setStrokeWidth(width);
    }

    public void setFixedLabel(Drawable[] fixedLabels) {
        if (fixedLabels == null)
            return;
        this.mFixedLabels = fixedLabels;
        postInvalidate();
    }

    public void setIndicateIndex(int index) {
        if ((mFixedLabels != null) && (mIndicateLabels != null)) {
            int length = mFixedLabels.length - 1;
            if ((index < 0) || (index > length)) {
                return;
            }
            mThumb = mIndicateLabels[index];
            for (int j = 0; j < mFixedLabels.length; j++) {
                if (j == index) {
                    mFixedLabels[j].setAlpha(0);
                } else {
                    mFixedLabels[j].setAlpha(255);
                }
            }
        }
        postInvalidate();
    }

    public void setIndicateLabel(Drawable[] labels) {
        if (labels == null)
            return;
        this.mIndicateLabels = labels;
        postInvalidate();
    }

    public void setMaxProgress(int max) {
        this.mMax = max;
        invalidate();
    }

    public void setOnSeekArcChangeListener(OnSeekArcChangeListener l) {
        this.mOnSeekArcChangeListener = l;
    }

    public void setOrientation(int paramInt) {
		//
    }

    public void setProgress(int progress) {
        LogHelper.d(TAG, "setProgress!!! progress=" + progress);
        updateProgress(progress, false);
    }

    public void setProgressWidth(int width) {
        LogHelper.d(TAG, "setProgressWidth!!! width=" + width);
        mProgressWidth = width;
        mArcTextPaint.setStrokeWidth(width);
    }

    public void setReverse(boolean reverse) {
        m_bReverse = reverse;
    }

    public void setRoundedEdges(boolean isEnabled) {
        mRoundedEdges = isEnabled;
        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
            mArcTextPaint.setStrokeCap(Paint.Cap.ROUND);
        } else {
            mArcPaint.setStrokeCap(Paint.Cap.SQUARE);
            mArcTextPaint.setStrokeCap(Paint.Cap.SQUARE);
        }
    }

    public void setStartAngle(int startAngle) {
        mStartAngle = startAngle;
        updateThumbPosition();
    }

    public void setSweepAngle(int sweepAngle) {
        mSweepAngle = sweepAngle;
        updateThumbPosition();
    }

    public void setText(String text) {
        if ((mArcTextPaint == null) || (TextUtils.isEmpty(text))) {
            m_bDrawText = false;
        } else {
            mArcText = text;
            mArcTextPaint.getTextBounds(mArcText, 0, mArcText.length(), mArcTextBounds);
            mFontMetrics = mArcTextPaint.getFontMetrics();
            // m_bDrawText = true;
            postInvalidate();
        }
    }

    public void setTouchInSide(boolean enable) {
        int centerH = this.mThumb.getIntrinsicHeight() / 2;
        int centerW = this.mThumb.getIntrinsicWidth() / 2;
        this.mTouchInside = enable;
        if (this.mTouchInside) {
            this.mTouchIgnoreRadius = (this.mArcRadius / 4.0F);
            return;
        }
        this.mTouchIgnoreRadius = (this.mArcRadius - Math.min(centerW, centerH));
    }

    public void setOnLayoutListener(OnLayoutListener listener) {
        this.mLayouted = listener;
    }

    public static enum Action {
        Expand, Collapse
    }

    private int dp2px(Context context, int dip) {
        return (int) (0.5F + dip * context.getResources().getDisplayMetrics().density);
    }

    public void setTitle(String title){
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void recycleBitmap(){
        if(!mBgBitmap.isRecycled()){
            mBgBitmap.recycle();
            mBgBitmap = null;
        }
    }

    public static abstract interface OnSeekArcChangeListener {
        public abstract void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser);

        public abstract void onStartAnimation(SeekArc seekArc);

        public abstract void onEndAnimation(SeekArc seekArc);

        public abstract void onStartTrackingTouch(SeekArc seekArc);

        public abstract void onStopTrackingTouch(SeekArc seekArc);
    }

    //add by huangfei for resource loss start
    public Bitmap readBitMap(Context context, int resId){
        LogHelper.d(TAG, "readBitMap");
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is,null,opt);
    }
    //add by huangfei for resource loss end

    //bv wuyonglin add for after iso and shutter speed change not auto exp should unavailable 20200117 start
    public void setArcPaintColor(boolean enable) {
        if (mEnabledispatchTouchEvent != enable) {
            mEnabledispatchTouchEvent = enable;
            if (enable) {
                mArcPaint.setColor(mContext.getResources().getColor(R.color.century_seekarc_gray));
		mThumb.setAlpha(255);
            } else {
                mArcPaint.setColor(mContext.getResources().getColor(R.color.century_seekarc_gray_unavailable));
		mThumb.setAlpha(102);
            }
            invalidate();
        }
    }
    //bv wuyonglin add for after iso and shutter speed change not auto exp should unavailable 20200117 end
}
