
package com.mediatek.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import com.mediatek.camera.R;
import com.mediatek.camera.common.utils.CameraUtil;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;

public class CHSeekBar extends SeekBar
{
    private final static String TAG = "century CHSeekBar";

    private LayoutInflater mInflater;
    private View mView;
    private Paint mPaint;
    private String mTextString;
    private int chseekbarTextPadding;

    public CHSeekBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        
        mPaint =  new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);//去除锯齿
        //start, wangsenhao , for seekBar beauty level textsize, 2020.04.21
        //mPaint.setTextSize((int)context.getResources().getDimension(R.dimen.century_textsize_medium));
        mPaint.setTextSize((int)context.getResources().getDimension(R.dimen.beauty_level_textsize_medium));
        //end, wangsenhao , for seekBar beauty level textsize, 2020.04.21
        mPaint.setTypeface(Typeface.SANS_SERIF);
        chseekbarTextPadding = (int)context.getResources().getDimension(R.dimen.chseekbar_text_padding);
    }

    public void setSeekBarText(String str)
    {
        mTextString = str;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // TODO Auto-generated method stub

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return super.onTouchEvent(event);
    }

    private int getViewWidth(View v)
    {
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(w, h);
        return v.getMeasuredWidth();
    }

    private int getViewHeight(View v)
    {
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(w, h);
        return v.getMeasuredHeight();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas)
    {
        // TODO Auto-generated method stub
         super.onDraw(canvas);
         
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        int width = getWidth() - left - right;
        int height = getHeight() - top - bottom;
        float thumb_x = (float)this.getProgress() * (width/ (float)this.getMax());
        try
        {
            float fontWidth = getFontlength(mPaint, mTextString);
            float fontHeight = getFontHeight(mPaint);

            if(!CameraUtil.isRTL){
                int x = left + (int)thumb_x - (int)(fontWidth/2);
                //int y = 5;

                Log.i(TAG, "x." + x + ", chseekbarTextPadding =" + chseekbarTextPadding+" fontHeight ="+fontHeight+" fontWidth ="+fontWidth+" thumb_x ="+thumb_x+" left ="+left+" top ="+top+" right ="+right+" 			bottom ="+bottom+" getHeight() ="+getHeight()+" getWidth() ="+getWidth()+" height ="+height+" getFontLeading =" +getFontLeading(mPaint)+" daad ="+ (int)(fontHeight/2));
                canvas.drawText(mTextString, x,chseekbarTextPadding + (int)(fontHeight/2), mPaint);
            }else{
                int x = (int)thumb_x - (int)(fontWidth/2);
                //int y = 5;

                Log.i(TAG, "x." + x + ", chseekbarTextPadding =" + chseekbarTextPadding+" fontHeight ="+fontHeight+" fontWidth ="+fontWidth+" thumb_x ="+thumb_x+" left ="+left+" top ="+top+" right ="+right+" 			bottom ="+bottom+" getHeight() ="+getHeight()+" getWidth() ="+getWidth()+" height ="+height+" getFontLeading =" +getFontLeading(mPaint)+" daad ="+ (int)(fontHeight/2));
                canvas.drawText(mTextString, width - x,chseekbarTextPadding + (int)(fontHeight/2), mPaint);

            }
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
    }
    
    /**
     * @return 返回指定笔和指定字符串的长度  
     */
    public static float getFontlength(Paint paint, String str) {
        return paint.measureText(str);
    }
    /**
     * @return 返回指定笔的文字高度  
     */
    public static float getFontHeight(Paint paint)  {
        FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }
    /**
     * @return 返回指定笔离文字顶部的基准距离
     */
    public static float getFontLeading(Paint paint) {
        FontMetrics fm = paint.getFontMetrics();
        return fm.leading- fm.ascent;
    }

}
