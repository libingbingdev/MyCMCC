package com.cmccpoc.activity.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnContactPresenceListener;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.activity.SessionAddActivity;
import com.cmccpoc.activity.home.adapter.AdapterMember;
import com.cmccpoc.activity.home.adapter.AdapterMember.CheckedCallBack;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.activity.home.widget.CallAlertDialog;
import com.cmccpoc.activity.home.widget.CallAlertDialog.OnAlertDialogCancelListener;
import com.cmccpoc.activity.home.widget.MemberAllView;
import com.cmccpoc.activity.home.widget.MemberAllView.MemberCheckListener;
import com.cmccpoc.config.Config;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.Util;

/**
 * 三大Fragment之一：成员列表Fragment。主要包含当前频道成员和全部成员，在这个fragment中可以进行临时呼叫，或者直接留言等
 * @author Yao
 */
public class MemberFragment extends BaseFragment implements OnClickListener,
		OnItemClickListener, CheckedCallBack, MemberCheckListener,
		OnContactPresenceListener, TextWatcher
{
	private static final int DIALOG_CALL = 99;
	private TextView tabMemberSession, tabMemberAll;
	private ImageView ivSerachIcon;
	private List<AirContact> tempCallMembers = null;
	Map<String, AirContact> tempCallMembersCache = new TreeMap<String, AirContact>();
	private LinkedHashMap<Integer, TextView> ids = new LinkedHashMap<Integer, TextView>();
	private ListView lvMember;
	private AdapterMember adapterMember;
	private CallAlertDialog alertDialog;
	private LinearLayout memAllContainer, addMemberPanel, searchPannelChannel, searchPannelAll;
	private EditText searchEditChannel, searchEditAll;
	private ImageView ivSearch;
	private Button btnSearch;
	private int currentSelectPage = R.id.tab_member_session;
	private MemberAllView memberAllView;
	private boolean memberSessionChecked = false;
	private boolean memberAllChecked = false;
	List<AirContact> memberSearchResult = new ArrayList<AirContact>();
	AlertDialog dialog;
	
	private static MemberFragment mInstance;
	/**
	 * 获取成员列表Fragment实例对象
	 * @return
	 */
	public static MemberFragment getInstance()
	{
		return mInstance;
	}
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mInstance = this;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		v = inflater.inflate(getLayout(), container, false);

		addMemberPanel = (LinearLayout) findViewById(R.id.add_member_panel);
		addMemberPanel.setOnClickListener(this);

		btnSearch = (Button) findViewById(R.id.btn_search);
		btnSearch.setOnClickListener(this);

		lvMember = (ListView) findViewById(R.id.talk_lv_member);
		lvMember.setAdapter(adapterMember = new AdapterMember(getActivity(), null, null, true, true, this));
		adapterMember.notifyDataSetChanged();
		lvMember.setOnItemClickListener(this);

		tabMemberSession = (TextView) findViewById(R.id.tab_member_session);
		tabMemberSession.setOnClickListener(this);
		tabMemberAll = (TextView) findViewById(R.id.tab_member_all);
		tabMemberAll.setOnClickListener(this);
		if (!Config.funcPTTButton)
		{
			tabMemberSession.setText("频道");
			tabMemberAll.setText("全部");
		}
		else 
		{
			tabMemberSession.setText("频道成员");
			tabMemberAll.setText("全部成员");
		}
		ivSerachIcon = (ImageView) findViewById(R.id.iv_search_icon);
		ivSerachIcon.setOnClickListener(this);

		memAllContainer = (LinearLayout) findViewById(R.id.mem_container);
		memberAllView = new MemberAllView(getActivity(), this, true);
		memAllContainer.addView(memberAllView);

		searchPannelChannel = (LinearLayout) findViewById(R.id.serach_pannel);
		searchEditChannel = (EditText) findViewById(R.id.et_search);
		searchPannelAll = (LinearLayout) memberAllView.findViewById(R.id.serach_pannel);
		searchEditAll = (EditText) memberAllView.findViewById(R.id.et_search);

		searchEditChannel.addTextChangedListener(this);
		ivSearch = (ImageView) findViewById(R.id.iv_search);
		ids.put(R.id.tab_member_session, tabMemberSession);
		ids.put(R.id.tab_member_all, tabMemberAll);

		// refreshTab(R.id.tab_member_session);

		if (mediaStatusBar != null)
			mediaStatusBar.setBarEnable(HomeActivity.PAGE_MEMBER, false);
		setSession(getSession());

		return v;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		try
		{
			searchEditChannel.setText("");
			searchEditAll.setText("");
			if (mediaStatusBar != null)
				mediaStatusBar.setBarEnable(HomeActivity.PAGE_MEMBER, false);
			setSession(getSession());
			if (getSession() != null)
			{
				currentSelectPage = R.id.tab_member_session;
				refreshTab(R.id.tab_member_session);
				if (getSession().getType() == AirSession.TYPE_CHANNEL)
				{
					addMemberPanel.setVisibility(View.GONE);
				}
				else
				{
					addMemberPanel.setVisibility(View.VISIBLE);
				}
			}
			else
			{
				addMemberPanel.setVisibility(View.GONE);
			}
		}
		catch (Exception e)
		{}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		// AirtalkeeContactPresence.getInstance().setContactPresenceListener(null);
		AirtalkeeContactPresence.getInstance().ContactPresenceUnsubscribe();
	}

	@Override
	public int getLayout()
	{
		return R.layout.frag_member_layout;
	}

	@Override
	public void dispatchBarClickEvent(int page, int id)
	{
		if (page == HomeActivity.PAGE_MEMBER)
		{
			switch (id)
			{
				case R.id.bar_left:
					callSelectMember(true);
					break;
				case R.id.bar_mid:
					AirtalkeeMessage.getInstance().MessageRecordPlayStop();
					callSelectMember(false);
					callSelectClean();
					break;
				case R.id.bar_right:
					callSelectClean();
					break;
			}
		}
	}

	/**
	 * 刷新 频道成员/全部成员 选项卡
	 * @param id 频道成员 or 全部成员
	 */
	public void refreshTab(int id)
	{
		Log.i(MemberFragment.class, "MemberFragment refreshTab start id = " + id);
		searchPannelAll.setVisibility(View.GONE);
		searchPannelChannel.setVisibility(View.GONE);
		ivSerachIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_white));
		Util.hideSoftInput(getActivity());
		Iterator<Integer> iter = ids.keySet().iterator();
		while (iter.hasNext())
		{
			Integer i = iter.next();
			TextView v = ids.get(i);
			v.setSelected(i == id);
		}
		try
		{
			if (id == R.id.tab_member_session) // 频道成员
			{
				Log.i(MemberFragment.class, "MemberFragment 频道成员 id = " + id);
				lvMember.setVisibility(View.VISIBLE);
				memAllContainer.setVisibility(View.GONE);
				if (memberSearchResult.size() > 0)
				{
					setSession(getSession());
				}
				tabMemberSession.setEnabled(false);
				tabMemberAll.setEnabled(true);
			}
			else if (id == R.id.tab_member_all)// 全部成员
			{
//				if (memberAllView == null || memberAllView.memberAll.size() < 1)
//				{
					memAllContainer.removeView(memberAllView);
					memberAllView = new MemberAllView(getActivity(), this, true);
					memberAllView.adapterMember.notifyDataSetChanged();
					memAllContainer.addView(memberAllView);
					searchPannelAll = (LinearLayout) memberAllView.findViewById(R.id.serach_pannel);
					searchEditAll = (EditText) memberAllView.findViewById(R.id.et_search);
				// }
				Log.i(MemberFragment.class, "MemberFragment 全部成员 id  = " + id);
				memberAllView.adapterMember.notifyMember(memberAllView.memberAll);
				lvMember.setVisibility(View.GONE);
				memAllContainer.setVisibility(View.VISIBLE);
				tabMemberSession.setEnabled(true);
				tabMemberAll.setEnabled(false);
			}
			else 
			{
				Log.i(MemberFragment.class, "MemberFragment else id  = " + id);
			}
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * 刷新搜索成员区域
	 * @param id 频道成员 or 全部成员
	 */
	private void refreshSearch(int id)
	{
		Log.i(MemberFragment.class, "MemberFragment refreshSearch id = " + id);
		try
		{
			if (id == R.id.tab_member_session) // 频道成员
			{
				if (searchPannelChannel.getVisibility() == View.GONE)
				{
					ivSerachIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_orange));
					searchPannelChannel.setVisibility(View.VISIBLE);
					searchEditChannel.requestFocus();
					Util.showSoftInput(getActivity());
				}
				else
				{
					ivSerachIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_white));
					searchPannelChannel.setVisibility(View.GONE);
					searchEditChannel.clearFocus();
					searchEditChannel.setText("");
					Util.hideSoftInput(getActivity());
				}
			}
			else if (id == R.id.tab_member_all)// 全部成员
			{
				if (searchPannelAll.getVisibility() == View.GONE)
				{
					ivSerachIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_orange));
					searchPannelAll.setVisibility(View.VISIBLE);
					searchEditAll.requestFocus();
					Util.showSoftInput(getActivity());
				}
				else
				{
					ivSerachIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_white));
					searchPannelAll.setVisibility(View.GONE);
					searchEditAll.clearFocus();
					searchEditAll.setText("");
					Util.hideSoftInput(getActivity());
				}
			}
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public void onClick(View v)
	{
		Log.i(MemberFragment.class, "MemberFragment onClick v.id = " + v.getId());
		switch (v.getId())
		{
			case R.id.tab_member_session:
			case R.id.tab_member_all:
			{
				currentSelectPage = v.getId();
				refreshTab(currentSelectPage);
				break;
			}
			case R.id.add_member_panel:
			{
				Intent it = new Intent(getActivity(), SessionAddActivity.class);
				it.putExtra("sessionCode", getSession().getSessionCode());
				it.putExtra("type", AirServices.TEMP_SESSION_TYPE_MESSAGE);
				getActivity().startActivity(it);
				break;
			}
			case R.id.iv_search_icon:
			{
				//refreshTab(currentSelectPage);
				refreshSearch(currentSelectPage);
				break;
			}
			case R.id.btn_search:
			{
				searchByKey();
				break;
			}
		}
	}

	/**
	 * 通过关键词进行搜索
	 */
	private void searchByKey()
	{
		String key = searchEditChannel.getText().toString().toLowerCase();
		memberSearchResult.clear();
		setSession(getSession());
		for (int i = 0; i < adapterMember.getCount(); i++)
		{
			AirContact contact = (AirContact) adapterMember.getItem(i);
			String displayName = contact.getDisplayName().toLowerCase();
			String ipocId = contact.getIpocId().toLowerCase();
			if (displayName.equalsIgnoreCase(key) || ipocId.equals(key) || displayName.contains(key) || ipocId.contains(key))
			{
				memberSearchResult.add(contact);
			}
		}
		refreshMembers(getSession(), memberSearchResult);
	}

	/**
	 * 设置session会话
	 * @param s 会话Entity
	 */
	public void setSession(AirSession s)
	{
		if (s != null)
		{
			switch (s.getType())
			{
				case AirSession.TYPE_CHANNEL:
				{
					AirChannel c = AirtalkeeChannel.getInstance().ChannelGetByCode(s.getSessionCode());
					if (c != null)
					{
						c.MembersSort();
						refreshMembers(s, c.MembersGet());
					}
					break;
				}
				case AirSession.TYPE_DIALOG:
				{
					s.MembersSort();
					refreshMembers(s, s.getMemberAll());
					break;
				}
			}
			// refreshMemberOnline(s.SessionPresenceList());
		}
		else
		{
			refreshMembers(null, null);
			// sessionBoxMember.refreshMemberOnline(null);
		}
	}

	/**
	 * 刷新成员状态
	 * @param session 会话Entity
	 * @param members 成员列表
	 */
	public void refreshMembers(AirSession session, List<AirContact> members)
	{
		try
		{
			adapterMember.notifyMember(session, members);
			adapterMember.notifyDataSetChanged();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 刷新频道成员列表
	 */
	@SuppressWarnings("unused")
	public void refreshMembers(AirChannel ch)
	{
		ch.MembersSort();
		if (ch != null)
			refreshMembers(ch.getSession(), ch.MembersGet());
		else
			refreshMembers(null, new ArrayList<AirContact>());
	}
	
	public void refreshMemberOnline(List<AirContact> memberOnline)
	{
	}
	
	public void refreshMemberOnline()
	{
		
	}

	
	/**
	 * 刷新全部成员
	 */
	public void refreshAllMembers()
	{
		try
		{
			memAllContainer.removeView(memberAllView);
			memberAllView = new MemberAllView(getActivity(), this, true);
			memberAllView.adapterMember.notifyDataSetChanged();
			memAllContainer.addView(memberAllView);
			memberAllView.adapterMember.notifyMember(memberAllView.memberAll);
		}
		catch (Exception e)
		{ }
	}
	
	public void refreshChannelMembers()
	{
		adapterMember.notifyDataSetChanged();
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		switch (parent.getId())
		{
			case R.id.talk_lv_member:
			{
				if (getSession().getType() == AirSession.TYPE_CHANNEL)
				{
					CheckBox cb = (CheckBox) view.findViewById(R.id.talk_cb_group_member);
					if (adapterMember.getSelectedMemberList().size() < 30 || cb.isChecked())
					{
						AirContact c = (AirContact) adapterMember.getItem(position);
						if (c != null)
						{
							if (!AirtalkeeAccount.getInstance().getUserId().equals(c.getIpocId()))
							{
								if (cb != null)
									cb.setChecked(!cb.isChecked());
							}
						}
					}
					else
						if(Toast.isDebug) Toast.makeText1(getActivity(), "呼叫人数不能超过30人", Toast.LENGTH_LONG).show();
				}
				break;
			}
		}
	}

	/**
	 * 呼叫选中的成员
	 * @param isCall 是否呼叫
	 */
	public void callSelectMember(boolean isCall)
	{
		if ((adapterMember.getSelectedMemberList() != null && adapterMember.getSelectedMemberList().size() > 0) || memberAllView.getSelectedMemberSize() > 0)
		{
			if (tempCallMembers == null)
				tempCallMembers = new ArrayList<AirContact>();
			tempCallMembers.clear();
			tempCallMembersCache.clear();
			for (AirContact c : adapterMember.getSelectedMemberList())
			{
				if (!TextUtils.equals(c.getIpocId(), AirtalkeeAccount.getInstance().getUserId()))
				{
					if (!tempCallMembersCache.containsKey(c.getIpocId()))
					{
						tempCallMembersCache.put(c.getIpocId(), c);
						tempCallMembers.add(c);
					}
				}
			}

			for (AirContact c : memberAllView.getSelectedMember())
			{
				if (!TextUtils.equals(c.getIpocId(), AirtalkeeAccount.getInstance().getUserId()))
				{
					if (!tempCallMembersCache.containsKey(c.getIpocId()))
					{
						tempCallMembersCache.put(c.getIpocId(), c);
						tempCallMembers.add(c);
					}
				}
			}

			if (tempCallMembers.size() > 0)
			{
				if (AirtalkeeAccount.getInstance().isEngineRunning())
				{
					AirSession s = SessionController.SessionMatch(tempCallMembers);
					if (isCall)
					{
						alertDialog = new CallAlertDialog(getActivity(), "正在呼叫" + s.getDisplayName(), "请稍后...", s.getSessionCode(), DIALOG_CALL, false, new OnAlertDialogCancelListener()
						{
							@Override
							public void onDialogCancel(int reason)
							{
								Log.i(MemberFragment.class, "MemberFragment reason = " + reason);
								switch (reason)
								{
									case AirSession.SESSION_RELEASE_REASON_NOTREACH:
										dialog = new AlertDialog(getActivity(), null, getString(R.string.talk_call_offline_tip), getString(R.string.talk_session_call_cancel), getString(R.string.talk_call_leave_msg), listener, reason);
										dialog.show();
										break;
									case AirSession.SESSION_RELEASE_REASON_REJECTED:
										if(Toast.isDebug) Toast.makeText1(AirServices.getInstance(), "对方已拒接", Toast.LENGTH_SHORT).show();
										break;
									case AirSession.SESSION_RELEASE_REASON_BUSY:
										if(Toast.isDebug) Toast.makeText1(AirServices.getInstance(), "对方正在通话中，无法建立呼叫", Toast.LENGTH_SHORT).show();
										break;
								}
							}
						});
						alertDialog.show();
					}
					else
					{
						AirtalkeeSessionManager.getInstance().getSessionByCode(s.getSessionCode());
						HomeActivity.getInstance().pageIndex = BaseActivity.PAGE_IM;
						HomeActivity.getInstance().onViewChanged(s.getSessionCode());
						HomeActivity.getInstance().panelCollapsed();
					}

				}
				else
				{
					Util.Toast(getActivity(), getString(R.string.talk_network_warning));
				}
			}
			else
			{
				Util.Toast(getActivity(), getString(R.string.talk_tip_session_call));
			}
		}
		else
		{
			Util.Toast(getActivity(), getString(R.string.talk_tip_session_call));
		}
	}

	/**
	 * 清除选中的成员
	 */
	public void callSelectClean()
	{
		memberAllChecked = false;
		memberSessionChecked = false;
		adapterMember.resetCheckBox();
		memberAllView.resetCheckBox();
		mediaStatusBar.setBarEnable(HomeActivity.PAGE_MEMBER, false);
	}

	@Override
	public void onChecked(boolean isChecked)
	{
		memberSessionChecked = isChecked;
		mediaStatusBar.setBarEnable(HomeActivity.PAGE_MEMBER, memberSessionChecked || memberAllChecked);
	}

	@Override
	public void onMemberChecked(boolean isChecked)
	{
		memberAllChecked = isChecked;
		mediaStatusBar.setBarEnable(HomeActivity.PAGE_MEMBER, memberSessionChecked || memberAllChecked);
	}

	@Override
	public void onListItemLongClick(int id, int selectedItem)
	{

	}

	private DialogListener listener = new DialogListener()
	{
		@Override
		public void onClickOk(int id, boolean isChecked)
		{

		}

		@Override
		public void onClickOk(int id, Object object)
		{
			AirtalkeeMessage.getInstance().MessageRecordPlayStop();
			callSelectMember(false);
			callSelectClean();
		}

		@Override
		public void onClickCancel(int id)
		{

		}
	};

	@Override
	public void onContactPresence(boolean isSubscribed, HashMap<String, Integer> presenceMap)
	{
		if (getSession() != null && getSession().getType() == AirSession.TYPE_DIALOG)
		{
			getSession().MembersSort();
		}
		adapterMember.notifyDataSetChanged();
		memberAllView.adapterMember.notifyDataSetChanged();
		if (HomeActivity.getInstance() != null)
			HomeActivity.getInstance().refreshChannel();
	}

	@Override
	public void onContactPresence(boolean isSubscribed, String uid, int state)
	{
		if (getSession() != null && getSession().getType() == AirSession.TYPE_DIALOG)
		{
			getSession().MembersSort();
		}
		adapterMember.notifyDataSetChanged();
		memberAllView.adapterMember.notifyDataSetChanged();
		if (HomeActivity.getInstance() != null)
			HomeActivity.getInstance().refreshChannel();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		btnSearch.setEnabled(!TextUtils.isEmpty(searchEditChannel.getText()));
		if (TextUtils.isEmpty(searchEditChannel.getText()))
		{
			setSession(getSession());
			ivSearch.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_member_search));
			ivSearch.setOnClickListener(null);
		}
		else
		{
			searchByKey();
			ivSearch.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_close_cicle));
			ivSearch.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					searchEditChannel.setText("");
				}
			});
		}
	}

	@Override
	public void afterTextChanged(Editable s)
	{

	}
}
