package com.cmccpoc.activity;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.BaseActivity;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.activity.home.widget.CallAlertDialog;
import com.cmccpoc.activity.home.widget.CallAlertDialog.OnAlertDialogCancelListener;
import com.cmccpoc.activity.home.widget.MemberAllView;
import com.cmccpoc.activity.home.widget.MemberAllView.MemberCheckListener;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.Util;

/**
 * 新建临时会话界面
 * @author Yao
 */
public class SessionNewActivity extends Activity implements OnClickListener, MemberCheckListener
{
	private LinearLayout containner;
	private MemberAllView memAllView;
	private ViewGroup bottom;
	private List<AirContact> tempCallMembers = null;
	private CallAlertDialog alertDialog;
	private int DIALOG_CALL = 111;

	private ImageView ivBtnLeft, ivBtnMid, ivBtnRight;
	private int[] memResChecked = new int[] { R.drawable.selector_fun_call, R.drawable.selector_fun_msg, R.drawable.selector_fun_cancel };
	private int[] memResUnchecked = new int[] { R.drawable.ic_fun_call_dis, R.drawable.ic_fun_msg_dis, R.drawable.ic_fun_cancel_dis };
	AlertDialog dialog;

	private static SessionNewActivity mInstance;
	/**
	 * 获取SessionNewActivity实例对象
	 * @return
	 */
	public static SessionNewActivity getInstance()
	{
		return mInstance;
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setContentView(R.layout.activity_session_new);
		bottom = (ViewGroup) findViewById(R.id.layout_bottom);
		memAllView = new MemberAllView(this, this, false);
		findViewById(R.id.btn_close).setOnClickListener(this);
		containner = (LinearLayout) findViewById(R.id.containner);
		containner.addView(memAllView);
		memAllView.getSearchPannel().setVisibility(View.VISIBLE);
		ivBtnLeft = (ImageView) findViewById(R.id.bar_left);
		ivBtnLeft.setOnClickListener(this);
		ivBtnMid = (ImageView) findViewById(R.id.bar_mid);
		ivBtnMid.setOnClickListener(this);
		ivBtnRight = (ImageView) findViewById(R.id.bar_right);
		ivBtnRight.setOnClickListener(this);
		mInstance = this;
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.btn_close:
				this.finish();
				break;
			case R.id.bar_left:
				callSelectMember(true);
				break;
			case R.id.bar_mid:
				AirtalkeeMessage.getInstance().MessageRecordPlayStop();
				callSelectMember(false);
				callSelectClean();
				this.finish();
				break;
			case R.id.bar_right:
				callSelectClean();
				break;
		}
	}

	/**
	 * 清除选中的成员
	 */
	public void callSelectClean()
	{
		memAllView.resetCheckBox();
		refreshBottomView(false);
	}

	/**
	 * 呼叫被选中的成员
	 * 若都没有在线，则会提示是否前去留言的提示窗口
	 * @param isCall 是否呼叫
	 */
	public void callSelectMember(boolean isCall)
	{
		if (tempCallMembers == null)
			tempCallMembers = new ArrayList<AirContact>();
		else
			tempCallMembers.clear();

		for (AirContact c : memAllView.getSelectedMember())
		{
			if (!TextUtils.equals(c.getIpocId(), AirtalkeeAccount.getInstance().getUserId()))
			{
				tempCallMembers.add(c);
			}
		}
		if (tempCallMembers.size() > 0)
		{
			if (AirtalkeeAccount.getInstance().isEngineRunning())
			{
				AirSession s = SessionController.SessionMatch(tempCallMembers);
				if (isCall)
				{
					alertDialog = new CallAlertDialog(this, "正在呼叫" + s.getDisplayName(), "请稍后...", s.getSessionCode(), DIALOG_CALL, false, new OnAlertDialogCancelListener()
					{
						@Override
						public void onDialogCancel(int reason)
						{
							// TODO Auto-generated method stub
							switch (reason)
							{
								case AirSession.SESSION_RELEASE_REASON_NOTREACH:
									dialog = new AlertDialog(mInstance, null, getString(R.string.talk_call_offline_tip), getString(R.string.talk_session_call_cancel), getString(R.string.talk_call_leave_msg), listener, reason);
									dialog.show();
									break;
								case AirSession.SESSION_RELEASE_REASON_REJECTED:
									if(Toast.isDebug) Toast.makeText1(mInstance, "对方已拒接", Toast.LENGTH_SHORT).show();
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
				Util.Toast(this, getString(R.string.talk_network_warning));
			}
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_tip_session_call));
		}

	}

	@Override
	public void onMemberChecked(boolean isChecked)
	{
		refreshBottomView(isChecked);
	}

	/**
	 * 刷新底部按钮状态
	 * @param isChecked 是否选中
	 */
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
			if (mInstance != null)
				finish();
		}

		@Override
		public void onClickCancel(int id)
		{
			// TODO Auto-generated method stub

		}
	};
}
