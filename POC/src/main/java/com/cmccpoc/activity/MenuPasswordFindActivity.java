package com.cmccpoc.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.sdk.OnUserPwdFindListener;
import com.airtalkee.sdk.controller.AccountInfoController;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.config.Config;
import com.cmccpoc.util.AirMmiTimer;
import com.cmccpoc.util.AirMmiTimerListener;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.Util;


/**
 更多：修改密码
 密码规则：6~15位数字 or 字母
 @author Yao */
public class MenuPasswordFindActivity extends ActivityBase implements OnClickListener, OnUserPwdFindListener, AirMmiTimerListener, OnCheckedChangeListener
{
    private static MenuPasswordFindActivity mInstance;
    private LinearLayout mLayoutBind;
    private RelativeLayout mLayoutPwd;
    private TextView verify_name, verify_tip;
    private EditText verify_text;
    private EditText pwd_text_new, pwd_text_confirm;
    private CheckBox pwd_check;
    private Button verify_button, pwd_button;

    private static final int UI_STATE_PHONE = 0;
    private static final int UI_STATE_VERIFY_CODE = 1;
    private static final int UI_STATE_PWD = 2;

    private final static int TIMEOUT = 60;
    private static int mState = UI_STATE_PHONE;
    private static int mTimer = TIMEOUT;
    private static boolean mTimerRunning = false;
    private static String mUserPhone = "";
    private static String mVerifyCode = "";

