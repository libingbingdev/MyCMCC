package com.cmccpoc.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.text.TextUtils;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.config.Config;
import com.cmccpoc.receiver.ReceiverMediaButton;

public class PttKeyServices extends Service
{
	public static  String ACTION = "com.cmccpoc.services.PttKeyServices";
	ReceiverMediaButton mediaButton;
	public static void startServices(Context context)
	{
		if(context != null)
		{
			Log.d(PttKeyServices.class, "start PttKeyServices");
			if(!TextUtils.isEmpty(Config.pttButtonAction) || !Utils.isEmpty(Config.pttButtonActionUp) && !Utils.isEmpty(Config.pttButtonActionDown))
			{
				Log.d(PttKeyServices.class, "this mobile ("+android.os.Build.MODEL+") has register ptt key");
				context.startService(new Intent(PttKeyServices.ACTION));
			}
			else
			{
				Log.d(PttKeyServices.class, "this mobile ("+android.os.Build.MODEL+") has not register ptt key");
			}
		}
	}
	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d(PttKeyServices.class, "PttKeyServices onCreate");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// TODO Auto-generated method stub
		Log.d(PttKeyServices.class, "Weptt PttKeyServices onStartCommand");
		resisterPTTKey();
		return START_STICKY;
	}
	
	public void resisterPTTKey()
	{
		Log.i(PttKeyServices.class, "Weptt resisterPTTKey begin");
		mediaButton = new ReceiverMediaButton();
		
		// Register ACTION_HEADSET_PLUG
		IntentFilter intentHeadset = new IntentFilter();
		intentHeadset.addAction(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(mediaButton, intentHeadset);
		
		// Register ACTION_MEDIA_BUTTON
		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		ComponentName comp = new ComponentName(getPackageName(), ReceiverMediaButton.class.getName());
		try
		{
			mAudioManager.registerMediaButtonEventReceiver(comp);
		}
		catch (NoSuchMethodError e)
		{
			
		}
		
		IntentFilter intentMediaBtn = new IntentFilter();
		intentMediaBtn.addAction(Intent.ACTION_MEDIA_BUTTON);
		registerReceiver(mediaButton, intentMediaBtn);

		// Register PTT
		if (!TextUtils.isEmpty(Config.pttButtonAction))
		{
			IntentFilter intentPttBtn = new IntentFilter();
			intentPttBtn.addAction(Config.pttButtonAction);
			registerReceiver(mediaButton, intentPttBtn);
		}
		if (!Utils.isEmpty(Config.pttButtonActionUp) && !Utils.isEmpty(Config.pttButtonActionDown))
		{
			IntentFilter intentPttBtnUp = new IntentFilter();
			intentPttBtnUp.addAction(Config.pttButtonActionUp);
			registerReceiver(mediaButton, intentPttBtnUp);

			IntentFilter intentPttBtnDown = new IntentFilter();
			intentPttBtnDown.addAction(Config.pttButtonActionDown);
			registerReceiver(mediaButton, intentPttBtnDown);
		}
		Log.i(PttKeyServices.class, "Weptt resisterPTTKey end");
	}

}
