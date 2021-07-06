package com.mediatek.camera.ui.shutter;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import androidx.core.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View.OnTouchListener;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import android.graphics.Rect;

import com.mediatek.camera.R;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.utils.CameraUtil;
//bv wuyonglin add for bug2771 20201031 start
import com.mediatek.camera.common.IAppUi;
//bv wuyonglin add for bug2771 20201031 end

public class TripleSwitchHorizontalScrollView extends HorizontalScrollView {
    private static final String TAG = "TripleSwitchHorizontalScrollView";
	  private static boolean DEBUG = true;
    protected IAppUi mCameraAppUI;  //bv wuyonglin add for bug2771 20201031
    private IApp mApp;
    private TripleSwitchHorizontalAdapter mAdapter;
    protected final LinearLayout mScrollStrip;
    private OnItemClickListener mOnItemClickListener;
	private OnFlingListener mOnFlingListener;
    private boolean isNeedReload = true;
    private boolean mIsChange;
    private float mDownX = -1f;
    private float mMoveX = -1f;
    private float mDownY = -1f;
    private float mMoveY = -1f;
    private float mYMove;
    private float mXMove;
    private int mTouchSlop;
    private int mSelectIndex = -1;
    private Map<View, Integer> mViewPos = new HashMap<View, Integer>();
    private int mNormalTextColor = 0;
    private int mSelectedTextColor = 0;
    private View mProxy;
    private boolean isScrolling = false;
    private boolean moveMode = true;

    public static abstract interface OnItemClickListener {
        //public abstract void onItemClick(int item);
        void onItemClick(int item);
        void onLongItemClick(int item);
        void onScroll(MotionEvent ev);
    }
	
	public interface OnFlingListener {
		public void doFling(int direction);
	}

    public TripleSwitchHorizontalScrollView(Context context, AttributeSet atts, CHorizontalScrollLayoutAdapter adapter) {
        this(context, atts);
    }

