package com.cmccpoc.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.config.Config;
import com.cmccpoc.listener.OnMmiLocationListener;
import com.cmccpoc.location.AirLocation;
import com.cmccpoc.util.ThemeUtil;

/**
 * 更多：位置回传
 * 就是上报位置坐标信息的
 * 开启后，将会以一定频率回传我的位置信息
 * 优先获取GPS，若GPS获取不到则获取基站定位
 * @author Yao
 */
public class MenuGpsActivity extends ActivityBase implements OnClickListener, OnMmiLocationListener, OnCheckedChangeListener
{
	private TextView gps_t, gps_t_text;
	int[] mFrequenceValue = { AirLocation.AIR_LOCATION_FRE_NAVIGATE, AirLocation.AIR_LOCATION_FRE_MINUTE_1, AirLocation.AIR_LOCATION_FRE_MINUTE_5, AirLocation.AIR_LOCATION_FRE_MINUTE_15, AirLocation.AIR_LOCATION_FRE_MINUTE_30, AirLocation.AIR_LOCATION_FRE_MINUTE_60 };
	String[] mFrequence = null;
	String[] mState = null;
	int mStateSelected = 0;
	int mFrequenceSelected = 0;// index

	private CheckBox cbGPS;
	private CheckBox cbGPSHigh;
	boolean gpsState;
	private TextView gpsFrequenceText; // 回传频率
	private TextView gpsFrequenceHigh; // 高精度回传
	private RadioGroup rgGpsFrequence;
	private RadioButton rb1Min, rb5Min, rb15Min, rb30Min, rb60Min ,rbClose;
	//private TextView tv1Min, rb5Min, rb15Min, rb30Min, rb60Min;

	private Context context;

	private String mAddress, mTime;

