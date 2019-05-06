package com.cmccpoc.widget;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.sdk.video.camera.VideoQuality;
import com.airtalkee.sdk.video.camera.VideoSession;
import com.airtalkee.sdk.video.camera.VideoSessionCallback;
import com.cmccpoc.R;
import com.cmccpoc.activity.MenuReportAsPicActivity;
import com.cmccpoc.activity.MenuReportAsVidActivity;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.config.Config;
import com.cmccpoc.util.Const;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.UriUtil;
import com.cmccpoc.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 上报视频时，拍摄录像的自定义Camera控件
 * 
 * @author Yao
 */
public class VideoCamera extends Activity implements OnClickListener, VideoSessionCallback, OnRequestPermissionsResultCallback
{
	public static final int PERMISSIONS_REQUEST_CAMERA = 1;
	public static String EXTRA_VIDEO_PATH = "extra_video_path";
	private ImageView mButtonStart;
	private ImageView mButtonFlash;
	private ImageView mVideoPlay;
	private ImageView mVideoPic;
	private Chronometer chronometer;
	private ImageView tvClose;
	private SurfaceView mSurfaceView;
	private VideoSession session;
	private VideoView mVideoView;
	private ImageView mButtonToAlbum;
	private ImageView mButtonToPhoto;
	private RelativeLayout rlTopbars;
	private RelativeLayout rlBottombars;
	private ImageView ivSure, ivClose;
	private ProgressBar mProgress;
	private int mProgressTimer = 0;
	
	private Timer mTimer;
	private TimerTask mTimerTask;
	
	private AlertDialog dialog;

	// 视频类型 0：IM， 1：上报
	private int videoType;
	private boolean isCameraSwitch = false;

