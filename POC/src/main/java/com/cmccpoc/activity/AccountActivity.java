package com.cmccpoc.activity;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.OnUserInfoListener;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactGroup;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.listener.AccountByImeiListener;
//import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.ChannelListActivity;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.activity.home.IMFragment;
import com.cmccpoc.activity.home.MemberFragment;
import com.cmccpoc.activity.home.MemberListActivity;
import com.cmccpoc.activity.home.PTTFragment;
import com.cmccpoc.activity.home.widget.WarningDialog;
import com.cmccpoc.application.MainApplication;
import com.cmccpoc.auth.AuthSso;
import com.cmccpoc.auth.AuthSsoListener;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirAccountManager;
import com.cmccpoc.control.AirReportManager;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.listener.OnMmiAccountListener;
import com.cmccpoc.listener.OnMmiChannelListener;
import com.cmccpoc.receiver.ReceiverExtSamsung;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.Util;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 处理用户登录界面的Activity
 * 登录成功：获取频道列表信息
 * 登录失败：根据不同原因会弹出不同的Toast提示
 */
public class AccountActivity extends ActivityBase implements /*OnClickListener,*/ OnMmiAccountListener, OnMmiChannelListener, OnUserInfoListener/*, OnTouchListener*/, AccountByImeiListener, AuthSsoListener
{
	private EditText etIpocid;
	private EditText etPwd;
	// private TextView tvRegister;
	private View btnLogin;
	private LinearLayout layoutInput, layoutWaiting;
	private TextView tvInfo;
	private TextView tvFindPwd, tvSso;

