package com.cmccpoc.activity.home.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;

import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.panosdk.plugin.indoor.util.ScreenUtils;
import com.cmccpoc.R;
import com.cmccpoc.activity.SessionNewActivity;
import com.cmccpoc.activity.VideoSessionActivity;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.activity.home.PTTFragment;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.Sound;

/**
 * 临时呼叫的接听和挂断弹窗（被叫）
 *
 * @author Yao
 */
public class InCommingAlertDialog extends Dialog implements DialogListener {
    private AirSession temAirSession;
    private boolean mVideoPull = false;
    protected int ivCallResource = R.drawable.ic_dialog_incoming;
    protected ImageView ivCallImage;
    protected TextView tvTitle;
    private Context context;
    private String title = null;
    private Button mOk,mBack;

    public InCommingAlertDialog(Context ct, AirSession s, AirContact caller, boolean isVideoPush, boolean isVideoPull) {

        super(ct, R.style.alert_dialog);
        Log.d("zlm", "InCommingAlertDialog");
        context = ct;
        temAirSession = s;
        final AirSession airSession = SessionController.SessionMatchSpecial(AirtalkeeSessionManager.SPECIAL_NUMBER_DISPATCHER, ct.getString(R.string.talk_tools_call_center));
        if (s != null) {
            if(s.equals(airSession)){//控制台呼入
                title = ct.getString(R.string.talk_call_center_text) + ct.getString(R.string.text_incomeing);
            }else {
                title = caller.getDisplayName() + ct.getString(R.string.talk_incoming);
            }
        }
        mVideoPull = isVideoPull;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        Log.d("zlm", "onCreate");
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        lp.width = ScreenUtils.getScreenWidth(context);
        lp.height = ScreenUtils.getSreenHeight(context);
        dialogWindow.setAttributes(lp);

        super.onCreate(savedInstanceState);
        this.setCancelable(false);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        setContentView(R.layout.dialog_call_receiver_layout);
        initView();
        fillView();
    }


    protected void fillView() {

        ivCallImage.setImageResource(ivCallResource);
        tvTitle.setText(title);
    }


    protected void initView() {
        ivCallImage = (ImageView) findViewById(R.id.iv_call_image);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        mOk= (Button) findViewById(R.id.ok);
        mBack= (Button) findViewById(R.id.back);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Intent i = new Intent();
            i.setClass(AirServices.getInstance(), VideoSessionActivity.class);
            i.putExtra("sessionCode", temAirSession.getSessionCode());
            i.putExtra("auto", true);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            AirServices.getInstance().startActivity(i);
        }
    };

    @Override
    public void onClickOk(int id, Object obj) {
        Log.i(InCommingAlertDialog.class.toString(), "InCommingAlertDialog click ok!");
        Sound.stopSound(Sound.PLAYER_INCOMING_RING);
        AirtalkeeSessionManager.getInstance().SessionIncomingAccept(temAirSession);
        AirtalkeeMessage.getInstance().MessageSystemGenerate(temAirSession, getContext().getString(R.string.talk_call_state_incoming_call), false);
        switchToSessionDialog(temAirSession);
        if (!HomeActivity.isShowing) {
            Intent home = new Intent(AirServices.getInstance(), HomeActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            AirServices.getInstance().startActivity(home);
        }
        if (PTTFragment.getInstance() != null)
            PTTFragment.getInstance().getVideoPannel().setVisibility(View.GONE);
        if (mVideoPull && Config.funcVideoPull) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(), 1000);
        }
        this.cancel();
    }

    @Override
    public void onClickCancel(int id) {
        try {
            Sound.stopSound(Sound.PLAYER_INCOMING_RING);
            if (temAirSession.getSessionState() != AirSession.SESSION_STATE_DIALOG) {
                AirtalkeeSessionManager.getInstance().SessionIncomingReject(temAirSession);
                AirtalkeeMessage.getInstance().MessageSystemGenerate(temAirSession, temAirSession.getCaller(), getContext().getString(R.string.talk_call_state_rejected_call), true);
            }
            this.cancel();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_MENU:
                mOk.setBackgroundResource(R.drawable.bg_list_focuse);
                break;
            case KeyEvent.KEYCODE_BACK:
                mBack.setBackgroundResource(R.drawable.bg_list_focuse);
                break;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_CALL: {
                mBack.setBackgroundResource(R.drawable.bg_list_normal);
                Log.i(InCommingAlertDialog.class.toString(), "InCommingAlertDialog click ok!");
                Sound.stopSound(Sound.PLAYER_INCOMING_RING);
                AirtalkeeSessionManager.getInstance().SessionIncomingAccept(temAirSession);
                AirtalkeeMessage.getInstance().MessageSystemGenerate(temAirSession, getContext().getString(R.string.talk_call_state_incoming_call), false);
                switchToSessionDialog(temAirSession);
                if (!HomeActivity.isShowing) {
                    Intent home = new Intent(AirServices.getInstance(), HomeActivity.class);
                    home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    AirServices.getInstance().startActivity(home);
                }
                if (PTTFragment.getInstance() != null)
                    PTTFragment.getInstance().getVideoPannel().setVisibility(View.GONE);
                if (mVideoPull && Config.funcVideoPull) {
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(), 1000);
                }
                this.cancel();
                return false;
            }
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ENDCALL: {
                mBack.setBackgroundResource(R.drawable.bg_list_normal);
                try {
                    Sound.stopSound(Sound.PLAYER_INCOMING_RING);
                    if (temAirSession.getSessionState() != AirSession.SESSION_STATE_DIALOG) {
                        AirtalkeeSessionManager.getInstance().SessionIncomingReject(temAirSession);
                        AirtalkeeMessage.getInstance().MessageSystemGenerate(temAirSession, temAirSession.getCaller(), getContext().getString(R.string.talk_call_state_rejected_call), true);
                    }
                    this.cancel();
                } catch (Exception e) {
                    // TODO: handle exception
                }
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 从当前会话切换到临时会话
     *
     * @param session 会话Entity
     */
    private void switchToSessionDialog(AirSession session) {
        //AirSessionControl.getInstance().setOnMmiSessionListener(null);
        if (session != null) {
            AirtalkeeSessionManager.getInstance().getSessionByCode(session.getSessionCode());
            HomeActivity mInstance = HomeActivity.getInstance();
            if (mInstance != null) {
                mInstance.onViewChanged(session.getSessionCode());
                mInstance.pageIndex = HomeActivity.PAGE_PTT;
                mInstance.panelCollapsed();
            }
            if (SessionNewActivity.getInstance() != null) {
                SessionNewActivity.getInstance().finish();
            }
        }
        this.cancel();
    }

    @Override
    public void onClickOk(int id, boolean isChecked) {
        // TODO Auto-generated method stub

    }
}
