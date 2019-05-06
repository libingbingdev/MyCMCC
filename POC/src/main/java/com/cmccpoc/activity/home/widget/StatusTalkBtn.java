package com.cmccpoc.activity.home.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.IMFragment;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.AirMmiTimer;
import com.cmccpoc.util.AirMmiTimerListener;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.Util;

import java.util.Date;
//import com.airtalkee.sdk.util.Log;

/**
 * PTT按键自定义控件 申请或释放话语权的时候，PTT按键根据不同的状态显示不同的图片 
 * @author Yao
 */
public class StatusTalkBtn extends LinearLayout implements OnTouchListener, AirMmiTimerListener
{
	private final int TIMEOUT_LONG_CLICK = 200;
	private AirSession session;
	private View btnTalk, textLay;
	private ImageView bgTalkBack, bgTalkFront;
	private TextView tvBold, tvNormal;
	private boolean isTalkLongClick = false;
	private AirChannel channel = null;


	public StatusTalkBtn(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		LayoutInflater.from(this.getContext()).inflate(R.layout.include_home_talkbtn, this);
	}

	/**
	 * 设置session会话
	 * @param s 会话Entity
	 */
	public void setSession(AirSession s)
	{
		this.session = s;
		if (s != null)
		{
			channel = AirtalkeeChannel.getInstance().ChannelGetByCode(s.getSessionCode());
		}
		refreshPttButton();
	}

	@Override
	protected void onFinishInflate()
	{
		// TODO Auto-generated method stub
		super.onFinishInflate();
		initFindView();
	}

	/**
	 * 初始化绑定控件
	 */
	private void initFindView()
	{
		btnTalk = findViewById(R.id.media_ptt_box);
		textLay = findViewById(R.id.media_talk_text_lay);
		bgTalkBack = (ImageView) findViewById(R.id.media_ptt_talk_press_bg);
		bgTalkFront = (ImageView) findViewById(R.id.media_ptt_talk_press_bg_front);
		tvBold = (TextView) findViewById(R.id.media_ptt_talk_text1);
		tvNormal = (TextView) findViewById(R.id.media_ptt_talk_text2);
		btnTalk.setOnTouchListener(this);
	}

	/**
	 * 刷新PTT按钮状态，根据不同状态显示不同的图片
	 */
	public void refreshPttButton()
	{
		if (session == null)
			return;
		//Log.d(StatusBarTitle.class, "session button state = " + session.getMediaButtonState());
		switch (session.getMediaButtonState())
		{
			case AirSession.MEDIA_BUTTON_STATE_IDLE:
				if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
				{
					bgTalkFront.setVisibility(View.VISIBLE);
					bgTalkBack.setVisibility(View.INVISIBLE);
					textLay.setVisibility(View.GONE);
					bgTalkFront.setImageResource(R.drawable.btn_talk_idle_new);
				}
				else
				{
					bgTalkFront.setVisibility(View.VISIBLE);
					bgTalkBack.setVisibility(View.INVISIBLE);
					textLay.setVisibility(View.GONE);
					bgTalkFront.setImageResource(R.drawable.btn_talk_disconnect);
					//if (session.getType() == AirSession.TYPE_DIALOG && Config.pttButtonVisibility == View.VISIBLE)
					{
//						btnTalkCall.setVisibility(View.VISIBLE);
//						btnTalkCall.setImageResource(ThemeUtil.getResourceId(R.attr.theme_talk_call_idle, contextMain));
					}
				}
//				refreshMediaState();

				break;
			case AirSession.MEDIA_BUTTON_STATE_CONNECTING:
				bgTalkFront.setVisibility(View.VISIBLE);
				bgTalkBack.setVisibility(View.INVISIBLE);
				textLay.setVisibility(View.VISIBLE);
				bgTalkFront.setImageResource(R.drawable.btn_talk_empy);
				tvBold.setVisibility(View.VISIBLE);
				tvNormal.setVisibility(View.VISIBLE);
				tvBold.setText("连接中");
				tvNormal.setText("...");
				//if (session.getType() == AirSession.TYPE_DIALOG && Config.pttButtonVisibility == View.VISIBLE)
				{
//					btnTalkCall.setVisibility(View.VISIBLE);
//					btnTalkCall.setImageResource(ThemeUtil.getResourceId(R.attr.theme_talk_call_ing, contextMain));
				}
				break;
			case AirSession.MEDIA_BUTTON_STATE_TALKING:
				bgTalkFront.setVisibility(View.GONE);
				bgTalkBack.setVisibility(View.VISIBLE);
				textLay.setVisibility(View.GONE);
				break;
			case AirSession.MEDIA_BUTTON_STATE_QUEUE:
				bgTalkFront.setVisibility(View.VISIBLE);
				bgTalkBack.setVisibility(View.INVISIBLE);
				textLay.setVisibility(View.VISIBLE);
				bgTalkFront.setImageResource(R.drawable.btn_talk_empy);
				tvBold.setVisibility(View.VISIBLE);
				tvNormal.setVisibility(View.VISIBLE);
				//Log.d(StatusBarTitle.class, "queues size = " + session.usersQueues().size());
				tvBold.setText(session.usersQueues().size()+"");
				tvNormal.setText("排队中");
				break;
			case AirSession.MEDIA_BUTTON_STATE_REQUESTING:
				bgTalkFront.setVisibility(View.VISIBLE);
				bgTalkBack.setVisibility(View.INVISIBLE);
				textLay.setVisibility(View.VISIBLE);
				bgTalkFront.setImageResource(R.drawable.btn_talk_empy);
				tvBold.setVisibility(View.VISIBLE);
				tvNormal.setVisibility(View.VISIBLE);
				tvBold.setText("申请中");
				tvNormal.setText("...");
				break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_DPAD_CENTER) {
			Log.i("wqq","wqq->onTouch:KEYCODE_DPAD_CENTER");
		}
		return false;
	}




