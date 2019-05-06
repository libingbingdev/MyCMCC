package com.hdqy.android.telephony;

import android.app.ActivityManager;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;

import com.dmyk.android.telephony.DmykAbsTelephonyManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HdqyInfoManagerImpl extends HdqyInfoManager {
    private static final String TAG = "HdqyInfoManagerImpl";
    public static final String UNSUPPORT = "unSupport";
    public static final String UNKNOWN = "unKnown";

    private DmykAbsTelephonyManager mDmykAbsTelephonyManager;

    public HdqyInfoManagerImpl(Context context) {
        super(context);
        mDmykAbsTelephonyManager = DmykAbsTelephonyManager.getDefault(context);
    }

    /**
     * Network type is unknown
     */
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    /**
     * Current network is GPRS
     */
    public static final int NETWORK_TYPE_GPRS = 1;
    /**
     * Current network is EDGE
     */
    public static final int NETWORK_TYPE_EDGE = 2;
    /**
     * Current network is UMTS
     */
    public static final int NETWORK_TYPE_UMTS = 3;
    /**
     * Current network is CDMA: Either IS95A or IS95B
     */
    public static final int NETWORK_TYPE_CDMA = 4;
    /**
     * Current network is EVDO revision 0
     */
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    /**
     * Current network is EVDO revision A
     */
    public static final int NETWORK_TYPE_EVDO_A = 6;
    /**
     * Current network is 1xRTT
     */
    public static final int NETWORK_TYPE_1xRTT = 7;
    /**
     * Current network is HSDPA
     */
    public static final int NETWORK_TYPE_HSDPA = 8;
    /**
     * Current network is HSUPA
     */
    public static final int NETWORK_TYPE_HSUPA = 9;
    /**
     * Current network is HSPA
     */
    public static final int NETWORK_TYPE_HSPA = 10;
    /**
     * Current network is iDen
     */
    public static final int NETWORK_TYPE_IDEN = 11;
    /**
     * Current network is EVDO revision B
     */
    public static final int NETWORK_TYPE_EVDO_B = 12;
    /**
     * Current network is LTE
     */
    public static final int NETWORK_TYPE_LTE = 13;
    /**
     * Current network is eHRPD
     */
    public static final int NETWORK_TYPE_EHRPD = 14;
    /**
     * Current network is HSPA+
     */
    public static final int NETWORK_TYPE_HSPAP = 15;
    /**
     * Current network is GSM
     */
    public static final int NETWORK_TYPE_GSM = 16;
    public static final int NETWORK_TYPE_TD_SCDMA = 17;
    /**
     * Current network is IWLAN
     */
    public static final int NETWORK_TYPE_IWLAN = 18;

    public static final int NETWORK_TYPE_LTE_CA = 19;

    private static final String PROP_MT_ROM_SIZE = "ro.mt.hardware.rom.size";
    private static final String PROP_MT_RAM_SIZE = "ro.mt.hardware.ram.size";
    private static final String PROP_MT_DEVICE_SW_NAME = "ro.mt.device.software.name";
    private static final String PROP_MT_DEVICE_SW_VERSIOM = "ro.mt.device.software.version";
    private static final String PROP_MT_PHONE_COUNT = "persist.msms.phone_count";
    private static final String PROP_MT_PRODUCT_MODEL = "ro.product.model";
    private static final String PROP_MT_PRODUCT_BRAND = "ro.product.brand";
    private static final String URI_APN_PERFER = "content://telephony/carriers/preferapn";
    private static final int S_NETWORK_TYPE_WIRED = 11;
    private static final int S_NETWORK_TYPE_5G = 5;
    private static final int S_NETWORK_TYPE_4G = 4;
    private static final int S_NETWORK_TYPE_3G = 3;
    private static final int S_NETWORK_TYPE_2G = 2;
    private static final int S_NETWORK_TYPE_WIFI = 1;
    private static final int S_NETWORK_TYPE_UNKNOWN = 0;

    @Override
    public String getDeviceId(int phoneId) {
        if (phoneId == 1) {
            return UNKNOWN;
        }
        String deviceid = "";
        try {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                deviceid = tm.getDeviceId();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return (!TextUtils.isEmpty(deviceid)) ? deviceid : UNKNOWN;
    }

    @Override
    public String getDeviceCMEI() {
        return UNSUPPORT;
    }

    @Override
    public String getSubscriberId(int phoneId) {
        /*String imsi = "";
        if (phoneId == 1) {
            return null;
        }
        if (!ishasSimCard(mContext)) {
            return null;
        }
        try {
            TelephonyManager mTelephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephonyMgr != null) {
                imsi = mTelephonyMgr.getSubscriberId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (!TextUtils.isEmpty(imsi)) ? imsi : null;*/
        return mDmykAbsTelephonyManager.getSubscriberId(phoneId);
    }

    @Override
    public String getIccId(int phoneId) {
        /*String icc = "";
        if (phoneId == 1) {
            return null;
        }
        if (!ishasSimCard(mContext)) {
            return null;
        }
        try {
            TelephonyManager mTelephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephonyMgr != null) {
                icc = mTelephonyMgr.getSimSerialNumber();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (!TextUtils.isEmpty(icc)) ? icc : null;*/
        return mDmykAbsTelephonyManager.getIccId(phoneId);
    }

    @Override
    public String getDeviceSoftwareVersion() {
        //return SystemProperties.get(PROP_MT_DEVICE_SW_VERSIOM, UNKNOWN);
        return mDmykAbsTelephonyManager.getDeviceSoftwareVersion();
    }

    @Override
    public int getCellId(int phoneId) {
        int cellid = -1;
        if (phoneId == 1) {
            return cellid;
        }
        if (!ishasSimCard(mContext)) {
            return cellid;
        }
        /*try {
            TelephonyManager mTelephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            GsmCellLocation location = null;
            if (mTelephonyMgr != null) {
                location = (GsmCellLocation) mTelephonyMgr.getCellLocation();
            }
            if (location != null) {
                cellid = location.getCid();//移动
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return mDmykAbsTelephonyManager.getCellId(phoneId);
    }


    /**
     * 判断是否包含SIM卡
     *
     * @return 状态
     */
    public static boolean ishasSimCard(Context context) {
        TelephonyManager telMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = 0;
        if (telMgr != null) {
            simState = telMgr.getSimState();
        }
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false;
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        return result;
    }

    @Override
    public int getMasterPhoneId() {
        /*if (!ishasSimCard(mContext)) {
            return -1; //不存在sim卡
        }
        return 0;*/
        return mDmykAbsTelephonyManager.getMasterPhoneId();
    }


    /**
     * @param simId 卡槽id
     * @return 返回值1代表SIM卡存在，反之不存在
     */
    public static int checkSimState(Context context, int simId) {
        int ret = 0;

        TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE + simId);
        if (telMgr == null) {
            telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telMgr == null) {
                return 0;
            }
        }
        if (telMgr.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
            ret = 1;
        }

        return ret;
    }


    @Override
    public String getRamStorageSize() {
        /*ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        if (am != null) {
            am.getMemoryInfo(mi);
        }
        String[] available = fileSize(mi.availMem);
        String[] total = fileSize(mi.totalMem);
        return total[0] + total[1];*/
        String ramSize = SystemProperties.get(PROP_MT_RAM_SIZE);
        Log.d(TAG, "getRamStorageSize: " + ramSize);
        if (ramSize.isEmpty()) {
            return "ro.mt.hardware.ram.size null";
        }
        return ramSize;
    }

    @Override
    public String getRomStorageSize() {
        /*File file = Environment.getDataDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long blockSize = statFs.getBlockSize();
        long totalBlocks = statFs.getBlockCount();
        long availableBlocks = statFs.getAvailableBlocks();
        String[] total = fileSize(totalBlocks * blockSize);
        String[] available = fileSize(availableBlocks * blockSize);
        return total[0] + total[1];*/
        String romSize = SystemProperties.get(PROP_MT_ROM_SIZE);
        Log.d(TAG, "getRomStorageSize: " + romSize);
        if (romSize.isEmpty()) {
            return "ro.mt.hardware.rom.size null";
        }
        return romSize;
    }

    @Override
    public String getMacAddress() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        boolean wifiIsEnabled = wifiManager.isWifiEnabled();
        if (wifiIsEnabled) {
            String macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
            Log.d(TAG, "getMacAddress: " + macAddress);
            return !TextUtils.isEmpty(macAddress) ? macAddress : UNKNOWN;
        }
        Log.d(TAG, "getMacAddress: unKnown");
        return UNKNOWN;
    }

    @Override
    public String getCPUModel() {
//        return Build.CPU_ABI;
        String str = UNKNOWN;
        try {
            str = getCpuHardwareByFile();
            if (TextUtils.isEmpty(str)) {
                str = Build.HARDWARE;
            }
        } catch (Exception localException) {
            str = Build.HARDWARE;
        }
        Log.d(TAG, "getCPUModel: " + str);
        return str;
    }

    private static String getCpuHardwareByFile() {
        HashMap localHashMap = new HashMap();
        Scanner localScanner = null;
        try {
            localScanner = new Scanner(new File("/proc/cpuinfo"));
            while (localScanner.hasNextLine()) {
                String[] arrayOfString = localScanner.nextLine().split(": ");
                if (arrayOfString.length > 1) {
                    localHashMap.put(arrayOfString[0].trim(), arrayOfString[1].trim());
                }
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        } finally {
            if (localScanner != null) {
                localScanner.close();
            }
        }
        return (String) localHashMap.get("Hardware");
    }

    @Override
    public String getOSVersion() {
        return "Android " + android.os.Build.VERSION.RELEASE;
    }

    @Override
    public Uri getAPNContentUri(int phoneId) {
        /*if (phoneId == 1) {
            return null;
        }
        int simCount = getPhoneCount();
        Uri PREFERAPN_URI = Uri.parse(URI_APN_PERFER);
        if (simCount == 1) {
            if (HdqyInfoManagerImpl.checkSimState(mContext, 1) == 0) {
                return null;
            } else {
                return PREFERAPN_URI;
            }
        } else if (simCount == 2) {
            if (HdqyInfoManagerImpl.checkSimState(mContext, phoneId) == 0) {
                return null;
            } else {
                return Uri.withAppendedPath(PREFERAPN_URI, "/subId/" + phoneId);
            }
        }*/
        return mDmykAbsTelephonyManager.getAPNContentUri(phoneId);
    }

    private static String[] fileSize(long size) {
        String str = "";
        if (size >= 1000) {
            str = "KB";
            size /= 1000;
            if (size >= 1000) {
                str = "MB";
                size /= 1000;
            }
        }
        /*将每3个数字用,分隔如:1,000*/
        DecimalFormat formatter = new DecimalFormat();
        formatter.setGroupingSize(3);
        String result[] = new String[2];
        result[0] = formatter.format(size);
        result[1] = str;
        return result;
    }

    @Override
    public int getPhoneCount() {
        String ENG_SIMTYPE = PROP_MT_PHONE_COUNT;
        String simCount = SystemProperties.get(ENG_SIMTYPE, "1");
        return Integer.parseInt(simCount);
    }

    @Override
    public String getPhoneNumber(int phoneId) {
        if (phoneId == 1) {
            return UNKNOWN;
        }
        phoneId = mDmykAbsTelephonyManager.getMasterPhoneId();
        String phoneNum = mDmykAbsTelephonyManager.getPhoneNumber(phoneId);
        Log.d(TAG, "getPhoneNumber: " + phoneNum + " MasterphoneId: " + phoneId);
        if (null == phoneNum || "".equals(phoneNum)) {
            return UNKNOWN;
        }
        return phoneNum;
    }

    @Override
    public int getNetChanel() {
        int strNetworkType = S_NETWORK_TYPE_UNKNOWN;
        NetworkInfo networkInfo;
        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            networkInfo = manager.getActiveNetworkInfo();
        } else {
            return S_NETWORK_TYPE_UNKNOWN;
        }
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return S_NETWORK_TYPE_WIFI;//wifi
            }
        }
        // return '4g','3g','2g'
        int phoneId = mDmykAbsTelephonyManager.getMasterPhoneId();
        int mNetworkType = mDmykAbsTelephonyManager.getNetworkType(phoneId);

        switch (mNetworkType) {
            case NETWORK_TYPE_LTE:
            case NETWORK_TYPE_IWLAN:
            case NETWORK_TYPE_LTE_CA:
                strNetworkType = S_NETWORK_TYPE_4G;
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
                strNetworkType = S_NETWORK_TYPE_3G;
                break;
            case NETWORK_TYPE_GPRS:
            case NETWORK_TYPE_EDGE:
            case NETWORK_TYPE_CDMA:
            case NETWORK_TYPE_1xRTT:
            case NETWORK_TYPE_IDEN:
            case NETWORK_TYPE_GSM:
                strNetworkType = S_NETWORK_TYPE_2G;
                break;
            case NETWORK_TYPE_UNKNOWN:
            default:
                strNetworkType = S_NETWORK_TYPE_UNKNOWN;
                break;
        }

        Log.e("GetNetworkType", "Network Type : " + strNetworkType);

        return strNetworkType;
    }

    @Override
    public int getMaxBatteryCapacity() {
        return 3700;
    }

    @Override
    public int getCurrentBatteryCapacity() {
        Object mPowerProfile;
        double batteryCapacity = 0;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class)
                    .newInstance(mContext);

            batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getBatteryCapacity")
                    .invoke(mPowerProfile);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return (int) batteryCapacity;
    }

    @Override
    public String getSnId() {
        return Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    public boolean isInternationalNetworkRoaming() {
        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = null;
        if (manager != null) {
            info = manager.getActiveNetworkInfo();
        }
        return info != null && info.isConnected() && info.isRoaming();
    }

    @Override
    public int getLac(int phoneId) {
        int tacOrLac = -1;
        if (phoneId == 1) {
            return tacOrLac;
        }
        if (!ishasSimCard(mContext)) {
            return tacOrLac;
        }
        /*TelephonyManager phoneManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            List<CellInfo> allCellinfo = null;
            if (phoneManager != null) {
                allCellinfo = phoneManager.getAllCellInfo();
            }
            if (allCellinfo != null) {
                CellInfo cellInfo = allCellinfo.get(0);
                if (cellInfo instanceof CellInfoGsm) {
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                    tacOrLac = cellInfoGsm.getCellIdentity().getLac();
                } else if (cellInfo instanceof CellInfoWcdma) {
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                    tacOrLac = cellInfoWcdma.getCellIdentity().getLac();

                } else if (cellInfo instanceof CellInfoLte) {
                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                    tacOrLac = cellInfoLte.getCellIdentity().getTac();
                }
            } else {//for older devices
                GsmCellLocation location = null;
                if (phoneManager != null) {
                    location = (GsmCellLocation) phoneManager.getCellLocation();
                }
                if (location != null) {
                    tacOrLac = location.getLac();
                }
            }
        } catch (Exception e) {
            GsmCellLocation location = (GsmCellLocation) phoneManager.getCellLocation();
            tacOrLac = location.getLac();
        }*/
        return mDmykAbsTelephonyManager.getLac(phoneId);
    }

    @Override
    public String getCTAModel() {
        return SystemProperties.get(PROP_MT_PRODUCT_MODEL, UNKNOWN);
    }

    @Override
    public String getBrand() {
        return SystemProperties.get(PROP_MT_PRODUCT_BRAND, UNKNOWN);
    }

    @Override
    public LocationMsg getLocation() {
        Log.d(TAG, "getLocationMsg Enter mLocationMsg: " + mLocationMsg.latitude + ", longitude: " + mLocationMsg.longitude + ", addrFrom: " + mLocationMsg.locationMode);
        return mLocationMsg;
    }

    /**
     * Reads a line from the specified file.
     *
     * @param filename the file to read from
     * @return the first line, if any.
     * @throws java.io.IOException if the file couldn't be read
     */
    public static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    public static String formatKernelVersion(String rawKernelVersion) {
        // Example (see tests for more):
        // Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com) \
        //     (gcc version 4.6.x-xxx 20120106 (prerelease) (GCC) ) #1 SMP PREEMPT \
        //     Thu Jun 28 11:02:39 PDT 2012
        final String PROC_VERSION_REGEX =
                "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
                        "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
                        "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
                        "(#\\d+) " +              /* group 3: "#1" */
                        "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
                        "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */

        Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
        if (!m.matches()) {
            Log.e("zlmm", "Regex did not match on /proc/version: " + rawKernelVersion);
            return "Unavailable";
        } else if (m.groupCount() < 4) {
            Log.e("zlmm", "Regex match on /proc/version only returned " + m.groupCount()
                    + " groups");
            return "Unavailable";
        }
        if (SystemProperties.get("ro.product.board.customer", "none").equalsIgnoreCase("cgmobile")) {
            //cg modify by xuyouqin start
            return m.group(1) + "\n" + // 3.0.31-g6fb96c9
                    m.group(3);// x@y.com #1

            //cg modify by xuyouqin end
        } else {
            return m.group(1) + "\n" + // 3.0.31-g6fb96c9
                    /*m.group(2) + " " + m.group(3) + "\n" +*/ // x@y.com #1
                    m.group(4); // Thu Jun 28 11:02:39 PDT 2012
        }

    }

    public static void  openOrCloseGPS(Context context, boolean open) {
        if (Build.VERSION.SDK_INT < 19) {
            Settings.Secure.setLocationProviderEnabled(context.getContentResolver(),
                    LocationManager.GPS_PROVIDER, open);
        } else {
            if (!open) {
                Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, android.provider.Settings.Secure.LOCATION_MODE_OFF);
            } else {
                Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, android.provider.Settings.Secure.LOCATION_MODE_BATTERY_SAVING);
            }
        }
    }

    public static boolean isOpenGPS(Context context, LocationManager myLocationManager) {
        if (Build.VERSION.SDK_INT < 19 && myLocationManager != null) {
            myLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (myLocationManager != null) {
                return myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }
        } else {
            int state = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
            return state != Settings.Secure.LOCATION_MODE_OFF;
        }
        return false;
    }

}
