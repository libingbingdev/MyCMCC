package com.cmccpoc.location;

/**
 * 监听地图位置信息
 * @author Yao
 *
 */
public interface OnMapListener
{
	/**
	 * 获取到地图位置
	 * @param isOk 获取状态
	 * @param id 标识
	 * @param type 位置信息类型
	 * @param isFinal 是否为最后一次
	 * @param latitude 精度
	 * @param longitude 维度
	 * @param altitude 海拔
	 * @param direction 方向
	 * @param speed 速度
	 * @param time 时间
	 * @param address 地址
	 */
	void OnMapLocation(boolean isOk, int id, int type, boolean isFinal, double latitude, double longitude, double altitude, float direction, float speed, String time, String address);
}
