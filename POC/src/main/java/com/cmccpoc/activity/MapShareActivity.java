package com.cmccpoc.activity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnMediaListener;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirLocationShare;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.MemberFragment;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.CallAlertDialog;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirLocationShareControl;
import com.cmccpoc.entity.LocationBean;
import com.cmccpoc.listener.OnMmiLocationShareListener;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.MapUtilBaidu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cmccpoc.util.MapUtilBaidu.showMarkerByView;


public class MapShareActivity extends Activity implements OnClickListener, OnTouchListener, OnMediaListener, OnMmiLocationShareListener, BaiduMap.OnMarkerClickListener, BaiduMap.OnMapLoadedCallback
{

    private static final int DIALOG_CALL = 99;
    private static final int DIALOG_BACK = 1;
    private static final int DIALOG_EXIT = 2;

    private static final int HANDLER_REFRESH_PTT = 1;
    private static final int HANDLER_LOCATE = 2;

    private AirSession session;
    private ImageView ivMapBack, ivMapExit, ivSessionStatus, ivMapLocate;
    private TextView tvMapInfo, tvSessionStatus;
    private ImageView btnTalkVideo;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private CallAlertDialog alertDialog;

    private Marker mMarker;
    // key:ipocid, value:Marker
    private Map<String, Marker> memberMap = new HashMap<String, Marker>();
    private double mLatitude = 0.0, mLongitude = 0.0;

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        // SDKInitializer.initialize(getApplicationContext());// 百度地图
        setContentView(R.layout.activity_map_share);
        bundle = getIntent().getExtras();
        if (bundle != null)
        {
            String sessionCode = bundle.getString("sessionCode");
            session = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode);
        }
        doInitView();
        AirLocationShareControl.getInstance().setOnMmiLocationShareListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (session != null)
        {
            AirtalkeeSessionManager.getInstance().setOnMediaListener(this);
            refreshPttState();
        }
        if (mMapView != null)
            mMapView.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        AirtalkeeSessionManager.getInstance().setOnMediaListener(null);
        if (mMapView != null)
            mMapView.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        AirLocationShareControl.getInstance().setOnMmiLocationShareListener(null);
        if (mMapView != null)
        {
            mMapView.onDestroy();
            mMapView = null;
        }
    }

    private void doInitView()
    {
        ivMapBack = (ImageView) findViewById(R.id.map_share_back);
        ivMapBack.setOnClickListener(this);
        ivMapExit = (ImageView) findViewById(R.id.btn_exit);
        ivMapExit.setOnClickListener(this);
        ivMapLocate = (ImageView) findViewById(R.id.btn_locate);
        ivMapLocate.setOnClickListener(this);
        tvMapInfo = (TextView) findViewById(R.id.map_share_info);
        btnTalkVideo = (ImageView) findViewById(R.id.bt_talk);
        btnTalkVideo.setOnTouchListener(this);
        tvSessionStatus = (TextView) findViewById(R.id.talk_video_status_panel);
        ivSessionStatus = (ImageView) findViewById(R.id.talk_video_status_iv);
        initBaiduMap();
    }

    private void initBaiduMap()
    {
        mMapView = (MapView) findViewById(R.id.share_map);
        mBaiduMap = mMapView.getMap();
        // 地图上比例尺
        mMapView.showScaleControl(false);
        // 隐藏缩放控件
        MapUtilBaidu.goneMapViewChild(mMapView, true, true);
        // 普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        // 改变地图状态
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));
        // 设置地图状态监听者
        // mBaiduMap.setOnMapStatusChangeListener(mapStatusChangeListener);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        if (Config.funcLocationShareMarkerClickable)
            mBaiduMap.setOnMarkerClickListener(this);
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {
                mBaiduMap.hideInfoWindow();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi)
            {
                return false;
            }
        });
        mBaiduMap.setOnMapLoadedCallback(this);
    }

    private boolean startShare()
    {
        Log.i(MapShareActivity.class, "[LOCSHARE] MapShareActivity startShare");
        return AirLocationShareControl.getInstance().startShare(session);
    }

    private void stopShare()
    {
        AirLocationShareControl.getInstance().stopShare(session);
    }

    private void locateMove(final boolean moveTo)
    {
        Log.i(MapShareActivity.class, "[LOCSHARE] onLocateSucceed lat:" + mLatitude + ", lon:" + mLongitude);
        if (mMarker != null)
            mMarker.remove();
        else
            mBaiduMap.clear();
        View view = getPopupView();
        TextView display = (TextView) view.findViewById(R.id.tv_display);
        display.setText(AirtalkeeAccount.getInstance().getUserName());
        display.setBackgroundResource(R.drawable.pop_up_pannel_grey);
        mMarker = showMarkerByView(mLatitude, mLongitude, view, mBaiduMap, 0, moveTo);
        if (moveTo)
        {
            MapStatus mMapStatus = new MapStatus.Builder().target(mMarker.getPosition()).zoom(mBaiduMap.getMaxZoomLevel() - 6).build();
            MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(mMapStatus);
            mBaiduMap.animateMapStatus(u);
        }
        memberMap.put(AirtalkeeAccount.getInstance().getUser().getIpocId(), mMarker);
        // param 坐标类型
        mHandler.sendEmptyMessage(HANDLER_REFRESH_PTT);
    }

    private void locate()
    {
        MapUtilBaidu.locateByBaiduMap(AirServices.getInstance(), 1000, new MapUtilBaidu.LocateListener()
        {
            @Override
            public void onLocateSucceed(LocationBean locationBean)
            {
                mLatitude = locationBean.getLatitude();
                mLongitude = locationBean.getLongitude();
                locateMove(true);
            }

            @Override
            public void onLocateFiled()
            {

            }

            @Override
            public void onLocating()
            {

            }
        });
    }

    private View getPopupView()
    {
        return LayoutInflater.from(this).inflate(R.layout.layout_popup_window_location_share, null);
    }

    /**
     刷新PTT按钮状态
     */
    private void refreshPttState()
    {
        if (session == null)
            return;
        if (session.getMediaState() == AirSession.MEDIA_STATE_LISTEN && session.getSpeaker() != null)
        {
            ivSessionStatus.setBackgroundResource(R.drawable.media_listen);
            tvSessionStatus.setText(session.getSpeaker().getIpocId() + " " + getString(R.string.talk_speaking));
        }
        else if (session.getMediaState() == AirSession.MEDIA_STATE_TALK)
        {
            ivSessionStatus.setBackgroundResource(R.drawable.media_talk);
            tvSessionStatus.setText(getString(R.string.talk_speak_me));
        }
        else
        {
            ivSessionStatus.setBackgroundResource(R.drawable.media_talk);
            tvSessionStatus.setText(getString(R.string.talk_session_speak_idle));
        }
        switch (session.getMediaButtonState())
        {
            case AirSession.MEDIA_BUTTON_STATE_IDLE:
            case AirSession.MEDIA_BUTTON_STATE_RELEASING:
                btnTalkVideo.setBackgroundResource(R.drawable.video_talk_normal);
                break;
            case AirSession.MEDIA_BUTTON_STATE_TALKING:
                btnTalkVideo.setBackgroundResource(R.drawable.video_talk_press);
                break;
            case AirSession.MEDIA_BUTTON_STATE_CONNECTING:
            case AirSession.MEDIA_BUTTON_STATE_REQUESTING:
                ivSessionStatus.setBackgroundResource(R.drawable.media_talk);
                tvSessionStatus.setText(getString(R.string.talk_click_applying));
            case AirSession.MEDIA_BUTTON_STATE_QUEUE:
                btnTalkVideo.setBackgroundResource(R.drawable.video_talk_normal);
                break;
        }

        tvMapInfo.setText(memberMap.size() + getString(R.string.talk_location_share_count));
    }

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case HANDLER_REFRESH_PTT:
                {
                    refreshPttState();
                    break;
                }
                case HANDLER_LOCATE:
                {
                    Log.i(MapShareActivity.class, "[LOCSHARE] MapShareActivity mHandler HANDLER_LOCATE in");
                    startShare();
                    locate();
                    showMarker(AirLocationShareControl.getInstance().getLocationShareMap(session.getSessionCode()));
                    break;
                }
            }
        }
    };

    private void showMarker(Map<String, AirLocationShare> subMap)
    {
        if (subMap != null && subMap.size() > 0)
        {
            for (Map.Entry<String, AirLocationShare> entry : subMap.entrySet())
            {
                String ipocid = entry.getKey();
                AirLocationShare entity = entry.getValue();
                if (!ipocid.equals(AirtalkeeAccount.getInstance().getUser().getIpocId()))
                {
                    Marker marker = memberMap.get(ipocid);
                    if (marker != null)
                        marker.remove();
                    View view = getPopupView();
                    TextView display = (TextView) view.findViewById(R.id.tv_display);
                    display.setText(entity.getDisplay());
                    marker = showMarkerByView(entity.getLatitude(), entity.getLongitude(), view, mBaiduMap, 0, false);
                    Bundle bundle = new Bundle();
                    bundle.putString("ipocid", ipocid);
                    marker.setExtraInfo(bundle);
                    memberMap.put(ipocid, marker);
                }
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.map_share_exit:
            case R.id.btn_exit:
            {
                /*
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.talk_location_share);
                builder.setMessage(R.string.talk_location_share_exit_tip);
                builder.setPositiveButton(getString(R.string.talk_ok_3), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        dialog.cancel();
                        stopShare();
                        // AirLocationShareControl.getInstance().release();
                        finish();
                    }
                });
                builder.setNegativeButton(getString(R.string.talk_no), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        dialog.cancel();
                    }
                });
                Dialog d = builder.create();
                d.show();*/
                showShareDialog(DIALOG_EXIT);
                break;
            }
            case R.id.map_share_back:
                showShareDialog(DIALOG_BACK);
                break;
            case R.id.btn_locate:
                locateMove(true);
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (session != null)
        {
            if (v.getId() == R.id.bt_talk)
            {
                if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
                {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        Log.i(VideoSessionActivity.class, "TalkButton onLongClick TalkRequest!");
                        AirtalkeeSessionManager.getInstance().TalkRequest(session, false);
                    }
                    else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                    {
                        Log.i(VideoSessionActivity.class, "TalkButton onLongClick TalkRelease!");
                        AirtalkeeSessionManager.getInstance().TalkRelease(session);
                    }
                    refreshPttState();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onMediaStateTalkPreparing(AirSession session)
    {
        refreshPttState();
    }

    @Override
    public void onMediaStateTalk(AirSession session)
    {
        refreshPttState();
    }

    @Override
    public void onMediaStateTalkEnd(AirSession session, int reason)
    {
        refreshPttState();
    }

    @Override
    public void onMediaStateListen(AirSession session, AirContact speaker)
    {
        refreshPttState();
    }

    @Override
    public void onMediaStateListenEnd(AirSession session)
    {
        refreshPttState();
    }

    @Override
    public void onMediaStateListenVoice(AirSession session)
    {
        refreshPttState();
    }

    @Override
    public void onMediaQueue(AirSession session, ArrayList<AirContact> queue)
    {
        refreshPttState();
    }

    @Override
    public void onMediaQueueIn(AirSession session)
    {
        refreshPttState();
    }

    @Override
    public void onMediaQueueOut(AirSession session)
    {
        refreshPttState();
    }

    @Override
    public void onMmiLocationShareTimer(double latitude, double longitude)
    {
        Log.i(MapShareActivity.class, "[LOCSHARE] onMmiLocationStareTimer");
        mLatitude = latitude;
        mLongitude = longitude;
        locateMove(false);
    }

    @Override
    public void onMmiLocationShareStart(Map<String, AirLocationShare> subMap)
    {
        if (session != null)
        {
            showMarker(subMap);
            mHandler.sendEmptyMessage(HANDLER_REFRESH_PTT);
        }
    }

    @Override
    public void onMmiLocationShareStop(String sessionCode)
    {
        if (session != null && sessionCode.equals(session.getSessionCode()))
            finish();
    }

    @Override
    public void onMmiLocationSharePoint(Map<String, AirLocationShare> subMap)
    {
        if (session != null)
        {
            showMarker(subMap);
            refreshPttState();
        }
    }

    @Override
    public void onMmiLocationShareMemberClean(Map<String, AirLocationShare> subMap, List<String> removeKey)
    {
        if (session != null)
        {
            if (removeKey.size() > 0)
            {
                for (int i = 0; i < removeKey.size(); i++)
                {
                    memberMap.get(removeKey.get(i)).remove();
                    memberMap.remove(removeKey.get(i));
                }
            }
            showMarker(subMap);
            refreshPttState();
        }
    }

    @Override
    public void onMmiLocationStateReceive(int locState, String ipocid, String userName)
    {
        // 只处理shareState = Stop的消息
        if (session != null && locState == AirMessage.LOCATION_SHARE_STOP)
        {
            if (memberMap.get(ipocid) != null)
                memberMap.get(ipocid).remove();
            memberMap.remove(ipocid);
            refreshPttState();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        Bundle bundle = marker.getExtraInfo();
        if (bundle == null)
            return true;
        final String ipocid = bundle.getString("ipocid");
        if (!TextUtils.isEmpty(ipocid) && !ipocid.equals(AirtalkeeAccount.getInstance().getUser().getIpocId()))
        {
            InfoWindow infoWindow;
            View markerView = LayoutInflater.from(this).inflate(R.layout.layout_popup_window_map_marker, null);
            TextView tvIpocid = (TextView) markerView.findViewById(R.id.marker_ipocid);
            tvIpocid.setText(ipocid);
            View video = markerView.findViewById(R.id.marker_video_call);
            video.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Toast.makeText(MapShareActivity.this, "video", Toast.LENGTH_SHORT).show();
                    callUser(ipocid, true);
                    mBaiduMap.hideInfoWindow();
                }
            });
            View voice = markerView.findViewById(R.id.marker_audio_call);
            voice.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Toast.makeText(MapShareActivity.this, "voice", Toast.LENGTH_SHORT).show();
                    callUser(ipocid, false);
                    mBaiduMap.hideInfoWindow();
                }
            });
            //得到点击的覆盖物的经纬度
            LatLng ll = marker.getPosition();
            //将marker所在的经纬度的信息转化成屏幕上的坐标
            Point p = mBaiduMap.getProjection().toScreenLocation(ll);
            p.y -= 90;
            LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);
            //初始化infoWindow，最后那个参数表示显示的位置相对于覆盖物的竖直偏移量，这里也可以传入一个监听器
            infoWindow = new InfoWindow(markerView, llInfo, 0);
            mBaiduMap.showInfoWindow(infoWindow);//显示此infoWindow
        }
        return true;
    }

    private void callUser(String userId, boolean withVideo)
    {
        AirContact user = new AirContact();
        user.setIpocId(userId);
        user.setDisplayName(userId);
        AirSession s = SessionController.SessionMatch(user);
        alertDialog = new CallAlertDialog(this, "正在呼叫" + s.getDisplayName(), "请稍后...", s.getSessionCode(), DIALOG_CALL, withVideo, new CallAlertDialog.OnAlertDialogCancelListener()
        {
            @Override
            public void onDialogCancel(int reason)
            {
                Log.i(MemberFragment.class, "MemberFragment reason = " + reason);
                switch (reason)
                {
                    case AirSession.SESSION_RELEASE_REASON_NOTREACH:
                        com.cmccpoc.util.Toast.makeText1(AirServices.getInstance(), "对方目前不在线！", com.cmccpoc.util.Toast.LENGTH_SHORT).show();
                        break;
                    case AirSession.SESSION_RELEASE_REASON_REJECTED:
                        com.cmccpoc.util.Toast.makeText1(AirServices.getInstance(), "对方已拒接", com.cmccpoc.util.Toast.LENGTH_SHORT).show();
                        break;
                    case AirSession.SESSION_RELEASE_REASON_BUSY:
                        com.cmccpoc.util.Toast.makeText1(AirServices.getInstance(), "对方正在通话中，无法建立呼叫", com.cmccpoc.util.Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        com.cmccpoc.util.Toast.makeText1(AirServices.getInstance(), "呼叫建立失败！", com.cmccpoc.util.Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        alertDialog.show();
    }

    private void showShareDialog(int dialogId)
    {
        if (dialogId == DIALOG_BACK)
        {
            AlertDialog backDialog = new AlertDialog(this, getString(R.string.talk_location_share_back_tip), null, new AlertDialog.DialogListener()
            {
                @Override
                public void onClickOk(int id, Object obj)
                {
                    finish();
                }

                @Override
                public void onClickOk(int id, boolean isChecked)
                {
                }

                @Override
                public void onClickCancel(int id)
                {
                }
            }, DIALOG_BACK);
            backDialog.show();
        }
        else if (dialogId == DIALOG_EXIT)
        {
            AlertDialog backDialog = new AlertDialog(this, getString(R.string.talk_location_share_exit_tip), null, new AlertDialog.DialogListener()
            {
                @Override
                public void onClickOk(int id, Object obj)
                {
                    stopShare();
                }

                @Override
                public void onClickOk(int id, boolean isChecked)
                {
                }

                @Override
                public void onClickCancel(int id)
                {
                }
            }, DIALOG_BACK);
            backDialog.show();
        }
    }

    @Override
    public void onMapLoaded()
    {
        Log.i(MapShareActivity.class, "[LOCSHARE] MapShareActivity onMapLoaded");
        mHandler.sendEmptyMessage(HANDLER_LOCATE);
    }
}
