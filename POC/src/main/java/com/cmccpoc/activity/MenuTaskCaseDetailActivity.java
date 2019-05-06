package com.cmccpoc.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.controller.AirTaskController;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.entity.AirTask;
import com.airtalkee.sdk.entity.AirTaskDetail;
import com.airtalkee.sdk.entity.AirTaskReport;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.PTTFragment;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.CallAlertDialog;
import com.cmccpoc.activity.home.widget.CallCenterDialog;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirTaskCaseManager;
import com.cmccpoc.entity.AirTaskCase;
import com.cmccpoc.listener.OnMmiAirTaskCaseListener;
import com.cmccpoc.listener.OnMmiLocationListener;
import com.cmccpoc.location.AirLocation;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.Util;
import com.cmccpoc.widget.VideoCamera;

import java.util.ArrayList;
import java.util.List;


public class MenuTaskCaseDetailActivity extends ActivityBase implements OnClickListener, AlertDialog.DialogListener, CallCenterDialog.CallCenterDialogListener, OnMmiAirTaskCaseListener
{
	public static final int MODE_VIEW = 0;
	public static final int MODE_NEW = 1;
	public static final int MODE_EDIT = 2;

	public static final String PARAM_TASK_ID = "taskId";
	public static final String PARAM_MODE = "mode";

	public static final int DIALOG_CALL_CENTER_CONFIRM = 100;

	private LinearLayout layoutBottom;
	private Button 	btnTaskReportSelect, btnTaskReportPic, btnTaskReportVid, btmTaskCall;

	private TextView tvTaskCode, tvTaskName, tvTaskNameTitle, tvTaskCar, tvTaskCarTitle, tvTaskDetail, tvTaskDetailTitle;
	private EditText tvTaskCodeEdit, tvTaskCarEdit, tvTaskDetailEdit;

	private RelativeLayout ivRightLay;
	private ImageView ivRight;

	CallCenterDialog dialogCallCenter;
	AlertDialog dialogCall;

