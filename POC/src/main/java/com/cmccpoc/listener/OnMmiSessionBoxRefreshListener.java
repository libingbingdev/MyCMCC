package com.cmccpoc.listener;

import com.airtalkee.sdk.entity.AirSession;

/**
 * 监听会话状态
 * @author Yao
 */
public interface OnMmiSessionBoxRefreshListener
{
	/**
	 * 会话刷新时
	 * @param session 会话Entity
	 */
	public void onMmiSessionRefresh(AirSession session);

	/**
	 * 会话建立后
	 * @param session 会话Entity
	 */
	public void onMmiSessionEstablished(AirSession session);

	/**
	 * 会话释放后
	 * @param session 会话Entity
	 */
	public void onMmiSessionReleased(AirSession session);
}
