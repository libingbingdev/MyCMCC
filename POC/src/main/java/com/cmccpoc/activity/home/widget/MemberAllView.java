package com.cmccpoc.activity.home.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.activity.ActivityPresetGroup;
import com.cmccpoc.activity.home.MemberFragment;
import com.cmccpoc.activity.home.adapter.AdapterMemberAll;
import com.cmccpoc.activity.home.adapter.AdapterMemberAll.CheckedCallBack;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.util.Util;

/**
 * 全体成员 自定义View控件
 * 
 * @author Yao
 */
public class MemberAllView extends LinearLayout implements OnClickListener, OnItemClickListener, TextWatcher, CheckedCallBack
{
	public interface MemberCheckListener
	{
		/**
		 * 成员选择
		 * @param isChecked 是否选择
		 */
		public void onMemberChecked(boolean isChecked);
	}

	public List<AirContact> memberAll;
	List<AirContact> memberSearchResult = new ArrayList<AirContact>();
	private ListView lvMemberAll;
	public AdapterMemberAll adapterMember;
	CallAlertDialog alertDialog;
	private LinearLayout searchPannel;
	private Button btnSearch;
	private EditText etSearch;
	private ImageView ivSearch;
	private View popupView;
	private View broadcastPannel, presetGroupPannel;
	private View pttBroadcastButton;
	private TextView tvPttBroadcast, tvPttBroadcastState;
	private ImageView ivPttBroadcast, ivCloseWindow, ivPttBroadcastDoing;
	private Chronometer pttTimer;
	private ProgressBar pbPttBroadcast;
	private PopupWindow popupWindow;
	private Timer mTimer;
	private TimerTask mTimerTask;
	private int seconds = 0;

	AnimationDrawable anim;
	boolean isPttBroadcastStart = false;
	
	private AirSession gSession;
	private MemberCheckListener memberCheckListener;

	@SuppressWarnings("deprecation")
	public MemberAllView(Context context, MemberCheckListener memberCheckListener, boolean isPttBroadcast)
	{
		super(context);
		this.memberCheckListener = memberCheckListener;
		// AirtalkeeSessionManager.getInstance().setOnMediaListener(this);
		LayoutInflater.from(context).inflate(R.layout.layout_member_all, this);
	//	btnSearch = (Button) findViewById(R.id.btn_search);
		etSearch = (EditText) findViewById(R.id.et_search);

	//	btnSearch.setOnClickListener(this);
		etSearch.addTextChangedListener(this);
		memberAll = getAllAirContacts();
		lvMemberAll = (ListView) findViewById(R.id.talk_lv_member_all);
		adapterMember = new AdapterMemberAll(context, this);
		lvMemberAll.setOnItemClickListener(this);
		adapterMember.notifyMember(memberAll);
		lvMemberAll.setAdapter(adapterMember);
		adapterMember.notifyDataSetChanged();
		searchPannel = (LinearLayout) findViewById(R.id.serach_pannel);
		ivSearch = (ImageView) findViewById(R.id.iv_search);
		presetGroupPannel = findViewById(R.id.preset_group_pannel);
		presetGroupPannel.setOnClickListener(this);
		presetGroupPannel.setVisibility(View.GONE);
		findViewById(R.id.v_preset_group).setVisibility(View.GONE);
		broadcastPannel = findViewById(R.id.ptt_broadcast_panel);
		if (AccountController.getUserInfo().getFuncChatBroadcast() && isPttBroadcast)
		{
			findViewById(R.id.v_ptt_broadcast).setVisibility(View.VISIBLE);
			broadcastPannel.setVisibility(View.VISIBLE);
			broadcastPannel.setOnClickListener(this);
		}
		else 
		{
			findViewById(R.id.v_ptt_broadcast).setVisibility(View.GONE);
			broadcastPannel.setVisibility(View.GONE);
		}
		popupView = LayoutInflater.from(context).inflate(R.layout.layout_popup_window_ptt_broadcast, null);
		ivPttBroadcast = (ImageView) popupView.findViewById(R.id.iv_ptt_broadcast);
		ivCloseWindow = (ImageView) popupView.findViewById(R.id.iv_close_ptt_broadcast);
		ivCloseWindow.setOnClickListener(this);
		pbPttBroadcast = (ProgressBar) popupView.findViewById(R.id.pb_ptt_broadcast);
		pttBroadcastButton = popupView.findViewById(R.id.ptt_broadcast_button);
		pttBroadcastButton.setOnClickListener(this);
		popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setOnDismissListener(new OnDismissListener()
		{
			@Override
			public void onDismiss()
			{
				refreshPttBroadcastState(true);
			}
		});
		tvPttBroadcast = (TextView) popupView.findViewById(R.id.tv_ptt_broadcast);
		tvPttBroadcastState = (TextView) popupView.findViewById(R.id.tv_ptt_broadcast_state);
		pttTimer = (Chronometer) popupView.findViewById(R.id.ptt_broadcast_timer);
		ivPttBroadcastDoing = (ImageView) popupView.findViewById(R.id.iv_ptt_broadcast_doing);
		mTimer = new Timer();
		
		gSession = AirtalkeeSessionManager.getInstance().GroupBroadcastSession();


	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus)
	{
		super.onWindowFocusChanged(hasWindowFocus);
	}

