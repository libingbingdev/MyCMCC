package com.cmccpoc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.activity.AccountActivity;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.services.AirServices;

/**
 * 接收媒体按键广播
 * 
 * @author Yao
 */
public class ReceiverMediaButton extends BroadcastReceiver
{
	public static boolean isPttPressed = false;
	public static boolean isChannelToogle = false;

	private static final String EARPHONE_PTT_BUTTON = "android.intent.action.PTT_BUTTON";

	@Override
	public void onReceive(Context context, Intent intent)
	{

	}

	private void launch(Context context)
	{
		Intent it = new Intent(context, AccountActivity.class);
		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(it);
	}
}
