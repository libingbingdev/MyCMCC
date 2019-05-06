package com.cmccpoc.activity;

import java.util.List;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.controller.AccountInfoController;
import com.airtalkee.sdk.engine.StructUserMark;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactGroup;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.listener.UserAccountListener;
import com.airtalkee.sdk.listener.UserInfoListener;
import com.cmccpoc.R;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Util;

/**
 * 更多：修改密码
 * 密码规则：6~15位数字 or 字母
 * @author Yao
 */
public class MenuPasswordActivity extends ActivityBase implements
		OnClickListener, OnCheckedChangeListener, UserInfoListener,
		UserAccountListener
{
	private EditText old_password;
	private EditText new_password;
	private EditText new_password_confirm;
	private Button change_password;
	private CheckBox show_password;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		// setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_password);

		doInitView();
	}

	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
	}

	/**
	 * 初始化绑定控件Id
	 */
	private void doInitView()
	{

		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_pwd_update);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);
		AccountInfoController.setUserInfoListener(this);
		old_password = (EditText) findViewById(R.id.old_password);
		new_password = (EditText) findViewById(R.id.new_password);
		new_password_confirm = (EditText) findViewById(R.id.new_password_confirm);
		change_password = (Button) findViewById(R.id.btn_change_password);
		show_password = (CheckBox) findViewById(R.id.show_password);
		old_password.addTextChangedListener(textWatcher);
		new_password.addTextChangedListener(textWatcher);
		new_password_confirm.addTextChangedListener(textWatcher);
		change_password.setOnClickListener(this);
		show_password.setOnCheckedChangeListener(this);
		checkEditTextNull();
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
			case R.id.btn_change_password:
			{
				String password = old_password.getText().toString().trim();;
				String password_new = new_password.getText().toString().trim();;
				String password_new_confirm = new_password_confirm.getText().toString().trim();
				// if (password_new.equals(""))
				// {
				// Util.Toast(mInstance,
				// getString(R.string.userlogin_pwd_isnull));
				// }
				// else
				// {
				//
				// }
				AirContact userinfo = AccountController.getUserInfo();
				if (userinfo != null)
				{
					String ipocid = userinfo.getIpocId();
					if (password_new.length() < 6 || password.length() < 6)
					{
						Util.Toast(this, getString(R.string.talk_pwd_error), R.drawable.ic_error);
						return;
					}
					else if (password_new.length() > 15 || password.length() > 15)
					{
						Util.Toast(this, getString(R.string.pwd_outof_lenth), R.drawable.ic_error);
						return;
					}
					else if (!userinfo.getPwd().equals(password))
					{
						Util.Toast(this, getString(R.string.pwd_no_same), R.drawable.ic_error);
						return;
					}
					else if (password.equals(password_new))
					{
						Util.Toast(this, getString(R.string.pwd_same_of_old), R.drawable.ic_error);
						return;
					}
					else if (!password_new.equals(password_new_confirm))
					{
						Util.Toast(this, getString(R.string.talk_pwd_confirm_not_equals), R.drawable.ic_error);
						return;
					}
					AccountInfoController.userSetPassword(ipocid, password, password_new);
					showDialog(R.id.talk_dialog_waiting);
				}
				break;
			}
			default:
				break;
		}
	}

	private TextWatcher textWatcher = new TextWatcher()
	{
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after)
		{
			checkEditTextNull();
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			checkEditTextNull();
		}

		@Override
		public void afterTextChanged(Editable s)
		{
			checkEditTextNull();
		}
	};

	/**
	 * 检测三个EditText是否为空，如果有一个为空，保存按钮就无法点击
	 */
	private void checkEditTextNull()
	{
		String password = old_password.getText().toString().trim();;
		String password_new = new_password.getText().toString().trim();;
		String password_new_confirm = new_password_confirm.getText().toString().trim();

		if (!password.equals("") && !password_new.equals("") && !password_new_confirm.equals(""))
		{
			change_password.setClickable(true);
			change_password.setBackgroundResource(R.drawable.selector_button_new);
		}
		else
		{
			change_password.setClickable(false);
			change_password.setBackgroundResource(R.drawable.btn_save_gray);
		}
	}

	@Override
	public void onUserOrganizationTree(boolean isOk, AirContactGroup org)
	{

	}

	@Override
	public void onUserOrganizationTreeSearch(boolean isOk, List<AirContact> contacts)
	{

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onUserInfoSetPassword(boolean isOk, String password)
	{
		// TODO Auto-generated method stub
		removeDialog(R.id.talk_dialog_waiting);
		if (isOk)
		{
			if (new_password.getText() != null)
			{
				AirServices.iOperator.putString("USER_PWD", new_password.getText().toString());
			}
			Util.Toast(this, getString(R.string.pwd_chage_success), R.drawable.ic_success);
			finish();
		}
		else
		{
			showDialog(R.id.talk_dialog_login_fail);
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		switch (buttonView.getId())
		{
			case R.id.show_password:
			{
				if (isChecked)
				{
					old_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					new_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					new_password_confirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					old_password.setSelection(old_password.getText().toString().length());
					new_password.setSelection(new_password.getText().toString().length());
					new_password_confirm.setSelection(new_password_confirm.getText().toString().length());
				}
				else
				{
					old_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
					new_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
					new_password_confirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
					old_password.setSelection(old_password.getText().toString().length());
					new_password.setSelection(new_password.getText().toString().length());
					new_password_confirm.setSelection(new_password_confirm.getText().toString().length());
				}
				break;
			}
		}
	}

	@Override
	public void UserFunctionSetting(AirFunctionSetting setting)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void UserLoginEvent(int result, AirContact user)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void UserLogoutEvent(boolean success)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void UserHeartbeatEvent(int result)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void UserRegisterEvent(boolean isOk, AirContact user)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void UserUnregisterEvent()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void UserAccountMatch(boolean isOk, AirContact user)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserInfoGetEvent(AirContact user)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserInfoUpdateEvent(boolean isOk, AirContact user)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserInfoRegisterByPhoneNumber(int result, String ipocid)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserInfoGetbackAccountByPhoneNumber(int result, String[] ipocids)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserInfoGenerateTempCodeByPhoneNumber(boolean isOk)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserInfoUpdatePhoneNum(boolean isOk)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserIdGetByPhoneNum(int result, StructUserMark user)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void UserServerChanged(String serverAddress)
	{
		// TODO Auto-generated method stub
		
	}
}
