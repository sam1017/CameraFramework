package com.mediatek.camera.feature.setting;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class AnimationFactory {

    private int mTranslateY;
    private int mDestination;
    private int mAnimationDuration;
    private ImageView mImageView;
    private FrameLayout.LayoutParams lp_imageview;

    public AnimationFactory() {

    }

    public TranslateAnimation creatAnimation(int TranslateDistance, int EndDestination, ImageView imageView, int duration){

        mTranslateY = TranslateDistance;
        mDestination = EndDestination;
        mImageView = imageView;
        mAnimationDuration = duration;
        lp_imageview = (FrameLayout.LayoutParams) mImageView.getLayoutParams();

        TranslateAnimation mAnimation = new TranslateAnimation(0,0,0,mTranslateY);
        mAnimation.setDuration(mAnimationDuration);
        mAnimation.setFillAfter(true);
        mAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mImageView.clearAnimation();
                lp_imageview.topMargin = mDestination;
                mImageView.setLayoutParams(lp_imageview);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return mAnimation;
    }
}
