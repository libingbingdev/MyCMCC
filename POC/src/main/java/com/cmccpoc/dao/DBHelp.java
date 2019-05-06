package com.cmccpoc.dao;

import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirNotice;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.entity.AirTask;
import com.airtalkee.sdk.entity.DBProxy;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.entity.AirReport;
import com.cmccpoc.entity.AirTaskCase;

/**
 * 数据库帮助类
 * @author Yao
 */

public class DBHelp extends SQLiteOpenHelper implements DBProxy, DBProxyReport, DBProxyTaskCase
{
	private TableChannelDao iChannelDao = null;
	private TableMessageDao iMessageDao = null;
	private TableSessionListDao iSessionListDao = null;
	private TableReportDao iReportDao = null;
	private TableTaskCaseDao iTaskCaseDap = null;

	private static SQLiteDatabase db = null;

	private static String UID = "";

	public DBHelp(Context context)
	{
		super(context, DBDefine.DBNAME, null, DBDefine.DBVERSION);
		iReportDao = TableReportDao.getInstance(this);
		iChannelDao = TableChannelDao.getInstance(this);
		iMessageDao = TableMessageDao.getInstance(this);
		iSessionListDao = TableSessionListDao.getInstance(this);
		iReportDao = TableReportDao.getInstance(this);
		iTaskCaseDap = TableTaskCaseDao.getInstance(this);
	}

