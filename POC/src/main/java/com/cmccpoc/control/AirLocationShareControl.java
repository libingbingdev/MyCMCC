package com.cmccpoc.control;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnLocationShareListener;
import com.airtalkee.sdk.controller.LocationShareController;
import com.airtalkee.sdk.entity.AirLocationShare;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.listener.OnMmiLocationShareListener;
import com.cmccpoc.location.AirLocationImp;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.AirMmiTimer;
import com.cmccpoc.util.AirMmiTimerListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 Created by Yao on 2017/6/22. */

public class AirLocationShareControl extends BDAbstractLocationListener implements OnLocationShareListener, AirMmiTimerListener
{
    private final int LOCATION_SHARE_TIMEOUT = 30 * 1000;
    private final int LOCATION_SHARE_POINT_TIMEOUT = 3 * 1000;

    private static final int HANDLER_MSG_WHAT_SEND_POINT = 1;
    private static final int HANDLER_MSG_WHAT_RECEIVE_STATE = 2;
    private static final int HANDLER_MSG_WHAT_RECEIVE_POINT = 3;
    private static final int HANDLER_MSG_WHAT_SEND_START = 4;
    private static final int HANDLER_MSG_WHAT_SEND_STOP = 5;

    private static final String HANDLER_MSG_PARAM_IPOCID = "ipocid";
    private static final String HANDLER_MSG_PARAM_USERNAME = "userName";
    private static final String HANDLER_MSG_PARAM_LOCSTATE = "locState";

    public final static double CELL_ERROR = 4.9E-324;
    private double mLatitude = 0.0;
    private double mLongitude = 0.0;

    private static AirLocationShareControl mInstance;
    private OnMmiLocationShareListener onMmiLocationShareListener;
    private Map<AirSession, Boolean> startMap = new HashMap<AirSession, Boolean>();

    private LocationClient mClientCell = null;
    private LocationClientOption mClientCellOption = null;

    private boolean isTimerRunning = false;

    private AirLocationShareControl()
    {
        AirtalkeeMessage.getInstance().setOnLocationShareListener(this);
        initBDShareLocation();
        // registLocationShareTimer();
    }

    public static AirLocationShareControl getInstance()
    {
        if (mInstance == null)
            mInstance = new AirLocationShareControl();
        return mInstance;
    }

