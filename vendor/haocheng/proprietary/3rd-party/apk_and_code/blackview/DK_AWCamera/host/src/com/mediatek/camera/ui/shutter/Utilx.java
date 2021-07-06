package com.mediatek.camera.ui.shutter;

import android.content.Context;
import androidx.core.view.ViewCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.OrientationEventListener;

public class Utilx {

    public static int getMeasuredWidth(View view) {
        if (view == null)
            return 0;
        return view.getMeasuredWidth();
    }

    public static int getWidth(View v) {
        if (v == null)
            return 0;
        return v.getWidth();
    }

    public static int getEnd(View view) {
        return getEnd(view, false);
    }

    public static int getEnd(View view, boolean flag) {
        if (view == null)
            return 0;
        if (isLayoutRtl(view)) {
            if (flag)
                return view.getLeft() + getPaddingEnd(view);
            return view.getLeft();
        }
        if (flag)
            return view.getRight() - getPaddingEnd(view);
        return view.getRight();
    }

    public static int getStart(View view) {
        return getStart(view, false);
    }

    public static int getStart(View view, boolean flag) {
        if (view == null)
            return 0;
        if (isLayoutRtl(view)) {
            if (flag)
                return view.getRight() - getPaddingStart(view);
            return view.getLeft();
        }
        if (flag)
            return view.getLeft() + getPaddingStart(view);
        return view.getLeft();
    }

    public static int getPaddingStart(View view) {
        if (view == null)
            return 0;
        return ViewCompat.getPaddingStart(view);
    }

    static int getPaddingEnd(View view) {
        if (view == null)
            return 0;
        return ViewCompat.getPaddingEnd(view);
    }

    static boolean isLayoutRtl(View view) {
        return ViewCompat.getLayoutDirection(view) == 1;
    }

    public static int dp2px(Context context, int dip) {
        return (int) (0.5F + dip * context.getResources().getDisplayMetrics().density);
    }

    public static int sp2px(Context context, int sp) {
        return (int) TypedValue.applyDimension(2, sp, context.getResources().getDisplayMetrics());
    }
	
	public static void setLayoutSelected(View view, boolean isSelected) {
        if (view == null) {
			android.util.Log.i("century", "setLayoutSelected view null");
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0, count = group.getChildCount(); i < count; i++) {
				android.util.Log.i("century", "setLayoutSelected view " + isSelected);
                group.getChildAt(i).setSelected(isSelected);
            }
        }
    }
	
	public static void setOrientation(View view, int orientation, boolean animation) {
        if (view == null) {
            return;
        }
		if (view.getTag() != null){
			//android.util.Log.i("century", "setOrientation=" + (String)view.getTag());
		}
        if (view instanceof Rotatable) {
            ((Rotatable) view).setOrientation(orientation, animation);
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0, count = group.getChildCount(); i < count; i++) {
                setOrientation(group.getChildAt(i), orientation, animation);
            }
        }
    }
	
	public static final int ORIENTATION_HYSTERESIS = 5;  
  
    public static int roundOrientation(int orientation, int orientationHistory) {  
        boolean changeOrientation = false;  
        if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {  
            changeOrientation = true;  
        } else {  
            int dist = Math.abs(orientation - orientationHistory);  
            dist = Math.min( dist, 360 - dist );  
            changeOrientation = ( dist >= 45 + ORIENTATION_HYSTERESIS );  
        }  
        if (changeOrientation) {  
            return ((orientation + 45) / 90 * 90) % 360;  
        } 
        return orientationHistory;  
    }  
}