    public TripleSwitchHorizontalScrollView(Context context, AttributeSet atts) {
        super(context, atts);
        setOverScrollMode(2);
        setFillViewport(false);
        setHorizontalScrollBarEnabled(false);
        mScrollStrip = new CHorizontalScrollStrip(context, atts, false);
        addView(mScrollStrip, -1, -1);
        Resources res = getResources();
        mNormalTextColor = res.getColor(R.color.shutter_title_normal_color);
        mSelectedTextColor = res.getColor(R.color.shutter_title_select_color);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(context));
    }

    //bv wuyonglin add for bug2771 20201031 start
    public void setContext(IAppUi cameraAppUI) {
        mCameraAppUI = cameraAppUI;
    }
    //bv wuyonglin add for bug2771 20201031 end

    public void setAdapter(TripleSwitchHorizontalAdapter adapter, IApp app) {
        mAdapter = adapter;
        mApp = app;
        initView();
    }
    
     public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /* add by liangchangwei for Text Rotate begin */
    public void onOrientationChanged(Activity activity, int orientation) {
        int count = mScrollStrip.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mScrollStrip.getChildAt(i);
            CameraUtil.rotateRotateLayoutChildView(activity, child,
                    orientation, true);
        }
    }
    /* add by liangchangwei for Text Rotate end */
    
    public void setOnFlingListener(OnFlingListener listener){
	mOnFlingListener = listener;
    }

    private void initView() {
        int count = mAdapter.getCount();
        Log.i(TAG,"initView count = " + count);
        mScrollStrip.removeAllViews();
        mViewPos.clear();
        mSelectIndex = -1;
        isNeedReload = true;
        smoothScrollTo(0, 0);
        for (int index = 0; index < count; index++) {
            View child = mAdapter.getView(index, null, this);
            CameraUtil.rotateRotateLayoutChildView(mApp.getActivity(), child,
                    mApp.getGSensorOrientation(), false);
            mViewPos.put(child, Integer.valueOf(index));
            child.setOnClickListener(mClickListener);
            child.setOnLongClickListener(mLongClickListener);
            mScrollStrip.addView(child);
        }
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
			//if (mActivity.getCameraAppUI().isNormalViewState()){ aland
			if(true){
				int i = mViewPos.get(view).intValue();
				if (mOnItemClickListener != null) {
					mOnItemClickListener.onItemClick(i);
				}
			}
        }
    };

    private View.OnLongClickListener mLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            int i = mViewPos.get(view).intValue();
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onLongItemClick(i);
            }
            return true;
        }
    };

    private void hilite(int selected) {
        int count = mScrollStrip.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mScrollStrip.getChildAt(i);
            if (selected == i) {
                child.setVisibility(INVISIBLE);
                //child.setSelected(true);
                //((TextView) child).setTextColor(mSelectedTextColor);
            } else {
		//bv wuyonglin add for bug2771 20201031 start
		if (count == 3 && mCameraAppUI.is4KVideo() && mCameraAppUI.getCurrentMode().equals("Video") && i == 0) {
                child.setVisibility(INVISIBLE);
		} else {
		//bv wuyonglin add for bug2771 20201031 end
                child.setVisibility(VISIBLE);
		}
                //child.setSelected(false);
                //((TextView) child).setTextColor(mNormalTextColor);
            }
        }
    }

    public void setSelectIndex(int index) {
        mSelectIndex = index;
        scrollToCenter(index);
    }

    //bv wuyonglin add for bug2771 20201031 start
    public int getSelectIndex() {
        return mSelectIndex;
    }

    public LinearLayout getScrollStrip() {
        return mScrollStrip;
    }
    //bv wuyonglin add for bug2771 20201031 end
    public int getCount() {
        if (mAdapter != null)
            return mAdapter.getCount();
        return 0;
    }

    private void scrollToCenter(int index) {
        if (isScrolling)
            return;
        isScrolling = true;
        int count = mScrollStrip.getChildCount();

        View view = mScrollStrip.getChildAt(index);
        int first = Utilx.getWidth(mScrollStrip.getChildAt(0));
        int selected = Utilx.getWidth(view);
        int startt = Utilx.getStart(view);
        int x = startt - (first - selected) / 2;

        hilite(index);
        //smoothScrollTo(x, 0);
        isScrolling = false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.i(TAG,"onLayout  l = " + l + " t = " + t + " r = " + r + " b = " + b);
        for (int i = 0, size = getChildCount(); i < size; i++) {
            View view = getChildAt(i);
        }

        if (((changed) || (isNeedReload)) && (mSelectIndex != -1)) {
            isNeedReload = false;
            //scrollToCenter(mSelectIndex);
            isScrolling = true;
            int count = mScrollStrip.getChildCount();
            int index = 0;
            if(count >2){
                index = 1;
            }else if(count <=2){
                index = 0;
            }

            View view = mScrollStrip.getChildAt(index);
            int first = Utilx.getWidth(mScrollStrip.getChildAt(0));
            int selected = Utilx.getWidth(view);
            int startt = Utilx.getStart(view);
            int x = startt - (first - selected) / 2;

            hilite(mSelectIndex);
            smoothScrollTo(x, 0);
            isScrolling = false;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(TAG,"onSizeChanged w = " + w + " h = " + h + " oldw = " + oldw + " oldh = " + oldh);
        if (mScrollStrip.getChildCount() > 0) {
            View child0 = this.mScrollStrip.getChildAt(0);
            View childx = mScrollStrip.getChildAt(mScrollStrip.getChildCount() - 1);
            int startx = (w - Utilx.getMeasuredWidth(child0)) / 2;
            int endx = (w - Utilx.getMeasuredWidth(childx)) / 2;
            mScrollStrip.setMinimumWidth(mScrollStrip.getMeasuredWidth());
            setPaddingRelative(startx, getPaddingTop(), endx, getPaddingBottom());
            setClipToPadding(false);
        }
    }

	@Override
	public void setEnabled(boolean enabled) {
        if (mViewPos != null) {
            for (View v : mViewPos.keySet()) {
				v.setEnabled(enabled);
			}
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled())
            return false;
        if (mOnItemClickListener != null) {
            View view = mScrollStrip.getChildAt(mSelectIndex);
            Rect rect = new Rect();
            int width = view.getRight()-view.getLeft();
            view.getGlobalVisibleRect(rect);             
            if(ev.getX()>rect.left && ev.getX()<rect.right+width/2 ){
                mOnItemClickListener.onScroll(ev);
            }
            return true;
        }
        return false;//
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY * 4);
    }

    public boolean onTouch(MotionEvent ev) {
        float currentX = ev.getRawX();
        if (!isEnabled())
            return false;
	    Log.i(TAG, "getAction:"+ev.getActionMasked()+ " mDownX:"+mDownX + " moveMode:"+moveMode);
        switch (ev.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            mDownX = ev.getRawX();
			moveMode = true;
            break;
        case MotionEvent.ACTION_MOVE:
            mMoveX = ev.getRawX();
            int dis = (int) (mMoveX - mDownX);
            int absd = Math.abs(dis);
			if (DEBUG){
				Log.i(TAG, "onTouch dis=" + dis + ",absd=" + absd + " mMoveX="+mMoveX);
			}
			//modify by huangfei for updown switch camera start
            //if (absd > 120 && moveMode && mDownX !=-1f) {
			if (absd > 200 && moveMode && mDownX !=-1f) {
			//modify by huangfei for updown switch camera end

                if (dis > 0) {
                    //setEnabled(false);
                    mOnFlingListener.doFling(1);
                } else {
                    //setEnabled(false);
                    mOnFlingListener.doFling(0);
                }
				mDownX = -1f;
            }

                break;
            case MotionEvent.ACTION_UP:
				mDownX = -1f;
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
		    case MotionEvent.ACTION_POINTER_DOWN:				
				moveMode = false;
				Log.i(TAG, "ACTION_POINTER_DOWN 1");
			 	break;
		    case MotionEvent.ACTION_POINTER_UP:	
				mDownX = -1f;
				moveMode = true;
				Log.i(TAG, "ACTION_POINTER_UP");
			 	break;
            default:
                break;
            }

        return false;
    }

}
