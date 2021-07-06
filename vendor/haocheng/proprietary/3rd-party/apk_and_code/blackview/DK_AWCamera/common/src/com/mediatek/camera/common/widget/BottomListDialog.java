package com.mediatek.camera.common.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import android.view.ViewGroup.LayoutParams;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.List;
import com.mediatek.camera.common.utils.CameraUtil;

import com.mediatek.camera.R;

public class BottomListDialog extends AlertDialog implements View.OnClickListener {

    public static Context mContext;

    private static List<BottomListMenuItem> btnMenu;

    private static String mTitle;
    private static String mSelectedValue;
    private static BottomListDialog dialog;
    private static List<String> mTitleList = new ArrayList<>();
    private static List<String> mSummaryList = new ArrayList<>();
    private static boolean isAlreadyShow = false;

    public static class BottomListMenuItem {
        private String content;
        private OnClickPositionListener clickListener;

        /**
         * @param content
         * @param clickListener
         */
        public BottomListMenuItem(String content, OnClickPositionListener clickListener) {
            this.content = content;
            this.clickListener = clickListener;
        }


        public BottomListMenuItem(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public OnClickPositionListener getClickListener() {
            return clickListener;
        }

        public void setClickListener(OnClickPositionListener clickListener) {
            this.clickListener = clickListener;
        }
    }

    public BottomListDialog(@NonNull Context context) {
        super(context);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window win = getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        View decorView = win.getDecorView();
        decorView.setBackground(mContext.getResources().getDrawable(R.drawable.bv_dialog_background));
        win.getDecorView().setPadding(0, 0, 0, 0);
        win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        Settings.Global.putInt(mContext.getContentResolver(), "dk.light.navigation", 1);

        win.setGravity(Gravity.BOTTOM);
        win.setWindowAnimations(R.style.BvDialogStyleAnimation);

        win.setAttributes(lp);

        if (CameraUtil.isEdgeToEdgeEnabled(mContext)){
            setContentView(R.layout.common_bottom_list_dialog_gestural);
        }else {
            setContentView(R.layout.common_bottom_list_dialog);
        }

        setContentView(R.layout.common_bottom_list_dialog);
        setCanceledOnTouchOutside(true);
        setCancelable(true);

        initView();
    }


    private void initView() {
        LinearLayout lyContents = (LinearLayout) findViewById(R.id.menu_content);
        if (btnMenu != null && btnMenu.size() > 0) {
            for (int i = 0; i < btnMenu.size(); i++) {
                final int index = i;
                View v = View.inflate(mContext, R.layout.common_bottom_list_dialog_item, null);
                v.findViewById(R.id.menu_line).setVisibility(View.GONE);
                if (btnMenu.get(i).getContent().equals(mSelectedValue)) {
                    v.findViewById(R.id.checked_icon).setVisibility(View.VISIBLE);
                } else {
                    v.findViewById(R.id.checked_icon).setVisibility(View.GONE);
                }

                TextView mTvContent = (TextView) v.findViewById(R.id.menu_button);
                mTvContent.setText(mTitleList.get(i));
                TextView mTvSummary = (TextView) v.findViewById(R.id.menu_summary);
                LinearLayout mLinearLayoutMenu = (LinearLayout) v.findViewById(R.id.ll_menu);
                if (mSummaryList != null) {
                    LinearLayout.LayoutParams mLayoutParams = (LinearLayout.LayoutParams) mLinearLayoutMenu.getLayoutParams();
                    mLayoutParams.height = mContext.getResources().getDimensionPixelSize(R.dimen.ll_summary_menu_height);
                    mLinearLayoutMenu.setLayoutParams(mLayoutParams);
                    LayoutParams p = mTvContent.getLayoutParams();
                    p.height = LayoutParams.WRAP_CONTENT;
                    mTvContent.setLayoutParams(p);
                    mTvSummary.setText(mSummaryList.get(i));
                } else {
                    mTvSummary.setVisibility(View.GONE);
                }
                final OnClickPositionListener mOnClickPositionListener = btnMenu.get(i).getClickListener();
                mLinearLayoutMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mOnClickPositionListener) {
                            mOnClickPositionListener.onClickPosition(index);
                            dismiss();
                        }
                    }
                });
                lyContents.addView(v);

            }
        }

        if (mTitle != null) {
            TextView menuTitle = (TextView) findViewById(R.id.menu_title);
            menuTitle.setText(mTitle);
        }
        findViewById(R.id.menu_base_content).setOnClickListener(this);
        findViewById(R.id.menu_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.menu_cancel || i == R.id.menu_base_content) {
            dismiss();
        }

    }

    public static class Builder {

        public Builder(Context context, String title, String selectedValue, List<String> titleList, List<String> summaryList) {
            mContext = context;
            mTitle = title;
            mSelectedValue = selectedValue;
            mTitleList = titleList;
            mSummaryList = summaryList;
            btnMenu = new ArrayList<BottomListMenuItem>();
        }

        /**
         * @param item
         */
        public Builder addMenuItem(BottomListMenuItem item) {
            btnMenu.add(item);
            return this;
        }

        /**
         * @param mReportList
         * @param clickListener
         */
        public Builder addMenuListItem(String[] mReportList, OnClickPositionListener clickListener) {
            BottomListMenuItem item = null;
            for (int i = 0; i < mReportList.length; i++) {
                item = new BottomListMenuItem(mReportList[i], clickListener);
                btnMenu.add(item);
            }
            return this;
        }


        public BottomListDialog show() {
            if (null != mContext  && !isAlreadyShow && (!(mContext instanceof Activity) || !((Activity) mContext).isFinishing())) {
                dialog = new BottomListDialog(mContext);
                isAlreadyShow = true;
                dialog.show();
                return dialog;
            }
            return null;
        }

        public void hide() {
            //bv wuyonglin add for monkey test not attached to window manager 20210123 start
            if (null != mContext) {
                android.util.Log.d("geek","((Activity) mContext).isRestricted() ="+((Activity) mContext).isRestricted()+" isFinishing ="+((Activity) mContext).isFinishing());
            }
            if (dialog != null && null != mContext && dialog.isShowing() && !((Activity) mContext).isRestricted()) {
            //bv wuyonglin add for monkey test not attached to window manager 20210123 end
                dialog.dismiss();
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        //bv xiaoye modify for bug-2216 20200919 start
        if (mContext != null){
            Settings.Global.putInt(mContext.getContentResolver(), "dk.light.navigation", 0);
        }
        isAlreadyShow = false;
        //bv xiaoye modify for bug-2216 20200919 end

        btnMenu = null;
        mContext = null;
    }
}
