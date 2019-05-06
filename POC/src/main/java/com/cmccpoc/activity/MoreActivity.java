package com.cmccpoc.activity;

import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.OnUserInfoListener;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactGroup;
import com.cmccpoc.R;
import com.cmccpoc.config.Config;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Util;

/**
 * 更多 选项设置Activity
 * @author Yao
 */
public class MoreActivity extends ActivityBase implements OnClickListener, OnUserInfoListener, OnSeekBarChangeListener
{
	public TextView tvUserName;
	public TextView tvUserIpocid;

	private SeekBar mVoiceVolumeSeekBar;
	private CheckBox mVoiceMode;

	private ImageView ivUnread;
	private static MoreActivity mInstance = null;
	/**
	 * 获取MoreActivity实例对象
	 * @return
	 */
	public static MoreActivity getInstance()
	{
		return mInstance;
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool);
		doInitView();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		tvUserName.setText(AirtalkeeAccount.getInstance().getUserName());
	}

	/**
	 * 初始化绑定控件Id
	 */
	private void doInitView()
	{
		mInstance = this;
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		mVoiceVolumeSeekBar = (SeekBar) findViewById(R.id.SoundSettingBarView);
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mVoiceVolumeSeekBar.setMax(max);
		mVoiceVolumeSeekBar.setProgress(Util.getStreamVolume(this));
		mVoiceVolumeSeekBar.setOnSeekBarChangeListener(this);
		
		ImageView icon = (ImageView) findViewById(R.id.talk_tv_user_head);
		if (AirtalkeeAccount.getInstance().getUser().getSecret() > 0)
			icon.setImageResource(R.drawable.head_portrait_secret);

		// findViewById(R.id.talk_setting_voice_mode).setOnClickListener(this);//
		// 收听模式
		mVoiceMode = (CheckBox) findViewById(R.id.talk_setting_voice_mode);
		switch (Util.getMode(AirServices.getInstance()))
		{
			case 0: // 扬声器
				mVoiceMode.setChecked(false);
				break;
			case 3:// 听筒
				mVoiceMode.setChecked(true);
				break;
			default:
				mVoiceMode.setChecked(false); // 置为扬声器
				break;
		}

		mVoiceMode.setOnClickListener(this);

		if (Config.funcTask)
		{
			TextView text = (TextView)findViewById(R.id.talk_setting_case_name);
			text.setText(getString(R.string.talk_tools_setting_task));
			findViewById(R.id.talk_setting_case).setVisibility(View.VISIBLE);
			findViewById(R.id.talk_setting_case_divider).setVisibility(View.VISIBLE);
			findViewById(R.id.talk_setting_case).setOnClickListener(this);
		}
		else
		{
			findViewById(R.id.talk_setting_case).setVisibility(View.GONE);
			findViewById(R.id.talk_setting_case_divider).setVisibility(View.GONE);
		}

		// GPS item
		findViewById(R.id.talk_lv_tool_gps).setOnClickListener(this);
		findViewById(R.id.talk_lv_tool_gps).setVisibility(View.VISIBLE);
		findViewById(R.id.talk_lv_tool_gps_divider).setVisibility(View.VISIBLE);
		
		findViewById(R.id.talk_setting_voice).setOnClickListener(this);
		findViewById(R.id.talk_lv_tool_video).setOnClickListener(this);// 实时视频设置
		findViewById(R.id.talk_lv_tool_channel).setOnClickListener(this);// 频道附着设置
		findViewById(R.id.talk_lv_tool_upload_record).setOnClickListener(this);// 上报记录
		findViewById(R.id.talk_lv_tool_help).setOnClickListener(this);// 使用和帮助
		findViewById(R.id.talk_tv_notice).setOnClickListener(this);// 广播
		findViewById(R.id.talk_lv_tool_about).setOnClickListener(this);// 关于

		findViewById(R.id.talk_change_theme).setVisibility(View.GONE);
		findViewById(R.id.talk_change_theme_divider).setVisibility(View.GONE);

		// Others
		findViewById(R.id.talk_lv_tool_exit).setOnClickListener(this);
		findViewById(R.id.talk_lv_tool_update).setOnClickListener(this);
		findViewById(R.id.talk_tv_user_name_panel).setOnClickListener(this);

		tvUserName = (TextView) findViewById(R.id.talk_tv_user_name);
		tvUserName.setText(AirtalkeeAccount.getInstance().getUserName());
		tvUserIpocid = (TextView) findViewById(R.id.talk_tv_user_ipocid);
		tvUserIpocid.setText(AirtalkeeAccount.getInstance().getUserId());

		ivUnread = (ImageView) findViewById(R.id.iv_Unread);
		//checkBrodcast();
	}
	
	/**
	 * 检测是否有广播，如果有则显示未读标记
	 */
	public void checkBrodcast()
	{
		if (Config.funcBroadcast && AirtalkeeAccount.getInstance().SystemBroadcastNumberGet() > 0)
		{
			ivUnread.setVisibility(View.VISIBLE);
		}
		else
		{
			ivUnread.setVisibility(View.GONE);
		}
	}

	@Override
	public void finish()
	{
		super.finish();
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		AirtalkeeUserInfo.getInstance().setOnUserInfoListener(null);
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		AirtalkeeUserInfo.getInstance().setOnUserInfoListener(this);
		checkBrodcast();
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
			{
				finish();
				break;
			}
			case R.id.talk_lv_tool_gps:
			{
				Intent it = new Intent(this, MenuGpsActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_setting_voice_mode:
			{
				Util.setMode(AirServices.getInstance());
				break;
			}
			case R.id.talk_setting_voice:
			{
				Intent it = new Intent(this, MenuSettingPttActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tool_upload_record:
			{
				Intent it = new Intent(this, MenuReportActivity.class);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tool_help:
			{
				Intent it = new Intent(this, MenuHelpActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_tv_notice:
			{
				Intent it = new Intent(this, MenuNoticeActivity.class);
				it.putExtra("url", AccountController.getDmWebNoticeUrl());
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tool_about:
			{
				Intent it = new Intent(this, MenuAboutActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_tv_user_name_panel:
			{
				Intent it = new Intent(this, MenuAccountActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tool_video:
			{
				Intent it = new Intent(this, MenuSettingSessionVideoActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tool_channel:
			{
				Intent it = new Intent(this, MenuSettingChannelActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_setting_case:
			{
				if (Config.funcTask)
				{
					Intent it = new Intent(this, MenuTaskCaseListActivity.class);
					startActivity(it);
				}
				break;
			}
		}
	}

	@Override
	public void onUserInfoGet(AirContact user)
	{
		// TODO Auto-generated method stub
		if (user != null)
		{
			tvUserName.setText(user.getDisplayName());
		}
	}

	@Override
	public void onUserInfoUpdate(boolean isOk, AirContact user)
	{
		// TODO Auto-generated method stub
		if (isOk)
		{
			tvUserName.setText(user.getDisplayName());
			Util.Toast(this, getString(R.string.talk_user_info_update_name_ok));
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_user_info_update_name_fail));
		}
	}

	@Override
	public void onUserIdGetByPhoneNum(int result, AirContact contact)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserOrganizationTree(boolean isOk, AirContactGroup org)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserOrganizationTreeSearch(boolean isOk, List<AirContact> contacts)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		boolean handled = false;
		switch (event.getKeyCode())
		{
			case KeyEvent.KEYCODE_VOLUME_UP:
				Util.setStreamVolumeUp(this);
				mVoiceVolumeSeekBar.setProgress(Util.getStreamVolume(this));
				handled = true;
				break;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				Util.setStreamVolumeDown(this);
				mVoiceVolumeSeekBar.setProgress(Util.getStreamVolume(this));
				handled = true;
				break;
		// case KeyEvent.KEYCODE_CAMERA:
		// if (event.getAction() == KeyEvent.ACTION_UP)
		// {
		// Util.setMode(this);
		// }
		// handled = true;
		// break;
		}
		return handled ? handled : super.dispatchKeyEvent(event);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		// TODO Auto-generated method stub
		Util.setStreamVolume(this, progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		// TODO Auto-generated method stub
	}

}
