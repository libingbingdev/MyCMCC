package com.cmccpoc.activity.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.OnContactPresenceListener;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.activity.AllMemberActivity;
import com.cmccpoc.activity.home.adapter.AdapterChannel;
import com.cmccpoc.activity.home.widget.MediaStatusBar;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.listener.OnMmiChannelListener;
import com.cmccpoc.listener.OnMmiSessionListener;
import com.cmccpoc.util.Util;
import com.airtalkee.sdk.AirtalkeeContactPresence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


public class ChannelListActivity extends BaseActivity implements AdapterView.OnItemClickListener ,OnMmiSessionListener, OnContactPresenceListener,AdapterView.OnItemLongClickListener,View.OnCreateContextMenuListener {
    private ListView mChannelList;
    private AdapterChannel adapterChannel;
    private TextView mTitleText;
    private Button mOk,mBack;
    private FrameLayout mMemberLayout;
    private boolean isChannel=true;
    private String mSessionId="";
    private int mCurrentPosition;
    private static final int MAX_ITEM_COUNT=3;
    private int item_id=0;
    private static ChannelListActivity mInstance;
    public static ChannelListActivity getInstance()
    {
        return mInstance;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_list);
        mTitleText=(TextView) findViewById(R.id.channel_tile_tv);
        mMemberLayout=(FrameLayout) findViewById(R.id.member_list_container);
        mChannelList=(ListView) findViewById(R.id.gv_channels);
        mOk= (Button) findViewById(R.id.ok);
        mBack= (Button) findViewById(R.id.back);
        adapterChannel = new AdapterChannel(this, null);
        mChannelList.setAdapter(adapterChannel);
        mChannelList.setOnItemClickListener(this);
        mChannelList.setOnCreateContextMenuListener(this);
        mChannelList.setOnItemLongClickListener(this);
        AirSessionControl.getInstance().setOnMmiSessionListener(this);
        AirtalkeeContactPresence.getInstance().setContactPresenceListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        sessionRefresh();

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        android.util.Log.d("zlm","mCurrentPosition="+mCurrentPosition);
        mCurrentPosition=i;
        changeChannel(mCurrentPosition);
        finish();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        return true;
    }

    private void intoMember(){
        Intent intent=new Intent(ChannelListActivity.this,MemberListActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("channel.id",mSessionId);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onChannelListGet(boolean isOk, List<AirChannel> channels) {
        super.onChannelListGet(isOk, channels);
        sessionRefresh();
    }

    @Override
    public void onChannelMemberListGet(String channelId, List<AirContact> members)
    {
        sessionRefresh();
    }

    @Override
    public void onChannelOnlineCount(LinkedHashMap<String, Integer> online)
    {
        sessionRefresh();
    }

    private void changeChannel(int position){
        ArrayList<String> airChannelList = new ArrayList<String>();
        final AirSession session = AirSessionControl.getInstance().getCurrentSession();
        if (session != null) {
            if (session.getType() == AirSession.TYPE_DIALOG) {
                AirSessionControl.getInstance().SessionEndCall(session);
            }
        }
        //如果更改前的频道不处于多频道监听，那么退出监听
        reMoveUnusedListen(airChannelList, session);

        AirChannel channel = (AirChannel) adapterChannel.getItem(position);
        if (channel != null) {

            if (AirtalkeeAccount.getInstance().isEngineRunning()) {
                AirSessionControl.getInstance().SessionChannelIn(channel.getId());
            } else {
                Util.Toast(this, this.getString(R.string.talk_network_warning));
            }
        }
        mChannelList.requestFocus();
        mChannelList.setSelection(position);
        sessionRefresh();
    }

    private void reMoveUnusedListen(ArrayList<String> airChannelList, AirSession session) {
        String airchannel_id1;
        String airchannel_id2;
        SharedPreferences sPreferences = getSharedPreferences("config", MODE_PRIVATE);
        airchannel_id1 = sPreferences.getString("airchannel_id1", "");
        if (!airchannel_id1.equals("")) {
            airChannelList.add(airchannel_id1);
        }
        airchannel_id2 = sPreferences.getString("airchannel_id2", "");
        if (!airchannel_id2.equals("")) {
            airChannelList.add(airchannel_id2);
        }
        if(session != null && !airChannelList.contains(session.getSessionCode())){
            AirSessionControl.getInstance().SessionChannelOut(session.getSessionCode());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_F4:
                if(mCurrentPosition<=0){
                    mCurrentPosition=Math.min(AirtalkeeChannel.getInstance().getChannels().size(),MAX_ITEM_COUNT);
                }
                mCurrentPosition--;
                changeChannel(mCurrentPosition);
                return true;
            case KeyEvent.KEYCODE_F5:
                mCurrentPosition++;
                mCurrentPosition=mCurrentPosition % Math.min((AirtalkeeChannel.getInstance().getChannels().size()),MAX_ITEM_COUNT);
                changeChannel(mCurrentPosition);
                return true;
            case KeyEvent.KEYCODE_MENU:
                mOk.setBackgroundResource(R.drawable.bg_list_focuse);
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                break;
            case KeyEvent.KEYCODE_BACK:
                mBack.setBackgroundResource(R.drawable.bg_list_focuse);
                break;
        }
        return super.onKeyDown(keyCode,event);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_F4:
            case KeyEvent.KEYCODE_F5:
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                mOk.setBackgroundResource(R.drawable.bg_list_normal);
                break;
            case KeyEvent.KEYCODE_MENU:
                mOk.setBackgroundResource(R.drawable.bg_list_normal);
                int position= mChannelList.getSelectedItemPosition();
                changeChannel(position);
                finish();
                break;
            case KeyEvent.KEYCODE_BACK:
                mBack.setBackgroundResource(R.drawable.bg_list_normal);
                break;
        }
        return super.onKeyUp(keyCode, event);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        AirtalkeeContactPresence.getInstance().setContactPresenceListener(null);
        AirSessionControl.getInstance().setOnMmiSessionListener(null);
    }

    private AirChannel getCurrentChannel(){
        mCurrentPosition=mChannelList.getSelectedItemPosition();
        AirChannel channel = (AirChannel) adapterChannel.getItem(mCurrentPosition);
        return channel;
    }

    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        item_id = info.position;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.channels_listener_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.channel_listen:
                AirSessionControl.getInstance().SessionChannelIn(getCurrentChannel(item_id).getId());
                sessionRefresh();
                break;
            case R.id.cancel_listen:
                AirSessionControl.getInstance().SessionChannelOut(getCurrentChannel(item_id).getId());
                sessionRefresh();
                break;
        }
        return super.onContextItemSelected(item);
    }*/

    @Override
    public void onSessionOutgoingRinging(AirSession session)
    {
        // TODO Auto-generated method stub
        sessionRefresh();
    }

    @Override
    public void onSessionEstablishing(AirSession session)
    {
        sessionRefresh();
    }

    @Override
    public void onSessionEstablished(AirSession session, int result) { sessionRefresh();}

    @Override
    public void onSessionReleased(AirSession session, int reason)
    {
        com.airtalkee.sdk.util.Log.i(MediaStatusBar.class, "onSessionReleased MediaStatusBar start reason = " + reason);
        sessionRefresh();
    }

    @Override
    public void onSessionPresence(AirSession session, List<AirContact> membersAll, List<AirContact> membersPresence) {
        sessionRefresh();
        if(MemberListActivity.getInstance()!=null){
            MemberListActivity.getInstance().refreshMembers(session, membersAll);
        }
       if(AllMemberActivity.getInstance()!=null){
           AllMemberActivity.getInstance().refreshMemberList();
       }

    }

    @Override
    public void onSessionMemberUpdate(AirSession session, List<AirContact> members, boolean isOk) {
        sessionRefresh();
        if(MemberListActivity.getInstance()!=null){
            MemberListActivity.getInstance().refreshMembers(session,session.getMemberAll());
        }
    }

    @Override
    public void onContactPresence(boolean isSubscribed, HashMap<String, Integer> presenceMap)
    {
        if (AllMemberActivity.getInstance() != null)
        {
            AllMemberActivity.getInstance().refreshMemberList();
        }
        if(MemberListActivity.getInstance()!=null){
            MemberListActivity.getInstance().updateMembers();
        }
        if (HomeActivity.getInstance() != null)
            HomeActivity.getInstance().refreshChannel();
    }

    @Override
    public void onContactPresence(boolean isSubscribed, String uid, int state)
    {
        if (AllMemberActivity.getInstance() != null)
        {
            AllMemberActivity.getInstance().refreshMemberList();
        }
        if(MemberListActivity.getInstance()!=null){
            MemberListActivity.getInstance().updateMembers();
        }
        if (HomeActivity.getInstance() != null)
            HomeActivity.getInstance().refreshChannel();
    }
    public void sessionRefresh() {
        if (adapterChannel != null) {
                adapterChannel.notifyDataSetChanged();
        }
    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.channels_listener_menu,menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem  listenMenu,cancelItem;
        listenMenu=menu.findItem(R.id.channel_listen);
        cancelItem=menu.findItem(R.id.cancel_listen);
        if((getCurrentChannel()).getSession() != null && getCurrentChannel().getSession().getSessionState() == AirSession.SESSION_STATE_DIALOG){
            listenMenu.setVisible(false);
            cancelItem.setVisible(true);
        }else{
            listenMenu.setVisible(true);
            cancelItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.channel_listen:
                AirSessionControl.getInstance().SessionChannelIn(getCurrentChannel().getId(),false);
                sessionRefresh();
                break;
            case R.id.cancel_listen:
                AirSessionControl.getInstance().SessionChannelOut(getCurrentChannel().getId());
                sessionRefresh();
                break;
        }
        return true;
    }*/
}
