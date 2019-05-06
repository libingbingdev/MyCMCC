package com.cmccpoc.activity;

import android.os.Bundle;
import android.view.KeyEvent;
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
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.config.Config;
import com.cmccpoc.util.Setting;
import com.cmccpoc.util.ThemeUtil;

/**
 * 更多：对讲设置
 * 一堆对讲设置选项，具体内容详见layout即可
 * @author Yao
 */
public class MenuSettingPttActivity extends ActivityBase implements /*OnClickListener,*/ OnCheckedChangeListener
{
	private CheckBox mVoiceAmplifier, mPttClick, mPttVolume, mPttAnswer,mPttIsb;
	View pttAnswerLayout;
	private TextView mFrequenceText;
	private int[] mFrequenceValue = {/* Config.ENGINE_MEDIA_HB_SECOND_HIGH,*/ Config.ENGINE_MEDIA_HB_SECOND_FAST, Config.ENGINE_MEDIA_HB_SECOND_MEDIUM, Config.ENGINE_MEDIA_HB_SECOND_SLOW };
	private int mFrequenceSelected = 0;

	private RadioGroup rgHeartBeat;
	private RadioButton rbSlow, rbNormal, rbFast, rbHigh;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_setting_ptt);
		doInitView();
	}

	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
	}

	/**
	 * 初始化绑定控件Id
	 */
	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.heart_settings);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		//btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		/*pttAnswerLayout = findViewById(R.id.talk_setting_answer);
		pttAnswerLayout.setOnClickListener(this);
		mPttAnswer = (CheckBox) findViewById(R.id.talk_setting_answer_check);
		mPttAnswer.setChecked(Setting.getPttAnswerMode());
		mPttAnswer.setOnCheckedChangeListener(this);

		findViewById(R.id.talk_setting_isb).setOnClickListener(this);
		mPttIsb = (CheckBox) findViewById(R.id.talk_setting_isb_check);
		mPttIsb.setChecked(Setting.getPttIsb());
		mPttIsb.setOnCheckedChangeListener(this);

		//if (Config.pttButtonVisibility == View.VISIBLE && Config.funcPTTButton)
		if (Config.funcPTTButton)
		{
			findViewById(R.id.talk_setting_ptt_click).setOnClickListener(this);
			mPttClick = (CheckBox) findViewById(R.id.talk_setting_ptt_click_check);
			mPttClick.setChecked(Setting.getPttClickSupport());
			mPttClick.setOnCheckedChangeListener(this);
		}
		else
		{
			findViewById(R.id.talk_setting_ptt_click).setVisibility(View.GONE);
			findViewById(R.id.talk_setting_ptt_click_line).setVisibility(View.GONE);
		}

		if (Config.pttButtonKeycode == KeyEvent.KEYCODE_UNKNOWN && Utils.isEmpty(Config.pttButtonAction))
		{
			findViewById(R.id.talk_setting_ptt_volume).setOnClickListener(this);
			mPttVolume = (CheckBox) findViewById(R.id.talk_setting_ptt_volume_check);
			mPttVolume.setChecked(Setting.getPttVolumeSupport());
			mPttVolume.setOnCheckedChangeListener(this);
		}
		else
		{
			findViewById(R.id.talk_setting_ptt_volume).setVisibility(View.GONE);
			findViewById(R.id.talk_setting_ptt_volume_line).setVisibility(View.GONE);
		}*/

		rgHeartBeat = (RadioGroup) findViewById(R.id.rg_hb_frequence);
		rbSlow = (RadioButton) findViewById(R.id.rb_slow);
		rbNormal = (RadioButton) findViewById(R.id.rb_normal);
		rbFast = (RadioButton) findViewById(R.id.rb_fast);
		//rbHigh = (RadioButton) findViewById(R.id.rb_high);
		if (Config.engineMediaSettingHbPackSize == Config.ENGINE_MEDIA_HB_SIZE_NONE)
		{
			mFrequenceText = (TextView) findViewById(R.id.talk_setting_hb_text);
			int hb = Setting.getPttHeartbeat();
			for (int i = 0; i < mFrequenceValue.length; i++)
			{
				if (hb == mFrequenceValue[i])
				{
					mFrequenceSelected = i;
					break;
				}
			}
			switch (mFrequenceSelected)
			{
				/*case 0:
					rbHigh.setChecked(true);
					break;*/
				case 0:
					rbFast.setChecked(true);
					break;
				case 1:
					rbNormal.setChecked(true);
					break;
				case 2:
					rbSlow.setChecked(true);
					break;

			}
			rgHeartBeat.setOnCheckedChangeListener(listener);
		}
		else
		{
			findViewById(R.id.talk_setting_hb).setVisibility(View.GONE);
			findViewById(R.id.talk_setting_hb_line).setVisibility(View.GONE);
		}

		//refreshPttAnswerItem();
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
	}

	/**
	 * radioGroup checked change listner
	 */
	private RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId)
		{
			int rid = group.getCheckedRadioButtonId();
			switch (rid)
			{
				case R.id.rb_slow:
				{
					if (rbSlow.isChecked())
					{
						Setting.setPttHeartbeat(mFrequenceValue[2]);
						AirtalkeeSessionManager.getInstance().setMediaEngineSetting(Config.engineMediaSettingHbSeconds, Config.engineMediaSettingHbPackSize);
					}
					break;
				}
				case R.id.rb_normal:
				{
					if (rbNormal.isChecked())
					{
						Setting.setPttHeartbeat(mFrequenceValue[1]);
						AirtalkeeSessionManager.getInstance().setMediaEngineSetting(Config.engineMediaSettingHbSeconds, Config.engineMediaSettingHbPackSize);
					}
					break;
				}
				case R.id.rb_fast:
				{
					if (rbFast.isChecked())
					{
						Setting.setPttHeartbeat(mFrequenceValue[0]);
						AirtalkeeSessionManager.getInstance().setMediaEngineSetting(Config.engineMediaSettingHbSeconds, Config.engineMediaSettingHbPackSize);
					}
					break;
				}
				/*case R.id.rb_high:
				{
					if (rbHigh.isChecked())
					{
						Setting.setPttHeartbeat(mFrequenceValue[0]);
						AirtalkeeSessionManager.getInstance().setMediaEngineSetting(Config.engineMediaSettingHbSeconds, Config.engineMediaSettingHbPackSize);
					}
					break;
				}*/
				default:
					break;
			}

		}
	};

