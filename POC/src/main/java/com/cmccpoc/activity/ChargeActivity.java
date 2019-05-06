package com.cmccpoc.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnSessionIncomingListener;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;

public class ChargeActivity extends Activity   {
    private ImageView iv_frame;
    private TextView tv_charge_state;
    private AnimationDrawable frameAnim;
    private BroadcastReceiver mPowerDisconnectReceiver = null;
    public final static String REMOVE_CHARGE = "com.android.action.remove.charge";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frameanim);
        iv_frame = (ImageView) findViewById(R.id.iv_frame);
        tv_charge_state = (TextView) findViewById(R.id.tv_charge_state);

        // 通过逐帧动画的资源文件获得AnimationDrawable示例
        //iv_frame.setImageResource(R.drawable.bullet_anim);
        frameAnim = (AnimationDrawable) getResources().getDrawable(R.drawable.bullet_anim);
        // 把AnimationDrawable设置为ImageView的背景
        iv_frame.setBackgroundDrawable(frameAnim);

        mPowerDisconnectReceiver = new PowerDisconnectReceiver();
        IntentFilter mIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mIntentFilter.addAction(REMOVE_CHARGE);
        registerReceiver(mPowerDisconnectReceiver, mIntentFilter);
        start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mPowerDisconnectReceiver);
    }

    /**
     * 开始播放
     */
    protected void start() {
        if (frameAnim != null && !frameAnim.isRunning()) {
            frameAnim.start();
        }
    }

    /**
     * 停止播放
     */
    protected void stop() {
        if (frameAnim != null && frameAnim.isRunning()) {
            frameAnim.stop();
        }
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        Log.d("zlm", "event.getKeyCode()=" + event.getKeyCode());
        if (!(event.getKeyCode() == KeyEvent.KEYCODE_POWER)) {
            stop();
            finish();
        }
        return false;
    }

    private int current;
    private int total;

    private class PowerDisconnectReceiver extends BroadcastReceiver {
        public void onReceive(Context content, Intent intent) {
            String mAction = intent.getAction();
            if (mAction.equals(REMOVE_CHARGE)) {
                stop();
                ChargeActivity.this.finish();
            } else {
                current = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                total = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                if (current * 100 / total == 100) {
                    tv_charge_state.setText(R.string.charge_finish);
                    stop();
                    iv_frame.setBackgroundDrawable(getResources().getDrawable(R.drawable.charge_05));
                } else {
                    start();
                    tv_charge_state.setText(R.string.charging);
                }
                int plugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                if (plugType == 0) {
                    stop();
                    ChargeActivity.this.finish();
                }
            }


        }
    }

}