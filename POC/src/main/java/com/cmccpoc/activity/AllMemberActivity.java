package com.cmccpoc.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnContactBlacklistListener;
import com.airtalkee.sdk.OnContactPresenceListener;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import android.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.BaseActivity;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.activity.home.MemberFragment;
import com.cmccpoc.activity.home.adapter.AdapterMemberAll;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.activity.home.widget.CallAlertDialog;
import com.cmccpoc.activity.home.widget.CallAlertDialog.OnAlertDialogCancelListener;
import com.cmccpoc.activity.home.widget.ToastUtils;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.listener.OnMmiChannelListener;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AllMemberActivity extends BaseActivity implements AdapterView.OnItemClickListener, AdapterMemberAll.CheckedCallBack ,OnContactPresenceListener {

    public interface MemberCheckListener

    {
        /**
         * 成员选择
         *
         * @param isChecked 是否选择
         */
        public void onMemberChecked(boolean isChecked);
    }

    private MemberCheckListener memberCheckListener;
    private ListView mAllMemberLv;
    public AdapterMemberAll adapterMember;
    public List<AirContact> memberAll;
    private List<AirContact> tempCallMembers = null;
    Map<String, AirContact> tempCallMembersCache = new TreeMap<String, AirContact>();
    private CallAlertDialog alertDialog;
    AlertDialog dialog;
    private Context mContext;
    private static final int DIALOG_CALL = 99;
    private PopupMenu mPop;
    private TextView mTitle;
    private static AllMemberActivity mInstance;
    public static AllMemberActivity getInstance()
    {
        return mInstance;
    }
    private Button mOk,mBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_member);
        mContext = this;
        mTitle=(TextView) findViewById(R.id.all_member_title);
        mAllMemberLv = (ListView) findViewById(R.id.all_member_listview);
        memberAll = getAllAirContacts();
        if(memberAll.size()>0){
            adapterMember = new AdapterMemberAll(this, null);
            adapterMember.notifyMember(memberAll);
            mAllMemberLv.setAdapter(adapterMember);
            adapterMember.notifyDataSetChanged();
            mAllMemberLv.setOnItemClickListener(this);
        }else{
            Util.Toast(this,getString(R.string.not_channel_session_hint));
        }
        mOk= (Button) findViewById(R.id.ok);
        mBack= (Button) findViewById(R.id.back);
        AirtalkeeContactPresence.getInstance().setContactPresenceListener(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMemberList();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        CheckBox cb = (CheckBox) view.findViewById(R.id.talk_cb_group_member);
        AirContact c = (AirContact) adapterMember.getItem(i);
        if (c != null)
        {
            if (!AirtalkeeAccount.getInstance().getUserId().equals(c.getIpocId()))
            {
                if (cb != null)
                    cb.setChecked(!cb.isChecked());
            }
        }

    }

    @Override
    public void onChecked(boolean isChecked) {
       /* if (memberCheckListener != null)
            memberCheckListener.onMemberChecked(isChecked);*/
    }

    /**
     * 获取全体成员 将所有频道内的成员取并集
     *
     * @return 全体成员列表
     */
    public List<AirContact> getAllAirContacts() {
        List<AirContact> contacts = new ArrayList<AirContact>();
        Map<String, AirContact> allMembers = new HashMap<String, AirContact>();
        List<AirChannel> channels = AirtalkeeChannel.getInstance().getChannels();
        if (channels != null && channels.size() > 0) {
            for (AirChannel channel : channels) {
                List<AirContact> members = channel.MembersGet();
                for (AirContact member : members) {
                   // Log.d("zlm","member.getState()=="+member.getState() +"member.chat=="+member.getStateInChat() +"id="+member.getDisplayName());
                    Log.d("zlm","id==="+AirtalkeeContactPresence.getInstance().getContactStateById(member.getIpocId()));

                    int state = AirtalkeeContactPresence.getInstance().getContactStateById(member.getIpocId());
                    if (state == AirContact.CONTACT_STATE_ONLINE_BG  || state == AirContact.CONTACT_STATE_ONLINE  ) {
                        allMembers.put(member.getIpocId(), member);
                    }
                }
            }
        }
        Iterator<Map.Entry<String, AirContact>> iter = allMembers.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, AirContact> entry = iter.next();
            contacts.add(entry.getValue());
        }
        Collections.sort(contacts, new Comparator<AirContact>() {
            @Override
            public int compare(AirContact member1, AirContact member2) {
                int result = 0;
                if (member1.chatSortSeed > member2.chatSortSeed)
                    result = -1;
                else if (member1.chatSortSeed < member2.chatSortSeed)
                    result = 1;
                return result;
            }
        });
        return contacts;
    }

    /**
     * 重置全体成员的选中状态
     */
    public void resetCheckBox() {
        if (adapterMember != null) {
            adapterMember.resetCheckBox();
        }
    }

    /**
     * 获取选择中的成员列表
     *
     * @return 成员列表
     */
    public List<AirContact> getSelectedMember() {
        if (adapterMember != null) {
            return adapterMember.getSelectedMemberList();
        }
        return null;
    }

    /**
     * 获取选择中的成员数
     *
     * @return 成员数
     */
    public int getSelectedMemberSize() {
        if (adapterMember != null) {
            return adapterMember.getSelectedMemberList().size();
        }
        return 0;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
       /* if(event.getKeyCode()==KeyEvent.KEYCODE_MENU){
            mPop=new PopupMenu(this,mTitle);
            mPop.getMenuInflater().inflate(R.menu.all_member_menu,mPop.getMenu());
            mPop.show();
            mPop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.selecte_list:
                            callSelectClean();
                            mPop.dismiss();
                            break;
                        case R.id.temp_talk:
                            Intent intent=new Intent(AllMemberActivity.this,HomeActivity.class);
                            startActivity(intent);
                            mPop.dismiss();
                            break;
                        case R.id.make_call:
                            callSelectMember(true);
                            mPop.dismiss();
                            break;
                    }
                    mPop.dismiss();
                    return true;
                }
            });

        }*/
        switch (keyCode){
            case KeyEvent.KEYCODE_MENU:

                mOk.setBackgroundResource(R.drawable.bg_list_focuse);
                break;
            case KeyEvent.KEYCODE_BACK:
                mBack.setBackgroundResource(R.drawable.bg_list_focuse);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_MENU:
                mOk.setBackgroundResource(R.drawable.bg_list_normal);
                callSelectMember(true);
                break;
            case KeyEvent.KEYCODE_BACK:
                mBack.setBackgroundResource(R.drawable.bg_list_normal);
                finish();
                break;
        }
        return super.onKeyUp(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mPop!=null)  mPop.dismiss();
        finish();
    }

    /**
     * 清除选中的成员
     */
    public void callSelectClean()
    {
        adapterMember.resetCheckBox();
    }


    /**
     * 呼叫选中的成员
     *
     * @param isCall 是否呼叫
     */
    public void callSelectMember(boolean isCall) {
        if ((adapterMember.getSelectedMemberList() != null && adapterMember.getSelectedMemberList().size() > 0) || getSelectedMemberSize() > 0) {
            if (tempCallMembers == null)
                tempCallMembers = new ArrayList<AirContact>();
            tempCallMembers.clear();
            tempCallMembersCache.clear();
            for (AirContact c : adapterMember.getSelectedMemberList()) {
                if (!TextUtils.equals(c.getIpocId(), AirtalkeeAccount.getInstance().getUserId())) {
                    if (!tempCallMembersCache.containsKey(c.getIpocId())) {
                        tempCallMembersCache.put(c.getIpocId(), c);
                        tempCallMembers.add(c);
                    }
                }
            }

            for (AirContact c : getSelectedMember()) {
                if (!TextUtils.equals(c.getIpocId(), AirtalkeeAccount.getInstance().getUserId())) {
                    if (!tempCallMembersCache.containsKey(c.getIpocId())) {
                        tempCallMembersCache.put(c.getIpocId(), c);
                        tempCallMembers.add(c);
                    }
                }
            }

            if (tempCallMembers.size() > 0) {
                if (AirtalkeeAccount.getInstance().isEngineRunning()) {
                    AirSession s = SessionController.SessionMatch(tempCallMembers);
                    if (isCall) {
                        alertDialog = new CallAlertDialog(mContext, "正在呼叫" + s.getDisplayName(), "请稍后...", s.getSessionCode(), DIALOG_CALL, false, new OnAlertDialogCancelListener() {
                            @Override
                            public void onDialogCancel(int reason) {
                                Log.i("zlm", "MemberFragment reason = " + reason);
                                switch (reason) {
                                    case AirSession.SESSION_RELEASE_REASON_NOTREACH:
                                        /*dialog = new AlertDialog(mContext, null, getString(R.string.talk_call_offline_tip), getString(R.string.talk_session_call_cancel), getString(R.string.talk_call_leave_msg), listener, reason);
                                        dialog.show();*/
                                        ToastUtils.showCustomImgToast(getString(R.string.other_outline),R.drawable.ic_out_line ,AllMemberActivity.this);
                                        break;
                                    case AirSession.SESSION_RELEASE_REASON_REJECTED:
                                        //Toast.makeText1(AirServices.getInstance(), "对方已拒接", Toast.LENGTH_SHORT).show();
                                        ToastUtils.showCustomImgToast(getString(R.string.other_refuse_call),R.drawable.ic_out_line ,AllMemberActivity.this);
                                        break;
                                    case AirSession.SESSION_RELEASE_REASON_ERROR:
                                        ToastUtils.showCustomImgToast(getString(R.string.call_limit),R.drawable.ic_warning ,AllMemberActivity.this);
                                        break;
                                    case AirSession.SESSION_RELEASE_REASON_BUSY:
                                        if(Toast.isDebug) Toast.makeText1(AirServices.getInstance(), "对方正在通话中，无法建立呼叫", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        });
                        alertDialog.show();
                    } else {
                        AirtalkeeSessionManager.getInstance().getSessionByCode(s.getSessionCode());
                        HomeActivity.getInstance().pageIndex = BaseActivity.PAGE_IM;
                        HomeActivity.getInstance().onViewChanged(s.getSessionCode());
                        HomeActivity.getInstance().panelCollapsed();
                    }

                } else {
                    Util.Toast(mContext, getString(R.string.talk_network_warning));
                }
            } else {
                Util.Toast(mContext, getString(R.string.talk_tip_session_call));
            }
        } else {
            Util.Toast(mContext, getString(R.string.talk_tip_session_call));
        }
    }


    private DialogListener listener = new DialogListener() {
        @Override
        public void onClickOk(int id, boolean isChecked)
        {

        }

        @Override
        public void onClickOk(int id, Object object)
        {
            //AirtalkeeMessage.getInstance().MessageRecordPlayStop();
            callSelectMember(false);
            callSelectClean();
        }

        @Override
        public void onClickCancel(int id)
        {

        }
    };


    @Override
    public void onChannelMemberListGet(String channelId, List<AirContact> members) {
        super.onChannelMemberListGet(channelId, members);
        refreshMemberList();

    }

    @Override
    public void onChannelMemberUpdateNotify(AirChannel ch, List<AirContact> members) {
        super.onChannelMemberUpdateNotify(ch, members);
        refreshMemberList();
    }

    @Override
    public void onChannelMemberAppendNotify(AirChannel ch, List<AirContact> members) {
        super.onChannelMemberAppendNotify(ch, members);
        refreshMemberList();
    }

    @Override
    public void onChannelMemberDeleteNotify(AirChannel ch, List<AirContact> members) {
        super.onChannelMemberDeleteNotify(ch, members);
        refreshMemberList();
    }

    public void refreshMemberList(){
        getAllAirContacts();
        adapterMember.notifyDataSetChanged();
    }

    @Override
    public void onContactPresence(boolean b, HashMap<String, Integer> hashMap) {
        refreshMemberList();
    }

    @Override
    public void onContactPresence(boolean var1, String var2, int var3){
        refreshMemberList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AirtalkeeContactPresence.getInstance().setContactPresenceListener(null);
    }



}
