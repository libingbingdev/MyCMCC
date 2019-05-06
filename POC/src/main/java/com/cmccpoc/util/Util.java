package com.cmccpoc.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.inputmethod.InputMethodManager;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.config.Config;
import com.cmccpoc.services.AirServices;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 app通用工具类
 @author Yao */
public class Util
{
    public static final int NOTIFI_ID_MESSAGE = 0;
    public static final int NOTIFI_ID_VOICE_LISTEN = 1;
    public static final int NOTIFI_ID_NOTICE = 2;
    public static final int NOTIFI_ID_FENCE_WARNING = 3;
    public static final int NOTIFI_ID_TASK_DISPATCH = 4;
    public static final int NOTIFI_ID_VOICE_TALK = 5;
    public static final int NOTIFI_ID_BACKGROUND_STAY = 6; // zuocy:设置后台常驻
    public static final int NOTIFI_ID_VOICE_RECORD = 10;
    public static  final int NO_NET_WORK=20;
    public static final int WIFI=21;
    public static final int NO_WIFI=22;
    static NotificationManager nm = null;

    public static final int DEVICEINFO_UNKNOWN = -1;
    /**
     显示通知
     @param id 通知类型
     @param context 上下文
     @param intent intent对象
     @param from 发送人id
     @param ticker 标题
     @param message 消息内容
     @param object 对象参数
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static void showNotification(int id, Context context, Intent intent, String from, String ticker, String message, Object object)
    {
        if (!Toast.isDebug) return;
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = null;
        int flag = -1;
        int drawableId = Config.app_icon_notify;
        switch (id)
        {
            case NOTIFI_ID_MESSAGE:
            {
                contentIntent = PendingIntent.getBroadcast(context, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                flag = Notification.FLAG_AUTO_CANCEL;
                break;
            }
            case NOTIFI_ID_NOTICE:
            case NOTIFI_ID_VOICE_RECORD:
            case NOTIFI_ID_VOICE_TALK:
            case NOTIFI_ID_FENCE_WARNING:
            case NOTIFI_ID_TASK_DISPATCH:
            {
                contentIntent = PendingIntent.getActivity(context, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                flag = Notification.FLAG_AUTO_CANCEL;
                break;
            }
            case NOTIFI_ID_BACKGROUND_STAY:
            {
                // 设置常驻Flag
                flag = Notification.FLAG_ONGOING_EVENT;
            }
        }
        try
        {
            Notification notification;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            {
                notification = new Notification.Builder(context).setSmallIcon(drawableId, 3).setShowWhen(flag != Notification.FLAG_ONGOING_EVENT).setContentText(message).setContentTitle(from).setContentIntent(contentIntent).setTicker(ticker).build();
            }
            else
            {
                notification = new Notification(drawableId, ticker, System.currentTimeMillis());
                notification.setLatestEventInfo(context, from, message, contentIntent);
            }
            if (flag != -1)
            {
                notification.flags = flag;
            }
            nm.notify(id, notification);
        }
        catch (NoSuchMethodError e)
        {
            //
        }
        catch (Exception e)
        {
            // TODO: handle exception
            // Log.e(Util.class, "util--182---error");
        }
    }

    /**
     关闭通知
     @param id
     */
    public static void closeNotification(int id)
    {
        try
        {
            if (nm != null)
            {
                nm.cancel(id);
            }
        }
        catch (Exception e)
        {
        }
    }