    public void cleanAllShare()
    {
        if (LocationShareController.getMaps() != null && LocationShareController.getMaps().size() > 0)
        {
            for (Map.Entry<String, Map<String, AirLocationShare>> entry : LocationShareController.getMaps().entrySet())
            {
                final String sessionCode = entry.getKey();
                Map<String, AirLocationShare> subMap = entry.getValue();
                if (subMap != null && subMap.size() > 0 && subMap.containsKey(AirtalkeeAccount.getInstance().getUser().getIpocId()))
                {
                    stopShare(AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode));
                }
            }
            LocationShareController.clearMap();
            startMap.clear();
        }
    }

    private void registLocationShareTimer()
    {
        if (AirServices.getInstance() != null && !isTimerRunning)
        {
            Log.i(AirLocationShareControl.class, "[LOCSHARE] registLocationShareTimer IN");
            AirMmiTimer.getInstance().TimerRegister(AirServices.getInstance(), this, false, true, 5000, true, null);
            isTimerRunning = true;
        }
    }

    private void unregistLocationShareTimer()
    {
        if (AirServices.getInstance() != null && isTimerRunning)
        {
            Log.i(AirLocationShareControl.class, "[LOCSHARE] unregistLocationShareTimer IN");
            AirMmiTimer.getInstance().TimerUnregister(AirServices.getInstance(), this);
            isTimerRunning = false;
        }
    }

    public void release()
    {
        // AirtalkeeMessage.getInstance().setOnLocationShareListener(null);
        unregistLocationShareTimer();
        doShareLocationStop();
        // mInstance = null;
    }

    public void setOnMmiLocationShareListener(OnMmiLocationShareListener l)
    {
        onMmiLocationShareListener = l;
    }

    public Map<String, AirLocationShare> getLocationShareMap(String sessionCode)
    {
        if (LocationShareController.getMaps() == null)
            return null;
        return LocationShareController.getMaps().get(sessionCode);
    }

    private void initBDShareLocation()
    {
        if ((mClientCell == null || mClientCellOption == null) && AirServices.getInstance() != null)
        {
            mClientCell = new LocationClient(AirServices.getInstance());
            mClientCellOption = new LocationClientOption();
            mClientCellOption.setOpenGps(true);
            mClientCellOption.setCoorType("bd09ll");
            mClientCellOption.setScanSpan(5 * LocationClientOption.MIN_SCAN_SPAN);
            mClientCellOption.setTimeOut(LOCATION_SHARE_POINT_TIMEOUT);
            mClientCell.setLocOption(mClientCellOption);
            // mClientCell.requestLocation();
        }
    }

    private void doShareLocationStart()
    {
        if (!mClientCell.isStarted())
        {
            mClientCell.registerLocationListener(this);
            mClientCell.start();
        }
    }

    private void doShareLocationStop()
    {
        if (mClientCell.isStarted())
        {
            mClientCell.unRegisterLocationListener(this);
            mClientCell.stop();
        }
    }

    public boolean startShare(final AirSession session)
    {
        boolean isSendStart = false;
        if (startMap.get(session) == null)
        {
            startMap.put(session, true);
            AirtalkeeMessage.getInstance().LocationShareStart(session);
            isSendStart = true;
        }
        else if (!startMap.get(session))
        {
            startMap.put(session, true);
            AirtalkeeMessage.getInstance().LocationShareStart(session);
            isSendStart = true;
        }
        return isSendStart;
    }

    @Override
    public void onLocationShareStart(Map<String, Map<String, AirLocationShare>> maps)
    {
        registLocationShareTimer();
        Log.i(AirLocationImp.class, "[LOCATIONSHARE] onLocationShareStart!");
        mHandler.sendEmptyMessage(HANDLER_MSG_WHAT_SEND_START);
    }

    public void stopShare(AirSession session)
    {
        if (startMap != null && startMap.get(session) != null)
        {
            startMap.remove(session);
            AirtalkeeMessage.getInstance().LocationShareStop(session);
        }
    }

    @Override
    public void onLocationShareStop(Map<String, Map<String, AirLocationShare>> maps, String sessionCode)
    {
        Message msg = new Message();
        msg.what = HANDLER_MSG_WHAT_SEND_STOP;
        msg.obj = sessionCode;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onLocationSharePoint(Map<String, Map<String, AirLocationShare>> maps)
    {
        registLocationShareTimer();
        mHandler.sendEmptyMessage(HANDLER_MSG_WHAT_RECEIVE_POINT);
    }

    @Override
    public void onLocationStateReceive(Map<String, Map<String, AirLocationShare>> maps, int locState, String ipocid, String userName)
    {
        registLocationShareTimer();
        Message msg = new Message();
        msg.what = HANDLER_MSG_WHAT_RECEIVE_STATE;
        Bundle bundle = new Bundle();
        bundle.putInt(HANDLER_MSG_PARAM_LOCSTATE, locState);
        bundle.putString(HANDLER_MSG_PARAM_IPOCID, ipocid);
        bundle.putString(HANDLER_MSG_PARAM_USERNAME, userName);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    @Override
    public void onMmiTimer(Context context, Object userData)
    {
        Log.i(AirLocationShareControl.class, "[LOCSHARE] onMmiTimer IN");
        Map<String, Map<String, AirLocationShare>> maps = LocationShareController.getMaps();
        if (maps != null && maps.size() > 0 && AirtalkeeAccount.getInstance().isEngineRunning())
        {
            final AirSession currentSession = AirSessionControl.getInstance().getCurrentSession();
            for (Map.Entry<String, Map<String, AirLocationShare>> entry : maps.entrySet())
            {
                final String sessionCode = entry.getKey();
                Map<String, AirLocationShare> subMap = entry.getValue();
                if (subMap != null && subMap.size() > 0)
                {
                    List<String> removeKey = new ArrayList<String>();
                    for (Map.Entry<String, AirLocationShare> subEntry : subMap.entrySet())
                    {
                        final String ipocid = subEntry.getKey();
                        if (ipocid.equals(AirtalkeeAccount.getInstance().getUser().getIpocId()))
                        {

                        }
                        else
                        {
                            long tsNow = System.currentTimeMillis();
                            if (tsNow - subEntry.getValue().getTs() > LOCATION_SHARE_TIMEOUT)
                                removeKey.add(ipocid);
                        }
                    }
                    if (removeKey.size() > 0)
                    {
                        for (int i = 0; i < removeKey.size(); i++)
                        {
                            LocationShareController.mapMemberRemove(sessionCode, removeKey.get(i));
                        }
                        /*if (HomeActivity.getInstance() != null)
                            HomeActivity.getInstance().refreshLocationShareView();*/
                    }
                    /*if (onMmiLocationShareListener != null && currentSession != null && currentSession.getSessionCode().equals(sessionCode))
                        onMmiLocationShareListener.onMmiLocationShareMemberClean(subMap, removeKey);*/
                }
            }
        }
        else
            release();
    }

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case HANDLER_MSG_WHAT_SEND_POINT:
                {
                    if (startMap.size() > 0)
                    {
                        for (Map.Entry<AirSession, Boolean> entry : startMap.entrySet())
                        {
                            final AirSession session = entry.getKey();
                            AirtalkeeMessage.getInstance().LocationSharePoiSend(session, mLatitude, mLongitude);
                            if (onMmiLocationShareListener != null)
                                onMmiLocationShareListener.onMmiLocationShareTimer(mLatitude, mLongitude);
                        }
                    }
                    break;
                }
                case HANDLER_MSG_WHAT_SEND_STOP:
                {
                    if (startMap.size() == 0)
                        doShareLocationStop();
                    /*if (HomeActivity.getInstance() != null)
                        HomeActivity.getInstance().refreshLocationShareView();*/
                    String sessionCode = msg.obj.toString();
                    if (onMmiLocationShareListener != null)
                        onMmiLocationShareListener.onMmiLocationShareStop(sessionCode);
                    break;
                }
                case HANDLER_MSG_WHAT_SEND_START:
                {
                    if (startMap.size() > 0)
                        doShareLocationStart();
                    if (onMmiLocationShareListener != null)
                    {
                        final AirSession session = AirSessionControl.getInstance().getCurrentSession();
                        if (session != null)
                        {
                            for (Map.Entry<String, Map<String, AirLocationShare>> entry : LocationShareController.getMaps().entrySet())
                            {
                                String sessionCode = entry.getKey();
                                if (sessionCode.equals(session.getSessionCode()))
                                {
                                    onMmiLocationShareListener.onMmiLocationShareStart(entry.getValue());
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
                case HANDLER_MSG_WHAT_RECEIVE_STATE:
                {
                   /* if (HomeActivity.getInstance() != null)
                        HomeActivity.getInstance().refreshLocationShareView();*/
                    if (onMmiLocationShareListener != null)
                    {
                        final AirSession session = AirSessionControl.getInstance().getCurrentSession();
                        if (session != null)
                        {
                            for (Map.Entry<String, Map<String, AirLocationShare>> entry : LocationShareController.getMaps().entrySet())
                            {
                                String sessionCode = entry.getKey();
                                if (sessionCode.equals(session.getSessionCode()))
                                {
                                    Bundle bundle = msg.getData();
                                    if (bundle != null)
                                        onMmiLocationShareListener.onMmiLocationStateReceive(bundle.getInt(HANDLER_MSG_PARAM_LOCSTATE), bundle.getString(HANDLER_MSG_PARAM_IPOCID), bundle.getString(HANDLER_MSG_PARAM_IPOCID));
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
                case HANDLER_MSG_WHAT_RECEIVE_POINT:
                {
                    /*if (HomeActivity.getInstance() != null)
                        HomeActivity.getInstance().refreshLocationShareView();*/
                    if (onMmiLocationShareListener != null)
                    {
                        final AirSession session = AirSessionControl.getInstance().getCurrentSession();
                        if (session != null)
                        {
                            for (Map.Entry<String, Map<String, AirLocationShare>> entry : LocationShareController.getMaps().entrySet())
                            {
                                String sessionCode = entry.getKey();
                                if (sessionCode.equals(session.getSessionCode()))
                                {
                                    onMmiLocationShareListener.onMmiLocationSharePoint(entry.getValue());
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    };

    @Override
    public void onReceiveLocation(BDLocation location)
    {
        Log.i(AirLocationShareControl.class, "[LOCSHARE]  X:" + location.getLatitude() + " Y:" + location.getLongitude());
        Message msg = new Message();
        msg.what = HANDLER_MSG_WHAT_SEND_POINT;
        if (CELL_ERROR != location.getLatitude())
            mLatitude = location.getLatitude();
        if (CELL_ERROR != location.getLongitude())
            mLongitude = location.getLongitude();
        mHandler.sendMessage(msg);
    }

}
