package com.mediatek.camera.feature.mode.aiworksfacebeauty.view;



import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color; 

//import com.android.camera.ui.Rotatable;


public class CRotateTextView extends TextView /*implements Rotatable */{
    private static final String TAG = "CRotateTextView1";
    private static final int ANIMATION_SPEED = 270; // 270 deg/sec
    private static final int MENU_TYPE_PREF = 0;
    private static final int MENU_TYPE_VIEW = 1;
    private static final float ITEM_TEXT_SIZE = 11.7f;

    private int mCurrentDegree = 0; // [0, 359]
    private int mStartDegree = 0;
    private int mTargetDegree = 0;
    private boolean mClockwise = false;
    private boolean mEnableAnimation = true;
    private long mAnimationStartTime = 0;
    private long mAnimationEndTime = 0;

    private int mType = MENU_TYPE_PREF;
    private int mIconid = -1;
    private boolean mSelected = true;

    public void setType(int type) {
        mType = type;
    }

    protected int getDegree() {
        return mTargetDegree;
    }

    public CRotateTextView(Context context) {
        super(context);
    }

    public CRotateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
	
	/*
	@Override
	public void setSelected(boolean isSelected){
		Log.i(TAG, "setSelected=" + isSelected);
		if (isSelected){
			super.setSelected(true);
			//setTextColor(Color.RED); //getResources().getColor(R.color.century_text_high_color)
		}else{
			//super.setSelected(true);
			//setTextColor(getResources().getColor(R.color.century_text_normal_color));
		}
		invalidate();
	}
	*/

    public void setContent(int drawableId, String str) {
		Log.e(TAG, "setContent=" + str);
		if (drawableId  != -1){
			Drawable drawable = getResources().getDrawable(drawableId);  
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());  
			this.setCompoundDrawables(null, drawable, null, null);
		}
        setText(str);
      //  setTextSize(ITEM_TEXT_SIZE);
    }
	
	public void setContent(int drawableId) {
        //setCompoundDrawablesWithIntrinsicBounds(0, drawableId, 0, 0);
          if (drawableId  != -1){
		Drawable drawable = getResources().getDrawable(drawableId);  
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());  
		this.setCompoundDrawables(null, drawable, null, null);
          }
    }
/*
    @Override
    public void setOrientation(int degree, boolean animation) {// orientation
        //Log.d(TAG, "[setOrientation]degree = " + degree + ", animation=" + animation + ",mOrientation=" + mTargetDegree);
        mEnableAnimation = animation;
        // make sure in the range of [0, 359]
        degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
        if (degree == mTargetDegree) {
            return;
        }

        mTargetDegree = degree;
        if (mEnableAnimation) {
            mStartDegree = mCurrentDegree;
            mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();

            int diff = mTargetDegree - mCurrentDegree;
            diff = diff >= 0 ? diff : 360 + diff; // make it in range [0, 359]
            // Make it in range [-179, 180]. That's the shorted distance between the two angles
            diff = diff > 180 ? diff - 360 : diff;

            mClockwise = diff >= 0;
            mAnimationEndTime = mAnimationStartTime + Math.abs(diff) * 1000 / ANIMATION_SPEED;
        } else {
            mCurrentDegree = mTargetDegree;
        }

        invalidate();
    }
*/
    @Override
    protected void onDraw(Canvas canvas) {
        int w = this.getWidth();
        int h = this.getHeight();
        //Log.i(TAG, "w=" + w + " h=" + h);
        if (w == 0 || h == 0) {
            Log.e(TAG, "[onDraw]w == 0 || h == 0, return!");
            return; // nothing to draw
        }

        if (mCurrentDegree != mTargetDegree) {
            long time = AnimationUtils.currentAnimationTimeMillis();
            if (time < mAnimationEndTime) {
                int deltaTime = (int) (time - mAnimationStartTime);
                int degree = mStartDegree + ANIMATION_SPEED * (mClockwise ? deltaTime : -deltaTime) / 1000;
                degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
                mCurrentDegree = degree;
                invalidate();
            } else {
                mCurrentDegree = mTargetDegree;
            }
        }

        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        int width = getWidth() - left - right;
        int height = getHeight() - top - bottom;

        int saveCount = canvas.getSaveCount();

        canvas.translate(left + width / 2, top + height / 2);
        canvas.rotate(-mCurrentDegree);
        canvas.translate(-w / 2, -h / 2);
        canvas.restoreToCount(saveCount);
        super.onDraw(canvas);
    }

    @Override
    protected void drawableStateChanged() {
		//Log.i(TAG, "drawableStateChanged");
        //setSelected(mSelected);
        super.drawableStateChanged();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            setAlpha(1.0f);
        } else {
            setAlpha(0.4f);
        }
    }
}
