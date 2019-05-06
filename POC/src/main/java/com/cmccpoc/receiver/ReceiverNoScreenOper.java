package com.cmccpoc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.services.AirServices;

public class ReceiverNoScreenOper extends BroadcastReceiver
{

	private static ReceiverNoScreenOper instance;
	private IntentFilter intentFilter = null;

	public static ReceiverNoScreenOper getInstance()
	{
		if (instance == null)
		{
			instance = new ReceiverNoScreenOper();
		}
		return instance;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		Log.e("m", "com.airtalkee.receiver onReceive");
	}

	public void receiveUnReigster()
	{
		if (AirServices.getInstance() != null)
			AirServices.getInstance().unregisterReceiver(instance);
	}
}
