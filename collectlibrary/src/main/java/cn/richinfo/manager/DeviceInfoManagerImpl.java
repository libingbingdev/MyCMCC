package cn.richinfo.manager;

import java.util.List;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import com.dmyk.android.telephony.DmykAbsTelephonyManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Message;
import android.os.Looper;
import android.app.ActivityThread;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.text.TextUtils;
import android.os.Build;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.location.Criteria;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
public class DeviceInfoManagerImpl extends DeviceInfoManager {

    static final String TAG = "DeviceInfoManagerImpl";
    private DmykAbsTelephonyManager mDmykAbsTelephonyManager;

    public DeviceInfoManagerImpl(Context context) {
        super(context);
        mDmykAbsTelephonyManager = DmykAbsTelephonyManager.getDefault(context);
    }

    /** Network type is unknown */
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    /** Current network is GPRS */
    public static final int NETWORK_TYPE_GPRS = 1;
    /** Current network is EDGE */
    public static final int NETWORK_TYPE_EDGE = 2;
    /** Current network is UMTS */
    public static final int NETWORK_TYPE_UMTS = 3;
    /** Current network is CDMA: Either IS95A or IS95B */
    public static final int NETWORK_TYPE_CDMA = 4;
    /** Current network is EVDO revision 0 */
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    /** Current network is EVDO revision A */
    public static final int NETWORK_TYPE_EVDO_A = 6;
    /** Current network is 1xRTT */
    public static final int NETWORK_TYPE_1xRTT = 7;
    /** Current network is HSDPA */
    public static final int NETWORK_TYPE_HSDPA = 8;
    /** Current network is HSUPA */
    public static final int NETWORK_TYPE_HSUPA = 9;
    /** Current network is HSPA */
    public static final int NETWORK_TYPE_HSPA = 10;
    /** Current network is iDen */
    public static final int NETWORK_TYPE_IDEN = 11;
    /** Current network is EVDO revision B */
    public static final int NETWORK_TYPE_EVDO_B = 12;
    /** Current network is LTE */
    public static final int NETWORK_TYPE_LTE = 13;
    /** Current network is eHRPD */
    public static final int NETWORK_TYPE_EHRPD = 14;
    /** Current network is HSPA+ */
    public static final int NETWORK_TYPE_HSPAP = 15;
    /** Current network is GSM */
    public static final int NETWORK_TYPE_GSM = 16;
    public static final int NETWORK_TYPE_TD_SCDMA = 17;
    /** Current network is IWLAN */
    public static final int NETWORK_TYPE_IWLAN = 18;

    public static final int NETWORK_TYPE_LTE_CA = 19;
    private static String PROP_MT_ROM_SIZE = "ro.mt.hardware.rom.size";
    private static String PROP_MT_RAM_SIZE = "ro.mt.hardware.ram.size";
    private static String PROP_MT_DEVICE_SW_NAME = "ro.mt.device.software.name";
    private static String PROP_MT_DEVICE_SW_VERSIOM = "ro.mt.device.software.version";
    private static String S_NETWORK_TYPE_4G = "4g";
    private static String S_NETWORK_TYPE_3G = "3g";
    private static String S_NETWORK_TYPE_2G = "2g";
    private static String S_NETWORK_TYPE_WIFI = "wifi";
    @Override
    public String getDeviceSoftwareName(){
        String mDeviceSoftName = SystemProperties.get(PROP_MT_DEVICE_SW_NAME);
        Log.d(TAG, "getDeviceSoftwareName : " + mDeviceSoftName);
        return mDeviceSoftName;
    }

    @Override
    public String getNetAccount(){
        Log.d(TAG, "getNetAccount is unSupport");
        return "unSupport";
    }

    @Override
    public LocationMsg getLocation() {
        Log.d(TAG, "getLocationMsg Enter mLocationMsg: " + mLocationMsg.latitude + ", longitude: " + mLocationMsg.longitude + ", addrFrom: " + mLocationMsg.locationMode);
        return mLocationMsg;
    }