	private Date dateOld = null;
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		Log.i("wqq","wqq->onTouch:0000");
		switch (v.getId())
		{
			case R.id.media_ptt_box:
			{
				try
				{
					boolean isAction = false;
					if (v.getId() == R.id.talk_btn_session)
					{
						if (event.getAction() == MotionEvent.ACTION_DOWN)
						{
							Log.i("wqq","wqq->onTouch:11111");
							double d = Math.sqrt((btnTalk.getWidth() / 2 - event.getX()) * (btnTalk.getWidth() / 2 - event.getX()) + (btnTalk.getHeight() / 2 - event.getY()) * (btnTalk.getHeight() / 2 - event.getY()));
							if (btnTalk.getWidth() / 2 >= d)
							{
								isAction = true;
							}
						}
						else
							isAction = true;
					}
					else
						isAction = true;

					if (isAction && session != null)
					{
						Log.i("wqq","wqq->onTouch:222");
						if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
						{
							Log.i("wqq","wqq->onTouch:333");
							if (Config.pttClickSupport)
							{
								if (event.getAction() == MotionEvent.ACTION_DOWN)
								{
									Log.i("wqq","wqq->onTouch:444");
									isTalkLongClick = false;
									v.setPressed(true);
									AirMmiTimer.getInstance().TimerRegister(getContext(), this, false, false, TIMEOUT_LONG_CLICK, false, null);
								}
								else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
								{
									Log.i("wqq","wqq->onTouch:555");
									AirMmiTimer.getInstance().TimerUnregister(getContext(), this);
									if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
									{
										if (isTalkLongClick)
										{
											AirtalkeeSessionManager.getInstance().TalkRelease(session);
											isTalkLongClick = false;
											v.setPressed(false);
										}
										else
										{
											Log.i("wqq","wqq->onTouch:666");
											AirtalkeeSessionManager.getInstance().TalkButtonClick(session, channel != null ? channel.isRoleAppling() : false);
										}
									}
									isTalkLongClick = false;
								}
							}
							else
							{
								if (event.getAction() == MotionEvent.ACTION_DOWN)
								{
									Log.i("wqq","wqq->onTouch:777");
									AirtalkeeSessionManager.getInstance().TalkRequest(session, channel != null ? channel.isRoleAppling() : false);
								}
								else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
								{
									Log.i("wqq","wqq->onTouch:888");
									AirtalkeeSessionManager.getInstance().TalkRelease(session);
								}
							}
						}
						else if (session.getSessionState() == AirSession.SESSION_STATE_CALLING)
						{
							if (event.getAction() == MotionEvent.ACTION_DOWN)
							{
								Log.i("wqq","wqq->onTouch:999");
								Date dateNew = new Date();
								float seconds = (dateNew.getTime() - dateOld.getTime()) / 1000f;
								//Log.i(StatusTalkBtn.class, "StatusTalkBtn seconds ="+seconds);
								if(seconds < 0.5)
								{
									if(Toast.isDebug) Toast.makeText1(AirServices.getInstance(), "点击间隔太短", Toast.LENGTH_SHORT).show();
									return false;
								}
								if (session.getType() == AirSession.TYPE_DIALOG)
								{
									AirSessionControl.getInstance().SessionEndCall(session);
								}
							}
							else if (event.getAction() == MotionEvent.ACTION_UP) 
							{
								Log.i("wqq","wqq->onTouch:121212");
								dateOld = new Date();
							}
						}
						else if (session.getSessionState() == AirSession.SESSION_STATE_IDLE)
						{
							if (event.getAction() == MotionEvent.ACTION_DOWN)
							{
								if (session.getType() == AirSession.TYPE_DIALOG)
								{
									AirSessionControl.getInstance().SessionMakeCall(session);
									AirtalkeeMessage.getInstance().MessageSystemGenerate(session, getContext().getString(R.string.talk_call_state_outgoing_call), false);
									if (IMFragment.getInstance() != null)
									{
										IMFragment.getInstance().refreshMessages();
									}
								}
								else if (session.getType() == AirSession.TYPE_CHANNEL)
								{
									if (AirtalkeeAccount.getInstance().isEngineRunning())
									{
										AirSessionControl.getInstance().SessionChannelIn(session.getSessionCode());
									}
									else
									{
										Util.Toast(getContext(), getContext().getString(R.string.talk_network_warning));
										AirtalkeeAccount.getInstance().NetworkOpen();
									}
								}
							}
							else if (event.getAction() == MotionEvent.ACTION_UP) 
							{
								dateOld = null;
							}
						}
						return true;

					}
					Log.i("wqq","wqq->onTouch:131313");
					break;
				}
				catch (Exception e)
				{ }
			}
		}
		return false;
	}

	@Override
	public void onMmiTimer(Context context, Object userData)
	{
		try
		{
			isTalkLongClick = true;
			AirtalkeeSessionManager.getInstance().TalkRequest(session, channel != null ? channel.isRoleAppling() : false);
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}


}
