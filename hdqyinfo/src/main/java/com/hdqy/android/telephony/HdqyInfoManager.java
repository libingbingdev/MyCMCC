package com.hdqy.android.telephony;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;

import android.util.Log;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * 对部分设备不支持的功能属性，返回unSupport
 * 设备支持但当前没有值，则返回unKnown
 */
public abstract class HdqyInfoManager {
    private static final String TAG = "HdqyInfoManager";
    public static HdqyInfoManager sInstance = null;
    public final static String ACTION_SIM_STATE_CHANGED = "com.zqgb.android.telephony.action.SIM_STATE_CHANGED";
    public final static String ACTION_CONNECTIVITY_CHANGE = "android.zqgb.net.conn.CONNECTIVITY_CHANGE";
    public final static String ACTION_KEY_CLICK = "android.zqgb.net.conn.KEY_CLICK";
    protected static Context mContext;
    private LocationManager locationManager;
    protected static LocationMsg mLocationMsg;
    private List<String> providers;

    private final static int LOCATION_MODE_NOSUPPORT = 0;
    private final static int LOCATION_MODE_GPS = 1;
    private final static int LOCATION_MODE_BEIDOU = 2;
    private final static int LOCATION_MODE_GALILEO = 4;
    private final static int LOCATION_MODE_GLONASS = 8;
    private final static int LOCATION_MODE_BSS = 16;
    private final static int LOCATION_MODE_WIFI = 32;

    public static final int GPS_ONLY_MODE = 1;
    public static final int BDS_ONLY_MODE = 2;
    public static final int GALILEO_ONLY_MODE = 4;
    public static final int GLONASS_ONLY_MODE = 8;
    public static final int BSS_ONLY_MODE = 16;
    public static final int WIFI_ONLY_MODE = 32;
    public static final int GPS_GLONASS_MODE = GPS_ONLY_MODE + GLONASS_ONLY_MODE;
    public static final int GPS_BDS_MODE = GPS_ONLY_MODE + BDS_ONLY_MODE;
    public static final String GPS_CSR_CONFIG_FIL_FOR_GE2 = "/data/gnss/config/config.xml";
    private static String PROP_MT_GPS_SUPPORT = "ro.mt.gps.support";

    private HdqyInfoManager() {
        mContext = null;
    }

    protected HdqyInfoManager(Context context) {
        Context appContext = context.getApplicationContext();
        if (appContext != null) {
            mContext = appContext;
        } else {
            mContext = context;
        }
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        providers = locationManager.getProviders(true);
    }

    public static class LocationMsg {
        /**
         * 当前手机所在longitude，取值范围：-180 —— 180，东经为正
         */
        public double longitude;
        /**
         * 当前手机所在latitude，取值范围：-90 —— 90，北纬为正
         */
        public double latitude;
        /**
         * 获取位置信息的来源
         * 不支持定位:0
         * GPS:1
         * 北斗:2
         * 伽利略:4
         * 格洛纳斯:8
         * 基站定位:16
         * WiFi定位:32
         * 协同定位(协同定位类型为原生定位类型或运算)(例如:gps+北斗 值为 1|2 = 3;gps+北斗+伽利略 值为 1|2|4 = 7)
         */
        public int locationMode;
    }

    public void testHdqyInfoManager() {
        Log.d("zlmm", "getDeviceId(0) =" + sInstance.getDeviceId(0));
        Log.d("zlmm", "getDeviceCMEI =" + sInstance.getDeviceCMEI());
        Log.d("zlmm", "getSubscriberId(0) =" + sInstance.getSubscriberId(0));
        Log.d("zlmm", "getIccId =" + sInstance.getIccId(0));
        Log.d("zlmm", "getLac(0) =" + sInstance.getLac(0));
        Log.d("zlmm", "getRomStorageSize =" + sInstance.getRomStorageSize());
        Log.d("zlmm", "getRamStorageSize =" + sInstance.getRamStorageSize());
        Log.d("zlmm", "getCPUModel =" + sInstance.getCPUModel());
        Log.d("zlmm", "getOSVersion =" + sInstance.getOSVersion());
        Log.d("zlmm", "getPhoneNumber =" + sInstance.getPhoneNumber(0));
        Log.d("zlmm", "getNetChanel =" + sInstance.getNetChanel());
        Log.d("zlmm", "getSnId =" + sInstance.getSnId());
        Log.d("zlmm", "getMasterPhoneId =" + sInstance.getMasterPhoneId());
        Log.d("zlmm", "isInternationalNetworkRoaming =" + sInstance.isInternationalNetworkRoaming());
    }

