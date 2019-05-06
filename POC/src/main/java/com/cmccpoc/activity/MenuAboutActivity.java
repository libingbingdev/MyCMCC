package com.cmccpoc.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeVersionUpdate;
import com.airtalkee.sdk.OnVersionUpdateListener;
import com.airtalkee.sdk.entity.AirStatisticsNetworkByte;
import com.airtalkee.sdk.util.IOoperate;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.widget.DialogVersionUpdate;
import com.cmccpoc.config.Config;
import com.cmccpoc.util.AirMmiTimer;
import com.cmccpoc.util.AirMmiTimerListener;
import com.cmccpoc.util.Language;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Util;

/**
 * 更多：关于版本Activity
 * 主要功能包括：检查更新并在线升级、查看运行时长与流量消耗
 * @author Yao
 */
public class MenuAboutActivity extends ActivityBase implements OnClickListener, /*AirMmiTimerListener,*/ OnVersionUpdateListener
{
	private LinearLayout statLayout, checkVersionLayout;
	private TextView statLayoutTime, statLayoutBytes, tvVersion,softwareNumber, versionMsg, versionCode,mSystemVersion;
	private ImageView ivUpdateIcon;
	private PopupMenu mPop;
	private int gStatRecv = 0;
	private int gStatSent = 0;
	private long gStatTime = 0;
	private IOoperate iOperate = null;
	private boolean isDownloading = false;
	private boolean isShow = false;
	private Button mOk,mBack;
	/**
	 * 是否正在下载
	 * @return
	 */
	public boolean isDownloading()
	{
		return isDownloading;
	}

	/**
	 * 设置是否正在下载中
	 * @param isDownloading
	 */
	public void setDownloading(boolean isDownloading)
	{
		this.isDownloading = isDownloading;
	}

	private final String STAT_RECV = "STAT_RECV";
	private final String STAT_SENT = "STAT_SENT";
	private final String STAT_TIME = "STAT_TIME";
	
	private static MenuAboutActivity mInstance;
	/**
	 * 获取关于版本Activity实例对象
	 * @return
	 */
	public static MenuAboutActivity getInstance()
	{
		return mInstance;
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		mInstance = this;
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_about);
		iOperate = new IOoperate();
		gStatRecv = 0;
		gStatSent = 0;
		gStatTime = 0;
		doInitView();
		mPop=new PopupMenu(this,checkVersionLayout);
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		/*if (statLayout.getVisibility() == View.VISIBLE)
		{
			AirMmiTimer.getInstance().TimerRegister(this, this, false, false, 1000, true, null);
		}*/
		isShow = false;
		checkVersion();
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		/*if (statLayout.getVisibility() == View.VISIBLE)
		{
			AirMmiTimer.getInstance().TimerUnregister(this, this);
		}*/
	}

	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
	}

	/**
	 * 初始化绑定控件Id
	 */
	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_about);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		tvVersion = (TextView) findViewById(R.id.talk_tv_version);
		tvVersion.setText(getString(R.string.talk_version) + Config.VERSION_NAME);

		softwareNumber = (TextView)findViewById(R.id.software_number_info);
		softwareNumber.setText(SystemProperties.get("ro.build.display.id"));
		mOk= (Button) findViewById(R.id.ok);
		mBack= (Button) findViewById(R.id.back);
		mOk.setText(R.string.update);

		versionMsg = (TextView) findViewById(R.id.talk_tv_update_msg);

		versionCode = (TextView) findViewById(R.id.talk_tv_version_code);
		ivUpdateIcon = (ImageView) findViewById(R.id.talk_iv_update_icon);
		checkVersionLayout = (LinearLayout) findViewById(R.id.talk_check_version);
		checkVersionLayout.setOnClickListener(this);

		//statLayout = (LinearLayout) findViewById(R.id.talk_tv_statistic);
		//statLayoutTime = (TextView) findViewById(R.id.talk_tv_statistic_time);
		//statLayoutBytes = (TextView) findViewById(R.id.talk_tv_statistic_bytes);
		//findViewById(R.id.talk_iv_refresh).setOnClickListener(this);
		
		//((ImageView) findViewById(R.id.icon_cmcc)).setImageResource(Config.app_icon);
		mSystemVersion=(TextView) findViewById(R.id.system_tv_info);
		mSystemVersion.setText("Android "+android.os.Build.VERSION.RELEASE);
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			/*case R.id.menu_left_button:
			case R.id.bottom_left_icon:
			{
				finish();
				break;
			}*/
			case R.id.talk_check_version:
			{
				if(!isDownloading)
				{
					isShow = true;
					checkVersion();
				}
				break;
			}
			/*case R.id.talk_iv_refresh:
			{
				AirtalkeeAccount.getInstance().statisticsNetworkByteClean();
				gStatRecv = 0;
				gStatSent = 0;
				gStatTime = 0;
				try
				{
					iOperate.putInt(STAT_RECV, 0);
					iOperate.putInt(STAT_SENT, 0);
					iOperate.putLong(STAT_TIME, 0);
				}
				catch (Exception e)
				{
					// TODO: handle exception
				}
				statLayoutTime.setText(getString(R.string.talk_statistic_time) + "00:00:00");
				statLayoutBytes.setText(getString(R.string.talk_statistic_bytes) + "0.0K");
				Util.Toast(this, getString(R.string.talk_statistic_tip));
				break;
			}*/
			default:
				break;
		}
	}

	/**
	 * 计时器：
	 * 统计app当前运行时长与消耗流量
	 */
