package com.cmccpoc.activity.home.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.activity.MoreActivity;
import com.cmccpoc.activity.SessionNewActivity;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.activity.home.PTTFragment;
import com.cmccpoc.activity.home.adapter.AdapterChannel;
import com.cmccpoc.activity.home.adapter.AdapterSession;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.util.Util;
import com.cmccpoc.widget.SlidingUpPanelLayout.PanelState;

/**
 * 会话和频道自定义View控件
 * 
 * @author Yao
 */
public class SessionAndChannelView extends LinearLayout implements OnClickListener, OnItemClickListener
{
	public interface ViewChangeListener
	{
		/**
		 * view改变时
		 * @param sessionCode 会话code
		 */
		public void onViewChanged(String sessionCode);
	}

	private GridView gvChannels;
	private GridView gvSession;
	private AdapterChannel adapterChannel;
	private AdapterSession adapterSession;
	private TextView tvChannelTitle, tvSessionTitle, tvSettingCancel;
	private CharSequence channelTitle, sessionTitle;
	private ViewChangeListener listener;
	private ImageView ivUnread, ivSetting, ivSlidingBack;
	private boolean showDialog = true;
	private AlertDialog dialog;
	private static SessionAndChannelView mInstance;

	public static SessionAndChannelView getInstance()
	{
		return mInstance;
	}

