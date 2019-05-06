package com.cmccpoc.dao;

import java.io.File;
import java.util.List;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.airtalkee.sdk.entity.AirImage;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.entity.AirReport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 上报记录操作类
 * @author Yao
 */
public class TableReportDao
{

	private static final String JSON_FILES = "files";
	private static final String JSON_PATH = "path";
	private static final String JSON_TYPE = "type";

	private static final TableReportDao instance = new TableReportDao();

	private TableReportDao()
	{};

	private static DBHelp dbHelp;

	protected static TableReportDao getInstance(DBHelp db)
	{
		dbHelp = db;
		return instance;
	}

	/********************************
	 * Message List
	 ********************************/

	/**
	 * 加载上报记录
	 * @param reports 上报列表
	 */
	protected void ReportLoad(List<AirReport> reports)
	{
		SQLiteDatabase db = dbHelp.DatabaseReadableGet();
		if (db != null)
		{
			String sql = "SELECT * FROM " + DBDefine.db_report + " WHERE " + DBDefine.t_report.UID + "=" + dbHelp.getUid();
			reports.clear();
			try
			{
				Cursor c = db.rawQuery(sql, null);
				while (c.moveToNext())
				{
					AirReport report = new AirReport();
					report.setCode(c.getString(c.getColumnIndex(DBDefine.t_report.code)));
					report.setResPath(c.getString(c.getColumnIndex(DBDefine.t_report.resPath)));
					report.setResUri(Uri.fromFile(new File(report.getResPath())));
					report.setResContent(c.getString(c.getColumnIndex(DBDefine.t_report.resContent)));
					report.setResSize(c.getInt(c.getColumnIndex(DBDefine.t_report.resSize)));
					report.setTime(c.getString(c.getColumnIndex(DBDefine.t_report.time)));
					report.setLocLatitude(Double.parseDouble(c.getString(c.getColumnIndex(DBDefine.t_report.locLatitude))));
					report.setLocLongitude(Double.parseDouble(c.getString(c.getColumnIndex(DBDefine.t_report.locLongitude))));
					report.setType(c.getInt(c.getColumnIndex(DBDefine.t_report.type)));
					report.setTypeExtension(c.getString(c.getColumnIndex(DBDefine.t_report.typeExt)));
					report.setState(c.getInt(c.getColumnIndex(DBDefine.t_report.state)) == 1 ? AirReport.STATE_RESULT_OK : AirReport.STATE_RESULT_FAIL);
					report.setProgress(0);

					report.setResFileMulti(c.getInt(c.getColumnIndex(DBDefine.t_report.resFileMulti)) == 1 ? true : false);
					if (report.isResFileMulti())
					{
						String json = c.getString(c.getColumnIndex(DBDefine.t_report.resFiles));
						if (json != null)
						{
							try
							{
								JSONObject obj = new JSONObject(json);
								if (obj != null)
								{
									JSONArray array = obj.optJSONArray(JSON_FILES);
									if (array != null)
									{
										report.getResFileList().clear();
										for (int i = 0; i < array.length(); i++)
										{
											AirImage image = new AirImage();
											image.setFileFullName(array.optJSONObject(i).optString(JSON_PATH));
											image.setFileUri(Uri.fromFile(new File(image.getFileFullName())));
											image.setType(array.optJSONObject(i).optInt(JSON_TYPE));
											report.getResFileList().add(image);
										}
									}
								}
							}
							catch (JSONException e)
							{
								e.printStackTrace();
							}
						}
					}
					reports.add(report);
				}
				c.close();
			}
			catch (Exception e)
			{
				Log.e(DBHelp.class, "[SQL EXCEPTION] " + sql + " -> " + e.getMessage());
			}
			dbHelp.DatabaseReadableClose(db);
		}
	}

	/**
	 * 添加一个上报记录
	 * @param report 上报Entity
	 */
	protected void ReportNew(AirReport report)
	{
		ContentValues cv = new ContentValues();
		cv.put(DBDefine.t_report.UID, dbHelp.getUid());
		cv.put(DBDefine.t_report.code, report.getCode());
		cv.put(DBDefine.t_report.resPath, report.getResPath());
		cv.put(DBDefine.t_report.resContent, report.getResContent());
		cv.put(DBDefine.t_report.resSize, report.getResSize());
		cv.put(DBDefine.t_report.locLatitude, report.getLocLatitude() + "");
		cv.put(DBDefine.t_report.locLongitude, report.getLocLongitude() + "");
		cv.put(DBDefine.t_report.time, report.getTime());
		cv.put(DBDefine.t_report.type, report.getType());
		cv.put(DBDefine.t_report.typeExt, report.getTypeExtension());
		cv.put(DBDefine.t_report.state, 0);
		cv.put(DBDefine.t_report.resFileMulti, report.isResFileMulti() ? 1 : 0);

		String json = "";
		if (report.isResFileMulti())
		{
			try
			{
				JSONObject obj = new JSONObject();
				if (obj != null)
				{
					JSONArray array = new JSONArray();
					for (int i = 0; i < report.getResFileList().size(); i ++)
					{
						JSONObject item = new JSONObject();
						item.put(JSON_PATH, report.getResFileList().get(i).getFileFullName());
						item.put(JSON_TYPE, report.getResFileList().get(i).getType());
						array.put(i, item);
					}
					obj.put(JSON_FILES, array);
					json = obj.toString();
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		cv.put(DBDefine.t_report.resFiles, json);

		dbHelp.insert(DBDefine.db_report, cv);
	}

	/**
	 * 新上报记录状态
	 * @param code 上报记录code
	 */
	protected void ReportResultOk(String code)
	{
		String sql = "UPDATE " + DBDefine.db_report + " SET " + DBDefine.t_report.state + " = 1 " + " WHERE " + DBDefine.t_report.UID + "=" + dbHelp.getUid() + " AND "
			+ DBDefine.t_report.code + " = '" + code + "'";
		dbHelp.update(sql);
	}

	/**
	 * 删除一条上报记录
	 * @param code 上报记录code
	 */
	protected void ReportDelete(String code)
	{
		String sql = "DELETE FROM " + DBDefine.db_report + " WHERE " + DBDefine.t_report.UID + "=" + dbHelp.getUid() + " AND " + DBDefine.t_report.code + " = '" + code + "'";
		dbHelp.del(sql);
	}

	/**
	 * 清除所有上报记录
	 */
	protected void ReportClean()
	{
		String sql = "DELETE FROM " + DBDefine.db_report + " WHERE " + DBDefine.t_report.UID + "=" + dbHelp.getUid() + " AND " + DBDefine.t_report.state + " = 1";
		dbHelp.del(sql);
	}

}