	/**
	 * 数据库更新
	 */
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2)
	{
		Log.e(DBHelp.class, "DB dbUpgrade!!");
		DBHelp.db = db;
		dropTable();
		db.setVersion(arg1);
		createTable();
	}

	@SuppressWarnings("static-access")
	public void onCreate(SQLiteDatabase db)
	{
		Log.e(DBHelp.class, "DB onCreate!!");
		this.db = db;
		createTable();
	}

	/**
	 * 初始化创建数据库
	 */
	private void createTable()
	{
		Log.e(DBHelp.class, "DB CREATE!!");
		db.execSQL(DBDefine.CREATE_T_REPORT);
		db.execSQL(DBDefine.CREATE_T_CHANNEL);
		db.execSQL(DBDefine.CREATE_T_MESSAGE);
		db.execSQL(DBDefine.CREATE_T_SESSION);
		db.execSQL(DBDefine.CREATE_T_SESSION_MEMBER);
		db.execSQL(DBDefine.CREATE_T_TASK_CASE);
	}

	/**
	 * 删除数据库
	 */
	private void dropTable()
	{
		Log.e(DBHelp.class, "DB DROP!!");
		// db = this.getWritableDatabase();
		if (DBDefine.Tables != null && DBDefine.Tables.length > 0)
		{
			for (int i = 0; i < DBDefine.Tables.length; i++)
			{
				String sql = "DROP TABLE IF EXISTS " + DBDefine.Tables[i];
				db.execSQL(sql);
			}
		}
		// db.close();
	}

	/************************************************
	 * 
	 * DB Write/Read lock
	 * 
	 ************************************************/

	private boolean isDbReading = false;
	private boolean isDbWriting = false;

	/**
	 *  获取数据库读权限
	 * @return SQLiteDB
	 */
	public SQLiteDatabase DatabaseReadableGet()
	{
		SQLiteDatabase db = null;
		try
		{
			while (isDbReading || isDbWriting)
			{
				Thread.sleep(1);
			}

			isDbReading = true;
			db = getReadableDatabase();
		}
		catch (Exception e)
		{
			// TODO: handle exception
			Log.e(DBHelp.class, "[DB Exception->]" + e.getMessage());
			isDbReading = false;
		}
		return db;
	}

	/**
	 *  取消数据库读权限
	 * @param SQLiteDB
	 */
	public void DatabaseReadableClose(SQLiteDatabase db)
	{
		try
		{
			db.close();
		}
		catch (Exception e)
		{
			// TODO: handle exception
			Log.e(DBHelp.class, "[DB Exception->]" + e.getMessage());
		}
		isDbReading = false;
	}

	/**
	 * 获取数据库写权限
	 * @return SQLiteDB
	 */
	public SQLiteDatabase DatabaseWritableGet()
	{
		SQLiteDatabase db = null;
		try
		{
			while (isDbReading || isDbWriting)
			{
				Thread.sleep(1);
			}

			isDbWriting = true;
			db = getWritableDatabase();
		}
		catch (Exception e)
		{
			// TODO: handle exception
			Log.e(DBHelp.class, "[DB Exception->]" + e.getMessage());
			isDbWriting = false;
		}
		return db;
	}

	/**
	 * 取消数据库写权限
	 * @param SQLiteDB
	 */
	public void DatabaseWritableClose(SQLiteDatabase db)
	{
		try
		{
			db.close();
		}
		catch (Exception e)
		{
			// TODO: handle exception
			Log.e(DBHelp.class, "[DB Exception->]" + e.getMessage());
		}
		isDbWriting = false;
	}

	/************************************************
	 * 
	 * DB Write action queue
	 * 
	 ************************************************/

	// private static final int DB_WRITE_ACTION_TYPE_NONE = -1;
	private static final int DB_WRITE_ACTION_TYPE_INSERT_CV = 0;
	private static final int DB_WRITE_ACTION_TYPE_INSERT_CV_LIST = 1;
	private static final int DB_WRITE_ACTION_TYPE_UPDATE = 2;
	private static final int DB_WRITE_ACTION_TYPE_DELETE = 3;

	private class dbWriteAction
	{
		public dbWriteAction()
		{}

		public String param1 = "";
		public Object param2 = null;
	}

	private Handler dbWriteHandler;

	private void dbActionDo(int type, String param1, Object param2)
	{
		int retry = 2000 / 5;
		while (dbWriteHandler == null)
		{
			Log.i(DBHelp.class, "[DB Waiting ready!!]");
			try
			{
				Thread.sleep(5);
			}
			catch (Exception e)
			{
				// TODO: handle exception
			}
			retry--;
			if (retry <= 0)
				return;
		}
		Message dbMsg = dbWriteHandler.obtainMessage();
		dbWriteAction action = new dbWriteAction();
		action.param1 = param1;
		action.param2 = param2;
		dbMsg.arg1 = type;
		dbMsg.obj = action;
		dbWriteHandler.sendMessage(dbMsg);
	}

	private void dbActionRun()
	{
		dbWriteThread.start();
	}

	/**
	 * 数据库CRUD操作
	 */
	private Thread dbWriteThread = new Thread()
	{
		@SuppressWarnings("unchecked")
		public void run()
		{
			// try
			{
				Looper.prepare();
				Log.i(DBHelp.class, "[DB Action Thread BEGIN]");
				dbWriteHandler = new Handler(Looper.myLooper())
				{
					public void handleMessage(Message msg)
					{
						dbWriteAction action = (dbWriteAction) msg.obj;
						try
						{
							db = DBHelp.this.DatabaseWritableGet();
						}
						catch (Exception e)
						{
							// TODO: handle exception
							Log.e(DBHelp.class, "ERROR getWritableDatabase: " + e.getMessage());
						}
						if (db != null)
						{
							try
							{
								Log.i(DBHelp.class, "[DB Action Begin] action = " + msg.arg1);
								switch (msg.arg1)
								{
									// 添加一条记录
									case DB_WRITE_ACTION_TYPE_INSERT_CV:
									{
										ContentValues cv = (ContentValues) action.param2;
										if (cv == null)
											break;
										db.beginTransaction();
										db.insert(action.param1, null, cv);
										db.setTransactionSuccessful();
										db.endTransaction();
										break;
									}
									// 添加多条记录
									case DB_WRITE_ACTION_TYPE_INSERT_CV_LIST:
									{
										List<ContentValues> cvs = (List<ContentValues>) action.param2;
										if (cvs == null)
											break;
										for (int i = 0; i < cvs.size(); i++)
										{
											db.beginTransaction();
											db.insert(action.param1, null, cvs.get(i));
											db.setTransactionSuccessful();
											db.endTransaction();
										}
										break;
									}
									// 更新一条记录
									case DB_WRITE_ACTION_TYPE_UPDATE:
									{
										// Object []bindArgs =
										// (Object[])action.param2;
										db.beginTransaction();
										db.execSQL(action.param1);
										db.setTransactionSuccessful();
										db.endTransaction();
										break;
									}
									// 删除一条记录
									case DB_WRITE_ACTION_TYPE_DELETE:
									{
										db.beginTransaction();
										db.execSQL(action.param1);
										db.setTransactionSuccessful();
										db.endTransaction();
										break;
									}
									default:
										break;
								}
								Log.i(DBHelp.class, "[DB Action End]");
							}
							catch (Exception e)
							{
								// TODO: handle exception
								Log.e(DBHelp.class, "[DB Exception->]" + e.getMessage());
							}
							try
							{
								DBHelp.this.DatabaseWritableClose(db);
							}
							catch (Exception e)
							{
								// TODO: handle exception
								Log.e(DBHelp.class, "ERROR close: " + e.getMessage());
							}
						}
					}
				};
				try
				{
					Thread.sleep(10);
				}
				catch (Exception e)
				{
					// TODO: handle exception
				}
				Log.i(DBHelp.class, "[DB Action Thread END]");
				Looper.loop();
			}
			// catch (Exception e)
			// {
			// Log.e(DBHelp.class,"[DB Write Exception] " + e.getMessage());
			// }
		}
	};

	/************************************************
	 * 
	 * DB Write action
	 * 
	 ************************************************/

	protected void insert(String tableName, ContentValues cv)
	{
		Log.d(DBHelp.class, "[DB]insert  tableName=[" + tableName + "] cv =[" + cv.toString() + "]");
		dbActionDo(DB_WRITE_ACTION_TYPE_INSERT_CV, tableName, cv);
	}

	protected void insert(String tableName, List<ContentValues> cvs)
	{
		Log.d(DBHelp.class, "[DB]insert  tableName=[" + tableName + "]");
		dbActionDo(DB_WRITE_ACTION_TYPE_INSERT_CV_LIST, tableName, cvs);
	}

	protected void update(String sql)
	{

		Log.d(DBHelp.class, "[DB]update  sql=[" + sql + "]");
		dbActionDo(DB_WRITE_ACTION_TYPE_UPDATE, sql, null);
	}

	protected void del(String sql)
	{
		Log.d(DBHelp.class, "[DB]del  sql=[" + sql + "]");
		dbActionDo(DB_WRITE_ACTION_TYPE_DELETE, sql, null);
	}

	/************************************************
	 * 
	 * DB Proxy
	 * 
	 ************************************************/

	public String getUid()
	{
		return UID;
	}

	@Override
	public void DbActionRun()
	{
		dbActionRun();
	}

	@Override
	public void DbSetUID(String uid)
	{
		// TODO Auto-generated method stub
		UID = uid;
	}

	@Override
	public void UserDbClean()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void UserDbLoad(AirContact user)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void UserDbSet(AirContact user)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void ContactDbCleanList()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void ContactDbGetList(List<AirContact> contacts)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void ContactDbDelete(String contact_id)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void ContactDbAppend(AirContact contact)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void ContactDbSet(List<AirContact> list)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void ContactDbUpdate(AirContact contact)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void ChannelDbAppend(AirChannel channel)
	{
		// TODO Auto-generated method stub
		iChannelDao.channelAppend(channel);
	}

	@Override
	public void ChannelDbClean()
	{
		// TODO Auto-generated method stub
		iChannelDao.channelClean();
	}

	@Override
	public void ChannelDbDelete(String channelId)
	{
		// TODO Auto-generated method stub
		iChannelDao.channelDelete(channelId);
	}

	@Override
	public List<AirChannel> ChannelDbLoad()
	{
		// TODO Auto-generated method stub
		return iChannelDao.channelLoad();
	}

	@Override
	public void ChannelDbLoad(List<AirChannel> channels)
	{
		// TODO Auto-generated method stub
		iChannelDao.channelLoad(channels);
	}

	@Override
	public void ChannelDbSave(List<AirChannel> channels)
	{
		// TODO Auto-generated method stub
		iChannelDao.channelSave(channels);
	}

	@Override
	public void ChannelDbUpdate(AirChannel channel)
	{
		// TODO Auto-generated method stub
		iChannelDao.channelUpdate(channel);
	}

	@Override
	public void ChannelMemberDbClean(String channelId)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void ChannelMemberDbLoad(AirChannel channel)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void ChannelMemberDbSave(AirChannel channel)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void MessageDbAppend(String sid, AirMessage msg)
	{
		// TODO Auto-generated method stub
		iMessageDao.messageAppend(sid, msg);
	}

	@Override
	public void MessageDbClean(String sid)
	{
		// TODO Auto-generated method stub
		iMessageDao.messageClean(sid);
	}

	@Override
	public void MessageDbCleanAll()
	{
		// TODO Auto-generated method stub
		iMessageDao.messageCleanAll();
	}

	@Override
	public void MessageDbDelete(String sid, String msgId)
	{
		// TODO Auto-generated method stub
		iMessageDao.messageDelete(sid, msgId);
	}

	@Override
	public List<AirMessage> MessageBbPttRecordQuery(String sid, int limit)
	{
		return iMessageDao.messageQueryLast(sid, limit);
	}

	@Override
	public void MessageDbLoad(AirSession session)
	{
		// TODO Auto-generated method stub
		iMessageDao.messageLoad(session);
	}

	@Override
	public List<AirMessage> MessageDbLoad(String sid, int msgPosition, int msgCount)
	{
		return iMessageDao.MessageDbLoad(sid, msgPosition, msgCount);
	}

	@Override
	public void MessageDbUpdate(AirMessage msg)
	{
		// TODO Auto-generated method stub
		iMessageDao.messageUpdate(msg);
	}

	@Override
	public void SessionDbAppend(AirSession session)
	{
		// TODO Auto-generated method stub
		iSessionListDao.sessionAppend(session);
	}

	@Override
	public void SessionDbClean()
	{
		// TODO Auto-generated method stub
		iSessionListDao.sessionClean();
	}

	@Override
	public void SessionDbCleanUnread(String sid)
	{
		// TODO Auto-generated method stub
		iSessionListDao.sessionCleanUnread(sid);
	}

	@Override
	public void SessionDbDelete(String sid)
	{
		// TODO Auto-generated method stub
		iSessionListDao.sessionDelete(sid);
	}

	@Override
	public List<AirSession> SessionDbLoad()
	{
		// TODO Auto-generated method stub
		return iSessionListDao.sessionLoad();
	}

	@Override
	public void SessionDbOrder(String sid)
	{
		// TODO Auto-generated method stub
		iSessionListDao.sessionOrder(sid);
	}

	@Override
	public void SessionDbUpdate(final String sid, final AirSession session)
	{
		// TODO Auto-generated method stub
		iSessionListDao.sessionUpdate(sid, session);
	}

	@Override
	public void SessionMemberDbAppend(String sid, AirContact contact)
	{
		// TODO Auto-generated method stub
		iSessionListDao.sessionMemberAppend(sid, contact);
	}

	@Override
	public void SessionMemberDbSave(String sid, List<AirContact> contacts)
	{
		// TODO Auto-generated method stub
		iSessionListDao.sessionMemberSave(sid, contacts);
	}

	@Override
	public void SessionMemberDbClean(String sid)
	{
		// TODO Auto-generated method stub
		iSessionListDao.sessionMemberClean(sid);
	}

	@Override
	public void SessionMemberDbDelete(String sid, String ipocid)
	{
		// TODO Auto-generated method stub
		iSessionListDao.sessionMemberDelete(sid, ipocid);
	}

	@Override
	public void SessionMemberDbLoad(AirSession session)
	{
		// TODO Auto-generated method stub
		iSessionListDao.sessionMemberLoad(session);
	}

	@Override
	public void SystemNoticeAppend(AirNotice notice)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void SystemNoticeClean()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void SystemNoticeLoad(List<AirNotice> notices)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void SystemNoticeDelete(int noticeId)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void DbReportClean()
	{
		iReportDao.ReportClean();
	}

	@Override
	public void DbReportDelete(String code)
	{
		iReportDao.ReportDelete(code);
	}

	@Override
	public void DbReportLoad(List<AirReport> reports)
	{
		iReportDao.ReportLoad(reports);
	}

	@Override
	public void DbReportNew(AirReport report)
	{
		iReportDao.ReportNew(report);
	}

	@Override
	public void DbReportResultOk(String code)
	{
		iReportDao.ReportResultOk(code);
	}

	@Override
	public void TaskCaseLoad(List<AirTaskCase> tasks) {
		iTaskCaseDap.TaskCaseLoad(tasks);
	}

	@Override
	public void TaskCaseNew(AirTaskCase task) {
		iTaskCaseDap.TaskCaseNew(task);
	}

	@Override
	public void TaskCaseUpdate(AirTaskCase task) {
		iTaskCaseDap.TaskCaseUpdate(task);
	}

	@Override
	public void TaskCaseDelete(String taskId) {
		iTaskCaseDap.TaskCaseDelete(taskId);
	}

	@Override
	public void TaskCaseClean() {

	}

}
