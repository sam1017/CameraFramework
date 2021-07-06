package com.mediatek.camera.common.preference;

import android.content.Context;
import android.graphics.Color;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.preference.PreferenceCategory;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.mediatek.camera.R;

public class BvPreferenceCategory extends PreferenceCategory {

    private static final String TAG=BvPreferenceCategory.class.getSimpleName();
    private TextView titleView;
    private View  divider;
    //bv zhangjiachu add for space bottom 20200508
    private View SpaceBottom;
    private View SpaceTop;


    private int bv_divider_color=-1;
    private int bv_divider_width=-1;
    private int bv_divider_height=-1;
    private boolean bv_divider_visible=true;

    private int bv_title_color=-1;
    private float bv_title_size=-1f;
    private boolean bv_title_visible=true;
    private String bv_title_text=null;
    //bv zhangjiachu add for space bottom 20200508
    private int bv_space_bottom_hight=-1;
    private int bv_space_top_hight=-1;
    private Context mContext;



    public BvPreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    public BvPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setLayoutResource(R.layout.bv_preferencecategory);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.bvPreferenceStyle, defStyleAttr, 0);
        int count = array.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.bvPreferenceStyle_bv_divider_width:
                    bv_divider_width=(int)array.getDimensionPixelSize(attr, -1);
                    // divider.setLayoutParams(new ViewGroup.LayoutParams((int)array.getDimension(attr, 0),divider.getHeight()));
                    break;
                case R.styleable.bvPreferenceStyle_bv_divider_height:
                    bv_divider_height=(int) array.getDimensionPixelSize(attr, -1);
                    //divider.setLayoutParams(new ViewGroup.LayoutParams(divider.getWidth(), (int) array.getDimension(attr, 0)));
                    break;
                case R.styleable.bvPreferenceStyle_bv_divider_color:
                    bv_divider_color=array.getColor(attr, Color.BLACK);
                    //divider.setBackgroundColor(array.getColor(attr, Color.BLACK));
                    break;
                case R.styleable.bvPreferenceStyle_bv_divider_visible:
                    bv_divider_visible=array.getBoolean(attr, true);
                    //titleView.setVisibility(array.getBoolean(attr, true) ? View.VISIBLE : View.GONE);
                    break;
                case R.styleable.bvPreferenceStyle_bv_title_color:
                    bv_title_color=array.getColor(attr, Color.BLACK);
                    //titleView.setTextColor(array.getColor(attr, Color.BLACK));
                    break;
                case R.styleable.bvPreferenceStyle_bv_title_size:
                    bv_title_size=array.getDimensionPixelSize(attr, 0);
                    //titleView.setTextSize(array.getDimension(attr, 0));
                    break;
                case R.styleable.bvPreferenceStyle_bv_title_visible:
                    bv_title_visible=array.getBoolean(attr, true);
                    //titleView.setVisibility(array.getBoolean(attr, true) ? View.VISIBLE : View.GONE);
                    break;
                    //bv zhangjiachu add for space bottom 20200508
                case R.styleable.bvPreferenceStyle_bv_space_bottom_hight:
                    bv_space_bottom_hight=(int) array.getDimensionPixelSize(attr, -1);
                    break;
                case R.styleable.bvPreferenceStyle_bv_space_top_hight:
                    bv_space_top_hight=(int) array.getDimensionPixelSize(attr, -1);
                    break;
                case R.styleable.bvPreferenceStyle_bv_title_text:
                    bv_title_text=array.getString(attr);
                    //titleView.setText(array.getString(attr));
                    break;
            }
        }
        array.recycle();
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        titleView= (TextView) view.findViewById(R.id.preference_title);
        divider = (View) view.findViewById(R.id.preference_divider);
        //bv zhangjiachu add 20200506
        SpaceBottom = (View) view.findViewById(R.id.space_bottom);
        SpaceTop = (View) view.findViewById(R.id.space_top);
        if (bv_space_top_hight > 0){
            view.getLayoutParams().height = mContext.getResources().getDimensionPixelOffset(R.dimen.settings_item_customized_height);
            SpaceTop.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams lp = SpaceTop.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = bv_space_top_hight;
            SpaceTop.setLayoutParams(lp);
        } else {
            LinearLayout.LayoutParams mTextViewLayoutParams = (LinearLayout.LayoutParams) titleView.getLayoutParams();
            mTextViewLayoutParams.topMargin = mContext.getResources().getDimensionPixelOffset(R.dimen.settings_title_view_top_margin);
            titleView.setLayoutParams(mTextViewLayoutParams);
            SpaceTop.setVisibility(View.GONE);
        }

        titleView.setVisibility(bv_title_visible ? View.VISIBLE : View.GONE);
        divider.setVisibility(bv_divider_visible ? View.VISIBLE : View.GONE);
        //bv zhangjiachu add for space bottom 20200508 start
        if (bv_space_bottom_hight > 0){
            SpaceBottom.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams lp = SpaceBottom.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = bv_space_bottom_hight;
            SpaceBottom.setLayoutParams(lp);
        } else {
            SpaceBottom.setVisibility(View.GONE);
        }
        //bv zhangjiachu add for space bottom 20200508 end
        if(bv_title_text!=null) {
            titleView.setText(bv_title_text);
        }
        if(bv_title_color!=-1){
            titleView.setTextColor(bv_title_color);
        }
        if(bv_title_size!=-1f){
            //titleView.setTextSize(bv_title_size);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX,bv_title_size);
        }
        /*add by liangchangwei for fixbug 7040 start */
        /*if(bv_divider_color!=-1){
            divider.setBackgroundColor(bv_divider_color);
        }*/
        /*add by liangchangwei for fixbug 7040 end */

        /*if(bv_divider_width !=-1 || bv_divider_height != -1) {//match_parent : -1px:
	    if (bv_divider_height == -1) {
            divider.setLayoutParams(new android.widget.LinearLayout.LayoutParams(bv_divider_width,1));
	    } else {
            divider.setLayoutParams(new android.widget.LinearLayout.LayoutParams(bv_divider_width,bv_divider_height));
	    }
        }*/

    }

    //bv xiawenwen add for design 20200408 start
    @Override
    public void setTitle(CharSequence title){
        bv_title_text = title.toString();
    }

    public void setContentDescription(String contentDescription) {
        //todo
    }
    //bv xiawenwen add for design 20200408 end

    //bv sunyue add for bug 1795 start 20200804
    public void setDividerVisible(boolean visible){
        bv_divider_visible = visible;
        if (divider != null){
            divider.setVisibility(bv_divider_visible ? View.VISIBLE : View.GONE);
        }
    }
    //bv sunyue add for bug 1795 start 20200804

}
