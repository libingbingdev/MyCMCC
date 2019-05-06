package com.cmccpoc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cmccpoc.R;

public class InputActivity extends Activity {
    private static final String TAG = InputActivity.class.getSimpleName();

    private Button mOk = null;
    private Button mBack = null;

    public static final String PHONE = "phone";
    public static final String PASSWORD = "password";

    private LinearLayout mPhoneLayout = null;
    private LinearLayout mPasswordLayout = null;
    private EditText mPhone = null;
    private EditText mPassword = null;

    private static final int PHONE_INPUT = 0;
    private static final int PASSWORD_INPUT = 1;

    private int mCurr = PHONE_INPUT;

    private static final int UNKNOW_METHOD = 0;
    private static final int NUMBER_METHOD = 1;
    private static final int UPPEREN_METHOD = 2;
    private static final int LOWEREN_METHOD = 3;
    private static final int SYMBOL_METHOD = 4;

    private LinearLayout mNumberSoftkeyboard = null;
    private LinearLayout mUpperenSoftkeyboard = null;
    private LinearLayout mLowerenSoftkeyboard = null;
    private LinearLayout mSymbolSoftkeyboard = null;

    private int mMethod = UNKNOW_METHOD;

    private static final int KEY_UP = KeyEvent.KEYCODE_DPAD_UP;
    private static final int KEY_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
    private static final int KEY_LEFT = KeyEvent.KEYCODE_MENU;
    private static final int KEY_RIGHT = KeyEvent.KEYCODE_BACK;

    private static final int ROW_COUNT = 4;
    private static final int COL_COUNT = 4;

    private View[][] mNumbers = new View[ROW_COUNT][COL_COUNT];
    private static final int[][] NUMBER_BUTTONS = {
            {R.id.button1, R.id.button2, R.id.button3, R.id.button4},
            {R.id.button5, R.id.button6, R.id.button7, R.id.button8},
            {R.id.buttonUpperenN, R.id.button9, R.id.button0, R.id.buttonDeleteN},
            {R.id.buttonOkN, R.id.buttonInvalidN1, R.id.buttonInvalidN2, R.id.buttonBackN}
    };

    private View[][] mUpperens = new View[ROW_COUNT][COL_COUNT];
    private static final int[][] UPPEREN_BUTTONS = {
            {R.id.buttonABC, R.id.buttonDEF, R.id.buttonGHI, R.id.buttonJKL},
            {R.id.buttonMNO, R.id.buttonPQRS, R.id.buttonTUV, R.id.buttonWXYZ},
            {R.id.buttonLowerenU, R.id.buttonNumberU, R.id.buttonSymbolU, R.id.buttonDeleteU},
            {R.id.buttonOkU, R.id.buttonInvalidU1, R.id.buttonInvalidU2, R.id.buttonBackU}
    };

    private View[][] mLowerens = new View[ROW_COUNT][COL_COUNT];
    private static final int[][] LOWEREN_BUTTONS = {
            {R.id.buttonabc, R.id.buttondef, R.id.buttonghi, R.id.buttonjkl},
            {R.id.buttonmno, R.id.buttonpqrs, R.id.buttontuv, R.id.buttonwxyz},
            {R.id.buttonUpperenL, R.id.buttonNumberL, R.id.buttonSymbolL, R.id.buttonDeleteL},
            {R.id.buttonOkL, R.id.buttonInvalidL1, R.id.buttonInvalidL2, R.id.buttonBackL}
    };

    private View[][] mSymbols = new View[ROW_COUNT][COL_COUNT];
    private static final int[][] SYMBOL_BUTTONS = {
            {R.id.buttonAdd, R.id.buttonDot, R.id.buttonSub, R.id.buttonLp},
            {R.id.buttonAt, R.id.buttonSpace, R.id.buttonPound, R.id.buttonStar},
            {R.id.buttonLowerenS, R.id.buttonNumberS, R.id.buttonLine, R.id.buttonDeleteS},
            {R.id.buttonOkS, R.id.buttonInvalidS1, R.id.buttonInvalidS2, R.id.buttonBackS}
    };

