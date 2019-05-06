package com.cmccpoc.activity.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.widget.SessionAndChannelView;
import com.cmccpoc.activity.home.widget.StatusBarTitle;
import com.cmccpoc.control.AirSessionControl;

/**
 * 临时会话列表 适配器
 * 临时会话列表的第一项为新建会话按钮，而非临时会话项，所以在计算总数的时候需要判断是否进入了编辑模式。（编辑模式没有新建会话按钮）
 * @author Yao
 */
public class AdapterSession extends BaseAdapter
{
	Context mContext;
	private AdapterSession adapterSession;
	private boolean isEditing = false;

	public AdapterSession(Context mContext)
	{
		adapterSession = this;
		this.mContext = mContext;
	}

	public boolean isEditing()
	{
		return isEditing;
	}

	/**
	 * 设置编辑模式
	 * @param isEditing 是否为编辑模式
	 */
	public void setEditing(boolean isEditing)
	{
		this.isEditing = isEditing;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		HodlerView hodler;
		if (!isEditing) // 非编辑状态
		{
			if (position == 0)
			{
				convertView = LayoutInflater.from(mContext).inflate(R.layout.session_header_item, null);
			}
			else
			{
				convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_session, null);
			}
			hodler = new HodlerView(convertView);
			convertView.setTag(hodler);
			try
			{
				hodler.fill((AirSession) getItem(position));
			}
			catch (Exception e)
			{}
		}
		else
		{
			if (position == 0)
			{
				convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_session, null);
				hodler = new HodlerView(convertView);
				convertView.setTag(hodler);
			}
			else
			{
				hodler = (HodlerView) convertView.getTag();
			}
			try
			{
				hodler.fill((AirSession) getItem(position));
			}
			catch (Exception e)
			{}
		}
		return convertView;
	}

	public class HodlerView
	{
		public View baseView;
		public TextView tvName;
		public TextView tvCreate;
		public TextView tvCount;
		public LinearLayout delPannel;
		public ImageView ivDel;
		public TextView tvCancel;
		public LinearLayout missedPanel;
		public TextView tvMissed;
		public TextView tvUnread;

		public HodlerView(View convertView)
		{
			this.baseView = (LinearLayout) convertView.findViewById(R.id.baseview);
			tvCreate = (TextView) convertView.findViewById(R.id.tv_create_session);
			tvName = (TextView) convertView.findViewById(R.id.tv_name);
			tvCount = (TextView) convertView.findViewById(R.id.tv_count);
			delPannel = (LinearLayout) convertView.findViewById(R.id.session_del_pannel);
			ivDel = (ImageView) convertView.findViewById(R.id.btn_session_del);
			missedPanel = (LinearLayout) convertView.findViewById(R.id.session_missed_panel);
			tvMissed = (TextView) convertView.findViewById(R.id.tv_session_missed);
			tvUnread = (TextView) convertView.findViewById(R.id.tv_unread_count);
		}

		/**
		 * 填充View控件
		 * @param item 会话Entity
		 */
		public void fill(final AirSession item)
		{
			if (item != null)
			{
				String display = item.getDisplayName();
				tvName.setText(display.toString());
				Log.d(AdapterSession.class, "AdapterSession fill state=" + item.getSessionState() + ",online=" + item.getSessionMemberOnlineCount());
				AirSession currentSession = AirSessionControl.getInstance().getCurrentSession();
				if (currentSession != null)
				{
					if (currentSession.getSessionCode().equals(item.getSessionCode()))
					{
						baseView.setBackgroundResource(R.drawable.selector_listitem_channel_1);
						tvCount.setText(currentSession.SessionPresenceList().size() + "/" + (currentSession.getMemberAll().size() + 1));
					}
					else
					{
						baseView.setBackgroundResource(R.drawable.selector_listitem_channel);
						tvCount.setText((item.getMemberAll().size() + 1) + "");
					}
				}
				else
				{
					baseView.setBackgroundResource(R.drawable.selector_listitem_channel);
					tvCount.setText((item.getMemberAll().size() + 1) + "");
				}
				if (item.getMessageUnreadCount() > 0)
				{
					tvUnread.setVisibility(View.VISIBLE);
					tvUnread.setText(item.getMessageUnreadCount() + "");
				}
				else
				{
					tvUnread.setVisibility(View.GONE);
				}
				if (isEditing)
				{
					delPannel.setVisibility(View.VISIBLE);
					ivDel.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							AirtalkeeSessionManager.getInstance().SessionRemove(item.getSessionCode());
							SessionAndChannelView.getInstance().resume();
							adapterSession.notifyDataSetChanged();
							if (StatusBarTitle.getInstance() != null)
								StatusBarTitle.getInstance().closeSession(item.getSessionCode());
						}
					});
				}
				else
				{
					delPannel.setVisibility(View.GONE);
				}

				String new_Message = (item.getMessageLast() != null) ? item.getMessageLast().getBody() : baseView.getResources().getString(R.string.main_default_message);

				if (mContext.getString(R.string.talk_call_state_missed_call).equals(new_Message))
				{
					tvMissed.setText(mContext.getString(R.string.talk_call_state_missed_call_short));
					missedPanel.setVisibility(View.VISIBLE);
				}
				else if (mContext.getString(R.string.talk_call_state_rejected_call).equals(new_Message))
				{
					tvMissed.setText(mContext.getString(R.string.talk_call_state_rejected_call_short));
					missedPanel.setVisibility(View.VISIBLE);
				}
				else
				{
					missedPanel.setVisibility(View.GONE);
				}
			}
		}
	}

	@Override
	public int getCount()
	{
		return isEditing ? AirtalkeeSessionManager.getInstance().getSessionList().size() : AirtalkeeSessionManager.getInstance().getSessionList().size() + 1;
	}

	@Override
	public Object getItem(int position)
	{
		AirSession ses = null;
		try
		{
			ses = isEditing ? AirtalkeeSessionManager.getInstance().getSessionList().get(position) : AirtalkeeSessionManager.getInstance().getSessionList().get(position - 1);
		}
		catch (Exception e)
		{}
		return ses;
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}
}