	private void bgAlpha(float alpha)
	{
		WindowManager.LayoutParams lp = MemberFragment.getInstance().getActivity().getWindow().getAttributes();
		lp.alpha = alpha;
		MemberFragment.getInstance().getActivity().getWindow().setAttributes(lp);
	}

	/**
	 * 成员搜索layout
	 * 
	 * @return LinearLayout
	 */
	public LinearLayout getSearchPannel()
	{
		return searchPannel;
	}

	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			int msgId = msg.what;
			switch (msgId)
			{
				case 1:
					popupWindow.dismiss();
					Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_timeup));
					break;
				default:
					break;
			}
		}
	};

	/**
	 * 获取全体成员 将所有频道内的成员取并集
	 * @return 全体成员列表
	 */
	public List<AirContact> getAllAirContacts()
	{
		List<AirContact> contacts = new ArrayList<AirContact>();
		Map<String, AirContact> allMembers = new HashMap<String, AirContact>();
		List<AirChannel> channels = AirtalkeeChannel.getInstance().getChannels();
		if (channels != null && channels.size() > 0)
		{
			for (AirChannel channel : channels)
			{
				List<AirContact> members = channel.MembersGet();
				for (AirContact member : members)
				{
					//M zlm  获取在线成员
					int state = AirtalkeeContactPresence.getInstance().getContactStateById(member.getIpocId());
					if (state == AirContact.CONTACT_STATE_ONLINE_BG  || state == AirContact.CONTACT_STATE_ONLINE  ) {
						allMembers.put(member.getIpocId(), member);
					}
				}
			}
		}
		Iterator<Entry<String, AirContact>> iter = allMembers.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry<String, AirContact> entry = iter.next();
			contacts.add(entry.getValue());
		}
		Collections.sort(contacts, new Comparator<AirContact>()
		{
			@Override
			public int compare(AirContact member1, AirContact member2)
			{
				int result = 0;
				if (member1.chatSortSeed > member2.chatSortSeed)
					result = -1;
				else if (member1.chatSortSeed < member2.chatSortSeed)
					result = 1;
				return result;
			}
		});
		return contacts;
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			/*case R.id.btn_search:
			{
				Util.hideSoftInput(getContext());
				searchByKey();
				break;
			}*/
			case R.id.iv_search:
			{
				etSearch.setText("");
				break;
			}
			case R.id.ptt_broadcast_panel:
			{
				bgAlpha(0.3f);
				popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
				break;
			}
			case R.id.preset_group_pannel:
			{
				Intent it = new Intent(MemberFragment.getInstance().getActivity(), ActivityPresetGroup.class);
				MemberFragment.getInstance().getActivity().startActivity(it);
				break;
			}
			case R.id.iv_close_ptt_broadcast:
			{
				popupWindow.dismiss();
				break;
			}
			case R.id.ptt_broadcast_button:
			{
				if (isPttBroadcastStart)
				{
					popupWindow.dismiss();
				}
				else
				{
					refreshPttBroadcastState(isPttBroadcastStart);
				}
				break;
			}
		}
	}

	private void refreshPttBroadcastState(boolean isStart)
	{
		if (!isStart)
		{
			mTimerTask = new TimerTask()
			{
				@Override
				public void run()
				{
					if (seconds > 120)
					{
						Message message = new Message();
						message.what = 1;
						mHandler.sendMessage(message);
					}
					else 
					{
						pbPttBroadcast.setProgress(seconds);
						seconds++;
					}
				}
			};
			mTimer.schedule(mTimerTask, 0, 1000);
			ivPttBroadcast.setBackgroundResource(R.anim.ptt_broadcast);
			anim = (AnimationDrawable) ivPttBroadcast.getBackground();
			anim.start();
			pttBroadcastButton.setBackgroundResource(R.drawable.bg_ptt_broadcast_bottom_start);
			tvPttBroadcast.setText(R.string.talk_ptt_broadcast_stop);
			ivCloseWindow.setVisibility(View.INVISIBLE);
			ivPttBroadcastDoing.setVisibility(View.VISIBLE);
			pttTimer.setBase(SystemClock.elapsedRealtime());
			pttTimer.setVisibility(View.VISIBLE);
			pttTimer.start();
			tvPttBroadcastState.setText(R.string.talk_ptt_broadcast_doing);
			isPttBroadcastStart = true;
			pbPttBroadcast.setVisibility(View.VISIBLE);
			if (gSession != null)
			{
				AirtalkeeSessionManager.getInstance().TalkRequest(gSession, gSession.getChannel().isRoleAppling());
			}
		}
		else
		{
			bgAlpha(1f);
			try
			{
				mTimerTask.cancel();
				anim.stop();
			}
			catch (Exception e) { }
			seconds = 0;
			ivPttBroadcast.setBackgroundResource(R.drawable.ptt_broadcast3);
			pttBroadcastButton.setBackgroundResource(R.drawable.bg_ptt_broadcast_bottom_stop);
			tvPttBroadcast.setText(R.string.talk_ptt_broadcast_start);
			ivCloseWindow.setVisibility(View.VISIBLE);
			ivPttBroadcastDoing.setVisibility(View.INVISIBLE);
			pttTimer.setVisibility(View.INVISIBLE);
			pttTimer.stop();
			pttTimer.setBase(SystemClock.elapsedRealtime());
			tvPttBroadcastState.setText(R.string.talk_ptt_broadcast_idle);
			isPttBroadcastStart = false;
			pbPttBroadcast.setVisibility(View.GONE);
			AirtalkeeSessionManager.getInstance().TalkRelease(gSession);
		}
	}

	/**
	 * 根据key模糊搜索成员
	 */
	private void searchByKey()
	{
		Log.i(MemberAllView.class, "memberall size = " + memberAll.size());
		String key = etSearch.getText().toString().toLowerCase();
		memberSearchResult.clear();
		if (memberAll == null || memberAll.size() == 0)
		{
			memberAll = getAllAirContacts();
			lvMemberAll.setAdapter(adapterMember);
		}
		if (TextUtils.isEmpty(key))
			adapterMember.notifyMember(memberAll);
		else
		{
			for (int i = 0; i < memberAll.size(); i++)
			{
				AirContact contact = memberAll.get(i);
				String displayName = contact.getDisplayName().toLowerCase();
				String ipocId = contact.getIpocId().toLowerCase();
				if (displayName.equalsIgnoreCase(key) || ipocId.equals(key) || displayName.contains(key) || ipocId.contains(key))
				{
					memberSearchResult.add(contact);
				}
			}
			adapterMember.notifyMember(memberSearchResult);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		switch (parent.getId())
		{
			case R.id.talk_lv_member_all:
			{
				CheckBox cb = (CheckBox) view.findViewById(R.id.talk_cb_group_member);
				AirContact c = (AirContact) adapterMember.getItem(position );
				if (c != null)
				{
					if (!AirtalkeeAccount.getInstance().getUserId().equals(c.getIpocId()))
					{
						if (cb != null && cb.isEnabled())
							cb.setChecked(!cb.isChecked());
					}
				}
				break;
			}
		}

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		//btnSearch.setEnabled(!TextUtils.isEmpty(etSearch.getText()));
		if (TextUtils.isEmpty(etSearch.getText()))
		{
			adapterMember.notifyMember(memberAll);
			ivSearch.setImageDrawable(getResources().getDrawable(R.drawable.ic_member_search));
			ivSearch.setOnClickListener(null);
		}
		else
		{
			searchByKey();
			ivSearch.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_cicle));
			ivSearch.setOnClickListener(this);
		}
	}

	@Override
	public void afterTextChanged(Editable s)
	{

	}

	@Override
	public void onChecked(boolean isChecked)
	{
		if (memberCheckListener != null)
			memberCheckListener.onMemberChecked(isChecked);
	}

	/**
	 * 重置全体成员的选中状态
	 */
	public void resetCheckBox()
	{
		if (adapterMember != null)
		{
			adapterMember.resetCheckBox();
		}
	}

	/**
	 * 获取选择中的成员列表
	 * @return 成员列表
	 */
	public List<AirContact> getSelectedMember()
	{
		if (adapterMember != null)
		{
			return adapterMember.getSelectedMemberList();
		}
		return null;
	}

	/**
	 * 获取选择中的成员数
	 * @return 成员数
	 */
	public int getSelectedMemberSize()
	{
		if (adapterMember != null)
		{
			return adapterMember.getSelectedMemberList().size();
		}
		return 0;
	}
}
