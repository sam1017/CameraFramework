/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2016. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.ui;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ICameraSettingView;

import java.util.ArrayList;
import java.util.List;

//*/ hct.huangfei, 20201130. enable navigationbar.
import com.mediatek.camera.common.utils.CameraUtil;
import android.view.Window;
import android.view.WindowManager;
//*/
//bv wuyonglin add for setting ui 20200923 start
import com.mediatek.camera.common.preference.BvPreferenceCategory;
import com.mediatek.camera.common.preference.Preference;
import com.mediatek.camera.CameraActivity;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import java.util.Set;
import java.util.Iterator;
import com.mediatek.camera.BvUtils;
import android.app.AlertDialog;
//bv wuyonglin add for setting ui 20200923 end

/**
 * Provide setting UI for camera.
 */
public class SettingFragment extends PreferenceFragment {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(SettingFragment.class.getSimpleName());

    private List<ICameraSettingView> mSettingViewList = new ArrayList<>();
    private StateListener mStateListener;
    private Toolbar mToolbar;
    //bv wuyonglin add for setting ui 20200923 start
    private BvPreferenceCategory mVideoCategoryPreference;
    private BvPreferenceCategory mPhotoCategoryPreference;
    private Preference mRestoreSettingsPreference;
    private AlertDialog mAlertDialog;
    //bv wuyonglin add for setting ui 20200923 end

    /**
     * Listener to listen setting fragment's state.
     */
    public interface StateListener {
        /**
         * Callback when setting fragment is created.
         */
        public void onCreate();

        /**
         * Callback when setting fragment is resumed.
         */
        public void onResume();

        /**
         * Callback when setting fragment is paused.
         */
        public void onPause();

        /**
         * Callback when setting fragment is destroyed.
         */
        public void onDestroy();

        //bv wuyonglin add for setting ui 20200923 start
        public void onRestoreSetting();
        //bv wuyonglin add for setting ui 20200923 end
    }

