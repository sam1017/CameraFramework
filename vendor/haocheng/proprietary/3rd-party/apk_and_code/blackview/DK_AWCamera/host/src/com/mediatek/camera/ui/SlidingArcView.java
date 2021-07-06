package com.mediatek.camera.ui;

import android.view.ViewGroup;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import com.mediatek.camera.Config;
import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.utils.CameraUtil;

/**
 * Created by aland on 17-5-27.
 */

public class SlidingArcView extends ViewGroup {
    public static String TAG = "SlidingArcView";
    private String titles[] = null;
    private int indexOfView = 37;  //  12
    private static final int ID_OF_VIEW = R.drawable.sliding_view_rect_more;
    private static final int ID_OF_NORMAL = R.drawable.sliding_view_rect_normal;
    private static final int ID_OF_MORE = R.drawable.sliding_view_rect_more;
    private static final int ID_OF_MOST = R.drawable.sliding_view_rect_most;
    private static final int ID_TYPE_NORMAL = 0;
    private static final int ID_TYPE_MORE = 1;
    private static final int ID_TYPE_MOST = 2;
    private static final int SIGN_VIEW_WIDTH = 5;
    private static final int SIGN_VIEW_HEIGHT = 30;
    private static final int SIGN_VIEW_SELECT_WIDTH = 7;
    private static final int SIGN_VIEWSELECT_HEIGHT = 60;

    private List<SignView> views = new ArrayList<>();
    private SignView MiddleView = null;
    private final int zoomRation = 11;
    private Bitmap mBackGround;
    private Context mContext;
    private int mSize;
    private int lastX = 0;
    private int downX = 0;
    private int downY = 0;
    private int mArcMargins = 50;
    private boolean isAnimationIng = false;
    private Rect mChangeImageBackgroundRect = null;
    private boolean startScroll = false;

    private int hctZoomScale = 2;    //add by lishuo for max zoom 2.0  *#*#default is 2#*#*

    //add by huangfei for zoom switch start
    private ArrayList<String> mZoomLevelValue = null;
    //add by huangfei for zoom switch end

    public interface QTScrollListener {
        void onTouch(int index,float direction);

        void onFingerUp(boolean up);
    }

    public SlidingArcView(Context context) {
        super(context, null);
    }

    public SlidingArcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    //add by huangfei for zoom switch start
    public void setZoomLevelValue(ArrayList<String> list){
        mZoomLevelValue = list;
        if(indexOfView == mZoomLevelValue.size()){
            hctZoomScale = 1;
        }else{
            hctZoomScale = 2;
        }
    }
    //add by huangfei for zoom switch end

    public void setChooseIndex(int index){
        isChooseIndex = index;
    }

    public SlidingArcView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(SlidingArcViewUtils.getScreenW(), mSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.i(TAG,"onLayout l = " + l + " t = " + t + " r = " + r + " b = " + b);
        Log.i(TAG," width = " + (r - l) + " height = " + (b - t));
        CentX = (r - l)/2;
        CentY = (b - t)/2;
        Log.i(TAG, "   onLayout   slidingArcView   CentX = " + CentX + " CentY = " + CentY);
    }

    private void init() {
        Log.i(TAG, "   init   slidingArcView   ");
        mSize = (int) mContext.getResources().getDimension(R.dimen.sliding_rectview_height);
        //titles = mContext.getResources().getStringArray(R.array.zoom_ratios);
        mBackGround = ((BitmapDrawable) mContext.getDrawable(R.drawable.sliding_arcview_bg)).getBitmap();
/*
        CentX = SlidingArcViewUtils.getScreenW() / 2;
        CentY = getHeight()/2 - 20; //SlidingArcViewUtils.getScreenW() / 2 + 60;//SlidingArcViewUtils.getScreenH() / 2 + 100;
*/
        //Log.i(TAG, "   init   slidingArcView   CentX = " + CentX + " CentY = " + CentY);
        RADIUS = SlidingArcViewUtils.getScreenW() / 2 - 20;
        //Log.i(TAG, "   init   RADIUS   " + RADIUS);

        View view = new View(mContext);
        MiddleView = new SignView(view, 1000);
        this.addView(view);

        for (int i = 0, len = indexOfView; i < len; i++) {
            View v = new View(mContext);
            SignView signView = new SignView(v, i);
            views.add(signView);
            this.addView(v);
            Log.i(TAG,"i = " + i + " width = " + v.getWidth() + " height = " + v.getHeight());
        }
        //setBackgroundResource(R.drawable.sliding_arcview_bg);
        setBackgroundColor(getResources().getColor(android.R.color.transparent));
        this.setClickable(true);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //drawBg(canvas);
        for (int i = 0; i < indexOfView; i++) {
            //views.get(i).initView(i);
            views.get(i).flush();
        }
        MiddleView.flush();
    }

