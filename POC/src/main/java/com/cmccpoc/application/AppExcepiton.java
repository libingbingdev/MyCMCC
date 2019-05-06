package com.cmccpoc.application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.lang.Thread.UncaughtExceptionHandler;
import android.content.Context;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

/**
 * poc异常处理类
 * @author Yao
 */
public class AppExcepiton implements UncaughtExceptionHandler
{

	// private Context mContext;
	private Thread.UncaughtExceptionHandler defaultExceptionHandler;
	private static AppExcepiton appException;

	private AppExcepiton()
	{}

	public static AppExcepiton getInstance()
	{
		if (appException == null)
		{
			appException = new AppExcepiton();
		}
		return appException;
	}

	/**
	 * 抛出异常
	 */
	@SuppressWarnings("finally")
	@Override
	public void uncaughtException(Thread thread, Throwable ex)
	{
		// TODO Auto-generated method stub
		try
		{
			StackTraceElement[] stack = ex.getStackTrace();// ex.getCause().getStackTrace();
			String path = null;
			if (defaultExceptionHandler != null)
			{
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state))
				{
					path = Environment.getExternalStorageDirectory().getPath();
				}
				path = path + "/AirTalkee/log";
				File file = new File(path);
				if (!file.exists())
				{
					file.mkdir();
				}
				deleteOldFile(path);
				String time = getCurrentTime();
				String fileName = time.substring(0, 9);
				File myFile = new File(path + "/" + fileName + ".log");

				String str = "\n" + time + "-->";
				FileOutputStream fos = new FileOutputStream(myFile, true);
				fos.write(str.getBytes());
				for (int i = 0; i < stack.length; i++)
				{
					fos.write(stack[i].toString().getBytes());
				}
				String exception = "Exception=[" + ex.toString() + "]";
				fos.write(exception.getBytes());
				fos.flush();
				fos.close();
			}
			Log.e("m", "exception" + ex.toString());
		}
		catch (Exception e)
		{
			Log.e("m", "exception" + ex.toString());
		}
		finally
		{
			defaultExceptionHandler.uncaughtException(thread, ex);
		}

	}

	/**
	 * 获取当前时间
	 * @return
	 */
	public String getCurrentTime()
	{
		Time t = new Time();
		t.setToNow();
		int year = t.year;
		int month = t.month + 1;
		int day = t.monthDay;
		int hour = t.hour;
		int minute = t.minute;
		int second = t.second;
		String time = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
		return time;

	}

	public void init(Context context)
	{
		// mContext = context;
		defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 删除旧文件
	 * @param path 文件路径
	 */
	public void deleteOldFile(final String path)
	{
		File file = new File(path);
		file.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String filename)
			{
				// TODO Auto-generated method stub
				File file = new File(path + "/" + filename);
				Long ago = file.lastModified();
				Long now = System.currentTimeMillis();
				if ((now - ago) > 31536000)
				{
					file.delete();
				}
				return false;
			}
		});

	}

}
