package com.mediatek.camera.ui.shutter;
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
import android.util.TypedValue;

import com.mediatek.camera.R;
//bv wuyonglin add for adjust screen height 1560px top bar position 20200628 start
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.view.Display;
//bv wuyonglin add for adjust screen height 1560px top bar position 20200628 end

public class CHorizontalScrollView extends HorizontalScrollView {
    private static final String TAG = "Century CHorizontalScrollView";
	  private static boolean DEBUG = true;
    //protected CameraActivity mActivity;
    private CHorizontalScrollLayoutAdapter mAdapter;
    protected final LinearLayout mScrollStrip;
    private OnItemClickListener mOnItemClickListener;
	private OnFlingListener mOnFlingListener;
    private boolean isNeedReload = true;
    private boolean mIsChange;
    private float mDownX = -1f;
    private float mMoveX = -1f;
    private float mXMove;
    private int mTouchSlop;
    private int mSelectIndex = -1;
    private Map<View, Integer> mViewPos = new HashMap<View, Integer>();
    private int mNormalTextColor = 0;
    private int mSelectedTextColor = 0;
    private View mProxy;
    private boolean isScrolling = false;
    private boolean moveMode = true;

    private boolean mEnalble = true;
	//bv wuyonglin add for all area fling to change mode start
    private boolean isFirstMove = true;
    private int dis = 0;
    private int absd = 0;
    //bv wuyonglin delete for adjust all icon position 20200612 start
    //private float mSelectedTextSize;
    private float mNormalTextSize;
    //bv wuyonglin delete for adjust all icon position 20200612 end
	//bv wuyonglin add for all area fling to change mode end

    public static abstract interface OnItemClickListener {
        //public abstract void onItemClick(int item);
	void onItemClick(int item);
    	}
	
	public interface OnFlingListener {
		public void doFling(int direction);
	}

    public CHorizontalScrollView(Context context, AttributeSet atts, CHorizontalScrollLayoutAdapter adapter) {
        this(context, atts);
    }

    public CHorizontalScrollView(Context context, AttributeSet atts) {
        super(context, atts);
        setOverScrollMode(2);
        setFillViewport(false);
        setHorizontalScrollBarEnabled(false);
        mScrollStrip = new CHorizontalScrollStrip(context, atts, false);
        addView(mScrollStrip, -1, -1);
        Resources res = getResources();
        mNormalTextColor = res.getColor(R.color.shutter_title_normal_color);
        mSelectedTextColor = res.getColor(R.color.shutter_title_select_color);
        //bv wuyonglin delete for adjust all icon position 20200612 start
        //mSelectedTextSize = res.getDimensionPixelSize(R.dimen.selected_text_size);
        //bv wuyonglin delete for adjust all icon position 20200612 end
        //bv wuyonglin add for adjust screen height 1560px top bar position 20200628 start
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        if (dm.heightPixels == 1560 || dm.heightPixels == 2300 || dm.heightPixels == 2400) {
        mNormalTextSize = res.getDimensionPixelSize(R.dimen.hct_shutter_title_size_1560px);
        } else {
        mNormalTextSize = res.getDimensionPixelSize(R.dimen.hct_shutter_title_size);
        }
        //bv wuyonglin add for adjust screen height 1560px top bar position 20200628 end
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(context));
    }

    /*public void setContext(CameraActivity activity) {
        mActivity = activity;
    }*/

    public void setAdapter(CHorizontalScrollLayoutAdapter adapter) {
        mAdapter = adapter;
        initView();
    }
    
     public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnFlingListener(OnFlingListener listener){
	mOnFlingListener = listener;
    }

    private void initView() {
        int count = mAdapter.getCount();
        mScrollStrip.removeAllViews();
        mViewPos.clear();
        mSelectIndex = -1;
        isNeedReload = true;
        smoothScrollTo(0, 0);
        for (int index = 0; index < count; index++) {
            View child = mAdapter.getView(index, null, this);
            mViewPos.put(child, Integer.valueOf(index));
            child.setOnClickListener(mClickListener);
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

    private void hilite(int selected) {
        int count = mScrollStrip.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mScrollStrip.getChildAt(i);
            if (selected == i) {
                //child.setSelected(true);
                ((TextView) child).setTextSize(TypedValue.COMPLEX_UNIT_PX, mNormalTextSize);    //bv wuyonglin modify for adjust all icon position 20200628
                ((TextView) child).setTextColor(mSelectedTextColor);
            } else {
                //child.setSelected(false);
                ((TextView) child).setTextSize(TypedValue.COMPLEX_UNIT_PX, mNormalTextSize);
                ((TextView) child).setTextColor(mNormalTextColor);
            }
        }
    }

    public void setSelectIndex(int index) {
        mSelectIndex = index;
        scrollToCenter(index);
    }

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
        smoothScrollTo(x, 0);
        isScrolling = false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        for (int i = 0, size = getChildCount(); i < size; i++) {
            View view = getChildAt(i);
        }

        if (((changed) || (isNeedReload)) && (mSelectIndex != -1)) {
            isNeedReload = false;
            scrollToCenter(mSelectIndex);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
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
        mEnalble = enabled;
        if (mViewPos != null) {
            for (View v : mViewPos.keySet()) {
				v.setEnabled(enabled);
			}
        }
    }

    public boolean getSwitchEnable(){
        return mEnalble;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

//bv wuyonglin delete for all area fling to change mode start
    /*@Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;//
    }*/
//bv wuyonglin delete for all area fling to change mode end

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY * 4);
    }

//bv wuyonglin delete for all area fling to change mode start
    /*public boolean onTouch(MotionEvent ev) {
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
    }*/
//bv wuyonglin delete for all area fling to change mode end

//bv wuyonglin add for all area fling to change mode add
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        float currentX = ev.getX();
        if (!isEnabled())
            return false;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                moveMode = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isFirstMove) {
                    mDownX = ev.getX();
                    isFirstMove = false;
                }
                mMoveX = ev.getX();
                dis = (int) (mMoveX - mDownX);
                absd = Math.abs(dis);
                if (DEBUG){
 				Log.i(TAG, "onTouch dis=" + dis + ",absd=" + absd + " mMoveX="+mMoveX);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (absd > 50 && moveMode && mDownX !=-1f) {
                    if (dis > 0) {
                        //setEnabled(false);
                        mOnFlingListener.doFling(1);
                    } else {
                        //setEnabled(false);
                        mOnFlingListener.doFling(0);
                    }
                }
                isFirstMove = true;
                mDownX = -1f;
                break;
            case MotionEvent.ACTION_CANCEL:
                isFirstMove = true;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                moveMode = false;
                Log.i(TAG, "ACTION_POINTER_DOWN 1");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mDownX = -1f;
                moveMode = true;
                isFirstMove = true;
                Log.i(TAG, "ACTION_POINTER_UP");
                break;
            default:
                break;
        }
        return false;
    }
//bv wuyonglin add for all area fling to change mode end
}