    private int mRow = 0, mCol = 0;

    private static final int[] ABC = {R.id.buttonA, R.id.buttonB, R.id.buttonC};
    private static final int[] DEF = {R.id.buttonD, R.id.buttonE, R.id.buttonF};
    private static final int[] GHI = {R.id.buttonG, R.id.buttonH, R.id.buttonI};
    private static final int[] JKL = {R.id.buttonJ, R.id.buttonK, R.id.buttonL};
    private static final int[] MNO = {R.id.buttonM, R.id.buttonN, R.id.buttonO};
    private static final int[] PQRS = {R.id.buttonP, R.id.buttonQ, R.id.buttonR, R.id.buttonS};
    private static final int[] TUV = {R.id.buttonT, R.id.buttonU, R.id.buttonV};
    private static final int[] WXYZ = {R.id.buttonW, R.id.buttonX, R.id.buttonY, R.id.buttonZ};
    private static final int[] abc = {R.id.buttona, R.id.buttonb, R.id.buttonc};
    private static final int[] def = {R.id.buttond, R.id.buttone, R.id.buttonf};
    private static final int[] ghi = {R.id.buttong, R.id.buttonh, R.id.buttoni};
    private static final int[] jkl = {R.id.buttonj, R.id.buttonk, R.id.buttonl};
    private static final int[] mno = {R.id.buttonm, R.id.buttonn, R.id.buttono};
    private static final int[] pqrs = {R.id.buttonp, R.id.buttonq, R.id.buttonr, R.id.buttons};
    private static final int[] tuv = {R.id.buttont, R.id.buttonu, R.id.buttonv};
    private static final int[] wxyz = {R.id.buttonw, R.id.buttonx, R.id.buttony, R.id.buttonz};

    private TextView[] mViews = null;
    private int mX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        mOk = (Button)findViewById(R.id.ok);
        mBack = (Button)findViewById(R.id.back);

        mPhoneLayout = (LinearLayout) findViewById(R.id.phone_layout);
        mPhoneLayout.setOnClickListener(mEditListener);
        mPasswordLayout = (LinearLayout) findViewById(R.id.password_layout);
        mPasswordLayout.setOnClickListener(mEditListener);

        mPhone = (EditText) findViewById(R.id.phone);
        mPassword = (EditText) findViewById(R.id.password);
        Intent intent = getIntent();
        mPhone.setText(intent.getStringExtra(PHONE));
        mPhone.setSelection(mPhone.getText().toString().length());
        mPassword.setText(intent.getStringExtra(PASSWORD));
        mPassword.setSelection(mPassword.getText().toString().length());

        mNumberSoftkeyboard = (LinearLayout) findViewById(R.id.number_softkeyboard);
        mUpperenSoftkeyboard = (LinearLayout) findViewById(R.id.upperen_softkeyboard);
        mLowerenSoftkeyboard = (LinearLayout) findViewById(R.id.loweren_softkeyboard);
        mSymbolSoftkeyboard = (LinearLayout) findViewById(R.id.symbol_softkeyboard);

        for(int i = 0; i < ROW_COUNT; i++) {
            for(int j = 0; j < COL_COUNT; j++) {
                mNumbers[i][j] = findViewById(NUMBER_BUTTONS[i][j]);
                mNumbers[i][j].setOnClickListener(mButtonListener);
                mUpperens[i][j] = findViewById(UPPEREN_BUTTONS[i][j]);
                mUpperens[i][j].setOnClickListener(mButtonListener);
                mLowerens[i][j] = findViewById(LOWEREN_BUTTONS[i][j]);
                mLowerens[i][j].setOnClickListener(mButtonListener);
                mSymbols[i][j] = findViewById(SYMBOL_BUTTONS[i][j]);
                mSymbols[i][j].setOnClickListener(mButtonListener);
            }
        }

