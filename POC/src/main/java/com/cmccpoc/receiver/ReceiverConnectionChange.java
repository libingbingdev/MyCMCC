package com.cmccpoc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.control.AirAccountManager;
import com.cmccpoc.control.AirSessionMediaSound;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.AirMmiTimer;
import com.cmccpoc.util.AirMmiTimerListener;
//import com.hdqy.android.telephony.HdqyInfoManager;

/**
 * 接收网络连接广播
 * @author Yao
 */
public class ReceiverConnectionChange extends BroadcastReceiver
{
	public static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	public static NetworkInfo activeNetInfo;
	private ConnectivityManager connectivityManager = null;
	private int ConnectionType = -1;
	private AirtalkeeAccount handleAccount = AirtalkeeAccount.getInstance();

	public void onReceive(Context context, Intent intent)
	{
		android.util.Log.e("m", "ReceiverConnectionChange  onReceive!!  Action = " + intent.getAction());
		if (intent.getAction().equals(ACTION))
		{
			 // M zlm 多终端集成
			sendNetChangeIntent(context);
			//政企终端接口
			sendBrodCast(context);

			if (connectivityManager == null)
				connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			activeNetInfo = connectivityManager.getActiveNetworkInfo();
			if (activeNetInfo != null)
			{
				if (activeNetInfo.getState() != NetworkInfo.State.CONNECTED)
				{
					Log.i(ReceiverConnectionChange.class, "ConnectionChangeReceiver  DISCONNECTED");
					networkClose();
				}
				else
				{
					Log.i(ReceiverConnectionChange.class, "ConnectionChangeReceiver  CONNECTED");
					if (ConnectionType != -1 && ConnectionType != activeNetInfo.getType())
					{
						Log.i(ReceiverConnectionChange.class, "ConnectionType changed!!!!");
						networkClose();
					}
					networkOpen();
					ConnectionType = activeNetInfo.getType();
				}
			}
			else
			{
				Log.i(ReceiverConnectionChange.class, "ConnectionChangeReceiver  activeNetInfo == null !!");
				networkClose();
			}
		}
	}

	// M zlm 多终端集成
	private void sendNetChangeIntent(Context context){
		android.util.Log.d("zzlm","context.getPackageName()="+context.getPackageName());
		Intent intent =new Intent();
		intent.setAction("android.richinfo.net.conn.CONNECTIVITY_CHANGE");
		intent.setPackage(context.getPackageName());
		context.sendBroadcast(intent);

	}

	//政企终端接口
	private void sendBrodCast(Context context){
		android.util.Log.d("zlmm","sendBrodCast.ACTION_CONNECTIVITY_CHANGE");
		//Intent intent =new Intent(HdqyInfoManager.ACTION_CONNECTIVITY_CHANGE);
		//context.sendBroadcast(intent);
	}

	// 网络打开
	private void networkOpen()
	{
		String userId = AirServices.iOperator.getString(AirAccountManager.KEY_ID, "");
		String userPwd = AirServices.iOperator.getString(AirAccountManager.KEY_PWD, "");
		Log.e(ReceiverConnectionChange.class, String.format("userId =[%s],userPwd =[%s]", userId, userPwd));
		handleAccount.setUserIdAndPwd(userId, userPwd);
		handleAccount.NetworkOpen();
	
	}

	// 网络关闭
	private void networkClose()
	{
		handleAccount.NetworkClose();
		try
		{
			AirSessionMediaSound.destoryState();
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

}