package com.cmccpoc.dao;

public final class DBDefine
{

	protected static final String DBNAME = "cmccpocdb";
	protected static final int DBVERSION = 11;

	protected static String Tables[] = { DBDefine.db_report, DBDefine.db_channel, DBDefine.db_message, DBDefine.db_session, DBDefine.db_session_member, DBDefine.db_task_case };

	/********************************
	 * Table name
	 ********************************/
	protected static final String db_report = "t_report";
	protected static final String db_channel = "t_channel";
	protected static final String db_message = "t_message";
	protected static final String db_session = "t_session";
	protected static final String db_session_member = "t_session_member";
	protected static final String db_task_case = "t_task_case";

	/********************************
	 * Table field
	 ********************************/

	protected static final class t_channel
	{
		protected static final String ID = "ID";
		protected static final String UID = "userid";
		protected static final String category = "category";
		protected static final String cid = "cid";
		protected static final String name = "name";
		protected static final String photoId = "photoId";
		protected static final String type = "type";
		protected static final String desc = "desc";
		protected static final String memberCount = "memberCount";
		protected static final String ownerId = "ownerId";
	}

	protected static final class t_message
	{
		protected static final String ID = "ID";
		protected static final String UID = "userid";
		protected static final String msgId = "msgId";
		protected static final String sid = "sid";
		protected static final String type = "type";
		protected static final String typeSub = "typeSub";
		protected static final String fromId = "fromId";
		protected static final String fromName = "fromName";
		protected static final String fromPhotoId = "fromPhotoId";
		protected static final String dtTime = "dtTime";
		protected static final String dtDate = "dtDate";
		protected static final String contentText = "contentText";
		protected static final String contentRes = "contentRes";
		protected static final String contentResLen = "contentResLen";
		protected static final String state = "state";
		protected static final String secret = "secret";
		protected static final String secretKey = "secretKey";
	}

	protected static final class t_report
	{
		protected static final String ID = "ID";
		protected static final String UID = "userid";
		protected static final String code = "code";
		protected static final String resPath = "resPath";
		protected static final String resSize = "resSize";
		protected static final String resFiles = "resFiles";
		protected static final String resFileMulti = "resFileMulti";
		protected static final String resContent = "resContent";
		protected static final String locLatitude = "locLatitude";
		protected static final String locLongitude = "locLongitude";
		protected static final String time = "time";
		protected static final String type = "type";
		protected static final String typeExt = "typeExt";
		protected static final String state = "state";
	}

	protected static final class t_session
	{
		protected static final String ID = "ID";
		protected static final String UID = "userid";
		protected static final String sid = "sid";
		protected static final String name = "name";
		protected static final String photoId = "photoId";
		protected static final String unreadCount = "unreadCount";
		protected static final String sOrder = "sOrder";
		protected static final String specialNumber = "specialNumber";
	}

	protected static final class t_session_member
	{
		protected static final String ID = "ID";
		protected static final String UID = "userid";
		protected static final String sid = "sid";
		protected static final String iId = "iId";
		protected static final String iName = "iName";
		protected static final String iPhotoId = "iPhotoId";
	}

	protected static final class t_task_case
	{
		protected static final String ID = "ID";
		protected static final String UID = "userid";
		protected static final String taskId = "taskId";
		protected static final String caseCode = "taskCode";		// 案件号
		protected static final String caseName = "taskName";		// 案件名称
		protected static final String carNo = "carNo";				// 车牌号
		protected static final String detail = "defail";
		protected static final String isLocal = "isLocal";
	}

	/********************************
	 * SQL create table
	 ********************************/

	private static final String CREATE = "CREATE TABLE IF NOT EXISTS ";

	protected static final String CREATE_T_CHANNEL = CREATE + db_channel + "(" + t_channel.ID + " integer PRIMARY KEY AUTOINCREMENT," + t_channel.UID + " integer,"
		+ t_channel.category + " integer," + t_channel.cid + " varchar(16)," + t_channel.name + " varchar(32)," + t_channel.photoId + " varchar(16)," + t_channel.type
		+ " integer," + t_channel.desc + " TEXT," + t_channel.memberCount + " integer," + t_channel.ownerId + " varchar(16)" + ")";

	protected static final String CREATE_T_MESSAGE = CREATE + db_message + "(" + t_message.ID + " integer PRIMARY KEY AUTOINCREMENT," + t_message.UID + " integer,"
		+ t_message.msgId + " varchar(16)," + t_message.sid + " varchar(16)," + t_message.fromId + " varchar(16)," + t_message.fromName + " varchar(32)," + t_message.fromPhotoId
		+ " varchar(16)," + t_message.type + " integer," + t_message.typeSub + " integer," + t_message.dtTime + " varchar(32)," + t_message.dtDate + " varchar(32),"
		+ t_message.state + " integer," + t_message.contentText + " TEXT," + t_message.contentRes + " TEXT," + t_message.contentResLen + " integer,"
		+ t_message.secret + " integer," + t_message.secretKey + " varchar(512)" + ")";

	protected static final String CREATE_T_REPORT = CREATE + db_report + "(" + t_report.ID + " integer PRIMARY KEY AUTOINCREMENT," + t_report.UID + " integer," + t_report.code
		+ " varchar(16)," + t_report.resFileMulti + " integer," + t_report.resFiles + " TEXT," + t_report.resPath + " TEXT," + t_report.resContent + " TEXT," + t_report.resSize + " integer," + t_report.locLatitude + " varchar(32),"
		+ t_report.locLongitude + " varchar(32)," + t_report.time + " varchar(64)," + t_report.type + " integer," + t_report.typeExt + " varchar(16)," + t_report.state
		+ " integer" + ")";

	protected static final String CREATE_T_SESSION = CREATE + db_session + "(" + t_session.ID + " integer PRIMARY KEY AUTOINCREMENT," + t_session.UID + " integer," + t_session.sid
		+ " varchar(16)," + t_session.name + " varchar(64)," + t_session.photoId + " varchar(16)," + t_session.unreadCount + " integer," + t_session.sOrder + " integer,"
		+ t_session.specialNumber + " integer" + ")";
	protected static final String CREATE_T_SESSION_MEMBER = CREATE + db_session_member + "(" + t_session_member.ID + " integer PRIMARY KEY AUTOINCREMENT," + t_session_member.UID
		+ " integer," + t_session_member.sid + " varchar(16)," + t_session_member.iId + " varchar(16)," + t_session_member.iName + " varchar(32)," + t_session_member.iPhotoId
		+ " varchar(16)" + ")";

	protected static final String CREATE_T_TASK_CASE = CREATE + db_task_case + "(" + t_task_case.ID + " integer PRIMARY KEY AUTOINCREMENT," + t_task_case.UID + " integer," + t_task_case.taskId
			+ " varchar(32)," + t_task_case.caseCode + " varchar(32)," + t_task_case.caseName + " varchar(64)," + t_task_case.carNo + " varchar(32)," + t_task_case.detail + " TEXT," + t_task_case.isLocal + " integer" + ")";

}
