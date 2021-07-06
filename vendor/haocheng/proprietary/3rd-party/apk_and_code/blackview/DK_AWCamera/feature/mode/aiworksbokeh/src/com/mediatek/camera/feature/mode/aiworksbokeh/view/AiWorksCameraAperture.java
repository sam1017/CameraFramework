package com.mediatek.camera.feature.mode.aiworksbokeh.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.util.Log;
import android.widget.SeekBar.OnSeekBarChangeListener;
import java.lang.Exception;

import com.mediatek.camera.R;


public class AiWorksCameraAperture extends RelativeLayout implements OnSeekBarChangeListener, AiWorksApertureView.ApertureChanged {
    private static final String TAG = "GangyunAiWorksCameraAperture";

    public static final int BOKEH_APERTUREVIEW_ALPHA = 150;

    private AiWorksApertureView mApertureView;
    private AiWorksFilterSeekBar mFilterSeekBar, mFilterSeekBar2;
    private Handler mHandler;
    private OnProgressChangedListener mListener;
    private final static int ON_HIDE_VIEW = 0;
    //private int seekbarmax = 80;
    //private int progress = 0;
    public int mApertureViewWidth = 150;
    private boolean isViewShow = false;
    private final int viewShowTime = 3000;
    public int mSeekBarViewWidth = 120;
    private int minRadius = 0;
    private int maxRadius = 8;;
    private int mRadius = 6;

