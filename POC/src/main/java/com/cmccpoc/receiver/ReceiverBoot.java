package com.cmccpoc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.activity.AccountActivity;
import com.cmccpoc.application.MainApplication;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirAccountManager;
import com.cmccpoc.services.AirServices;

/**
 * 广播接收器：启动poc服务
 * @author Yao
 */
public class ReceiverBoot extends BroadcastReceiver
{
	private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if(intent.getAction().equals(ACTION_SHUTDOWN)){
			try
			{
				AirServices.iOperator.putString(AirAccountManager.KEY_PWD, "");
				AirServices.iOperator.putBoolean(AirAccountManager.KEY_HB, false);
				MainApplication.appExit();
				AirtalkeeAccount.getInstance().Logout();
			}
			catch (Exception e)
			{
			}
		}
	}
}
