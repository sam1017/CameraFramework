package com.mediatek.camera.common.mode.photo.view;


/**
 * Created by young on 18-10-30.
 */

public interface IZoomSliderUI {

    void init();

    void cameraSwitch();

    void setZoomConfig(Object object);

    void unInit();

    void setZoomSliderUIListener(ZoomSliderUIListener zoomSliderUIListener);

    public interface ZoomSliderUIListener {
        void onZoomSliderReady(String ratio,float direction);
        void onSlidingArcViewHide(float ratio);
    }

    void removeZoomSliderUIListener(ZoomSliderUIListener zoomSliderUIListener);

    void showCircleTextView();

    boolean isShowAll();
}
