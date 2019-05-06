package com.cmccpoc.activity;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.baidu.mapapi.search.poi.PoiResult;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.adapter.AdapterPoiSearchInfo;
import com.cmccpoc.entity.LocationBean;
import com.cmccpoc.util.MapUtilBaidu;
import com.cmccpoc.util.MapUtilBaidu.LocateListener;
import com.cmccpoc.util.MapUtilBaidu.PoiSearchListener;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.Util;

public class ActivityLocationSearch extends Activity implements OnClickListener, TextWatcher, OnItemClickListener
{
	private Context mContext;

	private EditText etMapSearch;
	private View rlMapSearchView;
	private TextView tvMapSearch;
	private ListView lvSearchList;

	private LocationBean mLocationBean;
	private static List<LocationBean> searchPoiList;
	private AdapterPoiSearchInfo mSearchPoiAdapter;

	private boolean isNullOrEmpty = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_search);
		mContext = this;
		initView();
		locate();
	}

	private void initView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_title_location_search);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ivRightLay.setVisibility(View.INVISIBLE);

		etMapSearch = (EditText) findViewById(R.id.et_map_search);
		etMapSearch.addTextChangedListener(this);
		etMapSearch.requestFocus();
		rlMapSearchView = findViewById(R.id.rl_map_search);
		rlMapSearchView.setClickable(false);
		tvMapSearch = (TextView) findViewById(R.id.tv_map_search);
		tvMapSearch.setClickable(false);
		lvSearchList = (ListView) findViewById(R.id.lv_poi_list);
		lvSearchList = (ListView) findViewById(R.id.lv_poi_list);
		lvSearchList.setOnItemClickListener(this);
	}

	@Override
	public void finish()
	{
		Util.hideSoftInput(this);
		super.finish();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.rl_map_search:
			case R.id.tv_map_search:
			{
				getPoiByPoiSearch();
				break;
			}
			case R.id.menu_left_button:
			{
				finish();
				break;
			}
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{

	}

	@Override
	public void afterTextChanged(Editable s)
	{
		String content = etMapSearch.getText().toString();
		if (content != null && content.trim().length() > 0)
		{
			if (isNullOrEmpty)
			{
				rlMapSearchView.setOnClickListener(this);
				tvMapSearch.setOnClickListener(this);
				rlMapSearchView.setBackgroundResource(R.drawable.selector_button_map_search);
				tvMapSearch.setTextColor(getResources().getColor(R.color.white));
			}
			isNullOrEmpty = false;
		}
		else
		{
			if (!isNullOrEmpty)
			{
				rlMapSearchView.setClickable(false);
				tvMapSearch.setClickable(false);
				rlMapSearchView.setBackgroundResource(R.drawable.bg_map_search_gray);
				tvMapSearch.setTextColor(getResources().getColor(R.color.update_text_none));
			}
			isNullOrEmpty = true;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		mLocationBean = (LocationBean) mSearchPoiAdapter.getItem(position);
		mSearchPoiAdapter.setSelected(position);
		Intent data = new Intent();
		data.putExtra("latitude", mLocationBean.getLatitude());
		data.putExtra("longitude", mLocationBean.getLongitude());
		setResult(RESULT_OK, data);
		finish();
	}

	public void locate()
	{
		MapUtilBaidu.locateByBaiduMap(mContext, 2000, new LocateListener()
		{
			@Override
			public void onLocateSucceed(LocationBean locationBean)
			{
				mLocationBean = locationBean;
			}

			@Override
			public void onLocateFiled() { }

			@Override
			public void onLocating() { }
		});
	}

	public void getPoiByPoiSearch()
	{
		MapUtilBaidu.getPoiByPoiSearch(mLocationBean.getCity(), etMapSearch.getText().toString().trim(), 0, new PoiSearchListener()
		{
			@Override
			public void onGetSucceed(List<LocationBean> locationList, PoiResult res)
			{
				if (etMapSearch.getText().toString().trim().length() > 0)
				{
					if (searchPoiList == null)
					{
						searchPoiList = new ArrayList<LocationBean>();
					}
					searchPoiList.clear();
					searchPoiList.addAll(locationList);
					updateCityPoiListAdapter();
				}
			}

			@Override
			public void onGetFailed()
			{
				Toast.makeText(mContext, "抱歉，未能找到结果", Toast.LENGTH_SHORT).show();
			}
		});
	}

	// 刷新当前城市兴趣地点列表界面的adapter
	private void updateCityPoiListAdapter()
	{
		if (mSearchPoiAdapter == null)
		{
			mSearchPoiAdapter = new AdapterPoiSearchInfo(mContext, searchPoiList);
			lvSearchList.setAdapter(mSearchPoiAdapter);
		}
		else
		{
			mSearchPoiAdapter.notifyDataSetChanged();
		}
	}

}