    private void drawBg(Canvas canvas) {
        float x = getWidth() ;
        float y = getHeight();
        Log.i(TAG,"drawBg x = " + x + " y = " + y);
        RectF oval = new RectF(0, 0, getWidth(), getHeight());
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAlpha(100);
        paint.setAntiAlias(true);

        //canvas.drawArc(oval, 0, -180, false, paint);
        Log.i(TAG,"drawBg oval = " + oval);
        canvas.drawRect(oval, paint);

        /*Bitmap newBitmp = Bitmap.createScaledBitmap(mBackGround, getWidth(), getHeight(), false);
        if (newBitmp == null) {
            return;
        }
        BitmapShader mBitmapShader = new BitmapShader(newBitmp, Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);
        Bitmap dest = Bitmap.createBitmap(SlidingArcViewUtils.getScreenW(),mSize, Bitmap.Config.ARGB_8888);
        if (dest == null) {
            return;
        }
        Canvas c = new Canvas(dest);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(mBitmapShader);
        c.drawCircle(getWidth()/2, SlidingArcViewUtils.getScreenH() - 100, RADIUS+mArcMargins, paint);
        Log.i(TAG,"   init   RADIUS   "+RADIUS+"CentX " +CentX+" CentY - viewTopChange"
                +(CentY - viewTopChange) +"RADIUS"+RADIUS+mArcMargins);
        canvas.drawBitmap(dest, 0, 0, paint);

        newBitmp.recycle();
        newBitmp = null;
        dest.recycle();
        dest = null;*/
    }

    QTScrollListener qtScrollListener;
    QTItemClickListener qtItemClickListener;

    public interface QTItemClickListener {
        void onClick(View v, int index);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                /* add by bv liangchangwei for fixbug 2047 20200904 start -*/
                if(!startScroll){
                    startScroll = true;
                    return false;
                }
                /*--add by bv liangchangwei for fix bug 3752--*/
                if(CameraUtil.isVideo_opening){
                    return false;
                }
                /*--add by bv liangchangwei for fix bug 3752--*/
                /* add by bv liangchangwei for fixbug 2047 20200904 end -*/
/*                boolean inArea = isInChangeImageZone(this, (int)event.getX(), (int)event.getY());
                Log.i(TAG, " slidingView  onTouchEvent inArea: " + inArea);
                if(!inArea){
                    return true;
                }*/
                float direction = event.getX() - lastX;
                flushViews((int) event.getX() - lastX);

