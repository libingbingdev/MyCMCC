package com.cmccpoc.listener;

import com.airtalkee.sdk.entity.AirVideoShare;

public interface OnMmiVideoListener
{
	public void onVideoRecorderStart(int sessionId, int result);

	public void onVideoRecorderStop(int sessionId);

	public boolean onVideoRealtimeShareStart(AirVideoShare videoShare);

	public void onVideoRealtimeShareStop(AirVideoShare videoShare);

}
