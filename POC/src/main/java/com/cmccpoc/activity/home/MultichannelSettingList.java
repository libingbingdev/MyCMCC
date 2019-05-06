package com.cmccpoc.activity.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.activity.PttSettingsActivity;
import com.cmccpoc.activity.SettingActivity;
import com.cmccpoc.activity.home.adapter.AdapterMultichannel;
import com.cmccpoc.control.AirSessionControl;

import java.util.ArrayList;


public class MultichannelSettingList extends BaseActivity implements AdapterView.OnItemClickListener {
    private ListView mChannelList;
    private AdapterMultichannel adapterChannel;
    private Button mOk, mBack;
    String airchannel_id1, airchannel_id2;
    public ArrayList<String> oldairchannelList = new ArrayList<String>();
    public ArrayList<String> NewAirchannelList = new ArrayList<String>();
    private AirSession mCurrentSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multichannel_list);
        mChannelList = (ListView) findViewById(R.id.gv_channels);
        mOk = (Button) findViewById(R.id.ok);
        mBack = (Button) findViewById(R.id.back);
        mBack.setText(R.string.talk_no);
        mCurrentSession = AirSessionControl.getInstance().getCurrentSession();

        SharedPreferences sPreferences = getSharedPreferences("config", MODE_PRIVATE);
        airchannel_id1 = sPreferences.getString("airchannel_id1", "");
        if (!airchannel_id1.equals("")) {
            oldairchannelList.add(airchannel_id1);
            NewAirchannelList.add(airchannel_id1);
        }
        airchannel_id2 = sPreferences.getString("airchannel_id2", "");
        if (!airchannel_id2.equals("")) {
            oldairchannelList.add(airchannel_id2);
            NewAirchannelList.add(airchannel_id2);
        }
        Log.d("zlm", "username2=" + airchannel_id1);
        adapterChannel = new AdapterMultichannel(this, NewAirchannelList);
        mChannelList.setAdapter(adapterChannel);
        mChannelList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.cv_check);
        String id = ((AirChannel) adapterChannel.getItem(i)).getId();
        if (mCheckBox.isChecked()) {
            NewAirchannelList.remove(id);
        } else {
            NewAirchannelList.add(id);
        }
        mCheckBox.setChecked(!mCheckBox.isChecked());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AirtalkeeContactPresence.getInstance().setContactPresenceListener(null);
        AirSessionControl.getInstance().setOnMmiSessionListener(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                saveAirchannelId();
                mOk.setBackgroundResource(R.drawable.bg_list_focuse);
                break;
            case KeyEvent.KEYCODE_BACK:
                finish();
                mBack.setBackgroundResource(R.drawable.bg_list_focuse);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                mOk.setBackgroundResource(R.drawable.bg_list_normal);
                Intent intent=new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setClass(MultichannelSettingList.this,HomeActivity.class);
                startActivity(intent);
                finish();
                break;
            case KeyEvent.KEYCODE_BACK:
                mBack.setBackgroundResource(R.drawable.bg_list_normal);
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void saveAirchannelId() {
        SharedPreferences sPreferences = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = sPreferences.edit();
        for (String temp : NewAirchannelList) {
            if (!oldairchannelList.contains(temp)) { //add
                if (mCurrentSession != null && mCurrentSession.getSessionCode().equals(temp) && mCurrentSession.getType() == AirSession.TYPE_CHANNEL) {
                    // 当前所在频道
                } else {
                    Log.d("zlm", "SessionChannelIn=" + temp);
                    AirSessionControl.getInstance().SessionChannelIn(temp, false);
                }
            }
        }
        for (String temp : oldairchannelList) {
            if (!NewAirchannelList.contains(temp)) { //remove
                if (mCurrentSession != null && mCurrentSession.getSessionCode().equals(temp) && mCurrentSession.getType() == AirSession.TYPE_CHANNEL) {
                    // 当前所在频道
                } else {
                    Log.d("zlm", "SessionChannelOut=" + temp);
                    AirSessionControl.getInstance().SessionChannelOut(temp);
                }
            }
        }
        if (NewAirchannelList.size() == 0) {
            airchannel_id1 = airchannel_id2 = "";
        } else if (NewAirchannelList.size() == 1) {
            airchannel_id1 = getUseAirchannelID(NewAirchannelList.get(0));
            airchannel_id2 = "";
        } else {
            airchannel_id1 =getUseAirchannelID( NewAirchannelList.get(0));
            airchannel_id2 =getUseAirchannelID( NewAirchannelList.get(1));
        }
        editor.putString("airchannel_id1", airchannel_id1);
        editor.putString("airchannel_id2", airchannel_id2);
        editor.commit();
    }

    private String  getUseAirchannelID(String id ){
        for(AirChannel airChannel: AirtalkeeChannel.getInstance().getChannels()){
            if(airChannel.getId().equals(id)){
                return id;
            }
        }
        return "";
    }


}
