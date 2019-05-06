package com.cmccpoc.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.BaseActivity;
import com.cmccpoc.activity.home.ChannelListActivity;
import com.cmccpoc.activity.home.MemberListActivity;
import com.cmccpoc.activity.home.ShowTestActivity;
import com.cmccpoc.activity.home.adapter.MainGridAdapter;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirSessionControl;


import java.util.ArrayList;

public class FirstActivity extends BaseActivity implements AdapterView.OnItemClickListener,View.OnClickListener {

    private ListView mMainGv;
    private MainGridAdapter mgvAdapter;
    private ImageView mMoreMenu;
    private int mList[]={R.string.channel_talk_text,R.string.temp_session_text,R.string.member_list_text,R.string.torch_tv_text};
    private AirSession session;
    private String mSessionId="";
    private String id="";
    private PopupMenu mPop;
    private boolean isopen = false;
    private Button mOk,mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(Config.screenOrientation);
        setContentView(R.layout.activity_first);
        ArrayList<String> list= initData(this,mList);
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
            Log.i("wqq","string:"+contexts.getString(arry[i]));
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
                intent.setClass(this, ChannelListActivity.class);
                startActivity(intent);
                break;
            case 1:
                intent.setClass(FirstActivity.this, AllMemberActivity.class);
                startActivity(intent);
                break;
            case 2:
                intent.setClass(FirstActivity.this, MemberListActivity.class);
                startActivity(intent);
                break;
            case 3:
                intent=new Intent(FirstActivity.this, LightActivity.class);
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

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getKeyCode()==KeyEvent.KEYCODE_MENU){
            mPop=new PopupMenu(this,mMoreMenu);
            mPop.getMenuInflater().inflate(R.menu.more_settings_menu_new,mPop.getMenu());
            mPop.show();
            mPop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent intent=new Intent();
                    switch (item.getItemId()){
                        case R.id.account_info:
                            intent.setClass(FirstActivity.this, MenuAccountActivity.class);
                            startActivity(intent);
                            mPop.dismiss();
                            break;
                        case R.id.finalport_status:
                            intent.setClass(FirstActivity.this,TerminalStatusActivity.class);
                            startActivity(intent);
                            mPop.dismiss();
                            break;
                        case R.id.net_settings:
                            intent.setAction(Intent.ACTION_MAIN);
                            intent.setComponent(new ComponentName("com.android.phone","com.android.phone.MobileNetworkSettings"));
                            startActivity(intent);
                            mPop.dismiss();
                            break;
                        case R.id.ppt_talk_settings:
                            intent.setClass(FirstActivity.this,PttSettingsActivity.class);
                            startActivity(intent);
                            mPop.dismiss();
                            break;
                        case R.id.backup_default:
                            intent.setAction(Intent.ACTION_MAIN);
                            intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$PrivacySettingsActivity"));
                            startActivity(intent);
                            mPop.dismiss();
                            break;
                        case R.id.about_version:
                            intent.setClass(FirstActivity.this,MenuAboutActivity.class);
                            startActivity(intent);
                            mPop.dismiss();
                            break;
                        case R.id.show_test_menu:
                            intent.setClass(FirstActivity.this,ShowTestActivity.class);
                            startActivity(intent);
                            mPop.dismiss();
                            break;
                    }
                    return true;
                }
            });
            return true;

        }else if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
           if(mPop!=null) mPop.dismiss();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_DPAD_CENTER:
                break;
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
        switch (keyCode){
            case KeyEvent.KEYCODE_MENU:
                mOk.setBackgroundResource(R.drawable.bg_list_normal);
                int position= mMainGv.getSelectedItemPosition();
                handleClick(position);
                Log.d("zlm","position="+position );
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                mOk.setBackgroundResource(R.drawable.bg_list_normal);
                break;
            case KeyEvent.KEYCODE_BACK:
                mBack.setBackgroundResource(R.drawable.bg_list_normal);
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
}
