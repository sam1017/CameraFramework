package com.mediatek.camera.ui.shutter;
import com.mediatek.camera.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;
import com.mediatek.camera.common.widget.RotateImageView;
import com.mediatek.camera.feature.setting.TripleSwitchHorizontal;
import android.widget.ImageView;
import com.mediatek.camera.ui.CircleTextView;

public class TripleSwitchHorizontalAdapter {
    private List<TripleSwitchHorizontal.SwitchHolder> mDatas;
    private LayoutInflater mInflater;
    private int mLayoutId;
    private int mGapValue = 0;
    public static final int TRIPLE_SWITCH_MACRO = 0;  
    public static final int TRIPLE_SWITCH_WIDE = 1; 
    public static final int TRIPLE_SWITCH_NORMAL = 2;
    public static final int TRIPLE_SWITCH_ZOOM = 3;
    public static final int TRIPLE_SWITCH_NUM_ALL = 4;
    private static final int[] TRIPLE_SWITCH_ICONS = new int[TRIPLE_SWITCH_NUM_ALL];
    static {
        TRIPLE_SWITCH_ICONS[TRIPLE_SWITCH_MACRO] = R.drawable.ic_triple_switch_macro;
        TRIPLE_SWITCH_ICONS[TRIPLE_SWITCH_WIDE] = R.drawable.ic_triple_switch_wide;
        TRIPLE_SWITCH_ICONS[TRIPLE_SWITCH_NORMAL] = R.drawable.ic_triple_switch_normal;
        TRIPLE_SWITCH_ICONS[TRIPLE_SWITCH_ZOOM] = R.drawable.ic_triple_switch_zoom;
    };

    private class ViewHolder {
        //CircleTextView mText;
        ImageView mImageView;        
    }

    public TripleSwitchHorizontalAdapter(Context context, List<TripleSwitchHorizontal.SwitchHolder> datas, int layoutid) {
        mLayoutId = layoutid;
        mInflater = LayoutInflater.from(context);
        mDatas = datas;
        mGapValue = context.getResources().getDimensionPixelSize(R.dimen.triple_gap_value_of_mode_title);
        //mGapValue = context.getResources().getInteger(R.integer.gap_value_of_mode_title);
    }

    public int getCount() {
        return mDatas.size();
    }

    public View getView(int index, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.triple_switch_horizontal_item, parent, false);
            view.setPadding(mGapValue, 0, mGapValue, 0);
            holder.mImageView = (RotateImageView)view.findViewById(R.id.triple_switch_icon);
            //holder.mText = (CircleTextView)view.findViewById(R.id.triple_switch_select);
            holder.mImageView.setImageResource(TRIPLE_SWITCH_ICONS[mDatas.get(index).getSwitchHolderId()]);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        return view;
    }

}
