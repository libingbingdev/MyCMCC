package cn.richinfo.manager;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.location.Location;
import android.location.Criteria;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.HandlerThread;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.location.Criteria;
import android.os.SystemClock;
import android.os.Bundle;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import android.os.SystemProperties;

public abstract class DeviceInfoManager {

    protected static DeviceInfoManager sInstance;
    static final String TAG = "DeviceInfoManager";
    protected static Context mContext;
    protected static LocationMsg mLocationMsg;
    public static final String GPS_CSR_CONFIG_FIL_FOR_GE2 = "/data/gnss/config/config.xml";
    public static final int GPS_ONLY_MODE = 1;
    public static final int BDS_ONLY_MODE = 2;
    public static final int GLONASS_ONLY_MODE = 4;
    public static final int GPS_GLONASS_MODE = 14;
    public static final int GPS_BDS_MODE = 12;
    private static String PROP_MT_GPS_SUPPORT = "ro.mt.gps.support";

    private DeviceInfoManager() {
        mContext = null;
    }

    protected DeviceInfoManager(Context context) {
        Context appContext = context.getApplicationContext();
        if (appContext != null) {
            mContext = appContext;
        } else {
            mContext = context;
        }
    }

    public static DeviceInfoManager getInstance(Context context) {
        synchronized (DeviceInfoManager.class) {
            Log.d(TAG, "getInstance context: "+ context);
            if (sInstance == null) {
                sInstance = new DeviceInfoManagerImpl(context);
                mContext = context;
            } else {
                Log.wtf(TAG, "called multiple times!  mInstance = " + sInstance);
            }
        }
        return sInstance;
    }

    public static class LocationMsg{
        public double longitude;
        public double latitude;
        public int locationMode;
    }

   public static void startLocation(){
        String locationProvider;
        String gpsMode = "unKnown";
        Log.d(TAG, "startLocation Enter : "+ mLocationMsg);

        if (null == mLocationMsg) {
            mLocationMsg = new LocationMsg();
        }

        mLocationMsg.latitude = 0x1.fffffffffffffP+1023;//init with invalid value
        mLocationMsg.longitude =  0x1.fffffffffffffP+1023; //init with invalid value
        mLocationMsg.locationMode = 1;

        if("n".equals(SystemProperties.get(PROP_MT_GPS_SUPPORT))){
            mLocationMsg.locationMode = 0;
            Log.d(TAG, "Gps is not support");
            return;
        }

        if (mContext == null) {
            Log.d(TAG, "warning: mContext is null");
            return;
        }

        LocationManager locationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);

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
        } catch (Exception e){
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

    private static int getGPSMode(){

        int gps_mode = GPS_ONLY_MODE;
        String gpsModeValue = getValueFromXML(GPS_CSR_CONFIG_FIL_FOR_GE2, "PROPERTY", "CP-MODE");

        if("001".equals(gpsModeValue)){
            gps_mode = GPS_ONLY_MODE;
        }else if("100".equals(gpsModeValue)){
            gps_mode = GLONASS_ONLY_MODE;
        }else if("010".equals(gpsModeValue)){
            gps_mode = BDS_ONLY_MODE;
        }else if("101".equals(gpsModeValue)){
            gps_mode = GPS_GLONASS_MODE;
        }else if("011".equals(gpsModeValue)){
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

    public abstract String getNetworkType();

    public abstract String getDeviceSoftwareVersion();

    public abstract int getVoLTEState();

    public abstract String getRomStorageSize();

    public abstract String getRamStorageSize();

    public abstract String getMacAddress();

    public abstract String getCPUModel();

    public abstract String getOSVersion();

    public abstract LocationMsg getLocation();

    public abstract String getDeviceSoftwareName();

    public abstract String getNetAccount();

    public abstract String getPhoneNumber();

}