	@Override
	protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_gps);
		context = this;
		gpsState = AirLocation.getInstance(context).getSettingState();
		mStateSelected = gpsState ? 0 : 1;
		int fre = AirLocation.getInstance(context).getSettingFrequence();
		for (int i = 0; i < mFrequenceValue.length; i++)
		{
			if (fre == mFrequenceValue[i])
			{
				mFrequenceSelected = i;
			}
		}
		doInitView();

		AirLocation.getInstance(this).setListener(this, AirLocation.AIR_LOCATION_ID_LOOP);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	/**
	 * 初始化绑定用户Id 
	 */
	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_location);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		mState = getResources().getStringArray(R.array.gps_state);
		gps_t = (TextView) findViewById(R.id.gps_t);
		gps_t_text = (TextView) findViewById(R.id.gps_t_text);

		gpsFrequenceText = (TextView) findViewById(R.id.gps_frequence_text);
		gpsFrequenceHigh = (TextView) findViewById(R.id.gps_frequence_high_text);

		cbGPS = (CheckBox) findViewById(R.id.talk_setting_gps);
		cbGPS.setChecked(gpsState);
		cbGPS.setOnCheckedChangeListener(this);

		cbGPSHigh = (CheckBox) findViewById(R.id.gps_frequence_high);
		cbGPSHigh.setOnCheckedChangeListener(this);

		rgGpsFrequence = (RadioGroup) findViewById(R.id.rg_gps_frequence);
		rgGpsFrequence.setOnCheckedChangeListener(listener);
		rb1Min = (RadioButton) findViewById(R.id.rb_1_min);
		rb5Min = (RadioButton) findViewById(R.id.rb_5_min);
		rb15Min = (RadioButton) findViewById(R.id.rb_15_min);
		rb30Min = (RadioButton) findViewById(R.id.rb_30_min);
		rb60Min = (RadioButton) findViewById(R.id.rb_60_min);
		rbClose = (RadioButton) findViewById(R.id.rb_close);
		/*tv1Min = (TextView) findViewById(R.id.tv_1_min);
		rb5Min = (TextView) findViewById(R.id.tv_5_min);
		rb15Min = (TextView) findViewById(R.id.tv_15_min);
		rb30Min = (TextView) findViewById(R.id.tv_30_min);
		rb60Min = (TextView) findViewById(R.id.tv_60_min);*/
		if(gpsState==false){
			rbClose.setChecked(true);
		}else{
			initRadioGroup(gpsState, mFrequenceValue[mFrequenceSelected]);
		}
	}

	private void closeGPSSettings() {
		int currentMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
		if (currentMode != Settings.Secure.LOCATION_MODE_OFF) {
			int newMode = Settings.Secure.LOCATION_MODE_OFF;
			Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
			intent.putExtra("CURRENT_MODE", currentMode);
			intent.putExtra("NEW_MODE", newMode);
			sendBroadcast(intent, android.Manifest.permission.WRITE_SECURE_SETTINGS);
			Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE, newMode);
		}
	}

	private void openGPSSettings() {
		int currentMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
		if (currentMode != Settings.Secure.LOCATION_MODE_HIGH_ACCURACY) {
			int newMode = Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
			Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
			intent.putExtra("CURRENT_MODE", currentMode);
			intent.putExtra("NEW_MODE", newMode);
			sendBroadcast(intent, android.Manifest.permission.WRITE_SECURE_SETTINGS);
			Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE, newMode);
		}
	}
	
	/**
	 * radioButton更改check时触发
	 */
	private RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId)
		{
			int rid = group.getCheckedRadioButtonId();
			switch (rid)
			{
				case R.id.rb_1_min:
				{
					if (rb1Min.isChecked()) {
						set1MinView(true);
						Settings.System.putInt(getContentResolver(), "location_return", 1);
						openGPSSettings();
					}
					break;
				}
				case R.id.rb_5_min:
				{
					if (rb5Min.isChecked()) {
						set5MinView(true);
						Settings.System.putInt(getContentResolver(), "location_return", 1);
						openGPSSettings();
					}
					break;
				}
				case R.id.rb_15_min:
				{
					if (rb15Min.isChecked()) {
						set15MinView(true);
						Settings.System.putInt(getContentResolver(), "location_return", 1);
						openGPSSettings();
					}
					break;
				}
				case R.id.rb_30_min:
				{
					if (rb30Min.isChecked()) {
						set30MinView(true);
						Settings.System.putInt(getContentResolver(), "location_return", 1);
						openGPSSettings();
					}
					break;
				}
				case R.id.rb_60_min:
				{
					if (rb60Min.isChecked()) {
						set60MinView(true);
						Settings.System.putInt(getContentResolver(), "location_return", 1);
						openGPSSettings();
					}
					break;
				}
				case R.id.rb_close:
				{
					if (rbClose.isChecked()) {
						setGpsView(false);
						AirLocation.getInstance(context).loopTerminate();
						Settings.System.putInt(getContentResolver(), "location_return", 0);
						closeGPSSettings();
					}
					break;
				}
				default:
					// setHighFreView(true);
					break;
			}
		}
	};

	/**
	 * 初始化RadioGroup控件
	 * @param state GPS状态
	 * @param value 回传频率
	 */
	private void initRadioGroup(boolean state, int value)
	{
		if (state)
		{
			switch (value)
			{
				case AirLocation.AIR_LOCATION_FRE_NAVIGATE:
				{
					cbGPSHigh.setChecked(true);
					break;
				}
				case AirLocation.AIR_LOCATION_FRE_MINUTE_1:
				{
					rb1Min.setChecked(true);
					break;
				}
				case AirLocation.AIR_LOCATION_FRE_MINUTE_5:
				{
					rb5Min.setChecked(true);
					break;
				}
				case AirLocation.AIR_LOCATION_FRE_MINUTE_15:
				{
					rb15Min.setChecked(true);
					break;
				}
				case AirLocation.AIR_LOCATION_FRE_MINUTE_30:
				{
					rb30Min.setChecked(true);
					break;
				}
				case AirLocation.AIR_LOCATION_FRE_MINUTE_60:
				{
					rb60Min.setChecked(true);
					break;
				}
				default:
					break;
			}
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, value, true);
		}
		else
		{
			setGpsView(false);
		}
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
		AirLocation.getInstance(this).setListener(null, AirLocation.AIR_LOCATION_ID_LOOP);
	}

	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			gps_t_text.setText(mAddress);
			gps_t.setText(mTime);
		}
	};

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		// final Context context = this;
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
		}
	}

	@Override
	public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time)
	{
		Log.d(MenuGpsActivity.class, "[LOCATION] MenuGpsActivity time = " + time);
		//		if (isOk && id == AirLocation.AIR_LOCATION_ID_LOOP)
		//		{
		//			refreshGpsData(type, latitude, longitude, altitude, speed, time);
		//		}
	}
	
	@Override
	public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time, String address)
	{
		Log.d(MenuGpsActivity.class, "[LOCATION] MenuGpsActivity onLocationChanged addr = " + address + ", time = " + time);
		if (isOk && id == AirLocation.AIR_LOCATION_ID_LOOP)
		{
			mAddress = address + "附近";
			mTime = time;
			mHandler.sendEmptyMessage(0);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		final Context context = MenuGpsActivity.this;
		if (buttonView != null)
		{
			switch (buttonView.getId())
			{
				case R.id.talk_setting_gps:
				{
					if (cbGPS.isChecked())// 是否开启位置回传
					{
						setGpsView(true);
					}
					else
					{
						setGpsView(false);
						AirLocation.getInstance(context).loopTerminate();
						gps_t_text.setText("");
						gps_t.setText("");
					}
					break;
				}
				case R.id.gps_frequence_high:
				{
					if (cbGPSHigh.isChecked())// 是否开启高精度模式
					{
						if (cbGPS.isChecked())
						{
							setHighFreView(true);
							AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_NAVIGATE, true);
						}
					}
					else
					{
						setHighFreView(false);
						switch (mFrequenceValue[mFrequenceSelected])
						{
							case AirLocation.AIR_LOCATION_FRE_MINUTE_1:
								rb1Min.setChecked(true);
								AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_1, true);
								break;
							case AirLocation.AIR_LOCATION_FRE_MINUTE_5:
								rb5Min.setChecked(true);
								AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_5, true);
								break;
							case AirLocation.AIR_LOCATION_FRE_MINUTE_15:
								rb15Min.setChecked(true);
								AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_15, true);
								break;
							case AirLocation.AIR_LOCATION_FRE_MINUTE_30:
								rb30Min.setChecked(true);
								AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_30, true);
								break;
							case AirLocation.AIR_LOCATION_FRE_MINUTE_60:
								rb60Min.setChecked(true);
								AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_60, true);
								break;
							default:
								rb5Min.setChecked(true);
								AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_5, true);
								break;
						}
					}
					break;
				}
				default:
					break;
			}
		}
	}

	/**
	 * 设置GPSView
	 * @param b 是否选择
	 */
	private void setGpsView(boolean b)
	{
		if (b)
		{
			gpsFrequenceText.setTextColor(getResources().getColor(R.color.black));
			gpsFrequenceHigh.setTextColor(getResources().getColor(R.color.black_gray));
			cbGPSHigh.setEnabled(true);

			switch (mFrequenceSelected)
			{
				case 0:
					setHighFreView(true);
					cbGPSHigh.setChecked(true);
					AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_NAVIGATE, true);
					break;
				case 1:
					rb1Min.setChecked(true);
					AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_1, true);
					set1MinView(true);
					break;
				case 2:
					rb5Min.setChecked(true);
					AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_5, true);
					set5MinView(true);
					break;
				case 3:
					rb15Min.setChecked(true);
					AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_15, true);
					set15MinView(true);
					break;
				case 4:
					rb30Min.setChecked(true);
					AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_30, true);
					set30MinView(true);
					break;
				case 5:
					rb60Min.setChecked(true);
					AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_60, true);
					set60MinView(true);
					break;
				default:
					break;
			}
		}
		else
		{
			if (mFrequenceSelected == 0)
			{
				cbGPSHigh.setChecked(true);
			}
			gpsFrequenceText.setTextColor(getResources().getColor(R.color.text_gray));
			gpsFrequenceHigh.setTextColor(getResources().getColor(R.color.text_gray));
			cbGPSHigh.setEnabled(false);
		}
	}

	/**
	 * 选择高精度
	 * @param b 是否选择
	 */
	private void setHighFreView(boolean b)
	{
		if (b) // 高精度
		{
			mStateSelected = 0;
			mFrequenceSelected = 0;

			set1MinView(false);
			set5MinView(false);
			set15MinView(false);
			set30MinView(false);
			set60MinView(false);
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_NAVIGATE, true);
		}
	}

	/**
	 * 选择1分钟
	 * @param b 是否选择
	 */
	private void set1MinView(boolean b)
	{
		if (b)
		{
			mFrequenceSelected = 1;
			set5MinView(false);
			set15MinView(false);
			set30MinView(false);
			set60MinView(false);
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_1, true);
		}
	}

	/**
	 * 选择5分钟
	 * @param b 是否选择
	 */
	private void set5MinView(boolean b)
	{
		if (b)
		{
			mFrequenceSelected = 2;
			set1MinView(false);
			set15MinView(false);
			set30MinView(false);
			set60MinView(false);
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_5, true);
		}
	}

	/**
	 * 选择15分钟
	 * @param b 是否选择
	 */
	private void set15MinView(boolean b)
	{
		if (b)
		{
			mFrequenceSelected = 3;
			set5MinView(false);
			set1MinView(false);
			set30MinView(false);
			set60MinView(false);
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_15, true);
		}
	}

	/**
	 * 选择30分钟
	 * @param b 是否选择
	 */
	private void set30MinView(boolean b)
	{
		if (b)
		{
			mFrequenceSelected = 4;
			set5MinView(false);
			set15MinView(false);
			set1MinView(false);
			set60MinView(false);
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_30, true);
		}
	}

	/**
	 * 选择60分钟
	 * @param b 是否选择
	 */
	private void set60MinView(boolean b)
	{
		if (b)
		{
			mFrequenceSelected = 5;
			set5MinView(false);
			set15MinView(false);
			set30MinView(false);
			set1MinView(false);
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_60, true);
		}
	}

}
