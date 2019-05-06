package com.cmccpoc.activity;

import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cmccpoc.R;
import com.cmccpoc.auth.AuthSso;
import com.cmccpoc.config.Config;
import com.cmccpoc.util.Util;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.provider.Settings.Secure;

public class TerminalStatusActivity extends ActivityBase {
    private TextView mDeviceModel,mOperate,mSignalStrength,mMobileNetModel,mMobileNetStstus,mPhoneNumber,mImeiInfo,mSerialNumber;
    private Button mOk,mBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(Config.screenOrientation);
        setContentView(R.layout.activity_terminal_status);
        initview();
        getCurrentNetDBM(this);
    }

    private void initview(){
        mOk= (Button) findViewById(R.id.ok);
        mBack= (Button) findViewById(R.id.back);
        mOk.setVisibility(View.GONE);

        mDeviceModel=(TextView) findViewById(R.id.port_model_summary);
        mDeviceModel.setText(android.os.Build.MODEL);

        mOperate=(TextView) findViewById(R.id.net_operate_summary);
        mOperate.setText(Util.getOperator(this));

        mSignalStrength=(TextView) findViewById(R.id.signal_strength_summary);

        mMobileNetModel=(TextView) findViewById(R.id.mobile_network_model_summary);
        mMobileNetModel.setText(Util.getCurrentNetType());

        mMobileNetStstus=(TextView) findViewById(R.id.mobile_network_status_summary);
        if(Util.isWiFiConnected(this) && Util.isWifiDataEnable(this)){
            mMobileNetStstus.setText(this.getText(R.string.network_connected));
        }else if(Util.isMobileDataEnable(this) && !Util.isWiFiConnected(this)){
            mMobileNetStstus.setText(this.getText(R.string.network_connected));
        }else{
            mMobileNetStstus.setText(this.getText(R.string.network_no_connect));
        }

        mPhoneNumber=(TextView) findViewById(R.id.phone_number_summary);
        mPhoneNumber.setText(AuthSso.getInstance().mPhoneId);

        mImeiInfo=(TextView) findViewById(R.id.imei_info_summary);
        mImeiInfo.setText(Util.getImei(this));

        mSerialNumber=(TextView) findViewById(R.id.serial_nember_summary);
        mSerialNumber.setText(Secure.getString(getContentResolver(), Secure.ANDROID_ID));
    }

    private void getCurrentNetDBM(Context context) {

        final TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        PhoneStateListener mylistener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                String signalInfo = signalStrength.toString();
                String[] params = signalInfo.split(" ");


                if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                    //4G网络 最佳范围   >-90dBm 越大越好
                    int Itedbm = Integer.parseInt(params[9]);
                    mSignalStrength.setText(Itedbm + "dBm");

                } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS) {
                    //3G网络最佳范围  >-90dBm  越大越好  ps:中国移动3G获取不到  返回的无效dbm值是正数（85dbm）
                    //在这个范围的已经确定是3G，但不同运营商的3G有不同的获取方法，故在此需做判断 判断运营商与网络类型的工具类在最下方
                    String yys = Util.getOperator(getApplication());//获取当前运营商
                    if (yys=="CMCC") {
                        mSignalStrength.setText(0 + "dBm");//中国移动3G不可获取，故在此返回0
                    } else if (yys=="CUCC") {
                        int cdmaDbm = signalStrength.getCdmaDbm();
                        mSignalStrength.setText(cdmaDbm + "dBm");
                    } else if (yys == "CTCC") {
                        int evdoDbm = signalStrength.getEvdoDbm();
                        mSignalStrength.setText(evdoDbm + "dBm");
                    }

                } else {
                    //2G网络最佳范围>-90dBm 越大越好
                    int asu = signalStrength.getGsmSignalStrength();
                    int dbm = -113 + 2 * asu;
                    mSignalStrength.setText(dbm + "dBm");
                }

            }
        };
        //开始监听
        tm.listen(mylistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
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
