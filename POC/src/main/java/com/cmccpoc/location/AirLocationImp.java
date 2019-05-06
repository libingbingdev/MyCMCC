package com.cmccpoc.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.airtalkee.sdk.util.Log;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.cmccpoc.receiver.ReceiverConnectionChange;
import com.cmccpoc.util.AirMmiTimer;
import com.cmccpoc.util.AirMmiTimerListener;
import com.cmccpoc.util.Util;

/**
 获取位置信息
 @author Yao */
public class AirLocationImp
{

    public final static int LOCATION_TYPE_GPS = 0;
    public final static int LOCATION_TYPE_CELL_BAIDU = 2;

    /**
     计时器参数
     */
    public class TimerParam
    {
        public int id = 0;
        public OnMapListener listener = null;
        public int type = LOCATION_TYPE_GPS;
        public int timeoutSeconds = 0;
        public Context context = null;
        public LocationListener listenerGps = null;
        public BDLocationListener listenerCellBaidu = null;
    }

    public final static double CELL_ERROR = 4.9E-324;

    private LocationClient mClientCell = null;
    private LocationClientOption mClientCellOption = null;

    private boolean mClientCellGetting = false;

    private static int mType = LOCATION_TYPE_GPS;
    private static double mLatitude = 0;
    private static double mLongitude = 0;
    private static double mAltitude = 0;
    private static float mDirection = 0;
    private static float mSpeed = 0;
    private static String mTime = "";
    private static String mAddress = "";
    private boolean mGpsSuccess=false;

