package com.cmccpoc.auth;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;

//import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.cmcc.sso.sdk.util.SsoSdkConstants;

import com.cmccpoc.util.*;
import com.cmccpoc.config.*;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.util.Log;
import com.cmcc.sso.sdk.auth.AuthnHelper;
import com.cmcc.sso.sdk.auth.TokenListener;

public class AuthSso {

	public static final int USER_INFO_RESULT_OK = 0;
	public static final int USER_INFO_RESULT_FAIL = 1;
	public static final int USER_INFO_RESULT_INVALID = 2;

	private static String APP_ID = "01800219";
	private static String APP_KEY = "DE4214B0C4E63095";
	private static String SOURCE_ID = "018002";

	private static AuthSso mInstance = null;
	private static Context mContext = null;
	private AuthnHelper mAuthnHelper = null;
	public String mToken="";
	private AuthSsoListener mListener = null;
	
	private String dmCustomerId = "";
	private String dmUid = "";
	private String dmPwd = "";

	private final static int DM_TS = 90*1000;
	private long dmTs = 0;
	public String mPhoneId="";
	public  String mKey="";
	
	public static AuthSso getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new AuthSso();
		}
		return mInstance;
	}

	public void RunDmReport(Context context, String customerId, String uid)
	{
		if (System.currentTimeMillis() - dmTs > DM_TS)
		{
			DmResult result = new DmResult();
			mContext = context;
			dmCustomerId = customerId;
			checkDm( uid, result);
			dmTs = System.currentTimeMillis();

		}
	}

	public void init(final Context context){
		mAuthnHelper=new AuthnHelper(context);
		mAuthnHelper.enableLog(true);
		mAuthnHelper.getAccessToken(APP_ID, APP_KEY,Util.getPhoneNumber(context), SsoSdkConstants.LOGIN_TYPE_DEFAULT, new TokenListener() {
			@Override
			public void onGetTokenComplete(JSONObject jsonObject) {
				if(jsonObject!=null){
					try
					{
						Log.i("wqq","wqq====onGetTokenComplete"+jsonObject.toString());
						mToken = jsonObject.optString(SsoSdkConstants.VALUES_KEY_TOKEN);
						if(jsonObject.optString("resultCode")=="102101"){

						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		});
	}

	// ========================================================
	//
	// Interaction with DM
	//
	// ========================================================

	private class DmResult
	{
		public String info;
		public String phone = "";
		public String key = "";

	}

	private boolean checkDm( String uid, DmResult result)
	{
		boolean ret = false;
		JSONObject json_req = new JSONObject();
		JSONObject json_body = new JSONObject();
		JSONObject json_rsp = null;

		try
		{

			json_req.put("code", "1001");
			if (!TextUtils.isEmpty(mToken))
				json_body.put("ssoToken", mToken);
			if (!TextUtils.isEmpty(uid))
				json_body.put("uid", uid);
			json_body.put("consumerId", dmCustomerId);
			json_body.put("appInfo", Config.VERSION_NAME);
			json_body.put("appPackage", mContext.getPackageName());
			json_body.put("osVer", android.os.Build.VERSION.RELEASE);
			json_body.put("phoneModel", android.os.Build.MODEL);
			json_body.put("imei", Util.getImei(mContext));
			json_body.put("imsi", Util.getImsi(mContext));
			json_body.put("ts", System.currentTimeMillis());
			json_req.put("body", json_body.toString());

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		Log.d("wqq", "[AUTH] req:" + json_req.toString());

		// Do Http
		String json = null;
		try
		{
            URL url = new URL(Config.serverDmInfoUrl);
            HttpURLConnection con =(HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true); 
            con.setUseCaches(false);
            con.setRequestProperty("Content-Type","application/json;charset=utf-8");
            con.setRequestProperty("Pragma:","no-cache");
            con.setRequestProperty("Cache-Control","no-cache");
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(json_req.toString());
            out.flush();  
            out.close();
			int respCode = con.getResponseCode();
			Log.d("wqq", "[AUTH] respCode:" + con.getResponseCode());
			if ( respCode == 200 )
			{
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String line = "";
				StringBuffer buf = new StringBuffer();
				while ((line = br.readLine()) != null)
				{
					buf.append(line);
				}
				json = buf.toString();
				Log.d("wqq", "[AUTH] respBody:" + json);
			}
		}
		catch (MalformedURLException e)
		{
			System.out.println("发送 POST 请求出现异常"+e);
			e.printStackTrace();

		}
		catch (IOException e)
		{
			System.out.println("发送 POST 请求出现异常"+e);
			e.printStackTrace();
		}

		if ( json != null )
		{
			try
			{
				json_rsp = new JSONObject(json);
				Log.i("wqq", "json_rsp==" + json_rsp.toString());
				result.phone = json_rsp.optString("phone");
				if (!Utils.isEmpty(result.phone)) {
					mPhoneId = result.phone;
				}
				result.key = json_rsp.optString("key");
				if (!Utils.isEmpty(result.key)) {
					mKey = result.key;
				}
				ret = true;
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return ret;
	}


	// ========================================================
	
	private final static int MSG_SSO_GET_OK = 1;
	private final static int MSG_SSO_GET_FAIL = 2;
	private final static int MSG_DM_CHECK_OK = 10;
	private final static int MSG_DM_CHECK_FAIL = 11;
	private final static int MSG_DM_CHECK_INVALID = 12;

	private void noticeMessage(int what, Object obj)
	{
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;

		mHandler.sendMessage(msg);
	}

	private Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case MSG_SSO_GET_OK:
					if (mListener != null)
						mListener.onAuthSsoTokenGet(true);
					break;
					
				case MSG_SSO_GET_FAIL:
					if (mListener != null)
						mListener.onAuthSsoTokenGet(false);
					break;
					
				case MSG_DM_CHECK_OK:
					if (mListener != null)
						mListener.onAuthSsoUserInfoGet(USER_INFO_RESULT_OK, dmUid, dmPwd);
					break;

				case MSG_DM_CHECK_FAIL:
					if (mListener != null)
						mListener.onAuthSsoUserInfoGet(USER_INFO_RESULT_FAIL, null, null);
					break;
					
				case MSG_DM_CHECK_INVALID:
					if (mListener != null)
						mListener.onAuthSsoUserInfoGet(USER_INFO_RESULT_INVALID, null, null);
					break;
					
				default:
					break;
			}

		}
	};


	
}
