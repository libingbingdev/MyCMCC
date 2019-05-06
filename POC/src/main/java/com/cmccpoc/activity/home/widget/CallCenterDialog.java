package com.cmccpoc.activity.home.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cmccpoc.R;

/**
 * 自定义弹窗，可以自定义确定、取消按钮名称与功能（通过实现DialogListener接口，重写里面的方法）
 * @author Yao
 */
public class CallCenterDialog extends Dialog implements View.OnClickListener
{

	protected RadioButton buttonAdmin, buttonAttendence;
	private Context context = null;
	protected View c;
	protected View s;
	protected Button cancle, sure;

	protected CallCenterDialogListener listener;

	public interface CallCenterDialogListener
	{
		void onClickOnCallForAdministrator();

		void onClickOnCallForAttendence();

		void onClickOnCallCancel();
	}

	public void setListener(CallCenterDialogListener listener)
	{
		this.listener = listener;
	}

	public CallCenterDialog(Context context)
	{
		super(context, R.style.alert_dialog);
		this.context = context;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_call_center_layout);
		initView();
	}

	/**
	 * 初始化绑定窗扣内空间
	 */
	protected void initView()
	{
		buttonAdmin = (RadioButton) findViewById(R.id.radioAdmin);
		buttonAttendence = (RadioButton) findViewById(R.id.radioAttendence);
		c = findViewById(R.id.cancle);
		s = findViewById(R.id.sure);
		if (c instanceof Button)
			cancle = (Button) c;
		if (c instanceof Button)
			sure = (Button) s;
		c.setOnClickListener(this);
		s.setOnClickListener(this);
	}


	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.sure:
			{
				this.dismiss();
				if (null != listener)
				{
					if (buttonAdmin.isChecked())
					{
						listener.onClickOnCallForAdministrator();
					}
					else if (buttonAttendence.isChecked())
					{
						listener.onClickOnCallForAttendence();
					}
				}
				break;
			}
			case R.id.cancle:
			{
				this.cancel();
				if (null != listener)
					listener.onClickOnCallCancel();
				break;
			}
		}

	}

}