    /**
     获取位置
     @param actionId Id
     @param timeoutSeconds 超时时间
     */
    public void LocationGet(final Context context, final OnMapListener listener, final int actionId, final int timeoutSeconds, boolean isGPS)
    {
        android.util.Log.d("zlmg","LocationGet");
        LocationTerminate(context);
        if (AirLocation.gAirmLocMan == null)
            AirLocation.gAirmLocMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (AirLocation.gAirmLocMan.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) && isGPS)
        {
            android.util.Log.d("zlmg","doGpsLocation");
            doGpsLocation(actionId, timeoutSeconds, context, listener);
        }
        else
        {
            android.util.Log.d("zlmg","doCellLocation");
            doCellLocation(actionId, timeoutSeconds, context, listener);
        }
    }

    /**
     位置获取结束
     */
    public void LocationTerminate(final Context context)
    {
        TimerParam param = (TimerParam) AirMmiTimer.getInstance().TimerUnregister(context, doGpsLocationTimeout);
        if (param != null)
        {
            doRelease(context, param);
        }
    }

    public int getLocType()
    {
        return mType;
    }

    public double getLocLatitude()
    {
        return mLatitude;
    }

    public double getLocLongitude()
    {
        return mLongitude;
    }

    public double getLocAltitude()
    {
        return mAltitude;
    }

    public float getLocDirection()
    {
        return mDirection;
    }

    public float getLocSpeed()
    {
        return mSpeed;
    }

    public String getLocTime()
    {
        return mTime;
    }

    public String getLocPoi()
    {
        return mAddress;
    }

    private void listenerCallback(OnMapListener listener, int id, int type, boolean isFinal, double latitude, double longitude, double altitude, float direction, float speed, String time, String address)
    {

        android.util.Log.d("zlmg","listenerCallback="+address);
        boolean isOk = false;
        if (latitude != CELL_ERROR && latitude != 0)
            mLatitude = latitude;
        if (longitude != CELL_ERROR && longitude != 0)
            mLongitude = longitude;
        if (altitude != CELL_ERROR && altitude != 0)
            mAltitude = altitude;
        if (speed != CELL_ERROR && speed != 0)
            mSpeed = speed;
        if (direction != CELL_ERROR && direction != 0)
            mDirection = direction;

        if (mLatitude != 0 && mLongitude != 0)
        {
            isOk = true;
            mTime = time;
            mType = type;
        }
        if (address != null)
        {
            mAddress = address;
        }

        if (listener != null)
        {
            Log.d(AirLocationImp.class, "[LOCATION] AirLocationImp addr = " + mAddress + ", time = " + mTime);
            listener.OnMapLocation(isOk, id, type, isFinal, mLatitude, mLongitude, mAltitude, mDirection, mSpeed, mTime, mAddress);
        }
    }


    private void listenerCallback(OnMapListener listener, int id, int type, boolean isFinal, double latitude, double longitude, double altitude, float direction, float speed, String time)
    {

        android.util.Log.d("zlmg","listenerCallback");
        boolean isOk = false;
        if (latitude != CELL_ERROR && latitude != 0)
            mLatitude = latitude;
        if (longitude != CELL_ERROR && longitude != 0)
            mLongitude = longitude;
        if (altitude != CELL_ERROR && altitude != 0)
            mAltitude = altitude;
        if (speed != CELL_ERROR && speed != 0)
            mSpeed = speed;
        if (direction != CELL_ERROR && direction != 0)
            mDirection = direction;

        if (mLatitude != 0 && mLongitude != 0)
        {
            isOk = true;
            mTime = time;
            mType = type;
        }

        if (listener != null)
        {
            listener.OnMapLocation(isOk, id, type, isFinal, mLatitude, mLongitude, mAltitude, mDirection, mSpeed, mTime, mAddress);
        }
    }

    /**
     GPS Mode
     @param id Id
     @param timeoutSeconds 超时时长
     @param context 上下文
     @param listener listener
     */
    private void doGpsLocation(final int id, final int timeoutSeconds, final Context context, final OnMapListener listener)
    {
        TimerParam param = new TimerParam();
        final TimerParam fparam = param;

        LocationListener locationListener = new LocationListener()
        {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras)
            {
                Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][GPS] onStatusChanged, provider = " + provider + " status = " + status);
            }

            @Override
            public void onProviderEnabled(String provider)
            {
                Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][GPS] onProviderEnabled, provider = " + provider);
            }

            @Override
            public void onProviderDisabled(String provider)
            {
                Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][GPS] onProviderDisabled, provider = " + provider);
            }

            @Override
            public void onLocationChanged(Location location)
            {
                android.util.Log.d("zlmg","doGpsLocation。。。。。");
               // AirMmiTimer.getInstance().TimerUnregister(context, doGpsLocationTimeout);
                mGpsSuccess=true;
                Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][GPS] latitude: " + location.getLatitude() + ", longitude: " + location.getLongitude() + " Time:" + location.getTime());
                listenerCallback(listener, id, LOCATION_TYPE_GPS, true, location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getBearing(), location.getSpeed(), Util.getCurrentDate());
                Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][GPS] Closed!");
                doRelease(context, fparam);
            }
        };

        param.id = id;
        param.type = LOCATION_TYPE_GPS;
        param.timeoutSeconds = timeoutSeconds;
        param.context = context;
        param.listener = listener;
        param.listenerGps = locationListener;

        Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][GPS] Getting...");
        AirMmiTimer.getInstance().TimerRegister(context, doGpsLocationTimeout, true, true, timeoutSeconds * 1000, false, param);
        if (AirLocation.gAirmLocMan == null)
            AirLocation.gAirmLocMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // mLocMan.requestSingleUpdate(LocationManager.GPS_PROVIDER,
        // locationListener, null);
        AirLocation.gAirmLocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
    }

    /**
     GPS获取超时
     */
    private AirMmiTimerListener doGpsLocationTimeout = new AirMmiTimerListener()
    {
        @Override
        public void onMmiTimer(Context context, Object userData)
        {
            // TODO Auto-generated method stub
            TimerParam param = (TimerParam) userData;
            android.util.Log.d("zlmg","doGpsLocationTimeout。。。onMmiTimer");
            Log.i(AirLocationImp.class, "[LOCATION][ID:" + param.id + "] Timeout!");
            doRelease(context, param);
            if (param.type == LOCATION_TYPE_GPS && !mGpsSuccess)
            {
                Log.i(AirLocationImp.class, "[LOCATION][ID:" + param.id + "][GPS] Timeout to get CELL");
                android.util.Log.d("zlmg","doGpsLocationTimeout");
                doCellLocation(param.id, AirLocation.AIR_LOCATION_CELL_TRY_TIME - 5, param.context, param.listener);
            }
            mGpsSuccess=false;
        }
    };

    /**
     Cell Mode
     @param id Id
     @param timeoutSeconds 超时时长
     @param context 上下文
     @param listener
     */
    private void doCellLocation(final int id, final int timeoutSeconds, final Context context, final OnMapListener listener)
    {
        doCellLocation(id, true, timeoutSeconds, context, listener);
    }

    private void doCellLocation(final int id, final boolean isFinal, final int timeoutSeconds, final Context context, final OnMapListener listener)
    {
        if (mClientCell == null || mClientCellOption == null)
        {
            mClientCell = new LocationClient(context);
            mClientCellOption = new LocationClientOption();
            mClientCellOption.setOpenGps(false);
            mClientCellOption.setAddrType("all");
            mClientCellOption.setCoorType("gcj02");
            // mClientCellOption.disableCache(true);
            mClientCellOption.setScanSpan(LocationClientOption.MIN_SCAN_SPAN);
            // mClientCellOption.setPriority(LocationClientOption.NetWorkFirst);
            // mClientCellOption.setPoiNumber(0);
            // mClientCellOption.setPoiDistance(1000);
            // mClientCellOption.setPoiExtraInfo(false);
            mClientCell.setLocOption(mClientCellOption);
        }

        BDAbstractLocationListener locationCellListener = new BDAbstractLocationListener()
        {
            @Override
            public void onReceiveLocation(BDLocation location)
            {
                if (mClientCellGetting)
                {
                    long ts = 0;
                    Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][BAIDU-CELL][FINAL: " + isFinal + "] X:" + location.getLatitude() + " Y:" + location.getLongitude() + " Time:" + location.getTime() + " TimeGap:" + ts + "s");
                    if (ts > -AirLocation.AIR_LOCATION_CELL_TIME_GAP)
                    {
                        android.util.Log.d("zlmgg","doCellLocation。。。。。");
                        listenerCallback(listener, id, LOCATION_TYPE_CELL_BAIDU, isFinal, location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getDirection(), location.getSpeed(), Util.getCurrentDate(), location.getAddrStr());
                    }
                    else
                    {
                        Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][BAIDU-CELL] Time gap is too long!!! Ignore!");
                    }
                    mClientCellGetting = false;
                }
                if (mClientCell != null)
                {
                    mClientCell.unRegisterLocationListener(this);
                    mClientCell.stop();
                    Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][BAIDU-CELL] Closed!");
                }
            }

        };
        Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][BAIDU-CELL] Getting...");
        mClientCell.registerLocationListener(locationCellListener);
        // mClientCell.requestLocation();
        if (mClientCell.isStarted())
            mClientCell.stop();
        mClientCellGetting = true;
        mClientCell.start();
    }

    /**
     Release getting
     @param context 上下文
     @param param timer参数
     */
    private void doRelease(Context context, TimerParam param)
    {
        switch (param.type)
        {
            case LOCATION_TYPE_GPS:
            {
                if (AirLocation.gAirmLocMan != null && param.listenerGps != null)
                {
                    AirLocation.gAirmLocMan.removeUpdates(param.listenerGps);
                    Log.i(AirLocationImp.class, "[LOCATION][ID:" + param.id + "][GPS] Released!");
                }
                break;
            }
            case LOCATION_TYPE_CELL_BAIDU:
            {
                if (mClientCell != null)
                {
                    mClientCell.unRegisterLocationListener(param.listenerCellBaidu);
                    if (mClientCell.isStarted())
                        mClientCell.stop();
                    Log.i(AirLocationImp.class, "[LOCATION][ID:" + param.id + "][BAIDU-CELL] Released!");
                }
                break;
            }
        }
    }

}
