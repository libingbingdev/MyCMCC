package com.cmccpoc.activity.home.widget;



public interface VideoPanelListener
{
    public void onVideoPanelPreviewSurfaceCreated();

    public void onVideoPanelPreviewSurfaceDestroyed();

    public void onVideoPanelPreviewSurfaceChanged();

    public void onVideoPanelScreenChanged(VideoPanel.SCREEN_MODE screenMode);

}
