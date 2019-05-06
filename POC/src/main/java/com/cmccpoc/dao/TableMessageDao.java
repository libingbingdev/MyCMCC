package com.cmccpoc.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息操作类
 * @author Yao
 */
public class TableMessageDao
{
	private static final TableMessageDao instance = new TableMessageDao();
	private TableMessageDao()
	{};

	private static DBHelp dbHelp;

	protected static TableMessageDao getInstance(DBHelp db)
	{
		dbHelp = db;
		return instance;
	}

	/********************************
	 * Message List
	 ********************************/
	
	/**
	 * 添加一条消息
	 * @param sid 消息sessionId
	 * @param msg 消息Entity
	 */
	protected void messageAppend(String sid, final AirMessage msg)
	{
		ContentValues cv = new ContentValues();
		cv.put(DBDefine.t_message.UID, dbHelp.getUid());
		cv.put(DBDefine.t_message.sid, Utils.isEmpty(sid) ? "NONE" : sid);
		cv.put(DBDefine.t_message.msgId, msg.getMessageCode());
		cv.put(DBDefine.t_message.type, msg.getType());
		cv.put(DBDefine.t_message.typeSub, msg.getRecordType());
		cv.put(DBDefine.t_message.dtDate, msg.getDate());
		cv.put(DBDefine.t_message.dtTime, msg.getTime());
		cv.put(DBDefine.t_message.fromId, msg.getIpocidFrom());
		cv.put(DBDefine.t_message.fromName, msg.getInameFrom());
		cv.put(DBDefine.t_message.fromPhotoId, msg.getIphotoIdFrom());
		cv.put(DBDefine.t_message.contentText, msg.getBody() != null ? msg.getBody().replace("%", "％") : "");
		cv.put(DBDefine.t_message.contentRes, msg.getImageUri());
		cv.put(DBDefine.t_message.contentResLen, msg.getImageLength());
		cv.put(DBDefine.t_message.state, msg.getState());
		cv.put(DBDefine.t_message.secret, msg.getSecretType());
		cv.put(DBDefine.t_message.state, msg.getSecretKey());
		dbHelp.insert(DBDefine.db_message, cv);
	}

