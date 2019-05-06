package com.cmccpoc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cmccpoc.R;
import com.cmccpoc.config.Config;
import com.cmccpoc.util.ThemeUtil;

/**
 * 更多：帮助和反馈
 * 有两个选项按钮，一个跳转到使用手册，一个跳转到意见反馈
 * @author Yao
 */
public class MenuHelpActivity extends ActivityBase implements OnClickListener
{
	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_help);
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
		ivTitle.setText(R.string.talk_tools_help);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		// Manual item
		findViewById(R.id.talk_lv_tool_manual).setOnClickListener(this);

		// Defect report
		findViewById(R.id.talk_lv_tool_defect).setOnClickListener(this);
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
			case R.id.talk_lv_tool_manual:
			{
				Intent it = new Intent(this, MenuManualActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tool_defect:
			{
				Intent it = new Intent(this, MenuDefectReportActivity.class);
				startActivity(it);
				break;
			}
			default:
				break;
		}
	}
}
