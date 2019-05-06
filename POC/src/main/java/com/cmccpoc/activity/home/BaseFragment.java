package com.cmccpoc.activity.home;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.activity.home.widget.MediaStatusBar;
import com.cmccpoc.activity.home.widget.StatusBarBottom;
import com.cmccpoc.activity.home.widget.StatusBarTitle;

/**
 * 片段的基类，为三大块Fragment提供了共同的方法与变量等
 * @author Yao
 */
public abstract class BaseFragment extends Fragment implements OnSharedPreferenceChangeListener
{

	public static final String SESSION_EVENT_KEY = "session_event_key";

	/**
	 * 抽象方法 获取Layout的Id
	 * @return
	 */
	public abstract int getLayout();

	/**
	 * 抽象方法 为每个Fragment底部，三个不同的按钮定义了方法
	 * @param page pageIndex
	 * @param id 按钮的ViewId
	 */
	public abstract void dispatchBarClickEvent(int page, int id);

	protected View v;

	protected static MediaStatusBar mediaStatusBar;

	protected SharedPreferences sessionSp;

	/**
	 * 获取Fragment实例
	 * @param context 上下文
	 * @param name 名称
	 * @param view mediaStatusBar实例
	 * @return
	 */
	public static BaseFragment newInstantiate(Context context, String name, MediaStatusBar view)
	{
		mediaStatusBar = view;
		return (BaseFragment) Fragment.instantiate(context, name);
	}

	/**
	 * 获取当前Session会话
	 * @return
	 */
	protected AirSession getSession()
	{
		if (null == mediaStatusBar)
			return null;
		return mediaStatusBar.getSession();
	}

	/**
	 * 获取顶部自定义控件StatusBarTitle实例对象
	 * @return
	 */
	protected StatusBarTitle getStatusBarTitle()
	{
		return mediaStatusBar.getStatusBarTitle();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		registerOnBarClickReceiver();
	}

	@Override
	public void onAttach(Activity activity)
	{
		// TODO Auto-generated method stub
		super.onAttach(activity);
		this.sessionSp = activity.getSharedPreferences(SESSION_EVENT_KEY, 0);
		this.sessionSp.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onDetach()
	{
		// TODO Auto-generated method stub
		super.onDetach();
		// this.sessionSp.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		unRegisterOnBarClickReceiver();
	}

	@Override
	public void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
	}

	public View findViewById(int id)
	{
		return v.findViewById(id);
	}

	/**
	 * 注册按钮点击接收器
	 */
	protected void registerOnBarClickReceiver()
	{
		final IntentFilter filter = new IntentFilter();
		filter.addAction(StatusBarBottom.ACTION_BAR_ITEMCLICK);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		getActivity().registerReceiver(receiver, filter);
	}

	BroadcastReceiver receiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// TODO Auto-generated method stub
			if (intent.getAction().equals(StatusBarBottom.ACTION_BAR_ITEMCLICK))
			{
				int id = intent.getIntExtra(StatusBarBottom.EXTRA_ID, 0);
				int page = intent.getIntExtra(StatusBarBottom.EXTRA_PAGE, 0);
				dispatchBarClickEvent(page, id);
			}
		}
	};

	/**
	 * 取消注册点击事件
	 */
	public void unRegisterOnBarClickReceiver()
	{
		getActivity().unregisterReceiver(receiver);
	}

	/**
	 * 抽象方法 列表项长按
	 * @param id 控件Id
	 * @param selectedItem 被选项
	 */
	public abstract void onListItemLongClick(int id, int selectedItem);

}
