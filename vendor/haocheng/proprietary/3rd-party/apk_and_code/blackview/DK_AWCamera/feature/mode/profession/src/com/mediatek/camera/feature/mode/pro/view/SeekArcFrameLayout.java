package com.mediatek.camera.feature.mode.pro.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import javax.annotation.Nullable;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.mediatek.camera.Config;
import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogUtil;

public class SeekArcFrameLayout extends FrameLayout implements SeekArc.OnSeekArcChangeListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(SeekArcFrameLayout.class.getSimpleName());
    private static final int SEEKARC_NUM = 5;
    private Context mContext;

    private SeekArc mArcAwb;
    private SeekArc mArcIso;
    private SeekArc mArcMf;
    private SeekArc mArcExp;

    //add by huangfei for shutter start
    private SeekArc mArcShutter;
    //add by huangfei for shutter end

    private LinkedList<SeekArc> mArcList;

    //add by huangfei for seekarc change start
    private int mProgress = 0;
    //add by huangfei for seekarc change end

    //add by huangfei for MF start
    private int mDistance;
    //add by huangfei for MF end

    private OnHintChangeListener mOnHintChangeListener;

    private final int[] mWbFixedlabels = {
            R.drawable.century_label_wb_auto,
            R.drawable.century_label_wb_incandescent,
            R.drawable.century_label_wb_sunlight,
            R.drawable.century_label_wb_fluorescent,
            R.drawable.century_label_wb_cloudy
    };

    private final int[] mIndicateIds = {
            R.drawable.century_seek_arc_indicate_auto_selector,
            R.drawable.century_seek_arc_indicate_incandescent_selector,
            R.drawable.century_seek_arc_indicate_sun_selector,
            R.drawable.century_seek_arc_indicate_fluorescent_selector,
            R.drawable.century_seek_arc_indicate_cloudy_selector
    };

    private final String[] mWBValue = {
            "auto",
            "incandescent",
            "daylight",
            "fluorescent",
            "cloudy-daylight"
    };

    private final int[] mWBHintID = {
            R.string.pro_mode_auto,
            R.string.pro_mode_wb_incandescent,
            R.string.pro_mode_daylight,
            R.string.pro_mode_fluorescent,
            R.string.pro_mode_cloudy
    };

    private final String[] mISOValue = {
            "0",
            "100",
            "200",
            "400",
            "800",
            "1600"
    };

    //add by huangfei for shutter start
    private final String[] mShutterHintID = {
        "auto",
        "1/4000",
        "1/3200",
        "1/2500",
        "1/2000",
        "1/1600",
        "1/1000",
        "1/800",
        "1/640",
        "1/500",
        "1/400",
        "1/320",
        "1/250",
        "1/200",
        "1/160",
        "1/125",
        "1/100",
        "1/80",
        "1/50",
        "1/40",
        "1/20",
        "1/10",
        "1/8",
        "1/5",
        "1/4",
        "0.3",
        "0.4",
        "0.5",
        "0.6",
        "0.8",
        "1",
        "1.3",
        "1.6",
        "2",
        "2.5",
        "3.2",
        "4",
        "5",
        "6",
        "8",
        "10",
        "13",
        "15",
        "20",
        "25",
        "30",
    };

    private final long[] mShutterValue = {
        0L,
        250000L,
        312500L,
        400000L,
        500000L,
        625000L,
        1000000L,
        1250000L,
        1562500L,
        2000000L,
        2500000L,
        3125000L,
        4000000L,
        5000000L,
        6250000L,
        8000000L,
        10000000L,
        12500000L,
        20000000L,
        25000000L,
        50000000L,
        100000000L,
        125000000L,
        200000000L,
        250000000L,
        300000000L,
        400000000L,
        500000000L,
        600000000L,
        800000000L,
        1000000000L,
        1300000000L,
        1600000000L,
        2000000000L,
        2500000000L,
        3200000000L,
        4000000000L,
        5000000000L,
        6000000000L,
        8000000000L,
        10000000000L,
        13000000000L,
        15000000000L,
        20000000000L,
        25000000000L,
        30000000000L,        
    };
    //add by huangfei for shutter end

    public static final long[] mEXPValue = {
            -3,
            -2,
            -1,
            0,
            1,
            2,
            3
    };

    private Handler mHandler = new Handler();

    private ArrayMap<String, Integer> mData = new ArrayMap<>();

    public SeekArcFrameLayout(Context context) {
        this(context, null);
    }

    public SeekArcFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    void init(Context context) {
        mContext = context;
    }

    private Drawable[] createDrawable(int[] drawableIds) {
        Drawable[] drawables;

        if (drawableIds == null) {
            drawables = null;
        } else {
            int len = drawableIds.length;
            drawables = new Drawable[len];
            for (int i = 0; i < len; i++) {
                drawables[i] = this.mContext.getResources().getDrawable(drawableIds[i]);
                int x = drawables[i].getIntrinsicWidth() / 2;
                int y = drawables[i].getIntrinsicHeight() / 2;
                drawables[i].setBounds(-x, -y, x, y);
            }
        }
        return drawables;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View view = findViewById(R.id.pro_seek_bar);
        mArcAwb = ((SeekArc) view.findViewById(R.id.pro_seek_bar_wb));
        mArcAwb.setId(Style.AWB.constId);
        mArcAwb.setTitle(mContext.getResources().getString(R.string.pro_mode_wb));
        mArcAwb.setOnSeekArcChangeListener(this);

        mArcIso = ((SeekArc) view.findViewById(R.id.pro_seek_bar_iso));
        mArcIso.setId(Style.ISO.constId);
        mArcIso.setTitle(mContext.getResources().getString(R.string.pro_mode_iso));
        mArcIso.setOnSeekArcChangeListener(this);
        mArcMf = ((SeekArc) view.findViewById(R.id.pro_seek_bar_mf));

        //add by huang fei for MF start
        if(!Config.isMFSupport(mContext)){
            mArcMf.setVisibility(View.INVISIBLE);
        }
        //add by huang fei for MF end

        if(mArcMf!=null){
            mArcMf.setId(Style.MF.constId);
            mArcMf.setTitle(mContext.getResources().getString(R.string.pro_mode_focus));
            mArcMf.setOnSeekArcChangeListener(this);
        }        
        mArcExp = ((SeekArc) view.findViewById(R.id.pro_seek_bar_exp));
        mArcExp.setId(Style.EXP.constId);
        mArcExp.setTitle(mContext.getResources().getString(R.string.pro_mode_exposure));
        mArcExp.setOnSeekArcChangeListener(this);

        //add by huang fei for shutter start
        mArcShutter = ((SeekArc) view.findViewById(R.id.pro_seek_bar_shutter));
        if(!Config.isShutterSupport(mContext)){
            mArcShutter.setVisibility(View.INVISIBLE);
        }
        if(mArcShutter!=null){
            mArcShutter.setId(Style.SHUTTER.constId);
            mArcShutter.setTitle(mContext.getResources().getString(R.string.pro_mode_shutter));
            mArcShutter.setOnSeekArcChangeListener(this);
        }  
        //add by huang fei for shutter end

        mArcList = new LinkedList<SeekArc>();
        LinkedList<SeekArc> list = mArcList;
        SeekArc[] seekArcs = new SeekArc[SEEKARC_NUM];
        seekArcs[0] = mArcAwb;
        seekArcs[1] = mArcIso;
        seekArcs[2] = mArcMf;
        seekArcs[3] = mArcExp;
        seekArcs[4] = mArcShutter;
        list.addAll(Arrays.asList(seekArcs));

        setLabelResource();
        mArcAwb.setIndicateIndex(0);

        for (SeekArc seekArc : mArcList) {
            if (seekArc.getId() == Style.EXP.constId) {
                int index = mEXPValue.length/2;
                mData.put(getStyle(seekArc.getId()).constKey, index);
            } else {
                mData.put(getStyle(seekArc.getId()).constKey, 0);
            }
        }
    }

    public void destroy() {
        if (mArcList != null) {
            for (SeekArc seekArc : mArcList) {
                seekArc.recycleBitmap();
            }
            mArcList.clear();
            mArcList = null;
        }

        mArcAwb = null;
        mArcIso = null;
        mArcMf = null;
        mArcExp = null;

        //add by huangfei for shutter start
        mArcShutter = null;
        //add by huangfei for shutter end
    }

    private Style getStyle(int constid) {
        Style[] styles = Style.values();
        int len = Style.values().length;
        for (int i = 0; i < len; i++) {
            if (styles[i].constId == constid) {
                return styles[i];
            }
        }
        return null;
    }

    private int getLabelProgress(int index, int progeress) {
        if (index == 0) {
            return 0;
        }
        if (index == progeress - 1) {
            return 100;
        }
        if (index < 0) {
            index = progeress / 2;
        }
        int x = (int) (100.0F * (1.0F * index / (progeress - 1)));
        return (int) (100.0F * (1.0F * index / (progeress - 1)));
    }

    private int getActualIndex(int value, int length) {
        if (value == 0) {
            return 0;
        }
        if (value >= 100) {
            return length - 1;
        }
        float f = 100.0F / length;
        int x = (int) (1.0F * value / f);
        return (int) (1.0F * value / f);
    }

    private void setLabelResource() {
        Drawable[] fixedLabels = createDrawable(mWbFixedlabels);
        Drawable[] indicaters = createDrawable(mIndicateIds);
        if (mArcAwb != null) {
            mArcAwb.setFixedLabel(fixedLabels);
            mArcAwb.setIndicateLabel(indicaters);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            mHandler.postDelayed(mRunnable, 100);
        }
    }


    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            initViewPosition();
        }
    };


    private void initViewPosition() {
        if (mArcList != null && !mArcList.isEmpty()) {
            for (SeekArc seekArc : mArcList) {
                int index = mData.get(getStyle(seekArc.getId()).constKey);
                seekArc.setProgress(index);             
            }
        }
    }

    @Nullable
    private Hint savePersistData(SeekArc seekArc, int value) {
        int id = seekArc.getId();
        Resources res = mContext.getResources();
        mData.put(getStyle(seekArc.getId()).constKey, value);
        if (id == Style.AWB.constId) {
            int actualIndex = getActualIndex(value, mWBValue.length);
            seekArc.setIndicateIndex(actualIndex);
            return new Hint(mWBValue[actualIndex], res.getString(mWBHintID[actualIndex]));
        } else if (id == Style.ISO.constId) {
            int actualIndex = getActualIndex(value, mISOValue.length);
            String hint = (actualIndex == 0 ? res.getString(R.string.pro_mode_auto) : mISOValue[actualIndex]);
            return new Hint(mISOValue[actualIndex], hint);
        } else if (id == Style.MF.constId) {
            int actualIndex = getActualIndex(value, mDistance);
            String hint = (actualIndex == 0 ? res.getString(R.string.pro_mode_auto) : String.valueOf(value));
            return new Hint(String.valueOf(value), hint);
        } else if (id == Style.EXP.constId) {
            return new Hint(String.valueOf(mEXPValue[value]), String.valueOf(mEXPValue[value]));
        }
        
        //add by huangfei for shutter start
        else if (id == Style.SHUTTER.constId) {
            int actualIndex = getActualIndex(value, mShutterValue.length);
            String hint = (actualIndex == 0 ? res.getString(R.string.pro_mode_auto) : mShutterHintID[actualIndex]);
            return new Hint(String.valueOf(mShutterValue[actualIndex]) , hint);
        }
        //add by huangfei for shutter end

        return null;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public void onStartAnimation(SeekArc seekArc) {

    }

    @Override
    public void onEndAnimation(SeekArc seekArc) {
    }

    @Override
    public void onProgressChanged(SeekArc seekArc, int value, boolean reserved) {
        Hint hint = savePersistData(seekArc, value);

        //add by huangfei for seekarc change start
        mProgress = value;
        //add by huangfei for seekarc change end

        if (mOnHintChangeListener != null&&reserved) {
            mOnHintChangeListener.onProgressChanged(seekArc, hint.getValue(), hint.getHint());
        }
    }

    @Override
    public void onStartTrackingTouch(SeekArc seekArc) {
        int id = seekArc.getId();
        String title = seekArc.getTitle();
        String hint = "";
        if (id == Style.AWB.constId) {

        } else if (id == Style.ISO.constId) {

        } else if (id == Style.MF.constId) {

        } else if (id == Style.EXP.constId) {

        }
        updateArcViewState(id);
        if (mOnHintChangeListener != null) {
            mOnHintChangeListener.onStartTrackingTouch(title, hint, id);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekArc seekArc) {
        if (mOnHintChangeListener != null) {
            mOnHintChangeListener.onStopTrackingTouch();
        }

        //add by huangfei for seekarc change start
        int id = seekArc.getId();
        if (id == Style.AWB.constId) {
            int actualIndex = getActualIndex(mProgress, mWBValue.length);
            seekArc.setProgress(getLabelProgress(actualIndex, mWBValue.length));
        } else if (id == Style.ISO.constId) {
            int actualIndex = getActualIndex(mProgress, mISOValue.length);
            seekArc.setProgress(getLabelProgress(actualIndex, mISOValue.length));
        }else if (id == Style.MF.constId) {
            int actualIndex = getActualIndex(mProgress, mDistance);
            seekArc.setProgress(getLabelProgress(actualIndex, mDistance));
        }
        
        //add by huangfei for shutter start
        else if (id == Style.SHUTTER.constId) {
            int actualIndex = getActualIndex(mProgress, mShutterValue.length);
            seekArc.setProgress(getLabelProgress(actualIndex, mShutterValue.length));
        }
        //add by huangfei for shutter end

        //add by huangfei for seekarc change end

        showAllView();
    }

    private void updateArcViewState(int viewID) {
        mArcAwb.setVisibility(viewID == mArcAwb.getId() ? View.VISIBLE : View.INVISIBLE);
        mArcIso.setVisibility(viewID == mArcIso.getId() ? View.VISIBLE : View.INVISIBLE);
        mArcMf.setVisibility(viewID == mArcMf.getId() ? View.VISIBLE : View.INVISIBLE);
        if(mArcMf!=null){
            mArcMf.setVisibility(viewID == mArcMf.getId() ? View.VISIBLE : View.INVISIBLE);
        }
        mArcExp.setVisibility(viewID == mArcExp.getId() ? View.VISIBLE : View.INVISIBLE);

        //add by huangfei for shutter start
        if(mArcShutter!=null){
            mArcShutter.setVisibility(viewID == mArcShutter.getId() ? View.VISIBLE : View.INVISIBLE);
        }
        //add by huangfei for shutter end

    }

    private void showAllView() {
        if (mArcList != null && !mArcList.isEmpty()) {
            for (SeekArc seekArc : mArcList) {
                if(!Config.isMFSupport(mContext)&&seekArc.getId()==Style.MF.constId){
                    continue;
                }

                //add by huangfei for shutter start
                if(!Config.isShutterSupport(mContext)&&seekArc.getId()==Style.SHUTTER.constId){
                    continue;
                }
                //add by huangfei for shutter end

                seekArc.setVisibility(View.VISIBLE);
            }
        }
    }

    public static enum Style {
        AWB(0, "pref_camera_whitebalance_key"),
        ISO(1, "pref_camera_iso_key"),
        EXP(2, "pref_camera_exp_key"),
        MF(3, "pref_camera_mf_key"),
        SHUTTER(4, "pref_camera_shutter_key");
        int constId;
        String constKey;

        private Style(int id, String key) {
            this.constId = id;
            this.constKey = key;
        }

        public String toString() {
            return super.toString() + "(" + constId + "," + constKey + ")";
        }
    }

    class Hint {
        String mValue;
        String mHint;

        Hint(String value, String hint) {
            mValue = value;
            mHint = hint;
        }

        String getValue() {
            return mValue;
        }

        String getHint() {
            return mHint;
        }
    }

    public void setOnHintChangeListener(OnHintChangeListener l) {
        mOnHintChangeListener = l;
    }

    public static abstract interface OnHintChangeListener {
        public abstract void onProgressChanged(SeekArc seekArc, String progress, String hint);

        public abstract void onStartTrackingTouch(String title, String value, int index);

        public abstract void onStopTrackingTouch();
    }

    //add by huangfei for MF start
    public void setFoucsDistance(int distance) {
        mDistance = distance*5;
        if(mArcMf!=null){
            mArcMf.setMaxProgress(mDistance);
        }
    }
    public int getMFValue() {
        if(mArcMf!=null){
            return mArcMf.getProgress();
        }
        return 0;
    }
    public void resetMF(){
        if(mArcMf!=null){
            int actualIndex = getActualIndex(0, mDistance);
            mArcMf.setProgress(getLabelProgress(0, mDistance));
        }
    }
    public void initMF(){
        if (mOnHintChangeListener != null && mArcMf != null &&mArcMf.getProgress()!=0) {
            Hint hint = savePersistData(mArcMf, mProgress);
            mOnHintChangeListener.onProgressChanged(mArcMf, hint.getValue(), hint.getHint());
        }
    }
    //add by huangfei for MF end

    //bv wuyonglin add for after iso and shutter speed change not auto exp should unavailable 20200117 start
    public int getIsoValue() {
        if (mArcIso != null) {
            return mArcIso.getProgress();
        }
        return 0;
    }

    public int getShutterValue() {
        if (mArcShutter != null) {
            return mArcShutter.getProgress();
        }
        return 0;
    }

    public void setArcExpViewEnable(boolean enable) {
        mArcExp.setArcPaintColor(enable);
	if (enable) {
            mArcExp.setOnSeekArcChangeListener(this);
	} else {
            mArcExp.setOnSeekArcChangeListener(null);
	}
    }
    //bv wuyonglin add for after iso and shutter speed change not auto exp should unavailable 20200117 end
}