/*	@Override
	public void onMmiTimer(Context context, Object userData)
	{
		AirStatisticsNetworkByte net = AirtalkeeAccount.getInstance().statisticsNetworkByte();
		int statRecvBytes = net.getRecvBytes() + gStatRecv;
		int statSentBytes = net.getSentBytes() + gStatSent;
		long statTime = net.getTimeTotal() + gStatTime;

		String timeString = String.format("%02d:%02d:%02d", statTime / 1000 / 60 / 60, statTime / 1000 / 60 % 60, statTime / 1000 % 60);
		statLayoutTime.setText(getString(R.string.talk_statistic_time) + timeString);

		String total = "";
		int bytesTotal = (statRecvBytes + statSentBytes) / 1024;
		if (bytesTotal > 1024) // M
		{
			total = "" + (bytesTotal / 1024) + "." + ((bytesTotal % 1024) / 100) + "M";
		}
		else
		// K
		{
			total = "" + bytesTotal + "." + (((statRecvBytes + statSentBytes) % 1024) / 100) + "K";
		}

		int bytesInterval = net.getRecvBytesInterval() + net.getSentBytesInterval();
		String bytesString = getString(R.string.talk_statistic_bytes) + total;
		bytesString += " (";
		if (net.getTimeInterval() / 1000 > 1)
		{
			bytesString += bytesInterval / (net.getTimeInterval() / 1000);
		}
		else
		{
			bytesString += bytesInterval;
		}
		bytesString += "B/S)";
		statLayoutBytes.setText(bytesString);

		try
		{
			iOperate.putInt(STAT_RECV, statRecvBytes);
			iOperate.putInt(STAT_SENT, statSentBytes);
			iOperate.putLong(STAT_TIME, statTime);
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}*/

	/**
	 * 检查版本
	 * 如果有新版本，则弹出提示窗口通知要更新
	 */
	private void checkVersion()
	{
		String lang = Language.getLocalLanguage(MenuAboutActivity.this);
		String userId = AirtalkeeAccount.getInstance().getUserId();
		String versionCode = Util.appVersion(MenuAboutActivity.this);
		String imei = Util.getImei(this);
		if (TextUtils.isEmpty(imei))
			Toast.makeText(this, "IMEI为空，无法升级", Toast.LENGTH_LONG).show();
		else
			AirtalkeeVersionUpdate.getInstance().versionCheck(this, userId, Config.marketCode, lang, Config.VERSION_PLATFORM, Config.VERSION_TYPE, Config.model, imei, versionCode, 0);
	}

	@Override
	public void UserVersionUpdate(int versionFlag, String versionInfo, final String url)
	{
		// versionFlag = 1;
		if (versionFlag == 0)
		{
			// versionMsg.setVisibility(View.VISIBLE);
			versionMsg.setText(R.string.talk_verion_latest);
			versionMsg.setTextColor(getResources().getColor(R.color.update_text_none));
			versionCode.setVisibility(View.GONE);
			ivUpdateIcon.setVisibility(View.GONE);
		}
		else
		{
			ivUpdateIcon.setVisibility(View.VISIBLE);
			versionCode.setText(versionInfo);
			versionCode.setVisibility(View.VISIBLE);
			versionMsg.setText(R.string.talk_version_new);
			versionMsg.setTextColor(getResources().getColor(R.color.update_text_new));
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.talk_verion_title);
			builder.setMessage(versionInfo);
			builder.setPositiveButton(getString(R.string.talk_verion_upeate), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					try
					{
						dialog.cancel();
						DialogVersionUpdate update = new DialogVersionUpdate(MenuAboutActivity.this, url);
						update.show();
						versionMsg.setText("更新中...");
						isDownloading = true;
					}
					catch (Exception e)
					{
						// TODO: handle exception
					}
				}
			});
			if (versionFlag == 2)
			{
				builder.setCancelable(false);
			}
			else
			{
				builder.setNegativeButton(getString(R.string.talk_verion_cancel), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						try
						{
							dialog.cancel();
						}
						catch (Exception e)
						{
							// TODO: handle exception
						}
					}
				});
			}
			Dialog d = builder.create();
			d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			if (isShow)
			{
				d.show();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode){
			case KeyEvent.KEYCODE_MENU:
				mOk.setBackgroundResource(R.drawable.bg_list_focuse);
				break;
			case KeyEvent.KEYCODE_BACK:
				mBack.setBackgroundResource(R.drawable.bg_list_focuse);
				break;
		}
		return super.onKeyDown(keyCode,event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode){
			case KeyEvent.KEYCODE_MENU:
				mOk.setBackgroundResource(R.drawable.bg_list_normal);
				Intent intent = new Intent();
				intent.setClassName("com.adups.fota","com.adups.fota.GoogleOtaClient");
				startActivity(intent);

				break;
			case KeyEvent.KEYCODE_BACK:
				mBack.setBackgroundResource(R.drawable.bg_list_normal);
				break;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getKeyCode()==KeyEvent.KEYCODE_MENU){

			getMenuInflater().inflate(R.menu.update_version_menu,mPop.getMenu());
			mPop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					if(!isDownloading)
					{
						mPop.show();
						isShow = true;
						checkVersion();
					}
					mPop.dismiss();
					return true;
				}
			});
		}
		return super.dispatchKeyEvent(event);
	}
}
