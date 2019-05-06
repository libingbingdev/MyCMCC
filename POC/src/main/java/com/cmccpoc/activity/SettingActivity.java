package com.cmccpoc.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.BaseActivity;
import com.cmccpoc.activity.home.ChannelListActivity;
import com.cmccpoc.activity.home.MemberListActivity;
import com.cmccpoc.activity.home.MultichannelSettingActivity;
import com.cmccpoc.activity.home.ResetDeviceActivity;
import com.cmccpoc.activity.home.ShowTestActivity;
import com.cmccpoc.activity.home.adapter.MainGridAdapter;
import com.cmccpoc.config.Config;

import java.util.ArrayList;

public class SettingActivity extends BaseActivity implements AdapterView.OnItemClickListener,View.OnClickListener {

    private ListView mMainGv;
    private MainGridAdapter mgvAdapter;
    private ImageView mMoreMenu;
    private int mList[]={R.string.more_account_info,R.string.more_finalport_status,R.string.more_net_settings,R.string.more_ptt_talk_settings,
            R.string.more_channel_listen_setting,R.string.broadcast_listen_setting,R.string.more_backup,R.string.more_about_version};
    private int mCtaList[]={R.string.more_account_info,R.string.more_finalport_status,R.string.more_net_settings,R.string.more_ptt_talk_settings,
            R.string.more_channel_listen_setting,R.string.broadcast_listen_setting,R.string.more_backup,R.string.more_about_version ,R.string.location_setting ,R.string.blues_setting};
    private Button mOk,mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(Config.screenOrientation);
        setContentView(R.layout.activity_first);
        ArrayList<String> list;
        Log.d("zlmmm","==="+SystemProperties.get("ro.build.display.id"));
        if(SystemProperties.get("ro.build.display.id").contains("CTA")){
            list = initData(this,mCtaList);
        }else{
            list= initData(this,mList);
        }
        mMoreMenu=(ImageView) findViewById(R.id.more_bt);
        mMoreMenu.setOnClickListener(this);
        mMainGv=(ListView) findViewById(R.id.main_listview);
        mMainGv.setVerticalScrollBarEnabled(false);
        mgvAdapter=new MainGridAdapter(this,list);
        mMainGv.setAdapter(mgvAdapter);
        mMainGv.setOnItemClickListener(this);
        mOk= (Button) findViewById(R.id.ok);
        mBack= (Button) findViewById(R.id.back);


    }

    private ArrayList<String> initData(Context contexts,int[] arry){
        ArrayList<String> arrayList=null;
        arrayList=new ArrayList<String>();
        for(int i=0;i< arry.length;i++){
            arrayList.add(contexts.getString(arry[i]));
        }
        return arrayList;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        handleClick(i);

    }

    private void handleClick(int i) {
        Intent intent=new Intent();
        switch (i){
            case 0:
                intent.setClass(SettingActivity.this, MenuAccountActivity.class);
                startActivity(intent);
                break;
            case 1:
                intent.setClass(SettingActivity.this,TerminalStatusActivity.class);
                startActivity(intent);
                break;
            case 2:
               // intent.setAction(Intent.ACTION_MAIN);
                //intent.setComponent(new ComponentName("com.android.phone","com.android.phone.MobileNetworkSettings"));
                intent.setClass(SettingActivity.this,NetWorkSettingsActivity.class);
                startActivity(intent);
                break;
            case 3:
                intent.setClass(SettingActivity.this,PttSettingsActivity.class);
                startActivity(intent);
                break;
            case 4:
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setClass(SettingActivity.this,MultichannelSettingActivity.class);
                startActivity(intent);
                break;
            case 5:
                intent.setClass(SettingActivity.this, BroadcastSettingActivity.class);
                startActivity(intent);
                break;
            case 6:
                intent.setClass(SettingActivity.this, ResetDeviceActivity.class);
                startActivity(intent);
                break;
            case 7:
                intent.setClass(SettingActivity.this,MenuAboutActivity.class);
                startActivity(intent);
                break;
            case 8:
                intent.setAction(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$LocationSettingsActivity"));
                startActivity(intent);
                break;
            case 9:
                 intent.setAction(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$BluetoothSettingsActivity"));
                startActivity(intent);
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.more_bt){
            ////
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
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
            case KeyEvent.KEYCODE_DPAD_CENTER:
                mOk.setBackgroundResource(R.drawable.bg_list_normal);
                break;
            case KeyEvent.KEYCODE_MENU:
                mOk.setBackgroundResource(R.drawable.bg_list_normal);
                int position= mMainGv.getSelectedItemPosition();
                handleClick(position);
                break;
            case KeyEvent.KEYCODE_BACK:
                mBack.setBackgroundResource(R.drawable.bg_list_normal);
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
}
