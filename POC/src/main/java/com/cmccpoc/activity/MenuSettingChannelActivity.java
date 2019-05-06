package com.cmccpoc.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.entity.AirChannel;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.adapter.AdapterChannelAttach;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.util.ThemeUtil;

public class MenuSettingChannelActivity extends Activity implements OnItemClickListener
{

	private AdapterChannelAttach adapterChannelList;
	private ListView mList;
	
	private String sessionCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_menu_setting_channel);
		doInitView();
	}
	
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		adapterChannelList.notifyDataSetChanged();
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
	}
	
	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_channel);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(clickListener);
		
		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ivRightLay.setOnClickListener(clickListener);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		TextView tvSendLocation = (TextView) findViewById(R.id.tv_orange_button);
		tvSendLocation.setText(getString(R.string.talk_ok));
		tvSendLocation.setVisibility(View.VISIBLE);
		tvSendLocation.setOnClickListener(clickListener);

		for (int i = 0; i < AirtalkeeChannel.getInstance().getChannels().size(); i++)
		{
			if (AirtalkeeChannel.getInstance().getChannels().get(i).isAttachItem())
			{
				adapterChannelList = new AdapterChannelAttach(this, i);
				break;
			}
		}
		if (adapterChannelList == null)
		{
			adapterChannelList = new AdapterChannelAttach(this, 0);
		}
		mList = (ListView) findViewById(R.id.channel_list);
		mList.setAdapter(adapterChannelList);
		adapterChannelList.notifyDataSetChanged();
		mList.setOnItemClickListener(this);
	}

	private OnClickListener clickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
				case R.id.tv_orange_button:
				case R.id.talk_menu_right_button:
				{
					AirChannel channel = AirtalkeeChannel.getInstance().ChannelGetByCode(sessionCode);
					for (int i = 0; i < AirtalkeeChannel.getInstance().getChannels().size(); i++)
					{
						if (channel == AirtalkeeChannel.getInstance().getChannels().get(i))
						{
							channel.setAttachItem(true);
						}
						else
						{
							AirtalkeeChannel.getInstance().getChannels().get(i).setAttachItem(false);
						}
					}
					AirSessionControl.getInstance().channelAttachSave();
					finish();
					break;
				}
				case R.id.menu_left_button:
				case R.id.bottom_left_icon:
				{
					finish();
					break;
				}
			}
		}
	};
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		switch (parent.getId())
		{
			case R.id.channel_list:
			{
				AirChannel channel = (AirChannel) adapterChannelList.getItem(position);
				sessionCode = channel.getSession().getSessionCode();
				adapterChannelList.setSelected(position);
				adapterChannelList.notifyDataSetChanged();
				break;
			}
		}
	}
	
	

}
