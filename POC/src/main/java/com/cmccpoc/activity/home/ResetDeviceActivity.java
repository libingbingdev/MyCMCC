package com.cmccpoc.activity.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cmccpoc.R;
import com.cmccpoc.config.Config;

public class ResetDeviceActivity extends BaseActivity  {
    private Button mOk,mBack;
    TextView tvResetState;
    private boolean isResetWaiting=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(Config.screenOrientation);
        setContentView(R.layout.activity_reset_device);

        mOk= (Button) findViewById(R.id.ok);
        mBack= (Button) findViewById(R.id.back);
        tvResetState= (TextView) findViewById(R.id.tv_reset_mind);
        mOk.setText(R.string.talk_dialog_yes);
        mBack.setText(R.string.talk_dialog_no);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_MENU:
                if(!isResetWaiting) {
                    mOk.setBackgroundResource(R.drawable.bg_list_focuse);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                break;
            case KeyEvent.KEYCODE_BACK:
                if(!isResetWaiting) {
                    mBack.setBackgroundResource(R.drawable.bg_list_focuse);
                }
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
                if(!isResetWaiting) {
                    mOk.setBackgroundResource(R.drawable.bg_list_normal);
                    tvResetState.setText(R.string.reseting_mind);
                    mHandler.sendEmptyMessageDelayed(0x01, 2000);
                    isResetWaiting = true;
                    mOk.setVisibility(View.GONE);
                    mBack.setVisibility(View.GONE);
                }
                break;
            case KeyEvent.KEYCODE_BACK:
                if(!isResetWaiting) {
                    finish();
                    mBack.setBackgroundResource(R.drawable.bg_list_normal);
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }



    private  Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            sendBroadcast(new Intent("android.intent.action.ACTION_RESET_DEVICE"));
        }
    };

}
