/*
 *   Copyright Statement:
 *
 *     This software/firmware and related documentation ("MediaTek Software") are
 *     protected under relevant copyright laws. The information contained herein is
 *     confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *     the prior written permission of MediaTek inc. and/or its licensors, any
 *     reproduction, modification, use or disclosure of MediaTek Software, and
 *     information contained herein, in whole or in part, shall be strictly
 *     prohibited.
 *
 *     MediaTek Inc. (C) 2016. All rights reserved.
 *
 *     BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *    THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *     RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *     ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *     WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *     WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *     NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *     RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *     INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *     TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *     RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *     OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *     SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *     RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *     STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *     ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *     RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *     MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *     CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *     The following software/firmware and/or related documentation ("MediaTek
 *     Software") have been modified by MediaTek Inc. All revisions are subject to
 *     any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.camera.common.preference;

import android.content.Context;
import android.preference.PreferenceScreen;
import androidx.annotation.RequiresApi;
import androidx.annotation.IdRes;
import android.util.AttributeSet;
import android.view.View;

//add by huangfei for height of setting item start
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mediatek.camera.R;
//add by huangfei for height of setting item end
//bv wuyonglin add for setting ui 20200923 start
import android.widget.Checkable;
import android.widget.Switch;
import android.widget.RelativeLayout;
//bv wuyonglin add for setting ui 20200923 end

/**
 * Define a new switch preference class extends {@link android.preference.Preference}
 * to set view's id and content description.
 */

public class SwitchPreference extends android.preference.SwitchPreference {
    /**
     * This parameter define that setting view is disable or hidden when it isn't selectable.
     * True means its view should be hidden when it isn't selectable, false means its view
     * should be disable when it isn't selectable.
     */
    public static final boolean JUST_DISABLE_UI_WHEN_NOT_SELECTABLE = false;

    private CharSequence mContentDescription;
    @IdRes
    private int mID = View.NO_ID;
    private PreferenceScreen mRootPreference;
    private boolean mRemoved = false;
    //bv wuyonglin add for setting ui 20200923 start
    private boolean mDisableView = false;
    //bv wuyonglin add for setting ui 20200923 end

	//add by huangfei for height of setting item start
    private Context mContext;
	//add by huangfei for height of setting item end
	
    /**
     * Perform inflation from XML and apply a class-specific base style. This
     * constructor of Preference allows subclasses to use their own base style
     * when they are inflating.
     * @param context The Context this is associated with, through which it can
     *            access the current theme, resources.
     * @param attrs The attributes of the XML tag that is inflating the
     *            preference.
     * @param defStyleAttr An attribute in the current theme that contains a
     *            reference to a style resource that supplies default values for
     *            the view. Can be 0 to not look for defaults.
     * @param defStyleRes A resource identifier of a style resource that
     *            supplies default values for the view, used only if
     *            defStyleAttr is 0 or can not be found in the theme. Can be 0
     *            to not look for defaults.
     */
    public SwitchPreference(Context context, AttributeSet attrs,
                            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
		
		//add by huangfei for height of setting item start
        mContext = context; 
		//add by huangfei for height of setting item end
    }

    /**
     * Perform inflation from XML and apply a class-specific base style. This
     * constructor of Preference allows subclasses to use their own base style
     * when they are inflating.
     *
     * @param context The Context this is associated with, through which it can
     *            access the current theme, resources.
     * @param attrs The attributes of the XML tag that is inflating the
     *            preference.
     * @param defStyleAttr An attribute in the current theme that contains a
     *            reference to a style resource that supplies default values for
     *            the view. Can be 0 to not look for defaults.
     */
    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
		
