package com.cmccpoc.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.SearchView;
import android.widget.TextView;
import com.airtalkee.sdk.util.IOoperate;
import com.cmccpoc.config.Config;

/**
 * app主题类
 * @author Yao
 */
public class ThemeUtil
{
	static String key_theme = "key_theme";
	static IOoperate io;
	static int currentTheme = -1;

	/**
	 * 设置主题
	 * @param ac
	 */
	public static void setTheme(Activity ac)
	{
		int theme = io.getInt(key_theme, -1);
		if (theme != -1)
		{
			currentTheme = theme;
		}
		else
		{
			currentTheme = Config.defaultTheme;
		}
		ac.setTheme(currentTheme);
	}

	/**
	 * 更改主题
	 * @param ac
	 */
	public static void changeTheme(Activity ac)
	{
		currentTheme = Config.defaultTheme;
		io.putInt(key_theme, currentTheme);

		if (ac != null)
		{
			ac.finish();
			Intent it = new Intent(ac, ac.getClass());
			ac.startActivity(it);
		}
	}

	static
	{
		io = new IOoperate();
	}

	/**
	 * 获取颜色
	 * @param context 上下文
	 * @param attr 属性
	 * @return
	 */
	public static int getColor(Context context, int attr)
	{
		int[] attrs = new int[] { attr };
		TypedArray ta = context.obtainStyledAttributes(attrs);
		int color = ta.getColor(0, 430);
		ta.recycle();
		return color;
	}

	/**
	 * 获取Drawable
	 * @param attr 属性
	 * @param context 上下文
	 * @return
	 */
	public static Drawable getDrawable(int attr, Context context)
	{
		int[] attrs = new int[] { attr };
		TypedArray ta = context.obtainStyledAttributes(attrs);
		Drawable drawable = ta.getDrawable(0);
		ta.recycle();
		return drawable;

	}

	/**
	 * 获取资源Id
	 * @param attr 属性
	 * @param context 上下文
	 * @return
	 */
	public static int getResourceId(int attr, Context context)
	{
		int[] attrs = new int[] { attr };
		TypedArray ta = context.obtainStyledAttributes(attrs);
		int id = ta.getResourceId(0, 430);
		ta.recycle();
		return id;
	}

	/**
	 * 获取尺寸大小
	 * @param activity 界面对象
	 * @param attr 属性
	 * @param defaultValue 默认值
	 * @return
	 */
	public static int getDimensionPixelSize(Activity activity, int attr, int defaultValue)
	{
		int[] attrs = new int[] { attr };
		TypedArray ta = activity.obtainStyledAttributes(attrs);
		int value = ta.getDimensionPixelSize(0, defaultValue);
		ta.recycle();
		return value;
	}

	
	/**
	 * can't find a public theme attr to modify actionbar searchview text color
	 * @param searchView serchView
	 */
	public static void customActionBarSearchViewTextColor(SearchView searchView)
	{
		int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
		TextView textView = (TextView) searchView.findViewById(id);
		textView.setTextColor(Color.WHITE);
	}

}
