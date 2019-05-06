package com.cmccpoc.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.cmccpoc.activity.ChargeActivity;

/**
 * 监听usb
 *
 * @author Yao
 */
public class ReceiverSprdUsb extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Log.d("zlm", "action=" + action);
        // TODO Auto-generated method stub
        if (action != null && action.equals("android.hardware.usb.action.USB_STATE")) {
            if (intent.getBooleanExtra("connected", false)) {
                if (SystemProperties.get("persist.sys.sprd.mtbf", "1").equals("0")) {
                    return;
                }
                ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                String currentPackageName = "";
                if (manager != null) {
                    currentPackageName = manager.getRunningTasks(1).get(0).topActivity.getPackageName();
                }
                if (AirtalkeeAccount.getInstance().isAccountRunning() && !currentPackageName.contains("factorymode")) {
                    startChargeView(context);
                }

            }
        }
    }

    private void startChargeView(Context context) {
        Intent mIntent = new Intent();
        mIntent.setClass(context, ChargeActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(mIntent);
    }


}
