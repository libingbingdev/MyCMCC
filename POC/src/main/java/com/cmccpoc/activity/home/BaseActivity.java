package com.cmccpoc.activity.home;

import java.util.LinkedHashMap;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.activity.home.widget.SessionAndChannelView;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirAccountManager;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.listener.OnMmiAccountListener;
import com.cmccpoc.listener.OnMmiChannelListener;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.widget.PageIndicator;
//import com.hdqy.android.telephony.HdqyInfoManager;

/**
 * Activity的基类，在这定义了一些基本的方法和通用的变量
 * @author Yao
 */
public class BaseActivity extends FragmentActivity implements OnMmiAccountListener, OnMmiChannelListener
{
	public static final int PAGE_MEMBER = 0;
	public static final int PAGE_PTT = 1;
	public static final int PAGE_IM = 2;
	protected final FragmentManager fm = getSupportFragmentManager();

	protected static final Class<?>[] TABS = {
	/* 0 */MemberFragment.class,
	/* 1 */PTTFragment.class,
	/* 2 */IMFragment.class, };

	protected ViewPager viewPager;
	protected PageIndicator mPageIndicator;

	private static BaseActivity mInstance;
	protected AirSession session;

	/**
	 * 获取Activity实例
	 * @return
	 */
	public static BaseActivity getInstance()
	{
		return mInstance;
	}

