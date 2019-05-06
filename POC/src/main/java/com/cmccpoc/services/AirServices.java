package com.cmccpoc.services;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeContact;
import com.airtalkee.sdk.AirtalkeeMediaVideoControl;
import com.airtalkee.sdk.AirtalkeeMediaVisualizer;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.AirtalkeeUserRegister;
import com.airtalkee.sdk.AirtalkeeVersionUpdate;
import com.airtalkee.sdk.OnMediaVideoPullListener;
import com.airtalkee.sdk.OnSessionIncomingListener;
import com.airtalkee.sdk.OnSystemCallLimitListener;
import com.airtalkee.sdk.OnSystemUserControlListener;
import com.airtalkee.sdk.OnVersionUpdateListener;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.controller.ResVideoController;
import com.airtalkee.sdk.engine.AirEngine;
import com.airtalkee.sdk.engine.AirPower;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.entity.DBProxy;
import com.airtalkee.sdk.util.IOoperate;
//import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.activity.AccountActivity;
import com.cmccpoc.activity.ChargeActivity;
import com.cmccpoc.activity.VideoSessionActivity;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.activity.home.widget.DialogVersionUpdate;
import com.cmccpoc.activity.home.widget.InCommingAlertDialog;
import com.cmccpoc.activity.home.widget.ToastUtils;
import com.cmccpoc.activity.home.widget.WarningDialog;
import com.cmccpoc.application.MainApplication;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirAccountManager;
import com.cmccpoc.control.AirLocationShareControl;
import com.cmccpoc.control.AirMessageTransaction;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.control.AirSessionMediaSound;
import com.cmccpoc.control.AirVideoManager;
import com.cmccpoc.control.VoiceManager;
import com.cmccpoc.dao.DBHelp;
import com.cmccpoc.receiver.ReceiverConnectionChange;
import com.cmccpoc.receiver.ReceiverPhoneState;
import com.cmccpoc.receiver.ReceiverScreenOff;
import com.cmccpoc.receiver.ReceiverVideoKey;
import com.cmccpoc.util.AirMmiTimer;
import com.cmccpoc.util.Language;
import com.cmccpoc.util.Setting;
import com.cmccpoc.util.Sound;
import com.cmccpoc.util.SoundPlayer;
import com.cmccpoc.util.Util;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.umeng.analytics.MobclickAgent;

/**
 * POC服务，处理app启动与初始化服务
 * @author Yao
 */
public class AirServices extends Service implements OnSessionIncomingListener, OnMediaVideoPullListener, OnVersionUpdateListener, OnSystemUserControlListener, OnSystemCallLimitListener
{
	@SuppressWarnings("deprecation")
	private KeyguardManager.KeyguardLock mKeyguardLock;
	private KeyguardManager km;
	public static final String SERVICE_PATH = "com.cmccpoc.services.AirServices";
	private Dialog incomingDialog;
	private final IBinder mBinder = new LocalBinder();
	private final ReceiverConnectionChange ccr = new ReceiverConnectionChange();
	private BroadcastReceiver receiverScreen;
	private ReceiverVideoKey receiverVideoKey;
	private static AirServices context = null;
	public static boolean isScreenOn = false;

	public static boolean appRunning = false;
	public static DBProxy db_proxy = null;
	public static boolean VERSION_NEW = false;

	public static IOoperate iOperator = null;