    public void setStateListener(StateListener listener) {
        mStateListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogHelper.d(TAG, "[onCreate]");
        if (mStateListener != null) {
            mStateListener.onCreate();
        }
        super.onCreate(savedInstanceState);

        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if (mToolbar != null) {
            /*add by liangchangwei for fixbug 7040 start */
            initToolBar();
            /*add by liangchangwei for fixbug 7040 end */
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LogHelper.i(TAG, "[onClick], activity:" + getActivity());
                    if (getActivity() != null) {
                        getActivity().getFragmentManager().popBackStack();
                    }
                }
            });
        }


        addPreferencesFromResource(R.xml.camera_preferences);

        synchronized (this) {
            for (ICameraSettingView view : mSettingViewList) {
                LogHelper.i(TAG, "[loadView], mSettingViewList view:" + view);
                view.loadView(this);
            }
        }

	//bv wuyonglin add for setting ui 20200923 start
        if ("SlowMotion".equals(((CameraActivity) getActivity()).getAppUi().getCurrentMode())) {
            mPhotoCategoryPreference = (BvPreferenceCategory) findPreference("photo_category_screen");
            getPreferenceScreen().removePreference(mPhotoCategoryPreference);
            mVideoCategoryPreference = (BvPreferenceCategory) findPreference("video_category_screen");
            getPreferenceScreen().removePreference(mVideoCategoryPreference);
	} else if (!"Video".equals(((CameraActivity) getActivity()).getAppUi().getCurrentMode())) {
            mVideoCategoryPreference = (BvPreferenceCategory) findPreference("video_category_screen");
            getPreferenceScreen().removePreference(mVideoCategoryPreference);
	} else {
            mPhotoCategoryPreference = (BvPreferenceCategory) findPreference("photo_category_screen");
            getPreferenceScreen().removePreference(mPhotoCategoryPreference);
	}

        mRestoreSettingsPreference = (Preference) findPreference("key_restore_settings");
        mRestoreSettingsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                String title = (String) preference.getTitle();
                if (mAlertDialog != null && mAlertDialog.isShowing()) {
                    return true;
                }
                mAlertDialog = BvUtils.generateNormalDialog(getActivity(), title, getActivity().getResources().getString(R.string.restore_settings_message), null, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogHelper.d(TAG, "[onClick] v =" + v);
                        clearSharedPreferences();
                        if (mStateListener != null) {
                            mStateListener.onRestoreSetting();
                        }
                        getActivity().getFragmentManager().popBackStack();
                    }
                }, null, false);
                mAlertDialog.show();
                return true;
            }
        });
        /* -- add by bv liangchangwei for fixbug 3105 start --*/
        if(CameraUtil.is_videoHdr_Force){
            getPreferenceScreen().removePreference(mRestoreSettingsPreference);
        }
        /* -- add by bv liangchangwei for fixbug 3105 end --*/
	//bv wuyonglin add for setting ui 20200923 end
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogHelper.d(TAG, "[onActivityCreated]");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        LogHelper.d(TAG, "[onResume]");
        super.onResume();
	//bv wuyonglin add for setting ui 20200923 start
        getListView().setDivider(null);
        /*if (mToolbar != null) {
            mToolbar.setTitle(getActivity().getResources().getString(R.string.setting_title));
        }*/
	//bv wuyonglin add for setting ui 20200923 end
        synchronized (this) {
            for (ICameraSettingView view : mSettingViewList) {
                view.refreshView();
            }
        }
        if (mStateListener != null) {
            mStateListener.onResume();
        }
    }

    @Override
    public void onPause() {
        LogHelper.d(TAG, "[onPause]");
        super.onPause();

        if (mStateListener != null) {
            mStateListener.onPause();
        }
        //bv wuyonglin add for dialog not disappear 20200930 start
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        //bv wuyonglin add for dialog not disappear 20200930 end
    }

    @Override
    public void onDestroy() {
        LogHelper.d(TAG, "[onDestroy]");
        super.onDestroy();
        synchronized (this) {
            for (ICameraSettingView view : mSettingViewList) {
                view.unloadView();
            }
        }
        if (mStateListener != null) {
            mStateListener.onDestroy();
        }
        //*/ hct.huangfei, 202011204. enable navigationbar.
        //enableNavigationbar();
        //*/

    }

    /**
     * Add setting view instance to setting view list.
     *
     * @param view The instance of {@link ICameraSettingView}.
     */
    public synchronized void addSettingView(ICameraSettingView view) {
        if (view == null) {
            LogHelper.w(TAG, "[addSettingView], view:" + view, new Throwable());
            return;
        }
        if (!mSettingViewList.contains(view)) {
            mSettingViewList.add(view);
        }
    }

    /**
     * Remove setting view instance from setting view list.
     *
     * @param view The instance of {@link ICameraSettingView}.
     */
    public synchronized void removeSettingView(ICameraSettingView view) {
        mSettingViewList.remove(view);
    }

    /**
     * Refresh setting view.
     */
    public synchronized void refreshSettingView() {
        for (ICameraSettingView view : mSettingViewList) {
            view.refreshView();
        }
    }

    /**
     * Whether setting view tree has any visible child or not. True means it has at least
     * one visible child, false means it don't has any visible child.
     *
     * @return False if setting view tree don't has any visible child.
     */
    public synchronized boolean hasVisibleChild() {
        if (ICameraSettingView.JUST_DISABLE_UI_WHEN_NOT_SELECTABLE) {
            return mSettingViewList.size() > 0;
        }

        boolean visible = false;
        for (ICameraSettingView view : mSettingViewList) {
            if (view.isEnabled()) {
                visible = true;
            }
        }
        return visible;
    }

    //*/ hct.huangfei, 20201130. enable navigationbar.
    private void enableNavigationbar(){
        if(!CameraUtil.isEdgeToEdgeEnabled(getActivity())){
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                );
        }
    }
    //*/

    //bv wuyonglin add for setting ui 20200923 start
    private void clearSharedPreferences() {
        getActivity().getSharedPreferences("camera_sound",Context.MODE_PRIVATE).edit().clear().apply();
        getActivity().getSharedPreferences("com.mediatek.camera_global_scope_saving_timestamp",Context.MODE_PRIVATE).edit().clear().apply();
        getActivity().getSharedPreferences("com.mediatek.camera_preferences",Context.MODE_PRIVATE).edit().clear().apply();
        getActivity().getSharedPreferences("com.mediatek.camera_preferences_0",Context.MODE_PRIVATE).edit().clear().apply();
        getActivity().getSharedPreferences("com.mediatek.camera_preferences_0_saving_timestamp",Context.MODE_PRIVATE).edit().clear().apply();
        getActivity().getSharedPreferences("com.mediatek.camera_preferences_1",Context.MODE_PRIVATE).edit().clear().apply();
        getActivity().getSharedPreferences("com.mediatek.camera_preferences_2",Context.MODE_PRIVATE).edit().clear().apply();
        getActivity().getSharedPreferences("storage_path",Context.MODE_PRIVATE).edit().clear().apply();
    }
    //bv wuyonglin add for setting ui 20200923 end
    /*add by liangchangwei for fixbug 7040 start */
   public void initToolBar(){
       boolean mLastNightMode = (getActivity().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
               == Configuration.UI_MODE_NIGHT_YES;

       LogHelper.i(TAG,"lcw initToolBar mLastNightMode = " + mLastNightMode);
       if (mToolbar != null) {
           //bv wuyonglin add for setting ui 20200923 start
           //mToolbar.setTitle(getActivity().getResources().getString(R.string.setting_title));
           mToolbar.setBackgroundColor(getResources().getColor(R.color.white));
           TextView textView = mToolbar.findViewById(R.id.bv_toolbar_title);
           if (textView != null){
               textView.setTextColor(getActivity().getResources().getColor(R.color.black));
               textView.setText(getActivity().getResources().getString(R.string.setting_title));
           }
           //mToolbar.setTitleTextColor(
           //        getActivity().getResources().getColor(android.R.color.white));
           Drawable drawable ;
           if(CameraUtil.isRTL){
               drawable = getActivity().getResources().getDrawable(R.drawable.ic_bv_back_rtl);
           }else{
               drawable = getActivity().getResources().getDrawable(R.drawable.ic_bv_back);
           }
           mToolbar.setNavigationIcon(drawable);
           //bv wuyonglin add for setting ui 20200923 end
       }
   }
    /*add by liangchangwei for fixbug 7040 end */

}