    public class ApnMsg {
        /**
         * APN名称描述，如WAP设置
         */
        public String name;
        /**
         * MCC+MNC
         */
        public String numeric;
        /**
         * APN代理地址
         */
        public String proxy;
        /**
         * APN代理端口
         */
        public String port;
        /**
         * APN的名称，如cmwap
         */
        public String apn;
        /**
         * 用户名
         */
        public String user;
        /**
         * 密码
         */
        public String password;

        public ApnMsg() {
        }

        public ApnMsg(String name, String numeric, String proxy, String port, String apn, String user, String password) {
            this.name = name;
            this.numeric = numeric;
            this.proxy = proxy;
            this.port = port;
            this.apn = apn;
            this.user = user;
            this.password = password;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNumeric() {
            return numeric;
        }

        public void setNumeric(String numeric) {
            this.numeric = numeric;
        }

        public String getProxy() {
            return proxy;
        }

        public void setProxy(String proxy) {
            this.proxy = proxy;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getApn() {
            return apn;
        }

        public void setApn(String apn) {
            this.apn = apn;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            return "ApnMsg{" +
                    "name='" + name + '\'' +
                    ", numeric='" + numeric + '\'' +
                    ", proxy='" + proxy + '\'' +
                    ", port='" + port + '\'' +
                    ", apn='" + apn + '\'' +
                    ", user='" + user + '\'' +
                    ", password='" + password + '\'' +
                    '}';
        }
    }

    /**
     * 获取终端卡槽数量
     */
    public abstract int getPhoneCount();

    /**
     * 获取设备唯一标识IMEI
     * 权限要求：android.permission.READ_PHONE_STATE
     *
     * @param phoneId,卡槽位置ID，1个卡槽位置ID默认为0，2个卡槽位置ID分别为0和1
     * @return 设备唯一标识IMEI
     */
    public abstract String getDeviceId(int phoneId);

    /**
     * 有卡槽的设备这里可返回默认的unSupport
     * 获取设备唯一标识CMEI.CMEI是移动参考imei生成规则自定义的一套设备标识，主要用于无卡槽的终端产品。
     *
     * @return unSupport
     */
    public abstract String getDeviceCMEI();

    /**
     * 获取指定卡槽的SIM卡签约IMSI号
     * android.permission.READ_PHONE_STATE
     *
     * @param phoneId 卡槽位置ID，1个卡槽位置ID默认为0，2个卡槽位置ID分别为0和1
     * @return SIM卡IMSI号，如果对应卡槽不存在SIM卡，则返回空值
     */
    public abstract String getSubscriberId(int phoneId);

    /**
     * 获取指定卡槽的SIM卡签约ICCID号
     * android.permission.READ_PHONE_STATE
     *
     * @param phoneId 卡槽位置ID，1个卡槽位置ID默认为0，2个卡槽位置ID分别为0和1
     * @return SIM卡ICCID，如果对应卡槽不存在SIM卡，则返回空值
     */
    public abstract String getIccId(int phoneId);

    /**
     * @return 设备当前系统固件版本，非基带版本或内核版本及Android版本
     */
    public abstract String getDeviceSoftwareVersion();

    /**
     * 取对应卡槽的Cell-ID
     *
     * @param phoneId 卡槽位置ID，1个卡槽位置ID默认为0，2个卡槽位置ID分别为0和1
     * @return 对应卡槽的Cell-ID，如果对应卡槽未插SIM卡，则返回-1。若插入SIM卡为CDMA制式，则返回对应的Sid
     */
    public abstract int getCellId(int phoneId);

    /**
     * 获取对应卡槽的TAC/LAC信息
     *
     * @param phoneId 卡槽位置ID，1个卡槽位置ID默认为0，2个卡槽位置ID分别为0和1
     * @return 对应卡槽的TAC/LAC信息，如果对应卡槽未插SIM卡，则返回-1。若插入SIM卡为CDMA制式，则返回对应的Nid
     */
    public abstract int getLac(int phoneId);

    /**
     * @return 获取设备的入网型号, 须与工信部登记终端型号字段完全一致
     */
    public abstract String getCTAModel();

    /**
     * @return 获取设备的品牌
     */
    public abstract String getBrand();

    /**
     * 获取设备的ROM容量大小
     *
     * @return 返回设备的存储总容量（ROM）大小，与工信部登记终端ROM信息一致
     */
    public abstract String getRomStorageSize();

    /**
     * 获取设备的RAM容量大小
     *
     * @return 返回设备的内存总容量（RAM）大小，与工信部登记终端RAM信息一致
     */
    public abstract String getRamStorageSize();

    /**
     * 获取设备的WLAN MAC地址
     *
     * @return 返回设备的WLAN MAC地址
     */
    public abstract String getMacAddress();

    /**
     * 获取设备的AP型号
     *
     * @return 返回设备的应用处理器（AP）型号
     */
    public abstract String getCPUModel();

    /**
     * 获取设备的操作系统版本号
     *
     * @return 返回设备的操作系统版本号，指Android或YunOS大版本
     */
    public abstract String getOSVersion();

    /**
     * 获取对应卡槽的手机号码，若未插卡，返回“unknown”
     *
     * @param phoneId 卡槽位置ID，单卡槽手机传入参数为0，双卡槽位置ID分别为0和1
     * @return 对应卡槽的手机号码
     */
    public abstract String getPhoneNumber(int phoneId);

    /**
     * 发起定位请求，并缓存定位到的位置信息备接口getLocation来查询
     */
    public static void startLocation() {
        String locationProvider;
        String gpsMode = HdqyInfoManagerImpl.UNKNOWN;
        Log.d(TAG, "startLocation Enter : " + mLocationMsg);

        if (null == mLocationMsg) {
            mLocationMsg = new LocationMsg();
        }

        mLocationMsg.latitude = 0x1.fffffffffffffP+1023;//init with invalid value
        mLocationMsg.longitude = 0x1.fffffffffffffP+1023; //init with invalid value
        mLocationMsg.locationMode = 1;

        if ("n".equals(SystemProperties.get(PROP_MT_GPS_SUPPORT))) {
            mLocationMsg.locationMode = 0;
            Log.d(TAG, "Gps is not support");
            return;
        }

        if (mContext == null) {
            Log.d(TAG, "warning: mContext is null");
            return;
        }

        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (null == locationManager) {
            Log.d(TAG, "warning :getLocationMsg locationManager is null");
            return;//return invalid value
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "getLocationMsg isProviderEnabled GPS_PROVIDER");
            locationProvider = LocationManager.GPS_PROVIDER;
        } else { //GPS off, just return
            Log.d(TAG, "getLocationMsg GPS off, just return default value");
            return; //return invalid value
        }

        HandlerThread handlerThread = new HandlerThread("getLocation");
        handlerThread.start();
        try {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, handlerThread.getLooper());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "getLocationMsg sleep 55s");
        SystemClock.sleep(55000);
        Log.d(TAG, "getLocationMsg removeUpdates");
        mLocationMsg.locationMode = getGPSMode();
        locationManager.removeUpdates(locationListener);
        handlerThread.quit();
        Log.d(TAG, "getLocationMsg Exit latitude: " + mLocationMsg.latitude + ", longitude: " + mLocationMsg.longitude + ", addrFrom: " + mLocationMsg.locationMode);
    }

