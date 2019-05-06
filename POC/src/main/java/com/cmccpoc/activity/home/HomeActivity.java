package com.cmccpoc.activity.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMediaVisualizer;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.entity.AirVideoShare;
import com.cmccpoc.R;
import com.cmccpoc.activity.AllMemberActivity;
import com.cmccpoc.activity.FirstActivity;
import com.cmccpoc.activity.MenuAboutActivity;
import com.cmccpoc.activity.MenuAccountActivity;
import com.cmccpoc.activity.MenuNoticeActivity;
import com.cmccpoc.activity.MenuTaskCaseDetailActivity;
import com.cmccpoc.activity.MoreActivity;
import com.cmccpoc.activity.PttSettingsActivity;
import com.cmccpoc.activity.SessionAddActivity;
import com.cmccpoc.activity.SettingActivity;
import com.cmccpoc.activity.TerminalStatusActivity;
import com.cmccpoc.activity.VideoSessionActivity;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.activity.home.widget.CallAlertDialog;
import com.cmccpoc.activity.home.widget.CallCenterDialog;
import com.cmccpoc.activity.home.widget.MediaStatusBar;
import com.cmccpoc.activity.home.widget.SessionAndChannelView;
import com.cmccpoc.activity.home.widget.SessionAndChannelView.ViewChangeListener;
import com.cmccpoc.activity.home.widget.StatusBarTitle;
import com.cmccpoc.activity.home.widget.ToastUtils;
import com.cmccpoc.activity.home.widget.VideoList;
import com.cmccpoc.activity.home.widget.VideoListSelectListener;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.control.AirVideoManager;
import com.cmccpoc.listener.OnMmiLocationListener;
import com.cmccpoc.listener.OnMmiVideoListener;
import com.cmccpoc.location.AirLocation;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.DensityUtil;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.Util;
import com.cmccpoc.widget.PageIndicator;
import com.cmccpoc.widget.SlidingUpPanelLayout;
import com.cmccpoc.widget.SlidingUpPanelLayout.PanelSlideListener;
import com.cmccpoc.widget.SlidingUpPanelLayout.PanelState;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主界面 登录之后首先呈现出来的Activity，中间为PTT，左侧为频道成员，右侧为IM消息，顶部下来为频道与会话列表
 * 
 * @author Yao
 */
