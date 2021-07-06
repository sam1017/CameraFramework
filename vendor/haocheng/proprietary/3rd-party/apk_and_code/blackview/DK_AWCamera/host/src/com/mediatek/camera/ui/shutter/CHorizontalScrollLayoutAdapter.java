package com.mediatek.camera.ui.shutter;
import com.mediatek.camera.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

public class CHorizontalScrollLayoutAdapter {
    private List<ShutterButtonManager.ModeHolder> mDatas;
    private LayoutInflater mInflater;
    private int mLayoutId;
    private int mGapValue = 0;

    private class ViewHolder {
        TextView mText;
    }

    public CHorizontalScrollLayoutAdapter(Context context, List<ShutterButtonManager.ModeHolder> datas, int layoutid) {
        mLayoutId = layoutid;
        mInflater = LayoutInflater.from(context);
        mDatas = datas;
        mGapValue = context.getResources().getDimensionPixelSize(R.dimen.bv_gap_value_of_mode_title);
    }

    public int getCount() {
        return mDatas.size();
    }

    public View getView(int id, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(mLayoutId, parent, false);
            view.setPadding(mGapValue, 0, mGapValue, 0);    //bv wuyonglin modify for adjust all icon position from 17 to 34 20200612
            holder.mText = ((TextView) view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.mText.setText((CharSequence) mDatas.get(id).getShutterName());
        return view;
    }

}
