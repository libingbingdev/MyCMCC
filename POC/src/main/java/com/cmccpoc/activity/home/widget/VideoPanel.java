package com.cmccpoc.activity.home.widget;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeMediaVideoControl;
import com.airtalkee.sdk.OnMediaVideoProxyPlayerListener;
import com.airtalkee.sdk.entity.AirVideoShare;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.Util;
import com.cmccpoc.widget.ijkPlayer.IjkVideoView;
import com.cmccpoc.config.*;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by H on 2017/8/16.
 */

public class VideoPanel implements OnMediaVideoProxyPlayerListener, View.OnClickListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnInfoListener
{
    private Activity mActivity;
    private VideoPanelListener mListener = null;
    private int screenHeight = 0;
    private int screenWidth = 0;

    private final static int TOGGLE_TIME_MIN = 3*1000;
    private long toggleTimestamp = 0;

    private boolean nativeLoaded = false;

    public enum SCREEN_MODE
    {
        A, B, C, D
    }

    private SCREEN_MODE mScreenMode = SCREEN_MODE.A;

    private RelativeLayout mContainerA;
    private RelativeLayout mContainerB;

    private RelativeLayout mSurfacePlayerLayout;
    private IjkVideoView mSurfacePlayer;
    private TextView mSurfacePlayerInfo;

    private AirVideoShare mVideoShare = null;

    private SurfaceView mSurfacePreview;
    private ImageView mPreviewButton;
    private boolean mPreviewVisible = true;

    private int mvWidth = 0;
    private int mvHeight = 0;
    private boolean mvVertical = true;


    public VideoPanel(Activity activity, VideoPanelListener listener)
    {
        mActivity = activity;
        mListener = listener;

        mContainerA = (RelativeLayout) activity.findViewById(R.id.video_screen_full);
        mContainerB = (RelativeLayout) activity.findViewById(R.id.video_screen_small);

        RelativeLayout mark = (RelativeLayout) activity.findViewById(R.id.video_surface_player_mark);
        mark.setOnClickListener(this);
        mSurfacePlayerLayout = (RelativeLayout) activity.findViewById(R.id.video_surface_player_layout);
        mSurfacePlayerInfo = (TextView) activity.findViewById(R.id.video_surface_player_info);
        mSurfacePlayerInfo.setVisibility(View.GONE);
        mSurfacePlayer = (IjkVideoView) activity.findViewById(R.id.video_surface_player);
        mSurfacePlayer.setOnPreparedListener(this);
        mSurfacePlayer.setOnCompletionListener(this);
        mSurfacePlayer.setOnErrorListener(this);
        mSurfacePlayer.setOnInfoListener(this);

        mSurfacePreview = (SurfaceView) activity.findViewById(R.id.video_surface_recorder);
        mSurfacePreview.setOnClickListener(this);
        mSurfacePreview.getHolder().addCallback(mSurfaceHolder);

        mPreviewButton = (ImageView) activity.findViewById(R.id.video_preview);
        mPreviewButton.setOnClickListener(this);

        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;

        screenRefresh(mScreenMode);

        if (Config.funcVideoPlayProxy)
            AirtalkeeMediaVideoControl.getInstance().setOnMediaVideoProxyPlayerListener(this);
    }

    public void finish()
    {
        playerStop();
        if (Config.funcVideoPlayProxy)
            AirtalkeeMediaVideoControl.getInstance().setOnMediaVideoProxyPlayerListener(null);
    }

    public SurfaceHolder getSurfaceHolder()
    {
        return mSurfacePreview.getHolder();
    }

    public SCREEN_MODE getScreenMode()
    {
        return mScreenMode;
    }