public class HomeActivity extends BaseActivity implements /*PanelSlideListener,*/
		/*OnPageChangeListener,*/ ViewChangeListener, VideoListSelectListener, OnMmiVideoListener/*,MenuItem.OnMenuItemClickListener*/
{
	//private AirSession session;
	//private PageFragmentAdapter adapter;
	public /*SlidingUpPanelLayout*/LinearLayout mLayout;
	private SessionAndChannelView channelView;
	private LinearLayout contaner;
	private ImageView ivIMNew, ivIMPoint;
	private MediaStatusBar mediaStatusBar;
	private boolean isChannel = true;
	public static boolean isShowing = false;

	private VideoList mVideoList;

	private static HomeActivity mInstance;
	private PopupMenu mPopMenuMore;
	private Button mMenu,mSettings;
	private boolean shortPress = false;
	/**
	 * 获取HomeActivity实例
	 * 
	 * @return
	 */
	public static HomeActivity getInstance()
	{
		return mInstance;
	}

	/**
	 * 获取当前会话
	 * 
	 * @return 会话Entity
	 */
	public AirSession getSession()
	{
		return session;
	}

	/**
	 * 设置session会话
	 * 
	 * @param session
	 *            会话Entity
	 */
	public void setSession(AirSession session)
	{
		this.session = session;
	}

	public void setMediaStatusBarSession(AirSession session)
	{
		setSession(session);
		mediaStatusBar.setSession(session);
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
		com.airtalkee.sdk.util.Log.i(HomeActivity.class, "HomeActivity onCreate");
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		toMarquee();
		session = AirSessionControl.getInstance().getCurrentSession();
		mInstance = this;
		setContentView(R.layout.activity_home);
		setRequestedOrientation(Config.screenOrientation);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mediaStatusBar = (MediaStatusBar) findViewById(R.id.media_status_function_bar);
		mediaStatusBar.init((StatusBarTitle) findViewById(R.id.media_status_title_bar), session);
		if (Config.model.equals("6233"))
		{
			mediaStatusBar.getLayoutParams().height = 120;
		}
		ivIMNew = (ImageView) findViewById(R.id.iv_im_new);
		this.ivIMPoint = (ImageView) findViewById(R.id.iv_im_point);
		/*this.viewPager = (ViewPager) findViewById(R.id.home_activity_page_content);
		this.adapter = new PageFragmentAdapter(this, fm);
		this.viewPager.setAdapter(this.adapter);
		this.viewPager.setOnPageChangeListener(this);
		this.viewPager.setOffscreenPageLimit(3);
		this.mPageIndicator = (PageIndicator) findViewById(R.id.indicator);
		this.mPageIndicator.setViewPager(viewPager);*/

		DensityUtil.initScreen(this);
		int height = DensityUtil.getHeight(this) - DensityUtil.getStatusHeight(this) - 150;
		mLayout = (LinearLayout) findViewById(R.id.sliding_layout);
		//mLayout.setParalaxOffset(height);
		//mLayout.setPanelSlideListener(this);
		contaner = (LinearLayout) findViewById(R.id.sliding_layout_contaner);

		channelView = new SessionAndChannelView(this, this);
		contaner.addView(channelView);
		channelView.setVisibility(View.GONE);

		mMenu= (Button) findViewById(R.id.menu);
		mSettings= (Button) findViewById(R.id.settings);

		// slidingBack = (ImageView)
		// channelView.findViewById(R.id.sliding_back);
		if (null != session)
		{
			checkNewIM(false, session);
		}
		if (AirSessionControl.getInstance().getCurrentChannelSession() != null)
		{
			com.airtalkee.sdk.util.Log.d(HomeActivity.class, "HomeActivity onResumeFragments set setRoleAppling in");
			AirChannel channel = AirSessionControl.getInstance().getCurrentChannelSession().getChannel();
			channel.setRoleAppling(true);
		}

		Intent intent = getIntent();
		if (intent != null)
		{
			boolean isCheck = intent.getBooleanExtra("notice", false);
			if (isCheck)
			{
				mHandler.sendEmptyMessageDelayed(0, 1000);
			}
		}

		mVideoList = new VideoList(this, this);
		if (Config.funcVideoPlay)
		{
			mVideoList.setVisible(true);
			mVideoList.viewRefresh();
		}
		else
			mVideoList.setVisible(false);
	}
	
	private Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			if (Config.funcBroadcast && AirtalkeeAccount.getInstance().SystemBroadcastNumberGet() > 0)
			{
				com.cmccpoc.activity.home.widget.AlertDialog dialog = new com.cmccpoc.activity.home.widget.AlertDialog(HomeActivity.this, "系统通知", "当前有" + AirtalkeeAccount.getInstance().SystemBroadcastNumberGet() + "条未读广播", "取消", "点击查看", new DialogListener()
				{
					@Override
					public void onClickOk(int id, boolean isChecked)
					{
					}

					@Override
					public void onClickOk(int id, Object obj)
					{
						Intent noticeIt = new Intent(mInstance, MenuNoticeActivity.class);
						noticeIt.putExtra("url", AccountController.getDmWebNoticeUrl());
						startActivity(noticeIt);
					}

					@Override
					public void onClickCancel(int id)
					{
					}
				}, -1);
				//dialog.show(); M zlm 去掉文字广播显示;
			}
		};
	};

	/**
	 * 解决 部分机型的textView设置android:ellipsize="marquee"后 仍会显示省略号
	 */
	private void toMarquee()
	{
		ViewConfiguration configuration = ViewConfiguration.get(this);
		Class claz = configuration.getClass();
		try
		{
			Field field = claz.getDeclaredField("mFadingMarqueeEnabled");
			field.setAccessible(true);
			field.set(configuration, true);
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void finish()
	{
		super.finish();
		if (!isChannel)
		{
			AirtalkeeMessage.getInstance().MessageListMoreClean(session);
		}
		if (channelView != null)
		{
			channelView.unRegisterReceiver();
		}
		AirtalkeeMediaVisualizer.getInstance().setOnMediaAudioVisualizerListener(null);
	}

	/**
	 * 滑动
	 */
	/*@Override
	public void onPanelSlide(View panel, float slideOffset)
	{
		if (channelView.getVisibility() == View.GONE)
			channelView.setVisibility(View.VISIBLE);
	}*/

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		com.airtalkee.sdk.util.Log.i(HomeActivity.class, "HomeActivity onPause in");
		if (Config.funcVideoPlay)
		{
			AirVideoManager.getInstance().setVideoListener(null);
		}
		isShowing = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 展开
	 */
	/*@Override
	public void onPanelExpanded(View panel)
	{
		Log.i("HOME_ACTIVITY", "onPanelExpanded");
		isShowing = false;
		contaner.setBackgroundColor(0xff222222);
		// slidingBack.setVisibility(View.VISIBLE);
		channelView.resume();
		channelView.setVisibility(View.VISIBLE);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)finish;
		if (imm.isActive())
			imm.hideSoftInputFromWindow(mediaStatusBar.getBottomBarParent().getWindowToken(), 0);
		IMFragment.getInstance().textPannel.setVisibility(View.GONE);
	}*/

	/**
	 * 收起
	 */
	/*@Override
	public void onPanelCollapsed(View panel)
	{
		channelView.setVisibility(View.GONE);
		isShowing = true;
		panelCollapsed();
	}*/

	/**
	 * 收起
	 */
	public void panelCollapsed()
	{
		Log.i("HOME_ACTIVITY", "onPanelCollapsed");
		contaner.setBackgroundColor(0x00000000);
		// slidingBack.setVisibility(View.GONE);
		if (session != null)
		{
			if (mediaStatusBar != null)
				mediaStatusBar.setSession(session);
			// 解决刷新频道成员
			//MemberFragment memberFragment = (MemberFragment) adapter.getItem(0);
			if (isChannel)
			{
				//memberFragment.refreshMembers(session, session.getChannel().MembersGet());
			}
			else
			{
				//memberFragment.refreshMembers(session, session.getMemberAll());
			}
			/*PTTFragment.getInstance().refreshPlayback(session);
			this.onPageSelected(pageIndex);*/
			//adapter.notifyDataSetChanged();
			// 检测是否有新im消息
			checkNewIM(false, session);
			//mediaStatusBar.setMediaStatusBarVisibility(View.VISIBLE);

		}
		else
		{
			if (session != null && session.getSessionCode().startsWith("C"))
			{
				session = null;
			}
			mediaStatusBar.setSession(session);
			/*PTTFragment.getInstance().refreshPlayback(session);
			this.onPageSelected(pageIndex);*/
		}
		if (AirSessionControl.getInstance().getCurrentChannelSession() != null)
		{
			com.airtalkee.sdk.util.Log.d(HomeActivity.class, "HomeActivity onResumeFragments set setRoleAppling in");
			AirChannel channel = AirSessionControl.getInstance().getCurrentChannelSession().getChannel();
			channel.setRoleAppling(true);
		}
	}

	@Override
	protected void onResumeFragments()
	{
		// TODO Auto-generated method stub
		super.onResumeFragments();
		//this.onPageSelected(pageIndex);
	}

	/**
	 * 页面被选择时  modify wqq
	 */
	/*@Override
	public void onPageSelected(int page)
	{
		try
		{
			if (mediaStatusBar != null)
				mediaStatusBar.onPageChanged(page);
			if (mPageIndicator != null)
				mPageIndicator.onPageChanged(page);
			for (int i = 0; i < 3; i++)
			{
				if (null != adapter)
					if (i == page)
					{
						adapter.getItem(i).onResume();
					}
					else
					{
						adapter.getItem(i).onPause();
					}
			}
			pageIndex = page;
			viewPager.setCurrentItem(pageIndex);
			if (page == PAGE_IM)
			{
				checkNewIM(true, null);
				channelView.refreshChannelAndDialog();
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
				if (StatusBarTitle.getInstance() != null)
					StatusBarTitle.getInstance().refreshNewMsg();
				try
				{
					IMFragment.getInstance().setTextPannelVisiblity(View.GONE);
				}
				catch (Exception e)
				{}
			}
			else
			{
				// channelView.setVisibility(View.GONE);
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			}
			if (!Config.funcPTTButton)
				mediaStatusBar.enablePTTButton(View.GONE);
			else
				mediaStatusBar.enablePTTButton(View.VISIBLE);
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm.isActive())
				imm.hideSoftInputFromWindow(mediaStatusBar.getBottomBarParent().getWindowToken(), 0);
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2)
	{
		// TODO Auto-generated method stub

	}*/

	@Override
	public void onResume()
	{
		super.onResume();
		com.airtalkee.sdk.util.Log.i(HomeActivity.class, "HomeActivity onResume");
		Bundle bundle = null;
		if (getIntent() != null)
			bundle = getIntent().getExtras();
		if (bundle != null)
		{
			String sessionTag = bundle.getString("tag");
			if ("onMessageIncomingRecv".equals(sessionTag))
			{
				com.airtalkee.sdk.util.Log.i(HomeActivity.class, "HomeActivity onResume bundle not null");
				String sessionCode = bundle.getString("sessionCode");
				int sessionType = bundle.getInt("type", -1);
				if (sessionCode != null)
				{
					session = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode);
				}
				else
				{
					session = AirSessionControl.getInstance().getCurrentSession();
				}
				if (sessionType == AirServices.TEMP_SESSION_TYPE_MESSAGE)
				{
					// pageIndex = PAGE_IM;
					// this.onPageSelected(pageIndex);
				}
			}
		}
		if (mediaStatusBar != null)
		{
            session = AirSessionControl.getInstance().getCurrentSession();
			mediaStatusBar.setSession(session);
		}
		channelView.refreshChannelAndDialog();
		StatusBarTitle.getInstance().checkBrodcast();
		setIntent(null);

		if (Config.funcVideoPlay)
		{
			AirVideoManager.getInstance().setVideoListener(this);
			mVideoList.viewRefresh();
		}
		isShowing = true;
		Log.d("zlm","homeActivity...onresume");

	}

	public void refreshMenuButton() {
		if(session!=null) {
			if (session.getType() == AirSession.TYPE_DIALOG) {
				final AirSession airSession = SessionController.SessionMatchSpecial(AirtalkeeSessionManager.SPECIAL_NUMBER_DISPATCHER, getString(R.string.talk_tools_call_center));
				if(airSession.equals(session)){
					mMenu.setVisibility(View.GONE);
				}else{
					mMenu.setVisibility(View.VISIBLE);
				}
				mMenu.setText("成员");
				mSettings.setText("挂断");
			} else {
				mMenu.setVisibility(View.VISIBLE);
				mMenu.setText(R.string.main_menu);
				mSettings.setText(R.string.main_setting);
			}
		}
	}

	/***
	 * zuocy:当activity为singleTask模式，从其他activity中传回的intent接收不到intent 需要重写此方法
	 */
	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		setIntent(intent);
	}

	/*@Override  modify wqq
	public void onPanelAnchored(View panel)
	{
		Log.i("HOME_ACTIVITY", "onPanelAnchored");
	}

	@Override
	public void onPanelHidden(View panel)
	{
		Log.i("HOME_ACTIVITY", "onPanelHidden");
	}*/

	@Override
	public void onViewChanged(String sessionCode)
	{
		isChannel = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode).getType() == AirSession.TYPE_CHANNEL;
		if (isChannel)
		{
			session = AirSessionControl.getInstance().getCurrentChannelSession();
		}
		else
		{
			session = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode);
		}
		if (mLayout != null)
		{
			//mLayout.setPanelState(PanelState.COLLAPSED);
		}
	}

	@Override
	@Deprecated
	protected Dialog onCreateDialog(final int id)
	{
		// TODO Auto-generated method stub
		switch (id)
		{
			case R.id.talk_dialog_message_txt_send_fail:
			case R.id.talk_dialog_message_txt:
			{
				final ListAdapter items = mSimpleAdapter(this, IMFragment.menuArray, R.layout.account_switch_listitem, R.id.AccountNameView);
				return new AlertDialog.Builder(this).setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						removeDialog(id);
						if (items instanceof SimpleAdapter)
						{
							//getIMFragment().onListItemLongClick(id, whichButton);
						}
					}
				}).setOnCancelListener(new OnCancelListener()
				{
					@Override
					public void onCancel(DialogInterface dialog)
					{
						// TODO Auto-generated method stub
						removeDialog(id);
					}
				}).create();
			}
		}
		return super.onCreateDialog(id);
	}

	/**
	 * 获取IM消息Fragment实例
	 * 
	 * @return IMFragment实例  modify wqq
	 */
	/*private BaseFragment getIMFragment()
	{
		if (adapter != null)
		{
			return adapter.getItem(PAGE_IM);
		}
		return null;
	}*/

	/**
	 * 自定义适配器
	 * 
	 * @param context
	 *            上下文
	 * @param array
	 *            数据
	 * @param layout
	 *            layout
	 * @param id
	 *            id
	 * @return
	 */
	public SimpleAdapter mSimpleAdapter(Context context, String[] array, int layout, int id)
	{
		if (array == null)
			return null;
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		data.clear();
		for (int i = 0; i < array.length; i++)
		{
			Map<String, Object> listItem = new HashMap<String, Object>();
			listItem.put("accountName", array[i]);
			data.add(listItem);
		}
		return new SimpleAdapter(this, data, layout, new String[] { "accountName" }, new int[] { id });
	}

	@Override
	public void onVideoRecorderStart(int sessionId, int result) {

	}

	@Override
	public void onVideoRecorderStop(int sessionId) {

	}

	@Override
	public boolean onVideoRealtimeShareStart(AirVideoShare videoShare)
	{
		if (Config.funcVideoPlay)
		{
			mVideoList.viewRefresh();
		}
		return false;
	}

	@Override
	public void onVideoRealtimeShareStop(AirVideoShare videoShare)
	{
		if (Config.funcVideoPlay)
		{
			mVideoList.viewRefresh();
		}
	}

	/**
	 * 内部类：页面片段适配器
	 */
	/*class PageFragmentAdapter extends FragmentPagerAdapter
	{
		private List<BaseFragment> fragments = new ArrayList<BaseFragment>();

		public PageFragmentAdapter(Context ctx, FragmentManager fm)
		{
			super(fm);
			for (int i = 0; i < TABS.length; i++)
			{
				this.fragments.add(BaseFragment.newInstantiate(ctx, TABS[i].getName(), mediaStatusBar));
			}
		}

		@Override
		public BaseFragment getItem(int position)
		{
			return this.fragments.get(position);
		}

		@Override
		public int getCount()
		{
			return this.fragments.size();
		}
	}*/

	/**
	 * 检测是否有新im消息
	 * 
	 * @param toClean
	 *            是否清除所有未读消息
	 */
	public void checkNewIM(boolean toClean, AirSession mSession)
	{
		AirSession tmpSession = null;
		if (mSession == null)
			tmpSession = session;
		else
			tmpSession = mSession;
		int count = 0;
		try
		{
			if (tmpSession != null)
			{
				int type = tmpSession.getType();
				if (type == AirSession.TYPE_CHANNEL)
				{
					AirChannel channel = AirtalkeeChannel.getInstance().ChannelGetByCode(tmpSession.getSessionCode());
					if (channel != null)
					{
						if (toClean)
						{
							channel.msgUnReadCountClean();
						}
						count = channel.getMsgUnReadCount();
					}
					channelView.refreshChannel();
				}
				else if (type == AirSession.TYPE_DIALOG)
				{
					if (toClean)
					{
						if (tmpSession.getMessageUnreadCount() > 0)
						{
							AirServices.getInstance().dbProxy().SessionDbCleanUnread(tmpSession.getSessionCode());
						}
						tmpSession.setMessageUnreadCount(0);
					}

					count = tmpSession.getMessageUnreadCount();
				}
			}
			if (count > 0 && mSession == session)
			{
				ivIMNew.setVisibility(View.VISIBLE);
				ivIMPoint.setVisibility(View.VISIBLE);
			}
			else
			{
				ivIMNew.setVisibility(View.GONE);
				ivIMPoint.setVisibility(View.GONE);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void refreshChannel()
	{
		channelView.refreshChannel();
	}
/**modify by wq*/
	/*public void refreshLocationShareView()
	{
		((IMFragment) adapter.getItem(PAGE_IM)).refreshLocationShareView();
	}*/

	@Override
	public void onVideoListSelect(AirVideoShare videoShare)
	{
		if (Config.funcVideoPlay && session != null)
		{
			Intent i = new Intent();
			i.setClass(this, VideoSessionActivity.class);
			i.putExtra("sessionCode", session.getSessionCode());
			startActivity(i);
		}
	}

	@Override
	public void onVideoListNoSelect()
	{
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater=getMenuInflater();
		menuInflater.inflate(R.menu.more_settings_menu,menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem itemCall,itemChannelLock;
		itemCall=menu.findItem(R.id.call_end);
		itemChannelLock=menu.findItem(R.id.channel_lock);
		try{
			if(session!=null) {
				if (session.getType() == AirSession.TYPE_DIALOG) {
					itemChannelLock.setVisible(false);
					itemCall.setVisible(true);
				} else {
					itemChannelLock.setVisible(true);
					itemCall.setVisible(false);
					if (session.isVoiceLocked()) {
						itemChannelLock.setTitle("解锁频道");
					} else {
						itemChannelLock.setTitle("频道锁");
					}
				}
			}else{
				itemCall.setVisible(false);
				itemChannelLock.setVisible(false);
			}
		}catch (Exception e){}

		return true;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		Log.i("wqq","wqq-----menu1111");
	    switch (item.getItemId()){
           /* case R.id.more_button:
                Intent intent=new Intent(HomeActivity.this,MoreActivity.class);
                startActivity(intent);
                break;*/
            case R.id.call_end:
                if (session != null && session.getType() == AirSession.TYPE_DIALOG)
                {
                    if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG || session.getSessionState() == AirSession.SESSION_STATE_CALLING)
                        AirSessionControl.getInstance().SessionEndCall(session);
                    session = AirSessionControl.getInstance().getCurrentChannelSession();
                    setSession(session);
                    onResume();
                }
                break;
            case R.id.channel_lock:

                if (session != null && session.getType() == AirSession.TYPE_CHANNEL && session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
                {
                    mediaStatusBar.barTitle.updateStatusBar();
                }
                break;
        }
        return true;
    }


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(event.getKeyCode()==KeyEvent.KEYCODE_MENU ){
			mMenu.setBackgroundResource(R.drawable.bg_list_focuse);
		}else if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
			mSettings.setBackgroundResource(R.drawable.bg_list_focuse);
		}else if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_CENTER){
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				event.startTracking();
				if (event.getRepeatCount() == 0) {
					shortPress = true;
				}
				return true;
			}
		}
		return super.onKeyDown(keyCode,event);
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			shortPress = false;
			callStationCenter(AirtalkeeSessionManager.SPECIAL_NUMBER_DISPATCHER, false);
			return true;
		}
		return super.onKeyLongPress(keyCode, event);

	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode){
			case KeyEvent.KEYCODE_MENU:
				mMenu.setBackgroundResource(R.drawable.bg_list_normal);
			//	if(session!=null) {
					if (session!=null && session.getType() == AirSession.TYPE_DIALOG) {

						Log.d("zlm","session==="+session.getMemberAll().size() +"..."+session.getMemberAll().get(0).getDisplayName());
						Intent intent=new Intent();
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
						intent.setClass(HomeActivity.this, MemberListActivity.class);
						startActivity(intent);
					} else {
						Intent it = new Intent(this, FirstActivity.class);
						startActivity(it);
					}
			//	}
				return true;
			case KeyEvent.KEYCODE_BACK:
                mSettings.setBackgroundResource(R.drawable.bg_list_normal);
				//if(session!=null) {
					if (session!=null && session.getType() == AirSession.TYPE_DIALOG) {
						session = AirSessionControl.getInstance().getCurrentSession();
						AirSessionControl.getInstance().SessionEndCall(session);
						session = AirSessionControl.getInstance().getCurrentSession();
						setSession(session);
						setMediaStatusBarSession(session);
						//onResume();
					} else {
						Intent mInetnt = new Intent(this, SettingActivity.class);
						startActivity(mInetnt);
					}
			//	}
				return true;
			case KeyEvent.KEYCODE_DPAD_CENTER:
				if (shortPress) {
					//Toast.makeText(this, "shortPress", Toast.LENGTH_LONG).show();
				} else {
					//Don't handle longpress here, because the user will have to get his finger back up first
				}
				shortPress = false;
				return true;

		}
		return super.onKeyUp(keyCode, event);
	}

	//一键呼叫调度台
	private void callStationCenter(int specialNumber, boolean withVideo)
	{
		if (Config.funcCenterCall == AirFunctionSetting.SETTING_ENABLE)
		{
			if (AirtalkeeAccount.getInstance().isAccountRunning())
			{
				if (AirtalkeeAccount.getInstance().isEngineRunning())
				{
					AirLocation.getInstance(this).onceGet(new OnMmiLocationListener()
					{

						@Override
						public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time, String address)
						{
							// TODO Auto-generated method stub
						}

						@Override
						public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time)
						{
							// TODO Auto-generated method stub
						}
					}, 20);

					final AirSession s = SessionController.SessionMatchSpecial(specialNumber, getString(R.string.talk_tools_call_center));
					if (s != null)
					{
						CallAlertDialog alertDialog = new CallAlertDialog(this, "正在呼叫"+getString(R.string.talk_call_center_text)+"..." /*+ s.getDisplayName()*/, "请稍后...", s.getSessionCode(), PTTFragment.DIALOG_CALL_CENTER, withVideo, new CallAlertDialog.OnAlertDialogCancelListener()
						{
							@Override
							public void onDialogCancel(int reason)
							{
								// TODO Auto-generated method stub
								switch (reason)
								{
									case AirSession.SESSION_RELEASE_REASON_NOTREACH:
									{
										/*com.cmccpoc.activity.home.widget.AlertDialog dialog = new com.cmccpoc.activity.home.widget.AlertDialog(HomeActivity.this, null,
												getString(R.string.talk_call_offline_tip),
												getString(R.string.talk_session_call_cancel),
												getString(R.string.talk_call_leave_msg),
												null,
												PTTFragment.DIALOG_2_SEND_MESSAGE,
												s.getSessionCode());
										dialog.show();*/
										ToastUtils.showCustomImgToast(getString(R.string.call_center_fail),R.drawable.ic_out_line ,HomeActivity.this);
										break;
									}
									case AirSession.SESSION_RELEASE_REASON_REJECTED:
										ToastUtils.showCustomImgToast(getString(R.string.other_refuse_call),R.drawable.ic_out_line , HomeActivity.this);
										break;
									case AirSession.SESSION_RELEASE_REASON_ERROR:
										ToastUtils.showCustomImgToast(getString(R.string.call_limit),R.drawable.ic_warning ,HomeActivity.this);
										break;
									default:
										break;
								}
							}
						});
						alertDialog.show();
					}
				}
				else
				{
					Util.Toast(this, getString(R.string.talk_network_warning));
				}
			}
		}
	}

}
