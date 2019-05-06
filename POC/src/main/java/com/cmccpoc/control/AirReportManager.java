package com.cmccpoc.control;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeReport;
import com.airtalkee.sdk.OnReportListener;
import com.airtalkee.sdk.OnReportMultiListener;
import com.airtalkee.sdk.entity.AirImage;
import com.airtalkee.sdk.util.IOoperate;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.activity.MenuReportActivity;
import com.cmccpoc.activity.MenuReportAsPicActivity;
import com.cmccpoc.activity.MenuReportAsVidActivity;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.config.Config;
import com.cmccpoc.dao.DBProxyReport;
import com.cmccpoc.entity.AirReport;
import com.cmccpoc.listener.OnMmiReportListener;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.Util;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 上报记录管理类
 * @author Yao
 */
public class AirReportManager implements OnReportListener, OnReportMultiListener
{
	public static final int REPORT_IMAGE_MAX_CNT = 9;

	private static AirReportManager mInstance;
	private static DBProxyReport mDbProxy = null;
	private OnMmiReportListener reportListener = null;
	private IOoperate iOperate = new IOoperate();
	private List<AirReport> reports = new ArrayList<AirReport>();
	private AirReport reportDoing = null;
	private boolean isReportLoaded = false;

	public List<AirImage> mImages = new ArrayList<AirImage>();
	public List<AirImage> mImagesOrg = new ArrayList<AirImage>();


