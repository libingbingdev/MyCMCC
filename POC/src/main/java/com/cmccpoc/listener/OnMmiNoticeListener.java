package com.cmccpoc.listener;

/**
 * 监听通知
 * @author Yao
 */
public interface OnMmiNoticeListener
{
	/**
	 * 有新通知时
	 * @param number 通知数
	 */
	public void onMmiNoticeNew(int number);
}
