package com.cmccpoc.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;
import com.airtalkee.sdk.AirtalkeeReport;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.widget.ReportProgressAlertDialog;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirReportManager;
import com.cmccpoc.control.AirTaskCaseManager;
import com.cmccpoc.entity.AirTaskCase;
import com.cmccpoc.listener.OnMmiLocationListener;
import com.cmccpoc.listener.OnMmiReportListener;
import com.cmccpoc.location.AirLocation;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.Const;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.UriUtil;
import com.cmccpoc.util.Util;
import com.cmccpoc.widget.VideoCamera;

/**
 * 上报视频
 * 主要包括：上传视频资源，可选压缩或高清的
 * @author Yao
 */
public class MenuReportAsVidActivity extends ActivityBase implements OnClickListener, OnMmiLocationListener, OnMmiReportListener
{
	private final int VIDEO_MAX_SIZE = 100 * 1024 * 1024;

	private EditText report_detail;
	private ImageView ivVideoPic;
	private VideoView mVideoView;
	private MediaController mVideoController;
	private Button btn_post;
	private boolean isUploading = false;
	private int videoSize = 0;
	private Uri videoUri = null;
	private String videoPath = "";
	private PopupWindow pwImage = null;

	private AirTaskCase mTaskCase = null;

	private String type = null;
	private RadioButton rbBig, rbSmall;

	private android.widget.Toast myToast;
	ReportProgressAlertDialog reportDialog;
	public static boolean finishFlag;
	private static MenuReportAsVidActivity mInstance;

