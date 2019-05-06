package com.cmccpoc.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;

/**
 * IM消息中，在选择录音消息，长按录音时，会在界面上弹出一个区域，将触摸移动至该区域会取消当前的录音。
 * 本类就是弹出的取消录音界面的View
 * @author Yao
 */
public class MacRecordingView extends RelativeLayout
{
	public static final int START_RECORD = 0;
	public static final int START_TIME = 1;
	public static final int STOP_TIME = 2;
	public static final int RECORD_CANCEL = 3;
	public static final int RECORD_OK = 4;
	// private static double view_high = 0;
	private String time_length = "01:00";
	public static int temp = 900;
	private Chronometer chronometer;
	private ImageView mac_image, screen_image, record_cancel;
	private TextView text_view;
	private ProgressBar pro2;
	private View porLay = null;
	private Context mInstance;

	public MacRecordingView(Context context)
	{
		// TODO Auto-generated constructor stub
		super(context);
		mInstance = context;
	}

	public MacRecordingView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mInstance = context;
	}

	/**
	 * 定义一个Handler,根据不同的状态执行各中操作
	 */
	public Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what)
			{
				case START_RECORD:
					if (msg.obj != null)
					{
						reflashView((Integer) msg.obj);
					}
					break;
				case START_TIME:
					startTime();
					break;
				case STOP_TIME:
					if (msg.obj != null)
						stopTime((Boolean) msg.obj);
					break;
				case RECORD_CANCEL:
					recordCancel();
					break;
				case RECORD_OK:
					recording();
					break;
			}
		}
	};

	/**
	 * 计算大小
	 * @param bufSend 发送的二进制数据
	 */
	public void countSize(byte[] bufSend)
	{
		int v = 0;
		for (int i = 0; i < bufSend.length; i++)
		{
			v += Math.abs(Math.pow(bufSend[i], 3));
		}
		int value = (int) (Math.abs((int) (v / 100000)));
		registerMessage(START_RECORD, value);
	}

	/**
	 * 发送系统Message
	 * @param eventId 事件Id
	 * @param obj 传递对象
	 */
	public void registerMessage(int eventId, Object obj)
	{
		Log.e(MacRecordingView.class, "registerMessage eventId =" + eventId);
		Message message = handler.obtainMessage();
		message.arg1 = eventId;
		message.what = eventId;
		message.obj = obj;
		handler.sendMessage(message);
	}

	public void setText(String text)
	{
		if (text_view != null)
			text_view.setText(text);
	}

	/**
	 * 计时器开始
	 */
	private void startTime()
	{
		if (getVisibility() != View.VISIBLE)
			setVisibility(View.VISIBLE);
		recording();
		if (chronometer != null)
		{
			chronometer.setBase(SystemClock.elapsedRealtime());
			chronometer.start();
		}
		if (chronometer != null && chronometer.getOnChronometerTickListener() == null)
		{
			chronometer.setOnChronometerTickListener(new OnChronometerTickListener()
			{
				@Override
				public void onChronometerTick(Chronometer chronter)
				{
					// TODO Auto-generated method stub
					if (time_length.equals(chronter.getText()))
					{
						registerMessage(STOP_TIME, false);
					}
				}
			});
		}
	}

	/**
	 * 停止计时器
	 * @param recorderCancel 录音是否取消
	 */
	private void stopTime(boolean recorderCancel)
	{
		setVisibility(View.INVISIBLE);
		if (chronometer != null)
		{
			chronometer.stop();
			chronometer.setBase(SystemClock.elapsedRealtime());
		}
	}

	/**
	 * 初始化控件View
	 */
	public void initChild()
	{
		chronometer = (Chronometer) findViewById(R.id.chronometer);
		mac_image = (ImageView) findViewById(R.id.mac);
		screen_image = (ImageView) findViewById(R.id.image);
		record_cancel = (ImageView) findViewById(R.id.cancel);
		text_view = (TextView) findViewById(R.id.text);
		pro2 = (ProgressBar) findViewById(R.id.progress_small);
		porLay = findViewById(R.id.pro_lay);
	}

	/**
	 * 取消录音消息
	 */
	private void recordCancel()
	{
		if (mac_image.getVisibility() != View.INVISIBLE)
			mac_image.setVisibility(View.INVISIBLE);
		if (screen_image.getVisibility() != View.INVISIBLE)
			screen_image.setVisibility(View.INVISIBLE);
		if (record_cancel.getVisibility() != View.VISIBLE)
			record_cancel.setVisibility(View.VISIBLE);
		if (porLay.getVisibility() != INVISIBLE)
			porLay.setVisibility(INVISIBLE);
		if (!text_view.getText().equals(mInstance.getString(R.string.talk_rec_release)))
			text_view.setText(mInstance.getString(R.string.talk_rec_release));
	}

	/**
	 * 正在录音
	 */
	private void recording()
	{
		if (mac_image.getVisibility() != View.VISIBLE)
			mac_image.setVisibility(View.VISIBLE);
		if (screen_image.getVisibility() != View.VISIBLE)
			screen_image.setVisibility(View.VISIBLE);
		if (record_cancel.getVisibility() != View.INVISIBLE)
			record_cancel.setVisibility(View.INVISIBLE);
		if (porLay.getVisibility() != VISIBLE)
			porLay.setVisibility(VISIBLE);

		if (!text_view.getText().equals(mInstance.getString(R.string.talk_rec_cancel)))
			text_view.setText(mInstance.getString(R.string.talk_rec_cancel));
	}

	/**
	 * 刷新View
	 * @param high 高度
	 */
	private void reflashView(int high)
	{
		if (high > temp)
		{
			pro2.setVisibility(View.GONE);
		}
		else
		{
			pro2.setVisibility(View.VISIBLE);
		}
	}
}
