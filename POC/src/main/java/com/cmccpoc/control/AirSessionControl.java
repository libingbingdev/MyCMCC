package com.cmccpoc.control;

import android.content.Intent;
import android.util.Log;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnSessionListener;
import com.airtalkee.sdk.controller.ChannelController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.listener.OnMmiSessionListener;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.Sound;
import com.cmccpoc.util.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 会话业务管理类
 * @author Yao
 */
public class AirSessionControl implements OnSessionListener
{

	private static final String KEY_GROUP_KEEP = "GROUP_KEEP";
	private static final String KEY_GROUP_ATTACH = "GROUP_ATTACH_";

	private static final String JSON_GIDS = "gids";

	private List<AirChannel> mChannels = new ArrayList<AirChannel>();
	private List<AirSession> mSessions = new ArrayList<AirSession>();
	private HashMap<String, AirSession> mSessionMap = new HashMap<String, AirSession>();
	private AirSession mSessionCurrentChannel = null;
	private AirSession mCurrentTempSession = null;

	private static AirSessionControl mInstance = null;
	private OnMmiSessionListener sessionListener = null;
	private int currentChannelIndex = 0;
	private AirChannel currentSelectChannel = null;

	private AirSessionControl()
	{
	}

	/**
	 * 获取AirSessionControl实例
	 * @return AirSessionControl实例
	 */
	public static AirSessionControl getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new AirSessionControl();
			AirtalkeeSessionManager.getInstance().setOnSessionListener(mInstance);
		}
		return mInstance;
	}

	public void setOnMmiSessionListener(OnMmiSessionListener l)
	{
		this.sessionListener = l;
	}

	// ==============================
	// Session
	// ==============================

	/**
	 * 频道附着
	 * @param isKeep 保持状态
	 */
	public void SessionChannelAttach(boolean isKeep)
	{
		List<AirChannel> channels = new ArrayList<AirChannel>();
		if (isKeep)
		{
			channelJsonParse(KEY_GROUP_KEEP, channels);
		}
		else
		{
			channelJsonParse(KEY_GROUP_ATTACH + AirtalkeeAccount.getInstance().getUserId(), channels);
		}

		if (channels.size() > 0)
		{
			for (int i = 0; i < channels.size(); i++)
			{
				AirSession session = AirtalkeeSessionManager.getInstance().SessionCall(channels.get(i).getId());
				sessionListPut(session, false);
			}
		}
		else
		{
			channels = AirtalkeeChannel.getInstance().getChannels();
			if (channels.size() > 0)
			{
				AirSession session = AirtalkeeSessionManager.getInstance().SessionCall(channels.get(0).getId());
				sessionListPut(session, false);
			}
		}
		channelJsonBuild(KEY_GROUP_KEEP, mChannels);
	}

	/**
	 * 进入频道
	 * @param channelId 频道Id
	 */
	public void SessionChannelIn(String channelId)
	{
		AirSession s = mSessionMap.get(channelId);
		if (s != null && s.getSessionState() == AirSession.SESSION_STATE_DIALOG)
		{
			Log.d("zlm","SessionCall22222222");
			sessionListPut(s, true);
			// AirtalkeeSessionManager.getInstance().SessionLock(s, true);
		}
		else
		{
			Log.d("zlm","SessionCall");
			AirSession session = AirtalkeeSessionManager.getInstance().SessionCall(channelId);
			sessionListPut(session, true);   //进去频道不添加监听
		}
	}

	/**
	 * 进入频道
	 * @param channelId 频道Id
	 */
	public void SessionChannelIn(String channelId,boolean a)
	{
		AirSession s = mSessionMap.get(channelId);
		if (s != null && s.getSessionState() == AirSession.SESSION_STATE_DIALOG)
		{
			Log.d("zlm","SessionCall22222222");
			//sessionListPut(s, true);
			// AirtalkeeSessionManager.getInstance().SessionLock(s, true);
		}
		else
		{
			Log.d("zlm","SessionCall");
			AirSession session = AirtalkeeSessionManager.getInstance().SessionCall(channelId);
			//sessionListPut(session, true);   //进入频道
		}
	}

	/**
	 * 退出频道
	 * @param channelId 频道Id
	 */
	public void SessionChannelOut(String channelId)
	{
		AirtalkeeSessionManager.getInstance().SessionBye(channelId);
	}

	/**
	 * 发起临时组呼叫
	 * @param session 会话Entity
	 */
	public void SessionMakeCall(AirSession session)
	{
		sessionListPut(session, false);
		AirtalkeeSessionManager.getInstance().SessionCall(session);
	}

	/**
	 * 发起临时组呼叫
	 * @param session 会话Entity
	 */
	public void SessionMakeSpecialCall(AirSession session)
	{
		sessionListPut(session, false);
		AirtalkeeSessionManager.getInstance().SessionCallSpecial(session);
	}

	/**
	 * 挂断会话
	 * @param session 会话Entity
	 */
	public void SessionEndCall(AirSession session)
	{
		Sound.stopSound(Sound.PLAYER_CALL_DIAL);
		AirtalkeeSessionManager.getInstance().SessionBye(session);
	}

	// ==============================
	// Session list management
	// ==============================

	/**
	 * 获取当前所在会话
	 * @return 会话Entity
	 */
	public AirSession getCurrentSession()
	{
		AirSession session = null;
		if (mSessions.size() > 0)
		{
			session = mSessions.get(mSessions.size() - 1);
		}
		return session;
	}

	/**
	 * 当前频道是否开启抢断模式
	 * @return true/false
	 */
	public boolean getCurrentSessionGrap()
	{
		boolean isGrap = false;
		AirSession session = getCurrentSession();
		if (session != null && session.getChannel() != null)
		{
			isGrap = session.getChannel().isRoleAppling();
		}
		return isGrap;
	}

	/**
	 * 为当前频道赋值session
	 * @param session 会话Entity
	 */
	public void setCurrentChannelSession(AirSession session)
	{
		if (session != null && session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
		{
			sessionListPut(session, true);
		}
	}

	/**
	 * 获取当前频道session对象
	 * @return
	 */
	public AirSession getCurrentChannelSession()
	{
		return mSessionCurrentChannel;
	}

	/**
	 * 添加到会话列表中
	 * @param session 会话Entity
	 * @param save 是否保存
	 */
	private void sessionListPut(AirSession session, boolean save)
	{
		if (session != null)
		{
			AirSession s_map = mSessionMap.get(session.getSessionCode());
			AirSession s_list = null;
			for (int i = 0; i < mSessions.size(); i ++)
			{
				if (mSessions.get(i) == session)
				{
					s_list = mSessions.get(i);
					break;
				}
			}
			if (s_map != null || s_list != null)
			{
				mSessionMap.remove(session.getSessionCode());
				mSessions.remove(session);
				if (session.getChannel() != null)
					mChannels.remove(session.getChannel());
			}
			mSessions.add(session);
			mSessionMap.put(session.getSessionCode(), session);
			if (session.getChannel() != null)
				mChannels.add(session.getChannel());

			if (session.getType() == AirSession.TYPE_CHANNEL)
			{
				mSessionCurrentChannel = session;
			}

			if (save)
				channelJsonBuild(KEY_GROUP_KEEP, mChannels);
		}
	}

	/**
	 * 从会话列表中移除
	 * @param session 会话Entity
	 */
	public void sessionListRemove(AirSession session)
	{
		if (session != null)
		{
			mSessions.remove(session);
			mSessionMap.remove(session.getSessionCode());
			if (session.getChannel() != null)
				mChannels.remove(session.getChannel());
			if (session == mSessionCurrentChannel)
			{
				mSessionCurrentChannel = null;
				for (int i = mSessions.size() - 1; i >= 0; i--)
				{
					AirSession s = mSessions.get(i);
					if (s != null && s.getType() == AirSession.TYPE_CHANNEL)
					{
						mSessionCurrentChannel = s;
					}
				}
			}
			channelJsonBuild(KEY_GROUP_KEEP, mChannels);
		}
	}

	// ==============================
	// Events
	// ==============================

	@Override
	public void onSessionEstablished(AirSession session, int result)
	{
		// TODO Auto-generated method stub
		if (session != null)
		{
			if (session.getType() == AirSession.TYPE_DIALOG)
			{
				if (mCurrentTempSession != null && mCurrentTempSession != session)
				{
					AirSessionControl.getInstance().SessionEndCall(mCurrentTempSession);
				}
				Sound.stopSound(Sound.PLAYER_CALL_DIAL);
				Sound.playSound(Sound.PLAYER_CALL_BEGIN, false, AirServices.getInstance());
				sessionListPut(session, false);
				AirtalkeeSessionManager.getInstance().SessionLock(session, true);
				AirtalkeeSessionManager.getInstance().SessionLock(session, false);
				if (!HomeActivity.isShowing)
				{
					Intent home = new Intent(AirServices.getInstance(), HomeActivity.class);
					home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					AirServices.getInstance().startActivity(home);
				}
				mCurrentTempSession = session;
			}
			else
			{
				channelIndexSet(session.getChannel());
				if (session == mSessionCurrentChannel)
				{
					// AirtalkeeSessionManager.getInstance().SessionLock(session, true);
				}
			}
		}
		if (sessionListener != null)
		{
			sessionListener.onSessionEstablished(session, result);
		}
	}

	@Override
	public void onSessionEstablishing(AirSession session)
	{
		if (sessionListener != null)
		{
			sessionListener.onSessionEstablishing(session);
		}
	}

	@Override
	public void onSessionMemberUpdate(AirSession session, List<AirContact> members, boolean isOk)
	{
		// TODO Auto-generated method stub
		if (sessionListener != null)
		{
			sessionListener.onSessionMemberUpdate(session, members, isOk);
		}
	}

	@Override
	public void onSessionOutgoingRinging(AirSession session)
	{
		// TODO Auto-generated method stub
		Sound.playSound(Sound.PLAYER_CALL_DIAL, true, AirServices.getInstance());
		if (sessionListener != null)
		{
			sessionListener.onSessionOutgoingRinging(session);
		}
	}

	@Override
	public void onSessionPresence(AirSession session, List<AirContact> membersAll, List<AirContact> membersPresence)
	{
		if (sessionListener != null)
		{
			sessionListener.onSessionPresence(session, membersAll, membersPresence);
		}
	}

	@Override
	public void onSessionReleased(AirSession session, int reason)
	{
		if (reason != AirSession.SESSION_RELEASE_REASON_NETWORK_TERMINATE)
			sessionListRemove(session);
		if (session != null && session.getType() == AirSession.TYPE_DIALOG)
		{
			Sound.stopSound(Sound.PLAYER_CALL_DIAL);
			Sound.playSound(Sound.PLAYER_CALL_END, false, AirServices.getInstance());
		}
		if (sessionListener != null)
		{
			sessionListener.onSessionReleased(session, reason);
		}
		if (session.getType() == AirSession.TYPE_DIALOG)
		{
			if (reason == AirSession.SESSION_RELEASE_REASON_ISB) {
				if(Toast.isDebug) Toast.makeText1(AirServices.getInstance(), "对方为免打扰模式", Toast.LENGTH_LONG).show();
			}else {
				if(Toast.isDebug) Toast.makeText1(AirServices.getInstance(), "会话已结束", Toast.LENGTH_LONG).show();
				AirSessionControl.getInstance().SessionEndCall(session);
				session = AirSessionControl.getInstance().getCurrentChannelSession();
				HomeActivity.getInstance().setMediaStatusBarSession(session);
			}
		}
	}

	// ==============================
	// GROUP keep & attach JSON
	// ==============================

	/**
	 * Json格式化频道信息
	 * @param key 键
	 * @param channels 频道列表
	 */
	private void channelJsonParse(String key, List<AirChannel> channels)
	{
		String string = AirServices.iOperator.getString(key);
		if (string != null)
		{
			try
			{
				JSONObject json = new JSONObject(string);
				JSONArray array = json.optJSONArray(JSON_GIDS);
				if (array != null && array.length() > 0)
				{
					for (int i = 0; i < array.length(); i++)
					{
						AirChannel ch = AirtalkeeChannel.getInstance().ChannelGetByCode(array.getString(i));
						if (ch != null)
						{
							channels.add(ch);
						}
					}
				}
			}
			catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 构建Json格式频道信息
	 * @param key 键
	 * @param channels 频道列表
	 */
	private void channelJsonBuild(String key, List<AirChannel> channels)
	{
		try
		{
			JSONObject json = new JSONObject();
			JSONArray array = new JSONArray();
			for (int i = 0; i < channels.size(); i++)
			{
				array.put(i, channels.get(i).getId());
			}
			json.put(JSON_GIDS, array);
			AirServices.iOperator.putString(key, json.toString());
		}
		catch (Exception e)
		{
			AirServices.iOperator.putString(key, "");
		}
	}

	/**
	 * 加载频道附着
	 */
	public void channelAttachLoad()
	{
		List<AirChannel> channels = new ArrayList<AirChannel>();
		channelJsonParse(KEY_GROUP_ATTACH + AirtalkeeAccount.getInstance().getUserId(), channels);
		for (int i = 0; i < channels.size(); i++)
		{
			AirChannel ch = AirtalkeeChannel.getInstance().ChannelGetByCode(channels.get(i).getId());
			if (ch != null)
				ch.setAttachItem(true);
		}
	}

	/**
	 * 保存频道附着
	 */
	public void channelAttachSave()
	{
		List<AirChannel> channels = new ArrayList<AirChannel>();
		for (int i = 0; i < AirtalkeeChannel.getInstance().getChannels().size(); i++)
		{
			if (AirtalkeeChannel.getInstance().getChannels().get(i).isAttachItem())
				channels.add(AirtalkeeChannel.getInstance().getChannels().get(i));
		}
		channelJsonBuild(KEY_GROUP_ATTACH + AirtalkeeAccount.getInstance().getUserId(), channels);
	}
	
	/**
	 * 频道切换
	 * @return true/false
	 */
	public boolean channelToggle()
	{
		boolean isToogle = currentSelectChannel != null && mSessionCurrentChannel != null && !currentSelectChannel.getId().equals(mSessionCurrentChannel.getSessionCode());
		if (isToogle)
		{
			SessionChannelOut(mSessionCurrentChannel.getSessionCode());
			SessionChannelIn(currentSelectChannel.getId());
		}
		return isToogle;
	}

	/**
	 * 设置频道索引
	 * @param ch 频道Entity
	 */
	public void channelIndexSet(AirChannel ch)
	{
		if (ch != null)
		{
			List<AirChannel> channels = ChannelController.dataChannelsGet();
			if (channels != null && channels.size() > 0)
			{
				for (int i = 0; i < channels.size(); i++)
				{
					if (channels.get(i).getId().equals(ch.getId()))
					{
						currentChannelIndex = i;
						currentSelectChannel = null;
						break;
					}
				}
			}
		}
	}

	/**
	 * 频道选择
	 * @param plus 索引增减状态
	 */
	public void channelSelect(boolean plus)
	{
		List<AirChannel> channels = ChannelController.dataChannelsGet();

		if (channels != null && channels.size() > 0)
		{
			if (plus)
				currentChannelIndex++;
			else
				currentChannelIndex--;

			if (currentChannelIndex < 0)
				currentChannelIndex = channels.size() - 1;
			else if (currentChannelIndex >= channels.size())
				currentChannelIndex = 0;

			AirChannel channel = null;
			for (int i = 0; i < channels.size(); i++)
			{
				if (currentChannelIndex == i)
					channel = channels.get(i);
			}
			if (channel != null)
			{
				// TTS
				currentSelectChannel = channel;
			}
			else
			{
				// tip channel is null
			}
		}
		else
		{
			// tip channel list is null
		}
	}
	
	/**
	 * 清理频道会话
	 */
	public void SessionChannelClean()
	{
		for (int i = 0; i < mSessions.size(); i ++)
		{
			if (mSessions.get(i).getType() == AirSession.TYPE_CHANNEL)
				SessionChannelOut(mSessions.get(i).getSessionCode());
		}
	}
}
