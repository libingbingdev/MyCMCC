package com.cmccpoc.activity.home.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.entity.AirChannel;
import com.cmccpoc.R;

/**
 * 频道列表适配器
 * @author Yao
 */
@SuppressLint("UseSparseArrays")
public class AdapterChannelAttach extends BaseAdapter
{
	private Context context = null;
	private int selected;

	public AdapterChannelAttach(Context _context, int selected)
	{
		context = _context;
		this.selected = selected;
	}
	
	public void setSelected(int selected)
	{
		this.selected = selected;
	}

	@Override
	public int getCount()
	{
		return AirtalkeeChannel.getInstance().getChannels().size();
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
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = null;
		// Log.e(AdapterMember.class, "AdapterMember getView");
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_channel_attach, null);
			holder = new ViewHolder();
			holder.chState = (TextView) convertView.findViewById(R.id.talk_channel_current);
			holder.chName = (TextView) convertView.findViewById(R.id.talk_channel_text);
			holder.chChecked = (ImageView) convertView.findViewById(R.id.talk_channel_checked);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		final AirChannel channel = (AirChannel) getItem(position);
		if (channel != null)
		{
			holder.chName.setText(channel.getDisplayName());
			if (position == selected)
			{
				holder.chChecked.setImageResource(R.drawable.radio_selected);
			}
			else
			{
				holder.chChecked.setImageResource(R.drawable.rb_report_normal);
			}
			if (channel.isAttachItem())
			{
				holder.chState.setVisibility(View.VISIBLE);
			}
			else
			{
				holder.chState.setVisibility(View.GONE);
			}
		}
		return convertView;
	}

	class ViewHolder
	{
		ImageView chChecked;
		TextView chName, chState;
	}

}
