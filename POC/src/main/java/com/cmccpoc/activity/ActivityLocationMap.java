package com.cmccpoc.activity;

import org.json.JSONObject;
import org.json.JSONTokener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.sdk.util.Log;
import com.amap.api.maps2d.AMapException;
import com.amap.api.maps2d.AMapUtils;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;
import com.baidu.mapapi.utils.OpenClientUtil;
import com.cmccpoc.R;
import com.cmccpoc.config.Config;
import com.cmccpoc.entity.LocationBean;
import com.cmccpoc.util.MapUtilBaidu;
import com.cmccpoc.util.MapUtilBaidu.LocateListener;
import com.cmccpoc.util.ThemeUtil;

public class ActivityLocationMap extends Activity implements OnClickListener
{
	private Context mContext;
	private TextView tvLocationTitle, tvLocationAddr;
	private ImageView ivLocationNav;
	private View popMapView, popMapPanel;
	private PopupWindow popMapWindow;// 弹出窗口

	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private LocationBean mLocationBean;
	private OrientationListener orientationListener;

	int mXDirection;

	// 百度地图导航参数
	private LatLng bFromPoint, bEndPoint;
	private NaviParaOption bParams;
	// 高德地图导航参数
	private com.amap.api.maps2d.model.NaviPara aParams;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_location_map);
		mContext = this;
		aParams = new com.amap.api.maps2d.model.NaviPara();
		bParams = new NaviParaOption();
		bParams.startName("从这里开始");
		orientationListener = new OrientationListener(getApplicationContext());
		initView();
		savedInstanceState = getIntent().getExtras();
		if (savedInstanceState != null)
		{
			String msg = savedInstanceState.getString("message");
			JSONTokener jsonParser = new JSONTokener(msg);
			try
			{
				JSONObject location = (JSONObject) jsonParser.nextValue();
				tvLocationTitle.setText(location.getString("name"));
				tvLocationAddr.setText(location.getString("address"));
				initBaiduMap();
				locate();
				MapUtilBaidu.showMarkerByResource(location.getDouble("latitude"), location.getDouble("longitude"), R.drawable.ic_map_pointer, mBaiduMap, 0, true);
				bEndPoint = new LatLng(location.getDouble("latitude"), location.getDouble("longitude"));
				bParams.endPoint(bEndPoint);
				bParams.endName(location.getString("name"));
				aParams.setTargetPoint(new com.amap.api.maps2d.model.LatLng(location.getDouble("latitude"), location.getDouble("longitude")));
			}
			catch (Exception e)
			{ }
		}
		initOritationListener();
	}

	@Override
	protected void onStart()
	{
		// 关闭图层定位
		mBaiduMap.setMyLocationEnabled(true);
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// orientationListener.start();
			}
		}).start();

		super.onStart();
	}

	@Override
	protected void onStop()
	{
		// 关闭图层定位
		mBaiduMap.setMyLocationEnabled(false);
		// orientationListener.stop();
		super.onStop();
	}

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

	@SuppressWarnings("deprecation")
	private void initView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_title_location_msg);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		tvLocationTitle = (TextView) findViewById(R.id.tv_location_title);
		tvLocationAddr = (TextView) findViewById(R.id.tv_location_detail);
		ivLocationNav = (ImageView) findViewById(R.id.iv_location_nav);
		ivLocationNav.setOnClickListener(this);
		popMapPanel = findViewById(R.id.window_map_panel);
		popMapPanel.getBackground().setAlpha(200);
		popMapView = LayoutInflater.from(this).inflate(R.layout.layout_popup_window_map, null);
		popMapWindow = new PopupWindow(popMapView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		popMapWindow.setOutsideTouchable(true);
		popMapWindow.setFocusable(true);
		popMapWindow.setBackgroundDrawable(new BitmapDrawable());
		popMapWindow.setOnDismissListener(new OnDismissListener()
		{
			@Override
			public void onDismiss()
			{
				popMapPanel.setVisibility(View.GONE);
			}
		});
		popMapView.findViewById(R.id.baidu_map_pannel).setOnClickListener(this);
		popMapView.findViewById(R.id.amap_pannel).setOnClickListener(this);
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
		UiSettings mapSettings = mBaiduMap.getUiSettings();
		mapSettings.setRotateGesturesEnabled(false);
		mapSettings.setOverlookingGesturesEnabled(false);
	}

	public void locate()
	{
		MapUtilBaidu.locateByBaiduMap(mContext, 1000, new LocateListener()
		{
			@Override
			public void onLocateSucceed(LocationBean locationBean)
			{
				mLocationBean = locationBean;
				MapUtilBaidu.showMarkerByResource(locationBean.getLatitude(), locationBean.getLongitude(), R.drawable.ic_location_me, mBaiduMap, 0, false);
				// MapUtilBaidu.moveToTarget(locationBean.getLatitude(),
				// locationBean.getLongitude(), mBaiduMap);
				bFromPoint = new LatLng(locationBean.getLatitude(), locationBean.getLongitude());
				bParams.startPoint(bFromPoint);
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

	/**
	 * 初始化方向传感器
	 */
	private void initOritationListener()
	{
		orientationListener.setOnOrientationListener(new OnOrientationListener()
		{
			@Override
			public void onOrientationChanged(float x)
			{
				try
				{
					Log.i(ActivityLocationMap.class, "ActivityLocationMap onOrientationChanged mXDirection = " + x);
					mXDirection = (int) x;
					// 构造定位数据
					MyLocationData locData = new MyLocationData.Builder().accuracy(mLocationBean.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(mXDirection).latitude(mLocationBean.getLatitude()).longitude(mLocationBean.getLongitude()).build();
					// 设置定位数据
					mBaiduMap.setMyLocationData(locData);
					// 设置自定义图标
					BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_me);
					MyLocationConfiguration config = new MyLocationConfiguration(LocationMode.NORMAL, true, mCurrentMarker);
					mBaiduMap.setMyLocationConfigeration(config);
				}
				catch (Exception e)
				{}

			}
		});
	}

	private boolean isCanUpdateMap = true;
	private OnMapStatusChangeListener mapStatusChangeListener = new OnMapStatusChangeListener()
	{
		/**
		 * 手势操作地图，设置地图状态等操作导致地图状态开始改变。
		 * 
		 * @param status
		 *            地图状态改变开始时的地图状态
		 */
		public void onMapStatusChangeStart(MapStatus status)
		{
		}

		/**
		 * 地图状态变化中
		 * 
		 * @param status
		 *            当前地图状态
		 */
		public void onMapStatusChange(MapStatus status)
		{
		}

		/**
		 * 地图状态改变结束
		 * 
		 * @param status
		 *            地图状态改变结束后的地图状态
		 */
		public void onMapStatusChangeFinish(MapStatus status)
		{
			if (isCanUpdateMap)
			{
				LatLng ptCenter = new LatLng(status.target.latitude, status.target.longitude);
				MapStatusUpdateFactory.newLatLng(ptCenter);
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
			case R.id.iv_location_nav:
			{
				popMapPanel.setVisibility(View.VISIBLE);
				popMapWindow.showAtLocation(popMapView, Gravity.CENTER | Gravity.CENTER, 0, 0);
				break;
			}
			case R.id.baidu_map_pannel:
			{
				popMapWindow.dismiss();
				try
				{
					BaiduMapNavigation.openBaiduMapNavi(bParams, this);
				}
				catch (BaiduMapAppNotSupportNaviException e)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
					builder.setTitle("提示");
					builder.setPositiveButton("确认", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
							OpenClientUtil.getLatestBaiduMapApp(ActivityLocationMap.this);
						}
					});

					builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder.create().show();
				}
				break;
			}
			case R.id.amap_pannel:
			{
				popMapWindow.dismiss();
				try
				{
					AMapUtils.openAMapNavi(aParams, this);
				}
				catch (AMapException e)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage("您尚未安装高德地图app或app版本过低，点击确认安装？");
					builder.setTitle("提示");
					builder.setPositiveButton("确认", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
							AMapUtils.getLatestAMapApp(ActivityLocationMap.this);
						}
					});

					builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder.create().show();
				}
				break;
			}
			default:
				break;
		}
	}

	@SuppressWarnings("deprecation")
	class OrientationListener implements SensorEventListener
	{
		private Context context;
		private SensorManager sensorManager;
		private Sensor sensor;

		private float lastX;

		private OnOrientationListener onOrientationListener;

		public OrientationListener(Context context)
		{
			this.context = context;
		}

		public void setOnOrientationListener(OnOrientationListener onOrientationListener)
		{
			this.onOrientationListener = onOrientationListener;
		}

		// 开始
		public void start()
		{
			// 获得传感器管理器
			sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
			if (sensorManager != null)
			{
				// 获得方向传感器
				sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
			}
			// 注册
			if (sensor != null)
			{// SensorManager.SENSOR_DELAY_UI
				sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
			}

		}

		// 停止检测
		public void stop()
		{
			sensorManager.unregisterListener(this);
		}

		@Override
		public void onSensorChanged(SensorEvent event)
		{
			// 接受方向感应器的类型
			if (event.sensor.getType() == Sensor.TYPE_ORIENTATION)
			{
				// 这里我们可以得到数据，然后根据需要来处理
				float x = event.values[SensorManager.DATA_X];
				if (Math.abs(x - lastX) > 1.0)
				{
					onOrientationListener.onOrientationChanged(x);
				}
				lastX = x;
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{

		}
	}

	public interface OnOrientationListener
	{
		void onOrientationChanged(float x);
	}
}