    static LocationListener locationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged");
            mLocationMsg.latitude = location.getLatitude();
            mLocationMsg.longitude = location.getLongitude();
        }
    };

    private static int getGPSMode() {

        int gps_mode = GPS_ONLY_MODE;
        String gpsModeValue = getValueFromXML(GPS_CSR_CONFIG_FIL_FOR_GE2, "PROPERTY", "CP-MODE");

        if ("001".equals(gpsModeValue)) {
            gps_mode = GPS_ONLY_MODE;
        } else if ("100".equals(gpsModeValue)) {
            gps_mode = GLONASS_ONLY_MODE;
        } else if ("010".equals(gpsModeValue)) {
            gps_mode = BDS_ONLY_MODE;
        } else if ("101".equals(gpsModeValue)) {
            gps_mode = GPS_GLONASS_MODE;
        } else if ("011".equals(gpsModeValue)) {
            gps_mode = GPS_BDS_MODE;
        }

        Log.d(TAG, "getLocationMsg gpsMode:" + gps_mode);
        return gps_mode;
    }

    private static String getValueFromXML(String filepath, String elementName, String key) {
        String gpsMode = "unKnown";
        Log.d(TAG, "getValueFromXML filepath-> " + filepath + ", element name-> " + elementName + ", key-> " + key);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            File gpsConfig = new File(filepath);
            Document doc = db.parse(gpsConfig);
            NodeList nodeList = doc.getElementsByTagName(elementName);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                if (element.getAttribute("NAME").equals(key)) {
                    gpsMode = element.getAttribute("VALUE");
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception ->" + e);
            e.printStackTrace();
        }
        return gpsMode;
    }

    /**
     * 获取接口12定位后缓存的位置信息，LocationMsg 为 HdqyInfoManager内部类，
     * 包含三个public的成员变量：longitude、latitude、locationMode,,变量含义及取值范围参见：5.1.1.3内部类
     * 由于实时定位需要较长时间，故把发起定位请求和定位结果的查询分两个接口执行，
     * sdk会保证两个接口的执行时间至少间隔一分钟
     *
     * @return 获取接口startLocation定位后缓存的位置信息
     */
    public abstract LocationMsg getLocation();


    /**
     * ：获取指定卡槽正在使用或默认设置的APN数据库Uri
     *
     * @param phoneId 卡槽位置ID，1个卡槽位置ID默认为0，2个卡槽位置ID分别为0和1
     * @return 返回对应卡槽设置的APN数据库URI。如果对应卡槽不存在SIM卡，则返回空值
     */
    public abstract Uri getAPNContentUri(int phoneId);

    /**
     * 根据指定规则对APN进行设置, APN修改只针对正在使用的APN进行修改。
     *
     * @param phoneId 卡槽位置ID，1个卡槽位置ID默认为0，2个卡槽位置ID分别为0和1
     * @param apn
     * @return 本次配置结果，配置成功为true，任何原因导致的失败结果为false
     */
    public static boolean setAPN(int phoneId, ApnMsg apn) {
        Log.d("zlmm", "setAPN: apn=" + apn.toString());
        if (phoneId == 1) {
            return false;
        }
        Uri APN_LIST_URI = Uri.parse("content://telephony/carriers");
        Uri currentAPNUri = Uri.parse("content://telephony/carriers/preferapn");
        ContentResolver resolver = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("name", apn.getName());
        values.put("mcc", apn.getNumeric().substring(0, 3));
        values.put("mnc", apn.getNumeric().substring(3, apn.getNumeric().length()));
        values.put("numeric", apn.getNumeric());
        values.put("proxy", apn.getProxy());
        values.put("port", apn.getPort());
        values.put("apn", apn.getApn());
        values.put("user", apn.getUser());
        values.put("password", apn.getPassword());
        int apn_id = 0;
        try {
            apn_id = resolver.update(APN_LIST_URI, values, "numeric=?", new String[]{apn.getNumeric()});
            Log.d("zlmm", "setAPN: apn_id=" + apn_id);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        ContentValues valuesSetDef = new ContentValues();
        values.put("apn_id", apn_id);
        //更新当前状态APN信息
        resolver.update(currentAPNUri, valuesSetDef, "apn_id=?", new String[]{apn_id + ""});

        Cursor mCursor = mContext.getContentResolver().query(currentAPNUri,
                null, null, null, null);
        if (mCursor == null) {
            return false;
        }
        while (mCursor != null && mCursor.moveToNext()) {
            int apnId = mCursor.getShort(mCursor.getColumnIndex("_id"));
            String name = mCursor.getString(mCursor.getColumnIndex("name"));
            String numeric = mCursor.getString(mCursor.getColumnIndex("numeric"));
            String proxy = mCursor.getString(mCursor.getColumnIndex("proxy"));
            String port = mCursor.getString(mCursor.getColumnIndex("port"));
            String apnDef = mCursor.getString(mCursor.getColumnIndex("apn"));
            String user = mCursor.getString(mCursor.getColumnIndex("user"));
            String password = mCursor.getString(mCursor.getColumnIndex("password"));
            Log.d("zlmm", "setAPN: apnId=" + apnId + " , name=" + name
                    + " , numeric=" + numeric + " , proxy=" + proxy + " , port=" + port
                    + " , apnDef=" + apnDef + " , user=" + user + " , password=" + password);
            if (apnId == apn_id
                    && name.equals(apn.getName())
                    && numeric.equals(apn.getNumeric())
                    && proxy.equals(apn.getProxy())
                    && port.equals(apn.getPort())
                    && apnDef.equals(apn.getApn())
                    && user.equals(apn.getUser())
                    && password.equals(apn.getPassword())) {
                return true;
            }
        }
        if (mCursor != null) {
            mCursor.close();
        }
        return false;
    }

    /**
     * 设置系统日志开关
     *
     * @param mode
     */
    public static void setLogMode(boolean mode) {
        if (mode) {

        }
    }

    /**
     * 删除系统日志
     *
     * @param context
     * @param file    日志文件全路径名称
     */
    public static void deleteLogFile(Context context, String file) {
        File filePath = new File(file);
        if (filePath.isFile() && filePath.exists()) {
            filePath.delete();
        }
    }

    /**
     * 获取当前设备网络通道类型
     *
     * @return 返回当前设备网络通道类型（通道类型有4G、3G、2G、WIFI、有线）
     * 未知:0 ; WIFI:1 ; 2G:2 ; 3G:3 ; 4G:4 ; 5G:5 ; 有线:11
     */
    public abstract int getNetChanel();

    /**
     * 获取当前设备电池理论最大容量
     *
     * @return 电池理论最大容量，单位为mAh
     */
    public abstract int getMaxBatteryCapacity();

    /**
     * 获取当前设备电池实际容量
     *
     * @return 电池当前实际最大容量，单位为mAh
     */
    public abstract int getCurrentBatteryCapacity();

    /**
     * 获取当前设备唯一标识
     *
     * @return 当前设备唯一标识
     */
    public abstract String getSnId();

    /**
     * 当前设备是否国际漫游状态
     *
     * @return 国际漫游状态返回true，否则返回false
     */
    public abstract boolean isInternationalNetworkRoaming();

    /**
     * 获取主卡槽位置ID
     * 主卡定义：数据网络卡对应的卡槽位置为主卡位置
     *
     * @return 返回主卡槽对应的位置ID，为0或1；当终端支持Dual SIM Dual VoLTE时，返回值为3；
     * 如果当前不存在SIM卡，则返回-1
     */
    public abstract int getMasterPhoneId();


    /**
     * 获取该类的一个实例，需要做成单例模式（single intance）
     *
     * @param context
     * @return
     */
    public static HdqyInfoManager getDefault(Context context) {
        synchronized (HdqyInfoManager.class) {
            Log.d(TAG, "getInstance context: "+ context);
            if (sInstance == null) {
                sInstance = new HdqyInfoManagerImpl(context);
                mContext = context;
            } else {
                Log.wtf(TAG, "called multiple times!  mInstance = " + sInstance);
            }
        }
        return sInstance;
    }


}