    public void videoPosition(boolean isVertical)
    {
        if (isVertical)
        {
            RelativeLayout.LayoutParams pm = (RelativeLayout.LayoutParams) mContainerB.getLayoutParams();
            pm.setMargins(0, 0, 0, Util.ui_dip2px(mActivity, 150));
            mContainerB.setLayoutParams(pm);

            RelativeLayout.LayoutParams am = (RelativeLayout.LayoutParams) mPreviewButton.getLayoutParams();
            am.setMargins(0, 0, 0, Util.ui_dip2px(mActivity, 150));
            mPreviewButton.setLayoutParams(am);
        }
        else
        {
            RelativeLayout.LayoutParams pm = (RelativeLayout.LayoutParams) mContainerB.getLayoutParams();
            pm.setMargins(Util.ui_dip2px(mActivity, 100), 0, 0, Util.ui_dip2px(mActivity, 50));
            mContainerB.setLayoutParams(pm);

            RelativeLayout.LayoutParams am = (RelativeLayout.LayoutParams) mPreviewButton.getLayoutParams();
            am.setMargins(Util.ui_dip2px(mActivity, 100), 0, 0, Util.ui_dip2px(mActivity, 50));
            mPreviewButton.setLayoutParams(am);
        }
    }

    public void videoPreviewSetVisiblity(boolean isVisable)
    {
        mPreviewVisible = isVisable;
        if (isVisable)
            screenActionDoPreviewShow();
        else
            screenActionDoPreviewGone();
    }

    public void videoPreviewReset(int videoWidth, int videoHeight, boolean isVertical)
    {
        if (mPreviewVisible)
        {
            mvWidth = videoWidth;
            mvHeight = videoHeight;
            mvVertical = isVertical;
            mSurfacePreview.setVisibility(View.GONE);
            mSurfacePreview.setVisibility(View.VISIBLE);
            //changeVideoSize();
        }
    }

    public void videoScreenToggle()
    {
        //screenActionDoToggle();
    }

    public void videoPlayerStart(AirVideoShare videoShare)
    {
        screenActionDoPlayStart(videoShare);
    }

    public void videoPlayerStop()
    {
        screenActionDoPlayStop();
    }

    /*****************************************
     *
     * Handle SurfaceHolder
     *
     *****************************************/

    private void changeVideoSize() {
        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        float max;
        WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        if (mvVertical) {
            //竖屏模式下按视频宽度计算放大倍数值
            max = Math.max((float) mvWidth / (float) width,(float) mvHeight / (float) height);
        } else{
            //横屏模式下按视频高度计算放大倍数值
            max = Math.max(((float) mvWidth/(float) height),(float) mvHeight/(float) width);
        }

        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        mvWidth = (int) Math.ceil((float) mvWidth / max);
        mvHeight = (int) Math.ceil((float) mvHeight / max);

        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        mSurfacePreview.setLayoutParams(new RelativeLayout.LayoutParams(mvWidth, mvHeight));
    }

    private SurfaceHolder.Callback mSurfaceHolder = new SurfaceHolder.Callback()
    {
        @Override
        public void surfaceDestroyed(SurfaceHolder holder)
        {
            if (mListener != null)
                mListener.onVideoPanelPreviewSurfaceDestroyed();
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder)
        {
            if (mListener != null)
                mListener.onVideoPanelPreviewSurfaceCreated();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            if (mListener != null)
                mListener.onVideoPanelPreviewSurfaceChanged();
        }
    };

    /*****************************************
     *
     * Handle screen actions
     *
     *****************************************/