	public static AirServices getInstance()
	{
		return context;
	}

	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return mBinder;
	}

	public class LocalBinder extends Binder
	{
		public AirServices getService()
		{
			return AirServices.this;
		}
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		context = this;
		Log.i("wqq", "AirServices onCreate");
		appRun();
		registerReceiver(ccr, new IntentFilter(ReceiverConnectionChange.ACTION));
		registerScreenReceiver();
		registerVideoKeyReceiver();
		appRunning = true;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		unregisterReceiver(ccr);
		if (VoiceManager.getInstance() != null)
			VoiceManager.getInstance().release();
		if (receiverVideoKey != null)
			unregisterReceiver(receiverVideoKey);
		if (receiverScreen != null)
			unregisterReceiver(receiverScreen);
		context = null;
		Log.i("wqq", "AirServices onDestroy");
		appRunning = false;
		System.exit(0);
	}

	/**
	 * app运行时初始化一系列服务与类对象
	 */
	private void appRun()
	{
		try
		{
			Log.e("wqq", "AirServices  appRun!");
			IOoperate.setContext(this);
			iOperator = new IOoperate();
			AirMmiTimer.getInstance();
			Util.versionConfig(this);
			VoiceManager.newInstance(this);
			MobclickAgent.updateOnlineConfig(this);
			initImageLoader();
			SoundPlayer.soundInit(this);
			Setting.getPttClickSupport();
			Setting.getPttVolumeSupport();

			db_proxy = new DBHelp(this);
			db_proxy.DbActionRun();
			AirtalkeeAccount.getInstance();
			AirtalkeeMessage.getInstance();
			AirtalkeeSessionManager.getInstance();
			AirtalkeeChannel.getInstance();
			AirtalkeeUserInfo.getInstance();
			AirtalkeeContact.getInstance();
			AirtalkeeContact.getInstance();
			AirtalkeeUserRegister.getInstance();
			AirMessageTransaction.getInstance();
			AirSessionControl.getInstance();
			AirAccountManager.getInstance();
			AirVideoManager.getInstance();

			AirtalkeeMediaVisualizer.getInstance().setMediaAudioVisualizerValid(true, true);
			AirtalkeeMediaVisualizer.getInstance().setMediaAudioVisualizerSpectrumNumber(18);
			AirPower.PowerName("AudioMix");
			AirtalkeeAccount.getInstance().AirTalkeePowerManagerKeepWake(this);
			AirtalkeeAccount.getInstance().AirTalkeeConfig(this, Config.serverAddress, 4001, false, false, false);
			AirtalkeeAccount.getInstance().AirTalkeeConfigMarketCode(Config.marketCode);
			AirtalkeeAccount.getInstance().dbProxySet(db_proxy);
			AccountController.serviceDmOmaSet(Config.serverDmOmaIp, Config.serverDmOmaPort);
			AirtalkeeSessionManager.getInstance().setMediaEngineSetting(Setting.getPttHeartbeat(), Config.engineMediaSettingHbPackSize);
			AirtalkeeSessionManager.getInstance().setOnSessionIncomingListener(this);
			AirtalkeeSessionManager.getInstance().setSessionDialogSetAnswerMode(Setting.getPttAnswerMode() ? AirSession.INCOMING_MODE_AUTO : AirSession.INCOMING_MODE_MANUALLY);
			AirtalkeeSessionManager.getInstance().setSessionDialogSetIsbMode(Setting.getPttIsb());
			AirtalkeeSessionManager.getInstance().setOnMediaSoundListener(new AirSessionMediaSound(this));
			AirtalkeeMediaVideoControl.getInstance().setVideoAudioStream(Config.funcVideoAudioStream);
			if (Config.funcVideoPull)
				AirtalkeeMediaVideoControl.getInstance().setOnMediaVideoPullListener(this);
			AccountController.setAccountInfoAutoLoad(true);
			AccountController.setAccountInfoOfflineMsgLoad(true);
			ResVideoController.setVideoStreamPlay(Config.funcVideoPlay);
			AirtalkeeAccount.getInstance().setOnSystemUserControlListener(this);
			AirtalkeeAccount.getInstance().setOnSystemCallLimitListener(this);
			// SDKInitializer.initialize(getApplicationContext());// 百度地图
			AirtalkeeSessionManager.getInstance().setAudioAmplifier(Setting.getVoiceAmplifier());
			if (!Environment.MEDIA_REMOVED.equals(Environment.getExternalStorageState()))
			{
				AirtalkeeSessionManager.getInstance().MediaRealtimeRecordEnable();
			}
			AirtalkeeMessage.getInstance().setMessageListNumberMax(10);

			String userId = iOperator.getString(AirAccountManager.KEY_ID, "");
			String userPwd = iOperator.getString(AirAccountManager.KEY_PWD, "");
			boolean userHb = iOperator.getBoolean(AirAccountManager.KEY_HB, false);
			AirtalkeeAccount.getInstance().loginAutoBoot(userId, userPwd, userHb);
			AirLocationShareControl.getInstance();
		}
		catch (Exception e)
		{
			Log.e("wqq", "AirServices run Exception!");
			e.printStackTrace();
		}
	}

	public DBProxy dbProxy()
	{
		return db_proxy;
	}

	/**
	 * 注册屏幕状态广播
	 */
	public void registerScreenReceiver()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		receiverScreen = new ReceiverScreenOff();
		registerReceiver(receiverScreen, filter);
	}
	
	public void registerVideoKeyReceiver()
	{
		if (Config.model.equals("POC") || Config.model.equals("BXDS-23") || Config.model.equals("NT11"))
		{
			receiverVideoKey = new ReceiverVideoKey();
			registerReceiver(receiverVideoKey, new IntentFilter(ReceiverVideoKey.ACTION_VIDEO_KEY));
		}
	}
	/**
	 * 点亮屏幕
	 */
	@SuppressWarnings("deprecation")
	public void lightScreen()
	{
		PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "");
		wakeLock.acquire(20 * 1000);
	}

	/**
	 * 禁用锁屏功能
	 */
	@SuppressWarnings("deprecation")
	public void unlockScreen()
	{
		if (mKeyguardLock == null || km == null)
		{
			km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
			mKeyguardLock = km.newKeyguardLock("SimpleTimer");
		}
		if (!isScreenOn)
		{
			mKeyguardLock.disableKeyguard();
			isScreenOn = true;
		}
	}

	/**
	 * 尝试锁定屏幕
	 */
	@SuppressWarnings("deprecation")
	public void lockScreen()
	{
		if (mKeyguardLock == null || km == null)
		{
			km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
			mKeyguardLock = km.newKeyguardLock("SimpleTimer");
		}
		if (isScreenOn)
		{
			isScreenOn = false;
			mKeyguardLock.reenableKeyguard();
		}
	}

	/***********************************
	 * 
	 * PTT
	 * 
	 ***********************************/

	private boolean isCalling = false;

	@Override
	public void onSessionIncomingAlertStart(AirSession session, AirContact caller, boolean isAccepted, boolean videoPush, boolean videoPull)
	{
		// TODO Auto-generated method stub
		Log.i("wqq", "onSessionIncomingAlertStart");

		if (ReceiverPhoneState.isPhoneCalling(this) || isCalling)
		{
			Log.i("wqq", "onSessionIncomingAlertStart - SessionIncomingBusy (isCalling=" + isCalling + ") ");
			AirtalkeeSessionManager.getInstance().SessionIncomingBusy(session);
			AirtalkeeMessage.getInstance().MessageSystemGenerate(session, session.getCaller(), getString(R.string.talk_call_state_missed_call), true);
			return;
		}

		AirtalkeeMessage.getInstance().MessageRecordPlayStop();
		lightScreen();
		unlockScreen();
		if (session != null)
		{
			final AirSession temAirSession = session;
			if (Setting.getPttIsb())
			{
				AirtalkeeSessionManager.getInstance().SessionIncomingBusy(temAirSession);
				return;
			}
			else if (Setting.getPttAnswerMode() && !videoPull)
			{
				AirtalkeeSessionManager.getInstance().SessionIncomingAccept(temAirSession);
				AirtalkeeMessage.getInstance().MessageSystemGenerate(temAirSession, getString(R.string.talk_call_state_incoming_call), false);
				try
				{
					if (temAirSession != null)
					{
						AirtalkeeSessionManager.getInstance().getSessionByCode(temAirSession.getSessionCode());
						HomeActivity.getInstance().onViewChanged(session.getSessionCode());
						HomeActivity.getInstance().panelCollapsed();

						if (videoPull && Config.funcVideoPull)
						{
							Intent i = new Intent();
							i.setClass(this, VideoSessionActivity.class);
							i.putExtra("sessionCode", session.getSessionCode());
							i.putExtra("auto", true);
							startActivity(i);
						}
					}
				}
				catch (Exception e)
				{
					// TODO: handle exception
				}
				return;
			}
			else
			{
				if (videoPull)
				{
					if (VideoSessionActivity.isRecording())
						AirtalkeeSessionManager.getInstance().SessionIncomingBusy(temAirSession);
					else
					{
						Sound.playSound(Sound.PLAYER_INCOMING_RING, true, context);
						incomingDialog = new InCommingAlertDialog(context, temAirSession, caller, videoPush, videoPull);
						incomingDialog.show();
					}
				}
				else
				{
					Sound.playSound(Sound.PLAYER_INCOMING_RING, true, context);
					incomingDialog = new InCommingAlertDialog(context, temAirSession, caller, videoPush, videoPull);
					incomingDialog.show();
				}
			}
		}
	}

	@Override
	public void onSessionIncomingAlertStop(AirSession session)
	{
		// TODO Auto-generated method stub
		isCalling = false;
		Log.i("wqq", "onSessionIncomingAlertStop");
		Sound.stopSound(Sound.PLAYER_INCOMING_RING);
		if (session != null && !session.isCallHandled())
		{
			AirtalkeeMessage.getInstance().MessageSystemGenerate(session, session.getCaller(), getString(R.string.talk_call_state_missed_call), true);
		}
		if (incomingDialog != null)
		{
			incomingDialog.cancel();
		}
	}

	@Override
	public void onVideoPull(AirSession session, AirContact contact)
	{
		if (session != null && contact != null)
		{
			if (!VideoSessionActivity.isRecording())
			{
				Sound.playSound(Sound.PLAYER_INCOMING_RING, true, context);
				incomingDialog = new InCommingAlertDialog(context, session, contact, false, true);
				incomingDialog.show();
			}
		}
	}


	public static final int TEMP_SESSION_TYPE_OUTGOING = 0;
	public static final int TEMP_SESSION_TYPE_INCOMING = 1;
	public static final int TEMP_SESSION_TYPE_MESSAGE = 2;
	public static final int TEMP_SESSION_TYPE_RESUME = 10;

	/***********************************
	 * 
	 * 
	 ***********************************/

	/**
	 * 版本检测
	 */
	public void versionCheck()
	{
		String lang = Language.getLocalLanguage(this);
		AirtalkeeVersionUpdate.getInstance().versionCheck(this, AirtalkeeAccount.getInstance().getUserId(), Config.marketCode, lang, Config.VERSION_PLATFORM, Config.VERSION_TYPE, Config.model, Util.getImei(this), Config.VERSION_NAME, Config.VERSION_CODE);
	}

	@Override
	public void UserVersionUpdate(int versionFlag, String versionInfo, final String url)
	{
		if (versionFlag == 0)
			return;
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
					DialogVersionUpdate update = new DialogVersionUpdate(HomeActivity.getInstance(), url);
					update.show();
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
		d.show();
	}

	/**
	 * 初始化图片加载器
	 */
	private void initImageLoader()
	{
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO).writeDebugLogs().build();
		ImageLoader.getInstance().init(config);
	}

	private boolean isSecretValid = false;

	public boolean secretValid()
	{
		return isSecretValid;
	}

	/**
	 * 发送广播通用方法
	 * @param action action标记
	 */
	public static void sendBroadcast(String action)
	{
		if (action != null && action.length() > 0)
		{
			Intent intent = new Intent();
			intent.setAction(action);
			if (AirServices.getInstance() != null)
				AirServices.getInstance().sendBroadcast(intent);
			Log.i("wqq", "broadcast  " + action + "  send!!!");
		}
	}

	private static WindowManager mWindowManager = null;
	private static View mView = null;
	public static Boolean isShown = false;

	public void showPopWindow() {
		// 获取WindowManager
		if (isShown) {
			return;
		}
		isShown = true;
		mWindowManager = (WindowManager)
				getSystemService(Context.WINDOW_SERVICE);
		mView = LayoutInflater.from(this).inflate(R.layout.popupwindow_layout, null);
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		// 类型
		//params.type = WindowManager.LayoutParams.TYPE_TOAST;
		// WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
		android.util.Log.d("zlmt", "showPopWindow...TYPE_SYSTEM_ALERT");
		params.type = WindowManager.LayoutParams.TYPE_PHONE;
		int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		params.flags = flags;
		params.format = PixelFormat.TRANSLUCENT;
		params.width = WindowManager.LayoutParams.MATCH_PARENT;
		params.height = WindowManager.LayoutParams.MATCH_PARENT;
		params.gravity = Gravity.CENTER;
		mWindowManager.addView(mView, params);
	}

	public static void hidePopupWindow() {
		android.util.Log.d("zlmt", "hidePopupWindow...TYPE_SYSTEM_ALERT");
		if (isShown && null != mView) {
			mWindowManager.removeView(mView);
			isShown = false;
		}
	}

	@Override
	public void onSystemUserControl(int action)
	{
		android.util.Log.d("zlmt","onSystemUserControl");
		if (action == OnSystemUserControlListener.REMOTE_CONTROL_TURN_OFF)
		{
			lightScreen();
			if(!isShown){
				showPopWindow();
			}
			sendBroadcast(new Intent(ChargeActivity.REMOVE_CHARGE));
			AirtalkeeAccount.getInstance().Logout();
			/*Dialog dialog;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.talk_system_user_ctl_turn_off));
			builder.setCancelable(false);
			builder.setPositiveButton(getString(R.string.talk_exit), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					try
					{
						stopSelf();
						if (HomeActivity.getInstance() != null)
							HomeActivity.getInstance().moveTaskToBack(false);
						dialog.cancel();
						System.exit(0);
					}
					catch (Exception e)
					{
					}
				}
			});
			dialog = builder.create();
			dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			dialog.show();*/
		}
		else if (action == OnSystemUserControlListener.REMOTE_CONTROL_TURN_RESET)
		{
			Dialog dialog;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.talk_system_user_ctl_turn_reset));
			builder.setCancelable(false);
			builder.setPositiveButton(getString(R.string.talk_exit), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					try
					{
						dialog.cancel();
						iOperator.putString(AirAccountManager.KEY_PWD, "");
						iOperator.putBoolean(AirAccountManager.KEY_HB, false);
						stopSelf();
						System.exit(0);
					}
					catch (Exception e)
					{
						// TODO: handle exception
					}
				}
			});
			dialog = builder.create();
			dialog.show();
		}
	}

	@Override
	public void onSystemCallLimit(int action, String channelId)
	{
		android.util.Log.d("zlmt","onSystemCallLimit");
		if (action >= OnSystemCallLimitListener.LIMIT_CALL_IN || action <= OnSystemCallLimitListener.LIMIT_CHANNEL_ITEM)
		{
			String tip = "";
			switch (action)
			{
				case OnSystemCallLimitListener.LIMIT_CALL_IN:
					ToastUtils.showCustomImgToast(getString(R.string.call_limit), R.drawable.ic_warning, this);
					tip = getString(R.string.talk_system_limit_call_in);
					break;
				case OnSystemCallLimitListener.LIMIT_CALL_OUT:
					ToastUtils.showCustomImgToast(getString(R.string.call_limit), R.drawable.ic_warning, this);
					tip = getString(R.string.talk_system_limit_call_out);
					break;
				case OnSystemCallLimitListener.LIMIT_CHANNEL_ALL:
					tip = getString(R.string.talk_system_limit_channel);
					break;
				case OnSystemCallLimitListener.LIMIT_CHANNEL_ITEM:
				{
					ToastUtils.showCustomImgToast(getString(R.string.call_limit), R.drawable.ic_warning, this);
					String chName = channelId;
					AirChannel ch = AirtalkeeChannel.getInstance().ChannelGetByCode(channelId);
					if (ch != null)
						chName = ch.getDisplayName();
					tip = String.format(getString(R.string.talk_system_limit_channel_item), chName);
					AirSessionControl.getInstance().SessionChannelOut(channelId);
					AirSessionControl.getInstance().SessionChannelIn(channelId);
					break;
				}
			}
			Dialog dialog;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(tip);
			builder.setCancelable(false);
			builder.setPositiveButton(getString(R.string.talk_exit), new DialogInterface.OnClickListener()
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
			dialog = builder.create();
			dialog.show();
		}
	}
}
