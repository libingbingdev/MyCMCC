package com.cmccpoc.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.airtalkee.sdk.util.Log;
import com.cmccpoc.entity.AirTaskCase;

import java.util.List;

/**
 * 上报记录操作类
 * @author Yao
 */
public class TableTaskCaseDao
{

	private static final String JSON_FILES = "files";
	private static final String JSON_PATH = "path";
	private static final String JSON_TYPE = "type";

	private static final TableTaskCaseDao instance = new TableTaskCaseDao();

	private TableTaskCaseDao()
	{};

	private static DBHelp dbHelp;

	protected static TableTaskCaseDao getInstance(DBHelp db)
	{
		dbHelp = db;
		return instance;
	}

	/********************************
	 * Message List
	 ********************************/

	/**
	 * 加载记录
	 * @param  tasks 列表
	 */
	protected void TaskCaseLoad(List<AirTaskCase> tasks)
	{
		SQLiteDatabase db = dbHelp.DatabaseReadableGet();
		if (db != null)
		{
			String sql = "SELECT * FROM " + DBDefine.db_task_case + " WHERE " + DBDefine.t_task_case.UID + "=" + dbHelp.getUid() + " ORDER BY " + DBDefine.t_task_case.ID + " DESC";
			tasks.clear();
			try
			{
				Cursor c = db.rawQuery(sql, null);
				while (c.moveToNext())
				{
					AirTaskCase task = new AirTaskCase();
					task.setTaskId(c.getString(c.getColumnIndex(DBDefine.t_task_case.taskId)));
					task.setCaseCode(c.getString(c.getColumnIndex(DBDefine.t_task_case.caseCode)));
					task.setCaseName(c.getString(c.getColumnIndex(DBDefine.t_task_case.caseName)));
					task.setCarNo(c.getString(c.getColumnIndex(DBDefine.t_task_case.carNo)));
					task.setDetail(c.getString(c.getColumnIndex(DBDefine.t_task_case.detail)));
					task.setLocal(c.getInt(c.getColumnIndex(DBDefine.t_task_case.isLocal)) > 0 ? true : false);
					tasks.add(task);
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
	protected void TaskCaseNew(AirTaskCase task)
	{
		ContentValues cv = new ContentValues();
		cv.put(DBDefine.t_task_case.UID, dbHelp.getUid());
		cv.put(DBDefine.t_task_case.taskId, task.getTaskId());
		cv.put(DBDefine.t_task_case.caseCode, task.getCaseCode());
		cv.put(DBDefine.t_task_case.caseName, task.getCaseName());
		cv.put(DBDefine.t_task_case.carNo, task.getCarNo());
		cv.put(DBDefine.t_task_case.detail, task.getDetail());
		cv.put(DBDefine.t_task_case.isLocal, task.isLocal() ? 1 : 0);
		dbHelp.insert(DBDefine.db_task_case, cv);
	}

	protected void TaskCaseUpdate(AirTaskCase task)
	{
		String sql = String.format("UPDATE " + DBDefine.db_task_case + " SET " + DBDefine.t_task_case.caseCode + " = '%s', " + DBDefine.t_task_case.caseName + " = '%s', "
						+ DBDefine.t_task_case.carNo + " = '%s', " + DBDefine.t_task_case.detail + " = '%s' WHERE " + DBDefine.t_task_case.UID
						+ "=%s AND " + DBDefine.t_task_case.taskId + " = '%s'",
				task.getCaseCode(), task.getCaseName(), task.getCarNo(), task.getDetail(), dbHelp.getUid(), task.getTaskId());
		dbHelp.update(sql);
	}
	/**
	 * 删除一条上报记录
	 * @param code 上报记录code
	 */
	protected void TaskCaseDelete(String taskId)
	{
		String sql = "DELETE FROM " + DBDefine.db_task_case + " WHERE " + DBDefine.t_task_case.UID + "=" + dbHelp.getUid() + " AND " + DBDefine.t_task_case.taskId + " = '" + taskId + "'";
		dbHelp.del(sql);
	}

	/**
	 * 清除所有上报记录
	 */
	protected void TaskCaseClean()
	{
		String sql = "DELETE FROM " + DBDefine.db_task_case + " WHERE " + DBDefine.t_task_case.UID + "=" + dbHelp.getUid();
		dbHelp.del(sql);
	}

}
