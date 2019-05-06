package com.cmccpoc.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeVersionUpdate;
import com.airtalkee.sdk.OnVersionUpdateListener;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.widget.DialogVersionUpdate;
import com.cmccpoc.config.Config;
import com.cmccpoc.util.Language;
import com.cmccpoc.util.Util;

/**
 * 更多：关于版本Activity
 * 主要功能包括：检查更新并在线升级、查看运行时长与流量消耗
 *
 * @author Yao
 */
public class MenuUpdateActivity extends ActivityBase implements OnVersionUpdateListener {

    private TextView versionMsg;

    @Override
    protected void onCreate(Bundle bundle) {
        // TODO Auto-generated method stub
        super.onCreate(bundle);
        setContentView(R.layout.activity_tool_about);
        doInitView();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        checkVersion();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    /**
     * 初始化绑定控件Id
     */
    private void doInitView() {
        versionMsg = (TextView) findViewById(R.id.talk_tv_update_msg);
    }


    /**
     * 检查版本
     * 如果有新版本，则弹出提示窗口通知要更新
     */
    private void checkVersion() {
        String lang = Language.getLocalLanguage(MenuUpdateActivity.this);
        String userId = AirtalkeeAccount.getInstance().getUserId();
        String versionCode = Util.appVersion(MenuUpdateActivity.this);
        String imei = Util.getImei(this);
        if (TextUtils.isEmpty(imei))
            Toast.makeText(this, "IMEI为空，无法升级", Toast.LENGTH_LONG).show();
        else
            AirtalkeeVersionUpdate.getInstance().versionCheck(this, userId, Config.marketCode, lang, Config.VERSION_PLATFORM, Config.VERSION_TYPE, Config.model, imei, versionCode, 0);
    }

    @Override
    public void UserVersionUpdate(int versionFlag, String versionInfo, final String url) {
        // versionFlag = 1;
        if (versionFlag == 0) {
            // versionMsg.setVisibility(View.VISIBLE);
            versionMsg.setText(R.string.talk_verion_latest);
            versionMsg.setTextColor(getResources().getColor(R.color.update_text_none));

        } else {
            versionMsg.setText(R.string.talk_version_new);
            versionMsg.setTextColor(getResources().getColor(R.color.update_text_new));
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.talk_verion_title);
            builder.setMessage(versionInfo);
            builder.setPositiveButton(getString(R.string.talk_verion_upeate), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try {
                        dialog.cancel();
                        DialogVersionUpdate update = new DialogVersionUpdate(MenuUpdateActivity.this, url);
                        update.show();
                        versionMsg.setText("更新中...");
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            });
            if (versionFlag == 2) {
                builder.setCancelable(false);
            } else {
                builder.setNegativeButton(getString(R.string.talk_verion_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            dialog.cancel();
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                    }
                });
            }

        }
    }

}
