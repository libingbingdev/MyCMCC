package com.cmccpoc.activity.home.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 自定义TextView，用在频道列表界面上，当频道名称过长时，会以跑马灯的形式滚动
 * @author Yao
 *
 */
public class MarqueeTextView extends TextView
{
	
	public MarqueeTextView(Context context)
	{
		super(context);
	}

	public MarqueeTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public MarqueeTextView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	public boolean isFocused()
	{
		return true;
	}
}
