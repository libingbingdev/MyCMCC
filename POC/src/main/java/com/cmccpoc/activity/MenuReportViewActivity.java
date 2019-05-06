package com.cmccpoc.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;
import com.airtalkee.sdk.AirtalkeeReport;
import com.airtalkee.sdk.entity.AirImage;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.adapter.AdapterHorizontalListView;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirReportManager;
import com.cmccpoc.entity.AirReport;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Toast;
import com.cmccpoc.widget.HorizontialListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * 更多：上报记录预览
 * 就是查看一下某天上报记录的详细信息
 * @author Yao
 */
public class MenuReportViewActivity extends ActivityBase implements OnClickListener
{
	private MediaController mVideoController;
	private AirReport report = null;
	private ImageView iconImage;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private HorizontialListView hlImageList;
	private AdapterHorizontalListView imagesAdapter;
	DisplayImageOptions options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.msg_image).showImageOnFail(R.drawable.msg_image).resetViewBeforeLoading(true).cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true).displayer(new FadeInBitmapDisplayer(300)).build();

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_report_view);
		doInitView(bundle);
	}

	/**
	 * 初始化绑定控件Id
	 * @param bundle bundle参数，主要是为了获取reportCode
	 */
	private void doInitView(Bundle bundle)
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_report);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		bundle = getIntent().getExtras();
		if (bundle != null)
		{
			String code = bundle.getString("code");
			report = AirReportManager.getInstance().getReport(code);
			if (report != null)
			{
				Button btResend = (Button) findViewById(R.id.report_resend);
				btResend.setOnClickListener(this);
				imagesAdapter = new AdapterHorizontalListView(this, false);
				hlImageList = (HorizontialListView) findViewById(R.id.hl_image_list);
				hlImageList.setAdapter(imagesAdapter);
				iconImage = (ImageView) findViewById(R.id.report_image);
				VideoView iconVideo = (VideoView) findViewById(R.id.report_video);
				TextView content = (TextView) findViewById(R.id.report_content);
				TextView time = (TextView) findViewById(R.id.report_time);
				TextView tvFail = (TextView)findViewById(R.id.talk_report_fail_message);
				if (report.getType() == AirtalkeeReport.RESOURCE_TYPE_VIDEO)
				{
					mVideoController = new MediaController(this);
					iconImage.setVisibility(View.GONE);
					iconVideo.setVisibility(View.VISIBLE);
					iconVideo.setVideoPath(report.getResPath());
					iconVideo.setMediaController(mVideoController);
					try
					{
						iconVideo.start();
					}
					catch (Exception e)
					{
					}
				}
				else
				{
					iconVideo.setVisibility(View.GONE);

					if (report.isResFileMulti() && report.getResFileList().size() > 1)
					{
						iconImage.setVisibility(View.GONE);
						hlImageList.setVisibility(View.VISIBLE);
						imagesAdapter.notifyList(report.getResFileList());
					}
					else
					{
						iconImage.setVisibility(View.VISIBLE);
						hlImageList.setVisibility(View.GONE);
						imageLoader.displayImage(report.getResUri().toString(), iconImage);
					}
				}
				if (Utils.isEmpty(report.getResContent()))
				{
					content.setText(getString(R.string.talk_tools_report_description) + "：" + getString(R.string.talk_report_upload_no_content));
				}
				else
				{
					String txt = getString(R.string.talk_tools_report_description) + "：" + (report.getResContent().contains("\r") ? report.getResContent().substring(0, report.getResContent().lastIndexOf('\r')) : report.getResContent());
					content.setText(txt);
				}
				int state = report.getState();
				if (state == AirReport.STATE_RESULT_FAIL)
				{
					tvFail.setVisibility(View.VISIBLE);
					btResend.setVisibility(View.VISIBLE);
				}
				else
				{
					tvFail.setVisibility(View.GONE);
					btResend.setVisibility(View.GONE);
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String datetime = "";
				try
				{
					datetime = sdf.format(sdf.parse(report.getTime()));
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}
				time.setText(getString(R.string.talk_tools_report_date) + "：" + datetime);
			}
			else
			{
				finish();
			}
		}
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
			case R.id.report_resend:
				List<AirReport> reports = AirReportManager.getInstance().getReports();
				if (reports != null && reports.size() > 0)
				{
					for (int i = 0; i < reports.size(); i++)
					{
						if (reports.get(i).getState() == AirReport.STATE_UPLOADING)
						{
							if(Toast.isDebug) Toast.makeText1(this, "当前有文件正在上报中，请完成后再继续", Toast.LENGTH_LONG).show();
							return;
						}
					}
				}
				if (report != null)
				{
					AirReportManager.getInstance().ReportResend(report);
					MenuReportActivity.getInstance().adapterReport.setSelected(report, false);
					finish();
				}
				break;
		}
	}

}
