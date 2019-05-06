package com.cmccpoc.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;

import com.cmccpoc.R;
import com.cmccpoc.activity.home.BaseActivity;
import com.cmccpoc.activity.home.adapter.PttSettingsAdapter;
import com.cmccpoc.config.Config;

import java.util.ArrayList;

public class NetWorkSettingsActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private ListView mPttSettingsLv;
    private ArrayList<String> mList=new ArrayList<String>();
    private int mData[]={R.string.First_network_type,R.string.linked_apn_name};
    private Button mOk,mBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(Config.screenOrientation);
        setContentView(R.layout.activity_ptt_settings);

        mList=initData(this,mData);
        mPttSettingsLv=(ListView) findViewById(R.id.ptt_settings_lv);
        mOk= (Button) findViewById(R.id.ok);
        mBack= (Button) findViewById(R.id.back);
        PttSettingsAdapter pttAdapter=new PttSettingsAdapter(this,mList);
        mPttSettingsLv.setAdapter(pttAdapter);
        mPttSettingsLv.setOnItemClickListener(this);

    }
    private ArrayList<String> initData(Context context,int data[]){
        ArrayList<String> datas=new ArrayList<String>();
        if(data.length>0){
            for(int i=0;i<data.length;i++){
                datas.add(context.getString(data[i]));
            }
        }
        return datas;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent=new Intent();
        switch (position){
            case 0:
                intent.setAction(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.android.phone","com.android.phone.CmccNetworkActivity"));
                startActivity(intent);

                break;
            
            case 1:
                intent.setAction(Intent.ACTION_MAIN);
               // intent.setComponent(new ComponentName("com.android.settings","com.android.settings.ApnSettings"));
                intent.setComponent(new ComponentName("com.android.settings","com.android.settings.CmccApnSettingActivity"));
                startActivity(intent);

                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("zlm","onKeyDown  ...keyCode=="+keyCode);
        switch (keyCode){
            case KeyEvent.KEYCODE_MENU:
                mOk.setBackgroundResource(R.drawable.bg_list_focuse);
                break;
            case KeyEvent.KEYCODE_BACK:
                mBack.setBackgroundResource(R.drawable.bg_list_focuse);
                break;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("zlm","keyCode=="+keyCode);
        switch (keyCode){
            case KeyEvent.KEYCODE_MENU:
                mOk.setBackgroundResource(R.drawable.bg_list_normal);
                break;
            case KeyEvent.KEYCODE_BACK:
                finish();
                mBack.setBackgroundResource(R.drawable.bg_list_normal);
                break;
        }
        return super.onKeyUp(keyCode, event);
    }


}
