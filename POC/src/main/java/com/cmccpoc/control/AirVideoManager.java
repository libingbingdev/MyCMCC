package com.cmccpoc.control;

import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.WindowManager;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMediaVideoControl;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.OnAccountListener;
import com.airtalkee.sdk.OnAccountSettingListener;
import com.airtalkee.sdk.OnChannelListener;
import com.airtalkee.sdk.OnMediaVideoListener;
import com.airtalkee.sdk.engine.StructVideoData;
import com.airtalkee.sdk.engine.StructVideoParam;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.entity.AirVideoShare;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.activity.MenuTaskCaseDetailActivity;
import com.cmccpoc.activity.VideoSessionActivity;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.activity.home.PTTFragment;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.config.Config;
import com.cmccpoc.listener.OnMmiAccountListener;
import com.cmccpoc.listener.OnMmiChannelListener;
import com.cmccpoc.listener.OnMmiVideoListener;
import com.cmccpoc.location.AirLocation;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.Sound;

import java.util.LinkedHashMap;
import java.util.List;

//import com.airtalkee.sdk.AirtalkeeVideo;
//import com.airtalkee.sdk.OnVideoListener;

/**
 * 用户管理类
 * @author Yao
 */
public class AirVideoManager implements OnMediaVideoListener, DialogListener
{

	private static AirVideoManager mInstance;
	private OnMmiVideoListener mVideoListener = null;
	private AlertDialog dialog = null;
	private String dialogOwner = null;

	public static AirVideoManager getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new AirVideoManager();
			AirtalkeeMediaVideoControl.getInstance().setOnVideoListener(mInstance);
		}
		return mInstance;
	}

	public void setVideoListener(OnMmiVideoListener listener)
	{
		this.mVideoListener = listener;
	}



	/**
	 * 开始录制视频
	 * @param sessionId 会话Id
	 * @param result 结果状态
	 */
	@Override
	public void onVideoRecorderStart(int sessionId, int result)
	{
		if (mVideoListener != null)
			mVideoListener.onVideoRecorderStart(sessionId, result);
	}

	/**
	 * 结束录制视频
	 * @param sessionId 会话Id
	 */
	@Override
	public void onVideoRecorderStop(int sessionId)
	{
		if (mVideoListener != null)
			mVideoListener.onVideoRecorderStop(sessionId);
	}

	@Override
	public void onVideoRecorderStartEx(StructVideoParam param)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onVideoRecorderStopEx()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onVideoRecorderDataEx(StructVideoData data)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onVideoRealtimeShareStart(AirVideoShare videoShare)
	{
		if (Config.funcVideoPlay)
		{
			boolean isHandled = false;
			if (mVideoListener != null)
				isHandled = mVideoListener.onVideoRealtimeShareStart(videoShare);

			if (!isHandled)
			{
				if (AirServices.getInstance() != null)
				{
					if (dialog != null && dialog.isShowing())
						dialog.dismiss();
					dialog = new AlertDialog(AirServices.getInstance(),
							AirServices.getInstance().getString(R.string.talk_video_forward_real),
							videoShare.getOwnerName() + AirServices.getInstance().getString(R.string.talk_video_forward_content),
							AirServices.getInstance().getString(R.string.talk_dialog_no),
							AirServices.getInstance().getString(R.string.talk_dialog_yes),
							this, 0, videoShare);
					dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
					dialog.show();
					dialogOwner = videoShare.getOwner();
					Sound.playSound(Sound.PLAYER_NEWINFO, false, AirServices.getInstance());
				}
			}
		}
	}

	@Override
	public void onVideoRealtimeShareStop(AirVideoShare videoShare)
	{
		if (Config.funcVideoPlay)
		{
			if (mVideoListener != null)
				mVideoListener.onVideoRealtimeShareStop(videoShare);

			if (TextUtils.equals(videoShare.getOwner(), dialogOwner))
			{
				if (dialog != null && dialog.isShowing())
					dialog.dismiss();
				dialog = null;
				dialogOwner = null;
			}
		}
	}


	@Override
	public void onClickOk(int id, Object obj) {
		AirVideoShare videoShare = (AirVideoShare)obj;
		Intent i = new Intent();
		i.setClass(AirServices.getInstance(), VideoSessionActivity.class);
		i.putExtra("sessionCode", videoShare.getSessionCode());
        i.putExtra("owner", videoShare.getOwner());
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		AirServices.getInstance().startActivity(i);
	}

	@Override
	public void onClickOk(int id, boolean isChecked) {

	}

	@Override
	public void onClickCancel(int id) {

	}
}
