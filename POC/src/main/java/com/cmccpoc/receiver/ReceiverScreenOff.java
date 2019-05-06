package com.cmccpoc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.activity.AccountActivity;
import com.cmccpoc.activity.ChargeActivity;
import com.cmccpoc.services.AirServices;

/**
 * 接收屏幕亮或灭状态的广播
 * @author Yao
 */
public class ReceiverScreenOff extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		Log.e(ReceiverScreenOff.class, "ReceiverScreenOff");
		String action = intent.getAction();
		if (action.equals("android.intent.action.SCREEN_OFF") && isCharging(context) && AirtalkeeAccount.getInstance().isAccountRunning()	 )
		{
			startChargeView(context);
		}
	}


	private void startChargeView(Context context) {
		Intent mIntent=new Intent();
		mIntent.setClass(context, ChargeActivity.class);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		context.startActivity(mIntent);
	}

	public static boolean isCharging(Context context) {
		Intent batteryBroadcast = context.registerReceiver(null,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		// 0 means we are discharging, anything else means charging
		boolean isCharging = batteryBroadcast.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0;
		return isCharging;
	}


}
	