    public static boolean isFindingPwd = false;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle bundle)
    {
        // TODO Auto-generated method stub
        super.onCreate(bundle);
        Log.e(MenuPasswordFindActivity.class, "MenuPasswordFindActivity onCreate");
        // setRequestedOrientation(Config.screenOrientation);
        setContentView(R.layout.activity_tool_password_find);
        doInitView();
        if (mInstance != null)
        {
            AirMmiTimer.getInstance().TimerUnregister(mInstance, mInstance);
            if (mTimer > 0)
                AirMmiTimer.getInstance().TimerRegister(this, this, false, false, 1000, true, null);
            else
            {
                mTimerRunning = false;
                verify_tip.setText(getString(R.string.talk_pwd_find_re_get_again));
                verify_tip.setOnClickListener(this);
            }
        }
        refreshUI();
        isFindingPwd = true;
        mInstance = this;
    }

    /**
     初始化绑定控件Id
     */
    private void doInitView()
    {

        TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
        ivTitle.setText(R.string.talk_tools_pwd_find);
        View btnLeft = findViewById(R.id.menu_left_button);
        ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
        ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
        btnLeft.setOnClickListener(this);

        RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
        ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
        ivRight.setVisibility(View.GONE);
        ivRightLay.setVisibility(View.INVISIBLE);

        AccountInfoController.setUserPwdFindListener(this);

        mLayoutBind = (LinearLayout) findViewById(R.id.layout_verify);
        mLayoutPwd = (RelativeLayout) findViewById(R.id.layout_panel);
        verify_tip = (TextView) findViewById(R.id.talk_verify_tip);
        verify_name = (TextView) findViewById(R.id.talk_verify_name);
        verify_text = (EditText) findViewById(R.id.talk_verify_text);
        verify_text.addTextChangedListener(textWatcherVerify);
        verify_button = (Button) findViewById(R.id.talk_verify_button);
        verify_button.setOnClickListener(this);

        pwd_button = (Button) findViewById(R.id.btn_change_password);
        pwd_button.setOnClickListener(this);
        pwd_text_new = (EditText) findViewById(R.id.new_password);
        pwd_text_new.addTextChangedListener(textWatcherPwd);
        pwd_text_confirm = (EditText) findViewById(R.id.new_password_confirm);
        pwd_text_confirm.addTextChangedListener(textWatcherPwd);
        pwd_check = (CheckBox) findViewById(R.id.show_password);
        pwd_check.setOnCheckedChangeListener(this);

        checkEditTextVerify();
        checkEditTextPwd();
    }

    @Override
    public void finish()
    {
        super.finish();
        Log.e(MenuPasswordFindActivity.class, "MenuPasswordFindActivity finish");
        AirMmiTimer.getInstance().TimerUnregister(this, this);
        isFindingPwd = false;
        mTimerRunning = false;
        mTimer = TIMEOUT;
        mState = UI_STATE_PHONE;
        mUserPhone = "";
        mVerifyCode = "";
        mInstance = null;
    }

    private void refreshUI()
    {
        switch (mState)
        {
            case UI_STATE_PHONE:
            {
                verify_name.setText(getString(R.string.talk_pwd_find_bind_phone));
                mLayoutBind.setVisibility(View.VISIBLE);
                mLayoutPwd.setVisibility(View.GONE);
                verify_tip.setVisibility(View.GONE);
                verify_button.setText(getString(R.string.talk_pwd_find_get_verify_code));
                break;
            }
            case UI_STATE_VERIFY_CODE:
            {
                if (!mTimerRunning && mTimer != 0)
                {
                    AirMmiTimer.getInstance().TimerRegister(this, this, false, false, 1000, true, null);
                    mTimerRunning = true;
                }
                verify_name.setText(getString(R.string.talk_pwd_find_get_verify_code));
                mLayoutBind.setVisibility(View.VISIBLE);
                mLayoutPwd.setVisibility(View.GONE);
                verify_tip.setVisibility(View.VISIBLE);
                verify_button.setText(getString(R.string.talk_pwd_find_get_verify_confirm));
                break;
            }
            case UI_STATE_PWD:
            {
                mLayoutBind.setVisibility(View.GONE);
                mLayoutPwd.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        // TODO Auto-generated method stub
        switch (v.getId())
        {
            case R.id.menu_left_button:
            case R.id.bottom_left_icon:
            {
                showFinishDialog();
                break;
            }
            case R.id.talk_verify_button:
            {
                if (mState == UI_STATE_PHONE)
                {
                    // if (TextUtils.isEmpty(verify_text.getText()) || verify_text.getText().length() < 11)
                    if (!Util.isMobile(verify_text.getText().toString().trim()))
                        if(Toast.isDebug) Toast.makeText1(this, getString(R.string.talk_pwd_find_tip_not_phone), Toast.LENGTH_SHORT).show();
                    else
                    {
                        mUserPhone = verify_text.getText().toString();
                        showDialog(R.id.talk_dialog_waiting);
                        AccountInfoController.userPwdFindGetVerifyCode(mUserPhone, Config.serverDomain);
                        /*
                        verify_text.setText("");
                        verify_tip.setText("");
                        mState = UI_STATE_VERIFY_CODE;
                        */
                        refreshUI();
                    }
                }
                else if (mState == UI_STATE_VERIFY_CODE)
                {
                    int ret = AccountInfoController.userPwdVerifyCodeLocalCheck(verify_text.getText().toString());
                    if (ret != 0)
                    {
                        mVerifyCode = verify_text.getText().toString();
                        mState = UI_STATE_PWD;
                        refreshUI();
                    }
                    else
                        if(Toast.isDebug) Toast.makeText1(this, getString(R.string.talk_pwd_find_tip_verify_error), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.talk_verify_tip:
            {
                verify_text.setText("");
                mTimer = TIMEOUT;
                mTimerRunning = false;
                mState = UI_STATE_VERIFY_CODE;
                refreshUI();
                showDialog(R.id.talk_dialog_waiting);
                AccountInfoController.userPwdFindGetVerifyCode(mUserPhone, Config.serverDomain);
                break;
            }
            case R.id.btn_change_password:
            {
                if (TextUtils.equals(pwd_text_new.getText(), pwd_text_confirm.getText()))
                {
                    showDialog(R.id.talk_dialog_waiting);
                    AccountInfoController.userPwdFindUpdateNew(mUserPhone, Config.serverDomain, mVerifyCode, pwd_text_new.getText().toString());
                }
                else
                    Util.Toast(this, getString(R.string.talk_pwd_confirm_not_equals), R.drawable.ic_error);
                break;
            }
            default:
                break;
        }
    }

    protected Dialog onCreateDialog(int id)
    {
        if (id == R.id.talk_dialog_waiting)
        {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.talk_tip_waiting));
            dialog.setCancelable(false);
            return dialog;
        }
        return super.onCreateDialog(id);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            showFinishDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showFinishDialog()
    {
        if (dialog != null && dialog.isShowing())
            return;
        dialog = new AlertDialog(this, getString(R.string.talk_pwd_find_exit_tip), null, new AlertDialog.DialogListener()
        {
            @Override
            public void onClickOk(int id, Object obj)
            {
                finish();
            }

            @Override
            public void onClickOk(int id, boolean isChecked)
            {

            }

            @Override
            public void onClickCancel(int id)
            {

            }
        }, -1);
        dialog.show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        switch (buttonView.getId())
        {
            case R.id.show_password:
            {
                if (isChecked)
                {
                    pwd_text_new.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    pwd_text_confirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    pwd_text_new.setSelection(pwd_text_new.getText().toString().length());
                    pwd_text_confirm.setSelection(pwd_text_confirm.getText().toString().length());
                }
                else
                {
                    pwd_text_new.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    pwd_text_confirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    pwd_text_new.setSelection(pwd_text_new.getText().toString().length());
                    pwd_text_confirm.setSelection(pwd_text_confirm.getText().toString().length());
                }
                break;
            }
        }
    }

    @Override
    public void onMmiTimer(Context context, Object userData)
    {
        if (mState == UI_STATE_VERIFY_CODE)
        {
            mTimer--;
            if (mTimer <= 0)
            {
                verify_tip.setText(getString(R.string.talk_pwd_find_re_get_again));
                verify_tip.setOnClickListener(this);
                AirMmiTimer.getInstance().TimerUnregister(this, this);
                mTimerRunning = false;
            }
            else
            {
                String tip = String.format(getString(R.string.talk_pwd_find_re_get), mTimer);
                verify_tip.setVisibility(View.VISIBLE);
                verify_tip.setOnClickListener(null);
                verify_tip.setText(tip);
            }
        }
    }

    private TextWatcher textWatcherVerify = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            checkEditTextVerify();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            checkEditTextVerify();
        }

        @Override
        public void afterTextChanged(Editable s)
        {
            checkEditTextVerify();
        }
    };

    private TextWatcher textWatcherPwd = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            checkEditTextPwd();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            checkEditTextPwd();
        }

        @Override
        public void afterTextChanged(Editable s)
        {
            checkEditTextPwd();
        }
    };

    private void checkEditTextVerify()
    {
        if (TextUtils.isEmpty(verify_text.getText()))
        {
            verify_button.setClickable(false);
            verify_button.setBackgroundResource(R.drawable.btn_bg_gray);
        }
        else
        {
            verify_button.setClickable(true);
            verify_button.setBackgroundResource(R.drawable.btn_bg_normal);
        }
    }

    private void checkEditTextPwd()
    {
        if (TextUtils.isEmpty(pwd_text_new.getText()) || TextUtils.isEmpty(pwd_text_confirm.getText()))
        {
            pwd_button.setClickable(false);
            pwd_button.setBackgroundResource(R.drawable.btn_bg_gray);
        }
        else if (pwd_text_new.getText().length() < 6 || pwd_text_confirm.getText().length() < 6)
        {
            pwd_button.setClickable(false);
            pwd_button.setBackgroundResource(R.drawable.btn_bg_gray);
        }
        else
        {
            pwd_button.setClickable(true);
            pwd_button.setBackgroundResource(R.drawable.btn_bg_normal);
        }
    }

    @Override
    public void onUserPwdFindGetVerifyCode(int result)
    {
        removeDialog(R.id.talk_dialog_waiting);
        if (result == OnUserPwdFindListener.PWD_FIND_RESULT_OK)
        {
            verify_text.setText("");
            verify_tip.setText("");
            mState = UI_STATE_VERIFY_CODE;
            refreshUI();
        }
        else if (result == OnUserPwdFindListener.PWD_FIND_RESULT_USER_INVALID)
            if(Toast.isDebug) Toast.makeText1(this, getString(R.string.talk_pwd_find_user_not_found), Toast.LENGTH_LONG).show();
        else
            if(Toast.isDebug) Toast.makeText1(this, getString(R.string.talk_pwd_find_failed), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUserPwdFindUpdateNew(int result)
    {
        removeDialog(R.id.talk_dialog_waiting);
        if (result == OnUserPwdFindListener.PWD_FIND_RESULT_OK)
        {
            if(Toast.isDebug) Toast.makeText1(this, getString(R.string.talk_pwd_find_success), Toast.LENGTH_LONG).show();
            finish();
        }
        else if (result == OnUserPwdFindListener.PWD_FIND_RESULT_VERIFY_CODE_ERR)
            if(Toast.isDebug) Toast.makeText1(this, getString(R.string.talk_pwd_find_verify_code_failed), Toast.LENGTH_LONG).show();
        else
            if(Toast.isDebug) Toast.makeText1(this, getString(R.string.talk_pwd_find_failed), Toast.LENGTH_LONG).show();
    }
}
