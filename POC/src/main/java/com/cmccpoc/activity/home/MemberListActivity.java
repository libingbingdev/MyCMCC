package com.cmccpoc.activity.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnContactPresenceListener;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.activity.SessionAddActivity;
import com.cmccpoc.activity.home.adapter.AdapterMember;
import com.cmccpoc.activity.home.widget.MediaStatusBar;
import com.cmccpoc.control.AirAccountManager;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.listener.OnMmiChannelListener;
import com.cmccpoc.listener.OnMmiSessionListener;
import com.cmccpoc.services.AirServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


public class MemberListActivity extends BaseActivity  implements OnMmiChannelListener ,OnContactPresenceListener {
    private ListView lvMember;
    private AdapterMember adapterMember;
    private boolean isChannel=true;
   // private AirSession session;
   // private String sessionId="";
    private static MemberListActivity mInstance;
    public static MemberListActivity getInstance()
    {
        return mInstance;
    }
    private Button mOk,mBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);
        /*Intent intent=getIntent();
        if(intent!=null){
           sessionId =intent.getExtras().getString("channel.id","");
        }*/
        /*try {
            AirSession session = AirSessionControl.getInstance().getCurrentChannelSession();
            AirChannel channel=session.getChannel();
            if(channel!=null) {
                sessionId = channel.getId();
            }
        }catch (Exception e){}*/
        session=AirSessionControl.getInstance().getCurrentSession();
        mOk= (Button) findViewById(R.id.ok);
        mBack= (Button) findViewById(R.id.back);
       // Log.d("zlmm","session.getCaller()="+session.getCaller()+" ....= "+session.getCaller().equals( AirtalkeeAccount.getInstance().getUser()));
        if (session.getCaller() != null && session.getType() == AirSession.TYPE_DIALOG && session.getCaller().equals(AirtalkeeAccount.getInstance().getUser())) {
            mOk.setText(R.string.add_member);
        } else {
            mOk.setVisibility(View.GONE);
        }
        lvMember = (ListView) findViewById(R.id.member_lisview);
        adapterMember=new AdapterMember(this,null,null,true,true,null);
        if(session!=null){
            setSession(session);
        }
        lvMember.setAdapter(adapterMember);
        AirtalkeeContactPresence.getInstance().setContactPresenceListener(this);
        AirAccountManager.getInstance().setChannelListener(this);
    }

    /**
     * 设置session会话
     * @param s 会话Entity
     */
    public void setSession(AirSession s)
    {
        if (s != null)
        {
            switch (s.getType())
            {
                case AirSession.TYPE_CHANNEL:
                {
                    AirChannel c = AirtalkeeChannel.getInstance().ChannelGetByCode(s.getSessionCode());
                    if (c != null)
                    {
                        c.MembersSort();
                        refreshMembers(s, c.MembersGet());
                    }
                    break;
                }
                case AirSession.TYPE_DIALOG:
                {
                    s.MembersSort();
                    ArrayList<AirContact> mArrayList = new ArrayList<AirContact>();
                    mArrayList.add(AirtalkeeAccount.getInstance().getUser());
                    for (AirContact a : s.getMemberAll()) {
                        mArrayList.add(a);
                    }
                    refreshMembers(s, mArrayList);
                    break;
                }


            }
            // refreshMemberOnline(s.SessionPresenceList());
        }
        else
        {
            refreshMembers(null, null);
            // sessionBoxMember.refreshMemberOnline(null);
        }
    }

    /**
     * 刷新成员状态
     * @param session 会话Entity
     * @param members 成员列表
     */
    public void refreshMembers(AirSession session, List<AirContact> members)
    {
        try
        {
            adapterMember.notifyMember(session, members);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        updateMembers();
    }

    public void updateMembers(){
        adapterMember.notifyDataSetChanged();
    }
    private AirSession getSession(String sessionCode){

        isChannel = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode).getType() == AirSession.TYPE_CHANNEL;
        if (isChannel)
        {
            session = AirSessionControl.getInstance().getCurrentChannelSession();
        }
        else
        {
            session = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode);
        }
        return session;
    }

    @Override
    public void onChannelMemberDeleteNotify(AirChannel ch, List<AirContact> members) {
        super.onChannelMemberDeleteNotify(ch, members);
        refreshMembers(ch);


    }

    @Override
    public void onChannelMemberAppendNotify(AirChannel ch, List<AirContact> members) {
        super.onChannelMemberAppendNotify(ch, members);
        refreshMembers(ch);
    }

    @Override
    public void onChannelMemberUpdateNotify(AirChannel ch, List<AirContact> members) {
        super.onChannelMemberUpdateNotify(ch, members);
        refreshMembers(ch);
    }

    @Override
    public void onChannelMemberListGet(String channelId, List<AirContact> members) {
        super.onChannelMemberListGet(channelId, members);
        refreshMembers(getSession(channelId),members);


    }

    @Override
    public void onContactPresence(boolean isSubscribed, HashMap<String, Integer> presenceMap)
    {
        if (session != null && session.getType() == AirSession.TYPE_DIALOG)
        {
            session.MembersSort();
        }
        adapterMember.notifyDataSetChanged();
    }

    @Override
    public void onContactPresence(boolean isSubscribed, String uid, int state)
    {
       if (session != null && session.getType() == AirSession.TYPE_DIALOG)
        {
            session.MembersSort();
        }
        adapterMember.notifyDataSetChanged();
    }



    /**
     * 刷新频道成员列表
     */
    @SuppressWarnings("unused")
    public void refreshMembers(AirChannel ch)
    {
        ch.MembersSort();
        if (ch != null)
            refreshMembers(ch.getSession(), ch.MembersGet());
        else
            refreshMembers(null, new ArrayList<AirContact>());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AirtalkeeContactPresence.getInstance().setContactPresenceListener(null);
        AirAccountManager.getInstance().setChannelListener(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_MENU:
                mOk.setBackgroundResource(R.drawable.bg_list_focuse);
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                return false;
            case KeyEvent.KEYCODE_BACK:
                mBack.setBackgroundResource(R.drawable.bg_list_focuse);
                break;
        }
        return super.onKeyDown(keyCode,event);
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_DPAD_CENTER:
                return false;
            case KeyEvent.KEYCODE_MENU:
                mOk.setBackgroundResource(R.drawable.bg_list_normal);
                if(session.getCaller()!=null && session.getType()== AirSession.TYPE_DIALOG && session.getCaller().equals(AirtalkeeAccount.getInstance().getUser())){
                    Intent it = new Intent(this, SessionAddActivity.class);
                    it.putExtra("sessionCode", session.getSessionCode());
                    it.putExtra("type", AirServices.TEMP_SESSION_TYPE_MESSAGE);
                    startActivity(it);
                }
                break;
            case KeyEvent.KEYCODE_BACK:
                mBack.setBackgroundResource(R.drawable.bg_list_normal);
                break;
        }
        return super.onKeyUp(keyCode, event);
    }


}

