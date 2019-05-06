package com.cmccpoc.util;

import android.content.Context;

/**
 * 计时器
 * @author Yao
 */
public interface AirMmiTimerListener
{
	/**
	 * 计时器 
	 * @param context 上下文
	 * @param userData 用户数据
	 */
	public void onMmiTimer(Context context, Object userData);
}
