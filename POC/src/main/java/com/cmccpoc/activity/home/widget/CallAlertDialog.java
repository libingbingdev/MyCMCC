package com.cmccpoc.activity.home.widget;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.baidu.panosdk.plugin.indoor.util.ScreenUtils;
import com.cmccpoc.R;
import com.cmccpoc.activity.SessionNewActivity;
import com.cmccpoc.activity.VideoSessionActivity;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.activity.home.PTTFragment;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.listener.OnMmiSessionListener;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.Sound;

/**
 * 临时呼叫的接听和挂断弹窗（主叫）
 *
 * @author Yao
 */
public class CallAlertDialog extends Dialog implements OnMmiSessionListener {
    private String sessionCode;
    private boolean sessionWithVideo = false;
    private AirSession session;
    protected int ivCallResource = R.drawable.ic_dialog_outgoing;
    protected ImageView ivCallImage;
    private Context context;
    protected TextView tvTitle;
    String title = null;
    private Button mOk,mBack;

    public interface OnAlertDialogCancelListener {
        public void onDialogCancel(int reason);
    }

    OnAlertDialogCancelListener listener;

    public CallAlertDialog(Context context, String title, String content, String sessionCode, int id, boolean withVideo, OnAlertDialogCancelListener l) {

        this(context, title, content, sessionCode, id);
        this.context = context;
        this.title = title;
        this.sessionWithVideo = withVideo;
        this.listener = l;
    }

    public CallAlertDialog(Context context, String title, String content, String sessionCode, int id) {
        super(context, R.style.alert_dialog);
        this.sessionCode = sessionCode;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        lp.width = ScreenUtils.getScreenWidth(context);
        lp.height = ScreenUtils.getSreenHeight(context);
        dialogWindow.setAttributes(lp);
        setContentView(R.layout.dialog_call_receiver_layout);
        initView();
        fillView();

        session = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode);
        session.setCaller(AirtalkeeAccount.getInstance().getUser());
        AirSessionControl.getInstance().setOnMmiSessionListener(this);
        if (null != session) {
            if (session.getSpecialNumber() == 0) {
                AirSessionControl.getInstance().SessionMakeCall(session);
            } else {
                AirSessionControl.getInstance().SessionMakeSpecialCall(session);
            }
            AirtalkeeMessage.getInstance().MessageSystemGenerate(session, getContext().getString(R.string.talk_call_state_outgoing_call), false);
        }


    }


    /**
     * 根据不同的构造函数，确定是否显示取消按钮等
     */
    protected void fillView() {
        tvTitle.setText(title);
        ivCallImage.setImageResource(ivCallResource);
    }


    protected void initView() {
        ivCallImage = (ImageView) findViewById(R.id.iv_call_image);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        mOk= (Button) findViewById(R.id.ok);
        mOk.setVisibility(View.GONE);
        mBack= (Button) findViewById(R.id.back);
        mBack.setText(R.string.cancle);
    }

    /**
     * 挂断会话
     */
    private void finishCall() {
        if (session != null && session.getSessionState() != AirSession.SESSION_STATE_IDLE) {
            AirSessionControl.getInstance().SessionEndCall(session);
        }
    }


    @Override
    public void onSessionOutgoingRinging(AirSession session) {

    }

    @Override
    public void onSessionEstablishing(AirSession session) {

    }

    @Override
    public void onSessionEstablished(AirSession session, int result) {
        if (session != null) {
            AirtalkeeSessionManager.getInstance().getSessionByCode(session.getSessionCode());
            // AirtalkeeSessionManager.getInstance().SessionLock(session, false);
            final HomeActivity mInstance = HomeActivity.getInstance();
            if (mInstance != null) {
                mInstance.onViewChanged(session.getSessionCode());
                mInstance.pageIndex = HomeActivity.PAGE_PTT;
                mInstance.panelCollapsed();
            }
            if (SessionNewActivity.getInstance() != null) {
                SessionNewActivity.getInstance().finish();
            }

            if (sessionWithVideo) {
                Intent i = new Intent();
                i.setClass(getContext(), VideoSessionActivity.class);
                i.putExtra("sessionCode", session.getSessionCode());
                i.putExtra("auto", true);
                getContext().startActivity(i);
            }
        }
        //AirSessionControl.getInstance().setOnMmiSessionListener(null);
        this.cancel();
    }

    @Override
    public void onSessionReleased(AirSession session, int reason) {
        //AirSessionControl.getInstance().setOnMmiSessionListener(null);
        this.cancel();
        this.session = null;
        if (listener != null) {
            listener.onDialogCancel(reason);
        }
    }

    @Override
    public void onSessionPresence(AirSession session, List<AirContact> membersAll, List<AirContact> membersPresence) {

    }

    @Override
    public void onSessionMemberUpdate(AirSession session, List<AirContact> members, boolean isOk) {

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
        // TODO Auto-generated method stub

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ENDCALL: {
                mBack.setBackgroundResource(R.drawable.bg_list_normal);
                finishCall();
                dismiss();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


}
