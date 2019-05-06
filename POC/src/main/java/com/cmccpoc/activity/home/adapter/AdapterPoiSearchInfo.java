package com.cmccpoc.activity.home.adapter;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.cmccpoc.R;
import com.cmccpoc.entity.LocationBean;

public class AdapterPoiSearchInfo extends BaseAdapter
{
	private Context mContext;
	private List<LocationBean> mPoiInfoList;
	private int selected = -1;

	public AdapterPoiSearchInfo(Context context, List<LocationBean> list)
	{
		super();
		this.mContext = context;
		this.mPoiInfoList = list;
	}
	
	public AdapterPoiSearchInfo(Context context, List<LocationBean> list, int selected)
	{
		this(context, list);
		this.selected = selected;
	}


	public void setNewList(List<LocationBean> list, int selected)
	{
		this.mPoiInfoList = list;
		setSelected(selected);
	}

	public void setSelected(int selected)
	{
		this.selected = selected;
		this.notifyDataSetChanged();
	}

	private class ViewHolder
	{
		TextView tvLocationTitle, tvLocationDetail;
		ImageView ivLocationChecked;
	}

	@Override
	public int getCount()
	{
		return mPoiInfoList.size();
	}

	@Override
	public Object getItem(int position)
	{
		if (mPoiInfoList != null)
		{
			return mPoiInfoList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = null;
		if (convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_location, null);
			holder.ivLocationChecked = (ImageView) convertView.findViewById(R.id.iv_location_checked);
			holder.tvLocationDetail = (TextView) convertView.findViewById(R.id.tv_location_detail);
			holder.tvLocationTitle = (TextView) convertView.findViewById(R.id.tv_location_title);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tvLocationTitle.setText(mPoiInfoList.get(position).getLocName());
		holder.tvLocationDetail.setText(mPoiInfoList.get(position).getAddStr());
		if (selected == position)
		{
			holder.ivLocationChecked.setVisibility(View.VISIBLE);
		}
		else
		{
			holder.ivLocationChecked.setVisibility(View.GONE);
		}
		return convertView;
	}
}
