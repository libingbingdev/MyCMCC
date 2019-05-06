package com.cmccpoc.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.OnSystemDefectListener;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.config.Config;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Util;

/**
 * 更多：意见反馈
 * 就是提交意见用的。。
 * @author Yao
 */
public class MenuDefectReportActivity extends ActivityBase implements OnClickListener, OnSystemDefectListener
{
	private EditText mDefectContent;
	private ProgressBar mDefectWait;
	private Button mDefectButton;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_defect_report);
		doInitView();
		AirtalkeeAccount.getInstance().setOnSystemDefectListener(this);
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
		ivTitle.setText(R.string.talk_tools_defect);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		mDefectContent = (EditText) findViewById(R.id.defect_report_content);
		mDefectContent.addTextChangedListener(textWatcher);
		mDefectWait = (ProgressBar) findViewById(R.id.defect_report_progress);
		mDefectButton = (Button) findViewById(R.id.defect_report_post);
		mDefectButton.setOnClickListener(this);
		checkEditTextNull();
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
	 * 检查EditText是否为空，如果是空，则提交按钮不可点击
	 */
	private void checkEditTextNull()
	{
		String content = mDefectContent.getText().toString().trim();
		if (!content.equals(""))
		{
			mDefectButton.setClickable(true);
			mDefectButton.setBackgroundResource(R.drawable.selector_button_commit);
		}
		else
		{
			mDefectButton.setClickable(false);
			mDefectButton.setBackgroundResource(R.drawable.btn_commit_gray);
		}
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
		AirtalkeeAccount.getInstance().setOnSystemDefectListener(null);
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
			case R.id.defect_report_post:
			{
				if (!Utils.isEmpty(mDefectContent.getText().toString()))
				{
					mDefectWait.setVisibility(View.VISIBLE);
					mDefectButton.setClickable(false);
					AirtalkeeAccount.getInstance().SystemDefectReport(mDefectContent.getText().toString());
				}
				break;
			}
		}
	}

	@Override
	public void onSystemDefectReport(boolean isOk)
	{
		// TODO Auto-generated method stub
		mDefectWait.setVisibility(View.GONE);
		mDefectButton.setClickable(true);
		if (isOk)
		{
			Util.Toast(this, getString(R.string.talk_tools_defect_report_tip), R.drawable.ic_success);
			finish();
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_tools_defect_report_error), R.drawable.ic_error);
		}
	}

}