	public static AirReportManager getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new AirReportManager();
			mDbProxy = (DBProxyReport) AirServices.getInstance().dbProxy();
			AirtalkeeReport.getInstance().setReportListener(mInstance);
			AirtalkeeReport.getInstance().setReportMultiListener(mInstance);
		}
		return mInstance;
	}
	
	/**
	 * 设置正在上传的上报记录
	 * @param report 上报记录Entity
	 */
	public void setReportDoing(AirReport report)
	{
		reportDoing = report;
	}

	public void setReportListener(OnMmiReportListener listener)
	{
		this.reportListener = listener;
	}

	/**
	 * 加载上报记录
	 */
	public void loadReports()
	{
		if (!isReportLoaded)
		{
			mDbProxy.DbReportLoad(reports);
			isReportLoaded = true;
		}
	}

	public List<AirReport> getReports()
	{
		return reports;
	}

	/**
	 * 获取一条上报记录
	 * @param code 上报记录code
	 * @return 上报记录
	 */
	public AirReport getReport(String code)
	{
		AirReport report = null;
		for (int i = 0; i < reports.size(); i++)
		{
			if (TextUtils.equals(reports.get(i).getCode(), code))
			{
				report = reports.get(i);
				break;
			}
		}
		return report;
	}

	/**
	 * 执行资源上报
	 * @param resType 资源类型
	 * @param resTypeExtension 扩展资源类型
	 * @param resUri 资源Uri地址
	 * @param resPath 资源路径
	 * @param resContent 资源描述内容
	 * @param resSize 资源大小
	 * @param locationX 纬度
	 * @param locationY 精度
	 */
	public void Report(int resType, String resTypeExtension, Uri resUri, String resPath, String resContent, int resSize, int locationType, double locationX, double locationY)
	{
		Report(null, null, resType, resTypeExtension, resUri, resPath, resContent, resSize, locationType, locationX, locationY);
	}

	/**
	 * 执行资源上报
	 * @param taskId 任务Id
	 * @param taskName 任务名称
	 * @param resType 资源类型
	 * @param resTypeExtension 扩展资源类型
	 * @param resUri 资源Uri地址
	 * @param resPath 资源路径
	 * @param resContent 资源描述内容
	 * @param resSize 资源大小
	 * @param locationX 纬度
	 * @param locationY 精度
	 */
	public void Report(String taskId, String taskName, int resType, String resTypeExtension, Uri resUri, String resPath, String resContent, int resSize, int locationType, double locationX, double locationY)
	{
		boolean toReport = true;
		if (Utils.isEmpty(resPath))
		{
			toReport = false;
		}
		for (int i = 0; i < reports.size() && toReport; i++)
		{
			if (resPath.equals(reports.get(i).getResPath()))
			{
				toReport = false;
			}
		}

		if (toReport)
		{
			String fileName = "";
			if (!TextUtils.isEmpty(resPath))
			{
				int pos = TextUtils.lastIndexOf(resPath, '/');
				if (pos + 1 < resPath.length())
					fileName = resPath.substring(pos + 1);
			}
			AirReport report = new AirReport();
			report.setCode(Utils.getCurrentTimeInMillis() + "");
			report.setResUri(resUri);
			report.setResPath(resPath);
			report.setResFileName(fileName);
			report.setResContent(resContent);
			report.setResSize(resSize);
			report.setLocType(locationType);
			report.setLocLatitude(locationX);
			report.setLocLongitude(locationY);
			report.setType(resType);
			report.setTypeExtension(resTypeExtension);
			report.setTime(Util.getCurrentTime());
			report.setTaskId(taskId);
			report.setTaskName(taskName);
			if (!TextUtils.isEmpty(taskId))
				report.setTarget(AirReport.TARGET_TASK_DISPATCH);

			reportActionDoNew(report);
		}
	}

	public void ReportMulti(String taskId, List<AirImage> images, String resContent, int locationType, double locationX, double locationY)
	{
		if (images != null && images.size() > 0)
		{
			int size = 0;
			String fileName = images.get(0).getFileFullName();
			if (!TextUtils.isEmpty(images.get(0).getFileFullName()))
			{
				int pos = TextUtils.lastIndexOf(images.get(0).getFileFullName(), '/');
				if (pos + 1 < images.get(0).getFileFullName().length())
					fileName = images.get(0).getFileFullName().substring(pos + 1);
			}

			List<AirImage> resFileList = new ArrayList<AirImage>();
			for (int i = 0; i < images.size(); i ++)
			{
				resFileList.add(images.get(i));
				size += images.get(i).getSize();
			}
			AirReport report = new AirReport();
			report.setCode("M" + Utils.getCurrentTimeInMillis() + "");
			report.setResUri(images.get(0).getFileUri());
			report.setResPath(images.get(0).getFileFullName());
			report.setResFileName(fileName);
			report.setResContent(resContent);
			report.setResSize(size);
			report.setLocType(locationType);
			report.setLocLatitude(locationX);
			report.setLocLongitude(locationY);
			report.setType(images.get(0).getType());
			report.setTime(Util.getCurrentTime());
			report.setResFileMulti(true);
			report.setResFileList(resFileList);
			report.setTaskId(taskId);
//			report.setTaskName(taskName);

			reportActionDoNew(report);
		}
	}

	/**
	 * 重新上报（会产生新的上报记录）
	 * @param report 上报记录Entity
	 */
	public void ReportResend(AirReport report)
	{
		AirReport r = new AirReport();
		r.setCode(Utils.getCurrentTimeInMillis() + "");
		r.setResUri(report.getResUri());
		r.setResPath(report.getResPath());
		r.setResContent(report.getResContent());
		r.setResSize(report.getResSize());
		r.setLocLatitude(report.getLocLatitude());
		r.setLocLongitude(report.getLocLongitude());
		r.setType(report.getType());
		r.setTypeExtension(report.getTypeExtension());
		r.setTime(Util.getCurrentTime());
		r.setTaskId(report.getTaskId());
		r.setTaskName(report.getTaskName());
		if (!TextUtils.isEmpty(report.getTaskId()))
			r.setTarget(AirReport.TARGET_TASK_DISPATCH);

		reportActionDoNew(r);
	}

	/**
	 * 重试上传（不会产生新的上报记录）
	 * @param code
	 */
	public void ReportRetry(String code)
	{
		AirReport report = getReport(code);
		reportActionDoRetry(report);
	}

	/**
	 * 清理上报记录
	 */
	public void ReportClean()
	{
		List<AirReport> reportsTemp = new ArrayList<AirReport>();
		for (int i = 0; i < reports.size(); i++)
		{
			AirReport report = reports.get(i);
			if (report != null && report.getState() != AirReport.STATE_RESULT_OK)
			{
				reportsTemp.add(report);
			}
		}
		reports.clear();
		reports.addAll(reportsTemp);

		mDbProxy.DbReportClean();

		if (reportListener != null)
		{
			reportListener.onMmiReportResourceListRefresh();
		}
	}

	/**
	 * 删除一条上报记录
	 */
	public void ReportDelete(AirReport report)
	{
		if (report != null && report.getState() != AirReport.STATE_UPLOADING)
		{
			reports.remove(report);
			if (reportListener != null)
			{
				reportListener.onMmiReportDel();
			}
			mDbProxy.DbReportDelete(report.getCode());
		}
	}

	/**
	 * 删除多条上报记录
	 * @param reports 要删除的上报记录列表
	 */
	
	public void ReportsDelete(Map<AirReport, Boolean> reports)
	{
		if (null != reports && reports.size() > 0)
		{
			Iterator<Entry<AirReport, Boolean>> iterator = reports.entrySet().iterator();
			while (iterator.hasNext())
			{
				Map.Entry<AirReport, Boolean> entry = (Map.Entry<AirReport, Boolean>) iterator.next();
				if (entry.getValue())
				{
					ReportDelete(entry.getKey());
				}
			}
		}
	}

	/**
	 * 获取上报任务
	 * @param code 上报记录code
	 * @return 上报记录Entity
	 */
	private AirReport reportGetTask(String code)
	{
		AirReport report = null;
		for (int i = 0; i < reports.size(); i++)
		{
			if (TextUtils.equals(reports.get(i).getCode(), code))
			{
				if (i + 1 < reports.size())
				{
					report = reports.get(i + 1);
				}
				break;
			}
		}
		return report;
	}

	/********************************************
	 * 
	 * Actions
	 * 
	 ********************************************/
	/**
	 * 执行上报新记录
	 * @param report 上报记录Entity
	 */
	private void reportActionDoNew(AirReport report)
	{
		if (reportDoing == null)
		{
			if (reportToServer(report))
			{
				reports.add(report);
				report.setProgress(0);
				report.setState(AirReport.STATE_UPLOADING);
				reportDoing = report;
				mDbProxy.DbReportNew(report);
			}
		}
		else
		{
			reports.add(report);
			report.setProgress(0);
			report.setState(AirReport.STATE_WAITING);
			mDbProxy.DbReportNew(report);
		}

		if (reportListener != null)
		{
			reportListener.onMmiReportResourceListRefresh();
		}
		broadCastReportState();
	}

	/**
	 * 执行重试上报
	 * @param report 上报记录Entity
	 */
	private void reportActionDoRetry(AirReport report)
	{
		if (report != null && report.getState() == AirReport.STATE_RESULT_FAIL)
		{
			if (reportDoing == null)
			{
				if (reportToServer(report))
				{
					report.setProgress(0);
					report.setState(AirReport.STATE_UPLOADING);
					reportDoing = report;
				}
			}
			else
			{
				report.setProgress(0);
				report.setState(AirReport.STATE_WAITING);
			}

			if (reportListener != null)
			{
				reportListener.onMmiReportResourceListRefresh();
			}
			broadCastReportState();
		}
	}

	/**
	 * 上报进度
	 * @param progress 进度值 0~100
	 */
	private void reportActionProgress(int progress)
	{
		if (reportDoing != null)
		{
			reportDoing.setProgress(progress);
			if (reportListener != null)
			{
				reportListener.onMmiReportResourceListRefresh();
				reportListener.onMmiReportProgress(progress);
			}
		}
	}

	/**
	 * 上报结果
	 * @param statusCode 状态码
	 * @param resId 资源Id
	 */
	private void reportActionResult(int statusCode, String resId)
	{
		if (reportDoing != null)
		{
			Log.i(AirReportManager.class, "reportActionResult status code=" + statusCode + ",resId=" + resId);
			if (statusCode == AirtalkeeReport.RESOURCE_STATUS_CODE_OK)
			{
				mDbProxy.DbReportResultOk(reportDoing.getCode());
				if (reportDoing.getType() == AirtalkeeReport.RESOURCE_TYPE_PICTURE)
				{
					if (MenuReportAsPicActivity.getInstance() != null && MenuReportAsPicActivity.finishFlag)
					{
						MenuReportAsPicActivity.getInstance().finish();
					}
				}
				else
				{
					if (MenuReportAsVidActivity.getInstance() != null && MenuReportAsVidActivity.finishFlag)
					{
						MenuReportAsVidActivity.getInstance().finish();
					}
				}
				if(Toast.isDebug) Toast.makeText1(AirServices.getInstance(), R.drawable.ic_success, AirServices.getInstance().getString(R.string.talk_tools_report_success), Toast.LENGTH_LONG).show();
				// finish()
			}
			else if (statusCode == AirtalkeeReport.RESOURCE_STATUS_CODE_ERR_SPACE_OVERFLOW)
			{
				Util.Toast(AirServices.getInstance(), AirServices.getInstance().getString(R.string.talk_report_upload_err_space_overflow));
			}
			else
			{
				if (reportDoing.getType() == AirtalkeeReport.RESOURCE_TYPE_PICTURE)
				{
					final MenuReportAsPicActivity context = MenuReportAsPicActivity.getInstance();
					Log.i(AirReportManager.class, "context is " + (context == null ? "null" : "not null"));
					if (context != null)
					{
						AlertDialog dialog;
						if (statusCode == AirtalkeeReport.RESOURCE_STATUS_CODE_ERR_NETWORK)
							dialog = new AlertDialog(context, context.getString(R.string.talk_network_error), context.getString(R.string.talk_ok_2), dialogListener, false, AirtalkeeReport.RESOURCE_TYPE_PICTURE);
						else
							dialog = new AlertDialog(context, context.getString(R.string.talk_tools_report_fail), context.getString(R.string.talk_tools_report_fail_tip), context.getString(R.string.talk_tools_report_continue), context.getString(R.string.talk_ok_2), dialogListener, AirtalkeeReport.RESOURCE_TYPE_PICTURE);
						dialog.show();
					}
					else
						Util.Toast(AirServices.getInstance(), AirServices.getInstance().getString(R.string.talk_report_upload_pic_err), R.drawable.ic_error);
				}
				else
				{
					final MenuReportAsVidActivity context = MenuReportAsVidActivity.getInstance();
					if (context != null)
					{
						AlertDialog dialog;
						if (statusCode == AirtalkeeReport.RESOURCE_STATUS_CODE_ERR_NETWORK)
							dialog = new AlertDialog(context, context.getString(R.string.talk_network_error), context.getString(R.string.talk_ok_2), dialogListener, false, AirtalkeeReport.RESOURCE_TYPE_VIDEO);
						else
							dialog = new AlertDialog(context, context.getString(R.string.talk_tools_report_fail), context.getString(R.string.talk_tools_report_fail_tip), context.getString(R.string.talk_tools_report_continue), context.getString(R.string.talk_ok_2), dialogListener, AirtalkeeReport.RESOURCE_TYPE_VIDEO);
						dialog.show();
					}
					else
						Util.Toast(AirServices.getInstance(), AirServices.getInstance().getString(R.string.talk_report_upload_vid_err), R.drawable.ic_error);
				}
			}

			reportDoing.setState(statusCode == AirtalkeeReport.RESOURCE_STATUS_CODE_OK ? AirReport.STATE_RESULT_OK : AirReport.STATE_RESULT_FAIL);
			reportDoing.setProgress(0);
			broadCastReportState();

			AirReport report = reportGetTask(reportDoing.getCode());
			if (report != null)
			{
				if (reportToServer(report))
				{
					report.setProgress(0);
					report.setState(AirReport.STATE_UPLOADING);
					reportDoing = report;
				}
				else
				{
					reportDoing = null;
				}
			}
			else
			{
				reportDoing = null;
			}
			if (reportListener != null)
			{
				Log.i(AirReport.class, "reportListener is not null");
				reportListener.onMmiReportResourceListRefresh();
			}
			else 
			{
				Log.i(AirReport.class, "reportListener is null");
				if (MenuReportActivity.getInstance() != null)
					MenuReportActivity.getInstance().refreshReport();
			}
		}
	}
	
	private DialogListener dialogListener = new DialogListener()
	{
		@Override
		public void onClickOk(int id, boolean isChecked)
		{
		}
		
		@Override
		public void onClickOk(int id, Object obj)
		{
			Activity context = null;
			if (id == AirtalkeeReport.RESOURCE_TYPE_VIDEO)
				context = MenuReportAsVidActivity.getInstance();
			else if (id == AirtalkeeReport.RESOURCE_TYPE_PICTURE)
				context = MenuReportAsPicActivity.getInstance();
			if (context != null)
				context.finish();
		}
		
		@Override
		public void onClickCancel(int id)
		{
			if (id == AirtalkeeReport.RESOURCE_TYPE_VIDEO && MenuReportAsVidActivity.getInstance() != null)
				MenuReportAsVidActivity.getInstance().reportPost();
			else if (id == AirtalkeeReport.RESOURCE_TYPE_PICTURE && MenuReportAsPicActivity.getInstance() != null)
				MenuReportAsPicActivity.getInstance().reportPost();
		}
	};

	/**
	 * 上报到服务器
	 * @param report 上报记录Entity
	 * @return 是否成功
	 */
	private boolean reportToServer(AirReport report)
	{
		boolean isOk = false;
		if (AirtalkeeAccount.getInstance().getUser().getSecret() > 0) // 加密的客户端 不用断点续传
		{
			try
			{
				byte[] data = iOperate.readByteFile("", report.getResPath(), true);
				if (data != null)
				{
					AirtalkeeReport.getInstance().ReportResource(report.getType(), report.getTypeExtension(), data, report.getResContent(), report.getLocType(), report.getLocLatitude() + "", report.getLocLongitude() + "");
					isOk = true;
				}
			}
			catch (Exception e)
			{                                     
				e.printStackTrace();
			}
			isOk = true;
		}
		else 
		{
			if (report.isResFileMulti())
				AirtalkeeReport.getInstance().ReportMultiple(report.getCode(), report.getTaskId(), report.getResFileList(), report.getResContent(), report.getLocType(), report.getLocLatitude()+"", report.getLocLongitude()+"");
			else
				AirtalkeeReport.getInstance().ReportResource(report.getTaskId(), report.getType(), report.getResPath(), report.getResContent(), report.getLocType(), report.getLocLatitude() + "", report.getLocLongitude() + "");
			isOk = true;
		}
		return isOk;
	}

	/**
	 * 获取正在上报的记录Entity
	 * @return 上报记录Entity
	 */
	public AirReport getCurrentReportDoing()
	{
		return reportDoing;
	}

	/********************************************
	 * 
	 * Events
	 * 
	 ********************************************/

	@Override
	public void onReportResourceProgress(int progress)
	{
		try
		{
			reportActionProgress(progress);
		}
		catch (Exception e)
		{ }
	}

	@Override
	public void onReportResourceUploaded(int statusCode, String resId)
	{
		reportActionResult(statusCode, resId);
	}


	/********************************************
	 *
	 * Multi Events
	 *
	 ********************************************/

	@Override
	public void onReportMultiResult(boolean isOk, String code) {
		reportActionResult(isOk ? AirtalkeeReport.RESOURCE_STATUS_CODE_OK : AirtalkeeReport.RESOURCE_STATUS_CODE_ERR, "");
	}

	@Override
	public void onReportMultiFileProgress(boolean isOk, String code, int fileIndex, int progress) {
		try
		{
			int p = 0;
			if (reportDoing != null && reportDoing.isResFileMulti() && TextUtils.equals(reportDoing.getCode(), code))
			{
				int t = 0;
				int count = reportDoing.getResFileList().size();
				AirImage image = reportDoing.getResFileList().get(fileIndex);
				image.setProgress(progress);
				for (int i = 0; i < count; i ++)
				{
					t += reportDoing.getResFileList().get(i).getProgress();
				}
				p = t * 100 / (100 * count);
				Log.i(AirReportManager.class, "Upload Image Progress: " + p);
			}
			reportActionProgress(p);
		}
		catch (Exception e)
		{ }
	}

	@Override
	public void onReportMultiFileUpload(boolean isOk, String code, int fileIndex) {
		onReportMultiFileProgress(isOk, code, fileIndex, 100);
	}


	/********************************************
	 * 
	 * BroadCast
	 * 
	 ********************************************/
	public static final String AIR_ACTION = "com.cmccpoc.action";
	public static final int OPER_UPLOAD_PICTURE = 1000;
	public static final int OPER_UPLOAD_VIDEO = 1001;

	/**
	 * 发送广播通知上报状态
	 */
	private void broadCastReportState()
	{
		Intent intent = new Intent(AIR_ACTION);

		if (reportDoing != null)
		{
			int oper = OPER_UPLOAD_VIDEO;
			if (reportDoing.getType() == AirtalkeeReport.RESOURCE_TYPE_PICTURE)
				oper = OPER_UPLOAD_PICTURE;
			intent.putExtra("operCode", oper);
			try
			{
				JSONObject obj = new JSONObject();
				obj.put("state", reportDoing.getState());
				Log.d(AirReportManager.class, "------state----=" + obj.optInt("state"));

				intent.putExtra("json", obj.toString());
				if (AirServices.getInstance() != null)
				{
					AirServices.getInstance().sendBroadcast(intent);
				}
			}
			catch (Exception e)
			{
				Log.e(AirReportManager.class, "broadCastReportState Exception e" + e.toString());
			}
		}
	}

}