	public int pageIndex = PAGE_PTT;
	protected int actionType;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		AirAccountManager.getInstance().setAccountListener(this);
		mInstance = this;

	}

	@Override
	protected void onResume() {
		super.onResume();
		session = AirSessionControl.getInstance().getCurrentSession();
	}

	/**
	 * 是否需要隐藏软键盘
	 * @param v 
	 * @param event
	 * @return
	 */
	public boolean isShouldHideInput(View v, MotionEvent event)
	{
		if (v != null)
		{
			int[] leftTop = { 0, 0 };
			v.getLocationInWindow(leftTop);
			int left = leftTop[0];
			int top = leftTop[1];
			int bottom = top + v.getHeight();
			int right = left + v.getWidth();
			if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		session = AirSessionControl.getInstance().getCurrentSession();
		if (session != null)
		{
			if (session.getType() == AirSession.TYPE_CHANNEL)
			{
				if (event.getKeyCode() == Config.pttButtonKeycode || (Config.pttVolumeKeySupport && (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN))||(event.getKeyCode()==KeyEvent.KEYCODE_STAR))
				{
					if (AirtalkeeAccount.getInstance().isEngineRunning() && AirSessionControl.getInstance().getCurrentChannelSession() != null && AirSessionControl.getInstance().getCurrentChannelSession().getChannel() != null)
					{
						if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0)
						{
							sendBrodCast();//政企终端接口
							AirtalkeeSessionManager.getInstance().TalkRequest(AirSessionControl.getInstance().getCurrentChannelSession(), AirSessionControl.getInstance().getCurrentChannelSession().getChannel().isRoleAppling());
						}
						else if (event.getAction() == KeyEvent.ACTION_UP)
						{
							AirtalkeeSessionManager.getInstance().TalkRelease(AirSessionControl.getInstance().getCurrentChannelSession());
						}
					}
					return true;
				}
			}
			else if (session.getType() == AirSession.TYPE_DIALOG)
			{
				if (Config.pttVolumeKeySupport || event.getKeyCode()==KeyEvent.KEYCODE_STAR)
				{
					if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN || event.getKeyCode()==KeyEvent.KEYCODE_STAR)
					{
						if (AirtalkeeAccount.getInstance().isEngineRunning())
						{
							if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0)
							{
								sendBrodCast();//政企终端接口
								AirtalkeeSessionManager.getInstance().TalkRequest(session);
							}
							else if (event.getAction() == KeyEvent.ACTION_UP)
							{
								AirtalkeeSessionManager.getInstance().TalkRelease(session);

							}
						}
						return true;
					}
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}

	//政企终端接口
	private void sendBrodCast(){
		Log.d("zlmm","sendBrodCast..PTT");
		//Intent intent =new Intent(HdqyInfoManager.ACTION_KEY_CLICK);
		//intent.putExtra("keyType", "PTT");
		//sendBroadcast(intent);
	}


	@Override
	public void finish()
	{
		super.finish();
		AirAccountManager.getInstance().setAccountListener(null);
	}

	@Override
	public void onMmiHeartbeatLogin(int result)
	{
		if (result != 0)
		{
			if (result == AirtalkeeAccount.ACCOUNT_RESULT_ERR_SINGLE)
			{
				AirServices.iOperator.putString(AirAccountManager.KEY_ID, "");
				AirServices.iOperator.putString(AirAccountManager.KEY_PWD, "");
				new AlertDialog(this, getString(R.string.talk_account_other), getString(R.string.talk_exit), new DialogListener()
				{
					@Override
					public void onClickOk(int id, boolean isChecked)
					{
						// TODO Auto-generated method stub
					}

					@Override
					public void onClickOk(int id, Object obj)
					{
						System.exit(0);
					}

					@Override
					public void onClickCancel(int id)
					{
						System.exit(0);
					}
				}, false).show();
			}
			else
			{

			}
		}
	}

	@Override
	public void onMmiHeartbeatLogout()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onMmiHeartbeatException(int result)
	{
//		if (result == AirtalkeeAccount.ACCOUNT_RESULT_ERR_SINGLE)
//		{
//			new AlertDialog(this, getString(R.string.talk_account_other), getString(R.string.talk_exit), new DialogListener()
//			{
//				@Override
//				public void onClickOk(int id, boolean isChecked)
//				{
//					// TODO Auto-generated method stub
//				}
//
//				@Override
//				public void onClickOk(int id, Object obj)
//				{
//					System.exit(0);
//				}
//
//				@Override
//				public void onClickCancel(int id)
//				{
//					System.exit(0);
//				}
//			}, false).show();
//		}
	}

	@Override
	public void onChannelListGet(boolean isOk, List<AirChannel> channels)
	{
		try
		{
			SessionAndChannelView.getInstance().refreshChannelAndDialog();
		}
		catch (Exception e)
		{}
	}

	@Override
	public void onChannelMemberListGet(String channelId, List<AirContact> members)
	{
		if (HomeActivity.getInstance() != null)
		{
			try
			{
				HomeActivity.getInstance().refreshChannel();
			}
			catch (Exception e)
			{}
		}
	}

	@Override
	public void onChannelOnlineCount(LinkedHashMap<String, Integer> online)
	{

	}

	@Override
	public void onChannelPersonalCreateNotify(AirChannel ch)
	{
		try
		{
			MemberFragment.getInstance().refreshMembers(ch);
			MemberFragment.getInstance().refreshAllMembers();
		}
		catch (Exception e)
		{}
	}

	@Override
	public void onChannelPersonalDeleteNotify(AirChannel ch)
	{
		try
		{
			MemberFragment.getInstance().refreshMembers(ch);
			MemberFragment.getInstance().refreshAllMembers();
		}
		catch (Exception e)
		{}
	}

	@Override
	public void onChannelMemberAppendNotify(AirChannel ch, List<AirContact> members)
	{
		try
		{
			MemberFragment.getInstance().refreshMembers(ch);
		}
		catch (Exception e)
		{}
	}

	@Override
	public void onChannelMemberDeleteNotify(AirChannel ch, List<AirContact> members)
	{
		try
		{
			MemberFragment.getInstance().refreshMembers(ch);
		}
		catch (Exception e)
		{}
	}

	@Override
	public void onChannelMemberUpdateNotify(AirChannel ch, List<AirContact> members)
	{
		try
		{
			MemberFragment.getInstance().refreshMembers(ch);
		}
		catch (Exception e)
		{}
	}

}