    public static boolean dexCrcCheck(Context context)
    {
        boolean isOk = false;
        String apkPath = context.getPackageCodePath();
        Long dexCrc = Long.parseLong(context.getString(R.string.app_dex_crc));
        try
        {
            ZipFile zipfile = new ZipFile(apkPath);
            ZipEntry dexentry = zipfile.getEntry("classes.dex");
            Log.i(Util.class, "classes.dexcrc="+dexentry.getCrc());
            if(dexentry.getCrc() != dexCrc){
                isOk = false;
            }else{
                isOk = true;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return isOk;
    }

    /**
     获取app版本
     @param context 上下文
     @return
     */
    public static String appVersion(Context context)
    {
        String version = "";
        try
        {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return version;
    }

    public static int appVersionCode(Context context)
    {
        int code = 0;
        try
        {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            code = info.versionCode;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return code;
    }

    /**
     调节音量--增大
     @param context 上下文
     */
    public static void setStreamVolumeUp(Context context)
    {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = getStreamVolume(context);
        if (currentVolume < max)
        {
            currentVolume++;
            // Toast.makeText(context,
            // String.format(context.getString(R.string.talk_volume),
            // currentVolume), 0).show();
            setStreamVolume(context, currentVolume);
        }
        else
        {
            Toast.makeText(context, R.string.talk_volume_max, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     调节音量--减小
     @param context 上下文
     */
    public static void setStreamVolumeDown(Context context)
    {

        int currentVolume = getStreamVolume(context);
        if (currentVolume > 0)
        {
            currentVolume--;
            // Toast.makeText(context,
            // String.format(context.getString(R.string.talk_volume),
            // currentVolume), 0).show();
            setStreamVolume(context, currentVolume);
        }
        else
        {
            Toast.makeText(context, R.string.talk_volume_min, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     设置音量大小
     @param context 上下文
     @param streamVolume 音量
     */
    public static void setStreamVolume(Context context, int streamVolume)
    {
        try
        {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, streamVolume, AudioManager.FLAG_PLAY_SOUND);
        }
        catch (Exception e)
        {
            // Log.e(Util.class, e.toString());
        }
    }

    /**
     获取当前语音模式
     @param context 上下文
     @return
     */
    public static int getMode(Context context)
    {
        if (context != null)
        {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            return am.getMode();
        }
        return -1;
    }

    /**
     设置当前语音模式
     @param context 上下文
     */
    public static void setMode(Context context)
    {
        try
        {
            if (context != null)
            {
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                if (am.getMode() == AudioManager.MODE_NORMAL)
                {
                    am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    // am.setMode(AudioManager.MODE_IN_CALL);
                    am.setSpeakerphoneOn(false);
                }
                else
                {
                    am.setMode(AudioManager.MODE_NORMAL);
                    am.setSpeakerphoneOn(true);
                }
            }
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
    }

    /**
     获取音量值
     @param context 上下文
     @return
     */
    public static int getStreamVolume(Context context)
    {
        int streamVolume = 0;
        try
        {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            streamVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        catch (Exception e)
        {
            // Log.e(Util.class, e.toString());
        }
        return streamVolume;
    }

    /**
     获取日期
     @return
     */
    public static String getCurrentDate()
    {
        String currentTime = "";
        try
        {
            Date now = new Date();
            DateFormat d = DateFormat.getDateTimeInstance();
            currentTime = d.format(now);
        }
        catch (Exception e)
        {
            // TODO: handle exception
            // Log.e(Util.class, "getCurrentDate--->>error"+e.toString());
        }
        return currentTime;
    }

    /**
     获取时刻
     @return
     */
    public static String getCurrentTime()
    {
        Time t = new Time();
        t.setToNow();
        int year = t.year;
        int month = t.month + 1;
        int day = t.monthDay;
        int hour = t.hour;
        int minute = t.minute;
        int second = t.second;
        String time = year + "-" + month + "-" + day;
        time += " " + String.format("%02d:%02d:%02d", hour, minute, second);
        return time;
    }

    /**
     登陆信息
     @param result 登陆结果
     @param context 上下文
     @return
     */
    public static String loginInfo(int result, Context context)
    {
        String info = "";
        if (context == null)
            return info;
        switch (result)
        {
            case AirtalkeeAccount.ACCOUNT_RESULT_ERR_NETWORK:
                info = context.getResources().getString(R.string.talk_login_failed_general);
                break;
            case AirtalkeeAccount.ACCOUNT_RESULT_ERR_USER_NOTEXIST:
                info = context.getResources().getString(R.string.talk_login_failed_nouser);
                break;
            case AirtalkeeAccount.ACCOUNT_RESULT_ERR_SERVER_UNAVAILABLE:
                info = context.getResources().getString(R.string.talk_login_failed_server);
                break;
            case AirtalkeeAccount.ACCOUNT_RESULT_ERR_USER_PWD:
                info = context.getResources().getString(R.string.talk_login_login_failed_user_or_password);
                break;
            case AirtalkeeAccount.ACCOUNT_RESULT_ERR_USER_IN_BLACKLIST:
                info = context.getResources().getString(R.string.talk_login_login_failed_forbidden);
                break;
            case AirtalkeeAccount.ACCOUNT_RESULT_ERR_USER_NOT_ACCEPTABLE:
                info = context.getResources().getString(R.string.talk_login_login_failed_not_acceptable);
                break;
            case AirtalkeeAccount.ACCOUNT_RESULT_ERR_PWD_DUP:
                info = context.getResources().getString(R.string.talk_login_login_failed_pwd_duplicate);
                break;
            case AirtalkeeAccount.ACCOUNT_RESULT_ERR_ACCOUNT_LIMITED:
                info = context.getResources().getString(R.string.talk_userlogin_register_failed_limited);
                break;
            case AirtalkeeAccount.ACCOUNT_RESULT_ERR_ACCOUNT_EXPIRE:
                info = context.getResources().getString(R.string.talk_userlogin_login_expired);
                break;
            case AirtalkeeAccount.ACCOUNT_RESULT_ERR_ACCOUNT_FORBIDDEN:
                info = context.getResources().getString(R.string.talk_userlogin_login_forbidden);
                break;
            case AirtalkeeAccount.ACCOUNT_RESULT_ERR_CODE_EXPIRE:
                info = context.getResources().getString(R.string.talk_user_temp_code_expire);
                break;
            case AirtalkeeAccount.ACCOUNT_RESULT_ERR_SINGLE:
                info = context.getResources().getString(R.string.talk_login_login_failed_single);
                break;
            case AirtalkeeAccount.ACCOUNT_RESULT_ERR_LICENSE:
                info = context.getResources().getString(R.string.talk_login_failed_general);
                break;
            default:
                info = context.getResources().getString(R.string.talk_login_failed_general);
                break;
        }
        return info;
    }

    /**
     版本配置
     @param context
     */
    public static void versionConfig(Context context)
    {
        int preVersion = AirServices.iOperator.getInt("KEY_VERSION");
        int currentVersion = 0;
        try
        {
            currentVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        }
        catch (NameNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        AirServices.iOperator.putInt("KEY_VERSION", currentVersion);
        AirServices.VERSION_NEW = currentVersion > preVersion;
    }

    /**
     隐藏软键盘
     @param context 上下文
     */
    public static void hideSoftInput(Context context)
    {
        try
        {
            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        catch (Exception e)
        {
            Log.e(Util.class, "hideSoftInput-->error");
        }
    }

    /**
     显示软键盘
     @param context 上下文
     */
    public static void showSoftInput(Context context)
    {
        try
        {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), InputMethodManager.SHOW_IMPLICIT, InputMethodManager.RESULT_SHOWN);
        }
        catch (Exception e)
        {
            // TODO: handle exception
            // Log.e(Util.class, "hideSoftInput-->error");
        }
    }

    /**
     软键盘是否打开
     @param context 上下文
     @return
     */
    public static boolean isSoftKeybordOpen(Context context)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isFullscreenMode();
    }

    /**
     获取Spannable对象
     @param context 上下文
     @param text 文字内容
     @return
     */
    public static Spannable getSpannable(Context context, String text)
    {
        Spannable name = null;
        if (context != null && !Utils.isEmpty(text))
            name = Util.buildPlainMessageSpannable(context, text.replaceAll("\r", "").getBytes());
        return name;
    }

    /**
     构建消息容器
     @param context 上下文
     @param content 文字内容
     @return
     */
    public static Spannable buildPlainMessageSpannable(Context context, byte[] content)
    {
        try
        {
            String msg = "";
            if (content != null)
            {
                try
                {
                    msg = new String(content, "UTF-8");
                }
                catch (UnsupportedEncodingException e)
                {
                }
            }
            SpannableString spannable = new SpannableString(msg);
            Smilify.getInstance(context).addSmiley(spannable);
            return spannable;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     文字截取
     @param context 上下文
     @param clipStr 截取字符串
     */
    @SuppressWarnings("deprecation")
    public static void textClip(Context context, String clipStr)
    {
        try
        {
            ClipboardManager clip = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clip.setText(clipStr);
        }
        catch (Exception e)
        {
        }
    }

    /**
     获取图片临时名称
     @return
     */
    public static String getImageTempFileName()
    {
        String saveDir = Environment.getExternalStorageDirectory() + "/DCIM/Camera";
        File dir = new File(saveDir);
        if (!dir.exists())
        {
            dir.mkdir();
        }
        String fileName = saveDir + "/AIR" + Utils.getCurrentTimeInMillis() + ".jpg";
        return fileName;
    }

    public static String getImageTempFileName(String prefixion)
    {
        String saveDir = Environment.getExternalStorageDirectory() + "/DCIM/Camera";
        File dir = new File(saveDir);
        if (!dir.exists())
        {
            dir.mkdir();
        }
        String fileName = saveDir + "/" + prefixion + "-" + Utils.getCurrentTimeInMillis() + ".jpg";
        return fileName;
    }

    /**
     转换English时间格式
     @param date 时间字符串
     @return
     */
    public static String convertEnglishDate(String date)
    {
        String edateString = date;
        if (!Utils.isEmpty(date))
        {
            edateString = edateString.replace("年", "/");
            edateString = edateString.replace("月", "/");
            edateString = edateString.replace("日", "");
            String d[] = edateString.split("/");
            if (d != null && d.length == 3)
            {
                edateString = d[2] + "/";
                edateString += d[1] + "/";
                edateString += d[0];
            }
        }
        return edateString;
    }

    /**
     拼命是否亮
     @param context 上下文
     @return
     */
    public static boolean isScreenOn(Context context)
    {
        boolean isOn = true;
        if (context != null)
        {
            android.app.KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (mKeyguardManager != null)
            {
                isOn = !mKeyguardManager.inKeyguardRestrictedInputMode();
            }
        }
        return isOn;
    }

    /*********************************
     *
     * Toast
     *
     *********************************/

    private static Toast mToast = null;

    /**
     显示Toast提示
     @param context 上下文
     @param content 内容
     */
    public static void Toast(Context context, String content)
    {
        try
        {
            if(Toast.isDebug) Toast.makeText1(context, content, Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
    }

    /**
     显示Toast提示
     @param context 上下文
     @param content 内容
     @param icon icon图标
     */
    public static void Toast(Context context, String content, int icon)
    {
        try
        {
            if(Toast.isDebug) Toast.makeText1(context, icon, content, Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
    }

    /**
     显示Toast提示
     @param context 上下文
     @param content 内容
     @param seconds 显示时长
     @param icon icon图标
     */
    public static void Toast(Context context, String content, int seconds, int icon)
    {
        try
        {
            if(Toast.isDebug) Toast.makeText1(context, content, seconds).show();
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
    }

    /**
     获取MD5加密后字符串
     @param str 原字符串
     @return
     */
    public static String getMD5Str(String str)
    {
        MessageDigest messageDigest = null;
        try
        {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        }
        catch (NoSuchAlgorithmException e)
        {
            System.out.println("NoSuchAlgorithmException caught!");
            System.exit(-1);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++)
        {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return md5StrBuff.toString();
    }


    /**
     是否在后台运行
     @param context 上下文
     @return
     */
    public static boolean isBackground(Context context)
    {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (RunningAppProcessInfo appProcess : appProcesses)
        {
            if (appProcess.processName.equals(context.getPackageName()))
            {
                if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     获取当前网络状态
     @return
     */
    public static String getCurrentNetType()
    {
        String type = "";
        ConnectivityManager cm = (ConnectivityManager) AirServices.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null)
        {
            type = "null";
        }
        else if (info.getType() == ConnectivityManager.TYPE_WIFI)
        {
            type = "wifi";
        }
        else if (info.getType() == ConnectivityManager.TYPE_MOBILE)
        {
            int subType = info.getSubtype();
            if (subType == TelephonyManager.NETWORK_TYPE_CDMA || subType == TelephonyManager.NETWORK_TYPE_GPRS || subType == TelephonyManager.NETWORK_TYPE_EDGE)
            {
                type = "GSM";
            }
            else if (subType == TelephonyManager.NETWORK_TYPE_UMTS || subType == TelephonyManager.NETWORK_TYPE_HSDPA || subType == TelephonyManager.NETWORK_TYPE_EVDO_A || subType == TelephonyManager.NETWORK_TYPE_EVDO_0 || subType == TelephonyManager.NETWORK_TYPE_EVDO_B)
            {
                type = "TD-SCDMA";
            }
            else if (subType == TelephonyManager.NETWORK_TYPE_LTE)
            {// LTE是3g到4g的过渡，是3.9G的全球标准
                type = "TD-LTE";
            }
        }
        return type;
    }

    public static String getImsi(Context context)
    {
        String imsi = "";
        try
        {
            TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imsi = mTelephonyMgr.getSubscriberId();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return imsi;
    }

    /**
     获取IMEI号
     @param context
     */
    public static String getImei(Context context)
    {
        String deviceid = "";
        try
        {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            deviceid = tm.getDeviceId();
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
        return deviceid;
    }

    public static String clearHTMLTag(String htmlStr)
    {
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
        String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式

        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); //过滤script标签

        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); //过滤style标签

        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); //过滤html标签
        return htmlStr.trim(); //返回文本字符串 
    }

    public static Bitmap getVideoImage(Context mContext, String filePath)
    {
        if (filePath.length() > 0)
        {
            MediaMetadataRetriever rev = new MediaMetadataRetriever();
            File video = new File(filePath);
            if (video != null)
            {
                try
                {
                    rev.setDataSource(mContext, Uri.fromFile(video));
                    return rev.getFrameAtTime(1 * 1000 * 2000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                }
                catch (Exception e)
                {
                    return null;
                }
            }
            return null;
        }
        else
            return null;
    }

    /**
     验证手机格式
     */
    public static boolean isMobile(String number)
    {
        /*
        移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
        联通：130、131、132、152、155、156、185、186
        电信：133、153、180、189、（1349卫通）
        物联网卡：不确定。。
        总结起来就是第一位必定为1，第二位若不含物联网卡则为3或5或8，其他位置的可以为0-9
        */
        //"[1]"代表第1位为数字1，"\\d{9}"代表后面是可以是0～9的数字，有10或12位。
        String num = "^[1]\\d{10}|[1]\\d{12}$";
        if (TextUtils.isEmpty(number))
            return false;
        else
        {
            //matches():字符串是否在给定的正则表达式匹配
            return number.matches(num);
        }
    }

    public static int ui_dip2px(Context context, float dipValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }

    public static int ui_px2dip(Context context, float pxValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5f);
    }

    /**
     * 获取当前的运营商
     *
     * @param context
     * @return 运营商名字
     */
    public static String getOperator(Context context) {


        String ProvidersName = "";
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String IMSI = telephonyManager.getSubscriberId();
        if (IMSI != null) {
            if (IMSI.startsWith("46000") || IMSI.startsWith("46002") || IMSI.startsWith("46007")) {
                ProvidersName = "CMCC";
            } else if (IMSI.startsWith("46001")  || IMSI.startsWith("46006")) {
                ProvidersName = "CUCC";
            } else if (IMSI.startsWith("46003")) {
                ProvidersName = "CTCC";
            }
            return ProvidersName;
        } else {
            return " ";
        }
       // return telephonyManager.getSimOperatorName();
    }

    /**
     * 判断是否打开网络
     * @param context
     * @return
     */
    public static boolean isNetWorkAvailable(Context context){
        boolean isAvailable = false ;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isAvailable()){
            isAvailable = true;
        }
        return isAvailable;
    }

    /**
     * 获取网络类型
     * @param context
     * @return
     */
    public static int getNetWorkType(Context context) {
        if (!isNetWorkAvailable(context)) {
            return NO_NET_WORK;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting())
            return WIFI;
        else
            return NO_WIFI;
    }

    /**
     * 判断当前网络是否为wifi
     * @param context
     * @return  如果为wifi返回true；否则返回false
     */
    @SuppressWarnings("static-access")
    public static boolean isWiFiConnected(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo.getType() == manager.TYPE_WIFI ? true : false;
    }

    /**
     * 判断MOBILE网络是否可用
     * @param context
     * @return
     * @throws Exception
     */
    public static boolean isMobileDataEnable(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isMobileDataEnable = false;
        isMobileDataEnable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        return isMobileDataEnable;
    }

    /**
     * 判断wifi 是否可用
     * @param context
     * @return
     * @throws Exception
     */
    public static boolean isWifiDataEnable(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiDataEnable = false;
        isWifiDataEnable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        return isWifiDataEnable;
    }

    /**
    *获取本机号码
    *
     */
    public static String getPhoneNumber(Context context){
        String phoneNumber="N/A";
        TelephonyManager tm=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        phoneNumber= tm.getLine1Number();
        return phoneNumber;
    }

    /**
     * 判断是否包含SIM卡
     *
     * @return 状态
     */
    public static boolean ishasSimCard(Context context) {
        TelephonyManager telMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
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

    public static int getNumberOfCPUCores() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            // Gingerbread doesn't support giving a single application access to both cores, but a
            // handful of devices (Atrix 4G and Droid X2 for example) were released with a dual-core
            // chipset and Gingerbread; that can let an app in the background run without impacting
            // the foreground application. But for our purposes, it makes them single core.
            return 1;
        }
        int cores;
        try {
            cores = new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER).length;
        } catch (SecurityException e) {
            cores = DEVICEINFO_UNKNOWN;
        } catch (NullPointerException e) {
            cores = DEVICEINFO_UNKNOWN;
        }
        return cores;
    }

    private static final FileFilter CPU_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String path = pathname.getName();
            //regex is slow, so checking char by char.
            if (path.startsWith("cpu")) {
                for (int i = 3; i < path.length(); i++) {
                    if (path.charAt(i) < '0' || path.charAt(i) > '9') {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    };

    public static int getCPUMaxFreqKHz() {
        int maxFreq = DEVICEINFO_UNKNOWN;
        try {
            for (int i = 0; i < getNumberOfCPUCores(); i++) {
                String filename =
                        "/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq";
                File cpuInfoMaxFreqFile = new File(filename);
                if (cpuInfoMaxFreqFile.exists()) {
                    byte[] buffer = new byte[128];
                    FileInputStream stream = new FileInputStream(cpuInfoMaxFreqFile);
                    try {
                        stream.read(buffer);
                        int endIndex = 0;
                        //Trim the first number out of the byte buffer.
                        while (buffer[endIndex] >= '0' && buffer[endIndex] <= '9'
                                && endIndex < buffer.length) endIndex++;
                        String str = new String(buffer, 0, endIndex);
                        Integer freqBound = Integer.parseInt(str);
                        if (freqBound > maxFreq) maxFreq = freqBound;
                    } catch (NumberFormatException e) {
                        //Fall through and use /proc/cpuinfo.
                    } finally {
                        stream.close();
                    }
                }
            }
            if (maxFreq == DEVICEINFO_UNKNOWN) {
                FileInputStream stream = new FileInputStream("/proc/cpuinfo");
                try {
                    int freqBound = parseFileForValue("cpu MHz", stream);
                    freqBound *= 1000; //MHz -> kHz
                    if (freqBound > maxFreq) maxFreq = freqBound;
                } finally {
                    stream.close();
                }
            }
        } catch (IOException e) {
            maxFreq = DEVICEINFO_UNKNOWN; //Fall through and return unknown.
        }
        return maxFreq;
    }
    private static int parseFileForValue(String textToMatch, FileInputStream stream) {
        byte[] buffer = new byte[1024];
        try {
            int length = stream.read(buffer);
            for (int i = 0; i < length; i++) {
                if (buffer[i] == '\n' || i == 0) {
                    if (buffer[i] == '\n') i++;
                    for (int j = i; j < length; j++) {
                        int textIndex = j - i;
                        //Text doesn't match query at some point.
                        if (buffer[j] != textToMatch.charAt(textIndex)) {
                            break;
                        }
                        //Text matches query here.
                        if (textIndex == textToMatch.length() - 1) {
                            return extractValue(buffer, j);
                        }
                    }
                }
            }
        } catch (IOException e) {
            //Ignore any exceptions and fall through to return unknown value.
        } catch (NumberFormatException e) {
        }
        return DEVICEINFO_UNKNOWN;
    }

    private static int extractValue(byte[] buffer, int index) {
        while (index < buffer.length && buffer[index] != '\n') {
            if (buffer[index] >= '0' && buffer[index] <= '9') {
                int start = index;
                index++;
                while (index < buffer.length && buffer[index] >= '0' && buffer[index] <= '9') {
                    index++;
                }
                String str = new String(buffer, 0, start, index - start);
                return Integer.parseInt(str);
            }
            index++;
        }
        return DEVICEINFO_UNKNOWN;
    }
}
