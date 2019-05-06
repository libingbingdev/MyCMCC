package com.cmccpoc.activity;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirContact;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.widget.MemberAllView;
import com.cmccpoc.activity.home.widget.MemberAllView.MemberCheckListener;

/**
 * 临时会话 添加成员 界面
 * 将选中的成员添加到当前临时会话中
 * @author Yao
 */
public class SessionAddActivity extends Activity implements OnClickListener, MemberCheckListener
{
	private LinearLayout containner;
	private MemberAllView memAllView;
	private ViewGroup bottom;
	private List<AirContact> tempCallMembers = null;
	private ImageView ivAddMember;
	private String sessionCode = "";
	private Button mOk,mBack;
	@Override
	protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		Log.d("zlm","SessionAddActivity...onCreate");
		setContentView(R.layout.activity_session_addmember);
		bottom = (ViewGroup) findViewById(R.id.layout_bottom);
		memAllView = new MemberAllView(this, this, false);
		findViewById(R.id.btn_close).setOnClickListener(this);
		containner = (LinearLayout) findViewById(R.id.containner);
		containner.addView(memAllView);
		memAllView.getSearchPannel().setVisibility(View.VISIBLE);
		ivAddMember = (ImageView) findViewById(R.id.iv_add_member);
		ivAddMember.setOnClickListener(this);
		mOk= (Button) findViewById(R.id.ok);
		mBack= (Button) findViewById(R.id.back);
		bundle = getIntent().getExtras();
		if (bundle != null)
		{
			sessionCode = bundle.getString("sessionCode");
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.btn_close:
			{
				this.finish();
				break;
			}
			case R.id.iv_add_member:
			{
				callAddMember();
				break;
			}
		}
	}

	/**
	 * 清除选择成员
	 */
	public void callSelectClean()
	{
		memAllView.resetCheckBox();
		refreshBottomView(false);
	}

	/**
	 * 呼叫添加的成员
	 */
	public void callAddMember()
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
		AirtalkeeSessionManager.getInstance().sessionMemberUpdate(sessionCode, tempCallMembers);
		finish();
	}

	@Override
	public void onMemberChecked(boolean isChecked)
	{
		refreshBottomView(isChecked);
		if (null != memAllView.getSelectedMember() && memAllView.getSelectedMember().size() > 0)
		{
			ivAddMember.setImageResource(R.drawable.btn_add_orange);
			ivAddMember.setClickable(true);
		}
		else
		{
			ivAddMember.setImageResource(R.drawable.btn_add_black);
			ivAddMember.setClickable(false);
		}
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
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode){
			case KeyEvent.KEYCODE_MENU:
				mOk.setBackgroundResource(R.drawable.bg_list_focuse);
				break;
			case KeyEvent.KEYCODE_BACK:
				mBack.setBackgroundResource(R.drawable.bg_list_focuse);
				break;
		}
		return super.onKeyDown(keyCode,event);
	}


	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode){
			case KeyEvent.KEYCODE_MENU:
				mOk.setBackgroundResource(R.drawable.bg_list_normal);
				callAddMember();
				break;
			case KeyEvent.KEYCODE_BACK:
				mBack.setBackgroundResource(R.drawable.bg_list_normal);
				this.finish();
				break;
		}
		return super.onKeyUp(keyCode, event);
	}

}
