package com.cmccpoc.activity;

import android.app.Activity;
import android.os.Bundle;
import com.cmccpoc.util.ThemeUtil;

/**
 * 所有Activity的基类，主要设置了主题
 * @author Yao
 */
public class ActivityBase extends Activity
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ThemeUtil.setTheme(this);
	}
	
	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
	}

}
