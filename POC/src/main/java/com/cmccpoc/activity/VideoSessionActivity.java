package com.cmccpoc.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeMediaVideoControl;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeReport;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnMediaListener;
import com.airtalkee.sdk.OnMediaVideoAudioStreamListener;
import com.airtalkee.sdk.OnMediaVideoPictureListener;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.controller.MessageController;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.entity.AirVideoShare;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.sdk.video.audio.AudioStream;
import com.airtalkee.sdk.video.ctl.VideoRealRecorder;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.IMFragment;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.activity.home.widget.VideoList;
import com.cmccpoc.activity.home.widget.VideoListSelectListener;
import com.cmccpoc.activity.home.widget.VideoPanel;
import com.cmccpoc.activity.home.widget.VideoPanelListener;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.control.AirTaskCaseManager;
import com.cmccpoc.control.AirVideoManager;
import com.cmccpoc.entity.AirTaskCase;
import com.cmccpoc.listener.OnMmiLocationListener;
import com.cmccpoc.listener.OnMmiSessionListener;
import com.cmccpoc.listener.OnMmiVideoKeyListener;
import com.cmccpoc.listener.OnMmiVideoListener;
import com.cmccpoc.location.AirLocation;
import com.cmccpoc.receiver.ReceiverVideoKey;
import com.cmccpoc.util.AirMmiTimer;
import com.cmccpoc.util.AirMmiTimerListener;
import com.cmccpoc.util.Setting;
import com.cmccpoc.util.Sound;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 实时视频回传界面
 @author Yao */
public class VideoSessionActivity extends ActivityBase implements OnClickListener, OnTouchListener, AirMmiTimerListener, OnMmiSessionListener, OnMediaListener, OnCheckedChangeListener, OnMmiVideoListener, OnMediaVideoPictureListener, OnMmiVideoKeyListener, OnMediaVideoAudioStreamListener, VideoListSelectListener, VideoPanelListener

{
    public static final int VIDEO_TYPE_NONE = 0;
    public static final int VIDEO_TYPE_SYS = 10;

    public static final int VIDEO_STATE_NONE = 0;
    public static final int VIDEO_STATE_CONNECTING = 1;
    public static final int VIDEO_STATE_PREVIEW = 2;
    public static final int VIDEO_STATE_TRANSFER_CONNETING = 3;
    public static final int VIDEO_STATE_TRANSFERING = 4;
    public static final int VIDEO_STATE_TRANSFER_DISCONNECTING = 5;

    public static final int VIDEO_ACTION_DEVICE_CONNECT = 10;
    public static final int VIDEO_ACTION_DEVICE_CONNECTED_OK = 11;
    public static final int VIDEO_ACTION_DEVICE_CONNECTED_FAIL = 12;
    public static final int VIDEO_ACTION_DEVICE_DISCONNECT = 13;
    public static final int VIDEO_ACTION_DEVICE_RESET = 14;
    public static final int VIDEO_ACTION_TRANSFER_START = 15;
    public static final int VIDEO_ACTION_TRANSFER_STARTED_OK = 16;
    public static final int VIDEO_ACTION_TRANSFER_STARTED_FAIL = 17;
    public static final int VIDEO_ACTION_TRANSFER_STOP = 18;
    public static final int VIDEO_ACTION_TRANSFER_STOPPED = 19;

    private static VideoSessionActivity mInstance;
    private AirSession session;
    private AirSession sessionOrg = null;
    private int sessionVideoId = -1;
    private int cameraType = VIDEO_TYPE_SYS;
    private boolean cameraAuto = false;
    private boolean isCameraOpen = false;
    private String owner = "";

    private boolean mvSurfaceViewRunning = false;

    // private FrameLayout videoSettingsLayout;
    private RelativeLayout videoPanel;
    private TextView videoStatusText;
    private View flashPannel, capturePannel, voicePannel, voicePannelLand;

    private ImageView btnTalkVideo, btnTalkVideoLand, icVideoStatus;

    private ImageView ivVideoBtn, ivVideoBtnLand, mFlashButton, mCaptureButton, mVoiceButton, mVoiceButtonLand;
    private TextView tvVideoText, tvVideoTextLand, mVoiceText, mVoiceTextLand;

    private long mCameraCaptureTs = 0;
    private final int CAMERA_CAPTURE_TIME_GAP = 5000;

    private int mVideoState = VIDEO_STATE_NONE;

    private int mVideoWidth = 0;
    private int mVideoHeight = 0;
    private int mvFramerate = 0;
    private int mvBitrate = 0;

    private TextView tv_status;
    private Chronometer ch_time;
    private View iv_back;
    private TextView tvSetting;

    private boolean allowSettingSwitch = false;

    private RadioGroup videoSettingRadio;
    private View popSettingView;
    private PopupWindow popSettingWindow;// 弹出窗口
    private AirMessage iMessage;
    private String videoTime = "00:00";
    private boolean settingMode = false;
    private AirTaskCase mTaskCase = null;
    private boolean mVoiceIsPending = false;

    private AirVideoShare currentVideoShare = null;
    private VideoList mVideoList = null;
    private VideoPanel mVideoWindow = null;

    /**
     获取VideoSessionActivity实例对象
     @return
     */
    public static VideoSessionActivity getInstance()
    {
        return mInstance;
    }