/*	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
			case R.id.talk_setting_voice_amplifier:
				mVoiceAmplifier.setChecked(!mVoiceAmplifier.isChecked());
				break;
			case R.id.talk_setting_answer:
				mPttAnswer.setChecked(!mPttAnswer.isChecked());
				break;
			case R.id.talk_setting_isb:
				mPttIsb.setChecked(!mPttIsb.isChecked());
				break;
			case R.id.talk_setting_ptt_click:
				mPttClick.setChecked(!mPttClick.isChecked());
				break;
			case R.id.talk_setting_ptt_volume:
				mPttVolume.setChecked(!mPttVolume.isChecked());
				break;
		}
	}*/

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		// TODO Auto-generated method stub
		if (buttonView != null)
		{
			/*switch (buttonView.getId())
			{
				case R.id.talk_setting_voice_amplifier_check:
					Setting.setVoiceAmplifier(mVoiceAmplifier.isChecked());
					AirtalkeeSessionManager.getInstance().setAudioAmplifier(mVoiceAmplifier.isChecked());
					break;

				case R.id.talk_setting_answer_check:
					Setting.setPttAnswerMode(mPttAnswer.isChecked());
					if (mPttAnswer.isChecked())
						AirtalkeeSessionManager.getInstance().setSessionDialogSetAnswerMode(AirSession.INCOMING_MODE_AUTO);
					else
						AirtalkeeSessionManager.getInstance().setSessionDialogSetAnswerMode(AirSession.INCOMING_MODE_MANUALLY);
					break;

				case R.id.talk_setting_isb_check:
					Setting.setPttIsb(mPttIsb.isChecked());
					if (mPttIsb.isChecked())
						AirtalkeeSessionManager.getInstance().setSessionDialogSetIsbMode(true);
					else
						AirtalkeeSessionManager.getInstance().setSessionDialogSetIsbMode(false);

					refreshPttAnswerItem();
					break;

				case R.id.talk_setting_ptt_click_check:
					Setting.setPttClickSupport(mPttClick.isChecked());
					break;

				case R.id.talk_setting_ptt_volume_check:
					Setting.setPttVolumeSupport(mPttVolume.isChecked());
					break;

				default:
					break;
			}*/
		}
	}

	/**
	 * 刷新PTT应答模式选项
	 */
	/*private void refreshPttAnswerItem()
	{
		if (mPttIsb != null)
		{
			if (mPttIsb.isChecked())
			{
				pttAnswerLayout.setClickable(false);
				pttAnswerLayout.setEnabled(false);
				mPttAnswer.setChecked(false);
				mPttAnswer.setClickable(false);
				mPttAnswer.setEnabled(false);
			}
			else
			{
				pttAnswerLayout.setClickable(true);
				pttAnswerLayout.setEnabled(true);
				mPttAnswer.setClickable(true);
				mPttAnswer.setEnabled(true);
			}
		}
	}*/

}