    private void screenRefresh(SCREEN_MODE mode)
    {
        switch (mode)
        {
            case A:
            {
                mContainerA.removeAllViews();
                mContainerA.setVisibility(View.VISIBLE);
                mContainerB.removeAllViews();
                mContainerB.setVisibility(View.GONE);
                mContainerA.addView(mSurfacePreview);
                mContainerB.addView(mSurfacePlayerLayout);
                mPreviewButton.setVisibility(View.GONE);
                mSurfacePreview.setZOrderMediaOverlay(false);
                break;
            }
            case B:
            {
                mContainerA.removeAllViews();
                mContainerA.setVisibility(View.VISIBLE);
                mContainerB.removeAllViews();
                mContainerB.setVisibility(View.VISIBLE);
                mContainerA.addView(mSurfacePreview);
                mContainerB.addView(mSurfacePlayerLayout);
                mPreviewButton.setVisibility(View.GONE);
                mSurfacePreview.setZOrderMediaOverlay(false);
                break;
            }
            case C:
            {
                mContainerA.removeAllViews();
                mContainerA.setVisibility(View.VISIBLE);
                mContainerB.removeAllViews();
                mContainerB.setVisibility(View.VISIBLE);
                mContainerA.addView(mSurfacePlayerLayout);
                mContainerB.addView(mSurfacePreview);
                mPreviewButton.setVisibility(View.GONE);
                mSurfacePreview.setZOrderMediaOverlay(true);
                break;
            }
            case D:
            {
                mContainerA.removeAllViews();
                mContainerA.setVisibility(View.VISIBLE);
                mContainerB.removeAllViews();
                mContainerB.setVisibility(View.GONE);
                mContainerA.addView(mSurfacePlayerLayout);
                //mContainerB.addView(mSurfacePreview);
                mPreviewButton.setVisibility(View.VISIBLE);
                mSurfacePreview.setZOrderMediaOverlay(false);
                break;
            }
        }
    }

    private void screenActionDoPlayStart(AirVideoShare videoShare)
    {
        switch (mScreenMode)
        {
            case A:
            {
                if (mPreviewVisible)
                    mScreenMode = SCREEN_MODE.C;
                else
                    mScreenMode = SCREEN_MODE.D;
                screenRefresh(mScreenMode);
                if (mListener != null)
                    mListener.onVideoPanelScreenChanged(mScreenMode);
                playerStart(videoShare);
                break;
            }
            case B:
            {
                if (mPreviewVisible)
                    mScreenMode = SCREEN_MODE.C;
                else
                    mScreenMode = SCREEN_MODE.D;
                screenRefresh(mScreenMode);
                if (mListener != null)
                    mListener.onVideoPanelScreenChanged(mScreenMode);
                playerStart(videoShare);
                break;
            }
            case C:
            {
                playerStart(videoShare);
                break;
            }
            case D:
            {
                playerStart(videoShare);
                break;
            }
        }
    }

    private void screenActionDoPlayStop()
    {
        switch (mScreenMode)
        {
            case A:
            {
                // Nothing to do
                break;
            }
            case B:
            {
                mScreenMode = SCREEN_MODE.A;
                screenRefresh(mScreenMode);
                playerStop();
                if (mListener != null)
                    mListener.onVideoPanelScreenChanged(mScreenMode);
                break;
            }
            case C:
            {
                mScreenMode = SCREEN_MODE.A;
                screenRefresh(mScreenMode);
                playerStop();
                if (mListener != null)
                    mListener.onVideoPanelScreenChanged(mScreenMode);
                break;
            }
            case D:
            {
                mScreenMode = SCREEN_MODE.A;
                screenRefresh(mScreenMode);
                playerStop();
                if (mListener != null)
                    mListener.onVideoPanelScreenChanged(mScreenMode);
                break;
            }
        }
    }

    private void screenActionDoToggle()
    {
        if (System.currentTimeMillis() - toggleTimestamp < TOGGLE_TIME_MIN)
        {
            if(Toast.isDebug) Toast.makeText1(mActivity, mActivity.getString(R.string.talk_video_tip_fast_toggle), Toast.LENGTH_SHORT).show();
            return;
        }

        toggleTimestamp = System.currentTimeMillis();
        switch (mScreenMode)
        {
            case A:
            {
                // Nothing to do
                break;
            }
            case B:
            {
                if (mPreviewVisible)
                    mScreenMode = SCREEN_MODE.C;
                else
                    mScreenMode = SCREEN_MODE.D;
                screenRefresh(mScreenMode);
                if (mListener != null)
                    mListener.onVideoPanelScreenChanged(mScreenMode);
                break;
            }
            case C:
            {
                mScreenMode = SCREEN_MODE.B;
                screenRefresh(mScreenMode);
                if (mListener != null)
                    mListener.onVideoPanelScreenChanged(mScreenMode);
                break;
            }
            case D:
            {
                mScreenMode = SCREEN_MODE.B;
                screenRefresh(mScreenMode);
                if (mListener != null)
                    mListener.onVideoPanelScreenChanged(mScreenMode);
                break;
            }
        }
    }

