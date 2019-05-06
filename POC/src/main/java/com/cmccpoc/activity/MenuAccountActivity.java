package com.cmccpoc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.OnUserInfoListener;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactGroup;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.BaseActivity;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.config.Config;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Util;

import java.util.List;

/**
 * 更多：账户管理Activity
 * 主要功能包括：修改名称，修改密码，退出登录。
 *
 * @author Yao
 */
public class MenuAccountActivity extends BaseActivity implements OnUserInfoListener {
    public TextView tvUserName;
    public TextView tvUserIpocid;

    AlertDialog dialog;
    private Button mOk, mBack;

    @Override
    protected void onCreate(Bundle bundle) {
        // TODO Auto-generated method stub
        super.onCreate(bundle);
        setRequestedOrientation(Config.screenOrientation);
        setContentView(R.layout.activity_tool_account);
        doInitView();
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
        TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
        ivTitle.setText(R.string.talk_user_account_manage);
        View btnLeft = findViewById(R.id.menu_left_button);
        ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
        ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
        //btnLeft.setOnClickListener(this);

        RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
        ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
        ivRight.setVisibility(View.GONE);
        ivRightLay.setVisibility(View.INVISIBLE);

        tvUserName = (TextView) findViewById(R.id.talk_tv_user_name);
        tvUserName.setText(AirtalkeeAccount.getInstance().getUserName());

        tvUserIpocid = (TextView) findViewById(R.id.talk_tv_user_ipocid);
        tvUserIpocid.setText(AirtalkeeAccount.getInstance().getUserId());
        mOk= (Button) findViewById(R.id.ok);
        mOk.setVisibility(View.GONE);
        mBack= (Button) findViewById(R.id.back);

    }


    @Override
    public void onUserInfoGet(AirContact user) {
        if (user != null) {
            tvUserName.setText(user.getDisplayName());
        }
    }

    @Override
    public void onUserInfoUpdate(boolean isOk, AirContact user) {
        if (isOk) {
            tvUserName.setText(user.getDisplayName());
            Util.Toast(this, getString(R.string.talk_user_info_update_name_ok));
        } else {
            Util.Toast(this, getString(R.string.talk_user_info_update_name_fail));
        }

    }

    @Override
    public void onUserIdGetByPhoneNum(int result, AirContact contact) {

    }

    @Override
    public void onUserOrganizationTree(boolean isOk, AirContactGroup org) {

    }

    @Override
    public void onUserOrganizationTreeSearch(boolean isOk, List<AirContact> contacts) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (requestCode == 1) {
                super.onActivityResult(requestCode, resultCode, data);
                /* 取得来自SecondActivity页面的数据，并显示到画面 */
                Bundle bundle = data.getExtras();
                /* 获取Bundle中的数据，注意类型和key */
                String name = bundle.getString("newUserName");
                tvUserName.setText(name);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                mBack.setBackgroundResource(R.drawable.bg_list_focuse);
                break;
        }
        return super.onKeyDown(keyCode,event);
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                mBack.setBackgroundResource(R.drawable.bg_list_normal);
                finish();
                break;
        }
        return super.onKeyUp(keyCode, event);
    }


}
