package com.cmccpoc.activity;

import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.adapter.AdapterImagePager;

/**
 * IM消息--图片消息原图展示Activity
 * IM图片消息，点击后显示大图，可以左右滑动切换
 * @author Yao
 */
public class ActivityImagePager extends Activity
{
	ViewPager pager;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setContentView(R.layout.activity_image_pager);
		pager = (ViewPager) findViewById(R.id.pager);
		bundle = this.getIntent().getExtras();
		if (bundle != null)
		{
			List<String> imageUrls = bundle.getStringArrayList("images");
			int pagerPosition = bundle.getInt("position", 0);
			pager.setAdapter(new AdapterImagePager(imageUrls, this));
			pager.setCurrentItem(pagerPosition);
		}
	}

}
