package com.cmccpoc.activity;

import java.util.List;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.OnUserInfoListener;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactGroup;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.config.Config;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Util;

/**
 * 更多：编辑名称
 * 修改昵称的
 * @author Yao
 */
public class MenuDisplayActivity extends ActivityBase implements OnClickListener, OnUserInfoListener
{
	public EditText tvUserName;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_display);
		doInitView();
	}

	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
		tvUserName.setText(AirtalkeeAccount.getInstance().getUserName());
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		AirtalkeeUserInfo.getInstance().setOnUserInfoListener(null);
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		AirtalkeeUserInfo.getInstance().setOnUserInfoListener(this);
	}

	/**
	 * 初始化绑定控件Id
	 */
	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_user_username_edit);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		Intent accountIntent = getIntent();  
		tvUserName = (EditText) findViewById(R.id.talk_tv_user_name);
		tvUserName.setText(accountIntent.getStringExtra("oldUserName"));

		findViewById(R.id.talk_lv_tool_save).setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
			{
				finish();
				break;
			}
			case R.id.talk_lv_tool_save:
			{
				String value = tvUserName.getText().toString();
				tvUserName.setSingleLine();
				tvUserName.setHint(AirtalkeeUserInfo.getInstance().getUserInfo().getDisplayName());
				if (!Utils.isEmpty(value))
				{
					if (value.length() > 15)
					{
						Util.Toast(this, getString(R.string.talk_user_info_update_name_error));
					}
					else
					{
						try
						{
							AirtalkeeUserInfo.getInstance().UserInfoUpdate(value.trim());
							Util.Toast(this, getString(R.string.talk_channel_editname_success), R.drawable.ic_success);
							Intent it = new Intent(MenuDisplayActivity.this, MenuAccountActivity.class);
							it.putExtra("newUserName", value.trim());
							setResult(1, it);
							finish();
						}
						catch (Exception e)
						{
							Util.Toast(this, getString(R.string.talk_channel_editname_fail), R.drawable.ic_error);
						}
					}
				}
				break;
			}
			default:
				break;
		}
	}

	@Override
	public void onUserInfoGet(AirContact user)
	{
		if (user != null)
		{
			tvUserName.setText(user.getDisplayName());
		}
	}

	@Override
	public void onUserInfoUpdate(boolean isOk, AirContact user)
	{
		if (isOk)
		{
			tvUserName.setText(user.getDisplayName());
			Util.Toast(this, getString(R.string.talk_user_info_update_name_ok));
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_user_info_update_name_fail));
		}

	}

	@Override
	public void onUserIdGetByPhoneNum(int result, AirContact contact)
	{

	}

	@Override
	public void onUserOrganizationTree(boolean isOk, AirContactGroup org)
	{

	}

	@Override
	public void onUserOrganizationTreeSearch(boolean isOk, List<AirContact> contacts)
	{

	}
}
