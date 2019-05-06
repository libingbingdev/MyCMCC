package com.cmccpoc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.cmccpoc.R;

public class LoginActivity extends Activity implements View.OnClickListener {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private static final int REQUEST_INPUT = 1;

    private Button mOk = null;
    private Button mBack = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mOk = (Button)findViewById(R.id.ok);
        mBack = (Button)findViewById(R.id.back);

        Button auto = (Button) findViewById(R.id.button_auto);
        auto.setOnClickListener(this);
        Button manual = (Button) findViewById(R.id.button_manual);
        manual.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button_auto:
                setResult(RESULT_CANCELED);
                finish();
                break;

            case R.id.button_manual:
                startInput();
                break;

            default:
                Log.e(TAG, "onClick: view is " + view);
                break;
        }
    }

    private void startInput() {
        Intent intent = new Intent(this, InputActivity.class);
        intent.putExtra(InputActivity.PHONE, "");
        intent.putExtra(InputActivity.PASSWORD, "");
        startActivityForResult(intent, REQUEST_INPUT);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_MENU) {
            mOk.setBackgroundResource(R.drawable.bg_list_focuse);
        } else if(keyCode == KeyEvent.KEYCODE_BACK) {
            mBack.setBackgroundResource(R.drawable.bg_list_focuse);
        } else {
            Log.d(TAG, "onKeyDown:keyCode is " + keyCode);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp:keyCode is " + keyCode);
        if(keyCode == KeyEvent.KEYCODE_MENU) {
            mOk.setBackgroundResource(R.drawable.bg_list_normal);
            View view = this.getWindow().getDecorView().findFocus();
            if(view.getId() == R.id.button_manual) {
                startInput();
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_BACK) {
            mBack.setBackgroundResource(R.drawable.bg_list_normal);
            setResult(RESULT_CANCELED);
            finish();
            return true;
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_INPUT) {
            if(resultCode == RESULT_OK) {
                setResult(resultCode, data);
                finish();
            } else {
                Log.d(TAG, "onActivityResult:resultCode is " + resultCode);
            }
        } else {
            Log.e(TAG, "onActivityResult:requestCode is " + requestCode);
        }
    }
}