    @Override
    public String getNetworkType() {
        ConnectivityManager mConnectivityManager =(ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        if(info == null){
            return "unKnown";
        }//return wifi
        else if(info.getType() == ConnectivityManager.TYPE_WIFI){
            Log.d(TAG, "getNetType : " + S_NETWORK_TYPE_WIFI);
            return S_NETWORK_TYPE_WIFI;
        }
        // return '4g','3g','2g'
        int phoneId = mDmykAbsTelephonyManager.getMasterPhoneId();
        int mNetworkType = mDmykAbsTelephonyManager.getNetworkType(phoneId);
        String mNetTpeStr = null;

        switch(mNetworkType){
            case NETWORK_TYPE_LTE:
            case NETWORK_TYPE_IWLAN:
            case NETWORK_TYPE_LTE_CA:
                mNetTpeStr = S_NETWORK_TYPE_4G;
                break;
            case NETWORK_TYPE_UMTS:
            case NETWORK_TYPE_EVDO_0:
            case NETWORK_TYPE_EVDO_A:
            case NETWORK_TYPE_HSDPA:
            case NETWORK_TYPE_HSUPA:
            case NETWORK_TYPE_HSPA:
            case NETWORK_TYPE_EVDO_B:
            case NETWORK_TYPE_EHRPD:
            case NETWORK_TYPE_HSPAP:
            case NETWORK_TYPE_TD_SCDMA:
                mNetTpeStr = S_NETWORK_TYPE_3G;
                break;
            case NETWORK_TYPE_GPRS:
            case NETWORK_TYPE_EDGE:
            case NETWORK_TYPE_CDMA:
            case NETWORK_TYPE_1xRTT:
            case NETWORK_TYPE_IDEN:
            case NETWORK_TYPE_GSM:
                mNetTpeStr =  S_NETWORK_TYPE_2G;
                break;
            case NETWORK_TYPE_UNKNOWN:
            default:
                mNetTpeStr = "unKnown";
                break;
        }
        Log.d(TAG, "getNetType: " + mNetTpeStr);
        return mNetTpeStr;
    }

    @Override
    public String getDeviceSoftwareVersion() {
        String mDeviceSoftVersion = SystemProperties.get(PROP_MT_DEVICE_SW_VERSIOM);
        Log.d(TAG, "getDeviceSoftwareVersion: " + mDeviceSoftVersion);
        return mDeviceSoftVersion;

    }

    @Override
    public int getVoLTEState() {
        int phoneId = mDmykAbsTelephonyManager.getMasterPhoneId();
        int mVolteState = mDmykAbsTelephonyManager.getVoLTEState(phoneId);
        Log.d(TAG, "original getVoLTEState: " + mVolteState);

        if(mDmykAbsTelephonyManager.VOLTE_STATE_UNKNOWN == mVolteState){
            Log.d(TAG, "warning: error volte state : " + mVolteState);
            mVolteState = 0;
        }else if(1 == mVolteState){
            mVolteState = 0; //0 on
        }else{
            mVolteState = 1;  //1 off
        }
        return mVolteState;
    }

    @Override
    public String getRomStorageSize(){
        String romSize = SystemProperties.get(PROP_MT_ROM_SIZE);
        Log.d(TAG, "getRomStorageSize: " + romSize);
        if (romSize.isEmpty()) {
            return "ro.mt.hardware.rom.size null";
        }
        return romSize;
    }

    @Override
    public String getRamStorageSize(){
        String ramSize = SystemProperties.get(PROP_MT_RAM_SIZE);
        Log.d(TAG, "getRamStorageSize: " + ramSize);
        if (ramSize.isEmpty()) {
            return "ro.mt.hardware.ram.size null";
        }
        return ramSize;
    }

    @Override
    public String getMacAddress(){
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        boolean wifiIsEnabled = wifiManager.isWifiEnabled();
        if(wifiIsEnabled){
            String macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
            Log.d(TAG, "getMacAddress: " + macAddress);
            return !TextUtils.isEmpty(macAddress) ? macAddress : "unKnown";
        }
        Log.d(TAG, "getMacAddress: unKnown");
        return "unKnown";
    }

    @Override
    public String getCPUModel(){
        String str = "unKnown";
        try
        {
            str = getCpuHardwareByFile();
            if (TextUtils.isEmpty(str)) {
                str = Build.HARDWARE;
            }
        }
        catch (Exception localException)
        {
            str = Build.HARDWARE;
        }
        Log.d(TAG, "getCPUModel: " + str);
        return str;
    }

    @Override
    public String getOSVersion(){
        String mOSVersion = Build.VERSION.RELEASE;
        Log.d(TAG, "getOSVersion: " + mOSVersion);
        return mOSVersion;
    }

    @Override
    public String getPhoneNumber(){
        int phoneId = mDmykAbsTelephonyManager.getMasterPhoneId();
        String phoneNum = mDmykAbsTelephonyManager.getPhoneNumber(phoneId);
        Log.d(TAG, "getPhoneNumber: " + phoneNum + " MasterphoneId: " + phoneId);
        if(null == phoneNum || "".equals(phoneNum)){
            return "unKnown";
        }
        return phoneNum;
    }

    private static String getCpuHardwareByFile(){
        HashMap localHashMap = new HashMap();
        Scanner localScanner = null;
        try
        {
            localScanner = new Scanner(new File("/proc/cpuinfo"));
            while (localScanner.hasNextLine())
            {
                String[] arrayOfString = localScanner.nextLine().split(": ");
                if (arrayOfString.length > 1) {
                    localHashMap.put(arrayOfString[0].trim(), arrayOfString[1].trim());
                }
            }
        }
        catch (Exception localException)
        {
            localException.printStackTrace();
        }
        finally
        {
            if (localScanner != null) {
                localScanner.close();
            }
        }
        return (String)localHashMap.get("Hardware");
    }
}