	/**
	 * 获取MenuReportAsVidActivity对象实例
	 * @return
	 */
	public static MenuReportAsVidActivity getInstance()
	{
		return mInstance;
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		finishFlag = false;
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_report_as_vid);
		bundle = getIntent().getExtras();
		if (bundle != null)
		{
			String taskId = bundle.getString("taskId");
			if (!TextUtils.isEmpty(taskId))
				mTaskCase = AirTaskCaseManager.getInstance().getTask(taskId);
			videoPath = bundle.getString("extra_video_path");
			type = bundle.getString("type");
		}
		doInitView();
		refreshUI();
		loadAlbum(type);
		mInstance = this;
		AirReportManager.getInstance().setReportListener(this);
	}

	/**
	 * 加载视频相册
	 * @param type 是否为视频类型
	 */
	private void loadAlbum(String type)
	{
		if (type != null && type.equals("video"))
		{
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

	/**
	 * 初始化绑定控件Id
	 */
	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_report_vid);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);
		findViewById(R.id.report_item_panel).setOnClickListener(this);

		ivVideoPic = (ImageView) findViewById(R.id.report_image);
		ivVideoPic.setOnClickListener(this);
		mVideoView = (VideoView) findViewById(R.id.report_video);

		// mVideoView.setOnClickListener(this);
		report_detail = (EditText) findViewById(R.id.report_detail);
		btn_post = (Button) findViewById(R.id.report_btn_post);
		btn_post.setOnClickListener(this);

		mVideoController = new MediaController(this);
		mVideoView.setMediaController(mVideoController);

		rbBig = (RadioButton) findViewById(R.id.report_file_big);
		rbSmall = (RadioButton) findViewById(R.id.report_file_small);

	}

	/**
	 * 刷新UI状态
	 */
	private void refreshUI()
	{
		if (isUploading)
		{
			report_detail.setEnabled(false);
		}
		else
		{
			report_detail.setEnabled(true);
			if (!TextUtils.isEmpty(videoPath))
			{
				mVideoView.setVisibility(View.VISIBLE);
				mVideoView.setVideoPath(videoPath);
				videoSize = AirServices.iOperator.getFileSize("", videoPath, true);
				rbBig.setText("高清 " + MenuReportActivity.sizeMKB(videoSize));
				float smallSize = videoSize * 0.8F;
				rbSmall.setText("压缩 " + MenuReportActivity.sizeMKB((int) smallSize));
				mVideoView.start();
			}
			else
			{
				mVideoView.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
		AirReportManager.getInstance().setReportListener(null);
		if (reportDialog != null)
		{
			reportDialog.cancel();
		}
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
			case R.id.report_btn_post:
			{
				finishFlag = true;
				reportPost();
				break;
			}
			case R.id.report_image:
			case R.id.report_video:
			{
				break;
			}
			case R.id.image_video:
			{
				if (pwImage != null)
					pwImage.dismiss();
				break;
			}
			case R.id.report_item_panel:
			{
				Util.hideSoftInput(this);
				break;
			}
		}
	}

	/**
	 * 开始上报
	 */
	public void reportPost()
	{
		if (isUploading)
		{
			Util.Toast(this, getString(R.string.talk_report_uploading));
			return;
		}
		if (TextUtils.isEmpty(videoPath))
		{
			Util.Toast(this, getString(R.string.talk_report_upload_vid_err_select_pic));
			return;
		}
		isUploading = true;
		Util.hideSoftInput(this);
		refreshUI();
		myToast = Toast.makeText1(this, true, getString(R.string.talk_report_upload_getting_gps), Toast.LENGTH_LONG);
		myToast.show();
		Log.i(MenuReportAsVidActivity.class, "[REPORT-VID] reportPost start");
		AirLocation.getInstance(this).onceGet(this, 30);
	}

	Uri gUri;

	/**
	 * 获取输出的媒体文件Uri
	 * @return
	 */
	public Uri getOutputMediaFile()
	{
		// To be safe, you should check that the SDCard is mounted

		if (Environment.getExternalStorageState() != null)
		{
			// this works for Android 2.2 and above
			File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "SMW_VIDEO");

			// This location works best if you want the created images to be
			// shared
			// between applications and persist after your app has been
			// uninstalled.
			// Create the storage directory if it does not exist
			if (!mediaStorageDir.exists())
			{
				if (!mediaStorageDir.mkdirs())
				{
					Log.d(MenuReportAsVidActivity.class, "failed to create directory");
					return null;
				}
			}
			// Create a media file name
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			File mediaFile;
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
			return Uri.fromFile(mediaFile);
		}

		return null;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
		{
			finish();
			return;
		}
		Uri uri = null;
		switch (requestCode)
		{
			case Const.image_select.REQUEST_CODE_CREATE_VIDEO:
			{
				String filePath = data.getExtras().getString(VideoCamera.EXTRA_VIDEO_PATH);
				if (filePath != null)
				{
					int size = AirServices.iOperator.getFileSize("", filePath, true);
					if (size <= VIDEO_MAX_SIZE)
					{
						videoUri = uri;
						videoPath = filePath;
						videoSize = size;
						// mVideoView.setVideoURI(uri);
						mVideoView.setVideoPath(videoPath);
						mVideoView.start();
						refreshUI();
					}
					else
					{
						Util.Toast(this, getString(R.string.talk_report_upload_vid_err_size));
					}
				}

				break;
			}
			case Const.image_select.REQUEST_CODE_BROWSE_VIDEO:
			{
				String filePath = UriUtil.getPath(this, data.getData());
				if (filePath != null)
				{
					int size = AirServices.iOperator.getFileSize("", filePath, true);
					if (size <= VIDEO_MAX_SIZE)
					{
						videoUri = data.getData();
						videoPath = filePath;
						videoSize = size;
						mVideoView.setVideoPath(filePath);
						mVideoView.start();
						refreshUI();
					}
					else
					{
						Util.Toast(this, getString(R.string.talk_report_upload_vid_err_size));
					}
				}
				else
				{
					Util.Toast(this, getString(R.string.talk_report_upload_vid_err_select_pic));
				}
			}
			default:
				break;
		}
	}

	private void reportImage(int locType, double latitude, double longitude, double altitude, float speed, String time, String address)
	{
		if (isUploading)
		{
			if (myToast != null)
				myToast.cancel();
			String detail = report_detail.getText().toString();
			int size = rbBig.isChecked() ? videoSize : (int) (videoSize * 0.8f);
			if (mInstance != null)
			{
				reportDialog = new ReportProgressAlertDialog(this, MenuReportActivity.sizeMKB(size));
				try
				{
					reportDialog.show();
				}
				catch (Exception e)
				{}
			}
			File file = new File(videoPath);
			try
			{
				if (Config.marketCode != Config.MARKET_CODE_CMCC)
				{
					Date date = new Date(file.lastModified());
					if (date != null)
					{
						if (!TextUtils.isEmpty(detail))
							detail += "\r\n\r\n";

						SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String date_string = sfd.format(date);
						detail += getString(R.string.talk_report_upload_capture_time) + " " + date_string;
					}
				}
			}
			catch (Exception e)
			{
				detail = report_detail.getText().toString();
			}

			String taskId = "";
			String taskCode = "";
			String taskName = "";
			String taskCar = "";
			if (mTaskCase != null)
			{
				taskId = mTaskCase.getTaskId();
				taskCode = mTaskCase.getCaseCode();
				taskName = mTaskCase.getCaseName();
				taskCar = mTaskCase.getCarNo();
			}

			String resTypeExtension = AirServices.iOperator.getFileExtension(videoPath);
			if (Utils.isEmpty(resTypeExtension))
			{
				resTypeExtension = "3gp";
			}
			Log.i(MenuReportAsVidActivity.class, "VideoPicture: TASK[" + taskId + "][" + taskName + "] text=[" + report_detail.getText().toString() + "] x=[" + latitude + "] y=[" + longitude + "]");
			AirReportManager.getInstance().Report(taskId, taskName, AirtalkeeReport.RESOURCE_TYPE_VIDEO, resTypeExtension, videoUri, videoPath, detail, videoSize, locType, latitude, longitude);
			isUploading = false;
		}
	}

	@Override
	public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time)
	{

	}

	@Override
	public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time, String address)
	{
		Log.i(MenuReportAsVidActivity.class, "[REPORT-VID] onLocationChanged start");
		if (id == AirLocation.AIR_LOCATION_ID_ONCE)
			reportImage(type, latitude, longitude, 0, 0, "", "");
	}

	@Override
	public void onMmiReportResourceListRefresh()
	{
		
	}

	@Override
	public void onMmiReportDel()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onMmiReportProgress(int progress)
	{
		reportDialog.setFileProgress(progress);
	}
}
