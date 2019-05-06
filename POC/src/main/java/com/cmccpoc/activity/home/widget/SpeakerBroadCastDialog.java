package com.cmccpoc.activity.home.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.panosdk.plugin.indoor.util.ScreenUtils;
import com.cmccpoc.R;

/**
 * 临时呼叫的接听和挂断弹窗（主叫）
 *
 * @author Yao
 */
public class SpeakerBroadCastDialog extends Dialog  {
    private Context context;
    protected TextView tvSpeakerName;
    String title = null;


    public interface OnAlertDialogCancelListener {
        public void onDialogCancel(int reason);
    }

    public SpeakerBroadCastDialog(Context context, String title) {

        this(context);
        this.context = context;
        this.title = title;
    }

    public SpeakerBroadCastDialog(Context context) {
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

        setContentView(R.layout.dialog_broadcast_layout);
        initView();
        fillView();



    }

    /**
     * 根据不同的构造函数，确定是否显示取消按钮等
     */
    protected void fillView() {
        tvSpeakerName.setText(title);
    }


    protected void initView() {
        tvSpeakerName = (TextView) findViewById(R.id.speaker_name);
    }





}
