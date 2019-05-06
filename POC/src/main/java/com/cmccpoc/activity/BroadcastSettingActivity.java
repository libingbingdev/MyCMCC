package com.cmccpoc.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.controller.AccountController;
import com.cmccpoc.R;
import com.cmccpoc.config.Config;

public class BroadcastSettingActivity extends Activity {
    private RadioGroup rgHeartBeat;
    private RadioButton mOpen, mClose;
    private Button mOk, mBack;
    private int BroadcastMode;

    @Override
    protected void onCreate(Bundle bundle) {
        // TODO Auto-generated method stub
        super.onCreate(bundle);
        setContentView(R.layout.activity_setting_broadcast);
        doInitView();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    /**
     * 初始化绑定控件Id
     */
    private void doInitView() {
        mOk = (Button) findViewById(R.id.ok);
        mBack = (Button) findViewById(R.id.back);
        rgHeartBeat = (RadioGroup) findViewById(R.id.rg_hb_frequence);
        mOpen = (RadioButton) findViewById(R.id.open_broadcast);
        mClose = (RadioButton) findViewById(R.id.close_broadcast);

        int openState = Config.getFuncPttBroadcast(this) ? 1 : 0;
        switch (openState) {
            case 0:
                mClose.setChecked(true);
                break;
            case 1:
                mOpen.setChecked(true);
                break;
        }
        rgHeartBeat.setOnCheckedChangeListener(listener);

    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub
        super.finish();
    }

    private RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int rid = group.getCheckedRadioButtonId();
            switch (rid) {
                case R.id.open_broadcast: {
                    if (mOpen.isChecked()) {
                        //BroadcastMode = 1;
                        AirtalkeeSessionManager.getInstance().GroupBroadcastRun();
                        setFuncPttBroadcast(true);
                        finish();
                    }
                    break;
                }
                case R.id.close_broadcast: {
                    if (mClose.isChecked()) {
                       // BroadcastMode = 0;
                        AirtalkeeSessionManager.getInstance().SessionBye(AirtalkeeSessionManager.getInstance().GroupBroadcastSession());
                        setFuncPttBroadcast(false);
                        finish();

                    }
                    break;
                }
                default:
                    break;
            }

        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
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
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                mOk.setBackgroundResource(R.drawable.bg_list_normal);
                int rbid= rgHeartBeat.getFocusedChild().getId();
                if (rbid == R.id.open_broadcast) {
                    AirtalkeeSessionManager.getInstance().GroupBroadcastRun();
                    setFuncPttBroadcast(true);
                    finish();
                } else if (rbid == R.id.close_broadcast){
                    AirtalkeeSessionManager.getInstance().SessionBye(AirtalkeeSessionManager.getInstance().GroupBroadcastSession());
                    setFuncPttBroadcast(false);
                    finish();
                }
                break;
            case KeyEvent.KEYCODE_BACK:
                finish();
                mBack.setBackgroundResource(R.drawable.bg_list_normal);
                break;
        }
        return super.onKeyUp(keyCode, event);
    }


    public void setFuncPttBroadcast(boolean pptfun){
        SharedPreferences sPreferences = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putBoolean("funcPttBroadcast", pptfun);
        editor.commit();
    }

}
