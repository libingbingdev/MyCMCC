package com.cmccpoc.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.config.Config;
import com.cmccpoc.util.Language;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Util;

/**
 * 更多：广播 获取广播列表,webview而已
 * 
 * @author Yao
 */
public class MenuNoticeActivity extends ActivityBase implements OnClickListener
{
	private final String NOTICE_LIST_BASE = "textAnnouncementAction_listTextUI.action?userId=<UID>&lang=<LANG>&type=1";
	private final String NOTICE_LIST = "textAnnouncementAction_listTextUI.action";
	private final String NOTICE_LIST_ITEM = "comment_text_zq.jsp";

	private WebView webViewList;
	private ProgressBar webViewProgress;

	private boolean isWebContentShowing = false;

	private String contentUrl = "";

	@Override
	protected void onCreate(Bundle bundle)
	{
		Log.e(MenuNoticeActivity.class, "[NOTICE] onCreate");
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_notice);
		doInitView();

		try
		{
			Log.e(MenuNoticeActivity.class, "[NOTICE] onCreate try");
			String url = "";
			bundle = getIntent().getExtras();
			if (bundle != null)
			{
				url = bundle.getString("url");
			}
			Util.closeNotification(Util.NOTIFI_ID_NOTICE);
			AirtalkeeAccount.getInstance().SystemBroadcastNumberClean();
			openWeb(url);
		}
		catch (Exception e)
		{
			Log.e(MenuNoticeActivity.class, "[NOTICE] onCreate catch");
		}
		
	}

	/**
	 * 初始化绑定控件Id
	 */
	@SuppressLint("SetJavaScriptEnabled")
	private void doInitView()
	{
		try
		{
			Log.e(MenuNoticeActivity.class, "[NOTICE] doInitView try");
			TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
			ivTitle.setText(R.string.talk_tools_notice);
			View btnLeft = findViewById(R.id.menu_left_button);
			ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
			ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
			btnLeft.setOnClickListener(this);

			RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
			ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
			ivRight.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_refresh, this));
			ivRightLay.setOnClickListener(this);

			webViewProgress = (ProgressBar) findViewById(R.id.talk_layout_notice_progress);

			webViewList = (WebView) findViewById(R.id.talk_layout_notice_list);
			webViewList.getSettings().setJavaScriptEnabled(true);
			webViewList.setBackgroundColor(0x1f1f1f);
		}
		catch (Exception e)
		{
			Log.e(MenuNoticeActivity.class, "[NOTICE] doInitView catch");
		}

	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		try
		{
			if (webViewList != null)
			{
				Log.e(MenuNoticeActivity.class, "[NOTICE] finish try1 if");
				webViewList.clearHistory();
				webViewList.clearCache(true);
				webViewList.stopLoading();
			}
		}
		catch (Exception e)
		{
			Log.e(MenuNoticeActivity.class, "[NOTICE] finish catch1");
		}
	}

	@Override
	public void finish()
	{
		super.finish();
		try
		{
			if (webViewList != null)
			{
				Log.e(MenuNoticeActivity.class, "[NOTICE] finish try2 if");
				webViewList.removeAllViews();
				webViewList.destroy();
			}
		}
		catch (Exception e)
		{
			Log.e(MenuNoticeActivity.class, "[NOTICE] finish catch2");
		}
		
	}

	/**
	 * 打开web页面，然后用webview显示
	 * 
	 * @param url
	 *            url地址
	 */
	private void openWeb(String url)
	{
		if (!Utils.isEmpty(url))
		{
			if (url.endsWith(AccountController.getDmWebNoticeUrl()))
			{
				url = AccountController.getDmWebNoticeUrl() + NOTICE_LIST_BASE;
			}
			// url = "http://112.33.0.159:2880/airtalkeenotice/jsp/" + NOTICE_LIST_BASE;
			if (url.contains(NOTICE_LIST))
			{
				url = url.replace("<UID>", AirtalkeeAccount.getInstance().getUserId());
				url = url.replace("<LANG>", Language.getLocalLanguageZH(this));
				webLoadList(url);
				isWebContentShowing = false;
			}
		}
	}

	/**
	 * 关闭web页面
	 */
	private void closeWeb()
	{
		try
		{
			if (webViewList != null && !TextUtils.isEmpty(webViewList.getUrl()) && webViewList.getUrl().contains(NOTICE_LIST_ITEM))
			{
				Log.e(MenuNoticeActivity.class, "[NOTICE] closeWeb try if");
				webViewProgress.setVisibility(View.GONE);
				openWeb(AccountController.getDmWebNoticeUrl());
				isWebContentShowing = false;
			}
			else
			{
				Log.e(MenuNoticeActivity.class, "[NOTICE] closeWeb try else");
				finish();
			}
		}
		catch (Exception e)
		{
			Log.e(MenuNoticeActivity.class, "[NOTICE] closeWeb catch");
			finish();
		}
		
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				closeWeb();
				break;
			case R.id.talk_menu_right_button:
			case R.id.bottom_right_icon:
			{
				if (isWebContentShowing)
				{
					if (!Utils.isEmpty(contentUrl))
					{
						openWeb(contentUrl);
					}
				}
				else
				{
					webViewList.reload();
				}
				break;
			}
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getAction() == KeyEvent.ACTION_DOWN)
		{
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
			{
				closeWeb();
				return true;
			}
			else if (event.getKeyCode() == KeyEvent.KEYCODE_HOME)
			{
				finish();
			}
		}
		return super.dispatchKeyEvent(event);
	}

	private void webLoadList(String url)
	{
		if (!Utils.isEmpty(url))
		{
			webViewList.setWebChromeClient(new WebChromeClient()
			{
				@Override
				public void onProgressChanged(WebView view, int progress)
				{
					webViewProgress.setProgress(progress * 1000);
				}

			});

			webViewList.setWebViewClient(new WebViewClient()
			{
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url)
				{
					boolean isHandled = false;
					return isHandled;
				}

				@Override
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
				{
				}

				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon)
				{
					// TODO Auto-generated method stub
					super.onPageStarted(view, url, favicon);
					webViewProgress.setVisibility(View.VISIBLE);
				}

				@Override
				public void onPageFinished(WebView view, String url)
				{
					// TODO Auto-generated method stub
					super.onPageFinished(view, url);
					webViewProgress.setVisibility(View.GONE);
				}
			});

			webViewList.loadUrl(url);
		}
	}
}
