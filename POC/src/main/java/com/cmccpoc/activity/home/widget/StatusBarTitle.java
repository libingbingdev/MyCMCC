package com.cmccpoc.activity.home.widget;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.controller.MessageController;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.activity.MoreActivity;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.control.VoiceManager;
import com.cmccpoc.util.Toast;

import java.util.List;

/**
 顶部标题控件（锁定频道+频道名称+更多）
 @author Yao */
public class StatusBarTitle extends LinearLayout implements OnClickListener
{
    private TextView tvTitle, tvMediaStatus;
    private TextView tvState , tvDeviceName ,tvMemberNum;
    private ImageView ivMeidiaStatus;
    private View btnLeft, btnRight;

    private ImageView ivBtnLeft, ivUnReadDot, ivNoticeUnread,ivBtnRight;
    private AirSession session = null;

    private final int ID_LED_LISEN=11089;
    private NotificationManager mNotificationManager;

    private static StatusBarTitle mInstance;
    private SpeakerBroadCastDialog mSpeakerBroadCastDialog;

    public static StatusBarTitle getInstance()
    {
        return mInstance;
    }
    private Dialog WarnDialog;

    public StatusBarTitle(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater.from(this.getContext()).inflate(R.layout.include_home_title, this);
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onFinishInflate()
    {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        initFindView();
        mInstance = this;
    }

    /**
     初始化绑定控件
     */
    private void initFindView()
    {
        ivUnReadDot = (ImageView) findViewById(R.id.unread_dot);
        ivNoticeUnread = (ImageView) findViewById(R.id.iv_Unread);
        btnLeft = findViewById(R.id.left_button);
        btnRight = findViewById(R.id.right_button);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        ivMeidiaStatus = (ImageView) findViewById(R.id.iv_media_status);
        tvMediaStatus = (TextView) findViewById(R.id.tv_media_status);
        ivBtnLeft = (ImageView) findViewById(R.id.bottom_left_icon);
        ivBtnRight=(ImageView) findViewById(R.id.bottom_right_icon);
        // findViewById(R.id.title_drag).setOnClickListener(this);
        tvMemberNum= (TextView) findViewById(R.id.tv_member_num);
        tvDeviceName= (TextView) findViewById(R.id.tv_device_name);
        tvState= (TextView) findViewById(R.id.tv_state);

        btnLeft.setOnClickListener(this);
        btnRight.setOnClickListener(this);
        checkBrodcast();
    }

    /**
     检测是否有广播，如果有则显示未读标记
     */
    public void checkBrodcast()
    {
        if (Config.funcBroadcast && AirtalkeeAccount.getInstance().SystemBroadcastNumberGet() > 0)
        {
            ivNoticeUnread.setVisibility(View.VISIBLE);
        }
        else
        {
            ivNoticeUnread.setVisibility(View.GONE);
        }
    }

    /**
     设置session会话
     @param s 会话Entity
     */
    public void setSession(AirSession s)
    {
        this.session = s;
        Log.d("zlm","setSession");
        refreshMediaStatus();
        refreshNewMsg();
    }

    /**
     * 是否属于多频道
     */
    private boolean isMultiChannel() {
        SharedPreferences sPreferences = getContext().getSharedPreferences("config", getContext().MODE_PRIVATE);
        String airchannel_id1 = sPreferences.getString("airchannel_id1", "");
        String airchannel_id2 = sPreferences.getString("airchannel_id2", "");
        return ! (airchannel_id1.equals("") && airchannel_id2.equals(""));

    }

    /**
     刷新媒体会话状态
     */
    public void refreshMediaStatus()
    {
        Log.d("zlm","refreshMediaStatus   ...session="+session);
        if (session != null)
        {
            if(WarnDialog!=null ){
                Log.d("zlm"," WarnDialog.dismiss()   .. .");
                WarnDialog.dismiss();
                WarnDialog=null;
            }
            try
            {
                Log.d("zlm","refresh_mian_view");
                refresh_mian_view();
                // tvTitle.setCompoundDrawables(getResources().getDrawable(R.drawable.ic_drag_down),
                // null, null, null);
                switch (session.getSessionState())
                {
                    case AirSession.SESSION_STATE_CALLING:
                        tvMediaStatus.setText(R.string.talk_session_building);
                        ivMeidiaStatus.setImageResource(R.drawable.media_idle_green);
                        break;
                    case AirSession.SESSION_STATE_DIALOG:
                        switch (session.getMediaState())
                        {
                            case AirSession.MEDIA_STATE_IDLE:
                            {
                                mHandler.removeMessages(0x02);
                                tvMediaStatus.setText(R.string.talk_session_speak_idle);
                                ivMeidiaStatus.setImageResource(R.drawable.media_idle_green);
                                mNotificationManager.cancel(ID_LED_LISEN);
                                break;
                            }
                            case AirSession.MEDIA_STATE_TALK:
                            {
                                ivMeidiaStatus.setImageResource(R.drawable.media_listen);
                                tvMediaStatus.setText(R.string.talk_speak_me);
                                // mNotificationManager.cancel(ID_LED_LISEN);
                                led(HomeActivity.getInstance(),Color.BLUE);
                                break;
                            }
                            case AirSession.MEDIA_STATE_LISTEN:
                            {
                                AirContact contact = session.getSpeaker();
                                ivMeidiaStatus.setImageResource(R.drawable.media_listen);
                                if (contact != null)
                                {
                                    tvMediaStatus.setText(contact.getDisplayName() + "  " + this.getContext().getString(R.string.talk_speaking));
                                    led(HomeActivity.getInstance(),Color.GREEN);

                                }
                                break;
                            }
                        }
                        break;
                    case AirSession.SESSION_STATE_IDLE:
                        if (session.getType() == AirSession.TYPE_CHANNEL) {
                            tvMediaStatus.setText(R.string.talk_channel_idle);
                            mHandler.removeMessages(0x02);
                            mHandler.sendEmptyMessageDelayed(0x02,5000);
                        }else
                            tvMediaStatus.setText(R.string.talk_session_speak_idle);
                        ivMeidiaStatus.setImageResource(R.drawable.media_idle_gray);
                        mNotificationManager.cancel(ID_LED_LISEN);
                        break;
                }
            }
            catch (Exception e)
            {
                // TODO: handle exception
            }
        }
        else
        {
            tvTitle.setText(getContext().getString(R.string.talk_group_no_connect));
           if(AirtalkeeChannel.getInstance().getChannels().size()==0){
               if(WarnDialog==null){
                   WarnDialog=new WarningDialog(HomeActivity.getInstance(),HomeActivity.getInstance().getString(R.string.talk_group_no_connect),R.drawable.ic_warning);
                   WarnDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                   Log.d("zlm"," WarnDialog.show()   .. .");
                   WarnDialog.show();
               }


            }

        }
    }

    private void refresh_mian_view() {
        HomeActivity.getInstance().refreshMenuButton();
        final AirSession airSession = SessionController.SessionMatchSpecial(AirtalkeeSessionManager.SPECIAL_NUMBER_DISPATCHER,  getContext().getString(R.string.talk_tools_call_center));
        if (session.getType() == AirSession.TYPE_DIALOG) {
            ivBtnLeft.setImageResource(R.drawable.incoming_reject_icon);
            tvTitle.setVisibility(View.GONE);
            tvState.setVisibility(View.VISIBLE);
            if(airSession.equals(session)){
                tvState.setText(R.string.session_with_center);
            }else{
                tvState.setText(R.string.temp_session_on);
            }
            tvMemberNum.setVisibility(View.INVISIBLE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(session.getDisplayName());   //频道名
            if (isMultiChannel()) {
                tvState.setVisibility(View.VISIBLE);
                tvState.setText("多频道监听");
                tvMemberNum.setVisibility(View.GONE);
            } else {
                tvState.setVisibility(View.GONE);
                refreshTvMemberNum();
                mHandler.sendEmptyMessageDelayed(0x01,1000);
                if (session.isVoiceLocked()) {
                    ivBtnLeft.setImageResource(R.drawable.ic_lock);
                } else {
                    ivBtnLeft.setImageResource(R.drawable.ic_unlock);
                }
            }
        }
        tvDeviceName.setVisibility(View.VISIBLE);
        tvDeviceName.setText(AirtalkeeAccount.getInstance().getUserName()); //本机名
        Log.d("zlm", "AirtalkeeAccount.getInstance().getUserName()=" + AirtalkeeAccount.getInstance().getUserName());
    }

    private void refreshTvMemberNum() {
        int onlineNumber = 0;
        AirSession currentSession = AirSessionControl.getInstance().getCurrentSession();
        if(currentSession!=null) {
            AirChannel item = AirtalkeeChannel.getInstance().ChannelGetByCode(currentSession.getSessionCode());
            if (item != null) {
                List<AirContact> members = item.MembersGet();
                if (members != null && members.size() > 0) {
                    for (AirContact member : members) {
                        if (member.getStateInChat() == AirContact.IN_CHAT_STATE_ONLINE)
                            onlineNumber++;
                    }
                }
                tvMemberNum.setVisibility(View.VISIBLE);
                tvMemberNum.setText(onlineNumber + "/" + item.getCount());
            }
        }
    }

    private AirSession mOtherSession = null;

    public void otherSpeakerOn(AirSession session)
    {
        if (session != null && session.getSpeaker() != null && session.getChannel() != null)
        {
            Log.d("zlm","otherSpeakerOn="+session.getType());
            if(session.getChannel().getDisplayName().equals("G37025")){ //广播频道

                mNotificationManager.cancel(ID_LED_LISEN);
                led(HomeActivity.getInstance(),Color.BLUE);
                mOtherSession = session;
                mSpeakerBroadCastDialog=new SpeakerBroadCastDialog(getContext(),session.getSpeaker().getDisplayName());
                mSpeakerBroadCastDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                mSpeakerBroadCastDialog.show();

            }else{
                mNotificationManager.cancel(ID_LED_LISEN);
                led(HomeActivity.getInstance(),Color.BLUE);
                ivMeidiaStatus.setImageResource(R.drawable.media_listen);
                tvMediaStatus.setText(session.getChannel().getDisplayName() + ":" + session.getSpeaker().getDisplayName() + " " + this.getContext().getString(R.string.talk_speaking));
                mOtherSession = session;

            }

        }
    }
    public void dissmissDialog(){
        if(mSpeakerBroadCastDialog!=null &&mSpeakerBroadCastDialog.isShowing()){
            mSpeakerBroadCastDialog.dismiss();
        }
    }


    public void otherSpeakerOff(AirSession session)
    {
        if (session == mOtherSession)
        {
            otherSpeakerClean();
            mNotificationManager.cancel(ID_LED_LISEN);
        }
    }

    public void otherSpeakerClean()
    {
        if (session != null)
        {
            switch (session.getSessionState())
            {
                case AirSession.SESSION_STATE_CALLING:
                    tvMediaStatus.setText(R.string.talk_session_building);
                    ivMeidiaStatus.setImageResource(R.drawable.media_idle_green);
                    break;
                case AirSession.SESSION_STATE_DIALOG:
                    switch (session.getMediaState())
                    {
                        case AirSession.MEDIA_STATE_IDLE:
                        {
                            tvMediaStatus.setText(R.string.talk_session_speak_idle);
                            ivMeidiaStatus.setImageResource(R.drawable.media_idle_green);
                            break;
                        }
                        case AirSession.MEDIA_STATE_TALK:
                        {
                            ivMeidiaStatus.setImageResource(R.drawable.media_listen);
                            tvMediaStatus.setText(R.string.talk_speak_me);
                            break;
                        }
                        case AirSession.MEDIA_STATE_LISTEN:
                        {
                            AirContact contact = session.getSpeaker();
                            ivMeidiaStatus.setImageResource(R.drawable.media_listen);
                            if (contact != null)
                            {
                                tvMediaStatus.setText(contact.getDisplayName() + "  " + this.getContext().getString(R.string.talk_speaking));
                            }
                            break;
                        }
                    }
                    break;
                case AirSession.SESSION_STATE_IDLE:
                    if (session.getType() == AirSession.TYPE_CHANNEL)
                        tvMediaStatus.setText(R.string.talk_channel_idle);
                    else
                        tvMediaStatus.setText(R.string.talk_session_speak_idle);
                    ivMeidiaStatus.setImageResource(R.drawable.media_idle_gray);
                    break;
            }
            mOtherSession = null;
        }
    }

    @Override
    public void onClick(View arg0)
    {
        switch (arg0.getId())
        {
            case R.id.left_button:
                if (session != null && session.getType() == AirSession.TYPE_CHANNEL && session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
                {
                    if (session.isVoiceLocked())
                    {
                        AirtalkeeSessionManager.getInstance().SessionLock(session, false);
                        ivBtnLeft.setImageResource(R.drawable.ic_unlock);
                        if(Toast.isDebug) Toast.makeText1(this.getContext(), getContext().getString(R.string.talk_channel_unlock_tip), Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        AirtalkeeSessionManager.getInstance().SessionLock(session, true);
                        ivBtnLeft.setImageResource(R.drawable.ic_lock);
                        if(Toast.isDebug) Toast.makeText1(this.getContext(), getContext().getString(R.string.talk_channel_lock_tip), Toast.LENGTH_LONG).show();
                    }
                }
                else if (session != null && session.getType() == AirSession.TYPE_DIALOG)
                {
                    if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG || session.getSessionState() == AirSession.SESSION_STATE_CALLING)
                        AirSessionControl.getInstance().SessionEndCall(session);
                    session = AirSessionControl.getInstance().getCurrentChannelSession();
                    HomeActivity.getInstance().setSession(session);
                    HomeActivity.getInstance().onResume();
                    SessionAndChannelView.getInstance().refreshChannelAndDialog();
                }
                break;
            case R.id.right_button:
                Intent it = new Intent(this.getContext(), MoreActivity.class);
                this.getContext().startActivity(it);
                break;
        }
    }

    public void updateStatusBar(){
        if (session != null && session.getType() == AirSession.TYPE_CHANNEL && session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
        {
            if (session.isVoiceLocked())
            {
                AirtalkeeSessionManager.getInstance().SessionLock(session, false);
                ivBtnLeft.setImageResource(R.drawable.ic_unlock);
                if(Toast.isDebug) Toast.makeText1(this.getContext(), getContext().getString(R.string.talk_channel_unlock_tip), Toast.LENGTH_LONG).show();
            }
            else
            {
                AirtalkeeSessionManager.getInstance().SessionLock(session, true);
                ivBtnLeft.setImageResource(R.drawable.ic_lock);
                if(Toast.isDebug) Toast.makeText1(this.getContext(), getContext().getString(R.string.talk_channel_lock_tip), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void closeSession(String sessionCode)
    {
        if (session != null && TextUtils.equals(session.getSessionCode(), sessionCode))
        {
            if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG || session.getSessionState() == AirSession.SESSION_STATE_CALLING)
                AirSessionControl.getInstance().SessionEndCall(session);
            session = AirSessionControl.getInstance().getCurrentChannelSession();
            HomeActivity.getInstance().setSession(session);
            HomeActivity.getInstance().onResume();
            SessionAndChannelView.getInstance().refreshChannelAndDialog();
        }
    }

    /**
     刷新新消息，检测是否有未读
     */
    public void refreshNewMsg()
    {
        if (session != null)
        {
            int count = 0;

            List<AirChannel> channels = AirtalkeeChannel.getInstance().getChannels();
            for (int i = 0; i < channels.size(); i++)
            {
                AirChannel c = (AirChannel) channels.get(i);
                if (c != null)
                {
                    if (c.getMsgUnReadCount() > 0)
                        count++;
                }
            }
            count += MessageController.checkUnReadMessage();
            //ivUnReadDot.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void led(Context context,int color){
        Notification.Builder builder = new Notification.Builder(context);
        Notification notification = builder.build();
        notification.ledARGB = color; //0xFFff0000是红色
        notification.ledOnMS = 350;
        notification.ledOffMS =100;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS ;
        mNotificationManager.notify(ID_LED_LISEN, notification);
    }

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:
                    if(HomeActivity.isShowing) {
                        refreshTvMemberNum();
                    }
                    break;
                case 0x02:
                    mHandler.removeMessages(0x02);
                    AirtalkeeSessionManager.getInstance().TalkRequest(AirSessionControl.getInstance().getCurrentChannelSession(), AirSessionControl.getInstance().getCurrentChannelSession().getChannel().isRoleAppling());
                    AirtalkeeSessionManager.getInstance().TalkRelease(AirSessionControl.getInstance().getCurrentChannelSession());
                    mHandler.sendEmptyMessageDelayed(0x02,5000);
                    break;
            }

        }
    };
}
