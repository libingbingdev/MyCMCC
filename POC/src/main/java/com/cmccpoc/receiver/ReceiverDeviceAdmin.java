package com.cmccpoc.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;


public class ReceiverDeviceAdmin extends DeviceAdminReceiver
{
	private static Handler mHandler;

	public static void setHandler(Handler handler) {
		mHandler = handler;
    }
	
    @Override
    public void onEnabled(Context context, Intent intent)
    {
        if(mHandler != null)
        {
	        Message msg = mHandler.obtainMessage();
	        msg.what = ReceiverExtSamsung.ADMIN_ENABLED;
	        mHandler.sendMessage(msg);
        }
    }
    
    @Override
    public void onDisabled(Context context, Intent intent)
    {
        if(mHandler != null)
        {
	        Message msg = mHandler.obtainMessage();
	        msg.what = ReceiverExtSamsung.ADMIN_DISABLED;
	        mHandler.sendMessage(msg);
        }
    }

}
