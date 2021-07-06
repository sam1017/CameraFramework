/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.ui.modepicker;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.feature.mode.pro.ProEntry;
import com.mediatek.camera.feature.mode.mono.MonoEntry;
import com.mediatek.camera.feature.mode.aiworksbokeh.AiWorksBokehEntry;
import com.mediatek.camera.feature.mode.aiworksbokehcolor.AiWorksBokehColorEntry;
//bv wuyonglin add for Superphoto should fixed picture size 20201023 start
import com.mediatek.camera.feature.mode.superphoto.SuperphotoEntry;
//bv wuyonglin add for Superphoto should fixed picture size 20201023 end
import com.mediatek.camera.feature.mode.slowmotion.SlowMotionEntry;
import android.widget.RelativeLayout;

import java.util.List;

/**
 *  Mode item adapter, to store the supported mode list for recycler view.
 */
 class ModeItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * Define the Mode item type.
     */
    public enum ITEM_TYPE {
        ITEM_TYPE_IMAGE,
    }
    private static final LogUtil.Tag TAG = new LogUtil.Tag(ModeItemAdapter.class.getSimpleName());
    private String mCurrentModeName = "Normal";
    private Context mContext;

    /**
     * List view item click listener.
     */
    public interface  OnViewItemClickListener {
        /**
         * When a mode item is clicked, notify the event.
         * @param modeInfo Clicked mode info.
         * @return Is the mode change success.
         */
        boolean onItemCLicked(ModePickerManager.ModeInfo modeInfo);
    }

    private LayoutInflater mLayoutInflater;
    private OnViewItemClickListener mClickedListener;
    private List<ModePickerManager.ModeInfo> mModes;

    /**
     * constructor of ModeItemAdapter.
     * @param context App context.
     */
    public ModeItemAdapter(Context context, OnViewItemClickListener listener) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mClickedListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ModeViewHolder(mLayoutInflater.inflate(R.layout.mode_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ModeViewHolder) {
            //Since the visualsearch feature enters by clicking the icon of setting feature,
            //the mode feature name is obtained to hide its corresponding icon
            //get visualsearch mode name and hide icon start
            String visualsearch_mode_name= mContext.getResources().getString(mContext.getResources().getIdentifier("visual_search_mode_title",
            "string", mContext.getPackageName()));
            if (mModes.get(position).mName.equals(visualsearch_mode_name)) {
                ((ModeViewHolder) holder).mModeView.setVisibility(View.GONE);
                ((ModeViewHolder) holder).mTextView.setText("");
                ((ModeViewHolder) holder).mModeView.setContentDescription("");
            }
            //get visualsearch mode name and hide icon end
            ((ModeViewHolder) holder).mTextView.setText(mModes.get(position).mName);
            ((ModeViewHolder) holder).mModeView.setContentDescription(
                    mModes.get(position).mName);
            LogHelper.d(TAG, "onBindViewHolder: mode name = " + mModes.get(position).mName
                         + " position = " + position+" mPriority ="+mModes.get(position).mPriority);



            /*if (mModes.get(position).mName.equals(mCurrentModeName)) {
                if (mModes.get(position).mSelectedIcon != null) {
                    ((ModeViewHolder) holder).mImageView.setImageDrawable(
                            mModes.get(position).mSelectedIcon);
                } else {
                    ((ModeViewHolder) holder).mImageView.setImageResource(
                            R.drawable.ic_normal_mode_selected);
                }
            } else {
                if (mModes.get(position).mUnselectedIcon != null) {
                    ((ModeViewHolder) holder).mImageView.setImageDrawable(
                            mModes.get(position).mUnselectedIcon);
                } else {
                    ((ModeViewHolder) holder).mImageView.setImageResource(
                            R.drawable.ic_normal_mode_unselected);
                }
            }*/
             RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ((ModeViewHolder) holder).mImageView.getLayoutParams();
            layoutParams.topMargin = mContext.getResources().getDimensionPixelSize(R.dimen.image_view_top);
            ((ModeViewHolder) holder).mImageView.setVisibility(View.VISIBLE);
            if (mModes.get(position).mPriority == ProEntry.MODE_ITEM_PRIORITY) {
                    ((ModeViewHolder) holder).mImageView.setImageResource(
                            R.drawable.ic_pro_mode);
            } else if (mModes.get(position).mPriority == MonoEntry.MODE_ITEM_PRIORITY) {
                    ((ModeViewHolder) holder).mImageView.setImageResource(
                            R.drawable.ic_mono_mode);
            } else if (mModes.get(position).mPriority == AiWorksBokehEntry.MODE_ITEM_PRIORITY) {
                    ((ModeViewHolder) holder).mImageView.setImageResource(
                            R.drawable.ic_portrait_mode);
            } else if (mModes.get(position).mPriority == AiWorksBokehColorEntry.MODE_ITEM_PRIORITY) {
                    ((ModeViewHolder) holder).mImageView.setImageResource(
                            R.drawable.ic_portrait_color_mode);
            //bv wuyonglin add for Superphoto should fixed picture size 20201023 start
            } else if (mModes.get(position).mPriority == SuperphotoEntry.MODE_ITEM_PRIORITY) {
                    ((ModeViewHolder) holder).mImageView.setImageResource(
                            R.drawable.ic_superphoto_mode);
            //bv wuyonglin add for Superphoto should fixed picture size 20201023 end
            } else if (mModes.get(position).mPriority == SlowMotionEntry.MODE_ITEM_PRIORITY) {
                    ((ModeViewHolder) holder).mImageView.setImageResource(
                            R.drawable.ic_slowmotion_mode);
            //bv wuyonglin add for more mode item position 20200903 start
            } else if (mModes.get(position).mPriority == 90 || mModes.get(position).mPriority == 91 || mModes.get(position).mPriority == 92) {
                    ((ModeViewHolder) holder).mImageView.setVisibility(View.INVISIBLE);
                    layoutParams.topMargin = 0;
                    ((ModeViewHolder) holder).mImageView.setLayoutParams(layoutParams);
            //bv wuyonglin add for more mode item position 20200903 end
            }
            ((ModeViewHolder) holder).mTextView.setTag(mModes.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM_TYPE.ITEM_TYPE_IMAGE.ordinal();
    }

    @Override
    public int getItemCount() {
        return  mModes == null ? 0 : mModes.size();
    }


    public void setModeList(List<ModePickerManager.ModeInfo> modeList) {
        mModes = modeList;
        notifyDataSetChanged();
    }

    public void updateCurrentModeName(String name) {
        mCurrentModeName = name;
    }

    /**
     *  Mode View Holder, use to show in the Recycler view.
     */
    private class ModeViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView mTextView;
        ImageView mImageView;
        View mModeView;

        ModeViewHolder(View view) {
            super(view);
            mModeView = view;
            mTextView = (TextView) view.findViewById(R.id.text_view);
            mImageView = (ImageView) view.findViewById(R.id.image_view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickedListener.onItemCLicked((ModePickerManager.ModeInfo) mTextView.getTag())) {
                mCurrentModeName = ((ModePickerManager.ModeInfo) mTextView.getTag()).mName;
                LogHelper.d(TAG, "onClick: mode name = " + mCurrentModeName);
                notifyDataSetChanged();
            }
        }
    }
}

