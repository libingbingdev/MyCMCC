package com.cmccpoc.dao;

import com.cmccpoc.entity.AirTaskCase;

import java.util.List;

/**
 * 上报资源数据库操作接口
 * @author Yao
 */
public interface DBProxyTaskCase
{
	public void TaskCaseLoad(List<AirTaskCase> tasks);
	public void TaskCaseNew(AirTaskCase task);
	public void TaskCaseUpdate(AirTaskCase task);
	public void TaskCaseDelete(String taskId);
	public void TaskCaseClean();
}
