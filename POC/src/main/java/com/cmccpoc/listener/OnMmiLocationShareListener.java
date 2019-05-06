package com.cmccpoc.listener;

import com.airtalkee.sdk.entity.AirLocationShare;

import java.util.List;
import java.util.Map;

/**
 Created by Yao on 2017/6/22. */

public interface OnMmiLocationShareListener
{
    void onMmiLocationShareStart(Map<String, AirLocationShare> maps);

    void onMmiLocationShareStop(String sessionCode);

    void onMmiLocationSharePoint(Map<String, AirLocationShare> maps);

    void onMmiLocationShareMemberClean(Map<String, AirLocationShare> maps, List<String> removeKey);

    void onMmiLocationStateReceive(int locState, String ipocid, String userName);

    void onMmiLocationShareTimer(double latitude, double longitude);
}