    /**
     获取录制状态
     @return
     */
    public static boolean isRecording()
    {
        boolean isRecording = false;
        if (getInstance() != null)
            isRecording = getInstance().mVideoState >= VIDEO_STATE_TRANSFER_CONNETING && getInstance().mVideoState <= VIDEO_STATE_TRANSFER_DISCONNECTING;
        return isRecording;
    }

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_session_video);
        if (Config.funcVideoScreenSenor)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //setRequestedOrientation(Config.screenOrientation);
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        mInstance = this;

        bundle = getIntent().getExtras();
        if (bundle != null)
        {
            String sessionCode = bundle.getString("sessionCode");
            owner = bundle.getString("owner");
            sessionOrg = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode);
            session = sessionOrg;
            cameraAuto = bundle.getBoolean("auto", false);
            String taskId = bundle.getString("taskId");
            if (!TextUtils.isEmpty(taskId))
                mTaskCase = AirTaskCaseManager.getInstance().getTask(taskId);
            else
                mTaskCase = AirTaskCaseManager.getInstance().getTaskCurrent();
        }
        loadView();

        videoPreviewStart();
        refreshPttState();
        refreshBottom();

        AirMmiTimer.getInstance().TimerRegister(this, this, false, false, 500, true, null);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        AirMmiTimer.getInstance().TimerUnregister(this, this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        refreshBottomControl();
        mVideoWindow.videoPosition(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    /**
     初始化 绑定 控件ID
     */
    private void loadView()
    {
        videoPanel = (RelativeLayout) findViewById(R.id.talk_video_panel);
        videoPanel.setVisibility(View.GONE);
        videoStatusText = (TextView) findViewById(R.id.talk_video_status_panel);
        btnTalkVideo = (ImageView) findViewById(R.id.talk_btn_session_on_video);
        btnTalkVideo.setOnTouchListener(this);
        btnTalkVideoLand = (ImageView) findViewById(R.id.talk_btn_session_on_video_land);
        btnTalkVideoLand.setOnTouchListener(this);
        ivVideoBtn = (ImageView) findViewById(R.id.video_record);
        ivVideoBtn.setOnClickListener(this);
        ivVideoBtnLand = (ImageView) findViewById(R.id.video_record_land);
        ivVideoBtnLand.setOnClickListener(this);
        tvVideoText = (TextView) findViewById(R.id.tv_video_status_tip);
        tvVideoTextLand = (TextView) findViewById(R.id.tv_video_status_tip_land);
        icVideoStatus = (ImageView) findViewById(R.id.talk_video_status_iv);
        tv_status = (TextView) findViewById(R.id.tv_video_status);
        ch_time = (Chronometer) findViewById(R.id.ch_timer);
        if (!Config.funcVideoTimerShow)
            ch_time.setVisibility(View.GONE);
        iv_back = findViewById(R.id.iv_video_back);
        iv_back.setOnClickListener(this);
        tvSetting = (TextView) findViewById(R.id.tv_video_setting);
        tvSetting.setOnClickListener(this);
        flashPannel = findViewById(R.id.video_flash_pannel);
        capturePannel = findViewById(R.id.video_identifier_capture_pannel);

        if (Config.funcVideoSelfCapture)
        {
            capturePannel.setVisibility(View.VISIBLE);
            mCaptureButton = (ImageView) capturePannel.findViewById(R.id.video_identifier_capture);
            mCaptureButton.setOnClickListener(this);
        }
        else
        {
            capturePannel.setVisibility(View.GONE);
            if (Config.funcFlashMode)
            {
                flashPannel.setVisibility(View.VISIBLE);
                mFlashButton = (ImageView) flashPannel.findViewById(R.id.video_flash);
                mFlashButton.setOnClickListener(this);
            }
            else
                flashPannel.setVisibility(View.INVISIBLE);
        }

        voicePannel = findViewById(R.id.video_identifier_voice_pannel);
        voicePannel.setOnClickListener(this);
        voicePannelLand = findViewById(R.id.video_identifier_voice_pannel_land);
        voicePannelLand.setOnClickListener(this);
        mVoiceButton = (ImageView) voicePannel.findViewById(R.id.video_identifier_voice);
        mVoiceButtonLand = (ImageView) voicePannelLand.findViewById(R.id.video_identifier_voice_land);
        mVoiceText = (TextView) voicePannel.findViewById(R.id.video_identifier_voice_text);
        mVoiceTextLand = (TextView) voicePannelLand.findViewById(R.id.video_identifier_voice_text_land);

        mVideoWindow = new VideoPanel(this, this);

        mVideoList = new VideoList(this, this);
        mVideoList.setVisible(Config.funcVideoPlay);
        if (!TextUtils.isEmpty(owner) && session != null)
            mVideoList.activeSet(session.getSessionCode(), owner);
        mVideoList.viewRefresh();
        mVideoList.activePlay();

        AirtalkeeMediaVideoControl.getInstance().setVideoAudioStreamOnOff(Config.funcVideoAudioStream && Setting.getVideoVoice());

        initPopupSettingWindow();
    }

    private void videoPreviewStart()
    {
        if (cameraType != VIDEO_TYPE_NONE)
        {
            if (cameraType == VIDEO_TYPE_SYS)
            {
                boolean showPreview = mVideoState == VIDEO_STATE_TRANSFER_CONNETING || mVideoState == VIDEO_STATE_TRANSFERING;
                mVideoWindow.videoPreviewSetVisiblity(showPreview);
                Log.i(VideoSessionActivity.class, "VideoSessionActivity [VIDEO]+ Video setting: Size=" + mVideoWidth + "x" + mVideoHeight + " Framerate=" + mvFramerate + "(fps) Bitrate=" + mvBitrate / 1000 + "(kbps)");
            }
            videoDeviceStateMachine(VIDEO_ACTION_DEVICE_CONNECT, 0);
        }
    }

    private void videoPreviewStop()
    {
        Log.i(VideoSessionActivity.class, "VideoSessionActivity videoPreviewStop");
        mVideoWindow.videoPreviewSetVisiblity(false);
        videoDeviceStateMachine(VIDEO_ACTION_DEVICE_DISCONNECT, 0);
    }

    private void initPopupSettingWindow()
    {
        popSettingView = LayoutInflater.from(this).inflate(R.layout.layout_popup_window_video_setting, null);
        popSettingWindow = new PopupWindow(popSettingView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popSettingWindow.setOutsideTouchable(true);
        videoSettingRadio = (RadioGroup) popSettingView.findViewById(R.id.rg_video_setting);
        if (Setting.getVideoSettingType() == 0)
        {
            String quality = Setting.getVideoQuality();
            tvSetting.setText(quality);
            if (quality.equals("极速"))
                ((RadioButton) videoSettingRadio.findViewById(R.id.rb_video_low)).setChecked(true);
            else if (quality.equals("标清"))
                ((RadioButton) videoSettingRadio.findViewById(R.id.rb_video_normal)).setChecked(true);
            else if (quality.equals("高清"))
                ((RadioButton) videoSettingRadio.findViewById(R.id.rb_video_high)).setChecked(true);
            else if (quality.equals("超清"))
                ((RadioButton) videoSettingRadio.findViewById(R.id.rb_video_best)).setChecked(true);
        }
        else
        {
            tvSetting.setText("自定义");
            ((RadioButton) videoSettingRadio.findViewById(R.id.rb_video_custom)).setChecked(true);
        }
        for (int i = 0; i < videoSettingRadio.getChildCount(); i++)
        {
            if (videoSettingRadio.getChildAt(i) instanceof RadioButton)
            {
                RadioButton rbCurrent = (RadioButton) videoSettingRadio.getChildAt(i);
                if (rbCurrent.getId() == videoSettingRadio.getCheckedRadioButtonId())
                {
                    rbCurrent.setTextColor(0X7fFF9400);
                    selectQuality(rbCurrent.getId(), false);
                }
                else
                {
                    rbCurrent.setTextColor(getResources().getColor(R.color.white));
                }
            }
        }
        videoSettingRadio.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mVideoList.activeClean();
        videoPreviewStop();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ReceiverVideoKey.setOnMmiVideoKeyListener(null);
        refreshBottom();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (session != null)
        {
            // tvTitle.setText(session.getDisplayName());
            AirtalkeeSessionManager.getInstance().setOnMediaListener(this);
            AirVideoManager.getInstance().setVideoListener(this);
            AirtalkeeMediaVideoControl.getInstance().setOnMediaVideoPictureListener(this);
            AirtalkeeMediaVideoControl.getInstance().setOnVideoAudioStreamListener(this);
            AirSessionControl.getInstance().setOnMmiSessionListener(this);

            refreshPttState();
            refreshBottom();
            videoPreviewStart();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ReceiverVideoKey.setOnMmiVideoKeyListener(this);
    }

    @Override
    public void finish()
    {
        Log.i(VideoSessionActivity.class, "VideoSessionActivity finish");
        AirtalkeeSessionManager.getInstance().setOnMediaListener(null);
        AirVideoManager.getInstance().setVideoListener(null);
        AirtalkeeMediaVideoControl.getInstance().setOnVideoAudioStreamListener(null);
        AirSessionControl.getInstance().setOnMmiSessionListener(null);
        videoPreviewStop();
        mVideoList.activeClean();
        super.finish();
    }

    private void screenOrientationLock()
    {
        if (getScreenRotation() == AirtalkeeMediaVideoControl.CAMERA_SCREEN_ORIENTATION_LANDSCAPE_RIGHT)
        {
            if (!Config.model.equals("DSJ"))
            {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                Log.i(VideoSessionActivity.class, "VideoSessionActivity [VIDEO]+ screenOrientationLock to LANDSCAPE-RIGHT");
            }
        }
        else if (getScreenRotation() == AirtalkeeMediaVideoControl.CAMERA_SCREEN_ORIENTATION_LANDSCAPE_LEFT)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            Log.i(VideoSessionActivity.class, "VideoSessionActivity [VIDEO]+ screenOrientationLock to LANDSCAPE-LEFT");
        }
        else if (getScreenRotation() == AirtalkeeMediaVideoControl.CAMERA_SCREEN_ORIENTATION_PORTRAIT)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            Log.i(VideoSessionActivity.class, "VideoSessionActivity [VIDEO]+ screenOrientationLock to PORTRAIT");
        }
        else
        {
            Log.e(VideoSessionActivity.class, "VideoSessionActivity [VIDEO]+ screenOrientationLock ERROR: (" + getScreenRotation() + ")");
        }
    }

    private void screenOrientationUnlock()
    {
        if (Config.funcVideoScreenSenor)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            Log.i(VideoSessionActivity.class, "VideoSessionActivity [VIDEO]+ screenOrientationUnlock back to SENSOR");
        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            Log.i(VideoSessionActivity.class, "VideoSessionActivity [VIDEO]+ screenOrientationUnlock back to PORTRAIT");
        }
    }

    private int getScreenRotation()
    {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation)
        {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
            {
                if (Config.model.equals("DSJ"))
                {
                    return AirtalkeeMediaVideoControl.CAMERA_SCREEN_ORIENTATION_LANDSCAPE_RIGHT;
                }
                return AirtalkeeMediaVideoControl.CAMERA_SCREEN_ORIENTATION_PORTRAIT;
            }
            case Surface.ROTATION_90:
                return AirtalkeeMediaVideoControl.CAMERA_SCREEN_ORIENTATION_LANDSCAPE_RIGHT;
            case Surface.ROTATION_270:
                return AirtalkeeMediaVideoControl.CAMERA_SCREEN_ORIENTATION_LANDSCAPE_LEFT;
        }
        return 0;
    }

    /**
     刷新PTT按钮状态
     */
    public void refreshPttState()
    {
        if (session == null)
            return;
        switch (session.getMediaButtonState())
        {
            case AirSession.MEDIA_BUTTON_STATE_IDLE:
            case AirSession.MEDIA_BUTTON_STATE_RELEASING:
                btnTalkVideo.setBackgroundResource(R.drawable.video_talk_normal);
                btnTalkVideoLand.setBackgroundResource(R.drawable.video_talk_normal);
                break;
            case AirSession.MEDIA_BUTTON_STATE_TALKING:
                btnTalkVideo.setBackgroundResource(R.drawable.video_talk_press);
                btnTalkVideoLand.setBackgroundResource(R.drawable.video_talk_press);
                break;
            case AirSession.MEDIA_BUTTON_STATE_CONNECTING:
            case AirSession.MEDIA_BUTTON_STATE_REQUESTING:
            case AirSession.MEDIA_BUTTON_STATE_QUEUE:
                btnTalkVideo.setBackgroundResource(R.drawable.video_talk_press);
                btnTalkVideoLand.setBackgroundResource(R.drawable.video_talk_press);
                break;
        }

        if (session.getMediaState() == AirSession.MEDIA_STATE_LISTEN && session.getSpeaker() != null)
        {
            videoStatusText.setText(session.getSpeaker().getIpocId() + " " + getString(R.string.talk_video_listen));
            icVideoStatus.setBackgroundResource(R.drawable.media_listen);
            // videoStatusText.setVisibility(View.VISIBLE);
        }
        else if (session.getMediaState() == AirSession.MEDIA_STATE_TALK)
        {
            videoStatusText.setText(getString(R.string.talk_video_me));
            icVideoStatus.setBackgroundResource(R.drawable.media_talk);
            // videoStatusText.setVisibility(View.VISIBLE);
        }
        else
        {
            if (session.getMediaButtonState() == AirSession.MEDIA_BUTTON_STATE_REQUESTING)
                videoStatusText.setText(getString(R.string.talk_click_applying));
            else
                videoStatusText.setText(getString(R.string.talk_session_speak_idle));
            icVideoStatus.setBackgroundResource(R.drawable.media_talk);
        }
    }

    private void refreshBottom()
    {
        VideoPanel.SCREEN_MODE screenMode = mVideoWindow.getScreenMode();
        if (screenMode == VideoPanel.SCREEN_MODE.A || screenMode == VideoPanel.SCREEN_MODE.B)
        {
            if (allowSettingSwitch)
                tvSetting.setVisibility(View.VISIBLE);

            if (mVideoState == VIDEO_STATE_TRANSFERING || mVideoState == VIDEO_STATE_TRANSFER_CONNETING || mVideoState == VIDEO_STATE_TRANSFER_DISCONNECTING)
            {
                ivVideoBtn.setImageResource(R.drawable.ic_session_video_stop);
                ivVideoBtnLand.setImageResource(R.drawable.ic_session_video_stop);
                tvVideoText.setText(R.string.talk_session_video_stop);
                tvVideoTextLand.setText(R.string.talk_session_video_stop);
            }
            else
            {
                ivVideoBtn.setImageResource(R.drawable.ic_session_video_start);
                ivVideoBtnLand.setImageResource(R.drawable.ic_session_video_start);
                tvVideoText.setText(R.string.talk_session_video_start);
                tvVideoTextLand.setText(R.string.talk_session_video_start);
            }

            if (Config.funcVideoAudioStream)
            {
                voicePannel.setVisibility(View.VISIBLE);
                if (Setting.getVideoVoice())
                {
                    if (mVoiceIsPending)
                    {
                        mVoiceButton.setImageResource(R.drawable.ic_session_va_none);
                        mVoiceButtonLand.setImageResource(R.drawable.ic_session_va_none);
                        mVoiceText.setText(getString(R.string.talk_video_voice_none));
                        mVoiceTextLand.setText(getString(R.string.talk_video_voice_none));
                    }
                    else
                    {
                        mVoiceButton.setImageResource(R.drawable.ic_session_va_active);
                        mVoiceButtonLand.setImageResource(R.drawable.ic_session_va_active);
                        mVoiceText.setText(getString(R.string.talk_video_voice_active));
                        mVoiceTextLand.setText(getString(R.string.talk_video_voice_active));
                    }
                }
                else
                {
                    mVoiceButton.setImageResource(R.drawable.ic_session_va_inactive);
                    mVoiceButtonLand.setImageResource(R.drawable.ic_session_va_inactive);
                    mVoiceText.setText(getString(R.string.talk_video_voice_inactive));
                    mVoiceTextLand.setText(getString(R.string.talk_video_voice_inactive));
                }
            }
            else
            {
                voicePannel.setVisibility(View.GONE);
            }
        }
        else if (screenMode == VideoPanel.SCREEN_MODE.D || screenMode == VideoPanel.SCREEN_MODE.C)
        {
            if (allowSettingSwitch)
                tvSetting.setVisibility(View.GONE);

            ivVideoBtn.setImageResource(R.drawable.ic_session_video_finish);
            ivVideoBtnLand.setImageResource(R.drawable.ic_session_video_finish);
            tvVideoText.setText(R.string.talk_session_video_bye);
            tvVideoTextLand.setText(R.string.talk_session_video_bye);

            if (Config.funcVideoAudioStream)
            {
                voicePannel.setVisibility(View.VISIBLE);
                if (currentVideoShare != null)
                {
                    if (currentVideoShare.isAudioEnabled())
                    {
                        mVoiceButton.setImageResource(R.drawable.ic_session_va_active);
                        mVoiceButtonLand.setImageResource(R.drawable.ic_session_va_active);
                        mVoiceText.setText(getString(R.string.talk_video_voice_active));
                        mVoiceTextLand.setText(getString(R.string.talk_video_voice_active));
                    }
                    else
                    {
                        mVoiceButton.setImageResource(R.drawable.ic_session_va_inactive);
                        mVoiceButtonLand.setImageResource(R.drawable.ic_session_va_inactive);
                        mVoiceText.setText(getString(R.string.talk_video_voice_inactive));
                        mVoiceTextLand.setText(getString(R.string.talk_video_voice_inactive));
                    }
                }
            }
            else
            {
                voicePannel.setVisibility(View.GONE);
            }
        }
    }

    private void refreshBottomControl()
    {
        int r = getScreenRotation();
        if (r == AirtalkeeMediaVideoControl.CAMERA_SCREEN_ORIENTATION_PORTRAIT)
        {
            findViewById(R.id.talk_video_bottom_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.talk_video_bottom_layout_land).setVisibility(View.GONE);
        }
        else
        {
            findViewById(R.id.talk_video_bottom_layout).setVisibility(View.GONE);
            findViewById(R.id.talk_video_bottom_layout_land).setVisibility(View.VISIBLE);
        }
    }

    private void changePlayerSound()
    {
        VideoPanel.SCREEN_MODE screenMode = mVideoWindow.getScreenMode();
        if (screenMode == VideoPanel.SCREEN_MODE.A || screenMode == VideoPanel.SCREEN_MODE.B)
        {
            AirtalkeeMediaVideoControl.getInstance().VideoActionPlaySoundEnable(false);
        }
        else
        {
            if (mVideoState == VIDEO_STATE_TRANSFERING || mVideoState == VIDEO_STATE_TRANSFER_CONNETING)
                AirtalkeeMediaVideoControl.getInstance().VideoActionPlaySoundEnable(false);
            else if (currentVideoShare != null)
            {
                if (session != null)
                {
                    if (session.getMediaState() == AirSession.MEDIA_STATE_IDLE)
                    {
                        AirtalkeeMediaVideoControl.getInstance().VideoActionPlaySoundEnable(currentVideoShare.isAudioEnabled());
                    }
                    else
                    {
                        AirtalkeeMediaVideoControl.getInstance().VideoActionPlaySoundEnable(false);
                    }
                }
                else
                    AirtalkeeMediaVideoControl.getInstance().VideoActionPlaySoundEnable(currentVideoShare.isAudioEnabled());
            }

        }
    }

    @SuppressWarnings("deprecation")
    protected Dialog onCreateDialog(int id)
    {
        if (id == R.id.talk_dialog_waiting)
        {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.requesting));
            dialog.setCancelable(false);
            return dialog;
        }
        return super.onCreateDialog(id);
    }

    private void videoVoiceOn(boolean isOn)
    {
        Log.i(VideoSessionActivity.class, "videoVoiceOn");
        Setting.setVideoVoice(isOn);
        AirtalkeeMediaVideoControl.getInstance().setVideoAudioStreamOnOff(isOn);
    }


    private void reportLocation()
    {
        AirLocation.getInstance(this).onceGet(new OnMmiLocationListener()
        {
            @Override
            public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time, String address)
            {
                // TODO Auto-generated method stub
                AirtalkeeReport.getInstance().ReportLocation(Config.funcLocationMulti, type, latitude, longitude, altitude, 0, speed, "");
            }

            @Override
            public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time)
            {
                // TODO Auto-generated method stub
                AirtalkeeReport.getInstance().ReportLocation(Config.funcLocationMulti, type, latitude, longitude, altitude, 0, speed, "");
            }
        }, 20);
    }

    private boolean videoDeviceStateMachine(int action, int actionParam)
    {
        boolean isHandled = false;
        Log.e(VideoSessionActivity.class, "VideoSessionActivity [VIDEO]+ videoDeviceStateMachine-begin: state=" + mVideoState + " action=" + action);
        switch (mVideoState)
        {
            case VIDEO_STATE_NONE:
            {
                if (action == VIDEO_ACTION_DEVICE_CONNECT)
                {
                    if (cameraType == VIDEO_TYPE_SYS)
                    {
                        mVideoState = VIDEO_STATE_PREVIEW;
                        isHandled = true;
                    }
                }
                break;
            }
            case VIDEO_STATE_CONNECTING:
            {
                break;
            }
            case VIDEO_STATE_PREVIEW:
            {
                if (action == VIDEO_ACTION_DEVICE_DISCONNECT)
                {
                    mVideoState = VIDEO_STATE_NONE;
                    isHandled = true;
                }
                else if (action == VIDEO_ACTION_TRANSFER_START)
                {
                    if (cameraType == VIDEO_TYPE_SYS)
                    {
                        screenOrientationLock();

                        int sampleRateInHz = Setting.getLiveAudioSamplingRate();
                        int channelConfig = Setting.getLiveAudioChannelCount();
                        int audioFormat = Setting.getLiveAudioFormat();
                        int codecProfile = Setting.getLiveAudioAACProfile();
                        AudioStream.getInstance().setRecordParams(sampleRateInHz, channelConfig, audioFormat, codecProfile);

                        String custom = "";
                        if (mTaskCase != null)
                        {
                            String mark = "";
                            String markB64 = "";
                            mark += "账户：" + AirtalkeeAccount.getInstance().getUserId();
                            mark += "\n用户名：" + AirtalkeeAccount.getInstance().getUserName();
                            mark += "\n案件号：" + mTaskCase.getCaseCode();
                            byte[] b = Base64.encode(mark.getBytes(), Base64.DEFAULT);
                            markB64 = new String(b);

                            JSONObject json = new JSONObject();
                            try
                            {
                                json.put("taskId", mTaskCase.getTaskId());
                                json.put("mark", markB64);
                                custom = json.toString();
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        mVideoWindow.videoPreviewSetVisiblity(true);
                        sessionVideoId = AirtalkeeMediaVideoControl.getInstance().VideoActionRecorderStart(session.getSessionCode(), custom);
                        reportLocation();
                    }
                    mVideoState = VIDEO_STATE_TRANSFER_CONNETING;
                    //mVideoWindow.videoScreenPreviewLock(true);
                    isHandled = true;
                }
                else if (action == VIDEO_ACTION_DEVICE_RESET)
                {
                    if (Config.funcVideoPlay && mvSurfaceViewRunning)
                    {
                        AirtalkeeMediaVideoControl.getInstance().VideoActionCameraOrientation(mInstance, getScreenRotation());
                        isHandled = true;
                    }
                }
                break;
            }
            case VIDEO_STATE_TRANSFER_CONNETING:
            {
                if (action == VIDEO_ACTION_TRANSFER_STARTED_OK)
                {
                    mVideoState = VIDEO_STATE_TRANSFERING;
                    isHandled = true;
                }
                else if (action == VIDEO_ACTION_TRANSFER_STARTED_FAIL)
                {
                    mVideoState = VIDEO_STATE_PREVIEW;
                    isHandled = true;
                }
                else if (action == VIDEO_ACTION_TRANSFER_STOP)
                {
                    AirtalkeeMediaVideoControl.getInstance().VideoActionRecorderStop(sessionVideoId);
                    mVideoWindow.videoPreviewSetVisiblity(false);
                    mVideoState = VIDEO_STATE_PREVIEW;
                    screenOrientationUnlock();
                    isHandled = true;
                }
                else if (action == VIDEO_ACTION_DEVICE_DISCONNECT)
                {
                    AirtalkeeMediaVideoControl.getInstance().VideoActionRecorderStop(sessionVideoId);
                    mVideoWindow.videoPreviewSetVisiblity(false);
                    mVideoState = VIDEO_STATE_NONE;
                    screenOrientationUnlock();
                    isHandled = true;
                }
                break;
            }
            case VIDEO_STATE_TRANSFERING:
            {
                if (action == VIDEO_ACTION_TRANSFER_STOP)
                {
                    AirtalkeeMediaVideoControl.getInstance().VideoActionRecorderStop(sessionVideoId);
                    mVideoWindow.videoPreviewSetVisiblity(false);
                    mVideoState = VIDEO_STATE_TRANSFER_DISCONNECTING;
                    screenOrientationUnlock();
                    isHandled = true;
                }
                else if (action == VIDEO_ACTION_DEVICE_DISCONNECT)
                {
                    AirtalkeeMediaVideoControl.getInstance().VideoActionRecorderStop(sessionVideoId);
                    mVideoWindow.videoPreviewSetVisiblity(false);
                    mVideoState = VIDEO_STATE_NONE;
                    screenOrientationUnlock();
                    isHandled = true;
                }
                break;
            }
            case VIDEO_STATE_TRANSFER_DISCONNECTING:
            {
                if (action == VIDEO_ACTION_TRANSFER_STOPPED)
                {
                    mVideoState = VIDEO_STATE_PREVIEW;
                    isHandled = true;
                }
                else if (action == VIDEO_ACTION_DEVICE_DISCONNECT)
                {
                    mVideoState = VIDEO_STATE_NONE;
                    isHandled = true;
                }
                //mVideoWindow.videoScreenPreviewLock(false);
                break;
            }
        }
        Log.e(VideoSessionActivity.class, "VideoSessionActivity [VIDEO]+ videoDeviceStateMachine-end: state=" + mVideoState + " isHandled=" + isHandled);
        return isHandled;
    }

    private void handleStartVideoTransfer()
    {
        ivVideoBtn.setEnabled(false);
        ivVideoBtnLand.setEnabled(false);
        showDialog(R.id.talk_dialog_waiting);
        videoDeviceStateMachine(VIDEO_ACTION_TRANSFER_START, 0);
        refreshBottom();
    }

    private void handleStopVideoTransfer()
    {
        ivVideoBtn.setEnabled(false);
        ivVideoBtnLand.setEnabled(false);
        if (cameraAuto)
        {
            AlertDialog backDialog = new AlertDialog(this, getString(R.string.talk_video_tip_pull_video), null, new AlertDialog.DialogListener()
            {
                @Override
                public void onClickOk(int id, Object obj)
                {
                    if (session != null && session.getType() == AirSession.TYPE_DIALOG)
                        AirtalkeeSessionManager.getInstance().SessionBye(session);
                    showDialog(R.id.talk_dialog_waiting);
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Log.i(VideoSessionActivity.class, "VideoSessionActivity [VIDEO]+ handleStopVideoTransfer1");
                            videoDeviceStateMachine(VIDEO_ACTION_TRANSFER_STOP, 0);
                            refreshBottom();
                        }
                    }, 500);
                }

                @Override
                public void onClickOk(int id, boolean isChecked)
                {
                }

                @Override
                public void onClickCancel(int id)
                {
                    ivVideoBtn.setEnabled(true);
                    ivVideoBtnLand.setEnabled(true);
                }
            }, 1);
            backDialog.show();
        }
        else
        {
            showDialog(R.id.talk_dialog_waiting);
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    Log.i(VideoSessionActivity.class, "VideoSessionActivity [VIDEO]+ handleStopVideoTransfer2");
                    videoDeviceStateMachine(VIDEO_ACTION_TRANSFER_STOP, 0);
                    refreshBottom();
                }
            }, 500);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.video_record:
            case R.id.video_record_land:
            {
                VideoPanel.SCREEN_MODE screenMode = mVideoWindow.getScreenMode();
                if (screenMode == VideoPanel.SCREEN_MODE.A || screenMode == VideoPanel.SCREEN_MODE.B)
                {
                    if (isCameraOpen)
                    {
                        if (mVideoState == VIDEO_STATE_TRANSFERING || mVideoState == VIDEO_STATE_TRANSFER_CONNETING)
                        {
                            handleStopVideoTransfer();
                        }
                        else if (mVideoState == VIDEO_STATE_PREVIEW)
                        {
                            String netType = Util.getCurrentNetType();
                            if (netType.equals("2g"))
                            {
                                AlertDialog dialog = new AlertDialog(this, getString(R.string.talk_tools_session_video_confirm), null, new AlertDialog.DialogListener()
                                {
                                    @Override
                                    public void onClickOk(int id, Object obj)
                                    {
                                        handleStartVideoTransfer();
                                    }

                                    @Override
                                    public void onClickOk(int id, boolean isChecked)
                                    {
                                    }

                                    @Override
                                    public void onClickCancel(int id)
                                    {
                                        ivVideoBtn.setEnabled(true);
                                        ivVideoBtnLand.setEnabled(true);
                                    }
                                }, 0);
                                dialog.show();
                            }
                            else if (netType.equals("null"))
                                if(Toast.isDebug) Toast.makeText1(this, getString(R.string.talk_network_error), Toast.LENGTH_LONG).show();
                            else
                            {
                                handleStartVideoTransfer();
                            }
                        }
                    }
                    else
                        if(Toast.isDebug) Toast.makeText1(this, getString(R.string.talk_camera_open_failed), Toast.LENGTH_LONG).show();
                }
                else if (screenMode == VideoPanel.SCREEN_MODE.D || screenMode == VideoPanel.SCREEN_MODE.C)
                {
                    mVideoList.activeClean();
                }
                break;
            }
            case R.id.tv_video_setting:
            {
                if (!settingMode)
                {
                    if (Config.model.startsWith("SM"))// 三星note
                        popSettingWindow.showAtLocation(popSettingView, Gravity.RIGHT | Gravity.TOP, 5, 300);
                    else
                        popSettingWindow.showAtLocation(popSettingView, Gravity.RIGHT | Gravity.TOP, 5, 150);
                    tvSetting.setBackgroundResource(R.drawable.bg_video_setting_press);
                    tvSetting.setTextColor(0X7fFF9400);
                    settingMode = true;
                }
                else
                {
                    popSettingWindow.dismiss();
                    tvSetting.setBackgroundResource(R.drawable.bg_video_setting_normal);
                    tvSetting.setTextColor(getResources().getColor(R.color.white));
                    settingMode = false;
                }
                break;
            }
            case R.id.iv_video_back:
            {
                finish();
                break;
            }
            case R.id.video_flash:
            {
                boolean isFlashOn = AirtalkeeMediaVideoControl.getInstance().VideoCameraFlashIsOn();
                if (isFlashOn)
                    mFlashButton.setImageResource(R.drawable.ic_flash_off_holo_light);
                else
                    mFlashButton.setImageResource(R.drawable.ic_flash_on_holo_light);
                AirtalkeeMediaVideoControl.getInstance().VideoCameraFlash(!isFlashOn);
                break;
            }
            case R.id.video_identifier_capture:
            {
                if (System.currentTimeMillis() - mCameraCaptureTs >= CAMERA_CAPTURE_TIME_GAP)
                {
                    mCameraCaptureTs = System.currentTimeMillis();
                    AirtalkeeMediaVideoControl.getInstance().VideoCameraCapture(true);
                }
                else
                    Util.Toast(this, getString(R.string.talk_video_tip_take_fast));
                break;
            }
            case R.id.video_identifier_voice_pannel:
            case R.id.video_identifier_voice_pannel_land:
            {
                if (mVideoWindow.getScreenMode() == VideoPanel.SCREEN_MODE.A || mVideoWindow.getScreenMode() == VideoPanel.SCREEN_MODE.B)
                    videoVoiceOn(!Config.funcVideoAudioStreamOn);
                else if (currentVideoShare != null)
                {
                    currentVideoShare.setAudioEnabled(!currentVideoShare.isAudioEnabled());
                    changePlayerSound();
                }
                refreshBottom();
                break;
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {

        if (session != null)
        {
            if (v.getId() == R.id.talk_btn_session_on_video || v.getId() == R.id.talk_btn_session_on_video_land)
            {
                if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
                {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        Log.i(VideoSessionActivity.class, "TalkButton onLongClick TalkRequest!");
                        AirtalkeeSessionManager.getInstance().TalkRequest(session, false);
                    }
                    else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                    {
                        Log.i(VideoSessionActivity.class, "TalkButton onLongClick TalkRelease!");
                        AirtalkeeSessionManager.getInstance().TalkRelease(session);
                    }
                    refreshPttState();
                }
                else
                    Util.Toast(this, getString(R.string.talk_video_tip_no_connection));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME || event.getKeyCode() == KeyEvent.KEYCODE_BACK) && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            VideoPanel.SCREEN_MODE screenMode = mVideoWindow.getScreenMode();
            if (screenMode == VideoPanel.SCREEN_MODE.C || screenMode == VideoPanel.SCREEN_MODE.D)
            {
                mVideoList.activeClean();
                return true;
            }
            else if (mVideoState == VIDEO_STATE_TRANSFERING || mVideoState == VIDEO_STATE_TRANSFER_CONNETING)
            {
                handleStopVideoTransfer();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onSessionOutgoingRinging(AirSession session)
    {

    }

    @Override
    public void onSessionEstablishing(AirSession session)
    {

    }

    @Override
    public void onSessionEstablished(AirSession session, int result)
    {

    }

    @Override
    public void onSessionReleased(AirSession session, int reason)
    {
        finish();
    }

    @Override
    public void onSessionPresence(AirSession session, List<AirContact> membersAll, List<AirContact> membersPresence)
    {

    }

    @Override
    public void onSessionMemberUpdate(AirSession session, List<AirContact> members, boolean isOk)
    {

    }

    @Override
    public void onMediaStateTalkPreparing(AirSession session)
    {
        refreshPttState();
    }

    @Override
    public void onMediaStateTalk(AirSession session)
    {
        changePlayerSound();
        refreshPttState();
    }

    @Override
    public void onMediaStateTalkEnd(AirSession session, int reason)
    {
        changePlayerSound();
        refreshPttState();
    }

    @Override
    public void onMediaStateListen(AirSession session, AirContact speaker)
    {
        changePlayerSound();
        refreshPttState();
    }

    @Override
    public void onMediaStateListenEnd(AirSession session)
    {
        changePlayerSound();
        refreshPttState();
    }

    @Override
    public void onMediaStateListenVoice(AirSession session)
    {
    }

    @Override
    public void onMediaQueue(AirSession session, ArrayList<AirContact> queue)
    {
        refreshPttState();
    }

    @Override
    public void onMediaQueueIn(AirSession session)
    {
        refreshPttState();
    }

    @Override
    public void onMediaQueueOut(AirSession session)
    {
        refreshPttState();
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId)
    {
        switch (group.getId())
        {
            case R.id.rg_video_setting:
            {
                selectQuality(videoSettingRadio.getCheckedRadioButtonId(), true);
                for (int i = 0; i < videoSettingRadio.getChildCount(); i++)
                {
                    if (videoSettingRadio.getChildAt(i) instanceof RadioButton)
                    {
                        RadioButton rbCurrent = (RadioButton) videoSettingRadio.getChildAt(i);
                        if (rbCurrent.getId() == checkedId)
                        {
                            rbCurrent.setChecked(true);
                            rbCurrent.setTextColor(0X7fFF9400);
                            tvSetting.setText(rbCurrent.getText());
                        }
                        else
                        {
                            rbCurrent.setChecked(false);
                            rbCurrent.setTextColor(getResources().getColor(R.color.white));
                        }
                    }
                }
                popSettingView.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        popSettingWindow.dismiss();
                        tvSetting.setBackgroundResource(R.drawable.bg_video_setting_normal);
                        tvSetting.setTextColor(getResources().getColor(R.color.white));
                        settingMode = false;
                    }
                }, 1000);
                break;
            }
        }
    }

    /**
     设置选择视频质量
     @param radioButtonId
     @param flag 选中状态
     */
    public void selectQuality(int radioButtonId, boolean flag)
    {
        RadioButton button = (RadioButton) findViewById(radioButtonId);
        if (button == null)
            return;
        switch (radioButtonId)
        {
            case R.id.rb_video_low:
            case R.id.rb_video_normal:
            case R.id.rb_video_high:
            case R.id.rb_video_best:
                Setting.setVideoQuality(button.getText().toString());
                mVideoWidth = Setting.getVideoResolutionWidth();
                mVideoHeight = Setting.getVideoResolutionHeight();
                mvFramerate = Setting.getVideoFrameRate();
                Setting.setVideoSettingType(0);
                break;
            case R.id.rb_video_custom:
                mVideoWidth = Setting.getVideoCustomResolutionWidth();
                mVideoHeight = Setting.getVideoCustomResolutionHeight();
                mvFramerate = Setting.getVideoCustomFrameRate();
                Setting.setVideoSettingType(1);
                break;
        }
        if (AirtalkeeMediaVideoControl.getInstance().VideoRealtimeGetActvie() == null)
            Util.Toast(this, button.getText().toString());

        mvBitrate = Setting.getVideoCodeRate() * 1000;
        Log.i(VideoSessionActivity.class, "selectQuality mVideoWidth=" + mVideoWidth + " mVideoHeight=" + mVideoHeight);

        int rotation = getScreenRotation();
        mVideoWindow.videoPreviewReset(mVideoWidth, mVideoHeight, rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180);
    }

    @Override
    public void onVideoKey()
    {
    }

    @Override
    public void onVideoPictureTaking(String uid, boolean toFlashLamp)
    {
        if (mTaskCase != null)
        {
            String mark = "";
            mark += "账户：" + AirtalkeeAccount.getInstance().getUserId();
            mark += "\n用户名：" + AirtalkeeAccount.getInstance().getUserName();
            mark += "\n案件号：" + mTaskCase.getCaseCode();
            mark += "\n经度：" + AirLocation.getInstance(this).locationGetLatitude();
            mark += "\n纬度：" + AirLocation.getInstance(this).locationGetLongitude();
            mark += "\n拍摄地点：" + AirLocation.getInstance(this).locationGetPoi();
            mark += "\n拍摄时间：" + Utils.getDate() + " " + Utils.getTime();
            VideoRealRecorder.setWaterMarkText(mark);
            VideoRealRecorder.setCaptureTaskId(mTaskCase.getTaskId());
        }
        Sound.playSound(Sound.PLAYER_TAKE_PHOTO, this);
    }

    @Override
    public void onVideoPictureTaken(boolean isOk, byte[] data)
    {
        Util.Toast(this, getString(R.string.talk_video_tip_take_picture));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onVideoRecorderStart(int sessionId, int result)
    {
        removeDialog(R.id.talk_dialog_waiting);
        ivVideoBtn.setEnabled(true);
        ivVideoBtnLand.setEnabled(true);
        Log.e(VideoSessionActivity.class, "VideoSessionActivity onVideoRecorderStart result = " + result);
        if (result == 0)
        {
            if (videoDeviceStateMachine(VIDEO_ACTION_TRANSFER_STARTED_OK, 0))
            {
                Sound.playSound(Sound.PLAYER_MEDIAN_REC_PLAY_START, this);
                popSettingWindow.dismiss();
                iv_back.setVisibility(View.INVISIBLE);
                if (!allowSettingSwitch)
                    tvSetting.setVisibility(View.INVISIBLE);
                if (ch_time.getVisibility() != View.GONE)
                {
                    ch_time.start();
                    ch_time.setBase(SystemClock.elapsedRealtime());
                }
                tv_status.setText(getString(R.string.talk_video_uploading));
                if (!Config.funcVideoTimerShow)
                    tv_status.setVisibility(View.VISIBLE);
                refreshBottom();
            }
            else
            {
                AirtalkeeMediaVideoControl.getInstance().VideoActionRecorderStop(sessionVideoId);
                mVideoWindow.videoPreviewSetVisiblity(false);
                screenOrientationUnlock();
                Sound.playSound(Sound.PLAYER_MEDIAN_REC_PLAY_STOP, this);
            }
        }
        else
            videoDeviceStateMachine(VIDEO_ACTION_TRANSFER_STARTED_FAIL, 0);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onVideoRecorderStop(int sessionId)
    {
        ivVideoBtn.setEnabled(true);
        ivVideoBtnLand.setEnabled(true);
        Log.e(VideoSessionActivity.class, "VideoSessionActivity onVideoRecorderStop");
        Sound.playSound(Sound.PLAYER_MEDIAN_REC_PLAY_STOP, this);
        videoDeviceStateMachine(VIDEO_ACTION_TRANSFER_STOPPED, 0);
        videoTime = ch_time.getText().toString();
        if (ch_time.getVisibility() != View.GONE)
        {
            ch_time.stop();
            ch_time.setBase(SystemClock.elapsedRealtime());
        }
        iv_back.setVisibility(View.VISIBLE);
        if (!allowSettingSwitch)
            tvSetting.setVisibility(View.VISIBLE);
        if (!Config.funcVideoTimerShow)
            tv_status.setVisibility(View.GONE);
        refreshBottom();
        //AirSession session = AirSessionControl.getInstance().getCurrentChannelSession();
        if (session != null)
        {
            AirtalkeeMessage.getInstance().MessageRemove(session.getSessionCode(), iMessage);
            String msg = getString(R.string.talk_session_video_message_time) + videoTime;
            if (msg != null && !msg.trim().equals(""))
            {
                iMessage = MessageController.messageGenerate(session, "TEMP_VIDEO_SESSION", AirMessage.TYPE_SESSION_VIDEO, AccountController.getUserInfo(), msg);
                // AirtalkeeMessage.getInstance().MessageSessionVideoSend(session, msg, true);
                IMFragment.getInstance().refreshMessages();
            }
        }
        removeDialog(R.id.talk_dialog_waiting);
        if (cameraAuto)
        {
            finish();
        }
    }

    @Override
    public boolean onVideoRealtimeShareStart(AirVideoShare videoShare)
    {
        if (Config.funcVideoPlay)
        {
            mVideoList.viewRefresh();
        }
        return true;
    }

    @Override
    public void onVideoRealtimeShareStop(AirVideoShare videoShare)
    {
        if (Config.funcVideoPlay)
        {
            mVideoList.viewRefresh();
        }
    }

    @Override
    public void onVideoAudioStreamPending(boolean isPending)
    {
        Log.i(VideoSessionActivity.class, "onVideoAudioStreamPending");
        if (Config.funcVideoAudioStream && Config.funcVideoAudioStreamOn)
        {
            mVoiceIsPending = isPending;
            refreshBottom();
        }
    }

    /***********************************
     *
     *  VideoListListener
     *
     ***********************************/

    @Override
    public void onVideoListSelect(AirVideoShare videoShare)
    {
        if (videoShare != null && currentVideoShare != videoShare)
        {
            currentVideoShare = videoShare;
            changePlayerSound();
            mVideoWindow.videoPlayerStart(videoShare);
            session = AirtalkeeSessionManager.getInstance().getSessionByCode(videoShare.getSessionCode(), true);
            if (session != null && session.getType() == AirSession.TYPE_CHANNEL && session.getSessionState() != AirSession.SESSION_STATE_DIALOG)
                AirtalkeeSessionManager.getInstance().SessionCall(session.getSessionCode());
            Log.i(VideoSessionActivity.class, "PLAYER: play (" + videoShare.getOwner() + ")");
        }
    }

    @Override
    public void onVideoListNoSelect()
    {
        currentVideoShare = null;
        session = sessionOrg;
        mVideoWindow.videoPlayerStop();
    }

    /***********************************
     *
     *  VideoPanelListener
     *
     ***********************************/

    @Override
    public void onVideoPanelPreviewSurfaceCreated()
    {
        if (cameraType == VIDEO_TYPE_SYS)
        {
            Log.i(VideoSessionActivity.class, "[VIDEO] onVideoPanelPreviewSurfaceCreated");
            isCameraOpen = AirtalkeeMediaVideoControl.getInstance().VideoActionCameraStart(mInstance, mVideoWindow.getSurfaceHolder(), mVideoWidth, mVideoHeight, getScreenRotation(), mvBitrate, mvFramerate);
            if (cameraAuto && isCameraOpen)
                videoDeviceStateMachine(VIDEO_ACTION_TRANSFER_START, 0);
        }
        mvSurfaceViewRunning = true;
    }

    @Override
    public void onVideoPanelPreviewSurfaceDestroyed()
    {
        Log.i(VideoSessionActivity.class, "[VIDEO] onVideoPanelPreviewSurfaceDestroyed");
        mvSurfaceViewRunning = false;
        if (cameraType == VIDEO_TYPE_SYS)
        {
            AirtalkeeMediaVideoControl.getInstance().VideoActionCameraStop();
        }
    }

    @Override
    public void onVideoPanelPreviewSurfaceChanged()
    {
        Log.i(VideoSessionActivity.class, "[VIDEO] onVideoPanelPreviewSurfaceChanged");
        if (cameraType == VIDEO_TYPE_SYS)
        {
            videoDeviceStateMachine(VIDEO_ACTION_DEVICE_RESET, 0);
        }
    }

    @Override
    public void onVideoPanelScreenChanged(VideoPanel.SCREEN_MODE screenMode)
    {
        changePlayerSound();
        refreshBottom();
    }

    @Override
    public void onMmiTimer(Context context, Object userData) {
        if (mVideoState == VIDEO_STATE_PREVIEW)
            AirtalkeeMediaVideoControl.getInstance().VideoActionCameraOrientation(this, getScreenRotation());
    }
}
