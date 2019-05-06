package com.cmccpoc.activity.home.widget;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.cmccpoc.R;

/**
 * PTT媒体按键下方的底部按钮
 * @author Yao
 */
public class StatusBarBottom extends LinearLayout implements OnClickListener
{
	public interface OnBarItemClickListener
	{
		public void onBarItemClick(int itemId, int page);
	}

	public static final String ACTION_BAR_ITEMCLICK = "com.airtalkee.ACTION_BAR_ITEMCLICK";
	public static final String EXTRA_PAGE = "page";
	public static final String EXTRA_ID = "id";

	private ImageView ivBtnLeft, ivBtnMid, ivBtnRight;
	private int page = 0;
	private OnBarItemClickListener listener;
	private int leftRes, midRes, rightRes;

	public StatusBarBottom(int leftRes, int midRes, int rightRes, int page, Context context, OnBarItemClickListener l)
	{
		super(context);
		this.listener = l;
		this.page = page;
		this.leftRes = leftRes;
		this.midRes = midRes;
		this.rightRes = rightRes;

		LayoutInflater.from(this.getContext()).inflate(R.layout.include_function, this);
		
		initFindView();
		setVisibility(View.GONE);
	}

	/**
	 * 初始化绑定控件
	 */
	private void initFindView()
	{
		ivBtnLeft = (ImageView) findViewById(R.id.bar_left);
		ivBtnMid = (ImageView) findViewById(R.id.bar_mid);
		ivBtnRight = (ImageView) findViewById(R.id.bar_right);

		ivBtnLeft.setImageResource(leftRes);
		ivBtnMid.setImageResource(midRes);
		ivBtnRight.setImageResource(rightRes);

		ivBtnLeft.setOnClickListener(this);
		ivBtnMid.setOnClickListener(this);
		ivBtnRight.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0)
	{
		// TODO Auto-generated method stub
		if (listener != null)
			listener.onBarItemClick(arg0.getId(), page);

		final Intent intent = new Intent();
		intent.setAction(ACTION_BAR_ITEMCLICK);
		intent.putExtra(EXTRA_PAGE, page);
		intent.putExtra(EXTRA_ID, arg0.getId());
		
		getContext().sendBroadcast(intent);
	}
}