	private AirTaskCase mTask = null;
	private int mMode = MODE_VIEW;

	
	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		bundle = getIntent().getExtras();
		if (bundle != null)
		{
			String taskId = bundle.getString(PARAM_TASK_ID);
			if (!TextUtils.isEmpty(taskId))
				mTask = AirTaskCaseManager.getInstance().getTask(taskId);
			mMode = bundle.getInt(PARAM_MODE, MODE_VIEW);
		}
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_task_case_detail);
		doInitView();

		if (mMode == MODE_VIEW && mTask == null)
			finish();
		else
			refreshUI();
	}
	
	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		//if (Config.funcTask)
		//	AirTaskCaseManager.getInstance().setTaskCurrent(null);
		AirTaskCaseManager.getInstance().setTaskCaseListener(null);
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		AirTaskCaseManager.getInstance().setTaskCurrent(mTask);
		AirTaskCaseManager.getInstance().setTaskCaseListener(this);
	}
	
	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		if (Config.funcTask)
			ivTitle.setText(R.string.talk_tools_setting_task);
		else
			ivTitle.setText(R.string.talk_tools_setting_case);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setImageResource(R.drawable.ic_done);
		ivRightLay.setOnClickListener(this);

		layoutBottom = (LinearLayout) findViewById(R.id.talk_layout_task_detail_bottom);

		btnTaskReportSelect = (Button)findViewById(R.id.btn_task_report_select);
		btnTaskReportPic = (Button)findViewById(R.id.btn_task_report_pic);
		btnTaskReportVid = (Button)findViewById(R.id.btn_task_report_vid);
		btmTaskCall = (Button)findViewById(R.id.btn_task_call);
		btnTaskReportSelect.setOnClickListener(this);
		btnTaskReportPic.setOnClickListener(this);
		btnTaskReportVid.setOnClickListener(this);
		btmTaskCall.setOnClickListener(this);
		
		tvTaskCode = (TextView)findViewById(R.id.task_code);
		tvTaskName = (TextView)findViewById(R.id.task_name);
		tvTaskNameTitle = (TextView)findViewById(R.id.task_name_title);
		tvTaskCar = (TextView)findViewById(R.id.task_car);
		tvTaskCarTitle = (TextView)findViewById(R.id.task_car_title);
		tvTaskDetail = (TextView)findViewById(R.id.task_desc);
		tvTaskDetailTitle = (TextView)findViewById(R.id.task_desc_title);
		tvTaskCodeEdit = (EditText)findViewById(R.id.task_code_edit);
		tvTaskCarEdit = (EditText)findViewById(R.id.task_car_edit);
		tvTaskDetailEdit = (EditText)findViewById(R.id.task_desc_edit);

		if (Config.funcTask)
		{
			btmTaskCall.setVisibility(View.GONE);
			tvTaskNameTitle.setText(getString(R.string.talk_task_title));
			tvTaskDetailTitle.setText(getString(R.string.talk_task_content));
			findViewById(R.id.task_code_layout).setVisibility(View.GONE);
			findViewById(R.id.task_car_layout).setVisibility(View.GONE);
			findViewById(R.id.task_name_layout).setVisibility(View.VISIBLE);
		}
	}

	@SuppressWarnings("deprecation")
	protected Dialog onCreateDialog(int id)
	{
		if (id == R.id.talk_dialog_waiting)
		{
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage(getString(R.string.requesting));
			dialog.setCancelable(false);
			return dialog;
		}
		return super.onCreateDialog(id);
	}


	private void refreshUI()
	{
		switch (mMode)
		{
			case MODE_VIEW:
			{

				layoutBottom.setVisibility(View.VISIBLE);
				if (Config.funcTask)
					ivRightLay.setVisibility(View.INVISIBLE);
				else
					ivRightLay.setVisibility(View.VISIBLE);
				ivRight.setImageResource(R.drawable.btn_edit);
				tvTaskCode.setVisibility(View.VISIBLE);
				tvTaskCode.setText(mTask.getCaseCode());
				tvTaskName.setVisibility(View.VISIBLE);
				tvTaskName.setText(mTask.getCaseName());
				tvTaskCar.setVisibility(View.VISIBLE);
				tvTaskCar.setText(mTask.getCarNo());
				tvTaskDetail.setVisibility(View.VISIBLE);
				tvTaskDetail.setText(mTask.getDetail());
				tvTaskCodeEdit.setVisibility(View.GONE);
				tvTaskCarEdit.setVisibility(View.GONE);
				tvTaskDetailEdit.setVisibility(View.GONE);
				break;
			}
			case MODE_NEW:
			{
				ivRightLay.setVisibility(View.VISIBLE);
				ivRight.setImageResource(R.drawable.ic_done);
				layoutBottom.setVisibility(View.GONE);
				tvTaskCode.setVisibility(View.GONE);
				tvTaskName.setVisibility(View.GONE);
				tvTaskCar.setVisibility(View.GONE);
				tvTaskDetail.setVisibility(View.GONE);
				tvTaskCodeEdit.setVisibility(View.VISIBLE);
				tvTaskCodeEdit.setText("");
				tvTaskCarEdit.setVisibility(View.VISIBLE);
				tvTaskCarEdit.setText("");
				tvTaskDetailEdit.setVisibility(View.VISIBLE);
				tvTaskDetailEdit.setText("");
				break;
			}
			case MODE_EDIT:
			{
				ivRightLay.setVisibility(View.VISIBLE);
				ivRight.setImageResource(R.drawable.ic_done);
				layoutBottom.setVisibility(View.GONE);
				tvTaskCode.setVisibility(View.GONE);
				tvTaskName.setVisibility(View.GONE);
				tvTaskCar.setVisibility(View.GONE);
				tvTaskDetail.setVisibility(View.GONE);
				tvTaskCodeEdit.setVisibility(View.VISIBLE);
				tvTaskCodeEdit.setText(mTask.getCaseCode());
				tvTaskCarEdit.setVisibility(View.VISIBLE);
				tvTaskCarEdit.setText(mTask.getCarNo());
				tvTaskDetailEdit.setVisibility(View.VISIBLE);
				tvTaskDetailEdit.setText(mTask.getDetail());
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
				finish();
				break;
			case R.id.talk_menu_right_button:
				if (mMode == MODE_VIEW)
				{
					mMode = MODE_EDIT;
					refreshUI();
				}
				else if (mMode == MODE_NEW)
				{
					if (TextUtils.isEmpty(tvTaskCodeEdit.getText()))
					{
						Toast.makeText(this, "案件号不能为空！", Toast.LENGTH_SHORT).show();
						break;
					}
					else if (TextUtils.isEmpty(tvTaskCarEdit.getText()))
					{
						break;
					}

					showDialog(R.id.talk_dialog_waiting);
					AirTaskCaseManager.getInstance().TaskCreate(tvTaskCodeEdit.getText().toString(), tvTaskCodeEdit.getText().toString(), tvTaskCarEdit.getText().toString(), tvTaskDetailEdit.getText().toString());
					Util.hideSoftInput(this);
				}
				else if (mMode == MODE_EDIT)
				{
					if (TextUtils.isEmpty(tvTaskCodeEdit.getText()))
					{
						Toast.makeText(this, "案件号不能为空！", Toast.LENGTH_SHORT).show();
						break;
					}
					else if (TextUtils.isEmpty(tvTaskCarEdit.getText()))
					{
						break;
					}

					showDialog(R.id.talk_dialog_waiting);
					AirTaskCaseManager.getInstance().TaskUpdate(mTask.getTaskId(), tvTaskCodeEdit.getText().toString(), tvTaskCodeEdit.getText().toString(), tvTaskCarEdit.getText().toString(), tvTaskDetailEdit.getText().toString());
					Util.hideSoftInput(this);
				}
				break;
			case R.id.btn_task_report_select:
			{
				Intent it = new Intent(this, MenuReportAsPicActivity.class);
				it.putExtra("type", "image");
				it.putExtra("taskId", mTask.getTaskId());
				startActivity(it);
				break;
			}
			case R.id.btn_task_report_pic:
			{
				Intent it = new Intent(this, MenuReportAsPicActivity.class);
				it.putExtra("type", "camera");
				it.putExtra("taskId", mTask.getTaskId());
				startActivity(it);
				break;
			}
			case R.id.btn_task_report_vid:
			{
				Intent it = new Intent(this, VideoCamera.class);
				it.putExtra("videoType", 1);
				it.putExtra("taskId", mTask.getTaskId());
				startActivity(it);
				break;
			}
			case R.id.btn_task_call:
			{
				if (Config.funcCenterAttendence)
				{
					dialogCallCenter = new CallCenterDialog(this);
					dialogCallCenter.setListener(this);
					dialogCallCenter.show();
				}
				else
				{
					dialogCall = new AlertDialog(this, getString(R.string.talk_tools_call_center_confirm), null, this, DIALOG_CALL_CENTER_CONFIRM);
					dialogCall.show();
				}
				break;
			}
		}
	}

	private void callStationCenter(int specialNumber, boolean withVideo)
	{
		if (Config.funcCenterCall == AirFunctionSetting.SETTING_ENABLE)
		{
			if (AirtalkeeAccount.getInstance().isAccountRunning())
			{
				if (AirtalkeeAccount.getInstance().isEngineRunning())
				{
					AirLocation.getInstance(this).onceGet(new OnMmiLocationListener()
					{

						@Override
						public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time, String address)
						{
							// TODO Auto-generated method stub
						}

						@Override
						public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time)
						{
							// TODO Auto-generated method stub
						}
					}, 20);

					final AirSession s = SessionController.SessionMatchSpecial(specialNumber, getString(R.string.talk_tools_call_center));
					if (s != null)
					{
						CallAlertDialog alertDialog = new CallAlertDialog(this, "正在呼叫" + s.getDisplayName(), "请稍后...", s.getSessionCode(), PTTFragment.DIALOG_CALL_CENTER, withVideo, new CallAlertDialog.OnAlertDialogCancelListener()
						{
							@Override
							public void onDialogCancel(int reason)
							{
								// TODO Auto-generated method stub
								switch (reason)
								{
									case AirSession.SESSION_RELEASE_REASON_NOTREACH:
									{
										AlertDialog dialog = new AlertDialog(MenuTaskCaseDetailActivity.this, null,
												getString(R.string.talk_call_offline_tip),
												getString(R.string.talk_session_call_cancel),
												getString(R.string.talk_call_leave_msg),
												null,
												PTTFragment.DIALOG_2_SEND_MESSAGE,
												s.getSessionCode());
										dialog.show();
										break;
									}
									default:
										break;
								}
							}
						});
						alertDialog.show();
					}
				}
				else
				{
					Util.Toast(this, getString(R.string.talk_network_warning));
				}
			}
		}
	}

	@Override
	public void onClickOnCallForAdministrator() {
		callStationCenter(AirtalkeeSessionManager.SPECIAL_NUMBER_DISPATCHER, false);
	}

	@Override
	public void onClickOnCallForAttendence() {
		callStationCenter(AirtalkeeSessionManager.SPECIAL_NUMBER_ATTENDENCE, false);
	}

	@Override
	public void onClickOnCallCancel() {

	}

	@Override
	public void onClickOk(int id, Object obj)
	{
		// TODO Auto-generated method stub
		switch (id)
		{
			case DIALOG_CALL_CENTER_CONFIRM:
			{
				callStationCenter(AirtalkeeSessionManager.SPECIAL_NUMBER_DISPATCHER, false);
				break;
			}
		}
	}

	@Override
	public void onClickOk(int id, boolean isChecked)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onClickCancel(int id)
	{
		// TODO Auto-generated method stub

	}


	/********************
	 *
	 * AirTaskListener
	 *
	 *******************/

	@Override
	public void onTaskCaseListGet(boolean isOk, List<AirTaskCase> tasks) {

	}

	@Override
	public void onTaskCaseCreated(boolean isOk, AirTaskCase task) {
		removeDialog(R.id.talk_dialog_waiting);
		if (isOk)
		{
			mTask = task;
			mMode = MODE_VIEW;
			refreshUI();
			Util.Toast(this, "创建成功！");
			AirTaskCaseManager.getInstance().setTaskCurrent(mTask);
		}
		else
			Util.Toast(this, "操作失败，请重新尝试！");
	}

	@Override
	public void onTaskCaseUpdated(boolean isOk, AirTaskCase task) {
		removeDialog(R.id.talk_dialog_waiting);
		if (isOk)
		{
			mTask = task;
			mMode = MODE_VIEW;
			refreshUI();
			Util.Toast(this, "更新成功！");
		}
		else
			Util.Toast(this, "操作失败，请重新尝试！");
	}

	@Override
	public void onTaskCaseDeleted(boolean isOk, AirTaskCase task) {
		// Nothing to do!
	}
}