        mCurr = PHONE_INPUT;
        changeMethod(UNKNOW_METHOD);
    }

    private View.OnClickListener mEditListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "mEditListener:view is " + view + ".");
            mCurr = (view.getId() == R.id.password_layout ? 1 : 0);
            if(mCurr == PASSWORD_INPUT) {
                mPhoneLayout.setVisibility(View.GONE);
                mPasswordLayout.setVisibility(View.VISIBLE);
            } else {
                mPhoneLayout.setVisibility(View.VISIBLE);
                mPasswordLayout.setVisibility(View.GONE);
            }
            changeMethod(NUMBER_METHOD);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown:keyCode is " + keyCode);
        if(mMethod == UNKNOW_METHOD) {
            if(keyCode == KEY_LEFT) {
                mOk.setBackgroundResource(R.drawable.bg_list_focuse);
            } else if(keyCode == KEY_RIGHT) {
                mBack.setBackgroundResource(R.drawable.bg_list_focuse);
            } else {
                Log.d(TAG, "onKeyDown:keyCode is " + keyCode);
            }
            return super.onKeyDown(keyCode, event);
        } else if(mMethod == NUMBER_METHOD || mMethod == UPPEREN_METHOD
                || mMethod == LOWEREN_METHOD || mMethod == SYMBOL_METHOD) {
            if(keyCode == KEY_LEFT || keyCode == KEY_RIGHT || keyCode == KEY_UP || keyCode == KEY_DOWN) {
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        } else {
            Log.e(TAG, "onKeyDown:mMethod is " + mMethod);
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp:keyCode is " + keyCode);
        if(mMethod == UNKNOW_METHOD) {
            if(keyCode == KEY_LEFT) {
                mOk.setBackgroundResource(R.drawable.bg_list_normal);
                Intent intent = new Intent();
                intent.putExtra(PHONE, mPhone.getText().toString());
                intent.putExtra(PASSWORD, mPassword.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
                return true;
            } else if(keyCode == KEY_RIGHT) {
                mBack.setBackgroundResource(R.drawable.bg_list_normal);
                setResult(RESULT_CANCELED);
                finish();
                return true;
            } else {
                return super.onKeyUp(keyCode, event);
            }
        } else if(mMethod == NUMBER_METHOD || mMethod == UPPEREN_METHOD
                || mMethod == LOWEREN_METHOD || mMethod == SYMBOL_METHOD) {
            if (keyCode == KEY_LEFT) {
                if (mViews != null) {
                    if (mX > 0) {
                        mX -= 1;
                        mViews[mX].requestFocus();
                    } else {
                        Log.d(TAG, "onKeyUp:mX is 0");
                    }
                } else if (mCol > 0) {
                    if (mRow == ROW_COUNT - 1) {
                        mCol = 0;
                    } else {
                        mCol -= 1;
                    }
                    moveButton(mRow, mCol);
                } else {
                    Log.d(TAG, "onKeyUp:mCol is 0");
                }
                return true;
            } else if (keyCode == KEY_RIGHT) {
                if (mViews != null) {
                    if (mX < mViews.length - 1) {
                        mX += 1;
                        mViews[mX].requestFocus();
                    } else {
                        Log.d(TAG, "onKeyUp:mX is " + mX);
                    }
                } else if (mCol < COL_COUNT - 1) {
                    if (mRow == ROW_COUNT - 1) {
                        mCol = COL_COUNT - 1;
                    } else {
                        mCol += 1;
                    }
                    moveButton(mRow, mCol);
                } else {
                    Log.d(TAG, "onKeyUp:mCol is " + mCol);
                }
                return true;
            } else if (keyCode == KEY_UP) {
                if (mViews != null) {
                    mViews = null;
                    mX = 0;
                    moveButton(mRow, mCol);
                } else if (mRow > 0) {
                    mRow -= 1;
                    moveButton(mRow, mCol);
                } else {
                    Log.d(TAG, "onKeyUp:mRow is 0");
                }
                return true;
            } else if (keyCode == KEY_DOWN) {
                if (mViews != null) {
                    mViews = null;
                    mX = 0;
                    moveButton(mRow, mCol);
                } else if (mRow < ROW_COUNT - 1) {
                    if (mRow == ROW_COUNT - 2 && (mCol == 1 || mCol == 2)) {
                        Log.d(TAG, "onKeyUp:No down.");
                    } else {
                        mRow += 1;
                        moveButton(mRow, mCol);
                    }
                } else {
                    Log.d(TAG, "onKeyUp:mRow is " + mRow);
                }
                return true;
            } else {
                return super.onKeyUp(keyCode, event);
            }
        } else {
            Log.e(TAG, "onKeyUp:mMethod is " + mMethod);
            return super.onKeyUp(keyCode, event);
        }
    }

    private void moveButton(int row, int col) {
        Log.d(TAG, "moveButton:row is " + row + " and col is " + col + ".");
        mRow = row;
        mCol = col;
        if(mMethod == NUMBER_METHOD) {
            mNumbers[mRow][mCol].requestFocus();
        } else if(mMethod == UPPEREN_METHOD) {
            mUpperens[mRow][mCol].requestFocus();
        } else if(mMethod == LOWEREN_METHOD) {
            mLowerens[mRow][mCol].requestFocus();
        } else if(mMethod == SYMBOL_METHOD) {
            mSymbols[mRow][mCol].requestFocus();
        } else {
            Log.e(TAG, "moveButton:method is " + mMethod + ".");
        }
    }

    private View.OnClickListener mButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "mButtonListener:view is " + view + ".");
            for (int i = 0; i < ROW_COUNT; i++) {
                for (int j = 0; j < COL_COUNT; j++) {
                    if(mMethod == NUMBER_METHOD && view.getId() == NUMBER_BUTTONS[i][j]) {
                        inputText(view);
                    } else if(mMethod == UPPEREN_METHOD && view.getId() == UPPEREN_BUTTONS[i][j]) {
                        if(view instanceof LinearLayout) {
                            if(view.getId() == R.id.buttonABC) {
                                inputButton(ABC);
                            } else if(view.getId() == R.id.buttonDEF) {
                                inputButton(DEF);
                            } else if(view.getId() == R.id.buttonGHI) {
                                inputButton(GHI);
                            } else if(view.getId() == R.id.buttonJKL) {
                                inputButton(JKL);
                            } else if(view.getId() == R.id.buttonMNO) {
                                inputButton(MNO);
                            } else if(view.getId() == R.id.buttonPQRS) {
                                inputButton(PQRS);
                            } else if(view.getId() == R.id.buttonTUV) {
                                inputButton(TUV);
                            } else if(view.getId() == R.id.buttonWXYZ) {
                                inputButton(WXYZ);
                            } else {
                                Log.e(TAG, "mButtonListener:LinearLayout is error.");
                            }
                        } else {
                            inputText(view);
                        }
                    } else if(mMethod == LOWEREN_METHOD && view.getId() == LOWEREN_BUTTONS[i][j]) {
                        if(view instanceof LinearLayout) {
                            if(view.getId() == R.id.buttonabc) {
                                inputButton(abc);
                            } else if(view.getId() == R.id.buttondef) {
                                inputButton(def);
                            } else if(view.getId() == R.id.buttonghi) {
                                inputButton(ghi);
                            } else if(view.getId() == R.id.buttonjkl) {
                                inputButton(jkl);
                            } else if(view.getId() == R.id.buttonmno) {
                                inputButton(mno);
                            } else if(view.getId() == R.id.buttonpqrs) {
                                inputButton(pqrs);
                            } else if(view.getId() == R.id.buttontuv) {
                                inputButton(tuv);
                            } else if(view.getId() == R.id.buttonwxyz) {
                                inputButton(wxyz);
                            } else {
                                Log.e(TAG, "mButtonListener:LinearLayout is error.");
                            }
                        } else {
                            inputText(view);
                        }
                    } else if(mMethod == SYMBOL_METHOD && view.getId() == SYMBOL_BUTTONS[i][j]) {
                        inputText(view);
                    } else {
                        Log.d(TAG, "mButtonListener:mMethod is " + mMethod + ".");
                    }
                }
            }
        }
    };

    private View.OnClickListener mViewsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "mViewsListener:view is " + view + ".");
            inputText(view);
        }
    };

    private void changeMethod(int method) {
        Log.d(TAG, "changeMethod:method is " + method + ".");
        mMethod = method;
        mNumberSoftkeyboard.setVisibility(mMethod == NUMBER_METHOD ? View.VISIBLE : View.GONE);
        mUpperenSoftkeyboard.setVisibility(mMethod == UPPEREN_METHOD ? View.VISIBLE : View.GONE);
        mLowerenSoftkeyboard.setVisibility(mMethod == LOWEREN_METHOD ? View.VISIBLE : View.GONE);
        mSymbolSoftkeyboard.setVisibility(mMethod == SYMBOL_METHOD ? View.VISIBLE : View.GONE);
        moveButton(0, 0);
    }

    private void inputButton(int[] buttons) {
        Log.d(TAG, "inputButton:buttons is " + buttons + ".");
        mViews = new TextView[buttons.length];
        for(int i = 0; i < buttons.length; i++) {
            mViews[i] = (TextView) findViewById(buttons[i]);
            mViews[i].setOnClickListener(mViewsListener);
        }
        mX = 0;
        mViews[mX].requestFocus();
    }

    private void inputText(View view) {
        Log.d(TAG, "inputText:view is " + view + ".");
        String text = (mCurr == PASSWORD_INPUT ? mPassword.getText().toString() : mPhone.getText().toString());
        switch(view.getId()) {
            case R.id.buttonOkN:
            case R.id.buttonOkU:
            case R.id.buttonOkL:
            case R.id.buttonOkS:
                changeMethod(UNKNOW_METHOD);
                mPhoneLayout.setVisibility(View.VISIBLE);
                mPasswordLayout.setVisibility(View.VISIBLE);
                if(mCurr == PASSWORD_INPUT) {
                    mPasswordLayout.requestFocus();
                    Intent intent = new Intent();
                    intent.putExtra(PHONE, mPhone.getText().toString());
                    intent.putExtra(PASSWORD, mPassword.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    mPhoneLayout.requestFocus();
                }
                break;

            case R.id.buttonBackN:
            case R.id.buttonBackU:
            case R.id.buttonBackL:
            case R.id.buttonBackS:
                changeMethod(UNKNOW_METHOD);
                mPhoneLayout.setVisibility(View.VISIBLE);
                mPasswordLayout.setVisibility(View.VISIBLE);
                if(mCurr == PASSWORD_INPUT) {
                    mPasswordLayout.requestFocus();
                } else {
                    mPhoneLayout.requestFocus();
                }
                break;

            case R.id.buttonDeleteN:
            case R.id.buttonDeleteU:
            case R.id.buttonDeleteL:
            case R.id.buttonDeleteS:
                if (!text.isEmpty()) {
                    if(mCurr == PASSWORD_INPUT) {
                        mPassword.setText(text.substring(0, text.length() - 1));
                        mPassword.setSelection(mPassword.getText().toString().length());
                    } else {
                        mPhone.setText(text.substring(0, text.length() - 1));
                        mPhone.setSelection(mPhone.getText().toString().length());
                    }
                } else {
                    Log.d(TAG, "inputText:text is empty.");
                }
                break;

            case R.id.buttonNumberU:
            case R.id.buttonNumberL:
            case R.id.buttonNumberS:
                changeMethod(NUMBER_METHOD);
                break;

            case R.id.buttonUpperenN:
            case R.id.buttonUpperenL:
                changeMethod(UPPEREN_METHOD);
                break;

            case R.id.buttonLowerenU:
            case R.id.buttonLowerenS:
                changeMethod(LOWEREN_METHOD);
                break;

            case R.id.buttonSymbolU:
            case R.id.buttonSymbolL:
                changeMethod(SYMBOL_METHOD);
                break;

            case R.id.buttonSpace:
                if(mCurr == PASSWORD_INPUT) {
                    mPassword.setText(text + " ");
                    mPassword.setSelection(mPassword.getText().toString().length());
                } else {
                    mPhone.setText(text + " ");
                    mPhone.setSelection(mPhone.getText().toString().length());
                }
                break;

            case R.id.button0:
            case R.id.button1:
            case R.id.button2:
            case R.id.button3:
            case R.id.button4:
            case R.id.button5:
            case R.id.button6:
            case R.id.button7:
            case R.id.button8:
            case R.id.button9:
            case R.id.buttonAdd:
            case R.id.buttonDot:
            case R.id.buttonSub:
            case R.id.buttonLp:
            case R.id.buttonAt:
            case R.id.buttonPound:
            case R.id.buttonStar:
            case R.id.buttonLine:
                if(mCurr == PASSWORD_INPUT) {
                    mPassword.setText(text + ((TextView)view).getText());
                    mPassword.setSelection(mPassword.getText().toString().length());
                } else {
                    mPhone.setText(text + ((TextView)view).getText());
                    mPhone.setSelection(mPhone.getText().toString().length());
                }
                break;

            case R.id.buttonA:
            case R.id.buttonB:
            case R.id.buttonC:
            case R.id.buttonD:
            case R.id.buttonE:
            case R.id.buttonF:
            case R.id.buttonG:
            case R.id.buttonH:
            case R.id.buttonI:
            case R.id.buttonJ:
            case R.id.buttonK:
            case R.id.buttonL:
            case R.id.buttonM:
            case R.id.buttonN:
            case R.id.buttonO:
            case R.id.buttonP:
            case R.id.buttonQ:
            case R.id.buttonR:
            case R.id.buttonS:
            case R.id.buttonT:
            case R.id.buttonU:
            case R.id.buttonV:
            case R.id.buttonW:
            case R.id.buttonX:
            case R.id.buttonY:
            case R.id.buttonZ:
            case R.id.buttona:
            case R.id.buttonb:
            case R.id.buttonc:
            case R.id.buttond:
            case R.id.buttone:
            case R.id.buttonf:
            case R.id.buttong:
            case R.id.buttonh:
            case R.id.buttoni:
            case R.id.buttonj:
            case R.id.buttonk:
            case R.id.buttonl:
            case R.id.buttonm:
            case R.id.buttonn:
            case R.id.buttono:
            case R.id.buttonp:
            case R.id.buttonq:
            case R.id.buttonr:
            case R.id.buttons:
            case R.id.buttont:
            case R.id.buttonu:
            case R.id.buttonv:
            case R.id.buttonw:
            case R.id.buttonx:
            case R.id.buttony:
            case R.id.buttonz:
                if(mCurr == PASSWORD_INPUT) {
                    mPassword.setText(text + ((TextView)view).getText());
                    mPassword.setSelection(mPassword.getText().toString().length());
                } else {
                    mPhone.setText(text + ((TextView)view).getText());
                    mPhone.setSelection(mPhone.getText().toString().length());
                }
                mViews = null;
                mX = 0;
                moveButton(mRow, mCol);
                break;

            default:
                Log.e(TAG, "inputText:view is error.");
                break;
        }
    }
}