    public AiWorksCameraAperture(Context context, AttributeSet attrs) {
        super(context, attrs);
        try {
            initView(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AiWorksCameraAperture() {
        super(null);
    }

    public AiWorksCameraAperture(Context context) {
        super(context);
        try {
            initView(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public AiWorksCameraAperture(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        try {
            initView(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView(Context context) throws Exception {
        mApertureView = new AiWorksApertureView(context);
        mApertureView.setId(R.id.bokeh_aperture);
        mFilterSeekBar = (AiWorksFilterSeekBar) LayoutInflater.from(context).inflate(R.layout.aiworks_bokeh_seekbar, this, false);//new FilterSeekBar(context);
        mFilterSeekBar.setOnSeekBarChangeListener(this);
        mFilterSeekBar.setId(R.id.bokeh_aperture_seekbar);
        LayoutParams lp_seekbar = new LayoutParams(dip2px(context, 27), dip2px(context, 132));
        mFilterSeekBar2 = (AiWorksFilterSeekBar) LayoutInflater.from(context).inflate(R.layout.aiworks_bokeh_seekbar, this, false);
        mFilterSeekBar2.setOnSeekBarChangeListener(this);
        mFilterSeekBar2.setId(R.id.bokeh_aperture_seekbar);
        LayoutParams lp_seekbar2 = new LayoutParams(dip2px(context, 27), dip2px(context, 132));
        LayoutParams lp_aperture = new LayoutParams(dip2px(context, (float)62.75), dip2px(context, (float)62.75));
		lp_aperture.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(mApertureView, lp_aperture);
        lp_seekbar.addRule(RelativeLayout.RIGHT_OF, R.id.bokeh_aperture);
        lp_seekbar2.addRule(RelativeLayout.LEFT_OF, R.id.bokeh_aperture);
        addView(mFilterSeekBar2, lp_seekbar2);
        addView(mFilterSeekBar, lp_seekbar);
        mHandler = new MainHandler(context.getMainLooper());
    }

    public void convertSeekbarLeft(boolean left) {
        if (left) {
			mFilterSeekBar2.setVisibility(android.view.View.VISIBLE);
			mFilterSeekBar.setVisibility(android.view.View.GONE);
		}else{
			mFilterSeekBar2.setVisibility(android.view.View.GONE);
			mFilterSeekBar.setVisibility(android.view.View.VISIBLE);
		}
    }

    public int getApertureViewWidth() {
        return mApertureViewWidth;
    }

    public interface OnProgressChangedListener {
        void onProgressChanged(int arg1);
    }

    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        mListener = listener;
        mApertureView.setApertureChangedListener(this);
    }


    public void setApertureViewWidth(int width) {
        if (mApertureView != null) {
            ViewGroup.LayoutParams layout = mApertureView.getLayoutParams();
	    android.util.Log.d("geek","layout ="+layout);
	    if (layout != null) {
            layout.width = width;
            layout.height = width;
	    }
            mApertureViewWidth = width;
        }
    }


    public void setBokehValue(int max, int value) {
        if (mFilterSeekBar != null) {
            mFilterSeekBar.setMax(max);
            mFilterSeekBar.setProgressAndThumb(value);
            mFilterSeekBar2.setMax(max);
            mFilterSeekBar2.setProgressAndThumb(value);
        }
    }

    private class MainHandler extends Handler {

        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // this msg just used for VFB,so if you want use cFB,please be
                // careful
                case ON_HIDE_VIEW:
                    hideView();
                    break;

                default:
                    break;
            }
        }
    }

    public void hideView() {
        Log.e(TAG, "[hideView]");
        isViewShow = false;
        this.setVisibility(View.GONE);
    }

    public void showView() {
        isViewShow = true;
        int progress = mFilterSeekBar.getProgress();
        Log.e(TAG, "[showView]x:" + getX() + " y:" + getY() + " progress=" + progress);;
        mApertureView.setApertureAlpha(BOKEH_APERTUREVIEW_ALPHA);
        setApertureValue(mFilterSeekBar.getProgress(), true);
        mHandler.removeMessages(ON_HIDE_VIEW);
        this.setVisibility(View.VISIBLE);
        mFilterSeekBar.setProgressAndThumb(progress);
        //add by huangfei for bokeh position adjust start
        mFilterSeekBar2.setProgressAndThumb(progress);
         //add by huangfei for bokeh position adjust end
        mHandler.sendEmptyMessageDelayed(ON_HIDE_VIEW, viewShowTime);
    }


    @Override
    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
        mApertureView.setApertureAlpha(BOKEH_APERTUREVIEW_ALPHA);
        setApertureValue(arg1, true);
        mRadius = arg1;
        if (mHandler != null) {
            mHandler.removeMessages(ON_HIDE_VIEW);
        }
        //mFilterSeekBar2.setProgressAndThumb(arg1);
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub

        if(mListener!=null)
            mListener.onProgressChanged(mRadius);

        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(ON_HIDE_VIEW, viewShowTime);
        }
    }

    @Override
    public void onApertureChanged(float newapert) {
        // TODO Auto-generated method stub
        Log.e(TAG, "[onApertureChanged] newapert=" + newapert + " isViewShow=" + isViewShow);
        if (isViewShow) {
            //mListener.onGyApertureChanged((int) (newapert * 100));
            mHandler.removeMessages(ON_HIDE_VIEW);
            mHandler.sendEmptyMessageDelayed(ON_HIDE_VIEW, viewShowTime);
        }

    }

    private void setApertureValue(int value, boolean isShowView) {
        if (true) {
            mApertureView.setCurrentApert((float)value / (maxRadius - minRadius), isShowView);
        } else {
            mApertureView.setCurrentApert(1- ((float)value / (maxRadius - minRadius)), isShowView);
        }
    }

    private int mXPos, mYPost;

    public void setPos(int x, int y) {
        mXPos = x;
        mYPost = y;
    }

	public int getApertureCenterX() {
		int left = mApertureView.getLeft();
		int center_x = left + mApertureView.getWidth() / 2;
		//Log.e(TAG,  "gangyun_chan[getApertureCenterX] center_x:"+center_x);
		return center_x;
	}

	public int getApertureCenterY() {
		int top = mApertureView.getTop();
		int center_y = top + mApertureView.getHeight() / 2;
		return center_y;
	}

	public float getApertureCenterDisX() {
		float dis_x = getWidth() / 2 - getApertureCenterX();
		//Log.e(TAG,  "gangyun_chan [getApertureCenterDisX] dis_x:"+dis_x);
		return dis_x;
	}
	
    /**
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
