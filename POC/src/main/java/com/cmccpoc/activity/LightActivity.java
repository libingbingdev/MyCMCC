package com.cmccpoc.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ImageView;

import com.cmccpoc.R;
import com.cmccpoc.activity.home.BaseActivity;
import com.cmccpoc.config.Config;

public class LightActivity extends BaseActivity {


    private Button mOk, mBack;
    private ImageView iv_light_state;
    private static Camera camera;
    private static boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(Config.screenOrientation);
        setContentView(R.layout.activity_light);


        mOk = (Button) findViewById(R.id.ok);
        mBack = (Button) findViewById(R.id.back);
        iv_light_state = (ImageView) findViewById(R.id.iv_light_state);
        updateLightUi();

    }

    private void updateLightUi() {
        if (isOpen == false) {
            mOk.setText("打开");
            iv_light_state.setImageResource(R.drawable.ic_light_close);
        } else {
            mOk.setText("关闭");
            iv_light_state.setImageResource(R.drawable.ic_light_open);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_DPAD_CENTER:
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
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                Log.d("zlm","sendBroadcast");//
                mOk.setBackgroundResource(R.drawable.bg_list_normal);
                sendLightBroadcast();
                updateLightUi();
                break;
            case KeyEvent.KEYCODE_BACK:
                mBack.setBackgroundResource(R.drawable.bg_list_normal);
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void sendLightBroadcast() {
        Intent ledIntent=new Intent("intent.open.flashlight");
        if (!isOpen) {
            isOpen = true;
        } else {
            isOpen = false;
        }
        Log.d("zlm","sendBroadcast..isOpen="+isOpen);
        ledIntent.putExtra("flash.status",isOpen);
        sendBroadcast(ledIntent);
    }
}