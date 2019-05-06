package com.cmccpoc.auth;

public interface AuthSsoListener {

	public void onAuthSsoTokenGetting();
	
	public void onAuthSsoTokenGet(boolean isOk);
	
	public void onAuthSsoUserInfoGetting();
	
	public void onAuthSsoUserInfoGet(int result, String uid, String pwd);
	
}