	/**
	 * 加载消息列表
	 * @param session 会话Entity
	 */
	protected void messageLoad(AirSession session)
	{
		SQLiteDatabase db = dbHelp.DatabaseReadableGet();
		if (db != null)
		{
			String sql = "SELECT * FROM " + DBDefine.db_message + " WHERE " + DBDefine.t_message.UID + "=" + dbHelp.getUid() + " AND " + DBDefine.t_message.sid + " ='"
				+ session.getSessionCode() + "'";
			session.getMessages().clear();
			try
			{
				Cursor c = db.rawQuery(sql, null);
				while (c.moveToNext())
				{
					AirMessage msg = new AirMessage();
					msg.setMessageCode(c.getString(c.getColumnIndex(DBDefine.t_message.msgId)));
					msg.setSessionCode(c.getString(c.getColumnIndex(DBDefine.t_message.sid)));
					msg.setType(c.getInt(c.getColumnIndex(DBDefine.t_message.type)));
					msg.setRecordType(c.getInt(c.getColumnIndex(DBDefine.t_message.typeSub)));
					msg.setIpocidFrom(c.getString(c.getColumnIndex(DBDefine.t_message.fromId)));
					msg.setInameFrom(c.getString(c.getColumnIndex(DBDefine.t_message.fromName)));
					msg.setIphotoIdFrom(c.getString(c.getColumnIndex(DBDefine.t_message.fromPhotoId)));
					msg.setTime(c.getString(c.getColumnIndex(DBDefine.t_message.dtTime)));
					msg.setDate(c.getString(c.getColumnIndex(DBDefine.t_message.dtDate)));
					msg.setBody(c.getString(c.getColumnIndex(DBDefine.t_message.contentText)));
					msg.setImageUri(c.getString(c.getColumnIndex(DBDefine.t_message.contentRes)));
					msg.setImageLength(c.getInt(c.getColumnIndex(DBDefine.t_message.contentResLen)));
					if (msg.getBody() != null)
						msg.setBody(msg.getBody().replace("％", "%"));
					int state = c.getInt(c.getColumnIndex(DBDefine.t_message.state));
					msg.setState(state == AirMessage.STATE_RESULT_OK ? AirMessage.STATE_RESULT_OK : AirMessage.STATE_RESULT_FAIL);
					msg.setSecretType(c.getInt(c.getColumnIndex(DBDefine.t_message.secret)));
					msg.setSecretKey(c.getString(c.getColumnIndex(DBDefine.t_message.secretKey)));
					session.getMessages().add(msg);
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

	private boolean loading = false;

	/**
	 * 加载消息数据
	 * @param sid 消息sessionId
	 * @param msgPosition 消息列表的第msgPosition条
	 * @param msgCount 取列表的总数
	 * @return
	 */
	protected List<AirMessage> MessageDbLoad(String sid, int msgPosition, int msgCount)
	{
		boolean hasException = true;
		if (loading)
		{
			Log.e(DBHelp.class, "[ERROR] loading is true");
		}
		loading = true;
		List<AirMessage> messages = new ArrayList<AirMessage>();
		SQLiteDatabase db = null;
		String sql = "";
		try
		{
			db = dbHelp.DatabaseReadableGet();
			if (db != null)
			{
				sql = "SELECT * FROM " + DBDefine.db_message + " WHERE " + DBDefine.t_message.UID + "=" + dbHelp.getUid() + " AND " + DBDefine.t_message.sid + " ='" + sid + "'"
					+ " ORDER BY " + DBDefine.t_message.ID + " DESC" + " LIMIT " + msgPosition + ", " + msgCount;
				Cursor c = db.rawQuery(sql, null);
				boolean isok = c.moveToLast();
				do
				{
					if (isok)
					{
						AirMessage msg = new AirMessage();
						msg.setMessageCode(c.getString(c.getColumnIndex(DBDefine.t_message.msgId)));
						msg.setSessionCode(c.getString(c.getColumnIndex(DBDefine.t_message.sid)));
						msg.setType(c.getInt(c.getColumnIndex(DBDefine.t_message.type)));
						msg.setRecordType(c.getInt(c.getColumnIndex(DBDefine.t_message.typeSub)));
						msg.setIpocidFrom(c.getString(c.getColumnIndex(DBDefine.t_message.fromId)));
						msg.setInameFrom(c.getString(c.getColumnIndex(DBDefine.t_message.fromName)));
						msg.setIphotoIdFrom(c.getString(c.getColumnIndex(DBDefine.t_message.fromPhotoId)));
						msg.setTime(c.getString(c.getColumnIndex(DBDefine.t_message.dtTime)));
						msg.setDate(c.getString(c.getColumnIndex(DBDefine.t_message.dtDate)));
						msg.setBody(c.getString(c.getColumnIndex(DBDefine.t_message.contentText)));
						msg.setImageUri(c.getString(c.getColumnIndex(DBDefine.t_message.contentRes)));
						msg.setImageLength(c.getInt(c.getColumnIndex(DBDefine.t_message.contentResLen)));
						if (msg.getBody() != null)
							msg.setBody(msg.getBody().replace("％", "%"));
						int state = c.getInt(c.getColumnIndex(DBDefine.t_message.state));
						msg.setState(state == AirMessage.STATE_RESULT_OK ? AirMessage.STATE_RESULT_OK : AirMessage.STATE_RESULT_FAIL);
						msg.setSecretType(c.getInt(c.getColumnIndex(DBDefine.t_message.secret)));
						msg.setSecretKey(c.getString(c.getColumnIndex(DBDefine.t_message.secretKey)));
						messages.add(msg);
					}
				}
				while (c.moveToPrevious());
				c.close();
				dbHelp.DatabaseReadableClose(db);
				hasException = false;
			}
		}
		catch (Exception e)
		{
			Log.e(DBHelp.class, "[SQL EXCEPTION] " + sql + " -> " + e.getMessage());
			if (db != null && db.isOpen())
				dbHelp.DatabaseReadableClose(db);
			hasException = false;
		}
		finally
		{
			if (hasException)
			{
				Log.e(DBHelp.class, "[SQL EXCEPTION] MessageDbLoad finally error!!!");
			}
		}
		loading = false;
		return messages;
	}

	/**
	 * 消息更新
	 * @param msg 消息Entity
	 */
	protected void messageUpdate(final AirMessage msg)
	{
		String sql = String.format("UPDATE " + DBDefine.db_message + " SET " + DBDefine.t_message.sid + " = '%s', " + DBDefine.t_message.dtTime + " = '%s', "
			+ DBDefine.t_message.dtDate + " = '%s', " + DBDefine.t_message.contentText + " = '%s', " + DBDefine.t_message.contentRes + " = '%s', "
			+ DBDefine.t_message.contentResLen + " = %d, " + DBDefine.t_message.typeSub + " = %d, " + DBDefine.t_message.state + " = %d, " + DBDefine.t_message.secret + " = %d, " + DBDefine.t_message.secretKey + " = '%s' " + "WHERE " + DBDefine.t_message.UID
			+ "=%s AND " + DBDefine.t_message.msgId + " = '%s'", msg.getSessionCode(), msg.getTime(), msg.getDate(), msg.getBody() != null ? msg.getBody().replace("%", "％") : "", msg.getImageUri(), msg.getImageLength(),
			msg.getRecordType(), msg.getState(), msg.getSecretType(), msg.getSecretKey(), dbHelp.getUid(), msg.getMessageCode());
		dbHelp.update(sql);
	}

	/**
	 获取最新limit条之外的数据
	 @param sid
	 @param limit
	 @return
	 */
	protected List<AirMessage> messageQueryLast(String sid, int limit)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(DBDefine.db_message);
		sb.append(" WHERE ");
		sb.append(DBDefine.t_message.ID + " < ");
		sb.append("(SELECT ");
		sb.append(DBDefine.t_message.ID);
		sb.append(" FROM " + DBDefine.db_message);
		sb.append(" WHERE ");
		sb.append(DBDefine.t_message.type + " = " + AirMessage.TYPE_RECORD + " and " + DBDefine.t_message.typeSub + " = " + AirMessage.RECORD_TYPE_PTT + " AND " + DBDefine.t_message.sid + " = '" + sid + "' ");
		sb.append("ORDER BY ID DESC ");
		sb.append("LIMIT " + (limit - 1) + ", 1) AND ");
		sb.append(DBDefine.t_message.type + " = " + AirMessage.TYPE_RECORD + " and " + DBDefine.t_message.typeSub + " = " + AirMessage.RECORD_TYPE_PTT + " AND " + DBDefine.t_message.sid + " = '" + sid + "' ");
		Log.i(TableMessageDao.class, "[DB][MSG] SQLQUERY : " + sb.toString());
		List<AirMessage> messages = new ArrayList<AirMessage>();
		try
		{
			SQLiteDatabase db = dbHelp.DatabaseReadableGet();
			if (db != null)
			{
				Cursor c = db.rawQuery(sb.toString(), null);
				boolean isok = c.moveToLast();
				Log.i(TableMessageDao.class, "[DB][MSG] isok : " + isok);
				do
				{
					if (isok)
					{
						AirMessage msg = new AirMessage();
						msg.setMessageCode(c.getString(c.getColumnIndex(DBDefine.t_message.msgId)));
						msg.setSessionCode(c.getString(c.getColumnIndex(DBDefine.t_message.sid)));
						msg.setType(c.getInt(c.getColumnIndex(DBDefine.t_message.type)));
						msg.setRecordType(c.getInt(c.getColumnIndex(DBDefine.t_message.typeSub)));
						msg.setIpocidFrom(c.getString(c.getColumnIndex(DBDefine.t_message.fromId)));
						msg.setInameFrom(c.getString(c.getColumnIndex(DBDefine.t_message.fromName)));
						msg.setIphotoIdFrom(c.getString(c.getColumnIndex(DBDefine.t_message.fromPhotoId)));
						msg.setTime(c.getString(c.getColumnIndex(DBDefine.t_message.dtTime)));
						msg.setDate(c.getString(c.getColumnIndex(DBDefine.t_message.dtDate)));
						msg.setBody(c.getString(c.getColumnIndex(DBDefine.t_message.contentText)));
						msg.setImageUri(c.getString(c.getColumnIndex(DBDefine.t_message.contentRes)));
						msg.setImageLength(c.getInt(c.getColumnIndex(DBDefine.t_message.contentResLen)));
						if (msg.getBody() != null)
							msg.setBody(msg.getBody().replace("％", "%"));
						int state = c.getInt(c.getColumnIndex(DBDefine.t_message.state));
						msg.setState(state == AirMessage.STATE_RESULT_OK ? AirMessage.STATE_RESULT_OK : AirMessage.STATE_RESULT_FAIL);
						msg.setSecretType(c.getInt(c.getColumnIndex(DBDefine.t_message.secret)));
						msg.setSecretKey(c.getString(c.getColumnIndex(DBDefine.t_message.secretKey)));
						messages.add(msg);
					}
				}
				while (c.moveToPrevious());
				c.close();
				dbHelp.DatabaseReadableClose(db);
			}
		}
		catch (Exception e)
		{
			Log.e(DBHelp.class, "[SQL EXCEPTION] " + sb.toString() + " -> " + e.getMessage());
		}
		return messages;
	}

	/**
	 * 消息删除
	 * @param sid 消息sessionId
	 * @param msgId 消息Id
	 */
	protected void messageDelete(String sid, String msgId)
	{
		String sql = "DELETE FROM " + DBDefine.db_message + " WHERE " + DBDefine.t_message.UID + "=" + dbHelp.getUid() + " AND " + DBDefine.t_message.sid + " = '" + sid + "' AND "
			+ DBDefine.t_message.msgId + " = '" + msgId + "'";
		dbHelp.del(sql);
	}

	/**
	 * 清除某会话下的消息
	 * @param sid sessionId
	 */
	protected void messageClean(String sid)
	{
		String sql = "DELETE FROM " + DBDefine.db_message + " WHERE " + DBDefine.t_message.UID + "=" + dbHelp.getUid() + " AND " + DBDefine.t_message.sid + " = '" + sid + "'";
		dbHelp.del(sql);
	}

	/**
	 * 清除所有会话消息
	 */
	protected void messageCleanAll()
	{
		String sql = "DELETE FROM " + DBDefine.db_message + " WHERE " + DBDefine.t_message.UID + "=" + dbHelp.getUid();
		dbHelp.del(sql);
	}

}
