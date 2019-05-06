package com.cmccpoc.activity.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.control.AirSessionControl;
import java.util.ArrayList;

public class MultichannelSettingActivity extends BaseActivity {
    private Button mOk, mBack;
    String airchannel_id1, airchannel_id2;
    public ArrayList<String> oldairchannelList = new ArrayList<String>();
    private AirSession mCurrentSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multichannel_settings);
        mOk = (Button) findViewById(R.id.ok);
        mBack = (Button) findViewById(R.id.back);
        mOk.setText(R.string.main_setting);
        mBack.setText(R.string.main_close);

        mCurrentSession = AirSessionControl.getInstance().getCurrentSession();
        SharedPreferences sPreferences = getSharedPreferences("config", MODE_PRIVATE);
        airchannel_id1 = sPreferences.getString("airchannel_id1", "");
        if (!airchannel_id1.equals("")) {
            oldairchannelList.add(airchannel_id1);
        }
        airchannel_id2 = sPreferences.getString("airchannel_id2", "");
        if (!airchannel_id2.equals("")) {
            oldairchannelList.add(airchannel_id2);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                mOk.setBackgroundResource(R.drawable.bg_list_focuse);
                break;
            case KeyEvent.KEYCODE_BACK:
                cancelAllListen();
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
                Intent intent = new Intent();
                intent.setClass(MultichannelSettingActivity.this, MultichannelSettingList.class);
                startActivity(intent);
                break;
            case KeyEvent.KEYCODE_BACK:
                mBack.setBackgroundResource(R.drawable.bg_list_normal);
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void cancelAllListen() {
        for (String temp : oldairchannelList) {
            if (mCurrentSession != null && mCurrentSession.getSessionCode().equals(temp) && mCurrentSession.getType() == AirSession.TYPE_CHANNEL) {
                // 当前所在频道
            } else {
                Log.d("zlm", "cancle...SessionChannelOut=" + temp);
                AirSessionControl.getInstance().SessionChannelOut(temp);
            }
        }
        SharedPreferences sPreferences = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = sPreferences.edit();
        airchannel_id1 = airchannel_id2 = "";
        editor.putString("airchannel_id1", airchannel_id1);
        editor.putString("airchannel_id2", airchannel_id2);
        editor.commit();
    }


}





