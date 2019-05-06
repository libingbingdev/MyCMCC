package com.cmccpoc.control;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.WindowManager;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnMessageListener;
import com.airtalkee.sdk.OnMessagePttListener;
import com.airtalkee.sdk.OnSystemBroadcastListener;
import com.airtalkee.sdk.OnSystemFenceWarningListener;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.controller.AirTaskController;
import com.airtalkee.sdk.controller.AirTaskController$AirTaskListener;
import com.airtalkee.sdk.controller.AirTaskController$AirTaskPushListener;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirFenceWarning;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.entity.AirTask;
import com.airtalkee.sdk.entity.AirTaskDetail;
import com.airtalkee.sdk.entity.AirTaskReport;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.activity.AccountActivity;
import com.cmccpoc.activity.MenuNoticeActivity;
import com.cmccpoc.activity.MoreActivity;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.activity.home.widget.CallAlertDialog;
import com.cmccpoc.activity.home.widget.CallAlertDialog.OnAlertDialogCancelListener;
import com.cmccpoc.activity.home.widget.SessionAndChannelView;
import com.cmccpoc.activity.home.widget.StatusBarTitle;
import com.cmccpoc.activity.home.widget.ToastUtils;
import com.cmccpoc.config.Config;
import com.cmccpoc.listener.OnMmiMessageListener;
import com.cmccpoc.listener.OnMmiNoticeListener;
import com.cmccpoc.receiver.ReceiverNotification;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.Sound;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.Util;

/**
 * 消息业务处理类
 * @author Yao
 */
