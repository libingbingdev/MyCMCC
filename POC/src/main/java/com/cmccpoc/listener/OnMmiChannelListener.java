package com.cmccpoc.listener;

import java.util.LinkedHashMap;
import java.util.List;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;

/**
 * 监听频道相关操作
 * @author Yao
 */
public interface OnMmiChannelListener
{
	/**
	 * 获取频道列表时触发
	 * @param isOk  是否获取完成
	 * @param channels  频道列表
	 */
	public void onChannelListGet(boolean isOk, final List<AirChannel> channels);

	/**
	 * 获取频道成员时触发
	 * @param channelId  频道Id
	 * @param members  频道成员列表
	 */
	public void onChannelMemberListGet(String channelId, final List<AirContact> members);

	/**
	 * 获取频道在线人数
	 * @param online 在线成员map
	 */
	public void onChannelOnlineCount(final LinkedHashMap<String, Integer> online);

	/**
	 * 被加入新频道时触发
	 * @param ch 频道entity
	 */
	public void onChannelPersonalCreateNotify(AirChannel ch);

	/**
	 * 从频道中删除时
	 * @param ch 频道entity
	 */
	public void onChannelPersonalDeleteNotify(AirChannel ch);
	
	/**
	 * 新成员被加入到群组
	 * @param ch
	 */
	public void onChannelMemberAppendNotify(AirChannel ch, List<AirContact> members);

	/**
	 * 从频道中删除成员
	 * @param ch
	 */
	public void onChannelMemberDeleteNotify(AirChannel ch, List<AirContact> members);
	
	/**
	 * 更新频道成员信息
	 * @param ch
	 */
	public void onChannelMemberUpdateNotify(AirChannel ch, List<AirContact> members);

}
