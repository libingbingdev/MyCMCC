package com.cmccpoc.listener;

/**
 * 监听位置信息
 * @author Yao
 */
public interface OnMmiLocationListener
{
	/**
	 * 位置改变时
	 * @param isOk 状态
	 * @param id 循环标志
	 * @param type 位置类型
	 * @param latitude 维度
	 * @param longitude 精度
	 * @param altitude 高度
	 * @param speed 速度
	 * @param time 时间
	 */
	void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time);
	
	/**
	 * 位置改变时
	 * @param isOk 状态
	 * @param id 循环标志
	 * @param type 位置类型
	 * @param latitude 维度
	 * @param longitude 精度
	 * @param altitude 高度
	 * @param speed 速度
	 * @param time 时间
	 * @param address 地址
	 */
	void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time, String address);
}