	public SessionAndChannelView(Context context, ViewChangeListener l)
	{
		super(context);
		// TODO Auto-generated constructor stub
		LayoutInflater.from(this.getContext()).inflate(R.layout.layout_channels, this);
		this.listener = l;

		gvChannels = (GridView) findViewById(R.id.gv_channels);
		adapterChannel = new AdapterChannel(this.getContext(), null);
		tvChannelTitle = (TextView) findViewById(R.id.tv_channel_title);
		channelTitle = tvChannelTitle.getText();
		gvChannels.setAdapter(adapterChannel);
		gvChannels.setOnItemClickListener(this);

		gvSession = (GridView) findViewById(R.id.gv_session);
		tvSessionTitle = (TextView) findViewById(R.id.tv_session_title);
		sessionTitle = tvSessionTitle.getText();
		adapterSession = new AdapterSession(context);
		gvSession.setAdapter(adapterSession);
		gvSession.setOnItemClickListener(this);

		findViewById(R.id.channel_button_more).setOnClickListener(this);
		ivSetting = (ImageView) findViewById(R.id.iv_setting);
		ivSetting.setOnClickListener(this);

		tvSettingCancel = (TextView) findViewById(R.id.tv_setting_cancel);
		tvSettingCancel.setOnClickListener(this);

		ivUnread = (ImageView) findViewById(R.id.iv_Unread);
		checkBrodcast();
		ivSlidingBack = (ImageView) findViewById(R.id.sliding_back);
		ivSlidingBack.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//HomeActivity.getInstance().mLayout.setPanelState(PanelState.COLLAPSED);
			}
		});
		registerSessionUpdateListener();
		mInstance = this;
		refreshChannelAndDialog();
	}
	
	/**
	 * 检测广播消息，有则显示未读标记
	 */
	public void checkBrodcast()
	{
		if (Config.funcBroadcast && AirtalkeeAccount.getInstance().SystemBroadcastNumberGet() > 0)
		{
			ivUnread.setVisibility(View.VISIBLE);
		}
		else
		{
			ivUnread.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onFinishInflate()
	{
		// TODO Auto-generated method stub
		super.onFinishInflate();
	}

	/**
	 * 界面恢复时
	 */
	public void resume()
	{
		if (adapterChannel != null)
		{
			adapterChannel.notifyDataSetChanged();
			tvChannelTitle.setText(channelTitle + "(" + adapterChannel.getCount() + ")");
		}
		if (adapterSession != null)
		{
			tvSettingCancel.setVisibility(View.GONE);
			ivSetting.setVisibility(View.VISIBLE);
			gvSession.setClickable(true);
			adapterSession.setEditing(false);
			adapterSession.notifyDataSetChanged();
			int count = adapterSession.getCount();
			if (adapterSession.isEditing())
			{
				tvSessionTitle.setText(sessionTitle + "(" + count + ")");
			}
			else
			{
				tvSessionTitle.setText(sessionTitle + "(" + (count - 1) + ")");
			}
		}
		checkBrodcast();
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.channel_button_more:
				Intent it = new Intent(this.getContext(), MoreActivity.class);
				this.getContext().startActivity(it);
				break;
			case R.id.iv_setting:
			{
				int count = adapterSession.getCount();
				if (count > 1)
				{
					adapterSession.setEditing(true);
					adapterSession.notifyDataSetChanged();
					tvSettingCancel.setVisibility(View.VISIBLE);
					ivSetting.setVisibility(View.GONE);
					gvSession.setClickable(false);
				}
				break;
			}
			case R.id.tv_setting_cancel:
			{
				adapterSession.setEditing(false);
				adapterSession.notifyDataSetChanged();
				tvSettingCancel.setVisibility(View.GONE);
				ivSetting.setVisibility(View.VISIBLE);
				gvSession.setClickable(true);
				break;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
	{
		final AirSession session = AirSessionControl.getInstance().getCurrentSession();
		// TODO Auto-generated method stub
		switch (parent.getId())
		{
			case R.id.gv_channels:
				if (session != null)
				{
					if (session.getType() == AirSession.TYPE_DIALOG)
					{
						AirSessionControl.getInstance().SessionEndCall(session);
					}
				}
				AirChannel channel = (AirChannel) adapterChannel.getItem(position);
				if (channel != null)
				{
					if (AirtalkeeAccount.getInstance().isEngineRunning())
					{
						AirSessionControl.getInstance().SessionChannelIn(channel.getId());
						if (listener != null)
						{
							listener.onViewChanged(channel.getId());
						}
					}
					else
					{
						Util.Toast(getContext(), getContext().getString(R.string.talk_network_warning));
					}
				}
				refreshChannelAndDialog();
				PTTFragment.getInstance().getVideoPannel().setVisibility(View.GONE);
				break;
			case R.id.gv_session:
				if (!adapterSession.isEditing())
				{
					if (position == 0)
					{
						Intent it = new Intent(getContext(), SessionNewActivity.class);
						getContext().startActivity(it);
					}
					else
					{
						dialog = new AlertDialog(getContext(), getContext().getString(R.string.talk_in_temp_session_tip), getContext().getString(R.string.talk_no_tip), getContext().getString(R.string.talk_ok), true, new DialogListener()
						{
							@Override
							public void onClickOk(int id, boolean isChecked)
							{
								showDialog = !isChecked;
								AirSession s = (AirSession) adapterSession.getItem(position);
								if (session.getType() == AirSession.TYPE_DIALOG && session.getSessionState() == AirSession.SESSION_STATE_DIALOG && !s.getSessionCode().equals(session.getSessionCode()))
								{
									AirSessionControl.getInstance().SessionEndCall(session);
								}
								if (s != null)
								{
									// AirtalkeeSessionManager.getInstance().getSessionByCode(s.getSessionCode());
									if (listener != null)
									{
										listener.onViewChanged(s.getSessionCode());
									}
								}
								refreshChannelAndDialog();
								PTTFragment.getInstance().getVideoPannel().setVisibility(View.GONE);
							}

							@Override
							public void onClickOk(int id, Object obj)
							{
							}

							@Override
							public void onClickCancel(int id)
							{
								dialog.cancel();
							}
						}, 0);
						AirSession s = (AirSession) adapterSession.getItem(position);
						if ( session != null && showDialog && session.getType() == AirSession.TYPE_DIALOG && !s.getSessionCode().equals(session.getSessionCode()))
						{
							dialog.show();
						}
						else
						{
							if (session != null && session.getType() == AirSession.TYPE_DIALOG && session.getSessionState() == AirSession.SESSION_STATE_DIALOG && !s.getSessionCode().equals(session.getSessionCode()))
							{
								AirSessionControl.getInstance().SessionEndCall(session);
							}
							if (s != null)
							{
								if (listener != null)
								{
									listener.onViewChanged(s.getSessionCode());
								}
							}
							refreshChannelAndDialog();
							PTTFragment.getInstance().getVideoPannel().setVisibility(View.GONE);
						}
					}
				}
				break;
		}
	}

	/**
	 * 注册会话更新的监听
	 */
	private void registerSessionUpdateListener()
	{
		final IntentFilter filter = new IntentFilter();
		filter.addAction(MediaStatusBar.ACTION_ON_SESSION_UPDATE);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		getContext().registerReceiver(receiver, filter);
	}

	/**
	 * 广播接收器，监听会话是否被更新
	 */
	BroadcastReceiver receiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// TODO Auto-generated method stub
			if (intent.getAction().equals(MediaStatusBar.ACTION_ON_SESSION_UPDATE))
			{
				int type = intent.getIntExtra(MediaStatusBar.EXTRA_TYPE, 0);
				switch (type)
				{
					case MediaStatusBar.TYPE_ON_SESSION_MEMBER_UPDATE:
					case MediaStatusBar.TYPE_ON_SESSION_ESTABLISHED:
					case MediaStatusBar.TYPE_ON_SESSION_PRESENCE:
						if (adapterChannel != null)
							adapterChannel.notifyDataSetChanged();
						if (adapterSession != null)
							adapterSession.notifyDataSetChanged();
						break;
				}
			}
		}
	};

	public void unRegisterReceiver()
	{
		getContext().unregisterReceiver(receiver);
	}
	
	public void refreshChannel()
	{
		adapterChannel.notifyDataSetChanged();
	}

	public void refreshChannelAndDialog()
	{
		refreshChannel();
		adapterSession.notifyDataSetChanged();
	}
}
