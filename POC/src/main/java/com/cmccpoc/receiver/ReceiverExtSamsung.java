package com.cmccpoc.receiver;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.app.enterprise.ApplicationPolicy;
import android.app.enterprise.EnterpriseDeviceManager;
import android.app.enterprise.license.EnterpriseLicenseManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.config.Config;

public class ReceiverExtSamsung extends BroadcastReceiver
{
	private static Context mContext = null;
	
	private static boolean isActived = false;
	
    public final static int ADMIN_ENABLED = 1;
    public final static int ADMIN_DISABLED = 2;
	
	private static String PKG_NAME = null;
	private static final String PKG_LICENSE = "8985081D6440A434D0EA1FFB816F4BF7C2C98BE8E637C0A721061479451570FDF0109A17906CEBF5FE34742AF4013F3474898ABE6E3B9420228153595FC5E30F";
	
	public static void activePolicy(Activity activity)
	{
		if (Config.model.startsWith("SM-"))
		{
			mContext = activity;
			PKG_NAME = mContext.getPackageName();
	        isActived = true;
	        DevicePolicyManager mDPM = (DevicePolicyManager) activity.getSystemService(Activity.DEVICE_POLICY_SERVICE);
			ComponentName mCN = new ComponentName(activity, ReceiverDeviceAdmin.class);
	        if (mDPM != null && !mDPM.isAdminActive(mCN))
	        {
	        	ReceiverDeviceAdmin.setHandler(mHandler);
	            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
	            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mCN);
	            activity.startActivity(intent);
	        }

		}
	}
	
	private static Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (isActived)
			{
				if (msg.what == ADMIN_ENABLED)
				{
					try
					{
						EnterpriseLicenseManager elmManager = EnterpriseLicenseManager.getInstance(mContext);
						elmManager.activateLicense(PKG_LICENSE, PKG_NAME);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else if (msg.what == ADMIN_DISABLED)
				{
					
				}
			}
		}
	};

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		Log.d(ReceiverExtSamsung.class, "ReceiverExtSamsung onReceive");
		if (isActived && intent.getAction().equals(EnterpriseLicenseManager.ACTION_LICENSE_STATUS))
		{
			if (intent.hasExtra(EnterpriseLicenseManager.EXTRA_LICENSE_STATUS))
			{
				String status = intent.getStringExtra(EnterpriseLicenseManager.EXTRA_LICENSE_STATUS);
				if ("success".equals(status))
				{
					Log.d(ReceiverExtSamsung.class, "ReceiverExtSamsung onReceive: status=" + status);
					EnterpriseDeviceManager edm = (EnterpriseDeviceManager) context.getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);
					ApplicationPolicy appPolicy = edm.getApplicationPolicy();
					List<String> list = new ArrayList<String>();
					list.add(PKG_NAME);
					try {
					     boolean result = appPolicy.addPackagesToForceStopBlackList(list);
					     if (true == result) {
					         // user is not allowed to stop the blacklisted application packages
					     } else {
					         // previous behaviour for device still prevails
					     }
					} catch (SecurityException e) {
					     e.printStackTrace();
					}
				}
			}
		}
	}
}