                lastX = (int) event.getX();
                invalidate();
                //int index = (int) (getChooseViewIndex() / hctZoomScale);//modify by lishuo for max zoom 2.0
                Log.i(TAG,"slidingView  onTouchEvent index = " + getChooseViewIndex() + " direction = " + direction + " isChooseIndex = " + isChooseIndex);
                qtScrollListener.onTouch(isChooseIndex,direction);
                return false;
            case MotionEvent.ACTION_UP:
                qtScrollListener.onFingerUp(true);
                Log.i(TAG,"MotionEvent.ACTION_UP");
                return true;
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG,"MotionEvent.ACTION_DOWN");
                qtScrollListener.onFingerUp(false);
                downX = lastX = (int) event.getX();
                downY = (int) event.getY();
                return true;
        }
        return super.onTouchEvent(event);
    }

    public void refreshView(int dis) {
        flushViews(dis - views.get(0).getCentX());
        invalidate();
    }

    private void flushViews(int scrollX) {
        boolean isScroll = true;//if the scroll is false the view will not scroll

        if (scrollX < 0 && views.get(indexOfView - 1).getCentX() + scrollX <= CentX) {
            isScroll = false;
        }
        if (scrollX > 0 && views.get(0).getCentX() + scrollX >= CentX) {
            isScroll = false;
        }
        if (isScroll) {
            for (SignView view : views) {
                view.scroll(scrollX);
            }
        }
        MiddleView.flush();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        Log.i(TAG, " slidingView  onVisibilityChanged  " + visibility);
        /* add by bv liangchangwei for fixbug 2047 20200904 start -*/
        if(visibility == INVISIBLE){
            startScroll = false;
        }
        /* add by bv liangchangwei for fixbug 2047 20200904 end -*/
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

    public boolean isAnimationIng() {
        return isAnimationIng;
    }

    public int getArcMargins() {
        return mArcMargins;
    }

    public void setQtScrollListener(QTScrollListener listener) {
        this.qtScrollListener = listener;
    }

    private int viewTopChange = SlidingArcViewUtils.dp2px(20f);
    private SignView leftView;
    private SignView rightView;
    private int CentX;
    private int CentY;
    private int RADIUS;
    private int isChooseIndex = 4;
    private int sliding_size_of_view = 17;
    private int CentCount = 5;
    private SignView chooseView;

    private class SignView {
        private View view;
        private String title;
        private int centX;
        private int centY;
        private int index;
        private int size = 4;
        private int width = 4; //(SlidingArcViewUtils.getScreenW()) / 20; // 20
        private int height = 20;

        private boolean isChoose = false;
        private int angle = 4;
        private int maxAngle = 90;

        public SignView(View v, final int index) {

            this.index = index;
            this.view = v;
            this.title = ""/*titles[index]*/;
            if (index == 0) {
                leftView = this;
            }
            if (index == indexOfView - 1) {
                rightView = this;
            }
            if (index == isChooseIndex) {
                isChoose = true;
                chooseView = this;
            }
            if(index == 0 || (index + 1) %5 == 0){
                view.setBackgroundResource(ID_OF_MORE);
                this.width = 3;//(int) mContext.getResources().getDimension(R.dimen.sliding_size_of_view);
                this.height = 20;
                isChoose = false;
            }else if(index == 1000){
                view.setBackgroundResource(ID_OF_MOST);
                this.width = 4;//(int) mContext.getResources().getDimension(R.dimen.sliding_size_of_view);
                this.height = 40;
                isChoose = false;
            }else{
                view.setBackgroundResource(ID_OF_NORMAL);
                this.width = 3;//(int) mContext.getResources().getDimension(R.dimen.sliding_size_of_view);
                this.height = 20;
                isChoose = false;
            }
            initView(index);
        }

        public void initView(int index) {
            if(index == 1000){
                centX = CentX;
            }else if(index < isChooseIndex){
                centX = CentX - (isChooseIndex - index)*sliding_size_of_view;
            }else{
                centX = CentX + (index - isChooseIndex)*sliding_size_of_view;
            }
            centY = CentY;
            //centX = (int) (CentX + RADIUS * Math.cos((maxAngle - index * angle) * Math.PI / 180));
            //centY = (int) (CentY - RADIUS * Math.sin((maxAngle - index * angle) * Math.PI / 180));
            //Log.i(TAG,"initView index = " + index + " centX = " + centX + " centY = " + centY);
        }

        public void scroll(int scrollX) {
            //Log.i(TAG," + scroll index = " + this.index + " scrollX = " + scrollX + " centX = " + this.centX);
            if(scrollX > sliding_size_of_view){
                this.centX = this.centX + sliding_size_of_view;
            }else if(scrollX < -sliding_size_of_view){
                this.centX = this.centX - sliding_size_of_view;
            }else{
                this.centX = this.centX + scrollX;
            }

            if(Math.abs((CentX - this.centX)) < sliding_size_of_view/2){
                isChooseIndex = this.index;
                isChoose = true;
                chooseView = this;
            }else{
                isChoose = false;
            }
            if(Math.abs((CentX - this.centX)) <= 4){
                this.view.setVisibility(INVISIBLE);
            }else{
                this.view.setVisibility(VISIBLE);
            }

            //Log.i(TAG," - scroll index = " + this.index + " scrollX = " + scrollX + " centX = " + this.centX + " ischoose = " + isChoose + " isChooseIndex = " + isChooseIndex);
/*
            if (scrollX > 0) {
                maxAngle -= 2;
                this.centX = (int) (CentX + RADIUS * Math.cos((maxAngle - index * angle) * Math.PI / 180));
                this.centY = (int) (CentY - RADIUS * Math.sin((maxAngle - index * angle) * Math.PI / 180));
            } else if (scrollX < 0) {
                maxAngle += 2;
                this.centX = (int) (CentX + RADIUS * Math.cos((maxAngle - index * angle) * Math.PI / 180));
                this.centY = (int) (CentY - RADIUS * Math.sin((maxAngle - index * angle) * Math.PI / 180));
            }
*/
        }

        public int getCentX() {
            return centX;
        }

        public int getCentY() {
            return centY;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public View getView() {
            return view;
        }

        public String toString() {
            return "{ " + title + "(" + index + ") " + centX + " : " + centY + " }";
        }

        public void flush() {
            view.layout(centX - (width - 2), centY - height / 2, centX + 2, centY + height / 2);
            if (index == isChooseIndex) {
                isChoose = true;
                chooseView = this;
            }

            this.width = SIGN_VIEW_WIDTH;//(int) mContext.getResources().getDimension(R.dimen.sliding_size_of_view);
            this.height = SIGN_VIEW_HEIGHT;
            view.setAlpha(1.0f);
            isChoose = false;

            if(indexOfView >=37){   // front camera
                if(index == 0 || ((index-4) % 10 == 0 && index < 30) || index == indexOfView -1){
                    view.setBackgroundResource(ID_OF_MORE);

                }else if(index < indexOfView){
                    view.setBackgroundResource(ID_OF_NORMAL);
                }
            }else{ // back camera
                if((index % 10 == 0 && index < 30) || index == indexOfView -1){
                    view.setBackgroundResource(ID_OF_MORE);
                }else if(index < indexOfView){
                    view.setBackgroundResource(ID_OF_NORMAL);
                }
            }

            if(index == 1000){
                view.setBackgroundResource(ID_OF_MOST);
                this.width = SIGN_VIEW_SELECT_WIDTH;//(int) mContext.getResources().getDimension(R.dimen.sliding_size_of_view);
                this.height = SIGN_VIEWSELECT_HEIGHT;
                isChoose = false;
            }
            //Log.i(TAG,"   SignView  flush " + SignView.this.toString() + " index = " + this.index + " isChoose = " + isChoose + " isChooseIndex = " + isChooseIndex);
        }

        public void setMaxAngle(int maxAngle) {
            this.maxAngle = maxAngle;
        }

        public double getMaxAngle() {
            return this.maxAngle;
        }

        public void flushViewsByScale(int mAngle, int mIndex) {
/*            maxAngle = 90 + mAngle;
            centX = (int) (CentX + RADIUS * Math.cos((maxAngle - mIndex * angle) * Math.PI / 180));
            centY = (int) (CentY - RADIUS * Math.sin((maxAngle - mIndex * angle) * Math.PI / 180));*/
            //Log.i(TAG,"++ flushViewsByScale mIndex = " + mIndex + " index = " + this.index + " centX = " + centX + " isChooseIndex = " + isChooseIndex);
            //isChooseIndex = mIndex;
            if(mIndex == 1000){
                centX = CentX;
            }else if(mIndex < isChooseIndex){
                centX = CentX - (isChooseIndex - mIndex)*(int)mContext.getResources().getDimension(R.dimen.sliding_size_of_view);
            }else{
                centX = CentX + (mIndex - isChooseIndex)*(int)mContext.getResources().getDimension(R.dimen.sliding_size_of_view);
            }
            centY = CentY;
            //Log.i(TAG," -- flushViewsByScale centX = " + centX);
        }
    }

    public int getChooseViewIndex() {
        for (int i = 0; i < indexOfView; i++) {
            if (views.get(i).isChoose) {
                //CentCount = i;
                return i;
            }
        }
        return 0;
    }

    public void setIndexOfView(int index){
        Log.i(TAG,"setIndexOfView index = " + index + " indexOfView = " + indexOfView);
        if(indexOfView != index){
            indexOfView = index;
            for(int i =0; i< views.size(); i++){
                this.removeView(views.get(i).view);
            }
            views.clear();
            for (int i = 0, len = indexOfView; i < len; i++) {
                View v = new View(mContext);
                SignView signView = new SignView(v, i);
                views.add(signView);
                this.addView(v);
            }
        }
    }

    public void flushViewsByIndex(int index) {
        int mAngle = index * hctZoomScale * 4;//modify by lishuo for max zoom 2.0
        for (int i = 0; i < indexOfView; i++) {
            views.get(i).flushViewsByScale(mAngle, i);
        }
        MiddleView.flushViewsByScale(mAngle,1000);
    }

    
    private boolean isInChangeImageZone(View view, int x, int y) {
        if (null == mChangeImageBackgroundRect) {
            mChangeImageBackgroundRect = new Rect();
        }
        view.getDrawingRect(mChangeImageBackgroundRect);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        mChangeImageBackgroundRect.left = location[0];
        mChangeImageBackgroundRect.top = location[1];
        mChangeImageBackgroundRect.right = mChangeImageBackgroundRect.right + location[0];
        mChangeImageBackgroundRect.bottom = mChangeImageBackgroundRect.bottom + location[1];
        int distance = (int) Math.sqrt((Math.pow(x - CentX, 2) + Math.pow(y - CentY, 2)));            
        if(y>0 && y<(mChangeImageBackgroundRect.bottom-mChangeImageBackgroundRect.top) && Math.abs(distance) <= RADIUS + 20){
            return true;
        }
        return false;
    }
}