public class AirMessageTransaction implements OnMessageListener,
		OnMessagePttListener, OnSystemBroadcastListener,
		OnSystemFenceWarningListener, AirTaskController$AirTaskListener, DialogListener, AirTaskController$AirTaskPushListener
{
	private static final int DIALOG_2_SEND_MESSAGE = 101;
	private static final int DIALOG_CALL_CENTER = 100;
	private static final int DIALOG_CALL = 102;

	private static AirMessageTransaction mInstance = null;
	private OnMmiMessageListener msgListener = null;
	private OnMmiNoticeListener noticeListener = null;
	AlertDialog dialog;
	private CallAlertDialog alertDialog;

	private AirMessageTransaction()
	{
		AirtalkeeMessage.getInstance().setOnMessageListener(this);
		AirtalkeeMessage.getInstance().setOnMessagePttListener(this);
		AirtalkeeAccount.getInstance().setOnSystemBroadcastListener(this);
		AirtalkeeAccount.getInstance().setOnSystemFenceWarningListener(this);
	}

	public static AirMessageTransaction getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new AirMessageTransaction();
		}
		return mInstance;
	}

	public void setOnMessageListener(OnMmiMessageListener l)
	{
		this.msgListener = l;
	}

	public void setOnNoticeListener(OnMmiNoticeListener l)
	{
		this.noticeListener = l;
	}

	/**
	 * 接收到消息时
	 * @param messageList 消息列表
	 */
	@Override
	public void onMessageIncomingRecv(List<AirMessage> messageList)
	{
		if (msgListener != null)
			msgListener.onMessageIncomingRecv(messageList);
	}

	/**
	 * 接收到消息时
	 * @param isCustom 消息类型
	 * @param message 消息Entity
	 * @return
	 */
	@Override
	public void onMessageIncomingRecv(boolean isCustom, AirMessage message)
	{
		try
		{
			Log.i(AirMessageTransaction.class, "AirMessageTransaction onMessageIncomingRecv");
			Context ct = AirServices.getInstance();
			String from = "";
			String typeText = "";
			String msg = "";
			if (message != null)
			{
				from = message.getInameFrom();
				switch (message.getType())
				{
					case AirMessage.TYPE_PICTURE:
						typeText = Config.app_name + ct.getString(R.string.talk_msg_pic);
						msg = typeText;
						break;
					case AirMessage.TYPE_TEXT:
						typeText = Config.app_name + ct.getString(R.string.talk_msg_text);
						msg = message.getBody();
						break;
					case AirMessage.TYPE_RECORD: 
						AirtalkeeMessage.getInstance().MessageRecordPlayDownload(message);
						typeText = Config.app_name + ct.getString(R.string.talk_msg_rec);
						msg = typeText;
						break;
					case AirMessage.TYPE_SYSTEM:
						typeText = message.getBody();
						msg = typeText;
						break;
					case AirMessage.TYPE_CHANNEL_ALERT:
						typeText = ct.getString(R.string.talk_incoming_channel_alert_message);
						msg = typeText;
						break;
				}
			}

			boolean isHandled = false;
			if (msgListener != null)
				isHandled = msgListener.onMessageIncomingRecv(isCustom, message);
			if (!isHandled && message.getSessionCode() != null)
			{
				Intent realIntent = new Intent();
				realIntent.setClass(ct, HomeActivity.class);
				realIntent.putExtra("tag", "onMessageIncomingRecv");
				realIntent.putExtra("sessionCode", message.getSessionCode());
				realIntent.putExtra("type", AirServices.TEMP_SESSION_TYPE_MESSAGE);
				realIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Intent clickIntent = new Intent(ct, ReceiverNotification.class);
			    clickIntent.putExtra("realIntent", realIntent);  
				Util.showNotification(Util.NOTIFI_ID_MESSAGE, ct, clickIntent, from, typeText, msg, null);
				Sound.playSound(Sound.PLAYER_NEWINFO, false, ct);
				if (SessionAndChannelView.getInstance() != null)
				{
					SessionAndChannelView.getInstance().refreshChannelAndDialog();
					SessionAndChannelView.getInstance().resume();
				}
				if (HomeActivity.getInstance() != null)
					HomeActivity.getInstance().checkNewIM(false, message.getSession());
				if (StatusBarTitle.getInstance() != null)
					StatusBarTitle.getInstance().refreshNewMsg();
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	/**
	 * 发送消息
	 * @param isCustom 消息类型
	 * @param message 消息Entity
	 * @param isSent 是否发送
	 */
	@Override
	public void onMessageOutgoingSent(boolean isCustom, AirMessage message, boolean isSent)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageOutgoingSent(isCustom, message, isSent);
	}

	/**
	 * 消息更新
	 * @param message 消息Entity
	 */
	@Override
	public void onMessageUpdated(AirMessage message)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageUpdated(message);
	}

	/**
	 * 录音加载中
	 * @param msgCode 消息code
	 * @param resId 录音资源Id
	 */
	@Override
	public void onMessageRecordPlayLoaded(boolean isOk, String msgCode, String resId, byte[] resBytes)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageRecordPlayLoaded(isOk, msgCode, resId);
	}

	/**
	 * 录音加载中
	 * @param msgCode 消息code
	 * @param resId 录音资源Id
	 */
	@Override
	public void onMessageRecordPlayLoading(String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageRecordPlayLoading(msgCode, resId);
	}

	/**
	 * 开始播放录音
	 * @param msgCode 消息code
	 * @param resId 录音资源Id
	 */
	@Override
	public void onMessageRecordPlayStart(String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageRecordPlayStart(msgCode, resId);
	}

	/**
	 * 停止播放录音
	 * @param msgCode 消息code
	 * @param resId 录音资源Id
	 */
	@Override
	public void onMessageRecordPlayStop(String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageRecordPlayStop(msgCode, resId);
	}

	/**
	 * 开始录音
	 */
	@Override
	public void onMessageRecordStart()
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageRecordStart();
	}

	/**
	 * 录音录制结束
	 * @param seconds 录音时长
	 * @param msgCode 消息code
	 */
	@Override
	public void onMessageRecordStop(int seconds, String msgCode)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageRecordStop(seconds, msgCode);
	}

	/**
	 * 录音传输后
	 * @param msgCode 消息code
	 * @param resId 录音资源Id
	 */
	@Override
	public void onMessageRecordTransfered(String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageRecordTransfered(msgCode, resId);
	}

	/**
	 * 监听PTT消息
	 * @param session 会话Entity
	 * @param message 消息Entity
	 * @param msgCode 消息code
	 * @param resId 录音资源Id
	 */
	@Override
	public void onMessagePttRecord(AirSession session, AirMessage message, String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		if (session != null && message != null && AirSession.sessionType(message.getSessionCode()) == AirSession.TYPE_CHANNEL && !TextUtils.equals(message.getIpocidFrom(), AirtalkeeAccount.getInstance().getUserId()) && AirServices.getInstance() != null && !Util.isScreenOn(AirServices.getInstance()) && HomeActivity.getInstance() != null)
		{
			AirChannel channel = AirtalkeeChannel.getInstance().ChannelGetByCode(message.getSessionCode());
			if (channel != null)
			{
				String from = channel.getDisplayName();
				String typeText = AirServices.getInstance().getString(R.string.talk_msg_ptt);
				String msg = typeText + " (" + message.getInameFrom() + ")";
				message.setState(AirMessage.STATE_NEW);

				Intent intent = new Intent();
				intent.setClass(AirServices.getInstance(), AccountActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Util.showNotification(Util.NOTIFI_ID_VOICE_RECORD, AirServices.getInstance(), intent, from, typeText, msg, null);

				session.setMessageUnreadCount(session.getMessageUnreadCount() + 1);
			}
		}

		if (msgListener != null)
			msgListener.onMessageRecordPtt(session, message, msgCode, resId);
	}

	/**
	 * 系统广播
	 * @param number 广播数量
	 */
	@Override
	public void onSystemBroadcastNumber(int number)
	{
		// TODO Auto-generated method stub
		if (noticeListener != null)
		{
			noticeListener.onMmiNoticeNew(AirtalkeeAccount.getInstance().SystemBroadcastNumberGet());
		}
	}

	/**
	 * 系统广播接收处理
	 * @param title 广播标题
	 * @param url 广播地址
	 */
	@Override
	public void onSystemBroadcastPush(final String title, String url)
	{
		// TODO Auto-generated method stub
		final Context ct = AirServices.getInstance();
		if (ct != null)
		{
			Intent intent = new Intent();
			intent.setClass(ct, MenuNoticeActivity.class);
			intent.putExtra("url", AccountController.getDmWebNoticeUrl());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Util.showNotification(Util.NOTIFI_ID_NOTICE, ct, intent, title, "[" + ct.getString(R.string.talk_tools_notice) + "] " + title, title, null);
			Sound.playSound(Sound.PLAYER_NEWINFO, false, ct);
			// 弹出窗口
			try
			{
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable()
				{
					public void run()
					{
						dialog = new AlertDialog(ct, ct.getString(R.string.talk_tools_notice), title, ct.getString(R.string.talk_tools_know), ct.getString(R.string.talk_session_call), AirMessageTransaction.this, DIALOG_CALL);
						dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
						dialog.show();
					}
				});
				if (StatusBarTitle.getInstance() != null)
				{
					StatusBarTitle.getInstance().checkBrodcast();
				}
				if (MoreActivity.getInstance() != null)
				{
					MoreActivity.getInstance().checkBrodcast();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (noticeListener != null)
		{
			noticeListener.onMmiNoticeNew(AirtalkeeAccount.getInstance().SystemBroadcastNumberGet());
		}
	}

	@Override
	public void onClickOk(int id, Object obj)
	{
		switch (id)
		{
			case DIALOG_CALL:
				callStationCenter();
				break;
			case DIALOG_2_SEND_MESSAGE:
				if (obj != null)
				{
//					String sessionCode = obj.toString();
//					Context context = AirServices.getInstance();
//					if (null != context)
//					{
//						Intent it = new Intent(context, SessionDialogActivity.class);
//						it.putExtra("sessionCode", sessionCode);
//						it.putExtra("type", AirServices.TEMP_SESSION_TYPE_MESSAGE);
//						context.startActivity(it);
//					}
				}
				break;
		}
	}

	@Override
	public void onClickOk(int id, boolean isChecked)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onClickCancel(int id)
	{
		// TODO Auto-generated method stub
	}

	/**
	 * 呼叫调度中心
	 */
	private void callStationCenter()
	{
		final Context context = AirServices.getInstance();
		{
			if (AirtalkeeAccount.getInstance().isAccountRunning())
			{
				if (AirtalkeeAccount.getInstance().isEngineRunning())
				{
					final AirSession s = SessionController.SessionMatchSpecial(AirtalkeeSessionManager.SPECIAL_NUMBER_DISPATCHER, context.getString(R.string.talk_tools_call_center));
					if (s != null)
					{
						alertDialog = new CallAlertDialog(HomeActivity.getInstance(), "正在呼叫" + s.getDisplayName(), "请稍后...", s.getSessionCode(), DIALOG_CALL_CENTER, false, new OnAlertDialogCancelListener()
						{
							@Override
							public void onDialogCancel(int reason)
							{
								// TODO Auto-generated method stub
								switch (reason)
								{
									case AirSession.SESSION_RELEASE_REASON_NOTREACH:
										dialog = new AlertDialog(HomeActivity.getInstance(), null, context.getString(R.string.talk_call_offline_tip), context.getString(R.string.talk_session_call_cancel), context.getString(R.string.talk_call_leave_msg), AirMessageTransaction.this, DIALOG_2_SEND_MESSAGE, s.getSessionCode());
										dialog.show();
										break;
									case AirSession.SESSION_RELEASE_REASON_REJECTED:
										if(Toast.isDebug) Toast.makeText1(AirServices.getInstance(), "对方已拒接", Toast.LENGTH_SHORT).show();
										break;
								}
							}
						});
						alertDialog.show();
					}
				}
				else
				{
					Util.Toast(context, context.getString(R.string.talk_network_warning));
				}
			}
		}
	}

	@Override
	public void onTaskOpr(boolean b, int i, AirTask airTask) {

	}

	@Override
	public void onTaskListGet(boolean isOk, List<AirTask> tasks)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskState(boolean isOk, String taskCode)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskContentListGet(boolean isOk, ArrayList<AirTaskReport> tasks, String taskCode)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskDetailGet(boolean isOk, AirTaskDetail taskDetail, String taskCode)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskAlarm(boolean isOk)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onSystemFenceWarningPush(ArrayList<AirFenceWarning> fences)
	{
		Context ct = AirServices.getInstance();
		if (ct != null)
		{
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Util.showNotification(Util.NOTIFI_ID_FENCE_WARNING, ct, intent, ct.getString(R.string.talk_fence_warning_title), "[" + ct.getString(R.string.talk_fence_warning_title) + "] " + ct.getString(R.string.talk_fence_warning_tip), ct.getString(R.string.talk_fence_warning_tip), null);
			ToastUtils.showCustomImgToast(ct.getString(R.string.Fence_warning),R.drawable.ic_warning,ct);
			//Toast.makeText1(ct, R.drawable.ic_error, ct.getString(R.string.talk_fence_warning_title) + "\r\n" + ct.getString(R.string.talk_fence_warning_tip), Toast.LENGTH_LONG).show();
			Sound.playSound(Sound.PLAYER_PTI, false, ct);
		}
	}

	@Override
	public void onTaskDispatch(AirTask task) {

	}
}
