package com.cmccpoc.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * 宽度高度 工具类
 * @author Yao
 */
public class DensityUtil
{
	/**
	 * dip 转 px
	 * @param context 上下文
	 * @param dipValue dip值
	 * @return
	 */
	public static int dip2px(Context context, float dipValue)
	{
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * px 转 dip
	 * @param context 上下文
	 * @param pxValue px值
	 * @return
	 */
	public static int px2dip(Context context, float pxValue)
	{
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 获取屏幕宽度
	 * @param context
	 * @return
	 */
	public static int getWidth(Context context)
	{
		initScreen(context);
		if (screen != null)
		{
			return screen.widthPixels;
		}
		return 0;
	}

	/**
	 * 获取屏幕高度
	 * @param context
	 * @return
	 */
	public static int getHeight(Context context)
	{
		initScreen(context);
		if (screen != null)
		{
			return screen.heightPixels;
		}
		return 0;
	}

	/**
	 * 初始化屏幕
	 * @param context
	 */
	public static void initScreen(Context context)
	{
		if (screen == null)
		{
			DisplayMetrics dm = new DisplayMetrics();
			WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			windowManager.getDefaultDisplay().getMetrics(dm);
			screen = new Screen(dm.widthPixels, dm.heightPixels);
		}
	}

	/**
	 * 获取状态栏高度
	 * @param context
	 * @return
	 */
	public static int getStatusHeight(Context context)
	{
		if (STATUS_HEIGHT <= 0)
		{
			try
			{
				int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
				if (resourceId > 0)
				{
					STATUS_HEIGHT = context.getResources().getDimensionPixelSize(resourceId);
				}
			}
			catch (Exception ex)
			{
			}
		}
		return STATUS_HEIGHT;
	}

	private static Screen screen = null;
	private static int STATUS_HEIGHT = 0;

	public static class Screen
	{
		public int widthPixels;
		public int heightPixels;

		public Screen()
		{}

		public Screen(int widthPixels, int heightPixels)
		{
			this.widthPixels = widthPixels;
			this.heightPixels = heightPixels;
		}

		@Override
		public String toString()
		{
			return "(" + widthPixels + "," + heightPixels + ")";
		}

	}
}
