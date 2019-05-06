package com.cmccpoc.activity.home.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.baidu.panosdk.plugin.indoor.util.ScreenUtils;
import com.cmccpoc.R;
import com.cmccpoc.activity.SessionNewActivity;
import com.cmccpoc.activity.VideoSessionActivity;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.listener.OnMmiSessionListener;

import java.util.List;

/**
 * 临时呼叫的接听和挂断弹窗（主叫）
 *
 * @author Yao
 */
public class WarningDialog extends Dialog  {
    protected int ivCallResource = R.drawable.ic_warning;
    protected ImageView ivCallImage;

    private Context context;
    protected TextView tvTitle;
    String title = null;
    private Button mOk,mBack;

    public interface OnAlertDialogCancelListener {
        public void onDialogCancel(int reason);
    }

    public WarningDialog(Context context, String title,  int resid) {

        this(context);
        this.context = context;
        this.title = title;
        this.ivCallResource=resid;
    }

    public WarningDialog(Context context) {
        super(context, R.style.alert_dialog);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        lp.width = ScreenUtils.getScreenWidth(context);
        lp.height = ScreenUtils.getSreenHeight(context);
        dialogWindow.setAttributes(lp);

        setContentView(R.layout.dialog_warning_layout);
        initView();
        fillView();



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
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
               return false;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                return false;
        }
        return super.onKeyDown(keyCode,event);
    }



}