    private void screenActionDoPreviewShow()
    {
        switch (mScreenMode)
        {
            case A:
            {
                // Nothing to do
                break;
            }
            case B:
            {
                // Nothing to do
                break;
            }
            case C:
            case D:
            {
                mScreenMode = SCREEN_MODE.B;
                screenRefresh(mScreenMode);
                if (mListener != null)
                    mListener.onVideoPanelScreenChanged(mScreenMode);
                break;
            }
        }
    }

    private void screenActionDoPreviewGone()
    {
        switch (mScreenMode)
        {
            case A:
            {
                // Nothing to do
                break;
            }
            case B:
            {
                // Nothing to do
                break;
            }
            case C:
            {
                mScreenMode = SCREEN_MODE.D;
                screenRefresh(mScreenMode);
                if (mListener != null)
                    mListener.onVideoPanelScreenChanged(mScreenMode);
                break;
            }
            case D:
            {
                // Nothing to do
                break;
            }
        }
    }

    private void playerStart(AirVideoShare videoShare)
    {
        if (!nativeLoaded)
        {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
            nativeLoaded = true;
        }

        if (videoShare != mVideoShare)
        {
            mVideoShare = videoShare;
            mSurfacePlayerInfo.setVisibility(View.GONE);
            if (Config.funcVideoPlayProxy)
            {
                AirtalkeeMediaVideoControl.getInstance().VideoActionProxyPlayStart(mVideoShare);
            }
            else
            {
                if (mSurfacePlayer.isPlaying())
                    mSurfacePlayer.stopPlayback();
                mSurfacePlayer.setVideoPath(mVideoShare.getUrl());
                mSurfacePlayer.start();
            }
        }
    }

    private void playerStop()
    {
        if (mVideoShare != null)
        {
            if (mSurfacePlayer.isPlaying())
                mSurfacePlayer.stopPlayback();
            mSurfacePlayer.release(true);
            if (Config.funcVideoPlayProxy)
                AirtalkeeMediaVideoControl.getInstance().VideoActionProxyPlayStop(mVideoShare);
            mVideoShare = null;
        }

        if (nativeLoaded)
        {
            IjkMediaPlayer.native_profileEnd();
            nativeLoaded = false;
        }
    }

    /*****************************************
     *
     * Events
     *
     *****************************************/

    @Override
    public void onVideoProxyPlayerStart(int sessionId, String urlProxy)
    {
        Log.i(VideoPanel.class, "onVideoProxyPlayerStart: sessionId=" + sessionId + " urlProxy=" + urlProxy);
        if (mVideoShare != null && mVideoShare.getSessionId() == sessionId)
        {
            if (mSurfacePlayer.isPlaying())
                mSurfacePlayer.stopPlayback();
            mSurfacePlayer.setVideoPath(urlProxy);
            mSurfacePlayer.start();
        }
    }

    @Override
    public void onVideoProxyPlayerStop(int sessionId) {
        Log.i(VideoPanel.class, "onVideoProxyPlayerStop: sessionId=" + sessionId);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.video_surface_player_mark:
            {
                if (mScreenMode == SCREEN_MODE.B)
                    screenActionDoToggle();
                break;
            }
            case R.id.video_surface_recorder:
            {
                if (mScreenMode == SCREEN_MODE.C)
                    screenActionDoToggle();
                break;
            }
            case R.id.video_preview:
            {
                screenActionDoToggle();
                break;
            }
        }
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        mSurfacePlayerInfo.setVisibility(View.GONE);
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        mSurfacePlayerInfo.setVisibility(View.GONE);
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        return false;
    }
}
