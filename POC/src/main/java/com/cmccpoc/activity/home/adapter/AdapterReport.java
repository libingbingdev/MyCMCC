package com.cmccpoc.activity.home.adapter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;
import com.airtalkee.sdk.AirtalkeeReport;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.control.AirReportManager;
import com.cmccpoc.entity.AirReport;
import com.cmccpoc.util.Toast;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * 上报记录适配器
 * 
 * @author Yao
 */
public class AdapterReport extends BaseAdapter implements OnClickListener
{
	private Context context = null;
	private List<AirReport> reports = null;
	private Map<AirReport, Boolean> reportsState = new ConcurrentHashMap<AirReport, Boolean>();// ConcurrentHashMap
	private Map<String, Bitmap> reportVideoPic = new HashMap<String, Bitmap>();
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private boolean isEditing = false;
	DisplayImageOptions options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.report_default_vid).showImageOnFail(R.drawable.report_default_vid).resetViewBeforeLoading(true).cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true).displayer(new FadeInBitmapDisplayer(0)).build();
	ListView lv;
	
	public AdapterReport(Context _context, ListView lv)
	{
		this.lv = lv;
		context = _context;
		reports = AirReportManager.getInstance().getReports();
		if (reports != null && reports.size() > 0)
		{
			for (AirReport report : reports)
			{
				reportsState.put(report, false);
			}
		}
	}

	public boolean isEditing()
	{
		return isEditing;
	}

	public void setEditing(boolean isEditing)
	{
		this.isEditing = isEditing;
	}

	public Map<AirReport, Boolean> getReportMap()
	{
		return reportsState;
	}

	public int getSelectReportCount()
	{
		int count = 0;
		for (Map.Entry<AirReport, Boolean> entry : reportsState.entrySet())
		{
			if (entry.getValue())
			{
				count++;
			}
		}
		return count;
	}

	public void setSelected(AirReport report, boolean isChecked)
	{
		reportsState.put(report, isChecked);
	}
	
	public void removedSelected()
	{
		//List<AirReport> reportsList = null;
		if (null != reportsState && reportsState.size() > 0)
		{
			// reportsList = new ArrayList<AirReport>();
			Iterator<Entry<AirReport, Boolean>> iterator = reportsState.entrySet().iterator();
			while (iterator.hasNext())
			{
				Map.Entry<AirReport, Boolean> entry = (Map.Entry<AirReport, Boolean>) iterator.next();
				if (entry.getValue())
				{
					reportsState.remove(entry.getKey());
					//reportsList.add((AirReport)entry.getKey());
				}
			}
		}
//		if (reportsList != null && reportsList.size() > 0)
//		{
//			for (int i = 0; i < reportsList.size(); i++)
//			{
//				reportsState.remove(reportsList.get(i));
//			}
//		}
	}

	public void checkedAll(boolean isChecked)
	{
		if (null != reports && reports.size() > 0)
		{
			for (Map.Entry<AirReport, Boolean> entry : reportsState.entrySet())
			{
				if (entry.getKey().getState() != AirReport.STATE_UPLOADING)
					setSelected(entry.getKey(), isChecked);
			}
		}
	}

	public void clearVideoPic()
	{
		reportVideoPic.clear();
	}

	public void deleteVideoPic()
	{
		Iterator<Entry<AirReport, Boolean>> iterator = reportsState.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<AirReport, Boolean> entry = (Map.Entry<AirReport, Boolean>) iterator.next();
			if (entry.getValue())
			{
				reportVideoPic.remove(entry.getKey());
			}
		}

	}

	@Override
	public int getCount()
	{
		return reports.size();
	}

	@Override
	public Object getItem(int position)
	{
		AirReport report = null;
		if (reports.size() > 0)
		{
			report = reports.get(reports.size() - position - 1);
			// report = reports.get(position);
		}
		return report;
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = null;
		final AirReport report = (AirReport) getItem(position);
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_report, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.talk_report_icon);
			holder.video = (VideoView) convertView.findViewById(R.id.talk_report_video);
			holder.play = (ImageView) convertView.findViewById(R.id.talk_report_play);
			holder.task = (TextView) convertView.findViewById(R.id.talk_report_task);
			holder.time = (TextView) convertView.findViewById(R.id.talk_report_time);
			holder.detail = (TextView) convertView.findViewById(R.id.talk_report_detail);
			holder.progressBar = (ProgressBar) convertView.findViewById(R.id.talk_report_progress);
			// 重发按钮图片
			holder.stateRetry = (ImageView) convertView.findViewById(R.id.talk_report_retry);

			// 上传失败
			holder.failText = (TextView) convertView.findViewById(R.id.talk_report_fail_message);
			// 点击重发 等待重发
			holder.uploadStep = (TextView) convertView.findViewById(R.id.talk_report_retry_step);
			holder.cbReport = (CheckBox) convertView.findViewById(R.id.cb_report);
			// holder.cbReport.setOnCheckedChangeListener(new
			// OnCheckedChangeListener()
			// {
			// @Override
			// public void onCheckedChanged(CompoundButton buttonView, boolean
			// isChecked)
			// {
			// setSelected(report, !isChecked);
			// // notifyDataSetChanged();
			// }
			// });
			holder.cbReport.setEnabled(false);
			holder.ivReportEnter = (ImageView) convertView.findViewById(R.id.iv_report_enter);
			holder.retryLayout = (LinearLayout) convertView.findViewById(R.id.talk_report_retry_panel);
			holder.retryLayout.getBackground().setAlpha(200);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		try
		{
			fillView(report, holder);
		}
		catch (Exception e)
		{}
		return convertView;
	}

	/**
	 * 填充每个上报记录的View
	 * 
	 * @param report
	 *            上报记录Entity
	 * @param holder
	 *            结构
	 */
	private void fillView(final AirReport report, ViewHolder holder)
	{
		if (report != null)
		{
			holder.video.setVisibility(View.GONE);
			if (report.getType() == AirtalkeeReport.RESOURCE_TYPE_VIDEO)
			{
				holder.icon.setVisibility(View.VISIBLE);
				holder.video.setVideoPath(report.getResPath());
				Bitmap bitmap = reportVideoPic.get(report.getCode());
				if (bitmap == null)
				{
					try
					{
						MediaMetadataRetriever rev = new MediaMetadataRetriever();
						rev.setDataSource(context, Uri.fromFile(new File(report.getResPath())));
						bitmap = rev.getFrameAtTime(1 * 1000 * 2000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
						String path = report.getResPath().substring(0, report.getResPath().lastIndexOf(".") + 1) + "jpg";
						FileOutputStream fos = null;
						fos = new FileOutputStream(path);
						bitmap.compress(CompressFormat.JPEG, 50, fos);
						fos.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					reportVideoPic.put(report.getCode(), bitmap);
				}
				holder.icon.setImageBitmap(bitmap);
			}
			else
			{
				imageLoader.displayImage(report.getResUri().toString(), holder.icon);
				holder.icon.setVisibility(View.VISIBLE);
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
			holder.time.setText(context.getString(R.string.talk_tools_report_date) + "：" + datetime);
			// holder.size.setText(MenuReportActivity.sizeMKB(report.getResSize()));

			if (report.getTarget() == AirReport.TARGET_TASK_DISPATCH)
			{
				holder.task.setText(report.getTaskName());
				holder.task.setVisibility(View.VISIBLE);
			}
			else
			{
				holder.task.setVisibility(View.GONE);
			}
			if (!Utils.isEmpty(report.getResContent()))
			{
				String content = report.getResContent().contains("\r") ? report.getResContent().substring(0, report.getResContent().lastIndexOf('\r')) : report.getResContent();
				holder.detail.setText(context.getString(R.string.talk_tools_report_description) + "：" + content);
			}
			else
			{
				holder.detail.setText(context.getString(R.string.talk_tools_report_description) + "：" + context.getString(R.string.talk_report_upload_no_content) + System.getProperty("line.separator", "/n"));
			}
			Log.i(AdapterReport.class, "report state=" + report.getState());
			switch (report.getState())
			{
				case AirReport.STATE_WAITING:
				{
					holder.stateRetry.setVisibility(View.GONE);
					holder.detail.setVisibility(View.VISIBLE);
					holder.progressBar.setVisibility(View.VISIBLE);
					holder.uploadStep.setText(context.getString(R.string.talk_tools_report_waiting));
					holder.uploadStep.setVisibility(View.VISIBLE);
					holder.failText.setVisibility(View.GONE);
					holder.retryLayout.setVisibility(View.VISIBLE);
					break;
				}
				case AirReport.STATE_UPLOADING:
				{
					holder.uploadStep.setText(context.getString(R.string.talk_tools_report_uploading));
					holder.progressBar.setVisibility(View.VISIBLE);
					holder.uploadStep.setVisibility(View.VISIBLE);
					holder.failText.setVisibility(View.GONE);
					holder.stateRetry.setVisibility(View.GONE);
					holder.retryLayout.setVisibility(View.VISIBLE);
					holder.cbReport.setVisibility(View.GONE);
					break;
				}
				case AirReport.STATE_RESULT_OK:
				{
					holder.detail.setVisibility(View.VISIBLE);
					// holder.progressLayout.setVisibility(View.GONE);
					if (report.getType() == AirtalkeeReport.RESOURCE_TYPE_VIDEO)
					{
						holder.play.setVisibility(View.VISIBLE);
						holder.play.setImageResource(R.drawable.btn_report_play);
					}
					else
					{
						holder.play.setVisibility(View.GONE);
					}
					holder.progressBar.setVisibility(View.GONE);
					holder.stateRetry.setVisibility(View.GONE);
					holder.uploadStep.setVisibility(View.GONE);
					holder.failText.setVisibility(View.GONE);
					holder.retryLayout.setVisibility(View.GONE);
					break;
				}
				case AirReport.STATE_RESULT_FAIL:
				{
					holder.stateRetry.setImageResource(R.drawable.selector_report_retry);
					holder.stateRetry.setVisibility(View.VISIBLE);
					holder.stateRetry.setOnClickListener(this);
					holder.stateRetry.setTag(report.getCode());
					holder.failText.setVisibility(View.VISIBLE);
					holder.uploadStep.setText(context.getString(R.string.talk_tools_report_click));
					holder.uploadStep.setVisibility(View.VISIBLE);
					holder.progressBar.setVisibility(View.GONE);
					holder.retryLayout.setVisibility(View.VISIBLE);
					break;
				}
			}
			if (isEditing)
			{
				holder.cbReport.setVisibility(View.VISIBLE);
				holder.ivReportEnter.setVisibility(View.GONE);
				try
				{
					holder.cbReport.setChecked(reportsState.get(report));
				}
				catch (Exception e)
				{
					holder.cbReport.setChecked(false);
				}
			}
			else
			{
				holder.cbReport.setVisibility(View.GONE);
				holder.ivReportEnter.setVisibility(View.VISIBLE);
			}
		}
	}

	class ViewHolder
	{
		ImageView icon;
		ImageView play;
		TextView task;
		TextView time;
		TextView size;
		TextView detail;
		ProgressBar progressBar;
		ImageView state;
		ImageView stateRetry;
		TextView failText;
		TextView uploadStep;
		CheckBox cbReport;
		ImageView ivReportEnter;
		VideoView video;
		LinearLayout retryLayout;
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.talk_report_retry:
			{
				List<AirReport> reports = AirReportManager.getInstance().getReports();
				if (reports != null && reports.size() > 0)
				{
					for (int i = 0; i < reports.size(); i++)
					{
						if (reports.get(i).getState() == AirReport.STATE_UPLOADING)
						{
							if(Toast.isDebug) Toast.makeText1(context, "当前有文件正在上报中，请完成后再继续", Toast.LENGTH_LONG).show();
							return;
						}
					}
				}
				AirReportManager.getInstance().setReportDoing(null);
				AirReportManager.getInstance().ReportRetry((String) v.getTag());
				notifyDataSetChanged();
				break;
			}

			default:
				break;
		}
	}
}
