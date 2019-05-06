package com.cmccpoc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.activity.home.HomeActivity;

public class ReceiverNotification extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(ReceiverNotification.class, "ReceiverNotification onReceive in");
		if (!HomeActivity.isShowing)
		{
			Intent realIntent = intent.getParcelableExtra("realIntent");
			context.startActivity(realIntent);
		}
	}

}
