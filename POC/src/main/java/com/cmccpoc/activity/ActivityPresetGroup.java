package com.cmccpoc.activity;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.controller.AccountInfoController;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirPresetGroup;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.listener.PresetGroupListener;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.BaseActivity;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.activity.home.adapter.AdapterGroupPreset;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.activity.home.widget.CallAlertDialog;
import com.cmccpoc.activity.home.widget.CallAlertDialog.OnAlertDialogCancelListener;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.Util;

public class ActivityPresetGroup extends Activity implements OnClickListener, OnItemClickListener, PresetGroupListener
{
	private ListView mGroupList;
	private AdapterGroupPreset adapterGroups;

	private ViewGroup bottom;
	private ImageView ivBtnLeft, ivBtnMid, ivBtnRight;
	private int[] memResChecked = new int[] { R.drawable.selector_fun_call, R.drawable.selector_fun_msg, R.drawable.selector_fun_cancel };
	private int[] memResUnchecked = new int[] { R.drawable.ic_fun_call_dis, R.drawable.ic_fun_msg_dis, R.drawable.ic_fun_cancel_dis };

	private List<AirContact> callMembers;
	private CallAlertDialog alertDialog;
	private AlertDialog dialog;
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_preset);
		mContext = this;
		initView();
	}
	
	@Override
	public void finish()
	{
		super.finish();
		AccountInfoController.setPresetGroupListener(null);
	}

	private void initView()
	{
		findViewById(R.id.btn_close).setOnClickListener(this);
		mGroupList = (ListView) findViewById(R.id.lv_groups);
		mGroupList.setOnItemClickListener(this);
		ivBtnLeft = (ImageView) findViewById(R.id.bar_left);
		ivBtnLeft.setOnClickListener(this);
		ivBtnMid = (ImageView) findViewById(R.id.bar_mid);
		ivBtnMid.setOnClickListener(this);
		ivBtnRight = (ImageView) findViewById(R.id.bar_right);
		ivBtnRight.setOnClickListener(this);
		bottom = (ViewGroup) findViewById(R.id.layout_bottom);
		AccountInfoController.setPresetGroupListener(this);
		AirtalkeeUserInfo.getInstance().getPresetGroups();
	}

	private void refreshBottomView(boolean isChecked)
	{
		for (int i = 0; i < bottom.getChildCount(); i++)
		{
			View child = bottom.getChildAt(i);
			child.setEnabled(isChecked);
		}
		if (isChecked)
		{
			ivBtnLeft.setImageResource(memResChecked[0]);
			ivBtnMid.setImageResource(memResChecked[1]);
			ivBtnRight.setImageResource(memResChecked[2]);
		}
		else
		{
			ivBtnLeft.setImageResource(memResUnchecked[0]);
			ivBtnMid.setImageResource(memResUnchecked[1]);
			ivBtnRight.setImageResource(memResUnchecked[2]);
		}
	}
	
	/**
	 * 呼叫被选中的成员
	 * 若都没有在线，则会提示是否前去留言的提示窗口
	 * @param isCall 是否呼叫
	 */
	public void callSelectMember(boolean isCall)
	{
		callMembers = adapterGroups.getSelectedMembers();
		if (callMembers.size() > 0)
		{
			if (AirtalkeeAccount.getInstance().isEngineRunning())
			{
				AirSession s = SessionController.SessionMatch(callMembers);
				if (isCall)
				{
					alertDialog = new CallAlertDialog(this, "正在呼叫" + s.getDisplayName(), "请稍后...", s.getSessionCode(), 100, false, new OnAlertDialogCancelListener()
					{
						@Override
						public void onDialogCancel(int reason)
						{
							switch (reason)
							{
								case AirSession.SESSION_RELEASE_REASON_NOTREACH:
									dialog = new AlertDialog(mContext, null, getString(R.string.talk_call_offline_tip), getString(R.string.talk_session_call_cancel), getString(R.string.talk_call_leave_msg), listener, reason);
									dialog.show();
									break;
								case AirSession.SESSION_RELEASE_REASON_REJECTED:
									if(Toast.isDebug) Toast.makeText1(mContext, "对方已拒接", Toast.LENGTH_SHORT).show();
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
					finish();
					AirtalkeeSessionManager.getInstance().getSessionByCode(s.getSessionCode());
					HomeActivity.getInstance().pageIndex = BaseActivity.PAGE_IM;
					HomeActivity.getInstance().onViewChanged(s.getSessionCode());
					HomeActivity.getInstance().panelCollapsed();
				}
			}
			else
			{
				Util.Toast(this, getString(R.string.talk_network_warning));
			}
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_tip_session_call));
		}
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
			refreshBottomView(false);
		}

		@Override
		public void onClickCancel(int id)
		{

		}
	};

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.btn_close:
			{
				finish();
				break;
			}
			case R.id.bar_left:
				callSelectMember(true);
				break;
			case R.id.bar_mid:
				AirtalkeeMessage.getInstance().MessageRecordPlayStop();
				callSelectMember(false);
				adapterGroups.setSelected(-1);
				adapterGroups.notifyDataSetChanged();
				refreshBottomView(false);
				break;
			case R.id.bar_right:
				adapterGroups.setSelected(-1);
				adapterGroups.notifyDataSetChanged();
				refreshBottomView(false);
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		adapterGroups.setSelected(position);
		adapterGroups.notifyDataSetChanged();
		refreshBottomView(true);
	}

	@Override
	public void onPresetGroupsGet(List<AirPresetGroup> presetGroups)
	{
		if (presetGroups != null && presetGroups.size() > 0)
		{
			adapterGroups = new AdapterGroupPreset(this, -1, presetGroups);
			mGroupList.setAdapter(adapterGroups);
			mGroupList.setOnItemClickListener(this);
		}
	}

	@Override
	public void onPresetGroupMembersGet(List<AirContact> contacts)
	{
		
	}
}
