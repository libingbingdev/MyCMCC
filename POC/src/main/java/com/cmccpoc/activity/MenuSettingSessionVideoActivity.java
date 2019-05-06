package com.cmccpoc.activity;

import android.os.Bundle;
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
import com.cmccpoc.R;
import com.cmccpoc.config.Config;
import com.cmccpoc.util.Setting;
import com.cmccpoc.util.ThemeUtil;

/**
 * 更多：实时视频设置
 * 
 * @author Yao
 */
public class MenuSettingSessionVideoActivity extends ActivityBase implements
		OnClickListener, OnCheckedChangeListener
{
	private CheckBox cbVideoSettingType;
	private RadioGroup rgQuality, rgRateFrequence, rgFpsFrequence;
	private RadioButton rbQualityLow, rbQualityNormal, rbQualityHigh, rbQualityBest;
	private RadioButton rbRate320, rbRate480, rbRate720;
	private RadioButton rbFps10, rbFps15, rbFps20, rbFps25, rbFps30;
	private TextView tvSettingRate, tvSettingFps;

	// private int[] fpsFrequence = { 10, 15, 20, 25, 30 };

	@Override
	protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_setting_session_video);
		doInitView();
		initRgQuality();
		initRgRate();
		initRgFps();
		setVideoSettingState(Setting.getVideoSettingType() == 0 ? false : true);
		cbVideoSettingType.setChecked(Setting.getVideoSettingType() == 0 ? false : true);
	}

	/**
	 * 初始化绑定控件Id
	 */
	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_video);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		cbVideoSettingType = (CheckBox) findViewById(R.id.cb_video_setting_type);
		cbVideoSettingType.setOnCheckedChangeListener(this);

		rgQuality = (RadioGroup) findViewById(R.id.rg_quality_item);
		rgRateFrequence = (RadioGroup) findViewById(R.id.rg_rate_frequence);
		rgFpsFrequence = (RadioGroup) findViewById(R.id.rg_fps_frequence);

		tvSettingRate = (TextView) findViewById(R.id.tv_setting_rate);
		tvSettingFps = (TextView) findViewById(R.id.tv_setting_fps);

		rbQualityLow = ((RadioButton) rgQuality.findViewById(R.id.rb_quality_low));
		rbQualityNormal = ((RadioButton) rgQuality.findViewById(R.id.rb_quality_normal));
		rbQualityHigh = ((RadioButton) rgQuality.findViewById(R.id.rb_quality_high));
		rbQualityBest = ((RadioButton) rgQuality.findViewById(R.id.rb_quality_best));
		rbQualityLow.setOnCheckedChangeListener(this);
		rbQualityNormal.setOnCheckedChangeListener(this);
		rbQualityHigh.setOnCheckedChangeListener(this);
		rbQualityBest.setOnCheckedChangeListener(this);

		rbRate320 = ((RadioButton) rgRateFrequence.findViewById(R.id.rb_rate_low));
		rbRate480 = ((RadioButton) rgRateFrequence.findViewById(R.id.rb_rate_normal));
		rbRate720 = ((RadioButton) rgRateFrequence.findViewById(R.id.rb_rate_high));
		rbRate320.setOnCheckedChangeListener(this);
		rbRate480.setOnCheckedChangeListener(this);
		rbRate720.setOnCheckedChangeListener(this);

		rbFps10 = ((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_10));
		rbFps15 = ((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_15));
		rbFps20 = ((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_20));
		rbFps25 = ((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_25));
		rbFps30 = ((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_30));
		rbFps10.setOnCheckedChangeListener(this);
		rbFps15.setOnCheckedChangeListener(this);
		rbFps20.setOnCheckedChangeListener(this);
		rbFps25.setOnCheckedChangeListener(this);
		rbFps30.setOnCheckedChangeListener(this);
	}

	private void initRgQuality()
	{
		String currentQuality = Setting.getVideoQuality();
		if ("极速".equals(currentQuality))
		{
			rbQualityLow.setChecked(true);
		}
		else if ("标清".equals(currentQuality))
		{
			rbQualityNormal.setChecked(true);
		}
		else if ("高清".equals(currentQuality))
		{
			rbQualityHigh.setChecked(true);
		}
		else if ("超清".equals(currentQuality))
		{
			rbQualityBest.setChecked(true);
		}
	}

	/**
	 * 初始化分辨率 radioGroup
	 */
	private void initRgRate()
	{
		String currentRate = Setting.getVideoRate();
		if (currentRate.equals(Setting.VIDEO_RESOLUTION_RATE[2]))
		{
			rbRate320.setChecked(true);
		}
		else if (currentRate.equals(Setting.VIDEO_RESOLUTION_RATE[1]))
		{
			rbRate480.setChecked(true);
		}
		else if (currentRate.equals(Setting.VIDEO_RESOLUTION_RATE[0]))
		{
			rbRate720.setChecked(true);
		}
	}

	/**
	 * 初始化FPS radioGroup
	 */
	private void initRgFps()
	{
		int currentFps = Setting.getVideoCustomFrameRate();
		switch (currentFps)
		{
			case 10:
				rbFps10.setChecked(true);
				break;
			case 15:
				rbFps15.setChecked(true);
				break;
			case 20:
				rbFps20.setChecked(true);
				break;
			case 25:
				rbFps25.setChecked(true);
				break;
			case 30:
				rbFps30.setChecked(true);
				break;
		}
	}

	@Override
	public void finish()
	{
		super.finish();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (buttonView != null)
		{
			switch (buttonView.getId())
			{
				case R.id.cb_video_setting_type:
				{
					setVideoSettingState(isChecked);
					break;
				}
				case R.id.rb_quality_low:
				{
					if (Setting.getVideoSettingType() == 0 && isChecked)
					{
						Setting.setVideoQuality("极速");
					}
					break;
				}
				case R.id.rb_quality_normal:
				{
					if (Setting.getVideoSettingType() == 0 && isChecked)
					{
						Setting.setVideoQuality("标清");
					}
					break;
				}
				case R.id.rb_quality_high:
				{
					if (Setting.getVideoSettingType() == 0 && isChecked)
					{
						Setting.setVideoQuality("高清");
					}
					break;
				}
				case R.id.rb_quality_best:
				{
					if (Setting.getVideoSettingType() == 0 && isChecked)
					{
						Setting.setVideoQuality("超清");
					}
					break;
				}
				case R.id.rb_rate_low:
				{
					if (isChecked)
					{
						Setting.setVideoCustomResolutionWidth(480);
						Setting.setVideoCustomResolutionHeight(320);
					}
					break;
				}
				case R.id.rb_rate_normal:
				{
					if (isChecked)
					{
						Setting.setVideoCustomResolutionWidth(800);
						Setting.setVideoCustomResolutionHeight(480);
					}
					break;
				}
				case R.id.rb_rate_high:
				{
					if (isChecked)
					{
						Setting.setVideoCustomResolutionWidth(1280);
						Setting.setVideoCustomResolutionHeight(720);
					}
					break;
				}
				case R.id.rb_fps_10:
				{
					if (isChecked)
					{
						Setting.setVideoCustomFrameRate(10);
					}
					break;
				}
				case R.id.rb_fps_15:
				{
					if (isChecked)
					{
						Setting.setVideoCustomFrameRate(15);
					}
					break;
				}
				case R.id.rb_fps_20:
				{
					if (isChecked)
					{
						Setting.setVideoCustomFrameRate(20);
					}
					break;
				}
				case R.id.rb_fps_25:
				{
					if (isChecked)
					{
						Setting.setVideoCustomFrameRate(25);
					}
					break;
				}
				case R.id.rb_fps_30:
				{
					if (isChecked)
					{
						Setting.setVideoCustomFrameRate(30);
					}
					break;
				}
			}
			// Toast.makeText1(this, "清晰度：" + Setting.getVideoQuality() + "分辨率：" + Setting.getVideoRate() + "帧率：" + Setting.getVideoFrameRate(), Toast.LENGTH_SHORT).show();
		}
	}

	private void setVideoSettingState(boolean isChecked)
	{
		if (isChecked)
		{
			Setting.setVideoSettingType(1);
			tvSettingRate.setTextColor(getResources().getColor(R.color.black));
			tvSettingFps.setTextColor(getResources().getColor(R.color.black));
			for (int i = 0; i < rgQuality.getChildCount(); i++)
			{
				if (rgQuality.getChildAt(i) instanceof RadioButton)
				{
					RadioButton rbCurrent = (RadioButton) rgQuality.getChildAt(i);
					rbCurrent.setEnabled(false);
					if (rgQuality.getCheckedRadioButtonId() == rbCurrent.getId())
					{
						rbCurrent.setBackgroundResource(R.drawable.playvideo_volume_seekbar_thumb_unselected_new);
					}
					else
					{
						rbCurrent.setBackgroundResource(R.drawable.selector_radio_check);
					}
				}
			}
			for (int i = 0; i < rgRateFrequence.getChildCount(); i++)
			{
				if (rgRateFrequence.getChildAt(i) instanceof RadioButton)
				{
					RadioButton rbCurrent = (RadioButton) rgRateFrequence.getChildAt(i);
					rbCurrent.setEnabled(true);
					rbCurrent.setBackgroundResource(R.drawable.selector_radio_check);
				}
			}
			for (int i = 0; i < rgFpsFrequence.getChildCount(); i++)
			{
				if (rgFpsFrequence.getChildAt(i) instanceof RadioButton)
				{
					RadioButton rbCurrent = (RadioButton) rgFpsFrequence.getChildAt(i);
					rbCurrent.setEnabled(true);
					rbCurrent.setBackgroundResource(R.drawable.selector_radio_check);
				}
			}
		}
		else
		{
			Setting.setVideoSettingType(0);
			tvSettingRate.setTextColor(getResources().getColor(R.color.black_gray));
			tvSettingFps.setTextColor(getResources().getColor(R.color.black_gray));
			for (int i = 0; i < rgQuality.getChildCount(); i++)
			{
				if (rgQuality.getChildAt(i) instanceof RadioButton)
				{
					RadioButton rbCurrent = (RadioButton) rgQuality.getChildAt(i);
					rbCurrent.setEnabled(true);
					rbCurrent.setBackgroundResource(R.drawable.selector_radio_check);
					String currentQuality = Setting.getVideoQuality();
					Setting.setVideoQuality(currentQuality);
				}
			}
			for (int i = 0; i < rgRateFrequence.getChildCount(); i++)
			{
				if (rgRateFrequence.getChildAt(i) instanceof RadioButton)
				{
					RadioButton rbCurrent = (RadioButton) rgRateFrequence.getChildAt(i);
					rbCurrent.setEnabled(false);
					if (rgRateFrequence.getCheckedRadioButtonId() == rbCurrent.getId())
					{
						rbCurrent.setBackgroundResource(R.drawable.playvideo_volume_seekbar_thumb_unselected_new);
					}
					else
					{
						rbCurrent.setBackgroundResource(R.drawable.selector_radio_check);
					}
				}
			}
			for (int i = 0; i < rgFpsFrequence.getChildCount(); i++)
			{
				if (rgFpsFrequence.getChildAt(i) instanceof RadioButton)
				{
					RadioButton rbCurrent = (RadioButton) rgFpsFrequence.getChildAt(i);
					rbCurrent.setEnabled(false);
					if (rgFpsFrequence.getCheckedRadioButtonId() == rbCurrent.getId())
					{
						rbCurrent.setBackgroundResource(R.drawable.playvideo_volume_seekbar_thumb_unselected_new);
					}
					else
					{
						rbCurrent.setBackgroundResource(R.drawable.selector_radio_check);
					}
				}
			}
		}
	}

}