		//add by huangfei for height of setting item start
        mContext = context; 
		//add by huangfei for height of setting item end
    }

    /**
     * Constructor that is called when inflating a Preference from XML. This is
     * called when a Preference is being constructed from an XML file, supplying
     * attributes that were specified in the XML file. This version uses a
     * default style of 0, so the only attribute values applied are those in the
     * Context's Theme and the given AttributeSet.
     *
     * @param context The Context this is associated with, through which it can
     *            access the current theme, resources.
     * @param attrs The attributes of the XML tag that is inflating the
     *            preference.
     */
    public SwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
		
		//add by huangfei for height of setting item start
        mContext = context; 
		//add by huangfei for height of setting item end
    }

    /**
     * Constructor to create a Preference.
     *
     * @param context The Context in which to store Preference values.
     */
    public SwitchPreference(Context context) {
        super(context);
	
		//add by huangfei for height of setting item start
        mContext = context; 
		//add by huangfei for height of setting item end		
    }
	
	//add by huangfei for height of setting item start
    @Override
    protected View onCreateView(ViewGroup parent) {
         View view = super.onCreateView(parent);
 
         //bv wuyonglin delete for setting ui 20200927 start
         /*try {
            int height = mContext.getResources().getDimensionPixelOffset(R.dimen.settings_item_customized_height);
            if(height==0){
                view.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;               
            }else{
                view.getLayoutParams().height = height;
            }          
         } catch (Exception e){
             e.printStackTrace();
         }*/
         //bv wuyonglin delete for setting ui 20200927 end
 
         return view;
 
     }	
	//add by huangfei for height of setting item end

    @Override
    protected void onBindView(View view) {
        if (mContentDescription != null) {
            view.setContentDescription(mContentDescription);
        }
        if (mID != View.NO_ID) {
            view.setId(mID);
        }
        super.onBindView(view);

        //bv wuyonglin add for setting ui 20200923 start
        View checkableView = view.findViewById(com.android.internal.R.id.switch_widget);
        TextView textView = view.findViewById(com.android.internal.R.id.title);
        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) checkableView.getLayoutParams();
        params1.setMargins(params1.leftMargin, params1.topMargin,
                mContext.getResources().getDimensionPixelSize(R.dimen.switch_preference_margin_left), params1.bottomMargin);
        checkableView.setLayoutParams(params1);
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) textView.getLayoutParams();
        params2.setMargins(mContext.getResources().getDimensionPixelSize(R.dimen.switch_preference_margin_left), params2.topMargin,
                params2.rightMargin, params2.bottomMargin);
        textView.setLayoutParams(params2);
        if (checkableView != null && checkableView instanceof Checkable) {
            if (checkableView instanceof Switch) {
                final Switch switchView = (Switch) checkableView;
                switchView.setThumbResource(R.drawable.thumb_bv);
                switchView.setTrackResource(R.drawable.track_bv);
            }
        }
        setEnabledStateOnViews(view, isEnabled());
        //bv wuyonglin add for setting ui 20200923 end
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (JUST_DISABLE_UI_WHEN_NOT_SELECTABLE) {
            super.setEnabled(enabled);
            return;
        }
        //bv wuyonglin add for setting ui 20200923 start
        if (mDisableView) {
            mRootPreference.addPreference(this);
            super.setEnabled(enabled);
        } else {
            if (enabled) {
                mRootPreference.addPreference(this);
                mRemoved = false;
            } else if (!mRemoved) {
                mRootPreference.removePreference(this);
                mRemoved = true;
            /*add by liangchangwei for fixbug 7040 start */
            }else{
                mRootPreference.removePreference(this);
            /*add by liangchangwei for fixbug 7040 end */
            }
        }
        //bv wuyonglin add for setting ui 20200923 end
    }

    /**
     * Set view content description.
     *
     * @param contentDescription The content description.
     */
    public void setContentDescription(CharSequence contentDescription) {
        mContentDescription = contentDescription;
    }

    /**
     * Sets the identifier for this view. The identifier does not have to be
     * unique in this view's hierarchy. The identifier should be a positive
     * number.
     *
     * @param id a number used to identify the view
     */
    public void setId(@IdRes int id) {
        mID = id;
    }

    /**
     * Set the root preference of this preference.
     *
     * @param rootPreference The root preference of this preference.
     */
    public void setRootPreference(PreferenceScreen rootPreference) {
        mRootPreference = rootPreference;
    }

    //bv wuyonglin add for setting ui 20200923 start
    private void setEnabledStateOnViews(View v, boolean enabled) {
        v.setEnabled(enabled);
        if (v instanceof Switch) {
            if (!enabled) {
                v.setEnabled(!enabled);
                setEnabledStateOnViews(v, !enabled);
            }
        }

        if (v instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) v;
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                if (vg.getChildAt(i) instanceof Switch) {
                    setEnabledStateOnViews(vg.getChildAt(i), !enabled);
                    if (!enabled && ((Switch) vg.getChildAt(i)).isChecked()) {
                        ((Switch) vg.getChildAt(i)).setTrackResource(R.drawable.unabled_track_blue);
                    }
                } else {
                    setEnabledStateOnViews(vg.getChildAt(i), enabled);
                }
            }
        }
    }

    public void setDisableView(boolean disableView){
        mDisableView = disableView;
    }
    //bv wuyonglin add for setting ui 20200923 end
}
