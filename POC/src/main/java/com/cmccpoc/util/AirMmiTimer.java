package com.cmccpoc.util;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import com.airtalkee.sdk.engine.AirPower;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.services.AirServices;

/**
 * 计时器
 * 
 * @author Yao
 */
public class AirMmiTimer extends BroadcastReceiver
{
	public class TimerItem
	{
		public int id = 0;
		public PendingIntent intent = null;
		public AirMmiTimerListener listener = null;
		public Object userData = null;
		public boolean isAlarm = false;
		public boolean isWakeup = false;
		public boolean isLoop = false;
		public int timeout = 0;

		public TimerItem()
		{
		}
	}

	private static AirMmiTimer mInstance = new AirMmiTimer();

	/**
	 * 获取AirMmiTimer实例
	 * 
	 * @return
	 */
	public static AirMmiTimer getInstance()
	{
		return mInstance;
	}

	public AirMmiTimer()
	{
	}

	private static HashMap<AirMmiTimerListener, TimerItem> timerPoolByListener = new HashMap<AirMmiTimerListener, TimerItem>();
	private static HashMap<String, TimerItem> timerPoolById = new HashMap<String, TimerItem>();
	private static int timerFlag = 0;
	private static int timerIndex = 0;

	private Timer mTimer;
	private TimerTask mTimerTask;
	private int seconds;

	/**
	 * 注册Timer计时器
	 * 
	 * @param context
	 * @param listener
	 * @param isAlarm
	 *            是否告警
	 * @param isWakeup
	 *            是否唤醒
	 * @param timeout
	 *            超时时长
	 * @param loop
	 *            是否循环
	 * @param userData
	 *            user数据
	 */
	public void TimerRegister(final Context context, final AirMmiTimerListener listener, boolean isAlarm, boolean isWakeup, final int timeout, boolean loop, Object userData)
	{
		if (listener != null)
		{
			TimerUnregister(context, listener);
			try
			{
				timerIndex++;

				TimerItem item = new TimerItem();
				item.id = timerIndex;
				item.intent = null;
				item.listener = listener;
				item.userData = userData;
				item.timeout = timeout;
				item.isLoop = loop;
				item.isAlarm = isAlarm;
				item.isWakeup = isWakeup;
				timerPoolByListener.put(listener, item);
				timerPoolById.put(timerIndex + "", item);

				Message msg = new Message();
				msg.what = timerIndex;
				msg.obj = item;
				timeOutHandler.sendMessageDelayed(msg, timeout);
				Log.i(AirMmiTimer.class, "[MMI-TIMER][" + listener.toString() + "] TimerRegister id=" + timerIndex + " flag=" + timerFlag + " timeout=" + timeout + " loop=" + loop);
			}
			catch (Exception e)
			{}
		}
	}

	/**
	 * 取消注册Timer
	 * 
	 * @param context
	 * @param listener
	 * @return
	 */
	public Object TimerUnregister(Context context, AirMmiTimerListener listener)
	{
		Object userObject = null;
		try
		{
			if (listener != null)
			{
				TimerItem item = timerPoolByListener.get(listener);
				if (item != null)
				{
					int id = item.id;
					boolean isWakeup = false;
					userObject = item.userData;
					isWakeup = item.isWakeup;
					timeOutHandler.removeMessages(item.id);
					Log.i(AirMmiTimer.class, "[MMI-TIMER][" + listener.toString() + "] TimerUnregister id=" + id);
					timerPoolByListener.remove(listener);
					timerPoolById.remove(id + "");
				}
			}
		}
		catch (Exception e)
		{}
		return userObject;
	}

	public Handler timeOutHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			try
			{
				TimerItem item = (TimerItem) msg.obj;
				if (item != null)
				{
					if (timerPoolById.get(item.id + "") != null)
					{
						Log.i(AirMmiTimer.class, "[MMI-TIMER][" + item.listener.toString() + "] Timeout! id=" + item.id);
						item.listener.onMmiTimer(AirServices.getInstance(), item.userData);
						if (item.isLoop)
						{
							Message m = new Message();
							m.what = timerIndex;
							m.obj = item;
							timeOutHandler.sendMessageDelayed(m, item.timeout);
						}
						else
						{
							timerPoolByListener.remove(item.listener);
							timerPoolById.remove(item.id + "");
						}
					}
				}
			}
			catch (Exception e)
			{

			}
		}
	};

	@Override
	public void onReceive(Context context, Intent intent)
	{
	}

}
