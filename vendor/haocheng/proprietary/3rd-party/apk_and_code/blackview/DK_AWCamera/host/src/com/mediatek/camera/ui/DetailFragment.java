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

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import javax.annotation.Nullable;

import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.utils.CameraUtil;
//bv wuyonglin add for detail fragment auto adapt 20210407 start
import com.mediatek.camera.feature.mode.night.NightEntry;
import com.mediatek.camera.feature.mode.pro.ProEntry;
import com.mediatek.camera.feature.mode.mono.MonoEntry;
import com.mediatek.camera.feature.mode.aiworksfacebeauty.AiworksFaceBeautyEntry;
import com.mediatek.camera.feature.mode.aiworksbokeh.AiWorksBokehEntry;
import com.mediatek.camera.feature.mode.aiworksbokehcolor.AiWorksBokehColorEntry;
import com.mediatek.camera.feature.mode.superphoto.SuperphotoEntry;
import com.mediatek.camera.feature.mode.slowmotion.SlowMotionEntry;
//bv wuyonglin add for detail fragment auto adapt 20210407 end

import java.util.ArrayList;

/**
 * Provide setting UI for camera.
 */
public class DetailFragment extends Fragment {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(DetailFragment.class.getSimpleName());

    private Toolbar mToolbar;
    private SettingFragment.StateListener mStateListener;
    //bv wuyonglin add for detail fragment auto adapt 20210407 start
    private int[] modePriority;
    //bv wuyonglin add for detail fragment auto adapt 20210407 end

    private static final int DETAIL_NIGHT = 0;
    private static final int DETAIL_VIDEO = 1;
    private static final int DETAIL_PHOTO = 2;
    private static final int DETAIL_BEAUTY = 3;
    private static final int DETAIL_PROF = 4;
    private static final int DETAIL_SLOWMOTION = 5;
    private static final int DETAIL_4800W = 6;
    private static final int DETAIL_BOKEH = 7;
    private static final int DETAIL_BOKEHCOLOR = 8;
    private static final int DETAIL_COUNT = 9;

    ArrayList<CameraItem> data = new ArrayList<CameraItem>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.i(TAG,"DetailFragment [onCreate ]");
        if (mStateListener != null) {
            mStateListener.onCreate();
        }
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        LogHelper.d(TAG, "[onCreate] mToolbar = " + mToolbar);
        if (mToolbar != null) {
            //bv wuyonglin add for setting ui 20200923 start
            //mToolbar.setTitle(getActivity().getResources().getString(R.string.setting_title));
            LogHelper.i(TAG,"mToolbar setBackgroundColor black");
            mToolbar.setBackgroundColor(getResources().getColor(R.color.black));
            TextView textView = mToolbar.findViewById(R.id.bv_toolbar_title);
            if (textView != null){
                textView.setTextColor(getActivity().getResources().getColor(R.color.white));
                textView.setText(getActivity().getResources().getString(R.string.detail_title));
            }
            LogHelper.i(TAG,"mToolbar setNavigationIcon ic_bv_detail_back");
            Drawable drawable ;
            if(CameraUtil.isRTL){
                drawable = getActivity().getResources().getDrawable(R.drawable.ic_bv_detail_back_rtl);
            }else{
                drawable = getActivity().getResources().getDrawable(R.drawable.ic_bv_detail_back);
            }
            mToolbar.setNavigationIcon(drawable);
            //bv wuyonglin add for setting ui 20200923 end

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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogHelper.i(TAG,"DetailFragment [onCreateView ]");
        View view = inflater.inflate(R.layout.detail_layout, container, false);
        ListView listView = view.findViewById(R.id.list_view);
        initData();
        listView.setAdapter(new CamearAdapter());
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LogHelper.i(TAG,"DetailFragment [onViewCreated ]");
        view.setBackgroundResource(R.color.black);
    }

    @Nullable
    @Override
    public View getView() {
        return super.getView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        LogHelper.i(TAG,"onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mStateListener != null) {
            mStateListener.onResume();
        }
        LogHelper.i(TAG,"onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mStateListener != null) {
            mStateListener.onPause();
        }
        LogHelper.i(TAG,"onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mStateListener != null) {
            mStateListener.onDestroy();
        }
    }

    public void setStateListener(SettingFragment.StateListener listener) {
        mStateListener = listener;
    }


    class CamearAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            DetailHolder holder;
            /* add by bv liangchangwei for fixbug 3323 -- */
            boolean isFirst = false;
            /* add by bv liangchangwei for fixbug 3323 -- */
            if (view == null){
                view = View.inflate(getContext(), R.layout.detail_item, null);
                holder = new DetailHolder();
                holder.icon = view.findViewById(R.id.icon);
                holder.title = view.findViewById(R.id.title);
                holder.summary = view.findViewById(R.id.summary);
                view.setTag(holder);
                /* add by bv liangchangwei for fixbug 3323 -- */
                isFirst = true;
                /* add by bv liangchangwei for fixbug 3323 -- */
            } else {
                holder = (DetailHolder) view.getTag();
            }

