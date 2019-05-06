package com.cmccpoc.activity.home.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnContactPresenceListener;
import com.airtalkee.sdk.OnMediaListener;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.activity.MenuReportAsPicActivity;
import com.cmccpoc.activity.VideoSessionActivity;
import com.cmccpoc.activity.home.BaseFragment;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.activity.home.MemberFragment;
import com.cmccpoc.activity.home.PTTFragment;
import com.cmccpoc.activity.home.widget.StatusBarBottom.OnBarItemClickListener;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.listener.OnMmiSessionListener;
import com.cmccpoc.listener.OnMmiVideoKeyListener;
import com.cmccpoc.receiver.ReceiverVideoKey;
import com.cmccpoc.util.Util;

/**
 * 主界面上PTT媒体按键状态
 * 
 * @author Yao
 */
public class MediaStatusBar extends LinearLayout implements
		OnBarItemClickListener, OnMmiSessionListener, OnMediaListener,
		OnContactPresenceListener, OnMmiVideoKeyListener
{
	public static final int TYPE_ON_MEDIA_QUEUEOUT = 0;
	public static final int TYPE_ON_MEDIA_QUEUEIN = 1;
	public static final int TYPE_ON_MEDIA_QUEUE = 2;
	public static final int TYPE_ON_MEDIA_STATE_LISTEN_VOICE = 3;
	public static final int TYPE_ON_MEDIA_STATE_LISTEN_END = 4;
	public static final int TYPE_ON_MEDIA_STATE_LISTEN = 5;
	public static final int TYPE_ON_MEDIA_STATE_TALK_END = 6;
	public static final int TYPE_ON_MEDIA_STATE_TALK = 7;
	public static final int TYPE_ON_SESSION_OUTGOING_RINGING = 8;
	public static final int TYPE_ON_SESSION_ESTABLISHING = 9;
	public static final int TYPE_ON_SESSION_ESTABLISHED = 10;
	public static final int TYPE_ON_SESSION_RELEASED = 11;
	public static final int TYPE_ON_SESSION_PRESENCE = 12;
	public static final int TYPE_ON_SESSION_MEMBER_UPDATE = 13;
	public static final int TYPE_ON_MEDIA_STATE_TALK_PREPARING = 14;

	public static String ACTION_ON_SESSION_UPDATE = "ON_SESSION_UPDATE";
	public static String EXTRA_SESSION_CODE = "SESSION_CODE";
	public static String EXTRA_TYPE = "TYPE";

	private AirSession session;
	private int currentPage = 0;
	private LinearLayout barGroup;
	public StatusBarTitle barTitle;
	private StatusTalkBtn talkBtn;
	private int[] memRes = new int[] { R.drawable.selector_fun_call, R.drawable.selector_fun_msg, R.drawable.selector_fun_cancel };
	private int[] pttRes = new int[] { R.drawable.ic_fun_report, R.drawable.ic_fun_video, R.drawable.ic_fun_call_center };
	private int[] pttRes_PIC = new int[] { R.drawable.ic_fun_report, R.drawable.ic_fun_video, R.drawable.ic_fun_case };
	private int[] IMRes_D = new int[] { R.drawable.ic_fun_voice, R.drawable.ic_fun_other, R.drawable.ic_fun_input };
	private int[] IMRes_R = new int[] { R.drawable.ic_fun_voice, R.drawable.ic_image, R.drawable.ic_fun_input };

	private int[][] barArray = new int[][] { memRes, pttRes, IMRes_D };
	private boolean meOn = false, otherOn = false;
	private AirSession otherOnSession = null;

	@SuppressLint("UseSparseArrays")
	private Map<Integer, StatusBarBottom> bars = new HashMap<Integer, StatusBarBottom>();

	public StatusBarTitle getStatusBarTitle()
	{
		return barTitle;
	}

	protected SharedPreferences sessionSp;

	public MediaStatusBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		LayoutInflater.from(this.getContext()).inflate(R.layout.include_home_function, this);
		this.sessionSp = context.getSharedPreferences(BaseFragment.SESSION_EVENT_KEY, 0);
	}

	public StatusBarBottom getStatusBarBottom(int pageIndex)
	{
		return bars.get(pageIndex);
	}

	/**
	 * 初始化
	 * 
	 * @param title
	 *            StatusBarTitle控件
	 * @param s
	 *            会话Entity
	 */
	public void init(StatusBarTitle title, AirSession s)
	{
		this.barTitle = title;
		setSession(s);
	}

	/**
	 * 设置Session会话
	 * 
	 * @param s
	 *            会话Entity
	 */
	public void setSession(AirSession s)
	{
		listenerEnable();
		this.session = s;
		barTitle.setSession(this.session);
		talkBtn.setSession(this.session);
		android.util.Log.d("zlm","setSession");
		sessionRefresh();
		barTitle.otherSpeakerClean();
	}

	/**
	 * 获取会话
	 * 
	 * @return 会话Entity
	 */
	public AirSession getSession()
	{
		return this.session;
	}

	@Override
	protected void onFinishInflate()
	{
		// TODO Auto-generated method stub
		super.onFinishInflate();
		initFindView();
	}

	/**
	 * 初始化控件
	 */
	private void initFindView()
	{
		barGroup = (LinearLayout) findViewById(R.id.tools_bar);
		talkBtn = (StatusTalkBtn) findViewById(R.id.status_talk_btn);
		if (!Config.funcPTTButton)
			enablePTTButton(View.GONE);
		barInit();
	}

	public void enablePTTButton(int visiblity)
	{
		switch (visiblity)
		{
			case View.GONE:
				findViewById(R.id.media_ptt_content).setVisibility(View.GONE);
				findViewById(R.id.function_btn_layout).getLayoutParams().height = findViewById(R.id.tools_bar).getLayoutParams().height + findViewById(R.id.indicator_layout).getLayoutParams().height;
				break;
			case View.VISIBLE:
				findViewById(R.id.media_ptt_content).setVisibility(View.VISIBLE);
				findViewById(R.id.function_btn_layout).getLayoutParams().height = LayoutParams.WRAP_CONTENT;
				break;
			case View.INVISIBLE:
				break;
			default:
				break;
		}
	}

	public View getBottomBarParent()
	{
		return barGroup;
	}

	/**
	 * 页面切换
	 * 
	 * @param arg0
	 *            当前页面索引
	 */
	public void onPageChanged(int arg0)
	{
		// TODO Auto-generated method stub
		StatusBarBottom bar = bars.get(currentPage);
		bar.setVisibility(View.GONE);
		currentPage = arg0;
		bar = bars.get(currentPage);
		bar.setVisibility(View.VISIBLE);
	}

	/**
	 * 按钮初始化
	 */
	private void barInit()
	{
		for (int i = 0; i < barArray.length; i++)
		{
			StatusBarBottom bar = new StatusBarBottom(barArray[i][0], barArray[i][1], barArray[i][2], i, getContext(), this);
			barGroup.addView(bar);
			bars.put(i, bar);
		}
	}

	/**
	 * 设置按钮是否可以
	 * 
	 * @param pageIndex
	 *            界面索引
	 * @param enabled
	 *            是否可用
	 */
	public void setBarEnable(int pageIndex, boolean enabled)
	{
		if (bars != null && bars.size() > 0)
		{
			StatusBarBottom bar = bars.get(pageIndex);
			if (null != bar)
			{
				ViewGroup grop = (ViewGroup) bar.getChildAt(0);
				for (int i = 0; i < grop.getChildCount(); i++)
				{
					View child = grop.getChildAt(i);
					child.setEnabled(enabled);
				}
			}
		}
	}

	/**
	 * 设置按钮可见
	 * 
	 * @param visibility
	 *            可见state
	 */
	public void setMediaStatusBarVisibility(int visibility)
	{
		this.setVisibility(visibility);
	}

	@Override
	public void onBarItemClick(int itemId, int page)
	{
		// TODO Auto-generated method stub
		// Toast.makeText(getContext(), itemId + "--" + page, 0).show();
	}

	/**
	 * 监听器开启
	 */
	public void listenerEnable()
	{
		AirtalkeeContactPresence.getInstance().setContactPresenceListener(this);
		AirtalkeeSessionManager.getInstance().setOnMediaListener(this);
		AirSessionControl.getInstance().setOnMmiSessionListener(this);
		ReceiverVideoKey.setOnMmiVideoKeyListener(this);
		// AirMessageTransaction.getInstance().setOnMessageListener(IMFragment.getInstance());
	}

	/**
	 * 监听器关闭
	 */
	public void listenerDisable()
	{
		AirtalkeeSessionManager.getInstance().setOnMediaListener(null);
		AirtalkeeContactPresence.getInstance().setContactPresenceListener(null);
		AirSessionControl.getInstance().setOnMmiSessionListener(null);
		AirtalkeeMessage.getInstance().setOnMessageListListener(null);
		ReceiverVideoKey.setOnMmiVideoKeyListener(null);
	}

	/**
	 * 会话刷新
	 */
	public void sessionRefresh()
	{
		if (barTitle != null && talkBtn != null)
		{
			android.util.Log.d("zlm","sessionRefresh");
			barTitle.refreshMediaStatus();
			talkBtn.refreshPttButton();
			if (SessionAndChannelView.getInstance() != null)
			{
				SessionAndChannelView.getInstance().refreshChannelAndDialog();
			}
			if (MemberFragment.getInstance() != null)
			{
				MemberFragment.getInstance().onResume();
			}
		}
	}

	@Override
	public void onSessionOutgoingRinging(AirSession session)
	{
		// TODO Auto-generated method stub
		android.util.Log.d("zlm","onSessionOutgoingRinging");
		sessionRefresh();

	}

	@Override
	public void onSessionEstablishing(AirSession session)
	{
		android.util.Log.d("zlm","onSessionEstablishing");
		sessionRefresh();

	}

	@Override
	public void onSessionEstablished(AirSession session, int result)
	{
		android.util.Log.d("zlm","onSessionEstablished");
		sessionRefresh();
		notify2UpdateView(session.getSessionCode(), TYPE_ON_SESSION_ESTABLISHED);
	}

	@Override
	public void onSessionReleased(AirSession session, int reason)
	{
		Log.i(MediaStatusBar.class, "onSessionReleased MediaStatusBar start reason = " + reason);
		android.util.Log.d("zlm","onSessionReleased");
		sessionRefresh();

		notify2UpdateView(session.getSessionCode(), TYPE_ON_SESSION_RELEASED);
	}

	@Override
	public void onSessionPresence(AirSession session, List<AirContact> membersAll, List<AirContact> membersPresence)
	{
		try
		{
			notify2UpdateView(session.getSessionCode(), TYPE_ON_SESSION_PRESENCE);
			if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
			{
				MemberFragment.getInstance().refreshMembers(session, membersAll);
				MemberFragment.getInstance().refreshAllMembers();
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@Override
	public void onSessionMemberUpdate(AirSession session, List<AirContact> members, boolean isOk)
	{
		try
		{
			notify2UpdateView(session.getSessionCode(), TYPE_ON_SESSION_MEMBER_UPDATE);
			if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
			{
				MemberFragment.getInstance().refreshMembers(session, session.getMemberAll());
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@Override
	public void onMediaStateTalkPreparing(AirSession session)
	{
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			talkBtn.refreshPttButton();
		}
	}

	@Override
	public void onMediaStateTalk(AirSession session)
	{
		android.util.Log.d("zlm","onMediaStateTalk  ");
		meOn = true;
		barTitle.refreshMediaStatus();
		talkBtn.refreshPttButton();
	}

	@Override
	public void onMediaStateTalkEnd(AirSession session, int reason)
	{
		android.util.Log.d("zlm","onMediaStateTalkEnd  ");
		meOn = false;
		//if (!otherOn)
		{
			switch (reason)
			{
				case AirtalkeeSessionManager.TALK_FINISH_REASON_EXCEPTION:
					//Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_exception));
					ToastUtils.showCustomImgToast(getContext().getString(R.string.talk_channel_tip_media_exception),R.drawable.ic_warning,getContext());
					break;
				case AirtalkeeSessionManager.TALK_FINISH_REASON_LISTEN_ONLY:
					//Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_listen_only));
					ToastUtils.showCustomImgToast(getContext().getString(R.string.talk_channel_tip_media_listen_only),R.drawable.ic_warning,getContext());
					break;
				case AirtalkeeSessionManager.TALK_FINISH_REASON_SPEAKING_FULL:
					//Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_speak_full));
					ToastUtils.showCustomImgToast(getContext().getString(R.string.talk_channel_tip_media_speak_full),R.drawable.ic_warning,getContext());
					break;
				case AirtalkeeSessionManager.TALK_FINISH_REASON_TIMEOUT:
					//Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_timeout));
					ToastUtils.showCustomImgToast(getContext().getString(R.string.talk_channel_tip_media_timeout),R.drawable.ic_warning,getContext());
					break;
				case AirtalkeeSessionManager.TALK_FINISH_REASON_TIMEUP:
					//Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_timeup));
					ToastUtils.showCustomImgToast(getContext().getString(R.string.talk_channel_tip_media_timeup),R.drawable.ic_warning,getContext());
					break;
				case AirtalkeeSessionManager.TALK_FINISH_REASON_GRABED:
					//Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_interruptted));
					ToastUtils.showCustomImgToast(getContext().getString(R.string.talk_channel_tip_media_interruptted),R.drawable.ic_warning,getContext());
					break;
				default:
					break;
			}
			barTitle.refreshMediaStatus();
		}

		if (otherOn && otherOnSession != null && otherOnSession != session)
		{
			barTitle.otherSpeakerOn(otherOnSession);
		}
		/*
		else
		{
			if (otherOn && otherOnSession != null)
			{
				barTitle.otherSpeakerOn(otherOnSession);
			}
		}
		*/
		talkBtn.refreshPttButton();
		int val = sessionSp.getInt(BaseFragment.SESSION_EVENT_KEY, 1);
		sessionSp.edit().putInt(BaseFragment.SESSION_EVENT_KEY, val + 1).commit();
		if (null != PTTFragment.getInstance())
			PTTFragment.getInstance().refreshPlayback();
	}

	@Override
	public void onMediaStateListen(AirSession session, AirContact speaker)
	{
		android.util.Log.d("zlm","onMediaStateListen  ");
		if (this.session != null)
		{
			otherOn = true;
			if (TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
				barTitle.refreshMediaStatus();
			else
			{
				if (!meOn)
					barTitle.otherSpeakerOn(session);
			}
			otherOnSession  = session;
		}
	}

	@Override
	public void onMediaStateListenEnd(AirSession session)
	{
		android.util.Log.d("zlm","onMediaStateListenEnd  ");
		if (this.session != null)
		{
			otherOn = false;
			if (TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
			{
				barTitle.refreshMediaStatus();
				//PTTFragment.getInstance().refreshPlayback();  // M zlm 在对讲时，切换频道报错;
			}
			else
			{
				barTitle.dissmissDialog();
				if (!meOn)
				{
					barTitle.otherSpeakerOff(session);
				}
				if (TextUtils.equals(otherOnSession.getSessionCode(), session.getSessionCode()))
				{
					otherOnSession = null;
				}
			}
		}

	}

	@Override
	public void onMediaStateListenVoice(AirSession session)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onMediaQueue(AirSession session, ArrayList<AirContact> queue)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			talkBtn.refreshPttButton();
			// Util.Toast(getContext(),
			// getContext().getString(R.string.talk_channel_tip_media_queue_in));
		}
	}

	@Override
	public void onMediaQueueIn(AirSession session)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			talkBtn.refreshPttButton();
			Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_queue_in));
		}
	}

	@Override
	public void onMediaQueueOut(AirSession session)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			talkBtn.refreshPttButton();
			Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_queue_out));
		}
	}

	/**
	 * 发送广播 通知更新View
	 * 
	 * @param sessionCode
	 *            会话code
	 * @param type
	 *            类型
	 */
	private void notify2UpdateView(String sessionCode, int type)
	{
		final Intent intent = new Intent();
		intent.setAction(ACTION_ON_SESSION_UPDATE);
		intent.putExtra(EXTRA_SESSION_CODE, sessionCode);
		intent.putExtra(EXTRA_TYPE, type);
		getContext().sendBroadcast(intent);
	}

	@Override
	public void onContactPresence(boolean isSubscribed, HashMap<String, Integer> presenceMap)
	{
		if (getSession() != null && getSession().getType() == AirSession.TYPE_DIALOG)
		{
			getSession().MembersSort();
		}
		if (MemberFragment.getInstance() != null)
		{
			MemberFragment.getInstance().refreshAllMembers();
			MemberFragment.getInstance().refreshChannelMembers();
		}
		if (HomeActivity.getInstance() != null)
			HomeActivity.getInstance().refreshChannel();
	}

	@Override
	public void onContactPresence(boolean isSubscribed, String uid, int state)
	{
		if (getSession() != null && getSession().getType() == AirSession.TYPE_DIALOG)
		{
			getSession().MembersSort();
		}
		if (MemberFragment.getInstance() != null)
		{
			MemberFragment.getInstance().refreshAllMembers();
			MemberFragment.getInstance().refreshChannelMembers();
		}
		if (HomeActivity.getInstance() != null)
			HomeActivity.getInstance().refreshChannel();
	}

	@Override
	public void onVideoKey()
	{

	}


}
