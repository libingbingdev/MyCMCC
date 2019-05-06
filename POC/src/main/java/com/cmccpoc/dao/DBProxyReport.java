package com.cmccpoc.dao;

import java.util.List;
import com.cmccpoc.entity.AirReport;

/**
 * 上报资源数据库操作接口
 * @author Yao
 */
public interface DBProxyReport
{
	/**
	 * 加载上报记录
	 * @param reports 上报资源列表
	 */
	public void DbReportLoad(List<AirReport> reports);

	/**
	 * 新上报记录
	 * @param report 上报Entity
	 */
	public void DbReportNew(AirReport report);

	/**
	 * 新上报记录的状态
	 * @param code 上报资源code
	 */
	public void DbReportResultOk(String code);

	/**
	 * 删除一条上报记录
	 * @param code 上报资源code
	 */
	public void DbReportDelete(String code);

	/**
	 * 清楚所有上报记录
	 */
	public void DbReportClean();
}
