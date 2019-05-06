package com.cmccpoc.activity.home.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.control.AirSessionControl;

import java.util.ArrayList;
import java.util.List;

/**
 * 频道列表 适配器 
 * @author Yao
 */
public class AdapterChannel extends BaseAdapter
{
	public static final int GROUP_POSITION = 0;
	private static final int MAX_ITEM_COUNT=50;
	Context mContext;

	public AdapterChannel(Context mContext, ArrayList<AirChannel> data)
	{
		this.mContext = mContext;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		HodlerView hodler = null;
		if (convertView == null)
		{
			convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_channel, null);
			hodler = new HodlerView(convertView);
			convertView.setTag(hodler);
		}
		else
			hodler = (HodlerView) convertView.getTag();
		try
		{
			hodler.fill((AirChannel) getItem(position));
		}
		catch (Exception e)
		{}
		return convertView;
	}

	class HodlerView
	{
		public TextView tvName;
		public ImageView ivCurrent;
		public TextView tvCount;
		public LinearLayout baseView;
		public ImageView ivListener;
		public ImageView ivVoiceLocked;
		public TextView tvUnread;

		public HodlerView(View convertView)
		{
			tvName = (TextView) convertView.findViewById(R.id.tv_name);
			ivCurrent= (ImageView) convertView.findViewById(R.id.iv_current);
			tvCount = (TextView) convertView.findViewById(R.id.tv_count);
			baseView = (LinearLayout) convertView.findViewById(R.id.baseview);
			ivListener = (ImageView) convertView.findViewById(R.id.iv_listen);
			ivVoiceLocked = (ImageView) convertView.findViewById(R.id.iv_lock);
			//tvUnread = (TextView) convertView.findViewById(R.id.tv_unread_count);
		}

		/**
		 * 填充View
		 * @param item 频道Entity
		 */
		public void fill(final AirChannel item)
		{
			AirSession currentSession = AirSessionControl.getInstance().getCurrentSession();
			if (item != null)
			{
				//tvUnread.setText(item.getSession().getMessageUnreadCount() + "");
				tvName.setText(item.getDisplayName());
				if (currentSession != null && currentSession.getSessionCode().equals(item.getId()) && currentSession.getType() == AirSession.TYPE_CHANNEL)
				{
					ivCurrent.setVisibility(View.VISIBLE);
					//baseView.setBackgroundResource(R.drawable.selector_listitem_channel_1);
                    //tvName.setTextColor(Color.RED);
                    //tvName.setTextSize(20);
                    //tvName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
				}
				else
				{
					ivCurrent.setVisibility(View.INVISIBLE);
					//baseView.setBackgroundResource(R.drawable.selector_listitem_channel);
                    //tvName.setTextColor(Color.WHITE);
                    //tvName.setTextSize(12);
				}
				
				int onlineNumber = 0;
				if (item.getSession() != null && item.getSession().getSessionState() == AirSession.SESSION_STATE_DIALOG)
				{
					ivListener.setBackgroundResource(R.drawable.ic_listen_yellow);
					ivListener.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							AirSessionControl.getInstance().SessionChannelOut(item.getId());
							notifyDataSetChanged();
						}
					});
					/*if (item.getMsgUnReadCount() > 0)
					{
						tvUnread.setVisibility(View.VISIBLE);
						tvUnread.setText(item.getMsgUnReadCount() + "");
					}
					else
						tvUnread.setVisibility(View.GONE);*/
				}
				else
				{
					//tvCount.setText(item.getCount() + "");
					ivListener.setBackgroundResource(R.drawable.ic_listen);
					ivListener.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							AirSessionControl.getInstance().SessionChannelIn(item.getId());
							notifyDataSetChanged();
						}
					});
					/*if (item.getSession().getMessageUnreadCount() > 0)
						tvUnread.setVisibility(View.VISIBLE);
					else
						tvUnread.setVisibility(View.GONE);*/
				}
				List<AirContact> members = item.MembersGet();
				if (members != null && members.size() > 0)
				{
					for (AirContact member : members)
					{
						if (member.getStateInChat() == AirContact.IN_CHAT_STATE_ONLINE)
							onlineNumber++;
					}
				}
				tvCount.setText((onlineNumber > 0 ? onlineNumber + "/" : "") + item.getCount());
				if (item.getSession() != null && item.getSession().isVoiceLocked())
				{
					ivVoiceLocked.setVisibility(View.VISIBLE);
				}
				else
				{
					ivVoiceLocked.setVisibility(View.GONE);
				}

			}
		}
	}

	@Override
	public int getCount()
	{
	    if(AirtalkeeChannel.getInstance().getChannels()==null)  return 0;
		return Math.min(MAX_ITEM_COUNT,AirtalkeeChannel.getInstance().getChannels().size());
	}

	@Override
	public Object getItem(int position)
	{
		AirChannel ch = null;
		try
		{
			ch = AirtalkeeChannel.getInstance().getChannels().get(position);
		}
		catch (Exception e)
		{}
		return ch;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
