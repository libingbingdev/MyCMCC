package com.cmccpoc.listener;

import java.util.List;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;

/**
 * 监听消息相关操作
 * @author Yao
 */
public interface OnMmiMessageListener
{
	/**
	 * 接收到消息时
	 * @param isCustom 消息类型
	 * @param message 消息Entity
	 * @return
	 */
	public boolean onMessageIncomingRecv(boolean isCustom, AirMessage message);

	/**
	 * 接收到消息时
	 * @param messageList 消息列表
	 */
	public void onMessageIncomingRecv(List<AirMessage> messageList);

	/**
	 * 发送消息
	 * @param isCustom 消息类型
	 * @param message 消息Entity
	 * @param isSent 是否发送
	 */
	public void onMessageOutgoingSent(boolean isCustom, AirMessage message, boolean isSent);

	/**
	 * 消息更新
	 * @param message 消息Entity
	 */
	public void onMessageUpdated(AirMessage message);

	/**
	 * 开始录音
	 */
	public void onMessageRecordStart();

	/**
	 * 录音录制结束
	 * @param seconds 录音时长
	 * @param msgCode 消息code
	 */
	public void onMessageRecordStop(int seconds, String msgCode);

	/**
	 * 录音传输后
	 * @param msgCode 消息code
	 * @param resId 录音资源Id
	 */
	public void onMessageRecordTransfered(String msgCode, String resId);

	/**
	 * 录音加载中
	 * @param msgCode 消息code
	 * @param resId 录音资源Id
	 */
	public void onMessageRecordPlayLoading(String msgCode, String resId);

	/**
	 * 录音加载完成
	 * @param isOk
	 * @param msgCode 消息code
	 * @param resId 录音资源Id
	 */
	public void onMessageRecordPlayLoaded(boolean isOk, String msgCode, String resId);

	/**
	 * 开始播放录音
	 * @param msgCode 消息code
	 * @param resId 录音资源Id
	 */
	public void onMessageRecordPlayStart(String msgCode, String resId);

	/**
	 * 停止播放录音
	 * @param msgCode 消息code
	 * @param resId 录音资源Id
	 */
	public void onMessageRecordPlayStop(String msgCode, String resId);

	/**
	 * 录制PTT消息
	 * @param session 会话Entity
	 * @param message 消息Entity
	 * @param msgCode 消息code
	 * @param resId 录音资源Id
	 */
	public void onMessageRecordPtt(AirSession session, AirMessage message, String msgCode, String resId);
}
