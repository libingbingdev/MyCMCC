package com.cmccpoc.location;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import com.airtalkee.sdk.AirtalkeeReport;
import com.airtalkee.sdk.engine.AirTimer;
import com.airtalkee.sdk.engine.AirTimerWakeupListener;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.config.Config;
import com.cmccpoc.listener.OnMmiLocationListener;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.AirMmiTimer;
import com.cmccpoc.util.AirMmiTimerListener;

/**
 * 位置信息
 * 
 * @author Yao
 */
public class AirLocation implements OnMapListener, AirMmiTimerListener
{
	private final static String AIR_GPS_STATE = "AIR_GPS_STATE";
	private final static String AIR_GPS_FREQUENCE = "AIR_GPS_FREQUENCE";

	public final static int AIR_LOCATION_FRE_NAVIGATE = -1;
	public final static int AIR_LOCATION_FRE_MINUTE_1 = 60;
	public final static int AIR_LOCATION_FRE_MINUTE_5 = 5 * 60;
	public final static int AIR_LOCATION_FRE_MINUTE_15 = 15 * 60;
	public final static int AIR_LOCATION_FRE_MINUTE_30 = 30 * 60;
	public final static int AIR_LOCATION_FRE_MINUTE_60 = 60 * 60;

	public final static int AIR_LOCATION_ID_LOOP = 0;
	public final static int AIR_LOCATION_ID_ONCE = 1;

	public final static int AIR_LOCATION_CELL_TIME_GAP = 55; // Seconds
	public final static int AIR_LOCATION_CELL_TRY_TIME = 15;
	public final static int AIR_LOCATION_GPS_TIMEOUT = 40;

	private Context context = null;
	private OnMmiLocationListener listener = null;
	private static AirLocation mInstance = null;
	private static boolean isRunning = false;
	private int locationLoopTime = 0;

	private AirLocationImp locationOnce = new AirLocationImp();
	private AirLocationImp locationLoop = new AirLocationImp();

	public static LocationManager gAirmLocMan = null;

	public static AirLocation getInstance(Context context)
	{
		if (mInstance == null)
		{
			mInstance = new AirLocation(context);
		}
		return mInstance;
	}

	public AirLocation(Context context)
	{
		this.context = context;
	}

	// 获取系统GPS状态
	public boolean getSettingState()
	{
		return AirServices.iOperator.getBoolean(AirLocation.AIR_GPS_STATE, true);
	}

	// 获取回传频率
	public int getSettingFrequence()
	{
		int gpsFreq = AirServices.iOperator.getInt(AirLocation.AIR_GPS_FREQUENCE, AIR_LOCATION_FRE_MINUTE_5);
		switch (gpsFreq)
		{
			case AIR_LOCATION_FRE_NAVIGATE:
			case AIR_LOCATION_FRE_MINUTE_1:
			case AIR_LOCATION_FRE_MINUTE_5:
			case AIR_LOCATION_FRE_MINUTE_15:
			case AIR_LOCATION_FRE_MINUTE_30:
			case AIR_LOCATION_FRE_MINUTE_60:

				break;
			default:
				gpsFreq = AIR_LOCATION_FRE_MINUTE_5;
				break;
		}
		return gpsFreq;
	}

	public void setListener(OnMmiLocationListener listener, int id)
	{
		this.listener = listener;
		if (listener != null && id == AIR_LOCATION_ID_LOOP)
		{
			listener.onLocationChanged(true, AIR_LOCATION_ID_LOOP, locationLoop.getLocType(), locationLoop.getLocLatitude(), locationLoop.getLocLongitude(), locationLoop.getLocAltitude(), locationLoop.getLocSpeed(), locationLoop.getLocTime());
		}
	}

	// 设置默认频率
	public void setFrequenceDefault(int frequence, boolean isForce)
	{
		if (isForce)
		{
			AirServices.iOperator.putInt(AIR_GPS_FREQUENCE, frequence);
		}
		else
		{
			int gpsFreq = AirServices.iOperator.getInt(AirLocation.AIR_GPS_FREQUENCE, 0);
			if (gpsFreq == 0)
			{
				AirServices.iOperator.putInt(AIR_GPS_FREQUENCE, frequence);
			}
		}
	}

