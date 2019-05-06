package com.cmccpoc.application;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.support.multidex.MultiDex;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.util.IOoperate;
import com.airtalkee.sdk.util.Log;
import com.baidu.mapapi.SDKInitializer;
import com.cmccpoc.R;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirLocationShareControl;
import com.cmccpoc.services.PttKeyServices;
import com.cmccpoc.util.Util;
import com.cmccpoc.util.XmlModelReader;

import cn.richinfo.mt.MTSdk;
import cn.richinfo.mt.util.MobileUtil;

/**
 * 主程序类 处理第一次程序开启时需要启动或需要初始化的功能
 * @author Yao
 */
public class MainApplication extends Application
{
	private static boolean firstLaunch = true;
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		AirtalkeeAccount.getInstance().AirTalkeeConfigTrace(Config.TRACE_MODE);
		Log.d(MainApplication.class, "Weptt Application Start!");
		IOoperate.setContext(this.getApplicationContext());
		new XmlModelReader(this.getApplicationContext()).inflate(R.menu.ptt_config);
		Config.marketConfig(this.getApplicationContext());
		AppExcepiton.getInstance().init(this.getApplicationContext());
		PttKeyServices.startServices(this.getApplicationContext());
		firstLaunch = setFirstLaunch(new IOoperate().getBoolean("firstLaunch", true));
		// M zlm 多终端集成
		String imsi = MobileUtil.getIMSI(this);
		String imei = MobileUtil.getDeviceID(this);
		android.util.Log.d("zzlm","imei"+imei  +"imsi="+imsi);
		MTSdk.init(this, imei, null ,  imsi , "M100000035");
		//SDKInitializer.initialize(this.getApplicationContext());
	}

	@Override
	public void onLowMemory()
	{
		// TODO Auto-generated method stub
		super.onLowMemory();
		Log.w(MainApplication.class, "-----------------------------------------------------------");
		Log.w(MainApplication.class, "   --------- Weptt Application --LowMemory----------");
		Log.w(MainApplication.class, "-----------------------------------------------------------");
	}

	@Override
	public void onTerminate()
	{
		// TODO Auto-generated method stub
		super.onTerminate();
		Log.e(MainApplication.class, "Weptt Application Stop!");
	}
	
	/**
	 * 设置是否是第一次运行程序
	 * @param b
	 * @return
	 */
	public static boolean  setFirstLaunch(boolean b)
	{
		new IOoperate().putBoolean("firstLaunch", b);
		firstLaunch = b;
		return firstLaunch;
	}

	public static void appExit()
	{
		AirLocationShareControl.getInstance().cleanAllShare();
	}
	
	/**
	 * 返回是否是第一次运行程序
	 * @return
	 */
	public static boolean isFisrtLaunch()
	{
		return firstLaunch;
	}

	@Override
	protected void attachBaseContext(Context base)
	{
		super.attachBaseContext(base);
		MultiDex.install(base);
	}
}
