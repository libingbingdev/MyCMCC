package com.cmccpoc.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.entity.AirSession;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.activity.home.adapter.AdapterPoiAroundInfo;
import com.cmccpoc.config.Config;
import com.cmccpoc.entity.LocationBean;
import com.cmccpoc.util.MapUtilBaidu;
import com.cmccpoc.util.MapUtilBaidu.GeoCodePoiListener;
import com.cmccpoc.util.MapUtilBaidu.LocateListener;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapPointBaiduActivity extends Activity implements OnClickListener, OnItemClickListener
{
	static final int RESULT_LOCATION_SEARCH = 100;

	private static Context mContext;

	private ImageView ivMapSearch, ivMapLocate;
	private ImageView ivLoading;
	private ListView lvPoiList;

	/**
	 * 定位poi地名信息数据源
	 */
	// mPoiInfos的选中项
	private PoiInfo mPoiInfo;
	private List<PoiInfo> mPoiInfos;
	private AdapterPoiAroundInfo mPoiInfoAdpter;

	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private Marker mMarker;
	// 地图中点位置
	private LocationBean mLocationBean;
	
	private LatLng ptCenter = null;

	// 延时多少秒diss掉dialog
	private static final int DELAY_DISMISS = 1000 * 30;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		try
		{
			setRequestedOrientation(Config.screenOrientation);
			// SDKInitializer.initialize(getApplicationContext());
			setContentView(R.layout.activity_baidu_map);
			mContext = this;
			initView();
			initBaiduMap();
			locate();
		}
		catch (Exception e)
		{ }
	}

	private void initView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_title_location);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ivRightLay.setOnClickListener(this);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		TextView tvSendLocation = (TextView) findViewById(R.id.tv_orange_button);
		tvSendLocation.setText(getString(R.string.talk_channel_btn_send_msg));
		tvSendLocation.setVisibility(View.VISIBLE);
		tvSendLocation.setOnClickListener(this);

		ivMapSearch = (ImageView) findViewById(R.id.iv_map_search);
		ivMapSearch.setOnClickListener(this);
		ivMapLocate = (ImageView) findViewById(R.id.iv_map_locate);
		ivMapLocate.setOnClickListener(this);

		ivLoading = (ImageView) findViewById(R.id.iv_loading);
		lvPoiList = (ListView) findViewById(R.id.lv_poi_list);
		lvPoiList.setOnItemClickListener(this);
	}

	private void initBaiduMap()
	{
		mMapView = (MapView) findViewById(R.id.mv_baidu);
		mBaiduMap = mMapView.getMap();
		// 地图上比例尺
		mMapView.showScaleControl(false);
		// 隐藏缩放控件
		MapUtilBaidu.goneMapViewChild(mMapView, true, true);
		// 普通地图
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
		// 改变地图状态
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));
		// 设置地图状态监听者
		mBaiduMap.setOnMapStatusChangeListener(mapStatusChangeListener);
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
	}

	public void locate()
	{
		MapUtilBaidu.locateByBaiduMap(mContext, 1000, new LocateListener()
		{
			@Override
			public void onLocateSucceed(LocationBean locationBean)
			{
				try
				{
					mLocationBean = locationBean;
					if (mMarker != null)
						mMarker.remove();
					else if (mBaiduMap != null)
					{
						mBaiduMap.clear();
					}
					mMarker = MapUtilBaidu.showMarkerByResource(locationBean.getLatitude(), locationBean.getLongitude(), R.drawable.ic_map_bottom, mBaiduMap, 0, true);
					if (ptCenter == null)
					{
						ptCenter = new LatLng(locationBean.getLatitude(), locationBean.getLongitude());
						isCanUpdateMap = false;
						reverseGeoCode(ptCenter);
						mPoiInfo = (PoiInfo) mPoiInfoAdpter.getItem(0);
						mPoiInfoAdpter.setSelected(0);
						MapUtilBaidu.moveToTarget(ptCenter, mBaiduMap);
						ptCenter = null;
					}
				}
				catch (Exception e)
				{ }
			}

			@Override
			public void onLocateFiled()
			{

			}

			@Override
			public void onLocating()
			{

			}
		});
	}

	public void reverseGeoCode(LatLng ll)
	{
		MapUtilBaidu.getPoisByGeoCode(ll.latitude, ll.longitude, new GeoCodePoiListener()
		{
			@Override
			public void onGetSucceed(LocationBean locationBean, List<PoiInfo> poiList)
			{
				mLocationBean = (LocationBean) locationBean.clone();
				if(Toast.isDebug) Toast.makeText1(mContext, mLocationBean.getProvince() + "-" + mLocationBean.getCity() + "-" + mLocationBean.getDistrict() + "-" + mLocationBean.getStreet(), Toast.LENGTH_SHORT).show();
				if (mPoiInfos == null)
				{
					mPoiInfos = new ArrayList<PoiInfo>();
				}
				mPoiInfos.clear();
				if (poiList != null)
				{
					mPoiInfos.addAll(poiList);
				}
				else
				{
					Toast.makeText(mContext, "该周边没热点", Toast.LENGTH_SHORT).show();
				}
				updatePoiListAdapter(mPoiInfos, 0);
			}

			@Override
			public void onGetFailed()
			{
				Toast.makeText(mContext, "抱歉，未能找到结果", Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * 刷新热门地名列表界面的adapter
	 * 
	 * @param list
	 * @param index
	 */
	private void updatePoiListAdapter(List<PoiInfo> list, int index)
	{
		ivLoading.clearAnimation();
		ivLoading.setVisibility(View.GONE);
		lvPoiList.setVisibility(View.VISIBLE);
		if (mPoiInfoAdpter == null)
		{
			if (list.size() > 0)
				mPoiInfoAdpter = new AdapterPoiAroundInfo(mContext, list, 0);
			else
				mPoiInfoAdpter = new AdapterPoiAroundInfo(mContext, list, -1);
			lvPoiList.setAdapter(mPoiInfoAdpter);
		}
		else
		{
			mPoiInfoAdpter.setNewList(list, index);
		}
		if (list.size() > 0)
			mPoiInfo = list.get(0);
	}

	private static Animation hyperspaceJumpAnimation = null;
	Handler loadingHandler = new Handler()
	{
		@Override
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.what)
			{
				case 0:
				{
					if (ivLoading != null)
					{
						ivLoading.clearAnimation();
						ivLoading.setVisibility(View.GONE);
					}
					break;
				}
				case 1:
				{
					// 加载动画
					hyperspaceJumpAnimation = AnimationUtils.loadAnimation(mContext, R.anim.dialog_loading_animation);
					lvPoiList.setVisibility(View.GONE);
					ivLoading.setVisibility(View.VISIBLE);
					// 使用ImageView显示动画
					ivLoading.startAnimation(hyperspaceJumpAnimation);
					if (ivLoading != null && ivLoading.getVisibility() == View.VISIBLE)
					{
						loadingHandler.sendEmptyMessageDelayed(0, DELAY_DISMISS);
					}
					break;
				}
				default:
					break;
			}
		}
	};

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		if (mMapView != null)
		{
			mMapView.onDestroy();
			mMapView = null;
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		if (mMapView != null)
			mMapView.onResume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		if (mMapView != null)
			mMapView.onPause();
	}

	private boolean isCanUpdateMap = true;
	private OnMapStatusChangeListener mapStatusChangeListener = new OnMapStatusChangeListener()
	{
		/**
		 * 手势操作地图，设置地图状态等操作导致地图状态开始改变。
		 * @param status 地图状态改变开始时的地图状态
		 */
		public void onMapStatusChangeStart(MapStatus status) { }

		/**
		 * 地图状态变化中
		 * @param status 当前地图状态
		 */
		public void onMapStatusChange(MapStatus status) { }

		/**
		 * 地图状态改变结束
		 * @param status 地图状态改变结束后的地图状态
		 */
		public void onMapStatusChangeFinish(MapStatus status)
		{
			if (isCanUpdateMap)
			{
				ptCenter = new LatLng(status.target.latitude, status.target.longitude);
				// 反Geo搜索
				reverseGeoCode(ptCenter);
				if (ivLoading != null && ivLoading.getVisibility() == View.GONE)
				{
					loadingHandler.sendEmptyMessageDelayed(1, 0);
				}
			}
			else
			{
				isCanUpdateMap = true;
			}
		}
	};

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
			{
				finish();
				break;
			}
			case R.id.talk_menu_right_button:
			case R.id.tv_orange_button:
			{
				try
				{
					AirSession session = HomeActivity.getInstance().getSession();
					if (session != null)
					{
						double latitude = mPoiInfo.location.latitude;
						double longitude = mPoiInfo.location.longitude;
						JSONObject json = new JSONObject();
						try
						{
							json.put("latitude", latitude);
							json.put("longitude", longitude);
							json.put("name", mPoiInfo.name);
							json.put("address", mPoiInfo.address);
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
						String msgBody = json.toString();
						AirtalkeeMessage.getInstance().MessageLocationSend(session, msgBody, true);
						finish();
					}
					else
					{
						if(Toast.isDebug) Toast.makeText1(this, getString(R.string.talk_channel_idle), Toast.LENGTH_LONG).show();
					}
				}
				catch (Exception e)
				{
					if(Toast.isDebug) Toast.makeText1(this, getString(R.string.talk_title_location_send_error), Toast.LENGTH_LONG).show();
					finish();
				}
				break;
			}
			case R.id.iv_map_search:
			{
				Intent it = new Intent(this, ActivityLocationSearch.class);
				startActivityForResult(it, RESULT_LOCATION_SEARCH);
				break;
			}
			case R.id.iv_map_locate:
			{
				locate();
				break;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		isCanUpdateMap = false;
		mPoiInfo = (PoiInfo) mPoiInfoAdpter.getItem(position);
		mPoiInfoAdpter.setSelected(position);
		MapUtilBaidu.moveToTarget(mPoiInfo.location.latitude, mPoiInfo.location.longitude, mBaiduMap);
		mPoiInfoAdpter.notifyDataSetChanged();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (mMapView != null)
			mMapView.onResume();
		switch (requestCode)
		{
			case RESULT_LOCATION_SEARCH:
			{
				if (resultCode == RESULT_OK)
				{
					Bundle bundle = data.getExtras();
					if (bundle != null)
					{
						ptCenter = new LatLng(bundle.getDouble("latitude"), bundle.getDouble("longitude"));
					}
				}
				break;
			}
		}
	}
}