	private String mTaskId = "";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.video_camera);
		setRequestedOrientation(Config.screenOrientation);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null)
		{
			videoType = bundle.getInt("videoType");
			mTaskId = bundle.getString("taskId");
			if (mTaskId == null)
				mTaskId = "";
		}
		mProgress = (ProgressBar) findViewById(R.id.progress);
		mProgress.setMax(Config.funcVideoSectionTimeMax / 1000);
		session = VideoSession.newInstance(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
		mButtonStart = (ImageView) findViewById(R.id.start);
		mButtonFlash = (ImageView) findViewById(R.id.flash);
		mVideoPlay = (ImageView) findViewById(R.id.iv_im_video_play);
		mVideoPlay.setOnClickListener(this);
		mVideoPic = (ImageView) findViewById(R.id.iv_im_video);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		mVideoView = (VideoView) findViewById(R.id.vv_im_video);
		chronometer = (Chronometer) findViewById(R.id.chronometer1);
		tvClose = (ImageView) findViewById(R.id.close);
		ivClose = (ImageView) findViewById(R.id.bottom_close);
		ivSure = (ImageView) findViewById(R.id.sure);
		mButtonToAlbum = (ImageView) findViewById(R.id.to_album);
		mButtonToPhoto = (ImageView) findViewById(R.id.to_camera);
		rlTopbars = (RelativeLayout) findViewById(R.id.topbars);
		rlTopbars.getBackground().setAlpha(80);
		rlBottombars = (RelativeLayout) findViewById(R.id.bottombars);
		rlBottombars.getBackground().setAlpha(80);

		mButtonToAlbum.setOnClickListener(this);
		mButtonToPhoto.setOnClickListener(this);
		mButtonStart.setOnClickListener(this);
		mButtonFlash.setOnClickListener(this);
		tvClose.setOnClickListener(this);
		ivClose.setOnClickListener(this);
		ivSure.setOnClickListener(this);
		if (Config.funcFlashMode)
			mButtonFlash.setVisibility(View.VISIBLE);
		else
			mButtonFlash.setVisibility(View.INVISIBLE);
			
		/*
		int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
		// 检查是否已经授权该权限
		if (permission == PackageManager.PERMISSION_GRANTED)
		{
			session.setCallback(this);
			session.setSurfaceView(mSurfaceView);
			session.setPreviewOrientation(getPreviewDegree(this));
			session.startPreview();
		}
		else
		{
		}*/
		try
		{
			session.setCallback(this);
			session.setSurfaceView(mSurfaceView);
			session.setPreviewOrientation(getPreviewDegree(this));
			session.startPreview();
		}                              
		catch (Exception e)
		{
			if(Toast.isDebug) Toast.makeText1(this, getString(R.string.talk_permission_deny), Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		switch (requestCode)
		{
			case PERMISSIONS_REQUEST_CAMERA:
			{
				// 如果请求被取消，那么 result 数组将为空
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					Log.i(VideoCamera.class, "[VIDEO] onRequestPermissionsResult in");
					// 已经获取对应权限
					session.setCallback(this);
					session.setSurfaceView(mSurfaceView);
					session.setPreviewOrientation(getPreviewDegree(this));
					session.startPreview();
				}
				else
				{
					// 未获取到授权，取消需要该权限的方法
					Log.i(VideoCamera.class, "[VIDEO] onRequestPermissionsResult out");
					if(Toast.isDebug) Toast.makeText1(this, getString(R.string.talk_permission_deny), Toast.LENGTH_LONG).show();
				}
				break;
			}
		}
	}
	
	@Override
	public void finish()
	{
		super.finish();
		isCameraSwitch = false;
	}
	
	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.flash:
				toggleFlash();
				break;
			case R.id.start:
				int cameraId = session.getCameraId();
				if (cameraId >= 0)
					toggleStream();
				else
				{
					if(Toast.isDebug) Toast.makeText1(this, getString(R.string.talk_permission_deny), Toast.LENGTH_LONG);
				}
				break;
			case R.id.camera:
			{
				session.switchCamera(isCameraSwitch ? CameraInfo.CAMERA_FACING_BACK : CameraInfo.CAMERA_FACING_FRONT);
				isCameraSwitch = true;
				break;
			}
			case R.id.close:
			{
				setResult(RESULT_CANCELED);
				finish();
				break;
			}
			case R.id.bottom_close:
			{
				dialog = new AlertDialog(this, "确定放弃该视频？", "", new DialogListener()
				{
					@Override
					public void onClickOk(int id, boolean isChecked)
					{
						
					}
					@Override
					public void onClickOk(int id, Object obj)
					{
						setResult(RESULT_CANCELED);
						finish();
					}
					@Override
					public void onClickCancel(int id)
					{
					}
				}, 1);
				dialog.show();
				break;
			}
			case R.id.sure:
			{
				if (videoType == 1)
				{
					String path = session.getVideoFilePath();
					Intent data = new Intent(this, MenuReportAsVidActivity.class);
					data.putExtra(EXTRA_VIDEO_PATH, path);
					setResult(RESULT_OK, data);
					startActivity(data);
				}
				else if (videoType == 0)
				{
					String path = session.getVideoFilePath();
					Intent data = new Intent();
					data.putExtra(EXTRA_VIDEO_PATH, path);
					setResult(RESULT_OK, data);
				}
				finish();
				break;
			}
			case R.id.to_album:
			{
				if (session.getCameraId() >= 0)
				{
					if (videoType == 1)
					{
						session.release();
						Intent itImage = new Intent(this, MenuReportAsVidActivity.class);
						itImage.putExtra("type", "video");
						startActivity(itImage);
						finish();
					}
					else if (videoType == 0)
					{
						session.stopPreview();
						String status = Environment.getExternalStorageState();
						if (!status.equals(Environment.MEDIA_MOUNTED))
						{
							Util.Toast(this, getString(R.string.talk_insert_sd_card));
							return;
						}
						Intent localIntent = new Intent("android.intent.action.GET_CONTENT", null);
						localIntent.setType("video/*");
						startActivityForResult(localIntent, Const.image_select.REQUEST_CODE_BROWSE_VIDEO);
					}
				}
				break;
			}
			case R.id.to_camera:
			{
				try
				{
					if (session.getCameraId() >= 0)
					{
						if (videoType == 1)
						{
							session.release();
							Intent it = new Intent(this, MenuReportAsPicActivity.class);
							it.putExtra("type", "camera");
							startActivity(it);
							finish();
						}
						else if (videoType == 0)
						{
							session.release();
							Intent it = new Intent();
							it.putExtra("type", "imCamera");
							setResult(Activity.RESULT_CANCELED, it);
							finish();
						}
					}
				}
				catch (Exception e)
				{ }
				break;
			}
			case R.id.iv_im_video_play:
			{
				mVideoPlay.setVisibility(View.GONE);
				mVideoView.setVisibility(View.VISIBLE);
				mVideoPic.setVisibility(View.GONE);
				mVideoView.start();
				break;
			}
		}
	}

	/**
	 * 刷新拍照按钮状态
	 */
	public void refreshStartButton()
	{
		switch (session.getState())
		{
			case VideoSession.STATE_STARTED:
				mButtonStart.setImageResource(R.drawable.btn_report_video_stop);
				chronometer.start();
				chronometer.setBase(SystemClock.elapsedRealtime());
				chronometer.setTextColor(Color.RED);
				mButtonFlash.setVisibility(View.GONE);
				if (videoType == 0)
				{
					mProgress.setVisibility(View.VISIBLE);
				}
				else
				{
					mProgress.setVisibility(View.GONE);
				}
				ivSure.setVisibility(View.GONE);
				mButtonToAlbum.setVisibility(View.GONE);
				mButtonToPhoto.setVisibility(View.GONE);
				chronometer.setVisibility(View.VISIBLE);
				break;
			case VideoSession.STATE_STOPPED:
				mButtonStart.setImageResource(R.drawable.btn_report_video_stop);
				mButtonStart.setVisibility(View.INVISIBLE);
				chronometer.setVisibility(View.GONE);
				chronometer.setTextColor(Color.WHITE);
				mButtonFlash.setVisibility(View.GONE);
				mProgress.setVisibility(View.GONE);
				if (videoType == 0)
				{
					mVideoPlay.setVisibility(View.VISIBLE);
					mSurfaceView.setVisibility(View.GONE);
					mVideoView.setVisibility(View.GONE);
					mVideoView.setVideoPath(session.getVideoFilePath());
					mVideoPic.setVisibility(View.VISIBLE);
					mVideoPic.setImageBitmap(Util.getVideoImage(this, session.getVideoFilePath()));
				}
				else
				{
					mVideoPlay.setVisibility(View.GONE);
				}
				ivSure.setVisibility(View.VISIBLE);
				ivClose.setVisibility(View.VISIBLE);
				mButtonToAlbum.setVisibility(View.GONE);
				mButtonToPhoto.setVisibility(View.GONE);
				rlTopbars.setVisibility(View.GONE);
				chronometer.stop();
				chronometer.setBase(SystemClock.elapsedRealtime());
				break;
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		session.release();
		session = null;
		// mSurfaceView.getHolder().removeCallback(this);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (session != null && session.getState() == VideoSession.STATE_STARTED)
		{
			session.stopRecord();
			if (videoType == 0)
				timerStop();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case Const.image_select.REQUEST_CODE_BROWSE_VIDEO:
			{
				if (resultCode == Activity.RESULT_OK)
				{
					if (data != null)
					{
						Intent it = new Intent();
						it.putExtra(VideoCamera.EXTRA_VIDEO_PATH, UriUtil.getPath(this, data.getData()));
						setResult(resultCode, it);
					}
					finish();
				}
				else
				{
					if (session != null)
					{
						session.setCallback(this);
						session.setSurfaceView(mSurfaceView);
						session.startPreview();
					}
				}
				break;
			}
		}
	}

	long currentMillis = 0;

	/**
	 * 开始录制时，传输流数据
	 */
	public void toggleStream()
	{
		if (Utils.getCurrentTimeInMillis() - currentMillis > 2000)
		{
			if (session.getState() == VideoSession.STATE_STOPPED)
			{
				if (videoType == 0)
				{
					mProgressTimer = 0;
					timerStart();
				}
				session.startRecord();
			}
			else
			{
				session.stopRecord();
				if (videoType == 0)
					timerStop();
			}
			currentMillis = Utils.getCurrentTimeInMillis();
		}
	}
	
	private void timerStart()
	{
		mTimer = new Timer();
		mTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				if (mProgressTimer * 1000 > Config.funcVideoSectionTimeMax)
				{
					Message msg = new Message();
					msg.obj = mProgressTimer;
					timerHandler.sendMessage(msg);
				}
				else 
				{
					mProgress.setProgress(mProgressTimer);
					mProgressTimer++;
				}
			}
		};
		mTimer.schedule(mTimerTask, 0, 1000);
	}
	
	private void timerStop()
	{
		try
		{
			mTimer.cancel();
		}
		catch (Exception e)
		{ }
	}
	
	public Handler timerHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			if (session != null)
				session.stopRecord();
			timerStop();
		}
	};

	/**
	 * 刷新录制状态
	 */
	private void refreshFlashState()
	{
		if (session.getFlashState())
			mButtonFlash.setImageResource(R.drawable.ic_flash_on_holo_light);
		else
			mButtonFlash.setImageResource(R.drawable.ic_flash_off_holo_light);
	}

	public void toggleFlash()
	{
		session.toggleFlash();
	}

	@Override
	public void onSessionError(int reason, int streamType, Exception e)
	{
		Log.e(VideoCamera.class, "[VIDEO] Session Error reason = " + reason + ", streamType = " + streamType + ", error = " + e.toString());
	}

	@Override
	public void onPreviewStarted(boolean isOk)
	{
		if (!isOk)
		{
			finish();
		}
	}

	@Override
	public void onCameraSwitched(int cameraId)
	{
		
	}

	@Override
	public void onSessionStarted()
	{
		refreshStartButton();
	}

	@Override
	public void onSessionStopped()
	{
		refreshStartButton();
	}

	@Override
	public void onFlashToggle()
	{
		refreshFlashState();
	}

	/**
	 * 获取预览界面的角度
	 * 
	 * @param activity 界面
	 * @return 角度
	 */
	public static int getPreviewDegree(Activity activity)
	{
		// 获得手机的方向
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degree = 0;
		// 根据手机的方向计算相机预览画面应该选择的角度
		switch (rotation)
		{
			case Surface.ROTATION_0:
				degree = 90;
				break;
			case Surface.ROTATION_90:
				degree = 0;
				break;
			case Surface.ROTATION_180:
				degree = 270;
				break;
			case Surface.ROTATION_270:
				degree = 180;
				break;
		}
		Log.i(PhotoCamera.class, "PhotoCamera PreviewDegree degree = " + degree);
		return degree;
	}
	
	private String[] getCameraParam()
	{
		/*
		6233   前摄：CAM[1]:sp2509mipiraw
	       	   后摄：CAM[2]:sp0A09mipiraw
	    6233CD 前摄：CAM[1]:sp0A09mipiraw
	           后摄：CAM[2]:sp0A09mipiraw
	    CAM[1]:sp0a09mipirawback; CAM[2]:sp0a09mipiraw; 30w
	    CAM[1]:sp2509mipiraw; CAM[2]:sp0a09mipiraw; 200w
		*/
		String ret = null;
		final String camera_info = "/proc/driver/camera_info";
		File file = new File(camera_info);
		if (!file.exists() || file.isDirectory())
			return null;
		try
		{
			FileReader fReader = new FileReader(file);
			@SuppressWarnings("resource")
			BufferedReader bReader = new BufferedReader(fReader);
			ret = bReader.readLine(); 
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return ret.split(";");
	}
}