	private final int STATE_IDLE = 0;
	private final int STATE_SSO = 1;
	private final int STATE_LOGIN = 2;
	private final int STATE_LOADING = 3;
	private final int STATE_NETWORK=4;
	private final int STATE_NO_SIM_CARD=5;
    private Handler handler = new Handler();
    private MyHandler myHandler;
	private static AccountActivity instance = null;
	private static final int MSG_NO_SIM_CARD=6;
	private static final int MSG_NOT_NETWORK=7;
	private ImageView mIvLogoState;
	/**
	 * 获取AccountActivity的实例对象
	 * @return
	 */
	public static AccountActivity getInstance()
	{
		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i("wqq", "AccountActivity onCreate");
        AuthSso.getInstance().init(this);
		setRequestedOrientation(Config.screenOrientation);
		if (!Utils.isEmpty(Config.model) && Config.model.startsWith("OINOM"))
		{
			final Window win = getWindow();
			win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
			win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		}
		instance = this;
		setContentView(R.layout.activity_account);
		myHandler=new MyHandler(AccountActivity.this.getMainLooper());
		doInitFindView();

		ReceiverExtSamsung.activePolicy(this);

		/*if (Util.dexCrcCheck(this) == false)//user version error
		{
			//System.exit(0);
		}*/

		/*
		String packname = getPackageName();
		PackageManager pm = getPackageManager();
		boolean permission = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.SYSTEM_ALERT_WINDOW", packname));
		*/
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onStart()
	{
		super.onStart();
		if (MainApplication.isFisrtLaunch() && !Config.funcBootLaunch)
		{
			showDialog(R.id.talk_dialog_ascess_network);
			Log.i("wqq", "MainApplication.isFisrtLaunch()  && !Config.funcLaunch");
		} else {
            //new Task().execute();
		}
        getResponse();

	}

	private void getResponse(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //轮询token发请求获取phoneid和authkey
                while(TextUtils.isEmpty(AuthSso.getInstance().mToken) || !Util.isNetWorkAvailable(AccountActivity.this)){
					if(!Util.ishasSimCard(AccountActivity.this) || !Util.isNetWorkAvailable(AccountActivity.this)){
						Message msg = myHandler.obtainMessage();
                    	if(!Util.ishasSimCard(AccountActivity.this)){
                        	msg.what=MSG_NO_SIM_CARD;
                    	}else if(!Util.isNetWorkAvailable(AccountActivity.this)){
							msg.what=MSG_NOT_NETWORK;
						}
						myHandler.sendMessage(msg);
					}

					if(TextUtils.isEmpty(AuthSso.getInstance().mToken)){
						AuthSso.getInstance().init(AccountActivity.this);
						Log.i("wqq", "getMtoken: "+AuthSso.getInstance().mToken);
					}

					try {
						Thread.sleep(500);
					}catch (Exception e){
						e.printStackTrace();
					}
				}

				do {
					AuthSso.getInstance().RunDmReport(AccountActivity.this, Config.serverCmccCustomerId, AirtalkeeAccount.getInstance().getUserId());
				}while(TextUtils.isEmpty(AuthSso.getInstance().mKey));

                //向Handler发送处理操作
              	handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						new Task().execute();
						Log.i("wqq", "airtalkservice start..: ");
					}
				},50);
            }
        }).start();
    }
	/**
	 * 验证登录
	 */
	private void accountCheck()
	{
		if (!AirtalkeeAccount.getInstance().isAccountRunning() )
		{
			Log.i("wqq", "AccountActivity accountCheck");
			AirAccountManager.getInstance().setAccountListener(this);
			AirAccountManager.getInstance().setChannelListener(this);
			AirtalkeeUserInfo.getInstance().setOnUserInfoListener(this);
			String phoneId = AuthSso.getInstance().mPhoneId;
			String key = AuthSso.getInstance().mKey;
			Log.i("wqq", "phoneId=="+phoneId+" key=="+key);
			String userId = AirServices.iOperator.getString(AirAccountManager.KEY_ID);
			String userPwd = AirServices.iOperator.getString(AirAccountManager.KEY_PWD);
			Log.i("wqq", "userId=="+userId+" userPwd=="+userPwd);
			boolean toLogin = false;
			if (!Utils.isEmpty(phoneId) && !Utils.isEmpty(key))
			{
				toLogin = true;
			}
			if (toLogin) {
				AirtalkeeAccount.getInstance().LoginByAuthKey(phoneId, key);
				accountStateShow(STATE_LOGIN);
			}
			else
			{
				AirServices.iOperator.clean();
			}
		} else {
			Intent it = new Intent(this, HomeActivity.class);
			startActivity(it);
			finish();
		}
	}

	private void toFindPassword()
	{
		if (MenuPasswordFindActivity.isFindingPwd)
		{
			Intent it = new Intent(this, MenuPasswordFindActivity.class);
			startActivity(it);
		}
	}

	/**
	 * 用户登录状态反馈
	 * @param state:状态
	 */
	private void accountStateShow(int state)
	{
		switch (state)
		{
			case STATE_IDLE:
				layoutWaiting.setVisibility(View.GONE);
				break;
			case STATE_SSO:
				layoutWaiting.setVisibility(View.VISIBLE);
				tvInfo.setText(getString(R.string.talk_login_sso));
				break;
			case STATE_LOGIN:
				layoutWaiting.setVisibility(View.VISIBLE);
				tvInfo.setText(getString(R.string.talk_logining));
				break;
			case STATE_LOADING:
				layoutWaiting.setVisibility(View.VISIBLE);
				tvInfo.setText(getString(R.string.talk_login_loading));
				break;
            case STATE_NETWORK:
                layoutWaiting.setVisibility(View.VISIBLE);
                tvInfo.setText(getString(R.string.network_not_avilable));
                break;
            case STATE_NO_SIM_CARD:
                layoutWaiting.setVisibility(View.VISIBLE);
                mIvLogoState.setImageResource(R.drawable.ic_warning);
                tvInfo.setText(getString(R.string.no_simcard_hint));
                break;
			default:
				break;
		}
	}

	private void updateStateInfo(int resid,String text ){
		mIvLogoState.setImageResource(resid);
		tvInfo.setText(text);
	}

	private class Task extends AsyncTask<Void, Void, String[]>
	{
		int times = 0;
		protected String[] doInBackground(Void... params) {
			while (!AirServices.appRunning) {
				if (times == 0) {
                    Log.i("wqq", "Task execute start services");
                    Intent intent = new Intent(AirServices.SERVICE_PATH);
                    AccountActivity.this.startService(intent);
				}
				try {
					Log.i("wqq", "Task looping ... times=[" + times + "]");
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				times++;
				if (times > 30) {
					Log.i("wqq", "Task reset state");
					times = 0;
				}
			}
			return null;
		}

		protected void onPostExecute(String[] result) {
			Log.i("wqq", "Task post execute");
			accountCheck();
			super.onPostExecute(result);
		}
	}

	/**
	 * 在onCreate时初始化控件
	 */
	public void doInitFindView()
	{
		// TODO Auto-generated method stub
		layoutWaiting = (LinearLayout) findViewById(R.id.talk_account_waiting);
		tvInfo = (TextView) findViewById(R.id.talk_account_info);

		if (Config.funcShowCustomLogo) {
			TextView logoText = (TextView) findViewById(R.id.talk_copyright_text);
			ImageView logoImage = (ImageView) findViewById(R.id.talk_copyright_logo);
			if (Config.funcShowCustomLogoStringId1 != 0)
				logoText.setText(getString(Config.funcShowCustomLogoStringId1));
			if (Config.funcShowCustomLogoIconId != 0)
				logoImage.setImageResource(Config.funcShowCustomLogoIconId);
		}
		mIvLogoState=((ImageView) findViewById(R.id.iv_login_logo));
		mIvLogoState.setImageResource(Config.app_icon_login);

		if (Config.app_icon_login == R.drawable.icon_cmcc_orange)
			findViewById(R.id.logo).setVisibility(View.VISIBLE);

		if (Config.funcShowAppText) {
			TextView logo = (TextView) findViewById(R.id.logo);
			logo.setVisibility(View.VISIBLE);
			logo.setText(Config.app_name);
		}

		/*tvFindPwd = (TextView) findViewById(R.id.talk_btn_find_pwd);
		if (Config.funcPasswordFind)
		{
			tvFindPwd.setVisibility(View.VISIBLE);
			tvFindPwd.setOnClickListener(this);
		}
		else
			tvFindPwd.setVisibility(View.GONE);

		tvSso = (TextView) findViewById(R.id.talk_btn_sso);
		tvSso.setVisibility(View.GONE);*/
	}

	@Override
	public void finish()
	{
		super.finish();
		AirAccountManager.getInstance().setAccountListener(null);
	}

	/**
	 * 根据不同状态，构建Dialog窗口提示
	 */
	@SuppressWarnings("deprecation")
	protected Dialog onCreateDialog(int id)
	{
		if (id == R.id.talk_dialog_login_waiting)
		{
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage(getString(R.string.talk_logining));
			dialog.setOnCancelListener(new OnCancelListener()
			{

				@Override
				public void onCancel(DialogInterface dialog)
				{
					// TODO Auto-generated method stub
					AirServices.iOperator.putBoolean(AirAccountManager.KEY_HB, false);
					AirtalkeeAccount.getInstance().Logout();
				}
			});
			return dialog;
		}
		else if (id == R.id.talk_dialog_network_error)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.talk_network_error_hint));
			builder.setPositiveButton(getString(R.string.talk_set_network), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.cancel();
					Intent intent = null;
					if (android.os.Build.VERSION.SDK_INT > 10)
					{
						intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
					}
					else
					{
						intent = new Intent(Intent.ACTION_MAIN);
						ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
						intent.setComponent(componentName);
					}
					if (intent != null)
						startActivity(intent);
				}
			});

			builder.setNegativeButton(getString(R.string.talk_exit), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.cancel();
					finish();
				}
			});
			return builder.create();
		}
		/*else if (id == R.id.talk_dialog_ascess_network)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.talk_protocol));
			builder.setCancelable(false);
			builder.setPositiveButton(R.string.talk_ok, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.cancel();
					new Task().execute();

				}
			});
			/*builder.setNegativeButton(R.string.talk_no, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.cancel();
					finish();
				}
			});
			return builder.create();
		}*/
		return super.onCreateDialog(id);
	}

	/*@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			// 登录按钮
			case R.id.talk_btn_login:
			{
				Util.hideSoftInput(instance);
				{
					String ipocId = etIpocid.getText().toString();
					String pwd = etPwd.getText().toString();
					Log.i("wqq", ipocId + pwd);
					if (!ipocId.equals("") && !pwd.equals(""))
					{
						if (pwd.length() < 6)
						{
							Util.Toast(this, getString(R.string.talk_pwd_error), R.drawable.ic_error);
							return;
						}
						AirtalkeeAccount.getInstance().Login(ipocId, pwd);

						accountStateShow(STATE_LOGIN);
					}
					else
					{
						Util.Toast(this, getString(R.string.talk_account_isnotnull), R.drawable.ic_error);
					}
				}
				break;
			}
			case R.id.talk_btn_find_pwd:
			{
				Intent it = new Intent(this, MenuPasswordFindActivity.class);
				startActivity(it);
				break;
			}
			default:
				break;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			Util.hideSoftInput(instance);
		return super.onTouchEvent(event);
	}*/

	@Override
	public void onUserIdGetByPhoneNum(int result, AirContact contact)
	{
		// TODO Auto-generated method stub
		if (result == 0 && contact != null)
		{
			AirtalkeeAccount.getInstance().Login(contact.getIpocId(), contact.getPwd());
		}
		else
		{
			accountStateShow(STATE_IDLE);
			Util.Toast(this, getString(R.string.talk_login_login_failed_user_or_password), R.drawable.ic_error);
		}
	}

	@Override
	public void onUserInfoGet(AirContact user)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserInfoUpdate(boolean isOk, AirContact user)
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

	/*@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		// TODO Auto-generated method stub

		switch (v.getId())
		{
			case R.id.talk_et_ipocid:
			case R.id.talk_et_ipocpwd:
			{
				v.setFocusableInTouchMode(true);
			}
		}

		return false;
	}*/

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		// TODO Auto-generated method stub
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onMmiHeartbeatException(int result)
	{
	}

	@Override
	public void onUserIdGetByImei(int state, String uid, String pwd)
	{
		// TODO Auto-generated method stub
		if (state == 0)
		{
			etIpocid.setText(uid);
			etPwd.setText(pwd);
			Util.Toast(this, getString(R.string.talk_logining));
			AirtalkeeAccount.getInstance().LoginByAuthKey(uid, pwd);
			accountStateShow(STATE_LOGIN);
		}
		else if (state == 1)
		{
			Util.Toast(this, getString(R.string.talk_account_bind_error), R.drawable.ic_error);
			Util.Toast(this, getString(R.string.talk_account_get_error), R.drawable.ic_error);
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_account_get_error), R.drawable.ic_error);
		}
	}

	@Override
	public void onMmiHeartbeatLogin(int result)
	{
		Log.i("zlm", "onMmiHeartbeatLogin");
		// TODO Auto-generated method stub
		// removeDialog(R.id.talk_dialog_login_waiting);
		if (result == AirtalkeeAccount.ACCOUNT_RESULT_OK)
		{
			accountStateShow(STATE_LOADING);
			AirServices.iOperator.putBoolean(AirAccountManager.KEY_HB, true);
		}
		else
		{
			accountStateShow(STATE_IDLE);
			if(result==AirtalkeeAccount.ACCOUNT_RESULT_ERR_ACCOUNT_FORBIDDEN){
				AirServices.getInstance().showPopWindow();
				mHandler.sendEmptyMessageDelayed(0x01,5000);
			}else{
				Util.Toast(this, Util.loginInfo(result, this), R.drawable.ic_error);
				startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_LOGIN);
			}

		}
	}

	@Override
	public void onMmiHeartbeatLogout()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onChannelListGet(boolean isOk, List<AirChannel> channels)
	{
		Log.i("wqq", "AccountActivity onChannelListGet"+channels.size());//0
		if (isOk)
		{
			Log.i("wqq", "AccountActivity onChannelListGet OK!");
			AirReportManager.getInstance().loadReports();
			Log.i("zlm", "onChannelListGet...startActivity.....HomeActivity");
			Intent it = new Intent(this, HomeActivity.class);
			it.putExtra("notice", true);
			startActivity(it);
			finish();
			AirServices.getInstance().hidePopupWindow();
		}
		else
		{
			Log.i("wqq", "AccountActivity onChannelListGet Fail!");
			accountStateShow(STATE_IDLE);
			Util.Toast(this, getString(R.string.talk_channel_list_getfail), R.drawable.ic_error);
		}
	}

	@Override
	public void onChannelMemberListGet(String channelId, List<AirContact> members)
	{
		// TODO Auto-generated method stub
		if (ChannelListActivity.getInstance() != null)
		{
			ChannelListActivity.getInstance().sessionRefresh();
		}
	}

	@Override
	public void onChannelOnlineCount(LinkedHashMap<String, Integer> online)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onChannelPersonalCreateNotify(AirChannel ch)
	{
		if (AirSessionControl.getInstance().getCurrentChannelSession() == ch.getSession())
		{
			try
			{
				MemberListActivity.getInstance().refreshMembers(ch);
				AllMemberActivity.getInstance().refreshMemberList();
				ChannelListActivity.getInstance().sessionRefresh();
			}
			catch (Exception e)
			{ }

		}
	}

	@Override
	public void onChannelPersonalDeleteNotify(AirChannel ch)
	{
		if (AirSessionControl.getInstance().getCurrentChannelSession() == ch.getSession())
		{
			try
			{
				HomeActivity.getInstance().setMediaStatusBarSession(ch.getSession());
				ChannelListActivity.getInstance().sessionRefresh();
				MemberListActivity.getInstance().refreshMembers(AirSessionControl.getInstance().getCurrentChannelSession().getChannel());
				AllMemberActivity.getInstance().refreshMemberList();
				IMFragment.getInstance().onResume();
				PTTFragment.getInstance().onResume();
				PTTFragment.getInstance().refreshPlayback();
			}
			catch (Exception e)
			{ }
		}
	}

	@Override
	public void onChannelMemberAppendNotify(AirChannel ch, List<AirContact> members)
	{
		MemberListActivity.getInstance().refreshMembers(ch);
	}

	@Override
	public void onChannelMemberDeleteNotify(AirChannel ch, List<AirContact> members)
	{
		MemberListActivity.getInstance().refreshMembers(ch);
	}

	@Override
	public void onChannelMemberUpdateNotify(AirChannel ch, List<AirContact> members)
	{
		MemberListActivity.getInstance().refreshMembers(ch);
	}

	@Override
	public void onAuthSsoTokenGetting() {

	}

	@Override
	public void onAuthSsoTokenGet(boolean isOk) {
		if (!isOk)
		{
			Log.i("wqq", "AccountActivity onAuthSsoTokenGet Fail!");
			accountStateShow(STATE_IDLE);
			//Util.Toast(this, getString(R.string.talk_login_fail_sso), R.drawable.ic_error);
			updateStateInfo(R.drawable.ic_warning,getString(R.string.talk_login_fail));
		}
	}

	@Override
	public void onAuthSsoUserInfoGetting() {

	}

	@Override
	public void onAuthSsoUserInfoGet(int result, String uid, String pwd) {

		if (result == AuthSso.USER_INFO_RESULT_OK)
		{
			accountStateShow(STATE_LOGIN);
			AirtalkeeAccount.getInstance().LoginByAuthKey(uid, pwd);
		}
		else if (result == AuthSso.USER_INFO_RESULT_INVALID)
		{
			accountStateShow(STATE_IDLE);
			//Util.Toast(this, getString(R.string.talk_login_failed_invalid), R.drawable.ic_error);
			updateStateInfo(R.drawable.ic_warning,getString(R.string.talk_login_failed_invalid));
		}
		else
		{
			accountStateShow(STATE_IDLE);
			//Util.Toast(this, getString(R.string.talk_login_fail_sso), R.drawable.ic_error);
			updateStateInfo(R.drawable.ic_warning,getString(R.string.talk_login_fail));
		}
	}

	class MyHandler extends Handler{
	    public MyHandler(){

        }
        public MyHandler(Looper looper){
	        super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_NO_SIM_CARD:
                    accountStateShow(STATE_NO_SIM_CARD);
                    break;
                case MSG_NOT_NETWORK:
                    accountStateShow(STATE_NETWORK);
                    break;
            }
        }
    }


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode){
			case KeyEvent.KEYCODE_MENU:
			case KeyEvent.KEYCODE_BACK:
				return false;
		}
		return super.onKeyDown(keyCode,event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode){
			case KeyEvent.KEYCODE_MENU:
			case KeyEvent.KEYCODE_BACK:
				return false;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mHandler.removeMessages(0x01);
	}

	private static final int REQUEST_LOGIN = 1;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_LOGIN) {
			if(resultCode == RESULT_OK) {
                accountCheck(data.getStringExtra(InputActivity.PHONE), data.getStringExtra(InputActivity.PASSWORD));
			} else {
				accountCheck();
			}
		} else {
			Log.e("wqq", "onActivityResult:requestCode is " + requestCode);
		}
	}

    private void accountCheck(String userId, String userPwd) {
        if (!AirtalkeeAccount.getInstance().isAccountRunning()) {
            Log.i("wqq", "accountCheck(" + userId + ", " + userPwd + ")");
            if (!Utils.isEmpty(userId) && !Utils.isEmpty(userPwd)) {
                AirtalkeeAccount.getInstance().Login(userId, userPwd);
                accountStateShow(STATE_LOGIN);
            } else {
                AirServices.iOperator.clean();
            }
        } else {
            Log.i("zlm", "startActivity...accountCheck..HomeActivity");
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

	Handler	mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
				case 0x01:
					accountCheck();
			}

		}
	};


}
