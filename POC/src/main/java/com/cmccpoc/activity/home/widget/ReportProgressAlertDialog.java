package com.cmccpoc.activity.home.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;

/**
 * 视频、图片上报时，弹出的进度条窗口
 * @author Yao
 */
public class ReportProgressAlertDialog extends Dialog implements OnClickListener
{
	private ProgressBar reportBar;
	private TextView tvFileSize, tvFileProgress;
	private Button cancel;
	private Context mContext;

	private String fileSize;

	public ReportProgressAlertDialog(Context context, String fileSize)
	{
		super(context, R.style.alert_dialog);
		this.fileSize = fileSize;
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_alert_report);
		reportBar = (ProgressBar) findViewById(R.id.report_progress);
		reportBar.setMax(100);
		tvFileSize = (TextView) findViewById(R.id.tv_file_size);
		tvFileSize.setText(fileSize);

		tvFileProgress = (TextView) findViewById(R.id.tv_file_progress);
		cancel = (Button) findViewById(R.id.report_back);
		cancel.setOnClickListener(this);
	}

	/**
	 * 设置进度值
	 * @param progress 进度值
	 */
	public void setFileProgress(int progress)
	{
		Message msg = mHandler.obtainMessage();
		msg.what = 1;
		msg.arg1 = progress;
		mHandler.sendMessage(msg);
	}

	Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				tvFileProgress.setText("进度 " + msg.arg1 + "%");
				Log.i(ReportProgressAlertDialog.class, "Progress = " + msg.arg1);
				reportBar.setProgress(msg.arg1);
			}
		}
	};

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.report_back:
			{
				this.cancel();
				try
				{
					if (mContext != null)
						((Activity) mContext).finish();
				}
				catch (Exception e)
				{ }
				break;
			}
			default:
				break;
		}
	}

}
