package com.cmccpoc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.listener.OnMmiVideoKeyListener;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.Util;

public class ReceiverVideoKey extends BroadcastReceiver
{
	public static final String ACTION_VIDEO_KEY = "android.intent.action.FACER.up";
	private static final int ACTION_TIME = 2*1000;
	
	private static OnMmiVideoKeyListener listener = null;
	
	private long ts = 0;
	
	public static void setOnMmiVideoKeyListener(OnMmiVideoKeyListener l)
	{
		listener = l;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(ReceiverVideoKey.class, "onReceive action=" + intent.getAction());
		if (listener != null)
		{
			if (intent.getAction().equals(ACTION_VIDEO_KEY))
			{
				if (System.currentTimeMillis() - ts > ACTION_TIME)
				{
					Log.i(ReceiverVideoKey.class, "onReceive to handle action (" + intent.getAction() + ")");
					listener.onVideoKey();
					ts = System.currentTimeMillis();
				}
				else if (AirServices.getInstance() != null)
				{
					Util.Toast(AirServices.getInstance(), AirServices.getInstance().getString(R.string.talk_tip_operation_fast));
				}
			}
			abortBroadcast();
		}
	}
	
}
