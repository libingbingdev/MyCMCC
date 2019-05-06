package com.cmccpoc.dao;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.util.Log;

/**
 * 频道数据操作类
 * @author Yao
 */
public class TableChannelDao
{
	private static DBHelp dbHelp;
	private static final TableChannelDao instance = new TableChannelDao();

	protected static TableChannelDao getInstance(DBHelp db)
	{
		dbHelp = db;
		return instance;
	}

	/********************************
	 * Channel
	 ********************************/

	/**
	 * 清除所有频道
	 */
	protected void channelClean()
	{
		String sql = null;
		sql = "DELETE FROM " + DBDefine.db_channel + " WHERE " + DBDefine.t_channel.UID + " = " + dbHelp.getUid();
		dbHelp.del(sql);
	}

	/**
	 * 添加一个频道
	 * @param channel 频道Entity
	 */
	protected void channelAppend(final AirChannel channel)
	{
		ContentValues cv = channelAppendBuild(channel);
		dbHelp.insert(DBDefine.db_channel, cv);
	}

	/**
	 * 为频道属性赋值
	 * @param channel 频道Entity
	 * @return ContentValues
	 */
	protected ContentValues channelAppendBuild(AirChannel channel)
	{
		ContentValues cv = new ContentValues();
		cv.put(DBDefine.t_channel.UID, dbHelp.getUid());
		cv.put(DBDefine.t_channel.cid, channel.getId());
		cv.put(DBDefine.t_channel.name, channel.getDisplayName());
		cv.put(DBDefine.t_channel.category, 0);
		cv.put(DBDefine.t_channel.photoId, channel.getPhotoId());
		cv.put(DBDefine.t_channel.desc, channel.getDescription());
		cv.put(DBDefine.t_channel.type, channel.getRoomType());
		cv.put(DBDefine.t_channel.memberCount, channel.getMemberCount());
		cv.put(DBDefine.t_channel.ownerId, "");
		return cv;
	}

	/**
	 * 频道保存
	 * @param channels 频道列表
	 */
	protected void channelSave(List<AirChannel> channels)
	{
		String sql = "DELETE FROM " + DBDefine.db_channel + " WHERE " + DBDefine.t_channel.UID + "=" + dbHelp.getUid();
		dbHelp.del(sql);
		List<ContentValues> cvs = new ArrayList<ContentValues>();
		for (AirChannel ch : channels)
		{
			ContentValues cv = channelAppendBuild(ch);
			cvs.add(cv);
		}
		dbHelp.insert(DBDefine.db_channel, cvs);
	}

	/**
	 * 更新频道
	 * @param channel 频道Entity
	 */
	protected void channelUpdate(final AirChannel channel)
	{
		String sql = String.format("UPDATE " + DBDefine.db_channel + " SET " + DBDefine.t_channel.name + " = '%s', " + DBDefine.t_channel.photoId + " = '%s', "
			+ DBDefine.t_channel.desc + " = '%s', " + DBDefine.t_channel.ownerId + " = '%s', " + DBDefine.t_channel.type + " = %d, " + DBDefine.t_channel.memberCount + " = %d, "
			+ DBDefine.t_channel.category + " = %d " + "WHERE " + DBDefine.t_channel.UID + " = %s AND " + DBDefine.t_channel.cid + " = '%s'", channel.getDisplayName(),
			channel.getPhotoId(), channel.getDescription(), "", channel.getRoomType(), channel.getMemberCount(), 0, dbHelp.getUid(), channel.getId());
		dbHelp.update(sql);
	}

	/**
	 * 频道删除
	 * @param channelId 频道Id
	 */
	protected void channelDelete(String channelId)
	{
		String sql = null;
		sql = "DELETE FROM " + DBDefine.db_channel + " WHERE " + DBDefine.t_channel.cid + " = '" + channelId + "' AND " + DBDefine.t_channel.UID + "=" + dbHelp.getUid();
		dbHelp.del(sql);
	}

	/**
	 * 加载频道列表
	 * @return 频道列表
	 */
	protected List<AirChannel> channelLoad()
	{
		List<AirChannel> channels = new ArrayList<AirChannel>();
		SQLiteDatabase db = dbHelp.DatabaseReadableGet();
		if (db != null)
		{
			String sql = "SELECT * FROM " + DBDefine.db_channel + " WHERE " + DBDefine.t_channel.UID + "=" + dbHelp.getUid();
			try
			{
				Cursor c = db.rawQuery(sql, null);
				while (c.moveToNext())
				{
					AirChannel ch = new AirChannel();
					ch.setId(c.getString(c.getColumnIndex(DBDefine.t_channel.cid)));
					ch.setDisplayName(c.getString(c.getColumnIndex(DBDefine.t_channel.name)));
					ch.setPhotoId(c.getString(c.getColumnIndex(DBDefine.t_channel.photoId)));
					ch.setRoomType(c.getInt(c.getColumnIndex(DBDefine.t_channel.type)));
					ch.setDescription(c.getString(c.getColumnIndex(DBDefine.t_channel.desc)));
					ch.setMemberCount(c.getInt(c.getColumnIndex(DBDefine.t_channel.memberCount)));
					channels.add(ch);
				}
				c.close();
			}
			catch (Exception e)
			{
				Log.e(DBHelp.class, "[SQL EXCEPTION] " + sql + " -> " + e.getMessage());
			}
			dbHelp.DatabaseReadableClose(db);
		}
		return channels;
	}

	/**
	 * 加载频道
	 * @param channels 频道Entity
	 */
	protected void channelLoad(List<AirChannel> channels)
	{
		SQLiteDatabase db = dbHelp.DatabaseReadableGet();
		if (db != null)
		{
			String sql = "SELECT * FROM " + DBDefine.db_channel + " WHERE " + DBDefine.t_channel.UID + "=" + dbHelp.getUid();
			try
			{
				Cursor c = db.rawQuery(sql, null);
				channels.clear();
				while (c.moveToNext())
				{
					AirChannel ch = new AirChannel();
					ch.setId(c.getString(c.getColumnIndex(DBDefine.t_channel.cid)));
					ch.setDisplayName(c.getString(c.getColumnIndex(DBDefine.t_channel.name)));
					ch.setPhotoId(c.getString(c.getColumnIndex(DBDefine.t_channel.photoId)));
					ch.setRoomType(c.getInt(c.getColumnIndex(DBDefine.t_channel.type)));
					ch.setDescription(c.getString(c.getColumnIndex(DBDefine.t_channel.desc)));
					ch.setMemberCount(c.getInt(c.getColumnIndex(DBDefine.t_channel.memberCount)));
					channels.add(ch);
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

}
