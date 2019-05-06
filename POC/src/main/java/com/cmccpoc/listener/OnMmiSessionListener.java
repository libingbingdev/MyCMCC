package com.cmccpoc.listener;

import java.util.List;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;

/**
 * 监听会话
 * @author Yao
 */
public interface OnMmiSessionListener
{
	/**
	 * 临时会话响铃
	 * @param session 会话Entity
	 */
	public void onSessionOutgoingRinging(AirSession session);

	/**
	 * 会话正在建立时
	 * @param session 会话Entity
	 */
	public void onSessionEstablishing(AirSession session);

	/**
	 * 会话已经建立
	 * @param session 会话Entity
	 * @param isOk 建立成功状态
	 */
	public void onSessionEstablished(AirSession session, int result);

	/**
	 * 会话释放后
	 * @param session 会话Entity
	 * @param reason 会话释放原因
	 */
	public void onSessionReleased(AirSession session, int reason);

	/**
	 * 会话连通时
	 * @param session 会话Entity
	 * @param membersAll 全体成员列表
	 * @param membersPresence 当前成员
	 */
	public void onSessionPresence(AirSession session, final List<AirContact> membersAll, final List<AirContact> membersPresence);

	/**
	 * 会话成员更新时
	 * @param session 会话Entity
	 * @param members 会话成员列表
	 * @param isOk 更新状态
	 */
	public void onSessionMemberUpdate(AirSession session, List<AirContact> members, boolean isOk);

}
