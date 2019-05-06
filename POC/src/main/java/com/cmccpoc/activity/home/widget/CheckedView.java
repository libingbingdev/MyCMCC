package com.cmccpoc.activity.home.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class CheckedView extends RelativeLayout implements Checkable
{

	public CheckedView(Context context)
	{
		super(context);
	}

	public CheckedView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public CheckedView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	public void setChecked(boolean checked)
	{
		
	}

	@Override
	public boolean isChecked()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void toggle()
	{
		// TODO Auto-generated method stub

	}

}