            holder.icon.setImageDrawable(data.get(i).getIcon());
            holder.title.setText(data.get(i).getTitle());
            holder.summary.setText(data.get(i).getSummary());
            /* add by bv liangchangwei for fixbug 3323 -- */
            if(isFirst){
                RelativeLayout.LayoutParams params =
                        (RelativeLayout.LayoutParams) holder.title.getLayoutParams();
                int w = View.MeasureSpec.makeMeasureSpec(0,
                        View.MeasureSpec.UNSPECIFIED);
                int h = View.MeasureSpec.makeMeasureSpec(0,
                        View.MeasureSpec.UNSPECIFIED);
                holder.icon.measure(w, h);
                int iconheight = holder.icon.getMeasuredHeight();
                holder.title.measure(w,h);
                int titleheight = holder.title.getMeasuredHeight();
                int modifyY = (int)((iconheight - titleheight)/2);
                params.topMargin = params.topMargin + modifyY;
                holder.title.setLayoutParams(params);
            }
            /* add by bv liangchangwei for fixbug 3323 -- */
            return view;
        }

    }


    static class DetailHolder{
        ImageView icon;
        TextView title;
        TextView summary;
    }

    class CameraItem {
        private Drawable icon;
        private String title;
        private String summary;

        public CameraItem(Drawable icon, String title, String summary){
            this.icon = icon;
            this.title = title;
            this.summary = summary;
        }

        public Drawable getIcon() {
            return this.icon;
        }

        public String getTitle() {
            return this.title;
        }

        public String getSummary() {
            return this.summary;
        }
    }


    public void initData(){
        data.clear();
        //bv wuyonglin add for detail fragment auto adapt 20210407 start
        if (CameraUtil.getDeviceModel().equals("BL5000")) {
            modePriority = getActivity().getResources().getIntArray(R.array.mode_item_priority_bl5000);
        } else {
            modePriority = getActivity().getResources().getIntArray(R.array.mode_item_priority);
        }
        for(int i = 0; i < modePriority.length; i++){
            addCameraItem(modePriority[i]);
        }
        //bv wuyonglin add for detail fragment auto adapt 20210407 end
    }

    //bv wuyonglin modify for detail fragment auto adapt 20210407 start
    public void addCameraItem(int id){
        switch(id){
            case NightEntry.MODE_ITEM_PRIORITY:
                data.add(new CameraItem(getActivity().getDrawable(R.drawable.night),
                        getActivity().getString(R.string.night_title),
                        getActivity().getString(R.string.night_summary)));
                break;
            case 10:
                data.add(new CameraItem(getActivity().getDrawable(R.drawable.video),
                        getActivity().getString(R.string.video_title),
                        getActivity().getString(R.string.video_summary)));
                break;
            case 5:
                data.add(new CameraItem(getActivity().getDrawable(R.drawable.photo),
                        getActivity().getString(R.string.photo_title),
                        getActivity().getString(R.string.photo_summary)));
                break;
            case AiworksFaceBeautyEntry.MODE_ITEM_PRIORITY:
                data.add(new CameraItem(getActivity().getDrawable(R.drawable.beauty),
                        getActivity().getString(R.string.beauty_title),
                        getActivity().getString(R.string.beauty_summary)));
                break;
            case ProEntry.MODE_ITEM_PRIORITY:
                data.add(new CameraItem(getActivity().getDrawable(R.drawable.prof),
                        getActivity().getString(R.string.prof_title),
                        getActivity().getString(R.string.prof_summary)));
                break;
            case SlowMotionEntry.MODE_ITEM_PRIORITY:
                data.add(new CameraItem(getActivity().getDrawable(R.drawable.ic_slowmotion_mode),
                        getActivity().getString(R.string.slow_motion_title),
                        getActivity().getString(R.string.slowmotion_mode_summary)));
                break;
            case SuperphotoEntry.MODE_ITEM_PRIORITY:
                data.add(new CameraItem(getActivity().getDrawable(R.drawable.mode4800),
                        getActivity().getString(R.string.w4800_title),
                        getActivity().getString(R.string.w4800_summary)));
                break;
            case AiWorksBokehEntry.MODE_ITEM_PRIORITY:
                data.add(new CameraItem(getActivity().getDrawable(R.drawable.bokeh),
                        getActivity().getString(R.string.bokeh_title),
                        getActivity().getString(R.string.bokeh_summary)));
                break;
            case AiWorksBokehColorEntry.MODE_ITEM_PRIORITY:
                data.add(new CameraItem(getActivity().getDrawable(R.drawable.bokehcolor),
                        getActivity().getString(R.string.bokehcolor_title),
                        getActivity().getString(R.string.bokehcolor_summary)));
                break;
            case MonoEntry.MODE_ITEM_PRIORITY:
                data.add(new CameraItem(getActivity().getDrawable(R.drawable.mono),
                        getActivity().getString(R.string.mono_title),
                        getActivity().getString(R.string.mono_summary)));
                break;
            default:
                //Log.i(TAG,"");
                break;
        }
    //bv wuyonglin modify for detail fragment auto adapt 20210407 end
    }

}
