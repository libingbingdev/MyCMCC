package com.cmccpoc.activity.home.adapter;

import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirPresetGroup;
import com.cmccpoc.R;

/**
 * 频道列表适配器
 * @author Yao
 */
@SuppressLint("UseSparseArrays")
public class AdapterGroupPreset extends BaseAdapter
{
	private Context context = null;
	private int selected = -1;
	List<AirPresetGroup> presetGroups;
	
	public AdapterGroupPreset(Context _context, int selected, List<AirPresetGroup> presetGroups)
	{
		context = _context;
		this.selected = selected;
		this.presetGroups = presetGroups;
	}

	public void setSelected(int selected)
	{
		this.selected = selected;
	}
	
	public List<AirContact> getSelectedMembers()
	{
		return ((AirChannel) getItem(selected)).MembersGet();
	}
	
	@Override
	public int getCount()
	{
		return presetGroups.size();
	}

	@Override
	public Object getItem(int position)
	{
		return presetGroups.get(position);
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
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_group_preset, null);
			holder = new ViewHolder();
			holder.tvMemberSize = (TextView) convertView.findViewById(R.id.talk_group_member_size);
			holder.tvGroupName = (TextView) convertView.findViewById(R.id.talk_group_text);
			holder.tvChecked = (ImageView) convertView.findViewById(R.id.talk_group_checked);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		final AirPresetGroup group = (AirPresetGroup) getItem(position);
		if (group != null)
		{
			holder.tvGroupName.setText(group.getGroupName());
			holder.tvMemberSize.setText("（" + group.getMemberCount() + "）");
			if (position == selected)
			{
				holder.tvChecked.setImageResource(R.drawable.radio_selected);
			}
			else
			{
				holder.tvChecked.setImageResource(R.drawable.rb_report_normal);
			}
		}
		return convertView;
	}

	class ViewHolder
	{
		ImageView tvChecked;
		TextView tvGroupName, tvMemberSize;
	}
}
