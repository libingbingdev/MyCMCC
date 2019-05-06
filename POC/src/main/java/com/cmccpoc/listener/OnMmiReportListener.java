package com.cmccpoc.listener;


/**
 * 监听上报记录
 * @author Yao
 */
public interface OnMmiReportListener
{
	/**
	 * 上报记录刷新时
	 */
	public void onMmiReportResourceListRefresh();
	
	/**
	 * 上报资源被删除时
	 */
	public void onMmiReportDel();
	
	/**
	 * 上报进度
	 * @param progress 进度值 0~100
	 */
	public void onMmiReportProgress(int progress);
}