	// GPS是否激活
	public boolean GpsIsActive()
	{
		boolean isActive = false;
		LocationManager mLocMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (mLocMan.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
		{
			isActive = true;
		}
		mLocMan = null;
		return isActive;
	}

	// 激活GPS
	public void GpsActive()
	{
		Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		callGPSSettingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try
		{
			context.startActivity(callGPSSettingIntent);
		}
		catch (ActivityNotFoundException ex)
		{
			callGPSSettingIntent.setAction(Settings.ACTION_SETTINGS);
			try
			{
				context.startActivity(callGPSSettingIntent);
			}
			catch (Exception e)
			{}
		}
	}

	public double locationGetLatitude()
	{
		return locationLoop.getLocLatitude();
	}

	public double locationGetLongitude()
	{
		return locationLoop.getLocLongitude();
	}

	public String locationGetPoi()
	{
		return locationLoop.getLocPoi();
	}

	// =================================
	// LOOP
	// =================================

	private static long gLocationTs = 0;
	private AirTimerWakeupListener gLocationTimerWakeup = new AirTimerWakeupListener()
	{

		@Override
		public void onTimerWakeup()
		{
			// TODO Auto-generated method stub
			Log.i(AirLocation.class, "[Location] TimerWakeup check");
			if (System.currentTimeMillis() - gLocationTs >= locationLoopTime * 1000)
			{
				Log.i(AirLocation.class, "[Location] TimerWakeup do!");
				onMmiTimer(context, null);
				gLocationTs = System.currentTimeMillis();
			}
		}
	};

	public void loopCheck()
	{
		if (Config.funcCenterLocation == AirFunctionSetting.SETTING_ENABLE || Config.funcCenterLocation == AirFunctionSetting.SETTING_LOCATION_FORCE)
		{
			boolean gpsState = getSettingState();
			int gpsFreq = getSettingFrequence();
			if (Config.funcCenterLocation == AirFunctionSetting.SETTING_LOCATION_FORCE)
				gpsState = true;
			if (gpsState)
				loopRun(null, gpsFreq, false);
		}
	}

	public void loopRun(OnMmiLocationListener listener, int seconds, boolean changed)
	{
		if (changed)
		{
			loopTerminate();
		}
		if (!isRunning)
		{
			AirServices.iOperator.putBoolean(AIR_GPS_STATE, true);
			AirServices.iOperator.putInt(AIR_GPS_FREQUENCE, seconds);
			this.listener = listener;
			this.locationLoopTime = seconds;
			if (seconds == AIR_LOCATION_FRE_NAVIGATE)
			{
				seconds = 20;
			}
			else
			{
				AirMmiTimer.getInstance().TimerRegister(context, this, true, true, seconds * 1000, true, null);
				locationLoop.LocationGet(context, this, AIR_LOCATION_ID_LOOP, AIR_LOCATION_GPS_TIMEOUT, true);
			}
			isRunning = true;
		}
	}

	public void loopTerminate()
	{
		if (isRunning)
		{
			locationLoop.LocationTerminate(context);
			AirMmiTimer.getInstance().TimerUnregister(context, this);

			AirServices.iOperator.putBoolean(AIR_GPS_STATE, false);
			locationLoopTime = 0;
			isRunning = false;
		}
	}

	@Override
	public void onMmiTimer(Context context, Object userData)
	{
		if (isRunning)
		{
			// locationLoop.LocationGet(context, this, AIR_LOCATION_ID_LOOP,
			// locationLoopTime - AIR_LOCATION_CELL_TRY_TIME, true);
			locationLoop.LocationGet(context, this, AIR_LOCATION_ID_LOOP, AIR_LOCATION_GPS_TIMEOUT, true);
		}
	}

	// =================================
	// ONCE
	// =================================

	public void onceGet(OnMmiLocationListener listener, int timeoutSeconds)
	{
		this.listener = listener;
		locationOnce.LocationGet(context, this, AIR_LOCATION_ID_ONCE, timeoutSeconds, false);
	}

	// =================================
	// Event
	// =================================

	@Override
	public void OnMapLocation(boolean isOk, int id, int type, boolean isFinal, double latitude, double longitude, double altitude, float direction, float speed, String time, String address)
	{
		// TODO Auto-generated method stub
		if (isOk && isFinal && id == AIR_LOCATION_ID_LOOP)
		{
			if (type == AirLocationImp.LOCATION_TYPE_GPS)
			{
				AirtalkeeReport.getInstance().ReportLocation(AirtalkeeReport.LOCATION_TYPE_GPS, latitude, longitude, altitude, direction, speed);
			}
			else
			{
				AirtalkeeReport.getInstance().ReportLocation(AirtalkeeReport.LOCATION_TYPE_CELL, latitude, longitude, altitude, direction, speed);
			}
		}

		if (listener != null)
		{
			Log.d(AirLocation.class, "[LOCATION] AirLocation addr = " + address + ", time = " + time);
			listener.onLocationChanged(isOk, id, type, latitude, longitude, altitude, speed, time, address);
		}
	}

}
