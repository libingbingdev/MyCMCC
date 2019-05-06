package com.cmccpoc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cmccpoc.R;
import com.cmccpoc.activity.home.adapter.AdapterTaskCase;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirTaskCaseManager;
import com.cmccpoc.entity.AirTaskCase;
import com.cmccpoc.listener.OnMmiAirTaskCaseListener;
import com.cmccpoc.util.ThemeUtil;

import java.util.List;


public class MenuTaskCaseListActivity extends ActivityBase implements OnClickListener, OnItemClickListener, OnMmiAirTaskCaseListener
{
	private ListView mListView;
	private AdapterTaskCase mListAdapter = null;
	private ProgressBar mProgressBar;
	
	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_task_case_list);
		doInitView();
	}
	
	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		AirTaskCaseManager.getInstance().setTaskCaseListener(null);
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		mListAdapter.notifyDataSetChanged();
		AirTaskCaseManager.getInstance().setTaskCaseListener(this);
		super.onResume();
	}
	
	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		if (Config.funcTask)
			ivTitle.setText(R.string.talk_tools_setting_task);
		else
			ivTitle.setText(R.string.talk_tools_setting_case);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setImageResource(R.drawable.btn_add_member);
		if (Config.funcTask)
			ivRightLay.setVisibility(View.INVISIBLE);
		else
			ivRightLay.setOnClickListener(this);

		mListAdapter = new AdapterTaskCase(this, !Config.funcTask);
		mListView = (ListView)findViewById(R.id.talk_task_list);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(this);

		mProgressBar = (ProgressBar) findViewById(R.id.talk_task_loading);
		if (Config.funcTask)
		{
			boolean isReady = AirTaskCaseManager.getInstance().TaskListGet();
			if (!isReady)
				mProgressBar.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
			case R.id.talk_menu_right_button: {
				Intent it = new Intent(this, MenuTaskCaseDetailActivity.class);
				it.putExtra(MenuTaskCaseDetailActivity.PARAM_MODE, MenuTaskCaseDetailActivity.MODE_NEW);
				startActivity(it);
				break;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		switch (parent.getId())
		{
			case R.id.talk_task_list:
			{
				AirTaskCase task = (AirTaskCase)mListAdapter.getItem(position);
				if (task != null)
				{
					Intent it = new Intent(this, MenuTaskCaseDetailActivity.class);
					it.putExtra(MenuTaskCaseDetailActivity.PARAM_TASK_ID, task.getTaskId());
					it.putExtra(MenuTaskCaseDetailActivity.PARAM_MODE, MenuTaskCaseDetailActivity.MODE_VIEW);
					startActivity(it);
				}
			}
			break;
		}
	}

	@Override
	public void onTaskCaseListGet(boolean isOk, List<AirTaskCase> tasks) {
		mListAdapter.notifyDataSetChanged();
		mProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void onTaskCaseCreated(boolean isOk, AirTaskCase task) {

	}

	@Override
	public void onTaskCaseUpdated(boolean isOk, AirTaskCase task) {

	}

	@Override
	public void onTaskCaseDeleted(boolean isOk, AirTaskCase task) {

	}
}

