package com.cmccpoc.listener;

/**
 * 监听用户登录状态
 * @author Yao
 */
public interface OnMmiAccountListener
{
	/**
	 * 登录时
	 * @param result 登录状态
	 */
    public void onMmiHeartbeatLogin(int result);

    /**
     * 登出时
     */
	public void onMmiHeartbeatLogout();

	/**
	 * 登录异常
	 * @param result 异常结果
	 */
	public void onMmiHeartbeatException(int result);
}
