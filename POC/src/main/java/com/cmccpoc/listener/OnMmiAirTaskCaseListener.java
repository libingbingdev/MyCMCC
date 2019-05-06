package com.cmccpoc.listener;

import com.cmccpoc.entity.AirTaskCase;

import java.util.List;


/**
 * 监听会话
 * @author Yao
 */
public interface OnMmiAirTaskCaseListener
{
	/**
	 * 列表获取
	 */
	public void onTaskCaseListGet(boolean isOk, List<AirTaskCase> tasks);

	/**
	 * 任务创建结果
	 */
	public void onTaskCaseCreated(boolean isOk, AirTaskCase task);

	/**
	 * 任务更新结果
	 */
	public void onTaskCaseUpdated(boolean isOk, AirTaskCase task);

	/**
	 * 任务删除结果
	 */
	public void onTaskCaseDeleted(boolean isOk, AirTaskCase task);

}
