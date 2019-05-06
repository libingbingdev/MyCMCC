package com.cmccpoc.activity.home;
import com.cmccpoc.R;
import com.cmccpoc.util.Sound;
import com.cmccpoc.util.Util;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ShowTestActivity extends BaseActivity implements View.OnClickListener {
    private Button mStartBtn,mStopBtn;
    private TextView mCores,mMainFreq;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_test);
       /* WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        mLcdReso=(TextView) findViewById(R.id.lcd_info_resolution);
        mLcdReso.setText(screenWidth+"*"+screenHeight);*/
        mCores=(TextView) findViewById(R.id.cpu_info_kenel);
        if(Util.getNumberOfCPUCores()>1){
            mCores.setText(getString(R.string.cpu_info_summary_kenels));
        }
        mMainFreq=(TextView) findViewById(R.id.cpu_info_mainfq);
        mMainFreq.setText(getString(R.string.cpu_info_summary_mainfq)+String.format("%.1f", (double)Util.getCPUMaxFreqKHz()/1024/1024)+"Ghz");
        mStartBtn=(Button) findViewById(R.id.start_volume_btn);
        mStartBtn.setOnClickListener(this);
        mStopBtn=(Button) findViewById(R.id.stop_volume_btn);
        mStopBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_volume_btn:
                Sound.playSound(Sound.PLAYER_VOLUM_TEST2,this);
                break;
            case R.id.stop_volume_btn:
                Sound.stopSound(Sound.PLAYER_VOLUM_TEST2);
                break;
        }
    }
}
